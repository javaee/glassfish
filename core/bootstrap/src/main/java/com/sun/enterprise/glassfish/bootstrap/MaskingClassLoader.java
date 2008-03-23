package com.sun.enterprise.glassfish.bootstrap;

import java.util.Collection;
import java.net.URLClassLoader;
import java.net.URL;

/**
 * {@link ClassLoader} that masks a specified set of classes
 * from its parent class loader.
 *
 * <p>
 * This code is used to create an isolated environment.
 *
 * @author Kohsuke Kawaguchi
 */
public class MaskingClassLoader extends ClassLoader {

    private final String[] masks;
    private final URLClassLoader delegate;
    

/*    public MaskingClassLoader(String... masks) {
        this.masks = masks;
    }

    public MaskingClassLoader(Collection<String> masks) {
        this(masks.toArray(new String[masks.size()]));
    }
*/
    public MaskingClassLoader(ClassLoader parent, URL[] urls, String... masks) {
        super(parent);
        this.delegate = new URLClassLoader(urls, getMaskingClassLoader(masks));
        this.masks = masks;
    }

    public MaskingClassLoader(ClassLoader parent, URL[] urls, Collection<String> masks) {
        this(parent, urls, masks.toArray(new String[masks.size()]));
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

        try {
            for (String mask : masks) {
                if (name.startsWith(mask)) {
                    Class c = delegate.loadClass(name);
                    return c;
                }
            }
        } catch(ClassNotFoundException e) {

        }
        return super.loadClass(name, resolve);
/*        for (String mask : masks) {
            if(name.startsWith(mask))
                delegate.loadClass(name);
        }

        return super.loadClass(name, resolve);
        */
     }

    public ClassLoader getMaskingClassLoader(final String... masks) {
        return new ClassLoader() {
            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                for (String mask : masks) {
                    if (name.startsWith(mask)) {
                        throw new ClassNotFoundException(name);
                    }
                }
                return super.loadClass(name, resolve);
            };

        };
    }
}
