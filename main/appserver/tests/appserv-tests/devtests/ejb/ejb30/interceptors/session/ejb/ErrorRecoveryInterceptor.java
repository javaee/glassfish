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

package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import java.lang.reflect.Method;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;

import javax.ejb.*;
import javax.annotation.*;

public class ErrorRecoveryInterceptor {

    @Resource(name="sc") SessionContext sc;

    @EJB(name="foobar") Sless foobar;

    @AroundInvoke
    public Object interceptCall(InvocationContext ctx)
   	throws MyBadException, AssertionFailedException, Exception 
    {
	System.out.println("In ErrorREcoveryInterceptor");
	System.out.println("sless = " + foobar);
	System.out.println("sc = " + sc);

        Method method = ctx.getMethod();
        String methodName = method.getName();
        Object[] params = ctx.getParameters();
        boolean recoverFromError = false;
        Object retVal = ctx.proceed();

        if (methodName.equals("assertIfTrue")) {
            params[0] = new Boolean(false);
 
            try {
                ctx.setParameters(params);
            } catch (IllegalArgumentException illArgEx) {
                throw new MyBadException("Invalid type as argument", illArgEx);
            }

            retVal = ctx.proceed();
        }

        return retVal;
    }

}
