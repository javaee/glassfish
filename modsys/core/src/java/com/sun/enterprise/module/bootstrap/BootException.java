package com.sun.enterprise.module.bootstrap;

/**
 * Signals a fatal error in the module system launch.
 *
 * @author Kohsuke Kawaguchi
 */
public class BootException extends Exception {
    public BootException(String message) {
        super(message);
    }

    public BootException(String message, Throwable cause) {
        super(message, cause);
    }

    public BootException(Throwable cause) {
        super(cause);
    }
}
