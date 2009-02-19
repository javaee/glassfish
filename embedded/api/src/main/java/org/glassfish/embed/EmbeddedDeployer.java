
package org.glassfish.embed;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.module.impl.ClassLoaderProxy;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.v3.server.ApplicationLifecycle;
import com.sun.enterprise.v3.server.SnifferManager;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.*;
import org.glassfish.api.admin.ParameterNames;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentContextImpl;
import org.glassfish.embed.EmbeddedException;
import org.glassfish.embed.impl.SilentActionReport;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.component.Habitat;

/**
 * <code>EmbeddedDeployer</code> is used to deploy applications to {@link Server} 
 * <p>
 * <code>Server</code> must be started before calling {@link getDeployer} on
 * <code>Server</code> to get an instance of <code>EmbeddedDeployer</code>.
 *
 * @author bnevins
 */
public class EmbeddedDeployer {
    EmbeddedDeployer(Server server) throws EmbeddedException {
        this.server = server;
        mustBeStarted("EmbeddedDeployer Constructor");
        Habitat habitat = server.getHabitat();
        archiveFactory = habitat.getComponent(ArchiveFactory.class);
        appLife = server.getAppLife();
        efs = server.getInfo().getFileSystem();
        snifferManager = habitat.getComponent(SnifferManager.class);
        serverEnvironment = habitat.getComponent(ServerEnvironmentImpl.class);
    }


    /**
     * Deploys a WAR to <code>Server</code>.
     *
     * @param archive pathname of WAR file or directory
     * @throws EmbeddedException
     */

    public void deploy(File archive) throws EmbeddedException {
        try {
            mustBeStarted("deploy(File)");
            ReadableArchive a = archiveFactory.openArchive(archive);

            // TODO  WTF code.  For now just port from Server to here
            // WTF WTF WTF WTF
            // WTF WTF WTF WTF
            // WTF WTF WTF WTF
            if (!archive.isDirectory()) {
                ArchiveHandler h = appLife.getArchiveHandler(a);
                File appDir = new File(efs.getApplicationsDir(), a.getName());
                FileUtils.whack(appDir);
                appDir.mkdirs();
                h.expand(a, archiveFactory.createArchive(appDir));
                a.close();
                a = archiveFactory.openArchive(appDir);
            }
            deploy(a, new Properties());
        }
        catch (EmbeddedException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new EmbeddedException(ex);
        }
    }

    /**
     * Deploys a {@link org.glassfish.api.deployment.archive.ReadableArchive} to this Server.
     * <p/>
     * <p/>
     * This overloaded version of the deploy method is for advanced users.
     * It allows you specifying additional parameters to be passed to the deploy command
     *
     * @param a WAR as a <code>ReadableArchive</code>
     * @param params parameters of the deploy command
     * @throws EmbeddedException
     */
    public void deploy(ReadableArchive a, Properties params) throws EmbeddedException {
        try {
            mustBeStarted("deploy(ReadableArchive, Properties)");
            ArchiveHandler h = appLife.getArchiveHandler(a);

            // now prepare sniffers
            //is this required?
            ClassLoader parentCL = createSnifferParentCL(null, snifferManager.getSniffers());

            ClassLoader cl = h.getClassLoader(parentCL, a);
            Collection<Sniffer> activeSniffers = snifferManager.getSniffers(a, cl);

            // TODO: we need to stop this totally type-unsafe way of passing parameters
            if (params == null) {
                params = new Properties();
            }
            params.put(ParameterNames.NAME, a.getName());
            params.put(ParameterNames.ENABLED, "true");
            final DeploymentContextImpl deploymentContext = new DeploymentContextImpl(Logger.getAnonymousLogger(), a, params, serverEnvironment);

            SilentActionReport r = new SilentActionReport();
            ApplicationInfo appInfo = appLife.deploy(activeSniffers, deploymentContext, r);
            r.check();
            addApp(new Application(appInfo, deploymentContext));
            //return new Application(this, appInfo, deploymentContext);
        }
        catch (EmbeddedException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new EmbeddedException(ex);
        }
    }
    /**
     * Convenience method to deploy a scattered war archive on a given virtual server
     * and using the specified context root.
     *
     * @param war           the scattered war
     * @param contextRoot   the context root to use
     * @param virtualServer the virtual server ID
     * @throws EmbeddedException
     */
    public void deployScattered(ScatteredWar war, String contextRoot, String virtualServer) throws EmbeddedException {
        Properties params = new Properties();
        if (virtualServer == null) {
            virtualServer = "server";
        }
        params.put(ParameterNames.VIRTUAL_SERVERS, virtualServer);
        if (contextRoot != null) {
            params.put(ParameterNames.CONTEXT_ROOT, contextRoot);
        }
        deploy(war, params);
    }
     /*
      * undeploy the app that was deployed by this deployer with the given name.
      * @param name the name of the app
      * @throws org.glassfish.embed.EmbeddedException if any errors or the app does not exist
     */

