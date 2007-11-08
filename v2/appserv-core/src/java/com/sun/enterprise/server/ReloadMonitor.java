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

package com.sun.enterprise.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import com.sun.enterprise.admin.server.core.channel.ReconfigHelper;

/**
 * Monitors dynamic reload files for applications and stand alone modules 
 * from the server runtime. 
 *
 * @atuthor Nazrul Islam
 * @since   JDK 1.4
 */
class ReloadMonitor extends AbstractMonitor {

    /** name of the file that user will update time stamp to trigger a reload */
    static final String RELOAD_FILE = ".reload";
    
    static final Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);

    /** singleton instance */
    private static ReloadMonitor _instance = null;

    /**
     * Constructor - prohibits anyone from constructing this object.
     *
     * @param    pollInterval    polling interval
     */
    private ReloadMonitor(long pollInterval) {
        super(pollInterval);
    }

    /**
     * Returns the singleton instance. If the object was not created already, 
     * it uses the given polling interval. 
     *
     * @param    pollInterval    polling interval
     */
    static synchronized ReloadMonitor getInstance(long pollInterval) {
        if (_instance == null) {
            _instance = new ReloadMonitor(pollInterval);
        }
        return _instance;
    }

    /** 
     * Removes the given application or stand alone module from the 
     * monitored list.
     *
     * @param    id    registration name of application
     *
     * @return   true if removed successfully
     */
    boolean removeMonitoredEntry(String id) {

        boolean removed = false;

        // returns false if application name is null
        if (id == null) {
            return removed;
        }

        synchronized (this._monitoredEntries) {
            Iterator iter = this._monitoredEntries.iterator();
            while (iter.hasNext()) {
                MonitorableEntry entry = (MonitorableEntry) iter.next();
                if ( id.equals(entry.getId()) ) {
                    this._monitoredEntries.remove(entry);
                    removed = true;
                    break;
                }
            }
        }

        return removed;
    }

    /**
     * This method gets called from the monitor thread. This goes through 
     * all the monitored entried and checks the time stamps. If any of the 
     * time stamps is modified, it makes a callback to its listener. The 
     * callbacks are single threaded, i.e., waits for one to finish before 
     * makes the second call.
     *
     * <p> The time stamp of the monitored entry is set to the current 
     * time stamp before the callback is made. 
     */
    public void run() {

        try {
            ArrayList reloadList = new ArrayList();

            synchronized (_monitoredEntries) {
                Iterator iter = _monitoredEntries.iterator();
                MonitorableEntry entry = null;

                while (iter.hasNext()) {
                    entry                = (MonitorableEntry) iter.next();
                    File file            = entry.getMonitoredFile();
                    long lastModified    = file.lastModified();
                    long lastReloadedAt  = entry.getLastReloadedTimeStamp();

                    // time stamp is updated
                    if (lastModified > lastReloadedAt) {
                        // sets the time stamp so that it gets called once
                        entry.setLastReloadedTimeStamp(lastModified);

                        reloadList.add(entry);
                    }
                }
            }
            // found some entries with modified time stamp
            if (reloadList.size() > 0) {

                _logger.log(Level.FINEST,
                    "[ReloadMonitor] Monitor detected reloadable entry!");

                int size = reloadList.size();
                MonitorableEntry entry = null;

                for (int i=0; i<size; i++) {
                    entry = (MonitorableEntry) reloadList.get(i);

                    MonitorListener l = entry.getListener();

                    // calls back the listener
                    boolean success = l.reload(entry);

                    // log status
                    if (success) {
                        _logger.log(Level.INFO,
                            "core.application_reload_successful",
                            entry.getDisplayName());
                    } else {
                        _logger.log(Level.INFO,
                            "core.application_reload_failed",
                            entry.getDisplayName());
                    }
                }

                // Reload the web modules
                /*
                 * Remove compile time dependence on J2EERunner. Replace it
                 * temporarily with call to ReconfigHelper.
                J2EERunner.requestReconfiguration();
                */
                ReconfigHelper.sendReconfigMessage("");

                // removes all the entries after the call back
                reloadList.clear();
            }

        } catch (Throwable t) { 
            // catches any uncaught exceptions thrown from the handlers
            _logger.log(Level.WARNING, "core.exception", t);
        }
    }
}