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

package com.sun.enterprise.v3.admin.adapter;

import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.Application;
//import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Property;
import com.sun.enterprise.config.serverbeans.ServerTags;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.server.ServerEnvironmentImpl;
import com.sun.enterprise.universal.glassfish.SystemPropertyConstants;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyOutputBuffer;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.container.Adapter;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.RestrictTo;
import org.glassfish.api.event.EventTypes;
import org.glassfish.internal.api.AdminAuthenticator;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Habitat;

/**
 * An HK-2 Service that provides the functionality so that admin console access is handled properly.
 * The general contract of this adapter is as follows:
 * <ol>
 * <li>This adapter is *always* installed as a Grizzly adapter for a particular
 *     URL designated as admin URL in domain.xml. This translates to context-root
 *     of admin console application. </li>
 * <li>When the control comes to the adapter for the first time, user is asked
 *     to confirm if downloading the application is OK. In that case, the admin console
 *     application is downloaded and expanded. While the download and installation
 *     is happening, all the clients or browser refreshes get a status message.
 *     No push from the server side is attempted (yet).
 *     After the application is "installed", ApplicationLoaderService is contacted,
 *     so that the application is loaded by the containers. This application is
 *     available as a <code> system-application </code> and is persisted as
 *     such in the domain.xml. </li>
 * <li>Even after this application is available, we don't load it on server
 *     startup by default. It is always loaded <code> on demand </code>.
 *     Hence, this adapter will always be available to find
 *     out if application is loaded and load it in the container(s) if it is not.
 *     If the application is already loaded, it simply exits.
 * </li>
 * </ol>
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352; (km@dev.java.net)
 * @author Ken Paulsen (kenpaulsen@dev.java.net)
 *
 * @since GlassFish V3 (March 2008)
 */
@Service
public final class AdminConsoleAdapter extends GrizzlyAdapter implements Adapter, PostConstruct, EventListener {

    @Inject
    ServerEnvironmentImpl env;

    @Inject
    AdminService as; //need to take care of injecting the right AdminService

    private String contextRoot;
    private File ipsRoot;	// GF IPS Root
    private File warFile;	// GF Admin Console War File Location
    private String proxyHost;
    private int proxyPort;
    private AdapterState stateMsg   = AdapterState.UNINITIAZED;
    private boolean installing	    = false;
    private boolean isOK	    = false;  // FIXME: initialize this with previous user choice

    private final CountDownLatch latch = new CountDownLatch(1);

    @Inject
    private Logger log;

    @Inject
    ApplicationRegistry appRegistry;

    @Inject
    Domain domain;

    @Inject
    Habitat habitat;

    @Inject(optional=true)
    AdminAuthenticator authenticator=null;

    @Inject
    Events events;
    
    @Inject (name="server-config")
    Config serverConfig;
    
    AdminEndpointDecider epd;

    private String statusHtml;
    private String initHtml;

    //don't change the following without changing the html pages

    private static final String PROXY_HOST_PARAM = "proxyHost";
    private static final String PROXY_PORT_PARAM = "proxyPort";
    private static final String OK_PARAM         = "ok";
    private static final String CANCEL_PARAM     = "cancel";
    private static final String VISITOR_PARAM    = "visitor";

    private static final String VISITOR_TOKEN    = "%%%VISITOR%%%";
    private static final String MYURL_TOKEN      = "%%%MYURL%%%";
    private static final String STATUS_TOKEN     = "%%%STATUS%%%";

    static final String ADMIN_APP_NAME           = ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_APP_NAME;

    /**
     *	Constructor.
     */
    public AdminConsoleAdapter() throws IOException {
	initHtml   = Utils.packageResource2String("downloadgui.html");
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

	if (isApplicationLoaded()) {
	    // Let this pass to the admin console (do nothing)
	    handleLoadedState();
	} else {
	    // Console is not yet running...

	    // Only worry about auth before the console is running
	    handleAuth(req, res);

	    // See what type of request this is...
	    InteractionResult ir = getUserInteractionResult(req);
	    if (ir == InteractionResult.CANCEL) {
    // FIXME: What if they clicked Cancel?
	    }
	    synchronized(this) {
		if (isInstalling()) {
		    sendStatusPage(res);
		} else {
		    if (isApplicationLoaded()) {
			// Double check here that it is not now loaded (not
			// likely, but possible)
			handleLoadedState();
		    } else if (!hasPermission(ir)) {
			// Ask for permission
			sendConsentPage(req, res);
		    } else {
			try {
			    // We have permission and now we should install
			    // (or load) the application.
			    setInstalling(true);
			    setStateMsg(AdapterState.INSTALLING);
			    startThread();  // Thread must set installing false
			} catch (Exception ex) {
			    // Ensure we haven't crashed with the installing
			    // flag set to true (not likely).
			    setInstalling(false);
			    throw new RuntimeException(
				    "Unable to install Admin Console!", ex);
			}
			sendStatusPage(res);
		    }
		}
	    }
	}
    }

