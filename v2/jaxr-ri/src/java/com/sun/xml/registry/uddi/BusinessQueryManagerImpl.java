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
import com.sun.xml.registry.common.*;
import com.sun.xml.registry.common.util.*;
import com.sun.xml.registry.uddi.infomodel.*;

/**
 * This is the interface exposed by the Registry Service that implements the business style query interface. It is also
 * referred to as the focused query interface.
 * <p>
 * Many of the methods in this interface take similar arguments. These arguments are defined here in details once
 * and subsequently referred to in the method documentation:
 *
 * <p><B>findQualifiers</B> - Is a Collection of FindQualifiers. It specifies qualifiers that effect string matching, sorting,
 * and boolean predicate logic etc.
 *
 * <p><B>namePatterns</B> - Is a Collection of Strings. Each String is a partial or full
 * name pattern with wildcard searching as specified by the SQL-92 LIKE
 * specification. Unless otherwise specified in findQualifiers, this is a Logical OR and a match on any name qualifies as a match for
 * this criteria.
 *
 * <p><B>classifications</B> - Is a Collection of Classifications that classify the object.
 * This is analogous to catgegoryBag in UDDI.
 * Unless otherwise specified in findQualifiers, this is a Logical AND and match on ALL specified Classifications qualifies as a match
 * for this criteria.
 *
 * <p><B>specifications</B> - Is a Collection of RegistryObjects that
 * represent (proxy) a tecnical specification.
 * This is analogous to tModelBag in UDDI.
 * Unless otherwise specified in findQualifiers, this is a Logical AND and match on ALL specified Specifications qualifies as a match
 * for this criteria.
 *
 * <p><B>identifiers</B> - Is a Collection of Classifications that identify the object
 * using an identification scheme such as DUNS.
 * This is analogous to identifierBag in UDDI.
 * Unless otherwise specified in findQualifiers, this is a Logical AND and match on ALL specified Classifications qualifies as a match
 * for this criteria.
 *
 * <p><B>externalLinks</B> - Is a Collection of ExternalLinks that link the object to content outside
 * the registry. This is analogous to overviewDoc in UDDI.
 * Unless otherwise specified in findQualifiers, this is a Logical AND and match on ALL specified ExternalLinks qualifies as a match
 * for this criteria.
 *
 * <p><B>BulkResponse</B> - Contains Collection of objects returned by the find method.
 *
 * <p><DL><DT><B>Capability Level: 0 </B><DD>This interface is required to be implemented by all JAXR Providers.</DL>
 *
 * @see SQLQueryManager
 * @see FindQualifier
 * @see BulkResponse
 *
 * @author Farrukh S. Najmi
 */
/**
 * Class Declaration for Class1
 * @see
 * @author Kathy Walsh
 */
public class BusinessQueryManagerImpl extends QueryManagerImpl implements BusinessQueryManager {
    
    
    public BusinessQueryManagerImpl() {
        super();
    }
    
