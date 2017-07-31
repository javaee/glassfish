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

package com.sun.s1asdev.ejb.ejb30.interceptors.bindings.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import com.sun.s1asdev.ejb.ejb30.interceptors.bindings.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-interceptors-bindings");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-interceptors-bindingsID");
    }  
    
    public Client (String[] args) {
    }
    
    private static @EJB Sful sful;
    private static @EJB Sful2 sful2;
    private static @EJB Sless3 sless3;
    private static @EJB Sless4 sless4;
    private static @EJB Sless5 sless5;
    private static @EJB Sless6 sless6;

    public void doTest() {

        try {

            System.out.println("running Sful tests");
            sful.cef();
            sful.cefa();
            sful.cd();
            sful.ab();
            sful.abcd();
            sful.abef();
            sful.ef();
            sful.cdef();
            sful.abcdef();
            sful.acbdfe();
            sful.nothing();

            System.out.println("running Sful2 tests");

            sful2.abef(1);
            sful2.cd();
            sful2.ef();
            sful2.cdef();
            sful2.nothing();

            System.out.println("running Sless3 tests");

            sless3.dc();
            sless3.ba();
            sless3.dcba();
            sless3.baef();
            sless3.ef();
            sless3.dcf();
            sless3.dcef();
            sless3.nothing();
            sless3.dcbaef();
            sless3.abcdef();

            System.out.println("running Sless4 tests");

            sless4.abef(1);
            sless4.cbd();
            sless4.ef();
            sless4.cbdef();
            sless4.nothing();

            System.out.println("running Sless5 tests");

            sless5.abdc();
            sless5.dcfe();
            sless5.nothing();

            System.out.println("running Sless6 tests");

            sless6.ag();
            sless6.ag(1);
            
            sless6.bg();
            sless6.bg(1);
            
            sless6.cg();
            sless6.cg(1);
            sless6.cg("foo", 1.0);

            sless6.dg();
            sless6.dg(1);

            sless6.eg();
            sless6.eg(1);


            System.out.println("test complete");

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }
        
    	return;
    }

}

