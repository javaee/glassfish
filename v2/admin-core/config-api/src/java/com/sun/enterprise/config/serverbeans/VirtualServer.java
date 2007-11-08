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
 *	This generated bean class VirtualServer matches the DTD element virtual-server
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

public class VirtualServer extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String HTTP_ACCESS_LOG = "HttpAccessLog";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public VirtualServer() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public VirtualServer(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("http-access-log", HTTP_ACCESS_LOG, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			HttpAccessLog.class);
		this.createAttribute(HTTP_ACCESS_LOG, "log-directory", "LogDirectory", 
						AttrProp.CDATA,
						null, "${com.sun.aas.instanceRoot}/logs/access");
		this.createAttribute(HTTP_ACCESS_LOG, "iponly", "Iponly", 
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

	// This attribute is optional
	public void setHttpAccessLog(HttpAccessLog value) {
		this.setValue(HTTP_ACCESS_LOG, value);
	}

	// Get Method
	public HttpAccessLog getHttpAccessLog() {
		return (HttpAccessLog)this.getValue(HTTP_ACCESS_LOG);
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
			throw new ConfigException(StringManager.getManager(VirtualServer.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	* Getter for Id of the Element virtual-server
	* @return  the Id of the Element virtual-server
	*/
	public String getId() {
		return getAttributeValue(ServerTags.ID);
	}
	/**
	* Modify  the Id of the Element virtual-server
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setId(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ID, v, overwrite);
	}
	/**
	* Modify  the Id of the Element virtual-server
	* @param v the new value
	*/
	public void setId(String v) {
		setAttributeValue(ServerTags.ID, v);
	}
	/**
	* Getter for HttpListeners of the Element virtual-server
	* @return  the HttpListeners of the Element virtual-server
	*/
	public String getHttpListeners() {
			return getAttributeValue(ServerTags.HTTP_LISTENERS);
	}
	/**
	* Modify  the HttpListeners of the Element virtual-server
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setHttpListeners(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.HTTP_LISTENERS, v, overwrite);
	}
	/**
	* Modify  the HttpListeners of the Element virtual-server
	* @param v the new value
	*/
	public void setHttpListeners(String v) {
		setAttributeValue(ServerTags.HTTP_LISTENERS, v);
	}
	/**
	* Getter for DefaultWebModule of the Element virtual-server
	* @return  the DefaultWebModule of the Element virtual-server
	*/
	public String getDefaultWebModule() {
			return getAttributeValue(ServerTags.DEFAULT_WEB_MODULE);
	}
	/**
	* Modify  the DefaultWebModule of the Element virtual-server
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDefaultWebModule(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DEFAULT_WEB_MODULE, v, overwrite);
	}
	/**
	* Modify  the DefaultWebModule of the Element virtual-server
	* @param v the new value
	*/
	public void setDefaultWebModule(String v) {
		setAttributeValue(ServerTags.DEFAULT_WEB_MODULE, v);
	}
	/**
	* Getter for Hosts of the Element virtual-server
	* @return  the Hosts of the Element virtual-server
	*/
	public String getHosts() {
		return getAttributeValue(ServerTags.HOSTS);
	}
	/**
	* Modify  the Hosts of the Element virtual-server
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setHosts(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.HOSTS, v, overwrite);
	}
	/**
	* Modify  the Hosts of the Element virtual-server
	* @param v the new value
	*/
	public void setHosts(String v) {
		setAttributeValue(ServerTags.HOSTS, v);
	}
	/**
	* Getter for State of the Element virtual-server
	* @return  the State of the Element virtual-server
	*/
	public String getState() {
		return getAttributeValue(ServerTags.STATE);
	}
	/**
	* Modify  the State of the Element virtual-server
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setState(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.STATE, v, overwrite);
	}
	/**
	* Modify  the State of the Element virtual-server
	* @param v the new value
	*/
	public void setState(String v) {
		setAttributeValue(ServerTags.STATE, v);
	}
	/**
	* Get the default value of State from dtd
	*/
	public static String getDefaultState() {
		return "on".trim();
	}
	/**
	* Getter for Docroot of the Element virtual-server
	* @return  the Docroot of the Element virtual-server
	*/
	public String getDocroot() {
			return getAttributeValue(ServerTags.DOCROOT);
	}
	/**
	* Modify  the Docroot of the Element virtual-server
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDocroot(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DOCROOT, v, overwrite);
	}
	/**
	* Modify  the Docroot of the Element virtual-server
	* @param v the new value
	*/
	public void setDocroot(String v) {
		setAttributeValue(ServerTags.DOCROOT, v);
	}
	/**
	* Getter for LogFile of the Element virtual-server
	* @return  the LogFile of the Element virtual-server
	*/
	public String getLogFile() {
		return getAttributeValue(ServerTags.LOG_FILE);
	}
	/**
	* Modify  the LogFile of the Element virtual-server
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setLogFile(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.LOG_FILE, v, overwrite);
	}
	/**
	* Modify  the LogFile of the Element virtual-server
	* @param v the new value
	*/
	public void setLogFile(String v) {
		setAttributeValue(ServerTags.LOG_FILE, v);
	}
	/**
	* Get the default value of LogFile from dtd
	*/
	public static String getDefaultLogFile() {
		return "${com.sun.aas.instanceRoot}/logs/server.log".trim();
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public HttpAccessLog newHttpAccessLog() {
		return new HttpAccessLog();
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
	    ret = "virtual-server" + (canHaveSiblings() ? "[@id='" + getAttributeValue("id") +"']" : "") ;
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.STATE)) return "on".trim();
		if(attr.equals(ServerTags.LOG_FILE)) return "${com.sun.aas.instanceRoot}/logs/server.log".trim();
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
		str.append("HttpAccessLog");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getHttpAccessLog();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(HTTP_ACCESS_LOG, 0, str, indent);

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
		str.append("VirtualServer\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

