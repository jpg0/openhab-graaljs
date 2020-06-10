package org.openhab.automation.module.script.graaljs.internal.loader;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;

public class LoaderUtils {

    public static void withContextClassloader(LoaderBlock fn) {
        withContextClassloader(fn, Function.identity());
    }

    public static void withContextClassloader(LoaderBlock fn, Function<ClassLoader, ClassLoader> modifyClassloader) {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        ClassLoader replacement = modifyClassloader.apply(LoaderUtils.class.getClassLoader());

        try {
            Thread.currentThread().setContextClassLoader(replacement);
            fn.load();
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    public static <T> T wrapAccessInClassloader(T toWrap, Class<? super T>[] interfaces) {
        return (T)Proxy.newProxyInstance(LoaderUtils.class.getClassLoader(), interfaces, new InvocationHandler() {
            @Override
            public Object invoke(Object o, Method method, Object[] args) throws Throwable {
                Object[] returnOrError = new Object[2];

                withContextClassloader(() -> {
                    try {
                        returnOrError[0] = method.invoke(toWrap, args);
                    } catch (Exception e) {
                        returnOrError[1] = e;
                    }
                });

                if(returnOrError[1] != null){
                    throw new InvocationTargetException((Exception)returnOrError[1]);
                } else {
                    return returnOrError[0];
                }
            }
        });
    }

    @FunctionalInterface
    public interface LoaderBlock<T> {
        void load();
    }
}