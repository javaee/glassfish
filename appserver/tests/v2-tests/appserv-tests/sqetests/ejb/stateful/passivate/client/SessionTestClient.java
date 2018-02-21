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
 * SessionTestClient.java
 *
 * Created on October 17, 2003, 4:14 PM
 */

package sqetests.ejb.stateful.passivate.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import sqetests.ejb.stateful.passivate.util.*;
import sqetests.ejb.stateful.passivate.ejb.stateful.*;
import java.io.*;
import java.util.*;
import java.text.*;

/**
 *
 * @author  Deepa Singh
 */
public class SessionTestClient {
    private String testSuiteID="";
    private SimpleReporterAdapter stat;
    
    public Context initial;
    public Object objref;
    SessionRemoteHome home=null;
    SessionRemote remote=null;    
    
    boolean beanLocated=false;
    String m_action="all";
    int m_clients=1;
    
    /** Creates a new instance of SessionTestClient */
    public SessionTestClient(String ts_id,String numClients,String action) {
        stat =new SimpleReporterAdapter("appserv-tests");
        stat.addDescription("This testsuites tests lifecycle of sfsb");    
        testSuiteID=ts_id;       
        m_clients=new Integer(numClients).intValue();
        m_action=action;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length <3){
            System.out.println("Enter testsuite name SessionTestClient <ts_name> <no_clients> <action> all|create|run");
            return;
        }
        SessionTestClient Client =new SessionTestClient(args[0],args[1],args[2]);
        if(Client.runSetup()){
        Client.runStatefulTest();
        }
        else
            System.err.println("Test didn't run");        
    }
    
    public boolean runSetup() {
        System.out.println("Test Execution Starts---------->");
        try{
            initial = new InitialContext();
            System.out.println("Looking up SFSB0");
            objref = initial.lookup("java:comp/env/ejb/SFSBSession");
            home=(SessionRemoteHome)PortableRemoteObject.narrow(objref,
            SessionRemoteHome.class);
            beanLocated=true;
        }catch(Throwable e){
            System.out.println("Lookup of beans failed");
            e.printStackTrace();
            beanLocated=false;
        }
        return beanLocated;
        
    }
    
    public void runStatefulTest(){   
         SessionRemote[] remote=new SessionRemote[10];
        /*
         *Due to performance reasons, beans are not immediately 
         passivated after they are identified as candidates for passivation.
         The container performs passivations in batches 
         (that is after it accumulates some number of beans - which is set to 8 internally)
         *This is the reason why you do not see a passivation. 
         If you had created more than 8 sessions then you would see the expected behaviour. 
         *hence number 10 is chosen.
         *
         **/
        try {
            for(int i=0;i<10;i++){
        remote[i]=home.create("<"+i+">");
        }                   
            
            System.out.println("Started transaction on Stateful Bean 5,\n shouldn't get passivated");
            System.out.println(remote[5].txMethod());
            System.out.println("Now going to sleep for 40 secs to passivate beans");
            Thread.sleep(40000);
            //System.out.println("after getting activated"+remote[9].getMessage());
            stat.addStatus(testSuiteID+" "+"10 SFSB Creation",stat.PASS);
        }catch(javax.ejb.CreateException e){
            System.out.println("Error while creating beans");
            e.printStackTrace();
            stat.addStatus(testSuiteID+" "+"10 SFSB Creation",stat.FAIL);            
        }catch(java.lang.InterruptedException e){
            System.out.println("Error while sleeping");
            e.printStackTrace();
        }catch(Throwable e){
            System.out.println("Something unexpected happened,check logs");
            e.printStackTrace();
            stat.addStatus(testSuiteID+" "+"10 SFSB Creation",stat.FAIL);
        }
         try{             
             for(int i=0;i<10;i++){
                 System.out.println("......"+remote[i].getMessage());
                 remote[i].afterActivationBusinessMethod();
            }
        
         }catch(java.rmi.NoSuchObjectException e){
             System.out.println("java.rmi.NoSuchObjectException");
             System.out.println("Bean 9 removed");
             
             e.getMessage();
         }catch(java.rmi.RemoteException e){
             System.out.println("unforseen circumstances");
             e.printStackTrace();
         }catch(Throwable e){
             e.printStackTrace();
         }
         
         try{
             HashMap finalResult=new HashMap();
             finalResult=remote[9].getEJBRecorder();             
             System.out.println("Result Map====="+finalResult.toString());
             //i <9 instead of 10 as 9th bean is removed
             for(int i=0;i<10;i++){
                 String beankey=new String("<"+i+">");
                 //first echo results for one bean
                 HashMap singleBeanResult=(HashMap)finalResult.get(beankey);
                 String passivateresult=singleBeanResult.get(new String("passivate")).toString();
                 String activateresult=singleBeanResult.get(new String("activate")).toString();
                 System.out.println("ejbPassivate for bean <"+ i+"> :"+passivateresult);
                 System.out.println("ejbActivate for bean <"+ i+"> :"+activateresult);
                 if(i==5){
                     if(activateresult.equalsIgnoreCase("false")){
                         System.out.println("Bean 5 expectedly fails activation");
                         
                     }
                 }
             }
   
            // Test run is over,remove all SFSB after this(productization of test,returns server to clean state
            //close all resources in SFSB remove methods
            for(int i=0;i<10;i++){
                try{
                    remote[i].remove();
                    
                }catch(javax.ejb.RemoveException e){
                    System.out.println("Error while removing  :"+i+"SFSB");
                    if(i==5)
                        System.out.println("Bean 5 throws RemoveException");
                }catch(java.rmi.NoSuchObjectException e){
                    System.out.println("Error while removing  :"+i+"SFSB");
                    if(i==9)
                        System.out.println("Bean 9 is already removed");
                }catch(Exception e){
                    System.out.println("Error while removing  :"+i+"SFSB");
                    e.printStackTrace();
                }
            }
            stat.addStatus(testSuiteID+" "+"SFSB_removal",stat.PASS);
        }catch(Throwable e){
            e.printStackTrace();
            stat.addStatus(testSuiteID+" "+"SFSB_removal",stat.FAIL);
        }
        stat.printSummary();
    }     
    
}
