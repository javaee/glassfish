/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * Client.java
 *
 * Created on February 21, 2003, 3:20 PM
 */

import java.util.Collection;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 *
 * @author  mvatkina
 * @version
 */
public class Client {
    private static create.A1Home a1home;
    private static create.A1UnPKHome a1unhome;
    private static create.A2Home a2home;
    private static create.A2UnPKHome a2unhome;
    
    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
       
        try {
            System.out.println("START");
            stat.addDescription("createExceptionsTest");

            lookupBeans();
            runCreateException20Test();
            runCreateException11Test();
            runBeanStateResetTest();

	    stat.addStatus("ejbclient createExceptionsTest", stat.PASS);
            System.out.println("FINISH");

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
	    stat.addStatus("ejbclient createExceptionsTest", stat.FAIL);
        }
           stat.printSummary("createExceptionsTest");
    }

    private static void lookupBeans() throws NamingException {

        Context initial = new InitialContext();

        Object objref = initial.lookup("java:comp/env/ejb/CR2");
        a2home = (create.A2Home)PortableRemoteObject.narrow(objref, create.A2Home.class);

        objref = initial.lookup("java:comp/env/ejb/CRUN2");
        a2unhome = (create.A2UnPKHome)PortableRemoteObject.narrow(objref, 
                create.A2UnPKHome.class);

        objref = initial.lookup("java:comp/env/ejb/CR1");
        a1home = (create.A1Home)PortableRemoteObject.narrow(objref, create.A1Home.class);

        objref = initial.lookup("java:comp/env/ejb/CRUN1");
        a1unhome = (create.A1UnPKHome)PortableRemoteObject.narrow(objref, 
                create.A1UnPKHome.class);

    }

    /**
     * Test CreateException behavior in ejbCreate/ejbPostCreate for CMP 2.x beans.
     * If ejbCreate throws CreateException, the transaction is rolled back.
     * If ejbPostCreate throws CreateException, the transaction is committed.
     */
    private static void runCreateException20Test() throws java.rmi.RemoteException, 
            javax.ejb.FinderException, javax.ejb.RemoveException {

        // Test CreateException from ejbPostCreate
        try {
            a2home.create("A2");
        } catch (javax.ejb.CreateException e) {
            System.out.println("Caught expected CreateException: " + e.getMessage());
        }

        // The bean should be created. If not - an exception would be thrown.
        create.A2 a2bean = a2home.findByPrimaryKey("A2");
        System.out.println("FOUND A2 Bean after failed ejbPostCreate");

        // Test CreateException from ejbCreate 
        try {
            a2home.create();
        } catch (javax.ejb.CreateException e) {
            System.out.println("Caught expected CreateException: " + e.getMessage());
        }

        // Only 1 A2 bean should be found.
        Collection c = a2home.findAll();
        if (c.size() == 1)
            System.out.println("FOUND 1 A2 Bean after failed ejbCreate");
        else
            System.out.println("ERROR found " + c.size() + " A2 Beans");

        // Test 2.0 bean with Unknown PK for CreateException from ejbPostCreate.
        try {
            a2unhome.create("A2");
        } catch (javax.ejb.CreateException e) {
            System.out.println("Caught expected CreateException: " + e.getMessage());
        }

        // Bean should be created.
        c = a2unhome.findAll();
        if (c.size() == 1)
            System.out.println("FOUND A2 Bean with Unknown PK after failed ejbPostCreate");
        else
            System.out.println("ERROR found " + c.size() 
                + " A2 Beans with Unknown PK after failed ejbPostCreate");

    }

    /**
     * Test CreateException behavior in ejbCreate/ejbPostCreate for CMP 1.1 beans.
     * If ejbCreate throws CreateException, the transaction is rolled back.
     * If ejbPostCreate throws CreateException, the transaction is committed.
     */
    private static void runCreateException11Test() throws java.rmi.RemoteException, 
            javax.ejb.FinderException, javax.ejb.RemoveException {

        // Test CreateException from ejbPostCreate
        try {
            a1home.create("A1");
        } catch (javax.ejb.CreateException e) {
            System.out.println("Caught expected CreateException: " + e.getMessage());
        }

        // The bean should be created. If not - an exception would be thrown.
        create.A1 a1bean = a1home.findByPrimaryKey("A1");
        System.out.println("FOUND A1 Bean after failed ejbPostCreate");

        // Test CreateException from ejbPostCreate without a container transaction.
        try {
            a1home.create("A11", true); 
        } catch (javax.ejb.CreateException e) {
            System.out.println("Caught expected CreateException: " + e.getMessage());
        }

        // The bean should be created. If not - an exception would be thrown.
        a1bean = a1home.findByPrimaryKey("A11");
        System.out.println("FOUND A1 Bean (A11) after failed ejbPostCreate");

        // We will also test non-transactional remove, as there are no other
        // CMP unit test that tests this feature.
        a1bean.remove(); 

        // Only 1 A1 bean should be left.
        Collection c = a1home.findAll();
        if (c.size() == 1)
            System.out.println("FOUND 1 A1 Bean after non-tx remove");
        else
            System.out.println("ERROR found " + c.size()
                + " A1 Beans after non-tx remove");

        // Test CreateException from ejbCreate without a container transaction.
        try {
            a1home.create("A111", false); 
        } catch (javax.ejb.CreateException e) {
            System.out.println("Caught expected CreateException: " + e.getMessage());
        }

        // Nothing should be added to A1 beans.
        c = a1home.findAll();
        if (c.size() == 1)
            System.out.println("FOUND 1 A1 Beans after failed ejbCreate");
        else
            System.out.println("ERROR found " + c.size()
                + " A1 Beans after failed ejbCreate");

        // Test 1.1 bean with Unknown PK for CreateException from ejbPostCreate.
        try {
            a1unhome.create("A1");
        } catch (javax.ejb.CreateException e) {
            System.out.println("Caught expected CreateException: " + e.getMessage());
        }

        // Bean should be created.
        c = a1unhome.findAll();
        if (c.size() == 1)
            System.out.println("FOUND A1 Bean with Unknown PK after failed ejbPostCreate");
        else
            System.out.println("ERROR found " + c.size() 
                + " A1 Beans with Unknown PK after failed ejbPostCreate");

    }

    /** Test bean state to be reset before invoking bean's ejbCreate method.
     * Use different create() methods not to get unexpected CreateExceptions.
     */
    private static void runBeanStateResetTest() throws java.rmi.RemoteException {
        try {
            a2home.create(200);
            System.out.println("State of A2 Bean is reset");
        } catch (Exception e) {
            System.out.println("ERROR creating A2 Bean: " + e);
        }

        try {
            a2unhome.create(201);
            System.out.println("State of A2 Bean with Unknown PK is reset");
        } catch (Exception e) {
            System.out.println("ERROR creating A2 Bean with Unknown PK: " + e);
        }

        try {
            a1home.create(100);
            System.out.println("State of A1 Bean is reset");
        } catch (Exception e) {
            System.out.println("ERROR creating A1 Bean: " + e);
        }

        try {
            a1unhome.create(101);
            System.out.println("State of A1 Bean with Unknown PK is reset");
        } catch (Exception e) {
            System.out.println("ERROR creating A1 Bean with Unknown PK: " + e);
        }

    }
    
}
