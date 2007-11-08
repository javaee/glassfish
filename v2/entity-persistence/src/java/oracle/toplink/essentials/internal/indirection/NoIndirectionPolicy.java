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
package oracle.toplink.essentials.internal.indirection;

import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <h2>Purpose</h2>:
 * NoIndirectionPolicy implements the behavior necessary for a
 * a ForeignReferenceMapping (or TransformationMapping) to
 * directly use domain objects, as opposed to ValueHolders.
 *
 * @see ForeignReferenceMapping
 * @author Mike Norman
 * @since TOPLink/Java 2.5
 */
public class NoIndirectionPolicy extends IndirectionPolicy {

    /**
     * INTERNAL:
     * Construct a new indirection policy.
     */
    public NoIndirectionPolicy() {
        super();
    }

    /**
     * INTERNAL:
     *    Return a clone of the attribute.
     *  @param buildDirectlyFromRow indicates that we are building the clone directly
     *  from a row as opposed to building the original from the row, putting it in
     *  the shared cache, and then cloning the original.
     */
    public Object cloneAttribute(Object attributeValue, Object original, Object clone, UnitOfWorkImpl unitOfWork, boolean buildDirectlyFromRow) {
        // Since valueFromRow was called with the UnitOfWork, attributeValue
        // is already a registered result.
        if (buildDirectlyFromRow) {
            return attributeValue;
        }
        boolean isExisting = unitOfWork.isObjectRegistered(clone) && (!(unitOfWork.isOriginalNewObject(original)));
        return this.getMapping().buildCloneForPartObject(attributeValue, original, clone, unitOfWork, isExisting);
    }

    /**
     * INTERNAL:
     *    Return whether the collection type is appropriate for the indirection policy.
     * In this case, the type MUST be a Vector (or, in the case of jdk1.2,
     * Collection or Map).
     */
    protected boolean collectionTypeIsValid(Class collectionType) {
        return getCollectionMapping().getContainerPolicy().isValidContainerType(collectionType);
    }

    /**
     * INTERNAL:
     *    Return the reference row for the reference object.
     * This allows the new row to be built without instantiating
     * the reference object.
     * Return null if the object has already been instantiated.
     */
    public AbstractRecord extractReferenceRow(Object referenceObject) {
        return null;
    }

    /**
     * INTERNAL:
     *    Return the original indirection object for a unit of work indirection object.
     */
    public Object getOriginalIndirectionObject(Object unitOfWorkIndirectionObject, AbstractSession session) {
        // This code appears broken, but actually is unreachable because
        // only called when indirection is true.
        return unitOfWorkIndirectionObject;
    }

    /**
     * INTERNAL:
     * Return the "real" attribute value, as opposed to any wrapper.
     * This will trigger the wrapper to instantiate the value.
     */
    public Object getRealAttributeValueFromObject(Object object, Object attribute) {
        return attribute;
    }

    /**
     * INTERNAL:
     * Return the null value of the appropriate attribute. That is, the
     * field from the database is NULL, return what should be
     * placed in the object's attribute as a result.
     */
    public Object nullValueFromRow() {
        return null;
    }

    /**
     * INTERNAL:
     * Return whether the specified object is instantiated.
     */
    public boolean objectIsInstantiated(Object object) {
        return true;
    }

    /**
     * INTERNAL:
     *    Return whether the type is appropriate for the indirection policy.
     * In this case, the attribute type CANNOT be ValueHolderInterface.
     */
    protected boolean typeIsValid(Class attributeType) {
        return attributeType != ClassConstants.ValueHolderInterface_Class;
    }

    /**
     * INTERNAL:
     *    Return whether the indirection policy actually uses indirection.
     * Here, we must reply false.
     */
    public boolean usesIndirection() {
        return false;
    }

