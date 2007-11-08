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
 *	This generated bean class JmsService matches the DTD element jms-service
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

public class JmsService extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String JMS_HOST = "JmsHost";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public JmsService() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public JmsService(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("jms-host", JMS_HOST, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			JmsHost.class);
		this.createAttribute(JMS_HOST, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(JMS_HOST, "host", "Host", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(JMS_HOST, "port", "Port", 
						AttrProp.CDATA,
						null, "7676");
		this.createAttribute(JMS_HOST, "admin-user-name", "AdminUserName", 
						AttrProp.CDATA,
						null, "admin");
		this.createAttribute(JMS_HOST, "admin-password", "AdminPassword", 
						AttrProp.CDATA,
						null, "admin");
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

	// Get Method
	public JmsHost getJmsHost(int index) {
		return (JmsHost)this.getValue(JMS_HOST, index);
	}

	// This attribute is an array, possibly empty
	public void setJmsHost(JmsHost[] value) {
		this.setValue(JMS_HOST, value);
	}

	// Getter Method
	public JmsHost[] getJmsHost() {
		return (JmsHost[])this.getValues(JMS_HOST);
	}

	// Return the number of properties
	public int sizeJmsHost() {
		return this.size(JMS_HOST);
	}

	// Add a new element returning its index in the list
	public int addJmsHost(JmsHost value)
			throws ConfigException{
		return addJmsHost(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addJmsHost(JmsHost value, boolean overwrite)
			throws ConfigException{
		JmsHost old = getJmsHostByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(JmsService.class).getString("cannotAddDuplicate",  "JmsHost"));
		}
		return this.addValue(JMS_HOST, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeJmsHost(JmsHost value){
		return this.removeValue(JMS_HOST, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeJmsHost(JmsHost value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(JMS_HOST, value, overwrite);
	}

	public JmsHost getJmsHostByName(String id) {
	 if (null != id) { id = id.trim(); }
	JmsHost[] o = getJmsHost();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
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
			throw new ConfigException(StringManager.getManager(JmsService.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	* Getter for InitTimeoutInSeconds of the Element jms-service
	* @return  the InitTimeoutInSeconds of the Element jms-service
	*/
	public String getInitTimeoutInSeconds() {
		return getAttributeValue(ServerTags.INIT_TIMEOUT_IN_SECONDS);
	}
	/**
	* Modify  the InitTimeoutInSeconds of the Element jms-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setInitTimeoutInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.INIT_TIMEOUT_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the InitTimeoutInSeconds of the Element jms-service
	* @param v the new value
	*/
	public void setInitTimeoutInSeconds(String v) {
		setAttributeValue(ServerTags.INIT_TIMEOUT_IN_SECONDS, v);
	}
	/**
	* Get the default value of InitTimeoutInSeconds from dtd
	*/
	public static String getDefaultInitTimeoutInSeconds() {
		return "60".trim();
	}
	/**
	* Getter for Type of the Element jms-service
	* @return  the Type of the Element jms-service
	*/
	public String getType() {
		return getAttributeValue(ServerTags.TYPE);
	}
	/**
	* Modify  the Type of the Element jms-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setType(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.TYPE, v, overwrite);
	}
	/**
	* Modify  the Type of the Element jms-service
	* @param v the new value
	*/
	public void setType(String v) {
		setAttributeValue(ServerTags.TYPE, v);
	}
	/**
	* Getter for StartArgs of the Element jms-service
	* @return  the StartArgs of the Element jms-service
	*/
	public String getStartArgs() {
			return getAttributeValue(ServerTags.START_ARGS);
	}
	/**
	* Modify  the StartArgs of the Element jms-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setStartArgs(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.START_ARGS, v, overwrite);
	}
	/**
	* Modify  the StartArgs of the Element jms-service
	* @param v the new value
	*/
	public void setStartArgs(String v) {
		setAttributeValue(ServerTags.START_ARGS, v);
	}
	/**
	* Getter for DefaultJmsHost of the Element jms-service
	* @return  the DefaultJmsHost of the Element jms-service
	*/
	public String getDefaultJmsHost() {
			return getAttributeValue(ServerTags.DEFAULT_JMS_HOST);
	}
	/**
	* Modify  the DefaultJmsHost of the Element jms-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDefaultJmsHost(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DEFAULT_JMS_HOST, v, overwrite);
	}
	/**
	* Modify  the DefaultJmsHost of the Element jms-service
	* @param v the new value
	*/
	public void setDefaultJmsHost(String v) {
		setAttributeValue(ServerTags.DEFAULT_JMS_HOST, v);
	}
	/**
	* Getter for ReconnectIntervalInSeconds of the Element jms-service
	* @return  the ReconnectIntervalInSeconds of the Element jms-service
	*/
	public String getReconnectIntervalInSeconds() {
		return getAttributeValue(ServerTags.RECONNECT_INTERVAL_IN_SECONDS);
	}
	/**
	* Modify  the ReconnectIntervalInSeconds of the Element jms-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setReconnectIntervalInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.RECONNECT_INTERVAL_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the ReconnectIntervalInSeconds of the Element jms-service
	* @param v the new value
	*/
	public void setReconnectIntervalInSeconds(String v) {
		setAttributeValue(ServerTags.RECONNECT_INTERVAL_IN_SECONDS, v);
	}
	/**
	* Get the default value of ReconnectIntervalInSeconds from dtd
	*/
	public static String getDefaultReconnectIntervalInSeconds() {
		return "5".trim();
	}
	/**
	* Getter for ReconnectAttempts of the Element jms-service
	* @return  the ReconnectAttempts of the Element jms-service
	*/
	public String getReconnectAttempts() {
		return getAttributeValue(ServerTags.RECONNECT_ATTEMPTS);
	}
	/**
	* Modify  the ReconnectAttempts of the Element jms-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setReconnectAttempts(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.RECONNECT_ATTEMPTS, v, overwrite);
	}
	/**
	* Modify  the ReconnectAttempts of the Element jms-service
	* @param v the new value
	*/
	public void setReconnectAttempts(String v) {
		setAttributeValue(ServerTags.RECONNECT_ATTEMPTS, v);
	}
	/**
	* Get the default value of ReconnectAttempts from dtd
	*/
	public static String getDefaultReconnectAttempts() {
		return "3".trim();
	}
	/**
	* Getter for ReconnectEnabled of the Element jms-service
	* @return  the ReconnectEnabled of the Element jms-service
	*/
	public boolean isReconnectEnabled() {
		return toBoolean(getAttributeValue(ServerTags.RECONNECT_ENABLED));
	}
	/**
	* Modify  the ReconnectEnabled of the Element jms-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setReconnectEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.RECONNECT_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the ReconnectEnabled of the Element jms-service
	* @param v the new value
	*/
	public void setReconnectEnabled(boolean v) {
		setAttributeValue(ServerTags.RECONNECT_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of ReconnectEnabled from dtd
	*/
	public static String getDefaultReconnectEnabled() {
		return "true".trim();
	}
	/**
	* Getter for AddresslistBehavior of the Element jms-service
	* @return  the AddresslistBehavior of the Element jms-service
	*/
	public String getAddresslistBehavior() {
		return getAttributeValue(ServerTags.ADDRESSLIST_BEHAVIOR);
	}
	/**
	* Modify  the AddresslistBehavior of the Element jms-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAddresslistBehavior(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ADDRESSLIST_BEHAVIOR, v, overwrite);
	}
	/**
	* Modify  the AddresslistBehavior of the Element jms-service
	* @param v the new value
	*/
	public void setAddresslistBehavior(String v) {
		setAttributeValue(ServerTags.ADDRESSLIST_BEHAVIOR, v);
	}
	/**
	* Get the default value of AddresslistBehavior from dtd
	*/
	public static String getDefaultAddresslistBehavior() {
		return "random".trim();
	}
	/**
	* Getter for AddresslistIterations of the Element jms-service
	* @return  the AddresslistIterations of the Element jms-service
	*/
	public String getAddresslistIterations() {
		return getAttributeValue(ServerTags.ADDRESSLIST_ITERATIONS);
	}
	/**
	* Modify  the AddresslistIterations of the Element jms-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAddresslistIterations(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ADDRESSLIST_ITERATIONS, v, overwrite);
	}
	/**
	* Modify  the AddresslistIterations of the Element jms-service
	* @param v the new value
	*/
	public void setAddresslistIterations(String v) {
		setAttributeValue(ServerTags.ADDRESSLIST_ITERATIONS, v);
	}
	/**
	* Get the default value of AddresslistIterations from dtd
	*/
	public static String getDefaultAddresslistIterations() {
		return "3".trim();
	}
	/**
	* Getter for MqScheme of the Element jms-service
	* @return  the MqScheme of the Element jms-service
	*/
	public String getMqScheme() {
			return getAttributeValue(ServerTags.MQ_SCHEME);
	}
	/**
	* Modify  the MqScheme of the Element jms-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMqScheme(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MQ_SCHEME, v, overwrite);
	}
	/**
	* Modify  the MqScheme of the Element jms-service
	* @param v the new value
	*/
	public void setMqScheme(String v) {
		setAttributeValue(ServerTags.MQ_SCHEME, v);
	}
	/**
	* Getter for MqService of the Element jms-service
	* @return  the MqService of the Element jms-service
	*/
	public String getMqService() {
			return getAttributeValue(ServerTags.MQ_SERVICE);
	}
	/**
	* Modify  the MqService of the Element jms-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMqService(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MQ_SERVICE, v, overwrite);
	}
	/**
	* Modify  the MqService of the Element jms-service
	* @param v the new value
	*/
	public void setMqService(String v) {
		setAttributeValue(ServerTags.MQ_SERVICE, v);
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public JmsHost newJmsHost() {
		return new JmsHost();
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
	    ret = "jms-service";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.INIT_TIMEOUT_IN_SECONDS)) return "60".trim();
		if(attr.equals(ServerTags.RECONNECT_INTERVAL_IN_SECONDS)) return "5".trim();
		if(attr.equals(ServerTags.RECONNECT_ATTEMPTS)) return "3".trim();
		if(attr.equals(ServerTags.RECONNECT_ENABLED)) return "true".trim();
		if(attr.equals(ServerTags.ADDRESSLIST_BEHAVIOR)) return "random".trim();
		if(attr.equals(ServerTags.ADDRESSLIST_ITERATIONS)) return "3".trim();
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
		str.append("JmsHost["+this.sizeJmsHost()+"]");	// NOI18N
		for(int i=0; i<this.sizeJmsHost(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getJmsHost(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(JMS_HOST, i, str, indent);
		}

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
		str.append("JmsService\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

