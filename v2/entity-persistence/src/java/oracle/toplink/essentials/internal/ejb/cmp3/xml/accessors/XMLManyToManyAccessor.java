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

import java.util.List;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.ManyToManyAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.tables.MetadataJoinTable;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.tables.XMLJoinTable;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLConstants;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLHelper;

import org.w3c.dom.Node;

/**
 * A an extended many to many relationship accessor.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class XMLManyToManyAccessor extends ManyToManyAccessor {    
    protected Node m_node;
    protected XMLHelper m_helper;
    
    /**
     * INTERNAL:
     */
    public XMLManyToManyAccessor(MetadataAccessibleObject accessibleObject, Node node, XMLClassAccessor classAccessor) {
        super(accessibleObject, classAccessor);
        m_node = node;
        m_helper = classAccessor.getHelper();
    }
    
    /**
     * INTERNAL: (Override from ManyToManyAccessor)
     */
    public List<String> getCascadeTypes() {
        return m_helper.getCascadeTypes(m_node);
    }
    
    /**
     * INTERNAL: (Override from ManyToManyAccessor)
     */
    public String getFetchType() {
        return m_helper.getFetchTypeDefaultLAZY(m_node);
    }
    
    /**
     * INTERNAL: (Override from CollectionAccessor)
     */
    public MetadataJoinTable getJoinTable() {
        Node node = m_helper.getNode(m_node, XMLConstants.JOIN_TABLE);
        
        if (node == null) {
            return super.getJoinTable();
        } else {
            return new XMLJoinTable(node, m_helper, m_logger);
        }
    }
    
    /**
     * INTERNAL: (Override from CollectionAccessor)
     * 
     * Checks for a map-key node and returns its value if there is one. 
     * Otherwise, ask the parent to look for an annotation.
     */
    public String getMapKey() { 
        Node mapKeyNode = m_helper.getNode(m_node, XMLConstants.MAPKEY);
        String mapKeyValue = m_helper.getNodeValue(m_node, XMLConstants.MAPKEY);
        
        if (mapKeyNode == null) {
            return super.getMapKey();
        } else {
            return mapKeyValue;
        }
    }
    
    /**
     * INTERNAL: (Override from ManyToManyAccessor)
     */
    public String getMappedBy() {
        return m_helper.getMappedBy(m_node);
    }
    
    /**
     * INTERNAL: (Override from CollectionAccessor)
     * 
     * If the order value is not specified, "" is returned.
     */
    public String getOrderBy() {
        if (hasOrderBy()) {
            return m_helper.getNodeTextValue(m_node, XMLConstants.ORDER_BY);
        } else {
            return super.getOrderBy();
        }
    } 
    
    /**
     * INTERNAL: (Override from ManyToManyAccessor)
     */
    public Class getTargetEntity() {
        return m_helper.getTargetEntity(m_node);
    }
    
    /**
     * INTERNAL: (Override from CollectionAccessor)
	 * Checks for an order-by node. If one isn't found, as the parent to look
     * for an annotation.
     */
	public boolean hasOrderBy() {
		Node orderByNode = m_helper.getNode(m_node, XMLConstants.ORDER_BY);
        
        if (orderByNode == null) {
            return super.hasOrderBy();
        } else {
            return true;
        }
	}
}
