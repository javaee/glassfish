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


package com.sun.enterprise.cli.commands;
import com.sun.enterprise.cli.framework.*;
import javax.management.MBeanServerConnection;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.J2EEApplicationConfig;
import com.sun.appserv.management.config.EJBModuleConfig;
import com.sun.appserv.management.config.WebModuleConfig;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.WebServiceEndpointConfig;
import com.sun.appserv.management.ext.wsmgmt.WebServiceEndpointInfo;
import java.util.StringTokenizer;
import java.util.Map;


/**
 *Abstract base class for the Transformation Rule commands
 *
 */
abstract public class BaseTransformationRuleCommand extends GenericCommand
{
    protected static final String WEB_SERVICE_OPTION = "webservicename";
    
    /**
     *  parse the operand to get the web service name
     *  return the webservice endpoint config.
     *  @throws CommandException
     */
    protected WebServiceEndpointConfig getWebServiceEndpointConfig(
                            MBeanServerConnection mbsc, String fqWebServiceName,
                            boolean isCreateIfNone) 
                            throws CommandException, CommandValidationException
    {
        // this webserviceName is fully qualified name
        //String fqWebServiceName = getOption(WEB_SERVICE_OPTION);
        // extract the partially qualified web service endpoing config name
        int firstHashIdx = fqWebServiceName.indexOf("#");
        String wsName= null;
        if ( firstHashIdx != -1 ) {
            if ( firstHashIdx+1 == fqWebServiceName.length()) {
                throw new CommandException(getLocalizedString("InvalidFormatForWebservice"));
            }
            wsName =  fqWebServiceName.substring(firstHashIdx +1);
        }
        else
        {
            throw new CommandException(getLocalizedString("InvalidFormatForWebservice"));
        }
        String regName = fqWebServiceName.substring(0,firstHashIdx);
        StringTokenizer sTok = new StringTokenizer(fqWebServiceName,"#");
        int numTokens = sTok.countTokens();
        if ( numTokens == 3 ) {
            // this is an application
            J2EEApplicationConfig appConfig = getApplicationConfigMBean(mbsc, regName);
             if ( appConfig == null){
                 throw new CommandException (
                            getLocalizedString("NoAppFoundForWS", 
                                                new Object[] {regName}));             
             }
            DomainRoot domainRoot = ProxyFactory.getInstance(mbsc).getDomainRoot();
            final WebServiceEndpointInfo info =
                domainRoot.getWebServiceMgr().getWebServiceEndpointInfo(fqWebServiceName);
            if (info == null)
            {
                String moduleName = fqWebServiceName.substring(firstHashIdx + 1, 
                                            fqWebServiceName.lastIndexOf("#"));
                throw new CommandException(getLocalizedString("NoModuleOrEndpointFoundForWS", 
                                                new Object[]{fqWebServiceName}));
            }
             Map epMap = appConfig.getWebServiceEndpointConfigMap();
                if (epMap == null) {
                    if (isCreateIfNone)
                    {
                        return appConfig.createWebServiceEndpointConfig(wsName,null);
                    }
                } else {
                    WebServiceEndpointConfig wsEpConfig = (WebServiceEndpointConfig) epMap.get(wsName);
                    if ((wsEpConfig == null) && (isCreateIfNone)) {
                         return appConfig.createWebServiceEndpointConfig(wsName,null);
                    } else {
                        return wsEpConfig;
                    }
                }
        } else if ( numTokens == 2 ) {
            // this is a stand alone module
            // we need to figure out a type
            String modType = getStandAloneModuleType(mbsc, fqWebServiceName);
            if ( modType.equals(WebServiceEndpointInfo.EJB_IMPL)) {
                EJBModuleConfig ejbModuleConfig = getEJBModuleConfigMBean(mbsc, regName);
                 if ( ejbModuleConfig == null){
                     throw new CommandException (
                                getLocalizedString("NoEJBModuleFoundForWS", 
                                                    new Object[] {regName}));             
                }
                Map epMap = ejbModuleConfig.getWebServiceEndpointConfigMap();
                if ( epMap == null) {
                    if (isCreateIfNone)
                       return ejbModuleConfig.createWebServiceEndpointConfig(wsName,null);
                } else {
                    WebServiceEndpointConfig wsEpConfig = (WebServiceEndpointConfig) epMap.get(wsName);
                    if ((wsEpConfig == null) && (isCreateIfNone)) {
                         return ejbModuleConfig.createWebServiceEndpointConfig(wsName,null);
                    } else {
                        return wsEpConfig;
                    }
                }
            } else if (modType.equals(WebServiceEndpointInfo.SERVLET_IMPL) ) {
                WebModuleConfig webModuleConfig = getWebModuleConfigMBean(mbsc, regName);
                if ( webModuleConfig == null){
                     throw new CommandException (
                                getLocalizedString("NoWebModuleFoundForWS",
                                                     new Object[] {regName}));
                }
                Map epMap = webModuleConfig.getWebServiceEndpointConfigMap();
                if ( epMap == null) {
                    if (isCreateIfNone)
                       return webModuleConfig.createWebServiceEndpointConfig(wsName, null);
                } else {
                    WebServiceEndpointConfig wsEpConfig = (WebServiceEndpointConfig) epMap.get(wsName);
                     if ((wsEpConfig == null) && (isCreateIfNone)){
                         return webModuleConfig.createWebServiceEndpointConfig(wsName,null);
                    } else {
                        return wsEpConfig;
                    }
                }
                
            } else {
                // user entered non servlet/ejb module
                 throw new CommandException (
                            getLocalizedString("InvalidModuleTypeForWS"));
            }
        } else {
            // format of FQN is wrong
            throw new CommandValidationException(getLocalizedString("InvalidFormatForWebservice"));
        }
        // this wsc does not exist
        
        return null;
    }


