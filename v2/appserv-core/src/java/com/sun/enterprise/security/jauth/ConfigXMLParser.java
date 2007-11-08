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

package com.sun.enterprise.security.jauth;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import javax.security.auth.login.AppConfigurationEntry;

import sun.security.util.Debug;
import sun.security.util.PropertyExpander;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.clientbeans.ClientBeansResolver;
import com.sun.enterprise.config.clientbeans.ClientContainer;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.Switch;

import com.sun.logging.*;
import java.util.logging.*;

/**
 * Parser for message-security-config in domain.xml or sun-acc.xml
 */
class ConfigXMLParser implements ConfigParser {

    private static Logger _logger=null;
    static {
        _logger = LogDomains.getLogger(LogDomains.SECURITY_LOGGER);
    }

    // configuration info
    private HashMap configMap;

    // system property guaranteed to be set in appclient/Main.java,
    // and is guaranteed to be non-null
    private static final String SUNACC_XML_URL = "sun-acc.xml.url";

    private static final Debug debug =
		Debug.getInstance("configxmlparser", "[ConfigXMLParser]");

    ConfigXMLParser() throws IOException {

	// first read the module config from message-security-config

	HashMap newConfig = new HashMap();

	if (Switch.getSwitch().getContainerType() == Switch.EJBWEB_CONTAINER) {
	    readDomainXML(newConfig);
	} else {
	    // container == Switch.APPCLIENT_CONTAINER
	    readSunAccXML(newConfig);
	}
	configMap = newConfig;
    }

    private static void readDomainXML(HashMap newConfig) throws IOException {

	// auth-layer
	String intercept = null;

	try {
	    ConfigContext configCtx = 
                ApplicationServer.getServerContext().getConfigContext();

	    if (configCtx == null) {
		return;
	    }

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
	    ioe.initCause(ce);
	    throw ioe;
	}
    }

