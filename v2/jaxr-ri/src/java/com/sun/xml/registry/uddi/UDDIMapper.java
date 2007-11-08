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


/*
 * UDDIMapper.java
 *
 * Created on May 14, 2001, 9:17 AM
 */
package com.sun.xml.registry.uddi;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;



import com.sun.xml.registry.common.*;
import com.sun.xml.registry.common.util.*;
import com.sun.xml.registry.uddi.infomodel.*;
import com.sun.xml.registry.uddi.infomodel.PersonNameImpl;
import com.sun.xml.registry.common.tools.*;
import com.sun.xml.registry.uddi.bindings_v2_2.*;

import javax.xml.parsers.*;
import javax.xml.parsers.DocumentBuilder;

import java.io.*;

import java.net.*;
import java.util.*;

import java.lang.*;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.w3c.dom.*;

import java.util.logging.Logger;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPBody;

/**
 *
 *
 * @author  kwalsh
 * @version 1.0.5
 *
 */
public class UDDIMapper extends JAXRConstants{
    
    Logger logger = (Logger)
    AccessController.doPrivileged(new PrivilegedAction() {
        public Object run() {
            return Logger.getLogger(com.sun.xml.registry.common.util.Utility.LOGGING_DOMAIN + ".uddi");
        }
    });
    
    private static XMLUtil xmlUtil;
    private static MarshallerUtil marshallerUtil;
    
    
    private UDDIObjectCache objectManager = null;
    private RegistryServiceImpl service = null;
    private ConnectionImpl connection = null;
    private static JAXRConceptsManager manager = null;
    
    private Collection fromKeysOwned;
    private Collection toKeysOwned;
    
    private ClassificationScheme defaultPostalScheme;
    private ClassificationScheme jaxrPostalAddressScheme;
    private HashMap postalAddressMap;
    private HashMap equivalentConcepts;
    private HashMap semanticEquivalences;
    private long tokenTime;
    private Processor processor;
    private MapperHelpers helper;
    
    //for jaxb
    private JAXBContext jc;
    private ObjectFactory objFactory;
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    public UDDIMapper(RegistryService service) {
        this.service = (RegistryServiceImpl) service;
        //objectManager needs creation in Constructor
        this.objectManager =
                new UDDIObjectCache((RegistryServiceImpl)service);
        this.xmlUtil = XMLUtil.getInstance();
        try {
            marshallerUtil = marshallerUtil.getInstance();
        } catch (JAXBException jbe){
            System.out.println("Failure to initialize mapper");
        }
        this.helper = new MapperHelpers();
        initJAXBObjectFactory();
        
        
    }
    
    public UDDIObjectCache getObjectManager() {
        return this.objectManager;
    }
    
    
    private void initJAXBObjectFactory(){
        // create a JAXBContext
        try {
            if (jc == null)
                jc = JAXBContext.newInstance( "com.sun.xml.registry.uddi.bindings_v2_2" );
        } catch (JAXBException jbe){
            logger.log(Level.SEVERE, "Exiting unable to initial JAXB context", jbe);
            System.exit(1);
        }
        // create an ObjectFactory instance.
        // if the JAXBContext had been created with mutiple pacakge names,
        // we would have to explicitly use the correct package name when
        // creating the ObjectFactory.
        if (objFactory == null)
            objFactory = new ObjectFactory();
        
    }
    
    private Processor getProcessor(){
        if (processor == null)
            processor = new Processor(this.service, this);
        return processor;
    }
    
    private JAXRConceptsManager getConceptsManager()
    throws JAXRException {
        if (manager == null)
            manager = JAXRConceptsManager.getInstance(service.getConnection());
        return manager;
    }
    
    private ConnectionImpl getConnection() {
        if (this.service != null) {
            if (this.connection == null)
                this.connection = service.getConnection();
        }
        return this.connection;
    }
    
    private void setConnection() {
        if (this.service != null) {
            if (this.connection == null)
                this.connection = service.getConnection();
        }
    }
    
    /**
     * Finds all Organizations that match ALL of the criteria specified by the parameters of this call.
     * This is a Logical AND operation between all non-null parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     *
     * @return BulkResponse containing Collection of Organizations
     *
     */
    BulkResponse findOrganizations(Collection findQualifiers,
            Collection namePatterns, Collection classifications,
            Collection specifications, Collection identifiers,
            Collection externalLinks ) throws JAXRException {
        
        FindBusiness findBusiness = null;
        boolean parameterSet = false;
        findBusiness = objFactory.createFindBusiness();
        findBusiness.setGeneric(UDDIVERSION);
        
        String maxrows = this.getConnection().getMaxRows();
        if (maxrows != null) {
            int mrows = Integer.parseInt(maxrows);
            findBusiness.setMaxRows(mrows);
        }
        
        FindQualifiers fQualifiers =
                strings2FindQualifiers(findQualifiers);
        if (fQualifiers != null)
            findBusiness.setFindQualifiers(fQualifiers);
        
        Collection names = namePatterns2Names(namePatterns, false);
        
        
        IdentifierBag ibag =
                externalIdentifiers2IdentifierBag(identifiers);
        
        CategoryBag cbag =
                classifications2CategoryBag(classifications);
        
        TModelBag tbag =
                concepts2TModelBag(specifications);
        
        DiscoveryURLs urls =
                externalLinks2DiscoveryURLs(externalLinks);
        
        if ((names != null) && (!names.isEmpty())){
            //this adheres to UDDI V2.0
            findBusiness.getName().addAll(names);
            parameterSet = true;
        }
        
        if (ibag != null) {
            findBusiness.setIdentifierBag(ibag);
            parameterSet = true;
        }
        
        if (cbag != null) {
            findBusiness.setCategoryBag(cbag);
            parameterSet = true;
        }
        if (tbag != null) {
            findBusiness.setTModelBag(tbag);
            parameterSet = true;
        }
        if (urls != null) {
            findBusiness.setDiscoveryURLs(urls);
            parameterSet = true;
        }
        
        if (!parameterSet)
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Find_Criteria_Set_for_FindOrganization"));
        
        return getProcessor().processRequestJAXB(findBusiness, false, null, FIND);
        
    }
    
    BulkResponse saveOrganizations(Collection organizations) throws JAXRException {
        
        SaveBusiness saveBusiness = null;
        saveBusiness = objFactory.createSaveBusiness();
        saveBusiness.setGeneric(UDDIVERSION);
        
        String authInfo = null;
        
        Collection businessEntities =
                organizations2BusinessEntities(organizations, true);
        
        if (!businessEntities.isEmpty()) {
            saveBusiness.getBusinessEntity().addAll(businessEntities);
        } else {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Organization_information_to_save") );
        }
        
        authInfo = getAuthInfo();
        
        if (authInfo == null) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Credentials_present"));
        }
        saveBusiness.setAuthInfo(authInfo);
        
