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


