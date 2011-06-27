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

import org.glassfish.api.deployment.archive.Archive;
import org.glassfish.api.deployment.archive.ReadableArchive;
import static org.glassfish.osgijavaeebase.Constants.FILE_PROTOCOL;
import static org.glassfish.osgijavaeebase.Constants.REFERENCE_PROTOCOL;
import org.osgi.framework.Bundle;
import static org.osgi.framework.Constants.BUNDLE_VERSION;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import com.sun.enterprise.deploy.shared.AbstractReadableArchive;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.jar.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Adapts a {@link Bundle} to {@link Archive}.
 * It uses JAR File space of the bundle (via getEntry and getEntryPaths APIs),
 * so a bundle does not have to be in resolved state.
 * Since it represents JAR File space of the bundle, it does not
 * consider resources from any fragments.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiBundleArchive extends AbstractReadableArchive implements ReadableArchive, URIable, Iterable<BundleResource> {
    private Bundle b;

    private String name;

    private URI uri;

    private Map<String, ReadableArchive> subArchives = new HashMap<String, ReadableArchive>();

    public OSGiBundleArchive(Bundle b) {
        this.b = b;
        init();
    }

    /**
     * This method initializes {@link #uri} and {@link #name}
     */
    private void init() {
        // The only time we can rely on a bundle's location is when the
        // location string begins with reference: scheme, as both Felix and
        // equinox assumes that the rest of the location is a file.
        // In no other case, we can use rely on bundle.getLocation()
        // to arrive at the URI of the underlying archive.
        // e.g., user can install like this:
        // bundleContext.install ("file:/a/b.jar", new URL("file:/c/d.jar").openStream));
        // In the above case, although location returns a.jar, the actual archive
        // is read from d.jar.
        // So, we return a valid URI only for reference: scheme and in all
        // cases, we prefer to return null as opposed to throwing an exception
        // to keep the behavior same as MemoryMappedArchive.
        String location = b.getLocation();
        if (location != null && location.startsWith(REFERENCE_PROTOCOL)) {
            location = location.substring(REFERENCE_PROTOCOL.length());

            // We only know how to handle reference:file: type urls.
            if (location.startsWith(FILE_PROTOCOL)) {

                // Decode any URL escaped sequences.
                location = URLDecoder.decode(location);

                // Return iff referenced file exists.
                File file = new File(location.substring(FILE_PROTOCOL.length()));
                if (file.exists()) {
                    uri = file.toURI();
                }
            }
        }

        // See issue #10536. We can't use the same policy for obtaining
        // the name as OSGi container does.
//        if (uri != null) {
//            name = Util.getURIName(uri);
//        } else {
        // See if there is a symbolic name & version. Use them,
        // else use location. Either symbolic name or location must exist
        // in a bundle.
        String symName = b.getSymbolicName();
        String version = (String) b.getHeaders().get(BUNDLE_VERSION);
        if (symName != null) {
            name = version == null ?
                    symName : symName.concat("_").concat(version);
        } else {
            name = location;
        }
//        }
    }

    public void close() throws IOException {
    }

    public Enumeration<String> entries() {
        ArrayList<String> entries = new ArrayList<String>();
        getEntryPaths(entries, "/");
        ListIterator<String> entriesIter = entries.listIterator();
        while (entriesIter.hasNext()) {
            String next = entriesIter.next();
            if (next.endsWith("/")) {
                // return only file entries as per the conract of this method
                entriesIter.remove();
            }
        }
        return Collections.enumeration(entries);
    }

    /**
     * Returns the enumeration of first level directories in this
     * archive
     *
     * @return enumeration of directories under the root of this archive
     */
    public Collection<String> getDirectories() throws IOException {
        return getSubDiretcories("/");
    }

    /**
     * Return subdirectories under a given path. This returns only result from one level, i.e., non-recurssive
     *
     * @param path
     * @return
     */
    private Collection<String> getSubDiretcories(String path) {
        final Enumeration firstLevelEntries = b.getEntryPaths(path);
        if (firstLevelEntries == null) return Collections.EMPTY_LIST;
        Collection<String> firstLevelDirs = new ArrayList<String>();
        while (firstLevelEntries.hasMoreElements()) {
            String firstLevelEntry = (String) firstLevelEntries.nextElement();
            if (firstLevelEntry.endsWith("/")) firstLevelDirs.add(firstLevelEntry);
        }
        return firstLevelDirs;
    }

    private void getEntryPaths(Collection<String> entries, String path) {
        Enumeration<String> subPaths = b.getEntryPaths(path);
        if (subPaths != null) {
            while (subPaths.hasMoreElements()) {
                String next = subPaths.nextElement();
                entries.add(next);
                getEntryPaths(entries, next);
            }
        }
//        BECAUSE OF A BUG IN FELIX (FELIX-1210), THE CODE ABOVE DOES NOT WORK
//        WHEN THERE ARE NO DIRECTORY ENTRIES IN THE JAR FILE.
//        IF WE CONSISTENTLY FACE THE ISSUE, THEN WE CAN USE AN ALTERNATIVE IMPL BASED ON findEntries.
//        OF COURSE, IT WILL HAVE THE UNDESIRED SIDE EFFECT OF FINDINDG ENTRIES FROM FRAGMENTS AS WELL.
//        WE HAVE NASTY SIDE EFFECTS WHEN THAT HAPPENS. e.g. NPE. SO, WE DON'T USE THE ALTERNATIVE
//        IMPLEMENTATION ANY MORE. WE EXPECT JAR TO HAVE PROPER DIRECTORY ENTRIES.        
//        getEntryPaths2(entries, path); // call the new implementation
    }

    private void getEntryPaths2(Collection<String> entries, String path) {
        // findEntries expect the path to begin with "/"
        Enumeration e = b.findEntries(
                path.startsWith("/") ? path : "/".concat(path), "*", true);
        if (e != null) {
            while (e.hasMoreElements()) {
                URL next = (URL) e.nextElement();
                String nextPath = next.getPath();
                // As per the OSGi R4 spec,
                // "The getPath method for a bundle entry URL must return
                // an absolute path (a path that starts with '/') to a resource
                // or entry in a bundle. For example, the URL returned from
                // getEntry("myimages/test .gif ") must have a path of
                // /myimages/test.gif.
                entries.add(nextPath.substring(1)); // remove the leading "/"
            }
        }
    }

    public Enumeration<String> entries(String prefix) {
        Collection<String> entries = new ArrayList<String>();
        getEntryPaths(entries, prefix);
        return Collections.enumeration(entries);
    }

    public boolean isDirectory(String name) {
        return b.getEntry(name.endsWith("/") ? name : name + "/") != null;
    }

    public Manifest getManifest() throws IOException {
        URL url = b.getEntry(JarFile.MANIFEST_NAME);
        if (url != null) {
            InputStream is = url.openStream();
            try {
                return new Manifest(is);
            }
            finally {
                is.close();
            }

        }
        return null;
    }

    /**
     * It returns URI for the underlying file if it can locate such a file.
     * Else, it returns null.
     *
     * @return
     */
    public URI getURI() {
        return uri;
    }

    public long getArchiveSize() throws SecurityException {
        return -1; // Don't know how to calculate the size.
    }

    public String getName() {
        return name;
    }

    public InputStream getEntry(String name) throws IOException {
        URL entry = b.getEntry(name);
        return entry != null ? entry.openStream() : null;
    }

    public boolean exists(String name) {
        return b.getEntry(name) != null;
    }

    public long getEntrySize(String name) {
        return 0;
    }

    public void open(URI uri) throws IOException {
        throw new UnsupportedOperationException("Not applicable method");
    }

    public ReadableArchive getSubArchive(String name) throws IOException {
        if (!exists(name)) {
            return null;
        }
        synchronized (this) {
            if (!subArchives.containsKey(name)) {
                ReadableArchive subArchive =
                        isDirectory(name) ? new EmbeddedDirectoryArchive(name) : new EmbeddedJarArchive(name);
                subArchives.put(name, subArchive);
            }
            return subArchives.get(name);
        }
    }

    public boolean exists() {
        return true;
    }

    public boolean delete() {
        return false;
    }

    public boolean renameTo(String name) {
        return false;
    }

    public void setParentArchive(ReadableArchive parentArchive) {
        // Not needed until we support ear file containing bundles.
        throw new UnsupportedOperationException("Not supported");
    }

    public ReadableArchive getParentArchive() {
        return null;
    }

    public URI getEntryURI(String name) {
        try {
            return b.getEntry(name).toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDistanceFromTop() {
        return ""; // this is the top level archive
    }

    public Iterator<BundleResource> iterator() {
        return new BundleResourceIterator();
    }

    /**
     *
     * @return a Jar format InputStream for this bundle's content
     */
    public InputStream getInputStream() throws IOException {
        if (uri != null) {
            return uri.toURL().openStream();
        } else {
            // create a JarOutputStream on the fly from the bundle's content
            // Can we optimize by reading off Felix's cache? Investigate in future.
            PipedInputStream is = new PipedInputStream();
            final PipedOutputStream os = new PipedOutputStream(is);
            new Thread() {
                @Override
                public void run() {
                    try {
                        JarOutputStream jos = new JarOutputStream(os, getManifest());
                        ByteBuffer buf = ByteBuffer.allocate(1024);
                        for (String s : Collections.list(entries())) {
                            if (s.equals(JarFile.MANIFEST_NAME)) continue; // we have already inserted manifest
                            jos.putNextEntry(new JarEntry(s));
                            if (!isDirectory(s)) {
                                InputStream in = getEntry(s);
                                try {
                                    JarHelper.copy(in, jos, buf);
                                } finally {
                                    try {
                                        in.close();
                                    } catch (IOException e) {
                                        // ignore
                                    }
                                }
                            }
                            jos.closeEntry();
                        }
                        jos.close();
                        os.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                    }

                }
            }.start();
            return is;
        }
    }

    /**
     * A directory (typically a bundle classpath) in the bundle represented as an archive.
     */
    private class EmbeddedDirectoryArchive extends AbstractReadableArchive implements ReadableArchive, URIable {

        /**
         * This is the entry name by which this is identified in the bundle space.
         */
        private String distanceFromTop;

        public EmbeddedDirectoryArchive(String distanceFromTop) {
            this.distanceFromTop = distanceFromTop;
        }

        public InputStream getEntry(String name) throws IOException {
            if (!exists(name)) return null;
            String bundleEntry = distanceFromTop + name;
            return b.getEntry(bundleEntry).openStream();
        }

        public boolean exists(String name) {
            return OSGiBundleArchive.this.exists(distanceFromTop + name);
        }

        public long getEntrySize(String name) {
            return OSGiBundleArchive.this.getEntrySize(distanceFromTop + name);
        }

        public void open(URI uri) throws IOException {
            throw new UnsupportedOperationException();
        }

        public ReadableArchive getSubArchive(String name) throws IOException {
            return null;
        }

        public boolean exists() {
            return true;
        }

        public boolean delete() {
            return false;
        }

        public boolean renameTo(String name) {
            return false;
        }

        public void setParentArchive(ReadableArchive parentArchive) {
            throw new UnsupportedOperationException();
        }

        public ReadableArchive getParentArchive() {
            return OSGiBundleArchive.this;
        }

        public void close() throws IOException {
        }

        public Enumeration<String> entries() {
            return entries("");
        }

        public Enumeration<String> entries(String prefix) {
            Collection<String> entries = new ArrayList<String>();
            getEntryPaths(entries, distanceFromTop + prefix);

            // entries contains path names which is with respect to bundle root.
            // We need names with respect to this directory root.
            // So, we need to strip entryName from the entries.
            Collection<String> subEntries = stripEntryName(entries);
            return Collections.enumeration(subEntries);
        }

        /**
         * This method strips off entryName from collection of entries.
         * @param entries
         * @return
         */
        private Collection<String> stripEntryName(Collection<String> entries) {
            Collection<String> subEntries = new ArrayList<String>(entries.size());
            final int idx = distanceFromTop.length();
            for (String entry : entries) {
                subEntries.add(entry.substring(idx));
            }
            return subEntries;
        }

        public Collection<String> getDirectories() throws IOException {
            return stripEntryName(getSubDiretcories(distanceFromTop));
        }

        public boolean isDirectory(String name) {
            return exists(name.endsWith("/") ? name : name + "/");
        }

        public Manifest getManifest() throws IOException {
            return null;  //TODO(Sahoo): Not Yet Implemented
        }

        public URI getURI() {
            try {
                return b.getEntry(distanceFromTop).toURI();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        public long getArchiveSize() throws SecurityException {
            return 0;
        }

        public String getName() {
            return distanceFromTop;
        }

        public URI getEntryURI(String name) {
            return OSGiBundleArchive.this.getEntryURI(distanceFromTop + name);
        }

        public String getDistanceFromTop() {
            return distanceFromTop;
        }
    }

    /**
     * A jar (typically a bundle classpath) in the bundle represented as an archive.
     */
    private class EmbeddedJarArchive extends AbstractReadableArchive implements ReadableArchive, URIable {

        /**
         * This is the entry name by which this is identified in the bundle space.
         */
        private String distanceFromTop;

        /**
         * All the entries that this archive has
         */
        private List<String> entries = new ArrayList<String>();

        private EmbeddedJarArchive(String distanceFromTop) throws IOException {
            this.distanceFromTop = distanceFromTop;
            ZipInputStream zis = getZIS();
            try {
                while (true) {
                    ZipEntry ze = zis.getNextEntry();
                    if (ze == null) break;
                    entries.add(ze.getName());
                }
            } finally {
                closeZIS(zis);
            }
        }

        private ZipInputStream getZIS() throws IOException {
            // Since user can supply random entry and ask for an embedded archive, propagate the exception to user.
            return new ZipInputStream(b.getEntry(distanceFromTop).openStream());
        }

        private Collection<String> getEntries() {
            return entries;
        }

        public InputStream getEntry(String name) throws IOException {
            if (!exists(name)) return null;
            final ZipInputStream zis = getZIS();
            while (true) {
                ZipEntry ze = zis.getNextEntry();
                if (ze == null) break; // end of stream, which is unlikely because the entry exists.
                if (ze.getName().equals(name)) return zis;
            }
            // don't close the stream, as we are returning it to caller
            assert (false);
            return null;
        }

        public boolean exists(String name) {
            return getEntries().contains(name);
        }

        public long getEntrySize(String name) {
            if (exists(name)) {
                ZipInputStream zis = null;
                try {
                    zis = getZIS();
                    while (true) {
                        ZipEntry ze = zis.getNextEntry();
                        if (name.equals(ze.getName())) {
                            return ze.getSize();
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }  finally {
                    if (zis != null) {
                        closeZIS(zis);
                    }
                }
            }
            return 0;
        }

        public void open(URI uri) throws IOException {
            throw new UnsupportedOperationException("Not Applicable");
        }

        public ReadableArchive getSubArchive(String name) throws IOException {
            return null;  // Only one level embedding allowed in a bundle
        }

        public boolean exists() {
            return true;
        }

        public boolean delete() {
            return false;  //TODO(Sahoo): Not Yet Implemented
        }

        public boolean renameTo(String name) {
            return false;  //TODO(Sahoo): Not Yet Implemented
        }

        public void setParentArchive(ReadableArchive parentArchive) {
            throw new UnsupportedOperationException();
        }

        public ReadableArchive getParentArchive() {
            return OSGiBundleArchive.this;
        }

        public void close() throws IOException {
            // noop
        }

        public Enumeration<String> entries() {
            return Collections.enumeration(getEntries());
        }

        public Enumeration<String> entries(String prefix) {
            List<String> result = new ArrayList<String>();
            for (String entry : getEntries()) {
                if (entry.startsWith(prefix)) {
                    result.add(entry);
                }
            }
            return Collections.enumeration(result);
        }

        public Collection<String> getDirectories() throws IOException {
            List<String> result = new ArrayList<String>();
            for (String entry : getEntries()) {
                final int idx = entry.indexOf('/');
                if (idx != -1 && idx == entry.length() - 1) result.add(entry);
            }
            return result;
        }

        public boolean isDirectory(String name) {
            // directory entries always end with "/", so unless we append a "/" when not there, we are not going
            // to find it in our entry list.
            return exists(name.endsWith("/") ? name : (name + "/"));
        }

        public Manifest getManifest() throws IOException {
            String name = JarFile.MANIFEST_NAME;
            return exists(name) ? new Manifest(getEntry(name)) : null;
        }

        public URI getURI() {
            try {
                return b.getEntry(distanceFromTop).toURI();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        public long getArchiveSize() throws SecurityException {
            return 0; // unknown
        }

        public String getName() {
            return distanceFromTop;
        }

        public URI getEntryURI(String name) {
            return URI.create(EmbeddedJarURLStreamHandlerService.EMBEDDED_JAR_SCHEME + ":" + getURI() +
                    EmbeddedJarURLStreamHandlerService.SEPARATOR + name);
        }

        public String getDistanceFromTop() {
            return distanceFromTop;
        }

        private void closeZIS(ZipInputStream zis) {
            try {
                zis.close();
            } catch (Exception e) {
            }
        }
    }

    private class BundleResourceIterator implements Iterator<BundleResource> {

        private static final String DOT = ".";
        private final Iterator<BundleResource> delegate;
        private final Collection<BundleResource> bundleResources = new ArrayList<BundleResource>();

        private BundleResourceIterator() {
            // for each bundle classpath entry, get the subarchive
            String bcp = (String) b.getHeaders().get(org.osgi.framework.Constants.BUNDLE_CLASSPATH);
            if (bcp == null || bcp.isEmpty()) bcp = DOT;
            String seps = ";,";
            StringTokenizer bcpes = new StringTokenizer(bcp, seps);
            List<ReadableArchive> archives = new ArrayList<ReadableArchive>();
            while (bcpes.hasMoreTokens()) {
                String bcpe = bcpes.nextToken();
                bcpe = bcpe.trim();
                if (bcpe.startsWith("/")) bcpe = bcpe.substring(1); // it is always relative to bundle root
                if (bcpe.equals(DOT)) {
                    archives.add(OSGiBundleArchive.this);
                } else {
                    if (isDirectory(bcpe) && !bcpe.endsWith("/")) {
                        bcpe = bcpe.concat("/");
                    }
                    try {
                        ReadableArchive archive = getSubArchive(bcpe);
                        if (archive != null) archives.add(archive);
                    } catch (IOException e1) {
                        e1.printStackTrace(); // ignore and continue
                    }
                }
            }

            for (ReadableArchive archive : archives) {
                Enumeration<String> entries = archive.entries();
                final URIable urIable = URIable.class.cast(archive);
                while (entries.hasMoreElements()) {
                    String entry = entries.nextElement();
                    URI uri = urIable.getEntryURI(entry);
                    final String archivePath = urIable.getDistanceFromTop();
                    BundleResource bundleResource = new BundleResource(uri, entry, archivePath);
                    bundleResources.add(bundleResource);
                }
            }
            delegate = bundleResources.iterator();
        }

        public boolean hasNext() {
            return delegate.hasNext();
        }

        public BundleResource next() {
            return delegate.next();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    static class EmbeddedJarURLStreamHandlerService extends
            AbstractURLStreamHandlerService {
        /**
         * URI scheme used for resource embedded in a jar in a bundle
         */
        static final String EMBEDDED_JAR_SCHEME = "embeddedjar";

        /**
         * Separator used by embeddedjar scheme.
         */
        static final String SEPARATOR = "!/";

        public URLConnection openConnection(URL u) throws IOException {
            assert (u.getProtocol().equals(EMBEDDED_JAR_SCHEME));
            try {
                String schemeSpecificPart = u.toURI().getSchemeSpecificPart();
                int idx = schemeSpecificPart.indexOf(SEPARATOR);
                assert (idx > 0);
                URL embeddedURL = URI.create(schemeSpecificPart.substring(0, idx)).toURL();
                final URLConnection con = embeddedURL.openConnection();
                final String entryPath = schemeSpecificPart.substring(idx + 2);
                assert (entryPath.length() > 0);
                return new URLConnection(u) {
                    public void connect() throws IOException {
                        con.connect();
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        JarInputStream jis = new JarInputStream(con.getInputStream());
                        for (JarEntry je = jis.getNextJarEntry(); je != null; je = jis.getNextJarEntry()) {
                            if (je.getName().equals(entryPath)) {
                                return jis;
                            }
                        }
                        throw new IOException("No entry by name " + entryPath);
                    }
                };
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
