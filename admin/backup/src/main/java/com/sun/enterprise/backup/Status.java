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
 * Status.java
 *
 * Created on March 27, 2004, 10:40 PM
 */

package com.sun.enterprise.backup;

import com.sun.enterprise.backup.util.FileUtils;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 *
 * @author  Byron Nevins
 */
class Status
{
	String write(BackupRequest req)
	{
		props = new Properties();
		request = req;
		statusFile = new File(request.domainDir, Constants.PROPS_FILENAME);

		try
		{
			setProps();
			FileOutputStream out = new FileOutputStream(statusFile);
			props.store(out, Constants.PROPS_HEADER);
			return propsToString(false);
		}
		catch(Exception e)
		{
			return StringHelper.get("backup-res.CantWriteStatus", statusFile);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////

	/**
	 * @param file Either a zip file that contains backup.properties -- or backup.properties
	 * itself.  terse is automatically set to true.
	 * @return a String summary of the backup
	 */	
	String read(File file)
	{
		return read(file, true);
	}
	
	///////////////////////////////////////////////////////////////////////////////

	/**
	 * @param file Either a zip file that contains backup.properties -- or backup.properties
	 * itself.
	 * @param terse if true, give a short summary
	 * @return a String summary of the backup
	 */	
	String read(File file, boolean terse)
	{
		props = null;
		
		setPropsFromFile(file);
		if(props == null)
		{
			return badStatusFileMessage(file);
		}

		return propsToString(terse);
	}

	/**
	 * open the zip file, parse the status file and return the timestamp
	 * of when it was created.
	*/

	long getInternalTimestamp(File f)
	{
		props = null;
		setPropsFromFile(f);

		try
		{
			String s = props.getProperty(Constants.PROPS_TIMESTAMP_MSEC);
			return Long.parseLong(s);
		}
		catch(Exception e)
		{
			LoggerHelper.warning(badStatusFileMessage(f));
			return 0;
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////
	
	void delete()
	{
		if(!statusFile.delete())
		{
			// TBD warning message
			statusFile.deleteOnExit();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////
	
	String getDomainName()
	{
		if(props == null)
			return null;
		
		return props.getProperty(Constants.PROPS_DOMAIN_NAME);
	}

	///////////////////////////////////////////////////////////////////////////////
	//////  PRIVATE METHODS AND DATA    ///////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////

	/**
	 * @param file Either a zip file that contains backup.properties -- or backup.properties
	 * itself.
	 * @param terse if true, give a short summary
	 * @return a String summary of the backup
	 */	
	private void setPropsFromFile(File file)
	{
		props = null;
		ZipInputStream zis = null;


		if(file.getName().toLowerCase().endsWith(".properties"))
		{
			readPropertiesFile(file);
			// props is now set...
			return;
		}

		try
		{
			zis = new ZipInputStream(new FileInputStream(file));
			ZipEntry ze;

			while( (ze = zis.getNextEntry()) != null )
			{
				if(ze.getName().equals(Constants.PROPS_FILENAME))
				{
					props = new Properties();
					props.load(zis);
					break;
				}
			}
			// props may be null
		}
		catch(Exception e)
		{
			// overkill...
			props = null;
		}
		finally
		{
			if(zis != null)
			{
				try
				{
					zis.close();
				}
				catch(Exception e)
				{
				}
			}
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////

	private void readPropertiesFile(File propsFile)
	{
		try
		{
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(propsFile));
			props = new Properties();
			props.load(in);
			in.close();
		}
		catch(IOException ioe)
		{
			props = null;
		}
	}

	///////////////////////////////////////////////////////////////////////////////

	private void setProps()
	{
		props.setProperty(Constants.PROPS_USER_NAME, System.getProperty(Constants.PROPS_USER_NAME));
		props.setProperty(Constants.PROPS_TIMESTAMP_MSEC,	"" + request.timestamp);
		props.setProperty(Constants.PROPS_DOMAINS_DIR,	FileUtils.safeGetCanonicalPath(request.domainsDir));
		props.setProperty(Constants.PROPS_DOMAIN_DIR,	FileUtils.safeGetCanonicalPath(request.domainDir));
		props.setProperty(Constants.PROPS_BACKUP_FILE,	FileUtils.safeGetCanonicalPath(request.backupFile));
		props.setProperty(Constants.PROPS_DOMAIN_NAME,	request.domainName);
		props.setProperty(Constants.PROPS_DESCRIPTION,	request.description);
		props.setProperty(Constants.PROPS_TIMESTAMP_HUMAN,	new Date(request.timestamp).toString());
	}

	///////////////////////////////////////////////////////////////////////////////
	
	private String propsToString(boolean terse)
	{
		final String pre = "backup-res.Props.";
		StringBuffer sb = new StringBuffer();
		
		
		if(terse)
		{
			sb.append(props.getProperty(Constants.PROPS_BACKUP_FILE));
		}

		else
		{
			sb.append(StringHelper.get(pre + Constants.PROPS_DESCRIPTION, props.getProperty(Constants.PROPS_DESCRIPTION)));
			sb.append("\n");
			sb.append(StringHelper.get(pre + Constants.PROPS_BACKUP_FILE, props.getProperty(Constants.PROPS_BACKUP_FILE)));
			sb.append("\n");
			sb.append(StringHelper.get(pre + Constants.PROPS_TIMESTAMP_HUMAN, props.getProperty(Constants.PROPS_TIMESTAMP_HUMAN)));
			sb.append("\n");
			sb.append(StringHelper.get(pre + Constants.PROPS_DOMAINS_DIR, props.getProperty(Constants.PROPS_DOMAINS_DIR)));
			sb.append("\n");
			sb.append(StringHelper.get(pre + Constants.PROPS_DOMAIN_DIR, props.getProperty(Constants.PROPS_DOMAIN_DIR)));
			sb.append("\n");
			sb.append(StringHelper.get(pre + Constants.PROPS_DOMAIN_NAME, props.getProperty(Constants.PROPS_DOMAIN_NAME)));
			sb.append("\n");
			sb.append(StringHelper.get(pre + Constants.PROPS_USER_NAME, props.getProperty(Constants.PROPS_USER_NAME)));
		}

		return sb.toString();
	}
	
	///////////////////////////////////////////////////////////////////////////////

	private String badStatusFileMessage(File file)
	{
		String msg = StringHelper.get("backup-res.Props.backup.file", file);
		msg += "\n";
		msg += StringHelper.get("backup-res.CorruptBackupFile.NoStatusFile");
		return msg;
	}
	
	///////////////////////////////////////////////////////////////////////////////
	
	private BackupRequest	request;
	private File			statusFile;
	private Properties		props;
}


