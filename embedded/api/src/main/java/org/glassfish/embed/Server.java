/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

package org.glassfish.embed;

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.impl.ClassLoaderProxy;
import com.sun.enterprise.security.SecuritySniffer;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.v3.admin.CommandRunner;
import com.sun.enterprise.v3.admin.adapter.AdminConsoleAdapter;
import com.sun.enterprise.v3.server.APIClassLoaderServiceImpl;
import com.sun.enterprise.v3.server.ApplicationLifecycle;
import com.sun.enterprise.v3.server.DomainXml;
import com.sun.enterprise.v3.server.DomainXmlPersistence;
import com.sun.enterprise.v3.server.SnifferManager;
import com.sun.enterprise.v3.services.impl.LogManagerService;
import com.sun.enterprise.web.WebDeployer;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import com.sun.hk2.component.InhabitantsParser;
import com.sun.web.security.RealmAdapter;
import com.sun.web.server.DecoratorForJ2EEInstanceListener;
import java.io.*;
import java.net.MalformedURLException;
import java.util.*;
import org.glassfish.api.Startup;
import org.glassfish.api.admin.ParameterNames;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.autodeploy.AutoDeployService;
import org.glassfish.deployment.common.DeploymentContextImpl;
import org.glassfish.embed.impl.EmbeddedAPIClassLoaderServiceImpl;
import org.glassfish.embed.impl.EmbeddedApplicationLifecycle;
import org.glassfish.embed.impl.EmbeddedCommandRunner;
import org.glassfish.embed.impl.EmbeddedDomainXml;
import org.glassfish.embed.impl.EmbeddedServerEnvironment;
import org.glassfish.embed.impl.EmbeddedWebDeployer;
import org.glassfish.embed.impl.EntityResolverImpl;
import org.glassfish.embed.impl.ScatteredWarHandler;
import org.glassfish.embed.impl.SilentActionReport;
import org.glassfish.internal.api.Init;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.server.ServerEnvironmentImpl;
import org.glassfish.web.WebEntityResolver;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.Inhabitants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.embed.impl.EmbeddedModulesRegistryImpl;
import static org.glassfish.embed.ServerConstants.*;


/**
 * Entry point to the embedded GlassFish Server.
 * <p/>
 * <p/>
 * A <code>Server</code> object is constructed using an <code>EmbeddedInfo</code>
 * object.  Information about the server such as server name or HTTP port may be
 * set on {@link EmbeddedInfo}.  If no information is set, defaults are used.
 * The default server name is "server".  The default HTTP port is "8888".
 * <p/>
 * <p/>
 * After the <code>Server</code> has been created, the <code>Server</code> can
 * be started.
 * <xmp>
 * EmbeddedInfo info = new EmbeddedInfo();
 * info.setServerName("server");
 * info.setHttpPort("8080");
 * Server server = new Server(info);
 * server.start();
 * </xmp>
 * <p/>
 * TODO: the way this is done today is that the embedded API wraps the ugliness
 * of the underlying GFv3 internal abstractions, but ideally, it should be the
 * other way around &mdash; this should be the native interface inside GlassFish,
 * and application server launcher and CLI commands should be the client of this
 * API. This is how all the other sensible containers do it, like Tomcat and Jetty.
 *
 * November 2008.  Supporting multiple servers in one JVM
 * @author Kohsuke Kawaguchi
 * @author bnevins
 */
public class Server {
    /**
     * As of April 2008, several key configurations like HTTP listener
     * creation cannot be done once GFv3 starts running.
     * <p/>
     * We hide this from the client of this API by laziyl starting
     * the server, and this flag remembers which state we are in.
     */
    private boolean started;

    /*pkg-private*/ /*almost final*/ Habitat habitat;

    /**
     * To navigate around {@link #domainXml}.
     */
    private final XPath xpath = XPathFactory.newInstance().newXPath();
    //URL domainXmlUrl;
    /*pkg-private*/ URL defaultWebXml;

