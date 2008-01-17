package com.sun.enterprise.v3.server;

/**
 * Indicates a fatal problem during the boot sequence.
 *
 * This exception will prevent GlassFish from launching.
 *
 * @author Kohsuke Kawaguchi
 */
public class BootError extends Error {
    public BootError(String message) {
        super(message);
    }

    public BootError(String message, Throwable cause) {
        super(message, cause);
    }
}
