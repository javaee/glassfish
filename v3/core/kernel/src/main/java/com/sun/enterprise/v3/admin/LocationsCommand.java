package com.sun.enterprise.v3.admin;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import com.sun.appserv.server.util.Version;
import com.sun.enterprise.v3.server.ServerEnvironment;

/**
 * Locations command to indicate where this server is installed.
 * @author Jerome Dochez
 */
@Service(name="__locations")
@I18n("version.command")
public class LocationsCommand implements AdminCommand {
    
    @Inject
    ServerEnvironment env;

    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        report.getTopMessagePart().addProperty("Base-Root", env.getStartupContext().getRootDirectory().getParent());
        report.getTopMessagePart().addProperty("Domain-Root", env.getDomainRoot());
    }
}
