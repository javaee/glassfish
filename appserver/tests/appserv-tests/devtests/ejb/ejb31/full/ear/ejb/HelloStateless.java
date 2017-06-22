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

import org.omg.CORBA.ORB;

@Stateless
    @EJB(name="java:global/GS1", beanName="HelloSingleton", beanInterface=Hello.class)
public class HelloStateless implements HelloRemote {

    @EJB(name="java:app/env/AS1", beanName="HelloSingleton")
    private Hello h;

    @Resource(name="java:module/MORB1")
    private ORB orb;
    
    @EJB(name="lookupref1", lookup="java:app/env/AS1")
    private Hello lookupref1;

    @EJB(name="lookupref2", lookup="java:global/GS1")
    private Hello lookupref2;

    @EJB(name="lookupref3", lookup="java:module/HelloStateless!com.acme.HelloRemote")
    private HelloRemote lookupref3;

    // declare component-level dependency using fully-qualified
    // java:comp/env form.  
    @Resource(name="java:comp/env/foo") SessionContext sessionCtx;

    @PostConstruct 
    private void init() {
	System.out.println("HelloStateless::init()");
    }

    public String hello() {
	System.out.println("In HelloStateless::hello()");

	String appName = (String) sessionCtx.lookup("java:app/AppName");
	String moduleName = (String) sessionCtx.lookup("java:module/ModuleName");
	System.out.println("AppName = " + appName);
	System.out.println("ModuleName = " + moduleName);

	ORB orb1 = (ORB) sessionCtx.lookup("java:module/MORB1");
	ORB orb2 = (ORB) sessionCtx.lookup("java:module/env/MORB2");

	Hello s1 = (Hello) sessionCtx.lookup("java:global/" +
					   appName + "/" +
					   moduleName + "/" +
					   "HelloSingleton");

	Hello s2 = (Hello) sessionCtx.lookup("java:app/" +
							 moduleName + "/" +
							 "HelloSingleton");

	// Rely on default to resolve "java:comp/env/ declared resource
	SessionContext sc1 = (SessionContext)
	    sessionCtx.lookup("foo");

	SessionContext sc2 = (SessionContext)
	    sc1.lookup("java:comp/env/foo");

	Integer envEntry = (Integer)
	    sc1.lookup("java:app/env/value1");
	System.out.println("java:ap/env/value1 = " + envEntry);

	return "hello, world!\n";
    }

    @PreDestroy
    private void destroy() {
	System.out.println("HelloStateless::destroy()");
    }

}
