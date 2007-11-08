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

package com.sun.enterprise.ee.diagnostics;

import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.ee.diagnostics.collect.*;
import com.sun.enterprise.diagnostics.*;
import com.sun.enterprise.diagnostics.collect.Collector;
import com.sun.enterprise.diagnostics.report.html.HTMLReportWriter;
import com.sun.enterprise.ee.diagnostics.report.html.EEHTMLReportWriter;
import com.sun.logging.LogDomains;
import java.io.File;
import java.util.Map;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mu125243
 */
public class EEBackendObjectFactory extends SingleInstanceObjectFactory {
    private List<String> instances;
    private String targetType;
    
    
    private Logger logger = LogDomains.getLogger(LogDomains.ADMIN_LOGGER);
    /** Creates a new instance of BackendObjectFactory */
    public EEBackendObjectFactory(Map input, 
            List<String> instances, String targetType) {
        super(input);
        this.instances = instances;
        this.targetType = targetType;
    }
    
    
    public TargetResolver createTargetResolver(boolean local) throws DiagnosticException {
        EETargetType type = null;
        if(targetType != null) 
            type = new EETargetType(targetType);
        if (local){
            logger.log(Level.FINEST, "diagnostic-service.local_target_resolver") ;
            return new EELocalTargetResolver(options.getTargetName(), 
                options.getTargetDir(), instances,  type);
        }
        else {
            logger.log(Level.FINEST, "diagnostic-service.remote_target_resolver") ;
            return new EERemoteTargetResolver(options.getTargetName(), 
                options.getTargetDir(), instances,  type);
        }
        
    }
    
    public Collector createHarvester() {
        ExecutionContext context = config.getExecutionContext();
        
        // If execution context is DAS_EC ie. report generation is being invoked
        // in a remote mode and current process of execution of this code 
        // is DAS
        if(context.equals(EEExecutionContext.DAS_EC)) {
            logger.log(Level.FINEST, "diagnostic-service.domain_harvester");
            return new DomainHarvester(getReportConfig());
        }
        else { // the process of execution of else could be either client 
            // ie report generation is being invoked in local mode
            // or a node agent process in remote mode
            logger.log(Level.FINEST, "diagnostic-service.nodeagent_resolver") ;
            return new NodeAgentHarvester(getReportConfig());
        }
    }
    
    
    public HTMLReportWriter createHTMLReportWriter() {
        return new EEHTMLReportWriter(config);
    }

    public ReportGenerator createReportGenerator(ReportConfig config,
            Collector harvester,HTMLReportWriter reportWriter)
            throws DiagnosticException{
        return new EEReportGenerator(config, harvester, reportWriter);
    }
    
    protected  void analyzeInput()  throws DiagnosticException{
        super.analyzeInput();
        if(config != null && context.equals(EEExecutionContext.NODEAGENT_EC)) {
            //Read through map and add Service Config objects to ReportConfig obj
            Map configs = (Map)options.getMap().get(NodeAgentDataCollector.CONFIGS);
            if(configs != null) {
                String agentRepDir = target.getRepositoryDir() + File.separator +
                            target.getRepositoryName() ;
                String instanceRepDir = null;
                for (String instance : instances) {
                    Map instanceConfig = (Map)configs.get(instance);
                    if ((!(instance.equals(Constants.SERVER))) &&
                         (!(target.getType().equals(TargetType.INSTANCE)))){
                        // for example in case of node agent this value would be
                        //nodeagents/na1/instance1
                        instanceRepDir = agentRepDir + File.separator + instance;
                    }

                    // Read different attributes of diagnostic-service element 
                    // Create ServiceConfig object and add it to config
                    config.addInstanceSpecificConfig(new ServiceConfig(
                            ((Boolean)instanceConfig.get(ServerTags.COMPUTE_CHECKSUM)).booleanValue(),
                            ((Boolean)instanceConfig.get(ServerTags.CAPTURE_APP_DD)).booleanValue(), 
                            ((Boolean)instanceConfig.get(ServerTags.CAPTURE_INSTALL_LOG)).booleanValue(), 
                            ((Boolean)instanceConfig.get(ServerTags.VERIFY_CONFIG)).booleanValue(),
                            ((Boolean)instanceConfig.get(ServerTags.CAPTURE_HADB_INFO)).booleanValue(),
                            ((Boolean)instanceConfig.get(ServerTags.CAPTURE_SYSTEM_INFO)).booleanValue(),
                            ((Integer)instanceConfig.get(ServerTags.MIN_LOG_LEVEL)).intValue(),
                            ((Integer)instanceConfig.get(ServerTags.MAX_LOG_ENTRIES)).intValue(),
                            ((String)instanceConfig.get(ServerTags.LOG_FILE)), 
                            instanceRepDir, instance));
                }
            }
        }
    }
}
