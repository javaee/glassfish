package com.sun.enterprise.build;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.maven.MavenProjectRepository;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executes Glassfish by the current module (and all the other modules needed
 * for a particular distribution of GF.)
 *
 * @goal run
 * @phase compile
 * @requiresProject
 * @requiresDependencyResolution runtime
 * @aggregator
 *
 * @author Kohsuke Kawaguchi
 */
public class RunMojo extends DistributionAssemblyMojo {
    /**
     * Distribution of Glassfish to be used as a basis.
     *
     * @parameter
     * @required
     */
    protected ArtifactInfo distribution;

    /**
     * @component
     */
    protected ArtifactResolver artifactResolver;

    /**
     * @component
     */
    protected ArtifactFactory artifactFactory;

    /**
     * @component
     */
    protected MavenProjectBuilder projectBuilder;

    /**
     * @component
     */
    protected ArtifactMetadataSource artifactMetadataSource;

    /**
     * @parameter expression="${localRepository}"
     * @required
     */
    protected ArtifactRepository localRepository;

    /**
     * The root directory of the launched module system.
     *
     * If unspecified, the base installation image from {@link #distribution}
     * will be used.
     *
     * @parameter expression="${glassfish.home}"
     */
    private File rootDir;

    /**
     * Command-line options to be passed to {@link StartupContext}.
     *
     * @parameter
     */
    private String[] args = new String[0];

    /**
     * @parameter expression="${session}"
     */
    private MavenSession session;

    public void execute() throws MojoExecutionException, MojoFailureException {
        configLogger();
        
        Artifact dist;
        try {
            dist = distribution.toArtifact(artifactFactory);
            artifactResolver.resolve(dist,
                project.getRemoteArtifactRepositories(), localRepository);
        } catch (ArtifactResolutionException e1) {
            throw new MojoExecutionException("Error attempting to download the distribution POM", e1);
        } catch (ArtifactNotFoundException e11) {
            throw new MojoExecutionException("Distribution POM not found", e11);
        }

        MavenProject distPom;
        try {
            distPom = projectBuilder.buildFromRepository(dist, project.getRemoteArtifactRepositories(), localRepository);
            distPom.setFile(dist.getFile()); // maven doesn't seem to set this. shouldn't it?
        } catch (ProjectBuildingException e12) {
            throw new MojoExecutionException("Unable to parse distribution POM", e12);
        }

        // resolve transitive dependencies
        try {
            if (distPom.getDependencyArtifacts() == null )
                distPom.setDependencyArtifacts( distPom.createArtifacts( artifactFactory, null, null ) );

            ArtifactResolutionResult result = artifactResolver.resolveTransitively(
                distPom.getDependencyArtifacts(),
                dist, localRepository,
                project.getRemoteArtifactRepositories(),
                artifactMetadataSource, new ScopeArtifactFilter("runtime") );
            distPom.setArtifacts( result.getArtifacts() );

            // download any missing artifacts
            for (Object a : distPom.getArtifacts()) {
                artifactResolver.resolve( (Artifact)a, project.getRemoteArtifactRepositories(), localRepository );
            }
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("Failed to resolve dependencies of distribution POM",e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException("Failed to resolve dependencies of distribution POM",e);
        } catch (InvalidDependencyVersionException e) {
            throw new MojoExecutionException("Failed to resolve dependencies of distribution POM",e);
        }

        // do we have rootDir ?
        if(rootDir==null) {
            // where's the base installation image?
            Artifact baseImage = findBaseImage(distPom);
            rootDir = new File(new File(session.getExecutionRootDirectory()),"target/glassfish");
            if(!rootDir.exists()) {
                getLog().info(
                    String.format("Extracting %1s to %2s as the installation base image",
                        baseImage.getFile(), rootDir));
                rootDir.mkdirs();

                try {
                    Expand exp = new Expand();
                    exp.setProject(new Project());
                    exp.setSrc(baseImage.getFile());
                    exp.setDest(rootDir.getParentFile());
                    exp.execute();
                } catch (BuildException e) {
                    throw new MojoExecutionException("Failed to extract "+baseImage.getFile());
                }
            } else {
                getLog().info("Using existing glassfish installation image at "+rootDir);
            }
        }

        assert rootDir!=null;

        try {
            // Glassfish wants $GF_HOME/lib as the bootstrap directory
            new Main().launch(createModuleRegistry(distPom),new File(rootDir,"lib"),args);

            // TODO: what's the orderly shutdown sequence of Glassfish?
            // block forever for now.
            Object x = new Object();
            synchronized(x) {
                x.wait();
            }

        } catch (BootException e) {
            throw new MojoExecutionException("Failed to boot up the module system",e);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to boot up the module system",e);
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Failed to boot up the module system",e);
        }
    }

    /**
     * Finds the base installation image artifact from the distribution POM,
     * or throw an exception if fails.
     */
    private Artifact findBaseImage(MavenProject dist) throws MojoExecutionException {
        for (Object o : dist.getArtifacts()) {
            Artifact a = (Artifact) o;
            String type = a.getType();
            if(type!=null && type.equals("zip"))
                return a;
        }

        throw new MojoExecutionException("No base image found for "+dist.getFile());
    }

    /**
     * Creates a fully configured module registry.
     */
    protected ModulesRegistry createModuleRegistry(MavenProject root) throws IOException {
        ModulesRegistry r = ModulesRegistry.createRegistry();
        r.setParentClassLoader(this.getClass().getClassLoader());
        MavenProjectRepository lib = new MavenProjectRepository(root,artifactResolver,localRepository,artifactFactory);
        r.addRepository(lib);
        lib.initialize();
        return r;
    }

    /**
     * Added logging configuration.
     */
    private void configLogger() {
        Properties props = System.getProperties();
        for (Entry<Object,Object> e : props.entrySet()) {
            String key = e.getKey().toString();

            if(key.startsWith("logging.")) {
                Level value = Level.parse(e.getValue().toString());

                key = key.substring(8);
                Logger logger = Logger.getLogger(key);
                logger.setLevel(value);

                // the default root ConsoleHandler only logs messages above INFO,
                // so if we want to log more detailed levels, we need to install
                // a separate handler.
                if(value.intValue() < Level.INFO.intValue()) {
                    ConsoleHandler h = new ConsoleHandler();
                    h.setLevel(value);
                    logger.addHandler(h);
                }
            }
        }
    }
}
