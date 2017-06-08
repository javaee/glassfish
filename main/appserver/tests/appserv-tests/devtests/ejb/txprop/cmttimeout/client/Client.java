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

package com.sun.s1asdev.ejb.txprop.cmttimeout.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;

import com.sun.s1asdev.ejb.slsb.SimpleSLSBHome;
import com.sun.s1asdev.ejb.slsb.SimpleSLSB;
import com.sun.s1asdev.ejb.slsb.SimpleSLSBBean;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-txprop-cmttimeout");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-txprop-cmttimeout");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {

        try {
            Context ic = new InitialContext();
                
            System.out.println("Looking up ejb ref ");
            // create EJB using factory from container 
            Object objref = ic.lookup("java:comp/env/ejb/SimpleSLSBHome");
            System.out.println("objref = " + objref);
                
            SimpleSLSBHome  home = (SimpleSLSBHome)PortableRemoteObject.narrow
                (objref, SimpleSLSBHome.class);
                                                                     
            System.err.println("Narrowed home!!");
                
                
            SimpleSLSB f = home.create();
            System.err.println("Got the EJB!!");

            doTest1(f);
            doTest2(f);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("ejbclient main" , stat.FAIL);
        }
    }
        
    private void doTest1(SimpleSLSB ref) {
        try {
            // invoke method on the EJB
            System.out.println("invoking ejb");
            boolean result = ref.doSomething(8);

            System.out.println("successfully invoked ejb");
            stat.addStatus("ejbclient test1",
                    (result ? stat.PASS : stat.FAIL));
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("ejbclient test1" , stat.FAIL);
        }
    }

    private void doTest2(SimpleSLSB ref) {
        try {
            System.out.println("invoking ejb");
            boolean result = ref.doSomethingAndRollback();

            System.out.println("successfully invoked ejb");
            stat.addStatus("ejbclient test2",
                    (result ? stat.PASS : stat.FAIL));
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("ejbclient test2" , stat.FAIL);
        }
    }

}

