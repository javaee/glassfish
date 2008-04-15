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

/**
 * <P>This is the root class of all JMS API exceptions.
 *
 * <P>It provides the following information:
 * <UL>
 *   <LI> A provider-specific string describing the error. This string is 
 *        the standard exception message and is available via the
 *        <CODE>getMessage</CODE> method.
 *   <LI> A provider-specific string error code 
 *   <LI> A reference to another exception. Often a JMS API exception will 
 *        be the result of a lower-level problem. If appropriate, this 
 *        lower-level exception can be linked to the JMS API exception.
 * </UL>
 * @version     1.0 - 5 Oct 1998
 * @author      Mark Hapner
 * @author      Rich Burridge
 **/

public class JMSException extends Exception {

  /** Vendor-specific error code.
  **/
  private String errorCode;

  /** <CODE>Exception</CODE> reference.
  **/
  private Exception linkedException;


  /** Constructs a <CODE>JMSException</CODE> with the specified reason and 
   *  error code.
   *
   *  @param  reason        a description of the exception
   *  @param  errorCode     a string specifying the vendor-specific
   *                        error code
   **/
  public 
  JMSException(String reason, String errorCode) {
    super(reason);
    this.errorCode = errorCode;
    linkedException = null;
  }

  /** Constructs a <CODE>JMSException</CODE> with the specified reason and with
   *  the error code defaulting to null.
   *
   *  @param  reason        a description of the exception
   **/
  public 
  JMSException(String reason) {
    super(reason);
    this.errorCode = null;
    linkedException = null;
  }

  /** Gets the vendor-specific error code.
   *  @return   a string specifying the vendor-specific
   *                        error code
  **/
  public 
  String getErrorCode() {
    return this.errorCode;
  }

  /**
   * Gets the exception linked to this one.
   *
   * @return the linked <CODE>Exception</CODE>, null if none
  **/
  public 
  Exception getLinkedException() {
    return (linkedException);
  }

  /**
   * Adds a linked <CODE>Exception</CODE>.
   *
   * @param ex       the linked <CODE>Exception</CODE>
  **/
  public 
  synchronized void setLinkedException(Exception ex) {
      linkedException = ex;
  }
}
