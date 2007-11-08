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

package com.sun.enterprise.security.jmac.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import javax.security.auth.message.MessagePolicy;
import static javax.security.auth.message.MessagePolicy.*;

import sun.security.util.PropertyExpander;

import com.sun.enterprise.Switch;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.clientbeans.ClientBeansResolver;
import com.sun.enterprise.config.clientbeans.ClientContainer;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.security.jmac.AuthMessagePolicy;
import com.sun.enterprise.server.ApplicationServer;

import com.sun.logging.LogDomains;

/**
 * Parser for message-security-config in domain.xml or sun-acc.xml
 */
public class ConfigXMLParser implements ConfigParser { 
    private static Logger _logger=null;
    static {
        _logger = LogDomains.getLogger(LogDomains.SECURITY_LOGGER);
    }

    // configuration info
    private Map configMap = new HashMap();
    private Set<String> layersWithDefault = new HashSet<String>();

    // system property guaranteed to be set in appclient/Main.java,
    // and is guaranteed to be non-null
    private static final String SUNACC_XML_URL = "sun-acc.xml.url";

    ConfigXMLParser() throws IOException {
    }

    public void initialize(ConfigContext configCtx) throws IOException {
        boolean isAppClientContainer =
            (Switch.getSwitch().getContainerType() ==
            Switch.APPCLIENT_CONTAINER);
        if (configCtx == null) {
            if (isAppClientContainer) {
                try {
                    configCtx = ConfigFactory.createConfigContext
                        (System.getProperty(SUNACC_XML_URL),
                        true, false, false, ClientContainer.class,
                        new ClientBeansResolver());
                } catch(ConfigException cex) {
                    IOException iex = new IOException();
                    iex.initCause(cex);
                    throw iex;
                }
            } else { // container == Switch.EJBWEB_CONTAINER
                configCtx =
                    ApplicationServer.getServerContext().getConfigContext();
            }

            if (configCtx == null) {
                return;
            }
        }
        if (isAppClientContainer) {
            processClientConfigContext(configCtx, configMap);
        } else {
            processServerConfigContext(configCtx, configMap);
        }
    }

    private void processServerConfigContext(ConfigContext configCtx,
            Map newConfig) throws IOException {

        // auth-layer
        String intercept = null;

        try {
            Server configBean = ServerBeansFactory.getServerBean(configCtx);
            SecurityService secService =
                ServerBeansFactory.getSecurityServiceBean(configCtx);

            com.sun.enterprise.config.serverbeans.MessageSecurityConfig[]
                msgConfigs = secService.getMessageSecurityConfig();

            for (int j = 0; msgConfigs != null &&
                    j < msgConfigs.length; j++) {

                // single message-security-config for each auth-layer
                //
                // auth-layer is synonymous with intercept

                intercept = parseInterceptEntry(msgConfigs[j], newConfig);
                com.sun.enterprise.config.serverbeans.ProviderConfig[]
                        pConfigs = msgConfigs[j].getProviderConfig();

                for (int k = 0; pConfigs != null &&
                        k < pConfigs.length; k++) {
                    parseIDEntry(pConfigs[k], newConfig, intercept);
                }
            }
        } catch (ConfigException ce) {
            IOException ioe = new IOException();
            ioe.initCause(ce); throw ioe;
        }
    }

    private void processClientConfigContext(
            ConfigContext configCtx, Map newConfig) throws IOException {

        // auth-layer
        String intercept = null;

        try {
            ClientContainer cc = (ClientContainer)configCtx.getRootConfigBean();
            com.sun.enterprise.config.clientbeans.MessageSecurityConfig[]
                msgConfigs = cc.getMessageSecurityConfig();

            for (int j = 0; msgConfigs != null && j < msgConfigs.length; j++) {

                // single message-security-config for each auth-layer
                //
                // auth-layer is synonymous with intercept

                intercept = parseInterceptEntry(msgConfigs[j], newConfig);
                com.sun.enterprise.config.clientbeans.ProviderConfig[]
                        pConfigs = msgConfigs[j].getProviderConfig();

                for (int k = 0; pConfigs != null && k < pConfigs.length; k++) {
                     parseIDEntry(pConfigs[k], newConfig, intercept);
                }
            }
        } catch (ConfigException ce) {
            IOException ioe = new IOException();
            ioe.initCause(ce);
            throw ioe;
        }
    }

