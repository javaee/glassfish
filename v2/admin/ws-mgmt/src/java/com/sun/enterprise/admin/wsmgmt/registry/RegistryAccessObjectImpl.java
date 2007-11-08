/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * RegistryAccessObjectImpl.java
 *
 * Created on June 6, 2005, 4:16 PM
 */

package com.sun.enterprise.admin.wsmgmt.registry;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Iterator;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.List;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import javax.xml.registry.ConnectionFactory;
import com.sun.appserv.management.ext.wsmgmt.WebServiceEndpointInfo;
import com.sun.enterprise.admin.wsmgmt.WebServiceMgrBackEnd;
import com.sun.enterprise.config.ConfigException;
import com.sun.logging.LogDomains;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;
import com.sun.appserv.management.ext.wsmgmt.WebServiceEndpointInfo;

/**
 * RegistryAccessObject is based on DataAccessObject pattern. It facilitates
 * access to the registry operations. A new RegistryAccessObject is created
 * for each publishToRegistry, unpublishFromRegistry and listRegistryLocations
 * operation from asadmin.
 *
 * A RAO encapsulates connection objects to each of the listed registry locations
 * specificed by throws the jndi name of the connector connection pool.
 *
 * @author Harpreet Singh
 */
public class RegistryAccessObjectImpl implements RegistryAccessObject{
    
    private static final Logger _logger =
            Logger.getLogger(LogDomains.ADMIN_LOGGER);
    
    private WebServiceMgrBackEnd mgrBE= null;
    private Map webServiceInfoMap = null;
    
    private static final String DEFAULT_ORGANIZATION =
            "Sun Microsystems";
    private static final String DEFAULT_DESCRIPTION =
            "Default Description for Sun Java Application Server Web Service";
    
    private boolean isUDDI = false;
    private boolean isEbxml = false;
    private static int MINUS_ONE = -1;
    
    private static String JAXR_CONNECTION_FACTORY =
            "com.sun.connector.jaxr.JaxrConnectionFactory";
    private static String EBXML_CONNECTION_FACTORY =
            "com.sun.jaxr.ra.ebxml.JaxrConnectionFactory";
    /*
     * Set this to set a MockObject. There is a mock registry to publish
     * to. This is just for testing purposes. This allows to configure
     * domain xml file without having to setup a registry.
     */
    private String MOCK_REGISTRY = "com.sun.appserv.admin.wsmgmt.registry.mock";
    private boolean mockRegistry = false;
    
    /** Creates a new instance of RegistryAccessObject */
    public RegistryAccessObjectImpl() {
        mgrBE = WebServiceMgrBackEnd.getManager();
    }
    
