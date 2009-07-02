/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.monitor;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.List;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.factory.TreeNodeFactory;
import org.glassfish.gmbal.ManagedObjectManager;
import org.glassfish.gmbal.ManagedObjectManagerFactory;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.probe.provider.PluginPoint;
import org.glassfish.probe.provider.StatsProviderManagerDelegate;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.annotations.Inject;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import com.sun.enterprise.config.serverbeans.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

import org.glassfish.api.amx.AMXValues;

/**
 *
 * @author Jennifer
 */
@Service
@Scoped(Singleton.class)
public class StatsProviderManagerDelegateImpl implements StatsProviderManagerDelegate, PostConstruct {

    protected ProbeClientMediator pcm;

    private final MonitoringRuntimeDataRegistry mrdr;
    private final Domain domain;

    private final TreeNode serverNode;
    //private ManagedObjectManager mom;
    private final HashMap momMap = new HashMap();
    //MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
    private static final ObjectName MONITORING_ROOT = AMXValues.monitoringRoot();
    private static final ObjectName MONITORING_SERVER = AMXValues.serverMon( AMXValues.dasName() );
    private StatsProviderRegistry statsProviderRegistry;
    //private Map<Object, List<Object>> statsProviderRegistry = new HashMap();

    StatsProviderManagerDelegateImpl(ProbeClientMediator pcm, 
                    MonitoringRuntimeDataRegistry mrdr, Domain domain) {
        this.pcm = pcm;
        this.mrdr = mrdr;
        this.domain = domain;
        //serverNode is special, construct that first if doesn't exist
        serverNode = constructServerPP();
        statsProviderRegistry = new StatsProviderRegistry(mrdr);
    }

    public void postConstruct() {
        
        /* gmbal create root */
        try { 
            
            //TODO fix hard-coding of object name. Maybe use query.
            //Can we createFederated at MONITORING_ROOT, then create mom root using MONITORING_SERVER?
            //Set<ObjectName> set = mbeanServer.queryNames(new ObjectName(MONITORING_SERVER), null);

            // Don't think 1 mom per server will work as easily because there can only be 1 obj registered at mom root
            //mom = ManagedObjectManagerFactory.createFederated(new ObjectName(MONITORING_SERVER));
            //mom.stripPackagePrefix();
            //mom.createRoot();

            // There can only be 1 ManagedObject registered at mom root
            //??mom.registerAtRoot(PluginPoint.SERVER);
            //constructServerPP();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            // save the handles against config so you can enable/disable the handles
            // save the handles also against statsProvider so you can unregister when statsProvider is unregistered
        } catch (Exception e) {
            //e.printStackTrace;
        }

        /* gmbal registration */
        // For now create mom root using the statsProvider
        
        try {
            // 1 mom per statsProvider
            ManagedObjectManager mom = ManagedObjectManagerFactory.createFederated(MONITORING_SERVER);
            mom.stripPackagePrefix();
            mom.createRoot(statsProvider, subTreePath);
            momMap.put(statsProvider, mom);

            //To register hierarchy in mom specify parent ManagedObject, and the ManagedObject itself
            //DynamicMBean mbean = (DynamicMBean)mom.register(parent, obj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* config - TODO */
        //add configElement to monitoring level element if not already there - find out from Nandini

        //Make an entry to my own registry so I can manage the unregister, enable and disable
        statsProviderRegistry.registerStatsProvider(configElement, parentNode.getCompletePathName(), childNodeNames, handles, statsProvider);
    }

    public void unRegister(Object statsProvider) {
        try {
            //Unregister from Gmbal
            ((ManagedObjectManager) momMap.get(statsProvider)).unregister(statsProvider);
            //Unregister from the MonitoringDataTreeRegistry
            statsProviderRegistry.unregisterStatsProvider(statsProvider);
        } catch (Exception ex) {
            Logger.getLogger(StatsProviderManagerDelegateImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public void unregisterAll() {
        //try {
            //mom.close();
        //} catch (IOException ioe) {
          //  ioe.printStackTrace();
        //}
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

}
