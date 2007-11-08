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
 *	This generated bean class Cluster matches the DTD element cluster
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

public class Cluster extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String SERVER_REF = "ServerRef";
	static public final String RESOURCE_REF = "ResourceRef";
	static public final String APPLICATION_REF = "ApplicationRef";
	static public final String SYSTEM_PROPERTY = "SystemProperty";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public Cluster() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Cluster(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(5);
		this.createProperty("server-ref", SERVER_REF, 
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
		this.createProperty("resource-ref", RESOURCE_REF, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ResourceRef.class);
		this.createAttribute(RESOURCE_REF, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(RESOURCE_REF, "ref", "Ref", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("application-ref", APPLICATION_REF, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ApplicationRef.class);
		this.createAttribute(APPLICATION_REF, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(APPLICATION_REF, "virtual-servers", "VirtualServers", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(APPLICATION_REF, "lb-enabled", "LbEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(APPLICATION_REF, "disable-timeout-in-minutes", "DisableTimeoutInMinutes", 
						AttrProp.CDATA,
						null, "30");
		this.createAttribute(APPLICATION_REF, "ref", "Ref", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("system-property", SYSTEM_PROPERTY, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SystemProperty.class);
		this.createAttribute(SYSTEM_PROPERTY, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(SYSTEM_PROPERTY, "value", "Value", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
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
	public ServerRef getServerRef(int index) {
		return (ServerRef)this.getValue(SERVER_REF, index);
	}

	// This attribute is an array, possibly empty
	public void setServerRef(ServerRef[] value) {
		this.setValue(SERVER_REF, value);
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
			throw new ConfigException(StringManager.getManager(Cluster.class).getString("cannotAddDuplicate",  "ServerRef"));
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
	public ResourceRef getResourceRef(int index) {
		return (ResourceRef)this.getValue(RESOURCE_REF, index);
	}

	// This attribute is an array, possibly empty
	public void setResourceRef(ResourceRef[] value) {
		this.setValue(RESOURCE_REF, value);
	}

	// Getter Method
	public ResourceRef[] getResourceRef() {
		return (ResourceRef[])this.getValues(RESOURCE_REF);
	}

	// Return the number of properties
	public int sizeResourceRef() {
		return this.size(RESOURCE_REF);
	}

	// Add a new element returning its index in the list
	public int addResourceRef(ResourceRef value)
			throws ConfigException{
		return addResourceRef(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addResourceRef(ResourceRef value, boolean overwrite)
			throws ConfigException{
		ResourceRef old = getResourceRefByRef(value.getRef());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Cluster.class).getString("cannotAddDuplicate",  "ResourceRef"));
		}
		return this.addValue(RESOURCE_REF, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeResourceRef(ResourceRef value){
		return this.removeValue(RESOURCE_REF, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeResourceRef(ResourceRef value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(RESOURCE_REF, value, overwrite);
	}

	public ResourceRef getResourceRefByRef(String id) {
	 if (null != id) { id = id.trim(); }
	ResourceRef[] o = getResourceRef();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.REF)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public ApplicationRef getApplicationRef(int index) {
		return (ApplicationRef)this.getValue(APPLICATION_REF, index);
	}

	// This attribute is an array, possibly empty
	public void setApplicationRef(ApplicationRef[] value) {
		this.setValue(APPLICATION_REF, value);
	}

	// Getter Method
	public ApplicationRef[] getApplicationRef() {
		return (ApplicationRef[])this.getValues(APPLICATION_REF);
	}

	// Return the number of properties
	public int sizeApplicationRef() {
		return this.size(APPLICATION_REF);
	}

	// Add a new element returning its index in the list
	public int addApplicationRef(ApplicationRef value)
			throws ConfigException{
		return addApplicationRef(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addApplicationRef(ApplicationRef value, boolean overwrite)
			throws ConfigException{
		ApplicationRef old = getApplicationRefByRef(value.getRef());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Cluster.class).getString("cannotAddDuplicate",  "ApplicationRef"));
		}
		return this.addValue(APPLICATION_REF, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeApplicationRef(ApplicationRef value){
		return this.removeValue(APPLICATION_REF, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeApplicationRef(ApplicationRef value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(APPLICATION_REF, value, overwrite);
	}

	public ApplicationRef getApplicationRefByRef(String id) {
	 if (null != id) { id = id.trim(); }
	ApplicationRef[] o = getApplicationRef();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.REF)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public SystemProperty getSystemProperty(int index) {
		return (SystemProperty)this.getValue(SYSTEM_PROPERTY, index);
	}

	// This attribute is an array, possibly empty
	public void setSystemProperty(SystemProperty[] value) {
		this.setValue(SYSTEM_PROPERTY, value);
	}

	// Getter Method
	public SystemProperty[] getSystemProperty() {
		return (SystemProperty[])this.getValues(SYSTEM_PROPERTY);
	}

	// Return the number of properties
	public int sizeSystemProperty() {
		return this.size(SYSTEM_PROPERTY);
	}

	// Add a new element returning its index in the list
	public int addSystemProperty(SystemProperty value)
			throws ConfigException{
		return addSystemProperty(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addSystemProperty(SystemProperty value, boolean overwrite)
			throws ConfigException{
		SystemProperty old = getSystemPropertyByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Cluster.class).getString("cannotAddDuplicate",  "SystemProperty"));
		}
		return this.addValue(SYSTEM_PROPERTY, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSystemProperty(SystemProperty value){
		return this.removeValue(SYSTEM_PROPERTY, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeSystemProperty(SystemProperty value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(SYSTEM_PROPERTY, value, overwrite);
	}

	public SystemProperty getSystemPropertyByName(String id) {
	 if (null != id) { id = id.trim(); }
	SystemProperty[] o = getSystemProperty();
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
			throw new ConfigException(StringManager.getManager(Cluster.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	* Getter for Name of the Element cluster
	* @return  the Name of the Element cluster
	*/
	public String getName() {
		return getAttributeValue(ServerTags.NAME);
	}
	/**
	* Modify  the Name of the Element cluster
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.NAME, v, overwrite);
	}
	/**
	* Modify  the Name of the Element cluster
	* @param v the new value
	*/
	public void setName(String v) {
		setAttributeValue(ServerTags.NAME, v);
	}
	/**
	* Getter for ConfigRef of the Element cluster
	* @return  the ConfigRef of the Element cluster
	*/
	public String getConfigRef() {
		return getAttributeValue(ServerTags.CONFIG_REF);
	}
	/**
	* Modify  the ConfigRef of the Element cluster
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setConfigRef(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CONFIG_REF, v, overwrite);
	}
	/**
	* Modify  the ConfigRef of the Element cluster
	* @param v the new value
	*/
	public void setConfigRef(String v) {
		setAttributeValue(ServerTags.CONFIG_REF, v);
	}
	/**
	* Getter for HeartbeatEnabled of the Element cluster
	* @return  the HeartbeatEnabled of the Element cluster
	*/
	public boolean isHeartbeatEnabled() {
		return toBoolean(getAttributeValue(ServerTags.HEARTBEAT_ENABLED));
	}
	/**
	* Modify  the HeartbeatEnabled of the Element cluster
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setHeartbeatEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.HEARTBEAT_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the HeartbeatEnabled of the Element cluster
	* @param v the new value
	*/
	public void setHeartbeatEnabled(boolean v) {
		setAttributeValue(ServerTags.HEARTBEAT_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of HeartbeatEnabled from dtd
	*/
	public static String getDefaultHeartbeatEnabled() {
		return "true".trim();
	}
	/**
	* Getter for HeartbeatPort of the Element cluster
	* @return  the HeartbeatPort of the Element cluster
	*/
	public String getHeartbeatPort() {
			return getAttributeValue(ServerTags.HEARTBEAT_PORT);
	}
	/**
	* Modify  the HeartbeatPort of the Element cluster
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setHeartbeatPort(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.HEARTBEAT_PORT, v, overwrite);
	}
	/**
	* Modify  the HeartbeatPort of the Element cluster
	* @param v the new value
	*/
	public void setHeartbeatPort(String v) {
		setAttributeValue(ServerTags.HEARTBEAT_PORT, v);
	}
	/**
	* Getter for HeartbeatAddress of the Element cluster
	* @return  the HeartbeatAddress of the Element cluster
	*/
	public String getHeartbeatAddress() {
			return getAttributeValue(ServerTags.HEARTBEAT_ADDRESS);
	}
	/**
	* Modify  the HeartbeatAddress of the Element cluster
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setHeartbeatAddress(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.HEARTBEAT_ADDRESS, v, overwrite);
	}
	/**
	* Modify  the HeartbeatAddress of the Element cluster
	* @param v the new value
	*/
	public void setHeartbeatAddress(String v) {
		setAttributeValue(ServerTags.HEARTBEAT_ADDRESS, v);
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
	public ResourceRef newResourceRef() {
		return new ResourceRef();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ApplicationRef newApplicationRef() {
		return new ApplicationRef();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SystemProperty newSystemProperty() {
		return new SystemProperty();
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
	    ret = "cluster" + (canHaveSiblings() ? "[@name='" + getAttributeValue("name") +"']" : "") ;
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.HEARTBEAT_ENABLED)) return "true".trim();
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
		str.append("ResourceRef["+this.sizeResourceRef()+"]");	// NOI18N
		for(int i=0; i<this.sizeResourceRef(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getResourceRef(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(RESOURCE_REF, i, str, indent);
		}

		str.append(indent);
		str.append("ApplicationRef["+this.sizeApplicationRef()+"]");	// NOI18N
		for(int i=0; i<this.sizeApplicationRef(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getApplicationRef(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(APPLICATION_REF, i, str, indent);
		}

		str.append(indent);
		str.append("SystemProperty["+this.sizeSystemProperty()+"]");	// NOI18N
		for(int i=0; i<this.sizeSystemProperty(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getSystemProperty(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SYSTEM_PROPERTY, i, str, indent);
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
		str.append("Cluster\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

