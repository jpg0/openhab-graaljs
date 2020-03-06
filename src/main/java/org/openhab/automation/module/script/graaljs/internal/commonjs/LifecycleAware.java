package org.openhab.automation.module.script.graaljs.internal.commonjs;

public interface LifecycleAware {
    enum Event {
        LOADED, DISPOSED
    }

    void onLifecycleEvent(Event e, String scriptIdentifier);
}
