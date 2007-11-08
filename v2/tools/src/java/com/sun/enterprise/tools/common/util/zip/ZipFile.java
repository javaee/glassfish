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
 * ZipFile -- A utility class for exploding jar files that contain EJB(s).  Used *only* in this package by the EJBImporter class
 */

package com.sun.enterprise.tools.common.util.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.sun.enterprise.tools.common.util.diagnostics.Reporter;
import com.sun.enterprise.tools.common.util.Assertion;
import com.sun.enterprise.tools.common.util.ContainerHelper;

public class ZipFile
{
	public ZipFile(String zipFilename, String explodeDirName) throws ZipFileException
	{
		assertIt(zipFilename);
		assertIt(explodeDirName);

		this.zipFilename	= zipFilename;
		this.explodeDirName	= explodeDirName;

		try
		{
			checkZipFile();
                        zipStream = new ZipInputStream(new FileInputStream(zipFile));
                        //this(zipStream, explodeDirName);
			checkExplodeDir();
		}
		catch(Exception f)
		{
			throw new ZipFileException(f);
		}
	}
        
        
        public ZipFile(InputStream inStream, String explodeDirName) throws ZipFileException
	{
		//assertIt(zipFilename);
		assertIt(explodeDirName);

		//this.zipFilename	= zipFilename;
		this.explodeDirName	= explodeDirName;

		try
		{
			//checkZipFile();
                    zipStream = new ZipInputStream(inStream);
			checkExplodeDir();
		}
		catch(Assertion.Failure f)
		{
			throw new ZipFileException(f);
		}
	}
        
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public String toString()
	{
		String s = "Zip File Name: " + zipFilename + "\n";//NOI18N
		//s += "***** File Contents *********\n";//NOI18N
		//s += ContainerHelper.toOneString(getFileNames());
		
		return s;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*public String[] getFileNames()
	{
		Reporter.assertIt(files); //NOI18N
		
		return ContainerHelper.toStringArray(files);
	}*/

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public String[] explode() throws ZipFileException
	{
		ArrayList explodedFiles = new ArrayList();

		//createDirs();

		ZipInputStream zin = null;

		// OK -- at this point, we have a good zip file, and a list of all the files in the zip file.
		// We've created all the subdirectories needed to explode the files into.
		// let's get busy...

		try
		{
			zin = zipStream; // new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry ze;

			while( (ze = zin.getNextEntry()) != null )
			{
				String filename = ze.getName();
				
				if(isManifest(filename))
				{
					continue;	// don't bother with manifest file...
				}
									
                                File	fullpath	= new File(explodeDir, filename);
                                File	newDir		= fullpath.getParentFile();
			
                                if(newDir.mkdirs())
                                {	// note:  it returns false if dir already exists...
                                    Reporter.verbose("Created new directory:  " + newDir);//NOI18N
                                }

                                if(fullpath.delete())	// wipe-out pre-existing files
                                    Reporter.info("deleted pre-existing file: " + fullpath); //NOI18N
			
			//assertIt(newDir.exists() && newDir.isDirectory(), "Couldn't create directory: " + newDir);//NOI18N
				FileOutputStream os = getOutputStream(filename);

				if(os == null)	// e.g. if we asked to write to a directory instead of a file...
					continue;

				int totalBytes = 0;

				for(int numBytes = zin.read(buffer); numBytes > 0; numBytes = zin.read(buffer))
				{
					os.write(buffer, 0, numBytes);
					totalBytes += numBytes;
				} 
				os.close();				
				Reporter.verbose("Wrote " + totalBytes + " to " + filename);//NOI18N
				explodedFiles.add(filename);
			}
		}
		catch(IOException e)
		{
			throw new ZipFileException(e);
		}
		finally
		{
			Reporter.verbose("Closing zin...");//NOI18N
			try 
			{ 
				zin.close(); 
			}
			catch(IOException e) 
			{
				throw new ZipFileException("Got an exception while trying to close Jar input stream: " + e);//NOI18N
			}
		}
		Reporter.info("Successfully Exploded " + zipFilename + " to " + explodeDirName);//NOI18N
		return ContainerHelper.toStringArray(explodedFiles);
	}

	/***********************************************************************
	/******************************** Private ******************************
	/***********************************************************************/

	private void createFileNameList()	throws ZipFileException
	{
		ZipInputStream zin;

		assertIt( (files != null) ? false : true, "createFileNameList() called a second time.  Should only be called once and only once!" );//NOI18N
		files = new ArrayList();
		zin = null;
		try
		{
			ZipEntry ze;

			zin = zipStream; // new ZipInputStream( new FileInputStream( zipFile ) );
			while( (ze = zin.getNextEntry()) != null )
			{
				String name = ze.getName();
				zin.closeEntry();
				files.add(name);
			}
			zin.close();
		}
		catch( IOException e)
		{
			Reporter.error(e + "  " + zipFile);//NOI18N
			throw new ZipFileException(e);
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private void checkZipFile() throws ZipFileException
	{
		assertIt(zipFilename);
		zipFile = new File( zipFilename );
		assertIt( zipFile.exists(), "zipFile (" + zipFilename + ") doesn't exist" );//NOI18N
		assertIt( !zipFile.isDirectory(), "zipFile (" + zipFilename + ") is actually a directory!" );//NOI18N
		//createFileNameList();
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private void checkExplodeDir() throws ZipFileException
	{
		File dir;

		assertIt(explodeDirName);
		explodeDir = new File(explodeDirName);
		
		assertIt(explodeDir.exists(),		"Target Directory doesn't exist: " + explodeDirName );//NOI18N
		assertIt(explodeDir.isDirectory(),	"Target Directory isn't a directory: " + explodeDirName );//NOI18N
		assertIt(explodeDir.canWrite(),		"Can't write to Target Directory: " + explodeDirName );//NOI18N
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private static boolean isSpecial(String filename)
	{
		return filename.toUpperCase().startsWith(specialDir.toUpperCase());
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private void createDirs() throws ZipFileException
	{
		// go through the array of filenames from the zip -- and create the required directory
		// structure in the explode directory...

		Assertion.check(explodeDir, "Programmer Error -- need to setup explodeDir");//NOI18N
		Iterator iter = files.iterator();
		
		while(iter.hasNext())
		{
			String	fname		= (String) iter.next();
			File	fullpath	= new File(explodeDir, fname);
			File	newDir		= fullpath.getParentFile();
			
			if(newDir.mkdirs())
			{	// note:  it returns false if dir already exists...
				Reporter.verbose("Created new directory:  " + newDir);//NOI18N
			}

			if(fullpath.delete())	// wipe-out pre-existing files
				Reporter.info("deleted pre-existing file: " + fullpath); //NOI18N
			
			assertIt(newDir.exists() && newDir.isDirectory(), "Couldn't create directory: " + newDir);//NOI18N
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private FileOutputStream getOutputStream(String filename) throws ZipFileException
	{
		Assertion.check(explodeDir, "Programmer Error -- need to setup explodeDir");//NOI18N
		File f = new File(explodeDir, filename);

		if(f.isDirectory())
		{
			Reporter.warn("Weird!  A directory is listed as an entry in the jar file -- skipping...");//NOI18N
			return null;
		}

		try
		{
			return new FileOutputStream(f);
		}
		catch(FileNotFoundException e)
		{
			throw new ZipFileException(e);
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
		System.out.println( s ); //NOI18N
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private static void assertIt(String s) throws ZipFileException
	{
		if( s == null || s.length() < 0 )
			throw new ZipFileException();
		else
			return;
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private static void assertIt(String s, String mesg) throws ZipFileException
	{
		if( s == null || s.length() < 0 )
			throw new ZipFileException( mesg );
		else
			return;
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private static void assertIt(boolean b) throws ZipFileException
	{
		if( !b )
			throw new ZipFileException();
		else
			return;
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private static void assertIt(boolean b, String mesg) throws ZipFileException
	{
		if( !b )
			throw new ZipFileException( mesg );
		else
			return;
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] String_1darray1)
	{
		try
		{
			ZipFile zip = new ZipFile("D:\\test\\AccessorTestEnterpriseBean.jar", "D:/test/zipOut");//NOI18N
			
			pr("" + zip);//NOI18N
                        zip.explode();
		}
		catch(ZipFileException e)
		{
			pr("ZipFileException: " + e);//NOI18N
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private					String		zipFilename		= null;
	private					String		explodeDirName	= null;
	private					File		zipFile			= null;
	private					File		explodeDir		= null;
	private					ArrayList	files			= null;
	private static final	String		specialDir		= "META-INF/";//NOI18N
	private					byte[]		buffer			= new byte[16384];
        private ZipInputStream zipStream = null;
}
