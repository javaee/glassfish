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
package com.sun.enterprise.diagnostics.util;

import com.sun.logging.LogDomains;

import java.nio.channels.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.*;
import java.util.jar.*;

/**
 * Collection of helper methods to manipulate files.
 */
public class FileUtils {
    
    static final int BUFFER = 2048;

    private static Logger logger =
            LogDomains.getLogger(LogDomains.ADMIN_LOGGER);

    /**
     * Copy source file to destination file.
     *
     * @param srcFile Absolute path of the source file to be copied
     * @param destFile Absolute path of the destination file
     *
     */
    public static void copyFile(String srcFile, String destFile)
    throws IOException {
        FileInputStream istream = new FileInputStream(srcFile);
        File dest = new File(destFile);
        copyFile(istream, dest);
    }
    
    /**
     * Copy contents of the specified InputStream to destination
     * file.
     *
     * @param istream InputStream from which to read the source file
     * @param destFile Absolute path of the destination file
     *
     */
    public static void copyFile(InputStream istream, String destFile)
    throws IOException {
        File dest = new File(destFile);
        copyFile(istream, dest);
    }
    
    /**
     * Copy contents of the specified InputStream to destination
     * file.
     *
     * @param istream FileInputStream from which to read the source file
     * @param destFile Absolute path of the destination file
     *
     */
    public static void copyFile(FileInputStream istream, String destFile)
    throws IOException {
        File dest = new File(destFile);
        copyFile(istream, dest);
    }
    
    /**
     * Copy contents of the specified InputStream to desintation
     * file.
     *
     * @param istream InputStream from which to read the source file
     * @param dest Destination File to copy the source file to
     *
     */
    public static void copyFile(InputStream istream, File dest)
    throws IOException {
        OutputStream ostream = new FileOutputStream(dest);
        dest.createNewFile();
        copyFileToStream(istream, ostream);
        ostream.close();
    }
    
    /**
     * Copy contents of the specified InputStream to desintation
     * file.
     *
     * @param istream FileInputStream from which to read the source file
     * @param dest Destination File to copy the source file to
     *
     */
    public static void copyFile(FileInputStream istream, File dest)
    throws IOException {
        if (!dest.exists())  {
            dest.getParentFile().mkdirs();
        }
        FileOutputStream ostream = new FileOutputStream(dest);
        dest.createNewFile();
        copyFileToStream(istream, ostream);
        ostream.close();
    }
    
    /**
     * Copy file from an inputstream to a outputstream
     *
     * @param istream Source input stream
     * @param ostream Destination output stream
     *
     */
    public static void copyFileToStream(InputStream istream, OutputStream ostream)
    throws IOException {
        while(true){
            int nextByte = istream.read();
            if (nextByte==-1)
                break;
            ostream.write(nextByte);
        }
        istream.close();
    }
    
    /**
     * Copy file from an file inputstream to a file outputstream
     *
     * @param istream Source input stream
     * @param ostream Destination output stream
     *
     */
    public static void copyFileToStream(FileInputStream istream, FileOutputStream ostream)
    throws IOException {
        FileChannel srcChannel = istream.getChannel();
        FileChannel destChannel = ostream.getChannel();
        srcChannel.transferTo(0, srcChannel.size(), destChannel);
        srcChannel.close();
        destChannel.close();
        istream.close();
    }
    
    /**
     * Copy file from the specified inputstream to the specified Writer
     *
     * @param istream Source input stream
     * @param writer Writer to which the file will be written
     *
     */
    public static void copyFileToWriter(InputStream istream, Writer writer)
    throws IOException {
        while(true){
            int nextByte = istream.read();
            if (nextByte==-1)
                break;
            writer.write(nextByte);
        }
        istream.close();
    }
    
