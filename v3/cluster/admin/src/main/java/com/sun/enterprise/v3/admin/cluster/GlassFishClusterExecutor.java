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

import com.sun.enterprise.admin.util.Target;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.admin.remote.RemoteAdminCommand;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.StringUtils;
import com.sun.grizzly.config.dom.NetworkListener;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.*;
import org.glassfish.config.support.CommandTarget;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.common.util.admin.CommandModelImpl;
import org.glassfish.config.support.GenericCrudCommand;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.types.Property;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
@Service(name="GlassFishClusterExecutor")
public class GlassFishClusterExecutor implements ClusterExecutor, PostConstruct {

    @Inject
    private Domain domain;

    @Inject
    private ExecutorService threadExecutor;

    @Inject
    private Habitat habitat;

    @Inject(name= ServerEnvironment.DEFAULT_INSTANCE_NAME)
    protected Server server;

    @Inject
    private ServerEnvironment env;

    @Inject
    private Servers servers;

    @Inject
    private Configs configs;

    private static final LocalStringManagerImpl strings =
                        new LocalStringManagerImpl(GlassFishClusterExecutor.class);

    private RemoteInstanceCommandHelper helper;

    @Override
    public void postConstruct() {
        helper = new RemoteInstanceCommandHelper(env, servers, configs);
    }

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

        CommandModel model;
        try {
            CommandModelProvider c = (CommandModelProvider) command;
            model = c.getModel();
        } catch(ClassCastException e) {
            model = new CommandModelImpl(command.getClass());
        }
        org.glassfish.api.admin.Cluster clAnnotation = model.getClusteringAttributes();
        String targetName = parameters.getOne("target");
        if(targetName == null)
                targetName = "server";
        //Do replication only if this is DAS and only if the target is not "server", the default server or "domain"
        if((!CommandTarget.DAS.isValid(habitat, targetName))
                && (!CommandTarget.DOMAIN.isValid(habitat, targetName))) {
            Target target = habitat.getComponent(Target.class);

            //If the target is a cluster and dynamic reconfig enabled is false, no replication
            if(target.isCluster(targetName)) {
                String dynRecfg = target.getClusterConfig(targetName).getDynamicReconfigurationEnabled();
                if(Boolean.FALSE.equals(Boolean.valueOf(dynRecfg))) {
                    ActionReport aReport = context.getActionReport().addSubActionsReport();
                    aReport.setActionExitCode(ActionReport.ExitCode.WARNING);
                    aReport.setMessage(strings.getLocalString("glassfish.clusterexecutor.dynrecfgdisabled",
                            "WARNING : The command was not replicated to all cluster instances because the" +
                                    " dynamic-reconfig-enabled flag is set to false for cluster {0}", targetName));
                    return ActionReport.ExitCode.WARNING;
                }
            }
            List<Server> instancesForReplication = target.getInstances(targetName);
            if(instancesForReplication.size() == 0) {
                ActionReport aReport = context.getActionReport().addSubActionsReport();
                aReport.setActionExitCode(ActionReport.ExitCode.SUCCESS);
                aReport.setMessage(strings.getLocalString("glassfish.clusterexecutor.notargets",
                        "Did not find any suitable instances for target {0}; command executed on DAS only", targetName));
                return ActionReport.ExitCode.SUCCESS;
            }

            return(replicateCommand(commandName, (clAnnotation == null) ? FailurePolicy.Error : clAnnotation.ifFailure(),
                    (clAnnotation == null) ? FailurePolicy.Warn : clAnnotation.ifOffline(),
                    instancesForReplication, context, parameters));
        }
        return ActionReport.ExitCode.SUCCESS;
    }

    /**
     * Replicate a given command on given list of targ
     */
    private ActionReport.ExitCode replicateCommand(String commandName,
                                                   FailurePolicy failPolicy,
                                                   FailurePolicy offlinePolicy,
                                                   List<Server> instancesForReplication,
                                                   AdminCommandContext context,
                                                   ParameterMap parameters) {

        // TODO : Use Executor service to spray the commands on all instances

        ActionReport.ExitCode returnValue = ActionReport.ExitCode.SUCCESS;
        try {
            List<InstanceCommandExecutor> execList = getInstanceCommandList(commandName,
                                    instancesForReplication, context.getLogger());
            for(InstanceCommandExecutor rac : execList) {
                ActionReport aReport = context.getActionReport().addSubActionsReport();
                try {
                    rac.executeCommand(parameters);
                    aReport.setActionExitCode(ActionReport.ExitCode.SUCCESS);
                    if(StringUtils.ok(rac.getCommandOutput()))
                        aReport.setMessage(rac.getServer().getName() + " : " + rac.getCommandOutput());
                    else
                        aReport.setMessage(strings.getLocalString("glassfish.clusterexecutor.commandSuccessful",
                            "Command {0} executed successfully on server instance {1}", commandName,
                            rac.getServer().getName()));
                } catch (CommandException cmdEx) {
                    ActionReport.ExitCode finalResult;
                    if(cmdEx.getCause() instanceof java.net.ConnectException) {
                        finalResult = FailurePolicy.applyFailurePolicy(offlinePolicy, ActionReport.ExitCode.WARNING);
                        if(!finalResult.equals(ActionReport.ExitCode.FAILURE))
                            aReport.setMessage(strings.getLocalString("glassfish.clusterexecutor.warnoffline",
                                "WARNING : Instance {0} seems to be offline; Command was not replicated to that instance",
                                    rac.getServer().getName()));
                    } else {
                        finalResult = FailurePolicy.applyFailurePolicy(failPolicy, ActionReport.ExitCode.FAILURE);
                        if(finalResult.equals(ActionReport.ExitCode.FAILURE))
                            aReport.setMessage(strings.getLocalString("glassfish.clusterexecutor.commandFailed",
                                "Command {0} failed on server instance {1} : {2}", commandName, rac.getServer().getName(),
                                    cmdEx.getMessage()));
                        else
                            aReport.setMessage(strings.getLocalString("glassfish.clusterexecutor.commandWanring",
                                "WARNING : Command {0} did not complete successfully on server instance {1} : {2}",
                                    commandName, rac.getServer().getName(), cmdEx.getMessage()));
                    }
                    aReport.setActionExitCode(finalResult);
                    if(returnValue.equals(ActionReport.ExitCode.SUCCESS))
                        returnValue = finalResult;
                }
            }
        } catch (Exception ex) {
            ActionReport aReport = context.getActionReport().addSubActionsReport();
            ActionReport.ExitCode finalResult = FailurePolicy.applyFailurePolicy(failPolicy,
                    ActionReport.ExitCode.FAILURE);
            aReport.setActionExitCode(finalResult);
            aReport.setMessage(strings.getLocalString("glassfish.clusterexecutor.replicationfailed",
                    "Error during command replication : {0}", ex.getMessage()));
            context.getLogger().severe("Error during command replication; Reason : " +  ex.getLocalizedMessage());
            if(returnValue.equals(ActionReport.ExitCode.SUCCESS))
                returnValue = finalResult;
        }
        return returnValue;
    }

    /**
     * Given an list of server instances, create the InstanceCommandExecutor objects
     */
    private List<InstanceCommandExecutor> getInstanceCommandList(String commandName,
                                                         List<Server> servers, Logger logger) throws CommandException {
        ArrayList<InstanceCommandExecutor> list = new ArrayList<InstanceCommandExecutor>();
        for(Server svr : servers) {
            String host = helper.getHost(svr);
            int port = helper.getAdminPort(svr);
            list.add(new InstanceCommandExecutor(commandName, svr, host, port, logger));
        }
        return list;
    }
}
