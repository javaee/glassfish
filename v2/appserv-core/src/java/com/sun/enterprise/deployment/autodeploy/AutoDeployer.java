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
 * AutoDeployer.java
 *
 *
 * Created on February 19, 2003, 10:21 AM
 */

package com.sun.enterprise.deployment.autodeploy;

import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchiveFactory;
import com.sun.enterprise.deployment.deploy.shared.ArchiveFactory;
import com.sun.enterprise.util.io.FileUtils;
import java.io.File;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanException;
import com.sun.enterprise.admin.server.core.jmx.InitException;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.constant.DeploymentConstants;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.instance.ServerManager;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import java.util.Properties;
import java.util.HashMap;
import java.util.Date;
import java.io.IOException;
/**
 * Handles the logic of deploying the module/app to the required destination.</br>
 * The destination can be modified by calling setTarget(), default is first non-admin instance entry from domain.xml</br>
 * The specific directory scanner can be set using setDirectoryScanner, default is AutoDeployDirectoryScanner</br>
 * Present logic is  using MBean ManagedServerInstance as actual deployment service.
 * @author vikas
 */
public class AutoDeployer {
    
    private  ObjectName  mbeanName = null;
    private  MBeanServer mbs=null;
    
    private Boolean verify=null;
    private Boolean forceDeploy=null;
    private Boolean enabled=null;
    private Boolean jspPreCompilation=null;
    private boolean renameOnSuccess = true;
    
    private String targetInstance=null;
    private static final Logger sLogger=AutoDeployControllerImpl.sLogger;
    private static StringManager localStrings =
            StringManager.getManager( AutoDeployer.class );
    private DirectoryScanner directoryScanner=null;
    
    private boolean cancelDeployment =false;
    
    /*
     *Represent the result of attempting autodeployment of a single file.
     *PENDING indicates the file could not be opened as an archive,
     *perhaps because the file was still in the process of being copied when
     *the autodeployer tried to work with it.  PENDING could also mean
     *that the file has changed size since the last time the autodeployer
     *checked it.  It's then reasonable to think it might grow further,
     *so autodeployer waits until the next time through to check it again.
     */
    protected static final int DEPLOY_SUCCESS = 1;
    protected static final int DEPLOY_FAILURE = 2;
    protected static final int DEPLOY_PENDING = 3;
    
    /**
     * archive factory used to create an archive for each file to be deployed -
     *the archive is needed by Depl. Facility
     */
    private final AbstractArchiveFactory archiveFactory = new ArchiveFactory();
    
    private AutodeployRetryManager retryManager = new AutodeployRetryManager();
    /**
     * Creates a new instance of AutoDeployer */
    public AutoDeployer() {
        
        ///initialize other attributes
        verify= new Boolean(false);
        jspPreCompilation=new Boolean(false);
        forceDeploy=new Boolean(true);
        enabled=new Boolean(true);
    }
    
    /**
     * Creates a new instance of AutoDeployer */
    public AutoDeployer(boolean verify, boolean jspPreCompilation) {
        
        ///initialize other attributes
        this.verify= new Boolean(verify);
        this.jspPreCompilation=new Boolean(jspPreCompilation);
        forceDeploy=new Boolean(true);
        enabled=new Boolean(true);
    }
    
    /**
     * set  DirectoryScanner which will be used for filtering out deployeble component
     * @return
     */
    public void setDirectoryScanner(DirectoryScanner ds) {
        directoryScanner=ds;
    }
    
    /**
     * set  target server where the autual deployment will be done
     * @return
     */
    public void setTarget(String target) {
        this.targetInstance = target;
        mbeanName = getMBean(targetInstance);
    }
    
    /**
     * If an archive is successfully autodeployed, file will not be
     * renamed to archive_deployed
     */
    public void disableRenameOnSuccess() {
        renameOnSuccess = false;
    }
    
    /**
     * If an archive is successfully autodeployed will be renamed
     * to archive_deployed
     */
    public void enableRenameOnSuccess() {
        // FIXME - Mahesh
        renameOnSuccess = true;
    }
    
    /**
     *Set whether this AutoDeployer should verify or not.
     *@param boolean verify setting
     */
    public void setVerify(boolean verify) {
        this.verify = new Boolean(verify);
    }
    
    /**
     *Set whether this AutoDeployer should precompile JSPs or not.
     *@param boolean precompilation setting
     */
    public void setJspPreCompilation(boolean jspPreCompilation) {
        this.jspPreCompilation = new Boolean(jspPreCompilation);
    }
    
    /**
     * do deployment for all the deployable components in autoDeployDir dir.
     * @return
     */
    public void deployAll(File autoDeployDir , boolean verify, boolean jspPreCompilation) throws AutoDeploymentException {
        this.verify= new Boolean(verify);
        this.jspPreCompilation=new Boolean(jspPreCompilation);
        deployAll(autoDeployDir);
    }

