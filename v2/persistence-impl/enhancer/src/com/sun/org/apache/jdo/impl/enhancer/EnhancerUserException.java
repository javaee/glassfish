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
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.impl.enhancer;

import com.sun.org.apache.jdo.util.I18NHelper;

/**
 * Thrown to indicate that the class-file enhancer failed to perform an
 * operation due to an error.  The enhancer is guaranteed to remain in a
 * consistent state.
 *
 * @author Michael Bouschen
 */
public class EnhancerUserException
    extends Exception
{
    /** The Throwable that caused this exception to be thrown. */
    private Throwable cause;

    /** Flag indicating whether printStackTrace is being executed. */
    private boolean inPrintStackTrace = false;

    /** I18N support */
    private static I18NHelper msg = 
        I18NHelper.getInstance("com.sun.org.apache.jdo.impl.enhancer.Bundle");

    /**
     * Creates a new <code>EnhancerUserException</code> without detail
     * message. 
     */
    public EnhancerUserException() 
    {
    }
    
    /**
     * Creates a new <code>EnhancerUserException</code> with the specified
     * detail message.
     * @param message the detail message.
     */
    public EnhancerUserException(String message)
    {
        super(message);
    }

    /** 
     * Creates a new <code>EnhancerUserException</code> with the specified 
     * detail message and cause Throwable.
     * @param message the detail message.
     * @param cause the cause (which is saved for later retrieval by the
     * {@link #getCause()} method). (A null value is permitted, and
     * indicates that the cause is nonexistent or unknown.) 
     */
    public EnhancerUserException(String message, Throwable cause) 
    {
        super(message);
        this.cause = cause;
    }

    /** 
     * Returns the cause of this Exception or null if the cause is
     * nonexistent or unknown. (The cause is the Throwable that caused this 
     * Exception to get thrown.) 
     * @return the cause of this Exception or null if the cause is
     * nonexistent or unknown. 
     */
    public synchronized Throwable getCause() 
    {
        // super.printStackTrace calls getCause to handle the cause. 
        // Returning null prevents the superclass from handling the cause;
        // instead the local implementation of printStackTrace should
        // handle the cause. Otherwise, the cause is printed twice.
        return inPrintStackTrace ? null : cause;
    }

    /**
     * Initializes the cause of this throwable to the specified value. (The
     * cause is the Throwable that caused this Exception to get thrown.) 
     * @param cause the cause (which is saved for later retrieval by the
     * {@link #getCause()} method). (A null value is permitted, and
     * indicates that the cause is nonexistent or unknown.)
     * @return a reference to this <code>EnhancerUserException</code> 
     * instance.
     */
    public Throwable initCause(Throwable cause)
    {
        this.cause = cause;
        return this;
    }
    
    /** 
     * The <code>String</code> representation includes the name of the class,
     * the descriptive comment (if any),
     * and the <code>String</code> representation of the cause (if any). 
     * @return the <code>String</code>.
     */
    public synchronized String toString() 
    {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        // Do not include cause information, if called by printStackTrace;
        // the stacktrace will include the cause anyway.
        if ((cause != null) && !inPrintStackTrace) {
            sb.append("\n");  //NOI18N
            sb.append(msg.msg("MSG_CauseThrowable")); //NOI18N
            sb.append("\n");  //NOI18N
            sb.append(cause.toString()); //NOI18N
        }
        return sb.toString();
    }
  
    /**
     * Prints this <code>EnhancerUserException</code> and its backtrace to the 
     * standard error output.
     * Print cause Throwable's stack trace as well.
     */
    public void printStackTrace()
    {
        printStackTrace(System.err);
    }

    /**
     * Prints this <code>EnhancerUserException</code> and its backtrace to the 
     * specified print stream.
     * Print cause Throwable's stack trace as well.
     * @param s <code>PrintStream</code> to use for output
     */
    public synchronized void printStackTrace(java.io.PrintStream s) 
    { 
        synchronized (s) {
            inPrintStackTrace = true;
            super.printStackTrace(s);
            if (cause != null) {
                s.println(msg.msg("MSG_CauseThrowableStackTrace")); //NOI18N
                cause.printStackTrace(s);
            }
            inPrintStackTrace = false;
        }
    }
    
    /**
     * Prints this <code>EnhancerUserException</code> and its backtrace to the specified
     * print writer.
     * Print cause Throwable' stack trace as well.
     * @param s <code>PrintWriter</code> to use for output
     */
    public synchronized void printStackTrace(java.io.PrintWriter s) 
    { 
        synchronized (s) {
            inPrintStackTrace = true;
            super.printStackTrace(s);
            if (cause != null) {
                s.println(msg.msg("MSG_CauseThrowableStackTrace") + ' '); //NOI18N
                cause.printStackTrace(s);
            }
            inPrintStackTrace = false;
        }
    }
    
}
