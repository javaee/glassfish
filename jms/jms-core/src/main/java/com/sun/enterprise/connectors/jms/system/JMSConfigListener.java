/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import java.util.*;
import java.util.logging.*;
import java.beans.PropertyChangeEvent;

import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.SystemPropertyConstants;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.UnprocessedChangeEvent;
import org.jvnet.hk2.config.UnprocessedChangeEvents;
import org.jvnet.hk2.config.types.Property;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.ServerContext;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.connectors.jms.util.JmsRaUtil;

@Service
public class JMSConfigListener implements ConfigListener{
    // Injecting @Configured type triggers the corresponding change
    // events to be sent to this instance
    @Inject 
	private JmsService jmsservice;
   
    //@Inject 
//	private Cluster cluster;
    private ActiveJmsResourceAdapter aresourceAdapter;

   private static final Logger _logger = LogDomains.getLogger(
            JMSConfigListener.class, LogDomains.JMS_LOGGER);

   // String Manager for Localization
   private static StringManager sm
        = StringManager.getManager(JMSConfigListener.class);

   public void setActiveResourceAdapter(ActiveJmsResourceAdapter aresourceAdapter) {
           this.aresourceAdapter = aresourceAdapter;
   }


    /** Implementation of org.jvnet.hk2.config.ConfigListener */
   public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {

     // Events that we can't process now because they require server restart.
     //List<UnprocessedChangeEvent> unprocessedEvents = new ArrayList<UnprocessedChangeEvent>();
     Domain domain = Globals.get(Domain.class);
	_logger.log(Level.FINE, "In JMSConfigListener - recived config event");
     for (PropertyChangeEvent event : events) {
        String eventName = event.getPropertyName();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();

	_logger.log(Level.FINE, "In JMSConfigListener " + eventName + oldValue + newValue);
        boolean accepted = true;
        if (oldValue != null && oldValue.equals(newValue)) {
            _logger.log(Level.FINE, "Event " + eventName
                    + " did not change existing value of " + oldValue);
            continue;
        }
        if(event.getSource() instanceof JmsService ) {
         if (eventName.equals(ServerTags.MASTER_BROKER)) {
                 String oldMB = oldValue.toString();
                 String newMB = newValue.toString();

            _logger.log(Level.FINE, "Got JmsService Master Broker change event "
                + event.getSource() + " "
                + eventName + " " + oldMB + " " + newMB);
             Server newMBServer = domain.getServerNamed(newMB);
             if(newMBServer != null)
             {
                 Node node = domain.getNodeNamed(newMBServer.getNode());
                 String newMasterBrokerPort = JmsRaUtil.getJMSPropertyValue(newMBServer);
                 if(newMasterBrokerPort == null) newMasterBrokerPort = getDefaultJmsHost(jmsservice).getPort();
                 String newMasterBrokerHost = node.getNodeHost();
                 aresourceAdapter.setMasterBroker(newMasterBrokerHost + ":" + newMasterBrokerPort);
             }
         }
        }
       if (event.getSource() instanceof Cluster) {
	_logger.log(Level.FINE, "In JMSConfigListener - recieved cluster event " + event.getSource());
           //String serverName = System.getProperty(SystemPropertyConstants.SERVER_NAME);
           ServerContext serverContext = Globals.get(ServerContext.class);
           Server server = domain.getServerNamed(serverContext.getInstanceName());
           if (server != null){
               Cluster changedCluster = (Cluster) event.getSource();
               Cluster thisCluster = server.getCluster();
               if (! changedCluster.getName().equals(thisCluster.getName())){
                _logger.log(Level.FINE, "Got Cluster change event but ignoring the change since it does not pertain to this cluster"
                + event.getSource() + " "
                + eventName + " Changed Cluster: " + changedCluster.getName() + " this Cluster: " + thisCluster.getName());
                   continue;
               }
           }
            if (eventName.equals(ServerTags.SERVER_REF)) {
                String oldServerRef = oldValue.toString();
                String newServerRef = newValue.toString();
                _logger.log(Level.FINE, "Got Cluster change event for server_ref"
                + event.getSource() + " "
                + eventName + " " + oldServerRef + " " + newServerRef);
                //aresourceAdapter.
            } // else skip
        }
     }
        return null;
    }
     private JmsHost getDefaultJmsHost(JmsService jmsService){

            JmsHost jmsHost = null;
                String defaultJmsHostName = jmsService.getDefaultJmsHost();
                List jmsHostsList = jmsService.getJmsHost();

                for (int i=0; i < jmsHostsList.size(); i ++)
                {
                   JmsHost tmpJmsHost = (JmsHost) jmsHostsList.get(i);
                   if (tmpJmsHost != null && tmpJmsHost.getName().equals(defaultJmsHostName))
                         jmsHost = tmpJmsHost;
                }
            return jmsHost;
          }


}
