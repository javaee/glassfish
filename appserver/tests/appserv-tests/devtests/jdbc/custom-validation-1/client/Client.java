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

package com.sun.s1asdev.jdbc.customval.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.customval.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.customval.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    public static void main(String[] args)
            throws Exception {

        SimpleReporterAdapter stat = new
                SimpleReporterAdapter();
        String testSuite = "CustomValidation-1 ";
        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome convalBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP convalBMP = convalBMPHome.create();
	stat.addDescription("Custom Validation Test using a custom validator ");

        if (args != null && args.length > 0) {
            String param = args[0];

            switch (Integer.parseInt(param)) {
                case 1: {
                    if (convalBMP.test1()) {
                        stat.addStatus(testSuite + "test-1 ", stat.PASS);
                    } else {
                        stat.addStatus(testSuite + "test-1 ", stat.FAIL);
                    }
                    break;
                }
                case 3: {
                    if (convalBMP.test1()) {
                        stat.addStatus(testSuite + "test-3 ", stat.PASS);
                        System.out.println("test-3 returned true as validation is enabled ");
                    } else {
                        stat.addStatus(testSuite + "test-3 ", stat.FAIL);
                    }
                    break;
                }
                case 4: {
                    if (convalBMP.test1()) {
                        stat.addStatus(testSuite + "test-4 ", stat.PASS);
                        System.out.println("test-4 returned true as validation is enabled ");
                    } else {
                        stat.addStatus(testSuite + "test-4 ", stat.FAIL);
                    }
                    break;
                }

                case 2: {
                    try {
                        if(convalBMP.test1()){
                            stat.addStatus(testSuite + "test-2 ", stat.FAIL);
                        }else{
                            stat.addStatus(testSuite + "test-2 ", stat.PASS);
                            System.out.println("test-2 returned false as validation is not enabled ");
                        }
                    }
                    catch (Exception e) {
                        stat.addStatus(testSuite + "test1 ", stat.PASS);
                    }
                    break;
                }
            }
            stat.printSummary();
        }
    }
}
