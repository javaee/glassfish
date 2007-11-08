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


/** A <CODE>Queue</CODE> object encapsulates a provider-specific queue name. 
  * It is the way a client specifies the identity of a queue to JMS API methods.
  * For those methods that use a <CODE>Destination</CODE> as a parameter, a 
  * <CODE>Queue</CODE> object used as an argument. For example, a queue can
  * be used  to create a <CODE>MessageConsumer</CODE> and a 
  * <CODE>MessageProducer</CODE>  by calling:
  *<UL>
  *<LI> <CODE>Session.CreateConsumer(Destination destination)</CODE>
  *<LI> <CODE>Session.CreateProducer(Destination destination)</CODE>
  *
  *</UL>
  *
  * <P>The actual length of time messages are held by a queue and the 
  * consequences of resource overflow are not defined by the JMS API.
  *
  *
  *
  * @version     1.1 February 2 - 2000
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author      Kate Stout
  *
  * @see Session#createConsumer(Destination)
  * @see Session#createProducer(Destination)
  * @see Session#createQueue(String)
  * @see QueueSession#createQueue(String)
  */
 
public interface Queue extends Destination { 

    /** Gets the name of this queue.
      *  
      * <P>Clients that depend upon the name are not portable.
      *  
      * @return the queue name
      *  
      * @exception JMSException if the JMS provider implementation of 
      *                         <CODE>Queue</CODE> fails to return the queue
      *                         name due to some internal
      *                         error.
      */ 
 
    String
    getQueueName() throws JMSException;  


    /** Returns a string representation of this object.
      *
      * @return the provider-specific identity values for this queue
      */
 
    String
    toString();
}
