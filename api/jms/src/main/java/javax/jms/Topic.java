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


/** A <CODE>Topic</CODE> object encapsulates a provider-specific topic name. 
  * It is the way a client specifies the identity of a topic to JMS API methods.
 * For those methods that use a <CODE>Destination</CODE> as a parameter, a 
  * <CODE>Topic</CODE> object may used as an argument . For 
  * example, a Topic can be used to create a <CODE>MessageConsumer</CODE>
  * and a <CODE>MessageProducer</CODE>
  * by calling:
  *<UL>
  *<LI> <CODE>Session.CreateConsumer(Destination destination)</CODE>
  *<LI> <CODE>Session.CreateProducer(Destination destination)</CODE>
  *
  *</UL>
  *
  * <P>Many publish/subscribe (pub/sub) providers group topics into hierarchies 
  * and provide various options for subscribing to parts of the hierarchy. The 
  * JMS API places no restriction on what a <CODE>Topic</CODE> object 
  * represents. It may be a leaf in a topic hierarchy, or it may be a larger 
  * part of the hierarchy.
  *
  * <P>The organization of topics and the granularity of subscriptions to 
  * them is an important part of a pub/sub application's architecture. The JMS 
  * API 
  * does not specify a policy for how this should be done. If an application 
  * takes advantage of a provider-specific topic-grouping mechanism, it 
  * should document this. If the application is installed using a different 
  * provider, it is the job of the administrator to construct an equivalent 
  * topic architecture and create equivalent <CODE>Topic</CODE> objects.
  *
  * @version     1.1 - February 2, 2002
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author      Kate Stout
  *
  * @see        Session#createConsumer(Destination)
  * @see        Session#createProducer(Destination)
  * @see        javax.jms.TopicSession#createTopic(String)
  */
 
public interface Topic extends Destination {

    /** Gets the name of this topic.
      *  
      * <P>Clients that depend upon the name are not portable.
      *  
      * @return the topic name
      *  
      * @exception JMSException if the JMS provider implementation of 
      *                         <CODE>Topic</CODE> fails to return the topic
      *                         name due to some internal
      *                         error.
      */ 

    String
    getTopicName() throws JMSException;


    /** Returns a string representation of this object.
      *
      * @return the provider-specific identity values for this topic
      */

    String
    toString();
}
