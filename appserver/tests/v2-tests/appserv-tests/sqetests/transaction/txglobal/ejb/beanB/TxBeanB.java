/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.transaction.txglobal.ejb.beanB;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Enumeration;
import java.rmi.RemoteException;

import javax.jms.Queue;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.JMSException;
import javax.jms.QueueReceiver;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;


public class TxBeanB implements SessionBean {

    private Queue queue = null;
    private String user = null;
    private String dbURL1 = null;
    private String dbURL2 = null;
    private String password = null;
    private SessionContext ctx = null;   
    private QueueConnectionFactory qfactory = null;

    // ------------------------------------------------------------------------
    // Container Required Methods
    // ------------------------------------------------------------------------
    public void setSessionContext(SessionContext sc) {
        System.out.println("setSessionContext in BeanB");  
        try {
            ctx = sc;
            Context ic = new InitialContext();
            user = (String) ic.lookup("java:comp/env/user");
            password = (String) ic.lookup("java:comp/env/password");
            dbURL1 = (String) ic.lookup("java:comp/env/dbURL1");
            dbURL2 = (String) ic.lookup("java:comp/env/dbURL2");
        } catch (Exception ex) {
            System.out.println("Exception in setSessionContext: " +
                               ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void ejbCreate() {
        System.out.println("ejbCreate in BeanB");  
        try {
            Context context = new InitialContext();
            
            qfactory = (QueueConnectionFactory) 
            context.lookup("java:comp/env/jms/QCFactory");
            
            queue = (Queue) context.lookup("java:comp/env/jms/SampleQueue");

            System.out.println("QueueConnectionFactory & " +
                               "Queue Initialized Successfully");
        } catch (Exception ex) {
            System.out.println("Exception in ejbCreate: " + ex.toString());
            ex.printStackTrace();
        }
    } 

    public void ejbDestroy() {
        System.out.println("ejbDestroy in BeanB");  
    }

    public void ejbActivate() {
        System.out.println("ejbActivate in BeanB");  
    }
  
    public void ejbPassivate() {
        System.out.println("ejbPassivate in BeanB");  
    }

    public void ejbRemove() {
        System.out.println("ejbRemove in BeanB");  
    }

    // ------------------------------------------------------------------------
    // Business Logic Methods
    // ------------------------------------------------------------------------
    public void insert(String acc, float bal) throws RemoteException {
        Connection con1 = null;
        Connection con2 = null;
        System.out.println("insert in BeanB");  
        try {
            con1 = getConnection(dbURL1);
            Statement stmt1 = con1.createStatement();
            stmt1.executeUpdate("INSERT INTO txAccount VALUES ('" + acc +
                               "', " + bal + ")");
            System.out.println("Account added Successfully in DB1...");

            con2 = getConnection(dbURL2);
            Statement stmt2 = con2.createStatement();
            stmt2.executeUpdate("INSERT INTO txAccount VALUES ('" + acc +
                                "', " + bal + ")");
            System.out.println("Account added Successfully in DB2...");

            stmt1.close();
            stmt2.close();
        } catch (Exception ex) {
            System.out.println("Exception in insert: " + ex.toString());
            ex.printStackTrace();
        } finally {
            try {
                con1.close();
                con2.close();
            } catch (java.sql.SQLException ex) {
            }
        }
    }

    public void delete(String account) throws RemoteException {
        Connection con1 = null;
        Connection con2 = null;
        System.out.println("delete in BeanB");  
        try {
            con1 = getConnection(dbURL1);
            Statement stmt1 = con1.createStatement();
            stmt1.executeUpdate("DELETE FROM txAccount WHERE account = '"
                               + account + "'");
            System.out.println("Account deleted Successfully in DB1...");

            con2 = getConnection(dbURL2);
            Statement stmt2 = con2.createStatement();
            stmt2.executeUpdate("DELETE FROM txAccount WHERE account = '"
                               + account + "'");
            System.out.println("Account deleted Successfully in DB2...");

            stmt1.close();
            stmt2.close();
        } catch (Exception ex) {
            System.out.println("Exception in delete: " + ex.toString());
            ex.printStackTrace();
        } finally {
            try {
                con1.close();
                con2.close();
            } catch (java.sql.SQLException ex) {
            }
        }
    }

    public void sendJMSMessage(String msg) throws RemoteException {
        System.out.println("sendJMSMessage in BeanB");  
        try {
            QueueConnection qconn = qfactory.createQueueConnection();
            QueueSession qsession = qconn.createQueueSession(true, 0);
            QueueSender sender = qsession.createSender(queue);
            TextMessage message = qsession.createTextMessage();
            message.setText(msg);
            sender.send(message);
            sender.send(qsession.createMessage());
            System.out.println("Message added Successfully in Queue: " + msg);
            qsession.close();
            qconn.close();
        } catch (JMSException ex) {
            ex.printStackTrace();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }


    public boolean verifyResults(String account, String resource)
                                throws RemoteException {
        boolean result = false;
        System.out.println("verifyResults in BeanB");  
        try {
            if (resource.equals("DB1")) {
                result = checkResult(getConnection(dbURL1), account);
            } else if (resource.equals("DB2")) {
                result = checkResult(getConnection(dbURL2), account);
            } else if (resource.equals("JMS")) {
                QueueConnection qconn = qfactory.createQueueConnection();
                QueueSession session = qconn.createQueueSession(true, 0);
                QueueReceiver receiver = session.createReceiver(queue);
                qconn.start();

                Message message = receiver.receive(5000);
                if (message != null) {
                    if (message instanceof TextMessage) {
                        TextMessage msg = (TextMessage) message;
                        String str = msg.getText();
                        if ( str.equals(account) ) {
                            result = true;
                        }
                    }
                }

                // close the QueueSession
                session.close();
                qconn.close();
            }
        } catch (Exception ex) {
            System.out.println("Exception in verifyResults: " + ex.toString());
            ex.printStackTrace();
        }
        return result;
    }

    private boolean checkResult(Connection conn, String account) {
        boolean result = false;
        System.out.println("checkResult in BeanB");  
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM txAccount WHERE " +
                                             "account = '"+ account + "'");

            if ( rs.next() ) { 
                result = true;
            }
        } catch (Exception ex) {
            System.out.println("Exception in checkResult: " + ex.toString());
            ex.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (java.sql.SQLException ex) { }
        }
        return result;
    }

    private Connection getConnection(String dbURL) {
        Connection con = null;
        System.out.println("getConnection in BeanB");  
        try{
            Context context = new InitialContext();
            DataSource ds = (DataSource) context.lookup(dbURL);
            con = ds.getConnection(user, password);
            System.out.println("Got DB Connection Successfully...");
        } catch (Exception ex) {
            System.out.println("Exception in getConnection: " + ex.toString());
	    ex.printStackTrace();
        }
        return con;
    }
}
