package com.sun.enterprise.v3.services.impl;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.LogService;
import com.sun.enterprise.config.serverbeans.ModuleLogLevels;

import com.sun.common.util.logging.LoggingConfigImpl;
import com.sun.common.util.logging.LoggingPropertyNames;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;


/**
 * Startup service to update existing domain.xml to move log-service entries to
 * logging.properties file.
 *
 * @author Carla Mott
 */
@Service
public class UpgradeLogging implements ConfigurationUpgrade, PostConstruct {
    
    @Inject ( name="server-config")
    Config config;
    
    @Inject
    LoggingConfigImpl logConfig;
    

    public void postConstruct() {
    	// v3 uses logging.properties to configure the logging facility.  
    	// move all log-service elements to logging.properties
    	
    	final LogService logService = config.getLogService();
    	// check if null and exit
    	if (logService == null )
    		return;
        try {

            //Get the logLevels
            ModuleLogLevels mll = logService.getModuleLogLevels();

            Map logLevels = mll.getAllLogLevels();
            logLevels.put("file", logService.getFile());
            logLevels.put("use-system-logging", logService.getUseSystemLogging());
            //this can have multiple values so need to add
            logLevels.put("log-handler", logService.getLogHandler());
            logLevels.put("log-filter", logService.getLogFilter());
            logLevels.put("log-to-console",logService.getLogToConsole());
            logLevels.put("log-rotation-limit-in-bytes",logService.getLogRotationLimitInBytes());
            logLevels.put("log-rotation-timelimit-in-minutes", logService.getLogRotationTimelimitInMinutes());
            logLevels.put("alarms", logService.getAlarms());
            logLevels.put("retain-error-statistics-for-hours", logService.getRetainErrorStatisticsForHours());
            final Map<String, String> m =  new HashMap<String,String>(logLevels);



            ConfigSupport.apply(new SingleConfigCode<Config>() {
                public Object run(Config c) throws PropertyVetoException, TransactionFailure {

                        try {
                        	//update logging.properties
                            logConfig.updateLoggingProperties(m);
                            
                            c.setLogService(null);

                        } catch (IOException e) {
                        	Logger.getAnonymousLogger().log(Level.SEVERE, "Failure while upgrading log-service. Could not update logging.properties file. ", e);
                        }
                        return null;
                    }
                }, config);
            } catch(TransactionFailure tf) {
                Logger.getAnonymousLogger().log(Level.SEVERE, "Failure while upgrading log-service ", tf);
                throw new RuntimeException(tf);
            }        
    }
}
