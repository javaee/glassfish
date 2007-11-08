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


package com.sun.enterprise.management.support.oldconfig;


/**
	Generated: Thu Apr 15 00:03:00 PDT 2004
	Generated from:
	com.sun.appserv:type=module-log-levels,config=server-config,category=config
*/





public interface OldModuleLogLevelsMBean 
{
	public String	getAdmin();
	public void	setAdmin( final String value );

	public String	getClassloader();
	public void	setClassloader( final String value );

	public String	getCmpContainer();
	public void	setCmpContainer( final String value );

	public String	getCmp();
	public void	setCmp( final String value );

	public String	getConfiguration();
	public void	setConfiguration( final String value );

	public String	getConnector();
	public void	setConnector( final String value );

	public String	getCorba();
	public void	setCorba( final String value );

	public String	getDeployment();
	public void	setDeployment( final String value );

	public String	getEjbContainer();
	public void	setEjbContainer( final String value );

	public String	getJavamail();
	public void	setJavamail( final String value );

	public String	getJaxr();
	public void	setJaxr( final String value );

	public String	getJaxrpc();
	public void	setJaxrpc( final String value );

	public String	getJdo();
	public void	setJdo( final String value );

	public String	getJms();
	public void	setJms( final String value );

	public String	getJta();
	public void	setJta( final String value );

	public String	getJts();
	public void	setJts( final String value );

	public String	getMdbContainer();
	public void	setMdbContainer( final String value );

	public String	getNaming();
	public void	setNaming( final String value );

	public String	getResourceAdapter();
	public void	setResourceAdapter( final String value );

	public String	getRoot();
	public void	setRoot( final String value );

	public String	getSaaj();
	public void	setSaaj( final String value );

	public String	getSecurity();
	public void	setSecurity( final String value );

	public String	getServer();
	public void	setServer( final String value );

	public String	getVerifier();
	public void	setVerifier( final String value );

	public String	getWebContainer();
	public void	setWebContainer( final String value );


// -------------------- Operations --------------------
	public boolean	destroyConfigElement();
	public String	getDefaultAttributeValue( final String attributeName );
	
	
	

}