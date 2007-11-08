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

package com.sun.enterprise.cli.commands;

import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.cli.commands.monitor.*;
import javax.management.MBeanServerConnection;
import com.sun.appserv.management.monitor.ServerRootMonitor;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.DomainRoot;
import java.util.Map;
import java.io.File;
import java.util.TimerTask;
import java.util.Timer;
import java.io.File;


/**
 *  This class is the implementation for monitor command.
 *  @author <a href="mailto:jane.young@sun.com">Jane Young</a>
 *  @version $Revision: 1.3 $
 **/

public class MonitorCommand extends S1ASCommand
{
    private static final String MONITOR_TYPE = "type";
    private static final String INTERVAL = "interval";
    private static final String FILTER = "filter";
    private static final String FILENAME = "filename";
    public static String[] validTypes = {"httplistener", "keepalive", "filecache",
                                         "connectionqueue", "jdbcpool", "jvm",
                                         "threadpool", "servlet", "connection",
                                         "connectorpool", "endpoint", "entitybean",
                                         "messagedriven","statefulsession",
                                         "statelesssession", "httpservice", "webmodule"};
    
    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
        validateOptions();
        final String host         = getHost();
        final int port            = getPort();
        final String user         = getUser();
        final String password     = getPassword();
        final boolean verbose     = !getBooleanOption(TERSE);
        final String monitorType  = getOption(MONITOR_TYPE);
        final long interval       = Long.parseLong(getOption(INTERVAL)) * 1000;
        final String instanceName = (String) getOperands().get(0);
        final String filter       = getOption(FILTER);
        File fileName             = getFileName();
        
