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
package com.sun.enterprise.module.maven;

import com.sun.enterprise.module.maven.sc.ScriptCreator;
import static com.sun.enterprise.module.maven.sc.ScriptConstants.*;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.logging.Log;

/** A Rudimentary Mojo to create scripts for Windows and non-Windows Platforms,
 *  based on an intuitive file in a form similar to registry file.
 * @goal createscripts
 * @author Kedar Mhaswade (km@dev.java.net)
 */
public final class ScriptCreatorMojo extends AbstractMojo {

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /* start: define the parameters */
    /**
     * @parameter
     */
    private String[] configFiles;
    /**
     * @parameter
     */
    private String[] destDirs;
    /**
     * @parameter
     */
    private String destDir;

    /**
     * @parameter default-value=false
     */
    private boolean buildPlatformSpecific;
    
    /* end:   define the parameters */

    private final Log log = getLog();

    public void execute() throws MojoExecutionException {
        //dumpProperties(project, log);
        String basedir = project.getBasedir().getAbsolutePath();
        if (configFiles == null || configFiles.length == 0) {
            throw new MojoExecutionException("Required parameter <configFiles> not specified or no <configFile> specified, exiting ...");
        }
        if (destDir == null && destDirs == null) {
            throw new MojoExecutionException("Either <destDir> or <destDirs> should be specified");
        }
        Map<String, String> sd = new LinkedHashMap<String, String>();
        if (destDir != null) { // this has higher precedence
            for (String file : configFiles) {
                sd.put(file, destDir);
            }
        } else {
            if (destDirs.length != configFiles.length) 
                throw new MojoExecutionException("Number of <configFile>s and <destDir>s should be same");
            for (int i = 0 ; i < configFiles.length ; i ++) {
                sd.put(configFiles[i], destDirs[i]);
            } 
        }
        for (String file : configFiles) {
            File cf = new File(basedir, file);
            File dd = new File(basedir, sd.get(file));
            try {
                Properties env;
                if (buildPlatformSpecific) {
                    env = createBuildPlatformSpecificEnvironment();
                    new ScriptCreator(env).create();
                } else {
                    env = createEnvironment(cf, dd, WINDOWS);
                    new ScriptCreator(env).create();
                    env = createEnvironment(cf, dd, UNIX);
                    new ScriptCreator(env).create();
                }
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage());
            }
        }
    }

    private static void dumpProperties(MavenProject pro, Log log) {
        Properties p = pro.getProperties();
        log.debug("project: " + pro.getName());
        log.debug(p.toString());
    }
    private Properties createBuildPlatformSpecificEnvironment() {
        return (project.getProperties()); //for now ...
    }
    
    private Properties createEnvironment(File src, File destDir, String id) {
        Properties env = project.getProperties();
        env.put(SRC, src.getAbsolutePath());
        if (WINDOWS.equals(id)) {
            env.put(OPERATING_SYSTEM, WINDOWS);
            env.put(SCRIPT_HOME_TOKEN, WINDOWS_SCRIPT_HOME_VALUE);
            String destFileName = src.getName() + WIN_SCRIPT_EXTENSION;
            File destFilePath = new File(destDir, destFileName);
            env.put(DEST, destFilePath.getAbsolutePath());
        } else if (UNIX.equals(id)) {
            env.put(OPERATING_SYSTEM, UNIX);
            env.put(SCRIPT_HOME_TOKEN, UNIX_SCRIPT_HOME_VALUE);
            String destFileName = src.getName();
            File destFilePath = new File(destDir, destFileName);
            env.put(DEST, destFilePath.getAbsolutePath());            
        } else { //defaults to no-platform
        }
        return ( env );
    }
}
