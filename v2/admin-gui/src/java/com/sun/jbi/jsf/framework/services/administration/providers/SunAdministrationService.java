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

package com.sun.jbi.jsf.framework.services.administration.providers;

import com.sun.jbi.jsf.framework.common.GenericConstants;
import com.sun.jbi.jsf.framework.connectors.ServerConnector;
import com.sun.jbi.jsf.framework.common.ComponentInformationParser;
import com.sun.jbi.jsf.framework.common.ServiceAssemblyInformationParser;
import com.sun.jbi.jsf.framework.model.JBIComponentStatus;
import com.sun.jbi.jsf.framework.model.JBIComponentStatusDocument;
import com.sun.jbi.jsf.framework.model.JBIServiceAssemblyStatus;
import com.sun.jbi.jsf.framework.model.JBIServiceAssemblyStatusDocument;
import com.sun.jbi.jsf.framework.services.BaseServiceProvider;
import com.sun.jbi.jsf.framework.services.administration.AdministrationService;
import com.sun.jbi.ui.client.JBIAdminCommandsClientFactory;
import com.sun.jbi.ui.common.JBIAdminCommands;
import com.sun.jbi.ui.common.JBIRemoteException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.Set;
import javax.management.ObjectName;


/**
 * Sun Microsystems
 * @author ylee
 * @author graj
 */
public class SunAdministrationService extends BaseServiceProvider implements AdministrationService, Serializable {
    
    private static final String ADMIN_SERVICE_OBJECTNAME = "com.sun.jbi:ServiceName=JbiAdminUiService,ComponentType=System";
    private static String CAM_SERVER_MBEAN_NAME = "com.sun.cam:type=server,name=ServerMBean";
    
    protected List<JBIComponentStatus> bindingComponentList = new ArrayList<JBIComponentStatus>();
    protected List<JBIComponentStatus> serviceEngineList = new ArrayList<JBIComponentStatus>();
    protected List<JBIComponentStatus> sharedNamespacesList = new ArrayList<JBIComponentStatus>();
    protected List<JBIServiceAssemblyStatus> serviceAssemblyList = new ArrayList<JBIServiceAssemblyStatus>();
    
    protected String bindingComponents;
    protected String serviceEngines;
    protected String sharedLibraries;
    protected String serviceAssemblies;
    protected String sharedLibraryDependents;
    
    protected JBIComponentStatusDocument componentDoc = null;
    protected JBIServiceAssemblyStatusDocument serviceAssemblyStatusDoc = null;
   
    protected String mbeanName;
    protected ObjectName objectName = null;
    protected String targetName;
    
    protected Map<String,String> components = new HashMap<String,String>();

    private Logger logger = Logger.getLogger(SunAdministrationService.class.getName());


    /** Creates a new instance of AdministrationService
     * @param connector
     * @param targetName
     */
    public SunAdministrationService(ServerConnector connector, String targetName) {
        super(connector,targetName);
        mbeanName = ADMIN_SERVICE_OBJECTNAME;
        prepare(targetName);
    }
  
    public String getTargetName() {
        return this.targetName;
    }
    
    public void setTargetName(String target) {
        this.targetName = target;
    }
    
    
    public void prepare(String targetName) {
        this.targetName = targetName;
        initialize();
        constructDocumentObject();
        JBIComponentStatusDocument componentDoc = getJBIComponentStatusDocument();
        JBIServiceAssemblyStatusDocument assemblyDoc = getJBIServiceAssemblyStatusDocument();
        this.organize(componentDoc.getJbiComponentStatusList(), assemblyDoc.getJbiServiceAssemblyStatusList());
    }
    
    /**
     * @return Returns the bindingComponentList.
     */
    public List<JBIComponentStatus> getBindingComponentList() {
        return this.bindingComponentList;
    }

    /**
     * @return Returns the serviceAssemblyList.
     */
    public List<JBIServiceAssemblyStatus> getServiceAssemblyList() {
        return this.serviceAssemblyList;
    }

    /**
     * @return Returns the serviceEngineList.
     */
    public List<JBIComponentStatus> getServiceEngineList() {
        return this.serviceEngineList;
    }

