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

package com.sun.enterprise.server;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

import com.sun.enterprise.server.ManagerFactory;
import com.sun.enterprise.server.ApplicationManager;
import com.sun.enterprise.server.StandAloneConnectorModulesManager;
import com.sun.enterprise.server.StandAloneEJBModulesManager;
import com.sun.enterprise.deployment.autodeploy.AutoDeployConstants;
import com.sun.enterprise.deployment.autodeploy.AutoDeployDirectoryScanner;

/**
 * SystemAppScanner takes the system directory and scans the archives based on
 * Application-Type specified in the manifest file of archive.
 * TargetFileFilter filters the archives based on the targetType and 
 * Application-Type
 * @author Sandhya E
 */

public class SystemAppScanner extends AutoDeployDirectoryScanner {

    private String targetType;
	
    public SystemAppScanner(String targetType){
        this.targetType = targetType;
    }
    
    protected File[] getListOfFiles(File dir, boolean includeSubDir) {
        return dir.listFiles(new TargetFileFilter(targetType));
    }
    
}

class TargetFileFilter implements  FileFilter{
    private static final Logger sLogger= LogDomains.getLogger(LogDomains.CORE_LOGGER);
    
    String targetType = null;
    
    TargetFileFilter(String targetType){
        this.targetType = targetType;    
    }
    
	 
    
    /** 
     * Accept method implementation of FileFilter, specific for app/modules,/br> 
     *its not a directory/br>
     *write/read permission is provided </br>
     *valid name </br>
     *valid type(ear/jar/war/rar etc) </br>
     *
     */        
    public boolean accept(File f){
        boolean flag=false;
        String name= f.getName();        
        String fileType = null;
        int lastIndex=-1;
        
        if(name !=null && name.length() >0){
            lastIndex = name.lastIndexOf('.');
            try{
                if (lastIndex >= 0) 
                    fileType = name.substring(lastIndex + 1);
                if(!f.isDirectory() && f.canRead())
				{
                    name=name.substring(0,lastIndex); //valid name                
                    if(name !=null && name.length() >0)
                       flag=true;
                }
            }catch(SecurityException se){
                sLogger.log(Level.WARNING, "SecurityException occured while accessing :" +f.getName());
                //ignore the file, and lets just not accept its as deployable component file 
            }catch(Exception e){
                sLogger.log(Level.WARNING, "Exception occured while accessing :" +f.getName());
                //ignore the file, and lets just not accept its as deployable component file 
            }
        }
        if(flag && isApplicableToTarget(f) && !(isRegistered(name,fileType))) {
            sLogger.log(Level.FINE,"Selecting file ["+ f.getAbsolutePath()+"] for autodeployment");
            return true;
        }else 
            return false;
        
    }

	private boolean isApplicableToTarget(File f) {
            JarFile j = null;
	    try{
		    j = new JarFile(f);
		    Manifest m = j.getManifest();
		    Attributes a = m.getMainAttributes();
		    String appType = a.getValue(Constants.APPLICATION_TYPE);
			if(appType == null) appType = Constants.USER;
			if(targetType.equals(Constants.TARGET_TYPE_ADMIN)){
			    if(appType.equals(Constants.SYSTEM_ADMIN) || appType.equals(Constants.SYSTEM_ALL))
				    return true;
				else
				    return false;
			}else if(targetType.equals(Constants.TARGET_TYPE_INSTANCE)){
			    if(appType.equals(Constants.SYSTEM_INSTANCE) || appType.equals(Constants.SYSTEM_ALL))
				    return true;
				else
				    return false;
			}
			return false;
		}catch(Exception e) {
		    sLogger.log(Level.WARNING, "Exception occured while accessing :" +f.getName());
			return false;
		} finally {
                    if (j != null) {
                        try {
                            j.close();
                        } catch (IOException ioe) {
                            sLogger.log(Level.WARNING, "Error closing jar file after checking for autodeploy", ioe);
                        }
                    }
                }
	}

        /**
         * Checks if an app is registered. Takes the registration
         * name and type of the archive
         * Type of the archive is ear/war/jar/rar
         * @return true if app is registered
         **/
        private boolean isRegistered(String name, String type)
        {
            try{
            
                if(type.equals(AutoDeployConstants.EAR_EXTENSION)) {
                
                    ApplicationManager amgr =
                                ManagerFactory.getApplicationManager();
                    return amgr.isRegistered(name);
                    
                }else if(type.equals(AutoDeployConstants.JAR_EXTENSION)) {
                
                    StandAloneEJBModulesManager emgr =
                                ManagerFactory.getSAEJBModulesManager();
                    return emgr.isRegistered(name);
                    
                }else if(type.equals(AutoDeployConstants.RAR_EXTENSION)) {
                
                    StandAloneConnectorModulesManager cmgr =
                                ManagerFactory.getSAConnectorModulesManager();
                    return cmgr.isRegistered(name);
                    
                }else if(type.equals(AutoDeployConstants.WAR_EXTENSION)) {
                }   
                
            }catch(Exception e){
                sLogger.log(Level.FINE, "error_occured_in_isreg",e);
            }   
            return false;
        }   

}
        
