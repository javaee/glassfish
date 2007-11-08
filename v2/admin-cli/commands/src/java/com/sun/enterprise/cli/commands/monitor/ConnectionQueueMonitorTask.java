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

public class ConnectionQueueMonitorTask extends MonitorTask
{
    private final String displayFormat = "%1$-5s %2$-5s %3$-5s %4$-5s %5$-5s %6$-5s %7$-5s "+
                                         "%8$-5s %9$-5s %10$-5s";

    public ConnectionQueueMonitorTask(final ServerRootMonitor srm, final String filter, final Timer timer,
                                      final boolean verbose, final File fileName)
        throws MonitorTaskException
    {
        super(srm, filter, timer, verbose, fileName);
        final String connQueueMonitoring = localStrings.getString("commands.monitor.connection_queue_monitoring_title");
        final String title = String.format("%1$42s", connQueueMonitoring);
        CLILogger.getInstance().printMessage(title);
        displayHeader();
    }
    

    public void run()
    {
        if (srm == null) {
            super.cancelMonitorTask();
            return;
        }
        

        final HTTPServiceMonitor httpserviceMonitor = srm.getHTTPServiceMonitor();
        if (httpserviceMonitor == null) {
            cancelMonitorTask();
            return;
        }
        
            
        final ConnectionQueueMonitor connectionQueueMonitor = httpserviceMonitor.getConnectionQueueMonitor();
        if (connectionQueueMonitor == null) {
            cancelMonitorTask();
            return;
        }
        
        final ConnectionQueueStats connectionQueueStats = connectionQueueMonitor.getConnectionQueueStats();
        
        if (verbose && counter == NUM_ROWS)
        {
            displayHeader();
            counter = 0;  //reset to 0
        }
        displayData(connectionQueueStats);
        if (verbose) counter++;
    }

    
    private void displayHeader()
    {
        final String of = localStrings.getString("commands.monitor.of");
        final String que = localStrings.getString("commands.monitor.que");
        final String fifteen_a = localStrings.getString("commands.monitor.15a");
        final String one_a = localStrings.getString("commands.monitor.1a");
        final String five_a = localStrings.getString("commands.monitor.5a");
        final String tcon = localStrings.getString("commands.monitor.tcon");
        final String tque = localStrings.getString("commands.monitor.tque");
        final String id = localStrings.getString("commands.monitor.id");
        final String mque = localStrings.getString("commands.monitor.mque");
        final String pque = localStrings.getString("commands.monitor.pque");        
        
        final String header = String.format(displayFormat,
                                            of,que,fifteen_a,one_a,five_a,tcon,tque,id,
                                            mque,pque);
        
        CLILogger.getInstance().printMessage(header);
        if (fileName != null)
        {
            writeToFile(localStrings.getString("commands.monitor.connection_queue_write_to_file"));
        }
    }

    
    private void displayData(final ConnectionQueueStats cqs)
    {
        final String data = String.format(displayFormat,
                                          cqs.getCountOverflows().getCount(),
                                          cqs.getCountQueued().getCount(),
                                          cqs.getCountQueued15MinuteAverage().getCount(),
                                          cqs.getCountQueued1MinuteAverage().getCount(),
                                          cqs.getCountQueued5MinuteAverage().getCount(),
                                          cqs.getCountTotalConnections().getCount(),
                                          cqs.getCountTotalQueued().getCount(),
                                          cqs.getID().getCurrent(),
                                          cqs.getMaxQueued().getCount(),
                                          cqs.getPeakQueued().getCount());
                
        CLILogger.getInstance().printMessage(data);
        if (fileName != null)
        {
            final String fileData = String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s,"+
                                              "%8$s,%9$s,%10$s",
                                              cqs.getCountOverflows().getCount(),
                                              cqs.getCountQueued().getCount(),
                                              cqs.getCountQueued15MinuteAverage().getCount(),
                                              cqs.getCountQueued1MinuteAverage().getCount(),
                                              cqs.getCountQueued5MinuteAverage().getCount(),
                                              cqs.getCountTotalConnections().getCount(),
                                              cqs.getCountTotalQueued().getCount(),
                                              cqs.getID().getCurrent(),
                                              cqs.getMaxQueued().getCount(),
                                              cqs.getPeakQueued().getCount());
            writeToFile(fileData);
        }
    }

    
    public void displayDetails()
    {
        final String details = localStrings.getString("commands.monitor.connection_queue_detail");
        CLILogger.getInstance().printMessage(details);
    }


}
    
    

