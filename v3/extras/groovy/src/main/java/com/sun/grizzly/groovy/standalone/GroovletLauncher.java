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

package com.sun.grizzly.groovy.standalone;

import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.standalone.StaticStreamAlgorithm;
import com.sun.grizzly.util.ClassLoaderUtil;
import com.sun.grizzly.util.ExpandJar;
import java.io.File;
import java.net.URL;
//import java.net.URLClassLoader;
import com.sun.grizzly.groovy.*;
import java.net.URLClassLoader;

/**
 * Basic startup class used for standalone usage
 *
 * @author Martin Grebac
 */
public class GroovletLauncher {
        
    private static int port = 8080;
    
    private static String folder = ".";
    
    private static String groovletName;
    
    private static String applicationLoc;
    
    public GroovletLauncher() {
    }
    
    
    public static void main( String args[] ) throws Exception {       
        GroovletLauncher.start(args);
    }
    
    /**
     * Create a single <code>Grizzly</code> http listener.
     */
    private static void start(String args[]) throws Exception {
        final long t1 = System.currentTimeMillis();
        if(args.length == 0) {
            printHelpAndExit();
        }
        // parse options
        for (int i = 0; i < args.length - 1; i++) {
            String arg = args[i];
            
            if("-h".equals(arg) || "--help".equals(arg)) {
                printHelpAndExit();
            } else if("-a".equals(arg)) {
                i ++;
                applicationLoc = args[i];
            } else if(arg.startsWith("--application=")) {
                applicationLoc = arg.substring("--application=".length(), arg.length());
            } else if ("-p".equals(arg)) {
                i ++;
                setPort(args[i]);
            } else if (arg.startsWith("--port=")) {
                String num = arg.substring("--port=".length(), arg.length());
                setPort(num);
            }
        }
        
        if(applicationLoc == null) {
            System.err.println("Illegal War file.");
            printHelpAndExit();
        }
        
        groovletName = args[args.length - 1];

        if (new File("lib").exists()){
            // Load jar under the lib directory
            Thread.currentThread().setContextClassLoader(
                    ClassLoaderUtil.createClassloader(
                        new File("lib"),GroovletLauncher.class.getClassLoader()));
        }
        
        if (applicationLoc.endsWith(".war") || applicationLoc.endsWith(".jar")){
            File file = new File(applicationLoc);
            URL url = new URL("jar:file:" +
                                      file.getCanonicalPath() + "!/");
            folder = ExpandJar.expand(url);
        } else {
            folder = applicationLoc;
        }
        
        System.out.println("Running Groovlet from: " + folder);
        URL[] urls = new URL[2];
        urls[0] = new URL("file://" + folder);
        urls[1] = new URL("file://" + folder + "/WEB-INF/groovy/");
        ClassLoader urlClassloader = new URLClassLoader(urls,
                Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(urlClassloader);
        
        final SelectorThread selectorThread = new SelectorThread();
        selectorThread.setPort(port);
        SelectorThread.setWebAppRootPath(folder);
                            
        GroovletAdapter adapter = new GroovletAdapter();
        adapter.setRootFolder("file://" + folder);
        selectorThread.setAdapter(adapter);
        
        System.out.println("Launching Groovlet: " + groovletName);
        
        selectorThread.initEndpoint();
        new Thread(){
            @Override
            public void run(){
                try{
                    selectorThread.startEndpoint();
                } catch (Throwable t){
                    t.printStackTrace();
                }
            }
        }.start();

        System.out.println("Server startup in " + 
                            (System.currentTimeMillis() - t1) + " ms");

        synchronized(selectorThread){
            try{
                selectorThread.wait();
            } catch (Throwable t){
                t.printStackTrace();
            }
        }   

    }
   
    private static void setPort(String num) {
        try {
            port = Integer.parseInt(num);
        } catch (NumberFormatException e) {
            System.err.println("Illegal port number -- " + num);
            printHelpAndExit();
        }
    }    
    
    private static void printHelpAndExit() {
        System.err.println("Usage: " + GroovletLauncher.class.getCanonicalName() + " [options]");
        System.err.println();
        System.err.println("    -p, --port=port                  Runs GroovletContainer on the specified port.");
        System.err.println("                                     Default: 8080");
        System.err.println("    -a, --apps=application path      The Groovlet folder or jar or war location.");
        System.err.println("                                     Default: .");
        System.err.println("    -h, --help                       Show this help message.");
        System.exit(1);
    }

}
