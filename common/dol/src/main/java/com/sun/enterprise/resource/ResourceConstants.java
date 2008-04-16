/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.resource;

/**
 * A constants class housing all the resource related constants
 * @author Sivakumar Thyagarajan
 */
public final class ResourceConstants {

    //Attribute names constants
    // JDBC Resource
    public static final String JNDI_NAME = "jndi-name";

    public static final String POOL_NAME = "pool-name";

    // JMS Resource                                        
    public static final String RES_TYPE = "res-type";

    public static final String FACTORY_CLASS = "factory-class";

    public static final String ENABLED = "enabled";

    // External JNDI Resource
    public static final String JNDI_LOOKUP = "jndi-lookup-name";

    // JDBC Connection pool
    public static final String CONNECTION_POOL_NAME = "name";

    public static final String STEADY_POOL_SIZE = "steady-pool-size";

    public static final String MAX_POOL_SIZE = "max-pool-size";

    public static final String MAX_WAIT_TIME_IN_MILLIS = "max-wait-time-in-millis";

    public static final String POOL_SIZE_QUANTITY = "pool-resize-quantity";

    public static final String IDLE_TIME_OUT_IN_SECONDS = "idle-timeout-in-seconds";

    public static final String IS_CONNECTION_VALIDATION_REQUIRED = "is-connection-validation-required";

    public static final String CONNECTION_VALIDATION_METHOD = "connection-validation-method";

    public static final String FAIL_ALL_CONNECTIONS = "fail-all-connections";

    public static final String VALIDATION_TABLE_NAME = "validation-table-name";

    public static final String DATASOURCE_CLASS = "datasource-classname";

    public static final String TRANS_ISOLATION_LEVEL = "transaction-isolation-level";

    public static final String IS_ISOLATION_LEVEL_GUARANTEED = "is-isolation-level-guaranteed";

    //Mail resource
    public static final String MAIL_HOST = "host";

    public static final String MAIL_USER = "user";

    public static final String MAIL_FROM_ADDRESS = "from";

    public static final String MAIL_STORE_PROTO = "store-protocol";

    public static final String MAIL_STORE_PROTO_CLASS = "store-protocol-class";

    public static final String MAIL_TRANS_PROTO = "transport-protocol";

    public static final String MAIL_TRANS_PROTO_CLASS = "transport-protocol-class";

    public static final String MAIL_DEBUG = "debug";

    //Persistence Manager Factory resource
    public static final String JDBC_RESOURCE_JNDI_NAME = "jdbc-resource-jndi-name";

    //Admin Object resource
    public static final String RES_ADAPTER = "res-adapter";

    //Connector resource
    public static final String RESOURCE_TYPE = "resource-type";

    // ConnectorConnection Pool resource ...
    // child elements
    public static final String CONNECTOR_CONN_DESCRIPTION = "description";

    public static final String CONNECTOR_SECURITY_MAP = "security-map";

    public static final String CONNECTOR_PROPERTY = "property";

    //attributes....
    public static final String CONNECTOR_CONNECTION_POOL_NAME = "name";

    public static final String RESOURCE_ADAPTER_CONFIG_NAME = "resource-adapter-name";

    public static final String CONN_DEF_NAME = "connection-definition-name";

    public static final String CONN_STEADY_POOL_SIZE = "steady-pool-size";

    public static final String CONN_MAX_POOL_SIZE = "max-pool-size";

    public static final String CONN_POOL_RESIZE_QUANTITY = "pool-resize-quantity";

    public static final String CONN_IDLE_TIME_OUT = "idle-timeout-in-seconds";

    public static final String CONN_FAIL_ALL_CONNECTIONS = "fail-all-connections";

    //Security Map elements...
    public static final String SECURITY_MAP = "security-map";

    public static final String SECURITY_MAP_NAME = "name";

    public static final String PRINCIPAL = "principal";

    public static final String USERGROUP = "user-group";

    public static final String BACKEND_PRINCIPAL = "backend-principal";

    //Resource -Adapter config attributes.
    public static final String RES_ADAPTER_CONFIG = "resource-adapter-config";

    public static final String THREAD_POOL_IDS = "thread-pool-ids";

    public static final String RES_ADAPTER_NAME = "resource-adapter-name";

    //Backend Principal elements....
    public static final String USER_NAME = "user-name";

    public static final String PASSWORD = "password";

}
