package com.sun.enterprise.v3.admin.commands;

import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.ActionReport;

import com.sun.common.util.logging.LoggingConfigImpl;

import java.util.logging.LogManager;
import java.util.Enumeration;

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
        LogManager logMgr = LogManager.getLogManager();
        Enumeration<String> logNames = logMgr.getLoggerNames();
        while (logNames.hasMoreElements()) {
             final ActionReport.MessagePart part = report.getTopMessagePart()
                    .addChild();
             part.setMessage(logNames.nextElement());
        }

    }
}
