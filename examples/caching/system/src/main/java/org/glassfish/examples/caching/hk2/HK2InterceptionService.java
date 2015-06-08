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

package org.glassfish.examples.caching.hk2;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.glassfish.examples.caching.aop.CachingConstructorInterceptor;
import org.glassfish.examples.caching.aop.CachingMethodInterceptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InterceptionService;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.hk2.annotations.Service;

/**
 * This is the HK2 interception service that will be used
 * to determine if a method or constructor should be intercepted
 * 
 * @author jwells
 *
 */
@Service
public class HK2InterceptionService implements InterceptionService {
    private final static MethodInterceptor METHOD_INTERCEPTOR = new CachingMethodInterceptor();
    private final static ConstructorInterceptor CONSTRUCTOR_INTERCEPTOR = new CachingConstructorInterceptor();
    
    private final static List<MethodInterceptor> METHOD_LIST = Collections.singletonList(METHOD_INTERCEPTOR);
    private final static List<ConstructorInterceptor> CONSTRUCTOR_LIST = Collections.singletonList(CONSTRUCTOR_INTERCEPTOR);

    /**
     * Interception could happen on any class, so we can
     * easily return the all filter here
     */
    @Override
    public Filter getDescriptorFilter() {
        return BuilderHelper.allFilter();
    }

    /**
     * Returns the method interceptors if Cache is on the method
     * @param method The method under investigation
     * @return If Cache is on the method will return the caching interceptor list,
     * otherwise returns null
     */
    @Override
    public List<MethodInterceptor> getMethodInterceptors(Method method) {
        if (method.isAnnotationPresent(Cache.class)) {
            return METHOD_LIST;
        }
        
        return null;
    }

    /**
     * Returns the consturctor interceptors if Cache is on the constructor
     * @param constructor The constructor under investigation
     * @return If Cache is on the constructor will return the caching interceptor list,
     * otherwise returns null
     */
    @Override
    public List<ConstructorInterceptor> getConstructorInterceptors(
            Constructor<?> constructor) {
        if (constructor.isAnnotationPresent(Cache.class)) {
            return CONSTRUCTOR_LIST;
        }
        
        return null;
    }

}
