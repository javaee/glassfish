package com.sun.ejte.ccl.reporter;

/** 
	@Class: TestSuite
	@Description: Class holding One TestSuite info.
	@Author : Ramesh Mandava
	@Last Modified : By Ramesh on 10/24/2001
	@Last Modified : By Ramesh on 1/20/2002 , For preserving order of entry of tests 		used a separate testIdVector
*/


import java.util.Hashtable;
import java.util.Vector;

public class TestSuite 
{
	private String id;
	private String name;
	private String description;

	Hashtable testHash;
	Vector testIdVector;

	public TestSuite( String id, String name, String description )
	{
		this.id = id;
		this.name= name;
		this.description = description;
		testHash = new Hashtable();
		testIdVector = new Vector();
	}

	public TestSuite (String id, String name )
	{
		this.id = id;
		this.name = name;
		this.description=ReporterConstants.NA;
		testHash = new Hashtable();
		testIdVector = new Vector();
	}

	public TestSuite (String id )
	{
		this.id = id;
		this.name = ReporterConstants.NA;
		this.description=ReporterConstants.NA;
		testHash = new Hashtable();
		testIdVector = new Vector();
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

	public Vector getTestIdVector( )
	{
		return testIdVector;
	}
	public void setTestIdVector( Vector tidVector)
	{
		testIdVector= tidVector;
	}

	public Hashtable getTestHash( )
	{
		return testHash;
	}
	public void setTestHash( Hashtable testHash )
	{
		this.testHash= testHash;
	}

	public void addTest( Test myTest )
	{
		if ( testHash.put( myTest.getId().trim(), myTest) != null ) 
		{
			System.err.println("Error : Test was added before only. Still allowing. Old value of the test will be overridden" );
		}
			
		testIdVector.addElement( myTest.getId().trim() );
	}	

}
