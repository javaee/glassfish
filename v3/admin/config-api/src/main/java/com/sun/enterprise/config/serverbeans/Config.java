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
package com.sun.enterprise.config.serverbeans;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.DuckTyped;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.component.Injectable;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigView;
import org.jvnet.hk2.component.Habitat;

import java.beans.PropertyVetoException;
import java.util.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.Proxy;

import  com.sun.common.util.logging.LoggingConfigImpl;

import org.glassfish.config.support.datatypes.Port;

import org.glassfish.api.amx.AMXConfigInfo;
import org.glassfish.api.admin.config.Named;
import org.glassfish.api.admin.config.Container;

import org.glassfish.api.admin.config.PropertyDesc;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.admin.config.Property;
import org.glassfish.api.admin.config.PropertyBag;
import org.glassfish.api.admin.ServerEnvironment;

import org.glassfish.quality.ToDo;
import org.glassfish.server.ServerEnvironmentImpl;

/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "httpService",
    "iiopService",
    "adminService",
    "connectorService",
    "webContainer",
    "ejbContainer",
    "mdbContainer",
    "jmsService",
    "logService",
    "securityService",
    "transactionService",
    "monitoringService",
    "diagnosticService",
    "javaConfig",
    "availabilityService",
    "threadPools",
    "alertService",
    "groupManagementService",
    "managementRules",
    "systemProperty",
    "property"
}) */
@AMXConfigInfo( amxInterfaceName="com.sun.appserv.management.config.ConfigConfig" )
@Configured
public interface Config extends ConfigBeanProxy, Injectable, Named, PropertyBag, SystemPropertyBag {

    /**
     *  Name of the configured object
     *
     * @return name of the configured object
     FIXME: should set 'key=true'.  See bugs 6039, 6040
     */                                                                                                          
    @Attribute(required=true)
    String getName();

    public void setName(String value) throws PropertyVetoException;
    
    /**
     * Gets the value of the dynamicReconfigurationEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true")
    public String getDynamicReconfigurationEnabled();

    /**
     * Sets the value of the dynamicReconfigurationEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDynamicReconfigurationEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the httpService property.
     *
     * @return possible object is
     *         {@link HttpService }
     */
    @Element(required=true)
    public HttpService getHttpService();

    /**
     * Sets the value of the httpService property.
     *
     * @param value allowed object is
     *              {@link HttpService }
     */
    public void setHttpService(HttpService value) throws PropertyVetoException;

    /**
     * Gets the value of the iiopService property.
     *
     * @return possible object is
     *         {@link IiopService }
     */
    @Element(required=true)
    public IiopService getIiopService();

    /**
     * Sets the value of the iiopService property.
     *
     * @param value allowed object is
     *              {@link IiopService }
     */
    public void setIiopService(IiopService value) throws PropertyVetoException;

    /**
     * Gets the value of the adminService property.
     *
     * @return possible object is
     *         {@link AdminService }
     */
    @Element(required=true)
    public AdminService getAdminService();

    /**
     * Sets the value of the adminService property.
     *
     * @param value allowed object is
     *              {@link AdminService }
     */
    public void setAdminService(AdminService value) throws PropertyVetoException;

    /**
     * Gets the value of the connectorService property.
     *
     * @return possible object is
     *         {@link ConnectorService }
     */
    @Element    
    public ConnectorService getConnectorService();

    /**
     * Sets the value of the connectorService property.
     *
     * @param value allowed object is
     *              {@link ConnectorService }
     */
    public void setConnectorService(ConnectorService value) throws PropertyVetoException;

    /**
     * Gets the value of the webContainer property.
     *
     * @return possible object is
     *         {@link WebContainer }
     */
    @Element(required=true)
    public WebContainer getWebContainer();

    /**
     * Sets the value of the webContainer property.
     *
     * @param value allowed object is
     *              {@link WebContainer }
     */
    public void setWebContainer(WebContainer value) throws PropertyVetoException;

    /**
     * Gets the value of the ejbContainer property.
     *
     * @return possible object is
     *         {@link EjbContainer }
     */
    @Element(required=true)
    public EjbContainer getEjbContainer();

    /**
     * Sets the value of the ejbContainer property.
     *
     * @param value allowed object is
     *              {@link EjbContainer }
     */
    public void setEjbContainer(EjbContainer value) throws PropertyVetoException;

