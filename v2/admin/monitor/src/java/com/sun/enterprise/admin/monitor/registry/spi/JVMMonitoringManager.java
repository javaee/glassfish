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

package com.sun.enterprise.admin.monitor.registry.spi;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.management.ManagementFactory;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.management.ThreadInfo;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import javax.management.j2ee.statistics.Stats;
import com.sun.enterprise.admin.monitor.stats.*;
import com.sun.enterprise.admin.monitor.registry.MonitoredObjectType;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener;
import com.sun.enterprise.admin.monitor.registry.spi.MonitoringRegistrationHelper;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistrationException;
import com.sun.enterprise.admin.monitor.registry.StatsHolder;
import com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl;
import com.sun.enterprise.admin.monitor.registry.spi.reconfig.MonitoringConfigurationHandler;
import com.sun.enterprise.admin.monitor.stats.spi.JVMCompilationStatsImpl;
import com.sun.enterprise.admin.monitor.stats.spi.JVMClassLoadingStatsImpl;
import com.sun.enterprise.admin.monitor.stats.spi.JVMRuntimeStatsImpl;
import com.sun.enterprise.admin.monitor.stats.spi.JVMOperatingSystemStatsImpl;
import com.sun.enterprise.admin.monitor.stats.spi.JVMGarbageCollectorStatsImpl;
import com.sun.enterprise.admin.monitor.stats.spi.JVMMemoryStatsImpl;
import com.sun.enterprise.admin.monitor.stats.spi.JVMThreadStatsImpl;
import com.sun.enterprise.admin.monitor.stats.spi.JVMThreadInfoStatsImpl;


/**
 * Manager class responsible for registration/unregistration of all the
 * JVM1.5 Stats. In addition, this class is responsible for handling the
 * monitoring level change events pertaining to the JVM
 */

public class JVMMonitoringManager implements MonitoringLevelListener {
    
    private static final Logger logger = Logger.getLogger(AdminConstants.kLoggerName);    
    private static final JVMMonitoringManager jmm = new JVMMonitoringManager();
    private final int STACK_DEPTH = 5;
    private final String THREAD_NODE_NAME = "thread";
    
    /** Creates a new instance of JVMMonitoringManager */
    private JVMMonitoringManager() {
    }
    
    public static JVMMonitoringManager getInstance() {
        return jmm;
    }
    
