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

package com.sun.enterprise.backup;

import com.sun.enterprise.backup.util.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author  Byron Nevins
 */

public class RestoreManager extends BackupRestoreManager
{
	public RestoreManager(BackupRequest req) throws BackupException
	{
		super(req);
	}
	
	///////////////////////////////////////////////////////////////////////////////

	public String restore() throws BackupException
	{
		try
		{
			checkDomainName();
			ZipFile zf = new ZipFile(request.backupFile, tempRestoreDir);
			zf.explode();
			sanityCheckExplodedFiles();
			copyBackups();
			atomicSwap();
			setPermissions();
			String mesg = readAndDeletePropsFile();
			return mesg;
		}
		catch(BackupException be)
		{
			throw be;
		}
		catch(Exception e)
		{
			throw new BackupException("Restore Error", e);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////

	void init() throws BackupException
	{
		super.init();

		if(request.backupFile == null)
			initWithNoSpecifiedBackupFile();
		else
			initWithSpecifiedBackupFile();
		
		tempRestoreDir = new File(request.domainsDir, request.domainName +
			"_" + System.currentTimeMillis());
	}
	
	///////////////////////////////////////////////////////////////////////////////

	private void initWithSpecifiedBackupFile() throws BackupException
	{
		if(request.backupFile.length() <= 0)
			throw new BackupException("backup-res.CorruptBackupFile", request.backupFile);

		if(!FileUtils.safeIsDirectory(request.domainDir))
			request.domainDir.mkdirs();

		backupDir = new File(request.domainDir, Constants.BACKUP_DIR);

		// It's NOT an error to not exist.  The domain may not exist currently and, besides,
		// they are specifying the backup-file from anywhere potentially...
		if(!FileUtils.safeIsDirectory(backupDir))
			backupDir = null;

		//throw new BackupException("NOT YET IMPLEMENTED");
		
	}
	
	///////////////////////////////////////////////////////////////////////////////

	private void initWithNoSpecifiedBackupFile() throws BackupException
	{
		// if they did NOT specify a backupFile, then we *must* have a pre-existing
		// backups directory in a pre-existing domain directory.
		
		if(!FileUtils.safeIsDirectory(request.domainDir))
			throw new BackupException("backup-res.NoDomainDir", request.domainDir);

		backupDir = new File(request.domainDir, Constants.BACKUP_DIR);
		
		// It's an error to not exist...
		if(!FileUtils.safeIsDirectory(backupDir))
			throw new BackupException("backup-res.NoBackupDir", backupDir);

		BackupFilenameManager bfmgr = new BackupFilenameManager(backupDir);
		request.backupFile = bfmgr.latest();
		
		//request.backupFile = getNewestZip(backupDir);
	}
	
	///////////////////////////////////////////////////////////////////////////////

	/*
	private File getNewestZip(File dir) throws BackupException
	{
		File newestFile = null;
		long newestTime = 0;

		File[] zips = dir.listFiles(new ZipFilenameFilter());
		
		for(int i = 0; i < zips.length; i++)
		{
			//long time = zips[i].lastModified();
			Status status = new Status();
			long time = status.getInternalTimestamp(zips[i]);

			String msg = "filename: " + zips[i] + ", ts= " + time;
			if(time > newestTime)
			{
				newestFile = zips[i];
				newestTime = time;
				msg += " --- newest file so far";
			}
			else
				msg += " -- NOT newest";

			System.out.println(msg);
		}
		
		if(newestFile == null)
			throw new BackupException("backup-res.NoBackupFiles", dir);
		
		return newestFile;
	}
	*/
	///////////////////////////////////////////////////////////////////////////////

	private void copyBackups() throws IOException	
	{ 
		if(backupDir != null)
		{
			File tempRestoreDirBackups = new File(tempRestoreDir, Constants.BACKUP_DIR);
			FileUtils.copyTree(backupDir, tempRestoreDirBackups);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////

	private void atomicSwap() throws BackupException	
	{
		// 1 -- rename original domain dir
		// 2 -- rename new restored dir to domain dir
		// 3 -- delete original domain dir

		// tempRestoreDir
		File oldDomain = new File(request.domainsDir, 
			request.domainName + OLD_DOMAIN_SUFFIX + System.currentTimeMillis());
		
		// On Error -- just fail and delete the new files
		if(!request.domainDir.renameTo(oldDomain))
		{
			FileUtils.whack(tempRestoreDir);
			throw new BackupException("backup-res.CantRenameOriginalDomain", request.domainDir);
		}
		
		// On Error -- Delete the new files and undo the rename that was done
		//successfully above
		if(!tempRestoreDir.renameTo(request.domainDir))
		{
			oldDomain.renameTo(request.domainDir);
			FileUtils.whack(tempRestoreDir);
			throw new BackupException("backup-res.CantRenameRestoredDomain");
		}	
		
		FileUtils.whack(oldDomain);
	}
	
	///////////////////////////////////////////////////////////////////////////////

	private String readAndDeletePropsFile()
	{
		// The "backup.properties" file from the restored zip should be
		// in the domain dir now.
		File propsFile = new File(request.domainDir, Constants.PROPS_FILENAME);
		Status status = new Status();

		String mesg = StringHelper.get("backup-res.SuccessfulRestore", 
			request.domainName, request.domainDir );

		if(request.terse == false)
			mesg += "\n" + status.read(propsFile, false);
		
		if(!propsFile.delete())
			propsFile.deleteOnExit();
		
		return mesg;
	}

	/** zip is platform dependent -- so non-default permissions are gone!
	 */
	
	private void setPermissions()
	{
		File backups		= new File(request.domainDir, "backups");
		File bin			= new File(request.domainDir, "bin");
		File config			= new File(request.domainDir, "config");
		File webtmp			= new File(request.domainDir, "generated/tmp");
		File masterPassword	= new File(request.domainDir, "master-password");

		// note that makeExecutable(File f) will make all the files under f
		// executable if f happens to be a directory.
		FileUtils.makeExecutable(bin);
		
		FileUtils.protect(backups);
		FileUtils.protect(config);
		FileUtils.protect(masterPassword);
		FileUtils.protect(webtmp);
		
 		// Jan 19, 2005 -- rolled back the fix for 6206176.  It has been decided
		// that this is not a bug but rather a security feature.
		//FileUtils.whack(webtmp);

		// fix: 6206176 -- instead of setting permissions for the tmp dir, 
		// we just delete it.   This will allow, say, user 'A' to do the restore,
		// and then allow user 'B' (including root) to start the domain without
		// getting a web-container error.
		// see: bug 6194504 for the tmp dir details
		//old:  
		//new:  
	}
	
	///////////////////////////////////////////////////////////////////////////////

	private void sanityCheckExplodedFiles() throws BackupException
	{
		// Is the "magic" properties file where it is supposed to be?
		
		File statusFile = new File(tempRestoreDir, Constants.PROPS_FILENAME);
		
		if(!statusFile.exists())
		{
			// cleanup -- we are officially failing the restore!
			FileUtils.whack(tempRestoreDir);
			throw new BackupException("backup-res.RestoreError.CorruptBackupFile.NoStatusFile", request.domainName);
		}
	}	
	
	///////////////////////////////////////////////////////////////////////////////

	private void checkDomainName() throws BackupException
	{
		Status status = new Status();
		status.read(request.backupFile);
		String buDomainName = status.getDomainName();
		
		if(buDomainName == null)
        {
            //this means the backup zip is bad...
			throw new BackupException(StringHelper.get("backup-res.CorruptBackupFile", request.backupFile));
        }
        else if(!request.domainName.equals(buDomainName))
        {
            System.out.println(StringHelper.get("backup-res.DomainNameDifferentWarning", buDomainName, request.domainName));

            //LoggerHelper.warning(StringHelper.get("backup-res.DomainNameDifferentWarning", buDomainName, request.domainName));
            //throw new BackupException(StringHelper.get("backup-res.DomainNameDifferent", buDomainName, request.domainName));
        }
	}

	///////////////////////////////////////////////////////////////////////////////

	private static final	String OLD_DOMAIN_SUFFIX = "_beforeRestore_";
	private File tempRestoreDir;
	private File backupDir;
}
