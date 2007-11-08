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
 * ApplicationsConfigMBean.java
 *
 * Created on April 29, 2003, 5:45 PM
 * @author  sandhyae
 * <BR> <I>$Source: /cvs/glassfish/admin/mbeans/src/java/com/sun/enterprise/admin/mbeans/ApplicationsConfigMBean.java,v $
 *
 */

package com.sun.enterprise.admin.mbeans;

import com.sun.enterprise.admin.server.core.CustomMBeanException;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Enumeration;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanInfo;

import com.sun.enterprise.instance.InstanceEnvironment;
//config
import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigChange;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.EjbModule;
import com.sun.enterprise.config.serverbeans.ConnectorModule;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.PropertyResolver;
import com.sun.enterprise.config.serverbeans.LifecycleModule;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.admin.common.exception.DeploymentException;
import com.sun.enterprise.admin.common.exception.ServerInstanceException;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;
import com.sun.enterprise.admin.event.BaseDeployEvent;
import com.sun.enterprise.admin.event.ElementChangeHelper;
import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.event.AdminEventMulticaster;
import com.sun.enterprise.admin.event.AdminEventResult;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.util.HostAndPort;
import com.sun.enterprise.admin.common.MBeanServerFactory;
//phasing
import com.sun.enterprise.deployment.phasing.DeploymentService;
import com.sun.enterprise.deployment.phasing.DeploymentServiceUtils;
import com.sun.enterprise.deployment.phasing.DeploymentTarget;
import com.sun.enterprise.deployment.phasing.DeploymentTargetFactory;
//dbe
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.backend.IASDeploymentException;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.backend.DeploymentCommand;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.util.i18n.StringManager;
// sub-component-status
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.InboundResourceAdapter;
import com.sun.enterprise.deployment.OutboundResourceAdapter;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.io.DescriptorList;
import com.sun.enterprise.deployment.backend.DeploymentUtils;
import com.sun.enterprise.instance.InstanceFactory;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.instance.EjbModulesManager;
import com.sun.enterprise.instance.WebModulesManager;
import com.sun.enterprise.instance.ConnectorModulesManager;
import com.sun.enterprise.instance.AppclientModulesManager;
import java.util.Iterator;
import com.sun.enterprise.Switch;
import com.sun.enterprise.ManagementObjectManager;
import javax.enterprise.deploy.shared.ModuleType;
import com.sun.enterprise.deployment.util.XModuleType;
import com.sun.enterprise.connectors.ConnectorConstants;

import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.util.StringUtils;
import java.util.List;
import java.util.ListIterator;

import com.sun.enterprise.server.Constants;
import com.sun.enterprise.admin.mbeanapi.IApplicationsConfigMBean;
import com.sun.enterprise.admin.mbeans.custom.BasicCustomMBeanConfigQueries;
import com.sun.enterprise.admin.mbeans.custom.BasicCustomMBeanOperations;
import com.sun.enterprise.admin.mbeans.custom.CustomMBeanConfigQueries;
import com.sun.enterprise.admin.mbeans.custom.CustomMBeanOperationsMBean;

import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.ServerHelper;

import com.sun.enterprise.util.io.FileUtils;

/**
 * The MBean that represents the deployment management interface for iAS SE. In
 * other words it represents the <strong> deployment interface </strong> .
 * <p>
 * The MBeanServer will have one instance of this MBean. This MBean is lazy loaded.
 * <p>
 * ObjectName of this MBean is ias:type=Deployment, name=<instance-name>
 * @author  Sandhya E
 */
