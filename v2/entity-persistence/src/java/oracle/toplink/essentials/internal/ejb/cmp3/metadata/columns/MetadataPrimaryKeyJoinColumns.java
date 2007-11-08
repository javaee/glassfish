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

import java.util.List;
import java.util.ArrayList;

import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataDescriptor;

/**
 * Object to hold onto join column metadata.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class MetadataPrimaryKeyJoinColumns {
    private String m_sourceTableName;
    private String m_targetTableName;
    protected List<MetadataPrimaryKeyJoinColumn> m_pkJoinColumns;
    
    /**
     * INTERNAL:
     */
    public MetadataPrimaryKeyJoinColumns(String sourceTableName, String targetTableName) {
        m_sourceTableName = sourceTableName;
        m_targetTableName = targetTableName;
        m_pkJoinColumns = new ArrayList<MetadataPrimaryKeyJoinColumn>();
    }
    
    /**
     * INTERNAL:
     */
    public MetadataPrimaryKeyJoinColumns(PrimaryKeyJoinColumn[] primaryKeyJoinColumns, String sourceTableName, String targetTableName) {
        this(sourceTableName, targetTableName);
        
        // Process the primary key join column array.
        for (PrimaryKeyJoinColumn pkJoinColumn : primaryKeyJoinColumns) {
            m_pkJoinColumns.add(new MetadataPrimaryKeyJoinColumn(pkJoinColumn, sourceTableName, targetTableName));
        }
    }
    
    /**
     * INTERNAL:
     */
    public MetadataPrimaryKeyJoinColumns(PrimaryKeyJoinColumns primaryKeyJoinColumns, PrimaryKeyJoinColumn primaryKeyJoinColumn, String sourceTableName, String targetTableName) {
        this(sourceTableName, targetTableName);
        
        // Process all the primary key join columns first.
        if (primaryKeyJoinColumns != null) {
            for (PrimaryKeyJoinColumn pkJoinColumn : primaryKeyJoinColumns.value()) {
                m_pkJoinColumns.add(new MetadataPrimaryKeyJoinColumn(pkJoinColumn, sourceTableName, targetTableName));
            }
        }
        
        // Process the single primary key join column second.
        if (primaryKeyJoinColumn != null) {
            m_pkJoinColumns.add(new MetadataPrimaryKeyJoinColumn(primaryKeyJoinColumn, sourceTableName, targetTableName));
        }
    }
    
    /**
     * INTERNAL:
     * 
     * This method is called when it is time to process the primary key join
     * columns. So if the user didn't specify any, then we need to default
     * accordingly.
     */
    public List<MetadataPrimaryKeyJoinColumn> values(MetadataDescriptor descriptor) {
        // If no primary key join columns are specified ...
        if (m_pkJoinColumns.isEmpty()) {
            if (descriptor.hasCompositePrimaryKey()) {
                // Add a default one for each part of the composite primary
                // key. Foreign and primary key to have the same name.
                for (String primaryKeyField : descriptor.getPrimaryKeyFieldNames()) {
                    m_pkJoinColumns.add(new MetadataPrimaryKeyJoinColumn(m_sourceTableName, m_targetTableName, primaryKeyField));
                }
            } else {
                // Add a default one for the single case, not setting any
                // foreign and primary key names. They will default based
                // on which accessor is using them.
                m_pkJoinColumns.add(new MetadataPrimaryKeyJoinColumn(m_sourceTableName, m_targetTableName));
            }
        }
        
        return m_pkJoinColumns;
    }
    
    /**
     * INTERNAL:
     */
    public boolean loadedFromXML() {
        return false;
    }
}
