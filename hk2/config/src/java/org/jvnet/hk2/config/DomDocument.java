package org.jvnet.hk2.config;

/**
 * Represents a whole DOM tree.
 *
 * @author Kohsuke Kawaguchi
 */
public class DomDocument {
    /**
     * A hook to perform variable replacements on the attribute/element values
     * that are found in the configuration file.
     * The translation happens lazily when objects are actually created, not when
     * configuration is parsed, so this allows circular references &mdash;
     * {@link Translator} may refer to objects in the configuration file being read.
     */
    private volatile Translator translator = Translator.NOOP;

    /*package*/ Dom root;

    public Dom getRoot() {
        return root;
    }

    public Translator getTranslator() {
        return translator;
    }

    public void setTranslator(Translator translator) {
        this.translator = translator;
    }
}
