/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU 
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You 
 * may not use this file except in compliance with the License.  You can 
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
import static org.glassfish.admingui.plugins.NavigationNode.createNode;
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
        
        root.add(createNode("homeNode", "Home", null, "/welcome.xhtml", null));
/*        root.add(createNode("domainNode", "Domain", "/images/icons/domain.gif", "/demo/domain.xhtml", null));
        root.add(createNode("adminServerNode", "Admin Server", "/images/icons/instance.gif", "/demo/adminServer.xhtml", null));
        root.add(createNode("clusterNode", "Clusters", "/images/icons/cluster.gif", "/demo/clusters.xhtml", null));
        root.add(createNode("instancesNode", "Standalone Instances", "/images/icons/instance.gif", "/demo/instances.xhtml", null));
*/
        root.add(createNode("applicationsNode", "Applications", "/images/icons/instance.gif", "/demo/listApplications.xhtml", null));
        root.add(createNode("servicesNode", "Services", "/images/icons/instance.gif", "/demo/listServices.xhtml", null));
        root.add(createNode("templatesNode", "Templates", "/images/icons/instance.gif", "/demo/listTemplates.xhtml", null));
  //      root.addAll(getRootNodes());
        
        root.add(createNode("demoNode", "Drag&Drop Demo", null, "/demo/testApplications.xhtml", null));
        root.add(createNode("wizardNode", "Deployment Wizard Demo", null, "/demo/wizard.xhtml", null));
/*
        NavigationNode resources = createNode("resourcesNode", "Resources", "/images/icons/resources.gif", "/demo/resources.xhtml", new ArrayList<NavigationNode>() {{
                add(createNode("jdbcNode", "JDBC", "/images/icons/JDBC.gif", "/demo/jdbcResources.xhtml", null));
                add(createNode("connectorsNode", "Connectors", "/images/icons/connector.png", "/demo/connectorResources.xhtml", null));
                add(createNode("raConfigsNode", "Resource Adapter Configs", null, "/demo/jdbcConnectionPools.xhtml", null));
                add(createNode("jsmResourcesNode", "JMS Resources", null, "/demo/connectorConnectionPools.xhtml", null));
                add(new NavigationNode("javaMailNode", "JavaMail Resources"));
                add(new NavigationNode("jndiNode", "JNDI"));
        }});

        root.add(resources);
*/
        root.add(createNode("jason", "Jason", null, "/demo/jason.xhtml", null));
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
