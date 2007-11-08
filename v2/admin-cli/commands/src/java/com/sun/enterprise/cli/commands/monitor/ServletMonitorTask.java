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
import java.util.StringTokenizer;
import java.util.Map;
import java.util.Timer;
import java.util.List;
import java.util.Vector;
import java.io.File;

public class ServletMonitorTask extends MonitorTask
{
    private final String displayFormat = "%1$-8s %2$-8s %3$-8s %4$-8s %5$-8s %6$-8s "+
                                         "%7$-8s %8$-8s";

    private String appName     = null;
    private String webName     = null;
    private String servletName = null;    

    public ServletMonitorTask(final ServerRootMonitor srm, final String filter, final Timer timer,
                                             final boolean verbose, final File fileName)
        throws MonitorTaskException
    {
        super(srm, filter, timer, verbose, fileName);

        
        if (this.filter == null)
            getDefaultFilter();
        else
        {
            final StringTokenizer st = new StringTokenizer(this.filter, ":");
            if (st.countTokens() < 2 )
                throw new MonitorTaskException(localStrings.getString("commands.monitor.servlet_invalid_filter"));
            else if (st.countTokens() == 2)
            {
                webName = st.nextToken();
                servletName = st.nextToken();
            }
            else {
                appName = st.nextToken();
                webName = st.nextToken();
                servletName = st.nextToken();
            }
            verifyFilterValue();
        }
        final String servletTitle = localStrings.getString("commands.monitor.servlet_title", new Object[] {this.filter});
        final String title = String.format("%1$50s", servletTitle);
        CLILogger.getInstance().printMessage(title);
        displayHeader();        
    }
    

    public void run()
    {
        if (srm == null) {
            super.cancelMonitorTask();
            return;
        }
        
        Map<String,WebModuleVirtualServerMonitor> webMap = null;
        if (appName == null)
        {
            webMap = srm.getWebModuleVirtualServerMonitorMap();            
        }
        else
        {
            final Map<String,ApplicationMonitor> appMap = srm.getApplicationMonitorMap();
            if (appMap == null || appMap.size()<1) {
                cancelMonitorTask();
                return;
            }
            
            final ApplicationMonitor am = appMap.get(appName);
            webMap = am.getWebModuleVirtualServerMonitorMap();
        }
        if (webMap == null || webMap.size()<1) {
            cancelMonitorTask();
            return;
        }
        
        final ServletMonitor servletMonitor = getServletFromWebModule(webMap);
        if (servletMonitor == null) {
            cancelMonitorTask();
            return;
        }
        
        final AltServletStats servletStats = servletMonitor.getAltServletStats();
        if (verbose && counter == NUM_ROWS)
        {
            displayHeader();
            counter = 0;  //reset to 0
        }
        displayData(servletStats);
        if (verbose) counter++;
    }

    
    private ServletMonitor getServletFromWebModule(final Map<String, WebModuleVirtualServerMonitor> webMap)
    {
        final WebModuleVirtualServerMonitor webmoduleVS = webMap.get(webName);
        final Map<String, ServletMonitor> servletMap = webmoduleVS.getServletMonitorMap();
        return servletMap.get(servletName);
    }
    
    
    
    /**
     *  Returns all the possible webmodules in all the application.
     *  The return format is <app-name>:<webmodule>:<servlet>
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
                possibleFilters.addAll(getServletsInWebModules(appModule, innerWebModuleMap));
            }
        }
        return (String[])possibleFilters.toArray(new String[0]);
    }
    
    
    /**
     *  Returns all the possible servlets in all the webmodules.
     *  The return format is <webmodule>:<servlet>
     */
    private String[] possibleWebModuleFilters()
    {
        final Map<String,WebModuleVirtualServerMonitor> webModuleMap = srm.getWebModuleVirtualServerMonitorMap();

        List<String> possibleFilters = getServletsInWebModules(null, webModuleMap);
        return (String[])possibleFilters.toArray(new String[0]);
    }