    /**
     * Gets the value of the mdbContainer property.
     *
     * @return possible object is
     *         {@link MdbContainer }
     */
    @Element(required=true)
    public MdbContainer getMdbContainer();

    /**
     * Sets the value of the mdbContainer property.
     *
     * @param value allowed object is
     *              {@link MdbContainer }
     */
    public void setMdbContainer(MdbContainer value) throws PropertyVetoException;

    /**
     * Gets the value of the jmsService property.
     *
     * @return possible object is
     *         {@link JmsService }
     */
    @Element
    public JmsService getJmsService();

    /**
     * Sets the value of the jmsService property.
     *
     * @param value allowed object is
     *              {@link JmsService }
     */
    public void setJmsService(JmsService value) throws PropertyVetoException;

    /**
     * Gets the value of the logService property.
     *
     * @return possible object is
     *         {@link LogService }
     */
    @Element(required=true)
    public LogService getLogService();

    /**
     * Sets the value of the logService property.
     *
     * @param value allowed object is
     *              {@link LogService }
     */
    public void setLogService(LogService value) throws PropertyVetoException;

    /**
     * Gets the value of the securityService property.
     *
     * @return possible object is
     *         {@link SecurityService }
     */
    @Element(required=true)
    public SecurityService getSecurityService();

    /**
     * Sets the value of the securityService property.
     *
     * @param value allowed object is
     *              {@link SecurityService }
     */
    public void setSecurityService(SecurityService value) throws PropertyVetoException;

    /**
     * Gets the value of the transactionService property.
     *
     * @return possible object is
     *         {@link TransactionService }
     */
    @Element(required=true)
    public TransactionService getTransactionService();

    /**
     * Sets the value of the transactionService property.
     *
     * @param value allowed object is
     *              {@link TransactionService }
     */
    public void setTransactionService(TransactionService value) throws PropertyVetoException;

    /**
     * Gets the value of the monitoringService property.
     *
     * @return possible object is
     *         {@link MonitoringService }
     */
    @Element(required=true)
    public MonitoringService getMonitoringService();

    /**
     * Sets the value of the monitoringService property.
     *
     * @param value allowed object is
     *              {@link MonitoringService }
     */
    public void setMonitoringService(MonitoringService value) throws PropertyVetoException;

    /**
     * Gets the value of the diagnosticService property.
     *
     * @return possible object is
     *         {@link DiagnosticService }
     */
    @Element
    public DiagnosticService getDiagnosticService();

    /**
     * Sets the value of the diagnosticService property.
     *
     * @param value allowed object is
     *              {@link DiagnosticService }
     */
    public void setDiagnosticService(DiagnosticService value) throws PropertyVetoException;

    /**
     * Gets the value of the javaConfig property.
     *
     * @return possible object is
     *         {@link JavaConfig }
     */
    @Element(required=true)
    public JavaConfig getJavaConfig();

    /**
     * Sets the value of the javaConfig property.
     *
     * @param value allowed object is
     *              {@link JavaConfig }
     */
    public void setJavaConfig(JavaConfig value) throws PropertyVetoException;

    /**
     * Gets the value of the availabilityService property.
     *
     * @return possible object is
     *         {@link AvailabilityService }
     */
    @Element
    public AvailabilityService getAvailabilityService();

    /**
     * Sets the value of the availabilityService property.
     *
     * @param value allowed object is
     *              {@link AvailabilityService }
     */
    public void setAvailabilityService(AvailabilityService value) throws PropertyVetoException;

    /**
     * Gets the value of the threadPools property.
     *
     * @return possible object is
     *         {@link ThreadPools }
     */
    @Element(required=true)
    public ThreadPools getThreadPools();

    /**
     * Sets the value of the threadPools property.
     *
     * @param value allowed object is
     *              {@link ThreadPools }
     */
    public void setThreadPools(ThreadPools value) throws PropertyVetoException;

    /**
     * Gets the value of the alertService property.
     *
     * @return possible object is
     *         {@link AlertService }
     */
    @Element
    public AlertService getAlertService();

    /**
     * Sets the value of the alertService property.
     *
     * @param value allowed object is
     *              {@link AlertService }
     */
    public void setAlertService(AlertService value) throws PropertyVetoException;

