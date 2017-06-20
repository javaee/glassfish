/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.transaction.txstressreadonly.ejb.beanB;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import java.sql.Statement;
import java.sql.ResultSet;
import javax.transaction.UserTransaction;
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
    private String user1 = null;
    private String user2 = null;
    private String dbURL1 = null;
    private String dbURL2 = null;
    private String password1 = null;
    private String password2 = null;
private UserTransaction tx = null;
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
            user1 = (String) ic.lookup("java:comp/env/user1");
            password1 = (String) ic.lookup("java:comp/env/password1");
            user2 = (String) ic.lookup("java:comp/env/user2");
            password2 = (String) ic.lookup("java:comp/env/password2");
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
    public void insert(String acc, float bal, int identity) throws RemoteException {
        tx = ctx.getUserTransaction();
        Connection con1 = null;
        Connection con2 = null;
        System.out.println("insert in BeanB");
        try {
        tx.begin();
        con1 = getConnection(dbURL1,user1,password1);
        Statement stmt1 = con1.createStatement();
        ResultSet rs = stmt1.executeQuery("SELECT * FROM txAccount");
        System.out.println("Account Read Successfully from DB1...");
        con2 = getConnection(dbURL2,user2,password2);
        Statement stmt2 = con2.createStatement();
        stmt2.executeUpdate("INSERT INTO txAccount VALUES ('" + (acc+identity) +
                             "', " + bal + ")");
        System.out.println("Account added Successfully in DB2...");
        stmt1.close();
	    con1.close();
        stmt2.close();
        con2.close();
        tx.commit();
        tx.begin();
        con2 = getConnection(dbURL2,user2,password2);
        stmt2 = con2.createStatement();
        stmt2.executeUpdate("DELETE FROM txAccount WHERE account = '"
                            + (acc+identity) + "'");
        System.out.println("Account deleted Successfully in DB2...");
        stmt2.close();
        con2.close();
        tx.commit();
        } catch (Exception ex) {
            System.out.println("Exception in insert: " + ex.toString());
            ex.printStackTrace();
        } finally {
            try {
            //    con1.close();
             //   con2.close();
            } catch (Exception ex) {
            }
        }
    }



    private Connection getConnection(String dbURL,String user, String password) {
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
