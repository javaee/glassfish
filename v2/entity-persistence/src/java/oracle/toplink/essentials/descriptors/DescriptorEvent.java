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
import oracle.toplink.essentials.mappings.DatabaseMapping;
import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.exceptions.DescriptorException;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.sessions.*;
import oracle.toplink.essentials.sessions.Record;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: Encapsulate the information provided with descriptor events.
 * This is used as the argument to any event raised by the descriptor.
 * Events can be registered for, through two methods, the first is by providing a method
 * to be called on the object that a paticular operation is being performed on.
 * The second is by registering an event listener to be notified when any event occurs
 * for that descriptor.  The second method is more similar to the java beans event model
 * and requires the registered listener to implement the DescriptorEventListener interface.
 *
 * @see DescriptorEventManager
 * @see DescriptorEventListener
 * @see DescriptorEventAdapter
 */
public class DescriptorEvent extends EventObject {
    /**
     * The code of the descriptor event being raised.
     * This is an integer constant value from DescriptorEventManager.
     */
    protected int eventCode;

    /** The query causing the event. */
    protected DatabaseQuery query;

    /** Optionally a database row may be provided on some events, (such as aboutToUpdate). */
    protected Record record;
    protected ClassDescriptor descriptor;

    /**
     * The source object represents the object the event is being raised on,
     * some events also require a second object, for example the original object in a postClone.
     */
    protected Object originalObject;

    /** For the post merge event it is possible that there has been a change set generated.
     * This attribute will store the changeSet for the object just merged
     */
    protected ObjectChangeSet changeSet;

    /** The session in which the event is raised. */
    protected AbstractSession session;

    /** Event names for toString() */
    protected static String[] eventNames;

    /** Initialize the values */
    static {
        eventNames = new String[DescriptorEventManager.NumberOfEvents];

        eventNames[DescriptorEventManager.PreWriteEvent] = "PreWriteEvent";
        eventNames[DescriptorEventManager.PostWriteEvent] = "PostWriteEvent";
        eventNames[DescriptorEventManager.PreDeleteEvent] = "PostDeleteEvent";
        eventNames[DescriptorEventManager.PostDeleteEvent] = "PostDeleteEvent";
        eventNames[DescriptorEventManager.PreInsertEvent] = "PreInsertEvent";
        eventNames[DescriptorEventManager.PostInsertEvent] = "PostInsertEvent";
        eventNames[DescriptorEventManager.PreUpdateEvent] = "PreUpdateEvent";
        eventNames[DescriptorEventManager.PostUpdateEvent] = "PostUpdateEvent";
        eventNames[DescriptorEventManager.PostBuildEvent] = "PostBuildEvent";
        eventNames[DescriptorEventManager.PostRefreshEvent] = "PostRefreshEvent";
        eventNames[DescriptorEventManager.PostCloneEvent] = "PostCloneEvent";
        eventNames[DescriptorEventManager.PostMergeEvent] = "PostMergeEvent";
        eventNames[DescriptorEventManager.AboutToInsertEvent] = "AboutToInsertEvent";
        eventNames[DescriptorEventManager.AboutToUpdateEvent] = "AboutToUpdateEvent";
    }

    /**
     * PUBLIC:
     * Most events are trigger from queries, so this is a helper method.
     */
    public DescriptorEvent(int eventCode, ObjectLevelModifyQuery query) {
        this(query.getObject());
        this.query = query;
        this.eventCode = eventCode;
        this.session = query.getSession();
        this.descriptor = query.getDescriptor();
    }

    /**
     * PUBLIC:
     * All events require a source object.
     */
    public DescriptorEvent(Object sourceObject) {
        super(sourceObject);
    }

    /**
     * PUBLIC:
     * Re-populate the database row with the values from the source object based upon the
     * attribute's mapping. Provided as a helper method for modifying the row during event
     * handling.
     */
    public void applyAttributeValuesIntoRow(String attributeName) {
        ClassDescriptor descriptor = getSession().getDescriptor(getSource());
        DatabaseMapping mapping = descriptor.getMappingForAttributeName(attributeName);

        if (mapping == null) {
            throw ValidationException.missingMappingForAttribute(descriptor, attributeName, this.toString());
        }
        if (getRecord() != null) {
            mapping.writeFromObjectIntoRow(getSource(), (AbstractRecord)getRecord(), getSession());
        }
    }

