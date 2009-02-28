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

package com.sun.enterprise.util.diagnostics;

import java.io.*;
import java.util.*;
import com.sun.enterprise.util.Assertion;
import com.sun.enterprise.util.ObjectAnalyzer;
//Bug 4677074 begin
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
//Bug 4677074 end

/** General Purpose Debugging Output
 ** -- create a ReporterImpl object and then write with
 ** pr()
 ** If you construct with an Object -- that Object's class name will automatically be prepended to each message
 ** If you use pr(String metName, String mesg) -- the metName will be added to the ObjectName
 ** The output of ReporterImpl is controlled by an environmental variable
 ** if you call it with java -DaibDebug=true  -- it gets turned on...
 **/

public class ReporterImpl implements IReporterEnum
{

//Bug 4677074 begin
    static Logger _logger=LogDomains.getLogger(ReporterImpl.class, LogDomains.UTIL_LOGGER);
//Bug 4677074 end
	public ReporterImpl()
	{
		ctor(null, -1);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	ReporterImpl(int theSeverityLevel)
	{
		ctor(null, theSeverityLevel);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public ReporterImpl(String sid)
	{
		ctor(sid, -1);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	ReporterImpl(String sid, int theSeverityLevel)
	{
		ctor(sid, theSeverityLevel);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////                          //////////////////////////////////////////////////
	///////////////////////////     Configurating Stuff  //////////////////////////////////////////////////
	///////////////////////////                          //////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public void setSeverityLevel(int level)
	{
		debug("setSeverityLevel(" + level + ")");//NOI18N

		if(level < 0)
			level = 0;

		severityLevel = level;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public void setSeverityLevel(String level)
	{
		debug("setSeverityLevel(" + level + ")");//NOI18N

		severityLevel = calcSeverityLevel(level);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public int getSeverityLevel()
	{
		return severityLevel;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getSeverityLevelString()
	{
		if(severityLevel <= OFF)
			return severityNames[severityLevel];
		
		return severityNames[OFF];
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public void setName(String theName)
	{
		Assertion.check(theName);
		name = theName;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getName()
	{
		return name;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////                          //////////////////////////////////////////////////
	///////////////////////////     Message Writing      //////////////////////////////////////////////////
	///////////////////////////                          //////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public void verbose(Object o)
	{
		if(checkSeverity(VERBOSE))
			pr(VERBOSE, o);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void info(Object o)
	{
		if(checkSeverity(INFO))
			pr(INFO, o);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void warn(Object o)
	{
		// convenience method
		if(checkSeverity(WARN))
			pr(WARNING, o);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void warning(Object o)
	{
		if(checkSeverity(WARN))
			pr(WARNING, o);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void error(Object o)
	{
		if(checkSeverity(ERROR))
			pr(ERROR, o);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void critical(Object o)
	{
		if(checkSeverity(CRIT))
			pr(CRITICAL, o);
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void crit(Object o)
	{
		if(checkSeverity(CRIT))
			pr(CRITICAL, o);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void dump(Object o, String s)
	{
		if(checkSeverity(DUMP) && o != null)
		{
			if(s == null)
				s = "";
			
			String s2 = s + "\n**********  Object Dump Start  ***********\n" +//NOI18N
					ObjectAnalyzer.toStringWithSuper(o) + "\n**********  Object Dump End  ***********";//NOI18N
			pr(OBJECT_DUMP, s2);
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void dump(String s)
	{
		if(checkSeverity(DUMP))
		{
			pr(OBJECT_DUMP, s);
		}
	}


	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void dump(Object o)
	{
		dump(o, null);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////                          //////////////////////////////////////////////////
	///////////////////////////     ASSERTION STUFF      //////////////////////////////////////////////////
	///////////////////////////                          //////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	// TBD:  Nice to have the assert's print the CallerInfo stuff.
	// lots-o-typing needed!!

	public void insist(String s)
	{
		if(checkSeverity(ASSERT))
		{
			try
			{ 
				Assertion.check(s); 
			}
			catch(Assertion.Failure f) 
			{
				pr(ASSERT, new StackTrace().toString() + f);
				throw f; 
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public void insist(String checkme, String s)
	{
		if(checkSeverity(ASSERT))
		{
			try
			{ 
				Assertion.check(checkme, s); 
			}
			catch(Assertion.Failure f) 
			{
				pr(ASSERT, new StackTrace().toString() + f);
				throw f; 
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public void insist(boolean b)
	{
		if(checkSeverity(ASSERT))
		{
			try
			{ 
				Assertion.check(b); 
			}
			catch(Assertion.Failure f) 
			{
				pr(ASSERT, new StackTrace().toString() + f);
				throw f; 
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public void insist(boolean b, String s)
	{
		if(checkSeverity(ASSERT))
		{
			try
			{ 
				Assertion.check(b, s); 
			}
			catch(Assertion.Failure f) 
			{
				pr(ASSERT, new StackTrace().toString() + f);
				throw f; 
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public void insist(Object o)
	{
		if(checkSeverity(ASSERT))
		{
			try
			{ 
				Assertion.check(o); 
			}
			catch(Assertion.Failure f) 
			{
				pr(ASSERT, new StackTrace().toString() + f);
				throw f; 
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public void insist(Object o, String s)
	{
		if(checkSeverity(ASSERT))
		{
			try
			{ 
				Assertion.check(o, s); 
			}
			catch(Assertion.Failure f) 
			{
				pr(ASSERT, new StackTrace().toString() + f);
				throw f; 
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public void insist(double z)
	{
		if(checkSeverity(ASSERT))
		{
			try
			{ 
				Assertion.check(z); 
			}
			catch(Assertion.Failure f) 
			{
				pr(ASSERT, new StackTrace().toString() + f);
				throw f; 
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public void insist(double z, String s)
	{
		if(checkSeverity(ASSERT))
		{
			try
			{ 
				Assertion.check(z, s); 
			}
			catch(Assertion.Failure f) 
			{
				pr(ASSERT, new StackTrace().toString() + f);
				throw f; 
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public void insist(long l)
	{
		if(checkSeverity(ASSERT))
		{
			try
			{ 
				Assertion.check(l); 
			}
			catch(Assertion.Failure f) 
			{
				pr(ASSERT, new StackTrace().toString() + f);
				throw f; 
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public void insist(long l, String s)
	{
		if(checkSeverity(ASSERT))
		{
			try
			{ 
				Assertion.check(l, s); 
			}
			catch(Assertion.Failure f) 
			{
				pr(ASSERT, new StackTrace().toString() + f);
				throw f; 
			}
		}
	}
        
        /** Change the mechanism this object uses to deliver output to the user
         * @param lwriter A new output mechanism
         * @return The previous output mechanism.
         */        
        public ReporterWriter setWriter(ReporterWriter lwriter) {
            ReporterWriter retVal = null;
            if (null != lwriter) {
                retVal = this.writer;
                this.writer = lwriter;
            }
            return retVal;
        }

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////                          //////////////////////////////////////////////////
	///////////////////////////       PRIVATE STUFF      //////////////////////////////////////////////////
	///////////////////////////                          //////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void ctor(String theName, int theSeverityLevel)
	{
		if(theName != null && theName.length() > 0)
			setName(theName);

		if(theSeverityLevel >= 0)
			setSeverityLevel(theSeverityLevel);
		else
		{
			String sl = System.getProperty("ForteReporterDebugLevel");//NOI18N
			if(sl != null && sl.length() > 0)
			{
				//int sli = Integer.parseInt(sl);
				//setSeverityLevel(sli);
				setSeverityLevel(sl);
			}

			sl = System.getProperty("com.iplanet.util.diagnostic.Reporter.severityLevel");//NOI18N
			if(sl != null && sl.length() > 0)
			{
				//int sli = Integer.parseInt(sl);
				//setSeverityLevel(sli);
				setSeverityLevel(sl);
			}

			sl = System.getProperty("com.sun.enterprise.util.diagnostic.Reporter.severityLevel");//NOI18N
			if(sl != null && sl.length() > 0)
			{
				//int sli = Integer.parseInt(sl);
				//setSeverityLevel(sli);
				setSeverityLevel(sl);
			}

			sl = System.getProperty("Reporter");//NOI18N
			if(sl != null && sl.length() > 0)
			{
				//int sli = Integer.parseInt(sl);
				//setSeverityLevel(sli);
				setSeverityLevel(sl);
			}
		}

		className	= getClass().getName();
		writer		= new ReporterWriter(getName());

		debug("Ctor called");//NOI18N
		debug("ReporterImpl Severity Level:  " + getSeverityLevel());//NOI18N
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	private boolean checkSeverity(int severity)
	{
		if(severity >= severityLevel)
			return true;

		return false;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	private CallerInfo getCallerInfo()
	{
		try
		{
			return new CallerInfo(new Object[] { this });
		}
		catch(CallerInfoException e)
		{
			debug(e.toString());
			return null;
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void pr(int severity, Object o)
	{
		try
		{
			CallerInfo ci = getCallerInfo();
			
			String s;

			if(o == null)
			{
				s = "null Object argument";//NOI18N
			}
			else
			{
				s = o.toString();

				if(s == null) // || s.length() <= 0)
					s = "null toString result from Object";//NOI18N
			}


			if(ci != null)
				s = ci.toString() + ": " + s;//NOI18N

			writer.println(severity, s);
		}
		catch(Throwable e)
		{

//Bug 4677074			System.out.println("Got exception in ReporterImpl.pr():  " + e);//NOI18N
//Bug 4677074 begin
			_logger.log(Level.WARNING,"iplanet_util.pr_exception",e);
//Bug 4677074 end
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private String getClassName()
	{
		Assertion.check(className);
		return className;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	private int calcSeverityLevel(String original)
	{
		if(original == null || original.length() <= 0)
			return DISABLED;	

		String s = original.toUpperCase();

		// first let's see if it is an integer...

		try
		{
			int ret = Integer.parseInt(s);

			if(ret < 0)
				ret = 0;

			return ret;
		}
		catch(NumberFormatException e)
		{
		}

		// not a number -- let's check a few more possibilities...

		if(s.equals("ALL") || s.equals("NOISY") || s.equals("EVERYTHING") || s.equals("ON") //NOI18N
						|| s.equals("MONDO")  || s.equals("YES") || s.equals("TRUE") || s.equals("DUMP") || s.startsWith("MAX"))//NOI18N
			return 0;
		if(s.startsWith("NO") || s.equals("OFF") || s.equals("FALSE") || s.equals("QUIET") || s.startsWith("MIN"))//NOI18N
			return DISABLED;

		// it should be "WARN", "CRITICAL", etc.
		// since all of the values start with a different character,
		// just check the first character...
		char first = s.charAt(0);	// uppercase!!
		
		for(int i = 0; i < severityNames.length; i++)
		{
			if(severityNames[i].toUpperCase().charAt(0) == first)
				return i;
		}

		// I give up!
		debug("Unknown value for commandline argument \"-DIABDebug=" + original + "\"");//NOI18N
		return DISABLED;
	}

	private void debug(String s)
	{
		if(doDebug)
//Bug 4677074			System.out.println("ReporterImpl Report --> " + s);//NOI18N
//Bug 4677074 begin
			_logger.log(Level.FINE,"ReporterImpl Report --> " +s);
//Bug 4677074 end
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	private int						severityLevel		= OFF;
	private String					name				= "Main";//NOI18N
	private ReporterWriter			writer;
	private String					className;
	private static final boolean	doDebug				= false;
}

