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
 * A RegistryObject instance may be associated with zero or more RegistryObject instances. The information model defines an Association interface, an instance of which may be used to associate any two RegistryObject instances. 
 * 
 * <h2>Example of an Association</h2>
 * One example of such an association is between two ClassificationScheme instances, where one ClassificationScheme supersedes the other ClassificationScheme as shown in Figure 1. This may be the case when a new version of a ClassificationScheme is submitted. 
 * In Figure 1, we see how an Association is defined between a new version of the NAICS ClassificationScheme and an older version of the NAICS ClassificationScheme.
 * 
 * <p>
 * <center>
 * <img SRC="../images/associationInstance.gif" ALT="Example of RegistryObject Association">
 * <br><b>Figure 1. Example of RegistryObject Association</b>
 * </center>
 * <p> 
 *
 * <h2>Source and Target Objects</h2>
 * An Association instance represents an association between a source RegistryObject and a target RegistryObject. These are referred to as sourceObject and targetObject for the Association instance. It is important which object is the sourceObject and which is the targetObject as it determines the directional semantics of an Association.
 * In the example in Figure 1, it is important to make the newer version of NAICS ClassificationScheme be the sourceObject and the older version of NAICS be the targetObject because the associationType implies that the sourceObject supersedes the targetObject (and not the other way around).  
 * <h2>Association Types</h2>
 * Each Association must have an associationType attribute that identifies the type of that association. The associationType attribute is a reference to an enumeration Concept as defined by the predefined associationType ClassificationScheme in the JAXR specification. Our example uses the pre-defined associationType Concept named Supersedes.
 * 
 * <h2>Intramural Associations</h2>
 * A common use case for the Association interface is when a User "u" creates an Association "a" between two RegistryObjects "o1" and "o2" where association "a" and RegistryObjects "o1" and "o2" are objects that were created by the same User "u". This is the simplest use case where the association is between two objects that are owned by same User that is defining the Association. Such associations are referred to as intramural associations.
 * Figure 2 extends the previous example in Figure 1 for the intramural association case.
 * <p>
 * <center>
 * <img SRC="../images/associationInstanceIntramural.gif" ALT="Example of Intramural Association">
 * <br><b>Figure 2. Example of Intramural Association</b>
 * </center>
 *
 * <h2>Extramural Association</h2>
 * The information model also allows a more sophisticated use case where a User "u1" creates an Association "a" between two RegistryObjects "o1" and "o2" where association "a" is owned by User "u1", but RegistryObjects "o1" and "o2" are owned by User "u2" and User "u3" respectively.
 * In this use case the Association is being defined where either or both objects that are being associated are owned by a User different from the User defining the Association. Such associations are referred to as extramural associations. The Association interface provides a convenience method called isExtramural that returns true if the Association instance is an extramural Association.
 * Figure 3 extends the previous example in Figure 1 for the extramural association case. Note that it is possible for an extramural association to have two distinct Users rather than three distinct Users as shown in Figure 3. In such case, one of the two users owns two of the three objects involved (Association, sourceObject and targetObject).
 * <p>
 * <center>
 * <img SRC="../images/associationInstanceExtramural.gif" ALT="Example of Extramural Association">
 * <br><b>Figure 3. Example of Extramural Association</b>
 * </center>
 * <p>
 *
 * <h2>Confirmation of an Association</h2>
 * An association may need to be confirmed by the parties whose objects are involved in that Association. This section describes the semantics of confirmation of an association by the parties involved.
 * <h3>Confirmation of Intramural Associations</h3>
 * Intramural associations may be viewed as declarations of truth and do not require any explicit steps to confirm that Association as being true. In other words, intramural associations are implicitly considered be confirmed.
 * <h3>Confirmation of Extramural Associations</h3>
 * Extramural associations may be viewed as a unilateral assertion that may not be viewed as truth until it has been confirmed by the other (extramural) parties (Users "u2" and "u3" in example).  The confirmAssociation method on the BusinessLifeCycleManager interface may be called by the extramural parties that own the sourceObject or targetObject.
 * <h2>Visibility of Unconfirmed Associations</h2>
 * Extramural associations require each extramural party to confirm the assertion being made by the extramural Association before the Association is visible to 3rd parties that are not involved in the Association. This ensures that unconfirmed Associations are not visible to 3rd party registry clients.
 * <h2>Possible Confirmation States</h2>
 * Assume the most general case where there are three distinct User instances as shown in Figure 23 for an extramural Association. The extramural Association needs to be confirmed by both the other (extramural) parties (Users "u2" and "u3" in example) in order to be fully confirmed. The methods isConfirmedBySourceOwner and isConfiremedByTargetOwner in the Association interface provide access to confirmation state for both the sourceObject and targetObject. A third convenience method called isConfirmed provides a way to determine whether the Association is fully confirmed or not. So there are the following four possibilities related to confirmation state of an extramural Association:
 * <ul>
 * <li>The Association is confirmed neither by the owner of the sourceObject nor is it confirmed by owner of targetObject.</li>
 * <li>The Association is confirmed by the owner of the sourceObject but it is not confirmed by owner of targetObject.</li>
 * <li>The Association is not confirmed by the owner of the sourceObject but it is confirmed by owner of targetObject.</li>
 * <li>The Association is confirmed by the owner of the sourceObject and it is confirmed by owner of targetObject. This is the only state where the Association is fully confirmed.</li>
 * </ul>
 *
 * @see RegistryObject
 * @author Farrukh S. Najmi
 */
