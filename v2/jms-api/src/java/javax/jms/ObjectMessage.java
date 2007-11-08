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

/** An <CODE>ObjectMessage</CODE> object is used to send a message that contains
  * a serializable object in the Java programming language ("Java object").
  * It inherits from the <CODE>Message</CODE> interface and adds a body
  * containing a single reference to an object. Only <CODE>Serializable</CODE> 
  * Java objects can be used.
  *
  * <P>If a collection of Java objects must be sent, one of the 
  * <CODE>Collection</CODE> classes provided since JDK 1.2 can be used.
  *
  * <P>When a client receives an <CODE>ObjectMessage</CODE>, it is in read-only 
  * mode. If a client attempts to write to the message at this point, a 
  * <CODE>MessageNotWriteableException</CODE> is thrown. If 
  * <CODE>clearBody</CODE> is called, the message can now be both read from and 
  * written to.
  *
  * @version     1.0 - 6 August 1998
  * @author      Mark Hapner
  * @author      Rich Burridge
  *
  * @see         javax.jms.Session#createObjectMessage()
  * @see         javax.jms.Session#createObjectMessage(Serializable)
  * @see         javax.jms.BytesMessage
  * @see         javax.jms.MapMessage
  * @see         javax.jms.Message
  * @see         javax.jms.StreamMessage
  * @see         javax.jms.TextMessage
  */

public interface ObjectMessage extends Message {

    /** Sets the serializable object containing this message's data.
      * It is important to note that an <CODE>ObjectMessage</CODE>
      * contains a snapshot of the object at the time <CODE>setObject()</CODE>
      * is called; subsequent modifications of the object will have no 
      * effect on the <CODE>ObjectMessage</CODE> body.
      *
      * @param object the message's data
      *  
      * @exception JMSException if the JMS provider fails to set the object
      *                         due to some internal error.
      * @exception MessageFormatException if object serialization fails.
      * @exception MessageNotWriteableException if the message is in read-only
      *                                         mode.
      */

    void 
    setObject(Serializable object) throws JMSException;


    /** Gets the serializable object containing this message's data. The 
      * default value is null.
      *
      * @return the serializable object containing this message's data
      *  
      * @exception JMSException if the JMS provider fails to get the object
      *                         due to some internal error.
      * @exception MessageFormatException if object deserialization fails.
      */

    Serializable 
    getObject() throws JMSException;
}
