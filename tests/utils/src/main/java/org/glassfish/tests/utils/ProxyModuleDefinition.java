package org.glassfish.tests.utils;

import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModuleMetadata;
import com.sun.enterprise.module.ModuleDependency;
import com.sun.hk2.component.InhabitantsFile;

import java.util.jar.Manifest;
import java.util.Collections;
import java.util.Collection;
import java.util.Enumeration;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URI;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Mar 12, 2009
 * Time: 9:46:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProxyModuleDefinition implements ModuleDefinition {
        private final ModuleMetadata metadata = new ModuleMetadata();
        private final Manifest manifest = new Manifest();

        public ProxyModuleDefinition(ClassLoader classLoader) throws IOException {
            this(classLoader, Collections.singleton("default"));
        }

        public ProxyModuleDefinition(ClassLoader classLoader, Collection<String> habitatNames) throws IOException {
            for (String habitatName : habitatNames) {
                Enumeration<URL> inhabitants = classLoader.getResources(InhabitantsFile.PATH+'/'+habitatName);
                while (inhabitants.hasMoreElements()) {
                    URL url = inhabitants.nextElement();
                    metadata.addHabitat(habitatName,
                        new ModuleMetadata.InhabitantsDescriptor(url, readFully(url))
                    );
                }
            }
        }

        private static byte[] readFully(URL url) throws IOException {
            try {
                URLConnection con = url.openConnection();
                int len = con.getContentLength();
                InputStream in = con.getInputStream();
                try {
                    if(len<0) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int sz;
                        while((sz=in.read(buf))>=0)
                            baos.write(buf,0,sz);
                        return baos.toByteArray();
                    } else {
                        byte[] r = new byte[len];
                        new DataInputStream(in).readFully(r);
                        return r;
                    }
                } finally {
                    in.close();
                }
            } catch (IOException e) {
                IOException x = new IOException("Failed to read " + url);
                x.initCause(e);
                throw x;
            }
        }

        public String getName() {
            return toString();
        }

        public String[] getPublicInterfaces() {
            return null;
        }

        public ModuleDependency[] getDependencies() {
            return EMPTY_MODULE_DEFINITIONS_ARRAY;
        }

        public URI[] getLocations() {
            return EMPTY_URI_ARRAY;
        }

        public String getVersion() {
            return "1.0.0";
        }

        public String getImportPolicyClassName() {
            return null;
        }

        public String getLifecyclePolicyClassName() {
            return null;
        }

        public Manifest getManifest() {
            return manifest;
        }

        public ModuleMetadata getMetadata() {
            return metadata;
        }

        private static final String[] EMPTY_STRING_ARRAY = new String[0];
        private static final ModuleDependency[] EMPTY_MODULE_DEFINITIONS_ARRAY = new ModuleDependency[0];
        private static final URI[] EMPTY_URI_ARRAY = new URI[0];
    }
