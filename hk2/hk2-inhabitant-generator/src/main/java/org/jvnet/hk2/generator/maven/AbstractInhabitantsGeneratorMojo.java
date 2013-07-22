/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2013 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.generator.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.jvnet.hk2.generator.HabitatGenerator;

/**
 * Abstract Mojo for inhabitant generator
 */
public abstract class AbstractInhabitantsGeneratorMojo extends AbstractMojo {
    private final static String WAR_PACKAGING = "war";
    
    private final static String WEB_INF = "WEB-INF";
    private final static String CLASSES = "classes";
    
    /**
     * @parameter expression="${project.build.directory}"
     */
    private File targetDirectory;
    
    /**
     * The maven project.
     *
     * @parameter expression="${project}" @required @readonly
     */
    protected MavenProject project;
    
    /**
     * @parameter
     */
    private boolean verbose;
    
    /**
     * @parameter default-value=true
     */
    private boolean includeDate = true;
    
    /**
     * @parameter
     */
    private String locator;
    
    /**
     * @parameter expression="${supportedProjectTypes}" default-value="jar,ejb,war"
     */
    private String supportedProjectTypes;
    
    protected abstract boolean getNoSwap();
    protected abstract File getOutputDirectory();
    
    protected boolean isWar() {
       return WAR_PACKAGING.equals(project.getPackaging());
    }
    
    /**
     * This method will compile the inhabitants file based on
     * the classes just compiled
     */
    @Override
    public void execute() throws MojoFailureException {
        List<String> projectTypes = Arrays.asList(supportedProjectTypes.split(","));
        if(!projectTypes.contains(project.getPackaging())) {
            if (verbose) {
                getLog().info("hk2-inhabitant-generator skipping unknown packaging type " + project.getPackaging() +
                        " from known packaging types " + supportedProjectTypes);
            }
            return;
        }
        
        if (!getOutputDirectory().exists()) {
            if (!getOutputDirectory().mkdirs()) {
                getLog().info("Could not create output directory " +
                        getOutputDirectory().getAbsolutePath());
                return;
            }
        }
        
        if (!getOutputDirectory().exists()) {
            getLog().info("Exiting hk2-inhabitant-generator because could not find output directory " +
                  getOutputDirectory().getAbsolutePath());
            return;
        }
        
        if (verbose) {
            getLog().info("");
            getLog().info("hk2-inhabitant-generator generating into location " + getOutputDirectory().getAbsolutePath());
            getLog().info("");
        }
        
        LinkedList<String> arguments = new LinkedList<String>();
        
        arguments.add(HabitatGenerator.FILE_ARG);
        arguments.add(getOutputDirectory().getAbsolutePath());
        
        if (verbose) {
            arguments.add(HabitatGenerator.VERBOSE_ARG);
        }
        
        if (locator != null) {
            arguments.add(HabitatGenerator.LOCATOR_ARG);
            arguments.add(locator);
        }
        
        arguments.add(HabitatGenerator.SEARCHPATH_ARG);
        arguments.add(getBuildClasspath());
        
        if (getNoSwap()) {
            arguments.add(HabitatGenerator.NOSWAP_ARG);
        }
        
        if (!includeDate) {
            arguments.add(HabitatGenerator.NO_DATE_ARG);
        }
        
        if (isWar()) {
            // For WAR files, the hk2-locator files goes under WEB-INF/classes/hk2-locator, not META-INF/hk2-locator
            
            File outDir = new File(targetDirectory, project.getArtifactId());
            outDir = new File(outDir, WEB_INF);
            outDir = new File(outDir, CLASSES);
            outDir = new File(outDir, HabitatGenerator.HK2_LOCATOR);
            
            arguments.add(HabitatGenerator.DIRECTORY_ARG);
            arguments.add(outDir.getAbsolutePath());
        }
        
        String argv[] = arguments.toArray(new String[arguments.size()]);
        
        int result = HabitatGenerator.embeddedMain(argv);
        if (result != 0) {
            throw new MojoFailureException("Could not generate inhabitants file for " + getOutputDirectory());
        }
    }
    
    @SuppressWarnings("unchecked")
    private String getBuildClasspath() {
        StringBuilder sb = new StringBuilder();
        
        sb.append(project.getBuild().getOutputDirectory());
        sb.append(File.pathSeparator);
        
        if (!getOutputDirectory().getAbsolutePath().equals(
                project.getBuild().getOutputDirectory())) {
            
            sb.append(getOutputDirectory().getAbsolutePath());
            sb.append(File.pathSeparator);
        }

        List<Artifact> artList = new ArrayList<Artifact>(project.getArtifacts());
        Iterator<Artifact> i = artList.iterator();
        
        if (i.hasNext()) {
            sb.append(i.next().getFile().getPath());

            while (i.hasNext()) {
                sb.append(File.pathSeparator);
                sb.append(i.next().getFile().getPath());
            }
        }
        
        String classpath = sb.toString();
        if(verbose){
            getLog().info("");
            getLog().info("-- Classpath --");
            getLog().info("");
            getLog().info(classpath);
            getLog().info("");
        }
        return classpath;
    }      
}
