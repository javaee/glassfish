package org.jvnet.hk2.config;

/**
 * Constant names used in the metadata for configurable inhabitants.
 *
 * @author Kohsuke Kawaguchi
 */
public class ConfigMetadata {
    /**
     * Fully qualified name of the target class that an injector works with.
     */
    public static final String TARGET = "target";

    /**
     * Contracts that the target type implements.
     */
    public static final String TARGET_CONTRACTS = "target-contracts";

    /**
     * If the {@link #TARGET target type} is keyed, the FQCN that defines
     * the key field. This type is always assignable from the target type.
     *
     * This allows a symbol space to be defined on a base class B, and
     * different subtypes can participate.
     */
    public static final String KEYED_AS = "keyed-as";

    /**
     * The name of the property used as a key for exposing inhabitants,
     * as well as resolving references.
     *
     * <p>
     * This is either "@attr" or "&lt;element>" indicating
     * where the key is read.
     * 
     * @see Attribute#key()
     * @see Element#key()
     */
    public static final String KEY = "key";
}
