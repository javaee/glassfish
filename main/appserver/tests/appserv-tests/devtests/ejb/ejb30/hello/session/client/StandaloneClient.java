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

package com.sun.s1asdev.ejb.ejb30.hello.session.client;

import java.io.*;
import java.util.*;
import com.sun.s1asdev.ejb.ejb30.hello.session.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import javax.naming.*;

public class StandaloneClient {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-hello-sessionstandalone");
        StandaloneClient client = new StandaloneClient(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-hello-sessionstandaloneID");
    }  
    
    public StandaloneClient (String[] args) {
    }
    
    public void doTest() {

        try {

            InitialContext ic = new InitialContext();

            Sful sful1 = (Sful) ic.lookup
                ("com.sun.s1asdev.ejb.ejb30.hello.session.Sful");

            Sful sful2 = (Sful) ic.lookup
                ("com.sun.s1asdev.ejb.ejb30.hello.session.Sful");
            
            Sful sful3 = (Sful) ic.lookup
                ("com.sun.s1asdev.ejb.ejb30.hello.session.Sful#com.sun.s1asdev.ejb.ejb30.hello.session.Sful");

            Sless sless1 = (Sless) ic.lookup
                ("com.sun.s1asdev.ejb.ejb30.hello.session.Sless");

            Sless sless2 = (Sless) ic.lookup
                ("com.sun.s1asdev.ejb.ejb30.hello.session.Sless#com.sun.s1asdev.ejb.ejb30.hello.session.Sless");
            

            System.out.println("invoking stateful");
            sful1.hello();
            sful2.hello();
            sful3.hello();

            if( sful1.equals(sful2) || sful1.equals(sful3) ||
                sful2.equals(sful3) ) {
                throw new Exception("invalid equality checks on different " +
                                    "sful session beans");
            }

            System.out.println("invoking stateless");
            sless1.hello();
            sless2.hello();

            if( !sless1.equals(sless2) ) {
                throw new Exception("invalid equality checks on same " +
                                    "sless session beans");
            }

            System.out.println("test complete");

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }
        
    	return;
    }

}

