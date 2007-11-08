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
import com.sun.enterprise.ee.diagnostics.EETargetType;
import com.sun.enterprise.diagnostics.collect.*;
import com.sun.enterprise.diagnostics.*;
import com.sun.enterprise.diagnostics.util.ExcludeExtensionFilter;
import com.sun.enterprise.diagnostics.util.FileUtils;
import com.sun.enterprise.ee.diagnostics.EEConstants;
import com.sun.enterprise.ee.diagnostics.EEExecutionContext;
import com.sun.logging.LogDomains;

import java.util.Iterator;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 *
 * @author Manisha Umbarje
 */
public class NodeAgentHarvester extends Harvester {
    
    protected static final Logger logger = 
            LogDomains.getLogger(LogDomains.ADMIN_LOGGER);
    
    /** Creates a new instance of NodeAgentHarvester */
    public NodeAgentHarvester(ReportConfig config) {
        super(config);
    }
    
    
    public void addRemoteCollectors() {
    }
    /**
     * Initialize collectors
     */
    public void initialize() throws DiagnosticException{
        super.initialize();
        TargetType type = target.getType();
        if(type.equals(EETargetType.NODEAGENT) || 
                (config.getExecutionContext().equals(EEExecutionContext.NODEAGENT_EC) &&
                type.equals(EETargetType.DOMAIN))) {
            // Initialize NodeAgentInfoCollector
            // Initialize Config Collector to collect domain.xml
            //login.conf, server.policy
            String repositoryDir  = target.getTargetDir() + 
                    EEConstants.AGENT_DIR;
            String reportDir = target.getIntermediateReportDir() + 
                    EEConstants.AGENT_DIR;
            addCollector(new AgentConfigCollector(repositoryDir ,reportDir));

            addCollector(new LogCollector(reportDir, 
                    repositoryDir + Defaults.DEST_LOG_FILE));
        }
    }
   
}
