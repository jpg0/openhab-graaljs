package org.openhab.automation.module.script.graaljs.internal.commonjs;

import java.util.HashSet;
import java.util.Set;

public abstract class LifecycleAwareSet implements LifecycleAware {
    private Set<LifecycleAware> lifecycleAwareSet = new HashSet<>();

    protected void addLifecycleAware(LifecycleAware lifecycleAware) {
        lifecycleAwareSet.add(lifecycleAware);
    }

    @Override
    public void onLifecycleEvent(Event e, String scriptIdentifier) {
        lifecycleAwareSet.forEach(l -> l.onLifecycleEvent(e, scriptIdentifier));
    }
}
