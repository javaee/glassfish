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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.common_impl.LogHelper;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.grizzly.tcp.Request;
import com.sun.logging.LogDomains;
import org.glassfish.admin.payload.PayloadImpl;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.*;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.container.Adapter;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

import java.net.URLDecoder;
import java.util.StringTokenizer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.server.ServerEnvironmentImpl;

import java.net.HttpURLConnection;
import com.sun.enterprise.universal.GFBase64Decoder;
import com.sun.enterprise.v3.admin.adapter.AdminEndpointDecider;
import com.sun.enterprise.v3.admin.listener.GenericJavaConfigListener;
import com.sun.enterprise.v3.admin.listener.SystemPropertyListener;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.hk2.component.ConstructorWomb;

import java.io.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.lang.annotation.Annotation;

import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.RestrictTo;
import org.glassfish.internal.api.*;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.ConfigSupport;

/**
 * Listen to admin commands...
 * @author dochez
 */
public abstract class AdminAdapter extends GrizzlyAdapter implements Adapter, PostConstruct, EventListener {

    public final static String VS_NAME="__asadmin";
    public final static String PREFIX_URI = "/" + VS_NAME;
    public final static Logger logger = LogDomains.getLogger(ServerEnvironmentImpl.class, LogDomains.ADMIN_LOGGER);
    public final static LocalStringManagerImpl adminStrings = new LocalStringManagerImpl(AdminAdapter.class);
    private final static String GET = "GET";
    private final static String POST = "POST";
    private static final GFBase64Decoder decoder = new GFBase64Decoder();
    private static final String BASIC = "Basic ";

    private static final String QUERY_STRING_SEPARATOR = "&";

    @Inject
    ModulesRegistry modulesRegistry;

    @Inject
    CommandRunnerImpl commandRunner;

    @Inject
    ServerEnvironmentImpl env;


    @Inject
    Events events;
    
    @Inject(name="server-config")
    Config config;

    private AdminEndpointDecider epd = null;
    
    @Inject
    ServerContext sc;

    @Inject
    Habitat habitat;

    @Inject
    volatile AdminService as = null;

    @Inject
    volatile Domain domain;

    @Inject
    GenericJavaConfigListener listener;

    final Class<? extends Privacy> privacyClass;

    private boolean isRegistered = false;
            
    CountDownLatch latch = new CountDownLatch(1);

    protected AdminAdapter(Class<? extends Privacy> privacyClass) {
        this.privacyClass = privacyClass;
    }

    public void postConstruct() {
        events.register(this);
        
        epd = new AdminEndpointDecider(config, logger);
        registerDynamicReconfigListeners();
            this.setHandleStaticResources(true);
            this.setRootFolder(env.getProps().get(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY) + "/asadmindocroot/");
    }

