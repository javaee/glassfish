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
 *	This generated bean class NodeAgent matches the DTD element node-agent
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

public class NodeAgent extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String JMX_CONNECTOR = "JmxConnector";
	static public final String AUTH_REALM = "AuthRealm";
	static public final String LOG_SERVICE = "LogService";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public NodeAgent() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public NodeAgent(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(4);
		this.createProperty("jmx-connector", JMX_CONNECTOR, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			JmxConnector.class);
		this.createAttribute(JMX_CONNECTOR, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(JMX_CONNECTOR, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(JMX_CONNECTOR, "protocol", "Protocol", 
						AttrProp.CDATA,
						null, "rmi_jrmp");
		this.createAttribute(JMX_CONNECTOR, "address", "Address", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(JMX_CONNECTOR, "port", "Port", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(JMX_CONNECTOR, "accept-all", "AcceptAll", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(JMX_CONNECTOR, "auth-realm-name", "AuthRealmName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(JMX_CONNECTOR, "security-enabled", "SecurityEnabled", 
						AttrProp.CDATA,
						null, "true");
		this.createProperty("auth-realm", AUTH_REALM, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			AuthRealm.class);
		this.createAttribute(AUTH_REALM, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(AUTH_REALM, "classname", "Classname", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("log-service", LOG_SERVICE, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			LogService.class);
		this.createAttribute(LOG_SERVICE, "file", "File", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(LOG_SERVICE, "use-system-logging", "UseSystemLogging", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(LOG_SERVICE, "log-handler", "LogHandler", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(LOG_SERVICE, "log-filter", "LogFilter", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(LOG_SERVICE, "log-to-console", "LogToConsole", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(LOG_SERVICE, "log-rotation-limit-in-bytes", "LogRotationLimitInBytes", 
						AttrProp.CDATA,
						null, "500000");
		this.createAttribute(LOG_SERVICE, "log-rotation-timelimit-in-minutes", "LogRotationTimelimitInMinutes", 
						AttrProp.CDATA,
						null, "0");
		this.createAttribute(LOG_SERVICE, "alarms", "Alarms", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(LOG_SERVICE, "retain-error-statistics-for-hours", "RetainErrorStatisticsForHours", 
						AttrProp.CDATA,
						null, "5");
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
	public void setJmxConnector(JmxConnector value) {
		this.setValue(JMX_CONNECTOR, value);
	}

	// Get Method
	public JmxConnector getJmxConnector() {
		return (JmxConnector)this.getValue(JMX_CONNECTOR);
	}

	// This attribute is optional
	public void setAuthRealm(AuthRealm value) {
		this.setValue(AUTH_REALM, value);
	}

	// Get Method
	public AuthRealm getAuthRealm() {
		return (AuthRealm)this.getValue(AUTH_REALM);
	}

	// This attribute is mandatory
	public void setLogService(LogService value) {
		this.setValue(LOG_SERVICE, value);
	}

	// Get Method
	public LogService getLogService() {
		return (LogService)this.getValue(LOG_SERVICE);
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
			throw new ConfigException(StringManager.getManager(NodeAgent.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	* Getter for Name of the Element node-agent
	* @return  the Name of the Element node-agent
	*/
	public String getName() {
		return getAttributeValue(ServerTags.NAME);
	}
	/**
	* Modify  the Name of the Element node-agent
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.NAME, v, overwrite);
	}
	/**
	* Modify  the Name of the Element node-agent
	* @param v the new value
	*/
	public void setName(String v) {
		setAttributeValue(ServerTags.NAME, v);
	}
	/**
	* Getter for SystemJmxConnectorName of the Element node-agent
	* @return  the SystemJmxConnectorName of the Element node-agent
	*/
	public String getSystemJmxConnectorName() {
			return getAttributeValue(ServerTags.SYSTEM_JMX_CONNECTOR_NAME);
	}
	/**
	* Modify  the SystemJmxConnectorName of the Element node-agent
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSystemJmxConnectorName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SYSTEM_JMX_CONNECTOR_NAME, v, overwrite);
	}
	/**
	* Modify  the SystemJmxConnectorName of the Element node-agent
	* @param v the new value
	*/
	public void setSystemJmxConnectorName(String v) {
		setAttributeValue(ServerTags.SYSTEM_JMX_CONNECTOR_NAME, v);
	}
	/**
	* Getter for StartServersInStartup of the Element node-agent
	* @return  the StartServersInStartup of the Element node-agent
	*/
	public boolean isStartServersInStartup() {
		return toBoolean(getAttributeValue(ServerTags.START_SERVERS_IN_STARTUP));
	}
	/**
	* Modify  the StartServersInStartup of the Element node-agent
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setStartServersInStartup(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.START_SERVERS_IN_STARTUP, ""+(v==true), overwrite);
	}
	/**
	* Modify  the StartServersInStartup of the Element node-agent
	* @param v the new value
	*/
	public void setStartServersInStartup(boolean v) {
		setAttributeValue(ServerTags.START_SERVERS_IN_STARTUP, ""+(v==true));
	}
	/**
	* Get the default value of StartServersInStartup from dtd
	*/
	public static String getDefaultStartServersInStartup() {
		return "true".trim();
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public JmxConnector newJmxConnector() {
		return new JmxConnector();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public AuthRealm newAuthRealm() {
		return new AuthRealm();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public LogService newLogService() {
		return new LogService();
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
	    ret = "node-agent" + (canHaveSiblings() ? "[@name='" + getAttributeValue("name") +"']" : "") ;
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.START_SERVERS_IN_STARTUP)) return "true".trim();
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
		str.append("JmxConnector");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getJmxConnector();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(JMX_CONNECTOR, 0, str, indent);

		str.append(indent);
		str.append("AuthRealm");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getAuthRealm();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(AUTH_REALM, 0, str, indent);

		str.append(indent);
		str.append("LogService");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getLogService();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(LOG_SERVICE, 0, str, indent);

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
		str.append("NodeAgent\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

