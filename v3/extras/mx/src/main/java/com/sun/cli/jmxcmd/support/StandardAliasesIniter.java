/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/StandardAliasesIniter.java,v 1.1 2003/11/21 21:23:50 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/21 21:23:50 $
 */
 

package com.sun.cli.jmxcmd.support;

import javax.management.MBeanServerConnection;

import com.sun.cli.jmxcmd.support.CLISupportMBeanProxy;

/**
 */
public final class StandardAliasesIniter
{
		private
	StandardAliasesIniter()
	{
		// don't allow instantiation
	}
	
		private static void
	initAlias( AliasMgr aliasMgr, String name, String value )
		throws Exception
	{
		aliasMgr.deleteAlias( name );
		aliasMgr.createAlias( name, value );
	}
	
		public static void
	init( AliasMgr	aliasMgr )
	{
		try
		{
			initAlias( aliasMgr, StandardAliases.ALL_ALIAS, CLISupportStrings.ALL_TARGET );
			initAlias( aliasMgr, StandardAliases.CLI_ALIAS, CLISupportStrings.CLI_SUPPORT_TARGET );
			initAlias( aliasMgr, StandardAliases.ALIAS_MGR_ALIAS, CLISupportStrings.ALIAS_MGR_TARGET );
		}
		catch( Exception e )
		{
			// ignore
			e.printStackTrace();
		}
	}
};




		