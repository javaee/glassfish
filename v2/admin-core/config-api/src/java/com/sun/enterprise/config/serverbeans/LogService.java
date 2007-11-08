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
 *	This generated bean class LogService matches the DTD element log-service
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

public class LogService extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String MODULE_LOG_LEVELS = "ModuleLogLevels";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public LogService() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public LogService(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("module-log-levels", MODULE_LOG_LEVELS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ModuleLogLevels.class);
		this.createAttribute(MODULE_LOG_LEVELS, "root", "Root", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "server", "Server", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "ejb-container", "EjbContainer", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "cmp-container", "CmpContainer", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "mdb-container", "MdbContainer", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "web-container", "WebContainer", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "classloader", "Classloader", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "configuration", "Configuration", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "naming", "Naming", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "security", "Security", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "jts", "Jts", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "jta", "Jta", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "admin", "Admin", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "deployment", "Deployment", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "verifier", "Verifier", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "jaxr", "Jaxr", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "jaxrpc", "Jaxrpc", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "saaj", "Saaj", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "corba", "Corba", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "javamail", "Javamail", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "jms", "Jms", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "connector", "Connector", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "jdo", "Jdo", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "cmp", "Cmp", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "util", "Util", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "resource-adapter", "ResourceAdapter", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "synchronization", "Synchronization", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "node-agent", "NodeAgent", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "self-management", "SelfManagement", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "group-management-service", "GroupManagementService", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(MODULE_LOG_LEVELS, "management-event", "ManagementEvent", 
						AttrProp.CDATA,
						null, "INFO");
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
	public void setModuleLogLevels(ModuleLogLevels value) {
		this.setValue(MODULE_LOG_LEVELS, value);
	}

	// Get Method
	public ModuleLogLevels getModuleLogLevels() {
		return (ModuleLogLevels)this.getValue(MODULE_LOG_LEVELS);
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
			throw new ConfigException(StringManager.getManager(LogService.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	* Getter for File of the Element log-service
	* @return  the File of the Element log-service
	*/
	public String getFile() {
			return getAttributeValue(ServerTags.FILE);
	}
	/**
	* Modify  the File of the Element log-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setFile(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.FILE, v, overwrite);
	}
	/**
	* Modify  the File of the Element log-service
	* @param v the new value
	*/
	public void setFile(String v) {
		setAttributeValue(ServerTags.FILE, v);
	}
	/**
	* Getter for UseSystemLogging of the Element log-service
	* @return  the UseSystemLogging of the Element log-service
	*/
	public boolean isUseSystemLogging() {
		return toBoolean(getAttributeValue(ServerTags.USE_SYSTEM_LOGGING));
	}
	/**
	* Modify  the UseSystemLogging of the Element log-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setUseSystemLogging(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.USE_SYSTEM_LOGGING, ""+(v==true), overwrite);
	}
	/**
	* Modify  the UseSystemLogging of the Element log-service
	* @param v the new value
	*/
	public void setUseSystemLogging(boolean v) {
		setAttributeValue(ServerTags.USE_SYSTEM_LOGGING, ""+(v==true));
	}
	/**
	* Get the default value of UseSystemLogging from dtd
	*/
	public static String getDefaultUseSystemLogging() {
		return "false".trim();
	}
	/**
	* Getter for LogHandler of the Element log-service
	* @return  the LogHandler of the Element log-service
	*/
	public String getLogHandler() {
			return getAttributeValue(ServerTags.LOG_HANDLER);
	}
	/**
	* Modify  the LogHandler of the Element log-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setLogHandler(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.LOG_HANDLER, v, overwrite);
	}
	/**
	* Modify  the LogHandler of the Element log-service
	* @param v the new value
	*/
	public void setLogHandler(String v) {
		setAttributeValue(ServerTags.LOG_HANDLER, v);
	}
	/**
	* Getter for LogFilter of the Element log-service
	* @return  the LogFilter of the Element log-service
	*/
	public String getLogFilter() {
			return getAttributeValue(ServerTags.LOG_FILTER);
	}
	/**
	* Modify  the LogFilter of the Element log-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setLogFilter(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.LOG_FILTER, v, overwrite);
	}
	/**
	* Modify  the LogFilter of the Element log-service
	* @param v the new value
	*/
	public void setLogFilter(String v) {
		setAttributeValue(ServerTags.LOG_FILTER, v);
	}
	/**
	* Getter for LogToConsole of the Element log-service
	* @return  the LogToConsole of the Element log-service
	*/
	public boolean isLogToConsole() {
		return toBoolean(getAttributeValue(ServerTags.LOG_TO_CONSOLE));
	}
	/**
	* Modify  the LogToConsole of the Element log-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setLogToConsole(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.LOG_TO_CONSOLE, ""+(v==true), overwrite);
	}
	/**
	* Modify  the LogToConsole of the Element log-service
	* @param v the new value
	*/
	public void setLogToConsole(boolean v) {
		setAttributeValue(ServerTags.LOG_TO_CONSOLE, ""+(v==true));
	}
	/**
	* Get the default value of LogToConsole from dtd
	*/
	public static String getDefaultLogToConsole() {
		return "false".trim();
	}
	/**
	* Getter for LogRotationLimitInBytes of the Element log-service
	* @return  the LogRotationLimitInBytes of the Element log-service
	*/
	public String getLogRotationLimitInBytes() {
		return getAttributeValue(ServerTags.LOG_ROTATION_LIMIT_IN_BYTES);
	}
	/**
	* Modify  the LogRotationLimitInBytes of the Element log-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setLogRotationLimitInBytes(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.LOG_ROTATION_LIMIT_IN_BYTES, v, overwrite);
	}
	/**
	* Modify  the LogRotationLimitInBytes of the Element log-service
	* @param v the new value
	*/
	public void setLogRotationLimitInBytes(String v) {
		setAttributeValue(ServerTags.LOG_ROTATION_LIMIT_IN_BYTES, v);
	}
	/**
	* Get the default value of LogRotationLimitInBytes from dtd
	*/
	public static String getDefaultLogRotationLimitInBytes() {
		return "500000".trim();
	}
	/**
	* Getter for LogRotationTimelimitInMinutes of the Element log-service
	* @return  the LogRotationTimelimitInMinutes of the Element log-service
	*/
	public String getLogRotationTimelimitInMinutes() {
		return getAttributeValue(ServerTags.LOG_ROTATION_TIMELIMIT_IN_MINUTES);
	}
	/**
	* Modify  the LogRotationTimelimitInMinutes of the Element log-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setLogRotationTimelimitInMinutes(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.LOG_ROTATION_TIMELIMIT_IN_MINUTES, v, overwrite);
	}
	/**
	* Modify  the LogRotationTimelimitInMinutes of the Element log-service
	* @param v the new value
	*/
	public void setLogRotationTimelimitInMinutes(String v) {
		setAttributeValue(ServerTags.LOG_ROTATION_TIMELIMIT_IN_MINUTES, v);
	}
	/**
	* Get the default value of LogRotationTimelimitInMinutes from dtd
	*/
	public static String getDefaultLogRotationTimelimitInMinutes() {
		return "0".trim();
	}
	/**
	* Getter for Alarms of the Element log-service
	* @return  the Alarms of the Element log-service
	*/
	public boolean isAlarms() {
		return toBoolean(getAttributeValue(ServerTags.ALARMS));
	}
	/**
	* Modify  the Alarms of the Element log-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAlarms(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ALARMS, ""+(v==true), overwrite);
	}
	/**
	* Modify  the Alarms of the Element log-service
	* @param v the new value
	*/
	public void setAlarms(boolean v) {
		setAttributeValue(ServerTags.ALARMS, ""+(v==true));
	}
	/**
	* Get the default value of Alarms from dtd
	*/
	public static String getDefaultAlarms() {
		return "false".trim();
	}
	/**
	* Getter for RetainErrorStatisticsForHours of the Element log-service
	* @return  the RetainErrorStatisticsForHours of the Element log-service
	*/
	public String getRetainErrorStatisticsForHours() {
		return getAttributeValue(ServerTags.RETAIN_ERROR_STATISTICS_FOR_HOURS);
	}
	/**
	* Modify  the RetainErrorStatisticsForHours of the Element log-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setRetainErrorStatisticsForHours(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.RETAIN_ERROR_STATISTICS_FOR_HOURS, v, overwrite);
	}
	/**
	* Modify  the RetainErrorStatisticsForHours of the Element log-service
	* @param v the new value
	*/
	public void setRetainErrorStatisticsForHours(String v) {
		setAttributeValue(ServerTags.RETAIN_ERROR_STATISTICS_FOR_HOURS, v);
	}
	/**
	* Get the default value of RetainErrorStatisticsForHours from dtd
	*/
	public static String getDefaultRetainErrorStatisticsForHours() {
		return "5".trim();
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ModuleLogLevels newModuleLogLevels() {
		return new ModuleLogLevels();
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
	    ret = "log-service";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.USE_SYSTEM_LOGGING)) return "false".trim();
		if(attr.equals(ServerTags.LOG_TO_CONSOLE)) return "false".trim();
		if(attr.equals(ServerTags.LOG_ROTATION_LIMIT_IN_BYTES)) return "500000".trim();
		if(attr.equals(ServerTags.LOG_ROTATION_TIMELIMIT_IN_MINUTES)) return "0".trim();
		if(attr.equals(ServerTags.ALARMS)) return "false".trim();
		if(attr.equals(ServerTags.RETAIN_ERROR_STATISTICS_FOR_HOURS)) return "5".trim();
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
		str.append("ModuleLogLevels");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getModuleLogLevels();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(MODULE_LOG_LEVELS, 0, str, indent);

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
		str.append("LogService\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

