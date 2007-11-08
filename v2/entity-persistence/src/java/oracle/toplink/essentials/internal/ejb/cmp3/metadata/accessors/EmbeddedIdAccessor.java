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

import java.util.HashMap;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataColumn;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataDescriptor;
import oracle.toplink.essentials.internal.helper.DatabaseField;

import oracle.toplink.essentials.mappings.AggregateObjectMapping;
import oracle.toplink.essentials.mappings.DatabaseMapping;

/**
 * An embedded id relationship accessor.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class EmbeddedIdAccessor extends EmbeddedAccessor {
    // We store map of fields that are the primary key and add them only at the
    // end of processing since they may change when processing attribute 
    // overrides. They are mapped by attribute name.
    protected HashMap<String, DatabaseField> m_idFields = new HashMap<String, DatabaseField>();

    /**
     * INTERNAL:
     */
    public EmbeddedIdAccessor(MetadataAccessibleObject accessibleObject, ClassAccessor classAccessor) {
        super(accessibleObject, classAccessor);
    }
    
    /**
     * INTERNAL: (Override from MetadataAccesor)
     */
	public boolean isEmbeddedId() {
        return true;
    }
    
    /**
     * INTERNAL: (Override from EmbeddedAccessor)
     * 
     * Process an @EmbeddedId or embedded-id element.
     */    
    public void process() {
        if (m_descriptor.ignoreIDs()) {
            // XML/Annotation merging. XML wins, ignore annotations.
            m_logger.logWarningMessage(m_logger.IGNORE_EMBEDDED_ID, this);
        } else {
            // Check if we already processed an EmbeddedId for this entity.
            if (m_descriptor.hasEmbeddedIdAttribute()) {
                m_validator.throwMultipleEmbeddedIdsFound(getJavaClass(), getAttributeName(), m_descriptor.getEmbeddedIdAttributeName());
            } 
            
            // Check if we already processed an Id or IdClass.
            if (m_descriptor.hasPrimaryKeyFields()) {
                m_validator.throwEmbeddedIdAndIdFound(getJavaClass(), getAttributeName(), m_descriptor.getIdAttributeName());
            }
            
            // Set the PK class.
            m_descriptor.setPKClass(getReferenceClass());
            
            // Store the embeddedId attribute name.
            m_descriptor.setEmbeddedIdAttributeName(getAttributeName());
        }
        
        // Process the embeddable mapping specifics.
        super.process();
            
        // Add the fields from the embeddable as primary keys on the owning
        // metadata descriptor.
        for (DatabaseField field : m_idFields.values()) {
            m_descriptor.addPrimaryKeyField(field);
        }
    }
    
    /**
     * INTERNAL: (Override from EmbeddedAccesor)
     * 
     * Process an @AttributeOverride or attribute-override element for an 
     * embedded object, that is, an aggregate object mapping in TopLink.
	 */
	protected void processAttributeOverride(AggregateObjectMapping mapping, MetadataColumn column) {
        super.processAttributeOverride(mapping, column);
        
        // Update our primary key field with the attribute override field.
        // The super class with ensure the correct field is on the metadata
        // column.
        DatabaseField field = column.getDatabaseField();
        field.setTableName(m_descriptor.getPrimaryTableName());
        m_idFields.put(column.getAttributeName(), field);
	}
    
    /**
     * INTERNAL: (Override from EmbeddedAccesor)
     *
     * Process the embeddable class and gather up our 'original' collection of
     * primary key fields. They are original because they may change with the
     * specification of an attribute override.
     */
    protected MetadataDescriptor processEmbeddableClass() {
        MetadataDescriptor embeddableDescriptor = super.processEmbeddableClass();
        
        // After processing the embeddable class, we need to gather our 
        // primary keys fields that we will eventually set on the owning 
        // descriptor metadata.
        if (isEmbeddedId() && ! m_descriptor.ignoreIDs()) {
            if (embeddableDescriptor.getMappings().isEmpty()) {
                String accessType = embeddableDescriptor.usesPropertyAccess() ? AccessType.PROPERTY.name() : AccessType.FIELD.name();
                m_validator.throwEmbeddedIdHasNoAttributes(m_descriptor.getJavaClass(), embeddableDescriptor.getJavaClass(), accessType);
            }

            for (DatabaseMapping mapping : embeddableDescriptor.getMappings()) {
                DatabaseField field = (DatabaseField) mapping.getField().clone();
                field.setTableName(m_descriptor.getPrimaryTableName());
                m_idFields.put(mapping.getAttributeName(), field);
            }
        }
        
        return embeddableDescriptor;
    }
}
