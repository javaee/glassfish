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

package pe.ejb.ejb30.persistence.toplinksample.client;

import java.io.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import java.rmi.*;
import pe.ejb.ejb30.persistence.toplinksample.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    
    private static @EJB StatelessInterface sless;
    List rows;
    Iterator i;

    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

    public static String testSuiteID;
    
    public static void main(String[] args) {
     
     if(args.length==1) {
		 testSuiteID=args[0];
            }
       System.out.println("The TestSuite ID : " + testSuiteID);
       System.out.println("The args length is : " + args.length);
        stat.addDescription("ejb3_slsb_persistence");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb3_slsb_persistenceID");
    }
    
    public Client(String[] args) {}
    
    public void doTest() {
        try{

            try {
                //--setup test: persist all entities
                System.out.println("Client: invoking stateful setup");
                sless.setUp();
                stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence cleanUp", stat.PASS);
            } catch(Exception e) {
                System.out.println("Client: Error in setUp");
                e.printStackTrace();
                stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence setup", stat.FAIL);
            }

            try {
                //--addOrders test: add some orders through customer
                System.out.println("Client: getting customer orders");
                Collection c = sless.getCustomerOrders(1);
                if(c==null){
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getCustomerOrders:relationshipTest", 
                            stat.FAIL);
                    System.out.println("Client: got NULL Orders");
                } else {
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getCustomerOrders:relationshipTest", 
                            stat.PASS);
                    System.out.println("Client: got Orders of class:"+c.getClass().getName());
                    i=c.iterator();
                    while(i.hasNext())
                        System.out.println((OrderEntity)i.next());
                }
            } catch(Exception e) {
                System.out.println("Client: Error in getCustomerOrders:relationshipTest");
                e.printStackTrace();
                stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getCustomerOrders:relationshipTest", stat.FAIL);
            }

            try{
                //--getCustomers test: get customer by name and city
                rows=sless.getCustomers("Alice", "Santa Clara");
                if(rows == null){
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getCustomers", stat.FAIL);
                } else { 
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getCustomers", stat.PASS);
                    System.out.println("Client: Got Rows. Listing...");
                    i=rows.iterator();
                    while(i.hasNext())
                        System.out.println((CustomerEntity)i.next());
                }
            } catch(Exception e) {
                System.out.println("Client: Error in getCustomers");
                e.printStackTrace();
                stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getCustomers", stat.FAIL);
            }

            try{
                //--getAllCustomers test: get all customers
                rows=sless.getAllCustomers();
                if(rows == null){
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getAllCustomers", stat.FAIL);
                } else { 
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getAllCustomers", stat.PASS);
                    System.out.println("Client: Got allCustomer rows. Listing...");
                    i=rows.iterator();
                    while(i.hasNext())
                        System.out.println((CustomerEntity)i.next());
                }
            } catch(Exception e) {
                System.out.println("Client: Error in getAllCustomers");
                e.printStackTrace();
                stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getAllCustomers", stat.FAIL);
            }

            try{
                //--getAllItemsByName test
                rows=sless.getAllItemsByName();
                if(rows == null){
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getAllItemsByName", stat.FAIL);
                } else { 
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getAllItemsByName", stat.PASS);
                    System.out.println("Client: Got allItemsByName rows. Listing...");
                    i=rows.iterator();
                    while(i.hasNext())
                        System.out.println((ItemEntity)i.next());
                }
            } catch(Exception e) {
                System.out.println("Client: Error in getAllItemsByName");
                e.printStackTrace();
                stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getAllItemsByName", stat.FAIL);
            }

            try{
                //--getAllOrdersByItem test
                rows=sless.getAllItemsByName();
                if(rows == null){
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getAllOrdersByItem", 
                            stat.FAIL);
                } else { 
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getAllOrdersByItem", 
                            stat.PASS);
                    System.out.println("Client: Got AllOrdersByItem rows. Listing...");
                    i=rows.iterator();
                    while(i.hasNext())
                        System.out.println((ItemEntity)i.next());
                }
            } catch(Exception e) {
                System.out.println("Client: Error in getAllOrdersByItem");
                e.printStackTrace();
                stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getAllOrdersByItem", stat.FAIL);
            }

            try{
                //--cleanup test: remove all persisted entities
                System.out.println("Cleanup: DELETING ROWS...");
                sless.cleanUp();
                stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence cleanUp", stat.PASS);
            } catch(Exception e) {
                System.out.println("Client: Error in cleanUp");
                e.printStackTrace();
                stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence cleanUp", stat.FAIL);
            }

            //System.out.println("Client: Inserted and deleted row "+
            //        "through 3.0 persistence entity");
            //System.out.println(sless.getMessage());
            //stat.addStatus("ejb3_slsb_persistence sfsb_persistent_insert", stat.PASS);
        } catch(Throwable e) {
            System.out.println("Client: Unexpected Error,check server.log");
            e.printStackTrace();
            stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence ALLTESTS", stat.FAIL);
        }
        return;
    }   
}
