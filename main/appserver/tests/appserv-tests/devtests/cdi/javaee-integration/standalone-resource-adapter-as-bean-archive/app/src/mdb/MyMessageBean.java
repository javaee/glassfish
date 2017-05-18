/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package mdb;

import java.sql.Statement;

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import connector.MyMessageListener;
import javax.ejb.*;
import javax.naming.*;
import java.io.*;
import java.rmi.RemoteException;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import javax.sql.*;
import javax.jms.*;
import javax.transaction.*;


/**
 */
public class MyMessageBean implements MessageDrivenBean, 
        MyMessageListener {

    private transient MessageDrivenContext mdc = null;
    private Context context;
    
    @javax.inject.Inject
    private connector.TestCDIBean tb;

    /**
     * Constructor, which is public and takes no arguments.
     */
    public MyMessageBean() {}

    /**
     * setMessageDrivenContext method, declared as public (but 
     * not final or static), with a return type of void, and 
     * with one argument of type javax.ejb.MessageDrivenContext.
     *
     * @param mdc    the context to set
     */
    public void setMessageDrivenContext(MessageDrivenContext mdc) {
	this.mdc = mdc;
    }

    /**
     * ejbCreate method, declared as public (but not final or 
     * static), with a return type of void, and with no 
     * arguments.
     */
    public void ejbCreate() { }

    /**
     * onMessage method, declared as public (but not final or 
     * static), with a return type of void, and with one argument
     * of type javax.jms.Message.
     *
     * Casts the incoming Message to a TextMessage and displays 
     * the text.
     *
     * @param inMessage    the incoming message
     */
    public void onMessage(String inMessage) {

        debug("onMessage:: RECEIVED [" + inMessage + "]");
        debug("TestCDIBean injected:" + tb);

        if (tb == null)
            throw new RuntimeException("Injection of enabled Bean "
                    + "in RAR into a MDB injection point failed");

        try {
            if (inMessage.endsWith("WRITE")) {
                doDbStuff("WRITE", 
                        inMessage.substring(0, inMessage.lastIndexOf(":")));
            } else if (inMessage.endsWith("DELETE")) {
                doDbStuff("DELETE",
                        inMessage.substring(0, inMessage.lastIndexOf(":")));
            } else if (inMessage.endsWith("DELETE_ALL")) {
                doDbStuff("DELETE_ALL", "::");
            } else {
                //unsupported op.
            }
        } catch (Exception ex) {
            debug("UH OH...");
            ex.printStackTrace();
        }

    }
    
    /**
     * ejbRemove method, declared as public (but not final or 
     * static), with a return type of void, and with no 
     * arguments.
     */
    public void ejbRemove() {}


    private void doDbStuff(String op, String message) throws Exception {

        java.sql.Connection dbConnection = null;
        String id    = message.substring(0, message.indexOf(":"));
        String body  = message.substring(message.indexOf(":")+1); 
        try {
            Context ic = new InitialContext();
            
            if ("READ".equals(op)) {
                
                debug("Reading row from database...");
                
                // Creating a database connection
                /*
                  DataSource ds = (DataSource) ic.lookup("java:comp/env/MyDB");
                  debug("Looked up Datasource\n");
                  debug("Get JDBC connection, auto sign on");
                  dbConnection = ds.getConnection();
                  
                  Statement stmt = dbConnection.createStatement();
                  String query = 
                  "SELECT id from messages where id = 'QQ'";
                  ResultSet results = stmt.executeQuery(query);
                  results.next();
                  System.out.println("QQ has balance " + 
                  results.getInt("balance") + " dollars");
                  results.close();
                  stmt.close();
                  
                  System.out.println("Read one account\n");
                */

            } else if ("WRITE".equals(op)) {

                debug("Inserting one message in the database\n");
            
                // Creating a database connection
                DataSource ds = (DataSource) ic.lookup("java:comp/env/MyDB");
                //debug("Looked up Datasource\n");
                //debug("Get JDBC connection, auto sign on");
                dbConnection = ds.getConnection();
                
                createRow(id, body, dbConnection);
                System.out.println("Created one message\n");
                
            } else if ("DELETE".equals(op)) {
                
                debug("Deleting one message from the database\n");
                
                // Creating a database connection
                DataSource ds = (DataSource) ic.lookup("java:comp/env/MyDB");
                //debug("Looked up Datasource\n");
                //debug("Get JDBC connection, auto sign on");
                dbConnection = ds.getConnection();
                
                deleteRow(id, dbConnection);
                System.out.println("Deleted one message\n");
            } else if ("DELETE_ALL".equals(op)) {
                
                debug("Deleting all messages from the database\n");
                
                // Creating a database connection
                DataSource ds = (DataSource) ic.lookup("java:comp/env/MyDB");
                //debug("Looked up Datasource\n");
                //debug("Get JDBC connection, auto sign on");
                dbConnection = ds.getConnection();
                deleteAll(dbConnection);
                System.out.println("Deleted all messages\n");
            } else {
                //unsupported op
            }
            
        }finally{
            try{
                dbConnection.close();
            }catch(Exception ex){
                debug("Exception occured while closing database con nection.");
            }
        }
    }
    
    private void createRow(String id, String body, 
            java.sql.Connection dbConnection) 
        throws Exception {

        // Create row for this message
        debug("CreateRow with ID = " + id + ", BODY = " + body);
        Statement stmt = dbConnection.createStatement();
        String query = "INSERT INTO messages (messageId, message)" +
            "VALUES ('" + id + "', '" + body + "')";
        int resultCount = stmt.executeUpdate(query);
        if ( resultCount != 1 ) {
            throw new Exception(
                    "ERROR in INSERT !! resultCount = "+resultCount);
        }
        stmt.close();
    }

    private void deleteRow(String id, java.sql.Connection dbConnection) 
        throws Exception {
        
        // Delete row for this message
        debug("DeleteRow with ID = " + id);
        Statement stmt = dbConnection.createStatement();
        String query = "DELETE FROM messages WHERE messageId = '" + id + "'";
        int resultCount = stmt.executeUpdate(query);
        if ( resultCount != 1 ) {
            throw new Exception(
                    "ERROR in INSERT !! resultCount = "+resultCount);
        }
        stmt.close();
    }

    private void deleteAll(java.sql.Connection dbConnection) 
        throws Exception {
        
        // Delete row for this message
        Statement stmt = dbConnection.createStatement();
        String query = "DELETE FROM messages";
        int resultCount = stmt.executeUpdate(query);
        debug("Delete all rows from messages... count = " + resultCount);
        stmt.close();
    }

    private void debug(String msg) {
        System.out.println("[MyMessageBean] --> " + msg);
    }
}
