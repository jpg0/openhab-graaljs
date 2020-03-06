package org.openhab.automation.module.script.graaljs.internal.commonjs;

@FunctionalInterface
public interface ScriptEngineAware {
    void setScriptEngineProvider(ScriptEngineProvider provider);
}
