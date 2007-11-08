/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.config;

import java.util.Map;
import java.util.HashMap;

/**
 * 
 * The class defines TopLink properties' names.
 * 
 * JPA persistence properties could be specified either in PersistenceUnit or 
 * passes to createEntityManagerFactory / createContainerEntityManagerFactory
 * methods of EntityManagerFactoryProvider.
 * 
 * Property values are usually case-insensitive with some common sense exceptions,
 * for instance class names.
 * 
 * @see CacheType
 * @see TargetDatabase
 * @see TargetServer
 * 
 */
public class TopLinkProperties {
    //Persistence Unit Properties
    public static final String TRANSACTION_TYPE = "javax.persistence.transactionType";
    public static final String JTA_DATASOURCE = "javax.persistence.jtaDataSource";
    public static final String NON_JTA_DATASOURCE = "javax.persistence.nonJtaDataSource"; 
    // Connection properties.
    public static final String JDBC_DRIVER = "toplink.jdbc.driver";
    public static final String JDBC_URL = "toplink.jdbc.url";
    // use "" to reset user name 
    public static final String JDBC_USER = "toplink.jdbc.user";
    public static final String JDBC_PASSWORD = "toplink.jdbc.password";

    // TopLink JDBC (internal) connection pools properties. Ignored in case external connection pools are used.
    // Maximum number of connections in TopLink write connection pool by default is 10.
    public static final String JDBC_WRITE_CONNECTIONS_MAX = "toplink.jdbc.write-connections.max"; 
    // Minimum number of connections in TopLink write connection pool by default is 5.
    public static final String JDBC_WRITE_CONNECTIONS_MIN = "toplink.jdbc.write-connections.min";
    // Maximum number of connections in TopLink read connection pool by default is 2.
    public static final String JDBC_READ_CONNECTIONS_MAX = "toplink.jdbc.read-connections.max";
    // Minimum number of connections in TopLink read connection pool by default is 2.
    public static final String JDBC_READ_CONNECTIONS_MIN = "toplink.jdbc.read-connections.min";
    // Indicates wheather connections in TopLink read connection pool should be shared.
    // Valid values are case-insensitive "false" and "true"; "false" is default.
    public static final String JDBC_READ_CONNECTIONS_SHARED = "toplink.jdbc.read-connections.shared";

    // Bind all parameters property. Valid values are case-insensitive "true" and "false"; "true" is default.
    public static final String JDBC_BIND_PARAMETERS = "toplink.jdbc.bind-parameters";

    // Caching Prefixes
    // Property names formed out of these prefixes by appending either 
    // entity name, or class name (indicating that the property values applies only to a particular entity)
    // or DEFAULT suffix (indicating that the property value applies to all entities).
    // CACHE_SIZE_ properties default value is 1000
    public static final String CACHE_SIZE_ = "toplink.cache.size.";
    // All valid values for CACHE_TYPE_ properties are declared in CacheType class.
    public static final String CACHE_TYPE_ = "toplink.cache.type.";
    // Indicates whether entity's cache should be shared.
    // Valid values are case-insensitive "false" and "true"; "false" is default.
    public static final String CACHE_SHARED_ = "toplink.cache.shared.";
    
    // Default Suffix could be appended to some prefixes to form a property name
    public static final String DEFAULT = "default";
    
    // Default caching properties - apply to all entities. 
    // May be overridden by individual entity property with the same prefix.
    public static final String CACHE_SIZE_DEFAULT = CACHE_SIZE_ + DEFAULT;
    public static final String CACHE_TYPE_DEFAULT = CACHE_TYPE_ + DEFAULT;
    public static final String CACHE_SHARED_DEFAULT = CACHE_SHARED_ + DEFAULT;

    // Customizations properties

    // The type of logger. By default DefaultSessionLog is used.
    // Valid values are the logger class name which implements oracle.toplink.essentials.logging.SessionLog
    // or one of values defined in LoggerType.
    public static final String LOGGING_LOGGER = "toplink.logging.logger";
    // Valid values are names of levels defined in java.util.logging.Level,
    // default value is java.util.logging.Level.CONFIG.getName()
    public static final String LOGGING_LEVEL = "toplink.logging.level";
    
