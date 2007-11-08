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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

//
// Simple app to create modified attribute blob tables
//
package com.sun.enterprise.ee.web.sessmgmt;

import java.sql.*;
import javax.sql.*;
//import com.sun.hadb.jdbc.*;
import java.io.*;
import org.apache.catalina.*;
import org.apache.catalina.session.*;


public class BlobAttributeSession
{
    public static final String driver = "com.sun.hadb.jdbc.Driver";
    //To send output to stdout
    private static boolean _verbose = true;
    
    private static Connection con;
    private static String sessionHeaderTableName = "sessionheader";
    private static String sessionAttributeTableName = "sessionattribute";
    private static PreparedStatement preparedInsertSesHdrSql = null;	
    private static PreparedStatement preparedInsertSesAttrSql = null;	
    //private static LOBDescr lob = new LOBDescr();
  
    /** Displays a message to stdout if _verbose is true. This allows
     * messages to be displayed when invoked from the command line.
     */
    private static void message(String message) {
        if (_verbose) {
            System.out.println(message);
        }
    }
  
    public BlobAttributeSession(String url) throws SQLException
    {
        con = DriverManager.getConnection (url);
    }

        //Setting public modifier for using in UnitTests
    public void createHeaderTable () throws SQLException
    { 
        message("createHeaderTable");
        String query = "CREATE TABLE "+ sessionHeaderTableName+
            " (id varchar(100) not null,"+
            "valid char(1) not null,"+
            "maxinactive int not null,"+
            "lastaccess double integer,"+
            "appid varchar(100)," +
            "username varchar(100)," +
            "ssoid varchar(100)," +
            "primary key(id, appid))";

        Statement stmt = con.createStatement();
        stmt.executeUpdate (query);
    }
    
        //Setting public modifier for using in UnitTests
    public void createAttributeTable () throws SQLException
    { 
        /* This is how it should be. But Clustra does not support foreign key 
            constraint. Oracle supports. 
        String query = "CREATE TABLE "+ sessionAttributeTableName+
        " (id varchar(100) not null,"+
        "attributename varchar(100),"+
        "sessattrdata BLOB,"+
        "primary key (id, attributename),"
        "constraint foreign key id references " + sessionHeaderTableName +
        "(id))"; */    
        message("createAttributeTable");

        String query = "CREATE TABLE "+ sessionAttributeTableName+
            " (rowid varchar(200) not null,"+ // use session-id + ":" + attrname
            "sessattrdata BLOB,"  +   
            "id varchar(100) not null," + // session-id
            //"attributename varchar(100), constraint pk primary key(rowid))";
            "attributename varchar(100)," +
            "appid varchar(100)," + 
            "primary key(rowid, appid))";

        Statement stmt = con.createStatement();
        stmt.executeUpdate (query);
        //((LobConnection)con).createLobTable (null,sessionAttributeTableName);
        con.commit();
        message("Table " + sessionAttributeTableName + " created");    
    }    
	 
    //Setting public modifier for using in UnitTests
    public void dropHeaderTable () {
        try 
        {
            message("dropTable");
            String query = "DROP TABLE "+ sessionHeaderTableName;
            Statement stmt = con.createStatement();
            stmt.executeUpdate (query);
            //no lob for header table
            //((CluConnection)con).dropLobTable (null, sessionHeaderTableName);
        } 
        catch (SQLException e) 
        {
            message(e.toString());
        }
    }
  
    //Setting public modifier for using in UnitTests
    public void dropAttributeTable () {
        try 
        {
            message("dropTable");
            String query = "DROP TABLE "+ sessionAttributeTableName;
            Statement stmt = con.createStatement();
            stmt.executeUpdate (query);
            //((LobConnection)con).dropLobTable (null, sessionAttributeTableName);
        } 
        catch (SQLException e) 
        {
        message(e.toString());
        }
    } 
    
