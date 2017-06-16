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

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.annotation.PostConstruct;
import javax.ejb.PrePassivate;
import javax.ejb.PostActivate;

public class BaseInterceptor {

    private static int baseCount = 0;
    private static int prePassivateCount = 0;
    private static int postActivateCount = 0;

    private int interceptorID;
    private int computedInterceptorID;

    @PostConstruct
    private void afterCreation(InvocationContext ctx) {
        System.out.println("In BaseInterceptor.afterCreation");
        ctx.getContextData().put("PostConstruct", "BaseInterceptor");
        try {
            ctx.proceed();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @AroundInvoke
    public Object interceptCall(InvocationContext ctx)
   	throws Exception 
    {
        Object result = null;
	boolean setId = false;
	baseCount++;
    if (ctx.getMethod().getName().equals("setInterceptorId")) {
        java.util.Map map = ctx.getContextData();
        map.put("BaseInterceptor", this);
        setId = true;
    }
    try {
        result = ctx.proceed();
    } finally {
        if (setId == true) {
            computedInterceptorID = interceptorID * 2 + 1;
        }
    }
    return result;
    }

    public static int getInvocationCount() {
	    return baseCount;
    }
    
    @PrePassivate
    public void prePassivate(InvocationContext ctx) {
	prePassivateCount++;
	System.out.println("prePassivateCount: " + prePassivateCount);
    }

    @PostActivate
    public void postActivate(InvocationContext ctx) {
	postActivateCount++;
	System.out.println("postActivateCount: " + postActivateCount);
    }
    
    //Some package private methods to check state
    void setInterceptorID(int val) {
        this.interceptorID = val;
    }
    
    int getInterceptorID() {
        boolean valid = (computedInterceptorID == interceptorID * 2 + 1);
        if (! valid) {
            throw new IllegalStateException("" + interceptorID + " != " + computedInterceptorID);
        }
        return this.interceptorID;
    }

}
