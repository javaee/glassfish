/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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
 
package javax.servlet.jsp;

/**
 * A generic exception known to the JSP engine; uncaught
 * JspExceptions will result in an invocation of the errorpage
 * machinery.
 */

public class JspException extends Exception {

    private Throwable rootCause;


    /**
     * Construct a JspException.
     */
    public JspException() {
    }


    /**
     * Constructs a new JSP exception with the
     * specified message. The message can be written 
     * to the server log and/or displayed for the user. 
     *
     * @param msg 		a <code>String</code> 
     *				specifying the text of 
     *				the exception message
     *
     */
    public JspException(String msg) {
	super(msg);
    }


    /**
     * Constructs a new JSP exception when the JSP 
     * needs to throw an exception and include a message 
     * about the "root cause" exception that interfered with its 
     * normal operation, including a description message.
     *
     *
     * @param message 		a <code>String</code> containing 
     *				the text of the exception message
     *
     * @param rootCause		the <code>Throwable</code> exception 
     *				that interfered with the servlet's
     *				normal operation, making this servlet
     *				exception necessary
     *
     */
    
    public JspException(String message, Throwable rootCause) {
	super(message);
	this.rootCause = rootCause;
    }


    /**
     * Constructs a new JSP exception when the JSP 
     * needs to throw an exception and include a message
     * about the "root cause" exception that interfered with its
     * normal operation.  The exception's message is based on the localized
     * message of the underlying exception.
     *
     * <p>This method calls the <code>getLocalizedMessage</code> method
     * on the <code>Throwable</code> exception to get a localized exception
     * message. When subclassing <code>JspException</code>, 
     * this method can be overridden to create an exception message 
     * designed for a specific locale.
     *
     * @param rootCause 	the <code>Throwable</code> exception
     * 				that interfered with the JSP's
     *				normal operation, making the JSP exception
     *				necessary
     *
     */

    public JspException(Throwable rootCause) {
	super(rootCause.getLocalizedMessage());
	this.rootCause = rootCause;
    }

    
    /**
     * Returns the exception that caused this JSP exception.
     *
     *
     * @return			the <code>Throwable</code> 
     *				that caused this JSP exception
     *
     */
    
    public Throwable getRootCause() {
	return rootCause;
    }
}
