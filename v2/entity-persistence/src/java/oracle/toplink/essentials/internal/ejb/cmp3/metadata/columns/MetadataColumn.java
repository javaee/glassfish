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

import java.lang.reflect.AnnotatedElement;

import javax.persistence.Column;
import javax.persistence.AttributeOverride;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.MetadataAccessor;
import oracle.toplink.essentials.internal.helper.DatabaseField;

/**
 * Object to hold onto column metadata in a TopLink database field.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class MetadataColumn  {
    protected String m_attributeName;
    protected DatabaseField m_databaseField;
    protected AnnotatedElement m_annotatedElement;
    
    public static final int DEFAULT_SCALE = 0;
    public static final int DEFAULT_LENGTH = 255;
    public static final int DEFAULT_PRECISION = 0;
    
    public static final String DEFAULT_NAME = "";
    public static final String DEFAULT_TABLE = "";
    public static final String DEFAULT_COLUMN_DEFINITION = "";
    
    public static final boolean DEFAULT_UNIQUE = false;
    public static final boolean DEFAULT_NULLABLE = true;
    public static final boolean DEFAULT_UPDATABLE = true;
    public static final boolean DEFAULT_INSERTABLE = true;
    
    /**
     * INTERNAL:
     * Called for attribute overrides.
     */
    public MetadataColumn(AttributeOverride attributeOverride, AnnotatedElement annotatedElement) {
        this(attributeOverride.column(), attributeOverride.name(), annotatedElement);
    }
    
    /**
     * INTERNAL:
     * Called for basic mappings.
     */
    public MetadataColumn(Column column, MetadataAccessor accessor) {
        this(column, accessor.getAttributeName(), accessor.getAnnotatedElement());
    }
    
    /**
     * INTERNAL:
     */
    public MetadataColumn(Column column, String attributeName, AnnotatedElement annotatedElement) {
        this(attributeName, annotatedElement);
        
        if (column != null) {
            // Apply the values from the column annotation.
            m_databaseField.setUnique(column.unique());
            m_databaseField.setNullable(column.nullable());
            m_databaseField.setUpdatable(column.updatable());
            m_databaseField.setInsertable(column.insertable());
        
            m_databaseField.setScale(column.scale());
            m_databaseField.setLength(column.length());
            m_databaseField.setPrecision(column.precision());
        
            m_databaseField.setName(column.name());
            m_databaseField.setTableName(column.table());
            m_databaseField.setColumnDefinition(column.columnDefinition());
        }
    }
    
    /**
     * INTERNAL:
     * Initialize the database field with the default values.
     */
    public MetadataColumn(String attributeName, AnnotatedElement annotatedElement) {
        m_attributeName = attributeName;
        m_annotatedElement = annotatedElement;
        m_databaseField = new DatabaseField();
     
        // Apply default values.   
        m_databaseField.setUnique(DEFAULT_UNIQUE);
        m_databaseField.setNullable(DEFAULT_NULLABLE);
        m_databaseField.setUpdatable(DEFAULT_UPDATABLE);
        m_databaseField.setInsertable(DEFAULT_INSERTABLE);
        
        m_databaseField.setScale(DEFAULT_SCALE);
        m_databaseField.setLength(DEFAULT_LENGTH);
        m_databaseField.setPrecision(DEFAULT_PRECISION);
        
        m_databaseField.setName(DEFAULT_NAME);
        m_databaseField.setTableName(DEFAULT_TABLE);
        m_databaseField.setColumnDefinition(DEFAULT_COLUMN_DEFINITION);
    }
    
    /**
     * INTERNAL:
     */
    public AnnotatedElement getAnnotatedElement() {
        return m_annotatedElement;
    }
    
    /**
     * INTERNAL:
     */
    public String getAttributeName() {
        return m_attributeName;
    }
    
    /**
     * INTERNAL:
     */
    public DatabaseField getDatabaseField() {
        return m_databaseField;
    }
    
    /**
     * INTERNAL:
     */
    public String getUpperCaseAttributeName() {
        return m_attributeName.toUpperCase();
    }
    
    /**
     * INTERNAL:
     */
    public boolean loadedFromXML() {
        return false;
    }
    
    /**
     * INTERNAL:
     * 
     * This method will get called if we have an attribute override that
     * overrides another attribute override. See EmbeddedAccessor.
     */
    public void setDatabaseField(DatabaseField databaseField) {
        m_databaseField = databaseField;
    }
}
