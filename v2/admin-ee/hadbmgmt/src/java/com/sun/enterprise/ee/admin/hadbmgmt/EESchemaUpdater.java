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

package com.sun.enterprise.ee.admin.hadbmgmt;

import java.sql.*;
import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
//import com.sun.hadb.jdbc.*;
import com.sun.enterprise.web.SchemaUpdater;
import com.sun.enterprise.web.ServerConfigLookup;

/**
 *
 * @author  lwhite
 */
public class EESchemaUpdater implements SchemaUpdater {
    
    /** Creates a new instance of EESchemaUpdater */
    public EESchemaUpdater() {
    }    
    
    /** Creates a new instance of EESchemaUpdater */
    public EESchemaUpdater(String url, String user, String password) throws SQLException, ClassNotFoundException {
        this.url = url;
        this.user = user;
        this.password = password;
        
        Class.forName (driver);
        String connUrl = HADB_URL_PREFIX + user + "+" + password + "@" + url;
        message("EESchemaUpdater:connUrl = " + connUrl);
        
        con = DriverManager.getConnection(connUrl);
    }
    
    public void init() throws SQLException, ClassNotFoundException { 

        ServerConfigLookup lookup = new ServerConfigLookup();
        String connUrl = lookup.getConnectionURLFromConfig();
        String password = lookup.getConnectionPasswordFromConfig();
        String user = lookup.getConnectionUserFromConfig();
        //message("connUrl:" + connUrl + " password:" + password + " user:" + user);         
        this.init(connUrl, user, password);

    }     

    /**
     * initialize this instance with (connection) url, user and password
     * and initialize an HADB connection
     * @param url
     * @param user
     * @param password
     */    
    public void init(String url, String user, String password) throws SQLException, ClassNotFoundException {
        this.url = url;
        this.user = user;
        this.password = password;
        if(!checkInputParameters()) {
            return;
        }
        String strippedURL = stripURL(url);
        
        Class.forName (driver);
        //String connUrl = HADB_URL_PREFIX + user + "+" + password + "@" + url;
        String connUrl = HADB_URL_PREFIX + user + "+" + password + "@" + strippedURL;
        //message("EESchemaUpdater:connUrl = " + connUrl);
        
        con = DriverManager.getConnection(connUrl);
    }
    
    private void closeConnection() {
        try {
            con.close();
        } catch (Exception ex) {
            //deliberate no-op
            assert true;
        }
    }
    
    /**
     * return false if any of the user, password or connection url 
     * are missing
     */    
    private boolean checkInputParameters() {
        
        String strippedURL = stripURL(url);
        if( (user == null || user.equals("")) 
            || (password == null || password.equals(""))
            || (strippedURL.equals("")) ) {
                return false;
        } else {
            return true;
        }
    }    

   /**
   * take in a serverList string like "foo:15005, foo:15125"
   * and return as "foo:15005"
   * @param inputUrl 
   */    
    private String stripURL(String inputUrl) {
        //this should not happen
        if(inputUrl == null || inputUrl.equals("")) {
            return "";
        }        
        StringTokenizer st = new StringTokenizer(inputUrl, ",");
        ArrayList list = new ArrayList();
        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        String token = (String)list.get(0);
        String result = token;
        if(token.startsWith(HADB_URL_PREFIX)) {
            result = token.substring(
                        HADB_URL_PREFIX.length());            
        }
        return result;
    }
    
    private void describeTable () throws SQLException {
        message("describeTable");

        DatabaseMetaData dbmd = con.getMetaData();
        ResultSet rs = dbmd.getPrimaryKeys(null, null, BLOBSESSIONS);
        while (rs.next()) {
            String tableName = rs.getString("TABLE_NAME");
            String columnName = rs.getString("COLUMN_NAME");
            String keySeq = rs.getString("KEY_SEQ");
            String pkName = rs.getString("PK_NAME");
            message("table name: " + tableName);
            message("column name: " + columnName);
            message("sequence in key: " + keySeq);
            message("primary key name: " + pkName);        
        }
        con.commit();
        message("Table " + BLOBSESSIONS + " described");
    }
    
   /**
   * check if all the necessary HADB tables exist
   * if any are missing return false - otherwise true
   *
   * @throws SQLException 
   */     
    public boolean doTablesExist() throws SQLException, ClassNotFoundException {
        this.init();
        boolean result = true;
        result = doesTableExist(BLOBSESSIONS);
        if(!result) {
            return result;
        }
        result = doesTableExist(SESSIONHEADER);
        if(!result) {
            return result;
        }
        result = doesTableExist(SESSIONATTRIBUTE);
        if(!result) {
            return result;
        }        
        result = doesTableExist(SINGLESIGNON);
        if(!result) {
            return result;
        }
        result = doesTableExist(STATEFULSESSIONBEAN);
        this.closeConnection();
        
        return result;
    } 
    