    private List<String> getServletsInWebModules(final String appName,
                                                 final Map<String,WebModuleVirtualServerMonitor> webModuleMap)
    {
        List<String> possibleServlets = new Vector<String>();
        final String[] webModules = MapUtil.getKeyStrings(webModuleMap);
        if (webModuleMap != null && webModuleMap.size() > 0)
        {
            for (String webModule : webModules)
            {
                final WebModuleVirtualServerMonitor webmoduleVS = webModuleMap.get(webModule);
                final Map<String,ServletMonitor> servletMap = webmoduleVS.getServletMonitorMap();
                final String[] servlets = MapUtil.getKeyStrings(servletMap);
                for (String servlet : servlets)
                {
                    if (appName == null)
                        possibleServlets.add(webModule+":"+servlet);
                    else
                        possibleServlets.add(appName+":"+webModule+":"+servlet);                        
                }
            }
        }
        return possibleServlets;
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
            throw new MonitorTaskException("No value to monitor.");
        }
        else if (webModuleFilters.length == 1 && appModuleFilters.length < 1)
        {
            filter = webModuleFilters[0];
            final StringTokenizer st = new StringTokenizer(filter, ":");
            webName = st.nextToken();
            servletName = st.nextToken();
        }
        else if (webModuleFilters.length < 1 && appModuleFilters.length == 1)
        {
            filter = appModuleFilters[0];
            final StringTokenizer st = new StringTokenizer(filter, ":");
            appName = st.nextToken();
            webName = st.nextToken();
            servletName = st.nextToken();            
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
            sb.append(localStrings.getString("commands.monitor.available_servlet_elements"));
            for (String webmodule: webModuleFilters)
            {
                sb.append("    ");
                sb.append(webmodule);
                sb.append("\n");
            }
        }
        if (appModuleFilters.length > 0)
        {
            sb.append(localStrings.getString("commands.monitor.available_servlet_elements_in_appmodule"));
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
        if (appName != null)
        {
            final Map<String,ApplicationMonitor> appModuleMap = srm.getApplicationMonitorMap();
            if (appModuleMap.containsKey(appName))
            {
                final ApplicationMonitor am = appModuleMap.get(appName);
                final Map<String, WebModuleVirtualServerMonitor> innerWebModuleMap = am.getWebModuleVirtualServerMonitorMap();
                final boolean exist = checkIfServletExistInWebModule(innerWebModuleMap);
                if (!exist)
                    throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist_in2", new Object[] {servletName, appName, webName}));
            }
            else {
                throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist", new Object[] {appName}));
            }
        }
        else
        {
            final Map<String,WebModuleVirtualServerMonitor> webModuleMap = srm.getWebModuleVirtualServerMonitorMap();
            final boolean exist = checkIfServletExistInWebModule(webModuleMap);
            if (!exist)
                throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist_in1", new Object[] {servletName, webName}));
        }
    }
    

        /**
         * returns true if servlet exists in webmodule
         * @throws MonitorTaskException if webmodlue is invalid.
         */
    private boolean checkIfServletExistInWebModule(final Map<String,WebModuleVirtualServerMonitor> webModuleMap)
        throws MonitorTaskException
    {
        if (!webModuleMap.containsKey(webName))
        {
            if (appName == null)
                throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist", new Object[] {webName}));
            else
                throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist_in1", new Object[] {webName, appName}));
        }
        else {
            final WebModuleVirtualServerMonitor webmoduleVS = webModuleMap.get(webName);
            final Map<String,ServletMonitor> servletMap = webmoduleVS.getServletMonitorMap();
            if (!servletMap.containsKey(servletName))
                return false;
            else
                return true;
        }
    }
    
    
    private void displayHeader()
    {
        final String ec = localStrings.getString("commands.monitor.ec");
        final String mt = localStrings.getString("commands.monitor.mt");
        final String pt = localStrings.getString("commands.monitor.pt");
        final String rc = localStrings.getString("commands.monitor.rc");
        final String current = localStrings.getString("commands.monitor.current");
        final String maxtm = localStrings.getString("commands.monitor.maxtm");
        final String mintm = localStrings.getString("commands.monitor.mintm");
        final String totaltm = localStrings.getString("commands.monitor.totaltm");
        
        final String header = String.format(displayFormat,
                                            ec,mt,pt,rc,current,maxtm,mintm,totaltm);
        CLILogger.getInstance().printMessage(header);
        if (fileName != null)
        {
            writeToFile(localStrings.getString("commands.monitor.servlet_write_to_file"));
        }
    }

    
    private void displayData(final AltServletStats stat)
    {
        final String data = String.format(displayFormat,
                                          stat.getErrorCount().getCount(),
                                          stat.getMaxTime().getCount(),
                                          stat.getProcessingTime().getCount(),
                                          stat.getRequestCount().getCount(),
                                          stat.getServiceTime().getCount(),
                                          stat.getServiceTime().getMaxTime(),
                                          stat.getServiceTime().getMinTime(),
                                          stat.getServiceTime().getTotalTime());
        CLILogger.getInstance().printMessage(data);
        if (fileName != null)
        {
            final String fileData = String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s,%8$s",
                                                  stat.getErrorCount().getCount(),
                                                  stat.getMaxTime().getCount(),
                                                  stat.getProcessingTime().getCount(),
                                                  stat.getRequestCount().getCount(),
                                                  stat.getServiceTime().getCount(),
                                                  stat.getServiceTime().getMaxTime(),
                                                  stat.getServiceTime().getMinTime(),
                                                  stat.getServiceTime().getTotalTime());
            writeToFile(fileData);
        }
    }

    
    public void displayDetails()
    {
        final String details = localStrings.getString("commands.monitor.servlet_detail");
        CLILogger.getInstance().printMessage(details);
    }


}
    
    

