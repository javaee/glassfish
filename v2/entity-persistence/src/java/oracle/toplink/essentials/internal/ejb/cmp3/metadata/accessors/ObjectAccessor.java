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

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.ClassAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataJoinColumn;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataPrimaryKeyJoinColumn;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataDescriptor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataLogger;

import oracle.toplink.essentials.internal.helper.DatabaseField;
import oracle.toplink.essentials.internal.indirection.WeavedObjectBasicIndirectionPolicy;

import oracle.toplink.essentials.mappings.OneToOneMapping;

import java.util.List;

/**
 * A single object relationship accessor.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public abstract class ObjectAccessor extends RelationshipAccessor {
    protected ObjectAccessor(MetadataAccessibleObject accessibleObject, ClassAccessor classAccessor) {
        super(accessibleObject, classAccessor);
    }
    
    /**
     * INTERNAL: (Override from MetadataAccessor)
     * 
     * If a target entity is specified in metadata, it will be set as the 
     * reference class, otherwise we will use the raw class.
     */
    public Class getReferenceClass() {
        if (m_referenceClass == null) {
            m_referenceClass = getTargetEntity();
        
            if (m_referenceClass == void.class) {
                // Get the reference class from the accessible object and
                // log the defaulting contextual reference class.
                m_referenceClass = super.getReferenceClass();
                m_logger.logConfigMessage(getLoggingContext(), getAnnotatedElement(), m_referenceClass);
            } 
        }
        
        return m_referenceClass;
    }
    
    /**
     * INTERNAL:
     * Initialize a OneToOneMapping.
     */
    protected OneToOneMapping initOneToOneMapping() {
    	OneToOneMapping mapping = new OneToOneMapping();
        mapping.setIsReadOnly(false);
        mapping.setIsPrivateOwned(false);
        mapping.setIsOptional(isOptional());
        mapping.setAttributeName(getAttributeName());
        mapping.setReferenceClassName(getReferenceClassName());
        
        // If the global weave for value holders is true, the use the value
        // from usesIndirection. Otherwise, force it to false.
        boolean usesIndirection = (m_project.enableLazyForOneToOne()) ? usesIndirection() : false;
        if (usesIndirection && m_descriptor.usesPropertyAccess()) {
            mapping.setIndirectionPolicy(new WeavedObjectBasicIndirectionPolicy(getSetMethodName()));
        } else {
            mapping.setUsesIndirection(usesIndirection);
        }
        
        // Set the getter and setter methods if access is PROPERTY.
        setAccessorMethods(mapping);
        
        // Process the cascade types.
        processCascadeTypes(mapping);
        
        return mapping;
    }
    
    /**
     * INTERNAL:
     * Process the @JoinColumn(s) for the owning side of a one to one mapping.
     * The default pk and pk field names are used only with single primary key 
     * entities. The processor should never get as far as to use them with 
     * entities that have a composite primary key (validation exception will be 
     * thrown).
     */
    protected void processOneToOneForeignKeyRelationship(OneToOneMapping mapping) {         
        // If the pk field (referencedColumnName) is not specified, it 
        // defaults to the primary key of the referenced table.
        String defaultPKFieldName = getReferenceDescriptor().getPrimaryKeyFieldName();
        
        // If the fk field (name) is not specified, it defaults to the 
        // concatenation of the following: the name of the referencing 
        // relationship property or field of the referencing entity; "_"; 
        // the name of the referenced primary key column.
        String defaultFKFieldName = getUpperCaseAttributeName() + "_" + defaultPKFieldName;
            
        // Join columns will come from a @JoinColumn(s).
        List<MetadataJoinColumn> joinColumns = processJoinColumns();

        // Add the source foreign key fields to the mapping.
        for (MetadataJoinColumn joinColumn : joinColumns) {
            DatabaseField pkField = joinColumn.getPrimaryKeyField();
            pkField.setName(getName(pkField, defaultPKFieldName, MetadataLogger.PK_COLUMN));
            pkField.setTableName(getReferenceDescriptor().getPrimaryKeyTableName());
            
            DatabaseField fkField = joinColumn.getForeignKeyField();
            fkField.setName(getName(fkField, defaultFKFieldName, MetadataLogger.FK_COLUMN));
            // Set the table name if one is not already set.
            if (fkField.getTableName().equals("")) {
                fkField.setTableName(m_descriptor.getPrimaryTableName());
            }
            
            // Add a source foreign key to the mapping.
            mapping.addForeignKeyField(fkField, pkField);
            
            // If any of the join columns is marked read-only then set the 
            // mapping to be read only.
            if (fkField.isReadOnly()) {
                mapping.setIsReadOnly(true);
            }
        }
    }
    
    /**
     * INTERNAL:
     * Process the primary key join columns for the owning side of a one to one 
     * mapping. The default pk and pk field names are used only with single 
     * primary key entities. The processor should never get as far as to use 
     * them with entities that have a composite primary key (validation 
     * exception will be thrown).
     */
    protected void processOneToOnePrimaryKeyRelationship(OneToOneMapping mapping) {
        // Join columns will come from a @PrimaryKeyJoinColumn(s).
        MetadataDescriptor referenceDescriptor = getReferenceDescriptor();
        List<MetadataPrimaryKeyJoinColumn> primaryKeyJoinColumns = processPrimaryKeyJoinColumns(getPrimaryKeyJoinColumns(referenceDescriptor.getPrimaryTableName(), m_descriptor.getPrimaryTableName()));

        // Add the source foreign key fields to the mapping.
        for (MetadataPrimaryKeyJoinColumn primaryKeyJoinColumn : primaryKeyJoinColumns) {
            // The default primary key name is the primary key field name of the
            // referenced entity.
            DatabaseField pkField = primaryKeyJoinColumn.getPrimaryKeyField();
            pkField.setName(getName(pkField, referenceDescriptor.getPrimaryKeyFieldName(), m_logger.PK_COLUMN));
            
            // The default foreign key name is the primary key of the
            // referencing entity.
            DatabaseField fkField = primaryKeyJoinColumn.getForeignKeyField();
            fkField.setName(getName(fkField, m_descriptor.getPrimaryKeyFieldName(), m_logger.FK_COLUMN));
            
            // Add a source foreign key to the mapping.
            mapping.addForeignKeyField(fkField, pkField);
            
            // Mark the mapping read only
            mapping.setIsReadOnly(true);
        }
    }
    
    /**
     * INTERNAL:
     * Process the the correct metadata join column for the owning side of a 
     * one to one mapping.
     */
    protected void processOwningMappingKeys(OneToOneMapping mapping) {
        if (isOneToOnePrimaryKeyRelationship()) {
            processOneToOnePrimaryKeyRelationship(mapping);
        } else {
            processOneToOneForeignKeyRelationship(mapping);
        }
    }
}
