/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
package org.glassfish.admin.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;

import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;


/**
 * @author rajeshwar patil
 */
//@Path("monitoring{path:.*}")
@Path("domain{path:.*}")
public class MonitoringResource {

    @PathParam("path")
    String path;

    @GET
    //@Produces({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.TEXT_HTML})
    public List<TreeNode> getChildNodes() {
        List<TreeNode> list = new ArrayList<TreeNode>();
        MonitoringRuntimeDataRegistry monitoringRegistry = RestService.getMonitoringRegistry();

        if (path == null) {
            //FIXME - Return appropriate message to the user
            //return Response.status(400).entity("match pattern is invalid or null").build();
            return list;
        }

        if (monitoringRegistry == null) {
            //FIXME - Return appropriate message to the user
            //return Response.status(404).entity("monitoring facility not installed").build();
            return list;
        }

        if ((path.equals("")) || (path.equals("/"))) {
            //Return the sub-resource list of root nodes

            //FIXME - No MonitoringRuntimeDataRegistry API available to get hold of
            //all the root nodes. We need this in case of clustering. We need to
            //get hold of root nodes for all the server instances.
            TreeNode serverNode = monitoringRegistry.get("server");
            if (serverNode != null) {
                //check to make sure we do not display empty server resource
                //    - http://host:port/monitoring/domain/server
                //When you turn monitoring levels HIGH and then turn them OFF,
                //you may see empty server resource. This is because server tree
                //node has children (disabled) even when all the monitoring
                //levels are turned OFF.
                //Issue: 9921
                if (!serverNode.getEnabledChildNodes().isEmpty()) {
                    list.add(serverNode);
                }
                return list;
            } else {
                //No root node available, so nothing to list
                //FIXME - Return appropriate message to the user
                ///return Response.status(404).entity("No monitoring data. Please check monitoring levels are configured").build();
                return list;
            }
        }

        //ignore the starting slash
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        //replace all . with \.
        path = path.replaceAll("\\.", "\\\\.");

        String dottedName = path.replace('/', '.');

        String root;
        int index =  dottedName.indexOf('.');
        if (index != -1) {
            root = dottedName.substring(0, dottedName.indexOf('.'));
            dottedName = dottedName.substring(dottedName.indexOf('.') + 1 );
        } else {
            root = dottedName;
            dottedName = "";
        }

        //TreeNode rootNode = monitoringRegistry.get("server");
        TreeNode rootNode = monitoringRegistry.get(root);
        if (rootNode == null) {
            //No monitoring data, so nothing to list
            //FIXME - Return appropriate message to the user
            ///return Response.status(404).entity("No monitoring data. Please check monitoring levels are configured").build();
            return list;
        }

        TreeNode  currentNode;
        if (dottedName.length() > 0) {
            currentNode = rootNode.getNode(dottedName);
        } else {
            currentNode = rootNode;
        }


        if (currentNode == null) {
            //No monitoring data, so nothing to list
            return list;
            ///return Response.status(404).entity("Monitoring object not found").build();
        }

        if (currentNode.hasChildNodes()) {
            //print(currentNode.getChildNodes());
            //TreeNode.getChildNodes() is returning disabled nodes too.
            //Switching to new api TreeNode.getEnabledChildNodes() which returns
            //only the enabled nodes. Reference Issue: 9921
            list.addAll(currentNode.getEnabledChildNodes());
        } else {
            Object result = currentNode.getValue();
            System.out.println("result: " + result);
            list.add(currentNode);
        }
        return list;
    }


    private void print(java.util.Collection c) {
        java.util.Iterator it = c.iterator();
        TreeNode tn;
        while (it.hasNext()) {
            tn = (TreeNode) it.next();
            System.out.println("t: " + tn);
            System.out.println("Has children: " + tn.hasChildNodes());
        }
    }
}