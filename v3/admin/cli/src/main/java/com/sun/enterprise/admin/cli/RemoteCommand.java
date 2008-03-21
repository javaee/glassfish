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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import com.sun.enterprise.admin.cli.deployment.FileUploadUtil;
import com.sun.enterprise.admin.cli.util.CLIUtil;
import com.sun.enterprise.admin.cli.util.HttpConnectorAddress;
import com.sun.enterprise.admin.cli.util.AuthenticationInfo;

/**
 * RemoteCommand class 
 */
public class RemoteCommand {

    private static final RemoteCommand INSTANCE = new RemoteCommand();

    RemoteCommand() {
    }

    public static RemoteCommand getInstance() {
        return INSTANCE;
    }

    public void handleRemoteCommand(final String[] args) {
        handleRemoteCommand(args, "hk2-cli", null);
    }

    /**
     * Runs the command using the specified arguments and sending the result
     * to the caller-provided {@link OutputStream} in the requested format for processing.
     * @param args the arguments to use in building the command
     * @param responseFormatType direction to the server as to how to format the response; usually hk2-cli or xml-cli
     * @param userOut the {@link OutputStream} to which to write the command's response text
     */
    public void handleRemoteCommand(final String[] args,
                                     String responseFormatType,
                                     OutputStream userOut) {
        if (args.length == 0) {
            System.err.println("usage : asadmin <command> [parameters]");
            return;
        }
        try {
            //testing RemoteCommandParser.java
            final RemoteCommandParser rcp = new RemoteCommandParser(args);
            if (TRACE) {
                System.out.println("RemoteCommandParser: " + rcp);
            }
            final Map<String, String> params = rcp.getOptions();
            final Vector operands = rcp.getOperands();

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
                //temporary make user as optional
            final String user = (params.get("user") == null ? "" : params.get("user"));            
                //temporary make password as optional
            String password = "";
            if (params.get("passwordfile") != null) {
                Map passwordOptions = CLIUtil.readPasswordFileOptions(params.get("passwordfile"));
                password = (String)passwordOptions.get("password");
            }
            uriConnection = "/__asadmin/" + rcp.getCommandName();
            for (Map.Entry<String, String> param : params.entrySet()) {
                String paramName = param.getKey();
                //do not want to add host/port/upload/secure/user/password to the uri
                if (paramName.equals("host") || paramName.equals("port") ||
                    paramName.equals("upload") || paramName.equals("user") ||
                    paramName.equals("passwordfile") || paramName.equals("secure") ) {
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
                    System.err.println("Error encoding " + paramName + ", parameter value will be ignored");
                }
            }

            //add operands
            for (int ii = 0; ii < operands.size(); ii++) {
                final String operand = (String) operands.get(ii);
                if (rcp.getCommandName().equals("deploy")) {
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

            if (TRACE) {
                System.out.println("Connecting to " + uriConnection);
            }
            try {
                HttpConnectorAddress url = new HttpConnectorAddress(hostName, Integer.parseInt(hostPort), isSecure);
                url.setAuthenticationInfo(new AuthenticationInfo(user, password));

                if (fileName != null && uploadFile) {
                    if (fileName.exists()) {
                        HttpURLConnection urlConnection = FileUploadUtil.upload(url.toURL(uriConnection).toString(),
                                                                                fileName);
                        InputStream in = urlConnection.getInputStream();
                        handleResponse(params, in,
                                       urlConnection.getResponseCode());
                    } else {
                        throw new Exception("File " + fileName.getName() + " does not exist.");
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
                System.err.println("Cannot connect to host, is server up ?");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        if (fileName != null && commandName.equals("deploy")) {
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
                                 InputStream in, int code, OutputStream userOut) throws IOException {
        if (userOut == null) {
            handleResponse(params, in, code);
        } else {
            copyStream(in, userOut);
        }
    }

    private void handleResponse(Map<String, String> params,
                                 InputStream in, int code) throws IOException {
        if (TRACE) {
            // dump the content
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            copyStream(in, baos);
            System.out.println("Response\n=====");
            System.out.println(baos);
            System.out.println("=====");
            in = new ByteArrayInputStream(baos.toByteArray());
        }

        if (code == 200) {
            Manifest m = getManifest(in);
            if (m == null) {
                return;
            }

            if (params.size() == 1 && params.get("help") != null) {
                processHelp(m);
            } else {
                processMessage(m);
            }
        } else {
            System.out.println("Failed : error code " + code);
        }
    }

    private Manifest getManifest(InputStream is) {
        try {
            Manifest m = new Manifest();
            m.read(is);

            if (Boolean.getBoolean("dump.manifest")) {
                m.write(System.out);
            }
            return m;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {

                }
            }
        }
        return null;
    }

    private void processHelp(Manifest m) {
        System.out.println("");
        System.out.println(m.getMainAttributes().getValue("message"));
        System.out.println("");
        System.out.println("Parameters : ");
        Attributes attr = m.getMainAttributes();
        String keys = attr.getValue("keys");
        if (keys != null) {
            StringTokenizer token = new StringTokenizer(keys, ",");
            if (token.hasMoreTokens()) {
                while (token.hasMoreTokens()) {
                    String property = token.nextToken();
                    String name = attr.getValue(property + "_name");
                    String value = attr.getValue(property + "_value");
                    System.out.println("\t" + name + " : " + value);
                }
            }
        }
    }

    private void processMessage(Manifest m) {
        String exitCode = m.getMainAttributes().getValue("exit-code");
        String message = m.getMainAttributes().getValue("message");

        if (exitCode != null) {
            System.out.println(exitCode + " : " + message);
            if (!exitCode.equalsIgnoreCase("Success")) {
                return;
            }
        } else {
            System.out.println(message);
        }

        processOneLevel("", null, m, m.getMainAttributes());

    }

    private void processOneLevel(String prefix, String key, Manifest m,
                                  Attributes attr) {

        String keys = attr.getValue("keys");
        if (keys != null) {
            StringTokenizer token = new StringTokenizer(keys, ",");
            if (token.hasMoreTokens()) {
                System.out.print(prefix + "properties=(");
                while (token.hasMoreTokens()) {
                    String property = token.nextToken();
                    String name = attr.getValue(property + "_name");
                    String value = attr.getValue(property + "_value");
                    System.out.print(name + "=" + value);
                    if (token.hasMoreElements()) {
                        System.out.print(",");
                    }
                }
                System.out.println(")");
            }
        }
        String children = attr.getValue("children");
        if (children == null) {
            // no container currently started.
            return;
        }

        String childrenType = attr.getValue("children-type");
        StringTokenizer token = new StringTokenizer(children, ",");
        while (token.hasMoreTokens()) {
            String container = token.nextToken();
            int index = key == null ? 0 : key.length() + 1;
            System.out.println(prefix + childrenType + " : " + container.substring(index));
            // get container attributes
            Attributes childAttr = m.getAttributes(container);
            processOneLevel(prefix + "\t", container, m, childAttr);
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
    public static final boolean TRACE = Boolean.getBoolean("trace");
}


