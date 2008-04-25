
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
 *
 */

package com.sun.enterprise.groovy;

import com.sun.enterprise.v3.deployment.DeployCommand;
import com.sun.enterprise.v3.server.ServerEnvironment;
import com.sun.enterprise.v3.services.impl.GrizzlyService;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

/**
 * Deployer for Groovy scripts
 *
 * @author Martin Grebac 
 *
 */
@Service
public class GroovyDeployer implements Deployer<GroovyContainer, GroovyApplication> {
    
    @Inject
    ServerEnvironment env;
    
    @Inject
    GrizzlyService grizzlyAdapter;
    
    public boolean prepare(DeploymentContext context) {
        return true;
    }

    /**
     * Loads the meta date associated with the application.
     *
     * @parameters type type of metadata that this deployer has declared providing.
     */
    public <T> T loadMetaData(Class<T> type, DeploymentContext context) {
        return null;
    }

    /**
     * Returns the meta data assocated with this Deployer
     *
     * @return the meta data for this Deployer
     */
    public MetaData getMetaData() {
        return new MetaData(false, null, null, null);
    }

    public GroovyApplication load(GroovyContainer container, DeploymentContext context) {
        String contextRoot = getContextRoot(context);
        GroovyApplication adapter = registerAdapter(contextRoot, context);
        return adapter;
    }

    public void unload(GroovyApplication container, DeploymentContext context) {
        grizzlyAdapter.unregisterEndpoint(getContextRoot(context));
    }
    
    public void clean(DeploymentContext context) {
    }

    private String getContextRoot(DeploymentContext context) {
        String contextRoot = context.getCommandParameters().getProperty(DeployCommand.CONTEXT_ROOT);
        if (contextRoot == null) {
            contextRoot = env.getStartupContext().getArguments().get("--contextroot");
        }
        if (contextRoot == null || contextRoot.length() == 0) {
            contextRoot = "/" + context.getCommandParameters().getProperty(DeployCommand.NAME);
        }
        return contextRoot;
    }

    private GroovyApplication registerAdapter(String contextRoot, DeploymentContext context) {        
        GroovyApplication adapter = new GroovyApplication();
        String path = context.getCommandParameters().getProperty("path");
        if (path == null) {
            path = context.getCommandParameters().getProperty("location");
            if ((path != null) && (path.startsWith("file:/"))) {
                path = path.substring(5);
            }
        }
        adapter.setRootFolder(path);
        adapter.setContextRoot(contextRoot);
        context.getLogger().info("Loading application " + 
                context.getCommandParameters().getProperty(DeployCommand.NAME) 
                + " at " + contextRoot + " at " + path);
        grizzlyAdapter.registerEndpoint(contextRoot, adapter, adapter);
        return adapter;
    }

}
