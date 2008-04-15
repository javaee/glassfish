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
package com.sun.enterprise.deployment.util;

import java.io.*;
import java.io.IOException;
import java.io.FilenameFilter;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.nio.channels.*;
import java.util.jar.*;
import java.util.zip.ZipEntry;
import com.sun.logging.LogDomains;

//START OF IASRI 4660742
import java.util.logging.*;
import com.sun.logging.*;
import com.sun.enterprise.deployment.util.FileClassLoader;
import com.sun.enterprise.util.Print;
//END OF IASRI 4660742

/**
 * File pathname/location management utility methods
 */
public class FileUtil {

    // START OF IASRI 4660742
    static Logger _logger=LogDomains.getLogger(LogDomains.UTIL_LOGGER);
    // END OF IASRI 4660742

    // START OF IASRI 4679641
    // private static final boolean debug = false;
    private static final boolean debug = com.sun.enterprise.deployment.util.logging.Debug.enabled;
    // END OF IASRI 4679641
    private static final String JAR_FILE_NAME = "j2ee.jar";
    private static final String HOME_DIR_PROP="com.sun.enterprise.home";
    private static final String DEFAULT_HOME_DIR=System.getProperty("user.dir");

    // Platform-independent separator character used in jar file entries
    public  static final char JAR_SEPARATOR_CHAR = '/';

    private static final long JAR_ENTRY_UNKNOWN_VALUE = -1;
    private static final int  BYTE_READ_ERROR = -1;

    private static  String basedir = null;

    /* -------------------------------------------------------------------------------------
    */
    
    public static File getTempDirectory() {
	String temp = System.getProperty("java.io.tmpdir");
	String home = System.getProperty("user.name");
	if (home == null) {
	    home = ""; // <-- may not be the best choice for the default value
	}
        File tmp = null;
	if (temp == null) {
            tmp = new File(home, "tmp");
	} else {
            tmp = new File(temp, "j2ee-ri-" + home);
        }
        if (!tmp.exists()) {
            tmp.mkdirs();
        }
	return tmp;
    }

    /**
    * This method converts a relative file path to an absolute path
    * using the system property "com.sun.enterprise.home" as the server root.
    * If the property "com.sun.enterprise.home" has not been found, 
    * it will look for j2ee.jar under classpath to find out server root
    * If still cannot find it, it assumes
    * that the current working directory as the server root.
    * The relative paths specify UNIX file name separator conventions.
    * @param A path relative to the server root. 
    * @return An absolute file path.
    */

    public static String getAbsolutePath(String relativePath)
    {
	if(isAbsolute(relativePath))
		return relativePath;

	String rpath = relativePath.replace('/', File.separatorChar);
        if (basedir == null) setBaseDir();
	String path = basedir + File.separator + relativePath;

	return new File(path).getAbsolutePath();
    }

    private static void setBaseDir() {
        // use com.sun.enterprise.home property if defined
        basedir = System.getProperty(HOME_DIR_PROP);
        if (basedir != null) {
            return;
        } else {
            basedir = DEFAULT_HOME_DIR;
        }
        // look for j2ee.jar under java.class.path
        String classPath = System.getProperty("java.class.path");
        if (classPath == null) {
            return;
        } else {
            StringTokenizer st = 
                new StringTokenizer(classPath, File.pathSeparator);
            while (st.hasMoreTokens()) {
                String filename = st.nextToken();
                if (filename.endsWith(JAR_FILE_NAME)) {
                    try {
                        // found j2ee.jar, cd .. for j2ee root
                        String parent = 
                            (new File(filename)).getAbsoluteFile().
                            getParentFile().getParent();
                        if (parent != null) {
                            basedir = parent;
                        }
                        return;
                    } catch (NullPointerException ex) {
                        // cannot go up directories
                        return;
                    }
                }
            }
        }                                            
    }

    private static boolean isAbsolute(String fpath)
    {
	return new File(fpath).isAbsolute();
    }

