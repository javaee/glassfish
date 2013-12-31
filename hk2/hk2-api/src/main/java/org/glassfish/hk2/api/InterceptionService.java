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
package org.glassfish.hk2.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.jvnet.hk2.annotations.Contract;

/**
 * This service is implemented in order to configure
 * interceptors on methods or constructors provided by
 * hk2 services.  All implementations must be in the 
 * {@link javax.inject.Singleton} scope.  Only services
 * that are created by HK2 are candidates for interception.
 * In particular services created by the provide method of
 * the {@link Factory} interface can not be intercented.  
 * 
 * @author jwells
 */
@Contract
public interface InterceptionService {
    /**
     * If the returned filter returns true then the methods
     * of the service will be passed to {@link #getMethodInterceptors(Method)}
     * to determine if a method should be intercepted and the
     * constructor of the service will be passed to
     * {@link #getConstructorInterceptors(Constructor)} to
     * determine if the constructor should be intercepted
     * 
     * @return The filter that will be applied to a descriptor
     * to determine if it is to be intercepted.  Should not
     * return null
     */
    public Filter getDescriptorFilter();
    
    /**
     * Each non-final method of a service that passes the
     * {@link #getDescriptorFilter} method will be passed
     * to this method to determine if it will intercepted
     * 
     * @param method A non-final method that may
     * be intercepted
     * @return if null (or an empty list) then this method should
     * NOT be intercepted.  Otherwise the list of interceptors to
     * apply to this method
     */
    public List<MethodInterceptor> getMethodInterceptors(Method method);
    
    /**
     * The single chosen constructor of a service that passes the
     * {@link #getDescriptorFilter} method will be passed
     * to this method to determine if it will intercepted
     * 
     * @param constructor A constructor that may
     * be intercepted
     * @return if null (or an empty list) then this constructor should
     * NOT be intercepted.  Otherwise the list of interceptors to
     * apply to this method
     */
    public List<ConstructorInterceptor> getConstructorInterceptors(Constructor<?> constructor);

}