    /**
     * Copy lines containing search pattern from source file to destination file
     *
     * @param srcFileName Source filename
     * @param dstFileName Destination filename
     * @param searchPattern Search pattern
     *
     */
    public static void copySearchPatternToFile(String srcFileName,
            String dstFileName, String searchPattern)
            throws FileNotFoundException, IOException {
        BufferedReader inFile = new BufferedReader(new FileReader(srcFileName));
        PrintWriter out = new PrintWriter
                (new BufferedWriter(new FileWriter(dstFileName)));
        String fileLine;
        while((fileLine = inFile.readLine()) != null)
            if (fileLine.matches("(?i).*"+searchPattern+".*"))
                out.println(fileLine);
        if (inFile != null)
            inFile.close();
        if (out != null)
            out.close();
    }
    
    /**
     * Copy files from the specified source directory to destination directory
     *
     * @param srcDir Absolute path of the source directory to be copied
     * @param dstDir Absolute path of the destination directory and dstDir
     * should exist
     *
     */
    public static void copyDir(String srcDir, String dstDir)
    throws IOException {
        copyDir(srcDir, dstDir, null, false);
    }
    
    /**
     * Copy files from the specified source directory to destination directory
     *
     * @param srcDir Absolute path of the source directory to be copied
     * @param dstDir Absolute path of the destination directory and dstDir
     * should exist
     * @param subDir Boolean value specifies whether to copy files
     * from subdirectory
     *
     */
    public static void copyDir(String srcDir, String dstDir, boolean subDir)
    throws IOException {
        copyDir(srcDir, dstDir, null, subDir);
    }
    
    /**
     * Copy files from the specified source directory to destination directory
     *
     * @param srcDir Absolute path of the source directory to be copied
     * @param dstDir Absolute path of the destination directory and dstDir should exist
     * @param subDir Boolean value specifies whether to copy files from subdirectory
     *
     */
    public static void copyDir(String srcDir, String dstDir,
            FilenameFilter filter, boolean subDir)
            throws IOException {
        int numFiles = 0;
        String [] strFiles = null;
        
        if (filter == null)
            strFiles = (new File(srcDir)).list();
        else
            strFiles = (new File(srcDir)).list(filter);
        
        if (strFiles != null)
            numFiles = strFiles.length;
        
        for (int i=0; i<numFiles; i++) {
            String srcFile = srcDir+File.separator+strFiles[i];
            String dstFile = dstDir+File.separator+strFiles[i];
            if ((new File(srcFile)).isFile()) {
                copyFile(srcFile,dstFile);
            } else if(subDir) {
                File dstSubDir = new File(dstFile);
                dstSubDir.mkdirs();
                copyDir(srcFile,dstFile,filter,subDir);
            }
        }
    }
    
  /**
     * Extracts the jar files in the given directory.<BR>
     * Note: will not look recursively (in subdirs) for .jar files.
     * @param dir Directory where .jar files has to be searched.
     * @param dest Destination directory where files will be extracted.
     */
   public static void extractJarFiles(String dir, String dest){
       try{
           File aJarDir = new File(dir);
           File files[] = aJarDir.listFiles();

           FilenameFilter filter = getFilenameFilter(".jar");

           for(File file : files)
           {
               //if(file.getName().toLowerCase().endsWith(".jar")){
               if(filter.accept(aJarDir, file.getName())){
                   String fileName = file.getName();
                   //String dirName = fileName.substring(0, fileName.toLowerCase().indexOf(".jar"));
                   //File outputDir = new File(dest+ File.separator + dirName);
                   File outputDir = new File(dest);

                   outputDir.mkdirs();


                   unjar(file,"",outputDir.getAbsolutePath());

                    file.delete();
               }
           }
       }
       catch(Exception e){
            logger.log(Level.WARNING, e.getMessage(), e.fillInStackTrace());
       }
   }

