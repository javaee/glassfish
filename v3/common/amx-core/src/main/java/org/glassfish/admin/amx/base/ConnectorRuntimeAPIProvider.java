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
package org.glassfish.admin.amx.base;

import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.annotation.Description;
import org.glassfish.admin.amx.annotation.Param;
import org.glassfish.admin.amx.core.AMXProxy;

import java.util.Map;
import javax.management.MBeanOperationInfo;
import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.annotation.Stability;
import org.glassfish.admin.amx.annotation.Taxonomy;
import org.glassfish.api.amx.AMXMBeanMetadata;

/**
@since GlassFish V3
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
@AMXMBeanMetadata(singleton = true, globalSingleton = true, leaf = true)
public interface ConnectorRuntimeAPIProvider extends AMXProxy, Utility, Singleton
{
    /** Key into Map returned by various methods including {@link #getConnectionDefinitionPropertiesAndDefaults} */
    public static final String PROPERTY_MAP_KEY = "PropertyMapKey";

    /** Key into Map returned by various methods including {@link #getConnectionDefinitionNames}
     * {@link #getAdminObjectInterfaceNames}
     * {@link #getMessageListenerTypes}
     * {@link #getMessageListenerTypes}
     * **/
    public static final String STRING_ARRAY_KEY = "StringArrayKey";

    /** Key into Map returned by {@link #getActivationSpecClass} **/
    public static final String STRING_KEY = "StringKey";

    /** Key into Map returned by various methods including
     * {@link #getBuiltInCustomResources}
     * {@link #getMCFConfigProps}
     * {@link #getResourceAdapterConfigProps}
     * {@link #getAdminObjectConfigProps}
     * {@link #getConnectorConfigJavaBeans}
     * {@link #getMessageListenerConfigProps}
     * {@link #getMessageListenerConfigPropTypes} **/
    public static final String MAP_KEY = "MapKey";

    /** Key into Map returned by various methods including {@link #flushConnectionPool} */
    public static final String BOOLEAN_KEY = "BooleanKey";

    /** Key into Set returned by various methods including {@link #getValidationTableNames} {@link #getJdbcDriverClassNames} */
    public static final String SET_KEY = "SetKey";

    /** Key into Map returned by various methods including {@link #getConnectionDefinitionPropertiesAndDefaults}
     * {@link #getConnectionDefinitionNames}
     * {@link #getAdminObjectInterfaceNames}
     * {@link #getMessageListenerTypes}
     * {@link #getMessageListenerTypes}
     * {@link #getBuiltInCustomResources}
     * {@link #getValidationTableNames}
     * {@link #getJdbcDriverClassNames}
     * {@link #flushConnectionPool}
     * {@link #getMCFConfigProps}
     * {@link #getResourceAdapterConfigProps}
     * {@link #getAdminObjectConfigProps}
     * {@link #getConnectorConfigJavaBeans}
     * {@link #getMessageListenerConfigProps}
     * {@link #getMessageListenerConfigPropTypes}
     * **/
    public static final String REASON_FAILED_KEY = "ReasonFailedKey";

    /**
    Get properties of JDBC Data Source
    @see #PROPERTY_MAP_KEY
    @see #REASON_FAILED_KEY
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Returns the connection definition properties and their default values of a datasource class")
    public Map<String, Object> getConnectionDefinitionPropertiesAndDefaults(
            @Param(name = "datasourceClassName") String datasourceClassName,
            @Param(name = "resType") String resType);

    @ManagedAttribute
    @Description("List of built in custom resource factory classes")
    public Map<String, Object> getBuiltInCustomResources();

    @ManagedAttribute
    @Description("List of system resource-adapters that allow connector-connection-pool creation")
    public Map<String, Object> getSystemConnectorsAllowingPoolCreation();

    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("List of connection definition names for the given resource-adapter")
    public Map<String, Object> getConnectionDefinitionNames(@Param(name = "rarName") String rarName);

    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("get the MCF config properties of the connection definition")
    public Map<String, Object> getMCFConfigProps(
            @Param(name = "rarName") String rarName,
            @Param(name = "connectionDefName") String connectionDefName);

    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("List of administered object interfaces for the given resource-adapter")
    public Map<String, Object> getAdminObjectInterfaceNames(@Param(name = "rarName") String rarName);

    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("List of resource adapter configuration properties of a resource-adapter")
    public Map<String, Object> getResourceAdapterConfigProps(@Param(name = "rarName") String rarName);

    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("List of administered object configuration properties")
    public Map<String, Object> getAdminObjectConfigProps(
            @Param(name = "rarName") String rarName,
            @Param(name = "adminObjectIntf") String adminObjectIntf);

    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("List of java bean properties and their default values for a connection definition")
    public Map<String, Object> getConnectorConfigJavaBeans(
            @Param(name = "rarName") String rarName,
            @Param(name = "connectionDefName") String connectionDefName,
            @Param(name = "type") String type);

    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("get the activation spec class for the given message-listener type of a resource-adapter")
    public Map<String, Object> getActivationSpecClass(
            @Param(name = "rarName") String rarName,
            @Param(name = "messageListenerType") String messageListenerType);

    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("get message listener types of a resource-adapter")
    public Map<String, Object> getMessageListenerTypes(@Param(name = "rarName") String rarName);

    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("get message listener config properties for the given message-listener-type of a resource-adapter")
    public Map<String, Object> getMessageListenerConfigProps(@Param(name = "rarName") String rarName,
                                                             @Param(name = "messageListenerType") String messageListenerType);

    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("get message listener config property types for the given message-listener-type of a resource-adapter")
    public Map<String, Object> getMessageListenerConfigPropTypes(
            @Param(name = "rarName") String rarName,
            @Param(name = "messageListenerType") String messageListenerType);

    /**
     * Flush Connection pool.
     * @param poolName
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Flush connection pool by reinitializing all connections established in the pool")
    public Map<String, Object> flushConnectionPool(@Param(name = "poolName") String poolName);

    /**
     * Obtain connection validation table names.
     * @param poolName
     * @return set of validation table names.
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Get Connection validation table names for display in GUI")
    public Map<String, Object> getValidationTableNames(@Param(name = "poolName") String poolName);

    /**
     * Obtain Jdbc driver implementation class names.
     * @param dbVendor
     * @param resType
     * @return set of implementation class names.
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Get Jdbc driver implementation class names")
    public Map<String, Object> getJdbcDriverClassNames(@Param(name = "dbVendor") String dbVendor, 
                                                       @Param(name = "resType") String resType);

    /**
     * Ping the ConnectionPool and return status.
     * @param poolName
     * @return
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Ping Connection Pool and return status")
    public Map<String, Object> pingJDBCConnectionPool(final String poolName);

    /**
     * Obtain connection validation class names.
     * @param poolName
     * @return Set of connection validation class names for custom validation.
     */
    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    @Description("Get connection validation class names for custom validation")
    public Map<String, Object> getValidationClassNames( final String dbVendor );   

}
