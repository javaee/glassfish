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

/** A <CODE>ServerSession</CODE> object is an application server object that 
  * is used by a server to associate a thread with a JMS session (optional).
  *
  * <P>A <CODE>ServerSession</CODE> implements two methods:
  *
  * <UL>
  *   <LI><CODE>getSession</CODE> - returns the <CODE>ServerSession</CODE>'s 
  *       JMS session.
  *   <LI><CODE>start</CODE> - starts the execution of the 
  *       <CODE>ServerSession</CODE> 
  *       thread and results in the execution of the JMS session's 
  *       <CODE>run</CODE> method.
  * </UL>
  *
  * <P>A <CODE>ConnectionConsumer</CODE> implemented by a JMS provider uses a 
  * <CODE>ServerSession</CODE> to process one or more messages that have 
  * arrived. It does this by getting a <CODE>ServerSession</CODE> from the 
  * <CODE>ConnectionConsumer</CODE>'s <CODE>ServerSessionPool</CODE>; getting 
  * the <CODE>ServerSession</CODE>'s JMS session; loading it with the messages; 
  * and then starting the <CODE>ServerSession</CODE>.
  *
  * <P>In most cases the <CODE>ServerSession</CODE> will register some object 
  * it provides as the <CODE>ServerSession</CODE>'s thread run object. The 
  * <CODE>ServerSession</CODE>'s <CODE>start</CODE> method will call the 
  * thread's <CODE>start</CODE> method, which will start the new thread, and 
  * from it, call the <CODE>run</CODE> method of the 
  * <CODE>ServerSession</CODE>'s run object. This object will do some 
  * housekeeping and then call the <CODE>Session</CODE>'s <CODE>run</CODE> 
  * method. When <CODE>run</CODE> returns, the <CODE>ServerSession</CODE>'s run 
  * object can return the <CODE>ServerSession</CODE> to the 
  * <CODE>ServerSessionPool</CODE>, and the cycle starts again.
  *
  * <P>Note that the JMS API does not architect how the 
  * <CODE>ConnectionConsumer</CODE> loads the <CODE>Session</CODE> with 
  * messages. Since both the <CODE>ConnectionConsumer</CODE> and 
  * <CODE>Session</CODE> are implemented by the same JMS provider, they can 
  * accomplish the load using a private mechanism.
  *
  * @version     1.0 - 9 March 1998
  * @author      Mark Hapner
  * @author      Rich Burridge
  *
  * @see         javax.jms.ServerSessionPool
  * @see         javax.jms.ConnectionConsumer
  */

public interface ServerSession {

    /** Return the <CODE>ServerSession</CODE>'s <CODE>Session</CODE>. This must 
      * be a <CODE>Session</CODE> created by the same <CODE>Connection</CODE> 
      * that will be dispatching messages to it. The provider will assign one or
      * more messages to the <CODE>Session</CODE> 
      * and then call <CODE>start</CODE> on the <CODE>ServerSession</CODE>.
      *
      * @return the server session's session
      *  
      * @exception JMSException if the JMS provider fails to get the associated
      *                         session for this <CODE>ServerSession</CODE> due
      *                         to some internal error.
      **/

    Session
    getSession() throws JMSException;


    /** Cause the <CODE>Session</CODE>'s <CODE>run</CODE> method to be called 
      * to process messages that were just assigned to it.
      *  
      * @exception JMSException if the JMS provider fails to start the server
      *                         session to process messages due to some internal
      *                         error.
      */

    void 
    start() throws JMSException; 
}
