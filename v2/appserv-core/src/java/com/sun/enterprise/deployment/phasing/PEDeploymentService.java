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
 * PEDeploymentService.java
 *
 * Created on April 25, 2003, 11:10 AM
 * @author  sandhyae
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/phasing/PEDeploymentService.java,v $
 *
 */

package com.sun.enterprise.deployment.phasing;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.Iterator;
import java.util.Properties;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.InstanceNotFoundException;

import com.sun.enterprise.admin.common.exception.ServerInstanceException;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.event.ApplicationDeployEvent;
import com.sun.enterprise.admin.event.BaseDeployEvent;
import com.sun.enterprise.admin.event.ModuleDeployEvent;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.util.HostAndPort;
import com.sun.enterprise.appverification.factory.AppVerification;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.backend.ClientJarMakerRegistry;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.backend.DeploymentCommand;
import com.sun.enterprise.deployment.backend.DeploymentLogger;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.backend.DeploymentRequestRegistry;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.backend.IASDeploymentException;
import com.sun.enterprise.deployment.deploy.shared.InputJarArchive;
import com.sun.enterprise.deployment.Descriptor;
import com.sun.enterprise.deployment.interfaces.DeploymentImplConstants;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.management.deploy.DeploymentCallback;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;
import javax.security.auth.Subject;

/**
 * Manages the phases and maps deployment operations to deployment phases
 * @author  deployment dev team
 */
public class PEDeploymentService extends DeploymentService {
    
    /** Deployment Logger object for this class */
    public static final Logger sLogger = DeploymentLogger.get();
    
    /** Deployment auditing logger */
    private static final Logger auditLogger = LogDomains.getLogger(LogDomains.DPLAUDIT_LOGGER);
    
    /** Auditing resource bundle for message look-up */
    private static final ResourceBundle auditBundle = auditLogger.getResourceBundle();
    
    /** resource bundle */
    private static StringManager localStrings =
          StringManager.getManager( PEDeploymentService.class );
    
    /** context object that is used to share context with the phases */
    protected DeploymentContext deploymentContext = null;
    
    /** phase list for deploy to domain operation */
    private List deployToDomainPhaseList = null;
    
    /** phase list for undeploy from domain operation */
    private List undeployFromDomainPhaseList = null;
    
    /** phase list for deploy operation */
    private List deployPhaseList = null;
    
    /** phase list for undeploy operation */
    private List undeployPhaseList = null;
    
    /** phase list for associate operation */
    private List associatePhaseList = null;
    
    /** phase list for disassociate operation */
    private List disassociatePhaseList = null;
    
    private List stopPhaseList = null;
    
    private List startPhaseList = null;

    static final String DISASSOCIATE_ACTION = localStrings.getString(
                "enterprise.deployment.phasing.action.disassociate");
    static final String REDEPLOY_ACTION = localStrings.getString(
                "enterprise.deployment.phasing.action.redeploy");
    static final String STOP_ACTION = localStrings.getString(
                "enterprise.deployment.phasing.action.stop");
    static final String UNDEPLOY_ACTION = localStrings.getString(
                "enterprise.deployment.phasing.action.undeploy");

    /** 
     * Creates a new instance of PEDeploymentService 
     * @param configContext config context object
     */
    public PEDeploymentService(ConfigContext configContext) 
    {
        deploymentContext = new DeploymentContext();
        deploymentContext.setConfigContext(configContext);
        initializePhases();
    }

    /**
     * 
     */
    protected List getDeployPhaseListForTarget(DeploymentRequest req) {

        if (deployPhaseList != null) {
            return deployPhaseList;
        }

        //XXX FIXME.  Need to verify the type of the target.
        J2EECPhase j2eec = new J2EECPhase(deploymentContext);
        AssociationPhase associate = new AssociationPhase(deploymentContext);
        ResourceAdapterStartPhase raStart = 
            new ResourceAdapterStartPhase(deploymentContext);
        ApplicationStartPhase appStart = 
            new ApplicationStartPhase(deploymentContext);
        PreResCreationPhase preResCreation = 
            new PreResCreationPhase(deploymentContext);
        PostResCreationPhase postResCreation = 
            new PostResCreationPhase(deploymentContext);

        //(re)deploy phaseList
        deployPhaseList = new ArrayList();
        deployPhaseList.add(j2eec);
        deployPhaseList.add(associate);
        deployPhaseList.add(preResCreation);
        deployPhaseList.add(raStart);
        deployPhaseList.add(postResCreation);
        deployPhaseList.add(appStart);
        return deployPhaseList; 
    }
    