    // key components inside GlassFish. We access them all the time,
    // so we might just as well keep them here for ease of access.
    /*pkg-private*/ /*almost final*/ ApplicationLifecycle appLife;
    /*pkg-private*/ /*almost final*/ SnifferManager snifMan;
    /*pkg-private*/ /*almost final*/ ArchiveFactory archiveFactory;
    /*pkg-private*/ /*almost  final*/ ServerEnvironmentImpl env;
    private String id;

    /**
     * TODO constructors and startup need revamping!
     */


    Habitat getHabitat() {
        return habitat;
    }

    /**
     *
     * @return the name of this server
     */
    public String getServerName() {
        return this.info.name;
    }

    /**
     * Creates a Server object with the given EmbeddedInfo object.
     *
     * @param info EmbeddedInfo object which specifies information like server
     *             name and HTTP port
     * @throws org.glassfish.embed.EmbeddedException
     */
    public Server(EmbeddedInfo info) throws EmbeddedException {
        this.info = info;
        info.validate();
        efs = info.getFileSystem();
        setShutdownHook();
        readDomainXmlSource();

        try {
            jdbcHack();
        } catch (Exception e) {
            throw new EmbeddedException("jdbc_hack_failure", e);
        }

        createVirtualServer(createHttpListener(info.httpPort));
        addServer(info.name, this);
    }


    /**
     * @return the domainXml URL.
     * @throws EmbeddedException
     */

    public URL getDomainXmlUrl() throws EmbeddedException {
        File f = efs.getTargetDomainXml();

        try {
            return f.toURI().toURL();
        }
        catch (Exception ex) {
            throw new EmbeddedException("bad_file", f);
        }
    }

    /**
     *
     * @param url
     */
    public void setDefaultWebXml(URL url) {
        this.defaultWebXml = url;
    }

    /**
     *
     * @return
     */
    public URL getDefaultWebXml() {
        return defaultWebXml;
    }

    public EmbeddedFileSystem getFileSystem() {
        return efs;
    }
    
    /*
    URL getDomainXML() {
        return domainXmlUrl;
    }
     */

    /**
     * Sets the overall logging level for the Server.
     * @param level
     */
    public static void setLogLevel(Level level) {
        Logger.getLogger("javax.enterprise").setLevel(level);
    }

    /**
     * Tweaks the 'recipe' --- for embedded use, we'd like GFv3 to behave a little bit
     * differently from normal stand-alone use.
     * @param parser 
     * @return
     */
     protected InhabitantsParser decorateInhabitantsParser(InhabitantsParser parser) {
        // registering the server using the base class and not the current instance class
        // (GlassFish server may be extended by the user)
        parser.habitat.add(new ExistingSingletonInhabitant<Server>(Server.class, this));
        // register scattered web handler before normal WarHandler kicks in.
        Inhabitant<ScatteredWarHandler> swh = Inhabitants.create(new ScatteredWarHandler());
        parser.habitat.add(swh);
        parser.habitat.addIndex(swh, ArchiveHandler.class.getName(), null);

        // we don't want GFv3 to reconfigure all the loggers
        parser.drop(LogManagerService.class);

        // we don't need admin CLI support.
        // TODO: admin CLI should be really moved to a separate class
        parser.drop(AdminConsoleAdapter.class);

        // don't care about auto-deploy either
        try {
            Class.forName("org.glassfish.deployment.autodeploy.AutoDeployService");
            parser.drop(AutoDeployService.class);
        }
        catch (Exception e) {
            // ignore.  It may not be available
        }

        //TODO: workaround for a bug
        parser.replace(ApplicationLifecycle.class, EmbeddedApplicationLifecycle.class);

        parser.replace(APIClassLoaderServiceImpl.class, EmbeddedAPIClassLoaderServiceImpl.class);
        // we don't really parse domain.xml from disk
        parser.replace(DomainXml.class, EmbeddedDomainXml.class);

        // ... and we don't persist it either. 
        parser.replace(DomainXmlPersistence.class, EmbeddedDomainXml.class);
        try {
            // we provide our own ServerEnvironment
            EmbeddedServerEnvironment.setInstallRoot(efs.getInstallRoot());
        }
        catch (EmbeddedException ex) {
            //TODO ????
        }

        parser.replace(ServerEnvironmentImpl.class, EmbeddedServerEnvironment.class);

        {// adjustment for webtier only bundle
            parser.drop(DecoratorForJ2EEInstanceListener.class);

            // in the webtier-only bundle, these components don't exist to begin with.

            try {
                // security code needs a whole lot more work to work in the modular environment.
                // disabling it for now.
                parser.drop(SecuritySniffer.class);

                // WebContainer has a bug in how it looks up Realm, but this should work around that.
                parser.drop(RealmAdapter.class);
            } catch (LinkageError e) {
                // maybe we are running in the webtier only bundle
            }
        }

        // override the location of default-web.xml
        parser.replace(WebDeployer.class, EmbeddedWebDeployer.class);

        // override the location of cached DTDs and schemas
        parser.replace(WebEntityResolver.class, EntityResolverImpl.class);

        parser.replace(CommandRunner.class, EmbeddedCommandRunner.class);

        return parser;
    }

