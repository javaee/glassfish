/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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

