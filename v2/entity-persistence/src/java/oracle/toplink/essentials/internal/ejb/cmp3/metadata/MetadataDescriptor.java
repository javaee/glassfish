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
package oracle.toplink.essentials.internal.ejb.cmp3.metadata;

import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.toplink.essentials.descriptors.ClassDescriptor;

import oracle.toplink.essentials.descriptors.RelationalDescriptor;
import oracle.toplink.essentials.descriptors.VersionLockingPolicy;
import oracle.toplink.essentials.descriptors.TimestampLockingPolicy;

import oracle.toplink.essentials.internal.descriptors.OptimisticLockingPolicy;

import oracle.toplink.essentials.internal.ejb.cmp3.base.CMP3Policy;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataConstants;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.ClassAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.MetadataAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.RelationshipAccessor;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataColumn;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataJoinColumns;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.listeners.MetadataEntityListener;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLConstants;

import oracle.toplink.essentials.internal.helper.DatabaseField;
import oracle.toplink.essentials.internal.helper.DatabaseTable;

import oracle.toplink.essentials.mappings.DatabaseMapping;

/**
 * Common metatata descriptor for the annotation and xml processors. This class
 * is a wrap on an actual TopLink descriptor.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class MetadataDescriptor {
    protected ClassAccessor m_accessor;
	protected ClassDescriptor m_descriptor;
    protected Class m_javaClass;
    
    protected boolean m_ignoreIDs;
	protected boolean m_ignoreTables;
    protected boolean m_isCascadePersist;
    protected boolean m_ignoreAnnotations; // XML metadata complete
    protected boolean m_ignoreInheritance;
    protected Boolean m_usesPropertyAccess;
    
    protected String m_xmlSchema;   // XML metadata
    protected String m_xmlCatalog;  // XML metadata
    protected String m_xmlAccess;   // XML metadata
	protected String m_primaryTableName;
	protected String m_embeddedIdAttributeName;
    
    protected List<String> m_idAttributeNames;
    protected List<String> m_orderByAttributeNames;
    protected List<String> m_idOrderByAttributeNames;
    protected List<MetadataDescriptor> m_aggregateDescriptors;
    
    protected Map<String, Type> m_pkClassIDs;
    protected Map<String, MetadataAccessor> m_accessors;
    protected Map<String, String> m_pkJoinColumnAssociations;
    protected Map<String, MetadataColumn> m_attributeOverrides;
    protected Map<String, MetadataJoinColumns> m_associationOverrides;
    
    protected Map<String, RelationshipAccessor> m_relationshipAccessors;
    protected Map<String, Map<String, MetadataAccessor>> m_biDirectionalManyToManyAccessors;
    
    /**
     * INTERNAL: 
     */
    public MetadataDescriptor(Class javaClass) {
        init();
        m_descriptor = new RelationalDescriptor();
        m_descriptor.setExistenceChecking("Check database");
        m_descriptor.setAlias("");
        setJavaClass(javaClass);
    }
     
     /**
      * INTERNAL:
      */
    public void addAccessor(MetadataAccessor accessor) {
        m_accessors.put(accessor.getAttributeName(), accessor);
         
        if (accessor.isRelationship()) {
            m_relationshipAccessors.put(accessor.getAttributeName(), (RelationshipAccessor) accessor);
        }
         
        // Store bidirectional ManyToMany relationships so that we may look at 
        // attribute names when defaulting join columns.
        if (accessor.isManyToMany()) {
            String mappedBy = ((RelationshipAccessor) accessor).getMappedBy();
            
            if (! mappedBy.equals("")) {
                String referenceClassName = accessor.getReferenceClassName();
                
                // Initialize the map of bi-directional mappings for this class.
                if (! m_biDirectionalManyToManyAccessors.containsKey(referenceClassName)) {
                    m_biDirectionalManyToManyAccessors.put(referenceClassName, new HashMap<String, MetadataAccessor>());
                }
            
                m_biDirectionalManyToManyAccessors.get(referenceClassName).put(mappedBy, accessor);
            }
        }
    }
     
    /**
     * INTERNAL:
     */
    public void addAggregateDescriptor(MetadataDescriptor aggregateDescriptor) {
        m_aggregateDescriptors.add(aggregateDescriptor);
    }
    
    /**
     * INTERNAL:
     */
     public void addAssociationOverride(String attributeName, MetadataJoinColumns joinColumns) {
        m_associationOverrides.put(attributeName, joinColumns);   
     }
    
    /**
     * INTERNAL:
     */
    public void addAttributeOverride(MetadataColumn column) {
        m_attributeOverrides.put(column.getAttributeName(), column);
    }

    /**
     * INTERNAL:
     */
    public void addClassIndicator(Class entityClass, String value) {
        if (m_accessor.isInheritanceSubclass()) {
            m_accessor.getInheritanceParentDescriptor().addClassIndicator(entityClass, value);   
        } else {
            m_descriptor.getInheritancePolicy().addClassNameIndicator(entityClass.getName(), value);
        }
    }
    
    /** 
     * INTERNAL:
     */
    public void addDefaultEventListener(MetadataEntityListener listener) {
        m_descriptor.getEventManager().addDefaultEventListener(listener);
    }
    
    /**
     * INTERNAL:
     */
    public void addEntityListenerEventListener(MetadataEntityListener listener) {
        m_descriptor.getEventManager().addEntityListenerEventListener(listener);
    }

    
    /**
     * INTERNAL:
     */
    public void addIdAttributeName(String idAttributeName) {
        m_idAttributeNames.add(idAttributeName);    
    }
    
    /**
     * INTERNAL:
     */
    public void addMapping(DatabaseMapping mapping) {
        m_descriptor.addMapping(mapping);
    }
    
    /**
     * INTERNAL:
     */
    public void addMultipleTableForeignKeyField(DatabaseField pkField, DatabaseField fkField) {
        m_descriptor.addMultipleTableForeignKeyField(pkField, fkField);
        m_pkJoinColumnAssociations.put(fkField.getName(), pkField.getName());
    }
    
    /**
     * INTERNAL:
     */
    public void addMultipleTablePrimaryKeyField(DatabaseField pkField, DatabaseField fkField) {
        m_descriptor.addMultipleTablePrimaryKeyField(pkField, fkField);
    }
    
    /**
     * INTERNAL:
     * We store these to validate the primary class when processing
     * the entity class.
     */
    public void addPKClassId(String attributeName, Type type) {
        m_pkClassIDs.put(attributeName, type);
    }
    
    /**
     * INTERNAL:
     */
    public void addPrimaryKeyField(DatabaseField field) {
        m_descriptor.addPrimaryKeyField(field);
    }
    
    /**
     * INTERNAL:
     */
    public void addTable(DatabaseTable table) {
        m_descriptor.addTable(table);
    }
    
    /**
     * INTERNAL:
     */
    public boolean excludeSuperclassListeners() {
        return m_descriptor.getEventManager().excludeSuperclassListeners();
    }
    
    /**
     * INTERNAL:
     * This method will first check for an accessor with name equal to 
     * fieldOrPropertyName (that is, assumes it is a field name). If no accessor
     * is found than it assumes fieldOrPropertyName is a property name and 
     * converts it to its corresponding field name and looks for the accessor
     * again. If still no accessor is found and this descriptor metadata is
     * and an inheritance sublcass, than it will then look on the root metadata
     * descriptor. Null is returned otherwise.
     */
    public MetadataAccessor getAccessorFor(String fieldOrPropertyName) {
    	MetadataAccessor accessor = m_accessors.get(fieldOrPropertyName);
        
        if (accessor == null) {
            // Perhaps we have a property name ...
            accessor = m_accessors.get(MetadataHelper.getAttributeNameFromMethodName(fieldOrPropertyName));
           
            // If still no accessor and we are an inheritance subclass, check 
            // the root descriptor now.
            if (accessor == null && m_accessor.isInheritanceSubclass()) {
                accessor = m_accessor.getInheritanceParentDescriptor().getAccessorFor(fieldOrPropertyName);
            }
        }
        
        return accessor;
    }
    
    /**
     * INTERNAL:
     */
    public String getAlias() {
        return m_descriptor.getAlias();
    }
    
    /**
     * INTERNAL:
     */
    public MetadataJoinColumns getAssociationOverrideFor(String attributeName) {
        return m_associationOverrides.get(attributeName);
    }
    
    /**
     * INTERNAL:
     */
    public MetadataColumn getAttributeOverrideFor(String attributeName) {
        return m_attributeOverrides.get(attributeName);
    }

    /**
     * INTERNAL:
     */
    public DatabaseField getClassIndicatorField() {
        if (m_accessor.getInheritanceParentDescriptor() != null) {
            return m_accessor.getInheritanceParentDescriptor().getClassDescriptor().getInheritancePolicy().getClassIndicatorField();
        } else {
            if (getClassDescriptor().hasInheritance()) {
                return getClassDescriptor().getInheritancePolicy().getClassIndicatorField();
            } else {
                return null;
            }
        }
    }
    
    /**
     * INTERNAL:
     * The default table name is the descriptor alias, unless this descriptor 
     * metadata is an inheritance subclass with a SINGLE_TABLE strategy. Then 
     * it is the table name of the root descriptor metadata.
     */
    public String getDefaultTableName() {
        String defaultTableName = getAlias().toUpperCase();
        
        if (m_accessor.isInheritanceSubclass()) {    
            if (m_accessor.getInheritanceParentDescriptor().usesSingleTableInheritanceStrategy()) {
                defaultTableName = m_accessor.getInheritanceParentDescriptor().getPrimaryTableName();
            }
        }
        
        return defaultTableName;
    }
    
    /**
     * INTERNAL:
     */
    public String getCatalog() {
    	return m_xmlCatalog;
    }
    
    /**
     * INTERNAL:
     */
    public ClassAccessor getClassAccessor() {
        return m_accessor;
    }
    
    /**
     * INTERNAL:
     */
    public ClassDescriptor getClassDescriptor() {
        return m_descriptor;
    }
    
    /**
     * INTERNAL:
     */
    public String getEmbeddedIdAttributeName() {
        return m_embeddedIdAttributeName;
    }
    
    /**
     * INTERNAL:
     * Return the primary key attribute name for this entity.
     */
    public String getIdAttributeName() {
        if (getIdAttributeNames().isEmpty()) {
            if (m_accessor.isInheritanceSubclass()) {
                return m_accessor.getInheritanceParentDescriptor().getIdAttributeName();
            } else {
                return "";
            }
        } else {
            return (String) getIdAttributeNames().get(0);
        }
    }
    
    /**
     * INTERNAL:
     * Return the id attribute names declared on this descriptor metadata.
     */
    public List<String> getIdAttributeNames() {
        return m_idAttributeNames;
    }
    
    /**
     * INTERNAL:
     * Return the primary key attribute names for this entity. If there are no
     * id attribute names set then we are either:
     * 1) an inheritance subclass, get the id attribute names from the root
     *    of the inheritance structure.
     * 2) we have an embedded id. Get the id attribute names from the embedded
     *    descriptor metadata, which is equal the attribute names of all the
     *    direct to field mappings on that descriptor metadata. Currently does
     *    not traverse nested embeddables.
     */
    public List<String> getIdOrderByAttributeNames() {
        if (m_idOrderByAttributeNames.isEmpty()) {
            if (m_idAttributeNames.isEmpty()) {
                if (m_accessor.isInheritanceSubclass()) {  
                    // Get the id attribute names from our root parent.
                    m_idOrderByAttributeNames = m_accessor.getInheritanceParentDescriptor().getIdAttributeNames();
                } else {
                    // We must have a composite primary key as a result of an embedded id.
                    m_idOrderByAttributeNames = getAccessorFor(getEmbeddedIdAttributeName()).getReferenceDescriptor().getOrderByAttributeNames();
                } 
            } else {
                m_idOrderByAttributeNames = m_idAttributeNames;
            }
        }
            
        return m_idOrderByAttributeNames;
    }
    
    
    /**
     * INTERNAL:
     * Assumes hasBidirectionalManyToManyAccessorFor has been called before
     * hand. 
     */
     public MetadataAccessor getBiDirectionalManyToManyAccessor(String className, String attributeName) {
        return m_biDirectionalManyToManyAccessors.get(className).get(attributeName);
    }
    
    /**
     * INTERNAL:
     * This will return the attribute names for all the direct to field mappings 
     * on this descriptor metadata. This method will typically be called when an 
     * @Embedded or @EmbeddedId attribute has been specified in an @OrderBy.
     */
    public List<String> getOrderByAttributeNames() {
        if (m_orderByAttributeNames.isEmpty()) {
            for (DatabaseMapping mapping : getMappings()) {
                if (mapping.isDirectToFieldMapping()) {
                    m_orderByAttributeNames.add(mapping.getAttributeName());
                }
            }
        }
        
        return m_orderByAttributeNames;
    }

    /**
     * INTERNAL:
     */
    public Class getJavaClass() {
        return m_javaClass;
    }
    
    /**
     * INTERNAL:
     */
    public String getJavaClassName() {
        return m_descriptor.getJavaClassName();
    }
    
    /**
     * INTERNAL:
     */
	public MetadataLogger getLogger() {
        return getClassAccessor().getLogger();
	}
    
    /**
     * INTERNAL:
     */
    public DatabaseMapping getMappingForAttributeName(String attributeName) {
        return getMappingForAttributeName(attributeName, null);
    } 
    
    /**
     * INTERNAL:
     * 
     * Non-owning mappings that need to look up the owning mapping, should call 
     * this method with their respective accessor to check for circular mappedBy 
     * references. If the referencingAccessor is null, no check will be made.
     */
    public DatabaseMapping getMappingForAttributeName(String attributeName, MetadataAccessor referencingAccessor) {
        MetadataAccessor accessor = (MetadataAccessor) getAccessorFor(attributeName);
        
        if (accessor != null) {
            // If the accessor is a relationship accessor than it may or may
            // not have been processed yet. Fast track its processing if it
            // needs to be. The process call will do nothing if it has already
            // been processed.
            if (accessor.isRelationship()) {
                RelationshipAccessor relationshipAccessor = (RelationshipAccessor) accessor;
                
                // Check that we don't have circular mappedBy values which 
                // will cause an infinite loop.
                if (referencingAccessor != null && ! relationshipAccessor.isManyToOne() && relationshipAccessor.getMappedBy().equals(referencingAccessor.getAttributeName())) {
                    getValidator().throwCircularMappedByReferences(referencingAccessor.getJavaClass(), referencingAccessor.getAttributeName(), getJavaClass(), attributeName);
                }
                
                relationshipAccessor.processRelationship();
            }
            
            return m_descriptor.getMappingForAttributeName(attributeName);
        }
        
        // We didn't find a mapping on this descriptor, check our aggregate 
        // descriptors now.
        for (MetadataDescriptor aggregateDmd : m_aggregateDescriptors) {
            DatabaseMapping mapping = aggregateDmd.getMappingForAttributeName(attributeName, referencingAccessor);
            
            if (mapping != null) {
                return mapping;
            }
        }
        
        // We didn't find a mapping on the aggregate descriptors. If we are an
        // inheritance subclass, check for a mapping on the inheritance root
        // descriptor metadata.
        if (m_accessor.isInheritanceSubclass()) {
            return m_accessor.getInheritanceParentDescriptor().getMappingForAttributeName(attributeName, referencingAccessor);
        }
        
        // Found nothing ... return null.
        return null;
    } 
    
    /**
     * INTERNAL:
     */
    public List<DatabaseMapping> getMappings() {
        return m_descriptor.getMappings();
    }
    
    /**
     * INTERNAL:
     */
    public String getPKClassName() {
        String pkClassName = null;
        
        if (m_descriptor.hasCMPPolicy()) {
            pkClassName = ((CMP3Policy) m_descriptor.getCMPPolicy()).getPKClassName();    
        }
        
        return pkClassName;
    }
    
    /**
     * INTERNAL:
	 * Method to return the primary key field name for the given descriptor
     * metadata. Assumes there is one.
     */
    public String getPrimaryKeyFieldName() {
        return ((DatabaseField)(getPrimaryKeyFields().iterator().next())).getName();
    }
    
    /**
     * INTERNAL:
	 * Method to return the primary key field names for the given descriptor
     * metadata. getPrimaryKeyFieldNames() on ClassDescriptor returns qualified
     * names. We don't want that.
     */
    public List<String> getPrimaryKeyFieldNames() {
        List<DatabaseField> primaryKeyFields = getPrimaryKeyFields();
        List<String> primaryKeyFieldNames = new ArrayList<String>(primaryKeyFields.size());
        
        for (DatabaseField primaryKeyField : primaryKeyFields) {
            primaryKeyFieldNames.add(primaryKeyField.getName());
        }
        
        return primaryKeyFieldNames;
    }
    
    /**
     * INTERNAL:
     * Return the primary key fields for this descriptor metadata. If this is
     * an inheritance subclass and it has no primary key fields, then grab the 
     * primary key fields from the root.
     */
    public List<DatabaseField> getPrimaryKeyFields() {
        List<DatabaseField> primaryKeyFields = m_descriptor.getPrimaryKeyFields();
        
        if (primaryKeyFields.isEmpty() && m_accessor.isInheritanceSubclass()) {
            primaryKeyFields = m_accessor.getInheritanceParentDescriptor().getPrimaryKeyFields();
        }
        
        return primaryKeyFields;
    }
    
    /**
     * INTERNAL:
     * Recursively check the potential chaining of the primart key fields from 
     * a inheritance subclass, all the way to the root of the inheritance 
     * hierarchy.
     */
    public String getPrimaryKeyJoinColumnAssociation(String foreignKeyName) {
        String primaryKeyName = m_pkJoinColumnAssociations.get(foreignKeyName);
        
        if (primaryKeyName == null || ! m_accessor.isInheritanceSubclass()) {
            return foreignKeyName;
        } else {
            return m_accessor.getInheritanceParentDescriptor().getPrimaryKeyJoinColumnAssociation(primaryKeyName);
        } 
    }
    
    /**
     * INTERNAL:
     * Assumes there is one primary key field set. This method should be called 
     * when qualifying any primary key field (from a join column) for this 
     * descriptor. This method was created because in an inheritance hierarchy 
     * with a joined strategy we can't use getPrimaryTableName() since it would
     * return the wrong table name. From the spec, the primary key must be 
     * defined on the entity that is the root of the entity hierarchy or on a 
     * mapped superclass of the entity hierarchy. The primary key must be 
     * defined exactly once in an entity hierarchy.
     */
	public String getPrimaryKeyTableName() {
        return ((DatabaseField)(getPrimaryKeyFields().iterator().next())).getTable().getQualifiedName();
	}
    
    /**
     * INTERNAL:
     */
	public String getPrimaryTableName() {
        if (m_primaryTableName == null && m_accessor.isInheritanceSubclass()) {
            return m_accessor.getInheritanceParentDescriptor().getPrimaryTableName();
        } else {
            if (m_descriptor.isAggregateDescriptor()) {
                // Aggregate descriptors don't have table names.
                return "";
            }
            
            return m_primaryTableName;
        }
	}
    
    /**
     * INTERNAL:
     */
	public MetadataProject getProject() {
        return getClassAccessor().getProject();
	}
    
    /**
     * INTERNAL:
     */
    public Collection<RelationshipAccessor> getRelationshipAccessors() {
        return m_relationshipAccessors.values();
    }
    
    /**
     * INTERNAL:
     */
    public String getSchema() {
    	return m_xmlSchema;
    }
    
    /**
     * INTERNAL:
     */
    public DatabaseField getSequenceNumberField() {
        return m_descriptor.getSequenceNumberField();
    }
    
    /**
     * INTERNAL:
     */
    public MetadataValidator getValidator() {
        return getClassAccessor().getValidator();
    }
    
    /**
     * INTERNAL:
     */
    public boolean hasAssociationOverrideFor(String attributeName) {
        return m_associationOverrides.containsKey(attributeName);
    }
    
    /**
     * INTERNAL:
     */
    public boolean hasAttributeOverrideFor(String attributeName) {
        return m_attributeOverrides.containsKey(attributeName);
    }
    
    /**
     * INTERNAL:
     */
    public boolean hasCompositePrimaryKey() {
        return getPrimaryKeyFields().size() > 1 || getPKClassName() != null;
    }
    
    /**
     * INTERNAL:
     */
    public boolean hasEmbeddedIdAttribute() {
        return m_embeddedIdAttributeName != null;
    }
    
    /**
     * INTERNAL:
     */
    public boolean hasInheritance() {
        return m_descriptor.hasInheritance();
    }
    
    /**
     * INTERNAL:
     */
    public boolean hasBiDirectionalManyToManyAccessorFor(String className, String attributeName) {
        if (m_biDirectionalManyToManyAccessors.containsKey(className)) {
            return m_biDirectionalManyToManyAccessors.get(className).containsKey(attributeName);
        }
        
        return false;
    }

    /**
     * INTERNAL:
     */
    public boolean hasMappingForAttributeName(String attributeName) {
        return m_descriptor.getMappingForAttributeName(attributeName) != null;
    }
    
    /**
     * INTERNAL:
	 * Return true is the descriptor has primary key fields set.
     */
	public boolean hasPrimaryKeyFields() {
		return m_descriptor.getPrimaryKeyFields().size() > 0;
    }
    
    /**
     * INTERNAL:
     */
    public boolean ignoreIDs() {
        return m_ignoreIDs;    
    }
    
    /**
     * INTERNAL:
     */
    public boolean ignoreInheritance() {
        return m_ignoreInheritance;    
    }
    
    /**
     * INTERNAL:
     */
    public boolean ignoreTables() {
        return m_ignoreTables;    
    }
    
    /**
     * INTERNAL:
     */
    private void init() {
        m_xmlAccess = "";
        m_xmlSchema = "";
        m_xmlCatalog = "";
        
        m_ignoreIDs = false;
        m_ignoreTables = false;
        m_isCascadePersist = false;
        m_ignoreInheritance = false;
        m_ignoreAnnotations = false;
        
        m_idAttributeNames = new ArrayList<String>();
        m_orderByAttributeNames = new ArrayList<String>();
        m_idOrderByAttributeNames = new ArrayList<String>();
        m_aggregateDescriptors = new ArrayList<MetadataDescriptor>();
        
        m_pkClassIDs = new HashMap<String, Type>();
        m_accessors = new HashMap<String, MetadataAccessor>();
        m_pkJoinColumnAssociations = new HashMap<String, String>();
        m_attributeOverrides = new HashMap<String, MetadataColumn>();
        m_associationOverrides = new HashMap<String, MetadataJoinColumns>();
        
        m_relationshipAccessors = new HashMap<String, RelationshipAccessor>();
        m_biDirectionalManyToManyAccessors = new HashMap<String, Map<String, MetadataAccessor>>();
    }
    
    /**
     * INTERNAL:
     * Indicates that cascade-persist should be applied to all relationship 
     * mappings for this entity.
     */
    public boolean isCascadePersist() {
    	return m_isCascadePersist;
    }
    
    /**
     * INTERNAL:
     */
    public boolean isEmbeddable() {
        return m_descriptor.isAggregateDescriptor();
    }
    
    /**
     * INTERNAL:
     */
    public boolean isEmbeddableCollection() {
        return m_descriptor.isAggregateCollectionDescriptor();
    }
    
    /**
     * INTERNAL:
     * Indicates that we found an XML field access type for this metadata
     * descriptor.
     */
    public boolean isXmlFieldAccess() {
        return m_xmlAccess.equals(XMLConstants.FIELD);
    }
    
    /**
     * INTERNAL:
     * Indicates that we found an XML property access type for this metadata
     * descriptor.
     */
    public boolean isXmlPropertyAccess() {
        return m_xmlAccess.equals(XMLConstants.PROPERTY);
    }
    
    /**
     * INTERNAL:
     */
    public boolean pkClassWasNotValidated() {
        return ! m_pkClassIDs.isEmpty();
    }

    /**
     * INTERNAL:
     */
    public void setAccess(String access) {
        m_xmlAccess = access;
    }
    
    /**
     * INTERNAL:
     */
    public void setAlias(String alias) {
        m_descriptor.setAlias(alias);
    }
    
    /**
     * INTERNAL:
     */
    public void setCatalog(String xmlCatalog) {
    	m_xmlCatalog = xmlCatalog;
    }
            
    /**
     * INTERNAL:
     */
    public void setClassAccessor(ClassAccessor accessor) {
        m_accessor = accessor;
    }
    
    /**
     * INTERNAL:
     */
    public void setClassIndicatorField(DatabaseField field) {
        m_descriptor.getInheritancePolicy().setClassIndicatorField(field);    
    }
    
    /**
     * INTERNAL:
     */
	public void setDescriptor(ClassDescriptor descriptor) {
        m_descriptor = descriptor;
    }
    
    /**
     * INTERNAL:
     */
    public void setEmbeddedIdAttributeName(String embeddedIdAttributeName) {
        m_embeddedIdAttributeName = embeddedIdAttributeName;
    }
    
    /** 
     * INTERNAL:
     */
    public void setEntityEventListener(MetadataEntityListener listener) {
        m_descriptor.getEventManager().setEntityEventListener(listener);
    }
    
    /**
     * INTERNAL:
     */
    public void setExcludeDefaultListeners(boolean excludeDefaultListeners) {
        m_descriptor.getEventManager().setExcludeDefaultListeners(excludeDefaultListeners);
    }
    
    /**
     * INTERNAL:
     */
    public void setExcludeSuperclassListeners(boolean excludeSuperclassListeners) {
        m_descriptor.getEventManager().setExcludeSuperclassListeners(excludeSuperclassListeners);
    }
    
    /**
     * INTERNAL:
     */
    public void setIgnoreFlags() {
        m_ignoreInheritance = m_descriptor.hasInheritance();
        m_ignoreTables = m_descriptor.getTableNames().size() > 0;
        m_ignoreIDs = m_descriptor.getPrimaryKeyFieldNames().size() > 0;
    }
    
    /**
     * INTERNAL:
     * Stored on the root class of an inheritance hierarchy.
     */
    public void setInheritanceStrategy(String inheritanceStrategy) {
        if (inheritanceStrategy.equals(MetadataConstants.TABLE_PER_CLASS)) {
            getValidator().throwTablePerClassInheritanceNotSupported(getJavaClass());
        } else if (inheritanceStrategy.equals(MetadataConstants.SINGLE_TABLE)) {
            m_descriptor.getInheritancePolicy().setSingleTableStrategy();
        } else {
            m_descriptor.getInheritancePolicy().setJoinedStrategy();
        }
    }
    
    /**
     * INTERNAL:
     * Indicates that cascade-persist should be added to the set of cascade 
     * values for all relationship mappings.
     */
    public void setIsCascadePersist(boolean isCascadePersist) {
    	m_isCascadePersist = isCascadePersist;
    }
    
    /**
     * INTERNAL:
     */
    public void setIsEmbeddable() {
        m_descriptor.descriptorIsAggregate();
    }

    /**
     * INTERNAL:
     */
    public void setIsIsolated(boolean isIsolated) {
        m_descriptor.setIsIsolated(isIsolated);
    }
    
    /**
     * INTERNAL:
     * Used to set this descriptors java class. 
     */
    public void setJavaClass(Class javaClass) {
        m_javaClass = javaClass;
        m_descriptor.setJavaClassName(javaClass.getName());
    }
    
    /**
     * INTERNAL:
     */
    protected void setOptimisticLockingPolicy(OptimisticLockingPolicy policy) {
        m_descriptor.setOptimisticLockingPolicy(policy);
    }
    
    /**
     * INTERNAL:
     * Set the inheritance parent class for this class.
     */
    public void setParentClass(Class parent) {
        m_descriptor.getInheritancePolicy().setParentClassName(parent.getName());
    }
    
    /**
     * INTERNAL:
     */
    public void setPKClass(Class pkClass) {
        setPKClass(pkClass.getName());
    }
    
    /**
     * INTERNAL:
     */
    public void setPKClass(String pkClassName) {
        CMP3Policy policy = new CMP3Policy();
        policy.setPrimaryKeyClassName(pkClassName);
        m_descriptor.setCMPPolicy(policy);
    }
    
    /**
     * INTERNAL:
     */
	public void setPrimaryTable(DatabaseTable primaryTable) {
        addTable(primaryTable);
		m_primaryTableName = primaryTable.getQualifiedName();
	}

	/**
	 * INTERNAL:
	 */
	public void setSchema(String xmlSchema) {
    	m_xmlSchema = xmlSchema;
    }
    
    /**
     * INTERNAL:
     */
    public void setSequenceNumberField(DatabaseField field) {
        m_descriptor.setSequenceNumberField(field);
    }
    
    /**
     * INTERNAL:
     */
    public void setSequenceNumberName(String name) {
        m_descriptor.setSequenceNumberName(name);
    }
    
    /**
     * INTERNAL:
     * Indicates that all annotations should be ignored, and only default values 
     * set by the annotations processor.
     */
    public void setIgnoreAnnotations(boolean ignoreAnnotations) {
    	m_ignoreAnnotations = ignoreAnnotations;
    }
    
    /**
     * INTERNAL:
     * Sets the strategy on the descriptor's inheritance policy to SINGLE_TABLE.  
     * The default is JOINED.
     */
    public void setSingleTableInheritanceStrategy() {
        m_descriptor.getInheritancePolicy().setSingleTableStrategy();
    }
    
    /**
     * INTERNAL:
     * Indicates whether or not annotations should be ignored, i.e. only default 
     * values processed.
     */
    public boolean ignoreAnnotations() {
    	return m_ignoreAnnotations;
    }
    
    /**
     * INTERNAL:
     * 
     * Set the access-type while processing a class like Embeddable as it 
     * inherits the access-type from the referencing entity.
     */
    public void setUsesPropertyAccess(Boolean usesPropertyAccess) {
        m_usesPropertyAccess = usesPropertyAccess;
    }
    
    /**
     * INTERNAL:
     * Indicates if the strategy on the descriptor's inheritance policy is 
     * JOINED.
     */
    public boolean usesJoinedInheritanceStrategy() {
        return m_descriptor.getInheritancePolicy().isJoinedStrategy();
    }
    
    /**
     * INTERNAL:
     */
    public boolean usesOptimisticLocking() {
        return m_descriptor.usesOptimisticLocking();
    }
    
    /**
     * INTERNAL:
     * Returns true if this class uses property access. In an inheritance 
     * hierarchy, the subclasses inherit their access type from the parent.
     * The metadata helper method caches the class access types for 
     * efficiency.
     */
	public boolean usesPropertyAccess() {
        if (m_accessor.isInheritanceSubclass()) {
            return m_accessor.getInheritanceParentDescriptor().usesPropertyAccess();
        } else {
            if (m_usesPropertyAccess == null) {
                if (MetadataHelper.havePersistenceAnnotationsDefined(MetadataHelper.getFields(getJavaClass())) || isXmlFieldAccess()) {
                    if (isXmlPropertyAccess()) {
                        // WIP - throw an exception.
                    }
                
                    // We have persistence annotations defined on a field from 
                    // the entity or field access has been set via XML, set the 
                    // access to FIELD.
                    m_usesPropertyAccess = new Boolean(false);
                } else if (MetadataHelper.havePersistenceAnnotationsDefined(MetadataHelper.getDeclaredMethods(getJavaClass())) || isXmlPropertyAccess()) {
                    if (isXmlFieldAccess()) {
                        // WIP - throw an exception.
                    }
                
                    // We have persistence annotations defined on a method from 
                    // the entity or method access has been set via XML, set the 
                    // access to PROPERTY.
                    m_usesPropertyAccess = new Boolean(true);
                } else {
                    for (ClassAccessor mappedSuperclass : getClassAccessor().getMappedSuperclasses()) {
                        if (MetadataHelper.havePersistenceAnnotationsDefined(MetadataHelper.getFields(mappedSuperclass.getJavaClass()))) {
                            // We have persistence annotations defined on a 
                            // field from a mapped superclass, set the access 
                            // to FIELD.
                            m_usesPropertyAccess = new Boolean(false);
                            break;
                        } else if (MetadataHelper.havePersistenceAnnotationsDefined(MetadataHelper.getDeclaredMethods(mappedSuperclass.getJavaClass()))) {
                            // We have persistence annotations defined on a 
                            // method from a mapped superclass, set the access 
                            // to FIELD.
                            m_usesPropertyAccess = new Boolean(true);
                            break;
                        }
                    }
                
                    // We still found nothing ... we should throw an exception 
                    // here, but for now, set the access to PROPERTY. The user 
                    // will eventually get an exception saying there is no 
                    // primary key set if property access is not actually the
                    // case.
                    if (m_usesPropertyAccess == null) {
                        m_usesPropertyAccess = new Boolean(true);
                    }
                }
            }
        
            return m_usesPropertyAccess;
        }
    }
    
    /**
     * INTERNAL:
     * Indicates if the strategy on the descriptor's inheritance policy is 
     * SINGLE_TABLE.
     */
    public boolean usesSingleTableInheritanceStrategy() {
        return ! usesJoinedInheritanceStrategy();
    }
    
    /**
     * INTERNAL:
     */
    public void useTimestampLockingPolicy(DatabaseField field) {
        useVersionLockingPolicy(new TimestampLockingPolicy(field));
    }
    
    /**
     * INTERNAL:
     */
    public void useVersionLockingPolicy(DatabaseField field) {
        useVersionLockingPolicy(new VersionLockingPolicy(field));
    }
    
    /**
     * INTERNAL:
     */
    protected void useVersionLockingPolicy(VersionLockingPolicy policy) {
        policy.storeInObject();
        setOptimisticLockingPolicy(policy);
    }
    
    /**
     * INTERNAL:
     * This method is used only to validate id fields that were found on a
     * pk class were also found on the entity.
     */
    public void validatePKClassId(String attributeName, Type type) {
        if (m_pkClassIDs.containsKey(attributeName))  {
            Type expectedType =  m_pkClassIDs.get(attributeName);
            
            if (type == expectedType) {
                m_pkClassIDs.remove(attributeName);
            } else {
                getValidator().throwInvalidCompositePKAttribute(getJavaClass(), getPKClassName(), attributeName, expectedType, type);
            }
        }
    }
}
