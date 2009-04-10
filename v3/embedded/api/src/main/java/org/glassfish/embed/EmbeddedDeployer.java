
package org.glassfish.embed;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.module.impl.ClassLoaderProxy;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.v3.server.ApplicationLifecycle;
import org.glassfish.internal.deployment.SnifferManager;
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
 * @author Byron Nevins
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
     * Deploys an archive to <code>Server</code>.
     *
     * @param archive pathname of an archive file or directory
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
                // TODO ... h.expand(a, archiveFactory.createArchive(appDir));
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
     * It allows you to specify additional parameters to be passed to the deploy command.
     * If you do not specify the virtual server, "server" is used is used as the default.
     * If you do not specify the context root, application name is used as the default.
     *
     * <xmp>
     * Collection<URL> coll = new java.util.ArrayList();
     * coll.add(new URL("file:///C:\\samples\\hello\\WEB-INF\\classes"));
     * ReadableArchive archive = (ReadableArchive)new ScatteredArchive("testwar", new File("C:\\samples\\hello"), null, coll);
     * Properties params = new Properties();
     * params.put(ParameterNames.VIRTUAL_SERVERS, "myServer");
     * params.put(ParameterNames.CONTEXT_ROOT, "scatter");
     * server.getDeployer().deploy(archive, params);
     * </xmp>
     *
     *  List of parameters in <code>org.glassfish.api.admin.ParameterNames</code>:
     *  
     * <xmp>
     * public static final String NAME = "name";
     * public static final String COMPONENT = "component";
     * public static final String VIRTUAL_SERVERS = "virtualservers";
     * public static final String CONTEXT_ROOT = "contextroot";
     * public static final String PREVIOUS_CONTEXT_ROOT = "previous_contextroot";
     * public static final String LIBRARIES = "libraries";
     * public static final String DIRECTORY_DEPLOYED = "directorydeployed";
     * public static final String LOCATION = "location";
     * public static final String ENABLED = "enabled";
     * public static final String PRECOMPILE_JSP = "precompilejsp";
     * public static final String DEPLOYMENT_PLAN = "deploymentplan";
     * </xmp>
     *
     * For more information about the deploy options, see <a href="http://docs.sun.com/app/docs/doc/820-4497/deploy-1?a=view">deploy command</a>.
     *
     * @param a archive as a <code>ReadableArchive</code>
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

            //ClassLoader cl = h.getClassLoader(parentCL, a);
            ClassLoader cl = null; // TODO
            Collection<Sniffer> activeSniffers = snifferManager.getSniffers(a, cl);

            // TODO: we need to stop this totally type-unsafe way of passing parameters
            if (params == null) {
                params = new Properties();
            }
            params.put(ParameterNames.NAME, a.getName());
            params.put(ParameterNames.ENABLED, "true");
            
            //TODO...
            /*
            final DeploymentContextImpl deploymentContext = 
                    new DeploymentContextImpl( Logger.getAnonymousLogger(), a, params, serverEnvironment);

            SilentActionReport r = new SilentActionReport();
            //ApplicationInfo appInfo = appLife.deploy(activeSniffers, deploymentContext, r);
            r.check();
            addApp(new Application(appInfo, deploymentContext));
             */
        }
        catch (EmbeddedException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new EmbeddedException(ex);
        }
    }
    /**
     * Deploy a scattered archive on a given virtual server
     * and using the specified context root.
     *
     * @param archive       the scattered archive
     * @param contextRoot   the context root to use
     * @param virtualServer the virtual server ID
     * @throws EmbeddedException
     */
    void deploy(ReadableArchive archive, String contextRoot, String virtualServer) throws EmbeddedException {
        Properties params = new Properties();
        if (virtualServer == null) {
            virtualServer = "server";
        }
        params.put(ParameterNames.VIRTUAL_SERVERS, virtualServer);
        if (contextRoot != null) {
            params.put(ParameterNames.CONTEXT_ROOT, contextRoot);
        }
        deploy(archive, params);
    }
    /**
     * Convenience method to deploy a scattered archive
     * using the specified context root.  It will be deployed to the default
     * virtual server
     *
     * @param archive       the scattered archive
     * @param contextRoot   the context root to use
     * @throws EmbeddedException
     */
    void deploy(ReadableArchive archive, String contextRoot) throws EmbeddedException {
        deploy(archive, contextRoot, null);
    }

    /**
     * Convenience method to deploy a scattered war archive
     * using the name of the app as the context root.
     * It will be deployed to the default
     * virtual server
     * @param war           the scattered war
     * @throws EmbeddedException
     */
    //public void deployScattered(ScatteredWar war) throws EmbeddedException {
    //    deployScattered(war, null, null);
    //}
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
    public void undeployAll() throws EmbeddedException {
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
            //TODO
            //server.getAppLife().undeploy(app.getName(),deploymentContext, r);
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