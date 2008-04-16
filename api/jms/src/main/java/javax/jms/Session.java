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

import java.io.Serializable;

/** <P>A <CODE>Session</CODE> object is a single-threaded context for producing and consuming 
  * messages. Although it may allocate provider resources outside the Java 
  * virtual machine (JVM), it is considered a lightweight JMS object.
  *
  * <P>A session serves several purposes:
  *
  * <UL>
  *   <LI>It is a factory for its message producers and consumers.
  *   <LI>It supplies provider-optimized message factories.
  *   <LI>It is a factory for <CODE>TemporaryTopics</CODE> and 
  *        <CODE>TemporaryQueues</CODE>. 
  *   <LI> It provides a way to create <CODE>Queue</CODE> or <CODE>Topic</CODE>
  *      objects for those clients that need to dynamically manipulate 
  *      provider-specific destination names.
  *   <LI>It supports a single series of transactions that combine work 
  *       spanning its producers and consumers into atomic units.
  *   <LI>It defines a serial order for the messages it consumes and 
  *       the messages it produces.
  *   <LI>It retains messages it consumes until they have been 
  *       acknowledged.
  *   <LI>It serializes execution of message listeners registered with 
  *       its message consumers.
  *   <LI> It is a factory for <CODE>QueueBrowsers</CODE>.
  * </UL>
  *
  * <P>A session can create and service multiple message producers and 
  * consumers.
  *
  * <P>One typical use is to have a thread block on a synchronous 
  * <CODE>MessageConsumer</CODE> until a message arrives. The thread may then
  * use one or more of the <CODE>Session</CODE>'s <CODE>MessageProducer</CODE>s.
  *
  * <P>If a client desires to have one thread produce messages while others 
  * consume them, the client should use a separate session for its producing 
  * thread.
  *
  * <P>Once a connection has been started, any session with one or more 
  * registered message listeners is dedicated to the thread of control that 
  * delivers messages to it. It is erroneous for client code to use this session
  * or any of its constituent objects from another thread of control. The
  * only exception to this rule is the use of the session or connection 
  * <CODE>close</CODE> method.
  *
  * <P>It should be easy for most clients to partition their work naturally
  * into sessions. This model allows clients to start simply and incrementally
  * add message processing complexity as their need for concurrency grows.
  *
  * <P>The <CODE>close</CODE> method is the only session method that can be 
  * called while some other session method is being executed in another thread.
  *
  * <P>A session may be specified as transacted. Each transacted 
  * session supports a single series of transactions. Each transaction groups 
  * a set of message sends and a set of message receives into an atomic unit 
  * of work. In effect, transactions organize a session's input message 
  * stream and output message stream into series of atomic units. When a 
  * transaction commits, its atomic unit of input is acknowledged and its 
  * associated atomic unit of output is sent. If a transaction rollback is 
  * done, the transaction's sent messages are destroyed and the session's input 
  * is automatically recovered.
  *
  * <P>The content of a transaction's input and output units is simply those 
  * messages that have been produced and consumed within the session's current 
  * transaction.
  *
  * <P>A transaction is completed using either its session's <CODE>commit</CODE>
  * method or its session's <CODE>rollback</CODE> method. The completion of a
  * session's current transaction automatically begins the next. The result is
  * that a transacted session always has a current transaction within which its 
  * work is done.  
  *
  * <P>The Java Transaction Service (JTS) or some other transaction monitor may 
  * be used to combine a session's transaction with transactions on other 
  * resources (databases, other JMS sessions, etc.). Since Java distributed 
  * transactions are controlled via the Java Transaction API (JTA), use of the 
  * session's <CODE>commit</CODE> and <CODE>rollback</CODE> methods in 
  * this context is prohibited.
  *
  * <P>The JMS API does not require support for JTA; however, it does define 
  * how a provider supplies this support.
  *
  * <P>Although it is also possible for a JMS client to handle distributed 
  * transactions directly, it is unlikely that many JMS clients will do this.
  * Support for JTA in the JMS API is targeted at systems vendors who will be 
  * integrating the JMS API into their application server products.
  *
  * @version     1.1 February 2, 2002
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author      Kate Stout
  *
  * @see         javax.jms.QueueSession
  * @see         javax.jms.TopicSession
  * @see         javax.jms.XASession
  */ 
 
public interface Session extends Runnable {

    /** With this acknowledgment mode, the session automatically acknowledges
      * a client's receipt of a message either when the session has successfully 
      * returned from a call to <CODE>receive</CODE> or when the message 
      * listener the session has called to process the message successfully 
      * returns.
      */ 

