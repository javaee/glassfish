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

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.StringTokenizer;

import com.sun.enterprise.module.common_impl.LogHelper;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.logging.LogDomains;

import org.glassfish.admin.rest.RestService;
import org.glassfish.api.ActionReport;
import org.glassfish.api.container.Adapter;
import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.api.container.RequestDispatcher;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.RestrictTo;
import org.glassfish.internal.api.*;
import org.glassfish.server.ServerEnvironmentImpl;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;

import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.util.http.Cookie;

import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.ResourceConfig;


/**
 * Adapter for REST interface
 * @author Rajeshwar Patil
 */
public abstract class RestAdapter extends GrizzlyAdapter implements Adapter, PostConstruct, EventListener {

    public final static Logger logger = LogDomains.getLogger(ServerEnvironmentImpl.class, LogDomains.ADMIN_LOGGER);
    public final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(RestAdapter.class);

    @Inject
    ServerEnvironmentImpl env;

    @Inject(optional=true)
    AdminAuthenticator authenticator = null;

    @Inject
    Events events;
    
    @Inject
    Habitat habitat;

    CountDownLatch latch = new CountDownLatch(1);

    @Inject
    ClassLoaderHierarchy classLoaderHierrachy;

    @Inject
    RestService restService;

    protected RestAdapter() {
    }


    public void postConstruct() {
        events.register(this);
    }


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


    public boolean authenticate(Request req, ServerEnvironmentImpl serverEnviron)
            throws Exception {
        File realmFile = new File(serverEnviron.getProps().get(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY) + "/config/admin-keyfile");
        if (authenticator != null && realmFile.exists()) {
           return authenticator.authenticate(req, realmFile);
        }
        // no authenticator, this is fine.
        return true;
    }


    /**
     * Finish the response and recycle the request/response tokens. Base on
     * the connection header, the underlying socket transport will be closed
     */
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


    public void event(@RestrictTo(EventTypes.SERVER_READY_NAME) Event event) {
        if (event.is(EventTypes.SERVER_READY)) {
            latch.countDown();
            logger.fine("Ready to receive REST resource requests");
        }
        //the count-down does not start if any other event is received
    }


    protected abstract ResourceConfig getResourceConfig();


    private boolean authenticate(GrizzlyRequest req, ActionReport report, GrizzlyResponse res)
            throws Exception {
        boolean authenticated = authenticate(req.getRequest(), env);
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
        logger.fine("Exposing rest resource context root: " +  context);
        if ((context != null) || (!"".equals(context))) {
            RequestDispatcher rd =
                    habitat.getComponent(RequestDispatcher.class);
            Collection<String> virtualserverName = new ArrayList<String>();
            virtualserverName.add("__asadmin");

            ResourceConfig rc = getResourceConfig();

            //Use common classloader. Jersey artifacts are not visible through
            //module classloader
            ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                ClassLoader apiClassLoader = classLoaderHierrachy.getCommonClassLoader();
                Thread.currentThread().setContextClassLoader(apiClassLoader);
                adapter = ContainerFactory.createContainer(com.sun.grizzly.tcp.Adapter.class, rc);
            } finally {
                Thread.currentThread().setContextClassLoader(originalContextClassLoader);
            }

            ((GrizzlyAdapter) adapter).setResourcesContextPath(context);

            rd.registerEndpoint(context, virtualserverName, this, null);
            logger.info("Listening to REST requests at context: " +
                    context + "/domain");
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
}
