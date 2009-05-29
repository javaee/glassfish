package org.glassfish.enterprise.admin.ncli;

/** A generic command exception that might be thrown from the process of running a command.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net) (km@dev.java.net)
 */
public class GenericCommandException extends Exception {

    public GenericCommandException(String message) {
        super(message);
    }

    public GenericCommandException(String message, Throwable cause) {
        super(message, cause);
    }

}
