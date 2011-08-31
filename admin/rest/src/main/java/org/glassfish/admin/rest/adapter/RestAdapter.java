/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2011 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.adapter;

import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.module.common_impl.LogHelper;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.v3.admin.adapter.AdminEndpointDecider;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.grizzly.util.http.Cookie;
import com.sun.logging.LogDomains;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import javax.security.auth.login.LoginException;
import java.util.logging.Logger;

import org.glassfish.admin.rest.LazyJerseyInterface;
import org.glassfish.admin.rest.ResourceUtil;
import org.glassfish.admin.rest.RestService;
import org.glassfish.admin.rest.SessionManager;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.container.Adapter;
import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.RestrictTo;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.glassfish.admin.rest.Constants;

import org.glassfish.internal.api.AdminAccessController;
import org.glassfish.internal.api.ServerContext;
import java.util.logging.Level;

/**
 * Adapter for REST interface
 * @author Rajeshwar Patil, Ludovic Champenois
 */
public abstract class RestAdapter extends GrizzlyAdapter implements Adapter, PostConstruct, EventListener {

    public final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(RestAdapter.class);

    @Inject
    volatile AdminService as = null;

    @Inject
    Events events;

    @Inject
    Habitat habitat;

    @Inject(name=ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    CountDownLatch latch = new CountDownLatch(1);

    @Inject
    ServerContext sc;

    @Inject
    ServerEnvironment serverEnvironment;

    @Inject
    SessionManager sessionManager;

    private static final Logger logger = LogDomains.getLogger(RestAdapter.class, LogDomains.ADMIN_LOGGER);
    private volatile LazyJerseyInterface lazyJerseyInterface =null;

    private Map<Integer, String> httpStatus = new HashMap<Integer, String>() {{
        put(404, "Resource not found");
        put(500, "A server error occurred. Please check the server logs.");
    }};

    protected RestAdapter() {
        setAllowEncodedSlash(true);
    }


    @Override
    public void postConstruct() {
        epd = new AdminEndpointDecider(config, logger);
        events.register(this);
    }


    @Override
    public void service(GrizzlyRequest req, GrizzlyResponse res) {
        LogHelper.getDefaultLogger().finer("Rest adapter !");
        LogHelper.getDefaultLogger().log(Level.FINER, "Received resource request: {0}", req.getRequestURI());

        try {
            res.setCharacterEncoding(Constants.ENCODING);
            if (!latch.await(20L, TimeUnit.SECONDS)) {
                String msg = localStrings.getLocalString("rest.adapter.server.wait",
                        "Server cannot process this command at this time, please wait");
                reportError(req, res, HttpURLConnection.HTTP_UNAVAILABLE, msg);
                return;
            } else {
                if(serverEnvironment.isInstance()) {
                    if(!"GET".equalsIgnoreCase(req.getRequest().method().getString() ) ) {
                        String msg = localStrings.getLocalString("rest.resource.only.GET.on.instance", "Only GET requests are allowed on an instance that is not DAS.");
                        reportError(req, res, HttpURLConnection.HTTP_FORBIDDEN, msg);
                        return;
                    }
                }

                AdminAccessController.Access access = authenticate(req);
                if (access == AdminAccessController.Access.FULL) {
                    //Use double checked locking to lazily initialize adapter
                    if (adapter == null) {
                        synchronized(com.sun.grizzly.tcp.Adapter.class) {
                            if(adapter == null) {
                                exposeContext();  //Initializes adapter
                            }
                        }
                    }
                    //delegate to adapter managed by Jersey.
                    ((GrizzlyAdapter)adapter).service(req, res);

                } else { // Access != FULL

                    String msg;
                    int status;
                    if(access == AdminAccessController.Access.NONE) {
                        status = HttpURLConnection.HTTP_UNAUTHORIZED;
                        msg = localStrings.getLocalString("rest.adapter.auth.userpassword", "Invalid user name or password");
                        res.setHeader("WWW-Authenticate", "BASIC");
                    } else {
                        assert access == AdminAccessController.Access.FORBIDDEN;
                        status = HttpURLConnection.HTTP_FORBIDDEN;
                        msg = localStrings.getLocalString("rest.adapter.auth.forbidden", "Remote access not allowed. If you desire remote access, please turn on secure admin");
                    }
                    reportError(req, res, status, msg);
                }
            }
        } catch(InterruptedException e) {
                String msg = localStrings.getLocalString("rest.adapter.server.wait",
                        "Server cannot process this command at this time, please wait");
                reportError(req, res, HttpURLConnection.HTTP_UNAVAILABLE, msg); //service unavailable
                return;
        } catch(IOException e) {
                String msg = localStrings.getLocalString("rest.adapter.server.ioexception",
                        "REST: IO Exception "+e.getLocalizedMessage());
                reportError(req, res, HttpURLConnection.HTTP_UNAVAILABLE, msg); //service unavailable
                return;
        } catch(LoginException e) {
            String msg = localStrings.getLocalString("rest.adapter.auth.error", "Error authenticating");
            reportError(req, res, HttpURLConnection.HTTP_UNAUTHORIZED, msg); //authentication error
            return;
        } catch (Exception e) {
            StringWriter result = new StringWriter();
            PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            String msg = localStrings.getLocalString("rest.adapter.server.exception",
                    "REST:  Exception " + result.toString());
            reportError(req, res, HttpURLConnection.HTTP_UNAVAILABLE, msg); //service unavailable
            return;
        }
    }

    /**
     * Authenticate given request
     * @return Access as determined by authentication process.
     *         If authentication succeeds against local password or rest token FULL access is granted
     *         else the access is as returned by admin authenticator
     * @see ResourceUtil.authenticateViaAdminRealm(Habitat, Request)
     *
     */
    private AdminAccessController.Access authenticate(GrizzlyRequest req) throws LoginException, IOException {
        AdminAccessController.Access access = AdminAccessController.Access.FULL;
        boolean authenticated = authenticateViaLocalPassword(req);
        if (!authenticated) {
            authenticated = authenticateViaRestToken(req);
            if (!authenticated) {
                access = ResourceUtil.authenticateViaAdminRealm(habitat, req);
            }
        }
        return access;
    }

    private boolean authenticateViaRestToken(GrizzlyRequest req) {
        boolean authenticated = false;
        Cookie[] cookies = req.getCookies();
        String restToken = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("gfresttoken".equals(cookie.getName())) {
                    restToken = cookie.getValue();
                }
            }
        }

