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

package com.sun.s1asdev.ejb.timer.timerserialization.client;

import java.util.ArrayList;

import java.io.Serializable;
import javax.naming.*;
import javax.jms.*;
import javax.ejb.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.NoSuchObjectException;

import com.sun.s1asdev.ejb.timer.timerserialization.ejb.TimerSFSBHome;
import com.sun.s1asdev.ejb.timer.timerserialization.ejb.TimerSFSB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    // consts
    public static String kTestNotRun    = "TEST NOT RUN";
    public static String kTestPassed    = "TEST PASSED";
    public static String kTestFailed    = "TEST FAILED";
    
    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private static final int    MAX_TIMERS = 60;

    private ArrayList sfsbList = new ArrayList();

    public static void main (String[] args) {

        stat.addDescription("timerserialization");
        Client client = new Client(args);
        System.out.println("[TimerSerializationClinet] doTest()...");
        client.doTest();
        System.out.println("[TimerSerializationClinet] DONE doTest()...");
        stat.printSummary("timerserialization");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {
        initSFSBList();       //create MAX_TIMERS

        forceTimerSerialization();
        forceTimerDeserialization();
        accessTimers();
        getTimerHandles();
        cancelTimers();
    }

    private void initSFSBList() {
        System.out.println("[TimerSerializationClinet] Inside init....");
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/TimerSFSB");
            TimerSFSBHome home = (TimerSFSBHome)PortableRemoteObject.narrow
                (objref, TimerSFSBHome.class);
            for (int i=0; i < MAX_TIMERS; i++) {

                //Creating these many SFSBs will cause passivation
                TimerSFSB sfsb = (TimerSFSB) home.create("Timer_"+i);

                sfsbList.add(sfsb);
            }
            System.out.println("[TimerSerializationClinet] Initalization done");
        } catch(Exception e) {
            e.printStackTrace();
            //stat.addStatus("ejbclient localEntityGetEJBObject(-)" , stat.PASS);
        System.out.println("[TimerSerializationClinet] Exception in init....");
        e.printStackTrace();
        }
    }

    public void forceTimerSerialization() {
        try {

        for (int i=0; i < MAX_TIMERS; i++) {
            TimerSFSB sfsb = (TimerSFSB) sfsbList.get(i);
                sfsbList.get(i);

            //Note:- calling getName itself might cause some activation but will
            //  not excercise Timer deserialization as we haven't created
            //  any timers yet
            String timerName = sfsb.getName();
            sfsb.createTimer(15*1000);
            //sfsb.createTimer(60*1000);
            System.out.println("Created timer for: " + timerName);

            //Must force passivation of some SFSBs

            stat.addStatus("ejbclient forceTimerSerialization", stat.PASS);
        }

        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus("ejbclient forceTimerSerialization", stat.FAIL);

        }
    }

    public void forceTimerDeserialization() {
        try {
        for (int i=0; i < MAX_TIMERS; i++) {
            TimerSFSB sfsb = (TimerSFSB) sfsbList.get(i);
            String timerName = sfsb.getName();
            System.out.println("Activated SFSB bean for: " + timerName);

            stat.addStatus("ejbclient forceTimerDeserialization", stat.PASS);
        }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus("ejbclient forceTimerDeserialization", stat.FAIL);

        }
    }

    public void accessTimers() {
        try {
        for (int i=0; i < MAX_TIMERS; i++) {
            TimerSFSB sfsb = (TimerSFSB) sfsbList.get(i);
            String timerName = sfsb.getName();
            System.out.println("Accessing Timer bean for: " + timerName);
            sfsb.getTimeRemaining();
            System.out.println("Successfully accessed Timer bean for: " + timerName);

            stat.addStatus("ejbclient accessTimers", stat.PASS);
        }
        } catch (Exception ex) {
            stat.addStatus("ejbclient accessTimers", stat.FAIL);

        }
    }

    public void getTimerHandles() {
        try {
        for (int i=0; i < MAX_TIMERS; i++) {
            TimerSFSB sfsb = (TimerSFSB) sfsbList.get(i);
            String timerName = sfsb.getName();
            System.out.println("Accessing Timer bean for: " + timerName);
            sfsb.getTimerHandle();  // Will also test whether timer handles are serializable
            System.out.println("Successfully accessed Timer bean for: " + timerName);

            stat.addStatus("ejbclient getTimerHandles", stat.PASS);
        }
        } catch (Exception ex) {
            stat.addStatus("ejbclient getTimerHandles", stat.FAIL);

        }
    }


    public void cancelTimers() {
        try {
        for (int i=0; i < MAX_TIMERS; i++) {
            TimerSFSB sfsb = (TimerSFSB) sfsbList.get(i);
            String timerName = sfsb.getName();
            System.out.println("Accessing Timer bean for: " + timerName);
            sfsb.cancelTimer();  
            System.out.println("Successfully accessed Timer bean for: " + timerName);

            stat.addStatus("ejbclient cancelTimers", stat.PASS);
        }
        } catch (Exception ex) {
            stat.addStatus("ejbclient cancelTimers", stat.FAIL);

        }
    }


}
    
