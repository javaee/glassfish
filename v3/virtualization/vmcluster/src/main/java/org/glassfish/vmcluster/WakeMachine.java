package org.glassfish.vmcluster;

import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.vmcluster.config.Virtualizations;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: 1/13/11
 * Time: 11:00 AM
 * To change this template use File | Settings | File Templates.
 */
@Service(name="wake-machine")
public class WakeMachine implements AdminCommand {

    @Param(optional=true)
    String group=null;

    @Param(primary = true)
    String machineName;

    @Inject
    Virtualizations virtualizations;

    @Override
    public void execute(AdminCommandContext context) {

    }
}
