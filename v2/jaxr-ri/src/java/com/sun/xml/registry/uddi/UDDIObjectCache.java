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

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

import java.util.*;

import com.sun.xml.registry.uddi.infomodel.*;
import com.sun.xml.registry.uddi.*;

/**
 * 
 * @version 1.4, 01/11/00
 * 
 * 
 */

public class UDDIObjectCache {

    private Hashtable cache = new Hashtable();
    private LinkedList list = new LinkedList();
    Collection registryServices;
    

    private static final int MAX_CACHE_SIZE = 150;
    
    public UDDIObjectCache(RegistryServiceImpl service){
		//need to be able to get current service
		if (registryServices == null)
			registryServices = new ArrayList();
		addToRegistryServices(service);
    } 

   

    void addObjectToCache(RegistryObjectImpl ro, String serviceId) 
        throws JAXRException {
        //only put instances of ORG, ServiceBinding,Service, Concept/Classificationscheme
        //in cache			
        if (ro.getServiceId() == null)
                ro.setServiceId(serviceId);				
        add(ro);		
    }
	
    void addObjectsToCache(Collection registryObjects, String serviceId) 
        throws JAXRException {
        //only put instances of ORG, ServiceBinding,Service, Concept/Classificationscheme
        //in cache - need to check this	
        Iterator iter = registryObjects.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if (obj instanceof RegistryObjectImpl) {
                RegistryObjectImpl ro = (RegistryObjectImpl)obj;
                if (ro.getServiceId() == null)
                    ro.setServiceId(serviceId);					                                
                        if (ro.getServiceId() == null)
                                ro.setServiceId(serviceId);					
                        add(ro);
            }
        }						
    }
	

    //fetches object from cache given Key string
    RegistryObject fetchObjectFromCache(String id) throws JAXRException {
            return (RegistryObject)cache.get(id);
    }

    // looks in cache, if object in cache retrieves it
    // if the object isLoaded we are done -
    // else get the object from the UDDI registry given
    // its service
    public void fetchObject(RegistryObjectImpl registryObject, 
                                                    String serviceId) throws JAXRException {
       
        BulkResponse br = null;	
		  RegistryObjectImpl ro = null;
        if (registryObject != null) {
            RegistryServiceImpl service = (RegistryServiceImpl)registryObject.getRegistryService();
            
            if (service.getConnection().useCache()) {
                if (registryObject.isLoaded() == true) 
                    return;
            }
             service.getUDDIMapper().getRegistryObject(registryObject);
            if (service.getConnection().useCache())
                add(registryObject);
            
            registryObject.setStatusFlags(true, true, false);	         
        }

        //doesn't return object detail, but retrieves nothing -
        //this happens with Microlytics given as name
        if (registryObject != null)
            registryObject.setStatusFlags(true, true, false);	
    }
	
    void removeObjectFromCache(String id) {
        //remove the object from the LinkedList
        if (list.size() > 0)
                list.remove(id);

        RegistryObjectImpl registryObject = 
                (RegistryObjectImpl)cache.remove(id);			
        if (registryObject != null) {
                registryObject.setIsDeleted(true);
        }			
    }

    void removeObjectsFromCache(Collection keys) throws JAXRException {
        if (keys == null) return;  

        Iterator i = keys.iterator();
 
        try {          
             while (i.hasNext()) {
                 String id = ((Key)i.next()).getId();  
                 removeObjectFromCache(id); 
             }
        } catch (JAXRException je) {
          throw new JAXRException(
                ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").
                   getString("UDDIObjectManager:Error_deleting_objects_from_Cache"), je);
        }  
    }	

	//look for it in the linkedList, if its there remove it and
	//put it at the front of the linked list -
	//if the linkedList is full - take (remove)the last in the list 
	//and remove it from the cache
    private void add(RegistryObjectImpl registryObject) throws JAXRException {

       String rid = (String)registryObject.getKey().getId();
        //if the ro exists in LinkedList remove it
        synchronized(list) {
            list.remove(rid); 
            list.addFirst(rid);
        }
        //is it in the cache already?
        //cache s already MT safe	
        if (cache.size() >= MAX_CACHE_SIZE) {
        //remove the last object
        //is it in the cache already?
        //no - add to cache
            String id = null;
            synchronized(list) {
                    id  = (String)list.removeLast();
            }
            RegistryObjectImpl ro = (RegistryObjectImpl)cache.remove(id);
            //will be garbage collected when not references to object
        } 
        synchronized (this) {
            cache.put(rid, registryObject);
        }
     
    }
        
        
    // looks in cache, if object in cache retrieves it
    // if the object isLoaded we are done -
    // else get the object from the UDDI registry given
    // its service
    //are we putting associations in cache?
    public void fetchAssociations(RegistryObjectImpl registryObject, 
                                                    String serviceId) throws JAXRException {
       

        BulkResponse br = null;	

       if (registryObject != null) {
            String id = registryObject.getKey().getId();
            if (id == null) return;
            if (registryObject.areAssociationsLoaded() == true) 
                return;
            else {
                String regServiceId =
                registryObject.getServiceId();
                RegistryServiceImpl service = 
                    getService(regServiceId);
                br = 
                    service.getUDDIMapper().findAssociations(null, id, null, null);
            }
        }
        if (br.getExceptions() == null) {
            Collection registryObjects = br.getCollection();
            Iterator iter = registryObjects.iterator();
            while (iter.hasNext()) {
                RegistryObjectImpl ro = 
                        (RegistryObjectImpl)iter.next();
                add(ro);
                ro.setAssociationsLoaded(true);
            }				
        } 
    }
    
    void flushCache() {
        synchronized (this) {
            cache.clear();
        }
    } 
    
    void addToRegistryServices(RegistryServiceImpl service) {	
		synchronized(registryServices) {
			if (!registryServices.contains(service) )
				registryServices.add(service);
		}			
	}
	
    public RegistryServiceImpl getService(String id) {
		//look in the collection of services for service id
		synchronized(registryServices) {
			Iterator iter = registryServices.iterator();
			while (iter.hasNext()) {		
				RegistryServiceImpl service = 
					(RegistryServiceImpl) iter.next();
				if (service.getServiceId().equals(id))
					return service;
			}		
		}
		return null;		
    }
	
}
