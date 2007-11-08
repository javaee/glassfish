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

/*
 * ConfigAttributeName.java
 *
 * Created on November 20, 2001, 9:43 AM
 */

package com.sun.enterprise.admin.common.constant;

/**
 *
 * @author  sirajg
 * @version
 */

public interface ConfigAttributeName
{
   public final String PROPERTY_NAME_PREFIX = "property.";
    interface Server
    {
        final String kName                      = "name";
//        final String kLocale                    = "locale";
        final String kLogRoot                   = "logRoot";
        final String kSessionStore              = "sessionStore";
        final String kApplicationRoot           = "applicationRoot";
        //the following attrs are from application sub element
        final String kAppDynamicReloadEnabled  = "appDynamicReloadEnabled";
        final String kAppReloadPollInterval    = "appReloadPollInterval";
    }

    interface EjbContainer
    {
        final String kMinBeansInPool                = "steadyPoolSize";
        final String kBeanIncrementCount            = "poolResizeQuantity";
        final String kMaxPoolSize                   = "maxPoolSize";
        final String kCacheResizeQuantity           = "cacheResizeQuantity";
        final String kMaxBeansInCache               = "maxCacheSize";
        final String kIdleInPoolTimeoutInSeconds    = "idleInPoolTimeoutInSeconds";
        final String kIdleInCacheTimeoutInSeconds   = "idleInCacheTimeoutInSeconds";
        final String kRemovalTimeoutInSeconds       = "removalTimeoutInSeconds";
        final String kVictimSelectionAlgorithm      = "victimSelectionPolicy";
        final String kCommitOption                  = "commitOption";
        final String kLogLevel                      = "logLevel";
        final String kMonitoringEnabled             = "monitoringEnabled";
    }

    interface WebContainer
    {
        final String kLogLevel                   = "logLevel";
        final String kMonitoringEnabled          = "monitoringEnabled";
    }

/*    interface EJB
    {
	final String kMinBeansInPool        = "minBeansInPool";
	final String kMaxBeansInPool        = "maxBeansInPool";
	final String kBeanIdleTimeInSec     = "beanIdleTimeInSeconds";
    }

    interface EntityBean extends EJB
    {
	final String kVictimSelectionAlgorithm	= "victimSelectionAlgorithm";
	final String kCommitOption              = "commitOption";
    }

    interface ReadOnlyEntityBean extends EntityBean
    {
	final String kIsReadOnly            = "isReadOnly";
	final String kRefreshPeriodicity    = "refreshPeriodicityInSeconds";
    }

    interface SessionBean extends EJB
    {
	final String kInitialBeansInPool = "initialBeansInPool";
    }

    interface StatelessSessionBean extends SessionBean
    {
    }

    interface StatefulSessionBean extends SessionBean
    {
	     final String kVictimSelectionAlgorithm	= "victimSelectionAlgorithm";
    }
*/
    interface JNDI //external JNDI resource
    {
       final String kJndiName       = "name";
       final String kJndiLookupName = "LookupName";
       final String kResType        = "resType";
       final String kFactoryClass   = "factory";
       final String kEnabled        = "enabled";
       final String kDescription    = "description";
    }

    interface CUSTOM_RESOURCE
    {
       final String kJndiName       = "name";
       final String kResType        = "resType";
       final String kFactoryClass   = "factory";
       final String kEnabled        = "enabled";
       final String kDescription    = "description";
    }
    interface ExternalJndiResource extends JNDI
    {
    }
    
    interface CustomResource extends CUSTOM_RESOURCE
    {
    }

/*    interface JCA
    {
    }
*/
    interface StartupClass  //???
    {
        final String kName              = "name";
        final String kClass             = "class";
        final String kInitParamName     = "initParamName";
        final String kInitParamValue    = "initParamValue";
    }

    interface OrbComponent
    {
        final String kMessageFragmentSize        = "msgSize";
        final String kMinThreads                 = "steadyThreadPoolSize";
        final String kMaxThreads                 = "maxThreadPoolSize";
        final String kMaxConnections             = "maxConnections";
        final String kThreadIdleTimeoutInSeconds = "idleThreadTimeout";
        final String kLogLevel                   = "log";
        final String kMonitoringEnabled          = "monitor";
    }

    interface ORBListener
    {
        final String kId                    = "id";
        final String kAddress               = "address";
        final String kPort                  = "port";
        final String kEnabled               = "enabled";
        //----- SSL --------
        //(Ssl attibutes will be appended here by merging with Ssl Interface - see Ssl interface in this file)
    }

    interface TransactionService
    {
        final String kAutomaticTransactionRecovery = "automaticTransactionRecovery";
        final String kTransactionRecoveryTimeout   = "transactionTimeout";
        final String kTransactionLogDir            = "transactionLogFile";
        final String kHeuristicDecision            = "heuristicDecision";
        final String kKeypointInterval             = "keypointInterval";
        final String kLogLevel                     = "logLevel";
        final String kMonitoringEnabled            = "monitoringEnabled";
        }

