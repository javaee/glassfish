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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.admin.verifier.tests;

/*  Test Case to check the validity of JdbcConnection Pool fields
 *  Author : srini@sun.com
 **/

// 8.0 XML Verifier
//1import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigException;
import  com.sun.enterprise.config.serverbeans.JdbcResource;
import  com.sun.enterprise.config.serverbeans.JdbcConnectionPool;

import com.sun.enterprise.admin.verifier.*;

// Logging
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;


public class JdbcConnectionPoolTest extends ServerXmlTest implements ServerCheck {
    
     // Logging
    static Logger _logger = LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);

    static int minPool=0;
    static String connValReqd=null;
    static String connValMethod=null;
    
    public JdbcConnectionPoolTest() {
    }
 
    // check method called by command line verifier
    public Result check(ConfigContext context) {
        Result result;
        result = super.getInitializedResult();
        // 8.0 XML Verifier
        /*try {
            Server server = (Server)context.getRootConfigBean();
            Resources resource = server.getResources();
            JdbcConnectionPool[] pool = resource.getJdbcConnectionPool();
            for(int i=0;i<pool.length;i++){
                int minPool=0, maxPool=0;
                try {
                    minPool = Integer.parseInt(pool[i].getSteadyPoolSize());
                    maxPool = Integer.parseInt(pool[i].getMaxPoolSize());
                } catch(NumberFormatException e) {
                    result.failed("Bad Number");
                }
                if(maxPool < minPool) 
                    result.failed("Steady Pool size should be less than or equal to Maximum Pool size");
                if(pool[i].isIsConnectionValidationRequired() && pool[i].getConnectionValidationMethod().equals("table")) {
                        if(pool[i].getValidationTableName() == null || pool[i].getValidationTableName().equals(""))
                            result.failed("Required Table Name if Connection validation method is Table for " + pool[i].getName() );
                        else
                            result.passed("Validation Table Name for " + pool[i].getName() );
                }
                else
                        result.passed("Validation Passed for Connection Pool" + pool[i].getName());
            }
        }
        catch(Exception ex) {
            // Logging
            _logger.log(Level.FINE, "serverxmlverifier.exception", ex);
            result.failed("Exception : " + ex.getMessage());
        }*/
        return result;
    }
    
     //check method called by iasadmin and adminGUI
     public Result check(ConfigContextEvent ccce) {
        Result result = new Result();
        result.passed("Passed **");
        ConfigContext context = ccce.getConfigContext();
        Object value = ccce.getObject();
        String choice = ccce.getChoice();
        
        String beanName = ccce.getBeanName();
        if(beanName!=null) {
               String name = ccce.getName();
               return testSave(name,(String)value);
        }
        
        JdbcConnectionPool pool = (JdbcConnectionPool)value;
        String poolName = pool.getName();
        
        // check if connction pool name is valid object name Bug : 4698687 : start
        if(StaticTest.checkObjectName(poolName, result)) 
            result.passed("Valid Object Name");
        else {
            result.failed("Connection Pool Name Invalid ");
            return result;
        }
        // End Bug : 4698687
        
        String datasourceClassname = pool.getDatasourceClassname();
        
        // to be uncommented once warning is implemented
        /*if(isInValidDataSource(result, datasourceClassname))
            return result;*/
        
        if (choice != null && choice.equals("DELETE")){
            try{
                    // 8.0 XML Verifier
                    //Server server = (Server)context.getRootConfigBean();
                    //Resources resource = server.getResources();
                    Domain domain = (Domain)context.getRootConfigBean();
                    Resources resource = domain.getResources();
                    JdbcResource[] jdbcResource = resource.getJdbcResource();
                    if(jdbcResource.length == 0)
                            result.passed("Connect Pool not used by data source");
                    for(int i=0;i<jdbcResource.length;i++){
                        if(jdbcResource[i].getPoolName().equals(poolName)) {
                                result.failed("Connection Pool Used by Existing data source, cannot delete pool");
                                break;
                        }
                        else 
                               result.passed("Connect Pool not used by data source");
                    }
            }
            catch(Exception e) {
            }
        }
        else {
                 if(pool.isIsConnectionValidationRequired() && pool.getConnectionValidationMethod().equals("table")) {
                        if(pool.getValidationTableName() == null || pool.getValidationTableName().equals(""))
                               result.failed("Required Table Name if Connection validation method is Table");
                        else
                               result.passed("Validation Table Name");
                }
                else
                     result.passed("****** Passed Validation Table Name");
        }
        return result;
     }
     
     public Result testSave(String name, String value) {
           Result result = new Result();
           result.passed("Passed **");
           if(name.equals(ServerTags.STEADY_POOL_SIZE)) {
               try {
                    JdbcConnectionPoolTest.minPool = Integer.parseInt(value);
                    result.passed("Passed ");
               }catch(NumberFormatException e) {
                   result.failed("Bad Number : Steady pool size");
               }
           }
           if(name.equals(ServerTags.MAX_POOL_SIZE)){
               try {
                    int maxPool = Integer.parseInt(value);
                    if(maxPool < JdbcConnectionPoolTest.minPool) 
                        result.failed("Steady Pool size must be less than or equal to Maximum Pool size");
                    else 
                        result.passed("Passed ");
               }catch(NumberFormatException e) {
                   result.failed("Bad Number : Max pool size");
               }
           }
           if(name.equals(ServerTags.IS_CONNECTION_VALIDATION_REQUIRED)){
                JdbcConnectionPoolTest.connValReqd = value;
           }
           if(name.equals(ServerTags.CONNECTION_VALIDATION_METHOD)){
                JdbcConnectionPoolTest.connValMethod = value;
           }
           if(name.equals(ServerTags.VALIDATION_TABLE_NAME)){
               if(connValReqd.equals("true")) {
                   if(connValMethod.equals("table")) {
                       if(value == null || value.equals(""))
                            result.failed("Required table name");
                       else
                           result.passed("Passed ***");
                   }
                   else
                       result.passed("Passed ***");
               }
               else
                   result.passed("Passed ***");
           }// to be uncommented once warning is implemented
           /*if(name.equals(ServerTags.DATASOURCE_CLASSNAME)) {
                if(isInValidDataSource(result, value)) {
                    return result;
                }
           }*/
            // Check for transaction-isolation-level
           if (name.equals(ServerTags.TRANSACTION_ISOLATION_LEVEL)) {
                if (value != null) {
                    String isolation = (String) value;
                
                     if (isolation.equals(""))
                        result.failed("Transaction Isolation Level not specified");
                     else if ((isolation.equals("read-uncommitted")) ||
                        (isolation.equals("read-committed")) ||
                        (isolation.equals("repeatable-read")) ||
                        (isolation.equals("serializable")))
                         result.passed("Valid Transaction Isolation Level");
                     else result.failed("Invalid Transaction Isolation Level: " +  isolation);
                }
           }
           // Bug 4675624
           if(name.equals(ServerTags.IDLE_TIMEOUT_IN_SECONDS)) {
               if(value != null) {
                   try {
                       Integer.parseInt(value);
                       result.passed("Passed ***");
                   } catch(NumberFormatException e) {
                       result.failed("Idle Timeout : invalid number");
                   }
               }
           }
           return result;
     }
     
     public boolean isInValidDataSource(Result result, String datasourceClassname)  {
         boolean failed = true;
         try {
            Class c1 = Class.forName(datasourceClassname);
            Object obj = c1.newInstance();
            if (obj instanceof javax.sql.DataSource) {
                result.passed("Valid Data Source");
                failed = false;   
            }
            else if(obj instanceof javax.sql.XADataSource) {
                result.passed("Valid XA DataSource");
                failed = false;
            }
            else 
                result.failed("Invalid  Data Source Class not implementing, javax.sql.DataSource or javax.sql.XADataSource");
         }catch(Exception e) {
	     // Logging
	     _logger.log(Level.FINE, "serverxmlverifier.error_instantiation", e.getMessage());
             result.failed("Invalid DataSource class");
         }
         return failed;
     }
}
