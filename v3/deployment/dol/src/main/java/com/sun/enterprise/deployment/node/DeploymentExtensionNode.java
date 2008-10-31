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

import com.sun.enterprise.deployment.DeploymentExtensionDescriptor;
import com.sun.enterprise.deployment.ExtensionElementDescriptor;
import com.sun.enterprise.deployment.xml.TagNames;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Iterator;
import java.util.Map;

/**
 * This node is responsible for handling the deployment-extension xml fragment
 *
 * @author Jerome Dochez
 */
public class DeploymentExtensionNode extends DeploymentDescriptorNode {
        
   /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value. 
     *  
     * @return the map with the element name as a key, the setter method as a value
     */    
    protected Map getDispatchTable() {    
        Map table = super.getDispatchTable();
        table.put(TagNames.NAMESPACE, "setNameSpace");
        return table;
    }   
    
    /**
     * receives notification of the value for a particular tag
     * 
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        if (TagNames.MUST_UNDERSTAND.equals(element.getQName())) {
            if ("true".equals(value)) {
                throw new RuntimeException("a deployment-extension tagged with mustUnderstand attribute is not understood");
            }
        } else 
            super.setElementValue(element, value);        
    }
    
    /**
     *  @return true if the element tag can be handled by any registered sub nodes of the
     * current XMLNode
     */
    public boolean handlesElement(XMLElement element) {
        if (TagNames.EXTENSION_ELEMENT.equals(element.getQName())) {
            return false;
        } 
        return true;
    }
    
    /**
     * @return the handler registered for the subtag element of the curent  XMLNode
     */
    public  XMLNode getHandlerFor(XMLElement element) {
        ExtensionElementNode subNode = new ExtensionElementNode();        
        DeploymentExtensionDescriptor o = (DeploymentExtensionDescriptor) getDescriptor();
        o.addElement((ExtensionElementDescriptor) subNode.getDescriptor());
        subNode.setXMLRootTag(new XMLElement(TagNames.EXTENSION_ELEMENT));
        subNode.setParentNode(this);
        return subNode;
    }    
    
    /**
     * Adds  a new DOL descriptor instance to the descriptor instance associated with 
     * this XMLNode
     *
     * @param descriptor the new descriptor
     */
    public void addDescriptor(Object descriptor) {
        // done is getHandlerFor
    }
    
    /**
     * write the deployment extensions nodes associated with this node
     * 
     * @param parent node for the DOM tree
     * @param iterator of deployment extension descriptor
     */
    protected void writeDescriptor(Node parentNode, Iterator itr) {  
        
        ExtensionElementNode subNode = new ExtensionElementNode();
        do { 
            DeploymentExtensionDescriptor descriptor = (DeploymentExtensionDescriptor) itr.next();
            Element extensionNode = appendChild(parentNode, TagNames.DEPLOYMENT_EXTENSION);    
            setAttribute(extensionNode, TagNames.NAMESPACE, descriptor.getNameSpace());
            if (descriptor.getMustUnderstand()) {
                setAttribute(extensionNode, TagNames.MUST_UNDERSTAND, "true");
            }
            for (Iterator elements = descriptor.elements();elements.hasNext();) {      
                ExtensionElementDescriptor element = (ExtensionElementDescriptor) elements.next();
                subNode.writeDescriptor(extensionNode, TagNames.EXTENSION_ELEMENT, element );
            }
        } while(itr.hasNext());            
    }
}
