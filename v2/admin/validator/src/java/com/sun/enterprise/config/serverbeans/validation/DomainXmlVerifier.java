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

package com.sun.enterprise.config.serverbeans.validation;

// config imports
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContextEvent;


import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.IiopService;
import com.sun.enterprise.config.serverbeans.JmsService;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.SecurityService;
//import com.sun.enterprise.config.serverbeans.validation.tests.StaticTest;

import java.net.URL;

// Logging
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
 
//import com.sun.enterprise.config.serverbeans.VirtualServerClass;
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;

import java.util.Vector;
import java.io.*;

/**
    Class which invokes the config validator from Command Line
 
    @author Srinivas Krishnan
    @version 2.0
*/

public class DomainXmlVerifier {
    
    public ConfigContext configContext;
    public DomainMgr domainMgr;
    public String domainFile;
    public boolean error;
    public boolean debug = false;
    public static int count = 0;
    PrintStream   _out;

    static Logger _logger = LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);
    static {
        StringManagerHelper.setLocalStringsManager(DomainXmlVerifier.class);
    }
    static LocalStringManagerImpl smh = StringManagerHelper.getLocalStringsManager();
    public DomainXmlVerifier(String file) throws Exception {
        this(file, System.out);
    }
    public DomainXmlVerifier(String file, PrintStream out) throws Exception {
        _out = out;
        domainFile = file;
        configContext = ConfigFactory.createConfigContext(file);
        configContext.refresh();
        domainMgr = new DomainMgr(configContext, true);
        error = false;
    }
   
    public DomainMgr getDomainMgr() {
        return domainMgr;
    }
    
    public ConfigContext getConfigContext() {
        return configContext;
    }
    
    public void setDebug(boolean flag) {
        debug = flag;
    }
    
    public void output(Result result) {
        String element = result.getAssertion();
        String key = result.getTestName();
        if(!result.getErrorDetails().toString().equals("[]")) {
            error = true;
            out("Element  : " + element);
            if(key != null) 
                out("Key      : " + key);
            Vector error = result.getErrorDetails();
            for(int i=0;i<error.size();i++) {
                if(i==0) 
                    out("Error    : " + error.get(i));
                else if(i>0)
                    out("           " + error.get(i));
            }  
            out("");
            count++; // Number of test to be printed
        }
    }
    
    public void check(ConfigBean configBean) {
        ConfigContextEvent cce = new ConfigContextEvent(getConfigContext(), ConfigContextEvent.PRE_ADD_CHANGE,configBean.toString(),configBean,"VALIDATE");
        cce.setClassObject(configBean.parent());
        Result result = domainMgr.check(cce);
        if(result != null) 
            output(result);
    }
    
    public void checkUnique(ConfigBean configBean) {

        try {
            
            Domain domain = (Domain)configContext.getRootConfigBean();
            
            // Resources 
            Resources resource = domain.getResources();
//            checkDuplicate("admin-object-resource", resource.getAdminObjectResource(), "jndi-name");
//            checkDuplicate("resource-adapter-config", resource.getResourceAdapterConfig(), "resource-adapter-name");
            checkDuplicate("connector-connection-pool", resource.getConnectorConnectionPool(), "name");
            ConnectorConnectionPool[] connpool = resource.getConnectorConnectionPool();
            for(int i=0;i<connpool.length;i++)
                checkDuplicate("security-map", connpool[i].getSecurityMap(), "name");

//            checkDuplicate("connector-resource", resource.getConnectorResource(), "jndi-name");
//            checkDuplicate("custom-resource", resource.getCustomResource(), "jndi-name");
//            checkDuplicate("external-jndi-resource", resource.getExternalJndiResource(), "jndi-name");
//            checkDuplicate("jdbc-connection-pool", resource.getJdbcConnectionPool(), "name");
//            checkDuplicate("jdbc-resource", resource.getJdbcResource(), "jndi-name");
//            checkDuplicate("mail-resource", resource.getMailResource(), "jndi-name");
//            checkDuplicate("persistence-manger-factory-resource", resource.getPersistenceManagerFactoryResource(), "jndi-name");
            
            //Applications
            Applications applications = domain.getApplications();
//            checkDuplicate("appclient-module",applications.getAppclientModule(),"name");
//            checkDuplicate("connector-module",applications.getConnectorModule(),"name");
//            checkDuplicate("ejb-module", applications.getEjbModule(),"name");
//            checkDuplicate("j2ee-application", applications.getJ2eeApplication(),"name");
//            checkDuplicate("lifecycle-module", applications.getLifecycleModule(),"name");
//            checkDuplicate("web-module", applications.getWebModule(),"name");
            
            // Configs
            Configs configs = domain.getConfigs();
            checkDuplicate("config", configs.getConfig(), "name");
            Config[] config = configs.getConfig();
            for(int i=0;i<config.length;i++) {
                
                HttpService httpservice = config[i].getHttpService();
                //checkDuplicate("acl",httpservice.getAcl(), "name");
                checkDuplicate("http-listener",httpservice.getHttpListener(), "id");
                //checkDuplicate("mime",httpservice.getMime(), "name");
                checkDuplicate("virtual-server", httpservice.getVirtualServer(), "id");
                
                IiopService iiopservice = config[i].getIiopService();
                checkDuplicate("iiop-listener",iiopservice.getIiopListener(), "id");
                AdminService adminservice = config[i].getAdminService();
                checkDuplicate("jmx-connector",adminservice.getJmxConnector(), "name");
                
                JmsService jmsservice = config[i].getJmsService();
                checkDuplicate("jms-host",jmsservice.getJmsHost(), "name");
                
                SecurityService securityservice = config[i].getSecurityService();
                checkDuplicate("audit-module", securityservice.getAuditModule(), "name");
                checkDuplicate("auth-realm", securityservice.getAuthRealm(), "name");
                checkDuplicate("jacc-provider", securityservice.getJaccProvider(), "name");
            }

            Servers servers = domain.getServers();
            checkDuplicate("server", servers.getServer(), "name");
            
        } catch(Exception e) {
            e.printStackTrace();
        }

    }
    
    public void checkDuplicate(String elementName, ConfigBean[] cb, String attr) {
            Result result = new Result();
            result.setAssertion(elementName);
            for(int i=0;i<cb.length-1;i++) {
                        String name=cb[i].getAttributeValue(attr);
                        for(int j=i+1;j<cb.length;j++) {
                            if(name.equals(cb[j].getAttributeValue(attr))) {
                                result.failed("Duplicate Element : " + cb[j] + "(" + attr + "=" +  name + ")");
                                output(result); 
                            }
                        }
            }
    }
    
    
    public void preOrder(ConfigBean configBean) {
            if(configBean != null) {
                check(configBean);
                ConfigBean configBean1[] = configBean.getAllChildBeans();
                if(configBean1 != null) {
                    for(int j=0;j<configBean1.length;j++)  {
                           if(configBean1[j] != null)
                                preOrder(configBean1[j]);
                    }
                }
            }
    }
    
    public void out(String out) {
        if(count < 50)
            _out.println(out);
        else if(count >= 50 && debug) 
            _out.println(out);
    }
    
    public boolean validate() {
        try {
            checkUnique(configContext.getRootConfigBean());
            preOrder(configContext.getRootConfigBean());
            if(!error)
                _out.println("All Tests Passed, domain.xml is valid");
        } catch(Exception e) {
            e.printStackTrace();
        }
        return error;
    }
    
    public boolean invokeConfigValidator() {
        boolean failed = false;
        try {
            failed =  validate();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return failed;
    }
    
    public static void main(String args[]) throws ConfigException {
        
        // if no args supplied print the usage
        int i=0;
        if(args.length < 1) {
            System.out.println("Usage : verify-domain-xml [--verbose] domain_xml_file");
            return;
        }
        
        boolean isDebug = false;
        // if verbose set, set debug to true
        if(args[i].startsWith("--verbose")) {
            if(args.length == 1) {
                System.out.println("Usage : verify-domain-xml [--verbose]  domain_xml_file");
                return;
            }
            isDebug = true;
            i++;
        }
        
        // check if domain.xml path supplied exists
        File f = new File(args[i]); 
        if(!f.exists()) {   
            //TODO i18n
            System.out.println("Error Config file: " + args[i] + " is not found");
            return;
        }
        String file = args[i];
        boolean fileCheck = false;
        boolean classCheck = false;
        
        try {
            DomainXmlVerifier validator = new DomainXmlVerifier(file);
            if(isDebug)
                validator.debug = true;
            // do not check for file and class path
//            for(int j=0;j<args.length;j++) {
//                if(args[j].startsWith("--check-classpath"))
//                    classCheck = true;
//                if(args[j].startsWith("--check-filepath"))
//                    fileCheck = true;
//            }
//            StaticTest.fileCheck = fileCheck;
//            StaticTest.classPathCheck = classCheck;
            validator.invokeConfigValidator();
        } catch (ConfigException ce) {
            Throwable cause = ce.getCause();
            while(cause!=null && !(cause instanceof org.xml.sax.SAXParseException))
                cause = cause.getCause();
            if(cause!=null)
                System.out.println("XML: "+cause.getMessage());
            else
                ce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
