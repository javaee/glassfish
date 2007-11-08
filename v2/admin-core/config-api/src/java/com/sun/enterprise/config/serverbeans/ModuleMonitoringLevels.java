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
 *	This generated bean class ModuleMonitoringLevels matches the DTD element module-monitoring-levels
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

public class ModuleMonitoringLevels extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public ModuleMonitoringLevels() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ModuleMonitoringLevels(int options)
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
			throw new ConfigException(StringManager.getManager(ModuleMonitoringLevels.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	* Getter for ThreadPool of the Element module-monitoring-levels
	* @return  the ThreadPool of the Element module-monitoring-levels
	*/
	public String getThreadPool() {
		return getAttributeValue(ServerTags.THREAD_POOL);
	}
	/**
	* Modify  the ThreadPool of the Element module-monitoring-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setThreadPool(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.THREAD_POOL, v, overwrite);
	}
	/**
	* Modify  the ThreadPool of the Element module-monitoring-levels
	* @param v the new value
	*/
	public void setThreadPool(String v) {
		setAttributeValue(ServerTags.THREAD_POOL, v);
	}
	/**
	* Get the default value of ThreadPool from dtd
	*/
	public static String getDefaultThreadPool() {
		return "OFF".trim();
	}
	/**
	* Getter for Orb of the Element module-monitoring-levels
	* @return  the Orb of the Element module-monitoring-levels
	*/
	public String getOrb() {
		return getAttributeValue(ServerTags.ORB);
	}
	/**
	* Modify  the Orb of the Element module-monitoring-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setOrb(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ORB, v, overwrite);
	}
	/**
	* Modify  the Orb of the Element module-monitoring-levels
	* @param v the new value
	*/
	public void setOrb(String v) {
		setAttributeValue(ServerTags.ORB, v);
	}
	/**
	* Get the default value of Orb from dtd
	*/
	public static String getDefaultOrb() {
		return "OFF".trim();
	}
	/**
	* Getter for EjbContainer of the Element module-monitoring-levels
	* @return  the EjbContainer of the Element module-monitoring-levels
	*/
	public String getEjbContainer() {
		return getAttributeValue(ServerTags.EJB_CONTAINER);
	}
	/**
	* Modify  the EjbContainer of the Element module-monitoring-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setEjbContainer(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.EJB_CONTAINER, v, overwrite);
	}
	/**
	* Modify  the EjbContainer of the Element module-monitoring-levels
	* @param v the new value
	*/
	public void setEjbContainer(String v) {
		setAttributeValue(ServerTags.EJB_CONTAINER, v);
	}
	/**
	* Get the default value of EjbContainer from dtd
	*/
	public static String getDefaultEjbContainer() {
		return "OFF".trim();
	}
	/**
	* Getter for WebContainer of the Element module-monitoring-levels
	* @return  the WebContainer of the Element module-monitoring-levels
	*/
	public String getWebContainer() {
		return getAttributeValue(ServerTags.WEB_CONTAINER);
	}
	/**
	* Modify  the WebContainer of the Element module-monitoring-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setWebContainer(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.WEB_CONTAINER, v, overwrite);
	}
	/**
	* Modify  the WebContainer of the Element module-monitoring-levels
	* @param v the new value
	*/
	public void setWebContainer(String v) {
		setAttributeValue(ServerTags.WEB_CONTAINER, v);
	}
	/**
	* Get the default value of WebContainer from dtd
	*/
	public static String getDefaultWebContainer() {
		return "OFF".trim();
	}
	/**
	* Getter for TransactionService of the Element module-monitoring-levels
	* @return  the TransactionService of the Element module-monitoring-levels
	*/
	public String getTransactionService() {
		return getAttributeValue(ServerTags.TRANSACTION_SERVICE);
	}
	/**
	* Modify  the TransactionService of the Element module-monitoring-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setTransactionService(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.TRANSACTION_SERVICE, v, overwrite);
	}
	/**
	* Modify  the TransactionService of the Element module-monitoring-levels
	* @param v the new value
	*/
	public void setTransactionService(String v) {
		setAttributeValue(ServerTags.TRANSACTION_SERVICE, v);
	}
	/**
	* Get the default value of TransactionService from dtd
	*/
	public static String getDefaultTransactionService() {
		return "OFF".trim();
	}
	/**
	* Getter for HttpService of the Element module-monitoring-levels
	* @return  the HttpService of the Element module-monitoring-levels
	*/
	public String getHttpService() {
		return getAttributeValue(ServerTags.HTTP_SERVICE);
	}
	/**
	* Modify  the HttpService of the Element module-monitoring-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setHttpService(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.HTTP_SERVICE, v, overwrite);
	}
	/**
	* Modify  the HttpService of the Element module-monitoring-levels
	* @param v the new value
	*/
	public void setHttpService(String v) {
		setAttributeValue(ServerTags.HTTP_SERVICE, v);
	}
	/**
	* Get the default value of HttpService from dtd
	*/
	public static String getDefaultHttpService() {
		return "OFF".trim();
	}
	/**
	* Getter for JdbcConnectionPool of the Element module-monitoring-levels
	* @return  the JdbcConnectionPool of the Element module-monitoring-levels
	*/
	public String getJdbcConnectionPool() {
		return getAttributeValue(ServerTags.JDBC_CONNECTION_POOL);
	}
	/**
	* Modify  the JdbcConnectionPool of the Element module-monitoring-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setJdbcConnectionPool(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.JDBC_CONNECTION_POOL, v, overwrite);
	}
	/**
	* Modify  the JdbcConnectionPool of the Element module-monitoring-levels
	* @param v the new value
	*/
	public void setJdbcConnectionPool(String v) {
		setAttributeValue(ServerTags.JDBC_CONNECTION_POOL, v);
	}
	/**
	* Get the default value of JdbcConnectionPool from dtd
	*/
	public static String getDefaultJdbcConnectionPool() {
		return "OFF".trim();
	}
	/**
	* Getter for ConnectorConnectionPool of the Element module-monitoring-levels
	* @return  the ConnectorConnectionPool of the Element module-monitoring-levels
	*/
	public String getConnectorConnectionPool() {
		return getAttributeValue(ServerTags.CONNECTOR_CONNECTION_POOL);
	}
	/**
	* Modify  the ConnectorConnectionPool of the Element module-monitoring-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setConnectorConnectionPool(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CONNECTOR_CONNECTION_POOL, v, overwrite);
	}
	/**
	* Modify  the ConnectorConnectionPool of the Element module-monitoring-levels
	* @param v the new value
	*/
	public void setConnectorConnectionPool(String v) {
		setAttributeValue(ServerTags.CONNECTOR_CONNECTION_POOL, v);
	}
	/**
	* Get the default value of ConnectorConnectionPool from dtd
	*/
	public static String getDefaultConnectorConnectionPool() {
		return "OFF".trim();
	}
	/**
	* Getter for ConnectorService of the Element module-monitoring-levels
	* @return  the ConnectorService of the Element module-monitoring-levels
	*/
	public String getConnectorService() {
		return getAttributeValue(ServerTags.CONNECTOR_SERVICE);
	}
	/**
	* Modify  the ConnectorService of the Element module-monitoring-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setConnectorService(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CONNECTOR_SERVICE, v, overwrite);
	}
	/**
	* Modify  the ConnectorService of the Element module-monitoring-levels
	* @param v the new value
	*/
	public void setConnectorService(String v) {
		setAttributeValue(ServerTags.CONNECTOR_SERVICE, v);
	}
	/**
	* Get the default value of ConnectorService from dtd
	*/
	public static String getDefaultConnectorService() {
		return "OFF".trim();
	}
	/**
	* Getter for JmsService of the Element module-monitoring-levels
	* @return  the JmsService of the Element module-monitoring-levels
	*/
	public String getJmsService() {
		return getAttributeValue(ServerTags.JMS_SERVICE);
	}
	/**
	* Modify  the JmsService of the Element module-monitoring-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setJmsService(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.JMS_SERVICE, v, overwrite);
	}
	/**
	* Modify  the JmsService of the Element module-monitoring-levels
	* @param v the new value
	*/
	public void setJmsService(String v) {
		setAttributeValue(ServerTags.JMS_SERVICE, v);
	}
	/**
	* Get the default value of JmsService from dtd
	*/
	public static String getDefaultJmsService() {
		return "OFF".trim();
	}
	/**
	* Getter for Jvm of the Element module-monitoring-levels
	* @return  the Jvm of the Element module-monitoring-levels
	*/
	public String getJvm() {
		return getAttributeValue(ServerTags.JVM);
	}
	/**
	* Modify  the Jvm of the Element module-monitoring-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setJvm(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.JVM, v, overwrite);
	}
	/**
	* Modify  the Jvm of the Element module-monitoring-levels
	* @param v the new value
	*/
	public void setJvm(String v) {
		setAttributeValue(ServerTags.JVM, v);
	}
	/**
	* Get the default value of Jvm from dtd
	*/
	public static String getDefaultJvm() {
		return "OFF".trim();
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
	    ret = "module-monitoring-levels";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.THREAD_POOL)) return "OFF".trim();
		if(attr.equals(ServerTags.ORB)) return "OFF".trim();
		if(attr.equals(ServerTags.EJB_CONTAINER)) return "OFF".trim();
		if(attr.equals(ServerTags.WEB_CONTAINER)) return "OFF".trim();
		if(attr.equals(ServerTags.TRANSACTION_SERVICE)) return "OFF".trim();
		if(attr.equals(ServerTags.HTTP_SERVICE)) return "OFF".trim();
		if(attr.equals(ServerTags.JDBC_CONNECTION_POOL)) return "OFF".trim();
		if(attr.equals(ServerTags.CONNECTOR_CONNECTION_POOL)) return "OFF".trim();
		if(attr.equals(ServerTags.CONNECTOR_SERVICE)) return "OFF".trim();
		if(attr.equals(ServerTags.JMS_SERVICE)) return "OFF".trim();
		if(attr.equals(ServerTags.JVM)) return "OFF".trim();
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
		str.append("ModuleMonitoringLevels\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

