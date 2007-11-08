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

public class WebServiceEndpointMonitorTask extends MonitorTask
{
    private final String displayFormat = "%1$-8s %2$-8s %3$-8s %4$-8s %5$-8s %6$-8s "+
                                         "%7$-8s %8$-8s %9$-8s";

    private String appName = null;
    private String moduleName = null;  /* can be webmodule or ejbmodule.  They are unique. */
    private String endpointName = null;

    public WebServiceEndpointMonitorTask(final ServerRootMonitor srm, final String filter, final Timer timer,
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
                throw new MonitorTaskException(localStrings.getString("commands.monitor.endpoint_invalid_filter"));
            else if (st.countTokens() == 2)
            {
                moduleName = st.nextToken();
                endpointName = st.nextToken();
            }
            else
            {
                appName = st.nextToken();
                moduleName = st.nextToken();
                endpointName = st.nextToken();
            }
        }
        verifyFilterValue();
        final String endpointTitle = localStrings.getString("commands.monitor.endpoint_monitoring_title", new Object[] {this.filter});
        final String title = String.format("%1$50s", endpointTitle);
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
        Map<String,EJBModuleMonitor> ejbMap = null;        
        if (appName == null)
        {
            webMap = srm.getWebModuleVirtualServerMonitorMap();
            ejbMap = srm.getEJBModuleMonitorMap();
        }
        else
        {
            final Map<String,ApplicationMonitor> appMap = srm.getApplicationMonitorMap();
            if (appMap == null || appMap.size()<1) {
                cancelMonitorTask();
                return;
            }
            
            final ApplicationMonitor am = appMap.get(appName);
            if (am == null) {
                cancelMonitorTask();
                return;
            }
            webMap = am.getWebModuleVirtualServerMonitorMap();
            ejbMap = am.getEJBModuleMonitorMap();
        }
        final WebServiceEndpointMonitor endpointMonitor = getEndpointMonitor(webMap, ejbMap);
        if (endpointMonitor == null) {
            cancelMonitorTask();
            return;
        }
        
