/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.web.connector.grizzly.standalone;

import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import org.apache.coyote.Adapter;

/**
 * Basic startup class used when Grizzly standalone is used
 *
 * @author Jeanfrancois Arcand
 */
public class Main {
    
    /**
     * System property for the <code>SelectorThread</code> value.
     */
    private static final String SELECTOR_THREAD = 
                "com.sun.enterprise.web.connector.grizzly.selectorThread";
    
    private static final String ADAPTER = "com.sun.grizzly.adapterClass";
    
    static int port = 8080;
    
    static String folder = ".";
    
    public Main() {
    }
    
    
    public static void main( String args[] ) throws Exception {       
        Main main = new Main();        
        main.start(args);
    }

    
    /**
     * Create a single <code>Grizzly</code> http listener.
     */
    private static void start(String args[]) throws Exception {
        
        try{
            if ( args != null && args.length > 0) {
                port = Integer.parseInt(args[0]);
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
         
        try{
            if ( args != null && args.length > 1) {
                folder = args[1];
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }       
  
        URL[] urls;
        File libDir = new File("lib");
        if (libDir.exists()){
            if (libDir.isDirectory()){
                String[] jars = libDir.list(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        if (name.endsWith(".jar") || name.endsWith(".zip")){
                            return true;
                        } else {
                            return false;
                        }
                        
                    }
                });
                urls = new URL[jars.length];
                for (int i=0; i < jars.length; i++){
                   String path = new File("lib" + File.separator + jars[i])
                        .getCanonicalFile().toURL().toString();
                   urls[i] = new URL(path); 
                }
                URLClassLoader urlClassloader = new URLClassLoader(urls,
                        Main.class.getClassLoader());
                Thread.currentThread().setContextClassLoader(urlClassloader);
            }
        } else {
            System.out.println("lib folder is missing");
        }
        
        SelectorThread selectorThread = null;
        String selectorThreadClassname = System.getProperty(SELECTOR_THREAD);
        if ( selectorThreadClassname != null){
            selectorThread = (SelectorThread)loadInstance(selectorThreadClassname);
        } else {
            selectorThread = new SelectorThread();
            selectorThread
                    .setAlgorithmClassName(StaticStreamAlgorithm.class.getName());
        }        
        selectorThread.setPort(port);
        selectorThread.setWebAppRootPath(folder);
        
        String adapterClass =  System.getProperty(ADAPTER);
        Adapter adapter;
        if (adapterClass == null){
            adapter = new StaticResourcesAdapter();
        } else {
            adapter = (Adapter)loadInstance(adapterClass);
        }

        selectorThread.setAdapter(adapter);
        selectorThread.setDisplayConfiguration(true);
        selectorThread.initEndpoint();
        selectorThread.startEndpoint();
    }
   
    
    /**
     * Util to load classes using reflection.
     */
    private static Object loadInstance(String property){        
        Class className = null;                               
        try{                              
            className = Class.forName(property,true,
                    Thread.currentThread().getContextClassLoader());
            return className.newInstance();
        } catch (Throwable t) {
            t.printStackTrace();
            // Log me 
        }   
        return null;
    }      
   
}
