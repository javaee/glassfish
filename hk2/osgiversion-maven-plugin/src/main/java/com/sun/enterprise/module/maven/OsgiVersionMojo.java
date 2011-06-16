/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.module.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.osgi.Maven2OsgiConverter;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/**
 * Converts the project version into the OSGi format and
 * set that to "project.osgi.version" property.
 * It can be configured to drop certain portions from the
 * version. See {@link #dropVersionComponent}.
 *
 * @author Kohsuke Kawaguchi
 * @author Sanjeeb.Sahoo@Sun.COM
 * @goal compute-osgi-version
 * @threadSafe
 * @phase validate
 * @requiresProject
 */
public class OsgiVersionMojo extends AbstractMojo {
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * Flag used to determine what components of the version will be used
     * in OSGi version.
     * An OSGi version has four parts as shown below:
     * major.minor.micro.qualifer.
     * It is not always desirable to use all four parts while
     * exporting packages. In fact, maven version and OSGi version
     * behave just opposite during version comparison as shown below:
     * a maven version 1.2.3-SNAPSHOT is mapped to OSGi version 1.2.3.SNAPSHOT.
     * In maven, 1.2.3 > 1.2.3-SNAPSHOT, but in OSGi, 1.2.3 < 1.2.3.SNAPSHOT.
     * So, it is highly desirable to drop qualifier while computing the version.
     * Instead of hardcoding the policy, we let user tell us what portions will
     * be used in the OSGi version. If they ask us to drop minor, then only
     * major will be used. Similarly, if they ask us to drop qualifier, then
     * major, minor and micro portions will be used.
     * @parameter
     */
    protected String dropVersionComponent;

    /**
     * @parameter default-value="project.osgi.version"
     */
    protected String versionPropertyName;

    private enum VERSION_COMPONENT {major, minor, micro, qualifier};

    /**
     * @component
     */
    protected Maven2OsgiConverter converter;

    public void execute() throws MojoExecutionException, MojoFailureException {
        DefaultArtifactVersion projectVersion =
                new DefaultArtifactVersion(project.getVersion());
        VERSION_COMPONENT compToDrop = dropVersionComponent == null ?
                null : VERSION_COMPONENT.valueOf(dropVersionComponent);

        DefaultArtifactVersion newVersion = projectVersion;
        if (compToDrop != null) {
            switch (compToDrop) {
                case major: {
                    newVersion = new DefaultArtifactVersion("0.0.0");
                    break;
                }
                case minor: {
                    final int major = projectVersion.getMajorVersion();
                    newVersion = new DefaultArtifactVersion(major +"");
                    break;
                }
                case micro: {
                    final int major = projectVersion.getMajorVersion();
                    final int minor = projectVersion.getMinorVersion();
                    newVersion = new DefaultArtifactVersion(major + "." + minor);
                    break;
                }
                case qualifier: {
                    final int major = projectVersion.getMajorVersion();
                    final int minor = projectVersion.getMinorVersion();
                    final int micro = projectVersion.getIncrementalVersion();
                    newVersion = new DefaultArtifactVersion(major + "." + minor + "." + micro);
                    break;
                }
            }
        }
        String v = converter.getVersion(newVersion.toString());

        getLog().debug("OSGi Version for "+project.getVersion()+" is "+v);
        getLog().debug("It is set in project property called "+ versionPropertyName);
        project.getProperties().put(versionPropertyName,v);
    }
}
