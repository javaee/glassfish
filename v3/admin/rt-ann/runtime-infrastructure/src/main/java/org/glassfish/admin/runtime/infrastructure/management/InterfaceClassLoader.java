package org.glassfish.admin.runtime.infrastructure.management;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * A ClassLoader that builds arbitrary interfaces containing arbitrary lists
 * of methods.
 */
public class InterfaceClassLoader extends ClassLoader {
    public InterfaceClassLoader(ClassLoader parent) {
        super(parent);
    }
    
    public Class<?> findOrBuildInterface(String name, XMethod[] methods) {
        Class<?> c;
        c = findLoadedClass(name);
        if (c != null)
            return c;
        byte[] classBytes = InterfaceBuilder.buildInterface(name, methods);
        return defineClass(name, classBytes, 0, classBytes.length);
    }
}
