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
 *	This generated bean class SecurityService matches the DTD element security-service
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

public class SecurityService extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String AUTH_REALM = "AuthRealm";
	static public final String JACC_PROVIDER = "JaccProvider";
	static public final String AUDIT_MODULE = "AuditModule";
	static public final String MESSAGE_SECURITY_CONFIG = "MessageSecurityConfig";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public SecurityService() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public SecurityService(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(5);
		this.createProperty("auth-realm", AUTH_REALM, 
			Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			AuthRealm.class);
		this.createAttribute(AUTH_REALM, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(AUTH_REALM, "classname", "Classname", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("jacc-provider", JACC_PROVIDER, 
			Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			JaccProvider.class);
		this.createAttribute(JACC_PROVIDER, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(JACC_PROVIDER, "policy-provider", "PolicyProvider", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(JACC_PROVIDER, "policy-configuration-factory-provider", "PolicyConfigurationFactoryProvider", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("audit-module", AUDIT_MODULE, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			AuditModule.class);
		this.createAttribute(AUDIT_MODULE, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(AUDIT_MODULE, "classname", "Classname", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("message-security-config", MESSAGE_SECURITY_CONFIG, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MessageSecurityConfig.class);
		this.createAttribute(MESSAGE_SECURITY_CONFIG, "auth-layer", "AuthLayer", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(MESSAGE_SECURITY_CONFIG, "default-provider", "DefaultProvider", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(MESSAGE_SECURITY_CONFIG, "default-client-provider", "DefaultClientProvider", 
						AttrProp.CDATA | AttrProp.IMPLIED,
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
	public AuthRealm getAuthRealm(int index) {
		return (AuthRealm)this.getValue(AUTH_REALM, index);
	}

	// This attribute is an array containing at least one element
	public void setAuthRealm(AuthRealm[] value) {
		this.setValue(AUTH_REALM, value);
	}

	// Getter Method
	public AuthRealm[] getAuthRealm() {
		return (AuthRealm[])this.getValues(AUTH_REALM);
	}

	// Return the number of properties
	public int sizeAuthRealm() {
		return this.size(AUTH_REALM);
	}

	// Add a new element returning its index in the list
	public int addAuthRealm(AuthRealm value)
			throws ConfigException{
		return addAuthRealm(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addAuthRealm(AuthRealm value, boolean overwrite)
			throws ConfigException{
		AuthRealm old = getAuthRealmByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(SecurityService.class).getString("cannotAddDuplicate",  "AuthRealm"));
		}
		return this.addValue(AUTH_REALM, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeAuthRealm(AuthRealm value){
		return this.removeValue(AUTH_REALM, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeAuthRealm(AuthRealm value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(AUTH_REALM, value, overwrite);
	}

	public AuthRealm getAuthRealmByName(String id) {
	 if (null != id) { id = id.trim(); }
	AuthRealm[] o = getAuthRealm();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public JaccProvider getJaccProvider(int index) {
		return (JaccProvider)this.getValue(JACC_PROVIDER, index);
	}

	// This attribute is an array containing at least one element
	public void setJaccProvider(JaccProvider[] value) {
		this.setValue(JACC_PROVIDER, value);
	}

	// Getter Method
	public JaccProvider[] getJaccProvider() {
		return (JaccProvider[])this.getValues(JACC_PROVIDER);
	}

	// Return the number of properties
	public int sizeJaccProvider() {
		return this.size(JACC_PROVIDER);
	}

	// Add a new element returning its index in the list
	public int addJaccProvider(JaccProvider value)
			throws ConfigException{
		return addJaccProvider(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addJaccProvider(JaccProvider value, boolean overwrite)
			throws ConfigException{
		JaccProvider old = getJaccProviderByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(SecurityService.class).getString("cannotAddDuplicate",  "JaccProvider"));
		}
		return this.addValue(JACC_PROVIDER, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeJaccProvider(JaccProvider value){
		return this.removeValue(JACC_PROVIDER, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeJaccProvider(JaccProvider value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(JACC_PROVIDER, value, overwrite);
	}

	public JaccProvider getJaccProviderByName(String id) {
	 if (null != id) { id = id.trim(); }
	JaccProvider[] o = getJaccProvider();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public AuditModule getAuditModule(int index) {
		return (AuditModule)this.getValue(AUDIT_MODULE, index);
	}

	// This attribute is an array, possibly empty
	public void setAuditModule(AuditModule[] value) {
		this.setValue(AUDIT_MODULE, value);
	}

	// Getter Method
	public AuditModule[] getAuditModule() {
		return (AuditModule[])this.getValues(AUDIT_MODULE);
	}

	// Return the number of properties
	public int sizeAuditModule() {
		return this.size(AUDIT_MODULE);
	}

	// Add a new element returning its index in the list
	public int addAuditModule(AuditModule value)
			throws ConfigException{
		return addAuditModule(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addAuditModule(AuditModule value, boolean overwrite)
			throws ConfigException{
		AuditModule old = getAuditModuleByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(SecurityService.class).getString("cannotAddDuplicate",  "AuditModule"));
		}
		return this.addValue(AUDIT_MODULE, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeAuditModule(AuditModule value){
		return this.removeValue(AUDIT_MODULE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeAuditModule(AuditModule value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(AUDIT_MODULE, value, overwrite);
	}

	public AuditModule getAuditModuleByName(String id) {
	 if (null != id) { id = id.trim(); }
	AuditModule[] o = getAuditModule();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public MessageSecurityConfig getMessageSecurityConfig(int index) {
		return (MessageSecurityConfig)this.getValue(MESSAGE_SECURITY_CONFIG, index);
	}

	// This attribute is an array, possibly empty
	public void setMessageSecurityConfig(MessageSecurityConfig[] value) {
		this.setValue(MESSAGE_SECURITY_CONFIG, value);
	}

	// Getter Method
	public MessageSecurityConfig[] getMessageSecurityConfig() {
		return (MessageSecurityConfig[])this.getValues(MESSAGE_SECURITY_CONFIG);
	}

	// Return the number of properties
	public int sizeMessageSecurityConfig() {
		return this.size(MESSAGE_SECURITY_CONFIG);
	}

	// Add a new element returning its index in the list
	public int addMessageSecurityConfig(MessageSecurityConfig value)
			throws ConfigException{
		return addMessageSecurityConfig(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addMessageSecurityConfig(MessageSecurityConfig value, boolean overwrite)
			throws ConfigException{
		MessageSecurityConfig old = getMessageSecurityConfigByAuthLayer(value.getAuthLayer());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(SecurityService.class).getString("cannotAddDuplicate",  "MessageSecurityConfig"));
		}
		return this.addValue(MESSAGE_SECURITY_CONFIG, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeMessageSecurityConfig(MessageSecurityConfig value){
		return this.removeValue(MESSAGE_SECURITY_CONFIG, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeMessageSecurityConfig(MessageSecurityConfig value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(MESSAGE_SECURITY_CONFIG, value, overwrite);
	}

	public MessageSecurityConfig getMessageSecurityConfigByAuthLayer(String id) {
	 if (null != id) { id = id.trim(); }
	MessageSecurityConfig[] o = getMessageSecurityConfig();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.AUTH_LAYER)).equals(id)) {
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
			throw new ConfigException(StringManager.getManager(SecurityService.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	* Getter for DefaultRealm of the Element security-service
	* @return  the DefaultRealm of the Element security-service
	*/
	public String getDefaultRealm() {
		return getAttributeValue(ServerTags.DEFAULT_REALM);
	}
	/**
	* Modify  the DefaultRealm of the Element security-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDefaultRealm(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DEFAULT_REALM, v, overwrite);
	}
	/**
	* Modify  the DefaultRealm of the Element security-service
	* @param v the new value
	*/
	public void setDefaultRealm(String v) {
		setAttributeValue(ServerTags.DEFAULT_REALM, v);
	}
	/**
	* Get the default value of DefaultRealm from dtd
	*/
	public static String getDefaultDefaultRealm() {
		return "file".trim();
	}
	/**
	* Getter for DefaultPrincipal of the Element security-service
	* @return  the DefaultPrincipal of the Element security-service
	*/
	public String getDefaultPrincipal() {
			return getAttributeValue(ServerTags.DEFAULT_PRINCIPAL);
	}
	/**
	* Modify  the DefaultPrincipal of the Element security-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDefaultPrincipal(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DEFAULT_PRINCIPAL, v, overwrite);
	}
	/**
	* Modify  the DefaultPrincipal of the Element security-service
	* @param v the new value
	*/
	public void setDefaultPrincipal(String v) {
		setAttributeValue(ServerTags.DEFAULT_PRINCIPAL, v);
	}
	/**
	* Getter for DefaultPrincipalPassword of the Element security-service
	* @return  the DefaultPrincipalPassword of the Element security-service
	*/
	public String getDefaultPrincipalPassword() {
			return getAttributeValue(ServerTags.DEFAULT_PRINCIPAL_PASSWORD);
	}
	/**
	* Modify  the DefaultPrincipalPassword of the Element security-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDefaultPrincipalPassword(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DEFAULT_PRINCIPAL_PASSWORD, v, overwrite);
	}
	/**
	* Modify  the DefaultPrincipalPassword of the Element security-service
	* @param v the new value
	*/
	public void setDefaultPrincipalPassword(String v) {
		setAttributeValue(ServerTags.DEFAULT_PRINCIPAL_PASSWORD, v);
	}
	/**
	* Getter for AnonymousRole of the Element security-service
	* @return  the AnonymousRole of the Element security-service
	*/
	public String getAnonymousRole() {
		return getAttributeValue(ServerTags.ANONYMOUS_ROLE);
	}
	/**
	* Modify  the AnonymousRole of the Element security-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAnonymousRole(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ANONYMOUS_ROLE, v, overwrite);
	}
	/**
	* Modify  the AnonymousRole of the Element security-service
	* @param v the new value
	*/
	public void setAnonymousRole(String v) {
		setAttributeValue(ServerTags.ANONYMOUS_ROLE, v);
	}
	/**
	* Get the default value of AnonymousRole from dtd
	*/
	public static String getDefaultAnonymousRole() {
		return "AttributeDeprecated".trim();
	}
	/**
	* Getter for AuditEnabled of the Element security-service
	* @return  the AuditEnabled of the Element security-service
	*/
	public boolean isAuditEnabled() {
		return toBoolean(getAttributeValue(ServerTags.AUDIT_ENABLED));
	}
	/**
	* Modify  the AuditEnabled of the Element security-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAuditEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.AUDIT_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the AuditEnabled of the Element security-service
	* @param v the new value
	*/
	public void setAuditEnabled(boolean v) {
		setAttributeValue(ServerTags.AUDIT_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of AuditEnabled from dtd
	*/
	public static String getDefaultAuditEnabled() {
		return "false".trim();
	}
	/**
	* Getter for Jacc of the Element security-service
	* @return  the Jacc of the Element security-service
	*/
	public String getJacc() {
		return getAttributeValue(ServerTags.JACC);
	}
	/**
	* Modify  the Jacc of the Element security-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setJacc(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.JACC, v, overwrite);
	}
	/**
	* Modify  the Jacc of the Element security-service
	* @param v the new value
	*/
	public void setJacc(String v) {
		setAttributeValue(ServerTags.JACC, v);
	}
	/**
	* Get the default value of Jacc from dtd
	*/
	public static String getDefaultJacc() {
		return "default".trim();
	}
	/**
	* Getter for AuditModules of the Element security-service
	* @return  the AuditModules of the Element security-service
	*/
	public String getAuditModules() {
		return getAttributeValue(ServerTags.AUDIT_MODULES);
	}
	/**
	* Modify  the AuditModules of the Element security-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAuditModules(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.AUDIT_MODULES, v, overwrite);
	}
	/**
	* Modify  the AuditModules of the Element security-service
	* @param v the new value
	*/
	public void setAuditModules(String v) {
		setAttributeValue(ServerTags.AUDIT_MODULES, v);
	}
	/**
	* Get the default value of AuditModules from dtd
	*/
	public static String getDefaultAuditModules() {
		return "default".trim();
	}
	/**
	* Getter for ActivateDefaultPrincipalToRoleMapping of the Element security-service
	* @return  the ActivateDefaultPrincipalToRoleMapping of the Element security-service
	*/
	public boolean isActivateDefaultPrincipalToRoleMapping() {
		return toBoolean(getAttributeValue(ServerTags.ACTIVATE_DEFAULT_PRINCIPAL_TO_ROLE_MAPPING));
	}
	/**
	* Modify  the ActivateDefaultPrincipalToRoleMapping of the Element security-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setActivateDefaultPrincipalToRoleMapping(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ACTIVATE_DEFAULT_PRINCIPAL_TO_ROLE_MAPPING, ""+(v==true), overwrite);
	}
	/**
	* Modify  the ActivateDefaultPrincipalToRoleMapping of the Element security-service
	* @param v the new value
	*/
	public void setActivateDefaultPrincipalToRoleMapping(boolean v) {
		setAttributeValue(ServerTags.ACTIVATE_DEFAULT_PRINCIPAL_TO_ROLE_MAPPING, ""+(v==true));
	}
	/**
	* Get the default value of ActivateDefaultPrincipalToRoleMapping from dtd
	*/
	public static String getDefaultActivateDefaultPrincipalToRoleMapping() {
		return "false".trim();
	}
	/**
	* Getter for MappedPrincipalClass of the Element security-service
	* @return  the MappedPrincipalClass of the Element security-service
	*/
	public String getMappedPrincipalClass() {
			return getAttributeValue(ServerTags.MAPPED_PRINCIPAL_CLASS);
	}
	/**
	* Modify  the MappedPrincipalClass of the Element security-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMappedPrincipalClass(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MAPPED_PRINCIPAL_CLASS, v, overwrite);
	}
	/**
	* Modify  the MappedPrincipalClass of the Element security-service
	* @param v the new value
	*/
	public void setMappedPrincipalClass(String v) {
		setAttributeValue(ServerTags.MAPPED_PRINCIPAL_CLASS, v);
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public AuthRealm newAuthRealm() {
		return new AuthRealm();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public JaccProvider newJaccProvider() {
		return new JaccProvider();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public AuditModule newAuditModule() {
		return new AuditModule();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public MessageSecurityConfig newMessageSecurityConfig() {
		return new MessageSecurityConfig();
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
	    ret = "security-service";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.DEFAULT_REALM)) return "file".trim();
		if(attr.equals(ServerTags.ANONYMOUS_ROLE)) return "AttributeDeprecated".trim();
		if(attr.equals(ServerTags.AUDIT_ENABLED)) return "false".trim();
		if(attr.equals(ServerTags.JACC)) return "default".trim();
		if(attr.equals(ServerTags.AUDIT_MODULES)) return "default".trim();
		if(attr.equals(ServerTags.ACTIVATE_DEFAULT_PRINCIPAL_TO_ROLE_MAPPING)) return "false".trim();
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
		str.append("AuthRealm["+this.sizeAuthRealm()+"]");	// NOI18N
		for(int i=0; i<this.sizeAuthRealm(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getAuthRealm(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(AUTH_REALM, i, str, indent);
		}

		str.append(indent);
		str.append("JaccProvider["+this.sizeJaccProvider()+"]");	// NOI18N
		for(int i=0; i<this.sizeJaccProvider(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getJaccProvider(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(JACC_PROVIDER, i, str, indent);
		}

		str.append(indent);
		str.append("AuditModule["+this.sizeAuditModule()+"]");	// NOI18N
		for(int i=0; i<this.sizeAuditModule(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getAuditModule(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(AUDIT_MODULE, i, str, indent);
		}

		str.append(indent);
		str.append("MessageSecurityConfig["+this.sizeMessageSecurityConfig()+"]");	// NOI18N
		for(int i=0; i<this.sizeMessageSecurityConfig(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getMessageSecurityConfig(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MESSAGE_SECURITY_CONFIG, i, str, indent);
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
		str.append("SecurityService\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

