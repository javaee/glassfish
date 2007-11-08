/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.logging;

import java.util.*;
import java.text.*;
import java.io.*;
import oracle.toplink.essentials.sessions.Session;
import oracle.toplink.essentials.internal.databaseaccess.Accessor;
import oracle.toplink.essentials.internal.localization.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * Represents the abstract log that implements all the generic logging functions.
 * It contains a singleton SessionLog that logs messages from outside any TopLink session.
 * The singleton SessionLog can also be passed to a TopLink session when messages
 * are logged through that session.  When JDK1.4 is used, a singleton JavaLog is created.
 * Otherwise a singleton DefaultSessionLog is created.
 *
 * @see SessionLog
 * @see SessionLogEntry
 * @see DefaultSessionLog
 * @see JavaLog
 */
public abstract class AbstractSessionLog implements SessionLog, java.lang.Cloneable {

    /**
     * Represents the log level
     */
    protected int level;

    /**
     * Represents the singleton SessionLog
     */
    protected static SessionLog defaultLog;

    /**
     * Represents the session that owns this SessionLog
     */
    protected Session session;

    /**
     * Represents the session type of the session that owns this SessionLog
     */
    protected String sessionType;

    /**
     * Caches the session hash code string of the session that owns this SessionLog
     */
    protected String sessionHashCode;

    /**
     * Represents prefix to logged severe
     */
    protected static String SEVERE_PREFIX = null;

    /**
     * Represents prefix to logged warning
     */
    protected static String WARNING_PREFIX = null;

    /**
     * Represents prefix to logged info
     */
    protected static String INFO_PREFIX = null;

    /**
     * Represents prefix to logged config
     */
    protected static String CONFIG_PREFIX = null;

    /**
     * Represents prefix to logged fine
     */
    protected static String FINE_PREFIX = null;

    /**
     * Represents prefix to logged finer
     */
    protected static String FINER_PREFIX = null;

    /**
     * Represents prefix to logged finest
     */
    protected static String FINEST_PREFIX = null;

    /**
     * Cached TopLink prefix string.
     */
    protected static String TOPLINK_PREFIX = null;

    /**
     * Connection string
     */
    protected static final String CONNECTION_STRING = "Connection";

    /**
     * Thread string
     */
    protected static final String THREAD_STRING = "Thread";

    /**
     * Represents the writer that will receive the formatted log entries
     */
    protected Writer writer;
    
    /**
     * Format use to print the current date/time.
     */
    protected DateFormat dateFormat;
    
    /**
     * Allows the printing of the stack to be explictitly disabled/enabled.
     * CR #3870467.
     * null value is default behavoir of determining from log level.
     */
    protected Boolean shouldLogExceptionStackTrace;
    
    /**
     * Allows the printing of the date to be explictitly disabled/enabled.
     * CR #3870467.
     * null value is default behavoir of determining from log level.
     */
    protected Boolean shouldPrintDate;
    
    /**
     * Allows the printing of the thread to be explictitly disabled/enabled.
     * CR #3870467.
     * null value is default behavoir of determining from log level.
     */
    protected Boolean shouldPrintThread;
        
    /**
     * Allows the printing of the session to be explictitly disabled/enabled.
     * CR #3870467.
     * null value is default behavoir of determining from log level.
     */
    protected Boolean shouldPrintSession;

    /**
     * Allows the printing of the connection to be explictitly disabled/enabled.
     * CR #4157545.
     * null value is default behavoir of determining from log level.
     */
    protected Boolean shouldPrintConnection;

    /**
     * PUBLIC:
     * Create a new AbstractSessionLog
     */
    public AbstractSessionLog() {
        this.writer = new PrintWriter(System.out);
    }

    /**
     * PUBLIC:
     * <p>
     * Return the log level.  It is used when session is not available.
     * </p><p>
     *
     * @return the log level
     * </p>
     */
    public int getLevel() {
        return getLevel(null);
    }

    /**
     * PUBLIC:
     * <p>
     * Return the log level for the category name space.
     * </p><p>
     *
     * @return the log level
     * </p><p>
     * @param category  the string representation of a TopLink category, e.g. "sql", "transaction" ...
     * </p>
     */
    public int getLevel(String category) {
        return level;
    }

