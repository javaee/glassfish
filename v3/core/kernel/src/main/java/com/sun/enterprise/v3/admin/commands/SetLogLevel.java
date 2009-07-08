package com.sun.enterprise.v3.admin.commands;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.Param;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;

import com.sun.common.util.logging.LoggingConfigImpl;

import java.io.IOException;

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
        //check the logger name
        try {
            String propertyName = logger_name+".level";
            loggingConfig.setLoggingProperty(propertyName, level);
        } catch (IOException e) {

        }

    }    

}
