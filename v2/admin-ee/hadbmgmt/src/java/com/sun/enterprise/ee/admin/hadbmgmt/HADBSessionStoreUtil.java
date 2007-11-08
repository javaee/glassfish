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
                PROPRIETARY/CONFIDENTIAL. Use of this product is subject
                to license terms. Copyright \251 2001 Sun Microsystems, Inc.
                All rights reserved.
 
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

import java.io.*;
//jdk imports
import java.sql.*;
import java.util.*;

/*
 * Utility to manage the session store in HADB. This code was lifted mostly from Hercules
 * from class com.iplanet.ias.tools.cli.util.sessionstore.HADBSessionStoreUtil
 */

class Debug
{
    
    private Debug()
    {
    }
    
    private static final boolean _DEBUG_ENABLED = true;
    
    public static void println(String message)
    {
        if (_DEBUG_ENABLED)
        {
            LoggerHelper.fine(message);
        }
    }
    
    public static void println(Exception ex)
    {
        println(ex.toString());
    }
    
    public static void printStackTrace(Exception ex)
    {
        if (_DEBUG_ENABLED)
        {
            println(ex);
            //ex.printStackTrace();
        }
    }
}

public class HADBSessionStoreUtil
{
    // IMPORTANT; Don't bother setting a break point in this code --
    // this class is run in another process.
    
    private void initConnection() throws ClassNotFoundException
    {
        Class.forName(DRIVER);
    }
    
    /**
     *
     * @param user
     * @param password
     * @param url
     */
    public HADBSessionStoreUtil( String user, String password, String url)
    {
        storeuser = user;
        storepassword = password;
        storeurl = url;
    }
    
    /**
     *
     * @param user
     * @param password
     * @param url
     * @param systemUser
     * @param systemPassword
     */
    public HADBSessionStoreUtil( String user, String password, String url,
        String systemUser, String systemPassword)
    {
        storeuser = user;
        storepassword = password;
        storeurl = url;
        systemuser = systemUser;
        systempassword = systemPassword;
    }
    
    public HADBSessionStoreUtil( String user, String password, String url,
        String systemPassword)
    {
        storeuser = user;
        storepassword = password;
        storeurl = url;
        systemuser = SYSTEM_USER;
        systempassword = systemPassword;
    }
    
    // Just two of these operations are supported for session store.
    
    public void createSessionStore() throws HADBSetupException
    {
        try
        {
            getConnection(storeurl, systemuser, systempassword);
            String oldUser = getOldUser();
            dropTables();
            dropSchema();
            
            if (oldUser != null)
                dropUser(oldUser);
            
            setSchema("sysroot");
            
            if ( newUserExists() )
            {
                if (!(checkPermission().equals(SYSTEM_PRIVILEGE)))
                    dropUser();
            }
            
            createNewUser();
            createNewSchema();
            changeSchemaUser();
            hadbConnection.close();
            hadbConnection = null;
            getConnection();
            createModifiedSessionTables();
            createModifiedAttributeTables();
            createSingleSignOnTable();
            createStatefulSessionBeanTable();
            hadbConnection.commit();
            hadbConnection.close();
        }
        catch (Exception e)
        {
            Debug.printStackTrace(e);
            throw new HADBSetupException(e.toString(), e);
        }
    }
    
    /**
     *
     * @throws SessionStoreException
     */
    public void clearSessionStore() throws HADBSetupException
    {
        try
        {
            getConnection();
            setSchema();
            dropModifiedSessionTables();
            dropModifiedAttributeTables();
            dropSingleSignOnTable();
            dropStatefulSessionBeanTable();
            createModifiedSessionTables();
            createModifiedAttributeTables();
            createSingleSignOnTable();
            createStatefulSessionBeanTable();
            hadbConnection.commit();
            hadbConnection.close();
        }
        catch (Exception e)
        {
            Debug.printStackTrace(e);
            throw new HADBSetupException(e.toString(), e);
        }
    }
    
