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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.jar.JarArchiver;

import java.io.File;

/**
 * Creates a jar with a special manifest entry.
 *
 * <p>
 * Mostly copied from JarMojo. See http://jira.codehaus.org/browse/MNG-2789
 * why we can't rely on it.
 *
 * @author Kohsuke Kawaguchi
 * @goal package
 * @phase package
 * @requiresProject
 */
public class PackageMojo extends AbstractMojo {
    private static final String[] DEFAULT_EXCLUDES = new String[]{"**/package.html"};

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
            new Packager().configureManifet(project,archive,classesDirectory);

            File contentDirectory = classesDirectory;
            if (!contentDirectory.exists()) {
                getLog().warn("JAR will be empty - no content was marked for inclusion!");
            } else {
                archiver.getArchiver().addDirectory(contentDirectory, DEFAULT_INCLUDES, DEFAULT_EXCLUDES);
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
}