    /**
     * Gets the value of the groupManagementService property.
     *
     * @return possible object is
     *         {@link GroupManagementService }
     */
    @Element
    public GroupManagementService getGroupManagementService();

    /**
     * Sets the value of the groupManagementService property.
     *
     * @param value allowed object is
     *              {@link GroupManagementService }
     */
    public void setGroupManagementService(GroupManagementService value) throws PropertyVetoException;

    /**
     * Gets the value of the managementRules property.
     *
     * @return possible object is
     *         {@link ManagementRules }
     */
    @Element
    public ManagementRules getManagementRules();

    /**
     * Sets the value of the managementRules property.
     *
     * @param value allowed object is
     *              {@link ManagementRules }
     */
    public void setManagementRules(ManagementRules value) throws PropertyVetoException;

    /**
     * Gets the value of the systemProperty property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the systemProperty property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSystemProperty().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link SystemProperty }
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Any more legal system properties?" )
@PropertiesDesc(
    systemProperties=true,
    props={
        @PropertyDesc(name="HTTP_LISTENER_PORT", defaultValue="8080", dataType=Port.class),
        @PropertyDesc(name="HTTP_SSL_LISTENER_PORT", defaultValue="1043", dataType=Port.class),
        @PropertyDesc(name="HTTP_ADMIN_LISTENER_PORT", defaultValue="4848", dataType=Port.class),
        @PropertyDesc(name="IIOP_LISTENER_PORT", defaultValue="3700", dataType=Port.class),
        @PropertyDesc(name="IIOP_SSL_LISTENER_PORT", defaultValue="1060", dataType=Port.class),
        @PropertyDesc(name="IIOP_SSL_MUTUALAUTH_PORT", defaultValue="1061", dataType=Port.class),
        @PropertyDesc(name="JMX_SYSTEM_CONNECTOR_PORT", defaultValue="8686", dataType=Port.class)
    }
    )
    @Element
    public List<SystemProperty> getSystemProperty();


    //DuckTyped for accessing the logging.properties file

    @DuckTyped
    public Map<String, String> getLoggingProperties();

    @DuckTyped
    public String setLoggingProperty(String property, String value);
    
    @DuckTyped
    public Map<String, String> updateLoggingProperties( Map<String, String> properties);

    public class Duck {

        public static String setLoggingProperty(Config c, String property, String value){
            ConfigBean cb = (ConfigBean) ((ConfigView)Proxy.getInvocationHandler(c)).getMasterView();
            ServerEnvironmentImpl env = cb.getHabitat().getComponent(ServerEnvironmentImpl.class);
            LoggingConfigImpl loggingConfig = new LoggingConfigImpl();
            loggingConfig.setupConfigDir(env.getConfigDirPath());
            
            String prop = null;
            try{
                   prop= loggingConfig.setLoggingProperty(property, value);
            } catch (IOException ex){
            }
            return prop;
        }

        public static Map<String, String>getLoggingProperties(Config c) {
            ConfigBean cb = (ConfigBean) ((ConfigView)Proxy.getInvocationHandler(c)).getMasterView();
            ServerEnvironmentImpl env = cb.getHabitat().getComponent(ServerEnvironmentImpl.class);
            LoggingConfigImpl loggingConfig = new LoggingConfigImpl();
            loggingConfig.setupConfigDir(env.getConfigDirPath());

            Map <String, String> map = new HashMap<String, String>() ;
            try {
                map = loggingConfig.getLoggingProperties();
            } catch (IOException ex){
            }
            return map;
        }

        public static Map<String, String>updateLoggingProperties(Config c, Map<String, String>properties){
            ConfigBean cb = (ConfigBean) ((ConfigView)Proxy.getInvocationHandler(c)).getMasterView();
            ServerEnvironmentImpl env = cb.getHabitat().getComponent(ServerEnvironmentImpl.class);
            LoggingConfigImpl loggingConfig = new LoggingConfigImpl();
            loggingConfig.setupConfigDir(env.getConfigDirPath());
            
            Map <String, String> map = new HashMap<String, String>() ;
            try {
                map = loggingConfig.updateLoggingProperties(properties);
            } catch (IOException ex){
            }
            return map;
        }

    }
    /**
    	Properties as per {@link PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();

    /**
     * Get the configuration for other types of containers.
     * 
     * @return  list of containers configuration
     */
    @Element("*")
    List<Container> getContainers();
}



