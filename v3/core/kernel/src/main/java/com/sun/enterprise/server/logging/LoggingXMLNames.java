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
	
	public static final String alarms= "alarms";
	
	public static final String retainErrorStatisticsForHours = "retain-error-statistics-for-hours";
// logger names from DTD
	public static final String root = "root";
	public static final String server = "server";
	public static final String ejbcontainer = "ejb-container";
	public static final String cmpcontainer = "cmp-container";
	public static final String mdbcontainer = "mdb-container";
	public static final String webcontainer = "web-container";
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
	public static final String resourceadapter = "resource-adapter";
	public static final String synchronization = "synchronization";
	public static final String nodeAgent = "node-agent";
	public static final String selfmanagement = "self-management";
	public static final String groupmanagementservice = "group-management-service";
	public static final String managementevent ="management-event";
		

//mapping of the names used in domain.xml to the names used in logging.properties

	public static final Map<String , String> xmltoPropsMap = 
           new HashMap<String , String>() {{
    put(logRotationLimitInBytes, LoggingPropertyNames.logRotationLimitInBytes);
    put(logRotationTimelimitInMinutes, LoggingPropertyNames.logRotationTimelimitInMinutes);
    put(file, LoggingPropertyNames.file);
    put(logFormatter, LoggingPropertyNames.logFormatter);
    put(logHandler, LoggingPropertyNames.logHandler);
    put(useSystemLogging, LoggingPropertyNames.useSystemLogging);
    put(retainErrorStatisticsForHours, LoggingPropertyNames.retainErrorStatisticsForHours);
    put(logFilter, LoggingPropertyNames.logFilter);
    put(logToConsole, LoggingPropertyNames.logToConsole);
    put(alarms, LoggingPropertyNames.alarms);
    put(root, LogDomains.DOMAIN_ROOT+ "level");
    put(server, LogDomains.SERVER_LOGGER+ ".level");
    put(ejbcontainer, LogDomains.EJB_LOGGER+ ".level");
    put(cmpcontainer, LogDomains.CMP_LOGGER+ ".level");
    put(mdbcontainer, LogDomains.MDB_LOGGER+ ".level");
    put(webcontainer, LogDomains.WEB_LOGGER+ ".level");
    put(classloader, LogDomains.LOADER_LOGGER+ ".level");
    put(configuration, LogDomains.CONFIG_LOGGER+ ".level");
    put(naming, LogDomains.NAMING_LOGGER+ ".level");
    put(security, LogDomains.SECURITY_LOGGER+ ".level");
  //  put(jts, LogDomains.
    put(jta, LogDomains.JTA_LOGGER+ ".level");
    put(admin, LogDomains.ADMIN_LOGGER+ ".level");
    put(deployment, LogDomains.DPL_LOGGER+ ".level");
  //  put(verifier, LogDomains.
    put(jaxr, LogDomains.JAXR_LOGGER+ ".level");
    put(jaxrpc, LogDomains.JAXRPC_LOGGER+ ".level");
    put(saaj, LogDomains.SAAJ_LOGGER+ ".level");
    put(corba, LogDomains.CORBA_LOGGER+ ".level");
    put(javamail, LogDomains.JAVAMAIL_LOGGER+ ".level");
    put(jms, LogDomains.JMS_LOGGER+ ".level");
  //  put(connector, LogDomains.
    put(jdo, LogDomains.JDO_LOGGER+ ".level");
    put(cmp, LogDomains.CMP_LOGGER+ ".level");
    put(util, LogDomains.UTIL_LOGGER+ ".level");
    put(resourceadapter, LogDomains.RSR_LOGGER+ ".level");
  //  put(synchronization, LogDomains.
  //  put(nodeAgent, LogDomains.
    put(selfmanagement, LogDomains.SELF_MANAGEMENT_LOGGER+ ".level");
  //  put(groupManagementService, LogDomains.
  //  put(managementEvent, LogDomains.
}};
	
	}

