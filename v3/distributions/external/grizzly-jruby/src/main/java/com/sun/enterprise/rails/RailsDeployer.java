
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

package com.sun.enterprise.rails;

import com.sun.enterprise.v3.deployment.DeployCommand;
import com.sun.enterprise.v3.server.ServerEnvironment;
import com.sun.enterprise.v3.services.impl.GrizzlyService;
import com.sun.grizzly.arp.DefaultAsyncHandler;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import com.sun.grizzly.jruby.RubyObjectPool;
import com.sun.grizzly.jruby.RubyRuntimeAsyncFilter;
import java.util.Iterator;
import org.jruby.RubyArray;
import org.jruby.RubyException;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

/**
 * Deployer for Rails applications
 *
 * @author Jerome Dochez
 */
@Service
public class RailsDeployer implements Deployer<RailsContainer, RailsApplication> {
    
    @Inject
    ServerEnvironment env;
    
    @Inject
    GrizzlyService grizzlyAdapter;
    
    private static final String contextRootStr = "--contextRoot";
    private static final String numberOfRuntimeStr = "--jruby.runtime";
    
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

    public RailsApplication load(RailsContainer container, DeploymentContext context) {
        String contextRoot = getContextRoot(context);
        RubyObjectPool pool = setupRubyObjectPool(container, context);
        RailsApplication adapter = registerAdapter(pool, contextRoot, context);

        try {
            System.out.println("Starting Rails instances"); 
            pool.start();
        } catch (RaiseException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            // try to put some helpful information in the logs
            RubyException re = e.getException();
            String rubyMessage = (String)JavaEmbedUtils.rubyToJava(pool.borrowRuntime(), re.message, String.class);
            String message = "Failed to load Rails: " + rubyMessage + "\n";
            RubyArray backtrace = (RubyArray)re.backtrace();
            for(Iterator traceIt = backtrace.iterator(); traceIt.hasNext(); ) {
                    String traceLine = (String)traceIt.next();
                    message += "\t" + traceLine + "\n";
            }
            return null;
        }
         
        return adapter;
    }

    public void unload(RailsApplication container, DeploymentContext context) {
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

    private RailsApplication registerAdapter(RubyObjectPool pool,
            String contextRoot, DeploymentContext context) {
        RailsApplication adapter = new RailsApplication(pool);
        adapter.setContextRoot(contextRoot);
        pool.setAsyncEnabled(true);
        context.getLogger().info("Loading application " + 
                context.getCommandParameters().getProperty(DeployCommand.NAME) 
                + " at " + contextRoot);
        grizzlyAdapter.registerEndpoint(contextRoot, null, adapter, adapter);
        return adapter;
    }

    private RubyObjectPool setupRubyObjectPool(RailsContainer container, DeploymentContext context) {
        RubyObjectPool pool = container.getPool();
        String railsRoot = context.getSource().getURI().getSchemeSpecificPart();
        pool.setRailsRoot(railsRoot);        
        
        // Check to see if the user has set --jruby.runtime on the command prompt
        // when launching the gem.
        String jrubyRuntime = env.getStartupContext().getArguments().get("--runtimes");
        int numberOfRuntime = 1;
        if (jrubyRuntime != null) {
            try {
                numberOfRuntime = Integer.parseInt(jrubyRuntime);
            } catch (NumberFormatException ex) {
                // Ignoring the exception and taking a default value of 1
            }
        }
        pool.setNumberOfRuntime(numberOfRuntime);
        
        return pool;
    }
}
