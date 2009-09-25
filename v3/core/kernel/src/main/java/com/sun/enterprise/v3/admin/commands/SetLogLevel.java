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
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;


/**
 * Created by IntelliJ IDEA.
 * User: cmott
 * Date: Jul 8, 2009
 * Time: 11:48:20 AM
 * To change this template use File | Settings | File Templates.
 */

@Service(name="set-log-level")
public class SetLogLevel implements AdminCommand {

    @Param(name="name_value", primary=true, separator=':')
        Properties properties;
    
    @Inject
    LoggingConfigImpl loggingConfig;
    public void execute(AdminCommandContext context) {


        final ActionReport report = context.getActionReport();

        Map<String,String> m = new HashMap<String,String>();
        try {
            for (final Object key : properties.keySet()) {
                final String logger_name = (String) key;
                final String level = (String)properties.get(logger_name);
                m.put(logger_name+".level",(String)properties.get(logger_name) );

            }
            loggingConfig.updateLoggingProperties(m);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);


        }   catch (IOException e) {
            report.setMessage("Could not set logger levels ");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
    }

}
