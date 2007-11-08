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

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.client.ConnectionSource;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.client.TLSParams;
import com.sun.appserv.management.config.*;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.deploy.DeploymentMgr;
import com.sun.appserv.management.util.misc.SetUtil;
import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;
import com.sun.enterprise.deployapi.ProgressObjectImpl;
import com.sun.enterprise.deployapi.SunTarget;
import com.sun.enterprise.deployapi.SunTargetModuleID;
import com.sun.enterprise.deployment.backend.DeploymentStatus;
import com.sun.enterprise.deployment.client.JESProgressObject;
import com.sun.enterprise.deployment.deploy.shared.Archive;
import com.sun.enterprise.deployment.util.DeploymentProperties;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.util.i18n.StringManager;

import java.io.*;
import java.net.URL;
import java.util.*;

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.net.ssl.X509TrustManager;

/**
 * This interface implements basic deployment related facilities 
 * such as deploying any j2ee modules on a Domain Admin Server
 * or target servers as well as retrieving non portable artifacts
 * for successful runs in a client mode configuration.
 *
 */
public class DeploymentFacilityImpl implements DeploymentFacility {

    protected ConnectionSource dasConnection = null;
    protected ServerConnectionIdentifier serverId = null;
    protected SunTarget domain = null;
    protected Boolean localConnection = Boolean.FALSE;

    private static final String DAS = "server";
    private static StringManager localStrings = StringManager.getManager(DeploymentFacilityImpl.class);
    
    public DeploymentFacilityImpl() {}
    
    /**
     * Connects to a particular instance of the domain adminstration 
     * server using the provided connection information
     */
    public boolean connect(ServerConnectionIdentifier targetDAS) {
        try {
            TLSParams tlsParams = null;
            if (targetDAS.isSecure()) {
                X509TrustManager trustManager = 
                    (X509TrustManager)targetDAS.getConnectionEnvironment().
                    get(DefaultConfiguration.TRUST_MANAGER_PROPERTY_NAME);
                tlsParams = new TLSParams(trustManager, null);
            }

            dasConnection = new AppserverConnectionSource(
                AppserverConnectionSource.PROTOCOL_HTTP,
                targetDAS.getHostName(), targetDAS.getHostPort(),
                targetDAS.getUserName(), targetDAS.getPassword(),
                tlsParams, null);
        } catch (IllegalArgumentException illEx) {
            return false;
        }
        serverId = targetDAS;
        domain = new SunTarget(targetDAS);
        domain.setConnectionSource(dasConnection);
        domain.setTargetType(TargetType.DOMAIN);        
        return true;
    }
    
    /** 
     * @return true if we are connected to a domain adminstration 
     * server
     */
    public boolean isConnected() {
        if(dasConnection == null) {
            return false;
        }
        return true;
    }
    
    /** 
     * Disconnects from a domain administration server and releases
     * all associated resouces.
     */
    public boolean disconnect() {
        dasConnection = null;
        return true;
    }
        
    /**
     * Initiates a deployment operation on the server, using a source 
     * archive abstraction and an optional deployment plan if the 
     * server specific information is not embedded in the source 
     * archive. The deploymentOptions is a key-value pair map of 
     * deployment options for this operations. Once the deployment 
     * is successful, the targets server instances 
     * 
     * @param source is the j2ee module abstraction (with or without 
     * the server specific artifacts). 
     * @param deploymenPlan is the optional deployment plan is the source 
     * archive is portable.
     * @param the deployment options
     * @return a JESProgressObject to receive deployment events.
     */
    public JESProgressObject deploy(Target[] targets, Archive source, Archive deploymentPlan, Map deploymentOptions) {
        if(!isConnected()) {
            throw new IllegalStateException(localStrings.getString("enterprise.deployment.client.disconnected_state"));
        }
        
        SunTarget[] targetList = getSunTargets(targets);
        ProgressObjectImpl progressObject = new DeployAction(targetList);
        Object args[] = new Object[8];
        args[0] = dasConnection;
        args[1] = source;
        args[2] = deploymentPlan;
        args[3] = (deploymentOptions == null) ? new HashMap() : DeploymentProperties.propsToMap((Properties)deploymentOptions);
        args[4] = targetList;
        args[5] = domain;
        args[6] = localConnection;
        args[7] = serverId;
        progressObject.setCommand(CommandType.DISTRIBUTE, args);
        Thread newThread =  new Thread(progressObject);
        newThread.start();
        return progressObject;
    }
    
