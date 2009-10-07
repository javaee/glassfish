package com.sun.enterprise.v3.admin.commands;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;

import com.sun.common.util.logging.LoggingConfigImpl;
import com.sun.enterprise.util.LocalStringManagerImpl;

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
  
/*
 * Set Logger Level Command
 *
 * Updates one or more loggers' level
 *
 * Usage: set-log-level [-?|--help=false]
 * (logger_name=logging_value)[:logger_name=logging_value]*
 *
 */

@Service(name="set-log-level")
@I18n("set.log.level")
public class SetLogLevel implements AdminCommand {

    @Param(name="name_value", primary=true, separator=':')
        Properties properties;
    
    @Inject
    LoggingConfigImpl loggingConfig;

    String[] validLevels = {"SEVERE", "WARNING", "INFO", "FINE", "FINER", "FINEST"};
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(SetLogLevel.class);


    public void execute(AdminCommandContext context) {


        final ActionReport report = context.getActionReport();

        Map<String,String> m = new HashMap<String,String>();
        try {
            for (final Object key : properties.keySet()) {
                final String logger_name = (String) key;
                final String level = (String)properties.get(logger_name);
                // that is is a valid level
                boolean vlvl=false;
                for (String s: validLevels) {
                    if (s.equals(level) ) {
                        m.put(logger_name+".level", level );
                        vlvl=true;
                        break;
                    }
                }
                if (!vlvl) {
                    report.setMessage(localStrings.getLocalString("set.log.level.invalid",
                    "Invalid logger level found {0}.  Valid levels are: SEVERE, WARNING, INFO, FINE, FINER, FINEST", level));
                }


            }
            loggingConfig.updateLoggingProperties(m);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);


        }   catch (IOException e) {
            report.setMessage("Could not set logger levels ");
            report.setMessage(localStrings.getLocalString("set.log.level.failed",
                    "Could not set logger levels."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
    }

}
