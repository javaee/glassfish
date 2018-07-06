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

import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.Name;

import java.net.URL;
import javax.activation.DataHandler;

import com.sun.messaging.xml.MessageTransformer;
//import com.sun.messaging.TopicConnectionFactory;
import javax.jms.TopicConnectionFactory;

import javax.jms.TopicConnection;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Message;
import javax.jms.TopicSession;
import javax.jms.Topic;
import javax.jms.TopicPublisher;

import java.util.*;
import java.io.*;

/**
 * This example shows how to use the MessageTransformer utility to send SOAP
 * messages with JMS.
 * <p>
 * SOAP messages are constructed with javax.xml.soap API.  The messages
 * are converted with MessageTransformer utility to convert SOAP to JMS
 * message types.  The JMS messages are then published to the JMS topics.
 */
public class SendSOAPMessageWithJMS {

    TopicConnectionFactory tcf = null;
    TopicConnection tc = null;
    TopicSession session = null;
    Topic topic = null;

    TopicPublisher publisher = null;

    /**
     * default constructor.
     *
     * @param topicName a String that contains the name of a JMS Topic 
     *
     */
    public SendSOAPMessageWithJMS(String topicName) {
        init(topicName);
    }
     
    /**
     * Initialize JMS Connection/Session/Topic and Publisher.
     *
     * @param topicName a String that contains the name of a JMS Topic 
     *
     */
    public void init(String topicName) {
        try {
            ServiceLocator servicelocator = new ServiceLocator();
            tcf = servicelocator.getTopicConnectionFactory(JNDINames.TOPIC_CONNECTION_FACTORY);
            tc = tcf.createTopicConnection();
            session = tc.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            topic = servicelocator.getTopic(topicName); 
            publisher = session.createPublisher(topic);             
        } catch (JMSException jmse) {
            jmse.printStackTrace();
        } catch(ServiceLocatorException se) {
            se.printStackTrace();
        }
    }

    /**
     * Send SOAP message with JMS API.
     */
    public void send () throws Exception {

        /**
         * Construct a default SOAP message factory.
         */
        MessageFactory mf = MessageFactory.newInstance();
        /**
         * Create a SOAP message object.
         */
        System.out.println ("Create a SOAP message"); 
        SOAPMessage soapMessage = mf.createMessage();
        /**
         * Get SOAP part.
         */
        SOAPPart soapPart = soapMessage.getSOAPPart();
        /**
         * Get SOAP envelope.
         */
        SOAPEnvelope soapEnvelope = soapPart.getEnvelope();

        /**
         * Get SOAP body.
         */
        SOAPBody soapBody = soapEnvelope.getBody();
        /**
         * Create a name object. with name space http://www.sun.com/imq.
         */
        Name name = soapEnvelope.createName("HelloWorld", "hw", "http://www.sun.com/imq");
        /**
         * Add child element with the above name.
         */
        SOAPElement element = soapBody.addChildElement(name);

        /**
         * Add another child element.
         */
        element.addTextNode( "Welcome to Sun Web Services." );

        /**
         * Create an atachment with activation API.
         */
        
        URL url = getUrlFromPropsFile();
        System.out.println ("Attaching the file from URL: " + url); 

        DataHandler dh = new DataHandler (url);
        AttachmentPart ap = soapMessage.createAttachmentPart(dh);
        /**
         * set content type/ID.
         */
        ap.setContentType("text/html");
        ap.setContentId("cid-001");

        /**
         *  add the attachment to the SOAP message.
         */
        soapMessage.addAttachmentPart(ap);
        soapMessage.saveChanges();

        /**
         * Convert SOAP to JMS message.
         */
        System.out.println ("Convert the message to JMS message"); 
        Message m = MessageTransformer.SOAPMessageIntoJMSMessage( soapMessage, session );

        /**
         * publish JMS message.
         */
        System.out.println ("Publish the message"); 
        publisher.publish( m );
    }

    
    /** Read server and port from soaptojms.properties file
     *
     */
    
    public URL getUrlFromPropsFile() throws Exception {
        InputStream props = SendSOAPMessageWithJMS.class.getResourceAsStream("soaptojms.properties");
        
        Properties P = new Properties();
        P.load(props);
        
        return (new URL(P.getProperty("url")));
    }
    
            
    /**
     * Close JMS connection.
     *
     * @exception JMSException 
     */
    public void close() throws JMSException {
        tc.close();
    }
   
}
