/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.glassfish.hk2.tests.locator.provider;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.tests.locator.utilities.TestModule;
import org.glassfish.hk2.utilities.BuilderHelper;

/**
 * @author jwells
 */
public class ProviderModule implements TestModule {

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Module#configure(org.glassfish.hk2.api.Configuration)
     */
    @Override
    public void configure(DynamicConfiguration configurator) {
        configurator.bind(BuilderHelper.link(InstantiationChecker.class).build());
        configurator.bind(BuilderHelper.link(ProviderInjected.class).build());
        
        // These are for the iterable provider tests
        configurator.bind(BuilderHelper.link(EliManning.class).
                to(Character.class).
                to(FootballCharacter.class).
                named(ProviderTest.ELI).
                qualifiedBy(Giants.class.getName()).build());
        
        configurator.bind(BuilderHelper.link(ShadyMcCoy.class).
                to(Character.class).
                to(FootballCharacter.class).
                named(ProviderTest.SHADY).
                qualifiedBy(Eagles.class.getName()).build());
        
        configurator.bind(BuilderHelper.link(Ishmael.class).
                to(Character.class).
                to(BookCharacter.class).
                named(ProviderTest.ISHMAEL).
                build());
        
        configurator.bind(BuilderHelper.link(QueeQueg.class).
                to(Character.class).
                to(BookCharacter.class).
                named(ProviderTest.QUEEQUEG).
                build());
        
        configurator.bind(BuilderHelper.link(Menagerie.class).build());
        
    }

}