    /*****
    // Return the full path name of a file in the lib directory.
    public static String getFullPathFor(String filename)
    {
	// j2ee.jar is assumed to be in the lib directory
	// So first get the directory for j2ee.jar
        URL url = FileUtil.class.getResource("/jndi.properties");
        if (url == null) {
            // cannot find jndi.properties
            // probably due to security restriction
            throw new SecurityException("Cannot find jndi.properties, possibly due to security restriction.");
        }

        String jndiprops = url.getFile();                 
        // jndiprops is of the format:
        // "file:/blah/blah/j2ee.jar!/jndi.properties" on Solaris
        // "file:/C:/blah/blah/j2ee.jar!/jndi.properties" on Windows NT
        String jardir = jndiprops.substring(5, jndiprops.indexOf(JAR_FILE_NAME));           
	return jardir+filename;
    }
    *****/
    
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
     * Convert the zip entry name of a java .class file to its
     * class name.  E.g. "hello/Hello.class" would be converted
     * to "hello.Hello"
     * Entry is assumed to end in ".class".  If it doesn't, the
     * entry name is returned as is.
     * @param Zip entry name for a .class file
     */
    public static String classNameFromEntryName(String entryName) {
        String className = entryName;
        if( entryName.endsWith(".class") ) {
            int dotClassIndex = entryName.indexOf(".class");
            className = entryName.substring(0, dotClassIndex);
            className = className.replace(JAR_SEPARATOR_CHAR , '.');
        }
        return className;
    }

    /**
     * Convert the file name of a .class file to a class name.
     * E.g. "hello\Hello.class" would be converted
     * to "hello.Hello"
     * File is assumed to end in ".class".  If it doesn't, the
     * file name is returned as is.
     * @param File name of a .class file
     */
    public static String classNameFromFile(File file) {
        String className = file.toString();
        if ( className.endsWith(".class") ) {
            String contentFileStr = className.replace(File.separatorChar, '.');
            int cutOffPoint = contentFileStr.lastIndexOf(".class");
            className = contentFileStr.substring(0, cutOffPoint);
        }
        return className;
    }
    

    public static void copyFile(File sourceFile, File destFile)
        throws IOException {

        File parent = new File(destFile.getParent());
        if (!parent.exists()) {
            parent.mkdirs();
        }

        FileInputStream fis = new FileInputStream(sourceFile);
        FileChannel in = fis.getChannel();
        FileOutputStream fos = new FileOutputStream(destFile);
        FileChannel out = fos.getChannel();
        in.transferTo(0, in.size(), out);
        in.close();out.close();fis.close();fos.close();
    }

    /**
     * Return an array of filenames from a colon-separated
     * list of filenames
     */
    public static String[] parseFileList(String files) {

        Vector fileNames = new Vector();
        boolean checkDriveLetter = !(File.pathSeparator.equals(":"));
        StringTokenizer st = new StringTokenizer(files, ":");
        while(st.hasMoreTokens()) {
            String name = st.nextToken();
            if (checkDriveLetter && name.length() == 1) {
                // short-term fix for bug 4262319
                // win32 filename might include ':' (e.g. c:\myapp.jar)
                if (st.hasMoreTokens()) {
                    name = name + ":" + st.nextToken();
                }
            }
            fileNames.addElement(name);
        }
        int size = fileNames.size();
        String[] result = new String[size];
        for (int i=0; i< size; i++) {
            result[i] = (String) fileNames.elementAt(i);
        }
        return result;
    }

    /**
     * Test for equality of two jar entries.
     */
    public static boolean jarEntriesEqual(File file1, String entry1Name,
                                          File file2, String entry2Name)
        throws IOException {
        boolean identical = false;
        JarFile jarFile1  = null;
        JarFile jarFile2  = null;

        try {
            jarFile1 = new JarFile(file1);
            jarFile2 = new JarFile(file2);

            // Jar entries always use '/'.
            String jarEntry1Name = entry1Name.replace(File.separatorChar, 
                                                      JAR_SEPARATOR_CHAR);
            String jarEntry2Name = entry2Name.replace(File.separatorChar, 
                                                      JAR_SEPARATOR_CHAR);

            JarEntry entry1  = jarFile1.getJarEntry(jarEntry1Name);
            JarEntry entry2  = jarFile2.getJarEntry(jarEntry2Name);

            if( entry1 == null ) {
		/** IASRI 4660742
		if( debug ) { 
		    System.out.println(file1 + ":" + entry1Name + " not found"); 
		}
		**/
		// START OF IASRI 4660742
		if(debug && _logger.isLoggable(Level.FINE)) {
		    _logger.log(Level.FINE,file1 + ":" + entry1Name + " not found");
		}
		// END OF IASRI 4660742
		
	    }
	    else if( entry2 == null ) {
		/** IASRI 4660742
		if( debug ) { 
		    System.out.println(file2 + ":" + entry2Name + " not found"); 
		}
		**/
		// START OF IASRI 4660742
		if(debug && _logger.isLoggable(Level.FINE)) {
		    _logger.log(Level.FINE,file2 + ":" + entry2Name + " not found");
		}
		// END OF IASRI 4660742 
	    }
	    else {
		identical = jarEntriesEqual(jarFile1, entry1, jarFile2, entry2);
	    } 
	    if( debug ) {
		/** IASRI 4660742
		System.out.println("Are " + entry1Name + " and " + entry2Name + 
		" identical? " + ( identical ? "YES" : "NO"));
		**/
		// START OF IASRI 4660742 
		if(_logger.isLoggable(Level.FINE)) {
		    _logger.log(Level.FINE, "Are " + entry1Name + " and " 
		    + entry2Name + " identical? " + ( identical ? "YES" : "NO"));
		}
		// END OF IASRI 4660742 
	    }
	} catch(IOException e) {
	    if( debug ) { 
		/** IASRI 4660742
		e.printStackTrace(); 
		**/
		// START OF IASRI 4660742
		if(_logger.isLoggable(Level.WARNING))
		    _logger.log(Level.WARNING,"enterprise_util.excep_in_fileutil",e);
		    // END OF IASRI 4660742
	    }
	    throw e;
	}
	finally {
	    if( jarFile1 != null ) {
		jarFile1.close();
	    }
	    if( jarFile2 != null ) {
		jarFile2.close();
	    }
	}
	
	return identical;               
	}

