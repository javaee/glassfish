/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/misc/SafeSave.java,v 1.3 2005/11/08 22:39:23 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/08 22:39:23 $
 */
 
package com.sun.cli.jcmd.util.misc;

import java.io.File;
import java.io.IOException;


/**
	Utilities for performing a safe save involving a temp file
 */
public final class SafeSave
{
	private SafeSave()	{}
	
		private static File
	formTempName( String baseName )
	{
		return( new File ( baseName + System.currentTimeMillis() + ".temp" ) );
	}
	
	/**
		Reads a line, outputting an optional prompt first.  If the prompt is null
		then no prompt is printed.
	 */
		public static File
	getTempFile( File baseFile ) throws IOException
	{
		File	tempFile	= null;
		
		while ( (tempFile = formTempName( baseFile.toString() ) ).exists() )
		{
		}
		
		return( tempFile );
	}
	
	/**
		Replace the original file with the new file in a manner which will not result in data
		loss, with the worst risk being that the original file will be left with a different
		name.
	 */
		public static void
	replaceWithNew( File origFile, File newFile) throws IOException
	{
		final File	origTemp	= getTempFile( origFile );
		
		origFile.renameTo( origTemp );
		newFile.renameTo( origFile );
		origTemp.delete();
	}
}

