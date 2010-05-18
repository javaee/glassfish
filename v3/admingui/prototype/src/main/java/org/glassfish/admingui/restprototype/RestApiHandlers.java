/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admingui.restprototype;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import org.glassfish.admingui.common.util.GuiUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jasonlee
 */
public class RestApiHandlers {
    @Handler(id = "getDefaultProxyAttrsViaRest",
            input = {
                    @HandlerInput(name = "parentObjectNameStr", type = String.class, required = true),
                    @HandlerInput(name = "childType", type = String.class, required = true),
                    @HandlerInput(name = "orig", type = Map.class)
            },
            output = {
                    @HandlerOutput(name = "valueMap", type = Map.class)})
    public static void getDefaultValues(HandlerContext handlerCtx) {
        try {
            String endpoint = getRestEndPoint((String) handlerCtx.getInputValue("parentObjectNameStr"),
                    (String) handlerCtx.getInputValue("childType"));
            Map<String, String> orig = (Map) handlerCtx.getInputValue("orig");

            Map<String, String> defaultValues = new HashMap<String, String>();

            String options = options(endpoint, "application/xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(options.getBytes()));
            Element root = doc.getDocumentElement();
            NodeList nl = root.getElementsByTagName("Message-Parameters");
            if (nl.getLength() > 0) {
                NodeList params = nl.item(0).getChildNodes();
                Node child;
                for (int i = 0; i < params.getLength(); i++) {
                    child = params.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        String defaultValue = ((Element) child).getAttribute("Default-Value");
                        if (!"".equals(defaultValue) && (defaultValue != null)) { // null test necessary?
                            String nodeName = child.getNodeName();
                            nodeName = nodeName.substring(0, 1).toUpperCase() + nodeName.substring(1);
                            defaultValues.put(child.getNodeName(), defaultValue);
                        }
                    }
                }
            }

            System.out.println(defaultValues);
            if (orig == null) {
                handlerCtx.setOutputValue("valueMap", defaultValues);
            } else {
                //we only want to fill in any default value that is available. Preserve all other fields user has entered.
                for (String origKey : orig.keySet()) {
                    String defaultV = defaultValues.get(origKey);
                    if (defaultV != null) {
                        orig.put(origKey, defaultV);
                    }
                }
                handlerCtx.setOutputValue("valueMap", orig);
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    // This method may be a really bad idea. :P

    protected static String getRestEndPoint(String parent, String child) {
        String endpoint = parent;

        if (endpoint != null) {
            if (endpoint.startsWith("amx")) {
                // amx:pp=/domain,type=resources
                endpoint = endpoint.substring(endpoint.indexOf("/")); // Strip amx:...
                endpoint = endpoint.replaceAll(",type=", "/");
                endpoint = endpoint.replaceAll(",name=", "/");
            }
        }

        if (child != null) {
            endpoint += "/" + child;
        }

        return "http://localhost:4848/management" + endpoint;
    }

    protected static String get(String address, String responseType) {
        return Client.create().resource(address).accept(responseType).get(String.class);
    }

    protected static String post(String address, Map<String, String> payload, String responseType) {
        WebResource webResource = Client.create().resource(address);

        MultivaluedMap formData = new MultivaluedMapImpl();
        for (final Map.Entry<String, String> entry : payload.entrySet()) {
            formData.putSingle(entry.getKey(), entry.getValue());
        }
        ClientResponse cr = webResource.type("application/x-www-form-urlencoded")
                .accept(responseType)
                .post(ClientResponse.class, formData);
        return cr.toString();
    }

    protected static String put(String address) {
        throw new UnsupportedOperationException();
    }

    protected static String delete(String address) {
        throw new UnsupportedOperationException();
    }

    protected static String options(String address, String responseType) {
        return Client.create().resource(address).accept(responseType).options(String.class);
    }
}