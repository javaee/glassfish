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

import javax.ejb.Stateless;
import javax.ejb.*;
import javax.interceptor.Interceptors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import java.util.Map;

@Stateless
@Interceptors(InterceptorB.class)
public class StatelessBean {
    
    @Resource 
    private SessionContext sessionCtx;

    @Resource(mappedName="java:module/foomanagedbean")
    private FooManagedBean foo;

    @Resource(mappedName="java:app/ejb-ejb31-ejblite-javamodule-web/foomanagedbean")
    private FooManagedBean foo2;

    @EJB(name="stateless/singletonref")
    private SingletonBean singleton;

    @PostConstruct
    private void init() {
	System.out.println("In StatelessBean:init()");
    }

    public void hello() {
	System.out.println("In StatelessBean::hello()");

	Map<String, Object> ctxData = sessionCtx.getContextData();
	String fooctx = (String) ctxData.get("foo");
	System.out.println("foo from context data = " + fooctx);
	if( fooctx == null ) {
	    throw new EJBException("invalid context data");
	}
	ctxData.put("foobar", "foobar");

	FooManagedBean fmb = (FooManagedBean) 
	    sessionCtx.lookup("java:module/foomanagedbean");

	// Make sure dependencies declared in java:comp are visible
	// via equivalent java:module entries since this is a 
	// .war
	SessionContext sessionCtx2 = (SessionContext)
	    sessionCtx.lookup("java:module/env/com.acme.StatelessBean/sessionCtx");

	SingletonBean singleton2 = (SingletonBean)
	    sessionCtx2.lookup("java:module/env/stateless/singletonref");

	// Lookup a comp env dependency declared by another ejb in the .war
	SingletonBean singleton3 = (SingletonBean)
	    sessionCtx2.lookup("java:comp/env/com.acme.SingletonBean/me");

	// Lookup a comp env dependency declared by a servlet
	FooManagedBean fmbServlet = (FooManagedBean)
	    sessionCtx.lookup("java:comp/env/foo2ref");
	FooManagedBean fmbServlet2 = (FooManagedBean)
	    sessionCtx.lookup("java:module/env/foo2ref");

	// Ensure that each injected or looked up managed bean 
	// instance is unique
	Object fooThis = foo.getThis();
	Object foo2This = foo2.getThis();
	Object fmbThis = fmb.getThis();

	System.out.println("fooThis = " + fooThis);
	System.out.println("foo2This = " + foo2This);
	System.out.println("fmbThis = " + fmbThis);
	System.out.println("fmbServlet = " + fmbServlet);
	System.out.println("fmbServlet2 = " + fmbServlet2);

	if( ( fooThis == foo2This ) || ( fooThis == fmbThis  ) ||
	    ( foo2This == fmbThis ) ) {
	    throw new EJBException("Managed bean instances not unique");
	}

    }

    @PreDestroy
    private void destroy() {
	System.out.println("In StatelessBean:destroy()");
    }


}
