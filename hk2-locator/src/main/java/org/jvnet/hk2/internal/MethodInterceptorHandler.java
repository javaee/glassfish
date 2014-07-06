/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.glassfish.hk2.api.AOPProxyCtl;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.HK2Invocation;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;

import javassist.util.proxy.MethodHandler;

/**
 * This is the handler that runs the aopalliance method interception
 * 
 * @author jwells
 *
 */
public class MethodInterceptorHandler implements MethodHandler {
    private final ServiceLocatorImpl locator;
    private final Map<Method, List<MethodInterceptor>> interceptorLists;
    private final ActiveDescriptor<?> underlyingDescriptor;
    
    /* package */ MethodInterceptorHandler(ServiceLocatorImpl locator,
            ActiveDescriptor<?> underlyingDescriptor,
            Map<Method, List<MethodInterceptor>> interceptorLists) {
        this.locator = locator;
        this.interceptorLists = interceptorLists;
        this.underlyingDescriptor = underlyingDescriptor;
    }

    /* (non-Javadoc)
     * @see javassist.util.proxy.MethodHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args)
            throws Throwable {
        if (thisMethod.getName().equals(AOPProxyCtl.UNDERLYING_METHOD_NAME)) {
            return underlyingDescriptor;
        }
        
        List<MethodInterceptor> interceptors = interceptorLists.get(thisMethod);
        if (interceptors == null || interceptors.isEmpty()) {
            return ReflectionHelper.invoke(self, proceed, args, locator.getNeutralContextClassLoader());
        }
        
        if (!(interceptors instanceof RandomAccess)) {
            // Make sure we are indexable
            interceptors = new ArrayList<MethodInterceptor>(interceptors);
        }
        
        MethodInterceptor nextInterceptor = interceptors.get(0);
        
        return nextInterceptor.invoke(new MethodInvocationImpl(args,
                thisMethod, self, interceptors, 0, proceed, null));
    }
    
    private class MethodInvocationImpl implements MethodInvocation, HK2Invocation {
        private final Object[] arguments;  // Live!
        private final Method method;
        private final Object myself;
        private final List<MethodInterceptor> interceptors;
        private final int index;
        private final Method proceed;
        private HashMap<String, Object> userData;
        
        private MethodInvocationImpl(Object[] arguments,
                Method method,
                Object myself,
                List<MethodInterceptor> interceptors,
                int index,
                Method proceed,
                HashMap<String, Object> userData) {
            this.arguments = arguments;
            this.method = method;
            this.myself = myself;
            this.interceptors = interceptors;
            this.index = index;
            this.proceed = proceed;
            this.userData = userData;
        }

        @Override
        public Object[] getArguments() {
            return arguments;
        }

        @Override
        public AccessibleObject getStaticPart() {
            return method;
        }

        @Override
        public Object getThis() {
            return myself;
        }

        @Override
        public Method getMethod() {
            return method;
        }
        
        @Override
        public Object proceed() throws Throwable {
            int newIndex = index + 1;
            if (newIndex >= interceptors.size()) {
                // Call the actual method
                return ReflectionHelper.invoke(myself, proceed, arguments, locator.getNeutralContextClassLoader());
            }
            
            // Invoke the next interceptor
            MethodInterceptor nextInterceptor = interceptors.get(newIndex);
            
            return nextInterceptor.invoke(new MethodInvocationImpl(arguments,
                    method, myself, interceptors, newIndex, proceed, userData));
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.api.HK2Invocation#setUserData(java.lang.String, java.lang.Object)
         */
        @Override
        public void setUserData(String key, Object data) {
            if (key == null) throw new IllegalArgumentException();
            
            if (userData == null) userData = new HashMap<String, Object>();
            
            if (data == null) {
                userData.remove(key);
            }
            else {
                userData.put(key, data);
            }
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.api.HK2Invocation#getUserData(java.lang.String)
         */
        @Override
        public Object getUserData(String key) {
            if (key == null) throw new IllegalArgumentException();
            
            if (userData == null) return null;
            return userData.get(key);
        }
        
    }

}
