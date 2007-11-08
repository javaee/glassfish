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

/*
 * @(#) AutoDeployMonitor.java
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
package com.sun.enterprise.server;

import java.io.File;
import java.util.Iterator;
import com.sun.enterprise.util.io.FileUtils;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

/**
 * Monitors the auto deploy directory for new archives. 
 *
 * @atuthor Nazrul Islam
 * @since   JDK 1.4
 */
class AutoDeployMonitor extends AbstractMonitor {

    /** logger for this auto deploy monitor */
    static Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);

    /** singleton instance */
    private static AutoDeployMonitor _instance = null;

    /** filter for the archives */
    private static ArchiveFilter _archiveFilter = null;

    /** suffix added to the archives when deployed successfully */
    private static final String SUCCESS_EXT = ".deployed";

    /** suffix added to the archives when not deployed */
    private static final String ERROR_EXT = ".notdeployed";

    /**
     * Constructor - prohibits anyone from constructing this object.
     *
     * @param    pollInterval    polling interval
     */
    private AutoDeployMonitor(long pollInterval) {
        super(pollInterval);
    }

    /**
     * Returns the singleton instance. If the object was not created already, 
     * it uses the given polling interval. 
     *
     * @param    pollInterval    polling interval
     */
    static synchronized AutoDeployMonitor getInstance(long pollInterval) {
        if (_instance == null) {
            _instance       = new AutoDeployMonitor(pollInterval);
            _archiveFilter  = new ArchiveFilter();
        }
        return _instance;
    }

    /**
     * Periodically monitors the auto deploy directory of a server. 
     * If any new archive is detected, this monitor makes a callback 
     * to the listener provided in the monitorable entry.
     *
     * <p> This method checks the file extension of the archives found 
     * under the auto deploy directory. If the file extensions are .ear, 
     * .jar, .war, or .rar, it makes a callback to the listener. 
     * It is assumed that after processing the callback the handler will
     * rename the archive with proper extension. For example, for a successful
     * deployment, foo.ear may be renamed to foo.ear.deployed.
     * 
     */
    public void run() {

        try {
            synchronized (_monitoredEntries) {
                Iterator iter = _monitoredEntries.iterator();
                MonitorableEntry entry = null;

                // should be only one entry encapsulating the auto deploy dir
                while (iter.hasNext()) {
                    entry                = (MonitorableEntry) iter.next();
                    File autoDeployDir   = entry.getMonitoredFile();

                    File[] archives = autoDeployDir.listFiles(_archiveFilter);

                    if ( (archives == null) || (archives.length == 0) ) {
                        // no new archive
                        return;
                    }

                    MonitorListener l = entry.getListener();

                    for (int i=0; i<archives.length; i++) {

                        _logger.log(Level.FINE,
                            "[AutoDeployMonitor] Found " + archives[i]);

                        boolean success = l.deploy(entry, archives[i]);
                        if (success) {
                            File successExt = 
                                new File(archives[i].getParentFile(),
                                         archives[i].getName()+SUCCESS_EXT);
                            archives[i].renameTo(successExt);
                        } else {
                            File errorExt = 
                                new File(archives[i].getParentFile(),
                                         archives[i].getName()+ERROR_EXT);
                            archives[i].renameTo(errorExt);
                        }
                    }
                }
            }
        } catch (Throwable t) { 
            // catches any uncaught exceptions thrown from the handlers
            _logger.log(Level.WARNING, "core.exception", t);
        }
    }

    /** 
     * A filter for deployable archives.
     */
    private static class ArchiveFilter implements java.io.FileFilter {

        /**
         * Returns true if the given file is an ear, jar, war or rar.
         * 
         * @param    pathname    file to be tested
         * @return   true if the given file is an ear, jar, war or rar
         */
        public boolean accept(File pathname) {

            if ( FileUtils.isEar(pathname) 
                    || FileUtils.isJar(pathname)
                    || FileUtils.isWar(pathname)
                    || FileUtils.isRar(pathname) ) {
                return true;
            } else {
                return false;
            }
        }
    }
}
