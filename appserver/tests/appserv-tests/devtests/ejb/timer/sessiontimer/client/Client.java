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

package com.sun.s1asdev.ejb.timer.sessiontimer.client;

import java.io.Serializable;
import javax.naming.*;

import java.util.concurrent.*;

import javax.ejb.*;

import com.sun.s1asdev.ejb.timer.sessiontimer.TimerSession;
import com.sun.s1asdev.ejb.timer.sessiontimer.TimerSessionHome;
import com.sun.s1asdev.ejb.timer.sessiontimer.TimerSingletonRemote;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import javax.rmi.PortableRemoteObject;

public class Client {
    // consts
    public static String kTestNotRun    = "TEST NOT RUN";
    public static String kTestPassed    = "TEST PASSED";
    public static String kTestFailed    = "TEST FAILED";
    
    
    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    String jndiName = "ejb/ejb_timer_sessiontimer_TimerSession";
    public static void main(String args[]) {

        stat.addDescription("ejb-timer-sessiontimer");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-timer-sessiontimer");
    }

    public Client(String args[]) {
        for (int i=0; i<args.length; i++) {
            System.err.println("ARGS: " + args[i]);
        }

        if( args.length == 1) {
            jndiName = args[0];
        }
    }

    public String doTest() {
        String result = kTestPassed;
        TimerSession remoteObj = null;

        String ejbName = "ejbs/Timer";

        try {
            Context ic = new InitialContext();
            
            System.out.println("Looking up ejb ref " + ejbName);
            System.out.println("Doing timer test for " + jndiName);
//PG->            Object objref = ic.lookup(jndiName);


//            java.lang.Object objref = ic.lookup("java:comp/env/" + ejbName);
	    java.lang.Object objref = ic.lookup("ejb/ejb_timer_sessiontimer_TimerSession");

            System.out.println("---ejb stub---" + 
                objref.getClass().getClassLoader());
            System.out.println("---ejb classname---" + 
                objref.getClass().getName());
            System.out.println("---TimerSessionHome---" + 
                TimerSessionHome.class.getClassLoader());
            System.err.println("Looked up home!!");

	    TimerSingletonRemote tsr = (TimerSingletonRemote)
		ic.lookup("ejb/ejb_timer_sessiontimer_TimerSingleton");
            // clear out singleton state.  This allows multiple test
            // runs without having to redeploy
	    tsr.startTest();

            TimerSessionHome home = (TimerSessionHome)
                PortableRemoteObject.narrow(objref, TimerSessionHome.class);
            
            remoteObj = home.create();
	    int timeoutInSeconds = 5;
            TimerHandle handle = remoteObj.createTimer(timeoutInSeconds * 1000);

            System.out.println("Waiting for message");

	    boolean gotTimeout = tsr.waitForTimeout(timeoutInSeconds * 2);

	    // @@@
            System.out.println("TimerSession : jndi lookup for -> " + 
                jndiName + " <- test passed!!");
            
            stat.addStatus("sessiontimer " + jndiName, stat.PASS);
        } catch(Exception e) {
            System.out.println("TimerSession : " + jndiName + " test failed");
            e.printStackTrace();
            result = kTestFailed;
            stat.addStatus("sessiontimer " + jndiName, stat.FAIL);
        }
        finally {
            try {
                ((EJBObject)remoteObj).remove();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