    /**
     * Initiates a undeployment operation on the server 
     * @param module ID for the component to undeploy
     * @return a JESProgress to receive undeployment events
     */
    // FIXME - this should go once admin-cli changes it code
    public JESProgressObject undeploy(Target[] targets, String moduleID) {
        return(undeploy(targets, moduleID, null));
    }
    
    public JESProgressObject undeploy(Target[] targets, String moduleID, Map options) {
        if(!isConnected()) {
            throw new IllegalStateException(localStrings.getString("enterprise.deployment.client.disconnected_state"));
        }
        SunTarget[] targetList = getSunTargets(targets);
        ProgressObjectImpl progressObject = new UndeployAction(targetList);
        Object args[] = new Object[6];
        args[0] = dasConnection;
        args[1] = moduleID;
        args[2] = (options == null) ? new HashMap() : DeploymentProperties.propsToMap((Properties)options);
        args[3] = targetList;
        args[4] = domain;
        args[5] = localConnection;
        progressObject.setCommand(CommandType.UNDEPLOY, args);
        Thread newThread =  new Thread(progressObject);
        newThread.start();
        return progressObject;
    }
    
    /**
     * Enables a deployed component on the provided list of targets.
     */ 
    public JESProgressObject enable(Target[] targets, String moduleID) {
        return(changeState(targets, moduleID, CommandType.START));
    }

    /**
     * Disables a deployed component on the provided list of targets
     */
    public JESProgressObject disable(Target[] targets, String moduleID) {
        return(changeState(targets, moduleID, CommandType.STOP));
    }

    private JESProgressObject changeState(Target[] targets, String moduleID, CommandType cmd) {
        if(!isConnected()) {
            throw new IllegalStateException(localStrings.getString("enterprise.deployment.client.disconnected_state"));
        }
        SunTarget[] targetList = getSunTargets(targets);
        ProgressObjectImpl progressObject = new ChangeStateAction(targetList);
        Object args[] = new Object[5];
        args[0] = dasConnection;
        args[1] = targetList;
        args[2] = moduleID;
        args[3] = cmd;
        args[4] = domain;
        progressObject.setCommand(cmd, args);
        Thread newThread =  new Thread(progressObject);
        newThread.start();
        return progressObject;        
    }
    
    /**
     * Add an application ref on the selected targets
     */ 
    public JESProgressObject createAppRef(Target[] targets, String moduleID, Map options) {
        return(doApplicationReferenceAction(targets, moduleID, options, CommandType.DISTRIBUTE));
    }

    /**
     * remove the application ref for the provided list of targets.
     */
    public JESProgressObject deleteAppRef(Target[] targets, String moduleID, Map options) {
        return(doApplicationReferenceAction(targets, moduleID, options, CommandType.UNDEPLOY));
    }

    private JESProgressObject doApplicationReferenceAction(Target[] targets, String moduleID, Map options, CommandType cmd) {
        if(!isConnected()) {
            throw new IllegalStateException(localStrings.getString("enterprise.deployment.client.disconnected_state"));
        }
        SunTarget[] targetList = getSunTargets(targets);
        ProgressObjectImpl progressObject = new ApplicationReferenceAction(targetList);
        Object args[] = new Object[5];
        args[0] = dasConnection;
        args[1] = targetList;
        args[2] = moduleID;
        args[3] = cmd;
        args[4] = (options == null) ? new HashMap() : DeploymentProperties.propsToMap((Properties)options);
        progressObject.setCommand(cmd, args);
        Thread newThread =  new Thread(progressObject);
        newThread.start();
        return progressObject;        
    }

