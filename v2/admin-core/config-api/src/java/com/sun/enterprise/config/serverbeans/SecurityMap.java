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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
 
/**
 *	This generated bean class SecurityMap matches the DTD element security-map
 *
 */

package com.sun.enterprise.config.serverbeans;

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

public class SecurityMap extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String PRINCIPAL = "Principal";
	static public final String USER_GROUP = "UserGroup";
	static public final String BACKEND_PRINCIPAL = "BackendPrincipal";

	public SecurityMap() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public SecurityMap(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("principal", PRINCIPAL, Common.SEQUENCE_OR | 
			Common.TYPE_1_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("user-group", USER_GROUP, Common.SEQUENCE_OR | 
			Common.TYPE_1_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("backend-principal", BACKEND_PRINCIPAL, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			BackendPrincipal.class);
		this.createAttribute(BACKEND_PRINCIPAL, "user-name", "UserName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(BACKEND_PRINCIPAL, "password", "Password", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is an array containing at least one element
	public void setPrincipal(String[] value) {
		this.setValue(PRINCIPAL, value);
	}

	// Getter Method
	public String[] getPrincipal() {
		return (String[])this.getValues(PRINCIPAL);
	}

	// Return the number of properties
	public int sizePrincipal() {
		return this.size(PRINCIPAL);
	}

	// Add a new element returning its index in the list
	public int addPrincipal(String value)
			throws ConfigException{
		return addPrincipal(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addPrincipal(String value, boolean overwrite)
			throws ConfigException{
		return this.addValue(PRINCIPAL, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removePrincipal(String value){
		return this.removeValue(PRINCIPAL, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removePrincipal(String value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(PRINCIPAL, value, overwrite);
	}

	// This attribute is an array containing at least one element
	public void setUserGroup(String[] value) {
		this.setValue(USER_GROUP, value);
	}

	// Getter Method
	public String[] getUserGroup() {
		return (String[])this.getValues(USER_GROUP);
	}

	// Return the number of properties
	public int sizeUserGroup() {
		return this.size(USER_GROUP);
	}

	// Add a new element returning its index in the list
	public int addUserGroup(String value)
			throws ConfigException{
		return addUserGroup(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addUserGroup(String value, boolean overwrite)
			throws ConfigException{
		return this.addValue(USER_GROUP, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeUserGroup(String value){
		return this.removeValue(USER_GROUP, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeUserGroup(String value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(USER_GROUP, value, overwrite);
	}

	// This attribute is mandatory
	public void setBackendPrincipal(BackendPrincipal value) {
		this.setValue(BACKEND_PRINCIPAL, value);
	}

	// Get Method
	public BackendPrincipal getBackendPrincipal() {
		return (BackendPrincipal)this.getValue(BACKEND_PRINCIPAL);
	}

	/**
	* Getter for Name of the Element security-map
	* @return  the Name of the Element security-map
	*/
	public String getName() {
		return getAttributeValue(ServerTags.NAME);
	}
	/**
	* Modify  the Name of the Element security-map
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.NAME, v, overwrite);
	}
	/**
	* Modify  the Name of the Element security-map
	* @param v the new value
	*/
	public void setName(String v) {
		setAttributeValue(ServerTags.NAME, v);
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public BackendPrincipal newBackendPrincipal() {
		return new BackendPrincipal();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "security-map" + (canHaveSiblings() ? "[@name='" + getAttributeValue("name") +"']" : "") ;
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
		str.append("Principal["+this.sizePrincipal()+"]");	// NOI18N
		for(int i=0; i<this.sizePrincipal(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getValue(PRINCIPAL, i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(PRINCIPAL, i, str, indent);
		}

		str.append(indent);
		str.append("UserGroup["+this.sizeUserGroup()+"]");	// NOI18N
		for(int i=0; i<this.sizeUserGroup(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getValue(USER_GROUP, i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(USER_GROUP, i, str, indent);
		}

		str.append(indent);
		str.append("BackendPrincipal");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getBackendPrincipal();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(BACKEND_PRINCIPAL, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("SecurityMap\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

