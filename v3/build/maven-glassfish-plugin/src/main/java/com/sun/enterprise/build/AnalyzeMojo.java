package com.sun.enterprise.build;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Analyzes dependency declaration and see if there's anything redundantly declared.
 *
 * That is, our goal is to find the explicitly declared dependency X
 * that's also included in the transitive dependency. 
 *
 * @goal analyze-dependency
 * @requiresProject
 * @requiresDependencyResolution runtime
 *
 * @author Kohsuke Kawaguchi
 */
public class AnalyzeMojo extends AbstractGlassfishMojo {
    public void execute() throws MojoExecutionException, MojoFailureException {
        Map<String,Dep> ndd = determineAllNonDirectDependencies();

        // find duplicate
        for( Dependency d : (List<Dependency>)project.getDependencies() ) {
            Dep dep = ndd.get(toKey(d));
            if(dep!=null) {
                getLog().warn("Dependency "+d+" is redundant. It's included through"+dep.trail);
            }
        }
    }

    /**
     * List up all non-direct dependencies
     */
    private Map<String,Dep> determineAllNonDirectDependencies() throws MojoExecutionException {
        Map<String,Dep> m = new HashMap<String, Dep>();

        for( Artifact a : (Set<Artifact>)project.getArtifacts()) {
            try {
                MavenProject p = loadPom(a);
                for( Dependency d : (List<Dependency>)p.getDependencies() ) {
                    m.put(toKey(d),new Dep(d,a));
                }
            } catch (ProjectBuildingException e) {
                throw new MojoExecutionException("Failed to resolve "+a,e);
            }
        }

        return m;
    }

    private String toKey(Dependency d) {
        return d.getGroupId()+':'+d.getArtifactId();
    }

    private static class Dep {
        final List<String> trail;

        public Dep(Dependency d, Artifact a) {
            trail = a.getDependencyTrail();
        }
    }
}
