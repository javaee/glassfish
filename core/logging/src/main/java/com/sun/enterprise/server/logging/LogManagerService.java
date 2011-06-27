/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.server.logging;

import com.sun.common.util.logging.LoggingConfigImpl;
import com.sun.common.util.logging.LoggingOutputStream;
import com.sun.common.util.logging.LoggingXMLNames;
import com.sun.enterprise.admin.monitor.callflow.Agent;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.module.bootstrap.EarlyLogHandler;
import com.sun.enterprise.util.EarlyLogger;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.v3.logging.AgentFormatterDelegate;
import com.sun.logging.LogDomains;
import org.glassfish.api.admin.FileMonitoring;
import org.glassfish.internal.api.Init;
import org.glassfish.internal.config.UnprocessedConfigListener;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.*;
import org.jvnet.hk2.config.UnprocessedChangeEvent;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.*;

/**
 * Reinitialzie the log manager using our logging.properties file.
 *
 * @author Jerome Dochez
 * @author Carla Mott
 * @author Naman Mehta
 */

@Service
@Scoped(Singleton.class)
public class LogManagerService implements Init, PostConstruct, PreDestroy, org.glassfish.internal.api.LogManager {

    @Inject
    ServerEnvironmentImpl env;

    @Inject
    Habitat habitat;

    @Inject(optional = true)
    Agent agent = null;

    @Inject
    FileMonitoring fileMonitoring;

    @Inject
    LoggingConfigImpl loggingConfig;

    @Inject
    UnprocessedConfigListener ucl;

    @Inject
    Domain domain;

    final Map<String, Handler> gfHandlers = new HashMap<String, Handler>();
    Logger logger = LogDomains.getLogger(LogManagerService.class, LogDomains.CORE_LOGGER);

    PrintStream oStdOutBackup = System.out;
    PrintStream oStdErrBackup = System.err;

    String serverLogFileDetail = "";

        private final String SERVER_LOG_FILE_PROPERTY = "com.sun.enterprise.server.logging.GFFileHandler.file"; 

    /*
        Returns properties based on the DAS/Cluster/Instance
      */

    public Map<String, String> getLoggingProperties() throws IOException {

        Server targetServer = domain.getServerNamed(env.getInstanceName());

        Map<String, String> props = null;

        if (targetServer != null) {
            if (targetServer.isDas()) {
                props = loggingConfig.getLoggingProperties();
            } else if (targetServer.getCluster() != null) {
                props = loggingConfig.getLoggingProperties(targetServer.getCluster().getConfigRef());
            } else if (targetServer.isInstance()) {
                props = loggingConfig.getLoggingProperties(targetServer.getConfigRef());
            } else {
                props = loggingConfig.getLoggingProperties();
            }
        } else {
            props = loggingConfig.getLoggingProperties();
        }

        return props;
    }

    /*
        Returns logging file to be monitor during server is running.
     */

    public File getLoggingFile() throws IOException {

        File file = null;

        Server targetServer = domain.getServerNamed(env.getInstanceName());

        if (targetServer != null) {
            if (targetServer.isDas()) {
                file = new File(env.getConfigDirPath(), ServerEnvironmentImpl.kLoggingPropertiesFileName);
            } else if (targetServer.getCluster() != null) {
                String pathForLogging = env.getConfigDirPath() + File.separator + targetServer.getCluster().getConfigRef();
                File dirForLogging = new File(pathForLogging);

                file = new File(dirForLogging, ServerEnvironmentImpl.kLoggingPropertiesFileName);

                if (!file.exists()) {
                    loggingConfig.copyLoggingPropertiesFile(dirForLogging);
                    file = new File(dirForLogging, ServerEnvironmentImpl.kLoggingPropertiesFileName);
                }
            } else if (targetServer.isInstance()) {
                String pathForLogging = env.getConfigDirPath() + File.separator + targetServer.getConfigRef();
                File dirForLogging = new File(pathForLogging);

                file = new File(dirForLogging, ServerEnvironmentImpl.kLoggingPropertiesFileName);

                if (!file.exists()) {
                    loggingConfig.copyLoggingPropertiesFile(dirForLogging);
                    file = new File(dirForLogging, ServerEnvironmentImpl.kLoggingPropertiesFileName);
                }
            } else {
                file = new File(env.getConfigDirPath(), ServerEnvironmentImpl.kLoggingPropertiesFileName);
            }
        } else {
            file = new File(env.getConfigDirPath(), ServerEnvironmentImpl.kLoggingPropertiesFileName);
        }
        return file;
    }

