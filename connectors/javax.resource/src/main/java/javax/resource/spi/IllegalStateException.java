/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package javax.resource.spi;

/**
 * An <code>IllegalStateException</code> 
 * is thrown from a method if the callee (resource
 * adapter or application server for system contracts) is in an illegal or
 * inappropriate state for the method invocation.
 *
 * @version 1.0
 * @author Rahul Sharma
 * @author Ram Jeyaraman
 */

public class IllegalStateException extends javax.resource.ResourceException {

    /**
     * Constructs a new instance with null as its detail message.
     */
    public IllegalStateException() { super(); }

    /**
     * Constructs a new instance with the specified detail message.
     *
     * @param message the detail message.
     */
    public IllegalStateException(String message) {
	super(message);
    }

    /**
     * Constructs a new throwable with the specified cause.
     *
     * @param cause a chained exception of type <code>Throwable</code>.
     */
    public IllegalStateException(Throwable cause) {
	super(cause);
    }

    /**
     * Constructs a new throwable with the specified detail message and cause.
     *
     * @param message the detail message.
     *
     * @param cause a chained exception of type <code>Throwable</code>.
     */
    public IllegalStateException(String message, Throwable cause) {
	super(message, cause);
    }

    /**
     * Constructs a new throwable with the specified detail message and
     * an error code.
     *
     * @param message a description of the exception.
     * @param errorCode a string specifying the vendor specific error code.
     */
    public IllegalStateException(String message, String errorCode) {
	super(message, errorCode);
    }
}
