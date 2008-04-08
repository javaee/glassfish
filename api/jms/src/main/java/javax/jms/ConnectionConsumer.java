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

/** For application servers, <CODE>Connection</CODE> objects provide a special 
  * facility 
  * for creating a <CODE>ConnectionConsumer</CODE> (optional). The messages it 
  * is to consume are 
  * specified by a <CODE>Destination</CODE> and a message selector. In addition,
  * a <CODE>ConnectionConsumer</CODE> must be given a 
  * <CODE>ServerSessionPool</CODE> to use for 
  * processing its messages.
  *
  * <P>Normally, when traffic is light, a <CODE>ConnectionConsumer</CODE> gets a
  * <CODE>ServerSession</CODE> from its pool, loads it with a single message, and
  * starts it. As traffic picks up, messages can back up. If this happens, 
  * a <CODE>ConnectionConsumer</CODE> can load each <CODE>ServerSession</CODE>
  * with more than one 
  * message. This reduces the thread context switches and minimizes resource 
  * use at the expense of some serialization of message processing.
  *
  * @version     1.1 February 8, 2002
  * @author      Mark Hapner
  * @author      Rich Burridge
  *
  * @see javax.jms.Connection#createConnectionConsumer
  * @see javax.jms.Connection#createDurableConnectionConsumer
  * @see javax.jms.QueueConnection#createConnectionConsumer
  * @see javax.jms.TopicConnection#createConnectionConsumer
  * @see javax.jms.TopicConnection#createDurableConnectionConsumer
  */

public interface ConnectionConsumer {

    /** Gets the server session pool associated with this connection consumer.
      *  
      * @return the server session pool used by this connection consumer
      *  
      * @exception JMSException if the JMS provider fails to get the server 
      *                         session pool associated with this consumer due
      *                         to some internal error.
      */

    ServerSessionPool 
    getServerSessionPool() throws JMSException; 

 
    /** Closes the connection consumer.
      *
      * <P>Since a provider may allocate some resources on behalf of a 
      * connection consumer outside the Java virtual machine, clients should 
      * close these resources when
      * they are not needed. Relying on garbage collection to eventually 
      * reclaim these resources may not be timely enough.
      *  
      * @exception JMSException if the JMS provider fails to release resources 
      *                         on behalf of the connection consumer or fails
      *                         to close the connection consumer.
      */

    void 
    close() throws JMSException; 
}
