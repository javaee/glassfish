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

abstract class BeanMonitorTask extends MonitorTask
{
    String appName        = null;
    String ejbName        = null;
    String beanName = null;    

    public BeanMonitorTask(final ServerRootMonitor srm, final String filter, final Timer timer,
                              final boolean verbose, final File fileName)
        throws MonitorTaskException
    {
        super(srm, filter, timer, verbose, fileName);
    }

    
    /**
     *  Returns all the possible ejbmodules in all the application.
     *  The return format is <app-name>:<ejbmodule>:<bean>
     */
    String[] possibleAppModuleFilters()
    {
        final Map<String,ApplicationMonitor> appModuleMap = srm.getApplicationMonitorMap();

        List<String> possibleFilters = new Vector<String>();
        if (appModuleMap != null && appModuleMap.size() > 0)
        {
                //search through all application and check for the ejbs
            final String[] appModules = MapUtil.getKeyStrings(appModuleMap);
            for (String appModule : appModules)
            {
                ApplicationMonitor am = (ApplicationMonitor)appModuleMap.get(appModule);
                    //search for the ejb in the appmodule
                final  Map<String,EJBModuleMonitor> innerEjbModuleMap = am.getEJBModuleMonitorMap();
                possibleFilters.addAll(getBeansInEjbModule(appModule, innerEjbModuleMap));
            }
        }
        return (String[])possibleFilters.toArray(new String[0]);
    }
    
    
    /**
     *  Returns all the possible servlets in all the ejbmodules.
     *  The return format is <ejbmodule>:<servlet>
     */
    String[] possibleEjbModuleFilters()
    {
        final Map<String,EJBModuleMonitor> ejbModuleMap = srm.getEJBModuleMonitorMap();
        List<String> possibleFilters = getBeansInEjbModule(null, ejbModuleMap);
        return (String[])possibleFilters.toArray(new String[0]);
    }


    /**
     *  If filter option is not specified, then this method will
     *  get the default filter value.
     */
    void getDefaultFilter() throws MonitorTaskException
    {
        String[] appModuleFilters = possibleAppModuleFilters();
        String[] ejbModuleFilters = possibleEjbModuleFilters();        
            
        if (ejbModuleFilters.length < 1 && appModuleFilters.length < 1)
        {
            throw new MonitorTaskException(localStrings.getString("command.monitor.no_value_to_monitor"));
        }
        else if (ejbModuleFilters.length == 1 && appModuleFilters.length < 1)
        {
            filter = ejbModuleFilters[0];
            final StringTokenizer st = new StringTokenizer(filter, ":");
            ejbName = st.nextToken();
            beanName = st.nextToken();
        }
        else if (ejbModuleFilters.length < 1 && appModuleFilters.length == 1)
        {
            filter = appModuleFilters[0];
            final StringTokenizer st = new StringTokenizer(filter, ":");
            appName = st.nextToken();
            ejbName = st.nextToken();
            beanName = st.nextToken();            
        }
        else
        {
            final String msg = getAvailableFilterMessage(appModuleFilters, ejbModuleFilters);
            throw new MonitorTaskException(msg);
        }
    }


    private String getAvailableFilterMessage(final String[] appModuleFilters,
                                             final String[] ejbModuleFilters)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(localStrings.getString("commands.monitor.more_than_one_monitoring_elements"));
        if (ejbModuleFilters.length > 0)
        {
            sb.append(localStrings.getString("commands.monitor.available_ejbmodule_elements"));
            for (String ejbmodule : ejbModuleFilters)
            {
                sb.append("    ");
                sb.append(ejbmodule);
                sb.append("\n");
            }
        }
        if (appModuleFilters.length > 0)
        {
            sb.append(localStrings.getString("commands.monitor.available_appmodule_elements"));
            for (String appmodule: appModuleFilters)
            {
                sb.append("    ");
                sb.append(appmodule);
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    


    void verifyFilterValue() throws MonitorTaskException
    {
        if (appName != null)
        {
            final Map<String,ApplicationMonitor> appModuleMap = srm.getApplicationMonitorMap();
            if (appModuleMap.containsKey(appName))
            {
                final ApplicationMonitor am = appModuleMap.get(appName);
                final Map<String,EJBModuleMonitor> innerEJBModuleMap = am.getEJBModuleMonitorMap();
                
                final boolean exist = checkIfBeanExists(innerEJBModuleMap);
                if (!exist)
                    throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist_in2", new Object[] {beanName, appName, ejbName}));
            }
            else {
                throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist", new Object[] {appName}));
            }
        }
        else
        {
            final Map<String,EJBModuleMonitor> ejbModuleMap = srm.getEJBModuleMonitorMap();
            final boolean exist = checkIfBeanExists(ejbModuleMap);
            if (!exist)
                    throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist_in1", new Object[] {beanName, ejbName}));                
        }
    }
    

    abstract boolean checkIfBeanExists(final Map<String,EJBModuleMonitor> ejbModuleMap)
        throws MonitorTaskException;

    abstract List<String> getBeansInEjbModule(final String appName,
                                              final Map<String,EJBModuleMonitor> ejbModuleMap);
}
    
    

