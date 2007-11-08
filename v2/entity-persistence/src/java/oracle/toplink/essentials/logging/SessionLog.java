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

import java.io.Writer;
import oracle.toplink.essentials.sessions.Session;

/**
 * SessionLog is the ever-so-simple interface used by
 * TopLink to log generated messages and SQL. An implementor of
 * this interface can be passed to the TopLink session
 * (via the #setSessionLog(SessionLog) method); and
 * all logging data will be passed through to the implementor
 * via an instance of SessionLogEntry. This can be used
 * to supplement debugging; or the entries could be stored
 * in a database instead of logged to System.out; etc.
 *
 *  @see AbstractSessionLog
 *  @see SessionLogEntry
 *
 * @since TOPLink/Java 3.0
 */
public interface SessionLog {
    //TopLink log levels.  They are mapped to java.util.logging.Level values
    public static final int OFF = 8;

    //TL is not in a state to continue
    public static final int SEVERE = 7;

    //Exceptions that don't force a stop
    public static final int WARNING = 6;

    //Login and logout per server session with name
    public static final int INFO = 5;

    //Configuration info
    public static final int CONFIG = 4;

    //SQL
    public static final int FINE = 3;

    //Previously logged under logMessage and stack trace of exceptions at WARNING level
    public static final int FINER = 2;

    //Previously logged under logDebug
    public static final int FINEST = 1;
    public static final int ALL = 0;

    //TopLink categories used for logging name space.
    //If you add a new category, please Please ensure to add it to
    //loggerCategories array below
    public static final String SQL = "sql";
    public static final String TRANSACTION = "transaction";
    public static final String EVENT = "event";
    public static final String CONNECTION = "connection";
    public static final String QUERY = "query";
    public static final String CACHE = "cache";
    public static final String PROPAGATION = "propagation";
    public static final String SEQUENCING = "sequencing";
    public static final String EJB = "ejb";
    public static final String DMS = "dms";
    public static final String EJB_OR_METADATA = "ejb_or_metadata";
    public static final String WEAVER = "weaver";
    public static final String PROPERTIES = "properties";
    public final String[] loggerCategories = new String[] { SQL ,TRANSACTION ,EVENT ,CONNECTION ,QUERY ,CACHE ,PROPAGATION ,SEQUENCING ,EJB ,DMS ,EJB_OR_METADATA ,WEAVER ,PROPERTIES};

    /**
     * INTERNAL:
     * TopLink will call this method whenever something
     * needs to be logged (messages, SQL, etc.).
     * All the pertinent information will be contained in
     * the specified entry.
     *
     * @param entry oracle.toplink.essentials.sessions.LogEntry
     */
    void log(SessionLogEntry entry);

    /**
     * By default the stack trace is logged for SEVERE all the time and at FINER level for WARNING or less,
     * this can be turned off.
     */
    boolean shouldLogExceptionStackTrace();

    /**
     * By default the date is always printed, this can be turned off.
     */
    boolean shouldPrintDate();

    /**
     * By default the thread is logged at FINE or less level, this can be turned off.
     */
    boolean shouldPrintThread();

    /**
     * By default the connection is always printed whenever available, this can be turned off.
     */
    boolean shouldPrintConnection();

    /**
     * By default the Session is always printed whenever available, this can be turned off.
     */
    boolean shouldPrintSession();

    /**
     * By default stack trace is logged for SEVERE all the time and at FINER level for WARNING or less.
     * This can be turned off.
     */
    void setShouldLogExceptionStackTrace(boolean flag);

    /**
     * By default date is printed, this can be turned off.
     */
    void setShouldPrintDate(boolean flag);

    /**
     * By default the thread is logged at FINE or less level, this can be turned off.
     */
    void setShouldPrintThread(boolean flag);

    /**
     * By default the connection is always printed whenever available, this can be turned off.
     */
    void setShouldPrintConnection(boolean flag);

