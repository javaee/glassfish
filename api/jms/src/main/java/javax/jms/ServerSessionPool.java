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

/** A <CODE>ServerSessionPool</CODE> object is an object implemented by an 
  * application server to provide a pool of <CODE>ServerSession</CODE> objects 
  * for processing the messages of a <CODE>ConnectionConsumer</CODE> (optional).
  *
  * <P>Its only method is <CODE>getServerSession</CODE>. The JMS API does not 
  * architect how the pool is implemented. It could be a static pool of 
  * <CODE>ServerSession</CODE> objects, or it could use a sophisticated 
  * algorithm to dynamically create <CODE>ServerSession</CODE> objects as 
  * needed.
  *
  * <P>If the <CODE>ServerSessionPool</CODE> is out of 
  * <CODE>ServerSession</CODE> objects, the <CODE>getServerSession</CODE> call 
  * may block. If a <CODE>ConnectionConsumer</CODE> is blocked, it cannot 
  * deliver new messages until a <CODE>ServerSession</CODE> is 
  * eventually returned.
  *
  * @version     1.0 - 9 March 1998
  * @author      Mark Hapner
  * @author      Rich Burridge
  *
  * @see javax.jms.ServerSession
  */

public interface ServerSessionPool {

    /** Return a server session from the pool.
      *
      * @return a server session from the pool
      *  
      * @exception JMSException if an application server fails to
      *                         return a <CODE>ServerSession</CODE> out of its
      *                         server session pool.
      */ 

    ServerSession
    getServerSession() throws JMSException;
}
