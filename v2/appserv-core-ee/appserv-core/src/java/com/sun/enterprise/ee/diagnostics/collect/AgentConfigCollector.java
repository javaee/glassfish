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
import java.util.logging.Logger;

import com.sun.logging.LogDomains;

import com.sun.enterprise.ee.diagnostics.EEConstants;
import com.sun.enterprise.diagnostics.Data;
import com.sun.enterprise.diagnostics.collect.ConfigCollector;
import com.sun.enterprise.diagnostics.collect.WritableDataImpl;
import com.sun.enterprise.diagnostics.DiagnosticException;


/**
 * Responsible for capturing domain.xml, sun-acc.xml, server.policy, login.conf
 *
 * @author Manisha Umbarje
 */
public class AgentConfigCollector extends ConfigCollector {
    
    private String repositoryDir ;
    private String reportDir ;
        
    private static Logger logger = 
    LogDomains.getLogger(LogDomains.ADMIN_LOGGER);
    
    /**
     * @param repositoryDir central/local cache repository
     * @param reportDir directory in which config information is collected.
     * @targetType type of the target for which report generation is invoked
     * @nodeagent whether this collector is instantiated for a node agent or a 
     * instance
     */
    public AgentConfigCollector(String repositoryDir, String reportDir) {
        super(repositoryDir,reportDir);
    }
    
    
    /**
     * Capture config information
     * @throw DiagnosticException
     */
    public Data capture() throws DiagnosticException {
        WritableDataImpl dataObj = new WritableDataImpl();
        dataObj.addChild(super.captureXMLFile(EEConstants.DOMAIN_XML));
        dataObj.addChild(super.captureFile(EEConstants.SERVER_POLICY));
        dataObj.addChild(super.captureFile(EEConstants.LOGIN_CONF));
        dataObj.addChild(super.captureFile(EEConstants.NODEAGENT_DAS_PROPERTIES));
        dataObj.addChild(super.captureFile(EEConstants.NODEAGENT_PROPERTIES));
        
        return dataObj;
     }//captureConfigFiles
}
