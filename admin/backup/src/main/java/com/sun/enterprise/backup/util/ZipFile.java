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

/* Byron Nevins, April 2000
 * ZipFile -- A utility class for exploding archive (zip) files.
 */

package com.sun.enterprise.backup.util;

import java.io.*;
import java.util.*;
import java.util.zip.*;

//import com.sun.enterprise.util.io.FileUtils;
//import com.sun.enterprise.util.Assertion;


///////////////////////////////////////////////////////////////////////////////

public class ZipFile
{
	public ZipFile(String zipFilename, String explodeDirName) throws ZipFileException
	{
		this(new File(zipFilename), new File(explodeDirName));
	}

	///////////////////////////////////////////////////////////////////////////
	
	public ZipFile(File zipFile, File anExplodeDir) throws ZipFileException
	{
		//checkZipFile(zipFile);
		BufferedInputStream bis = null;
		
		try
		{
			bis = new BufferedInputStream(new FileInputStream(zipFile), BUFFER_SIZE);
			ctor(bis, anExplodeDir);
		}
		catch(Throwable e)
		{
			if (bis != null) 
			{
				try 
				{
					bis.close();
				} 
				catch (Throwable thr) 
				{
					throw new ZipFileException(thr);
				}
			}
			
			throw new ZipFileException(e);
		}
	}
        
	///////////////////////////////////////////////////////////////////////////
	