    /**
     * Initializes phaseList corresponding to deploy operations. Each phase is 
     * initialized with a deploymentContext object. Deployment operations that
     * are intialized are deploy/undeploy/associate/disassociate.
     */
    private void initializePhases() 
    {
        J2EECPhase j2eec = new J2EECPhase(deploymentContext);
        AssociationPhase associate = new AssociationPhase(deploymentContext);
        DisassociationPhase disassociate = new DisassociationPhase(deploymentContext);
        UndeployFromDomainPhase undeploy = new UndeployFromDomainPhase(deploymentContext);
        ResourceAdapterStartPhase raStart = 
            new ResourceAdapterStartPhase(deploymentContext);
        ApplicationStartPhase appStart = 
            new ApplicationStartPhase(deploymentContext);
        PreResCreationPhase preResCreation = 
            new PreResCreationPhase(deploymentContext);
        PostResCreationPhase postResCreation = 
            new PostResCreationPhase(deploymentContext);

        ResourceAdapterStopPhase raStop =
            new ResourceAdapterStopPhase(deploymentContext);
        ApplicationStopPhase appStop =
            new ApplicationStopPhase(deploymentContext);
        PreResDeletionPhase preResDeletion = 
            new PreResDeletionPhase(deploymentContext);
        PostResDeletionPhase postResDeletion = 
            new PostResDeletionPhase(deploymentContext);

        //(re)deploy to domain phaseList
        deployToDomainPhaseList = new ArrayList();
        //for special case (re)deploy to domain
        deployToDomainPhaseList.add(preResDeletion);
        deployToDomainPhaseList.add(postResDeletion);
        deployToDomainPhaseList.add(j2eec);
        deployToDomainPhaseList.add(preResCreation);
        deployToDomainPhaseList.add(postResCreation);
        
        //associate phaseList
        associatePhaseList = new ArrayList();
        associatePhaseList.add(associate);
        
        //disassociate phaseList
        disassociatePhaseList = new ArrayList();
        disassociatePhaseList.add(disassociate);
        
        //undeploy phaseList
        undeployPhaseList = new ArrayList();        
        undeployPhaseList.add(appStop);
        undeployPhaseList.add(preResDeletion);
        undeployPhaseList.add(raStop);
        undeployPhaseList.add(postResDeletion);
        undeployPhaseList.add(disassociate);
        undeployPhaseList.add(undeploy);
        
        //undeploy from domain phaseList
        undeployFromDomainPhaseList = new ArrayList();
        undeployFromDomainPhaseList.add(preResDeletion);
        undeployFromDomainPhaseList.add(postResDeletion);
        undeployFromDomainPhaseList.add(undeploy);

        startPhaseList = new ArrayList();
        startPhaseList.add(preResCreation);
        startPhaseList.add(raStart);
        startPhaseList.add(postResCreation);
        startPhaseList.add(appStart);
        
        stopPhaseList = new ArrayList();
        stopPhaseList.add(appStop);
        stopPhaseList.add(preResDeletion);
        stopPhaseList.add(raStop);
        stopPhaseList.add(postResDeletion);
    }
    

    private DeploymentStatus deploy(DeploymentRequest req, AuditInfo auditInfo) throws IASDeploymentException {
        // deploy to instance 
        DeploymentStatus result = null;
        if (req.getTarget() != null && 
            !req.getTarget().getName().equals("domain")) {
            result = executePhases(req, getDeployPhaseListForTarget(req));	
        // deploy to domain
        } else {
            result = executePhases(req, deployToDomainPhaseList);
        }
        if (auditInfo != null) {
            auditInfo.reportEnd(result.getStatus());
        }
        return result;
    }
    
    /**
     * This method deploys application to the DAS. Prepares the app, stores it in 
     * central repository and registers with config
     * @param req DeploymentRequest object
     */
    public DeploymentStatus deploy(DeploymentRequest req) throws IASDeploymentException
    {
        return deploy(req, createAuditInfoIfOn(req, AuditInfo.Operation.deploy));
    }
    
    /**
     * This method undeploys application from DAS. Removes the application from 
     * central repository and unregisters the application from config
     * @param req DeploymentRequest object
     */   
    public DeploymentStatus undeploy(DeploymentRequest req) 
        throws IASDeploymentException {
        return undeploy(req, createAuditInfoIfOn(req, AuditInfo.Operation.undeploy));
    }
    
    /**
     * This method undeploys application from DAS. Removes the application from 
     * central repository and unregisters the application from config, with
     * auditing if turned on.
     * @param req DeploymentRequest object
     * @param auditInfo the AuditInfo object, if any, to be used for auditing this operation
     */   
    private DeploymentStatus undeploy(DeploymentRequest req, AuditInfo auditInfo) throws IASDeploymentException
    {
        DeploymentStatus result = null;
        //Re-record instrospect/instrument/verifier data if
        //any of the application is un-deployed
        if (AppVerification.doInstrument()) {
            AppVerification.getInstrumentLogger().handleChangeInDeployment();
        }

        String moduleID = req.getName();
        DeployableObjectType type = req.getType();
        DeploymentServiceUtils.validate(moduleID, type, UNDEPLOY_ACTION, req);

        // undeploy from DAS
        if (req.getTarget() != null) {
            result = executePhases(req, undeployPhaseList);	
        // undeploy from domain
         } else {
            result = executePhases(req, undeployFromDomainPhaseList);
        }
        if (auditInfo != null) {
            auditInfo.reportEnd(result.getStatus());
        }
        return result;
    }
    

