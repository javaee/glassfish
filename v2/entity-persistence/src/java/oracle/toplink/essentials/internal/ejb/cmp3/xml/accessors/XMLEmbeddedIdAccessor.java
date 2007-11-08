/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.internal.ejb.cmp3.xml.accessors;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.ClassAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.EmbeddedIdAccessor;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataClass;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataDescriptor;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLConstants;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLHelper;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.columns.XMLColumn;

import oracle.toplink.essentials.mappings.AggregateObjectMapping;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An XML extended embedded id relationship accessor.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class XMLEmbeddedIdAccessor extends EmbeddedIdAccessor {    
    protected Node m_node;
    protected XMLHelper m_helper;
    
    /**
     * INTERNAL:
     */
    public XMLEmbeddedIdAccessor(MetadataAccessibleObject accessibleObject, Node node, XMLClassAccessor classAccessor) {
        super(accessibleObject, classAccessor);
        m_node = node;
        m_helper = classAccessor.getHelper();
    }
    
    /**
     * INTERNAL: (Overrride from EmbeddedAccesor)
     * 
     * Currently if the embedded is specified in XML with no attribute
     * overrides, we do NOT search the class for attribute overrides. It is
     * assumed that they are to be defaulted.
     */
    protected void processAttributeOverrides(AggregateObjectMapping mapping) {
        NodeList nodes = m_helper.getNodes(m_node, XMLConstants.ATTRIBUTE_OVERRIDE);
        
    	if (nodes != null) {
    		for (int i = 0; i < nodes.getLength(); i++) {
                processAttributeOverride(mapping, new XMLColumn(nodes.item(i), m_helper, getAnnotatedElement()));
    		}
    	}
    }
    
    /**
     * INTERNAL: (Overrride from EmbeddedAccesor)
     * 
     * Fast track processing a ClassAccessor for the given descriptor. 
     * Inheritance root classes and embeddables may be fast tracked.
     * 
     * NOTE: The class passed in may not have any XML representation. If so,
     * pass up to the parent.
     */
    protected ClassAccessor processAccessor(MetadataDescriptor descriptor) {
        Node node = m_helper.locateEntityNode(descriptor.getJavaClass());
        
        if (node != null) {
            XMLClassAccessor accessor = new XMLClassAccessor(new MetadataClass(descriptor.getJavaClass()), node, m_helper, m_processor, descriptor);
            descriptor.setClassAccessor(accessor);
            accessor.process();
            accessor.setIsProcessed();
            return accessor;
        } else {
            return super.processAccessor(descriptor);
        }
    }
}
