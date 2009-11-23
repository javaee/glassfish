
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;


import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.glassfish.api.embedded.ContainerBuilder;

import java.util.*;
import java.io.File;

public abstract class AbstractServerMojo extends AbstractMojo {
/**
 * @parameter expression="${serverID}" default-value="maven"
*/
    protected String serverID;

/**
 * @parameter expression="${port}" default-value="-1"
*/
    protected int port;


/**
 * @parameter expression="${installRoot}"
*/
    protected String installRoot;

/**
 * @parameter expression="${instanceRoot}"
*/
    protected String instanceRoot;
/**
 * @parameter expression="${configFile}"
*/
    protected String configFile;

/**
 * @parameter expression="${autoDelete}"
*/
    protected Boolean autoDelete;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * @component
     */
    protected MavenProjectBuilder projectBuilder;

    /**
     * @parameter expression="${localRepository}"
     * @required
     */
    protected ArtifactRepository localRepository;

    /**
     * @component
     */
    protected ArtifactResolver artifactResolver;

    /**
     * Used to construct artifacts for deletion/resolution...
     *
     * @component
     */
    private ArtifactFactory factory;

/**
 * @parameter expression="${containerType}" default-value="all"
*/
    protected String containerType;


    public abstract void execute() throws MojoExecutionException, MojoFailureException;


    void setClassPathProperty() throws ProjectBuildingException {
        String prop = System.getProperty("java.class.path");
        String classPath = getEmbeddedDependenciesClassPath();
        if (classPath != null && classPath.length() > 0) {
            if (prop != null && prop.length() > 0)
                prop = prop + File.pathSeparator;
            System.setProperty("java.class.path", prop + classPath);
        }
    }

    private String getEmbeddedDependenciesClassPath() throws ProjectBuildingException {
        String classPath = "";

        for( Artifact a : (Set<Artifact>)project.getPluginArtifacts()) {
            a.setVersion("3.0-SNAPSHOT");
            // get the plugin artifact and find the MavenProject (POM)
            MavenProject pluginProject = projectBuilder.buildFromRepository(a, project.getRemoteArtifactRepositories(), localRepository);
            List ea = resolveEmbeddedArtifacts(pluginProject);
            for ( Iterator it = ea.iterator(); it.hasNext(); ) {
                Artifact artifact = (Artifact) it.next();
                File f = artifact.getFile();
                if (f != null && f.getName().contains("glassfish-embedded")) {
                    classPath = classPath + f + File.pathSeparator;
                }
            }
        }
        return classPath;
    }

    private List resolveEmbeddedArtifacts( MavenProject project )  {
	List artifactList = new ArrayList();
        List dependencies = project.getDependencies();
        Set dependencyArtifacts = new HashSet();
        for ( Iterator it = dependencies.iterator(); it.hasNext(); ) {
            Dependency dependency = (Dependency) it.next();
            VersionRange vr = VersionRange.createFromVersion( dependency.getVersion() );
            Artifact artifact = factory.createDependencyArtifact( dependency.getGroupId(), dependency.getArtifactId(), vr, dependency.getType(), dependency.getClassifier(), dependency.getScope() );
            dependencyArtifacts.add( artifact );
        }
	for ( Iterator it = dependencyArtifacts.iterator(); it.hasNext(); ) {
            Artifact artifact = (Artifact) it.next();
            try {
                //resolve artifact from localRepository
                artifactResolver.resolve( artifact, Collections.EMPTY_LIST, localRepository );
		artifactList.add( artifact);
	    } catch ( ArtifactResolutionException e ) {
                // cannot resolve artifact
            } catch ( ArtifactNotFoundException e ) {
                //artifact not found..
	    }
        }
        return artifactList;
    }

    ContainerBuilder.Type getContainerBuilderType() {
        if (containerType == null || containerType.equalsIgnoreCase("all"))
            return ContainerBuilder.Type.all;
        else if (containerType.equalsIgnoreCase("web"))
            return ContainerBuilder.Type.web;
        else if (containerType.equalsIgnoreCase("ejb"))
            return ContainerBuilder.Type.ejb;
        else if (containerType.equalsIgnoreCase("jpa"))
            return ContainerBuilder.Type.jpa;
        else if (containerType.equalsIgnoreCase("webservices"))
            return ContainerBuilder.Type.webservices;
        return ContainerBuilder.Type.all;
    }

}
