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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.admin.event;

/**
 * Admin event listener exception. This exception can be thrown by event
 * listeners to indicate failure while processing the event.
 */
public class AdminEventListenerException extends java.lang.Exception {

    /**
     * Creates a new instance of <code>AdminEventListenerException</code>
     * without detail message.
     */
    public AdminEventListenerException() {
    }

    /**
     * Constructs an instance of <code>AdminEventListenerException</code> with
     * the specified detail message.
     * @param msg the detail message.
     */
    public AdminEventListenerException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>AdminEventListenerException</code> with
     * the specified detail message and root cause.
     * @param msg the detail message.
     * @param cause root cause of the exception 
     */
    public AdminEventListenerException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs an instance of <code>AdminEventListenerException</code> with
     * the specified root cause. The message for the exception is derived from
     * cause.getMessage() if cause is not null, otherwise the message is null.
     * @param cause root cause of the exception 
     */
    public AdminEventListenerException(Throwable cause) {
        super(cause);
    }
}
