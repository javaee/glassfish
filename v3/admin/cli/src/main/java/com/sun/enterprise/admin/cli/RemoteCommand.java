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
package com.sun.enterprise.admin.cli;

import com.sun.appserv.management.client.prefs.LoginInfo;
import com.sun.appserv.management.client.prefs.LoginInfoStore;
import com.sun.appserv.management.client.prefs.LoginInfoStoreFactory;
import com.sun.appserv.management.client.prefs.StoreException;
import com.sun.enterprise.universal.StringUtils;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.admin.cli.deployment.FileUploadUtil;
import com.sun.enterprise.admin.cli.remote.RemoteException;
import com.sun.enterprise.admin.cli.remote.RemoteFailureException;
import com.sun.enterprise.admin.cli.remote.RemoteResponseManager;
import com.sun.enterprise.admin.cli.remote.RemoteSuccessException;
import java.io.*;
import java.net.*;
import java.util.*;
import com.sun.enterprise.admin.cli.util.*;
import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.universal.collections.ManifestUtils;
import java.util.jar.*;
import sun.misc.BASE64Encoder;
/**
 * RemoteCommand class 
 */

public class RemoteCommand {
    public RemoteCommand() {
    }
    public RemoteCommand(String... args) throws CommandException {
        handleRemoteCommand(args);
    }


    public void handleRemoteCommand(String... args) throws CommandException {
        handleRemoteCommand(args, "hk2-cli", null);
    }

