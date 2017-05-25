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

package com.sun.s1asdev.ejb.ejb30.interceptors.session.client;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.ejb.EJB;
import com.sun.s1asdev.ejb.ejb30.interceptors.session.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-interceptors-session");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-interceptors-sessionID");
    }  
    
    public Client (String[] args) {
    }
    
    private static @EJB Sful sful;

    private static @EJB Sful sful0;
    private static @EJB Sful sful1;
    private static @EJB Sful sful2;
    private static @EJB Sful sful3;
    private static @EJB Sful sful4;
    private static @EJB Sful sful5;
    private static @EJB Sful sful6;
    private static @EJB Sful sful7;
    private static @EJB Sful sful8;
    private static @EJB Sful sful9;

    public void doTest() {
	doTest1();
	doTest2();
	doTest3();
	doTest4();
	doTest5();
	doTest6();

	doTest7_8();

        doTest9();
        doTest10();
    }

    private void doTest1() {
        try {

            System.out.println("invoking stateful");
            sful.hello();
	    System.out.println("+++++++++++++++++++++++++++++++++++++++");
	    System.out.println("+++++ InterceptorCallCount: " + sful.getCount());
	    System.out.println("+++++++++++++++++++++++++++++++++++++++");
            stat.addStatus("local test1" , stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local test1" , stat.FAIL);
        }
    }

    private void doTest2() {
        try {
            sful.throwAppException("XYZ");
            stat.addStatus("local test2" , stat.FAIL);
        } catch (AppException appEx) {
		System.out.println("Got expected AppException: " + appEx);
        	stat.addStatus("local test2" , stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local test2" , stat.FAIL);
        }
    }

    private void doTest3() {
        try {
            String result = sful.computeMid(4, 10);
	    System.out.println("[Test3]: Got: " + result);
            stat.addStatus("local test3" , stat.PASS);
        } catch (SwapArgumentsException swapEx) {
	    System.out.println("Got unexpected Exception: " + swapEx);
            stat.addStatus("local test3" , stat.FAIL);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local test3" , stat.FAIL);
        }
    }

    private void doTest4() {
        try {
            String result = sful.computeMid(23, 10);
	    System.out.println("[Test4]: Got: " + result
			    + " INSTEAD of SwapArgumentsException");
            stat.addStatus("local test4" , stat.FAIL);
        } catch (SwapArgumentsException swapEx) {
	    System.out.println("Got expected Exception: " + swapEx);
            stat.addStatus("local test4" , stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local test4" , stat.FAIL);
        }
    }

    private void doTest5() {
        try {
            String result = sful.callDummy();
	    System.out.println("[Test5]: Got: " + result
			    + " INSTEAD of CallBlockedException");
            stat.addStatus("local test5" , stat.FAIL);
        } catch (CallBlockedException callBlkEx) {
	    System.out.println("Got expected Exception: " + callBlkEx);
            stat.addStatus("local test5" , stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local test5" , stat.FAIL);
        }
    }

    private void doTest6() {
        try {
            sful.eatException();
            stat.addStatus("local test6" , stat.PASS);
        } catch(Exception e) {
	    System.out.println("Got unexpected Exception: " + e);
            e.printStackTrace();
            stat.addStatus("local test6" , stat.FAIL);
        }
    }

    private void doTest7_8() {
        try {
	    int sz = 20;
        Context ctx = new InitialContext();
	    sful.resetLifecycleCallbackCounters();
	    Sful[] sfuls = new Sful[sz];
	    for (int i=0; i<sz; i++) {
    		sfuls[i] = (Sful) ctx.lookup("com.sun.s1asdev.ejb.ejb30.interceptors.session.Sful");
    		int prevIndex = (i ==0) ? 0 : i-1;
    		System.out.println("Created sful["+i+"]: " + sfuls[i] + " ==> "
    		        + sfuls[prevIndex].equals(sfuls[i]));
		    sfuls[i].setID(i);
            sfuls[i].getCount();
	    }
	    sleepFor(10);
	    for (int i=0; i<sz; i++) {
	        sfuls[i].getCount();
	    }
	    int passivationCount = sful.getPrePassivateCallbackCount();
	    int activationCount = sful.getPostActivateCallbackCount();
	    
        boolean status = (passivationCount > 0) && (activationCount> 0);
        System.out.println("passivation: " + passivationCount + "; "
		    + "activation: " + activationCount);
        stat.addStatus("local test7" , 
		    (status == true) ? stat.PASS : stat.FAIL);
        
        boolean stateRestored = true;
        for (int i=0; i<sz; i++) {
            boolean ok = sfuls[i].isStateRestored();
            System.out.println("stateRestored[" + i + "]:" + ok);
            stateRestored = ok && status;
        }
        stat.addStatus("local test8" , 
                (stateRestored == true) ? stat.PASS : stat.FAIL);
        
        } catch(Exception e) {
            System.out.println("Got unexpected Exception: " + e);
                e.printStackTrace();
                stat.addStatus("local test7" , stat.FAIL);
        }
    }

    private void doTest9() {
        boolean checkSetParamsStatus = true;
        Map<String, Boolean> map = sful.checkSetParams();
        for (String testName : map.keySet()) {
            System.out.println("Test[" + testName + "]: " + map.get(testName));
            checkSetParamsStatus = checkSetParamsStatus && map.get(testName);
        }
        stat.addStatus("local test9" , 
                (checkSetParamsStatus == true) ? stat.PASS : stat.FAIL);
    }

    private void doTest10() {
        boolean trueStatus = false;
        try {
            sful.assertIfTrue(true);
        } catch (AssertionFailedException aEx) {
            trueStatus = true;
        } catch (Exception aEx) {
            trueStatus = false;
        }

        boolean failedCountStatus = false;
        int assertFailedCount = 0;
        try {
            failedCountStatus = (sful.getAssertionFailedCount() == 1);
        } catch (Exception aEx) {
            failedCountStatus = false;
        }

        stat.addStatus("local test10 " + trueStatus + " : " + sful.getAssertionFailedCount() , 
                ((trueStatus && failedCountStatus) == true) ? stat.PASS : stat.FAIL);
    }

    private static void sleepFor(int seconds) {
	while (seconds-- > 0) {
	    try {
		System.out.println("" + seconds + " left...");
		Thread.currentThread().sleep(1000);
	    } catch (InterruptedException inEx) {
	    }
	}
    }

}

