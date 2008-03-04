
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
 * AutoDeployedFilesManager.java
 *
 * Created on September 4, 2003
 *
 */

package org.glassfish.deployment.autodeploy;

import com.sun.logging.LogDomains;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 *  Contains the status of list of files that have been autodeployed. 
 *
 *  @author Mahesh Rangamani
*/

public class AutoDeployedFilesManager {
    
    private static final Logger sLogger=LogDomains.getLogger(LogDomains.DPL_LOGGER);
    static final String STATUS_DIR_NAME = ".autodeploystatus";
    protected  String statDir = null;


    public AutoDeployedFilesManager() {
    }
    
    protected AutoDeployedFilesManager(String s) {
        statDir = s;
    }
    
    /**
     * Create an instance from the persisted file in the specified directory.
     * @param statusDir Directory in which the status file is to read.
    */
    public static AutoDeployedFilesManager loadStatus(File statusDir) throws Exception {
        return loadStatus(statusDir.getAbsolutePath());
    }
    
    public static AutoDeployedFilesManager loadStatus(String autoDeploymentDir) throws Exception {

        String statusDir = autoDeploymentDir + File.separator + STATUS_DIR_NAME;
        File fileObj = new File(statusDir);
   
        AutoDeployedFilesManager adfm = new AutoDeployedFilesManager(statusDir);
        return adfm;
        
    }
    
    public void writeStatus() throws Exception {
           // Nothing to do 
    }
       
    /**
     * Update the status of the file as deployed. 
     *   
     */
    public void setDeployedFileInfo(File f) throws Exception {
      try {
        File statusFile = getStatusFile(f);
        statusFile.createNewFile();
        statusFile.setLastModified(f.lastModified());
      } catch (Exception e) { throw e; }
    }

    /*
     * Delete status info for file
     */ 
    public void deleteDeployedFileInfo(File f) throws Exception {
      try {
        File statusFile = getStatusFile(f);
        statusFile.delete();
      } catch (Exception e) { throw e;}
    }

    // calculate the status file path. 
    private File getStatusFile(File f) {
        File outDir = new File(this.statDir);
        outDir = obtainFileStatusDir(f, outDir, outDir.getParentFile());
        return new File(outDir, f.getName());
    }

    static File obtainFileStatusDir(File f, File statDir, File autoDeployDir) {
        File dir = f.getParentFile();
        while (!dir.getAbsolutePath().equals(autoDeployDir.getAbsolutePath())) {
            statDir = new File(statDir, dir.getName()); 
            dir = dir.getParentFile();
        }
        statDir.mkdirs();
        return statDir;
    }
   
    /**
      * Compare the list of files with the current status info 
      * and determine the files that need to be deployed 
      * 
      */
    public File[] getFilesForDeployment(File[] latestFiles) {

        if (latestFiles == null) return new File[0];

        ArrayList<File> arrList = new ArrayList<File>();
        for (File deployDirFile : latestFiles) {
            File statusFile = getStatusFile(deployDirFile);
            if (!statusFile.exists() || deployDirFile.lastModified() != statusFile.lastModified()) {            
                arrList.add(deployDirFile);
            }
        }
        return arrList.toArray(new File[0]);
        
    }
    
    /**
      * Compare the list of files with the current status info 
      * and determine the apps that need to be undeployed. 
      * 
      */
    public File[] getFilesForUndeployment(File[] latestFiles) {
        
        File statusDir = new File(statDir);  
        Set<File> statusFiles = 
                AutoDeployDirectoryScanner.getListOfFilesAsSet(statusDir, true);
        
        // The file might have been deleted. In that case, return null.
        if (statusFiles == null || statusFiles.isEmpty()){
            return null;
        }
        
        // now let's get the latestFiles status files names to remove them 
        // from the list of status files, any remaining ones will need to 
        // be undeployed
        for (File deployDirFile : latestFiles) {
            statusFiles.remove(getStatusFile(deployDirFile));
        }
        ArrayList<File> appNames = new ArrayList<File>();
        File autodeployDir = statusDir.getParentFile();        
        for(File statusDirFile : statusFiles) {
            
            // calculate the original file as it was copied in the autodeploy
            // directory
            File filePath = statusDir.getParentFile();
            File f = statusDirFile.getParentFile();
            while (!f.equals(statusDir)) {
                filePath = new File(filePath, f.getName());
                f = f.getParentFile();
            }
            filePath  = new File(filePath,  statusDirFile.getName());
            appNames.add(filePath);
        }
        
        // Add to the app names files any entries for which auto-undeployment
        // has been requested by the user's creation of xxx_undeploy_requested.
        appNames.addAll(getFilesToUndeployByRequest(latestFiles));
        return appNames.toArray(new File[0]);
    }    
    
    /* 
     * Returns files to be undeployed due to the presence of a xxx_undeployRequested
     * file in the autodeploy directory.
     * @param latestFiles the files in the autodeploy directory
     * @return Collection of File objects to undeploy due to the request marker files
     */
    private Collection<? extends File> getFilesToUndeployByRequest(File[] latestFiles) {
        ArrayList<File> appsRequested = new ArrayList<File>();
        
        for (File f : latestFiles) {
            /*
             * See if there is a corresponding _undeployRequested file for this
             * file.
             */
            if (isUndeployRequested(f)) {
                appsRequested.add(new UndeployRequestedFile(f));
            }
        }
        if (sLogger.isLoggable(Level.FINE) && ! appsRequested.isEmpty()) {
            StringBuilder sb = new StringBuilder("Undeployment requested using *_undeployRequested for ");
            for (File app : appsRequested) {
                sb.append("  " + app.getName() + System.getProperty("line.separator"));
            }
            sLogger.fine(sb.toString());
        }
        return appsRequested;
    }
    
    /*
     * Returns whether there is a request to undeploy the specified file.
     * @param f the file to check
     * @return whether or not an undeployment has been requested for the specified file
     */
    private static boolean isUndeployRequested(File f) {
        return appToUndeployRequestFile(f).exists();
    }
    
    /*
     * Returns the File object for the undeployment request for the specified app
     * file or directory.
     * @param f the File to check for an undeployment request
     * @return a File object for the undeploy request file itself
     */
    protected static File appToUndeployRequestFile(File f) {
        File undeployRequest = new File(f.getPath() + AutoDeployConstants.UNDEPLOY_REQUESTED);
        return undeployRequest;
    }
    
    /* 
     * A marker class to indicate that the file is to be undeployed and then
     * deleted by the autodeployer.
     */
    protected class UndeployRequestedFile extends File {
        public UndeployRequestedFile(File parent, String child) {
            super(parent, child);
        }
        
        public UndeployRequestedFile(String path) {
            super(path);
        }
        
        public UndeployRequestedFile(String parent, String child) {
            super(parent, child);
        }
        
        public UndeployRequestedFile(URI uri) {
            super(uri);
        }
        
        public UndeployRequestedFile(File f) {
            super(f.getAbsolutePath());
        }
    }
}


