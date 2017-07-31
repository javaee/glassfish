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

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.ejb.EJBException;
import javax.annotation.PostConstruct;

@Interceptors({
	com.sun.s1asdev.ejb.ejb30.interceptors.session.OverridingMethodsInterceptor.class,
	com.sun.s1asdev.ejb.ejb30.interceptors.session.DistinctMethodsInterceptor.class
})

@Stateless
public class DummyEJB
	extends DummyLevel2EJB
	implements Dummy
{
    int interceptorId;
    private boolean createCalled = false;
    private int beanAICount = 0;
    private int dummyEJBPostConstructCount = 0;

    DistinctMethodsInterceptor distinctInterceptor = null;
    OverridingMethodsInterceptor overridingInterceptor = null;

    private static final String distinctAIName = "com.sun.s1asdev.ejb.ejb30.interceptors.session.DistinctMethodsInterceptor";
    private static final String overridingAIName = "com.sun.s1asdev.ejb.ejb30.interceptors.session.OverridingMethodsInterceptor";

    @PostConstruct
    protected void overridablePostConstruct()
    {
	postConstructList.add("DummyEJB");
        createCalled = true;
        dummyEJBPostConstructCount++;
    }

    public String dummy() {
	    return "Dummy!!";
    }

    public void setInterceptorId(int val) {
        if( !createCalled ) {
            throw new EJBException("create was not called");
        }

        this.interceptorId = val;
    }

    public String isInterceptorCallCounOK() {

		boolean beanAIResult = (beanAICount == dummyBaseAICount)
			&& (beanAICount == dummyLevel2AICount)
			&& checkAroundInvokeSequence();

		return "" + distinctInterceptor.isAICountOK()
			+ " " + overridingInterceptor.isAICountOK()
			+ " " + beanAIResult
			+ " " +  checkAroundInvokeSequence();
	}


    public String isPostConstructCallCounOK() {

		boolean beanPCCountResult = (dummyEJBPostConstructCount == dummyBaseEJBPostConstructCount)
			&& (dummyLevel2EJBPostConstructCount == 0)
			&& checkPostConstructSequence();

		return "" + distinctInterceptor.isPostConstructCallCounOK()
			+ " " + overridingInterceptor.isPostConstructCallCounOK()
			+ " " + beanPCCountResult
			+ " " +  checkPostConstructSequence();
	}

    @AroundInvoke
    Object myOwnAroundInvoke(InvocationContext ctx)
        throws Exception {
		aroundInvokeList.add("DummyEJB");
		if (distinctInterceptor == null) {
			 distinctInterceptor = (DistinctMethodsInterceptor)
			 	ctx.getContextData().get(distinctAIName);
			 overridingInterceptor = (OverridingMethodsInterceptor)
			 	ctx.getContextData().get(overridingAIName);
		}

		beanAICount++;
        return ctx.proceed();
    }

    private boolean checkPostConstructSequence() {
	boolean result = postConstructList.size() == 2;
	if (result) {
	    "DummyBaseEJB".equals(postConstructList.get(0));
	    "DummyEJB".equals(postConstructList.get(1));
	}

	return result;
    }

    private boolean checkAroundInvokeSequence() {
	boolean result = aroundInvokeList.size() == 3;
	if (result) {
	    "DummyBaseEJB".equals(aroundInvokeList.get(0));
	    "DummyLevel2EJB".equals(aroundInvokeList.get(1));
	    "DummyEJB".equals(aroundInvokeList.get(2));
	}

	return result;
    }

}

