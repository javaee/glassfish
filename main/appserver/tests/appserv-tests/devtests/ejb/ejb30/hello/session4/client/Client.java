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

package com.sun.s1asdev.ejb.ejb30.hello.session4.client;

import java.io.*;
import java.util.*;
import javax.naming.InitialContext;
import javax.ejb.EJB;
import com.sun.s1asdev.ejb.ejb30.hello.session4.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-hello-session4");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-hello-session4ID");
    }  
    
    public Client (String[] args) {
    }

    @EJB(name="ejb/sful")
    private static Sful sful;

    // NOTE : Do not reference Sful2 within annotations
    // so that we ensure that the first time Sful2 is
    // accessed it is as part of the return value of
    // a business method.  This tests that the dynamic
    // interface generation machinery is working properly
    // for the case where a previously unseen remote 3.0
    // interface is somewhere within the return
    // value of a business method.

    // @EJB
    private static Sless sless;

    public void doTest() {

        try {
	    InitialContext ic = new InitialContext();

	    sful = (Sful) ic.lookup("ejb_ejb30_hello_session4_Sful#com.sun.s1asdev.ejb.ejb30.hello.session4.Sful");
            System.out.println("invoking stateful");
            String sfulId = "1";
            sful.setId(sfulId);
            sful.hello();
            sful.sameMethod();

            Sful2 sful_2 = sful.getSful2();
            sful_2.sameMethod();
            String sful_2Id = sful_2.getId();

            System.out.println("Expected id " + sfulId);
            System.out.println("Received id " + sful_2Id);
            if( !sful_2Id.equals(sfulId) ) {
                throw new Exception("sful bean id mismatch " + 
                                    sfulId + " , " + sful_2Id);
            }


            //            System.out.println("invoking stateful2");
            //            sful2.hello2();

            System.out.println("invoking stateless");

	    sless = (Sless) ic.lookup("com.sun.s1asdev.ejb.ejb30.hello.session4.Sless");
            sless.hello();

            System.out.println("test complete");

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }
        
    	return;
    }

}