    public void undeploy(String name) throws EmbeddedException {
        try {
            for(Application anApp : appList) {
                if(name.equals(anApp.getName())) {
                    anApp.undeploy();
                    appList.remove(anApp);
                    return;
                }
            }
        }
        catch(EmbeddedException ee) {
            throw ee;
        }

        throw new EmbeddedException("undeploy_error", name);
    }

    /**
     * undeploy all apps that were deployed by this deployer
     * @throws org.glassfish.embed.EmbeddedException
     */
    public void undeploy() throws EmbeddedException {
        for(Application anApp : appList) {
            anApp.undeploy();
        }
        appList.clear();
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////           END public API       ////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Sets up a parent classloader that will be used to create a temporary application
     * class loader to load classes from the archive before the Deployers are available.
     * Sniffer.handles() method takes a class loader as a parameter and this class loader
     * needs to be able to load any class the sniffer load themselves.
     *
     * @param parent   parent class loader for this class loader
     * @param sniffers sniffer instances
     * @return a class loader with visibility on all classes loadable by classloaders.
     */
    private ClassLoader createSnifferParentCL(ClassLoader parent, Collection<Sniffer> sniffers) {
        // Use the sniffers class loaders as the delegates to the parent class loader.
        // This will allow any class loadable by the sniffer (therefore visible to the sniffer
        // class loader) to be also loadable by the archive's class loader.
        ClassLoaderProxy cl = new ClassLoaderProxy(new URL[0], parent);
        for (Sniffer sniffer : sniffers) {
            cl.addDelegate(sniffer.getClass().getClassLoader());
        }
        return cl;
    }

    private void mustBeStarted(String method) throws EmbeddedException {
        server.mustBeStarted(CLASS_NAME + method);
        
    }

    private void addApp(Application anApp) {
        appList.add(anApp);
    }

    private Server                  server;
    private ArchiveFactory          archiveFactory;
    private ApplicationLifecycle    appLife;
    private EmbeddedFileSystem      efs;
    private SnifferManager          snifferManager;
    private ServerEnvironmentImpl   serverEnvironment;
    private static final String     CLASS_NAME = "EmbeddedDeployer.";
    private List<Application>       appList = new LinkedList<Application>();

    private class Application{
        Application(ApplicationInfo app, DeploymentContext deploymentContext) {
            //this.owner = owner;
            this.app = app;
            this.deploymentContext = deploymentContext;
        }

        void undeploy() throws EmbeddedException{
            SilentActionReport r = new SilentActionReport();
            server.getAppLife().undeploy(app.getName(),deploymentContext, r);
            r.check();
        }

        String getName() {
            return app.getName();
        }
        //private Server owner;
        private final ApplicationInfo app;
        private final DeploymentContext deploymentContext;
    }
}
/***
 *
 *

 */