        final WebServiceEndpointAggregateStats endpointStats = endpointMonitor.getWebServiceEndpointAggregateStats();
        if (verbose && counter == NUM_ROWS)
        {
            displayHeader();
            counter = 0;  //reset to 0
        }
        displayData(endpointStats);
        if (verbose) counter++;
    }

    
    private WebServiceEndpointMonitor getEndpointMonitor(final Map<String,WebModuleVirtualServerMonitor> webMap,
                                                         final Map<String,EJBModuleMonitor> ejbMap )
    {
        Map<String,WebServiceEndpointMonitor> endpointMap = null;
        if (webMap != null && webMap.containsKey(moduleName))
        {
            final WebModuleVirtualServerMonitor webmoduleVS = webMap.get(moduleName);
            endpointMap = webmoduleVS.getWebServiceEndpointMonitorMap();
        }
        else if (ejbMap != null && ejbMap.containsKey(moduleName))
        {
            final EJBModuleMonitor ejbModule = ejbMap.get(moduleName);
            endpointMap = ejbModule.getWebServiceEndpointMonitorMap();
        }
        else
        {
            CLILogger.getInstance().printMessage(localStrings.getString("commands.monitor.unable_to_monitor_endpoint"));
            cancelMonitorTask();
        }
        return endpointMap.get(endpointName);
    }
    
    
    
    /**
     *  Returns all the possible webmodules in all the application.
     *  The return format is <app-name>:<webmodule>:<endpoint>
     */
    private String[] possibleAppModuleFilters()
    {
        final Map<String,ApplicationMonitor> appModuleMap = srm.getApplicationMonitorMap();

        List<String> webModules = new Vector<String>();
        List<String> ejbModules = new Vector<String>();        
        if (appModuleMap != null && appModuleMap.size() > 0)
        {
                //search through all application and check for the webmodules and ejbmodules
            final String[] appModules = MapUtil.getKeyStrings(appModuleMap);
            for (String appModule : appModules)
            {
                ApplicationMonitor am = (ApplicationMonitor)appModuleMap.get(appModule);
                    //search for the webmodule in the appmodule
                final Map<String, WebModuleVirtualServerMonitor> innerWebModuleMap = am.getWebModuleVirtualServerMonitorMap();
                webModules.addAll(getEndpointsInWebModules(appModule, innerWebModuleMap));
                    //search for the ejbmodule in the appmodule                
                final Map<String,EJBModuleMonitor> innerEjbModuleMap = am.getEJBModuleMonitorMap();
                ejbModules.addAll(getEndpointsInEjbModules(appModule, innerEjbModuleMap));                
            }
        }
        List<String> combineModules = webModules;
        combineModules.addAll(ejbModules);
        return (String[])combineModules.toArray(new String[0]);
    }


    /**
     *  Returns all the possible endpoints in all the webmodules.
     *  The return format is <webmodule>:<endpoint>
     */
    private String[] possibleWebModuleFilters()
    {
        final Map<String,WebModuleVirtualServerMonitor> webModuleMap = srm.getWebModuleVirtualServerMonitorMap();

        List<String> possibleFilters = getEndpointsInWebModules(null, webModuleMap);
        return (String[])possibleFilters.toArray(new String[0]);
    }

    
    /**
     *  Returns all the possible endpoints in all the ejbs.
     *  The return format is <ejbmodule>:<endpoint>
     */
    private String[] possibleEjbModuleFilters()
    {
        final Map<String,EJBModuleMonitor> ejbModuleMap = srm.getEJBModuleMonitorMap();
        List<String> possibleFilters = getEndpointsInEjbModules(null, ejbModuleMap);
        return (String[])possibleFilters.toArray(new String[0]);
    }
    


    private List<String> getEndpointsInWebModules(final String appName,
                                                 final Map<String,WebModuleVirtualServerMonitor> webModuleMap)
    {
        List<String> possibleEndpoints = new Vector<String>();
        final String[] webModules = MapUtil.getKeyStrings(webModuleMap);
        if (webModuleMap != null && webModuleMap.size() > 0)
        {
            for (String webModule : webModules)
            {
                final WebModuleVirtualServerMonitor webmoduleVS = webModuleMap.get(webModule);
                final Map<String,WebServiceEndpointMonitor> endpointMap = webmoduleVS.getWebServiceEndpointMonitorMap();
                final String[] endpoints = MapUtil.getKeyStrings(endpointMap);
                for (String endpoint : endpoints)
                {
                    if (appName == null)
                        possibleEndpoints.add(webModule+":"+endpoint);
                    else
                        possibleEndpoints.add(appName+":"+webModule+":"+endpoint);
                }
            }
        }
        return possibleEndpoints;
    }


    private List<String> getEndpointsInEjbModules(final String appName,
                                                 final Map<String,EJBModuleMonitor> ejbModuleMap)
    {
        List<String> possibleEndpoints = new Vector<String>();
        final String[] ejbModules = MapUtil.getKeyStrings(ejbModuleMap);
        if (ejbModuleMap != null && ejbModuleMap.size() > 0)
        {
            for (String ejbModule : ejbModules)
            {
                final EJBModuleMonitor ejbModuleMonitor = ejbModuleMap.get(ejbModule);
                final Map<String,WebServiceEndpointMonitor> endpointMap = ejbModuleMonitor.getWebServiceEndpointMonitorMap();
                final String[] endpoints = MapUtil.getKeyStrings(endpointMap);
                for (String endpoint : endpoints)
                {
                    if (appName == null)
                        possibleEndpoints.add(ejbModule+":"+endpoint);
                    else
                        possibleEndpoints.add(appName+":"+ejbModule+":"+endpoint);
                }
            }
        }
        return possibleEndpoints;
    }


    /**
     *  If filter option is not specified, then this method will
     *  get the default filter value.
     *  Endpoint is in both standalone webmodule or a webmodule
     *  in a application or in ejb.  If there are more than one
     *  standalone webmodule or an application with a webmodule
     *  or ejb, this method will display the available filter
     *  selections.  If there is only one webmodule then that is
     *  the default filter value.
     */
    private void getDefaultFilter() throws MonitorTaskException
    {
        String[] appModuleFilters = possibleAppModuleFilters();
        String[] webModuleFilters = possibleWebModuleFilters();
        String[] ejbModuleFilters = possibleEjbModuleFilters();
            
        if (webModuleFilters.length < 1 && appModuleFilters.length < 1
            && ejbModuleFilters.length < 1)
        {
            throw new MonitorTaskException("No value to monitor.");
        }
        else if (webModuleFilters.length == 1 && appModuleFilters.length < 1
                 && ejbModuleFilters.length < 1)
        {
            filter = webModuleFilters[0];
            final StringTokenizer st = new StringTokenizer(filter, ":");
            moduleName = st.nextToken();
            endpointName = st.nextToken();
        }
        else if (webModuleFilters.length < 1 && appModuleFilters.length == 1
                 && ejbModuleFilters.length < 1)
        {
            filter = appModuleFilters[0];
            final StringTokenizer st = new StringTokenizer(filter, ":");
            appName = st.nextToken();
            moduleName = st.nextToken();
            endpointName = st.nextToken();            
        }
        else if (webModuleFilters.length < 1 && appModuleFilters.length < 1
                 && ejbModuleFilters.length == 1)
        {
            filter = ejbModuleFilters[0];
            final StringTokenizer st = new StringTokenizer(filter, ":");
            moduleName = st.nextToken();
            endpointName = st.nextToken();            
        }
        else
        {
            final String msg = getAvailableFilterMessage(appModuleFilters,
                                                         webModuleFilters,
                                                         ejbModuleFilters);
            throw new MonitorTaskException(msg);
        }
    }


    private String getAvailableFilterMessage(final String[] appModuleFilters,
                                             final String[] webModuleFilters,
                                             final String[] ejbModuleFilters)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(localStrings.getString("commands.monitor.more_than_one_monitoring_elements"));
        if (webModuleFilters.length > 0)
        {
            sb.append(localStrings.getString("commands.monitor.available_endpoint_in_webmodule"));
            for (String webmodule: webModuleFilters)
            {
                sb.append("    ");
                sb.append(webmodule);
                sb.append("\n");
            }
            
        }
        if (ejbModuleFilters.length > 0)
        {
            sb.append(localStrings.getString("commands.monitor.available_endpoint_in_ejbmodule"));
            for (String ejbmodule: ejbModuleFilters)
            {
                sb.append("    ");
                sb.append(ejbmodule);
                sb.append("\n");
            }
            
        }
        if (appModuleFilters.length > 0)
        {
            sb.append(localStrings.getString("commands.monitor.available_endpoint_in_appmodule"));
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
                final Map<String,WebModuleVirtualServerMonitor> innerWebModuleMap = am.getWebModuleVirtualServerMonitorMap();
                final Map<String,EJBModuleMonitor> innerEjbModuleMap = am.getEJBModuleMonitorMap();
                final boolean exist = checkIfEndpointExists(innerWebModuleMap, innerEjbModuleMap);
                if (!exist)
                    throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist_in2", new Object[] {endpointName, appName, moduleName}));
            }
            else {
                throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist", new Object[] {appName}));
            }
        }
        else
        {
            final Map<String,WebModuleVirtualServerMonitor> webModuleMap = srm.getWebModuleVirtualServerMonitorMap();
            final Map<String,EJBModuleMonitor> ejbModuleMap = srm.getEJBModuleMonitorMap();
            final boolean exist = checkIfEndpointExists(webModuleMap, ejbModuleMap);
            if (!exist)
                throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist_in1", new Object[] {endpointName, moduleName}));
        }
    }
    

        /**
         * returns true if endpoint exists in webmodule
         * @throws MonitorTaskException if webmodlue is invalid.
         */
    private boolean checkIfEndpointExists(final Map<String,WebModuleVirtualServerMonitor> webModuleMap,
                                          final Map<String,EJBModuleMonitor> ejbModuleMap )
        throws MonitorTaskException
    {
        final Map<String,WebServiceEndpointMonitor> endpointMap;
        if (webModuleMap.containsKey(moduleName))
        {
            final WebModuleVirtualServerMonitor webmoduleVS = webModuleMap.get(moduleName);
            endpointMap = webmoduleVS.getWebServiceEndpointMonitorMap();
        }
        else if (ejbModuleMap.containsKey(moduleName))
        {
            final EJBModuleMonitor ejbmodule = ejbModuleMap.get(moduleName);
            endpointMap = ejbmodule.getWebServiceEndpointMonitorMap();
        }
        else 
        {
            if (appName == null)
                throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist", new Object[] {moduleName}));
            else
                throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist_in1", new Object[] {moduleName, appName}));
        }
        if (!endpointMap.containsKey(endpointName))
            return false;
        else
            return true;
    }
    
    
    private void displayHeader()
    {
        final String art = localStrings.getString("commands.monitor.art");
        final String maxrt = localStrings.getString("commands.monitor.maxrt");
        final String minrt = localStrings.getString("commands.monitor.minrt");
        final String rt = localStrings.getString("commands.monitor.rt");
        final String tp = localStrings.getString("commands.monitor.tp");
        final String taf = localStrings.getString("commands.monitor.taf");
        final String tas = localStrings.getString("commands.monitor.tas");
        final String tf = localStrings.getString("commands.monitor.tf");
        final String tns = localStrings.getString("commands.monitor.tns");
        
        final String header = String.format(displayFormat,
                                            art,maxrt,minrt,rt,tp,taf,tas,tf,tns);
        CLILogger.getInstance().printMessage(header);
        if (fileName != null)
        {
            writeToFile(localStrings.getString("commands.monitor.endpoint_write_to_file"));
        }
    }

    
    private void displayData(final WebServiceEndpointAggregateStats stats)
    {
        final String data = String.format(displayFormat,
                                          stats.getAverageResponseTime().getCount(),
                                          stats.getMaxResponseTime().getCount(),
                                          stats.getMinResponseTime().getCount(),
                                          stats.getResponseTime().getCount(),
                                          stats.getThroughput().getCurrent().longValue(),
                                          stats.getTotalAuthFailures().getCount(),
                                          stats.getTotalAuthSuccesses().getCount(),
                                          stats.getTotalFaults().getCount(),
                                          stats.getTotalNumSuccess().getCount());
        CLILogger.getInstance().printMessage(data);
        if (fileName != null)
        {
            final String fileData = String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s,%8$s,%9$s",
                                                  stats.getAverageResponseTime().getCount(),
                                                  stats.getMaxResponseTime().getCount(),
                                                  stats.getMinResponseTime().getCount(),
                                                  stats.getResponseTime().getCount(),
                                                  stats.getThroughput().getCurrent().longValue(),
                                                  stats.getTotalAuthFailures().getCount(),
                                                  stats.getTotalAuthSuccesses().getCount(),
                                                  stats.getTotalFaults().getCount(),
                                                  stats.getTotalNumSuccess().getCount());
            writeToFile(fileData);
        }
    }

    
    public void displayDetails()
    {
        final String details = localStrings.getString("commands.monitor.endpoint_detail");
        CLILogger.getInstance().printMessage(details);
    }
}
    
    

