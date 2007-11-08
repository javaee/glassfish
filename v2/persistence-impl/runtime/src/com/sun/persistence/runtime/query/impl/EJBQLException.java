/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */



/*
 * EJBQLException.java
 *
 * Created on November 12, 2001
 */


package com.sun.persistence.runtime.query.impl;

/**
 * This class represents errors reported by the EJBQL compiler.
 * @author Michael Bouschen
 */
public class EJBQLException extends RuntimeException {
    /**
     * The Throwable that caused this EJBQLException.
     */
    Throwable cause;

    /**
     * Creates a new <code>EJBQLException</code> without detail message.
     */
    public EJBQLException() {
    }

    /**
     * Constructs a new <code>EJBQLException</code> with the specified detail
     * message.
     * @param msg the detail message.
     */
    public EJBQLException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new <code>EJBQLException</code> with the specified detail
     * message and cause.
     * @param msg the detail message.
     * @param cause the cause <code>Throwable</code>.
     */
    public EJBQLException(String msg, Throwable cause) {
        super(msg);
        this.cause = cause;
    }

    /**
     * Returns the cause of this <code>EJBQLException</code> or
     * <code>null</code> if the cause is nonexistent or unknown.
     * @return the cause of this or <code>null</code> if the cause is
     *         nonexistent or unknown.
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * The <code>String</code> representation includes the name of the class,
     * the descriptive comment (if any), and the <code>String</code>
     * representation of the cause <code>Throwable</code> (if any).
     * @return the <code>String</code>.
     */
    public String toString() {
        // calculate approximate size of the String to return
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        // include cause Throwable information
        if (cause != null) {
            sb.append("\n");  //NOI18N
            sb.append("Nested exception"); //NOI18N
            sb.append("\n");  //NOI18N
            sb.append(cause.toString());
        }
        return sb.toString();
    }
}
