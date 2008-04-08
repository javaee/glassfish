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

/** The delivery modes supported by the JMS API are <CODE>PERSISTENT</CODE> and
  * <CODE>NON_PERSISTENT</CODE>.
  *
  * <P>A client marks a message as persistent if it feels that the 
  * application will have problems if the message is lost in transit.
  * A client marks a message as non-persistent if an occasional
  * lost message is tolerable. Clients use delivery mode to tell a
  * JMS provider how to balance message transport reliability with throughput.
  *
  * <P>Delivery mode covers only the transport of the message to its 
  * destination. Retention of a message at the destination until
  * its receipt is acknowledged is not guaranteed by a <CODE>PERSISTENT</CODE> 
  * delivery mode. Clients should assume that message retention 
  * policies are set administratively. Message retention policy
  * governs the reliability of message delivery from destination
  * to message consumer. For example, if a client's message storage 
  * space is exhausted, some messages may be dropped in accordance with 
  * a site-specific message retention policy.
  *
  * <P>A message is guaranteed to be delivered once and only once
  * by a JMS provider if the delivery mode of the message is 
  * <CODE>PERSISTENT</CODE> 
  * and if the destination has a sufficient message retention policy.
  *
  *
  *
  * @version     1.0 - 7 August 1998
  * @author      Mark Hapner
  * @author      Rich Burridge
  */

public interface DeliveryMode {

    /** This is the lowest-overhead delivery mode because it does not require 
      * that the message be logged to stable storage. The level of JMS provider
      * failure that causes a <CODE>NON_PERSISTENT</CODE> message to be lost is 
      * not defined.
      *
      * <P>A JMS provider must deliver a <CODE>NON_PERSISTENT</CODE> message 
      * with an 
      * at-most-once guarantee. This means that it may lose the message, but it 
      * must not deliver it twice.
      */

    static final int NON_PERSISTENT = 1;

    /** This delivery mode instructs the JMS provider to log the message to stable 
      * storage as part of the client's send operation. Only a hard media 
      * failure should cause a <CODE>PERSISTENT</CODE> message to be lost.
      */

    static final int PERSISTENT = 2;
}
