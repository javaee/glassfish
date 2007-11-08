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
