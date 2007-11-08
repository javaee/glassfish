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

package com.sun.enterprise.jbi.serviceengine.core;

import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.deployment.DeploymentContext;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.backend.J2EEModuleExploder;
import com.sun.enterprise.deployment.client.DeploymentFacility;
import com.sun.enterprise.deployment.client.DeploymentFacilityFactory;
import com.sun.enterprise.deployment.client.JESProgressObject;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.deploy.shared.OutputJarArchive;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.jbi.serviceengine.util.JBIConstants;
import com.sun.enterprise.jbi.serviceengine.util.Util;
import com.sun.enterprise.jbi.serviceengine.util.soap.WSDLConverter;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.util.shared.ArchivistUtils;
import com.sun.enterprise.util.FileUtil;
import com.sun.logging.LogDomains;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import java.io.*;
import java.util.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.ObjectName;

/**
 * Represents JavaEE Service Engine ServiceUnitManager, responsible for
 * deploy/undeploy/start/stop of JavaEE application packaged inside the service
 * assembly.
 *
 * @author bhavanishankar@dev.java.net
 */
public class JavaEEServiceEngineSUManager implements ServiceUnitManager, JBIConstants {
    
    protected static final Logger logger=LogDomains.getLogger(LogDomains.DPL_LOGGER);
    
    private String className = "JavaEEServiceEngineSUManager :: ";
    private DeploymentFacility deployer;
    private String target = "server"; // default target is DAS instance.
    private JBIEndpointManager epManager;
    private Set<String> dummyCompApps;

    /**
     * Creates a new instance of JavaEEServiceEngineServiceUnitManager
     */
    public JavaEEServiceEngineSUManager() {
        createDeployer();
        epManager = new JBIEndpointManager();
        dummyCompApps = new HashSet<String>();
    }
    
    // START :: ServiceUnitManager callbacks.
    
