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
