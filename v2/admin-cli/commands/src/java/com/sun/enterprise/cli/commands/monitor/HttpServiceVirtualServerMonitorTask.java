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
import java.util.Map;
import java.util.Timer;
import java.io.File;

public class HttpServiceVirtualServerMonitorTask extends MonitorTask
{
    private final String displayFormat = "%1$-20s %2$-10s %3$-20s %4$-10s";

    public HttpServiceVirtualServerMonitorTask(final ServerRootMonitor srm, final String filter, final Timer timer,
                                               final boolean verbose, final File fileName)
        throws MonitorTaskException
    {
        super(srm, filter, timer, verbose, fileName);

        final HTTPServiceMonitor httpserviceMonitor = srm.getHTTPServiceMonitor();
        if (httpserviceMonitor == null) 
            throw new MonitorTaskException("Unable to monitor virtualserver");
        final Map<String,HTTPServiceVirtualServerMonitor> httpServiceMap = httpserviceMonitor.getHTTPServiceVirtualServerMonitorMap();
        
        if (httpServiceMap == null)
            throw new MonitorTaskException(localStrings.getString("commands.monitor.unable_to_monitor_virtual_server"));
        
        final String[] keys = MapUtil.getKeyStrings(httpServiceMap);
        if (this.filter == null)
        {
            checkForNumberOfElements(keys);
            this.filter = keys[0];
        }
        else {
            if (!httpServiceMap.containsKey(this.filter)) {
                throw new MonitorTaskException(localStrings.getString("commands.monitor.does_not_exist", new Object[] {this.filter}));
            }
        }
        final String virtualServerTitle=localStrings.getString("commands.monitor.virtual_server_title", new Object[] {this.filter});
        final String title = String.format("%1$50s", virtualServerTitle);
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
        
        final Map<String,HTTPServiceVirtualServerMonitor> httpServiceMap = httpserviceMonitor.getHTTPServiceVirtualServerMonitorMap();
        if (httpServiceMap == null || httpServiceMap.size()<1) {
            cancelMonitorTask();
            return;
        }
        
        final HTTPServiceVirtualServerMonitor httpServiceMonitor = httpServiceMap.get(filter);
        if (httpServiceMonitor == null) {
            cancelMonitorTask();
            return;
        }
            
        final HTTPServiceVirtualServerStats httpServiceStats = httpServiceMonitor.getHTTPServiceVirtualServerStats();
        if (verbose && counter == NUM_ROWS)
        {
            displayHeader();
            counter = 0;  //reset to 0
        }
        displayData(httpServiceStats);
        if (verbose) counter++;
    }

    
    private void displayHeader()
    {
        final String hosts = localStrings.getString("commands.monitor.hosts");
        final String id = localStrings.getString("commands.monitor.id");
        final String interfaces = localStrings.getString("commands.monitor.interfaces");
        final String mode = localStrings.getString("commands.monitor.mode");
        
        final String header = String.format(displayFormat, hosts,id,interfaces,mode);
        CLILogger.getInstance().printMessage(header);
        if (fileName != null)
        {
            writeToFile(hosts+","+id+","+interfaces+","+mode);            
        }
    }

    
    private void displayData(final HTTPServiceVirtualServerStats vss)
    {
        final String data = String.format(displayFormat,
                                          vss.getHosts().getCurrent(),
                                          vss.getID().getCurrent(),
                                          vss.getInterfaces().getCurrent(),
                                          vss.getMode().getCurrent());
        CLILogger.getInstance().printMessage(data);
        if (fileName != null)
        {
            final String fileData = String.format("%1$s,%2$s,%3$s,%4$s,",
                                          vss.getHosts().getCurrent(),
                                          vss.getID().getCurrent(),
                                          vss.getInterfaces().getCurrent(),
                                          vss.getMode().getCurrent());
            writeToFile(fileData);
        }
    }

    
    public void displayDetails()
    {
        final String details = localStrings.getString("commands.monitor.virtual_server_detail");
        CLILogger.getInstance().printMessage(details);
    }


}
    
    