    /**
     * Method that handles the registration of all the
     * stats for various subsystems of the JVM
     */
    public void registerStats(StatsHolder rootNode) {
        MonitoringLevel level = MonitoringConfigurationHandler.getLevel(MonitoredObjectType.JVM);
        // check monitoring level of JVM before registering
        if(level != MonitoringLevel.OFF) {
            try {
                registerJVMCompilationStats(rootNode);
                registerJVMClassLoadingStats(rootNode);
                registerJVMRuntimeStats(rootNode);
                registerJVMOperatingSystemStats(rootNode);
                registerJVMGarbageCollectorStats(rootNode);
                registerJVMMemoryStats(rootNode);
                registerJVMThreadStats(rootNode);
                if(level == MonitoringLevel.HIGH)
                    registerJVMThreadInfoStats(rootNode);
            } catch(MonitoringRegistrationException mre) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("MonitoringRegistrationException in the registration of JVM1.5 Stats: "+ mre.getLocalizedMessage());
                }
            } catch(Exception e) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Exception in the registration of JVM 1.5 Stats: "+ e.getLocalizedMessage());
                }
            }
        }
    }

    //All the register/unregister methods for various subsystems of 
    //the JVM
    
    /*
     * Register the JVMCompilationStats with the monitoring registry
     * @param   rootNode    the root node of the monitoring hierarchy
     * @throws MonitoringRegistrationException
     */
    public void registerJVMCompilationStats(StatsHolder rootNode) throws
           MonitoringRegistrationException {

        final StatsHolder jvmNode = rootNode.getChild(MonitoredObjectType.JVM.getTypeName());
        assert(jvmNode != null): "jvm node is null";
        StatsHolder childNode = jvmNode.addChild(MonitoredObjectType.JVM_COMPILATION.getTypeName(), 
                                                 MonitoredObjectType.JVM_COMPILATION);
        childNode.setStats(new JVMCompilationStatsImpl());
        childNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.JVMCompilationStats.class);
        childNode.setDottedName(DottedNameFactory.getJVMCompilationDottedName());
        childNode.setObjectName(MonitoringObjectNames.getJVMCompilationObjectName());
        childNode.registerMBean();
    }
           
    /*
     * Register the JVMCClassLoadingStats with the monitoring registry
     * @param   rootNode    the root node of the monitoring hierarchy
     * @throws MonitoringRegistrationException
     */
    public void registerJVMClassLoadingStats(StatsHolder rootNode)
           throws MonitoringRegistrationException {
               
        final StatsHolder jvmNode = rootNode.getChild(MonitoredObjectType.JVM.getTypeName());
        assert(jvmNode != null): "jvm node is null";
        StatsHolder childNode = jvmNode.addChild(MonitoredObjectType.JVM_CLASSLOADING.getTypeName(), 
                                                 MonitoredObjectType.JVM_CLASSLOADING);
        childNode.setStats(new JVMClassLoadingStatsImpl());
        childNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.JVMClassLoadingStats.class);
        childNode.setDottedName(DottedNameFactory.getJVMClassLoadingDottedName());
        childNode.setObjectName(MonitoringObjectNames.getJVMClassLoadingObjectName());
        childNode.registerMBean();
    }
            
    /*
     * Register the JVMRuntimeStats with the monitoring registry
     * @param   rootNode    the root node of the monitoring hierarchy
     * @throws MonitoringRegistrationException
     */
    public void registerJVMRuntimeStats(StatsHolder rootNode) throws 
           MonitoringRegistrationException {
     
        final StatsHolder jvmNode = rootNode.getChild(MonitoredObjectType.JVM.getTypeName());
        assert(jvmNode != null): "jvm node is null";
        StatsHolder childNode = jvmNode.addChild(MonitoredObjectType.JVM_RUNTIME.getTypeName(), 
                                                 MonitoredObjectType.JVM_RUNTIME);
        childNode.setStats(new JVMRuntimeStatsImpl());
        childNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.JVMRuntimeStats.class);
        childNode.setDottedName(DottedNameFactory.getJVMRuntimeDottedName());
        childNode.setObjectName(MonitoringObjectNames.getJVMRuntimeObjectName());
        childNode.registerMBean();
    }
    
    
    /*
     * Register the JVMOperatingSystemStats with the monitoring registry
     * @param   rootNode    the root node of the monitoring hierarchy
     * @throws MonitoringRegistrationException
     */
    public void registerJVMOperatingSystemStats(StatsHolder rootNode) 
           throws MonitoringRegistrationException {
    
        final StatsHolder jvmNode = rootNode.getChild(MonitoredObjectType.JVM.getTypeName());
        assert(jvmNode != null): "jvm node is null";
        StatsHolder childNode = jvmNode.addChild(MonitoredObjectType.JVM_OS.getTypeName(), 
                                                 MonitoredObjectType.JVM_OS);
        childNode.setStats(new JVMOperatingSystemStatsImpl());
        childNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.JVMOperatingSystemStats.class);
        childNode.setDottedName(DottedNameFactory.getJVMOSDottedName());
        childNode.setObjectName(MonitoringObjectNames.getJVMOSObjectName());
        childNode.registerMBean();
    }
    
    /*
     * Unregister the from the monitoring registry
     * @param   rootNode    root node of the monitoring hierarchy
     * @param   includeThreadInfo   boolean to indicate if the threadinfo stats
     *                              have to unregistered
     * @throws	MonitoringRegistrationException
     */
    public void unregisterStats(StatsHolder rootNode, boolean includeThreadInfo) throws 
           MonitoringRegistrationException {
        
        StatsHolder jvmNode = rootNode.getChild(MonitoredObjectType.JVM.getTypeName());
        assert(jvmNode != null):"jvm node is null";
        Collection c = new ArrayList();
        c.addAll(jvmNode.getAllChildren());
        Iterator iter = c.iterator();
        while(iter.hasNext()) {
            StatsHolder childNode = (StatsHolder)iter.next();
            
            if(childNode.getType() == MonitoredObjectType.JVM_GCS) {
                Collection c1 = new ArrayList();
                c1.addAll(childNode.getAllChildren());
                Iterator it = c1.iterator();
                while(it.hasNext()) {
                    StatsHolder s = (StatsHolder)it.next();
                    s.unregisterMBean();
                    childNode.removeChild(s.getName());
                }
            }
            
            if((childNode.getType() == MonitoredObjectType.JVM_THREAD) && includeThreadInfo) {
                Collection c1 = new ArrayList();
                c1.addAll(childNode.getAllChildren());
                Iterator it = c1.iterator();
                while(it.hasNext()) {
                    StatsHolder s = (StatsHolder)it.next();
                    s.unregisterMBean();
                    childNode.removeChild(s.getName());
                }
            }
            
            childNode.unregisterMBean();
            jvmNode.removeChild(childNode.getName());
        }
    }
    
    /**
     * Register the JVMGarbageCollectorStats with the monitoring registry
     * @param   rootNode    the root node of the monitoring hierarchy
     * @throws MonitoringRegistrationException
     */     
    public void registerJVMGarbageCollectorStats(StatsHolder rootNode) throws
           MonitoringRegistrationException {
               
        StatsHolder jvmNode = rootNode.getChild(MonitoredObjectType.JVM.getTypeName());
        assert(jvmNode != null):"jvm node is null";
        // setup the parent node for all garbage collectors
        StatsHolder gcsNode = jvmNode.addChild(MonitoredObjectType.JVM_GCS.getTypeName(), 
                                               MonitoredObjectType.JVM_GCS);
        gcsNode.setDottedName(DottedNameFactory.getJVMGCSDottedName());
        gcsNode.setObjectName(MonitoringObjectNames.getJVMGCSSObjectName());
        gcsNode.registerMBean();
        // now register stats for each garbage colelctor
        List beanList = ManagementFactory.getGarbageCollectorMXBeans();
        Iterator iter = beanList.iterator();
        while(iter.hasNext()) {
            GarbageCollectorMXBean bean = (GarbageCollectorMXBean) iter.next();
            StatsHolder gcNode = gcsNode.addChild(bean.getName(), MonitoredObjectType.JVM_GC);
            gcNode.setStats(new JVMGarbageCollectorStatsImpl(bean));
            gcNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.JVMGarbageCollectorStats.class);
            gcNode.setDottedName(DottedNameFactory.getJVMGCDottedName(bean.getName()));
            gcNode.setObjectName(MonitoringObjectNames.getJVMGCObjectName(bean.getName()));
            gcNode.registerMBean();
        }
    }    
    
    /**
     * Register the JVMMemoryStats with the monitoring registry
     * @param   rootNode    the root node of the monitoring hierarchy
     * @throws MonitoringRegistrationException
     */
    public void registerJVMMemoryStats(StatsHolder rootNode) 
                                        throws MonitoringRegistrationException {
        
        final StatsHolder jvmNode = rootNode.getChild(MonitoredObjectType.JVM.getTypeName());
        assert(jvmNode != null): "jvm node is null";
        StatsHolder childNode = jvmNode.addChild(MonitoredObjectType.JVM_MEMORY.getTypeName(), 
                                                 MonitoredObjectType.JVM_MEMORY);
        childNode.setStats(new JVMMemoryStatsImpl());
        childNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.JVMMemoryStats.class);
        childNode.setDottedName(DottedNameFactory.getJVMMemoryDottedName());
        childNode.setObjectName(MonitoringObjectNames.getJVMMemoryObjectName());
        childNode.registerMBean();
    }
    
    public void registerJVMThreadStats(StatsHolder rootNode) 
                                        throws MonitoringRegistrationException {
        
        final StatsHolder jvmNode = rootNode.getChild(MonitoredObjectType.JVM.getTypeName());
        assert(jvmNode != null): "jvm node is null";
        StatsHolder childNode = jvmNode.addChild(MonitoredObjectType.JVM_THREAD.getTypeName(),
                                                 MonitoredObjectType.JVM_THREAD);
        childNode.setStats(new JVMThreadStatsImpl());
        childNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.JVMThreadStats.class);
        childNode.setDottedName(DottedNameFactory.getJVMThreadDottedName());
        childNode.setObjectName(MonitoringObjectNames.getJVMThreadObjectName());
        childNode.registerMBean();
    }
    
    public void registerJVMThreadInfoStats(StatsHolder rootNode) throws 
                                               MonitoringRegistrationException {
        
        final StatsHolder jvmNode = rootNode.getChild(MonitoredObjectType.JVM.getTypeName());
        assert(jvmNode != null): "jvm node is null";
        final StatsHolder threadSystemNode = jvmNode.getChild(MonitoredObjectType.JVM_THREAD.getTypeName());
        assert(threadSystemNode != null): "thread-system node is null";
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long ids[] = bean.getAllThreadIds();
        for(int i=0; i < ids.length; i++) {
            ThreadInfo info = bean.getThreadInfo(ids[i], STACK_DEPTH);
            String threadName = THREAD_NODE_NAME + "-" + ids[i];
            StatsHolder childNode = threadSystemNode.addChild(threadName, MonitoredObjectType.JVM_THREAD_INFO);
            childNode.setStats(new JVMThreadInfoStatsImpl(info));
            childNode.setStatsClass(com.sun.enterprise.admin.monitor.stats.JVMThreadInfoStats.class);
            childNode.setDottedName(DottedNameFactory.getJVMThreadInfoDottedName(threadName));
            childNode.setObjectName(MonitoringObjectNames.getJVMThreadInfoObjectName(threadName));
            childNode.registerMBean();
        }
    }
    
    public void unregisterJVMThreadInfoStats(StatsHolder rootNode) throws 
                                               MonitoringRegistrationException {
        
        final StatsHolder jvmNode = rootNode.getChild(MonitoredObjectType.JVM.getTypeName());
        assert(jvmNode != null): "jvm node is null";
        final StatsHolder threadSystemNode = jvmNode.getChild(MonitoredObjectType.JVM_THREAD.getTypeName());
        assert(threadSystemNode != null): "thread-system node is null";
        Collection c = new ArrayList();
        c.addAll(threadSystemNode.getAllChildren());
        Iterator iter = c.iterator();
        while(iter.hasNext()) {
            StatsHolder s = (StatsHolder)iter.next();
            s.unregisterMBean();
            threadSystemNode.removeChild(s.getName());
        }
    }
    
    private void registerAllStats(StatsHolder rootNode, boolean includeThreadInfo) {
        try {
            registerJVMCompilationStats(rootNode);
            registerJVMClassLoadingStats(rootNode);
            registerJVMRuntimeStats(rootNode);
            registerJVMOperatingSystemStats(rootNode);
            registerJVMGarbageCollectorStats(rootNode);
            registerJVMMemoryStats(rootNode);
            registerJVMThreadStats(rootNode);
            if(includeThreadInfo)
                registerJVMThreadInfoStats(rootNode);
        } catch(MonitoringRegistrationException mre) {
            logger.finest("MonitoringRegistrationException in the registration of JVM1.5 Stats: "+ mre.getLocalizedMessage());
        } catch(Exception e) {
            logger.finest("Exception in the registration of JVM 1.5 Stats: "+ e.getLocalizedMessage());
        }
    }
       
    
    // Methods to handle the  monitoringlevel changes
    public void changeLevel (MonitoringLevel from, 
                             MonitoringLevel to,
                             MonitoredObjectType type) {
        
        logger.finest("changeLevel being invoked on the JVM");
        final MonitoringRegistrationHelper registryImpl	= 
			(MonitoringRegistrationHelper) MonitoringRegistrationHelper.getInstance();
        
        StatsHolder root = registryImpl.getRootStatsHolder();
        boolean includeThreadInfo = false;
        
        if(to == MonitoringLevel.OFF) {
            if(from == MonitoringLevel.HIGH)
                includeThreadInfo = true;
            try {
                unregisterStats(root, includeThreadInfo);
            } catch(MonitoringRegistrationException mre) {
                logger.finest("MonitoringRegistrationException in the unregistration of JVM 1.5 Stats: "+ mre.getLocalizedMessage());
            } catch(Exception e) {
                logger.finest("Exception in the unregistration of JVM 1.5 Stats: "+ e.getLocalizedMessage());
            }
        }
        
        if(from == MonitoringLevel.OFF) {
            if(to == MonitoringLevel.HIGH)
                includeThreadInfo = true;
            registerAllStats(root, includeThreadInfo);
        }
        
        if(from == MonitoringLevel.LOW && to == MonitoringLevel.HIGH) {
            try {
                registerJVMThreadInfoStats(root);
            } catch(MonitoringRegistrationException mre) {
                logger.finest("MonitoringRegistrationException in the registration of JVM ThreadInfoStats: "+ mre.getLocalizedMessage());
            } catch(Exception e) {
                logger.finest("Exception in the registration of JVM ThreadInfo Stats: "+ e.getLocalizedMessage());
            }
        }
        
        if(from == MonitoringLevel.HIGH && to == MonitoringLevel.LOW) {
            try{
                unregisterJVMThreadInfoStats(root);
            } catch(MonitoringRegistrationException mre) {
                logger.finest("MonitoringRegistrationException in the unregistration of JVM ThreadInfoStats: "+ mre.getLocalizedMessage());
            } catch(Exception e) {
                logger.finest("Exception in the unregistration of JVM ThreadInfo Stats: "+ e.getLocalizedMessage());
            }
        }
            
    }
    
    public void changeLevel (MonitoringLevel from,
                             MonitoringLevel to, 
                             Stats handback) {
    }
    
    public void setLevel (MonitoringLevel level) {        
    }        
}
