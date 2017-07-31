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

package com.sun.s1asdev.ejb.bmp.txtests.stateless.client;

import java.util.ArrayList;

import java.io.Serializable;
import javax.naming.*;
import javax.jms.*;
import javax.ejb.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.NoSuchObjectException;

import com.sun.s1asdev.ejb.bmp.txtests.stateless.ejb.SLSBHome;
import com.sun.s1asdev.ejb.bmp.txtests.stateless.ejb.SLSB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    // consts
    public static String kTestNotRun    = "TEST NOT RUN";
    public static String kTestPassed    = "TEST PASSED";
    public static String kTestFailed    = "TEST FAILED";
    
    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private SLSBHome home;

    public static void main (String[] args) {

        stat.addDescription("txtests");
        Client client = new Client(args);
        System.out.println("[txtests] doTest()...");
        client.doTest();
        System.out.println("[txtests] DONE doTest()...");
        stat.printSummary("txtests");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {
        initSLSB();       //create MAX_TIMERS

        doRollbackTest();
        doReturnParamTest();
    }

    private void initSLSB() {
        System.out.println("[txtests] Inside init....");
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/SLSBHome");
            this.home = (SLSBHome)PortableRemoteObject.narrow
                (objref, SLSBHome.class);
            System.out.println("[txtests] Initalization done");
        } catch(Exception e) {
            System.out.println("[txtests] Exception in init....");
            e.printStackTrace();
        }
    }

    public void doRollbackTest() {
        try {
            int intVal = (int) System.currentTimeMillis();
            SLSB slsb = (SLSB) home.create();
            boolean retVal = slsb.doRollbackTest(intVal);
            if (retVal) {
                stat.addStatus("txtests doRollbackTest", stat.PASS);
            } else {
                stat.addStatus("txtests doRollbackTest", stat.FAIL);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus("txtests doRollbackTest", stat.FAIL);
        }
    }

    public void doReturnParamTest() {
        try {
            int intVal = (int) System.currentTimeMillis();
            intVal++;
            SLSB slsb = (SLSB) home.create();
            boolean retVal = slsb.doReturnParamTest(intVal);
            if (retVal) {
                stat.addStatus("txtests doReturnParamTest", stat.PASS);
            } else {
                stat.addStatus("txtests doReturnParamTest", stat.FAIL);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus("txtests doReturnParamTest", stat.FAIL);
        }
    }

}
    
