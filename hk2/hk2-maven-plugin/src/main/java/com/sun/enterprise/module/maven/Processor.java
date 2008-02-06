package com.sun.enterprise.module.maven;

import org.apache.maven.artifact.factory.ArtifactFactory;

/**
 * Data-binding bean to represent an artifact.
 * <p>
 * We use this to allow people to specify annotation processors to be used for APT,
 * in mojo configuration.
 *
 * @author Kohsuke Kawaguchi
 */
public class Processor {
    private String groupId, artifactId, version;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Makes a Maven's artifact object out of it.
     */
    public org.apache.maven.artifact.Artifact createArtifact(ArtifactFactory factory) {
        return factory.createArtifact(groupId, artifactId, version, null, "jar");
    }

    /**
     * Makes a POM artifact out of it.
     */
    public org.apache.maven.artifact.Artifact createPOM(ArtifactFactory factory) {
        return factory.createArtifact(groupId, artifactId, version, null, "pom");
    }
}
