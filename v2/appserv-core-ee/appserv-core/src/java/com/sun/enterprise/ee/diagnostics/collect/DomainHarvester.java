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
package com.sun.enterprise.ee.diagnostics.collect;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.diagnostics.*;
import com.sun.enterprise.diagnostics.collect.Harvester;
import com.sun.enterprise.diagnostics.collect.MonitoringInfoCollector;
import com.sun.enterprise.ee.diagnostics.EETargetType;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.logging.LogDomains;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;


/**
 * Responsible for configuring various collectors based on diagnostic service
 * configuration and colleting diagnostic information for the entire report.
 *
 * @author Manisha Umbarje
 */
public class DomainHarvester extends Harvester {
    
    private static Logger logger =
            LogDomains.getLogger(LogDomains.ADMIN_LOGGER);
    
    private Map nodeAgentToInstances = new HashMap(5);
    /**
     * Creates instance of Harvester
     * @param config combined representation of CLIOptions and ReportTarget
     */
    public DomainHarvester(ReportConfig config) {
        super(config);
        populateNodeAgentToInstances();
    }
    
    public void addRemoteCollectors() throws DiagnosticException{
        addDomainCollectors();
    }
    
    private void populateNodeAgentToInstances() {
        List<String> instances = config.getTarget().getInstances();
        if(instances != null) {
            Iterator<String> iterator = instances.iterator();
            ConfigContext configContext = AdminService.getAdminService().getAdminContext().getAdminConfigContext();
            while(iterator.hasNext()) {
                try {
                    String instanceName = iterator.next();
                    if(!(instanceName.equals(Constants.SERVER))) {
                        Server server = ServerHelper.getServerByName(configContext,
                                instanceName) ;
                        String nodeAgent = server.getNodeAgentRef();
                        Object obj = nodeAgentToInstances.get(nodeAgent);
                        if (obj == null) {
                            obj = new ArrayList(5);
                        }
                        ArrayList list =(ArrayList)obj;
                        list.add(instanceName);

                        nodeAgentToInstances.put(nodeAgent, list);
                    }
                } catch(ConfigException ce) {
                    logger.log(Level.WARNING,
                            "diagnostic-service.error_retrieving_na" ,ce);
                }
            }
        }
    }
    
    private void addDomainCollectors() throws DiagnosticException {
        Set<Object> keys = nodeAgentToInstances.keySet();
        Iterator keyIterator = keys.iterator();
        
        while(keyIterator.hasNext()) {
            String nodeAgent = (String)keyIterator.next();
            if(nodeAgent != null) {
                List<String> naInstances =
                        (List<String>)nodeAgentToInstances.get(nodeAgent);
                addCollector(new NodeAgentDataCollector(config.getCLIOptions(),
                        nodeAgent, naInstances,
                        config.getTarget().getType().getType()));
                if(target.getType().equals(EETargetType.NODEAGENT) || target.getType().equals(EETargetType.INSTANCE)){
                    nodeAgent=null;
                }
                addMonitoringInfoCollectors(nodeAgent, naInstances);
            }
        }
        //handle the case where there are no instances associated with the node-agent
        if(keys.size()==0 && target.getType().equals(EETargetType.NODEAGENT)){
            addCollector(new NodeAgentDataCollector(config.getCLIOptions(),
                    target.getName(), new ArrayList<String>(),
                    config.getTarget().getType().getType()));
        }

        String reportDir = config.getTarget().getIntermediateReportDir();

        if(target.getType().equals(TargetType.DAS) || target.getType().equals(EETargetType.DOMAIN)){
            addCollector(new MonitoringInfoCollector(null,Constants.SERVER,target.getIntermediateReportDir()));
        }

        if(target.getType().equals(EETargetType.CLUSTER)) {

            // collectors cannot be added for a cluster without any instances.
            if(keys.size()==0){
                throw new DiagnosticException("Cannot collect diagnostic information for cluster [ "+target.getName()+" ] without any instance");
            }
            try
            {
            ConfigContext configContext = com.sun.enterprise.admin.server.core.AdminService.getAdminService().getAdminContext().getAdminConfigContext();
            Domain domain = ServerBeansFactory.getDomainBean(configContext);
            Clusters clusters = domain.getClusters();

            Cluster targetCluster = clusters.getClusterByName(target.getName());

            boolean    captureHadbInfoEnabled = isHadbCaptureEnabled(configContext,targetCluster.getName());

                addHadbInfoCollector(captureHadbInfoEnabled, target.getName(), reportDir);
            }
            catch(ConfigException ce){
                logger.log(Level.WARNING,
                        "Config Exception" ,ce);
            }
            catch(DiagnosticException de){
                logger.log(Level.WARNING,
                        "Diagnostic Exception" ,de);
            }
        }

        if(target.getType().equals(EETargetType.DOMAIN)){
        try{
        ConfigContext configContext = com.sun.enterprise.admin.server.core.AdminService.getAdminService().getAdminContext().getAdminConfigContext();
        Domain domain = ServerBeansFactory.getDomainBean(configContext);
        Clusters clusters = domain.getClusters();
          Cluster clusterArray[] = clusters.getCluster();
            boolean captureHadbInfoEnabled=false;

            for(Cluster cluster : clusterArray ){
                captureHadbInfoEnabled = isHadbCaptureEnabled(configContext, cluster.getName());

                addHadbInfoCollector(captureHadbInfoEnabled, cluster.getName(),reportDir);
            }
        }

        catch(ConfigException ce){
            logger.log(Level.WARNING,
                    "Config Exception" ,ce);
        }
        catch(DiagnosticException de){
            logger.log(Level.WARNING,
                    "Diagnostic Exception" ,de);
        }
        }
    }

    private boolean isHadbCaptureEnabled(ConfigContext configContext, String clusterName) throws ConfigException, DiagnosticException {

        boolean captureHadbInfoEnabled =false;

        Server[] instances = ServerHelper.getServersInCluster(configContext,clusterName);
        if(instances != null && instances.length>0){
           ServiceConfigHelper configHelper = new ServiceConfigHelper(target.getRepositoryDir() +File.separator + target.getRepositoryName(),instances[0].getName(),true);
           String booleanValue = configHelper.getAttribute("capture-hadb-info");
           captureHadbInfoEnabled = Boolean.parseBoolean(booleanValue);
        }
        return captureHadbInfoEnabled;
    }

    private void addHadbInfoCollector(boolean captureHadbInfoCollectorEnabled, String targetName, String reportDir){
            if(captureHadbInfoCollectorEnabled){
                addCollector(new HadbInfoCollector(targetName, reportDir));
            }
    }
}
