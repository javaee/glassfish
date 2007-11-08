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

import java.util.Map;
import java.util.Hashtable;
import java.util.Date;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.ErrorManager;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.security.*;
import java.io.File;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.RuntimeOperationsException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.NotificationListener;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ReflectionException;
import javax.management.ObjectName;
import javax.management.OperationsException;

import com.sun.enterprise.server.logging.stats.ErrorStatistics;
import com.sun.enterprise.server.logging.logviewer.backend.LogFilter;
import com.sun.enterprise.server.logging.diagnostics.MessageIdCatalog;

import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.SystemPropertyConstants;


/**
 *  Log MBean can
 *  1. Fetch the Loggers available in the server
 *  2. Fetch the log level of a given logger
 *  3. Dynamically change the log levels of all the avalaible Loggers
 *  4. Send SEVERE and WARNING Log messages as Alarms to all the interested 
 *     parties.
 *
 * 
 * @author Hemanth Puttaswamy
 */
public class LogMBean extends NotificationBroadcasterSupport 
    implements DynamicMBean
{
    // Conversion factor to convert minutes to milli seconds, and vice versa.
    private static final int MINUTES_TO_MILLISECONDS = 60000;
    
    private static final String LOGS_DIR = "logs";

    private ObjectName objectName; 

    // We can rotate Instance Log and Access Log at the same time using
    // this LogMBean API. PWC will set it's accessLogRotator implementation
    private LogRotatorSPI accessLogRotator = null;

    private static final String LOG_MBEAN_DESCRIPTION =
        " Log MBean can be used for Managinging and Monitoring Logs \n" +
        " The main features  provided by this MBean are .... \n" +
        " 1. Fetch the Loggers available in the server \n" +
        " 2. Fetch the log level of a given logger \n" +
        " 3. Dynamically change the log levels of all the avalaible Loggers \n"+
        " 4. Send SEVERE and WARNING Log messages as Alarms to all the "  + 
        "interested parties ";

    private static final String GET_LOGGER_NAMES = "getLoggerNames";

    private static final String GET_LOGGER_NAMES_DESCRIPTION = 
        " Gets all the Loggers currently running in the System. The returned "+
        " list is not sorted. ";

    private static final String GET_LOGGER_NAMES_UNDER = "getLoggerNamesUnder";

    private static final String GET_LOGGER_NAMES_UNDER_DESCRIPTION = 
        " Gets all the Loggers under a given logger. The returned list is not "+
        " sorted. ";

    private static final String GET_LOG_LEVEL = "getLogLevel";

    private static final String GET_LOG_LEVEL_DESCRIPTION = 
        " Gets the current Log Level of the given Logger. The 8 Possible " + 
        " values are 1.SEVERE 2.WARNING 3.INFO 4.CONFIG 5.FINE 6.FINER " +
        " 7.FINEST 8. Null";

    private static final String SET_LOG_LEVEL = "setLogLevel";

    private static final String SET_LOG_LEVEL_DESCRIPTION = 
         " Sets the log level of the given logger to the request level. An " +
         " IllegalArgument Exception will be raised if the level is not one " +
         " of the following 8 values.  1.SEVERE 2.WARNING 3.INFO 4.CONFIG " +
         " 5.FINE 6.FINER 7.FINEST 8.OFF";

    private static final String SET_LOG_LEVEL_FOR_MODULE = 
        "setLogLevelForModule";

    private static final String SET_LOG_LEVEL_FOR_MODULE_DESCRIPTION = 
         " Sets the log level for a given module .\n"  +
         " The Modules are admin, classloader, cmp-container, configuration "+
         " , connector, corba, deployment, ejb-container, javamail, jaxr, " +
         " jaxrpc, mdb-container, naming, saaj, security, server, " +
         " transaction, verifier, web-container, core "  +  
         " IllegalArgument Exception will be raised if the level is not one " +
         " of the following 8 values.  1.SEVERE 2.WARNING 3.INFO 4.CONFIG " +
         " 5.FINE 6.FINER 7.FINEST 8.OFF";

    private static final String GET_LOG_RECORD_USING_QUERY = 
        "getLogRecordsUsingQuery";

    private static final String GET_LOG_RECORD_USING_QUERY_DESCRIPTION = 
         " This method returns the Log Records from the server.log file \n"+
         " based on the query passed....." ;

    private static final String GET_ARCHIVED_LOG_FILES =
        "getArchivedLogfiles";

    private static final String GET_ARCHIVED_LOG_FILES_DESCRIPTION =
        " This method returns a list of Archived Log files currently " +
        " available in the domain/log directory"; 

    private static final String GET_DIAGNOSTIC_CAUSES_FOR_MESSAGEID =
        "getDiagnosticCausesForMessageId";

    private static final String 
        GET_DIAGNOSTIC_CAUSES_FOR_MESSAGEID_DESCRIPTION =
        " This method returns the Possible Causes for an Error Code specified";

    private static final String GET_DIAGNOSTIC_CHECKS_FOR_MESSAGEID =
        "getDiagnosticChecksForMessageId";

    private static final String 
        GET_DIAGNOSTIC_CHECKS_FOR_MESSAGEID_DESCRIPTION =
        " This method returns the Diagnostic Checks for an Error Code " +
        "specified";

    private static final String GET_DIAGNOSTIC_URI_FOR_MESSAGEID =
        "getDiagnosticURIForMessageId";

    private static final String GET_DIAGNOSTIC_URI_FOR_MESSAGEID_DESCRIPTION =
        " This method returns the URI to locate the latest Diagnostic info " +
        " on SUN's Docs Website";

    private static final String ROTATE_NOW =
        "rotateNow";

    private static final String ROTATE_NOW_DESCRIPTION =
        " Log file will be rotated when this method is called, This will " +
        " override the other two rotation mechanisms of rotation based on " +
        " log file size or timer.\n The timer will be restarted to schedule " +
        " next rotation if it is specified.";

    private static final String GET_LOGFILES_DIRECTORY = "getLogFilesDirectory";
    
    private static final String GET_ERROR_DISTRIBUTION = "getErrorDistribution";
    private static final String GET_ERROR_DISTRIBUTION_DESCRIPTION = "";

    private static final String GETLOGNAMES4LOGMODULE = "getLognames4LogModule";
    private static final String GETLOGNAMES4LOGMODULE_DESCRIPTION = 
         " This method returns the predefined logger names for the module name " +
         " passed in.";

    private static final String GET_LOGFILES_DIRECTORY_DESCRIPTION = 
        " This method returns the LogFiles Directory to help locate " +
        " Logfiles in the FileSystem...";

    private static final String NOTIFICATION_SEVERE_ALARM = 
        "SevereLogMessages";

    private static final String NOTIFICATION_SEVERE_ALARM_DESCRIPTION = 
        " All Logged Messages whose Log Level is SEVERE will be notified " +
        " to all the subscribed listeners..." ;

    private static final String NOTIFICATION_WARNING_ALARM = 
        "WarningLogMessages";

    private static final String NOTIFICATION_WARNING_ALARM_DESCRIPTION = 
        " All Logged Messages whose Log Level is WARNING will be notified " +
        " to all the subscribed listeners..." ;

    private static final String ATTR_LOGGER_NAMES = "LoggerNames";

    private static final String ATTR_LOGGER_NAMES_DESCRIPTION = 
        "All the Loggers currently running in the System. The list " +
        " is not sorted.";
    
    private static final String ATTR_LOGFILES_DIRECTORY = "LogFilesDirectory";

    private static final String ATTR_LOGFILES_DIRECTORY_DESCRIPTION = 
        "The LogFiles Directory to help locate " +
        "Logfiles in the FileSystem...";
        
    private static final String ATTR_ARCHIVED_LOG_FILES = "ArchivedLogfiles";

    private static final String ATTR_ARCHIVED_LOG_FILES_DESCRIPTION =
        "A list of Archived Log files currently " +
        "available in the domain/log directory";
    
    private static final String ATTR_ERROR_INFORMATION = "ErrorInformation";
    private static final String ATTR_ERROR_INFORMATION_DESCRIPTION =
        "ErrorInformation";
        
    private static final String ATTR_KEEP_ERROR_STATISTICS_FOR_HOURS =
        "KeepErrorStatisticsForHours";
    private static final String
            ATTR_KEEP_ERROR_STATISTICS_FOR_HOURS_DESCRIPTION =
        "The number of hours for which error statistics are maintained.";
    
    private static final String ATTR_KEEP_ERROR_STATISTICS_FOR_INTERVALS =
        "KeepErrorStatisticsForIntervals";
    private static final String
            ATTR_KEEP_ERROR_STATISTICS_FOR_INTERVALS_DESCRIPTION =
        "Number of intervals for which error statistics are maintained.";
    
    private static final String ATTR_ERROR_STATISTICS_INTERVAL_DURATION =
        "ErrorStatisticsIntervalDuration";
    private static final String
            ATTR_ERROR_STATISTICS_INTERVAL_DURATION_DESCRIPTION =
        "Duration of each interval for which error statistics are retained.";
    
    private static final MBeanOperationInfo[] operationInfoArray =
        new MBeanOperationInfo[11];
    
    private static final MBeanAttributeInfo[] attributeInfoArray =
        new MBeanAttributeInfo[7];
    
    /**
     * Info on this MBean
     */
    private static final MBeanInfo _mBeanInfo = initializeMBeanInfo();

    private static LogMBean mbeanInstance = null;

    /**
     *  A Singleton factory method.
     */
    public synchronized static LogMBean getInstance( ) { 
        if( mbeanInstance == null ) { 
            mbeanInstance = new LogMBean();
        }
        return mbeanInstance;
    }

    /**
     * A private default constructor to ensure Singleton pattern for LogMBean.
     */
    private LogMBean( ) { }
   

    public static MBeanInfo initializeMBeanInfo( ) {
        MBeanInfo mBeanInfo = null;
        
        try {
            if ( attributeInfoArray.length != 7 ) {
                throw new RuntimeException(
                    "attributeInfoArray array is the wrong length" );
            }

            attributeInfoArray[0] = new MBeanAttributeInfo(
                    ATTR_LOGGER_NAMES, "java.util.List",
                    ATTR_LOGGER_NAMES_DESCRIPTION, true, false, false);
            
            attributeInfoArray[1] = new MBeanAttributeInfo(
                ATTR_LOGFILES_DIRECTORY, "String",
                ATTR_LOGFILES_DIRECTORY_DESCRIPTION, true, false, false);
            
            attributeInfoArray[2] = new MBeanAttributeInfo(
                ATTR_ARCHIVED_LOG_FILES, "[Ljava.lang.String;",
                ATTR_ARCHIVED_LOG_FILES_DESCRIPTION, true, false, false);
            
            attributeInfoArray[3] = new MBeanAttributeInfo(
                ATTR_ERROR_INFORMATION, List.class.getName(),
                ATTR_ERROR_INFORMATION_DESCRIPTION, true, false, false);
            
            attributeInfoArray[4] = new MBeanAttributeInfo(
                ATTR_KEEP_ERROR_STATISTICS_FOR_HOURS, int.class.getName(),
                ATTR_KEEP_ERROR_STATISTICS_FOR_HOURS_DESCRIPTION,
                true, true, false);
            
            attributeInfoArray[5] = new MBeanAttributeInfo(
                ATTR_KEEP_ERROR_STATISTICS_FOR_INTERVALS, int.class.getName(),
                ATTR_KEEP_ERROR_STATISTICS_FOR_INTERVALS_DESCRIPTION,
                true, true, false);
                
            attributeInfoArray[6] = new MBeanAttributeInfo(
                ATTR_ERROR_STATISTICS_INTERVAL_DURATION, long.class.getName(),
                ATTR_ERROR_STATISTICS_INTERVAL_DURATION_DESCRIPTION,
                true, true, false);
                
            MBeanParameterInfo[] loggerNameParam = 
                {new MBeanParameterInfo("LoggerName", "String", "Logger Name")};
            operationInfoArray[0] = new MBeanOperationInfo(
                GET_LOGGER_NAMES_UNDER, GET_LOGGER_NAMES_UNDER_DESCRIPTION,
                loggerNameParam, "java.util.List", MBeanOperationInfo.ACTION);

            operationInfoArray[1] = new MBeanOperationInfo(GET_LOG_LEVEL,
                GET_LOG_LEVEL_DESCRIPTION, loggerNameParam, "String",
                MBeanOperationInfo.ACTION);

            MBeanParameterInfo[] loggerNameAndLevelParam = 
                {new MBeanParameterInfo("LoggerName", "String", "Logger Name"),
                 new MBeanParameterInfo("LogLevel", "String", "Logger Level") };
            operationInfoArray[2] = new MBeanOperationInfo(SET_LOG_LEVEL,
                SET_LOG_LEVEL_DESCRIPTION, loggerNameAndLevelParam, "void", 
                MBeanOperationInfo.ACTION);

            operationInfoArray[3] = new MBeanOperationInfo(
                SET_LOG_LEVEL_FOR_MODULE, SET_LOG_LEVEL_FOR_MODULE_DESCRIPTION,
                loggerNameAndLevelParam, "void", MBeanOperationInfo.ACTION);

            MBeanParameterInfo[] logviewerQueryParams = 
                {new MBeanParameterInfo("FileName", "String",
                     " If Filename is specified then it will be used to " +
                     " run the query. If the user specifies null for this " +
                     " argument then the current server.log file will be  " +
                     " used to run the query. This argument is specifed to " +
                     " typically run query on Archived Log File "),
                 new MBeanParameterInfo("FromRecord", "Long", 
                     "The location within the LogFile"),
                 new MBeanParameterInfo("next", "Boolean", 
                     "True to get the next set of results and False to get " +
                     " the previous results "),
                 new MBeanParameterInfo("forward", "Boolean", 
                     "True to search forward through the log file " ),
                 new MBeanParameterInfo("requestedCount", "Integer", 
                     "Number of desired records" ),
                 new MBeanParameterInfo("fromDate", "java.util.Date", 
                     " The lower bound date " ),
                 new MBeanParameterInfo("toDate", "java.util.Date", 
                     " The upper bound date " ),
                 new MBeanParameterInfo("logLevel", "String", 
                     " The minimum log level to display " ),
                 new MBeanParameterInfo("onlyLevel", "Boolean", 
                     " True to only display messsage for \"logLevel\""),
                 new MBeanParameterInfo("listOfModules", "java.util.List", 
                     " List Of Modules and Logger Names To Match" ),
                 new MBeanParameterInfo("nameValueMap", "java.util.Properties", 
                     " List Of Name Value Pairs to match " )
                 };
            operationInfoArray[4] = new MBeanOperationInfo(
                GET_LOG_RECORD_USING_QUERY, 
                GET_LOG_RECORD_USING_QUERY_DESCRIPTION,
                logviewerQueryParams, "javax.management.AttributeList", 
                MBeanOperationInfo.ACTION);

            MBeanParameterInfo[] diagnosticsParameter = 
                {new MBeanParameterInfo("messageId", "String", "Error Code") };
            operationInfoArray[5] = new MBeanOperationInfo(
                GET_DIAGNOSTIC_CAUSES_FOR_MESSAGEID, 
                GET_DIAGNOSTIC_CAUSES_FOR_MESSAGEID_DESCRIPTION,
                diagnosticsParameter, "java.util.ArrayList", 
                MBeanOperationInfo.ACTION);

            operationInfoArray[6] = new MBeanOperationInfo(
                GET_DIAGNOSTIC_CHECKS_FOR_MESSAGEID, 
                GET_DIAGNOSTIC_CHECKS_FOR_MESSAGEID_DESCRIPTION,
                diagnosticsParameter, "java.util.ArrayList", 
                MBeanOperationInfo.ACTION);

            operationInfoArray[7] = new MBeanOperationInfo(
                GET_DIAGNOSTIC_URI_FOR_MESSAGEID, 
                GET_DIAGNOSTIC_URI_FOR_MESSAGEID_DESCRIPTION,
                diagnosticsParameter, "String", MBeanOperationInfo.ACTION);

            operationInfoArray[8] = new MBeanOperationInfo(
                ROTATE_NOW, ROTATE_NOW_DESCRIPTION,
                null, "void", MBeanOperationInfo.ACTION);


            final MBeanParameterInfo[] getErrorDistributionParams = 
            {
                new MBeanParameterInfo("name", "long","time stamp"),
                new MBeanParameterInfo("name", Level.class.getName(), "log level")
            };
                     
            operationInfoArray[9] = new MBeanOperationInfo(
                GET_ERROR_DISTRIBUTION, GET_ERROR_DISTRIBUTION_DESCRIPTION,
                getErrorDistributionParams, List.class.getName(), MBeanOperationInfo.INFO);

            MBeanParameterInfo[] moduleNameParam = 
                {new MBeanParameterInfo("ModuleName", "String", "Module Name")};
            operationInfoArray[10] = new MBeanOperationInfo(
                GETLOGNAMES4LOGMODULE, 
                GETLOGNAMES4LOGMODULE_DESCRIPTION,
                moduleNameParam, "java.util.List", MBeanOperationInfo.ACTION);

            String[] alarmTypes = {NOTIFICATION_SEVERE_ALARM,
                 NOTIFICATION_WARNING_ALARM};
            MBeanNotificationInfo[] notificationInfo =
                {new MBeanNotificationInfo( alarmTypes, "Alarms", 
                    "Severe and Warning Log Messages will be notified ") };
                    
            mBeanInfo = new MBeanInfo("LogMBean", LOG_MBEAN_DESCRIPTION,
                attributeInfoArray, null, operationInfoArray,
                notificationInfo);

            // Now Try to register the LogMBean
            //new RegisterLogMBean( this ).start( );

        } catch( Exception e ) {
            new ErrorManager().error(
                "Error in LogMBean Initialization", e,
                ErrorManager.GENERIC_FAILURE );
        }
            
       return mBeanInfo;
    }



    public Object invoke(String operationName, Object[] params, 
        String[] signature) throws MBeanException, ReflectionException 
    {
        if( operationName == null || operationName.equals("")) {
            throw new RuntimeOperationsException(
                new IllegalArgumentException( "MBean.invoke operation name is "+
                    " null " ) );
        }
        AttributeList resultList = new AttributeList();
        if( operationName.equals( GET_LOGGER_NAMES ) ) {
            return getLoggerNames( );
        } else if( operationName.equals( GET_LOGGER_NAMES_UNDER ) ) {
            String loggerName = (String) params[0];
            return getLoggerNamesUnder( loggerName );
        } else if( operationName.equals( GETLOGNAMES4LOGMODULE ) ) {
            String module = (String) params[0];
            return getLognames4LogModule(module); 
        } else if( operationName.equals( GET_LOG_LEVEL ) ) {
            String loggerName = (String) params[0];
            return getLogLevel( loggerName );
        } else if( operationName.equals( SET_LOG_LEVEL ) ) {
            String loggerName = (String) params[0];
            String loglevel = (String) params[1];
            setLogLevel( loggerName, loglevel );
        } else if( operationName.equals( SET_LOG_LEVEL_FOR_MODULE ) ) {
            String module = (String) params[0];
            String loglevel = (String) params[1];
            setLogLevel( module, loglevel );
        } else if( operationName.equals( GET_LOG_RECORD_USING_QUERY) ) {
            String fileName = (String) params[0];
            Long fromRecord = (Long) params[1];
            Boolean next = (Boolean) params[2];
            Boolean forward = (Boolean) params[3];
            Integer requestedCount = (Integer) params[4];
            Date fromDate = (Date) params[5];
            Date toDate = (Date) params[6];
            String logLevel = (String) params[7];
            Boolean onlyLevel = (Boolean) params[8];
            List listOfModules = (java.util.List) params[9];
            java.util.Properties nvMap = (java.util.Properties) params[10];
            return getLogRecordsUsingQuery( fileName, fromRecord, next, forward,
                requestedCount, fromDate, toDate, logLevel, onlyLevel, 
                listOfModules, nvMap );
        } else if( operationName.equals( GET_ARCHIVED_LOG_FILES ) ) {
            return getArchivedLogFiles( );
        } else if( operationName.equals( GET_DIAGNOSTIC_CAUSES_FOR_MESSAGEID )){
            String messageId = (String) params[0];
            return 
                MessageIdCatalog.getInstance().getDiagnosticCausesForMessageId(
                    messageId );
        } else if( operationName.equals( GET_DIAGNOSTIC_CHECKS_FOR_MESSAGEID )){
            String messageId = (String) params[0];
            return 
                MessageIdCatalog.getInstance().getDiagnosticChecksForMessageId(
                    messageId );
        } else if( operationName.equals( GET_DIAGNOSTIC_URI_FOR_MESSAGEID )){
            String messageId = (String) params[0];
            return 
                MessageIdCatalog.getInstance().getDiagnosticURIForMessageId(
                    messageId );
        } else if( operationName.equals( ROTATE_NOW ) ) {
            this.rotateNow( );
        } else if( operationName.equals( GET_LOGFILES_DIRECTORY ) ) {
            return this.getLogFilesDirectory( ); 
        } else if( operationName.equals( GET_ERROR_DISTRIBUTION ) ) {
            return this.getErrorDistribution( (Long)params[0], (Level)params[1] ); 
        } else {
            throw new IllegalArgumentException( 
                "Requested operation " + operationName + " does not exist..." );
        }
        return resultList;
    }
     

   /**
     *  check if logger already existes
     */
    public boolean findLogger(String loggerName ) {
        Enumeration allLoggers = LogManager.getLogManager().getLoggerNames( );
        while( allLoggers.hasMoreElements( ) ) {
            String name = (String)allLoggers.nextElement();
            if (loggerName.equals(name))
                return true;
        }
        return false;
    }

    
    /**
     *  Gets all the Loggers currently running in the System. The returned
     *  list is not sorted.
     */
    public List getLoggerNames( ) {
        LinkedList loggerNamesList = new LinkedList( );
        Enumeration allLoggers = LogManager.getLogManager().getLoggerNames( );
        while( allLoggers.hasMoreElements( ) ) {
            loggerNamesList.add(allLoggers.nextElement( ));
        }
        return loggerNamesList;
    }

    /**
     *  Gets all the Loggers under a given logger. The returned list is not 
     *  sorted.
     */
    public List getLoggerNamesUnder( String loggerName ) {
        LinkedList filteredList = new LinkedList( );
        Iterator allLoggersIterator = getLoggerNames( ).iterator( );
        while( allLoggersIterator.hasNext( ) ) {
            String t = (String) allLoggersIterator.next( );
            if( t.startsWith( loggerName ) ) {
                filteredList.add( t );
            }
        }
        return filteredList;
    }


    /**
     *  Gets the current Log Level of the given Logger. The 8 Possible values 
     *  are
     *  1.SEVERE 2.WARNING 3.INFO 4.CONFIG 5.FINE 6.FINER 7.FINEST 8. Null
     */
    public String getLogLevel( String loggerName ) {
        String logLevelInString = null;
        Logger logger = Logger.getLogger( loggerName );
        if( logger == null ) {
             throw new RuntimeException( " Logger " + loggerName +
                 " does not exist" );
        }
        Level level = logger.getLevel( );
        boolean foundParentLevel = false;
        if( level == null ) {
            while( !foundParentLevel ) {
                logger = logger.getParent();
                if ( logger == null ){
                    break;
                }
                level = logger.getLevel( );
                if( level != null ) {
                    foundParentLevel = true;
                }
            }
        }
        if( level != null )  {
            logLevelInString = level.toString();
        }
        return logLevelInString;
    }

    /**
     *  Sets the log level of the given logger to the request level. An
     *  Exception will be raised if the level is not one of the following 8
     *  values.
     *  1.SEVERE 2.WARNING 3.INFO 4.CONFIG 5.FINE 6.FINER 7.FINEST 8.OFF
     *  
     *  JDK's java.util.logging.Level.parse will throw IllegalArgument exception
     *  if a wrong value is used for the Level. 
     *
     *  Apparently there seems to be a problem in propogating LogLevel from
     *  parent to child loggers in JDK. So, using this internal method to
     *  set the level for all the individual loggers...
     */
    public void setLogLevel( String loggerName, String level ) {
        internalSetLogLevel(loggerName,Level.parse(level) );
     /*   Level logLevel = Level.parse( level ); 
        // NOTE: Don't have to check for listIterator being null, because
        // even if a wrong logger name is used, a null list will be returned.
        Iterator listIterator = getLoggerNamesUnder( loggerName ).iterator();
        while ( listIterator.hasNext( ) ) {
            internalSetLogLevel( (String) listIterator.next(), logLevel );
        }
      */
    }

    /**
     *  Sets the log level for the given module to the request level. An
     *  Exception will be raised if the level is not one of the following 8
     *  values.
     *  1.SEVERE 2.WARNING 3.INFO 4.CONFIG 5.FINE 6.FINER 7.FINEST 8.OFF
     */
    public void setLogLevelForModule( String module, String level ) {
        String[] loggerNames = ModuleToLoggerNameMapper.getLoggerNames( module );
        for (int i=0;loggerNames!=null&&i<loggerNames.length;i++) { 
            setLogLevel(loggerNames[i], level );
        }
    }

    /**
     */
    private void internalSetLogLevel( final String loggerName, 
        final Level level )
    {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                Logger.getLogger( loggerName ).setLevel(level);
                return null;
            }});
    }

    /**
     *  The public method that Log Viewer Front End will be calling on.
     *  The query will be run on records starting from the fromRecord.
     *  If any of the elements for the query is null, then that element will
     *  not be used for the query.  If the user is interested in viewing only
     *  records whose Log Level is SEVERE and WARNING, then the query would
     *  look like:
     *
     *  fromDate = null, toDate = null, logLevel = WARNING, onlyLevel = false,
     *  listOfModules = null, nameValueMap = null.
     *
     *  @param  fromRecord      The location within the LogFile
     *  @param  next            True to get the next set of results, false to
     *                          get the previous set
     *  @param  forward         True to search forward through the log file
     *  @param  requestedCount  The # of desired return values
     *  @param  fromDate        The lower bound date
     *  @param  toDate          The upper bound date
     *  @param  logLevel        The minimum log level to display
     *  @param  onlyLevel       True to only display messsage for "logLevel"
     *  @param  listOfModules   List of modules to match
     *  @param  nameValueMap    NVP's to match
     *
     *  @return
     */
    public AttributeList getLogRecordsUsingQuery( 
        String logFilename, Long fromRecord, Boolean next, Boolean forward,
        Integer requestedCount, Date fromDate, Date toDate,
        String logLevel, Boolean onlyLevel, List listOfModules,
        Properties nameValueMap) 
    {
        return LogFilter.getLogRecordsUsingQuery(logFilename, fromRecord, next,
            forward, requestedCount, fromDate, toDate, logLevel, onlyLevel, 
            listOfModules, nameValueMap);
    } 

    /**
     * Gets all Archived log Files in the current Server instance.
     * 
     * @return An Array of all Archived Log filenames. 
     */
    public String[] getArchivedLogFiles( ) {
        String[] archivedLogFiles = new String[0];
        try {
            String instDir = System.getProperty( 
                SystemPropertyConstants.INSTANCE_ROOT_PROPERTY );
            String[] names = { instDir, LOGS_DIR };
            String logDir = StringUtils.makeFilePath( names, false );
            File file = new File( logDir );
            archivedLogFiles = file.list( new FilenameFilterImpl( ) );
            // Get the current log file name and see if we have that on the list already.
            // This is a corner case where user would not have given ".log" extension to
            // log file and the FileNameFilter has filtered it.
            // Also note that we are interested in getting just the filename, not the
            // completepath
            String currentFileName = new File(
                ServerLogManager.getLogService( ).getFile( )).getName( );
            boolean foundCurrentFile = false;
            int i = 0;
            for( i = 0; i < archivedLogFiles.length; i++ ) {
                if( currentFileName.equals( archivedLogFiles[i] ) ) {
                    foundCurrentFile = true;
                    break;
                }   
            }
            if( !foundCurrentFile ) {
                // If we don't have the currentLogFile in the list, then it must be
                // because it doesn't use .log extension. Now, build a new list with
                // the current log file name and the list returned from file.list( ) 
                String[] newArchivedLogFiles = new String[ archivedLogFiles.length + 1 ];
                newArchivedLogFiles[0] = currentFileName;
                for( i = 1; i < (archivedLogFiles.length + 1); i++ ) { 
                    newArchivedLogFiles[i] = archivedLogFiles[i - 1];
                }
                return newArchivedLogFiles;
            }
        } catch( Exception e ) {
            // This will go into System.err Logger. As this failure is in
            // the log module, we will stick with System.err here.
            System.err.println( 
                "Exception in LogMBean.getArchivedLogFiles.." + e );
            throw new RuntimeException( 
                "Exception in LogMBean.getArchivedLogFiles.." + e );
        }
        return archivedLogFiles;
    }

    /**
     *  This method rotates the server.log and access log file at this instant.
     */
    public void rotateNow( ) {
        FileandSyslogHandler.getInstance( ).requestRotation( );
        // Rotate Access Log also
        this.rotateAccessLog( );
    }

    /**
     * This method rotates the accessLog
     */
    public void rotateAccessLog( ) {
        if( accessLogRotator != null ) {
            accessLogRotator.rotate( );
       }
    }

    /**
     * Setter provided to set the AccessLog Rotator.
     */
    public void setAccessLogRotator( LogRotatorSPI logRotator ) {
        accessLogRotator = logRotator;
    }

    /**
     * This method returns the directory where log files are located.
     */
    public String getLogFilesDirectory( ) {
        try {
            String fileName = 
                FileandSyslogHandler.getInstance( ).getAbsoluteLogFileName( );
            return new File( fileName ).getParent( );
        } catch( Exception e ) {
            System.err.println( "Exception in LogMBean.getLogFilesDirectory()"
                + e );
        }
        return null;
    }


    /**
     *  This is an internal SPI to change the file name for FileandSyslogHandler
     */
    public void changeLogFileName( String fileName ) {
        FileandSyslogHandler.getInstance().changeFileName( fileName );
    }


    /**
     * Provides the exposed attributes and actions of the monitoring MBean using
     * an MBeanInfo object.
     * @return An instance of MBeanInfo with all attributes and actions exposed
     *         by this monitoring MBean.
     */
    public MBeanInfo getMBeanInfo() {
        return _mBeanInfo;
    }


    /**
     * Obtains the value of a specific monitored attribute. The LogMBean does
     * not have any attribute, this method always throws
     * AttributeNotFoundException when specified attribute is not null.
     *
     * @param attribute The name of the attribute to be retrieved
     * @return The value of the attribute retrieved.
     * @throws AttributeNotFoundException if attribute name is not valid
     */
    public Object getAttribute(String attribute) 
        throws AttributeNotFoundException
    {
        Object  result  = null;
        
        if ( ATTR_LOGGER_NAMES.equals( attribute ) ) {
            result  = getLoggerNames();
        }
        else if ( ATTR_LOGFILES_DIRECTORY.equals( attribute ) ) {
            result  = getLogFilesDirectory();
        }
        else if ( ATTR_ARCHIVED_LOG_FILES.equals( attribute ) ) {
            result  = getArchivedLogFiles();
        }
        else if ( ATTR_ERROR_INFORMATION.equals( attribute ) ) {
            result  = getErrorInformation();
        }
        else if ( ATTR_KEEP_ERROR_STATISTICS_FOR_INTERVALS.equals( attribute ) ) {
            result  = getKeepErrorStatisticsForIntervals();
        }
        else if ( ATTR_ERROR_STATISTICS_INTERVAL_DURATION.equals( attribute ) ) {
            result  = getErrorStatisticsIntervalMinutes();
        }
        else if ( ATTR_KEEP_ERROR_STATISTICS_FOR_HOURS.equals( attribute ) ) {
            result  = getKeepErrorStatisticsForHours();
        }
        else {
            if (attribute != null) {
                throw new AttributeNotFoundException("Attribute " + attribute
                        + " not found in LogMBean. There are no attributes.");
            } else {
                throw new IllegalArgumentException("Attribute Name is null");
            }
        }
        
        return result;
    }


    /**
     * Get the values of several attributes of the monitoring MBean. LogMBean
     * does not have any attributes, so this method will always return an
     * empty attribute list.
     *
     * @param attributes A list of the attributes to be retrieved.
     * @return The list of attributes retrieved.
     */
    public AttributeList getAttributes( String[] attributeNames ) 
    {
        final AttributeList attrs  = new AttributeList();
        
        for( final String name : attributeNames )
        {
            try
            {
                final Object value  = getAttribute( name );
                attrs.add( new Attribute( name, value ) );
            }
            catch( Exception e )
            {
                // ignore, per JMX spec
            }
        }
        
        return attrs;
    }

    /**
     * Set the values of several attributes. As LogMBean does not have any
     * attributes, the method will always return empty attribute list.
     * @param list A list of attributes. The identification of the attributes
     * to be set and the values that they are to be set to.
     * @return the list of attributes that were set, with their new values.
     */
    public AttributeList setAttributes( final AttributeList list ) {
        final AttributeList result = new AttributeList();
        
        for( final Object o : list ) {
            final Attribute attr    = (Attribute)o;
            
            try {
                setAttribute( attr );
                result.add( attr );
            }
            catch( Throwable t ) {
                // OK, per semantics of setAttributes();
            }
        }
        
        return result;
    }

    /**
     * Set the value of specified attribute. This method throws exception as
     * LogMBean does not have any attributes.
     */
    public void setAttribute( final Attribute attribute )
            throws AttributeNotFoundException {
        final String name   = attribute.getName();
        final Object value  = attribute.getValue();
        
        if ( ATTR_KEEP_ERROR_STATISTICS_FOR_INTERVALS.equals( name ) ) {
            setKeepErrorStatisticsForIntervals( (Integer)value );
        }
        else if ( ATTR_ERROR_STATISTICS_INTERVAL_DURATION.equals( name ) ) {
            setErrorStatisticsIntervalMinutes( (Long)value );
        }
        else if ( ATTR_KEEP_ERROR_STATISTICS_FOR_HOURS.equals( name ) ) {
            setKeepErrorStatisticsForHours( (Integer)value );
        }
        else {
            throw new AttributeNotFoundException( name );
        }
    }

    private void raiseAlarm(String type, LogRecord record) {
        LogAlarm logAlarm = new LogAlarm( type, this, record );
        sendNotification( logAlarm ); 
    }

    /**
     *  This method raises a WARNING type Alarm.
     */
    void raiseWarningAlarm(LogRecord record) {
        raiseAlarm( NOTIFICATION_WARNING_ALARM, record );
    }

    /**
     *  This method raises a SEVERE type Alarm.
     */
    void raiseSevereAlarm(LogRecord record) {
        raiseAlarm( NOTIFICATION_SEVERE_ALARM, record );
    }

    // Log error statistics APIs.
   
    /**
     * Set the number of intervals error statistics should be maintained.
     *
     * @param hours Number of intervals error statistics should be maintained.
     */
    public void setKeepErrorStatisticsForIntervals(
            final int numberOfIntervals) {
        ErrorStatistics.singleton().setNumOfIntervals(numberOfIntervals);
    }
    
    /**
     * @return Number of intervals for which error statistics are maintained.
     */
    public int getKeepErrorStatisticsForIntervals() {
        return ErrorStatistics.singleton().getNumOfIntervals();
    }
    
    /**
     * Set the duration of an interval, in minutes.
     *
     * @param minutes The duration of an interval.
     */
    public void setErrorStatisticsIntervalMinutes(final long minutes) {
        ErrorStatistics.singleton().setIntervalDuration(
                    minutes*MINUTES_TO_MILLISECONDS);
    }
    
    /**
     * @return The duration of an interval, in minutes.
     */
    public long getErrorStatisticsIntervalMinutes() {
        return ErrorStatistics.singleton().getIntervalDuration() /
                MINUTES_TO_MILLISECONDS;
    }

    /**
     * Set the number of hours error statistics should be maintained.
     *
     * @param hours Number of hours error statistics should be maintained.
     */
    public void setKeepErrorStatisticsForHours(final int hours) {
        ErrorStatistics.singleton().setNumOfIntervals(hours);
    }
        
    /**
     * @return The number of hours for which error statistics are maintained.
     */
    public int getKeepErrorStatisticsForHours() {
        return ErrorStatistics.singleton().getNumOfIntervals();
    }

    /**
     * @return a list of Map objects. Each map object contains
     * the tuple [TimeStamp, SevereCount, WarningCount].
     */    
    public List<Map<String,Object>> getErrorInformation() {
        return ErrorStatistics.singleton().getErrorInformation();
    }
    
    /**
     * @return a list of Map objects. Each map object contains
     * the tuple [ModuleId, SevereCount|WarningCount].
     */    
    public Map<String,Integer> getErrorDistribution(
            long timeStamp, Level level) {
        return ErrorStatistics.singleton().
                getErrorDistribution(timeStamp, level);
    }

    /**
     * @param  moduleName a predefined log module name. This name is same as the attribute
     * name of <module-log-levels> in <log-service> of domain.xml; 
     * When null is passed in, it means all log modules.
     * @return a ArrayList(!=null) of predefined logger names for this log module.
     */    
    public List getLognames4LogModule(String logModuleName) {
        String[] names = ModuleToLoggerNameMapper.getLoggerNames(logModuleName);
        return Arrays.asList(names);
    }
}     
