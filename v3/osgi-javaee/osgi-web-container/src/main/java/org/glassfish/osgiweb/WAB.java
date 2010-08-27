/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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


package org.glassfish.osgiweb;

import org.glassfish.api.deployment.archive.Archive;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.osgijavaeebase.OSGiBundleArchive;
import org.osgi.framework.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Servlet spec, the spec which defines the term Web Application, defines the overall structure of a
 * Web Application as a hierrachical arrangement of files (and directories), but does not mandate them to be
 * available in a hierarchical file system per se. See section #10.4 of Servlet 3.0 spec, which mentions
 * the following:
 * This specification defines a hierarchical structure used for deployment and
 * packaging purposes that can exist in an open file system, in an archive file, or in
 * some other form. It is recommended, but not required, that servlet containers
 * support this structure as a runtime representation.
 * <p/>
 * A WAB provides such a view of web application which is actually composed of a host OSGi bundle and zero or
 * more attached fragment bundles.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class WAB implements ReadableArchive {
    // Implementation Notes:
    // We don't create virtual jar from directory type Bundle-ClassPath entry, because rfc #66 says that
    // such entries should be treated like WEB-INF/classes/, which means, they must not be searched for
    // web-fragments.xml.

    // TODO(Sahoo): Lazy population of entries

    private Bundle host;
    private Bundle[] fragments;
    private Map<String, ArchiveEntry> entries = new HashMap<String, ArchiveEntry>();

    /**
     * All Bundle-ClassPath entries of type jars are represented as WEB-INF/lib/{N}.jar,
     * where N is a number starting with 0.
     */
    private final static String LIB_DIR = "WEB-INF/lib/";
    private final static String JAR_EXT = ".jar";
    private final static String CLASSES_DIR = "WEB-INF/classes/";
    private static final String DOT = ".";

    private final Map<Bundle, OSGiBundleArchive> archives;

    public WAB(Bundle host, Bundle[] fragments) {
        this.host = host;
        this.fragments = fragments!=null ? fragments : new Bundle[0];
        archives = new HashMap<Bundle, OSGiBundleArchive>(this.fragments.length + 1);
        init();
    }


    private synchronized OSGiBundleArchive getArchive(Bundle b) {
        OSGiBundleArchive archive = archives.get(b);
        if (archive == null) {
            archive = new OSGiBundleArchive(b);
            archives.put(b, archive);
        }
        return archive;
    }

    private synchronized void init() {
        List<Bundle> bundles = new ArrayList(Arrays.asList(fragments));
        bundles.add(0, host);
        for(Bundle b : bundles) {
            final OSGiBundleArchive archive = getArchive(b);
            for(final String entry : Collections.list(archive.entries())) {
                if(entries.containsKey(entry)) continue; // encountering second time - ignore
                ArchiveEntry archiveEntry = new ArchiveEntry() {
                    public String getName() {
                        return entry;
                    }

                    public URI getURI() throws URISyntaxException {
                        return archive.getEntryURI(entry);
                    }

                    public InputStream getInputStream() throws IOException {
                        return archive.getEntry(entry);
                    }
                };
                entries.put(entry, archiveEntry);
            }
        }

        final EffectiveBCP bcp = getEffectiveBCP();
        bcp.accept(new BCPEntry.BCPEntryVisitor() {
            private int i = 0;

            public void visitDir(final DirBCPEntry bcpEntry) {
                try {
                    // do special processing if the dir name is not WEB-INF/classes/
                    if (bcpEntry.getName().equals(CLASSES_DIR)) return;
                    final Archive subArchive = getArchive(bcpEntry.getBundle()).getSubArchive(bcpEntry.getName());
                    for (final String subEntry : Collections.list(subArchive.entries())) {
                        ArchiveEntry archiveEntry = new ArchiveEntry() {
                            public String getName() {
                                return CLASSES_DIR + subEntry;
                            }

                            public URI getURI() throws URISyntaxException {
                                return bcpEntry.getBundle().getEntry(bcpEntry.getName() + subEntry).toURI();
                            }

                            public InputStream getInputStream() throws IOException {
                                try {
                                    return getURI().toURL().openStream();
                                } catch (URISyntaxException e) {
                                    throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                                }
                            }
                        };
                        entries.put(archiveEntry.getName(), archiveEntry);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                }
            }

            public void visitJar(final JarBCPEntry bcpEntry) {
                // do special processing if the jar does not belong to WEB-INF/lib/
                if (bcpEntry.getName().startsWith(LIB_DIR) && bcpEntry.getName().endsWith(JAR_EXT)) {
                    String jarName = bcpEntry.getName().substring(LIB_DIR.length());
                    if(jarName.indexOf("/") == -1) {
                        return; // This jar is already first level jar in WEB-INF/lib
                    }
                }

                // do special processing for Bundle-ClassPath DOT
                if (bcpEntry.getName().equals(DOT)) {
                    final String newJarName = LIB_DIR + "Bundle" + bcpEntry.getBundle().getBundleId() + JAR_EXT;
                    entries.put(newJarName, new ArchiveEntry(){
                        public String getName() {
                            return newJarName;
                        }

                        public URI getURI() throws URISyntaxException {
                            return getArchive(bcpEntry.getBundle()).getURI();
                        }

                        public InputStream getInputStream() throws IOException {
                            return getArchive(bcpEntry.getBundle()).getInputStream();
                        }
                    });
                } else {
                    final String newJarName = LIB_DIR + "Bundle" + bcpEntry.getBundle().getBundleId() + "-" +
                            bcpEntry.getName().replace('/', '-') + JAR_EXT;
                    entries.put(newJarName, new ArchiveEntry() {
                        public String getName() {
                            return newJarName;
                        }

                        public URI getURI() throws URISyntaxException {
                            return bcpEntry.getBundle().getEntry(bcpEntry.getName()).toURI();
                        }

                        public InputStream getInputStream() throws IOException {
                            try {
                                return getURI().toURL().openStream();
                            } catch (URISyntaxException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            }
        });

    }

    private EffectiveBCP getEffectiveBCP() {
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
        return new Manifest(getEntry(JarFile.MANIFEST_NAME));
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

    interface ArchiveEntry {
        String getName();

        URI getURI() throws URISyntaxException;

        InputStream getInputStream() throws IOException;
    }

    interface BCPEntry {

        /**
         * @return path relative to its bundle.
         */
        String getName();

        /**
         * @return the bundle this entry belongs to. Please note, a host bundle can insert a classpath entry
         *         into a fragment bundle.
         */
        Bundle getBundle();

        void accept(BCPEntryVisitor visitor);

        interface BCPEntryVisitor {
            void visitDir(DirBCPEntry bcpEntry);

            void visitJar(JarBCPEntry bcpEntry);
        }
    }

    class DirBCPEntry implements BCPEntry {
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

    class JarBCPEntry implements BCPEntry {
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

    class EffectiveBCP {
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
                for (Bundle b : bundles) {
                    OSGiBundleArchive archive = getArchive(b);
                    if (archive.exists(s)) {
                        if (archive.isDirectory(s)) {
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
                OSGiBundleArchive archive = getArchive(bundle);
                if (DOT.equals(s)) {
                    result.add(createJarBCPEntry(DOT, bundle));
                } else if (archive.exists(s)) {
                    if (archive.isDirectory(s)) {
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
