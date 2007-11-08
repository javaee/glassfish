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

import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataHelper;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataLogger;

import oracle.toplink.essentials.internal.helper.DatabaseTable;

/**
 * Object to hold onto table metadata in a TopLink database table.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class MetadataTable  {
    protected String m_name;
    protected String m_schema;
    protected String m_catalog;
    protected MetadataLogger m_logger;
    protected DatabaseTable m_databaseTable;
    
    /**
     * INTERNAL:
     */
    public MetadataTable(MetadataLogger logger) {
        m_logger = logger;
        m_databaseTable = new DatabaseTable();
    }
    
    /**
     * INTERNAL:
     */
    public MetadataTable(Table table, MetadataLogger logger) {
        this(logger);
        
        if (table != null) {
            m_name = table.name();
            m_schema = table.schema();
            m_catalog = table.catalog();
            
            processName();
            processUniqueConstraints(table.uniqueConstraints());        
        }
    }
    
    /**
     * INTERNAL:
     */
    public String getCatalog() {
        return m_catalog;
    }
    
    /**
     * INTERNAL:
     * The context should be overridden by subclasses for more specific
     * logging messages.
     */
    public String getCatalogContext() {
        return m_logger.TABLE_CATALOG;
    }
    
    /**
     * INTERNAL:
     */
    public DatabaseTable getDatabaseTable() {
        return m_databaseTable;
    }
    
    /**
     * INTERNAL:
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * INTERNAL:
     * The context should be overridden by subclasses for more specific
     * logging messages.
     */
    public String getNameContext() {
        return m_logger.TABLE_NAME;
    }
    
    /**
     * INTERNAL:
     */
    public String getSchema() {
        return m_schema;
    }
    
    /**
     * INTERNAL:
     * The context should be overridden by subclasses for more specific
     * logging messages.
     */
    public String getSchemaContext() {
        return m_logger.TABLE_SCHEMA;
    }

    /**
     * INTERNAL:
     */
    public boolean loadedFromXML() {
        return false;
    }
    
    /**
     * INTERNAL:
     */
    protected void processName() {
        // Don't bother setting the name if name is blank.
        if (! m_name.equals("")) {
            setName(MetadataHelper.getFullyQualifiedTableName(m_name, m_catalog, m_schema));
        }
    }
    
    /**
     * INTERNAL:
     * Process the unique constraints for the given table.
     */
    protected void processUniqueConstraints(UniqueConstraint[] uniqueConstraints) {
        for (UniqueConstraint uniqueConstraint : uniqueConstraints) {
            m_databaseTable.addUniqueConstraints(uniqueConstraint.columnNames());
        }
    }
    
    /**
     * INTERNAL:
     */
    public void setName(String name) {
        m_databaseTable.setPossiblyQualifiedName(name);  
    }
}
