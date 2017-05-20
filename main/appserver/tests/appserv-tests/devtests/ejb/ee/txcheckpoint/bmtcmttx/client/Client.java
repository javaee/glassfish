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

package com.sun.s1asdev.ejb.ee.client;

import java.util.ArrayList;

import java.io.Serializable;
import javax.naming.*;
import javax.jms.*;
import javax.ejb.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.NoSuchObjectException;

import com.sun.s1asdev.ejb.ee.ejb.BMTSessionHome;
import com.sun.s1asdev.ejb.ee.ejb.BMTSession;
import com.sun.s1asdev.ejb.ee.ejb.CMTSessionHome;
import com.sun.s1asdev.ejb.ee.ejb.CMTSession;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private BMTSessionHome home;
    private BMTSession sfsb;
    private CMTSessionHome cmtHome;
    private CMTSession cmtSfsb;

    private String _sfsbPrefix = "SFSB_" + System.currentTimeMillis() + "_";

    public static void main (String[] args) {

        stat.addDescription("bmttx");
        Client client = new Client(args);
        System.out.println("[bmttx] doTest()...");
        client.doTest();
        System.out.println("[bmttx] DONE doTest()...");
        stat.printSummary("bmttx");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {
        initSFSB();     //create SFSBs 
        nonTxAccessCheck();       //access the SFBS
        txAccessCheck();       //access the SFBS
	txBMTCMTAccess();
    }

    private void initSFSB() {
        System.out.println("[bmttx] Inside init....");
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/BMTSessionHome");
            home = (BMTSessionHome)PortableRemoteObject.narrow
                (objref, BMTSessionHome.class);
            sfsb = (BMTSession) home.create(_sfsbPrefix);

            objref = ic.lookup("java:comp/env/ejb/CMTSessionHome");
            cmtHome = (CMTSessionHome)PortableRemoteObject.narrow
                (objref, CMTSessionHome.class);
            cmtSfsb = (CMTSession) cmtHome.create(_sfsbPrefix);
            System.out.println("[bmtcmttx] Initalization done");
            stat.addStatus("bmtcmttx initSFSBList", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            //stat.addStatus("bmtcmttx localEntityGetEJBObject(-)" , stat.PASS);
            System.out.println("[bmtcmttx] Exception in init....");
            e.printStackTrace();
            stat.addStatus("bmtcmttx initSFSBList", stat.FAIL);
        }
    }

    public void nonTxAccessCheck() {
        try {
	    String retrievedName = sfsb.getName();
	    boolean nameOK = _sfsbPrefix.equalsIgnoreCase(retrievedName);
	    boolean actCountOK = (sfsb.getActivateCount() == 0);

            if (nameOK && actCountOK) {
		retrievedName = cmtSfsb.getName();
		nameOK = _sfsbPrefix.equalsIgnoreCase(retrievedName);
		actCountOK = (cmtSfsb.getActivateCount() == 0);
	    }

            if (nameOK && actCountOK) {
                stat.addStatus("bmtcmttx nonTxAccessCheck", stat.PASS);
            } else {
                stat.addStatus("bmtcmttx nonTxAccessCheck", stat.FAIL);
            }

        } catch (Exception ex) {
            stat.addStatus("bmtcmttx nonTxAccessCheck", stat.FAIL);
        }
    }

    public void txAccessCheck() {
        try {
	    String retrievedName = sfsb.getName();
	    sfsb.startTx();
	    sfsb.incrementCount();
	    sfsb.commitTx();

	    boolean actCountOK = (sfsb.getActivateCount() == 1);
            if (actCountOK) {
                stat.addStatus("bmtcmttx BMTtxAccessCheck", stat.PASS);
            } else {
                stat.addStatus("bmtcmttx BMTtxAccessCheck", stat.FAIL);
            }

	    cmtSfsb.incrementCount();
	    int val =  cmtSfsb.getActivateCount();
	    actCountOK = (cmtSfsb.getActivateCount() == 1);
            if (actCountOK) {
                stat.addStatus("bmtcmttx CMTtxAccessCheck", stat.PASS);
            } else {
                stat.addStatus("bmtcmttx CMTtxAccessCheck: " + val, stat.FAIL);
            }

        } catch (Exception ex) {
            stat.addStatus("bmtcmttx txAccessCheck", stat.FAIL);
        }
    }

    private void txBMTCMTAccess() {
        try {
	    boolean passed = true;

	    CMTSession cmt = sfsb.getCMTSession();

	    int prevCount = sfsb.getActivateCount();
	    int prevCMTCount = cmt.getActivateCount();
	    
	    sfsb.startTx();

		sfsb.incrementCount();
		passed = passed && (sfsb.getActivateCount() == prevCount);

		sfsb.accessCMTBean();
		passed = passed && (sfsb.getActivateCount() == prevCount);

		cmt = sfsb.getCMTSession();
		passed = passed && (sfsb.getActivateCount() == prevCount);

	    sfsb.commitTx();

	    passed = passed && (sfsb.getActivateCount() == (prevCount+1));
	    passed = passed && (cmt.getActivateCount() == (prevCMTCount+1));

            if (passed) {
                stat.addStatus("bmtcmttx txBMTCMTAccess", stat.PASS);
            } else {
                stat.addStatus("bmtcmttx txBMTCMTAccess", stat.FAIL);
            }

        } catch (Exception ex) {
            stat.addStatus("bmtcmttx txBMTCMTAccess", stat.FAIL);
        }
    }
    private void sleepFor(int seconds) {
	System.out.println("Waiting for 10 seconds before accessing...");
	for (int i=0; i<seconds; i++) {
	    System.out.println("" + (10 - i) + " seconds left...");
	    try {
		Thread.currentThread().sleep(1*1000);
	    } catch (Exception ex) {
	    }
	}
    }

} //Client{}
