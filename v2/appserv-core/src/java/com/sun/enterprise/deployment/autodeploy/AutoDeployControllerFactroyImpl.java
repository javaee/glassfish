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
 * AutoDeployControllerFactroy.java 
 */

package com.sun.enterprise.deployment.autodeploy;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import java.io.File;

import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Servers;
//import com.sun.enterprise.config.serverbeans.ConfigRefs;
import com.sun.enterprise.config.serverbeans.Config;
//import com.sun.enterprise.config.serverbeans.DasConfig;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.DasConfig;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.PropertyResolver;

/**
 * Implements AutoDeployControllerFactroy</br>
 * create a instance of autodeploycontroller, depending upon the Servercontext passed</br>
 * if autodeploy is enabled in domain.xml return a new instance else return null.</br>
 *
 * @author  vikas
 */
public class AutoDeployControllerFactroyImpl implements AutoDeployControllerFactroy {
    
    private static final Logger sLogger=AutoDeployControllerImpl.sLogger;
    private static StringManager localStrings = 
                            StringManager.getManager( AutoDeployControllerFactroyImpl.class );
    
    
    /**
     * create a instance of autodeploycontroller, depending upon the Servercontext passed</br>
     *If autodeploy is enabled in domain.xml return a new instance else return null.</br>
     *If  autodeploydir is null/empty.-change to default with appropriate log message .</br>
     *If  autodeploydir is invalid(not a directory/not exit/not have read-write permission </br>
     *available) continue the thread, with appropriate log message.</br>
     *ALso both absolute and relative paths for autodeploy-dir are handled.</br>
     *If autodeploy-polling-interval is null/empty/invalid/ less than AutoDeployConstants.DEFAULT_POLLING_INTERVAL
     * - change to default with appropriate log message </br>
     *
     */
    public AutoDeployController createAutoDeployController(ServerContext context) throws AutoDeploymentException {
        
        ConfigContext confContext = context.getConfigContext();
        AutoDeployController autoDeployController = null;             
        String targetConfigurationName = null;
        Domain domain = null;
        
        String autoDeployDir=null ;
        String sourcedir=null;
        String strPollingInterval =null ;
        long pollingInterval;                      
        boolean verifyEnabled=false ;
        boolean preJspCompilation=false ;
        
        DasConfig dasConfig = null;
        try {
            //domain = (Domain)confContext.getRootConfigBean();
            //Config config = ServerBeansFactory.getConfigBean(confContext);
            //if(config != null) 
            //    as = config.getAdminService();
              dasConfig = ServerBeansFactory.getDasConfigBean(confContext);
        }catch (Exception ce){
                   sLogger.log(Level.SEVERE, "enterprise.deployment.backend.autoDeploymentStartFailure");
                   throw new AutoDeploymentException("Failed to start autodeploy", ce);
        }
        
       // targetConfigurationName= getTargetConfigName(domain);
        //read target configuration
       /* if(targetConfigurationName !=null && !targetConfigurationName.trim().equals("")){
            Config config = domain.getConfigs().getConfigByName(targetConfigurationName); 
            if(config !=null){
                //get appconfig specific to targetConfigurationName
                //ApplicationConfig appConfig= config.getApplicationConfig();
        */
              
                
        if(dasConfig != null) {
                boolean autodeployEnabled=dasConfig.isAutodeployEnabled();
                if(autodeployEnabled){                    
                    autoDeployDir=dasConfig.getAutodeployDir() ;
                    if(autoDeployDir != null) {
                        try {
                            autoDeployDir = new PropertyResolver(confContext,
                                context.getInstanceName()).
                                    resolve(autoDeployDir);
                            autoDeployDir=autoDeployDir.trim();
                        } catch (ConfigException ce) {
                            //log
                            autoDeployDir = null;
                        }
                    }
                    if(autoDeployDir == null || "".equals(autoDeployDir)) { 
                        //empty path so putting default
                        autoDeployDir = AutoDeployConstants.DEFAULT_AUTODEPLOY_DIR;
                        sourcedir= context.getInstanceEnvironment().getAutoDeployDirPath()+File.separator+autoDeployDir;
                        String msg = localStrings.getString("enterprise.deployment.autodeploy.invalid_source_dir_shifting_to_default",sourcedir);
                        sLogger.log(Level.WARNING, msg);
                    } else if((new File(autoDeployDir)).isAbsolute()) { 
                        //absolute path
                        sourcedir=autoDeployDir;
                    } else {
                        //relative path
                        sourcedir= context.getInstanceEnvironment().getAutoDeployDirPath()+File.separator+autoDeployDir; 
                    }
                    strPollingInterval = dasConfig.getAutodeployPollingIntervalInSeconds();
                    verifyEnabled=dasConfig.isAutodeployVerifierEnabled() ;
                    preJspCompilation=dasConfig.isAutodeployJspPrecompilationEnabled() ;
                    try {
                        try {
                            pollingInterval= Long.parseLong(strPollingInterval) ;
                            if(pollingInterval < AutoDeployConstants.MIN_POOLING_INTERVAL) {
                                String msg = localStrings.getString("enterprise.deployment.autodeploy.invalid_pooling_interval_shifting_to_default",strPollingInterval,AutoDeployConstants.MIN_POOLING_INTERVAL+"",AutoDeployConstants.DEFAULT_POLLING_INTERVAL+"");
                                sLogger.log(Level.WARNING, msg);
                                pollingInterval = AutoDeployConstants.DEFAULT_POLLING_INTERVAL;                                
                            }
                        } catch (NumberFormatException ne) { 
                            String msg = localStrings.getString("enterprise.deployment.autodeploy.invalid_pooling_interval_shifting_to_default",strPollingInterval,AutoDeployConstants.MIN_POOLING_INTERVAL+"",AutoDeployConstants.DEFAULT_POLLING_INTERVAL+"");
                            sLogger.log(Level.WARNING, msg);
                           // throw new AutoDeploymentException(ne);
                           pollingInterval = AutoDeployConstants.DEFAULT_POLLING_INTERVAL;
                        }
                        autoDeployController = new AutoDeployControllerImpl(sourcedir,pollingInterval);  
                        autoDeployController.setVerify(verifyEnabled);
                        autoDeployController.setPreJspCompilation(preJspCompilation);
                    } catch(AutoDeploymentException ae) {
                        sLogger.log(Level.SEVERE, "enterprise.deployment.backend.autoDeploymentStartFailure");
                        throw ae;

                    }
            //    }
            }//END if(config !=null)
        }

        return autoDeployController;

    } 
    
