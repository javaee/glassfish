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

package com.sun.enterprise.deployment.client;

import com.sun.appserv.management.base.UploadDownloadMgr;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.client.ConnectionSource;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.config.*;
import com.sun.appserv.management.deploy.DeploymentSupport;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.enterprise.deployapi.SunTarget;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.util.XModuleType;
import com.sun.enterprise.admin.common.ObjectNames;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.enterprise.deploy.shared.ModuleType;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

public class DeploymentClientUtils {

    private static final String APPS_CONFIG_MBEAN = 
        "com.sun.appserv:type=applications,category=config";
    private static ObjectName applicationsMBean =
        JMXUtil.newObjectName(APPS_CONFIG_MBEAN);

    public static final String mapModuleTypeToXType(ModuleType moduleType) {
        if (ModuleType.EAR.equals(moduleType)) {
            return XTypes.J2EE_APPLICATION_CONFIG;
        } else if (ModuleType.EJB.equals(moduleType)) {
            return XTypes.EJB_MODULE_CONFIG;
        } else if (ModuleType.RAR.equals(moduleType)) {
            return XTypes.RAR_MODULE_CONFIG;
        } else if (ModuleType.WAR.equals(moduleType)) {
            return XTypes.WEB_MODULE_CONFIG;
        } else if (ModuleType.CAR.equals(moduleType)) {
            return XTypes.APP_CLIENT_MODULE_CONFIG;
        }
        return null;
    }

    public static final ModuleType getModuleType(
        MBeanServerConnection mbsc, String moduleID) throws Exception {
        if (mbsc!=null) {
            String[] signature = new String[]{"java.lang.String"};
            Object[] params = new Object[]{moduleID};
            Integer modType = (Integer) mbsc.invoke(applicationsMBean, "getModuleType", params, signature);
            if(modType != null) {
                return XModuleType.getModuleType(modType.intValue());
            }
        }
        return null;
    }

    public static final DeploymentStatus deleteApplicationReference(MBeanServerConnection mbsc, String moduleID, 
                                                            SunTarget target, Map options) throws Exception {
        //System.out.println("DBG_PRINT : del-app-ref for target = " + target.getName());
        if (mbsc!=null) {
            String[] signature = new String[]{"java.lang.String", "java.lang.String", "java.util.Map"};
            Object[] params = new Object[]{target.getName(), moduleID, options};
            return (DeploymentStatus) (mbsc.invoke(
                    applicationsMBean, "deleteApplicationReference", params, signature));
        }
        return null;
    }
    
    public static final DeploymentStatus stopApplication(MBeanServerConnection mbsc, String moduleID, 
                                                            SunTarget target, Map options) throws Exception {
        //System.out.println("DBG_PRINT : app stop for target = " + target.getName());
        if (mbsc!=null) {
            String[] signature = new String[]{"java.lang.String", "java.lang.String", "java.util.Map"};
            Object[] params = new Object[]{moduleID, target.getName(), options};
            return (DeploymentStatus) (mbsc.invoke(
                    applicationsMBean, "stop", params, signature));
        }
        return null;
    }
    
    public static final DeploymentStatus createApplicationReference(MBeanServerConnection mbsc, String moduleID, 
                                        SunTarget target, Map options) throws Exception {
        //System.out.println("DBG_PRINT : create-app-ref for target = " + target.getName());
        if (mbsc!=null) {
            Object[] params = new Object[]{target.getName(), moduleID, options};
            String[] signature = new String[]{"java.lang.String", "java.lang.String", "java.util.Map"};
            return (DeploymentStatus) (mbsc.invoke(
                    applicationsMBean, "createApplicationReference", params, signature));
        }
        return null;
    }
    
    public static final DeploymentStatus startApplication(MBeanServerConnection mbsc, String moduleID, 
                                                            SunTarget target, Map options) throws Exception {
        //System.out.println("DBG_PRINT : app start for target = " + target.getName());
        if (mbsc!=null) {
            String[] signature = new String[]{"java.lang.String", "java.lang.String", "java.util.Map"};
            Object[] params = new Object[]{moduleID, target.getName(), options};
            return (DeploymentStatus) (mbsc.invoke(
                    applicationsMBean, "start", params, signature));
        }
        return null;
    }
    