    /**
     * PUBLIC:
     * <p>
     * Set the log level.  It is used when session is not available.
     * </p><p>
     *
     * @param level     the new log level
     * </p>
     */
    public void setLevel(int level) {
        setLevel(level, null);
    }

    /**
     * PUBLIC:
     * <p>
     * Set the log level for the category name space.
     * </p><p>
     *
     * @param level     the new log level
     * @param category  the string representation of a TopLink category, e.g. "sql", "transaction" ...
     * </p>
     */
    public void setLevel(int level, String category) {
        this.level = level;
    }

    /**
     * PUBLIC:
     * <p>
     * Check if a message of the given level would actually be logged.
     * It is used when session is not available.
     * </p><p>
     *
     * @return true if the given message level will be logged
     * </p><p>
     * @param level  the log request level
     * </p>
     */
    public boolean shouldLog(int level) {
        return shouldLog(level, null);
    }

    /**
     * PUBLIC:
     * <p>
     * Check if a message of the given level would actually be logged for the category name space.
     * !isOff() is checked to screen out the possibility when both
     * log level and log request level are set to OFF.
     * </p><p>
     *
     * @return true if the given message level will be logged
     * </p><p>
     * @param level  the log request level
     * @param category  the string representation of a TopLink category, e.g. "sql", "transaction" ...* </p>
     * </p>
     */
    public boolean shouldLog(int level, String category) {
        return (this.level <= level) && !isOff();
    }

    /**
     * PUBLIC:
     * <p>
     * Return the singleton SessionLog.  If the singleton SessionLog does not exist,
     * a new one is created based on the version of JDK being used from the Version class.
     * </p><p>
     *
     * @return the singleton SessionLog
     * </p>
     */
    public static SessionLog getLog() {
        if (defaultLog == null) {
            defaultLog = new DefaultSessionLog();
        }
        return defaultLog;
    }

    /**
     * PUBLIC:
     * <p>
     * Set the singleton SessionLog.
     * </p>
     *
     * @param sessionLog  a SessionLog
     * </p>
     */
    public static void setLog(SessionLog sessionLog) {
        defaultLog = sessionLog;
        defaultLog.setSession(null);
    }

    /**
     * PUBLIC:
     * <p>
     * Get the session.
     * </p>
     *
     * @return  session
     * </p>
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * PUBLIC:
     * <p>
     * Set the session.
     * </p>
     *
     * @param session  a Session
     * </p>
     */
    public void setSession(Session session) {
        if (this.session == null) {
            this.session = session;
            buildSessionType();
            buildSessionHashCode();
        }
    }

    /**
     * PUBLIC:
     * <p>
     * Log a message that does not need to be translated.  This method is intended for 
	 * external use when logging messages are wanted within the TopLink output.
     * </p><p>
     *
     * @param level the log request level value
     * </p><p>
     * @param message the string message
     * </p>
     */
    public void log(int level, String message) {
        if (!shouldLog(level)) {
            return;
        }
		//Bug#4566524  Pass in false for external use
        log(level, message, (Object[])null, false);
    }

    /**
     * INTERNAL:
     * <p>
     * Log a message with one parameter that needs to be translated.
     * </p><p>
     *
     * @param level  the log request level value
     * </p><p>
     * @param message  the string message
     * </p><p>
     * @param param  a parameter of the message
     * </p>
     */
    public void log(int level, String message, Object param) {
        if (!shouldLog(level)) {
            return;
        }
        log(level, message, new Object[] { param });
    }

    /**
     * INTERNAL:
     * <p>
     * Log a message with two parameters that needs to be translated.
     * </p><p>
     *
     * @param level the log request level value
     * </p><p>
     * @param message the string message
     * </p><p>
     * @param param1  a parameter of the message
     * </p><p>
     * @param param2  second parameter of the message
     * </p>
     */
    public void log(int level, String message, Object param1, Object param2) {
        if (!shouldLog(level)) {
            return;
        }
        log(level, message, new Object[] { param1, param2 });
    }