    /**
     * this is the same as clearSessionStore except
     * that exceptions during setSchema() are ignored
     * @throws HADBSetupException
     */
    public void runtimeClearSessionStore() throws HADBSetupException
    {
        try
        {
            getConnection();
            try
            {
                setSchema();
            }
            catch (Exception ex)
            {
                //deliberately eating this exception
                //to allow for users of default schema
            }
            dropModifiedSessionTables();
            dropModifiedAttributeTables();
            dropSingleSignOnTable();
            dropStatefulSessionBeanTable();
            createModifiedSessionTables();
            createModifiedAttributeTables();
            createSingleSignOnTable();
            createStatefulSessionBeanTable();
            hadbConnection.commit();
            hadbConnection.close();
        }
        catch (Exception e)
        {
            Debug.printStackTrace(e);
            throw new HADBSetupException(e.toString(), e);
        }
    }
    
    private void getConnection() throws SQLException, ClassNotFoundException
    {
        if ( hadbConnection == null)
        {
            initConnection();
            getConnection(storeurl, storeuser, storepassword);
        }
    }
    
    private void getConnection(String url)	throws SQLException, ClassNotFoundException
    {
        Debug.println(" Get connection....." );
        if ( hadbConnection == null)
        {
            initConnection();
            hadbConnection = DriverManager.getConnection(url);
        }
    }
    
    private void getConnection(String url, String userid, String userpassword)
    throws SQLException, ClassNotFoundException
    {
        Debug.println(" Get connection....." );
        if ( hadbConnection == null)
        {
            initConnection();
            hadbConnection = DriverManager.getConnection(url, userid, userpassword);
        }
    }
    
    private void createModifiedSessionTables() throws SQLException
    {
        Debug.println(" Creating table ....." + MODIFIED_SESSION_TABLENAME);
        String query = "CREATE TABLE "+
            MODIFIED_SESSION_TABLENAME+
            " (id varchar(100) not null,"+
            "valid char(1) not null,"+
            "maxinactive int not null,"+
            "lastaccess double integer,"+
            "appid varchar(100),"+
            "sessdata BLOB,"+
            "userName varchar(100),"+
            //"ssoid varchar(100))";
            "ssoid varchar(100)," +
            "primary key (id, appid))";
        
        Statement stmt = hadbConnection.createStatement();
        stmt.executeUpdate(query);
    }
    
    private void createModifiedSessionTablesPrevious() throws SQLException
    {
        Debug.println(" Creating table ....." + MODIFIED_SESSION_TABLENAME);
        
        String query = "CREATE TABLE "+
            MODIFIED_SESSION_TABLENAME+
            " (id varchar(100) not null primary key,"+
            "valid char(1) not null,"+
            "maxinactive int not null,"+
            "lastaccess double integer,"+
            "appid varchar(100),"+
            "sessdata BLOB,"+
            "userName varchar(100),"+
            "ssoid varchar(100))";
        
        Statement stmt = hadbConnection.createStatement();
        stmt.executeUpdate(query);
    }
    
    private void createModifiedAttributeTables() throws SQLException
    {
        createModifiedAttributesHeaderTable();
        createModifiedAttributesAttributeTable();
    }
    
    private void dropModifiedSessionTables() throws SQLException
    {
        Debug.println(" Droping table ....."+ MODIFIED_SESSION_TABLENAME);
        
        String query = "DROP TABLE "+ MODIFIED_SESSION_TABLENAME;
        
        Statement stmt = hadbConnection.createStatement();
        stmt.executeUpdate(query);
    }
    
    private void dropModifiedAttributeTables() throws SQLException
    {
        dropModifiedAttributesHeaderTable();
        dropModifiedAttributesAttributeTable();
    }
    
    private void createModifiedAttributesHeaderTable() throws SQLException
    {
        Debug.println(" Creating table ....."+ MODIFIED_ATTRIBUTES_HEADER_TABLENAME);
        
        String query = "CREATE TABLE "+
            MODIFIED_ATTRIBUTES_HEADER_TABLENAME+
            " (id varchar(100) not null,"+
            "valid char(1) not null,"+
            "maxinactive int not null,"+
            "lastaccess double integer,"+
            "appid varchar(100)," +
            "username varchar(100)," +
            "ssoid varchar(100)," +
            "primary key(id, appid))";
        
        Statement stmt = hadbConnection.createStatement();
        stmt.executeUpdate(query);
    }
    
