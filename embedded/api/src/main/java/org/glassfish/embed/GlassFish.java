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

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.impl.ModulesRegistryImpl;
import com.sun.enterprise.security.SecuritySniffer;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.v3.admin.adapter.AdminConsoleAdapter;
import com.sun.enterprise.v3.data.ApplicationInfo;
import com.sun.enterprise.v3.deployment.DeployCommand;
import com.sun.enterprise.v3.deployment.DeploymentContextImpl;
import com.sun.enterprise.v3.server.ApplicationLifecycle;
import com.sun.enterprise.v3.server.DomainXml;
import com.sun.enterprise.v3.server.DomainXmlPersistence;
import com.sun.enterprise.v3.server.ServerEnvironment;
import com.sun.enterprise.v3.server.SnifferManager;
import com.sun.enterprise.v3.services.impl.LogManagerService;
import com.sun.enterprise.web.WebDeployer;
import com.sun.hk2.component.InhabitantsParser;
import com.sun.web.security.RealmAdapter;
import com.sun.web.server.DecoratorForJ2EEInstanceListener;
import org.glassfish.api.Startup;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.autodeploy.AutoDeployService;
import org.glassfish.embed.impl.DomainXml2;
import org.glassfish.embed.impl.EntityResolverImpl;
import org.glassfish.embed.impl.ProxyModuleDefinition;
import org.glassfish.embed.impl.ServerEnvironment2;
import org.glassfish.embed.impl.SilentActionReport;
import org.glassfish.embed.impl.WebDeployer2;
import org.glassfish.embed.impl.DomainXmlHolder;
import org.glassfish.internal.api.Init;
import org.glassfish.web.WebEntityResolver;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.Inhabitants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point to the embedded GlassFish.
 *
 * <p>
 * TODO: the way this is done today is that the embedded API wraps the ugliness
 * of the underlying GFv3 internal abstractions, but ideally, it should be the
 * other way around &mdash; this should be the native interface inside GlassFish,
 * and application server launcher and CLI commands should be the client of this
 * API. This is how all the other sensible containers do it, like Tomcat and Jetty.
 *
 * @author Kohsuke Kawaguchi
 */
public class GlassFish {
    /**
     * As of April 2008, several key configurations like HTTP listener
     * creation cannot be done once GFv3 starts running.
     * <p>
     * We hide this from the client of this API by laziyl starting
     * the server, and this flag remembers which state we are in.
     */
    private boolean started;

    protected /*almost final*/ Habitat habitat;

    /**
     * Work around until the live HTTP listener support comes back.
     */
    private final Document domainXml;

    /**
     * To navigate around {@link #domainXml}.
     */
    private final XPath xpath = XPathFactory.newInstance().newXPath();

    // key components inside GlassFish. We access them all the time,
    // so we might just as well keep them here for ease of access.
    protected /*almost final*/ ApplicationLifecycle appLife;
    protected /*almost final*/ SnifferManager snifMan;
    protected /*almost final*/ ArchiveFactory archiveFactory;
    protected /*almost  final*/ ServerEnvironment env;

    /**
     * Starts an empty do-nothing GlassFish v3.
     *
     * <p>
     * In particular, no HTTP listener is configured out of the box, so you'd have to add
     * some programatically via {@link #createHttpListener(int)} and {@link #createVirtualServer(GFHttpListener)}.
     */
    public GlassFish() throws GFException {
        try {
            domainXml = parseDefaultDomainXml();
        } catch (IOException e) {
            throw new GFException(e);
        } catch (SAXException e) {
            throw new GFException(e);
        } catch (ParserConfigurationException e) {
            throw new GFException(e);
        }
    }

    /**
     * Starts GlassFish v3 with minimalistic configuration that involves
     * single HTTP listener listening on the given port.
     */
    public GlassFish(int httpPort) throws GFException {
        this();
        createVirtualServer(createHttpListener(httpPort));
        start();
    }

