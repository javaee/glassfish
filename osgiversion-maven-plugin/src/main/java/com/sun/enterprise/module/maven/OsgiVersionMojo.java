/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2015 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.hk2.maven.Version;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

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
    protected Version.COMPONENT dropVersionComponent;

    /**
     * @parameter default-value="project.osgi.version"
     */
    protected String versionPropertyName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Version projectVersion = new Version(project.getVersion());
        String v = projectVersion.convertToOsgi(dropVersionComponent);
        getLog().debug("OSGi Version for "+project.getVersion()+" is "+v);
        getLog().debug("It is set in project property called "+ versionPropertyName);
        project.getProperties().put(versionPropertyName,v);
    }
}