    private void createModifiedAttributesHeaderTablePrevious() throws SQLException
    {
        Debug.println(" Creating table ....."+ MODIFIED_ATTRIBUTES_HEADER_TABLENAME);
        
        String query = "CREATE TABLE "+
            MODIFIED_ATTRIBUTES_HEADER_TABLENAME+
            " (id varchar(100) not null primary key,"+
            "valid char(1) not null,"+
            "maxinactive int not null,"+
            "lastaccess double integer,"+
            "appid varchar(100)," +
            "username varchar(100)," +
            "ssoid varchar(100))";
        
        Statement stmt = hadbConnection.createStatement();
        stmt.executeUpdate(query);
    }
    
    private void createModifiedAttributesAttributeTable() throws SQLException
    {
        Debug.println(" Creating table ....."+
            MODIFIED_ATTRIBUTES_ATTRIBUTE_TABLENAME);
        
        String query = "CREATE TABLE "+
            MODIFIED_ATTRIBUTES_ATTRIBUTE_TABLENAME+
            " (rowid varchar(200) not null,"+ // use session-id + ":" + attrname
            "sessattrdata BLOB,"  +
            "id varchar(100) not null," + // session-id
            //"attributename varchar(100), constraint pk primary key(rowid))";
            "attributename varchar(100)," +
            "appid varchar(100)," +
            "primary key(rowid, appid))";
        
        Statement stmt = hadbConnection.createStatement();
        stmt.executeUpdate(query);
    }
    
    private void createModifiedAttributesAttributeTablePrevious() throws SQLException
    {
        Debug.println(" Creating table ....."+
            MODIFIED_ATTRIBUTES_ATTRIBUTE_TABLENAME);
        
        String query = "CREATE TABLE "+
            MODIFIED_ATTRIBUTES_ATTRIBUTE_TABLENAME+
            " (rowid varchar(200) not null,"+ // use session-id + ":" + attrname
            "sessattrdata BLOB,"  +
            "id varchar(100) not null," + // session-id
            "attributename varchar(100), constraint pk primary key(rowid))";
        Statement stmt = hadbConnection.createStatement();
        stmt.executeUpdate(query);
    }
    
    private void dropModifiedAttributesHeaderTable() throws SQLException
    {
        Debug.println(" Dropping table ....."+ MODIFIED_ATTRIBUTES_HEADER_TABLENAME);
        
        String query = "DROP TABLE "+ MODIFIED_ATTRIBUTES_HEADER_TABLENAME;
        
        Statement stmt = hadbConnection.createStatement();
        stmt.executeUpdate(query);
    }
    
    private void dropModifiedAttributesAttributeTable() throws SQLException
    {
        Debug.println(" Dropping table ....."+
            MODIFIED_ATTRIBUTES_ATTRIBUTE_TABLENAME);
        
        String query = "DROP TABLE "+
            MODIFIED_ATTRIBUTES_ATTRIBUTE_TABLENAME;
        
        Statement stmt = hadbConnection.createStatement();
        stmt.executeUpdate(query);
    }
    
    private void createSingleSignOnTable() throws SQLException
    {
        Debug.println(" Creating table ....."+ SINGLE_SIGNON_TABLENAME);
        
        String query = "CREATE TABLE "+
            SINGLE_SIGNON_TABLENAME+
            " (ssoid varchar(100) not null primary key,"+
            " lastaccess double integer,"+
            " authType varchar(100)," +
            " userName varchar(100))";
        
        Statement stmt = hadbConnection.createStatement();
        stmt.executeUpdate(query);
    }
    
    private void dropSingleSignOnTable() throws SQLException
    {
        Debug.println(" Dropping table ....."+ SINGLE_SIGNON_TABLENAME);
        
        String query = "DROP TABLE "+ SINGLE_SIGNON_TABLENAME;
        
        Statement stmt = hadbConnection.createStatement();
        stmt.executeUpdate(query);
        
    }
    
