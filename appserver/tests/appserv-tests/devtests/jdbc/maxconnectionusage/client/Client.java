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

package com.sun.s1asdev.jdbc.maxconnectionusage.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.maxconnectionusage.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.maxconnectionusage.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import java.rmi.RemoteException;

public class Client {

    SimpleReporterAdapter stat = new SimpleReporterAdapter();

    public static void main(String[] args)
            throws Exception {

        Client client = new Client();
        client.runTest();
    }

    public void runTest() throws Exception {
        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create();
        stat.addDescription("Max Connection Usage");

        if (simpleBMP.test1(false)) {
            stat.addStatus(" Max Connection Usage -  (local-TxNotSupported): ", stat.PASS);
        } else {
            stat.addStatus(" Max Connection Usage -  (local-TxNotSupported): ", stat.FAIL);
        }

        boolean useXA = false;
        boolean status = connectionSharingTest(simpleBMP, useXA, 21112);
        if (status) {
            stat.addStatus(" Max Connection Usage - (local-Tx-Sharing) : ", stat.PASS);
        } else {
            stat.addStatus(" Max Connection Usage - (local-Tx-Sharing) : ", stat.FAIL);
        }


        if (simpleBMP.test1(true)) {
            stat.addStatus(" Max Connection Usage -  (XA-TxNotSupported) : ", stat.PASS);
        } else {
            stat.addStatus(" Max Connection Usage -  (XA-TxNotSupported) : ", stat.FAIL);
        }

        //Commented as this test is not valid.
        //physical connection (ds.getConnection(conn)) will be different each time.
        
        /*useXA = true;
        status = connectionSharingTest(simpleBMP, useXA, 12221);

        if (status) {
            stat.addStatus(" Max Connection Usage - (XA-Tx-Sharing) : ", stat.PASS);
        } else {
            stat.addStatus(" Max Connection Usage - (XA-Tx-Sharing) : ", stat.FAIL);
        }*/

        stat.printSummary();
    }

    private boolean connectionSharingTest(SimpleBMP simpleBMP, boolean useXA, int value) throws RemoteException {
        String results[] = new String[10];
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                results[i] = simpleBMP.test2(useXA, value);
            } else {
                results[i] = simpleBMP.test3((i / 2) + 1, useXA, value);
            }
        }
        boolean status = true;
        String result = results[0];
        for (int i = 0; i < results.length; i++) {
            if (!results[i].equalsIgnoreCase(result)) {
                System.out.println("Result 0 : " + result);
                System.out.println("Result " + i + " : " + results[i]);
                status = false;
                break;
            }
        }

        String result2 = simpleBMP.test2(useXA, value);

        if (!result2.equalsIgnoreCase(result) && status ) {
            status = true;
        } else {
            System.out.println("Marking status as false during verification");
            System.out.println("is XA : " + useXA);
            System.out.println("Value : " + value);
            System.out.println("Result 1 : " + result);
            System.out.println("Result 2 : " + result2);
            status = false;
        }
        return status;
    }
}
