/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.server.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.LogService;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.server.pluggable.LoggingSupport;
import com.sun.enterprise.util.StringUtils;

/**
 * Support for logging features when used with http engine from
 * tomcat.
 */
public class TomcatLoggingSupport implements LoggingSupport {

    private static String defaultLogFileName = null;
    private static String logFileName = null;
    private static FileHandler fHandler = null;

    /**
     * Create a log handler object.
     */
    public Handler createLogHandler() {
        String fileName = getLogFileName();
        Handler h = null;
        try {
	    if (fHandler == null) {
                fHandler = new FileHandler(fileName, true);
	    }
        } catch (Exception e) {
            // If there is an exception in creation of file handler,
            // use a console handler instead.
            // NOI18N
            System.err.println("Error creating log handler for " + fileName
                    + ". Using ConsoleHandler -- " + e.getMessage());
            e.printStackTrace();
            h = new ConsoleHandler();
	    return h;
        }

	h = fHandler;
        return h;
    }

    /**
     * Get log file name. If a log file name is defined in configuration
     * then the method returns that value, otherwise it returns default
     * log file name.
     */
    private static String getLogFileName() {
        if (logFileName == null) {
            ServerContext sc = ApplicationServer.getServerContext();
            if (sc != null) {
                ConfigContext ctx = sc.getConfigContext();
                LogService ls = null;
                try {
                    ls = ServerBeansFactory.getConfigBean(ctx).getLogService();
                } catch (ConfigException ce) {
                    // Ignore this exception, intent is to use default file
                    // NOI18N
                    System.err.println("Error accessing log service config -- "
                            + ce.getMessage() + "\nUsing default file");
                    ce.printStackTrace();
                }
                if (ls != null) {
                    logFileName = ls.getFile();
                }
            }
        }
        if (logFileName != null) {
            return logFileName;
        } else {
            return getDefaultLogFileName();
        }
    }

    /**
     * Get default log file name. 
     */
    private static String getDefaultLogFileName() {
        if (defaultLogFileName == null) {
            ServerContext sc = ApplicationServer.getServerContext();
            String instDir;
            if (sc != null) {
                instDir = sc.getInstanceEnvironment().getInstancesRoot();
            } else {
                // FIX: Use some other system property before user.dir
                instDir = System.getProperty("user.dir");
            }
            String[] names = {instDir, LOGS_DIR, LOG_FILE};
            defaultLogFileName = StringUtils.makeFilePath(names, false);
        }
        return defaultLogFileName;
    }

    private static final String LOGS_DIR = "logs";
    private static final String LOG_FILE = "server.log";

}
