/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package com.acme;

import org.glassfish.tests.ejb.timertest.SimpleEjb;

import java.util.Map;
import java.util.HashMap;
import javax.ejb.*;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    public static void main(String[] s) {
        appName = s[0];
        System.err.println(".......... Testing module: " + appName);
        stat.addDescription(appName);
        Client t = new Client();
        try {
            t.test(appName, 1);
        } catch (Exception e) {
            stat.addStatus("EJB embedded with timertest 1", stat.FAIL);
            e.printStackTrace();
        }
        System.err.println("------------------------");
        if (s.length == 2 && s[1].equals("false")) {
            System.err.println("-------This part of the test will fail if ran against Full Profile ------------");
            try {
                t.test(appName, 2);
            } catch (Exception e) {
                stat.addStatus("EJB embedded with timertest 2", stat.FAIL);
                e.printStackTrace();
            }
            System.err.println("------------------------");
        } else {
            System.err.println("-------Do not run 2nd time until timer app reload is fixed ------------");
        }
        stat.printSummary(appName + "ID");
        System.exit(0);
    }

    private void test(String module, int id) {

        EJBContainer c = EJBContainer.createEJBContainer();
        // ok now let's look up the EJB...
        Context ic = c.getContext();
        try {
            System.err.println("Looking up EJB...");
            SimpleEjb ejb = (SimpleEjb) ic.lookup("java:global/" + module + "/SimpleEjb");
            System.err.println("Invoking EJB...");
            ejb.createTimer();
            Thread.sleep(8000);
            boolean result = ejb.verifyTimer();
            System.err.println("EJB timer called: " + result);
            if (!result)
                throw new Exception ("EJB timer was NOT called for 1 or 2 timers");

            stat.addStatus("EJB embedded with timertest" + id, stat.PASS);
        } catch (Exception e) {
            stat.addStatus("EJB embedded with timertest" + id, stat.FAIL);
            System.err.println("ERROR calling EJB:");
            e.printStackTrace();
        } finally {
            c.close();
        }
        System.err.println("Done calling EJB");
    }

}
