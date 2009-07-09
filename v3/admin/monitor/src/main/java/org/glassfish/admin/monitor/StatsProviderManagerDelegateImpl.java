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
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.factory.TreeNodeFactory;
import org.glassfish.gmbal.ManagedObjectManager;
import org.glassfish.gmbal.ManagedObjectManagerFactory;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.probe.provider.PluginPoint;
import org.glassfish.probe.provider.StatsProviderManagerDelegate;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import com.sun.enterprise.config.serverbeans.*;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Singleton;

import org.glassfish.api.amx.AMXValues;

/**
 *
 * @author Jennifer
 */
@Scoped(Singleton.class)
public class StatsProviderManagerDelegateImpl implements StatsProviderManagerDelegate {
    protected ProbeClientMediator pcm;
    ModuleMonitoringLevels config = null;
    private final MonitoringRuntimeDataRegistry mrdr;
    private final Domain domain;

    private final TreeNode serverNode;
    private static final ObjectName MONITORING_ROOT = AMXValues.monitoringRoot();
    static final ObjectName MONITORING_SERVER = AMXValues.serverMon( AMXValues.dasName() );
    private StatsProviderRegistry statsProviderRegistry;

    StatsProviderManagerDelegateImpl(ProbeClientMediator pcm,
                        MonitoringRuntimeDataRegistry mrdr, Domain domain, ModuleMonitoringLevels config) {
        this.pcm = pcm;
        this.mrdr = mrdr;
        this.domain = domain;
        this.config = config;
        //serverNode is special, construct that first if doesn't exist
        serverNode = constructServerPP();
        statsProviderRegistry = new StatsProviderRegistry(mrdr);
    }

    public void register(String configElement, PluginPoint pp,
                        String subTreePath, Object statsProvider) {

        /* Verify if PluginPoint exists, create one if it doesn't */
        TreeNode ppNode = getPluginPointNode(pp, serverNode);

        /* monitoring registration */

        /* construct monitoring tree at PluginPoint using subTreePath */
        TreeNode parentNode = createSubTree(ppNode, subTreePath);
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

        /* gmbal registration */
        // For now create mom root using the statsProvider
        //if (config.getMbeanEnabled) {
            ManagedObjectManager mom = registerGmbal(statsProvider, subTreePath);
        //}

        /* config - TODO */
        //add configElement to monitoring level element if not already there - find out from Nandini

        //Make an entry to my own registry so I can manage the unregister, enable and disable
        statsProviderRegistry.registerStatsProvider(configElement, parentNode.getCompletePathName(), childNodeNames, handles, statsProvider, subTreePath, mom);

        //If module monitoring level = OFF, disableStatsProvider for that configElement
        if (!getEnabledValue(configElement)) {
            statsProviderRegistry.disableStatsProvider(configElement);
        }
    }

    public void unRegister(Object statsProvider) {
        try {
            //Unregister from the MonitoringDataTreeRegistry and gmbal
            statsProviderRegistry.unregisterStatsProvider(statsProvider);
        } catch (Exception ex) {
            Logger.getLogger(StatsProviderManagerDelegateImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    //public void unregisterAll() {
    //    this.statsProviderRegistry.unregisterAll();
    //}

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
                mom.stripPackagePrefix();
                if (mbeanName != null && !mbeanName.isEmpty()) {
                    mom.createRoot(statsProvider, mbeanName);
                } else {
                    mom.createRoot(statsProvider);
                }
            }
            //To register hierarchy in mom specify parent ManagedObject, and the ManagedObject itself
            //DynamicMBean mbean = (DynamicMBean)mom.register(parent, obj);
        } catch (Exception e) {
            Logger.getLogger(StatsProviderManagerDelegateImpl.class.getName()).log(Level.SEVERE, "gmbal registration failed", e);
        }
        return mom;
    }

    private TreeNode createSubTreeNode(TreeNode parent, String child) {
        TreeNode childNode = parent.getNode(child);
        if (childNode == null) {
            childNode = TreeNodeFactory.createTreeNode(child, null, child);
            parent.addChild(childNode);
        }
        return childNode;
    }

    private TreeNode getPluginPointNode(PluginPoint pp, TreeNode serverNode) {
        //TODO
        if (pp.getName().equals(serverNode.getName()))
            return serverNode;
        else
            return createSubTree(serverNode, pp.getPath());
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

    // hard-code the module monitoring level attribute for now until the new monitoring config is available
    private boolean getEnabledValue(String configElement) {
        boolean enabled = false;
        if (this.config != null) {
            if (configElement.equals("connector-service")) {
                if (this.config.getConnectorService().equals("OFF")) {
                    enabled = false;
                } else {
                    enabled = true;
                }
            } else if (configElement.equals("ejb-container")) {
                if (this.config.getEjbContainer().equals("OFF")) {
                    enabled = false;
                } else {
                    enabled = true;
                }
            } else if (configElement.equals("http-service")) {
                if (this.config.getHttpService().equals("OFF")) {
                    enabled = false;
                } else {
                    enabled = true;
                }
            } else if (configElement.equals("jdbc-connection-pool")) {
                if (this.config.getJdbcConnectionPool().equals("OFF")) {
                    enabled = false;
                } else {
                    enabled = true;
                }
            } else if (configElement.equals("jms-service")) {
                if (this.config.getJmsService().equals("OFF")) {
                    enabled = false;
                } else {
                    enabled = true;
                }
            } else if (configElement.equals("jvm")) {
                if (this.config.getJvm().equals("OFF")) {
                    enabled = false;
                } else {
                    enabled = true;
                }
            } else if (configElement.equals("orb")) {
                if (this.config.getOrb().equals("OFF")) {
                    enabled = false;
                } else {
                    enabled = true;
                }
            } else if (configElement.equals("thread-pool")) {
                if (this.config.getThreadPool().equals("OFF")) {
                    enabled = false;
                } else {
                    enabled = true;
                }
            } else if (configElement.equals("transaction-service")) {
                if (this.config.getTransactionService().equals("OFF")) {
                    enabled = false;
                } else {
                    enabled = true;
                }
            } else if (configElement.equals("web-container")) {
                if (this.config.getWebContainer().equals("OFF")) {
                    enabled = false;
                } else {
                    enabled = true;
                }
            } else { // external modules turn always on for now
                enabled = true;
            }
        }
        return enabled;
    }

}
