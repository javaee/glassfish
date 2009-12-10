/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.enterprise.config.serverbeans.Domain;
import java.util.logging.Logger;

import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.config.ConfigSupport;

import com.sun.enterprise.util.LocalStringManagerImpl;
//import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.logging.LogDomains;

import org.glassfish.api.Startup;
import org.glassfish.internal.api.LocalPassword;
import org.glassfish.internal.api.RestInterfaceUID;
import org.glassfish.server.ServerEnvironmentImpl;


/**
 * @author Ludovic Champenois ludo@dev.java.net
 * @author Rajeshwar Patil
 */
@Service
public class RestService implements Startup, PostConstruct, PreDestroy, RestInterfaceUID {

    @Inject
    private static Habitat habitat;

    @Inject
    com.sun.enterprise.config.serverbeans.Domain domain;

    @Inject
    org.glassfish.flashlight.MonitoringRuntimeDataRegistry monitoringRegistry;

    @Inject
    ServerEnvironmentImpl env;

    @Inject
    LocalPassword localPassword;

    private static com.sun.enterprise.config.serverbeans.Domain theDomain;
    private static org.glassfish.flashlight.MonitoringRuntimeDataRegistry theMonitoringRegistry;
    private static ConfigSupport configSupport;

    public final static Logger logger =
            LogDomains.getLogger(RestService.class, LogDomains.ADMIN_LOGGER);
    public final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(RestService.class);

    @Override
    public Lifecycle getLifecycle() {
        // This service stays running for the life of the app server, hence SERVER.
        return Lifecycle.SERVER;
    }

    public static Habitat getHabitat() {
        return habitat;
    }
    public static ConfigSupport getConfigSupport() {
        return configSupport;
    }

    public static Domain getDomain() {
        return theDomain;
    }

    public static MonitoringRuntimeDataRegistry getMonitoringRegistry() {
        return theMonitoringRegistry;
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
    @Override
    public void postConstruct() {
        //events.register(this);
        logger.fine(localStrings.getLocalString("rest.service.initialization",
                "Initializing REST interface support"));
        try {
            initialize();
            //String rootFolder = env.getProps().get(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY) + "/asadmindocroot/";
            //InstallTestClient.doit(rootFolder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void preDestroy() {
    }


    @Override
    public String getUID() {
        if (_uid == null) {
            _uid = localPassword.getLocalPassword();
        }
        return _uid;
    }


    static String getRestUID() {
        return _uid;
    }


    private void initialize() throws Exception {
        //System.getProperties().put("com.sun.grizzly.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
        theDomain = domain;
        theMonitoringRegistry = monitoringRegistry;
        ConfigSupport cs =
                RestService.habitat.getComponent(ConfigSupport.class);
        configSupport = cs;
    }


    private static String _uid;
}
