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

import org.glassfish.embed.util.LoggerHelper;
import org.glassfish.embed.util.StringHelper;
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.logging.*;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import org.glassfish.api.admin.CommandRunner;
import com.sun.enterprise.v3.server.ApplicationLifecycle;
import com.sun.enterprise.web.EmbeddedWebContainer;
import com.sun.enterprise.web.VirtualServer;
import com.sun.enterprise.web.WebContainer;
import com.sun.enterprise.web.WebModule;
import com.sun.hk2.component.LazyInhabitant;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Wrapper;
import org.apache.catalina.servlets.DefaultServlet;
import org.glassfish.embed.impl.EmbeddedModulesRegistryImpl;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Startup;
import org.glassfish.internal.api.Init;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import static org.glassfish.embed.util.ServerConstants.*;

/**
 * Entry point to the embedded GlassFish Server.
 * <p/>
 * <p/>
 * A <code>Server</code> object is constructed using an <code>EmbeddedInfo</code>
 * object.  Information about the server such as server name or HTTP port may be
 * set on {@link EmbeddedInfo}.  If no information is set, "server" is the default
 * server name, and no ports are set by default.
 * <p/>
 * <p/>
 * After the <code>Server</code> has been created, the <code>Server</code> can
 * be started.
 * <xmp>
 * EmbeddedInfo info = new EmbeddedInfo();
 * info.setHttpPort("22222"); // example.
 * Server server = new Server(info);
 * server.start();
 * </xmp>
 * <p/>
 * @author Kohsuke Kawaguchi
 * @author Byron Nevins
 */
public class Server {

    /******************************************************************
     *************    public methods   ********************************
     ******************************************************************
     */
    /**
     * Creates a Server object with the given EmbeddedInfo object.
     *
     * @param info EmbeddedInfo object which specifies information like server
     *             name and HTTP port
     * @throws EmbeddedException
     */
    public Server(EmbeddedInfo info) throws EmbeddedException {
        this.info = info;
        info.validate();
        efs = info.getFileSystem();
        setShutdownHook();
        readDomainXmlSource();

        try {
            jdbcHack();
        }
        catch (Exception e) {
            throw new EmbeddedException("jdbc_hack_failure", e);
        }

        // only add listeners and virtual-servers when using *our* built-in
        // domain.xml.
        // And then only add listeners/connectors where the ports have been
        // explicitly set

        if (!efs.isUserDomainXml()) {
            if(getInfo().httpPort != DEFAULT_HTTP_PORT) {
                createHttpListener();
                createVirtualServer();
            }
            if(getInfo().adminHttpPort != DEFAULT_ADMIN_HTTP_PORT) {
                createAdminHttpListener();
                createAdminVirtualServer();
            }
            if(getInfo().jmxConnectorPort != DEFAULT_JMX_CONNECTOR_PORT) {
                createJMXConnector();
            }
        }

        copyWelcomeFile();
        // todo TODO
        //else check & make sure therir xml has a listener(??)
        addServer(info.name, this);
        writeXml();
        testDerby();
    }
    /**
     * Convenience method.  Creates a Server object with an HTTP Listener attached
     * to the given port number.
     *
     * @param port The Http port number to listen to
     * @throws EmbeddedException if any errors
     */
    public Server(int port) throws EmbeddedException {
        this(new EmbeddedInfo(port));
    }
    
