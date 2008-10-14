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
 * ServerHandlers.java
 *
 * Created on July 20, 2006, 1:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.glassfish.admingui.handlers;

import com.sun.appserv.management.base.XTypes;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.Vector;

import javax.faces.model.SelectItem;


import org.glassfish.admingui.common.util.AMXRoot;
import org.glassfish.admingui.common.util.GuiUtil;

import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.JavaConfig;
import com.sun.appserv.management.config.LogServiceConfig;
import com.sun.appserv.management.config.ProfilerConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.ModuleLogLevelsConfig;
import com.sun.appserv.management.config.DASConfig;
import com.sun.appserv.management.config.ProfilerConfigKeys;
import org.glassfish.admingui.common.util.AMXUtil;

/**
 *
 * @author Administrator
 */
public class ServerHandlers {

    /**
     *	<p> This handler returns the values for all the attributes in the
     *      Server Domain Attributes Page.</p>
     *	<p> Output value: "AppRoot" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "LogRoot" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Locale" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getServerDomainAttributes", output = {
        @HandlerOutput(name = "AppRoot", type = String.class),
        @HandlerOutput(name = "LogRoot", type = String.class),
        @HandlerOutput(name = "Locale", type = String.class)
    })
    public static void getServerDomainAttributes(HandlerContext handlerCtx) {

        DomainConfig domainConfig = AMXRoot.getInstance().getDomainConfig();
        String appRoot = domainConfig.getApplicationRoot();
        String logRoot = domainConfig.getLogRoot();
        String locale = domainConfig.getLocale();
        handlerCtx.setOutputValue("AppRoot", appRoot);
        handlerCtx.setOutputValue("LogRoot", logRoot);
        handlerCtx.setOutputValue("Locale", locale);

    }

    /**
     *	<p> This method saves the attributes on the
     *      Server Domain Attributes Page.</p>
     *	<p> Input value: "AppRoot" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "LogRoot" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Locale" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "saveServerDomainAttributes", input = {
        @HandlerInput(name = "AppRoot", type = String.class),
        @HandlerInput(name = "LogRoot", type = String.class),
        @HandlerInput(name = "Locale", type = String.class)
    })
    public static void saveServerDomainAttributes(HandlerContext handlerCtx) {

        DomainConfig domainConfig = AMXRoot.getInstance().getDomainConfig();

        domainConfig.setApplicationRoot((String) handlerCtx.getInputValue("AppRoot"));
        domainConfig.setLogRoot((String) handlerCtx.getInputValue("LogRoot"));
        domainConfig.setLocale((String) handlerCtx.getInputValue("Locale"));

    }

    /**
     *	<p> This handler returns the values for all the attributes in the
     *      Server Applications Config Page.</p>
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Reload" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "ReloadInterval" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AutoDeploy" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "AdminTimeout" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AutoDeployInterval" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AutoDeployTimeout" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Verifier" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Precompile" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "AutoDeployDirectory" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Properties" -- Type: <code>java.util.Map</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getServerAppsConfigAttributes", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true)
    }, output = {
        @HandlerOutput(name = "Reload", type = Boolean.class),
        @HandlerOutput(name = "ReloadInterval", type = String.class),
        @HandlerOutput(name = "AutoDeploy", type = Boolean.class),
        @HandlerOutput(name = "AdminTimeout", type = String.class),
        @HandlerOutput(name = "AutoDeployInterval", type = String.class),
        @HandlerOutput(name = "AutoDeployTimeout", type = String.class),
        @HandlerOutput(name = "Verifier", type = String.class),
        @HandlerOutput(name = "Precompile", type = String.class),
        @HandlerOutput(name = "AutoDeployDirectory", type = String.class),
        @HandlerOutput(name = "Properties", type = Map.class)
    })
    public static void getServerAppsConfigAttributes(HandlerContext handlerCtx) {

        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        DASConfig dConfig = config.getAdminServiceConfig().getDASConfig();
        String reload = dConfig.getDynamicReloadEnabled();
        String reloadInterval = dConfig.getDynamicReloadPollIntervalInSeconds();
        String autoDeploy = dConfig.getAutodeployEnabled();
        //refer to issue# 5698 and issue# 3691
        String adminTimeout = "";
        try {
            adminTimeout = dConfig.getAdminSessionTimeoutInMinutes();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        String autoDeployInterval = dConfig.getAutodeployPollingIntervalInSeconds();
        String autoDeployTimeout = dConfig.getAutodeployRetryTimeout();
        String autoDeployDirectory = dConfig.getAutodeployDir();
        String precompile = dConfig.getAutodeployJSPPrecompilationEnabled();
        String verifier = dConfig.getAutodeployVerifierEnabled();
        handlerCtx.setOutputValue("Reload", reload);
        handlerCtx.setOutputValue("ReloadInterval", reloadInterval);
        handlerCtx.setOutputValue("AutoDeploy", autoDeploy);
        handlerCtx.setOutputValue("AdminTimeout", adminTimeout);
        handlerCtx.setOutputValue("AutoDeployInterval", autoDeployInterval);
        handlerCtx.setOutputValue("AutoDeployTimeout", autoDeployTimeout);
        handlerCtx.setOutputValue("AutoDeployDirectory", autoDeployDirectory);
        handlerCtx.setOutputValue("Precompile", precompile);
        handlerCtx.setOutputValue("Verifier", verifier);
        handlerCtx.setOutputValue("Properties", dConfig.getPropertyConfigMap());

    }

    /**
     *	<p> This handler returns the DEFAULT values for all the attributes in the
     *      Server Applications Config Page.</p>
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Reload" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "ReloadInterval" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AutoDeploy" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "AdminTimeout" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AutoDeployInterval" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AutoDeployTimeout" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Verifier" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Precompile" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "AutoDeployDirectory" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getServerDefaultAppsConfigAttributes", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true)
    }, output = {
        @HandlerOutput(name = "Reload", type = Boolean.class),
        @HandlerOutput(name = "ReloadInterval", type = String.class),
        @HandlerOutput(name = "AutoDeploy", type = Boolean.class),
        @HandlerOutput(name = "AdminTimeout", type = String.class),
        @HandlerOutput(name = "AutoDeployInterval", type = String.class),
        @HandlerOutput(name = "AutoDeployTimeout", type = String.class),
        @HandlerOutput(name = "Verifier", type = Boolean.class),
        @HandlerOutput(name = "Precompile", type = Boolean.class),
        @HandlerOutput(name = "AutoDeployDirectory", type = String.class)
    })
    public static void getServerDefaultAppsConfigAttributes(HandlerContext handlerCtx) {

        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        Map defaultMap = config.getAdminServiceConfig().getDefaultValues(XTypes.DAS_CONFIG, true);
        String reload = (String) defaultMap.get("DynamicReloadEnabled");
        String reloadInterval = (String) defaultMap.get("DynamicReloadPollIntervalInSeconds");
        String autoDeploy = (String) defaultMap.get("AutodeployEnabled");
        String adminTimeout = (String) defaultMap.get("AdminSessionTimeoutInMinutes");
        String autoDeployInterval = (String) defaultMap.get("AutodeployPollingIntervalInSeconds");
        String autoDeployTimeout = (String) defaultMap.get("AutodeployRetryTimeout");
        String autoDeployDirectory = (String) defaultMap.get("AutodeployDir");
        String precompile = (String) defaultMap.get("AutodeployJspPrecompilationEnabled");
        String verifier = (String) defaultMap.get("AutodeployVerifierEnabled");

        if ("true".equals(reload)) {
            handlerCtx.setOutputValue("Reload", true);
        } else {
            handlerCtx.setOutputValue("Reload", false);
        }
        handlerCtx.setOutputValue("ReloadInterval", reloadInterval);
        if ("true".equals(autoDeploy)) {
            handlerCtx.setOutputValue("AutoDeploy", true);
        } else {
            handlerCtx.setOutputValue("AutoDeploy", false);
        }
        handlerCtx.setOutputValue("AdminTimeout", adminTimeout);
        handlerCtx.setOutputValue("AutoDeployInterval", autoDeployInterval);
        handlerCtx.setOutputValue("AutoDeployTimeout", autoDeployTimeout);
        handlerCtx.setOutputValue("AutoDeployDirectory", autoDeployDirectory);
        if ("true".equals(precompile)) {
            handlerCtx.setOutputValue("Precompile", true);
        } else {
            handlerCtx.setOutputValue("Precompile", false);
        }
        if ("true".equals(verifier)) {
            handlerCtx.setOutputValue("Verifier", true);
        } else {
            handlerCtx.setOutputValue("Verifier", false);
        }
    }

    /**
     *	<p> This method saves the attributes on the
     *      Server Applications Config Page.</p>
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>     
     *	<p> input value: "Reload" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> input value: "ReloadInterval" -- Type: <code>java.lang.String</code></p>
     *  <p> input value: "AutoDeploy" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> input value: "AdminTimeout" -- Type: <code>java.lang.String</code></p>
     *  <p> input value: "AutoDeployInterval" -- Type: <code>java.lang.String</code></p>
     *  <p> input value: "Verifier" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> input value: "Precompile" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> input value: "AutoDeployDirectory" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "saveServerAppsConfigAttributes", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true),
        @HandlerInput(name = "Reload", type = String.class),
        @HandlerInput(name = "ReloadInterval", type = String.class),
        @HandlerInput(name = "AutoDeploy", type = String.class),
        @HandlerInput(name = "AdminTimeout", type = String.class),
        @HandlerInput(name = "AutoDeployInterval", type = String.class),
        @HandlerInput(name = "AutoDeployTimeout", type = String.class),
        @HandlerInput(name = "Verifier", type = String.class),
        @HandlerInput(name = "Precompile", type = String.class),
        @HandlerInput(name = "AutoDeployDirectory", type = String.class),
        @HandlerInput(name = "newProps", type = Map.class)
    })
    public static void saveServerAppsConfigAttributes(HandlerContext handlerCtx) {
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        DASConfig dConfig = config.getAdminServiceConfig().getDASConfig();
        dConfig.setDynamicReloadEnabled((String) handlerCtx.getInputValue("Reload"));
        dConfig.setDynamicReloadPollIntervalInSeconds((String) handlerCtx.getInputValue("ReloadInterval"));
        dConfig.setAutodeployEnabled((String) handlerCtx.getInputValue("AutoDeploy"));
        dConfig.setAdminSessionTimeoutInMinutes((String) handlerCtx.getInputValue("AdminTimeout"));
        dConfig.setAutodeployPollingIntervalInSeconds((String) handlerCtx.getInputValue("AutoDeployInterval"));
        dConfig.setAutodeployRetryTimeout((String) handlerCtx.getInputValue("AutoDeployTimeout"));
        dConfig.setAutodeployDir((String) handlerCtx.getInputValue("AutoDeployDirectory"));
        dConfig.setAutodeployJSPPrecompilationEnabled((String) handlerCtx.getInputValue("Precompile"));
        dConfig.setAutodeployVerifierEnabled((String) handlerCtx.getInputValue("Verifier"));
        AMXUtil.updateProperties(dConfig, (Map) handlerCtx.getInputValue("newProps"));
    }

    /**
     *	<p> This handler returns the values for all the attributes in the
     *      Server JVM General Page.</p>
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "JavaHome" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "JavacOptions" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "DebugEnabled" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "DebugOptions" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RmicOptions" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "BytecodePreprocessor" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getServerJvmAttributes", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true)
    }, output = {
        @HandlerOutput(name = "JavaHome", type = String.class),
        @HandlerOutput(name = "JavacOptions", type = String.class),
        @HandlerOutput(name = "DebugEnabled", type = Boolean.class),
        @HandlerOutput(name = "DebugOptions", type = String.class),
        @HandlerOutput(name = "RmicOptions", type = String.class),
        @HandlerOutput(name = "BytecodePreprocessor", type = String.class)
    })
    public static void getServerJvmAttributes(HandlerContext handlerCtx) {

        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        JavaConfig javaConfig = config.getJavaConfig();
        String javaHome = javaConfig.getJavaHome();
        String javacOptions = javaConfig.getJavacOptions();
        String debugEnabled = javaConfig.getDebugEnabled();
        String debugOptions = javaConfig.getDebugOptions();
        String rmicOptions = javaConfig.getRMICOptions();
        String bytecodePreprocessors = javaConfig.getBytecodePreprocessors();
        handlerCtx.setOutputValue("JavaHome", javaHome);
        handlerCtx.setOutputValue("JavacOptions", javacOptions);
        handlerCtx.setOutputValue("DebugEnabled", debugEnabled);
        handlerCtx.setOutputValue("DebugOptions", debugOptions);
        handlerCtx.setOutputValue("RmicOptions", rmicOptions);
        handlerCtx.setOutputValue("BytecodePreprocessor", bytecodePreprocessors);

    }

    /**
     *	<p> This handler returns the default values for all the attributes in the
     *      Server Logging Levels Page.</p>
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Admin" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "EJB" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Classloader" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Configuration" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Connector" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Corba" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Deployment" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Javamail" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Jaxr" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Jaxrpc" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Jms" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Jta" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Jts" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MDB" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Naming" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Root" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Saaj" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Security" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "SelfManagement" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Server" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Util" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Verifier" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "WEB" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getDefaultServerModuleLogLevels", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true)
    }, output = {
        @HandlerOutput(name = "EJB", type = String.class),
        @HandlerOutput(name = "Admin", type = String.class),
        @HandlerOutput(name = "EJB", type = String.class),
        @HandlerOutput(name = "Classloader", type = String.class),
        @HandlerOutput(name = "Configuration", type = String.class),
        @HandlerOutput(name = "Connector", type = String.class),
        @HandlerOutput(name = "Corba", type = String.class),
        @HandlerOutput(name = "Deployment", type = String.class),
        @HandlerOutput(name = "Javamail", type = String.class),
        @HandlerOutput(name = "Jaxr", type = String.class),
        @HandlerOutput(name = "Jaxrpc", type = String.class),
        @HandlerOutput(name = "Jms", type = String.class),
        @HandlerOutput(name = "Jta", type = String.class),
        @HandlerOutput(name = "Jts", type = String.class),
        @HandlerOutput(name = "MDB", type = String.class),
        @HandlerOutput(name = "Naming", type = String.class),
        @HandlerOutput(name = "Root", type = String.class),
        @HandlerOutput(name = "Saaj", type = String.class),
        @HandlerOutput(name = "Security", type = String.class),
        @HandlerOutput(name = "SelfManagement", type = String.class),
        @HandlerOutput(name = "Server", type = String.class),
        @HandlerOutput(name = "Util", type = String.class),
        @HandlerOutput(name = "Verifier", type = String.class),
        @HandlerOutput(name = "WEB", type = String.class),
        @HandlerOutput(name = "PersistenceLogLevel", type = String.class),
        @HandlerOutput(name = "Jbi", type = String.class),
        @HandlerOutput(name = "NodeAgent", type = String.class),
        @HandlerOutput(name = "Synchronization", type = String.class),
        @HandlerOutput(name = "Gms", type = String.class),
        @HandlerOutput(name = "Jaxws", type = String.class)
    })
    public static void getDefaultServerModuleLogLevels(HandlerContext handlerCtx) {

        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        ModuleLogLevelsConfig mConfig = config.getLogServiceConfig().getModuleLogLevelsConfig();
        String admin = mConfig.getDefaultValue("Admin");
        String ejb = mConfig.getDefaultValue("EJBContainer");
        String classLoader = mConfig.getDefaultValue("Classloader");
        String configuration = mConfig.getDefaultValue("Configuration");
        String connector = mConfig.getDefaultValue("Connector");
        String corba = mConfig.getDefaultValue("CORBA");
        String deployment = mConfig.getDefaultValue("Deployment");
        String javamail = mConfig.getDefaultValue("Javamail");
        String jaxr = mConfig.getDefaultValue("JAXR");
        String jaxrpc = mConfig.getDefaultValue("JAXRPC");
        String jms = mConfig.getDefaultValue("JMS");
        String jta = mConfig.getDefaultValue("JTA");
        String jts = mConfig.getDefaultValue("JTS");
        String mdb = mConfig.getDefaultValue("MDBContainer");
        String naming = mConfig.getDefaultValue("Naming");
        String root = mConfig.getDefaultValue("Root");
        String saaj = mConfig.getDefaultValue("SAAJ");
        String security = mConfig.getDefaultValue("Security");
        String selfManagement = mConfig.getDefaultValue("SelfManagement");
        String server = mConfig.getDefaultValue("Server");
        String util = mConfig.getDefaultValue("Util");
        String verifier = mConfig.getDefaultValue("Verifier");
        String web = mConfig.getDefaultValue("WebContainer");
        handlerCtx.setOutputValue("Admin", admin);
        handlerCtx.setOutputValue("Classloader", classLoader);
        handlerCtx.setOutputValue("Configuration", configuration);
        handlerCtx.setOutputValue("Connector", connector);
        handlerCtx.setOutputValue("Corba", corba);
        handlerCtx.setOutputValue("Deployment", deployment);
        handlerCtx.setOutputValue("Javamail", javamail);
        handlerCtx.setOutputValue("Jaxr", jaxr);
        handlerCtx.setOutputValue("Jaxrpc", jaxrpc);
        handlerCtx.setOutputValue("Jms", jms);
        handlerCtx.setOutputValue("Jta", jta);
        handlerCtx.setOutputValue("Jts", jts);
        handlerCtx.setOutputValue("MDB", mdb);
        handlerCtx.setOutputValue("Naming", naming);
        handlerCtx.setOutputValue("EJB", ejb);
        handlerCtx.setOutputValue("Root", root);
        handlerCtx.setOutputValue("Saaj", saaj);
        handlerCtx.setOutputValue("Security", security);
        handlerCtx.setOutputValue("SelfManagement", selfManagement);
        handlerCtx.setOutputValue("Server", server);
        handlerCtx.setOutputValue("Util", util);
        handlerCtx.setOutputValue("Verifier", verifier);
        handlerCtx.setOutputValue("WEB", web);
        handlerCtx.setOutputValue("PersistenceLogLevel", admin);
        handlerCtx.setOutputValue("Jbi", "INFO");
        handlerCtx.setOutputValue("Jaxws", "INFO");
        if (AMXRoot.getInstance().isEE()) {
            handlerCtx.setOutputValue("NodeAgent", mConfig.getDefaultValue("NodeAgent"));
            handlerCtx.setOutputValue("Synchronization", mConfig.getDefaultValue("Synchronization"));
            handlerCtx.setOutputValue("Gms", mConfig.getDefaultValue("GroupManagementService"));
        }

    }

    /**
     *	<p> This handler returns the values for all the attributes in the
     *      Server Logging Levels Page.</p>
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Admin" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "EJB" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Classloader" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Configuration" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Connector" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Corba" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Deployment" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Javamail" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Jaxr" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Jaxrpc" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Jms" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Jta" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "Jts" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "MDB" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Naming" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Root" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Saaj" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Security" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "SelfManagement" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Server" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Util" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Verifier" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "WEB" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Properties" -- Type: <code>java.util.Map</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getServerModuleLogLevels", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true)
    }, output = {
        @HandlerOutput(name = "EJB", type = String.class),
        @HandlerOutput(name = "Admin", type = String.class),
        @HandlerOutput(name = "EJB", type = String.class),
        @HandlerOutput(name = "Classloader", type = String.class),
        @HandlerOutput(name = "Configuration", type = String.class),
        @HandlerOutput(name = "Connector", type = String.class),
        @HandlerOutput(name = "Corba", type = String.class),
        @HandlerOutput(name = "Deployment", type = String.class),
        @HandlerOutput(name = "Javamail", type = String.class),
        @HandlerOutput(name = "Jaxr", type = String.class),
        @HandlerOutput(name = "Jaxrpc", type = String.class),
        @HandlerOutput(name = "Jms", type = String.class),
        @HandlerOutput(name = "Jta", type = String.class),
        @HandlerOutput(name = "Jts", type = String.class),
        @HandlerOutput(name = "MDB", type = String.class),
        @HandlerOutput(name = "Naming", type = String.class),
        @HandlerOutput(name = "Root", type = String.class),
        @HandlerOutput(name = "Saaj", type = String.class),
        @HandlerOutput(name = "Security", type = String.class),
        @HandlerOutput(name = "SelfManagement", type = String.class),
        @HandlerOutput(name = "Server", type = String.class),
        @HandlerOutput(name = "Util", type = String.class),
        @HandlerOutput(name = "Verifier", type = String.class),
        @HandlerOutput(name = "WEB", type = String.class),
        @HandlerOutput(name = "Jbi", type = String.class),
        @HandlerOutput(name = "NodeAgent", type = String.class),
        @HandlerOutput(name = "Synchronization", type = String.class),
        @HandlerOutput(name = "Gms", type = String.class),
        @HandlerOutput(name = "Jaxws", type = String.class)
    })
    public static void getServerModuleLogLevels(HandlerContext handlerCtx) {

        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        ModuleLogLevelsConfig mConfig = config.getLogServiceConfig().getModuleLogLevelsConfig();
        String admin = mConfig.getAdmin();
        String ejb = mConfig.getEJBContainer();
        String classLoader = mConfig.getClassloader();
        String configuration = mConfig.getConfiguration();
        String connector = mConfig.getConnector();
        String corba = mConfig.getCORBA();
        String deployment = mConfig.getDeployment();
        String javamail = mConfig.getJavamail();
        String jaxr = mConfig.getJAXR();
        String jaxrpc = mConfig.getJAXRPC();
        String jms = mConfig.getJMS();
        String jta = mConfig.getJTA();
        String jts = mConfig.getJTS();
        String mdb = mConfig.getMDBContainer();
        String naming = mConfig.getNaming();
        String root = mConfig.getRoot();
        String saaj = mConfig.getSAAJ();
        String security = mConfig.getSecurity();
        String selfManagement = mConfig.getSelfManagement();
        String server = mConfig.getServer();
        String util = mConfig.getUtil();
        String verifier = mConfig.getVerifier();
        String web = mConfig.getWebContainer();
        String jaxws = AMXUtil.getPropertyValue(mConfig, JAXWS_MODULE_PROPERTY, "INFO");
        String jbi = AMXUtil.getPropertyValue(mConfig, JBI_MODULE_PROPERTY, "INFO");

        handlerCtx.setOutputValue("Admin", admin);
        handlerCtx.setOutputValue("Classloader", classLoader);
        handlerCtx.setOutputValue("Configuration", configuration);
        handlerCtx.setOutputValue("Connector", connector);
        handlerCtx.setOutputValue("Corba", corba);
        handlerCtx.setOutputValue("Deployment", deployment);
        handlerCtx.setOutputValue("Javamail", javamail);
        handlerCtx.setOutputValue("Jaxr", jaxr);
        handlerCtx.setOutputValue("Jaxrpc", jaxrpc);
        handlerCtx.setOutputValue("Jms", jms);
        handlerCtx.setOutputValue("Jta", jta);
        handlerCtx.setOutputValue("Jts", jts);
        handlerCtx.setOutputValue("MDB", mdb);
        handlerCtx.setOutputValue("Naming", naming);
        handlerCtx.setOutputValue("EJB", ejb);
        handlerCtx.setOutputValue("Root", root);
        handlerCtx.setOutputValue("Saaj", saaj);
        handlerCtx.setOutputValue("Security", security);
        handlerCtx.setOutputValue("SelfManagement", selfManagement);
        handlerCtx.setOutputValue("Server", server);
        handlerCtx.setOutputValue("Util", util);
        handlerCtx.setOutputValue("Verifier", verifier);
        handlerCtx.setOutputValue("WEB", web);
        handlerCtx.setOutputValue("Jbi", jbi);
        handlerCtx.setOutputValue("Jaxws", jaxws);
        if (AMXRoot.getInstance().isEE()) {
            handlerCtx.setOutputValue("NodeAgent", mConfig.getNodeAgent());
            handlerCtx.setOutputValue("Synchronization", mConfig.getSynchronization());
            handlerCtx.setOutputValue("Gms", mConfig.getGroupManagementService());
        }

    }

    @Handler(id = "getPredefinedLogLevels", output = {
        @HandlerOutput(name = "levelsLabel", type = List.class),
        @HandlerOutput(name = "levelsValue", type = List.class)
    })
    public static void getPredefinedLogLevels(HandlerContext handlerCtx) {
        List labels = new ArrayList();
        labels.add(GuiUtil.getMessage("logging.Finest"));
        labels.add(GuiUtil.getMessage("logging.Finer"));
        labels.add(GuiUtil.getMessage("logging.Fine"));
        labels.add(GuiUtil.getMessage("logging.Config"));
        labels.add(GuiUtil.getMessage("logging.Info"));
        labels.add(GuiUtil.getMessage("logging.Warning"));
        labels.add(GuiUtil.getMessage("logging.Severe"));
        labels.add(GuiUtil.getMessage("logging.Off"));

        List values = new ArrayList();
        values.add("FINEST");
        values.add("FINER");
        values.add("FINE");
        values.add("CONFIG");
        values.add("INFO");
        values.add("WARNING");
        values.add("SEVERE");
        values.add("OFF");

        handlerCtx.setOutputValue("levelsLabel", labels);
        handlerCtx.setOutputValue("levelsValue", values);
    }

    /**
     *	<p> This handler saves the values for all the attributes in the
     *      Server Logging Levels Page.</p>
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Admin" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "EJB" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Classloader" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Configuration" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Connector" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Corba" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Deployment" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Javamail" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Jaxr" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Jaxrpc" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Jms" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Jta" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Jts" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "MDB" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Naming" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Root" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Saaj" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Security" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "SelfManagement" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Server" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Util" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Verifier" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "WEB" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "saveServerModuleLogLevels", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true),
        @HandlerInput(name = "EJB", type = String.class),
        @HandlerInput(name = "Admin", type = String.class),
        @HandlerInput(name = "EJB", type = String.class),
        @HandlerInput(name = "Classloader", type = String.class),
        @HandlerInput(name = "Configuration", type = String.class),
        @HandlerInput(name = "Connector", type = String.class),
        @HandlerInput(name = "Corba", type = String.class),
        @HandlerInput(name = "Deployment", type = String.class),
        @HandlerInput(name = "Javamail", type = String.class),
        @HandlerInput(name = "Jaxr", type = String.class),
        @HandlerInput(name = "Jaxrpc", type = String.class),
        @HandlerInput(name = "Jms", type = String.class),
        @HandlerInput(name = "Jta", type = String.class),
        @HandlerInput(name = "Jts", type = String.class),
        @HandlerInput(name = "MDB", type = String.class),
        @HandlerInput(name = "Naming", type = String.class),
        @HandlerInput(name = "Root", type = String.class),
        @HandlerInput(name = "Saaj", type = String.class),
        @HandlerInput(name = "Security", type = String.class),
        @HandlerInput(name = "SelfManagement", type = String.class),
        @HandlerInput(name = "Server", type = String.class),
        @HandlerInput(name = "Util", type = String.class),
        @HandlerInput(name = "Verifier", type = String.class),
        @HandlerInput(name = "WEB", type = String.class),
        @HandlerInput(name = "Jbi", type = String.class),
        @HandlerInput(name = "Jaxws", type = String.class),
        @HandlerInput(name = "NodeAgent", type = String.class),
        @HandlerInput(name = "Synchronization", type = String.class),
        @HandlerInput(name = "Gms", type = String.class),
        @HandlerInput(name = "newProps", type = Map.class)
    })
    public static void saveServerModuleLogLevels(HandlerContext handlerCtx) {

        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        ModuleLogLevelsConfig mConfig = config.getLogServiceConfig().getModuleLogLevelsConfig();

        Map newProps = (Map) handlerCtx.getInputValue("newProps");
        /* TODO-V3
        //AMXUtil.updateProperties(mConfig, newProps, skipLogModulePropsList);
         */

