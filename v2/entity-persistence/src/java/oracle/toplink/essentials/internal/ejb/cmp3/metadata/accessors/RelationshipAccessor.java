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
import java.util.ArrayList;

import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;

import oracle.toplink.essentials.mappings.DatabaseMapping;
import oracle.toplink.essentials.mappings.ForeignReferenceMapping;

import oracle.toplink.essentials.indirection.ValueHolderInterface;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataConstants;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataDescriptor;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataJoinColumn;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataJoinColumns;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.ClassAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;

/**
 * An relational accessor.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public abstract class RelationshipAccessor extends MetadataAccessor {
    protected Class m_referenceClass;
    
    /**
     * INTERNAL:
     */
    protected RelationshipAccessor(MetadataAccessibleObject accessibleObject, ClassAccessor classAccessor) {
        super(accessibleObject, classAccessor);
    }
    
    /**
     * INTERNAL:
     * 
     * Return the cascade types for this accessor. This method is supported by 
     * all relational accessors.
     */
    public abstract List<String> getCascadeTypes();
    
    /**
     * INTERNAL:
     * WIP: Probably should make cascade types into its own object eventually.
     */
    public ArrayList<String> getCascadeTypes(CascadeType[] cascadeTypes) {
        ArrayList<String> cTypes = new ArrayList<String>();
        
        for (CascadeType cascadeType : cascadeTypes) {
            cTypes.add(cascadeType.name());
		}
        
        return cTypes;
    }
    
    /**
     * INTERNAL: (Overridden in XMLOneToOneAccessor, XMLManyToManyAccessor and XMLOneToManyAccessor)
     * Process the @JoinColumns and @JoinColumn.
     */    
    protected MetadataJoinColumns getJoinColumns() {
        JoinColumn joinColumn = getAnnotation(JoinColumn.class);
        JoinColumns joinColumns = getAnnotation(JoinColumns.class);
        
        return new MetadataJoinColumns(joinColumns, joinColumn);
    }
    
    /**
     * INTERNAL:
     * 
     * Return the logging context for this accessor.
     */
    protected abstract String getLoggingContext();
    
    /**
     * INTERNAL:
     * 
     * Subclasses that support processing a mapped by should override this 
     * method, otherwise a runtime development exception is thrown for those 
     * accessors who call this method and don't implement it themselves.
     */
    public String getMappedBy() {
        throw new RuntimeException("Development exception. The accessor: [" + this + "] should not call the getMappedBy method unless it overrides it.");
    }
    
    /**
     * INTERNAL:
     * Method to return an owner mapping. It will tell the owner class to
     * process itself if it hasn't already done so.
     */
    protected DatabaseMapping getOwningMapping() {
        String ownerAttributeName = getMappedBy();
        MetadataDescriptor ownerDescriptor = getReferenceDescriptor();
        DatabaseMapping mapping = ownerDescriptor.getMappingForAttributeName(ownerAttributeName, this);
        
        // If no mapping was found, there is an error in the mappedBy field, 
        // therefore, throw an exception.
        if (mapping == null) {
            m_validator.throwNoMappedByAttributeFound(ownerDescriptor.getJavaClass(), ownerAttributeName, getJavaClass(), getAttributeName());
        }
        
        return mapping;
    }
    
    /**
      * INTERNAL:
      * Return the reference metadata descriptor for this accessor.
      * This method does additional checks to make sure that the target
      * entity is indeed an entity class.
      */
    public MetadataDescriptor getReferenceDescriptor() {
        MetadataDescriptor descriptor;
       
        try {
            descriptor = super.getReferenceDescriptor();
        } catch (Exception exception) {
            descriptor = null;
        }
       
        if (descriptor == null || descriptor.isEmbeddable() || descriptor.isEmbeddableCollection()) {
            m_validator.throwNonEntityTargetInRelationship(getJavaClass(), getReferenceClass(), getAnnotatedElement());
        }
       
        return descriptor;
    }
    
    /**
     * INTERNAL:
     * Return the target entity for this accessor. This method is supported by 
     * all relational accessors.
     */
    public abstract Class getTargetEntity();
    
    /**
     * INTERNAL:
	 * Method to check if an annotated element has a @JoinColumn.
     */
	public boolean hasJoinColumn() {
		return isAnnotationPresent(JoinColumn.class);
    }
    
    /**
     * INTERNAL:
	 * Method to check if an annotated element has a @JoinColumns.
     */
	public boolean hasJoinColumns() {
		return isAnnotationPresent(JoinColumns.class);
    }
    
    /**
     * INTERNAL: (Overridden in XMLOneToOneAccessor)
	 * Method to check if an annotated element has a @PrimaryKeyJoinColumns or
     * at the very least a @PrimaryKeyJoinColumn.
     */
	public boolean hasPrimaryKeyJoinColumns() {
		return isAnnotationPresent(PrimaryKeyJoinColumns.class) || isAnnotationPresent(PrimaryKeyJoinColumn.class);
    }
    
    /**
     * INTERNAL:
     * Return true if this accessor represents a 1-1 primary key relationship.
     */
	public boolean isOneToOnePrimaryKeyRelationship() {
        return isOneToOne() && hasPrimaryKeyJoinColumns();
    }
    
    /**
     * INTERNAL:
     */
    protected void processCascadeTypes(ForeignReferenceMapping mapping) {
        for (String cascadeType : getCascadeTypes()) {
			setCascadeType(cascadeType, mapping);
		}
        
        // Apply the persistence unit default cascade-persist if necessary.
        if (m_descriptor.isCascadePersist() && ! mapping.isCascadePersist()) {
        	setCascadeType(CascadeType.PERSIST.name(), mapping);
        }
    }
    
    /**
     * INTERNAL:
     * Process a @JoinColumns or @JoinColumn. Will look for association
     * overrides.
     */	
    protected List<MetadataJoinColumn> processJoinColumns() { 
        if (m_descriptor.hasAssociationOverrideFor(getAttributeName())) {
            return processJoinColumns(m_descriptor.getAssociationOverrideFor(getAttributeName()), getReferenceDescriptor());
        } else {
            return processJoinColumns(getJoinColumns(), getReferenceDescriptor());
        }
    }
    
    /**
     * INTERNAL:
     * 
     * Process MetadataJoinColumns.
     */	
    protected List<MetadataJoinColumn> processJoinColumns(MetadataJoinColumns joinColumns, MetadataDescriptor descriptor) { 
        // This call will add any defaulted columns as necessary.
        List<MetadataJoinColumn> jColumns = joinColumns.values(descriptor);
        
        if (descriptor.hasCompositePrimaryKey()) {
            // The number of join columns should equal the number of primary key fields.
            if (jColumns.size() != descriptor.getPrimaryKeyFields().size()) {
                m_validator.throwIncompleteJoinColumnsSpecified(getJavaClass(), getAnnotatedElement());
            }
            
            // All the primary and foreign key field names should be specified.
            for (MetadataJoinColumn jColumn : jColumns) {
                if (jColumn.isPrimaryKeyFieldNotSpecified() || jColumn.isForeignKeyFieldNotSpecified()) {
                    m_validator.throwIncompleteJoinColumnsSpecified(getJavaClass(), getAnnotatedElement());
                }
            }
        } else {
            if (jColumns.size() > 1) {
                m_validator.throwExcessiveJoinColumnsSpecified(getJavaClass(), getAnnotatedElement());
            }
        }
        
        return jColumns;
    }
    
    /**
     * INTERNAL:
     * Front end validation before actually processing the relationship 
     * accessor. The process() method should not be called directly.
     */
    public void processRelationship() {
        // The processing of this accessor may have been fast tracked through a 
        // non-owning relationship. If so, no processing is required.
        if (! isProcessed()) {
            if (m_descriptor.hasMappingForAttributeName(getAttributeName())) {
                // Only true if there is one that came from Project.xml
                m_logger.logWarningMessage(m_logger.IGNORE_MAPPING, this);
            } else {
                // If a @Column is specified then throw an exception.
                if (hasColumn()) {
                    m_validator.throwRelationshipHasColumnSpecified(getJavaClass(), getAttributeName());
                }
                
                // Process the relationship accessor only if the target entity
                // is not a ValueHolderInterface.
                if (getTargetEntity() == ValueHolderInterface.class || (getTargetEntity() == void.class && getReferenceClass() == ValueHolderInterface.class)) {
                    // do nothing ... I'm too lazy (or too stupid) to do the negation of this expression :-)
                } else { 
                    process();
                }
            }
            
            // Set its processing completed flag to avoid double processing.
            setIsProcessed();
        }
    }

    /**
     * INTERNAL:
     * Set the cascade type on a mapping.
     */
    protected void setCascadeType(String type, ForeignReferenceMapping mapping) {
        if (type.equals(MetadataConstants.CASCADE_ALL) || type.equals(CascadeType.ALL.name())) {
            mapping.setCascadeAll(true);
        } else if (type.equals(MetadataConstants.CASCADE_MERGE) || type.equals(CascadeType.MERGE.name())) {
            mapping.setCascadeMerge(true);
        } else if (type.equals(MetadataConstants.CASCADE_PERSIST) || type.equals(CascadeType.PERSIST.name())) {
            mapping.setCascadePersist(true);
        }  else if (type.equals(MetadataConstants.CASCADE_REFRESH) || type.equals(CascadeType.REFRESH.name())) {
            mapping.setCascadeRefresh(true);
        } else if (type.equals(MetadataConstants.CASCADE_REMOVE) || type.equals(CascadeType.REMOVE.name())) {
            mapping.setCascadeRemove(true);
        }
    }
}
