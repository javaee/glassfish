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

package com.sun.s1asdev.ejb.timer.mdbtimer;

import java.rmi.RemoteException;
import javax.jms.*;
import javax.ejb.*;
import java.io.Serializable;
import javax.naming.*;
import java.util.Date;

public class MessageBean  
    implements MessageDrivenBean, MessageListener,TimedObject {
    private MessageDrivenContext mdc;

    public MessageBean(){
    }

    public void ejbTimeout(Timer t) {
        Date now = new Date();
        Date expire = (Date) t.getInfo();
        System.out.println("In messagebean:ejbtimeout at time " +
                           now);
        if( now.after(expire) ) {
            System.out.println("Cancelling timer");
            t.cancel();

            QueueConnection connection = null;
            try {
                InitialContext ic = new InitialContext();
                Queue queue = (Queue) ic.lookup("java:comp/env/jms/MyQueue");
                QueueConnectionFactory qcFactory = (QueueConnectionFactory)
                    ic.lookup("java:comp/env/jms/MyQueueConnectionFactory");
                connection = qcFactory.createQueueConnection();
                QueueSession session = connection.createQueueSession(false,
                     Session.AUTO_ACKNOWLEDGE);
                QueueSender sender = session.createSender(queue);
                ObjectMessage message = session.createObjectMessage();
                message.setObject(now);
                System.out.println("Sending message " + message);
                sender.send(message);
                System.out.println("message sent");
                
            } catch(NamingException e) {
                e.printStackTrace();
            }
            catch(JMSException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if(connection != null) {
                        connection.close();
                    }
                } catch(Exception e) {
                e.printStackTrace();
                }
            }

        } else {
            System.out.println("timer lives on!!!");
        }
    }

    public void onMessage(Message message) {
        System.out.println("Got message!!!");

        QueueConnection connection = null;
        try {
            InitialContext ic = new InitialContext();
            Queue queue = (Queue) ic.lookup("java:comp/env/jms/MyQueue");
            QueueConnectionFactory qcFactory = (QueueConnectionFactory)
                ic.lookup("java:comp/env/jms/MyQueueConnectionFactory");
            connection = qcFactory.createQueueConnection();
            QueueSession session = connection.createQueueSession(false,
                                   Session.AUTO_ACKNOWLEDGE);
            Date now = new Date();
            Date expire = new Date(now.getTime() + 25000);
            System.out.println("Creating periodic timer at " + now);
            Timer t = mdc.getTimerService().createTimer(10000, 10000, 
                                                        expire);
            System.out.println("Timer will be cancelled on first expiration after " +
                               expire);
            QueueSender sender = session.createSender(queue);
            ObjectMessage outMessage = session.createObjectMessage();
            outMessage.setObject(expire);
            System.out.println("Sending message " + outMessage);
            sender.send(outMessage);
            System.out.println("message sent");
            
        } catch(Exception e) {
            System.out.println("got exception in message bean");
            e.printStackTrace();
        } finally {
            try {
                if(connection != null) {
                    connection.close();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void setMessageDrivenContext(MessageDrivenContext mdc) {
	this.mdc = mdc;
	System.out.println("In MessageDrivenEJB::setMessageDrivenContext !!");
    }

    public void ejbCreate() throws RemoteException {
	System.out.println("In MessageDrivenEJB::ejbCreate !!");
    }

    public void ejbRemove() {
	System.out.println("In MessageDrivenEJB::ejbRemove !!");
    }

}
