/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.exceptions;

import java.io.*;
import oracle.toplink.essentials.internal.helper.JavaPlatform;
import oracle.toplink.essentials.exceptions.i18n.ExceptionMessageGenerator;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p>
 * <b>Purpose</b>: Any exception raised by TopLink should be a subclass of this exception class.
 */
public abstract class TopLinkException extends RuntimeException {
    protected transient AbstractSession session;
    protected Throwable internalException;
    protected static Boolean shouldPrintInternalException = null;
    protected String indentationString;
    protected int errorCode;
    protected static final String CR = System.getProperty("line.separator");
    //Bug#3559280  Added to avoid logging an exception twice
    protected boolean hasBeenLogged;

    /**
     * INTERNAL:
     * Return a new exception.
     */
    public TopLinkException() {
        this("");
    }

    /**
     * INTERNAL:
     * TopLink exception should only be thrown by TopLink.
     */
    public TopLinkException(String theMessage) {
        super(theMessage);
        this.indentationString = "";
        hasBeenLogged = false;
    }

    /**
     * INTERNAL:
     * TopLink exception should only be thrown by TopLink.
     */
    public TopLinkException(String message, Throwable internalException) {
        this(message);
        setInternalException(internalException);
    }

    /**
     * INTERNAL:
     * Convenience method - return a platform-specific line-feed.
     */
    protected static String cr() {
        return oracle.toplink.essentials.internal.helper.Helper.cr();
    }

    /**
     * PUBLIC:
     * Return the exception error code.
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * INTERNAL:
     * Used to print things nicely in the testing tool.
     */
    public String getIndentationString() {
        return indentationString;
    }

    /**
     * PUBLIC:
     * Return the internal native exception.
     * TopLink frequently catches Java exceptions and wraps them in its own exception
     * classes to provide more information.
     * The internal exception can still be accessed if required.
     */
    public Throwable getInternalException() {
        return internalException;
    }

    /**
     * PUBLIC:
     * Return the exception error message.
     * TopLink error messages are multi-line so that detail descriptions of the exception are given.
     */
    public String getMessage() {
        StringWriter writer = new StringWriter(100);

        // Avoid printing internal exception error message twice.
        if ((getInternalException() == null) || (!super.getMessage().equals(getInternalException().toString()))) {
            writer.write(cr());
            writer.write(getIndentationString());
            writer.write(ExceptionMessageGenerator.getHeader("DescriptionHeader"));
            writer.write(super.getMessage());
        }

        if (getInternalException() != null) {
            writer.write(cr());
            writer.write(getIndentationString());
            writer.write(ExceptionMessageGenerator.getHeader("InternalExceptionHeader"));
            writer.write(getInternalException().toString());

            if ((getInternalException() instanceof java.lang.reflect.InvocationTargetException) && ((((java.lang.reflect.InvocationTargetException)getInternalException()).getTargetException()) != null)) {
                writer.write(cr());
                writer.write(getIndentationString());
                writer.write(ExceptionMessageGenerator.getHeader("TargetInvocationExceptionHeader"));
                writer.write(((java.lang.reflect.InvocationTargetException)getInternalException()).getTargetException().toString());
            }
        }

        return writer.toString();
    }

    /**
     * PUBLIC:
     * Return the session.
     */
    public AbstractSession getSession() {
        return session;
    }

    /**
     * INTERNAL:
     * Return if this exception has been logged to avoid being logged more than once.
     */
    public boolean hasBeenLogged() {
        return hasBeenLogged;
    }

    /**
     * PUBLIC:
     * Print both the normal and internal stack traces.
     */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * PUBLIC:
     * Print both the normal and internal stack traces.
     */
    public void printStackTrace(PrintStream outStream) {
        printStackTrace(new PrintWriter(outStream));
    }

    /**
     * PUBLIC:
     * Print both the normal and internal stack traces.
     */
    public void printStackTrace(PrintWriter writer) {
        writer.write(ExceptionMessageGenerator.getHeader("LocalExceptionStackHeader"));
        writer.write(cr());
        super.printStackTrace(writer);

        if ((getInternalException() != null) && shouldPrintInternalException()) {
            writer.write(ExceptionMessageGenerator.getHeader("InternalExceptionStackHeader"));
            writer.write(cr());
            getInternalException().printStackTrace(writer);

            if ((getInternalException() instanceof java.lang.reflect.InvocationTargetException) && ((((java.lang.reflect.InvocationTargetException)getInternalException()).getTargetException()) != null)) {
                writer.write(ExceptionMessageGenerator.getHeader("TargetInvocationExceptionStackHeader"));
                writer.write(cr());
                ((java.lang.reflect.InvocationTargetException)getInternalException()).getTargetException().printStackTrace(writer);
            }
        }
        writer.flush();
    }

    /**
     * INTERNAL:
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * INTERNAL:
     * Set this flag to avoid logging an exception more than once.
     */
    public void setHasBeenLogged(boolean logged) {
        this.hasBeenLogged = logged;
    }

    /**
     * INTERNAL:
     * Used to print things nicely in the testing tool.
     */
    public void setIndentationString(String indentationString) {
        this.indentationString = indentationString;
    }

    /**
     * INTERNAL:
     * Used to specify the internal exception.
     */
    public void setInternalException(Throwable anException) {
        internalException = anException;
        JavaPlatform.setExceptionCause(this, anException);
    }

    /**
     *  INTERNAL:
     */
    public void setSession(AbstractSession session) {
        this.session = session;
    }

    /**
     * PUBLIC:
     * Allows overiding of TopLink's exception chaining detection.
     * @param booleam printException - If printException is true, the TopLink-stored
     * Internal exception will be included in a stack traceor in the exception message of a TopLinkException.
     * If printException is false, the TopLink-stored Internal Exception will not be included
     * in the stack trace or the exception message of TopLinkExceptions
     */
    public static void setShouldPrintInternalException(boolean printException) {
        shouldPrintInternalException = new Boolean(printException);
    }

    /**
     * INTERNAL
     * Check to see if the TopLink-stored internal exception should be printed in this
     * a TopLinkException's stack trace.  This method will check the static ShouldPrintInternalException
     * variable and if it is not set, estimate based on the JDK version used.
     */
    public static boolean shouldPrintInternalException() {
        if (shouldPrintInternalException == null) {
            shouldPrintInternalException = new Boolean(JavaPlatform.shouldPrintInternalException());
        }
        return shouldPrintInternalException.booleanValue();
    }

    /**
     * INTERNAL:
     */
    public String toString() {
        return getIndentationString() + ExceptionMessageGenerator.getHeader("ExceptionHeader") + getErrorCode() + "] (" + oracle.toplink.essentials.sessions.DatabaseLogin.getVersion() + "): " + getClass().getName() + getMessage();
    }
}
