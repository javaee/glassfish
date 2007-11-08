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

import javax.persistence.PrimaryKeyJoinColumn;

import oracle.toplink.essentials.internal.helper.DatabaseField;

/**
 * Object to hold onto join column metadata in a TopLink database fields.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class MetadataPrimaryKeyJoinColumn {
    protected DatabaseField m_pkField;
    protected DatabaseField m_fkField;
    
    public static final String DEFAULT_NAME = "";
    public static final String DEFAULT_COLUMN_DEFINITION = "";
    public static final String DEFAULT_REFERENCED_COLUMN_NAME = "";
    
    /**
     * INTERNAL:
     * Called for association override.
     */
    public MetadataPrimaryKeyJoinColumn(PrimaryKeyJoinColumn primaryKeyJoinColumn, String sourceTableName, String targetTableName) {
        this(sourceTableName, targetTableName);
        
        if (primaryKeyJoinColumn != null) {
            // Process the primary key field metadata.
            m_pkField.setName(primaryKeyJoinColumn.referencedColumnName());
        
            // Process the foreign key field metadata.
            m_fkField.setName(primaryKeyJoinColumn.name());
            m_fkField.setColumnDefinition(primaryKeyJoinColumn.columnDefinition());
        }
    }
    
    /**
     * INTERNAL:
     */
    public MetadataPrimaryKeyJoinColumn(String sourceTableName, String targetTableName) {
        this(sourceTableName, targetTableName, DEFAULT_REFERENCED_COLUMN_NAME, DEFAULT_NAME);
    }
    
    /**
     * INTERNAL:
     */
    public MetadataPrimaryKeyJoinColumn(String sourceTableName, String targetTableName, String defaultFieldName) {
        this(sourceTableName, targetTableName, defaultFieldName, defaultFieldName);
    }
    
    /**
     * INTERNAL:
     */
    protected MetadataPrimaryKeyJoinColumn(String sourceTableName, String targetTableName, String defaultPKFieldName, String defaultFKFieldName) {
        m_pkField = new DatabaseField();
        m_pkField.setName(defaultPKFieldName);
        m_pkField.setTableName(sourceTableName);
        
        m_fkField = new DatabaseField();
        m_fkField.setName(defaultFKFieldName);
        m_fkField.setTableName(targetTableName);
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
