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
 
/*
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/support/oldconfig/OldDomainMBean.java,v 1.2 2005/12/25 03:41:00 tcfujii Exp $
 * $Revision: 1.2 $
 * $Date: 2005/12/25 03:41:00 $
 */
package com.sun.enterprise.management.support.oldconfig;
 
 
 /**
	Generated: Wed Apr 14 23:43:29 PDT 2004
	Generated from:
	com.sun.appserv:type=domain,category=config
*/

import javax.management.ObjectName;
import javax.management.AttributeList;

/**
	Implementing class was: com.sun.enterprise.admin.mbeans.DomainMBean
*/

public interface OldDomainMBean  extends OldProperties
{
	public String	getApplicationRoot();
	public void	setApplicationRoot( final String value );

	public String	getLocale();
	public void	setLocale( final String value );

	public String	getLogRoot();
	public void	setLogRoot( final String value );

	public String resolveTokens(String value, String instanceName) throws Exception;
	
// -------------------- Operations --------------------
	public ObjectName	createApplications( final AttributeList attribute_list );
	public ObjectName	createClusters( final AttributeList attribute_list );
	public ObjectName	createConfigs( final AttributeList attribute_list );
	public ObjectName	createLbConfigurations( final AttributeList attribute_list );
	public ObjectName	createLoadbalancers( final AttributeList attribute_list );
	public ObjectName	createNodeControllers( final AttributeList attribute_list );
	public ObjectName	createResources( final AttributeList attribute_list );
	public ObjectName	createServers( final AttributeList attribute_list );
	public boolean	destroyConfigElement();
	public ObjectName	getApplications();
	public ObjectName	getClusters();
	public String	getConfigDir();
	public ObjectName	getConfigs();
	public String	getDefaultAttributeValue( final String attributeName );
	public AttributeList	getDefaultAttributeValues( final String param1, final String[] param2 );
	public AttributeList	getDefaultCustomProperties( final String param1, final AttributeList param2 );
	public ObjectName	getLbConfigurations();
	public ObjectName	getLoadbalancers();
	public ObjectName	getNodeControllers();
	
	
	public ObjectName	getResources();
	public ObjectName	getServers();
	public boolean	isAutoStartEnabled();
	public boolean	isAutoStartSupported();
	public void	removeApplications();
	public void	removeClusters();
	public void	removeConfigs();
	public void	removeLbConfigurations();
	public void	removeLoadbalancers();
	public void	removeNodeControllers();
	public void	removeResources();
	public void	removeServers();
	public void	setAutoStartEnabled( final boolean param1 );
	

}