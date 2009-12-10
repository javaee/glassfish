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

package com.sun.enterprise.deploy.shared;

import com.sun.enterprise.deployment.deploy.shared.Util;
import com.sun.enterprise.util.io.FileUtils;
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
 *
 * @author Jerome Dochez
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
        return (new File(this.archive, name)).isDirectory();
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
            if (f.isDirectory()) {
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
        if (subEntry.exists()) {
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
            subEntry.mkdirs();
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
        return input.exists();
    }

    /**
     * @return a @see java.io.InputStream for an existing entry in
     * the current abstract archive
     * @param name the entry name
     */
    public InputStream getEntry(String name) throws IOException {
            
        name = name.replace('/', File.separatorChar);
        File input = new File(archive, name);
        if (!input.exists()) {
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

    /**
     * Returns the entry size for a given entry name or 0 if not known
     *
     * @param name the entry name
     * @return the entry size
     */
    public long getEntrySize(String name) {
        name = name.replace('/', File.separatorChar);
        File input = new File(archive, name);
        if (!input.exists()) {
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
            if (!aList.isDirectory()) {
                fileName = fileName.replace(File.separatorChar, '/');
                if (!fileName.equals(JarFile.MANIFEST_NAME)) {
                    files.add(fileName);
                }
            } else {
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
        if (!input.exists()) {
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
        return os;   
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
    
}