    /*
     //REMOVED Since this should be resolved from ServerBeansFactory
    private String getTargetConfigName(Domain domain) throws AutoDeploymentException {
        
        String targetConfigurationName = null;        
        Server svr = null;
        String targetName = null;
        Servers svrs = null;
        Server[] svrArr = null;       
        svrs = domain.getServers();
        svrArr = svrs.getServer(); 
        for(int i = 0 ; i< svrArr.length; i++) {
            if(!svrArr[i].getName().equals("")) {
                targetName = svrArr[i].getName();
                svr = svrArr[i];
                break;
            }
        }
        //get target config name
        if(targetName == null || svr == null ) { 
            String msg = localStrings.getString("enterprise.deployment.autodeploy.target_server_not_found");
            sLogger.log(Level.SEVERE, msg);
            throw new AutoDeploymentException(msg);
        } else {
            ConfigRefs configRefs =svr.getConfigRefs();
            String[] refs= configRefs.getConfigRef();
            for(int i = 0 ; i< refs.length; i++) {
                if(!refs[i].equals("")) {
                    targetConfigurationName = refs[i];
                    break;
                }
            }
         }
        if(targetConfigurationName == null || targetConfigurationName.trim().equals("")) {
            String msg = localStrings.getString("enterprise.deployment.autodeploy.error_in_reading_config_params");
            sLogger.log(Level.SEVERE, msg);
            throw new AutoDeploymentException(msg);
            
        }
        return targetConfigurationName;        
    }
    
     */

}
