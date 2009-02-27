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

import com.sun.enterprise.server.logging.LoggingConfigImpl;
import com.sun.enterprise.server.logging.LoggingPropertyNames;

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
            final Map<String, String> m = new HashMap<String, String>(){{
            //get log-service elements and add to map so they will be set in
            // logging.properties
            put("file", logService.getFile());
            put("use-system-logging", logService.getUseSystemLogging());
            //this can have multiple values so need to add
                // check if handler or filter are null, as no default value 
            put("log-handler", logService.getLogHandler());
            put("log-filter", logService.getLogFilter());
            put("log-to-console",logService.getLogToConsole());
            put("log-rotation-limit-in-bytes",logService.getLogRotationLimitInBytes());
            put("log-rotation-timelimit-in-minutes", logService.getLogRotationTimelimitInMinutes());
            put("alarms", logService.getAlarms());
            put("retain-error-statistics-for-hours", logService.getRetainErrorStatisticsForHours());
                }};
            //Get the logLevels too
            /*
            ModuleLogLevels logLevels = logService.getModuleLogLevels();
            Properties p = logLevels.getProperty();
            Enumeration e = p.propertyNames();
            while (e.hasMoreElements()) {
                    String key = (String)e.nextElement();
                    //System.out.println("Debug "+key+ " " + p.getProperty(key));
                    m.put(key, p.getProperty(key));
            }
            */
            

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
