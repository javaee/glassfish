package org.glassfish.internal.api;

import org.jvnet.hk2.annotations.*;

/**
 * Manage a local password, which is a cryptographically secure random number
 * stored in a file with permissions that only allow the owner to read it.
 * A new local password is generated each time the server starts.  The
 * asadmin client can use it to authenticate when executing local commands,
 * such as stop-domain, without the user needing to supply a password.
 *
 * @author Bill Shannon
 */
@Contract
public interface LocalPassword {

    /**
     * Is the given password the local password?
     * @param password the password to test
     * @return true if it is a local password, false otherwise
     */
    public boolean isLocalPassword(String password);
}
