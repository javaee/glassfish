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

package com.sun.enterprise.admin.common.domains.registry;
/**
   Instances of this class represent an exception from the domain
   registry abstraction.
   <p>
   Subclasses indicate specific issues from that abstraction
*/
import java.security.PrivilegedActionException;

public class DomainRegistryException extends Exception
{
	  /**
		 Constructs a new exception with null as its detail
   message. The cause is not initialized, and may subsequently be
   initialized by a call to  {@link Throwable#initCause(Throwable)}
	  */

  public DomainRegistryException(){
	super();
  }

	  /**
		 Constructs a new exception with the specified detail
		 message. The cause is not initialized, and may subsequently
		 be initialized by a call to {@link Throwable#initCause(Throwable)}

		 @param message - the detail message. The detail message is
		 saved for later retrieval by the {@link Throwable#getMessage()} method.
	  */
  public DomainRegistryException(String message){
	super(message);
  }

	  /**
		 Constructs a new exception with the specified detail message and cause. 
		 <p>
		 Note that the detail message associated with cause is not
		 automatically incorporated in this exception's detail
		 message.

		 @param message - the detail message (which is saved for later
		 retrieval by the {@link Throwable#getMessage()} method).
		 @param cause - the cause (which is saved for later retrieval by the
		 {@link Throwable#getCause()} method). (A null value is
		 permitted, and indicates that the cause is nonexistent or
		 unknown.)
	  */
  public DomainRegistryException(String message,
				   Throwable cause){
    super(message+" "+cause.getMessage());
    this.cause = cause;
  }

	  /**
		 Constructs a new exception with the specified cause detail
		 message. The 
		 detail message will be <code>(cause==null ? null :
		 cause.toString())</code> (which typically contains the class
		 and detail message of cause). This constructor is useful for
		 exceptions that are little more than wrappers for other
		 throwables (for example, {@link PrivilegedActionException}).

		 @param cause - the cause (which is saved for later retrieval
		 by the {@link Throwable#getCause()} method). (A null value is
		 permitted, and indicates that the cause is nonexistent or
		 unknown.)
	  */
  public DomainRegistryException(Throwable cause){
    super(cause.getMessage());
    this.cause = cause;
  }

  private Throwable cause;
		 
}