    private void createStatefulSessionBeanTable() throws SQLException
    {
        Debug.println(" Creating table ....."+
            STATEFUL_SESSION_BEAN_TABLENAME);
        
        String query = "CREATE TABLE " +
            STATEFUL_SESSION_BEAN_TABLENAME +
            " (id varchar(100) not null primary key," +
            " clusterid varchar(100)," +
            " lastaccess double integer," +
            //" beandata integer ," +
            " beandata BLOB,"+
            " containerid varchar(100))";
        
        Statement stmt = hadbConnection.createStatement();
        stmt.executeUpdate(query);
    }
    
    private void dropStatefulSessionBeanTable() throws SQLException
    {
        Debug.println(" Dropping table ....."+
            STATEFUL_SESSION_BEAN_TABLENAME);
        
        String query = "DROP TABLE "+
            STATEFUL_SESSION_BEAN_TABLENAME;
        
        Statement stmt = hadbConnection.createStatement();
        stmt.executeUpdate(query);
    }
    
    private void createNewUser() throws SQLException
    {
        Debug.println(" Creating user .....");
        String query = "CREATE USER "+
            storeuser + " PASSWORD '"+
            storepassword + "'";
        
        Statement stmt = hadbConnection.createStatement();
        stmt.executeUpdate(query);

        /** NEW -- does not work.  
         * see http://archives.postgresql.org/pgsql-jdbc/2006-08/msg00101.php
         * neither of the 2 lines that follow work
        String query = "CREATE USER ? PASSWORD ?";
        String query = "CREATE USER ? PASSWORD '?'";
        PreparedStatement stmt = hadbConnection.prepareStatement(query);
        stmt.setString(1, storeuser);
        stmt.setString(2, storepassword);
        stmt.executeUpdate();
         */
    }
    
    private void createNewSchema() throws SQLException
    {
        Debug.println(" Creating schema.....");
        
        String query = "CREATE SCHEMA "+SCHEMA_NAME +
            " OWNER " + storeuser;
        
        Statement stmt = hadbConnection.createStatement();
        stmt.executeUpdate(query);
    }
    
    private void dropSchema()
    {
        Debug.println(" Droping schema.....");
        
        try
        {
            String query = "DROP SCHEMA "+SCHEMA_NAME;
            Statement stmt = hadbConnection.createStatement();
            stmt.executeUpdate(query);
        }
        catch (java.sql.SQLException sqle)
        {
            Debug.println(" Exception in dropschema "+sqle.toString());
        }
    }
    
    private void grantUsageToNewUser() throws SQLException
    {
        Debug.println(" Granting usage access to new user.....");
        
        String query = "GRANT USAGE ON SCHEMA "+
            SCHEMA_NAME +
            " TO " + storeuser;
        Statement stmt = hadbConnection.createStatement();
        stmt.executeUpdate(query);
    }
    
    private void changeSchemaUser() throws SQLException
    {
        Debug.println(" Change schema user to new user.....");
        
        String query = "ALTER USER "+ storeuser + " SCHEMA " +
            SCHEMA_NAME;
        
        Statement stmt = hadbConnection.createStatement();
        stmt.executeUpdate(query);
    }
    
    private void setSchema() throws SQLException
    {
        setSchema(SCHEMA_NAME) ;
    }
    
    private void setSchema(String schemaname) throws SQLException
    {
        Debug.println(" Changing schema of the user ....."+schemaname);
        
        String query = "SET SCHEMA "+ schemaname;
        Statement stmt = hadbConnection.createStatement();
        stmt.executeUpdate(query);
    }
    
    private void dropTables()
    {
        try
        {
            setSchema();
            dropModifiedSessionTables();
            dropModifiedAttributeTables();
            dropSingleSignOnTable();
            dropStatefulSessionBeanTable();
        }
        catch (java.sql.SQLException sqle)
        {
            Debug.println(" Exception in droptable "+sqle.toString());
        }
    }
    
    private boolean newUserExists() throws SQLException
    {
        Debug.println(" Does the user exist ? .....");
        String query = "SELECT * FROM " + ALL_USERS + " WHERE username = ?";
        PreparedStatement stmt = hadbConnection.prepareStatement(query);
        stmt.setString(1, storeuser);
        ResultSet userResult = stmt.executeQuery();

        if ( userResult.next() )
            return true;
        else
            return false;
    }
    
    private void dropUser() throws SQLException
    {
        Debug.println(" Droping User .....");
        dropUser(storeuser);
    }
    
