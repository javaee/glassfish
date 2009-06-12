package org.glassfish.extras.osgicontainer;

import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.glassfish.internal.api.DelegatingClassLoader;

import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import com.sun.enterprise.module.Module;

/**
 * Reference counting class loader
 *
 * @author Jerome Dochez
 */

public class RefCountingClassLoader extends DelegatingClassLoader implements PreDestroy {

    volatile int refs = 0;
    final Module module;

    public RefCountingClassLoader(ClassLoader parent,  Module module) {
        super(parent);
        this.addDelegate(new ProtectedClassLoader(parent, module.getClassLoader()));
        this.module = module;
    }
    
    public void increment() {
        refs++;
    }

    public synchronized void decrement() {
        refs--;
        if (refs==0) {
            module.stop();
            module.uninstall();
        }
    }


    public void preDestroy() {
        decrement();
    }


    private final static class ProtectedClassLoader implements DelegatingClassLoader.ClassFinder {

        final ClassLoader parent;
        final ClassLoader delegate;

        public ProtectedClassLoader(ClassLoader parent, ClassLoader delegate) {
            this.parent = parent;
            this.delegate = delegate;
        }

        public ClassLoader getParent() {
            return parent;
        }

        public Class<?> findClass(String name) throws ClassNotFoundException {
            return delegate.loadClass(name);
        }

        public Class<?> findExistingClass(String name) {
            try {
                return delegate.loadClass(name);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        public URL findResource(String name) {
            return delegate.getResource(name);
        }

        public Enumeration<URL> findResources(String name) throws IOException {
            return delegate.getResources(name);
        }
    }    
}
