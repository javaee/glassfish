
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



import java.io.IOException;
import java.io.File;

import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.clientbeans.ClientContainer;
import com.sun.enterprise.config.clientbeans.ClientBeansFactory;

import com.sun.logging.LogDomains;

/**
 * Class ACCLogManager is intended for use in the Application Client Container.
 * The sun-acc.xml file holds ACC configuration as well as logging 
 * configuration including log file name and log level. The FileHandler is 
 * installed as the only logging handler.
 */
public class ACCLogManager extends BaseLogManager {

    // property name for configuring name and location of ACC 
    // configuration file.
    private final String CLIENT_XML_FULL_NAME         =
        "com.sun.enterprise.appclient.ClientContainer";
    private final String DEFAULT_CLIENT_CONTAINER_XML = "sun-acc.xml";

    private Handler _clientHandler = null;
    private Level _logLevel = Level.INFO;

    private boolean initialized = false;

    public ACCLogManager() {
        super();
    }

    // This method is called when appclient container
    // figures out the location of sun-acc.xml
    synchronized public void init(String configFile) {

        if (initialized) return;
        initialized = true;

        try {
            ConfigContext ctx =
                ConfigFactory.createConfigContext
                (configFile, true, false, false,
                 com.sun.enterprise.config.clientbeans.ClientContainer.class,
		 new com.sun.enterprise.config.clientbeans.ClientBeansResolver());
            final ClientContainer cc  =
                ClientBeansFactory.getClientBean(ctx);

            String logLevel = cc.getLogService().getLevel();
            if (logLevel != null && !logLevel.equals("")) {
                _logLevel = Level.parse(logLevel);
            }
            
            String logFileName = cc.getLogService().getFile();
            if (logFileName != null && !logFileName.equals("")) {
                _clientHandler = new FileHandler(logFileName, true);
                _clientHandler.setFormatter(new SimpleFormatter());
                
                // workaround to delete lockfile upon exit
                File lockFile = new File(logFileName + ".lck");
                lockFile.deleteOnExit();
            }
            
        } catch (Exception ex) {
            if (_logger != null) {
                _logger.logrb(Level.SEVERE, null, null,
                              "com.sun.logging.enterprise.system.container.appclient.LogStrings",
                              "acc.cannot_create_log_handler", ex);
            }
        }

        // Previously registered loggers need to be
        // modified to use the right handler and set the
        // the right level.
        Enumeration e = getLoggerNames();
        while (e.hasMoreElements()) {
            String loggerName = (String) e.nextElement();
            Logger l = getLogger(loggerName);
            initializeLogger(l);
        }
    }


    synchronized protected void initializeLogger(final Logger l) {
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {

                    l.setLevel(_logLevel);

                    // only if using file handler
                    if (_clientHandler != null) {
                        // Explicitely remove all handlers. 
                        Handler[] h = l.getHandlers();
                        for (int i = 0; i < h.length; i++) {
                            l.removeHandler(h[i]);
                        }
                        
                        l.setUseParentHandlers(false);
                        
                        // Install handler and formatter. All other handlers are removed 
                        // intentionally. In theory setUseParentHandlers(false) should do
                        // the same thing, but there are subtle differences (again related 
                        // to static init) that was causing duplicate output in the log.
                        l.addHandler(_clientHandler);
                    }
                    return null;
                }
            }
        );
    }
}
