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
public class AssocWithThreadTest implements SimpleTest{

    
        Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();

    public Map<String, Boolean> runTest(DataSource ds1, PrintWriter out) {
        try {
            if (testAssocWithThread(ds1, out)) {
                resultsMap.put("assoc-with-thread", true);
            }else{
                resultsMap.put("assoc-with-thread", false);
            }
        } catch (Exception e) {
            resultsMap.put("assoc-with-thread", false);
        }
        return resultsMap;
    }
    
    private boolean testAssocWithThread(DataSource ds1, PrintWriter out) throws SystemException {

        
        HtmlUtil.printHR(out);
        out.println("<h4> Assoc-with-thread test </h4>");

        String result1 = test1(ds1, out);
        String result2 = test1(ds1, out);

        out.println("physical connection 1 : " + result1);
        out.println("physical connection 2 : " + result2);

        HtmlUtil.printHR(out);

        // check whether the connection acquired during two different method invocation, by a thread is same.
        if(result1.equalsIgnoreCase(result2)){
            return true;
        }else{
            return false;
        }
        
    }

    
     private String test1(DataSource ds1, PrintWriter out) throws SystemException {
        String result = null;
        Connection con = null;

        try {
            out.println("<br>");
            out.println("<h4> Starting test </h4>");
            InitialContext ic = new InitialContext();
            out.println("<br>");
            //pool is non-transactional so that sharing won't happen
            con = ds1.getConnection();
            com.sun.appserv.jdbc.DataSource myDS = ((com.sun.appserv.jdbc.DataSource) ds1);
            Connection con_ = myDS.getConnection(con);
            out.println("Thread [ " + Thread.currentThread().getName() +" ] Got connection - con : " + con_);
            result = con_.toString();
            out.println("<br>");

            out.println("<br>");
        } catch (Throwable e) {
            HtmlUtil.printException(e, out);
            result = null;
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
            }
            return result;
        }
    }
}
