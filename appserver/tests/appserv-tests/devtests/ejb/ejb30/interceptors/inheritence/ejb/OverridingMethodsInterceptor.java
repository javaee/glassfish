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

import java.util.List;
import java.util.ArrayList;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.annotation.PostConstruct;

public class OverridingMethodsInterceptor
	extends BaseLevel2Interceptor {

    protected static final String OVERRIDING_INTERCEPTOR_NAME = "OverridingInterceptor";

    protected int aiCount = 0;
    protected int pcCount = 0;


    @PostConstruct
    protected void overridablePostConstructMethod(InvocationContext ctx)
    	throws RuntimeException {
        postConstructList.add(OVERRIDING_INTERCEPTOR_NAME);
        pcCount++;
        try {
            ctx.proceed();
        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }


    @AroundInvoke
    protected Object overridableAroundInvokeMethod(InvocationContext ctx)
    	throws Throwable
    {
	aroundInvokeList.add(OVERRIDING_INTERCEPTOR_NAME);
        aiCount++;
        return ctx.proceed();
    }

    protected boolean isAICountOK() {
		return (0 == baseLevel2AICount)
			&& (aiCount == baseAICount)
			&& checkForCorrectSequence(aroundInvokeList);
	}

	protected boolean isPostConstructCallCounOK() {
		return (pcCount == basePCCount)
			&& (baseLevel2PCCount == 0)
			&& checkForCorrectSequence(postConstructList);
	}

    private boolean checkForCorrectSequence(List<String> list) {
	boolean result = list.size() == 2;
	if (result) {
	    BASE_INTERCEPTOR_NAME.equals(list.get(0));
	    OVERRIDING_INTERCEPTOR_NAME.equals(list.get(1));
	}
	for(String str : list) {
	    System.out.println("**OVERRIDING_INTERCEPTOR_TEST**: " + str);
	}
	System.out.println("**OVERRIDING_INTERCEPTOR_TEST**: " + result);


	return result;
    }


    String getName() {
       return OverridingMethodsInterceptor.class.getName();
    }
}
