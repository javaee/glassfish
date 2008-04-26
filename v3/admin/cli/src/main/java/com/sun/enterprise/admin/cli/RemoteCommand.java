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
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.admin.cli.remote.RemoteException;
import com.sun.enterprise.admin.cli.remote.RemoteResponseManager;
import com.sun.enterprise.admin.cli.remote.RemoteSuccessException;
import com.sun.enterprise.universal.io.FileUtils;
import java.io.*;
import java.net.*;
import java.util.*;
import com.sun.enterprise.admin.cli.util.*;
import com.sun.enterprise.cli.framework.*;
import java.util.jar.*;
import java.util.logging.Level;
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
            String uriString;
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
            
            uriString = "/__asadmin/" + rcp.getCommandName();
            
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
                    uriString = uriString + "?" + paramName + "=" + URLEncoder.encode(paramValue,
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
                    uriString = uriString + "?" + passwdName + "=" 
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
                    uriString = uriString + "?path=" + URLEncoder.encode(fileParam,
                                                                                   "UTF-8");
                    break;
                }
                uriString = uriString + "?DEFAULT=" + URLEncoder.encode(operand,
                                                                                  "UTF-8");
            }
            HttpURLConnection urlConnection = null;
            try {
                HttpConnectorAddress url = new HttpConnectorAddress(hostName, Integer.parseInt(hostPort), isSecure);
                logger.printDebugMessage("URI: " + uriString.toString());
                logger.printDebugMessage("URL: " + url.toString());
                logger.printDebugMessage("URL: " + url.toURL(uriString).toString());
                url.setAuthenticationInfo(new AuthenticationInfo(user, password));

                urlConnection = (HttpURLConnection)url.openConnection(uriString);
                urlConnection.setRequestProperty("User-Agent", responseFormatType);
                urlConnection.setRequestProperty(HttpConnectorAddress.AUTHORIZATION_KEY, url.getBasicAuthString());
                urlConnection.connect();
                upload(fileName, uploadFile, urlConnection);
                InputStream in = urlConnection.getInputStream();
                handleResponse(params, in, urlConnection.getResponseCode(),
                               userOut);
            } catch(ConnectException ce) {
                //this really means none was listening on the remote server end
                //implementation note: ConnectException extends IOException and tells us more!
                String msg = strings.get("ConnectException", hostName, hostPort);
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
        } catch (Exception e) {
            logger.printExceptionStackTrace(e);
            throw new CommandException(e);
        }
    }

    private void upload(File file, boolean uploadFile, HttpURLConnection conn) throws CommandException{

        OutputStream out = null;
        try {
            if (file == null || !uploadFile)
                return;
            if (!file.exists())
                throw new CommandException("File " + file.getName() + " does not exist.");
            out = conn.getOutputStream();
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

            // write upload file data
            byte[] buffer = new byte[1024 * 64];
            for (int i = bis.read(buffer); i > 0; i = bis.read(buffer)) {
                out.write(buffer, 0, i);
            }
            out.flush();
            bis.close();
        }
        catch (IOException ex) {
            throw new CommandException(ex.getMessage());
        }
        finally {
            try {
                if(out != null)
                    out.close();
            }
            catch (IOException ex) {
                // ignore
            }
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
        catch(RemoteSuccessException rse) {
           if(rrm != null)
               mainAtts = rrm.getMainAtts();
           Log.info(rse.getMessage()); 
           return;
        }
        catch(RemoteException rfe) {
           throw new CommandException("remote failure: " + rfe.getMessage());
        }
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

    Map<String,String> getMainAtts() {
        return mainAtts;
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
    
    /**
     * Do not print out the results of the version command from the server 
     * @param port The admin port of DAS
     * @return true if DAS can be reached and can handle commands, otherwise false.
     */
    static boolean pingDASQuietly(int port) {
        try {
            CLILogger.getInstance().pushAndLockLevel(Level.WARNING);
            return pingDAS(port);
        }
        finally {
            CLILogger.getInstance().popAndUnlockLevel();
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
    private Map<String,String>   mainAtts;
    private final static LocalStringsImpl strings = new LocalStringsImpl(RemoteCommand.class);
    private LoginInfoStore store = null;
}

