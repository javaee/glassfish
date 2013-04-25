/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package beans;

import connector.MyAdminObject;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.ejb.CreateException;
import java.util.Properties;
import java.sql.*;

import javax.transaction.UserTransaction;
import javax.naming.*;
import javax.sql.*;

public class MessageCheckerEJB implements SessionBean {

    private int WAIT_TIME = 15;
    private String user = "j2ee";
    private String password = "j2ee";
    private Properties beanProps = null;
    private SessionContext sessionContext = null;
    private Connection heldCon = null;
    private MyAdminObject Controls;

    public MessageCheckerEJB() {}

    public void ejbCreate() 
        throws CreateException {
        System.out.println("bean created");
        heldCon = null;
        /*
          if (holdConnection) {
          try {
          heldCon = getDBConnection();
          } catch (Exception ex) {
          ex.printStackTrace();
          throw new CreateException("Error in ejbCreate");
          }
          }
        */
    }

    public boolean done() {
        return Controls.done();
    }

    public boolean testAdminObjectResourceAdapterAssociation() {
        boolean pass = false;

        try {
            InitialContext ic = new InitialContext();
            MyAdminObject mao = (MyAdminObject) ic.lookup("java:comp/env/eis/testAdmin");
            if (mao.getResourceAdapter() != null) {
                System.out.println("mao.getRA() is *NOT* null");
                pass = true;
            } else {
                System.out.println("mao.getRA() is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        pass = getAndCloseConnection("java:module/jdbc/ejb-level-ds", 1);

        if (pass) {
            pass = getAndCloseConnection("java:module/jdbc/monitoring-ds", 5);
        } else {
            getAndCloseConnection("java:module/jdbc/monitoring-ds", 5);
        }
        return pass;
    }

    public boolean getAndCloseConnection(String resourceName, int count) {
        boolean pass = true;
        Connection conns[] = null;
        try {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup(resourceName);
            System.out.println("Looking up resource from MDB : [ " + resourceName + " ]:  " + ds);

            conns = new Connection[count];
            for (int i = 0; i < count; i++) {
                conns[i] = ds.getConnection();
                System.out.println("Got connection in MDB for resource: [ " + resourceName + " ]:  " + conns[i]);
            }
            pass = true;
        } catch (Exception e) {
            e.printStackTrace();
            pass = false;
        } finally {
            if (conns != null) {
                for (int i = 0; i < count; i++) {
                    try {
                        conns[i].close();
                        System.out.println("Closed connection in MDB for resource: [ " + resourceName + " ]:  " + conns[i]);
                    } catch (Exception e) {
                        //do nothing
                    }
                }
            }
        }
        return pass;
    }

    public int expectedResults() {
        return Controls.expectedResults();
    }

    public void notifyAndWait() {
        try {
            synchronized (Controls.getLockObject()) {
                //Tell the resource adapter the client is ready to run
                Controls.getLockObject().notifyAll(); 
                
                debug("NOTIFIED... START WAITING");
                //Wait until being told to read from the database
                Controls.getLockObject().wait(); 
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void hackGetConnection() {
        try{
            InitialContext ic = new InitialContext();
            DataSource timerDS   = (DataSource)ic.lookup("jdbc/__TimerPool");
            Connection con = timerDS.getConnection();
            con.close();
        }catch(Exception e){
            System.out.println("Hack to initialize tx-manager, failed to initialize");
        }
    }

    public int getMessageCount() {
	try {
            Connection con = getFreshConnection();
            int count1 = getCount(con);
            con.close();
            
	    /*
            synchronized(Controls.getLockObject()) {
                Controls.getLockObject().notify();
            }
	    */
                
            return count1;
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new EJBException(e);
	}
    }

    private int getCount(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        int count = 0;
        String messages = "";
        ResultSet result = stmt.executeQuery(
                "SELECT messageId, message "+ "FROM messages");
        while (result.next()) {
            count++;
            messages = messages + " - " + result.getString("messageId")+" "+
                result.getString("message") + "\n";
        }
        messages = messages + "count = " + count;
        System.out.println(messages);
        stmt.close();
        return count;
    }

    public void setSessionContext(SessionContext context) {
        sessionContext = context;
        try {
            Context ic = new InitialContext();
            user = (String) ic.lookup("java:comp/env/user");
            password = (String) ic.lookup("java:comp/env/password");
	    Controls = (MyAdminObject) ic.lookup("java:comp/env/eis/testAdmin");
	    System.out.println("CALLING INITILIZE ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]");
	    Controls.initialize();
	    System.out.println("CALLED INITILIZE ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]" + Controls);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void ejbRemove() {
        System.out.println("bean removed");
    }

    public void ejbActivate() {
        System.out.println("bean activated");
    }

    public void ejbPassivate() {
        System.out.println("bean passivated");
    }

    private Connection getFreshConnection() throws Exception {
        Connection oldHeldCon = heldCon;
        heldCon = null;
        Connection result = getDBConnection();
        heldCon = oldHeldCon;
        return result;
    }

    private Connection getDBConnection() throws Exception {
        if (heldCon != null) return heldCon;
        Connection con = null;
        try {
            Context ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup("java:comp/env/MyDB");
            debug("Looked up Datasource\n");
            debug("Get JDBC connection, auto sign on");
            con = ds.getConnection();
            
            if (con != null) {
                return con;
            } else {
                throw new Exception("Unable to get database connection ");
            }
        } catch (SQLException ex1) {
            //ex1.printStackTrace();
            throw ex1;
        }
    }
    
    private void closeConnection(Connection con) throws SQLException {
        if (heldCon != null) {
            return;
        } else {
            con.close();
        }
    }

    private void debug(String msg) {
        System.out.println("[MessageCheckerEJB]:: -> " + msg);
    }
}
