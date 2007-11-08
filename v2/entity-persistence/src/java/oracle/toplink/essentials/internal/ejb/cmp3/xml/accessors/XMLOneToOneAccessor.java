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

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.OneToOneAccessor;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataJoinColumns;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataPrimaryKeyJoinColumns;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLHelper;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.columns.XMLJoinColumns;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.columns.XMLPrimaryKeyJoinColumns;

import org.w3c.dom.Node;

/**
 * An extended one to one relationship accessor.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class XMLOneToOneAccessor extends OneToOneAccessor {    
    private Node m_node;
    private XMLHelper m_helper;
    
    /**
     * INTERNAL:
     */
    public XMLOneToOneAccessor(MetadataAccessibleObject accessibleObject, Node node, XMLClassAccessor classAccessor) {
        super(accessibleObject, classAccessor);
        
        m_node = node;
        m_helper = classAccessor.getHelper();
    }
    
    /**
     * INTERNAL: (Override from OneToOneAccessor)
     */
    public List<String> getCascadeTypes() {
        return m_helper.getCascadeTypes(m_node);
    }
    
    /**
     * INTERNAL: (Override from OneToOneAccessor)
     */
    public String getFetchType() {
        return m_helper.getFetchTypeDefaultEAGER(m_node);
    }
    
    /**
     * INTERNAL: (Override from RelationshipAccessor)
     */    
    protected MetadataJoinColumns getJoinColumns() {
        if (m_helper.nodeHasJoinColumns(m_node)) {
            return new XMLJoinColumns(m_node, m_helper);
        } else {
            return super.getJoinColumns();
        }
    }
    
    /**
     * INTERNAL: (Override from OneToOneAccessor)
     */
    public String getMappedBy() {
        return m_helper.getMappedBy(m_node);
    }
    
    /**
     * INTERNAL: (Override from MetadataAccessor)
     */    
    protected MetadataPrimaryKeyJoinColumns getPrimaryKeyJoinColumns(String sourceTableName, String targetTableName) {
        if (m_helper.nodeHasPrimaryKeyJoinColumns(m_node)) {
            return new XMLPrimaryKeyJoinColumns(m_node, m_helper, sourceTableName, targetTableName);
        } else {
            return super.getPrimaryKeyJoinColumns(sourceTableName, targetTableName);
        }
    }
    
    /**
     * INTERNAL: (Override from OneToOneAccessor)
     */
    public Class getTargetEntity() {
        return m_helper.getTargetEntity(m_node);
    }
    
    /**
     * INTERNAL: (Override from RelationshipAccessor)
     * 
     * Return true is this one-to-one has primary key join columns.
     */    
    public boolean hasPrimaryKeyJoinColumns() {
        if (m_helper.nodeHasPrimaryKeyJoinColumns(m_node)) {
            return true;
        } else {
            return super.hasPrimaryKeyJoinColumns();
        }
    }
    
    /**
     * INTERNAL: (Override from OneToOneAccessor)
     */
    public boolean isOptional() {
        return m_helper.isOptional(m_node);
    }
}
