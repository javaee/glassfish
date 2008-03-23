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

/** A <CODE>QueueSession</CODE> object provides methods for creating 
  * <CODE>QueueReceiver</CODE>, <CODE>QueueSender</CODE>, 
  * <CODE>QueueBrowser</CODE>, and <CODE>TemporaryQueue</CODE> objects.
  *
  * <P>If there are messages that have been received but not acknowledged 
  * when a <CODE>QueueSession</CODE> terminates, these messages will be retained 
  * and redelivered when a consumer next accesses the queue.
  *
  *<P>A <CODE>QueueSession</CODE> is used for creating Point-to-Point specific
  * objects. In general, use the <CODE>Session</CODE> object. 
  * The <CODE>QueueSession</CODE> is used to support
  * existing code. Using the <CODE>Session</CODE> object simplifies the 
  * programming model, and allows transactions to be used across the two 
  * messaging domains.
  * 
  * <P>A <CODE>QueueSession</CODE> cannot be used to create objects specific to the 
  * publish/subscribe domain. The following methods inherit from 
  * <CODE>Session</CODE>, but must throw an
  * <CODE>IllegalStateException</CODE> 
  * if they are used from <CODE>QueueSession</CODE>:
  *<UL>
  *   <LI><CODE>createDurableSubscriber</CODE>
  *   <LI><CODE>createTemporaryTopic</CODE>
  *   <LI><CODE>createTopic</CODE>
  *   <LI><CODE>unsubscribe</CODE>
  * </UL>
  *
  * @version     1.1 - April 2, 2002
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author      Kate Stout
  *
  * @see         javax.jms.Session
  * @see         javax.jms.QueueConnection#createQueueSession(boolean, int)
  * @see         javax.jms.XAQueueSession#getQueueSession()
  */

public interface QueueSession extends Session {

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
      */ 
 
    Queue
    createQueue(String queueName) throws JMSException;


    /** Creates a <CODE>QueueReceiver</CODE> object to receive messages from the
      * specified queue.
      *
      * @param queue the <CODE>Queue</CODE> to access
      *
      * @exception JMSException if the session fails to create a receiver
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid queue is specified.
      */

    QueueReceiver
    createReceiver(Queue queue) throws JMSException;


    /** Creates a <CODE>QueueReceiver</CODE> object to receive messages from the 
      * specified queue using a message selector.
      *  
      * @param queue the <CODE>Queue</CODE> to access
      * @param messageSelector only messages with properties matching the
      * message selector expression are delivered. A value of null or
      * an empty string indicates that there is no message selector 
      * for the message consumer.
      *  
      * @exception JMSException if the session fails to create a receiver
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid queue is specified.
      * @exception InvalidSelectorException if the message selector is invalid.
      *
      */ 

    QueueReceiver
    createReceiver(Queue queue, 
		   String messageSelector) throws JMSException;


    /** Creates a <CODE>QueueSender</CODE> object to send messages to the 
      * specified queue.
      *
      * @param queue the <CODE>Queue</CODE> to access, or null if this is an 
      * unidentified producer
      *
      * @exception JMSException if the session fails to create a sender
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid queue is specified.
      */
 
    QueueSender
    createSender(Queue queue) throws JMSException;


    /** Creates a <CODE>QueueBrowser</CODE> object to peek at the messages on 
      * the specified queue.
      *
      * @param queue the <CODE>Queue</CODE> to access
      *
      * @exception JMSException if the session fails to create a browser
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid queue is specified.
      */

    QueueBrowser 
    createBrowser(Queue queue) throws JMSException;


    /** Creates a <CODE>QueueBrowser</CODE> object to peek at the messages on 
      * the specified queue using a message selector.
      *  
      * @param queue the <CODE>Queue</CODE> to access
      * @param messageSelector only messages with properties matching the
      * message selector expression are delivered. A value of null or
      * an empty string indicates that there is no message selector 
      * for the message consumer.
      *  
      * @exception JMSException if the session fails to create a browser
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid queue is specified.
      * @exception InvalidSelectorException if the message selector is invalid.
      */ 

    QueueBrowser
    createBrowser(Queue queue,
		  String messageSelector) throws JMSException;


    /** Creates a <CODE>TemporaryQueue</CODE> object. Its lifetime will be that 
      * of the <CODE>QueueConnection</CODE> unless it is deleted earlier.
      *
      * @return a temporary queue identity
      *
      * @exception JMSException if the session fails to create a temporary queue
      *                         due to some internal error.
      */

    TemporaryQueue
    createTemporaryQueue() throws JMSException;
}
