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

import oracle.toplink.essentials.indirection.*;
import oracle.toplink.essentials.internal.helper.ClassConstants;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * UnitOfWorkQueryValueHolder wraps a database-stored object and
 * implements behavior to access it. The object is read from
 * the database by invoking a user-specified query.
 * This value holder is used only in the unit of work.
 *
 * @author    Sati
 */
public class UnitOfWorkQueryValueHolder extends UnitOfWorkValueHolder {
    protected UnitOfWorkQueryValueHolder(ValueHolderInterface attributeValue, Object clone, DatabaseMapping mapping, UnitOfWorkImpl unitOfWork) {
        super(attributeValue, clone, mapping, unitOfWork);
    }

    public UnitOfWorkQueryValueHolder(ValueHolderInterface attributeValue, Object clone, ForeignReferenceMapping mapping, AbstractRecord row, UnitOfWorkImpl unitOfWork) {
        this(attributeValue, clone, mapping, unitOfWork);
        this.row = row;
    }

    /**
     * Backup the clone attribute value.
     */
    protected Object buildBackupCloneFor(Object cloneAttributeValue) {
        return getMapping().buildBackupCloneForPartObject(cloneAttributeValue, null, null, getUnitOfWork());
    }

    /**
     * Clone the original attribute value.
     */
    public Object buildCloneFor(Object originalAttributeValue) {
        return getMapping().buildCloneForPartObject(originalAttributeValue, null, this.relationshipSourceObject, getUnitOfWork(), true);
    }

    /**
     * Ensure that the backup value holder is populated.
     */
    public void setValue(Object theValue) {
        // Must force instantiation to be able to compare with the old value.
        if (!isInstantiated()) {
            instantiate();
        }
        Object oldValue = getValue();
        super.setValue(theValue);
        updateForeignReferenceSet(theValue, oldValue);
    }

    /**
     * INTERNAL:
     * Here we now must check for bi-directional relationship.
     * If the mapping has a relationship partner then we must maintain the original relationship.
     * We only worry about ObjectReferenceMappings as the collections mappings will be handled by transparentIndirection
     */
    public void updateForeignReferenceRemove(Object value) {
        DatabaseMapping sourceMapping = this.getMapping();
        if (sourceMapping == null) {
            //mapping is a transient attribute. If it does not exist then we have been serialized
            return;
        }

        if (sourceMapping.isPrivateOwned()) {
            // don't null out backpointer on private owned relationship because it will cause an
            // extra update.
            return;
        }

        //	ForeignReferenceMapping partner = (ForeignReferenceMapping)getMapping().getRelationshipPartner();
        ForeignReferenceMapping partner = this.getRelationshipPartnerFor(value);
        if (partner != null) {
            if (value != null) {
                Object unwrappedValue = partner.getDescriptor().getObjectBuilder().unwrapObject(value, getSession());
                Object oldParent = partner.getRealAttributeValueFromObject(unwrappedValue, getSession());
                Object sourceObject = getRelationshipSourceObject();
                
                if ((oldParent == null) || (partner.isCollectionMapping() &&  !(partner.getContainerPolicy().contains(sourceObject, oldParent, getSession())))) {
                    // value has already been set
                    return;
                }

                if (partner.isObjectReferenceMapping()) {
                    // Check if it's already been set to null
                    partner.setRealAttributeValueInObject(unwrappedValue, null);
                } else if (partner.isCollectionMapping()) {
                    // If it is not in the collection then it has already been removed.
                    partner.getContainerPolicy().removeFrom(sourceObject, oldParent, getSession());
                }
            }
        }
    }

