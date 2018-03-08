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

package com.sun.enterprise.admin.mbeanapi.deployment;

/**
   An easy interface to read numbers and strings from 
   standard input

   @version 1.10 10 Mar 1997
   @author Cay Horstmann
*/

public class Console
{  
	/** print a prompt on the console but don't print a newline
      @param prompt the prompt string to display
    */

	public static void printPrompt(String prompt)
	{  
		System.out.print(prompt + " ");
		System.out.flush();
	}

	/** read a string from the console. The string is 
		terminated by a newline
		@return the input string (without the newline)
	*/

	public static String readLine()
	{  
		int ch;
		String r = "";
		boolean done = false;
		
		while (!done)
		{  
			try
			{  
				ch = System.in.read();
				if (ch < 0 || (char)ch == '\n')
					done = true;
				
				else if ((char)ch != '\r') // weird--it used to do \r\n translation
					r = r + (char) ch;
			}
			catch(java.io.IOException e)
			{  
				done = true;
			}
		}
		
		return r;
	}

	/**	read a string from the console. The string is 
		terminated by a newline
		@param prompt the prompt string to display
		@return the input string (without the newline)
	*/

	public static char getKey(String prompt)
	{  
		printPrompt(prompt);
		int ch = '\n';
		
		try
		{  
			ch = System.in.read();
		}
		catch(java.io.IOException e)
		{  
		}
		return (char)ch;
		
	}	
	
	/**	read a string from the console. The string is 
		terminated by a newline
		@param prompt the prompt string to display
		@return the input string (without the newline)
	*/

	public static String readLine(String prompt)
	{  
		printPrompt(prompt);
		return readLine();
	}

	/**	read an integer from the console. The input is 
	terminated by a newline
	@param prompt the prompt string to display
	@return the input value as an int
	@exception NumberFormatException if bad input
	*/

	public static int readInt(String prompt)
	{  
		while(true)
		{  
			printPrompt(prompt);
			
			try
			{  
				return Integer.valueOf
				(readLine().trim()).intValue();
			} 
			catch(NumberFormatException e)
			{  
				System.out.println("Not an integer. Please try again!");
			}
		}
	}

	/** read a floating point number from the console. 
	The input is terminated by a newline
	@param prompt the prompt string to display
	@return the input value as a double
	@exception NumberFormatException if bad input
	*/

	public static double readDouble(String prompt)
	{  
		while(true)
		{  
			printPrompt(prompt);
			
			try
			{  
				return Double.parseDouble(readLine().trim());
			} 
			catch(NumberFormatException e)
			{  
				System.out.println("Not a floating point number. Please try again!");
			}
		}
	}
}
