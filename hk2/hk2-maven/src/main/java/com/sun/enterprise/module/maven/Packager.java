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

import com.sun.enterprise.module.ImportPolicy;
import com.sun.enterprise.module.ManifestConstants;
import com.sun.enterprise.module.LifecyclePolicy;
import com.sun.enterprise.module.common_impl.Jar;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Prepares manifest entries in {@link MavenArchiveConfiguration}.
 *
 * <p>
 * This class is moved outside {@code PackageMojo} and doesn't rely
 * on Mojo resource injection to allow other plugins to reuse
 * this capability without hitting http://jira.codehaus.org/browse/MNG-2789.
 *
 * @author Kohsuke Kawaguchi
 */
public class Packager {
    /**
     * Creates META-INF/MANIFEST.MF with all the entries needed for HK2 runtime.
     *
     * @param pom
     *      The project from which we are creating manifest. 
     */
    public void writeManifest(MavenProject pom, File classesDirectory) throws IOException {
        Manifest mf = new Manifest();
        for( Map.Entry<String,String> e : configureManifest(pom,null,classesDirectory).entrySet()) {
            mf.getMainAttributes().put(
                new Attributes.Name(e.getKey()),e.getValue());
        }
        mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION,"1.0");
        
        File manifest = new File(classesDirectory, "META-INF/MANIFEST.MF");
        manifest.getParentFile().mkdir();
        FileOutputStream os = new FileOutputStream(manifest);
        try {
            mf.write(os);
        } finally{
            os.close();
        }
    }
    public Map<String,String> configureManifest(MavenProject pom, MavenArchiveConfiguration archive, File classesDirectory) throws IOException {
        Map<String,String> entries;
        if(archive!=null)
            entries = archive.getManifestEntries();
        else
            entries = new HashMap<String,String>();

        entries.put(ManifestConstants.BUNDLE_NAME, pom.getGroupId()+'.'+pom.getArtifactId() );

        // check META-INF/services/xxx.ImportPolicy to fill in Import-Policy
        configureImportPolicy(classesDirectory, entries, ImportPolicy.class, ManifestConstants.IMPORT_POLICY);
        configureImportPolicy(classesDirectory, entries, LifecyclePolicy.class, ManifestConstants.LIFECYLE_POLICY);


        // check direct dependencies to find out dependency modules.
        // we don't need to list transitive dependencies here, so use getDependencyArtifacts().

        TokenListBuilder dependencyModuleNames = new TokenListBuilder();
        Set<String> dependencyModules = new HashSet<String>();  // used to find transitive dependencies through other modules.
        for (Artifact a : (Set<Artifact>)pom.getDependencyArtifacts()) {
            if(a.getScope()!=null && a.getScope().equals("test"))
                continue;
            // http://www.nabble.com/V3-gf%3Arun-throws-NPE-tf4816802.html indicates
            // that some artifacts are not resolved at this point. Not sure when that could happen
            // so aborting with diagnostics if we find it. We need to better understand what this
            // means and work accordingly. - KK
            if(a.getFile()==null) {
                throw new AssertionError(a.getId()+" is not resolved. a="+a);
            }
            Jar jar;
            try {
                jar = Jar.create(a.getFile());
            } catch (IOException ioe) {
                // not a jar file, so continue.
                continue;
            }
            Manifest manifest = jar.getManifest();
            String name = null;
            if (manifest!=null) {
                Attributes attributes = manifest.getMainAttributes();

                name = attributes.getValue(ManifestConstants.BUNDLE_NAME);
            }
            if (name != null) {
                // this is a hk2 module
                if (!a.isOptional())
                    dependencyModuleNames.add(name);

                // even optional modules need to be listed here
                dependencyModules.add(a.getGroupId() + '.' + a.getArtifactId() + ':' + a.getVersion());
            }
        }

        // find jar files to be listed in Class-Path. This needs to include transitive
        // dependencies, except when the path involves a hk2 module.
        TokenListBuilder classPathNames = new TokenListBuilder(" ");
        TokenListBuilder classPathIds = new TokenListBuilder(" ");
        for (Artifact a : (Set<Artifact>)pom.getArtifacts()) {
            // check the trail. does that include hk2 module in the path?
            boolean throughModule = false;
            for (String module : dependencyModules)
                throughModule |= a.getDependencyTrail().get(1).toString().startsWith(module);
            if(throughModule)
                continue;   // yep

            if(a.getScope().equals("system") || a.getScope().equals("provided") || a.getScope().equals("test"))
                continue;   // ignore tools.jar and such dependencies.

            if(a.isOptional())
                continue;   // optional dependency
            
            classPathNames.add(stripVersion(a));
            classPathIds.add(a.getId());
        }
        if(!classPathNames.isEmpty()) {
            String existingClassPath = entries.get(ManifestConstants.CLASS_PATH);
            if(existingClassPath!=null)
                entries.put(ManifestConstants.CLASS_PATH,existingClassPath+" "+classPathNames);
            else
                entries.put(ManifestConstants.CLASS_PATH,classPathNames.toString());

            entries.put(ManifestConstants.CLASS_PATH_ID,classPathIds.toString());
        }

        return entries;
    }

    private void configureImportPolicy(File classesDirectory, Map<String, String> entries, Class<?> clazz, String entryName) throws IOException {
        File importPolicy = new File(classesDirectory, "META-INF/services/" + clazz.getName());
        if(importPolicy.exists()) {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(importPolicy), "UTF-8"));
            entries.put(entryName,in.readLine());
            in.close();
        }
    }

    private static String stripVersion(Artifact a) {
        String origName = a.getFile().getName();
        String newName = origName;
        int idx = origName.lastIndexOf(a.getVersion());
        if (idx != -1) {
            // remove version in artifactId-version(-classifier).type
            String baseFilename = origName.substring( 0, idx - 1 );
            String extension = origName.substring( idx + a.getVersion().length());
            newName = baseFilename + extension;
        }
        return newName;
    }
}
