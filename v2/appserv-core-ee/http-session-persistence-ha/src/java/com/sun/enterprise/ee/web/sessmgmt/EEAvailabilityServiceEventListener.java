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

/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

/*
 * EEAvailabilityServiceEventListener.java
 *
 * Created on July 11, 2005, 5:25 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.ArrayList;
import com.sun.enterprise.config.ConfigUpdate;

import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.event.availability.AvailabilityServiceEventListener;
import com.sun.enterprise.admin.event.availability.AvailabilityServiceEvent;

import com.sun.enterprise.web.ServerConfigLookup;


/**
 *
 * @author lwhite
 */
public class EEAvailabilityServiceEventListener implements AvailabilityServiceEventListener {
    
    /** Creates a new instance of EEAvailabilityServiceEventListener */
    public EEAvailabilityServiceEventListener() {
    }
    
    /** Creates a new instance of EEAvailabilityServiceEventListener */
    public EEAvailabilityServiceEventListener(EEHADBHealthChecker healthChecker) {
        _healthChecker = healthChecker;
    }    
    
    
    
    /**
     * Handles availability-service element attribute removal.
     *
     * @param event    Event to be processed.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void handleDelete(AvailabilityServiceEvent event)
             throws AdminEventListenerException {
        //no-op
        System.out.println("handleDelete: event = " + event);
    }
    
    /**
     * Handles availability-service element modification 
     * (attributes/properties values changed).
     *
     * @param event    Event to be processed.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void handleUpdate(AvailabilityServiceEvent event)
             throws AdminEventListenerException { 
        
        System.out.println("handleUpdate: event = " + event);
        //printConfigChangeList(event);
        ArrayList changedAttributes = this.prepareConfigChangeList(event);
        //testPrintChangedAttributes(changedAttributes);
        _healthChecker.resetConfigAttributes(changedAttributes);
    }
    
    //remove after testing
    private void testPrintChangedAttributes(ArrayList changedAttributes) {
        for(int i=0; i<changedAttributes.size(); i++) {
            System.out.println( (ConfigChangeElement)changedAttributes.get(i) );
        }
    }
    
    //remove after testing
    private void printConfigChangeList(AvailabilityServiceEvent event) {
        
        System.out.println("oldCtx: " + event.getOldConfigContext());
        System.out.println("newCtx: " + event.getConfigContext());
        ArrayList configChangeList = event.getConfigChangeList();
        for(int i=0; i<configChangeList.size(); i++) {
            ConfigUpdate nextConfigUpdate = (ConfigUpdate)configChangeList.get(i);
            String nextAttrName = nextConfigUpdate.getName();
            System.out.println("name[" + i + "]=" + nextAttrName);
            System.out.println(nextAttrName + ":oldval:" + nextConfigUpdate.getOldValue(nextAttrName));
            System.out.println(nextAttrName + ":newval:" + nextConfigUpdate.getNewValue(nextAttrName));
        }
        ServerConfigLookup lookup = new ServerConfigLookup();
        System.out.println("old value-health-check-enabled: " + lookup.getHadbHealthCheckFromConfig());               
        ServerConfigLookup lookup2 = new ServerConfigLookup(event.getConfigContext());
        System.out.println("new value-health-check-enabled: " + lookup2.getHadbHealthCheckFromConfigDynamic());
    }
    
    private ArrayList prepareConfigChangeList(AvailabilityServiceEvent event) {
        
        System.out.println("newCtx: " + event.getConfigContext());
        ArrayList list = new ArrayList();               
        ServerConfigLookup newLookup = new ServerConfigLookup(event.getConfigContext());
        ServerConfigLookup oldLookup = new ServerConfigLookup();
        
        //ha-store-healthcheck-enabled
        boolean oldHealthCheckEnabled = oldLookup.getHadbHealthCheckFromConfig();
        boolean newHealthCheckEnabled = newLookup.getHadbHealthCheckFromConfigDynamic();
        if(oldHealthCheckEnabled != newHealthCheckEnabled) {
            ConfigChangeElement element = 
                new ConfigChangeElement(EEHADBHealthChecker.HEALTH_CHECK_ENABLED, new Boolean(newHealthCheckEnabled));
            list.add(element);
        }
        
        //ha-store-healthcheck-interval-in-seconds
        int oldHealthCheckInterval = 
            oldLookup.getHaStoreHealthcheckIntervalInSecondsFromConfig();
        int newHealthCheckInterval = 
            newLookup.getHaStoreHealthcheckIntervalInSecondsFromConfigDynamic();
        if(oldHealthCheckInterval != newHealthCheckInterval) {
            ConfigChangeElement element = 
                new ConfigChangeElement(EEHADBHealthChecker.HEALTH_CHECK_INTERVAL, new Integer(newHealthCheckInterval));
            list.add(element);
        }
        
        boolean useNew = false;
        boolean recomputeConnectionURL = false;
        
        //ha-agent-hosts
        String oldAgentHosts = 
            oldLookup.getHadbAgentHostsFromConfig();
        String newAgentHosts = 
            newLookup.getHadbAgentHostsFromConfigDynamic();
        useNew = false;
        if(oldAgentHosts == null) {
            if(newAgentHosts != null) {
                useNew = true;
            }
        } else {
            if(!oldAgentHosts.equals(newAgentHosts)) {
                useNew = true;
            } 
        }
        if(useNew) {
            recomputeConnectionURL = true;
            ConfigChangeElement element = 
                new ConfigChangeElement(EEHADBHealthChecker.HA_AGENT_HOSTS, newAgentHosts);
            list.add(element);
        }        
        
        //ha-agent-port
        String oldAgentPort = 
            oldLookup.getHadbAgentPortFromConfig();
        String newAgentPort = 
            newLookup.getHadbAgentPortFromConfigDynamic();
        useNew = false;
        if(oldAgentPort == null) {
            if(newAgentPort != null) {
                useNew = true;
            }
        } else {
            if(!oldAgentPort.equals(newAgentPort)) {
                useNew = true;
            } 
        }
        if(useNew) {
            recomputeConnectionURL = true;
            ConfigChangeElement element = 
                new ConfigChangeElement(EEHADBHealthChecker.HA_AGENT_PORT, newAgentPort);
            list.add(element);
        }
        
        if(recomputeConnectionURL) {
            String newAgentConnUrl = 
                newLookup.getHadbAgentConnectionURLFromConfigDynamic();           
            ConfigChangeElement element = 
                new ConfigChangeElement(EEHADBHealthChecker.HA_AGENT_CONNECTION_URL, newAgentConnUrl);
            list.add(element);            
        }
        
        //ha-store-name
        String oldStoreName = 
            oldLookup.getHadbDatabaseNameFromConfig();
        String newStoreName = 
            newLookup.getHadbDatabaseNameFromConfigDynamic();
        useNew = false;
        if(oldStoreName == null) {
            if(newStoreName != null) {
                useNew = true;
            }
        } else {
            if(!oldStoreName.equals(newStoreName)) {
                useNew = true;
            } 
        }
        if(useNew) {
            ConfigChangeElement element = 
                new ConfigChangeElement(EEHADBHealthChecker.HA_STORE_NAME, newStoreName);
            list.add(element);
        }
                
        return list;
    }    
    
    /**
     * Handles availability-service element additions.
     *
     * @param event    Event to be processed.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void handleCreate(AvailabilityServiceEvent event)
             throws AdminEventListenerException { 
        //no-op
        System.out.println("handleCreate: event = " + event);
    }
    
    EEHADBHealthChecker _healthChecker = null;
    
}
