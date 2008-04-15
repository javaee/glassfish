/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.security.jauth;

/**
 * Authentication failed.
 *
 * <p> This exception is thrown by an AuthModule when authentication failed.
 * This exception is only thrown when the module has updated
 * the response message in the AuthParam.
 *
 * @version %I%, %G%
 */
public class FailureException extends AuthException {

    private static final long serialVersionUID = -6634814390418917726L;

    /**
     * Constructs a FailureException with no detail message. A detail
     * message is a String that describes this particular exception.
     */
    public FailureException() {
	super();
    }

    /**
     * Constructs a FailureException with the specified detail message.
     * A detail message is a String that describes this particular
     * exception.
     *
     * @param msg the detail message.
     */
    public FailureException(String msg) {
	super(msg);
    }
}

