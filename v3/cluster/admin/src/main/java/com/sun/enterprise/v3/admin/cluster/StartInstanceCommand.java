/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.admin.remote.RemoteAdminCommand;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.util.StringUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.Async;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.ParameterMap;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;

import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Habitat;

import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.cluster.ssh.connect.RemoteConnectHelper;
import java.io.IOException;
import java.lang.InterruptedException;
import com.sun.enterprise.config.serverbeans.Node;


/**
 * AdminCommand to stop the instance
 * server.
 * Shutdown of an instance.
 * If this is DAS -- we call the instance
 * If this is an instance we commit suicide
 *
 * note: This command is asynchronous.  We can't return anything so we just
 * log errors and return

 * @author Byron Nevins
 */
@Service(name = "start-instance")
@Scoped(PerLookup.class)
@I18n("start.instance.command")
public class StartInstanceCommand implements AdminCommand, PostConstruct {
    @Inject
    Habitat habitat;

    @Inject
    Node[] nodes;

    //the instance name supplied on the commandline
    @Param(optional=true)
    String name;

    private HashMap<String, Node> nodeMap;
    

    public void execute(AdminCommandContext context) {
        logger = context.getLogger();


        // assuming for now that the same requirements as list instances in terms of running on DAS or instances
        if(env.isDas()) {
            callInstance();
        }
        else if(env.isInstance()) {
            logger.info(Strings.get("start.instance.init"));
            Collection<Module> modules = registry.getModules(
                    "org.glassfish.core.glassfish");
            if(modules.size() == 1) {
                final Module mgmtAgentModule = modules.iterator().next();
                mgmtAgentModule.start();
            }
            else {
                logger.warning(modules.size() + " no of primordial modules found");
            }
            if(force) {
                System.exit(0);
            }
        }
        else {
            String msg = Strings.get("start.instance.notAnInstanceOrDas",
                    env.getRuntimeType().toString());
            logger.warning(msg);
        }
    }

    @Override
    public void postConstruct() {
        helper = new RemoteInstanceCommandHelper(env, servers, configs);
    }

    private void callInstance() {
//        try {
            if(!StringUtils.ok(instanceName)) {
                logger.severe(Strings.get("start.instance.noInstanceName"));
                return;
            }
            final Server instance = helper.getServer(instanceName);
            if(instance == null) {
                logger.severe(Strings.get("start.instance.noSuchInstance", instanceName));
                return;
            }                        
            final String noderef = helper.getNode(instance);
            if(noderef.equals("noNodeRef")) {
                logger.severe(Strings.get("start.instance.noSuchNodeRef", noderef));
                return;
            }
            if (noderef.equals("localhost")) {
                logger.info("starting instance on localhost");
                return;

            } else {
                RemoteConnectHelper rch = new RemoteConnectHelper(habitat, nodes, logger);
                // check if needs a remote connection
                if (rch.isRemoteConnectRequired(noderef)) {
                    // this command will run over ssh
                    rch.runCommand(noderef, "start-local-instance", instanceName);

                }
            }

    }
    
    @Inject
    private ServerEnvironment env;
    @Inject
    private Servers servers;
    @Inject
    private Configs configs;
    @Inject
    private ModulesRegistry registry;
    @Param(optional = true, defaultValue = "true")
    private Boolean force;
    @Param(optional = true, primary = true)
    private String instanceName;
    private Logger logger;
    private RemoteInstanceCommandHelper helper;
}