    /**
     * Test for equality of two jar entries.  
     * NOTE : Not yet optimized for large-file comparisons.
     * @@@ TODO : Read bytes in chunks.
     */
    public static boolean jarEntriesEqual(JarFile jarFile1, JarEntry entry1,
                                          JarFile jarFile2, JarEntry entry2)
        throws IOException
	{
        boolean identical = false;
        int entry1Size    = (int) entry1.getSize();
        int entry2Size    = (int) entry2.getSize();
           
        if( (entry1Size == JAR_ENTRY_UNKNOWN_VALUE) ||
            (entry2Size == JAR_ENTRY_UNKNOWN_VALUE) ||
            (entry1Size == entry2Size) ) {

            // Both files are 0 bytes long.
            if( entry1Size == 0 ) {
                return true;
            }

            InputStream inputStream1 = null;
            InputStream inputStream2 = null;
            try {
                inputStream1 = jarFile1.getInputStream(entry1);
                inputStream2 = jarFile2.getInputStream(entry2);

                byte[] file1Bytes = new byte[entry1Size];
                byte[] file2Bytes = new byte[entry2Size];
                
                int read=0;
                int numBytesRead1=0;
                do {
                    read = inputStream1.read(file1Bytes, numBytesRead1, entry1Size-numBytesRead1);
                    numBytesRead1 += read;
                } while  (read!=BYTE_READ_ERROR & numBytesRead1!=entry1Size);
                
                int numBytesRead2=0;
                do {
                    read = inputStream2.read(file2Bytes, numBytesRead2, entry2Size-numBytesRead2);
                    numBytesRead2 += read;
                } while  (read!=BYTE_READ_ERROR & numBytesRead2!=entry2Size);
                
               
                if( ( numBytesRead1 == BYTE_READ_ERROR ) ||
                    ( numBytesRead2 == BYTE_READ_ERROR ) ) {
                    throw new IOException("Byte read error " + numBytesRead1 + " " + numBytesRead2);
                }
                else if( Arrays.equals(file1Bytes, file2Bytes) ) {
                    identical = true;
                }
                else {
		    /** IASRI 4660742
		    if( debug ) { System.out.println("bytes not equal"); }
		    **/
		    // START OF IASRI 4660742
		    if(debug) {
			_logger.log(Level.FINE,"bytes not equal");
		    }
		    // END OF IASRI 4660742
		    
                }
            } catch(IOException e) {
                /** IASRI 4660742 
		            if( debug ) { e.printStackTrace(); }
		            **/
		            // START OF IASRI 4660742
                if (debug && _logger.isLoggable(Level.WARNING)) {
          	        _logger.log(Level.WARNING,"enterprise_util.excep_in_fileutil",e);
                 }
                // END OF IASRI 4660742

                throw e;
            } finally {
                if( inputStream1 != null ) { inputStream1.close(); }
                if( inputStream2 != null ) { inputStream2.close(); }
            }
        }
        else {
	          /** IASRI 4660742
            if( debug ) { 
                System.out.println("sz: " + entry1Size + " , " + entry2Size);   
            } 
	          **/
  	        // START OF IASRI 4660742
	          if(debug && _logger.isLoggable(Level.FINE)) {
		           _logger.log(Level.FINE,"sz: " + entry1Size + " , " + entry2Size);
  	        }
  	        // END OF IASRI 4660742

        }        
        return identical;
    }

