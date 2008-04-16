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

/** A <CODE>TopicSession</CODE> object provides methods for creating 
  * <CODE>TopicPublisher</CODE>, <CODE>TopicSubscriber</CODE>, and 
  * <CODE>TemporaryTopic</CODE> objects. It also provides a method for 
  * deleting its client's durable subscribers.
  *
  *<P>A <CODE>TopicSession</CODE> is used for creating Pub/Sub specific
  * objects. In general, use the  <CODE>Session</CODE> object, and 
  *  use <CODE>TopicSession</CODE>  only to support
  * existing code. Using the <CODE>Session</CODE> object simplifies the 
  * programming model, and allows transactions to be used across the two 
  * messaging domains.
  * 
  * <P>A <CODE>TopicSession</CODE> cannot be used to create objects specific to the 
  * point-to-point domain. The following methods inherit from 
  * <CODE>Session</CODE>, but must throw an 
  * <CODE>IllegalStateException</CODE> 
  * if used from <CODE>TopicSession</CODE>:
  *<UL>
  *   <LI><CODE>createBrowser</CODE>
  *   <LI><CODE>createQueue</CODE>
  *   <LI><CODE>createTemporaryQueue</CODE>
  *</UL>
  *
  * @version     1.1 - April 9, 2002
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author       Kate Stout
  *
  * @see         javax.jms.Session
  * @see	 javax.jms.Connection#createSession(boolean, int)
  * @see	 javax.jms.TopicConnection#createTopicSession(boolean, int)
  * @see         javax.jms.XATopicSession#getTopicSession()
  */

public interface TopicSession extends Session {

    /** Creates a topic identity given a <CODE>Topic</CODE> name.
      *
      * <P>This facility is provided for the rare cases where clients need to
      * dynamically manipulate topic identity. This allows the creation of a
      * topic identity with a provider-specific name. Clients that depend 
      * on this ability are not portable.
      *
      * <P>Note that this method is not for creating the physical topic. 
      * The physical creation of topics is an administrative task and is not
      * to be initiated by the JMS API. The one exception is the
      * creation of temporary topics, which is accomplished with the 
      * <CODE>createTemporaryTopic</CODE> method.
      *  
      * @param topicName the name of this <CODE>Topic</CODE>
      *
      * @return a <CODE>Topic</CODE> with the given name
      *
      * @exception JMSException if the session fails to create a topic
      *                         due to some internal error.
      */

    Topic
    createTopic(String topicName) throws JMSException;


    /** Creates a nondurable subscriber to the specified topic.
      *  
      * <P>A client uses a <CODE>TopicSubscriber</CODE> object to receive 
      * messages that have been published to a topic.
      *
      * <P>Regular <CODE>TopicSubscriber</CODE> objects are not durable. 
      * They receive only messages that are published while they are active.
      *
      * <P>In some cases, a connection may both publish and subscribe to a 
      * topic. The subscriber <CODE>NoLocal</CODE> attribute allows a subscriber
      * to inhibit the delivery of messages published by its own connection.
      * The default value for this attribute is false.
      *
      * @param topic the <CODE>Topic</CODE> to subscribe to
      *  
      * @exception JMSException if the session fails to create a subscriber
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid topic is specified.
      */ 

    TopicSubscriber
    createSubscriber(Topic topic) throws JMSException;


    /** Creates a nondurable subscriber to the specified topic, using a
      * message selector or specifying whether messages published by its
      * own connection should be delivered to it.
      *
      * <P>A client uses a <CODE>TopicSubscriber</CODE> object to receive 
      * messages that have been published to a topic.
      *  
      * <P>Regular <CODE>TopicSubscriber</CODE> objects are not durable. 
      * They receive only messages that are published while they are active.
      *
      * <P>Messages filtered out by a subscriber's message selector will 
      * never be delivered to the subscriber. From the subscriber's 
      * perspective, they do not exist.
      *
      * <P>In some cases, a connection may both publish and subscribe to a 
      * topic. The subscriber <CODE>NoLocal</CODE> attribute allows a subscriber
      * to inhibit the delivery of messages published by its own connection.
      * The default value for this attribute is false.
      *
      * @param topic the <CODE>Topic</CODE> to subscribe to
      * @param messageSelector only messages with properties matching the
      * message selector expression are delivered. A value of null or
      * an empty string indicates that there is no message selector 
      * for the message consumer.
      * @param noLocal if set, inhibits the delivery of messages published
      * by its own connection
      * 
      * @exception JMSException if the session fails to create a subscriber
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid topic is specified.
      * @exception InvalidSelectorException if the message selector is invalid.
      */

    TopicSubscriber 
    createSubscriber(Topic topic, 
		     String messageSelector,
		     boolean noLocal) throws JMSException;


