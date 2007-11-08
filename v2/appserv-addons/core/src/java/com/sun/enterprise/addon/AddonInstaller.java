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

/**
 * This class
 * iterates over all the jars under install/lib/addons
 * read the manifest file of each jar, if supplied
 * read main-class, instantiated it through reflection
 * call *main* method passing user/password as string array arguments 
 * 
 */
public class AddonInstaller {
    
    /** Creates a new instance of AddonInstaller */
    
    public AddonInstaller() {
        
    }
    
    /**
     * This will install all the addons under installDir/lib/addons
     * @param installDir Installation directory
     * @param instanceRoot Domain instance directory
     */
    public void installAllAddons(String installDir, String instanceRoot){
        
        String addonJar = "";
        //Properties registry = new Properties();
        Registry registry = null;
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            String addonDir = installDir + File.separator + AddonConstants.LIB + File.separator + AddonConstants.ADDONS;
            String domainConfigRoot = instanceRoot + File.separator + AddonConstants.CONFIG;
            //Logger.getAnonymousLogger().log(Level.FINE, "domainConfigRoot==="+domainConfigRoot);
            String domainRegistry = domainConfigRoot + File.separator + AddonConstants.DOMAIN_REGISTRY;
            File registryFile = new File(domainRegistry);
            registry = new Registry();
            registry.load(registryFile);
            File libAddonDirectory = new File(addonDir);
            File[] fileArray = libAddonDirectory.listFiles();
        
            for(int i = 0;i<fileArray.length;i++) {
                addonJar = fileArray[i].getName();
                String jarExtension = "";
                int dotLastIndex = addonJar.lastIndexOf(".");
                String jarNameWithoutExtension = addonJar;
                if(dotLastIndex != -1) {
                    jarExtension = addonJar.substring(dotLastIndex + 1);
                    jarNameWithoutExtension = addonJar.substring(0, dotLastIndex);
                }
                if(jarExtension.equalsIgnoreCase("jar")) {
                    //Logger.getAnonymousLogger().log(Level.INFO, "fileArray[i].getName()="+fileArray[i].getName());
                    //String key = domainName + "." + fileArray[i].getName() + "." + "installed";
                    String key = jarNameWithoutExtension + "." + "enabled";
                    String installed = registry.getProperty(key);
                    if(installed != null && installed.equals("true")) {
                        Logger.getAnonymousLogger().log(Level.FINE, "Addon "+addonJar+" is already installed");
                            continue;
                    }
                    Addon addon = new Addon(fileArray[i]);
                    boolean install = addon.install(installDir,instanceRoot);
                    if(install)
                    registry.setProperty(key, "true");
                    
                }
            }
            registry.store();
           
        }catch(Exception ex) {
            Logger.getAnonymousLogger().log(Level.WARNING, "Error while installing the addon "+addonJar, ex);
        }finally {
            try {
                if(registry != null)
                registry.close();
            }catch(Exception e) {
                
            }
        }
    
    }
    
    
    
    public static void main(String[] args) {
        AddonInstaller installer = new AddonInstaller();
        installer.installAllAddons(args[0], args[1]);
    }
    
}
