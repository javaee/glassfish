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

import java.util.*;
import java.io.Serializable;

/**
 * Implementation of SpecificationLink interface
 *
 * @author Farrukh S. Najmi
 * @author Bobby Bissett
 */
public class SpecificationLinkImpl extends RegistryObjectImpl implements SpecificationLink, Serializable {

    private Concept registryObject;
    private InternationalString usageDescription;
    private ArrayList usageParameters;
    private ServiceBinding serviceBinding;
    
    /**
     * Default constructor 
     */
    public SpecificationLinkImpl() {
	super();
	usageParameters = new ArrayList();
	usageDescription = new InternationalStringImpl();
    }
	
    /**
     * Get specification object
     */
    public RegistryObject getSpecificationObject() {
	return registryObject;
    }

    /**
     * Set specification object. For UDDI, this must be
     * a Concept.
     */
    public void setSpecificationObject(RegistryObject obj) throws JAXRException {
	if (obj instanceof Concept) {
	    registryObject = (Concept) obj;
            setIsModified(true);
	} else {
	    throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("SpecificationLinkImpl:For_UDDI_provider,_object_must_be_a_Concept"));
	}
    }

    /**
     * Gets the description of usage paramaters.
     */
    public InternationalString getUsageDescription() {
	return usageDescription;
    }

    /**
     * Sets  the description of usage paramaters.
     */
    public void setUsageDescription(InternationalString usageDescription){
	this.usageDescription = usageDescription;
        setIsModified(true);
    }
    
    /**
     * Gets any optional usage parameters. Each parameter is a String
     */
    public Collection getUsageParameters() {
	return (Collection) usageParameters.clone();
    }

    /**
     * Sets any optional usage parameters. This method checks to
     * make sure that all parameters in the collection are strings.
     * A null collection is treated as an empty collection.
     */
    public void setUsageParameters(Collection usageParameters) throws JAXRException {
	if (usageParameters == null) {
	    this.usageParameters.clear();
	} else {
            if (usageParameters.size() > 1){
                throw new InvalidRequestException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("SpecificationLinkImpl:single_usage_parameter_must_be_specified"));  
            }
 
	    Iterator iter = usageParameters.iterator();
	    while (iter.hasNext()) {
                Object object = iter.next();
		if (!(object instanceof String)) {
		    throw new JAXRException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("SpecificationLinkImpl:usage_parameters_must_be_strings"));
		}

                if (((String)object).length() > 255) {
                    throw new InvalidRequestException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("SpecificationLinkImpl:usage_parameters_size_must_be_less_than_255"));  
                }

                this.usageParameters.add (object);              
	    }
        }

        setIsModified(true);
    }

    /**
     * Get the parent ServiceBinding
     */
    public ServiceBinding getServiceBinding() {
        return serviceBinding;
    }

    /**
     * Internal method for setting the service binding
     */
    public void setServiceBinding(ServiceBinding binding) {
	serviceBinding = binding;
    }

    /**
     * Overrides behavior in RegistryObjectImpl to allow adding
     * external links. If an external link already exists, this 
     * method throws UnsupportedCapabilityException. See appendix
     * D of specification.
     */
    public void addExternalLink(ExternalLink link) throws JAXRException {
	if (externalLinks.size() > 0) {
	    throw new UnsupportedCapabilityException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("SpecificationLinkImpl:ExternalLink_already_exists,_cannot_add_more."));
	}
	if (link != null) {
	    ExternalLinkImpl externalLink = (ExternalLinkImpl) link;
	    externalLink.addLinkedObject(this);
	    externalLinks.add(externalLink);
            setIsModified(true);
	}
    }
       
    /**
     * Overrides behavior in RegistryObjectImpl to allow adding
     * external links. If an external link already exists or if
     * the collection contains more than one external link, this
     * method throws UnsupportedCapabilityException. See appendix
     * D of specification.
     */
    public void addExternalLinks(Collection links) throws JAXRException {
	if (externalLinks.size() > 0) {
	    throw new UnsupportedCapabilityException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("SpecificationLinkImpl:ExternalLink_already_exists,_cannot_add_more."));
	}
	if (links != null) {			
	    if (links.size() > 1) {
		throw new UnsupportedCapabilityException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("SpecificationLinkImpl:Cannot_add_more_than_one_ExternalLink"));
	    }
	    Iterator iter = links.iterator();
	    try {
		while (iter.hasNext()) {
		    addExternalLink((ExternalLink) iter.next());
		}	        
	    } catch (ClassCastException e) {
		throw new UnexpectedObjectException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("SpecificationLinkImpl:Objects_in_collection_must_be_ExternalLinks"), e);
	    }
	}
    }

    /**
     * Overrides behavior in RegistryObjectImpl to allow adding
     * external links. If an external link already exists or if
     * the collection contains more than one external link, this
     * method throws UnsupportedCapabilityException. See appendix
     * D of specification.
     */
    public void setExternalLinks(Collection links) throws JAXRException {
        if (links == null) {
            externalLinks.clear();
            return;
        }
	if (links.size() > 0) {
	    throw new UnsupportedCapabilityException(ResourceBundle.getBundle("com/sun/xml/registry/uddi/LocalStrings").getString("SpecificationLinkImpl:Cannot_set_more_than_one_link."));
	}
	externalLinks.clear();
	addExternalLinks(links);
    }
   
    /**
     * Override the behavior in RegistryObject to return a
     * provider generated id.
     */
    public Key getKey() throws JAXRException {
	if ((serviceBinding == null) || (serviceBinding.getKey() == null)) {
	    return null;
	}
	Service service = serviceBinding.getService();
	if ((service == null) || (service.getKey() == null)) {
	    return null;
	}
	if ((registryObject == null) || (registryObject.getKey() == null)) {
	    return null;
	}
	int sequenceId = ((ServiceBindingImpl) serviceBinding).getSequenceId(this);
	String id = service.getKey().getId() + ":" +
	    serviceBinding.getAccessURI() + ":" +
	    serviceBinding.getKey().getId() + ":" +
	    sequenceId + ":" +
	    registryObject.getKey().getId();
	
	return new KeyImpl(id);
    }

}