        return getProcessor().processRequestJAXB(saveBusiness, true, null, SAVE);
    }
    
    private String  getAuthInfo() throws JAXRException {
        String authInfo = getConnection().getAuthToken();
        if ( (authInfo == null) || (tokenExpired() == true) ){
            String result = getAuthorizationToken(getConnection().getAuthCreds());
            if (result == null) result="";
            getConnection().setAuthToken(result.toCharArray());
            getConnection().setAuthTokenTimestamp(timeStamp());
            return result;
        }
        return authInfo;
    }
    
    private long timeStamp() {
        return System.currentTimeMillis();
    }
    
    private  boolean tokenExpired(){
        logger.finest("Last Token time " + tokenTime);
        long diffTime = timeStamp() - getConnection().getAuthTokenTimestamp();
        logger.finest("Elapsed time between authTokens " + diffTime);
        long defaultTimeout = getConnection().getTokenTimeout();
        logger.finest("DefaultTimeout " + defaultTimeout);
        if (diffTime > defaultTimeout)
            return true;
        else return false;
    }
    
    
    
    BulkResponse saveServices(Collection services)
    throws JAXRException {
        
        SaveService saveService = null;
        
        saveService = objFactory.createSaveService();
        saveService.setGeneric(UDDIVERSION);
        
        String authInfo = null;
        
        Collection businessServices =
                services2BusinessServicesCollection(services, true);
        
        
        if (businessServices != null) {
            saveService.getBusinessService().addAll(businessServices);
        } else {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Service_Information_to_Save_or_Update"));
        }
        
        authInfo = getAuthInfo();
        if (authInfo == null) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Credential_Infomation"));
        }
        saveService.setAuthInfo(authInfo);
        
        return getProcessor().processRequestJAXB(saveService, true, null, SAVE);
    }
    
    
    BulkResponse saveServiceBindings(Collection serviceBindings)
    throws JAXRException {
        boolean publish = true;
        SaveBinding saveBindings = null;
        saveBindings = objFactory.createSaveBinding();
        saveBindings.setGeneric(UDDIVERSION);
        
        String authInfo = null;
        
        Collection bindingTemplates =
                serviceBindings2BindingTemplatesCollection(serviceBindings, publish);
        
        if (bindingTemplates != null) {
            saveBindings.getBindingTemplate().addAll(bindingTemplates);
        } else {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Binding_Information_to_be_saved_or_updated"));
        }
        authInfo = getAuthInfo();
        if (authInfo == null) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Credential_or_Invalid_Credential_Information"));
        }
        saveBindings.setAuthInfo(authInfo);
        
        return getProcessor().processRequestJAXB(saveBindings, true, null, SAVE);
    }
    
    
    BulkResponse saveConcepts(Collection concepts)
    throws JAXRException {
        
        boolean publish = true;
        SaveTModel saveTModel = null;
        saveTModel = objFactory.createSaveTModel();
        saveTModel.setGeneric(UDDIVERSION);
        
        Collection tModels =
                concepts2TModels(concepts, publish);
        
        if ((tModels != null) && (!tModels.isEmpty())) {
            saveTModel.getTModel().addAll(tModels);
        } else {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Concept_Information_or_Invalid_Concept_Information"));
        }
        String authInfo = getAuthInfo();
        if (authInfo == null) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Credentials_or_Invalid_Credentials"));
        }
        saveTModel.setAuthInfo(authInfo);
        
        return getProcessor().processRequestJAXB(saveTModel, true, null, SAVE);
    }
    
    BulkResponse saveClassificationSchemes(Collection schemes)
    throws JAXRException {
        
        boolean publish=true;
        
        SaveTModel saveTModel = null;
        saveTModel = objFactory.createSaveTModel();
        
        saveTModel.setGeneric(UDDIVERSION);
        
        Collection tModels =
                classificationSchemes2TModels(schemes, publish);
        
        if ((tModels != null) && (!tModels.isEmpty())) {
            saveTModel.getTModel().addAll(tModels);
        } else {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Concept_Information_or_Invalid_Concept_Information"));
        }
        
        String authInfo = getAuthInfo();
        if (authInfo == null) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Credentials_or_Invalid_Credentials"));
        }
        saveTModel.setAuthInfo(authInfo);
        
        return getProcessor().processRequestJAXB(saveTModel, true, null, SAVE);
    }
    
    
    BulkResponse deleteOrganizations(Collection organizationKeys)
    throws JAXRException {
        
        DeleteBusiness deleteBusiness =  null;
        deleteBusiness = objFactory.createDeleteBusiness();
        deleteBusiness.setGeneric(UDDIVERSION);
        
        Collection keys = keys2Keys(organizationKeys);
        if ((keys != null) && (!keys.isEmpty())) {
            deleteBusiness.getBusinessKey().addAll(keys);
        } else {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Business_Identified_to_Delete"));
        }
        String authInfo = getAuthInfo();
        if (authInfo == null) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Credentials_or_Invalid_Credentials"));
        }
        deleteBusiness.setAuthInfo(authInfo);
        
        return getProcessor().processRequestJAXB(deleteBusiness, true, organizationKeys, DELETE);
    }
    
    BulkResponse deleteServices(Collection serviceKeys) throws JAXRException {
        
        DeleteService  deleteService = null;
        deleteService = objFactory.createDeleteService();
        deleteService.setGeneric(UDDIVERSION);
        
        Collection keys = keys2Keys(serviceKeys);
        if ((keys != null) && (!keys.isEmpty())) {
            deleteService.getServiceKey().addAll(keys);
        } else {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Service_Keys_supplied_for_deletion"));
        }
        String authInfo = getAuthInfo();
        if (authInfo == null) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Invalid_Credential_Information"));
        }
        deleteService.setAuthInfo(authInfo);
        
        return getProcessor().processRequestJAXB(deleteService, true, serviceKeys, DELETE);
    }
    
    BulkResponse deleteServiceBindings(Collection bindingKeys) throws JAXRException {
        
        DeleteBinding deleteBinding = null;
        deleteBinding = objFactory.createDeleteBinding();
        deleteBinding.setGeneric(UDDIVERSION);
        
        Collection keys = keys2Keys(bindingKeys);
        if ((keys != null) && (!keys.isEmpty())) {
            deleteBinding.getBindingKey().addAll(keys);
        } else {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Service_Binding_Keys_supplied_for_deletion"));
        }
        
        String authInfo = getAuthInfo();
        if (authInfo == null) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Invalid_Credential_Information"));
        }
        deleteBinding.setAuthInfo(authInfo);
        return getProcessor().processRequestJAXB(deleteBinding, true, bindingKeys, DELETE);
    }
    
    BulkResponse deleteConcepts(Collection conceptKeys) throws JAXRException {
        
        DeleteTModel deleteTModel = null;
        deleteTModel = objFactory.createDeleteTModel();
        deleteTModel.setGeneric(UDDIVERSION);
        
        Collection keys = keys2Keys(conceptKeys);
        if ((keys != null) && (!keys.isEmpty())) {
            deleteTModel.getTModelKey().addAll(keys);
        } else {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Service_Binding_Keys_supplied_for_deletion"));
        }
        
        String authInfo = getAuthInfo();
        if (authInfo == null) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Invalid_Credential_Information"));
        }
        deleteTModel.setAuthInfo(authInfo);
        
        return getProcessor().processRequestJAXB(deleteTModel, true, conceptKeys, DELETE);
    }
    
    BulkResponse deleteAssociations(Collection associationKeys) throws JAXRException {
        
        DeletePublisherAssertions publisherAssertions = null;
        publisherAssertions = objFactory.createDeletePublisherAssertions();
        
        publisherAssertions.setGeneric(UDDIVERSION);
        
        Collection assertions =
                associationKeys2PublisherAssertions(associationKeys);
        publisherAssertions.getPublisherAssertion().addAll(assertions);
        
        String authInfo = getAuthInfo();
        if (authInfo == null) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Invalid_Credential_Information"));
        }
        publisherAssertions.setAuthInfo(authInfo);
        //return dispositionReport
        return getProcessor().processRequestJAXB(publisherAssertions, true, associationKeys, DELETE);
    }
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    
    /**
     * Finds all Services that match ALL of the criteria specified by the parameters of this call.
     * This is a Logical AND operation between all non-null parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     *
     * @param orgKey Key identifying an Organization. Required for UDDI providers.
     */
    BulkResponse findServices(Key orgKey,
            Collection findQualifiers,
            Collection namePatterns,
            Collection classifications,
            Collection specifications
            ) throws JAXRException {
        
        FindService findService = null;
        boolean parameterSet = false;
        
        findService = objFactory.createFindService();
        findService.setGeneric(UDDIVERSION);
        // findService.setMaxRows(150);
        //todo:maxrows?
        FindQualifiers fQualifiers =
                strings2FindQualifiers(findQualifiers);
        if (fQualifiers != null)
            findService.setFindQualifiers(fQualifiers);
        
        //todo: key can now be null
        String key = key2Key(orgKey);
        findService.setBusinessKey(key);
        Collection names = namePatterns2Names(namePatterns, false);
        CategoryBag cbag =
                classifications2CategoryBag(classifications);
        
        TModelBag tbag =
                concepts2TModelBag(specifications);
        
        if ((names != null) && (!names.isEmpty())) {
            findService.getName().addAll(names);
            parameterSet = true;
        }
        
        if (cbag != null) {
            findService.setCategoryBag(cbag);
            parameterSet = true;
        }
        
        if (tbag != null) {
            findService.setTModelBag(tbag);
            parameterSet = true;
        }
        
        if (!parameterSet) {
            throw new JAXRException( ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Find_Criteria_Specified") );
        }
        
        return getProcessor().processRequestJAXB(findService, false, null, FIND);
    }
    
    
    
    /**
     * Finds all ServiceBindings that match ALL of the criteria specified by the parameters of this call.
     * This is a Logical AND operation between all non-null parameters.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param serviceKey Key identifying a Service. Required for UDDI providers.
     *
     *
     * @return BulkResponse containing Collection of ServiceBindings
     */
    BulkResponse findServiceBindings(
            Key serviceKey,
            Collection findQualifiers,
            Collection classifications,
            Collection specifications
            ) throws JAXRException {
        
        
        FindBinding findBinding = null;
        boolean parameterSet = false;
        
        findBinding = objFactory.createFindBinding();
        findBinding.setGeneric(UDDIVERSION);
        
        String maxrows = this.getConnection().getMaxRows();
        if (maxrows != null) {
            int rows = Integer.parseInt(maxrows);
            findBinding.setMaxRows(rows);
        }
        
        FindQualifiers fQualifiers =
                strings2FindQualifiers(findQualifiers );
        if (fQualifiers != null)
            findBinding.setFindQualifiers(fQualifiers);
        
        String key = key2Key(serviceKey);
        if (key == null)
            throw new JAXRException( ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Service_Key_must_be_specified") );
        else {
            findBinding.setServiceKey(key);
        }
        
        TModelBag tbag =
                concepts2TModelBag(specifications);
        
        if (tbag != null) {
            findBinding.setTModelBag(tbag);
            parameterSet = true;
        }
        
        if (!parameterSet) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Find_Criteria_Specified") );
        }
        return getProcessor().processRequestJAXB(findBinding, false, null, FIND);
    }
    
    
    BulkResponse findConcepts(
            Collection findQualifiers,
            Collection namePatterns,
            Collection classifications,
            Collection externalIdentifiers,
            Collection externalLinks ) throws JAXRException {
        
        FindTModel findTModel = null;
        boolean parameterSet = false;
        
        
        findTModel = objFactory.createFindTModel();
        findTModel.setGeneric(UDDIVERSION);
        
        String maxrows = this.getConnection().getMaxRows();
        if (maxrows != null) {
            int rows = Integer.parseInt(maxrows);
            findTModel.setMaxRows(rows);
        }
        
        // can always transform this
        FindQualifiers fQualifiers =
                strings2FindQualifiers(findQualifiers);
        if (fQualifiers != null)
            findTModel.setFindQualifiers(fQualifiers);
        
        Collection names =
                namePatterns2Names(namePatterns, false);
        
        CategoryBag cbag =
                classifications2CategoryBag(classifications);
        
        IdentifierBag ibag =
                externalIdentifiers2IdentifierBag(externalIdentifiers);
        
        if (cbag != null) {
            findTModel.setCategoryBag(cbag);
            parameterSet = true;
        }
        
        if (ibag != null) {
            findTModel.setIdentifierBag(ibag);
            parameterSet = true;
        }
        
        
        ArrayList responses = new ArrayList();
        int len = names.size();
        if ((names != null) && (len > 0)) {
            for (int i = 0; i<len;i++) {
                findTModel.setName((Name)((ArrayList)names).get(i));
                BulkResponse br = getProcessor().processRequestJAXB(findTModel, false, null, FIND);
                Collection al = br.getCollection();
                Iterator it1 = al.iterator();
                responses.add(br);
            }
            
            //Removing duplicate entries
            try {
                int size = responses.size();
                for ( int iterator=0; iterator < size;  ) {
                    BulkResponse bulkresponse1 = (BulkResponse)responses.get(iterator);
                    BulkResponse bulkresponse2 = null;
                    if ( iterator+1 < size ) 
                        bulkresponse2 = (BulkResponse)responses.get(iterator+1);
                    if ( bulkresponse1 != null && bulkresponse2 != null ) {
                        Iterator it1 = bulkresponse1.getCollection().iterator();
                        Iterator it2 = bulkresponse2.getCollection().iterator();

                        boolean found = true;
                        while ( it1.hasNext() && it2.hasNext() ) {
                            if ( ((KeyImpl) ((ConceptImpl)it1.next()).getKey()).getId().
                                    equals( ((KeyImpl) ((ConceptImpl)it2.next()).getKey()).getId() ) ) {
                                found = true;
                            } else {
                                found = false;
                            }
                        }
                        
                        if ( found ) {
                            if ( !it1.hasNext() ) {
                                responses.remove(iterator);
                            } else if ( !it2.hasNext() ) {
                                responses.remove(iterator+1);
                            }
                        }
                    }
                    iterator = iterator + 2;
                }
            } catch (ClassCastException e) {
                //do nothing
            }
            
            BulkResponse bResponse =
                    BulkResponseImpl.combineBulkResponses(responses);
            return helper.extractRegistryObjectByClass(bResponse, "Concept");
            
        }
        if (!parameterSet)
            throw new JAXRException( ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Find_Criteria_Specified_specified") );
        BulkResponse response = getProcessor().processRequestJAXB(findTModel, false, null, FIND);
        return helper.extractRegistryObjectByClass(response, "Concept");
    }
    
    BulkResponse findClassificationSchemes(
            Collection findQualifiers,
            Collection namePatterns,
            Collection classifications,
            Collection externalLinks) throws JAXRException {
        
        return findClassificationSchemes(findQualifiers,
                namePatterns,
                classifications,
                externalLinks,
                true);
        
    }
    
    BulkResponse findClassificationSchemes(
            Collection findQualifiers,
            Collection namePatterns,
            Collection classifications,
            Collection externalLinks,
            boolean cull) throws JAXRException {
        
        ArrayList responses = new ArrayList();
        Collection schemes = null;
        BulkResponseImpl response = new BulkResponseImpl();
        if ((classifications == null) && (externalLinks == null)) {
            if (namePatterns != null) {
                schemes =
                        getConceptsManager().findClassificationSchemes(findQualifiers,
                        namePatterns, classifications, externalLinks);
                
                if (schemes.size() > 0) {
                    Iterator siter = schemes.iterator();
                    while (siter.hasNext()) {
                        try {
                            ClassificationScheme scheme =
                                    (ClassificationScheme) siter.next();
                            if (scheme != null) {
                                objectManager.addObjectToCache((RegistryObjectImpl)scheme,
                                        this.service.getServiceId());
                            }
                        } catch (ClassCastException cce) {
                            logger.log(Level.SEVERE, cce.getMessage(), cce);
                        }
                        response.setCollection(schemes);
                    }
                    responses.add(response);
                }
            }
        }
        
        //ok we are here so lets see if we can find them in the registry
        BulkResponse br = null;
        FindTModel findTModel = null;
        boolean parameterSet = false;
        Document doc = null;
        
        findTModel = objFactory.createFindTModel();
        findTModel.setGeneric(UDDIVERSION);
        
        String maxrows = this.getConnection().getMaxRows();
        if (maxrows != null) {
            int rows = Integer.parseInt(maxrows);
            findTModel.setMaxRows(rows);
        }
        
        // can always transform this
        FindQualifiers fQualifiers =
                strings2FindQualifiers(findQualifiers);
        if (fQualifiers != null)
            findTModel.setFindQualifiers(fQualifiers);
        
        Collection names =
                namePatterns2Names(namePatterns, false);
        
        CategoryBag cbag =
                classifications2CategoryBag(classifications);
        
        if (cbag != null) {
            findTModel.setCategoryBag(cbag);
            parameterSet = true;
        }
        
        int len = names.size();
        if ((names != null) && (len > 0)) {
            for (int i = 0; i<len;i++) {
                
                findTModel.setName((Name)((ArrayList)names).get(i));
                BulkResponse brr = getProcessor().processRequestJAXB(findTModel, false, null, FIND);
                responses.add(brr);
            }
            BulkResponse bResponse =
                    BulkResponseImpl.combineBulkResponses(responses);
            if (cull)
                return helper.cullDuplicates(helper.extractRegistryObjectByClass(bResponse, "ClassificationScheme"));
            
            return helper.extractRegistryObjectByClass(bResponse, "ClassificationScheme");
            
        }
        if (!parameterSet)
            throw new JAXRException( ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Find_Criteria_Specified_specified") );
        
        BulkResponse cresponse = getProcessor().processRequestJAXB(findTModel, false, null, FIND);
        responses.add(cresponse);
        BulkResponse fresponse = BulkResponseImpl.combineBulkResponses(responses);
        return helper.cullDuplicates(helper.extractRegistryObjectByClass(fresponse, "ClassificationScheme"));
    }
    
    
    /**
     * Find a ClassificationScheme by name based on the specified name pattern.
     * If specified name pattern matches more than one ClassificationScheme then
     * the one that is returned is implementation dependent.
     *
     * @param namePattern Is a String that is a partial or full
     * name pattern with wildcard searching as specified by the SQL-92 LIKE
     * specification.
     *
     */
    ClassificationScheme findClassificationSchemeByName(Collection findQualifiers, String namePattern)
    throws JAXRException {
        
        Collection namePatterns = new ArrayList();
        namePatterns.add(namePattern);
        BulkResponse br = findClassificationSchemes(
                null,
                namePatterns,
                null,
                null,
                true);
        
        if (br != null) {
            if (br.getExceptions() == null) {
                Collection schemes = br.getCollection();
                int size = schemes.size();
                logger.finest("Found schemes " + size);
                
                if ( size > 1) {
                    throw new InvalidRequestException(java.util.ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("Multiple_Schemes_matching_name_pattern"));
                }
                Iterator iter = schemes.iterator();
                while (iter.hasNext()) {
                    try {
                        ClassificationScheme cScheme =
                                (ClassificationScheme)iter.next();
                        return cScheme;
                    } catch (ClassCastException cce) {
                        logger.log(Level.SEVERE, cce.getMessage(), cce);
                        throw new UnexpectedObjectException(java.util.ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("Expected_ClassificationScheme"), cce);
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Find a Concept based on the path specified.
     * If specified path matches more than one ClassificationScheme then
     * the one that is most general (higher in the concept hierarchy) is returned.
     *
     *
     * @param path Is an XPATH expression that identifies the Concept.
     *
     */
    Concept findConceptByPath(String path) throws JAXRException {
        
        if (path != null){
            return getConceptsManager().findConceptByPath(path);
        }
        return null;
    }
    
    public Collection getChildConcepts(ClassificationScheme scheme) throws JAXRException {
        
        Collection concepts = null;
        if (((ClassificationSchemeImpl)scheme).isPredefined()){
            concepts =
                    getConceptsManager().getChildConcepts(scheme);
        }
        return concepts;
    }
    
    
    KeyedReference associationType2KeyedReference(Object associationType)
    throws JAXRException {
        
        if (associationType == null)
            return null;
        
        String typeName = null;
        String typeValue = null;
        if (associationType instanceof Concept) {
            typeName = ((Concept)associationType).getName().getValue();
            typeValue = ((Concept)associationType).getValue();
        } else if (associationType instanceof String){
            typeName = (String)associationType;
            typeValue = (String)associationType;
        }
        
        String keyName = null;
        String keyValue = null;
        if (typeValue.equalsIgnoreCase(EQUIVALENT_TO)) {
            keyName = IDENTITY;
            keyValue = IDENTITY;
        } else if (typeValue.equalsIgnoreCase(RELATES_TO)) {
            keyName = PEER_TO_PEER;
            keyValue = PEER_TO_PEER;
        } else if (typeValue.equalsIgnoreCase(HAS_CHILD)) {
            keyName = PARENT_TO_CHILD;
            keyValue = PARENT_TO_CHILD;
        } else {
            keyName = typeName;
            keyValue = typeValue;
        }
        
        KeyedReference keyedReference = null;
        keyedReference = objFactory.createKeyedReference();
        keyedReference.setTModelKey(RELATIONSHIPS);
        if (keyName != null)
            keyedReference.setKeyName(keyName);
        else throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Association_Type_required_to_save_Association_to_Registry"));
        
        //need to have keyValue set otherwise don't use KeyedRef
        if (keyValue != null)
            keyedReference.setKeyValue(keyValue);
        else throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Association_Type_required_to_save_Association_to_Registry"));
        
        return keyedReference;
    }
    
    Collection associationTypes2KeyedReferences(Collection associationTypes)
    throws JAXRException {
        
        Collection keyedReferences = new ArrayList();
        if (associationTypes != null) {
            Iterator iter = associationTypes.iterator();
            while (iter.hasNext()) {
                KeyedReference ref =
                        associationType2KeyedReference(iter.next());
                if (ref != null)
                    keyedReferences.add(ref);
            }
        }
        
        return keyedReferences;
    }
    
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    
    BulkResponse getOrganizations(Collection orgKeys) throws JAXRException {
        
        GetBusinessDetail detail = null;
        
        detail = objFactory.createGetBusinessDetail();
        detail.setGeneric(UDDIVERSION);
        
        Collection keys = keys2Keys(orgKeys);
        if ((keys != null) && (!keys.isEmpty())) {
            detail.getBusinessKey().addAll(keys);
        } else throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Organization_Keys_supplied"));
        
        return getProcessor().processRequestJAXB(detail, false, orgKeys, FIND);
    }
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    
    BulkResponse getServices(Collection orgKeys) throws JAXRException {
        
        GetServiceDetail detail = null;
        
        detail = objFactory.createGetServiceDetail();
        
        detail.setGeneric(UDDIVERSION);
        
        Collection keys = keys2Keys(orgKeys);
        if ((keys != null) && (!keys.isEmpty())) {
            detail.getServiceKey().addAll(keys);
        } else throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_service_Keys_Supplied"));
        
        return getProcessor().processRequestJAXB(detail, false, null, FIND);
    }
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    
    BulkResponse getServiceBindings(Collection bindingKeys) throws JAXRException {
        
        GetBindingDetail detail = null;
        
        detail = objFactory.createGetBindingDetail();
        
        detail.setGeneric(UDDIVERSION);
        
        Collection keys = keys2Keys(bindingKeys);
        if ((keys != null) && (!keys.isEmpty())) {
            detail.getBindingKey().addAll(keys);
        } else throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_ServiceBinding_Keys_supplied"));
        
        return getProcessor().processRequestJAXB(detail, false, null, FIND);
        
    }
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    
    BulkResponse getConcepts(Collection conceptKeys) throws JAXRException {
        
        GetTModelDetail detail = null;
        
        detail = objFactory.createGetTModelDetail();
        detail.setGeneric(UDDIVERSION);
        
        Collection keys = keys2Keys(conceptKeys);
        
        
        if ((keys != null) && (!keys.isEmpty())) {
            detail.getTModelKey().addAll(keys);
        } else throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Concept_keys_supplied"));
        
        return getProcessor().processRequestJAXB(detail, false, null, FIND);
    }
    
    
    /**
     *
     * Transforms a Collection of JAXR ExternalIdentifiers
     * to a UDDI IdentifierBag
     *
     * @param     identifiers   Collection of JAXR ExternalIdentifiers
     * @return    IdentifierBag Transformed UDDI identifier bag
     * @exception JAXRException
     *
     */
    IdentifierBag externalIdentifiers2IdentifierBag(Collection identifiers)
    throws UnexpectedObjectException, JAXRException {
        
        IdentifierBag ibag = null;
        if ((identifiers != null) && (!identifiers.isEmpty())) {
            
            ibag = objFactory.createIdentifierBag();
            
            try {
                
                ArrayList keyedRefs = new ArrayList();
                Iterator iter = identifiers.iterator();
                while(iter.hasNext()){
                    KeyedReference kref =
                            externalIdentifier2KeyedReference((ExternalIdentifier)iter.next());
                    if (kref != null){
                        keyedRefs.add(kref);
                    }
                }
                ibag.getKeyedReference().addAll(keyedRefs);
            } catch (ClassCastException cce) {
                logger.log(Level.WARNING, cce.getMessage(), cce);
                throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_ExternalIdentifier"), cce);
            }
        }
        return ibag;
    }
    
    
    /**
     *
     * Transforms a Collection of JAXR Organizations to UDDI BusinessEntities
     *
     *
     * @param       organizations Collection of organizations to transform
     * @return      BusinessEntity[]
     * @exception   JAXRException
     *
     */
    Collection organizations2BusinessEntities(Collection orgs, boolean publish)
    throws JAXRException {
        
        ArrayList entities = new ArrayList();
        if ((orgs != null) && (!orgs.isEmpty())) {
            Iterator iter = orgs.iterator();
            try {
                while (iter.hasNext()){
                    BusinessEntity businessEntity =
                            organization2BusinessEntity((OrganizationImpl)iter.next(), publish);
                    if (businessEntity != null) {
                        entities.add(businessEntity);
                    }
                }
            } catch (ClassCastException cce) {
                logger.log(Level.WARNING, cce.getMessage(), cce);
                throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_Organization"), cce);
            }
        }
        return entities;
    }
    
    /**
     *
     * Transforms a Collection of JAXR Organizations to UDDI BusinessEntities
     *
     *
     * @param       organizations       A JAXR organizations to transform
     * @return      BusinessEntity      The transformed UDDI BusinessEntity
     * @exception   JAXRException
     *
     */
    BusinessEntity organization2BusinessEntity(OrganizationImpl org, boolean publish)
    throws JAXRException {
        
        BusinessEntity entity = null;
        
        if (org != null) {
            entity = objFactory.createBusinessEntity();
            
            //get all the Org Data Members
            //use statics
            Key key =  org.getKey();
            Slot authName = org.getSlot("authorizedName");
            Slot operator = org.getSlot("operator");
            
            Collection names = getNames(org, publish);
            Collection descriptions = getDescriptions(org, publish);
            
            Collection users = org.getUsers();
            User primaryContact = org.getPrimaryContact();
            Collection services = org.getServices();
            Collection externalIdentifiers =
                    org.getExternalIdentifiers();
            Collection classifications =
                    org.getClassifications();
            Collection externalLinks = org.getExternalLinks();
            
            // let's do the easy transformations first
            //tod: - look at spec - key can now be null I think
            if (key != null) {
                String id = key.getId();
                if (id != null)
                    entity.setBusinessKey(id);
            } else entity.setBusinessKey("");
            
            //need to check if this is save or update/replace
            if (names != null) {
                entity.getName().addAll(names);
            } else throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:The_Organization_name_must_be_specified_to_save_an_Organization"));
            
            if (descriptions != null) {
                entity.getDescription().addAll(descriptions);
            }
            
            //Deal with Slots - need slot util method
            if (authName != null) {
                Collection values = authName.getValues();
                //todo: is values ever going to be null?
                //should only be 1 auth name
                if ((values !=null) && (!values.isEmpty())) {
                    String authNameString = (String) values.toArray()[0];
                    if (authNameString != null)
                        entity.setAuthorizedName(authNameString);
                }
            }
            
            if (operator != null) {
                Collection values = operator.getValues();
                if ((values !=null) && (!values.isEmpty())) {
                    //should only be 1 auth name
                    String operatorString = (String) values.toArray()[0];
                    if (operatorString != null)
                        entity.setOperator(operatorString);
                }
            }
            
            if ((externalIdentifiers != null) && (!externalIdentifiers.isEmpty())) {
                IdentifierBag ibag =
                        externalIdentifiers2IdentifierBag(externalIdentifiers);
                if (ibag != null)
                    entity.setIdentifierBag(ibag);
            }
            
            if ((classifications != null) && (!classifications.isEmpty())) {
                CategoryBag cbag =
                        classifications2CategoryBag(classifications);
                if (cbag != null)
                    entity.setCategoryBag(cbag);
            }
            
            if ((externalLinks != null) && (!externalLinks.isEmpty())) {
                DiscoveryURLs dUrl =
                        externalLinks2DiscoveryURLs(externalLinks);
                if (dUrl != null)
                    entity.setDiscoveryURLs(dUrl);
            }
            
            if ((services != null) && (!services.isEmpty())) {
                BusinessServices bServices =
                        services2BusinessServices(services, publish);
                if (bServices != null)
                    entity.setBusinessServices(bServices);
            }
            
            if ((users != null) && (!users.isEmpty())) {
                Contacts contacts = users2Contacts(users, publish);
                if (contacts != null)
                    entity.setContacts(contacts);
            }
        }
        return entity;
    }
    
    
    /**
     *
     * Transforms an IdentifierBag of UDDI Identifiers to a Collection
     * of JAXR ExternalIdentifiers
     *
     * @param       ibag        Collection of UDDI identifiers
     * @return      Collection  Colection of JAXR ExternalIdentifiers
     * @exception   JAXRException
     *
     */
    Collection identifierBag2ExternalIdentifiers(IdentifierBag ibag) throws JAXRException {
        Collection identifiers = null;
        if (ibag != null) {
            identifiers = new ArrayList();
            
            Collection keyedReference =
                    ibag.getKeyedReference();
            Iterator iter = keyedReference.iterator();
            while (iter.hasNext()) {
                ExternalIdentifier identifier =
                        keyedReference2ExternalIdentifier((KeyedReference)iter.next());
                if (identifier != null) {
                    identifiers.add(identifier);
                }
            }
        }
        return identifiers;
    }
    
    
    /**
     *
     * Transforms a collection of JAXR Services to UDDI BusinessServices
     *
     *
     * @param       services            Collection of JAXR Services
     * @return      BusinessServices    Essentially a UDDI Bag containing a
     *                                  list of BusinessService members
     * @exception   JAXRException
     *
     */
    BusinessServices services2BusinessServices(Collection services, boolean publish)
    throws JAXRException {
        
        BusinessServices businessServices = null;
        if (services != null) {
            businessServices = objFactory.createBusinessServices();
            
            try {
                Iterator iter = services.iterator();
                while (iter.hasNext()){
                    BusinessService businessService =
                            service2BusinessService((Service)iter.next(), publish);
                    if (businessService != null)
                        businessServices.getBusinessService().add(businessService);
                }
            } catch (ClassCastException cce) {
                logger.log(Level.WARNING, cce.getMessage(), cce);
                throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_Service"), cce);
            }
        }
        return businessServices;
    }
    
    Collection services2BusinessServicesCollection(Collection services, boolean publish) throws JAXRException {
        
        Collection businessServices = null;
        if (services != null) {
            businessServices = new ArrayList();
            
            try {
                Iterator iter = services.iterator();
                while (iter.hasNext()){
                    BusinessService businessService =
                            service2BusinessService((Service)iter.next(), publish);
                    if (businessService != null)
                        businessServices.add(businessService);
                }
            } catch (ClassCastException cce) {
                logger.log(Level.WARNING, cce.getMessage(), cce);
                throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_Service"), cce);
            }
        }
        return businessServices;
    }
    
    
    BusinessService service2BusinessService(Service service, boolean publish) throws JAXRException {
        
        BusinessService businessService = null;
        if (service != null) {
            businessService = objFactory.createBusinessService();
            //get the business key
            Organization org = service.getProvidingOrganization();
            if (org != null) {
                Key key = org.getKey();
                if (key != null) {
                    String id = key.getId();
                    if (id != null) {
                        businessService.setBusinessKey(id);
                        logger.finest("Setting business key");
                        logger.finest("key" + key);
                    }
                } else {
                    //todo: check spec can be null I think
                    logger.finest("Organization key is null");
                }
            } else {
                logger.warning(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Org_is_null"));
            }
            
            Key sKey = service.getKey();
            String sKeyString = null;
            if (sKey != null)
                sKeyString = sKey.getId();
            
            
            Collection name = getNames(service, publish);
            Collection description = getDescriptions(service, publish);
            
            Collection sbindings = service.getServiceBindings();
            Collection classifications = service.getClassifications();
            
            if (sKeyString != null)
                businessService.setServiceKey(sKeyString);
            else
                businessService.setServiceKey("");
            if (name != null)
                businessService.getName().addAll(name);
            if (description != null)
                businessService.getDescription().addAll(description);
            
            CategoryBag cbag =
                    classifications2CategoryBag(classifications);
            if (cbag != null)
                businessService.setCategoryBag(cbag);
            
            BindingTemplates bindingTemplates =
                    serviceBindings2BindingTemplates(sbindings, publish);
            if (bindingTemplates != null)
                businessService.setBindingTemplates(bindingTemplates);
        }
        return businessService;
    }
    
    BindingTemplates serviceBindings2BindingTemplates(Collection sbindings, boolean publish)
    throws JAXRException {
        
        BindingTemplates templates = null;
        
        if (sbindings != null) {
            templates = objFactory.createBindingTemplates();
            
            Iterator iter = sbindings.iterator();
            try {
                while (iter.hasNext()){
                    BindingTemplate bTemplate =
                            serviceBinding2BindingTemplate((ServiceBinding)iter.next(), publish);
                    if (bTemplate != null)
                        templates.getBindingTemplate().add(bTemplate);
                }
            } catch (ClassCastException cce) {
                logger.log(Level.WARNING, cce.getMessage(), cce);
                throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_ServiceBinding"), cce);
            }
        }
        return templates;
    }
    
    
    Collection serviceBindings2BindingTemplatesCollection(Collection sbindings, boolean publish)
    throws JAXRException {
        
        Collection templates = null;
        
        if (sbindings != null) {
            templates = new ArrayList();
            Iterator iter = sbindings.iterator();
            try {
                while (iter.hasNext()){
                    BindingTemplate bTemplate =
                            serviceBinding2BindingTemplate((ServiceBinding)iter.next(),publish);
                    if (bTemplate != null)
                        templates.add(bTemplate);
                }
            } catch (ClassCastException cce) {
                logger.log(Level.WARNING, cce.getMessage(), cce);
                throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_ServiceBinding"), cce);
            }
        }
        return templates;
    }
    
    BindingTemplate serviceBinding2BindingTemplate(ServiceBinding sbinding, boolean publish)
    throws JAXRException {
        
        BindingTemplate template = null;
        if (sbinding != null) {
            String bKeyString = null;
            
            template = objFactory.createBindingTemplate();
            
            //todo:why can't I use interfaces?
            Key bindingKey = sbinding.getKey();
            if (bindingKey != null)
                bKeyString = bindingKey.getId();
            
            Service service = sbinding.getService();
            if (service != null) {
                Key key = service.getKey();
                if (key != null) {
                    String id = key.getId();
                    if (id != null) {
                        template.setServiceKey(id);
                    } else
                        template.setServiceKey("");
                }
            } else {
                throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:A_ServiceBinding_must_be_associated_with_a_service."));
            }
            //todo - can't an serviceBinding have many descriptions?
            Description description = getDescription(sbinding);
            String accessURI = sbinding.getAccessURI();
            //do the easy ones first
            if (bKeyString != null)
                template.setBindingKey(bKeyString);
            else
                template.setBindingKey("");
            
            if (description != null)
                template.getDescription().add(description);
            
            ServiceBinding targetBinding =
                    sbinding.getTargetBinding();
            
            if ((accessURI == null) && (targetBinding == null))
                throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:AccessURI_or_targetBinding_needs_to_be_set_on_the_ServiceBinding_-_neither_is_set"));
            
            AccessPoint accessPoint = null;
            if (accessURI != null) {
                accessPoint = objFactory.createAccessPoint();
                
                accessPoint.setValue(accessURI);
                //get the URLType Concept
                Collection classifications = sbinding.getClassifications();
                //search for classification with concept specificed
                URLType urlType = null;
                Iterator citer = classifications.iterator();
                //this is if the userer has set the urlType
                boolean urlTypeSetByUser = false;
                while (citer.hasNext()) {
                    Classification classification = (Classification)citer.next();
                    Concept concept = classification.getConcept();
                    if (concept != null) {
                        //look at the concepts classificationScheme
                        ClassificationScheme cScheme = concept.getClassificationScheme();
                        InternationalString name = cScheme.getName();
                        String nameValue = null;
                        if (name != null){
                            nameValue = name.getValue();
                        }
                        
                        if (cScheme != null){
                            if (nameValue.equalsIgnoreCase("URLType")) {
                                //then we want this concepts value
                                String conceptValue = concept.getValue();
                                if (conceptValue.indexOf("https") != -1){
                                    urlType = URLType.HTTPS;
                                } else if (conceptValue.indexOf("http") != -1) {
                                    urlType = URLType.HTTP;
                                } else if (conceptValue.indexOf("ftp") != -1) {
                                    urlType = URLType.FTP;
                                } else if (conceptValue.indexOf("phone") != -1){
                                    urlType = URLType.PHONE;
                                } else if (conceptValue.indexOf("mailto") != -1) {
                                    urlType = URLType.MAILTO;
                                } else {
                                    urlType = URLType.OTHER;
                                }
                                urlTypeSetByUser = true;
                                break;
                            }
                            
                        }
                    }
                }
                if (!urlTypeSetByUser) {
                    urlType = helper.parseUrlForUrlType(accessURI);
                }
                if (urlType != null)
                    accessPoint.setURLType(urlType);
                else
                    accessPoint.setURLType(URLType.OTHER);
                
                template.setAccessPoint(accessPoint);
                
                
            } else {
                if (targetBinding != null) {
                    HostingRedirector redirector =
                            targetBinding2HostingRedirector(targetBinding);
                    if (redirector != null) {
                        template.setHostingRedirector(redirector);
                    }
                }
            }
            
            Collection specificationLinks = sbinding.getSpecificationLinks();
            TModelInstanceDetails tModelInstanceDetails= null;
            if (specificationLinks != null) {
                Collection tInstanceInfos =
                        specificationLinks2TModelInstanceInfos(specificationLinks, publish);
                
                tModelInstanceDetails = null;
                tModelInstanceDetails = objFactory.createTModelInstanceDetails();
                if (tInstanceInfos != null)
                    tModelInstanceDetails.getTModelInstanceInfo().addAll(tInstanceInfos);
                
                template.setTModelInstanceDetails(tModelInstanceDetails);
            }
        }
        return template;
    }
    
    
    Collection
            specificationLinks2TModelInstanceInfos(Collection specificationLinks, boolean publish)
            throws JAXRException {
        
        Collection tModelInstanceInfos = null;
        if (specificationLinks != null) {
            tModelInstanceInfos = new ArrayList();
            Iterator iter = specificationLinks.iterator();
            while (iter.hasNext()) {
                //get the data members
                SpecificationLink specLink = null;
                try {
                    specLink = (SpecificationLink) iter.next();
                } catch (ClassCastException cce) {
                    logger.log(Level.WARNING, cce.getMessage(), cce);
                    throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_SpecificationLink"), cce);
                }
                
                RegistryObject ro = specLink.getSpecificationObject();
                //TBXD - collection
                InternationalString usageDescription = specLink.getUsageDescription();
                Collection usageParms = specLink.getUsageParameters();
                Collection externalLinks = specLink.getExternalLinks();
                
                TModelInstanceInfo tMInstance = null;
                tMInstance = objFactory.createTModelInstanceInfo();
                if (ro != null) {           //isolate
                    Key key = ro.getKey();
                    if (key != null) {
                        String id = key.getId();
                        if (id != null) {
                            tMInstance.setTModelKey(id);
                        }
                    } else {
                        throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:The_Concept_Key_defining_the_Technical_interface_of_this_Service_Binding_must_be_supplied."));
                    }
                } else {
                    throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:The_Concept_defining_the_Technical_interface_of_this_Service_Binding_must_be_supplied."));
                }
                
                InstanceDetails details = null;
                if ((usageDescription != null) || (usageParms != null) ||
                        (externalLinks != null)) {
                    details = null;
                    details = objFactory.createInstanceDetails();
                    Collection desc = internationalString2Descriptions(usageDescription, publish);
                    if (usageDescription != null) {
                        details.getDescription().addAll(desc);
                    }
                    if (usageParms != null) {
                        Iterator iterb = usageParms.iterator();
                        if (iterb.hasNext()) {
                            details.setInstanceParms((String) iterb.next());
                        }
                    }
                    if (externalLinks != null) {
                        Iterator iterc = externalLinks.iterator();
                        if (iterc.hasNext()) {
                            OverviewDoc doc =
                                    externalLink2OverviewDoc(
                                    (ExternalLink) iterc.next(), publish);
                            if (doc != null)
                                details.setOverviewDoc(doc);
                        }
                    }
                }
                
                if (details != null) {
                    tMInstance.setInstanceDetails(details);
                    tModelInstanceInfos.add(tMInstance);
                }
            }
        }
        
        return tModelInstanceInfos;
    }
    
    HostingRedirector targetBinding2HostingRedirector(ServiceBinding tBinding)
    throws JAXRException {
        
        HostingRedirector redirector = null;
        if (tBinding != null) {
            redirector = null;
            redirector = objFactory.createHostingRedirector();
            Key key = tBinding.getKey();
            if (key != null){
                String keyString = key.getId();
                redirector.setBindingKey(keyString);
            } else
                redirector=null;
        }
        return redirector;
    }
    
    ServiceBinding hostingRedirector2TargetBinding(HostingRedirector redirector)
    throws JAXRException{
        ServiceBinding targetBinding = null;
        String key = redirector.getBindingKey();
        if (key != null) {
            //look in cache for serviceBinding
            targetBinding = (ServiceBinding)
            objectManager.fetchObjectFromCache(key);
            if (targetBinding == null){
                KeyImpl bindingKey = new KeyImpl(key);
                Collection bindingKeys = new ArrayList();
                bindingKeys.add(bindingKey);
                BulkResponse bulkResponse =
                        getServiceBindings(bindingKeys);
                if (bulkResponse.getExceptions() == null) {
                    //get the targetBinding
                    Collection bindings = bulkResponse.getCollection();
                    if (bindings != null) {
                        Iterator bindingIterator = bindings.iterator();
                        while (bindingIterator.hasNext()) {
                            targetBinding =
                                    (ServiceBinding) bindingIterator.next();
                            return targetBinding;
                        }
                    }
                }
            }
            return targetBinding;
        }
        return null;
    }
    
    Concept urlType2Concept(URLType urlType)
    throws JAXRException {
        
        String typeString = urlType.value();
        
        //ClassificationScheme scheme =
        Collection schemes =
                getConceptsManager().findClassificationSchemeByName(null,"URLType");
        //assume exact match
        ClassificationScheme scheme = (ClassificationScheme) schemes.iterator().next();
        
        if (scheme != null) {
            Collection childConcepts = scheme.getChildrenConcepts();
            if (childConcepts != null) {
                Iterator citer = childConcepts.iterator();
                while (citer.hasNext()) {
                    Concept concept = (Concept)citer.next();
                    InternationalString name = concept.getName();
                    String nameValue = name.getValue();
                    String conceptValue = concept.getValue();
                    if (typeString != null) {
                        if ((conceptValue != null) && (nameValue != null)){
                            if ((typeString.indexOf(nameValue) != -1) ||
                                    (typeString.indexOf(conceptValue) != -1)) {
                                return concept;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    Contacts users2Contacts(Collection users, boolean publish) throws JAXRException {
        
        Contacts contacts = null;
        contacts = objFactory.createContacts();
        if (users != null) {
            Iterator iter = users.iterator();
            try {
                
                while (iter.hasNext()){
                    Contact contact = user2Contact((User)iter.next(), publish);
                    if (contact != null) {
                        //add to contacts
                        contacts.getContact().add(contact);
                    }
                }
            } catch (ClassCastException cce) {
                logger.log(Level.WARNING, cce.getMessage(), cce);
                throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_User"), cce);
            }
        }
        return contacts;
    }
    
    Contact user2Contact(User user, boolean publish) throws JAXRException {
        
        Contact contact = null;
        if (user != null) {
            //lets get the user info first
            Collection telephoneNumbers = user.getTelephoneNumbers(null);
            //change this latter
            PersonNameImpl personName = (PersonNameImpl)user.getPersonName();
            //tbd Collection
            Collection postalAddresses =
                    user.getPostalAddresses();
            Collection emailAddresses = user.getEmailAddresses();
            
            Collection descriptions = getDescriptions(user, publish);
            String useType = user.getType();
            
            contact = null;
            contact = objFactory.createContact();
            
            if (personName != null) {
                String contactName = personName.getFullName();
                if (contactName != null)
                    contact.setPersonName(contactName);
            } else {
                return null;
            }
            
            if (descriptions != null) {
                contact.getDescription().addAll(descriptions);
            }
            if (useType != null)
                contact.setUseType(useType);
            
            //the telephone numbers
            if ((telephoneNumbers != null) && (!telephoneNumbers.isEmpty())) {
                Iterator iter = telephoneNumbers.iterator();
                try {
                    while (iter.hasNext()) {
                        Phone phone =
                                telephoneNumber2Phone(
                                (TelephoneNumber) iter.next());
                        if (phone != null) {}
                        contact.getPhone().add(phone);
                    }
                } catch (ClassCastException cce) {
                    logger.log(Level.WARNING, cce.getMessage(), cce);
                    throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_TelephoneNumber"), cce);
                }
            }
            
            if (emailAddresses != null && !emailAddresses.isEmpty()) {
                Iterator iter = emailAddresses.iterator();
                Collection emails = new ArrayList();
                try {
                    while (iter.hasNext()) {
                        EmailAddress emailAddress = (EmailAddress) iter.next();
                        String address = emailAddress.getAddress();
                        String type = emailAddress.getType();
                        //uddi email
                        Email email = null;
                        
                        email = objFactory.createEmail();
                        if (address != null)
                            email.setValue(address);
                        if (type != null)
                            email.setUseType(type);
                        emails.add(email);
                    }
                    contact.getEmail().addAll(emails);
                } catch (ClassCastException cce) {
                    logger.log(Level.WARNING, cce.getMessage(), cce);
                    throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_EmailAddress"), cce);
                }
            }
            
            if (postalAddresses != null) {
                Iterator piter = postalAddresses.iterator();
                
                while (piter.hasNext()) {
                    PostalAddress postalAddress = (PostalAddress)piter.next();
                    Address address = postalAddress2Address(postalAddress);
                    contact.getAddress().add(address);
                }
            }
        }
        return contact;
    }
    
    
    Phone telephoneNumber2Phone(TelephoneNumber telephoneNumber ) throws JAXRException {
        
        Phone phone = null;
        phone = objFactory.createPhone();
        if (telephoneNumber != null) {
            String type = telephoneNumber.getType();
            if (type != null) {
                phone.setUseType(type);
            }
            String number = telephoneNumber.getNumber();
            if (number != null)
                phone.setValue(number);
        }
        return phone;
    }
    
    Collection postalAddresses2Addresses(Collection postalAddresses) {
        return null;
    }
    
    Address postalAddress2Address(PostalAddress postalAddress) throws JAXRException {
        
        if (postalAddress == null)
            return null;
        
        initPostalSchemes();
        
        String type =
                //should this be getAddress--
                postalAddress.getType();
        logger.finest("Got type");
        Slot sortCode =
                postalAddress.getSlot("sortCode");
        logger.finest("SortCode");
        
        Address address = null;
        ClassificationScheme defaultScheme = defaultPostalScheme;
        
        address = postalAddressEquivalence2Address(defaultScheme, postalAddress);
        
        if (address == null) {
            try {
                Slot addressLines =
                        postalAddress.getSlot("addressLines");
                logger.finest("addressLines");
                
                address = objFactory.createAddress();
                Collection aLines = null;
                
                aLines =
                        postalAddressLines2AddressLines(addressLines);
                
                logger.finest("alines");
                if (aLines != null) {
                    address.getAddressLine().addAll(aLines);
                }
            } catch (ClassCastException cce) {
                throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_PostalAddress"), cce);
            }
        }
        
        if (address != null) {
            if (type != null) {
                address.setUseType(type);
            }
            
            if (sortCode != null) {
                Collection values = sortCode.getValues();
                //should just be 1 value
                if ((values != null) && (!values.isEmpty())) {
                    String value = (String)values.toArray()[0];
                    if (value != null)
                        address.setSortCode(value);
                }
            } else address.setSortCode("");
        }
        return address;
    }
    
    private Address postalAddressEquivalence2Address(ClassificationScheme defaultScheme,
            PostalAddress postalAddress) throws JAXRException {
        boolean unformated = false;
        //fix this to look for postalAddressScheme on postalAddress
        
        if (defaultScheme == null)
            unformated = true;
        
        ClassificationScheme postalScheme = null;
        if (postalAddress != null)
            postalScheme = postalAddress.getPostalScheme();
        
        String paId = null; //getDefaultPostalAddress from postalAddress passed
        Address address = null;
        //we know scheme is not null otherwise would have gotten this
        address = objFactory.createAddress();
        if (postalAddressMap == null) {
            mapPostalAddressAttributes(postalScheme);
            if (postalAddressMap == null) { //what should I do here?
                logger.warning(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_PostalAddressMapping"));
            }
        }
        
        
        
        Collection unformatedLines = new ArrayList();
        Collection addressLines = new ArrayList();
        //get the postalAddressAttributes
        String street = postalAddress.getStreet();
        if (street != null)
            unformatedLines.add(street);
        String streetNumber = postalAddress.getStreetNumber();
        if (streetNumber != null)
            unformatedLines.add(streetNumber);
        String city = postalAddress.getCity();
        if (city != null)
            unformatedLines.add(city);
        String state = postalAddress.getStateOrProvince();
        if (state != null)
            unformatedLines.add(state);
        String postalCode = postalAddress.getPostalCode();
        if (postalCode != null)
            unformatedLines.add(postalCode);
        String country = postalAddress.getCountry();
        if (country != null)
            unformatedLines.add(country);
        
        
        AddressLine line = null;
        
        if (streetNumber != null) {
            line =
                    postalAddressAttribute2AddressLine(postalAddressMap, streetNumber, STREETNUMBER);
            if (line != null)
                addressLines.add(line);
            line = null;
        }
        if (street != null) {
            line =
                    postalAddressAttribute2AddressLine(postalAddressMap, street, STREET);
            if (line != null)
                addressLines.add(line);
            line = null;
        }
        
        if (city != null) {
            line =
                    postalAddressAttribute2AddressLine(postalAddressMap, city, CITY);
            if (line != null)
                addressLines.add(line);
            line = null;
        }
        
        if (state != null) {
            line =
                    postalAddressAttribute2AddressLine(postalAddressMap, state, STATE);
            if (line != null)
                addressLines.add(line);
            line = null;
        }
        
        if (postalCode != null) {
            line =
                    postalAddressAttribute2AddressLine(postalAddressMap, postalCode, POSTALCODE);
            if (line != null)
                addressLines.add(line);
            line = null;
        }
        if (country != null) {
            line =
                    postalAddressAttribute2AddressLine(postalAddressMap, country, COUNTRY);
            if (line != null)
                addressLines.add(line);
            line = null;
        }
        if ((addressLines != null) && (!addressLines.isEmpty())) {
            address = null;
            address = objFactory.createAddress();
            if (defaultScheme != null){
                String defaultSchemeId = defaultScheme.getKey().getId();
                if (defaultSchemeId != null)
                    address.setTModelKey(defaultSchemeId);
            }
            
            address.getAddressLine().addAll(addressLines);
            return address;
        }
        
        return null;
        
    }
    
    AddressLine postalAddressAttribute2AddressLine(HashMap postalAddressMap, String attribute, String jaxrName)
    throws JAXRException {
        AddressLine streetLine = null;
        String keyName = null;
        String keyValue = null;
        AddressLine line = null;
        line = objFactory.createAddressLine();
        if ((attribute == null) || (attribute.equals("")))
            return null;
        
        Concept equivalentConcept = null;
        if (postalAddressMap!= null){
            equivalentConcept = (Concept)postalAddressMap.get(jaxrName);
        }
        
        if (equivalentConcept != null) {
            keyName = equivalentConcept.getName().getValue();
            keyValue = equivalentConcept.getValue();
        } else {
            keyName = jaxrName;
            keyValue = jaxrName;
        }
        
        if (attribute != null) {
            if (keyName != null)
                line.setKeyName(keyName);
            if (keyValue != null)
                line.setKeyValue(keyValue);
            line.setValue(attribute);
            return line;
        }
        return null;
    }
    
    Collection postalAddressLines2AddressLines(Slot addressLines)
    throws JAXRException {
        
        Collection aLines = null;
        //actually this may have to be ordered
        if (addressLines != null) {
            aLines = new ArrayList();
            Collection values = addressLines.getValues();
            if (values != null) {
                Iterator viter = values.iterator();
                while (viter.hasNext()) {
                    String line = (String)viter.next();
                    AddressLine aline = null;
                    aline = objFactory.createAddressLine();
                    if (aline != null) {
                        aline.setValue(line);
                        aLines.add(aline);
                    }
                }
            }
        }
        
        return aLines;
    }
    
    
    /**
     *
     * Transforms a collection of JAXR Concepts to a UDDI CategoryBag
     *
     * @param       concepts    Collection of JAXR Concepts
     * @return      CategoryBag CategoryBag Collection of UDDI Categories
     * @exception   JAXRException
     *
     */
    
    CategoryBag classifications2CategoryBag(Collection classifications) throws JAXRException {
        
        CategoryBag cbag = null;
        if ( (classifications != null) && (!classifications.isEmpty())) {
            cbag = null;
            cbag = objFactory.createCategoryBag();
            Classification classification = null;
            Iterator iter = classifications.iterator();
            try {
                
                while(iter.hasNext()) {
                    classification =
                            (Classification) iter.next();
                    
                    Concept concept = classification.getConcept();
                    
                    if (concept != null) {
                        
                        KeyedReference kref =
                                concept2KeyedReference(concept);
                        if (kref != null){
                            cbag.getKeyedReference().add(kref);
                        }
                    } else {
                        
                        ClassificationScheme cScheme =
                                classification.getClassificationScheme();
                        String value = classification.getValue();
                        Name name = getName(classification);
                        String nvalue = null;
                        if (name != null)
                            nvalue = name.getValue();
                        //if externalhas tobe one way - if internal another way
                        if ( cScheme != null) {
                            if ((value != null) && (!value.equalsIgnoreCase("")) ) {
                                
                                KeyedReference kref =
                                        classification2KeyedReference(cScheme, nvalue, value);
                                if (kref != null){
                                    cbag.getKeyedReference().add(kref);
                                }
                            } else throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Concept_specified_for_this_Classification"));
                        } else
                            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Concept_specified_for_this_Classification"));
                    }
                }
            } catch (ClassCastException cce) {
                logger.log(Level.WARNING, cce.getMessage(), cce);
                throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_Classification"), cce);
            }
        }
        return cbag;
    }
    
    /**
     *
     * Transforms a UDDI CategoryBag collection of Categories to
     * a Collection of JAXR Concepts
     *
     * @param       cbag        UDDI CategoryBag collection of Categories
     * @return      Collection  Collection of JAXR Concepts
     * @exception   JAXRException
     *
     */
    Collection categoryBag2Classifications(CategoryBag cbag) throws JAXRException {
        Collection classifications = null;
        if (cbag != null) {
            classifications = new ArrayList();
            Collection keyedReference =
                    cbag.getKeyedReference();
            Iterator iter = keyedReference.iterator();
            while(iter.hasNext()) {
                Classification classification =
                        keyedReference2Classification((KeyedReference) iter.next());
                if (classification != null)
                    classifications.add(classification);
            }
        }
        return classifications;
    }
    
    /**
     *
     * Transforms a collection of JAXR Concepts to a UDDI CategoryBag
     *
     * @param       concepts    Collection of JAXR Concepts
     * @return      CategoryBag CategoryBag Collection of UDDI Categories
     * @exception   JAXRException
     *
     */
    
    CategoryBag concepts2CategoryBag(Collection concepts) throws JAXRException {
        
        CategoryBag cbag = null;
        if ( (concepts != null) && (!concepts.isEmpty())) {
            cbag = null;
            cbag = objFactory.createCategoryBag();
            
            Iterator iter = concepts.iterator();
            try {
                while(iter.hasNext()) {
                    KeyedReference kref =
                            concept2KeyedReference( ((Concept)iter.next()));
                    if (kref != null) {
                        cbag.getKeyedReference().add(kref);
                    }
                }
            } catch (ClassCastException cce) {
                logger.log(Level.WARNING, cce.getMessage(), cce);
                throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_Concept"), cce);
            }
        }
        return cbag;
    }
    
    /**
     *
     * Transforms a UDDI CategoryBag collection of Categories to
     * a Collection of JAXR Concepts
     *
     * @param       cbag        UDDI CategoryBag collection of Categories
     * @return      Collection  Collection of JAXR Concepts
     * @exception   JAXRException
     *
     */
    Collection categoryBag2Concepts(CategoryBag cbag) throws JAXRException {
        Collection concepts = null;
        if (cbag != null) {
            concepts = new ArrayList();
            Collection keyedReference =
                    cbag.getKeyedReference();
            Iterator iter = keyedReference.iterator();
            while(iter.hasNext()) {
                Concept concept =
                        keyedReference2Concept((KeyedReference)iter.next());
                if (concept != null)
                    concepts.add(concept);
            }
        }
        return concepts;
    }
    
    
    /**
     *
     * Transforms a collection of JAXR Concepts to a UDDI TModelBag
     *
     * @param       concepts    Collection of Concepts
     * @return      TModelBag   Collection of UDDI TModel Keys
     * @exception   JAXRException
     *
     */
    TModelBag concepts2TModelBag(Collection concepts) throws JAXRException {
        
        TModelBag tbag = null;
        if ( (concepts != null) && (!concepts.isEmpty())) {
            tbag = null;
            tbag = objFactory.createTModelBag();
            Concept concept = null;
            Iterator iter = concepts.iterator();
            try {
                
                while (iter.hasNext()) {
                    concept = (Concept)iter.next();
                    //make sure that classificationScheme and parent are
                    //non-existent
                    ClassificationScheme scheme =
                            concept.getClassificationScheme();
                    Concept parent = concept.getParentConcept();
                    if ((scheme == null) && (parent == null)) {
                        Key key = concept.getKey();
                        tbag.getTModelKey().add(key.getId());
                    } else {
                        throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:ClassificationScheme_and_Parent_must_be_non-existent_for_specifications"));
                    }
                }
            } catch (ClassCastException cce) {
                logger.log(Level.WARNING, cce.getMessage(), cce);
                throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_Concept"), cce);
            }
        }
        return tbag;
    }
    
    /**
     *
     * Transforms a Collection of String to Array of Strings
     *
     * @param       namePatterns    Collection of Strings
     * @return      String[]        String Array
     * @exception   JAXRException
     *
     */
    String[] strings2Names(Collection namePatterns) throws JAXRException {
        
        String[] names = null;
        if (namePatterns != null) {
            try {
                names = (String[])namePatterns.toArray(new String[0]);
            } catch (ArrayStoreException ase) {
                throw new JAXRException(ase);
            }
        }
        return names;
    }
    
    /**
     *
     * Transforms a Collection of String to Array of Strings
     *
     * @param       namePatterns    Collection of Strings
     * @return      String[]        String Array
     * @exception   JAXRException
     *
     */
    Collection namePatterns2Names(Collection namePatterns, boolean publish)
    throws JAXRException {
        Collection names = new ArrayList();
        
        if (namePatterns != null) {
            Iterator iter = namePatterns.iterator();
            while (iter.hasNext()) {
                Object pattern = iter.next();
                if (pattern instanceof String) {
                    Name name = null;
                    Name dupName = null;
                    name = objFactory.createName();
                    dupName = objFactory.createName();
                    name.setValue((String)pattern);
                    dupName.setValue((String)pattern);
                    //locale
                    Locale defaultLocale = Locale.getDefault();
                    
                    String lang = defaultLocale.getLanguage();
                    if (!publish){
                        dupName.setLang(lang);
                        names.add(dupName);
                    }
                    
                    String country = defaultLocale.getCountry();
                    if (country != null && !country.equals(""))
                        lang += "-"+country;
                    name.setLang(lang);
                    names.add(name);
                    
                } else if (pattern instanceof InternationalString) {
                    Collection iname = internationalString2Names((InternationalString)pattern, publish);
                    if (iname != null) {
                        names.addAll(iname);
                    }
                }
            }
        }
        return names;
    }
    
    /**
     *
     * Transforms a JAXR Key to a UDDI Key String
     *
     * @param       jaxKey  JAXR Key
     * @return      String  UDDI Key which is a String
     * @exception
     *
     */
    String key2Key(Key jaxKey) throws JAXRException {
        String key = null;
        if (jaxKey != null) {
            key = jaxKey.getId();
        }
        return key;
    }
    
    /**
     *
     * Transforms a Collection of JAXR Keys to a UDDI String Array of Keys
     *
     * @param       jaxKeys     Collection of JAXR Keys
     * @return      String[]    String Array of UDDI Keys
     * @exception   JAXRException
     *
     */
    Collection keys2Keys(Collection jaxKeys) throws JAXRException {
        
        ArrayList keys = new ArrayList();
        if (jaxKeys != null) {
            Iterator iter = jaxKeys.iterator();
            try {
                while(iter.hasNext()){
                    keys.add(((Key)iter.next()).getId());
                }
            } catch (ClassCastException cce) {
                logger.log(Level.WARNING, cce.getMessage(), cce);
                throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_Keys"), cce);
            }
        }
        return keys;
    }
    
    /**
     *
     * Transform a Collection of JAXR String Qualifiers to a UDDI
     * FindQualifiers collection of FindQualifier
     *
     * @param       findQualifiers  Collection of JAXR findQualifier Strings
     * @return      FindQualifiers  Collection of UDDI FindQualifier Objects
     * @exception   JAXRException
     *
     */
    FindQualifiers strings2FindQualifiers(Collection findQualifiers)
    throws JAXRException {
        try {
            FindQualifiers fq = null;
            //FindQualifiers
            if ((findQualifiers != null) && (!findQualifiers.isEmpty())) {
                fq = null;
                fq = objFactory.createFindQualifiers();
                Iterator iter = findQualifiers.iterator();
                
                while (iter.hasNext()) {
                    fq.getFindQualifier().add((String)iter.next());
                }
            }
            return fq;
        } catch (ClassCastException cce) {
            logger.log(Level.WARNING, cce.getMessage(), cce);
            throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_String"), cce);
        }
    }
    
    
    /**
     *
     * Transforms Collection of JAXR ExternalLinks To UDDI DiscoveryURLs
     *
     * @param       links           Collection of JAXR ExternalLinks
     * @return      DiscoveryURLs   UDDI DiscoveryURLs
     * @exception   JAXRException
     *
     */
    //need to revisit - currently not tested
    DiscoveryURLs externalLinks2DiscoveryURLs(Collection links)
    throws JAXRException {
        
        DiscoveryURLs URLs = null;
        if ((links != null) && (!links.isEmpty())) {
            URLs = null;
            URLs = objFactory.createDiscoveryURLs();
            ExternalLink link = null;
            Iterator linksiter = links.iterator();
            try {
                
                while (linksiter.hasNext()) {
                    DiscoveryURL dURL = null;
                    dURL = objFactory.createDiscoveryURL();
                    link = (ExternalLink) linksiter.next();
                    String uri = link.getExternalURI();
                    Name name = getName(link);
                    
                    if (uri != null) {
                        dURL.setValue(uri);
                    }
                    if ((name == null) || (name.equals("")) ) {
                        name.setValue("Unknown Name");
                        //Locale
                        //todo: should this be default local lang?
                        name.setLang("");
                    }
                    dURL.setUseType(name.getValue());
                    //todo:???must do lang here
                    
                    URLs.getDiscoveryURL().add(dURL);
                }
            } catch (ClassCastException cce) {
                logger.log(Level.WARNING, cce.getMessage(), cce);
                throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_ExternalLink"), cce);
            }
        }
        return URLs;
    }
    
    
    
    
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    Collection concepts2TModels(Collection concepts, boolean publish) throws JAXRException {
        
        Collection tmodels = null;
        if ((concepts != null) && (!concepts.isEmpty())) {
            tmodels = new ArrayList();
            
            Iterator iter = concepts.iterator();
            try {
                while (iter.hasNext()) {
                    TModel tModel = concept2TModel((Concept)iter.next(), publish);
                    if (tModel != null)
                        tmodels.add(tModel);
                }
            } catch (ClassCastException cce) {
                logger.log(Level.WARNING, cce.getMessage(), cce);
                throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_Concept"), cce);
            }
        }
        
        return tmodels;
    }
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    Collection classificationSchemes2TModels(Collection schemes, boolean publish)
    throws JAXRException {
        
        Collection tmodels = null;
        if ((schemes != null) && (!schemes.isEmpty())) {
            tmodels = new ArrayList();
            Iterator iter = schemes.iterator();
            try {
                while (iter.hasNext()) {
                    
                    TModel tModel =
                            classificationScheme2TModel((ClassificationScheme) iter.next(), publish);
                    
                    if (tModel != null)
                        tmodels.add(tModel);
                }
            } catch (ClassCastException cce) {
                logger.log(Level.WARNING, cce.getMessage(), cce);
                throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_ClassificationScheme"), cce);
            }
        }
        return tmodels;
    }
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    TModel concept2TModel(Concept concept, boolean publish) throws JAXRException {
        
        TModel model = null;
        model = objFactory.createTModel();
        Concept aConcept = (ConceptImpl)concept;
        
        Key key = aConcept.getKey();
        if (key != null)
            model.setTModelKey(key.getId());
        else model.setTModelKey("");
        
        //Locale
        Name name = getName(concept);
        model.setName(name);
        //locale
        Collection description = getDescriptions(aConcept, publish);
        model.getDescription().addAll(description);
        
        Collection slots = aConcept.getSlots();
        //Find Slot Named authorized Name
        if (slots != null) {
            try {
                Iterator iter = slots.iterator();
                while (iter.hasNext()) {
                    Slot slot = (Slot) iter.next();
                    if (slot.getName().equals("authorizedName")) {
                        //get slot value - take 1st
                        Collection values = slot.getValues();
                        if ((values!=null) && (!values.isEmpty())) {
                            Object[] vals = values.toArray();
                            String authName = (String)vals[0];
                            if (authName != null)
                                model.setAuthorizedName(authName);
                        }
                    }
                    if (slot.getName().equals("operator")) {
                        //get slot value - take 1st
                        Collection values = slot.getValues();
                        if ((values!=null) && (!values.isEmpty())) {
                            Object[] vals = values.toArray();
                            String operator = (String)vals[0];
                            if (operator != null)
                                model.setOperator(operator);
                        }
                    }
                }
            } catch (ClassCastException cce) {
                logger.log(Level.WARNING, cce.getMessage(), cce);
                throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_Slot"), cce);
            }
        }
        
        Collection identifiers = aConcept.getExternalIdentifiers();
        if (identifiers != null) {
            IdentifierBag identifierBag =
                    externalIdentifiers2IdentifierBag(identifiers);
            if (identifierBag != null)
                model.setIdentifierBag(identifierBag);
        }
        //to categoryBag
        Collection concepts =
                aConcept.getClassifications();
        CategoryBag categoryBag = null;
        if (concepts != null) {
            categoryBag = classifications2CategoryBag(concepts);
        }
        categoryBag = addCategorization(categoryBag, LifeCycleManager.CONCEPT);
        if (categoryBag != null)
            model.setCategoryBag(categoryBag);
        
        //why only one Overview Doc
        Collection links= concept.getExternalLinks();
        if ((links != null) && (!links.isEmpty())){
            //if more than 1 external Link Exception?
            OverviewDoc overviewDoc =
                    externalLink2OverviewDoc((ExternalLink)links.toArray()[0], publish);
            model.setOverviewDoc(overviewDoc);
        }
        return model;
    }
    
    TModel classificationScheme2TModel(ClassificationScheme scheme, boolean publish) throws JAXRException {
        
        TModel model = null;
        
        model = objFactory.createTModel();
        Key key = scheme.getKey();
        if (key != null)
            model.setTModelKey(key.getId());
        else model.setTModelKey("");
        
        //Locale
        Name name = getName(scheme);
        model.setName(name);
        //locale
        Collection description = getDescriptions(scheme, publish);
        model.getDescription().addAll(description);
        
        Collection slots = scheme.getSlots();
        //Find Slot Named authorized Name
        if (slots != null) {
            try {
                Iterator iter = slots.iterator();
                while (iter.hasNext()) {
                    Slot slot = (Slot) iter.next();
                    if (slot.getName().equals("authorizedName")) {
                        //get slot value - take 1st
                        Collection values = slot.getValues();
                        if ((values!=null) && (!values.isEmpty())) {
                            Object[] vals = values.toArray();
                            String authName = (String)vals[0];
                            if (authName != null)
                                model.setAuthorizedName(authName);
                        }
                    }
                    if (slot.getName().equals("operator")) {
                        //get slot value - take 1st
                        Collection values = slot.getValues();
                        if ((values!=null) && (!values.isEmpty())) {
                            Object[] vals = values.toArray();
                            String operator = (String)vals[0];
                            if (operator != null)
                                model.setOperator(operator);
                        }
                    }
                }
            } catch (ClassCastException cce) {
                logger.log(Level.WARNING, cce.getMessage(), cce);
                throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_Slot"), cce);
            }
        }
        
        Collection identifiers = scheme.getExternalIdentifiers();
        if (identifiers != null) {
            IdentifierBag identifierBag =
                    externalIdentifiers2IdentifierBag(identifiers);
            if (identifierBag != null)
                model.setIdentifierBag(identifierBag);
        }
        //to categoryBag
        Collection concepts =
                scheme.getClassifications();
        CategoryBag categoryBag = null;
        if (concepts != null) {
            categoryBag = classifications2CategoryBag(concepts);
        }
        categoryBag = addCategorization(categoryBag, LifeCycleManager.CLASSIFICATION_SCHEME);
        if (categoryBag != null)
            model.setCategoryBag(categoryBag);
        
        
        //why only one Overview Doc
        Collection links= scheme.getExternalLinks();
        if ((links != null) && (!links.isEmpty())){
            //if more than 1 external Link Exception?
            OverviewDoc overviewDoc =
                    externalLink2OverviewDoc((ExternalLink)links.toArray()[0], publish);
            model.setOverviewDoc(overviewDoc);
        }
        return model;
    }
    
    CategoryBag addCategorization(CategoryBag categoryBag,
            String objectType) throws JAXRException {
        
        boolean present = false;
        Collection keyedReferences = null;
        if (categoryBag == null){
            categoryBag = objFactory.createCategoryBag();
            
        } else {
            //check to see if uddi-org:types set already
            keyedReferences =
                    categoryBag.getKeyedReference();
            if (keyedReferences != null) {
                Iterator iter = keyedReferences.iterator();
                while (iter.hasNext()){
                    KeyedReference ref = (KeyedReference)iter.next();
                    if (ref.getTModelKey().equalsIgnoreCase(UDDI_ORG_TYPES_KEY)){
                        present = true;
                        break;
                    }
                }
            }
        }
        
        if (!present) {
            KeyedReference keyedRef = null;
            keyedRef = objFactory.createKeyedReference();
            if (objectType.equalsIgnoreCase(LifeCycleManager.CLASSIFICATION_SCHEME)) {
                keyedRef.setTModelKey(UDDI_ORG_TYPES_KEY);
                keyedRef.setKeyName(UDDI_ORG_TYPES_NAME);
                keyedRef.setKeyValue(UDDI_CATEGORIZATION);
            } else if (objectType.equalsIgnoreCase(LifeCycleManager.CONCEPT)) {
                keyedRef.setTModelKey(UDDI_ORG_TYPES_KEY);
                keyedRef.setKeyName(UDDI_ORG_TYPES_NAME);
                keyedRef.setKeyValue(UDDI_SPECIFICATION);
            } else keyedRef = null;
            //now add to categoryBag
            if (keyedRef != null){
                categoryBag.getKeyedReference().add(keyedRef);
            }
        }
        return categoryBag;
    }
    
    //start here
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    Concept tModel2Concept(TModelInfo info) throws JAXRException {
        
        ConceptImpl concept = null;
        String tkey = info.getTModelKey();
        Name name = info.getName();
        
        if (this.getConnection().useCache()) {
            try {
                concept = (ConceptImpl)
                objectManager.fetchObjectFromCache(tkey);
            } catch (ClassCastException cce) {
                logger.finest("ClassCastException in tModelInfo2Concept on fetch, continuing");
            }
        }
        if (concept == null) {
            concept = new ConceptImpl();
            concept.setServiceId(this.service.getServiceId());
            concept.setRegistryService(this.service);
        }
        
        InternationalString iname = name2InternationalString(name);
        concept.setName(iname);
        concept.setKey(new KeyImpl(tkey));
        
        concept.setStatusFlags(true, false, false);
        //always add skeleton object for incremental loading
        objectManager.addObjectToCache(concept, service.getServiceId());
        return concept;
    }
    
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    
    Concept tModel2Concept(TModel model) throws JAXRException {
        
        ConceptImpl concept = null;
        String tModelKey = model.getTModelKey();
        //need to check if object retrieved is classificationScheme or concept
        //if classificationScheme we know what to do
        //always look for the skeleton
        if (getConnection().useCache()) {
            try {
                concept = (ConceptImpl)
                objectManager.fetchObjectFromCache(tModelKey);
            } catch (ClassCastException cce) {
                logger.finest("ClassCastException in tModel2Concept, continuing");
                concept = null;
            }
        }
        if (concept == null) {
            concept =
                    new ConceptImpl(new KeyImpl(tModelKey));
            concept.setServiceId(this.service.getServiceId());
            concept.setRegistryService(this.service);
        }
        
        
        concept.setIsLoaded(true);
        concept.setIsRetrieved(true);
        concept.setIsNew(false);
        //get the TModel Detail
        Name name = model.getName();
        String authorizedName = model.getAuthorizedName();
        
        SlotImpl authSlot =
                new SlotImpl("authorizedName", "authorizedName", authorizedName);
        concept.addSlot(authSlot);
        
        String operator = model.getOperator();
        SlotImpl operSlot =
                new SlotImpl("operator", "operator", operator);
        concept.addSlot(operSlot);
        
        
        ExternalLink link = null;
        OverviewDoc odoc =model.getOverviewDoc();
        if (odoc != null) {
            link = overviewDoc2ExternalLink(odoc);
            if (link != null){
                Collection links = new ArrayList();
                links.add(link);
                concept.setExternalLinks(links);
            }
        }
        
        Collection externalIds = null;
        IdentifierBag ibag = model.getIdentifierBag();
        if (ibag != null) {
            externalIds =
                    identifierBag2ExternalIdentifiers(ibag);
            if ((externalIds != null) && (!externalIds.isEmpty()))
                //need to set registry entry for each externalId
                concept.setExternalIdentifiers(externalIds);
        }
        
        Collection classifications = null;
        CategoryBag cbag = model.getCategoryBag();
        if (cbag != null) {
            classifications=
                    categoryBag2Classifications(cbag);
            if ((classifications != null) &&
                    (!classifications.isEmpty()))
                //need to set Registry entry for each classification Concept
                //each concept needs to have its root concept set -
                concept.setClassifications(classifications);
        }
        
        //now build the concept
        KeyImpl key = new KeyImpl(tModelKey);
        InternationalString iname = name2InternationalString(name);
        concept.setName(iname);
        
        Collection description = model.getDescription();
        InternationalString idesc =
                descriptions2InternationalString(description);
        if (idesc != null) {
            concept.setDescription(idesc);
        }
        
        //if using cache store it, otherwise clear it
        if (getConnection().useCache())
            objectManager.addObjectToCache(concept, service.getServiceId());
        else
            objectManager.removeObjectFromCache(concept.getKey().getId());
        
        return concept;
    }
    
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    
    RegistryObject tModel2ConceptOrClassificationScheme(TModel model) throws JAXRException {
        
        RegistryObjectImpl ro = null;
        
        String tModelKey = model.getTModelKey();
        ro = (RegistryObjectImpl)
        objectManager.fetchObjectFromCache(tModelKey);
        if (ro != null) {
            ro.setRegistryService(this.service);
            ro.setIsLoaded(true);
        }
        //need to check if object retrieved is classificationScheme or concept
        //if classificationScheme we know what to do
        
        Collection classifications = null;
        CategoryBag cbag = model.getCategoryBag();
        boolean scheme = false;
        if (cbag != null) {
            classifications=
                    categoryBag2Classifications(cbag);
            if ((classifications != null) &&
                    (!classifications.isEmpty())) {
                try {
                    //check the classification
                    //look for catorigazation
                    Iterator iter = classifications.iterator();
                    while (iter.hasNext()) {
                        scheme = isClassificationScheme((Classification)iter.next());
                        if (scheme)
                            break;
                    }
                } catch (ClassCastException cce) {
                    logger.log(Level.SEVERE, cce.getMessage(), cce);
                }
            }
        }
        
        RegistryObjectImpl registryObject = null;
        //got it from the cache
        if (ro != null) {
            if (scheme) {
                if (ro instanceof Concept)
                    registryObject =
                            new ClassificationSchemeImpl((ConceptImpl)ro);
            } else
                registryObject = ro;
        }
        
        if (registryObject == null) {
            if (scheme)
                registryObject =
                        new com.sun.xml.registry.uddi.infomodel.ClassificationSchemeImpl(new KeyImpl(tModelKey));
            else
                registryObject =
                        new com.sun.xml.registry.uddi.infomodel.ConceptImpl(new KeyImpl(tModelKey));
            registryObject.setServiceId(this.service.getServiceId());
            registryObject.setRegistryService(this.service);
            
        }
        registryObject.setIsRetrieved(true);
        registryObject.setIsNew(false);
        registryObject.setIsLoaded(true);
        
        String authorizedName = model.getAuthorizedName();
        
        SlotImpl authSlot =
                new SlotImpl("authorizedName", authorizedName, "authorizedName");
        registryObject.addSlot(authSlot);
        
        String operator = model.getOperator();
        SlotImpl operSlot =
                new SlotImpl("operator", "operator", operator);
        registryObject.addSlot(operSlot);
        
        if (classifications != null)
            registryObject.setClassifications(classifications);
        
        ExternalLink link = null;
        OverviewDoc odoc = model.getOverviewDoc();
        if (odoc != null) {
            link = overviewDoc2ExternalLink(odoc);
            if (link != null){
                Collection links = new ArrayList();
                links.add(link);
                //todo: why does it matter if concept?
                //if (registryObject instanceof Concept)
                registryObject.setExternalLinks(links);
                //else
                //    registryObject.setExternalLinks(links);
            }
        }
        
        Collection externalIds = null;
        IdentifierBag ibag = model.getIdentifierBag();
        if (ibag != null) {
            externalIds =
                    identifierBag2ExternalIdentifiers(ibag);
            if ((externalIds != null) && (!externalIds.isEmpty()))
                //need to set registry entry for each externalId
                registryObject.setExternalIdentifiers(externalIds);
        }
        
        Name name = model.getName();
        InternationalString iname = name2InternationalString(name);
        registryObject.setName(iname);
        
        Collection description = model.getDescription();
        InternationalString idesc =
                descriptions2InternationalString(description);
        registryObject.setDescription(idesc);
        //need to do lang
        
        objectManager.addObjectToCache(registryObject, service.getServiceId());
        return registryObject;
    }
    
    boolean isClassificationScheme(Classification classification)
    throws JAXRException {
        //todo: use constants
        boolean isScheme = false;
        String value = classification.getValue();
        if (value == null)
            return isScheme;
        if (value.equalsIgnoreCase(UDDI_IDENTIFIER))
            isScheme = true;
        else if (value.equalsIgnoreCase(UDDI_NAMESPACE))
            isScheme = true;
        else if (value.equalsIgnoreCase(UDDI_CATEGORIZATION))
            isScheme = true;
        else if (value.equalsIgnoreCase(UDDI_POSTALADDRESS))
            isScheme = true;
        return isScheme;
    }
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    
    KeyedReference externalIdentifier2KeyedReference(ExternalIdentifier identifier)
    throws JAXRException {
        
        KeyedReference keyedRef = null;
        keyedRef = objFactory.createKeyedReference();
        ClassificationScheme identScheme = identifier.getIdentificationScheme();
        //get Key for identificationScheme
        if (identScheme == null)
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:IdentificationScheme_missing_-_this_must_be_supplied"));
        Key ikey = identScheme.getKey();
        String id = null;
        if (ikey != null)
            id = ikey.getId();
        
        if (ikey != null)
            keyedRef.setTModelKey(id);
        else
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_IdentificationScheme_Key_-_this_must_be_supplied"));
        
        String name = null;
        //todo:why catch npe
        try {
            name = identifier.getName().getValue();
        } catch (NullPointerException npe) {}
        
        if (name != null)
            keyedRef.setKeyName(name);
        else
            keyedRef.setKeyName("");
        
        String value = identifier.getValue();
        if (value != null)
            keyedRef.setKeyValue(value);
        else
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_ExternialIdentifier_Value_supplied_-_this_must_be_supplied"));
        
        return keyedRef;
    }
    
    
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    ExternalIdentifier keyedReference2ExternalIdentifier(KeyedReference keyedRef)
    throws JAXRException {
        
        ExternalIdentifierImpl identifier = null;
        if (keyedRef != null) {
            String tModelKey = keyedRef.getTModelKey();
            String keyName = keyedRef.getKeyName();
            String value = keyedRef.getKeyValue();
            
            ClassificationScheme scheme = new ClassificationSchemeImpl();
            scheme.setKey(new KeyImpl(tModelKey));
            scheme.setName(new InternationalStringImpl(keyName));
            
            identifier =
                    new ExternalIdentifierImpl(scheme, keyName, value);
            identifier.setRegistryService(this.service);
        }
        return identifier;
    }
    
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    
    
    KeyedReference concept2KeyedReference(Concept concept) throws JAXRException {
        
        ConceptImpl aConcept = (ConceptImpl)concept;
        KeyedReference keyedRef = null;
        keyedRef = objFactory.createKeyedReference();
        if (concept != null) {
            ClassificationScheme parent =
                    concept.getClassificationScheme();
            if (parent != null) {
                Key key = parent.getKey();
                if (key == null)
                    throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Root_ClassificationScheme_key_supplied"));
                String pvalue = key.getId();
                String name = null;
                if (aConcept.getName() != null) {
                    name = aConcept.getName().getValue();
                }
                String value = aConcept.getValue();
                if (name != null)
                    keyedRef.setKeyName(name);
                else keyedRef.setKeyName("Unknown");
                if (value != null)
                    keyedRef.setKeyValue(value);
                else return null;
                if (pvalue != null)
                    keyedRef.setTModelKey(pvalue);
                else return null;
            } else throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Root_Concept_for_this_concept__") +
                    concept.getName());
        }
        return keyedRef;
    }
    
    KeyedReference classification2KeyedReference(ClassificationScheme scheme,
            String vname, String value)
            throws JAXRException {
        
        
        
        
        KeyedReference keyedRef = null;
        if (scheme != null) {
            keyedRef = null;
            
            keyedRef = objFactory.createKeyedReference();
            Key key = scheme.getKey();
            
            
            String pvalue = null;
            if ( key != null)
                pvalue = key.getId();
            
            if (vname != null)
                keyedRef.setKeyName(vname);
            else keyedRef.setKeyName("");
            if (value != null)
                keyedRef.setKeyValue(value);
            else throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Classification_must_have_a_value_"));
            if (pvalue != null)
                keyedRef.setTModelKey(pvalue);
            else throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Parent_ClassificationScheme_key_must_have_a_value_"));
        }
        return keyedRef;
    }
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    Concept keyedReference2Concept(KeyedReference keyedRef)
    throws JAXRException {
        
        ConceptImpl concept = null;
        String tModelKey = keyedRef.getTModelKey();
        String keyName = keyedRef.getKeyName();
        String value = keyedRef.getKeyValue();
        //check to see if object in cache
        concept =
                new ConceptImpl(new KeyImpl(tModelKey), keyName, value);
        concept.setName(new InternationalStringImpl(keyName));
        concept.setIsRetrieved(true);
        concept.setIsLoaded(false);
        concept.setIsNew(false);
        objectManager.addObjectToCache(concept, this.service.getServiceId());
        return concept;
    }
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    
    
    Classification keyedReference2Classification(KeyedReference keyedRef)
    throws JAXRException {
        
        String tModelKey = keyedRef.getTModelKey();
        String keyName = keyedRef.getKeyName();
        String value = keyedRef.getKeyValue();
        
        //always get ClassificationScheme from cache or from JAXRConcepts
        ClassificationSchemeImpl scheme = (ClassificationSchemeImpl)
        getConceptsManager().getClassificationSchemeById(tModelKey);
        if (scheme == null) {
            try {
                scheme = (ClassificationSchemeImpl)
                objectManager.fetchObjectFromCache(tModelKey);
                
            } catch (ClassCastException cce) {
                logger.finest("ClassCastException in keyedRef2Classification feting classificationScheme");
                scheme = null;
            }
        }
        
        if (scheme == null) {
            scheme = new ClassificationSchemeImpl();
            scheme.setKey(new KeyImpl(tModelKey));
            scheme.setIsLoaded(false);
            scheme.setIsNew(false);
            scheme.setIsRetrieved(true);
            objectManager.addObjectToCache(scheme, this.service.getServiceId());
        }
        
        ClassificationImpl classification = new ClassificationImpl();
        classification.setClassificationScheme(scheme);
        classification.setName(new InternationalStringImpl(keyName));
        classification.setValue(value);
        classification.setRegistryService(service);
        
        return classification;
    }
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    
    KeyedReference key2KeyedReference(Key key) throws JAXRException {
        
        KeyedReference keyedRef = null;
        keyedRef = objFactory.createKeyedReference();
        if (key != null) {
            String pvalue = key.getId();
            keyedRef.setTModelKey(pvalue);
        }
        return keyedRef;
    }
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    
    OverviewDoc externalLink2OverviewDoc(ExternalLink link, boolean publish) throws JAXRException {
        
        ExternalLinkImpl linx = (ExternalLinkImpl)link;
        OverviewDoc doc = null;
        doc = objFactory.createOverviewDoc();
        if (link != null) {
            String url = linx.getExternalURI();
            doc.setOverviewURL(url);
            Collection description = getDescriptions(linx, publish);
            doc.getDescription().addAll(description);
        }
        return doc;
    }
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    ExternalLink overviewDoc2ExternalLink(OverviewDoc doc) throws JAXRException {
        
        ExternalLink link = null;
        if (doc != null){
            String urlString = doc.getOverviewURL();
            Collection description = doc.getDescription();
            InternationalString idesc =
                    descriptions2InternationalString(description);
            link = new ExternalLinkImpl();
            link.setValidateURI(false);
            link.setExternalURI(urlString);
            if (idesc != null)
                link.setDescription(idesc);
        }
        return link;
    }
    
    
    
    Collection tModelInstanceInfos2SpecificationLinks(
            Collection instanceInfo )
            throws JAXRException {
        
        Collection specificationLinks = new ArrayList();
        Iterator iter = instanceInfo.iterator();
        
        if (instanceInfo != null) {
            while (iter.hasNext()){
                SpecificationLinkImpl specLink =
                        new SpecificationLinkImpl();
                specLink.setRegistryService(this.service);
                TModelInstanceInfo info =
                        (TModelInstanceInfo)iter.next();
                
                String tModelKey = info.getTModelKey(); //Concept
                Collection descript = info.getDescription();
                InstanceDetails instanceDetails =
                        info.getInstanceDetails();
                
                ConceptImpl classConcept = null;
                //look up in cache first
                if (tModelKey != null) {
                    try {
                        classConcept = (ConceptImpl)objectManager.fetchObjectFromCache(tModelKey);
                    } catch (ClassCastException cce) {
                        logger.finest("ClassCastException in tModelInstanceInfos2SpecificationLinks fetch concept, continuing");
                        classConcept = null;
                    }
                    
                    if (classConcept==null) {
                        classConcept =
                                new ConceptImpl();
                        classConcept.setRegistryService(this.service);
                        classConcept.setKey(new KeyImpl(tModelKey));
                        classConcept.setStatusFlags(true, false, false);
                        classConcept.setRegistryService(this.service);
                        objectManager.addObjectToCache(classConcept, this.service.getServiceId());
                    }
                    
                    if (classConcept != null) {
                        specLink.setSpecificationObject(classConcept);
                        logger.finest("setting specification concept");
                    }
                }
                
                if (instanceDetails != null) {
                    logger.finest("have instance Details");
                    OverviewDoc oDoc = instanceDetails.getOverviewDoc();
                    String params = instanceDetails.getInstanceParms();
                    Collection instanceDescription = instanceDetails.getDescription();
                    InternationalString idesc =
                            descriptions2InternationalString(instanceDescription);
                    if (idesc != null)
                        specLink.setUsageDescription(idesc);
                    
                    if (params != null) {
                        Collection up = new ArrayList();
                        up.add(params);
                        specLink.setUsageParameters(up);
                    }
                    
                    //maps to ExternalLink
                    ExternalLinkImpl exLink = null;
                    if (oDoc != null) {
                        logger.finest("oDoc is found");
                        exLink = (ExternalLinkImpl)overviewDoc2ExternalLink(oDoc);
                        
                        if (exLink == null)
                            logger.warning(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Exlink_is_null"));
                    }
                    
                    if (exLink != null) {
                        exLink.setRegistryService(this.service);
                        specLink.addExternalLink(exLink);
                    }
                }
                if (specLink != null) {
                    logger.finest("Adding specificationLink");
                    specificationLinks.add(specLink);
                }
            }
            return specificationLinks;
        }
        return null;
    }
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    
    Organization businessInfo2Organization(BusinessInfo info)throws JAXRException {
        
        OrganizationImpl org = null;
        
        String businessKey = info.getBusinessKey();
        
        //if using cache fetch
        if (getConnection().useCache()) {
            try {
                org = (OrganizationImpl)
                objectManager.fetchObjectFromCache(businessKey);
            } catch (ClassCastException cce){
                logger.finest("CLassCastException businessInfo2Org on fetch from cache, continuing");
                org = null;
            }
        }
        
        if (org == null) {
            org = new OrganizationImpl();
            org.setServiceId(this.service.getServiceId());
            
        }
        org.setRegistryService(this.service);
        org.setIsRetrieved(false);
        
        Collection name = info.getName();
        InternationalString iname = names2InternationalString(name);
        if (iname != null)
            org.setName(iname);
        
        if (businessKey != null)
            org.setKey(new KeyImpl(businessKey));
        
        Collection description = info.getDescription();
        InternationalString idesc = descriptions2InternationalString(description);
        if (idesc != null)
            org.setDescription(idesc);
        
        ServiceInfos serviceInfos = info.getServiceInfos();
        if (serviceInfos != null) {
            Collection serviceCollection = new ArrayList();
            Collection sInfo = serviceInfos.getServiceInfo();
            Iterator iter = sInfo.iterator();
            while (iter.hasNext()){
                ServiceImpl service =(ServiceImpl) serviceInfo2Service((ServiceInfo)iter.next());
                if (service != null) {
                    service.setServiceId(this.service.getServiceId());
                    service.setRegistryService(this.service);
                    serviceCollection.add(service);
                }
            }
            if (serviceCollection != null)
                org.setServices(serviceCollection);
        }
        
        //always add skeleton object to for incremental loading
        org.setIsRetrieved(true);
        org.setIsNew(false);
        
        //make sure org will be fetched from registry
        if (!getConnection().useCache())
            org.setIsLoaded(false);
        
        objectManager.addObjectToCache(org, this.service.getServiceId());
        return org;
    }
    
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    
    Collection relatedBusinessInfo2Associations(RelatedBusinessInfo info , Collection keys)
    throws JAXRException {
        
        OrganizationImpl relatedOrg = null;
        OrganizationImpl originalOrg = null;
        
        if (keys == null)
            logger.finest("Keys are null");
        //getOrgiginalOrg
        if ((keys != null) && (!keys.isEmpty())){
            Iterator keysIter = keys.iterator();
            KeyImpl key = (KeyImpl)keysIter.next();
            String originalOrgKeyId = key.getId();
            originalOrg = (OrganizationImpl)
            objectManager.fetchObjectFromCache(originalOrgKeyId);
            if (originalOrg == null) {
                originalOrg = (OrganizationImpl)
                getRegistryObject(originalOrgKeyId, LifeCycleManager.ORGANIZATION);
            }
        }
        
        String businessKey = info.getBusinessKey();
        logger.finest("Related Business Key " + businessKey);
        relatedOrg = (OrganizationImpl)
        objectManager.fetchObjectFromCache(businessKey);
        
        if (relatedOrg == null) {
            relatedOrg = new OrganizationImpl();
            relatedOrg.setServiceId(this.service.getServiceId());
            relatedOrg.setRegistryService(this.service);
        }
        relatedOrg.setIsRetrieved(true);
        relatedOrg.setIsNew(false);
        //kw reordering
        if (businessKey != null)
            relatedOrg.setKey(new KeyImpl(businessKey));
        
        Collection name = info.getName();
        InternationalString iname = names2InternationalString(name);
        if (iname != null)
            relatedOrg.setName(iname);
        
        if (businessKey != null)
            relatedOrg.setKey(new KeyImpl(businessKey));
        
        Collection description = info.getDescription();
        InternationalString idesc = descriptions2InternationalString(description);
        if (idesc != null)
            relatedOrg.setDescription(idesc);
        
        //shared relationships for the relatedOrg
        Collection sharedRelationships =
                info.getSharedRelationships();
        Collection associations =
                sharedRelationships2Associations(relatedOrg, originalOrg, sharedRelationships);
        //need to add the association to eachOrg
        //not sure I need to do this
        //originalOrg.addAssociations(associations);
        //only returns confirmedAssociations
        //TBD need to go through orgs associations
        //and make sure associations are as new as possible
        //relatedOrg.addAssociations(associations);
        //originalOrg.addAssociations(associations);
        if (associations != null) {
            logger.finest("RelatedBusinessInfo2Associations : associations = " + associations.size());
            //need to add the associations somewhere
        }
        objectManager.addObjectToCache(relatedOrg, this.service.getServiceId());
        return associations;
    }
    
    
    Collection sharedRelationships2Associations(Organization relatedOrg,
            Organization originalOrg, Collection sharedRelationships)
            throws JAXRException {
        
        Collection associations = new ArrayList();
        Iterator iter = sharedRelationships.iterator();
        while (iter.hasNext()){
            
            SharedRelationships sharedRelationship =
                    (SharedRelationships)iter.next();
            Direction direction =
                    sharedRelationship.getDirection();
            String  directionType = direction.value();
            Organization sourceObject = null;
            Organization targetObject = null;
            String sourceKeyId = null;
            String targetKeyId = null;
            
            
            
            
            if (directionType.equalsIgnoreCase("fromKey")) {
                
                sourceObject = relatedOrg;
                sourceKeyId = relatedOrg.getKey().getId();
                targetObject = originalOrg;
                targetKeyId = originalOrg.getKey().getId();
            } else if (directionType.equalsIgnoreCase("toKey")) {
                
                sourceObject = originalOrg;
                sourceKeyId = originalOrg.getKey().getId();
                targetObject = relatedOrg;
                targetKeyId = relatedOrg.getKey().getId();
            }
            
            //from this we should know wheter this is the source or target object
            Collection keyedReferences =
                    sharedRelationship.getKeyedReference();
            Collection associationTypes =
                    keyedReferences2AssociationTypes(keyedReferences);
            //for each of these associationTypes we want
            //to create an association
            Iterator typeIter = associationTypes.iterator();
            
            while (typeIter.hasNext()) {
                
                Concept type = (Concept)typeIter.next();
                AssociationImpl association = new AssociationImpl();
                association.setAssociationType(type);
                
                association.setSourceObject(sourceObject);
                association.setTargetObject(targetObject);
                association.setRegistryService(this.service);
                if (!sourceKeyId.equals(targetKeyId))
                    association.setIsExtramural(true);
                KeyImpl associationKey =
                        buildAssociationKey(sourceKeyId, targetKeyId, type.getValue());
                association.setKey(associationKey);
                //association has to be confirmed by both source and target
                //to be seen with this call
                association.setIsConfirmedBySourceOwner(true);
                association.setIsConfirmedByTargetOwner(true);
                associations.add(association);
                
            }
        }
        
        return associations;
    }
    
    Collection keyedReferences2AssociationTypes(Collection keyedReferences)
    throws JAXRException {
        
        Collection associationTypes = null;
        if (keyedReferences != null) {
            Iterator iter = keyedReferences.iterator();
            associationTypes = new ArrayList();
            while (iter.hasNext()){
                KeyedReference keyedRef = (KeyedReference)iter.next();
                String key = keyedRef.getTModelKey();
                String name = keyedRef.getKeyName();
                String value = keyedRef.getKeyValue();
                String newName = null;
                String newValue = null;
                //do mapping for name/value
                if (value.equals(PEER_TO_PEER)){
                    newName = RELATES_TO;
                    newValue = RELATES_TO;
                } else if (value.equals(IDENTITY)){
                    newName = EQUIVALENT_TO;
                    newValue = EQUIVALENT_TO;
                } else if (value.equals(PARENT_TO_CHILD)) {
                    newName = HAS_CHILD;
                    newValue = HAS_CHILD;
                } else {
                    newName = name;
                    newValue = value;
                }
                //need to get predefined concept ref
                ConceptImpl concept = new ConceptImpl();
                if (name != null){
                    concept.setName(new InternationalStringImpl(newName));
                }
                if (value != null){
                    concept.setValue(newValue);
                }
                if (key != null) {
                    concept.setKey(new KeyImpl(key));
                }
                associationTypes.add(concept);
            }
        }
        return associationTypes;
    }
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    
    Organization businessEntity2Organization(BusinessEntity entity)
    throws JAXRException {
        
        if (entity == null)
            return null;
        
        
        //retrieve it from the cache
        OrganizationImpl org = null;
        String businessKey = entity.getBusinessKey();
        
        //always retrieve skeleton object
        try {
            org = (OrganizationImpl)
            objectManager.fetchObjectFromCache(businessKey);
        } catch (ClassCastException cce) {
            logger.finest("ClassCastException fetching org in businessEntity2Org, continuing");
        }
        
        if (org == null) {
            org = new OrganizationImpl();
            org.setServiceId(this.service.getServiceId());
            org.setRegistryService(this.service);
            
        }
        org.setIsRetrieved(true);
        org.setIsNew(false);
        org.setIsLoaded(true);
        
        //slots on extensible Object
        String operator = entity.getOperator();
        SlotImpl operatorSlot = null;
        if (operator != null){
            operatorSlot = new SlotImpl(OPERATOR, operator, null);
            if (operatorSlot != null)
                org.addSlot(operatorSlot);
        }
        
        
        String authorizedName = entity.getAuthorizedName();
        SlotImpl authorizedNameSlot = null;
        if (authorizedName != null){
            authorizedNameSlot = new SlotImpl(AUTHORIZED_NAME, authorizedName, null);
            if (authorizedNameSlot != null)
                org.addSlot(authorizedNameSlot);
        }
        
        Collection businessName = entity.getName();
        Collection description = entity.getDescription();
        
        Contacts contacts = entity.getContacts();
        BusinessServices businessServices = entity.getBusinessServices();
        
        Collection identifiers = null;
        IdentifierBag ibag = entity.getIdentifierBag();
        if (ibag != null) {
            identifiers = identifierBag2ExternalIdentifiers(ibag);
            if (identifiers != null)
                org.setExternalIdentifiers(identifiers);
        }
        
        Collection classifications = null;
        CategoryBag cbag = entity.getCategoryBag();
        if (cbag != null) {
            classifications = categoryBag2Classifications(cbag);
            if (classifications != null) {
                org.setClassifications(classifications);
            }
        }
        Collection externalLinks = null;
        DiscoveryURLs urls = entity.getDiscoveryURLs();
        if (urls != null) {
            externalLinks = discoveryURLs2ExternalLinks(urls);
            if (externalLinks != null) {
                org.setExternalLinks(externalLinks);
            }
        }
        
        if (businessKey != null)
            org.setKey(new KeyImpl(businessKey));
        if (businessName != null) {
            InternationalString iname = names2InternationalString(businessName);
            if (iname != null)
                org.setName(iname);
        }
        if (description != null) {
            InternationalString idesc = descriptions2InternationalString(description);
            if (idesc != null)
                org.setDescription(idesc);
        }
        
        
        if (businessServices != null) {
            Collection businessService =
                    businessServices.getBusinessService();
            //check into this service collection bit -
            Collection serviceCollection = new ArrayList();
            if (businessService != null) {
                Iterator iter = businessService.iterator();
                while (iter.hasNext()){
                    Service service = businessService2Service((BusinessService)iter.next());
                    if (service != null) {
                        serviceCollection.add(service);
                    }
                }
                if (serviceCollection != null)
                    org.setServices(serviceCollection);
            }
        }
        
        if (contacts != null) {
            Collection users = contacts.getContact();
            if (users != null) {
                Iterator iter = users.iterator();
                Collection ousers = org.getUsers();
                org.removeUsers(ousers);
                int i = 0;
                while (iter.hasNext()){
                    UserImpl user = (UserImpl)contact2User((Contact)iter.next());
                    user.setIsLoaded(true);
                    user.setRegistryService(this.service);
                    if (user != null) {
                        user.setSubmittingOrganization(org);
                        user.setOrganization(org);
                    }
                    if (i == 0) {
                        org.setPrimaryContact(user);
                        i++;
                        //adds to user collection
                    } else {
                        org.addUser(user);
                    }
                }
            }
        }
        objectManager.addObjectToCache(org, service.getServiceId());
        return org;
    }
    
    Collection discoveryURLs2ExternalLinks(DiscoveryURLs urls)
    throws JAXRException {
        
        Collection externalLinks = null;
        Collection dUrls = urls.getDiscoveryURL();
        if (dUrls != null) {
            externalLinks = new ArrayList();
            Iterator iter = dUrls.iterator();
            while (iter.hasNext()){
                DiscoveryURL url = (DiscoveryURL)iter.next();
                String uri = url.getValue();
                String type = url.getUseType();
                
                ExternalLinkImpl link = new ExternalLinkImpl();
                link.setRegistryService(this.service);
                link.setValidateURI(false);
                if (uri != null)
                    link.setExternalURI(uri);
                if (type != null)
                    link.setName(new InternationalStringImpl(type));
                else link.setName(new InternationalStringImpl("ExternalLink"));
                externalLinks.add(link);
            }
        }
        return externalLinks;
    }
    
    User contact2User(Contact user) throws JAXRException {
        
        if (user != null) {
            String useType = user.getUseType();
            Collection descriptions = user.getDescription();
            String name = user.getPersonName();
            Collection phone = user.getPhone();
            Collection email = user.getEmail();
            Collection address = user.getAddress();
            
            UserImpl theUser = new UserImpl();
            if (name != null)
                theUser.setPersonName(new PersonNameImpl(name));
            
            if (useType != null)
                theUser.setType(useType);
            
            if (descriptions != null) {
                InternationalString description =
                        descriptions2InternationalString(descriptions);
                if (description != null)
                    theUser.setDescription(description);
            }
            
            if (phone != null) {
                Collection telephoneNumbers = phones2TelephoneNumbers(phone);
                theUser.setTelephoneNumbers(telephoneNumbers);
            }
            if (email != null) {
                Collection emails = emails2EmailAddresses(email);
                if (emails.size() > 0)
                    theUser.setEmailAddresses(emails);
            }
            if (address != null) {
                Collection postalAddresses = addresses2PostalAddresses(address);
                if (postalAddresses.size() > 0)
                    theUser.setPostalAddresses(postalAddresses);
            }
            return theUser;
        }
        return null;
    }
    
    private PostalAddress address2PostalAddress(Address address)
    throws JAXRException {
        
        PostalAddress postalAddress = null;
        
        if (address != null) {
            initPostalSchemes();
            
            Collection addressLines = address.getAddressLine();
            String postalSchemeId = address.getTModelKey();
            
            logger.finest("PostalSchemeId retrieved = " + postalSchemeId);
            if (postalSchemeId != null) {
                //we have a postalSchemeId - does it match a PostalScheme
                //getPostalSchemeById(StringId)
                //for now just temporary
                ClassificationScheme defaultScheme =
                        service.getDefaultPostalScheme();
                
                if (defaultScheme != null) {
                    if (defaultScheme.getKey().getId().equalsIgnoreCase(postalSchemeId)){
                        //we're half way there
                        //is there a semantic equivalence defined?
                        if (postalAddressMap == null) {
                            mapPostalAddressAttributes(defaultScheme);
                        }
                        if (postalAddressMap == null) { 
                            logger.warning(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_PostalAddressMapping"));
                        } else {
                            postalAddress =
                                    addressLines2PostalAddressEquivalence(defaultScheme, addressLines);
                        }
                    }
                }
                
            } //if postal address Id is null we didn't find any equivalence map to slot
            if ((postalAddress == null) && (addressLines != null)) {
                postalAddress = new PostalAddressImpl();
                SlotImpl addressSlot = new SlotImpl();
                addressSlot.setName("addressLines");
                Collection values = new ArrayList();
                
                Iterator liter = addressLines.iterator();
                while (liter.hasNext()){
                    values.add(((AddressLine)liter.next()).getValue());
                }
                addressSlot.setValues(values);
                postalAddress.addSlot(addressSlot);
            }
            
            String useType = address.getUseType();
            if (useType != null)
                postalAddress.setType(useType);
            
            String sortCode = address.getSortCode();
            if (sortCode != null) {
                SlotImpl sortCodeSlot = new SlotImpl();
                sortCodeSlot.setName("sortCode");
                Collection values = new ArrayList();
                values.add(sortCode);
                sortCodeSlot.setValues(values);
                postalAddress.addSlot(sortCodeSlot);
            }
        } //address not null
        return postalAddress;
    }
    
    PostalAddress addressLines2PostalAddressEquivalence(ClassificationScheme defaultScheme,
            Collection addressLines) throws JAXRException {
        
        SlotImpl addressSlot = null;
        Collection values = null;
        PostalAddress postalAddress = null;
        if (postalAddressMap == null)
            return null;
        
        if ((defaultScheme != null) &&
                (addressLines != null)) {
            
            //getDefaultPostalSchemeConcepts
            Collection defaultChildren = defaultScheme.getChildrenConcepts();
            Iterator liter = addressLines.iterator();
            while (liter.hasNext()){
                //is there an equivalence with each of the keyValues of the address lines?
                AddressLine line = (AddressLine)liter.next();
                String keyValue = line.getKeyValue();
                String keyName = line.getKeyName();
                
                if (keyValue != null) {
                    //is there a defaultChild with this value?
                    Iterator childIter = defaultChildren.iterator();
                    while (childIter.hasNext()) {
                        //this is the equivalent child
                        Concept child = (Concept)childIter.next();
                        //default postal equivalent keyvalue matched default postalScheme concept
                        //does the postal AddressMap contain the equivalent value?
                        
                        if (postalAddressMap.containsValue(child)) {
                            Set keys = postalAddressMap.keySet();
                            Iterator kiter = keys.iterator();
                            String childValue = child.getValue();
                            while (kiter.hasNext()) {
                                //this is the jaxr equivalent ConceptValue
                                String jaxrName = (String)kiter.next();
                                
                                //this is the equivalent value
                                //this is the user scheme value
                                Concept value = (Concept)postalAddressMap.get(jaxrName);
                                
                                //if this matches a defaultSchemeChild
                                
                                if (value.getValue().equalsIgnoreCase(keyValue)) {
                                    //we've got a match - the jaxr keyValue
                                    //is the jaxrattribute that we match to
                                    if (postalAddress == null) {
                                        postalAddress = new PostalAddressImpl();
                                    }
                                    postalAddress =
                                            mapEquivalentLine2PostalAttribute(jaxrName,
                                            line, postalAddress);
                                    break;
                                }
                            }
                        }
                        //}
                        
                    }
                } else if (keyName != null) {
                    //does keyName map to any jaxrChildName
                    ClassificationScheme jaxrPostalScheme =
                            findClassificationSchemeByName(null,"PostalAddressAttributes");
                    //assume exact match
                    if (jaxrPostalScheme != null) {
                        Collection children = jaxrPostalScheme.getChildrenConcepts();
                        Iterator citer = children.iterator();
                        while (citer.hasNext()) {
                            Concept child = (Concept) citer.next();
                            String childName = child.getName().getValue();
                            if (childName.equalsIgnoreCase(keyName)) {
                                if (postalAddress == null) {
                                    postalAddress = new PostalAddressImpl();
                                    postalAddress =
                                            mapEquivalentLine2PostalAttribute(keyName,
                                            line, postalAddress);
                                }
                            }
                        }
                    }
                } else { // map it to a slot
                    if (values == null)
                        values = new ArrayList();
                    
                    values.add(line.getValue());
                }
                
            } //end of lines
            
            if ((values != null) && (values.size() > 0)) {
                if (postalAddress == null)
                    postalAddress = new PostalAddressImpl();
                addressSlot = new SlotImpl();
                addressSlot.setName("addressLines");
                addressSlot.setValues(values);
                postalAddress.addSlot(addressSlot);
            }
            return postalAddress;
        }
        return null;
    }
    
    PostalAddress mapEquivalentLine2PostalAttribute(String jaxrName, AddressLine line,
            PostalAddress postalAddress)
            throws JAXRException {
        
        String content = line.getValue();
        
        if (content != null) {
            if (jaxrName.equalsIgnoreCase(STREETNUMBER)) {
                postalAddress.setStreetNumber(content);
            } else if (jaxrName.equalsIgnoreCase(STREET)) {
                postalAddress.setStreet(content);
            } else if (jaxrName.equalsIgnoreCase(CITY)) {
                postalAddress.setCity(content);
            } else if (jaxrName.equalsIgnoreCase(STATE)) {
                postalAddress.setStateOrProvince(content);
            } else if (jaxrName.equalsIgnoreCase(POSTALCODE)) {
                postalAddress.setPostalCode(content);
            } else if (jaxrName.equalsIgnoreCase(COUNTRY)) {
                postalAddress.setCountry(content);
            } else {
                logger.warning(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_matching_postal_Address_Attribute"));
            }
        }
        return postalAddress;
    }
    
    
    
    
    Collection addresses2PostalAddresses(Collection addresses)
    throws JAXRException {
        
        Collection postalAddresses = null;
        if (addresses != null) {
            postalAddresses = new ArrayList();
            
            Iterator iter = addresses.iterator();
            while (iter.hasNext()){
                PostalAddress postalAddress =
                        address2PostalAddress((Address)iter.next());
                if (postalAddress != null)
                    postalAddresses.add(postalAddress);
            }
            return postalAddresses;
        }
        return null;
    }
    
    Collection emails2EmailAddresses(Collection emails) throws JAXRException {
        
        Collection emailAddresses = null;
        if (emails != null) {
            emailAddresses = new ArrayList();
            Iterator iter = emails.iterator();
            while (iter.hasNext()){
                Email email = (Email)iter.next();
                String emailAddress = email.getValue();
                String useType = email.getUseType();
                EmailAddressImpl address = new EmailAddressImpl();
                if (emailAddress != null) {
                    address.setAddress(emailAddress);
                }
                if (useType != null) {
                    address.setType(useType);
                }
                emailAddresses.add(address);
            }
        }
        return emailAddresses;
    }
    
    Collection phones2TelephoneNumbers(Collection phones) throws JAXRException {
        Collection numbers = null;
        if (phones != null) {
            numbers = new ArrayList(phones.size());
            Iterator iter = phones.iterator();
            while (iter.hasNext()){
                TelephoneNumberImpl telephone = new TelephoneNumberImpl();
                Phone phone = (Phone)iter.next();
                String number = phone.getValue();
                if (number != null)
                    telephone.setNumber(number);
                String useType = phone.getUseType();
                if (useType != null) {
                    telephone.setType(useType);
                }
                numbers.add(telephone);
            }
            return numbers;
        }
        return null;
    }
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    ServiceBinding bindingTemplate2ServiceBinding(BindingTemplate template)
    throws JAXRException {
        
        ServiceBindingImpl sBinding = null;
        ServiceImpl sService = null;
        String tkey = template.getBindingKey();
        String skey = template.getServiceKey();
        
        //always fetch skeleton service
        try {
            sService = (ServiceImpl)
            objectManager.fetchObjectFromCache(skey);
        } catch (ClassCastException cce) {
            logger.finest("ClassCastException in bindingTemplate2ServiceBinding fetch service, continuing");
            sService = null;
        }
        
        
        try {
            sBinding  = (ServiceBindingImpl)
            objectManager.fetchObjectFromCache(tkey);
        } catch (ClassCastException cce) {
            logger.finest("ClassCastException in bindingTemplate2ServicBinding fetchin serviceBinding, continuing");
            sBinding = null;
        }
        
        
        if (sBinding == null) {
            sBinding = new ServiceBindingImpl();
            sBinding.setServiceId(this.service.getServiceId());
        }
        sBinding.setRegistryService(this.service);
        sBinding.setIsLoaded(true);
        sBinding.setIsRetrieved(false);
        sBinding.setIsNew(false);
        
        if (sService != null) {
            sService.setIsRetrieved(true);
            sBinding.setService(sService);
        }
        
        sBinding.setKey(new KeyImpl(tkey));
        
        Collection description = template.getDescription();
        InternationalString idesc = descriptions2InternationalString(description);
        sBinding.setDescription(idesc);
        //need to do lang
        
        
        AccessPoint accessPoint = null;
        HostingRedirector redirector = null;
        if (template != null) {
            accessPoint = template.getAccessPoint();
            redirector = template.getHostingRedirector();
        }
        
        //this can only be oneor the other
        if (accessPoint != null) {
            URLType type =
                    accessPoint.getURLType();
            if (type != null) {
                Concept urlTypeConcept = urlType2Concept(type);
                ClassificationImpl classification = new ClassificationImpl(urlTypeConcept);
                classification.setRegistryService(this.service);
                sBinding.addClassification(classification);
            }
            String uri = accessPoint.getValue();
            if (uri != null) {
                sBinding.setValidateURI(false);
                sBinding.setAccessURI(uri);
            }
        }
        
        if (redirector != null) {
            ServiceBinding redirectorBinding =
                    hostingRedirector2TargetBinding(redirector);
            if (redirectorBinding != null) {
                ((ServiceBindingImpl)redirectorBinding).setRegistryService(this.service);
                sBinding.setTargetBinding(redirectorBinding);
            }
        }
        //this can only be one or the other
        
        TModelInstanceDetails tModelInstanceDetails =
                template.getTModelInstanceDetails();
        if (tModelInstanceDetails != null) {
            Collection instanceInfo =
                    tModelInstanceDetails.getTModelInstanceInfo();
            if (instanceInfo != null) {
                logger.finest("Got InstanceInfo");
                Collection specLinks =
                        tModelInstanceInfos2SpecificationLinks(instanceInfo);
                if ((specLinks != null) && (specLinks.size() > 0))
                    sBinding.setSpecificationLinks(specLinks);
            }
        }
        
        if (this.getConnection().useCache())
            objectManager.addObjectToCache(sBinding, this.service.getServiceId());
        else
            objectManager.removeObjectFromCache(sBinding.getKey().getId());
        return sBinding;
    }
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    Service serviceInfo2Service(ServiceInfo sInfo) throws JAXRException {
        
        ServiceImpl rservice = null;
        String serviceKey = sInfo.getServiceKey();
        String businessKey = sInfo.getBusinessKey();
        OrganizationImpl sOrg = null;
        
        //only do fetch if using cache
        if (getConnection().useCache()) {
            try {
                rservice = (ServiceImpl)
                objectManager.fetchObjectFromCache(serviceKey);
            } catch (ClassCastException cce) {
                logger.finest("ClassCastException on fetch of service, serviceInfo2Service, continuing");
                rservice = null;
            }
        }
        
        if (rservice == null) {
            rservice = new ServiceImpl();
            rservice.setServiceId(this.service.getServiceId());
        }
        rservice.setRegistryService(this.service);
        rservice.setIsRetrieved(false);
        rservice.setKey(new KeyImpl(serviceKey));
        
        
        OrganizationImpl registryObject = null;
        if (businessKey != null){
            try {
                registryObject = (OrganizationImpl)
                objectManager.fetchObjectFromCache(businessKey);
            } catch (ClassCastException cce) {
                logger.finest("ClassCastException fetching providing Org, serviceInfo2Service, continuing");
                registryObject = null;
            }
            
            if (registryObject == null) {
                registryObject = new OrganizationImpl();
                registryObject.setKey(new KeyImpl(businessKey));
                registryObject.setIsRetrieved(true);
                registryObject.setRegistryService(this.service);
                registryObject.setIsNew(false);
                //always add skeleton object for incremental loading
                objectManager.addObjectToCache(registryObject, this.service.getServiceId());
            }
            if (registryObject != null) {
                rservice.setProvidingOrganization(registryObject);
            }
        }
        
        Collection name = sInfo.getName();
        InternationalString iname = names2InternationalString(name);
        rservice.setName(iname);
        
        //always add skeleton object for incremental loading
        rservice.setIsRetrieved(true);
        rservice.setIsNew(false);
        
        //make sure that object is fetched from registry for loading
        if (!getConnection().useCache())
            rservice.setIsLoaded(false);
        
        objectManager.addObjectToCache(rservice, this.service.getServiceId());
        return rservice;
    }
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    
    Service businessService2Service(BusinessService bService) throws JAXRException {
        
        ServiceImpl service = null;
        
        String bServiceKey = bService.getServiceKey();
        String businessKey = bService.getBusinessKey();
        
        //always get skeleton service for incremental loading
        try {
            service = (ServiceImpl)
            objectManager.fetchObjectFromCache(bServiceKey);
        } catch (ClassCastException cce) {
            logger.finest("ClassCastException in serviceDetail2Service fetching service, continuing");
            service = null;
        }
        
        if (service == null) {
            service = new ServiceImpl();
            service.setServiceId(this.service.getServiceId());
            service.setRegistryService(this.service);
        }
        service.setIsRetrieved(true);
        service.setIsNew(false);
        service.setIsLoaded(true);
        
        
        OrganizationImpl registryObject = null;
        //always try to fetch skeleton
        try {
            registryObject = (OrganizationImpl)
            objectManager.fetchObjectFromCache(businessKey);
        } catch (ClassCastException cce) {
            logger.finest("ClassCastException fething org in serviceDetail2Service, continuing");
            registryObject = null;
        }
        if (registryObject == null){
            registryObject = new OrganizationImpl();
            registryObject.setKey(new KeyImpl(businessKey));
            registryObject.setRegistryService(this.service);
            registryObject.setIsRetrieved(true);
            registryObject.setIsNew(false);
            registryObject.setIsLoaded(false);
            //this is skeleton, add for incremental loading
            objectManager.addObjectToCache(registryObject, this.service.getServiceId());
        }
        
        service.setProvidingOrganization(registryObject);
        KeyImpl serviceKey = new KeyImpl(bServiceKey);
        service.setKey(serviceKey);
        
        Collection description = bService.getDescription();
        InternationalString idesc =
                descriptions2InternationalString(description);
        if (idesc != null)
            service.setDescription(idesc);
        
        Collection bName = bService.getName();
        InternationalString iname = names2InternationalString(bName);
        if (iname != null)
            service.setName(iname);
        
        CategoryBag cbag = bService.getCategoryBag();
        Collection classifications = categoryBag2Classifications(cbag);
        if ((classifications != null) && (classifications.size() > 0))
            service.setClassifications(classifications);
        
        BindingTemplates bTemplates = bService.getBindingTemplates();
        if (bTemplates != null) {
            Collection template = bTemplates.getBindingTemplate();
            Collection bindings = new ArrayList();
            if (template != null) {
                Iterator iter = template.iterator();
                while (iter.hasNext()){
                    ServiceBindingImpl sBinding = (ServiceBindingImpl)
                    bindingTemplate2ServiceBinding((BindingTemplate)iter.next());
                    if (sBinding != null) {
                        objectManager.addObjectToCache(sBinding, this.service.getServiceId());
                        sBinding.setServiceId(this.service.getServiceId());
                        sBinding.setRegistryService(this.service);
                        sBinding.setStatusFlags(true, true, false);
                        bindings.add(sBinding);
                    }
                }
                //todo: confusing to me?
                Collection currentBindings = service.getServiceBindings();
                service.removeServiceBindings(currentBindings);
                service.addServiceBindings(bindings);
            }
        }
        if (this.getConnection().useCache())
            objectManager.addObjectToCache(service, this.service.getServiceId());
        else
            objectManager.removeObjectFromCache(service.getKey().getId());
        
        return service;
    }
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    //what if disposition report is success
    //may nee keys here
    //todo: this is related to disposition report
    BulkResponse results2BulkResponse(Collection results,
            Collection keys, String type) throws JAXRException {
        
        BulkResponseImpl br = new BulkResponseImpl();
        Collection exceptions = new ArrayList();
        //no matter what the keys are returned
        if (keys != null)
            br.setCollection(keys);
        
        if ((results != null) && (!results.isEmpty())) {
            Iterator iter = results.iterator();
            while (iter.hasNext()){
                Result result = (Result)iter.next();
                KeyType keyType = result.getKeyType();
                int errNo = result.getErrno();
                ErrInfo errInfo =
                        result.getErrInfo();
                
                String errCode = "";
                String content = "";
                if (errInfo != null) {
                    errCode = errInfo.getErrCode();
                    content = errInfo.getValue();
                    if (errCode.equalsIgnoreCase("E_success")) {
                        if (type.equals(DELETE)) getObjectManager().removeObjectsFromCache(keys);
                        return br;
                    }
                } else if (errNo == 0) {
                    if (type.equals(DELETE)) getObjectManager().removeObjectsFromCache(keys);
                    return br;
                }
                
                //fix keyType
                String message =    ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:UDDI_DispositionReport:_Error_Code_=_") + errCode +
                        ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:;_Error_Message_=_") + content +
                        ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:;_Error_Number_=_") + errNo;
                JAXRException ne = null;
                if (type.equals(FIND)) {
                    ne = new FindException(message);
                } else if (type.equals(SAVE)) {
                    ne = new SaveException(message);
                } else if (type.equals(DELETE)){
                    ne = new DeleteException(message);
                }
                br.addException(ne);
            }
        }
        return br;
    }
    
    /**
     * gets the specified RegistryObjects
     *
     * @return BulkResponse containing Collection of RegistryObjects.
     */
    
    //needs modification
    //now level 1
    //todo: take it out
    BulkResponse getRegistryObjects(Collection objectKeys) throws JAXRException {
        
        HashMap sortedKeys = sortObjectType(objectKeys);
        Collection unknownKeys = (Collection)sortedKeys.get("unknown");
        Collection tempKeys = null;
        
        Collection responses = new ArrayList();
        
        tempKeys = (Collection)sortedKeys.get("orgs");
        
        if (tempKeys != null) {
            tempKeys.addAll(unknownKeys);
            if ((tempKeys != null) && (!tempKeys.isEmpty())) {
                responses.add(getOrganizations(tempKeys));
            }
        }
        
        if (tempKeys != null) {
            tempKeys = (Collection)sortedKeys.get("services");
            tempKeys.addAll(unknownKeys);
            if ((tempKeys != null) && (!tempKeys.isEmpty())) {
                responses.add(getServices(tempKeys));
            }
        }
        
        if (tempKeys != null) {
            tempKeys = (Collection)sortedKeys.get("bindings");
            tempKeys.addAll(unknownKeys);
            if ((tempKeys != null) && (!tempKeys.isEmpty())) {
                responses.add(getServiceBindings(tempKeys));
            }
        }
        
        if (tempKeys != null) {
            tempKeys = (Collection)sortedKeys.get("concepts");
            tempKeys.addAll(unknownKeys);
            if ((tempKeys != null) && (!tempKeys.isEmpty())) {
                responses.add(getConcepts(tempKeys));
            }
        }
        
        if (tempKeys != null) {
            tempKeys = (Collection)sortedKeys.get("schemes");
            tempKeys.addAll(unknownKeys);
            if ((tempKeys != null) && (!tempKeys.isEmpty())) {
                responses.add(getConcepts(tempKeys));
            }
        }
        return BulkResponseImpl.combineBulkResponses(responses);
    }
    
    BulkResponse getRegistryObjects(Collection objectKeys, String objectType)
    throws JAXRException {
        
        BulkResponse bulkResponse = null;
        if (objectType.equals(LifeCycleManager.ORGANIZATION))
            bulkResponse = bulkResponse = getOrganizations(objectKeys);
        else if (objectType.equals(LifeCycleManager.SERVICE))
            bulkResponse = getServices(objectKeys);
        else if (objectType.equals(LifeCycleManager.SERVICE_BINDING))
            bulkResponse = getServiceBindings(objectKeys);
        else if (objectType.equals(LifeCycleManager.CONCEPT) ||
                objectType.equals(LifeCycleManager.CLASSIFICATION_SCHEME))
            bulkResponse = getConcepts(objectKeys);
        else
            throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Unknown_Object_Type_") + objectType + ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:_cannot_retrieve"));
        
        return bulkResponse;
    }
    
    
    //gets orgs and tmodels owned by caller
    BulkResponse getRegistryObjects() throws JAXRException {
        
        GetRegisteredInfo info = null;
        info = objFactory.createGetRegisteredInfo();
        info.setGeneric(UDDIVERSION);
        
        String authInfo = null;
        authInfo = getAuthInfo();
        if (authInfo == null) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Credentials_present"));
        }
        info.setAuthInfo(authInfo);
        return getProcessor().processRequestJAXB(info, true, null, FIND);
    }
    
    
    //todo - level 1 take out
    RegistryObject getRegistryObject(RegistryObjectImpl ro) throws JAXRException {
        
        BulkResponse bulkResponse = null;
        BulkResponse associationBulkResponse = null;
        
        if (ro == null)
            return null;
        
        Key objectKey = ro.getKey();
        Collection tempKeys = new ArrayList();
        tempKeys.add(objectKey);
        
        if (ro instanceof OrganizationImpl) {
            bulkResponse = getOrganizations(tempKeys);
        } else if (ro instanceof ServiceImpl)
            bulkResponse = getServices(tempKeys);
        else if (ro instanceof ServiceBindingImpl)
            bulkResponse = getServiceBindings(tempKeys);
        else if (ro instanceof ConceptImpl)
            bulkResponse = getConcepts(tempKeys);
        else {
            throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Unknown_Object_Type"));
        }
        if (bulkResponse.getExceptions() == null) {
            //get the registryObject
            Collection objs = bulkResponse.getCollection();
            Iterator iter = objs.iterator();
            if (iter.hasNext()){
                RegistryObject registryObj = (RegistryObject)iter.next();
                return registryObj;
            }
        }
        return null;
    }
    
    
    
    //this is object key
    RegistryObject getRegistryObject(String id, String objectType) throws JAXRException {
        if (id==null)
            return null;
        
        BulkResponse bulkResponse = null;
        
        RegistryObject ro =
                objectManager.fetchObjectFromCache(id);
        
        if (ro != null)
            return ro;
        
        KeyImpl objectKey = new KeyImpl(id);
        Collection keys = new ArrayList();
        keys.add(objectKey);
        
        BulkResponse br = getRegistryObjects(keys, objectType);
        
        if (br.getExceptions() == null) {
            Collection registryObjects = br.getCollection();
            RegistryObject registryObject = null;
            //just test to see if registryObject size is 1
            if (registryObjects.size() == 1) {
                registryObject = (RegistryObject)
                ((ArrayList)registryObjects).get(0);
                return registryObject;
            }
        } else {
            //throw an exception
            Collection exceptions = br.getExceptions();
            Iterator eiter = exceptions.iterator();
            if (eiter.hasNext()) {
                throw (JAXRException)eiter.next();
            }
        }
        return null;
    }
    
    
    String getAuthorizationToken(java.util.Set credentials)
    throws JAXRException {
        
        //on each invocation
        String userName = null;
        String password = null;
        char [] passwd = null;
        if ((credentials != null) && (credentials.size() > 0)) {
            Iterator iter = credentials.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                if (obj instanceof java.net.PasswordAuthentication) {
                    userName = ((PasswordAuthentication)obj).getUserName();
                    passwd = ((PasswordAuthentication)obj).getPassword();
                    password = new String(passwd);
                }
            }
        }
        
        if (userName == null || passwd == null)
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:User_Name_and/or_Password_not_set."));
        
        GetAuthToken getAuthToken = null;
        getAuthToken = objFactory.createGetAuthToken();
        getAuthToken.setGeneric(UDDIVERSION);
        getAuthToken.setUserID(userName);
        getAuthToken.setCred(password);
        password = null;
        BulkResponse br = null;
        
        br = getProcessor().processRequestJAXB(getAuthToken, true, null, SAVE);
        if (br.getExceptions() == null) {
            
            Collection tokens = br.getCollection();
            if (tokens.size() > 0)
                return (String)((ArrayList)tokens).get(0);
        } else {
            
            //get the exception
            Collection exceptions = br.getExceptions();
            Iterator eiter = exceptions.iterator();
            while (eiter.hasNext()) {
                JAXRException exception = (JAXRException)eiter.next();
                throw exception;
            }
        }
        
        return null;
    }
    
    HashMap sortObjectType(Collection objectKeys)
    throws UnexpectedObjectException, JAXRException {
        
        Collection orgs = new ArrayList();
        Collection services = new ArrayList();
        Collection bindings = new ArrayList();
        Collection concepts = new ArrayList();
        Collection classificationSchemes = new ArrayList();
        Collection unknown = new ArrayList();
        
        HashMap sortedKeys = new HashMap();
        
        Iterator iter = objectKeys.iterator();
        while (iter.hasNext()) {
            KeyImpl key = (KeyImpl) iter.next();
            RegistryObject ro = (RegistryObject)
            objectManager.fetchObjectFromCache(key.getId());
            if (ro != null) {
                if (ro instanceof Organization)
                    orgs.add(key);
                else if (ro instanceof Service)
                    services.add(key);
                else if (ro instanceof ServiceBinding)
                    bindings.add(key);
                else if (ro instanceof Concept)
                    concepts.add(key);
                else if (ro instanceof ClassificationScheme)
                    classificationSchemes.add(key);
                else
                    throw new UnexpectedObjectException();
            } else {
                if ((key.getId().indexOf("uuid") != -1) || (key.getId().indexOf("UUID") != -1)){
                    concepts.add(key);
                } else {
                    unknown.add(key);
                }
            }
        }
        
        sortedKeys.put("orgs",orgs);
        sortedKeys.put("services",services);
        sortedKeys.put("bindings",bindings);
        sortedKeys.put("concepts",concepts);
        sortedKeys.put("schemes",classificationSchemes);
        sortedKeys.put("unknown",unknown);
        
        return sortedKeys;
    }
    
    /**
     * Saves one or more CataloguedObjects to the registry.
     * If an object is not in the registry, then it is created in the registry.
     * If it already exists in the registry and has been modified, then its
     * state is updated (replaced) in the registry.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing the Collection of registryObjects for those objects that were
     * saved successfully and any SaveException that was encountered in case of partial commit.
     */
    BulkResponse saveObjects(Collection registryObjects)
    throws JAXRException {
        
        Collection responses = new ArrayList();
        
        Collection organizations = new ArrayList();
        Collection services = new ArrayList();
        Collection serviceBindings = new ArrayList();
        Collection concepts = new ArrayList();
        Collection classificationSchemes = new ArrayList();
        Iterator iter = registryObjects.iterator();
        while (iter.hasNext()) {
            Object cObject = iter.next();
            if (cObject instanceof Organization) {
                organizations.add(cObject);
            } else if (cObject instanceof Service) {
                services.add(cObject);
            } else if (cObject instanceof ServiceBinding) {
                serviceBindings.add(cObject);
            } else if (cObject instanceof Concept) {
                concepts.add(cObject);
            } else if (cObject instanceof ClassificationScheme) {
                classificationSchemes.add(cObject);
            } else {
                throw new UnexpectedObjectException();
            }
        }
        if (organizations.size() != 0)
            responses.add(saveOrganizations(organizations));
        if (services.size() != 0)
            responses.add(saveServices(services));
        if (serviceBindings.size() != 0)
            responses.add(saveServiceBindings(serviceBindings));
        if (concepts.size() != 0)
            responses.add(saveConcepts(concepts));
        if (classificationSchemes.size() != 0)
            responses.add(saveClassificationSchemes(classificationSchemes));
        
        return BulkResponseImpl.combineBulkResponses(responses);
    }
    
    
    /**
     * Deletes one or more previously submitted objects from the registry.
     *
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @return BulkResponse containing the Collection of keys for those objects that were
     * deleted successfully and any DeleteException that was encountered in case of partial commit.
     */
    BulkResponse deleteObjects(Collection keys)
    throws JAXRException {
        
        HashMap sortedKeys = (HashMap)sortObjectType(keys);
        Collection tempKeys = null;
        Collection responses = new ArrayList();
        
        
        tempKeys = (Collection)sortedKeys.get("orgs");
        if ((tempKeys != null) && (!tempKeys.isEmpty())) {
            responses.add(deleteOrganizations(tempKeys));
        }
        
        tempKeys = (Collection)sortedKeys.get("services");
        if ((tempKeys != null) && (!tempKeys.isEmpty())) {
            responses.add(deleteServices(tempKeys));
        }
        
        tempKeys = (Collection)sortedKeys.get("bindings");
        if ((tempKeys != null) && (!tempKeys.isEmpty())) {
            responses.add(deleteServiceBindings(tempKeys));
        }
        
        tempKeys = (Collection)sortedKeys.get("concepts");
        if ((tempKeys != null) && (!tempKeys.isEmpty())) {
            responses.add(deleteConcepts(tempKeys));
        }
        
        tempKeys = (Collection)sortedKeys.get("schemes");
        if ((tempKeys != null) && (!tempKeys.isEmpty())) {
            responses.add(deleteConcepts(tempKeys));
        }
        return BulkResponseImpl.combineBulkResponses(responses);
    }
    
    BulkResponse deleteObjects(Collection keys, String objectType)
    throws JAXRException {
        
        BulkResponse bulkResponse = null;
        if (objectType.equals(LifeCycleManager.ORGANIZATION))
            bulkResponse = deleteOrganizations(keys);
        if (objectType.equals(LifeCycleManager.SERVICE))
            bulkResponse = deleteServices(keys);
        if (objectType.equals(LifeCycleManager.SERVICE_BINDING))
            bulkResponse = deleteServiceBindings(keys);
        if (objectType.equals(LifeCycleManager.CONCEPT) ||
                objectType.equals(LifeCycleManager.CLASSIFICATION_SCHEME))
            bulkResponse = deleteConcepts(keys);
        return bulkResponse;
    }
    
    /**
     * Not yet implemented.
     */
    public BulkResponse saveAssociations(Collection associations,
            boolean replace) throws JAXRException {
        
        if (associations.size() == 0)
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("ConnectionImpl:UDDIMapper:No_Associations_found_to_save"));
        
        if (replace)
            return saveAllAssociations(associations, replace);
        
        AddPublisherAssertions publisherAssertions = null;
        publisherAssertions = objFactory.createAddPublisherAssertions();
        publisherAssertions.setGeneric(UDDIVERSION);
        
        String authInfo = getAuthInfo();
        if (authInfo == null) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Credentials_or_Invalid_Credential_Information"));
        }
        publisherAssertions.setAuthInfo(authInfo);
        
        Collection assertions =
                associations2PublisherAssertions(associations);
        if (assertions != null)
            publisherAssertions.getPublisherAssertion().addAll(assertions);
        
        //save must return keys of those objects saved-
        Collection associationKeys = new ArrayList();
        Iterator associationIterator = associations.iterator();
        while (associationIterator.hasNext()) {
            KeyImpl associationKey = (KeyImpl) ((Association)associationIterator.next()).getKey();
            associationKeys.add(associationKey);
        }
        //return disposition report with success
        return getProcessor().processRequestJAXB(publisherAssertions, true, associationKeys, SAVE);
    }
    
    /**
     * Not yet implemented.
     */
    public BulkResponse saveAllAssociations(Collection associations,
            boolean replace) throws JAXRException {
        
        //this is if replace is true-
        
        SetPublisherAssertions publisherAssertions = null;
        publisherAssertions = objFactory.createSetPublisherAssertions();
        publisherAssertions.setGeneric(UDDIVERSION);
        
        String authInfo = getAuthInfo();
        if (authInfo == null) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Credential_or_Invalid_Credential_Information"));
        }
        publisherAssertions.setAuthInfo(authInfo);
        
        Collection assertions =
                associations2PublisherAssertions(associations);
        publisherAssertions.getPublisherAssertion().addAll(assertions);
        
        //save must return keys of those objects saved-
        Collection associationKeys = new ArrayList();
        Iterator associationIterator = associations.iterator();
        while (associationIterator.hasNext()) {
            KeyImpl associationKey = (KeyImpl)
            ((Association)associationIterator.next()).getKey();
            associationKeys.add(associationKey);
        }
        
        //return publisherAssertions
        return getProcessor().processRequestJAXB(publisherAssertions, true, associationKeys, SAVE);
    }
    
    //finds all public associations - can be invoked by anyone
    
    //case 1 - finds visible associations on the id specified where
    //id is the sourceObject - from key in uddi speak
    //returns associations with id = sourceId
    
    
    //case 2 - finds visible associations on the id specified where
    //id is the target id (to key in uddi speak
    //returns associations with id = targetId
    
    
    //case 3 - finds visible associations on the id specified where the
    //source id represents the source object and the target Id
    //represents the target Organization
    //both to source and targetKey must match
    public BulkResponse findAssociations(Collection findQualifiers,
            String sourceKeyId,
            String targetKeyId,
            Collection associationTypes)
            throws JAXRException {
        
        int filteredOutcome = -1;
        
        FindRelatedBusinesses relatedBusiness = null;
        relatedBusiness = objFactory.createFindRelatedBusinesses();
        relatedBusiness.setGeneric(UDDIVERSION);
        String maxrows = this.getConnection().getMaxRows();
        if (maxrows != null) {
            int rows = Integer.parseInt(maxrows);
            relatedBusiness.setMaxRows(rows);
        }
        
        if ((sourceKeyId == null) && (targetKeyId == null))
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:RegistryObject_required_for_FindAssociatedObjects_method._"));
        
        Collection keys = new ArrayList();
        if ((sourceKeyId != null) && (targetKeyId != null)){
            filteredOutcome = SOURCE_KEY_MUST_MATCH_SOURCE_AND_TARGET_KEY_MUST_MATCH_TARGET;
            relatedBusiness.setBusinessKey(sourceKeyId);
            keys.add(new KeyImpl(sourceKeyId));
        } else if (sourceKeyId != null){
            filteredOutcome = SOURCE_KEY_MUST_MATCH_SOURCE;
            relatedBusiness.setBusinessKey(sourceKeyId);
            keys.add(new KeyImpl(sourceKeyId));
        } else if (targetKeyId != null) {
            filteredOutcome = TARGET_KEY_MUST_MATCH_TARGET;
            relatedBusiness.setBusinessKey(targetKeyId);
            keys.add(new KeyImpl(targetKeyId));
        } else
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Registry_Object_Key_required_for_findAssociatedObjects._"));
        
        
        FindQualifiers fQualifiers =
                strings2FindQualifiers(findQualifiers);
        if (fQualifiers != null)
            relatedBusiness.setFindQualifiers(fQualifiers);
        
        Collection keyedReferences =
                associationTypes2KeyedReferences(associationTypes);
        
        Collection responses = new ArrayList();
        BulkResponse bulkResponse = null;
        
        if ((keyedReferences != null) && (!keyedReferences.isEmpty())) {
            Iterator iter = keyedReferences.iterator();
            while (iter.hasNext()){
                relatedBusiness.setKeyedReference((KeyedReference)iter.next());
                responses.add(getProcessor().processRequestJAXB(relatedBusiness, false, keys, FIND));
            }
            bulkResponse = BulkResponseImpl.combineBulkResponses(responses);
        } else bulkResponse = getProcessor().processRequestJAXB(relatedBusiness, false, keys, FIND);
        
        if (bulkResponse.getExceptions() != null)
            return bulkResponse;
        
        return helper.filterAssociations(bulkResponse, filteredOutcome, sourceKeyId, targetKeyId);
    }
    
    
    public BulkResponse findCallerAssociations(Collection findQualifiers,
            Boolean callerIsConfirmed,
            Boolean otherIsConfirmed,
            Collection associationTypes)
            throws JAXRException {
        
        GetAssertionStatusReport getAssertionStatus = null;
        getAssertionStatus = objFactory.createGetAssertionStatusReport();
        
        String authInfo = null;
        authInfo = getAuthInfo();
        if (authInfo == null) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Credentials_present"));
        }
        getAssertionStatus.setGeneric(UDDIVERSION);
        getAssertionStatus.setAuthInfo(authInfo);
        
        
        //need to get All completion statuses
        Collection responses = new ArrayList();
        Collection statuses =
                confirmationState2CompletionStatus(callerIsConfirmed, otherIsConfirmed);
        
        fromKeysOwned = new ArrayList();
        toKeysOwned = new ArrayList();
        
        Iterator statusIter = statuses.iterator();
        while (statusIter.hasNext()) {
            
            String completionStatus = (String)statusIter.next();
            getAssertionStatus.setCompletionStatus(completionStatus);
            responses.add(getProcessor().processRequestJAXB(getAssertionStatus, true, null, FIND));
        }
        
        
        BulkResponse bResponse = BulkResponseImpl.combineBulkResponses(responses);
        if (bResponse.getExceptions() == null) {
            BulkResponse bulkResponse =
                    helper.filterAssociationsByConfirmationState(bResponse, callerIsConfirmed,
                    otherIsConfirmed,fromKeysOwned, toKeysOwned);
            if (associationTypes != null) {
                
                return helper.filterByAssociationTypes(bulkResponse, associationTypes);
            }
            else return bulkResponse;
        }
        
        return bResponse;
    }
    
    void confirmAssociation(Association association) throws JAXRException {
        
        if (association != null) {
            Collection associations = new ArrayList();
            associations.add(association);
            BulkResponse bulkResponse = saveAssociations(associations, false);
            
            if (bulkResponse.getExceptions() != null){
                Collection exceptions = bulkResponse.getExceptions();
                Iterator exIter = exceptions.iterator();
                if (exIter.hasNext()) {
                    throw (JAXRException)exIter.next();
                }
            }
        }
    }
    
    void unConfirmAssociation(Association association) throws JAXRException {
        
        if (association != null) {
            Collection associationIds = new ArrayList();
            String id = association.getKey().getId();
            if (id != null) {
                associationIds.add(id);
                BulkResponse bulkResponse = deleteAssociations(associationIds);
                if (bulkResponse.getExceptions() != null){
                    Collection exceptions = bulkResponse.getExceptions();
                    Iterator exIter = exceptions.iterator();
                    if (exIter.hasNext()) {
                        throw (JAXRException)exIter.next();
                        
                    }
                }
            }
        }
    }
    
    /**
     * Not yet implemented.
     */
    BulkResponse findSourceAssociations(Collection findQualifiers,
            Boolean sourceObjectConfirmed,
            Boolean targetObjectConfirmed,
            Collection associationTypes) throws JAXRException {
        
        GetPublisherAssertions getAssertions = null;
        getAssertions = objFactory.createGetPublisherAssertions();
        
        String authInfo = null;
        authInfo = getAuthInfo();
        
        if (authInfo == null) {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_Credentials_present"));
        }
        getAssertions.setGeneric(UDDIVERSION);
        getAssertions.setAuthInfo(authInfo);
        
        //may want to pass key
        //return publisherAssertionMessage
        return getProcessor().processRequestJAXB(getAssertions, true, null, FIND);
        
    }
    
    
    String makeRegistrySpecificRequest(String request, boolean secure)
    throws JAXRException {
        
        if (request == null)
            return null;
        
        logger.finest(request);
        
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(request.getBytes());
        
        SOAPMessage msg = null;
        try {
            Object obj = MarshallerUtil.getInstance().jaxbUnmarshalInputStream(inputStream);
            msg = MarshallerUtil.getInstance().jaxbMarshalObject(obj);
        } catch (JAXBException jbe) {
            throw new JAXRException(jbe);
        }
        
        
        Node response =
                service.send(msg, secure);
        Node resultNode = null;
        String name = response.getNodeName();
        
        if (((SOAPBody) response).hasFault()) {
            if (response instanceof Element) {
                NodeList list = ((Element) response).getElementsByTagName("dispositionReport");
                if (list != null) {
                    int listLength = list.getLength();
                    if (listLength > 0) {
                        
                        for (int i = 0; i < listLength; i++) {
                            Node n = list.item(i);
                            if (n != null) {
                                resultNode = n;
                                name = "dispositionReport";
                                break;
                            }
                        }
                    }
                }
            }
        } else
            resultNode = response.getFirstChild();
        
        ByteArrayOutputStream outStream = null;
        try {
            Object obj = MarshallerUtil.getInstance().jaxbUnmarshalObject(resultNode);
            outStream = (ByteArrayOutputStream) MarshallerUtil.getInstance().jaxbMarshalOutStream(obj);
        } catch (JAXBException jbe) {
            throw new JAXRException(jbe);
        }
        
        return outStream.toString();
    }
    
    Collection confirmationState2CompletionStatus(  Boolean callerIsConfirmed,
            Boolean otherIsConfirmed) {
        
        Collection statuses = new ArrayList();
        String completionStatus = null;
        
        //due to bugs in registries we just get all states
        if ((callerIsConfirmed != null) && (otherIsConfirmed != null)) {
            
            if (callerIsConfirmed.booleanValue() && otherIsConfirmed.booleanValue())
                statuses.add(COMPLETE);
            else {
                statuses.add(TO_KEY_INCOMPLETE);
                statuses.add(FROM_KEY_INCOMPLETE); //filter by me complete
            }
        } else {
            //anyCase with null have to do all three calles
            statuses.add(COMPLETE);
            statuses.add(TO_KEY_INCOMPLETE);
            statuses.add(FROM_KEY_INCOMPLETE);
        }
        return statuses;
    }
    
    
    Collection associationKeys2PublisherAssertions(Collection associationKeys)
    throws JAXRException {
        
        Collection assertions = new ArrayList();
        if (associationKeys != null){
            Iterator keyIter = associationKeys.iterator();
            try {
                while(keyIter.hasNext()){
                    KeyImpl associationKey = (KeyImpl)keyIter.next();
                    PublisherAssertion assertion =
                            associationKey2PublisherAssertion(associationKey);
                    if (assertion != null)
                        assertions.add(assertion);
                }
            } catch (ClassCastException cce) {
                throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_javax.xml.infomodel.Key"), cce);
            }
        }
        return assertions;
    }
    
    PublisherAssertion associationKey2PublisherAssertion(KeyImpl associationKey)
    throws JAXRException {
        
        PublisherAssertion assertion = null;
        if (associationKey != null) {
            String id = associationKey.getId();
            if (id != null) {
                StringTokenizer tokens = new StringTokenizer(id, ":", false);
                if (tokens.countTokens() == 3) {
                    String sourceKeyId = tokens.nextToken();
                    String targetKeyId = tokens.nextToken();
                    String associationType = tokens.nextToken();
                    assertion = null;
                    assertion = objFactory.createPublisherAssertion();
                    assertion.setFromKey(sourceKeyId);
                    assertion.setToKey(targetKeyId);
                    KeyedReference keyedRef =
                            associationType2KeyedReference(associationType);
                    
                    if (keyedRef != null)
                        assertion.setKeyedReference(keyedRef);
                    
                } else throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Association_Key_id_is_incorrectly_formated_-_expected_SourceObjectKeyId:TargetObjectKeyId:AssociationTypeString._"));
            }
        }
        return assertion;
    }
    
    
    
    Collection publisherAssertions2Associations(Collection publisherAssertions)
    throws JAXRException {
        Collection associations = null;
        if (publisherAssertions != null) {
            associations = new ArrayList();
            Iterator iter = publisherAssertions.iterator();
            while (iter.hasNext()){
                Association association =
                        publisherAssertion2Association((PublisherAssertion)iter.next());
                if (association != null) {
                    associations.add(association);
                }
            }
        }
        return associations;
    }
    
    Association publisherAssertion2Association(PublisherAssertion assertion)
    throws JAXRException {
        
        AssociationImpl association = null;
        if (assertion != null) {
            
            String fromKey = assertion.getFromKey();
            String toKey = assertion.getToKey();
            KeyedReference keyedRef = assertion.getKeyedReference();
            Concept associationType =
                    keyedReference2AssociationType(keyedRef);
            String associationTypeValue = associationType.getValue();
            KeyImpl key =
                    buildAssociationKey(fromKey, toKey, associationTypeValue);
            
            association = (AssociationImpl)
            objectManager.fetchObjectFromCache(key.getId());
            if (association == null) {
                association = new AssociationImpl();
                association.setServiceId(this.service.getServiceId());
                association.setRegistryService(this.service);
            }
            if (!fromKey.equalsIgnoreCase(toKey))
                association.setIsExtramural(true);
            
            association.setAssociationType(associationType);
            association.setIsRetrieved(true);
            association.setIsNew(false);
            
            if (key != null)
                association.setKey(key);
            //not fully loaded because no confirmation state
            association.setIsLoaded(false);
            
            
            Organization sourceObject = null;
            if ((fromKey != null) && (association.getSourceObject() == null)) {
                sourceObject = (Organization)
                objectManager.fetchObjectFromCache(fromKey);
                if (sourceObject == null)
                    sourceObject = (Organization)
                    getRegistryObject(fromKey, LifeCycleManager.ORGANIZATION);
                if (sourceObject != null) {
                    association.setSourceObject(sourceObject);
                    sourceObject.addAssociation(association);
                }
            }
            
            Organization targetObject = null;
            if ((toKey != null) && (association.getTargetObject() == null)) {
                targetObject = (Organization)
                objectManager.fetchObjectFromCache(toKey);
                if (targetObject == null)
                    targetObject = (Organization)
                    getRegistryObject(toKey, LifeCycleManager.ORGANIZATION);
                if (targetObject != null){
                    association.setTargetObject(targetObject);
                }
            }
            
            if (association != null)
                objectManager.addObjectToCache(association, this.service.getServiceId());
            
            return association;
        }
        return null;
    }
    
    Collection assertionStatusItems2Associations(Collection assertionStatusItems)
    throws JAXRException {
        Collection associations = null;
        if (assertionStatusItems != null) {
            associations = new ArrayList(assertionStatusItems.size());
            Iterator iter = assertionStatusItems.iterator();
            while (iter.hasNext()){
                Association association =
                        assertionStatusItem2Association((AssertionStatusItem)iter.next());
                if (association != null)
                    associations.add(association);
            }
            return associations;
        }
        return null;
    }
    
    Association assertionStatusItem2Association(AssertionStatusItem item)
    throws JAXRException {
        
        AssociationImpl association = null;
        if (item != null) {
            logger.finest("Got assertionStatusItem");
            String completionStatus = item.getCompletionStatus();
            String fromKey = item.getFromKey();
            String toKey = item.getToKey();
            KeyedReference keyedRef = item.getKeyedReference();
            
            //need associationtype
            Concept type = null;
            if (keyedRef != null)
                type =
                        keyedReference2AssociationType(keyedRef);
            
            String typeString = null;
            if (type != null) {
                typeString = type.getValue();
            }
            
            KeyImpl key = buildAssociationKey(fromKey, toKey, typeString);
            logger.finest("AssociationKeyIs " + key.getId());
            //getAssociation from cache
            association = (AssociationImpl)
            objectManager.fetchObjectFromCache(key.getId());
            if (association == null) {
                association = new AssociationImpl();
                association.setServiceId(this.service.getServiceId());
                association.setRegistryService(this.service);
            }
            if (key != null)
                association.setKey(key);
            
            if (!fromKey.equalsIgnoreCase(toKey))
                association.setIsExtramural(true);
            
            association.setIsRetrieved(true);
            association.setIsNew(false);
            association.setIsLoaded(true);
            
            KeysOwned keysOwned = item.getKeysOwned();
            String ownedFromKey = keysOwned.getFromKey();
            String ownedToKey = keysOwned.getToKey();
            
            if (ownedFromKey != null)
                fromKeysOwned.add(ownedFromKey);
            else if (ownedToKey != null)
                toKeysOwned.add(ownedToKey);
            else {};
            
            if (type != null)
                association.setAssociationType(type);
            
            //what do we do with get keys owned?
            logger.finest("Setting confirmationState");
            if (completionStatus.equals(COMPLETE)) {
                association.setIsConfirmedBySourceOwner(true);
                association.setIsConfirmedByTargetOwner(true);
            } else if (completionStatus.equals(TO_KEY_INCOMPLETE)) {
                association.setIsConfirmedBySourceOwner(true);
                association.setIsConfirmedByTargetOwner(false);
            } else if (completionStatus.equals(FROM_KEY_INCOMPLETE)) {
                association.setIsConfirmedBySourceOwner(false);
                association.setIsConfirmedByTargetOwner(true);
            }
            Organization sourceObject = null;
            if ((fromKey != null) && (association.getSourceObject() == null)){
                sourceObject = (Organization)
                objectManager.fetchObjectFromCache(fromKey);
                if (sourceObject == null)
                    sourceObject =(Organization)
                    getRegistryObject(fromKey, LifeCycleManager.ORGANIZATION);
                if (sourceObject != null){
                    association.setSourceObject(sourceObject);
                    sourceObject.addAssociation(association);
                }
            }
            
            Organization targetObject = null;
            if ((toKey != null) && (association.getTargetObject() == null)) {
                targetObject = (Organization)
                objectManager.fetchObjectFromCache(toKey);
                if (targetObject == null)
                    targetObject = (Organization)
                    getRegistryObject(toKey, LifeCycleManager.ORGANIZATION);
                if (targetObject != null) {
                    association.setTargetObject(targetObject);
                }
            }
            
            if (association != null)
                objectManager.addObjectToCache(association,
                        this.service.getServiceId());
            logger.finest("Returning association with a value");
            return association;
        }
        return null;
    }
    
    KeyImpl buildAssociationKey(String fromKey, String toKey, String typeString) {
        //now generate AssociationKey
        StringBuffer keyBuf = new StringBuffer(400);
        keyBuf.append(fromKey);
        keyBuf.append(":");
        keyBuf.append(toKey);
        keyBuf.append(":");
        keyBuf.append(typeString);
        return new KeyImpl(keyBuf.toString());
    }
    
    Concept keyedReference2AssociationType(KeyedReference keyedRef)
    throws JAXRException {
        
        //tbd should be ref to predefined ones-
        //actually we've got to mapp then to equivalent jaxr types
        ConceptImpl type = null;
        if (keyedRef != null) {
            String key = keyedRef.getTModelKey();
            String name = keyedRef.getKeyName();
            String value = keyedRef.getKeyValue();
            
            String newName = null;
            String newValue = null;
            
            if (value.equalsIgnoreCase(PEER_TO_PEER)){
                newName = RELATES_TO;
                newValue = RELATES_TO;
            } else if (value.equalsIgnoreCase(IDENTITY)){
                newName = EQUIVALENT_TO;
                newValue = EQUIVALENT_TO;
            } else if (value.equalsIgnoreCase(PARENT_TO_CHILD)){
                newName = HAS_CHILD;
                newValue = HAS_CHILD;
            } else {
                newName = name;
                newValue = value;
            }
            
            
            type = new ConceptImpl();
            if (key != null)
                type.setKey(new KeyImpl(key));
            if (name != null)
                type.setName(new InternationalStringImpl(newName));
            if (value != null)
                type.setValue(newValue);
        }
        return type;
    }
    
    /**
     * Not yet implemented.
     */
    public BulkResponse getRegistryObjects(String objectType)
    throws JAXRException {
        
        BulkResponseImpl sbr = new BulkResponseImpl();
        Collection organizations = new ArrayList();
        Collection services = new ArrayList();
        Collection serviceBindings = new ArrayList();
        Collection concepts = new ArrayList();
        Collection classificationSchemes = new ArrayList();
        
        if (objectType != null) {
            BulkResponse br = getRegistryObjects();
            if (br != null) {
                if (br.getExceptions() == null) {
                    Collection objects = br.getCollection();
                    
                    Iterator iter = objects.iterator();
                    while (iter.hasNext()) {
                        Object cObject = iter.next();
                        if (cObject instanceof Organization) {
                            organizations.add(cObject);
                        } else if (cObject instanceof Service) {
                            services.add(cObject);
                        } else if (cObject instanceof ServiceBinding) {
                            serviceBindings.add(cObject);
                        } else if (cObject instanceof Concept) {
                            concepts.add(cObject);
                        } else if (cObject instanceof ClassificationScheme) {
                            classificationSchemes.add(cObject);
                        } else {
                            throw new UnexpectedObjectException();
                        }
                    }
                    
                    //ok now got all the objects sorted -
                    //what am I looking for
                    if (objectType.equals(LifeCycleManager.ORGANIZATION)) {
                        sbr.setCollection(organizations);
                    } else if (objectType.equals(LifeCycleManager.SERVICE)) {
                        //get the services from the organization
                        sbr.setCollection( helper.getAllServicesFromOrganizations(organizations));
                    } else if (objectType.equals(LifeCycleManager.SERVICE_BINDING)) {
                        sbr.setCollection( helper.getAllServiceBindingsFromOrganizations(organizations));
                    } else if (objectType.equals(LifeCycleManager.CONCEPT)) {
                        sbr.setCollection(concepts);
                    } else if (objectType.equals(LifeCycleManager.CLASSIFICATION_SCHEME)) {
                        sbr.setCollection(classificationSchemes);
                    } else {
                        throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Unexpected_Object_type"));
                    }
                }
            }
        }
        return sbr;
    }
    
    
    
    
    Collection associations2PublisherAssertions(Collection associations)
    throws JAXRException {
        
        Collection assertions = new ArrayList();
        if (associations != null) {
            Iterator associationIterator = associations.iterator();
            try {
                while (associationIterator.hasNext()) {
                    Association association = (Association)associationIterator.next();
                    PublisherAssertion assertion =
                            association2PublisherAssertion(association);
                    if (assertion != null)
                        assertions.add(assertion);
                }
            } catch (ClassCastException cce) {
                throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Expected_Association_Object"), cce);
            }
        }
        return assertions;
    }
    
    PublisherAssertion association2PublisherAssertion(Association association)
    throws JAXRException {
        
        Concept associationType = association.getAssociationType();
        boolean isConfirmedBySourceOwner = association.isConfirmedBySourceOwner();
        boolean isConfirmedByTargetOwner = association.isConfirmedByTargetOwner();
        Organization sourceObject = null;
        Organization targetObject = null;
        
        try {
            sourceObject = (Organization)association.getSourceObject();
            targetObject = (Organization)association.getTargetObject();
        } catch (ClassCastException cce) {
            throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:The_Source_Object_and_the_Target_Object_of_the_Association_must_be_of_type_Organization."), cce);
        }
        
        PublisherAssertion assertion = null;
        assertion = objFactory.createPublisherAssertion();
        //need to check to make sure key is not null
        KeyImpl fromKey = (KeyImpl)sourceObject.getKey();
        if (fromKey != null){
            String fromKeyString = fromKey.getId();
            if (fromKeyString != null)
                assertion.setFromKey(fromKeyString);
            else throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Source_Object_key_id_needs_to_be_provided._"));
        } else throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Target_Object_Key_needs_to_be_provided._"));
        
        KeyImpl toKey = (KeyImpl)targetObject.getKey();
        if (toKey != null) {
            String toKeyString = toKey.getId();
            if (toKeyString != null)
                assertion.setToKey(toKeyString);
            else throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Source_Object_key_id_needs_to_be_provided._"));
        } else throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Target_Object_Key_needs_to_be_provided._"));
        
        if (associationType != null) {
            KeyedReference keyedRef = associationType2KeyedReference(associationType);
            if (keyedRef != null)
                assertion.setKeyedReference(keyedRef);
        } else {
            throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:Association_type_required_to_save_an_Association_in_the_Registry"));
        }
        return assertion;
    }
    
    
    
    
    
    /**
     *
     * @param
     * @return
     * @exception
     *
     */
    //is this used
    private Description getDescription(RegistryObject ro)
    throws JAXRException{
        Description description = null;
        Description dupDescription = null;
        String desc = null;
        String lang = null;
        try {
            InternationalString iString = ro.getDescription();
            if (iString != null) {
                desc = iString.getValue();
            }
        } catch (JAXRException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        if (desc == null) {
            desc = "";
        }
        description = null;
        description = objFactory.createDescription();
        dupDescription = objFactory.createDescription();
        description.setValue(desc);
        dupDescription.setValue(desc);
        
        String lng = Locale.getDefault().getLanguage();
        dupDescription.setLang(lng);
        String cty = Locale.getDefault().getCountry();
        if (cty != null && !cty.equals(""))
            lng += "-"+cty;
        description.setLang(lng);
        return description;
    }
    
    private Name getName(RegistryObject ro) throws JAXRException{
        Name name = null;
        String sname = null;
        try {
            InternationalString iString = ro.getName();
            sname = iString.getValue();
        } catch (JAXRException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        if (sname == null) {
            sname = "";
        }
        name = null;
        name = objFactory.createName();
        name.setValue(sname);
        String lng = Locale.getDefault().getLanguage();
        String cty = Locale.getDefault().getCountry();
        if (cty != null && !cty.equals(""))
            lng += "-"+cty;
        name.setLang(lng);
        return name;
    }
    
    private Collection getNames(RegistryObject ro, boolean publish) {
        
        Collection names = null;
        try {
            InternationalString iString = ro.getName();
            Collection localizedNames = iString.getLocalizedStrings();
            names = localizedStrings2Names(localizedNames, publish);
        } catch (JAXRException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        return names;
    }
    
    private Collection getDescriptions(RegistryObject ro, boolean publish) {
        
        Collection descriptions = null;
        try {
            InternationalString iString = ro.getDescription();
            Collection localizedDescriptions = iString.getLocalizedStrings();
            descriptions = localizedStrings2Descriptions(localizedDescriptions, publish);
        } catch (JAXRException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        return descriptions;
    }
    
    Collection internationalString2Names(InternationalString internationalString, boolean publish)
    throws JAXRException{
        
        Collection names = null;
        try {
            if (internationalString != null){
                Collection localizedNames = internationalString.getLocalizedStrings();
                names = localizedStrings2Names(localizedNames, publish);
            }
        } catch (JAXRException ex) {
            throw new JAXRException(ex.getMessage(), ex);
        }
        return names;
    }
    
    
    Collection localizedStrings2Names(Collection localizedStrings, boolean publish)
    throws JAXRException {
        Collection names = new ArrayList();
        if (localizedStrings != null) {
            Iterator liter = localizedStrings.iterator();
            while (liter.hasNext()) {
                LocalizedString lstring = (LocalizedString)liter.next();
                Locale locale = lstring.getLocale();
                String nameValue = lstring.getValue();
                String charset = lstring.getCharsetName();
                
                Name name = null;
                Name dupName = null;
                
                name = objFactory.createName();
                dupName = objFactory.createName();
                name.setValue(nameValue);
                dupName.setValue(nameValue);
                
                if (locale == null) locale = Locale.getDefault();
                String lang = locale.getLanguage().toLowerCase();
                
                //do the dup with just lang
                if (!publish){
                    dupName.setLang(lang);
                    names.add(dupName);
                }
                
                String country = locale.getCountry().toUpperCase();
                if (country != null && !country.equals(""))
                    lang += "-"+country;
                
                name.setLang(lang);
                names.add(name);
            }
        }
        return names;
    }
    
    Collection internationalString2Descriptions(InternationalString internationalString, boolean publish)
    throws JAXRException{
        
        Collection descriptions = null;
        try {
            if (internationalString != null){
                Collection localizedDescriptions = internationalString.getLocalizedStrings();
                descriptions = localizedStrings2Descriptions(localizedDescriptions, publish);
            }
        } catch (JAXRException ex) {
            throw new JAXRException(ex.getMessage(), ex);
        }
        return descriptions;
    }
    
    Collection localizedStrings2Descriptions(Collection localizedStrings, boolean publish)
    throws JAXRException {
        Collection descriptions = new ArrayList();
        if (localizedStrings != null) {
            Iterator liter = localizedStrings.iterator();
            while (liter.hasNext()) {
                LocalizedString lstring = (LocalizedString)liter.next();
                Locale locale = lstring.getLocale();
                String descriptionValue = lstring.getValue();
                String charset = lstring.getCharsetName();
                
                Description description = null;
                Description dupDescription = null;
                description = objFactory.createDescription();
                dupDescription = objFactory.createDescription();
                
                description.setValue(descriptionValue);
                dupDescription.setValue(descriptionValue);
                
                if (locale == null) locale = Locale.getDefault();
                String lang = locale.getLanguage().toLowerCase();
                //do the dup with just lang if
                if (!publish){
                    dupDescription.setLang(lang);
                    descriptions.add(dupDescription);
                }
                String country = locale.getCountry().toUpperCase();
                if (country != null && !country.equals(""))
                    lang += "-"+country;
                
                description.setLang(lang);
                descriptions.add(description);
            }
        }
        return descriptions;
    }
    
    Collection descriptions2LocalizedStrings(Collection descriptions)
    throws JAXRException {
        Collection localizedDescriptions = new ArrayList();
        Iterator diter = descriptions.iterator();
        while(diter.hasNext()){
            LocalizedString localizedString = new LocalizedStringImpl();
            Description description = (Description)diter.next();
            String descriptionValue = description.getValue();
            String langTag = description.getLang();
            if (descriptionValue != null)
                localizedString.setValue(descriptionValue);
            
            Locale locale = null;
            if (langTag != null) {
                int delimIndex = langTag.indexOf('-');
                if (delimIndex != -1) {
                    String lang = langTag.substring(0, delimIndex).toLowerCase();
                    String cntry = langTag.substring(delimIndex+1).toUpperCase();
                    locale = new Locale(lang, cntry);
                } else
                    locale = new Locale(langTag.toLowerCase(), "");
            } else {
                locale = Locale.getDefault();
            }
            localizedString.setLocale(locale);
            localizedDescriptions.add(localizedString);
        }
        return localizedDescriptions;
    }
    
    Collection names2LocalizedStrings(Collection names)
    throws JAXRException {
        Collection localizedNames = new ArrayList();
        Iterator niter = names.iterator();
        while (niter.hasNext()){
            Name name = (Name)niter.next();
            String nameValue = name.getValue();
            String langTag = name.getLang();
            
            LocalizedStringImpl localizedString = new LocalizedStringImpl();
            if (nameValue != null)
                localizedString.setValue(nameValue);
            Locale locale = null;
            
            if (langTag != null) {
                int delimIndex = langTag.indexOf('-');
                if (delimIndex != -1) {
                    String lang = langTag.substring(0, delimIndex).toLowerCase();
                    String cntry = langTag.substring(delimIndex+1).toUpperCase();
                    locale = new Locale(lang, cntry);
                } else
                    locale = new Locale(langTag.toLowerCase(), "");
            } else {
                locale = Locale.getDefault();
            }
            
            localizedString.setLocale(locale);
            localizedNames.add(localizedString);
        }
        return localizedNames;
    }
    
    LocalizedString name2LocalizedString(Name name)
    throws JAXRException {
        
        String nameValue = name.getValue();
        String langTag = name.getLang();
        
        LocalizedStringImpl localizedString = new LocalizedStringImpl();
        if (nameValue != null)
            localizedString.setValue(nameValue);
        Locale locale = null;
        
        if (langTag != null) {
            int delimIndex = langTag.indexOf('-');
            if (delimIndex != -1) {
                String lang = langTag.substring(0, delimIndex).toLowerCase();
                String cntry = langTag.substring(delimIndex+1).toUpperCase();
                locale = new Locale(lang, cntry);
            } else
                locale = new Locale(langTag.toLowerCase(), "");
        } else {
            locale = Locale.getDefault();
        }
        
        localizedString.setLocale(locale);
        return localizedString;
    }
    
    InternationalString name2InternationalString(Name name)
    throws JAXRException {
        InternationalStringImpl internationalString = new InternationalStringImpl();
        if (name != null) {
            LocalizedString localizedString = name2LocalizedString(name);
            if (localizedString != null) {
                internationalString = new InternationalStringImpl();
                internationalString.addLocalizedString(localizedString);
            }
        }
        return internationalString;
    }
    
    InternationalString names2InternationalString(Collection name)
    throws JAXRException {
        InternationalStringImpl internationalString = new InternationalStringImpl();
        if (name != null) {
            Collection localizedStrings = names2LocalizedStrings(name);
            if (localizedStrings != null) {
                internationalString = new InternationalStringImpl();
                internationalString.addLocalizedStrings(localizedStrings);
            }
        }
        return internationalString;
    }
    
    InternationalString descriptions2InternationalString(Collection desc)
    throws JAXRException {
        InternationalStringImpl internationalString = new InternationalStringImpl();
        if (desc != null) {
            Collection localizedStrings = descriptions2LocalizedStrings(desc);
            if (localizedStrings != null) {
                internationalString = new InternationalStringImpl();
                internationalString.addLocalizedStrings(localizedStrings);
            }
        }
        return internationalString;
    }
    
    private void mapPostalAddressAttributes(ClassificationScheme postalScheme)
    throws JAXRException {
        
        if (equivalentConcepts == null){
            initSemanticEquivalences();
            if (equivalentConcepts == null) {
                
                logger.finest("equivalent concepts are null");
                return;
            }
        }
        
        //what is the default postal scheme
        defaultPostalScheme = service.getDefaultPostalScheme();
        
        if (postalAddressMap == null)
            postalAddressMap = new HashMap();
        
        Collection jaxrChildren = null;
        Collection defaultChildren = null;
        if (jaxrPostalAddressScheme != null)
            jaxrChildren = jaxrPostalAddressScheme.getChildrenConcepts();
        
        ClassificationScheme postalAddressDefault = null;
        if (postalScheme == null) {
            if (defaultPostalScheme != null){
                defaultPostalScheme = this.getClassificationSchemeById(defaultPostalScheme.getKey().getId().trim());
                defaultChildren = defaultPostalScheme.getChildrenConcepts();
            }
        } else {
            if (postalScheme != null)
                defaultChildren = postalScheme.getChildrenConcepts();
        }
        
        
        
        Iterator jaxrIterator = jaxrChildren.iterator();
        while (jaxrIterator.hasNext()) {
            boolean match = false;
            
            logger.finest("checking to see if there are equivalent concepts for postalAddressMapping");
            Concept jaxrPostalConcept = (Concept)jaxrIterator.next();
            logger.finest("first postal key " + jaxrPostalConcept.getKey().getId());
            
            Concept jaxrEquivalentConcept = (Concept) equivalentConcepts.get(jaxrPostalConcept.getValue());
            if (jaxrEquivalentConcept == null) {
                logger.finest("no Equivalent Concept found in equivalentConcepts");
                
                postalAddressMap.put(jaxrPostalConcept.getValue(), null);
            }
            if (jaxrEquivalentConcept != null) {
                String equivConceptValue = jaxrEquivalentConcept.getValue();
                logger.finest("Equivalent Concept id " + equivConceptValue);
                
                if (defaultChildren != null) {
                    Iterator defaultIterator = defaultChildren.iterator();
                    while (defaultIterator.hasNext()) {
                        //is this a defaultPostalConcept?
                        Concept defaultPostalConcept = (Concept)defaultIterator.next();
                        String defaultValue = defaultPostalConcept.getValue();
                        logger.finest("defaultI value " + defaultValue);
                        if (defaultValue.equalsIgnoreCase(equivConceptValue)) {
                            //we've got a match
                            logger.finest("putting in postalAddressMap");
                            postalAddressMap.put(jaxrPostalConcept.getValue(), defaultPostalConcept);
                            match = true;
                        }
                    }
                } else postalAddressMap.put(jaxrPostalConcept.getValue(), null);
                
                if (!match)
                    logger.warning(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("UDDIMapper:No_match_found_for_JAXR_Postal_Address_Atribute_") + jaxrPostalConcept.getValue());
            }
            //go to next attribute
        } // done
        
    }
    
    ClassificationScheme getClassificationSchemeById(String id)
    throws JAXRException {
        if (id == null)
            return null;
        
        
        return getConceptsManager().getClassificationSchemeById(id);
    }
    
    void initPostalSchemes() throws JAXRException {
        
        initSemanticEquivalences();
        //getJAXRPostalScheme
        Collection jaxrPostalChildren = null;
        jaxrPostalAddressScheme =
                getConceptsManager().getClassificationSchemeById("PostalAddressAttributes");
        if (jaxrPostalAddressScheme == null) {
            logger.finest("Didn't find JAXR PostalAddressAttributes");
        } else {
            jaxrPostalChildren =
                    jaxrPostalAddressScheme.getChildrenConcepts();
            if (jaxrPostalChildren != null)
                logger.finest("got jaxr postal children");
        }
        
        defaultPostalScheme =
                service.getDefaultPostalScheme();
        
        Collection defaultChildren = null;
        if (defaultPostalScheme != null) {
            defaultChildren = defaultPostalScheme.getChildrenConcepts();
            if (defaultChildren != null)
                logger.finest("got default children");
        }  else logger.finest("default Postal Scheme not found");
        
    }
    
    private void initSemanticEquivalences() throws JAXRException {
        
        semanticEquivalences =
                getConnection().getSemanticEquivalences();
        if (semanticEquivalences == null) {
            logger.finest("SemanticEquivalences are null");
            return;
        }
        Collection jaxrPostalChildren = null;
        jaxrPostalAddressScheme =
                getConceptsManager().getClassificationSchemeById("PostalAddressAttributes");
        if (jaxrPostalAddressScheme == null) {
            logger.finest("Didn't find JAXR PostalAddressAttributes");
        } else {
            jaxrPostalChildren =
                    jaxrPostalAddressScheme.getChildrenConcepts();
            if (jaxrPostalChildren != null)
                logger.finest("got jaxr postal children " + jaxrPostalChildren.size());
        }
        
        ClassificationScheme defaultScheme = service.getDefaultPostalScheme();
        Collection  childConcepts = null;
        if (defaultScheme != null){
            childConcepts = defaultScheme.getChildrenConcepts();
        }
        
        //Got to iterate through the keys and Values
        Set keys = semanticEquivalences.keySet();
        Iterator keyIter = keys.iterator();
        Collection values = semanticEquivalences.values();
        Iterator valIter = values.iterator();
        while (keyIter.hasNext()) {
            
            String key = (String)keyIter.next();
            if (valIter.hasNext()){
                String val = (String)valIter.next();
            }
            
            if (key == null)
                logger.finest("key is null");
            String value = (String)semanticEquivalences.get(key);
            
            if (value == null)
                logger.finest("Value is null");
            Concept keyConcept = null;
            Concept valueConcept = null;
            if ((key != null) && (value != null)) {
                key = key.trim();
                value = value.trim();
                
                Iterator jiter = jaxrPostalChildren.iterator();
                while (jiter.hasNext()){
                    Concept jconcept = (Concept)jiter.next();
                    if (jconcept.getKey().getId().equalsIgnoreCase(key)){
                        keyConcept = jconcept;
                        break;
                    }
                    
                }
                if (keyConcept == null){
                    logger.finest("Did not find jaxr child key Equivalent Concept");
                    continue;
                }
                //findValueConcepts from defaultPostalScheme Cocepts - if no match-
                //crap out
                if (childConcepts != null){
                    Iterator citer = childConcepts.iterator();
                    while (citer.hasNext()){
                        Concept child = (Concept)citer.next();
                        Key ckey = child.getKey();
                        String cid = null;
                        if (ckey != null)
                            cid = ckey.getId();
                        if (cid != null){
                            if (cid.equalsIgnoreCase(value)){
                                valueConcept = child;
                                break;
                            }
                        }
                    }
                }
                
                
                if (valueConcept == null){
                    logger.finest("Did not find value Equivalent Concept");
                }
                if (equivalentConcepts== null)
                    equivalentConcepts = new HashMap();
                logger.finest("putting keyConcept valueConcept");
                
                equivalentConcepts.put(keyConcept.getValue(), valueConcept);
            }
        }
    }
}


