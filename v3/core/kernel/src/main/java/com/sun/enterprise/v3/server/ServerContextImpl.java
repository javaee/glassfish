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

package com.sun.enterprise.v3.server;

import org.glassfish.server.ServerEnvironmentImpl;
import com.sun.enterprise.module.bootstrap.StartupContext;
import org.glassfish.internal.api.ServerContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Singleton;

import javax.naming.InitialContext;
import java.io.File;
import java.util.Map;

/**
 * This is the Server Context object.
 *
 * @author Jerome Dochez
 */
@Service
@Scoped(Singleton.class)
public class ServerContextImpl implements ServerContext, PostConstruct {

    @Inject
    ServerEnvironmentImpl env;

    @Inject
    StartupContext startupContext;

    @Inject
    Habitat habitat;

    File instanceRoot;
    String instanceName = "server"; // weird
    String[] args;

    /** Creates a new instance of ServerContextImpl */
    public void postConstruct() {
        this.instanceRoot = env.getDomainRoot();
        this.args = new String[startupContext.getArguments().size()*2];
        int i=0;
        for (Map.Entry<String, String> entry : startupContext.getArguments().entrySet()) {
            args[i++] = entry.getKey();
            args[i++] = entry.getValue();
        }
    }
    
    public File getInstanceRoot() {
        return instanceRoot;
    }

    public String[] getCmdLineArgs() {
        return args;
    }

    public File getInstallRoot() {
        // $instanceRoot/../..
        return instanceRoot.getParentFile().getParentFile();
    }

    public String getInstanceName() {
        return instanceName;
    }

    public String getServerConfigURL() {
        File domainXML = new File(instanceRoot, ServerEnvironmentImpl.kConfigDirName);
        domainXML = new File(domainXML, ServerEnvironmentImpl.kConfigXMLFileName);
        return domainXML.toURI().toString();
    }

    public com.sun.enterprise.config.serverbeans.Server getConfigBean() {
        return habitat.getComponent(com.sun.enterprise.config.serverbeans.Server.class);
    }

    public InitialContext getInitialContext() {
        return null;
    }

    public ClassLoader getCommonClassLoader() {
        return null;
    }

    public ClassLoader getSharedClassLoader() {
        return null;
    }

    public ClassLoader getLifecycleParentClassLoader() {
        return null;
    }

    public String getDefaultDomainName() {
        return "com.sun.appserv";
    }
    /**
     * Returns the default habitat for this instance
     * @return default habitat
     */
    public Habitat getDefaultHabitat() {
        return habitat;
        
    }
}
