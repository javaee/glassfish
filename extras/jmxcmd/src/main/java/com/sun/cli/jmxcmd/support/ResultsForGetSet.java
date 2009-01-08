/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/ResultsForGetSet.java,v 1.2 2003/12/12 03:22:11 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/12/12 03:22:11 $
 */
 

package com.sun.cli.jmxcmd.support;

import java.util.Collections;
import java.util.Set;

import java.io.Serializable;
import javax.management.ObjectName;
import javax.management.AttributeList;

/**
 */
public final class ResultsForGetSet implements Serializable
{
	private final ObjectName	mObjectName;
	private final AttributeList	mAttributes;
	private final Set			mProblemNames;

		public ObjectName
	getName()
	{
		return( mObjectName );
	}
	
		public AttributeList
	getAttributes()
	{
		return( mAttributes );
	}
	
		public Set
	getProblemNames()
	{
		return( mProblemNames );
	}
	
		public
	ResultsForGetSet( ObjectName objectName, AttributeList attrList )
	{
		mObjectName	= objectName;
		mAttributes	= attrList;
		mProblemNames	= Collections.EMPTY_SET;
	}
	
	
		public
	ResultsForGetSet( ObjectName objectName, AttributeList attrList, Set problemNames)
	{
		mObjectName		= objectName;
		mAttributes		= attrList;
		mProblemNames	= problemNames;
	}
}
	