        /* -------------------------------------------------------------------------------------
    */

    public static boolean isEARFile(File file) {
        try {
            JarFile jar = new JarFile(file);
            ZipEntry result = jar.getEntry("META-INF/application.xml");
            jar.close();
            return result != null;
        } catch (IOException ex) {
            return false;
        }
    }


    public static boolean isWARFile(File file) {
        try {
            JarFile jar = new JarFile(file);
            ZipEntry result = jar.getEntry("WEB-INF/web.xml");
            jar.close();
            return result != null;
        } catch (IOException ex) {
            return false;
        }
    }

    public static boolean isEJBJar(File file) {
        try {
            JarFile jar = new JarFile(file);
            ZipEntry result = jar.getEntry("META-INF/ejb-jar.xml");
            jar.close();
            return result != null;
        } catch (IOException ex) {
            return false;
        }
    }

    public static boolean isRARFile(File file) {
        try {
            JarFile jar = new JarFile(file);
            ZipEntry result = jar.getEntry("META-INF/ra.xml");
            jar.close();
            return result != null;
        } catch (IOException ex) {
            return false;
        }
    }

    public static boolean isAppClientJar(File file) {
        try {
            JarFile jar = new JarFile(file);
            ZipEntry result = jar.getEntry("META-INF/application-client.xml");
            jar.close();
            return result != null;
        } catch (IOException ex) {
            return false;
        }
    }
    
    /**
     * Deletes all contained files/dirs in specified dir (does _not_ delete 'dir')
     * Returns true if all contents was successfully deleted
     * This specifically checks to make sure it isn't following symbolic links
     */
    public static boolean deleteDirContents(File dir)
    {

        if (dir.isDirectory()) {

	    try {
		boolean ok = true;
	    	File dirCon = dir.getCanonicalFile();
            	String ch[] = dirCon.list();
            	for (int i = 0; i < ch.length; i++) {
		    File file = new File(dir, ch[i]);
		    try {
		    	File fileCon = file.getCanonicalFile(); // may throw IOException
		    	if (fileCon.getParentFile().equals(dirCon)) {
			    // file is a proper child of the parent directory
                    	    if (!FileUtil.delete(fileCon)) { ok = false; }
		   	} else {
			    // This indicates that the actual child file is not part of the
			    // parent directory, likely because 'ch[i]' represents a symbolic
			    // link.  Calling 'file.delete()' here just deletes the symbolic
			    // link and not the file itself.
			    Print.dprintln("Symbolic link? " + file);
			    //Print.dprintln("  => dir        : " + dirCon);
			    //Print.dprintln("  => file parent: " + fileCon.getParentFile());
			    if (!file.delete()) { ok = false; } // try deleting symbolic link
		    	}
		    } catch (IOException ioe) {
			// unable to determine canonical path of child
		    	Print.dprintStackTrace("Can't delete: " + file, ioe);
			ok = false;
		    }
		}
		return ok;
            } catch (IOException ioe) {
		// unable to determine canonical path of parent dir
		Print.dprintStackTrace("Can't delete dir contents: " + dir, ioe);
		return false;
	    }

        }

	return false;

    }

    /**
     * Deletes all files and subdirectories under dir.
     * Returns true if the specified file/dir was deleted
     * This specifically checks to make sure it isn't following symbolic links
     */
    public static boolean deleteDir(File fileDir) {
	return FileUtil.delete(fileDir);
    }


    public static URL getEntryAsUrl(File moduleLocation, String uri) 
        throws MalformedURLException, IOException {
        URL url = null;
        try {
            url = new URL(uri);
        } catch(java.net.MalformedURLException e) {
            // ignore
            url = null;
        }
        if (url!=null) {
            return url;
        }
        if( moduleLocation != null ) {
            if( moduleLocation.isFile() ) {
                url = FileUtil.createJarUrl(moduleLocation, uri);
            } else {
                String path = uri.replace('/', File.separatorChar);
                url = new File(moduleLocation, path).toURI().toURL();
            }
        }
        return url;
    }

    public static URL createJarUrl(File jarFile, String entry) 
        throws MalformedURLException, IOException {
        return new URL("jar:" + jarFile.toURI().toURL() + "!/" + entry);
    }

