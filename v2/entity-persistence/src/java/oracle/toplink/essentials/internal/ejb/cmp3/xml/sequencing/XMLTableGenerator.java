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
package oracle.toplink.essentials.internal.ejb.cmp3.xml.sequencing;

import java.util.List;
import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLConstants;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.accessors.XMLAccessor;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.sequencing.MetadataTableGenerator;

/**
 * Object to hold onto an xml table generator metadata.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class XMLTableGenerator extends MetadataTableGenerator {
    protected Node m_node;
    protected XMLAccessor m_accessor;
    
    /**
     * INTERNAL:
     */
    public XMLTableGenerator(Node node, XMLAccessor accessor) {
        super(accessor.getDocumentName());
        
        m_node = node;
        m_accessor = accessor;
    }
    
    /**
     * INTERNAL:
     */
    public int getAllocationSize() {
        return m_accessor.getHelper().getNodeValue(m_node, XMLConstants.ATT_ALLOCATION_SIZE, 50);
    }
    
    /**
     * INTERNAL:
     */
    public String getCatalog() {
        return m_accessor.getHelper().getNodeValue(m_node, XMLConstants.ATT_CATALOG, m_accessor.getCatalog());
    }
    
    /**
     * INTERNAL:
     */
    public int getInitialValue() {
        return m_accessor.getHelper().getNodeValue(m_node, XMLConstants.ATT_INITIAL_VALUE, 0);
    }
    
    /**
     * INTERNAL:
     */
    public String getName() {
        return m_accessor.getHelper().getNodeValue(m_node, XMLConstants.ATT_NAME);
    }
    
    /**
     * INTERNAL:
     */
    public String getPkColumnName() {
        return m_accessor.getHelper().getNodeValue(m_node, XMLConstants.ATT_PK_COLUMN_NAME);
    }
    
    /**
     * INTERNAL:
     */
    public String getPkColumnValue() {
        return m_accessor.getHelper().getNodeValue(m_node, XMLConstants.ATT_PK_COLUMN_VALUE);
    }
    
    /**
     * INTERNAL:
     */
    public String getSchema() {
        return m_accessor.getHelper().getNodeValue(m_node, XMLConstants.ATT_SCHEMA, m_accessor.getSchema());
    }
    
    /**
     * INTERNAL:
     */
    public String getTable() {
        return m_accessor.getHelper().getNodeValue(m_node, XMLConstants.ATT_TABLE);
    }
    
    /**
     * INTERNAL:
     */
    public List<String[]> getUniqueConstraints() {
        if (m_uniqueConstraints == null) {
            m_uniqueConstraints = new ArrayList<String[]>();
            NodeList uniqueConstraintNodes = m_accessor.getHelper().getNodes(m_node, XMLConstants.UNIQUE_CONSTRAINTS);
        
            if (uniqueConstraintNodes != null) {
                for (int i = 0; i < uniqueConstraintNodes.getLength(); i++) {
                    NodeList columnNameNodes = m_accessor.getHelper().getTextColumnNodes(uniqueConstraintNodes.item(i));
                
                    if (columnNameNodes != null) {
                        List<String> columnNames = new ArrayList<String>(columnNameNodes.getLength());
                        for (int k = 0; k < columnNameNodes.getLength(); k++) {
                            String columnName = columnNameNodes.item(k).getNodeValue();
                        
                            if (columnName != null && !columnName.equals("")) {
                                columnNames.add(columnName);
                            }
                        }
                        if (columnNames.size() > 0) {
                            m_uniqueConstraints.add(columnNames.toArray(new String[0]));
                        }
                    }
                }
            }
        }
        
        return m_uniqueConstraints;
    }
    
    /**
     * INTERNAL:
     */
    public String getValueColumnName() {
        return m_accessor.getHelper().getNodeValue(m_node, XMLConstants.ATT_VALUE_COLUMN_NAME);
    }
    
    /**
     * INTERNAL:
     */
    public boolean loadedFromAnnotations() {
       return false; 
    }
    
    /**
     * INTERNAL:
     */
    public boolean loadedFromXML() {
       return true; 
    }
}
