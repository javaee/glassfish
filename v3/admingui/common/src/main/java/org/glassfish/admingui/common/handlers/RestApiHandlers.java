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
import java.net.URLEncoder;
import java.util.*;
import org.glassfish.admingui.common.util.RestResponse;

public class RestApiHandlers {
    public static final String FORM_ENCODING = "application/x-www-form-urlencoded";
    public static final String RESPONSE_TYPE = "application/xml";
    public static final String GUI_TOKEN_FOR_EMPTY_PROPERTY_VALUE = "()";
    public static final Client JERSEY_CLIENT = Client.create();

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
     * found, an Exception is thrown.  This is the REST-based alternative to getProxyAttrs.
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
            String entity = get(endpoint).getResponseBody();

            handlerCtx.setOutputValue("valueMap", getEntityAttrs(entity));
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    @Handler(id = "gf.checkIfEndPointExist",
            input = {
                    @HandlerInput(name = "endpoint", type = String.class, required = true)},
            output = {
                    @HandlerOutput(name = "edit", type = Boolean.class)
            })
    public static void checkIfEndPointExist(HandlerContext handlerCtx) {
        try {
            String endpoint = (String) handlerCtx.getInputValue("endpoint");
            String entity = get(endpoint).getResponseBody();
            Boolean edit = false;
            Map<String, String> tmp = getEntityAttrs(entity);
            if (tmp.size() != 0) {
                edit = true;
            }

            handlerCtx.setOutputValue("edit", edit);
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    /**
     *
     * REST-based version of createProxy
     * @param handlerCtx
     */
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
    public static void createEntity(HandlerContext handlerCtx) {
        Map<String, Object> attrs = (Map) handlerCtx.getInputValue("attrs");
        if (attrs == null) {
            attrs = new HashMap<String, Object>();
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

    @Handler(id = "gf.restExecuteCommand",
            input = {
                    @HandlerInput(name = "endpoint", type = String.class, required = true),
                    @HandlerInput(name = "attrs", type = Map.class, required = true),
                    @HandlerInput(name = "method", type = String.class, defaultValue = "post")},
            output = {
                    @HandlerOutput(name = "result", type = Integer.class),
                    @HandlerOutput(name = "output", type = String.class)
            })
    public static void restExecuteCommand(HandlerContext handlerCtx) {
        Map<String, Object> attrs = (Map<String, Object>) handlerCtx.getInputValue("attrs");
        if (attrs == null) {
            attrs = new HashMap<String, Object>();
        }
        String endpoint = (String) handlerCtx.getInputValue("endpoint");
        String method = ((String) handlerCtx.getInputValue("method")).toLowerCase();
        if ("post".equals(method)) {
            RestResponse response = post(endpoint, attrs);
            handlerCtx.setOutputValue("result", response.getResponseCode());
            handlerCtx.setOutputValue("output", response.getResponseBody());
        } else if ("get".equals(method)) {
            RestResponse response = get(endpoint, attrs);
            handlerCtx.setOutputValue("result", response.getResponseCode());
            handlerCtx.setOutputValue("output", getEntityAttrs(response.getResponseBody()).get("value"));
        }
    }

    /**
     *
     * REST-based version of createProxy
     * @param handlerCtx
     */
    @Handler(id = "gf.updateEntity",
            input = {
                    @HandlerInput(name = "endpoint", type = String.class, required = true),
                    @HandlerInput(name = "attrs", type = Map.class, required = true),
                    @HandlerInput(name = "skipAttrs", type = List.class),
                    @HandlerInput(name = "onlyUseAttrs", type = List.class),
                    @HandlerInput(name = "convertToFalse", type = List.class)},
            output = {
                    @HandlerOutput(name = "result", type = String.class)
            })
    public static void updateEntity(HandlerContext handlerCtx) {
        Map<String, Object> attrs = (Map) handlerCtx.getInputValue("attrs");
        if (attrs == null) {
            attrs = new HashMap<String, Object>();
        }
        String endpoint = (String) handlerCtx.getInputValue("endpoint");

        int status = sendUpdateRequest(endpoint, attrs, (List) handlerCtx.getInputValue("skipAttrs"),
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
                    @HandlerInput(name = "selectedRows", type = List.class, required = true),
                    @HandlerInput(name = "id", type = String.class, defaultValue = "Name"),
                    @HandlerInput(name = "cascade", type = String.class)
            })
    public static void deleteCascade(HandlerContext handlerCtx) {
        try {
            Map<String, Object> payload = new HashMap<String, Object>();
            String endpoint = (String) handlerCtx.getInputValue("endpoint");
            String id = (String) handlerCtx.getInputValue("id");
            String cascade = (String) handlerCtx.getInputValue("cascade");
            if (cascade != null) {
                payload.put("cascade", "false");
            }

            for (Map oneRow : (List<Map>) handlerCtx.getInputValue("selectedRows")) {
                delete(endpoint + "/" + (String) oneRow.get(id), payload);
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    @Handler(id = "gf.getChildList",
        input = {
            @HandlerInput(name = "parentEndpoint", type = String.class, required = true),
            @HandlerInput(name = "childType", type = String.class, required = true),
            @HandlerInput(name = "id", type = String.class, defaultValue = "Name")},
        output = {
            @HandlerOutput(name = "result", type = java.util.List.class)
    })
    public static void getChildList(HandlerContext handlerCtx) {
        try {
            handlerCtx.setOutputValue("result",
                    buildChildEntityList((String)handlerCtx.getInputValue("parentEndpoint"),
                    (String)handlerCtx.getInputValue("childType"), null,
                    (String)handlerCtx.getInputValue("id")));
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    @Handler(id = "gf.getChildrenNamesList",
        input = {
            @HandlerInput(name = "endpoint", type = String.class, required = true),
            @HandlerInput(name = "id", type = String.class, defaultValue = "Name")},
        output = {
            @HandlerOutput(name = "result", type = java.util.List.class)
    })
    public static void getChildrenNamesList(HandlerContext handlerCtx) {
        try {
            handlerCtx.setOutputValue("result",
                    getChildrenNames((String)handlerCtx.getInputValue("endpoint"),
                    (String)handlerCtx.getInputValue("id")));
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    //*******************************************************************************************************************
    //*******************************************************************************************************************
    protected static Map<String, String> buildDefaultValueMap(String endpoint) throws ParserConfigurationException, SAXException, IOException {
        Map<String, String> defaultValues = new HashMap<String, String>();

        String options = options(endpoint, "application/xml");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
	// FIXME: Add error handling at least to the line below.  If it fails
	// FIXME: we should log a message as to why and/or w/ the 
	// FIXME: options.getBytes()
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

    protected static MultivaluedMap buildMultivalueMap(Map<String, Object> payload) {
        MultivaluedMap formData = new MultivaluedMapImpl();
        for (final Map.Entry<String, Object> entry : payload.entrySet()) {
            final Object value = entry.getValue();
            formData.putSingle(entry.getKey(), (value != null) ? value.toString() : value);
        }
        return formData;
    }

    public static int sendCreateRequest(String endpoint, Map<String, Object> attrs, List<String> skipAttrs, List<String> onlyUseAttrs, List<String> convertToFalse) {
        removeSpecifiedAttrs(attrs, skipAttrs);
        attrs = buildUseOnlyAttrMap(attrs, onlyUseAttrs);
        attrs = convertNullValuesToFalse(attrs, convertToFalse);
        attrs = fixKeyNames(attrs);

        return post(endpoint, attrs).getResponseCode();
    }

    // This will send an update request.  Currently, this calls post just like the create does,
    // but the REST API will be modified to use PUT for updates, a more correct use of HTTP
    public static int sendUpdateRequest(String endpoint, Map<String, Object> attrs, List<String> skipAttrs, List<String> onlyUseAttrs, List<String> convertToFalse) {
        removeSpecifiedAttrs(attrs, skipAttrs);
        attrs = buildUseOnlyAttrMap(attrs, onlyUseAttrs);
        attrs = convertNullValuesToFalse(attrs, convertToFalse);
        attrs = fixKeyNames(attrs);

        return post(endpoint, attrs).getResponseCode();
    }

    protected static Map<String, Object> fixKeyNames(Map<String, Object> map) {
        Map<String, Object> results = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey().substring(0, 1).toLowerCase() + entry.getKey().substring(1);
            Object value = entry.getValue();
            results.put(key, value);
        }

        return results;
    }

    protected static void removeSpecifiedAttrs(Map<String, Object> attrs, List<String> removeList) {
        if (removeList == null || removeList.size() <= 0) {
            return;
        }
        Set<Map.Entry<String, Object>> attrSet = attrs.entrySet();
        Iterator<Map.Entry<String, Object>> iter = attrSet.iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Object> oneEntry = iter.next();
            if (removeList.contains(oneEntry.getKey())) {
                iter.remove();
            }
        }
    }

    protected static Map buildUseOnlyAttrMap(Map<String, Object> attrs, List<String> onlyUseAttrs) {
        if (onlyUseAttrs != null) {
            Map newAttrs = new HashMap();
            for (String key : onlyUseAttrs) {
                if (attrs.keySet().contains(key)) {
                    newAttrs.put(key, attrs.get(key));
                }
            }
            return newAttrs;
        } else {
            return attrs;
        }

    }

    // This is ugly, but I'm trying to figure out why the cleaner code doesn't work :(
    protected static Map<String, Object> convertNullValuesToFalse(Map<String, Object> attrs, List<String> convertToFalse) {
        if (convertToFalse != null) {
            Map<String, Object> newAttrs = new HashMap<String, Object>();
            String key;

            for (Map.Entry<String, Object> entry : attrs.entrySet()) {
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

    public static Map<String, String> getEntityAttrs(String entity) {
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
            e.printStackTrace();
        }

        return attrs;
    }

    /**
     * Converts the first letter of the given string to Uppercase.
     *
     * @param string the input string
     * @return the string with the Uppercase first letter
     */
    public static String upperCaseFirstLetter(String string) {
        if (string == null || string.length() <= 0) {
            return string;
        }
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static List<String> getChildResourceList(String document) throws SAXException, IOException, ParserConfigurationException {
        List<String> children = new ArrayList<String>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(document.getBytes()));
        Element root = doc.getDocumentElement();
        NodeList nl = root.getElementsByTagName("childResource");
        if (nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node child = nl.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    children.add(child.getTextContent());
                }
            }
        }

        return children;
    }

    /**
     * Given the parent URL and the desired childType, this method will build a List of Maps that
     * contains each child entities values.  In addition to the entity values, each row will
     * have a field, 'selected', set to false, as well as the URL encoded entity name ('encodedName').
     *
     * @param parent
     * @param childType
     * @param skipList
     * @return
     * @throws Exception
     */
    public static List<Map> buildChildEntityList(String parent, String childType, List skipList, String id) throws Exception {
        String endpoint = parent.endsWith("/") ?
            parent + childType : parent + "/" + childType;
        boolean hasSkip = true;
        if (skipList == null ){
            hasSkip = false;
        }
        boolean convert = false;
        if (childType.equals("property")){
            convert = true;
        }

        List<Map> childElements = new ArrayList<Map>();
        try {
            final RestResponse response = RestApiHandlers.get(endpoint);
            String parentString = response.getResponseBody();
            List<String> childUrls = getChildResourceList(parentString);
            for (String childUrl : childUrls) {
                String childString = RestApiHandlers.get(childUrl).getResponseBody();
                Map<String, String> entity = RestApiHandlers.getEntityAttrs(childString);
                HashMap<String, Object> oneRow = new HashMap<String, Object>();

                if (hasSkip && skipList.contains(entity.get(id))) {
                    continue;
                }

                oneRow.put("selected", false);
                for(String attrName : entity.keySet()){
                    oneRow.put(attrName, getA(entity, attrName, convert));
                }
                oneRow.put("encodedName", URLEncoder.encode(entity.get(id), "UTF-8"));
                oneRow.put("Name", entity.get(id));
                childElements.add(oneRow);
            }
        } catch (Exception e) {
            throw e;
        }
        return childElements;
    }

    private static String getA(Map<String, String> attrs,  String key, boolean convert){
        Object val = attrs.get(key);
        if (val == null){
            return "";
        }
        return (convert && (val.equals(""))) ? GUI_TOKEN_FOR_EMPTY_PROPERTY_VALUE : val.toString();
    }

    /**
     * Given the parent URL and the desired childType, this method will build a List of Strings that
     * contains child entity names.
     *
     * @param endpoint
     * @param id
     * @return
     * @throws Exception
     */
    public static List<String> getChildrenNames(String endpoint, String id) throws Exception {
        List<String> childElements = new ArrayList<String>();
        try {
            String foo = RestApiHandlers.get(endpoint).getResponseBody();
            List<String> childUrls = getChildResourceList(foo);
            for (String childUrl : childUrls) {                
                childElements.add(childUrl.substring(childUrl.lastIndexOf("/")+1));
            }
        } catch (Exception e) {
            throw e;
        }
        return childElements;
    }

    //******************************************************************************************************************
    // Jersey client methods
    //******************************************************************************************************************
    public static RestResponse get(String address) {
        return get(address, new HashMap<String, Object>());
    }
    
    public static RestResponse get(String address, Map<String, Object> payload) {
        return RestResponse.getRestResponse(JERSEY_CLIENT.resource(address)
                .queryParams(buildMultivalueMap(payload))
                .accept(RESPONSE_TYPE)
                .get(ClientResponse.class));
    }

    public static RestResponse post(String address, Map<String, Object> payload) {
        WebResource webResource = JERSEY_CLIENT.resource(address);
        MultivaluedMap formData = buildMultivalueMap(payload);
        ClientResponse cr = webResource.post(ClientResponse.class, formData);
        checkStatusForSuccess(cr);
        return RestResponse.getRestResponse(cr);
    }

    // TODO: This will be implemented when the REST API is updated to use PUTs for updates as is planned
    public static String put(String address) {
        throw new UnsupportedOperationException();
    }

    public static RestResponse delete(String address, Map<String, Object> payload) {
        WebResource webResource = JERSEY_CLIENT.resource(address);
        ClientResponse cr = webResource.queryParams(buildMultivalueMap(payload)).delete(ClientResponse.class);
        checkStatusForSuccess(cr);
        return RestResponse.getRestResponse(cr);
    }

    public static String options(String address, String responseType) {
        return JERSEY_CLIENT.resource(address)
                .accept(responseType)
                .options(String.class);
    }

    public static void checkStatusForSuccess(ClientResponse cr) {
        int status = cr.getStatus();
        if ((status < 200) || (status > 299)) {
            throw new RuntimeException(cr.toString());
        }
    }
    //******************************************************************************************************************
    // Jersey client methods
    //******************************************************************************************************************
}
