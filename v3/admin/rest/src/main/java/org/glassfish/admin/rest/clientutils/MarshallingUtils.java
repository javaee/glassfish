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
package org.glassfish.admin.rest.clientutils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author jasonlee
 */
public class MarshallingUtils {

    public static String getJsonForProperties(final Map<String, String> properties) {
        return getJsonForProperties(new ArrayList<Map<String, String>>() {{ add(properties); }} );
    }

    public static String getJsonForProperties(List<Map<String, String>> properties) {
        JSONArray ja = new JSONArray();

        for (Map<String, String> property : properties) {
            ja.put(getJsonObjectForMap(property));
        }

        return ja.toString();
    }

    public static List<Map<String, String>> getPropertiesFromJson(String json) {
        List<Map<String, String>> properties;
        json = json.trim();
        if (json.startsWith("{")) {
            properties = new ArrayList<Map<String, String>>();
            properties.add(processJsonMap(json));
        } else if (json.startsWith("[")) {
            properties = processJsonList(json);
        } else {
            throw new RuntimeException("The JSON string must start with { or ["); // i18n
        }

        return properties;
    }

    public static String getXmlForProperties(final Map<String, String> properties) {
        return getXmlForProperties(new ArrayList<Map<String, String>>() {{ add(properties); }} );
    }

    public static String getXmlForProperties(List<Map<String, String>> properties) {
        try {
            String xml = null;
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            StringWriter sw = new StringWriter();
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(sw);
            writer.writeStartDocument("UTF-8","1.0");
            writer.writeStartElement("list");
            for (Map<String, String> property : properties) {
                writer.writeStartElement("map");
                for (Map.Entry<String, String> entry : property.entrySet()) {
                writer.writeStartElement("entry");
                    writer.writeAttribute("key", entry.getKey());
                    writer.writeAttribute("value", entry.getValue());
                writer.writeEndElement();
                }
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            writer.close();
            return sw.toString();
        } catch (Exception ex) {
            Logger.getLogger(MarshallingUtils.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    public static List<Map<String, String>> getPropertiesFromXml(String xml) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        InputStream input = null;
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);
            input = new ByteArrayInputStream(xml.trim().getBytes("UTF-8"));
            XMLStreamReader parser = inputFactory.createXMLStreamReader(input);
            while (parser.hasNext()) {
                int event = parser.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT: {
                        if ("list".equals(parser.getLocalName())) {
                            list = processXmlList(parser);
                        }
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(MarshallingUtils.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } finally {
            try {
                input.close();
            } catch (IOException ex) {
                Logger.getLogger(MarshallingUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return list;
    }

    public static JSONObject getJsonObjectForMap(Map<String, String> property) throws RuntimeException {
        JSONObject jo = new JSONObject();
        for (Map.Entry<String, String> elem : property.entrySet()) {
            try {
                jo.put(elem.getKey(), elem.getValue());
            } catch (JSONException ex) {
                Logger.getLogger(MarshallingUtils.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
        }

        return jo;
    }

    protected static Map<String, String> processJsonMap(String json) {
        Map<String, String> map = new HashMap<String, String>();

        return map;
    }

    protected static List<Map<String, String>> processJsonList(String json) {
        List<Map<String, String>> properties = new ArrayList<Map<String, String>>();
        try {
            JSONArray ja = new JSONArray(json);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                Map<String, String> property = new HashMap<String, String>();
                Iterator iter = jo.keys();
                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    property.put(key, (String) jo.get(key));
                }
                properties.add(property);
            }
        } catch (Exception ex) {
            Logger.getLogger(MarshallingUtils.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }

        return properties;
    }

    protected static List processXmlList(XMLStreamReader parser) throws XMLStreamException {
        List list = new ArrayList();
        boolean endOfList = false;
        while (!endOfList) {
            int event = parser.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT: {
//                    if ("entry".equals(parser.getLocalName())) {
//                        list.add(processXmlListEntry(parser));
//                    }
                    if ("map".equals(parser.getLocalName())) {
                        list.add(processXmlMap(parser));
                    }
                    break;
                }
                case XMLStreamConstants.END_ELEMENT: {
                    if ("list".equals(parser.getLocalName())) {
                        endOfList = true;
                    }
                    break;
                }
                default: {
                    throw new RuntimeException ("XML parsing error"); // i18n
                }
            }
        }
        return list;
    }

    protected static Object processXmlListEntry(XMLStreamReader parser) throws XMLStreamException {
        Object entry = null;
        boolean endOfEntry = false;

        while (!endOfEntry) {
            int event = parser.next();
            switch (event) {
                case XMLStreamConstants.ATTRIBUTE: {
                    if ("value".equals(parser.getLocalName())) {
                        entry = parser.getText();
                    }
                    break;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    if ("map".equals(parser.getLocalName())) {
                        entry = processXmlMap(parser);
                    }
                    break;
                }
                case XMLStreamConstants.END_ELEMENT: {
                    if ("entry".equals(parser.getLocalName())) {
                        endOfEntry = true;
                    }
                    break;
                }
            }
        }

        return entry;
    }

    protected static Map processXmlMap(XMLStreamReader parser) throws XMLStreamException {
        boolean endOfMap = false;
        Map<String, String> entry = new HashMap();
        while (!endOfMap) {
            int event = parser.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT: {
                    if ("entry".equals(parser.getLocalName())) {
                        String name = parser.getAttributeValue(null, "key");
                        String value = parser.getAttributeValue(null, "value");
                        entry.put(name, value);
                    } else if ("map".equals(parser.getLocalName())) {
                        throw new RuntimeException("Unexpected XML element found:  " + parser.getLocalName());
                    } else if ("list".equals(parser.getLocalName())) {
                        throw new RuntimeException("Unexpected XML element found:  " + parser.getLocalName());
                    }
                    break;
                }
                case XMLStreamConstants.END_ELEMENT: {
                    if ("map".equals(parser.getLocalName())) {
                        endOfMap = true;
                    }
                    break;
                }
                default: {
                    throw new RuntimeException ("XML parsing error"); // i18n
                }
            }
        }
        return entry;
    }

    public static void main(String... args) {
        String json = "[{\"name\":\"foo1\",\"value\":\"bar1\",\"description\":\"baz1\"},{\"name\":\"foo2\",\"value\":\"bar2\",\"description\":\"baz2\"},{\"name\":\"foo3\",\"value\":\"bar3\",\"description\":\"baz3\"}]";

        String xml = "<list>" +
                "<map><entry key=\"name\" value=\"foo1\"/><entry key=\"value\" value=\"bar1\"/><entry key=\"description\" value=\"baz1\"/></map>" +
                "<map><entry key=\"name\" value=\"foo2\"/><entry key=\"value\" value=\"bar2\"/><entry key=\"description\" value=\"baz2\"/></map>" +
                "<map><entry key=\"name\" value=\"foo3\"/><entry key=\"value\" value=\"bar3\"/><entry key=\"description\" value=\"baz3\"/></map>" +
                "</list>";

        List<Map<String, String>> properties = getPropertiesFromJson(json);
        System.out.println("************");
        System.out.println("getPropertiesFromJson = " + properties);

        String newJson = getJsonForProperties(properties);
        System.out.println("************");
        System.out.println("newJson = " + newJson);

        properties = getPropertiesFromXml(xml);
        System.out.println("************");
        System.out.println("getPropertiesFromXml = " +properties);

        String newXml = getXmlForProperties(properties);
        System.out.println("************");
        System.out.println("xml = " + xml);
        System.out.println("newXml = " + newXml);
        properties = getPropertiesFromXml(newXml);
        System.out.println(properties);
    }
}
