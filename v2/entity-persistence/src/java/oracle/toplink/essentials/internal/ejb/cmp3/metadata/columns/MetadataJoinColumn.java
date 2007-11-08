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
package oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns;

import javax.persistence.JoinColumn;

import oracle.toplink.essentials.internal.helper.DatabaseField;

/**
 * Object to hold onto join column metadata in a TopLink database fields.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class MetadataJoinColumn {
    protected DatabaseField m_pkField;
    protected DatabaseField m_fkField;
    
    public static final String DEFAULT_NAME = "";
    public static final String DEFAULT_TABLE = "";
    public static final String DEFAULT_COLUMN_DEFINITION = "";
    public static final String DEFAULT_REFERENCED_COLUMN_NAME = "";
    
    public static final boolean DEFAULT_UNIQUE = false;
    public static final boolean DEFAULT_NULLABLE = true;
    public static final boolean DEFAULT_UPDATABLE = true;
    public static final boolean DEFAULT_INSERTABLE = true;
    
    /**
     * INTERNAL:
     */
    public MetadataJoinColumn() {
        this(DEFAULT_REFERENCED_COLUMN_NAME, DEFAULT_NAME);
    }
    
    /**
     * INTERNAL:
     * Called for association override.
     */
    public MetadataJoinColumn(JoinColumn joinColumn) {
        this();
        
        if (joinColumn != null) {
            // Process the primary key field metadata.
            m_pkField.setName(joinColumn.referencedColumnName());
        
            // Process the foreign key field metadata.
            m_fkField.setName(joinColumn.name());
            m_fkField.setTableName(joinColumn.table());
            m_fkField.setUnique(joinColumn.unique());
            m_fkField.setNullable(joinColumn.nullable());
            m_fkField.setUpdatable(joinColumn.updatable());
            m_fkField.setInsertable(joinColumn.insertable());
            m_fkField.setColumnDefinition(joinColumn.columnDefinition());
        }
    }
    
    /**
     * INTERNAL:
     */
    public MetadataJoinColumn(String defaultName) {
        this(defaultName, defaultName);
    }
    
    /**
     * INTERNAL:
     */
    protected MetadataJoinColumn(String defaultReferenceColumnName, String defaultName) {
        m_pkField = new DatabaseField();
        m_pkField.setName(defaultReferenceColumnName);
        
        m_fkField = new DatabaseField();
        m_fkField.setName(defaultName);
        m_fkField.setTableName(DEFAULT_TABLE);
        m_fkField.setUnique(DEFAULT_UNIQUE);
        m_fkField.setNullable(DEFAULT_NULLABLE);
        m_fkField.setUpdatable(DEFAULT_UPDATABLE);
        m_fkField.setInsertable(DEFAULT_INSERTABLE);
        m_fkField.setColumnDefinition(DEFAULT_COLUMN_DEFINITION);
    }
    
    /**
     * INTERNAL:
     */
    public DatabaseField getForeignKeyField() {
        return m_fkField;
    }
    
    /**
     * INTERNAL:
     */
    public DatabaseField getPrimaryKeyField() {
        return m_pkField;
    }
    
    /**
     * INTERNAL:
     */
    public boolean isForeignKeyFieldNotSpecified() {
        return m_fkField.getName().equals("");
    }
    
    /**
     * INTERNAL:
     */
    public boolean isPrimaryKeyFieldNotSpecified() {
        return m_pkField.getName().equals("");
    }
    
    /**
     * INTERNAL:
     */
    public boolean loadedFromXML() {
        return false;
    }
}
