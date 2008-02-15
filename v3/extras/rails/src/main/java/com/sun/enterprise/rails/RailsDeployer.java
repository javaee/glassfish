
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
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import com.sun.grizzly.jruby.RubyObjectPool;
import java.util.Iterator;
import org.jruby.RubyArray;
import org.jruby.RubyException;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jvnet.hk2.annotations.Service;

/**
 * Deployer for Rails applications
 *
 * @author Jerome Dochez
 */
@Service
public class RailsDeployer implements Deployer<RailsContainer, RailsApplication> {
    

    public boolean prepare(DeploymentContext context) {
        return true;
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
        String appContext = "/" + context.getCommandParameters().getProperty(DeployCommand.NAME);
        // TODO : we should really get the next available pool.
        RubyObjectPool pool = container.getPool();
        String contextRoot = context.getSource().getURI().getSchemeSpecificPart();
        if (contextRoot.endsWith("/")) {
            // rails adapter does not like trailing /
            contextRoot = contextRoot.substring(0, contextRoot.length()-1);
        }
        pool.setRailsRoot(contextRoot);
        
        RailsApplication adapter = new RailsApplication(pool);    
        adapter.setContextRoot(appContext);
        try {   
            pool.start();
        } catch (RaiseException e) {

            e.printStackTrace();

            System.out.println(e.getMessage());
			// try to put some helpful information in the logs
			RubyException re = e.getException();
			String rubyMessage = (String)JavaEmbedUtils.rubyToJava(pool.bollowRuntime(), re.message, String.class);
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
}