    /**
     * The name of the server is inside the contained EmbeddedInfo object
     * @return the name of this server
     * @see EmbeddedInfo
     */
    public String getServerName() {
        return this.info.name;
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
    * Executes the provided command.  If the command fails, EmbeddedException is thrown.
    * Server must be started before executing commands on <code>Server</code>.
    * Use <code>CommandParameters</code> to set the command operand and options.
    * Use <code>CommandExecution</code> to get information about the execution,
    * such as the exit code or message from the command execution.
    *
    * <xmp>
    * CommandParameters cp = new CommandParameters();
    * cp.setOperand("DerbyPool");
    * cp.setOption("datasourceclassname", "org.apache.derby.jdbc.ClientDataSource");
    * cp.setOption("isisolationguaranteed", "false");
    * cp.setOption("restype", "javax.sql.DataSource");
    * cp.setOption("property", "PortNumber=1527:Password=APP:User=APP:serverName=localhost:DatabaseName=sun-appserv-samples:connectionAttributes=\\;create\\\\=true");
    * CommandExecution ce = server.execute("create-jdbc-connection-pool", cp);
    * </xmp>
    *
    * @param commandName name of the command (e.g. "create-jdbc-resource")
    * @param params <code>CommandParameters</code> with the command operand and
    *  options set (e.g. setOption("connectionpoolid", "DerbyPool")).
    *  For operand use <code>CommandParameters setOperand</code>. (e.g. name of the JDBC resource, setOperand("jdbcA"))
    * @throws EmbeddedException
    * @see <a href="http://docs.sun.com/app/docs/doc/820-4495/gcode?a=view">asadmin commands</a>
    */
    public CommandExecution execute(String commandName, CommandParameters params) throws EmbeddedException {
        mustBeStarted("execute");
    
        CommandExecution ce = new CommandExecution();
        ActionReport report = ce.getActionReport();
        try {
             
             this.getHabitat().getComponent(CommandRunner.class).doCommand(commandName, params.getParams(), report);
             
        } catch (Throwable t) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(t);
            report.setMessage(t.getLocalizedMessage());
            report.setActionDescription("Last-chance CommandExecutor exception handler");
        }

        ActionReport.ExitCode exitCode = report.getActionExitCode();
        String msg = report.getMessage();
        Throwable t  = report.getFailureCause();

        if (exitCode.equals(exitCode.SUCCESS)) {
            LoggerHelper.info("command_successful", commandName);
            if (msg!=null)
                LoggerHelper.info(msg);
        } else if (exitCode.equals(exitCode.FAILURE)) {
            LoggerHelper.severe("command_failed", commandName);
            if (msg!=null)
                LoggerHelper.severe(msg);

            if (t == null) {
                throw new EmbeddedException("command_failed", commandName);
            } else {
                throw new EmbeddedException(StringHelper.get("command_failed", commandName), t);
            }
        }
        return ce;
    }

    /**
     * Convenience method that returns this server's EmbeddedFileSystem instance.
     * THE instance is contained in this server's EmbeddedInfo object.  It can
     * also be accessed through the EmbeddedInfo object.
     * @return EmbeddedFileSystem
     * @see EmbeddedFileSystem
     */
    public EmbeddedFileSystem getFileSystem() {
        return efs;
    }

    /**
     * Sets the log level on the given logger.
     *
     * If no logger name is provided, where loggerName is NULL, then the new log
     * level will be set on the Embedded Logger ("org.glassfish.embed") and the
     * GlassFish v3 Root Logger ("javax.enterprise").
     *
     * @param loggerName logger to set level on.  NULL is allowed.
     * @param level the new log {@link Level}
     * @see Table 9–1 Logger Namespaces for Enterprise Server Modules in
     * <a href="http://docs.sun.com/app/docs/doc/820-4495/abluj?a=view">Chapter 9 Administering Logging</a>
     * of the Sun GlassFish Enterprise Server v3 Prelude Administration Guide
     */
    public static void setLogLevel(String loggerName, Level level) {
        LoggerHelper.setLevel(loggerName, level);
    }

    /**
     * Sets the log level on the Embedded Logger ("org.glassfish.embed") and the
     * GlassFish v3 Root Logger ("javax.enterprise").
     *
     * @param newLevel the new log {@link Level}
     */
    public static void setLevel(Level newLevel) {
       setLogLevel(null, newLevel);
    }


