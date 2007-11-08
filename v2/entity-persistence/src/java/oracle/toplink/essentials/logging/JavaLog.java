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


/** Java class "JavaLog.java" generated from Poseidon for UML.
 *  Poseidon for UML is developed by <A HREF="http://www.gentleware.com">Gentleware</A>.
 *  Generated with <A HREF="http://jakarta.apache.org/velocity/">velocity</A> template engine.
 */
import java.util.*;
import java.util.logging.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import oracle.toplink.essentials.sessions.Session;
import oracle.toplink.essentials.internal.localization.i18n.*;

/**
 * PUBLIC:
 * <p>
 * This is a wrapper class for java.util.logging.  It is used when messages need to
 * be logged through java.util.logging.
 * </p>
 *  @see SessionLog
 *  @see AbstractSessionLog
 *  @see SessionLogEntry
 *  @see Session
 */
public class JavaLog extends AbstractSessionLog {

    /**
     * Stores the default session name in case there is the session name is missing.
     */
    public static final String TOPLINK_NAMESPACE = "oracle.toplink.essentials";
    public static final String DEFAULT_TOPLINK_NAMESPACE = TOPLINK_NAMESPACE + ".default";
    public static final String SESSION_TOPLINK_NAMESPACE = TOPLINK_NAMESPACE + ".session";

    /**
     * Stores all the java.util.logging.Levels.  The indexes are TopLink logging levels.
     */
    private static final Level[] levels = new Level[] { Level.ALL, Level.FINEST, Level.FINER, Level.FINE, Level.CONFIG, Level.INFO, Level.WARNING, Level.SEVERE, Level.OFF };

    /**
     * Stores the Logger for default TopLink namespace, i.e. "oracle.toplink.essentials".
     * This namespace should be a child of the TopLink namespace, e.g.
     * "oracle.toplink.essentials.default", otherwise AbstractSessionLog will set the explicit 
     * level for all TopLink loggers (overriding the container settings) in the VM on 
     * the first PU with the specified log level.
     */
    private static Logger defaultTopLinkLogger = Logger.getLogger(DEFAULT_TOPLINK_NAMESPACE);

    /**
     * Represents the HashMap that stores all the name space strings.
     * The keys are category names.  The values are namespace strings.
     */
    private Map nameSpaceMap  = new HashMap();

    /**
     * Stores the namespace for session, i.e."oracle.toplink.essentials.session.<sessionname>".
     */
    private String sessionNameSpace;

    /**
     * Stores the Logger for session namespace, i.e. "oracle.toplink.essentials.session.<sessionname>".
     */
    private Logger sessionLogger;

    private Map categoryloggers = new HashMap<String, Logger>();


    /**
     * PUBLIC:
     * <p>
     * Return the effective log level for the name space extracted from session and category.
     * If a Logger's level is set to be null then the Logger will use an effective Level that will
     * be obtained by walking up the parent tree and using the first non-null Level.
     * </p><p>
     *
     * @return the effective log level.
     * </p>
     */
    public int getLevel(String category) {
        Logger logger = getLogger(category);
        while ((logger != null) && (logger.getLevel() == null)) {
            logger = logger.getParent();
        }

        if (logger == null) {
            return OFF;
        }

        //For a given java.util.logging.Level, return the index (ie, TopLink logging level)
        int logLevel = logger.getLevel().intValue();
        for (int i = 0; i < levels.length ; i++) {
            if (logLevel == levels[i].intValue()) {
                return i;
            }
        }
        return OFF;
    }