    //<editor-fold defaultstate="collapsed" desc="Externally Visible Methods">
    /**
     * publish a web service to a set of registries
     * @param String[] list of registry-locations specified by the jndi name
     * of the connector-connection-pool. The list of names can be obtained by
     * using the listRegistryLocations method
     * @param String web service name
     * @param String load balancer host where this webservice is (optionally)
     * hosted. A null string signifies that the host is optional and is to be
     * ignored while publishing this web service
     * @param int load balancer port where this webservice is (optionally)
     * hosted. A value of -1 indicates that this is optional and is to be
     * ignored
     * @param int load balancer ssl port where this webservice is (optionally)
     * hosted. A value of -1 indicates that this is optional and is to be
     * ignored
     * @param String[] an (optional) list of categories that can qualify this
     * webservice. A null value indicates that this optional and is to be
     * ignored
     * @param String description an (optional) description. A null value
     * indicates that this is to be ignored
     * @return boolean true if published, false otherwise.
     * @todo Logging the exceptions
     */
    public boolean publish(String[] registryLocations, String webServiceName,
            String lbhost, int lbport, int lbsslport, String[] categories,
            String organization, String description, String wsdlFile) {
        
        webServiceInfoMap = getWebServiceInfoMap(webServiceName);
        if(!isWebServiceNameValid(webServiceName)){
            _logger.log(Level.SEVERE,
                    "registry.invalid_webservice_name_publish_failure",
                    webServiceName);
            return false;
        }
        ConfigHelper ch =
                ConfigHelper.getInstanceToUpdateConfig(webServiceInfoMap);
        
        String[] duplicate = ch.checkForDuplicateRegistries(registryLocations);
        if (duplicate != null) {
            // Error, we are publishing duplicate entries to the same registry.
            // Message has been logged. Throw an exception;
            StringBuffer message = new StringBuffer(
                    "Trying to publish Web Service with jndi entries pointing " +
                    "to the same " +
                    "registry. Remove jndi names that point to the same" +
                    " connector pool and retry. The" +
                    " duplicate entries are :  ");
            for (int i=0;i<duplicate.length; i++){
                message.append(duplicate[i] );
                if (i == (duplicate.length-1))
                    message.append(".");
                else
                    message.append(",");
            }
            throw new RuntimeException(message.toString());
        }
        
        String[] alreadyPublishedRegistries =
                ch.listAlreadyPublishedRegistryLocations(webServiceName,
                registryLocations);
        List<String> publishTo = new ArrayList<String>
                (Arrays.asList(registryLocations));
        if (alreadyPublishedRegistries != null)
            publishTo.removeAll(
                    new ArrayList<String> (Arrays.asList(alreadyPublishedRegistries)));
        
        if(publishTo.size() == 0) {
            String message = "Web Service " + webServiceName +
                    " already published to  the registries! Unpublish and " +
                    "republish.";
            _logger.log(Level.WARNING, message);
            throw new RuntimeException(message);
        }
        if(wsdlFile == null){
            _logger.log(Level.SEVERE,
                    "registry.wsdl_absent_publish_failure",
                    webServiceName);
            return false;
        }
        
        Map<String, String> publishedTo = new HashMap<String, String>();
        for(String registryLocation : publishTo){
            try{
                boolean isPublished = false;
                mockRegistry = Boolean.getBoolean(MOCK_REGISTRY);
                if (!mockRegistry){// mock registry is true only for testing
                    // domain xml
                    ConnectionFactory cf = lookupConnection(registryLocation);
                    if(cf != null){
                        isPublished =
                                publishWSDL(wsdlFile, cf, lbhost, lbport, lbsslport,
                                categories, organization, description,
                                webServiceName);
                    } else{
                        _logger.log(Level.WARNING,
                                "registry.registry_location_absent_publish_failure",
                                registryLocation);
                    }
                } else {
                    isPublished = true;
                }
                if(isPublished)
                    publishedTo.put(registryLocation, organization);
                
            } catch(NamingException ne){
                _logger.log(Level.SEVERE,
                        "registry.registry_location_absent_publish_failure",
                        registryLocation);
                
                _logger.log(Level.SEVERE,
                        "registry.registry_location_naming_exception_publish_failure",
                        ne);
            }
        }
        if(publishedTo.size() <= 0){
            // nothing was published
            _logger.log(Level.WARNING,
                    "registry.not_published", webServiceName);
            return false;
        }
        ch.addToConfig(webServiceName, publishedTo);
        return true;
    }
    
