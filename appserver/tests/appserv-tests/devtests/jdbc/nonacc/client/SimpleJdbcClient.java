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

package com.sun.s1asdev.jdbc.nonacc;

import javax.naming.*;
import java.sql.*;
import javax.sql.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleJdbcClient {
    
    public static void main( String argv[] ) throws Exception {
        String testSuite = "NonACC ";
        SimpleReporterAdapter stat = new SimpleReporterAdapter();
        stat.addDescription("Test a stand-alone java program that does getConnection");
        Connection con = null;
        Statement stmt = null;

        try {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup(argv[0]);
            con = ds.getConnection();
            System.out.println(" Got connection " + con );

            stmt = con.createStatement();
            stmt.executeQuery("SELECT * FROM NONACC");
            stat.addStatus(testSuite + "test1 ", stat.PASS ); 
        } catch( Exception e) {
            e.printStackTrace();
            stat.addStatus(testSuite + "test1 ",  stat.FAIL );
        } finally {
            if (stmt != null) { try { stmt.close(); }catch( Exception e) {} }
            if (con != null) { try { con.close(); }catch( Exception e) {} }
        }

        stat.printSummary();
    }
}

