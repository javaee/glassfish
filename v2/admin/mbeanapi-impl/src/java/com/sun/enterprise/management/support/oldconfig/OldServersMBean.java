/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

/**
	Generated: Thu Jun 10 14:46:04 PDT 2004
	Generated from:
	com.sun.appserv:type=servers,category=config
*/

package com.sun.enterprise.management.support.oldconfig;

import javax.management.ObjectName;
import javax.management.AttributeList;

import com.sun.enterprise.admin.servermgmt.RuntimeStatus;

/**
	Implementing class was: com.sun.enterprise.ee.admin.mbeans.ServersConfigMBean
*/

public interface OldServersMBean
{

// -------------------- Operations --------------------
	public void	clearRuntimeStatus( final String param1 );
	public ObjectName	createServer( final AttributeList attribute_list );
	public ObjectName	createServerInstance( final String param1, final String param2, final String param3, final String param4, final java.util.Properties param5 );
	public void	deleteServerInstance( final String param1 );
	public boolean	destroyConfigElement();
	public com.sun.enterprise.admin.servermgmt.RuntimeStatus	getRuntimeStatus( final String serverName );
	public ObjectName[]	getServer();
	public ObjectName	getServerByName( final String key );
	public ObjectName	listDASServerInstance();
	public String	listDASServerInstanceAsString( final boolean param1 );
	public ObjectName[]	listServerInstances( final String param1 );
	public String[]	listServerInstancesAsString( final String param1, final boolean param2 );
	public ObjectName[]	listUnclusteredServerInstances( boolean omitDAS );
	public ObjectName[]	listUnclusteredServerInstances();
	public String[]	listUnclusteredServerInstancesAsString( final boolean omitDAS );
	public void	removeServerByName( final String key );
	public void	startServerInstance( final String param1 );
	public void	stopServerInstance( final String param1 );

}