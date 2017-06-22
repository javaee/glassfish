/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2017 Oracle and/or its affiliates. All rights reserved.
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
