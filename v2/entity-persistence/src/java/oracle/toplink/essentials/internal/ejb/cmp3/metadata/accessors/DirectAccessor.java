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

import java.io.Serializable;

import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.Lob;
import javax.persistence.Temporal;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataConstants;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataHelper;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataLogger;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataColumn;

import oracle.toplink.essentials.internal.helper.DatabaseField;
import oracle.toplink.essentials.internal.helper.Helper;

import oracle.toplink.essentials.mappings.DatabaseMapping;
import oracle.toplink.essentials.mappings.converters.Converter;
import oracle.toplink.essentials.mappings.converters.EnumTypeConverter;
import oracle.toplink.essentials.mappings.converters.SerializedObjectConverter;
import oracle.toplink.essentials.mappings.converters.TypeConversionConverter;
import oracle.toplink.essentials.mappings.foundation.AbstractDirectMapping;

/**
 * A direct accessor.
 * 
 * Subclasses: BasicAccessor, XMLBasicAccessor, BasicCollectionAccessor,
 * BasicMapAccessor.
 * 
 * @author Guy Pelletier
 * @since TopLink 11g
 */
public abstract class DirectAccessor extends NonRelationshipAccessor {
    /**
     * INTERNAL:
     */
    public DirectAccessor(MetadataAccessibleObject accessibleObject, ClassAccessor classAccessor) {
        super(accessibleObject, classAccessor);
    }
    
    /**
     * INTERNAL:
     * This is used to return the column for a BasicAccessor. In the case
     * of a BasicCollectionAccessor or BasicMapAccessor, this method should
     * return the value column.
     * 
     * See BasicMapAccessor for processing on the key column.
     */
    protected abstract MetadataColumn getColumn(String loggingCtx);
    
    /**
     * INTERNAL:
     * Process column details from an @Column or column element into a 
     * MetadataColumn and return it. This will set correct metadata and log 
     * defaulting messages to the user. It also looks for attribute overrides.
     * 
     * This method will call getColumn() which assumes the subclasses will
     * return the appropriate MetadataColumn to process based on the context
     * provided.
     * 
     * @See BasicCollectionAccessor and BasicMapAccessor.
     */
    public DatabaseField getDatabaseField(String loggingCtx) {
        // Check if we have an attribute override first, otherwise process for 
        // a column (ignoring if for a key column on a basic map)
        MetadataColumn column;
        if (m_descriptor.hasAttributeOverrideFor(getAttributeName())) {
            column = m_descriptor.getAttributeOverrideFor(getAttributeName());
        } else {
            column = getColumn(loggingCtx);
        }
        
        // Get the actual database field and apply any defaults.
        DatabaseField field = column.getDatabaseField();
        
        // Set the correct field name, defaulting and logging when necessary.
        String defaultName = column.getUpperCaseAttributeName();
        field.setName(getName(field.getName(), defaultName, loggingCtx));
                    
        return field;
    }
    
    /**
     * INTERNAL: (Overridden in XMLBasicAccessor)
     */
     public String getEnumeratedType() {
        Enumerated enumerated = getAnnotation(Enumerated.class);
        
        if (enumerated == null) {
            return EnumType.ORDINAL.name();
        } else {
            return enumerated.value().name();
        }
     }
     
    /**
     * INTERNAL:
     * 
     * Return the temporal type for this accessor. Assumes there is a @Temporal.
     */
    public String getTemporalType() {
        Temporal temporal = getAnnotation(Temporal.class);
        return temporal.value().name();
    }
    
    /**
     * INTERNAL: (Overridden in XMLBasicAccessor)
     * 
	 * Return true if this accessor has a @Enumerated.
     */
	public boolean hasEnumerated() {
		return isAnnotationPresent(Enumerated.class);
    }
    
    /**
     * INTERNAL: (Overridden in XMLBasicAccessor)
     * 
	 * Return true if this accessor has a @Lob.
     */
	public boolean hasLob() {
		return isAnnotationPresent(Lob.class);
    }
    
    /**
     * INTERNAL: (Overridden in XMLBasicAccessor)
     * 
     * Return true if this accessor has a @Temporal.
     */
	public boolean hasTemporal() {
        return isAnnotationPresent(Temporal.class);
    }

    /**
     * INTERNAL:
     * 
     * Return true if this represents an enum type mapping. Will return true
     * if the accessor's reference class is an enum or if a @Enumerated exists.
     */
    public boolean isEnumerated() {
        return hasEnumerated() || MetadataHelper.isValidEnumeratedType(getReferenceClass());
    }
    
    /**
     * INTERNAL:
     * 
     * Return true if this accessor represents a BLOB/CLOB mapping.
     */
	public boolean isLob() {
        return hasLob();
    }
    
    /**
     * INTERNAL:
     * 
     * Return true if this accessor represents a serialized mapping.
     */
	public boolean isSerialized() {
        return MetadataHelper.isValidSerializedType(getReferenceClass());
    }
    
