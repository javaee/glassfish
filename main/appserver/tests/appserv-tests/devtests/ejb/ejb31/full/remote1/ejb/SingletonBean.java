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

import javax.ejb.*;
import javax.annotation.*;

import javax.naming.*;

@Singleton
@Startup
public class SingletonBean {

    @EJB Hello hello;
    @EJB HelloHome helloHome;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");

	try {
	    HelloRemote hr = helloHome.create();
	    System.out.println("HellohelloRemote.hello() says " + hr.hello());

	    System.out.println("Hello.hello() says " + hello.hello());

	    InitialContext ic = new InitialContext();

	    SessionContext ctx = (SessionContext) 
		ic.lookup("java:module/env/sesCtx");

	    SingletonBean me = (SingletonBean)
		ic.lookup("java:app/ejb-ejb31-full-remote1-ejb/SingletonBean");
	    
	    SingletonBean meToo = (SingletonBean)
		ic.lookup("java:app/ejb-ejb31-full-remote1-ejb/SingletonBean!com.acme.SingletonBean");

	    Hello m1 = (Hello) ic.lookup("java:module/env/M1");

	    HelloHome m2 = (HelloHome) ctx.lookup("java:module/M2");

	    Hello a1 = (Hello) ctx.lookup("java:app/env/A1");

	    HelloHome a2 = (HelloHome) ic.lookup("java:app/A2");

	    try {
		ic.lookup("java:comp/env/C1");
		throw new EJBException("Expected exception accessing private component environment entry of HelloBean");
	    } catch(NamingException e) {
		System.out.println("Successfully did *not* find HelloBean private component environment dependency");
	    }

	    try {
		ic.lookup("java:comp/C1");
		throw new EJBException("Expected exception accessing private component environment entry of HelloBean");
	    } catch(NamingException e) {
		System.out.println("Successfully did *not* find HelloBean private component environment dependency");
	    }

	    System.out.println("My AppName = " + 
			       ctx.lookup("java:app/AppName"));

	    System.out.println("My ModuleName = " + 
			       ctx.lookup("java:module/ModuleName"));

			       

	} catch(Exception e) {
	    throw new EJBException("singleton init error" , e);
	}

    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