    /**
     * Initialize the loggers
     */
    public void postConstruct() {

        // if the system property is already set, we don't need to do anything
        if (System.getProperty("java.util.logging.config.file") != null) {
            return;
        }

        // logging.properties massaging.
        final LogManager logMgr = LogManager.getLogManager();
        File logging = null;

        // reset settings
        try {
            logging = getLoggingFile();
            System.setProperty("java.util.logging.config.file", logging.getAbsolutePath());
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
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot read logging configuration file : ", e);
        }

        FormatterDelegate agentDelegate = null;
        if (agent != null) {
            agentDelegate = new AgentFormatterDelegate(agent);

        }

        // force the ConsoleHandler to use GF formatter
        String formatterClassname = null;
        try {

            Map<String, String> props = getLoggingProperties();
            formatterClassname = props.get("java.util.logging.ConsoleHandler.formatter");
            Class formatterClass = LogManagerService.class.getClassLoader().loadClass(formatterClassname);
            UniformLogFormatter formatter = (UniformLogFormatter) formatterClass.newInstance();
            for (Handler handler : logMgr.getLogger("").getHandlers()) {
                // only get the ConsoleHandler
                handler.setFormatter(formatter);
            }

            //setting initial server log file name which is used later to initiate PropertyChangeEvent for file name change
            serverLogFileDetail = props.get(SERVER_LOG_FILE_PROPERTY);

        } catch (java.io.IOException ex) {
            logger.log(Level.SEVERE, "logging.read.error", ex);

        } catch (ClassNotFoundException exc) {
            logger.log(Level.SEVERE, "logging.formatter.load ", formatterClassname);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "logging.set.formatter ", e);
        }

