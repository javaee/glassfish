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

/**
 * The Classification interface is used to classify RegistryObject instances. A RegistryObject may be classified along multiple dimensions by adding zero or more Classification instances to the RegistryObject. For example, an Organization may be classified by its industry, by the products it sells and by its geographical location. In this example the RegistryObject would have at least three Classification instances added to it.
 * The RegistryObject interface provides several addClassification methods to allow a client to add Classification instances to a Registry Object.
 *
 * 
 * <p>Figure 1 shows how a Classification classifies a RegistryObject using a ClassificationScheme.
 * <p>
 * <center>
 * <img SRC="../images/classificationAndRegistryObjectAndScheme.gif" ALT="Using a ClassificationScheme to Classify an Object">
 * <br><b>Figure 1. Using a ClassificationScheme to Classify an Object</b>
 * </center>
 * <p>
 *
 * <h2>Internal vs. External Taxonomies</h2>
 * A taxonomy may be represented within a JAXR provider in one of the following ways:
 *
 * <ol style='margin-top:0in' start=1 type=a>
 * <li>The taxonomy elements and their structural relationship with each other
 * are available within the JAXR provider. This case is referred to as <i>Internal
 * Taxonomy</i> since the structure of the taxonomy is available internal to the
 * JAXR provider.</li>
 *
 * <li>The taxonomy elements and their structural relationship with each other is
 * represented somewhere external to the JAXR provider. This case is
 * referred to as <i>External Taxonomy</i> since the structure of the
 * taxonomy is not available to the JAXR provider.</li>
 * </ol>
 *
 * <h2>Internal vs. External Classifications</h2>
 * The Classification interface allows the classification of
 * RegistryObjects using a ClassificationScheme whether the ClassificationScheme
 * represents an internal taxonomy or an external taxonomy. When a Classification
 * instance uses a ClassificationScheme representing an internal taxonomy then it
 * is referred to as an internal Classification. When a Classification instance
 * uses a ClassificationScheme representing an external taxonomy then it is
 * referred to as an external Classification.
 *
 * <h2>Internal Classification</h2>
 * 
 * <p>When a Classification instance is used to classify a
 * RegistryObject using an internal taxonomy it is referred to as an internal
 * Classification. A client must call the <span style='font-family:"Courier New"'>setConcept</span>
 * method on a Classification and define a reference to a Concept instance from
 * the Classification instance in order for that Classification to use an internal
 * taxonomy. It is not necessary for the client to call setClassificationScheme
 * for internal Classifications since the classifying Concept already knows it
 * root ClassificationScheme.</p>
 *
 * <h3>Example of Internal Classification</h3>
 * Figure 2 shows an example of internal classification using a Concept to represent a taxonomy element. The example classifies an Organization instance as a Book Publisher using the NAICS standard taxonomy available as an internal taxonomy. 
 * Note that the figure does not show all the Concepts between the Book Publishers node and the NAICS ClassificationScheme to save space. Had they been there they would have been linked together by the parent attribute of each Concept.
 * <p>
 * <center><img SRC="../images/internalClassificationInstance.gif" ALT="Example of Internal Classification">
 * <br><b>Figure 2. Example of Internal Classification</b>
 * </center>
 *
 * <h2>External Classification</h2>
 * When a Classification instance is used to classify a RegistryObject using an external taxonomy it is referred to as an external Classification. A client must call the setValue method on a Classification and define a unique value that logically represents a taxonomy element within the taxonomy whose structure is defined externally. It is necessary for the client call setClassificationScheme for external Classifications since there is no other way to infer the ClassificationScheme that represents the external taxonomy.
 *
 * <h3>Example of External Classification</h3>
 * Figure 3 shows an example of external classification. The example uses the same scenario where a Classification classifies an Organization instance as a Book Publisher using the NAICS standard taxonomy. However, this time the structure of the NAICS taxonomy is not available internally to the JAXR provider and consequently there is no Concept instance. Instead, the name and value attributes of the Classification are used to pinpoint the Book Publisher's taxonomy element. Note that name is optional but value is required.
 * <p>
 * <center><img SRC="../images/externalClassificationInstance.gif" ALT="Example of External Classification">
 * <br><b>Figure 3. Example of External Classification</b>
 * </center>
 *
 * <h2>An Example of Multiple Classifications</h2>
 * The next example shows how a RegistryObject may be classified by multiple classification schemes. In this example, two internal ClassificationSchemes named Industry and Geography are used to classify several Organization RegistryObjects by their industry and Geography. 
 * In Figure 4, in order to save space and improve readability, the Classification instances are not explicitly shown but are implied as associations between the RegistryObjects (shaded leaf node) and the associated Concepts.
 * <p>
 * <center><img SRC="../images/classificationTree.gif" ALT="Example of Multiple Classifications">
 * <br><b>Figure 4. Example of Multiple Classifications</b>
 * </center>
 *
 * @see RegistryObject
 * @see Concept
 * @author Farrukh S. Najmi
 */
public interface Classification extends RegistryObject {
    /**
     * Gets the Concept that is classifying the object.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @return the Concept that is classifying the classified object. null if this is a external Classification
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     * @supplierCardinality 0..*
     * @clientCardinality 0..*
     * @associationAsClass Classification
     */
    Concept getConcept() throws JAXRException;

	/** 
	 * Sets the concept for this internal classification. 
	 * <p>
	 * This method should be used mutually exclusively with
	 * the setClassificationScheme method. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param concept	the Concept that is classifying the classified object.
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void setConcept(Concept concept) throws JAXRException;


    /**
     * Gets the ClassificationScheme that is used in classifying the object.
	 * If the Classification is an internal Classification then this method
	 * should return the value returned by calling the getClassificationScheme
	 * method on the Concept representing the taxonomy element.
	 *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
	 * @return the ClassificationScheme used by this Classification
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     * @associates <{ClassificationScheme}>
     * @supplierCardinality 0..1
     * @label classificationScheme
     * @see ClassificationScheme
     */
    ClassificationScheme getClassificationScheme() throws JAXRException;

    /** 
     * Sets the ClassificationScheme for this external classification.
	 * <p>
	 * If this method is called then you must also call setValue method.
	 * This method should be used mutually exclusively with
	 * the setConcept method. 
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
     *
     * @param classificationScheme	the ClassificationScheme used by this Classification
     * @throws JAXRException	If the JAXR provider encounters an internal error
     *
     */
    void setClassificationScheme(ClassificationScheme classificationScheme) throws JAXRException;

    /** 
	 * Gets the taxonomy value for this Classification. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return	the value of the taxonomy element if external Classification; the value of the
	 * 			Concept representing the taxonomy element if internal Classification 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    public String getValue() throws JAXRException;
    
    /** 
	 * Sets the taxonomy value for this external Classification.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param value	the taxonomy value used by this external Classification
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    public void setValue(String value) throws JAXRException;


    /** 
	 * Gets the Object that is being classified. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return the RegistryObject that is classified by this Classification
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    RegistryObject getClassifiedObject() throws JAXRException;

    /** 
	 * Sets the object that is being classified. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param classifiedObject	the RegistryObject that is classified by this Classification 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void setClassifiedObject(RegistryObject classifiedObject) throws JAXRException;

	/** 
	 * Returns true if this is an external classification. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return <code>true</code> if this is an external Classification; <code>false</code> otherwise
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	boolean isExternal() throws JAXRException;
}
