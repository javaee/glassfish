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
package org.glassfish.admin.amx.impl.ext;

import java.util.Map;
import javax.management.ObjectName;

import java.util.HashMap;
import java.util.Set;

import org.glassfish.admin.amx.base.ConnectorRuntimeAPIProvider;
import org.glassfish.admin.amx.impl.mbean.AMXImplBase;
import org.glassfish.admin.amx.util.ExceptionUtil;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import javax.resource.ResourceException;

/**
 * ConnectorRuntime exposed APIs via AMX
 */
public final class ConnectorRuntimeAPIProviderImpl extends AMXImplBase
// implements Runtime
{
    private final Habitat mHabitat;

    public ConnectorRuntimeAPIProviderImpl(final ObjectName parent, Habitat habitat)
    {
        super(parent, ConnectorRuntimeAPIProvider.class);

        if (habitat != null)
        {
            mHabitat = habitat;
        }
        else
        {
            throw new IllegalStateException("Habitat is null");
        }
    }

    public Map<String, Object> getConnectionDefinitionPropertiesAndDefaults(final String datasourceClassName,
            final String resType) {
        final Map<String, Object> result = new HashMap<String, Object>();

        // get connector runtime
        try
        {
            final Map<String, Object> connProps = getConnectorRuntime().
                    getConnectionDefinitionPropertiesAndDefaults(datasourceClassName, resType);
            result.put(ConnectorRuntimeAPIProvider.PROPERTY_MAP_KEY, connProps);
        }
        catch (ComponentException e)
        {
            result.put(ConnectorRuntimeAPIProvider.PROPERTY_MAP_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }

        // got everything, now get properties
        return result;
    }

    private ConnectorRuntime getConnectorRuntime()
    {
        return mHabitat.getComponent(ConnectorRuntime.class, null);
    }

    public Map<String, Object> getSystemConnectorsAllowingPoolCreation(){
        final Map<String, Object> result = new HashMap<String, Object>();

        try
        {
            final String[] systemRars = getConnectorRuntime().getSystemConnectorsAllowingPoolCreation();
            result.put(ConnectorRuntimeAPIProvider.STRING_ARRAY_KEY, systemRars);
        }
        catch (ComponentException e)
        {
            result.put(ConnectorRuntimeAPIProvider.STRING_ARRAY_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        return result;
    }

    public Map<String, Object> getBuiltInCustomResources()
    {
        final Map<String, Object> result = new HashMap<String, Object>();

        try
        {
            final Map<String, String> customResources = getConnectorRuntime().getBuiltInCustomResources();
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, customResources);
        }
        catch (ComponentException e)
        {
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        return result;
    }

    public Map<String, Object> getConnectionDefinitionNames(String rarName)
    {
        final Map<String, Object> result = new HashMap<String, Object>();

        final String[] conDefnNames;
        try
        {
            conDefnNames = getConnectorRuntime().getConnectionDefinitionNames(rarName);
            result.put(ConnectorRuntimeAPIProvider.STRING_ARRAY_KEY, conDefnNames);
        }
        catch (ConnectorRuntimeException e)
        {
            result.put(ConnectorRuntimeAPIProvider.STRING_ARRAY_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        catch (ComponentException e)
        {
            result.put(ConnectorRuntimeAPIProvider.STRING_ARRAY_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        return result;
    }

    public Map<String, Object> getMCFConfigProps(String rarName, String connectionDefName)
    {
        final Map<String, Object> result = new HashMap<String, Object>();

        try
        {
            Map<String, String> configProperties = getConnectorRuntime().getMCFConfigProps(rarName, connectionDefName);
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, configProperties);
        }
        catch (ConnectorRuntimeException e)
        {
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        catch (ComponentException e)
        {
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        return result;
    }

    public Map<String, Object> getAdminObjectInterfaceNames(String rarName)
    {

        final Map<String, Object> result = new HashMap<String, Object>();

        try
        {
            final String[] adminObjectInterfaceNames = getConnectorRuntime().getAdminObjectInterfaceNames(rarName);
            result.put(ConnectorRuntimeAPIProvider.STRING_ARRAY_KEY, adminObjectInterfaceNames);
        }
        catch (ConnectorRuntimeException e)
        {
            result.put(ConnectorRuntimeAPIProvider.STRING_ARRAY_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        catch (ComponentException e)
        {
            result.put(ConnectorRuntimeAPIProvider.STRING_ARRAY_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        return result;
    }

    public Map<String, Object> getResourceAdapterConfigProps(String rarName)
    {
        final Map<String, Object> result = new HashMap<String, Object>();

        try
        {
            Map<String, String> configProperties = getConnectorRuntime().getResourceAdapterConfigProps(rarName);
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, configProperties);
        }
        catch (ConnectorRuntimeException e)
        {
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        catch (ComponentException e)
        {
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        return result;
    }

    public Map<String, Object> getAdminObjectConfigProps(String rarName, String adminObjectIntf)
    {
        final Map<String, Object> result = new HashMap<String, Object>();

        try
        {
            Map<String, String> configProperties = getConnectorRuntime().getAdminObjectConfigProps(rarName, adminObjectIntf);
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, configProperties);
        }
        catch (ConnectorRuntimeException e)
        {
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        catch (ComponentException e)
        {
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        return result;
    }

    public Map<String, Object> getConnectorConfigJavaBeans(String rarName, String connectionDefName, String type)
    {
        final Map<String, Object> result = new HashMap<String, Object>();

        try
        {
            Map<String, String> configProperties = getConnectorRuntime().getConnectorConfigJavaBeans(rarName, connectionDefName, type);
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, configProperties);
        }
        catch (ConnectorRuntimeException e)
        {
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        catch (ComponentException e)
        {
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        return result;
    }

    public Map<String, Object> getActivationSpecClass(String rarName,
                                                      String messageListenerType)
    {
        final Map<String, Object> result = new HashMap<String, Object>();

        try
        {
            String activationSpec = getConnectorRuntime().getActivationSpecClass(rarName, messageListenerType);
            result.put(ConnectorRuntimeAPIProvider.STRING_KEY, activationSpec);
        }
        catch (ConnectorRuntimeException e)
        {
            result.put(ConnectorRuntimeAPIProvider.STRING_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        catch (ComponentException e)
        {
            result.put(ConnectorRuntimeAPIProvider.STRING_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        return result;
    }

    public Map<String, Object> getMessageListenerTypes(String rarName)
    {

        final Map<String, Object> result = new HashMap<String, Object>();

        try
        {
            final String[] messageListenerTypes = getConnectorRuntime().getMessageListenerTypes(rarName);
            result.put(ConnectorRuntimeAPIProvider.STRING_ARRAY_KEY, messageListenerTypes);
        }
        catch (ConnectorRuntimeException e)
        {
            result.put(ConnectorRuntimeAPIProvider.STRING_ARRAY_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        catch (ComponentException e)
        {
            result.put(ConnectorRuntimeAPIProvider.STRING_ARRAY_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        return result;
    }

    public Map<String, Object> getMessageListenerConfigProps(String rarName,
                                                             String messageListenerType)
    {

        final Map<String, Object> result = new HashMap<String, Object>();

        try
        {
            Map<String, String> configProperties = getConnectorRuntime().getMessageListenerConfigProps(
                    rarName, messageListenerType);
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, configProperties);
        }
        catch (ConnectorRuntimeException e)
        {
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        catch (ComponentException e)
        {
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        return result;
    }

    public Map<String, Object> getMessageListenerConfigPropTypes(String rarName,
                                                                 String messageListenerType)
    {
        final Map<String, Object> result = new HashMap<String, Object>();

        try
        {
            Map<String, String> configProperties = getConnectorRuntime().getMessageListenerConfigPropTypes(
                    rarName, messageListenerType);
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, configProperties);
        }
        catch (ConnectorRuntimeException e)
        {
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        catch (ComponentException e)
        {
            result.put(ConnectorRuntimeAPIProvider.MAP_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        return result;
    }

    /**
     * Flush Connection pool.
     * @param poolName
     */
    public Map<String, Object> flushConnectionPool(final String poolName)
    {
        final Map<String, Object> result = new HashMap<String, Object>();

        if (mHabitat == null)
        {
            result.put(ConnectorRuntimeAPIProvider.BOOLEAN_KEY, false);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, "Habitat is null");
            return result;
        }
        try
        {
            final ConnectorRuntime connRuntime = mHabitat.getComponent(ConnectorRuntime.class, null);
            connRuntime.flushConnectionPool(poolName);
            result.put(ConnectorRuntimeAPIProvider.BOOLEAN_KEY, true);
        }
        catch (ConnectorRuntimeException ex)
        {
            result.put(ConnectorRuntimeAPIProvider.BOOLEAN_KEY, false);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(ex));
        }
        catch (ComponentException e)
        {
            result.put(ConnectorRuntimeAPIProvider.BOOLEAN_KEY, false);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        return result;
    }

    /**
     * Obtain connection validation table names.
     * @param poolName
     */
    public Map<String, Object> getValidationTableNames(final String poolName)
    {
        final Map<String, Object> result = new HashMap<String, Object>();

        try
        {
            final ConnectorRuntime connRuntime = mHabitat.getComponent(ConnectorRuntime.class, null);
            final Set<String> tableNames = connRuntime.getValidationTableNames(poolName);
            result.put(ConnectorRuntimeAPIProvider.SET_KEY, tableNames);
        }
        catch (ComponentException e)
        {
            result.put(ConnectorRuntimeAPIProvider.SET_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        catch (Exception e)
        {
            result.put(ConnectorRuntimeAPIProvider.SET_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        return result;
    }

    /**
     * Obtain Jdbc driver implementation class names.
     * @param dbVendor
     * @param resType
     */
    public Map<String, Object> getJdbcDriverClassNames(final String dbVendor,
                                                       final String resType)
    {
        final Map<String, Object> result = new HashMap<String, Object>();

        try
        {
            final ConnectorRuntime connRuntime = mHabitat.getComponent(ConnectorRuntime.class, null);
            final Set<String> implClassNames = connRuntime.getJdbcDriverClassNames(dbVendor, resType);
            result.put(ConnectorRuntimeAPIProvider.SET_KEY, implClassNames);
        }
        catch (ComponentException e)
        {
            result.put(ConnectorRuntimeAPIProvider.SET_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        catch (Exception e)
        {
            result.put(ConnectorRuntimeAPIProvider.SET_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        return result;
    }

    /**
     * Ping JDBC Connection Pool and return status
     * @param poolName
     * @return
     */
    public Map<String, Object> pingJDBCConnectionPool(final String poolName)
    {
        final Map<String, Object> result = new HashMap<String, Object>();

        if (mHabitat == null)
        {
            result.put(ConnectorRuntimeAPIProvider.BOOLEAN_KEY, false);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, "Habitat is null");
            return result;
        }
        try
        {
            final ConnectorRuntime connRuntime = mHabitat.getComponent(ConnectorRuntime.class, null);
            final boolean pingStatus = connRuntime.pingConnectionPool(poolName);
            result.put(ConnectorRuntimeAPIProvider.BOOLEAN_KEY, pingStatus);
        }
        catch (ResourceException ex)
        {
            result.put(ConnectorRuntimeAPIProvider.BOOLEAN_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(ex));
        }
        catch (ComponentException e)
        {
            result.put(ConnectorRuntimeAPIProvider.BOOLEAN_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        catch (Exception e)
        {
            result.put(ConnectorRuntimeAPIProvider.BOOLEAN_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        return result;
    }

    /**
     * Obtain connection validation class names.
     * @param poolName
     */
    public Map<String, Object> getValidationClassNames(final String dbVendor) {
        final Map<String, Object> result = new HashMap<String, Object>();

        try {
            final ConnectorRuntime connRuntime = mHabitat.getComponent(ConnectorRuntime.class, null);
            final Set<String> valClassNames = connRuntime.getValidationClassNames(dbVendor);
            result.put(ConnectorRuntimeAPIProvider.SET_KEY, valClassNames);
        } catch (ComponentException e) {
            result.put(ConnectorRuntimeAPIProvider.SET_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        } catch (Exception e) {
            result.put(ConnectorRuntimeAPIProvider.SET_KEY, null);
            result.put(ConnectorRuntimeAPIProvider.REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        return result;
    }

}
