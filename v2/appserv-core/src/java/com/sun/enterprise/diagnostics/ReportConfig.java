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
package com.sun.enterprise.diagnostics;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;


/**
 * Combined representation of diagnostic-service config elements in domain.xml
 * and command line options provided by the user at the time of report
 * generation. 
 * @author Manisha Umbarje
 */
public class ReportConfig {

    //CLI options
    private CLIOptions options;
    
    //Represents the target for which report is being generated
    private ReportTarget target;    
    
    private ExecutionContext context;
    
    // Configurtion of diagnostic-service element
    private List<ServiceConfig> configurations ;
    
    /**
     * Creates new instance of ReportConfig object
     */
    public ReportConfig(CLIOptions cliOptions, ReportTarget target, 
            ExecutionContext context) throws DiagnosticException {
        options = cliOptions;
        this.target = target;
        this.context = context;
        initialize();
    }
    
    /**
     * Add diagnostic service configuration corresponding to a instance
     * @param config diagnostic service config element from domain.xml
     */
    public void addInstanceSpecificConfig(ServiceConfig config) {
        if(config != null) {
            configurations.add(config);
        }
    }
    
    public void addInstanceConfigs(List<ServiceConfig> configs) {
        if(configs != null) {
            Iterator<ServiceConfig> iterator = configs.iterator();
            while(iterator.hasNext()) {
                addInstanceSpecificConfig(iterator.next());
            }
        }
    }

    public ServiceConfig getInstanceConfigByName(String name){
        ServiceConfig instanceConfig = null;
        for(ServiceConfig config : configurations){
            if(config.getInstanceName().equalsIgnoreCase(name)){
                instanceConfig = config;
                break;
            }
        }
        return instanceConfig;
    }
    
    /**
     * returns ReportTarget
     * @return target
     */
    public ReportTarget getTarget() {
        return target;
    }
    
    /**
     * @return CLIOptons
     */
    public CLIOptions getCLIOptions() {
        return options;
    }
    
    /**
     * Returns Execution context
     */
    public ExecutionContext getExecutionContext() {
        return context;
    }
    /**
     * In PE, only one configuration is returned. 
     * In SE/EE the report generation may be invoked for multiple instances
     * @return list of configurations
     */
    public Iterator<ServiceConfig> getInstanceConfigurations() {
         return configurations.iterator();
    }
 
    public String toString() {
        return options.toString() + target.toString() +  
                context.toString() +  configurations;
    }
    private void initialize() {
        configurations = new ArrayList();
    }
}
