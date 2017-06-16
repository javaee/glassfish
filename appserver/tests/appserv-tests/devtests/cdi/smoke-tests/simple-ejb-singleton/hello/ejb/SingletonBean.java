/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

import javax.naming.InitialContext;

import javax.management.j2ee.ManagementHome;
import javax.inject.Inject;

@Singleton
@Remote(Hello.class)
    @EJB(name="mejb", beanInterface=javax.management.j2ee.ManagementHome.class, mappedName="ejb/mgmt/MEJB")
@Startup
public class SingletonBean {

    @Resource
    private ORB orb;

    @Inject Foo foo;
    @Inject TestBean tb;

    /*Object returned from IIOP_OBJECT_FACTORY is still ior
    @EJB(mappedName="ejb/mgmt/MEJB")
    ManagementHome mHome1;
    */

    /* Object returned from IIOP_OBJECT_FACTORY is still ior
    @EJB(lookup="java:global/mejb/MEJBBean")
    ManagementHome mHome2;
    */

    /* Doesn't work b/c actual MEJB app Home interface is new glassfish
     * type, so actual type derived from field declaration is tacked
     * onto mappedName and results in NameNotFound
    @EJB(mappedName="java:global/mejb/MEJBBean")
    ManagementHome mHome2;
    */

    


    //MEJBHome mHome2;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");
        System.out.println("orb = " + orb);
	if( orb == null ) {
	    throw new EJBException("null ORB");
	}
	try {
	    // same problem ManagementHome mHomeL = (ManagementHome) new InitialContext().lookup("java:comp/env/mejb");
	    // same problem ManagementHome mHomeL2 = (ManagementHome) new InitialContext().lookup("java:global/mejb/MEJBBean");
	    // System.out.println("mHomeL = " + mHomeL);
	    // System.out.println("mHomeL2 = " + mHomeL2);
	} catch(Exception e) {
	    throw new EJBException(e);
	}

	// System.out.println("mHome1 = " + mHome1);
	//	System.out.println("mHome2 = " + mHome2);

    }
    
    public String hello() {
	System.out.println("In SingletonBean::hello()");
	return "hello, world!\n";
    }

    public void testError() {
	throw new Error("test java.lang.Error");
    }

    public String testInjection(){
        if (foo == null) return "foo is null";
        if (tb == null) return "tb is null";
        if (!foo.testInjection()) return "testInjection in Foo failed";
        return "";
    }
        

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
