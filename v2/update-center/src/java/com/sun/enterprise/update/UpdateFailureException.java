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
package com.sun.enterprise.update;

/**
 * An exception that provides information on update failure.
 *
 * @author Satish Viswanatham
 */
public class UpdateFailureException extends Exception {

    /**
     * Constructs an update failure exception with the specified message
     * and cause.
     *
     * @param   msg    the detail message for this exception
     * @param   cause  the cause of this error
     */
    public UpdateFailureException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs an update failure exception with the cause.
     *
     * @param   cause  the cause of this error
     */
    public UpdateFailureException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an update failure exception with the specified message.
     *
     * @param   msg    the detail message for this exception
     */
    public UpdateFailureException(String msg) {
        super(msg);
    }

    /**
     * Constructs an update failure exception.
     */
    public UpdateFailureException() {
        super();
    }
}
