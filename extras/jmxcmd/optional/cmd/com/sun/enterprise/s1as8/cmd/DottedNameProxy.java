/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/optional/cmd/com/sun/enterprise/s1as8/cmd/DottedNameProxy.java,v 1.4 2004/02/27 00:01:40 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/02/27 00:01:40 $
 */
 
package com.sun.enterprise.s1as8.cmd;

/**
	Proxy representing the DottedNames
 */
public interface DottedNameProxy
{
	public Object[]	dottedNameGet( String[] names );
	public Object	dottedNameGet( String name );
	public Object[]	dottedNameMonitoringGet( String[] names );
	public Object	dottedNameMonitoringGet( String names );
	public String[]	dottedNameList( String[] names );
	public String[]	dottedNameMonitoringList( String[] names );
	public Object[]	dottedNameSet( String[] names );
}







