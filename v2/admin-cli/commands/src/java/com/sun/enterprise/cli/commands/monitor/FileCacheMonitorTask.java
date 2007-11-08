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
import com.sun.appserv.management.util.misc.MapUtil;
import java.util.Map;
import java.util.HashMap;
import java.util.TimerTask;
import java.util.Timer;
import java.io.File;

public class FileCacheMonitorTask extends MonitorTask
{
    private final String displayFormat = "%1$-5s %2$-5s %3$-5s %4$-5s %5$-5s %6$-5s "+
                                         "%7$-5s %8$-5s %9$-2s %10$-5s %11$-5s %12$-5s %13$-5s "+
                                         "%14$-5s %15$-5s %16$-5s";

    public FileCacheMonitorTask(final ServerRootMonitor srm, final String filter, final Timer timer,
                                final boolean verbose, final File fileName)
        throws MonitorTaskException
    {
        super(srm, filter, timer, verbose, fileName);
        final String fileCacheTitle = localStrings.getString("commands.monitor.file_cache_monitoring_title");
        final String title = String.format("%1$55s", fileCacheTitle);
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
        

        final FileCacheMonitor fileCacheMonitor = httpserviceMonitor.getFileCacheMonitor();
        if (fileCacheMonitor == null) {
            cancelMonitorTask();
            return;
        }
        
        
        final FileCacheStats fileCacheStats = fileCacheMonitor.getFileCacheStats();
        
        if (verbose && counter == NUM_ROWS)
        {
            displayHeader();
            counter = 0;  //reset to 0
        }
        displayData(fileCacheStats);
        if (verbose) counter++;
    }

    
    private void displayHeader()
    {
        final String cch = localStrings.getString("commands.monitor.cch");
        final String ccm = localStrings.getString("commands.monitor.ccm");
        final String ce = localStrings.getString("commands.monitor.ce");
        final String ch = localStrings.getString("commands.monitor.ch");
        final String cih = localStrings.getString("commands.monitor.cih");
        final String cim = localStrings.getString("commands.monitor.cim");
        final String cm = localStrings.getString("commands.monitor.cm");
        final String coe = localStrings.getString("commands.monitor.coe");
        final String fe = localStrings.getString("commands.monitor.fe");
        final String me = localStrings.getString("commands.monitor.me");
        final String mhcs = localStrings.getString("commands.monitor.mhcs");
        final String mmcs = localStrings.getString("commands.monitor.mmcs");
        final String moe = localStrings.getString("commands.monitor.moe");
        final String sma = localStrings.getString("commands.monitor.sma");
        final String shc = localStrings.getString("commands.monitor.shc");
        final String smc = localStrings.getString("commands.monitor.smc");
        
        final String header = String.format(displayFormat,
                                            cch,ccm,ce,ch,cih,cim,cm,coe,fe,me,mhcs,
                                            mmcs,moe,sma,shc,smc);
        CLILogger.getInstance().printMessage(header);
        if (fileName != null)
        {
            writeToFile(localStrings.getString("commands.monitor.file_cache_write_to_file"));
        }
    }

    
    private void displayData(final FileCacheStats fcs)
    {
        final String data = String.format(displayFormat,
                                          fcs.getCountContentHits().getCount(),
                                          fcs.getCountContentMisses().getCount(),
                                          fcs.getCountEntries().getCount(),
                                          fcs.getCountHits().getCount(),
                                          fcs.getCountInfoHits().getCount(),
                                          fcs.getCountInfoMisses().getCount(),
                                          fcs.getCountMisses().getCount(),
                                          fcs.getCountOpenEntries().getCount(),
                                          fcs.getFlagEnabled().getCount(),
                                          fcs.getMaxEntries().getCount(),
                                          fcs.getMaxHeapCacheSize().getCount(),
                                          fcs.getMaxMmapCacheSize().getCount(),
                                          fcs.getMaxOpenEntries().getCount(),
                                          fcs.getSecondsMaxAge().getCount(),
                                          fcs.getSizeHeapCache().getCount(),
                                          fcs.getSizeMmapCache().getCount());
        CLILogger.getInstance().printMessage(data);
        if (fileName != null)
        {
            final String fileData = String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s,%8$s,%9$s,"+
                                                  "%10$s,%11$s,%12$s,%13$s,%14$s,%15$s,%16$s",
                                                  fcs.getCountContentHits().getCount(),
                                                  fcs.getCountContentMisses().getCount(),
                                                  fcs.getCountEntries().getCount(),
                                                  fcs.getCountHits().getCount(),
                                                  fcs.getCountInfoHits().getCount(),
                                                  fcs.getCountInfoMisses().getCount(),
                                                  fcs.getCountMisses().getCount(),
                                                  fcs.getCountOpenEntries().getCount(),
                                                  fcs.getFlagEnabled().getCount(),
                                                  fcs.getMaxEntries().getCount(),
                                                  fcs.getMaxHeapCacheSize().getCount(),
                                                  fcs.getMaxMmapCacheSize().getCount(),
                                                  fcs.getMaxOpenEntries().getCount(),
                                                  fcs.getSecondsMaxAge().getCount(),
                                                  fcs.getSizeHeapCache().getCount(),
                                                  fcs.getSizeMmapCache().getCount());
            writeToFile(fileData);
        }
    }

    
    public void displayDetails()
    {
        final String details = localStrings.getString("commands.monitor.file_cache_detail");
        CLILogger.getInstance().printMessage(details);
    }


}
    
    

