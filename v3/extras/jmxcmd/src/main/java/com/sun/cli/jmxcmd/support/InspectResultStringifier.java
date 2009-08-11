/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/InspectResultStringifier.java,v 1.4 2004/04/26 07:29:39 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/04/26 07:29:39 $
 */
 


package com.sun.cli.jmxcmd.support;

import java.util.Arrays;

import java.lang.reflect.Array;
import javax.management.*;

import org.glassfish.admin.amx.util.stringifier.*;
import org.glassfish.admin.amx.util.jmx.stringifier.MBeanFeatureInfoStringifierOptions;
import org.glassfish.admin.amx.util.jmx.stringifier.MBeanAttributeInfoStringifier;
import org.glassfish.admin.amx.util.jmx.stringifier.MBeanOperationInfoStringifier;
import org.glassfish.admin.amx.util.jmx.stringifier.MBeanConstructorInfoStringifier;
import org.glassfish.admin.amx.util.jmx.stringifier.MBeanNotificationInfoStringifier;
import org.glassfish.admin.amx.util.jmx.MBeanAttributeInfoComparator;
import org.glassfish.admin.amx.util.jmx.MBeanOperationInfoComparator;

/**
 */
public final class InspectResultStringifier implements Stringifier
{
	public static final InspectResultStringifier	DEFAULT	= new InspectResultStringifier();
	
		public
	InspectResultStringifier( )
	{
	}
	
	

		private String
	stringifyArray( Object [] a, Stringifier stringifier)
	{
		String	temp	= "";
		
		if ( Array.getLength( a ) != 0 )
		{
			temp	= "\n" + ArrayStringifier.stringify( a, "\n", stringifier);
		}
		return( temp );
	}

		public String
	stringify( Object o)
	{
		String			result	= "";
		final InspectResult	r	= (InspectResult)o;
	
		final MBeanFeatureInfoStringifierOptions options	= new MBeanFeatureInfoStringifierOptions( r.includeDescription, ",");
		
		result	= result + SupportUtil.getObjectNameDisplay( r.objectInstance.getObjectName() );

		if ( r.summary != null )
		{
			if ( result.length() != 0 )
			{
				result	= result + "\n";
			}

			result	= result + r.summary;
		}
		
		// Do formal terms like "Attributes" need to be I18n?  Probabably not as they are part of a specification.
		if ( r.attrInfo != null )
		{
			Arrays.sort( r.attrInfo, MBeanAttributeInfoComparator.INSTANCE );
			result	= result + "\n\n- Attributes -" +
						stringifyArray( r.attrInfo, new MBeanAttributeInfoStringifier(options) );
		}
		
		if ( r.operationsInfo != null )
		{
			Arrays.sort( r.operationsInfo, MBeanOperationInfoComparator.INSTANCE );
			result	= result + "\n\n- Operations -" +
						stringifyArray( r.operationsInfo, new MBeanOperationInfoStringifier(options) );
		}
		
		if ( r.constructorsInfo != null )
		{
			result	= result + "\n\n- Constructors -" +
						stringifyArray( r.constructorsInfo, new MBeanConstructorInfoStringifier(options) );
		}
		
		if ( r.notificationsInfo != null )
		{
			result	= result + "\n\n- Notifications -" + 
						stringifyArray( r.notificationsInfo, new MBeanNotificationInfoStringifier(options) );
		}
		
		return( result );
			
	}
}