    interface LogService
    {
        final String kLogFile                = "file";
        final String kLogLevel               = "level";
        final String kLogStdout              = "stdout";
        final String kLogStderr              = "stderr";
        final String kEchoLogToStderr        = "echoToStderr";
        final String kCreateConsole          = "createConsole";
        final String kLogVirtualServerId     = "LogVirtualServerId";
        final String kUseSystemLogging       = "useSystemLogging";
    }

    interface MdbContainer
    {
        final String kMinBeansInPool                    = "steadyPoolSize";
        final String kBeanIncrementCount                = "poolResizeQuantity";
        final String kMaxPoolSize                       = "maxPoolSize";
        final String kIdleInPoolTimeoutInSeconds        = "idleInPoolTimeoutInSeconds";
        final String kLogLevel                          = "logLevel";
        final String kMonitoringEnabled                 = "monitoringEnabled";
    }


    interface JmsService
    {
        final String kPort                      = "port";
        final String kAdminUsername             = "adminUserName";
        final String kAdminPassword             = "adminPassword";
        final String kInitTimeout               = "initTimeoutInSeconds";
        final String kStartArgs                 = "startArgs";
        final String kLogLevel                  = "logLevel";
        final String kEnabled                   = "enabled";
    }

    interface JDBCResource
    {
        final String kJndiName                  = "name";
        final String kPoolName                  = "pool";
        final String kEnabled                   = "enabled";
        final String kDescription               = "description";
    }

    interface JDBCConnectionPool
    {
        final String kId                            = "name";
        final String kDatasourceClassName           = "dsClassName";
        final String kResType                       = "resType";
        final String kMinConnectionsInPool          = "steadyPoolSize";
        final String kMaxConnectionsInPool          = "maxPoolSize";
        final String kMaxConnectionsWaitTime        = "maxWaitTime";
        final String kConnectionsIncrement          = "resizeValue";
        final String kConnectionIdleTimeout         = "idleTimeout";
        final String kTransactionIsolationLevel     = "transactionIsolationLevel";
        final String kIsIsolationLevelGuaranteed    = "isIsolationLevelGuaranteed";
        final String kIsConnectionValidationRequired= "isValidationRequired";
        final String kConnectionValidation          = "validationMethod";
        final String kValidationTableName           = "validationTable";
        final String kFailAllConnections            = "failAll";
        final String kDescription                   = "description";

   }

    interface JavaConfig
    {
       final String kJavaHome                   = "javahome";
       final String kDebugEnabled               = "debugEnabled";
       final String kDebugOptions               = "debugOptions";
       final String kRmicOptions                = "rmicoptions";
       final String kJavacOptions               = "javacOptions";
       final String kClasspathPrefix            = "classpathprefix";
       final String kServerClasspath            = "serverClasspath";
       final String kClasspathSuffix            = "classpathsuffix";
       final String kNativeLibraryPathPrefix    = "libpathprefix";
       final String kNativeLibraryPathSuffix    = "libpathsuffix";
       final String kEnvClasspathIgnored        = "envpathignore";
    }

    interface Profiler
    {
       final String kName                   = "name";
       final String kClasspath              = "classpath";
       final String kNativeLibraryPath      = "nativeLibraryPath";
       final String kEnabled                = "enabled";
    }
    interface MailResource
    {
        final String kJndiName                  = "name";
        final String kEnabled                   = "enabled";
        final String kStoreProtocol             = "storeProtocol";
        final String kStoreProtocolClass        = "storeProtocolClass";
        final String kTransportProtocol         = "transportProtocol";
        final String kTransportProtocolClass    = "transportProtocolClass";
        final String kHost                      = "host";
        final String kUser                      = "user";
        final String kFrom                      = "from";
        final String kDebug                     = "debug";
        final String kDescription               = "description";
    }

    interface JMSResource
    {
        final String kJndiName          = "name";
        final String kResType           = "resType";
        final String kEnabled           = "enabled";
        final String kDescription       = "description";
    }

    interface PMFactoryResource
    {
        final String kJndiName                      = "jndiName";
        final String kFactoryClass                  = "factoryClass";
        final String kJdbcResourceJndiName          = "JdbcResourceJndiName";
        final String kEnabled                       = "enabled";
        final String kDescription                   = "description";
    }

    interface HTTPService
    {
        final String kQosMetricsInterval             = "qosMetricsIntervalInSeconds";
        final String kQosRecomputeInterval           = "qosRecomputeTimeIntervalInMillis";
        final String kQosEnabled                     = "qosEnabled";
        //-------- HTTPQos interface (see this file) attributes will be appended here
    }
    interface HTTPListener
    {
        final String kEnabled               = "enabled";
        final String kId                    = "id";
        final String kAddress               = "address";
        final String kPort                  = "port";
        final String kFamily                = "family";
        final String kAcceptorThreads       = "acceptorThreads";
        final String kBlockingEnabled       = "blockingEnabled";
        final String kSecurityEnabled       = "securityEnabled";
        final String kDefaultVirtualServer  = "defaultVirtualServer";
        final String kServerName            = "serverName";
        //----- SSL --------
        //(Ssl attibutes will be appended here by merging with Ssl Interface - see Ssl interface in this file)
    }