    private void dropUser(String username) throws SQLException
    {
        Debug.println(" Droping User ....."+username);
        
        String query = "DROP USER "+username;
        Statement stmt = hadbConnection.createStatement();
        stmt.executeUpdate(query);
    }
    
    private String getOldUser() throws SQLException
    {
        Debug.println(" Getting Old User .....");
        
        String query = "SELECT ownername FROM "+
            ALL_SCHEMAS + " WHERE schemaname='" +
            SCHEMA_NAME + "'";
        Statement stmt = hadbConnection.createStatement();
        ResultSet userResult = stmt.executeQuery(query);
        
        if (userResult.next() )
        {
            return userResult.getString("ownername");
        }
        
        return null;
    }
    
    private String checkPermission() throws SQLException
    {
        Debug.println(" Checking permission of existing store user .....");
        String query = "SELECT privilege FROM " + ALL_USERS + 
                " WHERE username = ?";
        PreparedStatement stmt = hadbConnection.prepareStatement(query);
        stmt.setString(1, storeuser);
        ResultSet userResult = stmt.executeQuery();

        if (userResult.next() )
        {
            return userResult.getString("privilege");
        }
        
        return null;
    }
    
    private static void usage()
    {
        System.err.println(
            "usage: java HADBSessionStoreUtil [create|clear] url user password systemuser systempassword");
        System.exit(1);
    }
    
    private void setPasswords() throws HADBSetupException
    {
        storepassword = getProp(storepassword);
        systempassword = getProp(systempassword);
    }
    
    private String getProp(String fname) throws HADBSetupException
    {
        try
        {
            Properties props = new Properties();
            InputStream in = new FileInputStream(new File(fname));
            props.load(in);
            in.close();
            
            // there should be one and only one property in here.
            
            Enumeration e = props.elements();
            
            if(!e.hasMoreElements())
            {
                throw new HADBSetupException("No password in file: " + fname);
            }
            
            String val = (String)e.nextElement();
            
            if(e.hasMoreElements())
            {
                throw new HADBSetupException("More than one password in file: " + fname);
            }
            
            return val;
        }
        catch(IOException e)
        {
            System.out.println("IOException trying to read password from " + fname + " : " + e);
            return "";
        }
    }
    
    public static void main(String[] args)
    {
        // KLUDGEfest until we get rid of calling this in a seperate VM...
        if (args.length != 6)
        {
            usage();
        }
        else if (!args[0].equalsIgnoreCase("create") && !args[0].equalsIgnoreCase("clear"))
        {
            usage();
        }
        
        
        try
        {
            HADBSessionStoreUtil u = new HADBSessionStoreUtil(args[2], args[3], args[1],
                args[4], args[5]);
            
            // bnevins -- args[3], args[5] are now filenames that contain
            // storepassword, systempassword respectively.
            // the ctor only sets them, so now we figure out the passwords inside
            // and reset the class variables.  Mega-Kludge!
            
            u.setPasswords();
            
            if (args[0].equalsIgnoreCase("clear"))
            {
                u.clearSessionStore();
            }
            else
            {
                u.createSessionStore();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private String				storeuser = null;
    private String				storepassword = null;
    private String				storeurl = null;
    private String				systemuser = null;
    private String				systempassword = null;
    private Connection			hadbConnection = null;
    
    private static final String DRIVER = "com.sun.hadb.jdbc.Driver";
    private static final String SCHEMA_NAME = "haschema";
    private static final String ALL_SCHEMAS = "allschemas";
    private static final String ALL_USERS = "allusers";
    private static final String MODIFIED_SESSION_TABLENAME = "blobsessions";
    private static final String MODIFIED_ATTRIBUTES_HEADER_TABLENAME = "sessionheader";
    private static final String MODIFIED_ATTRIBUTES_ATTRIBUTE_TABLENAME = "sessionattribute";
    private static final String SINGLE_SIGNON_TABLENAME = "singlesignon";
    private static final String STATEFUL_SESSION_BEAN_TABLENAME = "statefulsessionbean";
    private static final String SYSTEM_USER = "system";
    private static final String SYSTEM_PRIVILEGE = "128";
}


