/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.cli.jmxcmd.support;

import java.util.Arrays;

import javax.management.Attribute;
import javax.management.AttributeList;

import com.sun.cli.jmxcmd.support.ResultsForGetSet;

import org.glassfish.admin.amx.util.stringifier.Stringifier;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;
import org.glassfish.admin.amx.util.stringifier.IteratorStringifier;
import org.glassfish.admin.amx.util.jmx.stringifier.AttributeStringifier;
import org.glassfish.admin.amx.util.jmx.AttributeComparator;


/**
 */
public class ResultsForGetSetStringifier implements Stringifier
{
	final Options	mOptions;
	
	public static final class Options
	{
		final static Options	DEFAULT	= new Options();
		
		final boolean	mIncludeObjectName;
		final int		mDisplayType;
		
		public final static int	DISPLAY_TERSE	= 0;
		public final static int	DISPLAY_PRETTY	= 1;
		
		
			public
		Options( boolean includeObjectName, int displayType )
		{
			mIncludeObjectName	= includeObjectName;
			mDisplayType		= DISPLAY_PRETTY;
		} 
		
			public
		Options(  )
		{
			this( true, DISPLAY_PRETTY );
		} 
	};
	
		public 
	ResultsForGetSetStringifier(  )
	{
		mOptions	= Options.DEFAULT;
	}
		public 
	ResultsForGetSetStringifier( Options options )
	{
		mOptions	= options;
	}

		public String
	stringify( Object o )
	{
		final ResultsForGetSet	result	= (ResultsForGetSet)o;
		
		String	str	= "";
		
		if ( mOptions.mIncludeObjectName )
		{
			str	= SupportUtil.getObjectNameDisplay( result.getName() );
		}
		final boolean	pretty	= mOptions.mDisplayType == Options.DISPLAY_PRETTY;
		
		final Stringifier attrS	= new AttributeStringifier();
		final AttributeList attributeList	= result.getAttributes();
		final Attribute []	attrList	= (Attribute[])attributeList.toArray( new Attribute[ attributeList.size() ]);
		Arrays.sort( attrList, AttributeComparator.INSTANCE );
		
		if ( pretty )
		{
			str	= str + "\n";
		}

		if ( attrList.length == 0 )
		{
			str	= str + "<no attributes found>";
		}
		else
		{
			String	delim;
			
			if ( pretty )
				delim	= "\n";
			else
				delim	= ",";
				
			str	= str + ArrayStringifier.stringify( attrList , delim, attrS );
		}
		
		if ( result.getProblemNames().size() != 0 )
		{
			str	= str + "\n" + "Failed Attributes: " +
				IteratorStringifier.stringify( result.getProblemNames().iterator(), "," );
		}
		
		return( str );
	}
}
