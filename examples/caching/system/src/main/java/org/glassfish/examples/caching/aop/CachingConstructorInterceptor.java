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

package org.glassfish.examples.caching.aop;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.ConstructorInvocation;

/**
 * This is an interceptor that will implement caching
 * of constructor results.
 * <p>
 * This interceptor does not import any hk2 API and thus
 * is a pure AOP Alliance method interceptor that might be
 * used in any software that enabled AOP Alliance interceptors
 * 
 * @author jwells
 *
 */
public class CachingConstructorInterceptor implements ConstructorInterceptor {
    private final HashMap<CacheKey, Object> cache = new HashMap<CacheKey, Object>();

    /* (non-Javadoc)
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    @Override
    public Object construct(ConstructorInvocation invocation) throws Throwable {
        Object args[] = invocation.getArguments();
        if (args.length != 1) {
            return invocation.proceed();
        }
        
        Object arg = args[0];
        if (arg == null) {
            return invocation.proceed();
        }
        
        Constructor<?> c = invocation.getConstructor();
        CacheKey key = new CacheKey(c.getDeclaringClass().getName(), "<init>", arg);
        
        if (!cache.containsKey(key)) {
            Object retVal = invocation.proceed();
            
            cache.put(key, retVal);
            
            return retVal;
        }
        
        // Found it in the cache!  Do not call the method
        return cache.get(key);
    }
}
