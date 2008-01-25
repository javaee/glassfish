package com.sun.hk2.component;

import static com.sun.hk2.component.InhabitantsFile.COMPANION_CLASS_KEY;
import org.jvnet.hk2.annotations.CagedBy;
import org.jvnet.hk2.annotations.CompanionOf;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Index;
import org.jvnet.hk2.annotations.InhabitantAnnotation;
import org.jvnet.hk2.annotations.InhabitantMetadata;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.CageBuilder;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
@CagedBy(CompanionSeed.Registerer.class)
public @interface CompanionSeed {
    /**
     * The type of the lead class.
     */
    @Index
    @InhabitantMetadata("lead")
    Class<?> lead();

    /**
     * Other metadata to capture the companion class.
     */
    @InhabitantMetadata("companionClass")
    Class<?> companion();

    /**
     * When {@link CompanionSeed} enters habitat, look
     * for existing inhabitants and make sure all of them
     * get its corresponding companions from this new seed.
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
            for (Inhabitant lead : habitat.getInhabitantsByType(i.metadata().getOne("lead")))
                lead.setCompanions(cons(lead.companions(),createCompanion(habitat,lead,i)));
        }

        /**
         * Allocates a new read-only list by adding one more element.
         */
        private <T> List<T> cons(Collection<T> list, T oneMore) {
            int sz = list.size();
            Object[] a = list.toArray(new Object[sz+1]);
            a[sz]=oneMore;
            return (List) Arrays.asList(a);
        }

        /**
         * Creates a companion inhabitant from the inhabitant of a {@link CompanionSeed},
         * to be associated with a lead component.
         */
        public static LazyInhabitant createCompanion(Habitat habitat, final Inhabitant<?> lead, final Inhabitant<?> seed) {
            Holder<ClassLoader> cl = new Holder<ClassLoader>() {
                    public ClassLoader get() {
                        return seed.type().getClassLoader();
                    }
                };
            LazyInhabitant ci = new LazyInhabitant(habitat, cl, seed.metadata().getOne(COMPANION_CLASS_KEY), MultiMap.<String,String>emptyMap()) {
                public Inhabitant lead() {
                    return lead;
                }
            };
            habitat.add(ci);
            return ci;
        }
    }
}
