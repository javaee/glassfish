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

import javax.persistence.ManyToMany;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataHelper;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataLogger;

import java.util.List;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.ClassAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;

import oracle.toplink.essentials.mappings.ManyToManyMapping;

/**
 * A many to many relationship accessor.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class ManyToManyAccessor extends CollectionAccessor {    
    private ManyToMany m_manyToMany;
    
    /**
     * INTERNAL:
     */
    public ManyToManyAccessor(MetadataAccessibleObject accessibleObject, ClassAccessor classAccessor) {
        super(accessibleObject, classAccessor);
        m_manyToMany = getAnnotation(ManyToMany.class);
    }
    
    /**
     * INTERNAL: (Overridden in XMLManyToManyAccessor)
     */
    public List<String> getCascadeTypes() {
        return getCascadeTypes(m_manyToMany.cascade());
    }
    
    /**
     * INTERNAL: (Overridden in XMLManyToManyAccessor)
     */
    public String getFetchType() {
        return m_manyToMany.fetch().name();
    }
    
    /**
     * INTERNAL:
     * 
     * Return the logging context for this accessor.
     */
    protected String getLoggingContext() {
        return m_logger.MANY_TO_MANY_MAPPING_REFERENCE_CLASS;
    }
    
    /**
     * INTERNAL: (Overridden in XMLManyToManyAccessor)
     */
    public String getMappedBy() {
        return m_manyToMany.mappedBy();
    }
    
    /**
     * INTERNAL: (Overridden in XMLManyToManyAccessor)
     */
    public Class getTargetEntity() {
        return m_manyToMany.targetEntity();
    }
    
    /**
     * INTERNAL:
     */
	public boolean isManyToMany() {
        return true;
    }
    
    /**
     * INTERNAL:
     * Process a @ManyToMany or many-to-many element into a TopLink MnayToMany 
     * mapping.
     */
    public void process() {
        // Create a M-M mapping and process common collection mapping metadata.
        ManyToManyMapping mapping = new ManyToManyMapping();
        process(mapping);

        if (getMappedBy().equals("")) { 
            // Processing the owning side of a M-M that is process a join table.
            processJoinTable(getJoinTable(), mapping);
        } else {
            // We are processing the a non-owning side of a M-M. Must set the
            // mapping read-only.
            mapping.setIsReadOnly(true);
            
            // Get the owning mapping from the reference descriptor metadata.
            ManyToManyMapping ownerMapping = null;
            if (getOwningMapping().isManyToManyMapping()){
            	ownerMapping = (ManyToManyMapping)getOwningMapping();
            } else {
            	// If improper mapping encountered, throw an exception.
            	getValidator().throwInvalidMappingEncountered(getJavaClass(), getReferenceClass());
            }

            // Set the relation table name from the owner.
	        mapping.setRelationTableName(ownerMapping.getRelationTableQualifiedName());
	             
	        // Add all the source foreign keys we found on the owner.
	        mapping.setSourceKeyFields(ownerMapping.getTargetKeyFields());
	        mapping.setSourceRelationKeyFields(ownerMapping.getTargetRelationKeyFields());
	            
	        // Add all the target foreign keys we found on the owner.
	        mapping.setTargetKeyFields(ownerMapping.getSourceKeyFields());
	        mapping.setTargetRelationKeyFields(ownerMapping.getSourceRelationKeyFields());
        }

        // Add the mapping to the descriptor.
        m_descriptor.addMapping(mapping);
    }
}
