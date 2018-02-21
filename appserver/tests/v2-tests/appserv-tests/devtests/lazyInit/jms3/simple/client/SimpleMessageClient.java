/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.ejb.mdb.simple.client;

import javax.jms.*;
import javax.naming.*;
import com.sun.messaging.Queue;
import com.sun.messaging.QueueConnectionFactory;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleMessageClient {
    
        QueueConnectionFactory  queueConnectionFactory = null;
        QueueConnection  queueConnection = null;
        QueueSession            queueSession = null;
        com.sun.messaging.Queue                   queue = null;
        QueueSender             queueSender = null;
        TextMessage             message = null;
        QueueReceiver           receiver = null;
        final String  MSG_TEXT = new String("Here is a client-acknowledge message");
        final int               NUM_MSGS = 3;
        private static boolean  allDone=false;
        
        private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");
        
        public static void main(String[] args) {
            
            stat.addDescription("This is to test simple "+
            "message driven bean sample.");
            SimpleMessageClient client=new SimpleMessageClient();
            client.setup();
            client.sendMessage();
            client.recvMessage();
            client.printReport();           
                        
        }
        
        
        public void setup(){
            try{
            queueConnectionFactory=new com.sun.messaging.QueueConnectionFactory();
            queue = new com.sun.messaging.Queue();                                  
            queue.setProperty("imqDestinationName", "new_queue_name");
            }catch(Throwable e)
            {
                stat.addStatus("simple jms3 jndiLookup", stat.FAIL);
                System.out.println("Problem in looking up connection factories");
                e.printStackTrace();
            }
            
        }
        
        

        
        public SimpleMessageClient(){}
        
        
        public void sendMessage(){
            try {
       

            queueConnection =
                queueConnectionFactory.createQueueConnection();
            queueSession =
                queueConnection.createQueueSession(false,
                    Session.CLIENT_ACKNOWLEDGE);
            queueSender = queueSession.createSender(queue);
            message = queueSession.createTextMessage();
            message.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
            //System.out.println("Created durable queue subscriber,persistent delivery mode");

            for (int i = 0; i < NUM_MSGS; i++) {                
                message.setText(MSG_TEXT + (i + 1));
                if(i==(NUM_MSGS-1))
                    message.setStringProperty("MESSAGE_NUM","LAST");
                System.out.println("Sending message: " + message.getText());
                queueSender.send(message);                              
                Thread.sleep(1000);
            }
            System.out.println("Sent 3 messages,now sleeping");          
        } catch (Throwable e) {
            System.out.println("Exception occurred: " + e.toString());
            stat.addStatus("simple jms3 main", stat.FAIL);
        } finally {
            System.out.println("In finally block of send message");
	    if (queueConnection !=null){
	    try{
	    queueConnection.close();
	    }catch(JMSException ex){
	    ex.printStackTrace();
	    }
	    }
           
            stat.addStatus("simple jms3 sendmessage", stat.PASS);                     
        } // finally
    }          
        
        public static void printReport(){
            if(allDone)
            stat.printSummary("simpleMdbID");            
           else
                System.out.println("MessageStream from server not finished");
        }    
        
    
    
    
    public void recvMessage(){
        QueueConnection connect=null;
        /*
         * Create connection.
         * Create session from connection; false means session is
         * not transacted.
         * Create consumer, then start message delivery.
         * Receive all text messages from destination until
         * a 3 messages are received indicating end of
         * message stream.
         * Close connection.
         */
        System.out.println("********************************");
	System.out.println("inside recvMessage of jms3 appclient");
        try {
            connect = queueConnectionFactory.createQueueConnection();
            QueueSession session = connect.createQueueSession(false,0);
            receiver=session.createReceiver(queue);  
            System.out.println("Started Receiver");
            connect.start();
            int msgcount=1;
            while (true) {                
                Message m = receiver.receive(10000);
                System.out.println("Bingo!. got a ack msg back from server");
                msgcount++;
                System.out.println("COUNT :"+msgcount);
                if (m != null) {
                    if (m instanceof TextMessage) {
                        message = (TextMessage) m;
                        System.out.println("Reading message: " +
                            message.getText());
                        String props=message.getStringProperty("MESSAGE_NUM");
                    } else {
                        break;
                        
                    }
                }
                if(msgcount>=3){
                System.out.println("All messages from server recieved******************");
                stat.addStatus("simple jms3 recvmessage", stat.PASS);
                break;
                }
            }
            System.out.println("******************");
            System.out.println("Messages from Queue finished**");
            System.out.println("******************");
        } catch (JMSException e) {
            System.out.println("Exception occurred: " + 
                e.toString());
            stat.addStatus("simple jms3 recvmessage", stat.FAIL);
        } catch(Throwable e){
            e.printStackTrace();
        }
        finally {
            if (connect != null) {
                try {
                    connect.close();
                } catch (JMSException e) {}
            }
        }
        allDone=true;
    }
    }




