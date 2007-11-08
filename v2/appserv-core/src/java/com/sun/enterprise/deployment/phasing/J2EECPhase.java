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
 * J2EECPhase.java
 *
 * Created on May 20, 2003, 3:13 PM
 * @author  sandhyae
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/phasing/J2EECPhase.java,v $
 *
 */

package com.sun.enterprise.deployment.phasing;

import com.sun.appserv.management.deploy.DeploymentProgress;
import com.sun.appserv.management.deploy.DeploymentProgressImpl;
import com.sun.enterprise.admin.event.BaseDeployEvent;
import com.sun.enterprise.appverification.factory.AppVerification;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.backend.DeployableObjectType;
import com.sun.enterprise.deployment.backend.Deployer;
import com.sun.enterprise.deployment.backend.DeployerFactory;
import com.sun.enterprise.deployment.backend.DeploymentEvent;
import com.sun.enterprise.deployment.backend.DeploymentEventInfo;
import com.sun.enterprise.deployment.backend.DeploymentEventType;
import com.sun.enterprise.deployment.backend.DeploymentLogger;
import com.sun.enterprise.deployment.backend.DeploymentRequest;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.backend.IASDeploymentException;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebService;
import com.sun.enterprise.management.deploy.DeploymentCallback;
import com.sun.enterprise.resource.Resource;
import com.sun.enterprise.resource.ResourceUtilities;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.RelativePathResolver;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

import javax.enterprise.deploy.shared.ModuleType;

/**
 * This class represents the J2EEC phase of the deployment process.
 * An application or module is prepared using the deployer and then registered with the 
 * config and a corresponding mbean registered with the mbean server
 * @author  Sandhya E
 */
public class J2EECPhase extends DeploymentPhase {
    
    /** Deployment Logger object for this class */
    public static final Logger sLogger = DeploymentLogger.get();
    
    /** string manager */
    private static StringManager localStrings =
        StringManager.getManager( J2EECPhase.class );
   

    /**
     * Creates a new instance of J2EECPhase 
     * @param deploymentCtx DeploymentContext object
     */
    public J2EECPhase(DeploymentContext deploymentCtx) {
        this.deploymentCtx = deploymentCtx;
        this.name = J2EEC;
    }
    
