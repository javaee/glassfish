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
 * RequestEndAccessObjectImplTest.java
 * JUnit based test
 *
 * Created on July 14, 2005, 10:47 AM
 */

package com.sun.enterprise.admin.monitor.callflow;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import junit.framework.*;
import java.sql.Connection;
import com.sun.enterprise.admin.monitor.callflow.TableInfo;

/**
 *
 * @author Harpreet Singh
 */
public class RequestEndAccessObjectImplTest extends TestCase {

    Connection con = null;
    TableAccessObject re = null;
    PreparedStatement pstmt = null;
    RequestEndTO[] requestEnd = new RequestEndTO[10];
    

    public RequestEndAccessObjectImplTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
      try{
            // TODO code application logic here
            String url="jdbc:derby://localhost:1527/sun-callflow;retrieveMessagesFromServerOnGetMessage=true;create=true;";            
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            con = DriverManager.getConnection(url, "APP", "APP");         
            // drop request start table
            re = RequestEndAccessObjectImpl.getInstance();            
        } catch (Exception e){
            e.printStackTrace();
        }        
    }

    protected void tearDown() throws Exception {
        try{
            con.close();
        } finally{
            con = null;            
        }
    }
    /**
     * Test of createTable method, of class com.sun.enterprise.admin.monitor.callflow.RequestEndAccessObjectImpl.
     */
    public void testCreateTable() {
        System.out.println("Request End testCreateTable");
        boolean result = false;
        try{
            result = re.createTable(con);
            System.out.println("Request End Create Table returned = "+ result);
        }catch (Exception e){
            e.printStackTrace();
        }
        assertTrue(result);        
    }

   public void testInsert (){
        System.out.println(" testStorerequestEnd");
        try{
            String insertSQL = re.getInsertSQL();
            System.out.println (" Insert SQL :"+ insertSQL);            
            pstmt = con.prepareStatement(insertSQL);
            
            for (int i = 0; i < requestEnd.length; i++) {
                requestEnd[i] = new RequestEndTO();
                requestEnd[i].requestId = "RequestID_"+i;
                requestEnd[i].timeStamp = System.nanoTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean result = re.insert (pstmt, requestEnd);
        System.out.println("testStorerequestEnd returned = "+result);
        assertTrue (result);
    }

   /**
     * Test of dropTable method, of class com.sun.enterprise.admin.monitor.callflow.RequestEndAccessObjectImpl.
     */
    public void testDropTable() {
        System.out.println("Request End testDropTable");
        boolean result = re.dropTable(con);
        System.out.println("Request End Drop Table returned = "+result);
        assertTrue (result);        
    }

    public static void main(java.lang.String[] argList) {
    }
    
}
