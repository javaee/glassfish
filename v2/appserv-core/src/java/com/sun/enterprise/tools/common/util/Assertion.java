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

package com.sun.enterprise.tools.common.util;

import com.sun.enterprise.tools.common.util.diagnostics.CallerInfo;
import com.sun.enterprise.tools.common.util.diagnostics.CallerInfoException;

/**
   A class for assertion checking

   @version 1.00 1 May 1999
   @version 1.10 5 June 2000
   @author Byron Nevins
*/

public class Assertion 
{  
   /**
      Check an assertion
      @param b the condition to check
      @param s a string describing the check
      @throws Assertion.Failure if condition not true
   */

   public static void check(boolean b, String s)
   {  if (doCheck && !b)
         toss(s);
   }

   /**
      Check an assertion
      @param b the condition to check
      @throws Assertion.Failure if condition not true
   */

   public static void check(boolean b)
   {  if (doCheck && !b)
         toss();
   }

   /**
      Check an assertion
      @param obj an object to check
      @param s a string describing the check
      @throws Assertion.Failure if object is null
   */

   public static void check(Object obj, String s)
   {  if (doCheck && obj == null)
         toss(s);
   }


   /**
      Check an assertion
      @param checkMe a String to check for length > 0
      @param s a string describing the check
      @throws Assertion.Failure if checkMe is null or zero-length
   */

   public static void check(String checkMe, String s)
   {  if (doCheck && (checkMe == null || checkMe.length() <= 0))
         toss(s);
   }


   /**
      Check an assertion
      @param checkMe a String to check for length > 0
      @throws Assertion.Failure if checkMe is null or zero-length
   */

   public static void check(String checkMe)
   {  if (doCheck && (checkMe == null || checkMe.length() <= 0))
         toss();
   }

   /**
      Check an assertion
      @param obj an object to check
      @throws Assertion.Failure if object is null
   */

   public static void check(Object obj)
   {  if (doCheck && obj == null)
         toss();
   }
  
   /**
      Check an assertion
      @param x a number
      @param s a string describing the check
      @throws Assertion.Failure if number is 0
   */

   public static void check(double x, String s)
   {  if (doCheck && x == 0)
         toss(s);
   }

   /**
      Check an assertion
      @param x a number
      @throws Assertion.Failure if number is 0
   */

   public static void check(double x)
   {  if (doCheck && x == 0)
         toss();
   }

   /**
      Check an assertion
      @param x a number
      @param s a string describing the check
      @throws Assertion.Failure if number is 0
   */

   public static void check(long x, String s)
   {  if (doCheck && x == 0)
         toss(s);
   }

   /**
      Check an assertion
      @param x a number
      @throws Assertion.Failure if number is 0
   */

   public static void check(long x)
   {  if (doCheck && x == 0)
         toss();
   }

   /**
      Turn checking on or off
      @param c true to turn checking on, false to turn checking off
   */

   public static void setCheck(boolean c)
   {  doCheck = c;
   }
   
   private static boolean doCheck = true;

   /**
      test stub
   */

   public static void main(String[] args)
   {  Assertion.check(args);
      Assertion.check(args.length, "No command line arguments");//NOI18N
   }

	/////////////////////////////////////////////////////////////////////////

	private static void toss()
	{
		toss(null);
	}

	/////////////////////////////////////////////////////////////////////////

	private static void toss(String gripe)
	{
		String msg = "\nAssertion failed";//NOI18N
		String ci = getCallerInfo();
		
		if(ci != null)
		{
			msg += " at " + ci;//NOI18N
		}

		if(gripe != null)
			msg += " --> " + gripe;//NOI18N
		
		throw new Failure(msg);
	}

	/////////////////////////////////////////////////////////////////////////

	private static String getCallerInfo()
	{
		try
		{
			CallerInfo ci = new CallerInfo( new Object[] { staticInstance });
			return ci.toString();
		}
		catch(CallerInfoException e)
		{
			return null;
		}
	}

	/////////////////////////////////////////////////////////////////////////

	private static Assertion	staticInstance = null;

	static
	{
		staticInstance = new Assertion();
	}

	/////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////

		
   public static class Failure extends RuntimeException
   {
		/**
		@param gripe a description of the reason for the failure
		*/
		public Failure(String gripe) 
		{ 
			super(gripe);
		}
	}
}

