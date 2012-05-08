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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jvnet.hk2.config.generator.AnnotationProcessorFactoryImpl;

import com.sun.tools.apt.Main;

/**
 * @author jwells
 * 
 * @goal generateInjectors
 * @phase generate-sources
 */
public class ConfigGeneratorPlugin extends AbstractMojo {
    private final static String GENERATED_SOURCES = "generated-sources";
    private final static String APT_GENERATED = "hk2-config-generator";
    /**
     * @parameter expression="${basedir}"
     */
    private String basedir;
    
    /**
     * @parameter expression="${project.build.directory}"
     */
    private String buildDirectory;
    
    /**
     * @parameter
     */
    private String classpath;
    
    private void getAllJavaFiles(File directory, final LinkedList<String> foundFiles) {
        File allJavaFiles[] = directory.listFiles(new FileFilter() {

            @Override
            public boolean accept(File name) {
                if (name.isDirectory()) {
                    getAllJavaFiles(name, foundFiles);
                    return false;
                }
                
                if (name.getName().endsWith(".java")) {
                    return true;
                }
                
                return false;
            }
            
        });
        
        for (File aJavaFile : allJavaFiles) {
            foundFiles.add(aJavaFile.getAbsolutePath());
        }
        
    }
    
    private boolean getAllSources(LinkedList<String> args) throws MojoFailureException {
        System.out.println("JRW(10) baseDir=" + basedir);
        File dotMe = new File(basedir);
        File src = new File(dotMe, "src");
        File main = new File(src, "main");
        File java = new File(main, "java");
        
        if (!java.exists() && !java.isDirectory()) {
            return false;
        }
        
        getAllJavaFiles(java, args);
        
        return true;
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
            
            return line;
        }
        catch (IOException ioe) {
            throw new MojoFailureException(ioe.getMessage());
        }
        
    }
    
    private void generateMinusSOption(LinkedList<String> args) {
        File buildDirectoryFile = new File(buildDirectory);
        File generatedSources = new File(buildDirectoryFile, GENERATED_SOURCES);
        File aptGeneratedFile = new File(generatedSources, APT_GENERATED);
        
        aptGeneratedFile.mkdirs();
        
        args.add("-s");
        args.add(aptGeneratedFile.getAbsolutePath());
    }
    
    private void generateMinusCPOption(LinkedList<String> args) throws MojoFailureException {
        if (classpath == null) throw new MojoFailureException("classpath argument may not be null");
        args.add("-cp");
        args.add(getClasspathFromFile());
    }
    
    /* (non-Javadoc)
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    private void internalExecute() throws Throwable {
        LinkedList<String> args = new LinkedList<String>();
        
        args.add("-nocompile");
        
        generateMinusSOption(args);
        generateMinusCPOption(args);
        if (!getAllSources(args)) return;  // No src/main/java
        
        String[] cmdLine = args.toArray(new String[args.size()]);
        Main.process(new AnnotationProcessorFactoryImpl(), cmdLine);
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
                System.out.println("Exception from hk2-config-generator[" + lcv++ + "]=" + cause.getMessage());
                cause.printStackTrace();
                
                cause = cause.getCause();
            }
            
            throw new MojoExecutionException(th.getMessage(), th);
        }
    }

}