    /**
     * INTERNAL: (Overridden in BasicMapAccessor)
     * 
     * Return true if this represents a temporal type mapping. Will return true
     * if the accessor's reference class is a temporal type or if a @Temporal 
     * exists.
     */
	public boolean isTemporal() {
        return hasTemporal() || MetadataHelper.isValidTemporalType(getReferenceClass());
    }
    
    /**
     * INTERNAL: (Overridden in BasicAccessor and BasicMapAccessor)
     * 
     * Process an @Enumerated. The method may still be called if no @Enumerated
     * has been specified but the accessor's reference class is a valid 
     * enumerated type.
     */
    protected void processEnumerated(DatabaseMapping mapping) {
        // If this accessor is tagged as an enumerated type, validate the
        // reference class.
        if (hasEnumerated()) {
            if (! MetadataHelper.isValidEnumeratedType(getReferenceClass())) {
                m_validator.throwInvalidTypeForEnumeratedAttribute(getJavaClass(), mapping.getAttributeName(), getReferenceClass());
            }
        }
        
        // Create an EnumTypeConverter and set it on the mapping.
        setConverter(mapping, new EnumTypeConverter(mapping, getReferenceClass(), getEnumeratedType().equals(EnumType.ORDINAL.name())));
    }
    
    /**
     * INTERNAL:
     * 
     * Process an @Enumerated, @Lob or @Temporal annotation. Will default
     * a serialized converter if necessary.
     */
    protected void processJPAConverters(DatabaseMapping mapping) {
        // Check for an enum first since it will fall into a serializable 
        // mapping otherwise (Enums are serialized)
        if (isEnumerated()) {
            processEnumerated(mapping);
        } else if (isLob()) {
            processLob(mapping);
        } else if (isTemporal()) {
            processTemporal(mapping);
            //gf 1637: converter for Temporal returns false for isMutable, needs to be overriden
            ((AbstractDirectMapping)mapping).setIsMutable(true);
        } else if (isSerialized()) {
            processSerialized(mapping);
        } else if (MetadataHelper.isValidDateType(this.getReferenceClass())){
            //gf 1637: override directmapping ismutable 
            ((AbstractDirectMapping)mapping).setIsMutable(true);
        }
    }
    
    /**
     * INTERNAL: (Overridden in BasicAccessor)
     * 
     * Process a @Lob or lob sub-element. The lob must be specified to process 
     * and create a lob type mapping.
     */
    protected void processLob(DatabaseMapping mapping) {
        // Set the field classification type on the mapping based on the
        // referenceClass type.
        if (MetadataHelper.isValidClobType(getReferenceClass())) {
            setFieldClassification(mapping, java.sql.Clob.class);   
            setConverter(mapping, new TypeConversionConverter(mapping));
        } else if (MetadataHelper.isValidBlobType(getReferenceClass())) {
            setFieldClassification(mapping, java.sql.Blob.class);
            setConverter(mapping, new TypeConversionConverter(mapping));
        } else if (Helper.classImplementsInterface(getReferenceClass(), Serializable.class)) {
            setFieldClassification(mapping, java.sql.Blob.class);
            setConverter(mapping, new SerializedObjectConverter(mapping));
        } else {
            // The referenceClass is neither a valid BLOB or CLOB attribute.   
            m_validator.throwInvalidTypeForLOBAttribute(getJavaClass(), mapping.getAttributeName(), getReferenceClass());
        }
    }
 
    /**
     * INTERNAL:
     * 
     * Process a converter for the given mapping. Will look for a converter
     * name from a @Convert specified on this accessor.
     */
    protected void processMappingConverter(DatabaseMapping mapping) {
        processJPAConverters(mapping);
    }
    
    /**
     * INTERNAL:
     * 
     * Process a potential serializable attribute. If the class implements 
     * the Serializable interface then set a SerializedObjectConverter on 
     * the mapping.
     */
    protected void processSerialized(DatabaseMapping mapping) {
        if (Helper.classImplementsInterface(getReferenceClass(), Serializable.class)) {
            SerializedObjectConverter converter = new SerializedObjectConverter(mapping);
            setConverter(mapping, converter);
        } else {
            m_validator.throwInvalidTypeForSerializedAttribute(getJavaClass(), mapping.getAttributeName(), getReferenceClass());
        }
    }
    
    /**
     * INTERNAL:
     * 
     * Process a temporal type accessor.
     */
    protected void processTemporal(DatabaseMapping mapping) {
        if (hasTemporal()) {
            if (MetadataHelper.isValidTemporalType(getReferenceClass())) {
                // Set a TypeConversionConverter on the mapping.
                setFieldClassification(mapping, MetadataHelper.getFieldClassification(getTemporalType()));
                setConverter(mapping, new TypeConversionConverter(mapping));
            } else {
                m_validator.throwInvalidTypeForTemporalAttribute(getJavaClass(), getAttributeName(), getReferenceClass());
            }    
        } else {
            m_validator.throwNoTemporalTypeSpecified(getJavaClass(), getAttributeName());
        }
    }
    
    /**
     * INTERNAL:
     */
    public abstract void setConverter(DatabaseMapping mapping, Converter converter);
    
    /**
     * INTERNAL:
     */
    public abstract void setFieldClassification(DatabaseMapping mapping, Class classification);
}
