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
package oracle.toplink.essentials.descriptors;

import java.util.*;
import oracle.toplink.essentials.exceptions.QueryException;
import oracle.toplink.essentials.internal.descriptors.ObjectBuilder;
import oracle.toplink.essentials.internal.descriptors.OptimisticLockingPolicy;
import oracle.toplink.essentials.internal.helper.DatabaseField;
import oracle.toplink.essentials.mappings.DatabaseMapping;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.queryframework.FetchGroup;
import oracle.toplink.essentials.queryframework.FetchGroupTracker;
import oracle.toplink.essentials.queryframework.ObjectLevelReadQuery;

/**
 * <p><b>Purpose</b>: The fetch group manager controls the named fetch groups defined at
 * the descriptor level. TopLink supports multiple, overlapped fetch groups, optionally with
 * one of them as the default fetch group.
 *
 * The domain object must implement oracle.toplink.essentials.queryframework.FetchGroupTracker interface,
 * in order to make use of the fetch group performance enhancement feature.
 *
 * Please refer to FetchGroup class for the prons and cons of fetch group usage.
 *
 * @see oracle.toplink.essentials.queryframework.FetchGroup
 * @see oracle.toplink.essentials.queryframework.FetchGroupTracker
 *
 * @author King Wang
 * @since TopLink 10.1.3.
 */
public class FetchGroupManager {
    //The group map is keyed by the group name, valued by the fetch group object. 
    private Map fetchGroups = null;

    //default fetch group
    private FetchGroup defaultFetchGroup;

    //ref to the descriptor
    private ClassDescriptor descriptor;

    /**
     * Constructor
     */
    public FetchGroupManager() {
    }

    /**
     * Add a named fetch group to the descriptor
     */
    public void addFetchGroup(FetchGroup group) {
        //create a new fetch group and put it in the group map.
        getFetchGroups().put(group.getName(), group);
    }

    /**
     * Return the fetch group map: keyed by the group name, valued by the fetch group object.
     */
    public Map getFetchGroups() {
        if (fetchGroups == null) {
            //lazy initialized
            fetchGroups = new HashMap(2);
        }

        return fetchGroups;
    }

    /**
     * Return the descriptor-level default fetch group.
     * All read object and read all queries would use the default fetch group if no fetch group
     * is explicitly defined for the query, unless setShouldUseDefaultFetchGroup(false); is also
     * called on the query.
     *
     * Default fetch group should be used carefully. It would be beneficial if most of the system queries
     * are for the subset of the object, so un-needed attributes data would not have to be read, and the
     * users do not have to setup every query for the given fetch group, as default one is always used.
     * However, if queries on object are mostly use case specific and not systematic, using default fetch group
     * could cause undesirable extra round-trip and performance degradation.
     *
     * @see oracle.toplink.essentials.queryframework.ObjectLevelReadQuery#setShouldUseDefaultFetchGroup(boolean)
     */
    public FetchGroup getDefaultFetchGroup() {
        return defaultFetchGroup;
    }

    /**
     * Return a pre-defined named fetch group.
     */
    public FetchGroup getFetchGroup(String groupName) {
        return (FetchGroup)getFetchGroups().get(groupName);
    }

    /**
     * Set the descriptor-level default fetch group.
     * All read object and read all queries would use the default fetch group if no fetch group is
     * explicitly defined for the query, unless setShouldUseDefaultFetchGroup(false);
     * is also called on the query.
     *
     * Default fetch group should be used carefully. It would be beneficial if most of the system queries
     * are for the subset of the object, so un-needed attributes data would not have to be read, and the
     * users do not have to setup every query for the given fetch group, as default one is always used.
     * However, if queries on object are mostly use case specific and not systematic, using default fetch group
     * could cause undesirable extra round-trip and performance degradation.
     *
     * @see oracle.toplink.essentials.queryframework.ObjectLevelReadQuery#setShouldUseDefaultFetchGroup(boolean)
     */
    public void setDefaultFetchGroup(FetchGroup newDefaultFetchGroup) {
        defaultFetchGroup = newDefaultFetchGroup;
    }

    /**
     * INTERNAL:
     * Return true if the object is partially fetched and cached.
     * It applies to the query with fetch group.
     */
    public boolean isPartialObject(Object domainObject) {
        if (domainObject != null) {
            FetchGroup fetchGroupInCache = ((FetchGroupTracker)domainObject).getFetchGroup();

            //if the fetch group reference is not null, it means the object is partial.
            return (fetchGroupInCache != null);
        }
        return false;
    }

    /**
     * INTERNAL:
     * Return if the cached object data is sufficiently valid against a fetch group
     */
    public boolean isObjectValidForFetchGroup(Object object, FetchGroup fetchGroup) {
        FetchGroup groupInObject = ((FetchGroupTracker)object).getFetchGroup();
        return (groupInObject == null) || groupInObject.isSupersetOf(fetchGroup);
    }