    static final int AUTO_ACKNOWLEDGE = 1;

    /** With this acknowledgment mode, the client acknowledges a consumed 
      * message by calling the message's <CODE>acknowledge</CODE> method. 
      * Acknowledging a consumed message acknowledges all messages that the 
      * session has consumed.
      *
      * <P>When client acknowledgment mode is used, a client may build up a 
      * large number of unacknowledged messages while attempting to process 
      * them. A JMS provider should provide administrators with a way to 
      * limit client overrun so that clients are not driven to resource 
      * exhaustion and ensuing failure when some resource they are using 
      * is temporarily blocked.
      *
      * @see javax.jms.Message#acknowledge()
      */ 

    static final int CLIENT_ACKNOWLEDGE = 2;

    /** This acknowledgment mode instructs the session to lazily acknowledge 
      * the delivery of messages. This is likely to result in the delivery of 
      * some duplicate messages if the JMS provider fails, so it should only be 
      * used by consumers that can tolerate duplicate messages. Use of this  
      * mode can reduce session overhead by minimizing the work the 
      * session does to prevent duplicates.
      */

    static final int DUPS_OK_ACKNOWLEDGE = 3;
    
    /** This value is returned from the method 
     * <CODE>getAcknowledgeMode</CODE> if the session is transacted.
     * If a <CODE>Session</CODE> is transacted, the acknowledgement mode
     * is ignored.
     */
    static final int SESSION_TRANSACTED = 0;

    /** Creates a <CODE>BytesMessage</CODE> object. A <CODE>BytesMessage</CODE> 
      * object is used to send a message containing a stream of uninterpreted 
      * bytes.
      *  
      * @exception JMSException if the JMS provider fails to create this message
      *                         due to some internal error.
      */ 
    

    BytesMessage 
    createBytesMessage() throws JMSException; 

 
    /** Creates a <CODE>MapMessage</CODE> object. A <CODE>MapMessage</CODE> 
      * object is used to send a self-defining set of name-value pairs, where 
      * names are <CODE>String</CODE> objects and values are primitive values 
      * in the Java programming language.
      *  
      * @exception JMSException if the JMS provider fails to create this message
      *                         due to some internal error.
      */ 

    MapMessage 
    createMapMessage() throws JMSException; 

 
    /** Creates a <CODE>Message</CODE> object. The <CODE>Message</CODE> 
      * interface is the root interface of all JMS messages. A 
      * <CODE>Message</CODE> object holds all the 
      * standard message header information. It can be sent when a message 
      * containing only header information is sufficient.
      *  
      * @exception JMSException if the JMS provider fails to create this message
      *                         due to some internal error.
      */ 

    Message
    createMessage() throws JMSException;


    /** Creates an <CODE>ObjectMessage</CODE> object. An 
      * <CODE>ObjectMessage</CODE> object is used to send a message 
      * that contains a serializable Java object.
      *  
      * @exception JMSException if the JMS provider fails to create this message
      *                         due to some internal error.
      */ 

    ObjectMessage
    createObjectMessage() throws JMSException; 


    /** Creates an initialized <CODE>ObjectMessage</CODE> object. An 
      * <CODE>ObjectMessage</CODE> object is used 
      * to send a message that contains a serializable Java object.
      *  
      * @param object the object to use to initialize this message
      *
      * @exception JMSException if the JMS provider fails to create this message
      *                         due to some internal error.
      */ 

    ObjectMessage
    createObjectMessage(Serializable object) throws JMSException;

 
    /** Creates a <CODE>StreamMessage</CODE> object. A 
      * <CODE>StreamMessage</CODE> object is used to send a 
      * self-defining stream of primitive values in the Java programming 
      * language.
      *  
      * @exception JMSException if the JMS provider fails to create this message
      *                         due to some internal error.
      */

    StreamMessage 
    createStreamMessage() throws JMSException;  

 
    /** Creates a <CODE>TextMessage</CODE> object. A <CODE>TextMessage</CODE> 
      * object is used to send a message containing a <CODE>String</CODE>
      * object.
      *  
      * @exception JMSException if the JMS provider fails to create this message
      *                         due to some internal error.
      */ 

    TextMessage 
    createTextMessage() throws JMSException; 


    /** Creates an initialized <CODE>TextMessage</CODE> object. A 
      * <CODE>TextMessage</CODE> object is used to send 
      * a message containing a <CODE>String</CODE>.
      *
      * @param text the string used to initialize this message
      *
      * @exception JMSException if the JMS provider fails to create this message
      *                         due to some internal error.
      */ 

