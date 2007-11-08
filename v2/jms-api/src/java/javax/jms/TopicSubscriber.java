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

/** A client uses a <CODE>TopicSubscriber</CODE> object to receive messages that
  * have been published to a topic. A <CODE>TopicSubscriber</CODE> object is the
  * publish/subscribe form of a message consumer. A <CODE>MessageConsumer</CODE>
  * can be created by using <CODE>Session.createConsumer</CODE>. 
  *
  * <P>A <CODE>TopicSession</CODE> allows the creation of multiple 
  * <CODE>TopicSubscriber</CODE> objects per topic.  It will deliver each 
  * message for a topic to each
  * subscriber eligible to receive it. Each copy of the message
  * is treated as a completely separate message. Work done on one copy has
  * no effect on the others; acknowledging one does not acknowledge the
  * others; one message may be delivered immediately, while another waits
  * for its subscriber to process messages ahead of it.
  *
  * <P>Regular <CODE>TopicSubscriber</CODE> objects are not durable. They 
  * receive only messages that are published while they are active.
  *
  * <P>Messages filtered out by a subscriber's message selector will never 
  * be delivered to the subscriber. From the subscriber's perspective, they 
  * do not exist.
  *
  * <P>In some cases, a connection may both publish and subscribe to a topic.
  * The subscriber <CODE>NoLocal</CODE> attribute allows a subscriber to inhibit
  * the 
  * delivery of messages published by its own connection.
  *
  * <P>If a client needs to receive all the messages published on a topic, 
  * including the ones published while the subscriber is inactive, it uses 
  * a durable <CODE>TopicSubscriber</CODE>. The JMS provider retains a record of
  * this durable 
  * subscription and insures that all messages from the topic's publishers 
  * are retained until they are acknowledged by this durable 
  * subscriber or they have expired.
  *
  * <P>Sessions with durable subscribers must always provide the same client 
  * identifier. In addition, each client must specify a name that uniquely 
  * identifies (within client identifier) each durable subscription it creates.
  * Only one session at a time can have a <CODE>TopicSubscriber</CODE> for a 
  * particular durable subscription. 
  *
  * <P>A client can change an existing durable subscription by creating a 
  * durable <CODE>TopicSubscriber</CODE> with the same name and a new topic 
  * and/or message 
  * selector. Changing a durable subscription is equivalent to unsubscribing 
  * (deleting) the old one and creating a new one.
  *
  * <P>The <CODE>unsubscribe</CODE> method is used to delete a durable 
  * subscription. The <CODE>unsubscribe</CODE> method can be used at the 
  * <CODE>Session</CODE> or <CODE>TopicSession</CODE> level.
  * This method deletes the state being 
  * maintained on behalf of the subscriber by its provider.
  *
  * <P>Creating a <CODE>MessageConsumer</CODE> provides the same features as
  * creating a <CODE>TopicSubscriber</CODE>. To create a durable subscriber, 
  * use of <CODE>Session.CreateDurableSubscriber</CODE> is recommended. The 
  * <CODE>TopicSubscriber</CODE> is provided to support existing code.
  * 
  * 
  * @version     1.1 - February 2, 2002
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author      Kate Stout
  *
  * @see         javax.jms.Session#createConsumer
  * @see         javax.jms.Session#createDurableSubscriber
  * @see         javax.jms.TopicSession
  * @see         javax.jms.TopicSession#createSubscriber
  * @see         javax.jms.MessageConsumer
  */

public interface TopicSubscriber extends MessageConsumer {

    /** Gets the <CODE>Topic</CODE> associated with this subscriber.
      *  
      * @return this subscriber's <CODE>Topic</CODE>
      *  
      * @exception JMSException if the JMS provider fails to get the topic for
      *                         this topic subscriber
      *                         due to some internal error.
      */ 

    Topic
    getTopic() throws JMSException;


    /** Gets the <CODE>NoLocal</CODE> attribute for this subscriber. 
      * The default value for this attribute is false.
      *  
      * @return true if locally published messages are being inhibited
      *  
      * @exception JMSException if the JMS provider fails to get the
      *                         <CODE>NoLocal</CODE> attribute for
      *                         this topic subscriber
      *                         due to some internal error.
      */ 

    boolean
    getNoLocal() throws JMSException;
}