    /**
     * do deployment for all the deployable components in autoDeployDir dir.
     * @return
     */
    public void deployAll(File autoDeployDir) throws AutoDeploymentException {
        deployAll(autoDeployDir, false);
    }    
    
    /**
     * do deployment for all the deployable components in autoDeployDir dir.
     * @return
     */
    public void deployAll(File autoDeployDir, boolean includeSubDir) throws AutoDeploymentException {
        
        
        //create with default scanner
        if(directoryScanner==null) {
            directoryScanner=new AutoDeployDirectoryScanner();
        }
        
        //set target to default
        if(this.targetInstance == null) {
            setTarget(getDefaultTarget());
        }
        
        File [] files= null;
        
        //get me all deployable entities
        files= directoryScanner.getAllDeployableModules(autoDeployDir, includeSubDir);
        
        /*
         *The following pattern appears in each of the sections below, even though
         *it is described in detail only once here.  This can be improved
         *once autodeploy is converted to use the DeploymentFacility.
         *
         *To support slowly-copied files, the deployApplication (and deployWarmodule, and
         *deployRarModule and deployEjbModule) methods return 
         *    DEPLOY_SUCCESS  if the file was successfully autodeployed
         *    DEPLOY_FAILURE  if the file failed to be autodeployed
         *    DEPLOY_PENDING  if the file needs to be tried again later
         *
         *The marker files should be updated only if the result is success or
         *failure.  So for each file of each type, make a separate decision
         *about whether to record the result or not based on the result of
         *the deploy* method.  Note that the boolean is initialized to true
         *so that if an exception is thrown, the file's marker files will be
         *updated.
         */
        if(files != null) {
            for (int i=0; ((i < files.length) && !cancelDeployment);i++) {
                boolean okToRecordResult = true;
                try {
                    okToRecordResult = (deploy(files[i], autoDeployDir,
                    getNameFromFilePath(autoDeployDir,  
                    files[i])) != DEPLOY_PENDING);
                } catch (AutoDeploymentException ae) {
                    //ignore and move to next file
                } finally {
                    if(renameOnSuccess && okToRecordResult)
                        directoryScanner.deployedEntity(autoDeployDir, files[i]);
                }
            }
        } 
    }
    
    
    
    /**
     * do undeployment for all deleted applications in autoDeployDir dir.
     * @return
     */
    public void undeployAll(File autoDeployDir) throws AutoDeploymentException {
        
        
        //create with default scanner
        if(directoryScanner==null) {
            directoryScanner=new AutoDeployDirectoryScanner();
        }
        
        if(mbs == null) {
            mbs = getMBeanServer();
        }
        //set target to default
        if(this.targetInstance == null) {
            setTarget(getDefaultTarget());
        }
        
        File[] apps= null;
        
        //get me all apps
        apps= directoryScanner.getAllFilesForUndeployment(autoDeployDir);
        
        //deploying all applications
        if(apps !=null) {
            for (int i=0; i< apps.length && !cancelDeployment;i++) {
                try {
                    
                    boolean stat = this.undeployApplication(apps[i], autoDeployDir, 
                    getNameFromFilePath(autoDeployDir, apps[i]));
                    /*
                     * Before managing the marker file for the app, see if 
                     * the autodeployer is responsible for deleting this app
                     * file and, if so, delete it.
                     */
                    if (undeployedByRequestFile(apps[i])) {
                        cleanupAppAndRequest(apps[i]);
                    }
                    
                    if (stat)
                        markUndeployed(apps[i]);
                    else
                        markUndeployFailed(apps[i]);
                    
                    
                } catch (AutoDeploymentException ae) {
                    //ignore and move to next file
                    markUndeployFailed(apps[i]);
                } finally {
                    // Mark the application as undeployed both in the case of success & failure.
                    directoryScanner.undeployedEntity(autoDeployDir, apps[i]);
                }
            }
        }
        /////////end for apps
    }
    
    private boolean undeployedByRequestFile(File f) {
        return f instanceof AutoDeployedFilesManager.UndeployRequestedFile;
    }
    
    private void cleanupAppAndRequest(File f) {
        boolean logFine = sLogger.isLoggable(Level.FINE);

        /*
         * Clean up the application file or directory.
         */
        if (f.isDirectory()) {
            if (logFine) {
                sLogger.fine("Deleting autodeployed directory " + f.getAbsolutePath() + " by request");
            }
            FileUtils.liquidate(f);
        } else {
            if (logFine) {
                sLogger.fine("Deleting autodeployed file " + f.getAbsolutePath() + " by request");
            }
            FileUtils.deleteFile(f);
        }
        
        /*
         * Remove the undeploy request file.
         */
        File requestFile = AutoDeployedFilesManager.appToUndeployRequestFile(f);
        if (logFine) {
            sLogger.fine("Deleting autodeploy request file " + requestFile.getAbsolutePath());
        }
        FileUtils.deleteFile(requestFile);
    }
    