    interface ConnectionGroup
    {
        final String kId                    = "id";
        final String kAddress               = "address";
        final String kDefaultVirtualServer  = "defaultVirtualServer";
        final String kServerName            = "serverName";
        //----- SSL --------
        //(Ssl attibutes will be appended here by merging with Ssl Interface - see Ssl interface in this file)
    }

    interface SecurityService
    {
        final String kDefaultRealm              = "defaultRealm";
        final String kDefaultPrincipal          = "defaultPrincipal";
        final String kDefaultPrincipalPassword  = "defaultPrincipalPassword";
        final String kAnonymousRole             = "anonymousRole";
        final String kAuditEnabled              = "auditEnabled";
        final String kLogLevel                  = "logLevel";
    }

   interface J2EEApplication
    {
        final String kName                      = "name";
        final String kLocation                  = "location";
        final String kVirtualServers            = "virtualServers";
        final String kEnabled                   = "enabled";
        final String kDescription               = "description";
    }
   interface StandaloneWebModule
    {
        final String kName                      = "name";
        final String kContextRoot               = "contextRoot";
        final String kLocation                  = "location";
        final String kVirtualServers            = "virtualServers";
        final String kEnabled                   = "enabled";
        final String kDescription               = "description";
    }
   interface StandaloneEjbJarModule
    {
        final String kName                      = "name";
        final String kLocation                  = "location";
        final String kEnabled                   = "enabled";
        final String kDescription               = "description";
    }
   interface StandaloneConnectorModule
    {
        final String kName                      = "name";
        final String kLocation                  = "location";
        final String kEnabled                   = "enabled";
        final String kDescription               = "description";
    }
    interface Mime
    {
        final String kId                   = "id";
        final String kFile                 = "file";
    }
    interface Acl
    {
        final String kId                   = "id";
        final String kFile                 = "file";
    }
    interface AuthDb
    {
        final String kId            = "id";
        final String kDataBase      = "database";
        final String kBaseDN        = "basedn";
        final String kCertMaps      = "certmaps";
    }
    interface AuthRealm
    {
        final String kName           = "name";
        final String kClassName      = "classname";
    }
    interface VirtualServerClass
    {
        final String kId                = "id";
        final String kConfigFile        = "configFile";
        final String kDefaultObject     = "defaultObject";
        final String kAcceptLanguage    = "acceptLanguage";
        final String kEnabled           = "enabled";
        //-------- HTTPQos interface (see this file) attributes will be appended here
    }
    interface VirtualServer
    {
        final String kId                = "id";
        final String kHttpListeners     = "httpListeners";
        final String kDefaultWebModule  = "defaultWebModule";
        final String kConfigFile        = "configFile";
        final String kDefaultObject     = "defaultObject";
        final String kHosts             = "hosts";
        final String kMime              = "mime";
        final String kState             = "state";
        final String kAcls              = "acls";
        final String kAcceptLanguage    = "accepotLanguage";
        final String kLogFile           = "logFile";
        // properties - pseudoattributes
/*        final String kPropDir           = "property.dir";
        final String kPropNice          = "property.nice";
        final String kPropUser          = "property.user";
        final String kPropGroup         = "property.group";
        final String kPropChRoot        = "property.chroot";
        final String kPropDocRoot       = "property.docroot";
        final String kPropAccessLog     = "property.accesslog";
*/
 //-------- HTTPQos interface (see this file) attributes will be appended here
    }

    interface HTTPQos
    {
        final String kBandwidthLimit           = "bandwidthLimit";
        final String kEnforceBandwidthLimit    = "enforceBandwidthLimit";
        final String kConnectionLimit          = "connectionLimit";
        final String kEnforceConnectionLimit   = "enforceConnectionLimit";
    }

    interface Ssl
    {
        final String kCertNickname       = "cert";
        final String kSsl2Enabled        = "ssl2";
        final String kSsl2Ciphers        = "ssl2Ciphers";
        final String kSsl3Enabled        = "ssl3";
        final String kSsl3TlsCiphers     = "ssl3Ciphers";
        final String kTlsEnabled         = "tls";
        final String kTlsRollbackEnabled = "tlsRollback";
        final String kClientAuthEnabled  = "clientAuth";
    }

    interface LifecycleModule
    {
        final String kName            = "name";
        final String kEnabled         = "enabled";
        final String kClassName       = "className";
        final String kClasspath       = "classPath";
        final String kLoadOrder       = "loadOrder";
        final String kIsFailureFatal  = "isFailureFatal";
        final String kDescription     = "description";
    }


}
