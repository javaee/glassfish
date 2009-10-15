/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/security/rmi/RMISSLClientSocketFactoryEnv.java,v 1.8 2004/07/29 20:19:17 llc Exp $
 * $Revision: 1.8 $
 * $Date: 2004/07/29 20:19:17 $
 */
package com.sun.cli.jmxcmd.security.rmi;

import java.io.File;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.HandshakeCompletedListener;

	
public interface RMISSLClientSocketFactoryEnv
{
   	public KeyManager[]	getKeyManagers( );
    
    public TrustManager[]	getTrustManagers( );
    
    public HandshakeCompletedListener getHandshakeCompletedListener( );
    
    public void		setTrace( final boolean trace );
    public boolean	getTrace(  );
}