        if(restToken != null) {
            authenticated  = sessionManager.authenticate(restToken, req);
        }
        return authenticated;
    }

    private boolean authenticateViaLocalPassword(GrizzlyRequest req) {
        Cookie[] cookies = req.getCookies();
        boolean authenticated = false;
        String uid = RestService.getRestUID();
        if (uid != null) {
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("gfrestuid")) {
                        if (cookie.getValue().equals(uid)) {
                            authenticated = true;
                            break;
                        }
                    }
                }
            }
        }
        return authenticated;
    }


    /**
     * Finish the response and recycle the request/response tokens. Base on
     * the connection header, the underlying socket transport will be closed
     */
    @Override
    public void afterService(GrizzlyRequest req, GrizzlyResponse res) throws Exception {

    }


    /**
     * Notify all container event listeners that a particular event has
     * occurred for this Adapter.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param data Event data
     */
    public void fireAdapterEvent(String type, Object data) {

    }


    @Override
    public void event(@RestrictTo(EventTypes.SERVER_READY_NAME) Event event) {
        if (event.is(EventTypes.SERVER_READY)) {
            latch.countDown();
            logger.fine("Ready to receive REST resource requests");
        }
        //the count-down does not start if any other event is received
    }


    /**
     * Checks whether this adapter has been registered as a network endpoint.
     */
    @Override
    public boolean isRegistered() {
        return isRegistered;
    }


    /**
     * Marks this adapter as having been registered or unregistered as a
     * network endpoint
     */
    @Override
    public void setRegistered(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }


    @Override
    public int getListenPort() {
        return epd.getListenPort();
    }


    @Override
    public InetAddress getListenAddress() {
        return epd.getListenAddress();
    }


    @Override
    public List<String> getVirtualServers() {
        return epd.getAsadminHosts();
    }



    protected abstract Set<Class<?>> getResourcesConfig();


//    private ActionReport getClientActionReport(GrizzlyRequest req) {
//        ActionReport report=null;
//        String requestURI = req.getRequestURI();
//        String acceptedMimeType = getAcceptedMimeType(req);
//        report = habitat.getComponent(ActionReport.class, acceptedMimeType);
//
//        if (report==null) {
//            // get the default one.
//            report = habitat.getComponent(ActionReport.class, "html");
//        }
//        report.setActionDescription("REST");
//        return report;
//    }

    /*
     * dynamically load the class that contains all references to Jersey APIs
     * so that Jersey is not loaded when the RestAdapter is loaded at boot time
     * gain a few 100millis at GlassFish startyp time
+     */
    protected LazyJerseyInterface getLazyJersey() {
        if (lazyJerseyInterface != null) {
            return lazyJerseyInterface;
        }
        synchronized (com.sun.grizzly.tcp.Adapter.class) {
            if (lazyJerseyInterface == null) {
               try {
                    Class<?> lazyInitClass = Class.forName("org.glassfish.admin.rest.LazyJerseyInit");
                    lazyJerseyInterface = (LazyJerseyInterface) lazyInitClass.newInstance();
                } catch (Exception ex) {
                    logger.log(Level.SEVERE,
                            "Error trying to call org.glassfish.admin.rest.LazyJerseyInit via instrospection: ", ex);
                }
            }
        }
        return lazyJerseyInterface;

    }

    private void exposeContext()
            throws EndpointRegistrationException {
        String context = getContextRoot();
        logger.fine("Exposing rest resource context root: " + context);
        if ((context != null) || (!"".equals(context))) {
            Set<Class<?>> classes = getResourcesConfig();
           // adapter = LazyJerseyInit.exposeContext(classes, sc, habitat);
            adapter = getLazyJersey().exposeContext(classes, sc, habitat);
            ((GrizzlyAdapter) adapter).setResourcesContextPath(context);
            
            logger.log(Level.INFO, "rest.rest_interface_initialized", context);
        }
    }


    private void reportError(GrizzlyRequest req, GrizzlyResponse res, int statusCode, String msg) {
        // delegate to the class that knows about all Jersey API
        // for faster startup of this adapter.
        getLazyJersey().reportError(req, res, statusCode, msg);
    }

    private volatile com.sun.grizzly.tcp.Adapter adapter = null;
    private boolean isRegistered = false;
    private AdminEndpointDecider epd = null;
}