    /**
     * INTERNAL:
     * Return true if the cached object data should be written in clone.
     * It is used in Fetch Group case when filling in the clone from the cached object.
     */
    public boolean shouldWriteInto(Object cachedObject, Object clone) {
        if (isPartialObject(clone)) {
            FetchGroup fetchGroupInSrc = ((FetchGroupTracker)cachedObject).getFetchGroup();
            FetchGroup fetchGroupInTarg = ((FetchGroupTracker)clone).getFetchGroup();

            //if the target fetch group is not null (i.e. fully fetched object) or if partially fetched, it's not a superset of that of the source, 
            //or if refresh is required, should always write (either refresh or revert) data from the cache to the clones.
            return (!((fetchGroupInTarg == null) || fetchGroupInTarg.isSupersetOf(fetchGroupInSrc)) || ((FetchGroupTracker)cachedObject).shouldRefreshFetchGroup());
        }
        return false;
    }

    /**
    * INTERNAL:
    * Write data of the partially fetched object into the working and backup clones
    */
    public void writePartialIntoClones(Object partialObject, Object workingClone, UnitOfWorkImpl uow) {
        FetchGroup fetchGroupInClone = ((FetchGroupTracker)workingClone).getFetchGroup();
        FetchGroup fetchGroupInObject = ((FetchGroupTracker)partialObject).getFetchGroup();
        Object backupClone = uow.getBackupClone(workingClone);

        //if refresh is set, force to fill in fecth group data
        if (((FetchGroupTracker)partialObject).shouldRefreshFetchGroup()) {
            //refresh and fill in the fecth group data
            refreshFetchGroupIntoClones(partialObject, workingClone, backupClone, fetchGroupInObject, fetchGroupInClone, uow);
        } else {//no refresh is enforced
            //revert the unfetched attributes of the clones.
            revertDataIntoUnfetchedAttributesOfClones(partialObject, workingClone, backupClone, fetchGroupInObject, fetchGroupInClone, uow);
        }

        //update fecth group in clone as the union of two
        fetchGroupInObject = unionFetchGroups(fetchGroupInObject, fetchGroupInClone);
        //finally, update clone's fetch group reference 
        setObjectFetchGroup(workingClone, fetchGroupInObject);
        setObjectFetchGroup(backupClone, fetchGroupInObject);
    }

