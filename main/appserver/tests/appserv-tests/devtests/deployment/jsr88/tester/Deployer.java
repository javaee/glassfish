/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package tester;

import java.io.*;
import java.util.*;
import java.beans.*;
import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.shared.factories.*;
import javax.enterprise.deploy.spi.factories.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.status.*;
import javax.enterprise.deploy.shared.ModuleType;

import com.sun.enterprise.deployapi.DeploymentFactoryInstaller;

public class Deployer implements ProgressListener {

    public static void main(String args[]) {
        
        try {
            System.out.println(" inst at " + System.getProperty("com.sun.aas.installRoot"));
            Deployer deployer = new Deployer();
            deployer.deploy(args);
	    System.exit(0);
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    } 
    
    
    public void deploy(String args[]) throws Exception {

        DeploymentFactoryInstaller.getInstaller();
        DeploymentManager dm = DeploymentFactoryManager.getInstance().getDeploymentManager("deployer:Sun:S1AS::localhost:4848", "admin", "adminadmin");
        ProgressObject dpo;
        File archive = new File(args[0]);
        File deploymentPlan = new File(args[1]);
        
        if (!deploymentPlan.exists()) {
             System.out.println("Warning, deploying with null deployment plan");
             dpo = dm.distribute(dm.getTargets(), archive, null);          
        } else {
            System.out.println("Deploying " + archive.getAbsolutePath() + " with plan " + deploymentPlan.getAbsoluteFile());
             dpo = dm.distribute(dm.getTargets(), archive, deploymentPlan);          
        }            
        dpo.addProgressListener(this);
        System.out.println("Deployment returned " + dpo);
        if (dpo!=null) {
            DeploymentStatus deploymentStatus = dpo.getDeploymentStatus();
            do {
                Thread.currentThread().sleep(200);
            } while (!(deploymentStatus.isCompleted() || deploymentStatus.isFailed()));            
            System.out.println("Deployment status is " + deploymentStatus);
        }
        TargetModuleID[] targetModuleIDs = dpo.getResultTargetModuleIDs();        
        dumpResultModuleIDs("Deployed " , dpo);
        
//        ModuleType moduleType = ((SunTargetModuleID) targetModuleIDs[0]).getModuleType();
        
//        listApplications(dm, moduleType , dm.getTargets(), null);
    }
    
    private void dumpResultModuleIDs(String prefix, ProgressObject po) {
        TargetModuleID[] targetModuleIDs = po.getResultTargetModuleIDs();
        for (int i=0;i<targetModuleIDs.length;i++) {            
            dumpModulesIDs(prefix, targetModuleIDs[i]);            
        }
    }    

    private void dumpModulesIDs(String prefix, TargetModuleID targetModuleID) {
        System.out.println(prefix + targetModuleID);
        TargetModuleID[] subs = targetModuleID.getChildTargetModuleID();
        if (subs!=null) {
            for (int i=0;i<subs.length;i++) {
                System.out.println(" Child " + i + "\n" + subs[i]);
            }
        }
    }        
    
    public void handleProgressEvent(javax.enterprise.deploy.spi.status.ProgressEvent progressEvent) {
        DeploymentStatus ds = progressEvent.getDeploymentStatus();
        System.out.println("Received Progress Event state " + ds.getState() + " msg = " + ds.getMessage());        
    }
    
}
