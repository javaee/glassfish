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

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import java.io.File;
import java.util.logging.Level;

/**
 *
 * @author mu125243
 */
public class PERemoteTargetResolver extends TargetResolver {
    
    private static final String REPOSITORY_CONFIG_CLASS_NAME =
            "com.sun.enterprise.admin.servermgmt.DomainConfig";
    private static final String GET_REP_ROOT_METHOD ="getRepositoryRoot";
    private static final String GET_REP_NAME = "getRepositoryName";
    
    /** Creates a new instance of PERemoteTargetResolver */
    public PERemoteTargetResolver(String target, String repositoryDir, boolean local) {
        super(target,repositoryDir,local);
    }
    
    
    public boolean validateTarget() throws DiagnosticException {
        ConfigContext configContext = getServerContext().getConfigContext();
        try {
            String domainName = ServerHelper.getAdministrativeDomainName(
                    configContext,null);
            
            if(domainName != null)
                return domainName.equals(target);
        } catch(ConfigException ce) {
            throw new DiagnosticException("Couldn't determine domain name");
        }
        throw new DiagnosticException("Couldn't determine domain name");
    }
    
    
    protected ServerContext getServerContext() {
        return ApplicationServer.getServerContext();
    }
    
    protected void setExecutionContext() {
        context = ExecutionContext.REMOTE_EC;
    }
    
    protected void determineRepositoryDetails() {
          File instanceRoot = new File(getServerContext().
                    getInstanceEnvironment().getInstancesRoot());
          repositoryDir = instanceRoot.getParentFile().getAbsolutePath();
          repositoryName = instanceRoot.getName();

          logger.log(Level.FINE, "diagnostic-service.resolver_repositoryDetails",
                    new Object[] {repositoryDir, repositoryName});
        /*try {
            Class classObj = Class.forName(REPOSITORY_CONFIG_CLASS_NAME);
            Constructor constructorObj = 
                    classObj.getDeclaredConstructor(new Class[]{});
            Object obj = constructorObj.newInstance(new Object[]{});
            Method method = classObj.getMethod(GET_REP_ROOT_METHOD, null);
            repositoryDir = (String)method.invoke(obj, null);

            method = classObj.getMethod(GET_REP_NAME, null);
            repositoryName = (String)method.invoke(obj, null);
            
            logger.log(Level.FINEST, 
                "diagnostic-service.target_dir", repositoryDir);
                if(type.equals(EETargetType.INSTANCE)) {
                    targetDir = targetDir + File.separator + naName + File.separator + target;
                } else {
                    targetDir = targetDir + File.separator + target;
                }
        } catch(Exception e){
            // WILL NEVER
        }*/
    }
    
    protected void determineTargetType() {
        if(type == null)
            setTargetType(TargetType.DAS);
    }
    
    protected void determineInstances(){
        addInstance(TargetType.DAS.getType());
    }
  
  
}
