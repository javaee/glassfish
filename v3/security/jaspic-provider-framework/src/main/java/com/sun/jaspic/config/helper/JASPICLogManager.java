/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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


package com.sun.jaspic.config.helper;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * 
 */
public class JASPICLogManager {

    /**
     * PACKAGE_ROOT the prefix for the packages where logger resource
     * bundles reside.
     */
    public static final String PACKAGE_ROOT = "com.sun.logging.";

    /**
     * RESOURCE_BUNDLE the name of the logging resource bundles.
     */
    public static final String RESOURCE_BUNDLE = "LogStrings";

    /**
     * Field
     */
    public static final String JASPIC_LOGGER = "enterprise.system.jaspic.security";

    /**
     * Method getLogger
     *
     * @param clazz
     * @param name
     * @return
     */

    public static synchronized Logger getLogger(final Class clazz) {
        return getLogger(clazz, JASPIC_LOGGER);
    }
    /**
     * Method getLogger
     *
     * @param clazz
     * @param name
     * @return
     */

    public static synchronized Logger getLogger(final Class clazz, final String name) {
        final String resBundleName = PACKAGE_ROOT + "." + name + "." + RESOURCE_BUNDLE;

        final ClassLoader cloader = clazz.getClassLoader();
        final String className = clazz.getName();
        final String loggerName = name + "." + className;
        Logger cLogger = LogManager.getLogManager().getLogger(loggerName);
        if (cLogger == null) {
            //first time through for this logger.  create it and find the resource bundle
            cLogger = new Logger(loggerName, null) {
                private final int offValue = Level.OFF.intValue();
                public void log(LogRecord record) {
                    if(record.getResourceBundle()==null) {
                        ResourceBundle rb = getResourceBundle();
                        if(rb!=null) {
                            record.setResourceBundle(rb);
                        }
                    }
                    record.setThreadID((int) Thread.currentThread().getId());
                    record.setLoggerName(loggerName);
                    record.setSourceClassName(className);
                    super.log(record);
                }


                /**
                 * Retrieve the localization resource bundle for this
                 * logger for the current default locale.
                 *
                 * @return localization bundle
                 *
                 */
                public ResourceBundle getResourceBundle() {

                    try {

                        return ResourceBundle.getBundle(resBundleName, Locale.getDefault(), cloader);
                    } catch (MissingResourceException e) {
                        Logger l = LogManager.getLogManager().getLogger(name);
                        l.log(Level.WARNING, "Can not find resource bundle for this logger. " + " class name that failed: " + clazz.getName());
                        //throw e;
                        return null;
                    }
                };
            };

            // We must not return an orphan logger (the one we just created) if
            // a race condition has already created one
            if (!addLoggerToLogManager(cLogger)) {
                final Logger existing = LogManager.getLogManager().getLogger(loggerName);
                if (existing == null) {
                    addLoggerToLogManager(cLogger);
                } else {
                    cLogger = existing;
                }

            }
        };
        return cLogger;
    }

    private static boolean addLoggerToLogManager(Logger logger) {
        // bnevins April 30, 2009 -- there is a bug in the JDK having to do with
        // the ordering of synchronization in the logger package.
        // The work-around is to ALWAYS lock in the order that the JDK bug
        // is assuming.  That means lock A-B-A instead of B-A
        // A == Logger.class, B == LogManager.class
        // I created this method to make it very very clear what is going on

        synchronized (Logger.class) {
            return LogManager.getLogManager().addLogger(logger);
        }
    }
}
