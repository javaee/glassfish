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
package com.sun.enterprise.ee.synchronization.cleaner;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigFactory;

import com.sun.enterprise.ee.synchronization.RequestMediator;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.instance.InstanceEnvironment;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;

import com.sun.enterprise.config.ConfigException;

/**
 * Cache repository cleaner main driver.  It is responsible for cleaning
 * the repository cache. The cleaner thread is a singleton and runs
 * when cookie expires. It uses a cookie to manage this state. Every 
 * un-associated application is removed by the cleaner. To avoid 
 * potential conflicts with hot deployment, the cleaner thread first
 * moves the application directories to a trash directory.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public class CacheRepositoryCleanerMain extends Thread {

    public static synchronized CacheRepositoryCleanerMain getInstance() {
        if (_instance == null) {
            _instance = new CacheRepositoryCleanerMain();
        }
        return _instance;
    }

    private CacheRepositoryCleanerMain() {
    }

    /**
     * Cache repository cleaner main. This should be called after 
     * synchronization is complete.
     *
     * @param  mReqs  request mediators
     */
    public void run(RequestMediator[] mReqs) {

        try {
            // name of this server
            String sName = System.getProperty(
                SystemPropertyConstants.SERVER_NAME);

            if (sName != null) {
                //_logger.log(Level.INFO, "synchronization.cleaner.begin");

                InstanceEnvironment env = new InstanceEnvironment(sName);
                String configPath = env.getConfigFilePath();
                ConfigContext ctx = null;

                // assumes there is a valid domain.xml 
                try {
                    ctx = ConfigFactory.createConfigContext(configPath);
                } catch (ConfigException ce) {
                    _logger.log(Level.INFO, 
                        "synchronization.cleaner.configctx_error", ce);
                }

                if (ctx != null) {
                    CacheRepositoryCleaner cleaner = 
                        new CacheRepositoryCleaner(ctx, sName, mReqs);
                    cleaner.gc();
                }

                //_logger.log(Level.INFO, "synchronization.cleaner.end");
            } else {
                _logger.log(Level.INFO, 
                    "synchronization.cleaner.servername_error");
            }
        } catch (Exception e) {
            // ignore
        }
    }

    public static void main(String[] args) {
        CacheRepositoryCleanerMain main = new CacheRepositoryCleanerMain();
        main.run();
    }

    // ---- INSTANCE VARIABLE(S) -----------------------------------------
    private static CacheRepositoryCleanerMain _instance = null;
    private static Logger _logger = 
        Logger.getLogger(EELogDomains.SYNCHRONIZATION_LOGGER);
}
