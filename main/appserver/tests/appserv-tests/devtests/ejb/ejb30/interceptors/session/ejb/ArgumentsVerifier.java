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

public class ArgumentsVerifier {

    @AroundInvoke
    public Object interceptCall(InvocationContext ctx)
   	throws MyBadException, Exception 
    {
        Method method = ctx.getMethod();
        String methodName = method.getName();
        Object[] params = ctx.getParameters();
        if (methodName.equals("addIntInt")) {
            System.out.println("**++**Args[0]: " + params[0].getClass().getName());
            System.out.println("**++**Args[1]: " + params[1].getClass().getName());
            params[0] = new Integer(10);
            params[1] = new Integer(20);
        } else if (methodName.equals("setInt")) {
            params[0] = new Long(10);
        } else if (methodName.equals("setLong")) {
            params[0] = new Integer(10);
        } else if (methodName.equals("addLongLong")) {
            params[0] = new Integer(10);
            params[0] = new Long(10);
        } else if (methodName.equals("setFoo")) {
            params[0] = new SubFoo();
        } else if (methodName.equals("setBar")) {
            params[0] = new SubFoo();   //Should fail
        } else if (methodName.equals("emptyArgs")) {
            params = new Object[1];
            params[0] = new Long(45);
        } else if (methodName.equals("objectArgs")) {
            params = new Object[0];
        } 

        try {
            ctx.setParameters(params);
        } catch (IllegalArgumentException illArgEx) {
            throw new MyBadException("Invalid type as argument", illArgEx);
        }
        Object retVal = ctx.proceed();

        if (methodName.equals("setIntInt")) {
            Integer ii = (Integer) retVal;
            if (! ii.equals(new Integer(30))) {
                throw new WrongResultException("Wrong result. expected 20. Got: " + ii);
            }
        }

        return retVal;
    }

}
