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
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.ReadableByteChannel;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.CheckedInputStream;
import java.util.zip.CRC32;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.io.FileUtils;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

/**
 * Explodes a zip file under the target directory.
 */
public class Unzipper {

    protected static final Logger _logger =
        LogDomains.getLogger(LogDomains.UTIL_LOGGER);

    protected static final int BUFFER_SIZE = 65536;

    /** Base directory where zip file is exploded */
    protected final String _targetDirectory;

    /** List of zip entry names to be ignored during unzip */
    protected final List _ignoreList;

    /** environment variable for nio usage */
    protected static final String _useNio;

    static {
        // use a temp variable so that '_useNio' can be 'final'
        String useNioTemp = null;
        try {
            useNioTemp = System.getProperty(Zipper.USE_NIO_KEY);
        } catch (Exception e) { 
            _logger.log(Level.FINE, "[Unzipper] Error while initializing system environment variable: " + Zipper.USE_NIO_KEY, e); 
        }
        _useNio = useNioTemp;
    }

    /**
     * Constructor.
     *
     * @param   targetDirectory   name of base directory where the zip content
     *                            will be exploded
     */
    public Unzipper(String targetDirectory) {
        _targetDirectory = targetDirectory;
        _ignoreList = new ArrayList();
    }

    /**
     * Ignores the given zip entry during unzip.
     *
     * @param   entryName  name of zip entry
     */
    public void ignoreEntry(String entryName) {
        if (entryName != null) {
            _ignoreList.add(entryName);
        }
    }

    /**
     * Returns true if given zip entry is excluded during unzip.
     *
     * @param   entryName  name of zip entry
     */
    public boolean isIgnoredEntry(String entryName) {
        return _ignoreList.contains(entryName);
    }

    /**
     * Explodes the zip bytes under the target directory.
     *
     * @param   zipBytes   bytes of a zip
     * @return  checksum of zip
     *
     * @throws  IOException  if an i/o error
     */
    public long writeZipBytes(byte[] zipBytes) throws IOException {

        return writeZipInputStream(new ByteArrayInputStream(zipBytes));
    }
    
    /**
     * Explodes the given zip file under the target directory.
     *
     * @param  zipFileName  file path to zip
     * @return checksum of zip
     * 
     * @throws  IOException  if an i/o error
     * @throws  FileNotFoundException  if zip file is not found
     */
    public long writeZipFile(String zipFileName)
                throws IOException, FileNotFoundException {

        return writeZipInputStream(new FileInputStream(zipFileName));
    }

    /**
     * Writes the content of a zip entry to a file using NIO.
     *
     * @param  zin  input stream from zip file
     * @param  fileName  file path where this entry will be saved
     * @param  sz  file size
     *
     * @throws  IOException  if an i/o error
     */
    private void writeFileWithNIO(ZipInputStream zin, String fileName, long sz) 
            throws IOException {

        ReadableByteChannel rc   = null;
        FileOutputStream out     = null;

        try {
            rc  = Channels.newChannel(zin);
            out = new FileOutputStream(fileName);
            FileChannel fc = out.getChannel();

            // read into the buffer
            long count    = 0;
            int attempts  = 0;
            while (count < sz) {
                long written = fc.transferFrom(rc, count, sz);
                count += written;

                if (written == 0) {
                    attempts++;
                    if (attempts > 100) {
                        String msg = _localStrMgr.getString("NioWriteError",
                                                            fileName);
                        throw new IOException(msg);
                    } 
                } else {
                    attempts = 0;
                }
            }

            out.close();
            out = null;
        } finally {
            if (out != null) {
                try { out.close(); } catch (Exception ex) { }
            }
        }
    }

    /**
     * Writes the content of a zip entry to a file using traditional IO.
     *
     * @param  zin  input stream from zip file
     * @param  fileName  file path where this entry will be saved
     *
     * @throws  IOException  if an i/o error
     */
    private void writeFileWithIO(InputStream zin, String fileName) 
            throws IOException {

        OutputStream out  = null;
        byte[] buffer     = new byte[BUFFER_SIZE];

        try {
            out = new BufferedOutputStream(new FileOutputStream(fileName));

            int len = 0;
            while ((len = zin.read(buffer, 0, BUFFER_SIZE)) >= 0) {
                out.write(buffer, 0, len);
            }
            out.close();
            out = null;
        } finally {
            if (out != null) {
                try { out.close(); } catch (Exception ex) { }
            }
        }
    }

