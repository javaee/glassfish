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
 *	This generated bean class JmxConnector matches the DTD element jmx-connector
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

public class JmxConnector extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String SSL = "Ssl";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public JmxConnector() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public JmxConnector(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("ssl", SSL, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Ssl.class);
		this.createAttribute(SSL, "cert-nickname", "CertNickname", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(SSL, "ssl2-enabled", "Ssl2Enabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(SSL, "ssl2-ciphers", "Ssl2Ciphers", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(SSL, "ssl3-enabled", "Ssl3Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(SSL, "ssl3-tls-ciphers", "Ssl3TlsCiphers", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(SSL, "tls-enabled", "TlsEnabled", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(SSL, "tls-rollback-enabled", "TlsRollbackEnabled", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(SSL, "client-auth-enabled", "ClientAuthEnabled", 
						AttrProp.CDATA,
						null, "false");
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

	// This attribute is optional
	public void setSsl(Ssl value) {
		this.setValue(SSL, value);
	}

	// Get Method
	public Ssl getSsl() {
		return (Ssl)this.getValue(SSL);
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
			throw new ConfigException(StringManager.getManager(JmxConnector.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	* Getter for Name of the Element jmx-connector
	* @return  the Name of the Element jmx-connector
	*/
	public String getName() {
		return getAttributeValue(ServerTags.NAME);
	}
	/**
	* Modify  the Name of the Element jmx-connector
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.NAME, v, overwrite);
	}
	/**
	* Modify  the Name of the Element jmx-connector
	* @param v the new value
	*/
	public void setName(String v) {
		setAttributeValue(ServerTags.NAME, v);
	}
	/**
	* Getter for Enabled of the Element jmx-connector
	* @return  the Enabled of the Element jmx-connector
	*/
	public boolean isEnabled() {
		return toBoolean(getAttributeValue(ServerTags.ENABLED));
	}
	/**
	* Modify  the Enabled of the Element jmx-connector
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the Enabled of the Element jmx-connector
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
	* Getter for Protocol of the Element jmx-connector
	* @return  the Protocol of the Element jmx-connector
	*/
	public String getProtocol() {
		return getAttributeValue(ServerTags.PROTOCOL);
	}
	/**
	* Modify  the Protocol of the Element jmx-connector
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setProtocol(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.PROTOCOL, v, overwrite);
	}
	/**
	* Modify  the Protocol of the Element jmx-connector
	* @param v the new value
	*/
	public void setProtocol(String v) {
		setAttributeValue(ServerTags.PROTOCOL, v);
	}
	/**
	* Get the default value of Protocol from dtd
	*/
	public static String getDefaultProtocol() {
		return "rmi_jrmp".trim();
	}
	/**
	* Getter for Address of the Element jmx-connector
	* @return  the Address of the Element jmx-connector
	*/
	public String getAddress() {
		return getAttributeValue(ServerTags.ADDRESS);
	}
	/**
	* Modify  the Address of the Element jmx-connector
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAddress(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ADDRESS, v, overwrite);
	}
	/**
	* Modify  the Address of the Element jmx-connector
	* @param v the new value
	*/
	public void setAddress(String v) {
		setAttributeValue(ServerTags.ADDRESS, v);
	}
	/**
	* Getter for Port of the Element jmx-connector
	* @return  the Port of the Element jmx-connector
	*/
	public String getPort() {
		return getAttributeValue(ServerTags.PORT);
	}
	/**
	* Modify  the Port of the Element jmx-connector
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setPort(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.PORT, v, overwrite);
	}
	/**
	* Modify  the Port of the Element jmx-connector
	* @param v the new value
	*/
	public void setPort(String v) {
		setAttributeValue(ServerTags.PORT, v);
	}
	/**
	* Getter for AcceptAll of the Element jmx-connector
	* @return  the AcceptAll of the Element jmx-connector
	*/
	public boolean isAcceptAll() {
		return toBoolean(getAttributeValue(ServerTags.ACCEPT_ALL));
	}
	/**
	* Modify  the AcceptAll of the Element jmx-connector
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAcceptAll(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ACCEPT_ALL, ""+(v==true), overwrite);
	}
	/**
	* Modify  the AcceptAll of the Element jmx-connector
	* @param v the new value
	*/
	public void setAcceptAll(boolean v) {
		setAttributeValue(ServerTags.ACCEPT_ALL, ""+(v==true));
	}
	/**
	* Get the default value of AcceptAll from dtd
	*/
	public static String getDefaultAcceptAll() {
		return "false".trim();
	}
	/**
	* Getter for AuthRealmName of the Element jmx-connector
	* @return  the AuthRealmName of the Element jmx-connector
	*/
	public String getAuthRealmName() {
		return getAttributeValue(ServerTags.AUTH_REALM_NAME);
	}
	/**
	* Modify  the AuthRealmName of the Element jmx-connector
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAuthRealmName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.AUTH_REALM_NAME, v, overwrite);
	}
	/**
	* Modify  the AuthRealmName of the Element jmx-connector
	* @param v the new value
	*/
	public void setAuthRealmName(String v) {
		setAttributeValue(ServerTags.AUTH_REALM_NAME, v);
	}
	/**
	* Getter for SecurityEnabled of the Element jmx-connector
	* @return  the SecurityEnabled of the Element jmx-connector
	*/
	public boolean isSecurityEnabled() {
		return toBoolean(getAttributeValue(ServerTags.SECURITY_ENABLED));
	}
	/**
	* Modify  the SecurityEnabled of the Element jmx-connector
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSecurityEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SECURITY_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the SecurityEnabled of the Element jmx-connector
	* @param v the new value
	*/
	public void setSecurityEnabled(boolean v) {
		setAttributeValue(ServerTags.SECURITY_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of SecurityEnabled from dtd
	*/
	public static String getDefaultSecurityEnabled() {
		return "true".trim();
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public Ssl newSsl() {
		return new Ssl();
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
	    ret = "jmx-connector" + (canHaveSiblings() ? "[@name='" + getAttributeValue("name") +"']" : "") ;
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.ENABLED)) return "true".trim();
		if(attr.equals(ServerTags.PROTOCOL)) return "rmi_jrmp".trim();
		if(attr.equals(ServerTags.ACCEPT_ALL)) return "false".trim();
		if(attr.equals(ServerTags.SECURITY_ENABLED)) return "true".trim();
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
		str.append("Ssl");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getSsl();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(SSL, 0, str, indent);

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
		str.append("JmxConnector\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

