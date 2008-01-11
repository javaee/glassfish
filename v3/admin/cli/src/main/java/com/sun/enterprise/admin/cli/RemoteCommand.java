package com.sun.enterprise.admin.cli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import com.sun.enterprise.admin.cli.deployment.FileUploadUtil;


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
            Map<String, String> params = rcp.getOptions();
                //upload option  for deploy command is default to true
            boolean uploadFile = false;
            if (params.get("upload")==null && rcp.getCommandName().equals("deploy"))
                uploadFile = true;
            else if (params.get("upload") != null)
                uploadFile = Boolean.parseBoolean(params.get("upload"));
            
            File fileName = null;
            String httpConnection;
            final String hostName = (params.get("host") == null ? "localhost" : params.get("host"));
            final String hostPort = (params.get("port") == null ? "8080" : params.get("port"));
            httpConnection = "http://" + hostName + ":" + hostPort + "/__asadmin/" + rcp.getCommandName();
            for (Map.Entry<String, String> param : params.entrySet()) {
                String paramName = param.getKey();
                    //do not want to pass host/port/upload to the backend
                if (paramName.equals("host") || paramName.equals("port") ||
                    paramName.equals("upload") ) {
                    continue;
                }
                try {
                    String paramValue = param.getValue();
                        // let's check if I am passing a valid path...
                    if (paramName.equals("path")) {
                        fileName = new File(paramValue);
                        if (fileName.exists()) {
                            if (!fileName.isAbsolute()) {
                                fileName = new File(System.getProperty("user.dir"), paramValue);
                            }
                            if (uploadFile) {
                                paramValue = fileName.getName();
                            } else {
                                paramValue = fileName.getAbsolutePath();
                            }
                        }
                    }
                    httpConnection = httpConnection + "?" + paramName + "=" + URLEncoder.encode(paramValue, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    System.err.println("Error encoding " + paramName + ", parameter value will be ignored");
                }
            }
        
            //add operands
            final Vector operands = rcp.getOperands();
            for (int ii=0; ii<operands.size(); ii++) {
                final String operand = (String)operands.get(ii);
                if (uploadFile) {
                    fileName = new File(operand);
                        //there should only be one operand if upload=true
                    httpConnection = httpConnection + "?path=" + URLEncoder.encode(fileName.getName(),
                                                                                      "UTF-8");
                    break;
                }
                httpConnection = httpConnection + "?DEFAULT=" + URLEncoder.encode(operand, "UTF-8");
            }

            if (TRACE) {
                System.out.println("Connecting to " + httpConnection);
            }
            try {
                if (fileName!= null && uploadFile) {
                    if (fileName.exists()) {
                        HttpURLConnection urlConnection = FileUploadUtil.upload(httpConnection, fileName);
                        InputStream in = urlConnection.getInputStream();
                        handleResponse(params, in, urlConnection.getResponseCode());
                    }
                    else {
                        throw new Exception("File "+ fileName.getName() + " does not exist.");
                    }
                }
                else {
                    URL url = new URL(httpConnection);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestProperty("User-Agent", "hk2-cli");
                    urlConnection.connect();

                    InputStream in = urlConnection.getInputStream();
                    handleResponse(params, in, urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                System.err.println("Cannot connect to host, is server up ?");
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    private void processOneLevel(String prefix, String key, Manifest m, Attributes attr) {

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


