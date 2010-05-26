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
package org.glassfish.admingui.common.handlers;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import org.glassfish.admingui.common.util.GuiUtil;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

public class RestApiHandlers {
    public static final String FORM_ENCODING = "application/x-www-form-urlencoded";
    public static final String RESPONSE_TYPE = "application/xml";

    @Handler(id = "gf.getDefaultValues",
            input = {
                    @HandlerInput(name = "endpoint", type = String.class, required = true),
                    @HandlerInput(name = "orig", type = Map.class)
            },
            output = {
                    @HandlerOutput(name = "valueMap", type = Map.class)
            })
    public static void getDefaultValues(HandlerContext handlerCtx) {
        try {
            String endpoint = (String) handlerCtx.getInputValue("endpoint");
            Map<String, String> orig = (Map) handlerCtx.getInputValue("orig");

            Map<String, String> defaultValues = buildDefaultValueMap(endpoint);

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

    /**
     * For the given REST endpoint, retrieve the values of the entity and return those as a Map.  If the entity is not
     * found, an Exception is thrown.
     */
    @Handler(id = "gf.getEntityAttrs",
            input = {
                    @HandlerInput(name = "endpoint", type = String.class, required = true)},
            output = {
                    @HandlerOutput(name = "valueMap", type = Map.class)
            })
    public static void getEntityAttrs(HandlerContext handlerCtx) {
        try {
            String endpoint = (String) handlerCtx.getInputValue("endpoint");
            String entity = get(endpoint);

            handlerCtx.setOutputValue("valueMap", getEntityAttrs(entity));
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    @Handler(id = "gf.createEntity",
            input = {
                    @HandlerInput(name = "endpoint", type = String.class, required = true),
                    @HandlerInput(name = "attrs", type = Map.class, required = true),
                    @HandlerInput(name = "skipAttrs", type = List.class),
                    @HandlerInput(name = "onlyUseAttrs", type = List.class),
                    @HandlerInput(name = "convertToFalse", type = List.class)},
            output = {
                    @HandlerOutput(name = "result", type = String.class)
            })
    public static void createProxy(HandlerContext handlerCtx) {
        Map<String, String> attrs = (Map) handlerCtx.getInputValue("attrs");
        if (attrs == null) {
            attrs = new HashMap<String, String>();
        }
        String endpoint = (String) handlerCtx.getInputValue("endpoint");

        int status = sendCreateRequest(endpoint, attrs, (List) handlerCtx.getInputValue("skipAttrs"),
                (List) handlerCtx.getInputValue("onlyUseAttrs"), (List) handlerCtx.getInputValue("convertToFalse"));

        if ((status != 200) && (status != 201)) {
            GuiUtil.getLogger().severe("CreateProxy failed.  parent=" + endpoint + "; attrs =" + attrs);
            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.error.checkLog"));
            return;
        }

        handlerCtx.setOutputValue("result", endpoint);
    }

    /**
     * // TODO: just these resources?
     * deleteCascade handles delete for jdbc connection pool and connector connection pool
     * The dependent resources jdbc resource and connector resource are deleted on deleting
     * the pools
     */
    @Handler(id = "gf.deleteCascade",
            input = {
                    @HandlerInput(name = "endpoint", type = String.class, required = true),
                    @HandlerInput(name = "selectedRows", type = List.class, required = true)
            })
    public static void deleteCascade(HandlerContext handlerCtx) {
        try {
            Map<String, String> payload = new HashMap<String, String>();
            String endpoint = (String) handlerCtx.getInputValue("objectNameStr");
            payload.put("cascade", "true");

            for (Map oneRow : (List<Map>) handlerCtx.getInputValue("selectedRows")) {
                delete(endpoint + "/" + (String) oneRow.get("Name"), payload);
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    protected static Map<String, String> buildDefaultValueMap(String endpoint) throws ParserConfigurationException, SAXException, IOException {
        Map<String, String> defaultValues = new HashMap<String, String>();

        String options = options(endpoint, "application/xml");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(options.getBytes()));
        Element root = doc.getDocumentElement();
        NodeList nl = root.getElementsByTagName("messageParameters");
        if (nl.getLength() > 0) {
            NodeList params = nl.item(0).getChildNodes();
            Node child;
            for (int i = 0; i < params.getLength(); i++) {
                child = params.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    String defaultValue = ((Element) child).getAttribute("defaultValue");
                    if (!"".equals(defaultValue) && (defaultValue != null)) { // null test necessary?
                        String nodeName = child.getNodeName();
                        nodeName = nodeName.substring(0, 1).toUpperCase() + nodeName.substring(1);
                        defaultValues.put(nodeName, defaultValue);
                    }
                }
            }
        }
        return defaultValues;
    }

    protected static MultivaluedMap buildMultivalueMap(Map<String, String> payload) {
        MultivaluedMap formData = new MultivaluedMapImpl();
        for (final Map.Entry<String, String> entry : payload.entrySet()) {
            formData.putSingle(entry.getKey(), entry.getValue());
        }
        return formData;
    }

    public static int sendCreateRequest(String endpoint, Map<String, String> attrs, List<String> skipAttrs, List<String> onlyUseAttrs, List<String> convertToFalse) {
        //Should specify either skipAttrs or onlyUseAttrs
        removeSpecifiedAttrs(attrs, skipAttrs);

        if (onlyUseAttrs != null) {
            Map newAttrs = new HashMap();
            for (String key : onlyUseAttrs) {
                if (attrs.keySet().contains(key)) {
                    newAttrs.put(key, attrs.get(key));
                }
            }
            attrs = newAttrs;
        }
        attrs = convertNullValuesToFalse(attrs, convertToFalse);
        attrs = fixKeyNames(attrs);

        return post(endpoint, attrs);
    }

    protected static Map<String, String> fixKeyNames(Map<String, String> map) {
        Map<String, String> results = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey().substring(0, 1).toLowerCase() + entry.getKey().substring(1);
            String value = entry.getValue();
            results.put(key, value);
        }

        return results;
    }

    protected static void removeSpecifiedAttrs(Map<String, String> attrs, List<String> removeList) {
        if (removeList == null || removeList.size() <= 0) {
            return;
        }
        Set<Map.Entry<String, String>> attrSet = attrs.entrySet();
        Iterator<Map.Entry<String, String>> iter = attrSet.iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> oneEntry = iter.next();
            if (removeList.contains(oneEntry.getKey())) {
                iter.remove();
            }
        }
    }

    // This is ugly, but I'm trying to figure out why the cleaner code doesn't work :(
    protected static Map<String, String> convertNullValuesToFalse(Map<String, String> attrs, List<String> convertToFalse) {
        if (convertToFalse != null) {
            Map<String, String> newAttrs = new HashMap<String, String>();
            String key;

            for (Map.Entry<String, String> entry : attrs.entrySet()) {
                key = entry.getKey();
                if (convertToFalse.contains(key) && ((entry.getValue() == null) || "null".equals(entry.getValue()))) {
                    newAttrs.put(key, "false");
                } else {
                    newAttrs.put(key, entry.getValue());
                }
            }
            return newAttrs;
        } else {
            return attrs;
        }
    }

    protected static Map<String, String> getEntityAttrs(String entity) {
        Map<String, String> attrs = new HashMap<String, String>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(entity.getBytes()));
            Element root = doc.getDocumentElement();
            NamedNodeMap nnm = root.getAttributes();
            for (int i = 0; i < nnm.getLength(); i++) {
                Node node = nnm.item(i);
                attrs.put(upperCaseFirstLetter(node.getNodeName()), node.getNodeValue());
            }
        } catch (Exception e) {
        }

        return attrs;
    }

    /**
     * Converts the first letter of the given string to Uppercase.
     *
     * @param string the input string
     * @return the string with the Uppercase first letter
     */
    protected static String upperCaseFirstLetter(String string) {
        if (string == null || string.length() <= 0) {
            return string;
        }
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    //******************************************************************************************************************
    // Jersey client methods
    //******************************************************************************************************************

    protected static String get(String address) {
        return Client.create().resource(address)
                .accept(RESPONSE_TYPE)
                .get(String.class);
    }

    protected static int post(String address, Map<String, String> payload) {
        WebResource webResource = Client.create().resource(address);
        MultivaluedMap formData = buildMultivalueMap(payload);
        ClientResponse cr = webResource.post(ClientResponse.class, formData);
        checkStatusForSuccess(cr);
        return cr.getStatus();
    }

    // TODO: This will be implemented when the REST API is updated to use PUTs for updates as is planned
    protected static String put(String address) {
        throw new UnsupportedOperationException();
    }

    protected static int delete(String address, Map<String, String> payload) {
        WebResource webResource = Client.create().resource(address);
        ClientResponse cr = webResource.queryParams(buildMultivalueMap(payload)).delete(ClientResponse.class);
        checkStatusForSuccess(cr);
        return cr.getStatus();
    }

    protected static String options(String address, String responseType) {
        return Client.create().resource(address)
                .accept(responseType)
                .options(String.class);
    }

    protected static void checkStatusForSuccess(ClientResponse cr) {
        int status = cr.getStatus();
        if ((status < 200) || (status > 299)) {
            throw new RuntimeException(cr.toString());
        }
    }
    //******************************************************************************************************************
    // Jersey client methods
    //******************************************************************************************************************
}