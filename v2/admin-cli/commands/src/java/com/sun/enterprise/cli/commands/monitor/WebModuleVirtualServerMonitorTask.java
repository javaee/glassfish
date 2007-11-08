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
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.appserv.management.util.misc.MapUtil;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.Timer;
import java.util.List;
import java.util.Vector;
import java.io.File;

public class WebModuleVirtualServerMonitorTask extends MonitorTask
{
    private final String displayFormat = "%1$-5s %2$-5s %3$-5s %4$-5s %5$-5s %6$-5s "+
                                         "%7$-5s %8$-8s %9$-10s %10$-5s";

    
//    private final String displayFormat = "%1$-4s %2$-4s %3$-4s %4$-4s %5$-4s %6$-4s "+
//                                         "%7$-4s %8$-4s %9$-4s %10$-4s %11$-4s %12$-4s %13$-4s "+
//                                         "%14$-4s %15$-4s %16$-4s %17$-10s %18$-4s %19$-4s "+
//                                         "%20$-4s %21$-4s";
    
    private String appFilter = null;
    private String webFilter = null;

    public WebModuleVirtualServerMonitorTask(final ServerRootMonitor srm, final String filter, final Timer timer,
                                             final boolean verbose, final File fileName)
        throws MonitorTaskException
    {
        super(srm, filter, timer, verbose, fileName);

        
        if (this.filter == null)
            getDefaultFilter();
        else
        {
            final StringTokenizer st = new StringTokenizer(this.filter, ":");
            if (st.countTokens() < 2)
                webFilter = st.nextToken();
            else {
                appFilter = st.nextToken();
                webFilter = st.nextToken();
            }
            verifyFilterValue();
        }
        final String webmoduleVSMonitoringTitle=localStrings.getString("commands.monitor.webmodule_virtual_server_monitoring_title", new Object[] {this.filter});
        final String title = String.format("%1$50s", webmoduleVSMonitoringTitle);
        CLILogger.getInstance().printMessage(title);
        displayHeader();        
    }
    

    public void run()
    {
        if (srm == null) {
            super.cancelMonitorTask();
            return;
        }
        
        WebModuleVirtualServerStats vsStat = null;
        Map<String,WebModuleVirtualServerMonitor> webMap = null;
        if (appFilter == null)
            webMap = srm.getWebModuleVirtualServerMonitorMap();
        else
        {
            final Map<String,ApplicationMonitor> appMap = srm.getApplicationMonitorMap();
            if (appMap == null || appMap.size()<1) {
                cancelMonitorTask();
                return;
            }
            
            final ApplicationMonitor am = appMap.get(appFilter);
            if (am == null) {
                cancelMonitorTask();
                return;
            }
            webMap = am.getWebModuleVirtualServerMonitorMap();

        }
        if (webMap == null || webMap.size()<1) cancelMonitorTask();
        final WebModuleVirtualServerMonitor vsMonitor = webMap.get(webFilter);
        if (vsMonitor == null) {
            cancelMonitorTask();
            return;
        }
        
        final WebModuleVirtualServerStats vsStats = vsMonitor.getWebModuleVirtualServerStats();
        if (verbose && counter == NUM_ROWS)
        {
            displayHeader();
            counter = 0;  //reset to 0
        }
        displayData(vsStats);
        if (verbose) counter++;
    }

    
    /**
     *  Returns all the possible webmodules in all the application.
     *  The return format is <app-name>:<webmodule>
     */
    private String[] possibleAppModuleFilters()
    {
        final Map<String,ApplicationMonitor> appModuleMap = srm.getApplicationMonitorMap();

        List<String> possibleFilters = new Vector<String>();
        if (appModuleMap != null && appModuleMap.size() > 0)
        {
                //search through all application and check for the webmodules
            final String[] appModules = MapUtil.getKeyStrings(appModuleMap);
            for (String appModule : appModules)
            {
                ApplicationMonitor am = (ApplicationMonitor)appModuleMap.get(appModule);
                    //search for the webmodule virtual server in the appmodule
                final Map<String, WebModuleVirtualServerMonitor> innerWebModuleMap = am.getWebModuleVirtualServerMonitorMap();
                if (innerWebModuleMap != null && innerWebModuleMap.size() > 0)
                {
                    final String[] webModules = MapUtil.getKeyStrings(innerWebModuleMap);
                    for (String webModule : webModules)
                        possibleFilters.add(appModule+":"+webModule);
                }
            }
        }
        return (String[])possibleFilters.toArray(new String[0]);
    }
    
    
    private String[] possibleWebModuleFilters()
    {
        final Map<String,WebModuleVirtualServerMonitor> webModuleMap = srm.getWebModuleVirtualServerMonitorMap();

        List<String> possibleFilters = new Vector<String>();
        final String[] webModules = MapUtil.getKeyStrings(webModuleMap);
        if (webModuleMap != null && webModuleMap.size() > 0)
        {
            for (String webModule : webModules)
                possibleFilters.add(webModule);
        }
        return (String[])possibleFilters.toArray(new String[0]);
    }