    // Category-specific logging level prefix
    // Property names formed out of this prefix by appending a category name
    // e.g.) toplink.logging.level.sql
    // Valid categories are defined in SessionLog
    public static final String CATEGORY_LOGGING_LEVEL_ = "toplink.logging.level.";
    
    // By default ("true") the date is always logged.
    // This can be turned off ("false").
    public static final String  LOGGING_TIMESTAMP = "toplink.logging.timestamp";
    // By default ("true") the thread is logged at FINE or less level.
    // This can be turned off ("false").
    public static final String  LOGGING_THREAD = "toplink.logging.thread";
    // By default ("true") the Session is always printed whenever available.
    // This can be turned off ("false").
    public static final String  LOGGING_SESSION = "toplink.logging.session";
    // By default ("true") stack trace is logged for SEVERE all the time and at FINER level for WARNING or less.
    // This can be turned off ("false").
    public static final String  LOGGING_EXCEPTIONS = "toplink.logging.exceptions";
    
    // Valid values are defined in TargetDatabase class - they correspond to database platforms currently supported by TopLink.
    // Also a customary database platform may be specified by supplying a full class name.
    // Default value is TargetDatabase.Auto which means TopLink will try to automatically determine
    // the correct database platrorm type.
    public static final String TARGET_DATABASE = "toplink.target-database";
    
    // By default a unique session name is generated by TopLink, but the user
    // can provide a customary session name - and make sure it's unique.
    public static final String SESSION_NAME = "toplink.session-name";
    
    // Indicates whether weaving should be performed - "true" by default.
	public static final String WEAVING = "toplink.weaving";
    
    // Valid values are defined in TargetServer class - they correspond to server platforms currently supported by TopLink.
    // Also a customary server platform may be specified by supplying a full class name.
    // Specifying a name of the class implementing ExternalTransactionController sets
    // CustomServerPlatform with this controller.
    // Default is TargetServer.None - JSE case.
    public static final String TARGET_SERVER = "toplink.target-server";
    
    // Allows session customization. The value is a full name for a class which implements SessionCustomizer.
    // Session customizer called after all other properties have been processed.
    public static final String SESSION_CUSTOMIZER = "toplink.session.customizer";
// Under review    public static final String RELATIONSHIPS_FETCH_DEFAULT = "toplink.relationships-fetch-default";

    // Customization Prefix
    // Property names formed out of this prefix by appending either 
    // entity name, or class name (indicating that the property values applies only to a particular entity)
    // Allows descriptor customization. The value is a full name for a class which implements DescriptorCustomizer.
    // Only session customizer is called after processing these properties.
    public static final String DESCRIPTOR_CUSTOMIZER_ = "toplink.descriptor.customizer.";
    
    /**
     * Defines EntityManager cache behaviour after  a call to flush method
     * followed by a call to clear method.
     * This property could be specified while creating either EntityManagerFactory 
     * (either in the map passed to createEntityManagerFactory method or in persistence.xml)
     * or EntityManager (in the map passed to createEntityManager method);
     * the latter overrides the former.
     * @see FlushClearCache
     */
    public static final String FLUSH_CLEAR_CACHE = "toplink.flush-clear.cache";
    
    // The following properties will not be displayed through logging but instead have an alternate value shown in the log
    public static final Map<String, String> PROPERTY_LOG_OVERRIDES = new HashMap<String, String>(1);
    
    //for gf3334, this property force persistence context to read through JTA-managed ("write") connection in case there is an active transaction.    
    public static final String JOIN_EXISTING_TRANSACTION = "toplink.transaction.join-existing";
    static {
        PROPERTY_LOG_OVERRIDES.put(JDBC_PASSWORD, "xxxxxx");
    }
    
    public static final String getOverriddenLogStringForProperty(String propertyName){
        return PROPERTY_LOG_OVERRIDES.get(propertyName);
    }
    
}
