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
package org.glassfish.hk2.tests.locator.lifecycle;

import javax.inject.Singleton;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.InstanceLifecycleListener;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.tests.locator.utilities.TestModule;
import org.glassfish.hk2.utilities.BuilderHelper;

/**
 * @author jwells
 *
 */
public class LifecycleModule implements TestModule {

    /* (non-Javadoc)
     * @see org.glassfish.hk2.tests.locator.utilities.TestModule#configure(org.glassfish.hk2.api.DynamicConfiguration)
     */
    @Override
    public void configure(DynamicConfiguration config) {
        // This is the LifecycleListener itself
        config.bind(BuilderHelper.link(
                InstanceLifecycleListenerImpl.class).
                to(InstanceLifecycleListener.class).
                in(Singleton.class.getName()).
                build());
        
        // This is the default one, actually created by the system
        config.bind(BuilderHelper.link(
                Notifier.class).
                named(Notifier.DEFAULT_NAME).
                in(Singleton.class.getName()).
                build());
        
        
        // This is the receiver that gets it from the system created guy
        config.bind(BuilderHelper.link(
                KnownInjecteeNotifyee.class).
                to(Notifyee.class).
                in(PerLookup.class.getName()).
                build());
        
        // The earth wind and fire ordered service, to check that ordering of the PRE_PRODUCTION is ok
        config.bind(BuilderHelper.link(OrderedLifecycleListener.class.getName()).
                to(InstanceLifecycleListener.class.getName()).
                in(Singleton.class.getName()).
                build());
        
        // This is earth, wind and fire as basic descriptors
        config.bind(BuilderHelper.link(Earth.class.getName()).
                to(EarthWindAndFire.class.getName()).
                in(Singleton.class.getName()).
                build());
        
        config.bind(BuilderHelper.link(Wind.class.getName()).
                to(EarthWindAndFire.class.getName()).
                build());
        
        config.bind(BuilderHelper.link(Fire.class.getName()).
                to(EarthWindAndFire.class.getName()).
                build());
        
        // This is water, sand and space done as active descriptors (but as a class)
        config.addActiveDescriptor(Water.class);
        config.addActiveDescriptor(Sand.class);
        config.addActiveDescriptor(Space.class);
        
    }

}
