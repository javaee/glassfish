/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 *
 */
package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.admin.remote.RemoteAdminCommand;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.grizzly.config.dom.NetworkListener;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.*;
import org.glassfish.common.util.admin.CommandModelImpl;
import org.glassfish.config.support.GenericCrudCommand;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A ClusterExecutor is responsible for remotely executing commands.
 * The list of target servers (either clusters or remote instances) is obtained
 * from the parameter list.
 *
 * @author Vijay Ramachandran
 */
@Service
public class GlassFishClusterExecutor implements ClusterExecutor {

    @Inject
    Domain domain;

    @Inject
    ExecutorService threadExecutor; 

    private static final LocalStringManagerImpl strings =
                        new LocalStringManagerImpl(GlassFishClusterExecutor.class);

    private ArrayList<RuntimeType> runtimeTypes = new ArrayList<RuntimeType>();
    private FailurePolicy onFailure = FailurePolicy.Error;
    private FailurePolicy ifOffline = FailurePolicy.Warn;

    /**
     * <p>Execute the passed command on targeted remote instances. The list of remote
     * instances is usually retrieved from the passed parameters (with a "target"
     * parameter for instance) or from the configuration.
     *
     * <p>Each remote execution must return a different ActionReport so the user
     * or framework can get feedback on the success or failure or such executions.
     *
     * @param commandName the command to execute
     * @param context the original command context
     * @param parameters the parameters passed to the original local command
     * @return an array of @{link org.glassfish.api.ActionReport} for each remote
     * execution status. 
     */
    public ActionReport.ExitCode execute(String commandName, AdminCommand command, AdminCommandContext context, ParameterMap parameters) {

        // Obtain the command model for this command.
        CommandModel model;
        try {
            CommandModelProvider c = (CommandModelProvider) command;
            model = c.getModel();
        } catch(ClassCastException e) {
            model = new CommandModelImpl(command.getClass());
        }

        // Get @Cluster annotation params; if not present, set required defaults.
        org.glassfish.api.admin.Cluster clAnnotation = model.getClusteringAttributes();
        if(clAnnotation == null) {
            runtimeTypes.add(RuntimeType.DAS);
            runtimeTypes.add(RuntimeType.INSTANCE);
        } else {
            if(clAnnotation.value().length == 0) {
                runtimeTypes.add(RuntimeType.DAS);
                runtimeTypes.add(RuntimeType.INSTANCE);
            } else {
                for(RuntimeType t : clAnnotation.value()) {
                    runtimeTypes.add(t);
                }
            }
            if(clAnnotation.ifFailure() != null) {
                onFailure = clAnnotation.ifFailure();
            }
            if(clAnnotation.ifOffline() != null) {
                ifOffline = clAnnotation.ifOffline();
            }
        }

        //If this command is meant for DAS only, return right now
        if(!runtimeTypes.contains(RuntimeType.INSTANCE))
                return ActionReport.ExitCode.SUCCESS;

        // Get target and find list of instances from target
        String target = parameters.getOne("target");
        ArrayList<Server> actualTargets = new ArrayList<Server>();

        //If given target is an instance itself, this is the only target
        if(domain.getServerNamed(target) != null) {
            actualTargets.add(domain.getServerNamed(target));
        } else {
            actualTargets.addAll(getInstancesForCluster(target));
        }

        if(actualTargets.size() == 0) {
            ActionReport aReport = context.getActionReport().addSubActionsReport();
            aReport.setActionExitCode(ActionReport.ExitCode.FAILURE);
            aReport.setMessage(strings.getLocalString("glassfish.clusterexecutor.notargets",
                    "Unable to find instances for target {0}", target));
            return getReturnValueFor(onFailure);
        }

        // TODO : Use Executor service to spray the commands on all instances
        ActionReport.ExitCode returnValue = ActionReport.ExitCode.SUCCESS;
        try {
            List<InstanceCommandExecutor> execList = getInstanceCommandList(commandName,
                                    actualTargets, context.getLogger());
            for(InstanceCommandExecutor rac : execList) {
                ActionReport aReport = context.getActionReport().addSubActionsReport();
                try {
                    String result = rac.executeCommand(parameters);
                    aReport.setActionExitCode(ActionReport.ExitCode.SUCCESS);
                    aReport.setMessage(strings.getLocalString("glassfish.clusterexecutor.commandSuccessful",
                            "Command " + commandName + "executed successfully on server instance " +
                            rac.getServer().getName() + ";" + result, commandName, rac.getServer().getName()));
                } catch (CommandException cmdEx) {
                    aReport.setActionExitCode(getReturnValueFor(onFailure));
                    aReport.setMessage(strings.getLocalString("glassfish.clusterexecutor.commandFailed",
                            "Command " + commandName + "failed on server instance " +
                            rac.getServer().getName() + ":", commandName,
                            rac.getServer().getName()));
                    aReport.setFailureCause(cmdEx);
                    if(returnValue.compareTo(ActionReport.ExitCode.SUCCESS)==0)
                        returnValue = getReturnValueFor(onFailure);
                }
            }
        } catch (Exception ex) {
            ActionReport aReport = context.getActionReport().addSubActionsReport();
            aReport.setActionExitCode(getReturnValueFor(onFailure));
            aReport.setMessage(strings.getLocalString("glassfish.clusterexecutor.replicationfailed",
                    "Error during command replication; Reason : " + ex.getLocalizedMessage(),
                    ex.getLocalizedMessage()));
            aReport.setFailureCause(ex);
            context.getLogger().severe(strings.getLocalString("glassfish.clusterexecutor.replicationfailed",
                            "Error during command replication; Reason : " + ex.getLocalizedMessage(),
                            ex.getLocalizedMessage()));
            if(returnValue.compareTo(ActionReport.ExitCode.SUCCESS)==0)
                returnValue = getReturnValueFor(onFailure);
        }
        return returnValue; 
    }

