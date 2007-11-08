
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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



import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.Filter;
import java.util.logging.ErrorManager;

import java.util.List;
import java.util.Collection;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ModuleLogLevels;
import com.sun.enterprise.config.serverbeans.LogService;
import com.sun.enterprise.config.serverbeans.Property;
import org.jvnet.glassfish.api.Startup;
import com.sun.enterprise.admin.monitor.callflow.Agent;

import org.jvnet.hk2.component.ComponentManager;


/**
 * Class ServerLogManager is a subclass of LogManager for use within the 
 * Application Server. The FileandSyslogHandler is installed
 * on each logger created by this log manager. 
 *
 * @author Jerome Dochez
 */
public class ServerLogManager extends BaseLogManager implements Startup  {

    Config config;

    ComponentManager componentManager;

    Collection<Handler> handlers;


    // If there is any CustomHandler and/or CustomFilter we will plug that.
    private static Handler customHandler = null;
    private static Filter customFilter = null;    

    private static boolean customFilterError = false;

    private static boolean customHandlerError = false;


    public synchronized void initializeLoggers(ComponentManager componentManager, Config config) {

        this.config = config;
        this.componentManager = componentManager;
        handlers = componentManager.getComponents(Handler.class);
        
        for (Logger logger : super._unInitializedLoggers) {
            initializeLogger(logger);
        }
        super._unInitializedLoggers.clear();
    }

    /**
     * Returns the life expectency of the service
     *
     * @return the life expectency.
     */
    public Lifecycle getLifecycle() {
        return Lifecycle.SERVER;
    }    

    /**
     * When a new logger is created in the system, this method will be invoked
     * to intialize the log level appropriately and also to set the required
     * LogHandlers.
     */
    protected synchronized void initializeLogger(Logger logger) {

        if( config==null) {
            _unInitializedLoggers.add( logger );
        } else {
            internalInitializeLogger( logger );            
        }
    }

