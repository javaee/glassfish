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

package com.sun.enterprise.web.stats;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.Statistic;
import com.sun.logging.LogDomains;
import com.sun.enterprise.web.VirtualServer;
import com.sun.enterprise.admin.monitor.stats.StringStatistic;
import com.sun.enterprise.admin.monitor.stats.StringStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.PWCVirtualServerStats;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;

/** 
 * Class representing Virtual Server stats in PE.
 */
public class PWCVirtualServerStatsImpl implements PWCVirtualServerStats {

    private static final Logger _logger = LogDomains.getLogger(
                                                    LogDomains.WEB_LOGGER);

    private long startTime;
    private GenericStatsImpl baseStatsImpl;
    private StringStatistic idStats;
    private StringStatistic modeStats;
    private StringStatistic hostsStats;
    private StringStatistic interfacesStats;

    /*
     * Constructor.
     */
    public PWCVirtualServerStatsImpl(VirtualServer vs) {

        initializeStatistics(vs);

        baseStatsImpl = new GenericStatsImpl(
            com.sun.enterprise.admin.monitor.stats.PWCVirtualServerStats.class,
            this);
    }

    /** 
     * Returns the virtual server ID.
     *
     * @return Virtual server ID
     */    
    public StringStatistic getId() {
        return idStats;
    }

    /** 
     * Returns the virtual server mode.
     *
     * @return Virtual server mode
     */    
    public StringStatistic getMode() {
        return modeStats;
    }

    /** 
     * Returns the host names of this virtual server
     *
     * @return Host names of this virtual server
     */    
    public StringStatistic getHosts() {
        return hostsStats;
    }
    
    /** 
     * Returns the interfaces of this virtual server
     *
     * @return Interfaces of this virtual server
     */    
    public StringStatistic getInterfaces() {
        return interfacesStats;
    }

    public Statistic[] getStatistics() {
        return baseStatsImpl.getStatistics();
    }
    
    public String[] getStatisticNames() {
        return baseStatsImpl.getStatisticNames();
    }

    public Statistic getStatistic(String str) {
        return baseStatsImpl.getStatistic(str);
    }

    /**
     * Initializes the stats from the given virtual server
     *
     * @param vs Virtual server from which to derive stats
     */
    private void initializeStatistics(VirtualServer vs) {

        startTime = System.currentTimeMillis();

        // ID
        idStats = new StringStatisticImpl(
                                vs.getID(),
                                "Id",
                                "String",
                                "Virtual Server ID",
                                startTime,
                                startTime);

        // Mode
        modeStats = new StringStatisticImpl(
                                vs.isActive() ? "active" : "unknown",
                                "Mode",
                                "unknown/active",
                                "Virtual Server mode",
                                startTime,
                                startTime);

        // Hosts
        String hosts = null;
        String[] aliases = vs.findAliases();
        if (aliases != null) {
            for (int i=0; i<aliases.length; i++) {
                if (hosts == null) {
                    hosts = aliases[i];
                } else {
                    hosts += ", " + aliases[i];
                }
            }
        }
        hostsStats = new StringStatisticImpl(
                                hosts,
                                "Hosts",
                                "String",
                                "The software virtual hostnames serviced by "
                                + "this Virtual Server",
                                startTime,
                                startTime);

        // Interfaces
        interfacesStats = new StringStatisticImpl(
                                "0.0.0.0", // XXX FIX
                                "Interfaces",
                                "String",
                                "The interfaces for which this Virtual Server "
                                + "has been configured",
                                startTime,
                                startTime);
    }


}
