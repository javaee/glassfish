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

package com.sun.s1asdev.ejb.ejb31.aroundtimeout.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import com.sun.s1asdev.ejb.ejb31.aroundtimeout.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) throws Exception {

        stat.addDescription("ejb-ejb31-aroundtimeout");
        Client client = new Client(args);
        System.out.println("Sleeping for 7 seconds before checking results...");
        Thread.sleep(7000);
        client.doTest();
        stat.printSummary("ejb-ejb31-aroundtimeoutID");
    }  
    
    public Client (String[] args) {
    }
    
    private static @EJB Sless3 sless3;
    private static @EJB Sless4 sless4;
    private static @EJB Sless5 sless5;
    private static @EJB Sless6 sless6;

    public void doTest() {

        try {

            System.out.println("verifying Sless3 tests");

            sless3.noaroundtimeout();
            sless3.verify();

            System.out.println("verifying Sless4 tests");

            sless4.verify();
            try {
                sless4.cbd();
                throw new Exception("Sless4:cbd AroundTimeout called when invoked through interface");
            } catch(EJBException e) {}

            System.out.println("verifying Sless5 tests");

            sless5.verify();
            sless5.abdc();

            System.out.println("verifying Sless6 & SlessEJB7 tests");

            sless6.noaroundtimeout();
            sless6.verify();

            System.out.println("test complete");

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }
        
    	return;
    }

}