    /**
     * Given the name of a cluster, return the list of servers assosicated with the cluster
     * @param clusterName name of cluster
     * @return List<Server> the list of servers associated with the given cluster
     */
    private List<Server> getInstancesForCluster(String clusterName) {
        ArrayList<Server> instanceList = new ArrayList<Server>();
        Cluster cluster = null;

        List<Cluster> clList = domain.getClusters().getCluster();
        for(Cluster cl : clList) {
            if(clusterName.equals(cl.getName())) {
                cluster = cl;
                break;
            }
        }
        if(cluster != null) {
            String clusterConfigName = cluster.getConfigRef();
            List<Server> svrList = domain.getServers().getServer();
            for(Server svr : svrList) {
                if(clusterConfigName.equals(svr.getConfigRef())) {
                    instanceList.add(svr);
                }
            }
        }
        return instanceList;
    }

    /**
     * Given an list of server instances, create the InstanceCommandExecutor objects
     */
    private List<InstanceCommandExecutor> getInstanceCommandList(String commandName,
                                                         List<Server> servers, Logger logger) throws CommandException {
        ArrayList<InstanceCommandExecutor> list = new ArrayList<InstanceCommandExecutor>();
        for(Server svr : servers) {
            NetworkListener adminListener =
              domain.getConfigs().getConfigByName(svr.getConfigRef()).getNetworkConfig().getNetworkListener("admin-listener");
            String host = svr.getNodeAgentRef();
            /**
             * TODO : Why no admin-listener for config pointed to by server instance?
            int port = Integer.parserInt(adminListener.getPort());
             */
            // TODO : Remove all these checks and hardcoded stuff once CLI is ready
            int port = Integer.parseInt("4848");
            if("instance1".equals(svr.getName()))
                port = Integer.parseInt("14848");
            if("instance2".equals(svr.getName()))
                port = Integer.parseInt("24848");
            NetworkListener httpListener =
                domain.getConfigs().getConfigByName(svr.getConfigRef()).getNetworkConfig().getNetworkListener("http-listener-1");
            String portStr = svr.getPropertyValue("ASADMIN_LISTENER_PORT");
            portStr = svr.getPropertyValue("JMS_PROVIDER_PORT");
            portStr = svr.getPropertyValue("HTTP_SSL_LISTENER_PORT");
            if(httpListener != null)
                portStr = httpListener.getPort();
            String adminListPort =  adminListener.getPort();
            //TODO : Replace code till here once CLI is ready

            list.add(new InstanceCommandExecutor(commandName, svr, host, port, logger));
        }
        return list;
    }

    private ActionReport.ExitCode getReturnValueFor(FailurePolicy policy) {
        ActionReport.ExitCode retValue = ActionReport.ExitCode.FAILURE;
        switch(policy) {
            case Ignore:
                retValue = ActionReport.ExitCode.SUCCESS;
                break;
            case Error:
                retValue = ActionReport.ExitCode.FAILURE;
                break;
            case Warn:
                retValue = ActionReport.ExitCode.WARNING;
                break;
        }
        return retValue;
    }
}
