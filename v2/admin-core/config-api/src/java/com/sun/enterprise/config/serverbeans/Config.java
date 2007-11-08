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
 *	This generated bean class Config matches the DTD element config
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

public class Config extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String HTTP_SERVICE = "HttpService";
	static public final String IIOP_SERVICE = "IiopService";
	static public final String ADMIN_SERVICE = "AdminService";
	static public final String CONNECTOR_SERVICE = "ConnectorService";
	static public final String WEB_CONTAINER = "WebContainer";
	static public final String EJB_CONTAINER = "EjbContainer";
	static public final String MDB_CONTAINER = "MdbContainer";
	static public final String JMS_SERVICE = "JmsService";
	static public final String LOG_SERVICE = "LogService";
	static public final String SECURITY_SERVICE = "SecurityService";
	static public final String TRANSACTION_SERVICE = "TransactionService";
	static public final String MONITORING_SERVICE = "MonitoringService";
	static public final String DIAGNOSTIC_SERVICE = "DiagnosticService";
	static public final String JAVA_CONFIG = "JavaConfig";
	static public final String AVAILABILITY_SERVICE = "AvailabilityService";
	static public final String THREAD_POOLS = "ThreadPools";
	static public final String ALERT_SERVICE = "AlertService";
	static public final String GROUP_MANAGEMENT_SERVICE = "GroupManagementService";
	static public final String MANAGEMENT_RULES = "ManagementRules";
	static public final String SYSTEM_PROPERTY = "SystemProperty";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public Config() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Config(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(21);
		this.createProperty("http-service", HTTP_SERVICE, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			HttpService.class);
		this.createProperty("iiop-service", IIOP_SERVICE, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			IiopService.class);
		this.createAttribute(IIOP_SERVICE, "client-authentication-required", "ClientAuthenticationRequired", 
						AttrProp.CDATA,
						null, "false");
		this.createProperty("admin-service", ADMIN_SERVICE, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			AdminService.class);
		this.createAttribute(ADMIN_SERVICE, "type", "Type", 
						AttrProp.CDATA,
						null, "server");
		this.createAttribute(ADMIN_SERVICE, "system-jmx-connector-name", "SystemJmxConnectorName", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("connector-service", CONNECTOR_SERVICE, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ConnectorService.class);
		this.createAttribute(CONNECTOR_SERVICE, "shutdown-timeout-in-seconds", "ShutdownTimeoutInSeconds", 
						AttrProp.CDATA,
						null, "30");
		this.createProperty("web-container", WEB_CONTAINER, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			WebContainer.class);
		this.createProperty("ejb-container", EJB_CONTAINER, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			EjbContainer.class);
		this.createAttribute(EJB_CONTAINER, "steady-pool-size", "SteadyPoolSize", 
						AttrProp.CDATA,
						null, "32");
		this.createAttribute(EJB_CONTAINER, "pool-resize-quantity", "PoolResizeQuantity", 
						AttrProp.CDATA,
						null, "16");
		this.createAttribute(EJB_CONTAINER, "max-pool-size", "MaxPoolSize", 
						AttrProp.CDATA,
						null, "64");
		this.createAttribute(EJB_CONTAINER, "cache-resize-quantity", "CacheResizeQuantity", 
						AttrProp.CDATA,
						null, "32");
		this.createAttribute(EJB_CONTAINER, "max-cache-size", "MaxCacheSize", 
						AttrProp.CDATA,
						null, "512");
		this.createAttribute(EJB_CONTAINER, "pool-idle-timeout-in-seconds", "PoolIdleTimeoutInSeconds", 
						AttrProp.CDATA,
						null, "600");
		this.createAttribute(EJB_CONTAINER, "cache-idle-timeout-in-seconds", "CacheIdleTimeoutInSeconds", 
						AttrProp.CDATA,
						null, "600");
		this.createAttribute(EJB_CONTAINER, "removal-timeout-in-seconds", "RemovalTimeoutInSeconds", 
						AttrProp.CDATA,
						null, "5400");
		this.createAttribute(EJB_CONTAINER, "victim-selection-policy", "VictimSelectionPolicy", 
						AttrProp.CDATA,
						null, "nru");
		this.createAttribute(EJB_CONTAINER, "commit-option", "CommitOption", 
						AttrProp.CDATA,
						null, "B");
		this.createAttribute(EJB_CONTAINER, "session-store", "SessionStore", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("mdb-container", MDB_CONTAINER, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MdbContainer.class);
		this.createAttribute(MDB_CONTAINER, "steady-pool-size", "SteadyPoolSize", 
						AttrProp.CDATA,
						null, "10");
		this.createAttribute(MDB_CONTAINER, "pool-resize-quantity", "PoolResizeQuantity", 
						AttrProp.CDATA,
						null, "2");
		this.createAttribute(MDB_CONTAINER, "max-pool-size", "MaxPoolSize", 
						AttrProp.CDATA,
						null, "60");
		this.createAttribute(MDB_CONTAINER, "idle-timeout-in-seconds", "IdleTimeoutInSeconds", 
						AttrProp.CDATA,
						null, "600");
		this.createProperty("jms-service", JMS_SERVICE, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			JmsService.class);
		this.createAttribute(JMS_SERVICE, "init-timeout-in-seconds", "InitTimeoutInSeconds", 
						AttrProp.CDATA,
						null, "60");
		this.createAttribute(JMS_SERVICE, "type", "Type", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(JMS_SERVICE, "start-args", "StartArgs", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(JMS_SERVICE, "default-jms-host", "DefaultJmsHost", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(JMS_SERVICE, "reconnect-interval-in-seconds", "ReconnectIntervalInSeconds", 
						AttrProp.CDATA,
						null, "5");
		this.createAttribute(JMS_SERVICE, "reconnect-attempts", "ReconnectAttempts", 
						AttrProp.CDATA,
						null, "3");
		this.createAttribute(JMS_SERVICE, "reconnect-enabled", "ReconnectEnabled", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(JMS_SERVICE, "addresslist-behavior", "AddresslistBehavior", 
						AttrProp.CDATA,
						null, "random");
		this.createAttribute(JMS_SERVICE, "addresslist-iterations", "AddresslistIterations", 
						AttrProp.CDATA,
						null, "3");
		this.createAttribute(JMS_SERVICE, "mq-scheme", "MqScheme", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(JMS_SERVICE, "mq-service", "MqService", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("log-service", LOG_SERVICE, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			LogService.class);
		this.createAttribute(LOG_SERVICE, "file", "File", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(LOG_SERVICE, "use-system-logging", "UseSystemLogging", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(LOG_SERVICE, "log-handler", "LogHandler", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(LOG_SERVICE, "log-filter", "LogFilter", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(LOG_SERVICE, "log-to-console", "LogToConsole", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(LOG_SERVICE, "log-rotation-limit-in-bytes", "LogRotationLimitInBytes", 
						AttrProp.CDATA,
						null, "500000");
		this.createAttribute(LOG_SERVICE, "log-rotation-timelimit-in-minutes", "LogRotationTimelimitInMinutes", 
						AttrProp.CDATA,
						null, "0");
		this.createAttribute(LOG_SERVICE, "alarms", "Alarms", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(LOG_SERVICE, "retain-error-statistics-for-hours", "RetainErrorStatisticsForHours", 
						AttrProp.CDATA,
						null, "5");
		this.createProperty("security-service", SECURITY_SERVICE, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SecurityService.class);
		this.createAttribute(SECURITY_SERVICE, "default-realm", "DefaultRealm", 
						AttrProp.CDATA,
						null, "file");
		this.createAttribute(SECURITY_SERVICE, "default-principal", "DefaultPrincipal", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(SECURITY_SERVICE, "default-principal-password", "DefaultPrincipalPassword", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(SECURITY_SERVICE, "anonymous-role", "AnonymousRole", 
						AttrProp.CDATA,
						null, "AttributeDeprecated");
		this.createAttribute(SECURITY_SERVICE, "audit-enabled", "AuditEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(SECURITY_SERVICE, "jacc", "Jacc", 
						AttrProp.CDATA,
						null, "default");
		this.createAttribute(SECURITY_SERVICE, "audit-modules", "AuditModules", 
						AttrProp.CDATA,
						null, "default");
		this.createAttribute(SECURITY_SERVICE, "activate-default-principal-to-role-mapping", "ActivateDefaultPrincipalToRoleMapping", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(SECURITY_SERVICE, "mapped-principal-class", "MappedPrincipalClass", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("transaction-service", TRANSACTION_SERVICE, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			TransactionService.class);
		this.createAttribute(TRANSACTION_SERVICE, "automatic-recovery", "AutomaticRecovery", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(TRANSACTION_SERVICE, "timeout-in-seconds", "TimeoutInSeconds", 
						AttrProp.CDATA,
						null, "0");
		this.createAttribute(TRANSACTION_SERVICE, "tx-log-dir", "TxLogDir", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(TRANSACTION_SERVICE, "heuristic-decision", "HeuristicDecision", 
						AttrProp.CDATA,
						null, "rollback");
		this.createAttribute(TRANSACTION_SERVICE, "retry-timeout-in-seconds", "RetryTimeoutInSeconds", 
						AttrProp.CDATA,
						null, "600");
		this.createAttribute(TRANSACTION_SERVICE, "keypoint-interval", "KeypointInterval", 
						AttrProp.CDATA,
						null, "2048");
		this.createProperty("monitoring-service", MONITORING_SERVICE, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MonitoringService.class);
		this.createProperty("diagnostic-service", DIAGNOSTIC_SERVICE, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			DiagnosticService.class);
		this.createAttribute(DIAGNOSTIC_SERVICE, "compute-checksum", "ComputeChecksum", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(DIAGNOSTIC_SERVICE, "verify-config", "VerifyConfig", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(DIAGNOSTIC_SERVICE, "capture-install-log", "CaptureInstallLog", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(DIAGNOSTIC_SERVICE, "capture-system-info", "CaptureSystemInfo", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(DIAGNOSTIC_SERVICE, "capture-hadb-info", "CaptureHadbInfo", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(DIAGNOSTIC_SERVICE, "capture-app-dd", "CaptureAppDd", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(DIAGNOSTIC_SERVICE, "min-log-level", "MinLogLevel", 
						AttrProp.CDATA,
						null, "INFO");
		this.createAttribute(DIAGNOSTIC_SERVICE, "max-log-entries", "MaxLogEntries", 
						AttrProp.CDATA,
						null, "500");
		this.createProperty("java-config", JAVA_CONFIG, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			JavaConfig.class);
		this.createAttribute(JAVA_CONFIG, "java-home", "JavaHome", 
						AttrProp.CDATA,
						null, "${com.sun.aas.javaRoot}");
		this.createAttribute(JAVA_CONFIG, "debug-enabled", "DebugEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(JAVA_CONFIG, "debug-options", "DebugOptions", 
						AttrProp.CDATA,
						null, "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n");
		this.createAttribute(JAVA_CONFIG, "rmic-options", "RmicOptions", 
						AttrProp.CDATA,
						null, "-iiop -poa -alwaysgenerate -keepgenerated -g");
		this.createAttribute(JAVA_CONFIG, "javac-options", "JavacOptions", 
						AttrProp.CDATA,
						null, "-g");
		this.createAttribute(JAVA_CONFIG, "classpath-prefix", "ClasspathPrefix", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(JAVA_CONFIG, "classpath-suffix", "ClasspathSuffix", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(JAVA_CONFIG, "server-classpath", "ServerClasspath", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(JAVA_CONFIG, "system-classpath", "SystemClasspath", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(JAVA_CONFIG, "native-library-path-prefix", "NativeLibraryPathPrefix", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(JAVA_CONFIG, "native-library-path-suffix", "NativeLibraryPathSuffix", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(JAVA_CONFIG, "bytecode-preprocessors", "BytecodePreprocessors", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(JAVA_CONFIG, "env-classpath-ignored", "EnvClasspathIgnored", 
						AttrProp.CDATA,
						null, "true");
		this.createProperty("availability-service", AVAILABILITY_SERVICE, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			AvailabilityService.class);
		this.createAttribute(AVAILABILITY_SERVICE, "availability-enabled", "AvailabilityEnabled", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(AVAILABILITY_SERVICE, "ha-agent-hosts", "HaAgentHosts", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(AVAILABILITY_SERVICE, "ha-agent-port", "HaAgentPort", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(AVAILABILITY_SERVICE, "ha-agent-password", "HaAgentPassword", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(AVAILABILITY_SERVICE, "ha-store-name", "HaStoreName", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(AVAILABILITY_SERVICE, "auto-manage-ha-store", "AutoManageHaStore", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(AVAILABILITY_SERVICE, "store-pool-name", "StorePoolName", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(AVAILABILITY_SERVICE, "ha-store-healthcheck-enabled", "HaStoreHealthcheckEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(AVAILABILITY_SERVICE, "ha-store-healthcheck-interval-in-seconds", "HaStoreHealthcheckIntervalInSeconds", 
						AttrProp.CDATA,
						null, "5");
		this.createProperty("thread-pools", THREAD_POOLS, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ThreadPools.class);
		this.createProperty("alert-service", ALERT_SERVICE, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			AlertService.class);
		this.createProperty("group-management-service", GROUP_MANAGEMENT_SERVICE, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			GroupManagementService.class);
		this.createAttribute(GROUP_MANAGEMENT_SERVICE, "fd-protocol-max-tries", "FdProtocolMaxTries", 
						AttrProp.CDATA,
						null, "3");
		this.createAttribute(GROUP_MANAGEMENT_SERVICE, "fd-protocol-timeout-in-millis", "FdProtocolTimeoutInMillis", 
						AttrProp.CDATA,
						null, "2000");
		this.createAttribute(GROUP_MANAGEMENT_SERVICE, "merge-protocol-max-interval-in-millis", "MergeProtocolMaxIntervalInMillis", 
						AttrProp.CDATA,
						null, "10000");
		this.createAttribute(GROUP_MANAGEMENT_SERVICE, "merge-protocol-min-interval-in-millis", "MergeProtocolMinIntervalInMillis", 
						AttrProp.CDATA,
						null, "5000");
		this.createAttribute(GROUP_MANAGEMENT_SERVICE, "ping-protocol-timeout-in-millis", "PingProtocolTimeoutInMillis", 
						AttrProp.CDATA,
						null, "2000");
		this.createAttribute(GROUP_MANAGEMENT_SERVICE, "vs-protocol-timeout-in-millis", "VsProtocolTimeoutInMillis", 
						AttrProp.CDATA,
						null, "1500");
		this.createProperty("management-rules", MANAGEMENT_RULES, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ManagementRules.class);
		this.createAttribute(MANAGEMENT_RULES, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
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

	// This attribute is mandatory
	public void setHttpService(HttpService value) {
		this.setValue(HTTP_SERVICE, value);
	}

	// Get Method
	public HttpService getHttpService() {
		return (HttpService)this.getValue(HTTP_SERVICE);
	}

	// This attribute is mandatory
	public void setIiopService(IiopService value) {
		this.setValue(IIOP_SERVICE, value);
	}

	// Get Method
	public IiopService getIiopService() {
		return (IiopService)this.getValue(IIOP_SERVICE);
	}

	// This attribute is mandatory
	public void setAdminService(AdminService value) {
		this.setValue(ADMIN_SERVICE, value);
	}

	// Get Method
	public AdminService getAdminService() {
		return (AdminService)this.getValue(ADMIN_SERVICE);
	}

	// This attribute is optional
	public void setConnectorService(ConnectorService value) {
		this.setValue(CONNECTOR_SERVICE, value);
	}

	// Get Method
	public ConnectorService getConnectorService() {
		return (ConnectorService)this.getValue(CONNECTOR_SERVICE);
	}

	// This attribute is mandatory
	public void setWebContainer(WebContainer value) {
		this.setValue(WEB_CONTAINER, value);
	}

	// Get Method
	public WebContainer getWebContainer() {
		return (WebContainer)this.getValue(WEB_CONTAINER);
	}

	// This attribute is mandatory
	public void setEjbContainer(EjbContainer value) {
		this.setValue(EJB_CONTAINER, value);
	}

	// Get Method
	public EjbContainer getEjbContainer() {
		return (EjbContainer)this.getValue(EJB_CONTAINER);
	}

	// This attribute is mandatory
	public void setMdbContainer(MdbContainer value) {
		this.setValue(MDB_CONTAINER, value);
	}

	// Get Method
	public MdbContainer getMdbContainer() {
		return (MdbContainer)this.getValue(MDB_CONTAINER);
	}

	// This attribute is optional
	public void setJmsService(JmsService value) {
		this.setValue(JMS_SERVICE, value);
	}

	// Get Method
	public JmsService getJmsService() {
		return (JmsService)this.getValue(JMS_SERVICE);
	}

	// This attribute is mandatory
	public void setLogService(LogService value) {
		this.setValue(LOG_SERVICE, value);
	}

	// Get Method
	public LogService getLogService() {
		return (LogService)this.getValue(LOG_SERVICE);
	}

	// This attribute is mandatory
	public void setSecurityService(SecurityService value) {
		this.setValue(SECURITY_SERVICE, value);
	}

	// Get Method
	public SecurityService getSecurityService() {
		return (SecurityService)this.getValue(SECURITY_SERVICE);
	}

	// This attribute is mandatory
	public void setTransactionService(TransactionService value) {
		this.setValue(TRANSACTION_SERVICE, value);
	}

	// Get Method
	public TransactionService getTransactionService() {
		return (TransactionService)this.getValue(TRANSACTION_SERVICE);
	}

	// This attribute is mandatory
	public void setMonitoringService(MonitoringService value) {
		this.setValue(MONITORING_SERVICE, value);
	}

	// Get Method
	public MonitoringService getMonitoringService() {
		return (MonitoringService)this.getValue(MONITORING_SERVICE);
	}

	// This attribute is optional
	public void setDiagnosticService(DiagnosticService value) {
		this.setValue(DIAGNOSTIC_SERVICE, value);
	}

	// Get Method
	public DiagnosticService getDiagnosticService() {
		return (DiagnosticService)this.getValue(DIAGNOSTIC_SERVICE);
	}

	// This attribute is mandatory
	public void setJavaConfig(JavaConfig value) {
		this.setValue(JAVA_CONFIG, value);
	}

	// Get Method
	public JavaConfig getJavaConfig() {
		return (JavaConfig)this.getValue(JAVA_CONFIG);
	}

	// This attribute is optional
	public void setAvailabilityService(AvailabilityService value) {
		this.setValue(AVAILABILITY_SERVICE, value);
	}

	// Get Method
	public AvailabilityService getAvailabilityService() {
		return (AvailabilityService)this.getValue(AVAILABILITY_SERVICE);
	}

	// This attribute is mandatory
	public void setThreadPools(ThreadPools value) {
		this.setValue(THREAD_POOLS, value);
	}

	// Get Method
	public ThreadPools getThreadPools() {
		return (ThreadPools)this.getValue(THREAD_POOLS);
	}

	// This attribute is optional
	public void setAlertService(AlertService value) {
		this.setValue(ALERT_SERVICE, value);
	}

	// Get Method
	public AlertService getAlertService() {
		return (AlertService)this.getValue(ALERT_SERVICE);
	}

	// This attribute is optional
	public void setGroupManagementService(GroupManagementService value) {
		this.setValue(GROUP_MANAGEMENT_SERVICE, value);
	}

	// Get Method
	public GroupManagementService getGroupManagementService() {
		return (GroupManagementService)this.getValue(GROUP_MANAGEMENT_SERVICE);
	}

	// This attribute is optional
	public void setManagementRules(ManagementRules value) {
		this.setValue(MANAGEMENT_RULES, value);
	}

	// Get Method
	public ManagementRules getManagementRules() {
		return (ManagementRules)this.getValue(MANAGEMENT_RULES);
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
			throw new ConfigException(StringManager.getManager(Config.class).getString("cannotAddDuplicate",  "SystemProperty"));
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
			throw new ConfigException(StringManager.getManager(Config.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	* Getter for Name of the Element config
	* @return  the Name of the Element config
	*/
	public String getName() {
		return getAttributeValue(ServerTags.NAME);
	}
	/**
	* Modify  the Name of the Element config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.NAME, v, overwrite);
	}
	/**
	* Modify  the Name of the Element config
	* @param v the new value
	*/
	public void setName(String v) {
		setAttributeValue(ServerTags.NAME, v);
	}
	/**
	* Getter for DynamicReconfigurationEnabled of the Element config
	* @return  the DynamicReconfigurationEnabled of the Element config
	*/
	public boolean isDynamicReconfigurationEnabled() {
		return toBoolean(getAttributeValue(ServerTags.DYNAMIC_RECONFIGURATION_ENABLED));
	}
	/**
	* Modify  the DynamicReconfigurationEnabled of the Element config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDynamicReconfigurationEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DYNAMIC_RECONFIGURATION_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the DynamicReconfigurationEnabled of the Element config
	* @param v the new value
	*/
	public void setDynamicReconfigurationEnabled(boolean v) {
		setAttributeValue(ServerTags.DYNAMIC_RECONFIGURATION_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of DynamicReconfigurationEnabled from dtd
	*/
	public static String getDefaultDynamicReconfigurationEnabled() {
		return "true".trim();
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public HttpService newHttpService() {
		return new HttpService();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public IiopService newIiopService() {
		return new IiopService();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public AdminService newAdminService() {
		return new AdminService();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ConnectorService newConnectorService() {
		return new ConnectorService();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public WebContainer newWebContainer() {
		return new WebContainer();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public EjbContainer newEjbContainer() {
		return new EjbContainer();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public MdbContainer newMdbContainer() {
		return new MdbContainer();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public JmsService newJmsService() {
		return new JmsService();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public LogService newLogService() {
		return new LogService();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SecurityService newSecurityService() {
		return new SecurityService();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public TransactionService newTransactionService() {
		return new TransactionService();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public MonitoringService newMonitoringService() {
		return new MonitoringService();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public DiagnosticService newDiagnosticService() {
		return new DiagnosticService();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public JavaConfig newJavaConfig() {
		return new JavaConfig();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public AvailabilityService newAvailabilityService() {
		return new AvailabilityService();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ThreadPools newThreadPools() {
		return new ThreadPools();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public AlertService newAlertService() {
		return new AlertService();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public GroupManagementService newGroupManagementService() {
		return new GroupManagementService();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ManagementRules newManagementRules() {
		return new ManagementRules();
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
	    ret = "config" + (canHaveSiblings() ? "[@name='" + getAttributeValue("name") +"']" : "") ;
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.DYNAMIC_RECONFIGURATION_ENABLED)) return "true".trim();
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
		str.append("HttpService");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getHttpService();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(HTTP_SERVICE, 0, str, indent);

		str.append(indent);
		str.append("IiopService");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getIiopService();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(IIOP_SERVICE, 0, str, indent);

		str.append(indent);
		str.append("AdminService");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getAdminService();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(ADMIN_SERVICE, 0, str, indent);

		str.append(indent);
		str.append("ConnectorService");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getConnectorService();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CONNECTOR_SERVICE, 0, str, indent);

		str.append(indent);
		str.append("WebContainer");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getWebContainer();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(WEB_CONTAINER, 0, str, indent);

		str.append(indent);
		str.append("EjbContainer");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getEjbContainer();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(EJB_CONTAINER, 0, str, indent);

		str.append(indent);
		str.append("MdbContainer");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getMdbContainer();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(MDB_CONTAINER, 0, str, indent);

		str.append(indent);
		str.append("JmsService");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getJmsService();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(JMS_SERVICE, 0, str, indent);

		str.append(indent);
		str.append("LogService");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getLogService();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(LOG_SERVICE, 0, str, indent);

		str.append(indent);
		str.append("SecurityService");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getSecurityService();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(SECURITY_SERVICE, 0, str, indent);

		str.append(indent);
		str.append("TransactionService");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getTransactionService();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(TRANSACTION_SERVICE, 0, str, indent);

		str.append(indent);
		str.append("MonitoringService");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getMonitoringService();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(MONITORING_SERVICE, 0, str, indent);

		str.append(indent);
		str.append("DiagnosticService");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getDiagnosticService();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(DIAGNOSTIC_SERVICE, 0, str, indent);

		str.append(indent);
		str.append("JavaConfig");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getJavaConfig();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(JAVA_CONFIG, 0, str, indent);

		str.append(indent);
		str.append("AvailabilityService");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getAvailabilityService();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(AVAILABILITY_SERVICE, 0, str, indent);

		str.append(indent);
		str.append("ThreadPools");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getThreadPools();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(THREAD_POOLS, 0, str, indent);

		str.append(indent);
		str.append("AlertService");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getAlertService();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(ALERT_SERVICE, 0, str, indent);

		str.append(indent);
		str.append("GroupManagementService");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getGroupManagementService();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(GROUP_MANAGEMENT_SERVICE, 0, str, indent);

		str.append(indent);
		str.append("ManagementRules");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getManagementRules();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(MANAGEMENT_RULES, 0, str, indent);

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
		str.append("Config\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

