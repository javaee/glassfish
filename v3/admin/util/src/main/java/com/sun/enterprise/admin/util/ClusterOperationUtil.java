package com.sun.enterprise.admin.util;

import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.StringUtils;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.*;
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
        List<Server> serversForReplication = new ArrayList<Server>();
        Target targetService = habitat.getComponent(Target.class);
        for(String t : targetNames) {
            if(CommandTarget.DAS.isValid(habitat, t) ||
                    CommandTarget.DOMAIN.isValid(habitat, t))
                continue;
            serversForReplication.addAll(targetService.getInstances(t));
        }
        return ClusterOperationUtil.replicateCommand(commandName, failPolicy, offlinePolicy, serversForReplication,
                context, parameters, habitat);
    }

    /**
     * Given an list of server instances, create the InstanceCommandExecutor objects
     */
    private static List<InstanceCommandExecutor> getInstanceCommandList(String commandName,
                                                         List<Server> servers, Logger logger) throws CommandException {
        ArrayList<InstanceCommandExecutor> list = new ArrayList<InstanceCommandExecutor>();
        for(Server svr : servers) {
            //TODO : As of now, the node-agent-ref is used to indicate host info for instance. This may change later
            String host = svr.getNodeAgentRef();
            //TODO : The following piece of code is kludge - pending config API changes for tokens in MS2
            int port = 4848;
            List<SystemProperty> sprops = svr.getSystemProperty();
            for(SystemProperty p : sprops) {
                if("ASADMIN_LISTENER_PORT".equals(p.getName())) {
                    port = Integer.parseInt(p.getValue());
                    break;
                }
            }
            list.add(new InstanceCommandExecutor(commandName, svr, host, port, logger));
        }
        return list;
    }
}
