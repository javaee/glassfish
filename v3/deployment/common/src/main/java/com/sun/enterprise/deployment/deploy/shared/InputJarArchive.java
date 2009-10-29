/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.deployment.deploy.shared;

import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.io.FileUtils;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This implementation of the Archive deal with reading
 * jar files either from a JarFile or from a JarInputStream
 *
 * @author Jerome Dochez
 */
@Service(name="jar")
@Scoped(PerLookup.class)
public class InputJarArchive extends JarArchive implements ReadableArchive {
    
    final static Logger logger = LogDomains.getLogger(DeploymentUtils.class, LogDomains.DPL_LOGGER);

    // the file we are currently mapped to 
    protected JarFile jarFile=null;
    
    // in case this abstraction is dealing with a jar file
    // within a jar file, the jarFile will be null and this
    // JarInputStream will contain the 
    protected JarInputStream jarIS=null; 
    
    // the archive Uri
    private URI uri;

    // parent jar file for embedded jar
    private InputJarArchive parentArchive=null;

    private StringManager localStrings = StringManager.getManager(getClass());
    
    /**
     * Get the size of the archive
     * @return tje the size of this archive or -1 on error
     */
    public long getArchiveSize() throws NullPointerException, SecurityException {
        if(uri == null) {
            return -1;
        }
        File tmpFile = new File(uri);
        return(tmpFile.length());
    }
    
    /** @return an @see java.io.OutputStream for a new entry in this
     * current abstract archive.
     * @param name the entry name
     */
    public OutputStream addEntry(String name) throws IOException {
        throw new UnsupportedOperationException("Cannot write to an JAR archive open for reading");        
    }
    
    /** 
     * close the abstract archive
     */
    public void close() throws IOException {
        if (jarFile!=null) {
            jarFile.close();
            jarFile=null;
        }
        if (jarIS!=null) {
            jarIS.close();
            jarIS=null;
        }
    }

    /**
     * Returns the collection of first level directories in this
     * archive.
     * <p>
     * Avoid having to fetch all the entries if we can avoid it.  The only time
     * we must do that is if size() is invoked on the collection.  Use
     * the CollectionWrappedEnumeration for this optimization which will
     * create a full in-memory list of the entries only if and when needed
     * to satisfy the size() method.
     *
     * @return collection of directories under the root of this archive
     */
    @Override
    public Collection<String> getDirectories() throws IOException {
        return new CollectionWrappedEnumeration<String>(
                new CollectionWrappedEnumeration.EnumerationFactory<String>() {

            @Override
            public Enumeration<String> enumeration() {
                return entries(true);
            }
        });
    }

    /** 
     * creates a new abstract archive with the given path
     *
     * @param uri the path to create the archive
     */
    public void create(URI uri) throws IOException {
        throw new UnsupportedOperationException("Cannot write to an JAR archive open for reading");        
    }

    @Override
    public Enumeration<String> entries() {
        return entries(false);
    }

