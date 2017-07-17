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

package org.glassfish.osgi_resources_test.client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {

        stat.addDescription("This is to test osgi-ee-resources modules");

        Connection con  = null;
        Statement stmt = null;
        try{
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            con = DriverManager.getConnection("jdbc:derby://localhost/testdb","dbuser","dbpassword");
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from OSGI_RESOURCES_TEST_RESULTS");
            boolean rowsFound = false;
            while(rs.next()){
                rowsFound = true;
                String testName = rs.getString(1);
                String testResult = rs.getString(2);
                System.out.println("testresult : " + testResult);
                if(testResult != null && !testResult.trim().isEmpty()){
                    if(testResult.trim().equalsIgnoreCase("pass")){
                        stat.addStatus("osgi-ee-resources-test : " + testName.trim(), stat.PASS);
                    }else{
                        stat.addStatus("osgi-ee-resources-test : " + testName.trim(), stat.FAIL);
                    }
                }else{
                    stat.addStatus("osgi-ee-resources-test : " + testName, "DID NOT RUN");
                }

                //System.out.println(testName + " : " + testResult);
            }
            if(rowsFound == false){
                    stat.addStatus("osgi-ee-resources-test : " ,  "DID NOT RUN");
            }
           System.out.println("rows found : " + rowsFound);
        }catch(Exception e){
            e.printStackTrace();
            stat.addStatus("osgi-ee-resources-test : ", "DID NOT RUN");
        }finally{
            try{
                if(stmt != null){
                    stmt.close();
                }
            }catch(Exception e){}
            try{
                 if(con != null){
                     con.close();
                 }
            }catch(Exception e){ e.printStackTrace();}
            try{
            stat.printSummary();
            }catch(Exception e){
               e.printStackTrace();
               }
        }
    }
}


/*

package com.sun.s1peqe.nonacc.simple.client;

import javax.naming.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.ts.tests.common.connector.whitebox.*;

public class SimpleMessageClient {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {

        stat.addDescription("This is to test simple "+
             "whitebox tx rar.");

        Context                 jndiContext = null;
             TSConnection con =  null;
        try {
            jndiContext = new InitialContext();
            TSConnectionFactory tscf =
                         //(TSConnectionFactory)jndiContext.lookup("jca/whitebox-tx-resource");
                         (TSConnectionFactory)jndiContext.lookup("__SYSTEM/resource/whitebox-tx#com.sun.ts.tests.common.connector.whitebox.TSConnectionFactory");
            System.out.println("lookup of TSCF succeeded");
            con = tscf.getConnection();

            System.out.println("connection created");
            stat.addStatus("simple whitebox tx rar", stat.PASS);
        } catch (Exception e) {
                e.printStackTrace();
        }finally{
             if(con != null){
               try{
               con.close();
               }catch(Exception e){
                   e.printStackTrace();
               }
               System.out.println("Connection closed successfully");
             }
        } // finally
    } // main
} // class


 */
