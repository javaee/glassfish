package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import com.sun.enterprise.deployment.Addressing;
import com.sun.enterprise.deployment.RespectBinding;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * This node does xml marshalling to/from web service respect-binding elements
 *
 * @author Bhakti Mehta
 */
public class RespectBindingNode extends DisplayableComponentNode {

    private final static XMLElement tag =
        new XMLElement(WebServicesTagNames.RESPECT_BINDING);


    public RespectBindingNode() {
        super();
    }

    /**
     * @return the XML tag associated with this XMLNode
     */
    protected XMLElement getXMLRootTag() {
        return tag;
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(WebServicesTagNames.RESPECT_BINDING_ENABLED, "setEnabled");

        return table;
    }

    /**
     * receives notification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        String qname = element.getQName();
        RespectBinding rb = (RespectBinding) getDescriptor();
        if (WebServicesTagNames.RESPECT_BINDING_ENABLED.equals(qname)) {
            rb.setEnabled(new Boolean(value));
        } else super.setElementValue(element, value);
    }

    /**
     * write the method descriptor class to a query-method DOM tree and
     * return it
     *
     * @param parent node in the DOM tree
     * @param node name for the root element of this xml fragment
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName,
                                RespectBinding rb) {
        Node wshNode = super.writeDescriptor(parent, nodeName, rb);

        writeDisplayableComponentInfo(wshNode, rb);
        appendTextChild(wshNode,
                WebServicesTagNames.RESPECT_BINDING_ENABLED,
                new Boolean(rb.isEnabled()).toString());

        return wshNode;
    }

  
}
