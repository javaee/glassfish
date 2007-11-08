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

package com.sun.enterprise.cli.commands.monitor;

import com.sun.enterprise.cli.framework.*;
import com.sun.appserv.management.monitor.*;
import com.sun.appserv.management.monitor.statistics.*;
import javax.management.j2ee.statistics.*;
import com.sun.appserv.management.j2ee.statistics.*;
import com.sun.appserv.management.util.misc.MapUtil;
import java.util.Map;
import java.util.Timer;
import java.io.File;

public class ConnectionManagerMonitorTask extends MonitorTask
{
    private final String displayFormat = "%1$-5s %2$-5s %3$-5s %4$-20s %5$-5s %6$-5s %7$-5s";

    public ConnectionManagerMonitorTask(final ServerRootMonitor srm, final String filter,
                                        final Timer timer, final boolean verbose, final File fileName)
        throws MonitorTaskException
    {
        super(srm, filter, timer, verbose, fileName);
        
        final Map<String,ConnectionManagerMonitor> connectionManagerMap  = srm.getConnectionManagerMonitorMap();
        
        if (connectionManagerMap == null)
            throw new MonitorTaskException(localStrings.getString("commands.monitor.unable_to_monitor_connection_manager"));
        
        final String[] keys = MapUtil.getKeyStrings(connectionManagerMap);
        if (this.filter == null)
        {
            checkForNumberOfElements(keys);
            this.filter = keys[0];
        }
        else {
            if (!connectionManagerMap.containsKey(this.filter)) {
                throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist", new Object[] {this.filter}));
            }
        }
        final String connManagerMonitoring = localStrings.getString("commands.monitor.connection_manager_monitoring_title", new Object[] {this.filter});
        final String title = String.format("%1$5s", connManagerMonitoring);
        CLILogger.getInstance().printMessage(title);
        displayHeader();        
    }
    

    public void run()
    {
        if (srm == null)
        {
            super.cancelMonitorTask();
            return;
        }
        
        final Map<String,ConnectionManagerMonitor> connectionManagerMap  = srm.getConnectionManagerMonitorMap();
        if (connectionManagerMap == null || connectionManagerMap.size()<0)
        {
            super.cancelMonitorTask();
            return;
        }
        
            
        final ConnectionManagerMonitor connectionManagerMonitor = connectionManagerMap.get(filter);
        if (connectionManagerMonitor == null)
        {
            cancelMonitorTask();
            return;
        }
            
        final ConnectionManagerStats connectionManagerStats = connectionManagerMonitor.getConnectionManagerStats();

        if (verbose && counter == NUM_ROWS)
        {
            displayHeader();
            counter = 0;  //reset to 0
        }
        displayData(connectionManagerStats);
        if (verbose) counter++;
    }

    
    private void displayHeader()
    {
        final String totalConn = localStrings.getString("commands.monitor.total_connections");
        final String ci = localStrings.getString("commands.monitor.ci");
        final String ciu = localStrings.getString("commands.monitor.ciu");
        final String low = localStrings.getString("commands.monitor.low");
        final String hi = localStrings.getString("commands.monitor.hi");
        final String cur = localStrings.getString("commands.monitor.cur");
        final String min = localStrings.getString("commands.monitor.min");
        final String max = localStrings.getString("commands.monitor.max");
        
        final String header = String.format("%1$35s", totalConn);
        final String subheader = String.format(displayFormat, ci, ciu, low, hi, cur, min, max);
        
        CLILogger.getInstance().printMessage(header);
        CLILogger.getInstance().printMessage(subheader);
        if (fileName != null)
        {
            writeToFile(localStrings.getString("commands.monitor.connection_manager_write_to_file"));
        }
    }

    
    private void displayData(final ConnectionManagerStats cms)
    {
        final String data = String.format(displayFormat,
                                          cms.getConnectionsIdle().getCount(),
                                          cms.getConnectionsInUse().getCount(),
                                          cms.getTotalConnections().getLowerBound(),
                                          cms.getTotalConnections().getUpperBound(),
                                          cms.getTotalConnections().getLowWaterMark(),
                                          cms.getTotalConnections().getHighWaterMark(),
                                          cms.getTotalConnections().getCurrent());
        CLILogger.getInstance().printMessage(data);

        if (fileName != null)
        {
            final String fileData = String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s",
                                          cms.getConnectionsIdle().getCount(),
                                          cms.getConnectionsInUse().getCount(),
                                          cms.getTotalConnections().getLowerBound(),
                                          cms.getTotalConnections().getUpperBound(),
                                          cms.getTotalConnections().getLowWaterMark(),
                                          cms.getTotalConnections().getHighWaterMark(),                                          
                                          cms.getTotalConnections().getCurrent());
            writeToFile(fileData);
        }
    }

    
    public void displayDetails()
    {
        final String details = localStrings.getString("commands.monitor.connection_manager_details");
        CLILogger.getInstance().printMessage(details);
    }


}
    
    

