package org.openhab.automation.module.script.extension.provider;

import org.openhab.automation.module.script.graaljs.internal.commonjs.LifecycleAware;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

public class LifecycleAwareDelegate<T> implements LifecycleAware {
    private BundleContext bundleContext;

    private T delegate;
    private Class<T> delegateClass;

    private ServiceRegistration<T> registration;

    public LifecycleAwareDelegate(T delegate, Class<T> delegateClass) {
        this(delegate, delegateClass, FrameworkUtil.getBundle(LifecycleAwareDelegate.class).getBundleContext());
    }

    public LifecycleAwareDelegate(T delegate, Class<T> delegateClass, BundleContext bundleContext) {
        this.delegate = delegate;
        this.delegateClass = delegateClass;
        this.bundleContext = bundleContext;
    }

    @Override
    public void onLifecycleEvent(Event e, String scriptIdentifier) {
        switch (e) {
        case LOADED:
            registration = bundleContext.registerService(delegateClass, delegate, null);
            break;
        case DISPOSED:
            registration.unregister();
            break;
        }
    }
}