    /** 
     * Phase specific execution logic will go in this method. Any phase implementing
     * this class will provide its implementation for this method.
     * preDeploy notifications are sent from the deployment backend(deployer) to
     * the cmp module.
     * @param phaseCtx the DeploymentPhaseContext object     
     */
    public void runPhase(DeploymentPhaseContext phaseCtx)
    {
        boolean wasUnRegistered = false;
        String	type		= null;
        boolean   isApp		= false;
        boolean	isRedeploy	= false;
        int		actionCode	= -1;
        String	targetName  = null;
        DeploymentTarget target = null;
        DeploymentRequest req = phaseCtx.getDeploymentRequest();
        DeploymentStatus status = phaseCtx.getDeploymentStatus();

        Deployer deployer = null;
        try {
            DeploymentCallback callback = req.getDeploymentCallback();
            if (callback != null) {
                int percent = 0;
                DeploymentProgress progress = new DeploymentProgressImpl(
                    (byte)percent, "deployment started", null);
                callback.deploymentProgress(progress);
            }
        
            // set the current deployment status in deployment request
            // to J2EECPhase deployment status
            req.setCurrentDeploymentStatus(status);

            req.setDescriptor(deploymentCtx.getApplication(req.getName()));

            deployer = DeployerFactory.getDeployer(req);
            
            //prePhaseNotify(getPrePhaseEvent(deployer.getPreEventInfo()));
            if(req.isApplication())
                isApp = true;
            
            deployer.doRequestPrepare();
            
            // the isReDeploy() call *must* come after doRequestPrepare() for modules.
            if(req.isReDeploy()) {
                isRedeploy = true;

                //Re-record instrospect/instrument/verifier data if
                //any of the application is re-deployed
                if (AppVerification.doInstrument()) {
                    AppVerification.getInstrumentLogger().handleChangeInDeployment();
                }
            }
            
            if(isRedeploy) {
                // clear the deploymentCtx cache now
                deploymentCtx.removeApplication(req.getName());               
                req.setDescriptor(null);

                target = (DeploymentTarget)req.getTarget();
                
                // In the case of redeploy to domain,
                // no stop event will be sent.
                if(target != null && ! target.getName().equals("domain")) {
                    targetName = target.getName();
                
                    if(isApp) {
                        type = null;
                        actionCode	= BaseDeployEvent.APPLICATION_UNDEPLOYED;
                    }
                    else {
                        type = DeploymentServiceUtils.getModuleTypeString(req.getType());
                        actionCode	= BaseDeployEvent.MODULE_UNDEPLOYED;
                    }
                    DeploymentServiceUtils.multicastEvent(actionCode, req.getName(), type, req.getCascade(), req.isForced(),  targetName);
                    wasUnRegistered = true;
                }
            }
            
            deployer.doRequestFinish();

            // cache the updated application object in deployment context 
            deploymentCtx.addApplication(req.getName(), req.getDescriptor());
 
            parseAndValidateSunResourcesXMLFiles(req);

            // check if an abort operation has been issued
            // throw exception if true
            DeploymentServiceUtils.checkAbort(req.getName());

            // do all the config update at the end for easy rollback
            if(isRedeploy) {
                DeploymentServiceUtils.updateConfig(req);
            } else {
                DeploymentServiceUtils.addToConfig(req);
            }
 
            // set context roots on config bean
            ApplicationConfigHelper.resetAppContextRoots(
                DeploymentServiceUtils.getConfigContext(), req.getName(), 
                true);

            wasUnRegistered = false;	// addToConfig re-registered it...

            deployer.cleanup();
            
            // everything went fine
            status.setStageStatus(DeploymentStatus.SUCCESS);            
            
            // some useful information for clients...
            sLogger.log(Level.INFO, "deployed with " + DeploymentProperties.MODULE_ID + " = " + req.getName());
            populateStatusProperties(status, req);
            if (callback != null) {
                int percent = 100;
                DeploymentProgress progress2 = new DeploymentProgressImpl(
                    (byte)percent, "deployment finished", null);
                callback.deploymentProgress(progress2);
            }
         
        } catch(Throwable t) {
            String msg =
            localStrings.getString("enterprise.deployment.phasing.j2eec.error" );
            if (t.getCause()!= null) 
                msg += t.getCause().toString();

            // For any failure during the J2EEC phase (during fresh deploy or redeploy), cleanup the domain.xml
            // so that the config is left in a clean state without any hanging j2ee-app/module elements without
            // any app-refs;
            // the target==null check ensures that this code is done for 8.1 only thereby preserving same
            // behavior for 8.0 apps
            try {
                if (target == null) {
                    if (deployer != null) {
                        deployer.removePolicy();
                    }
                    DeploymentServiceUtils.removeFromConfig(req.getName(),
                        req.getType());
                }
            } catch (Exception eee){}

            if(isRedeploy && wasUnRegistered && t instanceof IASDeploymentException && req.getReRegisterOnFailure()) {
                // DBE rollback re-registered.  We need to notify to get it reloaded now.
                if(isApp)
                    actionCode = BaseDeployEvent.APPLICATION_DEPLOYED;
                else
                    actionCode = BaseDeployEvent.MODULE_DEPLOYED;
                
                try {
                    DeploymentServiceUtils.multicastEvent(actionCode, req.getName(), type, req.getCascade(), req.isForced(), targetName);
                }
                catch(Throwable t2) {
                    msg += t2;
                }
            }
            // log
            sLogger.log(Level.SEVERE, msg, t);
            
            // we now update our status
            status.setStageStatus(DeploymentStatus.FAILURE);
            if (t instanceof java.io.Serializable) {
                status.setStageException(t);
            } else {
                sLogger.severe(localStrings.getString("enterprise.deployment.phasing.exception_notserializable", t.getClass()));
                sLogger.severe(localStrings.getString("enterprise.deployment.phasing.exception_notforwarded", t.getMessage()));
            }
            status.setStageStatusMessage(t.getMessage());
        }
    }
    
    /**
     * Event that will be broadcasted at the start of the phase
     * @param info deployment event info
     * @return DeploymentEvent
     **/
    protected DeploymentEvent getPrePhaseEvent(DeploymentEventInfo info) {
        //FIXME change the event type and include a eventSource
        return new DeploymentEvent(DeploymentEventType.PRE_DEPLOY, info );
    }
    
