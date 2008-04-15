/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.hk2.component;

import static com.sun.hk2.component.InhabitantsFile.COMPANION_CLASS_KEY;
import static com.sun.hk2.component.InhabitantsFile.COMPANION_CLASS_METADATA_KEY;
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

            // this is the name of the class with @CompanionOf
            String fqcn = seed.metadata().getOne(COMPANION_CLASS_KEY);

            String metadataLine = seed.metadata().getOne(COMPANION_CLASS_METADATA_KEY);
            MultiMap<String,String> metadata;
            if(metadataLine==null)
                metadata = MultiMap.emptyMap();
            else
                metadata = InhabitantsParser.buildMetadata(new KeyValuePairParser(metadataLine));

            LazyInhabitant ci = new LazyInhabitant(habitat, cl, fqcn, metadata) {
                public Inhabitant lead() {
                    return lead;
                }
            };
            habitat.add(ci);
            return ci;
        }
    }
}
