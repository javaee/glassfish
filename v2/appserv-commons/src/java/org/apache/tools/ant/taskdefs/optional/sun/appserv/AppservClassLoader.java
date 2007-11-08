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

/*
 * AppservClassLoader.java
 *
*/

package org.apache.tools.ant.taskdefs.optional.sun.appserv;

import java.util.Hashtable;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;


public class AppservClassLoader extends URLClassLoader {
    private Hashtable hashTable = new Hashtable(3); // small
    
    /** Creates new AppservClassLoader  _mapping is key=value;key=value....*/
    public AppservClassLoader() throws MalformedURLException, RuntimeException {
        super(new URL[0], AppservClassLoader.class.getClassLoader());
    }
    
    public void addURL(File f) throws MalformedURLException, RuntimeException {
            if (f.isFile()){
                addURL(f.toURI().toURL());
              ///  System.out.println("adding file  = "+f.getAbsolutePath());
              ///  System.out.println("adding file  exists= "+f.exists() );
            }
            else{
              ///  System.out.println("file does not exist!!! = "+f.getAbsolutePath());
            }
    }


    public static AppservClassLoader getClassLoader() {
        
        AppservClassLoader loader = null;

        try {
            loader = new AppservClassLoader();
            String installRoot = System.getProperty("com.sun.aas.installRoot");
            if (installRoot == null) {
                    //set installRoot to current directory
                installRoot = ".";
            }

            File f;
            
            final String classpathPrefix = System.getProperty("com.sun.ant.classpath.prefix");
            if (classpathPrefix != null) {
                f = new File(classpathPrefix);
                loader.addURL(f);
            }

            final String derbyRoot = System.getProperty("derby.root");
            if (derbyRoot != null) {
                //set derby jar file
                f = new File (derbyRoot+"/lib/derby.jar");
                loader.addURL(f);
            }
            
            f = new File(installRoot+"/lib");
            loader.addURL(f);
            f = new File(installRoot+"/lib/webservices-rt.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/webservices-tools.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/appserv-se.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/appserv-admin.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/appserv-ext.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/appserv-rt.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/appserv-cmp.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/admin-cli.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/commons-launcher.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/javaee.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/install/applications/jmsra/imqjmsra.jar");
            loader.addURL(f);
        }catch (Exception ex) {
            ex.printStackTrace();
                //throw new Exception(ex2.getLocalizedMessage());
        }
        return loader;
    }

}
