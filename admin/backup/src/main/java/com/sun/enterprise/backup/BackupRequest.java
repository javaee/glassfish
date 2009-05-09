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
 * BackupRequest.java
 *
 * Created on February 22, 2004, 1:40 AM
 */

package com.sun.enterprise.backup;

import java.io.*;
import java.util.*;

import com.sun.enterprise.backup.util.ObjectAnalyzer;
import com.sun.enterprise.backup.util.FileUtils;

/**
 * This class holds all of the values that the caller needs.  
 * An instance of this class can be used to create a request object.
 * @author  bnevins
 */

public class BackupRequest
{
	/**
	 * Create an instance
	 **/
	public BackupRequest(String domainsDirName, String domain, String desc)
	{
		setDomainsDir(domainsDirName);
		setDescription(desc);
		domainName	= domain;
	}
	
	/**
	 * Create an instance
	 **/
	public BackupRequest(String domainsDirName, String domain, String desc, String backupFileName)
	{
		this(domainsDirName, domain, desc);
		setBackupFile(backupFileName);
	}
	
	///////////////////////////////////////////////////////////////////////////

	public void setTerse(boolean b)
	{
		terse = b;
	}
	
	///////////////////////////////////////////////////////////////////////////

	public void setVerbose(boolean b)
	{
		verbose = b;
	}
	
	///////////////////////////////////////////////////////////////////////////

	public String toString()
	{
		return ObjectAnalyzer.toString(this);
	}
	
	///////////////////////////////////////////////////////////////////////////
	////////////     Private Methods     //////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	
	private void setDomainsDir(String name)
	{
		domainsDir	= FileUtils.safeGetCanonicalFile(new File(name));
	}

	///////////////////////////////////////////////////////////////////////////

	private void setBackupFile(String name)
	{
		backupFile	= FileUtils.safeGetCanonicalFile(new File(name));
	}

	///////////////////////////////////////////////////////////////////////////

	private void setDescription(String desc)
	{
		description = desc;
	}
	
	///////////////////////////////////////////////////////////////////////////
	////////////     Variables     ////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	
	final static String[] excludeDirs = {Constants.BACKUP_DIR + "/"}; 

	File	domainsDir;
	String	domainName;
	String	description;

	// VARIABLES POSSIBLY SET AT RUNTIME
	File	backupFile;
	
	// VARIABLES SET AT RUNTIME
	File	domainDir;
	long	timestamp;
	
	// variables used ONLY by ListManager
	// The reason it is here instead of in ListManager is so that
	// we can get a nice concise centralized display of ALL the variables
	// for all commands
	
	File	backupDir;
	boolean	terse	= false;
	boolean	verbose	= false;
}

