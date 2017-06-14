package com.sun.ejte.ccl.reporter;

/** 
	@Class: TestCase
	@Description: Class holding One TestCase info.
	@Author : Ramesh Mandava
	@Last Modified :Initial creation By Ramesh on 10/24/2001
*/


import java.util.Hashtable;

public class TestCase
{
	private String id;
	private String name;
	private String description;

	private String status;
	private String statusDescription;

	private String expected;
	private String actual;


	public TestCase( String id, String name, String description )
	{
		this.id = id;
		this.name= name;
		this.description = description;
		this.status = ReporterConstants.DID_NOT_RUN;
		this.statusDescription=ReporterConstants.NA;
		 this.expected = null;
                this.actual= null;

	}
	public TestCase( String id, String name )
	{
		this.id = id;
		this.name= name;
		this.description =ReporterConstants.NA ;
		this.status = ReporterConstants.DID_NOT_RUN;
		this.statusDescription=ReporterConstants.NA;
		 this.expected = null;
                this.actual= null;

	}

	public TestCase( String id )
	{
		this.id = id;
		this.name= ReporterConstants.NA;
		this.description =ReporterConstants.NA ;
		this.status = ReporterConstants.DID_NOT_RUN;
		this.statusDescription=ReporterConstants.NA;
		 this.expected = null;
                this.actual= null;

	}

	public void setStatus ( String status ) 
	{
		this.status = status;
	}
	public void setStatusDescription ( String statusDescription ) 
	{
		this.statusDescription = statusDescription;
		 this.expected = null;
                this.actual= null;

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



}
