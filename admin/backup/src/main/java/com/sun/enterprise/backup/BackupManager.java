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


public class BackupManager extends BackupRestoreManager
{
	public BackupManager(BackupRequest req) throws BackupException
	{
		super(req);
	}
	
	///////////////////////////////////////////////////////////////////////////////

	public final String backup() throws BackupException
	{
		String mesg = StringHelper.get("backup-res.SuccessfulBackup");
		String statusString = writeStatus();
		
		if(request.terse == false)
		{
			mesg += "\n\n" + statusString;
		}
		
		try
		{
			ZipStorage zs = new ZipStorage(request);
			zs.store();
			return mesg;
		}
		finally
		{
			status.delete();
			FileUtils.protect(request.backupFile);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////

	void init() throws BackupException
	{
		super.init();
		
		if(request.backupFile != null)
			throw new BackupException("backup-res.InternalError", "No backupFilename may be specified for a backup -- it is reserved for restore operations only.");
		
		if(!FileUtils.safeIsDirectory(request.domainDir))
			throw new BackupException("backup-res.NoDomainDir", request.domainDir);

		File backupDir = new File(request.domainDir, Constants.BACKUP_DIR);

		// not an error for this directory to not exist yet
		backupDir.mkdirs();

		// NOW it's an error to not exist...
		if(!FileUtils.safeIsDirectory(backupDir))
			throw new BackupException("backup-res.NoBackupDirCantCreate", backupDir);

		String ts = "" + request.timestamp + ".zip";
		BackupFilenameManager bfmgr = new BackupFilenameManager(backupDir);
		request.backupFile = bfmgr.next();
		//request.backupFile = new File(backupDir, ts);
	}
	
	///////////////////////////////////////////////////////////////////////////////
	
	private String writeStatus()
	{
		status = new Status();
		return status.write(request);
	}
	
	///////////////////////////////////////////////////////////////////////////////

	Status status;
}
