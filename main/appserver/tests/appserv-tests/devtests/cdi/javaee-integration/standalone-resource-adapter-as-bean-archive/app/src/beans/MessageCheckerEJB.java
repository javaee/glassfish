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
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package beans;

import connector.MyAdminObject;
import javax.rmi.PortableRemoteObject;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.ejb.CreateException;
import java.util.Properties;
import java.util.Vector;
import java.sql.*;
import java.rmi.RemoteException;

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
        System.out.println("bean removed");
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
	    System.out.println("CALLED INITILIZE ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]");
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
