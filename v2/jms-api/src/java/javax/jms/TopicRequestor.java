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

/** The <CODE>TopicRequestor</CODE> helper class simplifies
  * making service requests.
  *
  * <P>The <CODE>TopicRequestor</CODE> constructor is given a non-transacted 
  * <CODE>TopicSession</CODE> and a destination <CODE>Topic</CODE>. It creates a 
  * <CODE>TemporaryTopic</CODE> for the responses and provides a 
  * <CODE>request</CODE> method that sends the request message and waits 
  * for its reply.
  *
  * <P>This is a basic request/reply abstraction that should be sufficient 
  * for most uses. JMS providers and clients are free to create more 
  * sophisticated versions.
  *
  * @version     1.0 - 8 July 1998
  * @author      Mark Hapner
  * @author      Rich Burridge
  *
  * @see         javax.jms.QueueRequestor
  */

public class TopicRequestor {

    TopicSession    session;    // The topic session the topic belongs to.
    Topic           topic;      // The topic to perform the request/reply on.  
    TemporaryTopic  tempTopic;
    TopicPublisher  publisher;
    TopicSubscriber subscriber;


    /** Constructor for the <CODE>TopicRequestor</CODE> class.
      * 
      * <P>This implementation assumes the session parameter to be non-transacted,
      * with a delivery mode of either <CODE>AUTO_ACKNOWLEDGE</CODE> or 
      * <CODE>DUPS_OK_ACKNOWLEDGE</CODE>.
      *
      * @param session the <CODE>TopicSession</CODE> the topic belongs to
      * @param topic the topic to perform the request/reply call on
      *
      * @exception JMSException if the JMS provider fails to create the
      *                         <CODE>TopicRequestor</CODE> due to some internal
      *                         error.
      * @exception InvalidDestinationException if an invalid topic is specified.
      */ 

    public 
    TopicRequestor(TopicSession session, Topic topic) throws JMSException {
	this.session = session;
	this.topic   = topic;
        tempTopic    = session.createTemporaryTopic();
        publisher    = session.createPublisher(topic);
        subscriber   = session.createSubscriber(tempTopic);
    }


    /** Sends a request and waits for a reply. The temporary topic is used for
      * the <CODE>JMSReplyTo</CODE> destination; the first reply is returned, 
      * and any following replies are discarded.
      *
      * @param message the message to send
      *  
      * @return the reply message
      *  
      * @exception JMSException if the JMS provider fails to complete the
      *                         request due to some internal error.
      */

    public Message
    request(Message message) throws JMSException {
	message.setJMSReplyTo(tempTopic);
        publisher.publish(message);
	return(subscriber.receive());
    }


    /** Closes the <CODE>TopicRequestor</CODE> and its session.
      *
      * <P>Since a provider may allocate some resources on behalf of a 
      * <CODE>TopicRequestor</CODE> outside the Java virtual machine, clients 
      * should close them when they 
      * are not needed. Relying on garbage collection to eventually reclaim 
      * these resources may not be timely enough.
      *
      * <P>Note that this method closes the <CODE>TopicSession</CODE> object 
      * passed to the <CODE>TopicRequestor</CODE> constructor.
      *  
      * @exception JMSException if the JMS provider fails to close the
      *                         <CODE>TopicRequestor</CODE> due to some internal
      *                         error.
      */

    public void
    close() throws JMSException {

	// publisher and consumer created by constructor are implicitly closed.
	session.close();
	tempTopic.delete();
    }
}
