/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.deployment.node.runtime;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.XMLNode;
import com.sun.enterprise.deployment.runtime.JavaWebStartAccessDescriptor;
import com.sun.enterprise.deployment.runtime.JnlpDocDescriptor;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Runtime descriptor node for
 *   <sun-application-client>
 *     <java-web-start-support>
 *       <jnlp-doc>
 * 
 * @author tjquinn
 */
public class JnlpDocNode extends DeploymentDescriptorNode<JnlpDocDescriptor> {

    static void writeJnlpDocInfo(Node parent, JnlpDocDescriptor jnlpDoc) {
        if (jnlpDoc != null) {
            Element jnlpDocElement = appendChild(parent, RuntimeTagNames.JNLP_DOC);
            setAttribute(jnlpDocElement, JnlpDocDescriptor.HREF, jnlpDoc.getHref());
        }
    }

    JnlpDocDescriptor descriptor = null;

    public JnlpDocNode() {

    }
    
    public JnlpDocNode(XMLElement element) {
        setXMLRootTag(element);
    }
    
    @Override
    public JnlpDocDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new JnlpDocDescriptor();
            XMLNode parentNd = getParentNode();
            if (parentNd != null && (parentNd instanceof JavaWebStartAccessNode)) {
                Object parentDescriptor = parentNd.getDescriptor();
                if (parentDescriptor != null && (parentDescriptor instanceof JavaWebStartAccessDescriptor) ) {
                    JavaWebStartAccessDescriptor jwsDescriptor = (JavaWebStartAccessDescriptor) parentDescriptor;
                    jwsDescriptor.setJnlpDoc(descriptor);
                }
            }
        }
        return descriptor;
    }

    /**
     * parsed an attribute of an element
     *
     * @param the element name
     * @param the attribute name
     * @param the attribute value
     * @return true if the attribute was processed
     */
    @Override
    protected boolean setAttributeValue(XMLElement elementName,
        XMLElement attributeName, String value) {
        if (attributeName.getQName().equals(JnlpDocDescriptor.HREF)) {
            descriptor.setHref(value);
            return true;
        }
        return false;
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName,
        JnlpDocDescriptor jnlpDocDesc) {
	Element jnlpDocElement = (Element) super.writeDescriptor(parent, nodeName, jnlpDocDesc);
        final String attrValue = jnlpDocDesc.getAttributeValue(JnlpDocDescriptor.HREF);
        if (attrValue != null) {
            setAttribute(jnlpDocElement, JnlpDocDescriptor.HREF, attrValue);
        }
        return jnlpDocElement;
    }
}
