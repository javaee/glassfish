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

package com.sun.enterprise.web.reconfig;

import java.util.ArrayList;
import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.event.http.HSAccessLogEvent;
import com.sun.enterprise.admin.event.http.HSAccessLogEventListener;
import com.sun.enterprise.admin.event.http.HSVirtualServerEvent;
import com.sun.enterprise.admin.event.http.HSVirtualServerEventListener;
import com.sun.enterprise.admin.event.http.HSServiceEvent;
import com.sun.enterprise.admin.event.http.HSServiceEventListener;
import com.sun.enterprise.admin.event.http.HSHttpFileCacheEvent;
import com.sun.enterprise.admin.event.http.HSHttpFileCacheEventListener;
import com.sun.enterprise.admin.event.http.HSHttpListenerEvent;
import com.sun.enterprise.admin.event.http.HSHttpListenerEventListener;
import com.sun.enterprise.admin.event.http.HSHttpProtocolEvent;
import com.sun.enterprise.admin.event.http.HSHttpProtocolEventListener;
import com.sun.enterprise.admin.event.http.HSConnectionPoolEvent;
import com.sun.enterprise.admin.event.http.HSConnectionPoolEventListener;
import com.sun.enterprise.admin.event.http.HSKeepAliveEvent;
import com.sun.enterprise.admin.event.http.HSKeepAliveEventListener;
import com.sun.enterprise.admin.event.http.HSRequestProcessingEvent;
import com.sun.enterprise.admin.event.http.HSRequestProcessingEventListener;
import com.sun.enterprise.config.ConfigAdd;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigChange;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.web.PEWebContainer;

/**
 * Class responsible for handling dynamic reconfiguration events for
 * various elements in domain.xml.
 *
 * @author Jean-Francois Arcand
 * @author Jan Luehe
 */ 
