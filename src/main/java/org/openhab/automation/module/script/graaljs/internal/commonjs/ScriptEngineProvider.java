package org.openhab.automation.module.script.graaljs.internal.commonjs;

import org.graalvm.polyglot.Engine;

@FunctionalInterface
public interface ScriptEngineProvider {
    Engine getEngineForIdentifier(String engineIdentifier);
}
