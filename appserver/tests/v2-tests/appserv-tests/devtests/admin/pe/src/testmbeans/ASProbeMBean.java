/*
 * ASProbeMBean.java
 *
 * Created on March 29, 2006, 4:44 PM
 */
package testmbeans;

/**
 * Interface ASProbeMBean
 * ASProbe Description
 * @author bnevins
 */
public interface ASProbeMBean
{
    /**
     * Get Amount of memory allocated in JVM
     */
    public long     getHeapUsage();
    public int      getNumApps();
    public int      getNumEJBModules();
    public int      getNumWebModules();
    public int      getNumComponents();
    public int      getThreadCount();
    public int      getPeakThreadCount();
    public String[] getAppNames();
    public String[] getWebModuleNames();
}


