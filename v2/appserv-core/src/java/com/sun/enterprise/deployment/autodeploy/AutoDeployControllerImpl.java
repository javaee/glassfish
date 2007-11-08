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
 * AutoDeployMoniter.java
 *
 * Created on February 19, 2003, 10:21 AM
 *
 */

package com.sun.enterprise.deployment.autodeploy;


import com.sun.enterprise.deployment.backend.DeploymentLogger;
import java.util.Vector;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Enumeration;
import java.io.File;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.enterprise.config.ConfigContextEventListener;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.util.i18n.StringManager;


/**
 * Main class for autodeploy service. It uses jdk timer class (java.util.Timer) as scheduler.</br>
 * Presently its not implemented as MBean but later on, we can add the MBean behaviour easily.</br>
 *
 * Implements ConfigContextEventListener for handling config changes. Supports auto deployment, with  </br>
 * multiple source directories, using a single java.util.Timer and a single AutoDeployTask instance.</br>
 * So there will be only one thread running for deployment, even when yoy have more than one input</br>
 * source directory. It will complete deployment of all the modules/apps from one directory and then </br>
 * moves on to next.
 *
 * @author vikas
 */
public class AutoDeployControllerImpl implements AutoDeployController,ConfigContextEventListener {
    
    
    
    /**
     *vector (of File objects) containing the list of source directories
     */
    private  Vector autodeployDirs=new Vector();
    private  long pollingInterval = AutoDeployConstants.MIN_POOLING_INTERVAL;
    private boolean verify = false;
    private boolean preJspCompilation = false;
    
    private Timer timer =null;
    private AutoDeployTask deployTask=null;
    
    public static final Logger sLogger=DeploymentLogger.get();
    private static StringManager localStrings =
                            StringManager.getManager( AutoDeployControllerImpl.class );
    
    
    public AutoDeployControllerImpl(String autodeployDir, long pollingInterval) throws AutoDeploymentException {
        try {
            addAutoDeployDir(autodeployDir);
            setPollingInterval(pollingInterval);
        } catch(AutoDeploymentException ae){
            sLogger.log(Level.SEVERE, "enterprise.deployment.backend.autoDeploymentStartFailure");
            throw ae;
        }
    }
    
    public AutoDeployControllerImpl(String[] autodeployDirs, long pollingInterval) throws AutoDeploymentException {
        try {
            for (int i=0;i<autodeployDirs.length;i++) {
                addAutoDeployDir(autodeployDirs[i]);
            }
            setPollingInterval(pollingInterval);
        } catch(AutoDeploymentException ae){
            sLogger.log(Level.SEVERE, "enterprise.deployment.backend.autoDeploymentStartFailure");
            throw ae;
        }
        
    }
        
    /**
     * start autodeployment.
     */
    public boolean enableAutoDeploy() {
        timer = new Timer();
        deployTask=new AutoDeployTask();
        timer.schedule(deployTask,0,pollingInterval*1000);
        String msg = localStrings.getString("enterprise.deployment.autodeploy.autoDeployment_service_enabled");
        sLogger.log(Level.FINE, msg+System.currentTimeMillis());
        return true;
        
    }
    
    /**
     * stop/disable autodeployment thread.
     */
    public boolean disableAutoDeploy() {
        if(deployTask!=null) {
            deployTask.cancel();
        }
        if(timer!=null) {
            timer.cancel();
        }
        String msg = localStrings.getString("enterprise.deployment.autodeploy.autoDeployment_service_disabled");
        sLogger.log(Level.INFO, msg);
        return true;
        
        
    }
    
    /**
     * get all the input source directory.
     */
    public String[] getAllAutoDeployDirs() {
        if(autodeployDirs != null && ! autodeployDirs.isEmpty()) {
            String[] dirs=new String[autodeployDirs.size()];
            int i=0;
            for (Enumeration list = autodeployDirs.elements() ; list.hasMoreElements() ;i++) {
                
                dirs[i]=((File)list.nextElement()).getAbsolutePath();
            }
            
            return dirs;
        }else {
            return null;
        }
        
    }
    
