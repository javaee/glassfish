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

import java.util.Map;
import java.util.List;
import java.util.StringTokenizer;

import javax.persistence.MapKey;
import javax.persistence.OrderBy;
import javax.persistence.JoinTable;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataLogger;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataConstants;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataDescriptor;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataJoinColumn;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataJoinColumns;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.ClassAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.tables.MetadataJoinTable;

import oracle.toplink.essentials.internal.helper.DatabaseField;

import oracle.toplink.essentials.mappings.CollectionMapping;
import oracle.toplink.essentials.mappings.ManyToManyMapping;

/**
 * An annotation defined relational collections accessor.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public abstract class CollectionAccessor extends RelationshipAccessor {
    /**
     * INTERNAL:
     */
    public CollectionAccessor(MetadataAccessibleObject accessibleObject, ClassAccessor classAccessor) {
        super(accessibleObject, classAccessor);
    }
    
    /**
     * INTERNAL:
     * 
     * Add the relation key fields to a many to many mapping.
     */
    protected void addManyToManyRelationKeyFields(MetadataJoinColumns joinColumns, ManyToManyMapping mapping, String defaultFieldName, MetadataDescriptor descriptor, boolean isSource) {
        // Set the right context level.
        String PK_CTX, FK_CTX;
        if (isSource) {
            PK_CTX = MetadataLogger.SOURCE_PK_COLUMN;
            FK_CTX = MetadataLogger.SOURCE_FK_COLUMN;
        } else {
            PK_CTX = MetadataLogger.TARGET_PK_COLUMN;
            FK_CTX = MetadataLogger.TARGET_FK_COLUMN;
        }
        
        for (MetadataJoinColumn joinColumn : processJoinColumns(joinColumns, descriptor)) {
            // If the pk field (referencedColumnName) is not specified, it 
            // defaults to the primary key of the referenced table.
            String defaultPKFieldName = descriptor.getPrimaryKeyFieldName();
            DatabaseField pkField = joinColumn.getPrimaryKeyField();
            pkField.setName(getName(pkField, defaultPKFieldName, PK_CTX));
            pkField.setTableName(descriptor.getPrimaryKeyTableName());
            
            // If the fk field (name) is not specified, it defaults to the 
            // name of the referencing relationship property or field of the 
            // referencing entity + "_" + the name of the referenced primary 
            // key column. If there is no such referencing relationship 
            // property or field in the entity (i.e., a join table is used), 
            // the join column name is formed as the concatenation of the 
            // following: the name of the entity + "_" + the name of the 
            // referenced primary key column.
            DatabaseField fkField = joinColumn.getForeignKeyField();
            String defaultFKFieldName = defaultFieldName + "_" + defaultPKFieldName;
            fkField.setName(getName(fkField, defaultFKFieldName, FK_CTX));
            // Target table name here is the join table name.
            // If the user had specified a different table name in the join
            // column, it is igored. Perhaps an error or warning should be
            // fired off.
            fkField.setTableName(mapping.getRelationTableQualifiedName());
            
            // Add a target relation key to the mapping.
            if (isSource) {
                mapping.addSourceRelationKeyField(fkField, pkField);
            } else {
                mapping.addTargetRelationKeyField(fkField, pkField);
            }
        }
    }
    
    /**
     * INTERNAL: (Overridden in XMLManyToManyAccessor and XMLOneToManyAccessor)
     * Process a @JoinTable.
     */
    protected MetadataJoinTable getJoinTable() {
        JoinTable joinTable = getAnnotation(JoinTable.class);
        return new MetadataJoinTable(joinTable, m_logger);
    }
    
    /**
     * INTERNAL: (Overridden in XMLManyToManyAccessor and XMLOneToManyAccessor)
     * 
	 * Method to return a map key for a collection mapping. Assumes hasMapKey()
     * has been called before asking for the map key name.
     */
    public String getMapKey() {
        if (isAnnotationPresent(MapKey.class)) {
            MapKey mapKey = getAnnotation(MapKey.class);
            return mapKey.name();
        } else {
            return "";
        }
    }
    
    /**
     * INTERNAL: (Overridden in XMLManyToManyAccessor and XMLOneToManyAccessor)
     * 
	 * Return the order by value on this accessor. Assumes hasOrderBy() has been
     * called before asking for the order by value.
     */
    public String getOrderBy() {
        OrderBy orderBy = getAnnotation(OrderBy.class);
        return orderBy.value();
    }
    
    /**
     * INTERNAL: (Override from MetadataAccessor)
     * 
     * If a targetEntity is specified in metadata, it will be set as the 
     * reference class, otherwise we will look to extract one from generics.
     */
    public Class getReferenceClass() {
        if (m_referenceClass == null) {
            m_referenceClass = getTargetEntity();
        
            if (m_referenceClass == void.class) {
                // This call will attempt to extract the reference class from generics.
                m_referenceClass = m_accessibleObject.getReferenceClassFromGeneric();
        
                if (m_referenceClass == null) {
                    // Throw an exception. A relationship accessor must have a 
                    // reference class either through generics or a specified
                    // target entity on the mapping metadata.
                    m_validator.throwUnableToDetermineTargetEntity(getAttributeName(), getJavaClass());
                } else {
                    // Log the defaulting contextual reference class.
                    m_logger.logConfigMessage(getLoggingContext(), getAnnotatedElement(), m_referenceClass);
                }
            } 
        }
        
        return m_referenceClass;
    }
    
    /**
     * INTERNAL: (Overridden in XMLManyToManyAccessor and XMLOneToManyAccessor)
     * 
	 * Method to check if this accessor has an @OrderBy.
     */
    public boolean hasOrderBy() {
        return isAnnotationPresent(OrderBy.class);
    }
    
    /**
     * INTERNAL:
     * 
	 * Return true if this accessor uses a Map.
     */
    public boolean isMapCollectionAccessor() {
        return getRawClass().equals(Map.class);
    }
    
    /**
     * INTERNAL:
     */
    protected void process(CollectionMapping mapping) {
        mapping.setIsReadOnly(false);
        mapping.setIsPrivateOwned(false);
        mapping.setAttributeName(getAttributeName());
        mapping.setReferenceClassName(getReferenceClassName());
        
        // Will check for PROPERTY access
        setAccessorMethods(mapping);

        // Process the cascade types.
        processCascadeTypes(mapping);
        
        // Process an OrderBy id there is one.
        processOrderBy(mapping);
        
        // Process a MapKey if there is one.
        String mapKey = processMapKey(mapping);
        
        // Set the correct indirection on the collection mapping.
        // ** Note the reference class or reference class name needs to be set 
        // on the mapping before setting the indirection policy.
        setIndirectionPolicy(mapping, mapKey);
    }
    
    /**
     * INTERNAL:
     * Process a MetadataJoinTable.
     */
    protected void processJoinTable(MetadataJoinTable joinTable, ManyToManyMapping mapping) {
        // Build the default table name
        String defaultName = m_descriptor.getPrimaryTableName() + "_" + getReferenceDescriptor().getPrimaryTableName();
        
        // Process any table defaults and log warning messages.
        processTable(joinTable, defaultName);
        
        // Set the table on the mapping.
        mapping.setRelationTable(joinTable.getDatabaseTable());
        
        // Add all the joinColumns (source foreign keys) to the mapping.
        String defaultSourceFieldName;
        if (getReferenceDescriptor().hasBiDirectionalManyToManyAccessorFor(getJavaClassName(), getAttributeName())) {
            defaultSourceFieldName = getReferenceDescriptor().getBiDirectionalManyToManyAccessor(getJavaClassName(), getAttributeName()).getAttributeName();
        } else {
            defaultSourceFieldName = m_descriptor.getAlias();
        }
        addManyToManyRelationKeyFields(joinTable.getJoinColumns(), mapping, defaultSourceFieldName, m_descriptor, true);
        
        // Add all the inverseJoinColumns (target foreign keys) to the mapping.
        String defaultTargetFieldName = getAttributeName();
        addManyToManyRelationKeyFields(joinTable.getInverseJoinColumns(), mapping, defaultTargetFieldName, getReferenceDescriptor(), false);
    }
    
    /**
     * INTERNAL:
     * Process a MapKey for a 1-M or M-M mapping. Will return the map key
     * method name that should be use, null otherwise.
     */
    protected String processMapKey(CollectionMapping mapping) {
        String mapKey = null;
        
        if (isMapCollectionAccessor()) {
            MetadataDescriptor referenceDescriptor = getReferenceDescriptor();
            String mapKeyValue = getMapKey();
            
            if (mapKeyValue.equals("") && referenceDescriptor.hasCompositePrimaryKey()) {
                // No persistent property or field name has been provided, and
                // the reference class has a composite primary key class. Let
                // it fall through to return null for the map key. Internally,
                // TopLink will use an instance of the composite primary key
                // class as the map key.
            } else {
                // A persistent property or field name may have have been 
                // provided. If one has not we will default to the primary
                // key of the reference class. The primary key cannot be 
                // composite at this point.
                String fieldOrPropertyName = getName(mapKeyValue, referenceDescriptor.getIdAttributeName(), getLogger().MAP_KEY_ATTRIBUTE_NAME);
    
                // Look up the referenceAccessor
                MetadataAccessor referenceAccessor = referenceDescriptor.getAccessorFor(fieldOrPropertyName);
        
                if (referenceAccessor == null) {
                    m_validator.throwCouldNotFindMapKey(fieldOrPropertyName, referenceDescriptor.getJavaClass(), mapping);
                }
        
                mapKey = referenceAccessor.getName();
            }
        }
        
        return mapKey;
    }
    
    /**
     * INTERNAL:
     * Process an order by value (if specified) for the given collection 
     * mapping. Order by specifies the ordering of the elements of a collection 
     * valued association at the point when the association is retrieved.
     * 
     * The syntax of the value ordering element is an orderby_list, as follows:
     * 
     * orderby_list ::= orderby_item [, orderby_item]*
     * orderby_item ::= property_or_field_name [ASC | DESC]
     * 
     * When ASC or DESC is not specified, ASC is assumed.
     * 
     * If the ordering element is not specified, ordering by the primary key
     * of the associated entity is assumed.
     * 
     * The property or field name must correspond to that of a persistent
     * property or field of the associated class. The properties or fields 
     * used in the ordering must correspond to columns for which comparison
     * operators are supported.
     */
    protected void processOrderBy(CollectionMapping mapping) {
        if (hasOrderBy()) {
            String orderBy = getOrderBy();
            MetadataDescriptor referenceDescriptor = getReferenceDescriptor();
            
            if (orderBy.equals("")) {
                // Default to the primary key field name(s).
                List<String> orderByAttributes = referenceDescriptor.getIdOrderByAttributeNames();
            
                if (referenceDescriptor.hasEmbeddedIdAttribute()) {
                    String embeddedIdAttributeName = referenceDescriptor.getEmbeddedIdAttributeName();
                
                    for (String orderByAttribute : orderByAttributes) {
                        mapping.addAggregateOrderBy(embeddedIdAttributeName, orderByAttribute, false);
                    }
                } else {
                    for (String orderByAttribute : orderByAttributes) {
                        mapping.addOrderBy(orderByAttribute, false);
                    }
                }
            } else {
                StringTokenizer commaTokenizer = new StringTokenizer(orderBy, ",");
            
                while (commaTokenizer.hasMoreTokens()) {
                    StringTokenizer spaceTokenizer = new StringTokenizer(commaTokenizer.nextToken());
                    String propertyOrFieldName = spaceTokenizer.nextToken();
                    MetadataAccessor referenceAccessor = referenceDescriptor.getAccessorFor(propertyOrFieldName);
                
                    if (referenceAccessor == null) {
                        m_validator.throwInvalidOrderByValue(getJavaClass(), propertyOrFieldName, referenceDescriptor.getJavaClass(), getName());
                    }

                    String attributeName = referenceAccessor.getAttributeName();                    
                    String ordering = (spaceTokenizer.hasMoreTokens()) ? spaceTokenizer.nextToken() : MetadataConstants.ASCENDING;

                    if (referenceAccessor.isEmbedded()) {
                        for (String orderByAttributeName : referenceDescriptor.getOrderByAttributeNames()) {
                            mapping.addAggregateOrderBy(attributeName, orderByAttributeName, ordering.equals(MetadataConstants.DESCENDING));        
                        }
                    } else {
                        mapping.addOrderBy(attributeName, ordering.equals(MetadataConstants.DESCENDING));    
                    }
                }
            }
        }
    } 
}
