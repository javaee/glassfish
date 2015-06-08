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

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.osgi.framework.Version;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * The versioning scheme used by Maven, HK2 and OSGi are all different in
 * subtle ways. e.g., something that you represent as 1.0-SNAPSHOT in Maven
 * is represented as 1.0.0.SNAPSHOT in OSGi. This class encapsulate those
 * different schemes and provides utility methods to translate from one
 * to another.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
class VersionTranslator {
    /*
     * Implementation Note:
     * Maven version scheme looks like this:
     * <major>.<minor>.<revision>([-<qualifier>]|[-<build>])
     * It is impemented at:
     * http://svn.apache.org/viewvc/maven/artifact/trunk/src/main/java/org/apache/maven/artifact/versioning/DefaultArtifactVersion.java?view=markup
     * It is further described (to some extent) at:
     * http://docs.codehaus.org/display/MAVEN/Versioning
     * OSGi versioning scheme is defined at
     * http://www2.osgi.org/javadoc/r4/org/osgi/framework/Version.html#Version(java.lang.String)
     * Since they differ, we need to translate.
     */

    private static Logger logger = Logger.getAnonymousLogger();

    /**
     * Translates Maven Version String to OSGi Version String
     * @param mvs
     * @return a OSGi version string
     */
    public static String MavenToOSGi(String mvs) {
        try {
            ArtifactVersion mv = new DefaultArtifactVersion(mvs);
            int major = mv.getMajorVersion();
            int minor = mv.getMinorVersion();
            int micro = mv.getIncrementalVersion();

            // 0 is never a valid build number
            String qualifier = mv.getBuildNumber() != 0 ?
                    Integer.toString(mv.getBuildNumber()) : mv.getQualifier();
            logger.logp(Level.INFO, "VersionTranslator", "MavenToOSGi",
                    "qualifier = {0}", qualifier);
            Version ov =
                    qualifier == null ?
                    new Version(major, minor, micro)
                    : new Version(major, minor, micro, qualifier);
            logger.logp(Level.INFO, "VersionTranslator", "MavenToOSGi", "{0} -> {1}", new Object[]{mvs, ov});
            return ov.toString();
        } catch(RuntimeException e) {
            logger.logp(Level.INFO, "VersionTranslator", "MavenToOSGi",
                    "Following exception was raised " +
                    "while translating Maven version {0} to OSGi version: {1}",
                    new Object[]{mvs, e});
            throw e;
        }
    }

    /**
     * Translate OSGi version string to Maven Version String
     * @param ovs
     * @return
     */
    public static String OSGiToMaven(String ovs) {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }

    /**
     * Main method used for testing only
     * @param args
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Enter maven version:");
        DataInputStream in = new DataInputStream(System.in);
        String mvs = in.readLine();
        System.out.println("OSGI version: " + MavenToOSGi(mvs));
    }
}