    TextMessage
    createTextMessage(String text) throws JMSException;


    /** Indicates whether the session is in transacted mode.
      *  
      * @return true if the session is in transacted mode
      *  
      * @exception JMSException if the JMS provider fails to return the 
      *                         transaction mode due to some internal error.
      */ 

    boolean
    getTransacted() throws JMSException;
    
    /** Returns the acknowledgement mode of the session. The acknowledgement
     * mode is set at the time that the session is created. If the session is
     * transacted, the acknowledgement mode is ignored.
     *
     *@return            If the session is not transacted, returns the 
     *                  current acknowledgement mode for the session.
     *                  If the session
     *                  is transacted, returns SESSION_TRANSACTED.
     *
     *@exception JMSException   if the JMS provider fails to return the 
     *                         acknowledgment mode due to some internal error.
     *
     *@see Connection#createSession
     *@since 1.1
     */
    int 
    getAcknowledgeMode() throws JMSException;


    /** Commits all messages done in this transaction and releases any locks
      * currently held.
      *
      * @exception JMSException if the JMS provider fails to commit the
      *                         transaction due to some internal error.
      * @exception TransactionRolledBackException if the transaction
      *                         is rolled back due to some internal error
      *                         during commit.
      * @exception IllegalStateException if the method is not called by a 
      *                         transacted session.
      */

    void
    commit() throws JMSException;


    /** Rolls back any messages done in this transaction and releases any locks 
      * currently held.
      *
      * @exception JMSException if the JMS provider fails to roll back the
      *                         transaction due to some internal error.
      * @exception IllegalStateException if the method is not called by a 
      *                         transacted session.
      *                                     
      */

    void
    rollback() throws JMSException;


    /** Closes the session.
      *
      * <P>Since a provider may allocate some resources on behalf of a session 
      * outside the JVM, clients should close the resources when they are not 
      * needed. 
      * Relying on garbage collection to eventually reclaim these resources 
      * may not be timely enough.
      *
      * <P>There is no need to close the producers and consumers
      * of a closed session. 
      *
      * <P> This call will block until a <CODE>receive</CODE> call or message 
      * listener in progress has completed. A blocked message consumer
      * <CODE>receive</CODE> call returns <CODE>null</CODE> when this session 
      * is closed.
      *
      * <P>Closing a transacted session must roll back the transaction
      * in progress.
      * 
      * <P>This method is the only <CODE>Session</CODE> method that can 
      * be called concurrently. 
      *
      * <P>Invoking any other <CODE>Session</CODE> method on a closed session 
      * must throw a <CODE>JMSException.IllegalStateException</CODE>. Closing a 
      * closed session must <I>not</I> throw an exception.
      * 
      * @exception JMSException if the JMS provider fails to close the
      *                         session due to some internal error.
      */

    void
    close() throws JMSException;


    /** Stops message delivery in this session, and restarts message delivery
      * with the oldest unacknowledged message.
      *  
      * <P>All consumers deliver messages in a serial order.
      * Acknowledging a received message automatically acknowledges all 
      * messages that have been delivered to the client.
      *
      * <P>Restarting a session causes it to take the following actions:
      *
      * <UL>
      *   <LI>Stop message delivery
      *   <LI>Mark all messages that might have been delivered but not 
      *       acknowledged as "redelivered"
      *   <LI>Restart the delivery sequence including all unacknowledged 
      *       messages that had been previously delivered. Redelivered messages
      *       do not have to be delivered in 
      *       exactly their original delivery order.
      * </UL>
      *
      * @exception JMSException if the JMS provider fails to stop and restart
      *                         message delivery due to some internal error.
      * @exception IllegalStateException if the method is called by a 
      *                         transacted session.
      */ 

    void
    recover() throws JMSException;


    /** Returns the session's distinguished message listener (optional).
      *
      * @return the message listener associated with this session
      *
      * @exception JMSException if the JMS provider fails to get the message 
      *                         listener due to an internal error.
      *
      * @see javax.jms.Session#setMessageListener
      * @see javax.jms.ServerSessionPool
      * @see javax.jms.ServerSession
      */

    MessageListener
    getMessageListener() throws JMSException;


    /** Sets the session's distinguished message listener (optional).
      *
      * <P>When the distinguished message listener is set, no other form of 
      * message receipt in the session can 
      * be used; however, all forms of sending messages are still supported.
      * 
      * <P>This is an expert facility not used by regular JMS clients.
      *
      * @param listener the message listener to associate with this session
      *
      * @exception JMSException if the JMS provider fails to set the message 
      *                         listener due to an internal error.
      *
      * @see javax.jms.Session#getMessageListener
      * @see javax.jms.ServerSessionPool
      * @see javax.jms.ServerSession
      */

