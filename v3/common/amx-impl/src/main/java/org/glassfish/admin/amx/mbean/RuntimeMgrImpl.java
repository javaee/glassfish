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
package org.glassfish.admin.amx.mbean;

import java.util.Map;
import javax.management.ObjectName;

import com.sun.appserv.management.ext.runtime.RuntimeMgr;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.glassfish.api.container.Sniffer;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;

/**
    AMX RealmsMgr implementation.
    Note that realms don't load until {@link #loadRealms} is called.
 */
public final class RuntimeMgrImpl extends AMXNonConfigImplBase implements RuntimeMgr
{
        
        private final ApplicationRegistry appRegistry;
        
		public
	RuntimeMgrImpl( final ObjectName containerObjectName )
	{
            super( RuntimeMgr.J2EE_TYPE, RuntimeMgr.J2EE_TYPE, containerObjectName, RuntimeMgr.class, null);
            appRegistry = Globals.getDefaultHabitat().getComponent(ApplicationRegistry.class);
        }
    
        /**
         * 
         * Returns the deployment configuration(s), if any, for the specified
         * application.
         * <p>
         * For Java EE applications these will typically be the deployment
         * descriptors, with the map key the relative path to the DD and the
         * value that deployment descriptor's contents.  
         * 
         * @param appName name of the application of interest
         * @return map of app config names to config values
         */        
        public Map<String,String>
    getDeploymentConfigurations( final String appName)
    {
        final ApplicationInfo appInfo = appRegistry.get(appName);
        if (appInfo == null) {
            throw new IllegalArgumentException(appName);
        }
        
        final Map<String,String> result = new HashMap<String,String>();
        try {
            for (Sniffer sniffer : appInfo.getSniffers()) {
                result.putAll(sniffer.getDeploymentConfigurations(appInfo.getSource()));
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /* Connector Runtime exposed APIS */

    public Map<String,Object>  getConnectionDefinitionPropertiesAndDefaults( final String datasourceClassName )
            throws Exception{
        return getConnectorRuntime().getConnectionDefinitionPropertiesAndDefaults(datasourceClassName);
    }

    public Map<String,String> getBuiltInCustomResources() throws Exception {
        return getConnectorRuntime().getBuiltInCustomResources();
    }

    public String[] getConnectionDefinitionNames(String rarName) throws Exception{
        return getConnectorRuntime().getConnectionDefinitionNames(rarName);
    }

    public Properties getMCFConfigProps(String rarName,String connectionDefName) throws Exception {
        return getConnectorRuntime().getMCFConfigProps(rarName, connectionDefName);
    }

    public String[] getAdminObjectInterfaceNames(String rarName) throws Exception {
        return getConnectorRuntime().getAdminObjectInterfaceNames(rarName);
    }

    public Properties getResourceAdapterConfigProps(String rarName) throws Exception {
        return getConnectorRuntime().getResourceAdapterConfigProps(rarName);
    }

    public Properties getAdminObjectConfigProps(String rarName,String adminObjectIntf) throws Exception {
        return getConnectorRuntime().getAdminObjectConfigProps(rarName, adminObjectIntf);        
    }

    public Properties getConnectorConfigJavaBeans(String rarName, String connectionDefName,String type)
            throws Exception {
        return getConnectorRuntime().getConnectorConfigJavaBeans(rarName, connectionDefName, type);
    }

    public String getActivationSpecClass( String rarName,
             String messageListenerType) throws Exception {
        return getConnectorRuntime().getActivationSpecClass(rarName, messageListenerType);
    }

    public String[] getMessageListenerTypes(String rarName) throws Exception {
        return getConnectorRuntime().getMessageListenerTypes(rarName);
    }

    public Properties getMessageListenerConfigProps(String rarName,
         String messageListenerType)throws Exception {
        return getConnectorRuntime().getMessageListenerConfigProps(rarName, messageListenerType);
    }

    public Properties getMessageListenerConfigPropTypes(String rarName,
               String messageListenerType) throws Exception {
        return getConnectorRuntime().getMessageListenerConfigPropTypes(rarName, messageListenerType);
    }

    private ConnectorRuntime getConnectorRuntime(){
        return Globals.getDefaultHabitat().getComponent(ConnectorRuntime.class);
    }
    
    /* Connector Runtime exposed APIS */

}
