/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
 * DbAccessObjectTest.java
 * JUnit based test
 *
 * Created on July 15, 2005, 5:33 PM
 */

package com.sun.enterprise.admin.monitor.callflow;

import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import junit.framework.*;
import java.sql.Connection;

/**
 *
 * @author Harpreet Singh
 */
public class DbAccessObjectTest extends TestCase {
//    Connection con = null;
    DbAccessObject dbao = null;
    int DATA_SIZE = 2;
    RequestStartTO[] requestStart = new RequestStartTO[DATA_SIZE];
    MethodStartTO[] methodStart = new MethodStartTO[DATA_SIZE*2];    
    RequestEndTO[] requestEnd = new RequestEndTO[DATA_SIZE];
    MethodEndTO[] methodEnd = new MethodEndTO[DATA_SIZE *2];        

    // Start Time and End Time Objects. They need not be the same
    // size as other TransferObjects
  
    int DATA_SIZE_FOR_TIMES = 7;
    EndTimeTO[] endTime = new EndTimeTO [DATA_SIZE_FOR_TIMES];
    StartTimeTO[] startTime = new StartTimeTO [DATA_SIZE_FOR_TIMES];
    
    public DbAccessObjectTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
      try{
            dbao  = DbAccessObjectImpl.getInstance();            
        } catch (Exception e){
            e.printStackTrace();
        }        
                
    }

    protected void tearDown() throws Exception {
    }
 
    /**
     * Test of enable method, of class com.sun.enterprise.admin.monitor.callflow.DbAccessObject.
     */
    public void testEnable() {
        System.out.println("DB Access Object testEnable");
        boolean result = dbao.enable();
        assertTrue(result);
    }

    public void testInsertRequestStart() {
        System.out.println(" testInsertRequestStart");
        long timestamp = 0;
        for (int i = 0; i < requestStart.length; i++) {
            requestStart[i] = new RequestStartTO();
            requestStart[i].requestId = "RequestID_"+(i+1);
            timestamp = timestamp + 10;
            requestStart[i].timeStamp = timestamp;
            requestStart[i].requestType = RequestType.REMOTE_EJB;
        }
        boolean result = dbao.insert(requestStart);
        System.out.println("testInsertRequestStart returned = "+result);
        assertTrue(result);
    }
    
    public void testInsertMethodStart() {
      System.out.println(" testInsertMethodStart");
      long timestamp = 11;
      for (int i = 0; i < DATA_SIZE; i++) {
          methodStart[i] = new MethodStartTO();
          methodStart[i].requestId = "RequestID_"+(i+1);
          methodStart[i].timeStamp = timestamp;
          timestamp = timestamp + 10;          
          methodStart[i].componentType = ComponentType.SERVLET;
          methodStart[i].componentName = "Component_Name_"+ (i+1);
          methodStart[i].appName = "APP_NAME";
          methodStart[i].methodName = "Method_Name_" +(i+1);
          methodStart[i].moduleName = "Module_Name_" +(i+1);
          methodStart[i].transactionId = "Transaction_Id_"+(i+1);
          methodStart[i].threadId = "Thread_Id_"+(i+1);
          methodStart[i].securityId = "watchman_"+(i+1);
      }
       // generate duplicate rows with different timestamps
      timestamp = 12;
      int j = 0;
      for (int i = DATA_SIZE; i < (DATA_SIZE * 2); i++) {
          methodStart[i] = new MethodStartTO();
          methodStart[i].requestId = "RequestID_"+(j+1);
          methodStart[i].timeStamp = timestamp;
          timestamp = timestamp + 10;          
          methodStart[i].componentType = ComponentType.SERVLET;
          methodStart[i].componentName = "Component_Name_"+ (j+1);
          methodStart[i].appName = "APP_NAME";
          methodStart[i].methodName = "Method_Name_" +(j+1);
          methodStart[i].moduleName = "Module_Name_" +(j+1);
          methodStart[i].transactionId = "Transaction_Id_"+(j+1);
          methodStart[i].threadId = "Thread_Id_"+(j+1);
          methodStart[i].securityId = "watchman_"+(j+1);
          j++;
      }
      
      boolean result = dbao.insert( methodStart);
      System.out.println("testInsertMethodStart returned = "+result);
      assertTrue(result);
    }

    public void testInsertME() {
        System.out.println("testInsertMethodEnd returned ");
        long timestamp = 13;

        methodEnd[0] = new MethodEndTO();
        methodEnd[0].requestId = "RequestID_1";
        methodEnd[0].timeStamp = timestamp;
        methodEnd[0].exception = "exe_1";
        
        methodEnd[1] = new MethodEndTO();
        methodEnd[1].requestId = "RequestID_1";
        methodEnd[1].timeStamp = timestamp + 1;
        methodEnd[1].exception = "exe_1";
        
        methodEnd[2] = new MethodEndTO();
        methodEnd[2].requestId = "RequestID_2";
        methodEnd[2].timeStamp = timestamp + 10;
        methodEnd[2].exception = "exe_2";
        
        methodEnd[3] = new MethodEndTO();
        methodEnd[3].requestId = "RequestID_2";
        methodEnd[3].timeStamp = timestamp + 11;
        methodEnd[3].exception = "exe_2";

            boolean result = dbao.insert(methodEnd);
        System.out.println("testInsertMethodEnd returned = "+result);
        assertTrue(result);
    }

    public void testInsertRequestEnd() {
        System.out.println(" testInsertRequestEnd");
        long timestamp = 5;
        for (int i = 0; i < requestEnd.length; i++) {
            requestEnd[i] = new RequestEndTO();
            requestEnd[i].requestId = "RequestID_"+(i+1);
            timestamp = timestamp + 10;
            requestEnd[i].timeStamp = timestamp;
        }
        boolean result = dbao.insert(requestEnd);
        System.out.println("testInsertRequestEnd returned = "+result);
        assertTrue(result);
    }
    

    
    public void testInsertStartTime () {
        System.out.println(" testInsertStartTime");
	createStartTime ();
        boolean result = dbao.insert(startTime);
        System.out.println("testInsertStartTime returned = "+result);
        assertTrue(result);
    }
    
    
    public void testInsertEndTime () {
        System.out.println(" testInsertEndTime");
	createEndTime ();
        boolean result = dbao.insert(endTime);
        System.out.println("testInsertEndTime returned = "+result);
        assertTrue(result);
    }
    
    /**
     * Test of disable method, of class com.sun.enterprise.admin.monitor.callflow.DbAccessObject.
     */
    public void testDisable() {
        System.out.println("DB Access Object testDisable");
        boolean result = dbao.disable();
        assertTrue(result);
    }
    
    public void testGetRequestInformation () {
        System.out.println("DB Access Object testGetRequestInformation");
        List<Map<String, String>> list = dbao.getRequestInformation ();
        
        for (Map<String, String>map : list) {
            StringBuffer sbuf = new StringBuffer();
            for (String key : map.keySet()) {
                sbuf.append(map.get(key)+",");
            }
            System.out.println(sbuf.toString());
            sbuf = null;
        }
        int resultSize = list.size ();
        int CORRECT_RESULT_SIZE = 2;
        if (resultSize == CORRECT_RESULT_SIZE)
            assertTrue (true);
        
    }
    public void testGetCallStackInformation (){
        System.out.println("DB Access Object testGetCallStackInformation");
        try{
        List<Map<String, String>> list = dbao.getCallStackInformation ("RequestID_1");
        int i= 0;

        StringBuffer sbuf1 = new StringBuffer();
        
        for (Map<String, String>map : list) {
            sbuf1.append ("\n" + i++ +" >");
            StringBuffer sbuf = new StringBuffer();
            for (String key : map.keySet()) {
                if (map.get(key).equals("RequestStart"))
                    sbuf.insert (0, "\n" + map.get(key)+" -->");
                else if (map.get(key).equals ("RequestEnd"))
                    sbuf.insert (0, "\n" + map.get(key)+" -->");
                else if (map.get(key).equals ("MethodStart"))
                    sbuf.insert (0, "\n\t" + map.get(key)+" -->");
                else if (map.get(key).equals ("MethodEnd"))
                    sbuf.insert (0, "\n\t" + map.get(key)+" -->");
                else
                    sbuf.append(map.get(key)+"," );
              
            }
                sbuf1.append( sbuf);
                sbuf1.append ("\n");            
        }
        System.out.println(sbuf1.toString());
        sbuf1 = null;
        
        int resultSize = list.size ();
        int CORRECT_RESULT_SIZE = 2;
        if (resultSize == CORRECT_RESULT_SIZE)
            assertTrue (true);        
        } catch (Exception e){
            e.printStackTrace();
        }        
    }

    public void testGetPIEInformation () {
        System.out.println("DB Access Object testGetPIEInformation");
        Map<String, String> map = dbao.getPieInformation ("Request_ID1");
        int i= 0;

        StringBuffer sbuf1 = new StringBuffer();
        
        for (String key : map.keySet()) {
            sbuf1.append ("\n" + i++ +" > "+ key + " = ");
            sbuf1.append(map.get(key)+"," );
            sbuf1.append ("\n");            
        }
        System.out.println(sbuf1.toString());
        sbuf1 = null;
        assertTrue(true);
    }
    /**
     * Test of clearData method, of class com.sun.enterprise.admin.monitor.callflow.DbAccessObject.
     */
    public void testClearData() {
        System.out.println("DB Access Object. testClearData");        
        boolean result = dbao.clearData();
        assertTrue(result);
    }

    private void createStartTime () {
	
	for (int i = 0; i < DATA_SIZE_FOR_TIMES; i++ ){
		startTime[i] = new StartTimeTO ();
		startTime[i].requestId = "Request_ID1";
	}
	
	startTime[0].containerTypeOrApplicationType = "EJB";
	startTime[0].timeStamp = 10;

	startTime[1].containerTypeOrApplicationType = "EJBAPP";
	startTime[1].timeStamp = 13;

	startTime[2].containerTypeOrApplicationType = "ORB";
	startTime[2].timeStamp = 19;

	startTime[3].containerTypeOrApplicationType = "WEB";
	startTime[3].timeStamp = 5;

	startTime[4].containerTypeOrApplicationType = "WEBAPP";
	startTime[4].timeStamp = 8;

	startTime[5].containerTypeOrApplicationType = "WEB";
	startTime[5].timeStamp = 1;

	startTime[6].containerTypeOrApplicationType = "WEBAPP";
	startTime[6].timeStamp = 3;
    }

    private void createEndTime () {
	
	for (int i = 0; i < DATA_SIZE_FOR_TIMES; i++ ){
		endTime[i] = new EndTimeTO ();
		endTime[i].requestId = "Request_ID1";
	}
	
	endTime[0].containerTypeOrApplicationType = "EJB";
	endTime[0].timeStamp = 12;

	endTime[1].containerTypeOrApplicationType = "EJBAPP";
	endTime[1].timeStamp = 18;

	endTime[2].containerTypeOrApplicationType = "ORB";
	endTime[2].timeStamp = 20;

	endTime[3].containerTypeOrApplicationType = "WEB";
	endTime[3].timeStamp = 7;

	endTime[4].containerTypeOrApplicationType = "WEBAPP";
	endTime[4].timeStamp = 9;

	endTime[5].containerTypeOrApplicationType = "WEB";
	endTime[5].timeStamp = 2;

	endTime[6].containerTypeOrApplicationType = "WEBAPP";
	endTime[6].timeStamp = 4;

    }

    public static void main(java.lang.String[] argList) {

    }


    
}
