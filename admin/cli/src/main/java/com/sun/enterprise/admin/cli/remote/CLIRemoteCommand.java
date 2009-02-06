/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.FileUtils;
import com.sun.enterprise.universal.io.SmartFile;
import java.io.*;
import java.net.*;
import java.util.*;
import com.sun.enterprise.admin.cli.util.*;
import com.sun.enterprise.cli.framework.*;
import java.util.logging.Level;
import com.sun.enterprise.universal.BASE64Encoder;
import com.sun.enterprise.util.net.NetUtils;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CLIRemoteCommand {    
    
    public CLIRemoteCommand(String... args) throws CommandException {
        initialize(args);
    }

    /** 
     * designed for use by the RemoteDeploymentFacility class
     * @param args
     * @param responseFormatType
     * @param userOut
     * @throws com.sun.enterprise.cli.framework.CommandException
     */
    public CLIRemoteCommand(String[] args, String responseFormatType, OutputStream userOut) 
                throws CommandException {
        initialize(args);
        this.responseFormatType = responseFormatType;
        this.userOut = userOut;
    }


    /**
     * Runs the command using the specified arguments and sending the result
     * to the caller-provided {@link OutputStream} in the requested format for processing.
     * @param args the arguments to use in building the command
     * @param responseFormatType direction to the server as to how to format the response; usually hk2-agent or xml-cli
     * @param userOut the {@link OutputStream} to which to write the command's response text
     * @throws com.sun.enterprise.cli.framework.CommandException 
     */
    public void runCommand() throws CommandException {
        try {
            StringBuilder uriString = new StringBuilder(ADMIN_URI_PATH + commandName);

            for (Map.Entry<String, String> param : params.entrySet()) {
                String paramName = param.getKey();
                //do not want to add host/port/upload/secure/user/password to the uri
                if (isLocalParam(paramName))
                    continue;
                try {
                    String paramValue = param.getValue();
                    // let's check if I am passing a valid path...
                    addOption(uriString, paramName + "=" + URLEncoder.encode(paramValue, "UTF-8"));
                }
                catch (UnsupportedEncodingException e) {
                    logger.printError("Error encoding " + paramName + ", parameter value will be ignored");
                }
            }

            // add deployment path
            addFileOption(uriString, "path", doUpload, fileParameter);
            // add deployment plan
            addFileOption(uriString, "deploymentplan", doUpload, deploymentPlanParameter);

            // add passwordfile parameters if any
            if(encodedPasswords != null) {
                for(Map.Entry<String,String> entry : encodedPasswords.entrySet()) {
                    addOption(uriString, entry.getKey() + "=" + entry.getValue());
                }
            }
            
            if (commandName.equalsIgnoreCase("change-admin-password")) {
                addOption(uriString, "username="+user);
            }

            //add operands
            for (String operand : operands) {
                addOption(uriString, "DEFAULT=" + URLEncoder.encode(operand,
                        "UTF-8"));
            }

            HttpURLConnection urlConnection = null;
            try {
                HttpConnectorAddress url = new HttpConnectorAddress(hostName, hostPort, secure);
                logger.printDebugMessage("URI: " + uriString.toString());
                logger.printDebugMessage("URL: " + url.toString());
                logger.printDebugMessage("URL: " + url.toURL(uriString.toString()).toString());
                url.setAuthenticationInfo(new AuthenticationInfo(user, password));

                urlConnection = (HttpURLConnection) url.openConnection(uriString.toString());
                urlConnection.setRequestProperty("User-Agent", responseFormatType);
                urlConnection.setRequestProperty(HttpConnectorAddress.AUTHORIZATION_KEY, url.getBasicAuthString());
                urlConnection.setRequestMethod(chooseRequestMethod());
                if (doUpload) {
                    urlConnection.setChunkedStreamingMode(0); // use default value
                }
                urlConnection.connect();
                if (doUpload) {
                    upload(urlConnection);
                }
                InputStream in = urlConnection.getInputStream();
                handleResponse(params, in, urlConnection.getResponseCode(),
                        userOut);
            } catch(ConnectException ce) {
                //this really means nobody was listening on the remote server end
                //implementation note: ConnectException extends IOException and tells us more!
                String msg = strings.get("ConnectException", hostName, hostPort + "");
                throw new CommandException(msg, ce);
            } catch (IOException e) {
                String msg = null;
                if (urlConnection != null) {
                    int rc = urlConnection.getResponseCode();
                    if (HttpURLConnection.HTTP_UNAUTHORIZED == rc) {
                        msg = strings.get("InvalidCredentials", user);
                    } else {
                        msg = "Status: " + rc;
                    }
                } else {
                    msg = "Unknown Error";
                }
                throw new CommandException(msg, e);                
            }
        } catch (CommandException e) {
            throw e;
        } catch(SocketException se) {
            try {
                boolean serverAppearsSecure = 
                        NetUtils.isSecurePort(hostName, hostPort);
                if (serverAppearsSecure != secure) {
                    String msg = strings.get("ServerMaybeSecure", hostName, hostPort+"");
                    logger.printError(msg);
                    throw new CommandException(se);
                }
            } catch(IOException io) {
                io.printStackTrace();
                throw new CommandException(io);
            }
        }
        catch (Exception e) {
            logger.printExceptionStackTrace(e);
            throw new CommandException(e);
        }
    }

    /**
     * Adds a single option expression to the URI, preceding it with a ? if this
     * is the first option added or a & if this is not the first option added.
     * @param uriString the URI composed so far
     * @param option the option expression to be added
     * @return the URI so far, including the newly-added option
     */
    private StringBuilder addOption(StringBuilder uriString, String option) {
        String nextChar = (uriString.indexOf(QUERY_STRING_INTRODUCER) == -1) ? 
            QUERY_STRING_INTRODUCER : QUERY_STRING_SEPARATOR;
        uriString.append(nextChar).append(option);
        return uriString;
    }
    
    /**
     * Adds an option for a file argument, passing the name (for uploads) or the
     * path (for no-upload) operations. 
     * @param uriString the URI string so far
     * @param optionName the option which takes a path or name
     * @param isUpload whether the file is to be uploaded
     * @param parameter the File whose name or path should be passed
     * @return the URI string
     * @throws java.io.UnsupportedEncodingException
     */
    private StringBuilder addFileOption(
            StringBuilder uriString, 
            String optionName, 
            boolean isUpload, 
            File parameter) throws UnsupportedEncodingException {
        if(parameter != null) {
            // if we are about to upload it -- give just the name
            // o/w give the full path
            String pathToPass = (isUpload ? parameter.getName() : parameter.getPath());
            addOption(uriString, optionName + "=" + URLEncoder.encode(pathToPass, "UTF-8"));
        }
        return uriString;
    }
    
    /**
     * Decide what request method to use in building the HTTP request.
     * @return the request method appropriate to the current command and options
     */
    private String chooseRequestMethod() {
        if (doUpload) {
            return "POST";
        } else {
            return "GET";
        }
    }
    
    /**
     * Adds the path file and the deployment plan file to the HTTP request
     * payload as ZipEntry objects.
     * @param conn the connection used for sending the http request
     * @throws com.sun.enterprise.cli.framework.CommandException
     * @throws java.io.IOException
     */
    private void upload(HttpURLConnection conn) throws CommandException, IOException {

        /*
         * Each file to be uploaded is added to the HTTP request payload as a ZipEntry.
         */
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(conn.getOutputStream()));
        if (fileParameter != null) {
            upload(zos, fileParameter);
        }
        
        if (deploymentPlanParameter != null) {
            upload(zos, deploymentPlanParameter);
        }
        
        zos.flush();
    }
    
    /**
     * Adds the specified file as a ZipEntry to the ZipOutputStream (already
     * linked to the request's output stream).
     * @param out ZipOutputStream to receive the file to be uploaded
     * @param uploadFile the File to be uploaded
     * @throws com.sun.enterprise.cli.framework.CommandException
     */
    private void upload(ZipOutputStream out, File uploadFile) throws CommandException {
        ZipEntry entry = new ZipEntry(uploadFile.getName());
        entry.setTime(uploadFile.lastModified());
        
        try {
            out.putNextEntry(entry);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(uploadFile));

            // write upload file data
            byte[] buffer = new byte[1024 * 64];
            for (int i = bis.read(buffer); i > 0; i = bis.read(buffer)) {
                out.write(buffer, 0, i);
            }
            out.closeEntry();
            bis.close();
        }
        catch (IOException ex) {
            throw new CommandException(ex.getMessage());
        }
    }

    private void handleResponse(Map<String, String> params,
            InputStream in, int code, OutputStream userOut) throws IOException, CommandException {
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
                    //CommandNotFoundException from the server, then display the closest matching
                    //commands
                throw new CommandException(rfe.getMessage(), new InvalidCommandException());
            }
            throw new CommandException("remote failure: " + rfe.getMessage(), rfe);
        }
    }

    private void setBooleans() {
        // need to differentiate a null value from a null key.
        // contains --> key is in the map

        // only 3 -- I'm not making another array!
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
    }

    private boolean ok(String s) {
        return s != null && s.length() > 0 && !s.equals("null");
    }

    /* this is a TP2 Hack!  This class should be derived from S1ASCommand
     * and get automatic support for this stuff
     */
    @Override
    public String toString() {
        // always include terse and echo
        StringBuilder sb = new StringBuilder();
        sb.append(commandName).append(' ');
        sb.append("--echo=").append(Boolean.toString(echo)).append(' ');
        sb.append("--terse=").append(Boolean.toString(terse)).append(' ');
        Set<String> paramKeys = params.keySet();

        for (String key : paramKeys) {
            if (key.equals("terse") || key.equals("echo"))
                continue; //already done
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
     * @param port The admin port of DAS
     * @return true if DAS can be reached and can handle commands, otherwise false.
     */
    public static boolean pingDAS(CommandInvoker invoker) {
        try {
            invoker.invoke();
            return true;
        }
        catch (Exception ex) {
            ExceptionAnalyzer ea = new ExceptionAnalyzer(ex);
            if (ea.getFirstInstanceOf(java.net.ConnectException.class) != null) {
                CLILogger.getInstance().printDebugMessage("Got java.net.ConnectException");
                return false; // this definitely means server is not up
            } else if (ea.getFirstInstanceOf(java.io.IOException.class) != null) {
                CLILogger.getInstance().printDebugMessage("It appears that server has started, but for" +
                        " some reason the exception is thrown: " + ex.getMessage());
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
    public static boolean pingDASWithAuth(CommandInvoker invoker) {
        try {
            invoker.invoke();
            return true;
        }
        catch (Exception ex) {
            ExceptionAnalyzer ea = new ExceptionAnalyzer(ex);
            if (ea.getFirstInstanceOf(java.net.ConnectException.class) != null) {
                CLILogger.getInstance().printDebugMessage("Got java.net.ConnectException");
                return false; // this definitely means server is not up
            } else if (ea.getFirstInstanceOf(java.io.IOException.class) != null) {
                CLILogger.getInstance().printDebugMessage("Auth info is incorrect" 
                    + ex.getMessage());
                return false;
            } else {
                return false; //unknown error, shouldn't really happen
            }
        }
    }

    /**
     * Do not print out the results of the version command from the server 
     * @param port The admin port of DAS
     * @return true if DAS can be reached and can handle commands, otherwise false.
     */
    public static boolean pingDASQuietly(CommandInvoker invoker) {
        try {
            CLILogger.getInstance().pushAndLockLevel(Level.WARNING);
            return pingDAS(invoker);
        }
        finally {
            CLILogger.getInstance().popAndUnlockLevel();
        }
    }

    
    private void initialize(final String[] args) throws CommandException {
        try {
            final CLIRemoteCommandParser rcp = new CLIRemoteCommandParser(args);
            commandName = rcp.getCommandName();
            params = rcp.getOptions();
            operands = rcp.getOperands();
            initializeStandardParams();
            initializeDeploy();
            initializeLogger();
            logger.printDebugMessage("CLIRemoteCommandParser: " + rcp);
            initializeAuth();
        }
        catch(Exception e) {
            throw new CommandException(e.getMessage());
        }
    }
    private void initializeStandardParams() throws CommandException {
        setBooleans();
        hostName = params.get("host");
        
        if (hostName == null || hostName.length() == 0)
            hostName = "localhost";
        logger.printDebugMessage("host = " + hostName);
        initializePort();

        String s = params.get("secure");
        if(ok(s))
            secure = Boolean.parseBoolean(s);
        else
            secure = false;
    }

    private void initializePort() throws CommandException {
        String port = params.get("port");
        if(ok(port)) {
            try {
                hostPort = Integer.parseInt(port);

                if(hostPort < 1 || hostPort > 65535)
                    throw new CommandException(strings.get("badport", hostPort));
            }
            catch(NumberFormatException e) {
                // this makes the default port below fire
                port = null;
            }
        }

        if(!ok(port))
            hostPort = 4848; //the default port
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

    private void initializeDeploy() throws CommandException {
        // not a deployment command -- get outta here!
        if(!isDeployment())
            return;
        // if help is a paramter, then return manpage from server side
        // no need to validate the operand
        if (params.get("help") != null)
            return;

        // it IS a deployment command.  That means we MUST have a valid path
        // with the exception of redeploy command with a directory deployment
        String filename;
        
        // operand takes precedence over param
        if(operands.size() > 0) {
            filename = operands.get(0);
            operands.clear();
        }
        else {
            filename = params.get("path");
            params.remove("path");
        }
        
        // the path could be optional for redeploy command
        if (commandName.equals("redeploy") && filename == null) {
            return;
        }

        if(!ok(filename))
            throw new CommandException(strings.get("noDeployFile", commandName));
        
        fileParameter = SmartFile.sanitize(new File(filename));
        
        if(!fileParameter.exists())
            throw new CommandException(strings.get("badDeployFile", commandName, fileParameter));

        // check for deployment plan
        String deploymentPlan = params.get("deploymentplan");
        if (deploymentPlan != null) {
            deploymentPlanParameter = SmartFile.sanitize(
                new File(deploymentPlan));

            if(!deploymentPlanParameter.exists()) {
                throw new CommandException(strings.get("badDeploymentPlan", 
                    commandName, deploymentPlanParameter));
            }
        }

        // if we make it here -- we have a file param and the file exists!
        // if the file is a directory, then we keep doUpload as false
        // if it is a normal file then we set doUpload depending on the param.
        // remember doUpload is ALREADY set to false...
        
        if(!isDirDeployment()) {
            String upString = params.get("upload");
            if(!ok(upString)) {
                // default ==> true
                doUpload = true;
            }
            else
                doUpload = Boolean.parseBoolean(upString);
        }
    }

    private void initializeAuth() throws CommandException {
        LoginInfo li = null;
        
        try {
            store = LoginInfoStoreFactory.getDefaultStore();
            li = store.read(hostName, hostPort);
        }
        catch (StoreException se) {
            logger.printDebugMessage("Login info could not be read from ~/.asadminpass file");
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
        
        if (commandName.equalsIgnoreCase("change-admin-password")
		&& params.get("help") == null ) {
            try {
                password = getInteractiveOptionWithConfirmation(encodedPasswords);
                base64encode(encodedPasswords);
                return;
            } catch (CommandValidationException cve) {
                throw new CommandException(cve);
            }
        }
        
        if (  (commandName.equalsIgnoreCase("create-password-alias") || 
                commandName.equalsIgnoreCase("update-password-alias"))
	    && params.get("help") == null) {
            try {
                password = confirmInteractivelyAliasPassword(encodedPasswords);
                base64encode(encodedPasswords);                
            } catch (CommandValidationException cve) {
                throw new CommandException(cve);
            }
        }
        
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
        if(map == null || map.isEmpty())
            return;
        
        BASE64Encoder encoder = new BASE64Encoder();
        
        for(Map.Entry<String,String> entry : map.entrySet()) {
            String val = entry.getValue();
            
            if(val != null)
                entry.setValue(encoder.encode(val.getBytes()));
        }
    }

    private boolean isDeployment() {
        return commandName.equals("deploy") || commandName.equals("redeploy") 
            || commandName.equals("deploydir");
    }

    private boolean isDirDeployment() {
        return isDeployment() && fileParameter != null && fileParameter.isDirectory();
    }

    private boolean isLocalParam(String param) {
        return Arrays.asList(LOCAL_PARAMS).contains(param);
    }
    
    private boolean                         verbose = false;
    private boolean                         terse = false;
    private boolean                         echo = false;
    private Map<String, String>             mainAtts;
    private LoginInfoStore                  store;
    private Map<String, String>             params;
    private List<String>                    operands;
    private String                          commandName;
    private String                          responseFormatType = "hk2-agent";
    private OutputStream                    userOut;
    private boolean                         doUpload = false;
    private File                            fileParameter;
    private File                            deploymentPlanParameter;
    private Map<String, String>             encodedPasswords;

    private String                          hostName;
    private int                             hostPort;
    private boolean                         secure;
    private String                          user;
    private String                          password;
    
    private static final LocalStringsImpl   strings = new LocalStringsImpl(CLIRemoteCommand.class);
    private static final CLILogger          logger = CLILogger.getInstance();
    private static final String             SUCCESS = "SUCCESS";
    private static final String             FAILURE = "FAILURE";
    private static final String             MAGIC = "PlainTextActionReporter";

    private static final String[] LOCAL_PARAMS = new String[] 
    {
        "host",
        "port",
        "upload",
        "user",
        "passwordfile",
        "secure",
        "terse",
        "echo",
        "interactive",
    };

    private static final String QUERY_STRING_INTRODUCER = "?";
    private static final String QUERY_STRING_SEPARATOR = "&";
    private static final String ADMIN_URI_PATH = "/__asadmin/";
    public static final String  RELIABLE_COMMAND = "version";
}