    public Map getConfigMap() {
        return configMap;
    }

    public Set<String> getLayersWithDefault() {
        return layersWithDefault;
    }

    private String parseInterceptEntry(
            ConfigBean msgConfig, Map newConfig) throws IOException {

        String intercept = null;
        String defaultServerID = null;
        String defaultClientID = null;

        if (msgConfig instanceof com.sun.enterprise.config.serverbeans.MessageSecurityConfig) {
            com.sun.enterprise.config.serverbeans.MessageSecurityConfig serverMsgSecConfig = (com.sun.enterprise.config.serverbeans.MessageSecurityConfig)msgConfig;
            intercept = serverMsgSecConfig.getAuthLayer();
            defaultServerID = serverMsgSecConfig.getDefaultProvider();
            defaultClientID = serverMsgSecConfig.getDefaultClientProvider();
        } else if (msgConfig instanceof com.sun.enterprise.config.clientbeans.MessageSecurityConfig) {
            com.sun.enterprise.config.clientbeans.MessageSecurityConfig clientMsgSecConfig = (com.sun.enterprise.config.clientbeans.MessageSecurityConfig)msgConfig;
            intercept = clientMsgSecConfig.getAuthLayer();
            defaultServerID = clientMsgSecConfig.getDefaultProvider();
            defaultClientID = clientMsgSecConfig.getDefaultClientProvider();
        } 

        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("Intercept Entry: " +
                        "\n    intercept: " + intercept +
                        "\n    defaultServerID: " + defaultServerID +
                        "\n    defaultClientID:  " + defaultClientID);
        }

        if (defaultServerID != null || defaultClientID != null) {
            layersWithDefault.add(intercept);
        }

        GFServerConfigProvider.InterceptEntry intEntry =
            (GFServerConfigProvider.InterceptEntry)newConfig.get(intercept);
        if (intEntry != null) {
            throw new IOException("found multiple MessageSecurityConfig " +
                                "entries with the same auth-layer");
        }

