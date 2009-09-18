package com.sun.enterprise.v3.admin.commands;

import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.ActionReport;

import com.sun.common.util.logging.LoggingConfigImpl;

import java.util.logging.Logger;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import java.io.IOException;
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
@Service(name="list-logger-levels")
public class ListLoggerLevels implements AdminCommand {

    @Inject
    LoggingConfigImpl loggingConfig;

    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        try {
            HashMap<String,String>  props = (HashMap)loggingConfig.getLoggingProperties();

            ArrayList keys = new ArrayList();
            keys.addAll(props.keySet());
            Collections.sort(keys);
            Iterator it2 = keys.iterator();
            while (it2.hasNext())  {
                String name = (String)it2.next();
                if (name.endsWith(".level") && !name.equals(".level")) {
                final ActionReport.MessagePart part = report.getTopMessagePart()
                    .addChild();
                String n = name.substring(0,name.lastIndexOf(".level"));
                part.setMessage(n + ": "+ (String)props.get(name));
                }
            }


        } catch (IOException ex){
          report.setMessage("Unable to get the logger names");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(ex);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);


    }
}