    /**
     * Associates an application to a target.
     * @param req the DeploymentRequest
     * @param auditInfo the auditing information object that will audit this operation; null if no auditing desired
     * @return DeploymentStatus reporting the outcome of the operation
     */
    private DeploymentStatus associate(DeploymentRequest req, AuditInfo auditInfo) {
        DeploymentStatus result = executePhases(req, associatePhaseList);
        if (auditInfo != null) {
            auditInfo.reportEnd(result.getStatus());
        }
        return result;
    }
    
    /**
     * This method is used to associate an application to a target.
     * @param req DeploymentRequest object
     */
    public DeploymentStatus associate(DeploymentRequest req)
        throws IASDeploymentException {
        return associate(req, createAuditInfoIfOn(req, AuditInfo.Operation.associate));
    }

   /**
     * This method is used to associate an application to a target.
     * It constructs the DeploymentRequest using the parameters first.
     */
    public DeploymentStatus associate(String targetName,
        boolean enabled, String virtualServers, String referenceName)
        throws IASDeploymentException {
        try {
            long startTime = System.currentTimeMillis();
            //FIXME: add validation code such as checking context root
            DeployableObjectType type = 
                DeploymentServiceUtils.getRegisteredType(referenceName);
        
            final DeploymentTarget target =
                DeploymentServiceUtils.getAndValidateDeploymentTarget(
                    targetName, referenceName, false);
            
            InstanceEnvironment env =
                ApplicationServer.getServerContext().getInstanceEnvironment();
            DeploymentRequest req = new DeploymentRequest(
                                    env,
                                    type,
                                    DeploymentCommand.DEPLOY);

            /*
             * Force enabled to true for app client modules to address temporarily
             * issue 3248.
             */
            enabled = enabled || type.isCAR();
                
            req.setName(referenceName);
            req.setStartOnDeploy(enabled);
            req.setTarget(target);

            Properties optionalAttributes = new Properties();
            if(virtualServers!=null) {
                optionalAttributes.put(ServerTags.VIRTUAL_SERVERS, 
                    virtualServers);
            }
            req.setOptionalAttributes(optionalAttributes);
            return associate(req, createAuditInfoIfOn(req, AuditInfo.Operation.associate, startTime));
        } catch(Exception e) {
            if (e instanceof IASDeploymentException) {
                throw (IASDeploymentException)e;
            }
            else {
                throw new IASDeploymentException(e);
            }
        }
    }

    /**
     * This method is used to associate an application to a target.
     * It constructs the DeploymentRequest using the parameters first.
     */
    public DeploymentStatus associate(String targetName,
        String referenceName, Map options) throws IASDeploymentException {
        try {
            long startTime = System.currentTimeMillis();
            DeployableObjectType type =
                DeploymentServiceUtils.getRegisteredType(referenceName);

            DeploymentProperties dProps = new DeploymentProperties(options);
            
            /*
             *A preexisting reference is allowed to exist if a redeployment 
             *is underway and the reference's attributes are not changing 
             *during the redeployment, so long as a
             *preceding disassociation phase has recorded this fact.
             */
            boolean enforceValidation = true;
            if (dProps.getRedeploy()) {
                /*
                 *Make sure this app ref was saved during an earlier disassociation.
                 */
                DeploymentContext.SavedApplicationRefInfo info = 
                        deploymentContext.getSavedAppRef(referenceName, targetName);
                enforceValidation = (info == null);
            }

            /*
             * Force enabled to true for app client modules to address temporarily
             * issue 3248.
             */
            if (type.isCAR()) {
                dProps.setEnable(true);
            }
            
            final DeploymentTarget target = enforceValidation ? 
                DeploymentServiceUtils.getAndValidateDeploymentTarget(
                    targetName, referenceName, false) :
                DeploymentServiceUtils.getDeploymentTarget(targetName);

            InstanceEnvironment env =
                ApplicationServer.getServerContext().getInstanceEnvironment();
            DeploymentRequest req = new DeploymentRequest(
                                    env,
                                    type,
                                    DeploymentCommand.DEPLOY);

            String virtualServers = dProps.getVirtualServers();
            boolean enabled = dProps.getEnable();

            req.setName(referenceName);
            req.setStartOnDeploy(enabled);
            req.setTarget(target);
            req.setIsRedeployInProgress(dProps.getRedeploy());
            DeploymentServiceUtils.setResourceOptionsInRequest(req, dProps);

            Properties optionalAttributes = new Properties();
            if(virtualServers!=null) {
                optionalAttributes.put(ServerTags.VIRTUAL_SERVERS,
                    virtualServers);
            }
            req.setOptionalAttributes(optionalAttributes);

            return associate(req, createAuditInfoIfOn(req, AuditInfo.Operation.associate, startTime));

        } catch(Exception e) {
            if (e instanceof IASDeploymentException) {
                throw (IASDeploymentException)e;
            }
            else {
                throw new IASDeploymentException(e);
            }
        }
    }

