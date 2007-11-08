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


package com.sun.persistence.enterprise.impl;

import java.security.AccessController;
import java.security.PrivilegedAction;

import com.sun.persistence.utility.logging.LoggerFactoryJDK14;
import com.sun.persistence.utility.logging.LoggerJDK14;

import com.sun.logging.LogDomains;

/**
 * @author Craig Russell
 * @version 1.0
 */

public class LoggerFactoryAS extends LoggerFactoryJDK14 {

    /**
     * The top level of the logger domain for application server.
     */
    protected String DOMAIN_ROOT = "javax.enterprise.resource.jdo."; //NOI18N

    /**
     * Creates new LoggerFactory
     */
    public LoggerFactoryAS() {
    }

    protected String getDomainRoot() {
        return DOMAIN_ROOT;
    }

    /**
     * Create a new Logger.  Create a logger for the named component. The bundle
     * name is passed to allow the implementation to properly find and construct
     * the internationalization bundle.
     *
     * This operation is executed as a privileged action to allow permission
     * access for creating new logger and setting its defaults.
     * @param absoluteLoggerName the absolute name of this logger
     * @param bundleName the fully qualified name of the resource bundle
     * @return the logger
     */
    protected LoggerJDK14 createLogger(final String absoluteLoggerName,
            final String bundleName) {
        return (LoggerJDK14) AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        LoggerJDK14 result = new LoggerJDK14(
                                absoluteLoggerName, bundleName);

                        return result;
                    }
                });
    }

    /**
     * This method is a no-op in the Sun ONE Application server.
     */
    protected void configureFileHandler(LoggerJDK14 logger) {
    }

}

