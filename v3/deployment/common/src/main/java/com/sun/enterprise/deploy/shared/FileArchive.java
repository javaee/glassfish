/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deploy.shared;

import com.sun.enterprise.deployment.deploy.shared.Util;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.logging.LogDomains;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;

import java.io.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.net.URI;

/**
 * This implementation of the Archive interface maps to a directory/file
 * structure.
 * <p>
 * If the directory underlying the FileArchive is created by GlassFish
 * then FileArchive filters its contents so only
 * those files more recent than the creation of the archive itself are visible to
 * consumers.
 * <p>
 * The main motivation is to hide unwanted "left-over" files
 * from previous deployments that might linger, especially on Windows,
 * after the previous app had been undeployed.  (Deployment uses a FileArchive
 * to extract the user's JAR-based archive into the applications directory.)
 * Historically such left-over files arise after GlassFish expands an archive
 * into its exploded form but then some
 * code opens but does not close a file in that exploded directory tree.
 * <p>
 * An open left-over file can be overwritten-in-place on Windows, and
 * this happens when a caller invokes {@link #putNextEntry(java.lang.String) }
 * to create a new entry (file) inside the archive.  But a
 * left-over file that is not in the new app but is
 * still open by GlassFish cannot be deleted or renamed on Windows and so it will
 * remain in the expansion directory.  Such left-over files, if not filtered out,
 * can confuse GlassFish and the application.  By "stamping" the archive
 * creation date we can filter out such old, left-over files.
 * <p>
 * To support this feature, when FileArchive creates a directory it stores a
 * marker file there, the contents of which records the creation date/time of
 * the archive.  We cannot just use the lastModified value for the top-level
 * directory. Users might legitimately use "touch .reload" in the applications/appName
 * directory to trigger a dynamic reload of the app. If .reload does not already
 * exist then touch creates it, and this would update the lastModified of the
 * directory file.
 *
 * @author Jerome Dochez
 * @author Tim Quinn
 */
@Service(name="file")
@Scoped(PerLookup.class)
public class FileArchive extends AbstractReadableArchive implements WritableArchive {

    @Inject
    ArchiveFactory archiveFactory;
    
    // the archive abstraction directory.
    File archive;
    URI uri;
    
    // the currently opened entry
    OutputStream os=null;

    private static final Logger logger = LogDomains.getLogger(FileArchive.class, LogDomains.DPL_LOGGER);

    /*
     * time-stamps the archive and filters the archive's contents
     */
    private TimestampManager timestampManager;

    /*
     * records directories that have been created implicitly by putNextEntry
     * (and therefore their lastModified dates adjusted).  We only need to
     * adjust each directory once so by recording which we've already set we
     * avoid redundant setLastModified invocations on those directories.
     */
    private final Set<File> dirsCreated = new HashSet<File>();
    
    /** 
     * Open an abstract archive
     * @param uri path to the archive
     */
    public void open(URI uri) throws IOException {
        if (!uri.getScheme().equals("file")) {
            throw new IOException("Wrong scheme for FileArchive : " + uri.getScheme());
        }
        this.uri = uri;
        archive = new File(uri);
        if (!archive.exists()) {
            throw new FileNotFoundException(uri.getSchemeSpecificPart());
        }
        timestampManager = TimestampManager.Util.getInstanceForExistingFile(archive);
    }

    /**
     * @see #open(URI)
     * @param uri a string representing URI
     */
    public void open(String uri) throws IOException
    {
        open(URI.create(uri));
    }

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
    
    /** 
     * creates a new abstract archive with the given path
     * @param uri path to create the archive
     */
    public void create(URI uri) throws IOException {


        this.uri = uri;
        archive = new File(uri);
        timestampManager = TimestampManager.Util.getInstanceForNewFile(archive);
        archive.mkdirs();
    }

    /**
     * Close a previously returned sub archive
     *
     * @param subArchive output stream to close
     * @link Archive.getSubArchive}
     */
    public void closeEntry(WritableArchive subArchive) throws IOException {
        subArchive.close();

    }

    /**
     * close the abstract archive
     */
    public void close() throws IOException {
        // nothing to do 
    }
           
