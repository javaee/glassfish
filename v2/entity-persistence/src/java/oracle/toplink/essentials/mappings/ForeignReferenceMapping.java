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
package oracle.toplink.essentials.mappings;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.indirection.*;
import oracle.toplink.essentials.internal.descriptors.*;
import oracle.toplink.essentials.internal.expressions.*;
import oracle.toplink.essentials.internal.indirection.*;
import oracle.toplink.essentials.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedClassForName;
import oracle.toplink.essentials.internal.sessions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.internal.helper.Helper;
import oracle.toplink.essentials.sessions.DatabaseRecord;
import oracle.toplink.essentials.sessions.Session;

/**
 * <b>Purpose</b>: Abstract class for relationship mappings
 */
public abstract class ForeignReferenceMapping extends DatabaseMapping {

    /** This is used only in descriptor proxy in remote session */
    protected Class referenceClass;
    protected String referenceClassName;

    /** The session is temporarily used for initialization. Once used, it is set to null */
    protected transient AbstractSession tempInitSession;

    /** The descriptor of the reference class. */
    protected transient ClassDescriptor referenceDescriptor;

    /** This query is used to read referenced objects for this mapping. */
    protected transient ReadQuery selectionQuery;

    /** Indicates whether the referenced object is privately owned or not. */
    protected boolean isPrivateOwned;

    /** Implements indirection behavior */
    protected IndirectionPolicy indirectionPolicy;

    /** Indicates whether the selection query is TopLink generated or defined by the user. */
    protected transient boolean hasCustomSelectionQuery;

    /** Used to reference the other half of a bi-directional relationship. */
    protected DatabaseMapping relationshipPartner;

    /** Set by users, used to retreive the backpointer for this mapping */
    protected String relationshipPartnerAttributeName;

    /** Cascading flags used by the EntityManager */
    protected boolean cascadePersist;
    protected boolean cascadeMerge;
    protected boolean cascadeRefresh;
    protected boolean cascadeRemove;

    protected ForeignReferenceMapping() {
        this.isPrivateOwned = false;
        this.hasCustomSelectionQuery = false;
        this.useBasicIndirection();
        this.cascadePersist = false;
        this.cascadeMerge = false;
        this.cascadeRefresh = false;
        this.cascadeRemove = false;
    }

    /**
     * INTERNAL:
     * Clone the attribute from the clone and assign it to the backup.
     */
    public void buildBackupClone(Object clone, Object backup, UnitOfWorkImpl unitOfWork) {
        Object attributeValue = getAttributeValueFromObject(clone);
        Object clonedAttributeValue = getIndirectionPolicy().backupCloneAttribute(attributeValue, clone, backup, unitOfWork);
        setAttributeValueInObject(backup, clonedAttributeValue);
    }

    /**
     * INTERNAL:
     * Used during building the backup shallow copy to copy the
     * target object without re-registering it.
     */
    public abstract Object buildBackupCloneForPartObject(Object attributeValue, Object clone, Object backup, UnitOfWorkImpl unitOfWork);

    /**
     * INTERNAL:
     * Clone the attribute from the original and assign it to the clone.
     */
    public void buildClone(Object original, Object clone, UnitOfWorkImpl unitOfWork) {
        Object attributeValue = getAttributeValueFromObject(original);
        Object clonedAttributeValue = getIndirectionPolicy().cloneAttribute(attributeValue, original, clone, unitOfWork, false); // building clone from an original not a row.
        //GFBug#404 - fix moved to ObjectBuildingQuery.registerIndividualResult 
        setAttributeValueInObject(clone, clonedAttributeValue);
    }

    /**
     * INTERNAL:
     * A combination of readFromRowIntoObject and buildClone.
     * <p>
     * buildClone assumes the attribute value exists on the original and can
     * simply be copied.
     * <p>
     * readFromRowIntoObject assumes that one is building an original.
     * <p>
     * Both of the above assumptions are false in this method, and actually
     * attempts to do both at the same time.
     * <p>
     * Extract value from the row and set the attribute to this value in the
     * working copy clone.
     * In order to bypass the shared cache when in transaction a UnitOfWork must
     * be able to populate working copies directly from the row.
     */
    public void buildCloneFromRow(AbstractRecord databaseRow, JoinedAttributeManager joinManager, Object clone, ObjectBuildingQuery sourceQuery, UnitOfWorkImpl unitOfWork, AbstractSession executionSession) {
        Object attributeValue = valueFromRow(databaseRow, joinManager, sourceQuery, executionSession);
        Object clonedAttributeValue = getIndirectionPolicy().cloneAttribute(attributeValue, null,// no original
                                                                            clone, unitOfWork, true);// building clone directly from row.
        setAttributeValueInObject(clone, clonedAttributeValue);
    }

