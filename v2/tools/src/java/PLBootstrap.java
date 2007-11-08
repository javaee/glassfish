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

/**
 * This class is used for executing processes that are in an envronment that has a VERY limited
 * command line execute size.  The links off the start menu in windows has a maximum size of 259 characters.
 * The PLBootstart requires that appserver installroot path is in the command twice and 
 * the java installroot is the in the command once, so allowance have to be given for user directory location
 * preference.  The reason, PLBoostrap is not placed inside a package is to keep the command size as small as possible.
 * The associated properties file holds the classpath and property information requirer to setup the
 * com.sun.enterprise.tools.ProcessLauncer environment.
 */

import java.util.Properties;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.StringTokenizer;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.lang.reflect.Method;

public class PLBootstrap {
    
    public static final String INSTALL_ROOT_PROPERTY_NAME="com.sun.aas.installRoot";
    public static final String PROCESS_LAUCHER_PROPERTIES_FILE_NAME="processLauncher.properties";
    public static final String PROCESS_LAUCHER_LIBRARIES="processLauncher.libraries";
    public static final String PROCESS_LAUCHER_MAIN_CLASS="processLauncher.main.class";
    
    
    public static final boolean bDebug=false;
    
    public static void main(String[] args) {
        
        try {
            
            Properties props=System.getProperties();
            if (bDebug) props.list(System.out);
            
            // get processLauncher properties from installroot property
            // directory as this class
            String installRoot=System.getProperty(INSTALL_ROOT_PROPERTY_NAME);
            if (installRoot == null) {
                System.out.println("ERROR: The System property \"com.sun.aas.installRoot\" has to be set!");
                System.exit(1);
            }
            
            String appservLibPath=installRoot + File.separator + "lib" + File.separator;    
            // compile location to the properties file
            if (bDebug) System.out.println("Reading properties from - " + appservLibPath + PROCESS_LAUCHER_PROPERTIES_FILE_NAME);
            File propertiesFile=new File(appservLibPath + PROCESS_LAUCHER_PROPERTIES_FILE_NAME);
            // see if properties exit, if not exception
            if (!propertiesFile.canRead()) {
                throw new FileNotFoundException(propertiesFile.getPath());
            }

            // load properties from file
            Properties properties=new Properties();
            FileInputStream fInput=new FileInputStream(propertiesFile);
            properties.load(fInput);
            fInput.close();

            // get the classpath property and create an appropriate classloader
            URL[] classJars=stringToURLArray(appservLibPath, properties.getProperty(PROCESS_LAUCHER_LIBRARIES));
            URLClassLoader classLoader=new URLClassLoader(classJars, Thread.currentThread().getContextClassLoader());

            // Load ProcessLauncher class
            Class plClass=classLoader.loadClass(properties.getProperty(PROCESS_LAUCHER_MAIN_CLASS));
            if (bDebug) System.out.println("classloader = " + classLoader + " - " + plClass.getClassLoader());

            // get the method
            Method mainMethod=plClass.getDeclaredMethod("bootstrap", new Class[]{ String[].class });
            mainMethod.invoke(null, new Object[]{ args });
            
            // explicit exit
            System.exit(0);
            
       } catch (Throwable t) {
           t.printStackTrace();
           System.exit(1);

        }
    }
    
    
    private static URL[] stringToURLArray(String appservLibPath, String jarList) throws MalformedURLException {

        if (bDebug) System.out.println("jar list - " + jarList);
        
        // make sure list exists
        if (jarList == null || jarList.equals("")) {
            return new URL[0];
        }

        // loop though a build arraylist of jars
        ArrayList jars=new ArrayList();
        StringTokenizer stJars=new StringTokenizer(jarList, ",");
        URL resultantURL=null;
        String jarName=null, jarPath=null;
        while (stJars.hasMoreTokens()) {
            jarName=stJars.nextToken().trim();
            if (bDebug) System.out.println("creating url for - " + jarName);
            
            // if name starts with a "/" use is as a full path
            if (jarName.startsWith("/")) {
                jarPath=jarName;
            } else {
                jarPath=appservLibPath + jarName;
            }
            
            // should be in local path
            resultantURL=new URL("file:" + jarPath);
            if (bDebug) System.out.println("resultant url - " + resultantURL);
            jars.add(resultantURL);
        }
        return (URL[])jars.toArray(new URL[jars.size()]);
    }    
    
    
}