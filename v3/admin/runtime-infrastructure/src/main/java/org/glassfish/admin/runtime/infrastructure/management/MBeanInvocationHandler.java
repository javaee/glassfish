package org.glassfish.admin.runtime.infrastructure.management;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MBeanInvocationHandler implements InvocationHandler {
    public MBeanInvocationHandler(Object wrapped) {
        this.wrapped = wrapped;
    }

    public Object invoke(Object proxy, Method method, Object[] args)
    throws Throwable {
        Class<?> wrappedClass = wrapped.getClass();
        Method methodInWrapped =
            wrappedClass.getMethod(method.getName(), method.getParameterTypes());
        try {
            return methodInWrapped.invoke(wrapped, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    public Object getWrapped() {
        return this.wrapped;
    }

    private final Object wrapped;
}