        Collection<Handler> handlers = habitat.getAllByContract(Handler.class);
        if (handlers != null && handlers.size() > 0) {
            synchronized (logMgr) {
                // I need to reset the formatter for the existing console handlers
                Enumeration<String> loggerNames = logMgr.getLoggerNames();
                while (loggerNames.hasMoreElements()) {
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

            Map<String, String> map = getLoggingProperties();

            String filterClassName = map.get(LoggingXMLNames.xmltoPropsMap.get("log-filter"));
            if (filterClassName != null) {
                Filter filterClass = habitat.getComponent(java.util.logging.Filter.class, filterClassName);
                Logger rootLogger = Logger.global.getParent();
                if (rootLogger != null) {
                    rootLogger.setFilter(filterClass);
                }
            }
        } catch (java.io.IOException ex) {

        }


        // redirect stderr and stdout, a better way to do this
        //http://blogs.sun.com/nickstephen/entry/java_redirecting_system_out_and

        Logger _ologger = LogDomains.getLogger(LogManagerService.class, LogDomains.STD_LOGGER);
        LoggingOutputStream los = new LoggingOutputStream(_ologger, Level.INFO);
        LoggingOutputStream.LoggingPrintStream pout = los.new LoggingPrintStream(los);
        System.setOut(pout);

        Logger _elogger = LogDomains.getLogger(LogManagerService.class, LogDomains.STD_LOGGER);
        los = new LoggingOutputStream(_elogger, Level.SEVERE);
        LoggingOutputStream.LoggingPrintStream perr = los.new LoggingPrintStream(los);
        System.setErr(perr);

        /*Logger anonymousLogger = Logger.getAnonymousLogger();
       LoggingOutputStream los = new LoggingOutputStream(anonymousLogger, Level.INFO);
       PrintStream pout = new PrintStream(los,true);
       synchronized (pout) {
           System.setOut(pout);
       }


       los = new LoggingOutputStream(anonymousLogger, Level.SEVERE);
       PrintStream perr = new PrintStream(los,true);
       synchronized (perr) {
           System.setErr(perr);
       } */


        // finally listen to changes to the logging.properties file
        if (logging != null) {
            fileMonitoring.monitors(logging, new FileMonitoring.FileChangeListener() {
                public void changed(File changedFile) {
                    synchronized (gfHandlers) {
                        try {

                            Map<String, String> props = getLoggingProperties();
                            if (props == null)
                                return;
                            Set<String> keys = props.keySet();
                            for (String a : keys) {
                                if (a.endsWith(".level")) {
                                    String n = a.substring(0, a.lastIndexOf(".level"));
                                    Level l = Level.parse(props.get(a));
                                    if (logMgr.getLogger(n) != null) {
                                        logMgr.getLogger(n).setLevel(l);
                                    } else if (gfHandlers.containsKey(n)) {
                                        // check if this is one of our handlers
                                        Handler h = (Handler) gfHandlers.get(n);
                                        h.setLevel(l);
                                    } else if (n.equals("java.util.logging.ConsoleHandler")) {
                                        Logger logger = Logger.global.getParent();
                                        Handler[] h = logger.getHandlers();
                                        for (int i = 0; i < h.length; i++) {
                                            String name = h[i].toString();
                                            if (name.contains("java.util.logging.ConsoleHandler"))
                                                h[i].setLevel(l);
                                        }
                                    }

                                } else if (a.equals(SERVER_LOG_FILE_PROPERTY)) {
                                    //check if file name was changed and send notification
                                    if (!props.get(a).equals(serverLogFileDetail)) {
                                        PropertyChangeEvent pce = new PropertyChangeEvent(this, a, serverLogFileDetail, props.get(a));
                                        UnprocessedChangeEvents ucel = new UnprocessedChangeEvents(new UnprocessedChangeEvent(pce, "server log filename changed."));
                                        List<UnprocessedChangeEvents> b = new ArrayList();
                                        b.add(ucel);
                                        ucl.unprocessedTransactedEvents(b);
                                    }
                                }

                            }

                            logger.log(Level.INFO, "logging.update.levels");
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, "logging.read.error", e);
                        }
                    }

                }

                public void deleted(File deletedFile) {
                    logger.log(Level.INFO, "logging.properties file removed, updating log levels disabled");
                }
            });
        }
        // Log the messages that were generated very early before this Service
        // started.  Just use our own logger...
        List<EarlyLogger.LevelAndMessage> catchUp = EarlyLogger.getEarlyMessages();

        if (!catchUp.isEmpty()) {
            for (EarlyLogger.LevelAndMessage levelAndMessage : catchUp) {
                logger.log(levelAndMessage.level, levelAndMessage.msg);
            }
            catchUp.clear();
        }

        ArrayBlockingQueue<LogRecord> catchEarlyMessage = EarlyLogHandler.earlyMessages;

        while (!catchEarlyMessage.isEmpty()) {
            LogRecord logRecord = catchEarlyMessage.poll();
            if (logRecord != null) {
                logger.log(logRecord);
            }
        }

    }

    public void addHandler(Handler handler) {
        Logger rootLogger = Logger.global.getParent();
        if (rootLogger != null) {
            synchronized (gfHandlers) {
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
            System.setOut(oStdOutBackup);
            System.setErr(oStdErrBackup);
            System.out.println("Completed shutdown of Log manager service");
        } catch (ComponentException e) {
            e.printStackTrace();
        }
    }

}
