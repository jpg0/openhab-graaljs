package org.openhab.automation.module.script.graaljs.internal.commonjs.graaljs;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.graalvm.polyglot.Context;
import org.openhab.automation.module.script.graaljs.internal.commonjs.ScriptExtensionModuleProvider;
import org.openhab.automation.module.script.graaljs.internal.commonjs.dependency.DependencyTracker;
import org.openhab.automation.module.script.graaljs.internal.commonjs.graaljs.fs.DelegatingFileSystem;
import org.openhab.automation.module.script.graaljs.internal.commonjs.graaljs.fs.PrefixedSeekableByteChannel;
import org.eclipse.smarthome.config.core.ConfigConstants;

import javax.script.*;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Proxy;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;
import java.util.function.Function;

public class GraalJSCommonJSScriptEngine {

    private GraalJSScriptEngine engine;
    private ScriptExtensionModuleProvider scriptExtensionModuleProvider;

    private DependencyTracker dependencyTracker;

    private static final String REQUIRE_WRAPPER_NAME = "__wraprequire__";

    //these fields start as null because they are populated at scriptLoaded time
    @NonNullByDefault({}) private Reader scriptData;
    @NonNullByDefault({}) private String engineIdentifier;

    private GraalJSCommonJSScriptEngine(ScriptExtensionModuleProvider scriptExtensionModuleProvider, DependencyTracker dependencyTracker) {
        this.engine = GraalJSScriptEngine.create(null,
                Context.newBuilder("js")
                        .allowExperimentalOptions(true)
                        .allowAllAccess(true)
                        .option("js.commonjs-require-cwd", String.join(File.separator, ConfigConstants.getConfigFolder(), "automation","lib","javascript", "personal"))
                        .option("js.nashorn-compat", "true") //to ease migration
                        .option("js.commonjs-require", "true") //enable CommonJS module support
                        .fileSystem(new DelegatingFileSystem(FileSystems.getDefault().provider()){
                            @Override
                            public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options,
                                    FileAttribute<?>... attrs) throws IOException {
                                onLibLoaded(path.toString());

                                if(path.toString().endsWith(".js")) {
                                    return new PrefixedSeekableByteChannel(("require=" + REQUIRE_WRAPPER_NAME + "(require);").getBytes(), super.newByteChannel(path, options, attrs));
                                } else {
                                    return super.newByteChannel(path, options, attrs);
                                }
                            }
                        }));


        Function<Function<Object[], Object>, Function<String, Object>> wrapRequireFn = originalRequireFn -> moduleName -> scriptExtensionModuleProvider
                    .locatorFor(engine.getPolyglotContext(), engineIdentifier)
                    .locateModule(moduleName)
                    .map(m -> (Object) m)
                    .orElseGet(() -> originalRequireFn.apply(new Object[]{moduleName}));

        Bindings wrapper = new SimpleBindings();
        wrapper.put(REQUIRE_WRAPPER_NAME, wrapRequireFn);
        this.engine.setBindings(wrapper, ScriptContext.GLOBAL_SCOPE);

        this.engine.put("require", wrapRequireFn.apply((Function<Object[], Object>) this.engine.get("require")));


        this.scriptExtensionModuleProvider = scriptExtensionModuleProvider;
        this.dependencyTracker = dependencyTracker;
    }

    private void onLibLoaded(String libPath) {
        dependencyTracker.addLibForScript(this.engineIdentifier, libPath);
    }

    /**
     * Creates an implementation of ScriptEngine (& Invocable), wrapping the contained engine, that tracks the script
     * lifecycle and provides hooks for scripts to do so too.
     *
     * @return a ScriptEngine which logs script exceptions
     */
    public static GraalJSCommonJSScriptEngine create(ScriptExtensionModuleProvider scriptExtensionModuleProvider, DependencyTracker dependencyTracker) {
        return new GraalJSCommonJSScriptEngine(scriptExtensionModuleProvider, dependencyTracker);
    }

    public ScriptEngine createProxy() {
        return (ScriptEngine) Proxy.newProxyInstance(
                ScriptEngine.class.getClassLoader(),
                new Class<?>[]{ScriptEngine.class, Invocable.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("eval")) {
                        scriptData = (Reader)args[0];
                        return null;
                    } else if(method.getName().equals("invokeFunction")) {
                        if(args[0].equals("scriptLoaded")) {
                            engineIdentifier = (String)((Object[])args[1])[0];
                            loadScript();
                            return null;
                        } else if (args[0].equals("scriptUnloaded")) {
                            unloadScript();
                            return null;
                        } else {
                            throw new UnsupportedOperationException("Only supports invocation of scriptLoaded and scriptUnloaded functions");
                        }
                    } else if(method.getName().startsWith("get")) { //allow non-mutative calls
                        return method.invoke(engine, args);
                    }

                    throw new UnsupportedOperationException("Only eval & invokeFunction supported for this script engine (" + method.getName() + " called)");
                });
    }

    private void loadScript() throws ScriptException {
        engine.eval(scriptData);
        scriptExtensionModuleProvider.notifyScriptLoaded(engineIdentifier);
    }

    private void unloadScript(){
        dependencyTracker.removeScript(engineIdentifier);
    }
}
