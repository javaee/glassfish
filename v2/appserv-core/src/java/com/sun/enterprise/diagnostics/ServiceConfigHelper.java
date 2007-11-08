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
package com.sun.enterprise.diagnostics;

import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.serverbeans.DiagnosticService;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.LogService;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.logging.LogDomains;
import com.sun.enterprise.diagnostics.collect.DomainXMLHelper;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.*;



/**
 * Helper which helps reading diagnostic service config elements from
 * domain.xml either from runtime MBeans or by loading the document in local
 * mode.
 * @author Manisha Umbarje
 */
public class ServiceConfigHelper {
    private Element configElement;
    private Element diagnosticElement;
    private DomainXMLHelper xmlHelper;
    private boolean local;
    private String configName;
    private String repositoryDir;
    private String instanceName;
    private static String envInstanceRootVar ="${com.sun.aas.instanceRoot}";
   
    
    private static Logger logger = 
    LogDomains.getLogger(LogDomains.ADMIN_LOGGER);

    public ServiceConfigHelper(String instanceName) {
        this.instanceName = instanceName;
    }
    
    public ServiceConfigHelper(String repositoryDir, String instanceName, 
            boolean local) {
	this.repositoryDir = repositoryDir;
        this.instanceName = instanceName;
	this.local = local;
        
    }

    /**
     * Returns value of specified attribute from domain.xml
     */
    public String getAttribute(String attribute)
	    throws DiagnosticException {
	if (local){
            if (diagnosticElement == null)
                initializeXMLElements();
	    return getDOMAttribute(attribute);
	}
	return getRuntimeAttribute(attribute);
    }
    
    /**
     * Gets config name
     * @return config name
     */
    public String getConfigName() {
        return configName;
    }
    
    /**
     * Retrieves instance name
     * @return instance name
     */
    public String instanceName() {
        return instanceName;
    }
 
    /**
     * Initialize XML elements
     */
    private void initializeXMLElements() throws DiagnosticException {
        try {
            xmlHelper = new DomainXMLHelper(repositoryDir);
            Element element =  xmlHelper.getElement("server", instanceName);
            configName = xmlHelper.getAttribute(element, "config-ref");
            configElement = xmlHelper.getElement("config", configName);
            diagnosticElement = xmlHelper.getElement(configElement,
                                                    "diagnostic-service");
        }catch(Exception e) {
            e.printStackTrace();
            throw new DiagnosticException(e.getMessage());
        }
    }
    /**
     * Retrieves value from runtime attributes
     * @reutrn runtime value of a attribute
     */
    private String getRuntimeAttribute(String attribute)
	    throws DiagnosticException {
	try {

            logger.log(Level.FINE, "Instance Name :" + instanceName);

            ConfigContext configContext = 
                    AdminService.getAdminService().getAdminContext().getAdminConfigContext();
            Server server = ServerHelper.getServerByName(configContext, instanceName);
            configName = server.getConfigRef();
            Config config = ConfigAPIHelper.getConfigByName(configContext, 
                    configName);
	    if(attribute.equals(ServerTags.FILE)) {
		LogService logService = config.getLogService();
                return determineLogFile(logService.getFile());
	    }
	    else {
		DiagnosticService diagService = config.getDiagnosticService();
		return diagService.getAttributeValue(attribute);
	    }
	} catch(ConfigException ce) {
	    logger.log(Level.SEVERE,
		    "diagnostic-service.error_retrieving_logFileName",
		    ce.getMessage());
	    throw new DiagnosticException(ce.getMessage());
	}

    }//getRuntimeAttribute

    /**
     * Retrieves attribute from loaded DOM - domain.xml
     * @return returns value of a attribute in a local mode
     */
    private String getDOMAttribute(String attribute)
	    throws DiagnosticException {

	if(attribute.equals(ServerTags.FILE)) {
	    Element logElement = xmlHelper.getElement(configElement,"log-service");
	    return determineLogFile(logElement.getAttribute(ServerTags.FILE));
	}

	logger.log(Level.FINEST, "diagnostic-service.attribute_name",
                new Object[] {attribute});
	logger.log(Level.FINEST, "diagnostic-service.attribute_value",
                new Object[] {diagnosticElement.getAttribute(attribute)});

        return diagnosticElement.getAttribute(attribute);
    }//getDOMAttribute

    
    /**
     * Computes absolute path to log file from log-service/file
     * attribute. 
     * @param logFileName log-service/file value 
     * @return absolute path of the log file.
     */
    private String determineLogFile(String logFileName) {
        if(logFileName.startsWith(envInstanceRootVar)) {
            int length = envInstanceRootVar.length() ;
            if(length > 0) {
                String logFileSuffix = logFileName.substring(length);
                if(repositoryDir != null)
                    logFileName = repositoryDir + logFileSuffix;
                else
                    logFileName = logFileSuffix;
            }
         } 
         return logFileName;
    }
}
