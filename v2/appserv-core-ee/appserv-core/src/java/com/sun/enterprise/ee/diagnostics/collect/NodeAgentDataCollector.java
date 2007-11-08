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
package com.sun.enterprise.ee.diagnostics.collect;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.diagnostics.CLIOptions;
import com.sun.enterprise.diagnostics.Data;
import com.sun.enterprise.diagnostics.DiagnosticException;
import com.sun.enterprise.diagnostics.ServiceConfig;
import com.sun.enterprise.diagnostics.collect.Collector;
import com.sun.enterprise.diagnostics.collect.DataType;
import com.sun.enterprise.diagnostics.collect.FileData;
import com.sun.enterprise.ee.diagnostics.EETargetType;
import com.sun.logging.LogDomains;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;



/**
 *
 * @author Manisha Umbarje
 */
public class NodeAgentDataCollector implements Collector {
    
    private List<String> instances;
    private ConfigContext configContext;
    private String nodeAgent;
    private MBeanServerConnection mbsc;
    private Map customerInput;
    private Map configs;
    private String targetType;
    private static final String TARGET = "target";
    private static final String LOCAL = "local";
    private static final String NA_MBEAN_NAME = 
            "com.sun.appserver.nodeagent:type=NodeAgentDiagnostic," ;
    private static final String NA_MBEAN_CLASS =
             "com.sun.enterprise.ee.nodeagent.mbeans.NodeAgentDiagnostic";
    private static final String NA_REGISTRY_CLASS = 
            "com.sun.enterprise.ee.admin.clientreg.NodeAgentRegistry";
    private static final String GET_NA_CONNECTION_METHOD = "getNodeAgentConnection";
    public static final String CONFIGS ="configs";
    private static Logger logger = 
            LogDomains.getLogger(LogDomains.ADMIN_LOGGER);;
    /** Creates a new instance of NodeAgentDataCollector */
    public NodeAgentDataCollector(CLIOptions options , String nodeAgent, 
            List<String> instances, String targetType) {
        this.instances = instances;
        this.nodeAgent = nodeAgent;
        this.targetType = targetType;
        this.customerInput = options.getMap();
    }
    
    /**
     * Capture config information
     * @throw DiagnosticException
     */
    public Data capture() throws DiagnosticException {
        if(nodeAgent != null) {
            initialize();
            try {
                ObjectName objName =
                        new ObjectName(NA_MBEAN_NAME
                        +
                        "name="+ nodeAgent+",category=config");
                

                if(!targetType.equals(EETargetType.INSTANCE.getType()))
                    customerInput.put(TARGET, nodeAgent);
      
                // Add Service Configurations to CustomerInput
                addServiceConfigurations();
                
                Object [] params = new Object [] {customerInput, 
                        instances, targetType};
                String [] signature = new String [] {"java.util.Map", 
                        "java.util.List" , "java.lang.String"};
                
                if(!mbsc.isRegistered(objName)) {
                    try {
                        logger.log(Level.INFO, 
                                "diagnostic-service.create_na_diagnostic_mbean",
                                nodeAgent);
                        mbsc.createMBean(NA_MBEAN_CLASS,objName);
                        

                    } catch (Exception e) {
                        logger.log(Level.WARNING,
                                "diagnostic-service.error_creating_na_diagnostic_mbean",
                                e);
                    }
                }
                String returnValue = (String)mbsc.invoke(
                            objName, "generateReport", params, signature);
                logger.log(Level.INFO, "after collecting data from nodeagent : " + nodeAgent);
                if(returnValue != null) {
                    return new FileData(returnValue,DataType.NODEAGENT_DETAILS);
                } else {
                    logger.log(Level.WARNING, 
                            "Error collecting data from node agent " + nodeAgent);
                }    
            } catch (Exception e) {
                throw new DiagnosticException(e.getMessage());
            }
        }
        return null;
    }
    
    private void initialize() throws DiagnosticException {
        try {
            Class classObj = Class.forName(NA_REGISTRY_CLASS);
            Method method = classObj.getMethod(GET_NA_CONNECTION_METHOD, 
                    java.lang.String.class);
            Object obj = method.invoke(classObj, new Object[] {nodeAgent});
            mbsc = (MBeanServerConnection)obj;
        }catch(Exception e) {
            throw new DiagnosticException(" Error while reaching : " + 
                    nodeAgent + e.getMessage());
        }
    }
    
    private void addServiceConfigurations() {
        if(configs == null) {
            configs = new HashMap();
            customerInput.put(CONFIGS, configs);
        }
                
        for(String instance : instances) {
            try {
                Map instanceMap = new HashMap(8);
                ServiceConfig config = new ServiceConfig(false, null, instance);
                instanceMap.put(ServerTags.CAPTURE_APP_DD, new Boolean(config.isCaptureAppDDEnabled()));
                instanceMap.put(ServerTags.COMPUTE_CHECKSUM, new Boolean(config.isCaptureChecksumEnabled()));
                instanceMap.put(ServerTags.CAPTURE_SYSTEM_INFO, new Boolean(config.isCaptureSystemInfoEnabled()));
                instanceMap.put(ServerTags.CAPTURE_HADB_INFO, new Boolean(config.isCaptureHadbInfoEnabled()));
                instanceMap.put(ServerTags.CAPTURE_INSTALL_LOG, new Boolean(config.isCaptureInstallLogEnabled()));
                instanceMap.put(ServerTags.VERIFY_CONFIG, new Boolean(config.isVerifyDomainEnabled()));
                instanceMap.put(ServerTags.MAX_LOG_ENTRIES, new Integer(config.getMaxNoOfEntries()));
                instanceMap.put(ServerTags.MIN_LOG_LEVEL, new Integer(config.getMinLogLevel()));
                instanceMap.put(ServerTags.LOG_FILE, config.getLogFile());
                configs.put(instance, instanceMap);
            } catch(DiagnosticException de) {
                
            }
        }
    }
    
}
