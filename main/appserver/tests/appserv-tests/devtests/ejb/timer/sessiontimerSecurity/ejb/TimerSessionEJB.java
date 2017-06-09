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

package com.sun.s1asdev.ejb.timer.sessiontimersecurity;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.ejb.TimerService;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBContext;
import javax.jms.Session;
import java.rmi.RemoteException;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueConnection;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.naming.*;

public class TimerSessionEJB implements TimedObject, SessionBean 
{
	private SessionContext context;
	private QueueConnection connection;
	private QueueSession session;
	private Queue queue;
	private QueueSender sender;

	public void ejbCreate() throws RemoteException {}

	public void ejbRemove() throws RemoteException {}

	public void setSessionContext(SessionContext sc) {
		context = sc;
	}

	// business method to create a timer
    public void dummyBusinessMethod() {
        try {
            System.out.println("dummyBusinessMethod(): getCallerPrincipal() = " 
                + context.getCallerPrincipal());
            System.out.println("dummyBusinessMethod(): isCallerInRole(foo) = " 
                + context.isCallerInRole("foo"));
        } catch(IllegalStateException ise) {
            System.out.println(
                "dummyBusinessMethod(): isCallerInRole should not throw " 
                + "illegalstateexception in business method");
            throw ise;
        }
    }

	public TimerHandle createTimer(int ms) {
            System.out.println("Calling createTimer");
            System.out.println("createTimer(): getCallerPrincipal() = " 
                + context.getCallerPrincipal());
            System.out.println("createTimer(): isCallerInRole(foo) = " 
                + context.isCallerInRole("foo"));
            try {
                System.out.println("Calling isCallerInRole");
                boolean result = context.isCallerInRole("foo");
                if (!result) {
                    System.out.println("createTimer(): isCallerInRole failed");
                    throw new Exception("isCallerInRole() should not fail in a business method");
                }
            } catch(IllegalStateException ise) {
                System.out.println("isCallerInRole should not throw " 
                    + "illegalstateexception in business method");
                throw ise;
            } catch(Exception ise) {
                // no security-role-ref defined so illegalargumentexception is thrown
            }

            try {
                System.out.println("Calling getMessageContext");
                context.getMessageContext();
            } catch(IllegalStateException ise) {
                System.out.println("getMessageContext() successfully threw illegalStateException");
            }


		TimerService timerService = context.getTimerService();
		Timer timer = timerService.createTimer(ms, "created timer");
		return timer.getHandle();
	}

	// timer callback method
	public void ejbTimeout(Timer timer) {
        try {
            System.out.println("Calling ejbTimeout");
            java.security.Principal principal = context.getCallerPrincipal();
            System.out.println("In ejbTimeout(): getCallerPrincipal(), principal - " 
                + principal);
            System.out.println("In ejbTimeout(): isCallerInRole(foo), result - " 
                + context.isCallerInRole("foo"));
        } catch(IllegalStateException ise) {
            System.out.println("ejbTimeout(): getCallerPrincipal() failed by " 
                + "throwing IllegalStateException");
        }
        try {
            System.out.println("Calling isCallerInRole");
            boolean result = context.isCallerInRole("foo");
            System.out.println("In ejbTimeout(): isCallerInRole() = " + result);
            if (!result) {
                System.out.println("ejbTimeout(): isCallerInRole() failed" );
            }
        } catch(IllegalStateException ise) {
            System.out.println("ejbTimeout(): isCallerInRole() failed by " 
                + "throwing IllegalStateException");
        }

        try {
            System.out.println("Calling getMessageContext");
            context.getMessageContext();
        } catch(IllegalStateException ise) {
            System.out.println("ejbTimeout(): getMessageContext() successfully " 
                + "threw IllegalStateException");
        }


		// add message to queue
		try {


			InitialContext ic = new InitialContext();
			QueueConnectionFactory qcFactory = (QueueConnectionFactory)
				ic.lookup("java:comp/env/jms/MyQueueConnectionFactory");
			Queue queue = (Queue) ic.lookup("java:comp/env/jms/MyQueue");
			connection = qcFactory.createQueueConnection();

			QueueSession session = connection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
			sender  = session.createSender(queue);

			TextMessage message = session.createTextMessage();
			message.setText("ejbTimeout() invoked");
			System.out.println("Sending time out message");
			sender.send(message);
			System.out.println("Time out message sent");
		} catch(NamingException e) {
			e.printStackTrace();
		} catch(JMSException e) {
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

	public void ejbActivate() {}
	public void ejbPassivate() {}
}
