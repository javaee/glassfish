/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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


package org.glassfish.osgiweb;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.grizzly.config.dom.NetworkListener;
import org.glassfish.api.ActionReport;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.api.Globals;
import org.glassfish.server.ServerEnvironmentImpl;
import org.osgi.framework.Bundle;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

/**
 * This is a stateful service. This is responsible for deployment
 * of artifacts in JavaEE runtime.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class JavaEEDeploymentRequest
{
    private static final Logger logger =
            Logger.getLogger(JavaEEUndeploymentRequest.class.getPackage().getName());

    private ActionReport reporter;
    private Bundle b;
    private boolean dirDeployment;
    private Deployment deployer;
    private ArchiveFactory archiveFactory;
    private ServerEnvironmentImpl env;
    private ReadableArchive archive;
    private OSGiDeploymentContextImpl dc;

    public JavaEEDeploymentRequest(Deployment deployer,
                                   ArchiveFactory archiveFactory,
                                   ServerEnvironmentImpl env,
                                   ActionReport reporter,
                                   Bundle b)
    {
        this.deployer = deployer;
        this.archiveFactory = archiveFactory;
        this.env = env;
        this.reporter = reporter;
        this.b = b;
    }

    /**
     * Deploys a web application bundle in GlassFish Web container.
     * It properly rolls back if something goes wrong.
     */
    public OSGiWebContainer.OSGiApplicationInfo execute()
    {
        // This is where the fun is...
        try
        {
            prepare();
        }
        catch (Exception e)
        {
            // if prepare failed, there is nothing to undo.
            // populate report and return
            reporter.failure(logger,
                    "Failed while preparing to deploy bundle " + b, e);
            return null;
        }

        // Now actual deployment begins
        return deploy();
    }

    private void prepare() throws Exception
    {
        // The steps are described below:
        // 1. Create an Archive from the bundle
        //    - If the bundle has been installed with reference: scheme,
        //    get hold hold of the underlying file and read from it, else
        //    use the bundle directly to create the archive.
        // 2. Prepare a context for deployment. This includes setting up
        // various deployment options, setting up of an ArchiveHandler,
        // expansion of the archive, etc.
        // 3. Finally deploy and store the result in our inmemory map.

        archive = new OSGiBundleArchive(b);

        // Set up a deployment context
        OpsParams opsParams = getDeployParams(archive);
        dc = new OSGiDeploymentContextImpl(
                reporter,
                logger,
                archive,
                opsParams,
                env,
                b);

        // expand if necessary, else set directory deployment to true
        expandIfNeeded();
    }

    private OSGiWebContainer.OSGiApplicationInfo deploy()
    {
        // Need to declare outside to do proper cleanup of target dir
        // when deployment fails. We can't rely on exceptions as
        // deployer.deploy() eats most of the exceptions.
        ApplicationInfo appInfo = null;
        try
        {
            appInfo = deployer.deploy(dc);
            if (appInfo != null)
            {
                OSGiWebContainer.OSGiApplicationInfo osgiAppInfo = new OSGiWebContainer.OSGiApplicationInfo();
                osgiAppInfo.appInfo = appInfo;
                osgiAppInfo.isDirectoryDeployment = dirDeployment;
                osgiAppInfo.bundle = b;
                return osgiAppInfo;
            }
            else
            {
                logger.logp(Level.INFO, "JavaEEDeploymentRequest",
                        "deploy", "failed to deploy {0}", new Object[]{b});
                reporter.failure(logger, "failed to deploy " + b);
                return null;
            }
        }
        finally
        {
            if (!dirDeployment && appInfo == null)
            {
                try
                {
                    File dir = dc.getSourceDir();
                    assert (dir.isDirectory());
                    if (FileUtils.whack(dir)) {
                        logger.logp(Level.INFO, "JavaEEDeploymentRequest", "deploy",
                                "Deleted {0}", new Object[]{dir});
                    } else {
                        logger.logp(Level.WARNING, "JavaEEDeploymentRequest", "deploy", "Unable to delete {0} ", new Object[]{dir});
                    }
                }
                catch (Exception e2)
                {
                    logger.logp(Level.WARNING, "JavaEEDeploymentRequest", "deploy",
                            "Exception while cleaning up target directory.", e2);
                    // don't throw this anymore
                }
            }
        }
    }

    private void expandIfNeeded() throws IOException
    {
        // Try to obtain a handle to the underlying archive.
        // First see if it is backed by a file or a directory, else treat
        // it as a generic bundle.
        File file = makeFile(dc.getSource());

        // expand if necessary, else set directory deployment to true
        dirDeployment = file != null && file.isDirectory();
        if (dirDeployment)
        {
            logger.logp(Level.FINE, "JavaEEDeploymentRequest", "expandIfNeeded",
                    "Archive is already expanded at = {0}", new Object[]{file});
            return;
        }

        // ok we need to explode the directory somwhere and
        // remember to delete it on shutdown
        // We can't use archive name as it can contain file separator, so
        // we shall use a temporary name
        File tmpFile = File.createTempFile("osgiapp", "");

        // create a directory in place of the tmp file.
        tmpFile.delete();
        tmpFile = new File(tmpFile.getAbsolutePath());
        tmpFile.deleteOnExit();
        if (tmpFile.mkdirs())
        {
            WritableArchive targetArchive = archiveFactory.createArchive(tmpFile);
            new OSGiWarHandler().expand(archive, targetArchive, dc);
            logger.logp(Level.INFO, "JavaEEDeploymentRequest", "expand",
                    "Expanded at {0}", new Object[]{targetArchive.getURI()});
            archive = archiveFactory.openArchive(tmpFile);
            dc.setSource(archive); // set the new archive as source.
        } else {
            throw new IOException("Not able to expand " + archive.getName() +
                    " in " + tmpFile);
        }
    }

    /**
     * Return a File object that corresponds to this archive.
     * return null if it can't determine the underlying file object.
     *
     * @param a The archive
     * @return
     */
    public static File makeFile(ReadableArchive a)
    {
        try
        {
            return new File(a.getURI());
        }
        catch (Exception e)
        {
            // Ignore, if we can't convert
        }
        return null;
    }

    private OpsParams getDeployParams(ReadableArchive archive) throws Exception
    {
        DeployCommandParameters parameters = new DeployCommandParameters();
        parameters.name = archive.getName();

        // Set the contextroot explicitly, else it defaults to name.
        try
        {
            // We expect WEB_CONTEXT_PATH to be always present.
            // This is mandated in the spec.
            parameters.contextroot = archive.getManifest().
                    getMainAttributes().getValue(Constants.WEB_CONTEXT_PATH);
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
        parameters.enabled = Boolean.TRUE;
        parameters.origin = DeployCommandParameters.Origin.deploy;
        parameters.force = false;
        parameters.virtualservers = getDefaultVirtualServer();
        return parameters;
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
        NetworkListener nl = Globals.get(NetworkListener.class);
        return nl.findHttpProtocol().getHttp().getDefaultVirtualServer();
    }
}
