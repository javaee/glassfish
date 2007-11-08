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
package oracle.toplink.essentials.internal.ejb.cmp3.metadata.tables;

import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataLogger;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataJoinColumns;

/**
 * Object to hold onto table metadata in a TopLink database table.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class MetadataJoinTable extends MetadataTable {
    private JoinColumn[] m_joinColumns;
    private JoinColumn[] m_inverseJoinColumns;
    protected MetadataJoinColumns m_jColumns;
    protected MetadataJoinColumns m_inverseJColumns;
    
    /**
     * INTERNAL:
     */
    public MetadataJoinTable(MetadataLogger logger) {
        super(logger);
        
        m_joinColumns = new JoinColumn[] {};
        m_inverseJoinColumns = new JoinColumn[] {};
    }
    
    /**
     * INTERNAL:
     */
    public MetadataJoinTable(JoinTable joinTable, MetadataLogger logger) {
        this(logger);
        
        if (joinTable != null) {
            m_name = joinTable.name();
            m_schema = joinTable.schema();
            m_catalog = joinTable.catalog();
            m_joinColumns = joinTable.joinColumns();
            m_inverseJoinColumns = joinTable.inverseJoinColumns();
            
            processName();
            processUniqueConstraints(joinTable.uniqueConstraints());
        }
    }
    
    /**
     * INTERNAL:
     */
    public String getCatalogContext() {
        return m_logger.JOIN_TABLE_CATALOG;
    }
    
    /**
     * INTERNAL:
     */
    public MetadataJoinColumns getInverseJoinColumns() {
        if (m_inverseJColumns == null) {
            m_inverseJColumns = processInverseJoinColumns();
        }
        
        return m_inverseJColumns;
    }
    
    /**
     * INTERNAL:
     */
    public MetadataJoinColumns getJoinColumns() {
        if (m_jColumns == null) {
            m_jColumns = processJoinColumns();
        }
        
        return m_jColumns;
    }
    
    /**
     * INTERNAL:
     */
    public String getNameContext() {
        return m_logger.JOIN_TABLE_NAME;
    }
    
    /**
     * INTERNAL:
     */
    public String getSchemaContext() {
        return m_logger.JOIN_TABLE_SCHEMA;
    }
    
    /**
     * INTERNAL:
     */
    public boolean loadedFromXML() {
        return false;
    }
    
    /**
     * INTERNAL: (Overridden in XMLJoinTable)
     */
    protected MetadataJoinColumns processInverseJoinColumns() {
        return new MetadataJoinColumns(m_inverseJoinColumns);
    }
    
    /**
     * INTERNAL: (Overridden in XMLJoinTable)
     */
    protected MetadataJoinColumns processJoinColumns() {
        return new MetadataJoinColumns(m_joinColumns);
    }
}