    private boolean undeployApplication(File applicationFile, File autodeployDir,
    String name) throws AutoDeploymentException {
        JBIAutoDeployer jad = JBIAutoDeployer.getInstance();
        String saName = jad.getServiceAssemblyName(applicationFile, autodeployDir);
        if (saName == null) {
            return undeployJavaEEArchive(applicationFile, name);
        } else {
            return undeployJbiArchive(saName);
        }
    }

    /**
     * Undeploy a Java EE archive.
     */
    private boolean undeployJavaEEArchive(File applicationFile, String name) throws AutoDeploymentException {
        boolean status = false;
        
        Properties props = getUndeployActionProperties(name);
        
        String[] signature  = new String[]{"java.util.Properties"};
        
        Object[] params     = new Object[]{props};
        sLogger.log(Level.INFO, "Autoundeploying application :" + name);
        status = invokeUndeploymentService(applicationFile.getAbsolutePath(), AutoDeployConstants.UNDEPLOY_METHOD,params,signature);;
        return status;
        
    }

    /**
     * Undeploy a JBI archive.
     */
    private boolean undeployJbiArchive(String saName) 
    throws AutoDeploymentException {
        try {
            sLogger.log(Level.INFO, "Autoundeploying application :" + saName);
            JBIAutoDeployer jad = JBIAutoDeployer.getInstance();
            JBIDeployer jd = jad.getDeployer();
            jd.undeploy(getMBeanServer(), saName);
            String msg = localStrings.getString
            ("enterprise.deployment.autodeploy.successfully_autoundeployed",
            saName);
            sLogger.log(Level.INFO, msg);
            return true;
        } catch (Exception e) {
            while (e instanceof MBeanException) {
                e = ((MBeanException)e).getTargetException();
            }
            String msg = localStrings.getString(
            "enterprise.deployment.autodeploy.autoundeploy_failed",saName);
            sLogger.log(Level.INFO, msg);
            msg = localStrings.getString(
            "enterprise.deployment.autodeploy.invocation_exception",saName);
            AutoDeploymentException ae;
            ae=new AutoDeploymentException(msg, e);
            sLogger.log(Level.INFO, ae.getMessage());
            throw ae;
        }
    }
    
    /**
     * set cancel flag, which  will ensure that only if there is any current deployment is in process,</br>
     * it will be completed but the deployer will not do any more deployment.
     * @return
     */
    public void setCancel(boolean value){
        cancelDeployment=value;
    }
    
    /**
     * get cancel flag value
     * @return
     */
    public boolean isCancelled(){
        return cancelDeployment;
    }
    
    /**
     * setup the required internal variables
     */
    
    void init() throws AutoDeploymentException{
        // this sets 'mbs'
        getMBeanServer();
        
        if(targetInstance == null) {
            // side effect -- this also sets mbeanName...
            setTarget(getDefaultTarget());
        }
    }
        
    /**
     *Deploy any type of module.
     *@param file Absolute path of the file to be deployed
     *@param name the module ID of the applications
     *@return status of the deployment attempt: DEPLOY_SUCCESS, DEPLOY_FAILURE, or DEPLOY_PENDING 
     *@throws AutoDeploymentException if any invoked method throws an exception
     */
    protected int deploy(File deployablefile, File autodeployDir, 
    String name) throws AutoDeploymentException {
        
        int status=DEPLOY_FAILURE;
        if(cancelDeployment) {
            return status;
        }
        String file=deployablefile.getAbsolutePath();
        status = retryManager.testFileAsArchive(file);
        if (status != DEPLOY_SUCCESS) {
            return status;
        }

        JBIAutoDeployer jad = JBIAutoDeployer.getInstance();

        String msg = localStrings.getString(
        "enterprise.deployment.autodeploy.selecting_file",
        deployablefile.getAbsolutePath());
        sLogger.log(Level.INFO, msg);
        if (jad.isJbiArchive(deployablefile)) {
            return deployJbiArchive(deployablefile);
        } else {
            return deployJavaEEArchive(deployablefile, autodeployDir, name);
        }

    }

    /**
     * Deploy a JBI archive.
     */
    int deployJbiArchive(File file) throws AutoDeploymentException {
        int returnStatus=DeploymentStatus.FAILURE;
        try {
            JBIAutoDeployer jad = JBIAutoDeployer.getInstance();
            JBIDeployer jd = jad.getDeployer();
            String saName = jad.getServiceAssemblyName(file);
            jd.deploy(getMBeanServer(), file, saName);
            returnStatus = DeploymentStatus.SUCCESS;
            return DEPLOY_SUCCESS;
        } catch (Exception e) {
            while (e instanceof MBeanException) {
                e = ((MBeanException)e).getTargetException();
            }
            String msg = localStrings.getString
            ("enterprise.deployment.autodeploy.invocation_exception",file);
            AutoDeploymentException ae;
            ae=new AutoDeploymentException(msg, e);
            sLogger.log(Level.INFO, ae.getMessage());
            throw ae;
        } finally {
            markFileAfterDeployment(file, returnStatus);   
        }
    }

