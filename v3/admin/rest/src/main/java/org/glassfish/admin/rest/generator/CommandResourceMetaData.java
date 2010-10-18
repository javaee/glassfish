/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.admin.rest.generator;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Mitesh Meswani
 */
public class CommandResourceMetaData {
    public String command;
    public String httpMethod;
    public String resourcePath;
    public String displayName;
    public ParameterMetaData[] commandParams;

    public static class ParameterMetaData {
        String name;
        String value;
    }

    public static List<CommandResourceMetaData> getMetaData(String beanName) {
        //TODO need to read this from a file instead of from memory and then initialize data structure Map<String, List<CommandResourceMetaData> >
        List<CommandResourceMetaData> retVal = new LinkedList<CommandResourceMetaData>();
        for (String[] currentRow : configBeansToCommandResourcesMap) {
            if (beanName.equals(currentRow[0])) {
                CommandResourceMetaData metaData = new CommandResourceMetaData();
                metaData.command       = currentRow[1];
                metaData.httpMethod    = currentRow[2];
                metaData.resourcePath  = currentRow[3];
                metaData.displayName   = currentRow[4];
                // Each row has variable no of commandParams. If commandParams are present, extract them from current row and stuff into a String[]
                int PARAMETER_START_INDEX = 5;
                if(currentRow.length > PARAMETER_START_INDEX) {
                    metaData.commandParams = new ParameterMetaData[currentRow.length - PARAMETER_START_INDEX];
                    for(int i = PARAMETER_START_INDEX ; i < currentRow.length; i++) {
                        String[] nameValue = currentRow[i].split("=", 2); // The params are written as 'name=value', split them around "="
                        ParameterMetaData currentParam = new ParameterMetaData();
                        metaData.commandParams[i - PARAMETER_START_INDEX] = currentParam;
                        currentParam.name = nameValue[0];
                        currentParam.value = nameValue[1];
                    }
                }
                retVal.add(metaData);
            }
        }
        return retVal;
    }