    /**
     * INTERNAL:
     *    Verify that attributeType is correct for the
     * indirection policy. If it is incorrect, add an exception to the
     * integrity checker.
     * In this case, the attribute type CANNOT be ValueHolderInterface.
     */
    public void validateDeclaredAttributeType(Class attributeType, IntegrityChecker checker) throws DescriptorException {
        super.validateDeclaredAttributeType(attributeType, checker);
        if (!this.typeIsValid(attributeType)) {
            checker.handleError(DescriptorException.attributeAndMappingWithoutIndirectionMismatch(this.getMapping()));
        }
    }

    /**
     * INTERNAL:
     *    Verify that attributeType is an appropriate collection type for the
     * indirection policy. If it is incorrect, add an exception to the integrity checker.
     * In this case, the type MUST be a Vector (or, in the case of jdk1.2,
     * Collection or Map).
     */
    public void validateDeclaredAttributeTypeForCollection(Class attributeType, IntegrityChecker checker) throws DescriptorException {
        super.validateDeclaredAttributeTypeForCollection(attributeType, checker);
        if (!this.collectionTypeIsValid(attributeType)) {
            checker.handleError(DescriptorException.attributeTypeNotValid(this.getCollectionMapping()));
        }
    }

    /**
     * INTERNAL:
     *    Verify that getter returnType is correct for the
     * indirection policy. If it is incorrect, add an exception
     * to the integrity checker.
     * In this case, the return type CANNOT be ValueHolderInterface.
     */
    public void validateGetMethodReturnType(Class returnType, IntegrityChecker checker) throws DescriptorException {
        super.validateGetMethodReturnType(returnType, checker);
        if (!this.typeIsValid(returnType)) {
            checker.handleError(DescriptorException.returnAndMappingWithoutIndirectionMismatch(this.getMapping()));
        }
    }

    /**
     * INTERNAL:
     *    Verify that getter returnType is an appropriate collection type for the
     * indirection policy. If it is incorrect, add an exception to the integrity checker.
     * In this case, the type MUST be a Vector (or, in the case of jdk1.2,
     * Collection or Map).
     */
    public void validateGetMethodReturnTypeForCollection(Class returnType, IntegrityChecker checker) throws DescriptorException {
        super.validateGetMethodReturnTypeForCollection(returnType, checker);
        if (!this.collectionTypeIsValid(returnType)) {
            checker.handleError(DescriptorException.getMethodReturnTypeNotValid(getCollectionMapping()));
        }
    }

    /**
     * INTERNAL:
     *    Verify that setter parameterType is correct for the
     * indirection policy. If it is incorrect, add an exception
     * to the integrity checker.
     * In this case, the parameter type CANNOT be ValueHolderInterface.
     */
    public void validateSetMethodParameterType(Class parameterType, IntegrityChecker checker) throws DescriptorException {
        super.validateSetMethodParameterType(parameterType, checker);
        if (!this.typeIsValid(parameterType)) {
            checker.handleError(DescriptorException.parameterAndMappingWithoutIndirectionMismatch(this.getMapping()));
        }
    }

    /**
     * INTERNAL:
     *    Verify that setter parameterType is an appropriate collection type for the
     * indirection policy. If it is incorrect, add an exception to the integrity checker.
     * In this case, the type MUST be a Vector (or, in the case of jdk1.2,
     * Collection or Map).
     */
    public void validateSetMethodParameterTypeForCollection(Class parameterType, IntegrityChecker checker) throws DescriptorException {
        super.validateSetMethodParameterTypeForCollection(parameterType, checker);
        if (!this.collectionTypeIsValid(parameterType)) {
            checker.handleError(DescriptorException.setMethodParameterTypeNotValid(getCollectionMapping()));
        }
    }

    /**
     * INTERNAL:
     * Return the value to be stored in the object's attribute.
     *    This value is determined by the query.
     * In this case, simply execute the query and return its results.
     */
    public Object valueFromQuery(ReadQuery query, AbstractRecord row, AbstractSession session) {
        return session.executeQuery(query, row);
    }

    /**
     * INTERNAL:
     * Return the value to be stored in the object's attribute.
     *    This value is determined by the row.
     * In this case, simply return the object.
     */
    public Object valueFromRow(Object object) {
        return object;
    }
}
