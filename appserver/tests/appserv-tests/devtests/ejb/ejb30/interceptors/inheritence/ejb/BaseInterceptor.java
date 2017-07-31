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
import javax.ejb.PrePassivate;
import javax.ejb.PostActivate;

public class BaseInterceptor
	implements java.io.Serializable {

    protected static final String BASE_INTERCEPTOR_NAME = "BaseInterceptor";

    protected List<String> postConstructList = new ArrayList<String>();
    protected List<String> aroundInvokeList = new ArrayList<String>();

    protected int baseAICount = 0;
    protected int prePassivateCount = 0;
    private int postActivateCount = 0;
    protected int basePCCount = 0;


    @PostConstruct
    private void basePostConstruct(InvocationContext ctx)
    	throws RuntimeException {
	    postConstructList = new ArrayList<String>();
	    postConstructList.add(BASE_INTERCEPTOR_NAME);
        ctx.getContextData().put(getName(), this);
        basePCCount++;
        System.out.println("GGGG: @PostConstruct for: " + this.getClass().getName());
        try {
            ctx.proceed();
        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }


    @AroundInvoke
    public Object baseAroundInvoke(InvocationContext ctx)
   		throws Exception
    {
	    aroundInvokeList = new ArrayList<String>();
	    aroundInvokeList.add(BASE_INTERCEPTOR_NAME);
		ctx.getContextData().put(getName(), this);
		baseAICount++;
    	return ctx.proceed();
    }

    @PrePassivate
    public void prePassivate(InvocationContext ctx) {
	prePassivateCount++;
	}

    @PostActivate
    public void postActivate(InvocationContext ctx) {
	postActivateCount++;
	}


    String getName() {
       return BaseInterceptor.class.getName();
    }
}