    /**
     * PUBLIC:
     * Returns the Object changeSet if available
     */
    public ObjectChangeSet getChangeSet() {
        return changeSet;
    }

    /**
     * PUBLIC:
     * The source descriptor of the event.
     */
    public ClassDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * PUBLIC:
     * The source descriptor of the event.
     */
    public ClassDescriptor getClassDescriptor() {
		ClassDescriptor desc = getDescriptor();
		if (desc instanceof ClassDescriptor) {
			return (ClassDescriptor)desc;
		} else {
			throw ValidationException.cannotCastToClass(desc, desc.getClass(), ClassDescriptor.class);
		}
    }

    /**
     * PUBLIC:
     * The code of the descriptor event being raised.
     * This is an integer constant value from DescriptorEventManager.
     */
    public int getEventCode() {
        return eventCode;
    }

    /**
     * PUBLIC:
     * Synanym for source.
     */
    public Object getObject() {
        return getSource();
    }

    /**
     * PUBLIC:
     * The source object represents the object the event is being raised on,
     * some events also require a second object, for example the original object in a postClone.
     *
     * @see EventObject#getSource()
     */
    public Object getOriginalObject() {
        // Compute the original for unit of work writes.
        if ((originalObject == null) && getSession().isUnitOfWork() && (getQuery() != null) && (getQuery().isObjectLevelModifyQuery())) {
            setOriginalObject(((UnitOfWorkImpl)getSession()).getOriginalVersionOfObject(getSource()));
        }
        return originalObject;
    }

    /**
     * PUBLIC:
     * The query causing the event.
     */
    public DatabaseQuery getQuery() {
        return query;
    }

    /**
     * PUBLIC:
     * Return the record that is associated with some events,
     * such as postBuild, and aboutToUpdate.
     */
    public Record getRecord() {
        return record;
    }

    /**
     * PUBLIC:
     * The session in which the event is raised.
     */
    public AbstractSession getSession() {
        return session;
    }

    /**
     * INTERNAL:
     * Sets the Change set in the event if the change Set is available
     */
    public void setChangeSet(ObjectChangeSet newChangeSet) {
        changeSet = newChangeSet;
    }

