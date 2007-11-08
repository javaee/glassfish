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
 *	This generated bean class IiopListener matches the DTD element iiop-listener
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

public class IiopListener extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String SSL = "Ssl";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public IiopListener() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public IiopListener(int options)
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
			throw new ConfigException(StringManager.getManager(IiopListener.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	* Getter for Id of the Element iiop-listener
	* @return  the Id of the Element iiop-listener
	*/
	public String getId() {
		return getAttributeValue(ServerTags.ID);
	}
	/**
	* Modify  the Id of the Element iiop-listener
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setId(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ID, v, overwrite);
	}
	/**
	* Modify  the Id of the Element iiop-listener
	* @param v the new value
	*/
	public void setId(String v) {
		setAttributeValue(ServerTags.ID, v);
	}
	/**
	* Getter for Address of the Element iiop-listener
	* @return  the Address of the Element iiop-listener
	*/
	public String getAddress() {
		return getAttributeValue(ServerTags.ADDRESS);
	}
	/**
	* Modify  the Address of the Element iiop-listener
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAddress(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ADDRESS, v, overwrite);
	}
	/**
	* Modify  the Address of the Element iiop-listener
	* @param v the new value
	*/
	public void setAddress(String v) {
		setAttributeValue(ServerTags.ADDRESS, v);
	}
	/**
	* Getter for Port of the Element iiop-listener
	* @return  the Port of the Element iiop-listener
	*/
	public String getPort() {
		return getAttributeValue(ServerTags.PORT);
	}
	/**
	* Modify  the Port of the Element iiop-listener
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setPort(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.PORT, v, overwrite);
	}
	/**
	* Modify  the Port of the Element iiop-listener
	* @param v the new value
	*/
	public void setPort(String v) {
		setAttributeValue(ServerTags.PORT, v);
	}
	/**
	* Get the default value of Port from dtd
	*/
	public static String getDefaultPort() {
		return "1072".trim();
	}
	/**
	* Getter for SecurityEnabled of the Element iiop-listener
	* @return  the SecurityEnabled of the Element iiop-listener
	*/
	public boolean isSecurityEnabled() {
		return toBoolean(getAttributeValue(ServerTags.SECURITY_ENABLED));
	}
	/**
	* Modify  the SecurityEnabled of the Element iiop-listener
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSecurityEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SECURITY_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the SecurityEnabled of the Element iiop-listener
	* @param v the new value
	*/
	public void setSecurityEnabled(boolean v) {
		setAttributeValue(ServerTags.SECURITY_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of SecurityEnabled from dtd
	*/
	public static String getDefaultSecurityEnabled() {
		return "false".trim();
	}
	/**
	* Getter for Enabled of the Element iiop-listener
	* @return  the Enabled of the Element iiop-listener
	*/
	public boolean isEnabled() {
		return toBoolean(getAttributeValue(ServerTags.ENABLED));
	}
	/**
	* Modify  the Enabled of the Element iiop-listener
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the Enabled of the Element iiop-listener
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
	    ret = "iiop-listener" + (canHaveSiblings() ? "[@id='" + getAttributeValue("id") +"']" : "") ;
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.PORT)) return "1072".trim();
		if(attr.equals(ServerTags.SECURITY_ENABLED)) return "false".trim();
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
		str.append("IiopListener\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