    /**
     * INTERNAL:
     * Require for cloning, the part must be cloned.
     */
    public abstract Object buildCloneForPartObject(Object attributeValue, Object original, Object clone, UnitOfWorkImpl unitOfWork, boolean isExisting);

    /**
     * INTERNAL:
     * The mapping clones itself to create deep copy.
     */
    public Object clone() {
        ForeignReferenceMapping clone = (ForeignReferenceMapping)super.clone();

        clone.setIndirectionPolicy((IndirectionPolicy)indirectionPolicy.clone());
        clone.setSelectionQuery((ReadQuery)getSelectionQuery().clone());

        return clone;
    }

    /**
     * INTERNAL:
     * Compare the attributes belonging to this mapping for the objects.
     */
    public boolean compareObjects(Object firstObject, Object secondObject, AbstractSession session) {
        if (isPrivateOwned()) {
            return compareObjectsWithPrivateOwned(firstObject, secondObject, session);
        } else {
            return compareObjectsWithoutPrivateOwned(firstObject, secondObject, session);
        }
    }

    /**
     * Compare two objects if their parts are not private owned
     */
    protected abstract boolean compareObjectsWithoutPrivateOwned(Object first, Object second, AbstractSession session);

    /**
     * Compare two objects if their parts are private owned
     */
    protected abstract boolean compareObjectsWithPrivateOwned(Object first, Object second, AbstractSession session);

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this mapping to actual class-based
     * settings. This method is used when converting a project that has been built
     * with class names to a project with classes.
     * @param classLoader
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        super.convertClassNamesToClasses(classLoader);