    /**
     * Unpublishes a web service wsdl from a list of registries
     * @param String[] list of registry-locations
     * @param String web service name whose wsdl needs to be unpublised
     * @return boolean true if unpublished successfully
     */
    public boolean unpublishFromRegistry(String[] registryLocations,
            String webServiceName) {
        
        webServiceInfoMap = getWebServiceInfoMap(webServiceName);
        if(!isWebServiceNameValid(webServiceName)){
            _logger.log(Level.SEVERE,
                    "registry.invalid_webservice_name_unpublish_failure",
                    webServiceName);
            return false;
        }
        List<String> unpublishedFrom = new ArrayList<String>();
        for(int i=0; i<registryLocations.length;i++){
            try{
                boolean unpublished = false;
                mockRegistry = Boolean.getBoolean(MOCK_REGISTRY);
                if (!mockRegistry){// true only for MockRegistry
                    ConnectionFactory cf = lookupConnection(registryLocations[i]);
                    unpublished = unpublishFromRegistry(cf, webServiceName,
                            registryLocations[i]);
                }else {
                    unpublished = true;
                }
                if(unpublished)
                    unpublishedFrom.add(registryLocations[i]);
            } catch(NamingException ne){
                _logger.log(Level.SEVERE,
                        "registry.registry_location_absent_unpublish_failure",
                        registryLocations[i]);
                
                _logger.log(Level.SEVERE,
                        "registry.registry_location_naming_exception_unpublish_failure",
                        ne);
            }
        }
        if(unpublishedFrom.size() <=0){
            _logger.log(Level.WARNING,
                    "registry.not_unpublished", webServiceName);
            return false;
        }
        ConfigHelper ch =
                ConfigHelper.getInstanceToUpdateConfig(webServiceInfoMap);
        String[] unpublished = new String[unpublishedFrom.size()];
        unpublished = unpublishedFrom.toArray(unpublished);
        ch.deleteFromConfig(webServiceName, unpublished);
        return true;
    }
    
