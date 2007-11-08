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

import javax.persistence.SequenceGenerator;
import javax.persistence.TableGenerator;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataConstants;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataDescriptor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataProcessor;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.sequencing.MetadataTableGenerator;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.sequencing.MetadataSequenceGenerator;

/**
 * An relational accessor.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public abstract class NonRelationshipAccessor extends MetadataAccessor {
    /**
     * INTERNAL:
     */
    public NonRelationshipAccessor(MetadataAccessibleObject accessibleObject, MetadataProcessor processor, MetadataDescriptor descriptor) {
        super(accessibleObject, processor, descriptor);
    }
    
    /**
     * INTERNAL:
     */
    public NonRelationshipAccessor(MetadataAccessibleObject accessibleObject, ClassAccessor classAccessor) {
        super(accessibleObject, classAccessor);
    }
    
    /**
     * INTERNAL: (Overridden in XMLClassAccessor and XMLBasicAccessor)
	 * Process a @SequenceGenerator into a common metadata sequence generator.
     */
    protected void processSequenceGenerator() {
        SequenceGenerator sequenceGenerator = getAnnotation(SequenceGenerator.class);
        
        if (sequenceGenerator != null) {
            // Ask the common processor to process what we found.
            processSequenceGenerator(new MetadataSequenceGenerator(sequenceGenerator, getJavaClassName()));
        }
    }
    
    /**
     * INTERNAL:
     * Process a MetadataSequenceGenerator and add it to the project.
     */
    protected void processSequenceGenerator(MetadataSequenceGenerator sequenceGenerator) {
        // Check if the sequence generator name uses a reserved name.
        String name = sequenceGenerator.getName();
        
         if (name.equals(MetadataConstants.DEFAULT_TABLE_GENERATOR)) {
            m_validator.throwSequenceGeneratorUsingAReservedName(MetadataConstants.DEFAULT_TABLE_GENERATOR, sequenceGenerator.getLocation());
        } else if (name.equals(MetadataConstants.DEFAULT_IDENTITY_GENERATOR)) {
            m_validator.throwSequenceGeneratorUsingAReservedName(MetadataConstants.DEFAULT_IDENTITY_GENERATOR, sequenceGenerator.getLocation());
        }
            
        // Conflicting means that they do not have all the same values.
        if (m_project.hasConflictingSequenceGenerator(sequenceGenerator)) {
            MetadataSequenceGenerator otherSequenceGenerator = m_project.getSequenceGenerator(name);
            if (sequenceGenerator.loadedFromAnnotations() && otherSequenceGenerator.loadedFromXML()) {
                // WIP - should log a warning that we are ignoring this table generator.
                return;
            } else {
                m_validator.throwConflictingSequenceGeneratorsSpecified(name, sequenceGenerator.getLocation(), otherSequenceGenerator.getLocation());
            }
        }
            
        if (m_project.hasTableGenerator(name)) {
            MetadataTableGenerator otherTableGenerator = m_project.getTableGenerator(name);
            m_validator.throwConflictingSequenceAndTableGeneratorsSpecified(name, sequenceGenerator.getLocation(), otherTableGenerator.getLocation());
        }
            
        for (MetadataTableGenerator otherTableGenerator : m_project.getTableGenerators()) {
            if (otherTableGenerator.getPkColumnValue().equals(sequenceGenerator.getSequenceName())) {
                // generator name will be used instead of an empty sequence name / pk column name
                if(otherTableGenerator.getPkColumnValue().length() > 0) {
                    m_validator.throwConflictingSequenceNameAndTablePkColumnValueSpecified(sequenceGenerator.getSequenceName(), sequenceGenerator.getLocation(), otherTableGenerator.getLocation());
                }
            }
        }
        
        m_project.addSequenceGenerator(sequenceGenerator);
    }
    
    /**
     * INTERNAL: (Overridden in XMLClassAccessor and XMLBasicAccessor)
	 * Process a @TableGenerator into a common metadata table generator.
     */
    protected void processTableGenerator() {
        TableGenerator tableGenerator = getAnnotation(TableGenerator.class);
        
        if (tableGenerator != null) {
            // Ask the common processor to process what we found.
            processTableGenerator(new MetadataTableGenerator(tableGenerator, getJavaClassName()));
        }
    } 
    
    /**
     * INTERNAL:
     * Process a MetadataTableGenerator and add it to the project.
     */     
    protected void processTableGenerator(MetadataTableGenerator tableGenerator) {
        // Check if the table generator name uses a reserved name.
        String name = tableGenerator.getName();
        
        if (name.equals(MetadataConstants.DEFAULT_SEQUENCE_GENERATOR)) {
            m_validator.throwTableGeneratorUsingAReservedName(MetadataConstants.DEFAULT_SEQUENCE_GENERATOR, tableGenerator.getLocation());
        } else if (name.equals(MetadataConstants.DEFAULT_IDENTITY_GENERATOR)) {
            m_validator.throwTableGeneratorUsingAReservedName(MetadataConstants.DEFAULT_IDENTITY_GENERATOR, tableGenerator.getLocation());
        }

        // Conflicting means that they do not have all the same values.
        if (m_project.hasConflictingTableGenerator(tableGenerator)) {
            MetadataTableGenerator otherTableGenerator = m_project.getTableGenerator(name);
            if (tableGenerator.loadedFromAnnotations() && otherTableGenerator.loadedFromXML()) {
                // WIP - should log a warning that we are ignoring this table generator.
                return;
            } else {
                m_validator.throwConflictingTableGeneratorsSpecified(name, tableGenerator.getLocation(), otherTableGenerator.getLocation());
            }
        }
        
        if (m_project.hasSequenceGenerator(tableGenerator.getName())) {
            MetadataSequenceGenerator otherSequenceGenerator = m_project.getSequenceGenerator(name);
            m_validator.throwConflictingSequenceAndTableGeneratorsSpecified(name, otherSequenceGenerator.getLocation(), tableGenerator.getLocation());
        }
            
        for (MetadataSequenceGenerator otherSequenceGenerator : m_project.getSequenceGenerators()) {
            if (otherSequenceGenerator.getSequenceName().equals(tableGenerator.getPkColumnValue())) {
                // generator name will be used instead of an empty sequence name / pk column name
                if(otherSequenceGenerator.getSequenceName().length() > 0) {
                    m_validator.throwConflictingSequenceNameAndTablePkColumnValueSpecified(otherSequenceGenerator.getSequenceName(), otherSequenceGenerator.getLocation(), tableGenerator.getLocation());
                }
            }
        }
            
        // Add the table generator to the descriptor metadata.
        m_project.addTableGenerator(tableGenerator);    
    }
}
