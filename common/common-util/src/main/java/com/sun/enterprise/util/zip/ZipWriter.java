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

package com.sun.enterprise.util.zip;

import java.io.*;
import java.util.logging.*;
import java.util.zip.*;

import com.sun.enterprise.util.io.FileListerRelative;

public class ZipWriter
{
	public ZipWriter(String zipFilename, String dirName) throws ZipFileException
	{
		init(zipFilename, dirName);
		createItemList(null);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public ZipWriter(String zipFilename, String dirName, ZipItem[] theItems) throws ZipFileException
	{
		items = theItems;
		init(zipFilename, dirName);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public ZipWriter(String zipFilename, String dirName, String[] fileList) throws ZipFileException
	{
		init(zipFilename, dirName);
		createItemList(fileList);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public ZipWriter(OutputStream outStream, String dirName, String[] fileList) throws ZipFileException
	{
		init(outStream, dirName);
		createItemList(fileList);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void init(String outFileName, String dirName) throws ZipFileException
	{
		try
		{
			init(new FileOutputStream(outFileName), dirName);
		}
		catch(Exception e)
		{
			throw new ZipFileException(e);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void init(OutputStream outStream, String dirName) throws ZipFileException
	{
		try
		{
            if(dirName == null)
                throw new IllegalArgumentException("null dirName");
			
			//make sure it's really a directory
			File f = new File(dirName);

            if(!f.exists())
                throw new ZipFileException("directory (" + dirName + ") doesn't exist");

            if(!f.isDirectory())
                throw new ZipFileException(dirName + " is not a directory");
			
			// change the filename to be full-path & UNIX style
			try
			{
				dirName = f.getCanonicalPath();
			}
			catch(IOException e)
			{
				dirName = f.getAbsolutePath();
			}
			
			dirName = dirName.replace('\\', '/');	// all UNIX-style filenames...
			
			
			// we need the dirname to end in a '/'
			if(!dirName.endsWith("/"))
				dirName += "/";

			
			this.dirName		= dirName;
			zipStream			= new ZipOutputStream(outStream);
		}
        catch(ZipFileException zfe) 
        {
            throw zfe;
        }
        catch(Throwable t)
		{
			throw new ZipFileException(t);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Does not throw an exception when there is a duplicate zip entry.
     *
     * @throws  ZipFileException   if an error while creating the archive
     */
	public void safeWrite()  throws ZipFileException
	{
		try
		{
			for(int i = 0; i < items.length; i++)
			{
                try 
                {
                    addEntry(items[i]);
                } 
                catch (ZipException e) 
                {
                    // ignore - duplicate zip entry
                }
			}
			
			zipStream.close();
		}
		catch(ZipFileException z)
		{
			throw z;
		}
		catch(Exception e)
		{
			throw new ZipFileException(e);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void write()  throws ZipFileException
	{
		try
		{
			for(int i = 0; i < items.length; i++)
			{
				addEntry(items[i]);
			}
			
			zipStream.close();
		}
		catch(ZipFileException z)
		{
			throw z;
		}
		catch(Exception e)
		{
			throw new ZipFileException(e);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void addEntry(ZipItem item)  throws ZipFileException, IOException
	{
		int					totalBytes	= 0;
		FileInputStream		in			= new FileInputStream(item.file);
		ZipEntry			ze			= new ZipEntry(item.name);
		
		zipStream.putNextEntry(ze);

		for(int numBytes = in.read(buffer); numBytes > 0; numBytes = in.read(buffer))
		{
			zipStream.write(buffer, 0, numBytes);
			totalBytes += numBytes;
		}
		
		/* Bug 4753245
		 * WBN 11/22/02
		 * The in.close() method was missing -- causing problems when trying to delete this file in
		 * other code -- at least until the garbage collector gets around to closing it.
		 * Normally I wouldn't put this inside a try/catch -- an error ought to be returned,
		 * but this is a special low-risk-toleration time.  Adding the try/catch is safer.  The
		 * code will behave exactly like before if an Exception is thrown.  It can't possibly hurt anything
		 * to add the close()
		 */
		try	// note: feel free to remove this try sometime in the future!
		{
			in.close();
		}
		catch(Exception e)
		{
			// ignore it
			Logger.getAnonymousLogger().warning("Couldn't close the FileInputStream for the file: " + item.file);
		}
		
		zipStream.closeEntry();
		Logger.getAnonymousLogger().finer("Wrote " + item.name + " to Zip File.  Wrote " + totalBytes + " bytes.");
	}		

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void createItemList(String[] files) throws ZipFileException
	{
		try
		{
			if(files == null)
			{
				FileListerRelative lister = new FileListerRelative(new File(dirName));
				files = lister.getFiles();
			}

			if(files.length <= 0)
				throw new ZipFileException("No files to add!");

			items = new ZipItem[files.length];

			for(int i = 0; i < files.length; i++)
			{
				File f = new File(dirName + files[i]);
				items[i] = new ZipItem(f, files[i].replace('\\', '/'));	// just in case...
			}
		}
		catch(Throwable t)
		{
			throw new ZipFileException(t);
		}
			
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	String getDirName()
	{
		return dirName;
	}
		
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static void usage()
	{
		System.out.println("usage: java com.elf.util.zip.ZipWriter zip-filename directory-name");
		System.exit(1);
	}
		
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args)
	{
		if(args == null || args.length != 2)
			usage();
		
		try
		{
			ZipWriter zw = new ZipWriter(args[0], args[1]);
			zw.write();
		}
		catch(ZipFileException e)
		{
			System.exit(0);
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	//private					String			zipFilename		= null;
	private					String			dirName			= null;
	private					ZipOutputStream zipStream		= null;
	private					byte[]			buffer			= new byte[16384];
	private					ZipItem[]		items			= null;
}
