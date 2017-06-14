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

package com.sun.s1asdev.ejb.perf.timer1;

import java.io.Serializable;
import javax.jms.*;
import javax.ejb.*;
import javax.annotation.Resource;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    // consts
    public static String kTestNotRun    = "TEST NOT RUN";
    public static String kTestPassed    = "TEST PASSED";
    public static String kTestFailed    = "TEST FAILED";
    
    
    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private static @EJB TimerSession timerSession;

    private static @Resource(mappedName="jms/ejb_perf_timer1_TQCF")
        QueueConnectionFactory qcFactory;

    private static @Resource(mappedName="jms/ejb_perf_timer1_TQueue") 
        Queue queue;

    public static void main(String args[]) {

        stat.addDescription("ejb-timer-sessiontimer");
        Client client = new Client(args);
        int interval = Integer.parseInt(args[0]);
        int maxTimeouts = Integer.parseInt(args[1]);
        client.doTest(interval, maxTimeouts);
        stat.printSummary("ejb-timer-sessiontimer");
    }

    public Client(String args[]) {

    }

    public String doTest(int interval, int maxTimeouts) {
        String result = kTestPassed;
        QueueConnection connection = null; 

        timerSession.createTimer(interval, maxTimeouts);

        System.out.println("Creating periodic timer with interval of " +
                           interval + " milliseconds.");
        System.out.println("Max timeouts = " + maxTimeouts);
        
        try {
            connection = qcFactory.createQueueConnection();
            QueueSession session = 
                connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueReceiver receiver = session.createReceiver(queue);
            connection.start();
            System.out.println("Waiting for message");
            Message message = receiver.receive();
            TextMessage textMsg = (TextMessage)message;
            
            if ( (message == null) || 
                 (! textMsg.getText().equals("ejbTimeout() invoked")))
                throw new Exception("Received a null message ... TimeOut failed!!");
            System.out.println("Message : " + message);
            
            
            stat.addStatus("timer1 ", stat.PASS);
        } catch(Exception e) {

            stat.addStatus("timer1 ", stat.FAIL);

        } finally {
            if( connection != null ) {
                try {
                    connection.close();
                } catch(Exception e) {}
            }
        }

        return result;
    }
}
