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
 * NativeResults.java
 *
 * Created on April 14, 2003, 11:46 AM
 */

package com.sun.enterprise.util.io;
import java.util.*;
import java.util.logging.*;
import com.sun.enterprise.util.OS;

/**
 * Convert an integer error number returned from native code to a localized String
 * As of April, 2003 -- UNIX is the only supported OS.
 * @author  bnevins
 */

class NativeResults
{
	NativeResults(int id)
	{
		this.id = id;
		initStrings();
		setResultString();
		setResultException();
	}
	
	/**
	 * @return The localized String explanation of the error, or success.
	 */	
	String getResultString()
	{
		return resultString;
	}
	
	/**
	 * @return the NativeIOException -- suitable for throwing
	 */
	NativeIOException getResultException()
	{
		return exception;	// may be null!
	}
	
	///////////////////////////////////////////////////////////////////////////

	private void setResultString()
	{
		if(rb == null)
		{
			resultString = genericError + id;
			return;
		}
		
		try
		{
			resultString = rb.getString(errorKey + id);
			return;
		}
		catch(Throwable t)
		{
			// go into the next try block.  No need to nest it.
		}

		try
		{
			// note: the id is outside the parenthesis -- not a mistake...
			resultString =  rb.getString(unknownErrorKey) + id; 
			return;
		}
		catch(Throwable t)
		{
			resultString = genericError + id;
		}
	}
	
	///////////////////////////////////////////////////////////////////////////

	void setResultException()
	{
		if(id != 0)	// by definition -- an error!
			exception = new NativeIOException(resultString, id);
	}
	
	///////////////////////////////////////////////////////////////////////////

	private void initStrings()
	{
		try
		{
			rb = ResourceBundle.getBundle(RESOURCE_BUNDLE);
		}
		catch(Throwable t)
		{
			// any little error --> RuntimeException!!
			rb = null;
			logger.severe("Unable to get a ResourceBundle: " + RESOURCE_BUNDLE);
		}
		
		if(OS.isWindows())
			osString = "Windows";
		else
			osString = "UNIX";
		
		errorKey			= "enterprise_util." + osString + ".error.";
		unknownErrorKey		= errorKey + "unknown";
		genericError		= "UNKNOWN " + osString + " Error returned.  Errno =";
	}
	
	///////////////////////////////////////////////////////////////////////////

	private					Logger				logger				= IOLogger.getLogger();
	private					ResourceBundle		rb					= null;
	private static final	String				RESOURCE_BUNDLE		= "com.sun.logging.enterprise.system.util.LogStrings";
	private					String				errorKey;			// depends on OS
	private					String				unknownErrorKey;	// depends on OS
	private					String				genericError;		// depends on OS
	private					String				osString;			// depends on OS
	private					int					id;					// returned by native code
	private					String				resultString;
	private					NativeIOException	exception			= null;
	
	//////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args)
	{
		int errs[] = { 0, 1, 2, 4, 5, 13, 14, 20, 22, 30, 67, 78, 9999, 111, -2};

		for(int i = 0; i < errs.length; i++)
		{
			int id = errs[i];
			
			NativeResults nr = new NativeResults(id);
			String s = "ID: " + id + ",  ";
			
			if(nr.getResultException() == null)
				s += "NO ERROR, ";
			else
				s += "YES ERROR, ";
			
			s += nr.getResultString();
			
			System.out.println(s);
		}
	}
}