    /**
     *  Internal Method to initialize a list of unitialized loggers.
     */
    private void internalInitializeLogger( final Logger logger ) {
        
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<Object>() {
                public Object run() {
                    // Explicitly remove all handlers.
                    for (Handler h  : logger.getHandlers()) {
                        logger.removeHandler(h);
                    }

                    logger.setUseParentHandlers(false);
                    for (Handler handler : handlers) {
                        logger.addHandler(handler);   
                    }

                    Level logLevel = getConfiguredLogLevel(logger.getName());
                    if( logLevel != null ) {
                        logger.setLevel( logLevel );
                    }
                    postInitializeLogger( logger );
                    return null;
                }
            }
        );
    }

    /**
     *  This is where we plug in any custom Log Handler and Log Filter.
     */
    private void postInitializeLogger( final Logger logger ) {
        final Handler customHandler = getCustomHandler( );
        final Filter customFilter = getCustomFilter( );
        if( ( customHandler == null)
          &&( customFilter == null ) ) {
            return;
        }
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<Object>() {
                public Object run() {
                    if( customHandler != null ) {
                         logger.addHandler( customHandler );
                    }
                    if( customFilter != null ) {
                         logger.setFilter( customFilter );
                    }
                    return null;
                }
            }
        );
    } 

    /**
     *  If there is any custom handler we will use that.
     */
    private synchronized Handler getCustomHandler( ) {
        if( (customHandler != null ) 
          ||(customHandlerError) ) 
        {
            return customHandler;
        }
        LogService logService = getLogService( );
        if( logService == null ) {
            return null;
        }
        String customHandlerClassName = null;
        try {
            customHandlerClassName = logService.getLogHandler( );

            customHandler = (Handler) getInstance( customHandlerClassName );
            // We will plug in our UniformLogFormatter to the custom handler
            // to provide consistent results
            if( customHandler != null ) {
                customHandler.setFormatter( new UniformLogFormatter(componentManager.getComponent(Agent.class)) );
            }
        } catch( Exception e ) {
            customHandlerError = true; 
            new ErrorManager().error( "Error In Initializing Custom Handler " +
                customHandlerClassName, e, ErrorManager.GENERIC_FAILURE );
        }
        return customHandler;
    }

    /**
     *  If there is any Custom Filter we will use that.
     */
    private Filter getCustomFilter( ) {
        if(( customFilter != null ) 
          ||( customFilterError ) )
        {
            return customFilter;
        }
        LogService logService = getLogService( );
        if( logService == null ) {
            return null;
        }
        String customFilterClassName = null;
        try {
            customFilterClassName = logService.getLogFilter( );
            customFilter = (Filter) getInstance( customFilterClassName );
        } catch( Exception e ) {
            customFilterError = true;
            new ErrorManager().error( "Error In Instantiating Custom Filter " +
                customFilterClassName, e, ErrorManager.GENERIC_FAILURE );
        }
        return customFilter;
    }


    /**
     *  A Utility method to get the LogService Configuration element.
     */
    LogService getLogService( ) {

        return config.getLogService();
    }


    /**
     *  A Utility method to instantiate Custom Log Handler and Log Filter.
     */
    private static Object getInstance( final String className ) {
        if( className == null ) return null;
        return  java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<Object>() {
                public Object run() {
                    try {
                        ClassLoader cl =
                            Thread.currentThread().getContextClassLoader();
                        if (cl == null)
                            cl = ClassLoader.getSystemClassLoader();
                        return Class.forName( className, true,cl).newInstance();
                    } catch( Exception e ) {
                        new ErrorManager().error(
                            "Error In Instantiating Class " + className, e,
                            ErrorManager.GENERIC_FAILURE );
                    }
                    return null;
               }
           }
       );
    }


    /**
     * Given a logger name, this method returns its level as defined
     * in domain.xml (the app server config file).
     * _REVISIT_: 
     * 1. Replace multiple if (loggerName) checks with a Hashmap
     */
    public Level getConfiguredLogLevel(String loggerName) {
        
        Level logLevel = null;
        try {
            ModuleLogLevels allModulesLogLevels = config.getLogService().getModuleLogLevels();
            // _REVISIT_: Right now ModuleLogLevels element in Log-Service
            // is optional. If the user doesn't specify any module log levels
            // then we will use 'INFO' as the default. For 8.1 this should
            // be a required element.
            if( allModulesLogLevels == null ) { return Level.INFO; }
            if( allModulesLogLevels.getRoot( ).equals( "OFF" ) ) {
                return Level.OFF;
            }

            // _REVISIT_: This is a bad way of searching for a loggername match
            // clean this up after Technology Preview
            List<Property> elementProperties = allModulesLogLevels.getProperty();
            String logModName = ModuleToLoggerNameMapper.getModuleName(loggerName);

            if( elementProperties != null ) {
                for (Property property : elementProperties) {
                    if( property.getName().equals(loggerName) || property.getName().equals(logModName) ) {
                        return Level.parse( property.getValue());
                    }
                }
            }

        } catch ( Exception e ) {
            new ErrorManager().error( "Error In Setting Initial Loglevel", e,
                ErrorManager.GENERIC_FAILURE );
        } 
        return logLevel;
    }

   /**
     * @loggername  the logname (could be the name of a child logger of a module logger)
     * @return the module logger name.
     */
    public String getMatchedModuleLoggerName(String loggerName) {

       String name = ModuleToLoggerNameMapper.getModuleName(loggerName);
       if (name != null) {
           return name;
       }
       if (config==null) {
           return null;
       }
       try {
           ModuleLogLevels allModulesLogLevels = config.getLogService().getModuleLogLevels();
           List<Property> elementProperties = allModulesLogLevels.getProperty();
           if (elementProperties != null) {
               for (Property property : elementProperties) {
                   name = property.getName();
                   if (loggerName.startsWith(name)) {
                       return name;
                   }
               }
           }
       } catch (Exception e) {
           new ErrorManager().error("Error In Setting Initial Loglevel", e,
                   ErrorManager.GENERIC_FAILURE);
       }
       return null;
   }

}