	public ZipFile(InputStream inStream, File anExplodeDir) throws ZipFileException
	{
		ctor(inStream, anExplodeDir);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public ArrayList<String> explode() throws ZipFileException
	{
		files = new ArrayList<String>();
		ZipInputStream zin = null;

		try
		{
			zin = zipStream; // new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry ze;

			while( (ze = zin.getNextEntry()) != null )
			{
				String filename = ze.getName();
				
				/*
				if(isManifest(filename))
				{
					continue;	// don't bother with manifest file...
				}
				*/
				
				File fullpath = null;
				
				if(isDirectory(filename))
				{
					// just a directory -- make it and move on...
					fullpath = new File(explodeDir, filename.substring(0, filename.length() - 1));
					
					// bnevins.  FAT BUG FIX!!!  This was mkdir() not mkdirs()
					// this should be back-ported to appserv-commons
					
					fullpath.mkdirs();
					continue;
				}
					
				fullpath = new File(explodeDir, filename);
				File newDir	= fullpath.getParentFile();

				if(newDir.mkdirs())
				{	// note:  it returns false if dir already exists...
				}

				if(fullpath.delete())	// wipe-out pre-existing files
				{
				}

				BufferedOutputStream os = new BufferedOutputStream(getOutputStream(filename), BUFFER_SIZE);

				if(os == null)	// e.g. if we asked to write to a directory instead of a file...
					continue;

				int totalBytes = 0;

				for(int numBytes = zin.read(buffer); numBytes > 0; numBytes = zin.read(buffer))
				{
					os.write(buffer, 0, numBytes);
					totalBytes += numBytes;
				} 
				os.close();				
				files.add(filename);
			}
		}
		catch(IOException e)
		{
			throw new ZipFileException(e);
		}
		finally
		{
			try 
			{ 
				zin.close(); 
			}
			catch(IOException e) 
			{
				throw new ZipFileException("Got an exception while trying to close Jar input stream: " + e);//NOI18N
			}
		}
		return files;
	}

	///////////////////////////////////////////////////////////////////////////

	public ArrayList getFileList()
	{
		return files;
	}
	
	/***********************************************************************
	/******************************** Private ******************************
	/***********************************************************************/
	private static ArrayList<String> doExplode(ZipFile zf) throws ZipFileException
        {
            ArrayList<String> finalList = new ArrayList<String>(50);
            ArrayList<ZipFile> zipFileList = new ArrayList<ZipFile>();
            ArrayList tmpList = null;
            ZipFile tmpZf = null;
            Iterator itr = null;
            String fileName = null;

            zipFileList.add(zf);
            while (zipFileList.size() > 0)
            {
                // get "last" jar to explode
                tmpZf = zipFileList.remove(zipFileList.size() - 1);
                tmpList = tmpZf.explode();

                // traverse list of files
                itr = tmpList.iterator();
                while (itr.hasNext())
                {
                    fileName = (String)itr.next();
                    if ( ! fileName.endsWith(".jar") )
                    {
                        // add non-jar file to finalList
                        finalList.add(fileName);
                    }
                    else
                    {
                        // create ZipFile and add to zipFileList
                        File f = new File(tmpZf.explodeDir, fileName);
                        ZipFile newZf = new ZipFile(f, tmpZf.explodeDir);
                        zipFileList.add(newZf);
                    }

                    if (tmpZf != zf)  // don't remove first ZipFile
                    {
                        tmpZf.explodeDir.delete();
                    }
                }
            }
            return finalList;
        }

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void ctor(InputStream inStream, File anExplodeDir) throws ZipFileException
	{
		insist(anExplodeDir != null);
		explodeDir = anExplodeDir;

		try
		{
			zipStream = new ZipInputStream(inStream);
			checkExplodeDir();
		}
		catch(Throwable t)
		{
                    if (zipStream != null) {
                        try {
                            zipStream.close();
                        } catch (Throwable thr) {
                        }
                    }
		    throw new ZipFileException(t.toString());
		}
	}
        
	///////////////////////////////////////////////////////////////////////////
	
	private boolean isDirectory(String s)
	{
		char c = s.charAt(s.length() - 1);
		
		return c== '/' || c == '\\';
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private void checkZipFile(File zipFile) throws ZipFileException
	{
		insist(zipFile != null);
		
		String zipFileName = zipFile.getPath();

		insist( zipFile.exists(),		"zipFile (" + zipFileName + ") doesn't exist" );//NOI18N
		insist( !zipFile.isDirectory(), "zipFile (" + zipFileName + ") is actually a directory!" );//NOI18N
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private void checkExplodeDir() throws ZipFileException
	{
		String explodeDirName = explodeDir.getPath();
		
		// just in case...
		explodeDir.mkdirs();
		
		insist(explodeDir.exists(),			"Target Directory doesn't exist: "		+ explodeDirName );//NOI18N
		insist(explodeDir.isDirectory(),	"Target Directory isn't a directory: "	+ explodeDirName );//NOI18N
		insist(explodeDir.canWrite(),		"Can't write to Target Directory: "		+ explodeDirName );//NOI18N
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private static boolean isSpecial(String filename)
	{
		return filename.toUpperCase().startsWith(specialDir.toUpperCase());
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private FileOutputStream getOutputStream(String filename) throws ZipFileException
	{
		File f = new File(explodeDir, filename);

		if(f.isDirectory())
		{
			return null;
		}

		try
		{
			return new FileOutputStream(f);
		}
		catch(FileNotFoundException e)
		{
			throw new ZipFileException("filename: " + f.getPath() + "  " + e);
		}
		catch(IOException e)
		{
			throw new ZipFileException(e);
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	
	private boolean isManifest(String filename)
	{
		if(filename.toLowerCase().endsWith("manifest.mf"))//NOI18N
			return false;
		
		return false;
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////                                                   //////////////////////////
	////////////    Internal Error-Checking Stuff                  //////////////////////////
	////////////                                                   //////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////

	private static void pr(String s)
	{
		System.out.println( s );
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private static void insist(String s) throws ZipFileException
	{
		if( s == null || s.length() < 0 )
			throw new ZipFileException();
		else
			return;
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private static void insist(String s, String mesg) throws ZipFileException
	{
		if( s == null || s.length() < 0 )
			throw new ZipFileException( mesg );
		else
			return;
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private static void insist(boolean b) throws ZipFileException
	{
		if( !b )
			throw new ZipFileException();
		else
			return;
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private static void insist(boolean b, String mesg) throws ZipFileException
	{
		if( !b )
			throw new ZipFileException( mesg );
		else
			return;
	}

	/////////////////////////////////////////////////////////////////////////////////////////

        private static final int BUFFER_SIZE = 0x10000; //64k
	private					File			explodeDir		= null;
	private					ArrayList<String>files			= null;
	private static final	String			specialDir		= "META-INF/";//NOI18N
	private					byte[]			buffer			= new byte[BUFFER_SIZE];
	private					ZipInputStream	zipStream		= null;
}

////////////////  
