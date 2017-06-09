/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
 * GetWebUriTester.java
 *
 * Created on January 28, 2004, 3:36 PM
 */

package jsr88.deploymentmanager.distribute.clientconfiguration;

import java.io.File;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.ProgressObject;

import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.util.ModuleDescriptor;

import devtests.deployment.util.JSR88Deployer;

/**
 *
 * @author Jerome Dochez
 */
public class ClientConfigurationTester extends JSR88Deployer {
    
    /** Creates a new instance of DeployTargetModuleIDTester */
    public ClientConfigurationTester(String host, String port, String user, String password) {       
        super(host, port, user, password);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try {
            ClientConfigurationTester deployer = getDeployer(args);
            ProgressObject po = deployer.deploy(args);
            if (deployer.test(po, args[6])) {
                System.exit(0);
            } else {
                deployer.dumpModulesIDs("", po.getResultTargetModuleIDs());                
                System.exit(1);
            }
            
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    } 
    
    protected static ClientConfigurationTester getDeployer(String[] args) {
       return new ClientConfigurationTester(args[1], args[2], args[3], args[4]);                
    }
    
    
    protected ProgressObject deploy(String[] args) throws Exception {        
        
        File inputFile = new File(args[6]);
        if (!inputFile.exists()) {
            error("File not found : " + inputFile.getPath());
            System.exit(1);
        }
        File deploymentFile = null;
        if (args.length > 6) {
            deploymentFile = new File(args[7]);
            if (!args[7].equals("null")) {
                if (!deploymentFile.exists()) {
                    error("Deployment File not found : " + deploymentFile.getPath());
                    System.exit(1);
                }
            }
        }
        
        log("Deploying " + inputFile + " plan: " + deploymentFile);
        return deploy(inputFile, deploymentFile, false);
    }
    
    protected boolean test(ProgressObject po, String path)
        throws Exception
    {
        
        TargetModuleID[] moduleIDs = po.getResultTargetModuleIDs();
        if (moduleIDs.length==0) {
            // deployment failed ?
            log("Deployment failed, got zero TargetModuleID");
            System.exit(1);
        }
        
        // we are loading the deployed file and checking that the moduleIDs are
        // correct
        Application app = ApplicationArchivist.openArchive(new File(path));
        
        if (app.isVirtual()) {
            BundleDescriptor bd = app.getStandaloneBundleDescriptor();
            
            // standalone module, should be fast.
            if (moduleIDs.length!=1) {
                // wrong number...
                log("Error " + path + " is a standalone module, got more than 1 targetmoduleID");
                dumpModulesIDs("", moduleIDs);
                return false;
            }
            
            // let's just check that the web uri is fine
            if (bd.getModuleType().equals(ModuleType.WAR)) {
                if (moduleIDs[0].getWebURL()==null) {
                    log("Error : standalone web module and getWebURI() returned null");
                    return false;
                }
            }
        } else {
            for (int i=0;i<moduleIDs.length;i++) {                
                TargetModuleID parent = moduleIDs[i];
                Target target = parent.getTarget();
                log("Deployed on " + target.getName() + " with module ID " + parent.getModuleID());
                
                // now look at all the children
                TargetModuleID[] children = parent.getChildTargetModuleID();
                if (children==null) {
                    log("ERROR : App from " + path + " has " + app.getBundleDescriptors().size() +
                        " modules but I didn't get any children TagetModuleID");
                    return false;
                }                    
                    
                for (int j=0;j<children.length;j++) {
                    TargetModuleID aChild = children[j];
                    log("Processing " + aChild.getModuleID());
                    
                    String childModuleID = aChild.getModuleID();
                    String[] splitted = childModuleID.split("#");
                    if (splitted.length!=2) {
                        log("Unknown sub module id " + childModuleID);
                        return false;
                    }
                                                            
                    // look for the right module descriptor..
                    ModuleDescriptor md = app.getModuleDescriptorByUri(splitted[1]);
                    if (md==null) {
                        log("Cannot find module descriptor for " + childModuleID);
                        return false;
                    } else {
                        log("Found module descriptor for " + childModuleID);
                    }
                    if (md.getModuleType().equals(ModuleType.CAR)) {
                        // this is an appcliemt, let's execute it
                        ClientConfiguration cc = po.getClientConfiguration(aChild);
                        cc.execute();
                    }
                }                
            }
        }
        // if we are here, it's good !
        return true;      
    }  
}
