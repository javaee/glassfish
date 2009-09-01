package com.sun.enterprise.v3.admin.commands;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;

import com.sun.common.util.logging.LoggingConfigImpl;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: cmott
 * Date: Jul 8, 2009
 * Time: 11:48:20 AM
 * To change this template use File | Settings | File Templates.
 */
@Service(name="set-log-level")
public class SetLogLevel implements AdminCommand {
    @Param(name="logger-name", primary=true)
    String logger_name;

    @Param(name="level")
    String level;

    @Inject
    LoggingConfigImpl loggingConfig;

    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        try {
            loggingConfig.setLoggingProperty(logger_name+".level", level);
        }  catch (IOException e) {
            report.setMessage("Could not set logger level "+ logger_name);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

    }    

}
