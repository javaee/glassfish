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

import javax.xml.registry.*;

/**
 * ExtrinsicObjects provide metadata that describes submitted content whose 
 * type is not intrinsically known to the registry and therefore must be 
 * described by means of additional attributes (e.g., mime type).
 * <p>
 * Examples of content described by ExtrinsicObject include Collaboration 
 * Protocol Profiles (CPP), business process descriptions, and schemas.
 *
 * @author Farrukh S. Najmi   
 */
public interface ExtrinsicObject extends RegistryEntry {

    /**
     * Gets the mime type associated with this object. 
	 * Default is a NULL String. 
	 * 
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
	 * @return the mime type associated with this object
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    String getMimeType() throws JAXRException;

    /**
     * Sets the mime type associated with this object. 
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
	 * @param mimeType	the mime type associated with this object
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void setMimeType(String mimeType) throws JAXRException;

    /**
     * Determines whether the ExtrinsicObject is opaque (not readable) by the registry operator.
     * <p>
     * In some situations, a Submitting Organization may submit content that is encrypted and not even readable by the registry. This attribute allows the registry to know whether this is the case. 
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
	 * @return <code>true</code> if the ExtrinsicObject is readable by the registry operator; <code>false</code> otherwise
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    boolean isOpaque() throws JAXRException;

    /**
     * Sets whether the ExtrinsicObject is opaque (not readable) by the registry. 
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
     *
	 * @param isOpaque	boolean value set to <code>true</code> if the ExtrinsicObject is readable by the registry operator; <code>false</code> otherwise
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void setOpaque(boolean isOpaque) throws JAXRException;

    /** 
	 * Gets the repository item for this object.
	 * Must not return null. 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @return the DataHandler for the repository item
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    public javax.activation.DataHandler getRepositoryItem() throws JAXRException;

    /** 
	 * Sets the repository item for this object.
	 * 
	 *
	 * <p><DL><DT><B>Capability Level: 1 </B></DL> 	 
	 *
	 * @param repositoryItem	the DataHandler for the repository item. Must not be null 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    public void setRepositoryItem(javax.activation.DataHandler repositoryItem) throws JAXRException;
}
