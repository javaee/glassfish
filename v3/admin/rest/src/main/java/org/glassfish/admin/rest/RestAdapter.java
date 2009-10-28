/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.admin.rest;

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
import com.sun.logging.LogDomains;
import java.lang.reflect.Method;
import java.util.logging.Level;
import org.glassfish.api.ActionReport;
import org.glassfish.api.container.Adapter;
import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.RestrictTo;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.glassfish.internal.api.AdminAccessController;
import org.glassfish.internal.api.ServerContext;


/**
 * Adapter for REST interface
 * @author Rajeshwar Patil, Ludovic Champenois
 */
public abstract class RestAdapter extends GrizzlyAdapter implements Adapter, PostConstruct, EventListener {

    public final static Logger logger = LogDomains.getLogger(ServerEnvironmentImpl.class, LogDomains.ADMIN_LOGGER);
    public final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(RestAdapter.class);

    @Inject
    ServerEnvironmentImpl env;

    @Inject
    volatile AdminService as = null;

    @Inject
    Events events;
    
    @Inject
    Habitat habitat;

    @Inject(name="server-config")
    Config config;

    CountDownLatch latch = new CountDownLatch(1);

    @Inject
    ServerContext sc;

    @Inject
    RestService restService;

    protected RestAdapter() {
    }


    @Override
    public void postConstruct() {
        epd = new AdminEndpointDecider(config, logger);
        events.register(this);
    }


    @Override
    public void service(GrizzlyRequest req, GrizzlyResponse res) {
        LogHelper.getDefaultLogger().finer("Rest monitoring adapter !");
        LogHelper.getDefaultLogger().finer("Received monitoring resource request: " + req.getRequestURI());

        String requestURI = req.getRequestURI();
        ActionReport report = getClientActionReport(requestURI, req);

        try {
            if (!latch.await(20L, TimeUnit.SECONDS)) {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                String msg = localStrings.getLocalString("rest.adapter.server.wait",
                        "Server cannot process this command at this time, please wait");
                report.setMessage(msg);
                reportError(res, report, HttpURLConnection.HTTP_UNAVAILABLE);
                return;
            } else {
                Cookie[] cookies = req.getCookies();
                boolean isAdminClient = false;
                String uid = RestService.getRestUID();
                if (uid != null) {
                    if (cookies != null) {
                        for (Cookie cookie: cookies) {
                            if (cookie.getName().equals("gfrestuid")) {
                                if (cookie.getValue().equals(uid)) {
                                    isAdminClient = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (!isAdminClient) { //authenticate all clients except admin client
                    if (!authenticate(req, report, res)) //admin client - client with valid rest interface uid
                        return;
                }

                //delegate to adapter managed by Jersey.
                if (adapter == null) {
                    exposeContext();
                }
                ((GrizzlyAdapter)adapter).service(req, res);
            }
        } catch(InterruptedException e) {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                String msg = localStrings.getLocalString("rest.adapter.server.wait",
                        "Server cannot process this command at this time, please wait");
                report.setMessage(msg);
                reportError(res, report, HttpURLConnection.HTTP_UNAVAILABLE); //service unavailable
                return;
        } catch (Exception e) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            String msg = localStrings.getLocalString("rest.adapter.auth.error",
                    "Error authenticating");
            report.setMessage(msg);
            ///report.setActionDescription("Authentication error");
            ///res.setHeader("WWW-Authenticate", "BASIC");
            reportError(res, report, HttpURLConnection.HTTP_UNAUTHORIZED); //authentication error
            return;
        }
        
        try {
            res.setStatus(200);
            res.getOutputStream().flush();
            res.finishResponse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public boolean authenticate(Request req)
            throws Exception {
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
    public List<String> getVirtualServers() {
        return epd.getAsadminHosts();
    }



    protected abstract Set<Class<?>> getResourcesConfig();


    private boolean authenticate(GrizzlyRequest req, ActionReport report, GrizzlyResponse res)
            throws Exception {
        boolean authenticated = authenticate(req.getRequest());
        if (!authenticated) {
            String msg = localStrings.getLocalString("rest.adapter.auth.userpassword",
                    "Invalid user name or password");
            System.out.println("msg: " + msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            report.setActionDescription("Authentication error");
            res.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
            res.setHeader("WWW-Authenticate", "BASIC");
            res.setContentType(report.getContentType());
            report.writeReport(res.getOutputStream());
            res.getOutputStream().flush();
            res.finishResponse();
        }
        return authenticated;
    }


    private ActionReport getClientActionReport(String requestURI, GrizzlyRequest req) {
        ActionReport report=null;

        String userAgent = req.getHeader("User-Agent");
        if (userAgent!=null)
            report = habitat.getComponent(ActionReport.class, userAgent.substring(userAgent.indexOf('/')+1));
        if (report==null) {
            String accept = req.getHeader("Accept");
            if (accept!=null) {
                StringTokenizer st = new StringTokenizer(accept, ",");
                while (report==null && st.hasMoreElements()) {
                    final String scheme=st.nextToken();
                    report = habitat.getComponent(ActionReport.class, scheme.substring(scheme.indexOf('/')+1));
                }
            }
        }
        if (report==null) {
            // get the default one.
            report = habitat.getComponent(ActionReport.class);
        }
        return report;
    }


    private void exposeContext()
            throws EndpointRegistrationException {
        String context = getContextRoot();
        logger.fine("Exposing rest resource context root: " + context);
        if ((context != null) || (!"".equals(context))) {
            Set<Class<?>> classes = getResourcesConfig();

            // we replace the following code with instrospection code
            // in order to not load the jersery class until this method is called.
            // this way, we gain around 90ms at startup time by not loading jersey classes.
            // they are loaded only when a REST service is called.
            //// LazyJerseyInit lazyInit = new LazyJerseyInit();

            try {
                Class<?> lazyInitClass = Class.forName("org.glassfish.admin.rest.LazyJerseyInit");
                Method init = lazyInitClass.getMethod("exposeContext",
                        new Class[]{
                            Set.class,
                            ServerContext.class
                        });
                Object o[] = new Object[2];
                o[0] = classes;
                o[1] = sc;
                adapter = (GrizzlyAdapter) init.invoke(null, o);
                ((GrizzlyAdapter) adapter).setResourcesContextPath(context);
            } catch (Exception ex) {
                logger.log(Level.SEVERE,
                        "Error trying to call org.glassfish.admin.rest.LazyJerseyInit via instrospection: ", ex);

            }


            logger.info("Listening to REST requests at context: " + context + "/domain");
        }
    }


    private void reportError(GrizzlyResponse res, ActionReport report,
            int statusCode) {
        try {
            res.setStatus(statusCode);
            res.setContentType(report.getContentType());
            report.writeReport(res.getOutputStream());
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
