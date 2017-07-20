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

package com.sun.s1peqe.mq.cmt.excpt.client;

import javax.jms.*;
import javax.naming.*;
import java.sql.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleMessageClient {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {

        stat.addDescription("This is to test Lifecycle listener JMS lookup ");
        boolean passed = false;
        try {

	    Class.forName(args[0]);
	    String url = args[1];
	    java.sql.Connection con = DriverManager.getConnection(url,args[2],args[3]);
	    ResultSet rs = con.createStatement().executeQuery("select status from lifecycle_test1");
	    int count = 0;
	    while (rs.next()){
	        count = rs.getInt(1);
	    }
            rs.close();
	    con.close();
	    if (count == 0) {
	       throw new Exception("test failed because status : " + 0);
	    }
            passed = true;
        } catch (Throwable e) {
            System.out.println("Exception occurred: " + e.toString());
	    passed = false;
            stat.addStatus("simple lifecycle test1", stat.FAIL);
        } finally {
            if (passed) stat.addStatus("simple lifecycle test1", stat.PASS);
            stat.printSummary("simpleLifecycle");
            System.exit(0);
        } // finally
    } // main
} // class

