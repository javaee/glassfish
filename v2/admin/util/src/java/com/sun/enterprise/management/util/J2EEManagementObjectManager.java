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

package com.sun.enterprise.management.util;

import com.sun.enterprise.ManagementObjectManager;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.meta.MBeanRegistry;
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
import com.sun.enterprise.admin.server.core.AdminService;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.WebServicesDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.io.AppClientDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.ApplicationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.ConnectorDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.EjbDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.WebDeploymentDescriptorFile;
import com.sun.enterprise.deployment.node.J2EEDocumentBuilder;
import com.sun.enterprise.management.agent.MEJB;
import com.sun.enterprise.management.agent.MEJBUtility;
import com.sun.enterprise.management.model.*;
//import com.sun.enterprise.management.model.emma.*;
import com.sun.enterprise.management.util.J2EEModuleCallBack;
import com.sun.enterprise.server.*;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.jmx.trace.Trace;
//import com.sun.management.j2se.MOAgents.EmmaOptions;
//import com.sun.management.j2se.MOAgents.MOAgentFactory;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.logging.*;
import javax.management.*;
import javax.management.modelmbean.ModelMBean;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import com.sun.enterprise.instance.*;
import com.sun.enterprise.deployment.io.DescriptorConstants;
import java.io.*;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.util.io.FileUtils;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.management.j2ee.DASJ2EEServerImpl;
import com.sun.enterprise.admin.mbeans.DomainStatus;
import com.sun.enterprise.admin.mbeans.DomainStatusHelper;
import com.sun.enterprise.admin.mbeans.DomainStatusMBean;
import com.sun.appserv.management.j2ee.StateManageable;

import com.sun.enterprise.management.support.LoaderMBean;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.util.misc.SetUtil;

/* uncomment the following if the HtmlAdaptorServer is needed */
//import com.sun.jdmk.comm.HtmlAdaptorServer;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.j2ee.J2EEDomain;
import com.sun.appserv.management.j2ee.J2EECluster;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.enterprise.management.support.BootUtil;
import com.sun.enterprise.management.support.ObjectNames;
import com.sun.enterprise.management.j2ee.J2EEClusterImpl;
import com.sun.appserv.management.util.jmx.MBeanServerConnectionSource;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.enterprise.management.support.WebModuleSupport;

/**
 * J2EEManagementObjectManager is responsible for managing 
 * all JSR77 managed objects (MBeans).
 * It is a "glue" integration layer between the J2EE RI and
 * JMX (Java Management Extensions) and JSR77 implementations.
 * It includes functionality for initializing JMX,
 * registering and unregistering managed objects, starting and stopping
 * J2EE modules, etc.
 * The register/unregister methods are called from J2EE RI code when the
 * RI objects corresponding to the MBeans are created/removed.
 * RI code uses only the com.sun.enterprise.ManagementObjectManager
 * interface.
 *
 * @author Prakash Aradhya, Sanjeev Krishnan
 */

public class J2EEManagementObjectManager implements ManagementObjectManager {
    
    private MEJB mejbRef = null;
    private MEJBUtility mejbUtility = null;
    private static MBeanServer server = null;
    private static String domain = J2EEDomainMdl.DOMAINNAME;
 //   private static boolean moagentNotSet = true; 
    private static final String QUEUE = "javax.jms.Queue";
    private static final String TOPIC = "javax.jms.Topic";
    private static final String DEFAULT_VIRTUAL_SERVER = "server";

    private static Logger _logger = Logger.getLogger(AdminConstants.kLoggerName);

    /* uncomment the following if the HtmlAdaptorServer is needed */
    //private static HtmlAdaptorServer htmlAdaptorServer = null;

    /* Static initializer for mBean server */
    static {
        if (server == null) {
            initMBeanServer();
        }
        if ((server != null)) {
            registerHTMLAdaptor();
        }
    }
    
    public J2EEManagementObjectManager() {
        
	// set the default managed object agent
	// WARNING: This must be done before any managed objects are created    
	// if (moagentNotSet) {
        	//MOAgentFactory.setAgents(
		//    "com.sun.management.j2se.MOAgents.J2EEAgentImpl", 
		//    "com.sun.management.j2se.MOAgents.J2EEClientImpl");
//		moagentNotSet = false;
//	}
    }