    /**
     * Deploy a Java EE archive.
     */
    int deployJavaEEArchive(File deployablefile, File autodeployDir, 
    String name) throws AutoDeploymentException {
        int status=DEPLOY_FAILURE;
        // if it's redeploy, first undeploy, then deploy
        if (isModuleDeployed(name)) {
            boolean result = 
            undeployApplication(deployablefile, autodeployDir, name);
            if (!result) {
                return DEPLOY_FAILURE;
            }
        }

        Properties props = getDeployActionProperties(deployablefile,name);
        String[] signature  = new String[]{"java.util.Properties"};
        
        Object[] params     = new Object[]{props};
        
        //invoke
        if (invokeDeploymentService(deployablefile, AutoDeployConstants.DEPLOY_METHOD,params,signature)) {
            status = DEPLOY_SUCCESS;
        } else {
            status = DEPLOY_FAILURE;
        }
        return status;
    }

    
    boolean invokeDeploymentService(File deployablefile, String action,
            Object[] params,String[] signature)     throws AutoDeploymentException {
        
        String file=deployablefile.getAbsolutePath();
        boolean status=false;
        int returnStatus = DeploymentStatus.FAILURE;
        
        
        //invoke
        try {
            Object result=getMBeanServer().invoke(getMBeanName(),action, params,signature);
            returnStatus = parseResult(result);
        } catch (Exception e) {
            while (e instanceof MBeanException) {
                e = ((MBeanException)e).getTargetException();
            }
            returnStatus = DeploymentStatus.FAILURE;
            String msg = localStrings.getString
            ("enterprise.deployment.autodeploy.invocation_exception",file);
            AutoDeploymentException ae;
            ae=new AutoDeploymentException(msg, e);
            sLogger.log(Level.INFO, ae.getMessage());
            throw ae;
        } finally {
            status = markFileAfterDeployment(deployablefile, returnStatus);   
        }
        return status;
        
    }

    private boolean markFileAfterDeployment(File file, int returnStatus){
        String msg = null;
        boolean status = false;
        if(returnStatus == DeploymentStatus.SUCCESS) {
            if(renameOnSuccess) {
                markDeployed(file);
            }
            status = true;
            msg = localStrings.getString("enterprise.deployment.autodeploy.successfully_autodeployed",file);
            sLogger.log(Level.INFO, msg);
        } else if (returnStatus == DeploymentStatus.WARNING) {
            if(renameOnSuccess) {
                markDeployed(file);
            }
            status = true;
            msg = localStrings.getString("enterprise.deployment.autodeploy.warning_autodeployed",file);
            sLogger.log(Level.INFO, msg);
        } else if (returnStatus == DeploymentStatus.FAILURE) {
            markDeployFailed(file);
            status = false;
            msg = localStrings.getString("enterprise.deployment.autodeploy.autodeploy_failed",file);
            sLogger.log(Level.INFO, msg);
        }
        return status;
    }
    
    /**
     * get the MBean Name.
     * bnevins 9/16/03
     */
    
    final ObjectName getMBeanName() throws AutoDeploymentException {
        if(mbeanName == null)
            throw new AutoDeploymentException("Internal Error: mbeanName is null");
        
        return mbeanName;
    }
    
    /**
     * get the MBeanServer.  Initialize it if neccessary.
     * Note that the 'final' allows the compiler to inline
     * the method.  So this will be just as fast as accessing the variable
     * directly.
     * bnevins 9/16/03
     */
    
    final MBeanServer getMBeanServer() throws AutoDeploymentException {
        if(mbs == null)
            mbs = MBeanServerFactory.getMBeanServer();
        
        return mbs;
    }
    
    
    private ObjectName getMBean(String instanceName) {
        ObjectName mbean=null;
        try {
            mbean = new ObjectName("com.sun.appserv:type=applications,category=config");
        } catch(Exception e) {
            sLogger.log(Level.SEVERE, "enterprise.deployment.backend.autoDeploymentFailure",
                    new Object[] {e.getMessage()});
        }
        return mbean;
        
    }
    
