package com.sun.enterprise.build;

import org.apache.maven.artifact.handler.ArtifactHandler;

/**
 * Presumably because of the initialization order issue,
 * I can't convince Maven to load the artifact handler definition from components.xml.
 *
 * So as a hack we set it manually.
 * 
 * @author Kohsuke Kawaguchi
 */
public class DistributionArtifactHandler implements ArtifactHandler {
    public String getExtension() {
        return "zip";
    }

    public String getDirectory() {
        return null;
    }

    public String getClassifier() {
        return null;
    }

    public String getPackaging() {
        return "glassfish-distribution";
    }

    public boolean isIncludesDependencies() {
        return false;
    }

    public String getLanguage() {
        return "java";
    }

    public boolean isAddedToClasspath() {
        return false;
    }
}