    /**
     * Starts the server
     * @throws EmbeddedException
     */
    public void start() throws EmbeddedException {
        if (started)
            throw new EmbeddedException("already_started");
        //This breaks asadmin start-domain (for SQE)
        //if (this.getInfo().httpPort == DEFAULT_HTTP_PORT)
        //    throw new EmbeddedException("cant_start_server", this.getServerName());

        started = true;

        try {

            EmbeddedModulesRegistryImpl reg = new EmbeddedModulesRegistryImpl();

            // IT 54
            // You would never guess it but V3 code will take the PARENT directory of
            // the first arg to the StartupContext ctor and assume THAT is the install-dir.
            // So we need to send in a fake directory.

            StartupContext startupContext = new StartupContext(efs.getModulesDirectory(), new String[0]);
            Main main       = new EmbeddedBootstrap(this);
            habitat         = main.launch(reg, startupContext);
            appLife         = habitat.getComponent(ApplicationLifecycle.class);
            wc              = habitat.getComponent(WebContainer.class);
            ewc             = habitat.getComponent(EmbeddedWebContainer.class);
        }
        catch (Exception e) {
            throw new EmbeddedException(e);
        }
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

        // non-deamon threads still running, so call stop on AppServerStartup to stop server completely
        Object svc = habitat.getInhabitantByType("com.sun.enterprise.v3.server.AppServerStartup");
        if (svc instanceof LazyInhabitant) {
            Object real = ((LazyInhabitant) svc).get((LazyInhabitant) svc);
            if (real instanceof ModuleStartup) {
                ((ModuleStartup) real).stop();
            } else {
                LoggerHelper.info("cant_stop_server", this.info.name);
            }
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


    /**
     * If a browser is pointed to a web application that has no welcome file then
     * a file listing is shown in the browser if Listings is set to true.
     * The default is tken from the default web.xml file.  This method will override
     * whatever default is in web.xml
     * @param b true turns listings on, false turns them off
     * @throws EmbeddedException
     * @see setDefaultWebXml()
     */
    public void setListings(boolean b) throws EmbeddedException {
        Container[] vss = getVirtualServers();

        for (Container vs : vss) {
            if (!(vs instanceof VirtualServer))
                continue;   // should not happen

            Container[] wms = getWebModules(vs);

            for (Container wm : wms) {
                if (!(wm instanceof WebModule))
                    continue;   // should not happen

                try {
                    Wrapper wrapper = getDefaultServletWrapper(wm);
                    DefaultServlet ds = (DefaultServlet) wrapper.allocate();
                    ds.setListings(true);
                    wrapper.deallocate(ds);
                }
                catch (ServletException e) {
                    throw new EmbeddedException(e);
                }
            }
        }
    }




    /**
     * Returns an EmbeddedDeployer instance that can be used to deploy applications
     * to this server.
     * @return a freshly created EmbeddedDeployer for this server
     * @throws EmbeddedException
     * @see EmbeddedDeployer
     */
    public EmbeddedDeployer getDeployer() throws EmbeddedException {
        return new EmbeddedDeployer(this);
    }


    /******************************************************************
     *************    package-private methods   ********************************
     ******************************************************************
     */
    ApplicationLifecycle getAppLife() {
        return appLife;
    }

    /**
     * Returns the HK2 Habitat object used by this Server.
     * @return the one and only Habitat
     */
    public Habitat getHabitat() {
        return habitat;
    }

	EmbeddedInfo getInfo() {
	    return info;
    }

    void mustBeStarted(String methodName) throws EmbeddedException {
        if (!isStarted()) {
            throw new EmbeddedException("not_started", methodName);
        }
    }

    void mustNotBeStarted(String methodName) throws EmbeddedException {
        if (isStarted()) {
            throw new EmbeddedException("should_not_be_started", methodName);
        }
    }
    
    Engine getEngine() throws EmbeddedException {
        mustBeStarted("getEngine");
        Engine engine = wc.getEngine();

        if (engine == null) {
            throw new EmbeddedException("bad_engines");
        }

        return engine;
    }


    /******************************************************************
     *************    private   ********************************
     ******************************************************************
     */

    /**
     * Returns all virtual servers from this server's web container.
     * @return this server's virtual servers
     * @throws EmbeddedException
     */
    private Container[] getVirtualServers() throws EmbeddedException {
        Container[] vss = getEngine().findChildren();

        if (vss == null || vss.length <= 0)
            throw new EmbeddedException("bad_virtual_servers");

        return vss;
    }

    private void createAdminVirtualServer() throws EmbeddedException {
        mustNotBeStarted("createAdminVirtualServer");

        DomBuilder db = onHttpService();
        db.element("virtual-server")
                .attribute("id", info.adminVSName)
                .attribute("http-listeners", info.adminHttpListenerName)
                .attribute("hosts", "${com.sun.aas.hostName}") // ???
                .attribute("log-file", "")
                .element("property")
                    .attribute("name", "docroot")
                    .attribute("value", efs.getDocRootDir().getPath());
    }

    private void createAdminHttpListener() throws EmbeddedException {
        mustNotBeStarted("createAdminHttpListener");

        onHttpService().element("http-listener").attribute("id", info.adminHttpListenerName).attribute("address", "0.0.0.0").attribute("port", info.adminHttpPort).attribute("default-virtual-server", info.adminVSName).attribute("server-name", "").attribute("enabled", true);
    }

    private DomBuilder onAdminService() {
        try {
            return new DomBuilder((Element) xpath.evaluate("//admin-service", domainXmlDocument, XPathConstants.NODE));
        }
        catch (XPathExpressionException e) {
            throw new AssertionError(e);    // impossible
        }
    }

    private void createVirtualServer() throws EmbeddedException {
        // the following live update code doesn't work yet due to the missing functionality in the webtier.
        mustNotBeStarted("createVirtualServer");

        DomBuilder db = onHttpService();
        db.element("virtual-server")
                .attribute("id", "server")
                .attribute("http-listeners", info.httpListenerName)
                .attribute("hosts", "${com.sun.aas.hostName}") // ???
                .attribute("log-file", "")
                .element("property")
                    .attribute("name", "docroot")
                    .attribute("value", efs.getDocRootDir().getPath());
    }

    private void createHttpListener() throws EmbeddedException {
        // the following live update code doesn't work yet due to the missing functionality in the webtier.
        mustNotBeStarted("createHttpListener");

        onHttpService().element("http-listener") //hardcoding to http-listner-1 should not be a requirment, but the id is used to find the right Inhabitant
                .attribute("id", info.httpListenerName).attribute("address", "0.0.0.0").attribute("port", info.httpPort).attribute("default-virtual-server", "server").attribute("server-name", "").attribute("enabled", true);
    }

    /**
     * Starts the server if hasn't done so already.
     * Necessary to work around the live HTTP listener update.
     * It is an error to call this more than once.
     * @throws EmbeddedException
     */

    private void readDomainXmlSource() throws EmbeddedException {
        URL sourceUrl = efs.getSourceDomainXml();

        if (sourceUrl == null)
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
            }
        });
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
        if (cp.exists() && ds.exists() && xa.exists())
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
    /**
     * copy a simple "your server is running..." index.html file
     */
    private void copyWelcomeFile() throws EmbeddedException {
        if(welcomeExists())
            return;

        File out = new File(efs.getDocRootDir(), WELCOME_FILE);
        Class clazz = getClass();
        final String base = "/org/glassfish/embed";

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    clazz.getResourceAsStream(base + "/" + WELCOME_FILE)));
            copy(br, out);
        }
        catch(Exception e) {
            throw new EmbeddedException(StringHelper.get("bad_copy_welcome", out), e);
        }
    }

    private void testDerby() {
        try
        {
            String driver = "org.apache.derby.jdbc.EmbeddedDriver";

            Class.forName(driver).newInstance();
            LoggerHelper.info("Successfully loaded JavaDB driver");
        }
        catch(Exception ex)
        {
            LoggerHelper.info("could not load JavaDB driver");
        }



    }

    /*
     * Is index.* already in docroot?
     */
    private boolean welcomeExists() throws EmbeddedException {
       File[] files = efs.getDocRootDir().listFiles();

       if(files == null)
           return false;

       for(File f : files) {
           String name = f.getName().toLowerCase();

           if(name != null && name.startsWith("index"))
               return true;
       }

       return false;
    }

    private static void copy(BufferedReader in, File out) throws FileNotFoundException, IOException {
        // If we did regular byte copying -- we would have a horrible mess on Windows
        // this way we get the right line terminators on any platform.
        PrintWriter pw = new PrintWriter(out);

        for (String s = in.readLine(); s != null; s = in.readLine()) {
            pw.println(s);
        }

        pw.close();
        in.close();
    }

    private boolean isStarted() {
        return started;
    }


    private void writeXml() throws EmbeddedException {
        // Write domain.xml to target
        try {
            File domainFile = efs.getTargetDomainXml();
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.transform(new DOMSource(domainXmlDocument), new StreamResult(domainFile));
        }
        catch (Exception e) {
            throw new EmbeddedException("Failed to write domain XML", e);
        }
    }

    private Container[] getWebModules(Container vs) throws EmbeddedException {
        if (vs == null) {
            throw new EmbeddedException("bad_virtual_server", "null");
        }
        // Virtual Servers may have no Web Modules, but the array can not be null
        Container[] wm = vs.findChildren();

        if (wm == null)
            throw new EmbeddedException("bad_virtual_server", vs.getName());

        return wm;
    }

    private Wrapper getDefaultServletWrapper(Container wm) throws EmbeddedException {
        if (wm == null) {
            throw new EmbeddedException("bad_web_module", "null");
        }
        // note that all web modules have a default server
        Container[] servletWrappers = wm.findChildren();

        if (servletWrappers == null || servletWrappers.length <= 0)
            throw new EmbeddedException("bad_web_module", "No Servlets");

        for (Container servletWrapper : servletWrappers) {
            if ("default".equals(servletWrapper.getName()))
                return (Wrapper) servletWrapper;
        }

        throw new EmbeddedException("bad_web_module", "No Default Servlet");
    }

    private void createJMXConnector() throws EmbeddedException {
        mustNotBeStarted("createJMXConnector");

        onAdminService().element("jmx-connector").attribute("accept-all", false).attribute("address", "0.0.0.0").attribute("auth-realm-name", "admin-realm").attribute("enabled", true).attribute("name", "system").attribute("port", info.jmxConnectorPort).attribute("protocol", "rmi_jrmp").attribute("security-enabled", false);
    }

    private DomBuilder onHttpService() {
        try {
            return new DomBuilder((Element) xpath.evaluate("//http-service", domainXmlDocument, XPathConstants.NODE));
        }
        catch (XPathExpressionException e) {
            throw new AssertionError(e);    // impossible
        }
    }
    
    static {
		// initialize HK2
        // this ought to be in HK2Factory class itself
        com.sun.enterprise.module.impl.HK2Factory.initialize();
	}

    ////////////////////////////////////////////////////////
    /////////////   private variables //////////////////////
    ////////////////////////////////////////////////////////
    // API note from bnevins
    // I always keep private variables at the end of files.  Since
    // they are private they are merely an implementation detail and are not
    // of interest to anyone trying to see the API.
    // Java coding conventions are wrong about this, IMHO.  I believe the "variables at
    // the top" are merely a leftover from C where variables *had* to be at the top.

    private Habitat                     habitat;
    private final XPath                 xpath           = XPathFactory.newInstance().newXPath();
    private boolean                     started;
    private ApplicationLifecycle        appLife;
    private WebContainer                wc;
    private EmbeddedWebContainer        ewc;
    private Document                    domainXmlDocument;
    private EmbeddedFileSystem          efs;
    private EmbeddedInfo                info;
    private static Map<String, Server>  servers         = new HashMap<String, Server>();
}

/****************************************************************************    
 ******  POSSIBLY USEFUL stuff -- feel free to delete after March 2009 ******
 ****************************************************************************
 */
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

    /*
     *         <admin-service system-jmx-connector-name="system" type="das-and-server">
    <!-- The JSR 160 "system-jmx-connector" -->
    <!--
    <jmx-connector
    accept-all="false"
    address="0.0.0.0"
    auth-realm-name="admin-realm"
    enabled="true"
    name="system"
    port="8686"
    protocol="rmi_jrmp"
    security-enabled="false"
    />
    -->
    <!-- The JSR 160 "system-jmx-connector" -->
     */
    /**
     * As of April 2008, several key configurations like HTTP listener
     * creation cannot be done once GFv3 starts running.
     * <p/>
     * We hide this from the client of this API by laziyl starting
     * the server, and this flag remembers which state we are in.
     */
