/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.generator.ant;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.jvnet.hk2.generator.HabitatGenerator;

/**
 * @author jwells
 *
 */
public class HK2InhabitantGeneratorTask extends Task {
    private File targetDirectory = new File("target/classes");
    private boolean verbose = false;
    private String locator = null;
    private File outputDirectory = null;
    private boolean noswap = false;
    private Path classpath = null;
    private boolean includeDate = true;
    
    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }
    
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    public void setLocator(String locator) {
        this.locator = locator;
    }
    
    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
    
    public void setNoSwap(boolean noswap) {
        this.noswap = noswap;
    }
    
    public void setIncludeDate(boolean includeDate) {
        this.includeDate = includeDate;
    }
    
    public void addClasspath(Path classpath) {
        this.classpath = classpath;
    }
    
    public void execute() throws BuildException {
        List<String> args = new LinkedList<String>();
        
        if (targetDirectory != null) {
            if (!targetDirectory.isDirectory()) {
                throw new BuildException("targetDirectory " + targetDirectory.getAbsolutePath() +
                        " must point to the directory where the built classes reside");
            }
            
            args.add(HabitatGenerator.FILE_ARG);
            args.add(targetDirectory.getAbsolutePath());
        }
        
        if (verbose) {
            args.add(HabitatGenerator.VERBOSE_ARG);
        }
        
        if (!includeDate) {
            args.add(HabitatGenerator.NO_DATE_ARG);
        }
        
        if (locator != null) {
            args.add(HabitatGenerator.LOCATOR_ARG);
            args.add(locator);
        }
        
        if (outputDirectory != null) {
            if (!outputDirectory.isDirectory()) {
                if (outputDirectory.exists()) {
                    throw new BuildException("outputDirectory " + outputDirectory.getAbsolutePath() +
                            " exists and is not a directory");
                }
                
                if (!outputDirectory.mkdirs()) {
                    throw new BuildException("Could not create directory " + outputDirectory.getAbsolutePath());
                }
            }
            
            args.add(HabitatGenerator.DIRECTORY_ARG);
            args.add(outputDirectory.getAbsolutePath());
        }
        
        if (noswap) {
            args.add(HabitatGenerator.NOSWAP_ARG);
        }
        
        if (classpath != null) {
            args.add(HabitatGenerator.SEARCHPATH_ARG);
            args.add(classpath.toString());
        }
        
        String argv[] = args.toArray(new String[args.size()]);
        
        int result = HabitatGenerator.embeddedMain(argv);
        if (result != 0) {
            throw new BuildException("Could not generate inhabitants file for " + targetDirectory.getAbsolutePath());
        }
    }

}
