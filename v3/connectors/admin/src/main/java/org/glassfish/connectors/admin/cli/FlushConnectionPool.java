package org.glassfish.connectors.admin.cli;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;

@Service(name = "flush-connection-pool")
@Scoped(PerLookup.class)
@I18n("flush.connection.pool")
public class FlushConnectionPool implements AdminCommand {
    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(FlushConnectionPool.class);

    @Param(name = "pool_name", primary = true)
    String poolName;

    @Inject
    Resources resources;

    @Inject
    ConnectorRuntime _runtime;

    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        if (!isPoolExist()) {
            report.setMessage(localStrings.getLocalString(
                    "flush.connection.pool.notexist",
                    "Resource pool {0} does not exist.", poolName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;

        }

        try {
            _runtime.flushConnectionPool(poolName);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (ConnectorRuntimeException e) {
            report.setMessage(localStrings.getLocalString("flush.connection.pool.fail",
                    "Failed to flush connection pool {0} due to {1}.", poolName, e.getMessage()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

    private boolean isPoolExist() {
        for (Resource resource : resources.getResources()) {
            if (resource instanceof ResourcePool) {
                if (((ResourcePool) resource).getName().equals(poolName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
