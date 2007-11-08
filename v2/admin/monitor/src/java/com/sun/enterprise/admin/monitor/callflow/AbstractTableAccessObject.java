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
 * AbstractTableAccessObject.java
 *
 * Created on July 13, 2005, 6:19 PM
 */

package com.sun.enterprise.admin.monitor.callflow;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.admin.common.constant.AdminConstants;
/**
 *
 * @author Harpreet Singh
 */
public abstract class AbstractTableAccessObject implements TableAccessObject{
    
    private static final Logger logger =
 		Logger.getLogger(AdminConstants.kLoggerName); 
    private static final String SYSTEM_PROPERTY =
            "com.sun.enterprise.callflow.trace";
    
    public boolean traceOn =
            System.getProperty(SYSTEM_PROPERTY, "false").equals("true");
    /*
     * SQL 99 error code for a table that already exists
     */
    private static final String TABLE_EXISTS_SQL_ERROR_CODE = "X0Y32";
    
    /*
     * An equivalent XOPEN SQL State for table already exists should be present
     * but could not be found.
     */
    
    Connection con = null;
    /* Holds the table name specific to this server instance. 
     * For e.g. REQUEST_START_TBL on a server instance with name "foo" will be
     * REQUEST_START_TBL__FOO. 
     */
    String tableName = null;
    private static final String DEFAULT_SERVER_NAME = "server";
    private long totalEntriesProcessed = 0;
    
    private String name = "AbstractTableAccessObject";
    private static final String INVALID_TABLE_NAME_CHARACTER_DASH = "-";
    private static final String INVALID_TABLE_NAME_CHARACTER_DOT = ".";
    
    private static final String INVALID_TABLE_NAME_REPLACEMENT_STRING ="___";
    private static final String OVERRIDE_DEFAULT_REPLACEMENT_STRING = 
            "com.sun.enterprise.callflow.replacementstring";
    abstract public boolean createTable(Connection connection);
    abstract public boolean dropTable(Connection connection);
   
   boolean createStatmentAndExecuteUpdate(String oldsql, 
           String tableNameWithoutServerInstance){
       
        String sql = updateSqlWithTableName (oldsql, tableNameWithoutServerInstance);
        boolean result = false;
        Statement stmt = null;
        try{
            if (con != null){
                stmt = con.createStatement();
                stmt.executeUpdate(sql);
                result = true;
            }
        } catch (java.sql.SQLException se) {
            // log it
            logger.log(Level.WARNING, "Error accessing CallFlow tables!", se);
            result = false;
        } finally {
            if(stmt != null){
                try{
                    stmt.close();
                }catch(java.sql.SQLException s){
                    // log it
                }
            }
            stmt = null;
        }
        return result;
   } 
  
   /**
    * This method is used to create a database table. If the table already 
    * exists, it logs a message and returns successfully.
    * As there is no mechanism to actually test if the database exists, it creates
    * the table and if there is an exception, it assumes it is due to table 
    * being present.
    */
   boolean createTable(String oldsql, 
           String tableNameWithoutServerInstance){
       
        String sql = updateSqlWithTableName (oldsql, tableNameWithoutServerInstance);
        boolean result = false;
        Statement stmt = null;
        try{
            if (con != null){
                stmt = con.createStatement();
                stmt.executeUpdate(sql);
                result = true;
            }
        } catch (java.sql.SQLException se) {
            // log it
            if (se.getSQLState().equalsIgnoreCase (TABLE_EXISTS_SQL_ERROR_CODE)){
                logger.log (Level.FINE, "callflow.table_already_exists_error", 
                        tableNameWithoutServerInstance);               
            } else {
                logger.log(Level.WARNING, "callflow.table_creation_error", 
                        tableNameWithoutServerInstance); 
                logger.log(Level.WARNING, "callflow.table_creation_error", se);
            }
            result = true;
        } finally {
            if(stmt != null){
                try{
                    stmt.close();
                }catch(java.sql.SQLException s){
                    // log it
                }
            }
            stmt = null;
        }
        return result;
   } 
   public String getServerInstanceName () {
       // get the server name from config
       String server = DEFAULT_SERVER_NAME;
       ServerContext sc = ApplicationServer.getServerContext();
        if (sc != null) {
           server = removeInvalidCharactersFromTableName(sc.getInstanceName());
        } 
        return "__" + server;
   } 
 
   public String removeInvalidCharactersFromTableName (String instanceName){
       
       String overrideString = null; 
       overrideString = System.getProperty(OVERRIDE_DEFAULT_REPLACEMENT_STRING);
       if (overrideString == null){
        overrideString = this.INVALID_TABLE_NAME_REPLACEMENT_STRING;
       }
       String tmp = instanceName.replace(INVALID_TABLE_NAME_CHARACTER_DOT, overrideString);
       String modifiedInstanceName = tmp.replace (INVALID_TABLE_NAME_CHARACTER_DASH,
               overrideString);
       return modifiedInstanceName;
   }
   /**
    * Adds the server instance name to the table names in the SQL statements 
    * for create/delete and insert. All creates and deletes need to call them 
    * before they submit the query to be executed
    * @param String complete sql that has the table name without the server instance
    * name
    * @param String Name of the table, this table name will be appended by 
    * a "__" and server name
    */
   String updateSqlWithTableName (String oldsql, String table) {
       String newsql = new String(oldsql);
       newsql = newsql.replaceAll(table, tableName);
       
        return newsql;       
   }
 
   public boolean delete(PreparedStatement pstmt, String[] requestId) {
        if (pstmt == null)
            return false;
        
        boolean result = false;
        try{
            for (int i = 0 ; i<requestId.length; i++) {
                pstmt.setString(1, requestId[i]);
                pstmt.addBatch();
            }
            int[] updated = pstmt.executeBatch();
            result =  (updated.length == requestId.length)? true : false;
            if (result == false){
                logger.log (Level.WARNING, "callflow.error_delete_row");                
            }
        }  catch(BatchUpdateException bue) {
            // log it
            logger.log (Level.WARNING, "callflow.error_delete_row");
            logger.log(Level.FINE, "Error data into CallFlow tables", bue);
            result = false;
        }catch (SQLException se) {
            // log it
            logger.log (Level.WARNING, "callflow.error_delete_row");
            logger.log(Level.FINE, "Error inserting data into CallFlow tables", se);
            result = false;
        }
        return result;        
        
    }      

    public boolean isTraceOn() {
        return traceOn;
    }

    public long getTotalEntriesProcessed() {
        return totalEntriesProcessed;
    }

    public void addTotalEntriesProcessed(int totalEntriesProcessed) {
        this.totalEntriesProcessed += totalEntriesProcessed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
