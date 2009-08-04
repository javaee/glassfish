package org.glassfish.admin.monitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.gmbal.ManagedObjectManager;
import org.glassfish.gmbal.ManagedObjectManagerFactory;
import org.glassfish.external.probe.provider.StatsProvider;

public class StatsProviderRegistry {
    List<StatsProviderRegistryElement> regElements = new ArrayList();
    private Map<String, List<StatsProviderRegistryElement>>
                        configToRegistryElementMap = new HashMap();
    private Map<Object, StatsProviderRegistryElement>
                        statsProviderToRegistryElementMap = new HashMap();
    private Map<String, Boolean> configEnabledMap = new HashMap();
    private MonitoringRuntimeDataRegistry mrdr;
    private boolean isAMXReady = false;
    private boolean isMBeanEnabled = true;

    public StatsProviderRegistry(MonitoringRuntimeDataRegistry mrdr) {
        this.mrdr = mrdr;
    }

    public void registerStatsProvider(String configStr,
                        String parentTreeNodePath, List<String> childTreeNodeNames,
                        Collection<ProbeClientMethodHandle> handles,
                        Object statsProvider,
                        String mbeanName,
                        ManagedObjectManager mom) {

        StatsProviderRegistryElement spre =
                    new StatsProviderRegistryElement(
                            configStr, parentTreeNodePath, childTreeNodeNames,
                            handles, statsProvider, mbeanName, mom);
        // add a mapping from config to StatsProviderRegistryElement, so you can easily
        // retrieve all stats element for enable/disable functionality
        if (configToRegistryElementMap.containsKey(configStr)) {
            List<StatsProviderRegistryElement> spreList = configToRegistryElementMap.get(configStr);
            spreList.add(spre);
        } else {
            List<StatsProviderRegistryElement> spreList = new ArrayList();
            spreList.add(spre);
            configToRegistryElementMap.put(configStr, spreList);
        }
        // add a mapping from StatsProvider to StatsProviderRegistryElement
        // would make it easy for you when unregistering
        statsProviderToRegistryElementMap.put(statsProvider, spre);
    }

    public void unregisterStatsProvider(Object statsProvider) throws Exception {

        StatsProviderRegistryElement spre = statsProviderToRegistryElementMap.get(statsProvider);
        if (spre == null)
            throw new Exception("Invalid statsProvider, cannot unregister");

        // get the Parent node and delete all children nodes (only that we know of)
        String parentNodePath = spre.getParentTreeNodePath();
        List<String> childNodeNames = spre.getChildTreeNodeNames();
        TreeNode rootNode = mrdr.get("server");
        if (rootNode != null) {
            // This has to return one node
            List<TreeNode> nodeList = rootNode.getNodes(parentNodePath);
            TreeNode parentNode = nodeList.get(0);
            //Remove each of the child nodes
            Collection<TreeNode> childNodes = parentNode.getChildNodes();
            for (TreeNode childNode : childNodes) {
                if (childNodeNames.contains(childNode.getName())){
                    parentNode.removeChild(childNode);
                }
            }
        }

        //get the handles and unregister the listeners from Flashlight
        Collection<ProbeClientMethodHandle> handles = spre.getHandles();
        for (ProbeClientMethodHandle handle : handles) {
            // handle.remove????? Mahesh?
            //TODO IMPLEMENTATION
        }

        //unregister the statsProvider from Gmbal
        unregisterGmbal(spre);

        // Remove the entry of statsProviderRegistryElement from configToRegistryElementMap
        List<StatsProviderRegistryElement> spreList =
                    configToRegistryElementMap.get(spre.getConfigStr());
        if (spreList != null) {
            spreList.remove(spre);
            if (spreList.isEmpty())
                configToRegistryElementMap.remove(spre.getConfigStr());
        }

        // Remove the entry of statsProvider from the statsProviderToRegistryElementMap
        statsProviderToRegistryElementMap.remove(statsProvider);

        // Remove the reference to statsProvider in spre (so it gets picked up by GC)
        spre.setStatsProvider(null);

    }

    public void enableStatsProvider(String configElement) {
        List<StatsProviderRegistryElement> spreList = configToRegistryElementMap.get(configElement);

        if (spreList == null)
            return; // should throw an exception

        for (StatsProviderRegistryElement spre : spreList) {
            Object sp = spre.getStatsProvider();
            if (sp instanceof StatsProvider) {
                ((StatsProvider)sp).enable();
            }
            //Enable the child TreeNodes
            String parentNodePath = spre.getParentTreeNodePath();
            List<String> childNodeNames = spre.getChildTreeNodeNames();
            TreeNode rootNode = mrdr.get("server");
            if (rootNode != null) {
                // This has to return one node
                List<TreeNode> nodeList = rootNode.getNodes(parentNodePath);
                TreeNode parentNode = nodeList.get(0);
                //For each child Node, enable it
                Collection<TreeNode> childNodes = parentNode.getChildNodes();
                for (TreeNode childNode : childNodes) {
                    if (childNodeNames.contains(childNode.getName())){
                        if (!childNode.isEnabled())
                            childNode.setEnabled(true);
                    }
                }
            }

            //Enable the Flashlight handles for this statsProvider
            for (ProbeClientMethodHandle handle : spre.getHandles()) {
                if (!handle.isEnabled())
                    handle.enable();
            }

            //Reregister the statsProvider in Gmbal
            if (isAMXReady() && isMBeanEnabled()) {
                registerGmbal(spre);
            }
        }
    }

