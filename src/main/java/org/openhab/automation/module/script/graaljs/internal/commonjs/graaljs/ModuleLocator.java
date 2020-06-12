package org.openhab.automation.module.script.graaljs.internal.commonjs.graaljs;

import org.graalvm.polyglot.Value;

import java.util.Optional;

public interface ModuleLocator {
    Optional<Value> locateModule(String name);
}
