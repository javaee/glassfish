/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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


package org.glassfish.osgijpa;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.eclipse.persistence.tools.weaving.jpa.StaticWeaveProcessor;
import org.glassfish.osgiweb.OSGiBundleArchive;
import org.glassfish.osgiweb.OSGiWarHandler;
import org.glassfish.osgiweb.BundleClassLoader;
import org.glassfish.osgiweb.JarHelper;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.internal.api.Globals;

import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.net.URISyntaxException;
import java.io.*;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.util.io.FileUtils;

/**
 * Enhancer for EclipseLink.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
class EclipseLinkEnhancer implements JPAEnhancer {
    private static Logger logger =
            Logger.getLogger(EclipseLinkEnhancer.class.getPackage().getName());

    ArchiveFactory archiveFactory = Globals.get(ArchiveFactory.class);
    private static final String elPackage = "org.eclipse.persistence.*";

    public InputStream enhance(Bundle b, List<String> puRoots) throws IOException {
        // We need to explode the bundle if it is not a directory based deployment.
        // This is because, eclipselink enhancer can only scan file system artifacts.
        File explodedDir = makeFile(b);
        boolean dirDeployment = (explodedDir != null) ? explodedDir.isDirectory() : false;
        try {
            if (!dirDeployment) {
                explodedDir = explode(b);
            }

            // We need to make a copy of the exploded direactory where the enhanced bytes will be written to.
            final File enhancedDir = makeTmpDir("enhanced-osgiapp");
            FileUtils.copyTree(explodedDir, enhancedDir);

            ClassLoader cl = new BundleClassLoader(b);

            for (String puRoot : puRoots) {
                File source = new File(explodedDir, puRoot);
                File target = new File(enhancedDir, puRoot);
                try {
                    enhance(source, target, cl);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                }
            }
            updateManifest(new File(enhancedDir, JarFile.MANIFEST_NAME));
            return JarHelper.makeJar(enhancedDir, new Runnable() {
                public void run() {
                    if (FileUtils.whack(enhancedDir)) {
                        logger.logp(Level.INFO, "EclipseLinkEnhancer", "enhance", "Deleted {0} ", new Object[]{enhancedDir});
                    } else {
                        logger.logp(Level.INFO, "EclipseLinkEnhancer", "enhance", "Unable to delete {0} ", new Object[]{enhancedDir});
                    }
                }
            });
        } finally {
            if (!dirDeployment) {
                if (FileUtils.whack(explodedDir)) {
                    logger.logp(Level.INFO, "EclipseLinkEnhancer", "enhance", "Deleted {0} ", new Object[]{explodedDir});
                } else {
                    logger.logp(Level.WARNING, "EclipseLinkEnhancer", "enhance", "Unable to delete " + explodedDir);
                }
            }
        }
    }

    private void enhance(File source, File target, ClassLoader cl) throws IOException, URISyntaxException {
        logger.logp(Level.INFO, "EclipseLinkEnhancer", "enhance", "Source = {0}, Target = {1}",
                new Object[]{source, target});
        StaticWeaveProcessor proc = new StaticWeaveProcessor(source, target);
        proc.setClassLoader(cl);
        proc.performWeaving();
    }

    private void updateManifest(File mf) throws IOException {
        Manifest m = new Manifest();
        FileInputStream is = new FileInputStream(mf);
        try {
            m.read(is);
        } finally {
            is.close();
        }
        String value = m.getMainAttributes().getValue(Constants.DYNAMICIMPORT_PACKAGE);
        if (value != null) {
            // TODO(Sahoo): Don't add if org.eclipselink.* is already specified
            value = value.concat(", " + elPackage);
        } else {
            value = elPackage;
        }
        m.getMainAttributes().putValue(Constants.DYNAMICIMPORT_PACKAGE, value);

        // Mark the bundle as weaved to avoid infinite updates
        m.getMainAttributes().putValue(JPABundleProcessor.STATICALLY_WEAVED, "true");
        FileOutputStream os = new FileOutputStream(mf);
        try {
            m.write(os);
        } finally {
            os.close();
        }
    }
    /**
     * Creates a temporary directory with the given prefix.
     * It marks the directory for deletion upon shutdown of the JVM.
     *
     * @param prefix
     * @return File representing the directory just created
     * @throws IOException if it fails to create the directory
     */
    public static File makeTmpDir(String prefix) throws IOException {
        File tmpDir = File.createTempFile(prefix, "");

        // create a directory in place of the tmp file.
        tmpDir.delete();
        tmpDir = new File(tmpDir.getAbsolutePath());
        tmpDir.deleteOnExit();
        if (tmpDir.mkdirs()) {
            return tmpDir;
        } else {
            throw new IOException("Not able to create tmpdir " + tmpDir);
        }
    }

    /**
     * Return a File object that corresponds to this bundle.
     * return null if it can't determine the underlying file object.
     *
     * @param b the bundle
     * @return
     */
    public static File makeFile(Bundle b) {
        try {
            return new File(new OSGiBundleArchive(b).getURI());
        } catch (Exception e) {
            // Ignore if we can't convert
        }
        return null;
    }

    private File explode(Bundle b) throws IOException {
        File explodedDir = makeTmpDir("osgiapp");
        WritableArchive targetArchive = archiveFactory.createArchive(explodedDir);
        new OSGiWarHandler().expand(new OSGiBundleArchive(b), targetArchive, null);
        logger.logp(Level.INFO, "EclipseLinkEnhancer", "explode",
                "Exploded bundle {0} at {1} ", new Object[]{b, explodedDir});
        return explodedDir;
    }

}
