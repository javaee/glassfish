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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataDescriptor;
import oracle.toplink.essentials.internal.helper.DatabaseField;

/**
 * Object to hold onto join column metadata.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class MetadataJoinColumns {
    protected List<MetadataJoinColumn> m_joinColumns;
    
    /**
     * INTERNAL:
     */
    public MetadataJoinColumns() {
        m_joinColumns = new ArrayList<MetadataJoinColumn>();
    }
    
    /**
     * INTERNAL:
     */
    public MetadataJoinColumns(JoinColumns joinColumns, JoinColumn joinColumn) {
        this();
        
        // Process all the join columns first.
        if (joinColumns != null) {
            for (JoinColumn jColumn : joinColumns.value()) {
                m_joinColumns.add(new MetadataJoinColumn(jColumn));
            }
        }
        
        // Process the single key join column second.
        if (joinColumn != null) {
            m_joinColumns.add(new MetadataJoinColumn(joinColumn));
        }
    }
    
    /**
     * INTERNAL:
     */
    public MetadataJoinColumns(JoinColumn[] joinColumns) {
        this();
        
        for (JoinColumn joinColumn : joinColumns) {
            m_joinColumns.add(new MetadataJoinColumn(joinColumn));
        }
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
    public List<MetadataJoinColumn> values(MetadataDescriptor descriptor) {
        // If no join columns are specified ...
        if (m_joinColumns.isEmpty()) {
            if (descriptor.hasCompositePrimaryKey()) {
                // Add a default one for each part of the composite primary
                // key. Foreign and primary key to have the same name.
                for (String primaryKeyField : descriptor.getPrimaryKeyFieldNames()) {
                    m_joinColumns.add(new MetadataJoinColumn(primaryKeyField));
                }
            } else {
                // Add a default one for the single case, not setting any
                // foreign and primary key names. They will default based
                // on which accessor is using them.
                m_joinColumns.add(new MetadataJoinColumn());
            }
        } else {
            // Need to update any join columns that use a foreign key name
            // for the primary key name. E.G. User specifies the renamed id
            // field name from a primary key join column as the primary key in
            // an inheritance subclass.
            for (MetadataJoinColumn joinColumn : m_joinColumns) {
                DatabaseField pkField = joinColumn.getPrimaryKeyField();
                pkField.setName(descriptor.getPrimaryKeyJoinColumnAssociation(pkField.getName()));
            }
        }
        
        return m_joinColumns;
    }
}
