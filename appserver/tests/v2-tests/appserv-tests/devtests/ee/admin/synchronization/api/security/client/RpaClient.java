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

package com.sun.devtest.admin.synchronization.api.security.client;

import java.util.ArrayList;

import java.io.Serializable;
import javax.naming.*;
import javax.jms.*;
import javax.ejb.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.NoSuchObjectException;
import java.util.Properties;

import com.sun.devtest.admin.synchronization.api.security.shopping.*;

//import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class RpaClient {

    // consts
    public static String kTestNotRun    = "TEST NOT RUN";
    public static String kTestPassed    = "TEST PASSED";
    public static String kTestFailed    = "TEST FAILED";
    
    //private static SimpleReporterAdapter stat = 
     //   new SimpleReporterAdapter("appserv-tests");


    private RpaRemote hr = null;

    public static void main (String[] args) {

       //stat.addDescription("synchronization");
       if ( args.length != 3) {
            //stat.addStatus("client initSFSB-- wrong number of arguments", stat.FAIL);
            System.out.println("Usage: SyncClientTest provider-url ctxFactory jndi-name");
             return;
        } 

        RpaClient client = new RpaClient(args);
        System.out.println("[apiClient] doTest()...");
        client.doTest(args);
        System.out.println("[apiClient] DONE doTest()...");
        //stat.printSummary("synchronization");
    }  
    
    public RpaClient (String[] args) {
    }
    
    public void doTest(String[] args) {

        initSFSB(args);
        testSynch(args);
    }

    private void initSFSB(String[] args) {

        System.out.println("[apiClient] Inside init....");
        try {
            Context ic = null;
            Object objref = null;
            if ((args[0] == null) || (args[1] == null)) { 
                ic = new InitialContext();
                System.out.println("[apiClient] Lookingup Bean apiClient ");
                objref = ic.lookup("java:comp/env/ejb/apiSecurity");
            } else {
                Properties env = new Properties();
                env.put("java.naming.provider.url", args[0]);
                env.put("java.naming.factory.initial", args[1]);
                ic = new InitialContext(env);
                objref = ic.lookup(args[2]);
            }

            RpaHome home = (RpaHome)
                PortableRemoteObject.narrow (objref, RpaHome.class);

            hr = home.create("LizHurley");

            System.out.println("[passivateactivate] Initalization done");
            //stat.addStatus("init apiClient", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[apiClient] Exception in init....");
            e.printStackTrace();
            //stat.addStatus("client initSFSB", stat.FAIL);
        }
    }

    public void testSynch(String[] args) {
    		// invoke 3 overloaded methods on the EJB
        try{
		System.out.println ("Calling authorized method - addItem");
		hr.addItem("lipstick", 30);
		hr.addItem("mascara", 40);
                hr.addItem("lipstick2", 50);
                hr.addItem("sandals",  200);
                System.out.println(hr.getTotalCost());
                hr.deleteItem("lipstick2");
                java.lang.String[] shoppingList = hr.getItems();
                System.out.println("Shopping list for LizHurley");
                for (int i=0; i<shoppingList.length; i++){
                    System.out.println(shoppingList[i]);
                }

                System.out.println("Total Cost for Ms Hurley = "+
                    hr.getTotalCost());

        } catch(Exception re){
                re.printStackTrace();
		System.out.println("RealmPerApp:RpaLoginBean Test Failed");
                System.exit(-1);
	}
            System.out.println("RealmPerApp:RpaLoginBean Test Passed");

    }

} //Client{}