    private DeploymentStatus disassociate(DeploymentRequest req, AuditInfo auditInfo) 
        throws IASDeploymentException {
        String moduleID = req.getName();
        DeployableObjectType type = req.getType();
        DeploymentServiceUtils.validate(moduleID,type,DISASSOCIATE_ACTION, req);

        DeploymentStatus result = executePhases(req, disassociatePhaseList);
        if (auditInfo != null) {
            auditInfo.reportEnd(result.getStatus());
        }
        return result;
    }
    
    /**
     * This method removes references of an application on a particular target
     * @param req DeploymentRequest object
     */	 
    public DeploymentStatus disassociate(DeploymentRequest req)
        throws IASDeploymentException {
        return disassociate(req, createAuditInfoIfOn(req, AuditInfo.Operation.disassociate));
    }	
    
    /**
     * This method removes references of an application on a particular target
     * It constructs the DeploymentRequest using the parameters first.
     */
    public DeploymentStatus disassociate(String targetName,
        String referenceName) throws IASDeploymentException {
        try {   
            long startTime = System.currentTimeMillis();
            DeployableObjectType type =
                DeploymentServiceUtils.getRegisteredType(referenceName);
            
            final DeploymentTarget target =
                DeploymentServiceUtils.getAndValidateDeploymentTarget(
                targetName, referenceName, true);
                
            InstanceEnvironment env =
                ApplicationServer.getServerContext().getInstanceEnvironment();
            DeploymentRequest req = new DeploymentRequest(
                                    env,
                                    type,
                                    DeploymentCommand.UNDEPLOY);

            req.setName(referenceName);
            req.setTarget(target);
        
            return disassociate(req, createAuditInfoIfOn(req, AuditInfo.Operation.disassociate, startTime));
        } catch(Exception e) {
            if (e instanceof IASDeploymentException) {
                throw (IASDeploymentException)e;
            }
            else {
               throw new IASDeploymentException(e);
            }
        }
    }

    public DeploymentStatus disassociate(String targetName,
        String referenceName, Map options) throws IASDeploymentException {
        try {
            long startTime = System.currentTimeMillis();
            DeployableObjectType type = 
                DeploymentServiceUtils.getRegisteredType(referenceName);

            final DeploymentTarget target =
                DeploymentServiceUtils.getAndValidateDeploymentTarget(
                targetName, referenceName, true);

            InstanceEnvironment env =
                ApplicationServer.getServerContext().getInstanceEnvironment();
            DeploymentRequest req = new DeploymentRequest(
                                    env,
                                    type,
                                    DeploymentCommand.UNDEPLOY);

            req.setName(referenceName);
            req.setTarget(target);

            DeploymentProperties dProps = new DeploymentProperties(options);
            req.setCascade(dProps.getCascade());
            req.setForced(dProps.getForce());
            req.setExternallyManagedPath(dProps.getExternallyManaged());
            req.setIsRedeployInProgress(dProps.getRedeploy());
            DeploymentServiceUtils.setResourceOptionsInRequest(req, dProps);

            return disassociate(req, createAuditInfoIfOn(req, AuditInfo.Operation.disassociate, startTime));
        } catch(Exception e) {
            if (e instanceof IASDeploymentException) {
                throw (IASDeploymentException)e;
            }
            else {
                throw new IASDeploymentException(e);
            }
        }
    }

    private DeploymentStatus start(DeploymentRequest req, AuditInfo auditInfo)
    {
        DeploymentStatus result = executePhases(req, startPhaseList);
        if (auditInfo != null) {
            auditInfo.reportEnd(result.getStatus());
        }
        return result;
    }	
    
    public DeploymentStatus start(DeploymentRequest req) {
        return start(req, createAuditInfoIfOn(req, AuditInfo.Operation.start));
    }

