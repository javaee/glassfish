/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.monitor;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import javax.management.ObjectName;
import javax.management.MBeanServer;


import org.glassfish.api.monitoring.ContainerMonitoring;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.factory.TreeNodeFactory;
import org.glassfish.gmbal.ManagedObjectManager;
import org.glassfish.gmbal.ManagedObjectManagerFactory;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManagerDelegate;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import com.sun.enterprise.config.serverbeans.*;
import java.lang.management.ManagementFactory;
import java.io.IOException;

import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Singleton;
import org.glassfish.admin.monitor.StatsProviderRegistry.StatsProviderRegistryElement;

import org.glassfish.external.amx.MBeanListener;
import org.glassfish.external.amx.AMXGlassfish;
import static org.glassfish.external.amx.AMX.*;

import org.glassfish.flashlight.provider.FlashlightProbe;
import org.glassfish.flashlight.provider.ProbeRegistry;

/**
 *
 * @author Jennifer
 */
@Scoped(Singleton.class)
public class StatsProviderManagerDelegateImpl extends MBeanListener.CallbackImpl implements StatsProviderManagerDelegate {
    protected ProbeClientMediator pcm;
    MonitoringService monitoringService = null;
    private final MonitoringRuntimeDataRegistry mrdr;
    private final ProbeRegistry probeRegistry;
    private final Domain domain;

    private final TreeNode serverNode;
    private static final ObjectName MONITORING_ROOT = AMXGlassfish.DEFAULT.monitoringRoot();
    static final ObjectName MONITORING_SERVER = AMXGlassfish.DEFAULT.serverMon( AMXGlassfish.DEFAULT.dasName() );
    private static final String DOMAIN = MONITORING_SERVER.getDomain();
    private static final String PP = MONITORING_SERVER.getKeyProperty( PARENT_PATH_KEY);
    private static final String TYPE = MONITORING_SERVER.getKeyProperty( TYPE_KEY);
    private static final String NAME = MONITORING_SERVER.getKeyProperty( NAME_KEY);
    private static final String PARENT_PATH = PP + "/" + TYPE + "[" + NAME + "]" ;
    private static final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
    private boolean AMXReady = false;
    private StatsProviderRegistry statsProviderRegistry;

    boolean ddebug = false;

    StatsProviderManagerDelegateImpl(ProbeClientMediator pcm, ProbeRegistry probeRegistry,
            MonitoringRuntimeDataRegistry mrdr, Domain domain, MonitoringService monitoringService) {
        this.pcm = pcm;
        this.mrdr = mrdr;
        this.domain = domain;
        this.monitoringService = monitoringService;
        this.probeRegistry = probeRegistry;
        //serverNode is special, construct that first if doesn't exist
        serverNode = constructServerPP();
        statsProviderRegistry = new StatsProviderRegistry(mrdr);
    }

    public void register(String configElement, PluginPoint pp,
            String subTreePath, Object statsProvider) {
        // register the statsProvider
        printd("registering a statsProvider ");
        StatsProviderRegistryElement spre;
        // First check if the configElement associated for statsProvider is 'ON'
        if (getMonitoringEnabled() && getEnabledValue(configElement)) {
            printd(" enabled is true ");
            spre = statsProviderRegistry.getStatsProviderRegistryElement(statsProvider);

            if (spre == null) {
                statsProviderRegistry.registerStatsProvider(configElement, pp, subTreePath,
                        null, null, null, statsProvider, subTreePath, null);
                spre = statsProviderRegistry.getStatsProviderRegistryElement(statsProvider);
            }
            enableStatsProvider(spre);

        } else {
            printd(" enabled is false ");
            // Register with null values so to know that we need to register them individually and config is on
            statsProviderRegistry.registerStatsProvider(configElement, pp, subTreePath,
                    null, null, null, statsProvider, subTreePath, null);
            spre = statsProviderRegistry.getStatsProviderRegistryElement(statsProvider);
        }

        printd(spre.toString());
        printd("=========================================================");
    }

