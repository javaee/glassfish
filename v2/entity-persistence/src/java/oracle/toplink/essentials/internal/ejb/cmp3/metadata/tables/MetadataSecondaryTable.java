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

import javax.persistence.SecondaryTable;
import javax.persistence.PrimaryKeyJoinColumn;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataPrimaryKeyJoinColumns;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataLogger;

/**
 * Object to hold onto a secondary table metadata in a TopLink database table.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class MetadataSecondaryTable extends MetadataTable  {
    private PrimaryKeyJoinColumn[] m_pkJoinColumns;
    protected MetadataPrimaryKeyJoinColumns m_primaryKeyJoinColumns;
    
    /**
     * INTERNAL:
     */
    public MetadataSecondaryTable(MetadataLogger logger) {
        super(logger);
        m_primaryKeyJoinColumns = null; 
    }
    
    /**
     * INTERNAL:
     */
    public MetadataSecondaryTable(SecondaryTable secondaryTable, MetadataLogger logger) {
        this(logger);
        
        if (secondaryTable != null) {
            m_name = secondaryTable.name();
            m_schema = secondaryTable.schema();
            m_catalog = secondaryTable.catalog();
            m_pkJoinColumns = secondaryTable.pkJoinColumns();
            
            processName();
            processUniqueConstraints(secondaryTable.uniqueConstraints());
        }
    }
    
    /**
     * INTERNAL:
     */
    public String getCatalogContext() {
        return m_logger.SECONDARY_TABLE_CATALOG;
    }
    
    /**
     * INTERNAL:
     */
    public String getNameContext() {
        return m_logger.SECONDARY_TABLE_NAME;
    }
    
    /**
     * INTERNAL:
     */
    public MetadataPrimaryKeyJoinColumns getPrimaryKeyJoinColumns(String sourceTableName) {
        if (m_primaryKeyJoinColumns == null) {
            processPrimaryKeyJoinColumns(sourceTableName);
        }
        
        return m_primaryKeyJoinColumns;
    }
    
    /**
     * INTERNAL:
     */
    public String getSchemaContext() {
        return m_logger.SECONDARY_TABLE_SCHEMA;
    }
    
    /**
     * INTERNAL: (Overridden in XMLSecondaryTable)
     */
    protected void processPrimaryKeyJoinColumns(String sourceTableName) {
        m_primaryKeyJoinColumns = new MetadataPrimaryKeyJoinColumns(m_pkJoinColumns, sourceTableName, m_databaseTable.getQualifiedName());
    }
}