    public void init(String suId, String suPath) throws DeploymentException {
        String methodSig = className + "init(String, String)";
        logger.log(Level.FINE,
                methodSig + " suId = " + suId + ". suPath = " + suPath);
        try {
            if(isDummyApp(suPath))
                dummyCompApps.add(suId);

            epManager.storeAllEndpoints(suPath, suId);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
        
        /** 
         * Start the Java EE app & enable the endpoints in NMR
         */
        if (isDAS() && !isPrivateMBeanRegistered()) {
            logger.log(Level.FINE,
                    methodSig +
                    " either a non-DAS instance or start is done through" +
                    " private MBean, hence skipping start.");
            doStart(suId, target);
        }
        
        try {
            epManager.startAllEndpoints(suId);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
        
    }
    
    public void shutDown(String suId) throws DeploymentException {
        String methodSig = className + "shutDown(String)";
        logger.log(Level.FINE, methodSig + " suId = " + suId);
        try {
            dummyCompApps.remove(suId);
            epManager.removeAllEndpoints(suId);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }
    
    /**
     * Only deploy the JavaEE application and keep it in 'disable'
     * state until JBI fires start event.
     */
    public String deploy(String suId, String suPath) throws
            DeploymentException {
        String methodSig = className + "deploy(String, String)";
        logger.log(Level.FINE,
                methodSig + " suId = " + suId + ". suPath = " + suPath);
        
        if (!isDAS() || isPrivateMBeanRegistered()) {
            logger.log(Level.FINE,
                    methodSig + " either a non-DAS instance or deployment is done " +
                    "through private MBean, hence skipping deploy.");
            return Util.buildManagementMessage(
                    "STATUS_MSG",
                    "deploy",
                    "SUCCESS",
                    null,
                    suId,
                    null,
                    null);
        }
        return doDeploy(suId, suPath, target);
    }
    
    public String undeploy(String suId, String suPath) throws
            DeploymentException {
        String methodSig = className + "undeploy(String, String)";
        logger.log(Level.FINE,
                methodSig + " suId = " + suId + ". suPath = " + suPath);
        
        if (!isDAS() || isPrivateMBeanRegistered()) {
            logger.log(Level.FINE,
                    methodSig +
                    " either a non-DAS instance or undeployment is done" +
                    " through private MBean, hence skipping undeploy.");
            return Util.buildManagementMessage(
                    "STATUS_MSG",
                    "undeploy",
                    "SUCCESS",
                    null,
                    suId,
                    null,
                    null);
        }
        return doUnDeploy(suId, target);
    }
    
    /**
     * Enable the deployed JavaEE application during the start event.
     */
    public void start(String suId) throws DeploymentException {
        String methodSig = className + "start(String) :: NO-OP";
        logger.log(Level.FINE, methodSig + " suId = " + suId);
    }
    
    /**
     * Disable the deployed JavaEE application during the stop event.
     */
    public void stop(String suId) throws DeploymentException {
        String methodSig = className + "stop(String)";
        logger.log(Level.FINE, methodSig + " suId = " + suId);

        if (isDAS() && !isPrivateMBeanRegistered()) {
            logger.log(Level.FINE,
                    methodSig +
                    " either a non-DAS instance or deployment is done " +
                    "through private MBean, hence skipping stop.");
            doStop(suId, target);
        }
        
        try {
            epManager.stopAllEndpoints(suId);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }
    
    // END :: ServiceUnitManager callbacks.
    
    // START :: Methods exposed to the private JavaEEDeployerMBean.
    
    protected String doDeploy(String serviceUnitName,String appLocation,String target) {
        String methodSig = className + "doDeploy(String,String,String,String) "; // used for logging.
        boolean deploymentSuccessful = true;
        String deploymentError = null;
        Throwable deploymentException = null;
        String archivePath = null;
        AbstractArchive arch = null;
        try {
            if (!isDummyApp(appLocation)) {
                if(!(USED_WITH_NON_SOAP_WSDL.equalsIgnoreCase(System.getProperty(USED_WITH)))) {
                    appLocation = processWSDLs(appLocation);
                }
                archivePath = (new File(appLocation)).isDirectory() 
                    ? createArchive(appLocation, serviceUnitName)
                    : appLocation;
                
                logger.log(Level.FINE,
                        methodSig + " appName = " + serviceUnitName + ", archivePath = " +
                        archivePath + ", target = " + target);

                Map deployOptions = getDefaultDeploymentOptions(archivePath,
                        serviceUnitName,
                        target);
                arch = (new ArchiveFactory()).openArchive(archivePath);
                AbstractArchive plan = null;
                Target[] targets = deployer.createTargets(new String[]{target});
                logger.log(Level.FINE, methodSig + " calling backend deploy");
                JESProgressObject progressObject =
                        deployer.deploy(targets, arch, plan, deployOptions);
                DeploymentStatus status = deployer.waitFor(progressObject);
                logger.log(Level.FINE,
                        methodSig + " deployment complete. status = " + status.getStatus());
                if (status.getStatus() != DeploymentStatus.SUCCESS) {
                    deploymentSuccessful = false;
                    deploymentError = methodSig + status.toString();
                    deploymentException = status.getStageException();
                }
            } else {
                dummyCompApps.add(serviceUnitName);
            }
        } catch (Exception ex) {
            deploymentSuccessful = false;
            deploymentError = methodSig + ex.getMessage();
            deploymentException = ex;
        } finally {
            if(arch != null) {
                try {
                    arch.close();
                } catch(Exception ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }
        
        if(archivePath != null && (new File(appLocation)).isDirectory() ) {
            try {
                boolean deleteSuccessful = new File(archivePath).delete();
                if(!deleteSuccessful) {
                    logger.log(Level.SEVERE, "Unable to delete the archive " + archivePath);
                }
            } catch(Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        
        if(deploymentSuccessful) {
            logger.log(Level.FINE, methodSig + " successfully deployed " + serviceUnitName);
            return Util.buildManagementMessage(
                    "STATUS_MSG",
                    "deploy",
                    "SUCCESS",
                    null,
                    serviceUnitName,
                    null,
                    null);
        } else {
            logger.log(Level.SEVERE, deploymentError, deploymentException);
            return Util.buildManagementMessage(
                    "EXCEPTION_MSG",
                    "deploy",
                    "FAILED",
                    null,
                    serviceUnitName,
                    deploymentError,
                    deploymentException);
        }
    }
    
    protected String doUnDeploy(String serviceUnitName,String target) {
        String methodSig = className + "doUnDeploy(String,String,String) "; // used for logging.
        boolean undeploymentSuccessful = true;
        String undeploymentError = null;
        Throwable undeploymentException = null;
        try {
            //TODO: clear registries (wsdlCache)
            if(!dummyCompApps.remove(serviceUnitName)) {
                Target[] targets = deployer.createTargets(new String[]{target});
                JESProgressObject progressObject =
                        deployer.undeploy(targets, serviceUnitName, getUnDeploymentOptions());
                DeploymentStatus status = deployer.waitFor(progressObject);
                if (status.getStatus() != DeploymentStatus.SUCCESS) {
                    undeploymentSuccessful = false;
                    undeploymentError = methodSig + status.toString();
                    undeploymentException = status.getStageException();
                }
            }
            
        } catch (Exception ex) {
            undeploymentSuccessful = false;
            undeploymentError = methodSig + ex.getMessage();
            undeploymentException = ex;
        }
        
        if(undeploymentSuccessful) {
            logger.log(Level.FINE, methodSig + " successfully undeployed " + serviceUnitName);
            return Util.buildManagementMessage(
                    "STATUS_MSG",
                    "undeploy",
                    "SUCCESS",
                    null,
                    serviceUnitName,
                    null,
                    null);
        } else {
            logger.log(Level.SEVERE, undeploymentError, undeploymentException);
            return Util.buildManagementMessage(
                    "EXCEPTION_MSG",
                    "undeploy",
                    "FAILED",
                    null,
                    serviceUnitName,
                    undeploymentError,
                    undeploymentException);
        }
    }
    
    protected void doStart(String serviceUnitName, String target) {
        String methodSig = className + "doStart(String,String) "; // used for logging.
        boolean startSuccessful = true;
        String startError = null;
        Throwable startException = null;
        
        try {
            if(!dummyCompApps.contains(serviceUnitName)) {
                Target[] targets = deployer.createTargets(new String[]{target});
                JESProgressObject progressObject =
                        deployer.enable(targets, serviceUnitName);
                DeploymentStatus status = deployer.waitFor(progressObject);
                if (status.getStatus() != DeploymentStatus.SUCCESS) {
                    startSuccessful = false;
                    startError = methodSig + status.toString();
                    startException = status.getStageException();
                }
            }
            
        } catch (Exception ex) {
            startSuccessful = false;
            startError = methodSig + ex.getMessage();
            startException = ex;
        }
        
        if(startSuccessful) {
            logger.log(Level.FINE, methodSig + " successfully started " + serviceUnitName);
        } else {
            logger.log(Level.SEVERE, startError, startException);
        }
    }
    
    protected void doStop(String serviceUnitName, String target) {
        String methodSig = className + "doStop(String,String) "; // used for logging.
        boolean stopSuccessful = true;
        String stopError = null;
        Throwable stopException = null;
        
        try {
            if(!dummyCompApps.contains(serviceUnitName)) {
                Target[] targets = deployer.createTargets(new String[]{target});
                JESProgressObject progressObject =
                        deployer.disable(targets, serviceUnitName);
                DeploymentStatus status = deployer.waitFor(progressObject);
                if (status.getStatus() != DeploymentStatus.SUCCESS) {
                    stopSuccessful = false;
                    stopError = methodSig + status.toString();
                    stopException = status.getStageException();
                }
            }
            
        } catch (Exception ex) {
            stopSuccessful = false;
            stopError = methodSig + ex.getMessage();
            stopException = ex;
        }
        
        if(stopSuccessful) {
            logger.log(Level.FINE, methodSig + " successfully stopped " + serviceUnitName);
        } else {
            logger.log(Level.SEVERE, stopError, stopException);
        }
    }
    
    // END :: Methods exposed to the private JavaEEDeployerMBean.
    
    // START : Utility methods.
    
    private String createArchive(String dir, String archiveName) {
        String archivePath;
        try {
            Archivist archivist = ArchivistFactory.getArchivistForArchive(dir);
            String pathExtn = ".jar";
            if (ModuleType.EAR.equals(archivist.getModuleType())) {
                pathExtn = ".ear";
            } else if (ModuleType.WAR.equals(archivist.getModuleType())) {
                pathExtn = ".war";
            } else if (ModuleType.RAR.equals(archivist.getModuleType())) {
                pathExtn = ".rar";
            }
            String workspaceDir =
                    JavaEEServiceEngineContext.getInstance().
                    getJBIContext().getWorkspaceRoot();
            archivePath = workspaceDir + File.separator + archiveName + pathExtn;
            createJar(dir, archivePath);
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, ioe.getMessage(), ioe);
            archivePath = null;
        }
        return archivePath;
    }
    
    private void createJar(String sourcePath, String destinationPath)
    throws IOException {
        FileArchive source = new FileArchive();
        OutputJarArchive destination = new OutputJarArchive();
        try {
            source.open(sourcePath);
            Enumeration entries = source.entries();
            destination.create(destinationPath);
            while(entries.hasMoreElements()) {
                String entry = String.class.cast(entries.nextElement());
                InputStream is = null;
                OutputStream os = null;
                try {
                    is = source.getEntry(entry);
                    os = destination.putNextEntry(entry);
                    ArchivistUtils.copyWithoutClose(is, os);
                } finally {
                    if (is != null) is.close();
                    if (os != null) destination.closeEntry();
                }
            }
        } finally {
            source.close();
            destination.close();
        }
    }
    
    private void explodeArchive(String src, String dest) throws IOException {
        File srcFile = new File(src);
        File destFile = new File(dest);
        if(destFile.exists()) {
            DeploymentContext.deleteDirectory(destFile);
            destFile.mkdir();
        }
        Archivist archivist = ArchivistFactory.getArchivistForArchive(srcFile);
        try {
            ModuleType moduleType = archivist.getModuleType();
            if(ModuleType.EAR.equals(moduleType)) {
                J2EEModuleExploder.explodeEar(srcFile, destFile);
            } else if ( ModuleType.EJB.equals(moduleType) ||
                    ModuleType.CAR.equals(moduleType) ||
                    ModuleType.RAR.equals(moduleType) ||
                    ModuleType.WAR.equals(moduleType) ) {
                J2EEModuleExploder.explodeJar(srcFile, destFile);
            }
        } catch (Exception e) {
            IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
    }
    
    private Map getDefaultDeploymentOptions(String archivePath,
            String appName,
            String target) {
        DeploymentProperties deploymentOptions = new DeploymentProperties();
        deploymentOptions.setName(appName);
        deploymentOptions.setForce(true);
        deploymentOptions.setArchiveName(archivePath);
        deploymentOptions.setEnable(false);
        deploymentOptions.setTarget(target);
        deploymentOptions.setExternallyManaged(true);
        return deploymentOptions;
    }

    private Map getUnDeploymentOptions() {
        DeploymentProperties undeploymentOptions = new DeploymentProperties();
        undeploymentOptions.setExternallyManaged(true);
        return undeploymentOptions;
    }
    
    private boolean isPrivateMBeanRegistered() {
        try {
            ComponentContext jbiContext =
                    JavaEEServiceEngineContext.getInstance().getJBIContext();
            ObjectName privateMBeanName =
                    jbiContext.getMBeanNames().createCustomComponentMBeanName("JavaEEDeployer");
            boolean isPrivateMBeanRegistered =
                    jbiContext.getMBeanServer().isRegistered(privateMBeanName);
            logger.log(Level.FINE,
                    "isPrivateMBeanRegistered = " + isPrivateMBeanRegistered);
            return isPrivateMBeanRegistered;
        } catch(Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return false;
        }
    }
    
    private void createDeployer() {
        deployer = DeploymentFacilityFactory.getLocalDeploymentFacility();
    }
    
    private boolean isDAS() {
        try {
            return ServerHelper.isDAS(
                    AdminService.getAdminService().getAdminContext().getAdminConfigContext(),
                    ApplicationServer.getServerContext().getInstanceName());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, className + ex.getMessage(), ex);
            //better be more restrictive by returning false
            return false;
        }
    }

    /**
     * Check whether this is a dummy JavaEE application. In such case deployment 
     * will be skipped as the application will be deployed outside of the 
     * composite application.
     * In a dummy application allowed entries are -
     * META-INF/jbi.xml and META-INF/MANINFEST.MF.
     */
    private boolean isDummyApp(String appLocation) throws IOException {
        File app = new File(appLocation);
        if(app.isDirectory()) {
            for (Object fileObj : FileUtil.getAllFilesAndDirectoriesUnder(app)) {
                String path = ((File)fileObj).getPath();
                if(!("META-INF"+File.separator+"jbi.xml").equalsIgnoreCase(path) &&
                        !("META-INF"+File.separator+"MANIFEST.MF").equalsIgnoreCase(path) &&
                        !"META-INF".equalsIgnoreCase(path))
                    return false;
            }
        } else {
            ZipFile zipFile = new ZipFile(appLocation);
            for (Enumeration e = zipFile.entries(); e.hasMoreElements();) {
                String entryName = ((ZipEntry)e.nextElement()).getName();
                if(!("META-INF/jbi.xml").equalsIgnoreCase(entryName) &&
                        !("META-INF/MANIFEST.MF").equalsIgnoreCase(entryName) &&
                        !("META-INF/").equalsIgnoreCase(entryName)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Convert all the NonSOAP WSDLs under appLocation to SOAP WSDLs.
     */
    private String processWSDLs(String appLocation) {
        
        String newAppLocation;
        
        if(!(new File(appLocation).isDirectory())) {
            try {
                String workspaceDir =
                        JavaEEServiceEngineContext.getInstance().
                        getJBIContext().getWorkspaceRoot();
                
                newAppLocation = workspaceDir + File.separator + "exploded";
                
                explodeArchive(appLocation, newAppLocation);
            } catch(Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return appLocation;
            }
        } else { // Ideally the appLocation contents should be copied onto newAppLocation.
            newAppLocation = appLocation;
        }
        
        return
                WSDLConverter.convertWSDLs(newAppLocation).isEmpty()
                ? appLocation
                : newAppLocation;
    }
    
    // END : Utility methods.
}