    /**
     * Call the service method, and notify all listeners
     *
     * @exception Exception if an error happens during handling of
     *   the request. Common errors are:
     *   <ul><li>IOException if an input/output error occurs and we are
     *   processing an included servlet (otherwise it is swallowed and
     *   handled by the top level error handler mechanism)
     *       <li>ServletException if a servlet throws an exception and
     *  we are processing an included servlet (otherwise it is swallowed
     *  and handled by the top level error handler mechanism)
     *  </ul>
     *  Tomcat should be able to handle and log any other exception ( including
     *  runtime exceptions )
     */
    public void service(GrizzlyRequest req, GrizzlyResponse res) {



        LogHelper.getDefaultLogger().finer("Admin adapter !");
        LogHelper.getDefaultLogger().finer("Received something on " + req.getRequestURI());
        LogHelper.getDefaultLogger().finer("QueryString = " + req.getQueryString());

        String requestURI = req.getRequestURI();
    /*    if (requestURI.startsWith("/__asadmin/ADMINGUI")) {
            super.service(req, res);

        }*/
        ActionReport report = getClientActionReport(requestURI, req);
        // remove the qualifier if necessary
        if (requestURI.indexOf('.')!=-1) {
            requestURI = requestURI.substring(0, requestURI.indexOf('.'));
        }

        Payload.Outbound outboundPayload = PayloadImpl.Outbound.newInstance();

        try {
            if (!latch.await(20L, TimeUnit.SECONDS)) {
                report = this.getClientActionReport(req.getRequestURI(), req);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage("V3 cannot process this command at this time, please wait");            
            } else {
                if (!authenticate(req, report, res))
                    return;
                report = doCommand(requestURI, req, report, outboundPayload);
            }
        } catch(InterruptedException e) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage("V3 cannot process this command at this time, please wait");                        
        } catch (Exception e) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage("Exception while processing command: " + e);
        }
        
        try {
            res.setStatus(200);
            /*
             * Format the command result report into the first part (part #0) of
             * the outbound payload and set the response's content type based
             * on the payload's.  If the report is the only part then the
             * stream will be written as content type text/something and
             * will contain only the report.  If the payload already has
             * content - such as files to be downloaded, for example - then the
             * content type of the payload reflects its multi-part nature and
             * an implementation-specific content type will be set in the response.
             */
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            report.writeReport(baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            outboundPayload.addPart(0, report.getContentType(), "report", 
                    null /* no special props for report */, bais);
            res.setContentType(outboundPayload.getContentType());
            outboundPayload.writeTo(res.getOutputStream());
            res.getOutputStream().flush();
            res.finishResponse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean authenticate(Request req)
            throws Exception {
        String[] up = getUserPassword(req);
        String user = up[0];
        String password = up.length > 1 ? up[1] : "";
        AdminAccessController authenticator = habitat.getByContract(AdminAccessController.class);
        if (authenticator != null) {
            return authenticator.loginAsAdmin(user, password, as.getAuthRealmName());
        }
        return true;   //if the authenticator is not available, allow all access - per Jerome
    }

    /** A convenience method to extract user name from a request. It assumes the HTTP Basic Auth.
     *
     * @param req instance of Request
     * @return a two-element string array. If Auth header exists and can be correctly decoded, returns the user name
     *   and password as the two elements. If any error occurs or if the header does not exist, returns an array with
     *   two blank strings. Never returns a null.
     * @throws IOException in case of error with decoding the buffer (HTTP basic auth)
     */
    public static String[] getUserPassword(Request req) throws IOException {
        //implementation note: other adapters make use of this method
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null) {
            return new String[]{"", ""};
        }
        String enc = authHeader.substring(BASIC.length());
        String dec = new String(decoder.decodeBuffer(enc));
        int i = dec.indexOf(':');
        if (i < 0)
            return new String[] { "", "" };
        return new String[] { dec.substring(0, i), dec.substring(i + 1) };
    }

    private boolean authenticate(GrizzlyRequest req, ActionReport report, GrizzlyResponse res)
            throws Exception {
        boolean authenticated = authenticate(req.getRequest());
        if (!authenticated) {
            String msg = adminStrings.getLocalString("adapter.auth.userpassword",
                    "Invalid user name or password");
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

        // first we look at the command extension (ie list-applications.[json | html | mf]
        if (requestURI.indexOf('.')!=-1) {
            String qualifier = requestURI.substring(requestURI.indexOf('.')+1);
            report = habitat.getComponent(ActionReport.class, qualifier);
        } else {
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
        }
        if (report==null) {
            // get the default one.
            report = habitat.getComponent(ActionReport.class, "html");
        }
        return report;
    }

    protected abstract boolean validatePrivacy(AdminCommand command);

    private ActionReport doCommand(String requestURI, GrizzlyRequest req, ActionReport report,
            Payload.Outbound outboundPayload) {

        if (!requestURI.startsWith(getContextRoot())) {
            String msg = adminStrings.getLocalString("adapter.panic",
                    "Wrong request landed in AdminAdapter {0}", requestURI);
            report.setMessage(msg);
            LogHelper.getDefaultLogger().info(msg);
            return report;
        }

        // wbn handle no command and no slash-suffix
        String command = "";

        if (requestURI.length() > getContextRoot().length() + 1)
            command = requestURI.substring(getContextRoot().length() + 1);

        final ParameterMap parameters = extractParameters(req.getQueryString());
        try {
            Payload.Inbound inboundPayload = PayloadImpl.Inbound.newInstance(
                    req.getContentType(), req.getInputStream());
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("***** AdminAdapter "+req.getMethod()+"  *****");
            }
            AdminCommand adminCommand = commandRunner.getCommand(command, report, logger);
            if (adminCommand==null) {
                String message =
                    adminStrings.getLocalString("adapter.command.notfound",
                        "Command {0} not found", command);
                // cound't find command, not a big deal
                logger.log(Level.FINE, message);
                report.setMessage(message);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return report;
            }
            if (validatePrivacy(adminCommand)) {
            //if (adminCommand.getClass().getAnnotation(Visibility.class).privacy().equals(visibility.privacy())) {
                // todo : needs to be changed, we should reuse adminCommand
                CommandRunner.CommandInvocation inv = commandRunner.getCommandInvocation(command, report);
                inv.parameters(parameters).inbound(inboundPayload).outbound(outboundPayload).execute();
            } else {
                report.failure( logger,
                                adminStrings.getLocalString("adapter.wrongprivacy",
                                    "Command {0} does not have {1} visibility",
                                    command, privacyClass.getSimpleName().toLowerCase()),
                                null);
                return report;

            }
        } catch (Throwable t) {
            /*
             * Must put the error information into the report
             * for the client to see it.
             */
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(t);
            report.setMessage(t.getLocalizedMessage());
            report.setActionDescription("Last-chance AdminAdapter exception handler");
        }
        return report;
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
     
     
    /**
     *  extract parameters from URI and save it in ParameterMap obj
     *  
     *  @params requestString string URI to extract
     *
     *  @returns ParameterMap
     */
    ParameterMap extractParameters(final String requestString) {
        // extract parameters...
        final ParameterMap parameters = new ParameterMap();
        StringTokenizer stoken = new StringTokenizer(requestString == null ? "" : requestString, QUERY_STRING_SEPARATOR);
        while (stoken.hasMoreTokens()) {
            String token = stoken.nextToken();            
            if (token.indexOf("=") == -1) 
                continue;
            String paramName = null;
            String value = null;
            paramName = token.substring(0, token.indexOf("="));
            value = token.substring(token.indexOf("=") + 1);
            try {
                value = URLDecoder.decode(value, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.log(Level.WARNING, adminStrings.getLocalString("adapter.param.decode",
                        "Cannot decode parameter {0} = {1}"));
            }
            parameters.add(paramName, value);
        }

        // Dump parameters...
        if (logger.isLoggable(Level.FINER)) {
            for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
                for (String v : entry.getValue())
                    logger.finer("Key " + entry.getKey() + " = " + v);
            }
        }
        return parameters;
    }

    public void event(@RestrictTo(EventTypes.SERVER_READY_NAME) Event event) {
        if (event.is(EventTypes.SERVER_READY)) {
            latch.countDown();
            logger.fine("Ready to receive administrative commands");       
        }
        //the count-down does not start if any other event is received
    }
    
    
    public int getListenPort() {
        return epd.getListenPort();
    }
    
    public List<String> getVirtualServers() {
        return epd.getAsadminHosts();
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
    
    private void registerSystemPropertyListener() {
        ObservableBean ob = (ObservableBean)ConfigSupport.getImpl(domain);
        SystemPropertyListener ls = habitat.getComponent(SystemPropertyListener.class);
        ob.addListener(ls); //there should be a better way to do this ...
        Server s = domain.getServerNamed(SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME);
        ob = (ObservableBean)ConfigSupport.getImpl(s);
        ob.addListener(ls);
    }
    private void registerDynamicReconfigListeners() {
        registerSystemPropertyListener();
    }
}
