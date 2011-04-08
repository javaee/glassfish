/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Reads GlassFish properties used for building.
 *
 * @goal set-properties
 * @phase initialize
 *
 * @author Jane Young
 */
public class SetPropertiesMojo extends AbstractGlassfishMojo {

    /**
     * The properties files used when reading properties.
     * @parameter
     */
    private File[] files;

    /**
     * Optional paths to properties files to be used.
     * 
     * @parameter
     */
    private String[] filePaths;

    /** 
     * Set to true to not fail build if the file is not found. 
     * First checks if file exists and exits without attempting 
     * to replace anything. Only usable with file parameter.
     * @parameter default-value="true"
     */
    private boolean ignoreMissingFile;

    public void execute() throws MojoExecutionException, MojoFailureException {

        Properties projectProperties = new Properties();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];

            getLog().debug("file = " + file);
            String propFileName = file.getName();
            MavenProject mp = project;
            File baseDir = mp.getBasedir();
            getLog().debug(" propFileName = " + propFileName);
            while (baseDir!=null &&  !new File(baseDir, propFileName).exists()) {
		mp = mp.getParent();
                baseDir = mp.getBasedir();
                getLog().debug("mp.getBasedir() = " + mp.getBasedir());
	    }
            if (baseDir!= null && new File(baseDir, propFileName).exists()) {
                file = new File(baseDir, propFileName);
                try {
                    getLog().debug("Loading property file: " + file);

                    FileInputStream stream = new FileInputStream(file);
                    projectProperties = project.getProperties();

                    try {
                        projectProperties.load(stream);
                    } finally {
                        if (stream != null) {
                            stream.close();
                        }
                    }
                } catch (IOException e) {
                    throw new MojoExecutionException(
                            "Error reading properties file "
			    + file.getAbsolutePath(), e);
                }
            }
            else {
                if (ignoreMissingFile) {
                    getLog().warn(
                            "Ignoring missing properties file: "
			    + file.getAbsolutePath());
                } else {
                    throw new MojoExecutionException(
                            "Properties file not found: "
			    + file.getAbsolutePath());
                }
            }
        }
    }
}
