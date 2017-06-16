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

package com.sun.s1asdev.ejb.stubs.stubser.client;

import java.io.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import com.sun.s1asdev.ejb.stubs.stubser.FooHome;
import com.sun.s1asdev.ejb.stubs.stubser.Foo;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static final String HOME_HANDLE_FILE = "homehandle";
    private static final String FOO_HANDLE_FILE = "foohandle";

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

	System.out.println("cp = " + System.getProperty("java.class.path"));

        Client client = new Client(args);
	System.out.println("args.length = " + args.length);
	for(int i = 0; i < args.length; i++) {
	    System.out.println("arg " + i + " = " + args[0]);
	}
	

        if( args.length == 1) {
            stat.addDescription("ejb-stubs-stubser2");
            client.doRestartTest();
            stat.printSummary("ejb-stubs-stubser2ID");
        } else {
            stat.addDescription("ejb-stubs-stubser1");
            client.doTest();
            stat.printSummary("ejb-stubs-stubser1ID");
        }
        
    }  
    
    public Client (String[] args) {
    }
    
    public void doRestartTest() {
        try {
            // when we lookup a FooHome *before* deserializing the home reference,
            // everything works
	    // @@@
            System.out.println("Adding lookup before reconstitution ...");
            lookupHome();

            System.out.println("Attempting to reconstruct foo handle");
	    Handle fh = (Handle) readFromFile(FOO_HANDLE_FILE);
            Foo f = (Foo) fh.getEJBObject();
	    f.callHello();
	    
	    System.out.println("successfully invoked foo via reconsituted handle");

            System.out.println("Attempting to reconstruct home handle as first" +
                               " operation in client");
            HomeHandle hh = (HomeHandle) readFromFile(HOME_HANDLE_FILE);
            invoke((FooHome) hh.getEJBHome());

	    System.out.println("successfully invoked ejb via reconsituted home");

	    
            stat.addStatus("ejbclient restart2", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("ejbclient restart2" , stat.FAIL);
        }

    }

    public void doTest() {

        try {


            FooHome  home = lookupHome();
                                                                     
            System.err.println("Narrowed home!!");
            
	   

            // create home handle
            HomeHandle hh = home.getHomeHandle();
            serializeToFile(HOME_HANDLE_FILE, hh);
            HomeHandle hh2 = (HomeHandle) readFromFile(HOME_HANDLE_FILE);
            home = (FooHome) hh2.getEJBHome();

            Foo f = invoke(home);
            System.out.println("successfully invoked ejb via reconsituted home");

	    // create SFSB instance handle file
            Handle fh = f.getHandle();
            serializeToFile(FOO_HANDLE_FILE, fh);
            Handle fh2 = (Handle) readFromFile(FOO_HANDLE_FILE);
            Foo f2 = (Foo) fh2.getEJBObject();
	    f2.callHello();

            System.out.println("successfully invoked ejb");

            stat.addStatus("ejbclient main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("ejbclient main" , stat.FAIL);
        }
        
    	return;
    }

    private FooHome lookupHome() throws Exception {
        Context ic = new InitialContext();

        System.out.println("Looking up ejb ref ");
        // create EJB using factory from container 
        Object objref = ic.lookup("java:global/ejb-stubs-stubserApp/ejb-stubs-stubser-ejb/FooBean");
        System.out.println("objref = " + objref);
        System.err.println("Looked up home!!");
        
        FooHome  home = (FooHome)PortableRemoteObject.narrow
            (objref, FooHome.class);
        
        System.err.println("Narrowed home!!");

        return home;
    }
            
    private Foo invoke(FooHome fooHome) throws Exception {

        Foo f = fooHome.create();
        System.err.println("Got the EJB!!");
        
        // invoke method on the EJB
        System.out.println("invoking ejb");
        f.callHello();

	return f;

    }

    private void serializeToFile(String filename, Serializable s) throws Exception {
        FileOutputStream fos = new FileOutputStream(filename);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(s);
        os.close();
        System.out.println("serialized " + filename);
    }

    private Serializable readFromFile(String filename) throws Exception {
        FileInputStream fis = new FileInputStream(filename);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Serializable s = (Serializable) ois.readObject();
        ois.close();
        System.out.println("Reconstituted " + filename);

        return s;
    }

}