    public static FilenameFilter getFilenameFilter(final String extension){
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                boolean result = false;
                if(name !=null && extension !=null){
                    result = name.toLowerCase().endsWith(extension.toLowerCase());
                }
                return result;
            }
        };
        return filter;
    }
    /**
     * Jar the contents of the specified directory including all
     * subdirectories.
     *
     * @param jarFile Ouput JAR file
     * @param dir Absoilute path of the directory to be JAR'd
     *
     */
    public static void jarDirectory(File jarFile, String dir) {

        try{
            

            BufferedInputStream origin = null;
            File aJarDir = new File(dir);
            File parent = jarFile.getParentFile();
            
            if(!parent.exists())
                parent.mkdirs();
            
            FileOutputStream dest = new FileOutputStream(jarFile);
            JarOutputStream out = new JarOutputStream(new BufferedOutputStream(dest));
            out.setMethod(JarOutputStream.DEFLATED);
            byte data[] = new byte[BUFFER];
            
            List files = FileUtils.getFileListing( aJarDir, true );
            
            Iterator filesIter = files.iterator();
            int length = dir.length()  ;
            //System.out.println(" Directory : " + dir + " : Length : " + length);

            while( filesIter.hasNext() ){
                File f = (File)filesIter.next();
                String path = f.getPath();
                String relativePath = path.substring(length);
                if(relativePath.startsWith(""+File.separator))
                    relativePath = path.substring(length +1);
                    
                if (f.isDirectory())
                    relativePath = relativePath + "/";

                //System.out.println(" Relative Path :" +  relativePath);
                JarEntry entry = new JarEntry(relativePath);
                out.putNextEntry(entry);
                
                if (!f.isDirectory()){
                    FileInputStream fi = new FileInputStream(path);
                    origin = new BufferedInputStream(fi, BUFFER);
                    
                    int count;
                    while((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                }
                if(origin!=null)
                    try{origin.close();}catch(Exception e){e.printStackTrace();}
            }
            if(out!=null)
                try{out.close();}catch(Exception e){e.printStackTrace();}
        } catch (FileNotFoundException fnfEx){
            fnfEx.printStackTrace();
        } catch (IOException ioEx){
            ioEx.printStackTrace();
        }
    }
    
    /**
     * Jar the contents of the specified directory including all
     * subdirectories.
     *
     * @param jarFile Ouput JAR file
     * @param dir Absoilute path of the directory to be JAR'd
     *
     */
    public static void zipDirectory(File jarFile, String dir) {
        try{
            BufferedInputStream origin = null;
            File aJarDir = new File(dir);
            FileOutputStream dest = new FileOutputStream(jarFile);
            ZipOutputStream out = new ZipOutputStream(
                    new BufferedOutputStream(dest));
            out.setMethod(ZipOutputStream.DEFLATED);
            byte data[] = new byte[BUFFER];
            
            List files = FileUtils.getFileListing( aJarDir, true );
            
            Iterator filesIter = files.iterator();
            while( filesIter.hasNext() ){
                File f = (File)filesIter.next();
                String path = f.getPath();
                if (f.isDirectory())
                    path = path + "/";
                
                ZipEntry entry = new ZipEntry(path);
                out.putNextEntry(entry);
                
                if (!f.isDirectory()){
                    FileInputStream fi = new FileInputStream(path);
                    origin = new BufferedInputStream(fi, BUFFER);
                    
                    int count;
                    while((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                }
                if(origin!=null)
                    try{origin.close();}catch(Exception e){e.printStackTrace();}
            }
            if(out!=null)
                try{out.close();}catch(Exception e){e.printStackTrace();}
        } catch (FileNotFoundException fnfEx){
            fnfEx.printStackTrace();
        } catch (IOException ioEx){
            ioEx.printStackTrace();
        }
    }
    public static void unjar(File jarFile, String entryPath, String destDir) {
        
        final int BUFFER = 2048;
        try {
            BufferedOutputStream dest;
            BufferedInputStream is;
            byte data[] = new byte[BUFFER];
            
            if (entryPath==null)
                entryPath="";
            
            JarFile jarfile = new JarFile(jarFile);
            Enumeration e = jarfile.entries();
            
            while(e.hasMoreElements()) { //going through each entries one by one
                JarEntry entry = (JarEntry) e.nextElement();
                is = new BufferedInputStream(jarfile.getInputStream(entry));
                
                String path = entry.getName();
                if (path.startsWith(entryPath)) {
                    int start = path.lastIndexOf("/");
                    String basis = null;
                    if(start != -1) {
                        basis = path.substring(0, start);
                    }
                    String filename = path.substring(start + 1);
                    File tmpPath = new File(destDir);
                    if(basis != null && basis.length() != 0){
                        tmpPath = new File(tmpPath, basis);
                        tmpPath.mkdirs();
                    }
                    
                    if(filename != null && filename.length() != 0){
                        File destfile = new File(tmpPath, filename);
                        FileOutputStream fos = new FileOutputStream(destfile);
                        dest = new BufferedOutputStream(fos, BUFFER);
                        
                        int count;
                        while ((count = is.read(data, 0, BUFFER))!= -1) {
                            dest.write(data, 0, count);
                        }
                        dest.flush();
                        dest.close();
                        is.close();
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Recursively walk a directory tree and return a List of all
     * Files found; the List is sorted using File.compareTo.
     *
     * @param aStartingDir is a valid directory, which can be read.
     */
    static public List getFileListing( File aStartingDir , boolean recurse) throws FileNotFoundException{
        validateDirectory(aStartingDir);
        File[] filesAndDirs = aStartingDir.listFiles();
        List filesDirs = Arrays.asList(filesAndDirs);
        
        if (!recurse) {
            Collections.sort(filesDirs);
            return filesDirs;
        }
        
        Iterator filesIter = filesDirs.iterator();
        List result = new ArrayList();
        File file = null;
        while ( filesIter.hasNext() ) {
            file = (File)filesIter.next();
            result.add(file); //always add, even if directory
            if (recurse && !file.isFile()) {
                //must be a directory
                //recursive call!
                List deeperList = getFileListing(file, true);
                result.addAll(deeperList);
            }
            
        }
        Collections.sort(result);
        return result;
    }
    
    /**
     * Recursively walk a directory tree and return a List of all
     * Files matching the name pattern; the List is sorted using File.compareTo.
     *
     * @param aStartingDir is a valid directory, which can be read.
     * @boolean recurse
     * @param nameFilter pattern for name
     * @param comparator sorts files
     */
    static public List getFileListing(File aStartingDir, boolean recurse,
            FilenameFilter nameFilter,  Comparator comparator)
            throws FileNotFoundException {
        
        validateDirectory(aStartingDir);
        
        File[] filesAndDirs = aStartingDir.listFiles(nameFilter);
        List filesDirs = Arrays.asList(filesAndDirs);
        
        if (!recurse) {
            Collections.sort(filesDirs, comparator);
            return filesDirs;
        }
        
        Iterator filesIter = filesDirs.iterator();
        List result = new ArrayList();
        File file = null;
        while ( filesIter.hasNext() ) {
            file = (File)filesIter.next();
            result.add(file); //always add, even if directory
            if (recurse && !file.isFile()) {
                //must be a directory
                //recursive call!
                List deeperList = getFileListing(file,true);
                result.addAll(deeperList);
            }
            
        }
        Collections.sort(result, comparator);
        return result;
    }
    
    /**
     * Directory is valid if it exists, does not represent a file, and can be read.
     */
    static private void validateDirectory(File aDirectory) throws FileNotFoundException {
        if (aDirectory == null) {
            throw new IllegalArgumentException("Directory should not be null.");
        }
        if (!aDirectory.exists()) {
            throw new FileNotFoundException("Directory does not exist: " + aDirectory);
        }
        if (!aDirectory.isDirectory()) {
            throw new IllegalArgumentException("Is not a directory: " + aDirectory);
        }
        if (!aDirectory.canRead()) {
            throw new IllegalArgumentException("Directory cannot be read: " + aDirectory);
        }
    }
    
    
    
    /**
     * Get the name of the file or directory from an absolute path
     * @param absPath Absolute path
     * @return Name of the directory or file
     */
    public static String getFileName(String absPath) {
        File file = new File(absPath);
        return file.getName();
    }
    
    public static void deleteFile(String fileName){
        deleteFile(new File(fileName));
    }
    
    public static void deleteFile(File file){
        if (file != null) {
            if(file.isDirectory()){
                File[] files = file.listFiles();
                int size = files.length;
                for(int i =0; i < size; i++){
                    deleteFile(files[i]);
                }
            }
            file.delete();
        }
    }
    
    public static void moveFile(String sourceFile, String destFile)
    throws IOException {
        copyFile(sourceFile, destFile);
        new File(sourceFile).delete();
    }
}
