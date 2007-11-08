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

package com.sun.enterprise.addons;

import java.io.File;
import java.net.URL;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.appserv.addons.AddonFatalException;
import com.sun.enterprise.addons.util.JarFileFilter;
import com.sun.enterprise.util.OS;

/**
 * Utility class that computes the classpath setting
 * for the addon. Classpath is 
 * AS_HOME/lib/*.jar;AS_HOME/lib/ant/lib/*.jar
 *
 * @since 9.1
 * @author binod@dev.java.net
 */
class AddonClassPath {

    private static final String CONFIG = "config";
    private static final String ASENVCONF = "asenv.conf";
    private static final String ASENVBAT = "asenv.bat";
    private static final String ASANTREGEX = "(AS_ANT_LIB=)";
    private static Logger logger = null;

    static URL[] getClassPath(URL jar, String installRoot, Logger logr) 
    throws AddonFatalException {
        logger = logr;
        File asLib = new File (installRoot + File.separator + "lib");
        File antLib = getAntLib(installRoot);

        File[] asLibJars = asLib.listFiles(new JarFileFilter());
        File[] antLibJars = antLib.listFiles(new JarFileFilter());

        URL[] urls = new URL[asLibJars.length + antLibJars.length+1];
        int i = 0;
        try {
            for (File asLibJar : asLibJars ) {
                urls[i] = asLibJar.toURI().toURL();
                i++;
            }

            for (File antLibJar : antLibJars ) {
                urls[i] = antLibJar.toURI().toURL();
                i++;
            }
        } catch (Exception e) {
            throw new AddonFatalException(e);
        }

        urls[i] = jar;
        return urls;
    }


    private static boolean isUnix() {
        return OS.isUnix();
    }


    private static File getAntLib(String installDir) throws AddonFatalException{
        BufferedReader bf = null;
        FileInputStream in = null;
        try {
            String asenv="";
            if(isUnix()) {
                asenv = installDir + File.separator + CONFIG + File.separator + 
                        ASENVCONF;
            } else {
                asenv = installDir + File.separator + CONFIG + File.separator + 
                        ASENVBAT;
            }
        
            logger.log(Level.FINER, asenv);

            in = new FileInputStream(asenv);
            String antLib = "";
            bf = new BufferedReader(new InputStreamReader(in));
            String line = bf.readLine();
            while(line != null) {
                logger.log(Level.FINER,line);
                String[] asantLine = line.split(ASANTREGEX);
                if (asantLine.length > 1) {
                    if (isUnix()) {
                       asantLine = asantLine[1].split("\"");
                    } 
                    antLib = asantLine[1];
                    logger.log(Level.FINER,"antLib "+antLib);
                    return new File(antLib);
                }
                line = bf.readLine();
             }
             return null;
        }catch(Exception e) {
            logger.log(Level.SEVERE,e.getMessage(), e);
            throw new AddonFatalException(e);
        } finally {
            try {
                if(bf != null)  
                    bf.close();
                if(in != null)
                    in.close();  
            } catch (Exception e) {
                logger.log(Level.WARNING,e.getMessage(), e);
            }
        }
    }
}
