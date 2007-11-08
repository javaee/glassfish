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


package com.sun.ejb.ee.sfsb.store;

import java.sql.*;

import com.sun.hadb.jdbc.*;
import java.io.*;

/** Simple app to create blob table for tomcat demo
 * @author Sridhar Satuloori <Sridhar.Satuloori@Sun.Com>
 */
public class SFSBTable {
    /** HADB JDBC driver class name
     */    
    public static final String driver = "com.sun.hadb.jdbc.Driver";  
    //To send output to stdout
    private static boolean _verbose = true;
    
    /** Connection to connect to HADB
     */    
    private Connection con;
    
    /** Stateful Session Bean table name for storing sessions
     * for failover and (potentially) passivation
     */    
    private String tableName = "statefulsessionbean";

    /** creates the connection with url
     * @param url hadb jdbc url should be of form jdbc:sun:hadb:system+super@node1:port1,node2:port2
     * @throws SQLException
     */    
    public SFSBTable(String url) throws SQLException {
        con = DriverManager.getConnection(url);
    }

    /** Displays a message to stdout if _verbose is true. This allows
     * messages to be displayed when invoked from the command line.
     */
    private static void message(String message) {
        if (_verbose) {
            System.out.println(message);
        }
    }
    
    /** Creates a "statefulsession bean table i the database
     * @throws SQLException
     */    
    private void createTable() throws SQLException {
        message("createTable");
        String query = "CREATE TABLE " + tableName +
                       " (id varchar(100) not null primary key," +
                       " clusterid varchar(100)," +
                       " lastaccess double integer," +
                       //" beandata integer ," +
                       " beandata BLOB,"+
                       " containerid varchar(100))";
        message("\n" + query + "\n");

        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);
        //( (LobConnection) con).createLobTable(null, tableName);
        //((CluConnection)con).createLobTable (null,tableName);
    }
    
    /** Creates a "statefulsession bean table i the database
     * @throws SQLException
     */    
    private void createTablePrevious() throws SQLException {
        message("createTable");
        String query = "CREATE TABLE " + tableName +
                       " (id varchar(100) not null primary key," +
                       " clusterid varchar(100)," +
                       " lastaccess double integer," +
                       " beandata integer ," +
                       " containerid varchar(100))";
        message("\n" + query + "\n");

        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);
        ( (LobConnection) con).createLobTable(null, tableName);
        //((CluConnection)con).createLobTable (null,tableName);
    }    

    /** Drops the "statefulsessionbean" table from the HADB
     */    
    private void dropTable()
    {     
        try {
            message("dropTable");
            String query = "DROP TABLE " + tableName;
            Statement stmt = con.createStatement();
            stmt.executeUpdate(query);
            //((CluConnection)con).dropLobTable (null, tableName);
            //( (LobConnection) con).dropLobTable(null, tableName);
        } catch (SQLException ex) {
            message(ex.toString());
        }
   }
    
    /** Drops the "statefulsessionbean" table from the HADB
     */    
    private void dropTablePrevious() 
    {
        try {
            message("dropTable");
            String query = "DROP TABLE " + tableName;
            Statement stmt = con.createStatement();
            stmt.executeUpdate(query);
            //((CluConnection)con).dropLobTable (null, tableName);
            ( (LobConnection) con).dropLobTable(null, tableName);        
        } catch (SQLException ex) {
            message(ex.toString());
        }
    }    

    public static void recreateTable(String url, String user, String password) 
        throws SQLException, ClassNotFoundException 
    {        
        Class.forName(driver);
        String driverName = "jdbc:sun:hadb:" + user + "+" + password + "@" + url;
        message("driverName = " + driverName);
        SFSBTable bs = new SFSBTable(driverName);
        //SFSBTable bs = new SFSBTable(driverName + url);
        bs.dropTable();
        bs.createTable();
    }
    
    /** entry point to ths SFSBTable
     * @param args arguments should be jdbc:sun:hadb:system+super@node1:port1,node2:port2
     */    
    public static void main(String[] args) {
        if(args.length != 1 && args.length != 3)
        {
            System.out.println("usage: java SFSBTable url [user] [password]");
            System.exit(1);
        }
        try {            
            if (args.length == 3) {
                recreateTable(args[0], args[1], args[2]);
            } else {
                recreateTable(args[0], "system", "super");
            }
        }         
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}