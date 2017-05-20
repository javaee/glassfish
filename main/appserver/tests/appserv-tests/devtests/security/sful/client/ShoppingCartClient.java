/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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

package shopping;

import java.io.*;
import java.util.*;
import javax.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import com.sun.enterprise.security.LoginContext;
//import com.sun.enterprise.security.auth.login.common.LoginException;
import java.rmi.RemoteException;
import java.security.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class ShoppingCartClient {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        ShoppingCartClient client = new ShoppingCartClient(args);
        client.doTest();
    }
    
    public ShoppingCartClient(String[] args) {
        //super(args);
    }
    
    public String doTest() {
        
	ShoppingCartRemote hr=null;
        String res=null;
        Context ic = null;
        LoginContext lc=null;
        ShoppingCartHome home=null;
        String testId = "Sec::Stateful Login Bean";
    	try {
            stat.addDescription("Security::Statefule Login Bean");
	    ic = new InitialContext();
            // create EJB using factory from container 
            java.lang.Object objref = ic.lookup("statefulLoginBean");
		
	    System.err.println("Looked up home!!");
		
	    home = (ShoppingCartHome)PortableRemoteObject.narrow(
					   objref, ShoppingCartHome.class);
	    System.err.println("Narrowed home!!");
				
            hr = home.create("LizHurley");
            System.out.println("Got the EJB!!");

            // invoke 3 overloaded methods on the EJB
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

            boolean canSaveQuote;
            try {
                hr.saveAsQuote();
                canSaveQuote = true;
            } catch(Exception ex) {
                canSaveQuote = false;
            }

            if (canSaveQuote) {
                stat.addStatus(testId, stat.FAIL);
                System.out.println("RealmPerApp:RpaLoginBean Test Failed");
            } else {
                stat.addStatus(testId, stat.PASS);
                System.out.println("RealmPerApp:RpaLoginBean Test Passed");
            }
        } catch(Exception re){
            re.printStackTrace();
            stat.addStatus(testId, stat.FAIL);
            System.out.println("Shopping Cart:StatefulLoginBean Test Failed");
            System.exit(-1);
	} finally {
            stat.printSummary();
        }
        System.out.println("ShoppingCart:StatefulLoginBean Test Passed");
        return res;
        
    }

    
    public final static String helloStr = "Hello ShoppingCart!!!";
}

