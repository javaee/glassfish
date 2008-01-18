package com.sun.hk2.component;

import org.jvnet.hk2.annotations.CompanionOf;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Index;
import org.jvnet.hk2.annotations.InhabitantAnnotation;

/**
 * Marks the companion relationship with another component.
 *
 * <p>
 * Implementations of this contract are generated for each component
 * with the {@link CompanionOf} annotation.  
 *
 * @author Kohsuke Kawaguchi
 */
@Contract
@InhabitantAnnotation("default")
public @interface CompanionSeed {
    /**
     * The type of the lead class.
     */
    @Index
    Class<?> lead();

    /**
     * Other metadata to capture the companion class.
     */
    String metadata();
}
