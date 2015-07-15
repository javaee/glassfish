/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.utilities.reflection;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.glassfish.hk2.utilities.general.GeneralUtilities;

/**
 * A logger for HK2.  Currently implemented over the JDK logger
 * 
 * @author jwells
 */
public class Logger {
    private static final Logger INSTANCE = new Logger();
    private static final String HK2_LOGGER_NAME = "org.jvnet.hk2.logger";
    private static final boolean STDOUT_DEBUG = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
        @Override
        public Boolean run() {
            return Boolean.parseBoolean(
                System.getProperty("org.jvnet.hk2.logger.debugToStdout", "false"));
        }
            
    });
    
    private final java.util.logging.Logger jdkLogger;
    
    private Logger() {
        jdkLogger = java.util.logging.Logger.getLogger(HK2_LOGGER_NAME);
    }
    
    /**
     * Gets the singleton instance of the Logger
     * @return The singleton logger instance (will not return null)
     */
    public static Logger getLogger() {
        return INSTANCE;
    }

    /**
     * Sends this message to the Debug channel (FINER level in JDK parlance)
     * 
     * @param debuggingMessage The non-null message to log to the debug logger
     */
    public void debug(String debuggingMessage) {
        jdkLogger.finer(debuggingMessage);
        if (STDOUT_DEBUG) {
            System.out.println("HK2DEBUG: " + debuggingMessage);
        }
    }
    
    /**
     * Sends this message to the Debug channel (FINER level in JDK parlance)
     * 
     * @param warningMessage The non-null message to log to the debug logger
     */
    public void warning(String warningMessage) {
        jdkLogger.warning(warningMessage);
        if (STDOUT_DEBUG) {
            System.out.println("HK2DEBUG (Warning): " + warningMessage);
        }
    }
    
    /**
     * Prints a throwable to stdout
     * 
     * @param th The throwable to print
     */
    public static void printThrowable(Throwable th) {
        int lcv = 0;
        Throwable cause = th;
        
        while (cause != null) {
            System.out.println("HK2DEBUG: Throwable[" + lcv++ + "] message is " + cause.getMessage());
            cause.printStackTrace(System.out);
            
            cause = cause.getCause();
        }
    }
    
    /**
     * Sends this message to the Debug channel (FINER level in JDK parlance)
     * 
     * @param className The name of the class where this was thrown
     * @param methodName The name of the method where this was thrown
     * @param th The exception to log
     */
    public void debug(String className, String methodName, Throwable th) {
        jdkLogger.throwing(className, methodName, th);
        if (STDOUT_DEBUG) {
            System.out.println("HK2DEBUG: className=" + className + " methodName=" + methodName);
            printThrowable(th);
        }
    }
}
