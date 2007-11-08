/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */


package com.sun.persistence.utility.logging;

/**
 * This class manages the logging facility for JDO components.  It is the class
 * that keeps track of the log factory in use for getting loggers for use in JDO
 * components. <P> This class has no JDK 1.4 dependencies. <P> The log factory
 * is responsible for constructing the loggers and for ensuring that there is
 * only one logger per component.
 * @author Craig Russell
 * @version 1.0
 */

public class LogHelper {

    /**
     * Flag to tell we are running in JDK 1.4 and can use
     * java.util.logging.Logger implementation.
     */
    protected static boolean jdk14 = isJDK14();

    /**
     * LoggerFactory registered for creating new loggers.
     */
    protected static LoggerFactory loggerFactory = null;

    /**
     * Get a Logger.  This call is delegated to the registered LoggerFactory. If
     * there is no registered LoggerFactory, then initialize one based on
     * whether we are running in JDK 1.4 (or higher). The bundle name and class
     * loader are passed to allow the implementation to properly find and
     * construct the internationalization bundle. This method is synchronized to
     * avoid race conditions where two threads access a component using the same
     * Logger at the same time.
     * @param loggerName the relative name of this logger
     * @param bundleName the fully qualified name of the resource bundle
     * @param loader the class loader used to load the resource bundle, or null
     * @return the logger
     */
    public synchronized static Logger getLogger(String loggerName,
            String bundleName, ClassLoader loader) {
        // if an implementation has not registered a LoggerFactory, use a standard one.
        if (loggerFactory == null) {
            if (jdk14) {
                loggerFactory = new LoggerFactoryJDK14();
            } else {
                loggerFactory = new LoggerFactoryJDK13();
            }
        }
        return loggerFactory.getLogger(loggerName, bundleName, loader);
    }

    /**
     * Register a LoggerFactory for use in managed environments or for special
     * situations.  This factory will be delegated to for all getLogger
     * requests.
     * @param factory the LoggerFactory to use for all getLogger requests
     */
    public static void registerLoggerFactory(LoggerFactory factory) {
        loggerFactory = factory;
    }

    /**
     * Check to see if the JDK 1.4 logging environment is available.
     * @return true if JDK 1.4 logging is available
     */
    public static boolean isJDK14() {
        try {
            Class logger = Class.forName("java.util.logging.Logger"); //NOI18N
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

}
