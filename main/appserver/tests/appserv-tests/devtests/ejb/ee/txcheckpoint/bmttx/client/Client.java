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

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private BMTSessionHome home;
    private BMTSession sfsb;

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
	checkPersistedFirstTime();
        txAccessCheck();       //access the SFBS
    }

    private void initSFSB() {
        System.out.println("[bmttx] Inside init....");
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/BMTSessionHome");
            home = (BMTSessionHome)PortableRemoteObject.narrow
                (objref, BMTSessionHome.class);
            sfsb = (BMTSession) home.create(_sfsbPrefix);
            System.out.println("[bmttx] Initalization done");
            stat.addStatus("ejbclient initSFSBList", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            //stat.addStatus("ejbclient localEntityGetEJBObject(-)" , stat.PASS);
            System.out.println("[bmttx] Exception in init....");
            e.printStackTrace();
            stat.addStatus("ejbclient initSFSBList", stat.FAIL);
        }
    }

    public void checkPersistedFirstTime() {
        try {
	    int prevActCount =  sfsb.getActivateCount();
	    int nowActCount =  sfsb.getActivateCount();

	    sfsb.startTx();
	    sfsb.getTxName();
	    sfsb.commitTx();

	    sfsb.startTx();
	    sfsb.getTxName();
	    sfsb.commitTx();

	    stat.addStatus("ejbclient checkPersistedFirstTime"
		 + "(" + prevActCount + " : " + nowActCount + " : "
		 + sfsb.getActivateCount() + ")", stat.PASS);
	} catch (Exception ex) {
	    stat.addStatus("ejbclient checkPersistedFirstTime", stat.FAIL);
	}
    }

    public void txAccessCheck() {
        try {
	    int prevActCount = 0;
	    int nowActCount = 0;
	    
	    prevActCount =  sfsb.getActivateCount();
	    sfsb.getName();
	    nowActCount =  sfsb.getActivateCount();
	    stat.addStatus("ejbclient NonTxNonCheckpointedMethod"
		+ " (" + prevActCount + " == " + nowActCount + ")",
		((prevActCount == nowActCount) ? stat.PASS : stat.FAIL));

	    prevActCount =  sfsb.getActivateCount();
	    sfsb.checkpoint();
	    nowActCount =  sfsb.getActivateCount();
	    stat.addStatus("ejbclient NonTxCheckpointedMethod"
		+ " (" + prevActCount + " != " + nowActCount + ")",
		((prevActCount != nowActCount) ? stat.PASS : stat.FAIL));

	    prevActCount = nowActCount;
	    sfsb.startTx();
	    nowActCount =  sfsb.getActivateCount();
	    stat.addStatus("ejbclient utBeginCheck"
		+ " (" + prevActCount + " == " + nowActCount + ")",
		((prevActCount == nowActCount) ? stat.PASS : stat.FAIL));

	    sfsb.incrementCount();
	    nowActCount =  sfsb.getActivateCount();
	    stat.addStatus("ejbclient TxBusinessMethodInsideTx"
		+ " (" + prevActCount + " == " + nowActCount + ")",
		((prevActCount == nowActCount) ? stat.PASS : stat.FAIL));

	    sfsb.getTxName();
	    nowActCount =  sfsb.getActivateCount();
	    stat.addStatus("ejbclient TxMethodInsideTx"
		+ " (" + prevActCount + " == " + nowActCount + ")",
		((prevActCount == nowActCount) ? stat.PASS : stat.FAIL));

	    sfsb.getName();
	    nowActCount =  sfsb.getActivateCount();
	    stat.addStatus("ejbclient NonTxMethodInsideTx"
		+ " (" + prevActCount + " == " + nowActCount + ")",
		((prevActCount == nowActCount) ? stat.PASS : stat.FAIL));

	    sfsb.checkpoint();
	    nowActCount =  sfsb.getActivateCount();
	    stat.addStatus("ejbclient checkpointedMethodInsideTx"
		+ " (" + prevActCount + " == " + nowActCount + ")",
		((prevActCount == nowActCount) ? stat.PASS : stat.FAIL));

	    sfsb.commitTx();
	    nowActCount =  sfsb.getActivateCount();
	    stat.addStatus("ejbclient commitTxCheck"
		+ " (" + prevActCount + " == " + nowActCount + ")",
		(((prevActCount+1) == nowActCount) ? stat.PASS : stat.FAIL));

        } catch (Exception ex) {
	    ex.printStackTrace();
            stat.addStatus("ejbclient txAccessCheck", stat.FAIL);
        }
    }

    private void sleepFor(int seconds) {
	System.out.println("Waiting for " + seconds + " seconds before accessing...");
	for (int i=0; i<seconds; i++) {
	    System.out.println("" + (seconds - i) + " seconds left...");
	    try {
		Thread.currentThread().sleep(1*1000);
	    } catch (Exception ex) {
	    }
	}
    }

} //Client{}
