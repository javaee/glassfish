package org.jvnet.hk2.component;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.CagedBy;

/**
 * Receives notifications when a component with {@link CagedBy}
 * is entered into habitat.
 *
 * <p>
 * The concrete registration hook classes are instanciated as components
 * themselves, so in that way sub-classes can obtain contextual information,
 * such as {@link Habitat}. 
 *
 * @author Kohsuke Kawaguchi
 * @see CagedBy
 */
@Contract
public interface CageBuilder {
    /**
     * Called when an {@link Inhabitant} is entered into habitat.
     */
    void onEntered(Inhabitant<?> i);
}
