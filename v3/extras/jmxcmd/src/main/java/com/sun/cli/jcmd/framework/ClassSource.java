/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/ClassSource.java,v 1.3 2003/11/18 22:13:20 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2003/11/18 22:13:20 $
 */
 

package com.sun.cli.jcmd.framework;

import java.util.List;

/**
	A ClassSource is a source for Classes.  
 */
public interface ClassSource<T>
{
	/**
		Get an array of Classes.
	 */
	public List<Class<? extends T>>	getClasses();
};



