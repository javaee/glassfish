package com.sun.enterprise.module.maven;

import com.sun.enterprise.module.ManifestConstants;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.Repository;
import com.sun.enterprise.module.common_impl.AbstractRepositoryImpl;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link Repository} implementation that loads modules
 * from the transitive dependencies set in a {@link MavenProject}.
 *
 * @author Kohsuke Kawaguchi
 */
public class MavenProjectRepository extends AbstractRepositoryImpl {

    private final MavenProject project;

    // needed to resolve additional artifacts
    private final ArtifactResolver artifactResolver;
    private final ArtifactRepository localRepository;
    private final ArtifactFactory artifactFactory;


    /**
     * All {@link Artifact}s in the transitive dependency list
     * keyed by their {@link Artifact#getId() id}.
     */
    private final Map<String,Artifact> artifacts = new HashMap<String,Artifact>();
    
    public MavenProjectRepository(MavenProject project, ArtifactResolver artifactResolver, ArtifactRepository localRepository, ArtifactFactory artifactFactory) {
        super(project.getName(),project.getFile().toURI());
        this.project = project;
        this.artifactResolver = artifactResolver;
        this.localRepository = localRepository;
        this.artifactFactory = artifactFactory;

        // adds the ones that we already know of.
        Artifact ma = project.getArtifact();
        if(ma.getFile()!=null) {
            // if the 'ma' is the distribution module, it won't have its own output.
            artifacts.put(ma.getId(),ma);
        }

        for (Artifact a : (Set<Artifact>) project.getArtifacts())
            artifacts.put(a.getId(),a);
    }

    /**
     * When creating {@link MavenProjectRepository} from the current project (which is used
     * to launch mvn), and if the compile phase has run yet, then the main artifact is
     * still null.
     *
     * <p>
     * However, it's often convenient to pick up the files that were left in the file system
     * from the previous execution. This method checks this situation and updates {@link MavenProject}
     * accordingly, so that it can be then passed to the constructor of {@link MavenProjectRepository}.
     *
     * <p>
     * Think of this as a pre-processing phase to compensate for the lack of the compile phase
     * invocation.
     */
    public static void prepareProject(MavenProject project) throws IOException {
        Artifact ma = project.getArtifact();
        if(!project.getPackaging().equals("pom") && ma.getFile()==null) {
            File outdir = new File(project.getBuild().getOutputDirectory());
            if(!outdir.exists())
                logger.warning("No output directory "+outdir);
            else
                ma.setFile(outdir);
        }

        if(ma.getFile()!=null) {
            // if the 'ma' is the distribution module, it won't have its own output.
            if(ma.getFile().isDirectory()) {
                // if the main artifact is from target/classes, create META-INF.MF
                new Packager().writeManifest(project,ma.getFile());
            }
        }
    }

    @Override
    protected void loadModuleDefs(Map<String, ModuleDefinition> moduleDefs, List<URI> libraries) throws IOException {

        logger.info("Loading modules list from "+project.getFile());

        MavenModuleDefinition main = buildModule(project.getArtifact(), moduleDefs, libraries);
        if(main!=null) {
            // artifact from the main project, in case those are not compiled yet
            main.addClasspath(new File(project.getBuild().getOutputDirectory()));
            for (Resource res : (List<Resource>)project.getBuild().getResources())
                main.addClasspath(new File(res.getDirectory()));
        }

        for (Artifact a : (List<Artifact>) project.getAttachedArtifacts()) {
            buildModule(a, moduleDefs, libraries);
        }

        for (Artifact a : (Set<Artifact>) project.getArtifacts()) {
            buildModule(a, moduleDefs, libraries);
        }

        if(moduleDefs.isEmpty())
            throw new Error("No modules found");// should this error check be done by the caller of loadModuleDefs?

    }

    private MavenModuleDefinition buildModule(Artifact a, Map<String, ModuleDefinition> moduleDefs, List<URI> libraries)
            throws IOException {

        File jarFile = a.getFile();
        if(jarFile==null || (!jarFile.getName().endsWith(".jar") && !jarFile.isDirectory()))
            // between the compile phase and the package phase, the main artifact is
            // set to the target/classes. allow that to be used as a jar.
            return null;

        MavenModuleDefinition moduleDef = loadJar(jarFile);
        if(moduleDef.getManifest().getMainAttributes().getValue(ManifestConstants.BUNDLE_NAME)==null) {
            // project.getArtifacts() pick up all the transitive dependencies,
            // including to the normal jar files through modules.
            libraries.add(jarFile.toURI());
            return null;
        }

        if(logger.isLoggable(Level.CONFIG))
            logger.config("Adding module "+a.getId()+" trail: "+a.getDependencyTrail());

        moduleDefs.put(moduleDef.getName(), moduleDef);
        return moduleDef;
    }

    /*package*/ File resolveArtifact(String id) throws IOException {
        Artifact artifact = artifacts.get(id);
        if(artifact!=null)  return artifact.getFile();
        
        try {
            Matcher m = ID_PATTERN.matcher(id);
            if(!m.matches())
                throw new IllegalArgumentException("Wrong ID: "+id);
            Artifact a = artifactFactory.createArtifactWithClassifier(
                m.group(1), m.group(2), m.group(5), m.group(3), m.group(4)
            );

            artifactResolver.resolve(a, project.getRemoteArtifactRepositories(), localRepository );
            artifacts.put(a.getId(),a);
            return a.getFile();
        } catch (ArtifactResolutionException e) {
            throw new IOException2("Failed to resolve "+id,e);
        } catch (ArtifactNotFoundException e) {
            throw new IOException2("Failed to resolve "+id,e);
        }
    }

    // the format is 'groupId:artifactId:type(:classifier)?:version
    private static final String TOKEN = "([^:]+)";
    private static final Pattern ID_PATTERN = Pattern.compile(MessageFormat.format("{0}:{0}:{0}(?:\\:{0})?:{0}",TOKEN));


    protected MavenModuleDefinition loadJar(File jar) throws IOException {
        return new MavenModuleDefinition(this,jar);
    }

    private static final Logger logger = Logger.getLogger(MavenProjectRepository.class.getName());
}
