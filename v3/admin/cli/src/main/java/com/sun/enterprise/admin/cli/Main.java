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
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * my v3 main, basically some throw away code
 */
public class Main {
    public static void main(String[] args) {

        if (args.length==0) {
            System.err.println("usage : asadmin <command> [parameters]");
            return;
        }
        String command = args[0];
        int index=1;
        boolean primaryProvided = false;
        Map<String, String> params = new HashMap<String, String>();
        while (index<args.length) {
            if (!args[index].startsWith("--") && !args[index].startsWith("__")) {
                if (!primaryProvided) {
                    primaryProvided = true;
                    params.put("DEFAULT", args[index++]);
                } else {
                    System.err.println("panic, wrong parameter name " + args[index] + " at position " + index);
                    return;
                }
            }
            if (index<args.length) {
                String parameterName = args[index++].substring(2);
                if (index<args.length) {
                    if (args[index].startsWith("--") || args[index].startsWith("__")) {
                        params.put(parameterName, "");
                    } else {
                        params.put(parameterName, args[index++]);
                    }
                } else {
                    params.put(parameterName, "");
                }
            }
        }

        String httpConnection;
        String hostName = (params.get("host")==null?"localhost":params.get("host"));
        String hostPort = (params.get("port")==null?"8080":params.get("port"));
        httpConnection = "http://" + hostName + ":"+hostPort + "/__asadmin/" + command;
        for (Map.Entry<String, String> param : params.entrySet()) {
            try {
                String paramValue = param.getValue();
                // let's check if I am passing a valid path...
                File f = new File(paramValue);
                if (f.exists()) {
                    if (!f.isAbsolute())  {
                        f = new File(System.getProperty("user.dir"), paramValue);
                    }
                    paramValue = f.getAbsolutePath();
                }
                httpConnection=httpConnection+"?"+param.getKey()+"="+ URLEncoder.encode(paramValue, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                System.err.println("Error encoding " + param.getKey() + ", parameter value will be ignored");
            }
        }
        if (TRACE) {
            System.out.println("Connecting to "+httpConnection);
        }
        try {
            URL url = new URL(httpConnection);
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestProperty("User-Agent", "hk2-cli");
            urlConnection.connect();

            InputStream in = urlConnection.getInputStream();

            if(TRACE) {
                // dump the content
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                copyStream(urlConnection.getInputStream(),baos);
                System.out.println("Response\n=====");
                System.out.println(baos);
                System.out.println("=====");
                in = new ByteArrayInputStream(baos.toByteArray());
            }

            int code = urlConnection.getResponseCode();
            if (code==200) {
                Manifest m = getManifest(in);
                if (m==null) {
                    return;
                }

                if (params.size()==1 && params.get("help")!=null) {
                   processHelp(m);
               } else {
                   processMessage(m);
               }
            } else {
                System.out.println("Failed : error code " + code);
            }
        }  catch(IOException e) {
            System.err.println("Cannot connect to host, is server up ?");
        }
    }

    private static Manifest getManifest(InputStream is) {
        try {
            Manifest m = new Manifest();
            m.read(is);

            if (Boolean.getBoolean("dump.manifest")) {
                m.write(System.out);
            }
            return m;
        } catch(IOException e) {
            e.printStackTrace();            
        }
        finally {
            if (is!=null) {
                try {
                    is.close();
                } catch(IOException e) {
                    
                }
            }
        }
        return null;
    }

    private static void processHelp(Manifest m) {
        System.out.println("");
        System.out.println(m.getMainAttributes().getValue("message"));
        System.out.println("");
        System.out.println("Parameters : ");
        Attributes attr = m.getMainAttributes();
        String keys = attr.getValue("keys");
        if (keys!=null) {
            StringTokenizer token = new StringTokenizer(keys, ",");
            if (token.hasMoreTokens()) {
                while (token.hasMoreTokens()) {
                    String property = token.nextToken();
                    String name = attr.getValue(property + "_name");
                    String value = attr.getValue(property + "_value");
                    System.out.println("\t"+name+" : "+value);
                }
            }
        }

    }

    private static void processMessage(Manifest m)  {
        String exitCode = m.getMainAttributes().getValue("exit-code");
        String message = m.getMainAttributes().getValue("message");
        System.out.println(exitCode + " : " + message);
        if (!exitCode.equalsIgnoreCase("Success")) {
            return;
        }

        processOneLevel("", null, m, m.getMainAttributes());

    }

    private static void processOneLevel(String prefix, String key, Manifest m, Attributes attr) {


        String keys = attr.getValue("keys");
        if (keys!=null) {
            StringTokenizer token = new StringTokenizer(keys, ",");
            if (token.hasMoreTokens()) {
                System.out.print(prefix+"properties=(");
                while (token.hasMoreTokens()) {
                    String property = token.nextToken();
                    String name = attr.getValue(property + "_name");
                    String value = attr.getValue(property + "_value");
                    System.out.print(name+"="+value);
                    if (token.hasMoreElements()) {
                        System.out.print(",");
                    }
                }
                System.out.println(")");
            }
        }
        String children = attr.getValue("children");
        if (children==null) {
            // no container currently started.
            return;
        }

        String childrenType = attr.getValue("children-type");
        StringTokenizer token = new StringTokenizer(children, ",");
        while (token.hasMoreTokens()) {

          String container = token.nextToken();
          int index = key==null?0:key.length()+1;
          System.out.println(prefix +  childrenType + " : " + container.substring(index));
          // get container attributes
          Attributes childAttr = m.getAttributes(container);
          processOneLevel(prefix + "\t", container, m, childAttr);
        }
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) >= 0) {
            out.write(buf, 0, len);
        }
        out.close();
    }

    public static final boolean TRACE = Boolean.getBoolean("trace");
}


