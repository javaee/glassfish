/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/JDBCResource.java,v 1.4 2004/10/14 19:07:05 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/10/14 19:07:05 $
 */
 
package com.sun.cli.jmxcmd.jsr77;
 
import java.util.Set;


/**
 */
public interface JDBCResource extends J2EEResource
{
	public final static String	J2EE_TYPE	= J2EETypes.JDBC_RESOURCE;
	
	public String[]	getjdbcDataSources();


	public Set	getJDBCDataSourceSet();
}
