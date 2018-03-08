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
 * Utils.java
 *
 * Created on September 21, 2004, 2:17 PM
 */

package com.sun.enterprise.admin.mbeanapi.deployment;

import java.io.*;
import javax.swing.*;

/**
 *
 * @author  bnevins
 */
class Utils
{
	private Utils()
	{
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	static boolean ok(String s)
	{
		return s != null && s.length() > 0;
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	static File safeGetCanonicalFile(File f)
	{
		try
		{
			return f.getCanonicalFile();
		}
		catch(IOException ioe)
		{
			return f.getAbsoluteFile();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public static void messageBox(String msg, String title)
	{
		JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE); 
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	static class ArchiveFilter implements FileFilter
	{
		public boolean accept(File f)
		{
			// must end in .jar/.war/.rar/.ear
			
			String name = f.getName();
			
			return name.endsWith(".ear") || name.endsWith(".jar") || name.endsWith(".rar") || name.endsWith(".war");
		}
	}	
	
	//////////////////////////////////////////////////////////////////////////
	
	static class DirDeployFilter implements FileFilter
	{
		public boolean accept(File f)
		{
			String name = f.getName().toLowerCase();
			
			if(name.startsWith("meta-inf"))
				return false;
			
			return f.isDirectory();
		}
	}	
	public static class Sample
	{
		public String toString()
		{
			return "Sample: name = " + name + ", path= " + file;
		}
		public Sample(File f)
		{
			file = f;
			name = f.getName();
			
			if(!f.isDirectory())
				name = name.substring(0, name.length() - 4);
		}
		String	name;
		File	file;
	}
}
