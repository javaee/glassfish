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
package org.glassfish.examples.http.test;

import javax.inject.Singleton;

import org.glassfish.examples.http.AlternateInjectResolver;
import org.glassfish.examples.http.HttpEventReceiver;
import org.glassfish.examples.http.HttpRequest;
import org.glassfish.examples.http.HttpServer;
import org.glassfish.examples.http.Logger;
import org.glassfish.examples.http.RequestContext;
import org.glassfish.examples.http.RequestProcessor;
import org.glassfish.examples.http.RequestScope;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;

/**
 * TODO:  Once &#64;Service works this class should go
 * away in favor of just automatically registering the
 * services.  Until then we must do the registration
 * manually
 * 
 * @author jwells
 */
public class Populator {
    
    /**
     * TODO:  This should be removed once we have &#64;Service all hooked up
     * 
     * @param locator The locator to populate
     */
    public static void populate(ServiceLocator locator) {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        // The InjectionResolver we are showcasing
        config.bind(BuilderHelper.link(AlternateInjectResolver.class).
                to(InjectionResolver.class).
                in(Singleton.class.getName()).
                build());
        
        // The HttpEventReciever is in the default scope @PerLookup
        config.bind(BuilderHelper.link(HttpEventReceiver.class).
                build());
        
        // The HttpRequest is in the RequestScope
        config.bind(BuilderHelper.link(HttpRequest.class).
                in(RequestScope.class.getName()).
                build());
        
        // The HttpServer is a singleton
        config.bind(BuilderHelper.link(HttpServer.class).
                in(Singleton.class.getName()).
                build());
        
        // The RequestContext is the context implementation for RequestScope
        config.bind(BuilderHelper.link(RequestContext.class).
                to(Context.class).
                in(Singleton.class.getName()).
                build());
        
        // RequestProcessor processes a request from an HttpServer
        config.bind(BuilderHelper.link(RequestProcessor.class).
                build());
        
        // The logger is just another service to be injected
        config.bind(BuilderHelper.link(Logger.class).
                in(Singleton.class.getName()).
                build());
        
        // And commit
        config.commit();           
    }

}
