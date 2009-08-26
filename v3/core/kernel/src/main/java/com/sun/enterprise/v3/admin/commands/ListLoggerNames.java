package com.sun.enterprise.v3.admin.commands;

import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.ActionReport;

import com.sun.common.util.logging.LoggingConfigImpl;
import com.sun.common.util.logging.LoggingXMLNames;

import java.io.IOException;
import java.util.Map;
import java.util.Enumeration;
import java.util.Set;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
/**
 * Created by IntelliJ IDEA.
 * User: cmott
 * Date: Aug 26, 2009
 * Time: 5:32:17 PM
 * To change this template use File | Settings | File Templates.
 */
@Service(name="list-logger-names")
public class ListLoggerNames implements AdminCommand {

    @Inject
    LoggingConfigImpl loggingConfig;

    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        try {
            Map<String,String>  props = loggingConfig.getLoggingProperties();
            Set<String> keys = props.keySet();
            for (String logger : keys)   {
                if (logger.endsWith(".level") && !logger.equals("javax.level")) {
                    final ActionReport.MessagePart part = report.getTopMessagePart()
                        .addChild();
                    part.setMessage(logger.substring(0,logger.lastIndexOf(".level")));
                }

			}

            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        } catch (IOException ex) {
            String str = ex.getMessage();
            report.setMessage("Could not get Logger names"+ str);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(ex);
        }

    }
}
