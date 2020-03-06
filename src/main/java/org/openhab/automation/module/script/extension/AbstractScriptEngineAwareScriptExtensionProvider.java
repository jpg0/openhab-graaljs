package org.openhab.automation.module.script.extension;

import org.graalvm.polyglot.Engine;
import org.openhab.automation.module.script.graaljs.internal.commonjs.ScriptEngineAware;
import org.openhab.automation.module.script.graaljs.internal.commonjs.ScriptEngineProvider;

public abstract class AbstractScriptEngineAwareScriptExtensionProvider extends AbstractScriptExtensionProvider implements
        ScriptEngineAware, ScriptEngineProvider {
    private ScriptEngineProvider delegate;

    @Override
    public void setScriptEngineProvider(ScriptEngineProvider provider) {
        this.delegate = provider;
    }

    @Override
    public Engine getEngineForIdentifier(String engineIdentifier) {
        return delegate.getEngineForIdentifier(engineIdentifier);
    }
}