    /**
     * INTERNAL:
     * <p>
     * Log a message with three parameters that needs to be translated.
     * </p><p>
     *
     * @param level the log request level value
     * </p><p>
     * @param message the string message
     * </p><p>
     * @param param1  a parameter of the message
     * </p><p>
     * @param param2  second parameter of the message
     * </p><p>
     * @param param3  third parameter of the message
     * </p>
     */
    public void log(int level, String message, Object param1, Object param2, Object param3) {
        if (!shouldLog(level)) {
            return;
        }
        log(level, message, new Object[] { param1, param2, param3 });
    }

    /**
     * INTERNAL:
     * <p>
     * Log a message with an array of parameters that needs to be translated.
     * </p><p>
     *
     * @param level the log request level value
     * </p><p>
     * @param message the string message
     * </p><p>
     * @param params array of parameters to the message
     * </p>
     */
    public void log(int level, String message, Object[] params) {
        log(level, message, params, true);
    }

    /**
     * INTERNAL:
     * <p>
     * Log a message.  shouldTranslate determines if the message needs to be translated.
     * </p><p>
     *
     * @param level the log request level
     * </p><p>
     * @param message the string message
     * </p><p>
     * @param params array of parameters to the message
     * </p><p>
     * @param shouldTranslate true if the message needs to be translated
     * </p>
     */
    public void log(int level, String message, Object[] params, boolean shouldTranslate) {
        if (!shouldLog(level)) {
            return;
        }
        log(new SessionLogEntry(level, null, message, params, null, shouldTranslate));
    }

    /**
     * PUBLIC:
     * <p>
     * Log a SessionLogEntry
     * </p><p>
     *
     * @param entry SessionLogEntry that holds all the information for a TopLink logging event
     * </p>
     */
    public abstract void log(SessionLogEntry sessionLogEntry);
    
    /**
     * By default the session (and its connection is available) are printed,
     * this can be turned off.
     */
    public boolean shouldPrintSession() {
        return (shouldPrintSession == null) || shouldPrintSession.booleanValue();
    }

    /**
     * By default the session (and its connection is available) are printed,
     * this can be turned off.
     */
    public void setShouldPrintSession(boolean shouldPrintSession) {
        if (shouldPrintSession) {
            this.shouldPrintSession = Boolean.TRUE;
        } else {
            this.shouldPrintSession = Boolean.FALSE;            
        }
    }

    /**
     * By default the connection is printed, this can be turned off.
     */
    public boolean shouldPrintConnection() {
        return (shouldPrintConnection == null) || shouldPrintConnection.booleanValue();
    }

    /**
     * By default the connection is printed, this can be turned off.
     */
    public void setShouldPrintConnection(boolean shouldPrintConnection) {
        if (shouldPrintConnection) {
            this.shouldPrintConnection = Boolean.TRUE;
        } else {
            this.shouldPrintConnection = Boolean.FALSE;            
        }
    }

    /**
     * By default the stack is logged for FINER or less (finest).
     * The logging of the stack can also be explicitly turned on or off.
     */
    public boolean shouldLogExceptionStackTrace() {
        if (shouldLogExceptionStackTrace == null) {
            return getLevel() <= FINER;
        } else {
            return shouldLogExceptionStackTrace.booleanValue();
        }
    }

    /**
     * By default the stack is logged for FINER or less (finest).
     * The logging of the stack can also be explicitly turned on or off.
     */
    public void setShouldLogExceptionStackTrace(boolean shouldLogExceptionStackTrace) {
        if (shouldLogExceptionStackTrace) {
            this.shouldLogExceptionStackTrace = Boolean.TRUE;
        } else {
            this.shouldLogExceptionStackTrace = Boolean.FALSE;            
        }
    }

    /**
     * By default the date is always printed, but can be turned off.
     */
    public boolean shouldPrintDate() {
        return (shouldPrintDate == null) || (shouldPrintDate.booleanValue());
    }

    /**
     * By default the date is always printed, but can be turned off.
     */
    public void setShouldPrintDate(boolean shouldPrintDate) {
        if (shouldPrintDate) {
            this.shouldPrintDate = Boolean.TRUE;
        } else {
            this.shouldPrintDate = Boolean.FALSE;            
        }
    }

