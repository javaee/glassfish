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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.OneToOne;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataConstants;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;

import oracle.toplink.essentials.mappings.OneToOneMapping;

/**
 * A one to one relationship accessor. A @OneToOne annotation currently is not
 * required to be on the accessible object, that is, a 1-1 can default.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class OneToOneAccessor extends ObjectAccessor {
    private OneToOne m_oneToOne;
    
    /**
     * INTERNAL:
     */
    public OneToOneAccessor(MetadataAccessibleObject accessibleObject, ClassAccessor classAccessor) {
        super(accessibleObject, classAccessor);
        m_oneToOne = getAnnotation(OneToOne.class);
    }
    
    /**
     * INTERNAL: (Overridden in XMLOneToOneAccessor)
     */
    public List<String> getCascadeTypes() {
        if (m_oneToOne == null) {
            return new ArrayList<String>();
        } else {
            return getCascadeTypes(m_oneToOne.cascade());
        } 
    }
    
    /**
     * INTERNAL: (Overridden in XMLOneToOneAccessor)
     */
    public String getFetchType() {
        return (m_oneToOne == null) ? MetadataConstants.EAGER : m_oneToOne.fetch().name();
    }
    
    /**
     * INTERNAL:
     * 
     * Return the logging context for this accessor.
     */
    protected String getLoggingContext() {
        return m_logger.ONE_TO_ONE_MAPPING_REFERENCE_CLASS;
    }
    
    /**
     * INTERNAL: (Overridden in XMLOneToOneAccessor)
     */
    public String getMappedBy() {
        return (m_oneToOne == null) ? "" : m_oneToOne.mappedBy();
    }
    
    /**
     * INTERNAL: (Overridden in XMLOneToOneAccessor)
     */
    public Class getTargetEntity() {
        return (m_oneToOne == null) ? void.class : m_oneToOne.targetEntity();
    }
    
    /**
     * INTERNAL:
     */
	public boolean isOneToOne() {
        return true;
    }
    
    /**
     * INTERNAL:
     */
    public boolean isOptional() {
        return (m_oneToOne == null) ? true : m_oneToOne.optional();
    }
    
    /**
     * INTERNAL:
     * Process a @OneToOne or one-to-one element into a TopLink OneToOne 
     * mapping.
     */
    public void process() {
        // Initialize our mapping now with what we found.
        OneToOneMapping mapping = initOneToOneMapping();
        
        if (getMappedBy().equals("")) {
            // Owning side, look for JoinColumns or PrimaryKeyJoinColumns.
            processOwningMappingKeys(mapping);
        } else {	
            // Non-owning side, process the foreign keys from the owner.
            OneToOneMapping ownerMapping = null;
            if (getOwningMapping().isOneToOneMapping()){
            	ownerMapping = (OneToOneMapping)getOwningMapping();
            } else {
            	// If improper mapping encountered, throw an exception.
            	getValidator().throwInvalidMappingEncountered(getJavaClass(), getReferenceClass());
            }

            mapping.setSourceToTargetKeyFields(ownerMapping.getTargetToSourceKeyFields());
            mapping.setTargetToSourceKeyFields(ownerMapping.getSourceToTargetKeyFields());
        }
        
        // Add the mapping to the descriptor.
        m_descriptor.addMapping(mapping);
    }
}
