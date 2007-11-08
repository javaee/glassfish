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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.lang.annotation.Annotation;
import java.lang.Boolean;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

import javax.persistence.Column;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.ClassAccessor;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataClass;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataMethod;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataPrimaryKeyJoinColumn;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataPrimaryKeyJoinColumns;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.tables.MetadataTable;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataHelper;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataLogger;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataProject;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataValidator;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataConstants;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataProcessor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataDescriptor;

import oracle.toplink.essentials.internal.helper.ClassConstants;
import oracle.toplink.essentials.internal.helper.DatabaseField;
import oracle.toplink.essentials.internal.helper.Helper;

import oracle.toplink.essentials.internal.queryframework.CollectionContainerPolicy;

import oracle.toplink.essentials.mappings.CollectionMapping;
import oracle.toplink.essentials.mappings.DatabaseMapping;

/**
 * Top level metatata accessor.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public abstract class MetadataAccessor  {
    private boolean m_isProcessed;
    private Boolean m_isRelationship;
    
    protected MetadataLogger m_logger;
    protected MetadataProject m_project;
    protected MetadataProcessor m_processor;
    protected MetadataValidator m_validator;
    protected MetadataDescriptor m_descriptor;
    protected MetadataAccessibleObject m_accessibleObject;
    
    /**
     * INTERNAL:
     */
    public MetadataAccessor(MetadataAccessibleObject accessibleObject, ClassAccessor classAccessor) {
        this(accessibleObject, classAccessor.getProcessor(), classAccessor.getDescriptor());
    }
    
    /**
     * INTERNAL:
     */
    public MetadataAccessor(MetadataAccessibleObject accessibleObject, MetadataProcessor processor, MetadataDescriptor descriptor) {
        m_isProcessed = false;
        
        m_processor = processor;
        m_descriptor = descriptor;
        m_logger = processor.getLogger();
        m_project = processor.getProject();
        m_validator = processor.getValidator();
        
        m_accessibleObject = accessibleObject;
    }
    
    /**
     * INTERNAL:
     * Return the annotated element for this accessor.
     */
    public AnnotatedElement getAnnotatedElement() {
        return m_accessibleObject.getAnnotatedElement();
    }
    
    /**
     * INTERNAL:
     * Return the annotated element for this accessor.
     */
    protected <T extends Annotation> T getAnnotation(Class annotation) {
        return (T) getAnnotation(annotation, getAnnotatedElement());
    }
    
    /**
     * INTERNAL:
     * Return the annotated element for this accessor.
     */
    protected <T extends Annotation> T getAnnotation(Class annotation, AnnotatedElement annotatedElement) {
        return (T) MetadataHelper.getAnnotation(annotation, annotatedElement, m_descriptor);
    }
    
    /**
     * INTERNAL:
     * Return the attribute name for this accessor.
     */
    public String getAttributeName() {
        return m_accessibleObject.getAttributeName();
    }
    
    /**
     * INTERNAL:
     * Return the MetadataDescriptor for this accessor.
     */
    public MetadataDescriptor getDescriptor() {
        return m_descriptor;
    }
    
    /**
     * INTERNAL:
     * 
     * Subclasses that support processing a fetch type should override this 
     * method, otherwise a runtime development exception is thrown for those 
     * accessors who call this method and don't implement it themselves.
     */
    public String getFetchType() {
        throw new RuntimeException("Development exception. The accessor: [" + this + "] should not call the getFetchType method unless it overrides it.");
    }
        
    /**
     * INTERNAL: (Overridden in ClassAccessor)
     * Return the java class associated with this accessor's descriptor.
     */
    public Class getJavaClass() {
        return m_descriptor.getJavaClass();
    }
    
    /**
     * INTERNAL:
     * Return the java class that defines this accessor.
     */
    protected String getJavaClassName() {
        return getJavaClass().getName();
    }

    /**
     * INTERNAL:
     * Return the metadata validator.
     */
    public MetadataLogger getLogger() {
        return m_logger;
    }
    
    /**
     * INTERNAL:
     * Return the map key class from a generic Map type. If it is not generic,
     * then null is returned.
     */
    protected Class getMapKeyClass() {
        return m_accessibleObject.getMapKeyClass();
    }
    
    /**
     * INTERNAL:
     * Returns the name of this accessor. If it is a field, it will return 
     * the field name. For a method it will return the method name.
     */
    public String getName() {
        return m_accessibleObject.getName();
    }
    
    /**
     * INTERNAL:
     * Helper method to return a field name from a candidate field name and a 
     * default field name.
     * 
     * Requires the context from where this method is called to output the 
     * correct logging message when defaulting the field name.
     */
    protected String getName(DatabaseField field, String defaultName, String context) {
        return getName(field.getName(), defaultName, context);
    }
    
    /**
     * INTERNAL:
     * Helper method to return a field name from a candidate field name and a 
     * default field name.
     * 
     * Requires the context from where this method is called to output the 
     * correct logging message when defaulting the field name.
     *
     * In some cases, both the name and defaultName could be "" or null,
     * therefore, don't log any message and return name.
     */
    protected String getName(String name, String defaultName, String context) {
        // Check if a candidate was specified otherwise use the default.
        if (name != null && !name.equals("")) {
            return name;
        } else if (defaultName == null || defaultName.equals("")) {
            return "";
        } else {
            // Log the defaulting field name based on the given context.
            m_logger.logConfigMessage(context, getAnnotatedElement(), defaultName);
            return defaultName;
        }
	}
    
    /**
     * INTERNAL: (Overridden in XMLClassAccessor and XMLOneToOneAccessor)
     * Process the @PrimaryKeyJoinColumns and @PrimaryKeyJoinColumn.
     */    
    protected MetadataPrimaryKeyJoinColumns getPrimaryKeyJoinColumns(String sourceTableName, String targetTableName) {
        PrimaryKeyJoinColumn primaryKeyJoinColumn = getAnnotation(PrimaryKeyJoinColumn.class);
        PrimaryKeyJoinColumns primaryKeyJoinColumns = getAnnotation(PrimaryKeyJoinColumns.class);
        
        return new MetadataPrimaryKeyJoinColumns(primaryKeyJoinColumns, primaryKeyJoinColumn, sourceTableName, targetTableName);
    }
    
    /**
     * INTERNAL:
     * Return the MetadataProject.
     */
    public MetadataProject getProject() {
        return m_project;
    }
    
    /**
     * INTERNAL:
     * Return the MetadataProcessor.
     */
    public MetadataProcessor getProcessor() {
        return m_processor;
    }

    /**
     * INTERNAL:
     * Return the raw class for this accessor. 
     * Eg. For an accessor with a type of java.util.Collection<Employee>, this 
     * method will return java.util.Collection
     */
    public Class getRawClass() {
        return m_accessibleObject.getRawClass();   
    }
    
    /**
     * INTERNAL: (Overridden in CollectionAccessor and ObjectAccessor)
     * 
     * Return the reference class for this accessor. By default the reference
     * class is the raw class. Some accessors, namely relationship accessors,
     * will need to override this behavior to extract a reference class from
     * generics or a target entity specification.
     */
    public Class getReferenceClass() {
        return m_accessibleObject.getRawClass();
    }

    /**
     * INTERNAL:
     * 
     * Return the reference class name for this accessor.
     */
    public String getReferenceClassName() {
        return getReferenceClass().getName();
    }
    
    /**
     * INTERNAL:
     * Return the reference metadata descriptor for this accessor.
     */
    public MetadataDescriptor getReferenceDescriptor() {
        return m_project.getDescriptor(getReferenceClass());
    }
    
    /**
     * INTERNAL:
     * Return the relation type of this accessor.
     */
    protected Type getRelationType() {
        return m_accessibleObject.getRelationType();
    }
    
    /**
     * INTERNAL:
     * Returns the set method name of a method accessor. Note, this method
     * should not be called when processing field access.
     */
    protected String getSetMethodName() {
        return ((MetadataMethod) m_accessibleObject).getSetMethodName();
    }
    
    /**
     * INTERNAL:
     * Return the upper cased attribute name for this accessor. Used when
     * defaulting.
     */
    protected String getUpperCaseAttributeName() {
        return getAttributeName().toUpperCase();
    }
    
    /**
     * INTERNAL:
     * Return the upper case java class that defines this accessor.
     */
    protected String getUpperCaseShortJavaClassName() {
        return Helper.getShortClassName(getJavaClassName()).toUpperCase();
    }
    
    /**
     * INTERNAL:
     * Return the metadata validator.
     */
    public MetadataValidator getValidator() {
        return m_validator;
    }
    
    /**
     * INTERNAL:
	 * Method to check if an annotated element has a @Column.
     */
	protected boolean hasColumn() {
		return isAnnotationPresent(Column.class);
    }
    
    /**
     * INTERNAL:
     * 
	 * Method to check if this accessor has @PrimaryKeyJoinColumns.
     */
	protected boolean hasPrimaryKeyJoinColumns() {
		return isAnnotationPresent(PrimaryKeyJoinColumns.class);
    }
    
    /** 
     * INTERNAL:
     * Indicates whether the specified annotation is present on the annotated
     * element for this accessor. Method checks against the metadata complete
     * flag.
     */
    protected boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
        return isAnnotationPresent(annotation, getAnnotatedElement());
    }
    
    /** 
     * INTERNAL:
     * Indicates whether the specified annotation is present on the annotated
     * element for this accessor. Method checks against the metadata complete
     * flag.
     */
    protected boolean isAnnotationPresent(Class<? extends Annotation> annotation, AnnotatedElement annotatedElement) {
        return MetadataHelper.isAnnotationPresent(annotation, annotatedElement, m_descriptor);
    }
    
    /**
     * INTERNAL:
     * Return true if this accessor represents a basic mapping.
     */
    public boolean isBasic() {
        return false;
    }
    
    /**
     * INTERNAL:
     * Return true if this accessor represents a class.
     */
    public boolean isClass() {
        return false;
    }
    
    /**
     * INTERNAL:
     * Return true if this accessor represents an aggregate mapping.
     */
	public boolean isEmbedded() {
        return false;
    }
    
    /**
     * INTERNAL:
     * Return true if this accessor represents an aggregate id mapping.
     */
	public boolean isEmbeddedId() {
        return false;
    }
    
    /**
     * INTERNAL:
     * Return true if this accessor represents a m-m relationship.
     */
	public boolean isManyToMany() {
        return false;
    }
    
    /**
     * INTERNAL:
	 * Return true if this accessor represents a m-1 relationship.
     */
	public boolean isManyToOne() {
        return false;
    }
    
    /**
     * INTERNAL:
	 * Return true if this accessor represents a 1-m relationship.
     */
	public boolean isOneToMany() {
        return false;
    }
    
	/**
     * INTERNAL:
     * Return true if this accessor represents a 1-1 relationship.
     */
	public boolean isOneToOne() {
        return false;
    }
    
    /**
     * INTERNAL:
     * 
     * Subclasses that support processing an optional setting should override 
     * this  method, otherwise a runtime development exception is thrown for 
     * those  accessors who call this method and don't implement it themselves.
     */
    public boolean isOptional() {
        throw new RuntimeException("Development exception. The accessor: [" + this + "] should not call the isOptional method unless it overrides it.");
    }
	
	/**
     * INTERNAL:
	 * Return true if this accessor method represents a relationship. It will
     * cache the boolean value to avoid multiple checks and validation.
     */
	public boolean isRelationship() {
        if (m_isRelationship == null) {
            m_isRelationship = new Boolean(isManyToOne() || isManyToMany() || isOneToMany() || isOneToOne());
        }
        
        return m_isRelationship.booleanValue(); 
    }
    
    /**
     * INTERNAL:
	 * Return true if this is an XML processing accessor.
     */
	public boolean isXMLAccessor() {
        return false;
    }
    
    /**
     * INTERNAL:
	 * Return true if this accessor has already been processed.
     */
    public boolean isProcessed() {
        return m_isProcessed;    
    }
    
    /**
     * INTERNAL:
	 * Every accessor knows how to process themselves since they have all the
     * information they need.
     */
    public abstract void process();
    
    /**
     * INTERNAL: (Overidden in XMLClassAccessor and XMLEmbeddedAccessor)
     * Fast track processing a ClassAccessor for the given descriptor. 
     * Inheritance root classes and embeddables may be fast tracked.
     */
    protected ClassAccessor processAccessor(MetadataDescriptor descriptor) {
        ClassAccessor accessor = new ClassAccessor(new MetadataClass(descriptor.getJavaClass()), getProcessor(), descriptor);
        descriptor.setClassAccessor(accessor);
        accessor.process();
        return accessor;
    }
    
    /**
     * INTERNAL:
     * 
     * Process the primary key join columms for this accessors annotated element.
     */	
    protected List<MetadataPrimaryKeyJoinColumn> processPrimaryKeyJoinColumns(MetadataPrimaryKeyJoinColumns primaryKeyJoinColumns) {
        // This call will add any defaulted columns as necessary.
        List<MetadataPrimaryKeyJoinColumn> pkJoinColumns = primaryKeyJoinColumns.values(m_descriptor);
        
        if (m_descriptor.hasCompositePrimaryKey()) {
            // Validate the number of primary key fields defined.
            if (pkJoinColumns.size() != m_descriptor.getPrimaryKeyFields().size()) {
                m_validator.throwIncompletePrimaryKeyJoinColumnsSpecified(getJavaClass(), getAnnotatedElement());
            }
            
            // All the primary and foreign key field names should be specified.
            for (MetadataPrimaryKeyJoinColumn pkJoinColumn : pkJoinColumns) {
                if (pkJoinColumn.isPrimaryKeyFieldNotSpecified() || pkJoinColumn.isForeignKeyFieldNotSpecified()) {
                    m_validator.throwIncompletePrimaryKeyJoinColumnsSpecified(getJavaClass(), getAnnotatedElement());
                }
            }
        } else {
            if (pkJoinColumns.size() > 1) {
                m_validator.throwExcessivePrimaryKeyJoinColumnsSpecified(getJavaClass(), getAnnotatedElement());
            }
        }
        
        return pkJoinColumns;
    }
    
    /**
     * INTERNAL:
     * Common table processing for table, secondary table, join table and
     * collection table.
     */
    protected void processTable(MetadataTable table, String defaultName) {
        // Name could be "", need to check against the default name.
		String name = getName(table.getName(), defaultName, table.getNameContext());
        
        // Catalog could be "", need to check for an XML default.
        String catalog = getName(table.getCatalog(), m_descriptor.getCatalog(), table.getCatalogContext());
        
        // Schema could be "", need to check for an XML default.
        String schema = getName(table.getSchema(), m_descriptor.getSchema(), table.getSchemaContext());
        
        // Build a fully qualified name and set it on the table.
        table.setName(MetadataHelper.getFullyQualifiedTableName(name, catalog, schema));
    }
    
    /**
     * INTERNAL:
     * 
     * Set the getter and setter access methods for this accessor.
     */
    protected void setAccessorMethods(DatabaseMapping mapping) {
        if (m_descriptor.usesPropertyAccess()) {
            mapping.setGetMethodName(getName());
            mapping.setSetMethodName(getSetMethodName());
        }
    }
    
    /**
     * INTERNAL:
     * Return the annotated element for this accessor.
     */
    public void setAnnotatedElement(AnnotatedElement annotatedElement) {
        m_accessibleObject.setAnnotatedElement(annotatedElement);
    }
    
    /** 
     * INTERNAL:
	 * Set the correct indirection policy on a collection mapping. Method
     * assume that the reference class has been set on the mapping before
     * calling this method.
	 */
	protected void setIndirectionPolicy(CollectionMapping mapping, String mapKey) {
        Class rawClass = getRawClass();
        
        if (usesIndirection()) {            
            if (rawClass == Map.class) {
                mapping.useTransparentMap(mapKey);
            } else if (rawClass == List.class) {
                mapping.useTransparentList();
            } else if (rawClass == Collection.class) {
                mapping.useTransparentCollection();
                mapping.setContainerPolicy(new CollectionContainerPolicy(ClassConstants.IndirectList_Class));
            } else if (rawClass == Set.class) {
                mapping.useTransparentSet();
            } else {
                // Because of validation we should never get this far.
            }
        } else {
            mapping.dontUseIndirection();
            
            if (rawClass == Map.class) {
                mapping.useMapClass(java.util.Hashtable.class, mapKey);
            } else if (rawClass == Set.class) {
                mapping.useCollectionClass(java.util.HashSet.class);
            } else {
                mapping.useCollectionClass(java.util.Vector.class);
            }
        }
    }
    
    /**
     * INTERNAL:
     */
    public void setIsProcessed() {
        m_isProcessed = true;	
	}
    
    /**
     * INTERNAL:
     */
    public boolean usesIndirection() {
        return getFetchType().equals(MetadataConstants.LAZY);
    }
}
