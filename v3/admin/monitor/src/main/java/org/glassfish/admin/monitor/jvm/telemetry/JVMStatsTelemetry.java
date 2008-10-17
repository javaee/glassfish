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

package org.glassfish.admin.monitor.jvm.telemetry;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
//import com.sun.enterprise.util.i18n.StringManager;

import java.lang.reflect.Method;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.factory.TreeNodeFactory;
import org.glassfish.flashlight.statistics.Counter;
import org.glassfish.flashlight.statistics.factory.CounterFactory;
//import org.glassfish.admin.monitor.jvm.statistics.V2JVMStats;

public class JVMStatsTelemetry {
    
    private TreeNode jvmNode;
    
    //private Counter commitHeapSize = CounterFactory.createCount();
    private Logger logger;
    private boolean isEnabled = true;
    
    /** Creates a new instance of JVMStatsTelemetry */
    public JVMStatsTelemetry(TreeNode server, Logger logger) {
        try {
            this.logger = logger;

            // jvm node
            jvmNode = TreeNodeFactory.createTreeNode("jvm", null, "jvm");
            server.addChild(jvmNode);

            jvmNode.addChild(createClassLoadingSystemNode());
            jvmNode.addChild(createCompilationSystemNode());
            jvmNode.addChild(createGarbageCollectorsNode());
            jvmNode.addChild(createMemoryNode());
            jvmNode.addChild(createOperatingSystemNode());
            jvmNode.addChild(createRuntimeNode());
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(JVMStatsTelemetry.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(JVMStatsTelemetry.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(JVMStatsTelemetry.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(JVMStatsTelemetry.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(JVMStatsTelemetry.class.getName()).log(Level.SEVERE, null, ex);
        }
    }            
    
    public void enableMonitoring(boolean flag) {
        if (isEnabled != flag) {
            isEnabled = flag;
            jvmNode.setEnabled(flag);
        }
    }
    
    //public Counter getCommittedHeapSize() {
    //    commitHeapSize.setCount(heapUsage.getCommitted());
    //    return commitHeapSize;
    //}

    public boolean isEnabled() {
        return isEnabled;
    }
    
    private TreeNode createClassLoadingSystemNode() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        TreeNode classLoadingSystemNode = TreeNodeFactory.createTreeNode("class-loading-system", null, "jvm");
        ClassLoadingMXBean clBean = ManagementFactory.getClassLoadingMXBean();
        String[] mList = {"getLoadedClassCount", "getTotalLoadedClassCount", "getUnloadedClassCount"};
        for (String methodName : mList) {
            Method method = ClassLoadingMXBean.class.getMethod(methodName);
            //String nodeName = createNodeName(methodName);
            String nodeName = createNodeName(methodName, (method.getReturnType()).isAssignableFrom(String.class) );
            TreeNode tn = 
                    TreeNodeFactory.createMethodInvoker(nodeName, clBean, "jvm", method);
            classLoadingSystemNode.addChild(tn);
        }
        return classLoadingSystemNode;
    }
    
    private TreeNode createCompilationSystemNode() throws NoSuchMethodException {
        TreeNode compilationSystemNode = TreeNodeFactory.createTreeNode("compilation-system", null, "jvm");
        CompilationMXBean compBean = ManagementFactory.getCompilationMXBean();
        String[] mList = {"getName", "getTotalCompilationTime"};
        for (String methodName : mList) {
            Method method = CompilationMXBean.class.getMethod(methodName);
            //String nodeName = createNodeName(methodName);
            String nodeName = createNodeName(methodName, (method.getReturnType()).isAssignableFrom(String.class) );
            TreeNode tn = 
                    TreeNodeFactory.createMethodInvoker(nodeName, compBean, "jvm", method);
            compilationSystemNode.addChild(tn);
        }
        return compilationSystemNode;
    }
    
    private TreeNode createGarbageCollectorsNode() throws NoSuchMethodException {
        TreeNode garbageCollectorsNode = TreeNodeFactory.createTreeNode("garbage-collectors", null, "jvm");
        List<GarbageCollectorMXBean> gcBeanList = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeanList) {
            TreeNode gcNode = TreeNodeFactory.createTreeNode(gcBean.getName(), null, "garbage-collectors");
            String[] mList = {"getCollectionCount", "getCollectionTime"};
            for (String methodName : mList) {
                Method method = GarbageCollectorMXBean.class.getMethod(methodName);
                //String nodeName = createNodeName(methodName);
                String nodeName = createNodeName(methodName, (method.getReturnType()).isAssignableFrom(String.class) );
                TreeNode tn = 
                        TreeNodeFactory.createMethodInvoker(nodeName, gcBean, "jvm", method);
                gcNode.addChild(tn);
            }
            garbageCollectorsNode.addChild(gcNode);
        }
        return garbageCollectorsNode;
    }
    
    private TreeNode createMemoryNode() throws NoSuchMethodException {
        TreeNode memoryNode = TreeNodeFactory.createTreeNode("memory", null, "jvm");
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        createMemoryUsageNodes(memBean.getHeapMemoryUsage(), "Heap", memoryNode);
        createMemoryUsageNodes(memBean.getNonHeapMemoryUsage(), "NonHeap", memoryNode);
        Method method = MemoryMXBean.class.getMethod("getObjectPendingFinalizationCount", (Class[]) null);
        TreeNode tn = 
                    TreeNodeFactory.createMethodInvoker("objectpendingfinalizationcount-count", memBean, "jvm", method);
            memoryNode.addChild(tn);
        return memoryNode;
    }
    
    private void createMemoryUsageNodes(MemoryUsage memUsage, String type, TreeNode memoryNode) throws NoSuchMethodException {
        String[] mList = {"getCommitted", "getInit" , "getMax", "getUsed"};
        for (String methodName : mList) {
            Method method = memUsage.getClass().getMethod(methodName, (Class[]) null);
            String nodeName = createMemUsageNodeName(methodName, type, (method.getReturnType()).isAssignableFrom(String.class) );
            TreeNode tn = 
                    TreeNodeFactory.createMethodInvoker(nodeName, memUsage, "jvm", method);
            memoryNode.addChild(tn);
        }
    }
    
    private String createMemUsageNodeName(String methodName, String type, boolean isString) {
        //return createNodeName(methodName) + type + "Size";
        return createNodeName(methodName+type+"size", isString);
    }
    
    private TreeNode createOperatingSystemNode() throws NoSuchMethodException {
        TreeNode operatingSystemNode = TreeNodeFactory.createTreeNode("operating-system", null, "jvm");
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        String[] mList = {"getArch", "getAvailableProcessors", "getName", "getVersion"};
        for (String methodName : mList) {
            Method method = OperatingSystemMXBean.class.getMethod(methodName, (Class[]) null);
            //String nodeName = createNodeName(methodName);
            String nodeName = createNodeName(methodName, (method.getReturnType()).isAssignableFrom(String.class) );
            TreeNode tn = 
                    TreeNodeFactory.createMethodInvoker(nodeName, osBean, "jvm", method);
            operatingSystemNode.addChild(tn);
        }
        return operatingSystemNode;
    }
    
    private TreeNode createRuntimeNode() throws NoSuchMethodException {
        TreeNode runtimeNode = TreeNodeFactory.createTreeNode("runtime", null, "jvm");
        RuntimeMXBean rtBean = ManagementFactory.getRuntimeMXBean();
        String[] mList = {"getBootClassPath", "getClassPath", "getInputArguments", "getLibraryPath",
            "getManagementSpecVersion", "getName", "getSpecName", "getSpecVendor",
            "getSpecVersion", "getUptime", "getVmName", "getVmVendor", "getVmVersion"};
        for (String methodName : mList) {
            Method method = RuntimeMXBean.class.getMethod(methodName, (Class[]) null);
            //String nodeName = createNodeName(methodName);
            boolean isString = false;
            if ( (method.getReturnType()).isAssignableFrom(String.class) ) {
                isString = true;
            } else if ( (method.getReturnType()).isAssignableFrom(java.util.List.class)) {
                isString = true;
            }
            String nodeName = createNodeName(methodName, isString);
            TreeNode tn = 
                    TreeNodeFactory.createMethodInvoker(nodeName, rtBean, "jvm", method);
            runtimeNode.addChild(tn);
        }


        return runtimeNode;
    }
    
    private String createNodeName(String methodName, boolean isString) {
        if (isString) {
            return methodName.substring(3).toLowerCase() + "-current";
        } else {
            return methodName.substring(3).toLowerCase() + "-count";
        }
        //return methodName.substring(3,4).toLowerCase() + methodName.substring(4);
    }
    
    /*
    public Counter getCommittedNonHeapSize() {
        commitNonHeapSize.setCount(nonheapUsage.getCommitted());
        return (Counter)commitNonHeapSize.unmodifiableView();
    }
    
    public Counter getInitHeapSize() {
        initHeapSize.setCount(heapUsage.getInit());
        return (CountStatistic)initHeapSize.unmodifiableView();
    }
    
    public CountStatistic getInitNonHeapSize() {
        initNonHeapSize.setCount(nonheapUsage.getCommitted());
        return (CountStatistic)initNonHeapSize.unmodifiableView();
    }
    
    public CountStatistic getMaxHeapSize() {
        maxHeapSize.setCount(heapUsage.getMax());
        return (CountStatistic)maxHeapSize.unmodifiableView();
    }
    
    public CountStatistic getMaxNonHeapSize() {
        maxNonHeapSize.setCount(nonheapUsage.getInit());
        return (CountStatistic)maxNonHeapSize.unmodifiableView();
    }
    
    public CountStatistic getObjectPendingFinalizationCount() {
        objPendingCount.setCount(bean.getObjectPendingFinalizationCount());
        return (CountStatistic)objPendingCount.unmodifiableView();
    }
    
    public CountStatistic getUsedHeapSize() {
        usedHeapSize.setCount(heapUsage.getUsed());
        return (CountStatistic)usedHeapSize.unmodifiableView();
    }
    
    public CountStatistic getUsedNonHeapSize() {
        usedNonHeapSize.setCount(nonheapUsage.getUsed());
        return (CountStatistic)usedNonHeapSize.unmodifiableView();
    }
    */
}