    /**
     * @return Returns the sharedNamespacesList.
     */
    public List<JBIComponentStatus> getSharedNamespacesList() {
        return this.sharedNamespacesList;
    }    

    public String getComponentType(String name) {
        return components.get(name);
    }
    
    
    /**
     *
     */
    private void initialize() {
        JBIAdminCommands commands = null;
        Set set = null;
        String resultObject = null;
        try {
            this.serverConnection = serverConnector.getConnection();
            if (this.serverConnection != null) {
                commands = JBIAdminCommandsClientFactory.getInstance(this.serverConnection, false);
                if(commands != null) {
                    this.bindingComponents = commands.listBindingComponents(this.targetName);
                    this.serviceEngines = commands.listServiceEngines(this.targetName);
                    this.sharedLibraries = commands.listSharedLibraries(this.targetName);
                    this.serviceAssemblies = commands.listServiceAssemblies(this.targetName);
                }
            } else {
                logger.severe("Connection Failed");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    
    /**
     *
     * @param jbiComponentList
     * @param jbiServiceAssemblyList
     */
   private void organize(List<JBIComponentStatus> jbiComponentList, List<JBIServiceAssemblyStatus> jbiServiceAssemblyList) {
        Iterator iterator = jbiComponentList.iterator();
        JBIComponentStatus component = null;
        JBIServiceAssemblyStatus assembly = null;
        String type = null;
        while ((iterator != null) && (iterator.hasNext() == true)) {
            component = (JBIComponentStatus) iterator.next();
            if (component != null) {
                type = component.getType();
                if(type != null) {
                    if (type.equals(JBIComponentStatus.BINDING_TYPE)) {
                        this.bindingComponentList.add(component);
                        components.put(component.getName(),GenericConstants.BC_TYPE);
                    }
                    if (type.equals(JBIComponentStatus.ENGINE_TYPE)) {
                        this.serviceEngineList.add(component);
                        components.put(component.getName(),GenericConstants.SE_TYPE);
                    }
                    if (type.equals(JBIComponentStatus.NAMESPACE_TYPE)) {
                        this.sharedNamespacesList.add(component);
                    }
                }
            }
        }
        if(serviceAssemblyList != null) {
            this.serviceAssemblyList.addAll(jbiServiceAssemblyList);
        }
    }
   

    /**
     *
     * @param objectName
     * @param operationName
     * @param parameters
     * @return
     */
    public Object invoke(ObjectName objectName, String operationName, Object[] parameters) {
        String result = operationName+": ";
        String output = "";
        Throwable throwable = null;
        String[] signature = this.getSignatures(parameters);
        try {
            if(this.serverConnection != null) {
                output = (String)this.serverConnection.invoke(objectName,
                        operationName,
                        parameters,
                        signature);
            } 
            int e = 0, s = 0;
            final String pattern ="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
            if((e = output.indexOf(pattern, s)) >= 0) {
                result = output;
            } else {
                result += output;
            }
        } catch (Exception e) {
            result = this.formatException(e, operationName);
            e.printStackTrace();
        }
        return result;
    }

    /**
     *
     * @param str
     * @param pattern
     * @param replace
     * @return
     */
    protected String replace(String str, String pattern, String replace) {
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();

        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            result.append(replace);
            s = e+pattern.length();
        }
        result.append(str.substring(s));
        return result.toString();
    }

    /**
     *
     * @param e
     * @param operationName
     * @return
     */
    protected String formatException(Exception e, String operationName) {
        String result = "Failed execution of "+operationName+". ";
        Throwable throwable = e.getCause();
        if(throwable != null) {
            result += throwable.getMessage().trim();
        } else {
            result += e.getMessage();
        }
        return result;
    }


    protected JBIComponentStatusDocument getJBIComponentStatusDocument() {
        return this.componentDoc;
    }

    protected JBIServiceAssemblyStatusDocument getJBIServiceAssemblyStatusDocument() {
        return this.serviceAssemblyStatusDoc;
    }

    protected void constructDocumentObject() {
        componentDoc = new JBIComponentStatusDocument();
        serviceAssemblyStatusDoc = new JBIServiceAssemblyStatusDocument();
        JBIComponentStatusDocument component = null;
        JBIServiceAssemblyStatusDocument serviceAssembly = null;
        try {
            component = ComponentInformationParser.parse(this.bindingComponents);
            if((componentDoc != null) && (component != null)){
                componentDoc.addJbiComponentStatusList(component.getJbiComponentStatusList());
                //component.dump();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            serviceAssembly = ServiceAssemblyInformationParser.parse(this.serviceAssemblies);
            if((serviceAssemblyStatusDoc != null) && (serviceAssembly != null)){
                serviceAssemblyStatusDoc.addJbiServiceAssemblyStatusList(serviceAssembly.getJbiServiceAssemblyStatusList());
                //serviceAssembly.dump();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            component = ComponentInformationParser.parse(this.serviceEngines);
            if((componentDoc != null) && (component != null)){
                componentDoc.addJbiComponentStatusList(component.getJbiComponentStatusList());
                //component.dump();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            component = ComponentInformationParser.parse(this.sharedLibraries);
            if((componentDoc != null) && (component != null)){
                componentDoc.addJbiComponentStatusList(component.getJbiComponentStatusList());
                //component.dump();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void printOut() {
        if(this.bindingComponents != null) {
            System.out.println("/////////////////////////////////////////////");
            System.out.println("// -- Binding Components --");
            System.out.println("/////////////////////////////////////////////");
            System.out.println(this.bindingComponents);
        }
        if(this.serviceEngines != null) {
            System.out.println("/////////////////////////////////////////////");
            System.out.println("// -- Engine Components --");
            System.out.println("/////////////////////////////////////////////");
            System.out.println(this.serviceEngines);
        }
        if(this.sharedLibraries != null) {
            System.out.println("/////////////////////////////////////////////");
            System.out.println("// -- Shared Namespace Components --");
            System.out.println("/////////////////////////////////////////////");
            System.out.println(this.sharedLibraries);
        }
        if(this.serviceAssemblies != null) {
            System.out.println("/////////////////////////////////////////////");
            System.out.println("// -- Deployments --");
            System.out.println("/////////////////////////////////////////////");
            System.out.println(this.serviceAssemblies);
        }

    }

    /**
     * installs component ( service engine, binding component)
     * @return lifecycle object name string for service engine or binding component.
     * @param zipFilePath archive file in a zip format
     * @param paramProps Properties Object
     */
    public String installComponent(String zipFilePath, Properties paramProps) {
        String result = "";
        JBIAdminCommands commands = null;
        if (this.serverConnection != null) {
            try {
                commands = JBIAdminCommandsClientFactory.getInstance(this.serverConnection, false);
                if(commands != null) {
                    commands.installComponent(zipFilePath, paramProps, targetName);
                }   
            } catch (JBIRemoteException ex) {
                ex.printStackTrace();
            }   
        }
        
        /*
        Object[] params = new Object[2];
        params[0] = zipFilePath;
        params[1] = paramProps;
        result = (String)this.invoke(objectName,
                             GenericConstants.INSTALL_COMPONENT_OPERATION_NAME,
                             params);
         */
        return result;
    }

    /**
     * installs component ( service engine, binding component)
     * @return lifecycle object name string for service engine or binding component.
     * @param zipFilePath archive file in a zip format
     */
    public String installComponent(String zipFilePath) {
        String result = "";
        JBIAdminCommands commands = null;
        if (this.serverConnection != null) {
            try {
                commands = JBIAdminCommandsClientFactory.getInstance(this.serverConnection, false);
                if(commands != null) {
                    commands.installComponent(zipFilePath, targetName);
                }   
            } catch (JBIRemoteException ex) {
                ex.printStackTrace();
            }   
        }
        /*        
        Object[] params = new Object[2];
        params[0] = zipFilePath;
        params[1] = new Properties();
        result = this.installComponent(zipFilePath, new Properties());
        */
        return result;
    }

    /**
     * uninstalls component ( service engine, binding component)
     * @param componentName name of the component
     * @return result string
     */
    public String uninstallComponent(String componentName) {
        String result = "";
        JBIAdminCommands commands = null;
        if (this.serverConnection != null) {
            try {
                commands = JBIAdminCommandsClientFactory.getInstance(this.serverConnection, false);
                if(commands != null) {
                    commands.uninstallComponent(componentName, targetName);
                }   
            } catch (JBIRemoteException ex) {
                ex.printStackTrace();
            }   
        }        
        /*
        String[] params = new String[1];
        params[0] = componentName;
        result = (String)this.invoke(objectName,
                 GenericConstants.UNINSTALL_COMPONENT_OPERATION_NAME,
                 params);
         */
        return result;
    }

    /**
     * installs shared namespace
     * @return shared library name object name string for service engine or binding component.
     * @param zipFilePath archive file in a zip format
     */
    public String installSharedLibrary(String zipFilePath) {
        String result = "";
        JBIAdminCommands commands = null;
        if (this.serverConnection != null) {
            try {
                commands = JBIAdminCommandsClientFactory.getInstance(this.serverConnection, false);
                if(commands != null) {
                    commands.installSharedLibrary(zipFilePath, targetName);
                }   
            } catch (JBIRemoteException ex) {
                ex.printStackTrace();
            }   
        }         
        
        
        /*
        String[] params = new String[1];
        params[0] = zipFilePath;
        result = (String)this.invoke(objectName,
                             GenericConstants.INSTALL_SHARED_LIBRARY_OPERATION_NAME,
                             params);
         */
        return result;
    }

    /**
     * uninstalls shared library
     * @param sharedLibraryName name of the shared library
     * @return result string
     */
    public String uninstallSharedLibrary(String sharedLibraryName) {
        String result = "";
        
        JBIAdminCommands commands = null;
        if (this.serverConnection != null) {
            try {
                commands = JBIAdminCommandsClientFactory.getInstance(this.serverConnection, false);
                if(commands != null) {
                    commands.uninstallSharedLibrary(sharedLibraryName, targetName);
                }   
            } catch (JBIRemoteException ex) {
                ex.printStackTrace();
            }   
        }          
        /*
        String[] params = new String[1];
        params[0] = sharedLibraryName;
        result = (String)this.invoke(objectName,
                             GenericConstants.UNINSTALL_SHARED_LIBRARY_OPERATION_NAME,
                             params);
         */
        return result;
    }

    /**
     * starts component ( service engine, binding component)
     * @param componentName name of the component
     * @return result string
     */
    public String startComponent(String componentName) {
        String result = "";
        
        JBIAdminCommands commands = null;
        if (this.serverConnection != null) {
            try {
                commands = JBIAdminCommandsClientFactory.getInstance(this.serverConnection, false);
                if(commands != null) {
                    commands.startComponent(componentName, targetName);
                }   
            } catch (JBIRemoteException ex) {
                ex.printStackTrace();
            }   
        }          
        
        
        /*
        String[] params = new String[1];
        params[0] = componentName;
        result = (String)this.invoke(objectName,
                             GenericConstants.START_COMPONENT_OPERATION_NAME,
                             params);
         */
        return result;
    }

    /**
     * starts component ( service engine, binding component)
     * @param componentName name of the component
     * @return result string
     */
    public String stopComponent(String componentName) {
        String result = "";
        JBIAdminCommands commands = null;
        if (this.serverConnection != null) {
            try {
                commands = JBIAdminCommandsClientFactory.getInstance(this.serverConnection, false);
                if(commands != null) {
                    commands.stopComponent(componentName, targetName);
                }   
            } catch (JBIRemoteException ex) {
                ex.printStackTrace();
            }   
        }          
        
        /*
        String[] params = new String[1];
        params[0] = componentName;
        result = (String)this.invoke(objectName,
                             GenericConstants.STOP_COMPONENT_OPERATION_NAME,
                             params);
         */
        return result;
    }

    /**
     * starts component ( service engine, binding component)
     * @param componentName name of the component
     * @return result string
     */
    public String shutdownComponent(String componentName) {
        String result = "";
        JBIAdminCommands commands = null;
        if (this.serverConnection != null) {
            try {
                commands = JBIAdminCommandsClientFactory.getInstance(this.serverConnection, false);
                if(commands != null) {
                    commands.shutdownComponent(componentName, targetName);
                }   
            } catch (JBIRemoteException ex) {
                ex.printStackTrace();
            }   
        }          
        
        /*        
        String[] params = new String[1];
        params[0] = componentName;
        result = (String)this.invoke(objectName,
                             GenericConstants.SHUTDOWN_COMPONENT_OPERATION_NAME,
                             params);
         */
        return result;
    }

    /**
     * deploys service assembly
     * @return deployment status result as a management message xml
     * @param zipFilePath fie path
       */
    public String deployServiceAssembly(String zipFilePath) {
        String result = "";
        
        JBIAdminCommands commands = null;
        if (this.serverConnection != null) {
            try {
                commands = JBIAdminCommandsClientFactory.getInstance(this.serverConnection, false);
                if(commands != null) {
                    commands.deployServiceAssembly(zipFilePath, targetName);
                }   
            } catch (JBIRemoteException ex) {
                ex.printStackTrace();
            }   
        }          
        
        /*          
        String[] params = new String[1];
        params[0] = zipFilePath;
        result = (String)this.invoke(objectName,
                             GenericConstants.DEPLOY_SERVICE_ASSEMBLY_OPERATION_NAME,
                             params);
         */
        return result;
    }


    /**
     * start service assembly
     * @param serviceAssemblyName name of the service assembly
     * @return result string
     */
    public String startServiceAssembly(String serviceAssemblyName) {
        String result = "";
        JBIAdminCommands commands = null;
        if (this.serverConnection != null) {
            try {
                commands = JBIAdminCommandsClientFactory.getInstance(this.serverConnection, false);
                if(commands != null) {
                    commands.startServiceAssembly(serviceAssemblyName, targetName);
                }   
            } catch (JBIRemoteException ex) {
                ex.printStackTrace();
            }   
        }          
        
        /*        
        String[] params = new String[1];
        params[0] = serviceAssemblyName;
        result = (String)this.invoke(objectName,
                             GenericConstants.START_SERVICE_ASSEMBLY_OPERATION_NAME,
                             params);
         */
        return result;
    }

    /**
     * stop service assembly
     * @param serviceAssemblyName name of the service assembly
     * @return result string
     */
    public String stopServiceAssembly(String serviceAssemblyName) {
        String result = "";
        JBIAdminCommands commands = null;
        if (this.serverConnection != null) {
            try {
                commands = JBIAdminCommandsClientFactory.getInstance(this.serverConnection, false);
                if(commands != null) {
                    commands.stopServiceAssembly(serviceAssemblyName, targetName);
                }   
            } catch (JBIRemoteException ex) {
                ex.printStackTrace();
            }   
        }          
        
        /*        
        String[] params = new String[1];
        params[0] = serviceAssemblyName;
        result = (String)this.invoke(objectName,
                             GenericConstants.STOP_SERVICE_ASSEMBLY_OPERATION_NAME,
                             params);
         */
        return result;
    }

    /**
     * shutdown service assembly
     * @param serviceAssemblyName name of the service assembly
     * @return result string
     */
    public String shutdownServiceAssembly(String serviceAssemblyName) {
        String result = "";
        
        JBIAdminCommands commands = null;
        if (this.serverConnection != null) {
            try {
                commands = JBIAdminCommandsClientFactory.getInstance(this.serverConnection, false);
                if(commands != null) {
                    commands.shutdownServiceAssembly(serviceAssemblyName, targetName);
                }   
            } catch (JBIRemoteException ex) {
                ex.printStackTrace();
            }   
        }          
        
        /*         
        String[] params = new String[1];
        params[0] = serviceAssemblyName;
        result = (String)this.invoke(objectName,
                             GenericConstants.SHUTDOWN_SERVICE_ASSEMBLY_OPERATION_NAME,
                             params);
         */
        return result;
    }

    /**
     * undeploys service assembly
     * @param serviceAssemblyName name of the service assembly
     * @return result string
     */
    public String undeployServiceAssembly(String serviceAssemblyName) {
        String result = "";
        JBIAdminCommands commands = null;
        if (this.serverConnection != null) {
            try {
                commands = JBIAdminCommandsClientFactory.getInstance(this.serverConnection, false);
                if(commands != null) {
                    commands.undeployServiceAssembly(serviceAssemblyName, targetName);
                }   
            } catch (JBIRemoteException ex) {
                ex.printStackTrace();
            }   
        }          
        
        /*         
        String[] params = new String[1];
        params[0] = serviceAssemblyName;
        result = (String)this.invoke(objectName,
                             GenericConstants.UNDEPLOY_SERVICE_ASSEMBLY_OPERATION_NAME,
                             params);
         */
        return result;
    }



    /**
     * return component info xml text that has only binding component infos which satisfies
     * the options passed to the method.
     * @param state to return all the binding component that are in the specified state.
     * JBIComponentInfo.STARTED, STOPPED, INSTALLED or null for ANY state
     * @param sharedLibraryName to return all the binding component that have a dependency on the specified shared library.
     * Could be null for not filtering the binding component for this dependency.
     * @param serviceAssemblyName to return all the binding component that have the specified service assembly deployed on them.
     * Could be null for not filtering the binding component for this dependency.
     * @return xml string contain the list of binding component component infos
     */
    public String listBindingComponents(
        String state, String sharedLibraryName, String serviceAssemblyName )
        {
            String result = "";
            return result;
        }

    /**
     * returns a list of Binding Component and Service Engine infos in xml format, that are
     * dependent upon a specified Shared Library
     * @param sharedLibraryName the shared library name
     * @return xml string containing the list of componentInfos
     */
    public String listSharedLibraryDependents(String sharedLibraryName) {
        String result = "";
        return result;
    }



    /**
     * returns the list of service asssembly infos in a xml format that have the
     * service unit deployed on the specified component.
     * @param componentName to list all the service assemblies that have some deployments on this component.
     * @return xml string contain the list of service assembly infos
     */
    public String listServiceAssemblies(String componentName) {
        String result = "";
        String[] params = new String[1];
        params[0] = componentName;
        result = (String)this.invoke(objectName,
                             GenericConstants.LIST_SERVICE_ASSEMBLIES_OPERATION_NAME,
                             params);
        return result;
    }

    /**
     * return component info xml text that has only service engine infos.
     * @return the component info xml text.
     */
    public String listServiceEngines() {
        String result = "";
        return result;
    }


    /**
     * Returns the jbi.xml Deployment Descriptor for a Service Assembly.
     * 
     * @param serviceAssemblyName - the name of the Service Assembly
     * @return the jbi.xml deployment descriptor of the archive
     */
    public String getServiceAssemblyDeploymentDescriptor(
            String serviceAssemblyName)  {
         String result = "";
        JBIAdminCommands commands = null;
        if (this.serverConnection != null) {
            try {
                commands = JBIAdminCommandsClientFactory.getInstance(this.serverConnection, false);
                if(commands != null) {
                  result = commands.getServiceAssemblyDeploymentDescriptor(serviceAssemblyName);
                }   
            } catch (JBIRemoteException ex) {
                ex.printStackTrace();
            }   
        }          
        
        return result;
    }

    /**
     * Returns the jbi.xml Deployment Descriptor for a Service Unit.
     * 
     * @param serviceAssemblyName - the name of the Service Assembly
     * @param serviceUnitName -  the name of the Service Unit
     * @return the jbi.xml deployment descriptor of the archive
     */
    public String getServiceUnitDeploymentDescriptor(
            String serviceAssemblyName, String serviceUnitName) {
        String result = "";
        JBIAdminCommands commands = null;
        if (this.serverConnection != null) {
            try {
                commands = JBIAdminCommandsClientFactory.getInstance(this.serverConnection, false);
                if(commands != null) {
                  result =  commands.getServiceUnitDeploymentDescriptor(serviceAssemblyName,serviceUnitName);
                }   
            } catch (JBIRemoteException ex) {
                ex.printStackTrace();
            }   
        }          
        
        return result;
    }        	



}
