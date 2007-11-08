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

package com.sun.jbi.jsf.framework.services;

import com.sun.jbi.jsf.framework.services.configuration.ConfigurationService;
import com.sun.jbi.jsf.framework.services.management.*;
import com.sun.jbi.jsf.framework.services.statistics.StatisticsService;
import java.io.Serializable;
import java.util.logging.Logger;

import com.sun.jbi.jsf.framework.connectors.ServerConnector;
import com.sun.jbi.jsf.framework.connectors.LocalServerConnector;
import com.sun.jbi.jsf.framework.services.administration.AdministrationService;


/**
 * ServiceManager - singleton - provides the list of supported services
 *  e.g Configuration, Performance, Administration, Management
 *
 * @author Sun Microsystems
 */
public class ServiceManager implements Serializable {

    private ServerConnector connector;
    
    private static ServiceManager serviceManager;

    private static Logger logger = Logger.getLogger(ServiceManager.class.getName());

    
    private ServiceManager() {
        this(null);
    }

    public static ServiceManager getInstance() {
        if ( serviceManager==null ) {
            serviceManager = new ServiceManager();
        }
        return serviceManager;
    }
    
    
    public static ServiceManager getInstance(ServerConnector serverConnector) {
        if ( serviceManager==null ) {
            serviceManager = new ServiceManager(serverConnector);
        }
        return serviceManager;
    }
    
    
    /**
     * Creates a new instance of ServiceManager
     * @param connector
     */
    private ServiceManager(ServerConnector connector) {
        if ( connector==null ) {
            this.connector = new LocalServerConnector();
        } else {
            this.connector = connector;
        }
    }
    
    
   public ConfigurationService getConfigurationService(String targetName) {
       // todo - specific instance should be based on server type
       return ServiceFactory.getConfigurationService(this.connector,targetName);
   }

   
   public ManagementService getManagementService(String targetName) {
       // todo - specific instance should be based on server type
       return ServiceFactory.getManagementService(this.connector,targetName);
   }
   
   public AdministrationService getAdministrationService(String targetName) {
       // todo - specific instance should be based on server type
       return ServiceFactory.getAdministrationService(this.connector,targetName);
   }   
      
   public StatisticsService getStatisticsService(String targetName) {
       // todo - specific instance should be based on server type
       return ServiceFactory.getStatisticsService(this.connector,targetName);
   }
   
}
