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

import java.util.List;

import javax.persistence.ManyToOne;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataLogger;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.ClassAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;

import oracle.toplink.essentials.mappings.OneToOneMapping;

/**
 * A many to one relationship accessor.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class ManyToOneAccessor extends ObjectAccessor {    
    private ManyToOne m_manyToOne;
    
    /**
     * INTERNAL:
     */
    public ManyToOneAccessor(MetadataAccessibleObject accessibleObject, ClassAccessor classAccessor) {
        super(accessibleObject, classAccessor);
        m_manyToOne = getAnnotation(ManyToOne.class);
    }
    
    /**
     * INTERNAL: (Overridden in XMLManyToOneAccessor)
     */
    public List<String> getCascadeTypes() {
        return getCascadeTypes(m_manyToOne.cascade());
    }
    
    /**
     * INTERNAL: (Overridden in XMLManyToOneAccessor)
     */
    public String getFetchType() {
        return m_manyToOne.fetch().name();
    }
    
    /**
     * INTERNAL:
     * 
     * Return the logging context for this accessor.
     */
    protected String getLoggingContext() {
        return m_logger.MANY_TO_ONE_MAPPING_REFERENCE_CLASS;
    }
    
    /**
     * INTERNAL: (Overridden in XMLManyToOneAccessor)
     */
    public Class getTargetEntity() {
        return m_manyToOne.targetEntity();
    }
    
    /**
     * INTERNAL:
     */
	public boolean isManyToOne() {
        return true;
    }
    
    /**
     * INTERNAL: (Overridden in XMLManyToOneAccessor)
     */
    public boolean isOptional() {
        return m_manyToOne.optional();
    }
    
    /**
     * INTERNAL:
     * Process a @ManyToOne or many-to-one element into a TopLink OneToOne 
     * mapping.
     */
    public void process() {
        // Initialize our mapping now with what we found.
        OneToOneMapping mapping = initOneToOneMapping();

        // Now process the JoinColumns (if there are any) for this mapping.
        processOwningMappingKeys(mapping);
        
        // Add the mapping to the descriptor.
        m_descriptor.addMapping(mapping);
    }
}
