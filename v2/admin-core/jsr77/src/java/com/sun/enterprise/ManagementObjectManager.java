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
package com.sun.enterprise;

import com.sun.enterprise.deployment.*;
import javax.management.*;
import javax.management.ObjectName;
import com.sun.enterprise.management.agent.MEJBUtility;

/**
 * The ManagementObjectManager provides registration and unregistration
 * functionality for managed objects defined by JSR77. 
 *
 * @author Sanjeev Krishnan
 */
public interface ManagementObjectManager {

    public static final String RUNNING_STATE="Running";
    public static final String STOPPED_STATE="Stopped";
    public static final String FAILED_STATE="Failed";
    
    /* Constants for jsr77 modules */
    public static String J2EE_TYPE_EJB_MODULE = "EJBModule";
    public static String J2EE_TYPE_WEB_MODULE = "WebModule";
    public static String J2EE_TYPE_RAR_MODULE = "ResourceAdapterModule";
    public static String J2EE_TYPE_ACC_MODULE = "AppClientModule";
    public static String J2EE_TYPE_APPLICATION = "J2EEApplication";

    void registerJ2EEDomain();

    void registerJ2EEServer();   
    void registerDasJ2EEServers();   
    void registerDasJ2EEServer(String serverName);   
    void unregisterDasJ2EEServer(String serverName);   
    public String getServerBaseON(boolean amx, String serverName);

    void registerJCAConnectionFactory(String jcaResName, String jcaConnFactName,
				      String managedConFact); 

    void registerJCAManagedConnectionFactory(String connFactName); 

    void registerJCAResource(String name, String raName, 
                            String username, String password,
                            String[] propNames, String[] propValues);

    void registerJDBCDataSource(String dsJndiName, 
                            String dsName, 
                            String url,
                            String username, 
                            String password,
                            String[] propNames, 
                            String[] propValues); 

    void registerJDBCDriver(String name); 

    void registerJDBCResource(String name); 

    void registerJMSResource(String name, String resType,
                                    String username, String password,
                                    String[] propNames, String[] propValues); 
    
    void registerAdminObjectResource(String name, String raName, String resType,
                            String[] propNames, String[] propValues);

    void registerJNDIResource(String name); 

    void registerJTAResource(String name);

    void registerJVM();

    void registerJavaMailResource(String name);
    
    void registerRMI_IIOPResource(String name);

    void unregisterStandAloneModule(String moduleType, String name);

    void unregisterJCAResource(String name) throws Exception;

    void unregisterJavaMailResource(String name) throws Exception;
    
    void unregisterJMSResource(String name)throws Exception;

    void unregisterJDBCResource(String name) throws Exception; 
      
    MEJBUtility getMEJBUtility();
    
    void unregisterAdminObjectResource(String name, String resourceType ) 
        throws Exception;

    void unregister(ObjectName objectName) throws Exception;
    
    Object getModuleUserData(String moduleID, int moduleType) throws Exception;
    
    void setModuleUserData(String moduleID, Object obj, int moduleType)throws Exception;

    // api added for deploy objects

    // Application

    void createAppMBean(Application application, String serverName, String appLocation) 
			throws MBeanException;

    void deleteAppMBean(Application application, String serverName) 
			throws MBeanException;

    void createAppMBeanModules(Application application, String serverName, String appLocation) 
			throws MBeanException;

    void createAppMBeans(Application application, String serverName, String appLocation) 
			throws MBeanException;

    void deleteAppMBeans(Application application, String serverName) 
			throws MBeanException;


    // Ejb Module

    void createEJBModuleMBean(EjbBundleDescriptor ejbBundleDescriptor, 
	String serverName, String appLocation) throws MBeanException;

    void deleteEJBModuleMBean(EjbBundleDescriptor ejbBundleDescriptor, 
	String serverName) throws MBeanException;

    void createEJBMBeans(EjbBundleDescriptor ejbBundleDescriptor, 
	String serverName) throws MBeanException;

    void deleteEJBMBeans(EjbBundleDescriptor ejbBundleDescriptor, 
	String serverName) throws MBeanException;

    void createEJBMBean(EjbDescriptor ejbDescriptor, 
	String serverName) throws MBeanException;

    void deleteEJBMBean(EjbDescriptor ejbDescriptor, 
	String serverName) throws MBeanException;



    // Web Module
    void createWebModuleMBean(String appName, String parentName, 
	String serverName, String deploymentDescriptor) 
	throws MBeanException;

    void deleteWebModuleMBean(String appName, String parentName, 
	String serverName) throws MBeanException;

