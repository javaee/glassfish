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
package com.sun.enterprise.util;

import java.io.*;
import java.util.*;
import java.util.zip.*;

//START OF IASRI 4660742
import java.util.logging.*;
import com.sun.logging.*;
//END OF IASRI 4660742


/**
 * This class implements a simple utility for creating and extracting JAR
 * (Java Archive) files. The JAR format is based on the ZIP file
 * format, with optional meta-information stored in a MANIFEST entry.
 * To create an EJB JAR, use 
 * JarAccess.create(jarfile, baseDir, ejbNames, files).
 *
 * It borrows from the BeanBox and JDK code.
 */

public class JarAccess {
    // START OF IASRI 4660742
    static Logger _logger=LogDomains.getLogger(LogDomains.UTIL_LOGGER);
    // END OF IASRI 4660742

    public static final String MANIFEST = "META-INF/MANIFEST.MF";
    static final char SEPARATOR = File.separatorChar;

    private File jarName;	// the (relative to WD) name of the JarFile
    private File dirName;	// the (relative to WD) name of the base directory
    private String beanName;	// the (relative to base) beanFileName
    private String[] fileNames;	// all (relative to base) file names

    private static LocalStringManagerImpl localStrings = 
    new LocalStringManagerImpl(JarAccess.class);


    /**
     * Create a new JAR file;
     * Given a base directory, ejb names, and a set of files names,
     * these two relative to the base directory
     *
     * if baseDir is null, it means WD
     * if beanFIle is null, it means generate no MANIFEST
     *
     * Generates a *non-signed* MANIFEST
     */
    public static void create(OutputStream out,
			      File baseDir,
			      String[] ejbNames,
			      String[] files)
	throws IOException
    {
	int start = 0;
	if (ejbNames != null) {
	    start = 1;
	}
	JarEntrySource[] data = new JarEntrySource[files.length + start];
	if (ejbNames != null) {
	    data[0] = makeManifestEntry(ejbNames);
	}
	for (int i = 0; i<files.length; i++) {
	    data[i+start] = new JarEntrySource(entryName(files[i]),
					       new File(baseDir, files[i]));
	}
	create(out, data);
    }

    /**
     * An InputStream with the data about the Manifest
     */
    public static JarEntrySource makeManifestEntry(String[] ejbNames) {
	StringBuffer s = new StringBuffer("Manifest-Version: 1.0\n");
	s.append("\n");
	for ( int i=0; i<ejbNames.length; i++ ) {
	    s.append("Name: "+ejbNames[i]+"\n");
	    s.append("Enterprise-Bean: True\n");
	    s.append("\n");
	}
	return new JarEntrySource(MANIFEST,
	    new ByteArrayInputStream(s.toString().getBytes()));
    }

    /**
     * Creates a new ZIP file with a bunch of files
     */
    public static void create(OutputStream out,
		       String[] files) throws IOException {
	ZipOutputStream zos = new ZipOutputStream(out);
	for (int i = 0; i < files.length; i++) {
	    addEntry(zos, new JarEntrySource(new File(files[i])));
	}
	zos.close();
    }

      /**
     * Creates a new ZIP file with a bunch of entries
     */
    public static void create(OutputStream out,
		       JarEntrySource[] entries) throws IOException {
	ZipOutputStream zos = new ZipOutputStream(out);
	for (int i = 0; i < entries.length; i++) {
	    try {
	        addEntry(zos, entries[i]);
	    } catch ( Exception ex ) {
		/** IASRI 4660742 
    ex.printStackTrace(); 
    **/
		// START OF IASRI 4660742
		_logger.log(Level.SEVERE,"enterprise_util.excep_jaraccess_create",ex);
		// END OF IASRI 4660742
		throw new IOException("Invalid JAR entry: "
					+entries[i].getName());
	    }
	}
	zos.close();
    }

    private static String entryName(String name) {
	name = name.replace(File.separatorChar, '/');
	if (name.startsWith("/")) {
	    name = name.substring(1);
	} else if (name.startsWith("./")) {
	    name = name.substring(2);
	}
	return name;
    }