    /**
     * By default the thread is logged for FINE or less (finer,etc.).
     * The logging of the thread can also be explicitly turned on or off.
     */
    public boolean shouldPrintThread() {
        if (shouldPrintThread == null) {
            return getLevel() <= FINE;
        } else {
            return shouldPrintThread.booleanValue();
        }
    }

    /**
     * By default the thread is logged for FINE or less (finer,etc.).
     * The logging of the thread can also be explicitly turned on or off.
     */
    public void setShouldPrintThread(boolean shouldPrintThread) {
        if (shouldPrintThread) {
            this.shouldPrintThread = Boolean.TRUE;
        } else {
            this.shouldPrintThread = Boolean.FALSE;            
        }
    }

    /**
     * PUBLIC:
     * <p>
     * Return the writer that will receive the formatted log entries.
     * </p><p>
     *
     * @return the log writer
     * </p>
     */
    public Writer getWriter() {
        return writer;
    }

    /**
     * PUBLIC:
     * <p>
     * Set the writer that will receive the formatted log entries.
     * </p><p>
     *
     * @param writer  the log writer
     * </p>
     */
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    /**
     * Build and return a date format.
     * The default is a format that is sortable and easily parsed.
     * @return the date format
     */
    protected DateFormat buildDefaultDateFormat() {
        return new SimpleDateFormat("yyyy.MM.dd hh:mm:ss.SSS");
    }

    /**
     * PUBLIC:
     * Return the date format to be used when printing a log entry date.
     * @return the date format
     */
    public DateFormat getDateFormat() {
        if (dateFormat == null) {
            dateFormat = this.buildDefaultDateFormat();
        }
        return dateFormat;
    }

    /**
     * Return the specified date and/or time information in string.
     * The format will be determined by the date format settings.
     */
    protected String getDateString(Date date) {
        return this.getDateFormat().format(date);
    }

    /**
     * Return the supplement detail information including date, session, thread and connection.
     */
    protected String getSupplementDetailString(SessionLogEntry entry) {
        StringWriter writer = new StringWriter();

        if (shouldPrintDate()) {
            writer.write(getDateString(entry.getDate()));
            writer.write("--");
        }
        if (shouldPrintSession() && (entry.getSession() != null)) {
            writer.write(this.getSessionString(entry.getSession()));
            writer.write("--");
        }
        if (shouldPrintConnection() && (entry.getConnection() != null)) {
            writer.write(this.getConnectionString(entry.getConnection()));
            writer.write("--");
        }
        if (shouldPrintThread()) {
            writer.write(this.getThreadString(entry.getThread()));
            writer.write("--");
        }
        return writer.toString();
    }

    /**
     * Return the current session including the type and id.
     */
    protected String getSessionString(Session session) {
        // For bug 3422759 the session to log against should be the one in the
        // event, not the static one in the SessionLog, for there are many
        // sessions but only one SessionLog.
        if (session != null) {
            return ((AbstractSession)session).getLogSessionString();
        } else {
            return getSessionString();
        }
    }

    /**
     * Return the current session type.
     */
    protected void buildSessionType() {
        if (session != null) {
            sessionType = ((AbstractSession)session).getSessionTypeString();
        } else {
            sessionType = null;
        }
    }

    /**
     * Return the current session hash code.
     */
    protected void buildSessionHashCode() {
        if (session != null) {
            sessionHashCode = String.valueOf(System.identityHashCode(session));
        } else {
            sessionHashCode = null;
        }
    }

    /**
     * Return the current session including the type and id.
     */
    protected String getSessionString() {
        return sessionType + "(" + sessionHashCode + ")";
    }

    /**
     * Return the specified connection information.
     */
    protected String getConnectionString(Accessor connection) {
        // Bug 3630182 - if possible, print the actual connection's hashcode instead of just the accessor
        if (connection.getDatasourceConnection() == null){
            return CONNECTION_STRING + "(" + String.valueOf(System.identityHashCode(connection)) + ")";
        } else {
             return CONNECTION_STRING + "(" + String.valueOf(System.identityHashCode(connection.getDatasourceConnection())) + ")";   
        }
    }

