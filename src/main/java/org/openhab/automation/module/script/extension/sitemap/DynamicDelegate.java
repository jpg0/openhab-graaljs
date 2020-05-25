package org.openhab.automation.module.script.extension.sitemap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

public class DynamicDelegate<T> {
    private final Supplier<T> delegateSupplier;
    private final Class<T> forClass;

    private DynamicDelegate(Supplier<T> delegateSupplier, Class<T> forClass) {
        this.delegateSupplier = delegateSupplier;
        this.forClass = forClass;
    }

    public static <T> T wrap(Supplier<T> delegateSupplier, Class<T> forClass) {
        return new DynamicDelegate<T>(delegateSupplier, forClass).proxy();
    }
    
    private T proxy(){
        return (T)Proxy.newProxyInstance(getClass().getClassLoader(), forClass.getInterfaces(), new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(delegateSupplier.get(), args);
            }
        });
    }
}