     /**
      *
      * @param listener
      * @return
      * @throws org.glassfish.embed.EmbeddedException
      */
     public EmbeddedVirtualServer createVirtualServer(final EmbeddedHttpListener listener)
            throws EmbeddedException{
        // the following live update code doesn't work yet due to the missing functionality in the webtier.
        mustNotBeStarted("createVirtualServer");

        DomBuilder db = onHttpService();
        db.element("virtual-server")
                .attribute("id", "server")
                .attribute("http-listeners", DEFAULT_HTTP_LISTENER_NAME)
                .attribute("hosts", "${com.sun.aas.hostName}")   // ???
                .attribute("log-file", "")
                .element("property")
                .attribute("name", "docroot")
                .attribute("value", ".");
        /**
         * Write domain.xml to target
         */

        try {
            File domainFile = efs.getTargetDomainXml();
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.transform(new DOMSource(domainXmlDocument), new StreamResult(domainFile));
        } 
        catch (Exception e) {
            throw new EmbeddedException("Failed to write domain XML", e);
        }

        return new EmbeddedVirtualServer(null);

//        try {
//            Configs configs = habitat.getComponent(Configs.class);
//
//            HttpService httpService = configs.getConfig().get(0).getHttpService();
//            return  (GFVirtualServer) ConfigSupport.apply(new SingleConfigCode<HttpService>() {
//                public Object run(HttpService param) throws PropertyVetoException, TransactionFailure {
//                    VirtualServer vs = ConfigSupport.createChildOf(param, VirtualServer.class);
//                    vs.setId("server");
//                    vs.setHttpListeners(listener.core.getId());
//                    vs.setHosts("${com.sun.aas.hostName}");
////                    vs.setDefaultWebModule("no-such-module");
//
//                    Property property =
//                        ConfigSupport.createChildOf(vs, Property.class);
//                    property.setName("docroot");
//                    property.setValue(".");
//                    vs.getProperty().add(property);
//
//
//                    param.getVirtualServer().add(vs);
//                    return new GFVirtualServer(vs);
//                }
//            }, httpService);
//        } catch(TransactionFailure e) {
//            throw new GFException(e);
//        }
    }

     /**
      *
      * @param listenerPort
      * @return
      * @throws org.glassfish.embed.EmbeddedException
      */
     public EmbeddedHttpListener createHttpListener(final int listenerPort)
            throws EmbeddedException {
        // the following live update code doesn't work yet due to the missing functionality in the webtier.
        mustNotBeStarted("createHttpListener");

        onHttpService().element("http-listener")
                //hardcoding to http-listner-1 should not be a requirment, but the id is used to find the right Inhabitant
                .attribute("id", DEFAULT_HTTP_LISTENER_NAME)
                .attribute("address", "0.0.0.0")
                .attribute("port", listenerPort)
                .attribute("default-virtual-server", "server")
                .attribute("server-name", "")
                .attribute("enabled", true);

        return new EmbeddedHttpListener(String.valueOf(listenerPort), null);

//        try {
//            Configs configs = habitat.getComponent(Configs.class);
//
//            HttpService httpService = configs.getConfig().get(0).getHttpService();
//            return (GFHttpListener)ConfigSupport.apply(new SingleConfigCode<HttpService>() {
//                public Object run(HttpService param) throws PropertyVetoException, TransactionFailure {
//                    HttpListener newListener = ConfigSupport.createChildOf(param, HttpListener.class);
//                    newListener.setId("http-listener-"+listenerPort);
//                    newListener.setAddress("127.0.0.1");
//                    newListener.setPort(String.valueOf(listenerPort));
//                    newListener.setDefaultVirtualServer("server");
//                    newListener.setEnabled("true");
//
//                    param.getHttpListener().add(newListener);
//                    return new GFHttpListener(newListener);
//                }
//            }, httpService);
//        } catch(TransactionFailure e) {
//            throw new GFException(e);
//        }
    }

