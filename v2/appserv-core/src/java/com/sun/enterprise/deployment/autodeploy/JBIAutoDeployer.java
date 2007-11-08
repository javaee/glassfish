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

package com.sun.enterprise.deployment.autodeploy;

import java.io.File;
import java.io.FilenameFilter;
import java.util.IdentityHashMap;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.xml.sax.InputSource;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.jar.JarFile;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * This is a singleton that controls autodeployment of 
 * JBI service assemblies. It hold another utility
 * class JBIDeployer, which does all the mbean operations.
 *
 * One difference between JBI archives and Java EE archives
 * is that JBI service assembly conatin a deployment descriptor
 * where the name of the service assembly can be specified.
 * During deployment of the service assembly, this name will
 * be registered as the identifier of the service assembly.
 * For any further activity on the service assembly, this 
 * identifier will be used to refer the service assembly.
 * 
 * For all house keeping within the autodeploy framework,
 * .autodeploystatus directory is used to keep the status
 * files. Also, the java ee archive file name is used
 * as the status file name. But, for undeploying the JBI
 * archive, service assembly name need to be saved.
 * 
 * A new status directory (.jbi) is created inside the
 * .autodeploystatus directory. Status files with the name
 * jbiarchivefilename__serviceassemblyname will be created
 * in this directory during deployment and the same will be
 * removed during undeployment.
 *
 * @author binod@dev.java.net
 */
public class JBIAutoDeployer {

    private static final JBIAutoDeployer jad = new JBIAutoDeployer();

    private static final String JBI_STATUS_DIR = ".jbi";
    private static final String DELIMITER = "__";

    //xpath expression to get the service assembly name in 
    //the domain.xml
    private static final String SA_NAME_PATH =
            "jbi:jbi/jbi:service-assembly/jbi:identification/jbi:name";
    private String JBIXML = "META-INF/jbi.xml";

    private final JBIDeployer deployer;

    private static final Logger sLogger=AutoDeployControllerImpl.sLogger;
    private static final StringManager localStrings =
            StringManager.getManager( AutoDeployer.class );


    private JBIAutoDeployer() {
        deployer = new JBIDeployer();
    }

    static JBIAutoDeployer getInstance() {
        return jad;
    }

    /**
     * Return the object that do the mbean operations.
     */
    JBIDeployer getDeployer() {
        return deployer;
    }

    /**
     * Checks whether it is a JBI archive or not.
     */
    boolean isJbiArchive(File file) throws AutoDeploymentException {
        JarFile jf = null;
        try {
            String name = file.getName();
            String fileType = name.substring(name.lastIndexOf(".") + 1);
            if ("class".equals(fileType)) {
                return false;
            }
            jf = new JarFile(file);
            return jf.getEntry(JBIXML) != null;
        } catch(Exception e) {
            String msg = localStrings.getString
            ("enterprise.deployment.autodeploy.sa_invalid", file);
            sLogger.log(Level.FINE, msg, e);
            return false;
        } finally {
            try {
                if (jf != null) {
                    jf.close();
                }
            } catch (Exception e) {
                // ignore
                e.hashCode();   // silence FindBugs
            }
        }
    }

    /**
     * Return the service assembly name from the archive.
     */
    String getServiceAssemblyName(File file) throws AutoDeploymentException {
        JarFile jf = null;
        try {
            jf = new JarFile(file);
            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(getJbiNSContext());
            String saName = xPath.evaluate(SA_NAME_PATH,
            new InputSource(jf.getInputStream(jf.getEntry(JBIXML))));
            return saName;
        } catch(Exception e) {
            String msg = localStrings.getString
            ("enterprise.deployment.autodeploy.sa_invalid", file);
            sLogger.log(Level.INFO, msg, e);
            throw new AutoDeploymentException(msg, e);
        } finally {
            try {
                jf.close();
            } catch (Exception e) {
            }
        }
    }

    /** Need to handle JBI namespace */
    private NamespaceContext getJbiNSContext() {
        return new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                if (prefix.equals("jbi")) {
                    return "http://java.sun.com/xml/ns/jbi";
                } else {
                    return null;
                }
            }

            public String getPrefix(String uri) {
                return null;
            }

            public java.util.Iterator getPrefixes(String uri) {
                return null;
            }
        };
    }

    /**
     * Return the status file for the JBI archive.
     */
    File getStatusFile(File file, File statDir) {
        File jbiStatDir = new File(statDir, JBI_STATUS_DIR);
        File fStatDir = AutoDeployedFilesManager.obtainFileStatusDir
        (file, jbiStatDir, statDir.getParentFile());

        String saName = null;
        if (file.exists()) {
            try {
                if (isJbiArchive(file)) {
                   saName = getServiceAssemblyName(file);
                }
            } catch (Exception e) {
                sLogger.log(Level.FINE, e.getMessage(), e);
            }
        } else {
            saName = __getServiceAssemblyName(fStatDir, file);
        }

        if (saName == null) return null;

        return new File (fStatDir, file.getName() + DELIMITER + saName);
    }

    /**
     * Get the service assembly name of the deleted file.
     */
    String getServiceAssemblyName(File file, File autodeployDir) {
        try {
            File statDir = 
            new File(autodeployDir, AutoDeployedFilesManager.STATUS_DIR_NAME);
            if (statDir.getParentFile() == null) {
                return null;
            }
            File jbiStatDir = new File(statDir, JBI_STATUS_DIR);
            File fStatDir = AutoDeployedFilesManager.obtainFileStatusDir
            (file, jbiStatDir, statDir.getParentFile());
            return __getServiceAssemblyName(fStatDir, file);
        } catch (Exception e) {
             return null;
             // safe bet. JBI should not cause normal autodeploy to
             // fail.
        }
    }

    /**
     * Split the application name from the status file name and 
     * returns the service assembly name.
     */
    private String __getServiceAssemblyName(File fStatDir, File file) {
        File[] stFiles = fStatDir.listFiles(new ServiceAssemblyFinder(file));
        if (stFiles == null || stFiles.length == 0) {
            return null;
        }
        String[] splitNames = 
        stFiles[0].getName().split(file.getName() + DELIMITER , 2);
        return splitNames[1];
    }

    /**
     * Filters out all status files except for the particular file.
     */
    class ServiceAssemblyFinder implements FilenameFilter {
        File saFile = null;
        ServiceAssemblyFinder(File f) {
            saFile = f;
        }
        public boolean accept(File dir, String name) {
            if (name.startsWith(saFile.getName() + DELIMITER)) {
                return true;
            }
            return false;
        }
    }
}
