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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ExcludeDefaultListeners;
import javax.persistence.ExcludeSuperclassListeners;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;

import oracle.toplink.essentials.descriptors.ClassDescriptor;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.MetadataAccessor;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataClass;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataField;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataMethod;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataColumn;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataDiscriminatorColumn;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataJoinColumn;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataJoinColumns;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataPrimaryKeyJoinColumn;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataPrimaryKeyJoinColumns;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.listeners.MetadataEntityClassListener;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.listeners.MetadataEntityListener;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataHelper;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataProcessor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataDescriptor;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.queries.MetadataEntityResult;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.queries.MetadataFieldResult;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.queries.MetadataNamedNativeQuery;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.queries.MetadataNamedQuery;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.queries.MetadataSQLResultSetMapping;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.tables.MetadataSecondaryTable;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.tables.MetadataTable;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.accessors.XMLMappedSuperclassAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.listeners.XMLEntityListener;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLConstants;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLHelper;

import oracle.toplink.essentials.internal.helper.DatabaseField;
import oracle.toplink.essentials.internal.helper.Helper;

import oracle.toplink.essentials.queryframework.ColumnResult;
import oracle.toplink.essentials.queryframework.EntityResult;
import oracle.toplink.essentials.queryframework.FieldResult;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class accessor.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class ClassAccessor extends NonRelationshipAccessor {
    // This is the parent class that defines the inheritance strategy, and
    // not necessarilly the immediate parent class.
    protected Class m_inheritanceParentClass;
    protected Boolean m_isInheritanceSubclass;
    protected List<ClassAccessor> m_mappedSuperclasses;
    
    /**
     * INTERNAL:
     */
    public ClassAccessor(MetadataAccessibleObject accessibleObject, MetadataProcessor processor, MetadataDescriptor descriptor) {
        super(accessibleObject, processor, descriptor);
    }
    
    /**
     * INTERNAL:
	 * Add multiple fields to the descriptor. Called from either @Inheritance 
     * or @SecondaryTable context.
	 */
    protected void addMultipleTableKeyFields(MetadataPrimaryKeyJoinColumns primaryKeyJoinColumns, String PK_CTX, String FK_CTX) {
        // ProcessPrimaryKeyJoinColumns will validate the primary key join
        // columns passed in and will return a list of 
        // MetadataPrimaryKeyJoinColumn.
        for (MetadataPrimaryKeyJoinColumn primaryKeyJoinColumn : processPrimaryKeyJoinColumns(primaryKeyJoinColumns)) {
            // In an inheritance case this call will return the pk field on the
            // root class of the inheritance hierarchy. Otherwise in a secondary
            // table case it's the primary key field name off our own descriptor.
            String defaultPKFieldName = m_descriptor.getPrimaryKeyFieldName();

            DatabaseField pkField = primaryKeyJoinColumn.getPrimaryKeyField();
            pkField.setName(getName(pkField, defaultPKFieldName, PK_CTX));

            DatabaseField fkField = primaryKeyJoinColumn.getForeignKeyField();
            fkField.setName(getName(fkField, pkField.getName(), FK_CTX));
        
            if (fkField.getName().equals(pkField.getName())) {
                // Add a multiple table primary key to the descriptor.
                m_descriptor.addMultipleTablePrimaryKeyField(pkField, fkField);
            } else {
                // Add a multiple table foreign key to the descriptor.
                m_descriptor.addMultipleTableForeignKeyField(pkField, fkField);
            }
        }
    }
    
    /**
     * INTERNAL:
     * Create and return the appropriate accessor based on the accessible 
     * object given. Order of checking is important, careful when modifying
     * or adding, check what the isXyz call does to determine if the accessor
     * is of type xyz.
     */
    protected MetadataAccessor buildAccessor(MetadataAccessibleObject accessibleObject) {
        MetadataAccessor accessor = m_descriptor.getAccessorFor(accessibleObject.getAttributeName());
        
        if (accessor == null) {
            if (MetadataHelper.isBasic(accessibleObject, m_descriptor)) {
                return new BasicAccessor(accessibleObject, this);
            } else if (MetadataHelper.isEmbedded(accessibleObject, m_descriptor)) {
                return new EmbeddedAccessor(accessibleObject, this);
            } else if (MetadataHelper.isEmbeddedId(accessibleObject, m_descriptor)) {
                return new EmbeddedIdAccessor(accessibleObject, this);
            } else if (MetadataHelper.isManyToMany(accessibleObject, m_descriptor)) {
                return new ManyToManyAccessor(accessibleObject, this);
            } else if (MetadataHelper.isManyToOne(accessibleObject, m_descriptor)) {
                return new ManyToOneAccessor(accessibleObject, this);
            } else if (MetadataHelper.isOneToMany(accessibleObject, m_descriptor)) {
                // A OneToMany can currently default, that is, doesn't require
                // an annotation to be present.
                return new OneToManyAccessor(accessibleObject, this);
            } else if (MetadataHelper.isOneToOne(accessibleObject, m_descriptor)) {
                // A OneToOne can currently default, that is, doesn't require
                // an annotation to be present.
                return new OneToOneAccessor(accessibleObject, this);
            } else {
                // Default case (everything else currently falls into this)
                return new BasicAccessor(accessibleObject, this);
            }
        } else {
            return accessor;
        }
    }
    
    /**
     * INTERNAL:
     * Clear the mapped supeclasses. Do this after the class loader has changed.
     * The list of mapped superclasses is lazily initialized.
     */
    public void clearMappedSuperclasses() {
        m_mappedSuperclasses = null;     
    }
    
    /**
     * INTERNAL: (Overridden in XMLClassAccessor)
     * Return the discriminator value for this accessor.
     */
    protected String getDiscriminatorValue() {
        DiscriminatorValue discriminatorValue = getAnnotation(DiscriminatorValue.class);
        
        if (discriminatorValue == null) {
            return null;
        } else {
            return discriminatorValue.value();
        }
    }
    
    /**
     * INTERNAL: (Overridden in XMLClassAccessor)
     * 
     * Return the name of this entity class.
     */
    protected String getEntityName() {
        Entity entity = getAnnotation(Entity.class);
        return (entity == null) ? "" : entity.name();
    }
    
    /**
     * INTERNAL:
     * 
     * This method returns the first parnt entity in an inheritance hierarchy 
     * that defines the inheritance strategy. That is, has an @Inheritance or
     * inheritance xml element.
     */
	public Class getInheritanceParentClass() {
        if (m_inheritanceParentClass == null) {
            Class lastParent = null;
            Class parent = getJavaClass().getSuperclass();
        
            while (parent != Object.class) {
                if (hasInheritance(parent) || m_project.containsDescriptor(parent)) {
                    if (lastParent == null) {
                        // Set the immediate parent class on the descriptor.
                        m_descriptor.setParentClass(parent);
                    }
                    
                    lastParent = parent;
                    
                    if (hasInheritance(parent)) {
                        break; // stop looking.
                    }
                } 
                
                parent = parent.getSuperclass();
            }
        
            // Finally set whatever we found as the inheritance parent class. 
            // Which may be null.
            m_inheritanceParentClass = lastParent;
        }
  
        return m_inheritanceParentClass;
    }
    
    /**
     * INTERNAL: 
     * Store the descriptor metadata for the root of our inheritance hierarchy.
     */
    public MetadataDescriptor getInheritanceParentDescriptor() {
        return m_project.getDescriptor(getInheritanceParentClass());
    }
    
    /**
     * INTERNAL: (Overridden in XMLClassAccessor)
     * Return the inheritance strategy. This method should only be called
     * on the root of the inheritance hierarchy.
     */
    protected String getInheritanceStrategy() {
        Inheritance inheritance = getAnnotation(Inheritance.class);
        
        if (inheritance == null) {
            return "";
        } else {
            return inheritance.strategy().name();   
        }
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * Return the java class that defines this accessor. It may be an
     * entity, embeddable or mapped superclass.
     */
    public Class getJavaClass() {
        return (Class) getAnnotatedElement();
    }
    
    /**
     * INTERNAL:
     * Build a list of classes that are decorated with a @MappedSuperclass.
     */
	public List<ClassAccessor> getMappedSuperclasses() {
        if (m_mappedSuperclasses == null) {
            m_mappedSuperclasses = new ArrayList<ClassAccessor>();
            
            Class parent = getJavaClass().getSuperclass();
        
            while (parent != Object.class) {
                if (isInheritanceSubclass() && hasEntity(parent)) {
                    // In an inheritance case we don't want to keep looking
                    // for mapped superclasses if they are not directly above
                    // us before the next entity in the hierarchy.
                    break;
                } else if (m_project.hasMappedSuperclass(parent)) {
                    Node node = m_project.getMappedSuperclassNode(parent);
                    XMLHelper helper = m_project.getMappedSuperclassHelper(parent);                    
                    m_mappedSuperclasses.add(new XMLMappedSuperclassAccessor(new MetadataClass(parent), node, helper, m_processor, m_descriptor));
                } else if (isAnnotationPresent(MappedSuperclass.class, parent)) {
                    m_mappedSuperclasses.add(new MappedSuperclassAccessor(new MetadataClass(parent), m_processor, m_descriptor));
                }
                
                parent = parent.getSuperclass();
            }
        }
        
        return m_mappedSuperclasses;
    }
    
    /**
     * INTERNAL: (Overridden in XMLClassAccessor)
     * 
     * Return true if the class has an @Entity.
     */
    protected boolean hasEntity(Class cls) {
        return isAnnotationPresent(Entity.class, cls);
    }
    
    /**
     * INTERNAL:
     * 
     * Return true if this class accessor has an inheritance specifications.
     */
    protected boolean hasInheritance() {
        return hasInheritance(getJavaClass());
    }
    
    /**
     * INTERNAL: (Overridden in XMLClassAccessor)
     * 
     * Return true if the class has an @Inheritance.
     */
    protected boolean hasInheritance(Class entityClass) {
        return isAnnotationPresent(Inheritance.class, entityClass);
    }
    
    /**
     * INTERNAL:
     */
     public boolean isClass() {
     	return true;
     }
     
    /**
     * INTERNAL:
     */
    public boolean isInheritanceSubclass() {
        if (m_isInheritanceSubclass == null) {
            m_isInheritanceSubclass = new Boolean(getInheritanceParentClass() != null);
        }
        
        return m_isInheritanceSubclass;
    }
     
     /**
     * INTERNAL:
     */
    protected boolean isTransient(AnnotatedElement annotatedElement, int modifier) {
        if (isAnnotationPresent(Transient.class, annotatedElement)) {
            if (MetadataHelper.getDeclaredAnnotationsCount(annotatedElement, m_descriptor) > 1) {
                m_validator.throwMappingAnnotationsAppliedToTransientAttribute(annotatedElement);
            }
            
            return true;
        } else if (Modifier.isTransient(modifier)) {
            if (MetadataHelper.getDeclaredAnnotationsCount(annotatedElement, m_descriptor) > 0) {
                m_validator.throwMappingAnnotationsAppliedToTransientAttribute(annotatedElement);
            }
            
            return true;
        }
        
        return false;
    }

    /**
     * INTERNAL:
     * Return true is this annotated element is not marked transient, static or 
     * abstract.
     */
    protected boolean isValidPersistenceElement(AnnotatedElement annotatedElement, int modifiers) {
        return ! (isTransient(annotatedElement, modifiers) || Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers));
    }
    
    /**
     * INTERNAL:
     * Check to see if this is a valid field to process for persistence. It is 
     * valid if it is not static, transient or has a @Transient specified.
     */
    protected boolean isValidPersistenceField(Field field) {
        return (isValidPersistenceElement(field, field.getModifiers()));
    }
    
    /**
     * INTERNAL:
     * Check to see if this is a valid method to process for persistence. It is 
     * valid if it is not static, transient or has a @Transient specified.
     */
    protected boolean isValidPersistenceMethod(Method method) {
        // Ignore methods marked transient, static or abstract.
        if (isValidPersistenceElement(method, method.getModifiers())) {
            // Look for methods that begin with "get" or "is", ignore all others.
            String methodName = method.getName();
            if (MetadataHelper.isValidPersistenceMethodName(methodName)) {
                // Ignore get methods with parameters.
                if (method.getParameterTypes().length > 0) {
                    return false;
                }
            
                Method setMethod = MetadataHelper.getSetMethod(method, getJavaClass());
            
                if (setMethod == null) {
                    if (MetadataHelper.getDeclaredAnnotationsCount(method, m_descriptor) > 0) {
                        // We decorated the property with annotations, but have 
                        // no corresponding setter property.
                        m_validator.throwNoCorrespondingSetterMethodDefined(getJavaClass(), method);
                    }
                } else {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * INTERNAL:
     * Process the items of interest on an entity or embeddable class. The 
     * order of processing is important, care must be taken if changes must 
     * be made.
     */
    public void process() {        
        if (hasEntity(getJavaClass())) {
            // This accessor represents an @Entity class.
            // Set the ignore flags for items that are already defined.
            m_descriptor.setIgnoreFlags();
            
            // Process the @Entity.
            processEntity();
            
            // Process the @Table and @Inheritance.
            processTableAndInheritance();
            
            // Process the common class level attributes that an entity or
            // mapped superclass may define. This should be done before the
            // processMappedSuperclasses call since it will call this method 
            // also. We want to be able to grab the metadata off the actual 
            // entity class first because it needs to override any settings 
            // from the mapped superclass and may need to log a warning.
            processClassMetadata();
                
            // Process the @MappedSuperclass(es).
            processMappedSuperclasses();
                    
            // Process the accessors on this entity.
            processAccessors();
            
            // Validate we found a primary key.
            validatePrimaryKey();
                    
            // Process the @SecondaryTable(s).
            processSecondaryTables();
        } else {
            // This accessor represents an @Embeddable class
            m_descriptor.setIsEmbeddable();
            
            // Process the accessors on this embeddable.
            processAccessors();
        }
    }
    
    /**
     * INTERNAL:
     * Process an accessor method or field. Relationship accessors will be 
     * stored for later processing.
     */
    protected void processAccessor(MetadataAccessor accessor) {
        if (! accessor.isProcessed()) {
            // Store the accessor for later retrieval.
            m_descriptor.addAccessor(accessor);
            
            if (accessor.isRelationship()) {
                // Store the relationship accessors for later processing.
                m_project.addRelationshipDescriptor(m_descriptor);
            } else {
                accessor.process();
                accessor.setIsProcessed();
            }
        }
    }
    
    /**
     * INTERNAL:
     * Create mappings from the fields directly.
     */
    protected void processAccessorFields() {
        for (Field field : MetadataHelper.getFields(getJavaClass())) {
            if (isValidPersistenceField(field)) {
                processAccessor(buildAccessor(new MetadataField(field)));
            }
        }
    }
    
    /**
     * INTERNAL:
     * Create mappings via the class properties.
     */
    protected void processAccessorMethods() {
        for (Method method : MetadataHelper.getDeclaredMethods(getJavaClass())) {
            if (isValidPersistenceMethod(method)) {
                processAccessor(buildAccessor(new MetadataMethod(method)));
            }
        }
    }
    
    /**
     * INTERNAL:
     * Process the accessors for the given class.
     */
    protected void processAccessors() {
        // Process the fields or methods on the class.
        if (m_descriptor.usesPropertyAccess()) {
            processAccessorMethods();
        } else {
            processAccessorFields();
        }
    }
    
    /**
     * INTERNAL:
     * Process an @AssociationOverride for an Entity (or MappedSuperclass) 
     * that inherits from a MappedSuperclass.
     */
    protected void processAssociationOverride(String attributeName, MetadataJoinColumns joinColumns) {
        // Add association overrides from XML as we find them.
        if (joinColumns.loadedFromXML()) {
            m_descriptor.addAssociationOverride(attributeName, joinColumns);
        } else {
            // Association override from annotations should not override those 
            // loaded from XML.
            MetadataJoinColumns existingJoinColumns = m_descriptor.getAssociationOverrideFor(attributeName);
            
            if (existingJoinColumns == null || ! existingJoinColumns.loadedFromXML()) {
                m_descriptor.addAssociationOverride(attributeName, joinColumns);
            } else {
                // WIP should log a warning.
            }
        }
    }
    
    /**
     * INTERNAL: (Overriden in XMLClassAccessor)
     * Process an @AssociationOverrides for an Entity (or MappedSuperclass) 
     * that inherits from a MappedSuperclass.
     * 
     * It will also look for an @AssociationOverride.
     */
    protected void processAssociationOverrides() {
        // Look for an @AssociationOverrides.
        AssociationOverrides associationOverrides = getAnnotation(AssociationOverrides.class);
        if (associationOverrides != null) {
            for (AssociationOverride associationOverride : associationOverrides.value()) {
                processAssociationOverride(associationOverride.name(), new MetadataJoinColumns(associationOverride.joinColumns()));
            }
        }
        
        // Look for an @AssociationOverride.
        AssociationOverride associationOverride = getAnnotation(AssociationOverride.class);
        if (associationOverride != null) {
            processAssociationOverride(associationOverride.name(), new MetadataJoinColumns(associationOverride.joinColumns()));
        }
    }
    
    /**
     * INTERNAL:
     * Process the @AttributeOverrides and @AttributeOverride for an Entity (or 
     * MappedSuperclass) that inherits from a MappedSuperclass.
     */
    protected void processAttributeOverride(MetadataColumn column) {
        String attributeName = column.getAttributeName();
        
        // Add attribute overrides from XML as we find them.
        if (column.loadedFromXML()) {
            m_descriptor.addAttributeOverride(column);
        } else {
            // Attribute overrides from annotations should not override
            // those loaded from XML.
            MetadataColumn existingColumn = m_descriptor.getAttributeOverrideFor(attributeName);
            
            if (existingColumn == null || ! existingColumn.loadedFromXML()) {
                m_descriptor.addAttributeOverride(column);
            } else {
                // WIP should log a warning.
            }
        }
    }
    
    /**
     * INTERNAL: (Overridden in XMLClassAccessor)
     * Process the @AttributeOverrides and @AttributeOverride for an Entity (or 
     * MappedSuperclass) that inherits from a MappedSuperclass.
     */
    protected void processAttributeOverrides() {
        // Look for an @AttributeOverrides.
        AttributeOverrides attributeOverrides = getAnnotation(AttributeOverrides.class);	
        if (attributeOverrides != null) {
            for (AttributeOverride attributeOverride : attributeOverrides.value()) {
                processAttributeOverride(new MetadataColumn(attributeOverride, getAnnotatedElement()));
            }
        }
        
        // Look for an @AttributeOverride.
        AttributeOverride attributeOverride = getAnnotation(AttributeOverride.class);
        if (attributeOverride != null) {
            processAttributeOverride(new MetadataColumn(attributeOverride, getAnnotatedElement()));
        }
    }
    
    /**
     * INTERNAL:
     * Process the array of methods for lifecyle callback events and set them
     * on the given event listener.
     */
    protected void processCallbackMethods(Method[] candidateMethods, MetadataEntityListener listener) {
        for (Method method : candidateMethods) {
            if (isAnnotationPresent(PostLoad.class, method)) {
                setPostLoad(method, listener);
            }
            
            if (isAnnotationPresent(PostPersist.class, method)) {
                setPostPersist(method, listener);
            }
            
            if (isAnnotationPresent(PostRemove.class, method)) {
                setPostRemove(method, listener);
            }
            
            if (isAnnotationPresent(PostUpdate.class, method)) {
                setPostUpdate(method, listener);
            }
            
            if (isAnnotationPresent(PrePersist.class, method)) {
                setPrePersist(method, listener);
            }
            
            if (isAnnotationPresent(PreRemove.class, method)) {
                setPreRemove(method, listener);
            }
            
            if (isAnnotationPresent(PreUpdate.class, method)) {
                setPreUpdate(method, listener);
            }
        }
    }
    
    /**
     * INTERNAL:
     * 
     * Process the items of interest on an entity or mapped superclass class. 
     */
    protected void processClassMetadata() {
        // Process the @AttributeOverrides and @AttributeOverride.
        processAttributeOverrides();
                    
        // Process the @AssociationOverrides and @AssociationOverride.
        processAssociationOverrides();
        
        // Process the @NamedQueries and @NamedQuery.
        processNamedQueries();
                    
        // Process the @NamedNativeQueries and @NamedNativeQuery.
        processNamedNativeQueries();
                    
        // Process the @SqlRessultSetMapping.
        processSqlResultSetMappings();
                    
        // Process the @TableGenerator.
        processTableGenerator();
            
        // Process the @SequenceGenerator
        processSequenceGenerator();
                    
        // Process the @IdClass (pkClass).
        processIdClass();
        
        // Process the @ExcludeDefaultListeners.
        processExcludeDefaultListeners();
        
        // Process the @ExcludeSuperclassListeners.
        processExcludeSuperclassListeners();
    }
    
    /**
     * INTERNAL:
     * Process the default listeners defined in XML. This method will process 
     * the class for additional lifecycle callback methods that are decorated 
     * with annotations.
     * 
     * NOTE: We add the default listeners regardless if the exclude default 
     * listeners flag is set. This allows the user to change the exlcude flag 
     * at runtime and have the default listeners available to them.
     */
    protected void processDefaultListeners(ClassLoader loader) {
        Map<XMLHelper, NodeList> defaultListeners = m_project.getDefaultListeners();
        
        for (XMLHelper helper : defaultListeners.keySet()) {
            // Update the class loader.
            helper.setLoader(loader);
            NodeList nodes = defaultListeners.get(helper);
            
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                
                // Build an xml entity listener.
                XMLEntityListener listener = new XMLEntityListener(helper.getClassForNode(node), getJavaClass());
                
                // Process the lifecycle callback events from XML.
                Method[] candidateMethods = MetadataHelper.getCandidateCallbackMethodsForDefaultListener(listener);
                processLifecycleEvents(listener, node, helper, candidateMethods);
                
                // Process the candidate callback methods on this listener for
                // additional callback methods decorated with annotations.
                processCallbackMethods(candidateMethods, listener);
        
                // Add the listener to the descriptor.
                m_descriptor.addDefaultEventListener(listener);
            }
		}
    }
    
    /**
     * INTERNAL:
     * Process a @DiscriminatorColumn (if there is one, otherwise default) to 
     * set this classes indication field name for inheritance.
     */
    protected void processDiscriminatorColumn() {
        DiscriminatorColumn discriminatorColumn = getAnnotation(DiscriminatorColumn.class);
        processDiscriminatorColumn(new MetadataDiscriminatorColumn(discriminatorColumn));
    }
    
    /**
     * INTERNAL:
	 * Process a discriminator column to set this class indicatior field name 
     * for inheritance.
	 */
	protected void processDiscriminatorColumn(MetadataDiscriminatorColumn discriminatorColumn) {
        DatabaseField field = new DatabaseField();

        field.setName(getName(discriminatorColumn.getName(), MetadataDiscriminatorColumn.DEFAULT_NAME, m_logger.DISCRIMINATOR_COLUMN));
        field.setLength(discriminatorColumn.getLength());
        field.setTableName(m_descriptor.getPrimaryTableName());
        field.setColumnDefinition(discriminatorColumn.getColumnDefinition());
        field.setType(MetadataHelper.getDiscriminatorType(discriminatorColumn.getDiscriminatorType()));
        
        // Set the class indicator field on the inheritance policy.
        m_descriptor.setClassIndicatorField(field);
    }
	
    /**
     * INTERNAL:
	 * Process a discriminator value to set the class indicator on the root 
     * descriptor of the inheritance hierarchy. 
     * 
     * If there is no discriminator value, the class indicator defaults to 
     * the class name.
	 */
	protected void processDiscriminatorValue() {
        if (! Modifier.isAbstract(getJavaClass().getModifiers())) {
            // Add the indicator to the inheritance root class' descriptor. The
            // default is the short class name.
            String discriminatorValue = getDiscriminatorValue();
            
            if (discriminatorValue == null) {
                m_descriptor.addClassIndicator(getJavaClass(), Helper.getShortClassName(getJavaClassName()));    
            } else {
                m_descriptor.addClassIndicator(getJavaClass(), discriminatorValue);    
            }
        }
    }
    
    /**
	 * INTERNAL:
	 * Process an entity.
	 */
	protected void processEntity() {
        // Don't override existing alias.
        if (m_descriptor.getAlias().equals("")) {
            String alias = getEntityName();
            
            if (alias.equals("")) {
                alias = Helper.getShortClassName(getJavaClassName());
                m_logger.logConfigMessage(m_logger.ALIAS, m_descriptor, alias);
            }

            // Verify that the alias is not a duplicate.
            ClassDescriptor existingDescriptor = getProject().getSession().getProject().getDescriptorForAlias(alias);
            if (existingDescriptor != null) {
                m_validator.throwNonUniqueEntityName(existingDescriptor.getJavaClassName(), m_descriptor.getJavaClassName(), alias);
            }

            // Set the alias on the descriptor and add it to the project.
            m_descriptor.setAlias(alias);
            getProject().getSession().getProject().addAlias(alias, m_descriptor.getClassDescriptor());
        }
	}
    
    /**
     * INTERNAL: (Overridden in XMLCLassAccessor)
     * Process the entity class for lifecycle callback event methods.
     */
    public MetadataEntityListener processEntityEventListener(ClassLoader loader) {
        MetadataEntityClassListener listener = new MetadataEntityClassListener(getJavaClass());
            
        // Check the entity class for lifecycle callback annotations.
        processCallbackMethods(MetadataHelper.getCandidateCallbackMethodsForEntityClass(getJavaClass()), listener);
        
        return listener;
    }
    
    /**
     * INTERNAL: (Overridden in XMLClassAccessor)
     * Process the @EntityListeners for this class accessor.
     */
    public void processEntityListeners(Class entityClass, ClassLoader loader) {
        EntityListeners entityListeners = getAnnotation(EntityListeners.class);
        
        if (entityListeners != null) {
            for (Class entityListener : entityListeners.value()) {
                MetadataEntityListener listener = new MetadataEntityListener(entityListener, entityClass);
                
                // Process the candidate callback methods for this listener ...
                processCallbackMethods(MetadataHelper.getCandidateCallbackMethodsForEntityListener(listener), listener);
                
                // Add the entity listener to the descriptor event manager.    
                m_descriptor.addEntityListenerEventListener(listener);
            }
        }
    }
    
    /**
     * INTERNAL: (Overridden in XMLClassAccessor)
     * Process the @ExcludeDefaultListeners if one is specified (taking
     * metadata-complete into consideration).
     */
    protected void processExcludeDefaultListeners() {
        // Don't overrite a true flag that could be set from a subclass
        // that already exlcuded them.
        if (isAnnotationPresent(ExcludeDefaultListeners.class)) {
            m_descriptor.setExcludeDefaultListeners(true);
        }
    }
    
    /**
     * INTERNAL: (Overridden in XMLClassAccessor)
     * Process the @ExcludeSuperclassListeners if one is specified (taking
     * metadata-complete into consideration).
     */
    protected void processExcludeSuperclassListeners() {
        // Don't overrite a true flag that could be set from a subclass
        // that already exlcuded them.
        if (isAnnotationPresent(ExcludeSuperclassListeners.class)) {
            m_descriptor.setExcludeSuperclassListeners(true);
        }
    }
    
    /**
     * INTERNAL: (Overridden in XMLClassAccessor)
     * 
     * Process an @IdClass.
     */
    protected void processIdClass() {
        IdClass idClass = getAnnotation(IdClass.class);
        
        if (idClass != null) {
            processIdClass(idClass.value(), m_logger.IGNORE_ID_CLASS_ANNOTATION);
        }
    }
    
    /**
     * INTERNAL:
     * 
     * Process an @IdClass or id-class element. It is used to specify composite 
     * primary keys. The primary keys will be processed and stored from the PK 
     * class so that they may be validated against the fields or properties of 
     * the entity bean. The access type of a primary key class is determined by 
     * the access type of the entity for which it is the primary key.
     * 
     * NOTE: the class passed in may be a mapped-superclass or entity.
     */
    protected void processIdClass(Class idClass, String ignoreCtx) {
        if (m_descriptor.ignoreIDs()) {
            m_logger.logWarningMessage(ignoreCtx, m_descriptor, idClass);
        } else {
            m_descriptor.setPKClass(idClass);
                
            if (m_descriptor.usesPropertyAccess()) {
                for (Method method : MetadataHelper.getDeclaredMethods(idClass)) {
                    String methodName = method.getName();
                
                    if (MetadataHelper.isValidPersistenceMethodName(methodName)) {
                        m_descriptor.addPKClassId(MetadataHelper.getAttributeNameFromMethodName(methodName), MetadataHelper.getGenericReturnType(method));
                    }
                }
            } else {
                for (Field field : MetadataHelper.getFields(idClass)) {
                    m_descriptor.addPKClassId(field.getName(), MetadataHelper.getGenericType(field));
                }
            }   
        }
    }
    
    /**
     * INTERNAL:
     * 
     * Process the @Inheritance or inheritance tag metadata for a parent of
     * an inheritance hierarchy. One may or may not be specified for the entity 
     * class that is the root of the entity class hierarchy, so we need to
     * default in this case.
     */
    protected void processInheritance() {
        if (m_descriptor.ignoreInheritance()) {
            m_logger.logWarningMessage(m_logger.IGNORE_INHERITANCE, m_descriptor);
        } else {
            // Get the inheritance strategy and store it on the descriptor.
            String inheritanceStrategy = getInheritanceStrategy();
            if (inheritanceStrategy.equals("")) {
                inheritanceStrategy = InheritanceType.SINGLE_TABLE.name();
            }

            m_descriptor.setInheritanceStrategy(inheritanceStrategy);
                
            // Process the discriminator column metadata.
            processDiscriminatorColumn();
                
            // Process the discriminator value metadata.
            processDiscriminatorValue();
        }
    }
    
    /**
     * INTERNAL:
     * 
     * Process the inheritance metadata for an inheritance subclass. The
     * parent descriptor must be provided.
     */
    protected void processInheritanceSubclass(MetadataDescriptor parentDescriptor) {
        // Ignore any project.xml settings.
        if (m_descriptor.ignoreInheritance()) {
            // Log a warning that we are ignoring the inheritance metadata.
            m_logger.logWarningMessage(m_logger.IGNORE_INHERITANCE, m_descriptor);
        } else {
            // Inheritance.stategy() = SINGLE_TABLE, set the flag. Unless this
            // descriptor has its own inheritance.
            if (parentDescriptor.usesSingleTableInheritanceStrategy() && ! hasInheritance()) {
                m_descriptor.setSingleTableInheritanceStrategy();
            } else {
                // Inheritance.stategy() = JOINED, look for primary key join 
                // column(s) and add multiple table key fields.
                MetadataPrimaryKeyJoinColumns primaryKeyJoinColumns = getPrimaryKeyJoinColumns(m_descriptor.getPrimaryKeyTableName(), m_descriptor.getPrimaryTableName());
                addMultipleTableKeyFields(primaryKeyJoinColumns, m_logger.INHERITANCE_PK_COLUMN, m_logger.INHERITANCE_FK_COLUMN);
            }    
            
            // Process the discriminator value, unless this descriptor has
            // its own inheritance.
            if (! hasInheritance()) {
                processDiscriminatorValue();
            }
            
            // If the root descriptor has an id class, we need to set the same 
            // id class on our descriptor.
            if (parentDescriptor.hasCompositePrimaryKey()) {
                m_descriptor.setPKClass(parentDescriptor.getPKClassName());
            }
        }
    }
    
    /**
     * INTERNAL:
     * Process and array of @JoinColumn into a list of metadata join column.
     */
    protected List<MetadataJoinColumn> processJoinColumns(JoinColumn[] joinColumns) {
        ArrayList<MetadataJoinColumn> list = new ArrayList<MetadataJoinColumn>();
        
        for (JoinColumn joinColumn : joinColumns) {
            list.add(new MetadataJoinColumn(joinColumn));
        }
        
        return list;
    }
    
    /**
     * INTERNAL:
     * Process the XML lifecycle event for the given listener.
     */
    protected void processLifecycleEvent(MetadataEntityListener listener, Node node, String event, XMLHelper helper, Method[] candidateMethods) {
        Node eventNode = helper.getNode(node, event);
        
        if (eventNode != null) {
            String methodName = helper.getNodeValue(eventNode, XMLConstants.ATT_METHOD_NAME);
            Method method = MetadataHelper.getMethodForName(candidateMethods, methodName);

            if (method == null) {
                m_validator.throwInvalidCallbackMethod(listener.getListenerClass(), methodName);
            } else if (event.equals(XMLConstants.PRE_PERSIST)) {
                setPrePersist(method, listener);
            } else if (event.equals(XMLConstants.POST_PERSIST)) {
                setPostPersist(method, listener);
            } else if (event.equals(XMLConstants.PRE_REMOVE)) {
                setPreRemove(method, listener);
            } else if (event.equals(XMLConstants.POST_REMOVE)) {
                setPostRemove(method, listener);
            } else if (event.equals(XMLConstants.PRE_UPDATE)) {
                setPreUpdate(method, listener);
            } else if (event.equals(XMLConstants.POST_UPDATE)) {
                setPostUpdate(method, listener);
            } else if (event.equals(XMLConstants.POST_LOAD)) {
                setPostLoad(method, listener);
            }
        }
    }
    
    /**
     * INTERNAL:
     * Process the XML lifecycle events for the given listener.
     */
    protected void processLifecycleEvents(MetadataEntityListener listener, Node node, XMLHelper helper, Method[] candidateMethods) {
        processLifecycleEvent(listener, node, XMLConstants.PRE_PERSIST, helper, candidateMethods);
        processLifecycleEvent(listener, node, XMLConstants.POST_PERSIST, helper, candidateMethods);
        processLifecycleEvent(listener, node, XMLConstants.PRE_REMOVE, helper, candidateMethods);
        processLifecycleEvent(listener, node, XMLConstants.POST_REMOVE, helper, candidateMethods);
        processLifecycleEvent(listener, node, XMLConstants.PRE_UPDATE, helper, candidateMethods);
        processLifecycleEvent(listener, node, XMLConstants.POST_UPDATE, helper, candidateMethods);
        processLifecycleEvent(listener, node, XMLConstants.POST_LOAD, helper, candidateMethods);
    }
    
    /**
     * INTERNAL:
     * Process the listeners for this class.
     */
    public void processListeners(ClassLoader loader) {
        // Step 1 - process the default listeners.
        processDefaultListeners(loader);

        // Step 2 - process the entity listeners that are defined on the entity 
        // class and mapped superclasses (taking metadata-complete into 
        // consideration). Go through the mapped superclasses first, top -> down 
        // only if the exclude superclass listeners flag is not set.    
        if (! m_descriptor.excludeSuperclassListeners()) {
            List<ClassAccessor> mappedSuperclasses = getMappedSuperclasses();
            int mappedSuperclassesSize = mappedSuperclasses.size();
            
            for (int i = mappedSuperclassesSize - 1; i >= 0; i--) {
                mappedSuperclasses.get(i).processEntityListeners(getJavaClass(), loader);
            }
        }
        
        processEntityListeners(getJavaClass(), loader); 
                
        // Step 3 - process the entity class for lifecycle callback methods. Go
        // through the mapped superclasses as well.
        MetadataEntityListener listener = processEntityEventListener(loader);
        
        if (! m_descriptor.excludeSuperclassListeners()) {
            for (ClassAccessor mappedSuperclass : getMappedSuperclasses()) {
                mappedSuperclass.processMappedSuperclassEventListener(listener, getJavaClass(), loader);
            }
        }
        
        // Add the listener only if we actually found callback methods.
        if (listener.hasCallbackMethods()) {
            m_descriptor.setEntityEventListener(listener);
        }
    }

    /**
     * INTERNAL:
     * Process the @MappedSuperclass(es) if there are any. There may be
     * several MappedSuperclasses for any given Entity.
     */
    protected void processMappedSuperclass() {
        // Process the common class level attributes that an entity or
        // mapped superclass may define.
        processClassMetadata();
            
        // Process the accessors from the mapped superclass.
        processAccessors();
    }
    
    /**
     * INTERNAL:
     * Process the @MappedSuperclass(es) if there are any. There may be
     * several MappedSuperclasses for any given Entity.
     */
    protected void processMappedSuperclasses() {
        for (ClassAccessor mappedSuperclass : getMappedSuperclasses()) {
            mappedSuperclass.process();
        }
    }
    
    /**
     * INTERNAL: (Overridden in XMLCLassAccessor)
     * Process the mapped superclass class for lifecycle callback event methods.
     */
    public void processMappedSuperclassEventListener(MetadataEntityListener listener, Class entityClass, ClassLoader loader) {
        // Check the mapped superclass for lifecycle callback annotations.
        processCallbackMethods(MetadataHelper.getCandidateCallbackMethodsForMappedSuperclass(getJavaClass(), entityClass), listener);
    }
    
    /**
     * INTERNAL: (Overridden in XMLClassAccessor)
     * 
     * Process a @NamedNativeQueries. The method will also look for 
     * a @NamedNativeQuery. This method currently only stores the queries if 
     * there are some. The actually query processing isn't done till 
     * addNamedQueriesToSession is called.
     */
    protected void processNamedNativeQueries() {
        // Look for a @NamedNativeQueries.
        NamedNativeQueries namedNativeQueries = getAnnotation(NamedNativeQueries.class);
        if (namedNativeQueries != null) {
            for (NamedNativeQuery namedNativeQuery : namedNativeQueries.value()) {
                processNamedNativeQuery(new MetadataNamedNativeQuery(namedNativeQuery, getJavaClass()));
            }
        }
        
        // Look for a @NamedNativeQuery.
        NamedNativeQuery namedNativeQuery = getAnnotation(NamedNativeQuery.class);
        if (namedNativeQuery != null) {
            processNamedNativeQuery(new MetadataNamedNativeQuery(namedNativeQuery, getJavaClass()));
        }
    }
    
    /**
     * INTERNAL:
     * Process a MetadataNamedNativeQuery. The actually query processing isn't 
     * done till addNamedQueriesToSession is called.
     */
    protected void processNamedNativeQuery(MetadataNamedNativeQuery namedNativeQuery) {
        if (m_project.hasNamedNativeQuery(namedNativeQuery.getName())) {
            MetadataNamedNativeQuery existingNamedNativeQuery = m_project.getNamedNativeQuery(namedNativeQuery.getName());
            
            if (existingNamedNativeQuery.loadedFromAnnotations() && namedNativeQuery.loadedFromXML()) {
                // Override the existing query.
                m_project.addNamedNativeQuery(namedNativeQuery);
            } else {
                // Ignore the query and log a message.
                m_logger.logWarningMessage(namedNativeQuery.getIgnoreLogMessageContext(), namedNativeQuery.getLocation(), namedNativeQuery.getName());
            }
        } else {
            m_project.addNamedNativeQuery(namedNativeQuery);
        }
    }
    
    /**
     * INTERNAL: (Overridden in XMLClassAccessor)
     * 
     * Process a @NamedQueries. The method will also look for a @NamedQuery.
     * This method currently only stores the queries if there are some. The
     * actually query processing isn't done till addNamedQueriesToSession is
     * called.
     */
    protected void processNamedQueries() {
        // Look for a @NamedQueries.
        NamedQueries namedQueries = getAnnotation(NamedQueries.class);
        
        if (namedQueries != null) {
            for (NamedQuery namedQuery : namedQueries.value()) {
                processNamedQuery(new MetadataNamedQuery(namedQuery, getJavaClass()));
            }
        }
        
        // Look for a @NamedQuery.
        NamedQuery namedQuery = getAnnotation(NamedQuery.class);
        
        if (namedQuery != null) {
            processNamedQuery(new MetadataNamedQuery(namedQuery, getJavaClass()));
        }
    }
    
    /**
     * INTERNAL:
     * Add a metadata named query to the project. The actually query processing 
     * isn't done till addNamedQueriesToSession is called.
     */
    protected void processNamedQuery(MetadataNamedQuery namedQuery) {
        if (m_project.hasNamedQuery(namedQuery.getName())) {
            MetadataNamedQuery existingNamedQuery = m_project.getNamedQuery(namedQuery.getName());
            
            if (existingNamedQuery.loadedFromAnnotations() && namedQuery.loadedFromXML()) {
                // Override the existing query.
                m_project.addNamedQuery(namedQuery);
            } else {
                // Ignore the query and log a message.
                m_logger.logWarningMessage(namedQuery.getIgnoreLogMessageContext(), namedQuery.getLocation(), namedQuery.getName());
            }
        } else {
            m_project.addNamedQuery(namedQuery);
        }
    }
    
    /**
     * INTERNAL:
     * Process a MetadataSecondaryTable. Do all the table name defaulting and 
     * set the correct, fully qualified name on the TopLink DatabaseTable.
     */
    protected void processSecondaryTable(MetadataSecondaryTable secondaryTable) {
        // Process any table defaults and log warning messages.
        processTable(secondaryTable, secondaryTable.getName());
        
        // Add the table to the descriptor.
        m_descriptor.addTable(secondaryTable.getDatabaseTable());
        
        // Get the primary key join column(s) and add the multiple table key fields.
        MetadataPrimaryKeyJoinColumns primaryKeyJoinColumns = secondaryTable.getPrimaryKeyJoinColumns(m_descriptor.getPrimaryTableName());
        addMultipleTableKeyFields(primaryKeyJoinColumns, m_logger.SECONDARY_TABLE_PK_COLUMN, m_logger.SECONDARY_TABLE_FK_COLUMN);
    }
    
    /**
     * INTERNAL: (Overridden in XMLClassAccessor)
     * Process a @SecondaryTables. If one isn't found, try a @SecondaryTable.
     * WIP - If the @SecondaryTable does not define the pkJoinColumns(), we
     * could look for PrimaryKeyJoinColumns on the class itself. This is not
     * mandatory through.
     */
    protected void processSecondaryTables() {
        // Look for a SecondaryTables annotation.
        SecondaryTables secondaryTables = getAnnotation(SecondaryTables.class);
        if (secondaryTables != null) {
            if (m_descriptor.ignoreTables()) {
                m_logger.logWarningMessage(m_logger.IGNORE_SECONDARY_TABLE_ANNOTATION, getJavaClass());        
            } else {
                for (SecondaryTable secondaryTable : secondaryTables.value()) {
                    processSecondaryTable(new MetadataSecondaryTable(secondaryTable, m_logger));
                }
            }
        } else {
            // Look for a SecondaryTable annotation
            SecondaryTable secondaryTable = getAnnotation(SecondaryTable.class);
            if (secondaryTable != null) {
                if (m_descriptor.ignoreTables()) {
                    m_logger.logWarningMessage(m_logger.IGNORE_SECONDARY_TABLE_ANNOTATION, getJavaClass());        
                } else {    
                    processSecondaryTable(new MetadataSecondaryTable(secondaryTable, m_logger));
                }
            }
        }
    } 
    
    /**
     * INTERNAL:
     * Process an sql result set mapping metadata into a TopLink 
     * SqlResultSetMapping and store it on the session.
     */
    protected void processSqlResultSetMapping(MetadataSQLResultSetMapping sqlResultSetMapping) {        
        // Initialize a new SqlResultSetMapping (with the metadata name)
        oracle.toplink.essentials.queryframework.SQLResultSetMapping mapping = new oracle.toplink.essentials.queryframework.SQLResultSetMapping(sqlResultSetMapping.getName());
        
        // Process the entity results.
        for (MetadataEntityResult eResult : sqlResultSetMapping.getEntityResults()) {
            EntityResult entityResult = new EntityResult(eResult.getEntityClass().getName());
        
            // Process the field results.
            for (MetadataFieldResult fResult : eResult.getFieldResults()) {
                entityResult.addFieldResult(new FieldResult(fResult.getName(), fResult.getColumn()));
            }
        
            // Process the discriminator value;
            entityResult.setDiscriminatorColumn(eResult.getDiscriminatorColumn());
        
            // Add the result to the SqlResultSetMapping.
            mapping.addResult(entityResult);
        }
        
        // Process the column results.
        for (String columnResult : sqlResultSetMapping.getColumnResults()) {
            mapping.addResult(new ColumnResult(columnResult));
        }
            
        getProject().getSession().getProject().addSQLResultSetMapping(mapping);
    }
    
    /**
     * INTERNAL:
     * Process a @SqlResultSetMappings.
     */
    protected void processSqlResultSetMappings() {
        // Look for a @SqlResultSetMappings.
        SqlResultSetMappings sqlResultSetMappings = getAnnotation(SqlResultSetMappings.class);

        if (sqlResultSetMappings != null) {
            for (SqlResultSetMapping sqlResultSetMapping : sqlResultSetMappings.value()) {
                processSqlResultSetMapping(new MetadataSQLResultSetMapping(sqlResultSetMapping));
            }
        } else {
            // Look for a @SqlResultSetMapping.
            SqlResultSetMapping sqlResultSetMapping = getAnnotation(SqlResultSetMapping.class);
            
            if (sqlResultSetMapping != null) {
                processSqlResultSetMapping(new MetadataSQLResultSetMapping(sqlResultSetMapping));
            }
        }
    } 

    /**
     * INTERNAL: (Overridden in XMLClassAccessor)
	 * Process a @Table annotation.
	 */
	protected void processTable() {
        if (m_descriptor.ignoreTables()) {
            m_logger.logWarningMessage(m_logger.IGNORE_TABLE_ANNOTATION, getJavaClass());
        } else {
            Table table = getAnnotation(Table.class);
            processTable(new MetadataTable(table, m_logger));
        }
    }
    
    /**
     * INTERNAL:
     * 
	 * Process a MetadataTable. Do all the table name defaulting and set the
     * correct, fully qualified name on the TopLink DatabaseTable.
	 */
    protected void processTable(MetadataTable table) {
        // Process any table defaults and log warning messages.
        processTable(table, m_descriptor.getDefaultTableName());

        // Set the table on the descriptor.
        m_descriptor.setPrimaryTable(table.getDatabaseTable());
    }
    
    /**
     * INTERNAL:
     * 
     * Process any inheritance specifics. This method will fast track any 
     * parent inheritance processing, be it specified or defaulted.
     */
    protected void processTableAndInheritance() {
        // If we are an inheritance subclass, ensure our parent is processed 
        // first since it has information its subclasses depend on.
		if (isInheritanceSubclass()) {
            MetadataDescriptor parentDescriptor = getInheritanceParentDescriptor();
            
            // Process the parent class accesor if it hasn't already been done.
            ClassAccessor parentAccessor = parentDescriptor.getClassAccessor();
            if (parentAccessor == null) {
                parentAccessor = processAccessor(parentDescriptor);
            }
            
            // A parent, who didn't know they were a parent (a root class of an 
            // inheritance hierarchy that does not have an  @Inheritance 
            // annotation or XML tag) must process and default the inheritance 
            // parent metadata.
            if (! parentDescriptor.hasInheritance()) {
                parentAccessor.processInheritance();
            }
                
            // If this entity has inheritance metadata as well, then the 
            // inheritance stragety is mixed and we need to process the 
            // inheritance parent metadata for this entity's subclasses to 
            // process correctly.
            // WIP - check that that strategies are indeed changing ....
            if (hasInheritance()) {
                // Process the table metadata if there is one, otherwise default.
                processTable();
                
                // Process the parent inheritance specifics.
                processInheritance();
                
                // Process the inheritance subclass metadata.
                processInheritanceSubclass(parentDescriptor);
            } else {
                // Process the table information for this descriptor (for a 
                // joined strategy), if there is one specified. Must be called
                // before processing the inheritance metadata.
                if (parentDescriptor.usesJoinedInheritanceStrategy()) {
                    processTable();
                }
                
                // Process the inheritance subclass metadata.
                processInheritanceSubclass(parentDescriptor);
            }
		} else {
            // Process the table metadata if there is one, otherwise default.
            processTable();
            
            // If we have inheritance metadata, then process it now. If we are 
            // an inheritance root class that doesn't know it, a subclass will 
            // force this processing to occur.
            if (hasInheritance()) {
                processInheritance();
            }
        }
    }
    
    /**
     * INTERNAL:
     * Set the post load event method on the listener.
     */
    protected void setPostLoad(Method method, MetadataEntityListener listener) {
        listener.setPostBuildMethod(method);
        listener.setPostCloneMethod(method);
        listener.setPostRefreshMethod(method);
    }
    
    /**
     * INTERNAL:
     * Set the post persist event method on the listener.
     */
    protected void setPostPersist(Method method, MetadataEntityListener listener) {
        listener.setPostInsertMethod(method); 
    }
    
    /**
     * INTERNAL:
     * Set the post remove event method on the listener.
     */
    protected void setPostRemove(Method method,  MetadataEntityListener listener) {
        listener.setPostDeleteMethod(method);
    }
    
    /**
     * INTERNAL:
     * * Set the post update event method on the listener.
     */
    protected void setPostUpdate(Method method,  MetadataEntityListener listener) {
        listener.setPostUpdateMethod(method);
    }
            
    /**
     * INTERNAL:
     * Set the pre persist event method on the listener.
     */
    protected void setPrePersist(Method method,  MetadataEntityListener listener) {
        listener.setPrePersistMethod(method);
    }
    
    /**
     * INTERNAL:
     * Set the pre remove event method on the listener.
     */
    protected void setPreRemove(Method method,  MetadataEntityListener listener) {
        listener.setPreRemoveMethod(method);
    }
    
    /**
     * INTERNAL:
     * Set the pre update event method on the listener.
     */
    protected void setPreUpdate(Method method,  MetadataEntityListener listener) {
        listener.setPreUpdateWithChangesMethod(method);
    }
    
    /**
     * INTERNAL:
     * 
     * Call this method after a primary key should have been found.
     */
    protected void validatePrimaryKey() {
        // If this descriptor has a composite primary key, check that all 
        // our composite primary key attributes were validated. 
        if (m_descriptor.hasCompositePrimaryKey()) {
            if (m_descriptor.pkClassWasNotValidated()) {
                m_validator.throwInvalidCompositePKSpecification(getJavaClass(), m_descriptor.getPKClassName());
            }
        } else {
            // Descriptor has a single primary key. Validate an id 
            // attribute was found, unless we are an inheritance subclass
            // or an aggregate descriptor.
            if (! m_descriptor.hasPrimaryKeyFields() && ! isInheritanceSubclass()) {
                m_validator.throwNoPrimaryKeyAnnotationsFound(getJavaClass());
            }
        }  
    }
}
