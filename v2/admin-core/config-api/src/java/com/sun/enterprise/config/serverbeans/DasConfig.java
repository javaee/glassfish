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
 *	This generated bean class DasConfig matches the DTD element das-config
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

public class DasConfig extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public DasConfig() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public DasConfig(int options)
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
			throw new ConfigException(StringManager.getManager(DasConfig.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	* Getter for DynamicReloadEnabled of the Element das-config
	* @return  the DynamicReloadEnabled of the Element das-config
	*/
	public boolean isDynamicReloadEnabled() {
		return toBoolean(getAttributeValue(ServerTags.DYNAMIC_RELOAD_ENABLED));
	}
	/**
	* Modify  the DynamicReloadEnabled of the Element das-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDynamicReloadEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DYNAMIC_RELOAD_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the DynamicReloadEnabled of the Element das-config
	* @param v the new value
	*/
	public void setDynamicReloadEnabled(boolean v) {
		setAttributeValue(ServerTags.DYNAMIC_RELOAD_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of DynamicReloadEnabled from dtd
	*/
	public static String getDefaultDynamicReloadEnabled() {
		return "false".trim();
	}
	/**
	* Getter for DynamicReloadPollIntervalInSeconds of the Element das-config
	* @return  the DynamicReloadPollIntervalInSeconds of the Element das-config
	*/
	public String getDynamicReloadPollIntervalInSeconds() {
		return getAttributeValue(ServerTags.DYNAMIC_RELOAD_POLL_INTERVAL_IN_SECONDS);
	}
	/**
	* Modify  the DynamicReloadPollIntervalInSeconds of the Element das-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDynamicReloadPollIntervalInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DYNAMIC_RELOAD_POLL_INTERVAL_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the DynamicReloadPollIntervalInSeconds of the Element das-config
	* @param v the new value
	*/
	public void setDynamicReloadPollIntervalInSeconds(String v) {
		setAttributeValue(ServerTags.DYNAMIC_RELOAD_POLL_INTERVAL_IN_SECONDS, v);
	}
	/**
	* Get the default value of DynamicReloadPollIntervalInSeconds from dtd
	*/
	public static String getDefaultDynamicReloadPollIntervalInSeconds() {
		return "2".trim();
	}
	/**
	* Getter for AutodeployEnabled of the Element das-config
	* @return  the AutodeployEnabled of the Element das-config
	*/
	public boolean isAutodeployEnabled() {
		return toBoolean(getAttributeValue(ServerTags.AUTODEPLOY_ENABLED));
	}
	/**
	* Modify  the AutodeployEnabled of the Element das-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAutodeployEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.AUTODEPLOY_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the AutodeployEnabled of the Element das-config
	* @param v the new value
	*/
	public void setAutodeployEnabled(boolean v) {
		setAttributeValue(ServerTags.AUTODEPLOY_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of AutodeployEnabled from dtd
	*/
	public static String getDefaultAutodeployEnabled() {
		return "false".trim();
	}
	/**
	* Getter for AutodeployPollingIntervalInSeconds of the Element das-config
	* @return  the AutodeployPollingIntervalInSeconds of the Element das-config
	*/
	public String getAutodeployPollingIntervalInSeconds() {
		return getAttributeValue(ServerTags.AUTODEPLOY_POLLING_INTERVAL_IN_SECONDS);
	}
	/**
	* Modify  the AutodeployPollingIntervalInSeconds of the Element das-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAutodeployPollingIntervalInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.AUTODEPLOY_POLLING_INTERVAL_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the AutodeployPollingIntervalInSeconds of the Element das-config
	* @param v the new value
	*/
	public void setAutodeployPollingIntervalInSeconds(String v) {
		setAttributeValue(ServerTags.AUTODEPLOY_POLLING_INTERVAL_IN_SECONDS, v);
	}
	/**
	* Get the default value of AutodeployPollingIntervalInSeconds from dtd
	*/
	public static String getDefaultAutodeployPollingIntervalInSeconds() {
		return "2".trim();
	}
	/**
	* Getter for AutodeployDir of the Element das-config
	* @return  the AutodeployDir of the Element das-config
	*/
	public String getAutodeployDir() {
		return getAttributeValue(ServerTags.AUTODEPLOY_DIR);
	}
	/**
	* Modify  the AutodeployDir of the Element das-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAutodeployDir(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.AUTODEPLOY_DIR, v, overwrite);
	}
	/**
	* Modify  the AutodeployDir of the Element das-config
	* @param v the new value
	*/
	public void setAutodeployDir(String v) {
		setAttributeValue(ServerTags.AUTODEPLOY_DIR, v);
	}
	/**
	* Get the default value of AutodeployDir from dtd
	*/
	public static String getDefaultAutodeployDir() {
		return "autodeploy".trim();
	}
	/**
	* Getter for AutodeployVerifierEnabled of the Element das-config
	* @return  the AutodeployVerifierEnabled of the Element das-config
	*/
	public boolean isAutodeployVerifierEnabled() {
		return toBoolean(getAttributeValue(ServerTags.AUTODEPLOY_VERIFIER_ENABLED));
	}
	/**
	* Modify  the AutodeployVerifierEnabled of the Element das-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAutodeployVerifierEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.AUTODEPLOY_VERIFIER_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the AutodeployVerifierEnabled of the Element das-config
	* @param v the new value
	*/
	public void setAutodeployVerifierEnabled(boolean v) {
		setAttributeValue(ServerTags.AUTODEPLOY_VERIFIER_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of AutodeployVerifierEnabled from dtd
	*/
	public static String getDefaultAutodeployVerifierEnabled() {
		return "false".trim();
	}
	/**
	* Getter for AutodeployJspPrecompilationEnabled of the Element das-config
	* @return  the AutodeployJspPrecompilationEnabled of the Element das-config
	*/
	public boolean isAutodeployJspPrecompilationEnabled() {
		return toBoolean(getAttributeValue(ServerTags.AUTODEPLOY_JSP_PRECOMPILATION_ENABLED));
	}
	/**
	* Modify  the AutodeployJspPrecompilationEnabled of the Element das-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAutodeployJspPrecompilationEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.AUTODEPLOY_JSP_PRECOMPILATION_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the AutodeployJspPrecompilationEnabled of the Element das-config
	* @param v the new value
	*/
	public void setAutodeployJspPrecompilationEnabled(boolean v) {
		setAttributeValue(ServerTags.AUTODEPLOY_JSP_PRECOMPILATION_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of AutodeployJspPrecompilationEnabled from dtd
	*/
	public static String getDefaultAutodeployJspPrecompilationEnabled() {
		return "false".trim();
	}
	/**
	* Getter for DeployXmlValidation of the Element das-config
	* @return  the DeployXmlValidation of the Element das-config
	*/
	public String getDeployXmlValidation() {
		return getAttributeValue(ServerTags.DEPLOY_XML_VALIDATION);
	}
	/**
	* Modify  the DeployXmlValidation of the Element das-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDeployXmlValidation(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DEPLOY_XML_VALIDATION, v, overwrite);
	}
	/**
	* Modify  the DeployXmlValidation of the Element das-config
	* @param v the new value
	*/
	public void setDeployXmlValidation(String v) {
		setAttributeValue(ServerTags.DEPLOY_XML_VALIDATION, v);
	}
	/**
	* Get the default value of DeployXmlValidation from dtd
	*/
	public static String getDefaultDeployXmlValidation() {
		return "full".trim();
	}
	/**
	* Getter for AdminSessionTimeoutInMinutes of the Element das-config
	* @return  the AdminSessionTimeoutInMinutes of the Element das-config
	*/
	public String getAdminSessionTimeoutInMinutes() {
		return getAttributeValue(ServerTags.ADMIN_SESSION_TIMEOUT_IN_MINUTES);
	}
	/**
	* Modify  the AdminSessionTimeoutInMinutes of the Element das-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAdminSessionTimeoutInMinutes(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ADMIN_SESSION_TIMEOUT_IN_MINUTES, v, overwrite);
	}
	/**
	* Modify  the AdminSessionTimeoutInMinutes of the Element das-config
	* @param v the new value
	*/
	public void setAdminSessionTimeoutInMinutes(String v) {
		setAttributeValue(ServerTags.ADMIN_SESSION_TIMEOUT_IN_MINUTES, v);
	}
	/**
	* Get the default value of AdminSessionTimeoutInMinutes from dtd
	*/
	public static String getDefaultAdminSessionTimeoutInMinutes() {
		return "60".trim();
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
	    ret = "das-config";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.DYNAMIC_RELOAD_ENABLED)) return "false".trim();
		if(attr.equals(ServerTags.DYNAMIC_RELOAD_POLL_INTERVAL_IN_SECONDS)) return "2".trim();
		if(attr.equals(ServerTags.AUTODEPLOY_ENABLED)) return "false".trim();
		if(attr.equals(ServerTags.AUTODEPLOY_POLLING_INTERVAL_IN_SECONDS)) return "2".trim();
		if(attr.equals(ServerTags.AUTODEPLOY_DIR)) return "autodeploy".trim();
		if(attr.equals(ServerTags.AUTODEPLOY_VERIFIER_ENABLED)) return "false".trim();
		if(attr.equals(ServerTags.AUTODEPLOY_JSP_PRECOMPILATION_ENABLED)) return "false".trim();
		if(attr.equals(ServerTags.DEPLOY_XML_VALIDATION)) return "full".trim();
		if(attr.equals(ServerTags.ADMIN_SESSION_TIMEOUT_IN_MINUTES)) return "60".trim();
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
		str.append("DasConfig\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