        // DirectCollection mappings don't require a reference class.
        if (getReferenceClassName() != null) {
            Class referenceClass = null;
            try{
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        referenceClass = (Class)AccessController.doPrivileged(new PrivilegedClassForName(getReferenceClassName(), true, classLoader));
                    } catch (PrivilegedActionException exception) {
                        throw ValidationException.classNotFoundWhileConvertingClassNames(getReferenceClassName(), exception.getException());
                    }
                } else {
                    referenceClass = oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.getClassForName(getReferenceClassName(), true, classLoader);
                }
            } catch (ClassNotFoundException exc){
                throw ValidationException.classNotFoundWhileConvertingClassNames(getReferenceClassName(), exc);
            }
            setReferenceClass(referenceClass);
        }
    };

    /**
     * INTERNAL:
     * Builder the unit of work value holder.
     * Ignore the original object.
     * @param buildDirectlyFromRow indicates that we are building the clone directly
     * from a row as opposed to building the original from the row, putting it in
     * the shared cache, and then cloning the original.
     */
    public UnitOfWorkValueHolder createUnitOfWorkValueHolder(ValueHolderInterface attributeValue, Object original, Object clone, AbstractRecord row, UnitOfWorkImpl unitOfWork, boolean buildDirectlyFromRow) {
        return new UnitOfWorkQueryValueHolder(attributeValue, clone, this, row, unitOfWork);
    }

    /**
     * INTERNAL:
     * Return true if the merge should be bypassed. This would be the case for several reasons, depending on
     * the kind of merge taking place.
     */
    protected boolean dontDoMerge(Object target, Object source, MergeManager mergeManager) {
        if (!shouldMergeCascadeReference(mergeManager)) {
            return true;
        }
        if (mergeManager.shouldMergeOriginalIntoWorkingCopy()) {
            // For reverts we are more concerned about the target than the source.
            if (!isAttributeValueInstantiated(target)) {
                return true;
            }
        } else {
            if (mergeManager.shouldRefreshRemoteObject() && shouldMergeCascadeParts(mergeManager) && usesIndirection()) {
                return true;
            } else {
                if (!isAttributeValueInstantiated(source)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * PUBLIC:
     * Indirection means that a ValueHolder will be put in-between the attribute and the real object.
     * This allows for the reading of the target from the database to be delayed until accessed.
     * This defaults to true and is strongly suggested as it give a huge performance gain.
     */
    public void dontUseIndirection() {
        setIndirectionPolicy(new NoIndirectionPolicy());
    }

    /**
     * INTERNAL:
     * Clone and prepare the JoinedAttributeManager nested JoinedAttributeManager.
     * This is used for nested joining as the JoinedAttributeManager passed to the joined build object.
     */
    public ObjectLevelReadQuery prepareNestedJoins(JoinedAttributeManager joinManager, AbstractSession session) {
        // A nested query must be built to pass to the descriptor that looks like the real query execution would.
        ObjectLevelReadQuery nestedQuery = (ObjectLevelReadQuery)((ObjectLevelReadQuery)getSelectionQuery()).deepClone();
        //set session to be the session on which the query is being executed
        nestedQuery.setSession(session);
        // Recompute the joined indexes based on the nested join expressions.
        nestedQuery.getJoinedAttributeManager().setJoinedMappingIndexes_(null);
        nestedQuery.getJoinedAttributeManager().setJoinedMappingExpressions_(new ArrayList(1));
        nestedQuery.getJoinedAttributeManager().setJoinedAttributeExpressions_(extractNestedExpressions(joinManager.getJoinedAttributeExpressions(), nestedQuery.getExpressionBuilder(), false));
        // the next line sets isToManyJoinQuery flag
        nestedQuery.getJoinedAttributeManager().prepareJoinExpressions(session);
        nestedQuery.getJoinedAttributeManager().computeJoiningMappingIndexes(true, session, 0);
        if (joinManager.getBaseQuery().isLockQuery()) {
            ObjectLevelReadQuery baseQuery = ((ObjectLevelReadQuery)joinManager.getBaseQuery());
            if (baseQuery.getLockingClause().isForUpdateOfClause()) {
                ForUpdateOfClause clause = (ForUpdateOfClause)baseQuery.getLockingClause().clone();
                clause.setLockedExpressions(extractNestedExpressions(clause.getLockedExpressions(), nestedQuery.getExpressionBuilder(), true));
                nestedQuery.setLockingClause(clause);
            } else {
                nestedQuery.setLockingClause(baseQuery.getLockingClause());
            }
        }
        nestedQuery.setShouldMaintainCache(joinManager.getBaseQuery().shouldMaintainCache());
        nestedQuery.setShouldRefreshIdentityMapResult(joinManager.getBaseQuery().shouldRefreshIdentityMapResult());
        nestedQuery.setCascadePolicy(joinManager.getBaseQuery().getCascadePolicy());
        nestedQuery.setSession(null);

        return nestedQuery;

    }

    /**
     * INTERNAL:
     * Return the value of an attribute which this mapping represents for an object.
     */
    public Object getAttributeValueFromObject(Object object) throws DescriptorException {
        Object attributeValue = super.getAttributeValueFromObject(object);
        Object indirectionValue = getIndirectionPolicy().validateAttributeOfInstantiatedObject(attributeValue);

        // PERF: Allow the indirection policy to initialize null attribute values,
        // this allows the indirection objects to not be initialized in the constructor.
        if (indirectionValue != attributeValue) {
            setAttributeValueInObject(object, indirectionValue);
            attributeValue = indirectionValue;
        }
        return attributeValue;
    }

    /**
     * INTERNAL:
     * Returns the attribute value from the reference object.
     * If the attribute is using indirection the value of the value-holder is returned.
     * If the value holder is not instantiated then it is instantiated.
     */
    public Object getAttributeValueWithClonedValueHolders(Object object) {
        Object attributeValue = getAttributeValueFromObject(object);
        if (attributeValue instanceof DatabaseValueHolder){
            return ((DatabaseValueHolder)attributeValue).clone();
        } else if (attributeValue instanceof ValueHolder){
            return ((ValueHolder)attributeValue).clone();
        }
        return attributeValue;
    }

    /**
     * INTERNAL:
     * Return the mapping's indirection policy.
     */
    public IndirectionPolicy getIndirectionPolicy() {
        return indirectionPolicy;
    }

    /**
     * INTERNAL:
     * Returns the join criteria stored in the mapping selection query. This criteria
     * is used to read reference objects across the tables from the database.
     */
    public Expression getJoinCriteria(QueryKeyExpression exp) {
        Expression selectionCriteria = getSelectionCriteria();
        return exp.getBaseExpression().twist(selectionCriteria, exp);
    }

    /**
     * INTERNAL:
     * Returns the attribute value from the reference object.
     * If the attribute is using indirection the value of the value-holder is returned.
     * If the value holder is not instantiated then it is instantiated.
     */
    public Object getRealAttributeValueFromObject(Object object, AbstractSession session) {
            return getIndirectionPolicy().getRealAttributeValueFromObject(object, getAttributeValueFromObject(object));
    }

    /**
     * PUBLIC:
     * Returns the reference class.
     */
    public Class getReferenceClass() {
        return referenceClass;
    }

    /**
     * INTERNAL:
     * Returns the reference class name.
     */
    public String getReferenceClassName() {
        if ((referenceClassName == null) && (referenceClass != null)) {
            referenceClassName = referenceClass.getName();
        }
        return referenceClassName;
    }

    /**
     * INTERNAL:
     * Return the referenceDescriptor. This is a descriptor which is associated with
     * the reference class.
     */
    public ClassDescriptor getReferenceDescriptor() {
        if (referenceDescriptor == null) {
            if (getTempSession() == null) {
                return null;
            } else {
                referenceDescriptor = getTempSession().getDescriptor(getReferenceClass());
            }
        }

        return referenceDescriptor;
    }

    /**
     * INTERNAL:
     * Return the relationshipPartner mapping for this bi-directional mapping. If the relationshipPartner is null then
     * this is a uni-directional mapping.
     */
    public DatabaseMapping getRelationshipPartner() {
        if ((this.relationshipPartner == null) && (this.relationshipPartnerAttributeName != null)) {
            setRelationshipPartner(getReferenceDescriptor().getMappingForAttributeName(getRelationshipPartnerAttributeName()));
        }
        return this.relationshipPartner;
    }

    /**
     * PUBLIC:
     *  Use this method retreive the relationship partner attribute name of this bidirectional Mapping.
     */
    public String getRelationshipPartnerAttributeName() {
        return this.relationshipPartnerAttributeName;
    }

    /**
     * INTERNAL:
     * Returns the selection criteria stored in the mapping selection query. This criteria
     * is used to read reference objects from the database.
     */
    public Expression getSelectionCriteria() {
        return getSelectionQuery().getSelectionCriteria();
    }

    /**
     * INTERNAL:
     * Returns the read query assoicated with the mapping.
     */
    public ReadQuery getSelectionQuery() {
        return selectionQuery;
    }

    protected AbstractSession getTempSession() {
        return tempInitSession;
    }

    /**
     * INTERNAL:
     * Indicates whether the selection query is TopLink generated or defined by 
     * the user.
     */
    public boolean hasCustomSelectionQuery() {
        return hasCustomSelectionQuery;
    }

    /**
     * INTERNAL:
     * Initialize the state of mapping.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);
        initializeReferenceDescriptor(session);
        initializeSelectionQuery(session);
        getIndirectionPolicy().initialize();
    }

    /**
     * Initialize and set the descriptor for the referenced class in this mapping.
     */
    protected void initializeReferenceDescriptor(AbstractSession session) throws DescriptorException {
        if (getReferenceClass() == null) {
            throw DescriptorException.referenceClassNotSpecified(this);
        }

        ClassDescriptor refDescriptor = session.getDescriptor(getReferenceClass());

        if (refDescriptor == null) {
            throw DescriptorException.descriptorIsMissing(getReferenceClass().getName(), this);
        }

        if (refDescriptor.isAggregateDescriptor() && (!isAggregateCollectionMapping())) {
            throw DescriptorException.referenceDescriptorCannotBeAggregate(this);
        }

        // can not be isolated if it is null.  Seems that only aggregates do not set
        // the owning descriptor on the mapping.
        if ((!((this.getDescriptor() != null) && this.getDescriptor().isIsolated())) && refDescriptor.isIsolated()) {
            throw DescriptorException.isolateDescriptorReferencedBySharedDescriptor(refDescriptor.getJavaClassName(), this.getDescriptor().getJavaClassName(), this);
        }

        setReferenceDescriptor(refDescriptor);
    }

    /**
     * A subclass should implement this method if it wants non default behaviour.
     */
    protected void initializeSelectionQuery(AbstractSession session) throws DescriptorException {
        if (((ObjectLevelReadQuery)getSelectionQuery()).getReferenceClass() == null) {
            throw DescriptorException.referenceClassNotSpecified(this);
        }

        getSelectionQuery().setDescriptor(getReferenceDescriptor());
    }

    /**
     * INTERNAL:
     * The referenced object is checked if it is instantiated or not
     */
    public boolean isAttributeValueInstantiated(Object object) {
        return getIndirectionPolicy().objectIsInstantiated(getAttributeValueFromObject(object));
    }

    /**
     * PUBLIC:
     * Check cascading value for the CREATE operation.
     */
    public boolean isCascadePersist() {
        return this.cascadePersist;
    }

    /**
     * PUBLIC:
     * Check cascading value for the MERGE operation.
     */
    public boolean isCascadeMerge() {
        return this.cascadeMerge;
    }

    /**
     * PUBLIC:
     * Check cascading value for the REFRESH operation.
     */
    public boolean isCascadeRefresh() {
        return this.cascadeRefresh;
    }

    /**
     * PUBLIC:
     * Check cascading value for the REMOVE operation.
     */
    public boolean isCascadeRemove() {
        return this.cascadeRemove;
    }

    /**
     * INTERNAL:
     */
    public boolean isForeignReferenceMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Return true if referenced objects are provately owned else false.
     */
    public boolean isPrivateOwned() {
        return isPrivateOwned;
    }

    /**
     * INTERNAL:
     * Iterate on the iterator's current object's attribute defined by this mapping.
     * The iterator's settings for cascading and value holders determine how the
     * iteration continues from here.
     */
    public void iterate(DescriptorIterator iterator) {
        Object attributeValue = this.getAttributeValueFromObject(iterator.getVisitedParent());
        this.getIndirectionPolicy().iterateOnAttributeValue(iterator, attributeValue);
    }

    /**
     * INTERNAL:
     * Iterate on the attribute value.
     * The value holder has already been processed.
     */
    public abstract void iterateOnRealAttributeValue(DescriptorIterator iterator, Object realAttributeValue);

    /**
     * PUBLIC:
     * Sets the reference object to be a private owned. The default behaviour is non
     * private owned.
     */
    public void privateOwnedRelationship() {
        setIsPrivateOwned(true);
    }

    /**
     * PUBLIC:
     * Sets the cascading for all operations.
     */
    public void setCascadeAll(boolean value) {
        setCascadePersist(value);
        setCascadeMerge(value);
        setCascadeRefresh(value);
        setCascadeRemove(value);
    }

    /**
     * PUBLIC:
     * Sets the cascading for the CREATE operation.
     */
    public void setCascadePersist(boolean value) {
        this.cascadePersist = value;
    }

    /**
     * PUBLIC:
     * Sets the cascading for the MERGE operation.
     */
    public void setCascadeMerge(boolean value) {
        this.cascadeMerge = value;
    }

    /**
     * PUBLIC:
     * Sets the cascading for the REFRESH operation.
     */
    public void setCascadeRefresh(boolean value) {
        this.cascadeRefresh = value;
    }

    /**
     * PUBLIC:
     * Sets the cascading for the REMOVE operation.
     */
    public void setCascadeRemove(boolean value) {
        this.cascadeRemove = value;
    }

    /**
     * PUBLIC:
     * Relationship mappings creates a read query to read reference objects. If this default
     * query needs to be customize then user can specify its own read query to do the reading
     * of reference objects. One must instance of ReadQuery or subclasses of the ReadQuery.
     */
    public void setCustomSelectionQuery(ReadQuery query) {
        setSelectionQuery(query);
        setHasCustomSelectionQuery(true);
    }

    protected void setHasCustomSelectionQuery(boolean bool) {
        hasCustomSelectionQuery = bool;
    }

    /**
     * ADVANCED:
     * Set the indirection policy.
     */
    public void setIndirectionPolicy(IndirectionPolicy indirectionPolicy) {
        this.indirectionPolicy = indirectionPolicy;
        indirectionPolicy.setMapping(this);
    }

    /**
     * INTERNAL:
     * Used by Gromit
     */
    public void setIsPrivateOwned(boolean isPrivateOwned) {
        this.isPrivateOwned = isPrivateOwned;
    }

    /**
     * INTERNAL:
     * Set the value of the attribute mapped by this mapping,
     * placing it inside a value holder if necessary.
     * If the value holder is not instantiated then it is instantiated.
     */
    public void setRealAttributeValueInObject(Object object, Object value) throws DescriptorException {
        this.getIndirectionPolicy().setRealAttributeValueInObject(object, value);
    }

    /**
     * PUBLIC:
     * Set the referenced class.
     */
    public void setReferenceClass(Class referenceClass) {
        this.referenceClass = referenceClass;
        if (referenceClass != null) {
            setReferenceClassName(referenceClass.getName());
            // Make sure the reference class of the selectionQuery is set.
            setSelectionQuery(getSelectionQuery());
        }
    }

    /**
     * INTERNAL:
     * Used by MW.
     */
    public void setReferenceClassName(String referenceClassName) {
        this.referenceClassName = referenceClassName;
    }

    /**
     * Set the referenceDescriptor. This is a descriptor which is associated with
     * the reference class.
     */
    protected void setReferenceDescriptor(ClassDescriptor aDescriptor) {
        referenceDescriptor = aDescriptor;
    }

    /**
     * INTERNAL:
     * Sets the relationshipPartner mapping for this bi-directional mapping. If the relationshipPartner is null then
     * this is a uni-directional mapping.
     */
    public void setRelationshipPartner(DatabaseMapping mapping) {
        this.relationshipPartner = mapping;
    }

    /**
    * PUBLIC:
    *  Use this method to specify the relationship partner attribute name of a bidirectional Mapping.
    *  TopLink will use the attribute name to find the back pointer mapping to maintain referential integrity of
    *  the bi-directional mappings.
    */
    public void setRelationshipPartnerAttributeName(String attributeName) {
        this.relationshipPartnerAttributeName = attributeName;
    }

    /**
     * PUBLIC:
     * Sets the selection criteria to be used as a where clause to read
     * reference objects. This criteria is automatically generated by the
     * TopLink if not explicitly specified by the user.
     */
    public void setSelectionCriteria(Expression anExpression) {
        getSelectionQuery().setSelectionCriteria(anExpression);
    }

    /**
     * Sets the query
     */
    protected void setSelectionQuery(ReadQuery aQuery) {
        selectionQuery = aQuery;
        // Make sure the reference class of the selectionQuery is set.        
        if ((selectionQuery != null) && selectionQuery.isObjectLevelReadQuery() && (selectionQuery.getReferenceClassName() == null)) {
            ((ObjectLevelReadQuery)selectionQuery).setReferenceClass(getReferenceClass());
        }
    }

    /**
     * PUBLIC:
     * This is a property on the mapping which will allow custom SQL to be
     * substituted for reading a reference object.
     */
    public void setSelectionSQLString(String sqlString) {
        getSelectionQuery().setSQLString(sqlString);
        setCustomSelectionQuery(getSelectionQuery());
    }

    /**
     * PUBLIC:
     * This is a property on the mapping which will allow custom call to be
     * substituted for reading a reference object.
     */
    public void setSelectionCall(Call call) {
        getSelectionQuery().setCall(call);
        setCustomSelectionQuery(getSelectionQuery());
    }

    protected void setTempSession(AbstractSession session) {
        this.tempInitSession = session;
    }

    /**
     * PUBLIC:
     * Indirection means that a ValueHolder will be put in-between the attribute and the real object.
     * This allows for the reading of the target from the database to be delayed until accessed.
     * This defaults to true and is strongly suggested as it give a huge performance gain.
     * @see #useBasicIndirection()
     * @see #dontUseIndirection()
     */
    public void setUsesIndirection(boolean usesIndirection) {
        if (usesIndirection) {
            useBasicIndirection();
        } else {
            dontUseIndirection();
        }
    }

    protected boolean shouldInitializeSelectionCriteria() {
        if (hasCustomSelectionQuery()) {
            return false;
        }

        if (getSelectionCriteria() == null) {
            return true;
        }

        return false;
    }

    /**
     * INTERNAL:
     * Returns true if the merge should cascade to the mappings reference's parts.
     */
    public boolean shouldMergeCascadeParts(MergeManager mergeManager) {
        return ((mergeManager.shouldCascadeByMapping() && this.isCascadeMerge()) || (mergeManager.shouldCascadeAllParts()) || (mergeManager.shouldCascadePrivateParts() && isPrivateOwned()));
    }

    /**
     * Returns true if the merge should cascade to the mappings reference.
     */
    protected boolean shouldMergeCascadeReference(MergeManager mergeManager) {
        if (mergeManager.shouldCascadeReferences()) {
            return true;
        }

        // P2.0.1.3: Was merging references on non-privately owned parts
        // Same logic in:
        return shouldMergeCascadeParts(mergeManager);
    }

    /**
     * INTERNAL:
     * Returns true only if the object is scheduled for deletion.
     */
    protected boolean shouldObjectDeleteCascadeToPart(WriteObjectQuery query, Object object) {
        return (isPrivateOwned() || query.shouldCascadeAllParts() || query.shouldDependentObjectBeDeleted(object));
    }

    /**
     * Returns true if any process leading to object modification should also affect its parts
     * Usually used by write, insert and update.
     */
    protected boolean shouldObjectModifyCascadeToParts(ObjectLevelModifyQuery query) {
        if (isReadOnly()) {
            return false;
        }

        if (isPrivateOwned()) {
            return true;
        }

        // Only cascade dependents writes in uow.
        if (query.shouldCascadeOnlyDependentParts()) {
            return hasConstraintDependency();
        }

        return query.shouldCascadeAllParts();
    }

    /**
     * INTERNAL:
     * Return whether any process leading to object deletion should also affect its parts. 
     * Used in preDelete. Note that foreign key dependencies are reversed for deletes: 
     * The relationship side having the foreign key must be deleted before the other side is removed.
     */
    protected boolean shouldObjectModifyCascadeToPartsForPreDelete(ObjectLevelModifyQuery query) {
        if (isReadOnly()) {
            return false;
        }

        // Always cascade for privately-owned parts
        if (isPrivateOwned()) {
            return true;
        }

        // Foreign key dependencies are reversed for deletes
        if (query.shouldCascadeOnlyDependentParts()) {
            return !hasConstraintDependency();
        } 
        
        return query.shouldCascadeAllParts();
    }

    /**
     * PUBLIC:
     * Indirection means that a ValueHolder will be put in-between the attribute and the real object.
     * This allows for the reading of the target from the database to be delayed until accessed.
     * This defaults to true and is strongly suggested as it give a huge performance gain.
     */
    public void useBasicIndirection() {
        setIndirectionPolicy(new BasicIndirectionPolicy());
    }

    public void useWeavedIndirection(String setMethodName){
        setIndirectionPolicy(new WeavedObjectBasicIndirectionPolicy(setMethodName));
    }

    /**
     * PUBLIC:
     * Indirection means that some sort of indirection object will be put in-between the attribute and the real object.
     * This allows for the reading of the target from the database to be delayed until accessed.
     * This defaults to true and is strongly suggested as it give a huge performance gain.
     */
    public boolean usesIndirection() {
        return getIndirectionPolicy().usesIndirection();
    }

    /**
     * INTERNAL:
     * To validate mappings decleration
     */
    public void validateBeforeInitialization(AbstractSession session) throws DescriptorException {
        super.validateBeforeInitialization(session);

        if (getAttributeAccessor() instanceof InstanceVariableAttributeAccessor) {
            Class attributeType = ((InstanceVariableAttributeAccessor)getAttributeAccessor()).getAttributeType();
            getIndirectionPolicy().validateDeclaredAttributeType(attributeType, session.getIntegrityChecker());
        } else if (getAttributeAccessor() instanceof MethodAttributeAccessor) {
            Class returnType = ((MethodAttributeAccessor)getAttributeAccessor()).getGetMethodReturnType();
            getIndirectionPolicy().validateGetMethodReturnType(returnType, session.getIntegrityChecker());

            Class parameterType = ((MethodAttributeAccessor)getAttributeAccessor()).getSetMethodParameterType();
            getIndirectionPolicy().validateSetMethodParameterType(parameterType, session.getIntegrityChecker());
        }
    }

    /**
     * INTERNAL:
     * Return the value of the reference attribute or a value holder.
     * Check whether the mapping's attribute should be optimized through batch and joining.
     */
    public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery query, AbstractSession executionSession) throws DatabaseException {
        if(shouldUseValueFromRowWithJoin(joinManager)) {
            return valueFromRowInternalWithJoin(row, joinManager, executionSession);
        } else {
            return valueFromRowInternal(row, joinManager, executionSession);
        }
    }

    /**
     * INTERNAL:
     * Indicates whether valueFromRow should call valueFromRowInternalWithJoin (true)
     * or valueFromRowInternal (false)
     */
    protected boolean shouldUseValueFromRowWithJoin(JoinedAttributeManager joinManager) {
        return isJoiningSupported() && (
                joinManager.isAttributeJoined(getDescriptor(), getAttributeName()) ||
                joinManager.getBaseQuery().hasPartialAttributeExpressions()); // only true on OLRQ and above
    }

    /**
     * INTERNAL:
     * If the query used joining or partial attributes, build the target object directly.
     * If isJoiningSupported()==true then this method must be overridden.
     * Currently only 1-1, 1-m and m-m support joining.
     * Potentially agg-col, dc, dm may support it too (agg-col and dc by just implementing
     * isJoiningSupported(){return true;} - but that should be tested).
     */
    protected Object valueFromRowInternalWithJoin(AbstractRecord row, JoinedAttributeManager joinManager, AbstractSession executionSession) throws DatabaseException {
        throw ValidationException.mappingDoesNotOverrideValueFromRowInternalWithJoin(Helper.getShortClassName(this.getClass()));
    }

    /**
     * INTERNAL:
     * Return the value of the reference attribute or a value holder.
     * Check whether the mapping's attribute should be optimized through batch and joining.
     */
    protected Object valueFromRowInternal(AbstractRecord row, JoinedAttributeManager joinManager, AbstractSession executionSession) throws DatabaseException {
        // PERF: Direct variable access.
        ReadQuery targetQuery = this.selectionQuery;

        //CR #4365, 3610825 - moved up from the block below, needs to be set with 
        // indirection off. Clone the query and set its id.
        if (!this.indirectionPolicy.usesIndirection()) {
            targetQuery = (ReadQuery)targetQuery.clone();
            targetQuery.setQueryId(joinManager.getBaseQuery().getQueryId());
        }

        // if the source query is cascading then the target query must use the same settings
        if (targetQuery.isObjectLevelReadQuery() && (joinManager.getBaseQuery().shouldCascadeAllParts() || (this.isPrivateOwned && joinManager.getBaseQuery().shouldCascadePrivateParts()) || (this.cascadeRefresh && joinManager.getBaseQuery().shouldCascadeByMapping()))) {
            // If the target query has already been cloned (we're refreshing) avoid 
            // re-cloning the query again.
            if (targetQuery == this.selectionQuery) {
                targetQuery = (ObjectLevelReadQuery)targetQuery.clone();
            }

            ((ObjectLevelReadQuery)targetQuery).setShouldRefreshIdentityMapResult(joinManager.getBaseQuery().shouldRefreshIdentityMapResult());
            targetQuery.setCascadePolicy(joinManager.getBaseQuery().getCascadePolicy());

            // For queries that have turned caching off, such as aggregate collection, leave it off.
            if (targetQuery.shouldMaintainCache()) {
                targetQuery.setShouldMaintainCache(joinManager.getBaseQuery().shouldMaintainCache());
            }
        }
        if (joinManager.getBaseQuery().isObjectLevelReadQuery()){
            targetQuery = prepareHistoricalQuery(targetQuery, (ObjectLevelReadQuery)joinManager.getBaseQuery(), executionSession);
        }

        return this.indirectionPolicy.valueFromQuery(targetQuery, row, executionSession);
    }

    /**
     * INTERNAL:
     * Allow for the mapping to perform any historical query additions.
     * Return the new target query.
     */
    protected ReadQuery prepareHistoricalQuery(ReadQuery targetQuery, ObjectLevelReadQuery sourceQuery, AbstractSession executionSession) {
        return targetQuery;
    }

    /**
     * INTERNAL:
     */
    public AbstractRecord trimRowForJoin(AbstractRecord row, JoinedAttributeManager joinManager, AbstractSession executionSession) {
        // CR #... the field for many objects may be in the row,
        // so build the subpartion of the row through the computed values in the query,
        // this also helps the field indexing match.
        if (joinManager.getJoinedMappingIndexes_() != null) {
            Object value = joinManager.getJoinedMappingIndexes_().get(this);
            if(value != null) {
                return trimRowForJoin(row, value, executionSession);
            }
        }
        return row;
    }

    /**
     * INTERNAL:
     */
    public AbstractRecord trimRowForJoin(AbstractRecord row, Object value, AbstractSession executionSession) {
        // CR #... the field for many objects may be in the row,
        // so build the subpartion of the row through the computed values in the query,
        // this also helps the field indexing match.
        int fieldStartIndex;
        if(value instanceof Integer) {
            fieldStartIndex = ((Integer)value).intValue();
        } else {
            // must be Map of classes to Integers
            Map map = (Map)value;
            Class cls;
            if (getDescriptor().hasInheritance() && getDescriptor().getInheritancePolicy().shouldReadSubclasses()) {
                cls = getDescriptor().getInheritancePolicy().classFromRow(row, executionSession);
            } else {
                cls = getDescriptor().getJavaClass();
            }
            fieldStartIndex = ((Integer)map.get(cls)).intValue();
        }
        Vector trimedFields = Helper.copyVector(row.getFields(), fieldStartIndex, row.size());
        Vector trimedValues = Helper.copyVector(row.getValues(), fieldStartIndex, row.size());
        return new DatabaseRecord(trimedFields, trimedValues);
    }
}
