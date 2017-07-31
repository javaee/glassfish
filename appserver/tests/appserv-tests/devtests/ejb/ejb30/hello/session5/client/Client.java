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

package com.sun.s1asdev.ejb.ejb30.hello.session5.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import com.sun.s1asdev.ejb.ejb30.hello.session5.Sful2;
import com.sun.s1asdev.ejb.ejb30.hello.session5.Sless2;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-hello-session5");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-hello-session5ID");
    }  
    
    /**
     * This test ensures that we don't assume a remote ejb client has
     * access to all the classes for all the remote business interfaces that
     * an invoked bean has.  SfulEJB and SlessEJB each have two distinct
     * remote business interfaces, but this client only uses Sful2 and
     * Sless2 and does not package the Sful or Sless classes at all. The
     * internal generated RMI-IIOP stubs created at runtime should not
     * cause any classloading errors in the client. 
     */
    public Client (String[] args) {
    }

    @EJB(name="ejb/sful2", mappedName="shouldbeoverriddeninsunejbjar.xml")
    private static Sful2 sful2;

    @EJB(name="ejb/sless2", mappedName="ejb_ejb30_hello_session5_Sless")
    private static Sless2 sless2;

    public void doTest() {

        try {

            System.out.println("invoking stateful2");
            sful2.hello2();

            System.out.println("invoking stateless2");
            sless2.hello2();
            sless2.foo(1);

            System.out.println("test complete");

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }
        
    	return;
    }

}