    private static void readSunAccXML(HashMap newConfig) throws IOException {

	// auth-layer
	String intercept = null;

	try {
	    // ConfigContext configCtx = ConfigFactory.createConfigContext(url);

	    ConfigContext configCtx = ConfigFactory.createConfigContext
		                       (System.getProperty(SUNACC_XML_URL),
					true,
					false,
					false,
					ClientContainer.class,
					new ClientBeansResolver());
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

    public HashMap getConfigMap() {
	return configMap;
    }

    /**
     * XXX must duplicate for client and server side code
     *     because MessageSecurityConfig and subelements
     *     are in different packages
     */

    /**
     * XXX server-side XML duplicate methods
     */

    private static String parseInterceptEntry
	(com.sun.enterprise.config.serverbeans.MessageSecurityConfig msgConfig,
	HashMap newConfig)
		throws IOException {

	String intercept = msgConfig.getAuthLayer();
	String defaultServerID = msgConfig.getDefaultProvider();
	String defaultClientID = msgConfig.getDefaultClientProvider();

	if (debug != null) {
	    debug.println("Intercept Entry: " +
			"\n    intercept: " + intercept +
			"\n    defaultServerID: " + defaultServerID +
			"\n    defaultClientID:  " + defaultClientID);
	}

	ConfigFile.InterceptEntry intEntry = (ConfigFile.InterceptEntry)
						newConfig.get(intercept);
	if (intEntry != null) {
	    throw new IOException("found multiple MessageSecurityConfig " +
				"entries with the same auth-layer");
	}

	// create new intercept entry
	intEntry = new ConfigFile.InterceptEntry(defaultClientID,
					defaultServerID,
					null);
	newConfig.put(intercept, intEntry);
	return intercept;
    }

    private static void parseIDEntry
	(com.sun.enterprise.config.serverbeans.ProviderConfig pConfig,
	HashMap newConfig,
	String intercept)
		throws IOException {

	String id = pConfig.getProviderId();
	String type = pConfig.getProviderType();
	String moduleClass = pConfig.getClassName();
	ArrayList modules = new ArrayList();

	AuthPolicy requestPolicy =
		parseRequestPolicy(pConfig.getRequestPolicy());
	AuthPolicy responsePolicy =
		parseResponsePolicy(pConfig.getResponsePolicy());

	// get the module options

	HashMap options = new HashMap();
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
		_logger.warning("Container-auth: unable to expand provider property value - unexpanded value passed to provider");
		options.put(pConfig.getElementProperty(i).getName(),
			    pConfig.getElementProperty(i).getValue());
	    }
	}

	if (debug != null) {
	    debug.println("ID Entry: " +
			"\n    id: " + id +
			"\n    type: " + type +
			"\n    request policy: " + requestPolicy +
			"\n    response policy: " + responsePolicy +
			"\n    module class: " + moduleClass +
			"\n        options: " + options);
	}

	// create module entry

	AppConfigurationEntry entry = new AppConfigurationEntry
			(pConfig.getClassName(),
			AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
			options);
	modules.add(entry);

	// create ID entry

	ConfigFile.IDEntry idEntry = new ConfigFile.IDEntry(type,
				requestPolicy,
				responsePolicy,
				modules);

	ConfigFile.InterceptEntry intEntry = (ConfigFile.InterceptEntry)
					newConfig.get(intercept);
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

    private static AuthPolicy parseRequestPolicy
	(com.sun.enterprise.config.serverbeans.RequestPolicy policy) {

	// XXX identical source as parseResponsePolicy

	if (policy == null) {
	    return null;
	}

	int sourceAuthType = AuthPolicy.SOURCE_AUTH_NONE;
	boolean foundSource = true;
	String authType = policy.getAuthSource();

	if (AuthPolicy.SENDER.equals(authType)) {
	    sourceAuthType = AuthPolicy.SOURCE_AUTH_SENDER;
	} else if (AuthPolicy.CONTENT.equals(authType)) {
	    sourceAuthType = AuthPolicy.SOURCE_AUTH_CONTENT;
	} else {
	    if (debug != null) {
		debug.println("invalid or null auth source: " + authType);
	    }
	    foundSource = false;
	}
	
	boolean recipientAuth = false;
	boolean beforeContent = false;
	boolean foundRecipient = true;
	String recipient = policy.getAuthRecipient();

	if (AuthPolicy.BEFORE_CONTENT.equals(recipient)) {
	    recipientAuth = true;
	    beforeContent = true;
	} else if (AuthPolicy.AFTER_CONTENT.equals(recipient)) {
	    recipientAuth = true;
	    beforeContent = false;
	} else {
	    if (debug != null) {
		debug.println("invalid or null auth recipient: " + recipient);
	    }
	    foundRecipient = false;
	}

	if (!foundSource && !foundRecipient) {
	    return null;
	}

	return new AuthPolicy(sourceAuthType,
				recipientAuth,
				beforeContent);
    }

    private static AuthPolicy parseResponsePolicy
	(com.sun.enterprise.config.serverbeans.ResponsePolicy policy) {

	// XXX identical source as parseRequestPolicy

	if (policy == null) {
	    return null;
	}

	int sourceAuthType = AuthPolicy.SOURCE_AUTH_NONE;
	boolean foundSource = true;
	String authType = policy.getAuthSource();

	if (AuthPolicy.SENDER.equals(authType)) {
	    sourceAuthType = AuthPolicy.SOURCE_AUTH_SENDER;
	} else if (AuthPolicy.CONTENT.equals(authType)) {
	    sourceAuthType = AuthPolicy.SOURCE_AUTH_CONTENT;
	} else {
	    if (debug != null) {
		debug.println("invalid or null auth source: " + authType);
	    }
	    foundSource = false;
	}

	boolean recipientAuth = false;
	boolean beforeContent = false;
	boolean foundRecipient = true;
	String recipient = policy.getAuthRecipient();

	if (AuthPolicy.BEFORE_CONTENT.equals(recipient)) {
	    recipientAuth = true;
	    beforeContent = true;
	} else if (AuthPolicy.AFTER_CONTENT.equals(recipient)) {
	    recipientAuth = true;
	    beforeContent = false;
	} else {
	    if (debug != null) {
		debug.println("invalid or null auth recipient: " + recipient);
	    }
	    foundRecipient = false;
	}

	if (!foundSource && !foundRecipient) {
	    return null;
	}

	return new AuthPolicy(sourceAuthType,
				recipientAuth,
				beforeContent);
    }

    // XXX client-side XML duplicate methods

    private static String parseInterceptEntry
	(com.sun.enterprise.config.clientbeans.MessageSecurityConfig msgConfig,
	HashMap newConfig)
		throws IOException {

	String intercept = msgConfig.getAuthLayer();
	String defaultServerID = msgConfig.getDefaultProvider();
	String defaultClientID = msgConfig.getDefaultClientProvider();

	if (debug != null) {
	    debug.println("Intercept Entry: " +
			"\n    intercept: " + intercept +
			"\n    defaultServerID: " + defaultServerID +
			"\n    defaultClientID:  " + defaultClientID);
	}

	ConfigFile.InterceptEntry intEntry = (ConfigFile.InterceptEntry)
						newConfig.get(intercept);
	if (intEntry != null) {
	    throw new IOException("found multiple MessageSecurityConfig " +
				"entries with the same auth-layer");
	}

	// create new intercept entry
	intEntry = new ConfigFile.InterceptEntry(defaultClientID,
					defaultServerID,
					null);
	newConfig.put(intercept, intEntry);
	return intercept;
    }

    private static void parseIDEntry
	(com.sun.enterprise.config.clientbeans.ProviderConfig pConfig,
	HashMap newConfig,
	String intercept)
		throws IOException {

	String id = pConfig.getProviderId();
	String type = pConfig.getProviderType();
	String moduleClass = pConfig.getClassName();
	ArrayList modules = new ArrayList();

	AuthPolicy requestPolicy =
		parseRequestPolicy(pConfig.getRequestPolicy());
	AuthPolicy responsePolicy =
		parseResponsePolicy(pConfig.getResponsePolicy());

	// get the module options

	HashMap options = new HashMap();
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
		_logger.warning("Container-auth: unable to expand provider property value - unexpanded value passed to provider");
		options.put(pConfig.getElementProperty(i).getName(),
			    pConfig.getElementProperty(i).getValue());
	    }
	}

