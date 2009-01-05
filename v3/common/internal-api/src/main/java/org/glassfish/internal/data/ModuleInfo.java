/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.internal.data;

import org.glassfish.internal.data.ContainerInfo;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.ActionReport;
import org.glassfish.api.container.Adapter;
import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.api.container.RequestDispatcher;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Information about a module in a container. There is a one to one mapping
 * from module to containers. Containers running a module are accessible
 * through the ApplicationContainer interface.
 *
 * @author Jerome Dochez
 */
public class ModuleInfo {

    final private ContainerInfo ctrInfo;
    final RequestDispatcher requestDispatcher;

    private ApplicationContainer appCtr;

    public ModuleInfo(ContainerInfo container, RequestDispatcher requestDispatcher, ApplicationContainer appCtr) {
        this.ctrInfo = container;
        this.requestDispatcher = requestDispatcher;
        this.appCtr = appCtr;
    }

    /**
     * Returns the container associated with this application
     *
     * @return the container for this application
     */
    public ContainerInfo getContainerInfo() {
        return ctrInfo;
    }

    /**
     * Set the contaier associated with this application
     * @param appCtr the container for this application
     */
    public void setApplicationContainer(ApplicationContainer appCtr) {
        this.appCtr = appCtr;
    }

    /**
     * Returns the contaier associated with this application
     * @return the container for this application
     */
    public ApplicationContainer getApplicationContainer() {
        return appCtr;
    }

    public boolean start(ApplicationContext context, ProgressTracker tracker)
        throws Exception {

        if (!appCtr.start(context)) {
            return false;
        }

        tracker.add("started", ModuleInfo.class, this);

        // add the endpoint
        try {
            Adapter appAdapter = Adapter.class.cast(appCtr);
            requestDispatcher.registerEndpoint(appAdapter.getContextRoot(), appAdapter,appCtr);
        } catch (ClassCastException e) {
            // ignore the application may not publish endpoints.
        }
        return true;
    }

    /**
     * unloads the module from its container.
     *
     * @param info
     * @param context
     * @param report
     * @return
     */
    public boolean unload(ApplicationInfo info, DeploymentContext context, ActionReport report) {

        // then remove the application from the container
        Deployer deployer = ctrInfo.getDeployer();
        try {
            deployer.unload(appCtr, context);
        } catch(Exception e) {
            report.failure(context.getLogger(), "Exception while shutting down application container", e);
            return false;
        }
        if (info!=null) {
            ctrInfo.remove(info);
        }
        return true;
    }

    /**
     * Stops a module, meaning that components implemented by this module should not be accessed
     * by external modules
     *
     * @param context
     * @param logger
     * @return
     */
    public boolean stop(ApplicationContext context,  Logger logger) {
        // remove any endpoints if exists.
        //@TODO change EndportRegistrationException processing if required
        try {
            final Adapter appAdapter = Adapter.class.cast(appCtr);
            requestDispatcher.unregisterEndpoint(appAdapter.getContextRoot(), appCtr);
        } catch (EndpointRegistrationException e) {
            logger.log(Level.WARNING, "Exception during unloading module '" +
                    this + "'", e);
        } catch(ClassCastException e) {
            // do nothing the application did not have an adapter
        }

       return appCtr.stop(context);
    }    

}
