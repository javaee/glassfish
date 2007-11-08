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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataConstants;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataHelper;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataColumn;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.sequencing.MetadataGeneratedValue;

import oracle.toplink.essentials.internal.helper.DatabaseField;

import oracle.toplink.essentials.mappings.DatabaseMapping;
import oracle.toplink.essentials.mappings.DirectToFieldMapping;

import oracle.toplink.essentials.mappings.converters.Converter;

/**
 * A relational accessor. A @Basic annotation may or may not be present on the
 * accessible object.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class BasicAccessor extends DirectAccessor {
    private Basic m_basic;
    
    /**
     * INTERNAL:
     */
    public BasicAccessor(MetadataAccessibleObject accessibleObject, ClassAccessor classAccessor) {
        super(accessibleObject, classAccessor);
        m_basic = getAnnotation(Basic.class);
    }
    
    /**
     * INTERNAL: (Overridden in XMLBasicAccessor)
     * 
     * Build a metadata column.
     */
    protected MetadataColumn getColumn(String loggingCtx) {
        Column column = getAnnotation(Column.class);
        return new MetadataColumn(column, this);
    }
    
    /**
     * INTERNAL: (Overridden in XMLBasicAccessor)
     */
    public String getFetchType() {
        return (m_basic == null) ? MetadataConstants.EAGER : m_basic.fetch().name();
    }
    
    /**
     * INTERNAL: (Override from MetadataAccessor)
     */
     public boolean isBasic() {
     	return true;
     }
     
    /**
     * INTERNAL: (Overridden in XMLBasicAccessor)
     * 
     * Return true if this accessor represents an id field.
     */
	public boolean isId() {
        return isAnnotationPresent(Id.class);
    }
    
    /**
     * INTERNAL: (Overridden in XMLBasicAccessor)
     */
	public boolean isOptional() {
        return (m_basic == null) ? true : m_basic.optional();
    }
    
    /**
     * INTERNAL: (Overridden in XMLBasicAccessor)
     * 
	 * Return true if this accessor represents an optimistic locking field.
     */
	public boolean isVersion() {
        return isAnnotationPresent(Version.class);
    }
    
    /**
     * INTERNAL:
     * 
     * Process a basic accessor.
     */
    public void process() {
    	// Process the @Column or column element if there is one.
        DatabaseField field = getDatabaseField(m_logger.COLUMN);
        
        // Make sure there is a table name on the field.
        if (field.getTableName().equals("")) {
            field.setTableName(m_descriptor.getPrimaryTableName());
        }
        
        // Process an @Version or version element if there is one.
        if (isVersion()) {
            if (m_descriptor.usesOptimisticLocking()) {
                // Ignore the version locking if it is already set.
                m_logger.logWarningMessage(m_logger.IGNORE_VERSION_LOCKING, this);
            } else {
                processVersion(field);
            }
        } else if (isId()) {
            // Process an @Id or id element.
            processId(field);
        }
                
        if (m_descriptor.hasMappingForAttributeName(getAttributeName())) {
            // Ignore the mapping if one already exists for it.
            m_logger.logWarningMessage(m_logger.IGNORE_MAPPING, this);
        } else {
            // Process a DirectToFieldMapping, that is a Basic that could
            // be used in conjunction with a Lob, Temporal, Enumerated
            // or inferred to be used with a serialized mapping.
            processDirectToFieldMapping(field);
        }
    }
    
    /**
     * INTERNAL:
     * 
     * Process a Serialized or Basic into a DirectToFieldMapping. If neither 
     * is found a DirectToFieldMapping is created regardless.
     */
    protected void processDirectToFieldMapping(DatabaseField field) {
        DirectToFieldMapping mapping = new DirectToFieldMapping();
        mapping.setField(field);
        mapping.setIsReadOnly(field.isReadOnly());
        mapping.setAttributeName(getAttributeName());
		mapping.setIsOptional(isOptional());
        
        if (usesIndirection()) {
            m_logger.logWarningMessage(m_logger.IGNORE_BASIC_FETCH_LAZY, this);
        }
        
        // Will check for PROPERTY access
        setAccessorMethods(mapping);
        
        // Process a converter for this mapping. We will look for a @Convert
        // first. If none is found then we'll look for a JPA converter, that 
        // is, @Enumerated, @Lob and @Temporal. With everything falling into 
        // a serialized mapping if no converter whatsoever is found.
        processMappingConverter(mapping);
        
        // Add the mapping to the descriptor.
        m_descriptor.addMapping(mapping);
    }
    
    /**
     * INTERNAL: (Override from DirectAccessor)
     * 
     * Process an @Enumerated. The method may still be called if no @Enumerated
     * has been specified but the accessor's reference class is a valid 
     * enumerated type.
     */
    protected void processEnumerated(DatabaseMapping mapping) {
        // If the raw class is a collection or map (with generics or not), we 
        // don't want to put a TypeConversionConverter on the mapping. Instead, 
        // we will want a serialized converter. For example, we could have 
        // an EnumSet<Enum> relation type.
        if (MetadataHelper.isCollectionClass(getReferenceClass()) || MetadataHelper.isMapClass(getReferenceClass())) {
            processSerialized(mapping);
        } else {
            super.processEnumerated(mapping);
        }
    }
    
    /**
     * INTERNAL: (Overridden In XMLBasicAccessor)
     * 
     * Process a @GeneratedValue.
     */
    protected void processGeneratedValue(DatabaseField field) {
        GeneratedValue generatedValue = getAnnotation(GeneratedValue.class);
        
        if (generatedValue != null) {
            processGeneratedValue(new MetadataGeneratedValue(generatedValue), field);
        }
    }
    
    /**
     * INTERNAL:
     */
    protected void processGeneratedValue(MetadataGeneratedValue generatedValue, DatabaseField sequenceNumberField) {
        // Set the sequence number field on the descriptor.		
        DatabaseField existingSequenceNumberField = m_descriptor.getSequenceNumberField();
        
        if (existingSequenceNumberField == null) {
            m_descriptor.setSequenceNumberField(sequenceNumberField);
            getProject().addGeneratedValue(generatedValue, getJavaClass());
        } else {
            m_validator.throwOnlyOneGeneratedValueIsAllowed(getJavaClass(), existingSequenceNumberField.getQualifiedName(), sequenceNumberField.getQualifiedName());
        }
    }
    
    /**
     * INTERNAL:
     * 
     * Process an @Id or id element if there is one.
     */
    protected void processId(DatabaseField field) {
    	if (m_descriptor.ignoreIDs()) {
            // Project XML merging. XML wins, ignore annotations/orm xml.
            m_logger.logWarningMessage(m_logger.IGNORE_PRIMARY_KEY, this);
        } else {
            String attributeName = getAttributeName();
            
            if (m_descriptor.hasEmbeddedIdAttribute()) {
                // We found both an Id and an EmbeddedId, throw an exception.
                m_validator.throwEmbeddedIdAndIdFound(getJavaClass(), m_descriptor.getEmbeddedIdAttributeName(), attributeName);
            }
            
            // If this entity has a pk class, we need to validate our ids. 
            m_descriptor.validatePKClassId(attributeName, getReferenceClass());
        
            // Store the Id attribute name. Used with validation and OrderBy.
            m_descriptor.addIdAttributeName(attributeName);

            // Add the primary key field to the descriptor.            
            m_descriptor.addPrimaryKeyField(field);
	
            // Process the generated value for this id.
            processGeneratedValue(field);
            
            // Process a table generator.
            processTableGenerator();
            
            // Process a sequence generator.
            processSequenceGenerator();
        }
    }
    
    /**
     * INTERNAL: (Override from DirectAccessor)
     * 
     * Process a @Lob or lob sub-element. The lob must be specified to process 
     * and create a lob type mapping.
     */
    protected void processLob(DatabaseMapping mapping) {
        // If the raw class is a collection or map (with generics or not), we 
        // don't want to put a TypeConversionConverter on the mapping. Instead, 
        // we will want a serialized converter.
        if (MetadataHelper.isCollectionClass(getReferenceClass()) || MetadataHelper.isMapClass(getReferenceClass())) {
            setFieldClassification(mapping, java.sql.Blob.class);
            processSerialized(mapping);
        } else {
            super.processLob(mapping);
        }
    }
    
    /**
     * INTERNAL:
     */
    protected void processVersion(DatabaseField field) {
        Class lockType = getRawClass();
        field.setType(lockType);
        
        if (MetadataHelper.isValidVersionLockingType(lockType)) {
            m_descriptor.useVersionLockingPolicy(field);
        } else if (MetadataHelper.isValidTimstampVersionLockingType(lockType)) {
            m_descriptor.useTimestampLockingPolicy(field);
        } else {
            m_validator.throwInvalidTypeForVersionAttribute(getJavaClass(), getAttributeName(), lockType);
        }
    }
    
    /**
     * INTERNAL:
     */
    public void setConverter(DatabaseMapping mapping, Converter converter) {
        ((DirectToFieldMapping) mapping).setConverter(converter);
    }
    
    /**
     * INTERNAL:
     */
    public void setFieldClassification(DatabaseMapping mapping, Class classification) {
        ((DirectToFieldMapping) mapping).setFieldClassification(classification);
    }
}
