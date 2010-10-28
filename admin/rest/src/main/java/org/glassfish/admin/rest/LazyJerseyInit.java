/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import org.glassfish.api.container.EndpointRegistrationException;
import com.sun.grizzly.tcp.Adapter;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;
import java.util.Set;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.glassfish.admin.rest.adapter.Reloader;
import org.glassfish.admin.rest.resources.ReloadResource;
import org.glassfish.internal.api.ServerContext;
import org.jvnet.hk2.component.Habitat;

/**
 *
 * @author ludovic champenois ludo@dev.java.net
 */
/**
 * Class that initialize the Jersey container. It is called via introspection from RestAdapter
 * so that RestAdapter does not depend on Jersey classes. This way, we gain 90ms at startup time
 * and load the jersey classes only at the very last time, when needed.
 * @author ludo
 */
public class LazyJerseyInit {

    
    /**
     * Called via introspection in the RestAdapter service() method only when the GrizzlyAdapter is not initialized
     * @param classes set of Jersey Resources classes
     * @param sc the current ServerContext, needed to find the correct classpath
     * @return the correct GrizzlyAdapter
     * @throws EndpointRegistrationException
     */
    public static GrizzlyAdapter exposeContext(Set classes, ServerContext sc, Habitat habitat)
            throws EndpointRegistrationException {
        

        Adapter adapter = null;
        Reloader r = new Reloader();
        ResourceConfig rc = new DefaultResourceConfig(classes);
        rc.getMediaTypeMappings().put("xml", MediaType.APPLICATION_XML_TYPE);
        rc.getMediaTypeMappings().put("json", MediaType.APPLICATION_JSON_TYPE);
        rc.getMediaTypeMappings().put("html", MediaType.TEXT_HTML_TYPE);
        rc.getMediaTypeMappings().put("js", new MediaType("application","x-javascript"));
        
        RestConfig restConf = getRestConfig(habitat);
        if (restConf != null) {
            if (restConf.getLogOutput().equalsIgnoreCase("true")) { //enable output logging
                rc.getContainerResponseFilters().add(LoggingFilter.class);
            }
            if (restConf.getLogInput().equalsIgnoreCase("true")) { //enable input logging
                rc.getContainerRequestFilters().add(LoggingFilter.class);
            }
            if (restConf.getWadlGeneration().equalsIgnoreCase("false")) { //disable WADL

                rc.getFeatures().put(ResourceConfig.FEATURE_DISABLE_WADL, Boolean.TRUE);
            }
        }

        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_NOTIFIER, r);
        rc.getClasses().add(ReloadResource.class);

        //We can only inject these 3 extra classes in Jersey resources...
        //
        rc.getSingletons().add(new SingletonTypeInjectableProvider<Context, Reloader>(Reloader.class, r) {});
        rc.getSingletons().add(new SingletonTypeInjectableProvider<Context, ServerContext>(ServerContext.class, sc) {});
        rc.getSingletons().add(new SingletonTypeInjectableProvider<Context, Habitat>(Habitat.class, habitat) {});

        //Use common classloader. Jersey artifacts are not visible through
        //module classloader
        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader apiClassLoader = sc.getCommonClassLoader();
            Thread.currentThread().setContextClassLoader(apiClassLoader);
            adapter = ContainerFactory.createContainer(com.sun.grizzly.tcp.Adapter.class, rc);
        } finally {
            Thread.currentThread().setContextClassLoader(originalContextClassLoader);
        }
        //add a rest config listener for possible reload of Jersey
        new RestConfigChangeListener(habitat, r ,rc , sc);
        return (GrizzlyAdapter) adapter;
    }
    
    
    static protected RestConfig getRestConfig(Habitat habitat) {
        if (habitat == null) {
            return null;
        }
        Domain domain = habitat.getComponent(Domain.class);
        if (domain != null) {
            Config config = domain.getConfigNamed("server-config");
            if (config != null) {
                return config.getExtensionByType(RestConfig.class);

            }
        }
        return null;

    }
}