public class ReconfigListener
    implements HSAccessLogEventListener,
               HSHttpListenerEventListener,
               HSServiceEventListener,
               HSVirtualServerEventListener,
               HSHttpProtocolEventListener,
               HSHttpFileCacheEventListener,
               HSConnectionPoolEventListener,
               HSKeepAliveEventListener,
               HSRequestProcessingEventListener {
   
    private PEWebContainer webContainer;

    public ReconfigListener(PEWebContainer webContainer) {
        this.webContainer = webContainer;
    }


    /*
     * Dynamic reconfig events pertaining to <access-log>
     */
    
    public void handleDelete(HSAccessLogEvent event)
            throws AdminEventListenerException {
        // Ignore
    }
    
    public void handleUpdate(HSAccessLogEvent event)
            throws AdminEventListenerException {

        if (webContainer == null || event == null) {
            return;
        }
           
        try {
                   
            ConfigContext configContext = event.getConfigContext();
            Config config = ServerBeansFactory.getConfigBean(configContext);
            if (config == null) {
                return;
            }
            
            webContainer.updateAccessLog(config.getHttpService());

        } catch (Exception e) {
           throw new AdminEventListenerException(e);
        } 
    }

    public void handleCreate(HSAccessLogEvent event)
            throws AdminEventListenerException {
        // Ignore
    }


    /*
     * Dynamic reconfig events pertaining to <http-listener>
     */

    /**
     * Create a new http-listener.
     */
    public void handleCreate(HSHttpListenerEvent event)
            throws AdminEventListenerException {
        
        if (webContainer == null || event == null) {
            return;       
        }

        try {             
            ConfigContext configContext = event.getConfigContext();
            Config config = ServerBeansFactory.getConfigBean(configContext);
            
            if ( config == null) return;
            
            ConfigAdd configAdd = null;
            ArrayList configChangeList = event.getConfigChangeList();
            HttpListener httpBean = null;
            String xpath = null;
            Object object;
            Object configObject;
            
            for (int i=0; i < configChangeList.size(); i++){
                configObject = configChangeList.get(i);

                if ( configObject instanceof ConfigAdd) {
                    configAdd = (ConfigAdd)configObject;
                    xpath = configAdd.getXPath();
                    if( xpath != null){
                        object = configContext.exactLookup(xpath);
                        if ( object instanceof HttpListener){
                            httpBean = (HttpListener)object;
                            webContainer.createConnector(httpBean, 
                                                         config.getHttpService());
                        }
                    }
                }
            }
        } catch (Exception ex) {
           throw new AdminEventListenerException(ex);
        }  
    }
    
    /**
     * Update an existing http-listener.
     */
    public void handleUpdate(HSHttpListenerEvent event)
            throws AdminEventListenerException {   
        
        if (webContainer == null || event == null) {
            return;
        }
                                     
        try {                        
            ConfigContext configContext = event.getConfigContext();
            Config config = ServerBeansFactory.getConfigBean(configContext);
            
            if ( config == null) return;
            
            ConfigChange configChange = null;
            ArrayList<ConfigChange> configChangeList =
                event.getConfigChangeList();
            HttpListener httpBean = null;
            String xpath = null;
            String parentXpath = null;
            Object object;
            ElementProperty elementProperty = null;

            for (int i=0; i < configChangeList.size(); i++){
                configChange = configChangeList.get(i);
                xpath = configChange.getXPath();
                if (xpath != null) {
                    object = configContext.exactLookup(xpath);
                    if (object != null) {
                        if (object instanceof HttpListener) {
                            
                            if ( ((HttpListener)object).getDefaultVirtualServer()
                                    .equals(com.sun.enterprise.web.VirtualServer.ADMIN_VS)){
                                
                                if (configChangeList.size() == 1){
                                    throw new AdminEventListenerException("Restart required");
                                } else {
                                    continue;
                                }                                
                            }
                            
                            webContainer.updateConnector(
                                (HttpListener)object, 
                                config.getHttpService());
                        } else if (object instanceof ElementProperty) {
                            // Property has been added or updated
                            parentXpath = configChange.getParentXPath();
                            if (parentXpath == null) {
                                parentXpath =
                                    xpath.substring(0, xpath.lastIndexOf("/"));
                            }
                            httpBean = (HttpListener)
                                configContext.exactLookup(parentXpath);
                            webContainer.updateConnector(
                                httpBean, 
                                config.getHttpService());
                        }
                    } else {
                        // Property has been deleted
                        parentXpath =
                            xpath.substring(0, xpath.lastIndexOf("/"));
                        httpBean = (HttpListener)
                            configContext.exactLookup(parentXpath);
                        webContainer.updateConnector(
                            httpBean, 
                            config.getHttpService());
                    }
                }
            }
        } catch (Exception ex) {
           throw new AdminEventListenerException(ex);
        }
    }
    
    /**
     * Delete an existing http-listener. 
     *
     * Implementation note: we cannot use the same approach used when
     * handling create/update event since the element is deleted before this
     * method is invoked (strange behaviour). Instead we need to find by 
     * ourself which http-listener has been deleted.
     */
    public void handleDelete(HSHttpListenerEvent event) 
            throws AdminEventListenerException {
         
        if (webContainer == null || event == null) {
            return;       
        }

        try {
            ConfigContext configContext = event.getConfigContext();
            Config config = ServerBeansFactory.getConfigBean(configContext);
                
            if ( config == null) return;
                
            webContainer.deleteConnector(config.getHttpService());

        }  catch( Exception ex){
           throw new AdminEventListenerException(ex);
        }      
    }     


    /*
     * Dynamic reconfig events pertaining to <http-service>
     */

    public void handleDelete(HSServiceEvent event)
            throws AdminEventListenerException {
        // Ignore
    }

    public void handleUpdate(HSServiceEvent event)
            throws AdminEventListenerException {

        if (webContainer == null || event == null) {
            return;
        }

        try {
                   
            ConfigContext configContext = event.getConfigContext();
            Config config = ServerBeansFactory.getConfigBean(configContext);
            if (config == null) {
                return;
            }
            
            webContainer.updateHttpService(config.getHttpService());

        } catch (Exception e) {
           throw new AdminEventListenerException(e);
        } 
    }

    public void handleCreate(HSServiceEvent event)
            throws AdminEventListenerException {
        // Ignore
    }


    /*
     * Dynamic reconfig events pertaining to <virtual-server>
     */

    /**
     * Create a new virtual-server.
     */
    public void handleCreate(HSVirtualServerEvent event)
            throws AdminEventListenerException {
        
        if (webContainer == null || event == null) {
            return;
        }
           
        try {                                                          
            ConfigContext configContext = event.getConfigContext();
            Config config = ServerBeansFactory.getConfigBean(configContext);
                                    
            if ( config == null) return;
            
            ConfigAdd configAdd = null;
            ArrayList configChangeList = event.getConfigChangeList();
            VirtualServer vsBean = null;
            String xpath = null;
            Server serverBean = ServerBeansFactory.getServerBean(configContext);
            ElementProperty elementProperty = null;
            Object object;
            Object configObject;
            
            for (int i=0; i < configChangeList.size(); i++){
                configObject = configChangeList.get(i);

                if ( configObject instanceof ConfigAdd) {
                    configAdd = (ConfigAdd)configObject;
                    xpath = configAdd.getXPath();
                    if( xpath != null){
                        object = configContext.exactLookup(xpath);

                        if ( object instanceof VirtualServer){
                            vsBean = (VirtualServer)object;
                            webContainer.createHost(vsBean,configContext,true);
                        } else if (object instanceof ElementProperty) {
                            xpath = xpath.substring(0,xpath.lastIndexOf("/"));
                            vsBean = (VirtualServer)configContext.exactLookup(xpath);                       
                            elementProperty = (ElementProperty)object;
                            webContainer.updateHostProperties(
                                       vsBean, 
                                       elementProperty.getName(),
                                       elementProperty.getValue(),
                                       config.getHttpService(),
                                       config.getSecurityService());
                        }
                    }
                }
            }
        } catch (Exception ex) {
           throw new AdminEventListenerException(ex);
        } 
    }
    
    /**
     * Update an existing virtual-server.
     */
    public void handleUpdate(HSVirtualServerEvent event)
            throws AdminEventListenerException {  
        
        if (webContainer == null || event == null) {
            return;
        }
           
        try {                                            
            ConfigContext configContext = event.getConfigContext();
            Config config = ServerBeansFactory.getConfigBean(configContext);
            
            if ( config == null) return;
            
            ConfigChange configChange = null;
            ArrayList<ConfigChange> configChangeList = 
                event.getConfigChangeList();
            VirtualServer vsBean = null;
            String xpath = null;
            String parentXpath = null;
            Server serverBean = ServerBeansFactory.getServerBean(configContext);
            ElementProperty elementProperty = null;
            Object object;           

            for (int i=0; i < configChangeList.size(); i++){
                configChange = configChangeList.get(i);
                
                xpath = configChange.getXPath();
                if (xpath != null) {
                    object = configContext.exactLookup(xpath);
                    if (object != null) {
                        if ( object instanceof VirtualServer){
                            webContainer.updateHost((VirtualServer)object, 
                                                    config.getHttpService(), 
                                                    serverBean);
                        } else if (object instanceof ElementProperty) {
                            // Property has been added or updated
                            parentXpath = configChange.getParentXPath();
                            if (parentXpath == null) {
                                parentXpath =
                                    xpath.substring(0, xpath.lastIndexOf("/"));
                            }
                            vsBean = (VirtualServer)
                                configContext.exactLookup(parentXpath);
                            elementProperty = (ElementProperty)object;
                            webContainer.updateHostProperties(
                                       vsBean, 
                                       elementProperty.getName(),
                                       elementProperty.getValue(),
                                       config.getHttpService(),
                                       config.getSecurityService());
                        }

                    } else {
                        // Property has been deleted.
                        // Determine the property's name
                        int lastSlash = xpath.lastIndexOf("/");
                        parentXpath = xpath.substring(0, lastSlash);
                        String propName = xpath.substring(lastSlash);
                        if (propName != null) {
                            // e.g., /element-property[@name='sso-enabled']
                            int beginQuote = propName.indexOf("'");
                            int endQuote = propName.lastIndexOf("'");
                            if (endQuote > beginQuote) {
                                propName = propName.substring(beginQuote+1,
                                                              endQuote);
                                vsBean = (VirtualServer)
                                    configContext.exactLookup(parentXpath);
                                webContainer.updateHostProperties(
                                       vsBean,
                                       propName,
                                       null,
                                       config.getHttpService(),
                                       config.getSecurityService());
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
           throw new AdminEventListenerException(ex);
        } 
    }
    
    /**
     * Delete an existing virtual-server. 
     *
     * Implementation note: we cannot use the same approach used when
     * handling create/update event since the element is deleted before this
     * method is invoked (strange behaviour). Instead we need to find by 
     * ourself which virtual-server has been deleted.
     */
    public void handleDelete(HSVirtualServerEvent event)
            throws AdminEventListenerException {

        if (webContainer == null || event == null) {
            return;       
        }

        try {
            ConfigContext configContext = event.getConfigContext();
            Config config = ServerBeansFactory.getConfigBean(configContext);
            if (config != null) {
                webContainer.deleteHost(config.getHttpService());    
            }
        } catch( Exception ex){
           throw new AdminEventListenerException(ex);
        }    
    }


    /*
     * Dynamic reconfig events pertaining to <http-protocol>
     */

    public void handleDelete(HSHttpProtocolEvent event)
            throws AdminEventListenerException {
        // Ignore
    }

    public void handleUpdate(HSHttpProtocolEvent event)
            throws AdminEventListenerException {

        if (webContainer == null) {
            return;
        }

        try {
            ConfigContext configContext = event.getConfigContext();
            Config config = ServerBeansFactory.getConfigBean(configContext);
            if (config == null) {
                return;
            }
            webContainer.updateHttpService(config.getHttpService());
        } catch (Exception e) {
           throw new AdminEventListenerException(e);
        } 
    }

    public void handleCreate(HSHttpProtocolEvent event)
            throws AdminEventListenerException {
        // Ignore
    }


    /*
     * Dynamic reconfig events pertaining to <http-file-cache>
     */

    public void handleDelete(HSHttpFileCacheEvent event)
            throws AdminEventListenerException {
        // Ignore
    }

    public void handleUpdate(HSHttpFileCacheEvent event)
            throws AdminEventListenerException {

        if (webContainer == null) {
            return;
        }

        try {
            ConfigContext configContext = event.getConfigContext();
            Config config = ServerBeansFactory.getConfigBean(configContext);
            if (config == null) {
                return;
            }
            webContainer.updateHttpService(config.getHttpService());
        } catch (Exception e) {
           throw new AdminEventListenerException(e);
        } 
    }

    public void handleCreate(HSHttpFileCacheEvent event)
            throws AdminEventListenerException {
        // Ignore
    }


    /*
     * Dynamic reconfig events pertaining to <connection-pool>
     */

    public void handleDelete(HSConnectionPoolEvent event)
            throws AdminEventListenerException {
        // Ignore
    }

    public void handleUpdate(HSConnectionPoolEvent event)
            throws AdminEventListenerException {

        if (webContainer == null) {
            return;
        }

        try {
            ConfigContext configContext = event.getConfigContext();
            Config config = ServerBeansFactory.getConfigBean(configContext);
            if (config == null) {
                return;
            }
            webContainer.updateHttpService(config.getHttpService());
        } catch (Exception e) {
           throw new AdminEventListenerException(e);
        } 
    }

    public void handleCreate(HSConnectionPoolEvent event)
            throws AdminEventListenerException {
        // Ignore
    }
    
    
    /*
     * Dynamic reconfig events pertaining to <keep-alive>
     */

    public void handleDelete(HSKeepAliveEvent event)
            throws AdminEventListenerException {
        // Ignore
    }

    public void handleUpdate(HSKeepAliveEvent event)
            throws AdminEventListenerException {

        if (webContainer == null) {
            return;
        }

        try {
            ConfigContext configContext = event.getConfigContext();
            Config config = ServerBeansFactory.getConfigBean(configContext);
            if (config == null) {
                return;
            }
            webContainer.updateHttpService(config.getHttpService());
        } catch (Exception e) {
           throw new AdminEventListenerException(e);
        } 
    }

    public void handleCreate(HSKeepAliveEvent event)
            throws AdminEventListenerException {
        // Ignore
    }
    
    
    /*
     * Dynamic reconfig events pertaining to <request-processing>
     */

    public void handleDelete(HSRequestProcessingEvent event)
            throws AdminEventListenerException {
        // Ignore
    }

    public void handleUpdate(HSRequestProcessingEvent event)
            throws AdminEventListenerException {

        if (webContainer == null) {
            return;
        }

        try {
            ConfigContext configContext = event.getConfigContext();
            Config config = ServerBeansFactory.getConfigBean(configContext);
            if (config == null) {
                return;
            }
            webContainer.updateHttpService(config.getHttpService());
        } catch (Exception e) {
           throw new AdminEventListenerException(e);
        } 
    }

    public void handleCreate(HSRequestProcessingEvent event)
            throws AdminEventListenerException {
        // Ignore
    }


}
