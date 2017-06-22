/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.enterprise.deployment.node.runtime.common.wls;

import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.types.ResourceEnvReferenceContainer;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.TagNames;
import org.glassfish.deployment.common.Descriptor;
import org.w3c.dom.Node;

import java.util.Map;
import java.util.Set;

/**
 * This node handles resource-env-description in weblogic.xml
 *
 * @author  Shing Wai Chan
 */
public class ResourceEnvDescriptionNode extends RuntimeDescriptorNode<ResourceEnvReferenceDescriptor> {
    private ResourceEnvReferenceDescriptor descriptor = null;
    
    public ResourceEnvDescriptionNode() {
    }

    @Override
    public ResourceEnvReferenceDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new ResourceEnvReferenceDescriptor();
        }
        return descriptor;
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value. 
     *  
     * @return the map with the element name as a key, the setter method as a value
     */
    @Override
    protected Map getDispatchTable() {    
        Map table = super.getDispatchTable();
        table.put(RuntimeTagNames.JNDI_NAME, "setJndiName");
        return table;
    }

    @Override
    public void setElementValue(XMLElement element, String value) {
        if (TagNames.RESOURCE_ENV_REFERENCE_NAME.equals(element.getQName())) {
            Object parentDesc = getParentNode().getDescriptor();
            if (parentDesc instanceof ResourceEnvReferenceContainer) {
                try {
                    descriptor = ((ResourceEnvReferenceContainer)parentDesc).getResourceEnvReferenceByName(value);
                } catch (IllegalArgumentException iae) {
                    DOLUtils.getDefaultLogger().warning(iae.getMessage());
                }
            }
        } else {
            super.setElementValue(element, value);
        }
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName node name
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, ResourceEnvReferenceDescriptor descriptor) {
        Node refNode = appendChild(parent, nodeName);
        appendTextChild(refNode, TagNames.RESOURCE_ENV_REFERENCE_NAME, descriptor.getName());
        appendTextChild(refNode, RuntimeTagNames.JNDI_NAME, descriptor.getJndiName());
        return refNode;
    }    

    /**
     * write all occurrences of the descriptor corresponding to the current
     * node from the parent descriptor to an JAXP DOM node and return it
     *
     * This API will be invoked by the parent node when the parent node
     * writes out a mix of statically and dynamically registered sub nodes.
     *
     * This method should be overriden by the sub classes if it
     * needs to be called by the parent node.
     *
     * @param parent node in the DOM tree
     * @param nodeName the name of the node
     * @param parentDesc parent descriptor of the descriptor to be written
     * @return the JAXP DOM node
     */
    @Override
    public Node writeDescriptors(Node parent, String nodeName, Descriptor parentDesc) {
        if (parentDesc instanceof ResourceEnvReferenceContainer) {
            ResourceEnvReferenceContainer resourceEnvReferenceContainer =
                (ResourceEnvReferenceContainer)parentDesc;
            // resource-env-description*
            Set<ResourceEnvReferenceDescriptor> resourceEnvReferenceDescriptors =
                resourceEnvReferenceContainer.getResourceEnvReferenceDescriptors();
            for (ResourceEnvReferenceDescriptor resourceEnvReferenceDescriptor : 
                    resourceEnvReferenceDescriptors) {
                writeDescriptor(parent, nodeName, resourceEnvReferenceDescriptor);
            }
        }
        return parent;
    }
}