    private J2EEApplicationConfig getApplicationConfigMBean (MBeanServerConnection mbsc, String appName) {
        DomainRoot domainRoot = ProxyFactory.getInstance(mbsc).getDomainRoot();
        DomainConfig domainConfig = domainRoot.getDomainConfig();
        Map appCfgMap = domainConfig.getJ2EEApplicationConfigMap();
        if ( appCfgMap != null){
            return (J2EEApplicationConfig) appCfgMap.get(appName);
        } else {
            return null;
        }
    }
    
    private EJBModuleConfig getEJBModuleConfigMBean(MBeanServerConnection mbsc, String moduleName) {
        DomainRoot domainRoot = ProxyFactory.getInstance(mbsc).getDomainRoot();
        DomainConfig domainConfig = domainRoot.getDomainConfig();
        Map appCfgMap = domainConfig.getEJBModuleConfigMap();
        if ( appCfgMap != null){
            return (EJBModuleConfig) appCfgMap.get(moduleName);
        } else {
            return null;
        }
    }
    
    private WebModuleConfig getWebModuleConfigMBean(MBeanServerConnection mbsc, String moduleName) {
        DomainRoot domainRoot = ProxyFactory.getInstance(mbsc).getDomainRoot();
        DomainConfig domainConfig = domainRoot.getDomainConfig();
        Map appCfgMap = domainConfig.getWebModuleConfigMap();
        if ( appCfgMap != null){
            return (WebModuleConfig) appCfgMap.get(moduleName);
        } else {
            return null;
        }
    }
    
    private String getStandAloneModuleType(MBeanServerConnection mbsc, String fqName)
                    throws CommandException
    {
         DomainRoot domainRoot = ProxyFactory.getInstance(mbsc).getDomainRoot();
         final WebServiceEndpointInfo info =
            domainRoot.getWebServiceMgr().getWebServiceEndpointInfo(
                    fqName);
         if (info == null){
             throw new CommandException (
                        getLocalizedString("NoStandaloneModuleFoundForWS", new Object[]{fqName}));
         }
         return info.getServiceImplType();
    }


    /**
     *  validate the webservice name format.
     *  @throws CommandException
     */
    protected void validateWebServiceName(String fqWebServiceName, boolean validateIfNull) 
                    throws CommandValidationException
    {
         // this webserviceName is fully qualified name
        //String fqWebServiceName = getOption(WEB_SERVICE_OPTION); 
        if ((fqWebServiceName == null) && (!validateIfNull))
        {
            return;
        }
        else
        {
            //This will not happen as the webservicename is required option
            // for create & delete commands
        }
        int hashIdx = fqWebServiceName.lastIndexOf("#");
        if ( hashIdx != -1 ) {
            if ( hashIdx+1 == fqWebServiceName.length()) {
                throw new CommandValidationException(getLocalizedString("InvalidFormatForWebservice"));
            }
        }
        else
        {
            throw new CommandValidationException(getLocalizedString("InvalidFormatForWebservice"));
        }
    }
}

