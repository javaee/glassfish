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

import javax.xml.registry.*;
import java.util.*;


/**
 * A ClassificationScheme instance represents a taxonomy that may be used to classify or categorize RegistryObject instances.
 * A very common example of a classification scheme in science is the Classification of living things where living things are categorized in under a tree like structure. Another example is the Dewey Decimal system used in libraries to categorize books and other publications. A common example in eBusiness is that of North American Industry Classification System (NAICS), which is a classification scheme used to classify businesses and services by the industry to which they belong.
 *
 * <p>Figure 1 shows how a ClassificationScheme is used by a Classification to classify a RegistryObject.
 * <p>
 * <center>
 * <img SRC="../images/classificationAndRegistryObjectAndScheme.gif" ALT="Using a ClassificationScheme to Classify an Object">
 * <br><b>Figure 1. Using a ClassificationScheme to Classify an Object</b>
 * </center>
 */
public interface ClassificationScheme extends RegistryEntry {
    /** 
	 * Adds a child Concept. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param concept	the concept being added as a child of this object 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void addChildConcept(Concept concept) throws JAXRException;

    /** 
	 * Adds a Collection of Concept children. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param concepts	the Collection of Concepts being added as a children of this object 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void addChildConcepts(Collection concepts) throws JAXRException;

    /** 
	 * Removes a child Concept. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param concept	the concept being removed as a child Concept of this object 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void removeChildConcept(Concept concept) throws JAXRException;

    /** 
	 * Removes a Collection of children Concepts. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param concepts	the Collection of Concepts being removed as children Concepts of this object 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void removeChildConcepts(Collection concepts) throws JAXRException;

    /** 
	 * Gets number of children. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the number of children Concepts
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    int getChildConceptCount() throws JAXRException;

    /** 
     * Gets all immediate children Concepts. 	 
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @see javax.xml.registry.infomodel.Concept
     * @return Collection of Concept instances. The Collection may be empty but not null.	 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    Collection getChildrenConcepts() throws JAXRException;
    
    /** 
     * Gets all descendant Concepts. 	 
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @see javax.xml.registry.infomodel.Concept
     * @return Collection of Concept instances. The Collection may be empty but not null.	 
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    Collection getDescendantConcepts() throws JAXRException;
	
    /** 
     * Determines whether this ClassificationScheme is an external ClassificationScheme
     * or an internal ClassificationScheme.   
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @return <code>true</code>if this is an external ClassificationScheme; <code>false</code> otherwise
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    boolean isExternal() throws JAXRException;
	
    /** 
     * Gets the value type for this object.
	 * The value type describes how taxonomy values are defined
	 * within the scheme.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
	 *
     * @see ClassificationScheme#VALUE_TYPE_UNIQUE
	 * @see ClassificationScheme#VALUE_TYPE_EMBEDDED_PATH
	 * @see ClassificationScheme#VALUE_TYPE_NON_UNIQUE
	 * @return an integer constant that describes the type of values supported by this ClassificationScheme
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    int getValueType() throws JAXRException;

    /** 
     * Sets the value type for this object.
     * 	 
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     * @see ClassificationScheme#VALUE_TYPE_UNIQUE
     * @see ClassificationScheme#VALUE_TYPE_EMBEDDED_PATH
     * @see ClassificationScheme#VALUE_TYPE_NON_UNIQUE
	 *
	 * @param valueType	an integer constant that describes the type of values supported by this ClassificationScheme
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void setValueType(int valueType) throws JAXRException;
	
	/** Each taxonomy value in ClassificationScheme is unique. */ 
	public static final int VALUE_TYPE_UNIQUE=0;

	/** 
	 * Each taxonomy value in ClassificationScheme embeds the full path from scheme to that Concept.
	 * This also implies that each taxonomy value is unique.
	 */ 
	public static final int VALUE_TYPE_EMBEDDED_PATH=1;

	/** 
	 * Taxonomy values in ClassificationScheme may be repeated within the same scheme.
	 * However, two Concepts that have the same parent cannot have the same value.
	 */ 
	public static final int VALUE_TYPE_NON_UNIQUE=2;
	
}