public interface Association extends RegistryObject {

    /** 
	 * Gets the Object that is the source of this Association. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return The RegistryObject that is the source object of this Association 
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    RegistryObject getSourceObject() throws JAXRException;

    /** 
	 * Sets the Object that is the source of this Association. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param srcObject the RegistryObject that is the source object of this Association
	 *
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void setSourceObject(RegistryObject srcObject) throws JAXRException;

    /** 
	 * Gets the Object that is the target of this Association. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return The RegistryObject that is the target object of this Association 
	 *
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    RegistryObject getTargetObject() throws JAXRException;

    /** 
	 * Sets the Object that is the target of this Association. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param targetObject the RegistryObject that is the target object of this Association
	 *
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void setTargetObject(RegistryObject targetObject) throws JAXRException;

    /** 
	 * Gets the association type for this Association. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return The association type for this Association which is a Concept in the AssociationType ClassificationScheme 
	 *
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    Concept getAssociationType() throws JAXRException;

    /** 
	 * Sets the association type for this Association. 
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @param associationType the association type for this Association which is a Concept in the AssociationType ClassificationScheme 
	 *
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
    void setAssociationType(Concept associationType) throws JAXRException;
	
	/**
	 * Determines whether an Association is extramural or not.
	 * <p>
	 * An Extramural Association must be confirmed by the User(s) that own the 
	 * source and/or targert object, if they are different from the User who creates
	 * this extramural association. Both the sourceObject and targetObject owners must
	 * confirm an extramural Association, in order for it to be visible to
	 * third parties.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return	<code>true</code> if the sourceObject and/or the targetObject are 
	 *			owned by a User that is different from the User that created the Association; 
     *			<code>false</code> otherwise
	 *
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public boolean isExtramural() throws JAXRException;
		
	/**
	 * Determines whether an Association has been confirmed by the owner of the source object.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return	<code>true</code> if the association has been confirmed by the owner of the sourceObject;
	 *			<code>false</code> otherwise. For intramural Associations always return true
	 *
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 */
	public boolean isConfirmedBySourceOwner() throws JAXRException;

	/**
	 * Determines whether an Association has been confirmed by the owner of the target object.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @return	<code>true</code> if the association has been confirmed by the owner of the targetObject;
	 *			<code>false</code> otherwise. For intramural Associations always return true
	 *
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public boolean isConfirmedByTargetOwner() throws JAXRException;

	/**
	 * Determines whether an Association has been confirmed completely.
	 * <p>
	 * An association should only be visible to third parties (not involved
	 * with the Association) if isConfirmed returns true.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * @see Association#isConfirmedBySourceOwner()
	 * @see Association#isConfirmedByTargetOwner()
	 *
	 * @return	<code>true</code> if the isConfirmedBySourceOwner and isConfirmedByTargetOwner methods both return true;
	 *			<code>false</code> otherwise. For intramural Associations always return true
	 *
	 * @throws JAXRException	If the JAXR provider encounters an internal error
	 *
	 */
	public boolean isConfirmed() throws JAXRException;


	/**
	 * Returns true if the sourceObject is owned by the caller.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * <p>
	 */
	//public boolean callerIsSourceOwner() throws JAXRException;

	/**
	 * Returns true if the targetObject is owned by the caller.
	 *
	 * <p><DL><DT><B>Capability Level: 0 </B></DL> 	 
	 *
	 * <p>
	 */
	//public boolean CallerIsTargetOwner() throws JAXRException;

}
