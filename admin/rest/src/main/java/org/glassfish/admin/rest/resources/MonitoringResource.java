/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2011 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.resources;

import org.glassfish.admin.rest.utils.ProxyImpl;
import org.jvnet.hk2.component.Habitat;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;
import java.net.URL;
import java.util.Properties;
import java.util.TreeMap;
import javax.ws.rs.Consumes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.jersey.api.client.Client;
import org.glassfish.admin.rest.results.ActionReportResult;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.glassfish.external.statistics.Statistic;
import org.glassfish.external.statistics.Stats;

import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;

import static org.glassfish.admin.rest.provider.ProviderUtil.*;

/**
 * @author rajeshwar patil
 */
//@Path("monitoring{path:.*}")
@Path("domain{path:.*}")
@Produces({"text/html;qs=2",MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})
public class MonitoringResource {

    @PathParam("path")
    String path;
    @Context
    protected UriInfo uriInfo;

    @Context
    protected Habitat habitat;
    
    @Context
    protected Client client;
    
    @GET
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,"text/html;qs=2"})
    public ActionReportResult getChildNodes() {
        List<TreeNode> list = new ArrayList<TreeNode>();

        RestActionReporter ar = new RestActionReporter();
        ar.setActionDescription("Monitoring Data");
        ar.setMessage("");
        ar.setSuccess();
        // ar.getExtraProperties().put("jmxServiceUrls", jmxUrls);
        ActionReportResult result = new ActionReportResult(ar);
        MonitoringRuntimeDataRegistry monitoringRegistry =habitat.getComponent(MonitoringRuntimeDataRegistry.class);

        if (path == null) {
            //FIXME - Return appropriate message to the user
            //return Response.status(400).entity("match pattern is invalid or null").build();
            return result;
        }

        if (monitoringRegistry == null) {
            //FIXME - Return appropriate message to the user
            //return Response.status(404).entity("monitoring facility not installed").build();
            return result;
        }

        String currentInstanceName = System.getProperty("com.sun.aas.instanceName");
        if ((path.equals("")) || (path.equals("/"))) {
            //Return the sub-resource list of root nodes

            TreeNode serverNode = monitoringRegistry.get(currentInstanceName);
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
                    constructEntity(list,  ar);                    
                    Domain domain = habitat.getComponent(Domain.class);
                    Map<String, String> links = (Map<String, String>) ar.getExtraProperties().get("childResources");
                    for (Server s : domain.getServers().getServer()) {
                        if (!s.getName().equals("server")) {// add all non 'server' instances
                            links.put(s.getName(), getElementLink(uriInfo, s.getName()));
                        }
                    }                   
                }
                return result;
            } else {
                //No root node available, so nothing to list
                //FIXME - Return appropriate message to the user
                ///return Response.status(404).entity("No monitoring data. Please check monitoring levels are configured").build();
                return result;
            }
        }

        //ignore the starting slash
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if(!path.startsWith(currentInstanceName)) {
            //TODO need to make sure we are actually running on DAS else do not try to forward.
            // forward the request to instance
            Properties proxiedResponse = new MonitoringProxyImpl().proxyRequest(uriInfo, client, habitat);

            ar.setExtraProperties(proxiedResponse);
            return result;
        }

        //replace all . with \. Monitoring code expects a '.' to be escaped to distinguish from normal field seperator
        path = path.replaceAll("\\.", "\\\\.");

        String dottedName = path.replace('/', '.');

        String root;
        int index =  dottedName.indexOf('.');
        if (index != -1) {
            root = dottedName.substring(0, index);
            dottedName = dottedName.substring(index + 1 );
        } else {
            root = dottedName;
            dottedName = "";
        }

        TreeNode rootNode = monitoringRegistry.get(root);
        if (rootNode == null) {
            //No monitoring data, so nothing to list
            //FIXME - Return appropriate message to the user
            ///return Response.status(404).entity("No monitoring data. Please check monitoring levels are configured").build();
            return result;
        }

        TreeNode  currentNode;
        if (dottedName.length() > 0) {
            currentNode = rootNode.getNode(dottedName);
        } else {
            currentNode = rootNode;
        }


        if (currentNode == null) {
            //No monitoring data, so nothing to list
            return result;
            ///return Response.status(404).entity("Monitoring object not found").build();
        }

        if (currentNode.hasChildNodes()) {
            //print(currentNode.getChildNodes());
            //TreeNode.getChildNodes() is returning disabled nodes too.
            //Switching to new api TreeNode.getEnabledChildNodes() which returns
            //only the enabled nodes. Reference Issue: 9921
            list.addAll(currentNode.getEnabledChildNodes());
        } else {
            Object r = currentNode.getValue();
            System.out.println("result: " + r);
            list.add(currentNode);
        }
        constructEntity(list,  ar);
        return result;
    }

    private void constructEntity(List<TreeNode> nodeList, RestActionReporter ar) {
        Map map = new TreeMap();
        for (TreeNode node : nodeList) {
            //process only the leaf nodes, if any
            if (!node.hasChildNodes()) {
                //getValue() on leaf node will return one of the following -
                //Statistic object, String object or the object for primitive type
                Object value = node.getValue();

                if (value == null) {
                    return;
                }

                try {
                    if (value instanceof Statistic) {
                        Statistic statisticObject = (Statistic) value;
                        Map data = getStatistic(statisticObject);
                        map.put(node.getName(), data);
                    } else if (value instanceof Stats) {
                        Map subMap = new TreeMap();
                        for (Statistic statistic : ((Stats) value).getStatistics()) {
                            Map data2 = getStatistic(statistic);
                            subMap.put(statistic.getName(), data2);
                        }
                        map.put(node.getName(), subMap);

                    } else {
                        map.put(node.getName(), jsonValue(value));
                    }
                } catch (Exception exception) {
                    //log exception message as warning
                }

            }
        }
        ar.getExtraProperties().put("entity", map);
        Map<String, String> links = new TreeMap<String, String>();
        for (TreeNode node : nodeList) {
            //process only the non-leaf nodes, if any
            if (node.hasChildNodes()) {
                String name = node.getName();
                // Grizzly will barf if it sees backslash
                name = name.replace("\\.", ".");
                links.put(name, getElementLink(uriInfo, name));
            }

        }
        ar.getExtraProperties().put("childResources", links);

    }

    private static class MonitoringProxyImpl extends ProxyImpl {
        @Override
        public UriBuilder constructTargetURLPath(UriInfo sourceUriInfo, URL responseURLReceivedFromTarget) {
            return sourceUriInfo.getBaseUriBuilder().replacePath(responseURLReceivedFromTarget.getFile());
        }

        @Override
        public UriBuilder constructForwardURLPath(UriInfo sourceUriInfo) {
            //forward to URL that has same path as source request.
            return sourceUriInfo.getAbsolutePathBuilder();
        }

        @Override
        public String extractTargetInstanceName(UriInfo uriInfo) {
            return uriInfo.getPathSegments().get(1).getPath(); //pathSegment[0] == "monitoring"
        }
    }
}
