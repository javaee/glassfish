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
import java.util.Timer;
import java.io.File;

public class HttpListenerMonitorTask extends MonitorTask
{
    private final String displayFormat = "%1$-4s %2$-4s %3$-4s %4$-4s %5$-4s %6$-4s "+
                                         "%7$-4s %8$-4s %9$-4s %10$-4s %11$-4s %12$-4s %13$-4s "+
                                         "%14$-4s %15$-4s %16$-4s %17$-4s %18$-4s %19$-4s "+
                                         "%20$-4s %21$-4s %22$-4s %23$-4s %24$-4s %25$-4s %26$-4s";

    public HttpListenerMonitorTask(final ServerRootMonitor srm, final String filter, final Timer timer,
                                   final boolean verbose, final File fileName)
        throws MonitorTaskException
    {
        super(srm, filter, timer, verbose, fileName);
        final HTTPServiceMonitor httpserviceMonitor = srm.getHTTPServiceMonitor();
        if (httpserviceMonitor == null)
            throw new MonitorTaskException(localStrings.getString("commands.monitor.unable_to_monitor_httplistener"));
        
        final Map<String,HTTPListenerMonitor> httpListenerMap = httpserviceMonitor.getHTTPListenerMonitorMap();
        if (httpListenerMap == null)
            throw new MonitorTaskException(localStrings.getString("commands.monitor.unable_to_monitor_httplistener"));
        
        final String[] keys = MapUtil.getKeyStrings(httpListenerMap);
        if (this.filter == null)
        {
            checkForNumberOfElements(keys);
            this.filter = keys[0];
        }
        else {
            if (!httpListenerMap.containsKey(this.filter)) {
                throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist", new Object[] {this.filter}));
            }
        }
        final String httpListenerTitle=localStrings.getString("commands.monitor.httplistener_monitoring_title", new Object[] {this.filter});
        final String title = String.format("%1$60s", httpListenerTitle);
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
        

        final Map<String,HTTPListenerMonitor> httpListenerMap = httpserviceMonitor.getHTTPListenerMonitorMap();
        if (httpListenerMap == null || httpListenerMap.size()<1) {
            cancelMonitorTask();
            return;
        }
        
        final HTTPListenerMonitor httpListenerMonitor = httpListenerMap.get(filter);
        if (httpListenerMonitor == null) {
            cancelMonitorTask();
            return;
        }
        
        final HTTPListenerStats httpListenerStats = httpListenerMonitor.getHTTPListenerStats();

        if (verbose && counter == NUM_ROWS)
        {
            displayHeader();
            counter = 0;  //reset to 0
        }
        displayData(httpListenerStats);
        if (verbose) counter++;
    }

    
    private void displayHeader()
    {
        final String br = localStrings.getString("commands.monitor.br");
        final String bs = localStrings.getString("commands.monitor.bs");
        final String c200 = localStrings.getString("commands.monitor.c200");
        final String c2xx = localStrings.getString("commands.monitor.c2xx");
        final String c302 = localStrings.getString("commands.monitor.c302");
        final String c304 = localStrings.getString("commands.monitor.c304");
        final String c3xx = localStrings.getString("commands.monitor.c3xx");
        final String c400 = localStrings.getString("commands.monitor.c400");
        final String c401 = localStrings.getString("commands.monitor.c401");
        final String c403 = localStrings.getString("commands.monitor.c403");
        final String c404 = localStrings.getString("commands.monitor.c404");
        final String c4xx = localStrings.getString("commands.monitor.c4xx");
        final String c503 = localStrings.getString("commands.monitor.c503");
        final String c5xx = localStrings.getString("commands.monitor.c5xx");
        final String coc = localStrings.getString("commands.monitor.coc");
        final String co = localStrings.getString("commands.monitor.co");
        final String ctc = localStrings.getString("commands.monitor.ctc");
        final String ctb = localStrings.getString("commands.monitor.ctb");
        final String ec = localStrings.getString("commands.monitor.ec");
        final String moc = localStrings.getString("commands.monitor.moc");
        final String mst = localStrings.getString("commands.monitor.mst");
        final String mt = localStrings.getString("commands.monitor.mt");
        final String mtm = localStrings.getString("commands.monitor.mtm");
        final String pt = localStrings.getString("commands.monitor.pt");
        final String rc = localStrings.getString("commands.monitor.rc");
        
        final String header = String.format(displayFormat,
                                            br,bs,c200,c2xx,c302,c304,c3xx,c400,c401,c403,
                                            c404,c4xx,c503,c5xx,coc,co,ctc,ctb,ec,moc,mst,
                                            mt,mtm,mst,pt,rc);
        CLILogger.getInstance().printMessage(header);
        
        if (fileName != null)
        {
            writeToFile(localStrings.getString("commands.monitor.httplistener_write_to_file"));
        }
    }

    
    private void displayData(final HTTPListenerStats hls)
    {
        final String data = String.format(displayFormat,
                                          hls.getBytesReceived().getCount(),
                                          hls.getBytesSent().getCount(),
                                          hls.getCount200().getCount(),
                                          hls.getCount2xx().getCount(),
                                          hls.getCount302().getCount(),
                                          hls.getCount304().getCount(),
                                          hls.getCount3xx().getCount(),
                                          hls.getCount400().getCount(),
                                          hls.getCount401().getCount(),
                                          hls.getCount403().getCount(),
                                          hls.getCount404().getCount(),
                                          hls.getCount4xx().getCount(),
                                          hls.getCount503().getCount(),
                                          hls.getCount5xx().getCount(),
                                          hls.getCountOpenConnections().getCount(),
                                          hls.getCountOther().getCount(),
                                          hls.getCurrentThreadCount().getCount(),
                                          hls.getCurrentThreadsBusy().getCount(),
                                          hls.getErrorCount().getCount(),
                                          hls.getMaxOpenConnections().getCount(),
                                          hls.getMaxSpareThreads().getCount(),
                                          hls.getMaxThreads().getCount(),
                                          hls.getMaxTime().getCount(),
                                          hls.getMinSpareThreads().getCount(),
                                          hls.getProcessingTime().getCount(),
                                          hls.getRequestCount().getCount());
        CLILogger.getInstance().printMessage(data);
        if (fileName != null)
        {
            final String fileData = String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s,%8$s,"+
                                              "%9$s,%10$s,%11$s,%12$s,%13$s,%14$s,%15$s,"+
                                              "%16$s,%17$s,%18$s,%19$s,%20$s,%21$s,%22$s,"+
                                              "%23$s,%24$s,%25$s,%26$s",
                                              hls.getBytesReceived().getCount(),
                                              hls.getBytesSent().getCount(),
                                              hls.getCount200().getCount(),
                                              hls.getCount2xx().getCount(),
                                              hls.getCount302().getCount(),
                                              hls.getCount304().getCount(),
                                              hls.getCount3xx().getCount(),
                                              hls.getCount400().getCount(),
                                              hls.getCount401().getCount(),
                                              hls.getCount403().getCount(),
                                              hls.getCount404().getCount(),
                                              hls.getCount4xx().getCount(),
                                              hls.getCount503().getCount(),
                                              hls.getCount5xx().getCount(),
                                              hls.getCountOpenConnections().getCount(),
                                              hls.getCountOther().getCount(),
                                              hls.getCurrentThreadCount().getCount(),
                                              hls.getCurrentThreadsBusy().getCount(),
                                              hls.getErrorCount().getCount(),
                                              hls.getMaxOpenConnections().getCount(),
                                              hls.getMaxSpareThreads().getCount(),
                                              hls.getMaxThreads().getCount(),
                                              hls.getMaxTime().getCount(),
                                              hls.getMinSpareThreads().getCount(),
                                              hls.getProcessingTime().getCount(),
                                              hls.getRequestCount().getCount());
            writeToFile(fileData);
        }
    }

    
    public void displayDetails()
    {
        final String details = localStrings.getString("commands.monitor.httplistener_detail");
        CLILogger.getInstance().printMessage(details);
    }


}
    
    