    public void disableStatsProvider(String configElement) {
        List<StatsProviderRegistryElement> spreList = configToRegistryElementMap.get(configElement);
        if (spreList == null)
            return;

        for (StatsProviderRegistryElement spre : spreList) {
            Object sp = spre.getStatsProvider();
            if (sp instanceof StatsProvider) {
                ((StatsProvider)sp).disable();
            }
            //Disable the parent TreeNode
            String parentNodePath = spre.getParentTreeNodePath();
            List<String> childNodeNames = spre.getChildTreeNodeNames();
            TreeNode rootNode = mrdr.get("server");
            if (rootNode != null) {
                // This has to return one node
                List<TreeNode> nodeList = rootNode.getNodes(parentNodePath);
                TreeNode parentNode = nodeList.get(0);
                //Disable all the child nodes
                Collection<TreeNode> childNodes = parentNode.getChildNodes();
                for (TreeNode childNode : childNodes) {
                    if (childNodeNames.contains(childNode.getName())){
                        if (childNode.isEnabled())
                            childNode.setEnabled(false);
                    }
                }
            }

            //Disable the Flashlight handles for this statsProvider
            for (ProbeClientMethodHandle handle : spre.getHandles()) {
                if (handle.isEnabled())
                    handle.disable();
            }

            //unregister the statsProvider from Gmbal
            unregisterGmbal(spre);
            
        }
    }

    void registerGmbal(String configElement) {
        List<StatsProviderRegistryElement> spreList = configToRegistryElementMap.get(configElement);
        if (spreList == null)
            return; // should throw an exception

        for (StatsProviderRegistryElement spre : spreList) {
            registerGmbal(spre);
        }
    }

    void unregisterGmbal(String configElement) {
        List<StatsProviderRegistryElement> spreList = configToRegistryElementMap.get(configElement);
        if (spreList == null)
            return; // should throw an exception

        for (StatsProviderRegistryElement spre : spreList) {
            unregisterGmbal(spre);
        }
    }

    void registerGmbal(StatsProviderRegistryElement spre) {
        //Reregister the statsProvider in Gmbal
        Object statsProvider = spre.getStatsProvider();
        ManagedObjectManager mom = spre.getManagedObjectManager();
        if (mom == null) {
            mom = ManagedObjectManagerFactory.createFederated(StatsProviderManagerDelegateImpl.MONITORING_SERVER);
            spre.setManagedObjectManager(mom);
            String mbeanName = spre.getMBeanName();
            mom.stripPackagePrefix();
            if (mbeanName != null && !mbeanName.isEmpty()) {
                mom.createRoot(statsProvider, mbeanName);
            } else {
                mom.createRoot(statsProvider);
            }
        }
    }

    void unregisterGmbal(StatsProviderRegistryElement spre) {
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

    void registerAllGmbal() {
        for (StatsProviderRegistryElement spre :  statsProviderToRegistryElementMap.values()) {
            this.registerGmbal(spre);
        }
    }

    void unregisterAllGmbal() {
        for (StatsProviderRegistryElement spre :  statsProviderToRegistryElementMap.values()) {
            this.unregisterGmbal(spre);
        }
    }

    boolean getConfigEnabled(String configElement) {
        return configEnabledMap.get(configElement).booleanValue();
    }

    void setConfigEnabled(String configElement, boolean enabled) {
        configEnabledMap.put(configElement, Boolean.valueOf(enabled));
    }

    Collection<StatsProviderRegistryElement> getSpreList() {
        return statsProviderToRegistryElementMap.values();
    }

    void setAMXReady(boolean ready) {
        this.isAMXReady = ready;
    }

    boolean isAMXReady() {
        return this.isAMXReady;
    }

    void setMBeanEnabled(boolean enabled) {
        this.isMBeanEnabled = enabled;
    }

    boolean isMBeanEnabled() {
        return this.isMBeanEnabled;
    }

    class StatsProviderRegistryElement {
        String configStr;
        String parentTreeNodePath;
        List<String> childTreeNodeNames;
        Collection<ProbeClientMethodHandle> handles;
        Object statsProvider;
        String mbeanName;
        ManagedObjectManager mom;

        public StatsProviderRegistryElement(String configStr,
                        String parentTreeNodePath, List<String> childTreeNodeNames,
                        Collection<ProbeClientMethodHandle> handles,
                        Object statsProvider,
                        String mbeanName,
                        ManagedObjectManager mom) {
           this.configStr = configStr;
           this.handles = handles;
           this.parentTreeNodePath = parentTreeNodePath;
           this.childTreeNodeNames = childTreeNodeNames;
           this.statsProvider = statsProvider;
           this.mbeanName = mbeanName;
           this.mom = mom;
        }

        public String getConfigStr() {
           return configStr;
        }

        public List<String> getChildTreeNodeNames() {
            return childTreeNodeNames;
        }

        public String getParentTreeNodePath() {
            return parentTreeNodePath;
        }

        public Collection<ProbeClientMethodHandle> getHandles() {
            return handles;
        }

        public Object getStatsProvider() {
            return statsProvider;
        }

        private void setStatsProvider(Object statsProvider) {
            this.statsProvider = statsProvider;
        }

        public String getMBeanName() {
            return mbeanName;
        }

        public ManagedObjectManager getManagedObjectManager() {
            return mom;
        }

        void setManagedObjectManager(ManagedObjectManager mom) {
            this.mom = mom;
        }
    }
}