    private DomBuilder onHttpService() {
        try {
            return new DomBuilder((Element) xpath.evaluate("//http-service", domainXmlDocument, XPathConstants.NODE));
        } catch (XPathExpressionException e) {
            throw new AssertionError(e);    // impossible
        }
    }

    /**
     * Starts the server if hasn't done so already. 
     * Necessary to work around the live HTTP listener update.
     * It is an error to call this more than once.
     * @throws EmbeddedException
     */
    public void start() throws EmbeddedException{
        if (started)
            throw new EmbeddedException("already_started");

        started = true;

        try {
            
            EmbeddedModulesRegistryImpl reg = new EmbeddedModulesRegistryImpl();
            StartupContext startupContext = new StartupContext(efs.getInstallRoot(), new String[0]);


            // !!!!!!!!!!!!!!!!!!!!!!!!!
            // ANONYMOUS CLASS HERE!!
            // TODO
            // !!!!!!!!!!!!!!!!!!!!!!!!!
            Main main = new Main() {
                @Override
                protected InhabitantsParser createInhabitantsParser(Habitat habitat1) {
                    return decorateInhabitantsParser(super.createInhabitantsParser(habitat1));
                }

            };


            habitat = main.launch(reg, startupContext);
            appLife = habitat.getComponent(ApplicationLifecycle.class);
            snifMan = habitat.getComponent(SnifferManager.class);
            archiveFactory = habitat.getComponent(ArchiveFactory.class);
            env = habitat.getComponent(ServerEnvironmentImpl.class);
        } catch (Exception e) {
            throw new EmbeddedException(e);
        }

    }

    /**
     * Deploys WAR/EAR/RAR/etc to this Server.
     *
     * @param archive
     * @return always non-null. Represents the deployed application.
     * @throws EmbeddedException
     */
    public Application deploy(File archive) throws EmbeddedException {
        try {
            mustBeStarted("deploy");
            ReadableArchive a = archiveFactory.openArchive(archive);

            if (!archive.isDirectory()) {

                ArchiveHandler h = appLife.getArchiveHandler(a);
                File appDir = new File(efs.getAppsDir(), a.getName());
                FileUtils.whack(appDir);
                appDir.mkdirs();
                h.expand(a, archiveFactory.createArchive(appDir));
                a.close();
                a = archiveFactory.openArchive(appDir);
            }
            return deploy(a);
        }
        catch (EmbeddedException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new EmbeddedException(ex);
        }

    }

    /**
     * Deploys a {@link ReadableArchive} to this Server.
     * <p/>
     * <p/>
     * This overloaded version of the deploy method is for advanced users.
     * It allows the caller to deploy an application in a non-standard layout.
     * <p/>
     * <p/>
     * The deployment uses the {@link ReadableArchive#getName() archive name}
     * as the context path.
     *
     * @param a
     * @return The object that represents a deployed application.
     *         Never null.
     * @throws EmbeddedException
     */
    public Application deploy(ReadableArchive a) throws EmbeddedException {
        return deploy(a, null);
    }