    /**
     * Deletes all files and subdirectories under dir.
     * Returns true if the specified file/dir was deleted
     */
    public static boolean delete(File dir) {
        if (dir.isDirectory()) {
	    FileUtil.deleteDirContents(dir);
            return dir.delete(); // delete directory (only if it's empty)
	    // If this delete fails, it's likely due to one of the following:
	    // - Improper permissions.
	    // - On Unix, it's possible that the deleted content files were turned
	    //   into a bunch of '.nfsXXXX' files.  Thus this dir was not empty
	    //   and could not be deleted.
	    // - One of the content files failed to be deleted (see below) 
        } else {
            return dir.delete(); // delete file
	    // If this delete fails, it's likely due to one of the following:
	    // - Improper permissions.
	    // - Didn't exist.
	}
    }

    /* -------------------------------------------------------------------------------------
    */

    private static final boolean DeleteOnExit_ShutdownHook = true;

    private static Vector deleteOnExit_normal = null;
    private static Vector deleteOnExit_forced = null;

    /* 
    ** delete specified file on exit
    ** if specified file is a directory, delete the directory only if
    ** it is empty
    */
    public static File deleteOnExit(File file) {
	return deleteOnExit(file, false);
    }

    /* 
    ** delete specified file on exit
    ** if specified file is a directory and 'forceDeleteDir' is true, then
    ** delete the contents of the directory, followed by the directory itself.
    */
    public static File deleteOnExit(File file, boolean forceDeleteDir) {
	if (DeleteOnExit_ShutdownHook) {
	    if (!file.isAbsolute()) {
		Print.dprintStackTrace("File is not Absolute! " + file.toString());
	    } else {
	        if (deleteOnExit_forced == null) { 
		    deleteOnExit_forced = new Vector();
		    deleteOnExit_normal = new Vector();
		    Runtime.getRuntime().addShutdownHook(new Thread() {
		    	public void run() { 
			    _delFiles(deleteOnExit_forced, true); 
			    _delFiles(deleteOnExit_normal, false); 
			}
		    });
	    	}
		if (forceDeleteDir && file.isDirectory()) {
		    if (!deleteOnExit_forced.contains(file)) {
		        deleteOnExit_forced.add(file); 
		    }
		} else {
		    if (!deleteOnExit_normal.contains(file)) {
		        deleteOnExit_normal.add(file); 
		    }
		}
	    }
	} else {
	    file.deleteOnExit();
	}
	return file;
    }

    private static void _delFiles(java.util.List list, boolean forceDelete) {
	if (list == null) { 
	    return; 
	}

	/* sort list from longest to shortest */
	if (!forceDelete) {
	    // A directory must be empty in order to be deleted.
	    // Sorting the list attempts to place files ahead of directory deletes.
	    // The quick way to accomplish this is by sorting the list of files in
	    // descending order of the length of their path.  Doing so will always
	    // place files before the directory which contains them.
	    // The deletion of directories is not forced because a file may have
	    // been added to a given directory that was not marked 'deleteOnExit'.
	    // If it is preferred to delete directories which have been marked for
	    // 'deleteOnExit' regardless of whether they contain non-marked files
	    // or not, then this sort is not necessary.
	    Comparator fileComparator = new Comparator() {
	    	public int compare(Object o1, Object o2) {
		    String n1 = o1.toString(), n2 = o2.toString();
		    return n2.length() - n1.length();
	    	}
	        public boolean equals(Object o) {
		    return super.equals(o);
	    	}
	    };
	    Collections.sort(list, fileComparator);
	}

	/* delete files/directories */
	for (Iterator i = list.iterator(); i.hasNext();) {
	    File file = (File)i.next();
	    if (!file.isAbsolute()) {
		Print.dprintln("[Not Absolute!] " + file);
	    } else
	    if (file.exists()) {
		if (forceDelete) {
		    if (!FileUtil.delete(file)) {
	            	Print.dprintln("[Not Deleted!] " + file);
		    }
		} else {
		    // This will not delete a directory which still contains files.
		    if (!file.delete()) { // if (!FileUtil.delete(file)) {
	            	Print.dprintln("[Not Deleted!] " + file);
		    }
		}
	    } else {
		// File/Directory no longer exists.  Quietly ignore.
	    }
	}

    }

    
    public static String getClassNameFromFile(File f) throws IOException, ClassFormatError {
	FileClassLoader fcl = new FileClassLoader(f.toString());
	return fcl.getClassName(f);
    }
    
}
