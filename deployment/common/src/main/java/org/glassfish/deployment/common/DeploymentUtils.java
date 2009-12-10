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

package org.glassfish.deployment.common;

import com.sun.enterprise.util.io.FileUtils;
import org.glassfish.api.deployment.archive.ReadableArchive;
import com.sun.enterprise.deployment.deploy.shared.Util;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.internal.data.ApplicationRegistry;
import org.jvnet.hk2.component.Habitat;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.*;


/** 
 * Utility methods for deployment. 
 */

public class DeploymentUtils {

    private static final String WEB_INF = "WEB-INF";
    private static final String JSP_SUFFIX = ".jsp";
    private static final String RA_XML = "META-INF/ra.xml";
    private static final String APPLICATION_XML = "META-INF/application.xml";    
    private static final String EAR_EXTENSION = ".ear";
    private static final String WAR_EXTENSION = ".war";
    private static final String RAR_EXTENSION = ".rar";
    private static final String EXPANDED_WAR_SUFFIX = "_war";
    private static final String EXPANDED_RAR_SUFFIX = "_rar";
    private static final String EXPANDED_JAR_SUFFIX = "_jar";
    
    // checking whether the archive is a web archive
    public static boolean isWebArchive(ReadableArchive archive) {
        try {
            if (Util.getURIName(archive.getURI()).endsWith(WAR_EXTENSION)) {
                return true;
            }

            if (archive.exists(WEB_INF)) { 
                return true;
            } 
            Enumeration<String> entries = archive.entries();
            while (entries.hasMoreElements()) {
                String entryName = entries.nextElement();
                if (entryName.endsWith(JSP_SUFFIX)) { 
                    return true;
                }
            }
            return false;
        } catch (IOException ioe) {
            // ignore
        }
        return false;
    }

   /**
     * @param pathName
     * @return the default value of the EE name. 
     * The default name is the pathname with any filename extension 
     * (.jar, .war, .rar) removed, but with any directory names included.
     */
    public static String getDefaultEEName(String pathName) {
        if (pathName == null) {
            return null;
        }

        pathName = pathName.replace('\\', '/');

        if (pathName.endsWith("/")) {
            pathName = pathName.substring(0, pathName.length() - 1);
        }
        if (pathName.lastIndexOf("/") != -1) {
            pathName = pathName.substring(pathName.lastIndexOf(
                "/") + 1);
        }
        if (pathName.endsWith(".jar") || pathName.endsWith(".war")
            || pathName.endsWith(".rar") || pathName.endsWith(".ear")) {
            return pathName.substring(0, pathName.length() - 4);
        } else {
            return pathName;
        }
    }
    /**
     * check whether the archive is a .rar
     * @param archive archive to be tested
     * @return status of .rar or not
     */
    public static boolean isRAR(ReadableArchive archive){
        boolean isRar = false;
        try{
            if (Util.getURIName(archive.getURI()).endsWith(RAR_EXTENSION)) {
                return true;
            }

            isRar = archive.exists(RA_XML);
        }catch(IOException ioe){
            //ignore
        }
        return isRar;
    }

    /**
     * check whether the archive is a .ear
     * @param archive archive to be tested
     * @return status of .ear or not
     */
    public static boolean isEAR(ReadableArchive archive){
        boolean isEar = false;

        try{
            if (Util.getURIName(archive.getURI()).endsWith(EAR_EXTENSION)) {
                return true;
            }

            isEar = archive.exists(APPLICATION_XML);

            if (!isEar) {
                isEar = isEARFromIntrospecting(archive);
            } 
        }catch(IOException ioe){
            //ignore
        }
        return isEar;
    }

    // introspecting the sub archives to see if any of them 
    // ended with expected suffix
    private static boolean isEARFromIntrospecting(ReadableArchive archive) 
        throws IOException {
        for (String entryName : archive.getDirectories()) {
            // we don't have other choices but to look if any of
            // the subdirectories is ended with expected suffix
            if ( entryName.endsWith(EXPANDED_WAR_SUFFIX) || 
                 entryName.endsWith(EXPANDED_RAR_SUFFIX) || 
                 entryName.endsWith(EXPANDED_JAR_SUFFIX) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method returns the relative file path of an embedded module to 
     * the application root.
     * For example, if the module is expanded/located at 
     * $domain_dir/applications/j2ee-apps/foo/fooEJB_jar,
     * this method will return fooEJB_jar
     *
     *@param appRootPath The path of the application root which
     *                   contains the module 
     *                   e.g. $domain_dir/applications/j2ee-apps/foo
     *@param moduleUri The module uri
     *                 e.g. fooEJB.jar
     *@return The relative file path of the module to the application root
     */
    public static String getRelativeEmbeddedModulePath(String appRootPath,
        String moduleUri) {
        moduleUri = FileUtils.makeLegalNoBlankFileName(moduleUri);
        if (FileUtils.safeIsDirectory(new File(appRootPath, moduleUri))) {
            return moduleUri;
        } else {
            return FileUtils.makeFriendlyFilenameExtension(moduleUri);
        }
    }

    /**
     * This method returns the file path of an embedded module. 
     * For example, if the module is expanded/located at 
     * $domain_dir/applications/j2ee-apps/foo/fooEJB_jar,
     * this method will return 
     * $domain_dir/applications/j2ee-apps/foo/fooEJB_jar
     *
     *@param appRootPath The path of the application root which
     *                   contains the module 
     *                   e.g. $domain_dir/applications/j2ee-apps/foo
     *@param moduleUri The module uri
     *                 e.g. fooEJB.jar
     *@return The file path of the module
     */
    public static String getEmbeddedModulePath(String appRootPath,
        String moduleUri) {
        return appRootPath + File.separator + getRelativeEmbeddedModulePath(appRootPath, moduleUri) ;
    }

}