    /**
     * Deploys a {@link ReadableArchive} to this Server.
     * <p/>
     * <p/>
     * This overloaded version of the deploy method is for advanced users.
     * It allows you specifying additional parameters to be passed to the deploy command
     *
     * @param a
     * @param params
     * @return
     * @throws EmbeddedException
     */
    public Application deploy(ReadableArchive a, Properties params)  throws EmbeddedException {
        try {
            mustBeStarted("deploy");

            ArchiveHandler h = appLife.getArchiveHandler(a);

            // now prepare sniffers
            //is this required?
            ClassLoader parentCL = createSnifferParentCL(null, snifMan.getSniffers());

            ClassLoader cl = h.getClassLoader(parentCL, a);
            Collection<Sniffer> activeSniffers = snifMan.getSniffers(a, cl);

            // TODO: we need to stop this totally type-unsafe way of passing parameters
            if (params == null) {
                params = new Properties();
            }
            params.put(ParameterNames.NAME, a.getName());
            params.put(ParameterNames.ENABLED, "true");
            final DeploymentContextImpl deploymentContext = new DeploymentContextImpl(Logger.getAnonymousLogger(), a, params, env);

            SilentActionReport r = new SilentActionReport();
            ApplicationInfo appInfo = appLife.deploy(activeSniffers, deploymentContext, r);
            r.check();

            return new Application(this, appInfo, deploymentContext);
        }
        catch (EmbeddedException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new EmbeddedException(ex);
        }
    }

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
    public ClassLoader createSnifferParentCL(ClassLoader parent, Collection<Sniffer> sniffers) {
        // Use the sniffers class loaders as the delegates to the parent class loader.
        // This will allow any class loadable by the sniffer (therefore visible to the sniffer
        // class loader) to be also loadable by the archive's class loader.
        ClassLoaderProxy cl = new ClassLoaderProxy(new URL[0], parent);
        for (Sniffer sniffer : sniffers) {
            cl.addDelegate(sniffer.getClass().getClassLoader());
        }
        return cl;

    }


    /**
     * Convenience method to deploy a scattered war archive on a given virtual server
     * and using the specified context root.
     *
     * @param war           the scattered war
     * @param contextRoot   the context root to use
     * @param virtualServer the virtual server ID
     * @return
     * @throws EmbeddedException
     */
    public Application deployWar(ScatteredWar war, String contextRoot, String virtualServer) throws EmbeddedException {
        Properties params = new Properties();
        if (virtualServer == null) {
            virtualServer = "server";
        }
        params.put(ParameterNames.VIRTUAL_SERVERS, virtualServer);
        if (contextRoot != null) {
            params.put(ParameterNames.CONTEXT_ROOT, contextRoot);
        }
        return deploy(war, params);
    }

    /**
     * Convenience method to deploy a scattered war archive on the default virtual server.
     *
     * @param war         the archive
     * @param contextRoot the context root to use
     * @return 
     * @throws EmbeddedException
     */
    public Application deployWar(ScatteredWar war, String contextRoot) throws EmbeddedException {
        return deployWar(war, contextRoot, null);
    }

    /**
     * Convenience method to deploy a scattered war archive on the default virtual server
     * (as defined by the embedded domain.xml) and using the root "/" context.
     *
     * @param war the scattered war
     * @return
     * @throws EmbeddedException
     */
    public Application deployWar(ScatteredWar war) throws EmbeddedException {
        return deployWar(war, null, null);
    }

    /**
     * Stops the running server.
     * @throws EmbeddedException
     */
    public void stop() throws EmbeddedException {
        mustBeStarted("stop");

        for (Inhabitant<? extends Startup> svc : habitat.getInhabitants(Startup.class)) {
            svc.release();
        }

        for (Inhabitant<? extends Init> svc : habitat.getInhabitants(Init.class)) {
            svc.release();
        }

        started = false;
    }


    /**
     * Returns the <code>Server</code> object specified by the id.
     * Returns null if the server does not exist.
     *
     * @param id  name of the server
     * @return    the server specified by id, null if it doesn't exist
     */
    public static Server getServer(String id) {
        return servers.get(id);
    }
    ////////////////////////////////////////////////////////
    /////////////   private methods   //////////////////////
    ////////////////////////////////////////////////////////

