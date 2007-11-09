/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.phobos;

import com.sun.enterprise.v3.deployment.AbstractDeployer;
import com.sun.enterprise.v3.deployment.DeployCommand;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Service;

import java.io.File;
import java.util.Properties;


/**
 * Phobos Deployer
 *
 * @author Jerome Dochez
 */
@Service
public class PhobosDeployer extends AbstractDeployer
        implements Deployer<PhobosContainer, GlassFishPhobosAdapter> {

    
    /**
     * Loads the phobos container for a new phobos application.
     */
    public GlassFishPhobosAdapter load(PhobosContainer container, DeploymentContext context) {
       
        ReadableArchive source = context.getSource();

        // so far context root is application name`
        Properties parameters = context.getCommandParameters();
        String contextRoot = "/" + parameters.getProperty(DeployCommand.NAME);

        Properties envProp = new Properties();
        envProp.put("phobos.applicationDir", source.getURI().getSchemeSpecificPart() + File.separator + "application");
        envProp.put("phobos.frameworkDir", System.getProperty("phobos.home") + File.separator + "framework");
        envProp.put("phobos.environmentDir", source.getURI().getSchemeSpecificPart() + File.separator + "environment");
        envProp.put("phobos.staticDir", source.getURI().getSchemeSpecificPart() + File.separator + "static");
        envProp.put("com.sun.phobos.javascript.useInterpreter", "true");
            
        context.getLogger().fine("Context root is " + contextRoot);
                      
        return new GlassFishPhobosAdapter(contextRoot, envProp);
    }
    
    public void unload(GlassFishPhobosAdapter container, DeploymentContext context) {            
    }
      
}
