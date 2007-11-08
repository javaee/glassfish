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
package com.sun.enterprise.admin.wsmgmt.lifecycle.reconfig;

import com.sun.enterprise.admin.event.wsmgmt.WebServiceEndpointEvent;
import com.sun.enterprise.admin.event.wsmgmt.WebServiceEndpointEventListener;
import com.sun.enterprise.admin.wsmgmt.msg.MessageTraceMgr;
import com.sun.enterprise.admin.wsmgmt.WebServiceMgrBackEnd ;
import com.sun.enterprise.webservice.ServiceEngineRtObjectFactory;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.EjbModule;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.WebServiceEndpoint;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.admin.event.AdminEventListenerException;

import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;
import
com.sun.enterprise.admin.wsmgmt.lifecycle.AppServWSMonitorLifeCycleProvider;

/**
 * Listener impl to handle web-service-endpoint element events.
 */
public class WebServiceEndpointEventListenerImpl implements 
        WebServiceEndpointEventListener {

    /**
     * Handles web-service-endpoint element removal.
     *
     * @param event    Event to be processed.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void handleDelete(WebServiceEndpointEvent event)
             throws AdminEventListenerException {

        try { 
            ConfigBean bean = getWSEPBean(event, true);
            if (bean instanceof WebServiceEndpoint) {
                WebServiceEndpoint wsep = (WebServiceEndpoint) bean;
                String epName = wsep.getName();
                String appId = getApplicationId(wsep);

                // disables message trace for this endpoint
                MessageTraceMgr traceMgr = MessageTraceMgr.getInstance();
                traceMgr.disable(appId, epName);

                AppServWSMonitorLifeCycleProvider aplifeProv = 
                    new AppServWSMonitorLifeCycleProvider(); 
                aplifeProv.reconfigureMonitoring(wsep, appId, 
                MonitoringLevel.instance(wsep.getMonitoring()), 
                MonitoringLevel.instance("OFF"));

            } 
        } catch (Exception e) {
            throw new AdminEventListenerException(e);
        }
    }

    /**
     * Handles web-service-endpoint element modification 
     * (attributes/properties values changed).
     *
     * @param event    Event to be processed.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void handleUpdate(WebServiceEndpointEvent event)
             throws AdminEventListenerException {

        try {
            // set message in history size
            ConfigBean bean = getWSEPBean(event, false);
            if (bean instanceof WebServiceEndpoint) {
                WebServiceEndpoint wsep = (WebServiceEndpoint) bean;
                WebServiceEndpoint oWsep = 
                    (WebServiceEndpoint) getWSEPBean(event, true);

                // monitoring level
                String newMonitoring = wsep.getMonitoring();
                String oldMonitoring = oWsep.getMonitoring();

                // history size
                int historySize = 25;

                // end point name
                String epName = wsep.getName();

                String hs = wsep.getMaxHistorySize();
                if (hs != null) {
                    historySize = Integer.parseInt(hs);
                }

                // application id
                String appId = getApplicationId(wsep);

                // message trace manager
                MessageTraceMgr traceMgr = MessageTraceMgr.getInstance();

                // monitoring level changed
                if ( (newMonitoring != null) && (oldMonitoring != null) 
                        && (!newMonitoring.equals(oldMonitoring)) ) {

                    // monitoring level is changed to HIGH
                    if ("HIGH".equalsIgnoreCase(newMonitoring)) {
                        traceMgr.enable(appId, epName, historySize);

                    // monitoirng level is changed from HIGH to LOW or OFF
                    } else if ("HIGH".equalsIgnoreCase(oldMonitoring)) {
                        traceMgr.disable(appId, epName);
                    }
                } else {
                    // old history size
                    String oldHs = oWsep.getMaxHistorySize();
                    int oldHistorySize = 0;
                    if (oldHs != null) {
                        oldHistorySize = Integer.parseInt(oldHs);
                    }

                    // history size has changed and monitoring level is HIGH
                    if ((historySize != oldHistorySize) 
                            && ("HIGH".equalsIgnoreCase(newMonitoring))) {

                        traceMgr.setMessageHistorySize(appId, epName, 
                                                        historySize);
                    }
                }
                    
                AppServWSMonitorLifeCycleProvider aplifeProv = 
                    new AppServWSMonitorLifeCycleProvider(); 
                aplifeProv.reconfigureMonitoring(wsep, appId, 
                MonitoringLevel.instance(oldMonitoring), 
                MonitoringLevel.instance(newMonitoring));
                
                // Check for jbiEnabled Flag
                boolean jbiEnabledFlag = wsep.isJbiEnabled();
                boolean oldJbiEnabledSetting =oWsep.isJbiEnabled(); 
                if(jbiEnabledFlag  != oldJbiEnabledSetting) {
                    String endpointURI = WebServiceMgrBackEnd.getManager().
                              getEndpointURI(getApplicationId(wsep) + "#" + epName);
                    if(endpointURI != null && (ServiceEngineRtObjectFactory.getInstance().getFacade() != null)) {
                        ServiceEngineRtObjectFactory.getInstance().
                              getFacade().handleWebServiceEndpointEvent(
                              endpointURI,jbiEnabledFlag);
                    }
                } 
            }
        } catch (Exception e) {
            throw new AdminEventListenerException(e);
        }
    }

    /**
     * Handles element additions.
     *
     * @param event    Event to be processed.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void handleCreate(WebServiceEndpointEvent event)
             throws AdminEventListenerException {

        try {
            ConfigBean bean = getWSEPBean(event, false);
            if (bean instanceof WebServiceEndpoint) {
                WebServiceEndpoint wsep = (WebServiceEndpoint) bean;
                int historySize = 25;
                String epName = wsep.getName();
                String appId = getApplicationId(wsep);

                String hs = wsep.getMaxHistorySize();
                if (hs != null) {
                    historySize = Integer.parseInt(hs);
                }

                // monitoring level
                String newMonitoring = wsep.getMonitoring();

                if ("HIGH".equalsIgnoreCase(newMonitoring)) {
                    // enables message trace for this endpoint
                    MessageTraceMgr traceMgr = MessageTraceMgr.getInstance();
                    traceMgr.enable(appId, epName, historySize);
                }

                AppServWSMonitorLifeCycleProvider aplifeProv = 
                    new AppServWSMonitorLifeCycleProvider(); 
                aplifeProv.reconfigureMonitoring(wsep, appId, 
                MonitoringLevel.instance("OFF"),
                MonitoringLevel.instance(wsep.getMonitoring())); 
            } 
        } catch (Exception e) {
            throw new AdminEventListenerException(e);
        }
    }

    /**
     * Returns the web-service-endpoint config bean for this event.
     *
     * @param  event web service endpoint event
     * @param  old true when old config context is used
     *
     * @return  web service endpoing config bean
     */
    private ConfigBean getWSEPBean(WebServiceEndpointEvent event, boolean old) 
            throws ConfigException {

        if (event == null) {
            throw new IllegalArgumentException();
        }

        ConfigBean bean = null;
        ConfigContext ctx = null;
        String xpath = event.getElementXPath();
        if (old) {
            ctx = event.getOldConfigContext();
        } else {
            ctx = event.getConfigContext();
        }
        if (ctx != null) {
            bean = ctx.exactLookup(xpath);
        }

        return bean;
    }

    /**
     * Returns application registration name for this endpoing.
     * 
     * @param  bean  web service endpint config bean
     *
     * @return  application registration name
     */
    private String getApplicationId(WebServiceEndpoint bean) {
        String name = null;

        if (bean != null) {
            ConfigBean parent = (ConfigBean) bean.parent();
            if (parent instanceof J2eeApplication) {
                J2eeApplication app = (J2eeApplication) parent;
                name = app.getName();
            } else if (parent instanceof WebModule) {
                WebModule wm = (WebModule) parent;
                name = wm.getName();
            } else if (parent instanceof EjbModule) {
                EjbModule em = (EjbModule) parent;
                name = em.getName();
            }
        }
        return name;
    }
}
