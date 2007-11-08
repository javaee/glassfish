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

package com.sun.enterprise.server.ondemand;

import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.enterprise.server.ondemand.entry.EntryContext;
import com.sun.enterprise.server.ondemand.entry.EntryPoint;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.config.*;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.DeferredResourceConfig;
import com.sun.enterprise.deployment.*;


/**
 * Represents the group services needed by resources. The main components
 * of this servicegroup JMS provider or MQ. Almost all the resources
 * in Sun application server are lazily loaded anyway. So this service
 * group just concentrates on SJSMQ.
 *
 * @author Binod PG
 * @see ServiceGroup
 * @see ServiceGroupBuilder
 */
public class ResourcesServiceGroup extends ServiceGroup {

    /**
     * Triggers the start of the servicegroup. The entry context
     * that caused this startup is used by the servicegroup to obtain
     * any startup information it require.
     * 
     * @param context EntryContext object.
     * @see EntryContext.
     */
    public void start(EntryContext context) 
    throws ServiceGroupException {
        try {
            //loadSystemApps();

            startLifecycleServices(context.getServerContext());
            //(ConnectorRuntime.DEFAULT_JMS_ADAPTER);
        } catch (Exception e) {
            throw new ServiceGroupException(e);
        }
    }

    /**
     * Analyse the entrycontext and specifies whether this servicegroup
     * can be started or not.
     *
     * @return boolean If true is returned, this servicegroup can be started
     * If false is returned, the entrycontext  is not recognized by the 
     * servicegroup.
     */
    public boolean analyseEntryContext( EntryContext context ) {
        
        if (_logger.isLoggable(Level.FINER)) {
            _logger.log(Level.FINER, 
            "Analysing the context in Resources ServiceGroup :" + context);
        }

        if (context.get() == null) {
            return false;
        }

        if ( context.getEntryPointType() == EntryPoint.JNDI ) {
            // TODO. Move connectors lazy loading from naming to here.
            return false;
        }

        boolean result = false;
        try {
            ConfigContext ctxt = context.getServerContext().getConfigContext();
            Config conf = ServerBeansFactory.getConfigBean( ctxt );
            JmsService jmsService_ = conf.getJmsService();
            String defaultJmsHost = jmsService_.getDefaultJmsHost();
            JmsHost jmsHost_ = null;
            boolean embedded = true;

            if (defaultJmsHost==null || defaultJmsHost.equals("")) {
               jmsHost_ = ServerBeansFactory.getJmsHostBean(ctxt);
            } else {
               jmsHost_ = jmsService_.getJmsHostByName(defaultJmsHost);
            }

            if (jmsHost_ == null || jmsService_.getType() == null ||
                ! jmsService_.getType().equalsIgnoreCase("embedded")) {
                embedded = false;
            }

            if ( context.getEntryPointType() == EntryPoint.STARTUP ) {
                boolean onDemandStartup = ((Boolean) context.get()).booleanValue();
                if (onDemandStartup == true && embedded == false) {
                    result = true;
                }
            } else if (embedded == false) {
                return false;
            }

            if (context.getEntryPointType() == EntryPoint.APPLOADER ) {
                String mqRA = ConnectorRuntime.DEFAULT_JMS_ADAPTER;
                Descriptor desc = (Descriptor) context.get();
                if (desc instanceof Application) {
                    Application application = (Application) desc;
                    for (ConnectorDescriptor cd : (java.util.Set<ConnectorDescriptor>) 
                         application.getRarDescriptors()) {
                         Application app = cd.getApplication();
                         if (app !=null && mqRA.equals(app.getRegistrationName())) {
                             result = true;
                         }
                    }
                } else if (desc instanceof ConnectorDescriptor ) { 
                    ConnectorDescriptor cd = (ConnectorDescriptor) desc;
                    Application app = cd.getApplication();
                    if (app != null && mqRA.equals(app.getRegistrationName())) {
                        result = true;
                    }
                }
                
            }

            if ( context.getEntryPointType() == EntryPoint.PORT ) {
                String portStr = jmsHost_.getPort();
                if (Integer.parseInt(portStr.trim()) == 
                   ((Integer) context.get()).intValue()) {
                   result = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    /**
     * Loads all the system apps belongs to this servicegroup.
     * @see OnDemandServices
     * @see SystemAppLoader
     */
    private void loadSystemApps() {
        SystemAppLoader loader = OnDemandServer.getSystemAppLoader();
        loader.loadSystemApps(loader.getResourcesServiceGroupSystemApps());
    }


    /**
     * Start lifecycles belonging to this service group.
     * @see OnDemandServices
     */
    private void startLifecycleServices(ServerContext context) {
        String[][] services = OnDemandServices.getResourcesServiceGroupServices();
        super.startLifecycleServices(services, context);
    }

    /**
     * Stop the servicegroup. It stops all the lifecycle modules belongs to this
     * servicegroup.
     */
    public void stop(EntryContext context) throws ServiceGroupException {
        super.stopLifecycleServices();
    }

    /**
     * Abort the servicegroup. This is not called from anywhere as of now.
     */
    public void abort(EntryContext context) {
        super.stopLifecycleServices();
    }

}
