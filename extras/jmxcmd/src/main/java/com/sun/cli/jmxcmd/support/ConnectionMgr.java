/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/ConnectionMgr.java,v 1.4 2004/05/01 01:09:48 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/05/01 01:09:48 $
 */
 

package com.sun.cli.jmxcmd.support;

import java.util.Set;

import javax.management.remote.JMXConnector;
import com.sun.cli.jmxcmd.spi.JMXConnectorProvider;

public interface ConnectionMgr
{
	public JMXConnector	connect( String name, ConnectInfo connectInfo, boolean forceNew )
				throws Exception;
	
	public void	close( String name ) throws java.io.IOException;
	
	public Set					getNames();
	public ConnectInfo			getConnectInfo( String name );
	
	public void					addProvider( Class<? extends JMXConnectorProvider> providerClass )
									throws IllegalAccessException,
										InstantiationException, ClassNotFoundException;
	public void					removeProvider( Class<? extends JMXConnectorProvider> provider );
	public JMXConnectorProvider []	getProviders();
};