    /**
     * Event that will be broadcasted at the end of the phase
     * @return DeploymentEvent
     **/
    protected DeploymentEvent getPostPhaseEvent(DeploymentEventInfo info) {
        //FIXME change the event type and include a eventSource
        return new DeploymentEvent(DeploymentEventType.POST_DEPLOY, info);
    }

    /**
     * This method populate the properties that will be returned from server to 
     * the client, including moduleID and list of wsdl files.
     */
    private void populateStatusProperties(
                    DeploymentStatus status, DeploymentRequest request)
        throws IOException, IASDeploymentException {
        DeploymentStatus mainStatus = status.getMainStatus();
        populateModuleIDs(mainStatus, request);
        if (request.getDescriptor() != null) {
            populateWsdlFilesForPublish(mainStatus, request); 
        }
    }

    /**
     * Populate the moduleID, subModuleID and ModuleType information.
     * The returned data is constructed as follows:
     * For example: a.ear contains b.jar, c.jar and d.war
     * The entries in the DeploymentStatus additional properties are:
     * (in key = value format)
     * moduleid = a;
     * a_moduletype = application;
     * a_submoduleCount = 3;
     * a_moduleid_0 = b;
     * b_moduletype = ejb;
     * a_moduleid_1 = c;
     * c_moduletype = appclient;
     * a_moduleid_2 = d;
     * d_moduletype = web;
     * d_contextroot= contextroot;
     *
     * Note that the actual construct of the submoduleID is a bit more 
     * complicated than what is in the example above. But the idea is
     * the same.
     */
    private void populateModuleIDs(
                    DeploymentStatus status, DeploymentRequest request) {

        String sep = DeploymentStatus.KEY_SEPARATOR;

        //top module
        String key = DeploymentStatus.MODULE_ID;
        String moduleID = request.getName();
        status.addProperty(key, moduleID); //moduleID

        key =  com.sun.appserv.management.deploy.DeploymentStatus.MODULE_ID_KEY;
        status.addProperty(key, moduleID); //moduleID

        key = moduleID + sep + DeploymentStatus.MODULE_TYPE;
        ModuleType moduleType = request.getType().getModuleType();
        status.addProperty(key, String.valueOf(moduleType.getValue())); //moduleType

        //sub modules
        Application app = request.getDescriptor();
        if (app!=null) {
            if (!app.isVirtual()) {
                int counter = 0;
                for (Iterator it = app.getModules(); it.hasNext();) {
                    ModuleDescriptor md = (ModuleDescriptor) it.next();

                    key = moduleID + sep + 
                          DeploymentStatus.MODULE_ID + sep + 
                          String.valueOf(counter);
                    String subModuleID = moduleID + "#" + md.getArchiveUri();
                    status.addProperty(key, subModuleID); //subModuleID

                    key = subModuleID + sep + DeploymentStatus.MODULE_TYPE;
                    //subModuleType
                    status.addProperty(key, String.valueOf(md.getModuleType().getValue()));

                    if (ModuleType.WAR.equals(md.getModuleType())) {
                        WebBundleDescriptor wbd = 
                            (WebBundleDescriptor) md.getDescriptor();
                        key = subModuleID + sep + DeploymentStatus.CONTEXT_ROOT;
                        status.addProperty(key, getContextRoot(wbd)); //contextRoot
                    }
                    counter++;
                }

                key = moduleID + sep + DeploymentStatus.SUBMODULE_COUNT;
                status.addProperty(key, String.valueOf(counter)); //nums
            } else { //standalone module
                BundleDescriptor bd = app.getStandaloneBundleDescriptor();
                if (ModuleType.WAR.equals(bd.getModuleType())) {
                    WebBundleDescriptor wbd = (WebBundleDescriptor) bd;
                    key = moduleID + sep + DeploymentStatus.CONTEXT_ROOT;
                    status.addProperty(key, getContextRoot(wbd)); //contextRoot
                }
            }
        }
    }

