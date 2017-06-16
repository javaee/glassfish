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
import javax.ejb.*;
import javax.sql.DataSource;
import javax.naming.InitialContext;

/**
 *
 * @author marina vatkina
 */

@Stateless
public class MyBean {

    private static final String DEF_RESOURCE = "jdbc/__default";
    private static final String XA_RESOURCE = "jdbc/xa";

    public int verifydefault() throws Exception {
        InitialContext initCtx = new InitialContext();
        DataSource ds = (DataSource) initCtx.lookup(DEF_RESOURCE);

        return verify(ds);
   }

    public int verifyxa() throws Exception {
        InitialContext initCtx = new InitialContext();
        DataSource ds = (DataSource) initCtx.lookup(XA_RESOURCE);

        return verify(ds);
   }

    public int verify(DataSource ds) throws Exception {
        String selectStatement = "select * from student";
        Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement(selectStatement);
        ResultSet rs = ps.executeQuery();
        int result = 0;
        while (rs.next()) {
            result++;
            System.out.println("Found: " + rs.getString(1) + " : " + rs.getString(2));
        }
        rs.close();
        ps.close();
        c.close();

        return result;
    }

    public boolean testone(int id) throws Exception {
        InitialContext initCtx = new InitialContext();
        DataSource ds = (DataSource) initCtx.lookup(DEF_RESOURCE);

        return test(id, ds, false);
    }

    public boolean testtwo(int id) throws Exception {
        InitialContext initCtx = new InitialContext();
        DataSource ds1 = (DataSource) initCtx.lookup(DEF_RESOURCE);
        DataSource ds2 = (DataSource) initCtx.lookup(XA_RESOURCE);

        System.err.println("Insert in DEF_RESOURCE");
        boolean res1 = test(id, ds1, true);
        System.err.println("Insert in XA_RESOURCE");
        boolean res2 = test(id, ds2, true);
        return res1 && res2;
    }

    private boolean test(int id, DataSource ds, boolean useFailureInducer) throws Exception {
        String insertStatement = "insert into student values ( ? , ? )";
        Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement(insertStatement);

        if (useFailureInducer) {
            com.sun.jts.utils.RecoveryHooks.FailureInducer.activateFailureInducer();
            com.sun.jts.utils.RecoveryHooks.FailureInducer.setWaitPoint(com.sun.jts.utils.RecoveryHooks.FailureInducer.PREPARED, 60);
        }

        for (int i = 0; i < 3; i++) {
            System.err.println("Call # " + (i + 1));
            ps.setString(1, "BAA" + id + i);
            ps.setString(2, "BBB" + id + i);
            ps.executeUpdate();

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
        System.err.println("Insert successfully");

        return true;
    }

}
