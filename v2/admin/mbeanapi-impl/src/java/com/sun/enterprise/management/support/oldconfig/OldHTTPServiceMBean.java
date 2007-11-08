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
	Generated: Wed May 05 13:13:12 PDT 2004
	Generated from:
	com.sun.appserv:type=http-service,config=default-config,category=config
	com.sun.appserv:type=http-service,config=server-config,category=config
*/

package com.sun.enterprise.management.support.oldconfig;

import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.AttributeList;
import javax.management.NotificationFilter;


public interface OldHTTPServiceMBean extends OldProperties
{

// -------------------- Operations --------------------
	public void	addNotificationListener( final NotificationListener param1, final NotificationFilter param2, final Object param3 );
	public ObjectName	createAccessLog( final AttributeList attribute_list );
	public ObjectName	createConnectionPool( final AttributeList attribute_list );
	public ObjectName	createHttpFileCache( final AttributeList attribute_list );
	public ObjectName	createHttpListener( final AttributeList attribute_list );
	public ObjectName	createHttpProtocol( final AttributeList attribute_list );
	public ObjectName	createKeepAlive( final AttributeList attribute_list );
	public ObjectName	createRequestProcessing( final AttributeList attribute_list );
	public ObjectName	createVirtualServer( final AttributeList attribute_list );
	public boolean	destroyConfigElement();
	public ObjectName	getAccessLog();
	public ObjectName	getConnectionPool();
	public ObjectName	getHttpFileCache();
	public javax.management.ObjectName[]	getHttpListener();
	public ObjectName	getHttpListenerById( final String key );
	public ObjectName	getHttpProtocol();
	public ObjectName	getKeepAlive();
	public javax.management.MBeanNotificationInfo[]	getNotificationInfo();
	public ObjectName	getRequestProcessing();
	public javax.management.ObjectName[]	getVirtualServer();
	public ObjectName	getVirtualServerById( final String key );
	public void	removeAccessLog();
	public void	removeConnectionPool();
	public void	removeHttpFileCache();
	public void	removeHttpListenerById( final String key );
	public void	removeHttpProtocol();
	public void	removeKeepAlive();
	public void	removeNotificationListener( final NotificationListener param1 );
	public void	removeNotificationListener( final NotificationListener param1, final NotificationFilter param2, final Object param3 );
	public void	removeRequestProcessing();
	public void	removeVirtualServerById( final String key );
	public void	sendNotification( final javax.management.Notification param1 );
}