    private boolean isModuleDeployed (String moduleID) {
        if (moduleID == null) {
            return false;
        }

        try {
            String[] signature = new String[] {"java.lang.String"};
            Object[] params = new Object[] {moduleID};
            Object result = getMBeanServer().invoke(getMBeanName(), 
                "getModuleType", params, signature);

            if (result != null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private String  getDefaultVirtualServer(String instanceName) {
        String virtualServer=null;
        try {
            ConfigContext context = getConfigContext();
            //Domain domain = (Domain)context.getRootConfigBean();
            //HttpService service = getHttpService(domain,instanceName);
            HttpService service = ServerBeansFactory.getHttpServiceBean(context);
            if(service !=null){
                HttpListener[] hlArray = service.getHttpListener();
                //check not needed since there should always be atleast 1 httplistener
                //if you don't find one, use first one.
                HttpListener ls = null;
                if(hlArray != null && hlArray.length > 0){
                    ls=hlArray[0];
                    //default is the first one that is enabled.
                    for(int i = 0;i<hlArray.length;i++) {
                        if(hlArray[i].isEnabled()) {
                            ls = hlArray[i];
                            break;
                        }
                    }
                }
                if(ls!=null)
                    virtualServer = ls.getDefaultVirtualServer();
            }
        } catch(ConfigException e){
            String msg = localStrings.getString("enterprise.deployment.autodeploy.unable_to_get_virtualserver");
            sLogger.log(Level.WARNING, msg+e.getMessage());
        } catch(Exception e){String msg = localStrings.getString("enterprise.deployment.autodeploy.unable_to_get_virtualserver");
        sLogger.log(Level.WARNING, msg+e.getMessage());
        }
        
        return virtualServer;
        
    }

    private String getDefaultTarget() throws AutoDeploymentException {
        try{
            ConfigContext confContext = getConfigContext();
            Domain domain = (Domain)confContext.getRootConfigBean();
            Servers svrs = domain.getServers();
            Server[] svrArr = svrs.getServer();
            int size = svrArr.length;
            String targetName = null;
            /*I am putting this logic specific for TP, as its decided to deploy directly
             *to instsnce, and not DAS. It need to be changed to next release
             */
            for(int i = 0 ; i< size; i++) {
                if(!svrArr[i].getName().equals("")) {
                    targetName = svrArr[i].getName();
                    break;
                }
            }
            if(targetName == null) {
                sLogger.log(Level.SEVERE, "enterprise.deployment.backend.autoDeploymentFailure",
                        new Object[] {"Target server not found"});
                        throw new AutoDeploymentException("Target Server not found");
            }
            
            return targetName;
        }catch(Exception ex) {
            return null;
        }
    }
    
    
    private ConfigContext getConfigContext() throws ConfigException {
        /*InstanceEnvironment instanceEnvironment =
        new InstanceEnvironment(instanceName);
        String fileUrl  = instanceEnvironment.getConfigFilePath();
         
        ConfigContext configContext   =
        ConfigFactory.createConfigContext(fileUrl);*/
        ServerContext serverContext = AdminService.getAdminService().getContext();
        ConfigContext context = serverContext.getConfigContext();
        return context;
    }
    
    
    
    protected int parseResult(Object result) {
        if(result!=null && result instanceof DeploymentStatus) {
            DeploymentStatus status = (DeploymentStatus) result;
            if (status.getStatus()>DeploymentStatus.WARNING) {
                return DeploymentStatus.SUCCESS;
            } else {
                // ok we got either a warning or an error.
                // parse the deployment status and retrieve failure/warning msg
                ByteArrayOutputStream bos =
                        new ByteArrayOutputStream();
                PrintWriter pw = new PrintWriter(bos);
                DeploymentStatus.parseDeploymentStatus(status, pw);
                byte[] statusBytes = bos.toByteArray();
                String statusString = new String(statusBytes);
                
                if (status.getStatus()==DeploymentStatus.WARNING) {
                    // warning, let's log and continue
                    String msg = localStrings.getString(
                            "enterprise.deployment.warning_occured", statusString);
                    sLogger.log(Level.WARNING, msg);
                    return DeploymentStatus.WARNING;
                } else if (status.getStatus()==DeploymentStatus.FAILURE) {
                    sLogger.log(Level.SEVERE, "enterprise.deployment.backend.autoDeploymentFailure",
                            new Object[] {statusString});
                            return DeploymentStatus.FAILURE;
                }
            }
        }
        return DeploymentStatus.FAILURE;
    }
    
    static String getNameFromFilePath(File autodeployDir, File filePath) {   //creating module name as file name
        
        File parent = filePath.getParentFile();
        String moduleName = null;
        while (!parent.getAbsolutePath().equals(autodeployDir.getAbsolutePath())) {
            if (moduleName==null) {
                moduleName = parent.getName();
            } else {
                moduleName = parent.getName()+"_"+moduleName;
            }
            parent = parent.getParentFile();
        }
        if (moduleName==null) {
            moduleName = filePath.getName();
        } else {
            moduleName = moduleName + "_" + filePath.getName();
        }
        int toIndex = moduleName.lastIndexOf('.');
        if (toIndex > 0) {
            moduleName = moduleName.substring(0, toIndex);
        }
        return moduleName;
    }
    
    // Methods for creating operation status file(s)
    protected void markDeployed(File f) {
        try {
            deleteAllMarks(f);
            getDeployedFile(f).createNewFile();
        } catch (Exception e) { 
            //ignore 
        }
    }
    
    protected void markDeployFailed(File f) {
        try {
            deleteAllMarks(f);
            getDeployFailedFile(f).createNewFile();
        } catch (Exception e) { 
            //ignore 
        }
    }
    
    protected void markUndeployed(File f) {
        try {
            deleteAllMarks(f);
            getUndeployedFile(f).createNewFile();
        } catch (Exception e) { 
            //ignore 
        }
    }
    
    protected void markUndeployFailed(File f) {
        try {
            deleteAllMarks(f);
            getUndeployFailedFile(f).createNewFile();
        } catch (Exception e) { 
            //ignore 
        }
    }
    
    protected void deleteAllMarks(File f) {
        try {
            getDeployedFile(f).delete();
            getDeployFailedFile(f).delete();
            getUndeployedFile(f).delete();
            getUndeployFailedFile(f).delete();
        } catch (Exception e) { 
            //ignore 
        }
    }
    
    protected File getDeployedFile(File f) {
        String absPath = f.getAbsolutePath();
        File ret = new File(absPath + AutoDeployConstants.DEPLOYED);
        return ret;
    }
    
    protected File getDeployFailedFile(File f) {
        String absPath = f.getAbsolutePath();
        File ret = new File(absPath + AutoDeployConstants.DEPLOY_FAILED);
        return ret;
    }
    
    protected File getUndeployedFile(File f) {
        String absPath = f.getAbsolutePath();
        File ret = new File(absPath + AutoDeployConstants.UNDEPLOYED);
        return ret;
    }
    
    protected File getUndeployFailedFile(File f) {
        String absPath = f.getAbsolutePath();
        File ret = new File(absPath + AutoDeployConstants.UNDEPLOY_FAILED);
        return ret;
    }
    
    private boolean invokeUndeploymentService(String appFile, String action,
            Object[] params,String[] signature)     throws AutoDeploymentException {
        
        boolean status=false;
        int returnStatus = DeploymentStatus.FAILURE;
        //invoke
        try {
            Object result=getMBeanServer().invoke(getMBeanName(),action, params,signature);
            returnStatus = parseResult(result);
        } catch (Exception e) {
            while (e instanceof MBeanException) {
                e = ((MBeanException)e).getTargetException();
            }
            returnStatus = DeploymentStatus.FAILURE;
            String msg = localStrings.getString("enterprise.deployment.autodeploy.invocation_exception",appFile);
            AutoDeploymentException ae;
            ae=new AutoDeploymentException(msg, e);
            sLogger.log(Level.INFO, ae.getMessage());
            throw ae;
        } finally {
            if(returnStatus == DeploymentStatus.SUCCESS) {
                status = true;
                String msg = localStrings.getString("enterprise.deployment.autodeploy.successfully_autoundeployed",appFile);
                sLogger.log(Level.INFO, msg);
            }  else if (returnStatus == DeploymentStatus.WARNING) {
                status = true;
                String msg = localStrings.getString("enterprise.deployment.autodeploy.warning_autoundeployed",appFile);
                sLogger.log(Level.INFO, msg);
            } else if (returnStatus == DeploymentStatus.FAILURE) {
                status = false;
                String msg = localStrings.getString("enterprise.deployment.autodeploy.autoundeploy_failed",appFile);
                sLogger.log(Level.INFO, msg);
            }
        }
        return status;
        
    }
    
    
    
    protected Properties getDeployActionProperties(File deployablefile, String ID){
        
        DeploymentProperties dProps = new DeploymentProperties();
        dProps.setArchiveName(deployablefile.getAbsolutePath());
        dProps.setName(ID);
        dProps.setEnable(enabled.booleanValue());
        dProps.setVirtualServers(getDefaultVirtualServer(targetInstance));
        dProps.setForce(forceDeploy.booleanValue());
        dProps.setVerify(verify.booleanValue());
        dProps.setPrecompileJSP(jspPreCompilation.booleanValue());
        dProps.setResourceAction(DeploymentProperties.RES_DEPLOYMENT);
        dProps.setResourceTargetList("server");

        return (Properties)dProps;
    }

    protected Properties getUndeployActionProperties(String name){
        DeploymentProperties dProps = new DeploymentProperties();
        dProps.setName(name);
        dProps.setResourceAction(DeploymentProperties.RES_UNDEPLOYMENT);
        dProps.setResourceTargetList("server");

        return (Properties)dProps;
    }

    /**
     *Manages retrying of autodeployed files in case a file is copied slowly.
     *<p>
     *If a file is copied into the autodeploy directory slowly, it can appear there
     *before the copy has finished, causing the attempt to autodeploy it to fail.
     *This class encapsulates logic to retry such files on successive loops through
     *the autodeployer thread, reporting failure only if the candidate file remains
     *stable in size and cannot be opened after a period of time which defaults below
     *and is configurable using the config property name defined below.
     *<p>
     *The main public entry point is the openAsArchive method, which accepts a file and
     *tries to open it as an archive.  (The archive returned will be useful when
     *AutoDeployer is converted to use the DeploymentFacility.) When AutoDeployer tries
     *and fails to open a file as an archive, it records
     *that fact internally.  The manager adds to or updates
     *a map that describes all such failed files.  If AutoDeployer previously
     *reported failures to open the file and the file's size has been stable
     *for a configurable period of time, then the manager concludes that the
     *file is not simply a slow-copying file but is truly invalid.  In that case
     *it throws an exception.  When a file fails to open as an archive the first time,
     *or if it has failed to open before but its size has changed within the configurable
     *time, then the manager returns a null to indicate that we need to wait before trying
     *to process the file.  Once a file opens successfully, any record of that file is removed
     *from the map.
     */
    class AutodeployRetryManager {
        
        /**
         *Specifies the config property name and default and actual values for the retry limit.
         */
        private final String RETRY_LIMIT_NAME = "com.sun.appserv.autodeploy.retry.limit";
        private final long RETRY_LIMIT_DEFAULT = 30 * 1000; // 30 seconds
        private final long RETRY_LIMIT = Long.getLong(RETRY_LIMIT_NAME, RETRY_LIMIT_DEFAULT).longValue();
        
        /** Maps an invalid File to its corresponding Info object. */
        private HashMap invalidFiles = new HashMap();
        
        /**
         *Records pertinent information about a file judged to be invalid - that is,
         *unrecognizable as a legal archive:
         *<ul>
         *<li>the file object,
         *<li>the length of the file at the most recent check of the file,
         *<li>the time after which no more retries should be attempted.
         *</ul>
         */
        class Info {
            /** File recorded in this Info instance */
            private File file = null;
            
            /** File length the previous time this file was reported as
             * invalid. */
            private long recordedLength = 0;
            
            /** Timestamp after which all retries on this file should stop. */
            private long retryExpiration = 0;
            
            /**
             *Creates a new instance of the Info object for the specified file.
             *@param File to be recorded
             */
            public Info(File file) {
                this.file = file;
                update();
            }
            
            /**
             *Updates the Info object with the file's length and recomputes (if
             *appropriate) the retry due date and the expiration.
             */
            private void update() {
                long currentLength = file.length();
                if (recordedLength != currentLength) {
                    /*
                     *The file's size has changed.  Reset the time for this
                     *file's expiration.
                     */
                    long now = new Date().getTime();
                    retryExpiration = now + RETRY_LIMIT;
                }
                /*
                 *In all cases, update the recorded length with the file's
                 *actual current length.
                 */
                recordedLength = currentLength;
            }
            
            /**
             *Reports whether the file represented by the Info instance is
             *eligible for retry.
             *<p>
             *A file is eligible for retry if its length has changed since
             *the last recorded failure or if the length is stable but has not
             *yet been stable for the expiration period.
             *@return if the file should remain as a candidate for retry
             */
            private boolean isEligibleForRetry() {
                long now = new Date().getTime();
                boolean result = (now < retryExpiration);
                return result;
            }
            
        }
        
        /**
         *Tests to see if the file can be opened as an archive.
         *<p>
         *This method will not be needed if the main autodeployer uses
         *the deployment facility.  This is because the deployment facility
         *method invocations use an archive, so the openFileAsArchive method
         *will be used instead of this one.
         *@param file spec to be tested
         *@return whether the file was opened as an archive (DEPLOY_SUCCESS), 
         *   was not but should be tried later (DEPLOY_PENDING), or was
         *   not and should not be tried later (DEPLOY_FAILURE).
         *@throws AutoDeploymentException if an error occurred closing the archive
         */
        int testFileAsArchive(String file) throws AutoDeploymentException {
            int result;
            try {
                File inFile = new File(file);
                if (!inFile.isDirectory()) {
                    // either a j2ee jar file or a .class file
                    if (file.endsWith(".class")) {
                        return DEPLOY_SUCCESS;
                    }
                }
                AbstractArchive arch = openFileAsArchive(file);
                if (arch != null) {
                    result = DEPLOY_SUCCESS;
                    arch.close();
                } else {
                    result = DEPLOY_PENDING;
                }
            } catch (AutoDeploymentException ade) {
                result = DEPLOY_FAILURE;
            } catch (IOException ioe) {
                String msg = localStrings.getString("enterprise.deployment.autodeploy.error_closing_archive", file);
                throw new AutoDeploymentException(msg, ioe);
            }
            return result;
        }
        
        /**
         *Opens the specified file as an archive and tracks files that have
         *failed to open as archives.
         *@param String the file to try opening as an archive
         *@return the AbstractArchive for the file (null if it shoudl be tried later)
         *@throws AutoDeploymenException if the manager has been unable to open
         *the file as an archive past the configured expiration time
         */
        AbstractArchive openFileAsArchive(String file) throws AutoDeploymentException {
            AbstractArchive archive = null;
            File f = new File(file);
            if (shouldOpen(f)) {
                try {
                    /*
                     Try to open the file as an archive and then remove the file, if
                     *it is present, from the map tracking invalid files.
                     */
                    archive = archiveFactory.openArchive(file);
                    
                    recordSuccessfulOpen(f);
                    
                } catch (IOException ioe) {
                    String errorMsg = null;
                    /*
                     *If the archive variable was not assigned a non-null, then the file
                     *is not a valid archive.
                     */
                    if (archive == null) {
                        boolean failedPreviously = recordFailedOpen(f);
                        if ( ! failedPreviously) {
                            Info info = get(f);
                            errorMsg = localStrings.getString("enterprise.deployment.autodeploy.error_opening_start_retry", file, new Date(info.retryExpiration).toString());
                            sLogger.log(Level.INFO, errorMsg);
                        }
                    }
                }
            }
            return archive;
        }
        
        /**
         *Retrieves the Info object describing the specified file.
         *@param File for which the Info object is requested
         *@return Info object for the specified file
         *null if the file is not recorded as invalid
         */
        Info get(File file) {
            Info info = (Info) invalidFiles.get(file);
            return info;
        }
        
        /**
         *Indicates whether the AutoDeployer should try opening the specified
         *file as an archive.
         *<p>
         *The file should be opened if this retry manager has no information
         *about the file or if information is present and the file size is
         *unchanged from the previous failure to open.
         *@return if the file should be opened as an archive
         */
        private boolean shouldOpen(File file) {
            boolean result = true; // default is true in case the file is not being monitored
            String msg = null;
            boolean loggable = sLogger.isLoggable(Level.FINE);
            Info info = (Info) invalidFiles.get(file);
            if (info != null) {
                result = (file.length() == info.recordedLength);
                if (loggable) {
                    if (result) {
                        msg = localStrings.getString("enterprise.deployment.autodeploy.try_stable_length", file.getAbsolutePath());
                    } else {
                        msg = localStrings.getString("enterprise.deployment.autodeploy.no_try_unstable_length", file.getAbsolutePath(), String.valueOf(file.length()));
                    }
                }
                info.update();
            } else {
                if (loggable) {
                    msg = localStrings.getString("enterprise.deployment.autodeploy.try_not_monitored", file.getAbsolutePath());
                }
            }
            if (loggable) {
                sLogger.log(Level.FINE, msg);
            }
            return result;
        }
        
        /**
         *Records the fact that the autodeployer tried but failed to open this file
         *as an archive.
         *@param File the file that could not be interpreted as a legal archive
         *@return true if the file was previously recognized as an invalid one
         *@throws AutoDeploymentException if the file should no longer be retried
         */
        private boolean recordFailedOpen(File file) throws AutoDeploymentException {
            boolean fileAlreadyPresent;
            /*
             *Try to map the file to an existing Info object for it.
             */
            Info info = get(file);
            if (info == null) {
                /*
                 *This file was not previously noted as invalid.  Create a new
                 *entry and add it to the map.
                 */
                fileAlreadyPresent = false;
                info = new Info(file);
                invalidFiles.put(file, info);
                if (sLogger.isLoggable(Level.FINE)) {
                    String msg = localStrings.getString("enterprise.deployment.autodeploy.begin_monitoring", file.getAbsolutePath(), new Date(info.retryExpiration).toString());
                    sLogger.log(Level.FINE, msg);
                }
            } else {
                /*
                 *The file has previously been recorded as invalid.  Update
                 *the recorded info.
                 */
                fileAlreadyPresent = true;
                info.update();
                
                /*
                 *If the file is still eligible for later retries, just return.
                 *If the file size has been stable too long, then conclude that
                 *the file is just an invalid archive and throw an exception
                 *to indicate that.
                 */
                boolean loggable = sLogger.isLoggable(Level.FINE);
                if (info.isEligibleForRetry()) {
                    /*
                     *Just log that the file is still being monitored.
                     */
                    if (loggable) {
                        String msg = localStrings.getString("enterprise.deployment.autodeploy.continue_monitoring", file.getAbsolutePath(), new Date(info.retryExpiration).toString());
                        sLogger.log(Level.FINE, msg);
                    }
                } else {
                    /*
                     *Log that monitoring of this file will end, remove the file from
                     *the map, and throw an exception
                     *with the same message.
                     */
                    String msg = localStrings.getString("enterprise.deployment.autodeploy.abort_monitoring",
                            file.getAbsolutePath(), String.valueOf(RETRY_LIMIT));
                    if (loggable) {
                        sLogger.log(Level.FINE, msg);
                    }
                    invalidFiles.remove(file);
                    throw new AutoDeploymentException(msg);
                }
            }
            return fileAlreadyPresent;
        }
        
        /**
         *Marks a file as successfully opened and no longer subject to retry.
         *@param File that is no longer invalid
         *@return true if the file had been previously recorded as invalid
         */
        private boolean recordSuccessfulOpen(File file) {
            if (sLogger.isLoggable(Level.FINE)) {
                String msg = localStrings.getString("enterprise.deployment.autodeploy.end_monitoring", file.getAbsolutePath());
                sLogger.log(Level.FINE, msg);
            }
            return (invalidFiles.remove(file)) != null;
        }
    }
    
    
    
}

