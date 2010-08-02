/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.rest.utils.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author jasonlee
 */
public class XmlMap extends XmlObject {
    private final Map<String, Object> map;

    public XmlMap(String name) {
        super(name);
        this.map = new HashMap<String, Object>();
    }
    
    public XmlMap(String name, Map<String, Object> map) {
        super(name);
        this.map = map;
    }

    public XmlMap(String name, Properties properties) {
        super(name);
        this.map = new HashMap<String, Object>();
        for (Map.Entry entry : properties.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue());
        }
    }

    @Override
    public XmlMap put (String key, Object value) {
        map.put(key, value);

        return this;
    }

    @Override
    Node createNode(Document document) {
        Node mapNode = document.createElement("map");

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            Node entryNode = document.createElement("entry");
            Element entryElement = (Element)entryNode;
            entryElement.setAttribute("key", key);

            if (value instanceof XmlObject) {
                entryNode.appendChild(((XmlObject)value).createNode(document));
                mapNode.appendChild(entryNode);
            } else {
                if (value != null) {
                    entryElement.setAttribute("value", value.toString());
                }
            }
            mapNode.appendChild(entryNode);

        }

        return mapNode;
    }

}
