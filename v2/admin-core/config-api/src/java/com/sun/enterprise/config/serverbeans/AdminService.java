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
 *	This generated bean class AdminService matches the DTD element admin-service
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

public class AdminService extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String JMX_CONNECTOR = "JmxConnector";
	static public final String DAS_CONFIG = "DasConfig";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public AdminService() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public AdminService(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("jmx-connector", JMX_CONNECTOR, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
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
		this.createProperty("das-config", DAS_CONFIG, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			DasConfig.class);
		this.createAttribute(DAS_CONFIG, "dynamic-reload-enabled", "DynamicReloadEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(DAS_CONFIG, "dynamic-reload-poll-interval-in-seconds", "DynamicReloadPollIntervalInSeconds", 
						AttrProp.CDATA,
						null, "2");
		this.createAttribute(DAS_CONFIG, "autodeploy-enabled", "AutodeployEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(DAS_CONFIG, "autodeploy-polling-interval-in-seconds", "AutodeployPollingIntervalInSeconds", 
						AttrProp.CDATA,
						null, "2");
		this.createAttribute(DAS_CONFIG, "autodeploy-dir", "AutodeployDir", 
						AttrProp.CDATA,
						null, "autodeploy");
		this.createAttribute(DAS_CONFIG, "autodeploy-verifier-enabled", "AutodeployVerifierEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(DAS_CONFIG, "autodeploy-jsp-precompilation-enabled", "AutodeployJspPrecompilationEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(DAS_CONFIG, "deploy-xml-validation", "DeployXmlValidation", 
						AttrProp.CDATA,
						null, "full");
		this.createAttribute(DAS_CONFIG, "admin-session-timeout-in-minutes", "AdminSessionTimeoutInMinutes", 
						AttrProp.CDATA,
						null, "60");
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
	public JmxConnector getJmxConnector(int index) {
		return (JmxConnector)this.getValue(JMX_CONNECTOR, index);
	}

	// This attribute is an array, possibly empty
	public void setJmxConnector(JmxConnector[] value) {
		this.setValue(JMX_CONNECTOR, value);
	}

	// Getter Method
	public JmxConnector[] getJmxConnector() {
		return (JmxConnector[])this.getValues(JMX_CONNECTOR);
	}

	// Return the number of properties
	public int sizeJmxConnector() {
		return this.size(JMX_CONNECTOR);
	}

	// Add a new element returning its index in the list
	public int addJmxConnector(JmxConnector value)
			throws ConfigException{
		return addJmxConnector(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addJmxConnector(JmxConnector value, boolean overwrite)
			throws ConfigException{
		JmxConnector old = getJmxConnectorByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(AdminService.class).getString("cannotAddDuplicate",  "JmxConnector"));
		}
		return this.addValue(JMX_CONNECTOR, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeJmxConnector(JmxConnector value){
		return this.removeValue(JMX_CONNECTOR, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeJmxConnector(JmxConnector value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(JMX_CONNECTOR, value, overwrite);
	}

	public JmxConnector getJmxConnectorByName(String id) {
	 if (null != id) { id = id.trim(); }
	JmxConnector[] o = getJmxConnector();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// This attribute is optional
	public void setDasConfig(DasConfig value) {
		this.setValue(DAS_CONFIG, value);
	}

	// Get Method
	public DasConfig getDasConfig() {
		return (DasConfig)this.getValue(DAS_CONFIG);
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
			throw new ConfigException(StringManager.getManager(AdminService.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	* Getter for Type of the Element admin-service
	* @return  the Type of the Element admin-service
	*/
	public String getType() {
		return getAttributeValue(ServerTags.TYPE);
	}
	/**
	* Modify  the Type of the Element admin-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setType(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.TYPE, v, overwrite);
	}
	/**
	* Modify  the Type of the Element admin-service
	* @param v the new value
	*/
	public void setType(String v) {
		setAttributeValue(ServerTags.TYPE, v);
	}
	/**
	* Get the default value of Type from dtd
	*/
	public static String getDefaultType() {
		return "server".trim();
	}
	/**
	* Getter for SystemJmxConnectorName of the Element admin-service
	* @return  the SystemJmxConnectorName of the Element admin-service
	*/
	public String getSystemJmxConnectorName() {
			return getAttributeValue(ServerTags.SYSTEM_JMX_CONNECTOR_NAME);
	}
	/**
	* Modify  the SystemJmxConnectorName of the Element admin-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSystemJmxConnectorName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SYSTEM_JMX_CONNECTOR_NAME, v, overwrite);
	}
	/**
	* Modify  the SystemJmxConnectorName of the Element admin-service
	* @param v the new value
	*/
	public void setSystemJmxConnectorName(String v) {
		setAttributeValue(ServerTags.SYSTEM_JMX_CONNECTOR_NAME, v);
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
	public DasConfig newDasConfig() {
		return new DasConfig();
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
	    ret = "admin-service";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.TYPE)) return "server".trim();
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
		str.append("JmxConnector["+this.sizeJmxConnector()+"]");	// NOI18N
		for(int i=0; i<this.sizeJmxConnector(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getJmxConnector(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(JMX_CONNECTOR, i, str, indent);
		}

		str.append(indent);
		str.append("DasConfig");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getDasConfig();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(DAS_CONFIG, 0, str, indent);

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
		str.append("AdminService\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

