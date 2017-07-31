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

package client.intrfaces;

import javax.ejb.*;
import javax.naming.*;

import admin.AdminBaseDevTest;
import ejb32.intrfaces.St1;
import ejb32.intrfaces.St2;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import ejb32.intrfaces.St3;
import ejb32.intrfaces.St4;
import ejb32.intrfaces.St5;
import ejb32.intrfaces.St7;

public class Client extends AdminBaseDevTest {

    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

    private void deployFail(String path, String index) {
        System.out.println("Deploying " + path + " with index " + index);
        if (asadmin("deploy", path)) {
            System.out.println("Failed! " + path + " should not be deployed");
            AsadminReturn ar = getLastAsadminReturn();
            String err = ar.outAndErr;
            System.out.println("deployment output: " + err);
            stat.addStatus("ejb32-intrfaces neg" + index + ": ", stat.FAIL);
        } else {
            AsadminReturn ar = getLastAsadminReturn();
            String err = ar.outAndErr;
            System.out.println("deployment output: " + err);
            String expect = "1".equals(index) ? "St1" : "St7";
            if (!err.contains(expect)) {
                stat.addStatus("ejb32-intrfaces neg" + index + ": ", stat.FAIL);
            } else {
                stat.addStatus("ejb32-intrfaces neg" + index + ": ", stat.PASS);
            }
        }
    }

    // in case any failure occurs. cleanup deployed app
    private void undeploy(String app) {
        if (app.endsWith(".jar") || app.endsWith(".ear")) {
            app = app.substring(0, app.lastIndexOf('.'));
        }
        asadmin("undeploy", app);
    }

    public static void main(String args[]) {
        stat.addDescription("ejb32-intrfaces");
        Client c = new Client();
        if ("deploy-fail".equals(args[0])) {
            // negative tests
            c.deployFail(args[1], args[2]);
            c.undeploy(args[1]);
        } else {

            try {
                St1 view1 = (St1) new InitialContext().lookup("java:global/ejb32-intrfacesApp/ejb32-intrfaces-ejb/SingletonBean!ejb32.intrfaces.St1");
                St2 view2 = (St2) new InitialContext().lookup("java:global/ejb32-intrfacesApp/ejb32-intrfaces-ejb/SingletonBean!ejb32.intrfaces.St2");
                boolean pass1 = view1.st1().equals("StflEJB1.st3.StflEJB1.st4.SingletonBean.st1");
                boolean pass2 = view2.st2().equals("StlesEJB2.st7.SingletonBean.st2");
                boolean pass = pass1 && pass2;
                stat.addStatus("ejb32-intrfaces SingletonBean: ", ((pass) ? stat.PASS : stat.FAIL));

            } catch (Exception e) {
                stat.addStatus("ejb32-intrfaces SingletonBean: ", stat.FAIL);
                e.printStackTrace();
            }

            try {
                St5 view5 = (St5) new InitialContext().lookup("java:global/ejb32-intrfacesApp/ejb32-intrfaces-ejb/StflEJB!ejb32.intrfaces.St5");
            } catch (NamingException ne) {
                boolean pass = false;
                if (ne.getCause() instanceof NameNotFoundException) {
                    NameNotFoundException nnfe = (NameNotFoundException) ne.getCause();
                    if (nnfe.getMessage().contains("St5")) {
                        try {
                            St7 view7 = (St7) new InitialContext().lookup("java:global/ejb32-intrfacesApp/ejb32-intrfaces-ejb/StflEJB!ejb32.intrfaces.St7");
                            pass = view7.st7().equals("StlesEJB1.st6.StlesEJB1.st7.StflEJB.st7");
                            stat.addStatus("ejb32-intrfaces StflEJB: ", ((pass) ? stat.PASS : stat.FAIL));
                        } catch (Exception e) {
                            stat.addStatus("ejb32-intrfaces StflEJB: ", stat.FAIL);
                            e.printStackTrace();
                        }
                    }
                }
                if (!pass) {
                    stat.addStatus("ejb32-intrfaces StflEJB: ", stat.FAIL);
                }
            } catch (Exception e) {
                stat.addStatus("ejb32-intrfaces StflEJB: ", stat.FAIL);
                e.printStackTrace();
            }

            try {
                St5 view5 = (St5) new InitialContext().lookup("java:global/ejb32-intrfacesApp/ejb32-intrfaces-ejb/StlesEJB!ejb32.intrfaces.St5");
            } catch (NamingException ne) {
                boolean pass = false;
                if (ne.getCause() instanceof NameNotFoundException) {
                    NameNotFoundException nnfe = (NameNotFoundException) ne.getCause();
                    if (nnfe.getMessage().contains("St5")) {
                        try {
                            St3 view3 = (St3) new InitialContext().lookup("java:global/ejb32-intrfacesApp/ejb32-intrfaces-ejb/StlesEJB!ejb32.intrfaces.St3");
                            St4 view4 = (St4) new InitialContext().lookup("java:global/ejb32-intrfacesApp/ejb32-intrfaces-ejb/StlesEJB!ejb32.intrfaces.St4");
                            boolean pass1 = view3.st3().equals("SingletonBean1.st6.StlesEJB.st3");
                            boolean pass2 = view4.st4().equals("StflEJB2.st4.StflEJB2.st6.StlesEJB.st4");
                            pass = pass1 && pass2;
                            stat.addStatus("ejb32-intrfaces StlesEJB: ", ((pass) ? stat.PASS : stat.FAIL));
                        } catch (Exception e) {
                            stat.addStatus("ejb32-intrfaces StlesEJB: ", stat.FAIL);
                            e.printStackTrace();
                        }
                    }
                }
                if (!pass) {
                    stat.addStatus("ejb32-intrfaces StflEJB: ", stat.FAIL);
                }
            } catch (Exception e) {
                stat.addStatus("ejb32-intrfaces StlesEJB: ", stat.FAIL);
                e.printStackTrace();
            }

        }
        stat.printSummary("ejb32-intrfaces");

    }

    @Override
    protected String getTestDescription() {
        return "Test for EJB3.2 interfaces";
    }
}
