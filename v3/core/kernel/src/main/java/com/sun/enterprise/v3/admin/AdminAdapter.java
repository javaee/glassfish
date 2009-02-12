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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.common_impl.LogHelper;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.grizzly.tcp.Request;
import com.sun.logging.LogDomains;
import java.io.BufferedInputStream;
import java.io.InputStream;
import org.glassfish.api.ActionReport;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.container.Adapter;
import org.glassfish.internal.api.AdminAuthenticator;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.universal.glassfish.SystemPropertyConstants;
import org.glassfish.server.ServerEnvironmentImpl;

import java.net.HttpURLConnection;
import com.sun.enterprise.universal.BASE64Decoder;
import com.sun.enterprise.v3.admin.adapter.AdminEndpointDecider;
import com.sun.enterprise.v3.admin.listener.GenericJavaConfigListener;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.hk2.component.ConstructorWomb;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.RestrictTo;
import org.glassfish.internal.api.ServerContext;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigListener;

/**
 * Listen to admin commands...
 * @author dochez
 */
@Service
public class AdminAdapter extends GrizzlyAdapter implements Adapter, PostConstruct, EventListener {

    public final static String VS_NAME="__asadmin";
    public final static String PREFIX_URI = "/" + VS_NAME;
    public final static Logger logger = LogDomains.getLogger(ServerEnvironmentImpl.class, LogDomains.ADMIN_LOGGER);
    public final static LocalStringManagerImpl adminStrings = new LocalStringManagerImpl(AdminAdapter.class);
    public final static String GFV3 = "gfv3";
    private final static String GET = "GET";
    private final static String POST = "POST";
    private static final BASE64Decoder decoder = new BASE64Decoder();
    private static final String BASIC = "Basic ";
    private static final String UPLOAD_DIR_PREFIX = "upl-";

    private static final String QUERY_STRING_SEPARATOR = "&";

    @Inject
    ModulesRegistry modulesRegistry;

    @Inject
    CommandRunnerImpl commandRunner;

    @Inject
    ServerEnvironmentImpl env;

    @Inject(optional=true)
    AdminAuthenticator authenticator=null;

    @Inject
    Events events;
    
    @Inject(name="server-config")
    Config config;

    private AdminEndpointDecider epd = null;
    
    @Inject
    ServerContext sc;

    @Inject
    Habitat habitat;

    private boolean isRegistered = false;
            
    CountDownLatch latch = new CountDownLatch(1);

    public void postConstruct() {
        events.register(this);
        
        epd = new AdminEndpointDecider(config, logger);
        registerJavaConfigListener();
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

        try {
            if (!latch.await(20L, TimeUnit.SECONDS)) {
                report = this.getClientActionReport(req.getRequestURI(), req);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage("V3 cannot process this command at this time, please wait");            
            } else {
                if (!authenticate(req, report, res))
                    return;
                report = doCommand(requestURI, req, report);
            }
        } catch(InterruptedException e) {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage("V3 cannot process this command at this time, please wait");                        
        } catch (Exception e) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage("Error authenticating");
        }
        
