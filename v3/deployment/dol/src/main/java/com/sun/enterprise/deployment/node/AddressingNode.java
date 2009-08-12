package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import com.sun.enterprise.deployment.xml.TagNames;
import com.sun.enterprise.deployment.NameValuePairDescriptor;
import com.sun.enterprise.deployment.WebServiceHandler;
import com.sun.enterprise.deployment.Addressing;
import com.sun.enterprise.deployment.util.DOLUtils;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.w3c.dom.Node;

/**
 * This node does xml marshalling to/from web service addressing elements
 *
 * @author Bhakti Mehta
 */
public class AddressingNode extends DisplayableComponentNode{

    private final static XMLElement tag =
        new XMLElement(WebServicesTagNames.ADDRESSING);


    public AddressingNode() {
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
        table.put(WebServicesTagNames.ADDRESSING_ENABLED, "setEnabled");
        table.put(WebServicesTagNames.ADDRESSING_REQUIRED, "setRequired");
        table.put(WebServicesTagNames.ADDRESSING_RESPONSES, "setResponses");

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
        Addressing addressing = (Addressing) getDescriptor();
        if (WebServicesTagNames.ADDRESSING_ENABLED.equals(qname)) {
            addressing.setEnabled(new Boolean(value));
        } else if (WebServicesTagNames.ADDRESSING_REQUIRED.equals(qname)) {
            addressing.setRequired(new Boolean(value));
        } else if (WebServicesTagNames.ADDRESSING_RESPONSES.equals(qname)) {
            addressing.setResponses(value);
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
                                Addressing addressing) {
        Node wshNode = super.writeDescriptor(parent, nodeName, addressing);

        writeDisplayableComponentInfo(wshNode, addressing);
        appendTextChild(wshNode,
                WebServicesTagNames.ADDRESSING_ENABLED,
                new Boolean(addressing.isEnabled()).toString());

        appendTextChild(wshNode,
                WebServicesTagNames.ADDRESSING_REQUIRED,
                new Boolean(addressing.isRequired()).toString());
        appendTextChild(wshNode,
                WebServicesTagNames.ADDRESSING_RESPONSES,
                new Boolean(addressing.getResponses()).toString());




        return wshNode;
    }

    
}
