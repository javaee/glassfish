/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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


package org.glassfish.weld;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.jboss.weld.bootstrap.api.SingletonProvider;
import org.jboss.weld.bootstrap.api.helpers.TCCLSingletonProvider;

import java.security.AccessController;
import java.security.PrivilegedAction;


/**
 * This is a bundle activator which is responsible for configuring Weld bundle to be used in GlassFish.
 * As part of configuration, it configures the the SingletonProvider in Weld. It sets different SingletonProvider
 * for different profiles. e.g., in WebProfile, it sets
 * {@link org.jboss.weld.bootstrap.api.helpers.TCCLSingletonProvider}, where as
 * for full-javaee profile, it uses {@link org.glassfish.weld.ACLSingletonProvider}.
 * It tests profile by testing existence of
 * {@link org.glassfish.javaee.full.deployment.EarClassLoader}.
 *
 * As part of configuration of Weld, it also sets appropriate ClassLoaderProvider to be used by javassist.
 * We rely on using TCL for javassist defined proxies because they can load not only application defined classes
 * but also classes exported by any OSGi bundle as long as the operation is happening in the context of a Java EE app.
 *
 * It resets them in stop().
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class WeldActivator implements BundleActivator
{
    private javassist.util.proxy.ProxyFactory.ClassLoaderProvider oldCLP;

    public void start(BundleContext context) throws Exception
    {
        boolean earSupport = false;
        try {
            Class.forName("org.glassfish.javaee.full.deployment.EarClassLoader");
            earSupport = true;
        } catch (ClassNotFoundException cnfe) {
        }
        SingletonProvider.initialize(earSupport ?
                new ACLSingletonProvider() : new TCCLSingletonProvider());
        oldCLP = javassist.util.proxy.ProxyFactory.classLoaderProvider;
        javassist.util.proxy.ProxyFactory.classLoaderProvider = new GlassFishClassLoaderProvider();
        System.out.println("javassist.util.proxy.ProxyFactory.classLoaderProvider = " + javassist.util.proxy.ProxyFactory.classLoaderProvider);
    }

    public void stop(BundleContext context) throws Exception
    {
        SingletonProvider.reset();
        javassist.util.proxy.ProxyFactory.classLoaderProvider = oldCLP;
    }

    private static class GlassFishClassLoaderProvider implements javassist.util.proxy.ProxyFactory.ClassLoaderProvider {
        public java.lang.ClassLoader get(javassist.util.proxy.ProxyFactory proxyFactory) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                    public ClassLoader run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                });
            } else {
                return Thread.currentThread().getContextClassLoader();
            }
        }
    }
}