    /*
     * Adds a new file entry to the ZIP output stream.
     */
    static void addEntry(ZipOutputStream zos,
			 JarEntrySource source) throws IOException {
	String name = source.getName();
	if (name.equals("") || name.equals(".")) {
	    return;
	}

	//	long size = source.getLength();

	ZipEntry e = new ZipEntry(name);

	e.setTime(source.getTime());
	boolean markOnly = source.isMarkOnly();

	if (markOnly) {
	    e.setMethod(ZipEntry.STORED);
	    e.setSize(0);
	    e.setCrc(0);
	}
	zos.putNextEntry(e);
	if (! markOnly) {
	    byte[] buf = new byte[1024];
	    int len = 0;
	    InputStream is = new BufferedInputStream(source.getInputStream());

	    while (len != -1) {
		try{
	    	len = is.read(buf, 0, buf.length);
		}catch(EOFException eof){
			break;
		}

		if(len != -1)
			zos.write(buf, 0, len);
	    }

	    is.close();
	}
	zos.closeEntry();
    }

    /*
     * Extracts specified entries from JAR file.
     */
    public static void extract(InputStream in,
			       String files[]) throws IOException {
	ZipInputStream zis = new ZipInputStream(in);
	ZipEntry e;
	while ((e = zis.getNextEntry()) != null) {
	    if (files == null) {
		extractFile(zis, e);
	    } else {
		String name = e.getName().replace('/', File.separatorChar);
		for (int i = 0; i < files.length; i++) {
		    if (name.startsWith(files[i])) {
			extractFile(zis, e);
			break;
		    }
		}
	    }
	}
    }

    /*
     * Extracts next entry from JAR file, creating directories as needed.
     */
    private static void extractFile(ZipInputStream zis, ZipEntry e)
	throws IOException
    {
	File f = new File(e.getName().replace('/', File.separatorChar));
	if (e.isDirectory()) {
	    if (!f.exists() && !f.mkdirs() || !f.isDirectory()) {
		throw new IOException(f + ": could not create directory");
	    }
	} else {
	    if (f.getParent() != null) {
		File d = new File(f.getParent());
		if (!d.exists() && !d.mkdirs() || !d.isDirectory()) {
		    throw new IOException(d + ": could not create directory");
		}
	    }
	    OutputStream os = new FileOutputStream(f);
	    byte[] b = new byte[512];
	    int len;
	    while ((len = zis.read(b, 0, b.length)) != -1) {
		os.write(b, 0, len);
	    }
	    zis.closeEntry();
	    os.close();
	}
    }


    /*
     * Extracts specified entries from JAR file into the given directory.
     */
    public static Vector extract(InputStream in,
			       String files[],
			       String directory) throws IOException {
	Vector extractedFiles = new Vector();
	ZipInputStream zis = new ZipInputStream(in);
	ZipEntry e;
	while ((e = zis.getNextEntry()) != null) {
	    if (files == null) {
		File extractedFile = extractFile(zis, e, directory);
		extractedFiles.addElement(extractedFile);
	    } else {
		String name = e.getName().replace('/', File.separatorChar);
		for (int i = 0; i < files.length; i++) {
		    if (name.startsWith(files[i])) {
			File extractedFile = extractFile(zis, e, directory);
			extractedFiles.addElement(extractedFile);
			break;
		    }
		}
	    }
	}
	return extractedFiles;
    }


    /*
     * Extracts next entry from JAR file into the specified directory, 
     * creating intermediate directories as needed.
     */
    private static File extractFile(ZipInputStream zis, ZipEntry e, String dir)
	throws IOException
    {
	File f = new File(dir+File.separatorChar+e.getName().replace('/', File.separatorChar));
	if (e.isDirectory()) {
	    if (!f.exists() && !f.mkdirs() || !f.isDirectory()) {
		throw new IOException(f + ": could not create directory");
	    }
	} else {
	    if (f.getParent() != null) {
		File d = new File(f.getParent());
		if (!d.exists() && !d.mkdirs() || !d.isDirectory()) {
		    throw new IOException(d + ": could not create directory");
		}
	    }
	    OutputStream os = new FileOutputStream(f);
	    byte[] b = new byte[512];
	    int len;
	    while ((len = zis.read(b, 0, b.length)) != -1) {
		os.write(b, 0, len);
	    }
	    zis.closeEntry();
	    os.close();
	}
	return f;
    }

