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

import javax.ejb.*;
import javax.jms.*;
import javax.annotation.*;
import java.util.Date;

@Stateless
public class TimerSessionEJB implements TimerSession
{

    private static int timeoutCount = 0;
    private static int maxTimeouts = 0;
    private static Date startTime;

    @Resource TimerService timerSvc;

    @Resource(mappedName="jms/ejb_perf_timer1_TQCF")
    private QueueConnectionFactory qcFactory;

    private QueueConnection connection;

    private QueueSession session;
    
    @Resource(mappedName="jms/ejb_perf_timer1_TQueue")
    private Queue queue;

    private QueueSender sender;


    // business method to create a timer
    public void createTimer(int ms, int maxDeliveries) {

        timerSvc.createTimer(1, ms, "created timer");
        maxTimeouts = maxDeliveries;
        timeoutCount = 0;
        startTime = new Date();

    }

    @Timeout
    public void ejbTimeout(Timer timer) {

        timeoutCount++;
        
        if( timeoutCount <= maxTimeouts ) {
            return;
        }

        try {

            timer.cancel();
            Date endTime = new Date();
            long elapsed = endTime.getTime() - startTime.getTime();

            System.out.println("It took " + elapsed + " milliseconds " +
                               "for " + maxTimeouts + " timeouts");

            connection = qcFactory.createQueueConnection();

            QueueSession session = 
                connection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
            sender  = session.createSender(queue);

            TextMessage message = session.createTextMessage();
            message.setText("ejbTimeout() invoked");
            System.out.println("Sending time out message");
            sender.send(message);
            System.out.println("Time out message sent");

        } catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(connection != null) {
                    connection.close();
                    connection = null;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
	}

    }

}
