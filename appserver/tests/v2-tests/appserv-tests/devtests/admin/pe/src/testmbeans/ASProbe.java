/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

/*
 * ASProbe.java
 *
 * Created on March 29, 2006, 4:44 PM
 */
package testmbeans;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import javax.management.*;

/**
 * Class ASProbe
 * ASProbe Description
 * @author bnevins
 */
public class ASProbe implements ASProbeMBean
{
    public ASProbe()
    {
        refresh();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public long getHeapUsage()
    {
        refresh();
        return heapUsage;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public long getHeapMax()
    {
        refresh();
        return heapMax;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public int getThreadCount()
    {
        refresh();
        return threadCount;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public int getPeakThreadCount()
    {
        refresh();
        return peakThreadCount;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public int getNumApps()
    {
        refresh();
        return numApps;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public int getNumEJBModules()
    {
        refresh();
        return numEJB;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public int getNumWebModules()
    {
        refresh();
        return numWeb;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public int getNumComponents()
    {
        refresh();
        return numComponents;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public String[] getAppNames()
    {
        refresh();
        return appNames;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public String[] getWebModuleNames()
    {
        refresh();
        return webNames;
    }

    ///////////////////////////////////////////////////////////////////////////
    
    private void refresh()
    {
        long now = System.currentTimeMillis();
        
        if(now - timestamp < 2000) // no more often than once per 2 seconds
            return;
        
        timestamp = now;

        try
        {
            MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
            
            MemoryMXBean mmb = ManagementFactory.newPlatformMXBeanProxy(mbsc,
                ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);
            
            ThreadMXBean tmb = ManagementFactory.newPlatformMXBeanProxy(mbsc,
                ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);
            
            MemoryUsage mu = mmb.getHeapMemoryUsage();
            heapUsage = mu.getCommitted();
            heapMax = mu.getMax();
            
            peakThreadCount = tmb.getPeakThreadCount();
            threadCount = tmb.getThreadCount();
            
            final ObjectName on         = new ObjectName(BACKEND_MBEAN_ON);
            final Object[] operParams   = new Object[0];
            final String[] operSign     = new String[0];

            String oper           = "getAllDeployedJ2EEApplications";
            ObjectName[] names = (ObjectName[])mbsc.invoke(on, oper, operParams, operSign);
            numApps = names.length;
            
            oper = "getAllDeployedEJBModules";
            names = (ObjectName[])mbsc.invoke(on, oper, operParams, operSign);
            numEJB = names.length;
            
            oper = "getAllDeployedWebModules";
            names = (ObjectName[])mbsc.invoke(on, oper, operParams, operSign);
            numWeb = names.length;
            
            oper = "getAllDeployedComponents";
            names = (ObjectName[])mbsc.invoke(on, oper, operParams, operSign);
            numComponents = names.length;
            
            oper = "getJ2eeApplicationNamesList";
            appNames = (String[])mbsc.invoke(on, oper, operParams, operSign);

            oper = "getWebModuleNamesList";
            webNames = (String[])mbsc.invoke(on, oper, operParams, operSign);
        }
        catch(Exception e)
        {
            numApps = -1;
            heapUsage = -1;
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private long        timestamp;
    private int         numApps;
    private int         numEJB;
    private int         numWeb;
    private int         numComponents;
    private long        heapUsage;
    private long        heapMax;
    private int         peakThreadCount;
    private int         threadCount;
    private String[]    appNames;
    private String[]    webNames;
    private static final String BACKEND_MBEAN_ON = "com.sun.appserv:category=config,type=applications";
}