        mConfig.setAdmin((String) handlerCtx.getInputValue("Admin"));
        mConfig.setEJBContainer((String) handlerCtx.getInputValue("EJB"));
        mConfig.setClassloader((String) handlerCtx.getInputValue("Classloader"));
        mConfig.setConfiguration((String) handlerCtx.getInputValue("Configuration"));
        mConfig.setConnector((String) handlerCtx.getInputValue("Connector"));
        mConfig.setCORBA((String) handlerCtx.getInputValue("Corba"));
        mConfig.setDeployment((String) handlerCtx.getInputValue("Deployment"));
        mConfig.setJavamail((String) handlerCtx.getInputValue("Javamail"));
        mConfig.setJAXR((String) handlerCtx.getInputValue("Jaxr"));
        mConfig.setJAXRPC((String) handlerCtx.getInputValue("Jaxrpc"));
        mConfig.setJMS((String) handlerCtx.getInputValue("Jms"));
        mConfig.setJTA((String) handlerCtx.getInputValue("Jta"));
        mConfig.setJTS((String) handlerCtx.getInputValue("Jts"));
        mConfig.setMDBContainer((String) handlerCtx.getInputValue("MDB"));
        mConfig.setNaming((String) handlerCtx.getInputValue("Naming"));
        mConfig.setRoot((String) handlerCtx.getInputValue("Root"));
        mConfig.setSAAJ((String) handlerCtx.getInputValue("Saaj"));
        mConfig.setSecurity((String) handlerCtx.getInputValue("Security"));
        mConfig.setSelfManagement((String) handlerCtx.getInputValue("SelfManagement"));
        mConfig.setServer((String) handlerCtx.getInputValue("Server"));
        mConfig.setUtil((String) handlerCtx.getInputValue("Util"));
        mConfig.setVerifier((String) handlerCtx.getInputValue("Verifier"));
        mConfig.setWebContainer((String) handlerCtx.getInputValue("WEB"));
        if (AMXRoot.getInstance().isEE()) {
            mConfig.setNodeAgent((String) handlerCtx.getInputValue("NodeAgent"));
            mConfig.setSynchronization((String) handlerCtx.getInputValue("Synchronization"));
            mConfig.setGroupManagementService((String) handlerCtx.getInputValue("Gms"));
        }