    /**
     * INTERNAL:
     * Here we now must check for bi-directional relationship.
     * If the mapping has a relationship partner then we must maintain the original relationship.
     * We only worry about ObjectReferenceMappings as the collections mappings will be handled by transparentIndirection
     */
    public void updateForeignReferenceSet(Object value, Object oldValue) {
        if ((value != null) && (ClassConstants.Collection_Class.isAssignableFrom(value.getClass()))) {
            //I'm passing a collection into the valueholder not an object
            return;
        }
        if (getMapping() == null) {
            //mapping is a transient attribute. If it does not exist then we have been serialized
            return;
        }

        //	ForeignReferenceMapping partner = (ForeignReferenceMapping)getMapping().getRelationshipPartner();
        ForeignReferenceMapping partner = this.getRelationshipPartnerFor(value);
        if (partner != null) {
            if (value != null) {
                Object unwrappedValue = partner.getDescriptor().getObjectBuilder().unwrapObject(value, getSession());
                Object oldParent = partner.getRealAttributeValueFromObject(unwrappedValue, getSession());
                Object sourceObject = getRelationshipSourceObject();
                Object wrappedSource = getMapping().getDescriptor().getObjectBuilder().wrapObject(sourceObject, getSession());
                
                if ((oldParent == sourceObject) || (partner.isCollectionMapping() &&  partner.getContainerPolicy().contains(sourceObject, oldParent, getSession()))) {
                    // value has already been set
                    return;
                }

                // Set the Object that was refereceing this value to reference null, or remove value from its collection
                if (oldParent != null) {
                    if (getMapping().isObjectReferenceMapping()) {
                        if (!partner.isCollectionMapping()) {
                            // If the back pointer is a collection it's OK that I'm adding myself into the collection
                            ((ObjectReferenceMapping)getMapping()).setRealAttributeValueInObject(oldParent, null);
                        }
                    } else if (getMapping().isCollectionMapping() && (!partner.isManyToManyMapping())) {
                        getMapping().getContainerPolicy().removeFrom(unwrappedValue, getMapping().getRealAttributeValueFromObject(oldParent, getSession()), getSession());
                    }
                }
                
                if (oldValue != null) {
                    // CR 3487
                    Object unwrappedOldValue = partner.getDescriptor().getObjectBuilder().unwrapObject(oldValue, getSession());

                    // if this object was referencing a different object reset the back pointer on that object
                    if (partner.isObjectReferenceMapping()) {
                        partner.setRealAttributeValueInObject(unwrappedOldValue, null);
                    } else if (partner.isCollectionMapping()) {
                        partner.getContainerPolicy().removeFrom(sourceObject, partner.getRealAttributeValueFromObject(unwrappedOldValue, getSession()), getSession());
                    }
                }

                // Now set the back reference of the value being passed in to point to this object
                if (partner.isObjectReferenceMapping()) {
                    partner.setRealAttributeValueInObject(unwrappedValue, wrappedSource);
                } else if (partner.isCollectionMapping()) {
                    partner.getContainerPolicy().addInto(wrappedSource, oldParent, getSession());
                }
            } else {
                updateForeignReferenceRemove(oldValue);
            }
        }
    }

    /**
     * Helper method to retrieve the relationship partner mapping.  This will take inheritance
     * into account and return the mapping associated with correct subclass if necessary.  This
     * is needed for EJB 2.0 inheritance
     */
    private ForeignReferenceMapping getRelationshipPartnerFor(Object partnerObject) {
        ForeignReferenceMapping partner = (ForeignReferenceMapping)getMapping().getRelationshipPartner();
        if ((partner == null) || (partnerObject == null)) {
            // no partner, nothing to do
            return partner;
        }

        // if the target object is not an instance of the class type associated with the partner
        // mapping, try and look up the same partner mapping but as part of the partnerObject's
        // descriptor.  Only check if inheritance is involved...
        if (partner.getDescriptor().hasInheritance()) {
            ClassDescriptor partnerObjectDescriptor = this.getSession().getDescriptor(partnerObject);
            if (!(partner.getDescriptor().getJavaClass().isAssignableFrom(partnerObjectDescriptor.getJavaClass()))) {
                return (ForeignReferenceMapping)partnerObjectDescriptor.getMappingForAttributeName(partner.getAttributeName());
            }
        }
        return partner;
    }
}
