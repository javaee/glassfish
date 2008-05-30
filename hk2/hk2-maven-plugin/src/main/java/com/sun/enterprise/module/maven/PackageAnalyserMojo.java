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

import com.sun.enterprise.tools.verifier.hk2.PackageAnalyser;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Analyses bundle manifest entries in a repository and
 * generates various statistics like:
 * no. of  bundles, no. of exported packages, package wiring details and
 * split-packages.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @goal analyse-packages
 * @requiresDependencyResolution compile
 * @requiresProject
 */
public class PackageAnalyserMojo extends AbstractMojo {
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

    public void execute() throws MojoExecutionException, MojoFailureException {
        logger.logp(Level.INFO, "PackageAnalyserMojo", "execute", "Analysing modules");
        try {
            MavenProjectRepository repo = new MavenProjectRepository(
                    project, artifactResolver, localRepository, artifactFactory);
            repo.initialize();
            PackageAnalyser analyser = new PackageAnalyser(repo);
            Collection<PackageAnalyser.Wire> wires = analyser.analyseWirings();
            StringBuilder sb = new StringBuilder("Wiring details are given below:\n");
            for (PackageAnalyser.Wire w : wires) {
                sb.append(w + "\n");
            }
            sb.append("Total number of wires = " + wires.size() + "\n");
            sb.append("Split-Package details are given below:\n");
            Collection<PackageAnalyser.SplitPackage> splitPkgs = analyser.findSplitPackages();
            for (PackageAnalyser.SplitPackage p : splitPkgs) sb.append(p+"\n");
            sb.append("Total number of Split Packages = " + splitPkgs.size() + "\n");

            sb.append("******** GROSS STATISTICS *********\n");
            sb.append("Total number of bundles in this repository: " + analyser.findAllBundles().size()+"\n");
            sb.append("Total number of wires = " + wires.size() + "\n");
            Collection<String> exportedPkgs = analyser.findAllExportedPackages();
            sb.append("Total number of exported packages = " + exportedPkgs.size() + "\n");
            sb.append("Total number of split-packages = " + splitPkgs.size()+"\n");

            logger.logp(Level.INFO, "PackageAnalyserMojo", "execute", "{0}", new Object[]{sb});
            String reportFilePath =
                    System.getProperty("WiringReportPath",
                            System.getProperty("java.io.tmpdir")+ File.separator + "wires.xml");
            analyser.generateWiringReport(exportedPkgs, wires, new PrintStream(new FileOutputStream(new File(reportFilePath))));
            System.out.println("Wiring reported can be found at " + reportFilePath);
        } catch (IOException e) {
            throw new MojoExecutionException("Unexpected exception", e);
        }
    }

}