    public void unregister(Object statsProvider) {
        // Unregisters the statsProvider
        try {
            StatsProviderRegistryElement spre = statsProviderRegistry.getStatsProviderRegistryElement(statsProvider);
            if (spre == null)
                throw new Exception("Invalid statsProvider, cannot unregister");

            // get the Parent node and delete all children nodes (only that we know of)
            String parentNodePath = spre.getParentTreeNodePath();
            List<String> childNodeNames = spre.getChildTreeNodeNames();
            TreeNode rootNode = mrdr.get("server");
            if ((rootNode != null) && (parentNodePath != null)) {
                // This has to return one node
                List<TreeNode> nodeList = rootNode.getNodes(parentNodePath, false, true);
                if (nodeList.size() > 0) {
                    TreeNode parentNode = nodeList.get(0);
                    //Remove each of the child nodes
                    Collection<TreeNode> childNodes = parentNode.getChildNodes();
                    for (TreeNode childNode : childNodes) {
                        if (childNodeNames.contains(childNode.getName())){
                            parentNode.removeChild(childNode);
                        }
                    }
                    if (!parentNode.hasChildNodes())
                        removeParentNode(parentNode);
                }
            }

            //get the handles and unregister the listeners from Flashlight
            Collection<ProbeClientMethodHandle> handles = spre.getHandles();
            if (handles != null) {
                for (ProbeClientMethodHandle handle : handles) {
                    // handle.remove????? Mahesh?
                    //TODO IMPLEMENTATION
                    //For now disable the handle => remove the client from invokerlist
                    handle.disable();
                }
            }

            //unregister the statsProvider from Gmbal
            if (spre.getManagedObjectManager() != null)
                unregisterGmbal(spre);

            //Unregister from the MonitoringDataTreeRegistry and the map entries
            statsProviderRegistry.unregisterStatsProvider(statsProvider);
        } catch (Exception ex) {
            Logger.getLogger(StatsProviderManagerDelegateImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void removeParentNode(TreeNode parentNode) {
        TreeNode superParentNode = parentNode.getParent();
        if (superParentNode != null) {
            superParentNode.removeChild(parentNode);
            if (!superParentNode.hasChildNodes())
                removeParentNode(superParentNode);
        }
    }

    /* called from SPMD, when monitoring-enabled flag is turned on */
    public void updateAllStatsProviders() {
        // Looks like the monitoring-enabled flag is just turned ON. Lets do the catchup
        for (String configElement : statsProviderRegistry.getConfigElementList()) {
            Collection<StatsProviderRegistryElement> spreList =
                        statsProviderRegistry.getStatsProviderRegistryElement(configElement);
            for (StatsProviderRegistryElement spre : spreList) {
                boolean isConfigEnabled = getEnabledValue(configElement);
                if (isConfigEnabled != spre.isEnabled) {
                    if (isConfigEnabled)
                        enableStatsProvider(spre);
                    else
                        disableStatsProvider(spre);
                }
            }
        }
    }

    /* called from SPMD, when monitoring-enabled flag is turned off */
    public void disableAllStatsProviders() {
        // Looks like the monitoring-enabled flag is just turned OFF. Disable all the statsProviders which were on
        for (String configElement : statsProviderRegistry.getConfigElementList()) {
            Collection<StatsProviderRegistryElement> spreList =
                        statsProviderRegistry.getStatsProviderRegistryElement(configElement);
            for (StatsProviderRegistryElement spre : spreList) {
                if (spre.isEnabled) {
                    disableStatsProvider(spre);
                }
            }
        }
    }

    /* called from SMPD, when monitoring level for a module is turned on */
    public void enableStatsProviders(String configElement) {
        //If monitoring-enabled is false, just return
        if (!getMonitoringEnabled())
            return;
        //Enable all the StatsProviders for a given configElement
        printd("Enabling all the statsProviders for - " + configElement);
        List<StatsProviderRegistryElement> spreList = statsProviderRegistry.getStatsProviderRegistryElement(configElement);
        if (spreList == null)
            return;
        for (StatsProviderRegistryElement spre : spreList) {
            if (!spre.isEnabled())
                enableStatsProvider(spre);
        }
    }

    /* called from SMPD, when monitoring level for a module is turned off */
    public void disableStatsProviders(String configElement) {
        // I think we should still disable even when monitoring-enabled is false
        /*
        //If monitoring-enabled is false, just return
        if (!getMonitoringEnabled())
            return;
        */
        //Disable all the StatsProviders for a given configElement
        printd("Disabling all the statsProviders for - " + configElement);
        List<StatsProviderRegistryElement> spreList = statsProviderRegistry.getStatsProviderRegistryElement(configElement);
        if (spreList == null)
            return;
        for (StatsProviderRegistryElement spre : spreList) {
            if (spre.isEnabled())
                disableStatsProvider(spre);
        }
    }

    private boolean getMonitoringEnabled() {
        return Boolean.parseBoolean(monitoringService.getMonitoringEnabled());
    }

    private void enableStatsProvider(StatsProviderRegistryElement spre) {
        Object statsProvider = spre.getStatsProvider();
        printd("Enabling the statsProvider - " + statsProvider.getClass().getName());

         /* Step 1. Create the tree for the statsProvider */
        // Check if we already have TreeNodes created
        if (spre.getParentTreeNodePath() == null) {
            /* Verify if PluginPoint exists, create one if it doesn't */
            PluginPoint pp = spre.getPluginPoint();
            String subTreePath = spre.getSubTreePath();

            TreeNode ppNode = getPluginPointNode(pp, serverNode);
            TreeNode parentNode = createSubTree(ppNode, subTreePath);
            List<String> childNodeNames = createTreeForStatsProvider(parentNode, statsProvider);
            spre.setParentTreeNodePath(parentNode.getCompletePathName());
            spre.setChildNodeNames(childNodeNames);
        } else {
            updateTreeNodes(spre, true);
        }

        /* Step 2. register the StatsProvider to the flashlight */
        if (spre.getHandles() == null) {
            // register with flashlight and save the handles
            Collection<ProbeClientMethodHandle> handles = registerStatsProviderToFlashlight(statsProvider);
            spre.setHandles(handles);
        } else {
            //Enable the Flashlight handles for this statsProvider
            for (ProbeClientMethodHandle handle : spre.getHandles()) {
                if (!handle.isEnabled())
                    handle.enable();
            }
        }

        /* Step 3. gmbal registration */
        ManagedObjectManager mom = null;
        if (AMXReady && getMbeanEnabledValue()) {
            //Create mom root using the statsProvider
            String subTreePath = spre.getSubTreePath();
            mom = registerGmbal(statsProvider, subTreePath);
            spre.setManagedObjectManager(mom);
        }

        // Keep track of enabled flag for configElement.  Used later for register gmbal when AMXReady.
        String configElement = spre.getConfigStr();
        spre.setEnabled(true);
    }

    private void disableStatsProvider(StatsProviderRegistryElement spre) {
        printd("Disabling the statsProvider - " + spre.getStatsProvider().getClass().getName());
        /* Step 1. Disable the tree nodes for StatsProvider */
        updateTreeNodes(spre, false);

        /* Step 2. Disable flashlight handles (Ideally unregister them) */
        for (ProbeClientMethodHandle handle : spre.getHandles()) {
            if (!handle.isEnabled())
                handle.disable();
        }

        /* Step 3. Unregister gmbal */
        unregisterGmbal(spre);

        spre.setEnabled(false);
    }

    public void registerAllGmbal() {
        /* We do this when the mbean-enabled is turned on from off */

        printd("Registering all the statsProviders whose enabled flag is 'on' with Gmbal");
        for (StatsProviderRegistryElement spre : statsProviderRegistry.getSpreList()) {
            if (spre.isEnabled()) {
                ManagedObjectManager mom = registerGmbal(spre.getStatsProvider(), spre.getMBeanName());
                spre.setManagedObjectManager(mom);
            }
        }
    }

    public void unregisterAllGmbal() {
        /* We do this when the mbean-enabled is turned off from on */

        printd("Unregistering all the statsProviders whose enabled flag is 'off' with Gmbal");
        for (StatsProviderRegistryElement spre : statsProviderRegistry.getSpreList()) {
            if (spre.isEnabled()) {
                unregisterGmbal(spre);
            }
        }
    }

    private void updateTreeNodes(StatsProviderRegistryElement spre, boolean enable) {
        //Enable/Disable the child TreeNodes
        String parentNodePath = spre.getParentTreeNodePath();
        List<String> childNodeNames = spre.getChildTreeNodeNames();
        TreeNode rootNode = mrdr.get("server");
        if (rootNode != null) {
            // This has to return one node
            List<TreeNode> nodeList = rootNode.getNodes(parentNodePath, false, true);
            TreeNode parentNode = nodeList.get(0);
            //For each child Node, enable it
            Collection<TreeNode> childNodes = parentNode.getChildNodes();
            boolean hasUpdatedNode = false;
            for (TreeNode childNode : childNodes) {
                if (childNodeNames.contains(childNode.getName())){
                    //Enabling or Disabling the child node (based on enable flag)
                    if (childNode.isEnabled() != enable) {
                        printd(((enable)?"En":"Dis") + "abling the child node - " + childNode.getCompletePathName());
                        childNode.setEnabled(enable);
                        hasUpdatedNode = true;
                    }
                }
            }
            if (!hasUpdatedNode)
                return;
            //Make sure the tree path is affected with the changes.
            if (enable)
                enableTreeNode(parentNode);
            else
                disableTreeNode(parentNode);
        }

    }

    private void enableTreeNode(TreeNode treeNode) {
        if (!treeNode.isEnabled()) {
            treeNode.setEnabled(true);
            // recursevely call the enable on parent nodes, until the whole path is enabled
            if (treeNode.getParent() != null) {
                enableTreeNode(treeNode.getParent());
            }
        }
    }

    private void disableTreeNode(TreeNode treeNode) {
        if (treeNode.isEnabled()) {
            boolean isAnyChildEnabled = false;
            Collection<TreeNode> childNodes = treeNode.getChildNodes();
            printd("Parent Node = " + treeNode.getName() + "  childNodes.size()=" + childNodes.size());
            if (childNodes != null) {
                for (TreeNode childNode : childNodes) {
                    if (childNode.isEnabled()) {
                        isAnyChildEnabled = true;
                        break;
                    }
                }
            }
            // if none of the childs are enabled, disable the parent
            if (!isAnyChildEnabled) {
                treeNode.setEnabled(false);
                // recursevely call the disable on parent nodes
                if (treeNode.getParent() != null) {
                    disableTreeNode(treeNode.getParent());
                }
            }
        }
    }

    private List<String> createTreeForStatsProvider(TreeNode parentNode, Object statsProvider) {
        /* construct monitoring tree at PluginPoint using subTreePath */
        List<String> childNodeNames = new ArrayList();

        /* retrieve ManagedAttribute attribute id (v2 compatible) and method names */
        for (Method m : statsProvider.getClass().getDeclaredMethods()) {
            ManagedAttribute ma = m.getAnnotation(ManagedAttribute.class);
            if (ma != null) {
                String methodName = m.getName();
                String id = ma.id();
                if ((id == null) || id.isEmpty()) { // if id not specified, derive from method name
                    String methodNameLower = methodName.toLowerCase();
                    if (methodNameLower.startsWith("get") && methodNameLower.length() > 3) {
                        id = methodNameLower.substring(3);
                    }
                }

                TreeNode attrNode = TreeNodeFactory.createMethodInvoker(id, statsProvider, id, m);
                parentNode.addChild(attrNode);
                childNodeNames.add(attrNode.getName());
            }
        }
        return childNodeNames;
    }

    private Collection<ProbeClientMethodHandle> registerStatsProviderToFlashlight(Object statsProvider) {
        //register the statsProvider with Flashlight
        Collection<ProbeClientMethodHandle> handles = null;
        try {
            //System.out.println("****** Registering the StatsProvider (" + statsProvider.getClass().getName() + ") with flashlight");
            handles = pcm.registerListener(statsProvider);
        //System.out.println("********* handles = " + handles);
        // save the handles against config so you can enable/disable the handles
        // save the handles also against statsProvider so you can unregister when statsProvider is unregistered
        } catch (Exception e) {
            //e.printStackTrace();
            Logger.getLogger(StatsProviderManagerDelegateImpl.class.getName()).log(Level.SEVERE, "flashlight registration failed", e);
        }
        return handles;
    }

    private TreeNode createSubTree(TreeNode parent, String subTreePath) {
        StringTokenizer st = new StringTokenizer(subTreePath, "/");
        TreeNode parentNode = parent;

        while (st.hasMoreTokens()) {
            TreeNode subTreeNode = createSubTreeNode(parentNode, st.nextToken());
            parentNode = subTreeNode;
        }
        return parentNode;
    }

    private TreeNode createSubTreeNode(TreeNode parent, String child) {
        TreeNode childNode = parent.getNode(child);
        if (childNode == null) {
            childNode = TreeNodeFactory.createTreeNode(child, null, child);
            parent.addChild(childNode);
        }
        return childNode;
    }

    public boolean hasListeners(String probeStr) {
        boolean hasListeners = false;
        FlashlightProbe probe = probeRegistry.getProbe(probeStr);
        if (probe != null)
            return probe.isEnabled();
        return hasListeners;
    }

    //Called when AMX DomainRoot is loaded (when jconsole or gui is started)
    //Register statsProviders with gmbal whose configElement is enabled
    //Save mom in the spre.  Used in unregister with gmbal later for config change to OFF or undeploy
    //Set AMXReady flag to true
    @Override
    public void mbeanRegistered(final ObjectName objectName, final MBeanListener listener) {
        super.mbeanRegistered(objectName, listener);
        AMXReady = true;
        statsProviderRegistry.setAMXReady(true);
        if (this.getMbeanEnabledValue()) {
            for (StatsProviderRegistry.StatsProviderRegistryElement spre : statsProviderRegistry.getSpreList()) {
                if (spre.isEnabled()) {
                    ManagedObjectManager mom = registerGmbal(spre.getStatsProvider(), spre.getMBeanName());
                    spre.setManagedObjectManager(mom);
                }
            }
        }
    }

    StatsProviderRegistry getStatsProviderRegistry() {
        return this.statsProviderRegistry;
    }

    private ManagedObjectManager registerGmbal(Object statsProvider, String mbeanName) {
        ManagedObjectManager mom = null;
        //String mbeanName = subTreePath;
        try {
            // 1 mom per statsProvider
            mom = ManagedObjectManagerFactory.createFederated(MONITORING_SERVER);
            if (mom != null) {
                //if (!isMBeanRegistered(statsProvider, mbeanName)) {
                    mom.stripPackagePrefix();
                    if (mbeanName != null && !mbeanName.isEmpty()) {
                        mom.createRoot(statsProvider, mbeanName);
                    } else {
                        mom.createRoot(statsProvider);
                    }
                //}
            }
        //To register hierarchy in mom specify parent ManagedObject, and the ManagedObject itself
        //DynamicMBean mbean = (DynamicMBean)mom.register(parent, obj);
        } catch (Exception e) {
            Logger.getLogger(StatsProviderManagerDelegateImpl.class.getName()).log(Level.SEVERE, "gmbal registration failed", e);
        }
        return mom;
    }

    private void unregisterGmbal(StatsProviderRegistryElement spre) {
        //unregister the statsProvider from Gmbal
        ManagedObjectManager mom = spre.getManagedObjectManager();
        if (mom != null) {
            mom.unregister(spre.getStatsProvider());
            try {
                mom.close();
            } catch (IOException ioe) {
                Logger.getLogger(StatsProviderRegistry.class.getName()).log(Level.SEVERE, null, ioe);
            }
            spre.setManagedObjectManager(null);
        }
    }

    private TreeNode getPluginPointNode(PluginPoint pp, TreeNode serverNode) {
        //TODO
        if (pp.getName().equals(serverNode.getName()))
            return serverNode;
        else {
            String subTreePath = pp.getPath();
            // skip the "server", to avoid duplicate server node
            if (subTreePath.startsWith("server"))
                subTreePath = subTreePath.substring(subTreePath.indexOf("/") + 1 , subTreePath.length());
            return createSubTree(serverNode, subTreePath);
        }
    }

    private TreeNode constructServerPP() {
        TreeNode srvrNode = mrdr.get("server");
        if (srvrNode != null) {
            return srvrNode;
        }
        // server
        Server srvr = null;
        List<Server> ls = domain.getServers().getServer();
        for (Server sr : ls) {
            if ("server".equals(sr.getName())) {
                srvr = sr;
                break;
            }
        }
        srvrNode = TreeNodeFactory.createTreeNode("server", null, "server");
        mrdr.add("server", srvrNode);
        return srvrNode;
    }

    private boolean getEnabledValue(String configElement) {
        boolean enabled = false;
        String level = monitoringService.getMonitoringLevel(configElement);
        if (level != null) {
            if (level.equals(ContainerMonitoring.LEVEL_HIGH) ||
                    level.equals(ContainerMonitoring.LEVEL_LOW)) {
                enabled = true;
            }
        } else {
            Logger.getLogger(StatsProviderManagerDelegateImpl.class.getName()).log(Level.WARNING, "module-monitoring-level or container-monitoring config element for " + configElement + " does not exist.");
        }
        return enabled;
    }

    private boolean getMbeanEnabledValue() {
        return Boolean.parseBoolean(monitoringService.getMbeanEnabled());
    }

    public boolean isStatsProviderRegistered(Object statsProvider, String subTreePath) {
        boolean isStatsProviderRegistered = false;
        Collection<StatsProviderRegistry.StatsProviderRegistryElement> spreList = statsProviderRegistry.getSpreList();
        for (StatsProviderRegistry.StatsProviderRegistryElement spre : spreList) {
            if (spre.getStatsProvider().equals(statsProvider) && spre.getMBeanName().equals(statsProvider)) {
                isStatsProviderRegistered = true;
            }
        }
        return isStatsProviderRegistered;
    }

    public boolean isMBeanRegistered(Object statsProvider, String subTreePath) {
        return isMBeanRegistered(getObjectName(statsProvider, subTreePath));
    }

    public boolean isMBeanRegistered(ObjectName objectName) {
        return mbeanServer.isRegistered(objectName);
    }

    public ObjectName getObjectName(Object statsProvider, String subTreePath) {
        String typeValue = getTypeValue(statsProvider);
        String nameValue = getNameValue(subTreePath);
        return AMXGlassfish.DEFAULT.newObjectName(PARENT_PATH, typeValue, nameValue);
    }

    public String getTypeValue(Object statsProvider) {
        return statsProvider.getClass().getSimpleName();
    }

    public String getNameValue(String subTreePath) {
        return subTreePath;
    }

    private void printd(String str) {
        if (ddebug)
            System.out.println("APK : " + str);
    }

}
