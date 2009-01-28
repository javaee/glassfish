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
 * ClusterInfo.java
 *
 * Created on May 21, 2004, 3:03 PM
 */

package com.sun.enterprise.tools.upgrade.cluster;

/**
 *
 * @author  prakash
 */

/*
 * This class represents one clinstance.conf file. 
 */

import java.util.*;
import java.io.*;
import java.util.logging.*;
import com.sun.enterprise.tools.upgrade.common.UpgradeUtils;
import com.sun.enterprise.tools.upgrade.common.CommonInfoModel;

public class ClusterInfo {
    
    // This cluster name is not really extracted from clinstance.conf.  
    //This will be the name that is used to create cluster in 8.0.
    // This is useful in handling rest of cluster processing code.
    private String clusterName;
    private List clusteredInstanceList;
    // This domain name is added for AS8.x to AS9.0 upgrade
    private String domainName;
    private static Logger log = CommonInfoModel.getDefaultLogger();

    public ClusterInfo(){
    }
    
    public List getClusteredInstanceList(){
        return this.clusteredInstanceList;
    }
    
    public void parseClinstanceConfFile(File file) throws FileNotFoundException, IOException{
        if(clusteredInstanceList == null){
            clusteredInstanceList = new ArrayList();
        }
        clusteredInstanceList.clear();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line = null;
        ClusteredInstance clInstance = null;            
        while((line = reader.readLine()) != null){
            if(line.trim().startsWith("#")){
                // Comment line.  pass on
                continue;
            }
            if(line.trim().startsWith("instancename")){
                String instanceName = line.substring("instancename".length()).trim();
                clInstance = createNewInstance(instanceName);
                continue;
            } 
            if(clInstance != null)
                clInstance.extractDataFromLine(line);
        }
    }

    /**
      * Builds ClusteredInstance list with domain name
      * and respective cluster name.
      */	  
    public void updateClusteredInstanceList(String domainXMLFile, 
            String domainName, String clName, UpgradeUtils upgradeUtils) {
        if(clusteredInstanceList == null) {
            clusteredInstanceList = new ArrayList();
        } else {
			clusteredInstanceList.clear();
		}
        upgradeUtils.updateClusteredInstanceList(domainXMLFile,
            domainName, clName, clusteredInstanceList);
    }


    private ClusteredInstance createNewInstance(String instanceName){
        ClusteredInstance clInstance = new ClusteredInstance(instanceName);
        clusteredInstanceList.add(clInstance);
        return clInstance;
    }
    public ClusteredInstance getMasterInstance(){
        ClusteredInstance c = null; 
        for(Iterator it = this.clusteredInstanceList.iterator(); it.hasNext();){
            ClusteredInstance clInstance = (ClusteredInstance)it.next();
            if(clInstance.isMaster()){
                c = clInstance;
				break;
			}
        }
        return c;
    }
    
    public String getDomainName(){
        if(this.domainName == null){
            ClusteredInstance clInstance = this.getMasterInstance();
            if((clInstance == null) && (this.clusteredInstanceList.size() > 0)){
                clInstance = (ClusteredInstance)this.clusteredInstanceList.get(0);
            }
            this.domainName = clInstance.getDomain();
        }
        return this.domainName;
    }
    
    public void setDomainName(String dName){
        this.domainName = dName;
    }
    
    public String getClusterName(){
        return this.clusterName;
    }
    
    public void setClusterName(String clName){
        this.clusterName = clName;
    }
    
    public void print(){
        if(clusteredInstanceList != null){
            for(Iterator it = clusteredInstanceList.iterator(); it.hasNext();){
                ClusteredInstance clInst = (ClusteredInstance)it.next();
                log.info(clInst.getInstanceName());
                log.info(clInst.getUser());
                log.info(clInst.getHost());
                log.info(clInst.getPort());
                log.info(clInst.getDomain());
                log.info(clInst.getInstancePort());
                log.info(String.valueOf(clInst.isMaster()));
                log.info("\n");
            }
        }
    }    
}
