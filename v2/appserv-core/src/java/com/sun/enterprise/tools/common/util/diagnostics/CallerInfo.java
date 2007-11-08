package com.sun.enterprise.tools.common.util.diagnostics;

import java.io.*;
import java.util.*;
import com.sun.enterprise.tools.common.util.Assertion;

//import netscape.blizzard.util.*;
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
 * The class <code>CallerInfo</code> is a simple <I>C-struct</I> type of
 * data structure that contains a caller's class name, line number and method name.
 * It is used by Reporter classes in the util package of iAB
 *
 * @author  Byron Nevins
 * @version 1.1, 05/31/00
 * @see     netscape.blizzard.util.Reporter
 * @since   iAB 6.0
 */

public class CallerInfo
{
	public CallerInfo() throws CallerInfoException
	{
		this(null);
	}

	/////////////////////////////////////////////////////////////////

	public CallerInfo(Object[] ignoreUsToo) throws CallerInfoException
	{
		// STUPID PROGRAMMER TRICKS -- create an Exception and make it print a stack trace to an
		// output stream that we can convert to a String -- then look for the first line that does *not* have
		// our classname in it for the needed information...
		// 

		ByteArrayOutputStream	baos		= new ByteArrayOutputStream();
		PrintWriter				pw			= new PrintWriter(baos);
		Throwable				t			= new Throwable();
		String					me			= getClass().getName() + ".";//NOI18N

		ignoreVec.addElement(me);

		if(ignoreUsToo != null)
		{
			for(int i = 0; i < ignoreUsToo.length; i++)
				ignoreVec.addElement(ignoreUsToo[i].getClass().getName() + ".");//NOI18N
		}

		if(globalIgnoreVec.size() > 0)
		{
			for(Enumeration e = globalIgnoreVec.elements(); e.hasMoreElements(); )
				ignoreVec.addElement(e.nextElement().getClass().getName() + ".");//NOI18N
		}
		
		/*
		System.out.println("**** Ignore List Dump...*****");
		for(Enumeration e = ignoreVec.elements(); e.hasMoreElements(); )
		{
			System.out.println(e.nextElement().toString());
		}
		*/

		// create the stack trace
		t.printStackTrace(pw);
		pw.flush();
		
		// parse it...
		StringTokenizer st = new StringTokenizer(baos.toString(), "\n\r\t");//NOI18N

		if(st.countTokens() < 3)
		{
			// minimum:
			// 1. java.lang.Throwable:
			// 2. this method
			// 3. The method from who-knows-where that is calling a Reporter method...
			throw new CallerInfoException("Expected at least 3 lines from the stack dump -- only saw " + st.countTokens() + " lines");//NOI18N
		}
		
		st.nextToken();	// throw away first line ("java.lang.Throwable")//NOI18N
		
		while(st.hasMoreTokens())
		{
			String s = st.nextToken();

			if(!ignoreVec(s))
			{
				parseCallerInfo(s);
				return;
			}
		}
		
		throw new CallerInfoException("Couldn't find a caller method");//NOI18N
	}

	/////////////////////////////////////////////////////////////////

	public static void addToGlobalIgnore(Object o)
	{
		// check for dupes...
		for(Enumeration e = globalIgnoreVec.elements(); e.hasMoreElements(); )
			if(o == e.nextElement())
				return;

		globalIgnoreVec.addElement(o);
	}

	/////////////////////////////////////////////////////////////////

	void parseCallerInfo(String stackDumpLine) throws CallerInfoException
	{
		/* format of line:
		// "at netscape.blizzard.util.ReporterTester.goo(ReporterTester.java:26)"
		// "at netscape.blizzard.util.ReporterTester.goo(ReporterTester.java:)"
		// "at netscape.blizzard.util.ReporterTester.goo(ReporterTester.java)"
		// "at netscape.blizzard.util.ReporterTester.goo(Unknown Source)"*/
 
		if(!stackDumpLine.startsWith("at "))//NOI18N
			throw new CallerInfoException(badFormat + " -- no \"at \" at start of line (" + stackDumpLine +")");//NOI18N

		stackDumpLine = stackDumpLine.substring(3);
		String classInfo = parseAndRemoveLineInfo(stackDumpLine);
		parseClassInfo(classInfo);
	}

	/////////////////////////////////////////////////////////////////

	public String getClassName()
	{
		Assertion.check(className);
		return className;
	}


	/////////////////////////////////////////////////////////////////

	public String getFileName()
	{
		Assertion.check(fileName != null);
		return fileName;
	}

	/////////////////////////////////////////////////////////////////

	public String getMethodName()
	{
		Assertion.check(methodName);
		return methodName;
	}

	/////////////////////////////////////////////////////////////////

