/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.cli.jcmd.framework;
 
public class FileNotFoundException extends CmdException
{
    static final long serialVersionUID = -1925309294075470238L;
    
		public
	FileNotFoundException( String subCmdName, String filename )
	{
		super( subCmdName, "File Not Found: " + filename, FILE_NOT_FOUND_ERROR );
	}
	
	public static final int	FILE_NOT_FOUND_ERROR			= -21;
	
		public String
	getErrorCodeName()
	{
		return( "FILE_NOT_FOUND" );
	}
}
