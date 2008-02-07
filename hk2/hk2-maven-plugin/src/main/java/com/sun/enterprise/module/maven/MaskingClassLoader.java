package com.sun.enterprise.module.maven;

import java.util.Collection;

/**
 * Hide parent classes.
 * @author Kohsuke Kawaguchi
 */
class MaskingClassLoader extends ClassLoader {

    private final String[] masks;

    public MaskingClassLoader(String... masks) {
        this.masks = masks;
    }

    public MaskingClassLoader(Collection<String> masks) {
        this(masks.toArray(new String[masks.size()]));
    }

    public MaskingClassLoader(ClassLoader parent, String... masks) {
        super(parent);
        this.masks = masks;
    }

    public MaskingClassLoader(ClassLoader parent, Collection<String> masks) {
        this(parent, masks.toArray(new String[masks.size()]));
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return findSystemClass(name);
        } catch (ClassNotFoundException e) {
            // fall through
        }

        for (String mask : masks) {
            if(name.startsWith(mask))
                return super.loadClass(name, resolve);
        }

        throw new ClassNotFoundException();
    }
}