    public BusinessQueryManagerImpl(RegistryServiceImpl service) {
        super(service);
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
    public BulkResponse findOrganizations(Collection findQualifiers,
            Collection namePatterns, Collection classifications,
            Collection specifications, Collection identifiers,
            Collection externalLinks) throws JAXRException {
        if (!service.getConnection().isSynchronous()) {
            BulkResponseImpl response = new BulkResponseImpl();
            response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
            response.setRequestId(Utility.getInstance().generateUUID());
            service.storeBulkResponse(response);
            FuturesRequestManager.invokeCommand(
                    new JAXRCommand.FindOrganizationsCommand(service,
                    response, findQualifiers, namePatterns, classifications,
                    specifications, identifiers, externalLinks));
            return response;
        } else {
            return uddi.findOrganizations(findQualifiers, namePatterns,
                    classifications, specifications, identifiers,
                    externalLinks);
        }
    }
    
    /**
     * Finds all Services that match ALL of the criteria specified by the parameters of this call.
     * This is a Logical AND operation between all non-null parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     *
     * @param orgKey Key identifying an Organization. Required for UDDI providers.
     */
    public BulkResponse findServices(Key orgKey, Collection findQualifiers,
            Collection namePatterns, Collection classifications,
            Collection specifications) throws JAXRException {
        if (!service.getConnection().isSynchronous()) {
            BulkResponseImpl response = new BulkResponseImpl();
            response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
            response.setRequestId(Utility.getInstance().generateUUID());
            service.storeBulkResponse(response);
            FuturesRequestManager.invokeCommand(
                    new JAXRCommand.FindServicesCommand(service, response,
                    orgKey, findQualifiers, namePatterns, classifications,
                    specifications));
            return response;
        } else {
            return uddi.findServices(orgKey, findQualifiers, namePatterns,
                    classifications, specifications);
        }
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
    public BulkResponse findServiceBindings(Key serviceKey,
            Collection findQualifiers, Collection classifications,
            Collection specifications) throws JAXRException {
        if (!service.getConnection().isSynchronous()) {
            BulkResponseImpl response = new BulkResponseImpl();
            response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
            response.setRequestId(Utility.getInstance().generateUUID());
            service.storeBulkResponse(response);
            FuturesRequestManager.invokeCommand(
                    new JAXRCommand.FindServiceBindingsCommand(service,
                    response, serviceKey, findQualifiers, classifications,
                    specifications));
            return response;
        } else {
            return uddi.findServiceBindings(serviceKey, findQualifiers,
                    classifications, specifications);
        }
    }
    
    /**
     * Finds all ClassificationSchemes that match ALL of the criteria specified by the parameters of this call.
     * This is a Logical AND operation between all non-null parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     *
     *
     * @return BulkResponse containing Collection of ClassificationSchemes
     */
    public BulkResponse findClassificationSchemes(Collection findQualifiers,
            Collection namePatterns, Collection classifications,
            Collection externalLinks) throws JAXRException {
        if (!service.getConnection().isSynchronous()) {
            BulkResponseImpl response = new BulkResponseImpl();
            response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
            response.setRequestId(Utility.getInstance().generateUUID());
            service.storeBulkResponse(response);
            FuturesRequestManager.invokeCommand(
                    new JAXRCommand.FindClassificationSchemesCommand(service,
                    response, findQualifiers, namePatterns, classifications,
                    externalLinks));
            return response;
        } else {
            return uddi.findClassificationSchemes(
                    findQualifiers, namePatterns, classifications,
                    externalLinks);
        }
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
    public ClassificationScheme findClassificationSchemeByName(
            Collection findQualifiers, String namePattern) throws JAXRException {
        if (namePattern == null || namePattern.length() == 0) {
            return null;
        }
        return uddi.findClassificationSchemeByName(findQualifiers, namePattern);
    }
    
    /**
     * Finds all Concepts that match ALL of the criteria specified by the parameters of this call.
     * This is a Logical AND operation between all non-null parameters.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param findQualifier specifies qualifiers that effect string matching, sorting etc.
     *
     *
     * @return BulkResponse containing Collection of Concepts
     */
    public BulkResponse findConcepts(Collection findQualifiers,
            Collection namePatterns, Collection classifications,
            Collection externalIdentifiers, Collection externalLinks)
            throws JAXRException {
        if (!service.getConnection().isSynchronous()) {
            BulkResponseImpl response = new BulkResponseImpl();
            response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
            response.setRequestId(Utility.getInstance().generateUUID());
            service.storeBulkResponse(response);
            FuturesRequestManager.invokeCommand(
                    new JAXRCommand.FindConceptsCommand(service, response,
                    findQualifiers, namePatterns, classifications,
                    externalIdentifiers, externalLinks));
            return response;
        } else {
            return uddi.findConcepts(findQualifiers, namePatterns,
                    classifications, externalIdentifiers, externalLinks);
        }
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
    public Concept findConceptByPath(String path) throws JAXRException {
        
        return uddi.findConceptByPath(path);
    }
    
    /**
     * Finds all Pac
     * kages that match ALL of the criteria specified by the parameters of this call.
     * This is a Logical AND operation between all non-null parameters.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     *
     * @param findQualifier specifies qualifiers that effect string matching, sorting etc.
     *
     * @return BulkResponse containing Collection of Packages
     */
    public BulkResponse findRegistryPackages(
            Collection findQualifiers,
            Collection namePatterns,
            Collection classifications,
            Collection externalLinks
            ) throws JAXRException {
        throw new UnsupportedCapabilityException();
    }
 
    /**
     * Finds all Associations that match ALL of the criteria specified by the parameters of this call.
     * This is a Logical AND operation between all non-null parameters.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param sourceObjectId Is a String that represents the id for a RegistryObject which must be the sourceObject of the Associations that match. This parameter is ignored if specified as null.
     * @param targetObjectId Is a String that represents the id for a RegistryObject which must be the targetObject of the Associations that match. This parameter is ignored if specified as null.
     * @param associationTypes Is a Collection of associationsType. This is a logical OR operation across the collection. This parameter is ignored if specified as null.
     * @param sourceObjectConfirmed If true, provider must include Associations owned by caller that have their sourceObject confirmed. If false, provider must include Associations owned by caller that have their sourceObject not confirmed. This parameter is ignored if specified as null.
     * @param targetObjectConfirmed If true, provider must include Associations owned by caller that have their targetObject confirmed. If false, provider must include Associations owned by caller that have their targetObject not confirmed. This parameter is ignored if specified as null.
     *
     * @return BulkResponse containing Collection of Associations
     *
     */
    public BulkResponse findAssociations(Collection findQualifiers,
            String sourceObjectId, String targetObjectId,
            Collection associationTypes) throws JAXRException {
        if (!service.getConnection().isSynchronous()) {
            BulkResponseImpl response = new BulkResponseImpl();
            response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
            response.setRequestId(Utility.getInstance().generateUUID());
            service.storeBulkResponse(response);
            FuturesRequestManager.invokeCommand(
                    new JAXRCommand.FindAssociationsCommand(service,
                    response, findQualifiers, sourceObjectId, targetObjectId,
                    associationTypes));
            return response;
        } else {
            return uddi.findAssociations(findQualifiers, sourceObjectId,
                    targetObjectId, associationTypes);
        }
        
    }
    
    public BulkResponse findCallerAssociations(Collection findQualifiers,
            Boolean confirmedByCaller, Boolean confirmedByOther,
            Collection associationTypes) throws JAXRException {
        
        if (!service.getConnection().isSynchronous()) {
            BulkResponseImpl response = new BulkResponseImpl();
            response.setStatus(JAXRResponse.STATUS_UNAVAILABLE);
            response.setRequestId(Utility.getInstance().generateUUID());
            service.storeBulkResponse(response);
            FuturesRequestManager.invokeCommand(
                    new JAXRCommand.FindCallerAssociationsCommand(service,
                    response, findQualifiers, confirmedByCaller,
                    confirmedByOther, associationTypes));
            return response;
        } else {
            return uddi.findCallerAssociations(findQualifiers, confirmedByCaller,
                    confirmedByOther, associationTypes);
        }
    }
    
}

