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

import java.lang.reflect.*;

import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.mappings.DatabaseMapping;

/**
 * Validator class for the metadata processors. It defines the common 
 * validation exceptions used by the metadata processors.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class MetadataValidator  {    
    /**
     * INTERNAL:
     * This exception should be used to report a case where both fields and
     * properties of a class are annotated.
     */
    public void throwBothFieldsAndPropertiesAnnotatedException(Class javaClass) {
        throw ValidationException.bothFieldsAndPropertiesAnnotated(javaClass);
    }

    /**
     * INTERNAL:
     */
    public void throwCircularMappedByReferences(Class cls1, String attributeName1, Class cls2, String attributeName2) {
        throw ValidationException.circularMappedByReferences(cls1, attributeName1, cls2, attributeName2);
    }
    
    /**
     * INTERNAL:
     * This exception should be used to report a case where an embeddable
     * class is used by entity classes having conflicting access-type and
     * embeddable's access-type is determined by enclosing entity's access-type.
     * This is thrown to disallow different representation of the same
     * embeddable class.
     */
    public void throwConflictingAccessTypeInEmbeddable(Class embeddableClass) {
        throw ValidationException.conflictingAccessTypeForEmbeddable(embeddableClass);
    }

    /**
     * INTERNAL:
     */
    public void throwClassNotFoundWhileConvertingClassNames(String className, Exception exception) {
        throw ValidationException.classNotFoundWhileConvertingClassNames(className, exception);
    }
    
    /**
     * INTERNAL:
     */
    public void throwConflictingSequenceAndTableGeneratorsSpecified(String name, String sequenceGeneratorLocation, String tableGeneratorLocation) {
        throw ValidationException.conflictingSequenceAndTableGeneratorsSpecified(name, sequenceGeneratorLocation, tableGeneratorLocation);
    }
    
    /**
     * INTERNAL:
     */
    public void throwConflictingSequenceGeneratorsSpecified(String name, String location1, String location2) {
        throw ValidationException.conflictingSequenceGeneratorsSpecified(name, location1, location2);
    }
    
    /**
     * INTERNAL:
     */
    public void throwConflictingSequenceNameAndTablePkColumnValueSpecified(String name, String sequenceGeneratorLocation, String tableGeneratorLocation) {
        throw ValidationException.conflictingSequenceNameAndTablePkColumnValueSpecified(name, sequenceGeneratorLocation, tableGeneratorLocation);
    }
    
    /**
     * INTERNAL:
     */
    public void throwConflictingTableGeneratorsSpecified(String name, String location1, String location2) {
        throw ValidationException.conflictingTableGeneratorsSpecified(name, location1, location2);
    }
    
    /**
     * INTERNAL:
     */
    public void throwCouldNotFindMapKey(String fieldOrPropertyName, Class referenceClass, DatabaseMapping mapping) {
        throw ValidationException.couldNotFindMapKey(fieldOrPropertyName, referenceClass, mapping);
    }
    
    /**
     * INTERNAL:
     */
    public void throwEmbeddedIdAndIdFound(Class entityClass, String attributeName, String idAttributeName) {
        throw ValidationException.embeddedIdAndIdAnnotationFound(entityClass, attributeName, idAttributeName);
    }
    
    /**
     * INTERNAL:
     * This exception should be used to report a case where an entity uses
     * EmbeddedId, but there is no attribute specified in the embeddable class.
     * This is most likely caused by incorrect access-type of the embeddable
     * class.
     */
    public void throwEmbeddedIdHasNoAttributes(Class entityClass, Class embeddableClass, String accessType) {
        throw ValidationException.embeddedIdHasNoAttributes(entityClass, embeddableClass, accessType);
    }
    
    /**
     * INTERNAL:
     */
    public void throwErrorProcessingNamedQueryAnnotation(Class entityClass, String name, Exception exception) {
        throw ValidationException.errorProcessingNamedQueryAnnotation(entityClass, name, exception);
    }
    
    /**
     * INTERNAL:
     */
    public void throwExcessiveJoinColumnsSpecified(Class entityClass, Object element) {
        throw ValidationException.excessiveJoinColumnsSpecified((AnnotatedElement) element, entityClass);
    }
    
    /**
     * INTERNAL:
     */
    public void throwExcessivePrimaryKeyJoinColumnsSpecified(Class entityClass, AnnotatedElement element) {
        throw ValidationException.excessivePrimaryKeyJoinColumnsSpecified(element);
    }
    
    /**
     * INTERNAL:
     */
    public void throwIncompleteJoinColumnsSpecified(Class entityClass, Object element) {
        throw ValidationException.incompleteJoinColumnsSpecified((AnnotatedElement) element, entityClass);
    }  
    
    /**
     * INTERNAL:
     */
    public void throwIncompletePrimaryKeyJoinColumnsSpecified(Class entityClass, AnnotatedElement annotatedElement) {
        throw ValidationException.incompletePrimaryKeyJoinColumnsSpecified(annotatedElement);
    } 
    
    /**
     * INTERNAL:
     * This exception should be used to report a case where access-type that
     * is determined using XML is *different* from access-type determined
     * using annotations in the class.
     */
    public void throwIncorrectOverridingOfAccessType(Class javaClass, String xmlAccessType, String annotAccessType) {
        throw ValidationException.incorrectOverridingOfAccessType(javaClass, xmlAccessType, annotAccessType);
    }
    
    /**
     * INTERNAL:
     */
    public void throwInvalidCallbackMethod(Class listenerClass, String methodName) {
        throw ValidationException.invalidCallbackMethod(listenerClass, methodName);
    }
    
    /**
     * INTERNAL
     */
    public void throwInvalidCollectionTypeForRelationship(Class entityClass, Class rawClass, Object element) {
        throw ValidationException.invalidCollectionTypeForRelationship(entityClass, rawClass, element);
    }
    
    /**
     * INTERNAL:
     */
    public void throwInvalidCompositePKAttribute(Class entityClass, String pkClassName, String attributeName, Type expectedType, Type type) {
        throw ValidationException.invalidCompositePKAttribute(entityClass, pkClassName, attributeName, expectedType, type);
    }
    
    /**
     * INTERNAL:
     */
    public void throwInvalidCompositePKSpecification(Class entityClass, String idClassName) {
        throw ValidationException.invalidCompositePKSpecification(entityClass, idClassName);    
    }
    
    /**
     * INTERNAL:
     */
    public void throwInvalidEmbeddableAttribute(Class entityClass, String attributeName, Class embeddedClass, String embeddedAttributeName) {
        throw ValidationException.invalidEmbeddableAttribute(embeddedClass, embeddedAttributeName, entityClass, attributeName);
    }    
    
    /**
     * INTERNAL:
     * This exception should be used to report a case where the type of an
     * embedded field or property is not Embeddable.
     */
    public void throwInvalidEmbeddedAttribute(Class javaClass, String attributeName, Class embeddableClass) {
        throw ValidationException.invalidEmbeddedAttribute(javaClass, attributeName, embeddableClass);
    }
    
    /**
     * INTERNAL:
     */
    public void throwInvalidOrderByValue(Class entityClass, String propertyOrFieldName, Class referenceClass, String attributeName) {
        throw ValidationException.invalidOrderByValue(propertyOrFieldName, referenceClass, attributeName, entityClass);
    }
    
    /**
     * INTERNAL:
     */
    public void throwInvalidTypeForEnumeratedAttribute(Class entityClass, String attributeName, Class referenceClass) {
        throw ValidationException.invalidTypeForEnumeratedAttribute(attributeName, referenceClass, entityClass);
    }  
    
    /**
     * INTERNAL:
     */
    public void throwInvalidTypeForLOBAttribute(Class entityClass, String attributeName, Class referenceClass) {
        throw ValidationException.invalidTypeForLOBAttribute(attributeName, referenceClass, entityClass);
    }  
    
    /**
     * INTERNAL:
     */
    public void throwInvalidTypeForSerializedAttribute(Class entityClass, String attributeName, Class referenceClass) {
        throw ValidationException.invalidTypeForSerializedAttribute(attributeName, referenceClass, entityClass);   
    }  
    
    /**
     * INTERNAL:
     */
    public void throwInvalidTypeForTemporalAttribute(Class entityClass, String attributeName, Class referenceClass) {
        throw ValidationException.invalidTypeForTemporalAttribute(attributeName, referenceClass, entityClass);
    }
    
    /**
     * INTERNAL:
     */
    public void throwInvalidTypeForVersionAttribute(Class entityClass, String attributeName, Class lockingType) {
        throw ValidationException.invalidTypeForVersionAttribute(attributeName, lockingType, entityClass);
    }
    
    /**
     * INTERNAL:
     */
    public void throwInvalidMappingEncountered(Class entityClass, Class targetClass) {
    	throw ValidationException.invalidMapping(entityClass, targetClass);
    }
    
    /**
     * INTERNAL:
     */
     public void throwMappingAnnotationsAppliedToTransientAttribute(AnnotatedElement annotatedElement) {
        throw ValidationException.mappingAnnotationsAppliedToTransientAttribute(annotatedElement);
     }
     
    /**
     * INTERNAL:
     * This exception should be used when there is no mapping file found
     * found in class path.
     */
    public void throwMappingFileNotFound(String puName, String mappingFile) {
        throw ValidationException.mappingFileNotFound(puName, mappingFile);
    }
    
    /**
     * INTERNAL:
     */
    public void throwMultipleEmbeddedIdsFound(Class entityClass, String attributeName, String embeddedIdAttributeName) {
        throw ValidationException.multipleEmbeddedIdAnnotationsFound(entityClass, attributeName, embeddedIdAttributeName);
    }
    
    /**
     * INTERNAL:
     */
    public void throwNoCorrespondingSetterMethodDefined(Class entityClass, Method method) {
        throw ValidationException.noCorrespondingSetterMethodDefined(entityClass, method);
    }
    
    /**
     * INTERNAL:
     */
    public void throwNoMappedByAttributeFound(Class owningClass, String owningAttributeName, Class entityClass, String attributeName) {
        throw ValidationException.noMappedByAttributeFound(owningClass, owningAttributeName, entityClass, attributeName);
    }

    /**
      * INTERNAL:
      * This exception should be used to report use of non-entity class as target
      * of a relationship.
      */
    public void throwNonEntityTargetInRelationship(Class javaClass, Class targetEntity, AnnotatedElement annotatedElement) {
        throw ValidationException.nonEntityTargetInRelationship(javaClass, targetEntity, annotatedElement);
    }
    
    
    /**
     * INTERNAL:
     */  
    public void throwNonUniqueEntityName(String clsName1, String clsName2, String name) {
        throw ValidationException.nonUniqueEntityName(clsName1, clsName2, name);
    }
    
    /**
     * INTERNAL:
     * This exception should be used when there are multiple mapping files with
     * same name found in class path.
     */
    public void throwNonUniqueMappingFileName(String puName, String mf) {
        throw ValidationException.nonUniqueMappingFileName(puName, mf);
    }
    
    /**
     * INTERNAL:
     */
    public void throwNoPrimaryKeyAnnotationsFound(Class entityClass) {
        throw ValidationException.noPrimaryKeyAnnotationsFound(entityClass);   
    }

    /**
     * INTERNAL:
     */
    public void throwNoTemporalTypeSpecified(Class entityClass, String attributeName) {
        throw ValidationException.noTemporalTypeSpecified(attributeName, entityClass);
    }
    
    /**
     * INTERNAL:
     */
    public void throwOnlyOneGeneratedValueIsAllowed(Class entityClass, String existingField, String otherField) {
        throw ValidationException.onlyOneGeneratedValueIsAllowed(entityClass, existingField, otherField);
    }

    /**
     * INTERNAL:
     */
    public void throwRelationshipHasColumnSpecified(Class entityClass, String attributeName) {
        throw ValidationException.invalidColumnAnnotationOnRelationship(entityClass, attributeName);   
    }
    
    /**
     * INTERNAL:
     */  
    public void throwSequenceGeneratorUsingAReservedName(String location, String reservedName) {
        throw ValidationException.sequenceGeneratorUsingAReservedName(reservedName, location);
    }
    
    /**
     * INTERNAL:
     */  
    public void throwTableGeneratorUsingAReservedName(String location, String reservedName) {
        throw ValidationException.tableGeneratorUsingAReservedName(reservedName, location);
    }
    
    /**
     * INTERNAL:
     */  
    public void throwTablePerClassInheritanceNotSupported(Class cls) {
        throw ValidationException.tablePerClassInheritanceNotSupported(cls);
    }
    
    /**
     * INTERNAL:
     */
    public void throwUnableToDetermineClassForField(String attributeName, Class entityClass) {
        throw ValidationException.unableToDetermineClassForField(attributeName, entityClass);
    }
    
    /**
     * INTERNAL:
     */
    public void throwUnableToDetermineClassForProperty(String attributeName, Class entityClass) {
        throw ValidationException.unableToDetermineClassForProperty(attributeName, entityClass);
    }
    
    /**
     * INTERNAL:
     */
    public void throwUnableToDetermineTargetEntity(String attributeName, Class entityClass) {
        throw ValidationException.unableToDetermineTargetEntity(attributeName, entityClass);
    }

    /**
     * INTERNAL:
     */
    public void throwUniDirectionalOneToManyHasJoinColumnSpecified(String attributeName, Class entityClass) {
        throw ValidationException.uniDirectionalOneToManyHasJoinColumnAnnotations(attributeName, entityClass);   
    }
}
