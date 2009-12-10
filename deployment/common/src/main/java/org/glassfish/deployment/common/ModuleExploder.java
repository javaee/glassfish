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

package org.glassfish.deployment.common;


import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.api.deployment.archive.Archive;
import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.enterprise.deploy.shared.FileArchive;
import com.sun.enterprise.util.zip.ZipFile;
import com.sun.enterprise.util.zip.ZipFileException;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.logging.LogDomains;

/**
 * Simple Module exploder
 *
 * @author Jerome Dochez
 *
 */
public class ModuleExploder {

    protected static final StringManager localStrings =
            StringManager.getManager(ModuleExploder.class );

    protected static final Logger logger = LogDomains.getLogger(DeploymentUtils.class, LogDomains.DPL_LOGGER);

    protected static final String PRESERVED_MANIFEST_NAME = java.util.jar.JarFile.MANIFEST_NAME + ".preserved";

    protected static final String WEB_INF_PREFIX = "WEB-INF/";


    public static void explodeJar(File source, File destination) throws IOException {
        JarFile jarFile = null;
        String fileSystemName = null; // declared outside the try block so it's available in the catch block
        try {
            jarFile = new JarFile(source);
            Enumeration<JarEntry> e = jarFile.entries();
            while (e.hasMoreElements()) {
                JarEntry entry = e.nextElement();
                fileSystemName = entry.getName().replace('/', File.separatorChar);
                File out = new File(destination, fileSystemName);

                if (entry.isDirectory()) {
                    out.mkdirs();
                } else {
                    if (!out.getParentFile().exists()) {
                        out.getParentFile().mkdirs();
                    }
                    InputStream is = new BufferedInputStream(jarFile.getInputStream(entry));
                    FileOutputStream fos = FileUtils.openFileOutputStream(out);
                    FileUtils.copy(is, fos, entry.getSize());
                }
            }
        } catch(Throwable e) {
            /*
             *Use the logger here, even though we rethrow the exception.  In
             *at least some cases the caller does not propagate this exception
             *further, instead replacing it with a serializable
             *IASDeployException.  The added information is then lost.
             *By logging the exception here, we make sure the log file at least
             *displays as much as we know about the problem even though the
             *exception sent to the client may not.
             */
            String msg0 = localStrings.getString(
                    "enterprise.deployment.backend.error_expanding",
                    new Object[] {source.getAbsolutePath()});
            String msg = localStrings.getString(
                    "enterprise.deployment.backend.could_not_expand",
                    new Object[] {fileSystemName, destination.getAbsolutePath() });
            IOException ioe = new IOException(msg0);
            ioe.initCause(e);
            logger.log(Level.SEVERE, msg, ioe);
            throw ioe;
        } finally {
            if (jarFile != null) {
                jarFile.close();
            }
        }
    }
    

    public static void explodeModule(Archive source, File directory, boolean preserveManifest)
    throws IOException, DeploymentException {

        File explodedManifest = null;
        File preservedManifestFromArchive = null;

        FileArchive target = new FileArchive();
        target.create(directory.toURI());

        explodeJar(new File(source.getURI()), directory);

        if (preserveManifest) {
            explodedManifest = new File(directory, java.util.jar.JarFile.MANIFEST_NAME);
            if (explodedManifest.exists()) {
                /* Rename the manifest so it can be restored later. */
                preservedManifestFromArchive = new File(directory, PRESERVED_MANIFEST_NAME);
                if ( ! explodedManifest.renameTo(preservedManifestFromArchive)) {
                    throw new RuntimeException(localStrings.getString(
                            "enterprise.deployment.backend.error_saving_manifest",
                            new Object[]
                    { explodedManifest.getAbsolutePath(),
                              preservedManifestFromArchive.getAbsolutePath()
                    } ) ) ;
                }
            }
        }
        // now explode all top level jar files and delete them.
        // this cannot be done before since the optionalPkgDependency
        // require access to the manifest file of each .jar file.
        for (Enumeration itr = source.entries();itr.hasMoreElements();) {
            String fileName = (String) itr.nextElement();


            // check for optional packages depencies
            // XXX : JEROME look if this is still done
            // resolveDependencies(new File(directory, fileName));

             /*
              *Expand the file only if it is a jar and only if it does not lie in WEB-INF/lib.
              */
            if (fileName.toLowerCase().endsWith(".jar") && ( ! fileName.replace('\\', '/').toUpperCase().startsWith(WEB_INF_PREFIX)) ) {

                try {
                    File f = new File(directory, fileName);

                    ZipFile zip = new ZipFile(f, directory);
                    zip.explode();
                } catch(ZipFileException e) {
                    IOException ioe = new IOException(e.getMessage());
                    ioe.initCause(e);
                    throw ioe;
                }
            }
        }
         /*
          *If the archive's manifest was renamed to protect it from being overwritten by manifests from
          *jar files, then rename it back.  Delete an existing manifest file first if needed.
          */
        if (preservedManifestFromArchive != null) {
            if (explodedManifest.exists()) {
                if ( ! explodedManifest.delete()) {
                    throw new RuntimeException(localStrings.getString(
                            "enterprise.deployment.backend.error_deleting_manifest",
                            new Object []
                    { explodedManifest.getAbsolutePath(),
                              preservedManifestFromArchive.getAbsolutePath()
                    }
                    ) );
                }
            }

            if ( ! preservedManifestFromArchive.renameTo(explodedManifest)) {
                throw new RuntimeException(localStrings.getString(
                        "enterprise.deployment.backend.error_restoring_manifest",
                        new Object []
                { preservedManifestFromArchive.getAbsolutePath(),
                          explodedManifest.getAbsolutePath()
                }
                ) );
            }
        }

        source.close();
        target.close();
    }
}
