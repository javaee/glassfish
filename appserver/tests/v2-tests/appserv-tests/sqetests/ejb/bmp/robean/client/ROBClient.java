/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2018 Oracle and/or its affiliates. All rights reserved.
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

package samples.ejb.bmp.robean.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.util.*;
import samples.ejb.bmp.robean.ejb.*; 
import com.sun.ejb.ReadOnlyBeanNotifier;
import com.sun.ejb.containers.ReadOnlyBeanHelper;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 * A simple java client will: 
 * <ul>
 * <li>Locates the home interface of the enterprise bean
 * <li>Gets a reference to the remote interface
 * <li>Invokes business methods
 * </ul>
 */
public class ROBClient {
    InitialContext initContext = null;
    AddressHome addressHome = null;
    CustomerHome customerHome = null;
    CustomerTransactionalHome customerTransactionalHome = null;
    CustomerRefreshHome customerRefreshHome = null;
    CustomerProgRefreshHome customerProgRefreshHome = null;
    Address address = null;
    Customer customer = null;
    CustomerTransactional customerTransactional = null;
    CustomerRefresh customerRefresh = null;
    CustomerProgRefresh customerProgRefresh = null;
    Hashtable env = new java.util.Hashtable(1);
    String JNDIName = null;
    Object objref = null;

    private SimpleReporterAdapter stat =
    new SimpleReporterAdapter("appserv-tests");
    
    public static void main(String[] args) {
        ROBClient client = new ROBClient();
        
        // run the tests
        client.runTestClient();
    }

    public void runTestClient() {
        System.out.println("Starting read only bmp test...");
        try{
            stat.addDescription("Testing read only bmp app.");
            doLookup();
            runTest();
            stat.printSummary("robAppID");
        } catch (Exception ex) {
            System.out.println("Exception in runTestClient: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void doLookup() {
        try {
            initContext = new javax.naming.InitialContext();
        } catch (Exception e) {
            System.out.println("Exception occured when creating InitialContext: " + e.toString());
          
        }
        
        try {
            JNDIName = "java:comp/env/ejb/address";
            objref = initContext.lookup(JNDIName);
            addressHome = (AddressHome)PortableRemoteObject.narrow(objref, AddressHome.class);
        } catch (Exception e) {
            System.out.println("Addressbean home not found - Is the bean registered with JNDI? : " + e.toString());
          
        }
        
        try {
            JNDIName = "java:comp/env/ejb/customer";
            objref = initContext.lookup(JNDIName);
            customerHome = (CustomerHome)PortableRemoteObject.narrow(objref, CustomerHome.class);
        } catch (Exception e) {
            System.out.println("Customerbean home not found - Is the bean registered with JNDI? : " + e.toString());
          
        }
        
        try {
            JNDIName = "java:comp/env/ejb/customerTransactional";
            objref = initContext.lookup(JNDIName);
            customerTransactionalHome = (CustomerTransactionalHome)PortableRemoteObject.narrow(objref, CustomerTransactionalHome.class);
        } catch (Exception e) {
            System.out.println("CustomerTransactional bean home not found - Is the bean registered with JNDI? : " + e.toString());
          
        }
        
        try {
            JNDIName = "java:comp/env/ejb/customerRefresh";
            objref = initContext.lookup(JNDIName);
            customerRefreshHome = (CustomerRefreshHome)PortableRemoteObject.narrow(objref, CustomerRefreshHome.class);
        } catch (Exception e) {
            System.out.println("CustomerRefresh bean home not found - Is the bean registered with JNDI? : " + e.toString());
           
        }
        
        try {
            JNDIName = "java:comp/env/ejb/customerProgRefresh";
            objref = initContext.lookup(JNDIName);
            customerProgRefreshHome = (CustomerProgRefreshHome)PortableRemoteObject.narrow(objref, CustomerProgRefreshHome.class);
        } catch (Exception e) {
            System.out.println("CustomerProgRefresh bean home not found - Is the bean registered with JNDI? : " + e.toString());            
        }
    }
    
    public void init() {
        doLookup();
    }
    
    public void runTest(){
        String SSN="123456789";
        //insert into customer1 values ('123456789', 'Smith', 'Rob', '1111', 'First Street', 'San Jose', 'CA', '12345', 123);
        double currentBalance = 0;
        double transactionalBalance = 0;
        double refreshBalance = 0;
        double progRefreshBalance = 0;
        double progRefreshBalance_beforeNotifier = 0;
        double autoRefreshBalance = 0;
        
        double amount=250;
        
        //first get balance
        
        
        
        try{
            Customer customer=customerHome.findByPrimaryKey(new PKString(SSN));
            CustomerRefresh customerRefresh=customerRefreshHome.findByPrimaryKey(SSN);            
            CustomerProgRefresh customerProgRefresh=customerProgRefreshHome.findByPrimaryKey(new PKString1(SSN));
            
            //update
        customer.doCredit(amount);
        customer.doDebit(amount);
            
            //trans balance
            
            //get transaction balance
            currentBalance = customer.getBalance();            
            System.out.println("Original balance is :"+currentBalance);      
            
            try{
                //get programmatic balance
                progRefreshBalance_beforeNotifier = customerProgRefresh.getBalance();
                System.out.println("Balance before Programmatic refresh :" +progRefreshBalance_beforeNotifier);   
                
                 if ( currentBalance == progRefreshBalance_beforeNotifier ) {
                         stat.addStatus("Programmatic_Refresh_ReadOnly_Bean", stat.PASS);
                    }
               else {
                         stat.addStatus("Programmatic_Refresh_ReadOnly_Bean", stat.FAIL);
                    }
                
                ReadOnlyBeanNotifier notifier =
                ReadOnlyBeanHelper.getReadOnlyBeanNotifier("java:comp/env/ejb/customerProgRefresh");
                notifier.refresh(new PKString1(SSN));
            } catch (Exception e) {
                  stat.addStatus("Programmatic_Refresh_ReadOnly_Bean", stat.FAIL);
                  System.out.println("Exception while looking up ReadOnlyBeanNotifier class");
                return;
            }            
            
            customer.doCredit(amount);
            
            progRefreshBalance = customerProgRefresh.getBalance();
            System.out.println("Balance after Programmatic refresh is :" +progRefreshBalance);   
            
              if ( progRefreshBalance == currentBalance ) {
                
                         stat.addStatus("Programmatic_Refresh_ReadOnly_Bean ", stat.FAIL);
                    }
               else {
                         stat.addStatus("Programmatic_Refresh_ReadOnly_Bean", stat.PASS);
                    }    
            
           System.out.println("Sleeping for 80 seconds");
           Thread.sleep(80000);
           refreshBalance = customerRefresh.getBalance();
           System.out.println("Balance after time refresh is :" +refreshBalance);         
            
            if ( refreshBalance == currentBalance ) {
                         stat.addStatus("Refresh_ReadOnly_Bean ", stat.FAIL);
                    }
               else {
                         stat.addStatus("Refresh_ReadOnly_Bean", stat.PASS);
                    }
            
         }catch(Exception e){
             stat.addStatus("Programmatic_Refresh_ReadOnly_Bean ", stat.FAIL);
             stat.addStatus("Refresh_ReadOnly_Bean ", stat.FAIL);
            e.printStackTrace();
        }
        
    }    

} 
