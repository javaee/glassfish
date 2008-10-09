/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
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


package org.jvnet.hk2.osgiadapter;

import com.sun.enterprise.module.ManifestConstants;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModuleDependency;
import com.sun.enterprise.module.ModuleMetadata;
import com.sun.enterprise.module.common_impl.Jar;
import com.sun.enterprise.module.common_impl.LogHelper;
import com.sun.hk2.component.InhabitantsFile;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiModuleDefinition implements ModuleDefinition {

    private String name;
    private URI location;
    private String version;
    private String lifecyclePolicyClassName;
    private Manifest manifest;
    private ModuleMetadata metadata = new ModuleMetadata();

    public OSGiModuleDefinition(File jar) throws IOException {
        this(Jar.create(jar), jar.toURI());
    }

    public OSGiModuleDefinition(Jar jarFile, URI location) throws IOException {
        /*
        * When we support reading metadata from external manifest.mf file,
        * we can create a custom URI and the URL handler can merge the
        * manifest info. For now, just use the standard URI.
        */
        this.location = location;
        manifest = jarFile.getManifest();
        Attributes mainAttr = manifest.getMainAttributes();
        name = mainAttr.getValue(Constants.BUNDLE_SYMBOLICNAME);
        version = mainAttr.getValue(Constants.BUNDLE_VERSION);
        if (version == null) version = "0.0.0"; // default in OSGi
        lifecyclePolicyClassName = mainAttr.getValue(ManifestConstants.LIFECYLE_POLICY);
        jarFile.loadMetadata(metadata);
    }

    public OSGiModuleDefinition(Bundle b) throws IOException, URISyntaxException {
        this(new BundleJar(b), toURI(b));
    }

    private static URI toURI(Bundle b) throws URISyntaxException {
        try {
            return new URI(b.getLocation());
        } catch (URISyntaxException ue) {
            // On Equinox, bundles started via autostart have a strange location.
            // It is of the format: initial@reference:file:...
            // It can't be turned into an URI, so we take out initial@.
            if (b.getLocation().startsWith("initial@")) {
                return new URI(b.getLocation().substring("initial@".length()));
            } else {
                throw ue;
            }
        }
    }

    public String getName() {
        return name;
    }

    public String[] getPublicInterfaces() {
        throw new UnsupportedOperationException(
                "This method should not be called in OSGi environment, " +
                        "hence not supported");
    }

    /**
     * @return List of bundles on which this bundle depends on using Require-Bundle
     */
    public ModuleDependency[] getDependencies() {
        List<ModuleDependency> mds = new ArrayList<ModuleDependency>();
        String requiredBundles =
                manifest.getMainAttributes().getValue(Constants.REQUIRE_BUNDLE);
        if (requiredBundles != null) {
            Logger.logger.log(Level.INFO, name + " -> " + requiredBundles);
            // The string looks like
            // Require-Bundle: b1; version="[1.0, 2.0)", b2, b3;visbility:=reexport; version="1.0",...
            // First remove the regions that appear between a pair of quotes (""), as that
            // can confuse the tokenizer.
            // Then, tokenize using comma(,) as that separates one bundle from another.
            while (true) {
                int i1 = requiredBundles.indexOf('\"');
                if (i1 == -1) break;
                int i2 = requiredBundles.indexOf('\"', i1 + 1);
                StringBuilder sb = new StringBuilder();
                sb.append(requiredBundles.substring(0, i1));
                sb.append(requiredBundles.substring(i2 + 1));
                requiredBundles = sb.toString();
            }
            StringTokenizer st =
                    new StringTokenizer(requiredBundles, ",", false);
            while (st.hasMoreTokens()) {
                String requireBundle = st.nextToken();
                String requiredBundleName;
                int idx = requireBundle.indexOf(';');
                if (idx == -1) {
                    requiredBundleName = requireBundle;
                } else {
                    requiredBundleName = requireBundle.substring(0, idx);
                    // TODO(Sahoo): parse version and other stuff
                }
                mds.add(new ModuleDependency(requiredBundleName, null));
            }
        }
        return mds.toArray(new ModuleDependency[mds.size()]);
    }

    public URI[] getLocations() {
        return new URI[]{location};
    }

    public String getVersion() {
        return version;
    }

    public String getImportPolicyClassName() {
        throw new UnsupportedOperationException(
                "This method should not be called in OSGi environment, " +
                        "hence not supported");
    }

    public String getLifecyclePolicyClassName() {
        return lifecyclePolicyClassName;
    }

    public Manifest getManifest() {
        return manifest;
    }

    public ModuleMetadata getMetadata() {
        return metadata;
    }

    /**
     * Assists debugging.
     */
    @Override
    public String toString() {
        String bundleDescriptiveName =
                manifest.getMainAttributes().getValue(Constants.BUNDLE_NAME);
        return name + "(" + bundleDescriptiveName + ")" + ':' + version;
    }

    private static class BundleJar extends Jar {
        private static final String SERVICE_LOCATION = "META-INF/services";
        Bundle b;
        Manifest m;

        private BundleJar(Bundle b) throws IOException {
            this.b = b;
            InputStream is = b.getEntry(JarFile.MANIFEST_NAME).openStream();
            try {
                m = new Manifest(is);
            } finally {
                is.close();
            }
        }

        public Manifest getManifest() throws IOException {
            return m;
        }

        public void loadMetadata(ModuleMetadata result) {
            parseInhabitantsDescriptors(result);
            parseServiceDescriptors(result);
        }

        private void parseInhabitantsDescriptors(ModuleMetadata result) {
            if (b.getEntry(InhabitantsFile.PATH) == null) return;
            Enumeration<String> entries = b.getEntryPaths(InhabitantsFile.PATH);
            if (entries != null) {
                while (entries.hasMoreElements()) {
                    String entry = entries.nextElement();
                    String habitatName = entry.substring(InhabitantsFile.PATH.length() + 1);
                    final URL url = b.getEntry(entry);
                    try {
                        result.addHabitat(habitatName,
                                new ModuleMetadata.InhabitantsDescriptor(
                                        url, loadFully(url)
                                ));
                    } catch (IOException e) {
                        LogHelper.getDefaultLogger().log(Level.SEVERE,
                                "Error reading inhabitants list in " + b.getLocation(), e);
                    }
                }
            }
        }

        private void parseServiceDescriptors(ModuleMetadata result) {
            if (b.getEntry(SERVICE_LOCATION) == null) return;
            Enumeration<String> entries;
            entries = b.getEntryPaths(SERVICE_LOCATION);
            if (entries != null) {
                while (entries.hasMoreElements()) {
                    String entry = entries.nextElement();
                    String serviceName = entry.substring(SERVICE_LOCATION.length()+1);
                    InputStream is = null;
                    final URL url = b.getEntry(entry);
                    try {
                        is = url.openStream();
                        result.load(url, serviceName, is);
                    } catch (IOException e) {
                        LogHelper.getDefaultLogger().log(Level.SEVERE,
                                "Error reading service provider in " + b.getLocation(), e);
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e) {}
                        }
                    }
                }
            }
        }

        private byte[] loadFully(URL url) throws IOException {
            InputStream in = url.openStream();
            byte[] buf = new byte[0];
            try {
                int chunkSize = 512;
                byte[] chunk = new byte[chunkSize];
                while (true) {
                    int count = in.read(chunk, 0, chunkSize);
                    if (count == -1) break; // EOF
                    final int curLength = buf.length;
                    byte[] newbuf = new byte[curLength + count];
                    System.arraycopy(buf, 0, newbuf, 0, curLength);
                    System.arraycopy(chunk, 0, newbuf, curLength, count);
                    buf = newbuf;
                }
                return buf;
            } finally {
                in.close();
            }
        }

        public String getBaseName() {
            throw new UnsupportedOperationException("Method not implemented");
        }
    }
}
