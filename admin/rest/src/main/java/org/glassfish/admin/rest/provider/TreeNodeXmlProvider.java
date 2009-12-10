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
package org.glassfish.admin.rest.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

import org.glassfish.admin.rest.Constants;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.external.statistics.Stats;
import org.glassfish.external.statistics.Statistic;

/**
 * @author Rajeshwar Patil
 */
@Provider
@Produces(MediaType.APPLICATION_XML)
public class TreeNodeXmlProvider extends ProviderUtil implements MessageBodyWriter<List<TreeNode>> {

     @Context
     protected UriInfo uriInfo;

     @Override
     public long getSize(final List<TreeNode> proxy, final Class<?> type, final Type genericType,
               final Annotation[] annotations, final MediaType mediaType) {
          return -1;
     }


     @Override
     public boolean isWriteable(final Class<?> type, final Type genericType,
               final Annotation[] annotations, final MediaType mediaType) {
         if ("java.util.List<org.glassfish.flashlight.datatree.TreeNode>".equals(genericType.toString())) {
             return mediaType.isCompatible(MediaType.APPLICATION_XML_TYPE);
         }
         return false;
     }


     @Override
     public void writeTo(final List<TreeNode> proxy, final Class<?> type, final Type genericType,
               final Annotation[] annotations, final MediaType mediaType,
               final MultivaluedMap<String, Object> httpHeaders,
               final OutputStream entityStream) throws IOException, WebApplicationException {
         entityStream.write(getXml(proxy).getBytes());
     }


     private String getXml(List<TreeNode> proxy) {
        String result;
        result ="<" + getTypeKey();

        //display hint if module monitoring levels are OFF.
        if ((proxy.isEmpty()) && (uriInfo.getPath().equalsIgnoreCase("domain"))) {
            result = result + getHint(uriInfo, MediaType.APPLICATION_XML);
        }

        String attributes = getAttributes(proxy);
        if ((attributes != null) && (attributes.length() > 0)) {
            result = result + attributes;
        }

        result = result + getResourcesLinks(proxy);
        result = result + getEndXmlElement(getTypeKey());
        return result;
    }


    private String getTypeKey() {
       return upperCaseFirstLetter(eleminateHypen(getName(uriInfo.getPath(), '/')));
    }


    private String getAttributes(List<TreeNode> nodeList) {
        String result ="";
        //account for primitive values.
        //primitive value constitutes an xml element attribute
        for (TreeNode node : nodeList) {
            //process only the leaf nodes, if any
            if (!node.hasChildNodes()) {
                String value = xmlForPrimitiveValue(node.getValue());
                if (value.length() > 0) {
                    result = result + " " + node.getName() + "=" + quote(xmlForPrimitiveValue(node.getValue()));
                }
            }
        }

        result = result + ">" + "\n\n";

        //account for statistic or stats values
        //statistic or stats value constitutes an xml element
        for (TreeNode node : nodeList) {
            //process only the leaf nodes, if any
            if (!node.hasChildNodes()) {
                //getValue() on leaf node will return one of the following -
                //Statistic object, String object or the object for primitive type
                result = result + xmlForStatisticValue(node.getValue());
            }
        }

        return result;
    }


    private String getResourcesLinks(List<TreeNode> nodeList) {
        String result = "";
        String elementName;
        for (TreeNode node: nodeList) {
            //process only the non-leaf nodes, if any
            if (node.hasChildNodes()) {
                try {
                        result = result + Constants.INDENT; //indent
                        result = result + getStartXmlElement(getResourceKey().replace(' ', '-'));
                        elementName = node.getName();
                        result = result + getElementLink(uriInfo, elementName);
                        result = result + getEndXmlElement(getResourceKey().replace(' ', '-'));
                        result = result + "\n";
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (result.length() > 1) {
            result = result + "\n";
        }

        return result;
    }


    private String xmlForPrimitiveValue(Object value) {
        String result ="";
        if (value != null) {
            if (!((value instanceof Statistic)||(value instanceof Stats))) {
                result =  value.toString();
            }
        }
        return result;
    }


    private String xmlForStatisticValue(Object value) {
        String result ="";
        if (value == null) return result;

        try {
            if (value instanceof Statistic) {
                Statistic statisticObject = (Statistic)value;
                result = result + getStatisticRepresentation(statisticObject);
            } else if (value instanceof Stats) {
                String statResult;
                for (Statistic statistic: ((Stats)value).getStatistics()) {
                    statResult = getStatisticRepresentation(statistic);
                    if (!statResult.equals("")) {
                        result = result + statResult;
                        statResult = "";
                    }
                }
            }
            return result;
        } catch (Exception exception) {
            //log exception message as warning
        }

        return result;
    }


    private String getStatisticRepresentation(Statistic statistic)
            throws IllegalAccessException, InvocationTargetException {
        String result ="";
        //Swithching to getStatistic(Statistic) method i.e Gettting the attribute
        //map provided by monitoring infrastructure instead of introspecting
        Map map = getStatistic(statistic);
        Set<String> attributes = map.keySet();
        Object attributeValue;

        result = result + Constants.INDENT;
        //Replacing slash(/) from the element name with underscore(_) to form the
        //valid xml. Xml element names cannot start with forward slash(/)
        //or dash(-) character.
        result = result + "<" + statistic.getName().replace('/', '_');
        for (String attributeName: attributes) {
            attributeValue = map.get(attributeName);
            result = " " + result + " " + attributeName + "=" +
                 quote(attributeValue.toString());
        }
        result = result + ">";
        result = result + getEndXmlElement(statistic.getName().replace('/', '_'));

        result = result + "\n\n";
        return result;
    }
}
