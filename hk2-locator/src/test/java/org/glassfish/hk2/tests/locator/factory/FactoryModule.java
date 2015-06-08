/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.tests.locator.factory;

import java.util.Date;

import javax.inject.Singleton;

import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.tests.locator.utilities.TestModule;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;

/**
 * @author jwells
 *
 */
public class FactoryModule implements TestModule {

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Module#configure(org.glassfish.hk2.api.Configuration)
     */
    @Override
    public void configure(DynamicConfiguration configurator) {
        configurator.addActiveDescriptor(ErrorServiceImpl.class);
        configurator.bind(BuilderHelper.link(DateFactory.class).to(Date.class).buildFactory());
        configurator.bind(BuilderHelper.link(DateInjectee.class).in(Singleton.class).build());
        
        configurator.bind(BuilderHelper.link(
                FruitFactory.class).
                to(Apple.class).
                in(FruitScope.class.getName()).
                buildFactory(Singleton.class.getName()));
        // Apple is not in the list, but its factory is
        
        // Also bind the custom scope
        configurator.bind(
                BuilderHelper.link(FruitContext.class).
                to(Context.class).
                in(Singleton.class.getName()).
                build());
        
        // Now for our named factories.  They produce the same type (President)
        // but they each do it with a different name
        
        // Washington
        configurator.bind(BuilderHelper.link(
                WashingtonFactory.class).
                to(President.class).
                in(Singleton.class.getName()).
                named(FactoryTest.WASHINGTON_NAME).
                buildFactory(Singleton.class));
        
        // Jefferson
        configurator.bind(BuilderHelper.link(
                JeffersonFactory.class).
                to(President.class).
                in(Singleton.class.getName()).
                named(FactoryTest.JEFFERSON_NAME).
                buildFactory());
        
        // In the following test the Factory is put in
        // *without* using the buildFactory method, in order
        // to ensure that works properly.  Instead we build up
        // the factory with the normal build mechanism
        
        // First the class service
        configurator.bind(BuilderHelper.link(WidgetFactory.class.getName()).
                to(Factory.class.getName()).
                build());
        
        // Second the per-lookup method
        DescriptorImpl di = BuilderHelper.link(WidgetFactory.class.getName()).
                to(Widget.class.getName()).build();
        di.setDescriptorType(DescriptorType.PROVIDE_METHOD);
        configurator.bind(di);
        
        // For the test below we have an abstract factory that is in a proxiable scope
        // and which is producing something from a proxiable scope
        // First we add the context, then we add the factories
        configurator.bind(BuilderHelper.link(ProxiableSingletonContext.class.getName()).
                to(Context.class.getName()).
                in(Singleton.class.getName()).build());
        configurator.bind(BuilderHelper.link(AdamsFactory.class).
                to(AdamsVP.class).
                in(ProxiableSingleton.class.getName()).
                buildFactory(ProxiableSingleton.class.getName()));
        configurator.bind(BuilderHelper.link(JeffersonVPFactory.class).
                to(JeffersonVP.class).
                in(ProxiableSingleton.class.getName()).
                buildFactory(ProxiableSingleton.class.getName()));
        configurator.bind(BuilderHelper.link(BurrVPFactory.class).
                to(BurrVP.class).
                in(ProxiableSingleton.class.getName()).
                buildFactory(ProxiableSingleton.class.getName()));
    }

}
