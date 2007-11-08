/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.ee.util.zip;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipException;
import com.sun.enterprise.util.io.FileUtils;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.BufferOverflowException;

/**
 * Creates a zip file or a list of files that can be part of the zip.
 * The file list can be used for auditing purpose.
 * <b>NOT THREAD SAFE: mutable instance variables</b>
 */
public class Zipper {

    protected static final Logger _logger =
            LogDomains.getLogger(LogDomains.UTIL_LOGGER);
    
    protected static final int BUFFER_SIZE = 65536;

    /** base directory for this zip */
    protected String _baseDirectoryName;

    /** last modified time stamp for this zip */
    protected long _lastModifiedTime;

    /** contains absolute names of excluded file names */
    protected final List _excludeList = new ArrayList();

    /** contains regular expression of excluded patterns */
    protected final List _excludePatternList = new ArrayList();

    /** contains regular expression of include patterns */
    protected final List _includePatternList = new ArrayList();

    /** contains list of paths that must be included regardless of timestamp */
    protected final List _alwaysIncludeList = new ArrayList();

    /** if true, only adds files under a directory */
    protected boolean _shallowCopyEnabled = false;

    /** size of zipped file */
    protected long _size;

    /** maximum buffer size allowed */
    protected Long _maxBuffer;

    /** system environment variable to turn off nio usage */
    protected static final String USE_NIO_KEY = "com.sun.appserv.zip.nio";

    /** environment variable for nio usage */
    protected static final String _useNio;

    protected static final StringManager _localStrMgr = 
                            StringManager.getManager(Zipper.class);

    static {
        // use a temp variable so that '_useNio' can be 'final'
        String useNioTemp = null;
        try {
            useNioTemp = System.getProperty(USE_NIO_KEY);
        } catch (Exception e) { 
            _logger.log(Level.FINE, "[Zipper] Error while initializing system environment variable: " + USE_NIO_KEY, e); 
        }
        _useNio = useNioTemp;
    }

    /**
     * Constructor.
     *
     * @param  baseDirectoryName  name of base directory
     */
    public Zipper(String baseDirectoryName) {
        setBaseDirectory(baseDirectoryName);
        setLastModifiedTime(0L);
    }
    
    /** 
     * Constructor.
     *
     * @param  baseDirectoryName  name of base directory
     * @param  lastModified  last modified time for this zip
     */
    public Zipper(String baseDirectoryName, long lastModifiedTime) {
        setBaseDirectory(baseDirectoryName);
        setLastModifiedTime(lastModifiedTime);
    }
    
    /**
     * Sets the base directory.
     *
     * @param  baseDirectoryName  name of base directory
     */
    public void setBaseDirectory(String baseDirectoryName) {
        _baseDirectoryName = baseDirectoryName;
    }

    /**
     * Returns the zipped file size.
     * 
     * @return  zip file size
     */
    public long getZipSize() {
        return _size;
    }

    /**
     * Returns the maximum buffer allowed for this zipper.
     *
     * @return  max buffer
     */
    public long getMaxBuffer() {
        long val = 0;

        if (_maxBuffer != null) {
            val = _maxBuffer.longValue();
        }

        return val;
    }

    /**
     * Sets the maximum buffer allowed for this zipper.
     * @param   buffer  new buffer 
     */
    public void setMaxBuffer(long buffer) {
        _maxBuffer = new Long(buffer);
    }

    /**
     * Returns true if zip size is bigger than max buffer.
     *
     * @return  true if zip size is bigger than max buffer
     */
    public boolean isBufferOverflowed() {
        boolean tf = false;

        if (_maxBuffer != null) {

            // current zip size
            long size = getZipSize();

            // max buffer size
            long maxBuffer = getMaxBuffer();

            if (size > maxBuffer) {
                tf = true;
            }
        }

        return tf;
    }