    /**
     * PUBLIC:
     * <p>
     * Set the log level to a logger with name space extracted from the given category.
     * </p>
     */
    public void setLevel(final int level, String category) {
        final Logger logger = getLogger(category);
        if (logger == null) {
            return;
        }
        
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                logger.setLevel(getJavaLevel(level));
                return null; // nothing to return
            }
        });
    }

    /**
     * INTERNAL:
     * Return the name space for the given category from the map.
     */
    protected String getNameSpaceString(String category) {
        if (session == null) {
            return DEFAULT_TOPLINK_NAMESPACE;
        } else if ((category == null) || (category.length() == 0)) {
            return sessionNameSpace;
        } else {
            return  (String)nameSpaceMap.get(category);
        }
    }

    /**
     * INTERNAL:
     * Return the Logger for the given category
     */
    protected Logger getLogger(String category) {
        if (session == null) {
            return defaultTopLinkLogger;
        } else if ((category == null) || (category.length() == 0)) {
            return sessionLogger;
        } else {
            Logger logger = (Logger) categoryloggers.get(category);
            // If session != null, categoryloggers should have an entry for this category
            assert logger != null;
            return logger;
        }
    }


    /**
     * PUBLIC:
     * <p>
     * Set the session and session namespace.
     * </p>
     *
     * @param session  a Session
     * </p>
     */
    public void setSession(Session session) {
        super.setSession(session);
        if (session != null) {
            String sessionName = session.getName();
            if ((sessionName != null) && (sessionName.length() != 0)) {
                sessionNameSpace = SESSION_TOPLINK_NAMESPACE + "." + sessionName;
            } else {
                sessionNameSpace = DEFAULT_TOPLINK_NAMESPACE;
            }

            //Initialize loggers eagerly
            sessionLogger = Logger.getLogger(sessionNameSpace);
            for (int i = 0; i < loggerCategories.length; i++) {
                String loggerCategory =  loggerCategories[i]; 
                String loggerNameSpace = sessionNameSpace + "." + loggerCategory;
                nameSpaceMap.put(loggerCategory, loggerNameSpace);
                categoryloggers.put(loggerCategory, Logger.getLogger(loggerNameSpace));
            }
        }
    }

    /**
     * INTERNAL:
     * Return the corresponding java.util.logging.Level for a given TopLink level.
     */
    private Level getJavaLevel(int level) {
        return levels[level];
    }

    /**
     * PUBLIC:
     * <p>
     * Check if a message of the given level would actually be logged by the logger
     * with name space built from the given session and category.
     * Return the shouldLog for the given category from
     * </p><p>
     * @return true if the given message level will be logged
     * </p>
     */
    public boolean shouldLog(int level, String category) {
        Logger logger = getLogger(category);
        return logger.isLoggable(getJavaLevel(level));
    }

    /**
     * PUBLIC:
     * <p>
     * Log a SessionLogEntry
     * </p><p>
     * @param entry SessionLogEntry that holds all the information for a TopLink logging event
     * </p>
     */
    public void log(SessionLogEntry entry) {
        if (!shouldLog(entry.getLevel(), entry.getNameSpace())) {
            return;
        }

        Logger logger = getLogger(entry.getNameSpace());
        Level javaLevel = getJavaLevel(entry.getLevel());

        internalLog(entry, javaLevel, logger);
    }

    /**
     * INTERNAL:
     * <p>
     * Build a LogRecord
     * </p><p>
     * @param entry SessionLogEntry that holds all the information for a TopLink logging event
     * @param javaLevel the message level
     * </p>
     */
    protected void internalLog(SessionLogEntry entry, Level javaLevel, Logger logger) {
        // Format message so that we do not depend on the bundle
        TopLinkLogRecord lr = new TopLinkLogRecord(javaLevel, formatMessage(entry)); 

        lr.setSourceClassName(null);
        lr.setSourceMethodName(null);
        lr.setLoggerName(getNameSpaceString(entry.getNameSpace()));
        if (shouldPrintSession()) {
            lr.setSessionString(getSessionString(entry.getSession()));
        }
        if (shouldPrintConnection()) {
            lr.setConnection(entry.getConnection());
        }
        lr.setThrown(entry.getException());
        lr.setShouldLogExceptionStackTrace(shouldLogExceptionStackTrace());
        lr.setShouldPrintDate(shouldPrintDate());
        lr.setShouldPrintThread(shouldPrintThread());
        logger.log(lr);
    }

    /**
     * PUBLIC:
     * <p>
     * Log a throwable.
     * </p><p>
     * @param throwable a throwable
     * </p>
     */
    public void throwing(Throwable throwable) {
        getLogger(null).throwing(null, null, throwable);
    }

    /**
     * INTERNAL:
     * Each session owns its own session log because session is stored in the session log
     */
    public Object clone() {
        // There is no special treatment required for cloning here
        // The state of this object is described  by member variables sessionLogger and categoryLoggers.
        // This state depends on session.
        // If session for the clone is going to be the same as session for this there is no
        // need to do "deep" cloning.
        // If not, the session being cloned should call setSession() on its JavaLog object to initialize it correctly.
        JavaLog cloneLog = (JavaLog)super.clone();
        return cloneLog;
    }
}

