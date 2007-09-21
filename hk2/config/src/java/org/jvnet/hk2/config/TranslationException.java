package org.jvnet.hk2.config;

/**
 * Indicates a failure in {@link Translator#translate(String)}.
 *
 * <p>
 * This is an user error, so must be reported nicely.
 *  
 * @author Kohsuke Kawaguchi
 */
public class TranslationException extends ConfigurationException {
    public TranslationException(String message) {
        super(message);
    }

    public TranslationException(String message, Throwable cause) {
        super(message, cause);
    }
}