    /**
     * INTERNAL:
     * The source descriptor of the event.
     */
    public void setDescriptor(ClassDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * INTERNAL:
     * The code of the descriptor event being raised.
     * This is an integer constant value from DescriptorEventManager.
     */
    public void setEventCode(int eventCode) {
        this.eventCode = eventCode;
    }

    /**
     * INTERNAL:
     * The source object represents the object the event is being raised on,
     * some events also require a second object, for example the original object in a postClone.
     */
    public void setOriginalObject(Object originalObject) {
        this.originalObject = originalObject;
    }

    /**
     * INTERNAL:
     * The query causing the event.
     */
    public void setQuery(DatabaseQuery query) {
        this.query = query;
    }

    /**
     * INTERNAL:
     * Optionally a database row may be provided on some events, (such as aboutToUpdate).
     */
    public void setRecord(Record record) {
        this.record = record;
    }

    /**
     * INTERNAL:
     * The session in which the event is raised.
     */
    public void setSession(AbstractSession session) {
        this.session = session;
    }

    /**
     * INTERNAL:
     */
    public String toString() {
        String eventName = "UnkownEvent";

        if ((getEventCode() >= 0) && (getEventCode() < DescriptorEventManager.NumberOfEvents)) {
            eventName = eventNames[getEventCode()];
        }

        return eventName + "(" + getSource().getClass() + ")";
    }

    /**
     * ADVANCED:
     * Use this method when updating object attribute values, with unmapped objects Integer, String or others. in events to ensure that all
     * required objects are updated.  TopLink will automaticaly update all objects and changesets
     * envolved.  TopLink will update the field, in the row, to have the new value for the field
     * that this mapping maps to.
     */
    public void updateAttributeWithObject(String attributeName, Object value) {
        DatabaseMapping mapping = this.query.getDescriptor().getMappingForAttributeName(attributeName);
        if (mapping == null) {
            throw DescriptorException.mappingForAttributeIsMissing(attributeName, getDescriptor());
        }

        Object clone = this.getObject();
        Object cloneValue = value;
        Object original = null;

        //only set the original object if we need to update it, ie before the merge takes place
        if ((this.eventCode == DescriptorEventManager.PostCloneEvent) || (this.eventCode == DescriptorEventManager.PostMergeEvent)) {
            original = this.getOriginalObject();
        }
        Object originalValue = value;
        ObjectChangeSet eventChangeSet = this.getChangeSet();
        Object valueForChangeSet = value;

        if ((this.query != null) && this.query.isObjectLevelModifyQuery()) {
            clone = ((ObjectLevelModifyQuery)this.query).getObject();
            eventChangeSet = ((ObjectLevelModifyQuery)this.query).getObjectChangeSet();
        }
        ClassDescriptor descriptor = getSession().getDescriptor(value.getClass());

        if (descriptor != null) {
            //There is a descriptor for the value being passed in so we must be carefull
            // to convert the value before assigning it.
            if (eventChangeSet != null) {
                valueForChangeSet = descriptor.getObjectBuilder().createObjectChangeSet(value, (UnitOfWorkChangeSet)eventChangeSet.getUOWChangeSet(), getSession());
            }
            if (original != null) {
                // must be a unitOfWork because only the postMerge, and postClone events set this attribute
                originalValue = ((UnitOfWorkImpl)getSession()).getOriginalVersionOfObject(value);
            }
        }
        if (clone != null) {
            mapping.setRealAttributeValueInObject(clone, cloneValue);
        }
        if (original != null) {
            mapping.setRealAttributeValueInObject(original, originalValue);
        }
        if (getRecord() != null) {
            AbstractRecord tempRow = getDescriptor().getObjectBuilder().createRecord();

            // pass in temp Row because most mappings use row.add() not row.put() for
            // perf reasons.  We are using writeFromObjectIntoRow in order to support
            // a large number of types.
            mapping.writeFromObjectIntoRow(clone, tempRow, getSession());
            ((AbstractRecord)getRecord()).mergeFrom(tempRow);
        }
        if (eventChangeSet != null) {
            eventChangeSet.removeChange(attributeName);
            eventChangeSet.addChange(mapping.compareForChange(clone, ((UnitOfWorkImpl)getSession()).getBackupClone(clone), eventChangeSet, getSession()));
        }
    }

    /**
    * ADVANCED:
    * Use this method when updating object attribute values, with unmapped objects Integer, String or others. in events to ensure that all
    * required objects are updated.  TopLink will automaticaly update all objects and changesets
    * envolved.  TopLink will update the field, in the row, to have the new value for the field
    * that this mapping maps to.  If the attribute being updated is within an aggregate then pass the updated aggregate
    * and the attribute of the aggregate mapping into this method.
    */
    public void updateAttributeAddObjectToCollection(String attributeName, Object mapKey, Object value) {
        DatabaseMapping mapping = this.query.getDescriptor().getMappingForAttributeName(attributeName);
        if (mapping == null) {
            throw DescriptorException.mappingForAttributeIsMissing(attributeName, getDescriptor());
        }

        Object clone = this.getObject();
        Object cloneValue = value;
        Object original = null;

        //only set the original object if we need to update it, ie before the merge takes place
        if ((this.eventCode == DescriptorEventManager.PostCloneEvent) || (this.eventCode == DescriptorEventManager.PostMergeEvent)) {
            original = this.getOriginalObject();
        }
        Object originalValue = value;
        ObjectChangeSet eventChangeSet = this.getChangeSet();
        Object valueForChangeSet = value;

        if ((this.query != null) && this.query.isObjectLevelModifyQuery()) {
            clone = ((ObjectLevelModifyQuery)this.query).getObject();
            eventChangeSet = ((ObjectLevelModifyQuery)this.query).getObjectChangeSet();
        }
        ClassDescriptor descriptor = getSession().getDescriptor(value.getClass());

        if (descriptor != null) {
            //There is a descriptor for the value being passed in so we must be carefull
            // to convert the value before assigning it.
            if (eventChangeSet != null) {
                valueForChangeSet = descriptor.getObjectBuilder().createObjectChangeSet(value, (UnitOfWorkChangeSet)eventChangeSet.getUOWChangeSet(), getSession());
            }
            if (original != null) {
                // must be a unitOfWork because only the postMerge, and postClone events set this attribute
                originalValue = ((UnitOfWorkImpl)getSession()).getOriginalVersionOfObject(value);
            }
        }

        if (clone != null) {
            Object collection = mapping.getRealCollectionAttributeValueFromObject(clone, getSession());
            mapping.getContainerPolicy().addInto(mapKey, cloneValue, collection, getSession());
        }
        if (original != null) {
            Object collection = mapping.getRealCollectionAttributeValueFromObject(original, getSession());
            mapping.getContainerPolicy().addInto(mapKey, originalValue, collection, getSession());
        }
        if (getRecord() != null) {
            AbstractRecord tempRow = getDescriptor().getObjectBuilder().createRecord();

            // pass in temp Row because most mappings use row.add() not row.put() for
            // perf reasons.  We are using writeFromObjectIntoRow in order to support
            // a large number of types.
            mapping.writeFromObjectIntoRow(clone, tempRow, getSession());
            ((AbstractRecord)getRecord()).mergeFrom(tempRow);
        }
        if (eventChangeSet != null) {
            mapping.simpleAddToCollectionChangeRecord(mapKey, valueForChangeSet, eventChangeSet, getSession());
        }
    }

    /**
    * ADVANCED:
    * Use this method when updating object attribute values, with unmapped objects Integer, String or others. in events to ensure that all
    * required objects are updated.  TopLink will automaticaly update all objects and changesets
    * envolved.  TopLink will update the field, in the row, to have the new value for the field
    * that this mapping maps to.
    */
    public void updateAttributeRemoveObjectFromCollection(String attributeName, Object mapKey, Object value) {
        DatabaseMapping mapping = this.query.getDescriptor().getMappingForAttributeName(attributeName);
        if (mapping == null) {
            throw DescriptorException.mappingForAttributeIsMissing(attributeName, getDescriptor());
        }

        Object clone = this.getObject();
        Object cloneValue = value;
        Object original = null;

        //only set the original object if we need to update it, ie before the merge takes place
        if ((this.eventCode == DescriptorEventManager.PostCloneEvent) || (this.eventCode == DescriptorEventManager.PostMergeEvent)) {
            original = this.getOriginalObject();
        }
        Object originalValue = value;
        ObjectChangeSet eventChangeSet = this.getChangeSet();
        Object valueForChangeSet = value;

        if ((this.query != null) && this.query.isObjectLevelModifyQuery()) {
            clone = ((ObjectLevelModifyQuery)this.query).getObject();
            eventChangeSet = ((ObjectLevelModifyQuery)this.query).getObjectChangeSet();
        }
        ClassDescriptor descriptor = getSession().getDescriptor(value.getClass());

        if (descriptor != null) {
            //There is a descriptor for the value being passed in so we must be carefull
            // to convert the value before assigning it.
            if (eventChangeSet != null) {
                valueForChangeSet = descriptor.getObjectBuilder().createObjectChangeSet(value, (UnitOfWorkChangeSet)eventChangeSet.getUOWChangeSet(), getSession());
            }
            if (original != null) {
                // must be a unitOfWork because only the postMerge, and postClone events set this attribute
                originalValue = ((UnitOfWorkImpl)getSession()).getOriginalVersionOfObject(value);
            }
        }
        if (clone != null) {
            Object collection = mapping.getRealCollectionAttributeValueFromObject(clone, getSession());
            mapping.getContainerPolicy().removeFrom(mapKey, cloneValue, collection, getSession());
        }
        if (original != null) {
            Object collection = mapping.getRealCollectionAttributeValueFromObject(original, getSession());
            mapping.getContainerPolicy().removeFrom(mapKey, originalValue, collection, getSession());
        }
        if (getRecord() != null) {
            AbstractRecord tempRow = getDescriptor().getObjectBuilder().createRecord();

            // pass in temp Row because most mappings use row.add() not row.put() for
            // perf reasons.  We are using writeFromObjectIntoRow in order to support
            // a large number of types.
            mapping.writeFromObjectIntoRow(clone, tempRow, getSession());
            ((AbstractRecord)getRecord()).mergeFrom(tempRow);
        }
        if (eventChangeSet != null) {
            mapping.simpleRemoveFromCollectionChangeRecord(mapKey, valueForChangeSet, eventChangeSet, getSession());
        }
    }
}
