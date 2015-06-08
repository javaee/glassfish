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

import com.sun.enterprise.module.common_impl.Jar;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Logger;

import static org.osgi.framework.Constants.*;

/**
 * Prepares OSGi manifest entries in {@link MavenArchiveConfiguration}.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiPackager {
    /*
     * TODO:
     * 1. Does not yet calculate uses attribute in Export-Package
     */
     
    private static final Logger logger = Logger.getAnonymousLogger();

    private Properties props;


    public OSGiPackager(Properties props) {
        this.props = props;
    }

    /**
     * Reads information from the POM and the artifact archive to configure
     * the OSGi manifest entries. Returns a new set of entries if the archive
     * does not already have manifest entries, else it uses the existing entries
     * map. If any of the attribute already exists, then
     * it skips its processing honoring user's request. It uses the following
     * rules:
     *
     * Bundle-SymbolicName is assumed to be "${groupId}.${artifactId}"
     * Bundle-Version is derived from "${pom.version}"
     * using {@link VersionTranslator#MavenToOSGi(String)}
     * Bundle-Description is assumed to be "${pom.description}".
     * Bundle-Vendor is assumed to be "${pom.organization.name}".
     * Require-Bundle is populated by values read from pom dependencies
     * Note:
     * There is no support for Export-Package yet.
     * It sets Bundle-ManifestVersion as 2 which indicates OSGi r4 bundle.
     *
     * @param pom The Maven project object
     * @param archive The archive that is being built
     * @param classesDirectory output for javac
     * @return Manifest entries
     * @throws java.io.IOException
     */
    public Map<String,String> configureOSGiManifest(MavenProject pom, MavenArchiveConfiguration archive, File classesDirectory) throws IOException {
        Map<String,String> entries;
        if(archive!=null)
            entries = archive.getManifestEntries();
        else
            entries = new HashMap<String,String>();

        if(entries.get(BUNDLE_MANIFESTVERSION) == null) {
            // 2 indicates compliance with r4, note: there is no value called 1
            entries.put(BUNDLE_MANIFESTVERSION, "2");
        }

        if (entries.get(BUNDLE_NAME) == null) {
            // Bundle-Name is a human readable localizable name that can contain spaces
            entries.put(BUNDLE_NAME, pom.getName());
        }

        if (entries.get(BUNDLE_SYMBOLICNAME) == null) {
            // OSGi convention is to use reverse domain name for SymbolicName, hence use .
            entries.put(BUNDLE_SYMBOLICNAME, pom.getGroupId()+'.'+pom.getArtifactId());
        }

        if (entries.get(BUNDLE_VERSION) == null) {
            entries.put(BUNDLE_VERSION, VersionTranslator.MavenToOSGi(pom.getVersion()));
        }

        if (entries.get(BUNDLE_DESCRIPTION) == null) {
            if (pom.getDescription()!=null)
                entries.put(BUNDLE_DESCRIPTION, pom.getDescription());
        }

        if (entries.get(BUNDLE_VENDOR) == null) {
            if (pom.getOrganization()!=null && pom.getOrganization().getName() != null)
                entries.put(BUNDLE_VENDOR, pom.getOrganization().getName());
        }

        // Handle Require-Bundle.
        if (entries.get(REQUIRE_BUNDLE) == null) {
            String requiredBundles = generateRequireBundleHeader(discoverRequiredBundles(pom));
            if (requiredBundles.length()>0) {
                entries.put(REQUIRE_BUNDLE, requiredBundles);
            }
        }

        // Handle Export-Package
        if (entries.get(EXPORT_PACKAGE) == null) {
            List<ExportedPackage> packages = discoverPackages(classesDirectory);

            // don't use version until we resolve split package issues in GF
            String exportPackages = generateExportPackageHeader(packages, null);
            if (exportPackages.length()> 0) {
                entries.put(EXPORT_PACKAGE, exportPackages);
            }
        }
        return entries;
    }

    static class BundleDependency {
        String bundleSymbolicName;
        String versionRange; // version = 1.0.0.SNAPSHOT or "[1.0.0.SNAPSHOT, 1.0.0.SNAPSHOT]"
        String resolution; // optional or mandatory
        String visibility; // private or reexport
    }

    public List<BundleDependency> discoverRequiredBundles(MavenProject pom)
            throws IOException {
        List<BundleDependency> dependencies = new ArrayList<BundleDependency>();
        for (Artifact a : (Set<Artifact>)pom.getDependencyArtifacts()) {
            if("test".equals(a.getScope()) || "provided".equals(a.getScope()))
                continue;
            // http://www.nabble.com/V3-gf%3Arun-throws-NPE-tf4816802.html indicates
            // that some artifacts are not resolved at this point. Not sure when that could happen
            // so aborting with diagnostics if we find it. We need to better understand what this
            // means and work accordingly. - KK
            if(a.getFile()==null) {
                throw new AssertionError(a.getId()+" is not resolved. a="+a);
            }

            Attributes attributes = null;
            String name = null;
            Manifest manifest = Jar.create(a.getFile()).getManifest();
            if (manifest!=null) {
                attributes = manifest.getMainAttributes();
                name = attributes.getValue(BUNDLE_SYMBOLICNAME);
            }
            if (name != null && attributes.getValue(FRAGMENT_HOST)==null) {
                // this is a OSGi host module 
                BundleDependency bd = new BundleDependency();
                bd.bundleSymbolicName = name;

                final String version = attributes.getValue(BUNDLE_VERSION);
                // no need to translate, for it's already an OSGi version
                bd.versionRange = version; // maps to [version, infinity)

                // resolution=optional or mandatory
                bd.resolution = props.getProperty("resolution", RESOLUTION_MANDATORY);

                bd.visibility = props.getProperty("visibility", VISIBILITY_PRIVATE);
                dependencies.add(bd);
            }
        }
        return dependencies;
    }

    public String generateRequireBundleHeader(
            List<BundleDependency> dependencies) {
        // Require-Bundle:
        // a.b.c;version=1.0.0.b58g;resolution:=mandatory;visibility:=reexport,
        // p.q.r;version=1.0.0.SNAPSHOT;resolution:=optional;visibility:=private
        StringBuilder requiredBundles = new StringBuilder();
        for (BundleDependency bd : dependencies) {
            if(requiredBundles.length()!=0) {
                requiredBundles.append(",");
            }
            requiredBundles.append(bd.bundleSymbolicName);

            if(bd.versionRange!= null) {
                requiredBundles.append(';').
                        append(BUNDLE_VERSION_ATTRIBUTE).
                        append('=').
                        append(bd.versionRange);
            }

            requiredBundles.append(";").
                    append(RESOLUTION_DIRECTIVE).
                    append(":=").append(bd.resolution);

            requiredBundles.append(';').
                    append(VISIBILITY_DIRECTIVE).
                    append(":=").append(bd.visibility);
        }
        return requiredBundles.toString();
    }

    static class ExportedPackage implements Comparable<ExportedPackage>{
        String packageName;
        Collection<String> packagesUsed; // uses directive

        public ExportedPackage(
                String packageName,
                Collection<String> packagesUsed) {
            this.packageName = packageName;
            this.packagesUsed = packagesUsed;
        }

        public ExportedPackage(String packageName) {
            this(packageName, null);
        }

        @Override public int hashCode() {
            return packageName.hashCode();
        }

        @Override public boolean equals(Object obj) {
            if (obj instanceof ExportedPackage) {
                return packageName.equals(ExportedPackage.class.cast(obj).packageName);
            }
            return false;
        }

        public int compareTo(ExportedPackage exportedPackage) {
            return this.packageName.compareTo(exportedPackage.packageName);
        }
    }

    public static List<ExportedPackage> discoverPackages(final File classesDirectory) {
        final List<ExportedPackage> packages = new ArrayList<ExportedPackage>();
        classesDirectory.listFiles(
                new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        final File file = new File(dir, name);
                        if (file.isDirectory()) {
                            file.listFiles(this);
                        }
                        final String CLASS_EXT = ".class";
                        if (!name.endsWith(CLASS_EXT)) {
                            return false;
                        }
                        final String packagePath = dir.getPath().substring(classesDirectory.getPath().length()+1);
                        String packageName = packagePath.replace(File.separatorChar, '.');
                        logger.fine("packageName = " + packageName);
                        final ExportedPackage ep = new ExportedPackage(packageName);
                        if (!packages.contains(ep)) {
                            packages.add(ep);
                        }
                        return true;
                    }
                }
        );
        Collections.sort(packages);
        return packages;
    }

    public static String generateExportPackageHeader(
            Collection<ExportedPackage> packages, String version) {
        // Export-Packages=a.b.c;version=1.0.0.SNAPSHOT,p.q.r;x.y.z;version=2.0

        if (packages.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(ExportedPackage p : packages) {
            if(sb.length()>0) {
                sb.append(","); // exclude the first one
            }
            sb.append(p.packageName);

            // TODO: Make use of uses directive

            if (version!=null) {
                sb.append(";").append(VERSION_ATTRIBUTE).append("=").append(version);
            }
        }
        return sb.toString();
    }
}
