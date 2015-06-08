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

package org.jvnet.hk2.osgiadapter;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.framework.Constants;

import com.sun.enterprise.module.ManifestConstants;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModuleDependency;
import com.sun.enterprise.module.ModuleMetadata;
import com.sun.enterprise.module.common_impl.Jar;
import com.sun.enterprise.module.common_impl.LogHelper;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiModuleDefinition implements ModuleDefinition, Serializable {

    private String name;
    private String bundleName;
    private URI location;
    private String version;
    private Manifest manifest;
    private String lifecyclePolicyClassName;
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
        final Manifest m = jarFile.getManifest();
        manifest = m instanceof Serializable ? m : new SerializableManifest(m);
        Attributes mainAttr = manifest.getMainAttributes();
        bundleName = mainAttr.getValue(Constants.BUNDLE_NAME);

        name = mainAttr.getValue(Constants.BUNDLE_SYMBOLICNAME);
        // R3 bundles may not have any name, yet HK2 requires some name to be
        // assigned. So, we use location in such cases. We encounter this
        // problem when user has dropped some plain jars or R3 bundles
        // in modules dir. If you choose to use a different name,
        // please also change the code in OSGiModuleId class which makes
        // similar assumption.
        if (name == null) name = location.toString();
        version = mainAttr.getValue(Constants.BUNDLE_VERSION);
        lifecyclePolicyClassName = mainAttr.getValue(ManifestConstants.LIFECYLE_POLICY);
        jarFile.loadMetadata(metadata);
    }

    public OSGiModuleDefinition(Bundle b) throws IOException, URISyntaxException {
        this(new BundleJar(b), toURI(b));
    }

    static URI toURI(Bundle b) throws URISyntaxException {
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
                getManifest().getMainAttributes().getValue(Constants.REQUIRE_BUNDLE);
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
        return name + "(" + bundleName + ")" + ':' + version;
    }

    private static class BundleJar extends Jar {
        private static final String HK2_DESCRIPTOR_LOCATION = "META-INF/hk2-locator";

        private static final String SERVICE_LOCATION = "META-INF/services";
        Bundle b;
        Manifest m;

        private BundleJar(Bundle b) throws IOException {
            this.b = b;
            m = new BundleManifest(b);
        }

        public Manifest getManifest() throws IOException {
            return m;
        }

        public void loadMetadata(ModuleMetadata result) {
            parseServiceDescriptors(result);
            parseDescriptors(result);
        }

        private void parseServiceDescriptors(ModuleMetadata result) {
            /*
             * This optimisation was earlier not working because of FELIX-1210.
             */
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

        /**
         * Read in the hk2 descriptors
         */
        void parseDescriptors(ModuleMetadata result) {

            if (b.getEntry(HK2_DESCRIPTOR_LOCATION) == null) return;

            Enumeration<String> entries;
            entries = b.getEntryPaths(HK2_DESCRIPTOR_LOCATION);

            if (entries != null) {
                while (entries.hasMoreElements()) {
                    String entry = entries.nextElement();
                    String serviceLocatorName = entry.substring(HK2_DESCRIPTOR_LOCATION.length()+1);

                    final URL url = b.getEntry(entry);

                    InputStream is = null;

                    if (url != null) {

                        List<Descriptor> descriptors = new ArrayList<Descriptor>();

                        try {
                            is = url.openStream();

                            BufferedReader br = new BufferedReader(new InputStreamReader(is));

                            try {
                                boolean readOne = false;

                                do {
                                    DescriptorImpl descriptorImpl = new DescriptorImpl();

                                    readOne = descriptorImpl.readObject(br);

                                    if (readOne) {
                                        descriptors.add(descriptorImpl);
                                    }
                                } while (readOne);

                            } finally {
                                br.close();
                            }

                            result.addDescriptors(serviceLocatorName, descriptors);

                        } catch (IOException e) {
                            LogHelper.getDefaultLogger().log(Level.SEVERE,
                                    "Error reading descriptor in " + b.getLocation(), e);
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
        }


        public String getBaseName() {
            throw new UnsupportedOperationException("Method not implemented");
        }

        /**
         * Manifest of a bundle. It is optimized for serialization as it writes out only the bundle id to the stream.
         * So, it can only be desrialized in the context of OSGi and that too only if the bundle id is same.
         */
        private static class BundleManifest extends Manifest implements Serializable {
            private long bundleId;

            private BundleManifest() {
            }

            private BundleManifest(Bundle b) {
                this.bundleId = b.getBundleId();
                init(b);
            }

            private void init(Bundle b) {
                Attributes attrs = getMainAttributes();
                Dictionary headers = b.getHeaders();
                for (Object o : Collections.list(headers.keys())) {
                    attrs.putValue((String)o, (String)headers.get(o));
                }
            }

            // Serialization method
            private void writeObject(ObjectOutputStream out) throws IOException {
                out.defaultWriteObject();
            }

            private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
                in.defaultReadObject();
                ClassLoader cl;
                if (System.getSecurityManager() != null) {
                    cl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                        public ClassLoader run() {
                            return getClass().getClassLoader();
                        }
                    });
                } else {
                    cl = getClass().getClassLoader();
                }

                Bundle clBundle = ((BundleReference)cl).getBundle();

                if (clBundle == null) {
                    throw new RuntimeException(("Cannot resolve classLoader " + cl) + " bundle");

                }
                BundleContext bctx = clBundle.getBundleContext();

                if (bctx == null) {
                    throw new RuntimeException("Cannot obtain BundleContext");
                }

                Bundle bundle = bctx.getBundle(bundleId);

                if (bundle == null) {
                   throw new RuntimeException("Cannot obtain bundle " + bundleId);
                }

                init(bundle);
            }

        }
    }

    private static class SerializableManifest extends Manifest implements Serializable {

        private SerializableManifest()
        {
        }

        private SerializableManifest(Manifest man)
        {
            super(man);
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            write(out);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            read(in);
        }
    }

}
