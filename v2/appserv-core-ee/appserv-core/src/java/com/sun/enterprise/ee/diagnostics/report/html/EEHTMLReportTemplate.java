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
package com.sun.enterprise.ee.diagnostics.report.html;

import com.sun.enterprise.diagnostics.*;
import com.sun.enterprise.diagnostics.collect.DataType;
import com.sun.enterprise.diagnostics.report.html.*;
import com.sun.enterprise.ee.diagnostics.EETargetType;

import com.sun.enterprise.ee.diagnostics.EEConstants;
import com.sun.enterprise.ee.diagnostics.EEExecutionContext;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.ConfigContext;
import  com.sun.enterprise.admin.server.core.AdminService;
import java.util.logging.Logger;

import java.util.Iterator;
import java.util.ArrayList;

import java.io.File;
import com.sun.logging.LogDomains;

/**
 *
 * @author Manisha Umbarje
 */
public class EEHTMLReportTemplate extends HTMLReportTemplate {


    protected static final String hadb_information = "HADB Information";

    private static final Logger logger = 
            LogDomains.getLogger(LogDomains.ADMIN_LOGGER);
    
     /** 
      * Creates a new instance of EEHTMLReportTemplate
      * @ targetName name of the target for which HTML report is being generated
      * @ reportDir directory which contains data to generate HTML report summary
      */
    public EEHTMLReportTemplate(ReportConfig config, Data dataObj) {
            super(config,dataObj);
    }

    protected void addMiscellaneousInfo(){
        addHadbInfo();
}

    protected void addInstanceSpecificSection(Element element) {
        if(config.getExecutionContext().equals(EEExecutionContext.DAS_EC)){
            if(config.getTarget().getType().equals(EETargetType.DOMAIN) ||
                  config.getTarget().getType().equals(EETargetType.DAS) ||
                  config.getTarget().getType().equals(EETargetType.CLUSTER)) {
                ArrayList<String> nodeAgents = getNodeAgentNames();
                super.addInstanceSpecificSection(element);

                for(String nodeAgentName : nodeAgents) {
                    File file = new File(target.getIntermediateReportDir() +
                            File.separator + nodeAgentName);
                    if(file.exists()){
                        addLink(element, "Nodeagent : " + nodeAgentName, nodeAgentName +
                                File.separator + Defaults.REPORT_NAME , 1);
                    }
                }

            }
        } else {
            if (exists(target.getIntermediateReportDir(), EEConstants.AGENT))
                addLink(element, EEConstants.AGENT,
                        "." + File.separator + EEConstants.AGENT, 1);

            super.addInstanceSpecificSection(element);
        }
    }


    private void addHadbInfo(){

        boolean headerAdded=false;
        if( target.getType().equals(EETargetType.CLUSTER) ||
                target.getType().equals(EETargetType.DOMAIN) ){

        Iterator<Data> iterator = dataObjTraverser.getData(DataType.HADB_INFO);

        while(iterator.hasNext()){
            Data data = iterator.next();
            if(!headerAdded){
                addTitle(hadb_information, true, true, 0,0);
                headerAdded=true;
            }
            Element fileLink = new Link(data.getSource(), data.getSource(), null);
            bodyElement.add(fileLink);
            bodyElement.add(new HTMLElement(HTMLReportConstants.BR));
        }
        }
    }

    protected void addMonitoringInfo(Element element, String instanceName,
                                   int indentation ) {
        if (!config.getCLIOptions().isLocal()) {

            if (config.getExecutionContext().equals(EEExecutionContext.DAS_EC)) {

                if (config.getTarget().getType().equals(EETargetType.DOMAIN) ||
                        config.getTarget().getType().equals(TargetType.DAS)) {
                    addLink(element, monitoring_information,
                            Defaults.MONITORING_INFO_FILE, indentation);
                }
            } else {
                addLink(element, monitoring_information,
                                instanceName +
                                File.separator + Defaults.MONITORING_INFO_FILE,
                        indentation);
            }
        }
    }

    private ArrayList<String> getNodeAgentNames() {
        NodeAgent[] nodeagents = null;
        ArrayList<String> list = new ArrayList<String>();

        if(config.getTarget().getType().equals(EETargetType.DOMAIN)) {
            try {
                ConfigContext configContext =AdminService.getAdminService().
                        getAdminContext().getAdminConfigContext();
                Domain domain = ServerBeansFactory.getDomainBean(configContext);
                NodeAgents nodeAgentCollection = domain.getNodeAgents();
                nodeagents = nodeAgentCollection.getNodeAgent();
            } catch(Exception e) {
            }
        } else { // Safe to assume it's a cluster
            try {
                ConfigContext configContext = AdminService.getAdminService().
                        getAdminContext().getAdminConfigContext();
                nodeagents =
                        NodeAgentHelper.getNodeAgentsForCluster
                                (configContext, config.getTarget().getName());
            } catch(Exception e) {

            }
        }

        if(nodeagents != null) {
            for(NodeAgent agent : nodeagents) {
                list.add(agent.getName());
            }
        }
        return list;
    }
}
