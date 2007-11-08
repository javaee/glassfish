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
package oracle.toplink.essentials.internal.ejb.cmp3.metadata.sequencing;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;

/**
 * A wrapper class to the MetadataTableGenerator that holds onto a 
 * @TableGenerator for its metadata values.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class MetadataTableGenerator extends MetadataGenerator {
    protected TableGenerator m_tableGenerator;
    protected List<String[]> m_uniqueConstraints;
    
    /**
     * INTERNAL:
     */
    protected MetadataTableGenerator(String entityClassName) {
        super(entityClassName);
    }
    
    /**
     * INTERNAL:
     */
    public MetadataTableGenerator(TableGenerator tableGenerator, String entityClassName) {
        super(entityClassName);
        m_tableGenerator = tableGenerator;
    }
    
    /**
     * INTERNAL:
     */
    public boolean equals(Object objectToCompare) {
        if (objectToCompare instanceof MetadataTableGenerator) {
            MetadataTableGenerator generator = (MetadataTableGenerator) objectToCompare;
            
            if (!generator.getName().equals(getName())) { 
                return false;
            }
            
            if (generator.getInitialValue() != getInitialValue()) {
                return false;
            }
            
            if (generator.getAllocationSize() != getAllocationSize()) {
                return false;
            }
            
            if (!generator.getPkColumnName().equals(getPkColumnName())) {
                return false;
            }
            
            if (!generator.getValueColumnName().equals(getValueColumnName())) {
                return false;
            }
            
            if (!generator.getPkColumnValue().equals(getPkColumnValue())) {
                return false;
            }
            
            if (!generator.getTable().equals(getTable())) {
                return false;
            }
                
            if (!generator.getSchema().equals(getSchema())) {
                return false;
            }
                
            return generator.getCatalog().equals(getCatalog());
        }
        
        return false;
    }
    
    /**
     * INTERNAL:
     */
    public int getAllocationSize() {
        return m_tableGenerator.allocationSize();
    }
    
    /**
     * INTERNAL:
     * WIP - need to take into consideration the global catalog from XML.
     */
    public String getCatalog() {
        return m_tableGenerator.catalog();
    }
    
    /**
     * INTERNAL:
     */
    public int getInitialValue() {
        return m_tableGenerator.initialValue();
    }
    
    /**
     * INTERNAL:
     */
    public String getName() {
        return m_tableGenerator.name();
    }
    
    /**
     * INTERNAL:
     */
    public String getPkColumnName() {
        return m_tableGenerator.pkColumnName();
    }
    
    /**
     * INTERNAL:
     */
    public String getPkColumnValue() {
        return m_tableGenerator.pkColumnValue();
    }
    
    /**
     * INTERNAL:
     * WIP - need to take into consideration the global schema from XML.
     */
    public String getSchema() {
        return m_tableGenerator.schema();
    }
    
    /**
     * INTERNAL:
     */
    public String getTable() {
        return m_tableGenerator.table();
    }
 
    /**
     * INTERNAL:
     */
    public List<String[]> getUniqueConstraints() {
        if (m_uniqueConstraints == null) {
            m_uniqueConstraints = new ArrayList<String[]>();
            
            for (UniqueConstraint uniqueConstraint : m_tableGenerator.uniqueConstraints()) {
                m_uniqueConstraints.add(uniqueConstraint.columnNames());
            }
        }
        
        return m_uniqueConstraints;
    }
    
    /**
     * INTERNAL:
     */
    public String getValueColumnName() {
        return m_tableGenerator.valueColumnName();
    }
}
