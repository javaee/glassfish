/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.s1asdev.ejb.ejb30.interceptors.bindings;


import javax.ejb.Stateful;
import javax.interceptor.Interceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.ejb.EJBException;

@Stateful
@Interceptors({InterceptorC.class, InterceptorD.class})
public class SfulEJB implements Sful
{

    private boolean aroundInvokeCalled = false;
    private boolean postConstructCalled = false;

    // postConstruct declared in ejb-jar.xml
    private void postConstruct() {
        System.out.println("In SfulEJB:postConstruct");
        postConstructCalled = true;
    }

    @ExcludeDefaultInterceptors
    public void cd() {
        System.out.println("in SfulEJB:cd().  postConstruct = " + 
                           postConstructCalled);

        if( !postConstructCalled ) {
            throw new EJBException("postConstruct wasn't called");
        }

        // a little extra checking to make sure aroundInvoke is invoked...
        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke not called");
        }
        aroundInvokeCalled = false;
    }

    @ExcludeClassInterceptors
    public void ab() {}

    public void abcd() {}

    @ExcludeClassInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    public void abef() {}

    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    public void ef() {}

    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    @Interceptors({InterceptorC.class, InterceptorE.class, InterceptorF.class})
    public void cef() {}

    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    @Interceptors({InterceptorC.class, InterceptorE.class, InterceptorF.class, InterceptorA.class})
    public void cefa() {}

    @ExcludeDefaultInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    public void cdef() {}
    
    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    public void nothing() {}

    @Interceptors({InterceptorE.class, InterceptorF.class})    
    public void abcdef() {}

    // total ordering overridden in deployment descriptor
    @Interceptors({InterceptorE.class, InterceptorF.class})    
    public void acbdfe() {}

    // declared in ejb-jar.xml
    private Object aroundInvoke(InvocationContext ctx)
    {
        System.out.println("In SfulEJB:aroundInvoke");
        aroundInvokeCalled = true;
        Common.checkResults(ctx);
        try {
            return ctx.proceed();
        } catch(Exception e) {
            throw new EJBException(e);
        }
    }
    
}
