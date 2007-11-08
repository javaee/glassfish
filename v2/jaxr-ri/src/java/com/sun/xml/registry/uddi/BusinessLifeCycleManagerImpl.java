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
import javax.xml.registry.*;
import java.util.Collection;

/**
 * Class Declaration for Class1
 * @see
 * @author 
 */
public class BusinessLifeCycleManagerImpl extends LifeCycleManagerImpl 
	implements BusinessLifeCycleManager {

    public BusinessLifeCycleManagerImpl() {
        super();
    }
    
    public BusinessLifeCycleManagerImpl(RegistryServiceImpl service) {
        super(service);
    }
    
    /** 
	 * Adds or updates (replaces) specified Organizations.
	 * Partial commits are allowed. Processing stops on first SaveException encountered.
     *
	 * @return BulkResponse containing the Collection of keys for those objects that were 
	 * saved successfully and any SaveException that was encountered in case of partial commit.
	 *
     */
    public BulkResponse saveOrganizations(Collection organizations) 
	throws JAXRException{
            if (!service.getConnection().isSynchronous()) {
                BulkResponseImpl response = new BulkResponseImpl();
                response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
                response.setRequestId(Utility.getInstance().generateUUID());
                service.storeBulkResponse(response);
                FuturesRequestManager.invokeCommand(
                    new JAXRCommand.SaveOrganizationsCommand(service, response,
                        organizations));
                return response;
            } else {
                return uddi.saveOrganizations(organizations);
            }
    }

    /** 
     * Adds or updates (replaces) specified Services.
 	* Partial commits are allowed. Processing stops on first SaveException encountered.
 	* 
     * @return BulkResponse containing the Collection of keys for those objects that were 
     * saved successfully and any SaveException that was encountered in case of partial commit.
     */
    public BulkResponse saveServices(Collection services) throws JAXRException{
        if (!service.getConnection().isSynchronous()) {
            BulkResponseImpl response = new BulkResponseImpl();
            response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
            response.setRequestId(Utility.getInstance().generateUUID());
            service.storeBulkResponse(response);
            FuturesRequestManager.invokeCommand(
                new JAXRCommand.SaveServicesCommand(service, response,
                    services));
            return response;
        } else {
            return uddi.saveServices(services);
        }
    }

    /** 
     * Adds or updates (replaces) specified ServiceInterfaceBindings. 
     * Partial commits are allowed. Processing stops on first SaveException encountered.
     *
     * @return BulkResponse containing the Collection of keys for those objects that were 
     * saved successfully and any SaveException that was encountered in case of partial commit.
     */
    public BulkResponse saveServiceBindings(Collection bindings) throws JAXRException{
        if (!service.getConnection().isSynchronous()) {
            BulkResponseImpl response = new BulkResponseImpl();
            response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
            response.setRequestId(Utility.getInstance().generateUUID());
            service.storeBulkResponse(response);
            FuturesRequestManager.invokeCommand(
                new JAXRCommand.SaveServiceBindingsCommand(service, response,
                    bindings));
            return response;
        } else {
            return uddi.saveServiceBindings(bindings);
        }
    }

    /** 
     * Adds or updates (replaces) specified Concepts. 
     * Partial commits are allowed. Processing stops on first SaveException encountered.
     *
     * @return BulkResponse containing the Collection of keys for those objects that were 
     * saved successfully and any SaveException that was encountered in case of partial commit.
     */
    public BulkResponse saveConcepts(Collection concepts) throws JAXRException{
        if (!service.getConnection().isSynchronous()) {
            BulkResponseImpl response = new BulkResponseImpl();
            response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
            response.setRequestId(Utility.getInstance().generateUUID());
            service.storeBulkResponse(response);
            FuturesRequestManager.invokeCommand(
                new JAXRCommand.SaveConceptsCommand(service, response,
                    concepts));
            return response;
        } else {
            return uddi.saveConcepts(concepts);
        }
    }
    
    /** 
     * Saves specified ClassificationScheme instances.
     * If the object is not in the registry, then it is created in the registry.
     * If it already exists in the registry and has been modified, then its
     * state is updated (replaced) in the registry. 
     *
     * Partial commits are allowed. Processing stops on first SaveException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @return BulkResponse containing the Collection of keys for those objects that were 
     * saved successfully and any SaveException that was encountered in case of partial commit.
     */
    public BulkResponse saveClassificationSchemes(Collection schemes) throws JAXRException {
        if (!service.getConnection().isSynchronous()) {
            BulkResponseImpl response = new BulkResponseImpl();
            response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
            response.setRequestId(Utility.getInstance().generateUUID());
            service.storeBulkResponse(response);
            FuturesRequestManager.invokeCommand(
                new JAXRCommand.SaveClassificationSchemesCommand(service,
                    response, schemes));
            return response;
        } else {
            return uddi.saveClassificationSchemes(schemes);
        }
    }
    
    /**
     * Delete the organizations corresponding to specified Keys.
     * Partial commits are allowed. Processing stops on first DeleteException encountered.
     *
     * @return BulkResponse containing the Collection of keys for those objects that were 
     * deleted successfully and any DeleteException that was encountered in case of partial commit.
     */
    public BulkResponse deleteOrganizations(Collection organizationKeys) throws JAXRException{
        if (!service.getConnection().isSynchronous()) {
            BulkResponseImpl response = new BulkResponseImpl();
            response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
            response.setRequestId(Utility.getInstance().generateUUID());
            service.storeBulkResponse(response);
            FuturesRequestManager.invokeCommand(
                new JAXRCommand.DeleteOrganizationsCommand(service, response,
                    organizationKeys));
            return response;
        } else {
            return uddi.deleteOrganizations(organizationKeys);
        }
    }

    /**
     * Delete the services corresponding to specified Keys.
     * Partial commits are allowed. Processing stops on first DeleteException encountered.
     *
     * @return BulkResponse containing the Collection of keys for those objects that were 
     * deleted successfully and any DeleteException that was encountered in case of partial commit.
     */
    public BulkResponse deleteServices(Collection serviceKeys) throws JAXRException{
        if (!service.getConnection().isSynchronous()) {
            BulkResponseImpl response = new BulkResponseImpl();
            response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
            response.setRequestId(Utility.getInstance().generateUUID());
            service.storeBulkResponse(response);
            FuturesRequestManager.invokeCommand(
                new JAXRCommand.DeleteServicesCommand(service, response,
                    serviceKeys));
            return response;
        } else {
            return uddi.deleteServices(serviceKeys);
        }
    }

    /**
     * Delete the ServiceInterfaceBindings corresponding to specified Keys.
     * Partial commits are allowed. Processing stops on first DeleteException encountered.
     *
     * @return BulkResponse containing the Collection of keys for those objects that were 
     * deleted successfully and any DeleteException that was encountered in case of partial commit.
     */
    public BulkResponse deleteServiceBindings(Collection interfaceKeys) throws JAXRException{
        if (!service.getConnection().isSynchronous()) {
            BulkResponseImpl response = new BulkResponseImpl();
            response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
            response.setRequestId(Utility.getInstance().generateUUID());
            service.storeBulkResponse(response);
            FuturesRequestManager.invokeCommand(
                new JAXRCommand.DeleteServiceBindingsCommand(service, response,
                    interfaceKeys));
            return response;
        } else {
            return uddi.deleteServiceBindings(interfaceKeys);
        }
    }

    /**
     * Delete the Concepts corresponding to specified Keys.
     * Partial commits are allowed. Processing stops on first DeleteException encountered.
     *
     * @return BulkResponse containing the Collection of keys for those objects that were 
     * deleted successfully and any DeleteException that was encountered in case of partial commit.
     */
    public BulkResponse deleteConcepts(Collection conceptKeys) throws JAXRException{       
        if (!service.getConnection().isSynchronous()) {
            BulkResponseImpl response = new BulkResponseImpl();
            response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
            response.setRequestId(Utility.getInstance().generateUUID());
            service.storeBulkResponse(response);
            FuturesRequestManager.invokeCommand(
                new JAXRCommand.DeleteConceptsCommand(service, response,
                    conceptKeys));
            return response;
        } else {
            return uddi.deleteConcepts(conceptKeys);
        }
    }
	
	/**
     * Delete the ClassificationSchemes corresponding to the specified Keys.
     * Partial commits are allowed. Processing stops on first DeleteException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @return BulkResponse containing the Collection of keys for those objects that were 
     * deleted successfully and any DeleteException that was encountered in case of partial commit.
     */
    public BulkResponse deleteClassificationSchemes(Collection schemeKeys)
        throws JAXRException {
	    if (!service.getConnection().isSynchronous()) {
		BulkResponseImpl response = new BulkResponseImpl();
		response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
                response.setRequestId(Utility.getInstance().generateUUID());
                service.storeBulkResponse(response);
		FuturesRequestManager.invokeCommand(
                    new JAXRCommand.DeleteClassificationSchemesCommand(service,
                        response, schemeKeys));
		return response;
	    } else {
		return uddi.deleteConcepts(schemeKeys);
            }
    }
	
    /** 
     * Saves specified Association instances.
     * If the object is not in the registry, then it is created in the registry.
     * If it already exists in the registry and has been modified, then its
     * state is updated (replaced) in the registry. 
     *
     * Partial commits are allowed. Processing stops on first SaveException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param replace If set to true then the specified associations replace any existing associations owned by the caller. If set to false specif8ied associations are saved while preserving any existing associations that are not being updated by this call.
     * @return BulkResponse containing the Collection of keys for those objects that were 
     * saved successfully and any SaveException that was encountered in case of partial commit.
     */
    public BulkResponse saveAssociations(Collection associations,
        boolean replace) throws JAXRException {
	    if (!service.getConnection().isSynchronous()) {
		BulkResponseImpl response = new BulkResponseImpl();
		response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
                response.setRequestId(Utility.getInstance().generateUUID());
                service.storeBulkResponse(response);
		FuturesRequestManager.invokeCommand(
                    new JAXRCommand.SaveAssociationsCommand(service, response,
                        associations, replace));
		return response;
	    } else {
		return uddi.saveAssociations(associations, replace);
            }
	}
	
	
    /**
     * Delete the Associations corresponding to the specified Keys.
     * Partial commits are allowed. Processing stops on first DeleteException encountered.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @return BulkResponse containing the Collection of keys for those objects that were 
     * deleted successfully and any DeleteException that was encountered in case of partial commit.
     */
    public BulkResponse deleteAssociations(Collection schemeKeys)
        throws JAXRException {
	    if (!service.getConnection().isSynchronous()) {
		BulkResponseImpl response = new BulkResponseImpl();
		response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
                response.setRequestId(Utility.getInstance().generateUUID());
                service.storeBulkResponse(response);
		FuturesRequestManager.invokeCommand(
                    new JAXRCommand.DeleteAssociationsCommand(service, response,
                        schemeKeys));
		return response;
	    } else {
		return uddi.deleteAssociations(schemeKeys);
            }
    }
    
    //must be associations
    public void confirmAssociation(javax.xml.registry.infomodel.Association association) 
    throws JAXRException {   
        uddi.confirmAssociation(association);
    }
    
    
    public void  unConfirmAssociation(javax.xml.registry.infomodel.Association association) 
    throws JAXRException {        
        uddi.unConfirmAssociation(association);
    }
	
}