    public DeploymentStatus start(String moduleID, String targetName, 
        Map options) throws IASDeploymentException {
        try {
            long startTime = System.currentTimeMillis();
            DeployableObjectType type =
                DeploymentServiceUtils.getRegisteredType(moduleID);
        
            final DeploymentTarget target =
                DeploymentServiceUtils.getDeploymentTarget(targetName);
            
            InstanceEnvironment env = 
                ApplicationServer.getServerContext().getInstanceEnvironment();
            DeploymentRequest req = new DeploymentRequest(
                                    env,
                                    type,
                                    DeploymentCommand.DEPLOY);

            int actionCode;
            if(type.isAPP()) {
                actionCode = BaseDeployEvent.APPLICATION_DEPLOYED;
            }
            else {
                actionCode = BaseDeployEvent.MODULE_DEPLOYED;
            }
    
            req.setName(moduleID);
            req.setActionCode(actionCode);
            req.setTarget(target);

            DeploymentProperties dProps = new DeploymentProperties(options);
            req.setForced(dProps.getForce());
            DeploymentServiceUtils.setResourceOptionsInRequest(req, dProps);

            return start(req, createAuditInfoIfOn(req, AuditInfo.Operation.start, startTime));
       } catch(Exception e) {
            if (e instanceof IASDeploymentException) {
                throw (IASDeploymentException)e;
            }
            else {
                throw new IASDeploymentException(e);
            }
        }
    }

    private DeploymentStatus stop(DeploymentRequest req, AuditInfo auditInfo)
        throws IASDeploymentException {
        String moduleID = req.getName();
        DeployableObjectType type = req.getType();
        DeploymentServiceUtils.validate(moduleID,type,STOP_ACTION, req);

        DeploymentStatus result = executePhases(req, stopPhaseList);
        if (auditInfo != null) {
            auditInfo.reportEnd(result.getStatus());
        }
        return result;
    }
    
    public DeploymentStatus stop(DeploymentRequest req)
        throws IASDeploymentException {
        return stop(req, createAuditInfoIfOn(req, AuditInfo.Operation.stop));
    }	

    public DeploymentStatus stop(String moduleID, String targetName, 
        Map options) throws IASDeploymentException {
        try {
            long startTime = System.currentTimeMillis();
            DeployableObjectType type =
                DeploymentServiceUtils.getRegisteredType(moduleID);
     
            final DeploymentTarget target =
                DeploymentServiceUtils.getDeploymentTarget(targetName);
        
            InstanceEnvironment env =
                ApplicationServer.getServerContext().getInstanceEnvironment();
            DeploymentRequest req = new DeploymentRequest(
                                    env,
                                    type,
                                    DeploymentCommand.UNDEPLOY);
                
            int actionCode;
            if(type.isAPP()) {
                actionCode = BaseDeployEvent.APPLICATION_UNDEPLOYED;
            }   
            else {
                actionCode = BaseDeployEvent.MODULE_UNDEPLOYED;
            }                       
                                    
            req.setName(moduleID);
            req.setActionCode(actionCode);
            req.setTarget(target);

            DeploymentProperties dProps = new DeploymentProperties(options);
            req.setCascade(dProps.getCascade());
            req.setForced(dProps.getForce());
            req.setExternallyManagedPath(dProps.getExternallyManaged());
            DeploymentServiceUtils.setResourceOptionsInRequest(req, dProps);

            return stop(req, createAuditInfoIfOn(req, AuditInfo.Operation.stop, startTime));
        } catch(Exception e) {
            if (e instanceof IASDeploymentException) {
                throw (IASDeploymentException)e;
            }
            else {
                throw new IASDeploymentException(e);
            }
        }
    }

    
   /**
     * This method deploys application to the domain.
     * It constructs the DeploymentRequest using the parameters first.
     */
    public DeploymentStatus deploy(File deployFile, File planFile, 
        String archiveName, String moduleID, DeploymentProperties dProps, 
        DeploymentCallback callback) throws IASDeploymentException {
        try {
            if (deployFile == null) {
                throw new IASDeploymentException(
                localStrings.getString("deployfile_not_specified"));
            }

            /*
             *Save the current time to use in preparing the audit info later so
             *the audit measurement includes all the logic below.
             */
            long startTime = System.currentTimeMillis();
            sLogger.log(Level.FINE, "mbean.begin_deploy", moduleID);
            DeployableObjectType type = null;
            if (dProps.getType() != null) {
                type = DeploymentServiceUtils.getDeployableObjectType(dProps.getType());
            } else {
                type = DeploymentServiceUtils.getTypeFromFile(
                            moduleID, deployFile.getAbsolutePath());
            }                    

            InstanceEnvironment env =
                ApplicationServer.getServerContext().getInstanceEnvironment();
            DeploymentRequest req = new DeploymentRequest(
                                    env,
                                    type,
                                    DeploymentCommand.DEPLOY);
    
            DeploymentRequestRegistry.getRegistry().addDeploymentRequest(
                moduleID, req);

            req.setName(moduleID);
            boolean isRegistered = false;
            isRegistered = DeploymentServiceUtils.isRegistered(moduleID, type);
            // FIXME validation for new REDEPLOY property

            if (isRegistered) {
                DeploymentServiceUtils.validate(moduleID,type,REDEPLOY_ACTION, req);
            }

            req.setFileSource(deployFile);
            req.setDeploymentPlan(planFile);
            req.setForced(dProps.getForce());
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
            req.setDeploymentCallback(callback);
            req.setIsRedeployInProgress(dProps.getRedeploy());
            DeploymentServiceUtils.setResourceOptionsInRequest(req, dProps);

            Properties optionalAttributes = new Properties();
            String virtualServers = dProps.getVirtualServers();
            if(virtualServers!=null) {
                optionalAttributes.put(ServerTags.VIRTUAL_SERVERS,
                    virtualServers);
            }
            req.setOptionalAttributes(optionalAttributes);

            req.addOptionalArguments(dProps.prune());
            DeploymentServiceUtils.setHostAndPort(req);
            return deploy(req, createAuditInfoIfOn(req, AuditInfo.Operation.deploy, startTime));
        } catch(Exception e) {
            sLogger.log(Level.WARNING, "mbean.deploy_failed", e);
            if (e instanceof IASDeploymentException) {
                throw (IASDeploymentException)e;
            }
            else {
                throw new IASDeploymentException(e);
            }
        }
    }

