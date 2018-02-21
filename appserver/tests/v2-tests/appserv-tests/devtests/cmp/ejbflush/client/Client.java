/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * Client.java
 *
 * Created on February 21, 2003, 3:20 PM
 */

import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;


/**
 * This class is used to test setting CMP field 'name' to a value
 * that is too large for the column size that it is mapped to.
 * The test is executed with flush after business method (set to true
 * for setNameWithFlush()), and without flush (is not set, i.e. false
 * for seName()).
 * The test is executed for CMP1.1 bean (A1) and CMP2.x bean (A2).
 *
 * @author  mvatkina
 */
public class Client {
    
    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
       
        try {
            System.out.println("START");
	    stat.addDescription("ejbflush");

            Context initial = new InitialContext();

            Object objref = initial.lookup("java:comp/env/ejb/TestFlush");
            test.TestHome thome = (test.TestHome)PortableRemoteObject.narrow(objref, test.TestHome.class);

            test.Test t = thome.create();

            // Run CMP1.1 test without flush. This test will fail
            // at transaction commit.
            try {
                t.testA1();
                System.out.println("A1 FAILED");
            } catch (javax.ejb.CreateException e) {
                System.out.println("A1 FAILED");
            } catch (Exception e) {
                System.out.println("A1 OK");
            }

            // Run CMP2.x test without flush. This test will fail
            // at transaction commit.
            try {
                t.testA2();
                System.out.println("A2 FAILED");
            } catch (javax.ejb.CreateException e) {
                System.out.println("A1 FAILED");
            } catch (Exception e) {
                System.out.println("A2 OK");
            }

            // Run CMP1.1 test with flush. This test should throw 
            // a FlushException.
            try {
                t.testA1WithFlush();
                System.out.println("A1 FAILED");
            } catch (test.FlushException e) {
                System.out.println("A1 OK");
            } catch (Exception e) {  
                System.out.println("A1 FAILED " + e.getMessage());
            }

            // Run CMP1.1 test with flush. This test should throw 
            // a FlushException.
            try {
                t.testA2WithFlush();
                System.out.println("A2 FAILED");
            } catch (test.FlushException e) {
                System.out.println("A2 OK");
            } catch (Exception e) {  
                System.out.println("A2 FAILED " + e.getMessage());
            }

	    stat.addStatus("ejbclient ejbflush", stat.PASS);
            System.out.println("FINISH");

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
	    stat.addStatus("ejbclient ejbflush", stat.PASS);
        }

	  stat.printSummary("ejbflush");
    }
    
}
