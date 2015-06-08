/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
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
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.RandomAccess;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.ConstructorInvocation;
import org.glassfish.hk2.api.HK2Invocation;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;

/**
 * @author jwells
 *
 */
public class ConstructorInterceptorHandler {
    private static final ConstructorAction DEFAULT_ACTION = new ConstructorAction() {

        @Override
        public Object makeMe(Constructor<?> c, Object[] args, boolean neutralCCL) throws Throwable {
            return ReflectionHelper.makeMe(c, args, neutralCCL);
        }
        
    };
    
    /**
     * Call this to construct objects taking constructor interceptors into consideration
     * 
     * @param c The constructor to invoke
     * @param args The arguments to give to the constructor (intially)
     * @param neutralCCL Whether or not CCL should be neutral around calls to the constructor
     * @param interceptors The set of interceptors (may be null or empty)
     * @param action The action to perform to construct the object
     * @return The constructed object (as massaged by the interceptors)
     * @throws Throwable On error
     */
    public static Object construct(Constructor<?> c, Object args[], boolean neutralCCL, List<ConstructorInterceptor> interceptors, ConstructorAction action) throws Throwable {
        if (interceptors == null || interceptors.isEmpty()) {
            return action.makeMe(c, args, neutralCCL);
        }
        
        if (!(interceptors instanceof RandomAccess)) {
            interceptors = new ArrayList<ConstructorInterceptor>(interceptors);
        }
        
        ConstructorInterceptor firstInterceptor = interceptors.get(0);
        
        Object retVal = firstInterceptor.construct(new ConstructorInvocationImpl(c,
                args,
                neutralCCL,
                action,
                0,
                interceptors,
                null));
        
        if (retVal == null) {
            throw new AssertionError("ConstructorInterceptor construct method returned null for " + c);
        }
        
        return retVal;
    }
    
    /**
     * Call this to construct objects taking constructor interceptors into consideration
     * 
     * @param c The constructor to invoke
     * @param args The arguments to give to the constructor (intially)
     * @param neutralCCL Whether or not CCL should be neutral around calls to the constructor
     * @param interceptors The set of interceptors (may be null or empty)
     * @return The constructed object (as massaged by the interceptors)
     * @throws Throwable On error
     */
    public static Object construct(Constructor<?> c, Object args[], boolean neutralCCL, List<ConstructorInterceptor> interceptors) throws Throwable {
        return construct(c, args, neutralCCL, interceptors, DEFAULT_ACTION);
    }
    
    private static class ConstructorInvocationImpl implements ConstructorInvocation, HK2Invocation {
        private final Constructor<?> c;
        private final Object[] args;
        private final boolean neutralCCL;
        private Object myThis = null;
        private final int index;
        private final ConstructorAction finalAction;
        private final List<ConstructorInterceptor> interceptors;
        private HashMap<String, Object> userData;
        
        private ConstructorInvocationImpl(Constructor<?> c,
                Object args[],
                boolean neutralCCL,
                ConstructorAction finalAction,
                int index,
                List<ConstructorInterceptor> interceptors,
                HashMap<String, Object> userData) {
            this.c = c;
            this.args = args;
            this.neutralCCL = neutralCCL;
            this.finalAction = finalAction;
            this.index = index;
            this.interceptors = interceptors;
            this.userData = userData;
        }
        

        @Override
        public Object[] getArguments() {
            return args;
        }

        @Override
        public AccessibleObject getStaticPart() {
            return c;
        }

        @Override
        public Object getThis() {
            return myThis;
        }

        @Override
        public Object proceed() throws Throwable {
            int newIndex = index + 1;
            if (newIndex >= interceptors.size()) {
                myThis = finalAction.makeMe(c, args, neutralCCL);
                return myThis;
            }
            
            // Invoke the next interceptor
            ConstructorInterceptor nextInterceptor = interceptors.get(newIndex);
            
            myThis = nextInterceptor.construct(new ConstructorInvocationImpl(c, args, neutralCCL,
                    finalAction, newIndex, interceptors, userData));
            return myThis;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Constructor getConstructor() {
            return c;
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
