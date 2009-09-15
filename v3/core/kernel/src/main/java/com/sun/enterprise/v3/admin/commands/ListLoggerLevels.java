package com.sun.enterprise.v3.admin.commands;

import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.ActionReport;

import com.sun.common.util.logging.LoggingConfigImpl;

import java.util.logging.Logger;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Set;

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
            Map<String,String>  props = loggingConfig.getLoggingProperties();
            Map sortedMap = new TreeMap();
            
            Set<String> keys = props.keySet();
            
            for (String name : keys)   {
                sortedMap.put(name, props.get(name));
            }
            Iterator it = sortedMap.keySet().iterator();
            String name;
            while (it.hasNext()) {
                name = (String)it.next();
                if (name.endsWith(".level") && !name.equals(".level")) {
                final ActionReport.MessagePart part = report.getTopMessagePart()
                    .addChild();
                String n = name.substring(0,name.lastIndexOf(".level"));
                part.setMessage(n + ": "+ (String)sortedMap.get(name));
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
