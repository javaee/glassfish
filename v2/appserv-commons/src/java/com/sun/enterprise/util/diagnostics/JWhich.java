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

/**
 * @version 1.00 April 1, 2000
 * @author Byron Nevins
 */

package com.sun.enterprise.util.diagnostics;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
//Bug 4677074 begin
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
//Bug 4677074 end

public class JWhich
{
//Bug 4677074 begin
	static Logger _logger=LogDomains.getLogger(LogDomains.UTIL_LOGGER);
//Bug 4677074 end
	public static void main(String[] args)
	{
		if(args == null || args.length == 0)
		{
			usage();
			return;
		}

		int argNum = 0;
		
		if(args[0].toLowerCase().equals("-classpath"))//NOI18N
		{
			if(args.length != 3)
			{
				usage();
				return;
			}

			new JWhich(args[2], args[1]);
		}
		else
        {
			new JWhich(args[0]);
        }
	}

	///////////////////////////////////////////////////////////
	
	public JWhich(String classname, String classpathArg)
	{
		this.classpathArg = classpathArg;
		ctor(classname);
	}

	///////////////////////////////////////////////////////////
	
	public JWhich(String classname)
	{
		ctor(classname);
	}

	///////////////////////////////////////////////////////////
	
	public String getResult()
	{
		return result;
	}

	///////////////////////////////////////////////////////////
	
	private void ctor(String classname)
	{
		this.classname = classname;

		//if(doExhaustive)
			//doReflect = false;

		initClasspath();
		fixClassname();
		String[] locations = findClass();
		
		pr("");//NOI18N

		if(locations == null || locations.length <= 0)
		{
			pr("Can't find class");//NOI18N
			return;
		}

		for(int i = 0; i < locations.length; i++)
			pr(classname + " located in " + locations[i]);//NOI18N
		
		//if(doReflect)
			//new Reflect(classname);
	}

	///////////////////////////////////////////////////////////
	
	private static void usage()
	{
		System.out.println("Usage:  java  " + JWhich.class.getName() + " [-classpath a_classpath] classname");//NOI18N
	}

	///////////////////////////////////////////////////////////
	
	private void initClasspath()
	{
		String cp;

		if(classpathArg == null)
			cp = System.getProperty("java.class.path");//NOI18N
		else
			cp = classpathArg;

		StringTokenizer		tokens	= new StringTokenizer(cp, ";", false);//NOI18N
		int					nTokens = tokens.countTokens();

		classpath = new String[nTokens];

		debug("" + nTokens + " tokens.");//NOI18N
		
		for(int i = 0; tokens.hasMoreTokens(); i++) 
		{
			String s = tokens.nextToken();
			debug(s);
			classpath[i] = s;
		}
     }

	///////////////////////////////////////////////////////////
	
	private void fixClassname()
	{
		// change as follows:
		// com.netscape.blizzard.foo -->  com\netscape\blizzard\foo
		// com/netscape/blizzard/foo -->  com\netscape\blizzard\foo
		// com/netscape\blizzard.foo -->  com\netscape\blizzard\foo

		debug("old classname: " + classname);//NOI18N
		jarClassname = classname;

		classname = classname.replace('.', File.separatorChar);

		if(File.separatorChar != '/')
			classname = classname.replace('/', File.separatorChar);
		
		if(File.separatorChar != '\\')
			classname = classname.replace('\\', File.separatorChar);
		
		// classnames in jars ALWAYS look like: com/foo/goo.class

		jarClassname	= jarClassname.replace('.', '/');
		jarClassname	= jarClassname.replace('\\', '/');
		
		classname		= classname		+ ".class";//NOI18N
		jarClassname	= jarClassname	+ ".class";//NOI18N

		debug("new classname: " + classname);//NOI18N
		debug("new jarClassname: " + jarClassname);//NOI18N
	}

	///////////////////////////////////////////////////////////
	
	private String[] findClass()
	{
		ArrayList names = new ArrayList();

		for(int i = 0; i < classpath.length; i++)
		{
			String path = classpath[i];

			if(findClass(path))
			{
				names.add(path);
				debug("FOUND IT:  " + path);//NOI18N
			}
		}

		int num = names.size();

		debug("Found it in " + num + " places");//NOI18N

		if(num <= 0)
		{
			return null;
		}

		String[] ss = new String[num];
		ss = (String[])names.toArray(ss);
		return ss;
	}

	///////////////////////////////////////////////////////////
	
	private boolean findClass(String path)
	{
		if(path.toLowerCase().endsWith(".jar"))//NOI18N
		{
			return findClassInJar(path);
		}

		File f = new File(path + File.separator + classname);
		debug("Looking for " + f);//NOI18N

		return f.exists();
	}

	///////////////////////////////////////////////////////////
	
	private boolean findClassInJar(String path)
	{
		ZipInputStream zin = null;

		try
		{
			zin = new ZipInputStream(new FileInputStream(path));
			ZipEntry entry;

			while((entry = zin.getNextEntry()) != null)
			{  
				String name = entry.getName();
				zin.closeEntry();

				if(name.equals(jarClassname))
				{
					zin.close();
					return true;
				}
			}
			zin.close();
		}
		catch(IOException e)
		{
			debug("" + e + "  " + path);//NOI18N
		}
	
		return false;
	}

	///////////////////////////////////////////////////////////

	private void debug(String s)
	{
		if(debug_)
			pr(s);
	}

	///////////////////////////////////////////////////////////
	
	private void pr(String s)
	{
//Bug 4677074		System.out.println(s);
//Bug 4677074 begin
		_logger.log(Level.FINE,s);
//Bug 4677074 end
		result += s;
	}

	///////////////////////////////////////////////////////////
	
	private String[]	classpath		= null;
	private String		classpathArg	= null;
	private String		classname		= null;
	private String		jarClassname	= null;
	private boolean		doReflect		= false;
	private boolean		doExhaustive	= true;
	private boolean		debug_			= false;
	private String		result			= new String();
}
