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
package oracle.toplink.essentials.internal.ejb.cmp3.xml;

import java.lang.reflect.AnnotatedElement;

import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataValidator;

/**
 * XML validator class.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class XMLValidator extends MetadataValidator  { 
    /**
     * INTERNAL
     */
    public XMLValidator() {}
    
    /**
     * INTERNAL
     */
    public void throwEmbeddedIdAndIdFound(Class entityClass, String attributeName, String idAttributeName) {
        throw ValidationException.embeddedIdAndIdElementFound(entityClass, attributeName, idAttributeName);
    }
    
    /**
     * INTERNAL
     */
    public void throwErrorProcessingNamedQueryElement(String queryName, Exception exception) {
        throw ValidationException.errorProcessingNamedQueryElement(queryName, exception);
    }
    
    /**
     * INTERNAL
     */
    public void throwExcessiveJoinColumnsSpecified(Class entityClass, Object element) {
        throw ValidationException.excessiveJoinColumnElementsSpecified((String) element, entityClass);
    }
    
    /**
     * INTERNAL
     */
    public void throwExcessivePrimaryKeyJoinColumnsSpecified(Class entityClass, AnnotatedElement annotatedElement) {
        throw ValidationException.excessivePrimaryKeyJoinColumnElementsSpecified(entityClass);
    }
    
    /**
     * INTERNAL
     */
    public void throwIncompleteJoinColumnsSpecified(Class entityClass, Object element) {
        throw ValidationException.incompleteJoinColumnElementsSpecified(element, entityClass);
    }
    
    /**
     * INTERNAL
     */
    public void throwIncompletePrimaryKeyJoinColumnsSpecified(Class entityClass, AnnotatedElement annotatedElement) {
        throw ValidationException.incompletePrimaryKeyJoinColumnElementsSpecified(entityClass);
    }
    
    /**
     * INTERNAL
     */
    public void throwMultipleEmbeddedIdsFound(Class entityClass, String attributeName, String embeddedIdAttributeName) {
        throw ValidationException.multipleEmbeddedIdElementsFound(entityClass, attributeName, embeddedIdAttributeName);
    }
    
    /**
     * INTERNAL
     */
    public void throwNoMappedByAttributeFound(Class owningClass, String owningAttributeName, Class entityClass, String attributeName) {
        // ignore, not applicable.
    }
    
    /**
     * INTERNAL
     */
    public void throwNoTemporalTypeSpecified(Class entityClass, String attributeName) {
        // WIP - copied from AnnotationsValidator ... might need to have its own ...
        throw ValidationException.noTemporalTypeSpecified(attributeName, entityClass);
    }
    
    /**
     * INTERNAL
     */
    public void throwPersistenceUnitMetadataConflict(String element) {
        throw ValidationException.persistenceUnitMetadataConflict(element);
    }
    
    /**
     * INTERNAL
     */
    public void throwRelationshipHasColumnSpecified(Class entityClass, String attributeName) {
        throw ValidationException.invalidColumnElementOnRelationship(entityClass, attributeName);
    }
    
    /**
     * INTERNAL:
     */  
    public void throwSequenceGeneratorUsingAReservedName(String document, String reservedName) {
        throw ValidationException.sequenceGeneratorUsingAReservedName(reservedName, document);
    }
    
    /**
     * INTERNAL:
     */  
    public void throwTableGeneratorUsingAReservedName(String document, String reservedName) {
        throw ValidationException.tableGeneratorUsingAReservedName(reservedName, document);
    }
    
    /**
     * INTERNAL
     */
    public void throwUniDirectionalOneToManyHasJoinColumnSpecified(String attributeName, Class entityClass) {
        throw ValidationException.uniDirectionalOneToManyHasJoinColumnElements(attributeName, entityClass);
    }
}
