/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
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


package com.sun.enterprise.module.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.HashSet;
import java.io.File;
import java.io.IOException;

import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.tools.verifier.hk2.ModuleDependencyAnalyser;

/**
 * Does static analysis of classes in a module to ensure that
 * module dependency is correctly set up.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @goal analyse-dependency
 * @phase verify
 * @requiresDependencyResolution compile
 * @requiresProject
 */
public class DependencyAnalyserMojo extends AbstractMojo {

    private static Logger logger = Logger.getAnonymousLogger();

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The maven artifact.
     *
     * @parameter expression="${project.artifact}"
     * @required
     * @readonly
     */
    protected Artifact artifact;

    /**
     * @component
     */
    private ArtifactResolver artifactResolver;

    /**
     * @parameter expression="${localRepository}"
     */
    private ArtifactRepository localRepository;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

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
     * By default, we just print a warning and proceed.
     * @parameter default-value = false
     */
    private boolean failOnVerificationError;

    /**
     * Patterns excluded from dependency computation.
     * @parameter
     */
    private HashSet<String> excludedPatterns;

    public void execute() throws MojoExecutionException, MojoFailureException {
        // This is a brute force way of skipping executio of this plugin.
        if (Boolean.getBoolean("do-not-analyse-dependency")) {
            logger.logp(Level.INFO, "DependencyAnalyserMojo", "execute", "Skipping analyse-dependency goal");
            return;
        }
        logger.logp(Level.INFO, "DependencyAnalyserMojo", "execute", "Verifying module dependencies");

        if (!project.getPackaging().equals("hk2-jar")) {
            System.out.println("Skipping this project as it does not use hk2-jar packaging");
            return;
        }
        try {
            MavenProjectRepository repo = new MavenProjectRepository(
                    project, artifactResolver, localRepository, artifactFactory);
            repo.initialize();
            File location = artifact.getFile();
            if (location == null) {
                // This can happen if the goal is executed directly outside of verify phase
                location = new File(outputDirectory, finalName + ".jar");
                if (!location.exists()) {
                    throw new MojoExecutionException(location + " does not exist, so can't execute. Package the artifact first.");
                }
            }
            ModuleDefinition moduleDef = new MavenModuleDefinition(repo, location);
            ModuleDependencyAnalyser analyser = new ModuleDependencyAnalyser(moduleDef, repo);
            if (excludedPatterns!=null) {
                analyser.excludePatterns(excludedPatterns);
            }
            if (!analyser.analyse()) {
                String msg = "Missing dependency. See details below:\n" + analyser.getResultAsString();
                if (failOnVerificationError) {
                    throw new MojoExecutionException(msg);
                } else {
                    logger.logp(Level.WARNING, "DependencyAnalyserMojo", "execute", msg);
                }
            } else {
                logger.logp(Level.INFO, "DependencyAnalyserMojo", "execute", "Dependency correctly set up");
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unexpected exception", e);
        }
    }
}
