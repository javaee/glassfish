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
 * @(#) SystemAppLifecycle.java
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
package com.sun.enterprise.server;

import java.io.File;

import com.sun.enterprise.Switch;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.ConfigContext;

import com.sun.enterprise.deployment.autodeploy.AutoDeployer;
import com.sun.enterprise.deployment.autodeploy.AutoDeploymentException;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

import com.sun.enterprise.server.ondemand.OnDemandServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.enterprise.instance.ServerManager;

/**
 * This class implements the lifecycle methods used by the System Apps.  
 *
 * @author  Sandhya E
 */
public final class SystemAppLifecycle extends ApplicationLifecycle {

    /**
     * Right now this lifecycle is not registered as a lifecycle. 
     * This method will be explicitly called from J2EE Server. 
     * This method will call two lifecycle methods: onInit and onStartup.
     * OnShutdown and onTermination need not be called as the required 
     * task will be done ALC for now. This is a mockup startup method 
     * which will take care of lifecycle methods till onStartup
     */
    public void startup(ServerContext sc) throws ServerLifecycleException {
        onInitialization(sc);        
        onStartup(sc);
    }

    /**
     * Server is starting up applications
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onStartup(ServerContext sc) throws ServerLifecycleException {

        try {
            //deploys any new system apps
            deploySystemApps();

            loadSystemApps();
        } catch (Throwable th) {
            _logger.log(Level.SEVERE, 
                "core.unexpected_error_occured_while_app_loading", th);
        }
    }

    /**
     * Server has complted loading the system applications 
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onReady(ServerContext sc) throws ServerLifecycleException {
        // not required to notify listeners(ejb containers) as this will be done by ALC    
    }

    /**
     * Server is shutting down applications
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onShutdown() throws ServerLifecycleException {
        //not required
    }

    /**
     * Server is terminating the subsystems and the runtime environment.
     * Gracefully terminate the active use of the public methods of this
     * subsystem.  This method should be the last one called on a given
     * instance of this subsystem.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal 
     *  error that prevents this subsystem from being used
     */
    public void onTermination() throws ServerLifecycleException {
        //not required
    }

    /**
     * Loads all the deployed system applications. 
     */
    private void loadSystemApps() {
        _logger.log(Level.FINE, "core.loading_system_apps");
        // loads all deployed connector modules
        this._connMgr.loadSystem();

        // loads all deployed stand alone ejb modules
        this._ejbMgr.loadSystem();

        // loads all deployed j2ee applications
        this._applicationMgr.loadSystem();
    }
    private String getSystemAppDirPath(){
        String sysAppDirPath = System.getProperty(Constants.INSTALL_ROOT) + File.separator 
            + Constants.LIB + File.separator + Constants.LIB_INSTALL + File.separator + Constants.LIB_INSTALL_APPLICATIONS;
        return sysAppDirPath;
    }
    
    /**
     * Deploys all system apps under system directory to all 
     * server instances under <Servers> in domain.xml. There
     * are three categories in system apps, applicable to admin 
     * instance only, applicable to instances only, applicable to all. 
     * 
     */
    private void deploySystemApps(){
        //FIXME: deploying system apps shoudl be done using J2EEC when it is available.
        try{
            String[] targets = getTargets();
            if(targets == null) return;
            int size = targets.length;
            for(int i = 0; i < size; i++) {
                deployToTarget(targets[i]);
            }
        }catch(Exception ex){
            _logger.log(Level.SEVERE,
                "core.exception_while_deploying_system_apps", ex);
        }
    }

    /**
     * Deploys the system apps under system_app_dir to a specific target.
     * Target here is a single server
     * @param target targetserver to which application should be deployed
     */
    private void deployToTarget(String target) {
        String sysAppDirPath = getSystemAppDirPath();
        _logger.log(Level.FINE,"core.deploying_system_apps", new Object[]{target, sysAppDirPath});
        com.sun.enterprise.deployment.autodeploy.AutoDeployer deployer =
           new com.sun.enterprise.deployment.autodeploy.AutoDeployer();
        deployer.setTarget(target);
        deployer.setDirectoryScanner(new SystemAppScanner(getTargetType(target)));
        deployer.disableRenameOnSuccess();
		
        File sysAppDir = new File(sysAppDirPath);
        try{
            if(sysAppDir.exists() && sysAppDir.canRead()) {
                deployer.deployAll(sysAppDir);
                _logger.log(Level.FINE,"core.deployed_system_apps",target);
            } else {
                _logger.log(Level.WARNING, "core.system_app_dir_not_found", new Object[] {sysAppDirPath});
            }
        }catch(AutoDeploymentException ade) {
            _logger.log(Level.SEVERE,
                "core.exception_while_deploying_system_apps", ade);
        }
    }

    private String[] getTargets(){
        try{
            ConfigContext confContext = _context.getConfigContext();
            Domain domain = (Domain)confContext.getRootConfigBean();
            Servers svrs = domain.getServers();
            Server[] svrArr = svrs.getServer();
            int size = svrArr.length;
            String[] targetNames = new String[size];
            for(int i = 0 ; i< size; i++) {
               targetNames[i] = svrArr[i].getName(); 
            }
            return targetNames;
        }catch(Exception ex){
            _logger.log(Level.SEVERE,
                "core.exception_while_getting_targets", ex);
            return null;
        }
    }

    private String getTargetType(String targetName){

	/*
        if(targetName.equalsIgnoreCase(ServerManager.ADMINSERVER_ID))
            return Constants.TARGET_TYPE_ADMIN;
        else
            return Constants.TARGET_TYPE_INSTANCE;
	*/

	/**
	 * commented the above code since it doesn't return the 
	 * target type properly. In case of DAS since the instance name is
	 * server it returns the type as instance
	 *
	 * Following code uses the isDAS of ServerHelper to determine
	 * if the target type is admin or not
	 */

        try{
	    if (ServerHelper.isDAS(_context.getConfigContext(), targetName)) {
            	return Constants.TARGET_TYPE_ADMIN;
	    } else {
            	return Constants.TARGET_TYPE_INSTANCE;
	    }
        }catch(Exception ex){
            _logger.log(Level.SEVERE, "core.exception_while_getting_targetType", ex);
        }

	return null;
    }

}