    /**
     * Adds the given regular expressions to this zipper. Any files 
     * matching this expression will be excluded while the zip 
     * is created.
     * 
     * @param  list  regular expresion exclude list
     */
    public void addToExcludePatternList(List list) {
        if (list != null) { 
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                Pattern p = Pattern.compile( (String)iter.next() );
                _excludePatternList.add(p);
            }
        }
    }

    /**
     * Include patterns over writes all exclude patterns. Any files 
     * matching this expression will not be excluded while the zip 
     * is created.
     * 
     * @param  list  regular expresion include list
     */
    public void addToIncludePatternList(List list) {
        if (list != null) { 
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                Pattern p = Pattern.compile( (String)iter.next() );
                _includePatternList.add(p);
            }
        }
    }

    /**
     * Adds the absolute file names to the excluded file name list.
     *
     * @param  list  excluded file names
     */
    public void addToExcludeList(List list) {
        if (list != null) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                // uses all forward slashes to compare
                String s = (String) iter.next();
                if (s != null) {
                    String normalized = FileUtils.makeForwardSlashes(s);
                    _excludeList.add(normalized);
                }

            }
        }
    }

    /**
     * Adds the must includes to this zipper's list.
     *
     * @param  list  names of files that must always be included
     */
    public void addToAlwaysIncludeList(List list) {
        if (list != null) {
            _alwaysIncludeList.addAll(list);
        }
    }

    /**
     * Adds to this zipper's must include list.
     *
     * @param  include  name of file that must always be included
     */
    public void addToAlwaysIncludeList(String include) {
        if (include != null) {
            _alwaysIncludeList.add(include);
        }
    }

    /**
     * Returns true if this zipper has paths in the always include list.
     *
     * @return  true if zipper has always includes
     */
    public boolean hasAlwaysInclude() {
        return (!_alwaysIncludeList.isEmpty());
    }

    /**
     * Sets the shallow copy enabled flag. When true, all directories
     * under the root will be ignored. Only files will be added.
     *
     * @param  tf  true if shallow copy is enabled
     */
    public void setShallowCopyEnabled(boolean tf) {
        _shallowCopyEnabled = tf;
    }

    /**
     * Returns true if shallow copy is enabled.
     * 
     * @return  true if shallow copy is enabled
     */
    public boolean isShallowCopyEnabled() {
        return _shallowCopyEnabled;
    }
    
    /**
     * Sets the last modified time stamp for this zip file.
     *
     * @param  lastModifiedTime  last modified time stamp for this zip
     */
    public void setLastModifiedTime(long lastModifiedTime) {
        _lastModifiedTime = lastModifiedTime;
    }
    
    /**
     * Creates a zip from the given directory.
     *
     * @param  directoryName  file path to directory
     * @param  zipFileName    file path to zip file
     * 
     * @throws  IOException  if an i/o error
     * @throws  FileNotFoundException  if unable to create zip file 
     */
    public void createZipFileFromDirectory(String directoryName, 
        String zipFileName) throws IOException, FileNotFoundException {                                    
         createZipOutputStreamFromDirectory(
            new BufferedOutputStream(new FileOutputStream(zipFileName)), 
            directoryName);                      
    }


    /**
     * Creates a zip file from the given directory.
     *
     * @param  directoryName  name of directory
     * @return  bytes from newly created zip file
     *
     * @throws  IOException  if an i/o error
     */
    public byte[] createZipBytesFromDirectory(String directoryName) 
            throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFFER_SIZE);
        createZipOutputStreamFromDirectory(bos, directoryName);       
        return bos.toByteArray();
    }

    /**
     * Adds the given directory content as zip entries to the output stream.
     * 
     * @param  os  output stream to a zip file
     * @param  directoryName  file path to a directory
     *
     * @throws  IOException  if an i/o error
     */
    protected void createZipOutputStreamFromDirectory(OutputStream os, 
        String directoryName) throws IOException {

        ZipOutputStream out = null;
        try {             
            out = new ZipOutputStream(os);
            File directory = new File(directoryName);
            addDirectoryToZip(directory, out); 
            try {
                out.close();
                out = null;
            } catch (ZipException ex) {
                // ignore: an exception is thrown on close 
                // when the zip file is empty
            };
        } finally {
            if (out != null) {
                try { out.close(); } catch (Exception ex) { }
            }
       }
    }

    /**
     * Adds the file to the audit list if not excluded. 
     *
     * @param  file   file to be added
     * @param  out    audit list
     *
     * @throws  IOException  if an i/o error
     * @throws  FileNotFoundException  if file does not exist
     */
    public void addFileToList(File file, List out)
            throws IOException, FileNotFoundException {

        // force add the given file
        addFileToListInternal(file, _baseDirectoryName + PATH_SEPARATOR
                             + file.getName(), out, true);
    }   

    /**
     * Adds the given file to the output stream of the zip.
     *
     * @param  file  file to be added to the zip
     * @param  out   output stream from a zip
     *
     * @throws  IOException  if an i/o error
     * @throws  FileNotFoundException  if file does not exist
     */
    public void addFileToZip(File file, ZipOutputStream out)
            throws IOException, FileNotFoundException {

        // force add the given file
        addFileToZipInternal(file, _baseDirectoryName + PATH_SEPARATOR
                             + file.getName(), out, true);
    }   

    /**
     * Adds the given empty directory to the zip file.
     *
     * @param  file  empty directory 
     * @param  fileName  directory name
     * @param  out  output stream from zip file
     * @param  ignoreExclude  if true, always add the entry
     *
     * @throws  IOException  if an i/o error
     * @throws  FileNotFoundException  if file does not exist
     */
    protected void addEmptyDirToZipInternal(File file, String fileName, 
            ZipOutputStream out, boolean ignoreExclude)
            throws IOException, FileNotFoundException {

        ZipEntry entry = null;

        assert file.isDirectory();        

        // if file exists
        if (file.exists()) {

            // if file is always included
            if (isAlwaysIncluded(file)

                // or file is changed and not excluded
                || ((file.lastModified() >= _lastModifiedTime)
                    && !isExcluded(file, ignoreExclude))) {           

                _logger.fine("[Zipper] Adding Empty directory: " + fileName);

                entry = new ZipEntry(fileName);
                out.putNextEntry(entry);

                out.closeEntry();

            } else {
                _logger.fine("[Zipper] Ignoring Directory: " + fileName 
                    + " its modified time of " + file.lastModified() 
                    + " is < " + _lastModifiedTime);
            }
        } else {
            _logger.fine("[Zipper] Ignoring Directory: " + fileName 
            + " it does not exist");
        }
    }

    /**
     * Adds a file to the audit list.
     *
     * @param  file  file to be added to the audit list
     * @param  fileName  name of file
     * @param  out  audit list
     * @param  ignoreExclude  if true, always add the entry to the audit list
     *
     * @throws  IOException  if an i/o error
     * @throws  FileNotFoundException  if file does not exist
     */
    protected void addFileToListInternal(File file, String fileName, 
            List out, boolean ignoreExclude) 
            throws IOException, FileNotFoundException {

        assert file.isFile();        

        // if file exists 
        if (file.exists()) { 

            // file is always included
            if (isAlwaysIncluded(file)

                // or file is changed and not excluded 
                || ((file.lastModified() >= _lastModifiedTime)
                    && !isExcluded(file, ignoreExclude))) {           

                _logger.fine("[Zipper] Adding File: " + fileName);
                out.add(fileName);

            } else {
                _logger.fine("[Zipper] Ignoring File: " 
                    + fileName + " its modified time of "
                    + file.lastModified() + " is < " + _lastModifiedTime);
            }
        } else {
            _logger.fine("[Zipper] Ignoring File: " 
                + fileName + " it does not exist");
        }
    }

    /**
     * Adds the given file content to the zip output stream using NIO.
     * Before calling this method, a zip entry should be created. This 
     * method is optimal for large files.
     *
     * @param  file  file to be added to the zip
     * @param  out   output stream from the zip
     *
     * @throws  IOException  if an i/o error
     * @throws  FileNotFoundException  if file does not exist
     */
    private void addFileWithNIO(File file, ZipOutputStream out) 
            throws IOException, FileNotFoundException {
            
        FileInputStream fis     = null;
        FileChannel fc          = null;
        WritableByteChannel wbc = null;

        try {
            fis = new FileInputStream(file);
            fc = fis.getChannel();
            long sz = (long) fc.size();

            wbc = Channels.newChannel(out);

            long count    = 0;
            int attempts  = 0;
            while (count < sz) {
                long written = fc.transferTo(count, sz, wbc);
                count += written;

                if (written == 0) {
                    attempts++;
                    if (attempts > 100) {
                        String msg = _localStrMgr.getString("NioReadError", 
                                                    file.getAbsolutePath());
                        throw new IOException(msg);
                    }
                } else {
                    attempts = 0;
                }
            }
            _size += sz;

        } finally {
            if (fis != null) {
                try { fis.close(); } catch (Exception e) { }
            }
            if (fc != null) {
                try { fc.close(); } catch (Exception e) { }
            }
        }
    }

    /**
     * Adds the given file content to the zip output stream using traditional
     * IO. Before calling this method, a zip entry should be created. For small
     * file size, this method is optimal.
     *
     * @param  file  file to be added to the zip
     * @param  out   output stream from the zip
     *
     * @throws  IOException  if an i/o error
     * @throws  FileNotFoundException  if file does not exist
     */
    private void addFileWithIO(File file, ZipOutputStream out) 
            throws IOException, FileNotFoundException {

        BufferedInputStream origin = null;
        byte data[] = new byte[BUFFER_SIZE];
        int count;

        try {
            origin = new BufferedInputStream(new FileInputStream(file),
                                             BUFFER_SIZE);
            while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                out.write(data, 0, count);

                // adds to the total count
                _size += count;
            }
            origin.close();
            origin = null;
        } finally {
            if (origin != null) {
                try { origin.close(); } catch (Exception ex) { }
            }
        }
    }

    /**
     * Adds the given file content to the zip.
     * 
     * @param  file  file to be added to the zip
     * @param  fileName  name of file 
     * @param  out   output stream from the zip
     * @param  ignoreExclude  if true, always add the file
     *
     * @throws  IOException  if an i/o error
     * @throws  FileNotFoundException  if file does not exist
     */
    protected void addFileToZipInternal(File file, String fileName, 
            ZipOutputStream out, boolean ignoreExclude) 
            throws IOException, FileNotFoundException {

        assert file.isFile();        

        // throws exception is buffer max is reached
        if (isBufferOverflowed()) {
            throw new BufferOverflowException();
        }

        // if file exists 
        if (file.exists()) { 

            // file is always included 
            if (isAlwaysIncluded(file)

                // or file is changed and not excluded 
                || ((file.lastModified() >= _lastModifiedTime)
                    && !isExcluded(file, ignoreExclude))) {           

                _logger.fine("[Zipper] Adding File: " + fileName);

                long sz = file.length();

                // create zip entry
                ZipEntry entry = new ZipEntry(fileName);
                entry.setTime(file.lastModified());
                entry.setSize(sz);
                out.putNextEntry(entry);

                // add file content to zip entry
                if (sz > BUFFER_SIZE) {

                    // user turned off nio
                    if ((_useNio != null) 
                            && ("false".equalsIgnoreCase(_useNio))) {
                        
                        addFileWithIO(file, out); 

                    } else {
                        // user did not turn off nio
                        addFileWithNIO(file, out); 
                    }
                } else {
                    // file size is small, use regular io
                    addFileWithIO(file, out); 
                }

                // close zip entry
                out.closeEntry();

            } else {
                _logger.fine("[Zipper] Ignoring File: " 
                    + fileName + " its modified time of "
                    + file.lastModified() + " is < " + _lastModifiedTime);
            }
        } else {
            _logger.fine("[Zipper] Ignoring File: " 
                + fileName + " it does not exist");
        }
    }

    /**
     * Adds the directory to the audit list if not excluded.
     *
     * @param  directory  directory to be added
     * @param  out        audit list
     *
     * @throws  IOException  if an i/o error
     * @throws  FileNotFoundException  if file does not exist
     */
    public void addDirectoryToList(File directory, List out)
            throws IOException, FileNotFoundException {

        // force adds the given directory
        addDirectoryToListInternal(directory, out, _baseDirectoryName, true);
    }

    /**
     * Adds the content of the directory to the zip file.
     * 
     * @param  directory  directory to be added to the zip
     * @param  out  output stream from the zip
     *
     * @throws  IOException  if an i/o error
     * @throws  FileNotFoundException  if file does not exist
     */
    public void addDirectoryToZip(File directory, ZipOutputStream out)
            throws IOException, FileNotFoundException {

        // force adds the given directory
        addDirectoryToZipInternal(directory, out, _baseDirectoryName, true);
    }

    /**
     * Returns true if the file name matches with the excluded 
     * file names or the exclude patterns.
     *
     * @param   f  file name or directory
     * @param   ignoreExclude  ignores the exclude list and patterns when true
     * @return  true if file name is excluded
     */
    private boolean isExcluded(File f, boolean ignoreExclude) {

        if (ignoreExclude) {
            return false;
        }

        String file = f.getPath();
        
        // ignore dirs if shallow copy is enabled
        if (isShallowCopyEnabled() 
                && f.isDirectory()
                && !isIncluded(f)) {

            _logger.fine("[Zipper] File: " + file 
                  + " is shallow copy enabled, is a dir and not included.");
            return true;
        }

        // normalized path
        String nFile = FileUtils.makeForwardSlashes(file);

        // ignore if file is in the exclude list
        if (_excludeList.contains(nFile)) { 
            _logger.fine("[Zipper] File: " + nFile
                  + " is part of the exclude list.");
            return true;
        } 

        // ignore if file name matches the exclude patterns
        Iterator iter = _excludePatternList.iterator();
        while (iter.hasNext()) {
            Pattern p = (Pattern) iter.next();
            Matcher m = p.matcher(file);
            if (m.matches()) {
                _logger.fine("[Zipper] File: " + file 
                      + " is part of the exclude patterns.");
                return true;
            }
        }

        // file is not excluded
        _logger.fine("[Zipper] File: " + file + " is not excluded.");
        return false;
    }

    /**
     * Returns true if file name matches the include pattern list. 
     *
     * @param  f  file or directory
     */
    private boolean isIncluded(File f) {

        String file = f.getPath();

        Iterator iter = _includePatternList.iterator();
        while (iter.hasNext()) {
            Pattern p = (Pattern) iter.next();
            Matcher m = p.matcher(file);
            if (m.matches()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the given file must be included to the zip.
     *
     * @param  f  file or directory
     *
     * @return  true if the given file must be included
     */
    private boolean isAlwaysIncluded(File f) {

        String file = f.getPath();
        Iterator iter = _alwaysIncludeList.iterator();
        while (iter.hasNext()) {
            String name = (String) iter.next();
            if (file.indexOf(name) != -1) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds a directory to an audit list.
     *
     * @param  directory  directory to be added
     * @param  out        audit list
     * @param  baseDir    base directory
     * @param  ignoreExclude if true, always add the file to the list
     *
     * @throws  IOException  if an i/o error
     * @throws  FileNotFoundException  if file does not exist
     */
    private void addDirectoryToListInternal(File directory, List out, 
            String baseDir, boolean ignoreExclude) 
            throws IOException, FileNotFoundException {

        String fileName;

        // if directory exists
        if (directory.exists()) {  

            // if directory is always included 
            if (isAlwaysIncluded(directory)

                // or directory is not excluded
                || (!isExcluded(directory, ignoreExclude))) {           

                File files[] = directory.listFiles();

                for (int i = 0; i < files.length; i++) {
                    fileName = baseDir + PATH_SEPARATOR + files[i].getName();

                    if (files[i].isFile()) {
                        // does not force add the file
                        addFileToListInternal(files[i], fileName, out, false);
                    } else if (files[i].isDirectory()) {
                        // does not force add the file
                        addDirectoryToListInternal(files[i], out, 
                                                fileName, false);
                    } else {
                        assert false;
                    }
                }
            }
        }
    }

    /**
     * Adds a directory to the zip file.
     *
     * @param  directory  directory to be added
     * @param  out        output stream from the zip file
     * @param  baseDir    base directory
     * @param  ignoreExclude if true, always add the file to the list
     *
     * @throws  IOException  if an i/o error
     * @throws  FileNotFoundException  if file does not exist
     */
    private void addDirectoryToZipInternal(File directory, ZipOutputStream out,
            String baseDir, boolean ignoreExclude)
            throws IOException, FileNotFoundException {

        String fileName;

        assert directory.isDirectory();             

        // if directory exists
        if (directory.exists()) { 

            // if directory is always included
            if (isAlwaysIncluded(directory)

                // or directory is not excluded
                || (!isExcluded(directory, ignoreExclude))) {           

                File files[] = directory.listFiles();

                for (int i = 0; i < files.length; i++) {
                    fileName = baseDir + PATH_SEPARATOR + files[i].getName();

                    if (files[i].isFile()) {
                        // does not force add the file
                        addFileToZipInternal(files[i], fileName, out, false);
                    } else if (files[i].isDirectory()) {
                        // does not force add the file
                        addDirectoryToZipInternal(files[i], out, 
                                                fileName, false);
                    } else {
                        assert false;
                    }
                }

                // this is an empty directory, add the directory listing
                /*
                if (files.length == 0 ) {
                    String dirName = baseDir + PATH_SEPARATOR;
                    addEmptyDirToZipInternal(directory, dirName, out, false);
                }
                */
            }
        }
    }

    /**
     * Prints the usage of the main method.
     */
    public static void usage() {
        System.out.println("usage: directory-name zip-file-name <time>");
        System.exit(1);
    }


    /**
     * Test main method.
     */
    public static void main(String argv[]) {
        try {
            long modifiedTime = System.currentTimeMillis();
            if (argv.length < 2 || argv.length > 3) {
                usage();
            }
            if (argv.length == 3) {
                modifiedTime = Long.parseLong(argv[2]);
            }

            System.out.println("modifiedTime is " + modifiedTime);
            Zipper z = new Zipper(".", modifiedTime);
            z.createZipFileFromDirectory(argv[0], argv[1]);            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Always use forward slash in zipper. Since zipper can be used in mixed
    // environments
    private static final String PATH_SEPARATOR = "/";
}
