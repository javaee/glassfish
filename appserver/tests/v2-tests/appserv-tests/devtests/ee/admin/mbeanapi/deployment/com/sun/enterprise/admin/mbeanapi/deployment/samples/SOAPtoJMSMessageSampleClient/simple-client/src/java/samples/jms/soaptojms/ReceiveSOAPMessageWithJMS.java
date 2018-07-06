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
public class ReceiveSOAPMessageWithJMS implements MessageListener {

    TopicConnectionFactory tcf = null;
    TopicConnection tc = null;
    TopicSession session = null;
    Topic topic = null;
    TopicSubscriber subscriber = null;

    MessageFactory messageFactory = null;

    /**
     * Default constructor.
     *
     * @param topicName  a String that contains the name of a JMS Topic  
     *
     */
    public ReceiveSOAPMessageWithJMS(String topicName) {
        init(topicName);
    }

    /**
     * JMS Connection/Session/Destination/MessageListener set ups.
     *
     * @param topicName a String that contains the name of a JMS Topic  
     */
    public void init(String topicName) {
        try {

            /**
             * construct a default SOAP message factory.
             */
            messageFactory = MessageFactory.newInstance();

            /**
             * JMS set up.
             */            
            ServiceLocator servicelocator = new ServiceLocator();
            tcf = servicelocator.getTopicConnectionFactory(JNDINames.TOPIC_CONNECTION_FACTORY);
            tc = tcf.createTopicConnection();
            session = tc.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            topic = servicelocator.getTopic(topicName); 
            subscriber = session.createSubscriber(topic);
            subscriber.setMessageListener( this );
            tc.start();

            System.out.println ("Ready to receive SOAP messages ...");
            Thread.currentThread().join();
        } catch (Exception jmse) {
            jmse.printStackTrace();
        }
    }   
     
     
    /**
     * JMS Messages are delivered to this method. The body of the message
     * contains SOAP streams.
     *
     * 1.  The message conversion utility converts JMS message to SOAP
     * message type.
     * 2.  Get the attachment parts and print content information to the
     * standard output stream.
     *
     * @param message a delivered message
     *
     */
    public void onMessage (Message message) {

        try {

            /**
             * convert JMS to SOAP message.
             */
            System.out.println("Message received! Converting the JMS message to SOAP message");

            SOAPMessage soapMessage =
            MessageTransformer.SOAPMessageFromJMSMessage( message, messageFactory );

            /**
             * Print attachment counts.
             */
            System.out.println("Attachment counts: " + soapMessage.countAttachments());

            /**
             * Get attachment parts of the SOAP message.
             */
            Iterator iterator = soapMessage.getAttachments();
            while ( iterator.hasNext() ) {
                /**
                 * Get next attachment.
                 */
                AttachmentPart ap = (AttachmentPart) iterator.next();
                /**
                 * Get content type.
                 */
                String contentType = ap.getContentType();
                System.out.println("Content type: " + contentType);
                /**
                 * Get content Id.
                 */
                String contentId = ap.getContentId();
                System.out.println("Content Id: " + contentId);

                /**
                 * Check if this is a Text attachment.
                 */
                if ( contentType.indexOf("text") >=0 ) {
                    /**
                     * Get and print the string content if it is a text
                     * attachment.
                     */
                    String content = (String) ap.getContent();
                    System.out.println("Attachment content:\n\n" + content);
                    System.out.println("*** attachment content ends ***");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }
   
}
