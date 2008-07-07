/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.enterprise.module.maven;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.jar.JarArchiver;

import java.io.File;
import java.util.Properties;

/**
 * Creates a jar with a special manifest entry.
 *
 * <p>
 * Mostly copied from JarMojo. See http://jira.codehaus.org/browse/MNG-2789
 * why we can't rely on it.
 * </p>
 *
 * @author Kohsuke Kawaguchi
 * @goal package
 * @phase package
 * @requiresProject
 */
public class PackageMojo extends AbstractMojo {
    private static final String[] DEFAULT_INCLUDES = new String[]{"**/**"};

    /**
     * Directory containing the generated JAR.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    protected File outputDirectory;

    /**
     * Name of the generated JAR.
     *
     * @parameter alias="jarName" expression="${project.build.finalName}"
     * @required
     */
    protected String finalName;

    /**
     * The Jar archiver.
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#jar}"
     * @required
     */
    protected JarArchiver jarArchiver;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The maven archive configuration to use.
     * <p/>
     * See <a href="http://maven.apache.org/ref/current/maven-archiver/apidocs/org/apache/maven/archiver/MavenArchiveConfiguration.html">the Javadocs for MavenArchiveConfiguration</a>.
     *
     * @parameter
     */
    protected MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    /**
     * @component
     */
    protected MavenProjectHelper projectHelper;

    /**
     * Directory containing the classes.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    protected File classesDirectory;

    /**
     * Ant glob patterns to be excluded from the jar file, like "**<span></span>/*.bak"
     * The base directory for the pattern is specified in {@link #classesDirectory}.
     * 
     * @parameter
     */
    protected String[] excludes;

    /**
     * By default, we don't generate OSGi manifest 
     * @parameter default-value = false
     */
    private boolean generateOSGiHeaders;

    /**
     * Whether to generate optional dependency or mandatory dependency
     * @parameter default-value = "mandatory"
     */
    private String resolution;

    /**
     * Whether to reexport or not
     * @parameter default-value = "private"
     */
    private String visibility;

    protected final MavenProject getProject() {
        return project;
    }

    protected static File getJarFile(File basedir, String finalName) {
        return new File(basedir, finalName + ".jar");
    }

    /**
     * Generates the JAR.
     *
     * @todo Add license files in META-INF directory.
     */
    public File createArchive()
        throws MojoExecutionException {
        File jarFile = getJarFile(outputDirectory, finalName);

        MavenArchiver archiver = new MavenArchiver();

        archiver.setArchiver(jarArchiver);

        archiver.setOutputFile(jarFile);

        try {
            new Packager().configureManifest(project,archive,classesDirectory);

            if (generateOSGiHeaders) {
                Properties props = new Properties();
                props.put("resolution", resolution);
                props.put("visibility", visibility);
                new OSGiPackager(props).configureOSGiManifest(project,archive,classesDirectory);
            }

            File contentDirectory = classesDirectory;
            if (!contentDirectory.exists()) {
                getLog().warn("JAR will be empty - no content was marked for inclusion!");
            } else {
                archiver.getArchiver().addDirectory(contentDirectory, DEFAULT_INCLUDES, getExcludes());
            }

            archiver.createArchive(project, archive);

            return jarFile;
        } catch (Exception e) {
            // TODO: improve error handling
            throw new MojoExecutionException("Error assembling JAR", e);
        }
    }

    /**
     * Generates the JAR.
     *
     * @todo Add license files in META-INF directory.
     */
    public void execute() throws MojoExecutionException {
        File jarFile = createArchive();

        getProject().getArtifact().setFile(jarFile);
    }

    private String[] getExcludes() {
        if (excludes != null && excludes.length > 0)
            return excludes;
        // default
        return new String[]{"**/package.html"};
    }
}
