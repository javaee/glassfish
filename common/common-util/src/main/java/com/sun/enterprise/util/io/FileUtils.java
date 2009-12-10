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

/* 
 * FileUtils.java
 *
 * Created on November 16, 2001, 6:44 PM
 * 
 * @author  bnevins
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc., 
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A. 
 * All rights reserved. 
 * 
 * This software is the confidential and proprietary information 
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information"). 
 * You shall not disclose such Confidential Information and shall 
 * use it only in accordance with the terms of the license 
 * agreement you entered into with iPlanet/Sun Microsystems. 
 *
 */

/*
 * KEDAR/MURALI has made some changes to this class
 * so that it works with installer(LogDomains esp.).
*/

package com.sun.enterprise.util.io;

import java.io.*;
import java.util.*;

import com.sun.enterprise.util.OS;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.nio.channels.*;
import java.nio.ByteBuffer;
import java.util.regex.Pattern;


public class FileUtils {
    final static Logger _logger = Logger.getLogger("javax.enterprise.system.util");
    final static Logger _utillogger = com.sun.logging.LogDomains.getLogger(FileUtils.class,com.sun.logging.LogDomains.UTIL_LOGGER);

    ///////////////////////////////////////////////////////////////////////////

    public static boolean safeIsDirectory(File f) {
        if (f == null || !f.exists() || !f.isDirectory())
            return false;

        return true;
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean safeIsRealDirectory(String s) {
        return safeIsRealDirectory(new File(s));
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean safeIsRealDirectory(File f) {
        if (safeIsDirectory(f) == false)
            return false;

        // these 2 values while be different for symbolic links
        String canonical = safeGetCanonicalPath(f);
        String absolute = f.getAbsolutePath();

        if (canonical.equals(absolute))
            return true;

        /* Bug 4715043 -- WHOA -- Bug Obscura!!
           * In Windows, if you create the File object with, say, "d:/foo", then the
           * absolute path will be "d:\foo" and the canonical path will be "D:\foo"
           * and they won't match!!!
           **/
        if (OS.isWindows() && canonical.equalsIgnoreCase(absolute))
            return true;

        return false;
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean safeIsDirectory(String s) {
        return safeIsDirectory(new File(s));
    }

    ///////////////////////////////////////////////////////////////////////////

    public static String safeGetCanonicalPath(File f) {
        if (f == null)
            return null;

        try {
            return f.getCanonicalPath();
        }
        catch (IOException e) {
            return f.getAbsolutePath();
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    public static File safeGetCanonicalFile(File f) {
        if (f == null)
            return null;

        try {
            return f.getCanonicalFile();
        }
        catch (IOException e) {
            return f.getAbsoluteFile();
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean hasExtension(String filename, String ext) {
        if (filename == null || filename.length() <= 0)
            return false;

        return filename.endsWith(ext);
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean hasExtension(File f, String ext) {
        if (f == null || !f.exists())
            return false;

        return f.getName().endsWith(ext);
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean hasExtensionIgnoreCase(String filename, String ext) {
        if (filename == null || filename.length() <= 0)
            return false;

        return filename.toLowerCase().endsWith(ext.toLowerCase());
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean hasExtensionIgnoreCase(File f, String ext) {
        if (f == null || !f.exists())
            return false;

        return f.getName().toLowerCase().endsWith(ext.toLowerCase());
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isLegalFilename(String filename) {
        if (!isValidString(filename))
            return false;

        for (int i = 0; i < ILLEGAL_FILENAME_CHARS.length; i++)
            if (filename.indexOf(ILLEGAL_FILENAME_CHARS[i]) >= 0)
                return false;

        return true;
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isFriendlyFilename(String filename) {
        if (!isValidString(filename))
            return false;

        if (filename.indexOf(BLANK) >= 0 || filename.indexOf(DOT) >= 0)
            return false;

        return isLegalFilename(filename);
    }

    ///////////////////////////////////////////////////////////////////////////

    public static String makeLegalFilename(String filename) {
        if (isLegalFilename(filename))
            return filename;

        // let's use "__" to replace "/" and "\" (on Windows) so less chance
        // to collide with the actual name when reverting
        filename = filename.replaceAll("[/" + Pattern.quote("\\") + "]", "__");

        for (int i = 0; i < ILLEGAL_FILENAME_CHARS.length; i++)
            filename = filename.replace(ILLEGAL_FILENAME_CHARS[i], REPLACEMENT_CHAR);

        return filename;
    }

    ///////////////////////////////////////////////////////////////////////////

    public static String makeLegalNoBlankFileName(String filename)
    {
        return  makeLegalFilename(filename).replace(
            BLANK, REPLACEMENT_CHAR);
    }


    ///////////////////////////////////////////////////////////////////////////
    public static String makeFriendlyFilename(String filename) {
        if (isFriendlyFilename(filename))
            return filename;

        String ret = makeLegalFilename(filename).replace(BLANK, REPLACEMENT_CHAR);
        ret = ret.replace(DOT, REPLACEMENT_CHAR);
        return ret;
    }

    ///////////////////////////////////////////////////////////////////////////

    public static String makeFriendlyFilenameNoExtension(String filename) {
        int index = filename.lastIndexOf('.');

        if (index > 0)
            filename = filename.substring(0, index);

        return (makeFriendlyFilename(filename));
    }

    ///////////////////////////////////////////////////////////////////////////

    public static String makeFriendlyFilenameExtension(String filename) {
        if (filename == null) {
            return null;
        }

        filename = makeLegalNoBlankFileName(filename);

        String extension = "";
        if (filename.endsWith(".ear")) {
            filename = filename.substring(0, filename.indexOf(".ear"));
            extension = "_ear";
        } else if (filename.endsWith(".war")) {
            filename = filename.substring(0, filename.indexOf(".war"));
            extension = "_war";
        } else if (filename.endsWith(".jar")) {
            filename = filename.substring(0, filename.indexOf(".jar"));
            extension = "_jar";
        } else if (filename.endsWith(".rar")) {
            filename = filename.substring(0, filename.indexOf(".rar"));
            extension = "_rar";
        }
        return filename + extension;
    }

    public static String revertFriendlyFilenameExtension(String filename) {
        if (filename == null ||
                !(filename.endsWith("_ear") || filename.endsWith("_war") ||
                        filename.endsWith("_jar") || filename.endsWith("_rar"))) {
            return filename;
        }

        String extension = "";
        if (filename.endsWith("_ear")) {
            filename = filename.substring(0, filename.indexOf("_ear"));
            extension = ".ear";
        } else if (filename.endsWith("_war")) {
            filename = filename.substring(0, filename.indexOf("_war"));
            extension = ".war";
        } else if (filename.endsWith("_jar")) {
            filename = filename.substring(0, filename.indexOf("_jar"));
            extension = ".jar";
        } else if (filename.endsWith("_rar")) {
            filename = filename.substring(0, filename.indexOf("_rar"));
            extension = ".rar";
        }
        return filename + extension;
    }

    public static String revertFriendlyFilename(String filename) {

        //first, revert the file extension
        String name = revertFriendlyFilenameExtension(filename);

        //then, revert the rest of the string
        return name.replaceAll("__", "/");
    }

    /////////////////////////////////////////////////////////

    public static void liquidate(File parent) {
        whack(parent);
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isJar(String filename)
    {
        return hasExtension(filename, ".jar");
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isZip(String filename)
    {
        return hasExtensionIgnoreCase(filename, ".zip");
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isJar(File f)
    {
        return hasExtension(f, ".jar");
    }

    ///////////////////////////////////////////////////////////////////////////
    public static boolean isZip(File f)
    {
        return hasExtensionIgnoreCase(f, ".zip");
    }

    /**
     * Deletes a directory and its contents.
     * <p/>
     * If this method encounters a symbolic link in the subtree below "parent"
     * then it deletes the link but not any of the files pointed to by the link.
     * Note that whack will delete files if a symbolic link appears in the
     * path above the specified parent directory in the path.
     *
     * @param parent the File at the top of the subtree to delete
     * @return success or failure of deleting the directory
     */
    public static boolean whack(File parent) {
        return whack(parent, null);
    }

    /**
     * Deletes a directory and its contents.
     * <p/>
     * If this method encounters a symbolic link in the subtree below "parent"
     * then it deletes the link but not any of the files pointed to by the link.
     * Note that whack will delete files if a symbolic link appears in the
     * path above the specified parent directory in the path.
     *
     * @param parent the File at the top of the subtree to delete
     * @return success or failure of deleting the directory
     */
    public static boolean whack(File parent, Collection<File> undeletedFiles) {
        try {
            /*
            *Resolve any links up-stream from this parent directory and
            *then whack the resulting resolved directory.
            */
            return whackResolvedDirectory(parent.getCanonicalFile(), undeletedFiles);
        } catch (IOException ioe) {
            _utillogger.log(Level.SEVERE, "iplanet_util.io_exception", ioe);
            return false;
        }
    }

    /**
     * Deletes a directory and its contents.
     * <p/>
     * The whackResolvedDirectory method is invoked with a File argument
     * in which any upstream file system links have already been resolved.
     * This method will treate Any file passed in that does not have the same
     * absolute and canonical path - as evaluated in safeIsRealDirectory -
     * as a link and will delete the link without deleting any files in the
     * linked directory.
     *
     * @param parent the File at the top of the subtree to delete
     * @return success or failure of deleting the directory
     */
    private static boolean whackResolvedDirectory(File parent, Collection<File> undeletedFiles) {
        /*
        *Do not recursively delete the contents if the current parent
        *is a symbolic link.
        */
        if (safeIsRealDirectory(parent)) {
            File[] kids = parent.listFiles();

            for (int i = 0; i < kids.length; i++) {
                File f = kids[i];

                if (f.isDirectory())
                    whackResolvedDirectory(f, undeletedFiles);
                else if (!deleteFile(f) && undeletedFiles != null) {
                    undeletedFiles.add(f);
                }

            }
        }

        /*
        *Delete the directory or symbolic link.
        */
        return deleteFile(parent);
    }

    /**
     * Delete a file.  If on Windows and the delete fails, run the gc and retry the deletion.
     *
     * @param f file to delete
     * @return boolean indicating success or failure of the deletion atttempt; returns true if file is absent
     */
    public static boolean deleteFile(File f) {
        /*
        *The operation succeeds immediately if the file is deleted
        *successfully.  On systems that support symbolic links, the file
        *will be reported as non-existent if the file is a sym link to a
        *non-existent directory.  In that case invoke delete to remove
        *the link before checking for existence, since File.exists on
        *a symlink checks for the existence of the linked-to directory or
        *file rather than of the link itself.
        */
        if (f.delete()) {
            return true;
        }

        boolean log = _utillogger.isLoggable(FILE_OPERATION_LOG_LEVEL);
        String filePath = f.getAbsolutePath();
        ;

        /*
        *The deletion failed.  This could be simply because the file
        *does not exist.  In that case, log an appropriate message and
        *return.
        */
        if (!f.exists()) {
            if (log) {
                _utillogger.log(Level.FINE, "enterprise_util.delete_failed_absent", filePath);
            }
            return true;
        } else {
            /*
            *The delete failed and the file exists.  Log a message if that
            *level is enabled and return false to indicate the failure.
            */
            if (log) {
                _utillogger.log(FILE_OPERATION_LOG_LEVEL, "enterprise_util.error_deleting_file", filePath);
            }
            return false;
        }
    }

    /**
     * Opens a stream to the specified output file, retrying if necessary.
     *
     * @param out the output File for which a stream is needed
     * @return the FileOutputStream
     * @throws IOException for any errors opening the stream
     */
    public static FileOutputStream openFileOutputStream(File out) throws IOException {
        FileOutputStreamWork work = new FileOutputStreamWork(out);
        int retries = doWithRetry(work);
        if (work.workComplete()) {
            return work.getStream();
        } else {
            IOException ioe = new IOException();
            ioe.initCause(work.getLastError());
            throw ioe;
        }
    }

    /**
    * Return a set of all the files (File objects) under the directory specified, with
    * relative pathnames filtered with the filename filter (can be null for all files).
    */
    public static Set getAllFilesUnder(File directory, FilenameFilter filenameFilter) throws IOException {
	if (!directory.exists() || !directory.isDirectory()) {
	    throw new IOException("Problem with: " + directory + ". You must supply a directory that exists");
	}
        return getAllFilesUnder(directory, filenameFilter, true);
    }

    public static Set getAllFilesUnder(File directory, FilenameFilter filenameFilter, boolean relativize) throws IOException {
        Set allFiles = new TreeSet();
        File relativizingDir = relativize ? directory : null;
        recursiveGetFilesUnder( relativizingDir, directory, filenameFilter,
                                allFiles, false );
        return allFiles;
    }

    public static Set getAllFilesAndDirectoriesUnder(File directory) throws IOException {
	if (!directory.exists() || !directory.isDirectory()) {
	    throw new IOException("Problem with: " + directory + ". You must supply a directory that exists");
	}
	Set allFiles = new TreeSet();
	recursiveGetFilesUnder(directory, directory, null, allFiles, true);
	return allFiles;
    }

    // relativizingRoot can be null, in which case no relativizing is
    // performed.
    private static void recursiveGetFilesUnder(File relativizingRoot, File directory, FilenameFilter filenameFilter, Set set, boolean returnDirectories) {
	File[] files = directory.listFiles(filenameFilter);
	for (int i = 0; i < files.length; i++) {
	    if (files[i].isDirectory()) {
		recursiveGetFilesUnder(relativizingRoot, files[i], filenameFilter, set, returnDirectories);
		if (returnDirectories) {
                    if( relativizingRoot != null ) {
                        set.add(relativize(relativizingRoot, files[i]));
                    } else {
                        set.add(files[i]);
                    }
		}
	    } else {
                if( relativizingRoot != null ) {
                    set.add(relativize(relativizingRoot, files[i]));
                } else {
                    set.add(files[i]);
                }
	    }
    	}
    }

    /**
     * Given a directory and a fully-qualified file somewhere
     * under that directory, return the portion of the child
     * that is relative to the parent.
     */
    public static File relativize(File parent, File child) {
	String baseDir         = parent.getAbsolutePath();
	String baseDirAndChild = child.getAbsolutePath();

        String relative = baseDirAndChild.substring(baseDir.length(),
                                                    baseDirAndChild.length());

        // Strip off any extraneous file separator character.
        if( relative.startsWith(File.separator) ) {
            relative = relative.substring(1);
        }

	return new File(relative);
    }
    

    /**
     * Executes the supplied work object until the work is done or the max.
     * retry count is reached.
     *
     * @param work the RetriableWork implementation to be run
     * @return the number of retries performed; 0 indicates the work succeeded without having to retry
     */
    private static int doWithRetry(RetriableWork work) {
        int retries = 0;

        /*
        *Try the work the first time.  Ideally this will work.
        */
        work.run();

        /*
        *If the work failed and this is Windows - on which running gc may
        *unlock the locked file - then begin the retries.
        */
        if (!work.workComplete() && OS.isWindows()) {
            _utillogger.log(FILE_OPERATION_LOG_LEVEL, "enterprise_util.perform_gc");
            while (!work.workComplete() && retries++ < FILE_OPERATION_MAX_RETRIES) {
                try {
                    Thread.currentThread().sleep(FILE_OPERATION_SLEEP_DELAY_MS);
                } catch (InterruptedException ex) {
                }
                System.gc();
                work.run();
            }
        }
        return retries;
    }

    /**
     * Creates a String listing the absolute paths of files, separated by
     * the platform's line separator.
     *
     * @param files the Collection of File objects to be listed
     * @return String containing the absolute paths of the files with the line separator between them
     */
    public static String formatFileCollection(Collection<File> files) {
        StringBuilder sb = new StringBuilder();
        String lineSep = System.getProperty("line.separator");
        String prefix = "";
        for (File f : files) {
            sb.append(prefix).append(f.getAbsolutePath());
            prefix = lineSep;
        }
        return sb.toString();
    }
    ///////////////////////////////////////////////////////////////////////////

    public static File getDirectory(File f) {
        String filename = f.getAbsolutePath();
        return new File((new File(filename)).getParent());
    }

    ///////////////////////////////////////////////////////////////////////////

    public static File createTempFile(File directory) {
        File f = null;

        try {
            f = File.createTempFile(TMPFILENAME, "jar", directory);
        }
        catch (IOException ioe) {
//Bug 4677074			ioe.printStackTrace(); 
//Bug 4677074 begin
            _logger.log(Level.SEVERE, "iplanet_util.io_exception", ioe);
//Bug 4677074 end
        }

        f.deleteOnExit(); // just in case
        return f;
    }

    /**
     * Returns an array of abstract pathnames that matches with the given
     * file extension. If the given abstract pathname does not denote a
     * directory, then this method returns null. If there is no matching
     * file under the given directory and its sub directories,
     * it returns null;
     *
     * @param dirName dir name under which search will begin
     * @param ext     file extension to look for
     * @return an array of abstract pathnames that matches with the extension
     */
    public static File[] listAllFiles(File dirName, String ext) {
        File[] target = null;
        List list = searchDir(dirName, ext);

        if ((list != null) && (list.size() > 0)) {
            target = new File[list.size()];
            target = (File[]) list.toArray(target);
        }

        return target;
    }

    /**
     * Returns a list of abstract pathnames that matches with the given
     * file extension. If the given abstract pathname does not denote a
     * directory, then this method returns null. If there is no matching
     * file under the given directory and its sub directories, it returns
     * an empty list.
     *
     * @param dirName dir name under which search will begin
     * @param ext     file extension to look for
     * @return a list of abstract pathnames of type java.io.File
     *         that matches with the given extension
     */
    public static List searchDir(File dirName, String ext) {
        List targetList = null;

        if (dirName.isDirectory()) {
            targetList = new ArrayList();

            File[] list = dirName.listFiles();

            for (int i = 0; i < list.length; i++) {
                if (list[i].isDirectory()) {
                    targetList.addAll(searchDir(list[i], ext));
                } else {
                    String name = list[i].toString();
                    if (hasExtension(name, ext)) {
                        targetList.add(list[i]);
                    }
                }
            }
        }

        return targetList;
    }

    /**
     * Copies a file.
     *
     * @param from Name of file to copy
     * @param to   Name of new file
     * @throws IOException if an error while copying the content
     */
    public static void copy(String from, String to) throws IOException {
        //if(!StringUtils.ok(from) || !StringUtils.ok(to))
        if (from == null || to == null)
            throw new IllegalArgumentException("null or empty filename argument");

        File fin = new File(from);
        File fout = new File(to);

        copy(fin, fout);
    }

    /**
     * Copies a file.
     *
     * @param fin  File to copy
     * @param fout New file
     * @throws IOException if an error while copying the content
     */
    public static void copy(File fin, File fout) throws IOException {
        if (safeIsDirectory(fin)) {
            copyTree(fin, fout);
            return;
        }

        if (!fin.exists())
            throw new IllegalArgumentException("File source doesn't exist");

        if (!safeIsDirectory(fout.getParentFile()))
            fout.getParentFile().mkdirs();

        copyFile(fin, fout);
    }

    /**
     * Copies the entire tree to a new location.
     *
     * @param din  File pointing at root of tree to copy
     * @param dout File pointing at root of new tree
     * @throws IOException if an error while copying the content
     */
    public static void copyTree(File din, File dout)
            throws IOException {
        if (!safeIsDirectory(din))
            throw new IllegalArgumentException("Source isn't a directory");

        dout.mkdirs();

        if (!safeIsDirectory(dout))
            throw new IllegalArgumentException("Can't create destination directory");

        FileListerRelative flr = new FileListerRelative(din);
        String[] files = flr.getFiles();

        for (int i = 0; i < files.length; i++) {
            File fin = new File(din, files[i]);
            File fout = new File(dout, files[i]);

            copy(fin, fout);
        }
    }


    /**
     * Returns a String with uniform slashes such that all the
     * occurances of '\\' are replaced with '/'.
     * In other words, the returned string will have all forward slashes.
     * Accepts non-null strings only.
     *
     * @param inputStr non null String
     * @return a String which <code> does not contain `\\` character </code>
     */
    public static String makeForwardSlashes(String inputStr) {
        if (inputStr == null)
            throw new IllegalArgumentException("null String FileUtils.makeForwardSlashes");
        return (inputStr.replace('\\', '/'));
    }

    ///////////////////////////////////////////////////////////////////////////

    public static String getIllegalFilenameCharacters() {
        return ILLEGAL_FILENAME_STRING;
    }

    ///////////////////////////////////////////////////////////////////////////

    static boolean isValidString(String s) {
        return ((s != null) && (s.length() != 0));
    }


    /**
     * This method is used to copy a given file to another file
     * using the buffer sixe specified
     *
     * @param fin  the source file
     * @param fout the destination file
     */
    public static void copyFile(File fin, File fout) throws IOException {

        InputStream inStream = new BufferedInputStream(new FileInputStream(fin));
        FileOutputStream fos = FileUtils.openFileOutputStream(fout);
        copy(inStream, fos, fin.length());
    }


    public static void copy(InputStream in, FileOutputStream out, long size) throws IOException {

        try {
            copyWithoutClose(in, out, size);
        } finally {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }
    }

    public static void copyWithoutClose(InputStream in, FileOutputStream out, long size) throws IOException {

        ReadableByteChannel inChannel = Channels.newChannel(in);
        FileChannel outChannel = out.getChannel();
        outChannel.transferFrom(inChannel, 0, size);

    }

    public static void copy(InputStream in, OutputStream os, long size) throws IOException {
        if (os instanceof FileOutputStream) {
            copy(in, (FileOutputStream) os, size);
        } else {
            ReadableByteChannel inChannel = Channels.newChannel(in);
            WritableByteChannel outChannel = Channels.newChannel(os);
            if (size==0) {

                ByteBuffer byteBuffer = ByteBuffer.allocate(10240);
                int read;
                do {
                    read = inChannel.read(byteBuffer);
                    if (read>0) {
                        byteBuffer.limit(byteBuffer.position());
                        byteBuffer.rewind();
                        outChannel.write(byteBuffer);
                        byteBuffer.clear();
                    }
                } while (read!=-1);
            } else {
                ByteBuffer byteBuffer = ByteBuffer.allocate(Long.valueOf(size).intValue());
                inChannel.read(byteBuffer);
                byteBuffer.rewind();
                outChannel.write(byteBuffer);
            }
        }
    }

        /**
         *Rename, running gc on Windows if needed to try to force open streams to close.
         *@param fromFile to be renamed
         *@param toFile name for the renamed file
         *@return boolean result of the rename attempt
         */
        public static boolean renameFile(File fromFile, File toFile) {
            boolean log = _utillogger.isLoggable(FILE_OPERATION_LOG_LEVEL) || _utillogger.isLoggable(Level.FINE);

            RenameFileWork renameWork = new RenameFileWork(fromFile, toFile);
            int retries = doWithRetry(renameWork);
            boolean result = renameWork.workComplete();

            String fromFilePath = null;
            String toFilePath = null;
            if (log || ! result) {
                fromFilePath = fromFile.getAbsolutePath();
                toFilePath = toFile.getAbsolutePath();
            }

            /*
             *If the rename worked, then write an appropriate log message if the
             *logging level allows.
             */
            if (result) {
                if (log) {
                    /*
                     *If the rename worked without retries, then log a FINE message.
                     *If retries were needed then use the configured
                     *FILE_OPERATION_LOG_LEVEL.
                     */
                    if (retries == 0) {
                        if (_utillogger.isLoggable(Level.FINE)) {
                            _utillogger.log(Level.FINE, "enterprise_util.rename_initial_success", new Object [] {
                                fromFilePath, toFilePath } );
                        }
                    } else {        
                        _utillogger.log(FILE_OPERATION_LOG_LEVEL, "enterprise_util.retry_rename_success", new Object []
                            { fromFilePath, toFilePath, Integer.valueOf(retries) } );
                    }
                }
            } else {
                /*
                 *The rename has failed.  Write a warning message.
                 */
                _utillogger.log(Level.WARNING, "enterprise_util.retry_rename_failure", new Object []
                    { fromFilePath, toFilePath, Integer.valueOf(retries) } );
            }
            return result;
        }    

    /** Appends the given line at the end of given text file. If the given
     * file does not exist, an attempt is made to create it.
     * Note that this method can handle only text files.
     * @param fileName name of the text file that needs to be appended to
     * @param line the line to append to
     * @throws RuntimeException in case of any error - that makes it callable
     * from a code not within try-catch. Note that NPE will be thrown if either
     * argument is null.
     * Note that this method is not tested with String containing characters
     * with 2 bytes.
     */
    public static void appendText(String fileName, String line) throws
        RuntimeException    {
        RandomAccessFile file = null;
        try {
            final String MODE = "rw";
            file = new RandomAccessFile(fileName, MODE);
            file.seek(file.getFilePointer() + file.length());
            file.writeBytes(line);
        }
        catch(Exception e) {
            throw new RuntimeException("FileUtils.appendText()", e);
        }
        finally {
            try {
                if (file != null)
                    file.close();
            }
            catch(Exception e){}
        }
    }
    public static void appendText(String fileName, StringBuffer buffer)
    throws IOException, FileNotFoundException
    {
        appendText(fileName, buffer.toString());
    }
    ///////////////////////////////////////////////////////////////////////////
    
    /** A utility routine to read a <b> text file </b> efficiently and return
     * the contents as a String. Sometimes while reading log files of spawned
     * processes this kind of facility is handy. Instead of opening files, coding
     * FileReaders etc. this method could be employed. It is expected that the
     * file to be read is <code> small </code>.
     * @param fileName String representing absolute path of the file
     * @return String representing the contents of the file, empty String for an empty file
     * @throws java.io.IOException if there is an i/o error.
     * @throws java.io.FileNotFoundException if the file could not be found
     */
    public static String readSmallFile(final String fileName) 
            throws IOException, FileNotFoundException {
        return (readSmallFile(new File(fileName)) );
    }
    
    public static String readSmallFile(final File file) 
            throws IOException {
        final BufferedReader bf = new BufferedReader(new FileReader(file));
        final StringBuilder sb = new StringBuilder(); //preferred over StringBuffer, no need to synchronize
        String line = null;
        try {
            while ( (line = bf.readLine()) != null ) {
                sb.append(line);
                sb.append(System.getProperty("line.separator"));
            }
        }
        finally {
            try {
                bf.close();
            }
            catch (Exception e) {}
        }
        return ( sb.toString() );
    }

    /**
     * Represents a unit of work that should be retried, if needed, until it
     * succeeds or the configured retry limit is reached.
     * <p/>
     * The <code>run</code> method required by the Runnable interface is invoked
     * to perform the work.
     */
    private interface RetriableWork extends Runnable {

        /**
         * Returns whether the work to be done by this instance of RetriableWork
         * has been completed successfully.
         * <p/>
         * This method may be invoked multiple times and so should not have
         * side effects.
         *
         * @return whether the work has been successfully completed
         */
        public boolean workComplete();
    }

    /**
     *Retriable work for renaming a file.
     */
    private static class RenameFileWork implements RetriableWork {

        private File originalFile;
        private File newFile;
        private boolean renameResult = false;

        public RenameFileWork(File originalFile, File newFile) {
            this.originalFile = originalFile;
            this.newFile = newFile;
        }

        public boolean workComplete() {
            return renameResult;
        }

        public void run() {
            renameResult = originalFile.renameTo(newFile);
        }
    }

    /**
     * Retriable work for opening a FileOutputStream.
     */
    private static class FileOutputStreamWork implements RetriableWork {

        private FileOutputStream fos = null;
        private Throwable lastError = null;
        private File out;

        public FileOutputStreamWork(File out) {
            this.out = out;
        }

        public boolean workComplete() {
            return fos != null;
        }

        public void run() {
            try {
                fos = new FileOutputStream(out);
                lastError = null;
            } catch (IOException ioe) {
                lastError = ioe;
            }
        }

        public FileOutputStream getStream() {
            return fos;
        }

        public Throwable getLastError() {
            return lastError;
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    private static final int BUFFER_SIZE = 0x10000; // 64k
    private final static char[] ILLEGAL_FILENAME_CHARS = {'/', '\\', ':', '*', '?', '"', '<', '>', '|'};
    private final static String ILLEGAL_FILENAME_STRING = "\\/:*?\"<>|";
    private final static char REPLACEMENT_CHAR = '_';
    private final static char BLANK = ' ';
    private final static char DOT = '.';
    private static String TMPFILENAME = "scratch";
    /*
    *The following property names are private, unsupported, and unpublished.
    */
    private static final int FILE_OPERATION_MAX_RETRIES = Integer.getInteger("com.sun.appserv.winFileLockRetryLimit", 4).intValue();
        private static final int FILE_OPERATION_SLEEP_DELAY_MS = Integer.getInteger("com.sun.appserv.winFileLockRetryDelay", 100).intValue();
        private static final Level FILE_OPERATION_LOG_LEVEL = Level.FINE;
}
