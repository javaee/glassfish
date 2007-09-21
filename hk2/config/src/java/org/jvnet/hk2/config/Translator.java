package org.jvnet.hk2.config;

/**
 * Used to perform string pre-processing on values found in the configuration file.
 *
 * <p>
 * This hook allows applications to support variable
 * expansions like Ant in the configuration file.
 *
 * @author Kohsuke Kawaguchi
 */
public interface Translator {
    String translate(String str) throws TranslationException;

    /**
     * {@link Translator} that does nothing.
     */
    public static final Translator NOOP = new Translator() {
        public String translate(String str) {
            return str;
        }
    };
}
