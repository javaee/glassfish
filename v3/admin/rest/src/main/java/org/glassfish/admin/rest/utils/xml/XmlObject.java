/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.rest.utils.xml;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Result;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author jasonlee
 */
public class XmlObject {
    private String name;
    private Object value;
    private Map<String, Object> children = new HashMap<String, Object>();

    public XmlObject(String name) {
        this(name, null);
    }

    public XmlObject(String name, Object value) {
        this.name = name.toLowerCase();
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    protected Document getDocument() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public XmlObject put(String key, Object child) {
        if (child instanceof String) {
            children.put(key, child);
        } else if (child instanceof Number) {
            children.put(key, new XmlObject("Number", (Number)child));
        } else if (child instanceof XmlObject) {
            children.put(key, (XmlObject)child);
        }
        return this;
    }

    public Object remove(String key) {
        children.remove(key);
        return this;
    }

    public int childCount() {
        return children.size();
    }

    Node createNode(Document document) {
        Node node = document.createElement(getName());
        if (value != null) {
            node.setTextContent(value.toString());
        }
        Element element = (Element)node;
        for (Map.Entry<String, Object> child : children.entrySet()) {
            String key = child.getKey();
            Object value = child.getValue();
            if (value instanceof String) {
                element.setAttribute(key, value.toString());
            } else {
                XmlObject obj = (XmlObject)value;
                Node entryNode = document.createElement("entry");
                ((Element)entryNode).setAttribute("name", obj.getName());
                entryNode.appendChild(obj.createNode(document));
                node.appendChild(entryNode);
            }
//            element.setAttribute(attribute.getKey(), attribute.getValue());
        }

        return node;
    }

    @Override
    public String toString() {
        Document document = getDocument();
        document.appendChild(createNode(document));
        try {
            Source source = new DOMSource(document);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);

            return stringWriter.getBuffer().toString();
        } catch (Exception ex) {
            Logger.getLogger(XmlEntity.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

//    public static class XmlNumber extends XmlObject {
//        private Number value;
//
//        public XmlNumber(Number value) {
//            this("", value);
//        }
//
//        public XmlNumber(String name, Number value) {
//            super(name);
//            this.value = value;
//        }
//
//        Node createNode(Document document) {
//            Node numberNode = document.createElement("number");
//            numberNode.setTextContent(value.toString());
//            return numberNode;
//        }
//    }
}

