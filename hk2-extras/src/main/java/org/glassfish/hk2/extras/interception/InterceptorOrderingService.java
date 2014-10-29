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
package org.glassfish.hk2.extras.interception;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.hk2.annotations.Contract;

/**
 * This service can be used to modify, add or remove interceptors
 * to the set of interceptors that will be called on a Method
 * or Constructor in the default implementation of the
 * interception service.  If there are multiple implementations
 * of this service then they will be called in the natural
 * hk2 ordering of services with the result of the method being
 * fed into the next service.
 * 
 * @author jwells
 *
 */
@Contract
public interface InterceptorOrderingService {
    
    /**
     * This method is called for each method that may be intercepted by the default
     * interception service.  The incoming list is not modifiable.  If this method
     * returns null then the original list (the list passed in) will be used as-is.
     * If this method does NOT return null then the list returned will be the list
     * that will be used to intercept the given method.  This means that the interceptors
     * can be removed (if an empty list is returned) or modified.  Modifications can
     * include changes of order, additions and/or removals of the interceptors
     * passed into the method.  If this method throws an exception the exception
     * will be ignored and the interceptor list passed in will be used.
     * 
     * If the implementation would like to return MethodInterceptors that are not hk2
     * services it is recommended that they use {@link BuilderHelper#createConstantServiceHandle(Object)}
     * to create ServiceHandles representing their MethodInterceptors
     *  
     * @param method The method that is to be intercepted
     * @param currentList The list that will be used to intercept the method if this
     * service returns null
     * @return A non-null list of interceptors to use when intercepting this method.  The
     * returned list must be ordered.  If this method returns null then the list passed
     * in will be used
     */
    public List<ServiceHandle<MethodInterceptor>> modifyMethodInterceptors(Method method, List<ServiceHandle<MethodInterceptor>> currentList);
    
    /**
     * This method is called for each constructor that may be intercepted by the default
     * interception service.  The incoming list is not modifiable.  If this method
     * returns null then the original list (the list passed in) will be used as-is.
     * If this method does NOT return null then the list returned will be the list
     * that will be used to intercept the given constructor.  This means that the interceptors
     * can be removed (if an empty list is returned) or modified.  Modifications can
     * include changes of order, additions and/or removals of the interceptors
     * passed into the method.  If this method throws an exception the exception
     * will be ignored and the interceptor list passed in will be used
     *
     * If the implementation would like to return ConstructorInterceptors that are not hk2
     * services it is recommended that they use {@link BuilderHelper#createConstantServiceHandle(Object)}
     * to create ServiceHandles representing their ConstructorInterceptors
     *  
     * @param constructor The constructor that is to be intercepted
     * @param currentList The list that will be used to intercept the constructor if this
     * service returns null
     * @return A non-null list of interceptors to use when intercepting this constructor.  The
     * returned list must be ordered.  If this method returns null then the list passed
     * in will be used
     */
    public List<ServiceHandle<ConstructorInterceptor>> modifyConstructorInterceptors(Constructor<?> constructor, List<ServiceHandle<ConstructorInterceptor>> currentList);

}
