/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.admin.cli;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.io.ByteArrayOutputStream;

/**
 * A special logger for CLI commands.
 *
 * @author  pa100654
 */
public class CLILogger {
    private static boolean trace;
    private static boolean debug;
    private static CLILogger logger;
    private Logger s1asLogger;
    private final static String DEBUG_FLAG = "Debug";
    private final static String ENV_DEBUG_FLAG = "AS_DEBUG";
    private final static int	kDefaultBufferSize	= 512;
    private final static String PACKAGE_NAME = "com.sun.enterprise.admin.cli";

    /** Creates a new instance of CLILogger */
    protected CLILogger() {
        // bnevins 4-18-08 A quickly added trace. should clean up later.
        // TODO TODO TODO TODO

        // System Prop just needs to exist
        // Env Var. needs to be set to "true"
        String sys = System.getProperty(DEBUG_FLAG);
        boolean env = Boolean.parseBoolean(System.getenv(ENV_DEBUG_FLAG));
        trace = Boolean.parseBoolean(System.getenv("AS_TRACE"));

        debug = sys != null || env;

        if (trace)
            debug = true;

        s1asLogger = Logger.getLogger(PACKAGE_NAME, null);
        if (trace)
            s1asLogger.setLevel(Level.FINEST);
        else if (isDebug())
            s1asLogger.setLevel(Level.FINER);
        else {
            s1asLogger.setLevel(Level.INFO);
            //s1asLogger.setLevel(Level.SEVERE);
        }
        s1asLogger.addHandler(new CLILoggerHandler());
        s1asLogger.setUseParentHandlers(false);
    }

    public static boolean isDebug() {
        return debug;
    }

    /**
     * returns the instance of the logger
     */
    public static synchronized CLILogger getInstance() {
        if (logger == null) {
            logger = new CLILogger();
        }
        return logger;
    }

    /**
     * returns the current output Level
     * @return Level the java.util.logging.Level
     */
    public Level getOutputLevel() {
        return s1asLogger.getLevel();
    }

    /**
     * Sets the output Level
     * @param level the java.util.logging.Level
     */
    public void setOutputLevel(Level level) {
        if (!isDebug())
            s1asLogger.setLevel(level);
    }

    /**
     * Will messages at thislevel be logged?
     */
    public boolean isLoggable(Level level) {
        return s1asLogger.isLoggable(level);
    }

    /**
     * prints the message with level as INFO
     * @param message the message to be written on the output stream
     */
    public void printMessage(String message) {
        s1asLogger.log(Level.INFO, message);
    }

    /**
     * prints the message with level as FINE
     * @param message the message to be written on the output stream
     */
    public void printDetailMessage(String message) {
        s1asLogger.log(Level.FINE, message);
    }

    /**
     * prints the message with level as WARNING
     * @param message the message to be written on the output stream
     */
    public void printWarning(String message) {
        s1asLogger.log(Level.WARNING, message);
    }

    /**
     * prints the message with level as SEVERE
     * @param message the message to be written on the output stream
     */
    public void printError(String message) {
        s1asLogger.log(Level.SEVERE, message);
    }

    /**
     * prints the message with level as FINER
     * @param message the message to be written on the output stream
     */
    public void printDebugMessage(String message) {
        s1asLogger.log(Level.FINER, message);
    }

    /**
     * prints the message with level as FINEST
     * @param message the message to be written on the output stream
     */
    public void printTraceMessage(String message) {
        s1asLogger.log(Level.FINEST, message);
    }


    /**
     * prints the exception message with level as FINEST
     * @param e - the exception object to print
     */
    public void printExceptionStackTrace(java.lang.Throwable e) {
	/*
	java.lang.StackTraceElement[] ste = e.getStackTrace();
	for (int ii=0; ii<ste.length; ii++)
        {
	    printDebugMessage(ste[ii].toString());
	}
	*/
    	final ByteArrayOutputStream output =
            new ByteArrayOutputStream(kDefaultBufferSize);
    	e.printStackTrace(new java.io.PrintStream(output));
        printDebugMessage(output.toString());
    }

    public class CLILoggerHandler extends Handler {

        /** Creates a new instance of CLILoggerHandler */
        public CLILoggerHandler() {
        }

        public void publish(java.util.logging.LogRecord logRecord) {
            if (logRecord.getLevel() == Level.SEVERE) {
		System.err.println(logRecord.getMessage());
            } else {
                System.out.println(logRecord.getMessage());
            }
        }

        public void close() throws java.lang.SecurityException {
        }

        public void flush() {
        }
    }
}
