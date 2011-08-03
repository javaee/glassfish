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
    
    private List<TreeNodeBean> getRoot() {
        List<TreeNodeBean> root = new ArrayList<TreeNodeBean>();
        
        root.add(new TreeNodeBean("Home", "/welcome.xhtml"));
        root.add(new TreeNodeBean("Domain", "/images/icons/domain.gif", "/demo/domain.xhtml"));
        root.add(new TreeNodeBean("Admin Server", "/images/icons/instance.gif", "/demo/adminServer.xhtml"));
        root.add(new TreeNodeBean("Clusters", "/images/icons/cluster.gif", "/demo/clusters.xhtml"));
        root.add(new TreeNodeBean("Standalone Instances", "/images/icons/instance.gif", "/demo/instances.xhtml" ));
        root.add(new TreeNodeBean("Applications", "/images/icons/instance.gif", "/demo/testApplications.xhtml"));
        
        TreeNodeBean resources = new TreeNodeBean("Resources", "/images/icons/resources.gif");
        root.add(resources);
        resources.setChildren(new ArrayList<TreeNodeBean>() {{
                add(new TreeNodeBean("JDBC", "/images/icons/jdbc.gif"));
                add(new TreeNodeBean("Connectors", "/images/icons/connector.png"));
                add(new TreeNodeBean("Resource Adapter Configs", "/images/icons/connector.png"));
                add(new TreeNodeBean("JMS Resources"));
                add(new TreeNodeBean("JavaMail Resources"));
                add(new TreeNodeBean("JNDI"));
        }});

        return root;
    }
}
