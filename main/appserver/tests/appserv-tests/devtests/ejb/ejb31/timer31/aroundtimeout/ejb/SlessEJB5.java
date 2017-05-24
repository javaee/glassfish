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
import javax.ejb.Timer;
import javax.ejb.EJBException;
import javax.interceptor.Interceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.InvocationContext;


// Define some aroundtimeout via annotations and some via
// ejb-jar.xml.  Ejb-jar.xml bindings are additive and
// ordered after aroundtimeout at same level declared via
// annotations.
@Stateless
@Interceptors({InterceptorD.class})
public class SlessEJB5 implements Sless5
{
    private volatile static boolean aroundTimeoutCalled = false;
    boolean aroundAllCalled = false;

    private final static int EXPECTED = 3;

    // Called as a timeout and through interface. InderceptorD will 
    // set aroundAllCalled to true either way.
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB5-abdc")
    public void abdc() {
        System.out.println("in SlessEJB5:abdc().  aroundTimeoutCalled = " + 
                           aroundTimeoutCalled);

        // a little extra checking to make sure AroundTimeout is invoked...
        if( !aroundTimeoutCalled ) {
            throw new EJBException("bean class aroundTimeout not called");
        }
        // Enough if it is set once to true. Otherwise a timer can be executed 
        // between verify() and direct call to this method through the interface
        // aroundTimeoutCalled = false;

        if( !aroundAllCalled ) {
            throw new EJBException("InderceptorD aroundAll not called");
        }
        aroundAllCalled = false;
    }

    // Interceptor E added at method level within ejb-jar.xml
    @ExcludeDefaultInterceptors
    @Interceptors({InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB5-dcfe")
    public void dcfe() {}

    @ExcludeClassInterceptors
    @ExcludeDefaultInterceptors
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB5-nothing")
    public void nothing() {}

    // declared in ejb-jar.xml
    private Object aroundTimeout(InvocationContext ctx)
    {
        System.out.println("In SlessEJB5:aroundTimeout");
        aroundTimeoutCalled = true;
        Common.checkResults(ctx);
        try {
            return ctx.proceed();
        } catch(Exception e) {
            throw new EJBException(e);
        }
    }
    
    public void verify() {
        Common.checkResults("SlessEJB5", EXPECTED);
        aroundTimeoutCalled = true;
    }
}
    

