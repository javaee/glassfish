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

/** An <CODE>XAQueueConnectionFactory</CODE> provides the same create options as
  * a <CODE>QueueConnectionFactory</CODE> (optional).
  *
  * <P>The <CODE>XATopicConnectionFactory</CODE> interface is optional.  JMS providers 
  * are not required to support this interface. This interface is for 
  * use by JMS providers to support transactional environments. 
  * Client programs are strongly encouraged to use the transactional support
  * available in their environment, rather than use these XA
  * interfaces directly. 
  *
  * @version     1.1 - 8 April 2002
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author      Kate Stout
  *
  * @see         javax.jms.QueueConnectionFactory
  * @see         javax.jms.XAConnectionFactory
  */

public interface XAQueueConnectionFactory 
       extends XAConnectionFactory, QueueConnectionFactory {

    /** Creates an XA queue connection with the default user identity.
      * The connection is created in stopped mode. No messages 
      * will be delivered until the <code>Connection.start</code> method
      * is explicitly called.
      *
      * @return a newly created XA queue connection
      *
      * @exception JMSException if the JMS provider fails to create an XA queue 
      *                         connection due to some internal error.
      * @exception JMSSecurityException  if client authentication fails due to 
      *                         an invalid user name or password.
       */ 

    XAQueueConnection
    createXAQueueConnection() throws JMSException;


    /** Creates an XA queue connection with the specified user identity.
      * The connection is created in stopped mode. No messages 
      * will be delivered until the <code>Connection.start</code> method
      * is explicitly called.
      *  
      * @param userName the caller's user name
      * @param password the caller's password
      *  
      * @return a newly created XA queue connection
      *
      * @exception JMSException if the JMS provider fails to create an XA queue 
      *                         connection due to some internal error.
      * @exception JMSSecurityException  if client authentication fails due to 
      *                         an invalid user name or password.
      */ 

    XAQueueConnection
    createXAQueueConnection(String userName, String password) 
					     throws JMSException;
}
