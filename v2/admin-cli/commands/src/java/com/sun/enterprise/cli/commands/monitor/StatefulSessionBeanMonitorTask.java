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

public class StatefulSessionBeanMonitorTask extends BeanMonitorTask
{
    private final String displayFormat = "%1$-8s %2$-8s %3$-8s %4$-8s %5$-8s %6$-8s %7$-8s %8$-8s";

    public StatefulSessionBeanMonitorTask(final ServerRootMonitor srm, final String filter, final Timer timer,
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
                throw new MonitorTaskException(localStrings.getString("commands.monitor.stateful_session_bean_invalid_filter"));
            else if (st.countTokens() == 2)
            {
                ejbName = st.nextToken();
                beanName = st.nextToken();
            }
            else {
                appName = st.nextToken();
                ejbName = st.nextToken();
                beanName = st.nextToken();
            }
            verifyFilterValue();
        }
        final String statefulSessionBeanTitle = localStrings.getString("commands.monitor.stateful_session_bean_title", new Object[] {this.filter});
        final String title = String.format("%1$50s", statefulSessionBeanTitle);
        CLILogger.getInstance().printMessage(title);
        displayHeader();        
    }
    

    public void run()
    {
        if (srm == null) {
            super.cancelMonitorTask();
            return;
        }
        
        Map<String,EJBModuleMonitor> ejbMap = null;
        if (appName == null)
        {
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
            ejbMap = am.getEJBModuleMonitorMap();
        }
        if (ejbMap == null || ejbMap.size()<1) {
            cancelMonitorTask();
            return;
        }
        
        final StatefulSessionBeanMonitor monitor = getStatefulSessionBeanMonitor(ejbMap);
        if (monitor == null) {
            cancelMonitorTask();
            return;
        }
            
        final StatefulSessionBeanStats stat = monitor.getStatefulSessionBeanStats();
        
        if (verbose && counter == NUM_ROWS)
        {
            displayHeader();
            counter = 0;  //reset to 0
        }
        displayData(stat);
        if (verbose) counter++;
    }

    
    private StatefulSessionBeanMonitor getStatefulSessionBeanMonitor(final Map<String,EJBModuleMonitor> ejbMap)
    {
        final EJBModuleMonitor ejbModuleMonitor = ejbMap.get(ejbName);
        final Map<String,StatefulSessionBeanMonitor> mdbMap = ejbModuleMonitor.getStatefulSessionBeanMonitorMap();
        return mdbMap.get(beanName);
    }
    
    
    List<String> getBeansInEjbModule(final String appName,
                                     final Map<String,EJBModuleMonitor> ejbModuleMap)
    {
        List<String> possibleStatefulSessionBeans = new Vector<String>();
        final String[] ejbModules = MapUtil.getKeyStrings(ejbModuleMap);
        if (ejbModuleMap != null && ejbModuleMap.size() > 0)
        {
            for (String ejbModule : ejbModules)
            {
                final EJBModuleMonitor ejbModuleMonitor = ejbModuleMap.get(ejbModule);
                final Map<String,StatefulSessionBeanMonitor> beanMap = ejbModuleMonitor.getStatefulSessionBeanMonitorMap();
                final String[] statefulBeans = MapUtil.getKeyStrings(beanMap);
                for (String statefulBean : statefulBeans)
                {
                    if (appName == null)
                        possibleStatefulSessionBeans.add(ejbModule+":"+statefulBean);
                    else
                        possibleStatefulSessionBeans.add(appName+":"+ejbModule+":"+statefulBean);
                }
            }
        }
        return possibleStatefulSessionBeans;
    }

    
        /**
         * returns true if statefulsessionbean exists in ejbmodule
         * @throws MonitorTaskException if ejbmodule is invalid.
         */
    boolean checkIfBeanExists(final Map<String,EJBModuleMonitor> ejbModuleMap)
        throws MonitorTaskException
    {
        if (!ejbModuleMap.containsKey(ejbName))
        {
            if (appName == null)
                throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist", new Object[] {ejbName}));
            else
                throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist_in1", new Object[] {ejbName, appName}));
        }
        else {
            final EJBModuleMonitor ejbModuleMonitor = ejbModuleMap.get(ejbName);
            final Map<String,StatefulSessionBeanMonitor> beanMap = ejbModuleMonitor.getStatefulSessionBeanMonitorMap();
            if (!beanMap.containsKey(beanName))
                return false;
            else
                return true;
        }
    }
    
    
    private void displayHeader()
    {
        final String passiveCount = localStrings.getString("commands.monitor.PassiveCount");
        final String methodReadyCount = localStrings.getString("commands.monitor.MethodReadyCount");
        final String low = localStrings.getString("commands.monitor.low");
        final String high = localStrings.getString("commands.monitor.high");
        final String current = localStrings.getString("commands.monitor.current");
        final String create = localStrings.getString("commands.monitor.create");
        final String remove = localStrings.getString("commands.monitor.remove");
        
        final String header = String.format("%1$20s %2$27s", passiveCount, methodReadyCount);
        final String subHeader = String.format(displayFormat, low,high,current,low,high,
                                               current,create,remove);
        
        CLILogger.getInstance().printMessage(header);
        CLILogger.getInstance().printMessage(subHeader);

        if (fileName != null)
        {
            writeToFile(localStrings.getString("commands.monitor.stateful_session_write_to_file"));
        }
    }

    
    private void displayData(final StatefulSessionBeanStats stat)
    {
        final String data = String.format(displayFormat,
                                          stat.getPassiveCount().getLowWaterMark(),
                                          stat.getPassiveCount().getHighWaterMark(),
                                          stat.getPassiveCount().getCurrent(),
                                          stat.getMethodReadyCount().getLowWaterMark(),
                                          stat.getMethodReadyCount().getHighWaterMark(),
                                          stat.getMethodReadyCount().getCurrent(),
                                          stat.getCreateCount().getCount(),
                                          stat.getRemoveCount().getCount());
        CLILogger.getInstance().printMessage(data);
        if (fileName != null)
        {
            final String fileData = String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s,%8$s",
                                          stat.getPassiveCount().getLowWaterMark(),
                                          stat.getPassiveCount().getHighWaterMark(),
                                          stat.getPassiveCount().getCurrent(),
                                          stat.getMethodReadyCount().getLowWaterMark(),
                                          stat.getMethodReadyCount().getHighWaterMark(),
                                          stat.getMethodReadyCount().getCurrent(),
                                          stat.getCreateCount().getCount(),
                                          stat.getRemoveCount().getCount());
            writeToFile(fileData);
        }
    }

    
    public void displayDetails()
    {
        final String details = localStrings.getString("commands.monitor.stateful_session_detail");
        CLILogger.getInstance().printMessage(details);
    }


}
    
    