    /**
     * Populate the wsdl files entries to download (if any).
     */
    private void populateWsdlFilesForPublish(
                    DeploymentStatus status, DeploymentRequest request) 
        throws IOException, IASDeploymentException {

        ModuleType moduleType = request.getType().getModuleType();
        //only EAR, WAR and EJB archives could contain wsdl files for publish
        if (!(ModuleType.EAR.equals(moduleType) ||
              ModuleType.WAR.equals(moduleType) ||
              ModuleType.EJB.equals(moduleType))) {
            return;
        }

        String sep = DeploymentStatus.KEY_SEPARATOR;
        Application app = request.getDescriptor();
        String moduleID = request.getName();
        AbstractArchive moduleArchive = null;
        String key = null;
        String keyPrefix = null;

        FileArchive archive = new FileArchive();
        archive.open(RelativePathResolver.resolvePath(
                        getGeneratedAppLocation(request)));

        for (Iterator it = app.getWebServiceDescriptors().iterator(); 
                it.hasNext();) {
            WebService webService = (WebService) it.next();
            // No work needed if webservice is configured for URL publishing
            if (!webService.hasFilePublishing()) {
                continue;
            }

            // For file publishing, URL is a file URL for a directory
            String clientPublishURL = 
                    webService.getClientPublishUrl().toExternalForm();
            if (app.isVirtual()) { //standalone module
                keyPrefix = moduleID;
                moduleArchive = archive;
            }  else {
                ModuleDescriptor md = 
                        webService.getBundleDescriptor().getModuleDescriptor();
                keyPrefix = moduleID + "#" + md.getArchiveUri();
                moduleArchive = archive.getEmbeddedArchive(md.getArchiveUri());
            }

            key = keyPrefix + sep + DeploymentStatus.WSDL_PUBLISH_URL;
            status.addProperty(key, clientPublishURL);

            // Collect the names of all entries in or below the 
            // dedicated wsdl directory.
            BundleDescriptor bundle = webService.getBundleDescriptor();
            Enumeration entries = moduleArchive.entries(bundle.getWsdlDir());

            // Strictly speaking, we only need to write the files needed by 
            // imports of this web service's wsdl file.  However, it's not
            // worth actual parsing the whole chain just to figure this
            // out.  In the worst case, some unnecessary files under 
            // META-INF/wsdl or WEB-INF/wsdl will be written to the publish
            // directory.
            int counter = 0;
            while(entries.hasMoreElements()) {
                String name = (String) entries.nextElement();
                key = keyPrefix + sep + DeploymentStatus.WSDL_FILE_ENTRIES +
                            sep + String.valueOf(counter);
                status.addProperty(key, stripWsdlDir(name,bundle));

                //full path to the wsdl file location on the server
                String wsdlFileLocation = 
                        moduleArchive.getArchiveUri() + File.separator + 
                        name.replace('/', File.separatorChar);
                key = key + sep + DeploymentStatus.WSDL_LOCATION;
                status.addProperty(key, wsdlFileLocation);
                counter++;
            }
            key = keyPrefix + sep + DeploymentStatus.WSDL_FILE_ENTRIES 
                    + sep + DeploymentStatus.COUNT;
            status.addProperty(key, String.valueOf(counter));
        }
    }

    private String getContextRoot(WebBundleDescriptor wbd) {
        String contextRoot = wbd.getContextRoot();
        if (!contextRoot.startsWith("/")) {
            contextRoot = "/" + contextRoot;
        }
        return contextRoot;
    }

    private String getGeneratedAppLocation (DeploymentRequest request) 
        throws IASDeploymentException {
        String xmlDir = request.getDescriptor().getGeneratedXMLDirectory();
        // for upgrade scenario, we fall back to the original location
        if (xmlDir == null || !FileUtils.safeIsDirectory(xmlDir)) {
            xmlDir = DeploymentServiceUtils.getLocation(
                        request.getName(), request.getType());
        }
        return xmlDir;
    }

    /**
     * Return the entry name without "WEB-INF/wsdl" or "META-INF/wsdl".
     */
    private String stripWsdlDir(String entry, BundleDescriptor bundle) {
        String wsdlDir = bundle.getWsdlDir();
        return entry.substring(wsdlDir.length()+1);
    }

    // this API parses the sun-resources.xml files and check for duplicates
    // conflicts within the archive and with domain.xml
    private void parseAndValidateSunResourcesXMLFiles(DeploymentRequest req) 
        throws Exception {
        List<Resource> resourceList = DeploymentServiceUtils.getResourceList(
            req, true, deploymentCtx);
        ResourceUtilities.getResourceConflictsWithDomainXML(
            resourceList, DeploymentServiceUtils.getConfigContext());
    }
}
