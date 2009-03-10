/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.services.impl;

import com.sun.enterprise.admin.monitor.callflow.Agent;
import com.sun.enterprise.server.logging.FormatterDelegate;
import com.sun.enterprise.server.logging.UniformLogFormatter;
import com.sun.enterprise.server.logging.LoggingConfigImpl;
import com.sun.enterprise.v3.logging.AgentFormatterDelegate;
import com.sun.common.util.logging.LoggingOutputStream;
import com.sun.logging.LogDomains;
import org.glassfish.internal.api.Init;
import org.glassfish.internal.api.Globals;
import org.glassfish.api.branding.Branding;
import org.glassfish.api.admin.FileMonitoring;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Reinitialzie the log manager using our logging.properties file.
 *
 * @author Jerome Dochez
 * @author Carla Mott
 */

@Service
@Scoped(Singleton.class)
public class LogManagerService implements Init, PostConstruct, PreDestroy {

    @Inject
    Logger logger;    

    @Inject
    ServerEnvironmentImpl env;

    @Inject
    Habitat habitat;

    @Inject(optional=true)
    Agent agent=null;

    @Inject
    FileMonitoring fileMonitoring;

    @Inject
    LoggingConfigImpl loggingConfig;

    /**
     * Initialize the loggers
     */
    public void postConstruct() {

        // get the branding info and set it on the UniformLogFormatter
        UniformLogFormatter.branding = habitat.getByContract(Branding.class);
        
        // if the system property is already set, we don't need to do anything
        if (System.getProperty("java.util.logging.config.file")!=null) {
            return;
        }
        
        // logging.properties nassaging.
        final LogManager logMgr = LogManager.getLogManager();
        final File logging = new File(env.getConfigDirPath(), ServerEnvironmentImpl.kLoggingPropertiesFileNAme);
        System.setProperty("java.util.logging.config.file", logging.getAbsolutePath());
        // reset settings
        try {
            logMgr.readConfiguration();
        } catch(IOException e) {
             logger.log(Level.SEVERE, "Cannot read logging configuration file : ", e);
        }
        FormatterDelegate agentDelegate = null;
        if (agent!=null) {
            agentDelegate = new AgentFormatterDelegate(agent);

        }

        Collection<Handler> handlers = habitat.getAllByContract(Handler.class);
        if (handlers!=null && handlers.size()>0) {
            synchronized(logMgr) {
                // I need to reset the formatter for the existing console handlers
                Enumeration<String> loggerNames = logMgr.getLoggerNames();
                while(loggerNames.hasMoreElements()) {
                    String loggerName = loggerNames.nextElement();
                    logMgr.getLogger(loggerName);
                    for (Handler handler : logger.getHandlers()) {
                        if (handler.getFormatter() instanceof UniformLogFormatter) {
                            ((UniformLogFormatter) handler.getFormatter()).setDelegate(agentDelegate);
                        }
                    }
                }

                // add the new handlers to the root logger
                for (Handler handler : handlers) {
                    Logger rootLogger = Logger.global.getParent();
                    if (rootLogger!=null) {
                       rootLogger.addHandler(handler);
                    }
                }

            }
        }

        // redirect stderr and stdout
        LoggingOutputStream los = new LoggingOutputStream(Logger.getAnonymousLogger(), Level.INFO);
        PrintStream pout = new  PrintStream(los, true);
        System.setOut(pout);

        los = new LoggingOutputStream(Logger.getAnonymousLogger(), Level.SEVERE);
        PrintStream perr = new PrintStream(los, true);
        System.setErr(perr);
        

        // finally listen to changes to the logging.properties file
        if (logging!=null) {
            fileMonitoring.monitors(logging, new FileMonitoring.FileChangeListener() {
                public void changed(File changedFile) {
                    try {

                        Map<String,String> props = loggingConfig.getLoggingProperties();
                        Set<String> keys = props.keySet();
                        for (String a : keys)   {
                            if (a.endsWith(".level")) {
                                Level l = Level.parse(props.get(a));
                                if (logMgr.getLogger(a.substring(0,a.lastIndexOf(".level"))) != null) {
                                    logMgr.getLogger(a.substring(0,a.lastIndexOf(".level"))).setLevel(l);
                                }

                            }
                        }

                        logger.log(Level.INFO,"Updated log levels for loggers.");                                                     
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Cannot read logging.properties file : ", e);
                    }

                }
            });
        }

    }

    public void preDestroy() {
        //destroy the handlers
        try {
            for (Inhabitant<? extends Handler> i : habitat.getInhabitants(Handler.class)) {
                i.release();
            }
        } catch(ComponentException e) {
            e.printStackTrace();
        }
    }

}
