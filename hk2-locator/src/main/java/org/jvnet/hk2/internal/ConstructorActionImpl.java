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

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.glassfish.hk2.utilities.reflection.Logger;

/**
 * @author jwells
 *
 */
final class ConstructorActionImpl<T> implements ConstructorAction {
    private final static MethodFilter METHOD_FILTER = new MethodFilter() {

        @Override
        public boolean isHandled(Method method) {
            // We do not allow interception of finalize
            if (method.getName().equals("finalize")) return false;
            
            return true;
        }
        
    };
    
    /**
     * 
     */
    private final ClazzCreator<T> clazzCreator;
    
    /**
     * 
     */
    private final Map<Method, List<MethodInterceptor>> methodInterceptors;

    /**
     * @param methodInterceptors
     * @param clazzCreator TODO
     */
    ConstructorActionImpl(
            ClazzCreator<T> clazzCreator, Map<Method, List<MethodInterceptor>> methodInterceptors) {
        this.clazzCreator = clazzCreator;
        this.methodInterceptors = methodInterceptors;
    }

    @Override
    public Object makeMe(final Constructor<?> c, final Object[] args, final boolean neutralCCL)
            throws Throwable {
        final MethodInterceptorHandler methodInterceptor = new MethodInterceptorHandler(this.clazzCreator.locator, methodInterceptors);
            
        final ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(this.clazzCreator.implClass);
        proxyFactory.setFilter(METHOD_FILTER);
        
        return AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

            @Override
            public Object run() throws Exception {
                ClassLoader currentCCL = null;
                if (neutralCCL) {
                    currentCCL = Thread.currentThread().getContextClassLoader();
                }
          
                try {
                  return proxyFactory.create(c.getParameterTypes(), args, methodInterceptor);
                }
                catch (InvocationTargetException ite) {
                    Throwable targetException = ite.getTargetException();
                    Logger.getLogger().debug(c.getDeclaringClass().getName(), c.getName(), targetException);
                    if (targetException instanceof Exception) {
                        throw (Exception) targetException;
                    }
                    throw new RuntimeException(targetException);
                }
                finally {
                    if (neutralCCL) {
                        Thread.currentThread().setContextClassLoader(currentCCL);
                    }
                }
            }
                
        });
    }
}