    /**
     * By default the Session is always printed whenever available, this can be turned off.
     */
    void setShouldPrintSession(boolean flag);

    /**
     * PUBLIC:
     * Return the writer to which an accessor writes logged messages and SQL.
     * If not set, this reference usually defaults to a writer on System.out.
     * To enable logging, logMessages must be turned on in the session.
     */
    Writer getWriter();

    /**
     * PUBLIC:
     * Set the writer to which an accessor writes logged messages and SQL.
     * If not set, this reference usually defaults to a writer on System.out.
     * To enable logging, logMessages() is used on the session.
     */
    void setWriter(Writer log);

    /**
     * PUBLIC:
     * Return the log level.  Used when session is not available.
     */
    int getLevel();

    /**
     * PUBLIC:
     * Return the log level.  category is only needed where name space
     * is available.
     */
    int getLevel(String category);

    /**
     * PUBLIC:
     * Set the log level.  Used when session is not available.
     */
    void setLevel(int level);

    /**
     * PUBLIC:
     * Set the log level.  Category is only needed where name space
     * is available.
     */
    void setLevel(int level, String category);

    /**
     * PUBLIC:
     * Check if a message of the given level would actually be logged.
     * Used when session is not available.
     */
    boolean shouldLog(int level);

    /**
     * PUBLIC:
     * Check if a message of the given level would actually be logged.
     * Category is only needed where name space is available.
     */
    boolean shouldLog(int level, String category);

    /**
     * PUBLIC:
     * Log a message that does not need to be translated.  This method is intended for 
	 * external use when logging messages are wanted within the TopLink output.
     */
    void log(int level, String message);

    /**
     * INTERNAL:
     * Log a message with one parameter that needs to be translated.
     */
    public void log(int level, String message, Object param);

    /**
     * INTERNAL:
     * Log a message with two parameters that needs to be translated.
     */
    void log(int level, String message, Object param1, Object param2);

    /**
     * INTERNAL:
     * Log a message with three parameters that needs to be translated.
     */
    void log(int level, String message, Object param1, Object param2, Object param3);

    /**
     * INTERNAL:
     * This method is called when the log request is from somewhere session is not available.
     * The message needs to be translated.
     */
    void log(int level, String message, Object[] arguments);

    /**
     * INTERNAL:
     * This method is called when the log request is from somewhere session is not available.
     * shouldTranslate flag determines if the message needs to be translated.
     */
    void log(int level, String message, Object[] arguments, boolean shouldTranslate);

    /**
     * PUBLIC:
     * This method is called when a throwable at finer level needs to be logged.
     */
    void throwing(Throwable throwable);

    /**
     * PUBLIC:
     * This method is called when a severe level message needs to be logged.
     * The message will be translated
     */
    void severe(String message);

    /**
     * PUBLIC:
     * This method is called when a warning level message needs to be logged.
     * The message will be translated
     */
    void warning(String message);

    /**
     * PUBLIC:
     * This method is called when a info level message needs to be logged.
     * The message will be translated
     */
    void info(String message);

    /**
     * PUBLIC:
     * This method is called when a config level message needs to be logged.
     * The message will be translated
     */
    void config(String message);

    /**
     * PUBLIC:
     * This method is called when a fine level message needs to be logged.
     * The message will be translated
     */
    void fine(String message);

    /**
     * PUBLIC:
     * This method is called when a finer level message needs to be logged.
     * The message will be translated
     */
    void finer(String message);

    /**
     * PUBLIC:
     * This method is called when a finest level message needs to be logged.
     * The message will be translated
     */
    void finest(String message);

    /**
     * PUBLIC:
     * Log a throwable with level.
     */
    void logThrowable(int level, Throwable throwable);

    /**
     * PUBLIC:
     * Get the session that owns this SessionLog.
     */
    Session getSession();

    /**
     * PUBLIC:
     * Set the session that owns this SessionLog.
     */
    void setSession(Session session);
}
