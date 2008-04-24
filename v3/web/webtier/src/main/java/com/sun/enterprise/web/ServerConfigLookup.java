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

package com.sun.enterprise.web;

import com.sun.enterprise.config.serverbeans.AvailabilityService;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ManagerProperties;
import com.sun.enterprise.config.serverbeans.SessionConfig;
import com.sun.enterprise.config.serverbeans.SessionManager;
import com.sun.enterprise.config.serverbeans.SessionProperties;
import com.sun.enterprise.config.serverbeans.StoreProperties;

public class ServerConfigLookup {

    private Config configBean;

    public ServerConfigLookup(Config configBean) {
        this.configBean = configBean;
    }

    /**
     * Get the availability-service element from domain.xml.
     * return null if not found
     */     
    protected AvailabilityService getAvailabilityService() {
        if (configBean == null) {
            return null;
        }

        return configBean.getAvailabilityService();
    }    

    /**
     * Get the session manager bean from domain.xml
     * return null if not defined or other problem
     */  
    public SessionManager getInstanceSessionManager() { 
        if (configBean == null) {
            return null;
        }
        
        com.sun.enterprise.config.serverbeans.WebContainer webContainerBean 
            = configBean.getWebContainer();
        if (webContainerBean == null) {
            return null;
        }
        
        SessionConfig sessionConfigBean = webContainerBean.getSessionConfig();
        if (sessionConfigBean == null) {
            return null;
        }
        
        return sessionConfigBean.getSessionManager();
    }    
    
    /**
     * Get the manager properties bean from domain.xml
     * return null if not defined or other problem
     */  
    public ManagerProperties getInstanceSessionManagerManagerProperties() {
        
        SessionManager smBean = getInstanceSessionManager();
        if (smBean == null) {
            return null;
        }

        return smBean.getManagerProperties();
    } 
    
    /**
     * Get the store properties bean from domain.xml
     * return null if not defined or other problem
     */  
    public StoreProperties getInstanceSessionManagerStoreProperties() {
        
        SessionManager smBean = getInstanceSessionManager();
        if (smBean == null) {
            return null;
        }

        return smBean.getStoreProperties();
    } 

    /**
     * Get the session properties bean from server.xml
     * return null if not defined or other problem
     */      
    public SessionProperties getInstanceSessionProperties() { 
        if (configBean == null) {
            return null;
        }
        
        com.sun.enterprise.config.serverbeans.WebContainer webContainerBean 
            = configBean.getWebContainer();
        if (webContainerBean == null) {
            return null;
        }
        
        SessionConfig sessionConfigBean = webContainerBean.getSessionConfig();
        if(sessionConfigBean == null) {
            return null;
        }
        
        return sessionConfigBean.getSessionProperties();
    }        
}
