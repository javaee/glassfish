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

import java.io.*;
import java.util.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.descriptors.*;
import oracle.toplink.essentials.internal.sessions.MergeManager;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <h2>Purpose</h2>:
 * An IndirectionPolicy acts as a 'rules' holder that determines
 * the behavior of a ForeignReferenceMapping (or TransformationMapping)
 * with respect to indirection, or lack thereof.
 * <p>
 * <h3>Description</h3>:
 * IndirectionPolicy is an abstract class that defines the protocol to be implemented by
 * subclasses so that the assorted DatabaseMappings can use an assortment of
 * indirection policies:<ul>
 * <li> no indirection policy (read everything from database)
 * <li> basic indirection policy (use ValueHolders)
 * <li> transparent indirection policy (collections only)
 * <li> proxy indirection policy (transparent 1:1 indirection using JDK 1.3's <CODE>Proxy</CODE>)
 * </ul>
 *
 * <p>
 * <h3>Responsibilities</h3>:
 *     <ul>
 *     <li>instantiate the various IndirectionPolicies
 *     </ul>
 * <p>
 *
 * @see ForeignReferenceMapping
 * @author Mike Norman
 * @since TOPLink/Java 2.5
 */
public abstract class IndirectionPolicy implements Cloneable, Serializable {
    protected DatabaseMapping mapping;

    /**
     * INTERNAL:
     * Construct a new indirection policy.
     */
    public IndirectionPolicy() {
        super();
    }

    /**
     * INTERNAL:
     *    Return a backup clone of the attribute.
     */
    public Object backupCloneAttribute(Object attributeValue, Object clone, Object backup, UnitOfWorkImpl unitOfWork) {
        return this.getMapping().buildBackupCloneForPartObject(attributeValue, clone, backup, unitOfWork);
    }

    /**
     * INTERNAL
     * Return true if the refresh shoud refresh on this mapping or not.
     */
    protected ReadObjectQuery buildCascadeQuery(MergeManager mergeManager) {
        ReadObjectQuery cascadeQuery = new ReadObjectQuery();
        if (mergeManager.shouldCascadeAllParts()) {
            cascadeQuery.cascadeAllParts();
            cascadeQuery.refreshIdentityMapResult();
        }
        if (mergeManager.shouldCascadePrivateParts() && getForeignReferenceMapping().isPrivateOwned()) {
            cascadeQuery.cascadePrivateParts();
            cascadeQuery.refreshIdentityMapResult();
        }

        return cascadeQuery;
    }

    /**
     * INTERNAL:
     * Clones itself.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * INTERNAL:
     *    Return a clone of the attribute.
     *  @param builtDirectlyFromRow indicates that we are building the clone
     *  directly from a row as opposed to building the original from the
     *  row, putting it in the shared cache, and then cloning the original.
     */
    public abstract Object cloneAttribute(Object attributeValue, Object original, Object clone, UnitOfWorkImpl unitOfWork, boolean buildDirectlyFromRow);

    /**
     * INTERNAL:
     *    Return the primary key for the reference object (i.e. the object
     * object referenced by domainObject and specified by mapping).
     * This key will be used by a RemoteValueHolder.
     */
    public Vector extractPrimaryKeyForReferenceObject(Object referenceObject, AbstractSession session) {
        return this.getOneToOneMapping().extractPrimaryKeysFromRealReferenceObject(referenceObject, session);
    }

    /**
     * INTERNAL:
     *    Return the reference row for the reference object.
     * This allows the new row to be built without instantiating
     * the reference object.
     * Return null if the object has already been instantiated.
     */
    public abstract AbstractRecord extractReferenceRow(Object referenceObject);

    /**
     * INTERNAL:
     * Reduce casting clutter....
     */
    protected CollectionMapping getCollectionMapping() {
        return (CollectionMapping)this.getMapping();
    }

    /**
     * INTERNAL:
     * Reduce casting clutter....
     */
    protected ForeignReferenceMapping getForeignReferenceMapping() {
        return (ForeignReferenceMapping)this.getMapping();
    }

    /**
     * INTERNAL:
     * Return the database mapping that uses the indirection policy.
     */
    public DatabaseMapping getMapping() {
        return mapping;
    }

    /**
     * INTERNAL:
     * Reduce casting clutter....
     */
    protected ObjectReferenceMapping getOneToOneMapping() {
        return (ObjectReferenceMapping)this.getMapping();
    }

    /**
     * INTERNAL:
     *    Return the original indirection object for a unit of work indirection object.
     */
    public abstract Object getOriginalIndirectionObject(Object unitOfWorkIndirectionObject, AbstractSession session);

    /**
     * INTERNAL:
     * Return the "real" attribute value, as opposed to any wrapper.
     * This will trigger the wrapper to instantiate the value.
     */
    public abstract Object getRealAttributeValueFromObject(Object object, Object attribute);

    /**
     * INTERNAL:
     * Given a proxy object, trigger the indirection and return the actual object represented by the proxy.
     * For non-proxy indirection, this method will simply return the object.
     */
    public static Object getValueFromProxy(Object value) {
        return value;
    }

    /**
     * INTERNAL:
     * Initialize the indirection policy (Do nothing by default)
     */
    public void initialize() {
    }

    /**
     * INTERNAL:
     *    Iterate over the specified attribute value,
     * heeding the settings in the iterator.
     */
    public void iterateOnAttributeValue(DescriptorIterator iterator, Object attributeValue) {
        if (attributeValue != null) {
            this.getMapping().iterateOnRealAttributeValue(iterator, attributeValue);
        }
    }

    /**
     * INTERNAL:
     *    Return the null value of the appropriate attribute. That is, the
     * field from the database is NULL, return what should be
     * placed in the object's attribute as a result.
     */
    public abstract Object nullValueFromRow();

    /**
     * INTERNAL:
     * Return whether the specified object is instantiated.
     */
    public abstract boolean objectIsInstantiated(Object object);

    /**
     * INTERNAL:
     * set the database mapping that uses the indirection policy.
     */
    public void setMapping(DatabaseMapping mapping) {
        this.mapping = mapping;
    }

    /**
     * INTERNAL:
     *    Set the value of the appropriate attribute of target to attributeValue.
     * In this case, simply place the value inside the target.
     */
    public void setRealAttributeValueInObject(Object target, Object attributeValue) {
        this.getMapping().setAttributeValueInObject(target, attributeValue);
    }

    /**
     * INTERNAL:
     *    Return whether the indirection policy actually uses indirection.
     * The default is true.
     */
    public boolean usesIndirection() {
        return true;
    }
    
    /**
     * INTERNAL:
     *    Return whether the indirection policy uses transparent indirection.
     * The default is false.
     */
    public boolean usesTransparentIndirection(){
        return false;
    }

    /**
     * INTERNAL:
     *    Verify that the value of the attribute within an instantiated object
     * is of the appropriate type for the indirection policy.
     * If it is incorrect, throw an exception.
     * If the value is null return a new indirection object to be used for the attribute.
     */
    public Object validateAttributeOfInstantiatedObject(Object attributeValue) throws DescriptorException {
        return attributeValue;
    }

    /**
     * INTERNAL:
     *    Verify that the container policy is compatible with the
     * indirection policy. If it is incorrect, add an exception to the
     * integrity checker.
     */
    public void validateContainerPolicy(IntegrityChecker checker) throws DescriptorException {
        // by default, do nothing
    }

    /**
     * INTERNAL:
     *    Verify that attributeType is correct for the
     * indirection policy. If it is incorrect, add an exception to the
     * integrity checker.
     */
    public void validateDeclaredAttributeType(Class attributeType, IntegrityChecker checker) throws DescriptorException {
        // by default, do nothing
    }

    /**
     * INTERNAL:
     *    Verify that attributeType is an appropriate collection type for the
     * indirection policy. If it is incorrect, add an exception to the integrity checker.
     */
    public void validateDeclaredAttributeTypeForCollection(Class attributeType, IntegrityChecker checker) throws DescriptorException {
        // by default, do nothing
    }

    /**
     * INTERNAL:
     *    Verify that getter returnType is correct for the
     * indirection policy. If it is incorrect, add an exception
     * to the integrity checker.
     */
    public void validateGetMethodReturnType(Class returnType, IntegrityChecker checker) throws DescriptorException {
        // by default, do nothing
    }

    /**
     * INTERNAL:
     *    Verify that getter returnType is an appropriate collection type for the
     * indirection policy. If it is incorrect, add an exception to the integrity checker.
     */
    public void validateGetMethodReturnTypeForCollection(Class returnType, IntegrityChecker checker) throws DescriptorException {
        // by default, do nothing
    }

    /**
     * INTERNAL:
     *    Verify that setter parameterType is correct for the
     * indirection policy. If it is incorrect, add an exception
     * to the integrity checker.
     */
    public void validateSetMethodParameterType(Class parameterType, IntegrityChecker checker) throws DescriptorException {
        // by default, do nothing
    }

    /**
     * INTERNAL:
     *    Verify that setter parameterType is an appropriate collection type for the
     * indirection policy. If it is incorrect, add an exception to the integrity checker.
     */
    public void validateSetMethodParameterTypeForCollection(Class parameterType, IntegrityChecker checker) throws DescriptorException {
        // by default, do nothing
    }

    /**
     * INTERNAL:
     * Return the value to be stored in the object's attribute.
     *    This value is determined by the query.
     */
    public abstract Object valueFromQuery(ReadQuery query, AbstractRecord row, AbstractSession session);

    /**
     * INTERNAL:
     * Return the value to be stored in the object's attribute.
     *    This value is determined by the row.
     */
    public abstract Object valueFromRow(Object object);
}
