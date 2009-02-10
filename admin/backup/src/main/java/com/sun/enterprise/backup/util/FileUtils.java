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

package com.sun.enterprise.backup.util;

import java.io.*;
import java.util.*;

public class FileUtils
{
    private FileUtils()
    {
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public static boolean safeIsDirectory(File f)
    {
        if(f == null || !f.exists() || !f.isDirectory())
            return false;
        
        return true;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public static boolean safeIsDirectory(String s)
    {
        return safeIsDirectory(new File(s));
    }
    
    ///////////////////////////////////////////////////////////////////////////
        /*
        public static boolean safeIsRealDirectory(String s)
        {
                return safeIsRealDirectory(new File(s));
        }
         */
    ///////////////////////////////////////////////////////////////////////////
    
        /*
        public static boolean safeIsRealDirectory(File f)
        {
                if(safeIsDirectory(f) == false)
                        return false;
         
                // these 2 values while be different for symbolic links
                String canonical	= safeGetCanonicalPath(f);
                String absolute		= f.getAbsolutePath();
         
                if(canonical.equals(absolute))
                        return true;
         */
                /* Bug 4715043 -- WHOA -- Bug Obscura!!
                 * In Windows, if you create the File object with, say, "d:/foo", then the
                 * absolute path will be "d:\foo" and the canonical path will be "D:\foo"
                 * and they won't match!!!
                 **/
                 /*
                if(OS.isWindows() && canonical.equalsIgnoreCase(absolute))
                        return true;
                  
                return false;
        }
                  */
    
    ///////////////////////////////////////////////////////////////////////////
    
    public static String safeGetCanonicalPath(File f)
    {
        if(f == null)
            return null;
        
        try
        {
            return f.getCanonicalPath();
        }
        catch(IOException e)
        {
            return f.getAbsolutePath();
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public static File safeGetCanonicalFile(File f)
    {
        if(f == null)
            return null;
        
        try
        {
            return f.getCanonicalFile();
        }
        catch(IOException e)
        {
            return f.getAbsoluteFile();
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public static boolean isZip(String filename)
    {
        return hasExtensionIgnoreCase(filename, ".zip");
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public static boolean isZip(File f)
    {
        return hasExtensionIgnoreCase(f, ".zip");
    }
    
    /////////////////////////////////////////////////////////
    
    public static void whack(File parent)
    {
        if(safeIsDirectory(parent))
        {
            File[] kids = parent.listFiles();
            
            for(int i = 0; i < kids.length; i++)
            {
                File f = kids[i];
                
                if(f.isDirectory())
                    whack(f);
                
                if(!f.delete())
                {
                    f.deleteOnExit();
                }
            }
        }
        
        parent.delete();
    }
    
    /**
     */
    
    public static boolean protect(File f)
    {
        if(!f.exists())
            return true;
        
        if(OS.isUNIX())
            return protectUNIX(f);
        else
            return protectWindows(f);
    }
    
    /**
     **/
    
    public static boolean makeExecutable(File f)
    {
        if(!OS.isUNIX())
            return true;	// no such thing in Windows...
        
        if(!f.exists())
            return true; // no harm, no foul...
        
        if(!f.isDirectory())
            return makeExecutable(new File[] { f} );
        
        // if we get here -- f is a directory
        
        return makeExecutable(f.listFiles());
    }
    /**
     * Copies the entire tree to a new location.
     *
     * @param   sourceTree  File pointing at root of tree to copy
     * @param   destTree    File pointing at root of new tree
     *
     * @exception  IOException  if an error while copying the content
     */
    public static void copyTree(File din, File dout) throws IOException
    {
        if(!safeIsDirectory(din))
            throw new IllegalArgumentException("Source isn't a directory");
        
        dout.mkdirs();
        
        if(!safeIsDirectory(dout))
            throw new IllegalArgumentException("Can't create destination directory");
        
        FileListerRelative flr = new FileListerRelative(din);
        String[] files = flr.getFiles();
        
        for(int i = 0; i < files.length; i++)
        {
            File fin  = new File(din, files[i]);
            File fout = new File(dout, files[i]);
            
            copy(fin, fout);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    //////     PRIVATE METHODS      ///////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    
    private static boolean hasExtension(String filename, String ext)
    {
        if(filename == null || filename.length() <= 0)
            return false;
        
        return filename.endsWith(ext);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private static boolean hasExtension(File f, String ext)
    {
        if(f == null || !f.exists())
            return false;
        
        return f.getName().endsWith(ext);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private static boolean hasExtensionIgnoreCase(String filename, String ext)
    {
        if(filename == null || filename.length() <= 0)
            return false;
        
        return filename.toLowerCase().endsWith(ext.toLowerCase());
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private static boolean hasExtensionIgnoreCase(File f, String ext)
    {
        if(f == null || !f.exists())
            return false;
        
        return f.getName().toLowerCase().endsWith(ext.toLowerCase());
    }
    
    /**
     * Copies the bytes from the given input stream to the output stream.
     * It closes the streams afterwards.
     *
     * @param   inStream    input stream from the src
     * @param   outStream   output stream to the destination
     *
     * @exception  IOException  if an error while copying the content
     */
    private static void copy(InputStream inStream, OutputStream outStream) throws IOException
    {
        copyWithoutClose(inStream, outStream);
        
        // closes the streams
        inStream.close();
        outStream.close();
    }
    
    /**
     * Copies the bytes from the given input stream to the output stream.
     * It does not close the streams afterwards.
     *
     * @param   inStream    input stream from the src
     * @param   outStream   output stream to the destination
     *
     * @exception  IOException  if an error while copying the content
     */
    private static void copyWithoutClose(InputStream inStream, OutputStream outStream) throws IOException
    {
        BufferedInputStream bis = new BufferedInputStream(inStream, BUFFER_SIZE);
        BufferedOutputStream bos = new BufferedOutputStream(outStream, BUFFER_SIZE);
        byte[] buf = new byte[BUFFER_SIZE];
        
        int len = 0;
        while (len != -1)
        {
            try
            {
                len = bis.read(buf, 0, buf.length);
            }
            catch (EOFException eof)
            {
                break;
            }
            
            if (len != -1)
            {
                bos.write(buf, 0, len);
            }
        }
        bos.flush();
    }
    /**
     * Copies a file.
     *
     * @param   from		Name of file to copy
     * @param   to			Name of new file
     * @exception  IOException  if an error while copying the content
     */
    private static void copy(String from, String to) throws IOException
    {
        //if(!StringUtils.ok(from) || !StringUtils.ok(to))
        if(from == null || to == null)
            throw new IllegalArgumentException("null or empty filename argument");
        
        File fin  = new File(from);
        File fout = new File(to);
        
        copy(fin, fout);
    }
    /**
     * Copies a file.
     *
     * @param   from    File to copy
     * @param   to		New file
     *
     * @exception  IOException  if an error while copying the content
     */
    private static void copy(File fin, File fout) throws IOException
    {
        if(safeIsDirectory(fin))
        {
            copyTree(fin, fout);
            return;
        }
        
        if(!fin.exists())
            throw new IllegalArgumentException("File source doesn't exist");
        
        if(!safeIsDirectory(fout.getParentFile()))
            fout.getParentFile().mkdirs();
        
        copy(new FileInputStream(fin), new FileOutputStream(fout));
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private static boolean protectUNIX(File f)
    {
        if(f == null)
            return false;
        
        try
        {
            List<String>	cmds        = new ArrayList<String>();
            List<String>	cmdsDirs    = new ArrayList<String>();
            File[]			files	= null;
            ProcessBuilder	pb		= null;
            Process			p		= null;
            boolean			ret		= false;
            
            if(f.isDirectory())
            {
                // chmod to rwx------
                // and chmod files inside dir to rw-------
                // 6580444 -- make any subdirs drwxr-xr-x (0755) otherwise we can't
                // delete the whole tree as non-root for some reason.
                // notice that the original file, if a directory, WILL have 0700
                // this is exactly the way the permissions exist in the original
                // domain files.
                cmds.add("chmod");
                cmds.add("0700");
                cmds.add(safeGetCanonicalPath(f));
                pb = new ProcessBuilder(cmds);
                p = pb.start();
                ret = p.waitFor() == 0 ? true : false;
                
                files = f.listFiles();
                
                if(files == null || files.length < 1)
                    return ret;
            }
            else
            {
                ret = true;
                files = new File[] { f };
            }
            cmds.clear();
            cmds.add("chmod");
            cmds.add("0600");
            cmdsDirs.add("chmod");
            cmdsDirs.add("0755");
            
            
            for(File file : files)
            {
                if(file.isDirectory())
                    cmdsDirs.add(safeGetCanonicalPath(file));
                else
                    cmds.add(safeGetCanonicalPath(file));
            }
            
            pb = new ProcessBuilder(cmds);
            p = pb.start();
            
            // if any chmod returned false -- return false...
            ret = ret && (p.waitFor() == 0 ? true : false);

            if(cmdsDirs.size() > 0)
            {
                pb = new ProcessBuilder(cmdsDirs);
                p = pb.start();
    
                // if any chmod returned false -- return false...
                ret = ret && (p.waitFor() == 0 ? true : false);
            }
            
            return ret;
        }
        catch(Exception e)
        {
            return false;
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private static boolean protectWindows(File f)
    {
        // this is ugly.  We'return calling a program installed with Windows.
        // The program wants to confirm a change so we have to give it a 'Y'
        // at runtime...
        
        String fname = f.getAbsolutePath();
        String uname = System.getProperty("user.name");
        
        try
        {
            ProcessBuilder pb = new ProcessBuilder("cacls", fname, "/G", uname + ":F");
            Process p = pb.start();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            writer.write('Y');
            writer.newLine();
            writer.flush();
            return p.waitFor() == 0 ? true : false;
        }
        catch(Exception e)
        {
            return false;
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private static boolean makeExecutable(File[] files)
    {
        // WBN October 2005
        // dirspace bugfix -- what if there is a space in the dirname?  trouble!
        // changed the argument to a File array
        
        // we are using a String here so that you can pass in a bunch
        // of space-separated filenames.  Doing it one at a time would be inefficient...
        // make it executable for ONLY the user
        
        // Jan 19, 2005 -- rolled back the fix for 6206176.  It has been decided
        // that this is not a bug but rather a security feature.
        
        
        // BUGFIX: 6206176
        // permissions changed from 744 to 755.
        // The reason is that if user 'A' does a restore then user 'A' will be the only
        // user allowed to start or stop a domain.  Whether or not a user is allowed to start a domain
        // needs to be based on the AppServer authentication mechanism (username-password) rather
        // than on the OS authentication mechanism.
        // This case actually is common:  user 'A' does the restore, root tries to start the restored domain.
        
        if(files == null || files.length <= 0)
            return true;
        
        List<String> cmds = new ArrayList<String>();
        
        cmds.add("chmod");
        cmds.add("0744");
        
        for(File f : files)
            cmds.add(safeGetCanonicalPath(f));
        
        try
        {
            ProcessBuilder pb = new ProcessBuilder(cmds);
            Process p = pb.start();
            return p.waitFor() == 0 ? true : false;
        }
        catch(Exception e)
        {
            return false;
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private static final int BUFFER_SIZE = 0x10000; // 64k
    private final static	char[]	ILLEGAL_FILENAME_CHARS	=
    {'/', '\\', ':', '*', '?', '"', '<', '>', '|' };
    private final static	String	ILLEGAL_FILENAME_STRING	= "\\/:*?\"<>|";
    private final static	char	REPLACEMENT_CHAR		= '_';
    private final static	char	BLANK					= ' ';
    private final static	char	DOT						= '.';
    private static			String	TMPFILENAME				= "scratch";
}