    /**
     *  If filter option is not specified, then this method will
     *  get the default filter value.
     *  WebModuleVirtualServer is in both standalone webmodule or
     *  a webmodule in a application.  If there are more than one
     *  standalone webmodule or an application with a webmodule,
     *  this method will display the available filter selections.
     *  If there is only one webmodule then that is the default
     *  filter value.
     */
    private void getDefaultFilter() throws MonitorTaskException
    {
        String[] appModuleFilters = possibleAppModuleFilters();
        String[] webModuleFilters = possibleWebModuleFilters();        
            
        if (webModuleFilters.length < 1 && appModuleFilters.length < 1)
        {
            throw new MonitorTaskException(localStrings.getString("command.monitor.no_value_to_monitor"));
        }
        else if (webModuleFilters.length == 1 && appModuleFilters.length < 1)
        {
            webFilter = webModuleFilters[0];
            filter = webFilter;
        }
        else if (webModuleFilters.length < 1 && appModuleFilters.length == 1)
        {
            filter = appModuleFilters[0];
            final StringTokenizer st = new StringTokenizer(filter, ":");
            appFilter = st.nextToken();
            webFilter = st.nextToken();
        }
        else
        {
            final String msg = getAvailableFilterMessage(appModuleFilters, webModuleFilters);
            throw new MonitorTaskException(msg);
        }
    }


