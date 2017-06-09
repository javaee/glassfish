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

package com.sun.s1asdev.ejb.ejbc.equals;

import java.util.Enumeration;
import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;


public class FooBean implements SessionBean {

    private SessionContext sc;

    public FooBean() {}

    public void ejbCreate() throws RemoteException {
	System.out.println("In FooBean::ejbCreate !!");
    }

    public void setSessionContext(SessionContext sc) {
	this.sc = sc;
    }

    public void louie() { System.out.println("luigi luigi"); }

    public String sayHello() {
        System.out.println("in sayHello()");
        String returnValue = null;
        try {
            Context ic = new InitialContext();
            returnValue = (String) ic.lookup("java:comp/env/foo");
            // if the lookup succeeded, we're in hello2.  make sure we can call
            // cmt-related ejb context methods.
            System.out.println("getRollbackOnly = " + sc.getRollbackOnly());
        } catch(NamingException ne) {
            // we must be in hellobean(vs. hellobean2), so ignore this.
        }
        return returnValue;
    }

    public void callHello()  {
        System.out.println("in FooBean::callHello()");

        try {
            Context ic = new InitialContext();


            //

            FooLocalHome fooLocalHome = (FooLocalHome)
                ic.lookup("java:comp/env/ejb/melocal");
            FooLocal meLocal = fooLocalHome.create();
                
            System.out.println("Looking up ejb ref hello ");
            // create EJB using factory from container 
            Object objref = ic.lookup("java:comp/env/ejb/hello");
            System.out.println("objref = " + objref);
            System.err.println("Looked up home!!");
                
            HelloHome  home = (HelloHome)PortableRemoteObject.narrow
                (objref, HelloHome.class);
                                                                     
            System.err.println("Narrowed home!!");
            Hello hr = home.create();


            System.err.println("Got the EJB!!");
                
            System.out.println("invoking hello ejb");

            hr.sayHello();

            System.out.println("successfully invoked ejb");

            //

            System.out.println("Looking up ejb local ref hellolocal");

            HelloLocalHome  localHome = (HelloLocalHome)
                ic.lookup("java:comp/env/ejb/hellolocal");
            System.err.println("Looked up home!!");
                
            HelloLocal hl = localHome.create();
            System.err.println("Got the EJB!!");
                
            System.out.println("invoking hello ejb");

            hl.sayHello();

            System.out.println("successfully invoked local ejb");


            //

            System.out.println("Looking up ejb ref hello ");
            // create EJB using factory from container 
            objref = ic.lookup("java:comp/env/ejb/hello2");
            System.out.println("objref = " + objref);
            System.err.println("Looked up home!!");


            HelloHome  home2 = (HelloHome)PortableRemoteObject.narrow
                (objref, HelloHome.class);
                                                                     
            System.err.println("Narrowed home!!");
                
            Hello hr2 = home2.create();
            System.err.println("Got the EJB!!");
                
            System.out.println("invoking hello ejb");

            String said = hr2.sayHello();

            System.out.println("successfully invoked ejb");

            System.out.println("Looking up ejb local ref hellolocal2");

            HelloLocalHome  localHome2 = (HelloLocalHome)
                ic.lookup("java:comp/env/ejb/hellolocal2");
            System.err.println("Looked up home!!");
                
            HelloLocal hl2 = localHome2.create();
            System.err.println("Got the EJB!!");
                
            System.out.println("invoking hello2 ejb");

            String saidLocal = hl2.sayHello();

            System.out.println("successfully invoked local 2 ejb");

            if( (said != null) && said.equals(saidLocal) ) {
                System.out.println("successful return values from hello2");
            } else {
                throw new IllegalStateException("got wrong values " + said + ":" +
                                                saidLocal);
            }


            com.sun.ejb.Container cont = 
                ((com.sun.ejb.containers.EJBContextImpl) sc).getContainer();

            //
            // These test the Container.assertValidRemoteObject SPI
            //

            try {
                cont.assertValidRemoteObject(sc.getEJBObject());
                System.out.println("Assertion of my ejb object succeeded");
            } catch(javax.ejb.EJBException e) {
                e.printStackTrace();
                throw e;
            }

            try {
                cont.assertValidRemoteObject(null);
                throw new EJBException("assertion should have failed");
            } catch(javax.ejb.EJBException e) {
                System.out.println("Successfully caught the following exception:" + e.getMessage());
            }

            try {
                cont.assertValidRemoteObject(this);
                throw new EJBException("assertion should have failed");
            } catch(javax.ejb.EJBException e) {
                System.out.println("Successfully caught the following exception:" + e.getMessage());
            }

            try {
                cont.assertValidRemoteObject(home);
                throw new EJBException("assertion should have failed");
            } catch(javax.ejb.EJBException e) {
                System.out.println("Successfully caught the following exception:" + e.getMessage());
            }

            try {
                cont.assertValidRemoteObject(hr);
                throw new EJBException("assertion should have failed");
            } catch(javax.ejb.EJBException e) {
                System.out.println("Successfully caught the following exception:" + e.getMessage());
            }

            try {
                cont.assertValidRemoteObject(hr2);
                throw new EJBException("assertion should have failed");
            } catch(javax.ejb.EJBException e) {
                System.out.println("Successfully caught the following exception:" + e.getMessage());
            }

            try {
                cont.assertValidRemoteObject(meLocal);
                throw new EJBException("assertion should have failed");
            } catch(javax.ejb.EJBException e) {
                System.out.println("Successfully caught the following exception:" + e.getMessage());
            }

            //
            // These test the Container.assertValidLocalObject SPI
            //

            try {
                cont.assertValidLocalObject(meLocal);
                System.out.println("Assertion of my ejb local object succeeded");
            } catch(javax.ejb.EJBException e) {
                e.printStackTrace();
                throw e;
            }

            try {
                cont.assertValidLocalObject(null);
                throw new EJBException("assertion should have failed");
            } catch(javax.ejb.EJBException e) {
                System.out.println("Successfully caught the following exception:" + e.getMessage());
            }

            try {
                cont.assertValidLocalObject(hl);
                throw new EJBException("assertion should have failed");
            } catch(javax.ejb.EJBException e) {
                System.out.println("Successfully caught the following exception:" + e.getMessage());
            }

            try {
                cont.assertValidLocalObject(hl2);
                throw new EJBException("assertion should have failed");
            } catch(javax.ejb.EJBException e) {
                System.out.println("Successfully caught the following exception:" + e.getMessage());
            }

            try {
                cont.assertValidLocalObject(this);
                throw new EJBException("assertion should have failed");
            } catch(javax.ejb.EJBException e) {
                System.out.println("Successfully caught the following exception:" + e.getMessage());
            }

            try {
                cont.assertValidLocalObject(home);
                throw new EJBException("assertion should have failed");
            } catch(javax.ejb.EJBException e) {
                System.out.println("Successfully caught the following exception:" + e.getMessage());
            }

            try {
                cont.assertValidLocalObject(hr2);
                throw new EJBException("assertion should have failed");
            } catch(javax.ejb.EJBException e) {
                System.out.println("Successfully caught the following exception:" + e.getMessage());
                System.out.println("Successfully detected invalid ejb local object");
            }

        } catch(Exception e) {
            e.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(e);
            throw ise;
        }

    }