    /**
     * delete the archive
     */
    public boolean delete() {
        // delete the directory structure...
        try {
            return deleteDir(archive);
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isDirectory(String name) {
        final File candidate = new File(this.archive, name);
        return isEntryValid(candidate) && candidate.isDirectory();
    }

    /**
     * @return an @see java.util.Enumeration of entries in this abstract
     * archive
     */
    public Enumeration entries() {
        Vector namesList = new Vector();
        getListOfFiles(archive, namesList, null);
        return namesList.elements();
    }

    /**
     * Returns the enumeration of first level directories in this
     * archive
     * @return enumeration of directories under the root of this archive
     */
    public Collection<String> getDirectories() throws IOException {
        List<String> results = new ArrayList<String>();
        for (File f : archive.listFiles()) {
            if (f.isDirectory() && isEntryValid(f)) {
                results.add(f.getName());
            }
        }
        return results;
    }

    /**
     *  @return an @see java.util.Enumeration of entries in this abstract
     * archive, providing the list of embedded archive to not count their 
     * entries as part of this archive
     */
     public Enumeration entries(Enumeration embeddedArchives) {
     	Vector nameList = new Vector();
        List massagedNames = new ArrayList();
	while (embeddedArchives.hasMoreElements()) {
		String subArchiveName  = (String) embeddedArchives.nextElement();
                massagedNames.add(FileUtils.makeFriendlyFilenameExtension(subArchiveName));
	}        
     	getListOfFiles(archive, nameList, massagedNames);
     	return nameList.elements();
     }

    /** 
     * Returns an enumeration of the module file entries with the
     * specified prefix.  All elements in the enumeration are of 
     * type String.  Each String represents a file name relative 
     * to the root of the module. 
     * 
     * @param prefix the prefix of entries to be included
     * @return an enumeration of the archive file entries. 
     */ 
    public Enumeration<String> entries(String prefix) {
        prefix = prefix.replace('/', File.separatorChar);
        File file = new File(archive, prefix);
        Vector<String> namesList = new Vector<String>();
        getListOfFiles(file, namesList, null);
        return namesList.elements();
    }
    
    /**
     * @return true if this archive exists
     */
    public boolean exists() {
        return archive.exists();
    }

    /**
     *
     * create or obtain an embedded archive within this abstraction.
     *
     * @param name name of the embedded archive.
     */
    public ReadableArchive getSubArchive(String name) throws IOException {
        String subEntryName = getFileSubArchivePath(name);
        File subEntry = new File(subEntryName);
        if (subEntry.exists() && isEntryValid(subEntry)) {
            return archiveFactory.openArchive(subEntry);
        }
        return null;
    }
    /**
     * create or obtain an embedded archive within this abstraction.
     *
     * @param name name of the embedded archive.
     */
    public WritableArchive createSubArchive(String name) throws IOException {
        String subEntryName = getFileSubArchivePath(name);
        File subEntry = new File(subEntryName);
        if (!subEntry.exists()) {
            // time to create a new sub directory
            final long now = System.currentTimeMillis();
            subEntry.mkdirs();
            subEntry.setLastModified(now);
            adjustInterveningDirsLastModified(subEntry, now);
        }
        return archiveFactory.createArchive(subEntry);
    }

    /**
     *
     * create or obtain an embedded archive within this abstraction.
     *
     * @param name name of the embedded archive.
     */
    private String getFileSubArchivePath(String name) throws IOException {
       // Convert name to native form. See bug #6345029 for more details.
       name = name.replace('/', File.separatorChar);
       File file = new File(name);
       File subDir;
       if (file.isAbsolute()) {
           subDir = file;
       } else {
           // first we try to see if a sub directory with the right file
           // name exist
           subDir = new File(archive, FileUtils.makeFriendlyFilenameExtension(name));
       	   if (!subDir.exists()) {       	  
               // now we try to open a sub jar file...
               subDir = new File(archive, name);
               if (!subDir.exists()) {
                   // ok, nothing worked, reassing the name to the 
                   // sub directory one
                  subDir = new File(archive, FileUtils.makeFriendlyFilenameExtension(name));
              }                  
       	   }
       }
       return subDir.getPath();
    }
    
    /**
     * Returns the existence of the given entry name
     * The file name must be relative to the root of the module.
     *
     * @param name the file name relative to the root of the module.     
     * @return the existence the given entry name.
     */
    public boolean exists(String name) throws IOException {
        name = name.replace('/', File.separatorChar);
        File input = new File(archive, name);
        return input.exists() && isEntryValid(input);
    }

    /**
     * @return a @see java.io.InputStream for an existing entry in
     * the current abstract archive
     * @param name the entry name
     */
    public InputStream getEntry(String name) throws IOException {
            
        File input = getEntryFile(name);
        if (!input.exists() || input.isDirectory()
            || ! isEntryValid(input)) { // If name corresponds to directory, return null as it can not be opened
            return null;
        }
        FileInputStream fis = new FileInputStream(input);
        try {
            BufferedInputStream bis = new BufferedInputStream(fis);
            return bis;
        } catch (Throwable tx) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Throwable thr) {
                    IOException ioe = new IOException("Error closing FileInputStream after error opening BufferedInputStream for entry " + name);
                    ioe.initCause(thr);
                    throw ioe;
                }
            }
            IOException ioe = new IOException("Error opening BufferedInputStream for entry " + name);
            ioe.initCause(tx);
            throw ioe;
        }
    }

    private File getEntryFile(String name) {
        name = name.replace('/', File.separatorChar);
        return new File(archive, name);
    }

    /**
     * Returns the entry size for a given entry name or 0 if not known
     *
     * @param name the entry name
     * @return the entry size
     */
    public long getEntrySize(String name) {
        name = name.replace('/', File.separatorChar);
        File input = new File(archive, name);
        if (!input.exists() || ! isEntryValid(input)) {
            return 0;
        }
        return input.length();
    }

    /**
     * @return the manifest information for this abstract archive
     */
    public Manifest getManifest() throws IOException {
        InputStream is = null;
        try {
            is = getEntry(JarFile.MANIFEST_NAME);
            if (is!=null) {
                Manifest m = new Manifest(is);
                return m;
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return null;
    }

    /**
     * Returns the URI used to create or open the underlyong archive
     *
     * @return the URI for this archive.
     */
    public URI getURI() {
        return uri;
    }

    /**
     * rename the archive
     *
     * @param name the archive name
     */
    public boolean renameTo(String name) {
        return FileUtils.renameFile(archive, new File(name));
    }
    
    /**
     * Reports whether the entry is valid, in the sense that if this
     * archive has been created during this execution then the entry
     * requested was created later than the archive itself.
     * <p>
     * It is possible (for example, on Windows) for GlassFish to want to create
     * a new archive in a directory that already exists and contains stale
     * "left-over" files from a previous deployment, for example.  This method
     * causes the FileArchive implementation to hide any files that 
     * reside in the directory for an archive that was created during this VM 
     * execution but were not explicitly added to the archive using putNextEntry.
     * 
     * @param entry file to check
     * @return
     */
    private boolean isEntryValid(final File entry) {
        return timestampManager.isEntryValid(entry);
    }

    /**
     * Reports whether the entry is valid, in the sense that the entry is
     * more recent than the archive itself.
     * @param entryName name of the entry to check
     * @return
     */
    private boolean isEntryValid(final String entryName) {
        return isEntryValid(getEntryFile(entryName));
    }
    
    /**
     * utility method for deleting a directory and all its content
     */
    private boolean deleteDir(File directory) throws IOException {
        if (!directory.isDirectory()) {
            throw new FileNotFoundException(directory.getPath());
        }
        
        // delete contents
        File[] entries = directory.listFiles();
        for (int i=0;i<entries.length;i++) {
            if (entries[i].isDirectory()) {
                deleteDir(entries[i]);
            } else {
                FileUtils.deleteFile(entries[i]);
            }
        }
        // delete self
        return FileUtils.deleteFile(directory);
    } 
    
    /**
     * utility method for getting contents of directory and 
     * sub directories
     */
    private void getListOfFiles(File directory, Vector<String> files, List embeddedArchives) {
        // important: listFiles() returns null sometimes.  E.g. if directory is 
        // not a directory -- then null is returned and this method will throw an NPE

        if(directory == null || !directory.isDirectory())
            return;
        
        for (File aList : directory.listFiles()) {
            String fileName = aList.getAbsolutePath().substring(archive.getAbsolutePath().length() + 1);
            fileName = fileName.replace(File.separatorChar, '/');
            if (!aList.isDirectory()) {
                if (!fileName.equals(JarFile.MANIFEST_NAME) && isEntryValid(fileName)) {
                    files.add(fileName);
                }
            } else if (isEntryValid(fileName)) {
                files.add(fileName); // Add entry corresponding to the directory also to the list
                if (embeddedArchives != null) {
                    if (!embeddedArchives.contains(fileName)) {
                        getListOfFiles(aList, files, null);
                    }
                } else {
                    getListOfFiles(aList, files, null);
                }
            }
        }
    }          
    
    /** @return true if this archive abstraction supports overwriting of elements
     *
     */
    public boolean supportsElementsOverwriting() {
        return true;
    }
    
    /** delete an entry in the archive
     * @param name the entry name
     * @return true if the entry was successfully deleted
     *
     */
    public boolean deleteEntry(String name) {
        name = name.replace('/', File.separatorChar);
        File input = new File(archive, name);
        if (!input.exists() || ! isEntryValid(input)) {
            return false;
        }
        return input.delete();
    }

    /**
     * Closes the current entry
     */
    public void closeEntry() throws IOException {
        if (os!=null) {
            os.flush();
            os.close();
            os = null;
        }
    }
    
    /**
     * @returns an @see java.io.OutputStream for a new entry in this
     * current abstract archive.
     * @param name the entry name
     */    
    public OutputStream putNextEntry(String name) throws java.io.IOException {
        name = name.replace('/', File.separatorChar);
        
        File newFile = new File(archive, name);
        if (newFile.exists()) {
            if (!deleteEntry(name)) {
                // XXX add fine-level logging later
            }
        }
        // if the entry name contains directory structure, we need
        // to create those directories first.
        if (name.lastIndexOf(File.separatorChar)!=-1) {
            String dirs = name.substring(0, name.lastIndexOf(File.separatorChar));            
            (new File(archive, dirs)).mkdirs();
        }
        os = new BufferedOutputStream(new FileOutputStream(newFile));
        /*
         * Update the file's timestamp so it is recognized as a legitimate
         * entry in this archive.
         */
        final long now = System.currentTimeMillis();
        newFile.setLastModified(now);
        adjustInterveningDirsLastModified(newFile, now);
        return os;   
    }

    /**
     * Sets the lastModified for all directories from a new file's parent (the
     * directory containing it) up to but not including the archive's
     * top-level directory .
     *
     * @param newFile the newly-created file within the FileArchive
     * @param now timestamp to be used for ancestor directories
     */
    private void adjustInterveningDirsLastModified(final File newFile, final long now) {
        File parent = newFile.getParentFile();
        while ( ! parent.equals(archive)) {
            if ( ! dirsCreated.contains(parent)) {
                parent.setLastModified(now);
                dirsCreated.add(parent);
            }
            parent = parent.getParentFile();
        }
    }

    /**
     * Returns the name portion of the archive's URI.
     * <p>
     * For FileArhive the name is all of the path that follows
     * the last slash (ignoring a slash at the end of the path).  
     * <p>
     * Here are some example archive names for the specified FileArchive paths:
     * <ul>
     * <li>/a/b/c/d/ -> d
     * <li>/a/b/c/d  -> d
     * <li>/a/b/c.jar -> c.jar
     * </ul>
     * @return the name of the archive
     * 
     */
    public String getName() {
        return Util.getURIName(getURI());
    }

    /**
     * API which FileArchive methods should use for dealing with the TimestampManager
     * implementation.
     */
    private static interface TimestampManager {

        /**
         * Returns whether the specified file is valid - that is, is dated
         * after the archive was created.
         * @param f the file to check
         * @return true if the file is valid; false otherwise
         */
        boolean isEntryValid(File f);

        /**
         * Returns whether the specified file is for the hidden timestamp file
         * which FileArchive uses internally.
         * @param f the file to check
         * @return true if the File is the hidden timestamp file; false otherwise
         */
        boolean isEntryTimestampFile(File f);

        class Util {

            private final static long TIME_ROUNDING_FACTOR = 2000;

            /**
             * Factory method for a TimestampManager set up for an existing
             * directory (that is, a FileArchive which is opened rather than
             * created).
             * @param archive the directory to contain the archive
             * @return TimestampManager for the FileArchive to use
             */
            static TimestampManager getInstanceForExistingFile(final File archive) throws IOException {
                return new TimestampManagerImpl(archive);
            }

            /**
             * Factory method for a TimestampManager set up for a new
             * directory (that is, a FileArchive which is created rather than
             * opened).
             * @param archive the directory to contain the archive
             * @return TimestampManager for the FileArchive to use
             * @throws FileNotFoundException
             */
            static TimestampManager getInstanceForNewFile(final File archive) throws FileNotFoundException {
                /*
                 * The resolution on file dates/times can be coarser than
                 * miliseconds.  So we need to round the current time down to the
                 * nearest second using it as the archive's creation time.
                 */
                return new TimestampManagerImpl(archive, roundTimeDown(System.currentTimeMillis()));
            }

            private static long roundTimeDown(final long time) {
                return TIME_ROUNDING_FACTOR * (time / TIME_ROUNDING_FACTOR);
            }
        }
    }

    /**
     * Implementation of the TimestampManager interface.
     * <p>
     * When used for a FileArchive around an existing directory, this class
     * will look for (but not require) the hidden timestamp file.  If it exists,
     * the TimestampManager will use the contents to init the archive creation
     * time.
     * <p>
     * When used for a FileArchive around a new directory, this class
     * will create a timestamp file and write into it the time when the
     * FileArchive was created.  This will allow future opens of the archive
     * to know when the archive was created.
     * <p>
     * We cannot rely on the file
     * system lastModified for the archive's top-level directory.
     * Users might use the GlassFish dynamic reload
     * feature by using "touch applications/appName/.reload" which, if the
     * .reload file did not exist, will update the applications/appName
     * directory's lastModified.
     */
    private static class TimestampManagerImpl implements TimestampManager {
            
        /*
         * Hidden file path used for recording when GlassFish created the 
         * FileArchive.
         */
        private final static String STAMP_FILE_PATH = ".glassfishArchive";

        /*
         * marker value for the archive's creation time if it was not, in
         * fact, created by GlassFish.  In that case we cannot filter the
         * contents.
         */
        private final static long NON_GLASSFISH_ARCHIVE_LAST_MODIFIED = -1;

        /*
         * format used for reading and writing the archive creation time in
         * the marker file
         */
        private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        /*
         * Records when the archive was created by GlassFish, if applicable.
         */
        private long archiveCreation = NON_GLASSFISH_ARCHIVE_LAST_MODIFIED;

        private final File archive;
        private final File timestampFile;

        /**
         * Creates a manager for a FileArchive around an existing directory.
         * @param archive
         */
        private TimestampManagerImpl(final File archive) throws IOException {
            this.archive = archive;
            timestampFile = timestampFile(archive);
            archiveCreation = readTimeFromFile(timestampFile);
        }
        
        /**
         * Creates a manager for a FileArchive around a new directory.
         */
        private TimestampManagerImpl(final File archive, final long stamp) throws FileNotFoundException {
            this.archive = archive;
            archiveCreation = stamp;
            timestampFile = timestampFile(archive);
            writeTimeToFile(timestampFile, stamp);
        }
        
        
        private static File timestampFile(final File archive) {
            return new File(archive, STAMP_FILE_PATH);
        }

        
        @Override
        public boolean isEntryValid(final File f) {
            final boolean isFileAfterArchive = archiveCreation <= f.lastModified();
            final boolean result = ( ! f.equals(timestampFile))
                    &&
                    ((archiveCreation == NON_GLASSFISH_ARCHIVE_LAST_MODIFIED) ||
                     isFileAfterArchive
                    );
            if ( ! isFileAfterArchive) {
                logger.log(Level.WARNING,
                        "enterprise.deployment.filePredatesArchive",
                        new Object[]{
                            f.getAbsolutePath(),
                            dateFormat.format(new Date(f.lastModified())),
                            archive.getAbsolutePath(),
                            dateFormat.format(new Date(archiveCreation))});
            }
            return result;
        }

        @Override
        public boolean isEntryTimestampFile(File f) {
            return timestampFile(archive).equals(f);
        }

        private long readTimeFromFile(final File stampFile) throws IOException {
            LineNumberReader reader = null;
            try {
                reader = new LineNumberReader(new FileReader(stampFile));
                final String stampText = reader.readLine();
                try {
                    final Date stamp = dateFormat.parse(stampText);
                    logger.log(Level.FINE, "FileArchive.TimestampManagerImpl read timestamp {0} from archive {1}",
                            new Object[]{
                                dateFormat.format(stamp),
                                archive.getAbsolutePath()});
                    return stamp.getTime();
                } catch (ParseException ex) {
                    logger.log(Level.WARNING, stampFile.getAbsolutePath(), ex);
                    return NON_GLASSFISH_ARCHIVE_LAST_MODIFIED;
                }
                
            } catch (FileNotFoundException ex) {
                logger.log(Level.FINE, "Filearchive.TimestampManagerImpl did not find timestamp file in archive {0}; treating as not timestamped",
                        archive.getAbsolutePath());
                return NON_GLASSFISH_ARCHIVE_LAST_MODIFIED;
            } catch (IOException ex) {
                logger.log(Level.FINE, "FileArchive.TimestampManagerImpl detected eror reading timestamp file for archive " + archive.getAbsolutePath(),
                        ex);
                return NON_GLASSFISH_ARCHIVE_LAST_MODIFIED;
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }
        
        private void writeTimeToFile(final File stampFile, long stamp) throws FileNotFoundException {
            final PrintWriter pw = new PrintWriter(stampFile);
            pw.println(dateFormat.format(new Date(stamp)));
            pw.close();
        }
    }
    
}
