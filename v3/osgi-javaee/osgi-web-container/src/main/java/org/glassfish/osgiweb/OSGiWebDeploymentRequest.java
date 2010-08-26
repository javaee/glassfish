/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.osgiweb;

import org.glassfish.osgijavaeebase.OSGiDeploymentRequest;
import org.glassfish.osgijavaeebase.OSGiDeploymentContext;
import org.glassfish.api.ActionReport;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.server.ServerEnvironmentImpl;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.api.Globals;
import org.osgi.framework.Bundle;

import java.util.logging.Logger;
import java.util.List;
import java.util.StringTokenizer;
import java.io.IOException;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.config.serverbeans.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiWebDeploymentRequest extends OSGiDeploymentRequest {

    public OSGiWebDeploymentRequest(Deployment deployer, ArchiveFactory archiveFactory, ServerEnvironmentImpl env, ActionReport reporter, Bundle b) {
        super(deployer, archiveFactory, env, reporter, b);
    }

    protected OSGiDeploymentContext getDeploymentContextImpl(ActionReport reporter, Logger logger, ReadableArchive archive, OpsParams opsParams, ServerEnvironmentImpl env, Bundle b) throws Exception {
        return new OSGiWebDeploymentContext(reporter, logger, archive, opsParams, env, b);
    }

    @Override
    protected WAB makeArchive() {
        Bundle host = getBundle();
        Bundle[] fragments = getPackageAdmin().getFragments(host);
        return new WAB(host, fragments);
    }

    private PackageAdmin getPackageAdmin() {
        BundleContext ctx = BundleReference.class.cast(getClass().getClassLoader()).getBundle().getBundleContext();
        return PackageAdmin.class.cast(ctx.getService(ctx.getServiceReference(PackageAdmin.class.getName())));
    }

    @Override
    protected DeployCommandParameters getDeployParams() throws Exception {
        DeployCommandParameters parameters = super.getDeployParams();
        // Set the contextroot explicitly, else it defaults to name.
        try
        {
            // We expect WEB_CONTEXT_PATH to be always present.
            // This is mandated in the spec.
            parameters.contextroot = getArchive().getManifest().
                    getMainAttributes().getValue(org.glassfish.osgiweb.Constants.WEB_CONTEXT_PATH);
        }
        catch (IOException e)
        {
            // ignore and continue
        }
        if (parameters.contextroot == null || parameters.contextroot.length() == 0)
        {
            throw new Exception(Constants.WEB_CONTEXT_PATH +
                    " manifest header is mandatory");
        }
        parameters.virtualservers = getVirtualServers();
        return parameters;
    }

    private String getVirtualServers() {
        String virtualServers = null;
        try {
            virtualServers = getArchive().getManifest().getMainAttributes().getValue(
                    org.glassfish.osgiweb.Constants.VIRTUAL_SERVERS);
        } catch (Exception e) {
            // ignore
        }
        if (virtualServers == null) virtualServers = getDefaultVirtualServer();
        StringTokenizer st = new StringTokenizer(virtualServers);
        if (st.countTokens() > 1) {
            throw new IllegalArgumentException("Currently, we only support deployment to one virtual server.");
        }
        return virtualServers;
    }
    /**
     * @return comma-separated list of all defined virtual servers (exclusive
     * of __asadmin)
     */
    private String getAllVirtualServers() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        Domain domain = Globals.get(Domain.class);
        String target = "server"; // Need to understand how to dynamically obtains this
        Server server = domain.getServerNamed(target);
        if (server != null) {
            Config config = domain.getConfigs().getConfigByName(
                server.getConfigRef());
            if (config != null) {
                HttpService httpService = config.getHttpService();
                if (httpService != null) {
                    List<VirtualServer> hosts = httpService.getVirtualServer();
                    if (hosts != null) {
                        for (VirtualServer host : hosts) {
                            if (("__asadmin").equals(host.getId())) {
                                continue;
                            }
                            if (first) {
                                sb.append(host.getId());
                                first = false;
                            } else {
                                sb.append(",");
                                sb.append(host.getId());
                            }
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * @return the dafault virtual server
     */
    private String getDefaultVirtualServer() {
        com.sun.grizzly.config.dom.NetworkListener nl = Globals.get(com.sun.grizzly.config.dom.NetworkListener.class);
        return nl.findHttpProtocol().getHttp().getDefaultVirtualServer();
    }
}