    /**
     * This method undeploys application from domain.
     * It constructs the DeploymentRequest using the parameters first.
     *
     */
    public DeploymentStatus undeploy(String mModuleID,
        Map mParams) throws IASDeploymentException {
        sLogger.log(Level.FINE, "mbean.begin_undeploy", mModuleID);
        try {
            /*
             *Save the current time to use in preparing the audit info later so
             *the audit measurement includes all the logic below.
             */
            long startTime = System.currentTimeMillis();
            DeployableObjectType objectType = 
                DeploymentServiceUtils.getRegisteredType(mModuleID);

            DeploymentServiceUtils.checkAppReferencesBeforeUndeployFromDomain(
                mModuleID);

            if (objectType.isWEB()) {
                DeploymentServiceUtils.checkWebModuleReferences(mModuleID);
            }

            InstanceEnvironment env =
                ApplicationServer.getServerContext().getInstanceEnvironment();
            DeploymentRequest req = new DeploymentRequest(env,
                objectType, DeploymentCommand.UNDEPLOY);

            DeploymentRequestRegistry.getRegistry().addDeploymentRequest(
                mModuleID, req);

            DeploymentProperties dProps =
                new DeploymentProperties(mParams);
            req.setName(mModuleID);
            req.setCascade(dProps.getCascade());
            req.setExternallyManagedPath(dProps.getExternallyManaged());

            DeploymentServiceUtils.setResourceOptionsInRequest(req, dProps);

            req.addOptionalArguments(dProps.prune());
            return undeploy(req, createAuditInfoIfOn(req, AuditInfo.Operation.undeploy, startTime));
        }
        catch(Exception e) {
            String msg = localStrings.getString(
            "enterprise.deployment.phasing.deploymentservice.undeploy.failed",
            mModuleID, e.getLocalizedMessage());
            sLogger.log(Level.WARNING, msg);
            if (e instanceof IASDeploymentException) {
                throw (IASDeploymentException)e;
            }
            else {
                IASDeploymentException ias = 
                    new IASDeploymentException(e.getLocalizedMessage());
                ias.initCause(e);
                throw ias;
            }
        }
    }

