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
package org.jvnet.hk2.generator.maven;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.jvnet.hk2.generator.HabitatGenerator;

/**
 * Calls the generator with maven
 * 
 * @goal generateInhabitants
 * @phase process-classes
 */
public class MavenInhabitantsGenerator extends AbstractMojo {
    /**
     * @parameter expression="${project.build.outputDirectory}"
     */
    private String outputDirectory;
    
    /**
     * @parameter expression="${project.build.testOutputDirectory}"
     */
    private String testOutputDirectory;
    
    /**
     * @parameter
     */
    private boolean verbose;
    
    /**
     * @parameter
     */
    private String locator;
    
    /**
     * @parameter
     */
    private boolean test = false;
    
    /**
     * @parameter
     */
    private String classpath;
    
    /**
     * @parameter
     */
    private boolean noswap;

    /**
     * This method will compile the inhabitants file based on
     * the classes just compiled
     */
    public void execute() throws MojoFailureException {
        File output;
        if (!test) {
            output = new File(outputDirectory);
        }
        else {
            output = new File(testOutputDirectory);
        }
        
        if (!output.exists()) {
            if (verbose) {
                System.out.println("Exiting hk2-inhabitant-generator because could not find output directory " +
                  output.getAbsolutePath());
            }
            return;
        }
        
        if (verbose) {
            System.out.println("hk2-inhabitant-generator generating into location " + output.getAbsolutePath());
        }
        
        LinkedList<String> arguments = new LinkedList<String>();
        
        arguments.add(HabitatGenerator.FILE_ARG);
        arguments.add(output.getAbsolutePath());
        
        if (verbose) {
            arguments.add(HabitatGenerator.VERBOSE_ARG);
        }
        
        if (locator != null) {
            arguments.add(HabitatGenerator.LOCATOR_ARG);
            arguments.add(locator);
        }
        
        if (classpath != null) {
            String classpathValue = getClasspathFromFile();
            if (classpathValue == null) {
                throw new MojoFailureException("Found the file, but it did not contain a line with the classpath");
            }
            
            arguments.add(HabitatGenerator.SEARCHPATH_ARG);
            arguments.add(classpathValue);
        }
        
        if (noswap) {
            arguments.add(HabitatGenerator.NOSWAP_ARG);
        }
        
        String argv[] = arguments.toArray(new String[arguments.size()]);
        
        int result = HabitatGenerator.embeddedMain(argv);
        
        if (result != 0) {
            throw new MojoFailureException("Could not generate inhabitants file for " +
                (test ? testOutputDirectory : outputDirectory));
        }
    }
    
    private String getClasspathFromFile() throws MojoFailureException {
        File classpathFile = new File(classpath);
        if (!classpathFile.exists() || classpathFile.isDirectory() || !classpathFile.canRead()) {
            throw new MojoFailureException("Could not find or read file " + classpathFile.getAbsolutePath());
        }
        
        try {
            InputStream is = new FileInputStream(classpathFile);
            Reader reader = new InputStreamReader(is);
            BufferedReader bis = new BufferedReader(reader);
            
            String line = bis.readLine();
            
            bis.close();
            reader.close();
            is.close();
            
            if (test) {
                File buildDirectoryFile = new File(outputDirectory);
                
                // Make sure to add in the directory that has been built
                return buildDirectoryFile.getAbsolutePath() + File.pathSeparator + line;
            }
            
            return line;
        }
        catch (IOException ioe) {
            throw new MojoFailureException(ioe.getMessage());
        }
        
    }
}
