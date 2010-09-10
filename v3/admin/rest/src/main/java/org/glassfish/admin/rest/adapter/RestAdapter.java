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

package org.glassfish.admin.rest.adapter;

import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.module.common_impl.LogHelper;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.v3.admin.AdminAdapter;
import com.sun.enterprise.v3.admin.adapter.AdminEndpointDecider;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.grizzly.util.http.Cookie;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import javax.security.auth.login.LoginException;

import org.glassfish.admin.rest.LazyJerseyInit;
import org.glassfish.admin.rest.RestService;
import org.glassfish.admin.rest.SessionManager;
import org.glassfish.api.ActionReport;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import org.glassfish.admin.rest.CliFailureException;
import org.glassfish.admin.rest.provider.ActionReportResultHtmlProvider;
import org.glassfish.admin.rest.provider.ActionReportResultJsonProvider;
import org.glassfish.admin.rest.provider.ActionReportResultXmlProvider;
import org.glassfish.admin.rest.provider.BaseProvider;
import org.glassfish.admin.rest.results.ActionReportResult;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.glassfish.internal.api.AdminAccessController;
import org.glassfish.internal.api.ServerContext;


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
        LogHelper.getDefaultLogger().finer("Received resource request: " + req.getRequestURI());

        try {
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

                if (!authenticate(req)) {
                    //Could not authenticate throw error
                    String msg = localStrings.getLocalString("rest.adapter.auth.userpassword", "Invalid user name or password");
                    res.setHeader("WWW-Authenticate", "BASIC");
                    reportError(req, res, HttpURLConnection.HTTP_UNAUTHORIZED, msg);
                    return;
                }

                //delegate to adapter managed by Jersey.
                if (adapter == null) {
                    //is the URL contains ?ASM=true, force ASM for now. This is for testing period for now.
                    boolean useASM = "true".equals(req.getParameter("ASM"));
                    exposeContext(useASM);
                }

                ((GrizzlyAdapter)adapter).service(req, res);
                int status = res.getStatus();
                if (status < 200 || status > 299) {
                    String message = httpStatus.get(status);
                    if (message == null) {
                        // i18n
                        message = "Request returned " + status;
                    }

                    reportError(req, res, status, message);
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
        } catch (CliFailureException e) {
            reportError(req, res, HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage());
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

    private boolean authenticate(GrizzlyRequest req) throws LoginException, IOException {
        boolean authenticated = false;

        authenticated = authenticateViaLocalPassword(req);

        if(!authenticated) {
            authenticated = authenticateViaRestToken(req);
        }
        
        if(!authenticated) {
            authenticated = authenticateViaAdminRealm(req.getRequest());
        }

        return authenticated;

    }

    private boolean authenticateViaRestToken(GrizzlyRequest req) { 
        boolean authenticated = false;
//        String restToken = req.getAuthorization(); //TODO investigate using Authorization header for token
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
            authenticated  = SessionManager.getSessionManager().authenticate(restToken, req);
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


    private boolean authenticateViaAdminRealm(Request req) throws LoginException, IOException  {
        String[] up = AdminAdapter.getUserPassword(req);
        String user = up[0];
        String password = up.length > 1 ? up[1] : "";
        AdminAccessController authenticator = habitat.getByContract(AdminAccessController.class);
        if (authenticator != null) {
            return authenticator.loginAsAdmin(user, password, as.getAuthRealmName());
        }
        return true;   //if the authenticator is not available, allow all access - per Jerome
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



    protected abstract Set<Class<?>> getResourcesConfig(boolean useASM);

    private String getAcceptedMimeType(GrizzlyRequest req) {
        String type = null;
        String requestURI = req.getRequestURI();
        Set<String> acceptableTypes = new HashSet<String>() {{ add("html"); add("xml"); add("json"); }};

        // first we look at the command extension (ie list-applications.[json | html | mf]
        if (requestURI.indexOf('.')!=-1) {
            type = requestURI.substring(requestURI.indexOf('.')+1);
        } else {
            String userAgent = req.getHeader("User-Agent");
            if (userAgent != null) {
                String accept = req.getHeader("Accept");
                if (accept != null) {
                    if (accept.indexOf("html") != -1) {//html is possible so get it...
                        return "html";
                    }
                    StringTokenizer st = new StringTokenizer(accept, ",");
                    while (st.hasMoreElements()) {
                        String scheme=st.nextToken();
                        scheme = scheme.substring(scheme.indexOf('/')+1);
                        if (acceptableTypes.contains(scheme)) {
                            type = scheme;
                            break;
                        }
                    }
                }
            }
        }

        return type;
    }

    private ActionReport getClientActionReport(GrizzlyRequest req) {
        ActionReport report=null;
        String requestURI = req.getRequestURI();
        String acceptedMimeType = getAcceptedMimeType(req);
        report = habitat.getComponent(ActionReport.class, acceptedMimeType);

        if (report==null) {
            // get the default one.
            report = habitat.getComponent(ActionReport.class, "html");
        }
        report.setActionDescription("REST");
        return report;
    }


    private void exposeContext(boolean useASM)
            throws EndpointRegistrationException {
        String context = getContextRoot();
        logger.fine("Exposing rest resource context root: " + context);
        if ((context != null) || (!"".equals(context))) {
            Set<Class<?>> classes = getResourcesConfig(useASM);
            adapter = LazyJerseyInit.exposeContext(classes, sc);
            ((GrizzlyAdapter) adapter).setResourcesContextPath(context);
            
            logger.info("Listening to REST requests at context: " + context + "/domain");
        }
    }


    private void reportError(GrizzlyRequest req, GrizzlyResponse res, int statusCode, String msg) {
        try {
            // TODO: There's a lot of arm waving and flailing here.  I'd like this to be cleaner, but I don't
            // have time at the moment.  jdlee 8/11/10
            RestActionReporter report = new RestActionReporter(); //getClientActionReport(req);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            BaseProvider<ActionReportResult> provider;
            String type = getAcceptedMimeType(req);
            if ("xml".equals(type)) {
                res.setContentType(MediaType.APPLICATION_XML);
                provider = new ActionReportResultXmlProvider();
            } else if ("json".equals(type)) {
                res.setContentType(MediaType.APPLICATION_JSON);
                provider = new ActionReportResultJsonProvider();
            } else {
                res.setContentType(MediaType.TEXT_HTML);
                provider = new ActionReportResultHtmlProvider();
            }
            res.setStatus(statusCode);
            res.getOutputStream().write(provider.getContent(new ActionReportResult(report)).getBytes());
            res.getOutputStream().flush();
            res.finishResponse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private com.sun.grizzly.tcp.Adapter adapter = null;
    private boolean isRegistered = false;
    private AdminEndpointDecider epd = null;
}
