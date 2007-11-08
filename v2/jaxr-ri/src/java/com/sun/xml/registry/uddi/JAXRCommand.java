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
 * JAXRCommand.java
 *
 * Created on October 30, 2001, 11:55 AM
 */
package com.sun.xml.registry.uddi;

import com.sun.xml.registry.common.*;
import java.util.*;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

/**
 * Classes to represent JAXR methods. These are used
 * in asynchronous connections to make jaxr calls.
 *
 * @author Bobby Bissett
 */
public abstract class JAXRCommand {

    RegistryServiceImpl service;
    UDDIMapper mapper;
    BulkResponseImpl response;
    BulkResponseImpl content;
    
    /**
     * Constructor sets the service and response
     * object. Subclasses implement the business
     * part of the class.
     *
     * The constructor for each concrete subclass
     * takes a registry service and bulk response
     * parameter, followed by the parameters to the
     * actual JAXR method.
     */
    public JAXRCommand(RegistryServiceImpl service, BulkResponseImpl response)
        throws JAXRException {
            this.service = service;
            this.response = response;
            mapper = new UDDIMapper(service);
    }

    /**
     * Method overriden in each command.
     */
    abstract void execute() throws JAXRException;

    // Concrete classes below

    /**
     * Method found in QueryManager.
     */
    static class GetRegistryObjectsCommand extends JAXRCommand {
        public GetRegistryObjectsCommand(RegistryServiceImpl service,
            BulkResponseImpl response) throws JAXRException {
                super(service, response);
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.getRegistryObjects());
        }
    }

    /**
     * Method found in BusinessLifeCycleManager
     */
    static class DeleteAssociationsCommand extends JAXRCommand {
        Collection schemeKeys;
        public DeleteAssociationsCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection schemeKeys)
            throws JAXRException {
                super(service, response);
                this.schemeKeys = schemeKeys;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.deleteAssociations(schemeKeys));
        }
    }
    
    /**
     * Method found in BusinessLifeCycleManager
     */
    static class DeleteClassificationSchemesCommand extends JAXRCommand {
        Collection schemeKeys;
        public DeleteClassificationSchemesCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection schemeKeys)
            throws JAXRException {
                super(service, response);
                this.schemeKeys = schemeKeys;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.deleteConcepts(schemeKeys));
        }
    }
    
    /**
     * Method found in BusinessLifeCycleManager
     */
    static class DeleteConceptsCommand extends JAXRCommand {
        Collection conceptKeys;
        public DeleteConceptsCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection conceptKeys)
            throws JAXRException {
                super(service, response);
                this.conceptKeys = conceptKeys;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.deleteConcepts(conceptKeys));
        }
    }
    
    /**
     * Method found in LifeCycleManager
     */
    static class DeleteObjectsCommand extends JAXRCommand {
        Collection keys;
	String type;
        public DeleteObjectsCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection keys, String type) throws JAXRException {
                super(service, response);
                this.keys = keys;
		this.type = type;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.deleteObjects(keys, type));
        }
    }
    
    /**
     * Method found in BusinessLifeCycleManager
     */
    static class DeleteOrganizationsCommand extends JAXRCommand {
        Collection organizationKeys;
        public DeleteOrganizationsCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection organizationKeys)
            throws JAXRException {
                super(service, response);
                this.organizationKeys = organizationKeys;
        }
        void execute() throws JAXRException {
            response.updateResponse(
                mapper.deleteOrganizations(organizationKeys));
        }
    }
    
    /**
     * Method found in BusinessLifeCycleManager
     */
    static class DeleteServiceBindingsCommand extends JAXRCommand {
        Collection bindingKeys;
        public DeleteServiceBindingsCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection bindingKeys)
            throws JAXRException {
                super(service, response);
                this.bindingKeys = bindingKeys;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.deleteServiceBindings(bindingKeys));
        }
    }
    
    /**
     * Method found in BusinessLifeCycleManager
     */
    static class DeleteServicesCommand extends JAXRCommand {
        Collection serviceKeys;
        public DeleteServicesCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection serviceKeys)
            throws JAXRException {
                super(service, response);
                this.serviceKeys = serviceKeys;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.deleteServices(serviceKeys));
        }
    }
    
    /**
     * Method found in BusinessQueryManager
     */
    static class FindClassificationSchemesCommand extends JAXRCommand {
        Collection findQualifiers;
        Collection namePatterns;
        Collection classifications;
        Collection externalLinks;
        public FindClassificationSchemesCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection findQualifiers,
            Collection namePatterns, Collection classifications,
            Collection externalLinks) throws JAXRException {
                super(service, response);
                this.findQualifiers = findQualifiers;
                this.namePatterns = namePatterns;
                this.classifications = classifications;
                this.externalLinks = externalLinks;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.findClassificationSchemes(
	        findQualifiers, namePatterns, classifications, externalLinks));
        }
    }
    
    /**
     * Method found in BusinessQueryManager
     */
    static class FindConceptsCommand extends JAXRCommand {
        Collection findQualifiers;
        Collection namePatterns;
        Collection classifications;
        Collection identifiers;
        Collection externalLinks;
        public FindConceptsCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection findQualifiers,
            Collection namePatterns, Collection classifications,
            Collection identifiers, Collection externalLinks)
            throws JAXRException {
                super(service, response);
                this.findQualifiers = findQualifiers;
                this.namePatterns = namePatterns;
                this.classifications = classifications;
                this.identifiers = identifiers;
                this.externalLinks = externalLinks;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.findConcepts(findQualifiers,
                namePatterns, classifications, identifiers, externalLinks));
        }
    }
    
    /**
     * Method found in BusinessQueryManager
     */
    static class FindOrganizationsCommand extends JAXRCommand {
        Collection findQualifiers;
        Collection namePatterns;
        Collection classifications;
        Collection specifications;
        Collection identifiers;
        Collection externalLinks;
        public FindOrganizationsCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection findQualifiers,
            Collection namePatterns, Collection classifications,
            Collection specifications, Collection identifiers,
            Collection externalLinks) throws JAXRException {
                super(service, response);
                this.findQualifiers = findQualifiers;
                this.namePatterns = namePatterns;
                this.classifications = classifications;
                this.specifications = specifications;
                this.identifiers = identifiers;
                this.externalLinks = externalLinks;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.findOrganizations(findQualifiers,
                namePatterns, classifications, specifications, identifiers,
                externalLinks));
        }
    }

    /**
     * Method found in BusinessQueryManager
     */
    static class FindServiceBindingsCommand extends JAXRCommand {
        Key serviceKey;
        Collection findQualifiers;
        Collection classifications;
        Collection specifications;
        public FindServiceBindingsCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Key serviceKey,
            Collection findQualifiers, Collection classifications,
            Collection specifications) throws JAXRException {
                super(service, response);
                this.serviceKey = serviceKey;
                this.findQualifiers = findQualifiers;
                this.classifications = classifications;
                this.specifications = specifications;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.findServiceBindings(serviceKey,
                findQualifiers, classifications, specifications));
        }
    }
    
    /**
     * Method found in BusinessQueryManager
     */
    static class FindServicesCommand extends JAXRCommand {
        Key orgKey;
        Collection findQualifiers;
        Collection namePatterns;
        Collection classifications;
        Collection specifications;
        public FindServicesCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Key orgKey, Collection findQualifiers,
            Collection namePatterns, Collection classifications,
            Collection specifications) throws JAXRException {
                super(service, response);
                this.orgKey = orgKey;
                this.findQualifiers = findQualifiers;
                this.namePatterns = namePatterns;
                this.classifications = classifications;
                this.specifications = specifications;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.findServices(orgKey, findQualifiers,
            namePatterns, classifications, specifications));
        }
    }
    
    /**
     * Method found in BusinessQueryManager
     */
    static class FindAssociationsCommand extends JAXRCommand {
        Collection findQualifiers;
	String sourceObjectId;
	String targetObjectId;
	Collection associationTypes;
        public FindAssociationsCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection findQualifiers,
            String sourceObjectId, String targetObjectId,
            Collection associationTypes) throws JAXRException {
                super(service, response);
                this.findQualifiers = findQualifiers;
		this.sourceObjectId = sourceObjectId;
		this.targetObjectId = targetObjectId;
		this.associationTypes = associationTypes;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.findAssociations(findQualifiers,
                sourceObjectId, targetObjectId, associationTypes));
        }
    }
    
    /**
     * Method found in BusinessQueryManager
     */
    static class FindCallerAssociationsCommand extends JAXRCommand {
        Collection findQualifiers;
	Boolean confirmedByCaller;
	Boolean confirmedByOther;
	Collection associationTypes;
        public FindCallerAssociationsCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection findQualifiers,
	    Boolean confirmedByCaller, Boolean confirmedByOther,
            Collection associationTypes) throws JAXRException {
                super(service, response);
                this.findQualifiers = findQualifiers;
		this.confirmedByCaller = confirmedByCaller;
		this.confirmedByOther = confirmedByOther;
		this.associationTypes = associationTypes;
                
                
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.findCallerAssociations(findQualifiers,
                confirmedByCaller, confirmedByOther, associationTypes));
        }
    }
    
    /**
     * Method found in QueryManager.
     */
    static class GetRegistryObjectsByKeysCommand extends JAXRCommand {
        Collection objectKeys;
	String type;
        public GetRegistryObjectsByKeysCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection objectKeys, String type)
            throws JAXRException {
                super(service, response);
                this.objectKeys = objectKeys;
		this.type = type;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.getRegistryObjects(objectKeys, type));
        }
    }

    /**
     * Method found in QueryManager.
     */
    static class GetRegistryObjectsByTypeCommand extends JAXRCommand {
        String objectType;
        public GetRegistryObjectsByTypeCommand(RegistryServiceImpl service,
            BulkResponseImpl response, String objectType)
            throws JAXRException {
                super(service, response);
                this.objectType = objectType;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.getRegistryObjects(objectType));
        }
    }

    /**
     * Method found in BusinessLifeCycleManager
     */
    static class SaveAssociationsCommand extends JAXRCommand {
        Collection associations;
        boolean replace;
        public SaveAssociationsCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection associations,
            boolean replace) throws JAXRException {
                super(service, response);
                this.associations = associations;
                this.replace = replace;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.saveAssociations(associations,
                replace));
        }
    }
    
    /**
     * Method found in BusinessLifeCycleManager
     */
    static class SaveClassificationSchemesCommand extends JAXRCommand {
        Collection schemes;
        public SaveClassificationSchemesCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection schemes)
            throws JAXRException {
                super(service, response);
                this.schemes = schemes;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.saveClassificationSchemes(schemes));
        }
    }
    
    /**
     * Method found in BusinessLifeCycleManager
     */
    static class SaveConceptsCommand extends JAXRCommand {
        Collection concepts;
        public SaveConceptsCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection concepts)
            throws JAXRException {
                super(service, response);
                this.concepts = concepts;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.saveConcepts(concepts));
        }
    }
    
    /**
     * Method found in LifeCycleManager
     */
    static class SaveObjectsCommand extends JAXRCommand {
        Collection cataloguedObjects;
        public SaveObjectsCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection cataloguedObjects)
            throws JAXRException {
                super(service, response);
                this.cataloguedObjects = cataloguedObjects;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.saveObjects(cataloguedObjects));
        }
    }
    
    /**
     * Method found in BusinessLifeCycleManager
     */
    static class SaveOrganizationsCommand extends JAXRCommand {
        Collection organizations;
        public SaveOrganizationsCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection organizations)
            throws JAXRException {
                super(service, response);
                this.organizations = organizations;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.saveOrganizations(organizations));
        }
    }
    
    /**
     * Method found in BusinessLifeCycleManager
     */
    static class SaveServiceBindingsCommand extends JAXRCommand {
        Collection bindings;
        public SaveServiceBindingsCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection bindings)
            throws JAXRException {
                super(service, response);
                this.bindings = bindings;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.saveServiceBindings(bindings));
        }
    }
    
    /**
     * Method found in BusinessLifeCycleManager
     */
    static class SaveServicesCommand extends JAXRCommand {
        Collection services;
        public SaveServicesCommand(RegistryServiceImpl service,
            BulkResponseImpl response, Collection services)
            throws JAXRException {
                super(service, response);
                this.services = services;
        }
        void execute() throws JAXRException {
            response.updateResponse(mapper.saveServices(services));
        }
    }
 
}
