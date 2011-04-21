package org.glassfish.vmcluster.commands;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.Supplemental;
import org.glassfish.vmcluster.spi.*;
import org.glassfish.vmcluster.util.RuntimeContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

/**
 * Supplemental command to stop the virtual machine when instances are stopped.
 * @author Jerome Dochez
 */
@Service
@Supplemental(value = "stop-instance", on= Supplemental.Timing.After )
@Scoped(PerLookup.class)
@CommandLock(CommandLock.LockType.NONE)
public class SupplementalStopInstance implements AdminCommand {

    @Param(name="instance_name", primary = true)
    String instanceName;

    @Param(name="_vmShutdown", optional=true, defaultValue = "true")
    private String vmShutdown;

    @Inject
    GroupManagement groups;

    @Override
    public void execute(AdminCommandContext context) {
        if (!Boolean.valueOf(vmShutdown)) {
                context.getActionReport().setActionExitCode(ActionReport.ExitCode.SUCCESS);
                return;
        }
        String groupName = instanceName.substring(0, instanceName.indexOf("_"));
        String vmName = instanceName.substring(instanceName.lastIndexOf("_")+1, instanceName.length()-"Instance".length());

        Group group = groups.byName(groupName);
        try {
            VirtualMachine vm = group.vmByName(vmName);
            VirtualMachineInfo vmInfo = vm.getInfo();
            if (Machine.State.SUSPENDED.equals(vmInfo.getState())) {
                context.getActionReport().setActionExitCode(ActionReport.ExitCode.SUCCESS);
                return;
            }
            vm.stop();
        } catch (VirtException e) {
            RuntimeContext.logger.warning(e.getMessage());
        }
    }
}
