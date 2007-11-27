package com.sun.enterprise.build;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import com.sun.enterprise.module.impl.Jar;
import com.sun.enterprise.module.ManifestConstants;

import java.util.jar.Attributes;
import java.util.Set;
import java.util.HashSet;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractGlassfishMojo extends AbstractMojo {
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    protected boolean isModule(Artifact a) throws MojoExecutionException {
        try {
            Jar jar = Jar.create(a.getFile());
            if (jar.getManifest()==null) {
                return false;
            }
            Attributes attributes = jar.getManifest().getMainAttributes();
            String name = attributes.getValue(ManifestConstants.BUNDLE_NAME);
            return name!=null;
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to open "+a.getFile(),e);
        }
    }

    protected interface ArtifactFilter {
        boolean find(Artifact a);
    }

    /**
     * Finds artifacts that match the given filter.
     */
    protected Set<Artifact> findArtifacts(Set<Artifact> artifacts, ArtifactFilter filter) {
        Set<Artifact> r = new HashSet<Artifact>();

        for(Artifact a : artifacts) {
            if(filter.find(a)) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Including " + a.getGroupId() + ":" + a.getArtifactId() + ":"+ a.getVersion());
                    getLog().debug("From dependency trail : ");
                    for (int i=a.getDependencyTrail().size()-1;i>=0;i--) {
                        getLog().debug(" " + a.getDependencyTrail().get(i).toString());
                    }
                    getLog().debug("");
                }
                r.add(a);
            }
        }

        return r;
    }

    /**
     * Returns a set of {@link Artifact}s that have the given type.
     */
    protected Set<Artifact> findArtifactsOfType(Set<Artifact> artifacts, final String type) {
        return findArtifacts(artifacts,new ArtifactFilter() {
            public boolean find(Artifact a) {
                String t = a.getType();
                if(t==null)  t="jar"; // see http://maven.apache.org/pom.html
                return t.equals(type);
            }
        });
    }

    protected Set<Artifact> findArtifactsOfScope(Set<Artifact> artifacts, final String scope) {
        return findArtifacts(artifacts,new ArtifactFilter() {
            public boolean find(Artifact a) {
                String s = a.getScope();
                return s.equals(scope);
            }
        });
    }
}