    // Changing method signature -- take string sessionId instead of session 
    // object for using in UnitTests
    public void insertSessionHeader(String sessionId, Connection connection) throws IOException {

        String insertHdrSql = "INSERT into " + sessionHeaderTableName + "(" + 
            "id, valid, maxinactive, lastaccess, appid, username ) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            if (preparedInsertSesHdrSql == null)
                preparedInsertSesHdrSql = connection.prepareStatement(insertHdrSql);

            preparedInsertSesHdrSql.setString(1, sessionId);
            preparedInsertSesHdrSql.setString(2, "1");
            preparedInsertSesHdrSql.setInt(3, 60);
            preparedInsertSesHdrSql.setLong(4, 1000);
            preparedInsertSesHdrSql.setString(5, "cluster1:webapps");
            preparedInsertSesHdrSql.setString(6, "user1");
            //executeStatement(preparedInsertSesHdrSql, false);
            preparedInsertSesHdrSql.executeUpdate();

            ((Connection)con).commit();
        }
        catch(SQLException e) {
            try{((Connection)con).rollback();}catch(SQLException ee){}
            e.printStackTrace();
            throw new IOException("Error from HA Store: " + e.getMessage());
        }
        /*finally {
            if (preparedInsertSesHdrSql != null) {
            try {
                            preparedInsertSesHdrSql.close();
                                    } catch (Exception ex) {}
            }
        }*/

    }
  
  // Changing method signature -- take string sessionId  and string attrName 
  // instead of session object for using in UnitTests
    /*
    public void insertAttribute(String sessionId, String attrName, String attrVal, Connection con) throws IOException {
        LobConnection lobConn = (LobConnection) con;

        //initialize the lob descriptors that we use
         lob.setTableName(sessionAttributeTableName);
         lob.addKey("rowid", 1);
         lob.setLOBColumn("sessattrdata", 2);


        String insertAttrSql = "INSERT INTO " + sessionAttributeTableName + " ("+
            "rowid, sessattrdata, id, attributename) " +
            "VALUES (?, ?, ?, ?)";

            try {
            if (preparedInsertSesAttrSql == null) {
                preparedInsertSesAttrSql = lobConn.prepareLobStatement(insertAttrSql, lob);
            }

                Object attrData = attrVal;
                BufferedInputStream in = null;
                int buflength = 0;
                        in = getInputStream(attrData, buflength);
                preparedInsertSesAttrSql.setString(1, sessionId + ":" + attrName);
                preparedInsertSesAttrSql.setBinaryStream(2, in, buflength);
                preparedInsertSesAttrSql.setString(3, sessionId );
                preparedInsertSesAttrSql.setString(4, attrName);
                        //executeStatement(preparedInsertSesAttrSql, false);
                preparedInsertSesAttrSql.executeUpdate();
                ((Connection)lobConn).commit();
            }
            catch ( SQLException e ) {
                try{((Connection)lobConn).rollback();}catch(SQLException ee){}
                throw new IOException("Error from HA Store: " + e.getMessage());
            }
            //finally {
            //    try {
            //        ((Connection) lobConn).close();
            //    }
            //    catch (Exception e){
            //        e.printStackTrace();
            //    }
            //}
    }*/

    public BufferedInputStream getInputStream(Object attribute, int length) throws IOException {
        ByteArrayOutputStream bos = null;
        ByteArrayInputStream bis = null;
        ObjectOutputStream oos = null;
        BufferedInputStream in = null;

        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(new BufferedOutputStream(bos));

            oos.writeObject(attribute);
            oos.close();
            oos = null;

            byte[] obs = bos.toByteArray();
            bis = new ByteArrayInputStream(obs, 0, obs.length);
            in = new BufferedInputStream(bis, obs.length);
            length = obs.length;
        }
        finally {
            if ( oos != null )  {
                oos.close();
            }

            if (bis != null) {
                bis.close();
            }
        }
        return in;
    }

    public static void recreateTable(String url, String user, String password) 
        throws SQLException, ClassNotFoundException 
    {        
        Class.forName (driver);          
        String driverName = "jdbc:sun:hadb:" + user + "+" + password + "@" + url;
        message("driverName = " + driverName);
        BlobAttributeSession bs = new BlobAttributeSession(driverName);      
        bs.dropAttributeTable(); 
        bs.dropHeaderTable();     
        bs.createHeaderTable();
        bs.createAttributeTable();
    }
    
    public static void main (String[] args) {
        if(args.length != 1 && args.length != 3)
        {
            System.out.println("usage: java BlobSession url [user] [password]");
            System.exit(1);
        }
        try {            
            if (args.length == 3) {
                recreateTable(args[0], args[1], args[2]);
            } else {
                recreateTable(args[0], "system", "super");
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