    /**
     * list all application refs that are present in the provided list of targets
     */
    public TargetModuleID[] listAppRefs(String[] targets) throws IOException {
        if(!isConnected()) {
            throw new IllegalStateException(localStrings.getString("enterprise.deployment.client.disconnected_state"));
        }
        Vector tmpVector = new Vector();
        DomainConfig domainCfg = ProxyFactory.getInstance(dasConnection).getDomainRoot().getDomainConfig(); 
        Map serverProxies = domainCfg.getStandaloneServerConfigMap();
        Map clusterProxies = domainCfg.getClusterConfigMap();
        Map clusteredServerProxies = domainCfg.getClusteredServerConfigMap();
        for(int i=0; i<targets.length; i++) {
            Set proxySet = null;
            if(serverProxies.get(targets[i]) != null) {
                StandaloneServerConfig tgtProxy =
                    (StandaloneServerConfig)domainCfg.getContainee(
                        XTypes.STANDALONE_SERVER_CONFIG, targets[i]);
                proxySet = tgtProxy.getContaineeSet(XTypes.DEPLOYED_ITEM_REF_CONFIG);
            } else if(clusterProxies.get(targets[i]) != null) {
                ClusterConfig tgtProxy = 
                    (ClusterConfig)domainCfg.getContainee(
                        XTypes.CLUSTER_CONFIG, targets[i]);
                proxySet = tgtProxy.getContaineeSet(XTypes.DEPLOYED_ITEM_REF_CONFIG);
            } else if(clusteredServerProxies.get(targets[i]) != null) {
                ClusteredServerConfig tgtProxy = 
                    (ClusteredServerConfig)domainCfg.getContainee(
                        XTypes.CLUSTERED_SERVER_CONFIG, targets[i]);
                proxySet = tgtProxy.getContaineeSet(XTypes.DEPLOYED_ITEM_REF_CONFIG);
            } else if(TargetType.DOMAIN.equals(targets[i])) {
                StandaloneServerConfig tgtProxy =
                    (StandaloneServerConfig)domainCfg.getContainee(
                        XTypes.STANDALONE_SERVER_CONFIG, DAS);
                proxySet = tgtProxy.getContaineeSet(XTypes.DEPLOYED_ITEM_REF_CONFIG);
            } else {
                return null;
            }
            Object[] appRefs = proxySet.toArray();
            for(int k=0; k<appRefs.length; k++) {
                SunTarget aTarget = new SunTarget(serverId);
                aTarget.setAppServerInstance(targets[i]);
                aTarget.setConnectionSource(dasConnection);
                DeployedItemRefConfig item = (DeployedItemRefConfig) appRefs[k];
                SunTargetModuleID tgtId =  new SunTargetModuleID(item.getRef(), aTarget);
                tmpVector.add(tgtId);
            }
        }
        SunTargetModuleID[] result = new SunTargetModuleID[tmpVector.size()];
        return (TargetModuleID[]) tmpVector.toArray(result);
    }

    /** 
     * Create SunTarget[] from given Target[]
     */
    private SunTarget[] getSunTargets(Target[] givenTargets) throws IllegalArgumentException {
        SunTarget[] result = new SunTarget[givenTargets.length];
        for(int i=0; i<givenTargets.length; i++) {
            if(givenTargets[i] instanceof SunTarget) {
                result[i] = new SunTarget((SunTarget)givenTargets[i]);
            } else {
                throw new IllegalArgumentException(localStrings.getString("enterprise.deployment.client.notASunTarget",
                                                    givenTargets[i].getClass().getName()));
            }
        }
        return result;
    }
    
