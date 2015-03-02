/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.jvnet.hk2.internal;

import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.WeakHashMap;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ProxyCtl;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

/**
 * Utilities around proxying
 * 
 * @author jwells
 *
 */
public class ProxyUtilities {
    private final static Object proxyCreationLock = new Object();
    private final static WeakHashMap<WeakDelegatingCacheEntry, ServiceLocator> delegatingLoaderSet =
            new WeakHashMap<WeakDelegatingCacheEntry, ServiceLocator>();
    
    /**
     * We put the anchor as the value even though we don't use it in order to
     * make it easier to catch memory leaks here.  See MemoryTest
     * 
     * @param superclass
     * @param interfaces
     * @param callback
     * @param useJDKProxy
     * @param anchor This is put into the WeakMap to make sure that IF this
     * map should leak that it will leak big, making it easier to detect
     * @return
     */
    private static <T> T secureCreate(final Class<?> superclass,
            final Class<?>[] interfaces,
            final MethodHandler callback,
            boolean useJDKProxy,
            ServiceLocator anchor) {

        /* construct the classloader where the generated proxy will be created --
         * this classloader must have visibility into the javaassist classloader as well as
         * the superclass' classloader
         */
        ClassLoader myLoader;
        synchronized (delegatingLoaderSet) {
            ClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

                @Override
                public ClassLoader run() {
                    ClassLoader retVal = superclass.getClassLoader();
                    if (retVal == null) retVal = ClassLoader.getSystemClassLoader();
                    if (retVal == null) {
                        throw new IllegalStateException("Could not find system classloader or classloader of " + superclass.getName());
                    }
                    
                    return retVal;
                }
                
            });
            
            WeakDelegatingCacheEntry key = new WeakDelegatingCacheEntry(loader);
            WeakDelegatingCacheEntry found = null;
            for (WeakDelegatingCacheEntry entry : delegatingLoaderSet.keySet()) {
                if (key.equals(entry)) {
                    found = entry;
                    break;
                }
            }
            
            if (found == null) {
                found = key;
                
                delegatingLoaderSet.put(found, anchor);
            }
            
            myLoader = found.getDelegator();
        }
        final ClassLoader delegatingLoader = myLoader;

        if (useJDKProxy) {
            return AccessController.doPrivileged(new PrivilegedAction<T>() {

                @SuppressWarnings("unchecked")
                @Override
                public T run() {
                    return (T) Proxy.newProxyInstance(delegatingLoader, interfaces,
                            new MethodInterceptorInvocationHandler(callback));
                }

            });

        }


        return AccessController.doPrivileged(new PrivilegedAction<T>() {

            @SuppressWarnings("unchecked")
            @Override
            public T run() {
                synchronized (proxyCreationLock) {
                    ProxyFactory.ClassLoaderProvider originalProvider = ProxyFactory.classLoaderProvider;
                    ProxyFactory.classLoaderProvider = new ProxyFactory.ClassLoaderProvider() {
                        
                        @Override
                        public ClassLoader get(ProxyFactory arg0) {
                            return delegatingLoader;
                        }
                    };
                    
                    try {
                        ProxyFactory proxyFactory = new ProxyFactory();
                        proxyFactory.setInterfaces(interfaces);
                        proxyFactory.setSuperclass(superclass);

                        Class<?> proxyClass = proxyFactory.createClass();

                        try {
                            T proxy = (T) proxyClass.newInstance();

                            ((ProxyObject) proxy).setHandler(callback);

                            return proxy;
                        } catch (Exception e1) {
                            throw new RuntimeException(e1);
                        }
                    }
                    finally {
                        ProxyFactory.classLoaderProvider = originalProvider;
                        
                    }
                }
            }

        });

    }
    
    @SuppressWarnings("unchecked")
    public static <T> T generateProxy(Class<?> requestedClass,
            ServiceLocatorImpl locator,
            ActiveDescriptor<T> root,
            ServiceHandle<T> handle) {
        boolean isInterface = (requestedClass == null) ? false : requestedClass.isInterface() ;

        final Class<?> proxyClass;
        Class<?> iFaces[];
        if (isInterface) {
            proxyClass = requestedClass;
            iFaces = new Class<?>[2];
            iFaces[0] = proxyClass;
            iFaces[1] = ProxyCtl.class;
        }
        else {
            proxyClass = Utilities.getFactoryAwareImplementationClass(root);

            iFaces = Utilities.getInterfacesForProxy(root.getContractTypes());
        }

        T proxy;
        try {
            proxy = (T) secureCreate(proxyClass,
                iFaces,
                new MethodInterceptorImpl(locator, root, handle),
                isInterface, locator);
        }
        catch (Throwable th) {
            Exception addMe = new IllegalArgumentException("While attempting to create a Proxy for " + proxyClass.getName() +
                    " in proxiable scope " + root.getScope() + " an error occured while creating the proxy");

            if (th instanceof MultiException) {
                MultiException me = (MultiException) th;

                me.addError(addMe);

                throw me;
            }

            MultiException me = new MultiException(th);
            me.addError(addMe);
            throw me;
        }

        return proxy;
    }
    
    /**
     * The point of this class is to reside in a WeakHashMap as the
     * key and thus only have weak references to the classloaders
     * pointed to by this object.  It also reduces the number of
     * classloaders that are created by calculating the DelegatingClassLoader
     * only once, though the performance benefit of that is probably
     * out-weighed by the fact that we have to linearly go through
     * the WeakHashMap keys to get back the original key object
     * 
     * @author jwells
     *
     */
    private static class WeakDelegatingCacheEntry {
        private final ClassLoader key;
        private DelegatingClassLoader loader;
        
        private WeakDelegatingCacheEntry(ClassLoader key) {
            this.key = key;
        }
        
        private DelegatingClassLoader getDelegator() {
            if (loader != null) return loader;
            
            final Object backPointer = this;
            loader = AccessController
                .doPrivileged(new PrivilegedAction<DelegatingClassLoader>() {

                @Override
                public DelegatingClassLoader run() {
                    // create a delegating classloader that attempts to
                    // load from the superclass' classloader first,
                    // then hk2-locator's classloader second.
                    return new DelegatingClassLoader(
                            key,
                            backPointer,
                            ProxyFactory.class.getClassLoader(),
                            ProxyCtl.class.getClassLoader());
                }
            });
            
            return loader;
            
        }
        
        @Override
        public int hashCode() {
            return key.hashCode();
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof WeakDelegatingCacheEntry)) return false;
            
            WeakDelegatingCacheEntry other = (WeakDelegatingCacheEntry) o;
            
            return key.equals(other.key);
        }
    }
}