        go(user, password, host, port, verbose, fileName, monitorType, interval,
              instanceName, filter);
    }


    private void go(final String user, final String password, final String host,
                   final int port, final boolean verbose, final File fileName,
                   final String monitorType, final long interval,
                   final String instanceName, final String filterName)
        throws CommandException
    {
        Timer timer = new Timer();
        try
        {
                //use http connector
            MBeanServerConnection mbsc = getMBeanServerConnection(host, port, user, password);
            final DomainRoot domainRoot = ProxyFactory.getInstance(mbsc).getDomainRoot();
        
            verifyTargetInstance(domainRoot, instanceName);
            final ServerRootMonitor srm = getServerRootMonitor(domainRoot, instanceName);
            MonitorTask monitorTask = getTask(monitorType, srm, filterName, timer, verbose, fileName);
            timer.scheduleAtFixedRate(monitorTask, 0, interval);

            boolean done = false;
                // detect if a q or Q key is entered
            while (!done)
            {
                final char c = new CliUtil().getKeyboardInput();
                final String str = Character.toString(c);
                if (str.equals("q") || str.equals("Q"))
                {
                    timer.cancel();
                    done = true;
                }
                else if (str.equals("h") || str.equals("H"))
                {
                    monitorTask.displayDetails();
                }
                
            }
        }
        catch(MonitorTaskException mte) {
            timer.cancel();
            displayExceptionMessage(mte);            
        }
        catch(Exception e)
        {
            displayExceptionMessage(e);
        }
    }


    private File getFileName()
    {
        File fileName = null;
        if (getOption(FILENAME) != null)
        {
            fileName = new File(getOption(FILENAME));
            if (fileName.isDirectory())
            {
                final File tempFile = fileName;
                fileName = new File(tempFile, "monitor.log");
            }
        }
        return fileName;
    }


        /**
         * verify that the instanceName is an instance
         * @param domainRoot
         * @param instanceName to verify
         * @exception CommandException
         **/
    private void verifyTargetInstance(final DomainRoot domainRoot, final String instanceName) 
        throws CommandException
    {
        boolean isServer=false;
        isServer = domainRoot.getDomainConfig().getStandaloneServerConfigMap().keySet().contains(instanceName);
        if (! isServer ) 
        {
            throw new CommandException(getLocalizedString("TargetNotAnInstance", 
                                                 new Object[] {instanceName}));
        }
    }


    private ServerRootMonitor getServerRootMonitor(final DomainRoot domainRoot, final String instanceName)
        throws CommandException
    {
        Map<String,ServerRootMonitor> serverRootMonitorMap = 
                domainRoot.getMonitoringRoot().getServerRootMonitorMap();
        ServerRootMonitor serverRootMonitor = serverRootMonitorMap.get(instanceName);
        if (serverRootMonitor == null) {
                //***** NEED TO I18N THIS MSG  *****//
            throw new CommandException("Unable to monitoring" + instanceName);
        }
        return serverRootMonitor;
    }

    
    private MonitorTask getTask(final String type, final ServerRootMonitor srm,
                                final String filterName, final Timer timer, final boolean verbose,
                                final File fileName)
        throws MonitorTaskException
    {
        MonitorTask monitorTask = null;
        if (type.equals("jvm") )
            monitorTask = new JVMMonitorTask(srm, filterName, timer, verbose, fileName);
        else if (type.equals("jdbcpool"))
            monitorTask = new JDBCPoolMonitorTask(srm, filterName, timer, verbose, fileName);
        else if (type.equals("connectorpool"))
            monitorTask = new ConnectorPoolMonitorTask(srm, filterName, timer, verbose, fileName);
        else if (type.equals("threadpool"))
            monitorTask = new ThreadPoolMonitorTask(srm, filterName, timer, verbose, fileName);
        else if (type.equals("connectionqueue"))
            monitorTask = new ConnectionQueueMonitorTask(srm, filterName, timer, verbose, fileName);
        else if (type.equals("filecache"))
            monitorTask = new FileCacheMonitorTask(srm, filterName, timer, verbose, fileName);
        else if (type.equals("keepalive"))
            monitorTask = new KeepAliveMonitorTask(srm, filterName, timer, verbose, fileName);
        else if (type.equals("httplistener"))
            monitorTask = new HttpListenerMonitorTask(srm, filterName, timer, verbose, fileName);
        else if (type.equals("httpservice"))
            monitorTask = new HttpServiceVirtualServerMonitorTask(srm, filterName, timer, verbose, fileName);
        else if (type.equals("connection"))
            monitorTask = new ConnectionManagerMonitorTask(srm, filterName, timer, verbose, fileName);
        else if (type.equals("webmodule"))
            monitorTask = new WebModuleVirtualServerMonitorTask(srm, filterName, timer, verbose, fileName);
        else if (type.equals("servlet"))
            monitorTask = new ServletMonitorTask(srm, filterName, timer, verbose, fileName);
        else if (type.equals("endpoint"))
            monitorTask = new WebServiceEndpointMonitorTask(srm, filterName, timer, verbose, fileName);
        else if (type.equals("entitybean"))
            monitorTask = new EntityBeanMonitorTask(srm, filterName, timer, verbose, fileName);
        else if (type.equals("messagedriven"))
            monitorTask = new MessageDrivenBeanMonitorTask(srm, filterName, timer, verbose, fileName);
        else if (type.equals("statefulsession"))
            monitorTask = new StatefulSessionBeanMonitorTask(srm, filterName, timer, verbose, fileName);
        else if (type.equals("statelesssession"))
            monitorTask = new StatelessSessionBeanMonitorTask(srm, filterName, timer, verbose, fileName);
        else {
            StringBuffer sb = new StringBuffer();
            sb.append(getLocalizedString("InvalidMonitorType", new Object[] {type}));
            for (String validType : validTypes)
            {
                    //if first element, then do not append ","
                if (validType.equals(validTypes[0]))
                    sb.append(" " + validType);
                else 
                    sb.append(", "+validType);
            }
            sb.append(".");
            throw new MonitorTaskException(sb.toString());
        }
        return monitorTask;
    }
}
