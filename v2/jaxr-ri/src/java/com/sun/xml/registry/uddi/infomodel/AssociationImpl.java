/*
* The contents of this file are subject to the terms 
* of the Common Development and Distribution License 
* (the License).  You may not use this file except in
* compliance with the License.
* 
* You can obtain a copy of the license at 
* https://glassfish.dev.java.net/public/CDDLv1.0.html or
* glassfish/bootstrap/legal/CDDLv1.0.txt.
* See the License for the specific language governing 
* permissions and limitations under the License.
* 
* When distributing Covered Code, include this CDDL 
* Header Notice in each file and include the License file 
* at glassfish/bootstrap/legal/CDDLv1.0.txt.  
* If applicable, add the following below the CDDL Header, 
* with the fields enclosed by brackets [] replaced by
* you own identifying information: 
* "Portions Copyrighted [year] [name of copyright owner]"
* 
* Copyright 2007 Sun Microsystems, Inc. All rights reserved.
*/

package com.sun.xml.registry.uddi.infomodel;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;
import java.io.Serializable;
import java.util.Collection;

import com.sun.xml.registry.uddi.*;
import java.util.Iterator;

/**
 * Implementation of Association interface.
 */
public class AssociationImpl extends RegistryObjectImpl implements Association, Serializable {
    
    boolean isExtramural = false;
    boolean isConfirmedBySourceOwner = false;
    boolean isConfirmedByTargetOwner = false;
    
    private RegistryObject sourceObject;
    private RegistryObject targetObject;
    private Concept associationType;
    private boolean keyFieldsSet = false;
    private boolean keyGenerated = false;
    
    /**
     * Default constructor
     */
    public AssociationImpl() {
        
    }
    
    public AssociationImpl(RegistryObject targetObject, Concept associationType) throws JAXRException {
        
        this.targetObject = targetObject;
        this.associationType = associationType;
    }
    
    
    /**
     * Returns true if the association has been confirmed by the owner of the sourceObject.
     * For intramural Associations always return true.
     */
    public boolean isConfirmedBySourceOwner() throws JAXRException {
        return isConfirmedBySourceOwner;
    }
    
    /**
     * Returns true if the association has been confirmed by the owner of the targetObject.
     * For intramural Associations always return true.
     */
    public boolean isConfirmedByTargetOwner() throws JAXRException {
        return isConfirmedByTargetOwner;
    }
    
    /**
     * Returns true if the association has been confirmed by the owner of the sourceObject.
     * For intramural Associations always return true.
     */
    public void setIsConfirmedBySourceOwner(boolean confirmation)
    throws JAXRException {
        isConfirmedBySourceOwner = confirmation;
    }
    
    /**
     * Returns true if the association has been confirmed by the owner of the targetObject.
     * For intramural Associations always return true.
     */
    public void setIsConfirmedByTargetOwner(boolean confirmation)
    throws JAXRException {
        isConfirmedByTargetOwner = confirmation;
    }
    
    /**
     * Convenience method that returns true if isConfirmedBySourceOwner and
     * isConfirmedByTargetOwner both return true.
     * For intramural Associations always return true.
     */
    public boolean isConfirmed() throws JAXRException {
        return (isConfirmedBySourceOwner && isConfirmedByTargetOwner);
    }
    
    
    /**
     * Gets the Object that is the source of this Association.
     */
    public RegistryObject getSourceObject() throws JAXRException {
        return sourceObject;
    }
    
    /**
     * Sets the Object that is the source of this Association.
     */
    public void setSourceObject(RegistryObject srcObject) throws JAXRException {
        sourceObject = srcObject;
        checkObjects();
        if (areKeyFieldsSet()) {
            keyGenerated = false;
            createKey();
        }
    }
    
    /**
     * Gets the Object that is the target of this Association.
     */
    public RegistryObject getTargetObject() throws JAXRException {
        return targetObject;
    }
    
    /**
     * Sets the Object that is the target of this Association.
     */
    public void setTargetObject(RegistryObject targetObject) throws JAXRException {
        this.targetObject = targetObject;
        checkObjects();
        if (areKeyFieldsSet()) {
            keyGenerated = false;
            createKey();
        }
    }
    
    /**
     * Gets the predefined association type for this Association.
     */
    public Concept getAssociationType() throws JAXRException {
        return associationType;
    }
    
