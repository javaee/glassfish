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
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.util.misc.MapUtil;
import java.util.Timer;
import java.util.Map;
import java.util.HashMap;
import java.io.File;


public class JVMMonitorTask extends MonitorTask
{
    private final String displayFormat = "%1$-25s %2$-10s %3$-10s %4$-10s %5$-10s %6$-10s";

    public JVMMonitorTask(final ServerRootMonitor srm, final String filter, final Timer timer,
                          final boolean verbose, final File fileName)
    {
        super(srm, filter, timer, verbose, fileName);
        final String jvmTitle = localStrings.getString("commands.monitor.jvm_monitoring_title");
        final String title = String.format("%1$45s", jvmTitle);
        CLILogger.getInstance().printMessage(title);
        displayHeader();
    }
    

    public void run()
    {
        if (srm == null) {
            super.cancelMonitorTask();
            return;
        }
        
        final JVMStats jvmStats = srm.getJVMMonitor().getJVMStats();
        if (jvmStats == null) {
            cancelMonitorTask();
            return;
        }
        
        if (verbose && counter == NUM_ROWS)
        {
            displayHeader();
            counter = 0;  //reset to 0
        }
        displayData(jvmStats);
        if (verbose) counter++;
    }


    private void displayData(final JVMStats jvmStats)
    {
        final BoundedRangeStatistic heapSize = jvmStats.getHeapSize();
        final CountStatistic upTime = jvmStats.getUpTime();
        
        final String data = String.format(displayFormat,
                                          upTime.getCount(),
                                          heapSize.getLowerBound(),                                          
                                          heapSize.getUpperBound(),
                                          heapSize.getLowWaterMark(),
                                          heapSize.getHighWaterMark(),
                                          heapSize.getCurrent());
        CLILogger.getInstance().printMessage(data);        
        if (fileName != null)
        {
            final String fileData = String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s",
                                                  upTime.getCount(),
                                                  heapSize.getLowerBound(),                                          
                                                  heapSize.getUpperBound(),
                                                  heapSize.getLowWaterMark(),
                                                  heapSize.getHighWaterMark(),
                                                  heapSize.getCurrent());
            writeToFile(fileData);
        }
    }

    
    private void displayHeader()
    {
        final String upTime = localStrings.getString("commands.monitor.uptime");
        final String heapSize = localStrings.getString("commands.monitor.heapsize");
        final String current = localStrings.getString("commands.monitor.current");
        final String min = localStrings.getString("commands.monitor.min");
        final String max = localStrings.getString("commands.monitor.max");
        final String low = localStrings.getString("commands.monitor.low");
        final String high = localStrings.getString("commands.monitor.high");
        final String count = localStrings.getString("commands.monitor.count");
        
        final String header = String.format("%1$-45s %2$-20s", upTime, heapSize);
        final String subHeader = String.format(displayFormat, current, min, max, low,
                                               high, count);
        CLILogger.getInstance().printMessage(header);
        CLILogger.getInstance().printMessage(subHeader);                
        if (fileName != null) {
            writeToFile(upTime+":"+current+","+heapSize+":"+min+","+heapSize+":"+max+","+
                        heapSize+":"+low+","+heapSize+":"+high+","+heapSize+":"+count);            
        }
    }

    public void displayDetails()
    {
    };
    
}
