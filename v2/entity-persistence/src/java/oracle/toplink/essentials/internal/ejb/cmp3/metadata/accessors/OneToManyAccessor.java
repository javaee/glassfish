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
package oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors;

import javax.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataConstants;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataLogger;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.ClassAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;

import oracle.toplink.essentials.internal.helper.DatabaseField;

import oracle.toplink.essentials.mappings.ManyToManyMapping;
import oracle.toplink.essentials.mappings.OneToManyMapping;
import oracle.toplink.essentials.mappings.OneToOneMapping;

/**
 * A OneToMany relationship accessor. A @OneToMany annotation currently is not
 * required to be on the accessible object, that is, a 1-M can default.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class OneToManyAccessor extends CollectionAccessor {
    private OneToMany m_oneToMany;
    
    /**
     * INTERNAL:
     */
    public OneToManyAccessor(MetadataAccessibleObject accessibleObject, ClassAccessor classAccessor) {
        super(accessibleObject, classAccessor);
        m_oneToMany = getAnnotation(OneToMany.class);
    }
    
    /**
     * INTERNAL: (Overridden in XMLOneToManyAccessor)
     */
    public List<String> getCascadeTypes() {
        if (m_oneToMany == null) {
            return new ArrayList<String>();
        } else {
            return getCascadeTypes(m_oneToMany.cascade());
        } 
    }
    
    /**
     * INTERNAL: (Overridden in XMLOneToManyAccessor)
     */
    public String getFetchType() {
        return (m_oneToMany == null) ? MetadataConstants.LAZY : m_oneToMany.fetch().name();
    }
    
    /**
     * INTERNAL:
     * 
     * Return the logging context for this accessor.
     */
    protected String getLoggingContext() {
        return m_logger.ONE_TO_MANY_MAPPING_REFERENCE_CLASS;
    }
    
    /**
     * INTERNAL: (Overridden in XMLOneToManyAccessor)
     */
    public String getMappedBy() {
        return (m_oneToMany == null) ? "" : m_oneToMany.mappedBy();
    }
    
    /**
     * INTERNAL: (Overridden in XMLOneToManyAccessor)
     */
    public Class getTargetEntity() {
        return (m_oneToMany == null) ? void.class : m_oneToMany.targetEntity();
    }
    
    /**
     * INTERNAL:
     */
	public boolean isOneToMany() {
        return true;
    }
    
    /**
     * INTERNAL:
     * Process an @OneToMany or one-to-many element into a TopLink OneToMany 
     * mapping. If a JoinTable is found however, we must create a ManyToMany 
     * mapping.
     */
    public void process() {
        String mappedBy = getMappedBy();
        
        // Should be treated as a uni-directional mapping using a join table.
        if (mappedBy.equals("")) {
            // If we find a JoinColumn(s), then throw an exception.
            if (hasJoinColumn() || hasJoinColumns()) {
                getValidator().throwUniDirectionalOneToManyHasJoinColumnSpecified(getAttributeName(), getJavaClass());
            }
            
            // Create a M-M mapping and process common collection mapping
            // metadata.
            ManyToManyMapping mapping = new ManyToManyMapping();
            process(mapping);
            
            // Process the @JoinTable.
            processJoinTable(getJoinTable(), mapping);
            
            // Add the mapping to the descriptor.
            m_descriptor.addMapping(mapping);
        } else {
            // Create a 1-M mapping and process common collection mapping
            // metadata.
            OneToManyMapping mapping = new OneToManyMapping();
            process(mapping);
            
            // Non-owning side, process the foreign keys from the owner.
			OneToOneMapping ownerMapping = null;
            if (getOwningMapping().isOneToOneMapping()){ 
            	ownerMapping = (OneToOneMapping) getOwningMapping();
            } else {
				// If improper mapping encountered, throw an exception.
            	getValidator().throwInvalidMappingEncountered(getJavaClass(), getReferenceClass()); 
            }
                
            Map<DatabaseField, DatabaseField> keys = ownerMapping.getSourceToTargetKeyFields();
            for (DatabaseField fkField : keys.keySet()) {
                mapping.addTargetForeignKeyField(fkField, keys.get(fkField));
            }   
            
            // Add the mapping to the descriptor.
            m_descriptor.addMapping(mapping);
        }
    }
}
