/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.automation.module.script.graaljs.internal.commonjs;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.core.ConfigConstants;
import org.openhab.automation.module.script.graaljs.commonjs.internal.FilesystemFolder;
import org.openhab.automation.module.script.graaljs.commonjs.internal.Folder;
import org.openhab.automation.module.script.graaljs.commonjs.internal.JSModule;
import org.openhab.automation.module.script.graaljs.commonjs.internal.Require;
import org.openhab.automation.module.script.graaljs.internal.commonjs.dependency.DependencyTracker;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.Reader;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class which augments a GraalJSScriptEngine by adding CommonJS module support.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
@NonNullByDefault
public class CommonJSScriptEngine {

    private static final String COMMONJS_LIB_PATHS = "graaljs.commonjs.lib.paths";
    public static final String LIB_PATH = String.join(File.separator, ConfigConstants.getConfigFolder(), "automation","lib","javascript");
    private static final String DEFAULT_COMMONJS_LIB_PATHS = String.join(File.pathSeparator,
            String.join(File.separator, LIB_PATH,"personal",""),
            String.join(File.separator, LIB_PATH,"community",""),
            String.join(File.separator, LIB_PATH,"core",""));


    private GraalJSScriptEngine engine;
    private ScriptExtensionModuleProvider scriptExtensionModuleProvider;

    private DependencyTracker dependencyTracker;

    //these fields start as null because they are populated at scriptLoaded time
    @NonNullByDefault({}) private Reader scriptData;
    @NonNullByDefault({}) private String engineIdentifier;

    private CommonJSScriptEngine(GraalJSScriptEngine engine,
            ScriptExtensionModuleProvider scriptExtensionModuleProvider, DependencyTracker dependencyTracker) {
        this.engine = engine;
        this.scriptExtensionModuleProvider = scriptExtensionModuleProvider;
        this.dependencyTracker = dependencyTracker;
    }

    /**
     * Creates an implementation of ScriptEngine (& Invocable), wrapping the contained engine, that tracks the script
     * lifecycle and provides hooks for scripts to do so too.
     *
     * @return a ScriptEngine which logs script exceptions
     */
    public static CommonJSScriptEngine create(GraalJSScriptEngine engine, ScriptExtensionModuleProvider scriptExtensionModuleProvider, DependencyTracker dependencyTracker) {
        return new CommonJSScriptEngine(engine, scriptExtensionModuleProvider, dependencyTracker);
    }

    ScriptEngine createProxy() {
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

    private FilesystemFolder getModuleRoots() {
        return FilesystemFolder.create(new File(System.getenv("OPENHAB_CONF")+"/automation/lib/javascript/core/"), "UTF-8");
    }

    private void configureEngine(){
        String libPathsStr = System.getProperty(COMMONJS_LIB_PATHS);

        if(libPathsStr == null || libPathsStr.equals("")) {
            libPathsStr = DEFAULT_COMMONJS_LIB_PATHS;
        }

        List<Folder> libPaths = Arrays.stream(libPathsStr.split(File.pathSeparator))
                .filter(s -> (s != null && s.length() > 0))
                .map(File::new)
                .filter(f -> f.exists() && f.isDirectory())
                .map(f -> FilesystemFolder.create(f, "UTF-8"))
                .collect(Collectors.toList());

        JSModule root = Require.enable(
                engine.getPolyglotContext(),
                getModuleRoots(),
                engine.getPolyglotContext().eval("js", "this"),
                libPaths,
                scriptExtensionModuleProvider.locatorFor(engine.getPolyglotContext(), engineIdentifier));

        root.addDependencyListener((from, to) -> dependencyTracker.addLibForScript(engineIdentifier, to));
    }

    private void loadScript() throws ScriptException {
        configureEngine();
        engine.eval(scriptData);
        scriptExtensionModuleProvider.notifyScriptLoaded(engineIdentifier);
    }

    private void unloadScript(){
        dependencyTracker.removeScript(engineIdentifier);
    }
}
