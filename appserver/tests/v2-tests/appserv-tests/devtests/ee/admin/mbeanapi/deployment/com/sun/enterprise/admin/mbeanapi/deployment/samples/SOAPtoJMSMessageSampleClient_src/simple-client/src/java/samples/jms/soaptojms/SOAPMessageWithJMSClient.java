/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2018 Oracle and/or its affiliates. All rights reserved.
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

package samples.jms.soaptojms;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.AttachmentPart;

import com.sun.messaging.xml.MessageTransformer;
import javax.jms.TopicConnectionFactory;

import javax.jms.MessageListener;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.JMSException;
import javax.jms.TopicSubscriber;

import java.util.Iterator;

/**
 * This sample program shows a JMS message listener can use the MessageTransformer
 * utility to convert JMS messages back to SOAP messages.
 */
public class SOAPMessageWithJMSClient {
   
    /**    
     * The main program to send SOAP messages with JMS and ReceiveSOAPMessageWithJMS.
     */    
    public static void main (String[] args) {

        String topicName = JNDINames.TEST_MDB_TOPIC;
        String usage = "\nUsage: enter parameter Send or Receive (followed by optional Topic name). \n"+
                       "To Receive message: \"appclient -client SOAPtoJMSMessageSampleClient.jar Receive\" \n"+
                       "To Send message: \"appclient -client SOAPtoJMSMessageSampleClient.jar Send\"";                      

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("Receive")){
                if (args.length > 1) {
                    topicName = args[1];
                }
                try {
                    ReceiveSOAPMessageWithJMS rsm = new ReceiveSOAPMessageWithJMS(topicName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (args[0].equalsIgnoreCase("Send")){
                if (args.length > 1) {
                    topicName = args[1];
                }
                try {
                    SendSOAPMessageWithJMS ssm = new SendSOAPMessageWithJMS(topicName);
                    ssm.send();
                    ssm.close();
                    System.out.println("Finished");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
              System.out.println(usage);
            }            
        } else {
            System.out.println(usage);
        }

    }
}
