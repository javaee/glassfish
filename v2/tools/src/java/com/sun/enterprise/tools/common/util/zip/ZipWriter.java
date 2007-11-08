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
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.sun.enterprise.tools.common.util.diagnostics.Reporter;
import com.sun.enterprise.tools.common.util.diagnostics.StackTrace;
import com.sun.enterprise.tools.common.util.Assertion;
import com.sun.enterprise.tools.common.util.ContainerHelper;

public class ZipWriter
{
	public ZipWriter(String zipFilename, String dirName, String[] fileList) throws ZipFileException
	{
		try
		{
			// note -- these asserts will be caught & repackaged as a ZipFileException
			Reporter.assertIt(zipFilename); //NOI18N
			Reporter.assertIt(dirName); //NOI18N
			Reporter.assertIt(fileList); //NOI18N
			Reporter.assertIt(fileList.length > 0); //NOI18N
			
			//make sure it's really a directory
			File f = new File(dirName);
			Reporter.assertIt(f.exists(),		"directory (" + dirName + ") doesn't exist");//NOI18N
			Reporter.assertIt(f.isDirectory()); //NOI18N
			
			// change the filename to be full-path & UNIX style
			try
			{
				dirName = f.getCanonicalPath();
			}
			catch(IOException e)
			{
				Reporter.warn("Couldn't getCanonicalPath() for " + dirName);//NOI18N
			}
			
			dirName = dirName.replace('\\', '/');	// all UNIX-style filenames...
			
			
			// we need the dirname to end in a '/'
			if(!dirName.endsWith("/"))//NOI18N
				dirName += "/";//NOI18N

			// make sure the zipFile requested isn't the name of an existing directory
			f = new File(zipFilename);
			Reporter.assertIt(!f.isDirectory(), "zipFile (" + zipFilename + ") is actually a directory!" );//NOI18N

			for(int i = 0; i < fileList.length; i++)
			{
				fileList[i] = fileList[i].replace('\\', '/');	// just in case...
			}
			
			this.zipFilename	= zipFilename;
			this.dirName		= dirName;
			this.fileList		= fileList;
                        zipStream = new ZipOutputStream(new FileOutputStream(zipFilename));
		}
		catch(Exception f)
		{
                        Reporter.critical(new StackTrace(f)); //NOI18N
			throw new ZipFileException(f);
		}
	}

	public ZipWriter(OutputStream outStream, String dirName, String[] fileList) throws ZipFileException
	{
		try
		{
			// note -- these asserts will be caught & repackaged as a ZipFileException
			//Reporter.assertIt(zipFilename); //NOI18N
			Reporter.assertIt(dirName); //NOI18N
			Reporter.assertIt(fileList); //NOI18N
			Reporter.assertIt(fileList.length > 0); //NOI18N
			
			//make sure it's really a directory
			File f = new File(dirName);
			Reporter.assertIt(f.exists(),		"directory (" + dirName + ") doesn't exist");//NOI18N
			Reporter.assertIt(f.isDirectory()); //NOI18N
			
			// change the filename to be full-path & UNIX style
			try
			{
				dirName = f.getCanonicalPath();
			}
			catch(IOException e)
			{
				Reporter.warn("Couldn't getCanonicalPath() for " + dirName);//NOI18N
			}
			
			dirName = dirName.replace('\\', '/');	// all UNIX-style filenames...
			
			
			// we need the dirname to end in a '/'
			if(!dirName.endsWith("/"))//NOI18N
				dirName += "/";//NOI18N

			// make sure the zipFile requested isn't the name of an existing directory
			//f = new File(zipFilename);
			//Reporter.assertIt(!f.isDirectory(), "zipFile (" + zipFilename + ") is actually a directory!" );//NOI18N

			for(int i = 0; i < fileList.length; i++)
			{
				fileList[i] = fileList[i].replace('\\', '/');	// just in case...
			}
			
			//this.zipFilename	= zipFilename;
			this.dirName		= dirName;
			this.fileList		= fileList;
                        zipStream = new ZipOutputStream(outStream);
		}
		catch(Assertion.Failure f)
		{
			throw new ZipFileException(f);
		}
	}

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void write()  throws ZipFileException
	{
		try
		{
			//zipStream = new ZipOutputStream(new FileOutputStream(zipFilename));

			for(int i = 0; i < fileList.length; i++)
			{
				addEntry(fileList[i]);
			}
			
			zipStream.close();
		}
		catch(ZipFileException z)
		{
                    Reporter.critical(new StackTrace(z)); //NOI18N
			throw z;
		}
		catch(Exception e)
		{
                    Reporter.critical(new StackTrace(e)); //NOI18N
			throw new ZipFileException(e);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void addEntry(String entryName)  throws ZipFileException, IOException
	{
		int					totalBytes	= 0;
		FileInputStream		in			= new FileInputStream(dirName + entryName);
		ZipEntry			ze			= new ZipEntry(entryName);
		
		zipStream.putNextEntry(ze);

		for(int numBytes = in.read(buffer); numBytes > 0; numBytes = in.read(buffer))
		{
			zipStream.write(buffer, 0, numBytes);
			totalBytes += numBytes;
		}

		zipStream.closeEntry();
		Reporter.verbose("Wrote " + entryName + " to Zip File.  Wrote " + totalBytes + " bytes.");//NOI18N
	}		

		
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public String toString()
	{
		String s = "Zip File Name: " + zipFilename + "\n";//NOI18N
		s += "Directory Name: " + dirName + "\n";//NOI18N
		s += "***** File Contents *********\n";//NOI18N
		s += ContainerHelper.toOneString(fileList);
		
		return s;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] notUsed)
	{
		Reporter.setSeverityLevel(0); //NOI18N
		
		try
		{
			String[] array = { "hello.txt", "a\\a.txt", "a\\b/b.txt" };//NOI18N
			ZipWriter zw = new ZipWriter("E:\\temp\\hello/ZipWriter.jar", "E:/Temp\\hello", array);//NOI18N
			zw.write();
			Reporter.verbose("" + zw);//NOI18N
		}
		catch(ZipFileException e)
		{
			Reporter.verbose("ZipFileException: " + e);//NOI18N
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	private					String			zipFilename		= null;
	private					String			dirName			= null;
	private					ZipOutputStream zipStream		= null;
	private					String[]	fileList		= null;
	private					byte[]		buffer			= new byte[16384];
}
