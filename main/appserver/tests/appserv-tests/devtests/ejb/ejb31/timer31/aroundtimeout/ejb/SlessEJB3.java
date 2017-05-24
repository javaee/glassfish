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

package com.sun.s1asdev.ejb.ejb31.aroundtimeout;


import javax.ejb.Stateless;
import javax.ejb.Schedule;
import javax.ejb.EJBException;
import javax.interceptor.Interceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.AroundTimeout;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

@Stateless
// total ordering DCBA expressed in .xml
@Interceptors({InterceptorC.class, InterceptorD.class})
public class SlessEJB3 implements Sless3
{

    private final static int EXPECTED = 10;

    @ExcludeDefaultInterceptors
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-dc")
    private void dc() {}

    @ExcludeClassInterceptors
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-ba")
    protected void ba() {}

    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-dcba")
    void dcba() {}

    @ExcludeClassInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-baef")
    private void baef() {}

    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-ef")
    private void ef() {}

    @ExcludeDefaultInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-dcef")
    private void dcef() {}
    
    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-nothing")
    private void nothing() {}

    @Interceptors({InterceptorE.class, InterceptorF.class})    
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-dcbaef")
    private void dcbaef() {}

    // total ordering overridden in deployment descriptor
    @Interceptors({InterceptorE.class, InterceptorF.class})    
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-abcdef")
    private void abcdef() {}

    // binding described in deployment descriptor
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-dcf")
    private void dcf() {}

    // called only through interface
    @Interceptors({InterceptorA.class, InterceptorG.class})
    public void noaroundtimeout() {}

    @AroundTimeout
    private Object aroundTimeout(InvocationContext ctx) throws Exception
    {
        // Common will verify that noaroundtimeout() is not called.
        Common.checkResults(ctx);
        return null;
    }
    
    @AroundInvoke
    private Object aroundInvoke(InvocationContext ctx) throws Exception
    {
        String methodName = ctx.getMethod().getName();
        if (!methodName.equals("noaroundtimeout") && !methodName.equals("verify")) {
            throw new EJBException("SlessEJB3:aroundInvoke is called for timeout method " + methodName);
        }
        return ctx.proceed();
    }
    
    public void verify() {
        Common.checkResults("SlessEJB3", EXPECTED);
    }
}
