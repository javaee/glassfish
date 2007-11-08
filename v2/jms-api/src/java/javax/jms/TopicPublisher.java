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


package javax.jms;

/** A client uses a <CODE>TopicPublisher</CODE> object to publish messages on a 
  * topic. A <CODE>TopicPublisher</CODE> object is the publish-subscribe form
  * of a message producer.
  *
  * <P>Normally, the <CODE>Topic</CODE> is specified when a 
  * <CODE>TopicPublisher</CODE> is created.  In this case, an attempt to use 
  * the <CODE>publish</CODE> methods for an unidentified 
  * <CODE>TopicPublisher</CODE> will throw a 
  * <CODE>java.lang.UnsupportedOperationException</CODE>.
  *
  * <P>If the <CODE>TopicPublisher</CODE> is created with an unidentified 
  * <CODE>Topic</CODE>, an attempt to use the <CODE>publish</CODE> methods that 
  * assume that the <CODE>Topic</CODE> has been identified will throw a 
  * <CODE>java.lang.UnsupportedOperationException</CODE>.
  *
  * <P>During the execution of its <CODE>publish</CODE> method,
  * a message must not be changed by other threads within the client. 
  * If the message is modified, the result of the <CODE>publish</CODE> is 
  * undefined.
  * 
  * <P>After publishing a message, a client may retain and modify it
  * without affecting the message that has been published. The same message
  * object may be published multiple times.
  * 
  * <P>The following message headers are set as part of publishing a 
  * message: <code>JMSDestination</code>, <code>JMSDeliveryMode</code>, 
  * <code>JMSExpiration</code>, <code>JMSPriority</code>, 
  * <code>JMSMessageID</code> and <code>JMSTimeStamp</code>.
  * When the message is published, the values of these headers are ignored. 
  * After completion of the <CODE>publish</CODE>, the headers hold the values 
  * specified by the method publishing the message. It is possible for the 
  * <CODE>publish</CODE> method not to set <code>JMSMessageID</code> and 
  * <code>JMSTimeStamp</code> if the 
  * setting of these headers is explicitly disabled by the 
  * <code>MessageProducer.setDisableMessageID</code> or
  * <code>MessageProducer.setDisableMessageTimestamp</code> method.
  *
  *<P>Creating a <CODE>MessageProducer</CODE> provides the same features as
  * creating a <CODE>TopicPublisher</CODE>. A <CODE>MessageProducer</CODE> object is 
  * recommended when creating new code. The  <CODE>TopicPublisher</CODE> is
  * provided to support existing code.

  *
  *<P>Because <CODE>TopicPublisher</CODE> inherits from 
  * <CODE>MessageProducer</CODE>, it inherits the
  * <CODE>send</CODE> methods that are a part of the <CODE>MessageProducer</CODE> 
  * interface. Using the <CODE>send</CODE> methods will have the same
  * effect as using the
  * <CODE>publish</CODE> methods: they are functionally the same.
  * 
  *
  * @version    1.1 February 2, 2002
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author      Kate Stout
  *
  * @see Session#createProducer(Destination) 
  * @see TopicSession#createPublisher(Topic)
  */

public interface TopicPublisher extends MessageProducer {

    /** Gets the topic associated with this <CODE>TopicPublisher</CODE>.
      *
      * @return this publisher's topic
      *  
      * @exception JMSException if the JMS provider fails to get the topic for
      *                         this <CODE>TopicPublisher</CODE>
      *                         due to some internal error.
      */

    Topic 
    getTopic() throws JMSException;

 
    /** Publishes a message to the topic.
      * Uses the <CODE>TopicPublisher</CODE>'s default delivery mode, priority,
      * and time to live.
      *
      * @param message the message to publish
      *
      * @exception JMSException if the JMS provider fails to publish the message
      *                         due to some internal error.
      * @exception MessageFormatException if an invalid message is specified.
      * @exception InvalidDestinationException if a client uses this method
      *                         with a <CODE>TopicPublisher</CODE> with
      *                         an invalid topic.
      * @exception java.lang.UnsupportedOperationException if a client uses this
      *                         method with a <CODE>TopicPublisher</CODE> that
      *                         did not specify a topic at creation time.
      * 
      * @see javax.jms.MessageProducer#getDeliveryMode()
      * @see javax.jms.MessageProducer#getTimeToLive()
      * @see javax.jms.MessageProducer#getPriority()
      */

    void 
    publish(Message message) throws JMSException;


    /** Publishes a message to the topic, specifying delivery mode,
      * priority, and time to live.
      *
      * @param message the message to publish
      * @param deliveryMode the delivery mode to use
      * @param priority the priority for this message
      * @param timeToLive the message's lifetime (in milliseconds)
      *
      * @exception JMSException if the JMS provider fails to publish the message
      *                         due to some internal error.
      * @exception MessageFormatException if an invalid message is specified.
      * @exception InvalidDestinationException if a client uses this method
      *                         with a <CODE>TopicPublisher</CODE> with
      *                         an invalid topic.
      * @exception java.lang.UnsupportedOperationException if a client uses this
      *                         method with a <CODE>TopicPublisher</CODE> that
      *                         did not specify a topic at creation time.
      */
 
    void
    publish(Message message, 
            int deliveryMode, 
	    int priority,
	    long timeToLive) throws JMSException;


    /** Publishes a message to a topic for an unidentified message producer. 
      * Uses the <CODE>TopicPublisher</CODE>'s default delivery mode, 
      * priority, and time to live.
      *  
      * <P>Typically, a message producer is assigned a topic at creation 
      * time; however, the JMS API also supports unidentified message producers,
      * which require that the topic be supplied every time a message is
      * published.
      *
      * @param topic the topic to publish this message to
      * @param message the message to publish
      *  
      * @exception JMSException if the JMS provider fails to publish the message
      *                         due to some internal error.
      * @exception MessageFormatException if an invalid message is specified.
      * @exception InvalidDestinationException if a client uses
      *                         this method with an invalid topic.
      * 
      * @see javax.jms.MessageProducer#getDeliveryMode()
      * @see javax.jms.MessageProducer#getTimeToLive()
      * @see javax.jms.MessageProducer#getPriority()
      */ 

    void
    publish(Topic topic, Message message) throws JMSException;


    /** Publishes a message to a topic for an unidentified message 
      * producer, specifying delivery mode, priority and time to live.
      *  
      * <P>Typically, a message producer is assigned a topic at creation
      * time; however, the JMS API also supports unidentified message producers,
      * which require that the topic be supplied every time a message is
      * published.
      *
      * @param topic the topic to publish this message to
      * @param message the message to publish
      * @param deliveryMode the delivery mode to use
      * @param priority the priority for this message
      * @param timeToLive the message's lifetime (in milliseconds)
      *  
      * @exception JMSException if the JMS provider fails to publish the message
      *                         due to some internal error.
      * @exception MessageFormatException if an invalid message is specified.
      * @exception InvalidDestinationException if a client uses
      *                         this method with an invalid topic.
      */ 

    void
    publish(Topic topic, 
            Message message, 
            int deliveryMode, 
            int priority,
	    long timeToLive) throws JMSException;
}
