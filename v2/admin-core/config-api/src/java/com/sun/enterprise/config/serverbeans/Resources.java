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
 *	This generated bean class Resources matches the DTD element resources
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

public class Resources extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String CUSTOM_RESOURCE = "CustomResource";
	static public final String EXTERNAL_JNDI_RESOURCE = "ExternalJndiResource";
	static public final String JDBC_RESOURCE = "JdbcResource";
	static public final String MAIL_RESOURCE = "MailResource";
	static public final String PERSISTENCE_MANAGER_FACTORY_RESOURCE = "PersistenceManagerFactoryResource";
	static public final String ADMIN_OBJECT_RESOURCE = "AdminObjectResource";
	static public final String CONNECTOR_RESOURCE = "ConnectorResource";
	static public final String RESOURCE_ADAPTER_CONFIG = "ResourceAdapterConfig";
	static public final String JDBC_CONNECTION_POOL = "JdbcConnectionPool";
	static public final String CONNECTOR_CONNECTION_POOL = "ConnectorConnectionPool";

	public Resources() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Resources(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(10);
		this.createProperty("custom-resource", CUSTOM_RESOURCE, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			CustomResource.class);
		this.createAttribute(CUSTOM_RESOURCE, "jndi-name", "JndiName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(CUSTOM_RESOURCE, "res-type", "ResType", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(CUSTOM_RESOURCE, "factory-class", "FactoryClass", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(CUSTOM_RESOURCE, "object-type", "ObjectType", 
						AttrProp.CDATA,
						null, "user");
		this.createAttribute(CUSTOM_RESOURCE, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createProperty("external-jndi-resource", EXTERNAL_JNDI_RESOURCE, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ExternalJndiResource.class);
		this.createAttribute(EXTERNAL_JNDI_RESOURCE, "jndi-name", "JndiName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(EXTERNAL_JNDI_RESOURCE, "jndi-lookup-name", "JndiLookupName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(EXTERNAL_JNDI_RESOURCE, "res-type", "ResType", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(EXTERNAL_JNDI_RESOURCE, "factory-class", "FactoryClass", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(EXTERNAL_JNDI_RESOURCE, "object-type", "ObjectType", 
						AttrProp.CDATA,
						null, "user");
		this.createAttribute(EXTERNAL_JNDI_RESOURCE, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createProperty("jdbc-resource", JDBC_RESOURCE, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			JdbcResource.class);
		this.createAttribute(JDBC_RESOURCE, "jndi-name", "JndiName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(JDBC_RESOURCE, "pool-name", "PoolName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(JDBC_RESOURCE, "object-type", "ObjectType", 
						AttrProp.CDATA,
						null, "user");
		this.createAttribute(JDBC_RESOURCE, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createProperty("mail-resource", MAIL_RESOURCE, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MailResource.class);
		this.createAttribute(MAIL_RESOURCE, "jndi-name", "JndiName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(MAIL_RESOURCE, "store-protocol", "StoreProtocol", 
						AttrProp.CDATA,
						null, "imap");
		this.createAttribute(MAIL_RESOURCE, "store-protocol-class", "StoreProtocolClass", 
						AttrProp.CDATA,
						null, "com.sun.mail.imap.IMAPStore");
		this.createAttribute(MAIL_RESOURCE, "transport-protocol", "TransportProtocol", 
						AttrProp.CDATA,
						null, "smtp");
		this.createAttribute(MAIL_RESOURCE, "transport-protocol-class", "TransportProtocolClass", 
						AttrProp.CDATA,
						null, "com.sun.mail.smtp.SMTPTransport");
		this.createAttribute(MAIL_RESOURCE, "host", "Host", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(MAIL_RESOURCE, "user", "User", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(MAIL_RESOURCE, "from", "From", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(MAIL_RESOURCE, "debug", "Debug", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(MAIL_RESOURCE, "object-type", "ObjectType", 
						AttrProp.CDATA,
						null, "user");
		this.createAttribute(MAIL_RESOURCE, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createProperty("persistence-manager-factory-resource", PERSISTENCE_MANAGER_FACTORY_RESOURCE, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PersistenceManagerFactoryResource.class);
		this.createAttribute(PERSISTENCE_MANAGER_FACTORY_RESOURCE, "jndi-name", "JndiName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(PERSISTENCE_MANAGER_FACTORY_RESOURCE, "factory-class", "FactoryClass", 
						AttrProp.CDATA,
						null, "com.sun.jdo.spi.persistence.support.sqlstore.impl.PersistenceManagerFactoryImpl");
		this.createAttribute(PERSISTENCE_MANAGER_FACTORY_RESOURCE, "jdbc-resource-jndi-name", "JdbcResourceJndiName", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(PERSISTENCE_MANAGER_FACTORY_RESOURCE, "object-type", "ObjectType", 
						AttrProp.CDATA,
						null, "user");
		this.createAttribute(PERSISTENCE_MANAGER_FACTORY_RESOURCE, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createProperty("admin-object-resource", ADMIN_OBJECT_RESOURCE, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			AdminObjectResource.class);
		this.createAttribute(ADMIN_OBJECT_RESOURCE, "jndi-name", "JndiName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(ADMIN_OBJECT_RESOURCE, "res-type", "ResType", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(ADMIN_OBJECT_RESOURCE, "res-adapter", "ResAdapter", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(ADMIN_OBJECT_RESOURCE, "object-type", "ObjectType", 
						AttrProp.CDATA,
						null, "user");
		this.createAttribute(ADMIN_OBJECT_RESOURCE, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createProperty("connector-resource", CONNECTOR_RESOURCE, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ConnectorResource.class);
		this.createAttribute(CONNECTOR_RESOURCE, "jndi-name", "JndiName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(CONNECTOR_RESOURCE, "pool-name", "PoolName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(CONNECTOR_RESOURCE, "object-type", "ObjectType", 
						AttrProp.CDATA,
						null, "user");
		this.createAttribute(CONNECTOR_RESOURCE, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createProperty("resource-adapter-config", RESOURCE_ADAPTER_CONFIG, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ResourceAdapterConfig.class);
		this.createAttribute(RESOURCE_ADAPTER_CONFIG, "name", "Name", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(RESOURCE_ADAPTER_CONFIG, "thread-pool-ids", "ThreadPoolIds", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(RESOURCE_ADAPTER_CONFIG, "object-type", "ObjectType", 
						AttrProp.CDATA,
						null, "user");
		this.createAttribute(RESOURCE_ADAPTER_CONFIG, "resource-adapter-name", "ResourceAdapterName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("jdbc-connection-pool", JDBC_CONNECTION_POOL, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			JdbcConnectionPool.class);
		this.createAttribute(JDBC_CONNECTION_POOL, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(JDBC_CONNECTION_POOL, "datasource-classname", "DatasourceClassname", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(JDBC_CONNECTION_POOL, "res-type", "ResType", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(JDBC_CONNECTION_POOL, "steady-pool-size", "SteadyPoolSize", 
						AttrProp.CDATA,
						null, "8");
		this.createAttribute(JDBC_CONNECTION_POOL, "max-pool-size", "MaxPoolSize", 
						AttrProp.CDATA,
						null, "32");
		this.createAttribute(JDBC_CONNECTION_POOL, "max-wait-time-in-millis", "MaxWaitTimeInMillis", 
						AttrProp.CDATA,
						null, "60000");
		this.createAttribute(JDBC_CONNECTION_POOL, "pool-resize-quantity", "PoolResizeQuantity", 
						AttrProp.CDATA,
						null, "2");
		this.createAttribute(JDBC_CONNECTION_POOL, "idle-timeout-in-seconds", "IdleTimeoutInSeconds", 
						AttrProp.CDATA,
						null, "300");
		this.createAttribute(JDBC_CONNECTION_POOL, "transaction-isolation-level", "TransactionIsolationLevel", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(JDBC_CONNECTION_POOL, "is-isolation-level-guaranteed", "IsIsolationLevelGuaranteed", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(JDBC_CONNECTION_POOL, "is-connection-validation-required", "IsConnectionValidationRequired", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(JDBC_CONNECTION_POOL, "connection-validation-method", "ConnectionValidationMethod", 
						AttrProp.CDATA,
						null, "auto-commit");
		this.createAttribute(JDBC_CONNECTION_POOL, "validation-table-name", "ValidationTableName", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(JDBC_CONNECTION_POOL, "fail-all-connections", "FailAllConnections", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(JDBC_CONNECTION_POOL, "non-transactional-connections", "NonTransactionalConnections", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(JDBC_CONNECTION_POOL, "allow-non-component-callers", "AllowNonComponentCallers", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(JDBC_CONNECTION_POOL, "validate-atmost-once-period-in-seconds", "ValidateAtmostOncePeriodInSeconds", 
						AttrProp.CDATA,
						null, "0");
		this.createAttribute(JDBC_CONNECTION_POOL, "connection-leak-timeout-in-seconds", "ConnectionLeakTimeoutInSeconds", 
						AttrProp.CDATA,
						null, "0");
		this.createAttribute(JDBC_CONNECTION_POOL, "connection-leak-reclaim", "ConnectionLeakReclaim", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(JDBC_CONNECTION_POOL, "connection-creation-retry-attempts", "ConnectionCreationRetryAttempts", 
						AttrProp.CDATA,
						null, "0");
		this.createAttribute(JDBC_CONNECTION_POOL, "connection-creation-retry-interval-in-seconds", "ConnectionCreationRetryIntervalInSeconds", 
						AttrProp.CDATA,
						null, "10");
		this.createAttribute(JDBC_CONNECTION_POOL, "statement-timeout-in-seconds", "StatementTimeoutInSeconds", 
						AttrProp.CDATA,
						null, "-1");
		this.createAttribute(JDBC_CONNECTION_POOL, "lazy-connection-enlistment", "LazyConnectionEnlistment", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(JDBC_CONNECTION_POOL, "lazy-connection-association", "LazyConnectionAssociation", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(JDBC_CONNECTION_POOL, "associate-with-thread", "AssociateWithThread", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(JDBC_CONNECTION_POOL, "match-connections", "MatchConnections", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(JDBC_CONNECTION_POOL, "max-connection-usage-count", "MaxConnectionUsageCount", 
						AttrProp.CDATA,
						null, "0");
		this.createAttribute(JDBC_CONNECTION_POOL, "wrap-jdbc-objects", "WrapJdbcObjects", 
						AttrProp.CDATA,
						null, "false");
		this.createProperty("connector-connection-pool", CONNECTOR_CONNECTION_POOL, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ConnectorConnectionPool.class);
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "resource-adapter-name", "ResourceAdapterName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "connection-definition-name", "ConnectionDefinitionName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "steady-pool-size", "SteadyPoolSize", 
						AttrProp.CDATA,
						null, "8");
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "max-pool-size", "MaxPoolSize", 
						AttrProp.CDATA,
						null, "32");
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "max-wait-time-in-millis", "MaxWaitTimeInMillis", 
						AttrProp.CDATA,
						null, "60000");
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "pool-resize-quantity", "PoolResizeQuantity", 
						AttrProp.CDATA,
						null, "2");
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "idle-timeout-in-seconds", "IdleTimeoutInSeconds", 
						AttrProp.CDATA,
						null, "300");
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "fail-all-connections", "FailAllConnections", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "transaction-support", "TransactionSupport", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "is-connection-validation-required", "IsConnectionValidationRequired", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "validate-atmost-once-period-in-seconds", "ValidateAtmostOncePeriodInSeconds", 
						AttrProp.CDATA,
						null, "0");
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "connection-leak-timeout-in-seconds", "ConnectionLeakTimeoutInSeconds", 
						AttrProp.CDATA,
						null, "0");
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "connection-leak-reclaim", "ConnectionLeakReclaim", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "connection-creation-retry-attempts", "ConnectionCreationRetryAttempts", 
						AttrProp.CDATA,
						null, "0");
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "connection-creation-retry-interval-in-seconds", "ConnectionCreationRetryIntervalInSeconds", 
						AttrProp.CDATA,
						null, "10");
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "lazy-connection-enlistment", "LazyConnectionEnlistment", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "lazy-connection-association", "LazyConnectionAssociation", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "associate-with-thread", "AssociateWithThread", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "match-connections", "MatchConnections", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(CONNECTOR_CONNECTION_POOL, "max-connection-usage-count", "MaxConnectionUsageCount", 
						AttrProp.CDATA,
						null, "0");
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// Get Method
	public CustomResource getCustomResource(int index) {
		return (CustomResource)this.getValue(CUSTOM_RESOURCE, index);
	}

	// This attribute is an array, possibly empty
	public void setCustomResource(CustomResource[] value) {
		this.setValue(CUSTOM_RESOURCE, value);
	}

	// Getter Method
	public CustomResource[] getCustomResource() {
		return (CustomResource[])this.getValues(CUSTOM_RESOURCE);
	}

	// Return the number of properties
	public int sizeCustomResource() {
		return this.size(CUSTOM_RESOURCE);
	}

	// Add a new element returning its index in the list
	public int addCustomResource(CustomResource value)
			throws ConfigException{
		return addCustomResource(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addCustomResource(CustomResource value, boolean overwrite)
			throws ConfigException{
		CustomResource old = getCustomResourceByJndiName(value.getJndiName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Resources.class).getString("cannotAddDuplicate",  "CustomResource"));
		}
		return this.addValue(CUSTOM_RESOURCE, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeCustomResource(CustomResource value){
		return this.removeValue(CUSTOM_RESOURCE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeCustomResource(CustomResource value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(CUSTOM_RESOURCE, value, overwrite);
	}

	public CustomResource getCustomResourceByJndiName(String id) {
	 if (null != id) { id = id.trim(); }
	CustomResource[] o = getCustomResource();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.JNDI_NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public ExternalJndiResource getExternalJndiResource(int index) {
		return (ExternalJndiResource)this.getValue(EXTERNAL_JNDI_RESOURCE, index);
	}

	// This attribute is an array, possibly empty
	public void setExternalJndiResource(ExternalJndiResource[] value) {
		this.setValue(EXTERNAL_JNDI_RESOURCE, value);
	}

	// Getter Method
	public ExternalJndiResource[] getExternalJndiResource() {
		return (ExternalJndiResource[])this.getValues(EXTERNAL_JNDI_RESOURCE);
	}

	// Return the number of properties
	public int sizeExternalJndiResource() {
		return this.size(EXTERNAL_JNDI_RESOURCE);
	}

	// Add a new element returning its index in the list
	public int addExternalJndiResource(ExternalJndiResource value)
			throws ConfigException{
		return addExternalJndiResource(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addExternalJndiResource(ExternalJndiResource value, boolean overwrite)
			throws ConfigException{
		ExternalJndiResource old = getExternalJndiResourceByJndiName(value.getJndiName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Resources.class).getString("cannotAddDuplicate",  "ExternalJndiResource"));
		}
		return this.addValue(EXTERNAL_JNDI_RESOURCE, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeExternalJndiResource(ExternalJndiResource value){
		return this.removeValue(EXTERNAL_JNDI_RESOURCE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeExternalJndiResource(ExternalJndiResource value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(EXTERNAL_JNDI_RESOURCE, value, overwrite);
	}

	public ExternalJndiResource getExternalJndiResourceByJndiName(String id) {
	 if (null != id) { id = id.trim(); }
	ExternalJndiResource[] o = getExternalJndiResource();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.JNDI_NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public JdbcResource getJdbcResource(int index) {
		return (JdbcResource)this.getValue(JDBC_RESOURCE, index);
	}

	// This attribute is an array, possibly empty
	public void setJdbcResource(JdbcResource[] value) {
		this.setValue(JDBC_RESOURCE, value);
	}

	// Getter Method
	public JdbcResource[] getJdbcResource() {
		return (JdbcResource[])this.getValues(JDBC_RESOURCE);
	}

	// Return the number of properties
	public int sizeJdbcResource() {
		return this.size(JDBC_RESOURCE);
	}

	// Add a new element returning its index in the list
	public int addJdbcResource(JdbcResource value)
			throws ConfigException{
		return addJdbcResource(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addJdbcResource(JdbcResource value, boolean overwrite)
			throws ConfigException{
		JdbcResource old = getJdbcResourceByJndiName(value.getJndiName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Resources.class).getString("cannotAddDuplicate",  "JdbcResource"));
		}
		return this.addValue(JDBC_RESOURCE, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeJdbcResource(JdbcResource value){
		return this.removeValue(JDBC_RESOURCE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeJdbcResource(JdbcResource value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(JDBC_RESOURCE, value, overwrite);
	}

	public JdbcResource getJdbcResourceByJndiName(String id) {
	 if (null != id) { id = id.trim(); }
	JdbcResource[] o = getJdbcResource();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.JNDI_NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public MailResource getMailResource(int index) {
		return (MailResource)this.getValue(MAIL_RESOURCE, index);
	}

	// This attribute is an array, possibly empty
	public void setMailResource(MailResource[] value) {
		this.setValue(MAIL_RESOURCE, value);
	}

	// Getter Method
	public MailResource[] getMailResource() {
		return (MailResource[])this.getValues(MAIL_RESOURCE);
	}

	// Return the number of properties
	public int sizeMailResource() {
		return this.size(MAIL_RESOURCE);
	}

	// Add a new element returning its index in the list
	public int addMailResource(MailResource value)
			throws ConfigException{
		return addMailResource(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addMailResource(MailResource value, boolean overwrite)
			throws ConfigException{
		MailResource old = getMailResourceByJndiName(value.getJndiName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Resources.class).getString("cannotAddDuplicate",  "MailResource"));
		}
		return this.addValue(MAIL_RESOURCE, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeMailResource(MailResource value){
		return this.removeValue(MAIL_RESOURCE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeMailResource(MailResource value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(MAIL_RESOURCE, value, overwrite);
	}

	public MailResource getMailResourceByJndiName(String id) {
	 if (null != id) { id = id.trim(); }
	MailResource[] o = getMailResource();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.JNDI_NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public PersistenceManagerFactoryResource getPersistenceManagerFactoryResource(int index) {
		return (PersistenceManagerFactoryResource)this.getValue(PERSISTENCE_MANAGER_FACTORY_RESOURCE, index);
	}

	// This attribute is an array, possibly empty
	public void setPersistenceManagerFactoryResource(PersistenceManagerFactoryResource[] value) {
		this.setValue(PERSISTENCE_MANAGER_FACTORY_RESOURCE, value);
	}

	// Getter Method
	public PersistenceManagerFactoryResource[] getPersistenceManagerFactoryResource() {
		return (PersistenceManagerFactoryResource[])this.getValues(PERSISTENCE_MANAGER_FACTORY_RESOURCE);
	}

	// Return the number of properties
	public int sizePersistenceManagerFactoryResource() {
		return this.size(PERSISTENCE_MANAGER_FACTORY_RESOURCE);
	}

	// Add a new element returning its index in the list
	public int addPersistenceManagerFactoryResource(PersistenceManagerFactoryResource value)
			throws ConfigException{
		return addPersistenceManagerFactoryResource(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addPersistenceManagerFactoryResource(PersistenceManagerFactoryResource value, boolean overwrite)
			throws ConfigException{
		PersistenceManagerFactoryResource old = getPersistenceManagerFactoryResourceByJndiName(value.getJndiName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Resources.class).getString("cannotAddDuplicate",  "PersistenceManagerFactoryResource"));
		}
		return this.addValue(PERSISTENCE_MANAGER_FACTORY_RESOURCE, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removePersistenceManagerFactoryResource(PersistenceManagerFactoryResource value){
		return this.removeValue(PERSISTENCE_MANAGER_FACTORY_RESOURCE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removePersistenceManagerFactoryResource(PersistenceManagerFactoryResource value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(PERSISTENCE_MANAGER_FACTORY_RESOURCE, value, overwrite);
	}

	public PersistenceManagerFactoryResource getPersistenceManagerFactoryResourceByJndiName(String id) {
	 if (null != id) { id = id.trim(); }
	PersistenceManagerFactoryResource[] o = getPersistenceManagerFactoryResource();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.JNDI_NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public AdminObjectResource getAdminObjectResource(int index) {
		return (AdminObjectResource)this.getValue(ADMIN_OBJECT_RESOURCE, index);
	}

	// This attribute is an array, possibly empty
	public void setAdminObjectResource(AdminObjectResource[] value) {
		this.setValue(ADMIN_OBJECT_RESOURCE, value);
	}

	// Getter Method
	public AdminObjectResource[] getAdminObjectResource() {
		return (AdminObjectResource[])this.getValues(ADMIN_OBJECT_RESOURCE);
	}

	// Return the number of properties
	public int sizeAdminObjectResource() {
		return this.size(ADMIN_OBJECT_RESOURCE);
	}

	// Add a new element returning its index in the list
	public int addAdminObjectResource(AdminObjectResource value)
			throws ConfigException{
		return addAdminObjectResource(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addAdminObjectResource(AdminObjectResource value, boolean overwrite)
			throws ConfigException{
		AdminObjectResource old = getAdminObjectResourceByJndiName(value.getJndiName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Resources.class).getString("cannotAddDuplicate",  "AdminObjectResource"));
		}
		return this.addValue(ADMIN_OBJECT_RESOURCE, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeAdminObjectResource(AdminObjectResource value){
		return this.removeValue(ADMIN_OBJECT_RESOURCE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeAdminObjectResource(AdminObjectResource value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(ADMIN_OBJECT_RESOURCE, value, overwrite);
	}

	public AdminObjectResource getAdminObjectResourceByJndiName(String id) {
	 if (null != id) { id = id.trim(); }
	AdminObjectResource[] o = getAdminObjectResource();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.JNDI_NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public ConnectorResource getConnectorResource(int index) {
		return (ConnectorResource)this.getValue(CONNECTOR_RESOURCE, index);
	}

	// This attribute is an array, possibly empty
	public void setConnectorResource(ConnectorResource[] value) {
		this.setValue(CONNECTOR_RESOURCE, value);
	}

	// Getter Method
	public ConnectorResource[] getConnectorResource() {
		return (ConnectorResource[])this.getValues(CONNECTOR_RESOURCE);
	}

	// Return the number of properties
	public int sizeConnectorResource() {
		return this.size(CONNECTOR_RESOURCE);
	}

	// Add a new element returning its index in the list
	public int addConnectorResource(ConnectorResource value)
			throws ConfigException{
		return addConnectorResource(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addConnectorResource(ConnectorResource value, boolean overwrite)
			throws ConfigException{
		ConnectorResource old = getConnectorResourceByJndiName(value.getJndiName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Resources.class).getString("cannotAddDuplicate",  "ConnectorResource"));
		}
		return this.addValue(CONNECTOR_RESOURCE, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeConnectorResource(ConnectorResource value){
		return this.removeValue(CONNECTOR_RESOURCE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeConnectorResource(ConnectorResource value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(CONNECTOR_RESOURCE, value, overwrite);
	}

	public ConnectorResource getConnectorResourceByJndiName(String id) {
	 if (null != id) { id = id.trim(); }
	ConnectorResource[] o = getConnectorResource();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.JNDI_NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public ResourceAdapterConfig getResourceAdapterConfig(int index) {
		return (ResourceAdapterConfig)this.getValue(RESOURCE_ADAPTER_CONFIG, index);
	}

	// This attribute is an array, possibly empty
	public void setResourceAdapterConfig(ResourceAdapterConfig[] value) {
		this.setValue(RESOURCE_ADAPTER_CONFIG, value);
	}

	// Getter Method
	public ResourceAdapterConfig[] getResourceAdapterConfig() {
		return (ResourceAdapterConfig[])this.getValues(RESOURCE_ADAPTER_CONFIG);
	}

	// Return the number of properties
	public int sizeResourceAdapterConfig() {
		return this.size(RESOURCE_ADAPTER_CONFIG);
	}

	// Add a new element returning its index in the list
	public int addResourceAdapterConfig(ResourceAdapterConfig value)
			throws ConfigException{
		return addResourceAdapterConfig(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addResourceAdapterConfig(ResourceAdapterConfig value, boolean overwrite)
			throws ConfigException{
		ResourceAdapterConfig old = getResourceAdapterConfigByResourceAdapterName(value.getResourceAdapterName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Resources.class).getString("cannotAddDuplicate",  "ResourceAdapterConfig"));
		}
		return this.addValue(RESOURCE_ADAPTER_CONFIG, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeResourceAdapterConfig(ResourceAdapterConfig value){
		return this.removeValue(RESOURCE_ADAPTER_CONFIG, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeResourceAdapterConfig(ResourceAdapterConfig value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(RESOURCE_ADAPTER_CONFIG, value, overwrite);
	}

	public ResourceAdapterConfig getResourceAdapterConfigByResourceAdapterName(String id) {
	 if (null != id) { id = id.trim(); }
	ResourceAdapterConfig[] o = getResourceAdapterConfig();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.RESOURCE_ADAPTER_NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public JdbcConnectionPool getJdbcConnectionPool(int index) {
		return (JdbcConnectionPool)this.getValue(JDBC_CONNECTION_POOL, index);
	}

	// This attribute is an array, possibly empty
	public void setJdbcConnectionPool(JdbcConnectionPool[] value) {
		this.setValue(JDBC_CONNECTION_POOL, value);
	}

	// Getter Method
	public JdbcConnectionPool[] getJdbcConnectionPool() {
		return (JdbcConnectionPool[])this.getValues(JDBC_CONNECTION_POOL);
	}

	// Return the number of properties
	public int sizeJdbcConnectionPool() {
		return this.size(JDBC_CONNECTION_POOL);
	}

	// Add a new element returning its index in the list
	public int addJdbcConnectionPool(JdbcConnectionPool value)
			throws ConfigException{
		return addJdbcConnectionPool(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addJdbcConnectionPool(JdbcConnectionPool value, boolean overwrite)
			throws ConfigException{
		JdbcConnectionPool old = getJdbcConnectionPoolByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Resources.class).getString("cannotAddDuplicate",  "JdbcConnectionPool"));
		}
		return this.addValue(JDBC_CONNECTION_POOL, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeJdbcConnectionPool(JdbcConnectionPool value){
		return this.removeValue(JDBC_CONNECTION_POOL, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeJdbcConnectionPool(JdbcConnectionPool value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(JDBC_CONNECTION_POOL, value, overwrite);
	}

	public JdbcConnectionPool getJdbcConnectionPoolByName(String id) {
	 if (null != id) { id = id.trim(); }
	JdbcConnectionPool[] o = getJdbcConnectionPool();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public ConnectorConnectionPool getConnectorConnectionPool(int index) {
		return (ConnectorConnectionPool)this.getValue(CONNECTOR_CONNECTION_POOL, index);
	}

	// This attribute is an array, possibly empty
	public void setConnectorConnectionPool(ConnectorConnectionPool[] value) {
		this.setValue(CONNECTOR_CONNECTION_POOL, value);
	}

	// Getter Method
	public ConnectorConnectionPool[] getConnectorConnectionPool() {
		return (ConnectorConnectionPool[])this.getValues(CONNECTOR_CONNECTION_POOL);
	}

	// Return the number of properties
	public int sizeConnectorConnectionPool() {
		return this.size(CONNECTOR_CONNECTION_POOL);
	}

	// Add a new element returning its index in the list
	public int addConnectorConnectionPool(ConnectorConnectionPool value)
			throws ConfigException{
		return addConnectorConnectionPool(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addConnectorConnectionPool(ConnectorConnectionPool value, boolean overwrite)
			throws ConfigException{
		ConnectorConnectionPool old = getConnectorConnectionPoolByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Resources.class).getString("cannotAddDuplicate",  "ConnectorConnectionPool"));
		}
		return this.addValue(CONNECTOR_CONNECTION_POOL, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeConnectorConnectionPool(ConnectorConnectionPool value){
		return this.removeValue(CONNECTOR_CONNECTION_POOL, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeConnectorConnectionPool(ConnectorConnectionPool value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(CONNECTOR_CONNECTION_POOL, value, overwrite);
	}

	public ConnectorConnectionPool getConnectorConnectionPoolByName(String id) {
	 if (null != id) { id = id.trim(); }
	ConnectorConnectionPool[] o = getConnectorConnectionPool();
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
	public CustomResource newCustomResource() {
		return new CustomResource();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ExternalJndiResource newExternalJndiResource() {
		return new ExternalJndiResource();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public JdbcResource newJdbcResource() {
		return new JdbcResource();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public MailResource newMailResource() {
		return new MailResource();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public PersistenceManagerFactoryResource newPersistenceManagerFactoryResource() {
		return new PersistenceManagerFactoryResource();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public AdminObjectResource newAdminObjectResource() {
		return new AdminObjectResource();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ConnectorResource newConnectorResource() {
		return new ConnectorResource();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ResourceAdapterConfig newResourceAdapterConfig() {
		return new ResourceAdapterConfig();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public JdbcConnectionPool newJdbcConnectionPool() {
		return new JdbcConnectionPool();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ConnectorConnectionPool newConnectorConnectionPool() {
		return new ConnectorConnectionPool();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "resources";
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
		str.append("CustomResource["+this.sizeCustomResource()+"]");	// NOI18N
		for(int i=0; i<this.sizeCustomResource(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getCustomResource(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CUSTOM_RESOURCE, i, str, indent);
		}

		str.append(indent);
		str.append("ExternalJndiResource["+this.sizeExternalJndiResource()+"]");	// NOI18N
		for(int i=0; i<this.sizeExternalJndiResource(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getExternalJndiResource(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(EXTERNAL_JNDI_RESOURCE, i, str, indent);
		}

		str.append(indent);
		str.append("JdbcResource["+this.sizeJdbcResource()+"]");	// NOI18N
		for(int i=0; i<this.sizeJdbcResource(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getJdbcResource(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(JDBC_RESOURCE, i, str, indent);
		}

		str.append(indent);
		str.append("MailResource["+this.sizeMailResource()+"]");	// NOI18N
		for(int i=0; i<this.sizeMailResource(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getMailResource(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MAIL_RESOURCE, i, str, indent);
		}

		str.append(indent);
		str.append("PersistenceManagerFactoryResource["+this.sizePersistenceManagerFactoryResource()+"]");	// NOI18N
		for(int i=0; i<this.sizePersistenceManagerFactoryResource(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getPersistenceManagerFactoryResource(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(PERSISTENCE_MANAGER_FACTORY_RESOURCE, i, str, indent);
		}

		str.append(indent);
		str.append("AdminObjectResource["+this.sizeAdminObjectResource()+"]");	// NOI18N
		for(int i=0; i<this.sizeAdminObjectResource(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getAdminObjectResource(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(ADMIN_OBJECT_RESOURCE, i, str, indent);
		}

		str.append(indent);
		str.append("ConnectorResource["+this.sizeConnectorResource()+"]");	// NOI18N
		for(int i=0; i<this.sizeConnectorResource(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getConnectorResource(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CONNECTOR_RESOURCE, i, str, indent);
		}

		str.append(indent);
		str.append("ResourceAdapterConfig["+this.sizeResourceAdapterConfig()+"]");	// NOI18N
		for(int i=0; i<this.sizeResourceAdapterConfig(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getResourceAdapterConfig(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(RESOURCE_ADAPTER_CONFIG, i, str, indent);
		}

		str.append(indent);
		str.append("JdbcConnectionPool["+this.sizeJdbcConnectionPool()+"]");	// NOI18N
		for(int i=0; i<this.sizeJdbcConnectionPool(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getJdbcConnectionPool(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(JDBC_CONNECTION_POOL, i, str, indent);
		}

		str.append(indent);
		str.append("ConnectorConnectionPool["+this.sizeConnectorConnectionPool()+"]");	// NOI18N
		for(int i=0; i<this.sizeConnectorConnectionPool(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getConnectorConnectionPool(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CONNECTOR_CONNECTION_POOL, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Resources\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

