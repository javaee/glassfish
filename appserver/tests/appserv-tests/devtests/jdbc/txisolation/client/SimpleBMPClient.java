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

package com.sun.s1asdev.jdbc.txisolation.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.txisolation.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.txisolation.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import java.sql.Connection;

public class SimpleBMPClient {

    public static void main(String[] args)
            throws Exception {

        SimpleReporterAdapter stat = new SimpleReporterAdapter();
        String testSuite = "JDBCTxIsolation ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create();
	stat.addDescription("JDBC TX Isolation tests");
            boolean result = false;
            String testName = null;

        if (args != null && args.length > 0) {
            String param = args[0];

            switch (Integer.parseInt(args[0])) {

                case 1:
                    result = simpleBMP.test1(Connection.TRANSACTION_READ_COMMITTED);
                    testName = "read-committed, new connection";
                    break;
                case 2:
                    result = simpleBMP.test1(Connection.TRANSACTION_READ_UNCOMMITTED);
                    testName = "read-uncommitted, new connection";
                    break;
                case 3:
                    result = simpleBMP.test1(Connection.TRANSACTION_REPEATABLE_READ);
                    testName = "repeatable-read, new connection";
                    break;
                case 4:
                    result = simpleBMP.test1(Connection.TRANSACTION_SERIALIZABLE);
                    testName = "serializable, new connection";
                    break;
                 case 5:
                    simpleBMP.modifyIsolation(Connection.TRANSACTION_SERIALIZABLE);
                    result = simpleBMP.test1(Connection.TRANSACTION_READ_COMMITTED);
                    testName = "read-committed, guaranteed";
                    break;
                case 6:
                    simpleBMP.modifyIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                    result = simpleBMP.test1(Connection.TRANSACTION_READ_UNCOMMITTED);
                    testName = "read-uncommitted, guaranteed";
                    break;
                case 7:
                    simpleBMP.modifyIsolation(Connection.TRANSACTION_READ_COMMITTED);
                    result = simpleBMP.test1(Connection.TRANSACTION_REPEATABLE_READ);
                    testName = "repeatable-read, guaranteed";
                    break;
                case 8:
                    simpleBMP.modifyIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                    result = simpleBMP.test1(Connection.TRANSACTION_SERIALIZABLE);
                    testName = "serializable, guaranteed";
                    break;
            }
            if (result) {
                stat.addStatus(testSuite + testName, stat.PASS);
            } else {
                stat.addStatus(testSuite + testName, stat.FAIL);
            }


        }
        stat.printSummary();
    }
}
