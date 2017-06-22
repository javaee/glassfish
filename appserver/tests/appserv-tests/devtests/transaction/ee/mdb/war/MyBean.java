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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.acme;

import java.sql.*;
import java.util.Set;
import java.util.HashSet;
import javax.ejb.*;
import javax.jms.*;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.annotation.Resource;

/**
 *
 * @author marina vatkina
 */

@Singleton
public class MyBean {

    private static final String XA_RESOURCE = "jdbc/xa";

    @Resource(name="jms/MyQueueConnectionFactory", mappedName="jms/ejb_mdb_QCF")
    QueueConnectionFactory fInject;

    @Resource(mappedName="jms/ejb_mdb_Queue")
    Queue qInject;

    public void record(String msg) throws Exception {
        System.out.println("Adding msg: " + msg);

        InitialContext initCtx = new InitialContext();
        DataSource ds = (DataSource) initCtx.lookup(XA_RESOURCE);

        String insertStatement = "insert into messages values ( ? )";
        java.sql.Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement(insertStatement);

        ps.setString(1, msg);
        ps.executeUpdate();
        ps.close();
        c.close();
    }

    public int verifyxa() throws Exception {
        InitialContext initCtx = new InitialContext();
        DataSource ds = (DataSource) initCtx.lookup(XA_RESOURCE);

        return verify(ds, "student", 2) + verify(ds, "messages", 1);
   }

    public int verify(DataSource ds, String table, int columns) throws Exception {
        String selectStatement = "select * from " + table;
        java.sql.Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement(selectStatement);
        ResultSet rs = ps.executeQuery();
        int result = 0;
        while (rs.next()) {
            result++;
            StringBuffer buf = new StringBuffer();
            for (int i = 1; i <= columns; i++) {
                buf.append(": " + rs.getString(i));
            }
            System.out.println("Found: " + buf.toString());
        }
        rs.close();
        ps.close();
        c.close();

        return result;
    }

    public boolean testtwo(int id) throws Exception {
        InitialContext initCtx = new InitialContext();
        DataSource ds2 = (DataSource) initCtx.lookup(XA_RESOURCE);

        return test(id, ds2, true);
    }

    private boolean test(int id, DataSource ds, boolean useFailureInducer) throws Exception {
        String insertStatement = "insert into student values ( ? , ? )";
        java.sql.Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement(insertStatement);

        QueueConnection qConn = fInject.createQueueConnection();
        QueueSession qSession = qConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        QueueSender qSender = qSession.createSender(qInject);
        TextMessage tMessage = null;

        if (useFailureInducer) {
            com.sun.jts.utils.RecoveryHooks.FailureInducer.activateFailureInducer();
            com.sun.jts.utils.RecoveryHooks.FailureInducer.setWaitPoint(com.sun.jts.utils.RecoveryHooks.FailureInducer.PREPARED, 60);
        }

        for (int i = 0; i < 3; i++) {
            System.err.println("Call # " + (i + 1));
            ps.setString(1, "BAA" + id + i);
            ps.setString(2, "BBB" + id + i);
            ps.executeUpdate();

            tMessage = qSession.createTextMessage("MAA" + id + i);
            qSender.send(tMessage);

            if (!useFailureInducer) {
                try {
                    Thread.sleep(7000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        ps.close();
        c.close();
        qSession.close();
        qConn.close();
        System.err.println("Insert successfully");

        return true;
    }

}
