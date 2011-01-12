/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.osgijta;

import org.glassfish.osgijavaeebase.Extender;
import org.osgi.framework.BundleContext;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class JTAExtender implements Extender {
    private final BundleContext ctx;

    public JTAExtender(BundleContext ctx) {
        this.ctx = ctx;
    }

    public void start() {
        Class[] classes = {UserTransaction.class, TransactionManager.class, TransactionSynchronizationRegistry.class};
        String[] jndiNames = {"UserTransaction", "java:appserver/TransactionManager", "java:appserver/TransactionSynchronizationRegistry"};
        for (int i = 0; i < 3; ++i) {
            registerProxy(classes[i], jndiNames[i]);
        }
    }

    private void registerProxy(Class clazz, String jndiName) {
        InvocationHandler ih = new MyInvocationHandler(clazz, jndiName);
        Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{clazz}, ih);
        ctx.registerService(clazz.getName(), proxy, null);
    }

    public void stop() {
    }

    private class MyInvocationHandler implements InvocationHandler {
        private Class<?> clazz;
        private String jndiName;

        private MyInvocationHandler(Class<?> clazz, String jndiName) {
            this.clazz = clazz;
            this.jndiName = jndiName;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                InitialContext ic = getInitialContext();
                Object target = ic.lookup(jndiName);
                try {
                    return method.invoke(target, args);
                } catch (InvocationTargetException e) {
                    // We need to unwrap the real exception and throw it
                    throw e.getCause();
                }
            } catch (NamingException e) {
                throw new RuntimeException("JTA Service is not available.", e);
            }
        }
    }

    private InitialContext getInitialContext() throws NamingException {
        return new InitialContext();
    }
}
