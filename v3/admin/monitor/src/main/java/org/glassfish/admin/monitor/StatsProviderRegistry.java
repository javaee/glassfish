package org.glassfish.admin.monitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.gmbal.ManagedObjectManager;

public class StatsProviderRegistry {
    List<StatsProviderRegistryElement> regElements = new ArrayList();
    private Map<String, List<StatsProviderRegistryElement>>
                        configToRegistryElementMap = new HashMap();
    private Map<Object, StatsProviderRegistryElement>
                        statsProviderToRegistryElementMap = new HashMap();
    private MonitoringRuntimeDataRegistry mrdr;

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
        ManagedObjectManager mom = spre.getManagedObjectManager();
        if (mom != null) {
            //if (mom.getObjectName(statsProvider) != null) {
                mom.unregister(statsProvider);
                try {
                    mom.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            //}
        }

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
            Object statsProvider = spre.getStatsProvider();
            ManagedObjectManager mom = spre.getManagedObjectManager();
            String mbeanName = spre.getMBeanName();
            if (mom != null ) {
                //Cannot create a root if mom already has one
                //getObjectName cannot be called before a successful createRoot call
                //if (mom.getObjectName(statsProvider) == null) {
                    mom.stripPackagePrefix();
                    if (mbeanName != null && !mbeanName.isEmpty()) {
                        mom.createRoot(statsProvider, mbeanName);
                    } else {
                        mom.createRoot(statsProvider);
                    }
                //}
            }
        }
    }

    public void disableStatsProvider(String configElement) {
        List<StatsProviderRegistryElement> spreList = configToRegistryElementMap.get(configElement);
        if (spreList == null)
            return;

        for (StatsProviderRegistryElement spre : spreList) {
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
            Object statsProvider = spre.getStatsProvider();
            ManagedObjectManager mom = spre.getManagedObjectManager();
            if (mom != null) {
                //if (mom.getObjectName(statsProvider) != null) {
                    mom.unregister(statsProvider);
                    try {
                        mom.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                //}
            }
        }
    }

    private class StatsProviderRegistryElement {
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
    }
}
