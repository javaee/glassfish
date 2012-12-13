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
package org.glassfish.hk2.tests.locator.locator;

import java.util.Set;

import javax.inject.Singleton;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.tests.locator.utilities.TestModule;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;

/**
 * @author jwells
 *
 */
public class LocatorModule implements TestModule {

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Module#configure(org.glassfish.hk2.api.Configuration)
     */
    @Override
    public void configure(DynamicConfiguration configurator) {
        configurator.bind(BuilderHelper.link(BootCommand.class).
                to(AdminCommand.class).
                named("BootCommand").
                build());
        
        configurator.bind(BuilderHelper.link(GetStatisticsCommand.class).
                to(AdminCommand.class).
                named("GetStatisticsCommand").
                build());
        
        configurator.bind(BuilderHelper.link(ShutdownCommand.class).
                to(AdminCommand.class).
                named("ShutdownCommand").
                build());
        
        // This is part of the test, to use a non-BuilderHelper descriptor
        ForeignDescriptor fd = new ForeignDescriptor();
        fd.setImplementation(FrenchService.class.getName());
        
        Set<String> contracts = fd.getAdvertisedContracts();
        contracts.add(FrenchService.class.getName());
        
        configurator.bind(fd);
        
        DescriptorImpl latin = new DescriptorImpl();
        latin.setImplementation(LatinService.class.getName());
        latin.addAdvertisedContract(LatinService.class.getName());
        latin.addQualifier(Dead.class.getName());
        latin.setScope(Singleton.class.getName());
        
        configurator.bind(latin);
        
        DescriptorImpl thracian = new DescriptorImpl();
        thracian.setImplementation(ThracianService.class.getName());
        thracian.addAdvertisedContract(ThracianService.class.getName());
        thracian.addQualifier(Dead.class.getName());
        
        configurator.bind(thracian);
        
        // These are for the TypeLiteral tests
        configurator.bind(BuilderHelper.link(COBOL.class).
                                        to(ComputerLanguage.class).
                                        in(Singleton.class.getName()).
                                        build());
        
        configurator.bind(BuilderHelper.link(Fortran.class).
                to(ComputerLanguage.class).
                in(Singleton.class.getName()).
                build());
        
        configurator.bind(BuilderHelper.link(Java.class).
                to(ComputerLanguage.class).
                named(LocatorTest.JAVA_NAME).
                in(Singleton.class.getName()).
                build());
        
        // These are for a class with no scope annotation, and hence
        // should be allowed to take on any scope
        configurator.bind(BuilderHelper.link(NoScopeService.class.getName()).
                in(Singleton.class.getName()).build());
        
        configurator.bind(BuilderHelper.link(NoScopeService.class.getName()).
                in(PerLookup.class.getName()).build());
        
        // For the performance check of the cache
        configurator.bind(BuilderHelper.link(PerformanceService.class).
                in(Singleton.class.getName()).build());
        
        
    }

}
