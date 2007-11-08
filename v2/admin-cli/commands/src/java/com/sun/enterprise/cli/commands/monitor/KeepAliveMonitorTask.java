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

public class KeepAliveMonitorTask extends MonitorTask
{
    private final String displayFormat = "%1$-8s %2$-8s %3$-8s %4$-8s %5$-8s %6$-8s %7$-8s";

    public KeepAliveMonitorTask(final ServerRootMonitor srm, final String filter, final Timer timer,
                                final boolean verbose, final File fileName)
        throws MonitorTaskException
    {
        super(srm, filter, timer, verbose, fileName);
        final String keepAliveTitle=localStrings.getString("commands.monitor.keep_alive_monitoring");
        final String title = String.format("%1$40s", keepAliveTitle);
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
        

        final KeepAliveMonitor keepAliveMonitor = httpserviceMonitor.getKeepAliveMonitor();
        if (keepAliveMonitor == null) {
            cancelMonitorTask();
            return;
        }
        
        final KeepAliveStats keepAliveStats = keepAliveMonitor.getKeepAliveStats();

        if (verbose && counter == NUM_ROWS)
        {
            displayHeader();
            counter = 0;  //reset to 0
        }
        displayData(keepAliveStats);
        if (verbose) counter++;
    }

    
    private void displayHeader()
    {
        final String cc = localStrings.getString("commands.monitor.cc");
        final String cf = localStrings.getString("commands.monitor.cf");
        final String ch = localStrings.getString("commands.monitor.ch");
        final String cr = localStrings.getString("commands.monitor.cr");
        final String cto = localStrings.getString("commands.monitor.cto");
        final String mc = localStrings.getString("commands.monitor.mc");
        final String sto = localStrings.getString("commands.monitor.sto");
        
        final String header = String.format(displayFormat, cc,cf,ch,cr,cto,mc,sto);
        CLILogger.getInstance().printMessage(header);
        if (fileName != null)
        {
            writeToFile(localStrings.getString("commands.monitor.keep_alive_write_to_file"));
        }
    }

    
    private void displayData(final KeepAliveStats kas)
    {
        final String data = String.format(displayFormat,
                                          kas.getCountConnections().getCount(),
                                          kas.getCountFlushes().getCount(),
                                          kas.getCountHits().getCount(),
                                          kas.getCountRefusals().getCount(),
                                          kas.getCountTimeouts().getCount(),
                                          kas.getMaxConnections().getCount(),
                                          kas.getSecondsTimeouts().getCount());
        CLILogger.getInstance().printMessage(data);
        if (fileName != null)
        {
            final String fileData = String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s",
                                                  kas.getCountConnections().getCount(),
                                                  kas.getCountFlushes().getCount(),
                                                  kas.getCountHits().getCount(),
                                                  kas.getCountRefusals().getCount(),
                                                  kas.getCountTimeouts().getCount(),
                                                  kas.getMaxConnections().getCount(),
                                                  kas.getSecondsTimeouts().getCount());
            writeToFile(fileData);
        }
    }

    
    public void displayDetails()
    {
        final String details = localStrings.getString("commands.monitor.keep_alive_detail");
        CLILogger.getInstance().printMessage(details);
    }


}
    
    

