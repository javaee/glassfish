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

import com.sun.appserv.management.client.prefs.LoginInfo;
import com.sun.appserv.management.client.prefs.LoginInfoStore;
import com.sun.appserv.management.client.prefs.LoginInfoStoreFactory;
import com.sun.appserv.management.client.prefs.StoreException;
import com.sun.enterprise.admin.cli.CLIConstants;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.FileUtils;
import com.sun.enterprise.universal.io.SmartFile;
import java.io.*;
import java.net.*;
import java.util.*;
import com.sun.enterprise.admin.cli.util.*;
import com.sun.enterprise.cli.framework.*;
import java.util.Iterator;
import java.util.logging.Level;
import com.sun.enterprise.universal.GFBase64Encoder;
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
public class NCLIRemoteCommand {    

    public static final String  RELIABLE_COMMAND = "version";

    private static final LocalStringsImpl   strings =
	    new LocalStringsImpl(CLIRemoteCommand.class);
    private static final CLILogger          logger = CLILogger.getInstance();

    private static final Set<ValidOption>   known;
    private static final Set<String>        unsupported;

    private static final String QUERY_STRING_INTRODUCER = "?";
    private static final String QUERY_STRING_SEPARATOR = "&";
    private static final String ADMIN_URI_PATH = "/__asadmin/";
    private static final String COMMAND_NAME_REGEXP =
				    "^[a-zA-Z][-a-zA-Z0-9_]*$";
    private static final String UNSUPPORTED_CMD_FILE_NAME =
				    "unsupported-legacy-command-names";

    private Map<String, String>             mainAtts;
    private Map<String, String>             params;
    private List<String>                    operands;
    private String                          commandName;
    private Set<ValidOption>		    commandOpts;
    private String                          operandType;
    private int				    operandMin;
    private int				    operandMax;
    private String                          responseFormatType = "hk2-agent";
    private OutputStream                    userOut;
    private boolean                         doUpload = false;
    private boolean                         addedUploadOption = false;
    private Map<String, String>             encodedPasswords;
    private Payload.Outbound		    outboundPayload;

    private String                          hostName;
    private int                             hostPort;
    private boolean                         secure;
    private String                          user;
    private String                          password;
    private boolean                         verbose = false;
    private boolean                         terse = false;
    private boolean                         echo = false;
    private boolean                         interactive = false;
    private boolean                         help = false;

    /**
     * content-type used for each file-transfer part of a payload to or from
     * the server
     */
    private static final String FILE_PAYLOAD_MIME_TYPE =
	    "application/octet-stream";

    /*
     * Define the meta-options known by the asadmin command.
     */
    static {
	Set<ValidOption> opts = new HashSet<ValidOption>();
	addMetaOption(opts, "host", 'H', "STRING", false,
		CLIConstants.DEFAULT_HOSTNAME);
	addMetaOption(opts, "port", 'p', "STRING", false,
		"" + CLIConstants.DEFAULT_ADMIN_PORT);
	addMetaOption(opts, "user", 'u', "STRING", false, "anonymous");
	addMetaOption(opts, "password", 'w', "STRING", false, null);
	addMetaOption(opts, "passwordfile", 'W', "FILE", false, null);
	addMetaOption(opts, "secure", 's', "BOOLEAN", false, "false");
	addMetaOption(opts, "terse", 't', "BOOLEAN", false, "false");
	addMetaOption(opts, "echo", 'e', "BOOLEAN", false, "false");
	addMetaOption(opts, "interactive", 'I', "BOOLEAN", false, "false");
	addMetaOption(opts, "help", '?', "BOOLEAN", false, "false");
	known = Collections.unmodifiableSet(opts);
	Set<String> unsup = new HashSet<String>();
        file2Set(UNSUPPORTED_CMD_FILE_NAME, unsup);
	unsupported = Collections.unmodifiableSet(unsup);
    }

    /**
     * Interface to enable factoring out common HTTP connection management code.
     */
    interface HttpCommand {
	public void doCommand(HttpURLConnection urlConnection)
		throws CommandException, IOException;
    }