    public static boolean isModuleDeployed (MBeanServerConnection mbsc,
        String moduleID) throws Exception {
        if (moduleID == null) {
            return false;
        }
    
        if (mbsc!=null) {
            String[] signature = new String[] {"java.lang.String"};
            Object[] params = new Object[] {moduleID};
            Object result = mbsc.invoke(applicationsMBean, "getModuleType", 
                params, signature);
            if (result != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLifecycleModule(MBeanServerConnection mbsc, String moduleID) throws Exception {
        if (mbsc!=null) {
            String[] signature = new String[] {"java.lang.String"};
            Object[] params = new Object[] {moduleID};
            Boolean ret = (Boolean)mbsc.invoke(applicationsMBean, "isLifecycleModuleRegistered", params, signature);
            if(Boolean.TRUE.equals(ret)) {
                return true;
            }
        }
        return false;
    }
    
    public static DeploymentStatus createLifecycleModuleReference(MBeanServerConnection mbsc,
        String moduleID, String target, Map options) throws Exception {
        if (mbsc!=null) {
            String[] signature = new String[] {"java.lang.String", "java.lang.String", "java.util.Map"};
            Object[] params = new Object[] {moduleID, target, options};
            return((DeploymentStatus)mbsc.invoke(applicationsMBean, "createLifecycleModuleReference", params, signature));
        }
        return null;
    }
    
    public static DeploymentStatus removeLifecycleModuleReference(MBeanServerConnection mbsc,
        String moduleID, String target) throws Exception {
        if (mbsc!=null) {
            String[] signature = new String[] {"java.lang.String", "java.lang.String"};
            Object[] params = new Object[] {moduleID, target};
            return((DeploymentStatus)mbsc.invoke(applicationsMBean, "removeLifecycleModuleReference", params, signature));
        }
        return null;
    }
    
    public static final void changeStateOfModule(MBeanServerConnection mbsc, String moduleID, String type,
                                                        SunTarget target, boolean enable) throws Exception {
        //System.out.println("DBG_PRINT : Changing state in target  " + target.getName() + " to " + enable);
        if (mbsc!=null) {
            Object[] params = new Object[]{moduleID, type, target.getName()};
            String[] signature = new String[]{"java.lang.String", "java.lang.String", "java.lang.String"};

            if (enable) {
                mbsc.invoke(applicationsMBean, "enable", params, signature);
            } else {
                mbsc.invoke(applicationsMBean, "disable", params, signature);
            }
        }
        return;
    }
    
    public static boolean isTargetListComplete(Map deployedTargets, SunTarget[] targetList) throws IOException {
        Map tmpMap = new HashMap();
        tmpMap.putAll(deployedTargets);
        for(int i=0; i<targetList.length; i++) {
            if(tmpMap.get(targetList[i].getName()) != null) {
                tmpMap.remove(targetList[i].getName());
            }
        }
        if(tmpMap.size() != 0) {
            return false;
        }
        return true;
    }
    
    public static boolean isNewTarget(Map deployedTargets, SunTarget[] targetList) throws IOException {
        // During redeploy, if only one target is given and it is "domain", then 
        // there is no need to check if this is a new target
        if( (targetList.length == 1) && (TargetType.DOMAIN.equals(targetList[0].getName())) ) {
            return false;
        }
        if(deployedTargets.size() != targetList.length) {
            return true;
        }
        Map tmpMap = new HashMap();
        tmpMap.putAll(deployedTargets);
        for(int i=0; i<targetList.length; i++) {
            if(tmpMap.get(targetList[i].getName()) == null) {
                return true;
            }
        }
        return false;
    }
    
    public static Map getDeployedTargetList(ConnectionSource dasConnection, String id) throws IOException {

        // Create a Map of target names
        Map deployedTargets = new HashMap();

        // Get all targets from AMX
        DomainConfig domainCfg = ProxyFactory.getInstance(dasConnection).getDomainRoot().getDomainConfig();
        final Map serverProxies = domainCfg.getStandaloneServerConfigMap();
        final Map clusterProxies = domainCfg.getClusterConfigMap();
        String[] serverNames = (String[])serverProxies.keySet().toArray(new String[0]);
        String[] clusterNames = (String[])clusterProxies.keySet().toArray(new String[0]);
        
        // Now for each stand alone server targets, check if this id is deployed on that target
        // If so, add this target to the map
        for(int i=0; i<serverNames.length; i++) {
            StandaloneServerConfig tgtProxy = (StandaloneServerConfig)
                    serverProxies.get(serverNames[i]);
            DeployedItemRefConfig refProxy = (DeployedItemRefConfig)
                    tgtProxy.getContainee(
                        XTypes.DEPLOYED_ITEM_REF_CONFIG, id);
            if(refProxy != null) {
                deployedTargets.put(serverNames[i], new Boolean(refProxy.getEnabled()));
            }
        }
        
        // Now for each cluster targets, check if this id is deployed on that target
        // If so, add this target to the map
        for(int i=0; i<clusterNames.length; i++) {
            ClusterConfig tgtProxy = (ClusterConfig)clusterProxies.get(
                    clusterNames[i]);
            DeployedItemRefConfig refProxy = (DeployedItemRefConfig)
                    tgtProxy.getContainee(
                        XTypes.DEPLOYED_ITEM_REF_CONFIG, id);
            if(refProxy != null) {
                deployedTargets.put(clusterNames[i], new Boolean(refProxy.getEnabled()));
            }
        }
        return deployedTargets;
    }    
    
    public static DeploymentStatus 
                getDeploymentStatusFromAdminStatus(com.sun.appserv.management.deploy.DeploymentStatus stat) {
        DeploymentStatus tmp = new DeploymentStatus();
        tmp.setStageStatus(stat.getStageStatus());
        if(stat.getThrowable() != null) {
            tmp.setStageException(stat.getThrowable());
        }
        tmp.setStageDescription(stat.getStageDescription());
        tmp.setStageStatusMessage(stat.getStageStatusMessage());
        Iterator it = stat.getSubStages();
        while(it.hasNext()) {
            com.sun.appserv.management.deploy.DeploymentStatus stageStatus = 
                            DeploymentSupport.mapToDeploymentStatus((Map)it.next());
            tmp.addSubStage(getDeploymentStatusFromAdminStatus(stageStatus));
        }

        if (stat.getAdditionalStatus() != null) {
            it = stat.getAdditionalStatus().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                tmp.addProperty((String)entry.getKey(), (String)entry.getValue());
            }
        }
        return tmp;
    }    

    public static void doWsdlFilePublishing(DeploymentStatus status,
                                      ConnectionSource conn) 
            throws IOException {
        if (status == null) { return; }
        String sep = DeploymentStatus.KEY_SEPARATOR;

        String key = DeploymentStatus.MODULE_ID;
        String moduleID = status.getProperty(key);

        key = moduleID + sep + DeploymentStatus.SUBMODULE_COUNT;
        if (status.getProperty(key) == null) { //standalone module
            doWsdlFilePublishing(moduleID, status, conn);
        } else {
            int counter = (Integer.valueOf(status.getProperty(key))).intValue();
            for (int i = 0; i < counter; i++) { //for each submodule
                key = moduleID + sep + DeploymentStatus.MODULE_ID + sep + 
                        String.valueOf(i);
                String subModuleID = status.getProperty(key);
                doWsdlFilePublishing(subModuleID, status, conn);
            }
        }
    }

    private static void doWsdlFilePublishing(String keyPrefix, 
            DeploymentStatus status, ConnectionSource conn) 
                throws IOException {
        String sep = DeploymentStatus.KEY_SEPARATOR;
        String key = keyPrefix + sep + DeploymentStatus.MODULE_TYPE;
        ModuleType moduleType = ModuleType.getModuleType((new Integer(status.getProperty(key))).intValue());

        //only EAR, WAR and EJB archives could contain wsdl files for publish
        if (!(ModuleType.EAR.equals(moduleType) ||
              ModuleType.WAR.equals(moduleType) ||
              ModuleType.EJB.equals(moduleType))) {
            return;
        }

        key = keyPrefix + sep + DeploymentStatus.WSDL_PUBLISH_URL;
        String clientPublishURL = status.getProperty(key);

        if (clientPublishURL == null) { // No file publishing
            return;
        }

        URL u = new URL(clientPublishURL);
        String destinationDir = (new File(u.getFile())).getAbsolutePath();

        key = keyPrefix + sep + DeploymentStatus.WSDL_FILE_ENTRIES + sep + 
                DeploymentStatus.COUNT;
        int counter = (Integer.valueOf(status.getProperty(key))).intValue();
        for (int i = 0; i < counter; i++) {
            key = keyPrefix + sep + DeploymentStatus.WSDL_FILE_ENTRIES + sep +
                    String.valueOf(i);
            String entryName = 
                    (status.getProperty(key)).replace('/', File.separatorChar);

            key = key + sep + DeploymentStatus.WSDL_LOCATION;
            String wsdlFileLocation = status.getProperty(key);

            // publish file, preserving its location relative to the module 
            // wsdl directory.
            File outputFile = new File(destinationDir, entryName);
            File parentDir = outputFile.getParentFile();
            if( !parentDir.exists() ) {
                boolean madeDirs = parentDir.mkdirs();
                if( !madeDirs ) {
                    throw new IOException("Error creating "+outputFile);
                }
            }
            
            downloadFile(new File(wsdlFileLocation), outputFile, conn);
            //@@@ should log the location
        }
    }

    public static String downloadClientStubs (String moduleID, String destDir,
                                       ConnectionSource dasConnection) 
            throws IOException {
        Object[] params     = new Object[] {moduleID};
        String[] signature  = new String[] {"java.lang.String"};
        MBeanServerConnection mbsc = 
                        dasConnection.getExistingMBeanServerConnection();
        try {
            ModuleType moduleType = getModuleType(mbsc, moduleID);

            // only ejb, appclient, application are applicable for
            // retrieving client stubs
            if (!(ModuleType.EJB.equals(moduleType) ||
                ModuleType.CAR.equals(moduleType) ||
                ModuleType.EAR.equals(moduleType))) {
                DOLUtils.getDefaultLogger().log(
                    Level.WARNING, "retrieve_client_stub_not_applicable", 
                        new Object[] {moduleID, moduleType});
               return null;
            }

            //Note that we still rely on the SystemsServiceMBean to retrieve 
            //the client jar location from the server.  This should only 
            //happen on DAS.
            ObjectName ssMBean = ObjectNames.getSystemServicesObjectName();
            
            String filePath = (String) mbsc.invoke(
                ssMBean, "getClientStubJarLocation", params, signature);
            File srcFile = new File(filePath);
            downloadFile(srcFile, new File(destDir, srcFile.getName()), dasConnection);
            String exportedFileLocation = 
                new File(destDir, srcFile.getName()).getAbsolutePath();
            return exportedFileLocation;
        }catch(Exception e) {
            throw (IOException)(new IOException(e.getLocalizedMessage()).initCause(e));
        }
    }

    public static void downloadFile(File srcFile, File destFile, 
                                    ConnectionSource dasConnection) 
            throws IOException {

        File destDir = destFile.getParentFile();
        if (!destDir.exists() || !destDir.isDirectory() || !destDir.canWrite()) {
            //@@@ i18n
            throw new IOException(
                "Problem accessing directory " + destDir.getPath());
        }

        UploadDownloadMgr downloadMgr = 
            ProxyFactory.getInstance(dasConnection).getDomainRoot().getUploadDownloadMgr();
        int chunkSize = 32 * 1024;
        FileOutputStream fos = null;
        try {
            Object downloadID = downloadMgr.initiateDownload(srcFile, false);
            fos = new FileOutputStream(destFile);
            boolean done = false;
            while (!done)
            {
                byte[] bytes = downloadMgr.downloadBytes(downloadID, chunkSize);
                fos.write(bytes, 0, bytes.length);
                if (bytes.length < chunkSize) { done = true; }
            }
        } finally {
            if (fos != null) {
                try { fos.close(); }
                catch (Exception e) {}
            }
        }
    }
    
    public static void setResourceOptions (Map options, String rAction, 
        SunTarget[] targetList)  {
        options.put(DeploymentProperties.RESOURCE_ACTION, rAction);
        options.put(DeploymentProperties.RESOURCE_TARGET_LIST, 
            getTargetStringFromTargetList(targetList));
    }

    public static void setResourceOptions (Map options, String rAction,
        String target)  {
        options.put(DeploymentProperties.RESOURCE_ACTION, rAction);
        options.put(DeploymentProperties.RESOURCE_TARGET_LIST,
            target);
    }

    public static String getTargetStringFromTargetList (
        SunTarget[] targetList) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < targetList.length; i++) {
            sb.append(targetList[i].getName());
            if (i != targetList.length -1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

}