    private static String configBeansToCommandResourcesMap[][] = {
            //{config-bean, command, method, resource-path, command-action, command-params...}
            {"Application", "disable", "POST", "disable", "Disable", "id=$parent"},
            {"Application", "enable", "POST", "enable", "Enable", "id=$parent"},
            {"Application", "show-component-status", "GET", "show-component-status", "Show Component Status", "id=$parent"},
            {"Application", "_get-deployment-configurations", "GET", "_get-deployment-configurations", "Get Deployment Configurations", "appname=$parent"},
            {"Application", "get-client-stubs", "GET", "get-client-stubs", "Get Client Stubs", "appname=$parent"},
            {"AuthRealm", "__list-group-names", "GET", "list-group-names", "List Group Names", "realmName=$parent"},
            {"AuthRealm", "__supports-user-management", "GET", "supports-user-management", "Check Support", "realmName=$parent"},
            {"AuthRealm", "create-file-user", "POST", "create-user", "Create", "authrealmname=$parent"},
            {"AuthRealm", "delete-file-user", "DELETE", "delete-user", "Delete", "authrealmname=$parent"},
            {"AuthRealm", "list-file-users", "GET", "list-users", "List Users", "authrealmname=$parent"},
            {"AuthRealm", "update-file-user", "POST", "update-user", "Update User", "authrealmname=$parent"},
            {"Cluster", "__get-jmsdest", "GET", "__get-jmsdest", "Get JMS Destination", "target=$parent"},
            {"Cluster", "__update-jmsdest", "POST", "__update-jmsdest", "Get JMS Destination", "target=$parent"},
            {"Cluster", "create-jmsdest", "POST", "create-jmsdest", "Create JMS Destination", "target=$parent"},
            {"Cluster", "create-lifecycle-module", "POST", "create-lifecycle-module", "Create Lifecycle Module", "target=$parent"},
            {"Cluster", "delete-cluster", "POST", "delete-cluster", "Delete Cluster", "id=$parent"},
            {"Cluster", "delete-jmsdest", "DELETE", "delete-jmsdest", "Delete JMS Destination", "target=$parent"},
            {"Cluster", "delete-lifecycle-module", "DELETE", "delete-lifecycle-module", "Delete Lifecycle Module", "target=$parent"},
            {"Cluster", "flush-jmsdest", "POST", "flush-jmsdest", "Flush", "target=$parent"},
            {"Cluster", "get-health", "GET", "get-health", "Get Health", "id=$parent"},
            {"Cluster", "list-instances", "GET", "list-instances", "List Cluster Instances", "id=$parent"},
            {"Cluster", "list-jmsdest", "GET", "list-jmsdest", "List JMS Destinations", "id=$parent"},
            {"Cluster", "list-lifecycle-modules", "GET", "list-lifecycle-modules", "List Lifecycle Modules", "id=$parent"},
            {"Cluster", "jms-ping", "GET", "jms-ping", "Ping JMS", "id=$parent"},
            {"Cluster", "migrate-timers", "POST", "migrate-timers", "Migrate Timers"},
            {"Cluster", "start-cluster", "POST", "start-cluster", "Start Cluster", "id=$parent"},
            {"Cluster", "stop-cluster", "POST", "stop-cluster", "Stop Cluster", "id=$parent"},
            {"Config", "delete-config", "POST", "delete-config", "Delete Config", "id=$parent"},
            {"Configs", "copy-config", "POST", "copy-config", "Copy Config"},
            {"ConnectionPool", "ping-connection-pool", "GET", "ping", "Ping"},
            {"Domain", "enable-monitoring", "POST", "enable-monitoring", "Enable Monitoring"},
            {"Domain", "disable-monitoring", "POST", "disable-monitoring", "Disable Monitoring"},
            {"Domain", "create-instance", "POST", "create-instance", "Create Instance"},
            {"Domain", "_get-host-and-port", "GET", "host-port", "HostPort"},
            {"Domain", "_get-restart-required", "GET", "_get-restart-required", "Restart Reasons"},
            {"Domain", "get", "POST", "get", "Get"},
           // {"Domain", "set", "POST", "set", "Set"},
            {"Domain", "generate-domain-schema", "POST", "generate-domain-schema", "Generate Domain Schema"},
            {"Domain", "list-log-levels", "GET", "list-log-levels", "LogLevels"},
            {"Domain", "list-instances", "GET", "list-instances", "List Instances"},
            {"Domain", "list-persistence-types", "GET", "list-persistence-types", "List Persistence Types"},
            {"Domain", "restart-domain", "POST", "restart", "Restart"},
            {"Domain", "rotate-log", "POST", "rotate-log", "RotateLog"},
            {"Domain", "set-log-levels", "POST", "set-log-levels", "LogLevel"},
            {"Domain", "stop-domain", "POST", "stop", "Stop"},          
            {"Domain", "uptime", "GET", "uptime", "Uptime"},
            {"Domain", "version", "GET", "version", "Version"},
            {"Domain", "_get-runtime-info", "GET", "get-runtime-info", "Get Runtime Info"},
            {"Domain", "__locations", "GET", "location", "Location"},
            {"IiopListener", "create-ssl", "POST", "create-ssl", "Create", "id=$parent", "type=iiop-listener"},
            {"IiopListener", "delete-ssl", "DELETE", "delete-ssl", "Delete", "id=$parent", "type=iiop-listener"},
            {"IiopService", "create-ssl", "POST", "create-ssl", "Create", "type=iiop-service"},
            {"IiopService", "delete-ssl", "DELETE", "delete-ssl", "Delete", "type=iiop-service"},
            {"JavaConfig", "create-profiler", "POST", "create-profiler", "Create Profiler"},
            {"JavaConfig", "generate-jvm-report", "POST", "generate-jvm-report", "Generate Report"},
            {"ListApplication", "_get-context-root", "GET", "get-context-root", "Get Context Root"},
            {"ListApplication", "_get-relative-jws-uri", "GET", "_get-relative-jws-uri", "Get Relative JWS URI" },
            {"ListApplication", "create-lifecycle-module", "POST", "create-lifecycle-module", "Create Lifecycle Module"},
            {"ListApplication", "delete-lifecycle-module", "DELETE", "delete-lifecycle-module", "Delete Lifecycle Module"},
            {"ListApplication", "list-components", "GET", "list-components", "List Components" },
            {"ListApplication", "list-lifecycle-modules", "GET", "list-lifecycle-modules", "List Lifecycle Modules"},
            {"ListApplication", "list-sub-components", "GET", "list-sub-components", "List Subcomponents"},
            {"ListApplication", "__list-webservices", "GET", "list-webservices", "List Webservices"},
            {"ListAuthRealm", "__list-predefined-authrealm-classnames", "GET", "list-predefined-authrealm-classnames", "List Auth Realms"},
            {"NetworkListener", "create-ssl", "POST", "create-ssl", "Create", "id=$parent", "type=http-listener"},
            {"NetworkListener", "delete-ssl", "DELETE", "delete-ssl", "Delete", "id=$parent", "type=http-listener"},
            {"Node", "delete-node-ssh", "DELETE", "delete-node", "Delete Node", "id=$parent"},
            {"Node", "ping-node-ssh", "GET", "ping-node-ssh", "Ping Node", "id=$parent"},
            {"Node", "update-node-ssh", "POST", "update-node-ssh", "Update Node", "id=$parent"},
            {"Node", "_update-node", "POST", "_update-node", "Update Node", "name=$parent"},
            {"Nodes", "create-node-ssh", "POST", "create-node", "Create Node"},
            {"Protocol", "create-http", "POST", "create-http", "Create", "id=$parent"},
            {"Protocol", "create-protocol-filter", "POST", "create-protocol-filter", "Create", "protocol=$parent"},
            {"Protocol", "delete-protocol-filter", "DELETE", "delete-protocol-filter", "Delete", "protocol=$parent"},
            {"Protocol", "create-protocol-finder", "POST", "create-protocol-finder", "Create", "protocol=$parent"},
            {"Protocol", "delete-protocol-finder", "DELETE", "delete-protocol-finder", "Delete", "protocol=$parent"},
            {"Protocol", "delete-http", "DELETE", "delete-http", "Delete", "id=$parent"},
            {"Resources", "_get-connection-definition-properties-and-defaults", "GET", "get-connection-definition-properties-and-defaults", "Get Connection Definition Properties And Defaults"},
            {"Resources", "_get-built-in-custom-resources", "GET", "get-built-in-custom-resources", "Get Built In Custom Resources"},
            {"Resources", "_get-system-rars-allowing-pool-creation", "GET", "get-system-rars-allowing-pool-creation", "Get System Rars Allowing Pool Creation"},
            {"Resources", "_get-connection-definition-names", "GET", "get-connection-definition-names", "Get Connection Definition Names"},
            {"Resources", "_get-mcf-config-properties", "GET", "get-mcf-config-properties", "Get Mcf Config Properties"},
            {"Resources", "_get-admin-object-interface-names", "GET", "get-admin-object-interface-names", "Get Admin Object Interface Names"},
            {"Resources", "_get-admin-object-class-names", "GET", "get-admin-object-class-names", "Get Admin Object Class Names"},
            {"Resources", "_get-resource-adapter-config-properties", "GET", "get-resource-adapter-config-properties", "Get Resource Adapter Config Properties"},
            {"Resources", "_get-admin-object-config-properties", "GET", "get-admin-object-config-properties", "Get Admin Object Config Properties"},
            {"Resources", "_get-connector-config-java-beans", "GET", "get-connector-config-java-beans", "Get Connector Config Java Beans"},
            {"Resources", "_get-activation-spec-class", "GET", "get-activation-spec-class", "Get Activation Spec Class"},
            {"Resources", "_get-message-listener-types", "GET", "get-message-listener-types", "Get Message Listener Types"},
            {"Resources", "_get-message-listener-config-properties", "GET", "get-message-listener-config-properties", "Get Message Listener Config Properties"},
            {"Resources", "_get-message-listener-config-property-types", "GET", "get-message-listener-config-property-types", "Get Message Listener Config Property Types"},
            {"Resources", "_get-validation-table-names", "GET", "get-validation-table-names", "Get Validation Table Names"},
            {"Resources", "_get-jdbc-driver-class-names", "GET", "get-jdbc-driver-class-names", "Get Jdbc Driver Class Names"},
            {"Resources", "_get-validation-class-names", "GET", "get-validation-class-names", "Get Validation Class Names"},
            {"Resources", "_get-database-vendor-names", "GET", "get-database-vendor-names", "Get Database Vendor Names"},
            {"Resources", "flush-connection-pool", "POST", "flush-connection-pool", "Flush Connection Pool"},
            {"Resources", "ping-connection-pool", "GET", "ping-connection-pool", "Ping Connection Pool"},
            {"SecurityService", "list-supported-cipher-suites", "GET", "list-supported-cipher-suites", "List Supported Cipher Suites"},
            {"Server", "__get-jmsdest", "GET", "__get-jmsdest", "Get JMS Destination", "target=$parent"},
            {"Server", "__update-jmsdest", "POST", "__update-jmsdest", "Get JMS Destination", "target=$parent"},
            {"Server", "create-jmsdest", "POST", "create-jmsdest", "Create JMS Destination", "target=$parent"},
            {"Server", "create-lifecycle-module", "POST", "create-lifecycle-module", "Create Lifecycle Module", "target=$parent"},
            {"Server", "delete-jmsdest", "DELETE", "delete-jmsdest", "Delete JMS Destination", "target=$parent"},
            {"Server", "delete-lifecycle-module", "DELETE", "delete-lifecycle-module", "Delete Lifecycle Module", "target=$parent"},
            {"Server", "list-jmsdest", "GET", "list-jmsdest", "List JMS Destinations", "id=$parent"},
            {"Server", "list-lifecycle-modules", "GET", "list-lifecycle-modules", "List Lifecycle Modules", "id=$parent"},
            {"Server", "flush-jmsdest", "POST", "flush-jmsdest", "Flush", "target=$parent"},
            {"Server", "jms-ping", "GET", "jms-ping", "Ping JMS", "id=$parent"},
            {"Server", "delete-instance", "DELETE", "delete-instance", "Delete Instance", "id=$parent"},
            {"Server", "start-instance", "POST", "start-instance", "Start Instance", "id=$parent"},
            {"Server", "restart-instance", "POST", "restart-instance", "Restart Instance", "id=$parent"},
            {"Server", "stop-instance", "POST", "stop-instance", "Stop Instance", "id=$parent"},
            {"Server", "recover-transactions", "POST", "recover-transactions", "Recover", "id=$parent"},
            {"WorkSecurityMap", "update-connector-work-security-map", "POST", "update-connector-work-security-map", "Update", "id=$parent"}
    };
}