    private Document parseDefaultDomainXml() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        dbf.setNamespaceAware(true);  // domain.xml doesn't use namespace 
        return dbf.newDocumentBuilder().parse(getClass().getResource("/org/glassfish/embed/domain.xml").toExternalForm());
    }

    /**
     * Sets the overall logging level for GlassFish.
     */
    public static void setLogLevel(Level level) {
        Logger.getLogger("javax.enterprise").setLevel(level);
    }

    /**
     * Tweaks the 'recipe' --- for embedded use, we'd like GFv3 to behave a little bit
     * differently from normal stand-alone use.
     */
    protected InhabitantsParser decorateInhabitantsParser(InhabitantsParser parser) {
        // we don't want GFv3 to reconfigure all the loggers
        parser.drop(LogManagerService.class);

        // we don't need admin CLI support.
        // TODO: admin CLI should be really moved to a separate class
        parser.drop(AdminConsoleAdapter.class);

        // don't care about auto-deploy either
        parser.drop(AutoDeployService.class);

        // we don't really parse domain.xml from disk
        parser.replace(DomainXml.class, DomainXml2.class);
        parser.habitat.add(Inhabitants.create(new DomainXmlHolder(domainXml)));

        // ... and we don't persist it either. 
        parser.replace(DomainXmlPersistence.class, DomainXml2.class);

        // we provide our own ServerEnvironment
        parser.replace(ServerEnvironment.class, ServerEnvironment2.class);

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
        parser.replace(WebDeployer.class, WebDeployer2.class);

        // override the location of cached DTDs and schemas
        parser.replace(WebEntityResolver.class, EntityResolverImpl.class);

        return parser;
    }

    protected File createTempDir() throws IOException {
        File dir = File.createTempFile("glassfish","embedded");
        dir.delete();
        dir.mkdirs();
        return dir;
    }

    public GFVirtualServer createVirtualServer(final GFHttpListener listener) {
        // the following live update code doesn't work yet due to the missing functionality in the webtier.
        if(started)
            throw new IllegalStateException();

        DomBuilder db = onHttpService();
        db.element("virtual-server")
                .attribute("id","server")
                .attribute("http-listeners",listener.getId())
                .attribute("hosts","${com.sun.aas.hostName}")   // ???
                .attribute("log-file","")
                .element("property")
                    .attribute("name","docroot")
                    .attribute("value",".");

        return new GFVirtualServer(null);

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

    public GFHttpListener createHttpListener(final int listenerPort) {
        // the following live update code doesn't work yet due to the missing functionality in the webtier.
        if(started)
            throw new IllegalStateException();

        onHttpService().element("http-listener")
                .attribute("id",listenerPort)
                .attribute("address","0.0.0.0")
                .attribute("port",listenerPort)
                .attribute("default-virtual-server","server")
                .attribute("server-name","")
                .attribute("enabled",true);

        return new GFHttpListener(String.valueOf(listenerPort),null);

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
            return new DomBuilder((Element) xpath.evaluate("//http-service",domainXml, XPathConstants.NODE));
        } catch (XPathExpressionException e) {
            throw new AssertionError(e);    // impossible
        }
    }

    /**
     * Starts the server if hasn't done so already. Necessary to work around the live HTTP listener update.
     */
    private void start() {
        if(started) return;
        started = true;

        try {
            final Module[] proxyMod = new Module[1];
            ModulesRegistryImpl mrs = new ModulesRegistryImpl(null) {
                public Module find(Class clazz) {
                    Module m = super.find(clazz);
                    if(m==null)
                        return proxyMod[0];
                    return m;
                }
            };
            proxyMod[0] = mrs.add(new ProxyModuleDefinition(getClass().getClassLoader()));

            StartupContext startupContext = new StartupContext(createTempDir(), new String[0]);

            habitat = new Main() {
                @Override
                protected InhabitantsParser createInhabitantsParser(Habitat habitat1) {
                    return decorateInhabitantsParser(super.createInhabitantsParser(habitat1));
                }

            }.launch(mrs,startupContext);

            appLife = habitat.getComponent(ApplicationLifecycle.class);
            snifMan = habitat.getComponent(SnifferManager.class);
            archiveFactory = habitat.getComponent(ArchiveFactory.class);
            env = habitat.getComponent(ServerEnvironment.class);
        } catch (IOException e) {
            throw new GFException(e);
        } catch (BootException e) {
            throw new GFException(e);
        }

    }

    /**
     * Deploys WAR/EAR/RAR/etc to this GlassFish.
     *
     * @return
     *      always non-null. Represents the deployed application.
     */
    public GFApplication deploy(File archive) throws IOException {
        start();
        ReadableArchive a = archiveFactory.openArchive(archive);

        if(!archive.isDirectory()) {
            // explode (if I don't, WarHandler won't work)
            ArchiveHandler h = appLife.getArchiveHandler(a);

            File tmpDir = new File(a.getName());
            FileUtils.whack(tmpDir);
            tmpDir.mkdirs();
            h.expand(a, archiveFactory.createArchive(tmpDir));
            a.close();
            a = archiveFactory.openArchive(tmpDir);
        }

        return deploy(a);
    }

    public GFApplication deploy(ReadableArchive a) throws IOException {
        start();

        ArchiveHandler h = appLife.getArchiveHandler(a);

        // now prepare sniffers
        ClassLoader parentCL = snifMan.createSnifferParentCL(null);
        ClassLoader cl = h.getClassLoader(parentCL, a);
        Collection<Sniffer> activeSniffers = snifMan.getSniffers(a, cl);

        // TODO: we need to stop this totally type-unsafe way of passing parameters
        Properties params = new Properties();
        params.put(DeployCommand.NAME,a.getName());
        params.put(DeployCommand.ENABLED,"true");
        final DeploymentContextImpl deploymentContext = new DeploymentContextImpl(Logger.getAnonymousLogger(), a, params, env);
        deploymentContext.setClassLoader(cl);

        SilentActionReport r = new SilentActionReport();
        ApplicationInfo appInfo = appLife.deploy(activeSniffers, deploymentContext, r);
        r.check();

        return new GFApplication(this,appInfo,deploymentContext);
    }

    /**
     * Stops the running server.
     */
    public void stop() {
        if(!started)
            return;

        for (Inhabitant<? extends Startup> svc : habitat.getInhabitants(Startup.class)) {
            svc.release();
        }

        for (Inhabitant<? extends Init> svc : habitat.getInhabitants(Init.class)) {
            svc.release();
        }
    }
}
