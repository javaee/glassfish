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

package com.sun.enterprise.server;

import com.sun.enterprise.config.*;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;

import com.sun.logging.LogDomains;
import com.sun.enterprise.connectors.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.util.io.FileUtils;

/**
  *
  * @author    Srikanth P
  * @version
 */

public class ConnectorResourcesLoader  extends ResourcesLoader{

    static Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);
    
    /** context of the server instance runtime */


    public ConnectorResourcesLoader() throws ConfigException{
        super(); 
    }



    public void load() {
        ConfigBean[][] cb = null;
        try {
            ResourcesUtil resourceUtil = ResourcesUtil.createInstance();
            cb = resourceUtil.getConnectorResources();
            if(cb != null)
                load(cb);
        }
        catch(ConfigException ce) {
            _logger.log(Level.SEVERE,"core.connectorresource_read_error" );
            _logger.log(Level.SEVERE,"" ,ce);
        }
    }

    /* Called from ApplicationLifecycle.onstartup() to load connector resources of standalone RAs, which
     * are not System RAs
     * It does not load RAConfigs as it is already loaded by call to loadRAConfigs()
     */
    public void loadConnectorResources() {
        ConfigBean[][] cb = null;
        try {
            ResourcesUtil resourceUtil = ResourcesUtil.createInstance();
            cb = resourceUtil.getStandAloneNonSystemRarConnectorResourcesWithoutRAConfigs();
            if(cb != null)
                load(cb);
        }
        catch(ConfigException ce) {
            _logger.log(Level.SEVERE,"core.connectorresource_read_error" );
            _logger.log(Level.SEVERE,"" ,ce);
        }
    }

    /* Called from ApplicationLifecycle.onstartup() to load RAConfigs
     */
    public void loadRAConfigs() {
        ConfigBean[][] cb = new ConfigBean[1][];;
        try {
            ConfigBean[] raConfBeans = ResourcesUtil.createInstance().getResourceAdapterConfigs();
            if(raConfBeans != null) {
                cb[0] = raConfBeans;
                load(cb);
            }
        }
        catch(ConfigException ce) {
            _logger.log(Level.SEVERE,"core.connectorresource_read_error" );
            _logger.log(Level.SEVERE,"" ,ce);
        }
    }

    public void load(String rarName) {

        ConfigBean[][] cb = null;
        try {
            ResourcesUtil resourceUtil = ResourcesUtil.createInstance();
            cb = resourceUtil.getAllConnectorResourcesForRar(rarName);
            if(cb != null)
                load(cb);
        }
        catch(ConfigException ce) {
            _logger.log(Level.SEVERE,"core.connectorresource_read_error" );
            _logger.log(Level.SEVERE,"" ,ce);

        }
    }
    
    public void loadRAConfigs(String rarName) {
        ConfigBean[][] cb = new ConfigBean[1][];;
        try {
            ResourcesUtil resourceUtil = ResourcesUtil.createInstance();
            ConfigBean[] raConfBeans = resourceUtil.getResourceAdapterConfigs(rarName);
            if(raConfBeans != null) {
                cb[0] = raConfBeans;
                load(cb);
            }
        }
        catch(ConfigException ce) {
            _logger.log(Level.SEVERE,"core.connectorresource_read_error" );
            _logger.log(Level.SEVERE,"" ,ce);
        }
    }

    public void loadEmbeddedRarRAConfigs(String appName) {
        ConfigBean[][] cb = new ConfigBean[1][];;
        try {
            ResourcesUtil resourceUtil = ResourcesUtil.createInstance();
            ConfigBean[] raConfBeans = 
                    resourceUtil.getEmbeddedRarResourceAdapterConfigs(appName);
            if(raConfBeans != null) {
                cb[0] = raConfBeans;
                load(cb);
            }
        }
        catch(ConfigException ce) {
            _logger.log(Level.SEVERE,"core.connectorresource_read_error" );
            _logger.log(Level.SEVERE,"" ,ce);
        }
    }

    public void loadEmbeddedRarResources(String appName, Application appDesc) {

        Set rars = appDesc.getRarDescriptors();

        for (Iterator itr = rars.iterator(); itr.hasNext();) {
            ConnectorDescriptor cd = (ConnectorDescriptor) itr.next();
            String rarName = cd.getDeployName();
            String embeddedRarName = appName+
                       ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER+
                       FileUtils.makeFriendlyFilenameNoExtension(rarName);
            load(embeddedRarName);
        }
    }
    
    public void stopActiveResourceAdapters(){
        ConnectorRuntime.stopAllActiveResourceAdapters();
    }
    
}
