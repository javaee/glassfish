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

import com.sun.enterprise.universal.glassfish.SystemPropertyConstants;
import org.glassfish.api.Startup;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.config.ConfigSupport;

import com.sun.jersey.api.core.ResourceConfig;
//import com.sun.jersey.api.container.grizzly.GrizzlyServerFactory;

import com.sun.jersey.api.core.DefaultResourceConfig;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;

import com.sun.jersey.api.container.ContainerFactory;
import org.glassfish.api.container.RequestDispatcher;
import com.sun.logging.LogDomains;
import java.util.logging.Logger;
import org.glassfish.server.ServerEnvironmentImpl;

/**
 * @author Ludovic Champenois ludo@dev.java.net
 */
@Service
public class RestService implements Startup, PostConstruct, PreDestroy {

    @Inject
    public static Habitat habitat;
    @Inject
    com.sun.enterprise.config.serverbeans.Domain domain;
    public static com.sun.enterprise.config.serverbeans.Domain theDomain;
    @Inject
    org.glassfish.flashlight.MonitoringRuntimeDataRegistry monitoringRegistry;
    public static org.glassfish.flashlight.MonitoringRuntimeDataRegistry theMonitoringRegistry;
    public static ConfigSupport configSupport;
    //private final static String BASE_URI = "http://localhost:9998/";
    public final static Logger logger = LogDomains.getLogger(RestService.class, LogDomains.ADMIN_LOGGER);
    @Inject
    ServerEnvironmentImpl env;

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
        System.out.println("********************* Instrumenting Rest Resources ******************************");
        try {
            start();
//            String rootFolder = env.getProps().get(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY) + "/asadmindocroot/";
//
//            InstallTestClient.doit(rootFolder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("********************* DONE: Instrumenting Rest Resources ******************************");
    }

    public void preDestroy() {
    }

    private void start() throws Exception {
    //    System.getProperties().put("com.sun.grizzly.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");

        theDomain = domain;
        theMonitoringRegistry = monitoringRegistry;
        ConfigSupport cs = RestService.habitat.getComponent(ConfigSupport.class);
        configSupport = cs;



        RequestDispatcher rd = habitat.getComponent(RequestDispatcher.class);
        ResourceConfig rc = getResourceConfig();
        //        Adapter adap=new MyAdapter();//ContainerFactory.createContainer(Adapter.class, getResourceConfig());
        Adapter adap = ContainerFactory.createContainer(Adapter.class, rc);
        ((GrizzlyAdapter) adap).setResourcesContextPath("/rest-resources");
        Collection<String> virtualserverName = new ArrayList<String>();
        virtualserverName.add("__asadmin");
        rd.registerEndpoint("/rest-resources", virtualserverName, adap, null);
        System.out.println("************** listening to REST requests at http://localhost:4848/rest-resources/domain");

        // GrizzlyServerFactory.create(BASE_URI, getResourceConfig());


    }

    public static ResourceConfig getResourceConfig() {
        final Set<Class<?>> r = new HashSet<Class<?>>();

        //uncomment if you need to run the generator:
           r.add(GeneratorResource.class);
        r.add(org.glassfish.admin.rest.resources.DomainResource.class);
        r.add(DefaultConfigResource.class);
        r.add(org.glassfish.admin.rest.resources.MonitoringResource.class);


        System.out.println("************** resources: " + r);
        return new DefaultResourceConfig(r);
    }
}
