/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.monitor;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
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
import org.jvnet.hk2.component.Singleton;

/**
 *
 * @author Jennifer
 */
public class StatsProviderManagerDelegateImpl implements StatsProviderManagerDelegate, PostConstruct {

    private MonitoringRuntimeDataRegistry mrdr;
    private Domain domain;

    private TreeNode serverNode;
    //private ManagedObjectManager mom;
    private HashMap momMap = new HashMap();
    //MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
    private static final String MONITORING_ROOT = "v3:pp=/,type=mon";
    private static final String MONITORING_SERVER = "v3:pp=/mon,type=server-mon,name=das";

    StatsProviderManagerDelegateImpl(MonitoringRuntimeDataRegistry mrdr, Domain domain) {
        this.mrdr = mrdr;
        this.domain = domain;
        //serverNode is special, construct that first if doesn't exist
        serverNode = constructServerPP();

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
            }
        }

        /* gmbal registration */
        // For now create mom root using the statsProvider
        
        try {
            // 1 mom per statsProvider
            ManagedObjectManager mom = ManagedObjectManagerFactory.createFederated(new ObjectName(MONITORING_SERVER));
            mom.stripPackagePrefix();
            mom.createRoot(statsProvider);
            momMap.put(statsProvider, mom);

            //To register hierarchy in mom specify parent ManagedObject, and the ManagedObject itself
            //DynamicMBean mbean = (DynamicMBean)mom.register(parent, obj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* config */
        //add configElement to monitoring level element if not already there - find out from Nandini
    }

    public void unregister(Object statsProvider) {
        ((ManagedObjectManager)momMap.get(statsProvider)).unregister(statsProvider);
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
        TreeNode serverNode = mrdr.get("server");
        if (serverNode != null) {
            return serverNode;
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
        serverNode = TreeNodeFactory.createTreeNode("server", null, "server");
        mrdr.add("server", serverNode);
        return serverNode;
    }

}
