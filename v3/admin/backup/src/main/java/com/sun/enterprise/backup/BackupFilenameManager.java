/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

/*
 * BackupFilenameManager.java
 *
 * Created on August 13, 2004, 12:10 PM
 */

package com.sun.enterprise.backup;

import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Manage filenames to select for backups.
 * @author bnevins
 */
class BackupFilenameManager
{
	BackupFilenameManager(File backupDir, String domainName) throws BackupException
	{
		this.dir = backupDir;
                this.domainName = domainName;
		findZips();
		findLatest();
	}
	
	///////////////////////////////////////////////////////////////////////////

	File next() throws BackupException
	{
		int newVersionNum = 1;
		
		if(latestVersion != null)
			newVersionNum = latestVersion.num + 1;

                // Generate a file name of form domain1_2010_06_11_v0001.zip
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd");
                Date date = new Date();
                String fname = domainName + "_" + formatter.format(date) + "_v";
		String suffix = padWithLeadingZeroes(newVersionNum);

                desc = domainName + " backup created on "
                            + formatter.format(date) + " by user "
                            + System.getProperty(Constants.PROPS_USER_NAME);
		return new File(dir, fname + suffix + ".zip");
	}
	
	///////////////////////////////////////////////////////////////////////////

	File latest() throws BackupWarningException
	{
		if(latestVersion == null)
			throw new BackupWarningException("backup-res.NoBackupFiles", dir);
		
		return latestVersion.zip;
	}
	
	///////////////////////////////////////////////////////////////////////////////

	/** Looks through the backups directory and assembles
	 * a list of all backup files found.
	 * @throws BackupWarningException if there are no backup zip files
	 */	
	private void findZips() throws BackupWarningException
	{
		File[] zips = dir.listFiles(new ZipFilenameFilter());
		int len = 0;
		
		if(zips != null)
			len = zips.length;
		
		zipsAndNumbers = new ZipFileAndNumber[len];
		
		for(int i = 0; i < len; i++)
		{
			zipsAndNumbers[i] = new ZipFileAndNumber(zips[i]);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////

	/** Looks through the list of zips and sets the one with the biggest version number
	 */	
	private void findLatest() throws BackupWarningException
	{
		int biggest = 0;
		
		for(int i = 0; i < zipsAndNumbers.length; i++)
		{
			int curr = zipsAndNumbers[i].num;
			
			if(curr > biggest)
			{
				biggest = curr;
				latestVersion = zipsAndNumbers[i];
				LoggerHelper.fine(zipsAndNumbers[i].zip.toString() + " newest backup so far...");
			}
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////

	/** Convert the array of zip filenames into an array of the number suffixes.
	 */	
	private String padWithLeadingZeroes(int num) throws BackupException
	{
		if(num < 10)
			return "0000" + num;

		if(num < 100)
			return "000" + num;
		
		if(num < 1000)
			return "00" + num;
		
		if(num < 10000)
			return "0" + num;

		if(num < 100000)
			return "" + num;
		
		throw new BackupException("Latest version >= 100,000.  Delete some backup files.");
	}

        /*
         * Return description string
         *
         */
        public String getCustomizedDescription() {
            return desc;
        }
        
	///////////////////////////////////////////////////////////////////////////

       
	private static class ZipFileAndNumber
	{
		private ZipFileAndNumber(File zip)
		{
			this.zip = zip;
			String fname = zip.getName();
			
			if(isValid())
			{
				
				fname = fname.substring(fname.lastIndexOf("_v")+2, fname.length() - 4);
				try
				{
					num = Integer.parseInt(fname);
				}
				catch(Exception e)
				{
					// nothing to do -- num is already set to -1
				}
			}
		}
		
		/**
		 * make sure that:
		 * (1) the filename is the right format
		 * (2) that it is internally correct (has a status file with a timestamp
		 **/
		
		private boolean isValid()
		{
			Status status = new Status();
			long time = status.getInternalTimestamp(zip);
			return time > 0;
		}
                                
		private File	zip;
		private int		num = -1;
	}

	///////////////////////////////////////////////////////////////////////////

	private File			dir;
        private String                  domainName;
        private String                  desc;
	private	ZipFileAndNumber[]	zipsAndNumbers;
	private ZipFileAndNumber	latestVersion;
	
	///////////////////////////////////////////////////////////////////////////////


	public static void main(String[] args)
	{
		try
		{
			File f = new File("c:/tmp/test");
			BackupFilenameManager mgr = new BackupFilenameManager(f, "foo");
			File fnew = mgr.next();
			System.out.println("Next backup file: " + fnew);
			File fold = mgr.latest();
			System.out.println("Latest backup file: " + fold);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
