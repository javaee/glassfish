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

/*
 * AbstractLoggerFactory.java
 *
 * Created on May 13, 2002, 10:15 PM
 */


package com.sun.persistence.utility.logging;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rochelle Raccah
 * @version %I%
 */
abstract public class AbstractLoggerFactory implements LoggerFactory {
    private final static String _domainPrefix = "com.sun.persistence."; //NOI18N

    private final static Map _loggerCache = new HashMap();

    private static final String _bundleName = "com.sun.persistence.utility.logging.Bundle"; // NOI18N

    /**
     * Get the error logger which is used to log things during creation of
     * loggers.
     */
    protected static Logger getErrorLogger() {
        return LogHelper.getLogger(
                "", _bundleName, // NOI18N
                AbstractLoggerFactory.class.getClassLoader());
    }

    /**
     * Get a Logger.  The class that implements this interface is responsible
     * for creating a logger for the named component. The bundle name and class
     * loader are passed to allow the implementation to properly find and
     * construct the internationalization bundle.
     * @param relativeLoggerName the relative name of this logger
     * @param bundleName the fully qualified name of the resource bundle
     * @param loader the class loader used to load the resource bundle, or null
     * @return the logger
     */
    public synchronized Logger getLogger(String relativeLoggerName,
            String bundleName, ClassLoader loader) {
        String absoluteLoggerName = getAbsoluteLoggerName(relativeLoggerName);
        Logger value = (Logger) _loggerCache.get(absoluteLoggerName);

        if (value == null) {
            value = createLogger(absoluteLoggerName, bundleName, loader);

            if (value != null) {
                _loggerCache.put(absoluteLoggerName, value);
            }
        }

        return value;
    }

    /**
     * Create a new Logger.  Subclasses are responsible for creating a logger
     * for the named component.  The bundle name and class loader are passed to
     * allow the implementation to properly find and construct the
     * internationalization bundle.
     * @param absoluteLoggerName the absolute name of this logger
     * @param bundleName the fully qualified name of the resource bundle
     * @param loader the class loader used to load the resource bundle, or null
     * @return the logger
     */
    abstract protected Logger createLogger(String absoluteLoggerName,
            String bundleName, ClassLoader loader);

    protected String getDomainRoot() {
        return _domainPrefix;
    }

    protected String getAbsoluteLoggerName(String relativeLoggerName) {
        return (relativeLoggerName.startsWith("java") ? //NOI18N
                relativeLoggerName : (getDomainRoot() + relativeLoggerName));
    }
}
