package org.glassfish.admin.runtime.infrastructure.management;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

public class MBeanBuilder {
    private final InterfaceClassLoader loader;

    public MBeanBuilder(ClassLoader parentLoader) {
        loader = new InterfaceClassLoader(parentLoader);
    }

    public XStandardMBean buildMBean(Object x) {
        Class<?> c = x.getClass();
        Class<?> mbeanInterface = makeInterface(c);
        InvocationHandler handler = new MBeanInvocationHandler(x);
        return makeStandardMBean(mbeanInterface, handler);
    }

    private static <T> XStandardMBean makeStandardMBean(Class<T> intf,
                                                       InvocationHandler handler) {
        Object proxy =
                Proxy.newProxyInstance(intf.getClassLoader(),
                                       new Class<?>[] {intf},
                                       handler);
        T impl = intf.cast(proxy);
        try {
            return new XStandardMBean(impl, intf);
        } catch (NotCompliantMBeanException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Class makeInterface(Class implClass) {
        String interfaceName = implClass.getName() + "$WrapperMBean";
        try {
            return Class.forName(interfaceName, false, loader);
        } catch (ClassNotFoundException e) {
            // OK, we'll build it
        }
        Set<XMethod> methodSet = new LinkedHashSet<XMethod>();
        for (Method m : implClass.getMethods()) {
            if (m.isAnnotationPresent(ManagedAttribute.class) ||
                m.isAnnotationPresent(ManagedOperation.class)) {
                methodSet.add(new XMethod(m));
            }
        }
        if (methodSet.isEmpty()) {
            throw new IllegalArgumentException(
                "Class has no @ManagedOperation or @ManagedAttribute methods: "
                + implClass);
        }
        XMethod[] methods = methodSet.toArray(new XMethod[0]);
        return loader.findOrBuildInterface(interfaceName, methods);
    }
}
