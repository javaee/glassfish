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
 *	This generated bean class WebServiceEndpoint matches the DTD element web-service-endpoint
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

public class WebServiceEndpoint extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String REGISTRY_LOCATION = "RegistryLocation";
	static public final String TRANSFORMATION_RULE = "TransformationRule";

	public WebServiceEndpoint() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public WebServiceEndpoint(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("registry-location", REGISTRY_LOCATION, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			RegistryLocation.class);
		this.createAttribute(REGISTRY_LOCATION, "connector-resource-jndi-name", "ConnectorResourceJndiName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("transformation-rule", TRANSFORMATION_RULE, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			TransformationRule.class);
		this.createAttribute(TRANSFORMATION_RULE, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(TRANSFORMATION_RULE, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(TRANSFORMATION_RULE, "apply-to", "ApplyTo", 
						AttrProp.CDATA,
						null, "request");
		this.createAttribute(TRANSFORMATION_RULE, "rule-file-location", "RuleFileLocation", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// Get Method
	public RegistryLocation getRegistryLocation(int index) {
		return (RegistryLocation)this.getValue(REGISTRY_LOCATION, index);
	}

	// This attribute is an array, possibly empty
	public void setRegistryLocation(RegistryLocation[] value) {
		this.setValue(REGISTRY_LOCATION, value);
	}

	// Getter Method
	public RegistryLocation[] getRegistryLocation() {
		return (RegistryLocation[])this.getValues(REGISTRY_LOCATION);
	}

	// Return the number of properties
	public int sizeRegistryLocation() {
		return this.size(REGISTRY_LOCATION);
	}

	// Add a new element returning its index in the list
	public int addRegistryLocation(RegistryLocation value)
			throws ConfigException{
		return addRegistryLocation(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addRegistryLocation(RegistryLocation value, boolean overwrite)
			throws ConfigException{
		RegistryLocation old = getRegistryLocationByConnectorResourceJndiName(value.getConnectorResourceJndiName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(WebServiceEndpoint.class).getString("cannotAddDuplicate",  "RegistryLocation"));
		}
		return this.addValue(REGISTRY_LOCATION, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeRegistryLocation(RegistryLocation value){
		return this.removeValue(REGISTRY_LOCATION, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeRegistryLocation(RegistryLocation value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(REGISTRY_LOCATION, value, overwrite);
	}

	public RegistryLocation getRegistryLocationByConnectorResourceJndiName(String id) {
	 if (null != id) { id = id.trim(); }
	RegistryLocation[] o = getRegistryLocation();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.CONNECTOR_RESOURCE_JNDI_NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public TransformationRule getTransformationRule(int index) {
		return (TransformationRule)this.getValue(TRANSFORMATION_RULE, index);
	}

	// This attribute is an array, possibly empty
	public void setTransformationRule(TransformationRule[] value) {
		this.setValue(TRANSFORMATION_RULE, value);
	}

	// Getter Method
	public TransformationRule[] getTransformationRule() {
		return (TransformationRule[])this.getValues(TRANSFORMATION_RULE);
	}

	// Return the number of properties
	public int sizeTransformationRule() {
		return this.size(TRANSFORMATION_RULE);
	}

	// Add a new element returning its index in the list
	public int addTransformationRule(TransformationRule value)
			throws ConfigException{
		return addTransformationRule(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addTransformationRule(TransformationRule value, boolean overwrite)
			throws ConfigException{
		TransformationRule old = getTransformationRuleByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(WebServiceEndpoint.class).getString("cannotAddDuplicate",  "TransformationRule"));
		}
		return this.addValue(TRANSFORMATION_RULE, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeTransformationRule(TransformationRule value){
		return this.removeValue(TRANSFORMATION_RULE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeTransformationRule(TransformationRule value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(TRANSFORMATION_RULE, value, overwrite);
	}

	public TransformationRule getTransformationRuleByName(String id) {
	 if (null != id) { id = id.trim(); }
	TransformationRule[] o = getTransformationRule();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	/**
	* Getter for Name of the Element web-service-endpoint
	* @return  the Name of the Element web-service-endpoint
	*/
	public String getName() {
		return getAttributeValue(ServerTags.NAME);
	}
	/**
	* Modify  the Name of the Element web-service-endpoint
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.NAME, v, overwrite);
	}
	/**
	* Modify  the Name of the Element web-service-endpoint
	* @param v the new value
	*/
	public void setName(String v) {
		setAttributeValue(ServerTags.NAME, v);
	}
	/**
	* Getter for Monitoring of the Element web-service-endpoint
	* @return  the Monitoring of the Element web-service-endpoint
	*/
	public String getMonitoring() {
		return getAttributeValue(ServerTags.MONITORING);
	}
	/**
	* Modify  the Monitoring of the Element web-service-endpoint
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMonitoring(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MONITORING, v, overwrite);
	}
	/**
	* Modify  the Monitoring of the Element web-service-endpoint
	* @param v the new value
	*/
	public void setMonitoring(String v) {
		setAttributeValue(ServerTags.MONITORING, v);
	}
	/**
	* Get the default value of Monitoring from dtd
	*/
	public static String getDefaultMonitoring() {
		return "OFF".trim();
	}
	/**
	* Getter for MaxHistorySize of the Element web-service-endpoint
	* @return  the MaxHistorySize of the Element web-service-endpoint
	*/
	public String getMaxHistorySize() {
		return getAttributeValue(ServerTags.MAX_HISTORY_SIZE);
	}
	/**
	* Modify  the MaxHistorySize of the Element web-service-endpoint
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMaxHistorySize(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MAX_HISTORY_SIZE, v, overwrite);
	}
	/**
	* Modify  the MaxHistorySize of the Element web-service-endpoint
	* @param v the new value
	*/
	public void setMaxHistorySize(String v) {
		setAttributeValue(ServerTags.MAX_HISTORY_SIZE, v);
	}
	/**
	* Get the default value of MaxHistorySize from dtd
	*/
	public static String getDefaultMaxHistorySize() {
		return "25".trim();
	}
	/**
	* Getter for JbiEnabled of the Element web-service-endpoint
	* @return  the JbiEnabled of the Element web-service-endpoint
	*/
	public boolean isJbiEnabled() {
		return toBoolean(getAttributeValue(ServerTags.JBI_ENABLED));
	}
	/**
	* Modify  the JbiEnabled of the Element web-service-endpoint
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setJbiEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.JBI_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the JbiEnabled of the Element web-service-endpoint
	* @param v the new value
	*/
	public void setJbiEnabled(boolean v) {
		setAttributeValue(ServerTags.JBI_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of JbiEnabled from dtd
	*/
	public static String getDefaultJbiEnabled() {
		return "false".trim();
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public RegistryLocation newRegistryLocation() {
		return new RegistryLocation();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public TransformationRule newTransformationRule() {
		return new TransformationRule();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "web-service-endpoint" + (canHaveSiblings() ? "[@name='" + getAttributeValue("name") +"']" : "") ;
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.MONITORING)) return "OFF".trim();
		if(attr.equals(ServerTags.MAX_HISTORY_SIZE)) return "25".trim();
		if(attr.equals(ServerTags.JBI_ENABLED)) return "false".trim();
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
		str.append("RegistryLocation["+this.sizeRegistryLocation()+"]");	// NOI18N
		for(int i=0; i<this.sizeRegistryLocation(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getRegistryLocation(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(REGISTRY_LOCATION, i, str, indent);
		}

		str.append(indent);
		str.append("TransformationRule["+this.sizeTransformationRule()+"]");	// NOI18N
		for(int i=0; i<this.sizeTransformationRule(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getTransformationRule(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(TRANSFORMATION_RULE, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("WebServiceEndpoint\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

