/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/ParsedObject.java,v 1.1 2003/11/21 21:23:50 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/21 21:23:50 $
 */
 

package com.sun.cli.jmxcmd.support;

import org.glassfish.admin.amx.util.ClassUtil;


/**
 */
class ParsedObject
{
	Object	mObject;
	Class	mClass;
	boolean	mExactMatch;
	
	ParsedObject( Object o, Class c, boolean exactMatch)
	{
		mObject			= o;
		mClass			= c;
		mExactMatch		= exactMatch;
	}
	
		public String
	toString()
	{
		return( ClassUtil.getFriendlyClassname( mClass.getName() ) );
	}
}