        try {
            res.setStatus(200);
            res.setContentType(report.getContentType());
            report.writeReport(res.getOutputStream());
            res.getOutputStream().flush();
            res.finishResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean authenticate(Request req, ServerEnvironmentImpl serverEnviron)
            throws Exception {

        File realmFile = new File(serverEnviron.getProps().get(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY) + "/config/admin-keyfile");
        if (authenticator!=null && realmFile.exists()) {
           return authenticator.authenticate(req, realmFile);
        }
        // no authenticator, this is fine.
        return true;

    }

    private boolean authenticate(GrizzlyRequest req, ActionReport report, GrizzlyResponse res)
            throws Exception {
        boolean authenticated = authenticate(req.getRequest(), env);
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


        ActionReport report;

        // first we look at the command extension (ie list-applications.[json | html | mf]
        if (requestURI.indexOf('.')!=-1) {
            String qualifier = requestURI.substring(requestURI.indexOf('.')+1);
            report = habitat.getComponent(ActionReport.class, qualifier);
        } else {
            String userAgent = req.getHeader("User-Agent");
            report = habitat.getComponent(ActionReport.class, userAgent.substring(userAgent.indexOf('/')+1));
            if (report==null) {
                String accept = req.getHeader("Accept");
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

    private ActionReport doCommand(String requestURI, GrizzlyRequest req, ActionReport report) {

        if (!requestURI.startsWith(PREFIX_URI)) {
            String msg = adminStrings.getLocalString("adapter.panic",
                    "Wrong request landed in AdminAdapter {0}", requestURI);
            report.setMessage(msg);
            LogHelper.getDefaultLogger().info(msg);
            return report;
        }

        // wbn handle no command and no slash-suffix
        String command = "";

        if (requestURI.length() > PREFIX_URI.length() + 1) 
            command = requestURI.substring(PREFIX_URI.length() + 1);

        final Properties parameters = extractParameters(req.getQueryString());
        UploadedFilesInfo uploadedFilesInfo = null;
        try {
            if (req.getMethod().equalsIgnoreCase(GET)) {
                logger.fine("***** AdminAdapter GET  *****");
                commandRunner.doCommand(command, parameters, report);
            } 
            else if (req.getMethod().equalsIgnoreCase(POST)) {
                logger.fine("***** AdminAdapter POST *****");
                /*
                 * Extract any uploaded files from the POST payload.
                 */
                uploadedFilesInfo = new UploadedFilesInfo(req.getInputStream(), report);
                
                commandRunner.doCommand(command, parameters, report, uploadedFilesInfo.getFiles());
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
        } finally {
            if (uploadedFilesInfo != null) {
                uploadedFilesInfo.cleanup();
            }
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
     * Returns the context root for this adapter
     *
     * @return context root
     */
    public String getContextRoot() {
        return epd.getAsadminContextRoot();
    }


     
     
    /**
     *  extract parameters from URI and save it in Properties obj
     *  
     *  @params requestString string URI to extract
     *
     *  @returns Properties
     */
    Properties extractParameters(final String requestString) {
        // extract parameters...
        final Properties parameters = new Properties();
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
            parameters.setProperty(paramName, value);
        }

        // Dump parameters...
        if (logger.isLoggable(Level.FINER)) {
            for (Object key : parameters.keySet()) {
                logger.finer("Key " + key + " = " + parameters.getProperty((String) key));
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
    
    /**
     * Manages all aspects of uploaded files delivered via an ZipInputStream.
     * 
     * This class constructs a unique temporary directory, then creates one
     * temp file per ZipEntry in the stream to hold the uploaded content.  
     */
    private final class UploadedFilesInfo {
        private File tempFolder = null;
        private ArrayList<File> uploadedFiles;
        
        UploadedFilesInfo(final InputStream is, final ActionReport report) throws IOException {
            uploadedFiles = extractUploadedFiles(is, report);
        }
        
        private ArrayList<File> getFiles() {
            return uploadedFiles;
        }
        
        private void cleanup() {
            if (tempFolder != null) {
                FileUtils.whack(tempFolder);
                tempFolder = null;
            }
        }
        
        /**
         * uploads request from client and save the content in <os temp dir>/gfv3/<unique-dir>/<fileName>
         * @param req to process
         * @param report back to the client
         * @return <os temp dir>/gfv3/<fileName> files
         * @throws IOException if upload file cannot be created
         */
        private ArrayList<File> extractUploadedFiles(final InputStream is, final ActionReport report)
                throws IOException 
        {
            final String localTmpDir = System.getProperty("java.io.tmpdir");
            final File gfv3Folder = new File(localTmpDir, GFV3);
            if (!gfv3Folder.exists()) {
                gfv3Folder.mkdirs();
            }

            ArrayList<File> uploadedFiles = new ArrayList<File>();

            /*
             * Try to extract zip entries from the payload.
             */
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry entry = null;
            OutputStream os = null;

            try {
                tempFolder = createTempFolder(gfv3Folder);
                StringBuilder uploadedEntryNames = new StringBuilder();
                while ((entry = zis.getNextEntry()) != null) {
                    String entryName = entry.getName();

                    /*
                     * Note: the client should name the entries using only the name and type; no paths.
                     */
                    File uploadFile = new File(tempFolder, entryName);
                    //check for pre-existing file
                    if (uploadFile.exists()) {
                        if (!uploadFile.delete()) {
                            logger.warning(adminStrings.getLocalString(
                                    "adapter.command.overwrite",
                                    "Overwriting previously-uploaded file because the attempt to delete it failed: {0}",
                                    uploadFile.getAbsolutePath()));
                        }
                    }

                    os = new BufferedOutputStream(new FileOutputStream(uploadFile));
                    int bytesRead;
                    byte[] buffer = new byte[1024 * 64];
                    while ((bytesRead = zis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    os.close();
                    uploadedFiles.add(uploadFile);
                    uploadedEntryNames.append(entryName).append(" ");
                    logger.fine("Extracted uploaded entry " + entryName + " to " +
                            uploadFile.getAbsolutePath());
                }
                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            } 
            catch (Exception e) {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage("Error extracting uploaded file " + (entry == null ? "" : entry.getName()));
                report.setFailureCause(e);
                throw new IOException(report.getMessage());
            } 
            finally {
                if (os != null) {
                    os.close();
                }
            }
            return uploadedFiles;
        }
        
        private File createTempFolder(File parent) throws IOException {
            File result = File.createTempFile(UPLOAD_DIR_PREFIX, "", parent);
            try {
                if ( ! result.delete()) {
                    throw new IOException(
                            adminStrings.getLocalString(
                                "adapter.command.errorDeletingTempFile",
                                "Error deleting temporary file {0}",
                                result.getAbsolutePath()));
                }
                if ( ! result.mkdir()) {
                    throw new IOException(
                            adminStrings.getLocalString(
                                "adapter.command.errorCreatingDir",
                                "Error creating directory {0}",
                                result.getAbsolutePath()));
                }
                logger.fine("Created temporary upload folder " + result.getAbsolutePath());
                return result;
            } catch (Exception e) {
                IOException ioe = new IOException(adminStrings.getLocalString(
                        "adapter.command.errorCreatingUploadFolder", 
                        "Error creating temporary upload folder"));
                ioe.initCause(e);
                throw ioe;
            }
        }
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
    
    private void registerJavaConfigListener() {
        Habitat habitat = sc.getDefaultHabitat();
        ConstructorWomb<GenericJavaConfigListener> womb = new 
                ConstructorWomb<GenericJavaConfigListener>(GenericJavaConfigListener.class, habitat, null);
        ConfigListener jcl = womb.get(null);
    }
}
