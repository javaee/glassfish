/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;
import java.net.MalformedURLException;
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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.glassfish.admin.rest.RestService;
import org.glassfish.admin.rest.clientutils.MarshallingUtils;
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
    @GET
    //@Produces({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,"text/html;qs=2"})
    public ActionReportResult getChildNodes() {
        List<TreeNode> list = new ArrayList<TreeNode>();

        RestActionReporter ar = new RestActionReporter();
        ar.setActionDescription("Monitoring Data");
        ar.setMessage("");
        ar.setSuccess();
        // ar.getExtraProperties().put("jmxServiceUrls", jmxUrls);
        ActionReportResult result = new ActionReportResult(ar);
        MonitoringRuntimeDataRegistry monitoringRegistry = RestService.getMonitoringRegistry();

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
            // forward the request to instance
            Client client = null;

            try {
                client = Client.create();
                // TODO need to get actual host:port for instanceName
                String hostName = "localHost";
                String portNumber = "14848";
                final String MONITORING_DOMAIN = "/monitoring/domain/";
                String forwardURL = "http://" + hostName + ':' + portNumber + MONITORING_DOMAIN + path;
                ClientResponse response = client.resource(forwardURL ).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
                ClientResponse.Status status = ClientResponse.Status.fromStatusCode(response.getStatus());
                if(status.getFamily() == javax.ws.rs.core.Response.Status.Family.SUCCESSFUL) {
                    String jsonDoc = response.getEntity(String.class);
                    Map responseMap = MarshallingUtils.buildMapFromDocument(jsonDoc);
                    Map resultExtraProperties = (Map)responseMap.get("extraProperties");
                    if(resultExtraProperties != null) {
                        Properties responseExtraProperties = ar.getExtraProperties();
                        responseExtraProperties.put("entity", resultExtraProperties.get("entity"));
                        @SuppressWarnings({"unchecked"}) Map<String, String> childResources = (Map<String, String>) resultExtraProperties.get("childResources");
                        for (Map.Entry<String, String> entry : childResources.entrySet()) {
                            URL targetURL = null;
                            try {
                                URL originalURL = new URL(entry.getValue());
                                //TODO need to figure out what is DAS's hostname and port number (may be able to figure it out from Grizly Adapater)
                                targetURL = new URL(originalURL.getProtocol(), "localhost", 4848, originalURL.getFile() );
                            } catch (MalformedURLException e) {
                                //TODO There was an exception while parsing URL. Need to decide what to do. For now ignore the child entry
                            }
                            entry.setValue(targetURL.toString());
                        }
                        responseExtraProperties.put("childResources", childResources);
                    }
                }
            } finally {
                if(client != null) {
                    client.destroy();
                }
            }
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
                links.put(node.getName(), getElementLink(uriInfo, node.getName()));
            }

        }
        ar.getExtraProperties().put("childResources", links);

    }


}