    /* Initialization method for mBean server */
    private static void initMBeanServer() {

        // get or create mBean server
        try {
	    server = MBeanServerFactory.getMBeanServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Register html adaptor
     * Used for testing only
     */
    private static boolean _bHtmlAdaptorServerRegistered = false;
    private static void registerHTMLAdaptor() {
        /* uncomment the following if the HtmlAdaptorServer is needed */
        if(_bHtmlAdaptorServerRegistered)
            return;
        try {
            String strPort = System.getProperty("html.adaptor.port");
            if(strPort==null || strPort.trim().length()<1)
                return;
            //final int freePort = NetUtils.getFreePort();
            int port = (Integer.valueOf(strPort.trim())).intValue();
            Class cl =  Class.forName("com.sun.jdmk.comm.HtmlAdaptorServer");
            Constructor contr = cl.getConstructor(new Class[]{Integer.TYPE});
            Object adaptor = contr.newInstance(new Object[]{Integer.valueOf(port)});
            Method method = cl.getMethod("start");
            ObjectName htmlAdaptorObjectName = new ObjectName(
                    "Adaptor:name=html,port="+port);
            server.registerMBean(adaptor, htmlAdaptorObjectName);
            method.invoke(adaptor);
            _bHtmlAdaptorServerRegistered = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
	
    }

    public MEJBUtility getMEJBUtility(){
        if(this.mejbUtility == null){
            this.mejbUtility = MEJBUtility.getMEJBUtility();
        }
        return this.mejbUtility;
    }
    
    public void registerJ2EEDomain() {
        instantiateAndRegisterRuntimeMBean("J2EEDomain", null);
	registerDomainStatusMBean();
    }
    
    public void registerJ2EEServer() {
        J2EEServerMdl managedServerMdlObject = new J2EEServerMdl(
                ApplicationServer.getServerContext().getInstanceName(),
                com.sun.appserv.server.util.Version.getVersion());
        instantiateAndRegisterRuntimeMBean("J2EEServer", managedServerMdlObject);
    }
    
    public void registerJCAConnectionFactory(String jcaResName, 
				String jcaConnFactName, String managedConFact) 
    {
        Object managedBean = (Object)new JCAConnectionFactoryMdl(
            jcaResName, ApplicationServer.getServerContext().getInstanceName(), 
            jcaConnFactName, managedConFact);
        instantiateAndRegisterRuntimeMBean("JCAConnectionFactory", managedBean, jcaResName, jcaConnFactName);
    } 

    public void registerJCAManagedConnectionFactory(String connFactName) {
        Object managedBean = (Object)new JCAManagedConnectionFactoryMdl(
            connFactName, ApplicationServer.getServerContext().getInstanceName());
        instantiateAndRegisterRuntimeMBean("JCAManagedConnectionFactory", managedBean, connFactName);
    } 

    public void registerJCAResource(String name, String raName, 
                            String username, String password,
                            String[] propNames, String[] propValues) {
        Object managedBean = (Object)new JCAResourceMdl(name,
                                ApplicationServer.getServerContext().getInstanceName(),
                                raName, username, password, propNames, propValues);
        instantiateAndRegisterRuntimeMBean("JCAResource", managedBean, raName, name);
       
    } 

    public void registerAdminObjectResource(String name, String raName, String resType,
                            String[] propNames, String[] propValues) {
        //username and password for nulls in that order
        if(QUEUE.equals(resType) || TOPIC.equals(resType)) {
            registerJMSResource(name, resType, null, null, propNames, propValues);                   
        }
    } 
    
    public void registerJDBCDataSource(String dsJndiName, String dsName, String url,
                            String username, 
                            String password,
                            String[] propNames, 
                            String[] propValues) {
        Object managedBean = (Object)new JDBCDataSourceMdl(dsJndiName, dsName, url,
                                username,
                                ApplicationServer.getServerContext().getInstanceName(),
                                password,
                                propNames, 
                                propValues); 
        instantiateAndRegisterRuntimeMBean("JDBCDataSource", managedBean, dsJndiName, dsJndiName); //FIXME: why no dsJndiName,dsName
    } 

    public void registerJDBCDriver(String name) {
        instantiateAndRegisterRuntimeMBean("JDBCDriver", null, name);
    } 

    public void registerJDBCResource(String name) {
        instantiateAndRegisterRuntimeMBean("JDBCResource", null, name);
    } 


    public void registerJMSResource(String name, String resType,
                                    String username, String password,
                                    String[] propNames, String[] propValues) {
	    Object managedBean = (Object)new JMSResourceMdl(name, 
				                    ApplicationServer.getServerContext().getInstanceName(),
                                    resType, username, password, propNames, propValues);
        instantiateAndRegisterRuntimeMBean("JMSResource", managedBean, name);
    } 

    public void registerJNDIResource(String name) {
        instantiateAndRegisterRuntimeMBean("JNDIResource", null, name);
    } 

    public void registerJTAResource(String name) {
        instantiateAndRegisterRuntimeMBean("JTAResource", null, name);
    }
  
    public void registerJVM() {
        //instantiateAndRegisterRuntimeMBean("JVM", null, 
	//			MOAgentFactory.getAgent().getJVMId());
        instantiateAndRegisterRuntimeMBean("JVM", null, 
				getJVMId());
   }
    
    public void registerJavaMailResource(String name) {
        instantiateAndRegisterRuntimeMBean("JavaMailResource", null, name);
    }

    
    public void registerRMI_IIOPResource(String name) {
        instantiateAndRegisterRuntimeMBean("RMI_IIOPResource", null, name);
    }

    MEJB getMEJB() throws Exception {
        if ( mejbRef == null ) {
            InitialContext ic = new InitialContext();
            java.lang.Object objref = ic.lookup(
			    System.getProperty("mejb.name","ejb/mgmt/MEJB"));
        }
        return mejbRef;
    }
     
    public void unregisterStandAloneModule(String moduleType, String name) {
        try {
            unregisterStandAloneAndItsComponents(moduleType, name);
        } catch ( Exception ex ) {
            throw new RuntimeException(ex);
        }
    }

    private void unregisterStandAloneAndItsComponents(
		String moduleType, String moduleName)throws Exception {
    }

    public void unregisterJCAResource(String moduleName)throws Exception {
    }

    /**
     * Unregisters the jms resource managed object if the type is Queue
     * or Topic.
     */
    public void unregisterAdminObjectResource(
            String name, String resourceType) throws Exception {
        if(QUEUE.equals(resourceType) || TOPIC.equals(resourceType)) {
            unregisterResource(name,"JMSResource");              
        }
    }
    
    public void unregisterJMSResource(String name) throws Exception {
        unregisterResource(name,"JMSResource");
    }   

    public void unregisterJavaMailResource(String name) throws Exception {
        unregisterResource(name,"JavaMailResource");
    }
    
    public void unregisterJDBCResource(String name) throws Exception {
        unregisterResource(name,"JDBCResource");
    }

    
    /**
     * Unregisters a resource mbean given the resource jndi name and
     * the Managed Object type such as JDBCResource, JavaMailResource, etc.
     *
     * @param name The jndi name of the resource to be unregistered.
     * @param type The managed object type such as JDBCResource.
     * @throws Exception if the mbean cannot be unregistered.
     */
    void unregisterResource(String name, String type) throws Exception {
        final MBeanRegistry registry =
                MBeanRegistryFactory.getRuntimeMBeanRegistry();
        final String domainName =
                ApplicationServer.getServerContext().getDefaultDomainName();
        final String serverName =
                ApplicationServer.getServerContext().getInstanceName();
        final String[] location = new String[] {domainName, serverName, name};
        final ObjectName on = registry.getMbeanObjectName(type, location);
        if (on == null) {
            return;
        }
        final MBeanServer mbs =
               AdminService.getAdminService().getAdminContext().getMBeanServer();
        if (mbs.isRegistered(on)) {
            mbs.unregisterMBean(on);
        }
    }
    
    
    public Object getModuleUserData(
	String moduleID, int moduleType) throws Exception{
        return null;
    }
    
    public void setModuleUserData(
	String moduleID, Object obj, int moduleType)throws Exception{
    }

    private ObjectName getManagedObjectModule(
	String moduleID, int moduleType) throws Exception{
        
        return null;
    } 
    
       
       
    // Adding all the config mbeans at the end. we may move this to another file later
    //FIXME
    public void registerConfigJ2EEServer(String instance) {
throw new RuntimeException(">>>>>>>>>>>>>>>>>>>> registerConfigJ2EEServer not implemented <<<<<<<<<<<<<<<<<");
	//new J2EEServerMdlEmmaAgent(new EmmaOptions(J2EEDomainMdl.DOMAINNAME));
    }       


    //helper method for Runtime MBeanInstantiation

    private void instantiateAndRegisterRuntimeMBean(String type, Object managed) 
    {
        instantiateAndRegisterRuntimeMBean(type, managed, null, null, null);
    }


    //helper method for Runtime MBeanInstantiation
    private void instantiateAndRegisterRuntimeMBean(
	String type, Object managed, String name) 
    {
        instantiateAndRegisterRuntimeMBean(type, managed, null, null, name);
    }

    //helper method for Runtime MBeanInstantiation
    private void instantiateAndRegisterRuntimeMBean(
	String type, Object managed, String parentName, String name) 
    {
        instantiateAndRegisterRuntimeMBean(type, managed, null, parentName, name);
    }


    //helper method for Runtime MBeanInstantiation

    private void instantiateAndRegisterRuntimeMBean(
	String type, Object managed, String grandParentName, 
	String parentName, String name) {

        MBeanRegistry registry  = MBeanRegistryFactory.getRuntimeMBeanRegistry();
        String domainName = ApplicationServer.getServerContext().getDefaultDomainName();
        String serverName = ApplicationServer.getServerContext().getInstanceName();
 
        try {
            if(grandParentName!=null) {

               registry.instantiateMBean(
			type, 
			new String[]{domainName, serverName, grandParentName, 
				     parentName, name}, 
			managed, AdminService.getAdminService().getAdminContext().getAdminConfigContext(), true); //FIXME
            } else {
                if(parentName!=null) {
                   registry.instantiateMBean( 
				type, 
				new String[]{domainName, serverName, 
				             parentName, name}, 
				managed, AdminService.getAdminService().getAdminContext().getAdminConfigContext(), true); //FIXME
                } else {
                    if(name!=null) {
                       registry.instantiateMBean( 
				type, new String[]{domainName, serverName,name}, 
				managed, AdminService.getAdminService().getAdminContext().getAdminConfigContext(), true); //FIXME
                    } else {
                       registry.instantiateMBean( 
				type, new String[]{domainName, serverName}, 
				managed, AdminService.getAdminService().getAdminContext().getAdminConfigContext(), true); //FIXME
		    }
		}
	    }
        } catch (Exception e) {
            System.out.println("Exception msg:"+e.getMessage());
            e.printStackTrace();
        }
    } 

    // code for deploy objects

    /* Create application mBean */
    public void createAppMBean(Application application, String serverName, 
	String appLocation)
        throws MBeanException {

	// get the standard deployment descriptor file
        String xmlDesc = null;

        String j2eeDDLocation = application.getGeneratedXMLDirectory() + 
                                File.separator + 
                                DescriptorConstants.APPLICATION_DD_ENTRY;

        // get the string for deployment descriptor file
        xmlDesc = getStringForDDxml(j2eeDDLocation);

        StartStopCallback sscb = new StartStopCallback();

        J2EEModuleCallBackImpl module = new J2EEModuleCallBackImpl(
                application.getRegistrationName(),
                application.getRegistrationName(), serverName, xmlDesc, sscb);

	J2EEApplicationMdl managedResource = new J2EEApplicationMdl(module, application);
	instantiateAndRegisterRuntimeMBean( "J2EEApplication", managedResource, 
		application.getRegistrationName());

    }

    /* Delete application mBean */
    public void deleteAppMBean(Application application, String serverName) 
        throws MBeanException {

        try {
		String namePattern = 
			(domain + ":" + 
		 	"j2eeType=J2EEApplication" + "," + 
		 	"name=" + application.getRegistrationName() + "," + 
		 	"J2EEServer=" + serverName + "," +
		 	"*");

		ObjectName objName = findObjectName(namePattern);

		if (objName != null) {
            		unregister(objName);
		}
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /**
     * Create application mBean modules
     * J2EE Application may have several modules. According to JSR77
     * spec. each module will have an mBean.
     */
    public void createAppMBeanModules(Application application,
        String serverName, String appLocation) throws MBeanException {

        boolean registerComponents = false;

        // Register EJB modules
        java.util.Set ejbBundles = application.getEjbBundleDescriptors();

        for (Iterator it = ejbBundles.iterator(); it.hasNext();) {
            EjbBundleDescriptor bundleDesc = (EjbBundleDescriptor) it.next();
            registerEjbModuleAndItsComponents(bundleDesc, serverName,
                registerComponents, appLocation);
        }

	// Web module registration will be done within Tomcat container
	// FIXME: when webcore based regn needs to be done during 8.0 SE/EE
        // Register Web modules
	/*
        java.util.Set webBundles = application.getWebBundleDescriptors();

        for (Iterator it = webBundles.iterator(); it.hasNext();) {
            WebBundleDescriptor bundleDesc = (WebBundleDescriptor) it.next();
            registerWebModuleAndItsComponents(bundleDesc, serverName,
                registerComponents, appLocation);
        }
	*/

        // Register RAR modules
        java.util.Set rarBundles = application.getRarDescriptors();

        for (Iterator it = rarBundles.iterator(); it.hasNext();) {
            ConnectorDescriptor bundleDesc = (ConnectorDescriptor) it.next();
            registerResourceAdapterModuleAndItsComponents(bundleDesc,
                serverName, registerComponents, appLocation);
        }

        // Register AppClient modules
        java.util.Set appClientBundles = application.getApplicationClientDescriptors();

        for (Iterator it = appClientBundles.iterator(); it.hasNext();) {
            ApplicationClientDescriptor bundleDesc = (ApplicationClientDescriptor) it.next();
            registerAppClient(bundleDesc, serverName, appLocation);
        }
    }

    /**
     * Register all the modules of the application and its' children
     * objects.
     */
    public void createAppMBeans(Application application,
        String serverName, String appLocation) throws MBeanException {

        boolean registerComponents = true;

        // Register EJB modules
        java.util.Set ejbBundles = application.getEjbBundleDescriptors();

        for (Iterator it = ejbBundles.iterator(); it.hasNext();) {
            EjbBundleDescriptor bundleDesc = (EjbBundleDescriptor) it.next();
            registerEjbModuleAndItsComponents(bundleDesc, serverName,
                registerComponents, appLocation);
        }

	// Web module registration will be done within Tomcat container
	// FIXME: when webcore based regn needs to be done during 8.0 SE/EE
        // Register Web modules
	/*
        java.util.Set webBundles = application.getWebBundleDescriptors();

        for (Iterator it = webBundles.iterator(); it.hasNext();) {
            WebBundleDescriptor bundleDesc = (WebBundleDescriptor) it.next();
            registerWebModuleAndItsComponents(bundleDesc, serverName,
                registerComponents, appLocation);
        }
	*/

        // Register RAR modules
        java.util.Set rarBundles = application.getRarDescriptors();

        for (Iterator it = rarBundles.iterator(); it.hasNext();) {
            ConnectorDescriptor bundleDesc = (ConnectorDescriptor) it.next();
            registerResourceAdapterModuleAndItsComponents(bundleDesc,
                serverName, registerComponents, appLocation);
        }

        // Register AppClient modules
        java.util.Set appClientBundles = application.getApplicationClientDescriptors();

        for (Iterator it = appClientBundles.iterator(); it.hasNext();) {
            ApplicationClientDescriptor bundleDesc = (ApplicationClientDescriptor) it.next();
            registerAppClient(bundleDesc, serverName, appLocation);
        }
    }

    /* Register ejb module and its' children ejbs which is part of an application */
    public void registerEjbModuleAndItsComponents(
        EjbBundleDescriptor ejbBundleDescriptor, String serverName,
        boolean registerComponents, String appLocation) throws MBeanException {


        createEJBModuleMBean(ejbBundleDescriptor, serverName, appLocation);

        if (registerComponents) {
            createEJBMBeans(ejbBundleDescriptor, serverName);
        }
    }

    /* Register web module and its' children which is part of an application */
    public void registerWebModuleAndItsComponents(
        WebBundleDescriptor webBundleDescriptor, String serverName,
        boolean registerComponents, String appLocation) throws MBeanException {

	// get the string for deployment descriptor file
	String xmlDesc = getStringForDDxml(
                getModuleLocation(webBundleDescriptor, "WebModule"));

        String applicationName = "null";

        if (webBundleDescriptor.getApplication() != null) {
            if (!webBundleDescriptor.getModuleDescriptor().isStandalone()) {
                applicationName = webBundleDescriptor.getApplication()
                                                     .getRegistrationName();

                if (applicationName == null) {
                    applicationName = "null";
                }
            }
        }

        createWebModuleMBean(webBundleDescriptor.getModuleID(),
            applicationName, serverName, xmlDesc);

        if (registerComponents) {
            createWebMBeans(webBundleDescriptor, serverName);
        }
        try {
            createWSEndpointMBeans(webBundleDescriptor, serverName);
        } catch (MBeanException mbe) {
            _logger.log(Level.WARNING, 
                "admin.registerWebModuleAndItsComponents exception", mbe);
        }
    }

    /* Register connector module and its' children which is part of an application */
    public void registerResourceAdapterModuleAndItsComponents(
        ConnectorDescriptor bundleDesc, String serverName,
        boolean registerComponents, String appLocation) throws MBeanException {



        createRARModuleMBean(bundleDesc, serverName, appLocation);

        if (registerComponents) {
            createRARMBeans(bundleDesc, serverName);
        }
    }

    /* Register application client module */
    public void registerAppClient(
        ApplicationClientDescriptor bundleDesc, String serverName,
	String appLocation)
        throws MBeanException {

	// get the string for deployment descriptor file
        String xmlDesc = getStringForDDxml(
                getModuleLocation(bundleDesc, "AppClientModule"));

        String applicationName = "null";

        if (bundleDesc.getApplication() != null) {
            if (!bundleDesc.getModuleDescriptor().isStandalone()) {
                applicationName = bundleDesc.getApplication()
                                            .getRegistrationName();

                if (applicationName == null) {
                    applicationName = "null";
                }
            }
        }

        createAppClientModuleMBean(getModuleName(bundleDesc), applicationName,
            serverName, xmlDesc);
    }

    /* Unregister application client module */
    public void unregisterAppClient(
        ApplicationClientDescriptor bundleDesc, String serverName)
        throws MBeanException {

        String applicationName = "null";

        if (bundleDesc.getApplication() != null) {
            if (!bundleDesc.getModuleDescriptor().isStandalone()) {
                applicationName = bundleDesc.getApplication()
                                            .getRegistrationName();

                if (applicationName == null) {
                    applicationName = "null";
                }
            }
        }

        deleteAppClientModuleMBean(getModuleName(bundleDesc), applicationName,
            serverName);
    }


    /* Delete all mbeans of the application including modules and its' children */
    public void deleteAppMBeans(Application application,
        String serverName) throws MBeanException {

        try {
	    String namePattern = 
			(domain + ":" + 
		 	"j2eeType=J2EEApplication" + "," + 
		 	"name=" + application.getRegistrationName() + "," + 
		 	"J2EEServer=" + serverName + "," +
		 	"*");

	    ObjectName objName = findObjectName(namePattern);

	    if (objName != null) {
            	String[] modulesArr = (String[]) server.getAttribute(objName,
                    "modules");
            	ObjectName[] deployedModules = toObjectNameArray(modulesArr);

            	for (int moduleIndex = 0; moduleIndex < deployedModules.length;
                    moduleIndex++) {
                	unregisterModuleAndItsComponents(deployedModules[moduleIndex]);
            	}
	    }
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /* used by deleteAppMBeans */
    public void unregisterModuleAndItsComponents(ObjectName objectName)
        throws MBeanException {

        // unregister its components
        ObjectName[] comps = getComponents(objectName);

        for (int compsIndex = 0; compsIndex < comps.length; compsIndex++) {
            unregister(comps[compsIndex]);
        }

        // unregister module itself
        unregister(objectName);
    }

    /* Get child components of a given object say module */
    private ObjectName[] getComponents(ObjectName module)
        throws MBeanException {

        ObjectName[] components = null;

        try {
          if (module.getKeyProperty("j2eeType").equals("AppClientModule")) {
            // There should not be any components for app client module
            components = new ObjectName[] {  };
          } else if (module.getKeyProperty("j2eeType").equals("EJBModule")) {
            components = toObjectNameArray((String[]) server.getAttribute(
                        module, "ejbs"));
          } else if (module.getKeyProperty("j2eeType").equals("WebModule")) {
            components = toObjectNameArray((String[]) server.getAttribute(
                        module, "servlets"));
          } else if (module.getKeyProperty("j2eeType").equals("ResourceAdapterModule")) {
            components = toObjectNameArray((String[]) server.getAttribute(
                        module, "resourceAdapters"));
          }
        } catch (Exception e) {
          throw new MBeanException(e);
        }

        return components;
    }

    /* Create ejb module mBean */
    public void createEJBModuleMBean(EjbBundleDescriptor ejbBundleDescriptor,
        String serverName, String appLocation) throws MBeanException {


	// get the string for deployment descriptor file
        String xmlDesc = getStringForDDxml(
                getModuleLocation(ejbBundleDescriptor, "EJBModule"));

        String moduleName = getModuleName(ejbBundleDescriptor);
        String applicationName = getApplicationName(ejbBundleDescriptor);

        StartStopCallback sscb = new StartStopCallback();
        J2EEModuleCallBackImpl module = new J2EEModuleCallBackImpl(moduleName,
                applicationName, serverName, xmlDesc, sscb);

	EJBModuleMdl managedResource = new EJBModuleMdl(module, ejbBundleDescriptor);

	instantiateAndRegisterRuntimeMBean( "EJBModule", managedResource, 
		null, applicationName, moduleName);
        try {
            createWSEndpointMBeans(ejbBundleDescriptor, serverName);
        } catch (MBeanException mbe) {
            _logger.log(Level.WARNING, 
                "admin.registerEjbModuleAndItsComponents exception", mbe);
        }
    }

    /* Delete ejb module mBean */
    public void deleteEJBModuleMBean(EjbBundleDescriptor ejbBundleDescriptor,
        String serverName) throws MBeanException {


        String moduleName = getModuleName(ejbBundleDescriptor);
        String applicationName = getApplicationName(ejbBundleDescriptor);

        try {
		String namePattern = 
			(domain + ":" + 
		 	"j2eeType=EJBModule" + "," + 
		 	"name=" + moduleName + "," + 
		 	"J2EEApplication=" + applicationName + "," + 
		 	"J2EEServer=" + serverName + "," +
		 	"*");

		ObjectName objName = findObjectName(namePattern);

	    	if (objName != null) {
            		unregister(objName);
	    	}
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /* Create ejb mBeans within a given bundle descriptor */
    public void createEJBMBeans(
        EjbBundleDescriptor ejbBundleDescriptor, String serverName)
        throws MBeanException {


        //Vector beanDescriptors = application.getEjbDescriptors();
        java.util.Set ejbs = ejbBundleDescriptor.getEjbs();

        for (Iterator it = ejbs.iterator(); it.hasNext();) {
            EjbDescriptor nextDescriptor = (EjbDescriptor) it.next();

            createEJBMBean(nextDescriptor, serverName);
        }
    }

    /* Delete ejb mBeans within a given bundle descriptor */
    public void deleteEJBMBeans(
        EjbBundleDescriptor ejbBundleDescriptor, String serverName)
        throws MBeanException {


        java.util.Set ejbs = ejbBundleDescriptor.getEjbs();

        for (Iterator it = ejbs.iterator(); it.hasNext();) {

            EjbDescriptor nextDescriptor = (EjbDescriptor) it.next();

            deleteEJBMBean(nextDescriptor, serverName);
        }

    }

    /**
     * Creates all the web service endpoint runtime mbeans for the web or
     * ejb bundle.
     *
     * @param bundleDescriptor descriptor of the web/ejb bundle
     * @param serverName       instance on which these mbeans are created
     *
     * @throws MBeanException  incase of error
     */
    public void createWSEndpointMBeans(
        BundleDescriptor bundleDescriptor, String serverName)
        throws MBeanException { 

        if ( bundleDescriptor  == null) {
            return;
        }

        String ctxRoot = null;
        if ( bundleDescriptor instanceof WebBundleDescriptor) {
            ctxRoot = ((WebBundleDescriptor) bundleDescriptor).getContextRoot();
        }

        WebServicesDescriptor wss = bundleDescriptor.getWebServices();
        if ( wss != null) {
            java.util.Collection wsEps = wss.getEndpoints();

            for (Iterator it = wsEps.iterator(); it.hasNext();) {
                WebServiceEndpoint nextDescriptor = 
                        (WebServiceEndpoint) it.next();

                createWSEndpointMBean(ctxRoot, nextDescriptor, serverName);
            }
        }
    }

    /**
     * Deletes all the web service endpoint runtime mbeans for the web or
     * ejb bundle.
     *
     * @param bundleDescriptor descriptor of the web/ejb bundle
     * @param serverName       instance on which these mbeans are deleted
     *
     * @throws MBeanException  incase of error
     */
    public void deleteWSEndpointMBeans(
        BundleDescriptor bundleDescriptor, String serverName)
        throws MBeanException {

        if ( bundleDescriptor == null) {
            return;
        }

        String ctxRoot = null;
        if ( bundleDescriptor instanceof WebBundleDescriptor) {
            ctxRoot = ((WebBundleDescriptor) bundleDescriptor).getContextRoot();
        }

        WebServicesDescriptor wsd = bundleDescriptor.getWebServices();
        if ( wsd == null) {
            return;
        }

        java.util.Collection wsEps = wsd.getEndpoints();

        for (Iterator it = wsEps.iterator(); it.hasNext();) {
            WebServiceEndpoint nextDescriptor = (WebServiceEndpoint) it.next();

            try {
                deleteWSEndpointMBean(ctxRoot, nextDescriptor, serverName);
            } catch (MBeanException mbe) {
                _logger.log(Level.WARNING, 
                    "admin.deleteEJBMBeans exception", mbe);
            }
        }


    }

    /* Create mBean for a given ejb and register it with mBean server*/
    public void createEJBMBean(EjbDescriptor ejbDescriptor, String serverName)
        throws MBeanException {


        String ejbName = ejbDescriptor.getName();
        String ejbType = ejbDescriptor.getType();
        String ejbSessionType = null;
        if (ejbType.equals("Session")) {
            ejbSessionType = ((EjbSessionDescriptor) ejbDescriptor).getSessionType();
	}

        String moduleName = getModuleName(ejbDescriptor.getEjbBundleDescriptor());
        String appName = getApplicationName(ejbDescriptor.getEjbBundleDescriptor());

        Object ejbMB = null;
        String j2eeType = null;

        if (ejbType.equals("Entity")) {
            j2eeType = "EntityBean";
        } else if (ejbType.equals("Message-driven")) {
            j2eeType = "MessageDrivenBean";
        } else if (ejbType.equals("Session")) {
            if (ejbSessionType.equals("Stateless")) {
                j2eeType = "StatelessSessionBean";
            } else if (ejbSessionType.equals("Stateful")) {
                j2eeType = "StatefulSessionBean";
            }
        }

        if (ejbType.equals("Entity")) {
	    EntityBeanMdl managedResource = 
		new EntityBeanMdl(ejbName, moduleName, appName, serverName);
	    instantiateAndRegisterRuntimeMBean("EntityBean", managedResource,
		appName, moduleName, ejbName);
        } else if (ejbType.equals("Message-driven")) {
	    MessageDrivenBeanMdl managedResource = 
		new MessageDrivenBeanMdl(
		ejbName, moduleName, appName, serverName);
	    instantiateAndRegisterRuntimeMBean("MessageDrivenBean", managedResource,
		appName, moduleName, ejbName);
        } else if (ejbType.equals("Session")) {
            if (ejbSessionType.equals("Stateless")) {
	    	StatelessSessionBeanMdl managedResource = 
			new StatelessSessionBeanMdl(
			ejbName, moduleName, appName, serverName);
	    	instantiateAndRegisterRuntimeMBean("StatelessSessionBean", managedResource,
			appName, moduleName, ejbName);
            } else if (ejbSessionType.equals("Stateful")) {
	    	StatefulSessionBeanMdl managedResource = 
			new StatefulSessionBeanMdl(
			ejbName, moduleName, appName, serverName);
	    	instantiateAndRegisterRuntimeMBean("StatefulSessionBean", managedResource,
			appName, moduleName, ejbName);
            }
        }

    }

    /* Delete the mBean for a given ejb and unregister it from mBean server */
    public void deleteEJBMBean(EjbDescriptor ejbDescriptor, String serverName)
        throws MBeanException {


        String ejbName = ejbDescriptor.getName();
        String ejbType = ejbDescriptor.getType();
        String ejbSessionType = null;
        if (ejbType.equals("Session")) {
            ejbSessionType = ((EjbSessionDescriptor) ejbDescriptor).getSessionType();
	}

        String moduleName = getModuleName(ejbDescriptor.getEjbBundleDescriptor());
        String appName = getApplicationName(ejbDescriptor.getEjbBundleDescriptor());

        String j2eeType = null;

        if (ejbType.equals("Entity")) {
            j2eeType = "EntityBean";
        } else if (ejbType.equals("Message-driven")) {
            j2eeType = "MessageDrivenBean";
        } else if (ejbType.equals("Session")) {
            if (ejbSessionType.equals("Stateless")) {
                j2eeType = "StatelessSessionBean";
            } else if (ejbSessionType.equals("Stateful")) {
                j2eeType = "StatefulSessionBean";
            }
        }

        try {
		String namePattern = 
			(domain + ":" + 
		 	"j2eeType=" + j2eeType + "," + 
		 	"name=" + ejbName + "," + 
			"EJBModule=" + moduleName + "," + 
		 	"J2EEApplication=" + appName + "," + 
		 	"J2EEServer=" + serverName + "," +
		 	"*");

		ObjectName objName = findObjectName(namePattern);

	    	if (objName != null) {
            		unregister(objName);
	    	}
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /**
     * Creates the specified web service endpoint runtime mbean
     *
     * @param ctxRoot          context root for this web service endpoint
     * @param descriptor       descriptor of the web service endpoint
     * @param serverName       instance on which these mbean is created
     *
     * @throws MBeanException  incase of error
     */
    public void createWSEndpointMBean(String ctxRoot,
     WebServiceEndpoint wsDescriptor, 
        String serverName) throws MBeanException {


        BundleDescriptor bd = wsDescriptor.getBundleDescriptor();
        boolean isEjb = false;

        if ( bd instanceof EjbBundleDescriptor) {
            isEjb = true;
        }

        String epName = wsDescriptor.getEndpointName();
        String appName =  getApplicationName(bd);
        String regName = bd.getApplication().getRegistrationName();
        String moduleName = getModuleName(bd);

        if (ctxRoot != null ) {
            ServletWebServiceEndpointMdl managedResource = 
                new ServletWebServiceEndpointMdl(epName, moduleName, regName, 
                    serverName, bd.getApplication().isVirtual(), isEjb);
            String cRoot = null;
            if (( ctxRoot.length() > 0) && (ctxRoot.charAt(0) != '/')){
                cRoot = "/" + ctxRoot;
            } else {
                cRoot = ctxRoot;
            }
            instantiateAndRegisterRuntimeMBean(managedResource.getMBeanName(),
             managedResource, appName, WebModuleSupport.VIRTUAL_SERVER_PREFIX
             + DEFAULT_VIRTUAL_SERVER + cRoot, epName);
        } else {
            EJBWebServiceEndpointMdl managedResource = 
            new EJBWebServiceEndpointMdl(epName, moduleName,regName, serverName,
             bd.getApplication().isVirtual(),isEjb);
            instantiateAndRegisterRuntimeMBean(managedResource.getMBeanName(),
            managedResource, appName, moduleName, epName);
        }
    }

    /**
     * Deletes the specified web service endpoint runtime mbean
     *
     * @param descriptor       descriptor of the web service endpoint
     * @param serverName       instance on which these mbean is deleted
     *
     * @throws MBeanException  incase of error
     */
    public void deleteWSEndpointMBean(String ctxRoot,
        WebServiceEndpoint wsDescriptor, String serverName) 
        throws MBeanException {

        String epName = wsDescriptor.getEndpointName();
        BundleDescriptor bd = wsDescriptor.getBundleDescriptor();

        String moduleName = getModuleName(bd);
        String appName = getApplicationName(bd);

        try {
		String namePattern = null;

        if ( bd instanceof EjbBundleDescriptor) {
			namePattern = (domain + ":" + 
		 	"j2eeType=WebServiceEndpoint," + 
		 	"name=" + epName + "," + 
			"EJBModule=" + moduleName + "," + 
		 	"J2EEApplication=" + appName + "," + 
		 	"J2EEServer=" + serverName + "," +
		 	"*");
        } else if ( bd instanceof WebBundleDescriptor) {
			namePattern = (domain + ":" + 
		 	"j2eeType=WebServiceEndpoint," + 
		 	"name=" + epName + "," + 
			"WebModule=" + WebModuleSupport.VIRTUAL_SERVER_PREFIX
             + DEFAULT_VIRTUAL_SERVER + ctxRoot + "," + 
		 	"J2EEApplication=" + appName + "," + 
		 	"J2EEServer=" + serverName + "," +
		 	"*");
        }

		ObjectName objName = findObjectName(namePattern);

	    	if (objName != null) {
            		unregister(objName);
	    	} 
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /* Create mBean for the given web module and register it with mBean server */
    /*
    public WebModuleMBean getWebModuleMBean(String appName, String parentName,
        String serverName, String deploymentDescriptor) {

        StartStopCallback sscb = new StartStopCallback();
        J2EEModuleCallBackImpl module = new J2EEModuleCallBackImpl(appName,
                parentName, serverName, deploymentDescriptor, sscb);
        WebModuleMBean webModMB = new WebModuleMBean(module);

        return webModMB;
    }
    */


    /* Create mBean for the given web module and register it with mBean server */
    public void createWebModuleMBean(String appName, String parentName,
        String serverName, String deploymentDescriptor)
        throws MBeanException {

        StartStopCallback sscb = new StartStopCallback();
        J2EEModuleCallBackImpl module = new J2EEModuleCallBackImpl(appName,
                parentName, serverName, deploymentDescriptor, sscb);

        WebModuleMdl managedResource = new WebModuleMdl(module);

        instantiateAndRegisterRuntimeMBean( "WebModule", managedResource, 
		null, parentName, appName);
    }

    /* Delete mBean for the given web module by unregistering it from mBean server */
    public void deleteWebModuleMBean(String appName, String parentName,
        String serverName) throws MBeanException {

        try {
		String namePattern = 
			(domain + ":" + 
		 	"j2eeType=WebModule" + "," + 
		 	"name=" + appName + "," + 
		 	"J2EEApplication=" + parentName + "," + 
		 	"J2EEServer=" + serverName + "," +
		 	"*");

		ObjectName objName = findObjectName(namePattern);

	    	if (objName != null) {
            		unregister(objName);
	    	}
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /* Create all web mBeans within the given webBundleDescriptor */
    public void createWebMBeans(
        WebBundleDescriptor webBundleDescriptor, String serverName)
        throws MBeanException {

        Set set = webBundleDescriptor.getWebComponentDescriptorsSet();

        String applicationName = webBundleDescriptor.getApplication()
                                                    .getRegistrationName();

        if (applicationName == null) {
            applicationName = "null";
        }

        for (Iterator it = set.iterator(); it.hasNext();) {
            WebComponentDescriptor nextDescriptor = (WebComponentDescriptor) it.next();
            createWebMBean(nextDescriptor.getCanonicalName(),
                webBundleDescriptor.getModuleID(), applicationName, serverName);
        }
    }

    /* Delete all web mBeans within the given webBundleDescriptor */
    public void deleteWebMBeans(
        WebBundleDescriptor webBundleDescriptor, String serverName)
        throws MBeanException {

        Set set = webBundleDescriptor.getWebComponentDescriptorsSet();

        String applicationName = webBundleDescriptor.getApplication()
                                                    .getRegistrationName();

        if (applicationName == null) {
            applicationName = "null";
        }

        for (Iterator it = set.iterator(); it.hasNext();) {
            WebComponentDescriptor nextDescriptor = (WebComponentDescriptor) it.next();
            deleteWebMBean(nextDescriptor.getCanonicalName(),
                webBundleDescriptor.getModuleID(), applicationName, serverName);
        }
    }

    /* Create mBean for the given servlet */
    public void createWebMBean(String servletName, String moduleName,
        String appName, String serverName) throws MBeanException {

	ServletMdl managedResource = new ServletMdl(servletName, moduleName,
		appName, serverName);

        instantiateAndRegisterRuntimeMBean( "Servlet", managedResource, 
            appName, moduleName, servletName);

    }

    /* Delete mBean for the given servlet */
    public void deleteWebMBean(String servletName, String moduleName,
        String appName, String serverName) throws MBeanException {

        if (server == null) {
            initMBeanServer();
        }

        try {
		String namePattern = 
			(domain + ":" + 
		 	"j2eeType=Servlet" + "," + 
		 	"name=" + servletName + "," + 
			"WebModule=" + moduleName + "," +
		 	"J2EEApplication=" + appName + "," + 
		 	"J2EEServer=" + serverName + "," +
		 	"*");
		ObjectName objName = findObjectName(namePattern);

	    	if (objName != null) {
            		unregister(objName);
	    	}
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /* Create mBean for the given connector module */
    public void createRARModuleMBean(ConnectorDescriptor bundleDesc, 
        String serverName, String appLocation) throws MBeanException {

	// get the string for deployment descriptor file

	// fix for CTS bug# 6411637
        // if resource adapter module name is one of connector system apps
        // then set the location of deployment descriptor to the original
        // location and not the generated directory. These system apps are
        // directly loaded without generating descriptors

        String modLocation = "";
	if (bundleDesc.getModuleDescriptor().isStandalone()) {
            modLocation = appLocation + File.separator + 
                DescriptorConstants.RAR_DD_ENTRY;
        } else {
	    String moduleName = FileUtils.makeFriendlyFileName(
			 bundleDesc.getModuleDescriptor().getArchiveUri());
	    modLocation = appLocation + File.separator + moduleName + File.separator +
			  DescriptorConstants.RAR_DD_ENTRY;
        }

        String xmlDesc = getStringForDDxml(modLocation);

        String applicationName = getApplicationName(bundleDesc);

        String resAdName = getModuleName(bundleDesc);

        StartStopCallback sscb = new StartStopCallback();
        J2EEModuleCallBackImpl module = new J2EEModuleCallBackImpl(resAdName,
                applicationName, serverName, xmlDesc, sscb);

	ResourceAdapterModuleMdl managedResource = 
		new ResourceAdapterModuleMdl(module, resAdName);

	instantiateAndRegisterRuntimeMBean( "ResourceAdapterModule", managedResource, 
		null, applicationName, resAdName);

    }

    /* Delete mBean for the given connector module */
    public void deleteRARModuleMBean(ConnectorDescriptor cd,
        String serverName) throws MBeanException {


        String moduleName = getModuleName(cd);
        String applicationName = getApplicationName(cd);

        try {
		String namePattern = 
			(domain + ":" + 
		 	"j2eeType=ResourceAdapterModule" + "," + 
		 	"name=" + moduleName + "," + 
		 	"J2EEApplication=" + applicationName + "," + 
		 	"J2EEServer=" + serverName + "," +
		 	"*");

		ObjectName objName = findObjectName(namePattern);

	    	if (objName != null) {
            		unregister(objName);
	    	}
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /* Create all mBeans for the given connector descriptor */
    public void createRARMBeans(ConnectorDescriptor cd,
        String serverName) throws MBeanException {

        createRARMBean(cd, serverName);
    }

    /* Delete all mBeans for the given connector descriptor */
    public void deleteRARMBeans(ConnectorDescriptor cd,
        String serverName) throws MBeanException {


        deleteRARMBean(cd, serverName);
    }

    /* Create mBean for the given connector */
    public void createRARMBean(ConnectorDescriptor cd,
        String serverName) throws MBeanException {


        String rarName = cd.getName();
        String moduleName = getModuleName(cd);
        String appName = getApplicationName(cd);

	ResourceAdapterMdl managedResource = 
		new ResourceAdapterMdl(rarName, moduleName, appName, serverName);

	instantiateAndRegisterRuntimeMBean( "ResourceAdapter", managedResource, 
		appName, moduleName, rarName);

    }

    /* Delete mBean for the given connector */
    public void deleteRARMBean(ConnectorDescriptor cd,
        String serverName) throws MBeanException {


        String rarName = cd.getName();
        String moduleName = getModuleName(cd);
        String appName = getApplicationName(cd);

        try {
		String namePattern = 
			(domain + ":" + 
		 	"j2eeType=ResourceAdapter" + "," + 
		 	"name=" + rarName + "," + 
			"ResourceAdapterModule=" + moduleName + "," +
		 	"J2EEApplication=" + appName + "," + 
		 	"J2EEServer=" + serverName + "," +
		 	"*");
		ObjectName objName = findObjectName(namePattern);

	    	if (objName != null) {
            		unregister(objName);
	    	}
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /* Create mBean for the given application client module */
    public void createAppClientModuleMBean(String appName,
        String parentName, String serverName, String deploymentDescriptor)
        throws MBeanException {

        StartStopCallback sscb = new StartStopCallback();
        J2EEModuleCallBackImpl module = new J2EEModuleCallBackImpl(appName,
                parentName, serverName, deploymentDescriptor, sscb);

	AppClientModuleMdl managedResource = new AppClientModuleMdl(module);
	instantiateAndRegisterRuntimeMBean( "AppClientModule", managedResource, 
		null, parentName, appName);
    }

    /* Delete mBean for the given application client module */
    public void deleteAppClientModuleMBean(String applicationName,
        String parentName, String serverName) throws MBeanException {

        try {
		String namePattern = 
			(domain + ":" + 
		 	"j2eeType=AppClientModule" + "," + 
		 	"name=" + applicationName + "," + 
		 	"J2EEApplication=" + parentName + "," + 
		 	"J2EEServer=" + serverName + "," +
		 	"*");
		ObjectName objName = findObjectName(namePattern);
	    	if (objName != null) {
            		unregister(objName);
	    	}
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /* Set state for the given application */
    public void setApplicationState(int state, Application application, 
        String serverName) throws MBeanException {

        String applicationName = application.getRegistrationName();
	String namePattern = 
		(domain + ":" + 
		 "j2eeType=J2EEApplication" + "," + 
		 "name=" + applicationName + "," + 
		 "J2EEServer=" + serverName + "," +
		 "*");
	ObjectName objName = findObjectName(namePattern);
	if (objName != null) {
		setState(objName, state);
	}
    }

    /* Set state for the connector module */
    public void setRARModuleState(int state, ConnectorDescriptor connectorDescriptor,
        String serverName) throws MBeanException {

        String moduleName = getModuleName(connectorDescriptor);
        String applicationName = getApplicationName(connectorDescriptor);
	String namePattern = 
		(domain + ":" + 
		 "j2eeType=ResourceAdapterModule" + "," + 
		 "name=" + moduleName + "," + 
		 "J2EEApplication=" + applicationName + "," + 
		 "J2EEServer=" + serverName + "," +
		 "*");
	ObjectName objName = findObjectName(namePattern);
	if (objName != null) {
		setState(objName, state);
	}
    }


    /* Set state for the ejb module */
    public void setEJBModuleState(int state, EjbBundleDescriptor ejbBundleDescriptor,
        String serverName) throws MBeanException {

        String moduleName = getModuleName(ejbBundleDescriptor);
        String applicationName = getApplicationName(ejbBundleDescriptor);
	String namePattern = 
		(domain + ":" + 
		 "j2eeType=EJBModule" + "," + 
		 "name=" + moduleName + "," + 
		 "J2EEApplication=" + applicationName + "," + 
		 "J2EEServer=" + serverName + "," +
		 "*");
	ObjectName objName = findObjectName(namePattern);
	if (objName != null) {
		setState(objName, state);
	}
    }

    /**
     * Set state for the given object name array 
     * corresponding to a module or application 
     */

    private void setState(ObjectName objName, int state) throws MBeanException {
        try {
            if (server.isRegistered(objName)) {
            	Integer stateValueObj = Integer.valueOf(state);
            	server.setAttribute(objName,
                	(new javax.management.Attribute("state", stateValueObj)));
            }
        } catch (javax.management.InstanceNotFoundException infe) {
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }


    /* Get state for the given name pattern */
    public Integer getState(String namePattern) throws MBeanException {
	ObjectName objName = findObjectName(namePattern);
	if (objName != null) {
            try {
		Integer intObj = (Integer) server.getAttribute(objName, "state");
		return intObj;
	    } catch (javax.management.InstanceNotFoundException infe) {
            } catch (Exception e) {
            	throw new MBeanException(e);
	    }
	}
	return null;
    }


    /* Register a given object name with the mBean server */
    private void register(Object object, ObjectName objectName)
        throws MBeanException {
        if (server == null) {
            initMBeanServer();
        }

        try {
            if (!server.isRegistered(objectName)) {
                server.registerMBean(object, objectName);
            }
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /* Unregister a given object name from the mBean server */
    public void unregister(ObjectName objectName)
        throws MBeanException {

        try {
            if (server.isRegistered(objectName)) {
                server.unregisterMBean(objectName);
            }
        } catch (javax.management.InstanceNotFoundException infe) {
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /**
     * Returns array of javax.management.ObjectName corresponding to the
     * given string array of object names
     */
    public ObjectName[] toObjectNameArray(String[] strings)
        throws MBeanException {
        // It could be possible that strings are null
        if (strings != null) {
            ObjectName[] objectNames = new ObjectName[strings.length];

            for (int i = 0; i < strings.length; i++) {
                try {
                    objectNames[i] = new ObjectName(strings[i]);
                } catch (MalformedObjectNameException mne) {
                    throw new MBeanException(mne);
                }
            }

            return objectNames;
        }

        // return an empty array
        return new ObjectName[] {  };
    }

    /* get module name for connector module */
    public String getModuleName(BundleDescriptor bd) 
        throws MBeanException {

	String moduleName = null;

	if (bd.getModuleDescriptor().isStandalone()) {
	    moduleName = bd.getApplication().getRegistrationName();
	} else {
	    moduleName = bd.getModuleDescriptor().getArchiveUri();
	}

	return moduleName;

    }


    /* get module location */
    private String getModuleLocation(BundleDescriptor bd, String j2eeType)
        throws MBeanException {

	String moduleName = null;
	String modLocation = null;
    String ddRoot = bd.getApplication().getGeneratedXMLDirectory();

	if (bd.getModuleDescriptor().isStandalone()) {

	    moduleName = bd.getApplication().getRegistrationName();

          	if (j2eeType.equals("AppClientModule")) {
	    		modLocation = ddRoot + File.separator +
				DescriptorConstants.APP_CLIENT_DD_ENTRY;
          	} else if (j2eeType.equals("EJBModule")) {
	    		modLocation = ddRoot + File.separator +
				DescriptorConstants.EJB_DD_ENTRY;
          	} else if (j2eeType.equals("WebModule")) {
	    		modLocation = ddRoot + File.separator +
				DescriptorConstants.WEB_DD_ENTRY;
          	} else if (j2eeType.equals("ResourceAdapterModule")) {
	    		modLocation = ddRoot + File.separator +
				DescriptorConstants.RAR_DD_ENTRY;
          	}

	} else {

	    moduleName = FileUtils.makeFriendlyFileName(
			 bd.getModuleDescriptor().getArchiveUri());

          	if (j2eeType.equals("AppClientModule")) {
	    		modLocation = ddRoot + File.separator + moduleName + File.separator +
				DescriptorConstants.APP_CLIENT_DD_ENTRY;
          	} else if (j2eeType.equals("EJBModule")) {
	    		modLocation = ddRoot + File.separator + moduleName + File.separator +
				DescriptorConstants.EJB_DD_ENTRY;
          	} else if (j2eeType.equals("WebModule")) {
	    		modLocation = ddRoot + File.separator + moduleName + File.separator +
				DescriptorConstants.WEB_DD_ENTRY;
          	} else if (j2eeType.equals("ResourceAdapterModule")) {
	    		modLocation = ddRoot + File.separator + moduleName + File.separator +
				DescriptorConstants.RAR_DD_ENTRY;
          	}
	}

	return modLocation;
    }


    /* get application name for connector module */
    public String getApplicationName(BundleDescriptor bd) {

        String applicationName = "null";

        if (bd.getModuleDescriptor().isStandalone()) {
                return applicationName;
        } else {
            if (bd.getApplication() != null) {
                applicationName = bd.getApplication().getRegistrationName();

                if (applicationName == null) {
                    applicationName = "null";
                }
            }
        }
        return applicationName;
    }

    // Methods for retrieving object names

    // Method to retrieve single object name

    public ObjectName findObjectName(String namePattern)
        	throws MBeanException {

	ObjectName [] objNameArr = findObjectNames(namePattern);

	if ((objNameArr != null) && (objNameArr.length > 0)) { 
		if (objNameArr.length > 1) {
	    		throw new MBeanException(new Exception(
			"Found more than one mBean matchin the given object name pattern"));
		}
		return objNameArr[0];
	}

	return null;
    }


    // Method to retrieve array of object names

    private ObjectName[] findObjectNames(String namePattern)
        	throws MBeanException {

	ObjectName [] objNameArr = null;
	ObjectName objNamePattern = null;

	//String pattern = J2EEDomainMdl.DOMAINNAME + ":" + keys + ",*";

	try {
		objNamePattern = new ObjectName(namePattern);
	} catch (javax.management.MalformedObjectNameException mfone) {
	    throw new MBeanException(mfone);
	}

	Set s = new HashSet();
	s = server.queryNames(objNamePattern, null);
	if (s != null) {
		objNameArr = (ObjectName[]) s.toArray( new ObjectName[s.size()]);
		return objNameArr;
	}

	return objNameArr;
    }

    // Method to read deployment descriptor xml 
    // and return it as String

    private String getStringForDDxml(String fileName) 
        	throws MBeanException {
	FileReader fr = null;
	    try {

            if (!(new File(fileName)).exists()) {
                _logger.log(Level.FINE, "Descriptor does not exist " + fileName);
                return null;
            }

		fr = new FileReader(fileName);
		StringWriter sr = new StringWriter();

		char[] buf = new char[1024];
		int len = 0;
		while (len != -1) {
			try {
				len = fr.read(buf, 0, buf.length);
			} catch (EOFException eof) {
				break;
			}
			if (len != -1) {
				sr.write(buf, 0, len);
			}
		}

		fr.close();
		sr.close();

		return sr.toString();

	} catch (FileNotFoundException fnfe) {
        	//System.out.println("FileNotFoundException ...");
	    	throw new MBeanException(fnfe);
	} catch (IOException ioe) {
        	//System.out.println("IOException ...");
	    	throw new MBeanException(ioe);
	} finally {
		if (fr != null) {
			try {
				fr.close();
			} catch (IOException ioe) {
			}
		}
	}
    }


    /* Return j2ee tpe for a given ejb */
    public String getJ2eeTypeForEjb(EjbDescriptor ejbDescriptor)
        throws MBeanException {


        String ejbType = ejbDescriptor.getType();
        String ejbSessionType = null;
        if (ejbType.equals("Session")) {
            ejbSessionType = ((EjbSessionDescriptor) ejbDescriptor).getSessionType();
	}

        String j2eeType = null;

        if (ejbType.equals("Entity")) {
            j2eeType = "EntityBean";
        } else if (ejbType.equals("Message-driven")) {
            j2eeType = "MessageDrivenBean";
        } else if (ejbType.equals("Session")) {
            if (ejbSessionType.equals("Stateless")) {
                j2eeType = "StatelessSessionBean";
            } else if (ejbSessionType.equals("Stateful")) {
                j2eeType = "StatefulSessionBean";
            }
        }

	return j2eeType;
    }


    // register domain status mBean
    private void registerDomainStatusMBean() {
	try {
		ObjectName on = DomainStatusHelper.getDomainStatusObjectName();

		server.registerMBean(new DomainStatus(), on);

		// set this server state to RUNNING
		Object[] params = new Object[2];
		params[0] = (Object) ApplicationServer.getServerContext().getInstanceName();
		params[1] = (Object) Integer.valueOf(StateManageable.STATE_RUNNING);
		String[] signature = {"java.lang.String", "java.lang.Integer"};
		server.invoke(on, "setstate", params, signature);

	} catch (MalformedObjectNameException mne) {
		throw new RuntimeException(mne);
	} catch (InstanceNotFoundException infe) {
		throw new RuntimeException(infe);
	} catch (InstanceAlreadyExistsException iae) {
		throw new RuntimeException(iae);
	} catch (MBeanRegistrationException mre) {
		throw new RuntimeException(mre);
	} catch (NotCompliantMBeanException ncmbe) {
		throw new RuntimeException(ncmbe);
	} catch (MBeanException mbe) {
		throw new RuntimeException(mbe);
	} catch (ReflectionException rfe) {
		throw new RuntimeException(rfe);
	}
    }

    // register das j2ee servers
    public void registerDasJ2EEServers() {
	// get the list of servers
	String [] allServers = getAllServerNamesInDomain();
	if ((allServers == null) || (allServers.length == 0)) return;
	// register for each server
	for (int i=0; i<allServers.length; i++) {
                registerDasJ2EEServer(allServers[i]);
        }
    }


    // get the list of all server names within this domain
    private String[] getAllServerNamesInDomain() {
        // get the list of all instances for this domain
        ConfigContext configContext =
                AdminService.getAdminService().getAdminContext().getAdminConfigContext();
        Server [] serverArr = null;
        try {
            serverArr = ServerHelper.getServersInDomain(configContext);
        } catch (ConfigException ce) {
            ce.printStackTrace();
            _logger.log(Level.WARNING, "admin.get_servers_in_domain_error", ce);
        }
        if ((serverArr == null) || (serverArr.length == 0)) {
                return null;
        }

        String [] strServerArr = new String[serverArr.length];

        for (int i=0; i<serverArr.length; i++) {
                strServerArr[i] = serverArr[i].getName();
        }

        return strServerArr;
    }

    // register DasJ2EE Server
    public void registerDasJ2EEServer(String serverName) {

	// vars
	DomainStatusHelper dsh = new DomainStatusHelper();

	try {
	    // check if the DomainStatusMBean is registered or not
	    if (! server.isRegistered(dsh.getDomainStatusObjectName())) {
		registerDomainStatusMBean();
	    }

	    // set this server state to STARTING if not already set
	    try {
		dsh.getstate(serverName);
            } catch (Exception e) {
	        Object[] params = new Object[2];
	        params[0] = (Object) serverName;
	        params[1] = (Object) Integer.valueOf(StateManageable.STATE_STARTING);
	        String[] signature = {"java.lang.String", "java.lang.Integer"};
	        server.invoke(dsh.getDomainStatusObjectName(), "setstate", params, signature);
	    }

	    // object name for j2ee server
	    /*
	    String strON = (
                ApplicationServer.getServerContext().getDefaultDomainName() + ":" +
		"j2eeType=J2EEServer," +
		"name=" + serverName +
		",category=dasJ2EE"
		);
	    ObjectName on = new ObjectName(strON);
	    */
	    ObjectName on = new ObjectName(getServerBaseON(true, serverName));

	    DASJ2EEServerImpl ds = new DASJ2EEServerImpl();
	    ObjectInstance oi = server.registerMBean(ds, on);

	    // register the notification listener
	    server.addNotificationListener(
		dsh.getDomainStatusObjectName(), 
		oi.getObjectName(),
		null, null);
	} catch(MalformedObjectNameException e) {
            e.printStackTrace();
            _logger.log(Level.WARNING, "admin.registerDasJ2EEServer exception", e);
	} catch(javax.management.InstanceAlreadyExistsException iae) {
	} catch(javax.management.InstanceNotFoundException infe) {
            infe.printStackTrace();
            _logger.log(Level.WARNING, "admin.registerDasJ2EEServer exception", infe);
	} catch(javax.management.MBeanRegistrationException mre) {
            mre.printStackTrace();
            _logger.log(Level.WARNING, "admin.registerDasJ2EEServer exception", mre);
	} catch(javax.management.NotCompliantMBeanException ncmbe) {
	    ncmbe.printStackTrace();
            _logger.log(Level.WARNING, "admin.registerDasJ2EEServer exception", ncmbe);
	} catch(javax.management.MBeanException mbe) {
	    mbe.printStackTrace();
            _logger.log(Level.WARNING, "admin.registerDasJ2EEServer exception", mbe);
	} catch(javax.management.ReflectionException rfe) {
	    rfe.printStackTrace();
            _logger.log(Level.WARNING, "admin.registerDasJ2EEServer exception", rfe);
	}
    }
    

    // unregister das j2ee server
    public void unregisterDasJ2EEServer(String serverName) {
	try {
	    /*
	    ObjectName onPattern = new ObjectName(
                ApplicationServer.getServerContext().getDefaultDomainName() + ":" +
		"j2eeType=J2EEServer," +
		"name=" + serverName +
		",group=jsr77" +
		",*"
		);
	    */
	    ObjectName onPattern = new ObjectName(
		getServerBaseON(true, serverName) + 
		",*"
		);
	    // query mBean server
	    Set s = new HashSet();
	    s = server.queryNames(onPattern, null);
	    if ((s != null) && (s.size() > 0)) {
		ObjectName [] objNameArr = 
		    (ObjectName[]) s.toArray( new ObjectName[s.size()]);
	        server.unregisterMBean(objNameArr[0]);
	    }
	} catch(MalformedObjectNameException e) {
            e.printStackTrace();
            _logger.log(Level.WARNING, "admin.unregisterDASJ2EEServer exception", e);
	} catch (javax.management.InstanceNotFoundException infe) {
            _logger.log(Level.FINE, "admin.unregisterDASJ2EEServer exception", infe);
	} catch (MBeanException mbe) {
            _logger.log(Level.FINE, "admin.unregisterDASJ2EEServer exception", mbe);
	} catch (Exception ex) {
	    ex.printStackTrace();
            _logger.log(Level.FINE, "admin.unregisterDASJ2EEServer exception", ex);
	}
    }

    /*
    public ObjectName getServerON(boolean amx, String serverName) {
	ObjectName serverON = null;
	try {
	    String strON = getServerBaseON(amx, serverName);
	    serverON = findObjectName(strON + ",*");
	} catch (Exception e) {
	    e.printStackTrace();
            _logger.log(Level.WARNING, "admin.getServerON exception", e);
	}
	return serverON;
    }
    */

    public String getServerBaseON(boolean amx, String serverName) {

            String domainName = null;
    
            // get domain name
            if (amx) {
		try {
                    ObjectName pattern = Util.newObjectNamePattern( 
                        "*", LoaderMBean.LOADER_NAME_PROPS );
                    Set names = server.queryNames(pattern, null);
                    assert( names.size() == 1 );
                    ObjectName loaderON = (ObjectName) SetUtil.getSingleton(names);
                    domainName = 
                        (String) server.getAttribute(loaderON, "AdministrativeDomainName");
		} catch (Exception e) {
		    throw new RuntimeException(e);
		}
		//domainName = "amx";
            } else {
		/*
                ObjectName pattern = Util.newObjectNamePattern( 
                    "*", "j2eeType=J2EEDomain,category=runtime");
                Set names = server.queryNames(pattern, null);
                assert( names.size() == 1 );
                ObjectName domainON = (ObjectName) SetUtil.getSingleton(names);
                domainName = (String) server.getAttribute(domainON, "name");
		*/
                domainName =
		ApplicationServer.getServerContext().getDefaultDomainName();
            }
            
            return getServerBaseON(domainName, serverName);
    
    }

  
    public void registerTransactionService() {
	TransactionServiceMdl ts = new TransactionServiceMdl();
        instantiateAndRegisterRuntimeMBean("TransactionService", ts);
   }

    private String getServerBaseON(String domainName, String serverName) {
            return( domainName + ":" + "j2eeType=J2EEServer,name=" + serverName);
    }

    public void registerAllJ2EEClusters() {
        try {
            final Cluster[] clusters = ClusterHelper.getClustersInDomain(
                getAdminConfigContext());
            if ((null == clusters) || (clusters.length == 0)) {
                return;
            }
            final ArrayList al = new ArrayList(clusters.length);
            for (int i = 0; i < clusters.length; i++) {
                al.add(clusters[i].getName());
            }
            final String[] sa = (String[])al.toArray(new String[0]);
            _registerJ2EEClusters(sa);
        } catch (ConfigException ce) {
            throw new RuntimeException(ce.getMessage());
        }
    }

    public void registerJ2EECluster(String clusterName) {
        try {
            if (!ClusterHelper.isACluster(getAdminConfigContext(), clusterName)) {
                throw new RuntimeException(clusterName + " is not a valid cluster.");
            }
        } catch (ConfigException ce) {
            throw new RuntimeException(ce.getMessage());
        }
        _registerJ2EEClusters(new String[] {clusterName});
    }

    public void unregisterJ2EECluster(String clusterName) {
        if ((null == clusterName) || "".equals(clusterName)) {
            throw new IllegalArgumentException();
        }
        ObjectName on = null;
        final MBeanServer mbs = getAdminMBeanServer();
        final ProxyFactory proxyFactory = ProxyFactory.getInstance(
            new MBeanServerConnectionSource(mbs));
        final Set proxies = proxyFactory.getDomainRoot().getQueryMgr().
            queryJ2EETypeSet(J2EECluster.J2EE_TYPE);
        final Iterator it = proxies.iterator();
        while (it.hasNext()) {
            J2EECluster cluster = (J2EECluster)it.next();
            if (cluster.getName().equals(clusterName)) {
                on = Util.getObjectName(cluster);
                break;
            }
        }
        try {
            mbs.unregisterMBean(on);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void _registerJ2EEClusters(String[] clusters) {
        final ConfigContext ctx = getAdminConfigContext();
        final MBeanServer   mbs = getAdminMBeanServer();
        final String        amxJMXDomain  = BootUtil.getInstance().getAMXJMXDomainName();
        final String        j2eeTypeProp    = AMX.J2EE_TYPE_KEY + '=' + J2EECluster.J2EE_TYPE;
        for (int i = 0; i < clusters.length; i++) {
            final String nameProp = AMX.NAME_KEY + '=' + clusters[i];
            final String props = Util.concatenateProps(j2eeTypeProp, nameProp);
            final ObjectName on = JMXUtil.newObjectName(amxJMXDomain, props);
            try {
                mbs.registerMBean(new J2EEClusterImpl(null), on);
                _logger.info(on + " is registered.");
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    private ConfigContext getAdminConfigContext() {
        return AdminService.getAdminService().getAdminContext().
            getAdminConfigContext();
    }

    private MBeanServer getAdminMBeanServer() {
        return AdminService.getAdminService().getAdminContext().
            getMBeanServer();
    }

    private String getJVMId() {
        final String serverName =
                ApplicationServer.getServerContext().getInstanceName();
        return serverName + System.currentTimeMillis();
    }

}
