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


package com.sun.enterprise.connectors.jms.system;

import org.glassfish.api.Startup;
import org.glassfish.api.monitoring.MonitoringItem;
import org.glassfish.internal.api.Globals;
import org.glassfish.jms.admin.monitor.config.JmsServiceMI;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.api.ConnectorConstants;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.logging.LogDomains;
import com.sun.hk2.component.Holder;
import com.sun.enterprise.config.serverbeans.JmsHost;
import com.sun.enterprise.config.serverbeans.JmsService;
import com.sun.enterprise.config.serverbeans.MonitoringService;

import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

@Service
public class JmsProviderLifecycle implements Startup, PostConstruct{
    private static final String JMS_EAGER_STARTUP = "org.glassfish.jms.EagerStartup";
    //Lifecycle properties
    public static final String EMBEDDED="EMBEDDED";
    public static final String LOCAL="LOCAL";
    public static final String REMOTE="REMOTE";
    public static final String JMS_SERVICE = "jms-service";
    static Logger logger = LogDomains.getLogger(JmsProviderLifecycle.class, LogDomains.RSR_LOGGER);

    @Inject
    Habitat habitat;

    @Inject
    MonitoringService monitoringService;

   public Lifecycle getLifecycle() {
        return Lifecycle.SERVER;
    }
    public void postConstruct()
    {
       if (eagerStartupRequired())
       {
        try {
             String module = ConnectorConstants.DEFAULT_JMS_ADAPTER;
             String loc = ConnectorsUtil.getSystemModuleLocation(module);
             ConnectorRuntime connectorRuntime = habitat.getComponent(ConnectorRuntime.class);
             connectorRuntime.createActiveResourceAdapter(loc, module, null);
               } catch (ConnectorRuntimeException e) {
                   e.printStackTrace();
                   //logger.log(Level.WARNING, "Failed to start JMS RA");
                   e.printStackTrace();
               }
       }

       createMonitoringConfig();

    }

    private boolean eagerStartupRequired(){
        String integrationMode =getJmsService().getType();

        //we don't manage lifecycle of remote brokers
        if(REMOTE.equals(integrationMode))
                return false;

         //Eager startup is currently enabled based on a system property
        String jmsEagerStartup = System.getProperty(JMS_EAGER_STARTUP);

        //if embedded broker and system property is false or not defined, don't do eager startup
        if (EMBEDDED.equals(integrationMode) && (jmsEagerStartup == null || "".equals(jmsEagerStartup) || "false".equals(jmsEagerStartup)))
            return false;

        //if local broker and system property is false, don't do eager startup
        if (LOCAL.equals(integrationMode) &&  "false".equals(jmsEagerStartup))
            return false;

        //local broker has eager startup by default
        if(LOCAL.equals(integrationMode))
            return true;

        return false;
    }

        private JmsService getJmsService(){
            return habitat.getComponent(JmsService.class);
        }
    
    /**
     * Creates jms-service config element for monitoring.
     *
     * Check if the jms-service monitoring config has been created.
     * If it has not, then add it.
     */
    private void createMonitoringConfig() {
        List<MonitoringItem> itemList = monitoringService.getMonitoringItems();
        boolean hasMonitorConfig = false;
        for (MonitoringItem mi : itemList) {
            if (mi.getName().equals(JMS_SERVICE)) {
                hasMonitorConfig = true;
            }
        }

        try {
            if (!hasMonitorConfig) {
                ConfigSupport.apply(new SingleConfigCode<MonitoringService>() {

                    public Object run(MonitoringService param) throws PropertyVetoException, TransactionFailure {

                        MonitoringItem newItem = param.createChild(JmsServiceMI.class);
                        newItem.setName(JMS_SERVICE);
                        newItem.setLevel(MonitoringItem.LEVEL_OFF);
                        param.getMonitoringItems().add(newItem);
                        return newItem;
                    }
                }, monitoringService);
            }
        } catch (TransactionFailure tfe) {
            logger.log(Level.SEVERE, "Exception adding jms-service MonitoringItem", tfe);
        }
    }
}
