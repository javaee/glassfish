/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
 * SimplePersistence.java
 *
 * Created on May 19, 2003, 10:37 AM
 */

package com.sun.enterprise.admin.mbeanapi.deployment;

import java.io.*;
import java.util.*;
import java.net.URL;

/**
 *
 * @author  bnevins
 */

public class SimplePersistence
{
	public SimplePersistence(Object o)
	{
		this(o, null, true);
	}

	//////////////////////////////////////////////////////////////////////////
	
	public SimplePersistence(Object o, boolean autoFlush)
	{
		this(o, null, autoFlush);
	}

	//////////////////////////////////////////////////////////////////////////
	
	public SimplePersistence(Object o, String fname)
	{
		this(o, fname, true);
	}

	//////////////////////////////////////////////////////////////////////////
	
	public SimplePersistence(Object o, String fname, boolean autoFlush)
	{
		persistenceFileRootName = fname;
		this.autoFlush = autoFlush;
		init(o);
		load();
	}

	//////////////////////////////////////////////////////////////////////////
	
	public SimplePersistence(File f)
	{
		this(f, true);
	}

	//////////////////////////////////////////////////////////////////////////
	
	public SimplePersistence(File f, boolean autoFlush)
	{
		persistenceFile =f;
		this.autoFlush = autoFlush;
		
		if(!persistenceFile.exists())
			store();

		load();
	}

	//////////////////////////////////////////////////////////////////////////
	
	public String getProperty(String key)
	{
		return props.getProperty(key);
	}

	//////////////////////////////////////////////////////////////////////////
	
	public Properties getProperties()
	{ 
		return props;
	}

	//////////////////////////////////////////////////////////////////////////
	
	public void setProperty(String key, String value)
	{ 
		props.setProperty(key, value);
		
		if(autoFlush)
			store();
	}
	
	//////////////////////////////////////////////////////////////////////////

	public void store()
	{
		try
		{
			OutputStream os = new FileOutputStream(persistenceFile);
			props.store(os, "Simple Persistence Properties");
			os.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	//////////////////////////////////////////////////////////////////////////

	public final void clear()
	{
		props.clear();
	}
		
	//////////////////////////////////////////////////////////////////////////
	
	private void init(Object o)
	{
		Class	clazz	= o.getClass();
		Package pkg		= clazz.getPackage();
		
		// we need the name of a file in the caller's package.  ANY file will do!
		// so we use the caller's .class file
		
		final String classFileName = clazz.getName().substring(pkg.getName().length() + 1) + ".class";
		
		URL url = clazz.getResource(classFileName);
		String filename = url.getPath();
		
		if(canWrite(filename))
		{
			// make the properties filename simple
			if(persistenceFileRootName == null)
				persistenceFileRootName = "persist.properties";
			
			filename = filename.substring(0, filename.indexOf(classFileName));
			filename += persistenceFileRootName;
			persistenceFile = new File(filename);
		}
		else
		{
			// make the properties filename equal to the package name
			// to avoid conflicts with other callers.
			if(persistenceFileRootName == null)
				persistenceFileRootName = pkg.getName() + ".properties";
			
			// most likely -- we are running from a jar!
			persistenceFile = new File(System.getProperty("java.io.tmpdir"));
			persistenceFile = new File(persistenceFile, persistenceFileRootName);
		}

		if(!persistenceFile.exists())
		{
			// write an empty file...
			store();
		}
		
		System.out.println("Persistence File: " + persistenceFile.getAbsolutePath());
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	private void load()
	{
		try
		{  
			InputStream in = new FileInputStream(persistenceFile);
			props.load(in);
			in.close();
			System.out.println(props);
		}
		catch(IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	private boolean canWrite(String fname)
	{
		File f = new File(fname);
		
		if(f.exists())
		{
			return f.canWrite();
		}
		
		try
		{
			return f.createNewFile();
		}
		catch(IOException e)
		{
			return false;
		}
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	private					Properties	props					= new Properties();
	private					File		persistenceFile;
	private					Class		caller;
	private					String		persistenceFileRootName;
	private					boolean		autoFlush;
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		class foo {}
		SimplePersistence sp = new SimplePersistence(new foo(), true);
		sp.setProperty("foo", "goo");
		//m.getProps();
	}
	
}

