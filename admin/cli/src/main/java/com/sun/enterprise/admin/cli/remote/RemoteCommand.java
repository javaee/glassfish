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

package com.sun.enterprise.admin.cli.remote;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import javax.net.ssl.SSLException;

import org.jvnet.hk2.component.*;
import com.sun.enterprise.module.*;
import com.sun.enterprise.module.single.StaticModulesRegistry;

import com.sun.appserv.management.client.prefs.LoginInfo;
import com.sun.appserv.management.client.prefs.LoginInfoStore;
import com.sun.appserv.management.client.prefs.LoginInfoStoreFactory;
import com.sun.appserv.management.client.prefs.StoreException;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.FileUtils;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.universal.GFBase64Encoder;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.admin.cli.util.*;
import com.sun.enterprise.admin.cli.ProgramOptions.PasswordLocation;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.net.NetUtils;
import org.glassfish.admin.payload.PayloadFilesManager;
import org.glassfish.admin.payload.PayloadImpl;
import org.glassfish.api.admin.Payload;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * A remote command handled by the asadmin CLI.
 */
public class RemoteCommand extends CLICommand {

    private static final LocalStringsImpl   strings =
            new LocalStringsImpl(RemoteCommand.class);

    private static final String QUERY_STRING_INTRODUCER = "?";
    private static final String QUERY_STRING_SEPARATOR = "&";
    private static final String ADMIN_URI_PATH = "/__asadmin/";
    private static final String COMMAND_NAME_REGEXP =
                                    "^[a-zA-Z_][-a-zA-Z0-9_]*$";
    private static final String READ_TIMEOUT = "AS_ADMIN_READTIMEOUT";
    private static final int readTimeout;       // read timeout for URL conns

    private String                          responseFormatType = "hk2-agent";
    private OutputStream                    userOut;
    // return output string rather than printing it
    private boolean                         returnOutput = false;
    private String                          output;
    private boolean                         returnAttributes = false;
    private Map<String, String>             attrs;
    private boolean                         doUpload = false;
    private boolean                         addedUploadOption = false;
    private boolean                         doHelp = false;
    private Payload.Outbound                outboundPayload;
    private String                          usage;

    /**
     * A class loader for the "modules" directory.
     */
    private static ClassLoader moduleClassLoader;

    /**
     * A habitat just for finding man pages.
     */
    private static Habitat manHabitat;

    /*
     * Set a default read timeout for URL connections.
     */
    static {
        String rt = System.getProperty(READ_TIMEOUT);
        if (rt == null)
            rt = System.getenv(READ_TIMEOUT);
        if (rt != null)
            readTimeout = Integer.parseInt(rt);
        else
            readTimeout = 10 * 60 * 1000;       // 10 minutes
    }

    /**
     * content-type used for each file-transfer part of a payload to or from
     * the server
     */
    private static final String FILE_PAYLOAD_MIME_TYPE =
            "application/octet-stream";

    /**
     * Interface to enable factoring out common HTTP connection management code.
     */
    interface HttpCommand {
        public void doCommand(HttpURLConnection urlConnection)
                throws CommandException, IOException;
    }

    /**
     * Construct a new remote command object.  The command and arguments
     * are supplied later using the execute method in the superclass.
     */
    public RemoteCommand() throws CommandException {
        super();
        checkName();
    }

    /**
     * Construct a new remote command object.  The command and arguments
     * are supplied later using the execute method in the superclass.
     */
    public RemoteCommand(String name, ProgramOptions po, Environment env)
            throws CommandException {
        super(name, po, env);
        checkName();
    }

    /**
     * Make sure the command name is legitimate and
     * won't allow any URL spoofing attacks.
     */
    private void checkName() throws CommandException {
        if (!name.matches(COMMAND_NAME_REGEXP))
            throw new CommandException("Illegal command name: " + name);
    }

    /**
     * Construct a new remote command object.  The command and arguments
     * are supplied later using the execute method in the superclass.
     * This variant is used by the RemoteDeploymentFacility class to
     * control and capture the output.
     */
    public RemoteCommand(String name, ProgramOptions po, Environment env,
            String responseFormatType, OutputStream userOut)
            throws CommandException {
        this(name, po, env);
        this.responseFormatType = responseFormatType;
        this.userOut = userOut;
    }