    void
    setMessageListener(MessageListener listener) throws JMSException;

    /**
     * Optional operation, intended to be used only by Application Servers,
     * not by ordinary JMS clients.
     *
     * @see javax.jms.ServerSession
     */
    public void run();
    
    /** Creates a <CODE>MessageProducer</CODE> to send messages to the specified 
      * destination.
      *
      * <P>A client uses a <CODE>MessageProducer</CODE> object to send 
      * messages to a destination. Since <CODE>Queue</CODE> and <CODE>Topic</CODE> 
      * both inherit from <CODE>Destination</CODE>, they can be used in
      * the destination parameter to create a <CODE>MessageProducer</CODE> object.
      * 
      * @param destination the <CODE>Destination</CODE> to send to, 
      * or null if this is a producer which does not have a specified 
      * destination.
      *
      * @exception JMSException if the session fails to create a MessageProducer
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid destination
      * is specified.
      *
      * @since 1.1 
      * 
     */

    MessageProducer
    createProducer(Destination destination) throws JMSException;
    
    
       /** Creates a <CODE>MessageConsumer</CODE> for the specified destination.
      * Since <CODE>Queue</CODE> and <CODE>Topic</CODE> 
      * both inherit from <CODE>Destination</CODE>, they can be used in
      * the destination parameter to create a <CODE>MessageConsumer</CODE>.
      *
      * @param destination the <CODE>Destination</CODE> to access. 
      *
      * @exception JMSException if the session fails to create a consumer
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid destination 
      *                         is specified.
      *
      * @since 1.1 
      */

    MessageConsumer
    createConsumer(Destination destination) throws JMSException;

       /** Creates a <CODE>MessageConsumer</CODE> for the specified destination, 
      * using a message selector. 
      * Since <CODE>Queue</CODE> and <CODE>Topic</CODE> 
      * both inherit from <CODE>Destination</CODE>, they can be used in
      * the destination parameter to create a <CODE>MessageConsumer</CODE>.
      *
      * <P>A client uses a <CODE>MessageConsumer</CODE> object to receive 
      * messages that have been sent to a destination.
      *  
      *       
      * @param destination the <CODE>Destination</CODE> to access
      * @param messageSelector only messages with properties matching the
      * message selector expression are delivered. A value of null or
      * an empty string indicates that there is no message selector 
      * for the message consumer. 
      * 
      *  
      * @exception JMSException if the session fails to create a MessageConsumer
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid destination
       * is specified.
     
      * @exception InvalidSelectorException if the message selector is invalid.
      *
      * @since 1.1 
      */
    MessageConsumer     
    createConsumer(Destination destination, java.lang.String messageSelector) 
    throws JMSException;
    
    
     /** Creates <CODE>MessageConsumer</CODE> for the specified destination, using a
      * message selector. This method can specify whether messages published by 
      * its own connection should be delivered to it, if the destination is a 
      * topic. 
      *<P> Since <CODE>Queue</CODE> and <CODE>Topic</CODE> 
      * both inherit from <CODE>Destination</CODE>, they can be used in
      * the destination parameter to create a <CODE>MessageConsumer</CODE>.
      * <P>A client uses a <CODE>MessageConsumer</CODE> object to receive 
      * messages that have been published to a destination. 
      *               
      * <P>In some cases, a connection may both publish and subscribe to a 
      * topic. The consumer <CODE>NoLocal</CODE> attribute allows a consumer
      * to inhibit the delivery of messages published by its own connection.
      * The default value for this attribute is False. The <CODE>noLocal</CODE> 
      * value must be supported by destinations that are topics. 
      *
      * @param destination the <CODE>Destination</CODE> to access 
      * @param messageSelector only messages with properties matching the
      * message selector expression are delivered. A value of null or
      * an empty string indicates that there is no message selector 
      * for the message consumer.
      * @param NoLocal  - if true, and the destination is a topic,
      *                   inhibits the delivery of messages published
      *                   by its own connection.  The behavior for
      *                   <CODE>NoLocal</CODE> is 
      *                   not specified if the destination is a queue.
      * 
      * @exception JMSException if the session fails to create a MessageConsumer
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid destination
       * is specified.
     
      * @exception InvalidSelectorException if the message selector is invalid.
      *
      * @since 1.1 
      *
      */
    MessageConsumer     
    createConsumer(Destination destination, java.lang.String messageSelector, 
    boolean NoLocal)   throws JMSException;
    
    
      /** Creates a queue identity given a <CODE>Queue</CODE> name.
      *
      * <P>This facility is provided for the rare cases where clients need to
      * dynamically manipulate queue identity. It allows the creation of a
      * queue identity with a provider-specific name. Clients that depend 
      * on this ability are not portable.
      *
      * <P>Note that this method is not for creating the physical queue. 
      * The physical creation of queues is an administrative task and is not
      * to be initiated by the JMS API. The one exception is the
      * creation of temporary queues, which is accomplished with the 
      * <CODE>createTemporaryQueue</CODE> method.
      *
      * @param queueName the name of this <CODE>Queue</CODE>
      *
      * @return a <CODE>Queue</CODE> with the given name
      *
      * @exception JMSException if the session fails to create a queue
      *                         due to some internal error.
      * @since 1.1
      */ 
 
