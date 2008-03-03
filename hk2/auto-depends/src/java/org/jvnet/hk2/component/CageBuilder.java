package org.jvnet.hk2.component;

import org.jvnet.hk2.annotations.CagedBy;
import org.jvnet.hk2.annotations.Contract;

/**
 * Receives notifications when a component with {@link CagedBy}
 * is entered into habitat.
 *
 * <p>
 * The concrete registration hook classes are instanciated as components
 * themselves, so in that way sub-classes can obtain contextual information,
 * such as {@link Habitat}, via injection. 
 *
 * @author Kohsuke Kawaguchi
 * @see CagedBy
 */
@Contract
@CagedBy(CageBuilder.Registerer.class)
public interface CageBuilder {
    /**
     * Called when an {@link Inhabitant} is entered into habitat.
     */
    void onEntered(Inhabitant<?> i);

    /**
     * When {@link CageBuilder} enters habitat, look
     * for existing inhabitants that were supposed to be
     * caged by this and cage them all.
     *
     * <p>
     * This is because we don't generally know which one comes first
     * into the habitat &mdash; cage builder or caged component.
     *
     * <p>
     * Normal {@link CageBuilder}s register themselves as components,
     * but this one is defined in HK2 so we have to get in by a special means.
     */
    public static final class Registerer implements CageBuilder {
        private final Habitat habitat;

        public Registerer(Habitat habitat) {
            this.habitat = habitat;
        }

        public void onEntered(Inhabitant<?> i) {
            // and if a companion seed is added to something else, make sure
            // existing lead inhabitants will get this as a companion
            assert i.metadata()!=null;
            for (Inhabitant supposedToBeCaged : habitat.getInhabitants(CagedBy.class,i.typeName()))
                ((CageBuilder)i.get()).onEntered(supposedToBeCaged);
        }
    }
}
