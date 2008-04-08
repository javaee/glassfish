/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

/*
 * foo.java
 *
 * Created on November 11, 2001, 12:09 AM
 */

package com.sun.enterprise.util.io;
import java.io.*;
import java.util.*;

/**
 *
 * @author  bnevins
 * @version 
 */
public abstract class FileLister
{
	FileLister(File root)
	{
		mainRoot = root;
		fileList = new ArrayList();
	}

	abstract protected boolean relativePath();
	
	public String[] getFiles()
	{
		getFilesInternal(mainRoot);
		String[] files = new String[fileList.size()];
		
		if(files.length <= 0)
			return files;

		int len = 0;
		
		if(relativePath())
			len = mainRoot.getPath().length() + 1;
		
		for(int i = 0; i < files.length; i++)
		{
			files[i] = ((File)fileList.get(i)).getPath().substring(len).replace('\\', '/');
		}
		
		Arrays.sort(files, String.CASE_INSENSITIVE_ORDER);
		return files;
	}
	
	
	public void getFilesInternal(File root)
	{
		File[] files = root.listFiles();
		
		for(int i = 0; i < files.length; i++)
		{
			if(files[i].isDirectory())
			{
				getFilesInternal(files[i]);
			}
			else
				fileList.add(files[i]);	// actual file
		}
	}
		
		
    




	private	ArrayList	fileList	= null;
	private File		mainRoot	= null;
}