    /*
     * Lists contents of JAR file.
     */
    private static void list(InputStream in, String files[])
	throws IOException
    {
	ZipInputStream zis = new ZipInputStream(in);
	ZipEntry e;
	while ((e = zis.getNextEntry()) != null) {
	    String name = e.getName().replace('/', File.separatorChar);
	    /*
	     * In the case of a compressed (deflated) entry, the entry size
	     * is stored immediately following the entry data and cannot be
	     * determined until the entry is fully read. Therefore, we close
	     * the entry first before printing out its attributes.
	     */
	    zis.closeEntry();
	    if (files == null) {
		printEntry(e);
	    } else {
		for (int i = 0; i < files.length; i++) {
		    if (name.startsWith(files[i])) {
			printEntry(e);
			break;
		    }
		}
	    }
	}
    }

    /*
     * Prints entry information.
     */
    private static void printEntry(ZipEntry e)	throws IOException {
	output(e.getName());
    }

    /**
     * Parse the arguments
     */
    private boolean parseArgs(String args[]) {
	int l = args.length;
	int i;
	for (i = 0; i<l; i++) {
	    if (args[i].equals("-bean")) {
		if (i+1 >= l) {
		    error(localStrings.getLocalString("jaraccess.bean.option",
                                                      ""));
		    return false;
		}
		beanName = args[i+1];
		i += 1;
	    } else if (args[i].equals("-dir")) {
		if (i+1 >= l) {
		    error(localStrings.getLocalString("jaraccess.dir.option",
                                                      ""));
		    return false;
		}
		dirName = new File(args[i+1]);
		i += 1;
	    } else {
		break;
	    }
	}
	if (i+1 >= l) {
	    error(localStrings.getLocalString("jaraccess.num.args", ""));
	    return false;
	}
	jarName = new File(args[i]);
	i += 1;
	fileNames = new String[l-i];
	for (int j=0; j<l-i ; j++) {
	    fileNames[j] = args[j+i];
	}
	// printArgs();
	return true;
    }

    /**
     * Print the argumens read, for debugging
     */
    private void printArgs() {
	/** IASRI 4660742
	System.err.println("jarName: "+jarName);
	System.err.println("dirName: "+dirName);
	System.err.println("beanName: "+beanName);
	System.err.println("fileNames: "+fileNames);
	**/
	// START OF IASRI 4660742
  if (_logger.isLoggable(Level.FINE)) {
	    _logger.log(Level.FINE,"jarName: "+jarName);
	    _logger.log(Level.FINE,"dirName: "+dirName);
	    _logger.log(Level.FINE,"beanName: "+beanName);
	    _logger.log(Level.FINE,"fileNames: "+fileNames);
  }
	// END OF IASRI 4660742
	if (fileNames != null) {
	    for (int i=0; i<fileNames.length; i++) {
		/** IASRI 4660742
		System.err.println("fileNames["+i+"]: "+fileNames[i]);
		**/
		// START OF IASRI 4660742
    if (_logger.isLoggable(Level.FINE)) {
		    _logger.log(Level.FINE,"fileNames["+i+"]: "+fileNames[i]);
    }
		// END OF IASRI 4660742
	    }
	}
    }

    /**
     * Print an output message
     */
    protected static void output(String s) {
	/** IASRI 4660742
	System.err.println(s);
	**/
	// START OF IASRI 4660742
	_logger.log(Level.FINE,s);
	// END OF IASRI 4660742
    }

    /**
     * Print an error message
     */
    protected static void error(String s) {
	/** IASRI 4660742
	System.err.println(s);
	*/
	// START OF IASRI 4660742
	_logger.log(Level.SEVERE,"enterprise_util.some_error",s);
	// END OF IASRI 4660742
    }

}
