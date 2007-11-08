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
 * Classpath.java
 *
 * Created on September 28, 2001, 11:34 PM
 */

package com.sun.enterprise.util.diagnostics;
import com.sun.enterprise.util.diagnostics.Reporter;
import java.util.*;

/**
 *
 * @author  bnevins
 * @version 
 */

public class Classpath
{
	public static List getClasspathAsList()
	{
		String			cp		= getClasspathAsString();
		String			ps		= System.getProperty("path.separator");
		StringTokenizer st		= new StringTokenizer(cp, ps);
		List			list	= new ArrayList();
		
		while (st.hasMoreTokens()) 
		{
			list.add(st.nextToken());
		}
		
		return list;
	}
	
	public static List getClasspathAsBatchCommands()
	{
		List from = getClasspathAsList();
		List to	= new ArrayList();
		
		boolean first = true;
		
		for(Iterator iter = from.iterator(); iter.hasNext(); )
		{
			if(first)
			{
				to.add("set CLASSPATH=" + iter.next());
				first = false;
			}
			else
				to.add("set CLASSPATH=%CLASSPATH%;" + iter.next());
		}
		
		return to;
	}

	public static String getClasspathAsString()
	{
		return System.getProperty("java.class.path");
	}

	public static List getClasspathAsSortedList()
	{
		List list = getClasspathAsList();
		Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
		return list;
	}
		
	
	public static void main(String[] args)
	{
		pr("******  CLASSPATH as String *******");
		pr(getClasspathAsString());
		pr("******  CLASSPATH as List *******");
		pr(getClasspathAsList());
		pr("******  CLASSPATH as Sorted List *******");
		pr(getClasspathAsSortedList());
		pr("******  CLASSPATH as Batch Commands *******");
		pr(getClasspathAsBatchCommands());
	}
	
	private static void pr(String s)
	{
		System.out.println(s);
	}
	
	private static void pr(List c)
	{
		for(Iterator iter = c.iterator(); iter.hasNext(); )
		{
			pr((String)iter.next());
		}
	}
	
}
