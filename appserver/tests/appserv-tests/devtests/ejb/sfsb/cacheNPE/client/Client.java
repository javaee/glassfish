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

package com.sun.s1asdev.ejb.sfsb.cacheNPE.client;

import java.util.ArrayList;

import java.io.Serializable;
import javax.naming.*;
import javax.jms.*;
import javax.ejb.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.NoSuchObjectException;

import com.sun.s1asdev.ejb.sfsb.cacheNPE.ejb.SFSBHome;
import com.sun.s1asdev.ejb.sfsb.cacheNPE.ejb.SFSB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    // consts
    public static String kTestNotRun    = "TEST NOT RUN";
    public static String kTestPassed    = "TEST PASSED";
    public static String kTestFailed    = "TEST FAILED";
    
    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private ArrayList sfsbList = new ArrayList();

    private SFSBHome home;

    public static void main (String[] args) {

        stat.addDescription("cacheNPE");
        Client client = new Client(args);
        System.out.println("[cacheNPEClient] doTest()...");
        client.doTest();
        System.out.println("[cacheNPEClient] DONE doTest()...");
        stat.printSummary("cacheNPE");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {
        initSFSBList(0,4);     //create SFSBs 

        for( int j = 0; j < 400; j++) {
            accessSFSB(0,3,5);
        }

        try {
            java.lang.Thread.sleep( 30 * 1000 );
        } catch (Exception ex ) {
            System.out.println ( "Exception caught : " + ex );
            ex.printStackTrace();
        } 

        removeSFSB(3);
        removeSFSB(2);
        accessSFSB(1,4,5);

        initSFSBList(5,20);     //create SFSBs 

    }

    private void initSFSBList(int i, int MAXSFSBS) {
        System.out.println("[cacheNPEClient] Inside init....");
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/SFSB");
            home = (SFSBHome)PortableRemoteObject.narrow
                (objref, SFSBHome.class);
            for (; i < MAXSFSBS; i++) {

                //Creating these many SFSBs will cause passivation
                SFSB sfsb = (SFSB) home.create("SFSB_"+i);

                sfsbList.add(sfsb);
            }
            System.out.println("[cacheNPE] Initalization done");
            stat.addStatus("ejbclient initSFSBs", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[cacheNPEClient] Exception in init....");
            e.printStackTrace();
            stat.addStatus("ejbclient initSFSBs", stat.FAIL);
        }
    }

    public void accessSFSB(int i, int step, int MAXSFSBS) {
        try {
            boolean passed = true;
            int j=0;

            for (; i < MAXSFSBS; i=i+step) {
                SFSB sfsb = (SFSB) sfsbList.get(i);
                String sfsbName = sfsb.getName();
                
                boolean sessionCtxTest = sfsb.checkSessionContext();
                boolean initialCtxTest = sfsb.checkInitialContext();
                boolean entityHomeTest = sfsb.checkEntityHome();
                boolean entityLocalHomeTest = sfsb.checkEntityLocalHome();
                boolean entityRemoteTest = sfsb.checkEntityRemoteRef();
                boolean entityLocalTest = sfsb.checkEntityLocalRef();
                boolean homeHandleTest = sfsb.checkHomeHandle();
                boolean handleTest = sfsb.checkHandle();
                boolean utTest = sfsb.checkUserTransaction();

                System.out.println("In accessSFSB: for bean -> " + sfsbName 
                    + "; " + sessionCtxTest + "; " + initialCtxTest
                    + "; " + entityHomeTest + "; " + entityLocalHomeTest
                    + "; " + entityRemoteTest + "; " + entityLocalTest
                    + "; " + homeHandleTest + "; " + handleTest 
                    + "; " + utTest);

                passed = sessionCtxTest && initialCtxTest
                    && entityHomeTest && entityLocalHomeTest
                    && entityRemoteTest && entityLocalTest
                    && homeHandleTest && handleTest && utTest;

                if (! passed) {
                    break;
                }
            }
            

            if (passed) {
                stat.addStatus("ejbclient accessSFSBs", stat.PASS);
            } else {
                stat.addStatus("ejbclient accessSFSBs", stat.FAIL);
            }
        } catch (Exception ex) {
            stat.addStatus("ejbclient accessSFSB", stat.FAIL);

        }
    }

    public void removeSFSB(int i) {
        SFSB sfsb = (SFSB) sfsbList.get(i);
        try {
            String sfsbName = sfsb.getName();
            System.out.println("In removeSFSB for bean=" + sfsbName);
            sfsb.remove();
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus("ejbclient removeSFSBs", stat.FAIL);
            return;
        } 

        stat.addStatus("ejbclient removeSFSBs", stat.PASS);

    }

} //Client{}