    private String getAvailableFilterMessage(final String[] appModuleFilters,
                                             final String[] webModuleFilters)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(localStrings.getString("commands.monitor.more_than_one_monitoring_elements"));
        if (webModuleFilters.length > 0)
        {
            sb.append(localStrings.getString("commands.monitor.available_webmodule_elements"));
            for (String webmodule: webModuleFilters)
            {
                sb.append("    ");
                sb.append(webmodule);
                sb.append("\n");
            }
        }
        if (appModuleFilters.length > 0)
        {
            sb.append(localStrings.getString("commands.monitor.available_webmodule_elements_in_appmodule"));
            for (String appmodule: appModuleFilters)
            {
                sb.append("    ");
                sb.append(appmodule);
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    


    private void verifyFilterValue() throws MonitorTaskException
    {
        if (appFilter != null)
        {
            final Map<String,ApplicationMonitor> appModuleMap = srm.getApplicationMonitorMap();
            if (appModuleMap.containsKey(appFilter))
            {
                final ApplicationMonitor am = appModuleMap.get(appFilter);
                final Map<String, WebModuleVirtualServerMonitor> innerWebModuleMap = am.getWebModuleVirtualServerMonitorMap();
                if (!innerWebModuleMap.containsKey(webFilter))
                    throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist_in1", new Object[] {webFilter, appFilter}));
            }
            else {
                throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist", new Object[] {appFilter}));
            }
        }
        else
        {
            final Map<String,WebModuleVirtualServerMonitor> webModuleMap = srm.getWebModuleVirtualServerMonitorMap();
            if (!webModuleMap.containsKey(webFilter))
                throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist", new Object[] {webFilter}));
        }
    }
    

    
    private void displayHeader()
    {
        final String asc = localStrings.getString("commands.monitor.asc");
        final String ash = localStrings.getString("commands.monitor.ash");
        final String est = localStrings.getString("commands.monitor.est");
        final String jc = localStrings.getString("commands.monitor.jc");
        final String jec = localStrings.getString("commands.monitor.jec");
        final String jrc = localStrings.getString("commands.monitor.jrc");
        final String rst = localStrings.getString("commands.monitor.rst");
        final String svpt = localStrings.getString("commands.monitor.svpt");
        final String ss = localStrings.getString("commands.monitor.ss");
        final String sst = localStrings.getString("commands.monitor.sst");
        
        final String header = String.format(displayFormat,
                                            asc,ash,est,jc,jec,jrc,rst,svpt,ss,sst);                                            
            /*
        final String header = String.format(displayFormat,
                                            "asc","ash","csc","low","hi","cur","est","jc","jec",
                                            "psc","rst","svpt","sspt","low","hi","cur","ss","low",
                                            "hi","cur","sst");
            */
        CLILogger.getInstance().printMessage(header);
        if (fileName != null)
        {
            writeToFile(localStrings.getString("commands.monitor.webmodule_virtual_server_write_to_file"));
        }
    }

    
    private void displayData(final WebModuleVirtualServerStats stat)
    {

        final String data = String.format(displayFormat,
                                          stat.getActiveSessionsCurrent().getCount(),
                                          stat.getActiveSessionsHigh().getCount(),
//                                          stat.getCachedSessionsCurrent().getCount(),
//                                          stat.getContainerLatency().getLowWaterMark(),
//                                          stat.getContainerLatency().getHighWaterMark(),
//                                          stat.getContainerLatency().getCurrent(),
                                          stat.getExpiredSessionsTotal().getCount(),
                                          stat.getJSPCount().getCount(),
                                          stat.getJSPErrorCount().getCount(),
                                          stat.getJSPReloadCount().getCount(),
//                                          stat.getPassivatedSessionsCurrent().getCount(),
                                          stat.getRejectedSessionsTotal().getCount(),
                                          stat.getServletProcessingTimes().getCount(),
//                                          stat.getSessionPersistTime().getLowWaterMark(),
//                                          stat.getSessionPersistTime().getHighWaterMark(),
//                                          stat.getSessionPersistTime().getCurrent(),
                                          stat.getSessions().getCurrent(),
//                                          stat.getSessionSize().getLowWaterMark(),
//                                          stat.getSessionSize().getHighWaterMark(),
//                                          stat.getSessionSize().getCurrent(),
                                          stat.getSessionsTotal().getCount());

        CLILogger.getInstance().printMessage(data);
        if (fileName != null)
        {
            final String fileData = String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,"+
                                                  "%7$s,%8$s,%9$s,%10$s",
                                                  stat.getActiveSessionsCurrent().getCount(),
                                                  stat.getActiveSessionsHigh().getCount(),
                                                  stat.getExpiredSessionsTotal().getCount(),
                                                  stat.getJSPCount().getCount(),
                                                  stat.getJSPErrorCount().getCount(),
                                                  stat.getJSPReloadCount().getCount(),
                                                  stat.getRejectedSessionsTotal().getCount(),
                                                  stat.getServletProcessingTimes().getCount(),
                                                  stat.getSessions().getCurrent(),
                                                  stat.getSessionsTotal().getCount());
            writeToFile(fileData);
        }
    }

    
    public void displayDetails()
    {
        final String details = localStrings.getString("commands.monitor.webmodule_virtual_server_detail");
        CLILogger.getInstance().printMessage(details);
    }
}
    
    