    /**
     * Writes the given zip entry from the input stream under the target 
     * directory. 
     *
     * @param  entry  zip entry
     * @param  zin    input stream from zip
     *
     * @throws  IOException  if an i/o error
     */
    public void writeZipEntry(ZipEntry entry, ZipInputStream zin) 
            throws IOException {

        String fileName = _targetDirectory + File.separator + entry.getName();
        File file = new File(fileName);

        if (entry.isDirectory()) {
            _logger.fine("[Unzipper] Extracting directory: " + fileName);
            if (!file.exists()) {
                _logger.fine("[Unzipper] Creating Directory: " + file);
                Unzipper.safeMkDirs(file);
            }
        } else {
            _logger.fine("[Unzipper] Extracting file: " + fileName);
            File parent = file.getParentFile();

            if ((parent != null) && !parent.exists()) {
                _logger.fine("[Unzipper] Creating Directory: " + parent);
                // creates the parent dir; throws Runtime exception 
                // if fails to create the directory
                Unzipper.safeMkDirs(parent);
            }

            long size = entry.getSize();
            if (size > BUFFER_SIZE) {

                // if user turned off nio
                if ((_useNio != null) && ("false".equalsIgnoreCase(_useNio))) {

                    BufferedInputStream in = 
                        new BufferedInputStream(zin, BUFFER_SIZE); 
                    writeFileWithIO(in, fileName); 

                } else {
                    // user did not turn off nio
                    writeFileWithNIO(zin, fileName, size); 
                }
            } else {
                // small file, use regular io
                BufferedInputStream in = 
                    new BufferedInputStream(zin, BUFFER_SIZE); 
                writeFileWithIO(in, fileName); 
            }

            file.setLastModified(entry.getTime());
        }
    }
   
    /**
     * Explodes a zip file from the given input stream under the target 
     * directory.
     *
     * @param  in  input stream from zip file
     * @return checksum of the zip
     *
     * @throws  IOException  if an i/o error
     */
    public long writeZipInputStream(InputStream in) throws IOException {
                
        ZipInputStream zin      = null;
        CheckedInputStream cis  = null;
        ZipEntry entry          = null;

        try {
            cis   = new CheckedInputStream(in, new CRC32());            
            zin   = new ZipInputStream(cis);            
            entry = zin.getNextEntry();

            while (entry != null) {
                if (isIgnoredEntry(entry.getName())) {
                    // skip this entry
                    zin.closeEntry();
                    entry = zin.getNextEntry();
                    continue;
                } 

                // explodes this entry
                writeZipEntry(entry, zin); 

                zin.closeEntry();
                entry = zin.getNextEntry();
            }                                    

            // close input stream from zip
            zin.close();
            zin = null;
            return cis.getChecksum().getValue();

        } finally {
            if (cis != null) {
                try { cis.close(); } catch (Exception ex) { }
            }
            if (zin != null) {
                try { zin.close(); } catch (Exception ex) { }
            }
        }
    }

    /**
     * Creates a directory in fail safe mode.
     *
     * @param dir                   Directory to be created
     * @throws RuntimeException     When directory can not be created.
     */
    static void safeMkDirs(File dir) {

        boolean created = dir.mkdirs();
        if (!created && !dir.exists()) {
            String tmp = dir.getPath();
            if ( tmp.endsWith(File.separator+".") ) {
                // removing file separator and following dot
                tmp = tmp.substring(0, tmp.length()-2);
            }

            String normalizedTmp = normalizePath(tmp);
            File safeDir = new File(normalizedTmp);
            created = safeDir.mkdirs();

            if (!created && !safeDir.exists()) {
                String msg = _localStrMgr.getString(
                        "DirNotCreated", normalizedTmp);
                throw new RuntimeException(msg);
            }
        }
    }


    /**
     * Method usage
     */
    public static void usage() {
        System.out.println("usage: zip-file-name directory-name");
        System.exit(1);
    }

    /**
     * Method main
     *
     * @param argv
     */
    public static void main(String argv[]) {
        try {
            if (argv.length != 2) {
                usage();
            }

            Unzipper z = new Unzipper(argv[1]);            
            z.writeZipFile(argv[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

     /**
      * Normalizes the file path
      * It removes /./ or /. with / and \.\ and \. with \
      *
      * @param pathName          path name to be normalized
      */
     static String normalizePath(String path) {
 
         if (path == null) {
             return path;
         }
 
         // This is done to avoid JDK bug.
         // replaceAll( "\\", ...) fails with internal error
 
         if (File.separatorChar == '\\') {
             // If NT path contains a / skip normalization
             if (path.indexOf('/') >= 0 ) {
                 _logger.fine("[Unzipper] Path contains forward slash. Normalizing: " + path);
             }
 
             // For NT, first replace all \ with / and then search for /./
             // and replace /./ with / 
             // String is coverted back replacing all / to \
             path = FileUtils.makeForwardSlashes(path);
             path = path.replaceAll( "/\\./", "/");
             return path.replace( '/', '\\');
         } else {
             path = FileUtils.makeForwardSlashes(path);
             return path.replaceAll( File.separator+ "\\." + File.separator 
                                 , File.separator);
         }
     }

    //-- PRIVATE VARIABLES ------------------------------------------
    private static StringManager _localStrMgr =
                        StringManager.getManager( Unzipper.class );

}
