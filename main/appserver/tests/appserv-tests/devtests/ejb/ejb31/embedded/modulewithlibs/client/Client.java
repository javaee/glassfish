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
        stat.printSummary(appName + "ID");
    }

    private void test() {

        Map<String, Object> p = new HashMap<String, Object>();
        p.put(EJBContainer.MODULES, "sample");

        EJBContainer c = EJBContainer.createEJBContainer(p);
        // ok now let's look up the EJB...
        Context ic = c.getContext();
        try {
            System.out.println("Looking up EJB...");
            SimpleEjb ejb = (SimpleEjb) ic.lookup("java:global/sample/SimpleEjb");
            System.out.println("Invoking EJB...");
            System.out.println("EJB said: " + ejb.saySomething());
            System.out.println("JPA call returned: " + ejb.testJPA());

            stat.addStatus("EJB embedded with JPA", stat.PASS);
        } catch (Exception e) {
            stat.addStatus("EJB embedded with JPA", stat.FAIL);
            System.out.println("ERROR calling EJB:");
            e.printStackTrace();
        }
        System.out.println("Done calling EJB");

        try {
            System.out.println("Creating another container without closing...");
            EJBContainer c0 = EJBContainer.createEJBContainer();
            if (c0 != null) {
                stat.addStatus("EJB embedded create container", stat.FAIL);
                System.out.println("Created another container without closing the current...");
            }
        } catch (EJBException e) { 
            System.out.println("Caught expected: " + e.getMessage());
            stat.addStatus("EJB embedded create container", stat.PASS);
        }

        System.out.println("Closing container ...");
        try {
            c.close();
            stat.addStatus("EJB embedded close container", stat.PASS);
        } catch (Exception e) {
            stat.addStatus("EJB embedded close container", stat.FAIL);
            System.out.println("ERROR Closing container:");
            e.printStackTrace();
        }
        System.out.println("Done Closing container");

        System.out.println("Creating container after closing the previous...");
        try {
            c = EJBContainer.createEJBContainer(p);
            c.close();
            stat.addStatus("EJB embedded create new container", stat.PASS);
        } catch (Exception e) {
            stat.addStatus("EJB embedded create new container", stat.FAIL);
            System.out.println("ERROR in the 2nd container:");
            e.printStackTrace();
        }

        System.out.println("..........FINISHED Embedded test");
    }
}
