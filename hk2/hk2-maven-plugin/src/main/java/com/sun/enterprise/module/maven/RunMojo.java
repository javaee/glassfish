package com.sun.enterprise.module.maven;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.factory.ArtifactFactory;

import java.io.File;
import java.io.IOException;

/**
 * Launches the module system by using all the dependencies as modules.
 *
 * <p>
 * Not specifying "@execute phase=compile" so that it runs
 * quickly even with a large project.
 * </p>
 * 
 * @requiresProject
 * @goal run
 * @requiresDependencyResolution runtime
 * @phase compile
 *
 * @author Kohsuke Kawaguchi
 */
public class RunMojo extends AbstractMojo {

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

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
     * The root directory of the launched module system.
     *
     * @parameter expression="${rootDir}" default-value="${project.baseDir}"
     */
    private File rootDir;

    /**
     * Command-line options to be passed to {@link StartupContext}.
     *
     * @parameter
     */
    private String[] args = new String[0];

    /**
     * groupId:artifactId of the module which the hk2:run mojo just skips executing.
     *
     * This hack is used to specify the "hk2:run" for a test phase execution
     * in the parent POM for all the child modules (except the parent module itself.) 
     *
     * @parameter expression="${skipId}"
     */
    private String skipId;

    /**
     * groupId:artifactId of the module that includes {@link ModuleStartup}.
     * If omitted, the module list is searched to find one.
     *
     * @parameter expression="${mainModule}"
     */
    private String mainModule;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if(skipId!=null && skipId.equals(project.getGroupId()+":"+project.getArtifactId())) {
            getLog().info("Skipping");
            return;
        }
        
        // sanity check
        if(project.getPackaging().equals("hk2-jar") && project.getArtifact()==null) {
            getLog().warn("This project isn't compiled yet. Perhaps you meant 'mvn compile hk2:run'?");
        }

        try {
            if(mainModule==null)
                new Main().launch(createModuleRegistry(),rootDir,args);
            else
                new Main().launch(createModuleRegistry(),mainModule,rootDir,args);
        } catch (BootException e) {
            throw new MojoExecutionException("Failed to boot up the module system",e);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to boot up the module system",e);
        }
    }

    /**
     * Creates a fully configured module registry.
     */
    protected ModulesRegistry createModuleRegistry() throws IOException {
        ModulesRegistry r = ModulesRegistry.createRegistry();
        r.setParentClassLoader(this.getClass().getClassLoader());
        MavenProjectRepository lib = new MavenProjectRepository(project,artifactResolver,localRepository,artifactFactory);
        r.addRepository(lib);
        lib.initialize();
        return r;
    }

    public static String toModuleName(String groupId,String artifactId) {
        return groupId+':'+artifactId;
    }
}
