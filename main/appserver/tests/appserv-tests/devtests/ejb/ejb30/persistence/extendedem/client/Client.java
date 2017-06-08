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

package com.sun.s1asdev.ejb.ejb30.persistence.extendedem.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import com.sun.s1asdev.ejb.ejb30.persistence.extendedem.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String personName;

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-persistence-extendedem");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-persistence-extendedemID");
    }

    public Client (String[] args) {

        personName = "duke";

        if( args.length > 0 ) {
            personName = args[0];
        }

    }

    private static @EJB Sful sful;

    public void doTest() {

        try {

            System.out.println("I am in client");
            System.out.println("calling createPerson(" + personName + ")");
            sful.createPerson(personName);
            System.out.println("created ");

            System.out.println("calling findPerson(" + personName + ")");
            Person p = sful.findPerson();
            boolean statusFindPerson = p != null;
            System.out.println("found " + p);


            System.out.println("calling nonTxFindPerson(" + personName + ")");
            boolean statusNonTxFindPerson = (sful.nonTxFindPerson() != null);
            System.out.println("found " + p);

            System.out.println("calling nonTxFindPerson(" + personName + ")");
            boolean statusNonTxFindPerson2 = (sful.nonTxFindPerson() != null);
            System.out.println("found " + p);

            System.out.println("removing Person(" + personName + ")");
            boolean statusRemovePerson = sful.removePerson();
            System.out.println("removed Person(" + personName + ")");

            System.out.println("refreshing Person(" + personName + ")");
            boolean statusRefreshPerson = sful.refreshAndFindPerson();
            System.out.println("refreshed Person( and not found....");

            stat.addStatus("local statusFindPerson", (statusFindPerson) ? stat.PASS : stat.FAIL);
            stat.addStatus("local statusNonTxFindPerson", (statusNonTxFindPerson) ? stat.PASS : stat.FAIL);
            stat.addStatus("local statusNonTxFindPerson2", (statusNonTxFindPerson2) ? stat.PASS : stat.FAIL);
            stat.addStatus("local statusRemovePerson", (statusRemovePerson) ? stat.PASS : stat.FAIL);
			stat.addStatus("local statusRefreshPerson", (statusRefreshPerson) ? stat.FAIL : stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }

    	return;
    }


}