    /**
     * Helper method to define a meta-option.
     *
     * @param name  long option name
     * @param sname short option name
     * @param type  option type (STRING, BOOLEAN, etc.)
     * @param req   is option required?
     * @param def   default value for option
     */
    private static void addMetaOption(Set<ValidOption> opts, String name,
	    char sname, String type, boolean req, String def) {
	ValidOption opt = new ValidOption(name, type,
		req ? ValidOption.REQUIRED : ValidOption.OPTIONAL, def);
	String abbr = Character.toString(sname);
	opt.setShortName(abbr);
	opts.add(opt);
    }

    /**
     * Construct a new remote command object.  The command and arguments
     * are supplied later using the parse method.
     */
    public NCLIRemoteCommand() {
    }

    /**
     * Construct a new remote command object to execute the specified
     * command and arguments.
     *
     * @param args  the command and arguments, e.g., from the command line
     * @throws CommandException	if anything goes wrong
     */
    public NCLIRemoteCommand(String... args) throws CommandException {
        initialize(args);
	logger.printDebugMessage("Using new CLIRemoteCommand");
    }

    /** 
     * designed for use by the RemoteDeploymentFacility class
     * @param args
     * @param responseFormatType
     * @param userOut
     * @throws com.sun.enterprise.cli.framework.CommandException
     */
    public NCLIRemoteCommand(String[] args, String responseFormatType,
	    OutputStream userOut) throws CommandException {
        initialize(args);
        this.responseFormatType = responseFormatType;
        this.userOut = userOut;
    }

    /**
     * Parse the command and arguments, saving the results.
     */
    public void parse(String... args) throws CommandException {
	initialize(args);
    }


