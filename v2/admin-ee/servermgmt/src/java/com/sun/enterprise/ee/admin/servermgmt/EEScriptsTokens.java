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
 * EEScriptsTokens.java
 *
 * Created on August 18, 2003, 10:54 AM
 */

package com.sun.enterprise.ee.admin.servermgmt;

import java.io.File;

import com.sun.enterprise.admin.util.TokenValue;
import com.sun.enterprise.admin.util.TokenValueSet;

import com.sun.enterprise.admin.servermgmt.RepositoryConfig;

import com.sun.enterprise.util.io.FileUtils;

/**
 *
 * @author  kebbs
 */
public final class EEScriptsTokens {
       
    public static final String CONFIG_HOME = "CONFIG_HOME";
    public static final String INSTANCE_ROOT = "INSTANCE_ROOT";
            
    public static final String SERVER_NAME = "SERVER_NAME";
    public static final String DOMAIN_NAME = "DOMAIN_NAME";
    public static final String CLUSTER_NAME = "CLUSTER_NAME";

    //Used to tokenize node agent start/stop scripts
    public static TokenValueSet getBaseTokenValueSet(RepositoryConfig config)
    {
        final TokenValueSet tokens = new TokenValueSet();        
        TokenValue tv = new TokenValue(CONFIG_HOME, config.getConfigRoot());
        tokens.add(tv);     
        File instanceRoot = new EEFileLayout(config).getRepositoryDir();
        // removed because on windows couldn't cd to the config directory, this is like the domain now
        //tv = new TokenValue(INSTANCE_ROOT, FileUtils.makeForwardSlashes(instanceRoot.getAbsolutePath()));        
        tv = new TokenValue(INSTANCE_ROOT, instanceRoot.getAbsolutePath());        
        tokens.add(tv);
        return tokens;
    }
    
    //Used to tokenize node agent start/stop scripts
    public static TokenValueSet getTokenValueSet(AgentConfig agentConfig)
    {
        TokenValueSet tokens = getBaseTokenValueSet(agentConfig);
        TokenValue tv = new TokenValue(SERVER_NAME, agentConfig.getAgentName());
        tokens.add(tv);
        return tokens;
    }
    
    //Used to tokenize server instance start/stop scripts
    public static TokenValueSet getTokenValueSet(InstanceConfig instanceConfig) {
        TokenValueSet tokens = getBaseTokenValueSet(instanceConfig);
        
        TokenValue tv = new TokenValue(SERVER_NAME, instanceConfig.getInstanceName());
        tokens.add(tv);
        
        //Use the %%%DOMAIN_NAME%%% token for the agent name for now since we do
        //not want to alter startserv script too much at this point.
        tv = new TokenValue(DOMAIN_NAME, instanceConfig.getRepositoryName());
        tokens.add(tv);
        
        return tokens;
    }
}


