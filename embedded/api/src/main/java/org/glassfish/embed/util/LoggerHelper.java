/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

/* 
 * LoggerHelper.java
 *
 * Created on January 20, 2004, 5:55 PM
 * 
 * @author  bnevins
 *
 */
package org.glassfish.embed.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;
import static org.glassfish.embed.util.ServerConstants.*;


// Resource Bundle:
// com/sun/logging/enterprise/system/tools/deployment/backend/LogStrings.properties
public class LoggerHelper {

    private LoggerHelper() {
    }

    ///////////////////////////////////////////////////////////////////////////
    public final static Logger get() {
        // the final should cause this to be inlined...
        return logger;
    }

    /**
     * Sets the log level on the given logger.
     *
     * If no logger name is provided, where loggerName is NULL, then the new log
     * level will be set on the Embedded Logger ("org.glassfish.embed") and the
     * GlassFish v3 Root Logger ("javax.enterprise").
     *
     * @param loggerName logger to set level on.  NULL is allowed.
     * @param newLevel the new log {@link Level}
     * @see Table 9–1 Logger Namespaces for Enterprise Server Modules in
     * <a href="http://docs.sun.com/app/docs/doc/820-4495/abluj?a=view">Chapter 9 Administering Logging</a>
     * of the Sun GlassFish Enterprise Server v3 Prelude Administration Guide
     */
    public final static void setLevel(String loggerName, Level newLevel) {
        // the final should cause this to be inlined...
        if (loggerName != null) {
            Logger.getLogger(loggerName).setLevel(newLevel);
        } else {
            Logger.getLogger(ServerConstants.GFV3_ROOT_LOGGER).setLevel(newLevel);
            logger.setLevel(newLevel);
        }
    }

    /**
     * Sets the log level on the Embedded Logger ("org.glassfish.embed") and the
     * GlassFish v3 Root Logger ("javax.enterprise").
     *
     * @param newLevel the new log {@link Level}
     */
    public final static void setLevel(Level newLevel) {
       setLevel(null, newLevel);
    }

    ///////////////////////////////////////////////////////////////////////////
    ////////         Convenience methods        ///////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    public final static void finest(String s) {
        logger.finest(s);
    }

    public final static void finest(String s, Object o) {
        logger.log(Level.FINEST, s, new Object[]{o});
    }

    public final static void finer(String s) {
        logger.finer(s);
    }

    public final static void finer(String s, Object o) {
        logger.log(Level.FINER, s, new Object[]{o});
    }

    public final static void fine(String s) {
        logger.fine(s);
    }

    public final static void fine(String s, Object o) {
        logger.log(Level.FINE, s, new Object[]{o});
    }

    public final static void info(String s) {
        logger.info(s);
    }

    public final static void info(String s, Object o) {
        logger.log(Level.INFO, s, new Object[]{o});
    }

    public final static void warning(String s) {
        logger.warning(s);
    }

    public final static void warning(String s, Object o) {
        logger.log(Level.WARNING, s, new Object[]{o});
    }

    public final static void severe(String s) {
        logger.severe(s);
    }

    public final static void severe(String s, Object o) {
        logger.log(Level.SEVERE, s, new Object[]{o});
    }
    
    ///////////////////////////////////////////////////////////////////////////

    private static Logger logger = null;

    static {
        try {
            logger = Logger.getLogger(EMBEDDED_LOGGER, LOGGING_RESOURCE_BUNDLE);
        }
        catch (Throwable t) {
            try {
                logger = Logger.getLogger(EMBEDDED_LOGGER);
                logger.warning("Couldn't create Logger with a resource bundle.  Created a Logger without a Resource Bundle.");
            }
            catch (Throwable t2) {
            // now what?
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    /*
    public final static void startFileLogging() {
        setLogFile(getGfeLogPath());
    }
    */
    
    public final static void stopConsoleLogging() {
        for (Handler h  : rootLogger.getHandlers()) {
            if (h instanceof ConsoleHandler) {
                h.setLevel(Level.OFF);
            }
        }

    }

    public final static void setRootLoggerLevel(Level level) {
        // This will log records from all loggers including GFv3 loggers
        // to a log file gfe/gfe.log. By default
        rootLogger.setLevel(level);
    }

    public final static void setLogFile(String logFile) {
        FileHandler fh = null;
        try {
            fh = new FileHandler(logFile, true);
        } catch (IOException ex) {
            Logger.getLogger(LoggerHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(LoggerHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (fh != null) {
            fh.setFormatter(new SimpleFormatter());
            rootLogger.addHandler(fh);
        }
    }


    /*
    public static String getGfeLogPath() {
            return GFE_LOG;
    }
*/
    private static Logger rootLogger = Logger.getLogger("");
    private File logfile;
    //public static final String GFE_LOG = "gfe.log";
}

