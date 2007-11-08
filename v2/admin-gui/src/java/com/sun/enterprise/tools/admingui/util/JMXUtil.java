/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
/*
 * JMXUtil.java
 *
 * Created on August 1, 2006, 10:03 AM
 *
 * @author anilam
 *
 */
package com.sun.enterprise.tools.admingui.util;


import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.Attribute;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.config.MBeanConfigInstanceNotFoundException;
import javax.management.AttributeList;

import com.sun.appserv.management.j2ee.StateManageable;

import com.sun.enterprise.admin.common.Status;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;

public class JMXUtil {
    
    /** Creates a new instance of JMXUtil */
    public JMXUtil() {
    }
    
    public static MBeanServerConnection getMBeanServer() {
            return MBeanServerFactory.getMBeanServer();
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
        
        /*  TODD-Logging.  Fixed after loggin mechanism is available in admin-jsf.
         *
	if (Util.isLoggableFINE()) {
	    // Log some trace info
	    NonSyncStringBuffer buf = new NonSyncStringBuffer();
	    buf.append("***** Calling MBean Server *****\n");
	    buf.append("objectName = "+objectName+"\n");
	    buf.append("operationName = "+operationName+"\n");
	    if (params != null) {
		for (int i = 0; i < params.length; i++) {
		    buf.append("params["+i+"] = "+params[i]);
		}
	    }
	    if (signature  != null) {
		for (int i = 0; i < signature.length; i++) {
		    buf.append("types["+i+"] = "+signature[i]);
		}
	    }
	    buf.append("params = "+params);
	    buf.append("signature = "+signature);
	    Util.logFINE(buf.toString());
	}
	    if (params != null) {
		for (int i = 0; i < params.length; i++) {
		    buf.append("params["+i+"] = "+params[i]+"\n");
		}
	    }
	    buf.append("params = "+params);
	    buf.append("signature = "+signature);
        }
         */

	try {
            Object result =  getMBeanServer().invoke(objectName, operationName, params, signature);
            return result;
        } catch (Exception ex) {
            //The calling method will decide if to catch this exception or dump stack trace. ex issue#2902
            //ex.printStackTrace();
            throw new RuntimeException(ex.getCause());
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
            /* TODO-Logging
	    if (Util.isLoggableFINE()) {
                NonSyncStringBuffer buf = new NonSyncStringBuffer();
                buf.append(ex.getMessage());
                buf.append("Attribute not found on the given Object:\n");
                buf.append("    OBJECT NAME: "+objectName+"\n");
                buf.append("    ATTRIBUTE NAME: "+attributeName+"\n");
                Util.logFINE(buf.toString());
            }
	    throw new FrameworkException(ex);
             */
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
    
    
    public static void stopServerInstance(String serverName){
        //can't use AMX, refer to bug#6459672 and also issue# 2170
        if (serverName.equals("server")){
            JMXUtil.invoke("com.sun.appserv:j2eeType=J2EEServer,name=server,category=runtime",
                    "stop", null, null);
        }else{
            JMXUtil.invoke("com.sun.appserv:type=server,category=config,name="+serverName, 
                    "stop",null,null);
        }
    }
    
    public static void startServerInstance(String serverName){
        JMXUtil.invoke("com.sun.appserv:type=servers,category=config", 
                "startServerInstance",new Object[]{serverName},new String[]{"java.lang.String"});
        int state = AMXUtil.getJ2EEDomain().getJ2EEServerMap().get(serverName).getstate();
		while(state ==  StateManageable.STATE_STARTING) {
			try {
				Thread.sleep(1000);
        		state = AMXUtil.getJ2EEDomain().getJ2EEServerMap().get(serverName).getstate();
			} catch (InterruptedException ex) {
				//just squelch.
				break;
			}
		}
    }
    
     public static void clusterAction(String clusterName, Boolean start){

        /* first param is autohadboverride.
         * autohadboverride signals whether the HADB database  associated  with  the  cluster would
            be started, stopped, or deleted when the cluster was started,stopped,or deleted. 
            If hadboverride is set to true, the HADB database is started when the cluster is started.
            If set to false, the  HADB  database   is  not  started when the cluster is  started.  
            If the hadboverride  option  is  not  set,  the default is to use the cluster's autohadb setting.
            for GUI, we use the cluster's autohadb setting.
         */
        Object params[]= new Object[]{null, clusterName};
        String types[] = new String[]{"java.lang.String", "java.lang.String"};
        
        String action = (start)? "startCluster" :"stopCluster";
            
        JMXUtil.invoke("com.sun.appserv:type=clusters,category=config",action, params, types);
     }
    
    /* 
      * Utility method to return the image and the state string for display using JMX.
      * The AMX API's do not give an accurate state when starting an instance.
      * This object must implmenent StateManageable. 
      * If this is a J2EEServer, then it will also looks at the restart required flag
      * The String returned will be the <img .. > + state 
      */
     public static String getStatusForDisplay(String objName){
                String status = null;
                try {
                    RuntimeStatus sts = (RuntimeStatus)JMXUtil.invoke(objName, "getRuntimeStatus", null, null);
                        boolean restartNeeded = ((RuntimeStatus)sts).isRestartNeeded();
                        Status s = ((RuntimeStatus)sts).getStatus();
                        int statusCode = s.getStatusCode();
                        String statusString = "";
                        String imageString = "";
                        switch (statusCode) {
                            case Status.kInstanceStartingCode: {
                                imageString = AMXUtil.getStatusImage(StateManageable.STATE_STARTING);
                                statusString = GuiUtil.getMessage("common.startingState");
                                break;
                            }
                            case Status.kInstanceRunningCode: {
                                if (restartNeeded) {
                                    imageString = GuiUtil.getMessage("common.restartRequiredImage");
                                    statusString = GuiUtil.getMessage("common.restartRequired");
                                } else {
                                    imageString = AMXUtil.getStatusImage(StateManageable.STATE_RUNNING);
                                    statusString = GuiUtil.getMessage("common.runningState");
                                }
                                break;
                            }
                            case Status.kInstanceStoppingCode: {
                                imageString = AMXUtil.getStatusImage(StateManageable.STATE_STOPPING);
                                statusString = GuiUtil.getMessage("common.stoppingState");
                                break;
                            }
                            case Status.kInstanceNotRunningCode: {
                                imageString = AMXUtil.getStatusImage(StateManageable.STATE_STOPPED);
                                statusString = GuiUtil.getMessage("common.stoppedState");
                                break;
                            }
                        }
                        status = imageString + "&nbsp;" + statusString;
                }catch(Exception ex){ }
             return status;
     }        

    /* 
      * Utility method to return the RuntimStatus for an instance. 
      * The AMX API's do not give an accurate state when starting an instance.
      * This object must implmenent StateManageable. 
      */
     public static RuntimeStatus getRuntimeStatus(String name) {
		String objName = "com.sun.appserv:type=server,name="+name+",category=config";
		RuntimeStatus sts = null;
                try {
                    sts = (RuntimeStatus)JMXUtil.invoke(objName, "getRuntimeStatus", null, null);
                }catch(Exception ex){ 
			//squelching it on purpose
		 }
             return sts;
     }        

     public static int getRuntimeStatusCode(RuntimeStatus rsts) {
     	int statusCode = -1;

	if(rsts != null) {
		Status status = rsts.getStatus();
		if(status != null) {
			statusCode = status.getStatusCode();
		}
	}
	return statusCode;

     }
    
    public final static String DomainDiagnosticsMBeanName 
        = "com.sun.appserv:type=DomainDiagnostics,name=server,category=monitor";
    
    public final static String DomainDiagnosticsGenerateReportMethod
        = "generateReport";
    
}
