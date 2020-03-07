package org.openhab.automation.module.script.extension.rule;

import org.openhab.automation.module.script.extension.provider.SuspendableFixedProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component(service = ScriptRuleProviderManager.class)
public class ScriptRuleProviderManager {

    private List<ScriptRuleProvider> scriptRuleProviders = new CopyOnWriteArrayList<>();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "removeScriptRuleProvider")
    public void addScriptRuleProvider(ScriptRuleProvider scriptRuleProvider) {
        scriptRuleProviders.add(scriptRuleProvider);
    }

    public void removeScriptRuleProvider(ScriptRuleProvider scriptRuleProvider) {
        scriptRuleProviders.remove(scriptRuleProvider);
    }

    public void suspendAll() {
        scriptRuleProviders.forEach(SuspendableFixedProvider::suspend);
    }

    public void resumeAll() {
        scriptRuleProviders.forEach(SuspendableFixedProvider::resume);
    }
}
