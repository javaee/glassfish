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

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;

import static oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.EmbeddedAccessor.AccessType.MIXED;
import static oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.EmbeddedAccessor.AccessType.PROPERTY;
import static oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.EmbeddedAccessor.AccessType.FIELD;
import static oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.EmbeddedAccessor.AccessType.UNDEFINED;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataClass;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataColumn;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataDescriptor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataHelper;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLHelper;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLConstants;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.accessors.XMLClassAccessor;
import oracle.toplink.essentials.internal.helper.DatabaseField;

import oracle.toplink.essentials.mappings.AggregateObjectMapping;
import oracle.toplink.essentials.mappings.DatabaseMapping;
import oracle.toplink.essentials.mappings.OneToOneMapping;

import org.w3c.dom.Node;

/**
 * An embedded relationship accessor.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class EmbeddedAccessor extends MetadataAccessor {
    enum AccessType {FIELD, PROPERTY, UNDEFINED, MIXED};

    /**
     * INTERNAL:
     */
    public EmbeddedAccessor(MetadataAccessibleObject accessibleObject, ClassAccessor classAccessor) {
        super(accessibleObject, classAccessor);
    }
    
    /**
     * INTERNAL:
     * 
     * This method is responsible for configuring the right MetadataAccessor
     * object for processing of the Embeddable class represented by the
     * embeddableDescriptor that is passed in. This methood associates the
     * accessor with the descriptor as well.
     *
     * @param embeddableDescriptor descriptor for the Embeddable
     * @return a MetadataAccessor that will be used to read metadata for this class
     * @throws ValidationException if the embeddable class is neither
     * annotated as Embeddable nor specified as embeddable in XML.
     */
    protected ClassAccessor buildAccessor(MetadataDescriptor embeddableDescriptor) {
        ClassAccessor embeddableAccessor = null;

        Class embeddableClass = embeddableDescriptor.getJavaClass();

        if (m_project.hasEmbeddable(embeddableClass)) {
            Node node = m_project.getEmbeddableNode(embeddableClass);
            XMLHelper helper = m_project.getEmbeddableHelper(embeddableClass);
            embeddableAccessor = new XMLClassAccessor(new MetadataClass(embeddableClass), node, helper, m_processor, embeddableDescriptor);
        } else if (MetadataHelper.isAnnotationPresent(Embeddable.class, embeddableClass, embeddableDescriptor)) {
            embeddableAccessor = new ClassAccessor(new MetadataClass(embeddableClass), m_processor, embeddableDescriptor);
        } else {
            m_validator.throwInvalidEmbeddedAttribute(getJavaClass(), m_accessibleObject.getName(), embeddableClass);
        }

        // associate the accessor to the descriptor (as per Javadoc)
        embeddableDescriptor.setClassAccessor(embeddableAccessor);
        return embeddableAccessor;
    }
    
    /**
     * INTERNAL:
     * 
     * This method computes access-type based on placement of annotations.
     */
    protected AccessType computeAccessTypeFromAnnotation(MetadataDescriptor descriptor) {
        Class javaClass = descriptor.getJavaClass();
        boolean fieldAccess = MetadataHelper.havePersistenceAnnotationsDefined(MetadataHelper.getFields(javaClass));
        boolean propertyAccess = MetadataHelper.havePersistenceAnnotationsDefined(MetadataHelper.getMethods(javaClass));
        
        AccessType accessType = UNDEFINED;

        if (fieldAccess && propertyAccess) {
            accessType = MIXED;
        } else if (fieldAccess) {
            accessType = FIELD;
        } else if (propertyAccess) {
            accessType = PROPERTY;
        }

        return accessType;
    }
    
    /**
     * INTERNAL:
     * 
     * This method computes access-type based on metadata specified in mapping
     * XML file. If there is a default value for access is specified in
     * <persistence-unit-defaults> or <entity-mappings> and either there is
     * no <embeddable> for the given class or there is <access> specified
     * in the <embeddable>, the default value is used.
     */
    protected AccessType computeAccessTypeFromXML(MetadataDescriptor descriptor) {
        // step #1: what's PU level default
        String access = m_project.getPersistenceUnit() != null ? m_project.getPersistenceUnit().getAccess() : null;

        // step #2: what's specified in <embeddable> element

        Class javaClass = descriptor.getJavaClass();

        if (m_project.hasEmbeddable(javaClass)) {
            XMLHelper helper = m_project.getEmbeddableHelper(javaClass);
            Node node = m_project.getEmbeddableNode(javaClass);
            // override pu-level value by <embeddable> level if any
            // Note, we pass the puLevel value as the default value
            access = helper.getNodeValue(node, XMLConstants.ATT_ACCESS, access);
        }

        AccessType accessType = UNDEFINED;

        if (access != null && access.length() != 0) {
            accessType = AccessType.valueOf(access);
        }

        return accessType;
    }
    
    /**
     * INTERNAL:
     * 
     * This method is responsible for determining the access-type for an
     * Embeddable class represented by emDescr that is passed to
     * this method. This method should *not* be called more than once as this is
     * quite expensive. Now the rules:
     *
     * Rule 1: In the *absence* of metadata in embeddable class, access-type of
     * an embeddable is determined by the access-type of the enclosing entity.
     *
     * Rule 2: In the presence of metadata in embeddable class, access-type of
     * an embeddable is determined using that metadata. This allows sharing
     * of the embeddable in entities with conflicting access-types.
     *
     * Rule 3: It is an error to use a *metadata-less* embeddable class in
     * entities with conflicting access-types as that might result in
     * different database mapping for the same embeddable class.
     *
     * Rule 4: It is an error if metadata-complete == false, and
     * metadata is present *both* in annotations and XML, and
     * access-type as determined by each of them is *not* same.
     *
     * Rule 5: It is an error if *both* fields and properties of an embeddable
     * class are annotated and metadata-complete == false.
     *
     * @param emDesc descriptor for the Embeddable class
     */
    protected AccessType determineAccessTypeOfEmbedded(MetadataDescriptor emDesc) {
        final AccessType entityAccessType = m_descriptor.usesPropertyAccess() ? PROPERTY : FIELD;
        AccessType accessType = UNDEFINED;
        final boolean metadataComplete =emDesc.ignoreAnnotations();
        final AccessType accessTypeUsingAnnotation = computeAccessTypeFromAnnotation(emDesc);
        final AccessType accessTypeUsingXML = computeAccessTypeFromXML(emDesc);

        if (metadataComplete) {
            // metadata-complete is true, then use XML access-type if defined,
            // else use enclosing entity's access-type.
            accessType = accessTypeUsingXML != UNDEFINED ? accessTypeUsingXML : entityAccessType;
        } else {// metadata-complete is false
            if (accessTypeUsingAnnotation == UNDEFINED && accessTypeUsingAnnotation == UNDEFINED) {
                // metadata is absent, so we use enclosing entity's access-type
                accessType = entityAccessType;
            } else if (accessTypeUsingXML == UNDEFINED && accessTypeUsingAnnotation != UNDEFINED) {
                // annotation is present in embeddable class
                accessType = accessTypeUsingAnnotation;

                if (accessType == MIXED) {
                    m_validator.throwBothFieldsAndPropertiesAnnotatedException(emDesc.getJavaClass());
                }
            } else if (accessTypeUsingAnnotation == UNDEFINED && accessTypeUsingXML != UNDEFINED) {
                // access is defined using XML for embeddable class
                accessType = accessTypeUsingXML;
            } else if (accessTypeUsingAnnotation == accessTypeUsingXML) {
                // annotation is present as well as access is defined using XML
                // and they are same. So use it.
                accessType = accessTypeUsingAnnotation;
            } else {
                // annotation is present as well as access is defined using XML
                // and they are different. So report an exception.
                m_validator.throwIncorrectOverridingOfAccessType(emDesc.getJavaClass(), accessTypeUsingXML.toString(), accessTypeUsingAnnotation.toString());
            }
        }

        // we have taken every precaution to make sure that either an embeddable
        // has a well defined access-type or a suitable ValidationException
        // is thrown.
        assert(accessType != UNDEFINED && accessType != MIXED);

        return accessType;
    }
    
    /**
     * INTERNAL:
     */
	public boolean isEmbedded() {
        return true;
    }
    
    /**
     * INTERNAL:
     * 
     * This method is used to decide if annotations should be ignored or not for
     * the given embeddable class.
     */
    protected boolean isMetadataComplete(MetadataDescriptor emDesc) {
        final Class emClass = emDesc.getJavaClass();

        boolean metadataComplete = m_project.getPersistenceUnit() != null ? m_project.getPersistenceUnit().isMetadataComplete() : false;

        if (!metadataComplete) {
            // check <embeddable>
            if (m_project.hasEmbeddable(emClass)) {
                XMLHelper helper = m_project.getEmbeddableHelper(emClass);
                Node node = m_project.getEmbeddableNode(emClass);
                // check if metadata-complete at <embeddable> level
                metadataComplete = helper.getNodeValue(node, XMLConstants.ATT_METADATA_COMPLETE, false);
            }
        }

        return metadataComplete;
    }
    
    /**
     * INTERNAL:
     * 
     * This method is used to decide if a class metadata or not.
     */
    protected boolean isMetadataPresent(MetadataDescriptor descriptor) {
        AccessType annotAccessType = computeAccessTypeFromAnnotation(descriptor);
        AccessType xmlAccessType = computeAccessTypeFromXML(descriptor);

        return annotAccessType != UNDEFINED || xmlAccessType != UNDEFINED;
    }
    
    /**
     * INTERNAL: (Overridden in EmbeddedIdAccessor)
     * 
     * Process an @Embedded or embedded element.
     */    
    public void process() {
        // Tell the Embeddable class to process itself
        MetadataDescriptor referenceDescriptor = processEmbeddableClass();
        
        // Store this descriptor metadata. It may be needed again later on to
        // look up a mappedBy attribute.
        m_descriptor.addAggregateDescriptor(referenceDescriptor);
        
        if (m_descriptor.hasMappingForAttributeName(getAttributeName())) {
            // XML/Annotation merging. XML wins, ignore annotations.
            m_logger.logWarningMessage(m_logger.IGNORE_MAPPING, m_descriptor, this);
        } else {
            // Create an aggregate mapping and do the rest of the work.
            AggregateObjectMapping mapping = new AggregateObjectMapping();
            mapping.setIsReadOnly(false);
            mapping.setIsNullAllowed(true);
            mapping.setReferenceClassName(getReferenceClassName());
            mapping.setAttributeName(getAttributeName());    
        
            // Will check for PROPERTY access
            setAccessorMethods(mapping);
        
            // Process attribute overrides.
            processAttributeOverrides(mapping);
            
            // Process association overrides.
            processAssociationOverrides(mapping);
        
            // Add the mapping to the descriptor and we are done.
            m_descriptor.addMapping(mapping);
        }
    }
    
    /**
     * INTERNAL:
     * 
     * Process an @AssociationOverride for an embedded object, that is, an 
     * aggregate object mapping in TopLink. 
     * 
     * This functionality is not supported in XML, hence why this method is 
     * defined here instead of on MetadataProcessor.
     * 
     * Also this functionality is currently optional in the EJB 3.0 spec, but
     * since TopLink can handle it, it is implemented and assumes the user has
     * properly configured its use since it will fail silently.
	 */
	protected void processAssociationOverride(AssociationOverride associationOverride, AggregateObjectMapping aggregateMapping) {
        MetadataDescriptor aggregateDescriptor = getReferenceDescriptor();
        
        // AssociationOverride.name(), the name of the attribute we want to
        // override.
        String name = associationOverride.name();
        DatabaseMapping mapping = aggregateDescriptor.getMappingForAttributeName(name);
        
        if (mapping != null && mapping.isOneToOneMapping()) {
            int index = 0;
            
            for (JoinColumn joinColumn : associationOverride.joinColumns()) {
                // We can't change the mapping from the aggregate descriptor
                // so we have to add field name translations. This needs to be
                // tested since I am not entirely sure if this will acutally
                // work.
                // In composite primary key case, how do we association the
                // foreign keys? Right now we assume the association overrides
                // are specified in the same order as the original joinColumns,
                // therefore in the same order the foreign keys were added to
                // the mapping.
                DatabaseField fkField = (DatabaseField) ((OneToOneMapping) mapping).getForeignKeyFields().elementAt(index++);
                aggregateMapping.addFieldNameTranslation(joinColumn.name(), fkField.getName());
            }   
        } else {
            // For now fail silently.
        }
	}
    
    /**
     * INTERNAL:
     * 
     * Process an @AssociationOverrides for an embedded object, that is, an
     * aggregate object mapping in TopLink. 
     * 
     * It will also look for an @AssociationOverride.
     */
    protected void processAssociationOverrides(AggregateObjectMapping mapping) {
        // Look for an @AssociationOverrides.
        AssociationOverrides associationOverrides = getAnnotation(AssociationOverrides.class);
        if (associationOverrides != null) {
            for (AssociationOverride associationOverride : associationOverrides.value()) {
                processAssociationOverride(associationOverride, mapping);
            }
        }
        
        // Look for an @AssociationOverride.
        AssociationOverride associationOverride = getAnnotation(AssociationOverride.class);	
        if (associationOverride != null) {
            processAssociationOverride(associationOverride, mapping);
        }
    }
    
    /**
     * INTERNAL: (Overridden in EmbeddedIdAccessor)
     * 
     * Process an @AttributeOverride or attribute-override element for an 
     * embedded object, that is, an aggregate object mapping in TopLink.
	 */
	protected void processAttributeOverride(AggregateObjectMapping mapping, MetadataColumn column) {
        String attributeName = column.getAttributeName();
        
        // Set the attribute name on the aggregate.
        DatabaseMapping aggregateMapping = getReferenceDescriptor().getMappingForAttributeName(attributeName);
        
        if (aggregateMapping == null) {
            m_validator.throwInvalidEmbeddableAttribute(getJavaClass(), mapping.getAttributeName(), getReferenceDescriptor().getJavaClass(), attributeName);
        }
        
        // A sub-class to a mapped superclass may override an embedded attribute 
        // override.
        if (m_descriptor.hasAttributeOverrideFor(attributeName)) {
            // Update the field on this metadata column. We do that so that
            // an embedded id can associate the correct id fields.
            column.setDatabaseField(m_descriptor.getAttributeOverrideFor(attributeName).getDatabaseField());
        } 
        
        mapping.addFieldNameTranslation(column.getDatabaseField().getQualifiedName(), aggregateMapping.getField().getName());
	}
    
    /**
     * INTERNAL: (Overridden in XMLEmbeddedAccessor and XMLEmbeddedIdAccessor)
     * 
     * Process an @AttributeOverrides for an embedded object, that is, an
     * aggregate object mapping in TopLink. 
     * 
     * It will also look for an @AttributeOverride.
     */
    protected void processAttributeOverrides(AggregateObjectMapping mapping) {
        // Look for an @AttributeOverrides.
        AttributeOverrides attributeOverrides = getAnnotation(AttributeOverrides.class);
        
        if (attributeOverrides != null) {
            for (AttributeOverride attributeOverride : attributeOverrides.value()) {
                processAttributeOverride(mapping, new MetadataColumn(attributeOverride.column(), attributeOverride.name(), getAnnotatedElement()));
            }
        }
        
        // Look for an @AttributeOverride.
        AttributeOverride attributeOverride = getAnnotation(AttributeOverride.class);	
        if (attributeOverride != null) {
            processAttributeOverride(mapping, new MetadataColumn(attributeOverride.column(), attributeOverride.name(), getAnnotatedElement()));
        }
    }
    
    /**
     * INTERNAL: (Overridden in EmbeddedIdAccessor)
     * 
     * This method processes an embeddable class, if we have not processed it 
     * yet. The reason for lazy processing of embeddable class is because of 
     * rules  governing access-type of embeddable. See GlassFish issue #831 for 
     * more details.
     *
     * Be careful while changing order of processing.
     */
    protected MetadataDescriptor processEmbeddableClass() {
        final Class embeddableClass = getReferenceClass();
        MetadataDescriptor embeddableDescriptor = null;

        try {
            embeddableDescriptor = m_project.getDescriptor(embeddableClass);
        } catch (Exception exception) {
            // expected as we do lazy processing of embeddables.
        }

        if (embeddableDescriptor == null) {
            // The embeddable class is not yet processed, so process it now.
            embeddableDescriptor = new MetadataDescriptor(embeddableClass);
            // adding to projects sets up appropriate persistence-unit-defaults
            m_project.addDescriptor(embeddableDescriptor);
            embeddableDescriptor.setIgnoreAnnotations(isMetadataComplete(embeddableDescriptor));
            AccessType accessType = determineAccessTypeOfEmbedded(embeddableDescriptor);
            embeddableDescriptor.setUsesPropertyAccess(accessType == PROPERTY ? true : false);
            ClassAccessor embeddableAccessor = buildAccessor(embeddableDescriptor);
            embeddableAccessor.process();
            embeddableAccessor.setIsProcessed();
        } else {
            // We have already processed this embeddable class. let's validate 
            // that it is not used in entities with conflicting access type. 
            // Conflicting access-type is not allowed when there is no metadata 
            // in the embeddable class.
            if (!isMetadataPresent(embeddableDescriptor)) {
                boolean embeddableUsesPropertyAccess = embeddableDescriptor.usesPropertyAccess();
                boolean entityUsesPropertyAccess = m_descriptor.usesPropertyAccess();

                if (embeddableUsesPropertyAccess != entityUsesPropertyAccess) {
                    m_validator.throwConflictingAccessTypeInEmbeddable(embeddableClass);
                }
            }
        }
        
        return embeddableDescriptor;
    }
}
