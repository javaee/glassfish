/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admingui.common.handlers;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import java.util.logging.Level;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.MiscUtil;
import org.glassfish.admingui.common.util.RestResponse;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

public class RestApiHandlers {
    public static final String FORM_ENCODING = "application/x-www-form-urlencoded";
    // FIXME: Change this to "application/json"
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
     *	<p> For the given REST endpoint, retrieve the values of the entity and
     *	    return those as a Map.  If the entity is not found, an Exception is
     *	    thrown.  This is the REST-based alternative to getProxyAttrs.</p>
     */
    @Handler(id = "gf.getEntityAttrs",
            input = {
                    @HandlerInput(name = "endpoint", type = String.class, required = true),
                    @HandlerInput(name = "currentMap", type = Map.class),
		            @HandlerInput(name = "key", type=String.class, defaultValue="entity")},
            output = {
                    @HandlerOutput(name = "valueMap", type = Map.class)
            })
    public static void getEntityAttrs(HandlerContext handlerCtx) {
        // Get the inputs...
        String key = (String) handlerCtx.getInputValue("key");
        String endpoint = (String) handlerCtx.getInputValue("endpoint");
        Map<String, Object> currentMap = (Map<String, Object>) handlerCtx.getInputValue("currentMap");
        Map<String, Object> valueMap = null;

        try {
            valueMap = getEntityAttrs(endpoint, key);
            // Current values already set?
            if (currentMap != null) {
                valueMap.putAll(currentMap);
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }

        // Return the Map
        handlerCtx.setOutputValue("valueMap", valueMap);
    }

    @Handler(id = "gf.checkIfEndPointExist",
            input = {
                    @HandlerInput(name = "endpoint", type = String.class, required = true)},
            output = {
                    @HandlerOutput(name = "exists", type = Boolean.class)
            })
    public static void checkIfEndPointExist(HandlerContext handlerCtx) {
        handlerCtx.setOutputValue("exists", get((String) handlerCtx.getInputValue("endpoint")).isSuccess());
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

        RestResponse response  = sendCreateRequest(endpoint, attrs, (List) handlerCtx.getInputValue("skipAttrs"),
                (List) handlerCtx.getInputValue("onlyUseAttrs"), (List) handlerCtx.getInputValue("convertToFalse"));

        Map resultMap = parseResponse(response, handlerCtx, endpoint, attrs);
        //??? I believe this should return a Map, whats the point of returning the endpoint that was passed in.
        //But i haven't looked through all the code, so decide to leave it for now.
        handlerCtx.setOutputValue("result", endpoint);
    }

    /**
     *	<p> This handler can be used to execute a generic REST request.  It
     *	    will return a Java data structure based on the response of the
     *	    REST request.  'data' and 'attrs' are mutually exclusive.  'data'
     *	    is used to pass RAW data to the endpoint (such as JSON).</p>
     */
    @Handler(id = "gf.restRequest",
            input = {
                    @HandlerInput(name="endpoint", type=String.class, required=true),
                    @HandlerInput(name="attrs", type=Map.class, required=false),
                    @HandlerInput(name="data", type=Object.class, required=false),
                    @HandlerInput(name="contentType", type=String.class, required=false),
                    @HandlerInput(name="method", type=String.class, defaultValue="post")},
            output = {
                    @HandlerOutput(name="result", type=Map.class)})
    public static void restRequest(HandlerContext handlerCtx) {
        Map<String, Object> attrs = (Map<String, Object>) handlerCtx.getInputValue("attrs");
        String endpoint = (String) handlerCtx.getInputValue("endpoint");
        String method = (String) handlerCtx.getInputValue("method");
        handlerCtx.setOutputValue("result",  restRequest(endpoint, attrs, method, handlerCtx));
    }

    public static Map<String, Object> restRequest(String endpoint, Map<String, Object> attrs, String method, HandlerContext handlerCtx) {
	boolean useData = false;

	Object data = null;
        if (attrs == null) {
	    try {
                data = (handlerCtx == null) ? null : handlerCtx.getInputValue("data");
            } catch (Exception e) {
                //
            }
	    if (data != null) {
		// We'll send the raw data
		useData = true;
	    } else {
		// Initialize the attributes to an empty map
		attrs = new HashMap<String, Object>();
	    }
        }
        method = method.toLowerCase();

// FIXME: Move to fine level later.
        GuiUtil.getLogger().log(Level.INFO, "restRequest: endpoint={0}\nattrs={1}\nmethod={2}", new Object[]{endpoint, attrs, method});

	// Execute the request...
        RestResponse response = null;
        if ("post".equals(method)) {
	    if (useData) {
		response = post(endpoint, data, (String) handlerCtx.getInputValue("contentType"));
	    } else {
		response = post(endpoint, attrs);
	    }
        } else if ("get".equals(method)) {
            response = get(endpoint, attrs);
        } else if ("delete".equals(method)) {
            response = delete(endpoint, attrs);
        }

        return parseResponse(response, handlerCtx, endpoint, attrs);
    }

    /**
     *
     */
    private static Map<String, Object> parseResponse(RestResponse response, HandlerContext handlerCtx, String endpoint, Map attrs) {
        // Parse the response
        if (response != null) {
            try {
                int status = response.getResponseCode();
                if ((status != 200) && (status != 201)) {
                    GuiUtil.getLogger().severe(
                            "RestResponse.getResponse() failed.  endpoint = '"
                                    + endpoint + "'; attrs = '" + attrs + "'; RestResponse: "
                                    + response);
                    // If this is called from jsf, stop processing/show error.
                    if (handlerCtx != null) {
                        Object msgs = response.getResponse().get("messages");
                        if (msgs == null) {
                            GuiUtil.handleError(handlerCtx, "REST Request '"
                                    + endpoint + "' failed with response code '"
                                    + status + "'.");
                        } else if (msgs instanceof List) {
                            StringBuilder builder = new StringBuilder("");
                            for (Object obj : ((List<Object>) msgs)) {
                                if ((obj instanceof Map) && ((Map<String, Object>) obj).containsKey("message")) {
                                    obj = ((Map<String, Object>) obj).get("message");
                                }
                                builder.append(obj.toString());
                            }
                            GuiUtil.handleError(handlerCtx, builder.toString());
                        } else if (msgs instanceof Map) {
                            GuiUtil.handleError(handlerCtx,
                                    ((Map<String, Object>) msgs).get("message").toString());
                        } else {
                            throw new RuntimeException("Unexpected message type.");
                        }
                    }
                }
                return response.getResponse();
            } catch (Exception ex) {
                GuiUtil.getLogger().severe(
                        "RestResponse.getResponse() failed.  endpoint = '"
                                + endpoint + "'; attrs = '" + attrs + "'; RestResponse: "
                                + response);
                if (handlerCtx != null) {
                    //If this is called from the jsf as handler, we want to stop processing and show error
                    //instead of dumping the exception on screen.
                    // GuiUtil.getMessage("error.checkServerLog")
                    GuiUtil.handleException(handlerCtx, ex);
                } else {
                    //if this is called by other java handler, we tell the called handle the exception.
                    throw new RuntimeException(ex);
                }
            }
        }
        return null;
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

        RestResponse response = sendUpdateRequest(endpoint, attrs, (List) handlerCtx.getInputValue("skipAttrs"),
                (List) handlerCtx.getInputValue("onlyUseAttrs"), (List) handlerCtx.getInputValue("convertToFalse"));

        if (!response.isSuccess()) {
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
                    @HandlerInput(name = "id", type = String.class, defaultValue = "name"),
                    @HandlerInput(name = "cascade", type = String.class)
            })
    public static void deleteCascade(HandlerContext handlerCtx) {
        try {
            Map<String, Object> payload = new HashMap<String, Object>();
            String endpoint = (String) handlerCtx.getInputValue("endpoint");
            String id = (String) handlerCtx.getInputValue("id");
            String cascade = (String) handlerCtx.getInputValue("cascade");
            if (cascade != null) {
                payload.put("cascade", cascade);
            }

            for (Map oneRow : (List<Map>) handlerCtx.getInputValue("selectedRows")) {
                RestResponse response = delete(endpoint + "/" +
                        URLEncoder.encode((String) oneRow.get(id), "UTF-8"), payload);
                if (!response.isSuccess()) {
                    GuiUtil.handleError(handlerCtx, "Unable to delete the resource " + (String) oneRow.get(id));
                }
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }


    /*
     * Return List<Map<String, String>> which is for displaying as table in a the page.
     * If a skipList is specified,  any child whose id is specified in the skipList will not be included.
     * If a includeList is specifed,  any child whose id is NOT specified in the includeList will NOT be included.
     */
    @Handler(id = "gf.getChildList",
        input = {
            @HandlerInput(name = "parentEndpoint", type = String.class, required = true),
            @HandlerInput(name = "childType", type = String.class, required = true),
            @HandlerInput(name = "skipList", type = List.class, required = false),
            @HandlerInput(name = "includeList", type = List.class, required = false),
            @HandlerInput(name = "id", type = String.class, defaultValue = "name")},
        output = {
            @HandlerOutput(name = "result", type = java.util.List.class)
    })
    public static void getChildList(HandlerContext handlerCtx) {
        try {
            handlerCtx.setOutputValue("result",
                    buildChildEntityList((String)handlerCtx.getInputValue("parentEndpoint"),
                    (String)handlerCtx.getInputValue("childType"),
                    (List)handlerCtx.getInputValue("skipList"),
                    (List)handlerCtx.getInputValue("includeList"),
                    (String)handlerCtx.getInputValue("id")));
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    @Handler(id = "gf.getChildrenNamesList",
        input = {
            @HandlerInput(name = "endpoint", type = String.class, required = true),
            @HandlerInput(name = "id", type = String.class, defaultValue = "name")},
        output = {
            @HandlerOutput(name = "result", type = java.util.List.class)
    })
    public static void getChildrenNamesList(HandlerContext handlerCtx) {
        try {
            handlerCtx.setOutputValue("result", new ArrayList(getChildMap((String)handlerCtx.getInputValue("endpoint")).keySet()));
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }


    public static Map getAttributesMap(String endpoint) {
        RestResponse response = get(endpoint);
        if (!response.isSuccess()) {
            return new HashMap();
        }
        return getEntityAttrs(endpoint, "entity");
    }


    //*******************************************************************************************************************
    //*******************************************************************************************************************
    protected static Map<String, String> buildDefaultValueMap(String endpoint) throws ParserConfigurationException, SAXException, IOException {
        Map<String, String> defaultValues = new HashMap<String, String>();

        RestResponse response = options(endpoint, "application/xml");
        Map<String, Object> data = (Map<String, Object>)response.getResponse().get("data");
        Map<String, Object> extraProperties = (Map<String, Object>) data.get("extraProperties");
        List<Map<String, Object>> methods = (List<Map<String, Object>>) extraProperties.get("methods");
        for (Map<String, Object> method : methods) {
            if ("POST".equals(method.get("name"))) {
                Map<String, Object> messageParameters = (Map<String, Object>) method.get("messageParameters");
                for (Map.Entry<String, Object> entry : messageParameters.entrySet()) {
                    String param = entry.getKey();
                    String defaultValue = (String) ((Map) entry.getValue()).get("defaultValue");
                    if (!"".equals(defaultValue) && (defaultValue != null)) { // null test necessary?
                        defaultValues.put(param, defaultValue);
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
	    if (value instanceof List) {
		String key = entry.getKey();
		for (Object obj : ((List) value)) {
		    try {
			formData.add(key, obj);
		    } catch (ClassCastException ex) {
			// FIXME: Change to fine()
			GuiUtil.getLogger().info("Unable to add key (" + key + ") w/ value (" + obj + ").");
			// Allow it to continue b/c this property most likely
			// should have been excluded for this request
		    }
		}
	    } else {
		//formData.putSingle(entry.getKey(), (value != null) ? value.toString() : value);
		try {
		    formData.putSingle(entry.getKey(), value);
		} catch (ClassCastException ex) {
		    // FIXME: Change to fine()
		    GuiUtil.getLogger().info("Unable to add key (" + entry.getKey() + ") w/ value (" + value + ").");
		    // Allow it to continue b/c this property most likely
		    // should have been excluded for this request
		}
	    }
        }
        return formData;
    }

    public static RestResponse sendCreateRequest(String endpoint, Map<String, Object> attrs, List<String> skipAttrs, List<String> onlyUseAttrs, List<String> convertToFalse) {
        removeSpecifiedAttrs(attrs, skipAttrs);
        attrs = buildUseOnlyAttrMap(attrs, onlyUseAttrs);
        attrs = convertNullValuesToFalse(attrs, convertToFalse);
        attrs = fixKeyNames(attrs);

        return post(endpoint, attrs);
    }

    // This will send an update request.  Currently, this calls post just like the create does,
    // but the REST API will be modified to use PUT for updates, a more correct use of HTTP
    public static RestResponse sendUpdateRequest(String endpoint, Map<String, Object> attrs, List<String> skipAttrs, List<String> onlyUseAttrs, List<String> convertToFalse) {
        removeSpecifiedAttrs(attrs, skipAttrs);
        attrs = buildUseOnlyAttrMap(attrs, onlyUseAttrs);
        attrs = convertNullValuesToFalse(attrs, convertToFalse);
        attrs = fixKeyNames(attrs);

        return post(endpoint, attrs);
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

    public static Map<String, Object> getEntityAttrs(String endpoint, String key) {
        Map<String, Object> valueMap = new HashMap<String, Object>();
        try {
            // Use restRequest to query the endpoint
            Map<String, Object> result = restRequest(endpoint, (Map<String, Object>) null, "get", null);
            int responseCode = (Integer) result.get("responseCode");
            if ((responseCode < 200) || (responseCode > 299)) {
                throw new RuntimeException((String) result.get("responseBody"));
            }

            // Pull off the attribute Map
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            Map<String, Object> extraProperties = (Map<String, Object>) data.get("extraProperties");
            if (extraProperties.containsKey(key)) {
                valueMap = (Map<String, Object>) extraProperties.get(key);
            }
        } catch (Exception ex) {
	    throw new RuntimeException(ex);
        }

        return valueMap;
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
        Document doc = MiscUtil.getDocument(document);
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

    public static Map<String,Map> getMonitoringStatInfo(String document) throws SAXException, IOException, ParserConfigurationException {
        Map<String, Map> result = new HashMap<String, Map>();
        Document doc = MiscUtil.getDocument(document);
        Element root = doc.getDocumentElement();
        NodeList nl = root.getChildNodes();
        //NamedNodeMap nodeAttrs;
        if (nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node child = nl.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Map<String, String> attrs = new HashMap<String, String>();
                    NamedNodeMap nnm = child.getAttributes();
                    for (int j = 0; j < nnm.getLength(); j++) {
                        Node node = nnm.item(j);
                        attrs.put(node.getNodeName(), node.getNodeValue());
                    }
                    result.put(child.getNodeName(), attrs);
                }
            }
        }

        return result;
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
    public static List<Map> buildChildEntityList(String parent, String childType, List skipList, List includeList, String id) throws Exception {

        String endpoint = parent.endsWith("/") ?  parent + childType : parent + "/" + childType;
        boolean hasSkip = (skipList != null);
        boolean hasInclude = (includeList != null);
        boolean convert = childType.equals("property");

        List<Map> childElements = new ArrayList<Map>();
        try {
            List<String> childUrls = getChildList(endpoint);
            for (String childUrl : childUrls) {
                Map<String, Object> entity = getEntityAttrs(childUrl, "entity");
                HashMap<String, Object> oneRow = new HashMap<String, Object>();

                if (hasSkip && skipList.contains(entity.get(id))) {
                    continue;
                }

                if (hasInclude && (!includeList.contains(entity.get(id)))) {
                    continue;
                }

                oneRow.put("selected", false);
                for(String attrName : entity.keySet()){
                    oneRow.put(attrName, getA(entity, attrName, convert));
                }
                oneRow.put("encodedName", URLEncoder.encode(entity.get(id).toString(), "UTF-8"));
                oneRow.put("name", entity.get(id));
                childElements.add(oneRow);
            }
        } catch (Exception e) {
            throw e;
        }
        return childElements;
    }

    private static String getA(Map<String, Object> attrs,  String key, boolean convert){
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
     * @return
     * @throws Exception
     */
    public static List<String> getChildList(String endpoint) throws Exception {
        List<String> childElements = new ArrayList<String>();
        Map<String, String> childResources = getChildMap(endpoint);
        if (childResources != null) {
            childElements.addAll(childResources.values());
        }
        return childElements;
    }

    public static Map<String, String> getChildMap(String endpoint) throws Exception {
        Map<String, String> childElements = new TreeMap<String, String>();
        Map responseMap = restRequest(endpoint, new HashMap<String, Object>(), "get", null);
        Map data = (Map) responseMap.get("data");
        Map extraProperties = (Map) data.get("extraProperties");
        if (extraProperties != null) {
            childElements = (Map<String, String>) extraProperties.get("childResources");
            if (childElements == null) {
                childElements = new TreeMap<String, String>();
            }
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

    public static RestResponse post(String address, Object payload, String contentType) {
        WebResource webResource = JERSEY_CLIENT.resource(address);
	if (contentType == null) {
	    contentType = MediaType.APPLICATION_JSON;
	}
        if (payload instanceof Map) {
            payload = buildMultivalueMap((Map<String, Object>)payload);
        }
        ClientResponse cr = webResource.header("Content-Type", contentType).
		accept(RESPONSE_TYPE).post(ClientResponse.class, payload);
        //checkStatusForSuccess(cr);
        RestResponse rr = RestResponse.getRestResponse(cr);
        return rr;
    }

    public static RestResponse post(String address, Map<String, Object> payload) {
        WebResource webResource = JERSEY_CLIENT.resource(address);
        MultivaluedMap formData = buildMultivalueMap(payload);
        ClientResponse cr = webResource.accept(RESPONSE_TYPE).post(ClientResponse.class, formData);
        //checkStatusForSuccess(cr);
        RestResponse rr = RestResponse.getRestResponse(cr);
        return rr;
    }

    // TODO: This will be implemented when the REST API is updated to use PUTs for updates as is planned
    public static String put(String address) {
        throw new UnsupportedOperationException();
    }

    public static RestResponse delete(String address, Map<String, Object> payload) {
        WebResource webResource = JERSEY_CLIENT.resource(address);
        ClientResponse cr = webResource.queryParams(buildMultivalueMap(payload)).accept(RESPONSE_TYPE).delete(ClientResponse.class);
        checkStatusForSuccess(cr);
        return RestResponse.getRestResponse(cr);
    }

    public static RestResponse options(String address, String responseType) {
        WebResource webResource = JERSEY_CLIENT.resource(address);
        ClientResponse cr = webResource.accept(responseType).options(ClientResponse.class);
        checkStatusForSuccess(cr);
        return RestResponse.getRestResponse(cr);
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
