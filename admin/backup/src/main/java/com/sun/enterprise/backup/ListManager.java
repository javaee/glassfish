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


/*
 * ListManager.java
 *
 * Created on March 30, 2004, 9:01 PM
 */

package com.sun.enterprise.backup;

import com.sun.enterprise.backup.util.FileUtils;
import java.io.*;

/**
 *
 * This class is responsible for returning information about backups.
 * It opens each backup zip file and examines the properties file for the 
 * information that was stored when the backup was performed.
 * It returns all this information to CLI as a String.
 *
 * @author  bnevins
 */
public class ListManager extends BackupRestoreManager
{
	
	/** Creates an instance of ListManager.
	 * The superclass will call init() so it is
	 * possible for Exceptions to be thrown.
	 * @param req The BackupRequest instance with required information.
	 * @throws BackupException if there is a fatal error with the BackupRequest object.
	 * @throws BackupWarningException if there is a non-fatal error with the BackupRequest object.
	 */	
	public ListManager(BackupRequest req) throws BackupException, BackupWarningException
	{
		super(req);
	}

	/** 
	 * Find all backup zip files in a domain and return a String
	 * summarizing information about the backup.
	 * The summary is shorter if the "terse" option is true.
	 * @return a String summary
	 * @throws BackupException if there is a fatal error
	 */
	public String list() throws BackupException
	{
		StringBuffer sb = new StringBuffer();
		
		findZips();
		
		// it is GUARANTEED that the length > 0
		for(int i = 0; i < zips.length; i++)
		{
			Status status = new Status();
			sb.append(status.read(zips[i], request.terse));
			sb.append("\n");
			
			if(request.terse == false)
				sb.append("\n");
		}
		
		return sb.toString();
	}

	
	///////////////////////////////////////////////////////////////////////////////
	// 
	/**
	 * Finish initializing the BackupRequest object.
	 * note: this method is called by the super class...
	 * @throws BackupException for fatal errors
	 * @throws BackupWarningException for non-fatal errors - these are errors
	 * where we can not continue execution.
	 */	
	void init() throws BackupException, BackupWarningException
	{
		super.init();
		
		if(!FileUtils.safeIsDirectory(request.domainDir))
			throw new BackupException("backup-res.NoDomainDir", request.domainDir);

		request.backupDir = new File(request.domainDir, Constants.BACKUP_DIR);

		// It's a warning to not exist...
		if(!FileUtils.safeIsDirectory(request.backupDir))
			throw new BackupWarningException("backup-res.NoBackupDir", request.backupDir);
	}
	
	///////////////////////////////////////////////////////////////////////////////

	/** Looks through the backups directory and assembles
	 * a list of all backup files found.
	 * @throws BackupWarningException if there are no backup zip files
	 */	
	void findZips() throws BackupWarningException
	{
		zips = request.backupDir.listFiles(new ZipFilenameFilter());

		if(zips == null || zips.length <= 0)
			throw new BackupWarningException("backup-res.NoBackupFiles", request.backupDir);
	}
	
	///////////////////////////////////////////////////////////////////////////////

	File[] zips;
}