    /**
     * Runs the command using the specified arguments and sending the result
     * to the caller-provided {@link OutputStream} in the requested format for processing.
     * @param args the arguments to use in building the command
     * @param responseFormatType direction to the server as to how to format the response; usually hk2-cli or xml-cli
     * @param userOut the {@link OutputStream} to which to write the command's response text
     * @throws com.sun.enterprise.cli.framework.CommandException 
     */
    public void handleRemoteCommand(final String[] args,
                                     String responseFormatType,
                                     OutputStream userOut) 
                                     throws CommandException{
        if (args.length == 0) {
            throw new CommandException(strings.get("remote.noargs"));
        }
        try {
            //testing RemoteCommandParser.java
            final RemoteCommandParser rcp = new RemoteCommandParser(args);
            logger.printDebugMessage("RemoteCommandParser: " + rcp);
            final Map<String, String> params = rcp.getOptions();
            setBooleans(params);
            if (terse) {
                logger.setOutputLevel(java.util.logging.Level.INFO);
            } 
            else {
                logger.setOutputLevel(java.util.logging.Level.FINE);
            }
            
            final Vector operands = rcp.getOperands();

            if(echo) {
                logger.printMessage(toString(args[0], params, operands));
            }
            else if(logger.isDebug()){
                logger.printDebugMessage(toString(args[0], params, operands));
            }
            //upload option  for deploy command is default to true
            //operand takes precedence over --path option
            final boolean uploadFile = getUploadFile((String) params.get("upload"),
                                                      rcp.getCommandName(),
                                                      operands.size() > 0 ? (String) operands.firstElement() : (String) params.get("path"));
            File fileName = null;
            String uriConnection;
            final String hostName = (params.get("host") == null ? "localhost" : params.get("host"));
            final String hostPort = (params.get("port") == null ? "8080" : params.get("port"));
                //set default value of secure to false
            final String secure = (params.get("secure") == null ? "false" : params.get("secure"));
            final boolean isSecure = Boolean.parseBoolean(secure);
            String user = null, password = null;
            Map<String, String> passwordOptions = null;            
            if (params.get("passwordfile") != null) {
                passwordOptions = 
                    CLIUtil.readPasswordFileOptions(params.get("passwordfile"), true);
            }
            try {
                store = LoginInfoStoreFactory.getDefaultStore();
            } catch (StoreException se) {
                throw new CommandException(se);
            }
            LoginInfo li    = store.read(hostName, Integer.parseInt(hostPort));
            user            = getUser(li, params);
            //System.out.println("User = " + user);
            password        = getPassword(li, params);
            //System.out.println("Password = " + password);
            
            uriConnection = "/__asadmin/" + rcp.getCommandName();
            
            for (Map.Entry<String, String> param : params.entrySet()) {
                String paramName = param.getKey();
                //do not want to add host/port/upload/secure/user/password to the uri
                if (paramName.equals("host") || paramName.equals("port") ||
                    paramName.equals("upload") || paramName.equals("user") ||
                    paramName.equals("passwordfile") || paramName.equals("secure") ||
                    paramName.equals("terse") || paramName.equals("echo") ||
                    paramName.equals("interactive") ) {
                    continue;
                }
                try {
                    String paramValue = param.getValue();
                    // let's check if I am passing a valid path...
                    if (paramName.equals("path")) {
                        fileName = new File(paramValue);
                        //get new paramValue since it may be
                        //absoluate path if uploadFile=false
                        paramValue = getFileParam(uploadFile, fileName);
                    }
                    uriConnection = uriConnection + "?" + paramName + "=" + URLEncoder.encode(paramValue,
                                                                                                "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    logger.printError("Error encoding " + paramName + ", parameter value will be ignored");
                }
            }

            // add passwordfile parameters if any
            BASE64Encoder base64encoder = new sun.misc.BASE64Encoder();
            if (passwordOptions != null) {
                for (String passwdName : passwordOptions.keySet()) {
                    String encodedpasswd = base64encoder.encode(
                            passwordOptions.get(passwdName).getBytes());
                    uriConnection = uriConnection + "?" + passwdName + "=" 
                            + encodedpasswd;
                }
            }
       
            //add operands
            for (int ii = 0; ii < operands.size(); ii++) {
                final String operand = (String) operands.get(ii);
                if (rcp.getCommandName().equals("deploy") ||
                    rcp.getCommandName().equals("redeploy")) {
                    fileName = new File(operand);
                    final String fileParam = getFileParam(uploadFile, fileName);
                    //there should only be one operand for deploy command
                    uriConnection = uriConnection + "?path=" + URLEncoder.encode(fileParam,
                                                                                   "UTF-8");
                    break;
                }
                uriConnection = uriConnection + "?DEFAULT=" + URLEncoder.encode(operand,
                                                                                  "UTF-8");
            }

            try {
                HttpConnectorAddress url = new HttpConnectorAddress(hostName, Integer.parseInt(hostPort), isSecure);
                logger.printDebugMessage("URL: " + url.toURL(uriConnection).toString());
                url.setAuthenticationInfo(new AuthenticationInfo(user, password));

                if (fileName != null && uploadFile) {
                    if (fileName.exists()) {
                        HttpURLConnection urlConnection = FileUploadUtil.upload(url.toURL(uriConnection).toString(),
                                                                                fileName);
                        InputStream in = urlConnection.getInputStream();
                        handleResponse(params, in,
                                       urlConnection.getResponseCode());
                    } else {
                        throw new CommandException("File " + fileName.getName() + " does not exist.");
                    }
                }
                else {
                    final HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection(uriConnection);
                    urlConnection.setRequestProperty("User-Agent",
                                                     responseFormatType);
                    urlConnection.setRequestProperty(HttpConnectorAddress.AUTHORIZATION_KEY, url.getBasicAuthString());
                    urlConnection.connect();

                    InputStream in = urlConnection.getInputStream();
                    handleResponse(params, in, urlConnection.getResponseCode(),
                                   userOut);
                }
            } catch (IOException e) {
                throw new CommandException("Cannot connect to host, is server up ?");
            }
        } catch (CommandException e) {
            throw e;
        } catch (Exception e) {
            logger.printExceptionStackTrace(e);
            throw new CommandException(e);
        }
    }

    /**
     * Returns either the name of the file/directory or the canonical form
     * of the file/directory.  If <code>uploadFile</code> is
     * <code>false</code> and file/directory exists on the client then the
     * canonical form is returned else the file/directory name is returned.
     *
     * @param uploadFile    indicates if file is to be uploaded to the
     *                      server.
     * @param fileName      name of the file/directory.
     * @return              returns either the file name or the canonical
     *                      form of the file/directory.
     *                      returns <code>null</code> if
     *                      <code>fileName</code> equals <code>null</code>.
     */
    String getFileParam(final boolean uploadFile, final File fileName) {
        if (fileName == null) {
            return null;
        }
        String paramValue = fileName.getName();
        if (fileName.exists() && !uploadFile) {
            try {
                paramValue = fileName.getCanonicalPath();
            } catch (IOException ioe) {
                paramValue = fileName.getAbsolutePath();
            }
        }
        return paramValue;
    }

    /**
     * Returns the <code>uploadFile</code> value.
     * If <code>uploadFile</code> is <code>true</code>, then HTTP
     * Post connect is established to upload file to server side.
     * <p>
     * <code>uploadFile</code> is determined by the following cases:
     * (1) if <code>uploadOption</code> is <code>null</code>,
     *     <code>commandName</code> is deploy and <code>fileName</code>
     *     is valid then <code>uploadFile</code> is always <code>true</code>.
     * (2) if <code>commandName</code> is deploy and <code>fileName</code>
     *     is a directory, regardless of <code>uploadOption</code>,
     *     <code>uploadFile</code> is always <code>false</code>.
     * (3) if <code>uploadOption</code> is not <code>true</code> or
     *     <code>null</code> and <code>commandName</code> is not
     *     deploy then <code>uploadFile</code> is always <code>false</code>.
     * (4) if <code>uploadOption</code> is <code>true</code> (not case
     *     sensitive) and <code>commandName</code> is not deploy then
     *     <code>uploadFile</code> is always <code>true</code>.
     *
     * @param uploadOption    upload option value specified on the
     *                        command line.
     * @param commandName     command name specified on the command line.
     * @fileName              fileName specified on the command line.
     * @return                <code>true</code> or <code>false</code>
     */
    boolean getUploadFile(final String uploadOption,
                          final String commandName,
                          final String fileName) {

        boolean uploadFile = Boolean.parseBoolean(uploadOption);
        if (fileName != null &&
            (commandName.equals("deploy") || commandName.equals("redeploy"))) {
            if (new File(fileName).isDirectory()) {
                //for directory deployment uploadFile is always false.
                uploadFile = false;
            } else if (uploadOption == null) {
                //to be compatible with GFv2, upload option is default
                //to true for deploy command
                uploadFile = true;
            }
        }
        return uploadFile;
    }

    private void handleResponse(Map<String, String> params,
                                 InputStream in, int code, OutputStream userOut) throws IOException, CommandException {
        if (userOut == null) {
            handleResponse(params, in, code);
        } else {
            copyStream(in, userOut);
        }
    }

    private void handleResponseNew(Map<String, String> params,
                                 InputStream in, int code) throws IOException, CommandException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copyStream(in, baos);
        String responseString = baos.toString();
        in = new ByteArrayInputStream(baos.toByteArray());

        try {
            RemoteResponseManager rrm = new RemoteResponseManager(baos, code);
            rrm.process();
        }
        catch(RemoteSuccessException rse) {
           Log.info(rse.getMessage()); 
           return;
        }
        catch(RemoteException rfe) {
           throw new CommandException("remote failure: " + rfe.getMessage());
        }
    }
    
