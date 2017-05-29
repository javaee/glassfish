/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.acme;

import org.glassfish.tests.ejb.sample.SimpleEjb;

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
        stat.addDescription(appName);
        Client t = new Client();
        try {
            t.test();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            t.testError();
        } catch (Exception e) {
            e.printStackTrace();
        }

        stat.printSummary(appName + "ID");
    }

    private void testError() {

        Map<String, Object> p = new HashMap<String, Object>();
        p.put(EJBContainer.MODULES, new String[] {"sample", "foo", "bar"});

        System.err.println("-----------------------");
        System.err.println("-----------------------");
        try {
            System.err.println("===> Requesting wrong set of modules....");
            EJBContainer c = EJBContainer.createEJBContainer(p);
            System.err.println("==> ERROR - Did NOT get an exception");
            stat.addStatus("create container with errors in MODULES", stat.FAIL);
        } catch (EJBException e) {
            String msg = e.getMessage();
            System.err.println("==> Caught expected: " + msg);
            stat.addStatus("create container with errors in MODULES", stat.PASS);
        }
        System.err.println("-----------------------");
        System.err.println("-----------------------");

        p = new HashMap<String, Object>();
        p.put(EJBContainer.PROVIDER, "foo");
        System.err.println("-----------------------");
        System.err.println("-----------------------");
        try {
            System.err.println("==> Creating container with a wrong provider...");
            EJBContainer c1 = EJBContainer.createEJBContainer(p);
            if (c1 != null) {
                stat.addStatus("create container with a wrong provider", stat.FAIL);
                System.err.println("==> ERROR: Created container with a wrong provider...");
            } else {
                stat.addStatus("create container with a wrong provider", stat.PASS);
            }
        } catch (EJBException e) { 
            System.err.println("==> Caught expected: " + e.getMessage());
            stat.addStatus("create container with a wrong provider", stat.PASS);
        }
        System.err.println("-----------------------");
        System.err.println("-----------------------");

    }

    private void test() {

        Map<String, Object> p = new HashMap<String, Object>();
        p.put(EJBContainer.MODULES, "sample");
        System.err.println("-----------------------");
        System.err.println("-----------------------");

        EJBContainer c = EJBContainer.createEJBContainer(p);
        // ok now let's look up the EJB...
        Context ic = c.getContext();
        try {
            System.err.println("Looking up EJB...");
            SimpleEjb ejb = (SimpleEjb) ic.lookup("java:global/sample/SimpleEjb");
            System.err.println("Invoking EJB...");
            System.err.println("EJB said: " + ejb.saySomething());
            System.err.println("JPA call returned: " + ejb.testJPA());

            stat.addStatus("EJB embedded with JPA", stat.PASS);
        } catch (Exception e) {
            stat.addStatus("EJB embedded with JPA", stat.FAIL);
            System.err.println("==> ERROR calling EJB:");
            e.printStackTrace();
        }
        System.err.println("Done calling EJB");
        System.err.println("-----------------------");
        System.err.println("-----------------------");

        try {
            System.err.println("==> Creating another container without closing...");
            EJBContainer c0 = EJBContainer.createEJBContainer();
            if (c0 != null) {
                stat.addStatus("create container without closing the current", stat.FAIL);
                System.err.println("==> ERROR: Created another container without closing the current...");
            }
        } catch (EJBException e) { 
            System.err.println("==> Caught expected: " + e.getMessage());
            stat.addStatus("create container without closing the current", stat.PASS);
        }
        System.err.println("-----------------------");
        System.err.println("-----------------------");

        System.err.println("==> Closing container ...");
        try {
            c.close();
            stat.addStatus("EJB embedded close container", stat.PASS);
        } catch (Exception e) {
            stat.addStatus("EJB embedded close container", stat.FAIL);
            System.err.println("==> ERROR Closing container:");
            e.printStackTrace();
        }
        System.err.println("==> Done Closing container");
        System.err.println("-----------------------");
        System.err.println("-----------------------");

        System.err.println("==> Creating container after closing the previous...");
        try {
            c = EJBContainer.createEJBContainer(p);
            c.close();
            stat.addStatus("EJB embedded create 2nd container", stat.PASS);
        } catch (Exception e) {
            stat.addStatus("EJB embedded create 2nd container", stat.FAIL);
            System.err.println("==> ERROR in the 2nd container:");
            e.printStackTrace();
        }

        System.err.println("..........FINISHED Embedded test");
        System.err.println("-----------------------");
        System.err.println("-----------------------");
    }
}
