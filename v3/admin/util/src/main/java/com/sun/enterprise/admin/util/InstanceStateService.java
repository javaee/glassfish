/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.util;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import org.glassfish.api.Startup;
import org.glassfish.api.admin.*;
import org.glassfish.api.admin.ServerEnvironment;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.component.Singleton;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * This service is called at startup and parses the instance state file
 * @author Vijay Ramachandran
 */
@Service
@Scoped(Singleton.class)
public class InstanceStateService implements Startup, PostConstruct {

    @Inject
    private Habitat habitat;

    @Inject
    private ServerEnvironment serverEnv;

    @Inject
    private Domain domain;

    @Inject
    private Logger logger;

    @Inject
    private CommandThreadPool cmdPool;

    private InstanceStateFileProcessor stateProcessor;

    private HashMap<String, InstanceState> instanceStates = new HashMap<String, InstanceState>();

    private final int MAX_RECORDED_FAILED_COMMANDS = 10;

    public InstanceStateService() {}

    /**
     * Process the instance file if this is DAS and there are instances configured already in this domain
     */
    @Override
    public void postConstruct() {
        // If this is not the DAS, no need for instance state processing
        if(serverEnv.isInstance()) {
            return;
        }
        stateProcessor = new InstanceStateFileProcessor(habitat, instanceStates, domain,
                serverEnv.getConfigDirPath().getAbsolutePath()+ File.separatorChar+".instancestate");
        // There are no instances configured in this domain as yet; no need for instance state service
        if(domain.getServers().getServer().size() == 1) {
            return;
        }
        try {
            stateProcessor.parse();
        } catch (Exception e) {
            logger.severe("Error while parsing instance state file : " + e.getLocalizedMessage());
        }
    }

    public synchronized void addServerToStateService(String instanceName) {
        if(instanceStates.get(instanceName) != null)
            return;
        try {
            instanceStates.put(instanceName, new InstanceState(InstanceState.StateType.NO_RESPONSE));
            stateProcessor.addNewServer(instanceName);
        } catch (Exception e) {
            logger.severe("Error while adding new server state to instance state : " + e.getLocalizedMessage());
        }
    }

    public synchronized void addFailedCommandToInstance(String instance, String cmd, ParameterMap params) {
        String cmdDetails = cmd;
        String defArg = params.getOne("DEFAULT");
        if (defArg != null) cmdDetails += " " + defArg;

        try {
            InstanceState i = instanceStates.get(instance);
            if( (i != null) && (i.getFailedCommands().size() < MAX_RECORDED_FAILED_COMMANDS) ) {
                i.addFailedCommands(cmdDetails);
                stateProcessor.addFailedCommand(instance, cmdDetails);
            }
        } catch (Exception e) {
            logger.severe("Error while adding new server state to instance state : " + e.getLocalizedMessage());
        }
    }

    public synchronized void removeFailedCommandsForInstance(String instance) {
        try {
            InstanceState i = instanceStates.get(instance);
            if(i != null) {
                i.removeFailedCommands();
                stateProcessor.removeFailedCommands(instance);
            }
        } catch (Exception e) {
            logger.severe("Error while failed commands from instance state : " + e.getLocalizedMessage());
        }
    }

    public InstanceState.StateType getState(String instanceName) {
        InstanceState s = instanceStates.get(instanceName);
        if(s == null)
            return InstanceState.StateType.NO_RESPONSE;
        return s.getState();
    }

    public List<String> getFailedCommands(String instanceName) {
        InstanceState s = instanceStates.get(instanceName);
        if(s == null)
            return new ArrayList<String>();
        return(s.getFailedCommands());
    }

    public synchronized InstanceState.StateType setState(String name, InstanceState.StateType newState, boolean force) {
        boolean updateXML = false;
        InstanceState.StateType ret = newState;
        InstanceState.StateType currState = instanceStates.get(name).getState();
        if(currState == null) {
            instanceStates.put(name, new InstanceState(newState));
            updateXML = true;
            ret = newState;
        } else {
            if(!force && currState.equals(InstanceState.StateType.RESTART_REQUIRED)) {
                // If current state is RESTART_REQUIRED, no updates to state is allowed because
                // only an instance restart can move this instance out of RESTART_REQD state
                updateXML = false;
                ret = currState;
            } else {
                // Do update only if there is a change from RUNNING to NO_RESPONSE or vice versa
                if(!currState.equals(newState)) {
                    instanceStates.get(name).setState(newState);
                    updateXML = true;
                    ret = newState;
                }
            }
        }
        try {
            if(force || updateXML) {
                stateProcessor.updateState(name, newState.getDescription());
            }
        } catch (Exception e) {
            logger.severe("Error while setting instance state : " + e.getLocalizedMessage());
        }
        return ret;
    }

    public synchronized void removeInstanceFromStateService(String name) {
        instanceStates.remove(name);
        try {
            stateProcessor.removeInstanceNode(name);
        } catch (Exception e) {
            logger.severe("Error while removing instance node : " + e.getLocalizedMessage());
        }
    }

    public Future<InstanceCommandResult> submitJob(Server server, InstanceCommand ice, InstanceCommandResult r) {
        InstanceState s = instanceStates.get(server.getName());
        if(s == null)
            return null;
        return cmdPool.submitJob(ice, r);
    }

    @Override
    public Lifecycle getLifecycle() {
        return Startup.Lifecycle.SERVER;
    }
}