    /**
     * Sets the predefined association type for this Association.
     */
    public void setAssociationType(Concept associationType) throws JAXRException {
        this.associationType = associationType;
        if (areKeyFieldsSet()) {
            keyGenerated = false;
            createKey();
        }
    }
    
    
    /**
     * Returns true if the sourceObject and targetObject are owned by two different
     * Users.
     * An Extramural Association must be made bilateral by the User that is
     * not the creator of the Extramural Association, in order for it to be visible to
     * third parties.
     */
    public boolean isExtramural() throws JAXRException {
        return checkExtramural();
    }
    
    /**
     * Returns true if the sourceObject and targetObject are owned by two different
     * Users.
     * An Extramural Association must be made bilateral by the User that is
     * not the creator of the Extramural Association, in order for it to be visible to
     * third parties.
     */
    public void setIsExtramural(boolean extramural) throws JAXRException {
        isExtramural = extramural;
    }
    
    
    
    public void createKey()throws JAXRException {
        
        if (!keyGenerated) {
            if (keyFieldsSet) {
                String sourceKey = sourceObject.getKey().getId();
                String targetKey = targetObject.getKey().getId();
                String associationTypeValue = associationType.getValue();
                
                StringBuffer keyBuf = new StringBuffer(400);
                keyBuf.append(sourceKey);
                keyBuf.append(":");
                keyBuf.append(targetKey);
                keyBuf.append(":");
                keyBuf.append(associationTypeValue);
                
                this.key = new KeyImpl(keyBuf.toString());
                keyGenerated = true;
            }
        }
    }
    
    boolean areKeyFieldsSet()
    throws JAXRException {
        if ((sourceObject != null) && (targetObject != null) && (associationType != null)) {
            Key sourceKey = sourceObject.getKey();
            Key targetKey = targetObject.getKey();
            String value = associationType.getValue();
            if ((sourceKey != null) && (targetKey != null) && (value != null))
                keyFieldsSet = true;
        }
        return keyFieldsSet;
    }
    
    /*
     * Method checks to see if both source and target are new
     * objects (not in registry). If so, then they both belong to
     * the same user and the association is intramural.
     *
     * This method should be run whenever source or target
     * objects change.
     */
    private void checkObjects() throws JAXRException {
        if ((sourceObject == null) || (targetObject == null)) {
            return;
        }
        Key sourceKey = sourceObject.getKey();
        Key targetKey = targetObject.getKey();
        if ((sourceKey == null) && (targetKey == null)) {
            isConfirmedBySourceOwner = true;
            isConfirmedByTargetOwner = true;
            isExtramural = isExtramural();
        }
    }
    
    
    
    private boolean checkExtramural() throws JAXRException {
        if   ((sourceObject !=null) && (targetObject != null)){
            if (((RegistryObjectImpl)sourceObject).isLoaded() &&
                    ((RegistryObjectImpl)targetObject).isLoaded()){
                
                Slot sourceAuthNameSlot = sourceObject.getSlot(JAXRConstants.AUTHORIZED_NAME);
                Collection sourceValues = sourceAuthNameSlot.getValues();
                Slot targetAuthNameSlot = targetObject.getSlot(JAXRConstants.AUTHORIZED_NAME);
                Collection targetValues = targetAuthNameSlot.getValues();
                if ((!sourceValues.isEmpty()) && (!targetValues.isEmpty())){
                    if (!sourceValues.iterator().next().equals(targetValues.iterator().next()))
                        isExtramural = true;
                    else isExtramural = false;
                    return isExtramural;
                }
            } else if (((RegistryObjectImpl)sourceObject).isNew() ||
                    ((RegistryObjectImpl)targetObject).isNew()){
                return checkUserExtramural();
                
            } else
                return checkUserExtramural();
        }
        return isExtramural;
    }
    
    private boolean checkUserExtramural() throws JAXRException {
        
        if ((sourceObject !=null) && (targetObject != null)){
            RegistryServiceImpl sRS =
                    (RegistryServiceImpl)sourceObject.getLifeCycleManager().getRegistryService();
            RegistryServiceImpl tRS =
                    (RegistryServiceImpl)targetObject.getLifeCycleManager().getRegistryService();
            String sString = sRS.getCurrentUser();
            String tString = tRS.getCurrentUser();
            if (sString.equals(tString))
                isExtramural = false;
            else isExtramural = true;
        }
        return isExtramural;
    }
    
}

