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
 *	This generated bean class ModuleLogLevels matches the DTD element module-log-levels
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

public class ModuleLogLevels extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public ModuleLogLevels() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ModuleLogLevels(int options)
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
			throw new ConfigException(StringManager.getManager(ModuleLogLevels.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	* Getter for Root of the Element module-log-levels
	* @return  the Root of the Element module-log-levels
	*/
	public String getRoot() {
		return getAttributeValue(ServerTags.ROOT);
	}
	/**
	* Modify  the Root of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setRoot(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ROOT, v, overwrite);
	}
	/**
	* Modify  the Root of the Element module-log-levels
	* @param v the new value
	*/
	public void setRoot(String v) {
		setAttributeValue(ServerTags.ROOT, v);
	}
	/**
	* Get the default value of Root from dtd
	*/
	public static String getDefaultRoot() {
		return "INFO".trim();
	}
	/**
	* Getter for Server of the Element module-log-levels
	* @return  the Server of the Element module-log-levels
	*/
	public String getServer() {
		return getAttributeValue(ServerTags.SERVER);
	}
	/**
	* Modify  the Server of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setServer(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SERVER, v, overwrite);
	}
	/**
	* Modify  the Server of the Element module-log-levels
	* @param v the new value
	*/
	public void setServer(String v) {
		setAttributeValue(ServerTags.SERVER, v);
	}
	/**
	* Get the default value of Server from dtd
	*/
	public static String getDefaultServer() {
		return "INFO".trim();
	}
	/**
	* Getter for EjbContainer of the Element module-log-levels
	* @return  the EjbContainer of the Element module-log-levels
	*/
	public String getEjbContainer() {
		return getAttributeValue(ServerTags.EJB_CONTAINER);
	}
	/**
	* Modify  the EjbContainer of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setEjbContainer(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.EJB_CONTAINER, v, overwrite);
	}
	/**
	* Modify  the EjbContainer of the Element module-log-levels
	* @param v the new value
	*/
	public void setEjbContainer(String v) {
		setAttributeValue(ServerTags.EJB_CONTAINER, v);
	}
	/**
	* Get the default value of EjbContainer from dtd
	*/
	public static String getDefaultEjbContainer() {
		return "INFO".trim();
	}
	/**
	* Getter for CmpContainer of the Element module-log-levels
	* @return  the CmpContainer of the Element module-log-levels
	*/
	public String getCmpContainer() {
		return getAttributeValue(ServerTags.CMP_CONTAINER);
	}
	/**
	* Modify  the CmpContainer of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setCmpContainer(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CMP_CONTAINER, v, overwrite);
	}
	/**
	* Modify  the CmpContainer of the Element module-log-levels
	* @param v the new value
	*/
	public void setCmpContainer(String v) {
		setAttributeValue(ServerTags.CMP_CONTAINER, v);
	}
	/**
	* Get the default value of CmpContainer from dtd
	*/
	public static String getDefaultCmpContainer() {
		return "INFO".trim();
	}
	/**
	* Getter for MdbContainer of the Element module-log-levels
	* @return  the MdbContainer of the Element module-log-levels
	*/
	public String getMdbContainer() {
		return getAttributeValue(ServerTags.MDB_CONTAINER);
	}
	/**
	* Modify  the MdbContainer of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMdbContainer(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MDB_CONTAINER, v, overwrite);
	}
	/**
	* Modify  the MdbContainer of the Element module-log-levels
	* @param v the new value
	*/
	public void setMdbContainer(String v) {
		setAttributeValue(ServerTags.MDB_CONTAINER, v);
	}
	/**
	* Get the default value of MdbContainer from dtd
	*/
	public static String getDefaultMdbContainer() {
		return "INFO".trim();
	}
	/**
	* Getter for WebContainer of the Element module-log-levels
	* @return  the WebContainer of the Element module-log-levels
	*/
	public String getWebContainer() {
		return getAttributeValue(ServerTags.WEB_CONTAINER);
	}
	/**
	* Modify  the WebContainer of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setWebContainer(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.WEB_CONTAINER, v, overwrite);
	}
	/**
	* Modify  the WebContainer of the Element module-log-levels
	* @param v the new value
	*/
	public void setWebContainer(String v) {
		setAttributeValue(ServerTags.WEB_CONTAINER, v);
	}
	/**
	* Get the default value of WebContainer from dtd
	*/
	public static String getDefaultWebContainer() {
		return "INFO".trim();
	}
	/**
	* Getter for Classloader of the Element module-log-levels
	* @return  the Classloader of the Element module-log-levels
	*/
	public String getClassloader() {
		return getAttributeValue(ServerTags.CLASSLOADER);
	}
	/**
	* Modify  the Classloader of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setClassloader(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CLASSLOADER, v, overwrite);
	}
	/**
	* Modify  the Classloader of the Element module-log-levels
	* @param v the new value
	*/
	public void setClassloader(String v) {
		setAttributeValue(ServerTags.CLASSLOADER, v);
	}
	/**
	* Get the default value of Classloader from dtd
	*/
	public static String getDefaultClassloader() {
		return "INFO".trim();
	}
	/**
	* Getter for Configuration of the Element module-log-levels
	* @return  the Configuration of the Element module-log-levels
	*/
	public String getConfiguration() {
		return getAttributeValue(ServerTags.CONFIGURATION);
	}
	/**
	* Modify  the Configuration of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setConfiguration(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CONFIGURATION, v, overwrite);
	}
	/**
	* Modify  the Configuration of the Element module-log-levels
	* @param v the new value
	*/
	public void setConfiguration(String v) {
		setAttributeValue(ServerTags.CONFIGURATION, v);
	}
	/**
	* Get the default value of Configuration from dtd
	*/
	public static String getDefaultConfiguration() {
		return "INFO".trim();
	}
	/**
	* Getter for Naming of the Element module-log-levels
	* @return  the Naming of the Element module-log-levels
	*/
	public String getNaming() {
		return getAttributeValue(ServerTags.NAMING);
	}
	/**
	* Modify  the Naming of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setNaming(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.NAMING, v, overwrite);
	}
	/**
	* Modify  the Naming of the Element module-log-levels
	* @param v the new value
	*/
	public void setNaming(String v) {
		setAttributeValue(ServerTags.NAMING, v);
	}
	/**
	* Get the default value of Naming from dtd
	*/
	public static String getDefaultNaming() {
		return "INFO".trim();
	}
	/**
	* Getter for Security of the Element module-log-levels
	* @return  the Security of the Element module-log-levels
	*/
	public String getSecurity() {
		return getAttributeValue(ServerTags.SECURITY);
	}
	/**
	* Modify  the Security of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSecurity(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SECURITY, v, overwrite);
	}
	/**
	* Modify  the Security of the Element module-log-levels
	* @param v the new value
	*/
	public void setSecurity(String v) {
		setAttributeValue(ServerTags.SECURITY, v);
	}
	/**
	* Get the default value of Security from dtd
	*/
	public static String getDefaultSecurity() {
		return "INFO".trim();
	}
	/**
	* Getter for Jts of the Element module-log-levels
	* @return  the Jts of the Element module-log-levels
	*/
	public String getJts() {
		return getAttributeValue(ServerTags.JTS);
	}
	/**
	* Modify  the Jts of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setJts(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.JTS, v, overwrite);
	}
	/**
	* Modify  the Jts of the Element module-log-levels
	* @param v the new value
	*/
	public void setJts(String v) {
		setAttributeValue(ServerTags.JTS, v);
	}
	/**
	* Get the default value of Jts from dtd
	*/
	public static String getDefaultJts() {
		return "INFO".trim();
	}
	/**
	* Getter for Jta of the Element module-log-levels
	* @return  the Jta of the Element module-log-levels
	*/
	public String getJta() {
		return getAttributeValue(ServerTags.JTA);
	}
	/**
	* Modify  the Jta of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setJta(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.JTA, v, overwrite);
	}
	/**
	* Modify  the Jta of the Element module-log-levels
	* @param v the new value
	*/
	public void setJta(String v) {
		setAttributeValue(ServerTags.JTA, v);
	}
	/**
	* Get the default value of Jta from dtd
	*/
	public static String getDefaultJta() {
		return "INFO".trim();
	}
	/**
	* Getter for Admin of the Element module-log-levels
	* @return  the Admin of the Element module-log-levels
	*/
	public String getAdmin() {
		return getAttributeValue(ServerTags.ADMIN);
	}
	/**
	* Modify  the Admin of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAdmin(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ADMIN, v, overwrite);
	}
	/**
	* Modify  the Admin of the Element module-log-levels
	* @param v the new value
	*/
	public void setAdmin(String v) {
		setAttributeValue(ServerTags.ADMIN, v);
	}
	/**
	* Get the default value of Admin from dtd
	*/
	public static String getDefaultAdmin() {
		return "INFO".trim();
	}
	/**
	* Getter for Deployment of the Element module-log-levels
	* @return  the Deployment of the Element module-log-levels
	*/
	public String getDeployment() {
		return getAttributeValue(ServerTags.DEPLOYMENT);
	}
	/**
	* Modify  the Deployment of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDeployment(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DEPLOYMENT, v, overwrite);
	}
	/**
	* Modify  the Deployment of the Element module-log-levels
	* @param v the new value
	*/
	public void setDeployment(String v) {
		setAttributeValue(ServerTags.DEPLOYMENT, v);
	}
	/**
	* Get the default value of Deployment from dtd
	*/
	public static String getDefaultDeployment() {
		return "INFO".trim();
	}
	/**
	* Getter for Verifier of the Element module-log-levels
	* @return  the Verifier of the Element module-log-levels
	*/
	public String getVerifier() {
		return getAttributeValue(ServerTags.VERIFIER);
	}
	/**
	* Modify  the Verifier of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setVerifier(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.VERIFIER, v, overwrite);
	}
	/**
	* Modify  the Verifier of the Element module-log-levels
	* @param v the new value
	*/
	public void setVerifier(String v) {
		setAttributeValue(ServerTags.VERIFIER, v);
	}
	/**
	* Get the default value of Verifier from dtd
	*/
	public static String getDefaultVerifier() {
		return "INFO".trim();
	}
	/**
	* Getter for Jaxr of the Element module-log-levels
	* @return  the Jaxr of the Element module-log-levels
	*/
	public String getJaxr() {
		return getAttributeValue(ServerTags.JAXR);
	}
	/**
	* Modify  the Jaxr of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setJaxr(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.JAXR, v, overwrite);
	}
	/**
	* Modify  the Jaxr of the Element module-log-levels
	* @param v the new value
	*/
	public void setJaxr(String v) {
		setAttributeValue(ServerTags.JAXR, v);
	}
	/**
	* Get the default value of Jaxr from dtd
	*/
	public static String getDefaultJaxr() {
		return "INFO".trim();
	}
	/**
	* Getter for Jaxrpc of the Element module-log-levels
	* @return  the Jaxrpc of the Element module-log-levels
	*/
	public String getJaxrpc() {
		return getAttributeValue(ServerTags.JAXRPC);
	}
	/**
	* Modify  the Jaxrpc of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setJaxrpc(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.JAXRPC, v, overwrite);
	}
	/**
	* Modify  the Jaxrpc of the Element module-log-levels
	* @param v the new value
	*/
	public void setJaxrpc(String v) {
		setAttributeValue(ServerTags.JAXRPC, v);
	}
	/**
	* Get the default value of Jaxrpc from dtd
	*/
	public static String getDefaultJaxrpc() {
		return "INFO".trim();
	}
	/**
	* Getter for Saaj of the Element module-log-levels
	* @return  the Saaj of the Element module-log-levels
	*/
	public String getSaaj() {
		return getAttributeValue(ServerTags.SAAJ);
	}
	/**
	* Modify  the Saaj of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSaaj(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SAAJ, v, overwrite);
	}
	/**
	* Modify  the Saaj of the Element module-log-levels
	* @param v the new value
	*/
	public void setSaaj(String v) {
		setAttributeValue(ServerTags.SAAJ, v);
	}
	/**
	* Get the default value of Saaj from dtd
	*/
	public static String getDefaultSaaj() {
		return "INFO".trim();
	}
	/**
	* Getter for Corba of the Element module-log-levels
	* @return  the Corba of the Element module-log-levels
	*/
	public String getCorba() {
		return getAttributeValue(ServerTags.CORBA);
	}
	/**
	* Modify  the Corba of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setCorba(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CORBA, v, overwrite);
	}
	/**
	* Modify  the Corba of the Element module-log-levels
	* @param v the new value
	*/
	public void setCorba(String v) {
		setAttributeValue(ServerTags.CORBA, v);
	}
	/**
	* Get the default value of Corba from dtd
	*/
	public static String getDefaultCorba() {
		return "INFO".trim();
	}
	/**
	* Getter for Javamail of the Element module-log-levels
	* @return  the Javamail of the Element module-log-levels
	*/
	public String getJavamail() {
		return getAttributeValue(ServerTags.JAVAMAIL);
	}
	/**
	* Modify  the Javamail of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setJavamail(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.JAVAMAIL, v, overwrite);
	}
	/**
	* Modify  the Javamail of the Element module-log-levels
	* @param v the new value
	*/
	public void setJavamail(String v) {
		setAttributeValue(ServerTags.JAVAMAIL, v);
	}
	/**
	* Get the default value of Javamail from dtd
	*/
	public static String getDefaultJavamail() {
		return "INFO".trim();
	}
	/**
	* Getter for Jms of the Element module-log-levels
	* @return  the Jms of the Element module-log-levels
	*/
	public String getJms() {
		return getAttributeValue(ServerTags.JMS);
	}
	/**
	* Modify  the Jms of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setJms(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.JMS, v, overwrite);
	}
	/**
	* Modify  the Jms of the Element module-log-levels
	* @param v the new value
	*/
	public void setJms(String v) {
		setAttributeValue(ServerTags.JMS, v);
	}
	/**
	* Get the default value of Jms from dtd
	*/
	public static String getDefaultJms() {
		return "INFO".trim();
	}
	/**
	* Getter for Connector of the Element module-log-levels
	* @return  the Connector of the Element module-log-levels
	*/
	public String getConnector() {
		return getAttributeValue(ServerTags.CONNECTOR);
	}
	/**
	* Modify  the Connector of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setConnector(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CONNECTOR, v, overwrite);
	}
	/**
	* Modify  the Connector of the Element module-log-levels
	* @param v the new value
	*/
	public void setConnector(String v) {
		setAttributeValue(ServerTags.CONNECTOR, v);
	}
	/**
	* Get the default value of Connector from dtd
	*/
	public static String getDefaultConnector() {
		return "INFO".trim();
	}
	/**
	* Getter for Jdo of the Element module-log-levels
	* @return  the Jdo of the Element module-log-levels
	*/
	public String getJdo() {
		return getAttributeValue(ServerTags.JDO);
	}
	/**
	* Modify  the Jdo of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setJdo(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.JDO, v, overwrite);
	}
	/**
	* Modify  the Jdo of the Element module-log-levels
	* @param v the new value
	*/
	public void setJdo(String v) {
		setAttributeValue(ServerTags.JDO, v);
	}
	/**
	* Get the default value of Jdo from dtd
	*/
	public static String getDefaultJdo() {
		return "INFO".trim();
	}
	/**
	* Getter for Cmp of the Element module-log-levels
	* @return  the Cmp of the Element module-log-levels
	*/
	public String getCmp() {
		return getAttributeValue(ServerTags.CMP);
	}
	/**
	* Modify  the Cmp of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setCmp(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CMP, v, overwrite);
	}
	/**
	* Modify  the Cmp of the Element module-log-levels
	* @param v the new value
	*/
	public void setCmp(String v) {
		setAttributeValue(ServerTags.CMP, v);
	}
	/**
	* Get the default value of Cmp from dtd
	*/
	public static String getDefaultCmp() {
		return "INFO".trim();
	}
	/**
	* Getter for Util of the Element module-log-levels
	* @return  the Util of the Element module-log-levels
	*/
	public String getUtil() {
		return getAttributeValue(ServerTags.UTIL);
	}
	/**
	* Modify  the Util of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setUtil(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.UTIL, v, overwrite);
	}
	/**
	* Modify  the Util of the Element module-log-levels
	* @param v the new value
	*/
	public void setUtil(String v) {
		setAttributeValue(ServerTags.UTIL, v);
	}
	/**
	* Get the default value of Util from dtd
	*/
	public static String getDefaultUtil() {
		return "INFO".trim();
	}
	/**
	* Getter for ResourceAdapter of the Element module-log-levels
	* @return  the ResourceAdapter of the Element module-log-levels
	*/
	public String getResourceAdapter() {
		return getAttributeValue(ServerTags.RESOURCE_ADAPTER);
	}
	/**
	* Modify  the ResourceAdapter of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setResourceAdapter(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.RESOURCE_ADAPTER, v, overwrite);
	}
	/**
	* Modify  the ResourceAdapter of the Element module-log-levels
	* @param v the new value
	*/
	public void setResourceAdapter(String v) {
		setAttributeValue(ServerTags.RESOURCE_ADAPTER, v);
	}
	/**
	* Get the default value of ResourceAdapter from dtd
	*/
	public static String getDefaultResourceAdapter() {
		return "INFO".trim();
	}
	/**
	* Getter for Synchronization of the Element module-log-levels
	* @return  the Synchronization of the Element module-log-levels
	*/
	public String getSynchronization() {
		return getAttributeValue(ServerTags.SYNCHRONIZATION);
	}
	/**
	* Modify  the Synchronization of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSynchronization(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SYNCHRONIZATION, v, overwrite);
	}
	/**
	* Modify  the Synchronization of the Element module-log-levels
	* @param v the new value
	*/
	public void setSynchronization(String v) {
		setAttributeValue(ServerTags.SYNCHRONIZATION, v);
	}
	/**
	* Get the default value of Synchronization from dtd
	*/
	public static String getDefaultSynchronization() {
		return "INFO".trim();
	}
	/**
	* Getter for NodeAgent of the Element module-log-levels
	* @return  the NodeAgent of the Element module-log-levels
	*/
	public String getNodeAgent() {
		return getAttributeValue(ServerTags.NODE_AGENT);
	}
	/**
	* Modify  the NodeAgent of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setNodeAgent(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.NODE_AGENT, v, overwrite);
	}
	/**
	* Modify  the NodeAgent of the Element module-log-levels
	* @param v the new value
	*/
	public void setNodeAgent(String v) {
		setAttributeValue(ServerTags.NODE_AGENT, v);
	}
	/**
	* Get the default value of NodeAgent from dtd
	*/
	public static String getDefaultNodeAgent() {
		return "INFO".trim();
	}
	/**
	* Getter for SelfManagement of the Element module-log-levels
	* @return  the SelfManagement of the Element module-log-levels
	*/
	public String getSelfManagement() {
		return getAttributeValue(ServerTags.SELF_MANAGEMENT);
	}
	/**
	* Modify  the SelfManagement of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSelfManagement(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SELF_MANAGEMENT, v, overwrite);
	}
	/**
	* Modify  the SelfManagement of the Element module-log-levels
	* @param v the new value
	*/
	public void setSelfManagement(String v) {
		setAttributeValue(ServerTags.SELF_MANAGEMENT, v);
	}
	/**
	* Get the default value of SelfManagement from dtd
	*/
	public static String getDefaultSelfManagement() {
		return "INFO".trim();
	}
	/**
	* Getter for GroupManagementService of the Element module-log-levels
	* @return  the GroupManagementService of the Element module-log-levels
	*/
	public String getGroupManagementService() {
		return getAttributeValue(ServerTags.GROUP_MANAGEMENT_SERVICE);
	}
	/**
	* Modify  the GroupManagementService of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setGroupManagementService(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.GROUP_MANAGEMENT_SERVICE, v, overwrite);
	}
	/**
	* Modify  the GroupManagementService of the Element module-log-levels
	* @param v the new value
	*/
	public void setGroupManagementService(String v) {
		setAttributeValue(ServerTags.GROUP_MANAGEMENT_SERVICE, v);
	}
	/**
	* Get the default value of GroupManagementService from dtd
	*/
	public static String getDefaultGroupManagementService() {
		return "INFO".trim();
	}
	/**
	* Getter for ManagementEvent of the Element module-log-levels
	* @return  the ManagementEvent of the Element module-log-levels
	*/
	public String getManagementEvent() {
		return getAttributeValue(ServerTags.MANAGEMENT_EVENT);
	}
	/**
	* Modify  the ManagementEvent of the Element module-log-levels
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setManagementEvent(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MANAGEMENT_EVENT, v, overwrite);
	}
	/**
	* Modify  the ManagementEvent of the Element module-log-levels
	* @param v the new value
	*/
	public void setManagementEvent(String v) {
		setAttributeValue(ServerTags.MANAGEMENT_EVENT, v);
	}
	/**
	* Get the default value of ManagementEvent from dtd
	*/
	public static String getDefaultManagementEvent() {
		return "INFO".trim();
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
	    ret = "module-log-levels";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.ROOT)) return "INFO".trim();
		if(attr.equals(ServerTags.SERVER)) return "INFO".trim();
		if(attr.equals(ServerTags.EJB_CONTAINER)) return "INFO".trim();
		if(attr.equals(ServerTags.CMP_CONTAINER)) return "INFO".trim();
		if(attr.equals(ServerTags.MDB_CONTAINER)) return "INFO".trim();
		if(attr.equals(ServerTags.WEB_CONTAINER)) return "INFO".trim();
		if(attr.equals(ServerTags.CLASSLOADER)) return "INFO".trim();
		if(attr.equals(ServerTags.CONFIGURATION)) return "INFO".trim();
		if(attr.equals(ServerTags.NAMING)) return "INFO".trim();
		if(attr.equals(ServerTags.SECURITY)) return "INFO".trim();
		if(attr.equals(ServerTags.JTS)) return "INFO".trim();
		if(attr.equals(ServerTags.JTA)) return "INFO".trim();
		if(attr.equals(ServerTags.ADMIN)) return "INFO".trim();
		if(attr.equals(ServerTags.DEPLOYMENT)) return "INFO".trim();
		if(attr.equals(ServerTags.VERIFIER)) return "INFO".trim();
		if(attr.equals(ServerTags.JAXR)) return "INFO".trim();
		if(attr.equals(ServerTags.JAXRPC)) return "INFO".trim();
		if(attr.equals(ServerTags.SAAJ)) return "INFO".trim();
		if(attr.equals(ServerTags.CORBA)) return "INFO".trim();
		if(attr.equals(ServerTags.JAVAMAIL)) return "INFO".trim();
		if(attr.equals(ServerTags.JMS)) return "INFO".trim();
		if(attr.equals(ServerTags.CONNECTOR)) return "INFO".trim();
		if(attr.equals(ServerTags.JDO)) return "INFO".trim();
		if(attr.equals(ServerTags.CMP)) return "INFO".trim();
		if(attr.equals(ServerTags.UTIL)) return "INFO".trim();
		if(attr.equals(ServerTags.RESOURCE_ADAPTER)) return "INFO".trim();
		if(attr.equals(ServerTags.SYNCHRONIZATION)) return "INFO".trim();
		if(attr.equals(ServerTags.NODE_AGENT)) return "INFO".trim();
		if(attr.equals(ServerTags.SELF_MANAGEMENT)) return "INFO".trim();
		if(attr.equals(ServerTags.GROUP_MANAGEMENT_SERVICE)) return "INFO".trim();
		if(attr.equals(ServerTags.MANAGEMENT_EVENT)) return "INFO".trim();
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
		str.append("ModuleLogLevels\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

