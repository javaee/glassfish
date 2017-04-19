/**
 * Copyright š 2002 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.s1peqe.transaction.txlao.ejb.beanB;

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
import java.util.*;

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
    private String dbURL1XA = null;
    private String dbURL2XA = null;
    private String dbURL1NonXA = null;
    private String dbURL2NonXA = null;
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
            dbURL1XA = (String) ic.lookup("java:comp/env/dbURL1-XA");
            dbURL2XA = (String) ic.lookup("java:comp/env/dbURL2-XA");
            dbURL1NonXA = (String) ic.lookup("java:comp/env/dbURL1-NonXA");
            dbURL2NonXA = (String) ic.lookup("java:comp/env/dbURL2-NonXA");
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
    public void firstXAJDBCSecondNonXAJDBC(String acc, float bal) throws RemoteException {

       Connection con1 = null;
       Connection con2 = null;
       System.out.println("insert in BeanB");
        try {
            con1 = getConnection(dbURL1XA);
            Statement stmt1 = con1.createStatement();
            stmt1.executeUpdate("INSERT INTO txAccount VALUES ('" + acc +
                               "', " + bal + ")");
            System.out.println("Account added Successfully in "+dbURL1XA+"...");

            con2 = getConnection(dbURL2NonXA);
            Statement stmt2 = con2.createStatement();
            stmt2.executeUpdate("INSERT INTO txAccount VALUES ('" + acc +
                                "', " + bal + ")");
            System.out.println("Account added Successfully in "+ dbURL2NonXA+"DB2...");

            stmt1.close();
            stmt2.close();
        } catch (Exception ex) {
            System.out.println("Exception in insert: " + ex.toString());
            ex.printStackTrace();
            throw new RemoteException(ex.getMessage());
        } finally {
            try {
                con1.close();
                con2.close();
            } catch (java.sql.SQLException ex) {
            }
        }
    }

    public void firstNonXAJDBCSecondXAJDBC(String acc, float bal) throws RemoteException {

        Connection con1 = null;
        Connection con2 = null;
        System.out.println("insert in BeanB");
        try {
            con1 = getConnection(dbURL1NonXA);
            Statement stmt1 = con1.createStatement();
            stmt1.executeUpdate("INSERT INTO txAccount VALUES ('" + acc +
                               "', " + bal + ")");
            System.out.println("Account added Successfully in "+dbURL1NonXA+"...");

            con2 = getConnection(dbURL2XA);
            Statement stmt2 = con2.createStatement();
            stmt2.executeUpdate("INSERT INTO txAccount VALUES ('" + acc +
                                "', " + bal + ")");
            System.out.println("Account added Successfully in"+dbURL2XA+"...");

            stmt1.close();
            stmt2.close();
        } catch (Exception ex) {
            System.out.println("Exception in insert: " + ex.toString());
            ex.printStackTrace();
            throw new RemoteException(ex.getMessage());
        } finally {
            try {
                con1.close();
                con2.close();
            } catch (java.sql.SQLException ex) {
            }
        }
    }

    public void firstXAJDBCSecondXAJDBC(String acc, float bal) throws RemoteException {

        Connection con1 = null;
        Connection con2 = null;
        System.out.println("insert in BeanB");
        try {
            con1 = getConnection(dbURL1XA);
            Statement stmt1 = con1.createStatement();
            stmt1.executeUpdate("INSERT INTO txAccount VALUES ('" + acc +
                               "', " + bal + ")");
            System.out.println("Account added Successfully in "+dbURL1XA+"...");

            con2 = getConnection(dbURL2XA);
            Statement stmt2 = con2.createStatement();
            stmt2.executeUpdate("INSERT INTO txAccount VALUES ('" + acc +
                                "', " + bal + ")");
            System.out.println("Account added Successfully in"+dbURL2XA+"...");

            stmt1.close();
            stmt2.close();
        } catch (Exception ex) {
            System.out.println("Exception in insert: " + ex.toString());
            ex.printStackTrace();
            throw new RemoteException(ex.getMessage());
        } finally {
            try {
                con1.close();
                con2.close();
            } catch (java.sql.SQLException ex) {
            }
        }
    }

    public void firstNonXAJDBCSecondNonXAJDBC(String acc, float bal) throws RemoteException {

        Connection con1 = null;
        Connection con2 = null;
        System.out.println("insert in BeanB");
        try {
            con1 = getConnection(dbURL1NonXA);
            Statement stmt1 = con1.createStatement();
            stmt1.executeUpdate("INSERT INTO txAccount VALUES ('" + acc +
                               "', " + bal + ")");
            System.out.println("Account added Successfully in "+dbURL1NonXA+"...");

            con2 = getConnection(dbURL2NonXA);
            Statement stmt2 = con2.createStatement();
            stmt2.executeUpdate("INSERT INTO txAccount VALUES ('" + acc +
                                "', " + bal + ")");
            System.out.println("Account added Successfully in"+dbURL2NonXA+"...");

            stmt1.close();
            stmt2.close();
        } catch (Exception ex) {
            System.out.println("Exception in insert: " + ex.toString());
            ex.printStackTrace();
            throw new RemoteException(ex.getMessage());
        } finally {
            try {
                con1.close();
                con2.close();
            } catch (java.sql.SQLException ex) {
            }
        }
    }

    public void firstXAJMSSecondNonXAJDBC(String msg, String acc, float bal) throws RemoteException {
        System.out.println("sendJMSMessage in BeanB");
        sendJMSMessage(msg);
        Connection con1 = null;

        System.out.println("insert in BeanB");
        try {
            con1 = getConnection(dbURL2NonXA);
            Statement stmt1 = con1.createStatement();
            stmt1.executeUpdate("INSERT INTO txAccount VALUES ('" + acc +
                               "', " + bal + ")");
            System.out.println("Account added Successfully in "+dbURL2NonXA+"...");

            stmt1.close();
        } catch (Exception ex) {
            System.out.println("Exception in insert: " + ex.toString());
            ex.printStackTrace();
            throw new RemoteException(ex.getMessage());
        } finally {
            try {
                con1.close();
            } catch (java.sql.SQLException ex) {
            }
        }
    }

    public void firstNonXAJDBCOnly(String acc, float bal) throws RemoteException {

        Connection con1 = null;
        System.out.println("insert in BeanB");
        try {
            con1 = getConnection(dbURL1NonXA);
            Statement stmt1 = con1.createStatement();
            stmt1.executeUpdate("INSERT INTO txAccount VALUES ('" + acc +
                               "', " + bal + ")");
            System.out.println("Account added Successfully in "+dbURL1NonXA+"...");

            stmt1.close();
        } catch (Exception ex) {
          System.out.println("Exception in insert: " + ex.toString());
          ex.printStackTrace();
          throw new RemoteException(ex.getMessage());
        } finally {
            try {
                con1.close();
            } catch (java.sql.SQLException ex) {
            }
        }

    }

    public void rollbackXAJDBCNonXAJDBC(String acc, float bal) throws RemoteException {
        System.out.println("rollbackXAJDBCNonXAJDBC in BeanA");
        try {
              insert(dbURL1XA,acc,bal);
              ctx.setRollbackOnly();
              insert(dbURL2NonXA,acc,bal);
        } catch(Throwable ex) {
          System.out.println("Exception in rollbackXAJDBCNonXAJDBC: " + ex.toString());
          //ex.printStackTrace();
          throw new RemoteException(ex.getMessage());
        }
    }
    public void rollbackNonXAJDBCXAJDBC(String acc, float bal) throws RemoteException {
        System.out.println("rollbackXAJDBCNonXAJDBC in BeanA");
        try {
              insert(dbURL1NonXA,acc,bal);
              ctx.setRollbackOnly();
              insert(dbURL2XA,acc,bal);
        } catch(Throwable ex) {
          System.out.println("Exception in rollbackXAJDBCNonXAJDBC: " + ex.toString());
          //ex.printStackTrace();
          throw new RemoteException(ex.getMessage());
        }
    }
    public void insert(String dbURL,String acc, float bal) throws RemoteException {
        Connection con = null;
        System.out.println("insert in BeanB");
        try {
            con = getConnection(dbURL);
            Statement stmt = con.createStatement();
            stmt.executeUpdate("INSERT INTO txAccount VALUES ('" + acc +
                               "', " + bal + ")");
            System.out.println("Account added Successfully in "+dbURL+"...");

            stmt.close();
        } catch (Exception ex) {
            System.out.println("Exception in insert: " + ex.toString());
            ex.printStackTrace();
            throw new RemoteException(ex.getMessage());
        } finally {
            try {
                con.close();
            } catch (java.sql.SQLException ex) {
            }
        }
    }

    public void delete(String dbURL, String account) throws RemoteException {
        Connection con = null;
        System.out.println("delete in BeanB");
        try {
            con = getConnection(dbURL);
            Statement stmt = con.createStatement();
            stmt.executeUpdate("DELETE FROM txAccount WHERE account = '"
                               + account + "'");
            System.out.println("Account deleted Successfully in "+dbURL+"...");

            stmt.close();
        } catch (Exception ex) {
            System.out.println("Exception in delete: " + ex.toString());
            ex.printStackTrace();
            throw new RemoteException(ex.getMessage());
        } finally {
            try {
                con.close();
            } catch (java.sql.SQLException ex) {
            }
        }
    }

    public void delete(String account)  throws RemoteException {
      Connection con1 = null;
      Connection con2 = null;
      Connection con3 = null;
      Connection con4 = null;
        System.out.println("delete in BeanB");
        try {
            con1 = getConnection(dbURL1XA);
            Statement stmt1 = con1.createStatement();
            stmt1.executeUpdate("DELETE FROM txAccount WHERE account = '"
                               + account + "'");
            System.out.println("Account deleted Successfully in dbURL1XA...");

            con2 = getConnection(dbURL2XA);
            Statement stmt2 = con2.createStatement();
            stmt2.executeUpdate("DELETE FROM txAccount WHERE account = '"
                               + account + "'");
            System.out.println("Account deleted Successfully in dbURL2XA...");

            stmt1.close();
            stmt2.close();
        } catch (Exception ex) {
            System.out.println("Exception in delete: " + ex.toString());
            ex.printStackTrace();
            throw new RemoteException(ex.getMessage());
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
            System.out.println("sendJMSMessage:String being sent = "+msg);
            message.setText(msg);
            sender.send(message);
            System.out.println("sendJMSMessage:Message sent succefully");
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


    public boolean verifyResults(String account, String resource, String resType)
                                throws RemoteException {
        boolean result = false;
        System.out.println("verifyResults in BeanB");
        try {
            if (resource.equals("DB1") && resType.equals("XA")) {
                result = checkResult(getConnection(dbURL1XA), account);
            } else if (resource.equals("DB2") && resType.equals("XA")) {
                result = checkResult(getConnection(dbURL2XA), account);
            } else if (resource.equals("DB1") && resType.equals("NonXA")) {
                result = checkResult(getConnection(dbURL1NonXA), account);
            } else if (resource.equals("DB2") && resType.equals("NonXA") ) {
                result = checkResult(getConnection(dbURL2NonXA), account);
            } else if (resource.equals("JMS")) {
                QueueConnection qconn = qfactory.createQueueConnection();
                QueueSession session = qconn.createQueueSession(true, 0);
                QueueReceiver receiver = session.createReceiver(queue);
                qconn.start();

                Message message = receiver.receive(5000);
                System.out.println("verifyResults:Message received = "+message);
                if (message != null) {
                    System.out.println("message is not null");
                    if (message instanceof TextMessage) {
                        TextMessage msg = (TextMessage) message;
                        String str = msg.getText();
                        System.out.println("verifyResults:string received = "+str);
                        if ( str.equals(account) ) {
                            result = true;
                        }
                    } else {
                        message = receiver.receive(5000);
                        System.out.println("verifyResults:Message received = "+message);
                        if (message != null) {
                            System.out.println("message is not null");
                            if (message instanceof TextMessage) {
                                TextMessage msg = (TextMessage) message;
                                String str = msg.getText();
                                System.out.println("verifyResults:string received = "+str);
                                if ( str.equals(account) ) {
                                    result = true;
                                }
                            }
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
