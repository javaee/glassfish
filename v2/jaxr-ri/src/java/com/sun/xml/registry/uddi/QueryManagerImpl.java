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


package com.sun.xml.registry.uddi;

import com.sun.xml.registry.common.*;
import com.sun.xml.registry.common.util.*;
import java.util.Collection;
import java.util.Locale;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.RegistryObject;


/**
 * Class Declaration for Class1
 * @see
 * @author 
 */
public class QueryManagerImpl implements QueryManager {
    
    RegistryServiceImpl service;
    UDDIMapper uddi;
    
    //for testing
    public QueryManagerImpl() {
        // Fix for CRs 6520297 and 6520354
        String country = Locale.getDefault().getCountry();        
        // Default country is 'null' on Solaris.  Use 'US' as default country
        // and 'en' as default languge.
        if (country == null || country == "") {
            Locale.setDefault(Locale.US);
        }
        System.out.println("Default locale: "+Locale.getDefault().toString());
    }
    
    public QueryManagerImpl(RegistryServiceImpl service) {
        this();
        this.service = service;
        uddi = service.getUDDIMapper();
    }
    /**
     * gets the specified RegistryObjects
     *
     * @return BulkResponse containing Collection of RegistryObjects.
     */
    public BulkResponse getRegistryObjects(Collection objectKeys)  throws JAXRException{
        throw new UnsupportedCapabilityException();
    }
    
    /**
     * Gets the RegistryObjects owned by the caller.
     * The objects are returned as their concrete type (e.g. Organization, User etc.).
     *
     *
     * @return BulkResponse containing a hetrogeneous Collection of RegistryObjects (e.g. Organization, User etc.).
     */
    public BulkResponse getRegistryObjects()  throws JAXRException {
        if (!service.getConnection().isSynchronous()) {
            BulkResponseImpl response = new BulkResponseImpl();
            response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
            response.setRequestId(Utility.getInstance().generateUUID());
            service.storeBulkResponse(response);
            FuturesRequestManager.invokeCommand(
                new JAXRCommand.GetRegistryObjectsCommand(service,
                    response));
            return response;
        } else {
            return uddi.getRegistryObjects();
        }
    }
    
  
    
    /**
     * Gets the RegistryObject specified by the Id.
     *
     * @return RegistryObject Is the object is returned as their concrete type (e.g. Organization, User etc.).
     */
    public RegistryObject getRegistryObject(String id)  throws JAXRException{
        throw new UnsupportedCapabilityException();
    }
    
    /**
     * Gets the RegistryObject specified by the Id.
     *
     * @return RegistryObject Is the object is returned as their concrete type (e.g. Organization, User etc.).
     */
    public BulkResponse getRegistryObjects(String objectType)  throws JAXRException{
        if (!service.getConnection().isSynchronous()) {
            BulkResponseImpl response = new BulkResponseImpl();
            response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
            response.setRequestId(Utility.getInstance().generateUUID());
            service.storeBulkResponse(response);
            FuturesRequestManager.invokeCommand(
                new JAXRCommand.GetRegistryObjectsByTypeCommand(service,
                    response, objectType));
            return response;
        } else {
            return uddi.getRegistryObjects(objectType);
        }
    }
    
    public RegistryObject getRegistryObject(String id, String type) throws JAXRException {
         return uddi.getRegistryObject(id, type); 
    }
    
    public BulkResponse getRegistryObjects(Collection ids, String type) throws JAXRException {
        if (!service.getConnection().isSynchronous()) {
            BulkResponseImpl response = new BulkResponseImpl();
            response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
            response.setRequestId(Utility.getInstance().generateUUID());
            service.storeBulkResponse(response);
            FuturesRequestManager.invokeCommand(
                new JAXRCommand.GetRegistryObjectsByKeysCommand(service,
                    response, ids, type));
            return response;
        } else {
            return uddi.getRegistryObjects(ids, type);
        }       
    }
    
    public RegistryService getRegistryService(){
        return service;
    }
}