    /**
     * add input source directory. to the list(Vector).
     */
    public void addAutoDeployDir(String autodeployDir) throws AutoDeploymentException {
        if(!validateDir(autodeployDir)) {
           String msg = localStrings.getString("enterprise.deployment.autodeploy.invalid_source_dir",autodeployDir);
           sLogger.log(Level.INFO, msg);
        }        
        if((locateAlreadyExistingDir(autodeployDir)== -1)){ 
            autodeployDirs.add(new File(autodeployDir));
        } else {
        String msg = localStrings.getString("enterprise.deployment.autodeploy.duplicate_source_dir",autodeployDir);
        sLogger.log(Level.WARNING, msg);                
        //ignore, dir already exist
        }
        
    }
    
    
    /**
     * remove input source dir. from the list(Vector).
     */
    
    public void removeAutoDeployDir(String autodeployDir) {
        int location=locateAlreadyExistingDir(autodeployDir);
        if(location>=0) {
            this.autodeployDirs.remove(location);
        }
    }
    
    /**
     * get polling duration.
     */
    public long getPollingInterval() {
        return pollingInterval;
        
    }
    
    /**
     * set polling interval, polling interval can not be -ve. && more than
     */
    public void setPollingInterval(long pollInterval) throws AutoDeploymentException {
        
        if(validatePollingInterval(pollInterval)) {
            this.pollingInterval=pollInterval;
        } else{
            String sInterval= new String(""+pollInterval);
            String sMinInterval= new String(""+AutoDeployConstants.MIN_POOLING_INTERVAL);
            String msg = localStrings.getString("enterprise.deployment.autodeploy.invalid_pooling_interval", sInterval, sMinInterval);
            sLogger.log(Level.INFO, msg+pollInterval);
            throw new AutoDeploymentException(msg+pollInterval);
        }
    }
    
    /**
     * is verify flag enabled or not. If enabled verification happnes before </br>
     * every deployment.
     */
    public boolean isVerifyEnabled() {
        return verify;
    }
    
    /**
     * set verify flag.
     */
    public void setVerify(boolean verify) {
        this.verify=verify;
    }
    
    /**
     * is PreJspCompilation flag enabled or not. If enabled PreJspCompilation compilation ignored.</br>
     *
     */
    public boolean isPreJspCompilationEnabled() {
        return preJspCompilation;
    }
    
    /**
     * set PreJspCompilation flag.
     */
    public void setPreJspCompilation(boolean preJspCompilation) {
        this.preJspCompilation=preJspCompilation;
    }
    
    
    private boolean validateDir(String dir) {
        boolean valid=false;
        File autodeployDir=new File(dir);
        if(autodeployDir != null && autodeployDir.exists()
                && autodeployDir.isDirectory()&& autodeployDir.canWrite() && 
                autodeployDir.canRead() /*&& (locateAlreadyExistingDir(dir)== -1)*/) {
            valid=true;
        }
        return valid;
        
    }
    
    private int locateAlreadyExistingDir(String dir) {
        //return -1 if the vector autoDeployDirs dont have this dir
        int location=-1;
        String extingDirs[]=getAllAutoDeployDirs();
        if(extingDirs!=null) {
            for (int i=0; i<extingDirs.length ; i++) {
                if(dir.equalsIgnoreCase(extingDirs[i])) {
                    location=i; //I got this dir in list
                    break;
                }
            }
            
        }
        return location;
    }
    
    private boolean validatePollingInterval(long time) {
        boolean valid=false;
        
        if(time >= AutoDeployConstants.MIN_POOLING_INTERVAL) {
            valid= true;
        }
        return valid;
        
    }
    
    
    /**
     *Implement notification listner.</br>
     * before config add, delete, set, update or flush. type is in ccce
     */
    public void preChangeNotification(ConfigContextEvent ccce) {
        //not implemented
    }
    
    
    /**
     *Implement notification listner.</br>
     * after config add, delete, set, update or flush. type is in ccce
     */
    public void postChangeNotification(ConfigContextEvent ccce) {
        //not implemented
    }
    
    /**
     *Implement notification listner.</br>
     * before config add, delete, set, update or flush. type is in ccce
     */
    public void preAccessNotification(ConfigContextEvent ccce) {
        //not implemented
    }
    