    private void handleResponse(Map<String, String> params,
                                 InputStream in, int code) throws IOException, CommandException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copyStream(in, baos);
        String responseString = baos.toString();
        in = new ByteArrayInputStream(baos.toByteArray());

        logger.printDebugMessage("--------  RESPONSE DUMP         --------------");
        logger.printDebugMessage(responseString);
        logger.printDebugMessage("----------------------------------------------");
        
        if (code != 200) {
            throw new CommandException("Failed : error code " + code);
        }

        // it might not be formatted properly -- e.g. output of "amx" and man pages
        // in that case just print the message...
        try {
            serverResponse = getServerData(in);
        }
        catch(Exception e) {
            // handled below...
        }
        if (serverResponse == null) {
            processPlainText(responseString);
        }
        
        
        
        else if (params.size() == 1 && params.get("help") != null) {
            processHelp(serverResponse);
        } else {
            processMessage(serverResponse);
        }
        

    }

    private Map<String, Map<String,String>> getServerData(InputStream is) throws IOException {
        try {
            Manifest m = new Manifest();
            m.read(is);
            Map<String, Map<String,String>> serverResponse = ManifestUtils.normalize(m);
            return serverResponse;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {

                }
            }
        }
    }

    private void processHelp(Map<String,Map<String,String>> serverResponse) throws CommandException {
        
        Map<String,String> mainAtts = serverResponse.get(ManifestUtils.MAIN_ATTS);
        
        // if we got a "real" man page -- process it & return
        if(processManPage(mainAtts))
            return;

        String usageText = mainAtts.get("SYNOPSIS_value");

        if(usageText == null) {
            // this is one way to figure out there was an error!
            throw new CommandException(strings.get("remoteError", 
                    mainAtts.get("message")));
        }
        
        
        System.out.println("NAME :");
        displayInProperLen(mainAtts.get("message"));
        System.out.println("");
        System.out.println("SYNOPSIS :");
        if (usageText.startsWith("Usage: ")) {
            System.out.println("\t" + mainAtts.get("SYNOPSIS_value").substring(7));            
        } else {
            System.out.println("\t" + mainAtts.get("SYNOPSIS_value"));
        }
        System.out.println("");
        boolean displayOptionTitle = true;
        String keys = mainAtts.get("keys");
        List<String> operands = new ArrayList();
        if (keys != null) {
            StringTokenizer token = new StringTokenizer(keys, ";");
            if (token.hasMoreTokens()) {
                while (token.hasMoreTokens()) {
                    String property = token.nextToken();
                    if (property.endsWith("operand")) {
                            //collect operands and display later
                        operands.add(property);
                        continue;
                    }
                    if (property.endsWith("SYNOPSIS")) {
                            //do not want to display operand and synopsis
                        continue;
                    }
                    if (displayOptionTitle) {
                            //display only once
                        System.out.println("OPTIONS : ");
                        displayOptionTitle = false;
                    }
                    
                    String name = mainAtts.get(property + "_name");
                    String value = mainAtts.get(property + "_value");
                    logger.printMessage("\t--" + name);
                    displayInProperLen(value);
                    logger.printMessage("");
                }
            }
        }
        displayOperands(operands, mainAtts);
    }

    // bnevins Apr 8, 2008
    private boolean processManPage(Map<String,String> mainAtts) throws CommandException {
        String manPage = mainAtts.get("MANPAGE_value");

        if(!ok(manPage)) {
            return false;
        }

        logger.printMessage(manPage);
        return true;
    }
        

    
    private void displayOperands(final List<String> operands, Map<String,String> mainAtts) {
        //display operands
        if (!operands.isEmpty()) {
            System.out.println("OPERANDS : ");
            for (String operand : operands) {
                final String value = mainAtts.get(operand + "_value");
                String displayStr = operand.substring(0, operand.length()-8)
                                    + " - " + value;
                displayInProperLen(displayStr);
                logger.printMessage("");            
            }
        }
    }
    

    private void displayInProperLen(String strToDisplay) {
        int index = 0;
        for (int ii=0; ii+70<strToDisplay.length();ii+=70) {
            index=ii+70;
            String subStr = strToDisplay.substring(ii, index+1);
            if (subStr.endsWith(" ") || subStr.endsWith(",") ||
                subStr.endsWith(".") || subStr.endsWith("-") ) {
                logger.printMessage("\t" + subStr);
                ii++;
                index++;
            } else {
                logger.printMessage("\t" + strToDisplay.substring(ii, index) + "-");
            }
        }
        if (index < strToDisplay.length()) {
            logger.printMessage("\t" + strToDisplay.substring(index));
        }
    }
    

    private void processMessage(Map<String,Map<String,String>> serverResponse) throws CommandException {
        
        Map<String,String> mainAtts = serverResponse.get(ManifestUtils.MAIN_ATTS);
        
        String exitCode = mainAtts.get("exit-code");
        String message = mainAtts.get("message");

        if (exitCode == null || exitCode.equalsIgnoreCase("Success")) {
            if(ok(message))
                logger.printMessage(message);
            processOneLevel("", null, serverResponse, mainAtts);
            return;
        }
        // bnevins -- this block is pretty bizarre!
        //if there is any children message, then display it
        final String childMsg = mainAtts.get("children");
        if (childMsg != null && !childMsg.equals("")) {
            StringTokenizer childTok = new StringTokenizer(childMsg, ";");
            while (childTok.hasMoreTokens()) {
                logger.printMessage(childTok.nextToken());
            }
        }

        if(ok(message))
            message = exitCode + " : " + message;
        else
            message = exitCode;
        
        String cause = mainAtts.get("cause");

        // TODO We may need to  change this post-TP2
        if( CLILogger.isDebug()) {
            if(ok(cause)) {
                message += StringUtils.NEWLINE + strings.get("cause", cause);
            }
        }        
        throw new CommandException(message);
    }

    private void processOneLevel(String prefix, String key, Map<String, Map<String,String>> serverResponse,
                                  Map<String,String> atts) {

        if(atts == null) {
            return;
        }
        String keys = atts.get("keys");
        String output = "";
        if (keys != null) {
            StringTokenizer token = new StringTokenizer(keys, ";");
            boolean displayProperties = false;
            while (token.hasMoreTokens()) {
                String property = token.nextToken();
                //a kludge for NB plugin
                if (!property.startsWith("nb-")) {
                    if (!displayProperties) {
                        output += prefix + "properties=(";
                        displayProperties = true;
                    }
                        
                    String name = atts.get(property + "_name");
                    String value = atts.get(property + "_value");
                    output += name + "=" + value;
                    if (token.hasMoreElements()) {
                        output += ",";
                    }
                }
                if (displayProperties) {
                    output += ")";
                    logger.printMessage(output);
                    output = "";
                }
            }
        }
        String children = atts.get("children");
        if (children == null) {
            // no container currently started.
            return;
        }

        String childrenType = atts.get("children-type");
        StringTokenizer token = new StringTokenizer(children, ";");
        while (token.hasMoreTokens()) {
            String container = token.nextToken();
            int index = key == null ? 0 : key.length() + 1;
            if (childrenType.equals("null") || childrenType.equals("")) {
                logger.printMessage(container.substring(index));
            } else {
                logger.printMessage(prefix + childrenType + " : " + container.substring(index));
            }
            // get container attributes
            Map<String,String> childAtts = serverResponse.get(container);
            processOneLevel(prefix + "\t", container, serverResponse, childAtts);
        }
    }

    private void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) >= 0) {
            out.write(buf, 0, len);
        }
        out.close();
    }

    private void processPlainText(String response) throws CommandException {
        // format:
        // "PlainTextActionReporterSUCCESS..." or
        // "PlainTextActionReporterFAILURE..." or
        if(response.startsWith(MAGIC)) {
            response = response.substring(MAGIC.length());
            
            if(response.startsWith(SUCCESS)) {
                logger.printMessage(response.substring(SUCCESS.length()));
            }
            else if(response.startsWith(FAILURE)) {
                throw new CommandException(
                    strings.get("remoteError", response.substring(FAILURE.length())));
            }
            return;
        }
        // Unknown Format -- print it...
        logger.printDetailMessage(strings.get("unknownFormat"));
        logger.printMessage(response);
    }

    private void setBooleans(Map<String, String> params) {
        // need to differentiate a null value from a null key.
        // contains --> key is in the map
        
        // only 3 -- I'm not making another array!
        if(params.containsKey("verbose")) {
            String value = params.get("verbose");
            if(ok(value))
                verbose = Boolean.parseBoolean(params.get("verbose"));
            else
                verbose = true;
        }
        if(params.containsKey("echo")) {
            String value = params.get("echo");
            if(ok(value))
                echo = Boolean.parseBoolean(params.get("echo"));
            else
                echo = true;
        }            
        if(params.containsKey("terse")) {
            String value = params.get("terse");
            if(ok(value))
                terse = Boolean.parseBoolean(params.get("terse"));
            else
                terse = true;
        }
    }

    private boolean ok(String s) {
        return s != null && s.length() > 0 && !s.equals("null");
    }

    /* this is a TP2 Hack!  THis class should be derived from S1ASCommand
     * and get automatic support for this stuff
     */
    private String toString(String commandName, Map<String,String> params, Vector operands) {
        // always include terse and echo
        StringBuilder sb = new StringBuilder();
        sb.append(commandName).append(' ');
        sb.append("--echo=").append(Boolean.toString(echo)).append(' ');
        sb.append("--terse=").append(Boolean.toString(terse)).append(' ');
        Set<String> paramKeys = params.keySet();
        
        for(String key : paramKeys) {
            if(key.equals("terse") || key.equals("echo"))
                continue; //already done
            String value = params.get(key);
            sb.append("--").append(key);
            if(ok(value)) {
                sb.append('=').append(value);
            }
            sb.append(' ');
        }

        for(Object o : operands) {
            sb.append(o).append(' ');
        }
        
        return sb.toString();
    }

    Map<String,Map<String,String>> getServerResponse() {
        return serverResponse;
    }
    
    /**
     * See if DAS is alive.
     * @param port The admin port of DAS
     * @return true if DAS can be reached and can handle commands, otherwise false.
     */
    static boolean pingDAS(int port)
    {
        try {
            new RemoteCommand("version", "--port", Integer.toString(port));
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }
    private String getUser(LoginInfo li, Map<String, String> params) {
        String user = params.get("user");
        if (user == null && li != null) { //not on command line & in .asadminpass
            user = li.getUser();
        }
        return ( user );
    }
    
    private String getPassword(LoginInfo li, Map<String, String> params) throws CommandException {
        Map<String, String> passwordOptions = null;
        String password = null;
        if (params.get("passwordfile") != null) {
            passwordOptions = 
                CLIUtil.readPasswordFileOptions(params.get("passwordfile"), true);
            password = 
               (String)passwordOptions.get(CLIUtil.ENV_PREFIX + "PASSWORD");
        }
        if (password == null && li != null) { //not in passwordfile and in .asadminpass
            password = li.getPassword();
        }
        return ( password );
    }            
    private static final CLILogger logger = CLILogger.getInstance();
    private static final String SUCCESS = "SUCCESS";
    private static final String FAILURE = "FAILURE";
    private static final String MAGIC = "PlainTextActionReporter";
    private boolean verbose = false;
    private boolean terse = false;
    private boolean echo = false;
    private Map<String,Map<String,String>> serverResponse;
    private final static LocalStringsImpl strings = new LocalStringsImpl(RemoteCommand.class);
    private LoginInfoStore store = null;
}

