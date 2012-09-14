/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.config.generator.maven;

import com.sun.tools.apt.Main;
import java.io.File;
import java.util.*;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.jvnet.hk2.config.generator.AnnotationProcessorFactoryImpl;

/**
 * @author jwells
 * 
 * @goal generateInjectors
 * @phase generate-sources 
 * @requiresDependencyResolution test
 */
public class ConfigGeneratorPlugin extends AbstractMojo {
    private final static String GENERATED_SOURCES = "generated-sources/hk2-config-generator/src";
    private final static String MAIN_NAME = "main";
    private final static String TEST_NAME = "test";
    private final static String JAVA_NAME = "java";

    /**
     * The maven project.
     *
     * @parameter expression="${project}" @required @readonly
     */
    protected MavenProject project;
    
    /**
     * @parameter expression="${project.build.directory}"
     */
    private File buildDirectory;
    
    /**
     * @parameter expression="${project.build.sourceDirectory}"
     */
    private File sourceDirectory;
    
    /**
     * @parameter expression="${project.build.testSourceDirectory}"
     */
    private File testSourceDirectory;
    
    /**
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File outputDirectory;
    
    /**
     * @parameter
     */
    private boolean test;
    
    /**
     * @parameter
     */
    private boolean verbose;
    
    /**
     * @parameter expression="${supportedProjectTypes}" default-value="jar"
     */
    private String supportedProjectTypes;    
    
    /* (non-Javadoc)
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    private void internalExecute() throws Throwable {
        List<String> projectTypes = Arrays.asList(supportedProjectTypes.split(","));
        File srcDir = (test) ? testSourceDirectory : sourceDirectory;
        if(!projectTypes.contains(project.getPackaging())
                || !srcDir.exists() 
                || !srcDir.isDirectory()){
            return;
        }
        
        // prepare the generated source directory
        File generatedSources = new File(buildDirectory, GENERATED_SOURCES);
        File phaseGeneratedFile = (test) ? new File(generatedSources, TEST_NAME) : new File(generatedSources, MAIN_NAME);
        File javaGeneratedFile = new File(phaseGeneratedFile, JAVA_NAME);
        javaGeneratedFile.mkdirs();
        
        // prepare command line arguments
        LinkedList<String> args = new LinkedList<String>();
        args.add("-nocompile");
        args.add("-s");
        args.add(javaGeneratedFile.getAbsolutePath());
        args.add("-cp");
        args.add(getBuildClasspath());
        args.addAll(FileUtils.getFileNames(srcDir, "**/*.java", "",true));
        
        String[] cmdLine = args.toArray(new String[args.size()]);
        if(verbose){
            getLog().info("");
            getLog().info("-- Apt Command Line --");
            getLog().info("");
            getLog().info(Arrays.toString(cmdLine));
            getLog().info("");
        }
        Main.process(new AnnotationProcessorFactoryImpl(), cmdLine);
        
        // make the generated source directory visible for compilation
        if(test){
            project.addTestCompileSourceRoot(javaGeneratedFile.getAbsolutePath());
            if (getLog().isInfoEnabled()) {
                getLog().info("Test Source directory: " + javaGeneratedFile + " added.");
            }            
        } else {
            project.addCompileSourceRoot(javaGeneratedFile.getAbsolutePath());
            if (getLog().isInfoEnabled()) {
                getLog().info("Source directory: " + javaGeneratedFile + " added.");
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
          internalExecute();
        }
        catch (Throwable th) {
            if (th instanceof MojoExecutionException) {
                throw (MojoExecutionException) th;
            }
            if (th instanceof MojoFailureException) {
                throw (MojoFailureException) th;
            }
            
            Throwable cause = th;
            int lcv = 0;
            while (cause != null) {
                getLog().error("Exception from hk2-config-generator[" + lcv++ + "]=" + cause.getMessage());
                cause.printStackTrace();
                
                cause = cause.getCause();
            }
            
            throw new MojoExecutionException(th.getMessage(), th);
        }
    }
    
    private String getBuildClasspath() {
        StringBuilder sb = new StringBuilder();
        // Make sure to add in the directory that has been built
        if (test) {
            sb.append(outputDirectory.getAbsolutePath());
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
