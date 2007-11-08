/*
 * $Id: DescriptionTextRule.java,v 1.1 2005/09/20 21:11:31 edburns Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * https://javaserverfaces.dev.java.net/CDDL.html or
 * legal/CDDLv1.0.txt. 
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.    
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * [Name of File] [ver.__] [Date]
 * 
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.faces.config.rules;


import org.apache.commons.digester.NodeCreateRule;
import org.apache.commons.digester.Rule;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import com.sun.faces.config.beans.DescriptionBean;


/**
 * <p>Digester rule for the <code>&lt;description&gt;</code> element,
 * used to absorb the body content of the matching element even if it
 * contains mixed markup and body content, and then save it as the
 * value of the <code>description</code> property of the corresponding
 * <code>DescriptionBean</code> instance..</p>
 */

public class DescriptionTextRule extends NodeCreateRule {


    // ------------------------------------------------------------- Constructor


    /**
     * <p>Construct a variant of <code>NodeCreateRule</code> that will
     * create a <code>DocumentFragment</code> object.
     */
    public DescriptionTextRule() throws Exception {

        super(Node.DOCUMENT_FRAGMENT_NODE);

    }


    // ------------------------------------------------------------ Rule Methods


    /**
     * <p>Ensure that the object at the top of the stack is a
     * <code>DescriptionBean</code>, then perform the standard
     * superclass processing.</p>
     *
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just 
     *   the element name otherwise
     * @param attributes The attribute list of this element
     *
     * @exception IllegalStateException if the parent stack element is not
     *  of type FeatureBean
     */
    public void begin(String namespace, String name,
                      Attributes attributes) throws Exception {

        assert (digester.peek() instanceof DescriptionBean);        

        // Perform our standard superclass processing
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("[DescriptionTextRule]{" +
                                       digester.getMatch() +
                                       "} Begin");
        }
        super.begin(namespace, name, attributes);

    }


    /**
     * <p>No body processing is required.</p>
     *
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just 
     *   the element name otherwise
     * @param text The text of the body of this element
     */
    public void body(String namespace, String name,
                     String text) throws Exception {
    }


    /**
     * <p>Serialize the resulting DOM into text, and use it to set the
     * <code>description</code> property of the parent
     * <code>DescriptionBean</code>.</p>
     *
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just 
     *   the element name otherwise
     *
     * @exception IllegalStateException if the popped object is not
     *  of the correct type
     */
    public void end(String namespace, String name) throws Exception {

        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("[DescriptionTextRule]{" +
                                       digester.getMatch() +
                                       "} End (" + digester.peek() + ")");
        }

        // Pop the DOM object off the stack (works around a bug in
        // NodeCreateRule that won't be fixed until version 1.6 of
        // commons-digester is released)
        Node root = (Node) digester.pop();        

        // Serialize the child nodes into a StringBuffer
        DescriptionBean db = (DescriptionBean) digester.peek();
        StringBuffer sb = new StringBuffer();
        NodeList kids = root.getChildNodes();
        int n = kids.getLength();
        for (int i = 0; i < n; i++) {
            serialize(sb, kids.item(i));
        }

        // Use the StringBuffer to set the value of the description property
        db.setDescription(sb.toString());

    }


    /**
     * <p>No finish processing is required.</p>
     *
     */
    public void finish() throws Exception {
    }


    // ---------------------------------------------------------- Public Methods


    public String toString() {

        StringBuffer sb = new StringBuffer("DescriptionTextRule[]");
        return (sb.toString());

    }


    // --------------------------------------------------------- Package Methods


    // --------------------------------------------------------- Private Methods


    /**
     * <p>Append the serialized version of the specified node to the
     * string buffer being accumulated.</p>
     *
     * @param sb StringBuffer to which serialized text is appended
     * @param node Node to be serialized
     *
     * @exception Exception if any processing exception occurs
     */
    private void serialize(StringBuffer sb, Node node) throws Exception {

        // Processing depends on the node type
        switch (node.getNodeType()) {

        case Node.ELEMENT_NODE:

            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("  Processing element node '" +
                                           node.getNodeName() + "'");
            }

            // Open the element and echo the attributes
            sb.append("<");
            sb.append(node.getNodeName());
            NamedNodeMap attrs = node.getAttributes();
            int n = attrs.getLength();
            for (int i = 0; i < n; i++) {
                Node attr = attrs.item(i);
                sb.append(" ");
                sb.append(attr.getNodeName());
                sb.append("=\"");
                sb.append(attr.getNodeValue());
                sb.append("\"");
            }

            // Does this element have any children?
            NodeList kids = node.getChildNodes();
            int m = kids.getLength();
            if (m > 0) {

                // Yes -- serialize child elements and close parent element
                sb.append(">");
                for (int j = 0; j < m; j++) {
                    serialize(sb, kids.item(j));
                }
                sb.append("</");
                sb.append(node.getNodeName());
                sb.append(">");

            } else {

                // No -- shorthand close of the parent element
                sb.append(" />");

            }
            break;

        case Node.TEXT_NODE:

            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("  Processing text node '" +
                                           node.getNodeValue() + "'");
            }

            // Append the text to our accumulating buffer
            sb.append(node.getNodeValue());
            break;

        default:
            throw new IllegalArgumentException("Cannot process node '" +
                                               node.getNodeName() +
                                               "' of type '" +
                                               node.getNodeType());

        }

    }


}
