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
 
/**
 *	This generated bean class ClientCredential matches the DTD element client-credential
 *
 */

package com.sun.enterprise.config.clientbeans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.Serializable;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.StaleWriteConfigException;
import com.sun.enterprise.util.i18n.StringManager;

// BEGIN_NOI18N

public class ClientCredential extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public ClientCredential() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ClientCredential(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(1);
		this.createProperty("property", ELEMENT_PROPERTY, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ElementProperty.class);
		this.createAttribute(ELEMENT_PROPERTY, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(ELEMENT_PROPERTY, "value", "Value", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is an array, possibly empty
	public void setElementProperty(int index, ElementProperty value) {
		this.setValue(ELEMENT_PROPERTY, index, value);
	}

	// Get Method
	public ElementProperty getElementProperty(int index) {
		return (ElementProperty)this.getValue(ELEMENT_PROPERTY, index);
	}

	// This attribute is an array, possibly empty
	public void setElementProperty(ElementProperty[] value) {
		this.setValue(ELEMENT_PROPERTY, value);
	}

	// Getter Method
	public ElementProperty[] getElementProperty() {
		return (ElementProperty[])this.getValues(ELEMENT_PROPERTY);
	}

	// Return the number of properties
	public int sizeElementProperty() {
		return this.size(ELEMENT_PROPERTY);
	}

	// Add a new element returning its index in the list
	public int addElementProperty(ElementProperty value)
			throws ConfigException{
		return addElementProperty(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addElementProperty(ElementProperty value, boolean overwrite)
			throws ConfigException{
		ElementProperty old = getElementPropertyByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(ClientCredential.class).getString("cannotAddDuplicate",  "ElementProperty"));
		}
		return this.addValue(ELEMENT_PROPERTY, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeElementProperty(ElementProperty value){
		return this.removeValue(ELEMENT_PROPERTY, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeElementProperty(ElementProperty value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(ELEMENT_PROPERTY, value, overwrite);
	}

	public ElementProperty getElementPropertyByName(String id) {
	 if (null != id) { id = id.trim(); }
	ElementProperty[] o = getElementProperty();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ClientTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	/**
	* Getter for UserName of the Element client-credential
	* @return  the UserName of the Element client-credential
	*/
	public String getUserName() {
		return getAttributeValue(ClientTags.USER_NAME);
	}
	/**
	* Modify  the UserName of the Element client-credential
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setUserName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ClientTags.USER_NAME, v, overwrite);
	}
	/**
	* Modify  the UserName of the Element client-credential
	* @param v the new value
	*/
	public void setUserName(String v) {
		setAttributeValue(ClientTags.USER_NAME, v);
	}
	/**
	* Getter for Password of the Element client-credential
	* @return  the Password of the Element client-credential
	*/
	public String getPassword() {
		return getAttributeValue(ClientTags.PASSWORD);
	}
	/**
	* Modify  the Password of the Element client-credential
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setPassword(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ClientTags.PASSWORD, v, overwrite);
	}
	/**
	* Modify  the Password of the Element client-credential
	* @param v the new value
	*/
	public void setPassword(String v) {
		setAttributeValue(ClientTags.PASSWORD, v);
	}
	/**
	* Getter for Realm of the Element client-credential
	* @return  the Realm of the Element client-credential
	*/
	public String getRealm() {
			return getAttributeValue(ClientTags.REALM);
	}
	/**
	* Modify  the Realm of the Element client-credential
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setRealm(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ClientTags.REALM, v, overwrite);
	}
	/**
	* Modify  the Realm of the Element client-credential
	* @param v the new value
	*/
	public void setRealm(String v) {
		setAttributeValue(ClientTags.REALM, v);
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ElementProperty newElementProperty() {
		return new ElementProperty();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "client-credential";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
	return null;
	}
	//
	public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.add(c);
	}

	//
	public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.remove(c);
	}
	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("ElementProperty["+this.sizeElementProperty()+"]");	// NOI18N
		for(int i=0; i<this.sizeElementProperty(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getElementProperty(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(ELEMENT_PROPERTY, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ClientCredential\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

