/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/security/sasl/provider/PLAINServerProvider.java,v 1.1 2004/03/09 00:44:44 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/03/09 00:44:44 $
 */
package com.sun.cli.jmxcmd.security.sasl.provider;

import java.security.Provider;

public final class PLAINServerProvider extends Provider
{
    	public
    PLAINServerProvider()
    {
		super("SaslServerFactory", 1.0, "SASL/PLAIN SERVER MECHANISM");
		put("SaslServerFactory." + PLAINServerFactory.PLAIN, PLAINServerFactory.class.getName() );
    }
}