    /**
     * Returns an enumeration of the entry names in the archive.
     *
     * @param directoriesOnly whether to report directories only or non-directories only
     * @return enumeration of the matching entry names, excluding the manifest
     */
    private Enumeration<String> entries(final boolean directoriesOnly) {
        if (parentArchive != null) {
            try {
                return new SubarchiveEntryEnumeration(directoriesOnly);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            try {
                if (jarFile == null) {
                    getJarFile(uri);
                }
            } catch (IOException ioe) {
                return Collections.enumeration(Collections.EMPTY_LIST);
            }
            if (jarFile == null) {
                return Collections.enumeration(Collections.EMPTY_LIST);
            }

            return new TopLevelEntryEnumeration(directoriesOnly);
        }
    }
    
    
    /**
     *  @return an @see java.util.Enumeration of entries in this abstract
     * archive, providing the list of embedded archive to not count their 
     * entries as part of this archive
     */
     public Enumeration entries(Enumeration embeddedArchives) {
	// jar file are not recursive    
  	return entries();
    }

    public JarEntry getJarEntry(String name) {
        if (jarFile!=null) {
            return jarFile.getJarEntry(name);
        }
        return null;
    }
    
    /**
     * Returns the existence of the given entry name
     * The file name must be relative to the root of the module.
     *
     * @param name the file name relative to the root of the module.          * @return the existence the given entry name.
     */
    public boolean exists(String name) throws IOException {
        if (jarFile!=null) {
            ZipEntry ze = jarFile.getEntry(name);
            if (ze!=null) {
                return true;
            }
        }
        return false;
    }    

    /**
     * @return a @see java.io.InputStream for an existing entry in
     * the current abstract archive
     * @param entryName entry name
     */
    public InputStream getEntry(String entryName) throws IOException {
        if (jarFile!=null) {
            ZipEntry ze = jarFile.getEntry(entryName);
            if (ze!=null) {
                return new BufferedInputStream(jarFile.getInputStream(ze));
            } else {
                return null;
            }            
        } else
	if ((parentArchive != null) && (parentArchive.jarFile != null)) {
            JarEntry je;
            // close the current input stream
            if (jarIS!=null) {
                jarIS.close();
            }
            
            // reopen the embedded archive and position the input stream
            // at the beginning of the desired element
	    JarEntry archiveJarEntry = (uri != null)? parentArchive.jarFile.getJarEntry(uri.getSchemeSpecificPart()) : null;
	    if (archiveJarEntry == null) {
		return null;
	    }
            jarIS = new JarInputStream(parentArchive.jarFile.getInputStream(archiveJarEntry));
            do {
                je = jarIS.getNextJarEntry();
            } while (je!=null && !je.getName().equals(entryName));
            if (je!=null) {
                return new BufferedInputStream(jarIS);
            } else {
                return null;
            }
        } else {
	    return null;
	}
    }

    /**
     * Returns the entry size for a given entry name or 0 if not known
     *
     * @param name the entry name
     * @return the entry size
     */
    public long getEntrySize(String name) {
        if (jarFile!=null) {
            ZipEntry ze = jarFile.getEntry(name);
            if (ze!=null) {
                return ze.getSize();
            }
        }
        return 0;
    }

    /** Open an abstract archive
     * @param uri the path to the archive
     */
    public void open(URI uri) throws IOException {
       this.uri = uri;
       jarFile = getJarFile(uri);
    }
    
    /**
     * @return a JarFile instance for a file path
     */
    protected JarFile getJarFile(URI uri) throws IOException {
        if (!uri.getScheme().equals("jar")) {
            throw new IOException("Wrong scheme for InputJarArchive : " + uri.getScheme());
        }
        jarFile = null;
        try {
            File file = new File(uri.getSchemeSpecificPart());
            if (file.exists()) {
                jarFile = new JarFile(file);
            }
        } catch(IOException e) {
            logger.log(Level.WARNING,
                "enterprise.deployment.backend.fileOpenFailure", 
                new Object[]{uri});
            // add the additional information about the path
            // since the IOException from jdk doesn't include that info
            String additionalInfo = localStrings.getString(
                "enterprise.deployment.invalid_zip_file", uri);
            logger.log(Level.WARNING,
                e.getLocalizedMessage() + " --  " + additionalInfo);
        }
        return jarFile;
    }       
    
    
    /** 
     * @return the manifest information for this abstract archive
     */
    public Manifest getManifest() throws IOException {
        if (jarFile!=null) {
            return jarFile.getManifest();
        } 
        if (parentArchive!=null) {    
            // close the current input stream
            if (jarIS!=null) {
                jarIS.close();
            }
            // reopen the embedded archive and position the input stream
            // at the beginning of the desired element
            if (jarIS==null) {
                jarIS = new JarInputStream(parentArchive.jarFile.getInputStream(parentArchive.jarFile.getJarEntry(uri.getSchemeSpecificPart())));
            }
            Manifest m = jarIS.getManifest();
            if (m==null) {
               java.io.InputStream is = getEntry(java.util.jar.JarFile.MANIFEST_NAME);
               if (is!=null) {
                    m = new Manifest();
                    m.read(is);
                    is.close();
               }
            }
            return m;
        }                        
        return null;
    }

    /**
     * Returns the path used to create or open the underlying archive
     *
     * @return the path for this archive.
     */
    public URI getURI() {
        return uri;
    }

    /**
     * @return true if this abstract archive maps to an existing 
     * jar file
     */
    public boolean exists() {
        return jarFile!=null;
    }
    
    /**
     * deletes the underlying jar file
     */
    public boolean delete() {
        if (jarFile==null) {
            return false;
        }
        try {
            jarFile.close();
            jarFile = null;
        } catch (IOException ioe) {
            return false;
        }
        return FileUtils.deleteFile(new File(uri));
    }
    
    /**
     * rename the underlying jar file
     */
    public boolean renameTo(String name) {
        if (jarFile==null) {
            return false;
        }
        try {
            jarFile.close();
            jarFile = null;
        } catch (IOException ioe) {
            return false;
        }        
        return FileUtils.renameFile(new File(uri), new File(name));
    }
    
    /**
     * @return an Archive for an embedded archive indentified with
     * the name parameter
     */
    public ReadableArchive getSubArchive(String name) throws IOException {
        if (jarFile!=null) {
            // for now, I only support one level down embedded archives
            InputJarArchive ija = new InputJarArchive();
            JarEntry je = jarFile.getJarEntry(name);
            if (je!=null) {
                JarInputStream jis = new JarInputStream(new BufferedInputStream(jarFile.getInputStream(je)));
                try {
                    ija.uri = new URI("jar",name, null);
                } catch(URISyntaxException e) {
                    // do nothing
                }
                ija.jarIS = jis;
                ija.parentArchive = this;
                return ija;
            }
        }
        return null;
    }
    
    /**
     * Logic for enumerations of the entry names that is common between the
     * top-level archive implementation and the subarchive implementation.
     * <p>
     * The goal is to wrap an Enumeration around the underlying entries 
     * available in the archive, whether the archive is a top-level archive or a
     * subarchive within a parent archive.  This avoids collecting all
     * the entry names first and then returning an enumeration of the collection;
     * that can be very costly for large JARs.
     */
    private abstract class EntryEnumeration implements Enumeration<String> {

        /** look-ahead of one entry */
        private JarEntry nextMatchingEntry;
        private final boolean directoriesOnly;

        private EntryEnumeration(final boolean directoriesOnly) {
            this.directoriesOnly = directoriesOnly;
        }

        /**
         * Finishes the initialization for the enumeration; MUST be invoked
         * from the subclass constructor after super(...).
         */
        protected void completeInit() {
            nextMatchingEntry = skipToNextMatchingEntry();
        }

        @Override
        public boolean hasMoreElements() {
            return nextMatchingEntry != null;
        }

        @Override
        public String nextElement() {
            if (nextMatchingEntry == null) {
                throw new NoSuchElementException();
            }
            final String answer = nextMatchingEntry.getName();
            nextMatchingEntry = skipToNextMatchingEntry();
            return answer;
        }

        /**
         * Returns the next JarEntry available from the archive.
         * @return the next available JarEntry; null if no more are available
         */
        protected abstract JarEntry getNextJarEntry();

        protected JarEntry skipToNextMatchingEntry() {
            JarEntry candidateNextEntry;
            while ((candidateNextEntry = getNextJarEntry()) != null) {
                if (directoriesOnly == candidateNextEntry.isDirectory() &&
                       ! candidateNextEntry.getName().equals(JarFile.MANIFEST_NAME)) {
                    break;
                }
            }
            return candidateNextEntry;
        }
    }

    /**
     * Enumerates the entries from a top-level archive (as opposed to a
     * subarchive within a parent archive).
     * <p>
     * This implementation uses the enumeration of JarEntry objects from the
     * JarFile itself.
     */
    private class TopLevelEntryEnumeration extends EntryEnumeration {
        private final Enumeration<JarEntry> jarEnum = jarFile.entries();

        private TopLevelEntryEnumeration(final boolean directoriesOnly) {
            super(directoriesOnly);
            completeInit();
        }

        @Override
        protected JarEntry getNextJarEntry() {
            if (jarEnum.hasMoreElements()) {
                return jarEnum.nextElement();
            } else {
                return null;
            }
        }
    }

    /**
     * Enumerates the entries from a sub-archive.
     * <p>
     * This implementation uses a JarInputStream to obtain successive
     * JarEntry objects from the subarchive, via the parent archive.
     */
    private class SubarchiveEntryEnumeration extends EntryEnumeration {

        private final JarInputStream jis;
        private boolean reachedEndOfStream = false;

        private SubarchiveEntryEnumeration(final boolean directoriesOnly) throws IOException {
            super(directoriesOnly);
            jis = new JarInputStream(parentArchive.jarFile.getInputStream(
                    parentArchive.jarFile.getJarEntry(uri.getSchemeSpecificPart())));
            completeInit();
        }

        @Override
        protected JarEntry getNextJarEntry() {
            if (reachedEndOfStream) {
                return null;
            }
            try {
                final JarEntry result = jis.getNextJarEntry();
                if (result == null) {
                    jis.close();
                    reachedEndOfStream = true;
                }
                return result;
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            if ( ! reachedEndOfStream) {
                jis.close();
            }
        }
    }

    /**
     * <p>
     * Note that the nextSlot field is always updated, even if we are using
     * the original enumeration to return the next value from the iterator.  This
     * is so that, if the caller invokes size() which causes us to build the
     * ArrayList containing all the elements -- even if that invocation comes while
     * the iterator is being used to return values -- the subsequent invocations
     * of hasNext and next will use the correct place in the newly-constructed
     * ArrayList of values.
     *
     * @param <T>
     */
    static class CollectionWrappedEnumeration<T> extends AbstractCollection<T> {

        /** Used only if size is invoked */
        private ArrayList<T> entries = null;

        /** always updated, even if we use the enumeration */
        private int nextSlot = 0;

        private final EnumerationFactory<T> factory;

        private Enumeration<T> e;

        static interface EnumerationFactory<T> {
            public Enumeration<T> enumeration();
        }

        CollectionWrappedEnumeration(final EnumerationFactory<T> factory) {
            this.factory = factory;
            e = factory.enumeration();
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {

                @Override
                public boolean hasNext() {
                    return (entries != null) ?
                        nextSlot < entries.size() :
                        e.hasMoreElements();
                }

                @Override
                public T next() {
                    T result = null;
                    if (entries != null) {
                        if (nextSlot >= entries.size()) {
                            throw new NoSuchElementException();
                        }
                        result = entries.get(nextSlot++);
                    } else {
                        result = e.nextElement();
                        nextSlot++;
                    }
                    return result;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public int size() {
            if (entries == null) {
                populateEntries();
            };
            return entries.size();
        }

        private void populateEntries() {
            entries = new ArrayList<T>();
            /*
             * Fill up the with data from
             * a new enumeration.
             */
            for (Enumeration<T> newE = factory.enumeration(); newE.hasMoreElements(); ) {
                entries.add(newE.nextElement());
            }
            e = null;
        }
    }
}
