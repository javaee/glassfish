/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package com.sun.enterprise.ee.synchronization.util.concurrent;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import java.util.Random;

class BarrierWorker implements Runnable {

    int id;
    CyclicBarrier barrier;
    long tout;

    BarrierWorker (int num, CyclicBarrier bar, long t) { 
        id = num; 
        barrier = bar;
        tout = t;
    }

    public void run() {
        // pre barrier work here
        System.out.println("Doing pre barrier work for thread id "+id);
        boolean ex = false;

        try {
            /*
            Random rd = new Random();
            if (rd.nextBoolean()) {
                Thread.currentThread().sleep(200);
            }
            */
            //barrier.await(tout);
            barrier.attemptBarrier(tout);
        } catch (Exception e) {
            ex = true;
            System.out.println(">>Got exception in worker: "+id);
            System.out.println(e);
        }
        if (!ex) {
            System.out.println("Doing post barrier work for thread id "+id);
        }
    }
}

class BarrierAction implements Runnable {
    public void run() {
        System.out.println("Barrier Action in Thread: " 
                    + Thread.currentThread().getName());
    }
}


public class BarrierUnitTest extends TestCase {

    public BarrierUnitTest(String name) {
        super(name);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    public void testBarrier() {
        commonBarrier(false);
    }
    
    public void testBarrierWithTimeout() {
        timeout = 1;
        commonBarrier(false);
        timeout = 0;
    }
    
    public void commonBarrier(boolean testInterrupt) {         

        System.out.println("Total Threads: " + N);
        System.out.println("Timeout: " + timeout);

        Thread [] threads = new Thread[N];

        CyclicBarrier barrier = new CyclicBarrier(N, new BarrierAction());
        for ( int i =0; i < N; i++ ) {
            threads[i] = new Thread(new BarrierWorker(i, barrier, timeout));
        }

        // start the threads
        for ( int i=0; i < N; i++ ) {
            threads[i].start();
            if (testInterrupt) {
                if (i > 5) {
                    threads[2].interrupt();
                }
            }
        }
        //System.out.println("Waiting: " + barrier.getNumberWaiting());

        // join the threads
        for (int i =0; i < N; i++) {
            try {
                threads[i].join();
            } catch(Exception e) {
                fail(" Thread " + i + " could not be joined");
            }
        }
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(BarrierUnitTest.class);
    }

    /** Number of threads for barrier **/
    private int N = 10;

    private long timeout = 0;

}
