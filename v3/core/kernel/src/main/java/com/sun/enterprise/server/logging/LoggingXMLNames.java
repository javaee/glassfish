package com.sun.enterprise.server.logging;

import com.sun.logging.LogDomains;

import java.util.Map;
import java.util.HashMap;


public class LoggingXMLNames {
	
	public static final String file = "file";
	
	public static final String logRotationLimitInBytes = "log-rotation-limit-in-bytes";
	
	public static final String logRotationTimelimitInMinutes ="log-rotation-timelimit-in-minutes";
	
	public static String logFormatter = "log-formatter";
	
	public static final String logHandler = "log-handler";
	
	public static final String useSystemLogging	= "use-system-logging";
	
	public static final String logFilter= "log-filter";
	
	public static final String logToConsole = "log-to-console";
	
	public static final String alarms= "com.sun.enterprise.server.logging.alarms";
	
	public static final String retainErrorStatisticsForHours = "retain-error-statistics-for-hours";
// logger names 	
	public static final String root = "root";
	public static final String server = "server";
	public static final String ejbContainer = "ejb-container";
	public static final String cmpContainer = "cmp-container";
	public static final String mdbContainer = "mdb-container";
	public static final String webContainer = "web-container";
	public static final String classloader = "classloader";
	public static final String configuration = "configuration";
	public static final String naming = "naming";
	public static final String security = "security";
	public static final String jts = "jts";
	public static final String jta = "jta";
	public static final String admin = "admin";
	public static final String deployment = "deployment";
	public static final String verifier = "verifier";
	public static final String jaxr = "jaxr";
	public static final String jaxrpc ="jaxrpc";
	public static final String saaj ="saaj";
	public static final String corba = "corba";
	public static final String javamail ="javamail";
	public static final String jms ="jms";
	public static final String connector ="connector";
	public static final String jdo = "jdo";
	public static final String cmp = "cmp";
	public static final String util = "util";
	public static final String resourceAdapter = "resource-adapter";
	public static final String synchronization = "synchronization";
	public static final String nodeAgent = "node-agent";
	public static final String selfManagement = "self-management";
	public static final String groupManagementService = "group-management-service";
	public static final String managementEvent ="management-event";
		

//mapping of the names used in domain.xml to the names used in logging.properties

	public static final Map<String , String> xmltoPropsMap =            new HashMap<String , String>() {{    put(logRotationLimitInBytes, LoggingPropertyNames.logRotationLimitInBytes);    put(logRotationTimelimitInMinutes, LoggingPropertyNames.logRotationTimelimitInMinutes);    put(file, LoggingPropertyNames.file);
    put(logFormatter, LoggingPropertyNames.logFormatter);
    put(logHandler, LoggingPropertyNames.logHandler);
    put(useSystemLogging, LoggingPropertyNames.useSystemLogging);
    put(retainErrorStatisticsForHours, LoggingPropertyNames.retainErrorStatisticsForHours);
    put(logFilter, LoggingPropertyNames.logFilter);
    put(logToConsole, LoggingPropertyNames.logToConsole);
    put(alarms, LoggingPropertyNames.alarms);
    put(root, LogDomains.DOMAIN_ROOT);
    put(server, LogDomains.SERVER_LOGGER);
    put(ejbContainer, LogDomains.EJB_LOGGER);
    put(cmpContainer, LogDomains.CMP_LOGGER);
    put(mdbContainer, LogDomains.MDB_LOGGER);
    put(webContainer, LogDomains.WEB_LOGGER);
    put(classloader, LogDomains.LOADER_LOGGER);
    put(configuration, LogDomains.CONFIG_LOGGER);
    put(naming, LogDomains.NAMING_LOGGER);
    put(security, LogDomains.SECURITY_LOGGER);
  //  put(jts, LogDomains.
    put(jta, LogDomains.JTA_LOGGER);
    put(admin, LogDomains.ADMIN_LOGGER);
    put(deployment, LogDomains.DPL_LOGGER);
  //  put(verifier, LogDomains.
    put(jaxr, LogDomains.JAXR_LOGGER);
    put(jaxrpc, LogDomains.JAXRPC_LOGGER);
    put(saaj, LogDomains.SAAJ_LOGGER);
    put(corba, LogDomains.CORBA_LOGGER);
    put(javamail, LogDomains.JAVAMAIL_LOGGER);
    put(jms, LogDomains.JMS_LOGGER);
  //  put(connector, LogDomains.
    put(jdo, LogDomains.JDO_LOGGER);
    put(cmp, LogDomains.CMP_LOGGER);
    put(util, LogDomains.UTIL_LOGGER);
    put(resourceAdapter, LogDomains.RSR_LOGGER);
  //  put(synchronization, LogDomains.
  //  put(nodeAgent, LogDomains.
    put(selfManagement, LogDomains.SELF_MANAGEMENT_LOGGER);
  //  put(groupManagementService, LogDomains.
  //  put(managementEvent, LogDomains.}};
	
	}