    /** Creates a durable subscriber to the specified topic.
      *  
      * <P>If a client needs to receive all the messages published on a 
      * topic, including the ones published while the subscriber is inactive,
      * it uses a durable <CODE>TopicSubscriber</CODE>. The JMS provider
      * retains a record of this 
      * durable subscription and insures that all messages from the topic's 
      * publishers are retained until they are acknowledged by this 
      * durable subscriber or they have expired.
      *
      * <P>Sessions with durable subscribers must always provide the same 
      * client identifier. In addition, each client must specify a name that 
      * uniquely identifies (within client identifier) each durable 
      * subscription it creates. Only one session at a time can have a 
      * <CODE>TopicSubscriber</CODE> for a particular durable subscription.
      *
      * <P>A client can change an existing durable subscription by creating 
      * a durable <CODE>TopicSubscriber</CODE> with the same name and a new 
      * topic and/or 
      * message selector. Changing a durable subscriber is equivalent to 
      * unsubscribing (deleting) the old one and creating a new one.
      *
      * <P>In some cases, a connection may both publish and subscribe to a 
      * topic. The subscriber <CODE>NoLocal</CODE> attribute allows a subscriber
      * to inhibit the delivery of messages published by its own connection.
      * The default value for this attribute is false.
      *
      * @param topic the non-temporary <CODE>Topic</CODE> to subscribe to
      * @param name the name used to identify this subscription
      *  
      * @exception JMSException if the session fails to create a subscriber
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid topic is specified.
      */ 

    TopicSubscriber
    createDurableSubscriber(Topic topic, 
			    String name) throws JMSException;


    /** Creates a durable subscriber to the specified topic, using a
      * message selector or specifying whether messages published by its
      * own connection should be delivered to it.
      *  
      * <P>If a client needs to receive all the messages published on a 
      * topic, including the ones published while the subscriber is inactive,
      * it uses a durable <CODE>TopicSubscriber</CODE>. The JMS provider
      * retains a record of this 
      * durable subscription and insures that all messages from the topic's 
      * publishers are retained until they are acknowledged by this 
      * durable subscriber or they have expired.
      *
      * <P>Sessions with durable subscribers must always provide the same
      * client identifier. In addition, each client must specify a name which
      * uniquely identifies (within client identifier) each durable
      * subscription it creates. Only one session at a time can have a
      * <CODE>TopicSubscriber</CODE> for a particular durable subscription.
      * An inactive durable subscriber is one that exists but
      * does not currently have a message consumer associated with it.
      *
      * <P>A client can change an existing durable subscription by creating 
      * a durable <CODE>TopicSubscriber</CODE> with the same name and a new 
      * topic and/or 
      * message selector. Changing a durable subscriber is equivalent to 
      * unsubscribing (deleting) the old one and creating a new one.
      *
      * @param topic the non-temporary <CODE>Topic</CODE> to subscribe to
      * @param name the name used to identify this subscription
      * @param messageSelector only messages with properties matching the
      * message selector expression are delivered.  A value of null or
      * an empty string indicates that there is no message selector 
      * for the message consumer.
      * @param noLocal if set, inhibits the delivery of messages published
      * by its own connection
      *  
      * @exception JMSException if the session fails to create a subscriber
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid topic is specified.
      * @exception InvalidSelectorException if the message selector is invalid.
      */ 
 
    TopicSubscriber
    createDurableSubscriber(Topic topic,
                            String name, 
			    String messageSelector,
			    boolean noLocal) throws JMSException;


    /** Creates a publisher for the specified topic.
      *
      * <P>A client uses a <CODE>TopicPublisher</CODE> object to publish 
      * messages on a topic.
      * Each time a client creates a <CODE>TopicPublisher</CODE> on a topic, it
      * defines a 
      * new sequence of messages that have no ordering relationship with the 
      * messages it has previously sent.
      *
      * @param topic the <CODE>Topic</CODE> to publish to, or null if this is an
      * unidentified producer
      *
      * @exception JMSException if the session fails to create a publisher
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid topic is specified.
     */

    TopicPublisher 
    createPublisher(Topic topic) throws JMSException;


    /** Creates a <CODE>TemporaryTopic</CODE> object. Its lifetime will be that 
      * of the <CODE>TopicConnection</CODE> unless it is deleted earlier.
      *
      * @return a temporary topic identity
      *
      * @exception JMSException if the session fails to create a temporary
      *                         topic due to some internal error.
      */
 
    TemporaryTopic
    createTemporaryTopic() throws JMSException;


    /** Unsubscribes a durable subscription that has been created by a client.
      *  
      * <P>This method deletes the state being maintained on behalf of the 
      * subscriber by its provider.
      *
      * <P>It is erroneous for a client to delete a durable subscription
      * while there is an active <CODE>TopicSubscriber</CODE> for the 
      * subscription, or while a consumed message is part of a pending 
      * transaction or has not been acknowledged in the session.
      *
      * @param name the name used to identify this subscription
      *  
      * @exception JMSException if the session fails to unsubscribe to the 
      *                         durable subscription due to some internal error.
      * @exception InvalidDestinationException if an invalid subscription name
      *                                        is specified.
      */

    void
    unsubscribe(String name) throws JMSException;
}