   /**
   * check if the table <tableName> exists
   * if it exists return true else false
   * @param tableName - name of the table to check
   * @throws SQLException 
   */ 
    public boolean doesTableExist(String tableName) throws SQLException {
        message("checking if " + tableName + " table exists");
        boolean tableExists = true;
        
        try {
            DatabaseMetaData dbmd = con.getMetaData();
            ResultSet rs = dbmd.getPrimaryKeys(null, null, tableName);
            //DatabaseMetaData does not throw an exception when the table
            //does not exist - rather it return an empty ResultSet so we
            //check for that
            if(!rs.next()) {
                tableExists = false;
            }
        } catch (SQLException ex) {
            tableExists = false;
            try {
                con.rollback();
            } catch (Exception ex1) {}
        } finally {
            try {
                con.commit();
            } catch (Exception ex2) {}
        }
        if(tableExists) {
            message("Table " + tableName + " exists");
        } else {
            message("Table " + tableName + " does not exist");
        }
        return tableExists;
    }        

   /**
   * check if the blobsession table is
   * defined in an earlier form (version 1)
   * with only id as the primary key or
   * the new form (version 2) with a compound
   * primary key containing 2 columns
   * return true if version 2 otherwise return false
   * 
   * @throws SQLException 
   */    
    private boolean isSchemaVersion2() throws SQLException {
        int ct = 0;     //ct of number of columns in pk
        DatabaseMetaData dbmd = con.getMetaData();
        ResultSet rs = dbmd.getPrimaryKeys(null, null, BLOBSESSIONS);

        while (rs.next()) {
            ct++;
        }
        //if blobsessions table has 2-column compound key return true
        //message("EESchemaUpdater:isSchemaVersion2: " + (ct == 2));
        return (ct == 2);
    }
    
    public void doSchemaCheck() throws IOException {
        boolean ableToProceed = true;
        try {
            init();
        } catch (Exception ex) {
            ableToProceed = false;
            System.out.println("EESchemaUpdater: - skipping schema update");
            /*
            IOException ioex = 
                (IOException)new IOException("EESchemaUpdater init error:").initCause(ex);
            throw ioex;
             */
        }
        if(ableToProceed) {
            checkAndUpdateHADBSchema();
        }
    }
    
    public void checkAndUpdateHADBSchema() throws IOException {
        if(!checkInputParameters()) {
            return;
        }        
        boolean isVersion2 = true;
        boolean proceed = false;
        try {
            isVersion2 = this.isSchemaVersion2();
            proceed = true;
        } catch (Exception ex) {
            //this could happen if a table was dropped, etc.
            proceed = false;
        }
        //message("proceed with check & update:" + proceed);
        if(!proceed) {
            return;
        }
        if(!isVersion2) {
            message("recreating HADB tables");
            String connUrl = url;
            if(!url.startsWith(HADB_URL_PREFIX)) {
                connUrl = HADB_URL_PREFIX + url;
            }
            HADBSessionStoreUtil storeUtil = null;
            try {
                storeUtil = 
                    new HADBSessionStoreUtil(user, password, connUrl);
                //storeUtil.clearSessionStore();
                storeUtil.runtimeClearSessionStore();
            } catch (HADBSetupException ex) {
                throw new IOException(ex.getMessage());
            }
        } else {
            message("not recreating HADB tables");
        }
    }
    
    /** Displays a message to stdout if _verbose is true. This allows
    * messages to be displayed when invoked from the command line.
    */
    private static void message(String message) {
        if (_verbose) {
            System.out.println(message);
        }
    }    
    
    public static final String driver = "com.sun.hadb.jdbc.Driver";
    private static final String BLOBSESSIONS = "blobsessions";
    private static final String SESSIONHEADER = "sessionheader";
    private static final String SESSIONATTRIBUTE = "sessionattribute";
    private static final String SINGLESIGNON = "singlesignon";
    private static final String STATEFULSESSIONBEAN = "statefulsessionbean";    
    private Connection con;
    //To send output to stdout
    private static boolean _verbose = true;
    private static String HADB_URL_PREFIX = "jdbc:sun:hadb:";
    
    private String user = null;
    private String password = null;
    private String url = null;
    
    public static void main (String[] args) {
        if(args.length != 1 && args.length != 3) {
            System.out.println("EESchemaUpdater usage: java EESchemaUpdater url [user] [password]");
            System.exit(1);
        }
        EESchemaUpdater upd = null;
        try {          
            if (args.length == 3) {
                upd = new EESchemaUpdater(args[0], args[1], args[2]);
            } else {
                upd = new EESchemaUpdater(args[0], "system", "super123");
            }
            message("NEW SCHEMA: " + upd.isSchemaVersion2());
            upd.describeTable();
            upd.checkAndUpdateHADBSchema();            
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
}
