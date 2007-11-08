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

/**
 * Implementation of ExternalIdentifier interface
 *
 * @author Farrukh S. Najmi
 */
public class ExternalIdentifierImpl extends RegistryObjectImpl implements ExternalIdentifier, Serializable {
    
    private String value;
    private RegistryObject registryObject;
    private ClassificationScheme identificationScheme;
    
    public ExternalIdentifierImpl() {
        super();
    }
    
    public ExternalIdentifierImpl(Key key, String name, String value) {
        this();
        this.key = key;
        this.name = new InternationalStringImpl(name);
        this.value = value;
        try {
            if (keyFieldsSet())
                createKey();
        } catch (JAXRException ex){
               //do nothing 
       }
        
    }
    
    public ExternalIdentifierImpl(ClassificationScheme identificationScheme,
        String name, String value) {
            this();
            this.identificationScheme = identificationScheme;
            this.name = new InternationalStringImpl(name);
            this.value = value;
            try {
            if (keyFieldsSet())
                createKey();
            } catch (JAXRException ex){
               //do nothing 
            }
    }
    
    /**
     * Gets the parent registry object
     */
    public RegistryObject getRegistryObject() throws JAXRException {
        return registryObject;
    }

    /**
     * Internal method to set the registry object
     */
    public void setRegistryObject(RegistryObject registryObject) throws JAXRException {
        this.registryObject = registryObject;
        if (keyFieldsSet())
            createKey();
    }
    
    /**
     * Gets the value of an ExternalIdentifier
     */
    public String getValue() throws JAXRException {
        return value;
    }
    
    /**
     * Sets the value of an ExternalIdentifier
     */
    public void setValue(String value) throws JAXRException {
        this.value = value;
        setIsModified(true);
        if (keyFieldsSet())
            createKey();
    }

    /**
     * Gets the ClassificationScheme that is used as the identification scheme
     * for identifying this object.
     */
    public ClassificationScheme getIdentificationScheme() throws JAXRException {
        return identificationScheme;
    }
    
    /**
     * Sets the ClassificationScheme that is used as the identification scheme
     * for identifying this object.
     */
    public void setIdentificationScheme(ClassificationScheme classificationScheme)
        throws JAXRException {
            identificationScheme = classificationScheme;
            setIsModified(true);
            if (keyFieldsSet())
            createKey();
    }
    
    boolean keyFieldsSet()  throws JAXRException {
        boolean set = true;
        if (isRetrieved()) {
            if ((registryObject == null) || (identificationScheme == null) || (value == null)) {
                set = false;
                if ((registryObject.getKey() == null) || (identificationScheme.getKey() == null))
                    set = false;
            }
        }
        return set;
    }
    
    Key createKey() throws JAXRException {
        String registryObjectId = null;
        String identificationSchemeId = null;
        if (isRetrieved()) {
            if (registryObject != null)
                registryObjectId = registryObject.getKey().getId();
            if (identificationScheme != null)
                identificationSchemeId = identificationScheme.getKey().getId();
        }
        
        StringBuffer buf = new StringBuffer(400);
        buf.append(registryObjectId);
        buf.append(":");
        buf.append(identificationSchemeId);
        buf.append(":");
        buf.append(value);
        
        this.key = new KeyImpl(buf.toString());
        return this.key;
    }
    
}
