package com.sun.enterprise.module.maven;

import com.sun.enterprise.module.maven.sc.ScriptCreator;
import static com.sun.enterprise.module.maven.sc.ScriptConstants.*;
import java.io.File;
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
    private String configFile;
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
        dumpProperties(project, log);
        String basedir = project.getBasedir().getAbsolutePath();
        if (configFile == null) {
            throw new MojoExecutionException("Required parameter <configFile> not specified, exiting ...");
        }
        if (destDir == null) {
            throw new MojoExecutionException("Required parameter <destDir> not specified, exiting ...");
        }
        File cf = new File(basedir, configFile);
        File dd = new File(basedir, destDir);
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
