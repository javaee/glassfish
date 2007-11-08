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
 *	This generated bean class MailResource matches the DTD element mail-resource
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

public class MailResource extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String DESCRIPTION = "Description";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public MailResource() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public MailResource(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("description", DESCRIPTION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
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

	/**
	* Return  the Description of the Element mail-resource
	*/
	public String getDescription() {
		return (String) getValue(ServerTags.DESCRIPTION);
	}
	/**
	* Modify  the Description of the Element mail-resource
	* @param v the new value
	*/
	public void setDescription(String v){
		setValue(ServerTags.DESCRIPTION, (null != v ? v.trim() : null));
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
			throw new ConfigException(StringManager.getManager(MailResource.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	/**
	* Getter for JndiName of the Element mail-resource
	* @return  the JndiName of the Element mail-resource
	*/
	public String getJndiName() {
		return getAttributeValue(ServerTags.JNDI_NAME);
	}
	/**
	* Modify  the JndiName of the Element mail-resource
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setJndiName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.JNDI_NAME, v, overwrite);
	}
	/**
	* Modify  the JndiName of the Element mail-resource
	* @param v the new value
	*/
	public void setJndiName(String v) {
		setAttributeValue(ServerTags.JNDI_NAME, v);
	}
	/**
	* Getter for StoreProtocol of the Element mail-resource
	* @return  the StoreProtocol of the Element mail-resource
	*/
	public String getStoreProtocol() {
		return getAttributeValue(ServerTags.STORE_PROTOCOL);
	}
	/**
	* Modify  the StoreProtocol of the Element mail-resource
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setStoreProtocol(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.STORE_PROTOCOL, v, overwrite);
	}
	/**
	* Modify  the StoreProtocol of the Element mail-resource
	* @param v the new value
	*/
	public void setStoreProtocol(String v) {
		setAttributeValue(ServerTags.STORE_PROTOCOL, v);
	}
	/**
	* Get the default value of StoreProtocol from dtd
	*/
	public static String getDefaultStoreProtocol() {
		return "imap".trim();
	}
	/**
	* Getter for StoreProtocolClass of the Element mail-resource
	* @return  the StoreProtocolClass of the Element mail-resource
	*/
	public String getStoreProtocolClass() {
		return getAttributeValue(ServerTags.STORE_PROTOCOL_CLASS);
	}
	/**
	* Modify  the StoreProtocolClass of the Element mail-resource
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setStoreProtocolClass(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.STORE_PROTOCOL_CLASS, v, overwrite);
	}
	/**
	* Modify  the StoreProtocolClass of the Element mail-resource
	* @param v the new value
	*/
	public void setStoreProtocolClass(String v) {
		setAttributeValue(ServerTags.STORE_PROTOCOL_CLASS, v);
	}
	/**
	* Get the default value of StoreProtocolClass from dtd
	*/
	public static String getDefaultStoreProtocolClass() {
		return "com.sun.mail.imap.IMAPStore".trim();
	}
	/**
	* Getter for TransportProtocol of the Element mail-resource
	* @return  the TransportProtocol of the Element mail-resource
	*/
	public String getTransportProtocol() {
		return getAttributeValue(ServerTags.TRANSPORT_PROTOCOL);
	}
	/**
	* Modify  the TransportProtocol of the Element mail-resource
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setTransportProtocol(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.TRANSPORT_PROTOCOL, v, overwrite);
	}
	/**
	* Modify  the TransportProtocol of the Element mail-resource
	* @param v the new value
	*/
	public void setTransportProtocol(String v) {
		setAttributeValue(ServerTags.TRANSPORT_PROTOCOL, v);
	}
	/**
	* Get the default value of TransportProtocol from dtd
	*/
	public static String getDefaultTransportProtocol() {
		return "smtp".trim();
	}
	/**
	* Getter for TransportProtocolClass of the Element mail-resource
	* @return  the TransportProtocolClass of the Element mail-resource
	*/
	public String getTransportProtocolClass() {
		return getAttributeValue(ServerTags.TRANSPORT_PROTOCOL_CLASS);
	}
	/**
	* Modify  the TransportProtocolClass of the Element mail-resource
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setTransportProtocolClass(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.TRANSPORT_PROTOCOL_CLASS, v, overwrite);
	}
	/**
	* Modify  the TransportProtocolClass of the Element mail-resource
	* @param v the new value
	*/
	public void setTransportProtocolClass(String v) {
		setAttributeValue(ServerTags.TRANSPORT_PROTOCOL_CLASS, v);
	}
	/**
	* Get the default value of TransportProtocolClass from dtd
	*/
	public static String getDefaultTransportProtocolClass() {
		return "com.sun.mail.smtp.SMTPTransport".trim();
	}
	/**
	* Getter for Host of the Element mail-resource
	* @return  the Host of the Element mail-resource
	*/
	public String getHost() {
		return getAttributeValue(ServerTags.HOST);
	}
	/**
	* Modify  the Host of the Element mail-resource
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setHost(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.HOST, v, overwrite);
	}
	/**
	* Modify  the Host of the Element mail-resource
	* @param v the new value
	*/
	public void setHost(String v) {
		setAttributeValue(ServerTags.HOST, v);
	}
	/**
	* Getter for User of the Element mail-resource
	* @return  the User of the Element mail-resource
	*/
	public String getUser() {
		return getAttributeValue(ServerTags.USER);
	}
	/**
	* Modify  the User of the Element mail-resource
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setUser(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.USER, v, overwrite);
	}
	/**
	* Modify  the User of the Element mail-resource
	* @param v the new value
	*/
	public void setUser(String v) {
		setAttributeValue(ServerTags.USER, v);
	}
	/**
	* Getter for From of the Element mail-resource
	* @return  the From of the Element mail-resource
	*/
	public String getFrom() {
		return getAttributeValue(ServerTags.FROM);
	}
	/**
	* Modify  the From of the Element mail-resource
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setFrom(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.FROM, v, overwrite);
	}
	/**
	* Modify  the From of the Element mail-resource
	* @param v the new value
	*/
	public void setFrom(String v) {
		setAttributeValue(ServerTags.FROM, v);
	}
	/**
	* Getter for Debug of the Element mail-resource
	* @return  the Debug of the Element mail-resource
	*/
	public boolean isDebug() {
		return toBoolean(getAttributeValue(ServerTags.DEBUG));
	}
	/**
	* Modify  the Debug of the Element mail-resource
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDebug(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DEBUG, ""+(v==true), overwrite);
	}
	/**
	* Modify  the Debug of the Element mail-resource
	* @param v the new value
	*/
	public void setDebug(boolean v) {
		setAttributeValue(ServerTags.DEBUG, ""+(v==true));
	}
	/**
	* Get the default value of Debug from dtd
	*/
	public static String getDefaultDebug() {
		return "false".trim();
	}
	/**
	* Getter for ObjectType of the Element mail-resource
	* @return  the ObjectType of the Element mail-resource
	*/
	public String getObjectType() {
		return getAttributeValue(ServerTags.OBJECT_TYPE);
	}
	/**
	* Modify  the ObjectType of the Element mail-resource
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setObjectType(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.OBJECT_TYPE, v, overwrite);
	}
	/**
	* Modify  the ObjectType of the Element mail-resource
	* @param v the new value
	*/
	public void setObjectType(String v) {
		setAttributeValue(ServerTags.OBJECT_TYPE, v);
	}
	/**
	* Get the default value of ObjectType from dtd
	*/
	public static String getDefaultObjectType() {
		return "user".trim();
	}
	/**
	* Getter for Enabled of the Element mail-resource
	* @return  the Enabled of the Element mail-resource
	*/
	public boolean isEnabled() {
		return toBoolean(getAttributeValue(ServerTags.ENABLED));
	}
	/**
	* Modify  the Enabled of the Element mail-resource
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the Enabled of the Element mail-resource
	* @param v the new value
	*/
	public void setEnabled(boolean v) {
		setAttributeValue(ServerTags.ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of Enabled from dtd
	*/
	public static String getDefaultEnabled() {
		return "true".trim();
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
	    ret = "mail-resource" + (canHaveSiblings() ? "[@jndi-name='" + getAttributeValue("jndi-name") +"']" : "") ;
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.STORE_PROTOCOL)) return "imap".trim();
		if(attr.equals(ServerTags.STORE_PROTOCOL_CLASS)) return "com.sun.mail.imap.IMAPStore".trim();
		if(attr.equals(ServerTags.TRANSPORT_PROTOCOL)) return "smtp".trim();
		if(attr.equals(ServerTags.TRANSPORT_PROTOCOL_CLASS)) return "com.sun.mail.smtp.SMTPTransport".trim();
		if(attr.equals(ServerTags.DEBUG)) return "false".trim();
		if(attr.equals(ServerTags.OBJECT_TYPE)) return "user".trim();
		if(attr.equals(ServerTags.ENABLED)) return "true".trim();
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
		str.append("Description");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDescription();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DESCRIPTION, 0, str, indent);

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
		str.append("MailResource\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

