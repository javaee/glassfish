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

import java.io.InputStream;
import java.io.IOException;


import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.logging.LogDomains;


/**
 * Class BaseLogManager serves as an abstract base class for the Application
 * Client Container and Application Server loggers. The main purpose of 
 * this class is to override the addLogger method which provides a hook 
 * to attach custom handlers and formatters and to provide a default 
 * resource bundle if none is provided. Note that providing a default resource
 * bundle somewhat alters the semantics of a Logger.getLogger(name) call
 * followed by a Logger.getLogger(name, resourcebundle) call. With the 
 * BaseHandler installed, the later call will have no effect as the call to
 * Logger.getLogger(name) implicitely defines a resource bundle.
 */
public abstract class BaseLogManager extends LogManager {

    // Due to implementation details of the LogManager, we must
    // defer initializations until static initialization of the java.util.logging 
    // classes are complete and until the configuration is read. 
    // (In the FileHandler failure to do this results in improper
    // file initialization and in the server handler, statically initialized
    // loggers will always log to the stdout). 
    private boolean _configurationRead = false;
    protected List<Logger> _unInitializedLoggers = new ArrayList<Logger>();

    // Used to log internal messages, this is essentially the first logger
    // created.
    protected static Logger _logger = null;

    protected BaseLogManager() {
        super();
    }

    // Used to fabricate a resource bundle name from the logger name. The
    // default resource bundle name is used if one is not provided.
    public String getLoggerResourceBundleName(String loggerName) {

        String result = loggerName + "." + LogDomains.RESOURCE_BUNDLE;

        return result.replaceFirst(LogDomains.DOMAIN_ROOT,
                                   LogDomains.PACKAGE_ROOT);
    }

    protected abstract void initializeLogger(Logger logger);

    protected void doInitializeLogger(Logger logger) 
    {
        String loggerName = logger.getName( );
        // We don't want to associate a Log Resource Bundle to org.apache or
        // tomcat loggers.
        // _REVISIT_: Clean this code not set any resource bundle in future,
        // because resource bundles should be associated by the modules itself
        if(! ( ( loggerName.startsWith( "org.apache" )  )
          || ( loggerName.startsWith( "com.sun.faces" ) )  
          || ( loggerName.startsWith( "tomcat" ) ) ) )
        { 
            // This is tricky. If the logger was not created with a resource 
            // bundle, we want to re-create it (to provide it with our default
            // resource bundle name); however, due to implementation details in
            // Logger.getLogger(name, resourceBundleName), we should get back 
            // the same instance
            if (logger.getResourceBundleName() == null) {
                try {
                    Logger newLogger =
                        Logger.getLogger(logger.getName(), 
                            getLoggerResourceBundleName(logger.getName()));

                    assert(logger == newLogger);
                } catch (Throwable ex) {
                    // This exception is intentionally eaten. It indicates 
                    // that the default resource bundle (specified by 
                    // getLoggerResourceBundleName) does not exist.
                    // Logger is already created.
                }
            }
        }
       

        // Finally call the real initialization
        initializeLogger(logger);
    }

    // We subclass readConfiguration to keep track of whether the configuration
    // has been read. There are multiple readConfiguration methods, but all result
    // in this one being invoked. As soon as the configuration is read, we 
    // go back an reinitialize any loggers whose initialization was deferred
    // prior to the configuration being read.
    public void readConfiguration(InputStream ins) 
        throws IOException, SecurityException 
    {
        super.readConfiguration(ins);
        synchronized (_unInitializedLoggers) {
            _configurationRead = true;
            Iterator iter = _unInitializedLoggers.iterator();
            while (iter.hasNext()) {
                Logger l = (Logger)iter.next();
                doInitializeLogger(l);
            }
            _unInitializedLoggers.clear();
        }
    }

    // We override addLogger on the security manager so that we have a
    // hook during Logger.getLogger() time where we can attach our own
    // custom handlers and formatters, etc.
    public boolean addLogger(Logger logger) {

        // The first call to initializeLogger may have a null _logger.
        boolean result = super.addLogger(logger);

        // result will be true if the logger does not exist already
        if (result) {
            try {

                // The first logger created becomes the logger that we will
                // use internally to log.
                if (_logger == null) {
                    _logger = logger;
                }

                // Defer initialization until the configuration (e.g. logging.properties) is
                // read. Once the configuration is read we go back and re-initialize any
                // loggers that were created prior to the configuration being read. 
                synchronized (_unInitializedLoggers) {
                    if (!_configurationRead) {
                        _unInitializedLoggers.add(logger);
                        return result;
                    } else {
                        doInitializeLogger(logger);
                    }
                }
            } catch (Throwable th) {
                th.printStackTrace();
                _logger.log(Level.SEVERE, "addLogger exception ", th);
            }
        }

        return result;
    }
}

