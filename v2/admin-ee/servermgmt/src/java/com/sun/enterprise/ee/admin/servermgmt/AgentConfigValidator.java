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
 * AgentConfigValidator.java
 *
 * Created on August 17, 2003, 9:10 PM
 */

package com.sun.enterprise.ee.admin.servermgmt;

import com.sun.enterprise.admin.servermgmt.DomainConfigValidator;
import com.sun.enterprise.admin.servermgmt.FileValidator;
import com.sun.enterprise.admin.servermgmt.StringValidator;
import com.sun.enterprise.admin.servermgmt.PortValidator;

import com.sun.enterprise.util.i18n.StringManager;

/**
 *
 * @author  kebbs
 */
public class AgentConfigValidator extends DomainConfigValidator {
    
    private static final StringManager _strMgr = 
        StringManager.getManager(AgentConfigValidator.class);

    private static final String lInstallRoot = _strMgr.getString("installRoot");
    private static final String lAgentRoot = _strMgr.getString("agentRoot");    
    private static final String lAdminPort = _strMgr.getString("adminPort");
    private static final String lUser = _strMgr.getString("adminUuser");
    private static final String lPassword = _strMgr.getString("adminPassword");
    
    static DomainConfigEntryInfo[] _entries = new DomainConfigEntryInfo[]
    {
        new DomainConfigEntryInfo(AgentConfig.K_INSTALL_ROOT, 
                                  "java.lang.String", lInstallRoot, 
                                  new FileValidator(lInstallRoot, "dr")),
        new DomainConfigEntryInfo(AgentConfig.K_AGENT_ROOT, 
                                  "java.lang.String", lAgentRoot,
                                  new FileValidator(lAgentRoot, "drw")),        
        new DomainConfigEntryInfo(AgentConfig.K_ADMIN_PORT, 
                                  "java.lang.Integer", lAdminPort,
                                  new PortValidator(lAdminPort)),        
        new DomainConfigEntryInfo(AgentConfig.K_PASSWORD, 
                                  "java.lang.String", lPassword,
                                  new StringValidator(lPassword)),
        new DomainConfigEntryInfo(AgentConfig.K_USER, 
                                  "java.lang.String", lUser,
                                  new StringValidator(lUser))
    };
   
    AgentConfigValidator()
    {        
        super(_strMgr.getString("agentConfig"), AgentConfig.class, _entries);
    }    

    protected boolean isValidate(String name, Object domainConfig) {
      return true;
    }
}    
