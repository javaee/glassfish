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
package com.sun.enterprise.ee.synchronization;

import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;

/**
 * Base class for all synchronization request builders.
 *
 * @author Nazrul Islam
 */
public abstract class RequestBuilderBase {
    
    public RequestBuilderBase(ConfigContext ctx, String serverName) {
        _configCtx = ctx;
        _serverName = serverName;
    }

    abstract String getApplicationName(ConfigBean cb);

    public ApplicationSynchRequest build(ConfigBean cb) {

        ApplicationSynchRequest asr = new ApplicationSynchRequest();

        // generated/policy/appId directory
        SynchronizationRequest sr = buildPolicyDir(cb);
        asr.setPolicyRequest(sr);

        // applications/j2ee-xxx/appId directory
        sr = buildAppDir(cb);
        asr.setApplicationRequest(sr);

        // generated/xml/appId directory
        sr = buildXmlDir(cb);
        asr.setXMLRequest(sr);

        // lib/applibs directory
        sr = buildAppLibsDir(cb);
        asr.setAppLibsRequest(sr);

        // java-web-start/appId directory
        sr = buildJwsDir(cb);
        asr.setJwsRequest(sr);

        return asr;
    }

    SynchronizationRequest buildRequest(String src) {

        SynchronizationRequest sr = null;
        String dest = "."; 
        String tsFile =  src + File.separator + TIMESTAMP_EXT; 

        BufferedReader is = null;
        long modTime = 0; 
        Properties tempEnv = new Properties();

        sr = new SynchronizationRequest(src, dest, modTime,
                    SynchronizationRequest.TIMESTAMP_MODIFIED_SINCE, null);
        Properties env = sr.getEnvironmentProperties();
        env.putAll(tempEnv);

        sr.setServerName(_serverName);
        sr.setCacheTimestampFile(tsFile);
        sr.setBaseDirectory(src);

        // sets the garbage collection on for directories
        try {
            File f = sr.getFile();

            // src is a real directory
            if (f.isDirectory()) {
                sr.setGCEnabled(true);
            }
        } catch (Exception e) {
            _logger.log(Level.FINE, "Error while setting GC enabled", e);
        }

        return sr;
    }

    String getPolicyDir(ConfigBean cb) {
        String src = OPEN_PROP + SystemPropertyConstants.INSTANCE_ROOT_PROPERTY
                   + CLOSE_PROP + File.separator 
                   + PEFileLayout.GENERATED_DIR
                   + File.separator + PEFileLayout.POLICY_DIR
                   + File.separator + getApplicationName(cb);
        return src;
    }

    SynchronizationRequest buildPolicyDir(ConfigBean cb) {

        return buildRequest( getPolicyDir(cb) );
    }

    String getJspDir(ConfigBean cb, String appOrModule) {
        String src = OPEN_PROP 
                   + SystemPropertyConstants.INSTANCE_ROOT_PROPERTY 
                   + CLOSE_PROP 
                   + File.separator + PEFileLayout.GENERATED_DIR 
                   + File.separator + PEFileLayout.JSP_DIR 
                   + File.separator + appOrModule
                   + File.separator + getApplicationName(cb);
        return src;
    }

    SynchronizationRequest buildJspDir(ConfigBean cb, String appOrModule) {
        return buildRequest( getJspDir(cb, appOrModule) );
    }

    String getEjbDir(ConfigBean cb, String appOrModule) {
        String src = OPEN_PROP 
                   + SystemPropertyConstants.INSTANCE_ROOT_PROPERTY 
                   + CLOSE_PROP 
                   + File.separator + PEFileLayout.GENERATED_DIR 
                   + File.separator + PEFileLayout.EJB_DIR 
                   + File.separator + appOrModule
                   + File.separator + getApplicationName(cb);
        return src;
    }

    SynchronizationRequest buildEjbDir(ConfigBean cb, String appOrModule) {
        return buildRequest( getEjbDir(cb, appOrModule) );
    }

    String getXmlDir(ConfigBean cb, String appOrModule) {
        String src = OPEN_PROP 
                   + SystemPropertyConstants.INSTANCE_ROOT_PROPERTY 
                   + CLOSE_PROP 
                   + File.separator + PEFileLayout.GENERATED_DIR 
                   + File.separator + PEFileLayout.XML_DIR 
                   + File.separator + appOrModule
                   + File.separator + getApplicationName(cb);
        return src;
    }

    SynchronizationRequest buildXmlDir(ConfigBean cb, String appOrModule) {
        return buildRequest( getXmlDir(cb, appOrModule) );
    }

    String getAppLibsDir(ConfigBean cb) {
        String src = OPEN_PROP 
                   + SystemPropertyConstants.INSTANCE_ROOT_PROPERTY 
                   + CLOSE_PROP 
                   + File.separator + PEFileLayout.LIB_DIR
                   + File.separator + PEFileLayout.APPLIBS_DIR 
                   + File.separator;
        return src;
    }

    SynchronizationRequest buildAppLibsDir(ConfigBean cb) {
        return buildRequest( getAppLibsDir(cb) );
    }

    String getJwsDir(ConfigBean cb) {
        String src = OPEN_PROP 
                   + SystemPropertyConstants.INSTANCE_ROOT_PROPERTY 
                   + CLOSE_PROP 
                   + File.separator + PEFileLayout.JAVA_WEB_START_DIR
                   + File.separator + getApplicationName(cb); 
                   
        return src;
    }

    SynchronizationRequest buildJwsDir(ConfigBean cb) {
        return buildRequest( getJwsDir(cb) );
    }

    abstract SynchronizationRequest buildAppDir(ConfigBean cb);

    abstract SynchronizationRequest buildJspDir(ConfigBean cb);

    abstract SynchronizationRequest buildEjbDir(ConfigBean cb);

    abstract SynchronizationRequest buildXmlDir(ConfigBean cb);


    //---- PRIVATE VARIABLES -------------------------
    private ConfigContext _configCtx = null;
    private String	  _serverName = null;
    String OPEN_PROP = "${";
    String CLOSE_PROP = "}";
    private String TIMESTAMP_EXT = ".com_sun_appserv_timestamp";
    private static Logger _logger = Logger.getLogger(
                        EELogDomains.SYNCHRONIZATION_LOGGER);
}
