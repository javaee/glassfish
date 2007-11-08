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

package com.sun.enterprise.addon;

import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.util.OS;

/**
 * This class represents a particular addon
 * @author servesh.singh@sun.com
 */
public class Addon {
    
    private File addonJar;
   
    /** Creates a new instance of Addon */
    public Addon(File addonJar) {
        this.addonJar = addonJar;
    }
    
    public boolean install(String installDir, String instanceRoot) throws Exception 
    {
        JarFile file = new JarFile(addonJar);
        Manifest mf = file.getManifest();
        Attributes attributes = null;
        if(mf != null) {
            attributes = mf.getMainAttributes();
            if(attributes != null) {
                String mainClass = attributes.getValue(Attributes.Name.MAIN_CLASS);
                Logger.getAnonymousLogger().log(Level.FINE, "Addon getting installed: "+addonJar);
                Logger.getAnonymousLogger().log(Level.FINE, "Main Class "+mainClass); 
                if(mainClass != null) {
                    URL url = addonJar.toURI().toURL();
                    List<URL> classPath = new ArrayList<URL>();
                    classPath.add(url);
                    addClasspath(installDir, classPath);
                    //URL[] urls = new URL[] {url};
                    URL[] urls = classPath.toArray(new URL[] {});
                    URLClassLoader loader = new URLClassLoader(urls);
                    Class main = loader.loadClass(mainClass);
                    //for(int j =0; j < urls.length; j++) {
                    //    Logger.getAnonymousLogger().log(Level.INFO, "urls "+urls[j].toString());   
                    //}
                    Object obj = main.newInstance();
                    String[] argu = new String[] {installDir, instanceRoot};
                    Class[] types = new Class[] {argu.getClass()};
                    Method method = main.getMethod(AddonConstants.MAIN, types);
                    Object[] args = new Object[] { argu };
                    method.invoke(obj,args);
                    return true;
                    
                } else
                    return false;
            } else
                return false;
        } else
            return false;
    
    }
    
    /**
     * This will add the ant jars and lib jars into classpath
     * @param installDir Installation Directory
     * @param classPath 
     */
    private void addClasspath(String installDir, List classPath) throws Exception{
        BufferedReader bf = null;
        InputStream in = null;
        try {
            String asenv="";
            if(OS.isUNIX())
                asenv = installDir + File.separator + AddonConstants.CONFIG + File.separator + AddonConstants.ASENVCONF;
            else
             asenv = installDir + File.separator + AddonConstants.CONFIG + File.separator + AddonConstants.ASENVBAT;
        
            in = new FileInputStream(asenv);
            String antLib = "";
            bf = new BufferedReader(new InputStreamReader(in));
            String line = bf.readLine();
            while(line != null) {
                if(line.indexOf(AddonConstants.ASANTLIB) != -1) {
                    int pos = line.indexOf("=");
                    if (pos > 0) {
                        String lhs = (line.substring(0, pos)).trim();
                        String rhs = (line.substring(pos + 1)).trim();

                        if (OS.isWindows()) {    //trim off the "set "
                            lhs = (lhs.substring(3)).trim();
                        }

                        if (OS.isUNIX()) {      // take the quotes out
                            pos = rhs.indexOf("\"");
                            if(pos != -1) {
                                rhs = (rhs.substring(pos+1)).trim();
                                pos = rhs.indexOf("\"");
                                if(pos != -1)
                                rhs = (rhs.substring(0, pos)).trim();
                            }
                
                        }
                        antLib = rhs;
                        break;    
                    }
                }
                line = bf.readLine();
             }
                
            Logger.getAnonymousLogger().log(Level.FINE,"antLib "+antLib);
            File antLibDir = new File(antLib);
            File[] fileArray = antLibDir.listFiles();
        
            for(int i = 0;i<fileArray.length;i++) {
                if(fileArray[i].getName().endsWith(".jar")) {
                    URL url = fileArray[i].toURI().toURL();
                    classPath.add(url);
                }
            }
            File installLib = new File(installDir + File.separator + AddonConstants.LIB );
            File[] installLibArray = installLib.listFiles();
        
            Logger.getAnonymousLogger().log(Level.FINE,"installLib "+installLib.getAbsolutePath());
            for(int i = 0;i<installLibArray.length;i++) {
                if(installLibArray[i].getName().endsWith(".jar")) {
                    URL url = installLibArray[i].toURI().toURL();
                    classPath.add(url);
                }
            }
        }catch(Exception e) {
            throw e;
        } finally {
          if(bf != null)  
            bf.close();
          if(in != null)
            in.close();  
        }
        
    }
    
}
