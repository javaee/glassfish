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
 *	This generated bean class LbConfig matches the DTD element lb-config
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

public class LbConfig extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String CLUSTER_REF = "ClusterRef";
	static public final String SERVER_REF = "ServerRef";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public LbConfig() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public LbConfig(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("cluster-ref", CLUSTER_REF, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ClusterRef.class);
		this.createAttribute(CLUSTER_REF, "ref", "Ref", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(CLUSTER_REF, "lb-policy", "LbPolicy", 
						AttrProp.CDATA,
						null, "round-robin");
		this.createAttribute(CLUSTER_REF, "lb-policy-module", "LbPolicyModule", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("server-ref", SERVER_REF, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ServerRef.class);
		this.createAttribute(SERVER_REF, "ref", "Ref", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(SERVER_REF, "disable-timeout-in-minutes", "DisableTimeoutInMinutes", 
						AttrProp.CDATA,
						null, "30");
		this.createAttribute(SERVER_REF, "lb-enabled", "LbEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(SERVER_REF, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
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
	public ClusterRef getClusterRef(int index) {
		return (ClusterRef)this.getValue(CLUSTER_REF, index);
	}

	// This attribute is an array, possibly empty
	public void setClusterRef(ClusterRef[] value) {
		this.setValue(CLUSTER_REF, value);
		if (value != null && value.length > 0) {
			// It's a mutually exclusive property.
			setServerRef(null);
		}
	}

	// Getter Method
	public ClusterRef[] getClusterRef() {
		return (ClusterRef[])this.getValues(CLUSTER_REF);
	}

	// Return the number of properties
	public int sizeClusterRef() {
		return this.size(CLUSTER_REF);
	}

	// Add a new element returning its index in the list
	public int addClusterRef(ClusterRef value)
			throws ConfigException{
		return addClusterRef(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addClusterRef(ClusterRef value, boolean overwrite)
			throws ConfigException{
		ClusterRef old = getClusterRefByRef(value.getRef());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(LbConfig.class).getString("cannotAddDuplicate",  "ClusterRef"));
		}
		return this.addValue(CLUSTER_REF, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeClusterRef(ClusterRef value){
		return this.removeValue(CLUSTER_REF, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeClusterRef(ClusterRef value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(CLUSTER_REF, value, overwrite);
	}

	public ClusterRef getClusterRefByRef(String id) {
	 if (null != id) { id = id.trim(); }
	ClusterRef[] o = getClusterRef();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.REF)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public ServerRef getServerRef(int index) {
		return (ServerRef)this.getValue(SERVER_REF, index);
	}

	// This attribute is an array, possibly empty
	public void setServerRef(ServerRef[] value) {
		this.setValue(SERVER_REF, value);
		if (value != null && value.length > 0) {
			// It's a mutually exclusive property.
			setClusterRef(null);
		}
	}

	// Getter Method
	public ServerRef[] getServerRef() {
		return (ServerRef[])this.getValues(SERVER_REF);
	}

	// Return the number of properties
	public int sizeServerRef() {
		return this.size(SERVER_REF);
	}

	// Add a new element returning its index in the list
	public int addServerRef(ServerRef value)
			throws ConfigException{
		return addServerRef(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addServerRef(ServerRef value, boolean overwrite)
			throws ConfigException{
		ServerRef old = getServerRefByRef(value.getRef());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(LbConfig.class).getString("cannotAddDuplicate",  "ServerRef"));
		}
		return this.addValue(SERVER_REF, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeServerRef(ServerRef value){
		return this.removeValue(SERVER_REF, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeServerRef(ServerRef value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(SERVER_REF, value, overwrite);
	}

	public ServerRef getServerRefByRef(String id) {
	 if (null != id) { id = id.trim(); }
	ServerRef[] o = getServerRef();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.REF)).equals(id)) {
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
			throw new ConfigException(StringManager.getManager(LbConfig.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	* Getter for Name of the Element lb-config
	* @return  the Name of the Element lb-config
	*/
	public String getName() {
		return getAttributeValue(ServerTags.NAME);
	}
	/**
	* Modify  the Name of the Element lb-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.NAME, v, overwrite);
	}
	/**
	* Modify  the Name of the Element lb-config
	* @param v the new value
	*/
	public void setName(String v) {
		setAttributeValue(ServerTags.NAME, v);
	}
	/**
	* Getter for ResponseTimeoutInSeconds of the Element lb-config
	* @return  the ResponseTimeoutInSeconds of the Element lb-config
	*/
	public String getResponseTimeoutInSeconds() {
		return getAttributeValue(ServerTags.RESPONSE_TIMEOUT_IN_SECONDS);
	}
	/**
	* Modify  the ResponseTimeoutInSeconds of the Element lb-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setResponseTimeoutInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.RESPONSE_TIMEOUT_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the ResponseTimeoutInSeconds of the Element lb-config
	* @param v the new value
	*/
	public void setResponseTimeoutInSeconds(String v) {
		setAttributeValue(ServerTags.RESPONSE_TIMEOUT_IN_SECONDS, v);
	}
	/**
	* Get the default value of ResponseTimeoutInSeconds from dtd
	*/
	public static String getDefaultResponseTimeoutInSeconds() {
		return "60".trim();
	}
	/**
	* Getter for HttpsRouting of the Element lb-config
	* @return  the HttpsRouting of the Element lb-config
	*/
	public boolean isHttpsRouting() {
		return toBoolean(getAttributeValue(ServerTags.HTTPS_ROUTING));
	}
	/**
	* Modify  the HttpsRouting of the Element lb-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setHttpsRouting(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.HTTPS_ROUTING, ""+(v==true), overwrite);
	}
	/**
	* Modify  the HttpsRouting of the Element lb-config
	* @param v the new value
	*/
	public void setHttpsRouting(boolean v) {
		setAttributeValue(ServerTags.HTTPS_ROUTING, ""+(v==true));
	}
	/**
	* Get the default value of HttpsRouting from dtd
	*/
	public static String getDefaultHttpsRouting() {
		return "false".trim();
	}
	/**
	* Getter for ReloadPollIntervalInSeconds of the Element lb-config
	* @return  the ReloadPollIntervalInSeconds of the Element lb-config
	*/
	public String getReloadPollIntervalInSeconds() {
		return getAttributeValue(ServerTags.RELOAD_POLL_INTERVAL_IN_SECONDS);
	}
	/**
	* Modify  the ReloadPollIntervalInSeconds of the Element lb-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setReloadPollIntervalInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.RELOAD_POLL_INTERVAL_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the ReloadPollIntervalInSeconds of the Element lb-config
	* @param v the new value
	*/
	public void setReloadPollIntervalInSeconds(String v) {
		setAttributeValue(ServerTags.RELOAD_POLL_INTERVAL_IN_SECONDS, v);
	}
	/**
	* Get the default value of ReloadPollIntervalInSeconds from dtd
	*/
	public static String getDefaultReloadPollIntervalInSeconds() {
		return "60".trim();
	}
	/**
	* Getter for MonitoringEnabled of the Element lb-config
	* @return  the MonitoringEnabled of the Element lb-config
	*/
	public boolean isMonitoringEnabled() {
		return toBoolean(getAttributeValue(ServerTags.MONITORING_ENABLED));
	}
	/**
	* Modify  the MonitoringEnabled of the Element lb-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMonitoringEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MONITORING_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the MonitoringEnabled of the Element lb-config
	* @param v the new value
	*/
	public void setMonitoringEnabled(boolean v) {
		setAttributeValue(ServerTags.MONITORING_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of MonitoringEnabled from dtd
	*/
	public static String getDefaultMonitoringEnabled() {
		return "false".trim();
	}
	/**
	* Getter for RouteCookieEnabled of the Element lb-config
	* @return  the RouteCookieEnabled of the Element lb-config
	*/
	public boolean isRouteCookieEnabled() {
		return toBoolean(getAttributeValue(ServerTags.ROUTE_COOKIE_ENABLED));
	}
	/**
	* Modify  the RouteCookieEnabled of the Element lb-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setRouteCookieEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ROUTE_COOKIE_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the RouteCookieEnabled of the Element lb-config
	* @param v the new value
	*/
	public void setRouteCookieEnabled(boolean v) {
		setAttributeValue(ServerTags.ROUTE_COOKIE_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of RouteCookieEnabled from dtd
	*/
	public static String getDefaultRouteCookieEnabled() {
		return "true".trim();
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ClusterRef newClusterRef() {
		return new ClusterRef();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ServerRef newServerRef() {
		return new ServerRef();
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
	    ret = "lb-config" + (canHaveSiblings() ? "[@name='" + getAttributeValue("name") +"']" : "") ;
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.RESPONSE_TIMEOUT_IN_SECONDS)) return "60".trim();
		if(attr.equals(ServerTags.HTTPS_ROUTING)) return "false".trim();
		if(attr.equals(ServerTags.RELOAD_POLL_INTERVAL_IN_SECONDS)) return "60".trim();
		if(attr.equals(ServerTags.MONITORING_ENABLED)) return "false".trim();
		if(attr.equals(ServerTags.ROUTE_COOKIE_ENABLED)) return "true".trim();
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
		str.append("ClusterRef["+this.sizeClusterRef()+"]");	// NOI18N
		for(int i=0; i<this.sizeClusterRef(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getClusterRef(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CLUSTER_REF, i, str, indent);
		}

		str.append(indent);
		str.append("ServerRef["+this.sizeServerRef()+"]");	// NOI18N
		for(int i=0; i<this.sizeServerRef(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getServerRef(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SERVER_REF, i, str, indent);
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
		str.append("LbConfig\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

