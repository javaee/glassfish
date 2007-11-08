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

import java.util.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.indirection.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.descriptors.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <h2>Purpose</h2>:
 * BasicIndirectionPolicy implements the behavior necessary for a
 * a ForeignReferenceMapping (or TransformationMapping) to
 * use ValueHolders to delay the reading of objects from the database
 * until they are actually needed.
 *
 * @see ForeignReferenceMapping
 * @author Mike Norman
 * @since TOPLink/Java 2.5
 */
public class BasicIndirectionPolicy extends IndirectionPolicy {

    /**
     * INTERNAL:
     * Construct a new indirection policy.
     */
    public BasicIndirectionPolicy() {
        super();
    }

    /**
     * INTERNAL:
     *    Return a backup clone of the attribute.
     */
    public Object backupCloneAttribute(Object attributeValue, Object clone, Object backup, UnitOfWorkImpl unitOfWork) {
        //no need to check if the attribute is a valueholder because closeAttribute
        // should always be called first
        ValueHolderInterface valueHolder = (ValueHolderInterface)attributeValue;// cast the value
        ValueHolder result = new ValueHolder();

        // delay instantiation until absolutely necessary
        if ((!(valueHolder instanceof UnitOfWorkValueHolder)) || valueHolder.isInstantiated()) {
            result.setValue(super.backupCloneAttribute(valueHolder.getValue(), clone, backup, unitOfWork));
        } else {
            ((UnitOfWorkValueHolder)valueHolder).setBackupValueHolder(result);
        }

        return result;
    }

    /**
     * INTERNAL:
     *    Return a clone of the attribute.
     *  @param buildDirectlyFromRow indicates that we are building the clone
     *  directly from a row as opposed to building the original from the
     *  row, putting it in the shared cache, and then cloning the original.
     */
    public Object cloneAttribute(Object attributeValue, Object original, Object clone, UnitOfWorkImpl unitOfWork, boolean buildDirectlyFromRow) {
        ValueHolderInterface valueHolder = (ValueHolderInterface) attributeValue;
        ValueHolderInterface result;
        
        if (!buildDirectlyFromRow && unitOfWork.isOriginalNewObject(original)) {
            // CR#3156435 Throw a meaningful exception if a serialized/dead value holder is detected.
            // This can occur if an existing serialized object is attempt to be registered as new.
            if ((valueHolder instanceof DatabaseValueHolder)
                    && (! ((DatabaseValueHolder) valueHolder).isInstantiated())
                    && (((DatabaseValueHolder) valueHolder).getSession() == null)
                    && (! ((DatabaseValueHolder) valueHolder).isSerializedRemoteUnitOfWorkValueHolder())) {
                throw DescriptorException.attemptToRegisterDeadIndirection(original, getMapping());
            }
            if (this.getMapping().getRelationshipPartner() == null) {
                result = new ValueHolder();
                result.setValue(this.getMapping().buildCloneForPartObject(valueHolder.getValue(), original, clone, unitOfWork, false));
            } else {
                //if I have a relationsip partner trigger the indiretion so that the value will be inserted
                // because of this call the entire tree should be recursively cloned
                AbstractRecord row = null;
                if (valueHolder instanceof DatabaseValueHolder) {
                    row = ((DatabaseValueHolder)valueHolder).getRow();
                }
                result = this.getMapping().createUnitOfWorkValueHolder(valueHolder, original, clone, row, unitOfWork, buildDirectlyFromRow);

                Object newObject = this.getMapping().buildCloneForPartObject(valueHolder.getValue(), original, clone, unitOfWork, false);
                ((UnitOfWorkValueHolder)result).privilegedSetValue(newObject);
                ((UnitOfWorkValueHolder)result).setInstantiated();
            }
        } else {
            AbstractRecord row = null;
            if (valueHolder instanceof DatabaseValueHolder) {
                row = ((DatabaseValueHolder)valueHolder).getRow();
            }
            result = this.getMapping().createUnitOfWorkValueHolder(valueHolder, original, clone, row, unitOfWork, buildDirectlyFromRow);
        }
        return result;
    }

    /**
     * INTERNAL:
     *    Return the primary key for the reference object (i.e. the object
     * object referenced by domainObject and specified by mapping).
     * This key will be used by a RemoteValueHolder.
     */
    public Vector extractPrimaryKeyForReferenceObject(Object referenceObject, AbstractSession session) {
        if (this.objectIsInstantiated(referenceObject)) {
            return super.extractPrimaryKeyForReferenceObject(((ValueHolderInterface)referenceObject).getValue(), session);
        } else {
            return this.getOneToOneMapping().extractPrimaryKeysForReferenceObjectFromRow(this.extractReferenceRow(referenceObject));
        }
    }

    /**
     * INTERNAL:
     *    Return the reference row for the reference object.
     * This allows the new row to be built without instantiating
     * the reference object.
     * Return null if the object has already been instantiated.
     */
    public AbstractRecord extractReferenceRow(Object referenceObject) {
        if (this.objectIsInstantiated(referenceObject)) {
            return null;
        } else {
            return ((DatabaseValueHolder)referenceObject).getRow();
        }
    }

    /**
     * INTERNAL:
     * Return the original indirection object for a unit of work indirection object.
     * This is used when building a new object from the unit of work when the original fell out of the cache.
     */
    public Object getOriginalIndirectionObject(Object unitOfWorkIndirectionObject, AbstractSession session) {
        if (unitOfWorkIndirectionObject instanceof UnitOfWorkValueHolder) {
            ValueHolderInterface valueHolder = ((UnitOfWorkValueHolder)unitOfWorkIndirectionObject).getWrappedValueHolder();
            if ((valueHolder != null) && (valueHolder instanceof DatabaseValueHolder)) {
                ((DatabaseValueHolder)valueHolder).releaseWrappedValueHolder();
            }
            return valueHolder;
        } else {
            return unitOfWorkIndirectionObject;
        }
    }
   
