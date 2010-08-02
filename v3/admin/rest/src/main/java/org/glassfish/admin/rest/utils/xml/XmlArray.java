/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.rest.utils.xml;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author jasonlee
 */
public class XmlArray extends XmlObject {
    private List<XmlObject> elements = new ArrayList<XmlObject>();

    public XmlArray(String name) {
        super(name);
    }

    public XmlArray(String name, List<XmlObject> elements) {
        super(name);
        this.elements = elements;
    }

    public XmlArray put(XmlObject obj) {
        elements.add(obj);
        return this;
    }

    @Override
    Node createNode(Document document) {
        Node listNode = document.createElement("list");

        for (XmlObject element : elements) {
            listNode.appendChild(element.createNode(document));
        }

        return listNode;
    }
}