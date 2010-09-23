/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun 
 * Microsystems, Inc.  Use is subject to license terms.
 * 
 */

package mdb;

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
