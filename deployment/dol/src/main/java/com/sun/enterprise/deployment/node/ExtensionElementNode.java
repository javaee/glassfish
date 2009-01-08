/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.Descriptor;
import com.sun.enterprise.deployment.ExtensionElementDescriptor;
import com.sun.enterprise.deployment.xml.TagNames;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

import java.util.Iterator;
import java.util.Map;

/**
 * This node is responsible for loading and saving any type of 
 * xml node and store them in a DeploymentExtensionElement 
 *
 * @author Jerome Dochez
 */
public class ExtensionElementNode extends DeploymentDescriptorNode {
    
    ExtensionElementDescriptor descriptor;
    
    /** Creates a new instance of DynamicDescriptorNode */
    public ExtensionElementNode() {
        descriptor = new ExtensionElementDescriptor();
    }

   /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value. 
     *  
     * @return the map with the element name as a key, the setter method as a value
     */    
    protected Map getDispatchTable() {   
        return null;
    }
    
    /**
     * Adds a new DOL descriptor instance to the descriptor associated with this 
     * XMLNode
     * 
     * @param XMLNode the sub-node adding the descriptor;
     * @param descriptor the new descriptor
     */
    protected void addNodeDescriptor(DeploymentDescriptorNode node) {
        // nodes are added upon creation
        ExtensionElementDescriptor dad = (ExtensionElementDescriptor) node.getDescriptor();
        Iterator itr = dad.getElementNames();
        // jump over first element;
        if (itr.hasNext()) itr.next();
        
        if (itr.hasNext() && !dad.hasAttributes()) {
            descriptor.addElement(node.getXMLRootTag().getCompleteName(), dad);
        } else {
            descriptor.addElement(node.getXMLRootTag().getCompleteName(), dad.getElement(node.getXMLRootTag().getCompleteName()));
        }
    }
    
   /**
    * @return the descriptor instance to associate with this XMLNode
    */    
    public Object getDescriptor() {    
        return descriptor;
    }
    
    /**
     *  @return true if the element tag can be handled by any registered sub nodes of the
     * current XMLNode
     */
    public boolean handlesElement(XMLElement element) {
        // we are never handling xml fragment only leaf nodes, we create subnodes for that.
        return false;
    }
    
    /**
     * @return the handler registered for the subtag element of the curent  XMLNode
     */
    public  XMLNode getHandlerFor(XMLElement element) {
        ExtensionElementNode subNode = new ExtensionElementNode();        
        subNode.setParentNode(this);
        subNode.setXMLRootTag(new XMLElement(element.getCompleteName()));
        return subNode;
    }
    
    /**
     * SAX Parser API implementation, we don't really care for now.
     */
    public void startElement(XMLElement element, Attributes attributes) {
        if (attributes.getLength()>0) {
            for (int i=0;i<attributes.getLength();i++) {        
                if (attributes.getLocalName(i).equals("type")) {
                    // type declaration... replace the standard xml declaration with ours...
                    descriptor.getAttributes().addExtraAttribute("xsi:type", attributes.getValue(i));
                } else {
                    descriptor.getAttributes().addExtraAttribute(attributes.getQName(i), attributes.getValue(i));                
                }
            }
        }
    }
    
    /**
     * receives notification of the value for a particular tag
     * 
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        descriptor.addElement(element.getCompleteName(), value);
    } 
    
    /** 
     * receives notification of the end of an XML element by the Parser
     * 
     * @param element the xml tag identification
     * @return true if this node is done processing the XML sub tree
     */
    public boolean endElement(XMLElement element) {
        boolean allDone = element.getCompleteName().equals(getXMLRootTag().getCompleteName()) || element.getQName().equals(TagNames.EXTENSION_ELEMENT);        
        if (allDone) {
            postParsing();    
            ((DeploymentDescriptorNode) getParentNode()).addNodeDescriptor(this);            
        }
        return allDone;        
    }
    
    /**
     * write the deployment extensions nodes associated with this node
     * 
     * @param parent node for the DOM tree
     * @param deployment extension element
     */
    protected void writeDescriptor(Node parentNode, String tagName, ExtensionElementDescriptor descriptor) {
        Element node = appendChildNS(parentNode, tagName, descriptor);
        if (descriptor.hasAttributes()) {
            Map attributes = descriptor.getAttributes().getExtraAttributes();
            for (Iterator itr = attributes.keySet().iterator();itr.hasNext();) {
                String key = (String) itr.next();
                String value = (String) attributes.get(key);
                String namespace = "";
                if (key.indexOf(':')!=-1) {
                    String prefix = key.substring(0, key.indexOf(':'));
                    namespace = getNamespaceFor(descriptor, parentNode, prefix);
                }                
                node.setAttributeNS(namespace, key, value);
            }
        }
        addNamespaceDeclaration(node, descriptor);
        for (Iterator itr = descriptor.getElementNames();itr.hasNext();) {
            String elementName = (String) itr.next();
            Object value = descriptor.getElement(elementName);
            if (value instanceof ExtensionElementDescriptor) {
                writeDescriptor(node, elementName, (ExtensionElementDescriptor) value);
            } else 
            if (value instanceof String) {
                appendTextChildNS(node, elementName, (String) value, descriptor);
            }
        }                
    }
    
    /**
     * <p>
     * Append a new element child to the current node 
     * </p>
     * @param parentNode is the parent node for the new child element
     * @param elementName is new element tag name
     * @return the newly created child node
     */
    public Element appendChildNS(Node parent, String elementName, Descriptor descriptor) {
        if (elementName.indexOf(':')!=-1) {
            String prefix = elementName.substring(0, elementName.indexOf(':'));
            elementName = elementName.substring(elementName.indexOf(':')+1);
            
            String namespace = getNamespaceFor(descriptor, parent, prefix);
            
            Element child = getOwnerDocument(parent).createElementNS(namespace, elementName);            
            child.setPrefix(prefix);
            parent.appendChild(child);
            return child;
        }
        return super.appendChild(parent, elementName);
    }

    /**
     * look in the mapping defined in this descriptor and in all parent nodes 
     * for the right namespace for the passed prefix
     */
    private String getNamespaceFor(Descriptor descriptor, Node parent, String prefix) {
        
        Map prefixMapping = descriptor.getPrefixMapping();
        String namespace = null;
        if (prefixMapping!=null) {
            namespace = (String) prefixMapping.get(prefix);
        }
        if (namespace==null) {
            Element currentNode = (Element) parent;
            namespace="";
            while (currentNode!=null && namespace.length()==0) {
                namespace = currentNode.getAttributeNS("http://www.w3.org/2000/xmlns/", prefix);
                if (namespace.length()==0) 
                    currentNode = (Element) currentNode.getParentNode();
            }
        }
        return namespace;
    }

    
    /**
     * <p>
     * Append a new text child
     * </p>
     * @param parent for the new child element
     * @param elementName is the new element tag name
     * @param text the text for the new element
     * @result the newly create child node
     */
    public Node appendTextChildNS(Node parent, String elementName, String text, Descriptor descriptor) {
        
        if (text == null || text.length()==0) 
            return null;
        
        Node child = appendChildNS(parent, elementName, descriptor);
        child.appendChild(getOwnerDocument(child).createTextNode(text));        
        return child;
    }       
    
}
