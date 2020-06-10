package org.openhab.automation.module.script.graaljs.internal.commonjs.graaljs;

import org.openhab.automation.module.script.graaljs.internal.commonjs.ScriptExtensionModuleProvider;
import org.openhab.automation.module.script.graaljs.internal.commonjs.dependency.DependencyTracker;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.script.ScriptEngine;

/**
 * @author Jonathan Gilbert
 */
@Component(service = GraalJSCommonJSScriptEngineManager.class)
public class GraalJSCommonJSScriptEngineManager {
    private DependencyTracker dependencyTracker;

    @Reference
    public void setDependencyTracker(DependencyTracker dependencyTracker) {
        this.dependencyTracker = dependencyTracker;
    }

    public ScriptEngine create(ScriptExtensionModuleProvider scriptExtensionModuleProvider) {
        GraalJSCommonJSScriptEngine commonJSScriptEngine = GraalJSCommonJSScriptEngine
                .create(scriptExtensionModuleProvider, dependencyTracker);
        return commonJSScriptEngine.createProxy();
    }
}
