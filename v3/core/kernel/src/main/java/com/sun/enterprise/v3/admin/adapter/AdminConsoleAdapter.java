/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
package com.sun.enterprise.v3.admin.adapter;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.v3.admin.AdminAdapter;
import com.sun.enterprise.v3.common.PlainTextActionReporter;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyOutputBuffer;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.logging.LogDomains;
import com.sun.pkg.client.Image;
import com.sun.pkg.client.Version;
import org.glassfish.api.ActionReport;
import org.glassfish.api.container.Adapter;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.RestrictTo;
import org.glassfish.internal.api.AdminAccessController;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import java.beans.PropertyVetoException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An HK-2 Service that provides the functionality so that admin console access is handled properly.
 * The general contract of this adapter is as follows:
 * <ol>
 * <li>This adapter is *always* installed as a Grizzly adapter for a particular
 * URL designated as admin URL in domain.xml. This translates to context-root
 * of admin console application. </li>
 * <li>When the control comes to the adapter for the first time, user is asked
 * to confirm if downloading the application is OK. In that case, the admin console
 * application is downloaded and expanded. While the download and installation
 * is happening, all the clients or browser refreshes get a status message.
 * No push from the server side is attempted (yet).
 * After the application is "installed", ApplicationLoaderService is contacted,
 * so that the application is loaded by the containers. This application is
 * available as a <code> system-application </code> and is persisted as
 * such in the domain.xml. </li>
 * <li>Even after this application is available, we don't load it on server
 * startup by default. It is always loaded <code> on demand </code>.
 * Hence, this adapter will always be available to find
 * out if application is loaded and load it in the container(s) if it is not.
 * If the application is already loaded, it simply exits.
 * </li>
 * </ol>
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352; (km@dev.java.net)
 * @author Ken Paulsen (kenpaulsen@dev.java.net)
 * @since GlassFish V3 (March 2008)
 */
@Service
public final class AdminConsoleAdapter extends GrizzlyAdapter implements Adapter, PostConstruct, EventListener {

    @Inject
    ServerEnvironmentImpl env;

    @Inject
    AdminService adminService; //need to take care of injecting the right AdminService


    private String contextRoot;
    private File ipsRoot;    // GF IPS Root
    private File warFile;    // GF Admin Console War File Location
    private String proxyHost;
    private int proxyPort = 8080;
    private AdapterState stateMsg = AdapterState.UNINITIAZED;
    private boolean installing = false;
    private boolean isOK = false;  // FIXME: initialize this with previous user choice
    private boolean errorOccurred   = false;
    private String currentDeployedVersion   = "";     //Version of admin console that is currently deployed
    private String downloadedVersion = null;            //Version of the console IPS package that is downloaded

    private final CountDownLatch latch = new CountDownLatch(1);

    @Inject
    private Logger log;

    @Inject
    ApplicationRegistry appRegistry;

    @Inject
    Domain domain;

    @Inject
    Habitat habitat;

    @Inject
    volatile AdminService as = null;

    @Inject
    Events events;

    @Inject(name = "server-config")
    Config serverConfig;

    AdminEndpointDecider epd;

    private String statusHtml;
    private String initHtml;

    private boolean isRegistered = false;
    private ResourceBundle bundle;

    //don't change the following without changing the html pages

    private static final String PROXY_HOST_PARAM = "proxyHost";
    private static final String PROXY_PORT_PARAM = "proxyPort";
    private static final String OK_PARAM         = "ok";
    private static final String CANCEL_PARAM     = "cancel";

    private static final String MYURL_TOKEN = "%%%MYURL%%%";
    private static final String STATUS_TOKEN = "%%%STATUS%%%";
    private static final String REDIRECT_TOKEN = "%%%LOCATION%%%";
    private static final String ADMIN_CONSOLE_IPS_PKGNAME = "glassfish-gui";

    private static final String RESOURCE_PACKAGE = "com/sun/enterprise/v3/admin/adapter";


    private static final String INSTALL_ROOT = "com.sun.aas.installRoot";
    static final String ADMIN_APP_NAME = ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_APP_NAME;