    /**
     * List the RegistryLocations. A registry location is the jndi name of a
     * connection pool that points to a registry determined by the
     * connector connection definition of the type JAXR_REGISTRY_TYPE
     * @return String[] list of registry-location
     */
    public String[] listRegistryLocations(){
        ConfigHelper ch = ConfigHelper.getInstanceToQueryRegistryLocations();
        String[] list =  null;
        list = ch.listRegistryLocations();
        return list;
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Publish Support Methods">
    private boolean publishWSDL(String wsdlFile, ConnectionFactory cf,
            String lbhost, int lbport, int lbsslport,
            String[] categories, String organization, String description,
            String webServiceName){
        
        boolean value = false;
        try{
            
            String aURI =
                    (String)webServiceInfoMap.get(WebServiceEndpointInfo.END_POINT_URI_KEY);
            StringBuffer sbuf = null;
            StringBuffer secureSbuf = null;
            if(lbport != -1){
                sbuf = new StringBuffer();
                sbuf.append("http://"+lbhost+":"+lbport);
                if (!aURI.startsWith("/"))
                    sbuf.append("/");
                sbuf.append(aURI+"?wsdl");
                _logger.log(Level.INFO, "registry.access_url",
                        new Object[] {webServiceName, sbuf.toString()});
                
            }
            if (lbsslport != -1){
                secureSbuf = new StringBuffer();
                secureSbuf.append("https://"+lbhost+":"+lbsslport);
                if (!aURI.startsWith("/"))
                    secureSbuf.append("/");
                secureSbuf.append(aURI+"?wsdl");
                _logger.log(Level.INFO, "registry.access_url",
                        new Object[] {webServiceName, secureSbuf.toString()});
            }
            String[] accessURI;
            if ((lbport != MINUS_ONE ) && (lbsslport != MINUS_ONE)){
                accessURI = new String [2];
                accessURI[0] = sbuf.toString();
                accessURI[1] = secureSbuf.toString();
            } else {
                accessURI = new String[1];
                accessURI[0] = (sbuf!=null)?sbuf.toString():secureSbuf.toString();
            }
            
            Connection con = getConnection(cf);
            RegistryService rs = con.getRegistryService();
            BusinessLifeCycleManager blcm = rs.getBusinessLifeCycleManager();
            BusinessQueryManager bqm = rs.getBusinessQueryManager();
            
            // Create organization name and description
            // change the org type
            if (organization == null)
                organization = DEFAULT_ORGANIZATION;
            
            Organization org = blcm.createOrganization(organization);
            if(description == null)
                description = DEFAULT_DESCRIPTION;
            
            org.setName(blcm.createInternationalString(organization));
            org.setDescription(blcm.createInternationalString(description));
            
            org = createClassificationSchemeAndClassification(blcm, bqm, org,
                    webServiceName);
            org = createServiceAndServiceBindingsAndExternalLink(blcm, org,
                    description, accessURI, webServiceName);
            
            org = categorizeCategoriesViaSlots(blcm, org, webServiceName,
                    categories);
            // finally publish it.
            value = publishOrg(blcm, org);
        } catch (JAXRException je){
            _logger.log(Level.SEVERE, "registry.publish_failure_exception", je);
            throw new RuntimeException(je);
        }
        return value;
    }
    
    private boolean publishOrg(BusinessLifeCycleManager blcm,
            Organization org) throws JAXRException {
        boolean value = false;
        List<Organization> orgs = new ArrayList<Organization> ();
        orgs.add(org);
        BulkResponse response = blcm.saveOrganizations(orgs);
        if (response.getStatus() == BulkResponse.STATUS_SUCCESS) {
            _logger.log(Level.INFO, "registry.successful_publish",
                    org.getName().getValue());
            value = true;
        } else{
            _logger.log(Level.SEVERE, "registry.organization_not_published",
                    org.getName().getValue());
            
            Collection exceptions = response.getExceptions();
            displayExceptions(exceptions, Level.SEVERE);
            value = false;
        }
        return value;
    }
    /**
     * Create Classification and Classification Schemes
     * the name of the category and the value is the Web Service Name.
     * @param blcm BusinessLifeCycleManager to publish to
     * @param Organization the organization that needs the slots
     * @param webServiceName the name of the Web Service
     * @return Organization  modified Organization Object with the added slots
     * @thows JAXRException when ServiceBinding cannot be created.
     */
    
    private Organization createClassificationSchemeAndClassification(
            BusinessLifeCycleManager blcm, BusinessQueryManager bqm,
            Organization org,
            String webServiceName)
            throws JAXRException {
        try{
            ClassificationScheme classScheme = null;
            Collection findQualifiers = createFindQualifiers();
            
            classScheme = bqm.findClassificationSchemeByName(findQualifiers,
                    webServiceName);
            
            if (classScheme == null){
                classScheme = blcm.createClassificationScheme(webServiceName,
                        webServiceName);
                List<ClassificationScheme> classificationSchemeList =
                        new ArrayList<ClassificationScheme> ();
                classificationSchemeList.add(classScheme);
                if (this.isUDDI) // UDDI does not create a classification scheme
                    // in saveOrgs while ebxml creates one by default :-(
                    blcm.saveClassificationSchemes(classificationSchemeList);
            }
            // find the classification scheme
            
            // Define find qualifiers and name patterns
            if (this.isUDDI)
                classScheme = bqm.findClassificationSchemeByName(findQualifiers,
                        webServiceName);
            
            if (classScheme == null){
                _logger.log(Level.SEVERE,
                        "registr.cannot_find_classification_scheme",
                        webServiceName);
                return org;
            }
            Collection<Classification> classificationExists
                    = classScheme.getClassifications();
            Classification wsdlSpecClassification = null;
            if (!classificationExists.isEmpty()){
                for (Classification classification : classificationExists){
                    if (classification.getName().getValue().equals(webServiceName)){
                        wsdlSpecClassification = classification;
                        break;
                    }
                }
            }
            if(wsdlSpecClassification == null){
                if (this.isUDDI){
                    wsdlSpecClassification =
                            blcm.createClassification(classScheme, "wsdlSpec",
                            "wsdlSpec");
                    wsdlSpecClassification.setValue(webServiceName);
                    
                } else {
                    wsdlSpecClassification =
                            blcm.createClassification(classScheme, webServiceName,
                            webServiceName);
                    wsdlSpecClassification.setValue(webServiceName);
                    wsdlSpecClassification.setClassificationScheme(classScheme);
                    wsdlSpecClassification.setName
                            (blcm.createInternationalString(webServiceName));
                }
                classScheme.addClassification(wsdlSpecClassification);
                // have to do this again as adding classificationscheme and
                // classification at the same time bombs on UDDI.
                List<ClassificationScheme> classificationSchemeList =
                        new ArrayList<ClassificationScheme> ();
                classificationSchemeList.add(classScheme);
                if (this.isUDDI)
                    blcm.saveClassificationSchemes(classificationSchemeList);
            }
            org.addClassification(wsdlSpecClassification);
        }catch (JAXRException je){
            _logger.log(Level.SEVERE, "registry.classification_creation_failed");
            throw je;
        }
        return org;
    }
    
    
    /**
     * Categorize all listed categories into Slots. The name of the slot is
     * the name of the category and the value is the Web Service Name.
     * @param blcm BusinessLifeCycleManager to publish to
     * @param Organization the organization that needs the slots
     * @param webServiceName the name of the Web Service
     * @param categories the list of categories to categorize the object in
     * @return Organization  modified Organization Object with the added slots
     * @thows JAXRException when ServiceBinding cannot be created.
     */
    
    private Organization categorizeCategoriesViaSlots(
            BusinessLifeCycleManager blcm, Organization org,
            String webServiceName, String[] categories)
            throws JAXRException{
        try{
            // categorize categories into slots
            if (categories == null || categories.length <=0 )
                return org;
            for (int i = 0; i< categories.length; i++){
                List<String> slotValues = new ArrayList<String> ();
                slotValues.add(webServiceName);
                Slot slot = blcm.createSlot(categories[i], slotValues, null);
                org.addSlot(slot);
            }
        }catch (JAXRException je){
            _logger.log(Level.SEVERE, "registry.slot_creation_failed");
            throw je;
        }
        return org;
    }
    
    
    
    /**
     * Create a ServiceBinding Object in the registry
     * @param blcm BusinessLifeCycleManager to publish to
     * @param description for the ServiceBinding
     * @param accessURI the URI the WSDL is found
     * @param name of the service binding
     * @return ServiceBinding Object
     * @thows JAXRException when ServiceBinding cannot be created.
     */
    private ServiceBinding createServiceBinding(
            BusinessLifeCycleManager blcm, String description, String accessURI,
            String name)
            throws JAXRException{
        ServiceBinding binding = null;
        try{
            binding = blcm.createServiceBinding();
            binding.setDescription(blcm.createInternationalString(description));
            binding.setValidateURI(false);
            binding.setAccessURI(accessURI);
            binding.setName(blcm.createInternationalString(name+accessURI));
        }catch (JAXRException je){
            _logger.log(Level.SEVERE, "registry.service_binding_creation_failed");
            throw je;
        }
        return binding;
    }
    
    /**
     * Create a Service with an enclosing ServiceBinding Object in the registry
     * that points to the WSDL of the Web Service.
     * @param blcm BusinessLifeCycleManager to publish to
     * @param org Organization that the Service should be created under
     * @param description for the ServiceBinding
     * @param accessURI the URI the WSDL is found
     * @return Organization the modified Organization Object
     * @thows JAXRException when ServiceBinding cannot be created.
     */
    private Organization createServiceAndServiceBindingsAndExternalLink(
            BusinessLifeCycleManager blcm, Organization org,
            String description, String[] accessURI, String webServiceName)
            throws JAXRException{
        try{
            String serviceName = org.getName().toString() + ":" +
                    webServiceName;
            // Creating a Organization Name qualified Service.
            // This will allow web services with same names published by
            // different organization to co-exist
            Service service = blcm.createService(serviceName);
            service.setDescription(blcm.createInternationalString(description));
            for (String uri: accessURI){
                ServiceBinding binding = createServiceBinding(blcm, description,
                        uri, webServiceName );
                service.addServiceBinding(binding);
                ExternalLink wsdlLink =
                        blcm.createExternalLink( uri, description);
                wsdlLink.setValidateURI(false);
                wsdlLink.setName(blcm.createInternationalString(serviceName));
                // UDDI does not let us add external Links to services
                // JAXR should really hide us from this - but does not :-(
                if (this.isUDDI == false)
                    service.addExternalLink(wsdlLink);
                org.addExternalLink(wsdlLink);
            }
            org.addService(service);
        } catch (JAXRException je){
            _logger.log(Level.SEVERE, "registry.service_creation_failed");
            throw je;
        }
        return org;
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Unpublish Support Methods">
    private boolean unpublishFromRegistry(ConnectionFactory cf,
            String webServiceName, String registryLocation){
        
        boolean retvalue = false;
        try{
            Connection con = getConnection(cf);
            RegistryService rs = con.getRegistryService();
            BusinessLifeCycleManager blcm = rs.getBusinessLifeCycleManager();
            BusinessQueryManager bqm = rs.getBusinessQueryManager();
            // Define find qualifiers and name patterns
            Collection findQualifiers = createFindQualifiers();
            ClassificationScheme classificationScheme =
                    bqm.findClassificationSchemeByName(findQualifiers,
                    webServiceName);
            
            if (classificationScheme == null){
                _logger.log(Level.SEVERE,
                        "registry.cannot_find_classification_scheme",
                        webServiceName);
                retvalue = false;
            }
            Classification classification = null;
            if (this.isUDDI){
                classification =
                        blcm.createClassification(classificationScheme, "wsdlSpec",
                        "wsdlSpec");
                classification.setValue(webServiceName);
                
            } else {
                classification =
                        blcm.createClassification(classificationScheme,
                        webServiceName, webServiceName);
                classification.setValue(webServiceName);
            }
            classification.setName(blcm.createInternationalString(webServiceName));
            Collection<Classification> classList = null;
            if (classificationScheme != null){
                classificationScheme.addClassification(classification);
                classList = classificationScheme.getClassifications();
            } else {
                classList =  new ArrayList<Classification> ();
                classList.add(classification);
            }
            webServiceInfoMap = getWebServiceInfoMap(webServiceName);
            if(!isWebServiceNameValid(webServiceName)){
                _logger.log(Level.SEVERE,
                        "registry.invalid_webservice_name_unpublish_failure",
                        webServiceName);
                return false;
            }
            ConfigHelper ch =
                    ConfigHelper.getInstanceToUpdateConfig(webServiceInfoMap);
            String org = ch.getOrganizationName(webServiceName,
                    registryLocation);
            if (org == null){
                _logger.log(Level.SEVERE, "Cannot find Organization Name for " +
                        "web service in the appserver");
                return false;
            }
            Collection orgs = new ArrayList <String>();
            orgs.add(org);
            BulkResponse br = bqm.findOrganizations(findQualifiers,
                    (Collection) orgs,
                    (Collection) null,
                    (Collection) null,
                    (Collection)null, (Collection)null);
            
            boolean deletedOrg = true;
            if (br.getStatus() == BulkResponse.STATUS_SUCCESS){
                Collection<Organization> orgCollection = br.getCollection();
                for (Organization organization : orgCollection){
                    String orgName = organization.getName().getValue();
                    if (orgName == null)
                        continue;
                    if(!orgName.equals(org))
                        continue;
                    _logger.log(Level.INFO, "registry.about_to_unpublish",
                            new Object[] {webServiceName , orgName});
                    
                    deleteServicesAndServiceBindingsAndExternalLinks(organization,
                            webServiceName, blcm);
                    
                    organization.removeClassifications(
                            organization.getClassifications());
                    // delete organization
                    deleteOrg(blcm, organization, webServiceName);
                    break;
                }
                deleteClassificationScheme(blcm, classificationScheme,
                        webServiceName);
                retvalue = true;
            }
        } catch(JAXRException je){
            _logger.log(Level.SEVERE, "registry.unpublish_failed", je);
            retvalue = false;
        }
        return retvalue;
    }
    
    
    /**
     * Delete Service and ServiceBinding Objects from registry.
     * @param organization to delete from
     * @param webServiceName name of web service
     */
    private void deleteServicesAndServiceBindingsAndExternalLinks(
            Organization organization,
            String webServiceName, BusinessLifeCycleManager blcm)
            throws JAXRException {
        Collection<Service> servicesCollection = organization.getServices();
        // keys for all services to be deleted
        Collection<Key> servicesKey = new ArrayList<Key>();
        for (Service service : servicesCollection){
            Collection<ServiceBinding> binding = service.getServiceBindings();
            // Get all ServiceBindings for this service
            Collection<Key> bindingsKey = new ArrayList<Key>();
            for (ServiceBinding sb : binding){
                String bindingString = sb.getName().toString();
                bindingsKey.add(sb.getKey());
            }
            // delink bindings from services before deleting. Else deletion
            // is not complete
            service.removeServiceBindings(binding);
            // delete service bindings
            BulkResponse bulkResponse = blcm.deleteServiceBindings(bindingsKey);
            Collection exceptions = bulkResponse.getExceptions();
            if ( exceptions != null){
                _logger.log(Level.WARNING,
                        "registry.delete_servicebindings_failed",
                        new Object[]{webServiceName});
                displayExceptions(exceptions, Level.SEVERE);
            }
            // delink services from external links
            // UDDI does not let us add external Links to services
            // JAXR should really hide us from this - but does not :-(
            if (this.isUDDI == false)            
                service.removeExternalLinks(service.getExternalLinks());
            servicesKey.add(service.getKey());
        }
        
        // deleting ExternalLinks from the repository
        deleteExternalLinks(organization, webServiceName, blcm);
        
        // delink services from organization else deletion is incomplete
        organization.removeServices(servicesCollection);
        // delete services
        BulkResponse bulkResponse = blcm.deleteServices(servicesKey);
        Collection exceptions = bulkResponse.getExceptions();
        if ( exceptions != null){
            _logger.log(Level.SEVERE, "registry.delete_services_failed",
                    new Object[]{webServiceName});
            displayExceptions(exceptions, Level.SEVERE);
        }
        
    }
    
    private void deleteExternalLinks(Organization organization,
            String webServiceName, BusinessLifeCycleManager blcm) {
        try{
            Collection<ExternalLink> exlink = organization.getExternalLinks();
            // delinking ExternalLinks from the Organization
            Collection<Key> key = new ArrayList<Key>();
            for (ExternalLink link : exlink){
                key.add(link.getKey());
            }
            organization.removeExternalLinks(exlink);
            BulkResponse bulkResponse = blcm.deleteObjects(key);
            Collection exceptions = bulkResponse.getExceptions();
            if ( exceptions != null){
                _logger.log(Level.SEVERE, "registry.delete_externallink_failed",
                        new Object[]{webServiceName});
                displayExceptions(exceptions, Level.SEVERE);
            }
        } catch (JAXRException je){
            // ignore but log it
            _logger.log(Level.INFO, "Could not lookup ExternalLinks (URI) for" +
                    " webservice " + webServiceName + ". Nothing to delete!");
        }
    }
    private boolean deleteClassificationScheme(BusinessLifeCycleManager blcm,
            ClassificationScheme scheme,
            String webServiceName) throws JAXRException {
        
        Key key = scheme.getKey();
        Collection<Key> keysToDelete = new ArrayList<Key>();
        keysToDelete.add(key);
        
        BulkResponse bulkResponse = blcm.deleteClassificationSchemes(keysToDelete);
        Collection exceptions = bulkResponse.getExceptions();
        if ( exceptions != null){
            _logger.log(Level.SEVERE,
                    "registry.classification_scheme_delete_unsuccessful",
                    scheme.getName().getValue());
            displayExceptions(exceptions, Level.SEVERE);
            return false;
        } else {
            _logger.log(Level.INFO,
                    "registry.classification_scheme_delete_successful",
                    new Object[]{scheme.getName().getValue(), webServiceName});
            return true;
        }
    }
    
    private boolean deleteOrg(BusinessLifeCycleManager blcm, Organization org,
            String webServiceName) throws JAXRException {
        Key orgKey = org.getKey();
        Collection services = org.getServices();
        if (!services.isEmpty()){
            _logger.log(Level.SEVERE, "registry.cleanup_services_first",
                    org.getName().getValue());
            return false;
        }
        Collection keysToDelete = new ArrayList();
        keysToDelete.add(orgKey);
        BulkResponse bulkResponse = blcm.deleteOrganizations(keysToDelete);
        Collection exceptions = bulkResponse.getExceptions();
        if ( exceptions != null){
            _logger.log(Level.SEVERE, "registry.delete_org_failed",
                    org.getName().getValue());
            displayExceptions(exceptions, Level.SEVERE);
            return false;
        } else {
            _logger.log(Level.INFO, "registry.delete_org_succeeded",
                    new Object[] {org.getName().getValue(), webServiceName});
            return true;
        }
    }
    
    private boolean deleteOrg(BusinessLifeCycleManager blcm,
            String key, String webServiceName) throws JAXRException{
        Key orgKey = blcm.createKey(key);
        Collection keysToDelete = new ArrayList();
        keysToDelete.add(orgKey);
        BulkResponse bulkResponse = blcm.deleteOrganizations(keysToDelete);
        Collection exceptions = bulkResponse.getExceptions();
        if ( exceptions != null){
            _logger.log(Level.SEVERE, "registry.delete_org_failed",
                    key);
            displayExceptions(exceptions, Level.SEVERE);
            return false;
        } else {
            _logger.log(Level.INFO, "registry.delete_org_succeeded",
                    new Object[] {key, webServiceName});
            return true;
        }
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Utility Methods">
    private Connection getConnection(ConnectionFactory cf)
    throws JAXRException {
        Connection con = null;
        try{
            Method method = cf.getClass().getMethod("getConnection", (java.lang.Class[])null);
            con = (Connection)method.invoke(cf, (Object[])null);
            if (JAXR_CONNECTION_FACTORY.equals(cf.getClass().getName())){
                isUDDI = true;
            } else if (EBXML_CONNECTION_FACTORY.equals(cf.getClass().getName())){
                isEbxml = true;
            }
            if (con == null){
                con = cf.createConnection();
            }
        }catch (java.lang.NoSuchMethodException nsme) {
            con = cf.createConnection();
        } catch (java.lang.IllegalAccessException iae){
            con = cf.createConnection();
        } catch (java.lang.reflect.InvocationTargetException ite){
            con = cf.createConnection();
        }
        return con;
    }
 
    /*
     * Determines if the webservice name is valid
     * @param String webservice name
     * @return boolean true if the name is valid
     */
    private boolean isWebServiceNameValid(String webServiceName){
        if(webServiceName == null) //sanity check
            return false;
        
        boolean retValue = (webServiceInfoMap == null)?false:true;
        return retValue;
    }
    
    /**
     * Gets the map representation of WebServiceInfo
     * @param String, the name of the webservice
     * @return Map, map representation of WebServiceInfo
     */
    private Map getWebServiceInfoMap(String webServiceName){
        webServiceInfoMap =
                (webServiceName == null)? null:
                    mgrBE.getWebServiceInfoMap(webServiceName);
        return webServiceInfoMap;
    }
    
    /**
     * Looks up a connection corresponding to a registryLocation
     */
    private ConnectionFactory lookupConnection(String registryLocation)
    throws NamingException {
        
        InitialContext ic = new InitialContext();
        ConnectionFactory cf = (javax.xml.registry.ConnectionFactory)
        ic.lookup(registryLocation);
        _logger.fine("RegistryAccessObject.lookupConnection : " +
                "for registryLocation " +registryLocation);
        return cf;
    }
    
    private Collection createFindQualifiers() {
        List<String> findQualifiers = new ArrayList<String>();
        findQualifiers.add(FindQualifier.SORT_BY_NAME_DESC);
//        findQualifiers.add(FindQualifier.EXACT_NAME_MATCH);
        return findQualifiers;
    }
    
    private void displayExceptions(Collection exceptions, Level level) {
        Iterator it = exceptions.iterator();
        while (it.hasNext()){
            Exception e = (Exception) it.next();
            _logger.log(level, " JAXR Exception ", e);
        }
    }
    //</editor-fold>
//    2. create a special jaxr-ra host
}
        