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
 * ClustersInfoManager.java
 *
 * Created on May 21, 2004, 3:02 PM
 */

package com.sun.enterprise.tools.upgrade.cluster;

/**
 *
 * @author  prakash
 */
import java.io.*;
import java.util.*;
//Added for 6460927
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.*;
import java.util.logging.*;
import com.sun.enterprise.tools.upgrade.common.CommonInfoModel;
import com.sun.enterprise.tools.upgrade.common.DomainInfo;
import com.sun.enterprise.tools.upgrade.common.UpgradeUtils;
import com.sun.enterprise.tools.upgrade.common.UpgradeConstants;

/*
 * This class manages multpile clinstance conf files represented by cluster info.
 *  Processes cluster files and stores the list.
 */

public class ClustersInfoManager {
    
    private static ClustersInfoManager clusterManager;
    private List clusterInfoList;
    private StringManager stringManager = StringManager.getManager(LogService.UPGRADE_LOGGER); 
    private static Logger logger=LogService.getLogger(LogService.UPGRADE_LOGGER);
    
    // This table maintains domain name as key and List of IIOPClusters defined in server.xml as the value.
    private Hashtable iiopClustersMapping;
    
    // This table maintains persistenceStoreProperties for each domain
    private Hashtable persistenceStoreMapping;
    
    // Class representing Loadbalancer.conf file.
    private LBInfo loadBalancerInfo;
    
    /** Creates a new instance of ClustersInfoManager */
    public ClustersInfoManager() {
    }
    
    public static ClustersInfoManager getClusterInfoManager(){
        if(clusterManager == null)
            clusterManager = new ClustersInfoManager();
        return clusterManager;        
    }
    
    public ClusterInfo parseClinstanceConfFile(String fileName)throws FileNotFoundException, IOException{
        return this.parseClinstanceConfFile(new File(fileName));
    }
    
    public ClusterInfo parseClinstanceConfFile(File file) throws FileNotFoundException, IOException{
        if(!file.exists())
            throw new FileNotFoundException();
        ClusterInfo clInfo = new ClusterInfo();
        clInfo.parseClinstanceConfFile(file);
        return clInfo;
    }
    
    public List getClusterInfoList(){
        return this.clusterInfoList;
    }
    
    public boolean processClinstanceConfFiles(List clinstanceConfFiles){
        if(clusterInfoList == null)
            clusterInfoList = new ArrayList();
        clusterInfoList.clear();
        for(Iterator it = clinstanceConfFiles.iterator(); it.hasNext();){
            try{
                clusterInfoList.add(this.parseClinstanceConfFile((String)it.next())); 
            }catch(FileNotFoundException fne){
                logger.severe(stringManager.getString("enterprise.tools.upgrade.clinstanceConfFileMissing"));
                System.exit(1);
                // if file is not found, that cluster is not added to the list.  Continue processing other cluster files
            }catch(Exception ex){
                logger.severe(stringManager.getString("enterprise.tools.upgrade.clinstanceConfFileProcessingException"));
                return false;
            }
        }
        return true;
    }
    
    /**
     * Gathers cluster information when source domain is 8.x
     */
    public void gatherClusterInfo(CommonInfoModel commonInfo){
        if(clusterInfoList == null)
            clusterInfoList = new ArrayList();
        clusterInfoList.clear();
        for(java.util.Iterator dItr = 
                commonInfo.getDomainMapping().keySet().iterator(); dItr.hasNext();){
            DomainInfo dInfo = (DomainInfo)commonInfo.getDomainMapping().get(dItr.next());
            String domainName = dInfo.getDomainName();
            String domainXmlFile = dInfo.getDomainPath() + File.separator +
                    UpgradeConstants.AS_CONFIG_DIRECTORY + File.separator + 
                    "domain.xml";
            //Build the clusterInfoList
            UpgradeUtils.getUpgradeUtils(commonInfo).updateClusterList(
                    domainXmlFile, domainName, clusterInfoList);
        }
    }
    
    public LBInfo getLoadBalancerInfo(){
        return this.loadBalancerInfo;
    }
    
    public boolean processLoadBalancerFile(String fileName){
        if(this.loadBalancerInfo == null)
            this.loadBalancerInfo = new LBInfo();
        if(! new File(fileName).exists())
            return false;
        return this.loadBalancerInfo.processLoadBalancerFile(fileName);
    }
    
    public Hashtable getIIOPClustersMapping(){
        return this.iiopClustersMapping;
    }
    
    public IIOPCluster getIIOPCluster(String domainName, String clusterName){
        // If IIOPCluster for the domain not created, create one.
        // Domain name is the target domain name
        if(this.iiopClustersMapping == null)
            this.iiopClustersMapping = new Hashtable();
        List clList = (List)this.iiopClustersMapping.get(domainName);
        IIOPCluster iiopCluster = null;
        if((clList != null) && (!clList.isEmpty())){
            for(Iterator iIt = clList.iterator(); iIt.hasNext();){
                java.lang.Object iiopClusterObject = iIt.next();
                IIOPCluster iCluster = (IIOPCluster)iiopClusterObject;
                if(iCluster.getClusterName().equals(clusterName)){
                    iiopCluster = iCluster;
                    break;
                }
            }
        }else{
            clList = new ArrayList();
        }
        if(iiopCluster == null){
            iiopCluster = new IIOPCluster(clusterName);
            clList.add(iiopCluster);
            this.iiopClustersMapping.put(domainName,clList);
        }
        return iiopCluster;
    }
    
    public boolean isIIOPClusterExists(String domainName, String clusterName){
        if(this.iiopClustersMapping == null)
            return false;
        List clList = (List)this.iiopClustersMapping.get(domainName);
        if((clList == null) || (clList.isEmpty())){
            return false;
        }
        for(Iterator iIt = clList.iterator(); iIt.hasNext();){
            if(((IIOPCluster)iIt.next()).getClusterName().equals(clusterName)){
                return true;
            }
        }
        return false;
    }
    
    public Hashtable getPersistenceStorePropertiesMapping(){
        return this.persistenceStoreMapping;
    }
    
    public void addPersistenceStoreProperty(String domainName, String name, String value){
        if(this.persistenceStoreMapping == null){
            persistenceStoreMapping = new Hashtable();
        }
        Properties propsList = (Properties)this.persistenceStoreMapping.get(domainName);
        if(propsList == null)
            propsList = new Properties();
        propsList.setProperty(name,value);
        this.persistenceStoreMapping.put(domainName,propsList);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        List aList = new ArrayList();
        aList.add("c:\\temp\\clinstance.conf");
        aList.add("c:\\temp\\clinstance.conf");
        ClustersInfoManager cIM = ClustersInfoManager.getClusterInfoManager();
        cIM.processClinstanceConfFiles(aList);
        cIM.print();
        
    }
    
    public void print(){
        if(clusterInfoList != null){
            for(Iterator it = clusterInfoList.iterator(); it.hasNext();){
                ClusterInfo clInfo = (ClusterInfo)it.next();
                com.sun.enterprise.tools.upgrade.common.CommonInfoModel.getDefaultLogger().info("\n\n\n *****************");
                clInfo.print();                
            }
        }
    }        
}