    @Override
    protected void prepare()
            throws CommandException, CommandValidationException  {
        try {
            processProgramOptions();

            initializeAuth();

            /*
             * If this is a help request, we don't need the command
             * metadata and we throw away all the other options and
             * fake everything else.
             */
            if (doHelp || programOpts.isHelp()) {
                commandOpts = new HashSet<ValidOption>();
                ValidOption opt = new ValidOption("help", "BOOLEAN",
                        ValidOption.OPTIONAL, "false");
                opt.setShortName("?");
                commandOpts.add(opt);
                return;
            }

            /*
             * Find the metadata for the command.
             */
            /*
            commandOpts = cache.get(name, ts);
            if (commandOpts == null)
                // goes to server
             */
            try {
                fetchCommandMetadata();
            } catch (AuthenticationException ex) {
                /*
                 * Failed to authenticate to server.
                 * If we can update our authentication information
                 * (e.g., by prompting the user for the username
                 * and password), try again.
                 */
                if (updateAuthentication())
                    fetchCommandMetadata();
                else
                    throw ex;
                logger.printDebugMessage("Updated authentication worked");
            }
            if (commandOpts == null) {
                String msg = metadataErrors != null ? metadataErrors.toString() : ""; 
                throw new CommandException(strings.get("InvalidCommand", name),
                        new InvalidCommandException(msg));
            }

            // everyone gets a --help option until we have a help command
            // on the server
            addOption(commandOpts, "help", '?', "BOOLEAN", false, "false");
        } catch (CommandException cex) {
            logger.printDebugMessage("RemoteCommand.prepare throws " + cex);
            throw cex;
        } catch (Exception e) {
            logger.printDebugMessage("RemoteCommand.prepare throws " + e);
            throw new CommandException(e.getMessage());
        }
    }

    /**
     * If it's a help request, don't prompt for any missing options.
     */
    @Override
    protected void validate()
            throws CommandException, CommandValidationException  {
        if (doHelp || programOpts.isHelp() || getBooleanOption("help"))
            return;
        super.validate();
    }

    /**
     * We do all our help processing in executeCommand.
     */
    @Override
    protected boolean checkHelp()
            throws CommandException, CommandValidationException {
        return false;
    }

