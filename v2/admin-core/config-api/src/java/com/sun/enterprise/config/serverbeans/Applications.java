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
 *	This generated bean class Applications matches the DTD element applications
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

public class Applications extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String LIFECYCLE_MODULE = "LifecycleModule";
	static public final String J2EE_APPLICATION = "J2eeApplication";
	static public final String EJB_MODULE = "EjbModule";
	static public final String WEB_MODULE = "WebModule";
	static public final String CONNECTOR_MODULE = "ConnectorModule";
	static public final String APPCLIENT_MODULE = "AppclientModule";
	static public final String MBEAN = "Mbean";
	static public final String EXTENSION_MODULE = "ExtensionModule";

	public Applications() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Applications(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(8);
		this.createProperty("lifecycle-module", LIFECYCLE_MODULE, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			LifecycleModule.class);
		this.createAttribute(LIFECYCLE_MODULE, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(LIFECYCLE_MODULE, "class-name", "ClassName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(LIFECYCLE_MODULE, "classpath", "Classpath", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(LIFECYCLE_MODULE, "load-order", "LoadOrder", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(LIFECYCLE_MODULE, "is-failure-fatal", "IsFailureFatal", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(LIFECYCLE_MODULE, "object-type", "ObjectType", 
						AttrProp.CDATA,
						null, "user");
		this.createAttribute(LIFECYCLE_MODULE, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createProperty("j2ee-application", J2EE_APPLICATION, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			J2eeApplication.class);
		this.createAttribute(J2EE_APPLICATION, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(J2EE_APPLICATION, "location", "Location", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(J2EE_APPLICATION, "object-type", "ObjectType", 
						AttrProp.CDATA,
						null, "user");
		this.createAttribute(J2EE_APPLICATION, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(J2EE_APPLICATION, "libraries", "Libraries", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(J2EE_APPLICATION, "availability-enabled", "AvailabilityEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(J2EE_APPLICATION, "directory-deployed", "DirectoryDeployed", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(J2EE_APPLICATION, "java-web-start-enabled", "JavaWebStartEnabled", 
						AttrProp.CDATA,
						null, "true");
		this.createProperty("ejb-module", EJB_MODULE, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			EjbModule.class);
		this.createAttribute(EJB_MODULE, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(EJB_MODULE, "location", "Location", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(EJB_MODULE, "object-type", "ObjectType", 
						AttrProp.CDATA,
						null, "user");
		this.createAttribute(EJB_MODULE, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(EJB_MODULE, "libraries", "Libraries", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(EJB_MODULE, "availability-enabled", "AvailabilityEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(EJB_MODULE, "directory-deployed", "DirectoryDeployed", 
						AttrProp.CDATA,
						null, "false");
		this.createProperty("web-module", WEB_MODULE, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			WebModule.class);
		this.createAttribute(WEB_MODULE, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(WEB_MODULE, "context-root", "ContextRoot", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(WEB_MODULE, "location", "Location", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(WEB_MODULE, "object-type", "ObjectType", 
						AttrProp.CDATA,
						null, "user");
		this.createAttribute(WEB_MODULE, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(WEB_MODULE, "libraries", "Libraries", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(WEB_MODULE, "availability-enabled", "AvailabilityEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(WEB_MODULE, "directory-deployed", "DirectoryDeployed", 
						AttrProp.CDATA,
						null, "false");
		this.createProperty("connector-module", CONNECTOR_MODULE, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ConnectorModule.class);
		this.createAttribute(CONNECTOR_MODULE, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(CONNECTOR_MODULE, "location", "Location", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(CONNECTOR_MODULE, "object-type", "ObjectType", 
						AttrProp.CDATA,
						null, "user");
		this.createAttribute(CONNECTOR_MODULE, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(CONNECTOR_MODULE, "directory-deployed", "DirectoryDeployed", 
						AttrProp.CDATA,
						null, "false");
		this.createProperty("appclient-module", APPCLIENT_MODULE, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			AppclientModule.class);
		this.createAttribute(APPCLIENT_MODULE, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(APPCLIENT_MODULE, "location", "Location", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(APPCLIENT_MODULE, "directory-deployed", "DirectoryDeployed", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(APPCLIENT_MODULE, "java-web-start-enabled", "JavaWebStartEnabled", 
						AttrProp.CDATA,
						null, "true");
		this.createProperty("mbean", MBEAN, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Mbean.class);
		this.createAttribute(MBEAN, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(MBEAN, "object-type", "ObjectType", 
						AttrProp.CDATA,
						null, "user");
		this.createAttribute(MBEAN, "impl-class-name", "ImplClassName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(MBEAN, "object-name", "ObjectName", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(MBEAN, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createProperty("extension-module", EXTENSION_MODULE, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ExtensionModule.class);
		this.createAttribute(EXTENSION_MODULE, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(EXTENSION_MODULE, "location", "Location", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(EXTENSION_MODULE, "module-type", "ModuleType", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(EXTENSION_MODULE, "object-type", "ObjectType", 
						AttrProp.CDATA,
						null, "user");
		this.createAttribute(EXTENSION_MODULE, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(EXTENSION_MODULE, "libraries", "Libraries", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(EXTENSION_MODULE, "availability-enabled", "AvailabilityEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(EXTENSION_MODULE, "directory-deployed", "DirectoryDeployed", 
						AttrProp.CDATA,
						null, "false");
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// Get Method
	public LifecycleModule getLifecycleModule(int index) {
		return (LifecycleModule)this.getValue(LIFECYCLE_MODULE, index);
	}

	// This attribute is an array, possibly empty
	public void setLifecycleModule(LifecycleModule[] value) {
		this.setValue(LIFECYCLE_MODULE, value);
	}

	// Getter Method
	public LifecycleModule[] getLifecycleModule() {
		return (LifecycleModule[])this.getValues(LIFECYCLE_MODULE);
	}

	// Return the number of properties
	public int sizeLifecycleModule() {
		return this.size(LIFECYCLE_MODULE);
	}

	// Add a new element returning its index in the list
	public int addLifecycleModule(LifecycleModule value)
			throws ConfigException{
		return addLifecycleModule(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addLifecycleModule(LifecycleModule value, boolean overwrite)
			throws ConfigException{
		LifecycleModule old = getLifecycleModuleByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Applications.class).getString("cannotAddDuplicate",  "LifecycleModule"));
		}
		return this.addValue(LIFECYCLE_MODULE, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeLifecycleModule(LifecycleModule value){
		return this.removeValue(LIFECYCLE_MODULE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeLifecycleModule(LifecycleModule value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(LIFECYCLE_MODULE, value, overwrite);
	}

	public LifecycleModule getLifecycleModuleByName(String id) {
	 if (null != id) { id = id.trim(); }
	LifecycleModule[] o = getLifecycleModule();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public J2eeApplication getJ2eeApplication(int index) {
		return (J2eeApplication)this.getValue(J2EE_APPLICATION, index);
	}

	// This attribute is an array, possibly empty
	public void setJ2eeApplication(J2eeApplication[] value) {
		this.setValue(J2EE_APPLICATION, value);
	}

	// Getter Method
	public J2eeApplication[] getJ2eeApplication() {
		return (J2eeApplication[])this.getValues(J2EE_APPLICATION);
	}

	// Return the number of properties
	public int sizeJ2eeApplication() {
		return this.size(J2EE_APPLICATION);
	}

	// Add a new element returning its index in the list
	public int addJ2eeApplication(J2eeApplication value)
			throws ConfigException{
		return addJ2eeApplication(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addJ2eeApplication(J2eeApplication value, boolean overwrite)
			throws ConfigException{
		J2eeApplication old = getJ2eeApplicationByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Applications.class).getString("cannotAddDuplicate",  "J2eeApplication"));
		}
		return this.addValue(J2EE_APPLICATION, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeJ2eeApplication(J2eeApplication value){
		return this.removeValue(J2EE_APPLICATION, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeJ2eeApplication(J2eeApplication value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(J2EE_APPLICATION, value, overwrite);
	}

	public J2eeApplication getJ2eeApplicationByName(String id) {
	 if (null != id) { id = id.trim(); }
	J2eeApplication[] o = getJ2eeApplication();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public EjbModule getEjbModule(int index) {
		return (EjbModule)this.getValue(EJB_MODULE, index);
	}

	// This attribute is an array, possibly empty
	public void setEjbModule(EjbModule[] value) {
		this.setValue(EJB_MODULE, value);
	}

	// Getter Method
	public EjbModule[] getEjbModule() {
		return (EjbModule[])this.getValues(EJB_MODULE);
	}

	// Return the number of properties
	public int sizeEjbModule() {
		return this.size(EJB_MODULE);
	}

	// Add a new element returning its index in the list
	public int addEjbModule(EjbModule value)
			throws ConfigException{
		return addEjbModule(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addEjbModule(EjbModule value, boolean overwrite)
			throws ConfigException{
		EjbModule old = getEjbModuleByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Applications.class).getString("cannotAddDuplicate",  "EjbModule"));
		}
		return this.addValue(EJB_MODULE, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeEjbModule(EjbModule value){
		return this.removeValue(EJB_MODULE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeEjbModule(EjbModule value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(EJB_MODULE, value, overwrite);
	}

	public EjbModule getEjbModuleByName(String id) {
	 if (null != id) { id = id.trim(); }
	EjbModule[] o = getEjbModule();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public WebModule getWebModule(int index) {
		return (WebModule)this.getValue(WEB_MODULE, index);
	}

	// This attribute is an array, possibly empty
	public void setWebModule(WebModule[] value) {
		this.setValue(WEB_MODULE, value);
	}

	// Getter Method
	public WebModule[] getWebModule() {
		return (WebModule[])this.getValues(WEB_MODULE);
	}

	// Return the number of properties
	public int sizeWebModule() {
		return this.size(WEB_MODULE);
	}

	// Add a new element returning its index in the list
	public int addWebModule(WebModule value)
			throws ConfigException{
		return addWebModule(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addWebModule(WebModule value, boolean overwrite)
			throws ConfigException{
		WebModule old = getWebModuleByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Applications.class).getString("cannotAddDuplicate",  "WebModule"));
		}
		return this.addValue(WEB_MODULE, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeWebModule(WebModule value){
		return this.removeValue(WEB_MODULE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeWebModule(WebModule value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(WEB_MODULE, value, overwrite);
	}

	public WebModule getWebModuleByName(String id) {
	 if (null != id) { id = id.trim(); }
	WebModule[] o = getWebModule();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public ConnectorModule getConnectorModule(int index) {
		return (ConnectorModule)this.getValue(CONNECTOR_MODULE, index);
	}

	// This attribute is an array, possibly empty
	public void setConnectorModule(ConnectorModule[] value) {
		this.setValue(CONNECTOR_MODULE, value);
	}

	// Getter Method
	public ConnectorModule[] getConnectorModule() {
		return (ConnectorModule[])this.getValues(CONNECTOR_MODULE);
	}

	// Return the number of properties
	public int sizeConnectorModule() {
		return this.size(CONNECTOR_MODULE);
	}

	// Add a new element returning its index in the list
	public int addConnectorModule(ConnectorModule value)
			throws ConfigException{
		return addConnectorModule(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addConnectorModule(ConnectorModule value, boolean overwrite)
			throws ConfigException{
		ConnectorModule old = getConnectorModuleByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Applications.class).getString("cannotAddDuplicate",  "ConnectorModule"));
		}
		return this.addValue(CONNECTOR_MODULE, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeConnectorModule(ConnectorModule value){
		return this.removeValue(CONNECTOR_MODULE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeConnectorModule(ConnectorModule value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(CONNECTOR_MODULE, value, overwrite);
	}

	public ConnectorModule getConnectorModuleByName(String id) {
	 if (null != id) { id = id.trim(); }
	ConnectorModule[] o = getConnectorModule();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public AppclientModule getAppclientModule(int index) {
		return (AppclientModule)this.getValue(APPCLIENT_MODULE, index);
	}

	// This attribute is an array, possibly empty
	public void setAppclientModule(AppclientModule[] value) {
		this.setValue(APPCLIENT_MODULE, value);
	}

	// Getter Method
	public AppclientModule[] getAppclientModule() {
		return (AppclientModule[])this.getValues(APPCLIENT_MODULE);
	}

	// Return the number of properties
	public int sizeAppclientModule() {
		return this.size(APPCLIENT_MODULE);
	}

	// Add a new element returning its index in the list
	public int addAppclientModule(AppclientModule value)
			throws ConfigException{
		return addAppclientModule(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addAppclientModule(AppclientModule value, boolean overwrite)
			throws ConfigException{
		AppclientModule old = getAppclientModuleByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Applications.class).getString("cannotAddDuplicate",  "AppclientModule"));
		}
		return this.addValue(APPCLIENT_MODULE, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeAppclientModule(AppclientModule value){
		return this.removeValue(APPCLIENT_MODULE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeAppclientModule(AppclientModule value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(APPCLIENT_MODULE, value, overwrite);
	}

	public AppclientModule getAppclientModuleByName(String id) {
	 if (null != id) { id = id.trim(); }
	AppclientModule[] o = getAppclientModule();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public Mbean getMbean(int index) {
		return (Mbean)this.getValue(MBEAN, index);
	}

	// This attribute is an array, possibly empty
	public void setMbean(Mbean[] value) {
		this.setValue(MBEAN, value);
	}

	// Getter Method
	public Mbean[] getMbean() {
		return (Mbean[])this.getValues(MBEAN);
	}

	// Return the number of properties
	public int sizeMbean() {
		return this.size(MBEAN);
	}

	// Add a new element returning its index in the list
	public int addMbean(Mbean value)
			throws ConfigException{
		return addMbean(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addMbean(Mbean value, boolean overwrite)
			throws ConfigException{
		Mbean old = getMbeanByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Applications.class).getString("cannotAddDuplicate",  "Mbean"));
		}
		return this.addValue(MBEAN, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeMbean(Mbean value){
		return this.removeValue(MBEAN, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeMbean(Mbean value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(MBEAN, value, overwrite);
	}

	public Mbean getMbeanByName(String id) {
	 if (null != id) { id = id.trim(); }
	Mbean[] o = getMbean();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public ExtensionModule getExtensionModule(int index) {
		return (ExtensionModule)this.getValue(EXTENSION_MODULE, index);
	}

	// This attribute is an array, possibly empty
	public void setExtensionModule(ExtensionModule[] value) {
		this.setValue(EXTENSION_MODULE, value);
	}

	// Getter Method
	public ExtensionModule[] getExtensionModule() {
		return (ExtensionModule[])this.getValues(EXTENSION_MODULE);
	}

	// Return the number of properties
	public int sizeExtensionModule() {
		return this.size(EXTENSION_MODULE);
	}

	// Add a new element returning its index in the list
	public int addExtensionModule(ExtensionModule value)
			throws ConfigException{
		return addExtensionModule(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addExtensionModule(ExtensionModule value, boolean overwrite)
			throws ConfigException{
		ExtensionModule old = getExtensionModuleByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Applications.class).getString("cannotAddDuplicate",  "ExtensionModule"));
		}
		return this.addValue(EXTENSION_MODULE, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeExtensionModule(ExtensionModule value){
		return this.removeValue(EXTENSION_MODULE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeExtensionModule(ExtensionModule value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(EXTENSION_MODULE, value, overwrite);
	}

	public ExtensionModule getExtensionModuleByName(String id) {
	 if (null != id) { id = id.trim(); }
	ExtensionModule[] o = getExtensionModule();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public LifecycleModule newLifecycleModule() {
		return new LifecycleModule();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public J2eeApplication newJ2eeApplication() {
		return new J2eeApplication();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public EjbModule newEjbModule() {
		return new EjbModule();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public WebModule newWebModule() {
		return new WebModule();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ConnectorModule newConnectorModule() {
		return new ConnectorModule();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public AppclientModule newAppclientModule() {
		return new AppclientModule();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public Mbean newMbean() {
		return new Mbean();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ExtensionModule newExtensionModule() {
		return new ExtensionModule();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "applications";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
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
		str.append("LifecycleModule["+this.sizeLifecycleModule()+"]");	// NOI18N
		for(int i=0; i<this.sizeLifecycleModule(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getLifecycleModule(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(LIFECYCLE_MODULE, i, str, indent);
		}

		str.append(indent);
		str.append("J2eeApplication["+this.sizeJ2eeApplication()+"]");	// NOI18N
		for(int i=0; i<this.sizeJ2eeApplication(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getJ2eeApplication(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(J2EE_APPLICATION, i, str, indent);
		}

		str.append(indent);
		str.append("EjbModule["+this.sizeEjbModule()+"]");	// NOI18N
		for(int i=0; i<this.sizeEjbModule(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getEjbModule(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(EJB_MODULE, i, str, indent);
		}

		str.append(indent);
		str.append("WebModule["+this.sizeWebModule()+"]");	// NOI18N
		for(int i=0; i<this.sizeWebModule(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getWebModule(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(WEB_MODULE, i, str, indent);
		}

		str.append(indent);
		str.append("ConnectorModule["+this.sizeConnectorModule()+"]");	// NOI18N
		for(int i=0; i<this.sizeConnectorModule(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getConnectorModule(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CONNECTOR_MODULE, i, str, indent);
		}

		str.append(indent);
		str.append("AppclientModule["+this.sizeAppclientModule()+"]");	// NOI18N
		for(int i=0; i<this.sizeAppclientModule(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getAppclientModule(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(APPCLIENT_MODULE, i, str, indent);
		}

		str.append(indent);
		str.append("Mbean["+this.sizeMbean()+"]");	// NOI18N
		for(int i=0; i<this.sizeMbean(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getMbean(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MBEAN, i, str, indent);
		}

		str.append(indent);
		str.append("ExtensionModule["+this.sizeExtensionModule()+"]");	// NOI18N
		for(int i=0; i<this.sizeExtensionModule(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getExtensionModule(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(EXTENSION_MODULE, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Applications\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

