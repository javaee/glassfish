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
import com.sun.enterprise.v3.logging.AgentFormatterDelegate;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.common.util.logging.LoggingOutputStream;
import com.sun.common.util.logging.LoggingXMLNames;
import com.sun.common.util.logging.LoggingConfigImpl;
import com.sun.enterprise.util.SystemPropertyConstants;


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
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Filter;
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

    @Inject ( name="server-config")
    Config config;

    @Inject
    LoggingConfigImpl loggingConfig;

    final Map <String, Handler> gfHandlers = new HashMap <String,Handler>();
    
    /**
     * Initialize the loggers
     */
    public void postConstruct() {
        
        // if the system property is already set, we don't need to do anything
        if (System.getProperty("java.util.logging.config.file")!=null) {
            return;
        }
        
        // logging.properties nassaging.
        final LogManager logMgr = LogManager.getLogManager();
        File logging = new File(env.getConfigDirPath(), ServerEnvironmentImpl.kLoggingPropertiesFileName);
        System.setProperty("java.util.logging.config.file", logging.getAbsolutePath());
        // reset settings


        try {
            if (!logging.exists()) {
                Logger.getAnonymousLogger().log(Level.WARNING, logging.getAbsolutePath() + " not found, creating new file from template.");
                String rootFolder = env.getProps().get(com.sun.enterprise.util.SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
                String templateDir = rootFolder + File.separator + "lib" + File.separator + "templates";
                File src = new File(templateDir, ServerEnvironmentImpl.kLoggingPropertiesFileName);
                File dest = new File(env.getConfigDirPath(), ServerEnvironmentImpl.kLoggingPropertiesFileName);
                FileUtils.copy(src, dest);
                logging = new File(env.getConfigDirPath(), ServerEnvironmentImpl.kLoggingPropertiesFileName);
            }
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
                    addHandler(handler);
                }

            }
        }
        // add the filter if there is one
        try {
            Map<String,String> map = loggingConfig.getLoggingProperties();
            String filterClassName = map.get(LoggingXMLNames.xmltoPropsMap.get("log-filter"));
            if (filterClassName != null) {
                Filter filterClass = habitat.getComponent(java.util.logging.Filter.class,filterClassName);
                Logger rootLogger = Logger.global.getParent();
                if (rootLogger!=null) {
                       rootLogger.setFilter(filterClass);
                }
            }
        } catch (java.io.IOException ex){

        }

        // redirect stderr and stdout, a better way to do this
        //http://blogs.sun.com/nickstephen/entry/java_redirecting_system_out_and
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
                    synchronized(gfHandlers) {
                        try {

                            Map<String,String> props = loggingConfig.getLoggingProperties();
                            if ( props == null)
                                return;
                            Set<String> keys = props.keySet();
                            for (String a : keys)   {
                                if (a.endsWith(".level")) {
                                    String n = a.substring(0,a.lastIndexOf(".level"));
                                    Level l = Level.parse(props.get(a));
                                    if (logMgr.getLogger(n) != null) {
                                        logMgr.getLogger(n).setLevel(l);
                                    } else if (gfHandlers.containsKey(n)) {
                                        // check if this is one of our handlers
                                        Handler h = (Handler) gfHandlers.get(n);
                                        h.setLevel(l);
                                    } else if (n.equals("java.util.logging.ConsoleHandler")) {
                                        Logger logger = Logger.global.getParent();
                                        Handler[] h= logger.getHandlers();
                                        for(int i=0;i<h.length;i++){
                                            String name = h[i].toString();
                                            if(name.contains("java.util.logging.ConsoleHandler"))
                                                h[i].setLevel(l);
                                        }
                                    }

                                }

                            }
                            logger.log(Level.INFO,"Updated log levels for loggers.");
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, "Cannot read logging.properties file : ", e);
                        }
                    }

                }

                public void deleted(File deletedFile) {
                    logger.log(Level.INFO, "logging.properties file removed, updating log levels disabled");
                }
            });
        }
    }

    /**
     * Adds a new handler to the root logger
     * @param handler handler to be iadded.
     */
    public void addHandler(Handler handler) {
        Logger rootLogger = Logger.global.getParent();
        if (rootLogger!=null) {
            synchronized(gfHandlers) {
               rootLogger.addHandler(handler);
               String handlerName = handler.toString();
               gfHandlers.put(handlerName.substring(0, handlerName.indexOf("@")), handler);
            }
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
