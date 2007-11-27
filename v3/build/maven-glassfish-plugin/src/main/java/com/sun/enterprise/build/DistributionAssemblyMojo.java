package com.sun.enterprise.build;

import com.sun.enterprise.module.ManifestConstants;
import com.sun.enterprise.module.impl.Jar;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.ZipFileSet;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;

/**
 * Creates a glassfish distribution image.
 *
 * @goal assemble
 * @phase package
 * @requiresProject
 * @requiresDependencyResolution runtime
 *
 * @author Kohsuke Kawaguchi
 */
public class DistributionAssemblyMojo extends AbstractGlassfishMojo {

    /**
     * The directory where the final image will be created.
     *
     * @parameter expression="${outputDirectory}" default-value="${project.build.directory}"
     */
    protected File outputDirectory;

    /**
     * The file name of the created distribution image.
     *
     * @parameter expression="${finalName}" default-value="${project.build.finalName}.zip"
     */
    protected String finalName;

    public void execute() throws MojoExecutionException, MojoFailureException {

        Set artifacts = project.getArtifacts();

        Set<Artifact> images = findArtifactsOfType(artifacts,"zip");
        Artifact baseImage = findBaseImage(images);

        // find all maven modules
        Set<Artifact> modules = findArtifactsOfScope(artifacts, "runtime");

        outputDirectory.mkdirs();

        // create a zip file
        Zip zip = new Zip();
        zip.setProject(new Project());
        File target = new File(outputDirectory, finalName);
        zip.setDestFile(target);

        // add the base image jar as <zipgroupfileset>
        ZipFileSet zfs = new ZipFileSet();
        zfs.setSrc(baseImage.getFile());
        zfs.setDirMode("755");
        zfs.setFileMode("644"); // work around for http://issues.apache.org/bugzilla/show_bug.cgi?id=42122
        zip.addZipfileset(zfs);

        // then put all modules
        for (Artifact a : modules) {
            zfs = new ZipFileSet();
            zfs.setFile(a.getFile());
            zfs.setPrefix("glassfish/lib");
            zip.addZipfileset(zfs);
        }

        getLog().info("Creating the distribution");
        long time = System.currentTimeMillis();
        zip.execute();
        getLog().info("Packaging took "+(System.currentTimeMillis()-time)+"ms");

        project.getArtifact().setFile(target);
        // normally I shouldn't have to do this. Maven is supposed to pick up
        // the glassfish-distribution artifact handler definition from components.xml
        // and use that.
        // but because of what seems like an ordering issue, I can't get this work.
        // ArtifactHandlerManager just don't get glassfish-distribution ArtifactHandler.
        // so to make this work, I'm overwriting artifact handler here as a work around.
        // This may be somewhat unsafe, as other processing could have already
        // happened with the old incorrect artifact handler, but at least this
        // seems to make the deploy/install phase work.
        project.getArtifact().setArtifactHandler(new DistributionArtifactHandler());
    }

    /**
     * Finds the base image ".zip" file from dependency list.
     *
     * <p>
     * The interesting case is let's say where we are building pe, in which
     * case we see both pe-base and nucleus-base (through nucleus.)
     * So we look for one with the shortest dependency path. 
     */
    private Artifact findBaseImage(Set<Artifact> images) throws MojoExecutionException {
        if(images.isEmpty())
            throw new MojoExecutionException("No base image zip dependency is given");

        Set<Artifact> shortest = new HashSet<Artifact>();
        int shortestLen = Integer.MAX_VALUE;

        for (Artifact a : images) {
            int l = a.getDependencyTrail().size();
            if(l<shortestLen) {
                shortest.clear();
                shortestLen = l;
            }
            if(l==shortestLen)
                shortest.add(a);
        }

        if(shortest.size()>1)
            throw new MojoExecutionException("More than one base image zip dependency is specified: "+shortest);

        return shortest.iterator().next();
    }
}
