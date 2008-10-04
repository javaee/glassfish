/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/optional/mbeans/com/sun/enterprise/jmx/kstat/kstatRepository.java,v 1.2 2003/10/03 22:43:33 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/10/03 22:43:33 $
 */
package com.sun.enterprise.jmx.kstat;

import java.util.Set;

/**
 */
public interface kstatRepository
{
	public Set			getModuleNames();
	public Set			getNamesInModule( String moduleName );
	public kstat		getkstat( String moduleName, String name );
};

