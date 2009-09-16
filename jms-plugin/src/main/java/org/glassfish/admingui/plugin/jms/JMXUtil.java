/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.plugin.jms;

import java.lang.management.ManagementFactory;
import java.util.Set;
import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 *
 * @author jasonlee
 */
public class JMXUtil {

    /** Creates a new instance of JMXUtil */
    public JMXUtil() {
    }

    public static MBeanServerConnection getMBeanServer() {
            return ManagementFactory.getPlatformMBeanServer();
    }

    public static Object invoke(String objectName, String operationName) {
        return invoke(objectName, operationName, new Object[]{}, new String[]{});
    }

    public static Object invoke(String objectName, String operationName, Object[] params, String[] signature)
    {
        try{
            Object result=invoke( new ObjectName(objectName), operationName, params, signature);
            return result;
        }catch (javax.management.MalformedObjectNameException ex){
            System.out.println("MalformedObjectNameException: " + objectName);
            throw new RuntimeException(ex);
        }
    }

    public static Object invoke(ObjectName objectName, String operationName, Object[] params, String[] signature)
    {

	try {
            Object result =  getMBeanServer().invoke(objectName, operationName, params, signature);
            return result;
        } catch (Exception ex) {
            //The calling method will decide if to catch this exception or dump stack trace. ex issue#2902
            //ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static Object getAttribute(String objectName, String attributeName) {
        try{
            Object result = getAttribute(new ObjectName(objectName), attributeName);
            return result;
        } catch(MalformedObjectNameException ex) {
            return null;  //TODO-ErrorHandling
        }

    }

    public static Object getAttribute(ObjectName objectName, String attributeName) {
	try {
	    return getMBeanServer().getAttribute(objectName, attributeName);
	} catch (Exception ex) {
            return null;
	}
    }

    public static void setAttribute(String objectName, Attribute attributeName) {
	try {
	    setAttribute(new ObjectName(objectName), attributeName);
	}catch (javax.management.MalformedObjectNameException ex){
            System.out.println("MalformedObjectNameException: " + objectName);
            throw new RuntimeException(ex);
        }
    }

    public static void setAttribute(ObjectName objectName, Attribute attributeName) {
	try {
	    getMBeanServer().setAttribute(objectName, attributeName);
	} catch (Exception  ex) {
            throw new RuntimeException(ex);
        }

    }

    /**
     * Method that obtains the default values from domain
     * @param objName Object Name of Mbean
     * @param params array containing the name of attribute whose default
     * value is needed
     * @return an Object representing the default value
     */
    public static Object getDefaultAttributeValue(String objName, String[] params){
        String operName = "getDefaultAttributeValue";
        String[] signature = {"java.lang.String"};
        Object defaultValue = invoke(objName, operName, params, signature);
        return defaultValue;
    }

    public static boolean isValidMBean(String objectName) {
        boolean valid = false;
        try {
            Set beans = getMBeanServer().queryMBeans(new ObjectName(objectName), null);
            if (beans.size() > 0)
                valid = true;
        } catch (Exception ex) {
            // ignore
        }
        return valid;
    }

    public final static String DomainDiagnosticsMBeanName
        = "com.sun.appserv:type=DomainDiagnostics,name=server,category=monitor";

    public final static String DomainDiagnosticsGenerateReportMethod
        = "generateReport";

}