public class ApplicationsConfigMBean extends BaseConfigMBean
    implements IApplicationsConfigMBean, CustomMBeanOperationsMBean, CustomMBeanConfigQueries
{
    
    /** default target name when there is no target in the request object **/
    public static final String DEFAULT_TARGET = "domain";
    
    /** FIXME **/
    public static final Logger sLogger = Logger.getLogger(AdminConstants.kLoggerName);
    
    // Begin EE: 4946914 - Cluster deployment support

    /** Deployment Service object used to invoke operations like deployment, undeployment **/
    protected DeploymentService deployService = null;
    
    /** instance name **/
    protected String mInstanceName   = null;
    
    /** context object **/
    protected ConfigContext m_configContext = null;
    protected CustomMBeanOperationsMBean cmo    = null;
    protected CustomMBeanConfigQueries cmcq     = null;
    
    protected static final Object[] emptyParams = new Object[]{};
    protected static final String[] emptySignature = new String[]{};
    
    
    protected static final StringManager localStrings =
    StringManager.getManager( ApplicationsConfigMBean.class );
     
    
    protected static final String TYPE_APPLICATION = "application";
    protected static final String TYPE_EJB = "ejb";
    protected static final String TYPE_WEB = "web";
    protected static final String TYPE_CONNECTOR = "connector";
    protected static final String TYPE_APPCLIENT = "appclient";
    
    protected static final String JSR88_TYPE_APPLICATION = "ear";
    protected static final String JSR88_TYPE_EJB = "ejb";
    protected static final String JSR88_TYPE_WEB = "war";
    protected static final String JSR88_TYPE_CONNECTOR = "rar";
    protected static final String JSR88_TYPE_APPCLIENT = "car";

    protected static final String DOMAIN_TARGET = "domain";
    
    protected static final DeployableObjectType[] deployableObjectTypes = 
                                                new DeployableObjectType[] {
                                                    DeployableObjectType.APP,
                                                    DeployableObjectType.EJB,
                                                    DeployableObjectType.WEB,
                                                    DeployableObjectType.CONN,
                                                    DeployableObjectType.CAR,
                                                    DeployableObjectType.LCM, 
                                                    DeployableObjectType.CMB 
                                                };

    // End EE: 4946914 - Cluster deployment support
    
    private static final TargetType[] VALID_LIST_TYPES = new TargetType[] {
        TargetType.CLUSTER, TargetType.SERVER, TargetType.DAS, TargetType.DOMAIN};
        

    /**
     * Creates a new instance of ApplicationsConfigMBean
     * @param instanceName name of this instance
     * @param configContext context object
     */
    public ApplicationsConfigMBean(String instanceName, ConfigContext configContext) throws MBeanConfigException {
        super();
        mInstanceName = instanceName;
        m_configContext = configContext;
        deployService = DeploymentService.getDeploymentService(configContext);
        initCustomMBeanHandlers();
    }
    
    public ApplicationsConfigMBean() throws MBeanConfigException  {
        super();
        initCustomMBeanHandlers();
    }   

    protected void initCustomMBeanHandlers() {
        cmo     = new BasicCustomMBeanOperations();
        cmcq    = new BasicCustomMBeanConfigQueries();
    }
    
    private DeploymentTarget getAndValidateDeploymentTarget(String targetName,
        String appName, boolean isRegistered) throws IASDeploymentException
    {
        return getAndValidateDeploymentTarget(targetName, appName, isRegistered, false);
    }
    
    private DeploymentTarget getAndValidateDeploymentTarget(String targetName,
        String appName, boolean isRegistered, boolean isDeleting) throws IASDeploymentException
    {
        try {
            final DeploymentTarget target = getTargetFactory().getTarget(
                getConfigContext(), getDomainName(), targetName);

            if (isRegistered) {
                if (targetName == null) {
                    //If the targetName passed in was null, we need to set it to its default value.
                    targetName = target.getTarget().getName();
                }
                if (target.getTarget().getType() == TargetType.DOMAIN && isDeleting) {
                    // If the target is the domain and the object is being deleted, then we must
                    // ensure that the there are no references to it.
                    if (ApplicationHelper.isApplicationReferenced(getConfigContext(), appName)) {
                        throw new IASDeploymentException(localStrings.getString("applicationIsReferenced", 
                            appName, ApplicationHelper.getApplicationReferenceesAsString(
                                getConfigContext(), appName)));
                    }
                }
                else if (target.getTarget().getType() == TargetType.SERVER ||
                    target.getTarget().getType() == TargetType.DAS) {  
                    // If the application exists, we must ensure that if a standalone server instance is
                    // the target, that it must be the only entity referring to the application and
                    // indeed it must have a reference to the application
                    if (!ServerHelper.serverReferencesApplication(getConfigContext(), 
                        targetName, appName) && isDeleting) {
                        throw new IASDeploymentException(localStrings.getString("serverApplicationRefDoesNotExist", 
                            targetName, appName));
                    } else if (!ApplicationHelper.isApplicationReferencedByServerOnly(getConfigContext(), 
                            appName, targetName)) {
                        throw new IASDeploymentException(localStrings.getString("applicationHasMultipleRefs", 
                            targetName, appName, ApplicationHelper.getApplicationReferenceesAsString(
                                getConfigContext(), appName)));
                    }
                } else if (target.getTarget().getType() == TargetType.CLUSTER) {
                    // If the application exists, we must ensure that if a cluster is
                    // the target, that it must be the only entity referring to the application and
                    // indeed it must have a reference to the application
                     if (!ClusterHelper.clusterReferencesApplication(getConfigContext(), 
                        targetName, appName) && isDeleting) {
                        throw new IASDeploymentException(localStrings.getString("clusterApplicationRefDoesNotExist", 
                            targetName, appName));
                    } else if (!ApplicationHelper.isApplicationReferencedByClusterOnly(getConfigContext(), 
                            appName, targetName)) {
                        throw new IASDeploymentException(localStrings.getString("applicationHasMultipleRefs", 
                            targetName, appName, ApplicationHelper.getApplicationReferenceesAsString(
                                getConfigContext(), appName)));                     
                     }
                }
            }
            return target;
        } catch (IASDeploymentException ex) {
            throw (ex);
        } catch (Exception ex) {
            throw new IASDeploymentException(ex);
        }
    }
    
    /**
     * Deploys a component to the given array of targets which can be
     * domains, clusters, or standalone instances. Since there are restrictions
     * around how clusters and standalone instance share deployments, the 
     * component bits are deployed only to the first target in the list and then
     * application references are created for the rest of the targets in the
     * array specified.
     *
     * @return DeploymentStatus
     * @param props The properties associated with a deployment.
     * @param targets An array of available targets for deployment.
     * @throws DeploymentException when an error occurs during association or
     * deployment of the bits
     */
    public DeploymentStatus deploy(Properties props, String [] targets) 
                throws DeploymentException{
        DeploymentStatus status = null;
        props.setProperty(DeploymentProperties.TARGET, targets[0]);
        status = deploy(props);
        for(int i = 1; i < targets.length; i++) {
            try {
                associateApplication(props, targets[i]);
            } catch(MBeanConfigException m) {
                DeploymentException e = new DeploymentException(m.getMessage());
                e.initCause(m);
                throw e;
            }
        }
        return status;
    }
    
    /**
     * Associates a deployed application with an available target for
     * deployment. Once an application has been deployed to a cluster, domain,
     * or server instance, this method will create an application reference
     * for that target.
     *
     * @param props Properties of the application to be referenced. This 
     * includes setting the following properties:
     * <p> 
     * DeploymentProperties.NAME
     * DeploymentProperties.ENABLE
     * DeploymentProperties.VIRTUAL_SERVERS
     * </p>
     * @param targetName The name of the target to associate the application.
     */
    public void associateApplication(Properties props, String targetName) 
            throws MBeanConfigException {
        try {
            final DeploymentTarget target = getTargetFactory().getTarget(
                    getConfigContext(), getDomainName(), targetName);
            boolean enabled = Boolean.valueOf(props.getProperty(DeploymentProperties.ENABLE)).booleanValue();
            target.addAppReference(
                    props.getProperty(DeploymentProperties.NAME), 
                    enabled,
                    props.getProperty(DeploymentProperties.VIRTUAL_SERVERS));
        } catch(Exception e) {
            MBeanConfigException m = new MBeanConfigException(e.getMessage());
            m.initCause(e);
            throw m;
        }
    }   
    
    /**
     * Undeploys a component to the given array of targets which can be
     * domains, clusters, or standalone instances. Since there are restrictions
     * around how clusters and standalone instances share deployments, 
     * the application references are removed for all the targets except for 
     * the first one in the array. After this, the component bits are undeployed
     * from the first target in the array.
     * 
     * @return DeploymentStatus
     * @param props The properties associated with an undeployment.
     * @param targets An array of targets for undeploying the application.
     * @throws DeploymentException when an error occurs during removal of the
     * application references or physical undeployment of the bits.
     */
    public DeploymentStatus undeploy(Properties props, String [] targets) 
                throws DeploymentException{
        DeploymentStatus status = null;
        props.setProperty(DeploymentProperties.TARGET, targets[0]);
        for(int i = 1; i < targets.length; i++) {
            try {
                disassociateApplication(props, targets[i]);
            } catch (MBeanConfigException m) {
                DeploymentException e = new DeploymentException(m.getMessage());
                e.initCause(m);
                throw e;
            }
        }
        status = undeploy(props);
        return status;
    }
    
    /**
     * Removes the application reference from the specified target on which
     * that application reference exists.
     *
     * @param props The properties of the application. The following property
     * must be set for the application to be disassociated:
     * <p>
     * DeploymentProperties.NAME
     * </p>
     * @param targetName The name of the target that the application must be
     * removed from.
     */
    public void disassociateApplication(Properties props, String targetName)
            throws MBeanConfigException {
        try {
            final DeploymentTarget target = getTargetFactory().getTarget(
                    getConfigContext(), getDomainName(), targetName);
            target.removeAppReference(
                    props.getProperty(DeploymentProperties.NAME));
        } catch(Exception e) {
            MBeanConfigException m = new MBeanConfigException(e.getMessage());
            m.initCause(e);
            throw m;
        }
    }   

    /**
     * Return application/module type
     * @param String stand-alone application or stand-alone module name
     * @param Integer Integer representation of application or module type 
     *                as jsr88 module type
     *                pl. refer to javax.enterprise.deploy.shared.ModuleType
     *                Note that we cannot return ModuleType directory to
     *                the client since it is not Serializable
     */
     public Integer getModuleType(String standAloneModuleName)
        throws ServerInstanceException {

        sLogger.log(Level.FINE, "getModuleType - begin" +
                " standAloneModuleName = " + standAloneModuleName);

        // initialization
        J2EEModule j2eeModule = new J2EEModule(standAloneModuleName);

        // get j2ee module type
        ModuleType moduleType = j2eeModule.getModuleType();
        if (moduleType == null) {
            return null;
        }

        return (Integer.valueOf(moduleType.getValue()));
    }

    
    /**
     * Generic API to invoke deployment
     * @see DeploymentProperties.java
     */
    public com.sun.enterprise.deployment.backend.DeploymentStatus deploy(Properties props)
                            throws DeploymentException 
    {
        DeploymentProperties dProps = new DeploymentProperties(props);
        String archiveName = dProps.getArchiveName();
        String name = dProps.getName(archiveName);
        DeployableObjectType type = getTypeFromFile(name, archiveName);
        if(archiveName == null)
            throw new IllegalArgumentException("archiveName not specified");
       
        sLogger.log(Level.FINE, "mbean.begin_deploy", archiveName);                
               
        java.io.File deployFile = new java.io.File(archiveName);
        int actionCode = BaseDeployEvent.APPLICATION_DEPLOYED;  
        try {
            InstanceEnvironment env = new InstanceEnvironment(getInstanceName());
            DeploymentRequest req = new DeploymentRequest(
                                        env,
                                        type,
                                        DeploymentCommand.DEPLOY);
            
            
            /* If app exists & forceDeploy is false it's an error.
             * It should be detected by the deployment backend.
             */
            boolean isRegistered = false ; //isRegistered(name, type);
            ObjectName componentON = 
                getRegisteredComponentObjectName(name, type);

            if(componentON != null)
                isRegistered = true;

            if(isRegistered)
                validate(componentON, REDEPLOY_ACTION);

            if(type.isAPP())
            {
                actionCode = BaseDeployEvent.APPLICATION_DEPLOYED;
            }
            else
            {
                actionCode = BaseDeployEvent.MODULE_DEPLOYED;
            }
			
            req.setFileSource(deployFile);
            req.setName(name);
            req.setForced(dProps.getForce());
            // for redeployment
            req.setCascade(true);
            if(type.isWEB()) {
                req.setDefaultContextRoot(dProps.getDefaultContextRoot(
                    archiveName));
                req.setContextRoot(dProps.getContextRoot());
            }
            req.setVerifying(dProps.getVerify());
            req.setPrecompileJSP(dProps.getPrecompileJSP());
            req.setGenerateRMIStubs(dProps.getGenerateRMIStubs());
            req.setAvailabilityEnabled(dProps.getAvailabilityEnabled());
            req.setStartOnDeploy(dProps.getEnable());
            req.setDescription(dProps.getDescription());
            req.setLibraries(dProps.getLibraries());
            req.setJavaWebStartEnabled(dProps.getJavaWebStartEnabled());
            req.setExternallyManagedPath(dProps.getExternallyManaged());
            req.setActionCode(actionCode);
            DeploymentServiceUtils.setResourceOptionsInRequest(req, dProps);
            final DeploymentTarget target = getAndValidateDeploymentTarget(dProps.getTarget(), 
                name, isRegistered);
            req.setTarget(target);
            
            Properties optionalAttributes = new Properties();
            //optionalAttributes.put(ServerTags.ENABLED, String.valueOf(bEnabled));
            String virtualServers = dProps.getVirtualServers();
            if(virtualServers!=null)
                optionalAttributes.put(ServerTags.VIRTUAL_SERVERS, dProps.getVirtualServers());
            req.setOptionalAttributes(optionalAttributes);
            if(props == null)
                props = new Properties();
            else
                props = dProps.prune();
            req.addOptionalArguments(props);
            
            setHostAndPort(req);
            return getDeploymentService().deploy(req);
        }
        catch(Exception e) {
            if (actionCode == BaseDeployEvent.APPLICATION_DEPLOYED) {
                sLogger.log(Level.WARNING, "mbean.deploy_failed", e);
            }
            else {
                sLogger.log(Level.WARNING, "mbean.redeploy_failed", e);
            }
            DeploymentStatus ds = new DeploymentStatus();
            ds.setStageException(e);
            ds.setStageStatus(DeploymentStatus.FAILURE);
            ds.setStageStatusMessage(e.getMessage());
            ds.setStageDescription("Deployment");
            return ds;
        }
        finally {
            deleteFile(archiveName);
        }
    }
        /////////////////////////////

    
    /**
     * undeploys the specified app/module
     * @param name name by which this module is registered
     * @param props Properties to be used for undeploying
     * @return true if app/module is unloaded successfully after undeployment,
     * false otherwise. If the instance is not running the method returns
     * <strong>true</strong>
     * @throws DeploymentException if undeployment fails
     * @see DeploymentProperties.java
     */
    public com.sun.enterprise.deployment.backend.DeploymentStatus undeploy(Properties props)
                                    throws DeploymentException {
        String name = props.getProperty(DeploymentProperties.NAME);
        if(name == null)                    
            throw new DeploymentException("name not specified in undeploy");
        
        sLogger.log(Level.FINE, "mbean.begin_undeploy", name);
        try {
                       
            DeployableObjectType objectType = getRegisteredType(name);

            ObjectName componentON =
                getRegisteredComponentObjectName(name,objectType);
            validate(componentON, UNDEPLOY_ACTION);

            //BUG 4739891 begin
            if (objectType.isWEB()) {
                checkWebModuleReferences(name);
            }
            //BUG 4739891 end
            DeploymentRequest req = new DeploymentRequest(
                                         new InstanceEnvironment(getInstanceName()),
                                         objectType, DeploymentCommand.UNDEPLOY);
            DeploymentProperties dProps = new DeploymentProperties(props);
            req.setName(name);
            req.setCascade(dProps.getCascade());
            req.setReload(dProps.getReload());
            req.setForced(false);
            req.setExternallyManagedPath(dProps.getExternallyManaged());
            DeploymentServiceUtils.setResourceOptionsInRequest(req, dProps);
            
            props = dProps.prune();

            req.addOptionalArguments(props);
            
            if(objectType.isAPP())
                req.setActionCode(BaseDeployEvent.APPLICATION_UNDEPLOYED);
            else                
                req.setActionCode(BaseDeployEvent.MODULE_UNDEPLOYED);
            
            final DeploymentTarget target = getAndValidateDeploymentTarget(dProps.getTarget(), 
                name, true, true);
            req.setTarget(target);
                                                                                
            String dependentResource = DeploymentServiceUtils.checkConnectorDependentResourcesForUndeploy(req);
            if (dependentResource != null) {
                String msg = localStrings.getString("admin.mbeans.acmb.dependentresexist", new Object[] {dependentResource});
                throw new IASDeploymentException( msg );
            }

            return getDeploymentService().undeploy(req);
        }
        catch(Exception e) {
            sLogger.log(Level.WARNING, "mbean.undeploy_failed", e);
            DeploymentStatus ds = new DeploymentStatus();
            ds.setStageException(e);
            ds.setStageStatus(DeploymentStatus.FAILURE);
            ds.setStageStatusMessage(e.getMessage());
            ds.setStageDescription("Undeployment");
            return ds;            
        }
    }

    public DeploymentStatus createApplicationReference(String targetName,
        boolean enabled, String virtualServers, String referenceName) {
        try {
            sLogger.log(Level.FINE, "mbean.create_app_reference",
                referenceName);
            return DeploymentService.getDeploymentService().associate(targetName, enabled, virtualServers, referenceName);
        } catch (Exception e) {
            sLogger.log(Level.WARNING, "mbean.create_app_reference_failed",
                e);
            DeploymentStatus ds = new DeploymentStatus();
            ds.setStageException(e);
            ds.setStageStatus(DeploymentStatus.FAILURE);
            ds.setStageStatusMessage(e.getMessage());
            ds.setStageDescription("Association");
            return ds;
        }
    }

    public DeploymentStatus deleteApplicationReference(String targetName,
        String referenceName) {
        try {
            sLogger.log(Level.FINE, "mbean.delete_app_reference",
                referenceName);
            return DeploymentService.getDeploymentService(
                ).disassociate(targetName, referenceName);
        } catch (Exception e) {
            sLogger.log(Level.WARNING, "mbean.delete_app_reference_failed",
                e);
            DeploymentStatus ds = new DeploymentStatus();
            ds.setStageException(e);
            ds.setStageStatus(DeploymentStatus.FAILURE);
            ds.setStageStatusMessage(e.getMessage());
            ds.setStageDescription("Disassociation");
            return ds;
        }
    }

   public DeploymentStatus createApplicationReference(String targetName,
        String referenceName, Map options) {
        try {
            sLogger.log(Level.FINE, "mbean.create_app_reference",
                referenceName);          
            return DeploymentService.getDeploymentService().associate(targetName, 
                referenceName, options);
        } catch (Exception e) {
            sLogger.log(Level.WARNING, "mbean.create_app_reference_failed",
                e);
            DeploymentStatus ds = new DeploymentStatus();
            ds.setStageException(e);
            ds.setStageStatus(DeploymentStatus.FAILURE);
            ds.setStageStatusMessage(e.getMessage());
            ds.setStageDescription("Association");
            return ds;
        }   
    }           

    public Map createApplicationReferenceAndReturnStatusAsMap(
        String targetName, String referenceName, Map options) {
        DeploymentStatus oldStatus = createApplicationReference(targetName,
            referenceName, options);
        return oldStatus.asMap();
    }

    public DeploymentStatus deleteApplicationReference(String targetName,
        String referenceName, Map options) {
        try {
            sLogger.log(Level.FINE, "mbean.delete_app_reference",
                referenceName);
            return DeploymentService.getDeploymentService(
                ).disassociate(targetName, referenceName, options);
        } catch (Exception e) {
            sLogger.log(Level.WARNING, "mbean.delete_app_reference_failed",
                e);
            DeploymentStatus ds = new DeploymentStatus();
            ds.setStageException(e);
            ds.setStageStatus(DeploymentStatus.FAILURE);
            ds.setStageStatusMessage(e.getMessage());
            ds.setStageDescription("Disassociation");
            return ds;
        }
    }

    public Map deleteApplicationReferenceAndReturnStatusAsMap(
        String targetName, String referenceName, Map options) {
        DeploymentStatus oldStatus = deleteApplicationReference(targetName,
            referenceName, options);
        return oldStatus.asMap();
    }

    public DeploymentStatus start(String moduleID, String targetName, 
        Map options) {
        try {
            sLogger.log(Level.FINE, "mbean.start", moduleID);
            return DeploymentService.getDeploymentService().start(moduleID, 
                targetName, options); 
        } catch (Exception e) {
            sLogger.log(Level.WARNING, "mbean.start_failed", e);
            DeploymentStatus ds = new DeploymentStatus();
            ds.setStageException(e);
            ds.setStageStatus(DeploymentStatus.FAILURE);
            ds.setStageStatusMessage(e.getMessage());
            ds.setStageDescription("Start");
            return ds;
        }
    }

    public Map startAndReturnStatusAsMap(
        String moduleID, String targetName, Map options) {
        DeploymentStatus oldStatus = start(moduleID, targetName, options);
        return oldStatus.asMap();
    }

    public DeploymentStatus stop(String moduleID, String targetName, 
        Map options) {
        try {
            sLogger.log(Level.FINE, "mbean.stop", moduleID);
            DeploymentStatus status = DeploymentService.getDeploymentService().stop(moduleID, 
                targetName, options);
            //deployment already added all required config changes to its events
            // so, we remove them to avoid extra deployment events
            getConfigContext().flush();
            getConfigContext().resetConfigChangeList();
            return status;
        } catch (Exception e) {
            sLogger.log(Level.WARNING, "mbean.stop_failed", e);
            DeploymentStatus ds = new DeploymentStatus();
            ds.setStageException(e);
            ds.setStageStatus(DeploymentStatus.FAILURE);
            ds.setStageStatusMessage(e.getMessage());
            ds.setStageDescription("Stop");
            return ds;
        }
    }

    public Map stopAndReturnStatusAsMap(
        String moduleID, String targetName, Map options) {
        DeploymentStatus oldStatus = stop(moduleID, targetName, options);
        return oldStatus.asMap();
    }

    /**
     * Deploys the specified application
     * @param Properties properties object containing the following properties
     *      archiveName the complete path of the archive to be deployed.
     *      appName the name with which the application needs to be registered.
     *      forceDeploy True indicates that if this application has alreay
     *                   been deployed it will be overwritten. False indicates
     *                   that an existing application should not be overwritten
     *                   and should result in exception. If there exists no deployed
     *                   application and the forceDeploy is false, it is considered
     *                   as normal deployment.
     *      bVerify true if application has to be verified
     *      bPreCompileJSP true if jsps in application are to be pre compiled
     *      cmp properties
     *      targetName target to which deployment should be done
     * @throws DeploymentException if the deployment is not successful
     * @return true if application is loaded successfully after deployment,
     *              false otherwise. If the instance is not running the method returns
     *              <strong>true</strong>
     */
    public boolean deployJ2EEApplication(Properties props)
                                         throws DeploymentException {
                                             
        DeploymentProperties dProps = new DeploymentProperties(props);
        String archiveName = dProps.getArchiveName();
        if(archiveName == null)
            throw new IllegalArgumentException("archiveName not specified");
        sLogger.log(Level.FINE, "mbean.begin_deploy", archiveName);
        java.io.File deployFile = new java.io.File(archiveName);
        int actionCode = BaseDeployEvent.APPLICATION_DEPLOYED;
        try {


            InstanceEnvironment env = new InstanceEnvironment(getInstanceName());
            DeploymentRequest req = new DeploymentRequest(
                                        env,
                                        DeployableObjectType.APP,
                                        DeploymentCommand.DEPLOY);
            
            
            String appName = dProps.getName(archiveName);
            /* If app exists & forceDeploy is false it's an error.
             * It should be detected by the deployment backend.
             */
            boolean isAppExists = isRegistered(appName, DeployableObjectType.APP);
            actionCode = (isAppExists && dProps.getForce()) ?
                             BaseDeployEvent.APPLICATION_REDEPLOYED : BaseDeployEvent.APPLICATION_DEPLOYED;
            req.setFileSource(deployFile);
            req.setName(appName);
            req.setForced(dProps.getForce());
            req.setVerifying(dProps.getVerify());
            req.setPrecompileJSP(dProps.getPrecompileJSP());
            req.setStartOnDeploy(dProps.getEnable());
            req.setActionCode(actionCode);
            final DeploymentTarget target = getAndValidateDeploymentTarget(dProps.getTarget(), 
                appName, isAppExists);
            req.setTarget(target);
            
            Properties optionalAttributes = new Properties();
            //optionalAttributes.put(ServerTags.ENABLED, String.valueOf(bEnabled));
            String virtualServers = dProps.getVirtualServers();
            if(virtualServers!=null)
                optionalAttributes.put(ServerTags.VIRTUAL_SERVERS, dProps.getVirtualServers());
            req.setOptionalAttributes(optionalAttributes);
            if(props == null)
                props = new Properties();
            else
                props = dProps.prune();
            req.addOptionalArguments(props);
            
            setHostAndPort(req);
            
            getDeploymentService().deploy(req);
        }
        catch(Exception e) {
            if (actionCode == BaseDeployEvent.APPLICATION_DEPLOYED) {
                sLogger.log(Level.WARNING, "mbean.deploy_failed", e);
            }
            else {
                sLogger.log(Level.WARNING, "mbean.redeploy_failed", e);
            }
            DeploymentException newE = new DeploymentException(e.getMessage());
            newE.initCause(e);
            throw newE;
        }
        finally {
            deleteFile(archiveName);
        }
        return true;
    }
    
    
    /**
     * Deploys the specified standalone ejb module
     * @param Properties properties object containing following properties
     *     archiveName the complete path of the archive to be deployed.
     *     moduleName the name with which the ejb module needs to be registered.
     *     forceDeploy True indicates that if this module has alreay
     *                   been deployed it will be overwritten. False indicates
     *                   that an existing module should not be overwritten
     *                   and should result in exception. If there exists no deployed
     *                   application and the forceDeploy is false, it is considered
     *                   as normal deployment.
     *     bVerify true if this archive has to be verified
     *     targetName target to which deployment should be done 
     *     cmp properties
     * @throws DeploymentException if the deployment is not successful
     * @return true if module is loaded successfully after deployment,
     *              false otherwise. If the instance is not running the method returns
     *              <strong>true</strong>
     */
    public boolean deployEJBJarModule(Properties props) 
                                      throws DeploymentException {
         
        DeploymentProperties dProps = new DeploymentProperties(props);
        String filePath = dProps.getArchiveName();
        if (filePath == null) {
            throw new IllegalArgumentException();
        }
        boolean loadStatus = true;
        sLogger.log(Level.FINE, "deploymentservice.begin_deploy", filePath);
        java.io.File deployFile = new java.io.File(filePath);
        int actionCode = BaseDeployEvent.MODULE_DEPLOYED;
        try {

            
            InstanceEnvironment env = new InstanceEnvironment(getInstanceName());
            //Prepare Request
            DeploymentRequest req = new DeploymentRequest(
                                        env,
                                        DeployableObjectType.EJB,
                                        DeploymentCommand.DEPLOY);

            String moduleName = dProps.getName(filePath);
            boolean isModuleExists = isRegistered(moduleName,
                                         DeployableObjectType.EJB);
            actionCode = (isModuleExists && dProps.getForce()) ?
                BaseDeployEvent.MODULE_REDEPLOYED : BaseDeployEvent.MODULE_DEPLOYED;
            req.setFileSource(deployFile);
            req.setName(moduleName);            
            //req.setShared(isShared);
            req.setForced(dProps.getForce());
            req.setVerifying(dProps.getVerify());
            req.setStartOnDeploy(dProps.getEnable());
            req.setActionCode(actionCode);
            final DeploymentTarget target = getAndValidateDeploymentTarget(dProps.getTarget(), 
                moduleName, isModuleExists);
            req.setTarget(target);
            if(props == null)
                props = new Properties();
            else
                props = dProps.prune();
            req.addOptionalArguments(props);            
            //Request Ready
            
            getDeploymentService().deploy(req);
        }
        catch(Exception e) {
            if (actionCode == BaseDeployEvent.MODULE_DEPLOYED) {
                sLogger.log(Level.WARNING, "mbean.deploy_failed", e);
            }
            else {
                sLogger.log(Level.WARNING, "mbean.redeploy_failed", e);
            }
            throw new DeploymentException(e.getMessage());
        }
        finally {
            deleteFile(filePath);
        }
        return loadStatus;
    }
    
    /**
     * Deploys the specified standalone web module
     * @param Properties properties object containing following properties
     *     archiveName the complete path of the archive to be deployed.
     *     webAppName the name with which the web module needs to be registered.
     *     contextRoot context root for this web module
     *     bEnabled webApp enabled if true
     *     virtualServers
     *     forceDeploy True indicates that if this application has alreay
     *                   been deployed it will be overwritten. False indicates
     *                   that an existing application should not be overwritten
     *                   and should result in exception. If there exists no deployed
     *                   application and the forceDeploy is false, it is considered
     *                   as normal deployment.
     *     bVerify true if this archive has to be verified
     *     bPreCompileJSP true if jsps in application are to be pre compiled
     *     targetName target to which deployment should be done
     * @throws DeploymentException if the deployment is not successful
     * @return true if module is loaded successfully after deployment,
     *              false otherwise. If the instance is not running the method returns
     *              <strong>true</strong>
     */
    public boolean deployWarModule(Properties props)
                                   throws DeploymentException {
        
        DeploymentProperties dProps = new DeploymentProperties(props);
        String filePath = dProps.getArchiveName();
        if (filePath == null) {
            throw new IllegalArgumentException();
        }
        
        boolean loadStatus = true;
        sLogger.log(Level.FINE, "mbean.begin_deploy", filePath);
        java.io.File deployFile = new java.io.File(filePath);
        int actionCode = BaseDeployEvent.MODULE_DEPLOYED;
        try {

            InstanceEnvironment env = new InstanceEnvironment(getInstanceName());
            
            //Prepare Request
            DeploymentRequest req = new DeploymentRequest(
                                        env,
                                        DeployableObjectType.WEB,
                                        DeploymentCommand.DEPLOY);
            
            String webAppName = dProps.getName(filePath);
            boolean isModuleExists = isRegistered(webAppName,
                                         DeployableObjectType.WEB);
            actionCode = (isModuleExists && dProps.getForce()) ?
                              BaseDeployEvent.MODULE_REDEPLOYED : BaseDeployEvent.MODULE_DEPLOYED;
                              
            req.setFileSource(deployFile);
            req.setName(webAppName);
            req.setContextRoot(dProps.getContextRoot());
            req.setForced(dProps.getForce());
            req.setVerifying(dProps.getVerify());
            req.setPrecompileJSP(dProps.getPrecompileJSP());
            req.setStartOnDeploy(dProps.getEnable());            
            req.setActionCode(actionCode);
            final DeploymentTarget target = getAndValidateDeploymentTarget(dProps.getTarget(), 
                webAppName, isModuleExists);
            req.setTarget(target);
            //req.setShared(false);
            // get the webserver hostname and ports
            setHostAndPort(req);
            Properties optionalAttributes = new Properties();
            //optionalAttributes.put(ServerTags.ENABLED, String.valueOf(bEnabled));
            String virtualServers = dProps.getVirtualServers();
            if(virtualServers!=null)
                optionalAttributes.put(ServerTags.VIRTUAL_SERVERS, virtualServers);
            req.setOptionalAttributes(optionalAttributes);
                       
            getDeploymentService().deploy(req);
        }
        catch(Exception e) {
            if (actionCode == BaseDeployEvent.MODULE_DEPLOYED) {
                sLogger.log(Level.WARNING, "mbean.deploy_failed", e);
            }
            else {
                sLogger.log(Level.WARNING, "mbean.redeploy_failed", e);
            }
            throw new DeploymentException(e.getMessage());
        }
        finally {
            deleteFile(filePath);
        }
        return loadStatus;
    }
    
    /**
     * Deploys the specified standalone ejb module
     * @param Properties properties object containing following properties
     *     archiveName the complete path of the archive to be deployed.
     *     moduleName the name with which the connector module needs to be registered.
     *     forceDeploy True indicates that if this module has alreay
     *                   been deployed it will be overwritten. False indicates
     *                   that an existing module should not be overwritten
     *                   and should result in exception. If there exists no deployed
     *                   module and the forceDeploy is false, it is considered
     *                   as normal deployment.
     *     bVerify true if this archive has to be verified
     *     targetName target to which deployment should be done
     * @throws DeploymentException if the deployment is not successful
     * @return true if module is loaded successfully after deployment,
     *              false otherwise. If the instance is not running the method returns
     *              <strong>true</strong>
     */
    public boolean deployConnectorModule(Properties props)
                                         throws DeploymentException {
        
        DeploymentProperties dProps = new DeploymentProperties(props);
        String filePath = dProps.getArchiveName();
        if (filePath == null) {
            throw new IllegalArgumentException();
        }
        
        boolean loadStatus = true;
        sLogger.log(Level.FINE, "mbean.begin_deploy", filePath);
        java.io.File deployFile = new java.io.File(filePath);
        int actionCode = BaseDeployEvent.MODULE_DEPLOYED;
        try {

            InstanceEnvironment env = new InstanceEnvironment(getInstanceName());
            
            DeploymentRequest req = new DeploymentRequest(
                                        env,
                                        DeployableObjectType.CONN,
                                        DeploymentCommand.DEPLOY);
            
            String moduleName = dProps.getName(filePath);
            boolean isModuleExists = isRegistered(moduleName,
                                         DeployableObjectType.CONN);
            actionCode = (isModuleExists && dProps.getForce()) ?
                BaseDeployEvent.MODULE_REDEPLOYED : BaseDeployEvent.MODULE_DEPLOYED;
            
            req.setFileSource(deployFile);
            req.setName(moduleName);
            req.setForced(dProps.getForce());
            req.setVerifying(dProps.getVerify());
            req.setActionCode(actionCode);
            final DeploymentTarget target = getAndValidateDeploymentTarget(dProps.getTarget(), 
                moduleName, isModuleExists);
            req.setTarget(target);
            req.setStartOnDeploy(dProps.getEnable());            
            //req.setShared(false);
            
            setDeployDirOwner(req, env);
            getDeploymentService().deploy(req);            
        }
        catch(Exception e) {
            if (actionCode == BaseDeployEvent.MODULE_DEPLOYED) {
                sLogger.log(Level.WARNING, "mbean.deploy_failed", e);
            }
            else {
                sLogger.log(Level.WARNING, "mbean.redeploy_failed", e);
            }
            throw new DeploymentException(e.getMessage());
        }
        finally {
            deleteFile(filePath);
        }
        return loadStatus;
    }

    /**
     * Returns all deployable targets in this domain. All groups
     * and all servers(servers that are not part of any groups)
     * @return array of targets to which deployment can be performed
     *               [ an array of 0 if there are no targets]
     * @throws MBeanConfigException
     */

    public String[] getTargets() throws MBeanConfigException{
        try{
          
            ArrayList targetList = new ArrayList();
            MBeanServer mbs = MBeanServerFactory.getMBeanServer();
            //FIXME handle clusters
            //add groups 
            /*
            ObjectName groupsON = new ObjectName(getDomainName()+":type=clusters,category=config");
            try{
            ObjectName[] groupONArr = (ObjectName[])mbs.invoke(groupsON, "getCluster", emptyParams, emptySignature);
            for(int i = 0; i < groupONArr.length; i++){
                targetList.add(mbs.getAttribute(groupONArr[i], "name"));              
            }
            }catch(Exception e){
            }
            */
            //FIXME add only standalone servers
            //add servers
            
            try{
                ObjectName serversON = new ObjectName(getDomainName()+":type=servers,category=config");
            ObjectName[] serverONArr = (ObjectName[])mbs.invoke(serversON, "getServer", emptyParams, emptySignature);
            for(int i = 0; i<serverONArr.length; i++){
                targetList.add(mbs.getAttribute(serverONArr[i], "name"));              
            }   
            }catch(Exception e)
            {
                targetList.add("server");
            }
            
            return (String[])targetList.toArray(new String[]{});
          
        }catch(Throwable t) {
            throw new MBeanConfigException(t.getMessage());
        }
    }
    
    /**
     * Returns all deployed modules of specified type on default target
     * @param moduleType type of the module(app|ejb|web|connector)
     * @return array of deployed modules of type moduleType
     * @throws MBeanConfigException
     */
    public String[] getAvailableModules(String moduleType) throws MBeanConfigException {
        String target = DEFAULT_TARGET;
        return getAvailableModules(moduleType, new String[]{target});
    }
    
    /**
     * Returns all deployed modules of specified type and on specified target
     * @param moduleType type of the module(war/jar/ear/rar)
     * @param target target server
     * @return array of deployed modules of type moduleType and on target
     */
    public String[] getAvailableModules(String moduleType, String[] targetList)
                                        throws MBeanConfigException {
        return getAvailableModules(moduleType, targetList, false);
    }

    /**
     * Returns all user deployed modules of specified type and on specified
     * target. The result from this method excludes all system modules.
     * @param moduleType type of the module(war/jar/ear/rar)
     * @param targetList list of target servers
     * @return array of deployed modules of type moduleType and on target
     */
    public String[] getAvailableUserModules(String moduleType,
            String[] targetList)
            throws MBeanConfigException {
        return getAvailableModules(moduleType, targetList, true);
    }

    /**
     * Get deployed modules of specified type on specified list of targets.
     * @param moduleType type of the module (war/jar/ear/rar/connector), see
     *     the TYPE_* constants.
     * @param targetList list of target servers
     * @param excludeSystemApps whether to exclude system apps or not
     * @return array of deployed modules of specified type on specified
     *     list of targets.
     */
    private String[] getAvailableModules(String moduleType, String[] targetList,
            boolean excludeSystemApps)
            throws MBeanConfigException {
        try{
            DeployableObjectType type = getDeployableObjectType(moduleType);
            ArrayList listOfModules = new ArrayList();
            for(int i = 0 ; i< targetList.length;i++)
            {
                listOfModules.addAll(getModules(targetList[i], type, null, excludeSystemApps));
            }
            return (String[])listOfModules.toArray(new String[listOfModules.size()]);
        }catch(Exception e){
            throw new MBeanConfigException(e.getMessage());
        }
    }
    
    /**
     * Returns an array of all currently running modules of specified type
     * and on specified target
     * @return array of running modules of type moduleType and on target
     * @param moduleType type of the module(war/jar/ear/rar)
     * @param target target server
     */
    public String[] getRunningModules(String moduleType, String[] targetList)
    throws MBeanConfigException {
        return getRunningModules(moduleType, targetList, false);
    }

    /**
     * Returns an array of all currently running user modules of specified
     * type and on specified target
     * @return array of running modules of type moduleType and on target
     * @param moduleType type of the module(war/jar/ear/rar)
     * @param target target server
     */
    public String[] getRunningUserModules(String moduleType, String[] targetList)
    throws MBeanConfigException {
        return getRunningModules(moduleType, targetList, true);
    }

    /**
     * Get running modules of specified type on specified list of targets.
     * @param moduleType type of the module (war/jar/ear/rar/connector), see
     *     the TYPE_* constants.
     * @param targetList list of target servers
     * @param excludeSystemApps whether to exclude system apps or not
     * @return array of running modules of specified type on specified
     *     list of targets.
     */
    private String[] getRunningModules(String moduleType, String[] targetList,
            boolean excludeSystemApps)
    throws MBeanConfigException {
        try{
            DeployableObjectType type = getDeployableObjectType(moduleType);
            ArrayList listOfModules= new ArrayList();
            for (int i = 0; i < targetList.length; i++) {
                listOfModules.addAll(getModules(targetList[i], type,
                                                Boolean.valueOf(true),
                                                excludeSystemApps));
            }
            return (String[])listOfModules.toArray(new String[listOfModules.size()]);
        }catch(Exception e){
            throw new MBeanConfigException(e.getMessage());
        }
    }
    
    /**
     * Returns an array of all non running modules of specified type
     * and on target
     * @param moduleType type of the module(war/jar/ear/rar)
     * @param target target server
     * @return array of non running modules[ an array of length 0 if no running modules are present]
     */
    
    public String[] getNonRunningModules(String moduleType, String[] targetList)
    throws MBeanConfigException {
        return getNonRunningModules(moduleType, targetList, false);
    }

    /**
     * Returns an array of all non running user modules of specified type
     * and on target
     * @param moduleType type of the module(war/jar/ear/rar)
     * @param target target server
     * @return array of non running modules[ an array of length 0 if no running modules are present]
     */
    public String[] getNonRunningUserModules(String moduleType, String[] targetList)
    throws MBeanConfigException {
        return getNonRunningModules(moduleType, targetList, false);
    }

    /**
     * Get not running modules of specified type on specified list of targets.
     * @param moduleType type of the module (war/jar/ear/rar/connector), see
     *     the TYPE_* constants.
     * @param targetList list of target servers
     * @param excludeSystemApps whether to exclude system apps or not
     * @return array of not running modules of specified type on specified
     *     list of targets.
     */
    private String[] getNonRunningModules(String moduleType,
            String[] targetList, boolean excludeSystemApps)
    throws MBeanConfigException {
        try{
            DeployableObjectType type = getDeployableObjectType(moduleType);
            ArrayList listOfModules= new ArrayList();
            for (int i = 0; i < targetList.length; i++) {
                listOfModules.addAll(getModules(targetList[i], type,
                                                Boolean.valueOf(false),
                                                excludeSystemApps));
            }
            return (String[])listOfModules.toArray(new String[listOfModules.size()]);
        }catch(Exception e){
            throw new MBeanConfigException(e.getMessage());
        }
    }
    
    /**
     * Returns the list of deployed J2EEApplications. These are the
     * applications that are deployed to domain, and are registered
     * under <applications>
     * @return list of deployed j2ee applications
     * @throws ServerInstanceException
     */
    public String[] getDeployedJ2EEApplications() throws ServerInstanceException {
        String[] apps = new String[0];
        try {
            sLogger.log(Level.FINE, "mbean.list_components");
            Applications appsConfigBean =
            (Applications) ConfigBeansFactory.getConfigBeanByXPath(
            getConfigContext(), ServerXPathHelper.XPATH_APPLICATIONS);
            J2eeApplication[] j2eeApps = appsConfigBean.getJ2eeApplication();
            if (j2eeApps != null) {
                apps = new String[j2eeApps.length];
                for(int i=0; i<j2eeApps.length; i++) {
                    apps[i] = j2eeApps[i].getName();
                }
            }
        }
        catch (Exception e) {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
        return ( apps );
    }
    
    /**
     * Returns an array of standalone ejb module names that are deployed to
     * this server instance.
     * @return an array of deployed ejb module names.  Returns an array of 0
     * length if none are deployed.
     * @throws ServerinstanceException
     */
    public String[] getDeployedEJBModules() throws ServerInstanceException {
        String[] ejbModules = new String[0];
        try {
            sLogger.log(Level.FINE, "mbean.list_components");
            Applications appsConfigBean =
            (Applications) ConfigBeansFactory.getConfigBeanByXPath(
            getConfigContext(), ServerXPathHelper.XPATH_APPLICATIONS);
            EjbModule[] modules = appsConfigBean.getEjbModule();
            if (modules != null) {
                ejbModules = new String[modules.length];
                for(int i=0; i<modules.length; i++) {
                    ejbModules[i] = modules[i].getName();
                }
            }
        }
        catch (Exception e) {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
        return ejbModules;
    }
    
    /**
     * Returns an array of standalone war module names that are deployed to
     * this server instance.
     * @return an array of deployed web module names. Returns an array of 0
     * length if none are deployed.
     * @throws ServerinstanceException
     */
    public String[] getDeployedWebModules() throws ServerInstanceException {
        String[] webModules = new String[0];
        try {
            sLogger.log(Level.FINE, "mbean.list_components");
            Applications appsConfigBean =
                (Applications) ConfigBeansFactory.getConfigBeanByXPath(
                     getConfigContext(), ServerXPathHelper.XPATH_APPLICATIONS);
            WebModule[] modules = appsConfigBean.getWebModule();
            if (modules != null) {
                webModules = new String[modules.length];
                for(int i=0; i<modules.length; i++) {
                    webModules[i] = modules[i].getName();
                }
            }
        }
        catch (Exception e) {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
        return webModules;
    }
    
    /**
     * Returns an array of deployed connectors.
     * @return an array of deployed connectors. Returns an array of 0 length
     * if none are deployed.
     * @throws ServerinstanceException
     */
    public String[] getDeployedConnectors() throws ServerInstanceException {
        String[] connectors = new String[0];
        try {
            sLogger.log(Level.FINE, "mbean.list_components");
            Applications appsConfigBean =
            (Applications) ConfigBeansFactory.getConfigBeanByXPath(
            getConfigContext(), ServerXPathHelper.XPATH_APPLICATIONS);
            ConnectorModule[] connectorConfigBeans =
            appsConfigBean.getConnectorModule();
            if (connectorConfigBeans != null) {
                connectors = new String[connectorConfigBeans.length];
                for(int i = 0; i < connectors.length; i++) {
                    connectors[i] = connectorConfigBeans[i].getName();
                }
            }
        }
        catch (Exception e) {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
        return connectors;
    }
    
    
    /**
     * Returns an array of all the deployed components.
     * @return an array of deployed components. Returns an array of 0 length
     * if none are deployed.
     * @throws ServerinstanceException
     */
    public ObjectName[] getAllDeployedComponents() throws ServerInstanceException {
                
        int totalCount=0;
        ObjectName[] allComponents = null;
        
        try {
            sLogger.log(Level.FINE, "mbean.list_components");
            ObjectName[] components= null;
            for(int count = 0; count < deployableObjectTypes.length; count++) {
                components = getAllRegisteredComponentsOfType(deployableObjectTypes[count]);
                if(components != null && components.length > 0) {
                    totalCount=totalCount+components.length;
                }
            }
            allComponents = new ObjectName[totalCount];
            
            if(totalCount > 0) {
                totalCount=0;
                for(int count = 0; count < deployableObjectTypes.length; count++) {
                    components = getAllRegisteredComponentsOfType(deployableObjectTypes[count]);
                    if(components != null && components.length > 0) {
                        
                        for(int count1=0; count1<components.length; count1++,totalCount++) {
                            //add component to main array
                            allComponents[totalCount] = components[count1];
                            
                        }
                    }
                } 
            }
            
        }
        catch (Exception e) {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
        return (allComponents);
    }
    
    
    /**
     * Returns the list of deployed J2EEApplications ObjectName. These are the
     * applications that are deployed to domain, and are registered
     * under <applications>
     * @return list of deployed j2ee applications
     * @throws ServerInstanceException
     */
    public ObjectName[] getAllDeployedJ2EEApplications() throws ServerInstanceException {
        return getAllRegisteredComponentsOfType(deployableObjectTypes[0]);
    }
    
    /**
     * Returns an array of standalone ejb module ObjectName that are deployed to
     * this server instance.
     * @return an array of deployed ejb module ObjectName.  Returns an array of 0
     * length if none are deployed.
     * @throws ServerinstanceException
     */
    public ObjectName[] getAllDeployedEJBModules() throws ServerInstanceException {
        return getAllRegisteredComponentsOfType(deployableObjectTypes[1]);
    }
    
    /**
     * Returns an array of standalone war module ObjectName that are deployed to
     * this server instance.
     * @return an array of deployed web module ObjectName. Returns an array of 0
     * length if none are deployed.
     * @throws ServerinstanceException
     */
    public ObjectName[] getAllDeployedWebModules() throws ServerInstanceException {
       return getAllRegisteredComponentsOfType(deployableObjectTypes[2]);
    }
    
    /**
     * Returns an array of deployed connectors ObjectName.
     * @return an array of deployed connectors ObjectName. Returns an array of 0 length
     * if none are deployed.
     * @throws ServerinstanceException
     */
    public ObjectName[] getAllDeployedConnectors() throws ServerInstanceException {
        
        return getAllRegisteredComponentsOfType(deployableObjectTypes[3]);
    }

    /**
     * Returns the names of connectors embedded within a deployed EAR.
     * @param appName The name of the deployed EAR. If the name is null, 
     * searches for embedded connectors within all the EARs deployed on the 
     * given target.
     * @param targetName Target used to filter the apps deployed on the
     * specific target. If target is domain, all the applications deployed to 
     * the domain are searched.
     * @return Returns an array of names of the embedded connectors. The 
     * returned array will atleast be of 0 length.
     * @throws ServerInstanceException
     */
    public String[] getEmbeddedConnectorNames(String appName, 
        String targetName) throws ServerInstanceException 
    {
        final List names = new ArrayList();
        try {
            final com.sun.enterprise.admin.target.Target target = 
                getListTarget(targetName);
            final String[] apps = filterAppsByTargetAndAppName(target, appName);
            for (int i = 0; i < apps.length; i++) {
                final Application ad = getDescrForApplication(apps[i]);
                final String app_name = ad.getRegistrationName();
                final Set bds = ad.getRarDescriptors();
                for(Iterator it = bds.iterator(); it.hasNext(); ) {
                    final ConnectorDescriptor cd = (ConnectorDescriptor)it.next();
                    final String rarJndiName = app_name + 
                        ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER + 
                        FileUtils.makeFriendlyFilenameNoExtension(cd.getDeployName());
                    names.add(rarJndiName);
                }
            }
        } catch (ConfigException e) {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
        return (String[])names.toArray(new String[0]);
    }

    String[] filterAppsByTargetAndAppName(
        com.sun.enterprise.admin.target.Target target, String appName)
        throws ConfigException
    {
        //1. Apply target filter
        /**
         * Begin_Note: The following logic should be part of 
         * getAllDeployedJ2EEApplications(target).
         */
        ObjectName[] appObjectNames = getAllRegisteredComponentsOfType(
            deployableObjectTypes[0]);
        if (target.getType() != 
            com.sun.enterprise.admin.target.TargetType.DOMAIN) {
            final ApplicationRef[] appRefs = target.getApplicationRefs();
            appObjectNames = ObjectNameAppRefComparator.intersect(
                appObjectNames, appRefs);
        }
        //End_Note
        final Set s = new HashSet();
        if (null != appObjectNames) {
            for (int i = 0; i < appObjectNames.length; i++) {
                s.add(appObjectNames[i].getKeyProperty(NAME));
            }
        }
        //2. Apply appName filter
        if (null != appName) {
            if (s.contains(appName)) {
                return new String[] {appName};
            } else {
                throw new ConfigException(localStrings.getString(
                    "admin.mbeans.acmb.appRefDoesnotExistForTheTarget", 
                    appName, target.getName()));
            }
        }
        return (String[])s.toArray(new String[0]);
    }

    ////// new set of getAll methods that take target

    /**
     * Returns an array of all the deployed components.
     * @param targetName all deployed components on the specified target
     * @return an array of deployed components. Returns an array of 0 length
     * if none are deployed.
     * @throws ServerinstanceException
     */
    public ObjectName[] getAllDeployedComponents(String targetName)
        throws ServerInstanceException
    {
        ObjectName[] oa = new ObjectName[0];
        try
        {
            final com.sun.enterprise.admin.target.Target target = 
                getListTarget(targetName);
            final ObjectName[] allComponents = getAllDeployedComponents();
            if (target.getType() == TargetType.DOMAIN)
            {
                return allComponents;
            }
            final ApplicationRef[] appRefs = target.getApplicationRefs();
            oa = ObjectNameAppRefComparator.intersect(allComponents, appRefs);
        }
        catch (Exception e) {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
        return oa;
    }

        protected com.sun.enterprise.admin.target.Target 
    getListTarget(String targetName) throws ConfigException
    {
        return com.sun.enterprise.admin.target.TargetBuilder.INSTANCE.createTarget(
                    VALID_LIST_TYPES, targetName, getConfigContext());
    }

    /**
     * Returns an array of all the user deployed components.
     * @return an array of user deployed components. 
     * @throws ServerinstanceException
     */
    public ObjectName[] getAllUserDeployedComponents() throws ServerInstanceException {
                
        ObjectName [] allComponents = new ObjectName[0];
        
        ObjectName [] sysNuserComponents = getAllDeployedComponents();
        
        try {
            ArrayList arrList = new ArrayList();
            
            for (int i=0; i<sysNuserComponents.length; i++) {
                String type = sysNuserComponents[i].getKeyProperty("type");		
                    // hack for app-client-module since it doesn't have
                    // object type in the dtd
                if (type.equals("appclient-module")) {
                    arrList.add(sysNuserComponents[i]);
                    continue;
                }
                    // get the object-type attribute
                    // to determine if it is a system app
                MBeanServer mbs = MBeanServerFactory.getMBeanServer();
                String objectType = (String)mbs.getAttribute(sysNuserComponents[i], OBJECT_TYPE);
                if ((objectType != null) && 
                    (objectType.startsWith(Constants.SYSTEM_PREFIX))) {
                    continue;
                }
                    // add the object name 
                arrList.add(sysNuserComponents[i]);
            }
            if (!arrList.isEmpty()) {
                allComponents = (ObjectName[])arrList.toArray(new ObjectName[arrList.size()]);
            }

        } catch (Exception e) {
            sLogger.log(Level.WARNING, "ApplicationsConfigMBean.getAllUserDeployedComponents", e);
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
        return (allComponents);
    }

    /**
     * Returns an array of user deployed components.
     * @param targetName 
     * @return an array of user deployed components. Returns an array of 0 
     * length if none are deployed.
     * @throw ServerInstanceException
     */
    public ObjectName[] getAllUserDeployedComponents(String targetName)
        throws ServerInstanceException
    {
        ObjectName[] oa = new ObjectName[0];
        try
        {
            final com.sun.enterprise.admin.target.Target target = 
                getListTarget(targetName);
            final ObjectName[] allComponents = getAllUserDeployedComponents();
            if (target.getType() == TargetType.DOMAIN)
            {
                return allComponents;
            }
            final ApplicationRef[] appRefs = target.getApplicationRefs();
            oa = ObjectNameAppRefComparator.intersect(allComponents, appRefs);
        }
        catch (Exception e) {
            sLogger.log(Level.WARNING, "mbean.list_failed", e);
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
        return oa;
    }

    /**
     * Returns the list of deployed J2EEApplications ObjectName. These are the
     * applications that are deployed to domain, and are registered
     * under <applications>
     * @target all deployed components on the specified target
     * @return list of deployed j2ee applications
     * @throws ServerInstanceException
     */
    public ObjectName[] getAllDeployedJ2EEApplications(String target) throws ServerInstanceException {
    //FIXME provide implementation for target
        return getAllRegisteredComponentsOfType(deployableObjectTypes[0]);
    }

    /**
     * Returns an array of standalone ejb module ObjectName that are deployed to
     * this server instance.
     * @target all deployed components on the specified target
     * @return an array of deployed ejb module ObjectName.  Returns an array of 0
     * length if none are deployed.
     * @throws ServerinstanceException
     */
    public ObjectName[] getAllDeployedEJBModules(String target) throws ServerInstanceException {
    //FIXME provide implementation for target
        return getAllRegisteredComponentsOfType(deployableObjectTypes[1]);
    }

    /**
     * Returns an array of standalone war module ObjectName that are deployed to
     * this server instance.
     * @target all deployed components on the specified target
     * @return an array of deployed web module ObjectName. Returns an array of 0
     * length if none are deployed.
     * @throws ServerinstanceException
     */
    public ObjectName[] getAllDeployedWebModules(String target) throws ServerInstanceException {
    //FIXME provide implementation for target
       return getAllRegisteredComponentsOfType(deployableObjectTypes[2]);
    }

    /**
     * Returns an array of deployed connectors ObjectName.
     * @target all deployed components on the specified target
     * @return an array of deployed connectors ObjectName. Returns an array of 0 length
     * if none are deployed.
     * @throws ServerinstanceException
     */
    public ObjectName[] getAllDeployedConnectors(String target) throws ServerInstanceException {
    //FIXME provide implementation for target
        return getAllRegisteredComponentsOfType(deployableObjectTypes[3]);
    }

    /**
     * Returns an array of deployed appclient ObjectName.
     * @target all deployed components on the specified target
     * @return an array of deployed appclient ObjectName.
     * Returns an array of 0 length if none are deployed.
     * @throws ServerinstanceException
     */
    public ObjectName[] getAllDeployedAppclientModules(String target)
                            throws ServerInstanceException
    {
        //FIXME provide implementation for target
        return getAllRegisteredComponentsOfType(DeployableObjectType.CAR);
    }
    
    private void sendEnableConfigChangeEventExplicitly(String targetName) 
        throws ConfigException, DeploymentException {
        // first flush the config change
        ConfigContext ctx = getConfigContext();
        if (ctx.isChanged()) {
            ctx.flush();
        }

        // then remove this config change from the config change list, so 
        // no events will be sent by ConfigInterceptor
        ArrayList changeList = ctx.getConfigChangeList(); 
        ElementChangeHelper elementHelper = new ElementChangeHelper(); 
        ConfigChange enabledConfigChange = 
            ElementChangeHelper.removeEnabledChange(changeList);

        // now send this event explicitly here
        if (enabledConfigChange != null) {
            ArrayList enabledChangeList = new ArrayList(); 
            ArrayList eventList = new ArrayList(); 
            enabledChangeList.add(enabledConfigChange);
            AdminEvent[] elementChangeEvents = 
                elementHelper.generateElementChangeEventsFromChangeList(
                    targetName, enabledChangeList, ctx);
            if (elementChangeEvents != null) {
                for (int i=0; i<elementChangeEvents.length; i++) {
                    eventList.add(elementChangeEvents[i]);
                }
            }

            Iterator iter = eventList.iterator();
            while (iter.hasNext()) {
                AdminEvent event = (AdminEvent)iter.next();
                if (sLogger.isLoggable(Level.FINEST)) {
                    sLogger.log(Level.FINEST, "mbean.event_sent",
                            event.getEventInfo());
                } else {
                    sLogger.log(Level.INFO, "mbean.send_event", 
                        event.toString());
                }
                AdminEventResult result = AdminEventMulticaster.multicastEvent(
                    event);            
                sLogger.log(Level.FINE, "mbean.event_res", 
                    result.getResultCode());
                sLogger.log(Level.FINEST, "mbean.event_reply",
                    result.getAllMessagesAsString());

                AdminEventListenerException ale = null;
                ale = result.getFirstAdminEventListenerException();
                if (ale != null) {
                    sLogger.log(Level.WARNING, "mbean.event_failed",
                        ale.getMessage());
                    DeploymentException de =
                        new DeploymentException(ale.getMessage());
                    de.initCause(ale);
                    throw de;
                }
            }
        }
    }

    /**
     * Enables an application or module on the specified target
     * In case of a cluster, the application refs of the server
     * instances in that cluster are also enabled.
     * @param appName name of the application to be enabled
     * @param type
     * @param target target name on which app has to be enabled
     */
    public void enable(String appName, String type, String target)
    throws MBeanConfigException, DeploymentException {

        try{
            ObjectName componentON = null;
            if(type != null) {
                DeployableObjectType objectType = getDeployableObjectType(type);    
                componentON = getRegisteredComponentObjectName(appName, objectType);
            } else {
                componentON = getRegisteredComponentObjectName(appName);
            }

            if (componentON != null) {
                String configMBModuleType = componentON.getKeyProperty("type");
                if (configMBModuleType != null) {
                    if (configMBModuleType.equals(ServerTags.APPCLIENT_MODULE)) {
                        throw new DeploymentException(
                            localStrings.getString("admin.mbeans.J2EEModule.not_applicable_op",
                            "enable", appName));
                    }
                }
            }

            MBeanServer mbs = MBeanServerFactory.getMBeanServer();
                
            //enable application in central repository
            //FIXME. This one should not be necessary once we fix JSR88 client to
            //have enabled=true upon deploying to domain
            Attribute attr = new Attribute("enabled","true");
            if(componentON != null) {
                mbs.setAttribute(componentON, attr);
            } 

            // if target is not domain, also enable app-refs for the target. 
	    // In case of cluster, also enable for every instance in a cluster
            if (!target.equals(DOMAIN_TARGET)) {           
	        setAppEnableInRefs(appName, target, true);
            }

            sendEnableConfigChangeEventExplicitly(target);

        }catch(Throwable te){
            if (! (te instanceof DeploymentException)) {
                sLogger.log(Level.SEVERE, "enable", te);
                sLogger.log(Level.FINE,"enable exception");
            }
                throw new MBeanConfigException(te.getMessage());
        }
    }
    
    /**
     * Disable an application or module on the specified target
     * In case of a cluster, the application refs of the server
     * instances in that cluster are also disabled.
     * @param appName name of the application to be disabled
     * @param type
     * @param target target name on which app has to be disabled
     */
    public void disable(String appName, String type, String target)
    throws MBeanConfigException, DeploymentException {
        try{
            ObjectName componentON = null;

            if(type != null) {
                DeployableObjectType objectType = getDeployableObjectType(type);
                componentON = getRegisteredComponentObjectName(appName, objectType);
            }
            else {
                componentON = getRegisteredComponentObjectName(appName);
            }

            if (componentON != null) {
                String configMBModuleType = componentON.getKeyProperty("type");
                if (configMBModuleType != null) {
                    if (configMBModuleType.equals(ServerTags.APPCLIENT_MODULE)) {
                        throw new DeploymentException(
                            localStrings.getString("admin.mbeans.J2EEModule.not_applicable_op",
                            "disable", appName));
                    }
                }
            }

            MBeanServer mbs = MBeanServerFactory.getMBeanServer();
            
            Attribute attr = new Attribute("enabled","false");
            // if target is domain, disable the domain level 
            // application enabled attribute
            if (target.equals(DOMAIN_TARGET)) {
                if(componentON != null ) {
                    mbs.setAttribute(componentON, attr);
                }
            } else {
	        // otherwise disable app-refs for the target. 
	        // In case of cluster, also disable for every instance in a 
                // cluster
	        setAppEnableInRefs(appName, target, false);
            }

            sendEnableConfigChangeEventExplicitly(target);

        } catch(Throwable te){
            if (! (te instanceof DeploymentException)) {
                sLogger.log(Level.FINE,"disable exception");
            }
            throw new MBeanConfigException(te.getMessage());
        }
    }


    /**
     * enable/disable app-refs for the target. 
     * In case of cluster, also enable/disable 
     * for every instance in a cluster
     */
    private void setAppEnableInRefs(String appName, String target, boolean enable) 
								throws Exception {
        Attribute attr = new Attribute("enabled", "" + enable);
        MBeanServer mbs = MBeanServerFactory.getMBeanServer();
        ObjectName apprefON = getRegisteredAppRefObjectName(appName, target);

	if (apprefON != null) {
            mbs.setAttribute(apprefON,attr);
	}

        if (isCluster(target)) {
	    ObjectName[] ons = getAllRefObjectNames(appName, target);
	    if (ons != null) {
		for (int i = 0; i < ons.length; i ++ ) {
                    mbs.setAttribute(ons[i],attr);
	   	}
            }
        }
	    
    }

    /**
     * gets object names for application refs in all server
     * instances in the cluster
     */
    private ObjectName[] getAllRefObjectNames(String appName, String cluster) 
								throws Exception {
	Server[] servers = ServerHelper.getServersInCluster(getConfigContext(), cluster);
	if(servers == null) return null;
	ObjectName[] ons = new ObjectName[servers.length];
        MBeanServer mbs = MBeanServerFactory.getMBeanServer();
	ObjectName serverON = null;
	for (int j = 0; j < servers.length; j ++ ) {
            serverON = super.getServerObjectName(servers[j].getName());
            ObjectName[] apprefONArr = (ObjectName[])mbs.invoke(serverON,
               		"getApplicationRef", emptyParams, emptySignature);
            for(int i =0; i< apprefONArr.length;i++)
            {
                String ref = (String)mbs.getAttribute(apprefONArr[i], "ref");
                if(appName.equals(ref))
		    ons[j] = apprefONArr[i];
            }
	}
	return ons;
    }

    private boolean isCluster(String targetName) 
					throws Exception {
        final com.sun.enterprise.admin.target.Target target =
            com.sun.enterprise.admin.target.TargetBuilder.INSTANCE.createTarget(
                VALID_LIST_TYPES, targetName, getConfigContext());
        if (target.getType() == TargetType.CLUSTER) {
	    return true;
        }
	return false;
    }

    /**
     * Returns the status of the application as in config.
     * is specified target is null/blank/"domain" then only the
     * enabled flag of actual application is used. Else enabled
     * flag of application-ref is also used to determine the
     * enabled status
     * @param name name of the component whose status is returned
     * @target name of the target [server/group/domain]
     * @throws MBeanConfigException in case of any exception
     */
    public boolean getStatus(String name, String target)
                      throws MBeanConfigException
    {
        try{
            boolean appEnabled = true;
            boolean appRefEnabled = true;
            MBeanServer mbs = MBeanServerFactory.getMBeanServer();
            ObjectName componentON = null;
            
            try{
                componentON = getRegisteredComponentObjectName(name);
            }catch(Exception e){
                if(componentON == null)
                    throw new MBeanConfigException(e.getMessage());
            }
            String appEnabledStr = (String)mbs.getAttribute(componentON,
                                                   ServerTags.ENABLED);

            if("false".equalsIgnoreCase(appEnabledStr))
                appEnabled = false;

            if(!isDefaultTarget(target))
            {
                ObjectName apprefON =
                            getRegisteredAppRefObjectName(name, target);
                String appRefEnabledStr =
                        (String)mbs.getAttribute(apprefON, ServerTags.ENABLED);
                if("false".equalsIgnoreCase(appRefEnabledStr))
                    appRefEnabled = false;

            }

            if(appEnabled && appRefEnabled)
            {
                return true;
            }

            return false;

        }catch(Exception e){
            sLogger.log(Level.WARNING,"Exception in getStatus:"+e.getMessage());
            throw new MBeanConfigException(e.getMessage());
        }
    }

    
    /**
     * Returns true if redeployment of is supported
     * @return true/false
     */
    public boolean isRedeploySupported()
    throws MBeanConfigException {
        return true;
    }
    
    ////////////Application Versioning/////////////
    
    /**
     * Returns a list of all available versions of an application
     * @param appName
     * @param type type of the application
     * @return list of versions of the app
     */
    public String[] getAvailableVersions(String appName, String type) throws MBeanConfigException {
        throw new MBeanConfigException("Notyet supported");
    }
    
    /**
     * Returns the default(currently active) version of the specified application
     * or module
     * @param appName
     * @param type type of the application
     * @return default Version
     **/
    public String getDefaultVersion(String appName, String type) throws MBeanConfigException {
        return "";
    }
    
    /**
     * Returns the last modified time for an application or module
     * @param appName
     * @param type type of the application
     * @return lastModifiedTime
     */
    public String getLastModified(String appName, String type) throws MBeanConfigException {
        return "";
    }
    
    /**
     * Returns true if repository cleaner is enabled
     * @return boolean
     */
    public boolean isRepositoryCleanerEnabled() throws MBeanConfigException {
        return true;
    }
    
    /**
     * Returns the polling interval for repository cleaner
     * @return int
     */
    public int getRepositoryCleanerPollingInterval() throws MBeanConfigException {
        return 2;
    }
    
    /**
     * Sets the repository cleaner interval to specified time
     * @param interval polling interval for repository cleaner
     */
    public void setRepositoryCleanerPollingInterval(int interval)
    throws MBeanConfigException {
    }
    
    /**
     * Returns maximum application versions stored in application repository
     * @return int
     */
    public int getMaxApplicationVersions() throws MBeanConfigException {
        return 10;
    }
    
    /**
     * Sets maximum application version stored in application repository to
     * specified number
     * @param maxVersion maximum app version in app repository
     */
    public void setMaxApplicationVersions(int maxVersions)
    throws MBeanConfigException {
    }
    
    ////////////////Auto Deployment////////////////////
    
    /**
     * Returns true if autoDeployment is enabled
     * @return boolean
     */
    public boolean isAutoDeployEnabled() throws MBeanConfigException {
        return true;
    }
    
    /**
     * Enables auto deployment
     */
    public void setAutoDeployEnabled() throws MBeanConfigException {
    }
    
    /**
     * Returns true if auto deployment jspPreCompilation is enabled
     * @return boolean
     */
    public boolean isAutoDeployJspPreCompilationEnabled()
    throws MBeanConfigException {
        return true;
    }
    
    /**
     * Enabled auto deployment jspPreCompilation
     */
    public void setAutoDeployJspPreCompilationEnabled()
    throws MBeanConfigException {
    }
    
    /* 
     *  The following lificeycle module realated operations are added to fulfil
     *  request unified API for all applications (bug #4934857)
     */
    /**
     */
    public ObjectName[] getLifecycleModule(String targetName) throws Exception
    {
        final com.sun.enterprise.admin.target.Target target = 
            com.sun.enterprise.admin.target.TargetBuilder.INSTANCE.createTarget(
                VALID_LIST_TYPES, targetName, getConfigContext());
        final ObjectName[] registeredModules = 
            (ObjectName[])invoke("getLifecycleModule", null, null);
        if (target.getType() == TargetType.DOMAIN)
        {
            return registeredModules;
        }
        final ApplicationRef[] refs = target.getApplicationRefs();
        final ArrayList al = new ArrayList();
        for (int i = 0; i < registeredModules.length; i++)
        {
            final String name = registeredModules[i].getKeyProperty(
                ServerTags.NAME);
            for (int j = 0; j < refs.length; j++)
            {
                if (refs[j].getRef().equals(name))
                {
                    al.add(registeredModules[i]);
                    continue;
                }
            }
        }
        final ObjectName[] refModules = new ObjectName[al.size()];
        for (int i = 0; i < refModules.length; i++)
        {
            refModules[i] = (ObjectName)al.get(i);
        }
        return refModules;
    }

    /**
     */
    public ObjectName getLifecycleModuleByName(String key, String targetName) 
        throws Exception
    {
        final com.sun.enterprise.admin.target.Target target = 
            com.sun.enterprise.admin.target.TargetBuilder.INSTANCE.createTarget(
                VALID_LIST_TYPES, targetName, getConfigContext());
        final ObjectName on = (ObjectName)invoke("getLifecycleModuleByName", 
            new Object[]{key}, new String[]{"java.lang.String"});
        if (!(target.getType() == TargetType.DOMAIN))
        {
            final ApplicationRef[] refs = target.getApplicationRefs();
            boolean isReffed = false;
            for (int i = 0; i < refs.length; i++)
            {
                if (refs[i].getRef().equals(key))
                {
                    isReffed = true;
                    break;
                }
            }
            if (!isReffed)
            {
                throw new ConfigException(localStrings.getString(
                    "applicationRefDoesnotExist", targetName, key));
            }
        }
        return on;
    }

    static Map getAttributeValues(AttributeList attributeList, String[] names)
    {
        int len = names.length;
        final Map values = new HashMap(len);
        final Iterator it = attributeList.iterator();
        while (it.hasNext())
        {
            final Attribute a = (Attribute)it.next();
            final String name = a.getName();
            for (int i = 0; i < len; i++)
            {
                if (name.equals(names[i]))
                {
                    values.put(name, a.getValue());
                }
            }
        }
        return values;
    }

    static final String NAME    = ServerTags.NAME;
    static final String ENABLED = ServerTags.ENABLED;

    public Boolean isLifecycleModuleRegistered(String name)
    {
        Boolean isRegistered = Boolean.FALSE;
        try
        {
            final ObjectName on = (ObjectName)invoke("getLifecycleModuleByName", 
                new Object[]{name}, new String[]{"java.lang.String"});
            if(on != null) {
                isRegistered = Boolean.TRUE;
            }
        }
        catch (Exception e)
        {
            //ignore ok.
        }
        return isRegistered;
    }

    /**
     * Creates a life cycle module element and adds an app ref to the given
     * target.
     * @param attribute_list
     * @param targetName
     * @return Returns ObjectName corresponding to the created life cycle 
     * module.
     * @throws Exception
     * @deprecated Use method with another signature with Properties instead
     */
    public ObjectName createLifecycleModule(AttributeList attribute_list, 
                                            String        targetName)
        throws Exception
    {
        return createLifecycleModule(attribute_list, null, targetName);

    }
    
    /**
     * Creates a life cycle module element and adds an app ref to the given
     * target.
     * @param attribute_list
     * @param targetName
     * @param props
     * @return Returns ObjectName corresponding to the created life cycle 
     * module.
     * @throws Exception
     */
    public ObjectName createLifecycleModule(AttributeList attribute_list, 
                                            Properties    props,
                                            String        targetName)
        throws Exception
    {
        final Map values = getAttributeValues(attribute_list, 
                                new String[]{NAME, ENABLED});
        final String name = (String)values.get(NAME);

	ConfigContext ctx = getConfigContext();
	ConfigBean app = ApplicationHelper.findApplication(ctx, name);
	if ( (app!=null) && !(app instanceof LifecycleModule) ) {

		throw new ConfigException(localStrings.getString(
		    "admin.mbeans.acmb.duplicateLCMName", name));
	}

        Boolean isRegistered = isLifecycleModuleRegistered(name);
        final DeploymentTarget target = getAndValidateDeploymentTarget(
            targetName, name, isRegistered.booleanValue(), false);
        /**
            TODO: There is a catch here. What if the module was already 
            registered. Should we just add the app reference to the given 
            target or should we allow the following config call to raise an
            exception?
         */
        final ObjectName on = (ObjectName)invoke("createLifecycleModule", 
                    new Object[]{attribute_list}, 
                    new String[]{"javax.management.AttributeList"});

                    
        //add properties into lifecycle element
        if (props!=null && props.size()>0) {
            //get created bean
            ArrayList arr = new ArrayList();
            for (Enumeration e = props.propertyNames(); e.hasMoreElements() ;) {
                String propName = (String)e.nextElement();
                String propValue = (String)props.getProperty(propName);
                if (propValue != null) {
                    ElementProperty ep = new ElementProperty();
                    ep.setName(propName);
                    ep.setValue(propValue);                    
                    arr.add(ep);
                }
            }
            if(arr.size()>0)
            {
                LifecycleModule lcm = (LifecycleModule)ApplicationHelper.findApplication(ctx, name);
                lcm.setElementProperty((ElementProperty[])arr.toArray(new ElementProperty[arr.size()]));
            }
        }

        final String isEnabled = (String)values.get(ENABLED);
        boolean enabled = Boolean.valueOf(isEnabled).booleanValue();
        target.addAppReference(name, enabled, null);
        return on;
    }

    /**
     * Creates a reference for a lifecycle module in the given target
     * @param referenceName the module ID
     * @param targetName the target
     * @param options
     * @return returns a DeploymentStatus
     */
    public DeploymentStatus createLifecycleModuleReference(String referenceName, String targetName, Map options) {
        DeploymentStatus ds = new DeploymentStatus();
        ds.setStageDescription("CreateLifecycleModuleReference");
        try {
            final DeploymentTarget target = getAndValidateDeploymentTarget(
                targetName, referenceName, false, false);
            boolean enabled = (new DeploymentProperties(options)).getEnable();
            target.addAppReference(referenceName, enabled, null);
            ds.setStageStatus(DeploymentStatus.SUCCESS);
        } catch(Exception e) {
            ds.setStageException(e);
            ds.setStageStatus(DeploymentStatus.FAILURE);
            ds.setStageStatusMessage(e.getMessage());
        }
        return ds;
    }
    
    /**
     * Deletes a reference for a lifecycle module in the given target
     * @param referenceName the module ID
     * @param targetName the target
     * @param options
     * @return returns a DeploymentStatus
     * @throws Exception
     */
    public DeploymentStatus removeLifecycleModuleReference(String referenceName, String targetName) {
        DeploymentStatus ds = new DeploymentStatus();
        ds.setStageDescription("RemoveLifecycleModuleReference");
        try {
            final DeploymentTarget target = getAndValidateDeploymentTarget(
                targetName, referenceName, false, false);
            target.removeAppReference(referenceName);
            ds.setStageStatus(DeploymentStatus.SUCCESS);
        } catch(Exception e) {
            ds.setStageException(e);
            ds.setStageStatus(DeploymentStatus.FAILURE);
            ds.setStageStatusMessage(e.getMessage());
        }
        return ds;
    }

    /**
     * Removes the app reference to the life cycle module from the given target.
     * Performs integrity checks and removes the corresponding life cycle
     * module element from the config.
     * @param name
     * @param targetName
     * @throws Exception
     */
    public void removeLifecycleModuleByName(String name, String targetName)
        throws Exception
    {
        Boolean isRegistered = isLifecycleModuleRegistered(name);
        final DeploymentTarget target = getAndValidateDeploymentTarget(
            targetName, name, isRegistered.booleanValue(), true);
        target.removeAppReference(name);
        invoke("removeLifecycleModuleByName", new Object[]{name}, 
            new String[]{"java.lang.String"});
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Helper methods
    ////////////////////////////////////////////////////////////////////////////
   
    /**
     *
     */
    private DeploymentService getDeploymentService() throws DeploymentException {
        if(deployService == null) {
            deployService = DeploymentService.getDeploymentService(
                                getConfigContext());
        }        
        return deployService;
    }

    /**
     *
     */
    private String getInstanceName() throws DeploymentException
    {

        if(mInstanceName == null)
        {
            try{
                MBeanServer mbs = MBeanServerFactory.getMBeanServer();
                ObjectName serversON = new ObjectName(getDomainName()+":type=servers,category=config");
                ObjectName[] serverONArr = (ObjectName[])mbs.invoke(serversON, "getServer", new Object[]{}, new String[]{});
                mInstanceName = (String)mbs.getAttribute(serverONArr[0], "name");                
            }catch(Exception e){
                sLogger.log(Level.WARNING,"Could not obtain instanceName");
                throw new DeploymentException("Could not obtain instanceName");
            }
        }
        return mInstanceName;
    }
     
    /**
     * Checks if application or module of name name and type type
     * is already registered in the config
     * @param name name of the app/module
     * @param type [Application,Ejb,Web,Connector,Appclient]
     * @returns true if specified app/module is registered
     */
     private boolean isRegistered(String name, DeployableObjectType type) {        
        try {
            if(getRegisteredComponentObjectName(name,type) != null)
                return true;
            else
                return false;
        }
        catch (Exception e) {
            sLogger.log(Level.WARNING, "appexists failed", e); //noi18N
        }
        return true;
    }
     
    /**
     * Returns the type of the registered component.
     * If the component is not registered throws a DeploymentException
     * @param name name of the component
     * @return type DeployableObjectType of the registered component(app/module)
     */
    private DeployableObjectType getRegisteredType(String name) throws DeploymentException
    {
        try{
            for(int i = 0; i< deployableObjectTypes.length; i++)
            {
                if(isRegistered(name, deployableObjectTypes[i]))
                    return deployableObjectTypes[i];
            }        
        }catch(Exception e){
            throw new DeploymentException(e.getMessage());
        }       
        throw new DeploymentException("Component not registered");
    }
    
    /**
     * This method returns the deployableObjectType of an archive by checking the 
     * deployable descriptors in the archive
     * @param filePath absolute path to the archive
     * @return type DeployableObjectType
     */
    private DeployableObjectType getTypeFromFile(String moduleID, String filePath) throws DeploymentException{
        return DeploymentServiceUtils.getTypeFromFile(moduleID, filePath);
    }
    
    /**
     * This method checks if any of the virtual servers has the given web
     * module as default-web-module. If yes, it throws exception.
     * @param webModuleName the name of the web module.
     * @throws ConfigException if any of the virtual servers has this web
     * module as default-web-module.
     */
    private void checkWebModuleReferences(String webModuleName)
    throws ConfigException {
        ArrayList virtualServerIds = new ArrayList();
        
        //ms1  Server rootElement = ServerBeansFactory.getServerBean(context);
        Config config  = (Config) ConfigBeansFactory.getConfigBeanByXPath(getConfigContext(),ServerXPathHelper.XPATH_CONFIG);
        HttpService httpService = config.getHttpService();
        VirtualServer[] virtualServers = httpService.getVirtualServer();
        for (int j = 0; j < virtualServers.length; j++) {
            VirtualServer aServer   = virtualServers[j];
            String defWebModule     = aServer.getDefaultWebModule();
            if ((defWebModule != null) &&
            (defWebModule.equals(webModuleName))) {
                virtualServerIds.add(aServer.getId());
            }
        }

        if (!virtualServerIds.isEmpty()) {
            throw new ConfigException(localStrings.getString(
                                    "admin.mbeans.acmb.def_web_module_refs_exist",
                                                            virtualServerIds.toString(), webModuleName));
        }
    }
    
    
    /**
     * Deletes a file from the temporary location.
     * Deletes the given file only if it is in the temporary location.
     * FIXME: After starting to use Repository object, this method will not be required.
     */
    private void deleteFile(String filePath) {
        
        try{
            File f = new File(filePath);
        if (f.exists()) {
            File parentDir = f.getParentFile();
            File tmpDir = new File(AdminService.getAdminService().
            getTempDirPath(), getInstanceName());
            /* note that the above call may return a null */
            if (tmpDir != null && tmpDir.equals(parentDir)) {
                boolean couldDelete = f.delete();
                if (couldDelete) {
                    sLogger.log(Level.FINE, "mbean.delete_temp_file_ok", filePath);
                }
                else {
                    sLogger.log(Level.INFO, "mbean.delete_temp_file_failed", filePath);
                }
            }
        }
        }catch(Exception e){
            sLogger.log(Level.WARNING,"could not deletefile"+filePath);
        }
    }
    
    
    

    /**
     * Converts moduleType in string to DeployableObjectType
     * @param moduleType application/ejb/web/connector
     * @return DeployableObjectType
     * @throws DeploymentException 
     */
    private DeployableObjectType getDeployableObjectType(String moduleType) throws DeploymentException {
        if(moduleType.equals(TYPE_APPLICATION) || moduleType.equals(JSR88_TYPE_APPLICATION)) {
            return DeployableObjectType.APP;
        }
        else if(moduleType.equals(TYPE_EJB) || moduleType.equals(JSR88_TYPE_EJB)) {
            return DeployableObjectType.EJB;
        }
        else if(moduleType.equals(TYPE_WEB) || moduleType.equals(JSR88_TYPE_WEB)) {
            return DeployableObjectType.WEB;
        }
        else if(moduleType.equals(TYPE_CONNECTOR) || moduleType.equals(JSR88_TYPE_CONNECTOR)) {
            return DeployableObjectType.CONN;
        }
        else if(moduleType.equals(TYPE_APPCLIENT) || moduleType.equals(JSR88_TYPE_APPCLIENT)) {
            return DeployableObjectType.CAR;
        }
        else if(moduleType.equals(XModuleType.LCM.toString())) {
            return DeployableObjectType.LCM;
        }
        else if(moduleType.equals(XModuleType.CMB.toString())) {
            return DeployableObjectType.CMB;
        }
        else {
            throw new DeploymentException("Unknown deployable object type");
        }
    }
    
    /**
     * legacy method from managedserverinstance
     * @param dir
     * @param user
     */
    private void chownDir(File dir, String user) {
        if (dir == null || user == null || user.trim().equals("")) {
            return;
        }
        String err = null;
        /*installConfig is removed and we need better alternative */
        /*
        installConfig cfg = new installConfig();
        err = cfg.chownDir(dir.getAbsolutePath(), user);
        if (err != null) {
            sLogger.log(Level.WARNING, err);
        }
         */
    }
    
    /**
     * @throws ServerInstanceException
     */
    
    public HostAndPort getHostAndPort() throws ServerInstanceException {
        return getHostAndPort(false);
    }
    
    /**
     * @param securityEnabled
     * @throws ServerInstanceException
     */
    public HostAndPort getHostAndPort(boolean securityEnabled) throws ServerInstanceException {
        HostAndPort hAndp = null;
        try {
            MBeanServer mbs = MBeanServerFactory.getMBeanServer();
            ObjectName objectName = new ObjectName(getDomainName()+":type=configs,category=config");
            String operationName1 = "getConfig";
            ObjectName[] configs = (ObjectName[])mbs.invoke(objectName,operationName1, emptyParams,emptySignature);
            String configName = (String)mbs.getAttribute(configs[0], "name");
            ObjectName httpService = new ObjectName(getDomainName()+":type=http-service,config="+configName+",category=config");
            String operationName2 = "getHttpListener";
            ObjectName[] httpListener = (ObjectName[])mbs.invoke(httpService, operationName2,emptyParams,emptySignature);

            String serverName = null;
            int port = 0;
            for (int i = 0; i < httpListener.length; i++) {
                AttributeList attrs = mbs.getAttributes(httpListener[i],
                        httpListenerAttrNames);
                Boolean bb = Boolean.valueOf((String)getNamedAttributeValue(
                        attrs, LISTENER_ENABLED));
                boolean enabled = ((bb == null) ? false : bb.booleanValue());
                if (!enabled) {
                    // Listener is not enabled
                    continue;
                }
                String vs = (String)getNamedAttributeValue(attrs, DEF_VS);
                if (ADMIN_VS.equals(vs)) {
                    // Listener is reserved for administration
                    continue;
                }
                bb = Boolean.valueOf((String)getNamedAttributeValue(
                        attrs, SEC_ENABLED));
                boolean sec_on = ((bb == null) ? false : bb.booleanValue());
                if (securityEnabled == sec_on) {
                    serverName = (String)getNamedAttributeValue(attrs,
                            SERVER_NAME);
                    if (serverName == null || serverName.trim().equals("")) {
                        serverName = getDefaultHostName();
                    }
                    String portStr = (String)getNamedAttributeValue(attrs,
                            PORT);
                    String redirPort = (String)getNamedAttributeValue(attrs,
                            REDIRECT_PORT);
                    if (redirPort != null && !redirPort.trim().equals("")) {
                        portStr = redirPort;
                    }
                    String resolvedPort = 
                        new PropertyResolver(getConfigContext(), 
                            getInstanceName()).resolve(portStr);
                    port = Integer.parseInt(resolvedPort);
                    break;
                }
            }
            hAndp = new HostAndPort(serverName, port);
        }
        catch (Exception e) {
            ServerInstanceException sie = 
                new ServerInstanceException(e.getLocalizedMessage()); 
            sie.initCause(e);
            throw sie;
        }
        return hAndp;
    }
    
    /**
     * Gets the host and port for a given stand-alone module id
     *
     * @param standAloneModuleId
     * @param securityEnabled
     * @throws ServerInstanceException
     */
    public HostAndPort getHostAndPort(String standAloneModuleId, boolean securityEnabled) 
	throws ServerInstanceException {

        HostAndPort hAndp = null;
	boolean setHP = false;

        try {

	    // Application Ref element for the given module

	    String appRefXPath = ServerXPathHelper.getServerIdXpath(getInstanceName())
				 + ServerXPathHelper.XPATH_SEPARATOR
				 + ServerTags.APPLICATION_REF + "[@"
				 + ServerTags.REF + "='" + standAloneModuleId + "']";

	    ApplicationRef appRef = (ApplicationRef) ConfigBeansFactory.getConfigBeanByXPath(
				                     getConfigContext(), appRefXPath);

            // if no virtual server, pick up first
            if (appRef.getVirtualServers()!=null) {
                return getHostAndPort(securityEnabled);
            }
            
	    // Get the list of virtual servers from the Application Ref

	    String appRefvs = null;
            List vsList = StringUtils.parseStringList(appRef.getVirtualServers(), " ,");
            if (vsList==null) {
                return getHostAndPort(securityEnabled);
            }
	    ListIterator vsListIter = vsList.listIterator();

	    // Iterate for each of the virtual servers

	    while(vsListIter.hasNext()) {
                String virtualServer = (String) vsListIter.next();
                HostAndPort hp = getVirtualServerHostAndPort(virtualServer, securityEnabled);
                if (hp!=null) {
                    return hp;
                }
            }
        } catch (Exception e) {
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
        
        return null;
    }
    
    /**
     *
     */
    public HostAndPort getVirtualServerHostAndPort(String vs, boolean securityEnabled) 
        throws ServerInstanceException
    {
        String serverName = null;
        int port = 0;
        
        try {
            MBeanServer mbs = MBeanServerFactory.getMBeanServer();
            
            ObjectName objectName = new ObjectName(
            getDomainName()+":type=configs,category=config");
            String operationName1 = "getConfig";
            ObjectName[] configs = (
            ObjectName[])mbs.invoke(objectName,operationName1,
            emptyParams,emptySignature);
            String configName = (String)mbs.getAttribute(configs[0], "name");
            ObjectName httpService = new ObjectName(
            getDomainName()+":type=http-service,config="+configName+",category=config");
            
            // http listeners for the given config
            
            String operationName2 = "getHttpListener";
            ObjectName[] httpListener = (ObjectName[])mbs.invoke(httpService,
            operationName2,emptyParams,emptySignature);
            
            // virtual servers for the given config
            
            operationName2 = "getVirtualServer";
            ObjectName[] virtualServer = (ObjectName[])mbs.invoke(httpService,
            operationName2,emptyParams,emptySignature);
            
            // iterate for each of the config virtual server
            
            for (int i = 0; i < virtualServer.length; i++) {
                
                AttributeList vsAttrs = mbs.getAttributes(virtualServer[i], vsAttrNames);
                
                // virtual server id check
                //
                // if the virtual server obtained from application ref
                // does not match with the virtual server from config
                // then continue with next virtual server
                
                String id = (String)getNamedAttributeValue(vsAttrs, ID);
                if (! id.equals(vs)) {
                    continue;
                }
                
                // should we check for state, let us assume ON for PE
                
                // http listener
                //
                // Obtain the http listeners list from the virtual server
                // and iterate to match with the http listeners from config.
                // When a match is found get the host and port data
                
                String httpListeners = (String) getNamedAttributeValue(vsAttrs, "http-listeners");
                String vsHttpListener = null;
                List httpListenerList = StringUtils.parseStringList(httpListeners, " ,");
                ListIterator hlListIter = httpListenerList.listIterator();
                
                while(hlListIter.hasNext()) {
                    
                    vsHttpListener = (String) hlListIter.next();
                    
                    for (int j = 0; j < httpListener.length; j++) {
                        
                        AttributeList attrs = mbs.getAttributes(httpListener[j],
                        httpListenerAttrNames);
                        
                        // http listener id check
                        String listenerId = (String)getNamedAttributeValue(attrs, ID);
                        if (! listenerId.equals(vsHttpListener)) {
                            continue;
                        }
                        
                        Boolean bb = Boolean.valueOf((String)getNamedAttributeValue(
                        attrs, LISTENER_ENABLED));
                        boolean enabled = ((bb == null) ? false : bb.booleanValue());
                        if (!enabled) {
                            // Listener is not enabled
                            continue;
                        }
                        bb = Boolean.valueOf((String)getNamedAttributeValue( attrs, SEC_ENABLED));
                        boolean sec_on = ((bb == null) ? false : bb.booleanValue());
                        if (securityEnabled == sec_on) {
                            serverName = (String)getNamedAttributeValue(attrs, SERVER_NAME);
                            if (serverName == null || serverName.trim().equals("")) {
                                serverName = getDefaultHostName();
                            }
                            String portStr = (String)getNamedAttributeValue(attrs, PORT);
                            String redirPort = (String)getNamedAttributeValue(attrs, REDIRECT_PORT);
                            if (redirPort != null && !redirPort.trim().equals("")) {
                                portStr = redirPort;
                            }
                            final String resolvedPort = 
                                    new PropertyResolver(getConfigContext(), 
                                        getInstanceName()).resolve(portStr);
                            port = Integer.parseInt(resolvedPort);
                            return new HostAndPort(serverName, port);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
        return null;
    }
    
    /**
     * @param request DeploymentRequest
     * @param env instance environment
     */
    private void setDeployDirOwner(DeploymentRequest request,
    InstanceEnvironment env) {
        //Need an alternative. Ramakanth. 04/23/2003
        /*
        try {
            String instanceUser = env.getInstanceUser();
            File deployDir = request.getDeployedDirectory();
            chownDir(deployDir, instanceUser);
            File stubsDir = request.getStubsDirectory();
            chownDir(stubsDir, instanceUser);
            File jspDir = request.getJSPDirectory();
            chownDir(jspDir, instanceUser);
        } catch (Throwable t) {
            sLogger.log(Level.WARNING, "mbean.deploy_chown_failed",
            t.getMessage());
            sLogger.log(Level.FINE, "general.unexpected_exception", t);
        }
         */
    }
    
    
    private ObjectName[] getAllRegisteredComponentsOfType(DeployableObjectType type) {
        String operationName = null;
        ObjectName[] ONArr = null;
        try{
            if(type.isAPP())
                operationName = "getJ2eeApplication";
            else if(type.isEJB())
                operationName = "getEjbModule";
            else if(type.isWEB())
                operationName = "getWebModule";
            else if(type.isCONN())
                operationName = "getConnectorModule";
            else if(type.isCAR())
                operationName = "getAppclientModule";
            
            ONArr = (ObjectName[])invoke(operationName,emptyParams,emptySignature);
        }catch(Exception e){
            sLogger.log(Level.FINE, e.getMessage());
            ONArr = new ObjectName[]{};
        }
        return ONArr;
    }
    
    private ArrayList getModules(String target, DeployableObjectType type, Boolean state, boolean excludeSystemApps) throws Exception
    {
        ArrayList list = new ArrayList();
        String operationName = "getApplicationRef";
        try{
            ObjectName[] appONArr = getAllRegisteredComponentsOfType(type);
            ObjectName serverON = new ObjectName(getDomainName()+":type=server,name="
            +target+",category=config");
            MBeanServer mbs = MBeanServerFactory.getMBeanServer();
            ObjectName[] apprefONArr = (ObjectName[])mbs.invoke(serverON, operationName,
                emptyParams, emptySignature);
            for(int i =0 ; i< apprefONArr.length ; i++) {
                String refName = (String)mbs.getAttribute(apprefONArr[i], "ref");

                     String refenabled = (String)mbs.getAttribute(apprefONArr[i], "enabled");
                     //if refEnabled only determines a stop/start then neednot do this below.
                     for(int j =0 ; j < appONArr.length ; j++) {
                         String name = (String)mbs.getAttribute(appONArr[j], "name");
                         if(name.equals(refName)) {
                             /*
                              *Use an assumed value of "user" for app client modules, because they do not
                              *have an explicit object type attribute.
                              */
                             String objectType = (type.equals(DeployableObjectType.CAR) 
                                ? Constants.USER 
                                : (String)mbs.getAttribute(appONArr[j], OBJECT_TYPE));
                             if (excludeSystemApps
                                     && (objectType != null)
                                     && (objectType.startsWith(Constants.SYSTEM_PREFIX))) {
                                 break;
                             }
                             if(state == null) {
                                 list.add(refName);
                             }else {
                                 /*
                                  *App client modules do not support the "enabled" attribute, so assume any
                                  *app client module is enabled.  For other types, ask the MBean if the module
                                  *is enabled or not.
                                  */
                                 String enabled = (type.equals(DeployableObjectType.CAR) 
                                    ? "true" 
                                    : (String)mbs.getAttribute(appONArr[j], "enabled"));
                                 if(state.equals(Boolean.TRUE)) {
                                     if(enabled.equalsIgnoreCase("true") && refenabled.equalsIgnoreCase("true"))
                                         list.add(refName);
                                 }
                                 else if(state.equals(Boolean.FALSE)) {
                                     if(enabled.equalsIgnoreCase("false") || refenabled.equalsIgnoreCase("false"))
                                         list.add(refName);
                                 }
                             }
                         }
                     
                }
                      
                }                
        }catch(Exception e) {            
            sLogger.log(Level.FINE, e.getMessage());        
            throw e;
        }
        return list;       
    }
    
    private ObjectName constructComponentObjectName(String name, DeployableObjectType type){
        ObjectName ON = null;
        try{
            if(type.isAPP())
                ON = new ObjectName(getDomainName()+":type=j2ee-application,name="+name+",category=config");
            else if(type.isEJB())
                ON = new ObjectName(getDomainName()+":type=ejb-module,name="+name+",category=config");
            else if(type.isAPP())
                ON = new ObjectName(getDomainName()+":type=web-module,name="+name+",category=config");
            else if(type.isAPP())
                ON = new ObjectName(getDomainName()+":type=connector-module,name="+name+",category=config");
            else if(type.isAPP())
                ON = new ObjectName(getDomainName()+":type=appclient-module,name="+name+",category=config");
        }catch(Exception e){
        }
        return ON;
    }

    /**
     *
     */
    private ObjectName getRegisteredComponentObjectName(String name, DeployableObjectType type) {
        ObjectName ret = null;
        try {    
            String operationName = null;
            if(type.equals(DeployableObjectType.APP)) {
                operationName = "getJ2eeApplicationByName";
            }   
            else if(type.equals(DeployableObjectType.EJB)) {
                operationName = "getEjbModuleByName";
            }   
            else if(type.equals(DeployableObjectType.WEB)) {
                operationName = "getWebModuleByName";
            }   
            else if(type.equals(DeployableObjectType.CONN)) {
                operationName = "getConnectorModuleByName"; 
            }   
            else if(type.equals(DeployableObjectType.CAR)) {
                operationName = "getAppclientModuleByName";
            }   
            else if(type.equals(DeployableObjectType.LCM)) {
                operationName = "getLifecycleModuleByName";
            }   
            else if(type.equals(DeployableObjectType.CMB)) {
                operationName = "getMbeanByName";
            }   
            Object[] params = new Object[]{name};
            String[] signature = new String[]{"java.lang.String"};
            ret = (ObjectName)invoke(operationName, params, signature);
            
        }catch (Exception e) {
//            sLogger.log(Level.WARNING, "appexists failed", e); //noi18N
        }   
        return ret;
    }   

    private ObjectName getRegisteredComponentObjectName(String name)
                                                  throws DeploymentException {
        ObjectName ret = null;

       for(int i=0 ; i < deployableObjectTypes.length ; i++)
       {
           ret = getRegisteredComponentObjectName(name,deployableObjectTypes[i]);
           if(ret != null)
               return ret;
       }
       throw new DeploymentException("Component not registered");
    }

    private ObjectName getRegisteredAppRefObjectName(String name, String targetName)
        throws DeploymentException 
    {
        try{
            final com.sun.enterprise.admin.target.Target target = 
                com.sun.enterprise.admin.target.TargetBuilder.INSTANCE.
                createTarget(VALID_LIST_TYPES, targetName, getConfigContext());
            assert target != null;

            ObjectName targetON = null;
            if (target.getType() == 
                    com.sun.enterprise.admin.target.TargetType.CLUSTER) {
                targetON = super.getClusterObjectName(targetName);
            } else if (target.getType() == 
                    com.sun.enterprise.admin.target.TargetType.DOMAIN) {
                //Domain will not have app refs.
                return null;
            } else {
                targetON = super.getServerObjectName(targetName);
            }
            assert targetON != null;

            MBeanServer mbs = MBeanServerFactory.getMBeanServer();
            ObjectName[] apprefONArr = (ObjectName[])mbs.invoke(targetON, 
               "getApplicationRef", emptyParams, emptySignature);
            for(int i =0; i< apprefONArr.length;i++)
            {
                String ref = (String)mbs.getAttribute(apprefONArr[i], "ref");
                if(name.equals(ref))
                    return apprefONArr[i];
            }
       }catch(Exception e){
          throw new DeploymentException(e.getMessage()); 
       }
       throw new DeploymentException("Component not registered");
    }


    // Code  for retrieving list of components within an application
    // or module.

    /**
     * Returns the list of sub-components for a given module
     *
     * This method needs to be updated for SE/EE to include target
     *
     * @return list of sub-components for a given module
     * @throws ServerInstanceException
     */
    public String[] getModuleComponents(String standAloneModuleName)
                throws ServerInstanceException {

        String [] modComponents = new String[0];

        if ((standAloneModuleName == null) || (standAloneModuleName.length() < 1)) {
                throw new ServerInstanceException(
			localStrings.getString("admin.mbeans.ssmb.invalid_app_or_module_name", ""));
        }

        try {
            sLogger.log(Level.FINE, 
                "ApplicationsConfigMBean.getModuleComponents for " + standAloneModuleName);

            // Get module type which is required for getting module descriptor.
            // This process validates the existence of given module.

            String j2eeType = getJ2eeType(standAloneModuleName);

            if (j2eeType == null) {
                throw new ServerInstanceException(
			localStrings.getString("admin.mbeans.ssmb.invalid_app_or_module_name", 
			standAloneModuleName));
            }

            // Get the module descriptor and
            // list of component names

            if (j2eeType.equals("EJBModule")) {
                BundleDescriptor bd = getDescrForStandAloneEjbModule(standAloneModuleName);
                modComponents = getValidatedObjectNames(
			getEjbModuleComponents((EjbBundleDescriptor)bd));
                return modComponents;
            } else if (j2eeType.equals("WebModule")) {
                BundleDescriptor bd = getDescrForStandAloneWebModule(standAloneModuleName);
                modComponents = getValidatedObjectNames(
                	getWebModuleComponents((WebBundleDescriptor)bd));
                return modComponents;
            } else if (j2eeType.equals("ResourceAdapterModule")) {
                BundleDescriptor bd = getDescrForStandAloneRarModule(standAloneModuleName);
                modComponents = getValidatedObjectNames(
                	getRarModuleComponents((ConnectorDescriptor)bd));
                return modComponents;
            } else if (j2eeType.equals("AppClientModule")) {
                BundleDescriptor bd = getDescrForStandAloneCarModule(standAloneModuleName);
                modComponents = getValidatedObjectNames(getCarModuleComponents(bd));
                return modComponents;
            } else if (j2eeType.equals("J2EEApplication")) {
                Application ad = getDescrForApplication(standAloneModuleName);
                modComponents = getValidatedObjectNames(getApplicationComponents(ad));
                return modComponents;
            } else {
                throw new ServerInstanceException(
			localStrings.getString("admin.mbeans.ssmb.invalid_app_or_module_name", 
			standAloneModuleName));
            }

        } catch (Exception e) {
            sLogger.log(Level.WARNING, "ApplicationsConfigMBean.getModuleComponents failed", e);
            throw new ServerInstanceException(e.getLocalizedMessage());
        }

    }


    /**
     * Returns the list of sub-components for a given module within an application
     *
     * This method needs to be updated for SE/EE to include target
     *
     * @return list of sub-components for a given module
     * @throws ServerInstanceException
     */
    public String[] getModuleComponents(String appName, String modName) 
                throws ServerInstanceException {

        String [] modComponents = new String[0];

        if ((modName == null) || (modName.length() < 1)) {
                throw new ServerInstanceException("invalid ModuleName");
        }

        try {
            sLogger.log(Level.FINE, 
                "ApplicationsConfigMBean.getModuleComponents for application = " +
                appName + " and module = " + modName);

            // Get application descriptor

            AppsManager am = InstanceFactory.createAppsManager(getInstanceName());
	    Application appD = null;
	    try {
            appD = (Application) DeploymentUtils.getDescriptor(appName, am);
	    } catch (java.lang.NullPointerException npe) {
                throw new ServerInstanceException(
			localStrings.getString("admin.mbeans.ssmb.invalid_appname", appName));
	    }

            // Get the bundle descriptor for the given module
            // and determine its' type

            BundleDescriptor bd = null;
            ModuleType modType = null;
            java.util.Set bds = appD.getBundleDescriptors();
            for(Iterator it=bds.iterator(); it.hasNext(); ) {
                bd = (BundleDescriptor)it.next();
                if ((bd.getModuleDescriptor().getArchiveUri()).equals(modName) ||
                     bd.getModuleID().equals(modName) ||
		     bd.getName().equals(modName)) {
                        modType = bd.getModuleType();
			break;
                }
            }

            // invoke approprite module to list components

            if (modType == ModuleType.EJB) {
                modComponents = getValidatedObjectNames(
                	getEjbModuleComponents((EjbBundleDescriptor)bd));
                return modComponents;
            } else if (modType == ModuleType.WAR) {
                modComponents = getValidatedObjectNames(
                	getWebModuleComponents((WebBundleDescriptor)bd));
                return modComponents;
            } else if (modType == ModuleType.RAR) {
                modComponents = getValidatedObjectNames(
                	getRarModuleComponents((ConnectorDescriptor)bd));
                return modComponents;
            } else if (modType == ModuleType.CAR) {
                modComponents = getValidatedObjectNames(
                	getCarModuleComponents(bd));
                return modComponents;
            } else {
                    throw new ServerInstanceException("invalid module or application name");
            }

        } catch (Exception e) {
            sLogger.log(Level.WARNING, "ApplicationsConfigMBean.getModuleComponents failed", e);
            throw new ServerInstanceException(e.getLocalizedMessage());
        }

    }

    /**
     * Returns j2ee type for the given module.
     * @return j2ee type for the given module
     * @throws ServerInstanceException
     */
    String getJ2eeType(String dName)
                throws ServerInstanceException {

        // iterate through each of j2ee types

        String j2eeType = null;

        try {

            // Application
            Applications appsConfigBean =
                    (Applications) ConfigBeansFactory.getConfigBeanByXPath(
                    getConfigContext(), ServerXPathHelper.XPATH_APPLICATIONS);

            // J2EEApplication
            J2eeApplication[] j2eeApps = appsConfigBean.getJ2eeApplication();
            if (j2eeApps != null) {
                for(int i=0; i<j2eeApps.length; i++) {
                    if ((j2eeApps[i].getName()).equals(dName)) {
                        return "J2EEApplication";
                    }
                }
            }        

            // EJBModule
            EjbModule[] eModules = appsConfigBean.getEjbModule();
            if (eModules != null) {
                for(int i=0; i<eModules.length; i++) {
                    if ((eModules[i].getName()).equals(dName)) {
                        return "EJBModule";
                    }
                }
            }

            // WebModule
            WebModule[] wModules = appsConfigBean.getWebModule();
            if (wModules != null) {
                for(int i=0; i<wModules.length; i++) {
                    if ((wModules[i].getName()).equals(dName)) {
                        return "WebModule";
                    }
                }
            }

            // ResourceAdapterModule
            ConnectorModule[] connectorConfigBeans = appsConfigBean.getConnectorModule();
            if (connectorConfigBeans != null) {
                for(int i = 0; i < connectorConfigBeans.length; i++) {
                    if ((connectorConfigBeans[i].getName()).equals(dName)) {
                        return "ResourceAdapterModule";
                    }
                }
            }

        } catch (Exception e) {
                throw new ServerInstanceException(e.getLocalizedMessage());
        }
        return j2eeType;
    }

    /**
     * Returns ejb module components
     * @return String [] ejb module components
     * @throws ServerInstanceException
     */
    BundleDescriptor getDescrForStandAloneEjbModule (String ejbModuleName) 
                throws ServerInstanceException {
        try {
            EjbModulesManager ejbModMgr = 
                InstanceFactory.createEjbModuleManager(getInstanceName());
            return (BundleDescriptor) 
                DeploymentUtils.getDescriptor(ejbModuleName, ejbModMgr);
        } catch (Exception e) {
            throw new ServerInstanceException(e.getLocalizedMessage());
        }

    }

    String [] getEjbModuleComponents(EjbBundleDescriptor bd)
                throws ServerInstanceException {

        String [] sArr = null;
        
        try {
                java.util.Set ejbs = bd.getEjbs();
		ManagementObjectManager mom = Switch.getSwitch().getManagementObjectManager();
                String moduleName = mom.getModuleName(bd);
                String applicationName = mom.getApplicationName(bd);
                EjbDescriptor ed = null;
                sArr = new String[ejbs.size()];
                int i=0;
                String j2eeType = null;

                for(Iterator it=ejbs.iterator(); it.hasNext(); ) {
                        ed = (EjbDescriptor) it.next();
                        j2eeType = mom.getJ2eeTypeForEjb(ed);
                        sArr[i] = ("j2eeType=" + j2eeType + "," +
                                   "name=" + ed.getName() + "," + 
                                   "EJBModule=" + moduleName + "," + 
                                   "J2EEApplication=" + applicationName);
                        i++;
                }
                
        } catch (Exception e) {
                throw new ServerInstanceException(e.getLocalizedMessage());
        }

        return sArr;
    }

    /**
     * Returns web module components
     * @return String [] web module components
     * @throws ServerInstanceException
     */
    BundleDescriptor getDescrForStandAloneWebModule (String moduleName) 
                throws ServerInstanceException {
        try {
            WebModulesManager webModMgr = 
                InstanceFactory.createWebModuleManager(getInstanceName());
            return (BundleDescriptor)
                DeploymentUtils.getDescriptor(moduleName, webModMgr);
        } catch (Exception e) {
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
    }

    String [] getWebModuleComponents(WebBundleDescriptor bd)
                throws ServerInstanceException {

        String [] sArr = null;
        
        try {
                java.util.Set webDescriptors = bd.getWebDescriptors(); 
		ManagementObjectManager mom = Switch.getSwitch().getManagementObjectManager();
                String moduleName = mom.getModuleName(bd);
                String applicationName = mom.getApplicationName(bd);
                WebComponentDescriptor wd = null;
                sArr = new String[webDescriptors.size()];
                int i=0;
                String j2eeType = null;
		String servletName = null;
		String cName = null;
		String dName = null;
		String sName = null;

                for(Iterator it=webDescriptors.iterator(); it.hasNext(); ) {
                        wd = (WebComponentDescriptor) it.next();

			dName = wd.getDisplayName();
			sName = wd.getName();
			cName = wd.getCanonicalName();

			if ((dName != null) && (dName.length() > 0)) {
				servletName = dName;
			} else if ((sName != null) && (sName.length() > 0)) {
				servletName = sName;
			} else if ((cName != null) && (cName.length() > 0)) {
				servletName = cName;
			} else {
				servletName = "";
			}

                        j2eeType = "Servlet";
                        sArr[i] = ("j2eeType=" + j2eeType + "," +
                                   "name=" + servletName + "," + 
                                   "WebModule=" + moduleName + "," + 
                                   "J2EEApplication=" + applicationName);
                        i++;
                }
                
        } catch (Exception e) {
                throw new ServerInstanceException(e.getLocalizedMessage());
        }

        return sArr;
    }


    BundleDescriptor getDescrForStandAloneCarModule (String moduleName) 
                throws ServerInstanceException {
        try {
            AppclientModulesManager appClModMgr = 
                InstanceFactory.createAppclientModulesManager(getInstanceName());
            return (BundleDescriptor)
                DeploymentUtils.getDescriptor(moduleName, appClModMgr);
        } catch (Exception e) {
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
    }


    String [] getCarModuleComponents(BundleDescriptor bd)
                throws ServerInstanceException {

        String [] sArr = new String[1];
        
        try {
		ManagementObjectManager mom = Switch.getSwitch().getManagementObjectManager();
                String moduleName = bd.getModuleID();
                String applicationName = mom.getApplicationName(bd);
		sArr[0] =  ("j2eeType=AppClientModule," + 
			    "name=" + moduleName + "," +
			    "J2EEApplication=" + applicationName);
        } catch (Exception e) {
                throw new ServerInstanceException(e.getLocalizedMessage());
        }

        return sArr;
    }


    /**
     * Returns connector module components
     * @return String [] connector module components
     * @throws ServerInstanceException
     */
    BundleDescriptor getDescrForStandAloneRarModule (String moduleName) 
                throws ServerInstanceException {
        try {
            ConnectorModulesManager connModMgr = 
                InstanceFactory.createConnectorModulesManager(getInstanceName());
            return (BundleDescriptor)
                DeploymentUtils.getDescriptor(moduleName, connModMgr);
        } catch (Exception e) {
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
    }

    String [] getRarModuleComponents(ConnectorDescriptor bd)
                throws ServerInstanceException {

        String [] sArr = null;
        
        try {
		ManagementObjectManager mom = Switch.getSwitch().getManagementObjectManager();
                String moduleName = mom.getModuleName(bd);
                String applicationName = mom.getApplicationName(bd);
                String j2eeType = null;
                int i = 0;

                // Assign memory for inbound and outbound vars
                InboundResourceAdapter ibRA = bd.getInboundResourceAdapter();
                OutboundResourceAdapter obRA = bd.getOutboundResourceAdapter();
                int kount = 0;
                if (ibRA != null) { kount = kount + 1; }
                if (obRA != null) { kount = kount + 1; }
                if (kount > 0) {sArr = new String[kount];}

                // Inbound

                if (ibRA != null) {
                        j2eeType = "ResourceAdapter";
                        sArr[i] = ("j2eeType=" + j2eeType + "," +
                                   "name=" + ibRA.getName() + "," + 
                                   "ResourceAdapterModule=" + moduleName + "," + 
                                   "J2EEApplication=" + applicationName);
                        i++;
                }

                // OutBound

                if (obRA != null) {
                        j2eeType = "ResourceAdapter";
                        sArr[i] = ("j2eeType=" + j2eeType + "," +
                                   "name=" + obRA.getName() + "," + 
                                   "ResourceAdapterModule=" + moduleName + "," + 
                                   "J2EEApplication=" + applicationName);
                        i++;
                }

        } catch (Exception e) {
                throw new ServerInstanceException(e.getLocalizedMessage());
        }

        return sArr;
    }


    /**
     * Returns components within an application
     * @return String [] application components
     * @throws ServerInstanceException
     */
    Application getDescrForApplication (String appName) 
                throws ServerInstanceException {
        try {
		    AppsManager appsMgr = 
                InstanceFactory.createAppsManager(getInstanceName());
            return (Application) 
                DeploymentUtils.getDescriptor(appName, appsMgr);
        } catch (Exception e) {
            throw new ServerInstanceException(e.getLocalizedMessage());
        }
    }

    /**
     * Returns an array of system resource adapters name string.
     * Presently we need this seperate methode as the system resource adapters are
     * not registered in the config. this method will not be required if the system
     * resource adapters (connector modules) will have entry in domain.xml
     * @return an array of syestem connectors name string.
     * @throws ServerinstanceException
     */
    public String[] getAllSystemConnectors() throws ServerInstanceException {
        final List<String>  names   = ConnectorConstants.systemRarNames;
        
        return names.toArray( new String[ names.size() ] );
    }
    
    String [] getApplicationComponents(Application ad)
                throws ServerInstanceException {

        String [] sArr = null;
        
        try {
		ManagementObjectManager mom = Switch.getSwitch().getManagementObjectManager();
                String applicationName = ad.getRegistrationName();
                String j2eeType = null;

		java.util.Set bds = null;
		BundleDescriptor bd = null;

		// Determine number of modules and initialize the String array
                int i = 0;
		sArr = new String [ ad.getApplicationClientDescriptors().size() +
				    ad.getEjbBundleDescriptors().size() +
				    ad.getRarDescriptors().size() +
				    ad.getWebBundleDescriptors().size() ];

		// App client modules
		bds = ad.getApplicationClientDescriptors();
		for(Iterator it=bds.iterator(); it.hasNext(); ) {
			bd = (BundleDescriptor) it.next();
			sArr[i] = ("j2eeType=AppClientModule" + "," +
				   "name=" + bd.getModuleDescriptor().getArchiveUri() + "," +
				   "J2EEApplication=" + applicationName);
			i++;
		}

		// Ejb modules
		bds = ad.getEjbBundleDescriptors();
		for(Iterator it=bds.iterator(); it.hasNext(); ) {
			bd = (BundleDescriptor) it.next();
			sArr[i] = ("j2eeType=EJBModule" + "," +
				   "name=" + bd.getModuleDescriptor().getArchiveUri() + "," +
				   "J2EEApplication=" + applicationName);
			i++;
		}

		// Connector modules
		bds = ad.getRarDescriptors();
		for(Iterator it=bds.iterator(); it.hasNext(); ) {
			bd = (BundleDescriptor) it.next();
			sArr[i] = ("j2eeType=ResourceAdapterModule" + "," +
				   "name=" + bd.getModuleDescriptor().getArchiveUri() + "," +
				   "J2EEApplication=" + applicationName);
			i++;
		}

		// Web modules
		bds = ad.getWebBundleDescriptors();
		for(Iterator it=bds.iterator(); it.hasNext(); ) {
			bd = (BundleDescriptor) it.next();
			sArr[i] = ("j2eeType=WebModule" + "," +
				   "name=" + bd.getModuleDescriptor().getArchiveUri() + "," +
				   "J2EEApplication=" + applicationName);
			i++;
		}


        } catch (Exception e) {
                throw new ServerInstanceException(e.getLocalizedMessage());
        }

        return sArr;
    }

    // Method to validate and return object names

    private String [] getValidatedObjectNames(String [] strArr)
                throws ServerInstanceException {

	// Append the domain name and server name to the input string array
	// and return it.

	String [] sArr = new String[strArr.length];

	try {
		for (int i=0; i<strArr.length; i++) {
			sArr[i] = ("com.sun.appserv" + ":" +
				   strArr[i] + "," +
				   "J2EEServer=" + getInstanceName());
		}
        } catch (Exception e) {
                throw new ServerInstanceException(e.getLocalizedMessage());
        }

	return sArr;

    }

    /**
     * Determines whether the specified target is default target.
     * if CLI/GUI specifies a null, blank, "domain" target, it is
     * considered to be default target.
     * @param target target name
     * @return true if specified target is default
     */
    private boolean isDefaultTarget(String target)
    {
        if(target == null
           || target.length() == 0
           || target.equalsIgnoreCase(DEFAULT_TARGET))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Set http listener host and port in deployment request. If the server
     * is not configured properly the defaults used are localhost:8080 for
     * clear and localhost:8181 for SSL.
     */
    private void setHostAndPort(DeploymentRequest req)
            throws ServerInstanceException {
                
        String virtualServers = (String) req.getOptionalAttributes().get(ServerTags.VIRTUAL_SERVERS);
        if (virtualServers==null) {
            HostAndPort hap = getHostAndPort(false);
            if(hap != null) {
                req.setHttpHostName(getHostName(hap));
                req.setHttpPort(getPort(hap, false));
            }
            hap = getHostAndPort(true);
            if(hap != null) {
                req.setHttpsHostName(getHostName(hap));
                req.setHttpsPort(getPort(hap, true));
            }
        } else {
            StringTokenizer st = new StringTokenizer(virtualServers,",");
            if (st.hasMoreTokens()) {
                String aVirtualServer = st.nextToken();
                HostAndPort hap = getVirtualServerHostAndPort(aVirtualServer, false);
                if(hap != null) {
                    req.setHttpHostName(getHostName(hap));
                    req.setHttpPort(getPort(hap, false));
                }
                hap = getVirtualServerHostAndPort(aVirtualServer, true);
                if(hap != null) {
                    req.setHttpsHostName(getHostName(hap));
                    req.setHttpsPort(getPort(hap, true));
                }
            }
        }
    }
            
    public String getHostName(HostAndPort hap) {
        String hostName = hap.getHost();
        if (hostName == null || hostName.trim().equals("")) {
            hostName = getDefaultHostName();
        }
        return hostName;
    }

    private String getDefaultHostName() {
        String defaultHostName = "localhost";
        try {
            InetAddress host = InetAddress.getLocalHost();
            defaultHostName = host.getCanonicalHostName();
        } catch(UnknownHostException uhe) {
            sLogger.log(Level.FINEST, "mbean.get_local_host_error", uhe);
            sLogger.log(Level.INFO, "mbean.use_default_host");
        }
        return defaultHostName; 
    }

    private int getPort(HostAndPort hap, boolean securityEnabled) {
        int port = hap.getPort();
        if (port == 0) {
            port = getDefaultPort(securityEnabled);
        }
        return port;
    }

    private int getDefaultPort(boolean securityEnabled) {
        int port = 0;
        if (securityEnabled) {
            port = 8181;
        } else {
            port = 8080;
        }
        sLogger.log(Level.INFO, "mbean.use_default_port", String.valueOf(port));
        return port;
    }

    /**
     * Get value of named attribute from attributes list. If an attribute with
     * specified name does not exist, the method returns null. If there are
     * than one attributes with same name then the method returns value of
     * first matching attribute.
     *
     * @param attrs list of attributes
     * @param attrName name of the attribute
     * @return value of the specified attrName or null if the attrName is
     *     not present in specified attrs
     */
    private Object getNamedAttributeValue(AttributeList attrs,
            String attrName) {
        if (attrs == null || attrName == null) {
            return null;
        }
        Object value = null;
        Iterator iter = attrs.iterator();
        while (iter.hasNext()) {
           Attribute attr = (Attribute)iter.next();
           if (attrName.equals(attr.getName())) {
               value = attr.getValue();
               break;
           }
        }
        return value;
    }

    /**
     * This method checks if the objectName specified is of
     * a system application/module
     * @param componentON objectName of the application
     * @param action deploy/redeploy/undeploy/enable/disable
     * @throws MBeanConfigException if application/module is 
     * of type system
     */
    private void validate(ObjectName componentON, String action)
        throws MBeanConfigException {
       
        if(componentON == null)
            return;

	//
	// fix for bug#s 4939623 and 4939625
	// if the type is appclient-module return
	// since DTD doesn't have object-type attribute for appclient-module
	//

	String componentType = componentON.getKeyProperty(ServerTags.TYPE);
	if ((componentType != null) && (componentType.length() > 0)) {
		if (componentType.equals(ServerTags.APPCLIENT_MODULE)) {
			return;
		}
	}

        boolean allowSystemAppModification = 
            (Boolean.valueOf(System.getProperty(
                Constants.ALLOW_SYSAPP_DEPLOYMENT, "false")).booleanValue());
        
        if(allowSystemAppModification)
            return;
            
        String objectType = null;
        try{
            
            MBeanServer mbs = MBeanServerFactory.getMBeanServer();
            objectType = (String)mbs.getAttribute(componentON, OBJECT_TYPE);
            
        }catch(Exception e){
            String msg = localStrings.getString(
                "admin.mbeans.acmb.exception_object_type");
            sLogger.log(Level.FINE, msg);
            throw new MBeanConfigException(e.getMessage());
        }
        
        if(objectType!= null && objectType.startsWith(Constants.SYSTEM_PREFIX))
        {   
            String msg = localStrings.getString(
                "admin.mbeans.acmb.component_is_system",new Object[]{action});
            throw new MBeanConfigException(msg);
        }
    
    }

    public void associate(String appName, String target) 
    throws MBeanException, MBeanConfigException {
    }    
    
    public void disassociate(String appName, String target) 
    throws MBeanException, MBeanConfigException {
    }    

    private DeploymentTargetFactory getTargetFactory() {
        return DeploymentTargetFactory.getDeploymentTargetFactory();
    }
    
    // Begin EE: 4946914 - Cluster deployment support

    protected static final String ADMIN_VS = com.sun.enterprise.web.VirtualServer.ADMIN_VS;

     // Attribue names for http listeners
    protected static final String PORT = "port";
    protected static final String DEF_VS = "default-virtual-server";
    protected static final String SERVER_NAME = "server-name";
    protected static final String REDIRECT_PORT = "redirect-port";
    protected static final String SEC_ENABLED = "security-enabled";
    protected static final String LISTENER_ENABLED = "enabled";
    protected static final String OBJECT_TYPE = "object-type";
        
    // Attribute names for virtual server
    protected static final String HOSTS = "hosts";
    protected static final String HTTP_LISTENERS = "http_listeners";
    protected static final String DEFAULT_WEB_MODULE = "default_web_module";
    protected static final String STATE = "state";
    protected static final String ID = "id";
    
    //actions
    protected static final String DEPLOY_ACTION = "deploy";
    protected static final String REDEPLOY_ACTION = "redeploy";
    protected static final String UNDEPLOY_ACTION = "undeploy";
    protected static final String ENABLE_ACTION = "enable";
    protected static final String DISABLE_ACTION = "disable";


    protected static String[] httpListenerAttrNames = {LISTENER_ENABLED,
            DEF_VS, SERVER_NAME, REDIRECT_PORT, PORT, SEC_ENABLED, ID };
            
    protected static String[] vsAttrNames = {HOSTS, HTTP_LISTENERS, 
	    DEFAULT_WEB_MODULE, STATE, ID};            
            
    // End EE: 4946914 - Cluster deployment support

    private static final class ObjectNameAppRefComparator
    {
        private ObjectNameAppRefComparator()
        {
        }

        private static int compare(ObjectName on, ApplicationRef ref)
        {
            String k1 = on.getKeyProperty(NAME);
            String k2 = ref.getRef();
            return k1.compareTo(k2);
        }

        public static ObjectName[] intersect(final ObjectName[]     oa, 
                                             final ApplicationRef[] ra)
        {
            if ((null == oa) || (0 == oa.length) || 
                (null == ra) || (0 == ra.length))
            {
                return new ObjectName[0];
            }
            final ArrayList al = new ArrayList();
            for (int i = 0; i < ra.length; i++)
            {
                for (int j = 0; j < oa.length; j++)
                {
                    if (0 == compare(oa[j], ra[i]))
                    {
                        al.add(oa[j]);
                        break;
                    }
                }
            }
            return (ObjectName[])al.toArray(new ObjectName[al.size()]);
        }
    }


    /**
     * Returns deployment descriptor locations for the given
     * combination of standAloneModuleName and subComponentName.
     *
     * For a j2ee_application of type ear, both values for
     * standAloneModuleName and subComponentName must be supplied.
     *
     * In case of  stand alone module, the subComponentName will be null.
     *
     * @return String [] list of deployment descriptor locations
     *         null if no descriptors found
     */

     public String [] getDeploymentDescriptorLocations (
	String standAloneModuleName, String subComponentName) 
	throws ServerInstanceException {

        sLogger.log(Level.FINE, "getDeploymentDescriptorLocations - begin" +
		" standAloneModuleName = " + standAloneModuleName + 
		" subComponentName = " + subComponentName);

	// local variables
	J2EEModule j2eeModule = null;

	// initialization
	if (subComponentName != null) {
		j2eeModule = new J2EEModule(standAloneModuleName, subComponentName);
	} else {
		j2eeModule = new J2EEModule(standAloneModuleName);
	}

	// get j2ee module type
	ModuleType moduleType = j2eeModule.getModuleType();
	if (moduleType == null) {
		throw new ServerInstanceException(
		localStrings.getString("admin.mbeans.acmb.invalidModuleType"));
	}
        sLogger.log(Level.FINE, "getDeploymentDescriptorLocations - moduleType" + moduleType);

	// get dd location
	String ddLocation = j2eeModule.getDeploymentDescriptorsLocation();
	if (ddLocation == null) {
		throw new ServerInstanceException(
		localStrings.getString("admin.mbeans.acmb.invalidDeplDescrLoc"));
	}
        sLogger.log(Level.FINE, "getDeploymentDescriptorLocations - ddLocation" + ddLocation);

	// get the list of descriptor locations for the above j2ee module type
	String [] ddList = DescriptorList.getDescriptorsList(moduleType);
	if ((ddList == null) || (ddList.length < 1)) {
		return null;
	}
	ArrayList arrL = new ArrayList();
	String fileLocation = null;
	for (int i=0; i<ddList.length; i++) {
		// if the descriptor exists then add to the list
		fileLocation = ddLocation + File.separator + ddList[i];
		try {
			File file = new File(fileLocation);
			if (file.exists()) {
				arrL.add(fileLocation);
			}
		} catch (Exception e) {
        		sLogger.log(Level.WARNING, 
			"getDeploymentDescriptorLocations - descriptor does not exist for " +
			fileLocation);
			// continue with next file
		}
	}

	// return dd locations array
	if (arrL.size() > 0) {
		String [] strArr = new String[arrL.size()];
		for (int j=0; j<arrL.size(); j++) {
			strArr[j] = (String) arrL.get(j);
        		sLogger.log(Level.FINE, 
				"getDeploymentDescriptorLocations: " +
				"next dd location = " + strArr[j]);
		}
		return strArr;
	}
	return null;
     }


     /**
      * Returns xml deployment descriptor as string
      * @param String deploymentDescriptorLocation for whcih the descriptor 
      *               needs to be returned.
      */
      public String getDeploymentDescriptor(
		String deploymentDescriptorLocation) 
		throws ServerInstanceException {

        	sLogger.log(Level.FINE, "getDeploymentDescriptor - begin" +
		" deploymentDescriptorLocation = " + deploymentDescriptorLocation);

		if (deploymentDescriptorLocation == null) return null;

		J2EEModule j2eeModule = new J2EEModule();

		String str = j2eeModule.getStringForDDxml(deploymentDescriptorLocation);

		if (str != null) {
        	    sLogger.log(Level.FINE, "getDeploymentDescriptor: for " +
			deploymentDescriptorLocation + " = " +
			str);
		}

		return str;
      }

    public String createMBean(String target, String className) throws CustomMBeanException {
        return ( cmo.createMBean(target, className) );
    }

    public String createMBean(String target, Map<String, String> params) throws CustomMBeanException {
        Map<String,String> paramsCopy = new HashMap<String,String>(params);
		return ( cmo.createMBean(target, paramsCopy) );
    }

    public String createMBean(String target, Map<String, String> params, Map<String, String> attributes) throws CustomMBeanException {
        Map<String,String> paramsCopy = new HashMap<String,String>(params);
        return ( cmo.createMBean(target, paramsCopy, attributes) );
    }

    public void createMBeanRef(String target, String ref) throws CustomMBeanException {
        cmo.createMBeanRef(target, ref);
    }

    public String deleteMBean(String target, String name) throws CustomMBeanException {
        return ( cmo.deleteMBean(target, name) );
    }

    public void deleteMBeanRef(String target, String ref) throws CustomMBeanException {
        cmo.deleteMBeanRef(target, ref);
    }

    public boolean existsMBean(String target, String name) throws CustomMBeanException {
        return ( cmcq.existsMBean(target, name) );
    }

    public boolean isMBeanEnabled(String target, String name) throws CustomMBeanException {
        return ( cmcq.isMBeanEnabled(target, name) );
    }

    public List<ObjectName> listMBeanConfigObjectNames(String target) throws CustomMBeanException {
        return ( cmcq.listMBeanConfigObjectNames(target) );
    }

    public List<ObjectName> listMBeanConfigObjectNames(String target, int type, boolean state) throws CustomMBeanException {
        return (cmcq.listMBeanConfigObjectNames(target, type, state) );
    }

    public List<String> listMBeanNames(String target) throws CustomMBeanException {
        return ( cmcq.listMBeanNames(target) );
    }

    /**
     * Return the MBeanInfo of a given Custom MBean.  
     * The MBean must be loadable from the standard App Server location.
     * The code does this:
     * <ul>
     * <li>Register the MBean in the MBeanServer
     * <li>Fetch and save the MBeanInfo
     * <li>Unregister the MBean
     * </ul>
     * Note that if the MBean can't be deployed successfully then this method won't work.
     * @param classname 
     * @throws com.sun.enterprise.admin.mbeans.custom.CustomMBeanException 
     * @return The MBeanInfo object
     */
    public MBeanInfo getMBeanInfo(String classname) throws CustomMBeanException {
        return ( cmo.getMBeanInfo(classname) );
    }
}
