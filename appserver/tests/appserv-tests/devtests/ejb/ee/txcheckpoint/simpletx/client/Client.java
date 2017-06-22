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

package com.sun.s1asdev.ejb.ee.txcheckpoint.simpletx.client;

import java.util.ArrayList;

import java.io.Serializable;
import javax.naming.*;
import javax.jms.*;
import javax.ejb.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.NoSuchObjectException;

import com.sun.s1asdev.ejb.ee.txcheckpoint.simpletx.ejb.SFSBHome;
import com.sun.s1asdev.ejb.ee.txcheckpoint.simpletx.ejb.SFSB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private SFSBHome home;
    private SFSB sfsb;

    private String _sfsbPrefix = "SFSB_" + System.currentTimeMillis() + "_";

    public static void main (String[] args) {

        stat.addDescription("simpletx");
        Client client = new Client(args);
        System.out.println("[simpletxClient] doTest()...");
        client.doTest();
        System.out.println("[simpletxClient] DONE doTest()...");
        stat.printSummary("simpletx");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {
        initSFSBList();     //create SFSBs 
        cmtNonTxAccess();       //access the SFBS
        cmtTxAccess();       //access the SFBS
        cmtNonTxAccess(sfsb);       //access the SFBS
	removeTest();
    }

    private void initSFSBList() {
        System.out.println("[simpletxClient] Inside init....");
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/SFSBHome");
            home = (SFSBHome)PortableRemoteObject.narrow
                (objref, SFSBHome.class);
            sfsb = (SFSB) home.create(_sfsbPrefix);
            System.out.println("[simpletx] Initalization done");
            stat.addStatus("ejbclient initSFSBList", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            //stat.addStatus("ejbclient localEntityGetEJBObject(-)" , stat.PASS);
            System.out.println("[simpletxClient] Exception in init....");
            e.printStackTrace();
            stat.addStatus("ejbclient initSFSBList", stat.FAIL);
        }
    }

    public void cmtTxAccess() {
        try {
	    int prevCount = sfsb.getActivateCount();
	    String retrievedName = sfsb.getTxName();
	    boolean nameOK = _sfsbPrefix.equalsIgnoreCase(retrievedName);
	    int nowCount = sfsb.getActivateCount();
	    boolean actCountOK = (prevCount+1 == nowCount);
	    String msg = "(" + prevCount + "+1 == " + nowCount + ")";
            if (nameOK && actCountOK) {
                stat.addStatus("ejbclient cmtTxAccess " + msg, stat.PASS);
            } else {
                stat.addStatus("ejbclient cmtTxAccess " + msg, stat.FAIL);
            }

        } catch (Exception ex) {
            stat.addStatus("ejbclient accessSFSB", stat.FAIL);
        }
    }

    public void cmtNonTxAccess() {
        try {
	    int prevCount = sfsb.getActivateCount();
	    String retrievedName = sfsb.getName();
	    boolean nameOK = _sfsbPrefix.equalsIgnoreCase(retrievedName);
	    int nowCount = sfsb.getActivateCount();
	    boolean actCountOK = (prevCount == nowCount);
	    String msg = "(" + prevCount + " == " + nowCount + ")";
            if (nameOK && actCountOK) {
                stat.addStatus("ejbclient cmtNonTxAccess " + msg, stat.PASS);
            } else {
                stat.addStatus("ejbclient cmtNonTxAccess " + msg, stat.FAIL);
            }

        } catch (Exception ex) {
            stat.addStatus("ejbclient accessSFSB", stat.FAIL);
        }
    }

    public void cmtNonTxAccess(SFSB sfsb) {
        for (int i=0; i<200; i++) {
	try {
	    int prevCount = sfsb.getActivateCount();
	    String retrievedName = sfsb.getName();
	    boolean nameOK = _sfsbPrefix.equalsIgnoreCase(retrievedName);
	    int nowCount = sfsb.getActivateCount();
	    boolean actCountOK = (prevCount == nowCount);
	    String msg = "(" + prevCount + " == " + nowCount + ")";
            if (nameOK && actCountOK) {
                stat.addStatus("ejbclient cmtNonTxAccess_2 " + msg, stat.PASS);
            } else {
		stat.addStatus("ejbclient cmtNonTxAccess_2", stat.FAIL);
            }
	    System.out.println("[" + i + "] cmtNonTxAccess_2 status: " + (nameOK && actCountOK));
        } catch (Exception ex) {
            stat.addStatus("ejbclient cmtNonTxAccess_2", stat.FAIL);
        }
	sleepFor(1, false);
	}
    }

    public void removeTest() {
	SFSB sfsb = null;
	boolean passed = false;
	try {
	    String myName = "_2_" + _sfsbPrefix + "_2_";
	    sfsb = (SFSB) home.create(myName);
	    String retrievedName = sfsb.getTxName();
	    boolean nameOK = myName.equalsIgnoreCase(retrievedName);
	    sfsb.remove();
	    try {
		sfsb.getTxName();
		passed = false;	    //Expecting an exception
	    } catch (Exception ex) {
		passed = true;
	    }
            if (passed) {
                stat.addStatus("ejbclient removeTest_1 ", stat.PASS);
            } else {
		stat.addStatus("ejbclient removeTest_1", stat.FAIL);
            }

	    passed = false;
	    myName = "_4_" + _sfsbPrefix + "_4_";
	    sfsb = (SFSB) home.create(myName);
	    retrievedName = sfsb.getName();
	    nameOK = myName.equalsIgnoreCase(retrievedName);
	    sfsb.remove();
	    try {
		sfsb.getName();
		passed = false;	    //Expecting an exception
	    } catch (Exception ex) {
		passed = true;
	    }
            if (passed) {
                stat.addStatus("ejbclient removeTest_2 ", stat.PASS);
            } else {
		stat.addStatus("ejbclient removeTest_2", stat.FAIL);
            }

        } catch (Exception ex) {
            stat.addStatus("ejbclient removeTest", stat.FAIL);
        }
    }

    private void sleepFor(int seconds) {
	sleepFor(seconds, true);
    }

    private void sleepFor(int seconds, boolean verbose) {
	if (verbose) {
	    System.out.println("Waiting for " + seconds + " before accessing...");
	}
	for (int i=0; i<seconds; i++) {
	    if (verbose) {
		System.out.println("" + (seconds - i) + " seconds left...");
	    }
	    try {
		Thread.currentThread().sleep(1*1000);
	    } catch (Exception ex) {
	    }
	}
    }

} //Client{}