        AMXUtil.setPropertyValue(mConfig, JBI_MODULE_PROPERTY, (String) handlerCtx.getInputValue("Jbi"));
        AMXUtil.setPropertyValue(mConfig, JAXWS_MODULE_PROPERTY, (String) handlerCtx.getInputValue("Jaxws"));

    }

    /**
     *	<p> This handler returns the default values for all the attributes in the
     *      Server JVM General Page.</p>
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "JavaHome" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "JavacOptions" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "DebugEnabled" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "DebugOptions" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RmicOptions" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getServerDefaultJvmAttributes", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true)
    }, output = {
        @HandlerOutput(name = "JavaHome", type = String.class),
        @HandlerOutput(name = "Options", type = String.class),
        @HandlerOutput(name = "DebugEnabled", type = Boolean.class),
        @HandlerOutput(name = "DebugOptions", type = String.class),
        @HandlerOutput(name = "RmicOptions", type = String.class),
        @HandlerOutput(name = "BytecodePreprocessor", type = String.class)
    })
    public static void getServerDefaultJvmAttributes(HandlerContext handlerCtx) {

        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        Map defaultMap = config.getDefaultValues(XTypes.JAVA_CONFIG, true);
        String javaHome = (String) defaultMap.get("JavaHome");
        String javacOptions = (String) defaultMap.get("JavacOptions");
        String rmicOptions = (String) defaultMap.get("RmicOptions");
        String debugOptions = (String) defaultMap.get("DebugOptions");
        String bytecodePreprocessors = (String) defaultMap.get("BytecodePreprocessors");
        String debugEnabled = (String) defaultMap.get("DebugEnabled");

        handlerCtx.setOutputValue("JavaHome", javaHome);
        handlerCtx.setOutputValue("Options", javacOptions);
        if ("true".equals(debugEnabled)) {
            handlerCtx.setOutputValue("DebugEnabled", true);
        } else {
            handlerCtx.setOutputValue("DebugEnabled", false);
        }

        handlerCtx.setOutputValue("DebugOptions", debugOptions);
        handlerCtx.setOutputValue("RmicOptions", rmicOptions);
        handlerCtx.setOutputValue("BytecodePreprocessor", bytecodePreprocessors);

    }

    /**
     *	<p> This method saves the attributes on the
     *      Server JVM General Page.</p>
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "JavaHome" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "JavacOptions" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "DebugEnabled" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "DebugOptions" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RmicOptions" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "BytecodePreprocessor" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "saveServerJvmAttributes", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true),
        @HandlerInput(name = "JavaHome", type = String.class, required = true),
        @HandlerInput(name = "JavacOptions", type = String.class),
        @HandlerInput(name = "DebugEnabled", type = Boolean.class),
        @HandlerInput(name = "DebugOptions", type = String.class),
        @HandlerInput(name = "RmicOptions", type = String.class),
        @HandlerInput(name = "BytecodePreprocessor", type = String.class)
    })
    public static void saveServerJvmAttributes(HandlerContext handlerCtx) {

        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        JavaConfig javaConfig = config.getJavaConfig();

        javaConfig.setJavaHome((String) handlerCtx.getInputValue("JavaHome"));
        javaConfig.setJavacOptions((String) handlerCtx.getInputValue("JavacOptions"));
        javaConfig.setDebugEnabled("" + handlerCtx.getInputValue("DebugEnabled"));
        javaConfig.setDebugOptions((String) handlerCtx.getInputValue("DebugOptions"));
        javaConfig.setRMICOptions((String) handlerCtx.getInputValue("RmicOptions"));
        javaConfig.setBytecodePreprocessors((String) handlerCtx.getInputValue("BytecodePreprocessor"));

    }

    /**
     *	<p> This handler returns the values for all the attributes in the
     *      Server JVM Path Settings.</p>
     *	<p> Output value: "SystemClasspath" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "ServerClasspath" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "IgnoreEnvClasspath" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "ClasspathPrefix" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "ClasspathSuffix" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "NativeLibPathPrefix" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "NativeLibPathSuffix" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getServerJvmPathSettings", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true)
    }, output = {
        @HandlerOutput(name = "SystemClasspath", type = String.class),
        @HandlerOutput(name = "IgnoreEnvClasspath", type = Boolean.class),
        @HandlerOutput(name = "ServerClasspath", type = String.class),
        @HandlerOutput(name = "ClasspathPrefix", type = String.class),
        @HandlerOutput(name = "ClasspathSuffix", type = String.class),
        @HandlerOutput(name = "NativeLibPathPrefix", type = String.class),
        @HandlerOutput(name = "NativeLibPathSuffix", type = String.class)
    })
    public static void getServerJvmPathSettings(HandlerContext handlerCtx) {

        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        JavaConfig javaConfig = config.getJavaConfig();
        String systemClasspath = javaConfig.getSystemClasspath();
        String serverClasspath = javaConfig.getServerClasspath();
        String ignoreEnvClasspath = javaConfig.getEnvClasspathIgnored();
        String classpathPrefix = javaConfig.getClasspathPrefix();
        String classpathSuffix = javaConfig.getClasspathSuffix();
        String nativeLibPathPrefix = javaConfig.getNativeLibraryPathPrefix();
        String nativeLibPathSuffix = javaConfig.getNativeLibraryPathSuffix();
        handlerCtx.setOutputValue("SystemClasspath", formatStringsforViewing(systemClasspath));
        handlerCtx.setOutputValue("ServerClasspath", formatStringsforViewing(serverClasspath));
        handlerCtx.setOutputValue("IgnoreEnvClasspath", ignoreEnvClasspath);
        handlerCtx.setOutputValue("ClasspathPrefix", formatStringsforViewing(classpathPrefix));
        handlerCtx.setOutputValue("ClasspathSuffix", formatStringsforViewing(classpathSuffix));
        handlerCtx.setOutputValue("NativeLibPathPrefix", formatStringsforViewing(nativeLibPathPrefix));
        handlerCtx.setOutputValue("NativeLibPathSuffix", formatStringsforViewing(nativeLibPathSuffix));

    }

    /**
     *	<p> This method saves the attributes on the
     *      Server JVM Path Settings.</p>
     *
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "SystemClasspath" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "ServerClasspath" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "IgnoreEnvClasspath" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "ClasspathPrefix" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "ClasspathSuffix" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "NativeLibPathPrefix" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "NativeLibPathSuffix" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "saveServerJvmPathSettings", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true),
        @HandlerInput(name = "SystemClasspath", type = String.class),
        @HandlerInput(name = "ServerClasspath", type = String.class),
        @HandlerInput(name = "IgnoreEnvClasspath", type = Boolean.class),
        @HandlerInput(name = "ClasspathPrefix", type = String.class),
        @HandlerInput(name = "ClasspathSuffix", type = String.class),
        @HandlerInput(name = "NativeLibPathPrefix", type = String.class),
        @HandlerInput(name = "NativeLibPathSuffix", type = String.class)
    })
    public static void saveServerJvmPathSettings(HandlerContext handlerCtx) {

        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        JavaConfig javaConfig = config.getJavaConfig();
        String sysCP = (String) handlerCtx.getInputValue("SystemClasspath");
        String serverCP = (String) handlerCtx.getInputValue("ServerClasspath");
        String cpPrefix = (String) handlerCtx.getInputValue("ClasspathPrefix");
        String cpSuffix = (String) handlerCtx.getInputValue("ClasspathSuffix");
        String nativePrefix = (String) handlerCtx.getInputValue("NativeLibPathPrefix");
        String nativeSuffix = (String) handlerCtx.getInputValue("NativeLibPathSuffix");
        javaConfig.setSystemClasspath(formatStringsforSaving(sysCP));
        javaConfig.setServerClasspath(formatStringsforSaving(serverCP));
        javaConfig.setEnvClasspathIgnored("" + handlerCtx.getInputValue("IgnoreEnvClasspath"));
        javaConfig.setClasspathPrefix(formatStringsforSaving(cpPrefix));
        javaConfig.setClasspathSuffix(formatStringsforSaving(cpSuffix));
        javaConfig.setNativeLibraryPathPrefix(formatStringsforSaving(nativePrefix));
        javaConfig.setNativeLibraryPathSuffix(formatStringsforSaving(nativeSuffix));

    }

    //This converts any tab/NL etc to ${path.separator} before passing to backend for setting.
    //In domain.xml, it will be written out like  c:foo.jar${path.separator}c:bar.jar
    private static String formatStringsforSaving(String values) {
        String token = "";
        if ((values != null) &&
                (values.toString().trim().length() != 0)) {
            Iterator it = GuiUtil.parseStringList(values, "\t\n\r\f").iterator();
            while (it.hasNext()) {
                String nextToken = (String) it.next();
                token += nextToken + PATH_SEPARATOR;
            }
            int end = token.length() - PATH_SEPARATOR.length();
            if (token.lastIndexOf(PATH_SEPARATOR) == end) {
                token = token.substring(0, end);
            }
        }
        return token;
    }

    //This is the reserve of the above method.
    //We want to separator and display each jar in one line in the text box.
    private static String formatStringsforViewing(String values) {

        if (values == null || GuiUtil.isEmpty(values.trim())) {
            return "";
        }
        String s1 = values.trim().replaceAll("\\.jar:", "\\.jar\\$\\{path.separator\\}");
        String s2 = s1.replaceAll("\\.jar;", "\\.jar\\$\\{path.separator\\}");
        String[] strArray = s2.split("\\$\\{path.separator\\}");
        String result = "";
        for (String s : strArray) {
            result = result + s + "\n";
        }
        return result.trim();
    }

    /**
     *	<p> This handler returns the values for all the attributes in the
     *      Logging General Settings.</p>
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "LogFile" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Alarms" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "SystemLog" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "LogHandler" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "LogFilter" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RotationLimit" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RotationTimeLimit" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "RetainErrorStats" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getLogGeneralSettings", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true)
    }, output = {
        @HandlerOutput(name = "LogFile", type = String.class),
        @HandlerOutput(name = "Alarms", type = String.class),
        @HandlerOutput(name = "SystemLog", type = String.class),
        @HandlerOutput(name = "LogHandler", type = String.class),
        @HandlerOutput(name = "LogFilter", type = String.class),
        @HandlerOutput(name = "RotationLimit", type = String.class),
        @HandlerOutput(name = "RotationTimeLimit", type = String.class),
        @HandlerOutput(name = "RetainErrorStats", type = String.class)
    })
    public static void getLogGeneralSettings(HandlerContext handlerCtx) {

        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        LogServiceConfig lc = config.getLogServiceConfig();
        String logFile = lc.getFile();
        String alarms = lc.getAlarms();
        String systemLog = lc.getUseSystemLogging();
        String logHandler = lc.getLogHandler();
        String logFilter = lc.getLogFilter();
        String rotationLimit = lc.getLogRotationLimitInBytes();
        String rotationTimeLimit = lc.getLogRotationTimeLimitInMinutes();
        String retainErrorStats = lc.getRetainErrorStatisticsForHours();
        handlerCtx.setOutputValue("LogFile", logFile);
        handlerCtx.setOutputValue("Alarms", alarms);
        handlerCtx.setOutputValue("SystemLog", systemLog);
        handlerCtx.setOutputValue("LogHandler", logHandler);
        handlerCtx.setOutputValue("LogFilter", logFilter);
        handlerCtx.setOutputValue("RotationLimit", rotationLimit);
        handlerCtx.setOutputValue("RotationTimeLimit", rotationTimeLimit);
        handlerCtx.setOutputValue("RetainErrorStats", retainErrorStats);

    }

    /**
     *	<p> This method saves the attributes on the
     *      Server Log General Settings.</p>
     *
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "LogFile" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Alarms" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "SystemLog" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "LogHandler" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "LogFilter" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RotationLimit" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RotationTimeLimit" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "RetainErrorStats" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "PropsList" -- Type: <code>java.util.List</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "saveServerLogGeneralSettings", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true),
        @HandlerInput(name = "LogFile", type = String.class),
        @HandlerInput(name = "Alarms", type = String.class),
        @HandlerInput(name = "SystemLog", type = String.class),
        @HandlerInput(name = "LogHandler", type = String.class),
        @HandlerInput(name = "LogFilter", type = String.class),
        @HandlerInput(name = "RotationLimit", type = String.class),
        @HandlerInput(name = "RotationTimeLimit", type = String.class),
        @HandlerInput(name = "RetainErrorStats", type = String.class),
        @HandlerInput(name = "AddProps", type = Map.class),
        @HandlerInput(name = "RemoveProps", type = ArrayList.class)
    })
    public static void saveServerLogGeneralSettings(HandlerContext handlerCtx) {

        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        try {
            LogServiceConfig lc = config.getLogServiceConfig();
            ArrayList removeProps = (ArrayList) handlerCtx.getInputValue("RemoveProps");
            Map addProps = (Map) handlerCtx.getInputValue("AddProps");
            String[] remove = (String[]) removeProps.toArray(new String[removeProps.size()]);
            for (int i = 0; i < remove.length; i++) {
                lc.removePropertyConfig(remove[i]);
            }
            if (addProps != null) {
                Iterator additer = addProps.keySet().iterator();
                while (additer.hasNext()) {
                    Object key = additer.next();
                    String addvalue = (String) addProps.get(key);
                    AMXUtil.setPropertyValue(lc, (String) key, addvalue);

                }
            }
            lc.setFile((String) handlerCtx.getInputValue("LogFile"));
            lc.setAlarms((String) handlerCtx.getInputValue("Alarms"));
            lc.setUseSystemLogging((String) handlerCtx.getInputValue("SystemLog"));
            lc.setLogHandler((String) handlerCtx.getInputValue("LogHandler"));
            lc.setLogFilter((String) handlerCtx.getInputValue("LogFilter"));
            lc.setLogRotationLimitInBytes((String) handlerCtx.getInputValue("RotationLimit"));
            lc.setLogRotationTimeLimitInMinutes((String) handlerCtx.getInputValue("RotationTimeLimit"));
            lc.setRetainErrorStatisticsForHours((String) handlerCtx.getInputValue("RetainErrorStats"));
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     *	<p> This handler returns the selected row keys.</p>
     *
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "saveJvmOptions", input = {
        @HandlerInput(name = "NameList", type = ArrayList.class, required = true),
        @HandlerInput(name = "ConfigName", type = String.class, required = true)
    })
    public static void saveJvmOptions(HandlerContext handlerCtx) {
        ArrayList names = (ArrayList) handlerCtx.getInputValue("NameList");
        if (names == null) {
            GuiUtil.handleError(handlerCtx, "saveJvmOptions(): NameList passed in is NULL");
            return;
        }
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        try {
            JavaConfig javaConfig = config.getJavaConfig();
            String[] options = (String[]) names.toArray(new String[names.size()]);
            javaConfig.setJVMOptions(options);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     *	<p> This handler returns the selected row keys.</p>
     *
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "saveProfilerJvmOptions", input = {
        @HandlerInput(name = "NameList", type = ArrayList.class, required = true),
        @HandlerInput(name = "ConfigName", type = String.class, required = true)
    })
    public static void saveProfilerJvmOptions(HandlerContext handlerCtx) {
        ArrayList names = (ArrayList) handlerCtx.getInputValue("NameList");
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        try {
            ProfilerConfig profilerConfig = config.getJavaConfig().getProfilerConfig();
            if (names != null && names.size() > 0) {
                String[] options = (String[]) names.toArray(new String[names.size()]);
                profilerConfig.setJVMOptions(options);
            }else{
                profilerConfig.setJVMOptions(new String[0]);
            }

        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     *	<p> This handler returns the values for all the attributes in the
     *      Server Profiler Settings.</p>
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "ProfilerName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "ProfilerEnabled" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Classpath" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "NativeLibrary" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getServerProfilerAttributes", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true)
    }, output = {
        @HandlerOutput(name = "ProfilerName", type = String.class),
        @HandlerOutput(name = "ProfilerEnabled", type = Boolean.class),
        @HandlerOutput(name = "Classpath", type = String.class),
        @HandlerOutput(name = "NativeLibrary", type = String.class),
        @HandlerOutput(name = "edit", type = Boolean.class)
    })
    public static void getServerProfilerAttributes(HandlerContext handlerCtx) {

        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        try {
            JavaConfig javaConfig = config.getJavaConfig();
            if (javaConfig.getProfilerConfig() != null) {
                ProfilerConfig profilerConfig = javaConfig.getProfilerConfig();
                String name = profilerConfig.getName();
                String enabled = profilerConfig.getEnabled();
                String classPath = profilerConfig.getClasspath();
                String nativeLibrary = profilerConfig.getNativeLibraryPath();
                handlerCtx.setOutputValue("Classpath", classPath);
                handlerCtx.setOutputValue("NativeLibrary", nativeLibrary);
                handlerCtx.setOutputValue("ProfilerName", name);
                handlerCtx.setOutputValue("ProfilerEnabled", enabled);
                handlerCtx.setOutputValue("edit", true);
            } else {
                handlerCtx.setOutputValue("ProfilerEnabled", true);
                handlerCtx.setOutputValue("edit", false);
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     *	<p> This method saves the attributes on the
     *      Server Profiler Settings.</p>
     *
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "ProfilerName" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Classpath" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "ProfilerEnabled" -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "NativeLibrary" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "saveServerProfilerSettings", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true),
        @HandlerInput(name = "ProfilerName", type = String.class),
        @HandlerInput(name = "Classpath", type = String.class),
        @HandlerInput(name = "ProfilerEnabled", type = Boolean.class),
        @HandlerInput(name = "NativeLibrary", type = String.class)
    })
    public static void saveServerProfilerSettings(HandlerContext handlerCtx) {

        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        String name = (String) handlerCtx.getInputValue("ProfilerName");
        if (name == null) {
            GuiUtil.handleError(handlerCtx, "Profile Name: Name passed in is NULL");
            return;
        }
        JavaConfig javaConfig = config.getJavaConfig();
        ProfilerConfig profiler = javaConfig.getProfilerConfig();
        String classpath = (String) handlerCtx.getInputValue("Classpath");
        String nativelibrary = (String) handlerCtx.getInputValue("NativeLibrary");
        Boolean profilerenabled = (Boolean) handlerCtx.getInputValue("ProfilerEnabled");
        try {
            if (profiler == null) {
                Map map = new HashMap();
                if (classpath != null) {
                    map.put(ProfilerConfigKeys.CLASSPATH_KEY, classpath);
                }
                if (nativelibrary != null) {
                    map.put(ProfilerConfigKeys.NATIVE_LIBRARY_PATH_KEY, nativelibrary);
                }
                map.put(ProfilerConfigKeys.ENABLED_KEY, (profilerenabled == null) ? "false" : profilerenabled.toString());
                javaConfig.createProfilerConfig(name, map);
            } else {
                if (classpath != null) {
                    profiler.setClasspath(classpath);
                }
                if (nativelibrary != null) {
                    profiler.setNativeLibraryPath(nativelibrary);
                }
                profiler.setEnabled((profilerenabled == null) ? "false" : "" + profilerenabled);

            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     *	<p> This method returns the logger list for the 
     *      Module Log Levels Page.</p>
     *
     *	<p> Input value: "Module" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "LogList" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getLoggerList", input = {
        @HandlerInput(name = "Module", type = String.class, required = true)
    }, output = {
        @HandlerOutput(name = "LogList", type = String.class)
    })
    public static void getLoggerList(HandlerContext handlerCtx) {

        String module = (String) handlerCtx.getInputValue("Module");
        String logList = getLoggerList(module);
        if (module.equals("jbi")) {
            handlerCtx.setOutputValue("LogList", "( " + JBI_MODULE_PROPERTY + ")");
        } else if (module.equals("jaxws")) {
            handlerCtx.setOutputValue("LogList", "( " + JAXWS_MODULE_PROPERTY + ")");
        } else {
            handlerCtx.setOutputValue("LogList", "( " + logList + ")");
        }
    }

    /**
     *	<p> This method returns the logger list for the 
     *      Persistence Module Log Levels Page.</p>
     *
     *	<p> Output value: "LogList" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getPersistenceLoggerList", output = {
        @HandlerOutput(name = "LogList", type = String.class)
    })
    public static void getPersistenceLoggerList(HandlerContext handlerCtx) {

        String jdo = getLoggerList("jdo");
        String cmp = getLoggerList("cmp");
        handlerCtx.setOutputValue("LogList", "( " + PERSISTENCE_MODULE_PROPERTY + "; " + jdo + cmp + ")");
    }

    private static String getLoggerList(String module) {
        String[] params = {module};
        String[] types = {"java.lang.String"};

        String logList = "";
        /* TODO-V3
        List loggers = (List) JMXUtil.invoke(
        "com.sun.appserv:name=logmanager,category=runtime,server=server",
        "getLognames4LogModule", params, types);
        if (loggers != null){
        for(int cnt = 0; cnt < loggers.size(); cnt++){
        logList += loggers.get(cnt);
        logList +="; ";
        }
        }
        return logList;
         */
        return " ";
    }

    /**
     *	<p> This method returns the logger list for the 
     *      Module Log Levels Page.</p>
     *
     *	<p> Input value: "Module" -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "LogList" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getPersistenceLogLevel", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true)
    }, output = {
        @HandlerOutput(name = "PersistenceLogLevel", type = String.class)
    })
    public static void getPersistenceLogLevel(HandlerContext handlerCtx) {
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        ModuleLogLevelsConfig mConfig = config.getLogServiceConfig().getModuleLogLevelsConfig();
        handlerCtx.setOutputValue("PersistenceLogLevel", AMXUtil.getPropertyValue(mConfig, PERSISTENCE_MODULE_PROPERTY, mConfig.getJDO()));
    }

    /**
     *	<p> This method saves the Persistence Log Level
     *      in the Log Level Settings Page.</p>
     *
     *	<p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "Value" -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "setPersistenceLogLevel", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true),
        @HandlerInput(name = "Value", type = String.class)
    })
    public static void setPersistenceLogLevel(HandlerContext handlerCtx) {

        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        String value = (String) handlerCtx.getInputValue("Value");
        ModuleLogLevelsConfig mConfig = config.getLogServiceConfig().getModuleLogLevelsConfig();

        AMXUtil.setPropertyValue(mConfig, PERSISTENCE_MODULE_PROPERTY, value);
        mConfig.setJDO(value);
        mConfig.setCMP(value);
    }

    /**
     *	<p> This method generates the diagnostic report </p>
     *
     *	<p> Input value: "Description" -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "BugIds" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "StartDate" -- Type: <code>java.lang.Date</code></p>
     *  <p> Input value: "EndDate" -- Type: <code>java.lang.Date</code></p>
     *  <p> Input value: "Target" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "ReportLocation -- Type: <code>java.lang.String</code>/</p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "generateDiagnosticReport", input = {
        @HandlerInput(name = "Description", type = String.class),
        @HandlerInput(name = "BugIds", type = String.class),
        @HandlerInput(name = "StartDate", type = Date.class),
        @HandlerInput(name = "EndDate", type = Date.class),
        @HandlerInput(name = "Target", type = String.class)
    }, output = {
        @HandlerOutput(name = "ReportLocation", type = String.class)
    })
    public static void generateDiagnosticReport(HandlerContext handlerCtx) {
        HashMap map = new HashMap();
        map.put("logstartdate", (Date) handlerCtx.getInputValue("StartDate"));
        map.put("logenddate", (Date) handlerCtx.getInputValue("EndDate"));
        map.put("bugids", (String) handlerCtx.getInputValue("BugIds"));
        map.put("input", (String) handlerCtx.getInputValue("Description"));
        map.put("target", (String) handlerCtx.getInputValue("Target"));
        String reportLocation = "";
    /* TODO-V3
    try {
    reportLocation = (String)JMXUtil.invoke(
    JMXUtil.DomainDiagnosticsMBeanName,
    JMXUtil.DomainDiagnosticsGenerateReportMethod,
    new Object[] { map },
    new String[] { "java.util.Map" });
    } catch(Exception ex) {
    GuiUtil.handleException(handlerCtx, ex);
    }
    handlerCtx.setOutputValue("ReportLocation", reportLocation);
     */

    }

    /**
     *	<p> This method returns the confidential properties </p>
     *
     *  <p> Output value: "ConfidentialProps" -- Type: <code>java.util.List</code>/</p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getConfidentialProps", output = {
        @HandlerOutput(name = "ConfidentialProps", type = List.class)
    })
    public static void getConfidentialProps(HandlerContext handlerCtx) {
        /* TODO-V3
        List propList = (List)JMXUtil.getAttribute(
        JMXUtil.DomainDiagnosticsMBeanName,
        "ConfidentialProperties");
        List mapList = new ArrayList(); 
        for (int i = 0; i < propList.size(); i++) {
        Map oneRow = new HashMap();
        oneRow.put("prop", propList.get(i));
        mapList.add(oneRow);
        }
        handlerCtx.setOutputValue("ConfidentialProps", mapList);
         */
    }

    /**
     *	<p> This method returns the jvm options </p>
     *
     *  <p> Output value: "JvmOptions" -- Type: <code>java.util.List</code>/</p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getJvmOptions", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true)
    }, output = {
        @HandlerOutput(name = "JvmOptions", type = List.class)
    })
    public static void getJvmOptions(HandlerContext handlerCtx) {

        //more null pointer checking put in due to issue#2247
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfigsConfig().getConfigConfigMap().get(configName);
        List<List<Map<String, Object>>> list = new ArrayList<List<Map<String, Object>>>();
        JavaConfig javaConfig = null;
        try {
            if (config == null) {
                System.out.println("getJvmOptions: getConfig() returns NULL,  configName = " + configName);
            } else {
                javaConfig = config.getJavaConfig();
                if (javaConfig == null) {
                    System.out.println("getJvmOptions: getJavaConfig() returns NULL,  configName = " + configName);
                } else {
                    String[] jvmOptions = javaConfig.getJVMOptions();
                    list.add(convertToListOfMap(jvmOptions, "option"));
                }
            }
        } catch (Exception ex) {
            System.out.println("Catch exception when trying to get the jvm option list.");
            ex.printStackTrace();
        }
        handlerCtx.setOutputValue("JvmOptions", list);
    }

    /**
     *	<p> This method returns the jvm options </p>
     *
     *  <p> Output value: "JvmOptions" -- Type: <code>java.util.List</code>/</p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getJvmOptionsForProfiler", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true)
    }, output = {
        @HandlerOutput(name = "JvmOptions", type = List.class)
    })
    public static void getJvmOptionsForProfiler(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfigsConfig().getConfigConfigMap().get(configName);
        List<List<Map<String, Object>>> list = new ArrayList<List<Map<String, Object>>>();
        //more null pointer checking put in due to issue#2247
        try {
            if (config == null) {
                System.out.println("!!!!! getJvmOptionsForProfiler:  getConfig() returns NULL. configName=" + configName);
            } else {
                JavaConfig javaConfig = config.getJavaConfig();
                if (javaConfig == null) {
                    System.out.println("!!!!! getJvmOptionsForProfiler: getJavaConfig() returns NULL; configName=" + configName);
                } else {
                    ProfilerConfig profilerConfig = javaConfig.getProfilerConfig();
                    String[] jvmOptions = (profilerConfig == null) ? new String[]{} : profilerConfig.getJVMOptions();
                    list.add(convertToListOfMap(jvmOptions, "option"));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        handlerCtx.setOutputValue("JvmOptions", list);
    }

    /**
     *	<p> This method returns the properties </p>
     *
     *  <p> Output value: "Properties" -- Type: <code>java.util.List</code>/</p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getModuleLogLevelProperties", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true)
    }, output = {
        @HandlerOutput(name = "Properties", type = Map.class)
    })
    public static void getModuleLogLevelProperties(HandlerContext handlerCtx) {
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        ModuleLogLevelsConfig mConfig = config.getLogServiceConfig().getModuleLogLevelsConfig();
        Map newMap = AMXUtil.getNonSkipPropertiesMap(mConfig, skipLogModulePropsList);
        handlerCtx.setOutputValue("Properties", newMap);
        handlerCtx.setOutputValue("Properties", " ");
    }

    /**
     *	<p> This method returns the properties </p>
     *
     *  <p> Output value: "Properties" -- Type: <code>java.util.List</code>/</p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getLoggingProperties", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true)
    }, output = {
        @HandlerOutput(name = "Properties", type = Map.class)
    })
    public static void getLoggingProperties(HandlerContext handlerCtx) {
        ConfigConfig config = AMXRoot.getInstance().getConfig(((String) handlerCtx.getInputValue("ConfigName")));
        LogServiceConfig lc = config.getLogServiceConfig();
        handlerCtx.setOutputValue("Properties", config.getLogServiceConfig().getPropertyConfigMap());
    }

    /**
     *	<p> This method returns values for the JVM Report </p>
     *  <p> Output value: "ViewsList" -- Type: <code>java.util.Array</code>/</p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getViews", input = {
        @HandlerInput(name = "InstanceName", type = String.class),
        @HandlerInput(name = "View", type = String.class)
    }, output = {
        @HandlerOutput(name = "Report", type = String.class)
    })
    public static void getViews(HandlerContext handlerCtx) {
        String instanceName = (String) handlerCtx.getInputValue("InstanceName");
        String selectedView = (String) handlerCtx.getInputValue("View");
        if (selectedView == null || selectedView.equals("")) {
            selectedView = "Summary";
        }
        /* TODO-V3
        try{
        String operationName = (String)viewOperationMap.get(selectedView);
        String objName = "com.sun.appserv:type=JVMInformationCollector,category=monitor,server=server";
        String report = (String)JMXUtil.invoke(objName, operationName, new Object[] {instanceName},
        new String[] {"java.lang.String"});
        SelectItem[] viewsList = ConfigurationHandlers.getOptions((String[])viewOperationMap.keySet().toArray(new String[viewOperationMap.size()]));       
        handlerCtx.setOutputValue("Report", report);   
        }catch(Exception ex){
        ex.printStackTrace();
        handlerCtx.setOutputValue("Report", "");   
        }
         */
        handlerCtx.setOutputValue("Report", " ");
    }

    /**
     *	<p> Returns the filter list for Messages Table.
     *  <p> Output  value: "ReportListValue" -- Type: <code>java.util.List</code></p>
     *  <p> Output  value: "ReportListLabel" -- Type: <code>java.util.List</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getJVMReportList", output = {
        @HandlerOutput(name = "ReportListValue", type = java.util.List.class),
        @HandlerOutput(name = "ReportListLabel", type = java.util.List.class)
    })
    public static void getJVMReportList(HandlerContext handlerCtx) {
        List label = new ArrayList();
        label.add(GuiUtil.getMessage("jvmReport.Summary"));
        label.add(GuiUtil.getMessage("jvmReport.Memory"));
        label.add(GuiUtil.getMessage("jvmReport.ClLoading"));
        label.add(GuiUtil.getMessage("jvmReport.ThDump"));

        List value = new ArrayList();
        value.add("Summary");
        value.add("Memory");
        value.add("Class Loading");
        value.add("Thread Dump");


        handlerCtx.setOutputValue("ReportListValue", value);
        handlerCtx.setOutputValue("ReportListLabel", label);
    }

    /**
     *	<p> This handler provides the recover transaction functionality </p>
     *	<p> Input value: "InstanceName"            -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "SupportCluster"            -- Type: <code>java.lang.Boolean</code></p>
     *	<p> Output value: "Running                 -- Type: <code>java.lang.Boolean</code></p>
     *	<p> Output value: "ServerName              -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "ServersList"            -- Type: <code>SelectItem[].class</code></p
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getServers", input = {
        @HandlerInput(name = "InstanceName", type = String.class, required = true),
        @HandlerInput(name = "SupportCluster", type = Boolean.class, required = true)
    }, output = {
        @HandlerOutput(name = "NotRunning", type = Boolean.class),
        @HandlerOutput(name = "ServerName", type = String.class),
        @HandlerOutput(name = "ServersList", type = SelectItem[].class)
    })
    public static void getServers(HandlerContext handlerCtx) {
        String instanceName = (String) handlerCtx.getInputValue("InstanceName");
        boolean supportCluster = (Boolean) handlerCtx.getInputValue("SupportCluster");
        String destServer = "";
        Vector<String> targets = new Vector();
        boolean notRunning = true;
    /* TODO-V3
    try {
    if(supportCluster) {
    Set<String> standaloneSet = AMXRoot.getInstance().getServersConfig().getStandaloneServerConfigMap().keySet();
    Set<String> clusterSet = AMXRoot.getInstance().getServersConfig().getClusteredServerConfigMap().keySet();           
    Set<String> allTargets = new TreeSet<String>();
    allTargets.addAll(standaloneSet);
    allTargets.addAll(clusterSet);
    Iterator<String> iter = allTargets.iterator();
    while(iter.hasNext()){
    String targetName = iter.next();
    RuntimeStatus rsts = JMXUtil.getRuntimeStatus(targetName);
    int state = JMXUtil.getRuntimeStatusCode(rsts);
    if(state == Status.kInstanceRunningCode){
    targets.add(targetName);
    if(instanceName.equals(targetName)){
    notRunning = false;
    destServer = instanceName;
    }
    }
    }
    }
    else {
    notRunning = false;
    targets.add(instanceName);
    }
    if(destServer.equals("")) {
    //default select the first running instance
    destServer = targets.get(0);
    }
    String[] targetNames = (String[])targets.toArray(new String[targets.size()]);
    handlerCtx.setOutputValue("NotRunning", notRunning);
    handlerCtx.setOutputValue("ServerName", destServer);
    //SelectItem[] options = ConfigurationHandlers.getOptions(targetNames, targetNames);
    //handlerCtx.setOutputValue("ServersList", ConfigurationHandlers.getModOptions(targetNames));
    }catch(Exception ex){
    GuiUtil.handleException(handlerCtx, ex);
    }
     */
    }

    /**
     *	<p> This handler provides the recover transaction functionality </p>
     *	<p> Input value: "InstanceName"            -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "ServerName"              -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "TransactionsLogDir       -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "recoverTransactions", input = {
        @HandlerInput(name = "SupportCluster", type = Boolean.class, required = true),
        @HandlerInput(name = "InstanceName", type = String.class),
        @HandlerInput(name = "ServerName", type = String.class),
        @HandlerInput(name = "TransactionsLogDir", type = String.class)
    })
    public static void recoverTransactions(HandlerContext handlerCtx) {
        try {
            String objName = "com.sun.appserv:type=transactions-recovery,category=config";
            String operName = "recoverTransactions";
            boolean supportCluster = ((Boolean) handlerCtx.getInputValue("SupportCluster")).booleanValue();
        /* TODO-V3
        if(! supportCluster){
        JMXUtil.invoke(objName, operName, null, null);
        }else{
        String instanceName = (String)handlerCtx.getInputValue("InstanceName");
        String serverName = (String)handlerCtx.getInputValue("ServerName");
        String logDir = (String)handlerCtx.getInputValue("TransactionsLogDir");
        //commenting this out, looks like backend needs null, if empty
        //if((!instanceName.equals("server")) && (logDir == null)){
        //    logDir = "";
        //}
        Object[] params = {instanceName, serverName, logDir};
        String[] signature = {"java.lang.String", "java.lang.String", "java.lang.String"};
        JMXUtil.invoke(objName, operName, params, signature);
        }*/
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    private static List<Map<String, Object>> convertToListOfMap(Object[] values, String key) {
        List<Map<String, Object>> list = new ArrayList();
        if (values != null) {
            Map<String, Object> map = null;
            for (Object val : values) {
                map = new HashMap<String, Object>();
                map.put(key, val);
                map.put("selected", false);
                list.add(map);
            }
        }

        return list;
    }
    private static HashMap viewOperationMap = new HashMap();
    

    static {
        viewOperationMap.put("Summary", "getSummary");
        viewOperationMap.put("Memory", "getMemoryInformation");
        viewOperationMap.put("Class Loading", "getClassInformation");
        viewOperationMap.put("Thread Dump", "getThreadDump");
    }
    private static final String PERSISTENCE_MODULE_PROPERTY = "oracle.toplink.essentials";
    private static final String JAXWS_MODULE_PROPERTY = "javax.enterprise.resource.webservices.jaxws";
    private static final String JBI_MODULE_PROPERTY = "com.sun.jbi";
    private static List skipLogModulePropsList = new ArrayList();
    

    static {
        skipLogModulePropsList.add(PERSISTENCE_MODULE_PROPERTY);
        skipLogModulePropsList.add(JAXWS_MODULE_PROPERTY);
        skipLogModulePropsList.add(JBI_MODULE_PROPERTY);
    }
    private static final String PATH_SEPARATOR = "${path.separator}";
}