    Queue
    createQueue(String queueName) throws JMSException;
    
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
      * @since 1.1
      */

    Topic
    createTopic(String topicName) throws JMSException;

     /** Creates a <CODE>QueueBrowser</CODE> object to peek at the messages on 
      * the specified queue.
      *
      * @param queue the <CODE>queue</CODE> to access
      *
      * @exception InvalidDestinationException if an invalid destination
      *                         is specified 
      *
      * @since 1.1 
      */
    
    
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
      *
      * @since 1.1
      */ 

    TopicSubscriber
    createDurableSubscriber(Topic topic, 
			    String name) throws JMSException;


    /** Creates a durable subscriber to the specified topic, using a
      * message selector and specifying whether messages published by its
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
      *
      * @since 1.1
      */ 
 
    TopicSubscriber
    createDurableSubscriber(Topic topic,
                            String name, 
			    String messageSelector,
			    boolean noLocal) throws JMSException;
    
  /** Creates a <CODE>QueueBrowser</CODE> object to peek at the messages on 
      * the specified queue.
      *  
      * @param queue the <CODE>queue</CODE> to access
      *
      *  
      * @exception JMSException if the session fails to create a browser
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid destination
      *                         is specified 
      *
      * @since 1.1 
      */ 
    QueueBrowser 
    createBrowser(Queue queue) throws JMSException;


    /** Creates a <CODE>QueueBrowser</CODE> object to peek at the messages on 
      * the specified queue using a message selector.
      *  
      * @param queue the <CODE>queue</CODE> to access
      *
      * @param messageSelector only messages with properties matching the
      * message selector expression are delivered. A value of null or
      * an empty string indicates that there is no message selector 
      * for the message consumer.
      *  
      * @exception JMSException if the session fails to create a browser
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid destination
      *                         is specified 
      * @exception InvalidSelectorException if the message selector is invalid.
      *
      * @since 1.1 
      */ 

    QueueBrowser
    createBrowser(Queue queue,
		  String messageSelector) throws JMSException;

    
     /** Creates a <CODE>TemporaryQueue</CODE> object. Its lifetime will be that 
      * of the <CODE>Connection</CODE> unless it is deleted earlier.
      *
      * @return a temporary queue identity
      *
      * @exception JMSException if the session fails to create a temporary queue
      *                         due to some internal error.
      *
      *@since 1.1
      */

    TemporaryQueue
    createTemporaryQueue() throws JMSException;
   

     /** Creates a <CODE>TemporaryTopic</CODE> object. Its lifetime will be that 
      * of the <CODE>Connection</CODE> unless it is deleted earlier.
      *
      * @return a temporary topic identity
      *
      * @exception JMSException if the session fails to create a temporary
      *                         topic due to some internal error.
      *
      * @since 1.1  
      */
 
    TemporaryTopic
    createTemporaryTopic() throws JMSException;


    /** Unsubscribes a durable subscription that has been created by a client.
      *  
      * <P>This method deletes the state being maintained on behalf of the 
      * subscriber by its provider.
      *
      * <P>It is erroneous for a client to delete a durable subscription
      * while there is an active <CODE>MessageConsumer</CODE>
      * or <CODE>TopicSubscriber</CODE> for the 
      * subscription, or while a consumed message is part of a pending 
      * transaction or has not been acknowledged in the session.
      *
      * @param name the name used to identify this subscription
      *  
      * @exception JMSException if the session fails to unsubscribe to the 
      *                         durable subscription due to some internal error.
      * @exception InvalidDestinationException if an invalid subscription name
      *                                        is specified.
      *
      * @since 1.1
      */

    void
    unsubscribe(String name) throws JMSException;
   
}
