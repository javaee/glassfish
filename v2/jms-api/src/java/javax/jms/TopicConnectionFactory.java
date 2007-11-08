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

/** A client uses a <CODE>TopicConnectionFactory</CODE> object to create 
  * <CODE>TopicConnection</CODE> objects with a publish/subscribe JMS provider.
  *
  * <P>A<CODE> TopicConnectionFactory</CODE> can be used to create a 
  * <CODE>TopicConnection</CODE>, from which specialized topic-related objects
  * can be  created. A more general, and recommended approach 
  * is to use the <CODE>ConnectionFactory</CODE> object.
  *  
  * <P> The <CODE>TopicConnectionFactory</CODE> object
  * should be used to support existing code.
  *
  * @version    1.1 - February 2, 2002
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author      Kate Stout
  *
  * @see         javax.jms.ConnectionFactory
  */

public interface TopicConnectionFactory extends ConnectionFactory {

    /** Creates a topic connection with the default user identity.
      * The connection is created in stopped mode. No messages 
      * will be delivered until the <code>Connection.start</code> method
      * is explicitly called.
      *
      * @return a newly created topic connection
      *
      * @exception JMSException if the JMS provider fails to create a topic 
      *                         connection due to some internal error.
      * @exception JMSSecurityException if client authentication fails due to 
      *                                 an invalid user name or password.
      */ 

    TopicConnection
    createTopicConnection() throws JMSException;


    /** Creates a topic connection with the specified user identity.
      * The connection is created in stopped mode. No messages 
      * will be delivered until the <code>Connection.start</code> method
      * is explicitly called.
      *  
      * @param userName the caller's user name
      * @param password the caller's password
      *  
      * @return a newly created topic connection
      *
      * @exception JMSException if the JMS provider fails to create a topic 
      *                         connection due to some internal error.
      * @exception JMSSecurityException if client authentication fails due to 
      *                                 an invalid user name or password.
      */ 

    TopicConnection
    createTopicConnection(String userName, String password) 
					     throws JMSException;
}
