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

package com.sun.s1asdev.ejb.ee.methodcheckpoint.multi.client;

import java.util.ArrayList;

import java.io.Serializable;
import javax.naming.*;
import javax.jms.*;
import javax.ejb.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.NoSuchObjectException;

import com.sun.s1asdev.ejb.ee.ejb.SFSBHome;
import com.sun.s1asdev.ejb.ee.ejb.SFSB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static final int INCREMENT_VAL = 60000;

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private SFSBHome home;
    private SFSB sfsb;

    private String accountName = "Account[" + System.currentTimeMillis() + "]";

    public static void main (String[] args) {

        stat.addDescription("multi");
        Client client = new Client(args);
        System.out.println("[multiClient] doTest()...");
        client.doTest();
        System.out.println("[multiClient] DONE doTest()...");
        stat.printSummary("multi");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {
        createSFSB();     //create SFSBs 
        initialStateTest();       //access the SFBS
	nonTxNonCheckpointTest();
	nonTxCheckpointTest();
    }

    private void createSFSB() {
	String testCaseName = "ee.multiClient createSFSB ";
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/SFSBHome");
            home = (SFSBHome)PortableRemoteObject.narrow
                (objref, SFSBHome.class);
            sfsb = (SFSB) home.create(accountName, 4000);
            System.out.println("[multi] Initalization done");
	    stat.addStatus(testCaseName, stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            //stat.addStatus("ejbclient localEntityGetEJBObject(-)" , stat.PASS);
            System.out.println("[multiClient] Exception in init....");
            e.printStackTrace();
	    stat.addStatus(testCaseName, stat.FAIL);
        }
    }

    public void initialStateTest() {
	String testCaseName = "ee.multiClient initialStateTest ";
        try {
	    boolean nameOK = true;
	    nameOK = accountName.equals(sfsb.getAccountHolderName());
	    int preBalance = sfsb.getBalance();
	    int preCheckpointedBalance = sfsb.getCheckpointedBalance();
	    boolean balanceOK = (preBalance != preCheckpointedBalance);
	    if (!balanceOK) {
		System.out.println(testCaseName + " failing because: "
			+ preBalance + " == " + preCheckpointedBalance
			+ " failed");
	    }
	    stat.addStatus(testCaseName,
		    ((nameOK && balanceOK) ? stat.PASS : stat.FAIL));
        } catch (Exception ex) {
	    stat.addStatus(testCaseName, stat.FAIL);
        }
    }

    public void nonTxNonCheckpointTest() {
	String testCaseName = "ee.multiClient nonTxNonCheckpointTest ";
        try {
	    int preBalance = sfsb.getBalance();
	    int preCheckpointedBalance = sfsb.getCheckpointedBalance();
	    sfsb.incrementBalance(INCREMENT_VAL);
	    sfsb.nonTxNonCheckpointedMethod();
	    int postBalance = sfsb.getBalance();
	    int postCheckpointedBalance = sfsb.getCheckpointedBalance();

	    boolean ok = (preBalance+INCREMENT_VAL== postBalance)
		&& (preCheckpointedBalance == postCheckpointedBalance);
	    if (!ok) {
		System.out.println(testCaseName + " failing because: "
		    + "(" + preBalance + " + " + INCREMENT_VAL
		    + " == " + postBalance + ")"
		    + " && (" + preCheckpointedBalance + " == "
		    + postCheckpointedBalance + ")"
		    + " failed");
	    }
	    stat.addStatus(testCaseName, (ok ? stat.PASS : stat.FAIL));
        } catch (Exception ex) {
	    ex.printStackTrace();
	    stat.addStatus(testCaseName, stat.FAIL);
        }
    }

    public void nonTxCheckpointTest() {
	String testCaseName = "ee.multiClient nonTxCheckpointTest ";
        try {
	    int preBalance = sfsb.getBalance();
	    int preCheckpointedBalance = sfsb.getCheckpointedBalance();
	    sfsb.incrementBalance(INCREMENT_VAL);
	    sfsb.nonTxCheckpointedMethod();
	    int postBalance = sfsb.getBalance();
	    int postCheckpointedBalance = sfsb.getCheckpointedBalance();

	    boolean ok = (preBalance+INCREMENT_VAL == postBalance)
		&& (preCheckpointedBalance != postCheckpointedBalance)
		&& (postBalance == postCheckpointedBalance);
	    stat.addStatus(testCaseName, (ok ? stat.PASS : stat.FAIL));
        } catch (Exception ex) {
	    ex.printStackTrace();
	    stat.addStatus(testCaseName, stat.FAIL);
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
