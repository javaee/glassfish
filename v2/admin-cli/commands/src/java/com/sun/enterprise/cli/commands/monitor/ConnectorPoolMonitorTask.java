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
import javax.management.MBeanServerConnection;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.appserv.management.util.misc.MapUtil;
import java.util.Map;
import java.util.HashMap;
import java.util.TimerTask;
import java.util.Timer;
import java.io.File;

public class ConnectorPoolMonitorTask extends ConnectionPoolTask
{
    public ConnectorPoolMonitorTask(final ServerRootMonitor srm, final String filter, final Timer timer,
                                    final boolean verbose, final File fileName)
        throws MonitorTaskException
    {
        super(srm, filter, timer, verbose, fileName);
        final Map<String,ConnectorConnectionPoolMonitor> connectorMap = srm.getConnectorConnectionPoolMonitorMap();

        if (connectorMap == null)
            throw new MonitorTaskException(localStrings.getString("commands.monitor.unable_to_monitor_connection_pool"));
        
        final String[] keys = MapUtil.getKeyStrings(connectorMap);
        if (this.filter == null)
        {
            checkForNumberOfElements(keys);
            this.filter = keys[0];
        }
        else {
            if (!connectorMap.containsKey(this.filter)) {
                throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist", new Object[] {this.filter}));
            }
        }
        final String connPoolMonitoring = localStrings.getString("commands.monitor.connection_pool_monitoring", new Object[] {this.filter});
        final String title = String.format("%1$50s", connPoolMonitoring);
        CLILogger.getInstance().printMessage(title);
        displayHeader();        
    }
    

    public void run()
    {
        if (srm == null) {
            super.cancelMonitorTask();
            return;
        }
        
        final Map<String,ConnectorConnectionPoolMonitor> connectorMap = srm.getConnectorConnectionPoolMonitorMap();
            //maybe lost connection?
        if (connectorMap == null || connectorMap.size()<1) {
            cancelMonitorTask();
            return;
        }
        
        if (verbose && counter == NUM_ROWS)
        {
            displayHeader();
            counter = 0;  //reset to 0
        }
        monitorConnectionPool(filter, connectorMap);
        if (verbose) counter++;
    }

    private void monitorConnectionPool(final String element,
                                       final Map<String,ConnectorConnectionPoolMonitor> connectorMap)
    {
        final ConnectorConnectionPoolMonitor connectorPoolMonitor = connectorMap.get(element);
        final ConnectorConnectionPoolStats stats = connectorPoolMonitor.getConnectorConnectionPoolStats();
        displayData(stats);
    }

}
