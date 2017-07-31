/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejte.ccl.reporter;

/**
	@Class: Test
	@Description: Class holding One Test info.
	@Author : Ramesh Mandava
	@Last Modified :Initial creation By Ramesh on 10/24/2001
	@Last Modified : By Ramesh on 1/20/2002 , For preserving order of entry of 
		testcases used a separate testCaseIdVector

*/


import java.util.Hashtable;
import java.util.Vector;

public class Test
{
	private String id;
	private String name;
	private String description;

	private String status;
	private String statusDescription;

	private String expected;
	private String actual;

	Hashtable testCaseHash;
	Vector testCaseIdVector; // Added to preserve order of entry


	public Test( String id, String name, String description )
	{
		this.id = id;
		this.name= name;
		this.description = description;
		this.status = ReporterConstants.OPTIONAL;
		this.statusDescription=ReporterConstants.OPTIONAL;
		this.expected = null;
		this.actual= null;
		testCaseHash = new Hashtable();
		testCaseIdVector= new Vector();
	}
	public Test( String id, String name )
	{
		this.id = id;
		this.name= name;
		this.description =ReporterConstants.NA ;
		this.status = ReporterConstants.OPTIONAL;
		this.statusDescription=ReporterConstants.OPTIONAL;
		this.expected = null;
		this.actual= null;
		testCaseHash = new Hashtable();
		testCaseIdVector= new Vector();
	}

	public Test( String id )
	{
		this.id = id;
		this.name= ReporterConstants.NA;
		this.description =ReporterConstants.NA ;
		this.status = ReporterConstants.OPTIONAL;
		this.statusDescription=ReporterConstants.OPTIONAL;
		this.expected = null;
		this.actual= null;
		testCaseHash = new Hashtable();
		testCaseIdVector= new Vector();
	}

	public void setStatus ( String status ) 
	{
		this.status = status;
	}
	public void setStatusDescription ( String statusDescription ) 
	{
		this.statusDescription = statusDescription;
		this.expected=null;
		this.actual =null;
	}

	public void setExpected( String expected )
	{
		this.expected = expected;
	}

	public void setActual( String actual )
	{
		this.actual = actual;
	}

	public String getId( )
	{
		return id;
	}

	public String getName( )
	{
		return name;
	}

	public String getDescription( )
	{
		return description;
	}

	public String getStatus( )
	{
		return status;
	}
	public String getStatusDescription( )
	{
		return statusDescription;
	}

	public String getExpected( )
	{
		return expected;
	}

	public String getActual( )
	{
		return actual;
	}

	public Vector getTestCaseIdVector( )
	{
		return testCaseIdVector;
	}
	public void setTestCaseIdVector( Vector tidVector)
	{
		testCaseIdVector = tidVector;
	}
	public Hashtable getTestCaseHash( )
	{
		return testCaseHash;
	}
	public void setTestCaseHash( Hashtable testCaseHash )
	{
		this.testCaseHash= testCaseHash;
	}

	public void addTestCase( TestCase testCase )
	{
		if ( testCaseHash.put( testCase.getId().trim(), testCase ) != null )
		{
			System.err.println(" Error? Test Case : " + testCase.getId().trim() + " already added. Overwriting the previous test case" );
		}
		testCaseIdVector.addElement( testCase.getId().trim() );
	}
}