    /**
     *
     */
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
     *	<p> This method sets the current state.</p>
     */
    void setStateMsg(AdapterState msg) {
	stateMsg = msg;
    }

    /**
     *	<p> This method returns the current state, which will be one of the
     *	    following values:</p>
     *
     *	<ul><li>AdapterSate.UNINITIAZED</li>
     *	    <li>AdapterSate.INSTALLING</li>
     *	    <li>AdapterSate.APPLICATION_NOT_INSTALLED</li>
     *	    <li>AdapterSate.APPLICATION_INSTALLED_BUT_NOT_LOADED</li>
     *	    <li>AdapterSate.APPLICATION_LOADED</li></ul>
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
	    File realmFile = new File(env.getProps().get(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY) + "/config/admin-keyfile");
	    if (authenticator!=null && realmFile.exists()) {
		if (!authenticator.authenticate(greq.getRequest(), realmFile)) {
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
        setIPSRoot(as.getProperty(ServerTags.IPS_ROOT).getValue());
        setWarFileLocation(as.getProperty(ServerTags.ADMIN_CONSOLE_DOWNLOAD_LOCATION).getValue());
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
	} else if (warFile.exists()) {
// FIXME: set state to DOWNLOADED: adapter.setStateMsg(AdapterState.DOWNLOADED);
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
	// FIXME: Change all INFO to FINE
	log.info("AdminConsoleAdapter's STATE IS: " +  getStateMsg());
	if (log.isLoggable(Level.FINE)) {
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
    private void setWarFileLocation(String value) {
	    if ((value != null) && !("".equals(value))) {
		warFile = new File(ipsRoot, value);
//System.out.println("Admin Console will be downloaded to: " + warFile);
		if (log.isLoggable(Level.FINE)) {
		    log.fine("Admin Console will be downloaded to: "
			    + warFile.getAbsolutePath());
		}
	    } else {
		if (log.isLoggable(Level.INFO)) {
		    log.info("The value (" + value + ") for: "
			    + ServerTags.ADMIN_CONSOLE_DOWNLOAD_LOCATION + " is invalid");
		}
	    }
    }

    /**
     *
     */
    private void setIPSRoot(String value) {
        ipsRoot  = new File(value);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "GlassFish IPS Root: "
                    + ipsRoot.getAbsolutePath());
        }
        if (!ipsRoot.canWrite()) {
            log.warning(ipsRoot.getAbsolutePath() + " can't be written to, download will fail");
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
     *	<p> Determines if the user has permission.</p>
     */
    private boolean hasPermission(InteractionResult ir) {
	//do this quickly as this is going to block the grizzly worker thread!
	//check for returning user?
	if (ir == InteractionResult.OK) {
// FIXME: I need to "remember" this answer in a persistent way!! Or it will popup this message EVERY time after the server restarts.
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
	    if (proxyHost != null) {
		String ps = req.getParameter(PROXY_PORT_PARAM);
		try {
		    proxyPort = Integer.parseInt(ps);
		} catch (NumberFormatException nfe) {
		    //ignore
		}
	    }
// FIXME: I need to "remember" this answer in a persistent way!! Or it will popup this message EVERY time after the server restarts.
	    isOK = true;
	    return InteractionResult.OK;
	} else if (req.getParameter(CANCEL_PARAM) != null) {
	    // Canceled
// FIXME: I need to "remember" this answer in a persistent way!! Or it will popup this message EVERY time after the server restarts.
	    isOK = false;
	    return InteractionResult.CANCEL;
	}

	// This is a first-timer
	return InteractionResult.FIRST_TIMER;
    }

    /**
     *
     */
    private synchronized void sendConsentPage(GrizzlyRequest req, GrizzlyResponse res) { //should have only one caller
	GrizzlyOutputBuffer ob = res.getOutputBuffer();
	res.setStatus(200);
	res.setContentType("text/html");
	byte[] bytes;
	try {
	    try {
		String hp = (contextRoot.endsWith("/")) ? contextRoot : contextRoot + "/";
		/*
		String hp = (contextRoot.startsWith("/")) ? "" : "/";
		hp += contextRoot + "/";
		*/
		bytes = initHtml.replace(MYURL_TOKEN, hp).getBytes();
	    } catch (Exception ex) {
		bytes = ("Catastrophe:" + ex.getMessage()).getBytes();
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
    private void sendStatusPage(GrizzlyResponse res) {
	GrizzlyOutputBuffer ob = res.getOutputBuffer();
	res.setStatus(200);
	res.setContentType("text/html");
	byte[] bytes;
	try {
	    String status = "" + getStateMsg();
	    bytes = statusHtml.replace(STATUS_TOKEN, status).getBytes();
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
    private void handleLoadedState() {
//System.out.println(" Handle Loaded State!!");
	// do nothing
	statusHtml = null;
	initHtml   = null;
    }
    
    public int getListenPort() {
        return epd.getListenPort();
    }
    
    public List<String> getVirtualServers() {
        return epd.getGuiHosts();
    }
}