    /**
     * Downloads a particular file from the server repository. 
     * The filePath is a relative path from the root directory of the 
     * deployed component identified with the moduleID parameter. 
     * The resulting downloaded file should be placed in the 
     * location directory keeping the relative path constraint. 
     * Note that the current implementation only supports the download
     * of the appclient jar file.
     * 
     * @param location is the root directory where to place the 
     * downloaded file
     * @param moduleID is the moduleID of the deployed component 
     * to download the file from
     * @param moduleURI is the relative path to the file in the repository 
     * @return the downloaded local file absolute path.
     */
    public String downloadFile(File location, String moduleID, String moduleURI) 
            throws IOException {
        if(!isConnected()) {
            throw new IllegalStateException(
                localStrings.getString("enterprise.deployment.client.disconnected_state"));
        }

        return DeploymentClientUtils.downloadClientStubs(
                    moduleID, location.getAbsolutePath(), dasConnection);
    }
    
    /**
     * Wait for a Progress object to be in a completed state 
     * (sucessful or failed) and return the DeploymentStatus for 
     * this progress object.
     * @param the progress object to wait for completion
     * @return the deployment status
     */
    public DeploymentStatus waitFor(JESProgressObject po) {
        DeploymentStatus status = null;
        do {
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException ie) {
                // Exception swallowed deliberately
            }
            status = po.getCompletedStatus();
        } while(status == null);
        return status;
    }
     
    public Target[] createTargets(String[] targets ) {
        if(!isConnected()) {
            throw new IllegalStateException(localStrings.getString("enterprise.deployment.client.disconnected_state"));
        }
        Target[] targetsArray = new Target[targets.length];
        Map serverProxies = null;
        Map clusterProxies = null;

        try {
            // parse through given targets
            for(int i=0; i<targets.length; i++) {
                
                // if this is "domain" add a domain target
                if(TargetType.DOMAIN.equals(targets[i])) {
                    // Add a domain target
                    SunTarget dom = new SunTarget(serverId);
                    dom.setAppServerInstance(TargetType.DOMAIN);
                    dom.setConnectionSource(dasConnection);
                    dom.setTargetType(TargetType.DOMAIN);
                    targetsArray[i] = dom;
                    continue;
                }
                // if this is "server" add a server target
                if(DAS.equals(targets[i])) {
                    // Add a target for default server
                    SunTarget serv = new SunTarget(serverId);
                    serv.setAppServerInstance(DAS);
                    serv.setConnectionSource(dasConnection);
                    serv.setTargetType(TargetType.STAND_ALONE_SERVER);
                    targetsArray[i] = serv;
                    continue;
                }
                // for PE, it will not come here at all; go ahead and get proxies and server/cluster keysets from the proxies
                if(serverProxies == null || clusterProxies == null) {
                    DomainConfig domainCfg = ProxyFactory.getInstance(dasConnection).getDomainRoot().getDomainConfig();
                    serverProxies = domainCfg.getStandaloneServerConfigMap();
                    clusterProxies = domainCfg.getClusterConfigMap();
                }
                // check if ctarget is a stand alone server
                if(serverProxies.get(targets[i]) != null) {
                    SunTarget aTarget = new SunTarget(serverId);
                    aTarget.setAppServerInstance(targets[i]);
                    aTarget.setConnectionSource(dasConnection);
                    aTarget.setTargetType(TargetType.STAND_ALONE_SERVER);
                    targetsArray[i] = aTarget;
                    continue;
                }
                // check if ctarget is a cluster
                if(clusterProxies.get(targets[i]) != null) {
                    SunTarget aTarget = new SunTarget(serverId);
                    aTarget.setAppServerInstance(targets[i]);
                    aTarget.setConnectionSource(dasConnection);
                    aTarget.setTargetType(TargetType.CLUSTER);
                    targetsArray[i] = aTarget;
                    continue;
                }
                // if we are here, it means given target does not exist at all - return null
                return null;
            }
        } catch (Throwable ex) {
            // it's too late to change the DeploymentFacility.createTargets 
            // interface to throw approriate exceptions as this point, 
            // but let's at least re-throw the RuntimeException so 
            // connection failure (for example, due to wrong user/password) 
            // could be reported back to user.
            if (ex instanceof RuntimeException) {
                throw (RuntimeException)ex;
            }
            return null;
        }
        return targetsArray;
    }
}
