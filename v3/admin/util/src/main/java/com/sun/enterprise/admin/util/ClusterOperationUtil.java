package com.sun.enterprise.admin.util;

import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.StringUtils;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.*;
import org.glassfish.internal.api.Target;
import org.glassfish.config.support.CommandTarget;
import org.jvnet.hk2.component.Habitat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
public class ClusterOperationUtil {
    private static final LocalStringManagerImpl strings =
                        new LocalStringManagerImpl(ClusterOperationUtil.class);

    /**
     * Replicate a given command on given list of targ
     */
    public static ActionReport.ExitCode replicateCommand(String commandName,
                                                   FailurePolicy failPolicy,
                                                   FailurePolicy offlinePolicy,
                                                   List<Server> instancesForReplication,
                                                   AdminCommandContext context,
                                                   ParameterMap parameters,
                                                   Habitat habitat) {

        // TODO : Use Executor service to spray the commands on all instances

        ActionReport.ExitCode returnValue = ActionReport.ExitCode.SUCCESS;
        InstanceState instanceState = habitat.getComponent(InstanceState.class);
        try {
            List<InstanceCommandExecutor> execList = getInstanceCommandList(commandName,
                                    instancesForReplication, context.getLogger(), habitat);
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
                            aReport.setMessage(strings.getLocalString("glassfish.clusterexecutor.commandWarning",
                                "WARNING : Command {0} did not complete successfully on server instance {1} : {2}",
                                    commandName, rac.getServer().getName(), cmdEx.getMessage()));
                    }
                    aReport.setActionExitCode(finalResult);
                    if(returnValue.equals(ActionReport.ExitCode.SUCCESS))
                        returnValue = finalResult;
                    instanceState.setState(rac.getServer().getName(), InstanceState.StateType.RESTART_REQUIRED);
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

    public static ActionReport.ExitCode replicateCommand(String commandName,
                                                   FailurePolicy failPolicy,
                                                   FailurePolicy offlinePolicy,
                                                   Collection<String> targetNames,
                                                   AdminCommandContext context,
                                                   ParameterMap parameters,
                                                   Habitat habitat) {
        ActionReport.ExitCode result = ActionReport.ExitCode.SUCCESS;
        Target targetService = habitat.getComponent(Target.class);
        for(String t : targetNames) {
            if(CommandTarget.DAS.isValid(habitat, t) ||
                    CommandTarget.DOMAIN.isValid(habitat, t))
                continue;
            //If the target is a cluster and dynamic reconfig enabled is false, no replication
            if(targetService.isCluster(t)) {
                String dynRecfg = targetService.getClusterConfig(t).getDynamicReconfigurationEnabled();
                if(Boolean.FALSE.equals(Boolean.valueOf(dynRecfg))) {
                    ActionReport aReport = context.getActionReport().addSubActionsReport();
                    aReport.setActionExitCode(ActionReport.ExitCode.WARNING);
                    aReport.setMessage(strings.getLocalString("glassfish.clusterexecutor.dynrecfgdisabled",
                            "WARNING : The command was not replicated to all cluster instances because the" +
                                    " dynamic-reconfig-enabled flag is set to false for cluster {0}", t));
                    InstanceState instanceState = habitat.getComponent(InstanceState.class);
                    for(Server s : targetService.getInstances(t))
                        instanceState.setState(s.getName(), InstanceState.StateType.RESTART_REQUIRED);
                    result = ActionReport.ExitCode.WARNING;
                    continue;
                }
            }
            parameters.set("target", t);
            ActionReport.ExitCode returnValue = ClusterOperationUtil.replicateCommand(commandName,
                    failPolicy, offlinePolicy, targetService.getInstances(t), context, parameters, habitat);
            if(!returnValue.equals(ActionReport.ExitCode.SUCCESS)) {
                result = returnValue;
            }
        }
        return result;
    }

    /**
     * Given an list of server instances, create the InstanceCommandExecutor objects
     */
    private static List<InstanceCommandExecutor> getInstanceCommandList(String commandName,
                                                         List<Server> servers, Logger logger, Habitat habitat) throws CommandException {
        ArrayList<InstanceCommandExecutor> list = new ArrayList<InstanceCommandExecutor>();
        RemoteInstanceCommandHelper rich = new RemoteInstanceCommandHelper(habitat);
        for(Server svr : servers) {
            String host = svr.getHost();
            int port = rich.getAdminPort(svr);
            list.add(new InstanceCommandExecutor(commandName, svr, host, port, logger));
        }
        return list;
    }
}