	if (debug != null) {
	    debug.println("ID Entry: " +
			"\n    id: " + id +
			"\n    type: " + type +
			"\n    request policy: " + requestPolicy +
			"\n    response policy: " + responsePolicy +
			"\n    module class: " + moduleClass +
			"\n        options: " + options);
	}

	// create module entry

	AppConfigurationEntry entry = new AppConfigurationEntry
			(pConfig.getClassName(),
			AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
			options);
	modules.add(entry);

	// create ID entry

	ConfigFile.IDEntry idEntry = new ConfigFile.IDEntry(type,
				requestPolicy,
				responsePolicy,
				modules);

	ConfigFile.InterceptEntry intEntry = (ConfigFile.InterceptEntry)
					newConfig.get(intercept);
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

    private static AuthPolicy parseRequestPolicy
	(com.sun.enterprise.config.clientbeans.RequestPolicy policy) {

	if (policy == null) {
	    return null;
	}

	int sourceAuthType = AuthPolicy.SOURCE_AUTH_NONE;
	boolean foundSource = true;
	String authType = policy.getAuthSource();

	if (AuthPolicy.SENDER.equals(authType)) {
	    sourceAuthType = AuthPolicy.SOURCE_AUTH_SENDER;
	} else if (AuthPolicy.CONTENT.equals(authType)) {
	    sourceAuthType = AuthPolicy.SOURCE_AUTH_CONTENT;
	} else {
	    if (debug != null) {
		debug.println("invalid or null auth source: " + authType);
	    }
	    foundSource = false;
	}

	boolean recipientAuth = false;
	boolean beforeContent = false;
	boolean foundRecipient = true;
	String recipient = policy.getAuthRecipient();

	if (AuthPolicy.BEFORE_CONTENT.equals(recipient)) {
	    recipientAuth = true;
	    beforeContent = true;
	} else if (AuthPolicy.AFTER_CONTENT.equals(recipient)) {
	    recipientAuth = true;
	    beforeContent = false;
	} else {
	    if (debug != null) {
		debug.println("invalid or null auth recipient: " + recipient);
	    }
	    foundRecipient = false;
	}

	if (!foundSource && !foundRecipient) {
	    return null;
	}

	return new AuthPolicy(sourceAuthType,
				recipientAuth,
				beforeContent);
    }

    private static AuthPolicy parseResponsePolicy
	(com.sun.enterprise.config.clientbeans.ResponsePolicy policy) {

	if (policy == null) {
	    return null;
	}

	int sourceAuthType = AuthPolicy.SOURCE_AUTH_NONE;
	boolean foundSource = true;
	String authType = policy.getAuthSource();

	if (AuthPolicy.SENDER.equals(authType)) {
	    sourceAuthType = AuthPolicy.SOURCE_AUTH_SENDER;
	} else if (AuthPolicy.CONTENT.equals(authType)) {
	    sourceAuthType = AuthPolicy.SOURCE_AUTH_CONTENT;
	} else {
	    if (debug != null) {
		debug.println("invalid or null auth source: " + authType);
	    }
	    foundSource = false;
	}

	boolean recipientAuth = false;
	boolean beforeContent = false;
	boolean foundRecipient = true;
	String recipient = policy.getAuthRecipient();

	if (AuthPolicy.BEFORE_CONTENT.equals(recipient)) {
	    recipientAuth = true;
	    beforeContent = true;
	} else if (AuthPolicy.AFTER_CONTENT.equals(recipient)) {
	    recipientAuth = true;
	    beforeContent = false;
	} else {
	    if (debug != null) {
		debug.println("invalid or null auth recipient: " + recipient);
	    }
	    foundRecipient = false;
	}

	if (!foundSource && !foundRecipient) {
	    return null;
	}

	return new AuthPolicy(sourceAuthType,
				recipientAuth,
				beforeContent);
    }
}