    /**
     * Return the specified thread information.
     */
    protected String getThreadString(Thread thread) {
        return THREAD_STRING + "(" + String.valueOf(thread) + ")";
    }

    /**
     * Print the prefix string representing TopLink logging
     */

    //Bug3135111  Prefix strings are not translated until the first time they are used.
    protected void printPrefixString(int level) {
        try {
            switch (level) {
            case SEVERE:
                if (SEVERE_PREFIX == null) {
                    SEVERE_PREFIX = LoggingLocalization.buildMessage("toplink_severe");
                }
                this.getWriter().write(SEVERE_PREFIX);
                break;
            case WARNING:
                if (WARNING_PREFIX == null) {
                    WARNING_PREFIX = LoggingLocalization.buildMessage("toplink_warning");
                }
                this.getWriter().write(WARNING_PREFIX);
                break;
            case INFO:
                if (INFO_PREFIX == null) {
                    INFO_PREFIX = LoggingLocalization.buildMessage("toplink_info");
                }
                this.getWriter().write(INFO_PREFIX);
                break;
            case CONFIG:
                if (CONFIG_PREFIX == null) {
                    CONFIG_PREFIX = LoggingLocalization.buildMessage("toplink_config");
                }
                this.getWriter().write(CONFIG_PREFIX);
                break;
            case FINE:
                if (FINE_PREFIX == null) {
                    FINE_PREFIX = LoggingLocalization.buildMessage("toplink_fine");
                }
                this.getWriter().write(FINE_PREFIX);
                break;
            case FINER:
                if (FINER_PREFIX == null) {
                    FINER_PREFIX = LoggingLocalization.buildMessage("toplink_finer");
                }
                this.getWriter().write(FINER_PREFIX);
                break;
            case FINEST:
                if (FINEST_PREFIX == null) {
                    FINEST_PREFIX = LoggingLocalization.buildMessage("toplink_finest");
                }
                this.getWriter().write(FINEST_PREFIX);
                break;
            default:
                if (TOPLINK_PREFIX == null) {
                    TOPLINK_PREFIX = LoggingLocalization.buildMessage("toplink");
                }
                this.getWriter().write(TOPLINK_PREFIX);
            }
        } catch (IOException exception) {
            throw ValidationException.logIOError(exception);
        }
    }

    /**
     * PUBLIC:
     * Set the date format to be used when
     * printing a log entry date.
     * @param dateFormat java.text.DateFormat
     */
    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * Return the formatted message based on the information from the given SessionLogEntry.
     * The message will either be translated and formatted or formatted only depending
     * on if the shouldTranslate flag is set to true of false.
     */
    protected String formatMessage(SessionLogEntry entry) {
        String message = entry.getMessage();
        if (entry.shouldTranslate()) {
            if (entry.getLevel() > FINE) {
                message = LoggingLocalization.buildMessage(message, entry.getParameters());
            } else {
                message = TraceLocalization.buildMessage(message, entry.getParameters());
            }
        } else {
            //bug#2988,if there are entry parameters and the string "{0" contained in the message
            //body, we assume it needs to be formatted.
            if (entry.getParameters()!=null && entry.getParameters().length>0 && message.indexOf("{0") >= 0) {
                message = java.text.MessageFormat.format(message, entry.getParameters());
            }
        }
        return message;
    }

    /**
     * PUBLIC:
     * <p>
     * Log a throwable at FINER level.
     * </p><p>
     *
     * @param throwable a Throwable
     * </p>
     */
    public void throwing(Throwable throwable) {
        if (shouldLog(FINER)) {
            SessionLogEntry entry = new SessionLogEntry(null, throwable);
            entry.setLevel(FINER);
            log(entry);
        }
    }
    
