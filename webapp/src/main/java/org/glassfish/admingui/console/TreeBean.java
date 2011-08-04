/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.console;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import org.apache.myfaces.trinidad.model.ChildPropertyTreeModel;
import org.apache.myfaces.trinidad.model.TreeModel;
import org.glassfish.admingui.plugins.ConsolePluginMetadata;
import org.glassfish.admingui.plugins.NavigationNode;
import org.glassfish.admingui.plugins.PluginService;
import org.glassfish.admingui.plugins.jsf.PluginUtil;

/**
 *
 * @author jdlee
 */
@ManagedBean
@ApplicationScoped
public class TreeBean {
    private transient TreeModel model = null;

    public TreeModel getModel() throws IntrospectionException {
        if (model == null) {
            model = new ChildPropertyTreeModel(getRoot(), "children");
        }
        return model;
    }   
    
    private List<NavigationNode> getRoot() {
        List<NavigationNode> root = new ArrayList<NavigationNode>();
        
        root.add(new NavigationNode("Home", null, "/welcome.xhtml"));
        root.add(new NavigationNode("Domain", "/images/icons/domain.gif", "/demo/domain.xhtml"));
        root.add(new NavigationNode("Admin Server", "/images/icons/instance.gif", "/demo/adminServer.xhtml"));
        root.add(new NavigationNode("Clusters", "/images/icons/cluster.gif", "/demo/clusters.xhtml"));
        root.add(new NavigationNode("Standalone Instances", "/images/icons/instance.gif", "/demo/instances.xhtml" ));
        root.add(new NavigationNode("Applications", "/images/icons/instance.gif", "/demo/testApplications.xhtml"));
        root.addAll(getRootNodes());
        
        NavigationNode resources = new NavigationNode("Resources", "/images/icons/resources.gif");
        root.add(resources);
        resources.setChildren(new ArrayList<NavigationNode>() {{
                add(new NavigationNode("JDBC", "/images/icons/jdbc.gif"));
                add(new NavigationNode("Connectors", "/images/icons/connector.png"));
                add(new NavigationNode("Resource Adapter Configs", "/images/icons/connector.png"));
                add(new NavigationNode("JMS Resources"));
                add(new NavigationNode("JavaMail Resources"));
                add(new NavigationNode("JNDI"));
        }});

        return root;
    }

    private List<NavigationNode> getRootNodes() {
        PluginService ps = PluginUtil.getPluginService();
        List<NavigationNode> nodes = new ArrayList<NavigationNode>();
        
        for (ConsolePluginMetadata cpm : ps.getPlugins()) {
            nodes.addAll(cpm.getNavigationNodes("root"));
        }
        
        return nodes;
    }
}
