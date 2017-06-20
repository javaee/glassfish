/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.acme;

import javax.ejb.MessageDriven;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Timer;
import javax.interceptor.AroundTimeout;
import javax.interceptor.InvocationContext;
import javax.interceptor.Interceptors;

import javax.annotation.Resource;
import javax.ejb.MessageDrivenContext;
import javax.jms.MessageListener;
import javax.jms.Message;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.*;
import javax.ejb.EJBException;

@Interceptors(InterceptorA.class)
@MessageDriven(mappedName="jms/ejb_ejb31_timer31_mdb_InQueue", description="mymessagedriven bean description")
 @RolesAllowed("foo")
public class MessageBean implements MessageListener {

    String mname;

    @EJB 
    private SingletonBean singleton;

    @Resource
	private MessageDrivenContext mdc;
     
    @PostConstruct
    public void init() {
        System.out.println("In MessageBean::init()");
    }

    public void onMessage(Message message) {
	System.out.println("In MessageBean::onMessage()");
	System.out.println("getCallerPrincipal = " + mdc.getCallerPrincipal());
        verifyMethodName("onMessage");
    }

    @Schedule(second="*/1", minute="*", hour="*")
    private void onTimeout() {
	System.out.println("In MessageBean::onTimeout()");
	System.out.println("getCallerPrincipal = " + mdc.getCallerPrincipal());

        verifyMethodName("onTimeout");
	try {
	    System.out.println("IsCallerInRole('foo')= " + 
			       mdc.isCallerInRole("foo"));
	    throw new EJBException("Expecting IllegalStateEXception for call to isCallerInRole() from timer callback");
	} catch(IllegalStateException ise) {
	    System.out.println("Successfully received exception for invocation of isCallerInRole from timer callback");
	}
	    
        if (singleton.getAroundTimeoutCalled(null)) {
	    singleton.test1Passed();
        }
    }

    private void onDDTimeout(Timer t) {
	System.out.println("In MessageBean::onDDTimeout()");
        if (singleton.getAroundTimeoutCalled((String)t.getInfo())) {
	    singleton.test2Passed();
        }
    }

    
    @PreDestroy
    public void destroy() {
        System.out.println("In MessageBean::destroy()");
    }

    @AroundTimeout
    private Object around_timeout(InvocationContext ctx) throws Exception {
        String info = (String)((Timer)ctx.getTimer()).getInfo();
        System.out.println("In MessageBean::AroundTimeout() for info " + info);
        singleton.setAroundTimeoutCalled(info);
        return ctx.proceed();
    }

    private void verifyMethodName(String name) {
        try {
            if (mname == null || !mname.equals(name))
                throw new EJBException("Expecting method named " + name + " got " + mname);
        } finally {
            mname = null;
        }
    }

}
