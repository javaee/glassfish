/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/JCmdStringifierRegistryIniter.java,v 1.3 2004/03/13 01:47:19 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/03/13 01:47:19 $
 */
 
package com.sun.cli.jcmd;

import org.glassfish.admin.amx.util.stringifier.StringifierRegistryIniter;
import org.glassfish.admin.amx.util.stringifier.StringifierRegistryIniterImpl;
import org.glassfish.admin.amx.util.stringifier.StringifierRegistry;

/**
	Registers all JCmd-specific Stringifiers.
 */
public class JCmdStringifierRegistryIniter extends StringifierRegistryIniterImpl
{
		public
	JCmdStringifierRegistryIniter( StringifierRegistry registry )
	{
		super( registry );
		
		// no JCmd-specific initers yet
	}
	
}