    /**
     * Runs the command using the specified arguments.
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {

        try {
            initializeDoUpload();

            // if uploading, we need a payload
            if (doUpload)
                outboundPayload = PayloadImpl.Outbound.newInstance();

            StringBuilder uriString = new StringBuilder(ADMIN_URI_PATH).
                    append(name).append(QUERY_STRING_INTRODUCER);
            GFBase64Encoder encoder = new GFBase64Encoder();
            if (doHelp)
                addStringOption(uriString, "help", "true");
            for (Map.Entry<String, String> param : options.entrySet()) {
                String paramName = param.getKey();
                String paramValue = param.getValue();

                // if we know what the command options are, we process the
                // parameters by type here
                ValidOption opt = getValidOption(paramName);
                if (opt == null) {      // XXX - should never happen
                    String msg = strings.get("unknownOption",
                            name, paramName);
                    throw new CommandException(msg);
                }
                if (opt.getType().equals("FILE")) {
                    addFileOption(uriString, paramName, paramValue);
                } else if (opt.getType().equals("PASSWORD")) {
                    addStringOption(uriString, paramName,
                                encoder.encode(paramValue.getBytes()));
                } else
                    addStringOption(uriString, paramName, paramValue);
            }

            // add operands
            for (String operand : operands) {
                if (operandType.equals("FILE"))
                    addFileOption(uriString, "DEFAULT", operand);
                else
                    addStringOption(uriString, "DEFAULT", operand);
            }

            // remove the last character, whether it was "?" or "&"
            uriString.setLength(uriString.length() - 1);
            try {
                executeRemoteCommand(uriString.toString());
            } catch (AuthenticationException ex) {
                /*
                 * Failed to authenticate to server.
                 * If we can update our authentication information
                 * (e.g., by prompting the user for the username
                 * and password), try again.
                 */
                if (updateAuthentication())
                    executeRemoteCommand(uriString.toString());
                else
                    throw ex;
            }
        } catch (CommandException ex) {
            // if a --help request failed, try to emulate it locally
            if (programOpts.isHelp() || getBooleanOption("help")) {
                Reader r = getLocalManPage();
                if (r != null) {
                    try {
                        BufferedReader br = new BufferedReader(r);
                        PrintWriter pw = new PrintWriter(System.out);
                        char[] buf = new char[8192];
                        int cnt;
                        while ((cnt = br.read(buf)) > 0)
                            pw.write(buf, 0, cnt);
                        pw.flush();
                        return SUCCESS;
                    } catch (IOException ioex2) {
                        // ignore it and throw original exception
                    } finally {
                        try {
                            r.close();
                        } catch (IOException ioex3) {
                            // ignore it
                        }
                    }
                }
            }
            throw ex;
        } catch (IOException ioex) {
            // possibly an error caused while reading or writing a file?
            throw new CommandException("I/O Error", ioex);
        }
        return SUCCESS;
    }

    /**
     * Actually execute the remote command.
     */
    private void executeRemoteCommand(String uri) throws CommandException {
        doHttpCommand(uri, chooseRequestMethod(), new HttpCommand() {
            public void doCommand(HttpURLConnection urlConnection)
                    throws CommandException, IOException {

            if (doUpload) {
                /*
                 * If we are uploading anything then set the content-type
                 * and add the uploaded part(s) to the payload.
                 */
                urlConnection.setChunkedStreamingMode(0); // use default value
                urlConnection.setRequestProperty("Content-Type",
                        outboundPayload.getContentType());
            }
            urlConnection.connect();
            if (doUpload) {
                outboundPayload.writeTo(urlConnection.getOutputStream());
                outboundPayload = null; // no longer needed
            }
            InputStream in = urlConnection.getInputStream();

            String responseContentType = urlConnection.getContentType();

            Payload.Inbound inboundPayload =
                PayloadImpl.Inbound.newInstance(responseContentType, in);

            boolean isReportProcessed = false;
            PayloadFilesManager downloadedFilesMgr =
                    new PayloadFilesManager.Perm();
            Iterator<Payload.Part> partIt = inboundPayload.parts();
            while (partIt.hasNext()) {
                /*
                 * The report will always come first among the parts of
                 * the payload.  Be sure to process the report right away
                 * so any following data parts will be accessible.
                 */
                if (!isReportProcessed) {
                    handleResponse(options, partIt.next().getInputStream(),
                            urlConnection.getResponseCode(), userOut);
                    isReportProcessed = true;
                } else {
                    processDataPart(downloadedFilesMgr, partIt.next());
                }
            }
            }
        });
    }

    /**
     * Execute the command and return the output as a string
     * instead of writing it out.
     */
    public String executeAndReturnOutput(String... args)
            throws CommandException, CommandValidationException {
        /*
         * Tell the low level output processing to just save the output
         * string instead of writing it out.  Yes, this is pretty gross.
         */
        returnOutput = true;
        execute(args);
        returnOutput = false;
        return output;
    }

    /**
     * Execute the command and return the main attributes from the manifest
     * instead of writing out the output.
     */
    public Map<String, String> executeAndReturnAttributes(String... args)
            throws CommandException, CommandValidationException {
        /*
         * Tell the low level output processing to just save the attributes
         * instead of writing out the output.  Yes, this is pretty gross.
         */
        returnAttributes = true;
        execute(args);
        returnAttributes = false;
        return attrs;
    }

    /**
     * Set up an HTTP connection, call cmd.doCommand to do all the work,
     * and handle common exceptions.
     *
     * @param uriString     the URI to connect to
     * @param httpMethod    the HTTP method to use for the connection
     * @param cmd           the HttpCommand object
     * @throws CommandException if anything goes wrong
     */
    private void doHttpCommand(String uriString, String httpMethod,
            HttpCommand cmd) throws CommandException {
        HttpURLConnection urlConnection = null;
        try {
            HttpConnectorAddress url = new HttpConnectorAddress(
                            programOpts.getHost(), programOpts.getPort(),
                            programOpts.isSecure());
            logger.printDebugMessage("URI: " + uriString);
            logger.printDebugMessage("URL: " + url.toString());
            logger.printDebugMessage("URL: " +
                    url.toURL(uriString.toString()).toString());
            logger.printDebugMessage("Using auth info: User: " +
                programOpts.getUser() + ", Password: " +
                (ok(programOpts.getPassword()) ? "<non-null>" : "<null>"));
            String user = programOpts.getUser();
            String pwd = programOpts.getPassword();
            if (user != null || pwd != null)
                url.setAuthenticationInfo(new AuthenticationInfo(user, pwd));

            urlConnection = (HttpURLConnection)
                    url.openConnection(uriString.toString());
            urlConnection.setRequestProperty("User-Agent", responseFormatType);
            urlConnection.setRequestProperty(
                    HttpConnectorAddress.AUTHORIZATION_KEY,
                    url.getBasicAuthString());
            urlConnection.setRequestMethod(httpMethod);
            urlConnection.setReadTimeout(readTimeout);
            cmd.doCommand(urlConnection);
            logger.printDebugMessage("doHttpCommand succeeds");

        } catch (ConnectException ce) {
            logger.printDebugMessage("doHttpCommand: connect exception " + ce);
            // this really means nobody was listening on the remote server
            // note: ConnectException extends IOException and tells us more!
            String msg = strings.get("ConnectException",
                    programOpts.getHost(), programOpts.getPort() + "");
            throw new CommandException(msg, ce);
        } catch (UnknownHostException he) {
            logger.printDebugMessage("doHttpCommand: host exception " + he);
            // bad host name
            String msg = strings.get("UnknownHostException",
                                        programOpts.getHost());
            throw new CommandException(msg, he);
        } catch (SocketException se) {
            logger.printDebugMessage("doHttpCommand: socket exception " + se);
            try {
                boolean serverAppearsSecure = NetUtils.isSecurePort(
                                programOpts.getHost(), programOpts.getPort());
                if (serverAppearsSecure && !programOpts.isSecure()) {
                    String msg = strings.get("ServerMaybeSecure",
                            programOpts.getHost(), programOpts.getPort() + "");
                    logger.printMessage(msg);
                    // retry using secure connection
                    programOpts.setSecure(true);
                    try {
                        doHttpCommand(uriString, httpMethod, cmd);
                    } finally {
                        programOpts.setSecure(false);
                    }
                    return;
                }
                throw new CommandException(se);
            } catch(IOException io) {
                logger.printExceptionStackTrace(io);
                throw new CommandException(io);
            }
        } catch (SSLException se) {
            logger.printDebugMessage("doHttpCommand: SSL exception " + se);
            try {
                boolean serverAppearsSecure = NetUtils.isSecurePort(
                                programOpts.getHost(), programOpts.getPort());
                if (!serverAppearsSecure && programOpts.isSecure()) {
                    String msg = strings.get("ServerIsNotSecure",
                            programOpts.getHost(), programOpts.getPort() + "");
                    logger.printError(msg);
                }
                throw new CommandException(se);
            } catch(IOException io) {
                logger.printExceptionStackTrace(io);
                throw new CommandException(io);
            }
        } catch (IOException e) {
            logger.printDebugMessage("doHttpCommand: IO exception " + e);
            String msg = "I/O Error: " + e.getMessage();
            if (urlConnection != null) {
                try {
                    int rc = urlConnection.getResponseCode();
                    if (HttpURLConnection.HTTP_UNAUTHORIZED == rc) {
                        PasswordLocation pwloc =
                            programOpts.getPasswordLocation();
                        if (pwloc == PasswordLocation.PASSWORD_FILE) {
                            msg = strings.get("InvalidCredentialsFromFile",
                                                programOpts.getUser(),
                                                programOpts.getPasswordFile());
                        } else if (pwloc == PasswordLocation.LOGIN_FILE) {
                            try {
                                LoginInfoStore store =
                                    LoginInfoStoreFactory.getDefaultStore();
                                msg = strings.get("InvalidCredentialsFromLogin",
                                                    programOpts.getUser(),
                                                    store.getName());
                            } catch (StoreException ex) {
                                // ignore it
                            }
                        } else {
                            msg = strings.get("InvalidCredentials",
                                                programOpts.getUser());
                        }
                        throw new AuthenticationException(msg);
                    } else {
                        msg = "Status: " + rc;
                    }
                } catch (IOException ioex) {
                    // ignore it
                }
            }
            throw new CommandException(msg, e);
        } catch (Exception e) {
            logger.printDebugMessage("doHttpCommand: exception " + e);
            logger.printExceptionStackTrace(e);
            throw new CommandException(e);
        }
    }

    /**
     * Return the name of the command.
     * 
     * @return  the command name
     */
    /*
    public String getCommandName() {
        return name;
    }
    */

    /**
     * Get the usage text.
     * If we got usage information from the server, use it.
     *
     * @return usage text
     */
    public String getUsage() {
        if (usage == null)
            return super.getUsage();

        StringBuilder usageText = new StringBuilder();
        usageText.append(strings.get("Usage", strings.get("Usage.asadmin")));
        usageText.append(" ");
        usageText.append(usage);
        return usageText.toString();
    }

    /**
     * Get the man page from the server.  If the man page isn't
     * available, e.g., because the server is down, try to find
     * it locally by looking in the modules directory.
     */
    public Reader getManPage() {
        try {
            /*
             * Can't use --help option because processProgramOptions
             * will complain that it's deprecated syntax.
             */
            doHelp = true;
            String manpage = executeAndReturnOutput(name);
            doHelp = false;
            return new StringReader(manpage);
        } catch (CommandException cex) {
            // ignore
        } catch (CommandValidationException cvex) {
            // ignore
        }

        /*
         * Can't find the man page remotely, try to find it locally.
         * XXX - maybe should only do this on connection failure
         */
        Reader r = getLocalManPage();
        return r != null ? r : super.getManPage();
    }

    /**
     * Try to find a local version of the man page for this command.
     */
    private Reader getLocalManPage() {
        logger.printDetailMessage(strings.get("NoRemoteManPage"));
        String cmdClass = getCommandClass(getName());
        ClassLoader mcl = getModuleClassLoader();
        if (cmdClass != null && mcl != null) {
            return CLIManFileFinder.getCommandManFile(getName(), cmdClass,
                                                Locale.getDefault(), mcl);
        }
        return null;
    }

    private void processDataPart(final PayloadFilesManager downloadedFilesMgr,
            final Payload.Part part) throws IOException {
        /*
         * Remaining parts are typically files to be downloaded.
         */
        Properties partProps = part.getProperties();
        String dataRequestType = partProps.getProperty("data-request-type");
        if (dataRequestType.equals("file-xfer")) {
            /*
             * Treat this part as a downloaded file.  The
             * server is responsible for setting the part's file name
             * to be a valid URI, relative to the file-xfer-root or absolute,
             * that will deliver the file according to the user's
             * wishes.
             */
            downloadedFilesMgr.extractFile(part);
        }
    }

    /**
     * Adds a single option expression to the URI.  Appends a '?' in preparation
     * for the next option.
     *
     * @param uriString the URI composed so far
     * @param option the option expression to be added
     * @return the URI so far, including the newly-added option
     */
    private StringBuilder addStringOption(StringBuilder uriString, String name,
            String option) {
        try {
            String encodedOption = URLEncoder.encode(option, "UTF-8");
            uriString.append(name).
                append('=').
                append(encodedOption).
                append(QUERY_STRING_SEPARATOR);
        } catch (UnsupportedEncodingException e) {
            // XXX - should never happen
            logger.printError("Error encoding value for: " + name +
                    ", Value:" + option + ", parameter value will be ignored");
        }
        return uriString;
    }
    
    /**
     * Adds an option for a file argument, passing the name (for uploads) or the
     * path (for no-upload) operations. 
     *
     * @param uriString the URI string so far
     * @param optionName the option which takes a path or name
     * @param filename the name of the file
     * @return the URI string
     * @throws java.io.IOException
     */
    private StringBuilder addFileOption(
            StringBuilder uriString,
            String optionName,
            String filename) throws IOException {
        File f = SmartFile.sanitize(new File(filename));
        logger.printDebugMessage("FILE PARAM: " + optionName + " = " + f);
        // attach the file to the payload
        if (doUpload)
            outboundPayload.attachFile(FILE_PAYLOAD_MIME_TYPE,
                f.toURI(),
                optionName,
                null,
                f);
        if (f != null) {
            // if we are about to upload it -- give just the name
            // o/w give the full path
            String pathToPass = (doUpload ? f.getName() : f.getPath());
            addStringOption(uriString, optionName, pathToPass);
        }
        return uriString;
    }
    
    /**
     * Decide what request method to use in building the HTTP request.
     * @return the request method appropriate to the current command and options
     */
    private String chooseRequestMethod() {
        // XXX - should be part of command metadata
        if (doUpload) {
            return "POST";
        } else {
            return "GET";
        }
    }
    
    private void handleResponse(Map<String, String> params,
            InputStream in, int code, OutputStream userOut)
            throws IOException, CommandException {
        if (userOut == null) {
            handleResponse(params, in, code);
        } else {
            FileUtils.copyStream(in, userOut);
        }
    }

    private void handleResponse(Map<String, String> params,
            InputStream in, int code) throws IOException, CommandException {
        RemoteResponseManager rrm = null;

        try {
            rrm = new RemoteResponseManager(in, code);
            rrm.process();
        } catch (RemoteSuccessException rse) {
            if (returnOutput)
                output = rse.getMessage();
            else if (returnAttributes)
                attrs = rrm.getMainAtts();
            else
                logger.printMessage(rse.getMessage());
            return;
        } catch (RemoteException rfe) {
            // XXX - gross
            if (rfe.getRemoteCause().indexOf("CommandNotFoundException")>0) {
                    // CommandNotFoundException from the server, then display
                    // the closest matching commands
                throw new CommandException(rfe.getMessage(),
                        new InvalidCommandException());
            }
            throw new CommandException(
                        "remote failure: " + rfe.getMessage(), rfe);
        }
    }

    /**
     * Get the metadata for the command from the server.
     *
     * @return the options for the command
     * @throws CommandException if the server can't be contacted
     */
    protected void fetchCommandMetadata() throws CommandException {

        // XXX - there should be a "help" command, that returns XML output
        //StringBuilder uriString = new StringBuilder(ADMIN_URI_PATH).
                //append("help").append(QUERY_STRING_INTRODUCER);
        //addStringOption(uriString, "DEFAULT", name);
        StringBuilder uriString = new StringBuilder(ADMIN_URI_PATH).
                append(name).append(QUERY_STRING_INTRODUCER);
        addStringOption(uriString, "Xhelp", "true");

        // remove the last character, whether it was "?" or "&"
        uriString.setLength(uriString.length() - 1);

        doHttpCommand(uriString.toString(), "GET", new HttpCommand() {
            public void doCommand(HttpURLConnection urlConnection)
                    throws CommandException, IOException {

                //urlConnection.setRequestProperty("Accept: ", "text/xml");
                urlConnection.setRequestProperty("User-Agent", "metadata");
                urlConnection.connect();
                InputStream in = urlConnection.getInputStream();

                String responseContentType = urlConnection.getContentType();
                Payload.Inbound inboundPayload =
                    PayloadImpl.Inbound.newInstance(responseContentType, in);

                boolean isReportProcessed = false;
                Iterator<Payload.Part> partIt = inboundPayload.parts();
                while (partIt.hasNext()) {
                    /*
                     * There should be only one part, which should be the
                     * metadata, but skip any other parts just in case.
                     */
                    if (!isReportProcessed) {
                        metadataErrors = new StringBuilder();
                        commandOpts =
                                parseMetadata(partIt.next().getInputStream(), metadataErrors);
                        logger.printDebugMessage(
                            "fetchCommandMetadata: got command opts: " +
                            commandOpts);
                        isReportProcessed = true;
                    } else {
                        partIt.next();  // just throw it away
                    }
                }
            }
        });
    }

    /**
     * Parse the XML metadata for the command on the input stream.
     *
     * @param in the input stream
     * @return the set of ValidOptions
     */
    private Set<ValidOption> parseMetadata(InputStream in, StringBuilder errors) {
        if (logger.isLoggable(Level.FINER)) { // XXX - assume "debug" == "FINER"
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                FileUtils.copyStream(in, baos);
            } catch (IOException ex) { }
            in = new ByteArrayInputStream(baos.toByteArray());
            String response = baos.toString();
            logger.printDebugMessage("------- RAW METADATA RESPONSE ---------");
            logger.printDebugMessage(response);
            logger.printDebugMessage("------- RAW METADATA RESPONSE ---------");
        }

        Set<ValidOption> valid = new LinkedHashSet<ValidOption>();
        boolean sawFile = false;
        try {
            DocumentBuilder d =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = d.parse(in);
            NodeList cmd = doc.getElementsByTagName("command");
            Node cmdnode = cmd.item(0);
            if (cmdnode == null) {
                Node report = doc.getElementsByTagName("action-report").item(0);
                String cause = getAttr(report.getAttributes(), "failure-cause");
                if (cause != null)
                    errors.append(cause);
                return null;    // no command info, must be invalid command or something wrong with command implementation
            }
            NamedNodeMap cmdattrs = cmdnode.getAttributes();
            usage = getAttr(cmdattrs, "usage");
            String dashOk = getAttr(cmdattrs, "unknown-options-are-operands");
            if (dashOk != null)
                unknownOptionsAreOperands = Boolean.parseBoolean(dashOk);
            NodeList opts = doc.getElementsByTagName("option");
            for (int i = 0; i < opts.getLength(); i++) {
                Node n = opts.item(i);
                NamedNodeMap attrs = n.getAttributes();
                ValidOption opt = new ValidOption(
                        getAttr(attrs, "name"),
                        getAttr(attrs, "type"),
                        Boolean.parseBoolean(getAttr(attrs, "optional")) ?
                            ValidOption.OPTIONAL : ValidOption.REQUIRED,
                        getAttr(attrs, "default"));
                String sn = getAttr(attrs, "short");
                if (ok(sn))
                    opt.setShortName(sn);
                if (opt.getType().equals("PASSWORD") &&
                        getAttr(attrs, "description") != null)
                    // XXX - hack alert!  description is stored in default value
                    // but only for passwords for now
                    opt.setDefaultValue(getAttr(attrs, "description"));
                valid.add(opt);
                if (opt.getType().equals("FILE"))
                    sawFile = true;
            }
            // should be only one operand item
            opts = doc.getElementsByTagName("operand");
            for (int i = 0; i < opts.getLength(); i++) {
                Node n = opts.item(i);
                NamedNodeMap attrs = n.getAttributes();
                operandName = getAttr(attrs, "name");
                operandType = getAttr(attrs, "type");
                operandMin = Integer.parseInt(getAttr(attrs, "min"));
                String max = getAttr(attrs, "max");
                if (max.equals("unlimited"))
                    operandMax = Integer.MAX_VALUE;
                else
                    operandMax = Integer.parseInt(max);
                if (operandType.equals("FILE"))
                    sawFile = true;
            }

            /*
             * If one of the options or operands is a FILE,
             * make sure there's also a --upload option available.
             * XXX - should only add it if it's not present
             * XXX - should just define upload parameter on remote command
             */
            if (sawFile) {
                valid.add(new ValidOption("upload", "BOOLEAN",
                        ValidOption.OPTIONAL, null));
                addedUploadOption = true;
            }
        } catch (ParserConfigurationException pex) {
            // ignore for now
            return null;
        } catch (SAXException sex) {
            // ignore for now
            return null;
        } catch (IOException ioex) {
            // ignore for now
            return null;
        }
        return valid;
    }

    /**
     * Return the value of a named attribute, or null if not set.
     */
    private static String getAttr(NamedNodeMap attrs, String name) {
        Node n = attrs.getNamedItem(name);
        if (n != null)
            return n.getNodeValue();
        else
            return null;
    }

    /**
     * Search all the parameters that were actually specified to see
     * if any of them are FILE type parameters.  If so, check for the
     * "--upload" option.
     */
    private void initializeDoUpload() throws CommandException {
        boolean sawFile = false;
        boolean sawDirectory = false;
        for (Map.Entry<String, String> param : options.entrySet()) {
            String paramName = param.getKey();
            ValidOption opt = getValidOption(paramName);
            if (opt != null && opt.getType().equals("FILE")) {
                sawFile = true;
                // if any FILE parameter is a directory, turn off doUpload
                String filename = param.getValue();
                File file = new File(filename);
                if (file.isDirectory())
                    sawDirectory = true;
            }
        }

        // now check the operands for files
        if (operandType != null && operandType.equals("FILE")) {
            for (String filename : operands) {
                sawFile = true;
                // if any FILE parameter is a directory, turn off doUpload
                File file = new File(filename);
                if (file.isDirectory())
                    sawDirectory = true;
            }
        }

        if (sawFile) {
            // found a FILE param, is doUpload set?
            String upString = getOption("upload");
            if (ok(upString))
                doUpload = Boolean.parseBoolean(upString);
            else
                doUpload = !isLocal(programOpts.getHost());
            if (sawDirectory && doUpload) {
                // oops, can't upload directories
                logger.printDebugMessage("--upload=" + upString +
                                            ", doUpload=" + doUpload);
                throw new CommandException(strings.get("CantUploadDirectory"));
            }
        }

        if (addedUploadOption)
            options.remove("upload");    // XXX - remove it

        logger.printDebugMessage("doUpload set to " + doUpload);
    }

    /**
     * Does the given hostname represent the local host?
     */
    private static boolean isLocal(String hostname) {
        if (hostname.equalsIgnoreCase("localhost"))     // the common case
            return true;
        try {
            // let NetUtils do the hard work
            InetAddress ia = InetAddress.getByName(hostname);
            return NetUtils.isLocal(ia.getHostAddress());
        } catch (UnknownHostException ex) {
            /*
             * Sometimes people misconfigure their name service and they
             * can't even look up the name of their own machine.
             * Too bad.  We just give up and say it's not local.
             */
            return false;
        }
    }

    /**
     * Get the ValidOption descriptor for the named option.
     *
     * @param name  the option name
     * @return      the ValidOption descriptor
     */
    private ValidOption getValidOption(String name) {
        for (ValidOption opt : commandOpts)
            if (opt.getName().equals(name))
                return opt;
        return null;
    }

    /**
     * If we're interactive, prompt for a new username and password.
     * Return true if we're successful in collecting new information
     * (and thus the caller should try the request again).
     */
    protected boolean updateAuthentication() {
        Console cons;
        if (programOpts.isInteractive() && (cons = System.console()) != null) {
            // if appropriate, tell the user why authentication failed
            PasswordLocation pwloc = programOpts.getPasswordLocation();
            if (pwloc == PasswordLocation.PASSWORD_FILE) {
                logger.printDetailMessage(strings.get("BadPasswordFromFile",
                                                programOpts.getPasswordFile()));
            } else if (pwloc == PasswordLocation.LOGIN_FILE) {
                try {
                    LoginInfoStore store =
                        LoginInfoStoreFactory.getDefaultStore();
                    logger.printDetailMessage(
                        strings.get("BadPasswordFromLogin", store.getName()));
                } catch (StoreException ex) {
                    // ignore it
                }
            }

            String user = null;
            // only prompt for a user name if the user name is set to
            // the default.  otherwise, assume the user specified the
            // correct username to begin with and all we need is the password.
            if (programOpts.getUser() == null) {
                cons.printf("%s ", strings.get("AdminUserPrompt"));
                user = cons.readLine();
                if (user == null)
                    return false;
            }
            String password;
            String puser = ok(user) ? user : programOpts.getUser();
            if (ok(puser))
                password = readPassword(
                                strings.get("AdminUserPasswordPrompt", puser));
            else
                password = readPassword(strings.get("AdminPasswordPrompt"));
            if (password == null)
                return false;
            if (ok(user))      // if none entered, don't change
                programOpts.setUser(user);
            programOpts.setPassword(password, PasswordLocation.USER);
            return true;
        }
        return false;
    }

    private void initializeAuth() throws CommandException {
        LoginInfo li = null;
        
        try {
            LoginInfoStore store = LoginInfoStoreFactory.getDefaultStore();
            li = store.read(programOpts.getHost(), programOpts.getPort());
            if (li == null)
                return;
        } catch (StoreException se) {
            logger.printDebugMessage(
                    "Login info could not be read from ~/.asadminpass file");
            return;
        }

        /*
         * If we don't have a user name, initialize it from .asadminpass.
         * In that case, also initialize the password unless it was
         * already specified (overriding what's in .asadminpass).
         *
         * If we already have a user name, and it's the same as what's
         * in .asadminpass, and we don't have a password, use the password
         * from .asadminpass.
         */
        if (programOpts.getUser() == null) {
            // not on command line and in .asadminpass
            logger.printDebugMessage("Getting user name from ~/.asadminpass: " +
                                        li.getUser());
            programOpts.setUser(li.getUser());
            if (programOpts.getPassword() == null) {
                // not in passwordfile and in .asadminpass
                logger.printDebugMessage(
                    "Getting password from ~/.asadminpass");
                programOpts.setPassword(li.getPassword(),
                    ProgramOptions.PasswordLocation.LOGIN_FILE);
            }                
        } else if (programOpts.getUser().equals(li.getUser())) {
            if (programOpts.getPassword() == null) {
                // not in passwordfile and in .asadminpass
                logger.printDebugMessage(
                    "Getting password from ~/.asadminpass");
                programOpts.setPassword(li.getPassword(),
                    ProgramOptions.PasswordLocation.LOGIN_FILE);
            }                
        }
    }

    /**
     * Given a command name, return the name of the class that implements
     * that command in the server.
     */
    private static String getCommandClass(String cmdName) {
        Habitat h = getManHabitat();
        String cname = "org.glassfish.api.admin.AdminCommand";
        for (Inhabitant<?> command : h.getInhabitantsByContract(cname)) {
            for (String name : Inhabitants.getNamesFor(command, cname)) {
                if (name.equals(cmdName))
                    return command.typeName();
            }
        }
        return null;
    }

    /**
     * Return a Habitat used just for reading man pages from the
     * modules in the modules directory.
     */
    private static Habitat getManHabitat() {
        if (manHabitat != null)
            return manHabitat;
        ModulesRegistry registry =
                new StaticModulesRegistry(getModuleClassLoader());
        manHabitat = registry.createHabitat("default");
        return manHabitat;
    }

    /**
     * Return a ClassLoader that loads classes from all the modules
     * (jar files) in the <INSTALL_ROOT>/modules directory.
     */
    private static ClassLoader getModuleClassLoader() {
        if (moduleClassLoader != null)
            return moduleClassLoader;
        try {
            File installDir = new File(System.getProperty(
                                SystemPropertyConstants.INSTALL_ROOT_PROPERTY));
            File modulesDir = new File(installDir, "modules");
            moduleClassLoader = new DirectoryClassLoader(modulesDir,
                                            CLICommand.class.getClassLoader());
            return moduleClassLoader;
        } catch (IOException ioex) {
            return null;
        }
    }
}