    public boolean assertValidRemoteObject(String msg)  {
        try {
            Context ic = new InitialContext();

            System.out.println("Looking up ejb ref hello ");
            // create EJB using factory from container 
            Object objref = ic.lookup("java:comp/env/ejb/hello");
            System.out.println("objref = " + objref);
            System.err.println("Looked up home!!");
                
            HelloHome  home = (HelloHome)PortableRemoteObject.narrow
                (objref, HelloHome.class);
                                                                     
            System.err.println("Narrowed home!!");
            Hello hr = home.create();

	    return (hr.assertValidRemoteObject() == null)
		? true: false;
	} catch (Exception ex) {
	    throw new EJBException(ex);
	}
    }

    public String assertValidRemoteObject()  {
        System.out.println("in FooBean::assertValidRemoteObject()");

        try {
            Context ic = new InitialContext();

            Object objref = ic.lookup("java:comp/env/ejb/hello2");
                
            HelloHome  home = (HelloHome)PortableRemoteObject.narrow
                (objref, HelloHome.class);
                                                                     
            Hello hr2 = home.create();

            hr2.sayHello();

            System.out.println("successfully invoked sayHello() ejb2");

            //
            com.sun.ejb.Container cont = 
                ((com.sun.ejb.containers.EJBContextImpl) sc).getContainer();

            //
            // This test the Container.assertValidRemoteObject SPI
            //

            try {
                cont.assertValidRemoteObject(sc.getEJBObject());
                System.out.println("assertValidRemoteObject: Successfully compared (ejbo1 == ejo1)");
            } catch(javax.ejb.EJBException e) {
                e.printStackTrace();
                System.out.println("assertValidRemoteObject: Failed (ejbo1 == ejbo1)");
                return "Failed same EJBObject assert: " + e;
            }

            try {
                cont.assertValidRemoteObject(home);
                String str = "Comparison of (ejbo and home) should have failed";
		System.out.println(str);
		return str;
            } catch(javax.ejb.EJBException e) {
                System.out.println("assertValidRemoteObject: Successfully caught expected exception:" + e.getMessage());
            }

            try {
                cont.assertValidRemoteObject(null);
                String str = "Comparison of (ejbo and null) should have failed";
		System.out.println(str);
		return str;
            } catch(javax.ejb.EJBException e) {
                System.out.println("assertValidRemoteObject: Successfully caught expected exception:" + e.getMessage());
            }

            try {
                cont.assertValidRemoteObject(this);
                String str = "Comparison of (ejbo and bean) should have failed";
		System.out.println(str);
		return str;
            } catch(javax.ejb.EJBException e) {
                System.out.println("assertValidRemoteObject: Successfully caught expected exception:" + e.getMessage());
            }

            try {
                cont.assertValidRemoteObject(hr2);
                String str = "Comparison of (ejbo and ejbo_from_diff_container) should have failed";
		System.out.println(str);
		return str;
            } catch(javax.ejb.EJBException e) {
                System.out.println("assertValidRemoteObject: Successfully caught expected exception:" + e.getMessage());
            }


	    return null;
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return "Caught unexpected exception: " + ex.toString();
	}
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
