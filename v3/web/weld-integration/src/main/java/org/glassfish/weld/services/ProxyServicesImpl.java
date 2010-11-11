/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld.services;

import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.SecureClassLoader;
import java.util.Map;
import java.util.WeakHashMap;

import org.jboss.weld.serialization.spi.ProxyServices;

/**
 * An implementation of the <code>ProxyServices</code> Service.
 * 
 * This implementation uses a delegate classloader that delegates to 
 * the Thread context classloader and the weld bundle classloader for 
 * loading Weld defined proxies. Weld proxies can load not only application 
 * defined classes but also classes exported by weld OSGi bundle. 
 * 
 * @author Sivakumar Thyagarajan
 */
public class ProxyServicesImpl implements ProxyServices {

    //Application ClassLoader vs Proxy Classloader
    private Map<ClassLoader, WeakReference<ClassLoader>> proxyClassLoaders = 
        new WeakHashMap<ClassLoader, WeakReference<ClassLoader>>();

    @Override
    public ClassLoader getClassLoader(final Class<?> proxiedBeanType) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            return AccessController
                    .doPrivileged(new PrivilegedAction<ClassLoader>() {
                        public ClassLoader run() {
                            return _getClassLoader();
                        }
                    });
        } else {
            return _getClassLoader();
        }
    }

    private ClassLoader _getClassLoader() {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        WeakReference<ClassLoader> wr = this.proxyClassLoaders.get(tcl);
        ClassLoader proxyCL = ((wr == null) ? null : wr.get());
        if (proxyCL == null) {
            ClassLoader weldBundleCL = ProxyServices.class.getClassLoader();
            proxyCL = new ProxyClassLoader(tcl /* set as parent */,
                    weldBundleCL);
            this.proxyClassLoaders.put(tcl, new WeakReference<ClassLoader>(
                    proxyCL));
        }
        return proxyCL;
    }

    @Override
    public Class<?> loadBeanClass(final String className) {
        try {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                return (Class<?>) AccessController
                        .doPrivileged(new PrivilegedExceptionAction<Object>() {
                            public Object run() throws Exception {
                                ClassLoader cl = _getClassLoader();
                                return Class.forName(className, true, cl);
                            }
                        });
            } else {
                ClassLoader cl = _getClassLoader();
                return Class.forName(className, true, cl);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void cleanup() {
        // nothing to cleanup in this implementation.
        proxyClassLoaders.clear();
    }

    class ProxyClassLoader extends SecureClassLoader {
        private ClassLoader delegateCL;

        public ProxyClassLoader(ClassLoader parentCL, ClassLoader delegateCL) {
            super(parentCL);
            this.delegateCL = delegateCL;
        }

        @Override
        public Class findClass(String name) throws ClassNotFoundException {
            return delegateCL.loadClass(name);
        }

    }

}