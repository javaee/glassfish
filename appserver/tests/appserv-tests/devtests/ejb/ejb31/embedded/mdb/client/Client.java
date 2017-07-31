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

package com.acme;

import org.glassfish.tests.ejb.mdb.SimpleEjb;

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
        System.out.println(".......... Testing module: " + appName);
        stat.addDescription(appName);
        Client t = new Client();
        try {
            t.test(appName);
            stat.addStatus("EJB embedded with MDB", stat.PASS);
        } catch (Exception e) {
            e.printStackTrace();
            stat.addStatus("EJB embedded with MDB", stat.FAIL);
        }
        stat.printSummary(appName + "ID");
        System.exit(0);

    }

    private void test(String module) throws Exception {

        EJBContainer c = null;
        try {
            c = EJBContainer.createEJBContainer();
            Context ic = c.getContext();
            SimpleEjb ejb = (SimpleEjb) ic.lookup("java:global/" + module + "/SimpleEjb");
            System.out.println("Invoking EJB...");
            String result = ejb.saySomething();
            System.out.println("EJB said: " + result);

            System.out.println("Waiting for ack...");
            Thread.sleep(3000);
            boolean ack = ejb.getAck();
            System.out.println("Ack: " + ack);
            if (!ack)
                throw new EJBException("MDB wasn't called!!!");

        } finally {
            if (c!=null) c.close();
        }
        System.out.println("Done calling EJB");
    }

}