	public int getLineNumber()
	{
		return lineNumber;
	}

	/////////////////////////////////////////////////////////////////

	public String toString()
	{
		Assertion.check(className);
		Assertion.check(methodName);
		Assertion.check(fileName != null);

		StringBuffer sb = new StringBuffer();
		sb.append(className);
		sb.append(".");//NOI18N
		sb.append(methodName);

		if(fileName.length() > 0)
			sb.append("(" + fileName + ":" + lineNumber + ")");//NOI18N
		else
			sb.append("(Unknown Source");//NOI18N

		return sb.toString();
	}

	/////////////////////////////////////////////////////////////////

	public String toStringDebug()
	{
		Assertion.check(className);
		Assertion.check(methodName);
		Assertion.check(fileName != null);

		StringBuffer sb = new StringBuffer(getClass().getName());
		sb.append("  dump:");//NOI18N
		sb.append("\nClass Name: ");//NOI18N
		sb.append(className);
		sb.append("\nMethod Name: ");//NOI18N
		sb.append(methodName);
		sb.append("\nFile Name: ");//NOI18N

		if(fileName.length() > 0)
		{
			sb.append(fileName);
			sb.append("\nLine Number: ");//NOI18N
			sb.append(lineNumber);
		}
		else
			sb.append("unknown");//NOI18N

		return sb.toString();
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////                                    ////////////////////////////////////////
	///////////////////////////       PRIVATE STUFF (and main)     ////////////////////////////////////////
	///////////////////////////                                    ////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	private String parseAndRemoveLineInfo(String s) throws CallerInfoException
	{
		// lineInfo --> debug build:  "(ReporterTester.java:26)"
		// release build:  "(Unknown Source)"
		// I think this is a possibility: (foo.java) or (foo.java:) -- but can't reproduce...
		
		fileName = "";//NOI18N
		lineNumber = -1;
		
		int left	= s.indexOf('(');
		int right	= s.indexOf(')');
		
		if(left < 0 || right < 0 || right <= left)
			throw new CallerInfoException(badFormat + " -- no parenthesis in line:" + s);//NOI18N

		String lineInfo = s.substring(left + 1, right);
		s = s.substring(0, left);

		if(lineInfo.length() <= 0)
			return s;	//()

		if(lineInfo.equals("Unknown Source"))//NOI18N
			return s; 

		int colon = lineInfo.indexOf(':');

		if(colon < 0)
		{
			// (foo.java)
			fileName = lineInfo;
			return s;
		}
		if(colon == lineInfo.length() - 1)
		{
			// (foo.java:)
			fileName = lineInfo.substring(0, colon);
			return s;
		}
		// (foo.java:125)

		fileName = lineInfo.substring(0, colon);
		
		try
		{
			lineNumber = Integer.parseInt(lineInfo.substring(colon + 1));
		}
		catch(NumberFormatException e)
		{
			// it's already set to -1
		}
		
		return s;
	}

	/////////////////////////////////////////////////////////////////

	private void parseClassInfo(String s) throws CallerInfoException
	{
		// format:  netscape.blizzard.foo.goo.aMethod

		if(s.indexOf('.') < 0)
			throw new CallerInfoException(badFormat + " -- no \".\" in the fully-qualified method name");//NOI18N

		if(s.indexOf('.') == 0)
			throw new CallerInfoException(badFormat + " fully-qualified method name starts with a dot");//NOI18N

		int index = s.lastIndexOf('.');

		className	= s.substring(0, index);
		methodName	= s.substring(index + 1);
	}

	/////////////////////////////////////////////////////////////////

	private boolean ignoreVec(String s)
	{
		Assertion.check(ignoreVec);
		final int size = ignoreVec.size();
		Assertion.check(size > 0);

		for(int i = 0; i < size; i++)
			if(s.indexOf((String)(ignoreVec.elementAt(i))) >= 0)
				return true;

		return false;
	}

	/////////////////////////////////////////////////////////////////
	
/*
 public static void main(String[] args)
	{
		new CallerInfoTester();
	}
*/
	/////////////////////////////////////////////////////////////////

	private			String	className;
	private			String	fileName;
	private			String	methodName;
	private			int		lineNumber;
	private final	String	badFormat		= "Bad Format in stack dump line";//NOI18N
	private			Vector	ignoreVec		= new Vector();
	private static	Vector	globalIgnoreVec	= new Vector();
}

/*
class CallerInfoTester
{
	CallerInfoTester()
	{
		try
		{
			CallerInfo ci = new CallerInfo(null);
			System.out.println("CallerInfoTester here!!!");
			System.out.println(ci.toString());
		}
		catch(CallerInfoException e)
		{
			System.out.println(e.toString());
		}
	}
}
*/
