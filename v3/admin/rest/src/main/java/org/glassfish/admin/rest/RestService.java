/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.Set;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.config.ConfigSupport;

import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.logging.LogDomains;

import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.api.container.RequestDispatcher;
import org.glassfish.api.Startup;
import org.glassfish.server.ServerEnvironmentImpl;

import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;

//import com.sun.jersey.api.container.grizzly.GrizzlyServerFactory;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

/**
 * @author Ludovic Champenois ludo@dev.java.net
 * @author Rajeshwar Patil
 */
@Service
public class RestService implements Startup, PostConstruct, PreDestroy {

    @Inject
    public static Habitat habitat;

    @Inject
    com.sun.enterprise.config.serverbeans.Domain domain;

    @Inject
    org.glassfish.flashlight.MonitoringRuntimeDataRegistry monitoringRegistry;

    @Inject
    ServerEnvironmentImpl env;

    public static com.sun.enterprise.config.serverbeans.Domain theDomain;
    public static org.glassfish.flashlight.MonitoringRuntimeDataRegistry theMonitoringRegistry;
    public static ConfigSupport configSupport;

    public final static Logger logger =
            LogDomains.getLogger(RestService.class, LogDomains.ADMIN_LOGGER);

    public Lifecycle getLifecycle() {
        // This service stays running for the life of the app server, hence SERVER.
        return Lifecycle.SERVER;
    }

    /*
     *     @Inject(name= ServerEnvironment.DEFAULT_INSTANCE_NAME)
    protected Server server;
     *
     *
     *         CommandRunner cr = RestService.habitat.getComponent(CommandRunner.class);
    ActionReport ar =RestService.habitat.getComponent(ActionReport.class);
    Properties p = new Properties();
    AdminCommand ac;

    cr.doCommand("list-applications", p, ar);
    System.out.println("exec command"+ar.getActionExitCode());


     *
     * */
    public void postConstruct() {
        //events.register(this);
        System.out.println("Instrumenting Rest Resources");
        try {
            start();
            //String rootFolder = env.getProps().get(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY) + "/asadmindocroot/";
            //InstallTestClient.doit(rootFolder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void preDestroy() {
    }


    private void start() throws Exception {
        //System.getProperties().put("com.sun.grizzly.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");

        theDomain = domain;
        theMonitoringRegistry = monitoringRegistry;
        ConfigSupport cs =
                RestService.habitat.getComponent(ConfigSupport.class);
        configSupport = cs;

        //expose configuration resources
        exposeContext("/management");
        
        //expose monitoring resources
        exposeContext("/monitoring");
    }


    private void exposeContext(String context) 
            throws EndpointRegistrationException {
        if ((context != null) || (!"".equals(context))) {
            RequestDispatcher rd =
                    habitat.getComponent(RequestDispatcher.class);
            Collection<String> virtualserverName = new ArrayList<String>();
            virtualserverName.add("__asadmin");

            ResourceConfig rc = getResourceConfig(context);
            Adapter adapter =
                    ContainerFactory.createContainer(Adapter.class, rc);
            ((GrizzlyAdapter) adapter).setResourcesContextPath(context);

            rd.registerEndpoint(context, virtualserverName, adapter, null);
            System.out.println("Listening to REST requests at context: " +
                    context + "/domain");
        }
    }


    public static ResourceConfig getResourceConfig(String context) {
        final Set<Class<?>> r = new HashSet<Class<?>>();

        if (context.equals("/management")) {
            //uncomment if you need to run the generator:
            //r.add(GeneratorResource.class);
            r.add(org.glassfish.admin.rest.resources.DomainResource.class);
        }

        if (context.equals("/monitoring")) {
            r.add(org.glassfish.admin.rest.resources.MonitoringResource.class);
        }

        return new DefaultResourceConfig(r);
    }
}
