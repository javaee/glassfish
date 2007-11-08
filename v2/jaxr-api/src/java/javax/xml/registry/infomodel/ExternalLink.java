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

package javax.xml.registry.infomodel;

import java.net.*;
import java.util.*;
import javax.xml.registry.*;

/** 
 *  
 * ExternalLink instances model a named URI to content that may reside outside  
 * the registry.  
 * RegistryObject may be associated with any number of ExternalLinks to annotate  
 * a RegistryObject with external links to external content. 
 * <p> 
 * Consider the case where a Submitting Organization submits a repository item  
 * (e.g. a DTD) and wants to associate some external content to that object 
 * (e.g. the Submitting Organization's home page). The ExternalLink enables this  
 * capability.  
 * 
 * @see RegistryObject 
 * @author Farrukh S. Najmi    
 */
public interface ExternalLink extends RegistryObject, URIValidator {    

    /** 
     * Gets the collection of RegistryObjects that are annotated by this 
     * ExternalLink.     
     *	 
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 	 
     *	 
     * @return Collection of RegistryObjects. Return an empty Collection if no RegistryObjects	 
     * are annotated by this object.	 
     * @throws JAXRException	If the JAXR provider encounters an internal error	 
     *	 
     */    
    Collection getLinkedObjects() throws JAXRException;    
     
    /** 	 
     * Gets URI to the an external resource.	 
     * Default is a NULL String. 	 
     *	 
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 	 
     *	 
     * @return	the URI String for this object (e.g. "http://java.sun.com") 	 
     * @throws JAXRException	If the JAXR provider encounters an internal error	 
     *	 
     */    
    String getExternalURI() throws JAXRException;    
     
    /** 	 
     * Sets URI for an external resource. 	 
     *	 
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 	 
     *	 
     * @param uri	the URI String for this object (e.g. "http://java.sun.com")	 
     * @throws JAXRException	If the JAXR provider encounters an internal error	 
     *	 
     */ 
    void setExternalURI(String uri) throws JAXRException;
}
