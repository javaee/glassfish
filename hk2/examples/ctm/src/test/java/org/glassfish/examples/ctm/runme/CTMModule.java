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
package org.glassfish.examples.ctm.runme;

import javax.inject.Singleton;

import org.glassfish.examples.ctm.Environment;
import org.glassfish.examples.ctm.EnvironmentFactory;
import org.glassfish.examples.ctm.ServiceProviderEngine;
import org.glassfish.examples.ctm.TenantManager;
import org.glassfish.examples.ctm.TenantScoped;
import org.glassfish.examples.ctm.TenantScopedContext;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.utilities.BuilderHelper;

/**
 * TODO:  This should be done via auto-depends (via Service and contract
 * and all that).  However, since those don't work yet with the new
 * API, we must code this up by hand.
 * 
 * @author jwells
 *
 */
public class CTMModule {

    /**
     * Configures the HK2 instance
     * 
     * @param configurator
     */
    public void configure(DynamicConfiguration configurator) {
        // Bind our custom scope
        configurator.bind(
                BuilderHelper.link(TenantScopedContext.class).
                              to(Context.class).
                              in(Singleton.class.getName()).
                              build());
        
        // Bind our factory
        configurator.bind(
                BuilderHelper.link(EnvironmentFactory.class).
                              to(Environment.class).
                              in(TenantScoped.class.getName()).
                              buildFactory(Singleton.class.getName()));
        
        // We implemented the TenantManager as a service (nice!)
        configurator.bind(
                BuilderHelper.link(TenantManager.class).
                              in(Singleton.class.getName()).
                              build());
        
        // And of course, ServiceProviderEngine is a service
        configurator.bind(
                BuilderHelper.link(ServiceProviderEngine.class).
                              in(Singleton.class.getName()).
                              build());

    }

}
