/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
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


package org.glassfish.osgijavaeebase;

import com.sun.enterprise.deploy.shared.AbstractReadableArchive;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.osgi.framework.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * This is a very important class in our implementation of hybrid applications. This class maps a bundle and its
 * attached fragments to Java EE archive format.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class OSGiJavaEEArchive extends AbstractReadableArchive implements ReadableArchive {

    // TODO(Sahoo): Lazy population of entries

    protected Bundle host;
    protected Bundle[] fragments;
    private Map<String, ArchiveEntry> entries = new HashMap<String, ArchiveEntry>();
    protected final static String JAR_EXT = ".jar";
    protected static final String DOT = ".";
    protected final Map<Bundle, OSGiBundleArchive> archives;

    public OSGiJavaEEArchive(Bundle[] fragments, Bundle host) {
        this.fragments = fragments!=null ? fragments : new Bundle[0];
        archives = new HashMap<Bundle, OSGiBundleArchive>(this.fragments.length + 1);
        this.host = host;
        init();
        // ensure that we replace the MANIFEST.MF by host's manifest. If host does not have a manifest,
        // then this archive will also not have a manifest.
        final URI hostManifestURI = getArchive(host).getEntryURI(JarFile.MANIFEST_NAME);
        if (hostManifestURI == null) {
            getEntries().remove(JarFile.MANIFEST_NAME);
        } else {
            getEntries().put(JarFile.MANIFEST_NAME, new ArchiveEntry() {
                public String getName() {
                    return JarFile.MANIFEST_NAME;
                }

                public URI getURI() {
                    return hostManifestURI;
                }

                public InputStream getInputStream() throws IOException {
                    return getURI().toURL().openStream();
                }
            });
        }
    }

    protected synchronized OSGiBundleArchive getArchive(Bundle b) {
        OSGiBundleArchive archive = archives.get(b);
        if (archive == null) {
            archive = new OSGiBundleArchive(b);
            archives.put(b, archive);
        }
        return archive;
    }

    protected Map<String, ArchiveEntry> getEntries() {
        return entries;
    }

    protected abstract void init();

    protected EffectiveBCP getEffectiveBCP() {
        EffectiveBCPBuilder builder = new EffectiveBCPBuilder();
        builder.createForHost();
        for (Bundle f : fragments) {
            builder.createForFragment(f);
        }
        return builder.build();
    }

    public InputStream getEntry(String name) throws IOException {
        final ArchiveEntry archiveEntry = entries.get(name);
        return archiveEntry!= null ? archiveEntry.getInputStream() : null;
    }

    public boolean exists(String name) throws IOException {
        return entries.containsKey(name);
    }

    public long getEntrySize(String name) {
        return 0; // can't determine
    }

    public void open(URI uri) throws IOException {
        throw new UnsupportedOperationException();
    }

    public ReadableArchive getSubArchive(String name) throws IOException {
        return null;  //TODO(Sahoo): Not Yet Implemented
    }

    public boolean exists() {
        return true;
    }

    public boolean delete() {
        return false;
    }

    public boolean renameTo(String name) {
        return false; // can't rename
    }

    public void setParentArchive(ReadableArchive parentArchive) {
        throw new UnsupportedOperationException();
    }

    public ReadableArchive getParentArchive() {
        return null;
    }

    public void close() throws IOException {
        // nothing to do
    }

    public Enumeration<String> entries() {
        final Enumeration<String> all = Collections.enumeration(entries.keySet());

        // return only file entries as per the conract of this method
        return new Enumeration<String> () {
            String next = getNext();
            public boolean hasMoreElements() {
                return next!= null;
            }

            public String nextElement() {
                if (hasMoreElements()) {
                    String result = next;
                    next = getNext();
                    return result;
                }
                throw new NoSuchElementException();
            }

            private String getNext() {
                while (all.hasMoreElements()) {
                    String s = all.nextElement();
                    if (!s.endsWith("/")) { // not a directory entry
                        return s;
                    }
                }
                return null;
            }
        };
    }

    public Enumeration<String> entries(final String prefix) {
        final Enumeration<String> all = entries();
        return new Enumeration<String> (){
            String next = getNext();
            public boolean hasMoreElements() {
                return next!= null;
            }

            public String nextElement() {
                if (hasMoreElements()) {
                    String result = next;
                    next = getNext();
                    return result;
                }
                throw new NoSuchElementException();
            }

            private String getNext() {
                while (all.hasMoreElements()) {
                    String s = all.nextElement();
                    if (s.startsWith(prefix)) {
                        return s;
                    }
                }
                return null;
            }
        };
    }

    public Collection<String> getDirectories() throws IOException {
        Collection<String> dirEntries = new ArrayList<String>();
        Enumeration<String> all = entries();
        while(all.hasMoreElements()) {
            final String s = all.nextElement();
            if (s.endsWith("/")) dirEntries.add(s);
        }
        return dirEntries;
    }

    public boolean isDirectory(String name) {
        return name.endsWith("/"); // TODO(Sahoo): Check if this is correct.
    }

    public Manifest getManifest() throws IOException {
        final InputStream is = getEntry(JarFile.MANIFEST_NAME);
        if (is != null) {
            try {
                return new Manifest(is);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        } else {
            return null;
        }
    }

    public URI getURI() {
        return null; // this represents a collection, so return null
    }

    public long getArchiveSize() throws SecurityException {
        return 0;
    }

    public String getName() {
        return getArchive(host).getName();
    }

    protected interface ArchiveEntry {
        String getName();

        URI getURI() throws URISyntaxException;

        InputStream getInputStream() throws IOException;
    }

    protected interface BCPEntry {

        /**
         * @return path relative to its bundle.
         */
        String getName();

        /**
         * @return the bundle this entry belongs to. Please note, a host bundle can insert a classpath entry
         *         into a fragment bundle.
         */
        Bundle getBundle();

        void accept(OSGiJavaEEArchive.BCPEntry.BCPEntryVisitor visitor);

        interface BCPEntryVisitor {
            void visitDir(OSGiJavaEEArchive.DirBCPEntry bcpEntry);

            void visitJar(OSGiJavaEEArchive.JarBCPEntry bcpEntry);
        }
    }

    protected class DirBCPEntry implements BCPEntry {
        private String name;
        private Bundle bundle;

        public DirBCPEntry(String name, Bundle bundle) {
            this.name = name;
            this.bundle = bundle;
        }

        public String getName() {
            return name;
        }

        public Bundle getBundle() {
            return bundle;
        }

        public void accept(BCPEntryVisitor visitor) {
            visitor.visitDir(this);
        }
    }

    protected class JarBCPEntry implements BCPEntry {
        private String name;
        private Bundle bundle;

        public JarBCPEntry(String name, Bundle bundle) {
            this.name = name;
            this.bundle = bundle;
        }

        public String getName() {
            return name;
        }

        public Bundle getBundle() {
            return bundle;
        }

        public void accept(BCPEntryVisitor visitor) {
            visitor.visitJar(this);
        }
    }

    protected class EffectiveBCP {
        private List<BCPEntry> bcpEntries = new ArrayList<BCPEntry>();

        public List<BCPEntry> getBCPEntries() {
            return bcpEntries;
        }

        public void accept(BCPEntry.BCPEntryVisitor visitor) {
            for (BCPEntry bcpEntry : getBCPEntries()) {
                bcpEntry.accept(visitor);
            }
        }

        public void add(BCPEntry bcpEntry) {
            bcpEntries.add(bcpEntry);
        }

    }

    class EffectiveBCPBuilder {
        private EffectiveBCP result = new EffectiveBCP();

        public EffectiveBCP build() {
            return result;
        }

        void createForHost() {
            List<Bundle> bundles = new ArrayList(Arrays.asList(fragments));
            bundles.add(0, host); // search in host first
            for (String s : tokenizeBCP(host)) {
                s = s.trim();
                for (Bundle b : bundles) {
                    OSGiBundleArchive archive = getArchive(b);
                    if (DOT.equals(s)) {
                        result.add(createJarBCPEntry(DOT, b));
                    } else if (archive.exists(s)) {
                        if (archive.isDirectory(s)) {
                            if (!s.endsWith("/")) {
                                s = s.concat("/"); // This ensures that entries from subarchive won't have leading /
                            }
                            result.add(createDirBCPEntry(s, b));
                        } else {
                            result.add(createJarBCPEntry(s, b));
                        }
                    }
                }
            }
        }

        /**
         * @param bundle fragment bundle
         */
        void createForFragment(Bundle bundle) {
            for (String s : tokenizeBCP(bundle)) {
                s = s.trim();
                OSGiBundleArchive archive = getArchive(bundle);
                if (DOT.equals(s)) {
                    result.add(createJarBCPEntry(DOT, bundle));
                } else if (archive.exists(s)) {
                    if (archive.isDirectory(s)) {
                        if (!s.endsWith("/")) {
                            s = s.concat("/"); // This ensures that entries from subarchive won't have leading /
                        }
                        result.add(createDirBCPEntry(s, bundle));
                    } else {
                        result.add(createJarBCPEntry(s, bundle));
                    }
                }
            }

        }

        private JarBCPEntry createJarBCPEntry(String entryPath, Bundle bundle) {
            return new JarBCPEntry(entryPath, bundle);
        }

        private DirBCPEntry createDirBCPEntry(String entryPath, Bundle bundle) {
            return new DirBCPEntry(entryPath, bundle);
        }

        /**
         * Parses Bundle-ClassPath of a bundle and returns it as a sequence of String tokens.
         */
        private String[] tokenizeBCP(Bundle b) {
            String bcp = (String) b.getHeaders().get(org.osgi.framework.Constants.BUNDLE_CLASSPATH);
            if (bcp == null || bcp.isEmpty()) bcp = DOT;
            return bcp.split(";|,");
        }
    }
}
