package com.sun.enterprise.module.maven;

import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.common_impl.AbstractFactory;
import com.sun.enterprise.module.ModulesRegistry;
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
 * @execute phase=compile
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
     * groupId:artifactId of the module that includes {@link ModuleStartup}.
     * If omitted, the module list is searched to find one.
     *
     * @parameter expression="${mainModule}"
     */
    private String mainModule;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if(project.getPackaging().equals("pom")) {
            // we want to run "mvn test" from parent module and run tests of all descendants,
            // but there's nothing to test in POM module itself.
            getLog().info("Skipping");
            return;
        }

        // sanity check
        if(project.getPackaging().equals("hk2-jar") && (project.getArtifact()==null || project.getArtifact().getFile()==null)) {
            getLog().warn("This project isn't compiled yet. Perhaps you meant 'mvn compile hk2:run'?");
        }

        if(rootDir==null)
            rootDir = project.getBasedir();
    
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
        ModulesRegistry r = AbstractFactory.getInstance().createModulesRegistry();
        // code in habitat needs to see HK2 classes, but we don't want them to see other classes and libraries
        // that Maven loads into this maven plugin
        r.setParentClassLoader(new MaskingClassLoader(this.getClass().getClassLoader(),"org.jvnet.hk2","com.sun.enterprise"));
        MavenProjectRepository lib = new MavenProjectRepository(project,artifactResolver,localRepository,artifactFactory);
        r.addRepository(lib);
        lib.initialize();
        return r;
    }

    public static String toModuleName(String groupId,String artifactId) {
        return groupId+':'+artifactId;
    }
}