    /**
     * INTERNAL:
     * Translate the string value of the log level to the constant value.
     * If value is null or invalid use the default.
     */
    public static int translateStringToLoggingLevel(String loggingLevel){
        if (loggingLevel == null){
            return INFO;
        } else if (loggingLevel.equals("OFF")){
            return OFF;
        } else if (loggingLevel.equals("SEVERE")){
            return SEVERE;
        } else if (loggingLevel.equals("WARNING")){
            return WARNING;
        } else if (loggingLevel.equals("INFO")){
            return INFO;
        } else if (loggingLevel.equals("CONFIG")){
            return CONFIG;
        } else if (loggingLevel.equals("FINE")){
            return FINE;
        } else if (loggingLevel.equals("FINER")){
            return FINER;
        } else if (loggingLevel.equals("FINEST")){
            return FINEST;
        } else if (loggingLevel.equals("ALL")){
            return ALL;
        }
        return INFO;
    }

    /**
     * INTERNAL:
     * Translate the string value of the log level to the constant value.
     * If value is null or invalid use the default.
     */
    public static String translateLoggingLevelToString(int loggingLevel){
        if (loggingLevel == OFF){
            return "OFF";
        } else if (loggingLevel == SEVERE){
            return "SEVERE";
        } else if (loggingLevel == WARNING){
            return "WARNING";
        } else if (loggingLevel == INFO){
            return "INFO";
        } else if (loggingLevel == CONFIG){
            return "CONFIG";
        } else if (loggingLevel == FINE){
            return "FINE";
        } else if (loggingLevel == FINER){
            return "FINER";
        } else if (loggingLevel == FINEST){
            return "FINEST";
        } else if (loggingLevel == ALL){
            return "ALL";
        }
        return "INFO";
    }

    /**
     * PUBLIC:
     * <p>
     * This method is called when a severe level message needs to be logged.
     * The message will be translated
     * </p><p>
     *
     * @param message  the message key
     * </p>
     */
    public void severe(String message) {
        log(SEVERE, message, (Object[])null);
    }

    /**
     * PUBLIC:
     * <p>
     * This method is called when a warning level message needs to be logged.
     * The message will be translated
     * </p><p>
     *
     * @param message  the message key
     * </p>
     */
    public void warning(String message) {
        log(WARNING, message, (Object[])null);
    }

    /**
     * PUBLIC:
     * <p>
     * This method is called when a info level message needs to be logged.
     * The message will be translated
     * </p><p>
     *
     * @param message  the message key
     * </p>
     */
    public void info(String message) {
        log(INFO, message, (Object[])null);
    }

    /**
     * PUBLIC:
     * <p>
     * This method is called when a config level message needs to be logged.
     * The message will be translated
     * </p><p>
     *
     * @param message  the message key
     * </p>
     */
    public void config(String message) {
        log(CONFIG, message, (Object[])null);
    }

    /**
     * PUBLIC:
     * <p>
     * This method is called when a fine level message needs to be logged.
     * The message will be translated
     * </p><p>
     *
     * @param message  the message key
     * </p>
     */
    public void fine(String message) {
        log(FINE, message, (Object[])null);
    }

    /**
     * PUBLIC:
     * <p>
     * This method is called when a finer level message needs to be logged.
     * The message will be translated
     * </p><p>
     *
     * @param message  the message key
     * </p>
     */
    public void finer(String message) {
        log(FINER, message, (Object[])null);
    }

    /**
     * PUBLIC:
     * <p>
     * This method is called when a finest level message needs to be logged.
     * The message will be translated
     * </p><p>
     *
     * @param message  the message key
     * </p>
     */
    public void finest(String message) {
        log(FINEST, message, (Object[])null);
    }

    /**
     * PUBLIC:
     * <p>
     * Log a throwable with level.
     * </p><p>
     *
     * @param level  the log request level value
     * </p><p>
     * @param throwable  a Throwable
     * </p>
     */
    public void logThrowable(int level, Throwable throwable) {
        // Must not create the log if not logging as is a performance issue.
        if (shouldLog(level)) {
            log(new SessionLogEntry(null, level, null, throwable));
        }
    }

    /**
     * PUBLIC:
     * Check if the log level is set to off.
     */
    public boolean isOff() {
        return this.level == OFF;
    }

    /**
     * INTERNAL:
     * Each session owns its own session log because session is stored in the session log
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (Exception exception) {
            return null;
        }
    }
}