    /**
     * Refresh the fetch group data into the working and backup clones.
     * This is called if refresh is enforced
     */
    private void refreshFetchGroupIntoClones(Object cachedObject, Object workingClone, Object backupClone, FetchGroup fetchGroupInObject, FetchGroup fetchGroupInClone, UnitOfWorkImpl uow) {
        Vector mappings = descriptor.getMappings();
        boolean isObjectPartial = (fetchGroupInObject != null);
        Set fetchedAttributes = isObjectPartial ? fetchGroupInObject.getAttributes() : null;
        for (int index = 0; index < mappings.size(); index++) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.get(index);
            if ((!isObjectPartial) || ((fetchedAttributes != null) && fetchedAttributes.contains(mapping.getAttributeName()))) {
                //only fill in the unfetched attributes into clones
                mapping.buildClone(cachedObject, workingClone, uow);
                    mapping.buildClone(workingClone, backupClone, uow);
            }
        }
    }

    /**
     * Revert the clones' unfetched attributes, and leave fetched ones intact.
     */
    private void revertDataIntoUnfetchedAttributesOfClones(Object cachedObject, Object workingClone, Object backupClone, FetchGroup fetchGroupInObject, FetchGroup fetchGroupInClone, UnitOfWorkImpl uow) {
        //if(fetchGroupInClone == null || fetchGroupInClone.isSupersetOf(fetchGroupInObject)) {
        if (isObjectValidForFetchGroup(workingClone, fetchGroupInObject)) {
            //if working clone is fully fetched or it's fetch group is superset of that of the cached object
            //no reversion is needed, so simply return
            return;
        }
        Vector mappings = descriptor.getMappings();

        //fetched attributes list in working clone
        Set fetchedAttributesClone = fetchGroupInClone.getAttributes();
        for (int index = 0; index < mappings.size(); index++) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.get(index);

            //only revert the attribute which is fetched by the cached object, but not fecthed by the clones.
            if (isAttributeFetched(cachedObject, mapping.getAttributeName()) && (!fetchedAttributesClone.contains(mapping.getAttributeName()))) {
                //only fill in the unfetched attributes into clones
                mapping.buildClone(cachedObject, workingClone, uow);
                    mapping.buildClone(workingClone, backupClone, uow);
            }
        }
    }

    /**
     * INTERNAL:
     * Copy fetch group refrerence from the source object to the target
     */
    public void copyFetchGroupInto(Object source, Object target) {
        if (isPartialObject(source)) {
            ((FetchGroupTracker)target).setFetchGroup(((FetchGroupTracker)source).getFetchGroup());
        }
    }

    /**
     * INTERNAL:
     * Union the fetch group of the domain object with the new fetch group.
     */
    public void unionFetchGroupIntoObject(Object source, FetchGroup newFetchGroup) {
        FetchGroupTracker tracker = (FetchGroupTracker)source;
        tracker.setFetchGroup(unionFetchGroups(tracker.getFetchGroup(), newFetchGroup));
    }

    /**
     * INTERNAL:
     * Union two fetch groups.
     */
    public FetchGroup unionFetchGroups(FetchGroup first, FetchGroup second) {
        if ((first == null) || (second == null)) {
            return null;
        }

        //return the superset if applied
        if (first.isSupersetOf(second)) {
            return first;
        } else if (second.isSupersetOf(first)) {
            return second;
        }

        //otherwise, union two fetch groups
        StringBuffer unionGroupName = new StringBuffer(first.getName());
        unionGroupName.append("_");
        unionGroupName.append(second.getName());
        FetchGroup unionFetchGroup = new FetchGroup(unionGroupName.toString());
        unionFetchGroup.addAttributes(first.getAttributes());
        unionFetchGroup.addAttributes(second.getAttributes());
        return unionFetchGroup;
    }

    /**
     * INTERNAL:
     * Reset object attributes to the default values.
     */
    public void reset(Object source) {
        ((FetchGroupTracker)source).resetFetchGroup();
    }

    /**
     * INTERNAL:
     * Reset object attributes to the default their values.
     */
    public void setObjectFetchGroup(Object source, FetchGroup fetchGroup) {
        if (descriptor.getFetchGroupManager() != null) {
            ((FetchGroupTracker)source).setFetchGroup(fetchGroup);
        }
    }

    /**
     * INTERNAL:
     * Set if the tracked object is fetched from executing a query with or without refresh.
     */
    public void setRefreshOnFetchGroupToObject(Object source, boolean shouldRefreshOnFetchgroup) {
        ((FetchGroupTracker)source).setShouldRefreshFetchGroup(shouldRefreshOnFetchgroup);
    }

    /**
     * Return true if the attribute of the object has already been fetched
     */
    public boolean isAttributeFetched(Object object, String attributeName) {
        FetchGroup fetchgroup = ((FetchGroupTracker)object).getFetchGroup();
        return (fetchgroup == null) || (fetchgroup.getAttributes().contains(attributeName));
    }

    /**
     * INTERNAL:
     * Return the referenced descriptor.
     */
    public ClassDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * INTERNAL:
     * Return the referenced descriptor.
     */
    public ClassDescriptor getClassDescriptor() {
		return getDescriptor();
    }

    /**
     * Set the referenced descriptor.
     */
    public void setDescriptor(ClassDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * INTERNAL:
     * Prepare the query with the fetch group to add group attributes to the query
     * for partial reading.
     */
    public void prepareQueryWithFetchGroup(ObjectLevelReadQuery query) {
        //initialize query's fetch group
        query.initializeFetchGroup();
        if ((query.getFetchGroup() == null) || query.getFetchGroup().hasFetchGroupAttributeExpressions()) {
            //simply return if fetch group is not defined; or if defined, it has been prepared already.
            return;
        } else {
            if (query.isReportQuery()) {
                //fetch group does not work with report query
                throw QueryException.fetchGroupNotSupportOnReportQuery();
            }
            if (query.hasPartialAttributeExpressions()) {
                //fetch group does not work with partial attribute reading
                throw QueryException.fetchGroupNotSupportOnPartialAttributeReading();
            }
        }
        Set attributes = query.getFetchGroup().getAttributes();
        ObjectBuilder builder = query.getDescriptor().getObjectBuilder();

        //First add all primary key attributes into the fetch group
        Iterator pkMappingIter = builder.getPrimaryKeyMappings().iterator();

        while (pkMappingIter.hasNext()) {
            DatabaseMapping pkMapping = (DatabaseMapping)pkMappingIter.next();
            DatabaseField pkField = pkMapping.getField();

            // Add pk attribute to the fetch group attributes list
            attributes.add(pkMapping.getAttributeName());
        }

        //second, add version/optimistic locking object attributes into the fetch group if applied.
        OptimisticLockingPolicy lockingPolicy = getDescriptor().getOptimisticLockingPolicy();
        if (query.shouldMaintainCache() && (lockingPolicy != null)) {
            lockingPolicy.prepareFetchGroupForReadQuery(query.getFetchGroup(), query);
        }

        //thrid, prepare all fetch group attributes 
        Iterator attrIter = attributes.iterator();
        while (attrIter.hasNext()) {
            String attrName = (String)attrIter.next();
            DatabaseMapping mapping = builder.getMappingForAttributeName(attrName);
            if (mapping == null) {
                //the attribute name defined in the fetch group is not mapped
                throw QueryException.fetchGroupAttributeNotMapped(attrName);
            }

            //partially fetch each fetch group attribute
            if (mapping.isCollectionMapping()) {
                query.getFetchGroup().addFetchGroupAttribute(query.getExpressionBuilder().anyOf(attrName));
            } else {
                query.getFetchGroup().addFetchGroupAttribute(query.getExpressionBuilder().get(attrName));
            }
        }
    }

    /**
    * INTERNAL:
    * Clone the fetch group manager
    */
    public Object clone() {
        Object object = null;
        try {
            object = super.clone();
        } catch (Exception exception) {
            ;
        }
        return object;
    }
}
