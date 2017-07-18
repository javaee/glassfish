/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package com.sun.enterprise.module.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Generates a consolidated OSGI bundle with a consolidated HK2 header
 *
 * @goal hk2-generate
 * @phase prepare-package
 *
 * @requiresProject true
 * @requiresDependencyResolution compile
 * @author Sivakumar Thyagarajan
 */
/* We use prepare-package as the phase as we need to perform this consolidation before the maven-bundle-plugin's bundle goal gets executed in the package phase.*/
public class HK2GenerateMojo extends AbstractMojo {

    private final static String META_INF = "META-INF";
    private final static String HK2_LOCATOR = "hk2-locator";
    private final static String DEFAULT = "default";
    private final static String JAR_ENTRY = "META-INF/hk2-locator/default";
    private final static int BUFFER_SIZE = 4096;
    /**
     * Directory where the manifest will be written
     *
     * @parameter expression="${manifestLocation}"
     * default-value="${project.build.outputDirectory}"
     */
    protected File manifestLocation;
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException {
        Set<Artifact> dependencyArtifacts = project.getDependencyArtifacts();
        if (dependencyArtifacts == null) {
            return;
        }

        try {
            OutputStream catStream = getCatOutputStream();

            // Create the consolidated inhabitant file contents by
            // catting all the dependency artifacts together
            for (Artifact a : (Set<Artifact>) project.getDependencyArtifacts()) {
                if (a.getScope() != null && a.getScope().equals("test")) {
                    continue;
                }
                getLog().info("Dependency Artifact: " + a.getFile().toString());

                JarFile jf = new JarFile(a.getFile());
                JarEntry je = jf.getJarEntry(JAR_ENTRY);
                if (je == null) {
                    continue;
                }

                getLog().debug("Dependency Artifact " + a + " has Inhabitants File: " + je);

                catJarEntry(jf, je, catStream);
            }
        } catch (IOException ioe) {
            throw new MojoExecutionException(ioe.getMessage(), ioe);
        }
    }

    private void catJarEntry(JarFile jf, JarEntry e, OutputStream catStream)
            throws IOException {
        byte buf[] = new byte[BUFFER_SIZE];

        InputStream is = jf.getInputStream(e);
        int readLength;
        while ((readLength = is.read(buf)) > 0) {
            catStream.write(buf, 0, readLength);
        }
    }

    private OutputStream getCatOutputStream() throws MojoExecutionException, IOException {
        String inhabitantsDir = "" + manifestLocation + File.separatorChar
                + META_INF + File.separatorChar + HK2_LOCATOR;

        File inhabitantsDirFile = new File(inhabitantsDir);

        if (inhabitantsDirFile.exists()) {
            if (!inhabitantsDirFile.isDirectory()) {
                throw new MojoExecutionException("File "
                        + inhabitantsDirFile.getAbsolutePath() + " is not a directory");
            }
        } else {
            boolean success = inhabitantsDirFile.mkdirs();
            if (!success) {
                throw new MojoExecutionException("Unable to created directory "
                        + inhabitantsDirFile.getAbsolutePath());
            }
        }

        File defaultFile = new File(inhabitantsDirFile, DEFAULT);
        FileOutputStream fos = new FileOutputStream(defaultFile,true);
        return fos;
    }
}