    /**
     * Runs the command using the specified arguments and sending the result
     * to the caller-provided {@link OutputStream} in the requested format for
     * processing.
     */
    public void runCommand() throws CommandException {

	if (commandName == null)
	    throw new CommandException("No Command");

	try {
	    initializeDoUpload();

	    // if uploading, we need a payload
	    if (doUpload)
                outboundPayload = PayloadImpl.Outbound.newInstance();

            StringBuilder uriString = new StringBuilder(ADMIN_URI_PATH).
		    append(commandName).append(QUERY_STRING_INTRODUCER);
            for (Map.Entry<String, String> param : params.entrySet()) {
                String paramName = param.getKey();
		String paramValue = param.getValue();

		// if we know what the command options are, we process the
		// parameters by type here
		ValidOption opt = getOption(paramName);
		if (opt == null) {	// XXX - should never happen
		    String msg = strings.get("unknownOption",
			    commandName, paramName);
		    throw new CommandException(msg);
		}
		if (opt.getType().equals("FILE"))
		    addFileOption(uriString, paramName, paramValue);

		addOption(uriString, paramName, paramValue);
            }

            // add passwordfile parameters if any
            if (encodedPasswords != null) {
                for (Map.Entry<String,String> entry : encodedPasswords.entrySet()) {
                    addOption(uriString, entry.getKey(), entry.getValue());
                }
            }

	    // XXX - remove this special case
            if (commandName.equalsIgnoreCase("change-admin-password")) {
                addOption(uriString, "username", user);
            }

            // add operands
            for (String operand : operands) {
		if (operandType.equals("FILE"))
		    addFileOption(uriString, "DEFAULT", operand);
		else
		    addOption(uriString, "DEFAULT", operand);
            }

	    // remove the last character, whether it was "?" or "&"
	    uriString.setLength(uriString.length() - 1);
	    doHttpCommand(uriString.toString(), chooseRequestMethod(),
		    new HttpCommand() {
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

                final String responseContentType = urlConnection.getContentType();

                Payload.Inbound inboundPayload = PayloadImpl.Inbound.newInstance(
                        responseContentType, in);
                
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
                        handleResponse(params, partIt.next().getInputStream(),
                                urlConnection.getResponseCode(), userOut);
                        isReportProcessed = true;
                    } else {
                        processDataPart(downloadedFilesMgr, partIt.next());
                    }
                }
		}
	    });
	} catch (IOException ioex) {
	    // possibly an error caused while reading or writing a file?
	    throw new CommandException("I/O Error", ioex);
	}
    }

    /**
     * Set up an HTTP connection, call cmd.doCommand to do all the work,
     * and handle common exceptions.
     *
     * @param uriString	    the URI to connect to
     * @param httpMethod    the HTTP method to use for the connection
     * @param cmd	    the HttpCommand object
     * @throws CommandException	if anything goes wrong
     */
    private void doHttpCommand(String uriString, String httpMethod,
	    HttpCommand cmd) throws CommandException {
	HttpURLConnection urlConnection = null;
	try {
	    HttpConnectorAddress url =
		    new HttpConnectorAddress(hostName, hostPort, secure);
	    logger.printDebugMessage("URI: " + uriString.toString());
	    logger.printDebugMessage("URL: " + url.toString());
	    logger.printDebugMessage("URL: " +
		    url.toURL(uriString.toString()).toString());
	    url.setAuthenticationInfo(new AuthenticationInfo(user, password));

	    urlConnection = (HttpURLConnection)
		    url.openConnection(uriString.toString());
	    urlConnection.setRequestProperty("User-Agent", responseFormatType);
	    urlConnection.setRequestProperty(
		    HttpConnectorAddress.AUTHORIZATION_KEY,
		    url.getBasicAuthString());
	    urlConnection.setRequestMethod(httpMethod);
	    cmd.doCommand(urlConnection);

	} catch (ConnectException ce) {
	    // this really means nobody was listening on the remote server
	    // note: ConnectException extends IOException and tells us more!
	    String msg = strings.get("ConnectException",
		    hostName, hostPort + "");
	    throw new CommandException(msg, ce);
        } catch(SocketException se) {
            try {
                boolean serverAppearsSecure =
                        NetUtils.isSecurePort(hostName, hostPort);
                if (serverAppearsSecure != secure) {
                    String msg = strings.get("ServerMaybeSecure",
			    hostName, hostPort+"");
                    logger.printError(msg);
                    throw new CommandException(se);
                }
            } catch(IOException io) {
                logger.printExceptionStackTrace(io);
                throw new CommandException(io);
            }
	} catch (IOException e) {
	    String msg = "Unknown I/O Error";
	    if (urlConnection != null) {
		try {
		    int rc = urlConnection.getResponseCode();
		    if (HttpURLConnection.HTTP_UNAUTHORIZED == rc) {
			msg = strings.get("InvalidCredentials", user);
		    } else {
			msg = "Status: " + rc;
		    }
		} catch (IOException ioex) {
		    // ignore it
		}
	    }
	    throw new CommandException(msg, e);
	} catch (Exception e) {
            logger.printExceptionStackTrace(e);
            throw new CommandException(e);
        }
    }

    /**
     * Return the name of the command.
     * 
     * @return	the command name
     */
    public String getCommandName() {
	return commandName;
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
    private static StringBuilder addOption(StringBuilder uriString, String name,
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
            addOption(uriString, optionName, pathToPass);
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
        }
        else {
            FileUtils.copyStream(in, userOut);
        }
    }

    private void handleResponse(Map<String, String> params,
            InputStream in, int code) throws IOException, CommandException {
        RemoteResponseManager rrm = null;

        try {
            rrm = new RemoteResponseManager(in, code);
            rrm.process();
        }
        catch (RemoteSuccessException rse) {
            if (rrm != null)
                mainAtts = rrm.getMainAtts();
            Log.info(rse.getMessage());
            return;
        }
        catch (RemoteException rfe) {
            if (rfe.getRemoteCause().indexOf("CommandNotFoundException")>0) {
                    // CommandNotFoundException from the server, then display
		    // the closest matching commands
                throw new CommandException(rfe.getMessage(),
			new InvalidCommandException());
            }
            throw new CommandException("remote failure: " + rfe.getMessage(), rfe);
        }
    }

    private static boolean ok(String s) {
        return s != null && s.length() > 0 && !s.equals("null");
    }

    @Override
    public String toString() {
        // always include terse and echo
        StringBuilder sb = new StringBuilder();
        sb.append("--echo=").append(Boolean.toString(echo)).append(' ');
        sb.append("--terse=").append(Boolean.toString(terse)).append(' ');
	// XXX - include all the meta-options?
        sb.append(commandName).append(' ');
        Set<String> paramKeys = params.keySet();

        for (String key : paramKeys) {
            String value = params.get(key);
            sb.append("--").append(key);
            if (ok(value)) {
                sb.append('=').append(value);
            }
            sb.append(' ');
        }

        for (Object o : operands) {
            sb.append(o).append(' ');
        }

        return sb.toString();
    }

    public Map<String, String> getMainAtts() {
        return mainAtts;
    }

    /**
     * See if DAS is alive.
     * @param invoker instance of CommandInvoker
     * @return true if DAS can be reached and can handle commands,
     *	    otherwise false.
     */
    // XXX - only used below?
    public static boolean pingDAS(CommandInvoker invoker) {
        try {
            invoker.invoke();
            return true;
        }
        catch (Exception ex) {
            ExceptionAnalyzer ea = new ExceptionAnalyzer(ex);
            if (ea.getFirstInstanceOf(java.net.ConnectException.class) != null) {
                CLILogger.getInstance().printDebugMessage(
			"Got java.net.ConnectException");
                return false; // this definitely means server is not up
            } else if (ea.getFirstInstanceOf(java.io.IOException.class) != null) {
                CLILogger.getInstance().printDebugMessage(
			"It appears that server has started, but for" +
                        " some reason the exception is thrown: " +
			ex.getMessage());
                return true;
            } else {
                return false; //unknown error, shouldn't really happen
            }
        }
    }

    /**
     * See if DAS can be contacted with the present authentication info.
     * @return true if DAS can be reached with this authentication info
     */
    // XXX - not used?
    public static boolean pingDASWithAuth(CommandInvoker invoker) {
        try {
            invoker.invoke();
            return true;
        }
        catch (Exception ex) {
            ExceptionAnalyzer ea = new ExceptionAnalyzer(ex);
            if (ea.getFirstInstanceOf(java.net.ConnectException.class) != null) {
                CLILogger.getInstance().printDebugMessage(
			"Got java.net.ConnectException");
                return false; // this definitely means server is not up
            } else if (ea.getFirstInstanceOf(java.io.IOException.class) != null) {
                CLILogger.getInstance().printDebugMessage(
			"Auth info is incorrect" + ex.getMessage());
                return false;
            } else {
                return false; //unknown error, shouldn't really happen
            }
        }
    }

    /**
     * Do not print out the results of the version command from the server 
     * @param invoker instance of CommandInvoker that invokes the command
     * @return true if DAS can be reached and can handle commands,
     *	    otherwise false.
     */
    // XXX - only used by start-domain, stop-domain
    public static boolean pingDASQuietly(CommandInvoker invoker) {
        try {
            CLILogger.getInstance().pushAndLockLevel(Level.WARNING);
            return pingDAS(invoker);
        }
        finally {
            CLILogger.getInstance().popAndUnlockLevel();
        }
    }

    /**
     * Get the metadata for the command from the server.
     *
     * @param cmdName
     * @return the options for the command
     * @throws CommandException	if the server can't be contacted
     */
    private void fetchCommandMetadata(String cmdName) throws CommandException {

	// XXX - there should be a "help" command, that returns XML output
	//StringBuilder uriString = new StringBuilder(ADMIN_URI_PATH).
		//append("help").append(QUERY_STRING_INTRODUCER);
	//addOption(uriString, "DEFAULT", cmdName);
	StringBuilder uriString = new StringBuilder(ADMIN_URI_PATH).
		append(cmdName).append(QUERY_STRING_INTRODUCER);
	addOption(uriString, "Xhelp", "true");

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
			commandOpts =
				parseMetadata(partIt.next().getInputStream());
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
    private Set<ValidOption> parseMetadata(InputStream in) {
	Set<ValidOption> valid = new HashSet<ValidOption>();
	boolean sawFile = false;
	try {
	    DocumentBuilder d =
		    DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    Document doc = d.parse(in);
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
		opt.setShortName(getAttr(attrs, "short"));
		valid.add(opt);
		if (opt.getType().equals("FILE"))
		    sawFile = true;
	    }
	    // should be only one operand item
	    opts = doc.getElementsByTagName("operand");
	    for (int i = 0; i < opts.getLength(); i++) {
		Node n = opts.item(i);
		NamedNodeMap attrs = n.getAttributes();
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
			ValidOption.OPTIONAL, "true"));
		addedUploadOption = true;
	    }
	} catch (ParserConfigurationException pex) {
	    // ignore all for now
	    return null;
	} catch (SAXException sex) {
	    // ignore all for now
	    return null;
	} catch (IOException ex) {
	    // ignore all for now
	    return null;
	}
	return valid;
    }

    private static String getAttr(NamedNodeMap attrs, String name) {
	Node n = attrs.getNamedItem(name);
	if (n != null)
	    return n.getNodeValue();
	else
	    return null;
    }

    private void initialize(final String[] argv) throws CommandException {
        try {
	    if (argv.length == 0)
		throw new CommandException("No Command");

	    Parser rcp;
	    int cmdArgsStart;
	    // if the first argument is an option, we're using the new form
	    if (argv[0].startsWith("-")) {
		/*
		 * Parse all the asadmin options, stopping at the first
		 * non-option, which is the command name.
		 */
		rcp = new Parser(argv, 0, known, false);
		params = rcp.getOptions();
		operands = rcp.getOperands();
		if (operands.size() == 0)
		    throw new CommandException("No Command");
		commandName = operands.get(0);
		cmdArgsStart = 1;
	    } else {        // first arg is not an option, using old form
		/*
		 * asadmin options and command options are intermixed.
		 * Parse the entire command line for asadmin options,
		 * removing them from the command line, and ignoring
		 * unknown options.  The remaining command line starts
		 * with the command name.
		 */
		commandName = argv[0];
		// XXX - meta-options parsing depends on whether command is old?
		rcp = new Parser(argv, 1, known, true);
		params = rcp.getOptions();
		operands = rcp.getOperands();
		cmdArgsStart = 0;
		// warn about deprecated use of meta-options
		if (params.size() > 0) {
		    // at least one program option specified after command name
		    Set<String> names = params.keySet();
		    String[] na = names.toArray(new String[names.size()]);
		    System.out.println("Deprecated syntax: " + commandName +
			    ", Options: " + Arrays.toString(na));
		}
	    }

	    /*
	     * Make sure the command name is legitimate and
	     * won't allow any URL spoofing attacks.
	     */
	    if (!commandName.matches(COMMAND_NAME_REGEXP))
		throw new CommandException("Illegal command name: " + commandName);

	    checkUnsupportedLegacyCommand(commandName);

            initializeStandardParams();
            initializeLogger();
            logger.printDebugMessage("CLIRemoteCommandParser: " + rcp);
            initializeAuth();

	    /*
	     * If this is a help request, we don't need the command
	     * metadata and we throw away all the other options and
	     * fake everything else.
	     */
	    if (help) {
		commandOpts = new HashSet<ValidOption>();
		addMetaOption(commandOpts, "help", '?', "BOOLEAN",
			false, "false");
		params = new HashMap<String, String>();
		params.put("help", "true");
		operands = Collections.emptyList();
		return;
	    }

	    /*
	     * Now parse the resulting command using the command options.
	     */

	    /*
	     * Find the metadata for the command.
	     */
	    /*
	    commandOpts = cache.get(commandName, ts);
	    if (commandOpts == null)
		// goes to server
	     */
	    fetchCommandMetadata(commandName);
	    if (commandOpts == null)
		throw new CommandException("Unknown command: " + commandName);
	    String[] cmdArgs = operands.toArray(new String[operands.size()]);
	    rcp = new Parser(cmdArgs, cmdArgsStart, commandOpts, false);
            params = rcp.getOptions();
	    operands = rcp.getOperands();
	    logger.printDebugMessage("params: " + params);
	    logger.printDebugMessage("operands: " + operands);

	    /*
	     * Check for missing params and operands.
	     */
	    if (operands.size() < operandMin)
		throw new CommandException(
			strings.get("notEnoughOperands", commandName));
	    if (operands.size() > operandMax)
		throw new CommandException(
			strings.get("tooManyOperands", commandName));
	    boolean missingOption = false;
	    for (ValidOption opt : commandOpts) {
		if (opt.isValueRequired() != ValidOption.REQUIRED)
		    continue;
		if (opt.getType().equals("PASSWORD"))
		    continue;	// passwords are handled later
		if (params.get(opt.getName()) == null) {
		    missingOption = true;
		    System.out.println(
			    strings.get("missingOption", "--" + opt.getName()));
		}
	    }
	    if (missingOption)
		throw new CommandException(
			strings.get("missingOptions", commandName));
            initializeCommandPassword();
	} catch (CommandException cex) {
	    throw cex;
        } catch(Exception e) {
            throw new CommandException(e.getMessage());
        }
    }

    /**
     * Initialize the asadmin program options based on the parsed parameters.
     *
     * @throws CommandException
     */
    private void initializeStandardParams() throws CommandException {
        if (params.containsKey("verbose")) {
            String value = params.get("verbose");
            if (ok(value))
                verbose = Boolean.parseBoolean(params.get("verbose"));
            else
                verbose = true;
        }
        if (params.containsKey("echo")) {
            String value = params.get("echo");
            if (ok(value))
                echo = Boolean.parseBoolean(params.get("echo"));
            else
                echo = true;
        }
        if (params.containsKey("terse")) {
            String value = params.get("terse");
            if (ok(value))
                terse = Boolean.parseBoolean(params.get("terse"));
            else
                terse = true;
        }
        if (params.containsKey("interactive")) {
	    String value = params.get("interactive");
	    if (ok(value))
		interactive = Boolean.parseBoolean(params.get("interactive"));
	    else
		interactive = true;
	} else
		interactive = true;	// XXX - set based on Console

        if (params.containsKey("help"))
	    help = true;    // don't care about the value

        hostName = params.get("host");
        
        if (hostName == null || hostName.length() == 0)
            hostName = CLIConstants.DEFAULT_HOSTNAME;
        logger.printDebugMessage("host = " + hostName);

        String port = params.get("port");
        if (ok(port)) {
            String badPortMsg = strings.get("badport", port);
            try {
                hostPort = Integer.parseInt(port);

                if (hostPort < 1 || hostPort > 65535)
                    throw new CommandException(badPortMsg);
            } catch (NumberFormatException e) {
                throw new CommandException(badPortMsg);
            }
        }

        if (!ok(port))
            hostPort = CLIConstants.DEFAULT_ADMIN_PORT; // the default port

        String s = params.get("secure");
        if (ok(s))
            secure = Boolean.parseBoolean(s);
        else
            secure = false;
    }

    private void initializeLogger() {
        if (terse)
            logger.setOutputLevel(java.util.logging.Level.INFO);
        else
            logger.setOutputLevel(java.util.logging.Level.FINE);
        if (echo)
            logger.printMessage(toString());
        else if (logger.isDebug())
            logger.printDebugMessage(toString());
    }

    /**
     * Search all the parameters that were actually specified to see
     * if any of them are FILE type parameters.  If so, check for the
     * "--upload" option.
     */
    private void initializeDoUpload() {
	boolean sawFile = false;
	boolean sawDirectory = false;
	for (Map.Entry<String, String> param : params.entrySet()) {
	    String paramName = param.getKey();
	    ValidOption opt = getOption(paramName);
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
	if (operandType.equals("FILE")) {
	    for (String filename : operands) {
		sawFile = true;
		// if any FILE parameter is a directory, turn off doUpload
		File file = new File(filename);
		if (file.isDirectory())
		    sawDirectory = true;
	    }
	}

	if (sawFile && !sawDirectory) {
	    // found a FILE param, is doUpload set?
	    String upString = params.get("upload");
	    if (ok(upString))
		doUpload = Boolean.parseBoolean(upString);
	    else
		doUpload = true;	// defaults to true
	}

	if (addedUploadOption)
		params.remove("upload");    // XXX - remove it
    }

    /**
     * Get the ValidOption descriptor for the named option.
     *
     * @param name  the option name
     * @return	    the ValidOption descriptor
     */
    private ValidOption getOption(String name) {
	for (ValidOption opt : commandOpts)
	    if (opt.getName().equals(name))
		return opt;
	return null;
    }

    private void initializeAuth() throws CommandException {
        LoginInfo li = null;
        
        try {
	    LoginInfoStore store = LoginInfoStoreFactory.getDefaultStore();
            li = store.read(hostName, hostPort);
        }
        catch (StoreException se) {
            logger.printDebugMessage(
		    "Login info could not be read from ~/.asadminpass file");
        }
        initializeUser(li);
        initializePassword(li);
    }

    private void initializeUser(LoginInfo li) {
        user = params.get("user");
        if (user == null && li != null) { //not on command line & in .asadminpass
            user = li.getUser();
        }
    }
    
    private void initializePassword(LoginInfo li) throws CommandException {

        encodedPasswords = new HashMap<String, String>();

        // this is for asadmin-login command's special processing.        
        if (params.get("password") != null) {
            password = params.get("password");
            params.remove("password");
            encodedPasswords.put(CLIUtil.ENV_PREFIX+"PASSWORD",password);
            base64encode(encodedPasswords);
        }

        String pwfile = params.get("passwordfile");
        
        if (ok(pwfile)) {
            encodedPasswords = CLIUtil.readPasswordFileOptions(pwfile, true);
            password = encodedPasswords.get(CLIUtil.ENV_PREFIX + "PASSWORD");
            base64encode(encodedPasswords);
        }
        
        if (!ok(password) && li != null) { // not in passwordfile and in .asadminpass
            password = li.getPassword();
        }                
    }

    /**
     * Initialize all the passwords required by the command.
     *
     * @throws CommandException
     */
    private void initializeCommandPassword() throws CommandException {
        if (commandName.equalsIgnoreCase("change-admin-password")) {
            try {
                password = getInteractiveOptionWithConfirmation(encodedPasswords);
                base64encode(encodedPasswords);
                return;
            } catch (CommandValidationException cve) {
                throw new CommandException(cve);
            }
        }

        if ((commandName.equalsIgnoreCase("create-password-alias") ||
                commandName.equalsIgnoreCase("update-password-alias"))) {
            try {
                password = confirmInteractivelyAliasPassword(encodedPasswords);
                base64encode(encodedPasswords);
            } catch (CommandValidationException cve) {
                throw new CommandException(cve);
            }
        }

	/*
	 * Go through all the valid options and check for required password
	 * options that weren't specified in the password file.  If option
	 * is missing and we're interactive, prompt for it.
	 */
	for (ValidOption opt : commandOpts) {
	    if (!opt.getType().equals("PASSWORD"))
		continue;
	    if (encodedPasswords == null)   // initialize if not already done
		encodedPasswords = new HashMap<String, String>();
	    String pwdname = opt.getName();
	    if (ok(encodedPasswords.get(pwdname)))
		continue;
	    if (opt.isValueRequired() != ValidOption.REQUIRED)
		continue;
	    try {
		String pwd = getPassword(opt.getName());
		if (pwd == null)
		    throw new CommandException(strings.get("missingPassword",
			commandName, pwdname));
		pwd = new GFBase64Encoder().encode(pwd.getBytes());
		encodedPasswords.put(pwdname, pwd);
            } catch (CommandValidationException cve) {
                throw new CommandException(cve);
            }
	}
    }
    
    private String confirmInteractivelyAliasPassword(
	    Map<String, String> encodedPasswords)
	    throws CommandValidationException {
        final String prompt = getLocalizedString("AliasPasswordPrompt");
        final String confirmationPrompt = 
            getLocalizedString("AliasPasswordConfirmationPrompt");

        String aliasPassword = getInteractiveOption(prompt);
        encodedPasswords.put(CLIUtil.ENV_PREFIX+"ALIASPASSWORD",aliasPassword);
        
        String aliasPasswordAgain = getInteractiveOption(confirmationPrompt);
        
        if (!aliasPassword.equals(aliasPasswordAgain)) {
            throw new CommandValidationException(getLocalizedString(
                "OptionsDoNotMatch", new Object[]{"Admin Password"}));
        }
        return aliasPassword;        
    }
    
    private String getPassword(String passwordName)
	    throws CommandValidationException {

	if (!interactive)
	    return null;

	// XXX - prompts aren't right
        final String newprompt = getLocalizedString("AdminNewPasswordPrompt");
        final String confirmationPrompt =
            getLocalizedString("AdminNewPasswordConfirmationPrompt");

        String newpassword = getInteractiveOption(newprompt);
        if (!isPasswordValid(newpassword)) {
            throw new CommandValidationException(getLocalizedString(
                    "PasswordLimit", new Object[]{"Admin"}));
        }

        String newpasswordAgain =
            getInteractiveOption(confirmationPrompt);
        if (!newpassword.equals(newpasswordAgain)) {
            throw new CommandValidationException(getLocalizedString(
                "OptionsDoNotMatch", new Object[]{"Admin Password"}));
        }
	return newpassword;
    }

    private String getInteractiveOptionWithConfirmation(
	    Map<String, String> encodedPasswords)
	    throws CommandValidationException {
        
        final String prompt = getLocalizedString("AdminPasswordPrompt");
        final String newprompt = getLocalizedString("AdminNewPasswordPrompt");
        final String confirmationPrompt = 
            getLocalizedString("AdminNewPasswordConfirmationPrompt");

        String oldpassword = getInteractiveOption(prompt);
        if (!isPasswordValid(oldpassword)) {
            throw new CommandValidationException(getLocalizedString(
                    "PasswordLimit", new Object[]{"Admin"}));
        }
        encodedPasswords.put(CLIUtil.ENV_PREFIX+"PASSWORD",oldpassword);
        
        String newpassword = getInteractiveOption(newprompt);
        if (!isPasswordValid(newpassword)) {
            throw new CommandValidationException(getLocalizedString(
                    "PasswordLimit", new Object[]{"Admin"}));
        }
        encodedPasswords.put(CLIUtil.ENV_PREFIX+"NEWPASSWORD",newpassword);
        
        String newpasswordAgain = 
            getInteractiveOption(confirmationPrompt);
        if (!newpassword.equals(newpasswordAgain)) {
            throw new CommandValidationException(getLocalizedString(
                "OptionsDoNotMatch", new Object[]{"Admin Password"}));
        }
        return oldpassword;
    }

    protected String getInteractiveOption(String prompt)
            throws CommandValidationException {

        String optionValue;
        try {
            InputsAndOutputs.getInstance().getUserOutput().print(prompt);
            InputsAndOutputs.getInstance().getUserOutput().flush();
            optionValue = new CliUtil().getPassword();
        } catch (java.lang.NoClassDefFoundError e) {
            optionValue = readInput();
        } catch (java.lang.UnsatisfiedLinkError e) {
            optionValue = readInput();
        } catch (Exception e) {
            throw new CommandValidationException(e);
        }
        return optionValue;
    }
    
    private String readInput() {
        try {
            return InputsAndOutputs.getInstance().getUserInput().getLine();
        } catch (IOException ioe) {
            return null;
        }
    }

    protected boolean isPasswordValid(String passwd) {
        return (passwd.length() < 8)? false:true;
    }

    private String getLocalizedString(String key) {
        LocalStringsManager lsm = null;
        try {
            lsm = LocalStringsManagerFactory.getCommandLocalStringsManager();
        } catch (CommandValidationException cve) {
            return LocalStringsManager.DEFAULT_STRING_VALUE;
        }
        return lsm.getString(key);
    }
    
    private String getLocalizedString(String key, Object[] toInsert) {
        LocalStringsManager lsm = null;
        try {
            lsm = LocalStringsManagerFactory.getCommandLocalStringsManager();
            return lsm.getString(key, toInsert);
        } catch (CommandValidationException cve) {
            return LocalStringsManager.DEFAULT_STRING_VALUE;
        }
    }

    private static void base64encode(Map<String,String> map) {
        if (map == null || map.isEmpty())
            return;

        GFBase64Encoder encoder = new GFBase64Encoder();

        for (Map.Entry<String,String> entry : map.entrySet()) {
            String val = entry.getValue();
            
            if (val != null)
                entry.setValue(encoder.encode(val.getBytes()));
        }
    }

    /**
     * If this is an unsupported command, throw an exception.
     */
    private static void checkUnsupportedLegacyCommand(String cmd)
            throws CommandException {
        for (String c : unsupported) {
            if (c.equals(cmd)) {
                throw new CommandException(
                    "Previously supported command: " + cmd +
		    " is not supported for this release.");
            }
        }
        // it is a supported command; do nothing
    }

    /**
     * Read the named resource file and add the first token on each line
     * to the set.  Skip comment lines.
     */
    private static void file2Set(String file, Set<String> set) {
        BufferedReader reader = null;
        try {
            InputStream is = CLIRemoteCommand.class.getClassLoader().
				getResourceAsStream(file);
	    if (is == null)
		return;	    // in case the resource doesn't exist
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#"))
                    continue; // # indicates comment
                StringTokenizer tok = new StringTokenizer(line, " ");
                // handles with or without space, rudimendary as of now
                String cmd = tok.nextToken();
                set.add(cmd);
            }
        } catch (IOException e) {
	    e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ee) {
                    // ignore
                }

            }
        }
    }
}