    /**
     *Implement notification listner.</br>
     * after config add, delete, set, update or flush. type is in ccce
     */
    public void postAccessNotification(ConfigContextEvent ccce) {
        //not implemented
    }
    
    
    
    
    private class AutoDeployTask extends TimerTask {
        
        /**
         * Implementation of TimerTask run method. It will call deployAll() </br>
         * method of AutoDeployer for every source dirs. </br>
         *
         *for (Enumeration list = autodeployDirs.elements() ; list.hasMoreElements() ;) { </br>
         *               File sourceDir = (File)list.nextElement(); </br>
         *              AutoDeployer.deployAll(sourceDir,verify); </br>
         *           }
         *
         */
        private AutoDeployer currentDeployer= null;
        
        private DirectoryScanner directoryScanner = null;
        
        public void run() {
            
            try{
                if (sLogger.isLoggable(Level.FINEST)) {
                    String msg = localStrings.getString("enterprise.deployment.autodeploy.thread_started");
                    sLogger.log(Level.FINEST, msg + System.currentTimeMillis());
                }
                
                if(autodeployDirs != null && ! autodeployDirs.isEmpty()) {
                    
                    for (Enumeration list = autodeployDirs.elements() ; list.hasMoreElements() ;) {
                        
                        if(currentDeployer !=null && currentDeployer.isCancelled()) {//if last cycle is terminated using "cancel" signal let me JUST BREAK
                            break;
                        }
                        
                        File sourceDir = (File)list.nextElement();
                        if (sLogger.isLoggable(Level.FINEST)) {
                            sLogger.log(Level.FINEST, 
                                        localStrings.getString("enterprise.deployment.autodeploy.processing_source_directory",sourceDir));
                        }
                        // quick check to see if our only file is the .autodeploystatus
                        if (sourceDir.isDirectory()) {
                            File[] sourceDirFiles = sourceDir.listFiles();
                            if (sourceDirFiles.length==1 
                                && AutoDeployedFilesManager.STATUS_DIR_NAME.equals(sourceDirFiles[0].getName())) {
                                continue;
                            }
                        }

                        //making a quick check that this dir contain any new deployable entity.
                        AutoDeployer deployer = getAutoDeployer();
                        try {
                            deployer.deployAll(sourceDir, true);
                            deployer.undeployAll(sourceDir);
                        }catch(Exception ae) {
                            sLogger.log(Level.SEVERE, "enterprise.deployment.backend.autoDeploymentFailure",
                                    new Object[] {ae.getMessage()});
                            //let me continue for the next directory
                        }
                        
                        if(deployer.isCancelled()) {//if this cycle is terminated using "cancel" signal let me JUST BREAK
                            break;
                        }
                    }
                    
                }
                
            }catch(Exception e){
                currentDeployer=null;
                sLogger.log(Level.SEVERE, "enterprise.deployment.backend.autoDeploymentFailure", new Object[] {e.getMessage()});
            }
        }
        
        /**
         * cancel method will ensure that if there is any current deployment is in process,</br>
         * it will be completed before terminating the run
         * @return
         */
        public boolean cancel(){
            boolean flag=false;
            //let me just complete this execution
            if(currentDeployer !=null) {
                currentDeployer.setCancel(true);
            }
            super.cancel();
            flag=true;
            return flag;
            
        }
        
        private DirectoryScanner getDirectoryScanner() {
            if (directoryScanner == null) {
                directoryScanner = new AutoDeployDirectoryScanner();
            }
            return directoryScanner;
        }

        private AutoDeployer getAutoDeployer() {
            if (currentDeployer == null) {
                currentDeployer = new AutoDeployer(verify, preJspCompilation);
            } else {
                /*
                 *Before returning the pre-existing instance, set the characteristics because
                 *they might have been changed since the .
                 */
                currentDeployer.setVerify(verify);
                currentDeployer.setJspPreCompilation(preJspCompilation);
            }
            currentDeployer.setDirectoryScanner(getDirectoryScanner());
            return currentDeployer;
        }
    }
}