    private void readDomainXmlSource() throws EmbeddedException {
        URL sourceUrl = efs.getSourceDomainXml();

        if(sourceUrl == null)
            throw new EmbeddedException("bad_domain_xml");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            domainXmlDocument = dbf.newDocumentBuilder().parse(sourceUrl.toExternalForm());
        }
        catch (Exception ex) {
            // TODO ??? better string here....
            throw new EmbeddedException("parser_error", ex);
        }
    }

    private static void addServer(String name, Server server) {
        servers.put(name, server);
    }

    private void setShutdownHook() {
        //final String msg = strings.get("serverStopped", info.getType());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    // logger won't work anymore...
                    //System.out.println(msg);
                    // TODO TEMP
                    efs.cleanup();
                }
                catch (EmbeddedException ex) {
                    System.out.println("Could not cleanup files.");
                    // we can't do anyhting!
                }
            }});
    }

    /**
     * Nov 4, 2008 bnevins
     * temporary horrible hack.  Core v3 looks at a specific location in the filesystem
     * for magical files.  Today, I can not change core, so I'm hacking from the embedded
     * side.  This will go away before the final release.
     *
     * Hint: com.sun.appserv.connectors.internal.api.ConnectorsUtil.getSystemModuleLocation
     */
    private void jdbcHack() throws IOException {
        // NASTY CODE!!!!
        //File root = EmbeddedFileSystem.getInstallRoot();
        String cpName = "__cp_jdbc_ra/META-INF";
        String dsName = "__ds_jdbc_ra/META-INF";
        String xaName = "__xa_jdbc_ra/META-INF";

        // the directories on disk
        File cp = new File(ConnectorsUtil.getSystemModuleLocation(cpName));
        File ds = new File(ConnectorsUtil.getSystemModuleLocation(dsName));
        File xa = new File(ConnectorsUtil.getSystemModuleLocation(xaName));

        // create them if necessary
        cp.mkdirs();
        ds.mkdirs();
        xa.mkdirs();

        // these are the magic files that core JDBC code wants
        cp = new File(cp, "ra.xml");
        ds = new File(ds, "ra.xml");
        xa = new File(xa, "ra.xml");

        // if they already exist -- we are done!
        if(cp.exists() && ds.exists() && xa.exists())
            return;

        Class clazz = getClass();
        final String base = "/org/glassfish/embed";

        BufferedReader cpr = new BufferedReader(new InputStreamReader(
                clazz.getResourceAsStream(base + "/" + cpName + "/ra.xml")));
        BufferedReader dsr = new BufferedReader(new InputStreamReader(
                clazz.getResourceAsStream(base + "/" + dsName + "/ra.xml")));
        BufferedReader xar = new BufferedReader(new InputStreamReader(
                clazz.getResourceAsStream(base + "/" + xaName + "/ra.xml")));

        copy(cpr, cp);
        copy(dsr, ds);
        copy(xar, xa);
    }

    private static void copy(BufferedReader in, File out) throws FileNotFoundException, IOException {
        // If we did regular byte copying -- we would have a horrible mess on Windows
        // this way we get the right line termintors on any platform.
        PrintWriter pw = new PrintWriter(out);

        for(String s = in.readLine(); s != null; s = in.readLine()) {
            pw.println(s);
        }

        pw.close();
        in.close();
    }

    private boolean isStarted() {
        return started;
    }

    private void mustBeStarted(String methodName) throws EmbeddedException {
        if(!isStarted()) {
            throw new EmbeddedException("not_started", methodName);
        }
    }

    private void mustNotBeStarted(String methodName) throws EmbeddedException {
        if(isStarted()) {
            throw new EmbeddedException("should_not_be_started", methodName);
        }
    }

    ////////////////////////////////////////////////////////
    /////////////   private variables //////////////////////
    ////////////////////////////////////////////////////////

    /**
     * Work around until the live HTTP listener support comes back.
     */
    private Document                    domainXmlDocument;
    private EmbeddedFileSystem          efs;
    private EmbeddedInfo                info;
    private static Map<String,Server>   servers = new HashMap<String,Server>();
}
