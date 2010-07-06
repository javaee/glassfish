/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

import org.omg.CORBA.ORB;


@Singleton
@Startup
    @EJB(name="java:app/env/AS2", beanName="HelloStateless", beanInterface=HelloRemote.class)
    @DependsOn("Singleton2")
public class HelloSingleton implements Hello {

    @Resource SessionContext sessionCtx;

    @Resource(mappedName="java:module/foobarmanagedbean")
    private FooBarManagedBean fbmb;

    @Resource
    private FooBarManagedBean fbmb2;

    @Resource(name="java:module/env/MORB2")
    private ORB orb;

    @Resource 
    private FooManagedBean foo;
    
    @Resource(name="foo2ref", mappedName="java:module/somemanagedbean")
    private FooManagedBean foo2;

    @Resource(name="foo3ref", mappedName="java:module/somemanagedbean")
    private Foo foo3;

    private FooManagedBean foo4;
    private FooManagedBean foo5;
    private Foo foo6;
    private FooManagedBean foo7;
    private Foo foo8;

    @Resource(name = "java:app/env/myString")
    protected String myString;

    @EJB(name="java:app/env/appLevelEjbRef")
    private Hello hello;
    
    String appName;
    String moduleName;

    @PostConstruct    
    private void init() {
	System.out.println("HelloSingleton::init()");

	System.out.println("myString = '" + myString + "'");
	if( (myString == null) || !(myString.equals("myString") ) ) {
	    throw new RuntimeException("Invalid value " + myString + " for myString");
	}

	appName = (String) sessionCtx.lookup("java:app/AppName");
	moduleName = (String) sessionCtx.lookup("java:module/ModuleName");

	ORB orb1 = (ORB) sessionCtx.lookup("java:module/MORB1");
	ORB orb2 = (ORB) sessionCtx.lookup("java:module/env/MORB2");

	System.out.println("AppName = " + appName);
	System.out.println("ModuleName = " + moduleName);

	foo4 = (FooManagedBean) sessionCtx.lookup("java:module/somemanagedbean");
	foo5 = (FooManagedBean) sessionCtx.lookup("java:app/" + moduleName +
						  "/somemanagedbean");
	foo6 = (Foo) sessionCtx.lookup("java:app/" + moduleName +
						  "/somemanagedbean");
	foo7 = (FooManagedBean) sessionCtx.lookup("java:comp/env/foo2ref");
	foo8 = (Foo) sessionCtx.lookup("java:comp/env/foo3ref");
    }

    public String hello() {
	
	System.out.println("HelloSingleton::hello()");


	foo.foo();
	foo2.foo();
	foo3.foo();

	foo4.foo();
	foo5.foo();
	foo6.foo();
	foo7.foo();
      	foo8.foo();

	return "hello, world!\n";
    }


    @PreDestroy
    private void destroy() {
	System.out.println("HelloSingleton::destroy()");
    }

}