    public boolean quit(String moduleID) {
        DeploymentRequest request = DeploymentRequestRegistry.getRegistry().getDeploymentRequest(moduleID);
        if (request != null) {
            request.setAbort(true);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the path for the client jar file of a deployed application
     */
    public static String getClientJarPath(String moduleID) {
        
        // let's ensure first that our client jar is ready.
        ClientJarMakerRegistry registry = ClientJarMakerRegistry.getInstance();
        
        if (registry.isRegistered(moduleID)) {
            
            // let's wait until it is finished.
            registry.waitForCompletion(moduleID);
        }
        
        return moduleID + DeploymentImplConstants.ClientJarSuffix;
        
    }
    
    /** 
     * utility method to succesively invoke DeploymentPhases instances 
     * provided as a list and rollback them in case of an exception 
     * during one of the phase execution
     *<p>
     *Note - after the phases are executed executePhases invokes the done
     *method on the DeploymentRequest to release its resources, in particular
     *the EJBClassLoaders.  The DeploymentRequest cannot be reused for
     *another deployment upon return from executePhases.
     *
     * @param DeploymentRequest the request to be served
     * @param phases is the @see List of phases to execute
     */
    protected DeploymentStatus executePhases(DeploymentRequest req, List phases)
    {
        try {
            Descriptor.setBoundsChecking(true);

            // first we create our deployment status to return feedback to the user
            DeploymentStatus ds = new DeploymentStatus();
            ds.setStageDescription("Deployment");
            req.setCurrentDeploymentStatus(ds);

            DeploymentPhaseContext phaseCtx[] = new DeploymentPhaseContext[phases.size()]; 
            for(int i=0 ; i < phases.size() ; i++)
            {
                try{            
                    // create a new status for this phase
                    DeploymentStatus phaseDs = new DeploymentStatus(ds);

                    // execute the phase
                    phaseCtx[i] = ((DeploymentPhase)phases.get(i)).executePhase(req, phaseDs);

                    // if the previous phase did not excecute successfully.
                    // we need to allow previous phases to rollback.
                    if (phaseDs.getStageStatus()<DeploymentStatus.WARNING) {
                        rollbackPhases(phases, phaseCtx, i-1);
                        // return main deployment status
                        return ds;
                    }

                } catch(Throwable dpe) {
                    // an exception has occured in the I'th phase, we need to rollback
                    // all previously executed phases (and ignore any failures that may
                    // be raised by this rollback).

                    String msg = 
                        localStrings.getString( "enterprise.deployment.phasing.deploymentservice.exception");
                    sLogger.log(Level.SEVERE, msg ,dpe);
                    rollbackPhases(phases, phaseCtx, i-1);

                    // we register the original exception as it was raised
                    // by the executePhase method in our deployment status
                    ds.setStageStatus(DeploymentStatus.FAILURE);
                    if (dpe instanceof java.io.Serializable) {
                        ds.setStageException(dpe);
                    } else {
                        sLogger.severe(localStrings.getString("enterprise.deployment.phasing.exception_notserializable", dpe.getClass()));
                        sLogger.severe(localStrings.getString("enterprise.deployment.phasing.exception_notforwarded", dpe.getMessage()));
                    }                
                    ds.setStageException(dpe);
                    ds.setStageStatusMessage(dpe.getMessage());
                    return ds;
                }
            }
            // we do not set the state of the deployment status
            // since it will be provided by the sub phases status.
            return ds;
        } finally {
            req.done();
        }
    }
    
    /**
     * Rollback previously excuted phases
     */
    private void rollbackPhases(List phases, DeploymentPhaseContext phaseCtx[], int index) {
        // now we are going to rollback all the previously executed phases
        for (int j=index; j>=0 ; j--) {
            try {
                ((DeploymentPhase)phases.get(j)).rollback(phaseCtx[j]);
            } catch(Exception rollbackException) {
                // it failed ! just log
                String msg =
                    localStrings.getString( "enterprise.deployment.phasing.deploymentservice.rollbackexception");
                sLogger.log(Level.INFO, msg ,rollbackException);
                // we swallow the exception to allow all phases to execute their
                // rollback
                
            }
        }
        
    }

    /**
     * This method finds moduleID by explicitly loading the dd for its 
     * display name.
     * This method is called by the DeployThread if and only if the client
     * uses JSR88.distribute with InputStream signature.
     * @@ todo: Ideally we do not want to load the deployment descriptor 
     * more than once.  This one is a necessary evil since we need the 
     * moduleID *now* before proceeding with the rest of deployment.  
     * Bigger re-construction is needed if we want to optimize deployment 
     * further for JSR88 using InputStream. 
     * NOTE that we choose to load the dd and use the display name as
     * the moduleID instead of the uploaded file name for backward
     * compatibility and clarity (uploaded file name is not descriptive).
     * @param file the deployed file
     * @return the moduleID derived from this file using the dd's display name
     */
    public String getModuleIDFromDD (File file) throws Exception {
        Archivist source = ArchivistFactory.getArchivistForArchive(file);
        InputJarArchive archive = new InputJarArchive();
        archive.open(file.getAbsolutePath());
        Descriptor descriptor = null;
        String moduleID = null;
        String displayName = null;
        try {
            descriptor = source.readStandardDeploymentDescriptor(archive);
        } catch (Exception ex) { 
            //ignore 
        }
        if (descriptor != null) {
            displayName = descriptor.getDisplayName();
        }
        if ((displayName != null) && (displayName.length() > 0)) {
            moduleID = displayName;
        } else {
            //We give up.  Use the uploaded file name instead
            moduleID = 
                (new DeploymentProperties()).getName(file.getAbsolutePath());
        }

        moduleID = moduleID.replace(' ','_');

        // This moduleID will be later used to construct file path,
        // replace the illegal characters in file name
        //  \ / : * ? " < > | with _
        moduleID = moduleID.replace('\\', '_').replace('/', '_');
        moduleID = moduleID.replace(':', '_').replace('*', '_');
        moduleID = moduleID.replace('?', '_').replace('"', '_');
        moduleID = moduleID.replace('<', '_').replace('>', '_');
        moduleID = moduleID.replace('|', '_');

        // This moduleID will also be used to construct an ObjectName 
        // to register the module, so replace additional special 
        // characters , =  used in property parsing with -
        moduleID = moduleID.replace(',', '_').replace('=', '_');

        return moduleID;
    }
    
    /**
     *Returns an instance of AuditInfo if the audit logging level mandates; null otherwise.
     *@param req the DeploymentRequest to be audited
     *@param operation the type of work being performed (deployment, undeployment, etc.)
     *@return AuditInfo or null, depending on the deployment audit logging level
     */
    private AuditInfo createAuditInfoIfOn(DeploymentRequest req, AuditInfo.Operation operation) {
        return auditLogger.isLoggable(Level.INFO) ? new AuditInfo(req, operation, System.currentTimeMillis()) : null;
    }

    /**
     *Returns an instance of AuditInfo if the audit logging level mandates; null otherwise.
     *@param req the DeploymentRequest to be audited
     *@param operation the type of work being performed (deployment, undeployment, etc.)
     *@param startTime start time of the operation being audited (typically via System.currentTimeMillis() )
     *@return AuditInfo or null, depending on the deployment audit logging level
     */
    private AuditInfo createAuditInfoIfOn(DeploymentRequest req, AuditInfo.Operation operation, long startTime) {
        return auditLogger.isLoggable(Level.INFO) ? new AuditInfo(req, operation, startTime) : null;
    }
    
    /**
     *Records information about auditing of a deployment operation and creates the
     *auditing messages.
     */
    private static class AuditInfo {
 
        /** keys for the audit messages */
        private static final String START_MESSAGE_KEY = "audit.start.message";
        private static final String END_MESSAGE_KEY = "audit.end.message";
        
        /**
         * possible outcome values when reporting completion of an operation 
         * Note that these same values must be used in the LogStrings.properties file
         * message keys.
         */
        enum Outcome {end(DeploymentStatus.SUCCESS), warning(DeploymentStatus.WARNING), fail(DeploymentStatus.FAILURE);
            private int deploymentStatus;
            
            Outcome(int deploymentStatus) {
                this.deploymentStatus = deploymentStatus;
            }
            
            private boolean matches(int deploymentStatus) {
                return (deploymentStatus == this.deploymentStatus);
            }
            
            private String getAuditMessage() {
                return auditBundle.getString("audit.outcome." + toString());
            }
        }
        
        /**
         * possible operation types - these values must also be used in 
         * LogStrings.properties message keys
         */
        enum Operation {deploy, undeploy, associate, disassociate, start, stop;
            private String getAuditMessage() {
                return auditBundle.getString("audit.operation." + toString());
                
            }
        
        }
        
        /** when the operation started */
        private long startTime;
        
        /** name of the principal under which the operation is running */
        private String principal;
        
        /** the deployment request being performed */
        private DeploymentRequest request;
        
        /**
         * operation this AuditInfo instance corresponds to
         */
        private Operation operation;

        /**
         *Creates a new instance of AuditInfo, providing the current time as 
         *the operation start time.
         *@param req the DeploymentRequest being performed
         *@param operation the Operation being performed
         *@param startTime when the operation started
         */
        private AuditInfo(DeploymentRequest req, Operation operation, long startTime) {
            request = req;
            this.startTime = startTime;
            this.operation = operation;
            principal = "Unknown";
            AccessControlContext acc = AccessController.getContext();
            Subject subject = Subject.getSubject(acc);
            if (subject == null) {

            } else {
                Iterator iter = subject.getPrincipals().iterator();
                // retrieve only the first principal
                if (iter.hasNext()) {
                    Principal p = (Principal) iter.next();
                    principal = p.getName();
                }
            }
            auditLogger.log(Level.INFO, START_MESSAGE_KEY, new String[]{ principal, operation.getAuditMessage(), request.getName(), request.getType().toString() } );
        }
        
        /**
         * Logs the ending deployment audit information.
         * @param outcome String set to one of "end," "warning," or "fail"
         */
        private void reportEnd(int deploymentStatus) {
            auditLogger.log(Level.INFO, END_MESSAGE_KEY, computeEndParameters(findOutcome(deploymentStatus)));
        }

        private Outcome findOutcome(int deploymentStatus) {
            Outcome result = null;
            for (Outcome outcome : Outcome.values() ) {
                if (outcome.matches(deploymentStatus) ) {
                    result = outcome;
                    break;
                }
            }
            if (result == null) {
                throw new IllegalArgumentException("Deployment status value of " + deploymentStatus + " could not be mapped to an audit outcome");
            }
            return result;
        }
        
        /**
         * Prepares the message parameters for the ending message.
         * @return String array suitable for passing to the logger.log method.
         */
        private String[] computeEndParameters(Outcome outcome) {
            String[] result = new String[] 
                {principal, 
                 operation.getAuditMessage(), 
                 outcome.getAuditMessage(), 
                 request.getName(),
                 request.getType().toString(),
                 String.valueOf(System.currentTimeMillis() - startTime)
                 };
             return result;
        }
    }
}
