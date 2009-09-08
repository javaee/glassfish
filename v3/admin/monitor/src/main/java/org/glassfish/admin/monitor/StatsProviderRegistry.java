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
import org.glassfish.external.probe.provider.PluginPoint;

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

    public void registerStatsProvider(String configStr, PluginPoint pp, String subTreePath,
                        String parentTreeNodePath, List<String> childTreeNodeNames,
                        Collection<ProbeClientMethodHandle> handles,
                        Object statsProvider,
                        String mbeanName,
                        ManagedObjectManager mom) {

        StatsProviderRegistryElement spre =
                    new StatsProviderRegistryElement(
                            configStr, pp, subTreePath, parentTreeNodePath, childTreeNodeNames,
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

    StatsProviderRegistryElement getStatsProviderRegistryElement(Object statsProvider) {
        return statsProviderToRegistryElementMap.get(statsProvider);    
    }

    List<StatsProviderRegistryElement> getStatsProviderRegistryElement(String configElement) {
        return (this.configToRegistryElementMap.get(configElement));
    }

    Collection<StatsProviderRegistryElement> getSpreList() {
        return statsProviderToRegistryElementMap.values();
    }

    Collection<String> getConfigElementList() {
        return this.configToRegistryElementMap.keySet();
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
        PluginPoint pp;
        String subTreePath;
        String parentTreeNodePath;
        List<String> childTreeNodeNames;
        Collection<ProbeClientMethodHandle> handles;
        Object statsProvider;
        String mbeanName;
        ManagedObjectManager mom;
        boolean isEnabled = false;

        public StatsProviderRegistryElement(String configStr, PluginPoint pp, String subTreePath,
                        String parentTreeNodePath, List<String> childTreeNodeNames,
                        Collection<ProbeClientMethodHandle> handles,
                        Object statsProvider,
                        String mbeanName,
                        ManagedObjectManager mom) {
           this.configStr = configStr;
           this.pp = pp;
           this.subTreePath = subTreePath;
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

        public PluginPoint getPluginPoint() {
            return pp;
        }

        public String getSubTreePath() {
            return subTreePath;
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

        boolean isEnabled() {
            return isEnabled;
        }

        void setEnabled(boolean enabled) {
            isEnabled = enabled;
        }

        public void setParentTreeNodePath(String completePathName) {
            this.parentTreeNodePath = completePathName;
        }

        public void setChildNodeNames(List<String> childNodeNames) {
            this.childTreeNodeNames = childNodeNames;
        }

        public void setHandles(Collection<ProbeClientMethodHandle> handles) {
            this.handles = handles;
        }

        public String toString() {
            this.pp = pp;
            this.subTreePath = subTreePath;
            this.handles = handles;
            this.parentTreeNodePath = parentTreeNodePath;
            String str = "    configStr = " + configStr + "\n" +
                         "    statsProvider = " + statsProvider.getClass().getName() + "\n" +
                         "    PluginPoint = " + pp + "\n" +
                         "    handles = " + ((handles==null)?"null":"not null") + "\n" +
                         "    parentTreeNodePath = " + parentTreeNodePath;
            return str;
        }
    }
}
