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
