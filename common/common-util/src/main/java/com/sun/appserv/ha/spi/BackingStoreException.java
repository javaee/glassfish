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

package com.sun.appserv.ha.spi;

/**
 * This exception is thrown to signal any BackingStore specific exception.
 * <code>BackingStore.getCause()</code> can be used to get the actual store
 * specific exception.
 */
public class BackingStoreException extends Exception {

    /**
     * Creates a BackingStoreException with null as its detail message. The
     * cause is not initialized, and may subsequently be initialized by a call
     * to <code>Throwable.initCause(java.lang.Throwable)</code>.
     */
    public BackingStoreException() {
    }

    /**
     * constructs a BackingStoreException with the specified detail message
     * 
     * @param message
     *            the detail message. The detail message is saved for later
     *            retrieval by the <code>Throwable.getMessage()</code> method.
     */
    public BackingStoreException(String message) {
        super(message);
    }

    /**
     * Constructs a new BackingStoreException exception with the specified cause
     * and a detail message of (cause==null ? null : cause.toString())
     * 
     * @param message
     *            the detail message. The detail message is saved for
     * @param th
     *            the cause
     */
    public BackingStoreException(String message, Throwable th) {
        super(message, th);
    }

}
