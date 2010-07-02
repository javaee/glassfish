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
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.util.StringUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Async;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;

import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;

import com.sun.enterprise.universal.process.LocalAdminCommand;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Habitat;

import org.glassfish.cluster.ssh.connect.RemoteConnectHelper;
import java.io.IOException;
import java.lang.InterruptedException;
import com.sun.enterprise.config.serverbeans.Node;


/**
 * AdminCommand to start the instance
 * server.
 * If this is DAS -- we call the instance
 * If this is an instance we start it
 *
 * @author Carla Mott
 */
@Service(name = "start-instance")
@Scoped(PerLookup.class)
@I18n("start.instance.command")
public class StartInstanceCommand implements AdminCommand, PostConstruct {
    @Inject
    Habitat habitat;

    @Inject
    Node[] nodeList;

    @Inject
    private Nodes nodes;

    @Inject
    private CommandRunner cr;

    @Inject
    private ServerEnvironment env;
    
    @Param(optional = true, defaultValue = "true")
    private Boolean force;
    @Param(optional = true, primary = true)

    private String instanceName;
    private Logger logger;
    private RemoteInstanceCommandHelper helper;

    private AdminCommandContext ctx;
    private String noderef;
    private Server instance;

    private static final String NL = System.getProperty("line.separator");

    public void execute(AdminCommandContext context) {
        logger = context.getLogger();
        this.ctx=context;
        if(!StringUtils.ok(instanceName)) {
            logger.severe(Strings.get("start.instance.noInstanceName"));
            return;
        }
        instance = helper.getServer(instanceName);
        if(instance == null) {
            logger.severe(Strings.get("start.instance.noSuchInstance", instanceName));
            return;
        }
        noderef = helper.getNode(instance);
        if(noderef.equals("noNodeRef")) {
             logger.severe(Strings.get("start.instance.noSuchNodeRef", noderef));
            return;
        }

        if(env.isDas()) {
            callInstance();
        }
        else if(env.isInstance()) {
            startInstance();

        } else {
            String msg = Strings.get("start.instance.notAnInstanceOrDas",
                    env.getRuntimeType().toString());
            logger.warning(msg);
        }
    }

    @Override
    public void postConstruct() {
        helper = new RemoteInstanceCommandHelper(habitat);
    }

    private void startInstance()  {
        LocalAdminCommand lac = new LocalAdminCommand("start-local-instance",instanceName);
        try {
            int status = lac.execute();
        } catch (ProcessManagerException ex) {
            
        }
    }

    private void callInstance() {
        int dasPort = helper.getAdminPort(SystemPropertyConstants.DAS_SERVER_NAME);
        String dasHost = System.getProperty(SystemPropertyConstants.HOST_NAME_PROPERTY);
        RemoteConnectHelper rch = new RemoteConnectHelper(habitat, nodeList, logger, dasHost, dasPort);

        if (rch.isLocalhost(nodes.getNode(noderef))) {
            startInstance();
        } else  if (rch.isRemoteConnectRequired(noderef)) {  // check if needs a remote connection
            // this command will run over ssh
            StringBuilder output = new StringBuilder();
            ParameterMap map = new ParameterMap();
            map.set("DEFAULT", instanceName);
            int status = rch.runCommand(noderef, "start-local-instance",
                     map, output);
            if (output.length() > 0) {
                 logger.info(output.toString());
            }
            if (status != 1){
                ActionReport report = ctx.getActionReport();
                String msg = Strings.get("start.remote.instance.failed", instanceName);
                logger.warning(msg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(output + NL + msg);
             }
        } else {
            ActionReport report = ctx.getActionReport();
            String msg = Strings.get("start.remote.instance.failed", instanceName);
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);            
        }
    }
    
}