        // create new intercept entry
        intEntry = new GFServerConfigProvider.InterceptEntry(defaultClientID,
                defaultServerID, null);
        newConfig.put(intercept, intEntry);
        return intercept;
    }

    // duplicate implementation for clientbeans config
    private void parseIDEntry(
            com.sun.enterprise.config.clientbeans.ProviderConfig pConfig,
            Map newConfig, String intercept)
            throws IOException {

        String id = pConfig.getProviderId();
        String type = pConfig.getProviderType();
        String moduleClass = pConfig.getClassName();
        MessagePolicy requestPolicy = parsePolicy(pConfig.getRequestPolicy());
        MessagePolicy responsePolicy = parsePolicy(pConfig.getResponsePolicy());

        // get the module options

        Map options = new HashMap();
        String key;
        String value;

        for (int i = 0; i < pConfig.sizeElementProperty(); i++) {
            try {
                options.put(pConfig.getElementProperty(i).getName(),
                            PropertyExpander.expand
                            (pConfig.getElementProperty(i).getValue(),
                             false));
            } catch (sun.security.util.PropertyExpander.ExpandException ee) {
                // log warning and give the provider a chance to 
                // interpret value itself.
                if (_logger.isLoggable(Level.WARNING)) {
                    _logger.warning("jmac.unexpandedproperty");
                }
                options.put(pConfig.getElementProperty(i).getName(),
                            pConfig.getElementProperty(i).getValue());
            }
        }

        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("ID Entry: " +
                        "\n    module class: " + moduleClass +
                        "\n    id: " + id +
                        "\n    type: " + type +
                        "\n    request policy: " + requestPolicy +
                        "\n    response policy: " + responsePolicy +
                        "\n    options: " + options);
        }

        // create ID entry

        GFServerConfigProvider.IDEntry idEntry =
                new GFServerConfigProvider.IDEntry(type, moduleClass,
                requestPolicy, responsePolicy, options);

        GFServerConfigProvider.InterceptEntry intEntry =
                (GFServerConfigProvider.InterceptEntry)newConfig.get(intercept);
        if (intEntry == null) {
            throw new IOException
                ("intercept entry for " + intercept +
                " must be specified before ID entries");
        }

        if (intEntry.idMap == null) {
            intEntry.idMap = new HashMap();
        }

        // map id to Intercept
        intEntry.idMap.put(id, idEntry);
    }

    private void parseIDEntry(
            com.sun.enterprise.config.serverbeans.ProviderConfig pConfig,
            Map newConfig, String intercept)
            throws IOException {

        String id = pConfig.getProviderId();
        String type = pConfig.getProviderType();
        String moduleClass = pConfig.getClassName();
        MessagePolicy requestPolicy = parsePolicy(pConfig.getRequestPolicy());
        MessagePolicy responsePolicy = parsePolicy(pConfig.getResponsePolicy());

        // get the module options

        Map options = new HashMap();
        String key;
        String value;

        for (int i = 0; i < pConfig.sizeElementProperty(); i++) {
            try {
                options.put(pConfig.getElementProperty(i).getName(),
                            PropertyExpander.expand
                            (pConfig.getElementProperty(i).getValue(),
                             false));
            } catch (sun.security.util.PropertyExpander.ExpandException ee) {
                // log warning and give the provider a chance to 
                // interpret value itself.
                if (_logger.isLoggable(Level.WARNING)) {
                    _logger.warning("jmac.unexpandedproperty");
                }
                options.put(pConfig.getElementProperty(i).getName(),
                            pConfig.getElementProperty(i).getValue());
            }
        }

        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("ID Entry: " +
                        "\n    module class: " + moduleClass +
                        "\n    id: " + id +
                        "\n    type: " + type +
                        "\n    request policy: " + requestPolicy +
                        "\n    response policy: " + responsePolicy +
                        "\n    options: " + options);
        }

        // create ID entry

        GFServerConfigProvider.IDEntry idEntry =
                new GFServerConfigProvider.IDEntry(type, moduleClass,
                requestPolicy, responsePolicy, options);

        GFServerConfigProvider.InterceptEntry intEntry =
                (GFServerConfigProvider.InterceptEntry)newConfig.get(intercept);
        if (intEntry == null) {
            throw new IOException
                ("intercept entry for " + intercept +
                " must be specified before ID entries");
        }

        if (intEntry.idMap == null) {
            intEntry.idMap = new HashMap();
        }

        // map id to Intercept
        intEntry.idMap.put(id, idEntry);
    }

    private MessagePolicy parsePolicy(ConfigBean policy) {

        if (policy == null) {
            return null;
        }

        String authSource = null;
        String authRecipient = null;

        if (policy instanceof
                com.sun.enterprise.config.serverbeans.RequestPolicy) {
            com.sun.enterprise.config.serverbeans.RequestPolicy serverRequestPolicy = (com.sun.enterprise.config.serverbeans.RequestPolicy)policy;
            authSource = serverRequestPolicy.getAuthSource();
            authRecipient = serverRequestPolicy.getAuthRecipient();
        } else if (policy instanceof
               com.sun.enterprise.config.serverbeans.ResponsePolicy) {
            com.sun.enterprise.config.serverbeans.ResponsePolicy serverResponsePolicy = (com.sun.enterprise.config.serverbeans.ResponsePolicy)policy;
            authSource = serverResponsePolicy.getAuthSource();
            authRecipient = serverResponsePolicy.getAuthRecipient();
        } else if (policy instanceof
                com.sun.enterprise.config.clientbeans.RequestPolicy) {
            com.sun.enterprise.config.clientbeans.RequestPolicy clientRequestPolicy = (com.sun.enterprise.config.clientbeans.RequestPolicy)policy;
            authSource = clientRequestPolicy.getAuthSource();
            authRecipient = clientRequestPolicy.getAuthRecipient();
        } else if (policy instanceof
                com.sun.enterprise.config.clientbeans.ResponsePolicy) {
            com.sun.enterprise.config.clientbeans.ResponsePolicy clientResponsePolicy = (com.sun.enterprise.config.clientbeans.ResponsePolicy)policy;
            authSource = clientResponsePolicy.getAuthSource();
            authRecipient = clientResponsePolicy.getAuthRecipient();
        }

        return AuthMessagePolicy.getMessagePolicy(authSource, authRecipient);
    }
}
