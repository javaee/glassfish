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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

  
package org.glassfish.deployment.common;

import com.sun.logging.LogDomains;

import java.io.*;
import java.text.MessageFormat;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.logging.*;
import java.util.*;


/**
 * This class resolves the dependencies between optional packages and also between 
 * apps/stand-alone modules that depend on optional packages
 * @author Sheetal Vartak
 */

public class OptionalPkgDependency {

    //optional packages are stored in hashtable that is keyed by the extension name
    private static Hashtable optionalPackageStore = new Hashtable();

    // Fully qualified names of all jar files in all ext dirs.  Note that
    // this will include even jars that do not explicitly declare the
    // extension name and specification version in their manifest.
    private static Set extDirJars = new LinkedHashSet();

    private static Logger logger = LogDomains.getLogger(DeploymentUtils.class, LogDomains.DPL_LOGGER);

    public OptionalPkgDependency() {      
    }

    public static boolean optionalPkgDependencyLogic(Manifest manifest, String archiveUri) {     

        boolean dependencySatisfied = true;

        String extensionList = null;
        try {
            extensionList = manifest.getMainAttributes().
                    getValue(Attributes.Name.EXTENSION_LIST);
            logger.fine("extensionList..." + extensionList);
        } catch (Exception npe) {
            //ignore this exception
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE,
                        "OptionalPkgDependency : exception occurred ==> " + npe.toString());
            }
        }
        
        if (extensionList == null) 
            return dependencySatisfied;
        
        StringTokenizer st =
                new StringTokenizer(extensionList, " ");
        while (st.hasMoreTokens()) {
            
            String token = st.nextToken().trim();
            String extName = manifest.getMainAttributes().
                    getValue(token + "-" + Attributes.Name.EXTENSION_NAME);
            
            String specVersion = manifest.getMainAttributes().
                    getValue(token + "-" + Attributes.Name.SPECIFICATION_VERSION);
            
            if (specVersion == null) {
                specVersion = new String("");
            }
            if (!optionalPackageStore.containsKey(extName)) {
                logger.log(Level.WARNING,
                        "enterprise.deployment.backend.optionalpkg.dependency.notexist",
                        new Object[] {extName, archiveUri});
                        dependencySatisfied = false;
            } else if (!specVersion.equals("") &&
                    (optionalPackageStore.get(extName).toString().equals("")) ||
                    !specVersion.equals(optionalPackageStore.get(extName).toString())) {
                logger.log(Level.WARNING,
                        "enterprise.deployment.backend.optionalpkg.dependency.notexist",
                        new Object[] {extName, archiveUri});
                        dependencySatisfied = false;
            }
        }
        if (dependencySatisfied == true) {
            logger.log(Level.INFO,
                    "enterprise.deployment.backend.optionalpkg.dependency.satisfied",
                    new Object[] {archiveUri});
        }
        return dependencySatisfied;
    }

    /**
     * check whether the optional packages have all their 
     * internal dependencies resolved
     */
    public static void satisfyOptionalPackageDependencies() {

	String ext_dirStr = new String(
				       System.getProperty("java.ext.dirs"));
	logger.fine("ext_dirStr..." + ext_dirStr);

	Vector ext_dirs = new Vector();
	StringTokenizer st = new StringTokenizer(ext_dirStr, File.pathSeparator);
	while (st.hasMoreTokens()) {
	    String next = st.nextToken();
	    logger.log(Level.FINE,"string tokens..." + next);
	    ext_dirs.addElement(next);
	}
        
        /*
         *Records the files that are legitimate JARs.
         */
        ArrayList<File> validOptionalPackageFiles = new ArrayList<File>();
        
        for (int v = 0; v < ext_dirs.size(); v++) {
            
            File ext_dir = new File((String)ext_dirs.elementAt(v));
            
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE,"extension dir..." + ext_dir);
            }
            
            
            File[] optionalPackages = ext_dir.listFiles();
            if (optionalPackages != null) {
                try {
                    for (int i = 0; i < optionalPackages.length; i++) {
                        
                        if (logger.isLoggable(Level.FINE)) {
                            logger.log(Level.FINE,"optional package..." + optionalPackages[i]);
                        }
                        /*
                         *Skip any candidate that does not end with .jar or is a
                         *directory.
                         */
                        if (optionalPackages[i].isDirectory()) {
                            logger.log(Level.FINE, 
                                    "Skipping optional package processing on " + 
                                    optionalPackages[i].getAbsolutePath() + 
                                    "; it is a directory");
                            continue;
                        } else if ( ! optionalPackages[i].getName().toLowerCase().endsWith(".jar")) {
                            logger.log(Level.FINE,
                                    "Skipping optional package processing on " +
                                    optionalPackages[i].getAbsolutePath() +
                                    "; it does not appear to be a JAR file based on its file type");
                            continue;
                        }
                        JarFile jarFile = null;
                        try {
                            jarFile = new JarFile(optionalPackages[i]);

                            Manifest manifest = jarFile.getManifest();
                            validOptionalPackageFiles.add(optionalPackages[i]);
                            extDirJars.add(optionalPackages[i].toString());

                            //Extension-Name of optional package
                            if (manifest!=null) {
                                String extNameOfOptionalPkg = manifest.getMainAttributes().
                                        getValue(Attributes.Name.EXTENSION_NAME);
                                String specVersion = manifest.getMainAttributes().
                                        getValue(Attributes.Name.SPECIFICATION_VERSION);
                                logger.fine("Extension " + optionalPackages[i].getAbsolutePath() + 
                                        ", extNameOfOPtionalPkg..." + extNameOfOptionalPkg + 
                                        ", specVersion..." + specVersion);
                                if (extNameOfOptionalPkg != null) {
                                    if (specVersion == null) {
                                        logger.log(Level.WARNING,
                                                "enterprise.tools.deployment.backend.optionalpkg.dependency.specversion.null",
                                                new Object[] {extNameOfOptionalPkg});
                                                specVersion = new String("");
                                    }
                                    optionalPackageStore.put(extNameOfOptionalPkg,
                                            specVersion);

                                }
                            }
                        } catch (Throwable thr) {
                            /*
                             *Log a warning, with stack trace if logging is FINE.
                             */
                            String msg = logger.getResourceBundle().getString("enterprise.deployment.backend.optionalpkg.dependency.error");
                            if (logger.isLoggable(Level.FINE)) {
                                logger.log(Level.FINE, MessageFormat.format(msg, optionalPackages[i].getAbsolutePath(), thr.getMessage()), thr);
                            } else {
                                logger.log(Level.INFO, MessageFormat.format(msg, optionalPackages[i].getAbsolutePath(), thr.getMessage()));
                            }
                        } finally {
                            if (jarFile != null) {
                                jarFile.close();
                            }
                        }
                    }
                    for (File file : validOptionalPackageFiles) {
                        JarFile jarFile = null;
                        try {
                            jarFile = new JarFile(file);
                            Manifest m = jarFile.getManifest();
                            if (m!=null) {
                                optionalPkgDependencyLogic(m, file.getAbsolutePath());
                            }
                        } catch (IOException ioe) {
                            logger.log(Level.WARNING,
                                "enterprise.deployment.backend.optionalpkg.invalid.zip", new Object[] {file.getAbsolutePath(), ioe.getMessage()});
                        }finally {
                            if (jarFile!=null)
                                jarFile.close();
                        }
                    }
                } catch (IOException e) {
                    logger.log(Level.WARNING,
                            "enterprise.deployment.backend.optionalpkg.dependency.exception", new Object[] {e.getMessage()});
                }
            }            
        }
    }

    /**
     * Adds all the jar files in all of the ext dirs into a single string
     * in classpath format.  Returns the empty string if there are no
     * jar files in any ext dirs.
     */
    public static String getExtDirFilesAsClasspath() {
       
        StringBuffer classpath = new StringBuffer();

        for(Iterator iter = extDirJars.iterator(); iter.hasNext();) {
            String next = (String) iter.next();
            if( classpath.length() > 0 ) {
                classpath.append(File.pathSeparator);                
            }
            classpath.append(next);
        }

        return classpath.toString();
    }
    
}