    void createWebMBeans(WebBundleDescriptor webBundleDescriptor, 
	String serverName) throws MBeanException; 

    void deleteWebMBeans(WebBundleDescriptor webBundleDescriptor, 
	String serverName) throws MBeanException;

    void createWebMBean(String servletName, String moduleName, String appName, 
	String serverName) throws MBeanException;

    void deleteWebMBean(String servletName, String moduleName, String appName, 
	String serverName) throws MBeanException;

    void registerWebModuleAndItsComponents(WebBundleDescriptor webBundleDescriptor, 
	String serverName, boolean registerComponents, String appLocation) 
	throws MBeanException;

    // Web Service (monitoring) endpoints

    /**
     * Creates all the web service endpoint runtime mbeans for the web or
     * ejb bundle.
     *
     * @param bundleDescriptor descriptor of the web/ejb bundle
     * @param serverName       instance on which these mbeans are created
     *
     * @throws MBeanException  incase of error
     */
    void createWSEndpointMBeans(BundleDescriptor bundleDescriptor, 
	String serverName) throws MBeanException;

    /**
     * Deletes all the web service endpoint runtime mbeans for the web or
     * ejb bundle.
     *
     * @param bundleDescriptor descriptor of the web/ejb bundle
     * @param serverName       instance on which these mbeans are deleted
     *
     * @throws MBeanException  incase of error
     */
    void deleteWSEndpointMBeans(BundleDescriptor bundleDescriptor, 
	String serverName) throws MBeanException;

    /**
     * Creates the specified web service endpoint runtime mbean
     *
     * @param ctxRoot          context root for this web service endpoint
     * @param descriptor       descriptor of the web service endpoint
     * @param serverName       instance on which these mbean is created
     *
     * @throws MBeanException  incase of error
     */
    void createWSEndpointMBean(String ctxRoot, WebServiceEndpoint descriptor, 
	String serverName) throws MBeanException;

    /**
     * Deletes the specified web service endpoint runtime mbean
     *
     * @param ctxRoot          context root for this web service endpoint
     * @param descriptor       descriptor of the web service endpoint
     * @param serverName       instance on which these mbean is deleted
     *
     * @throws MBeanException  incase of error
     */
    void deleteWSEndpointMBean(String ctxRoot, WebServiceEndpoint descriptor, 
	String serverName) throws MBeanException;


    // Connector Module

    void createRARModuleMBean(ConnectorDescriptor bundleDesc, 
	String serverName, String appLocation) throws MBeanException;

    void deleteRARModuleMBean(ConnectorDescriptor cd, 
	String serverName) throws MBeanException;

    void createRARMBeans(ConnectorDescriptor cd, 
	String serverName) throws MBeanException;

    void deleteRARMBeans(ConnectorDescriptor cd, 
	String serverName) throws MBeanException;
     
    void createRARMBean(ConnectorDescriptor cd, 
	String serverName) throws MBeanException;

    void deleteRARMBean(ConnectorDescriptor cd, 
	String serverName) throws MBeanException;
     
    void registerResourceAdapterModuleAndItsComponents(ConnectorDescriptor bundleDesc, 
	String serverName, boolean registerComponents, String appLocation) 
	throws MBeanException;


    // Application Client Module

    void createAppClientModuleMBean(String appName, String parentName, 
	String serverName, String deploymentDescriptor) throws MBeanException;

    void deleteAppClientModuleMBean(String appName, String parentName, 
	String serverName) throws MBeanException;

    void registerAppClient(ApplicationClientDescriptor bundleDesc, 
	String serverName, String appLocation) throws MBeanException;
  
    void unregisterAppClient(ApplicationClientDescriptor bundleDesc,
     String serverName) throws MBeanException;

    // State Management

    void setApplicationState(int state, Application application, 
	String serverName) throws MBeanException;

    void setEJBModuleState(int state, EjbBundleDescriptor ejbBundleDescriptor, 
	String serverName) throws MBeanException;

    void setRARModuleState(int state, ConnectorDescriptor connectorDescriptor, 
	String serverName) throws MBeanException;

    Integer getState(String namePattern) throws MBeanException;

    // Utility methods

    String getModuleName(BundleDescriptor bd) throws MBeanException;
    
    String getApplicationName(BundleDescriptor bd);

    ObjectName findObjectName(String namePattern) throws MBeanException;

    String getJ2eeTypeForEjb(EjbDescriptor ejbDescriptor) throws MBeanException;

    void registerTransactionService();

    void registerAllJ2EEClusters();
    void registerJ2EECluster(String clusterName);
    void unregisterJ2EECluster(String clusterName);

}