    /**
     * INTERNAL:
     * Return the "real" attribute value, as opposed to any wrapper.
     * This will trigger the wrapper to instantiate the value.
     */
    public Object getRealAttributeValueFromObject(Object object, Object attribute) {
        // Changed for CR 4245. Use a static reference instead of .class
        if (ClassConstants.ValueHolderInterface_Class.isAssignableFrom(attribute.getClass())) {
            return ((ValueHolderInterface)attribute).getValue();
        } else {
            return attribute;
        }
    }

    /**
     * INTERNAL:
     * Iterate over the specified attribute value,
     */
    public void iterateOnAttributeValue(DescriptorIterator iterator, Object attributeValue) {
        iterator.iterateValueHolderForMapping((ValueHolderInterface)attributeValue, this.getMapping());
    }

    /**
     * INTERNAL:
     * Return the null value of the appropriate attribute. That is, the
     * field from the database is NULL, return what should be
     * placed in the object's attribute as a result.
     * In this case, return an empty ValueHolder.
     */
    public Object nullValueFromRow() {
        return new ValueHolder();
    }

    /**
     * INTERNAL:
     * Return whether the specified object is instantiated.
     */
    public boolean objectIsInstantiated(Object object) {
        return ((ValueHolderInterface)object).isInstantiated();
    }

    /**
     * INTERNAL:
     * Set the value of the appropriate attribute of target to attributeValue.
     * In this case, place the value inside the target's ValueHolder.
     */
    public void setRealAttributeValueInObject(Object target, Object attributeValue) {
        ValueHolderInterface holder = (ValueHolderInterface)this.getMapping().getAttributeValueFromObject(target);
        if (holder == null) {
            holder = new ValueHolder(attributeValue);
        } else {
            holder.setValue(attributeValue); 
        }
        super.setRealAttributeValueInObject(target, holder);
    }
    

    /**
     * INTERNAL:
     *    Return whether the type is appropriate for the indirection policy.
     * In this case, the attribute type MUST be ValueHolderInterface.
     */
    protected boolean typeIsValid(Class attributeType) {
        return attributeType == ClassConstants.ValueHolderInterface_Class ||
            attributeType == ClassConstants.WeavedAttributeValueHolderInterface_Class;
    }

    /**
     * INTERNAL:
     * Verify that the value of the attribute within an instantiated object
     * is of the appropriate type for the indirection policy.
     * In this case, the attribute must be non-null and it must be a
     * ValueHolderInterface.
     * If the value is null return a new indirection object to be used for the attribute.
     */
    public Object validateAttributeOfInstantiatedObject(Object attributeValue) {
        // PERF: If the value is null, create a new value holder instance for the attribute value,
        // this allows for indirection attributes to not be instantiated in the constructor as they
        // are typically replaced when reading or cloning so is very inefficent to initialize.
        if (attributeValue == null) {
            return new ValueHolder();
        }
        if (!(attributeValue instanceof ValueHolderInterface)) {
            throw DescriptorException.valueHolderInstantiationMismatch(attributeValue, this.getMapping());
        }
        return attributeValue;
    }

    /**
     * INTERNAL:
     *    Verify that attributeType is correct for the
     * indirection policy. If it is incorrect, add an exception to the
     * integrity checker.
     * In this case, the attribute type MUST be ValueHolderInterface.
     */
    public void validateDeclaredAttributeType(Class attributeType, IntegrityChecker checker) throws DescriptorException {
        super.validateDeclaredAttributeType(attributeType, checker);
        if (!this.typeIsValid(attributeType)) {
            checker.handleError(DescriptorException.attributeAndMappingWithIndirectionMismatch(this.getMapping()));
        }
    }

    /**
     * INTERNAL:
     *    Verify that getter returnType is correct for the
     * indirection policy. If it is incorrect, add an exception
     * to the integrity checker.
     * In this case, the return type MUST be ValueHolderInterface.
     */
    public void validateGetMethodReturnType(Class returnType, IntegrityChecker checker) throws DescriptorException {
        super.validateGetMethodReturnType(returnType, checker);
        if (!this.typeIsValid(returnType)) {
            checker.handleError(DescriptorException.returnAndMappingWithIndirectionMismatch(this.getMapping()));
        }
    }

    /**
     * INTERNAL:
     *    Verify that setter parameterType is correct for the
     * indirection policy. If it is incorrect, add an exception
     * to the integrity checker.
     * In this case, the parameter type MUST be ValueHolderInterface.
     */
    public void validateSetMethodParameterType(Class parameterType, IntegrityChecker checker) throws DescriptorException {
        super.validateSetMethodParameterType(parameterType, checker);
        if (!this.typeIsValid(parameterType)) {
            checker.handleError(DescriptorException.parameterAndMappingWithIndirectionMismatch(this.getMapping()));
        }
    }

    /**
     * INTERNAL:
     * Return the value to be stored in the object's attribute.
     *    This value is determined by the query.
     * In this case, wrap the query in a ValueHolder for later invocation.
     */
    public Object valueFromQuery(ReadQuery query, AbstractRecord row, AbstractSession session) {
        return new QueryBasedValueHolder(query, row, session);
    }

    /**
     * INTERNAL:
     * Return the value to be stored in the object's attribute.
     *    This value is determined by the row.
     * In this case, simply wrap the object in a ValueHolder.
     */
    public Object valueFromRow(Object object) {
        return new ValueHolder(object);
    }
}
