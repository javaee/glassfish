package com.sun.enterprise.connectors.util;

public class ClassLoadingUtility {

    /**
     * Get the current thread's context class loader which is set to
     * the CommonClassLoader by ApplicationServer
     *
     * @return the thread's context classloader if it exists;
     *         else the system class loader.
     */
    private static ClassLoader getClassLoader() {
        if (Thread.currentThread().getContextClassLoader() != null) {
            return Thread.currentThread().getContextClassLoader();
        } else {
            return ClassLoader.getSystemClassLoader();
        }
    }

    /**
     * Loads the class with the common class loader.
     *
     * @param the class name
     * @return the loaded class
     * @throws if the class is not found.
     * @see getClassLoader()
     */
    public static Class loadClass(String className) throws ClassNotFoundException {
        return getClassLoader().loadClass(className);
    }
}
