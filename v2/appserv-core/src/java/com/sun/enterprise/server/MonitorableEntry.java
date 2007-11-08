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
 * @(#) MonitorableEntry.java
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

/**
 * Represents a monitor entry in dynamic reloading. 
 *
 * @author Nazrul Islam
 * @since  JDK 1.4
 */
final class MonitorableEntry {

    /**
     * Constructor.
     *
     * @param   id    registered name of the application or stand alone module
     * @param   displayName    the user-friendly display name
     * @param   monitoredFile  file to be monitored
     * @param   listener       listener that handles the callback
     */
    MonitorableEntry(String id, String displayName, File monitoredFile,
                     MonitorListener listener) {

        this.id              = id;
        this.displayName     = displayName;
        this.monitoredFile   = monitoredFile;
        this.lastReloadedAt  = monitoredFile.lastModified();
        this.listener        = listener;
    }

    /**
     * Constructor.
     *
     * @param   id    registered name of the application or stand alone module
     * @param   monitoredFile  file to be monitored
     * @param   listener       listener that handles the callback
     */
    MonitorableEntry(String id, File monitoredFile, MonitorListener listener) {

        this(id, id, monitoredFile, listener);
    }

    /**
     * Constructor. Use this when "id" is not necessary.
     *
     * @param   monitoredFile  file to be monitored
     * @param   listener       listener that handles the callback
     */
    MonitorableEntry(File monitoredFile, MonitorListener listener) {

        this("AutoDeployDirectory", monitoredFile, listener);
    }

    /**
     * Returns the id of the entry.
     *
     * @return    the id of the entry
     */
    String getId() {
        return this.id;
    }

    /**
     * Returns the display name of the entry (usually this is the same as
     * the id).
     *
     * @return    the display name of the entry
     */
    String getDisplayName() {
        return this.displayName;
    }

    /**
     * Returns the last attempted reload time stamp of the monitored entry.
     *
     * @return    last attempted reload time stamp of the monitored entry
     */
    long getLastReloadedTimeStamp() {
        return this.lastReloadedAt;
    }

    /** 
     * Returns the listener for this entry
     *
     * @return    the listener for this entry
     */
    MonitorListener getListener() {
        return this.listener;
    }

    /**
     * Returns the monitored file for this entry.
     *
     * @return    the monitored file for this entry
     */
    File getMonitoredFile() {
        return this.monitoredFile;
    }

    /**
     * Sets the time stamp of last reload attempt.
     *
     * @param    ts    new time stamp
     */
    void setLastReloadedTimeStamp(long ts) {
        this.lastReloadedAt = ts;
    }
            
    /**
     * Returns a hash code for this object.
     *
     * @return    hash code for this object
     */
    public int hashCode() {
        return id.hashCode();
    }
            
    /**
     * Indicates whether the given object is equal to this one. 
     * 
     * @return    true if the given object is equal
     */
    public boolean equals(Object other) {
        return monitoredFile.equals(((MonitorableEntry) other).monitoredFile);
    }

    // ---- INSTANCE VARIABLE(S) - PRIVATE --------------------------------
    private String          id;
    private String          displayName;
    private File            monitoredFile;
    private long            lastReloadedAt;
    private MonitorListener	listener;
}