    // Flag set to true for directory deploy, false for war
    private static final boolean directoryDeploy = true;

    /**
     * Constructor.
     */
    public AdminConsoleAdapter() throws IOException {
        initHtml = Utils.packageResource2String("downloadgui.html");
        statusHtml = Utils.packageResource2String("status.html");
    }

    /**
     *
     */
    public String getContextRoot() {
        return epd.getGuiContextRoot(); //default is /admin
    }

    /**
     *
     */
    public void afterService(GrizzlyRequest req, GrizzlyResponse res) throws Exception {
    }

    /**
     *
     */
    public void fireAdapterEvent(String type, Object data) {
    }

    /**
     *
     */
    public void service(GrizzlyRequest req, GrizzlyResponse res) {
        try {
            if (!latch.await(100L, TimeUnit.SECONDS)) {
                // todo : better error reporting.
                log.severe("Cannot process admin console request in time");
                return;
            }
        } catch (InterruptedException ex) {
            log.severe("Cannot process admin console request");
            return;
        }
        logRequest(req);
        if (isResourceRequest(req)) {
            try {
                handleResourceRequest(req, res);
            } catch (IOException ioe) {
                if (log.isLoggable(Level.SEVERE)) {
                    log.severe("Unable to serve resource: "
                                  + req.getRequestURI()
                                  + ".  Cause: "
                                  + ioe.toString());
                }
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE,
                            ioe.toString(),
                            ioe);
                }
            }
            return;
        }
        bundle = getResourceBundle(req.getLocale());
        res.setContentType("text/html; charset=UTF-8");

        if (isApplicationLoaded()) {
            // Let this pass to the admin console (do nothing)
            handleLoadedState();
        } else {
            InteractionResult ir = getUserInteractionResult(req);
            if (ir == InteractionResult.CANCEL) {
// FIXME: What if they clicked Cancel?
	    }
	    synchronized(this) {
		if (downloadedVersion == null) {
		    setDownloadedVersion();
		}
		if (isInstalling()) {
		    sendStatusPage(req, res);
		} else {
                    if (isErrorOccurred()) {
                        restore();
                        sendStatusPage(req, res);
                        return;
                    } else if (isApplicationLoaded()) {
			// Double check here that it is not yet loaded (not
			// likely, but possible)
			handleLoadedState();
		    } else if (!hasPermission(ir)) {
			// Ask for permission
                        handleAuth(req, res);
			sendConsentPage(req, res);
		    } else {
                        if (redeployNeeded()) {
                            setStateMsg(AdapterState.APPLICATION_PREPARE_UPGRADE);
                            sendStatusPage(req, res);
                            if (!prepareRedeploy()) {
                                setErrorOccurred(true);
                                sendStatusPage(req, res);
                                return;
                            }
                        }
			try {
			    // We have permission and now we should install
			    // (or load) the application.
			    setInstalling(true);
			    startThread();  // Thread must set installing false
			} catch (Exception ex) {
			    // Ensure we haven't crashed with the installing
			    // flag set to true (not likely).
			    setInstalling(false);
			    throw new RuntimeException(
				    "Unable to install Admin Console!", ex);
			}
			sendStatusPage(req, res);
		    }
		}
	    }
	}

    }

    /**
     * @param req the GrizzlyRequest
     * @return <code>true</code> if the request is for a resource with a known content
     *  type otherwise <code>false</code>.
     */
    private boolean isResourceRequest(GrizzlyRequest req) {

        return (getContentType(req.getRequestURI()) != null);

    }


    private String getContentType(String resource) {

        if (resource == null || resource.length() == 0) {
            return null;
        }
        // this may need to be expanded upon the future, in which case, the
        // current implementation may not be worth maintaining
        if (resource.endsWith(".gif")) {
            return "image/gif";
        } else if (resource.endsWith(".jpg")) {
            return "image/jpg";
        } else {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Unhandled content-type: " + resource);
            }
            return null;
        }

    }

    private void handleResourceRequest(GrizzlyRequest req, GrizzlyResponse res)
    throws IOException {

        String resourcePath = RESOURCE_PACKAGE + req.getRequestURI();

        ClassLoader loader = AdminConsoleAdapter.class.getClassLoader();

        InputStream in = null;
        try {
            in = loader.getResourceAsStream(resourcePath);
            if (in == null) {
                if (log.isLoggable(Level.WARNING)) {
                    log.warning("Resource not found: " + resourcePath);
                }
                return;
            }
            byte[] buf = new byte[512];
            ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
            for (int i = in.read(buf); i != -1; i = in.read(buf)) {
                baos.write(buf, 0, i);
            }
            String contentType = getContentType(resourcePath);
            if (contentType != null) {
                res.setContentType(contentType);
            }
            res.setContentLength(baos.size());
            OutputStream out = res.getOutputStream();
            baos.writeTo(out);
            out.flush();

        } finally {
            if (in != null) {
                in.close();
            }
        }

    }

    /**
     * returns true if there is any error occurs during the upgrade process.
     */
    private boolean isErrorOccurred() {
        return errorOccurred;
    }

    /**
     * Set error condition.
     */
    private void setErrorOccurred(boolean error) {
        errorOccurred=error;
    }

    /**
     *
     */
    //We will try to backup the old bits, if the old directory doesn't exist,
    //issue warning, and continue. see issue# 6477
    private boolean prepareRedeploy() {
        try {
            if (!stopAndCleanup()) {
                setStateMsg(AdapterState.APPLICATION_CLEANUP_FALED);
                return false;
            }
            File parentFile = warFile.getParentFile();
            File currentDeployedDir = new File( parentFile,ADMIN_APP_NAME);
            if (!currentDeployedDir.exists()) {
                logger.log(Level.WARNING, currentDeployedDir + " does not exist. Will not do backup for this.");
                return true;
            }
            File backupDir = new File(parentFile, ADMIN_APP_NAME+".backup");
            if (currentDeployedDir.renameTo(backupDir)) {
                return true;
	    }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception in prepareRedeploy() " + ex.getMessage());
            //ex.printStackTrace();
        }
        logger.log(Level.SEVERE, "Cannot backup previous version of __admingui ");
        setStateMsg(AdapterState.APPLICATION_BACKUP_FALED);
        return true;
    }

    private void restore() {
        setStateMsg(AdapterState.APPLICATION_RESTORE);
        File parentFile = warFile.getParentFile();
        File currentDeployedDir = new File(parentFile, ADMIN_APP_NAME);
        File backupDir = new File(parentFile, ADMIN_APP_NAME + ".backup");
        backupDir.renameTo(currentDeployedDir);
        setStateMsg(AdapterState.APPLICATION_UPGRADE_FALED);
    }

    private boolean isApplicationLoaded() {
        return (stateMsg == AdapterState.APPLICATION_LOADED);
    }

    /**
     *
     */
    boolean isInstalling() {
        return installing;
    }

    /**
     *
     */
    void setInstalling(boolean flag) {
        installing = flag;
    }


    /**
     * Checks whether this adapter has been registered as a network endpoint.
     */
    public boolean isRegistered() {
        return isRegistered;
    }

    /**
     * Marks this adapter as having been registered or unregistered as a
     * network endpoint
     */
    public void setRegistered(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }


    /**
     * <p> This method sets the current state.</p>
     */
    void setStateMsg(AdapterState msg) {
        stateMsg = msg;
        log.log(Level.INFO, msg + "");
    }

    /**
     * <p> This method returns the current state, which will be one of the
     * valid values defined by {@link AdapterState}.</p>
     */
    AdapterState getStateMsg() {
        return stateMsg;
    }

    /**
     *
     */
    public void postConstruct() {
        events.register(this);
        //set up the environment properly
        init();
    }

    /**
     *
     */
    public void event(@RestrictTo(EventTypes.SERVER_READY_NAME) Event event) {
        latch.countDown();
        if (log != null) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "AdminConsoleAdapter is ready.");
            }
        }
    }

    /**
     *
     */
    private void handleAuth(GrizzlyRequest greq, GrizzlyResponse gres) {
        try {
            AdminAccessController authenticator = habitat.getByContract(AdminAccessController.class);

            if (authenticator != null) {
                Request req = greq.getRequest();
                String[] userPass = AdminAdapter.getUserPassword(req);
                String pswd = (userPass.length >= 2) ? userPass[1] : "";
                if (!authenticator.loginAsAdmin(userPass[0], pswd, as.getAuthRealmName())) {
                    setStateMsg(AdapterState.AUTHENTICATING);
                    gres.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
                    gres.addHeader("WWW-Authenticate", "BASIC");
                    gres.finishResponse();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     *
     */
    private void init() {

        // Save the IPS root and admin console war locations
        // For upgrade scenario, the property may not be set. refer to issue# 9529.   We hardcode some info here, this should match
        // the out-of-box domain.xml

        Property iprop = adminService.getProperty(ServerTags.IPS_ROOT);
        if(iprop == null){
            File f = new File (System.getProperty(INSTALL_ROOT));
            ipsRoot = new File(f, "..");
            writeAdminServiceProp(ServerTags.IPS_ROOT, "${" + INSTALL_ROOT + "}/..");
        }else{
            ipsRoot = new File(iprop.getValue());
        }

        Property locProp = adminService.getProperty(ServerTags.ADMIN_CONSOLE_DOWNLOAD_LOCATION);
        if(locProp == null || locProp.getValue()==null || locProp.getValue().equals("")){
            String iRoot = System.getProperty(INSTALL_ROOT) + "/lib/install/applications/admingui.war";
            warFile = new File(iRoot.replace('/', File.separatorChar));
            writeAdminServiceProp(ServerTags.ADMIN_CONSOLE_DOWNLOAD_LOCATION, "${" + INSTALL_ROOT + "}/lib/install/applications/admingui.war");
        }else{
            //For any non-absolute path, we start from the installation, ie glassfishv3
            //eg, v3 prelude upgrade, where the location property was "glassfish/lib..."
            String locValue = locProp.getValue();
            warFile = new File (locValue);
            if (! warFile.isAbsolute()){
                File tmp = new File (System.getProperty(INSTALL_ROOT), "..");
                warFile = new File (tmp, locValue);
            }
        }

        Property prop = adminService.getProperty(ServerTags.ADMIN_CONSOLE_VERSION);
        if (prop != null) {
            currentDeployedVersion = prop.getValue();
        } else {
            currentDeployedVersion = "";
        }

        if (log.isLoggable(Level.FINE)){
            log.log(Level.FINE, "GlassFish IPS Root: " + ipsRoot.getAbsolutePath());
            log.log(Level.FINE, "Admin Console download location: " + warFile.getAbsolutePath());
            log.log(Level.FINE, "Current Deployed version: " + currentDeployedVersion);
        }

        initState();
        epd = new AdminEndpointDecider(serverConfig, log);
        contextRoot = epd.getGuiContextRoot();
    }

    /**
     *
     */
    private void initState() {
        // It is a given that the application is NOT loaded to begin with
        if (appExistsInConfig()) {
            isOK = true; // FIXME: I don't think this is good enough
            setStateMsg(AdapterState.APPLICATION_INSTALLED_BUT_NOT_LOADED);
        } else if (new File(warFile.getParentFile(), ADMIN_APP_NAME).exists() || warFile.exists()) {
	    // The exploded dir, or the .war exists... mark as downloded
            if (logger.isLoggable(Level.FINE)) {
                setStateMsg(AdapterState.DOWNLOADED);
            }
            isOK = true;
        } else {
            setStateMsg(AdapterState.APPLICATION_NOT_INSTALLED);
        }
    }

    /**
     *
     */
    private boolean appExistsInConfig() {
        return (getConfig() != null);
    }

    /**
     *
     */
    Application getConfig() {
        //no application-ref logic here -- that's on purpose for now
        Application app = domain.getSystemApplicationReferencedFrom(env.getInstanceName(), ADMIN_APP_NAME);

        return app;
    }

    /**
     *
     */
    private void logRequest(GrizzlyRequest req) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("AdminConsoleAdapter's STATE IS: " + getStateMsg());
            log.log(Level.FINE, "Current Thread: " + Thread.currentThread().getName());
            Enumeration names = req.getParameterNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                String values = Arrays.toString(req.getParameterValues(name));
                log.fine("Parameter name: " + name + " values: " + values);
            }
        }
    }

    
    /**
     *
     */
    enum InteractionResult {
        OK,
        CANCEL,
        FIRST_TIMER;
    }

    /**
     * <p> Determines if the user has permission.</p>
     */
    private boolean hasPermission(InteractionResult ir) {
        //do this quickly as this is going to block the grizzly worker thread!
        //check for returning user?
        if (ir == InteractionResult.OK) {
            isOK = true;
        }
        return isOK;
    }

    /**
     *
     */
    private void startThread() {
        new InstallerThread(ipsRoot, warFile, proxyHost, proxyPort, this, habitat, domain, env, contextRoot, log, epd.getGuiHosts()).start();
    }

    /**
     *
     */
    private synchronized InteractionResult getUserInteractionResult(GrizzlyRequest req) {
        if (req.getParameter(OK_PARAM) != null) {
            proxyHost = req.getParameter(PROXY_HOST_PARAM);
            if ((proxyHost != null) && !proxyHost.equals("")) {
                String ps = req.getParameter(PROXY_PORT_PARAM);
                try {
                    proxyPort = Integer.parseInt(ps);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException(
                            "The specified proxy port (" + ps
                                    + ") must be a valid port integer!", nfe);
                }
            }
// FIXME: I need to "remember" this answer in a persistent way!! Or it will popup this message EVERY time after the server restarts.
            setStateMsg(AdapterState.PERMISSION_GRANTED);
            isOK = true;
            return InteractionResult.OK;
        } else if (req.getParameter(CANCEL_PARAM) != null) {
            // Canceled
// FIXME: I need to "remember" this answer in a persistent way!! Or it will popup this message EVERY time after the server restarts.
            setStateMsg(AdapterState.CANCELED);
            isOK = false;
            return InteractionResult.CANCEL;
        }

        // This is a first-timer
        return InteractionResult.FIRST_TIMER;
    }

    private GrizzlyOutputBuffer getOutputBuffer(GrizzlyResponse res) {
        GrizzlyOutputBuffer ob = res.getOutputBuffer();
        res.setStatus(202);
        res.setContentType("text/html");
        ob.setEncoding("UTF-8");
        return ob;
    }

    /**
     *
     */
    private synchronized void sendConsentPage(GrizzlyRequest req, GrizzlyResponse res) { //should have only one caller
        setStateMsg(AdapterState.PERMISSION_NEEDED);
        byte[] bytes;
        try {
            GrizzlyOutputBuffer ob = getOutputBuffer(res);
            try {
                // Replace locale specific Strings
                String localHtml = replaceTokens(initHtml, bundle);

                // Replace path token
                String hp = (contextRoot.endsWith("/")) ? contextRoot : contextRoot + "/";
                bytes = localHtml.replace(MYURL_TOKEN, hp).getBytes("UTF-8");
            } catch (Exception ex) {
                bytes = ("Catastrophe:" + ex.getMessage()).getBytes("UTF-8");
            }
            res.setContentLength(bytes.length);
            ob.write(bytes, 0, bytes.length);
            ob.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     *
     */
    private void sendStatusPage(GrizzlyRequest req, GrizzlyResponse res) {
        byte[] bytes;
        try {
            GrizzlyOutputBuffer ob = getOutputBuffer(res);
            // Replace locale specific Strings
            String localHtml = replaceTokens(statusHtml, bundle);

            // Replace state token
            String status = getStateMsg().getI18NKey();
            try {
                // Try to get a localized version of this key
                status = bundle.getString(status);
            } catch (MissingResourceException ex) {
                // Use the non-localized String version of the status
                status = getStateMsg().toString();
            }
            String locationUrl = req.getScheme()
                                 + "://" + req.getServerName()
                                 + ':' + req.getServerPort() + "/login.jsf";
            localHtml = localHtml.replace(REDIRECT_TOKEN, locationUrl);
            bytes = localHtml.replace(STATUS_TOKEN, status).getBytes("UTF-8");
            res.setContentLength(bytes.length);
            ob.write(bytes, 0, bytes.length);
            ob.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * <p> This method returns the resource bundle for localized Strings used
     * by the AdminConsoleAdapter.</p>
     *
     * @param    locale    The Locale to be used.
     */
    private ResourceBundle getResourceBundle(Locale locale) {
        return ResourceBundle.getBundle(
                "com.sun.enterprise.v3.admin.adapter.LocalStrings", locale);
    }

    /**
     * <p> This method replaces all tokens in text with values from the given
     * <code>ResourceBundle</code>.  A token starts and ends with 3
     * percent (%) characters.  The value between the percent characters
     * will be used as the key to the given <code>ResourceBundle</code>.
     * If a key does not exist in the bundle, no substitution will take
     * place for that token.</p>
     *
     * @return The same text except with substituted tokens when available.
     * @param    text    The text containing tokens to be replaced.
     * @param    bundle    The <code>ResourceBundle</code> with keys for the value
     */
    private String replaceTokens(String text, ResourceBundle bundle) {
        int start = 0, end = 0;
        String key = null;
        String newString = null;
        StringBuffer buf = new StringBuffer("");
        Enumeration<String> keys = bundle.getKeys();

        while (start != -1) {
            // Find start of token
            start = text.indexOf("%%%", end);
            if (start != -1) {
                // First copy the stuff before the start
                buf.append(text.substring(end, start));

                // Move past the %%%
                start += 3;

                // Find end of token
                end = text.indexOf("%%%", start);
                if (end != -1) {
                    try {
                        // Copy the token value to the buffer
                        buf.append(
                                bundle.getString(text.substring(start, end)));
                    } catch (MissingResourceException ex) {
                        // Unable to find the resource, so we don't do anything
                        buf.append("%%%" + text.substring(start, end) + "%%%");
                    }

                    // Move past the %%%
                    end += 3;
                } else {
                    // Add back the %%% because we didn't find a matching end
                    buf.append("%%%");

                    // Reset end so we can copy the remainder of the text
                    end = start;
                }
            }
        }

        // Copy the remainder of the text
        buf.append(text.substring(end));

        // Return the new String
        return buf.toString();
    }

    /**
     * 
     */
    public String getDownloadedVersion() {
        return downloadedVersion;
    }

    public void setDownloadedVersion() {
	if (downloadedVersion == null) {
	    downloadedVersion = "";
	}
        try{
            Image image = new Image(ipsRoot);
            if (image != null) {
                List<Image.FmriState> fList = image.getInventory(new String[]{ADMIN_CONSOLE_IPS_PKGNAME}, false);
                if (fList.size() > 0) {
                    downloadedVersion = fList.get(0).fmri.getVersion().toString();
                }
            } else {
                log.log(Level.WARNING, "!!!! No information relating to update center.");
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "!!!!! Cannot create Update Center Image for " + ipsRoot );
            //ex.printStackTrace();
        }
    }

    public String getCurrentDeployedVersion() {
        return currentDeployedVersion;
    }

    public AdminService getAdminService() {
        return adminService;
    }

    public String getIPSPackageName() {
        return ADMIN_CONSOLE_IPS_PKGNAME;
    }

    private boolean redeployNeeded() {
	if (isDirectoryDeploy()) {
	    return false;
	}
        //for first access after installation, deployedVersion will be "",  we don't want to do redeployment.
        //it will just go through install and loading.
        if (currentDeployedVersion == null || currentDeployedVersion.equals("")) {
            return false;
        }
        //if we don't know the downloaded version, we don't want to do redeployment either.
        //this maybe the case during development where web.zip doesn't include UC info.
        if (downloadedVersion.equals ("")) {
            return false;
        }

        Version deployed = new Version(currentDeployedVersion);
        Version downloaded = new Version(downloadedVersion);
        int compare = deployed.compareTo(downloaded);

        //-1 if this version is less than downloaded, 0 if they are equal, 1 if this version is greater than downloaded
	return (compare == -1);
    }

    private void writeAdminServiceProp(final String propName, final String propValue){
        try{
            ConfigSupport.apply(new SingleConfigCode<AdminService>() {
                public Object run(AdminService adminService) throws PropertyVetoException, TransactionFailure {
                    Property newProp = adminService.createChild(Property.class);
                    adminService.getProperty().add(newProp);
                    newProp.setName(propName);
                    newProp.setValue(propValue);
                    return newProp;
                }
            }, adminService);
        }catch(Exception ex){
            log.log(Level.WARNING, "Cannot write property for AdminService in domain.xml; " + propName + ":" + propValue);
            //ex.printStackTrace();
        }
    }

    public void updateDeployedVersion() {
        try{
            final Property prop = adminService.getProperty(ServerTags.ADMIN_CONSOLE_VERSION);
            if (prop == null) {
                writeAdminServiceProp(ServerTags.ADMIN_CONSOLE_VERSION, downloadedVersion );
            } else {
                if (! downloadedVersion.equals(prop.getValue())) {
                    ConfigSupport.apply(new SingleConfigCode<Property>() {
                        public Object run(Property prop) throws PropertyVetoException, TransactionFailure {
                            prop.setValue(downloadedVersion);
                            return prop;
                        }
                    }, prop);
                }
            }
        } catch (Exception ex) {
            log.log(Level.FINE, "!!!! Error, cannot update deployed version in domain.xml");
            //ex.printStackTrace();
        }
    }

    /**
     *
     */
    private void handleLoadedState() {
//System.out.println(" Handle Loaded State!!");
        // do nothing
        statusHtml = null;
        initHtml = null;
    }

    public int getListenPort() {
        return epd.getListenPort();
    }

    public List<String> getVirtualServers() {
        return epd.getGuiHosts();
    }


    /**
     * Stop (if running) and cleanup existing admin gui installation, usually performed during upgrades.
     * the entries in the domain.xml will NOT be removed, this is an inplace upgrade,
     * not a redeploy.
     *
     * @return true if stopping and cleaning the current installation was successful
     */
    private boolean stopAndCleanup() {

        Application app = getConfig();
        if (app==null) {
            // never deployed/ran, nothing to worry about
            return true;
        }
        final String location = app.getLocation();
        final Logger logger = LogDomains.getLogger(this.getClass(), LogDomains.CORE_LOGGER);
        try {
            final ArchiveFactory archiveFactory = habitat.getComponent(ArchiveFactory.class);
            final ReadableArchive archive = archiveFactory.openArchive(new File(location));

            UndeployCommandParameters parameters = new UndeployCommandParameters(ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_APP_NAME);
            parameters.origin = UndeployCommandParameters.Origin.unload;
            Deployment deployment = habitat.getComponent(Deployment.class);
            ActionReport report = new PlainTextActionReporter();
            
            ExtendedDeploymentContext context = deployment.getBuilder(logger, parameters, report).source(archive).build();

            ApplicationInfo info = appRegistry.get(ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_APP_NAME);
            if (info!=null) {
                deployment.undeploy(ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_APP_NAME, context);
            } else {
                // no need to worry, let's just delete all created metadata.
                context.clean();
            }
            if (report.getActionExitCode() != ActionReport.ExitCode.SUCCESS) {
                logger.log(Level.SEVERE, "Cannot undeploy current admin gui ", report.getFailureCause());
                return false;
            }
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Exception while stopping and cleaning previous instance of admin GUI", ioe);
            return false;
        }
        return true;
    }

    /**
     *	<p> This method returns true if the server only supports directory
     *	    deployment of the admin console application.  false means that a
     *	    .war file will be supplied which must be expanded.</p>
     */
    public static boolean isDirectoryDeploy() {
	return directoryDeploy;
    }
}
