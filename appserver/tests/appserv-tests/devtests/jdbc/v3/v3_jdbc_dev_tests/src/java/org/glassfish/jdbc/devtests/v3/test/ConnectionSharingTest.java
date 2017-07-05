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

package org.glassfish.jdbc.devtests.v3.test;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.SystemException;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;

/**
 *
 * @author jagadish
 */
public class ConnectionSharingTest implements SimpleTest{

    
    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();

    public Map<String, Boolean> runTest(DataSource ds1, PrintWriter out) {
        HtmlUtil.printHR(out);
        try {
            if (testConnectionSharing(ds1, out)) {
                resultsMap.put("connection-sharing", true);
            }else{
                resultsMap.put("connection-sharing", false);
            }
        } catch (Exception e) {
            resultsMap.put("connection-sharing", false);
        }
        HtmlUtil.printHR(out);
        return resultsMap;
    }

    
     private boolean testConnectionSharing(DataSource ds1, PrintWriter out) throws SystemException {
        boolean result = false;
        Connection con = null;
        Connection con1 = null;
        Statement stmt = null;
        ResultSet rs = null;

        out.println("<h4> connection-sharing test </h4>");
        javax.transaction.UserTransaction ut = null;
        try {
            out.println("<br>Starting test ...");
            InitialContext ic = new InitialContext();
            ut = (javax.transaction.UserTransaction) ic.lookup("java:comp/UserTransaction");
            out.println("<br>Able to lookup UserTransaction");
            ut.begin();
            out.println("<br> Started UserTransaction");

            out.println("<br>Trying to get connection ...");

            out.println("<br>ds value : " + ds1);
            con = ds1.getConnection();
            com.sun.appserv.jdbc.DataSource myDS = ((com.sun.appserv.jdbc.DataSource) ds1);
            Connection con_ = myDS.getConnection(con);
            out.println("<br>Got connection - con : " + con_);

            con1 = ds1.getConnection();
            Connection con1_ = myDS.getConnection(con1);
            out.println("<br> Got connection - con1 : " + con1_);
            if (con1_ == con_) {
                result = true;
            }

            ut.commit();


        } catch (Throwable e) {
            HtmlUtil.printException(e, out);
            out.println("Rolling back transaction<br>");
            ut.rollback();
            result = false;
        } finally {

            try {
                if (con1 != null) {
                    con1.close();
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
            }

            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
            }
            
            out.println("<br> Test result : " + result);
            return result;
        }
    }
}
